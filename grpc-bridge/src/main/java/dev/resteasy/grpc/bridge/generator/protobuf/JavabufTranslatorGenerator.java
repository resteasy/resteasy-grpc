package dev.resteasy.grpc.bridge.generator.protobuf;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.logging.Logger;

import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;

import dev.resteasy.grpc.bridge.runtime.Utility;
import dev.resteasy.grpc.bridge.runtime.protobuf.AssignFromJavabuf;
import dev.resteasy.grpc.bridge.runtime.protobuf.AssignToJavabuf;
import dev.resteasy.grpc.bridge.runtime.protobuf.JavabufTranslator;
import dev.resteasy.grpc.bridge.runtime.protobuf.ReturnJavaClass;
import dev.resteasy.grpc.bridge.runtime.protobuf.TranslateFromJavabuf;
import dev.resteasy.grpc.bridge.runtime.protobuf.TranslateToJavabuf;

/**
 * Generates a class, &lt;prefix&gt;JavabufTranslator, that can translate back and forth between a Java class and
 * its protobuf representation in Java (i.e., its javabuf version).
 * <p/>
 * For example,
 * <p/>
 * 1. Start with two classes:
 *
 * <pre>
 * package example.grpc;
 *
 * public class CC3 {
 *    String s;
 *    public CC3(String s) {
 *       this.s = s;
 *    }
 * }
 *
 * package example.grpc;
 *
 * public class CC2 extends CC3 {
 *    int j;
 *    public CC2(String s, int j) {
 *       super(s);
 *       this.j = j;
 *    }
 * }
 * </pre>
 *
 * 2. Generate an Example.proto file from the two classes:
 *
 * <pre>
 * syntax = "proto3";
 * package example.grpc;
 * option java_package = "example.grpc";
 * option java_outer_classname = "Example_proto";
 *
 * message example.grpc___CC3 {
 *   string s = 1;
 * }
 *
 * message example.grpc___CC2 {
 *   int32 j = 2;
 *   example.grpc___CC3 cC3___super = 3;
 * }
 * </pre>
 *
 * 3. Use the protobuf compiler to generate class {@code Example_proto} with the javabuf versions
 * {@code Example_proto.example.grpc___CC2} and {@code Example_proto.example.grpc___CC3} of
 * {@code CC2} and {@code CC3}, respectively.
 * <p/>
 *
 * Now, {@code JavabufTranslatorGenerator} will generate an {@code ExampleJavabufTranslator} class that
 * can be used as follows:
 *
 * <pre>
 * &#064;Test
 * public void test() {
 *
 *     // 1. Create a CC2.
 *     CC2 cc2 = new CC2("abc", 19);
 *
 *     // 2. Translate to javabuf form
 *     Message message = ExampleJavabufTranslator.translateToJavabuf(cc2);
 *
 *     // 3. Manually create a javabuf representation of the same CC2 and demonstrate they're the same.
 *     Example_proto.example.grpc___CC3 cc3Message = Example_proto.example.grpc___CC3.newBuilder().setS("abc").build();
 *     Example_proto.example.grpc___CC2 cc2Message = Example_proto.example.grpc___CC2.newBuilder().setJ(19)
 *             .setCC3Super(cc3Message).build();
 *     Assert.assertEquals(message, cc2Message);
 *
 *     // 4. A. Translate the javabuf object created in step 2 back to its original java form.
 *     //    B. Demonstrate it's the same as the java object created in step 1.
 *     CC2 cc2_new = (CC2) ExampleJavabufTranslator.translateFromJavabuf(message);
 *     Assert.assertEquals(cc2, cc2_new);
 * }
 * </pre>
 */
public class JavabufTranslatorGenerator {

    private static Logger logger = Logger.getLogger(JavabufTranslatorGenerator.class);
    private static final String LS = System.lineSeparator();

    public interface AssignTo {
        void assign(Object from, DynamicMessage.Builder builder);
    }

    public interface AssignFrom {
        void assign(Message message, Object object);
    }

    private static final Map<String, Class<?>> PRIMITIVE_WRAPPER_TYPES = new HashMap<String, Class<?>>();
    private static final Map<String, String> GET_METHODS = new HashMap<String, String>();
    private static final Map<Class<?>, String> PRIMITIVE_DEFAULTS = new HashMap<Class<?>, String>();
    private static final Map<String, String> WRAPPER_TO_PRIMITIVE = new HashMap<String, String>();
    private static final Set<String> NULLABLE_WRAPPERS = new HashSet<String>();
    private static final Set<String> JAVA_WRAPPER_TYPES = new HashSet<String>();
    private static final Set<Class<?>> arrayClasses = new HashSet<Class<?>>();

    //    private static final String ArrayWrapperBuilderMethod = "   public void set_%1$s_Array(Object[] array) {%n"
    //            + "      dev_resteasy_grpc_arrays___ArrayHolder.Builder ahb = dev_resteasy_grpc_arrays___ArrayHolder.newBuilder();%n"
    //            + "      %1$s_Array.Builder ab = %1$s_Array.newBuilder();%n"
    //            + "      %1$s_wrapper.Builder wb = %1$s_wrapper.newBuilder();%n"
    //            + "      for (int i = 0; i < Array.getLength(array); i++) {%n"
    //            + "         if (array[i] == null) {%n"
    //            + "            wb.clear().setNoneField(Array_proto.dev_resteasy_grpc_arrays___NONE.newBuilder().build());%n"
    //            + "         } else {%n"
    //            + "            wb.clear().set%2$sField((%1$s) INSTANCE.translateToJavabuf(array[i]));%n"
    //            + "         }%n"
    //            + "         ab.add%2$sWrapperField(wb);%n"
    //            + "      }%n"
    //            + "      ahb.set%2$sArrayField(ab);%n"
    //            + "   }";
    //dev_resteasy_grpc_arrays___Integer___Array.Builder                 elementBuilder.setJavaUtilHashMapHIDDENNodeField((java_util_HashMap_HIDDEN_Node) INSTANCE.translateToJavabuf(aa[i]));

    //    private static final String ArrayBuilderClass = //dev_resteasy_grpc_arrays___short
    //            "      public Message assignToJavabuf(Object x) {%n"
    //                    + "         if (x == null) {%n"
    //                    + "            return null;%n"
    //                    + "         }%n"
    //                    + "         %1$s[] aa = (%1$s[]) x;%n"
    //                    + "         %2$s___%3$sArray.Builder builder = %2$s___%3$sArray.newBuilder();%n"
    //                    + "         for (int i = 0; i < aa.length; i++) {%n"
    //                    + "            builder.add%4$sField(aa[i]);%n"
    //                    + "            if (aa[i] == null) {%n"
    //                    + "               builder.addStringField(dev_resteasy_grpc_arrays___NONE);%n"
    //                    + "            } else {%n"
    //                    + "               builder.addStringField((%2$s) INSTANCE.translateToJavabuf(aa[i]));%n"
    //                    + "            }%n"
    //                    + "         }%n"
    //                    + "         return builder.build();%n"
    //                    + "      }%n"
    //                    + "%n"
    //                    + "      public void clear() {%n"
    //                    + "         //%n"
    //                    + "      }%n"
    //    //                    + "   }%n%n"
    //    ;
    private static final String ArrayPrimitiveBuilderClass = "      public Message assignToJavabuf(Object x) {%n"
            + "         if (x == null) {%n"
            + "            return null;%n"
            + "         }%n"
            + "         %1$s[] aa = (%1$s[]) x;%n"
            + "         dev_resteasy_grpc_arrays___%2$s___Array.Builder arrayBuilder = dev_resteasy_grpc_arrays___%2$s___Array.newBuilder();%n"
            + "         for (int i = 0; i < aa.length; i++) {%n"
            + "            arrayBuilder.add%3$sField(aa[i]);%n"
            + "         }%n"
            + "         return arrayBuilder.build();%n"
            + "      }%n"
            + "%n"
            + "      public void clear() {%n"
            + "         //%n"
            + "      }%n";

    private static final String ArrayPrimitiveWrapperBuilderClass = "      public Message assignToJavabuf(Object x) {%n"
            + "         if (x == null) {%n"
            + "            return null;%n"
            + "         }%n"
            + "         %1$s[] aa = (%1$s[]) x;%n"
            + "         %2$s___wrapper.Builder elementBuilder = %2$s___wrapper.newBuilder();%n"
            + "         %2$s___WArray.Builder arrayBuilder = %2$s___WArray.newBuilder();%n"
            + "         for (int i = 0; i < aa.length; i++) {%n"
            + "            if (aa[i] == null) {%n"
            + "               elementBuilder.clear().setNoneField(dev_resteasy_grpc_arrays___NONE.newBuilder());%n"
            + "            } else {%n"
            + "               elementBuilder.set%3$sField(aa[i]);%n"
            + "            }%n"
            + "            arrayBuilder.clear().add%3$sField(elementBuilder);%n"
            + "         }%n"
            + "         return arrayBuilder.build();%n"
            + "      }%n"
            + "%n"
            + "      public void clear() {%n"
            + "         //%n"
            + "      }%n";

    private static final String ArrayWrapperBuilderClass = "      public Message assignToJavabuf(Object x) {%n"
            + "         if (x == null) {%n"
            + "            return null;%n"
            + "         }%n"
            + "         %1$s[] aa = (%1$s[]) x;%n"
            + "         %2$s___wrapper.Builder elementBuilder = %2$s___wrapper.newBuilder();%n"
            + "         %2$s___WArray.Builder arrayBuilder = %2$s___WArray.newBuilder();%n"
            + "         for (int i = 0; i < aa.length; i++) {%n"
            + "            if (aa[i] == null) {%n"
            + "               elementBuilder.clear().setNoneField(dev_resteasy_grpc_arrays___NONE.newBuilder());%n"
            + "            } else {%n"
            + "               elementBuilder.set%3$sField((%1$s) INSTANCE.translateToJavabuf(aa[i]));%n"
            + "            }%n"
            + "            arrayBuilder.clear().add%3$sField(elementBuilder);%n"
            + "         }%n"
            + "         return arrayBuilder.build();%n"
            + "      }%n"
            + "%n"
            + "      public void clear() {%n"
            + "         //%n"
            + "      }%n";

    private static final String ArrayWrapperBuilderClassForBytes = "      public Message assignToJavabuf(Object x) {%n"
            + "         if (x == null) {%n"
            + "            return null;%n"
            + "         }%n"
            + "         %1$s[] aa = (%1$s[]) x;%n"
            + "         %2$s___wrapper.Builder elementBuilder = %2$s___wrapper.newBuilder();%n"
            + "         %2$s___WArray.Builder arrayBuilder = %2$s___WArray.newBuilder();%n"
            + "         for (int i = 0; i < aa.length; i++) {%n"
            + "            if (aa[i] == null) {%n"
            + "               elementBuilder.clear().setNoneField(dev_resteasy_grpc_arrays___NONE.newBuilder());%n"
            + "            } else {%n"
            + "               byte[] bs = new byte[] { aa[i].byteValue() };%n"
            + "               elementBuilder.clear().setByteField(ByteString.copyFrom(bs));%n"
            + "            }%n"
            + "            arrayBuilder.clear().add%1$sField(elementBuilder);%n"
            + "         }%n"
            + "         return arrayBuilder.build();%n"
            + "      }%n"
            + "%n"
            + "      public void clear() {%n"
            + "         //%n"
            + "      }%n";

    private static final String ArrayWrapperBuilderClassForCharacters = "      public Message assignToJavabuf(Object x) {%n"
            + "         if (x == null) {%n"
            + "            return null;%n"
            + "         }%n"
            + "         %1$s[] aa = (%1$s[]) x;%n"
            + "         %2$s___wrapper.Builder elementBuilder = %2$s___wrapper.newBuilder();%n"
            + "         %2$s___WArray.Builder arrayBuilder = %2$s___WArray.newBuilder();%n"
            + "         for (int i = 0; i < aa.length; i++) {%n"
            + "            if (aa[i] == null) {%n"
            + "               elementBuilder.clear().setNoneField(dev_resteasy_grpc_arrays___NONE.newBuilder());%n"
            + "            } else {%n"
            + "               elementBuilder.set%1$sField(String.valueOf(aa[i]));%n"
            + "            }%n"
            + "            arrayBuilder.clear().add%1$sField(elementBuilder);%n"
            + "         }%n"
            + "         return arrayBuilder.build();%n"
            + "      }%n"
            + "%n"
            + "      public void clear() {%n"
            + "         //%n"
            + "      }%n";

    /*
     * // case "java.lang.Character": {
     * // Character[] cs = (Character[]) o;
     * // dev_resteasy_grpc_arrays___CharArray.Builder builder = dev_resteasy_grpc_arrays___CharArray.newBuilder();
     * // for (int i = 0; i < cs.length; i++) {
     * // builder.addCharField(String.valueOf(cs[i]));
     * // }
     * // return ahBuilder.setCharArrayField(builder).setComponentClass("java.lang.Character").build();
     * // }
     *
     * byte[] bs = new byte[] { aa[i].byteValue() };
     * elementBuilder.clear().setByteField(ByteString.copyFrom(bs));
     */
    /*
     *
     * public void set_java_util_HashMap_HIDDEN_Node_Array(Object[] array) {
     * dev_resteasy_grpc_arrays___ArrayHolder.Builder ahb = dev_resteasy_grpc_arrays___ArrayHolder.newBuilder();
     * java_util_HashMap_HIDDEN_Node_Array.Builder ab = java_util_HashMap_HIDDEN_Node_Array.newBuilder();
     * java_util_HashMap_HIDDEN_Node_wrapper.Builder wb = java_util_HashMap_HIDDEN_Node_wrapper.newBuilder();
     * for (int i = 0; i < Array.getLength(array); i++) {
     * if (array[i] == null) {
     * wb.clear().setNoneField(Array_proto.dev_resteasy_grpc_arrays___NONE.newBuilder().build());
     * } else {
     * wb.clear().setJavaUtilHashMapHIDDENNodeField((java_util_HashMap_HIDDEN_Node) INSTANCE.translateToJavabuf(array[i]));
     * }
     * ab.addJavaUtilHashMapHIDDENNodeWrapperField(wb);
     * }
     * ahb.setJavaUtilHashMapHIDDENNodeArrayField(ab);
     * }
     *
     * java_util_HashMap_HIDDEN_Node
     * JavaUtilHashMapHIDDENNode
     * public void set_java_util_HashMap_HIDDEN_Node_Array(Object[] array) {
     * dev_resteasy_grpc_arrays___ArrayHolder.Builder ahb = dev_resteasy_grpc_arrays___ArrayHolder.newBuilder();
     * java_util_HashMap_HIDDEN_Node_Array.Builder ab = java_util_HashMap_HIDDEN_Node_Array.newBuilder();
     * java_util_HashMap_HIDDEN_Node_wrapper.Builder wb = java_util_HashMap_HIDDEN_Node_wrapper.newBuilder();
     * for (int i = 0; i < Array.getLength(array); i++) {
     * if (array[i] == null) {
     * wb.clear().setNoneField(Array_proto.dev_resteasy_grpc_arrays___NONE.newBuilder().build());
     * } else {
     * wb.clear().setJavaUtilHashMapHIDDENNodeField((java_util_HashMap_HIDDEN_Node) INSTANCE.translateToJavabuf(array[i]));
     * }
     * ab.addJavaUtilHashMapHIDDENNodeWrapperField(wb);
     * }
     * ahb.setJavaUtilHashMapHIDDENNodeArrayField(ab);
     * }
     *
     * public void set_%1$s_Array(Object[] array) {
     * dev_resteasy_grpc_arrays___ArrayHolder.Builder ahb = dev_resteasy_grpc_arrays___ArrayHolder.newBuilder();
     * %1$s_Array.Builder ab = %1$s_Array.newBuilder();
     * %1$s_wrapper.Builder wb = %1$s_wrapper.newBuilder();
     * for (int i = 0; i < Array.getLength(array); i++) {
     * if (array[i] == null) {
     * wb.clear().setNoneField(Array_proto.dev_resteasy_grpc_arrays___NONE.newBuilder().build());
     * } else {
     * wb.clear().set%2$sField((java_util_HashMap_HIDDEN_Node) INSTANCE.translateToJavabuf(array[i]));
     * }
     * ab.add%2$sWrapperField(wb);
     * }
     * ahb.setJavaUtilHashMapHIDDENNodeArrayField(ab);
     * }
     *
     */

    static {
        PRIMITIVE_WRAPPER_TYPES.put("gByte", byte.class);
        PRIMITIVE_WRAPPER_TYPES.put("gShort", short.class);
        PRIMITIVE_WRAPPER_TYPES.put("gInteger", int.class);
        PRIMITIVE_WRAPPER_TYPES.put("gLong", long.class);
        PRIMITIVE_WRAPPER_TYPES.put("gFloat", float.class);
        PRIMITIVE_WRAPPER_TYPES.put("gDouble", double.class);
        PRIMITIVE_WRAPPER_TYPES.put("gBoolean", boolean.class);
        PRIMITIVE_WRAPPER_TYPES.put("gCharacter", char.class);
        PRIMITIVE_WRAPPER_TYPES.put("gString", String.class);

        GET_METHODS.put("Byte", ".byteValue()");
        GET_METHODS.put("Short", ".shortValue()");
        GET_METHODS.put("Integer", ".intValue()");
        GET_METHODS.put("Long", ".longValue()");
        GET_METHODS.put("Float", ".floatValue()");
        GET_METHODS.put("Double", ".doubleValue()");
        GET_METHODS.put("Boolean", ".booleanValue()");
        GET_METHODS.put("Character", ".toString()");
        GET_METHODS.put("String", "");

        PRIMITIVE_DEFAULTS.put(boolean.class, "false");
        PRIMITIVE_DEFAULTS.put(byte.class, "(byte)0");
        PRIMITIVE_DEFAULTS.put(short.class, "(short)0");
        PRIMITIVE_DEFAULTS.put(int.class, "0");
        PRIMITIVE_DEFAULTS.put(long.class, "0L");
        PRIMITIVE_DEFAULTS.put(float.class, "0.0f");
        PRIMITIVE_DEFAULTS.put(double.class, "0.0d");
        PRIMITIVE_DEFAULTS.put(char.class, "'\\u0000'");

        WRAPPER_TO_PRIMITIVE.put("Boolean", "boolean");
        WRAPPER_TO_PRIMITIVE.put("Byte", "byte");
        WRAPPER_TO_PRIMITIVE.put("Short", "short");
        WRAPPER_TO_PRIMITIVE.put("Integer", "int");
        WRAPPER_TO_PRIMITIVE.put("Long", "long");
        WRAPPER_TO_PRIMITIVE.put("Float", "float");
        WRAPPER_TO_PRIMITIVE.put("Double", "double");
        WRAPPER_TO_PRIMITIVE.put("Character", "char");
        WRAPPER_TO_PRIMITIVE.put("String", "String");
        WRAPPER_TO_PRIMITIVE.put("Any", "Any");

        NULLABLE_WRAPPERS.add("dev_resteasy_grpc_arrays___Boolean");
        NULLABLE_WRAPPERS.add("dev_resteasy_grpc_arrays___Byte");
        NULLABLE_WRAPPERS.add("dev_resteasy_grpc_arrays___Short");
        NULLABLE_WRAPPERS.add("dev_resteasy_grpc_arrays___Integer");
        NULLABLE_WRAPPERS.add("dev_resteasy_grpc_arrays___Long");
        NULLABLE_WRAPPERS.add("dev_resteasy_grpc_arrays___Float");
        NULLABLE_WRAPPERS.add("dev_resteasy_grpc_arrays___Double");
        NULLABLE_WRAPPERS.add("dev_resteasy_grpc_arrays___Character");
        NULLABLE_WRAPPERS.add("dev_resteasy_grpc_arrays___String");

        JAVA_WRAPPER_TYPES.add("Byte");
        JAVA_WRAPPER_TYPES.add("Short");
        JAVA_WRAPPER_TYPES.add("Integer");
        JAVA_WRAPPER_TYPES.add("Long");
        JAVA_WRAPPER_TYPES.add("Float");
        JAVA_WRAPPER_TYPES.add("Double");
        JAVA_WRAPPER_TYPES.add("Boolean");
        JAVA_WRAPPER_TYPES.add("Character");
        JAVA_WRAPPER_TYPES.add("String");
    }

    public static void main(String[] args) {
        System.out.println("!!! JavabufTranslatorGenerator !!!");
        if (args == null || args.length != 3) {
            logger.info("need two args:");
            logger.info("  arg[0]: root directory");
            logger.info("  arg[1]: javabuf wrapper class name");
            logger.info("  arg[2]: prefix");
            return;
        }
        try {
            int index = args[1].lastIndexOf('.');
            String simpleName = index < 0 ? args[1] : args[1].substring(index + 1);
            String translatorClass = simpleName + "JavabufTranslator";
            Class<?>[] wrappedClasses = getWrappedClasses(args);
            for (Class c : wrappedClasses) {
                System.out.println("wrappedClass: " + c.getName());
            }
            StringBuilder sb = new StringBuilder();
            classHeader(args, translatorClass, wrappedClasses, sb);
            classBody(args, wrappedClasses, sb);
            finishClass(sb);
            writeTranslatorClass(args, translatorClass, sb);
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
        }
    }

    private static Class<?>[] getWrappedClasses(String[] args) throws ClassNotFoundException {
        List<Class<?>> wrapperClasses = getWrapperClasses(args);
        List<Class<?>> wrappedClasses = new ArrayList<Class<?>>();
        for (Class<?> clazz : wrapperClasses) {
            wrappedClasses.addAll(Arrays.asList(clazz.getClasses()));
        }
        return wrappedClasses.toArray(new Class<?>[wrappedClasses.size()]);
    }

    private static List<Class<?>> getWrapperClasses(String[] args) throws ClassNotFoundException {
        List<Class<?>> list = new ArrayList<Class<?>>();
        Class<?> wrapperClass = Class.forName(args[1] + "_proto", true, Thread.currentThread()
                .getContextClassLoader());
        list.add(wrapperClass);
        getArrayClasses(wrapperClass);
        try {
            Class<?> arrayWrapperClass = Class.forName("dev.resteasy.grpc.arrays.Array_proto", true,
                    Thread.currentThread().getContextClassLoader());
            System.out.println("getWrapperClasses(): got " + arrayWrapperClass.getName());
            list.add(arrayWrapperClass);
            getArrayClasses(arrayWrapperClass);
            return list;
        } catch (Exception ignore) {
            // Array_proto class is not available: ignore
            return list;
        }
    }

    /*
     * for (Class<?> innerClazz : clazz.getDeclaredClasses()) {
     * System.out.println("innerClazz: " + innerClazz.getName());
     * if (innerClazz.getName().endsWith("$Builder")) {
     * builderClazz = innerClazz;
     * for (Method m : innerClazz.getDeclaredMethods()) {
     * if (m.getName().startsWith("set")) {
     * System.out.println("m set: " + m.getName());
     * }
     * }
     * }
     * if (innerClazz.getName().endsWith("MessageTypeCase")) {
     * enumClazz = innerClazz;
     * }
     */

    private static void getArrayClasses(Class<?> clazz) {
        System.out.println("WRAPPER CLASS: " + clazz.getName());
        List<String> classnames = new ArrayList<String>();
        for (Class<?> c : clazz.getDeclaredClasses()) {
            System.out.println("checking WRAPPER CLASS: " + c.getName());
            if ("dev_resteasy_grpc_arrays___ArrayHolder".equals(c.getSimpleName())) {
                for (Class<?> c2 : c.getDeclaredClasses()) {
                    System.out.println("inner: " + c2.getName() + ", " + c2.getSimpleName());
                    if ("MessageTypeCase".equals(c2.getSimpleName())) {
                        for (Object o : c2.getEnumConstants()) {
                            if (o.toString().contains("_FIELD")) {
                                System.out.println(o.toString().substring(0, o.toString().indexOf("_FIELD")));
                                classnames.add(o.toString().substring(0, o.toString().indexOf("_FIELD")).toLowerCase());
                            }
                        }
                    }
                }
            }
        }
        for (Class<?> c : clazz.getDeclaredClasses()) {
            if (classnames.contains(c.getSimpleName().toLowerCase())) {
                arrayClasses.add(c);
                System.out.println("ADDING array class: " + c.getName());
            }
        }
    }

    private static void classHeader(String[] args, String translatorClass, Class<?>[] wrappedClasses, StringBuilder sb) {
        sb.append("package ").append(wrappedClasses[0].getPackage().getName()).append(";" + LS + LS);
        imports(wrappedClasses, sb, args);
        sb.append("public class ")
                .append(translatorClass)
                .append(" implements JavabufTranslator {" + LS);
    }

    private static void imports(Class<?>[] wrappedClasses, StringBuilder sb, String[] args) {
        sb.append("import java.lang.reflect.Array;" + LS)
                .append("import java.lang.reflect.Constructor;" + LS)
                .append("import java.lang.reflect.Field;" + LS)
                .append("import java.util.ArrayList;" + LS)
                .append("import java.util.HashMap;" + LS)
                .append("import java.util.HashSet;" + LS)
                .append("import java.util.List;" + LS)
                .append("import java.util.Map;" + LS)
                .append("import java.util.Set;" + LS)
                .append("import com.google.protobuf.Any;" + LS)
                .append("import com.google.protobuf.ByteString;" + LS)
                .append("import com.google.protobuf.Descriptors;" + LS)
                .append("import com.google.protobuf.Descriptors.Descriptor;" + LS)
                .append("import com.google.protobuf.Descriptors.FieldDescriptor;" + LS)
                .append("import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;" + LS)
                .append("import com.google.protobuf.Internal.IntList;" + LS)
                .append("import com.google.protobuf.Message;" + LS)
                //                .append("import ").append(ArrayUtility.class.getCanonicalName()).append(";" + LS)
                .append("import dev.resteasy.grpc.arrays.ArrayUtility;" + LS)
                .append("import ").append(Utility.class.getCanonicalName()).append(";" + LS)
                .append("import ").append(AssignFromJavabuf.class.getCanonicalName()).append(";" + LS)
                .append("import ").append(AssignToJavabuf.class.getCanonicalName()).append(";" + LS)
                .append("import ").append(JavabufTranslator.class.getCanonicalName()).append(";" + LS)
                .append("import ").append(ReturnJavaClass.class.getCanonicalName()).append(";" + LS)
                .append("import ").append(TranslateFromJavabuf.class.getCanonicalName()).append(";" + LS)
                .append("import ").append(TranslateToJavabuf.class.getCanonicalName()).append(";" + LS)
                //                .append("import ").append(args[2]).append(".dev_resteasy_grpc_arrays___ArrayHolder;" + LS)
                .append("import dev.resteasy.grpc.arrays.Array_proto;" + LS);
        sb.append("//XXXXXXXX HERE XXXXXXXXX" + LS);
        for (Class<?> clazz : wrappedClasses) {
            System.out.println("wrapped: " + clazz.getName() + ", package: " + clazz.getPackageName());
            sb.append("//wrapped: " + clazz.getName() + LS);
            if (clazz.isInterface()) {
                continue;
            }
            //            if ("dev.resteasy.grpc.arrays".equals(clazz.getPackageName())) {
            //                continue;
            //            }
            if (clazz.getSimpleName().endsWith("OrBuilder")) {
                continue;
            }
            //            if ("dev_resteasy_grpc_arrays___ArrayHolder".equals(clazz.getSimpleName())) {
            //                continue;
            //            }
            //            if (clazz.getName().endsWith("_wrapper") || clazz.getName().endsWith("_Array")) {
            //                continue;
            //            }
            //            if (clazz.getName().contains("_HIDDEN_")) {
            //                continue;
            //            }
            String simpleName = clazz.getSimpleName();
            if ("gEmpty".equals(simpleName)) {
                continue;
            }
            String elementType = simpleName.contains("_Array") ? simpleName.substring(0, simpleName.indexOf("_Array")) : "";
            String wrappedType = simpleName.contains("_wrapper") ? simpleName.substring(0, simpleName.indexOf("_wrapper")) : "";
            if (PRIMITIVE_WRAPPER_TYPES.containsKey(simpleName)
                    || PRIMITIVE_WRAPPER_TYPES.containsKey(elementType)
                    || PRIMITIVE_WRAPPER_TYPES.containsKey(wrappedType)) {
                sb.append("import ").append(clazz.getName().replace("$", ".")).append(";" + LS);
            } else if ("GeneralEntityMessage".equals(simpleName)
                    || "GeneralReturnMessage".equals(simpleName)
                    || "ServletInfo".equals(simpleName)
                    || "gNewCookie".equals(simpleName)
                    || "gCookie".equals(simpleName)
                    || "gHeader".equals(simpleName)
                    || "FormMap".equals(simpleName)
                    || "FormValues".equals(simpleName)) {
                continue;
            } else if (clazz.getName().contains("_HIDDEN_")
                    || clazz.getName().endsWith("ELEMENT_WRAPPER")
                    || "dev_resteasy_grpc_arrays___ArrayHolder".equals(simpleName)
                    || "dev_resteasy_grpc_arrays___ArrayHolder___WArray".equals(simpleName)
                    || clazz.getName().endsWith("_Array")
                    || clazz.getName().endsWith("_wrapper")
                    || "dev.resteasy.grpc.arrays".equals(clazz.getPackageName())) {
                sb.append("import ")
                        .append(clazz.getName().replace("$", "."))
                        .append(";" + LS);
            } else {
                System.out.println("imports: " + clazz.getName() + ", " + originalCanonicalClassName(clazz.getName()));
                sb.append("import ")
                        .append(originalCanonicalClassName(clazz.getName()))
                        .append(";" + LS);
                sb.append("import ")
                        .append(clazz.getName().replace("$", "."))
                        .append(";" + LS);
            }
        }
        sb.append("" + LS);
    }

    private static void classBody(String[] args, Class<?>[] wrappedClasses, StringBuilder sb) throws Exception {
        privateVariables(sb, args);
        staticInit(wrappedClasses, sb);
        publicMethods(sb, wrappedClasses[0], args);
        privateMethods(sb, wrappedClasses, args);
        for (Class<?> clazz : wrappedClasses) {
            System.out.println("classBody(): " + clazz.getName());
            if (clazz.isInterface()) {
                continue;
            }
            String simpleName = clazz.getSimpleName();
            if ("GeneralEntityMessage".equals(simpleName) || "GeneralReturnMessage".equals(simpleName)) {
                continue;
            }
            createTranslator(args, clazz, sb);
        }
        createArrayHandlingClasses(sb);
    }

    private static void staticInit(Class<?>[] classes, StringBuilder sb) throws ClassNotFoundException {
        sb.append("   static {" + LS);
        for (Class<?> clazz : classes) {
            if (clazz.isInterface()) {
                continue;
            }
            System.out.println("staticInit(): " + clazz.getName());
            //            if (clazz.getName().endsWith("ELEMENT_WRAPPER")) {
            //                for (Class<?> clazz2 : clazz.getDeclaredClasses()) {
            //
            //                    System.out.println("clazz2: " + clazz2.getName());
            //                    if (clazz2.getName().endsWith("MessageTypeCase")) {
            //                        Object[] os = clazz2.getEnumConstants();
            //                        for (int i = 0; i < os.length; i++) {
            //                            System.out.println("  enum: " + os[i].toString());
            //
            //                        }
            //                    }
            //                }
            //            }
            //            boolean isAbstract = Modifier.isAbstract(clazz.getModifiers());
            //            private static String javabufToJava(String javabufName, String simpleName, boolean canonical) {

            String simpleName = clazz.getSimpleName();
            if ("gEmpty".equals(simpleName)
                    || "GeneralEntityMessage".equals(simpleName)
                    || "GeneralReturnMessage".equals(simpleName)
                    || "ServletInfo".equals(simpleName)
                    || "gNewCookie".equals(simpleName)
                    || "gCookie".equals(simpleName)
                    || "gHeader".equals(simpleName)
                    || "FormMap".equals(simpleName)
                    || "FormValues".equals(simpleName)) {
                continue;
            }
            //            if ("dev.resteasy.grpc.arrays".equals(clazz.getPackageName())) {
            //                continue;
            //            }

            boolean isAbstract = false;
            if (!PRIMITIVE_WRAPPER_TYPES.containsKey(simpleName)) {
                try {
                    isAbstract = isAbstract(clazz);
                    //                    String originalName = originalSimpleName(clazz.getName());
                    //                    String className = javabufToJava(clazz.getName(), originalName, true);
                    //                    System.out.println("staticInit(): " + clazz + ", " + originalName + ", " + className);
                    //                    //                    Class<?> c = Class.forName(originalClassName(simpleName), true,
                    //                    Class<?> c = Class.forName(className, true,
                    //                            Thread.currentThread().getContextClassLoader());
                    //                    isAbstract = Modifier.isAbstract(c.getModifiers());
                    //                    System.out.println("staticInit(): isAbstract: " + isAbstract);
                    //                    System.out.println("c: " + c.getName());
                    //                    //                    if (Modifier.isAbstract(c.getModifiers())) {
                    //                    //                        System.out.println("skipping " + c);
                    //                    //                        continue;
                    //                    //                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    continue;
                }
            }

            System.out.println("staticInit(): simpleName: " + simpleName + ", originalClassName(simpleName): "
                    + originalClassName(simpleName));
            if (simpleName.endsWith("___Array")) {
                sb.append(LS + "      toJavabufMap.put(\"")
                        .append(originalClassName(simpleName) + ".Array" + "\" , new ")
                        //                    .append(".class.getName(), new ")
                        .append(simpleName)
                        .append("_ToJavabuf());" + LS);
            } else if (simpleName.endsWith("___WArray")) {
                sb.append(LS + "      toJavabufMap.put(\"")
                        .append(originalClassName(simpleName) + ".WArray" + "\" , new ")
                        //                    .append(".class.getName(), new ")
                        .append(simpleName)
                        .append("_ToJavabuf());" + LS);
            } else {
                sb.append(LS + "      toJavabufMap.put(\"")
                        .append(originalClassName(simpleName) + "\" , new ")
                        //                    .append(".class.getName(), new ")
                        .append(simpleName)
                        .append("_ToJavabuf());" + LS);

                System.out.println("toJavamap: isAbstract: " + isAbstract);
                if (!isAbstract) {
                    sb.append("      fromJavabufMap.put(")
                            .append("\"" + simpleName + "\"")
                            .append(", new ")
                            .append(simpleName)
                            .append("_FromJavabuf());" + LS);
                }
                sb.append("      toJavabufClassMap.put(\"")
                        //                    .append(originalCanonicalClassName(simpleName) + "\", ")
                        .append(originalClassName(simpleName) + "\", ")
                        //                    .append(originalClassName(simpleName) + ".class.getName(), ")
                        .append(simpleName + ".class);" + LS);
                sb.append("      fromJavabufClassMap.put(")
                        .append(simpleName + ".class.getName(), ")
                        //                    .append(fromJavabufClassValue(originalClassName(clazz.getCanonicalName())) + ");" + LS);
                        //                    .append(fromJavabufClassValue(originalClassName(simpleName)) + ");" + LS);
                        .append(getJavabufClassValue(simpleName, false, false) + ");" + LS);
                //                    .append(originalClassName(simpleName) + ".class);");
            }
        }
        sb.append(LS);
        for (Class<?> clazz2 : classes) {
            System.out.println("wrapper?: " + clazz2.getName());
            if (clazz2.getName().endsWith("___WRAPPER")) {
                sb.append("      ARRAY_WRAPPER_TYPES.add(\"")
                        .append(clazz2.getName())
                        .append("\");" + LS);
            }
        }
        sb.append(LS);
        sb.append("      WRAPPER_TYPES.add(java.lang.Boolean.class);" + LS);
        sb.append("      WRAPPER_TYPES.add(java.lang.Byte.class);" + LS);
        sb.append("      WRAPPER_TYPES.add(java.lang.Short.class);" + LS);
        sb.append("      WRAPPER_TYPES.add(java.lang.Integer.class);" + LS);
        sb.append("      WRAPPER_TYPES.add(java.lang.Long.class);" + LS);
        sb.append("      WRAPPER_TYPES.add(java.lang.Float.class);" + LS);
        sb.append("      WRAPPER_TYPES.add(java.lang.Double.class);" + LS);
        sb.append("      WRAPPER_TYPES.add(java.lang.Character.class);" + LS);
        sb.append("      WRAPPER_TYPES.add(java.lang.String.class);" + LS);
        sb.append("   }" + LS + LS);
    }

    private static boolean isAbstract(Class<?> clazz) throws ClassNotFoundException {
        if (clazz.getName().endsWith("___Array") || clazz.getName().endsWith("___WArray")) {
            return false;
        }
        String originalName = originalSimpleName(clazz.getName());
        String className = javabufToJava(clazz.getName(), originalName, true);
        System.out.println("staticInit(): " + clazz + ", " + originalName + ", " + className);
        //                    Class<?> c = Class.forName(originalClassName(simpleName), true,
        Class<?> c = Class.forName(className, true,
                Thread.currentThread().getContextClassLoader());
        return Modifier.isAbstract(c.getModifiers());
    }

    private static void publicMethods(StringBuilder sb, Class<?> clazz, String[] args) {
        sb.append("   public boolean handlesToJavabuf(Class<?> clazz) {" + LS)
                .append("      return clazz.isPrimitive() || toJavabufMap.containsKey(clazz.getName());" + LS)
                .append("   }" + LS + LS)
                .append("   public boolean handlesFromJavabuf(Class<?> clazz) {" + LS)
                .append("      return clazz.isPrimitive() || toJavabufMap.containsKey(clazz.getName());" + LS)
                .append("   }" + LS + LS)
                .append("   public Message translateToJavabuf(Object o) {" + LS)
                .append("      if (o.getClass().isArray()) {" + LS)
                //                .append("         return ArrayUtility.getHolder(this, o);" + LS)
                .append("         return arrayToJavabuf(o);" + LS)
                .append("      }" + LS)
                .append("      TranslateToJavabuf ttj = toJavabufMap.get(o.getClass().getName());" + LS)
                .append("      if (ttj == null) {" + LS)
                .append("         throw new RuntimeException(o.getClass() + \" is not recognized\");" + LS)
                .append("      }" + LS)
                .append("      ttj.clear();" + LS)
                .append("      return ttj.assignToJavabuf(o);" + LS)
                .append("   }" + LS + LS)
                .append("   public Message arrayToJavabuf(Object o) {" + LS)
                .append("      if (o.getClass().getComponentType().isArray()) {" + LS)
                .append("         return new dev_resteasy_grpc_arrays___ArrayHolder___WArray_ToJavabuf().assignToJavabuf(o);"
                        + LS)
                .append("      }" + LS)
                .append("      if (o.getClass().getComponentType().isPrimitive()) {" + LS)
                .append("         String name = o.getClass().getComponentType().getSimpleName();" + LS)
                .append("         name = name.substring(0, 1).toUpperCase() + name.substring(1);" + LS)
                .append("         TranslateToJavabuf ttj = toJavabufMap.get(\"dev.resteasy.grpc.arrays.\" + name + \".Array\");"
                        + LS)
                .append("         return ttj.assignToJavabuf(o);" + LS)
                .append("      }" + LS)
                .append("      String name = o.getClass().getComponentType().getSimpleName();" + LS)
                .append("      name = name.substring(0, 1).toUpperCase() + name.substring(1);" + LS)
                .append("      TranslateToJavabuf ttj = toJavabufMap.get(\"dev.resteasy.grpc.arrays.\" + name + \".WArray\");"
                        + LS)
                .append("      return ttj.assignToJavabuf(o);" + LS)
                .append("   }" + LS + LS)
                .append("   @SuppressWarnings(\"rawtypes\")" + LS)
                .append("   public Class translateToJavabufClass(Class<?> clazz) {" + LS)
                .append("      return toJavabufClassMap.get(clazz.getName());" + LS)
                .append("   }" + LS + LS)
                .append("   @SuppressWarnings(\"rawtypes\")" + LS)
                .append("   public Class translateToJavabufClass(String classname) {" + LS)
                .append("      return toJavabufClassMap.get(classname);" + LS)
                .append("   }" + LS + LS)
                .append("   @SuppressWarnings(\"rawtypes\")" + LS)
                .append("   @Override" + LS)
                .append("   public Class translatefromJavabufClass(String classname) {" + LS)
                .append("      return fromJavabufClassMap.get(classname);" + LS)
                .append("   }" + LS + LS)
                .append("   @Override" + LS)
                .append("   public String getOuterClassname() {" + LS)
                .append("      return \"").append(clazz.getDeclaringClass().getName()).append("\";" + LS)
                .append("   }" + LS + LS)
                .append("   public Object translateFromJavabuf(Message message) {" + LS)
                .append("      if (" + args[2]
                        + "_proto.dev_resteasy_grpc_arrays___ArrayHolder.class.equals(message.getClass())) {" + LS)
                .append("         try {" + LS)
                .append("            return ArrayUtility.getArray(this, (dev_resteasy_grpc_arrays___ArrayHolder) message);"
                        + LS)
                .append("         } catch (Exception e) {" + LS)
                .append("            throw new RuntimeException(e);" + LS)
                .append("         }" + LS)
                .append("      }" + LS)
                .append("      String s = null;" + LS)
                .append("      try {" + LS)
                .append("         s = message.getDescriptorForType().getFullName();" + LS)
                .append("         s = s.substring(s.lastIndexOf(\".\") + 1);" + LS)
                .append("         TranslateFromJavabuf tfj = fromJavabufMap.get(s);" + LS)
                .append("         if (tfj == null) {" + LS)
                .append("            throw new RuntimeException(message.getClass() + \" is not recognized\");" + LS)
                .append("         }" + LS)
                .append("         return tfj.assignFromJavabuf(message);" + LS)
                .append("      } catch (Exception e) {" + LS)
                .append("         throw new RuntimeException(e);" + LS)
                .append("      }" + LS)
                .append("   }" + LS + LS);
    }

    private static void createTranslator(String[] args, Class<?> clazz, StringBuilder sb) throws Exception {
        createTranslatorToJavabuf(args, clazz, sb);
        createTranslatorFromJavabuf(args, clazz, sb);
    }

    private static void privateVariables(StringBuilder sb, String[] args) {
        sb.append("   private static JavabufTranslator INSTANCE = new CC1JavabufTranslator();" + LS);
        sb.append(
                "   private static Map<String, TranslateToJavabuf> toJavabufMap = new HashMap<String, TranslateToJavabuf>();"
                        + LS);
        sb.append(
                "   private static Map<String, TranslateFromJavabuf> fromJavabufMap = new HashMap<String, TranslateFromJavabuf>();"
                        + LS);
        sb.append("   private static JavabufTranslator translator = new ").append(args[1]).append("JavabufTranslator();" + LS);
        sb.append(
                "   private static dev_resteasy_grpc_arrays___ArrayHolder_ToJavabuf arrayHolderToJavabuf = new dev_resteasy_grpc_arrays___ArrayHolder_ToJavabuf();"
                        + LS);
        sb.append("   private static Set<Class<?>> WRAPPER_TYPES = new HashSet<Class<?>>();" + LS);
        sb.append("   private static Set<String> ARRAY_WRAPPER_TYPES = new HashSet<String>();" + LS + LS);
        sb.append("   @SuppressWarnings(\"rawtypes\")" + LS);
        sb.append("   private static Map<String, Class> toJavabufClassMap = new HashMap<String, Class>();" + LS);
        sb.append("   private static Map<String, Class<?>> fromJavabufClassMap = new HashMap<String, Class<?>>();"
                + LS);
        sb.append("   private static Set<String> hiddenClasses = new HashSet<String>();" + LS);
        sb.append(
                "       private static Map<String, Constructor<?>> constructors = new HashMap<String, Constructor<?>>();" + LS);
    }

    private static void privateMethods(StringBuilder sb, Class<?>[] classes, String[] args) {
        //        sb.append("   private static AssignToJavabuf toJavabuf(Class<?> javaClass, FieldDescriptor fd) {" + LS)
        sb.append("   private static AssignToJavabuf toJavabuf(Class<?> clazz, FieldDescriptor fd) {" + LS)
                .append("      try {" + LS)
                .append("         AssignToJavabuf assignToJavabuf = (obj, messageBuilder) -> {" + LS)
                .append("            try {" + LS)
                .append("               if (obj == null) {" + LS)
                .append("                  return;" + LS)
                .append("               }" + LS)
                .append("                  final Field field = Utility.getField(clazz, fd.getName());" + LS)
                .append("                  field.setAccessible(true);" + LS)

                .append("                  if (field.getType().isArray()) {" + LS)
                .append("                        Object array = field.get(obj);" + LS)
                .append("                        if (array == null) {" + LS)
                .append("                           return;" + LS)
                .append("                        }" + LS)
                .append("                        Class<?> componentType = array.getClass().getComponentType();" + LS)
                .append("                        if (componentType.isArray()) {" + LS)
                .append("                           messageBuilder.setField(fd, arrayHolderToJavabuf.assignToJavabuf(field.get(obj)));"
                        + LS)
                .append("                        } else if (componentType.isPrimitive() || WRAPPER_TYPES.contains(componentType)) {"
                        + LS)
                .append("                           if (Character.class.equals(componentType) || char.class.equals(componentType)) {"
                        + LS)
                .append("                              messageBuilder.setField(fd, charsToString(array));" + LS)
                .append("                           } else if (byte.class.equals(componentType)) {"
                        + LS)
                .append("                              messageBuilder.setField(fd, ByteString.copyFrom((byte[])array));" + LS)
                .append("                           } else if (Byte.class.equals(componentType)) {" + LS)
                .append("                              for (int i = 0; i < Array.getLength(array); i++) {" + LS)
                .append("                                 messageBuilder.addRepeatedField(fd, Array.get(array, i));" + LS)
                .append("                              }" + LS)
                .append("                           } else {" + LS)
                .append("                              for (int i = 0; i < Array.getLength(array); i++) {" + LS)
                .append("                                 messageBuilder.addRepeatedField(fd, Array.get(array, i));" + LS)
                .append("                              }" + LS)
                .append("                           }" + LS)
                /*
                 * } else if (Object.class.equals(componentType)) {
                 * for (int i = 0; i < Array.getLength(array); i++) {
                 * messageBuilder.addRepeatedField(fd, Any.pack(translator.translateToJavabuf(Array.get(array, i))));
                 * }
                 */
                .append("                        } else if (Object.class.equals(componentType)) {" + LS)
                .append("System.out.println(Array.getLength(array));" + LS)
                .append("                           for (int i = 0; i < Array.getLength(array); i++) {" + LS)
                .append("                              if (Array.get(array, i) != null) {" + LS)
                .append("                                 messageBuilder.addRepeatedField(fd, Any.pack(translator.translateToJavabuf(Array.get(array, i))));"
                        + LS)
                .append("                              }" + LS)
                .append("                           }" + LS)
                .append("                        } else {" + LS)
                .append("                           /* insert array handling */" + LS)
                //                .append("                           messageBuilder.addRepeatedField(fd, ELEMENT_WRAPPER.newBuilder().setPosition(Array.getLength(array)).build());"
                //                        + LS)
                //                .append("                           for (int i = 0; i < Array.getLength(array); i++) {" + LS)
                //                .append("                              if (Array.get(array, i) != null) {" + LS)
                //                //                                        messageBuilder.addRepeatedField(fd, wrapArrayElement(translator.translateToJavabuf(Array.get(array, i)), i));
                //                .append("                                 messageBuilder.addRepeatedField(fd, wrapArrayElement(translator.translateToJavabuf(Array.get(array, i)), i));"
                //                        + LS)
                //                .append("                              }" + LS)
                //                .append("                           }" + LS)
                .append("                        }" + LS)
                .append("                    }" + LS)
                //                .append("                  } else if" + LS)

                .append("                  else if (\".google.protobuf.Any\".equals(fd.toProto().getTypeName())) {" + LS)
                .append("                     if (field.get(obj) != null) {" + LS)
                .append("                        Class<?> c = field.get(obj).getClass();" + LS)
                .append("                        if (Any.class.equals(c)) {" + LS)
                .append("                           messageBuilder.setField(fd, field.get(obj));" + LS)
                .append("                        } else {" + LS)
                .append("                           Message message = toJavabufMap.get(c.getName()).assignToJavabuf(field.get(obj));"
                        + LS)
                .append("                           messageBuilder.setField(fd, Any.pack(message));" + LS)
                .append("                        }" + LS)
                .append("                     }" + LS)

                .append("                  } else if (!String.class.equals(field.getType()) && toJavabufMap.keySet().contains(field.getType().getName())) {"
                        + LS)
                //                .append("                     if (\".google.protobuf.Any\".equals(fd.toProto().getTypeName())) {" + LS)
                //                .append("                        Message message = toJavabufMap.get(field.getType().getName()).assignToJavabuf(field.get(obj));"
                //                        + LS)
                //                .append("                        if (message != null) {" + LS)
                //                .append("                           messageBuilder.setField(fd, Any.pack(message));" + LS)
                //                .append("                        }" + LS)
                //                .append("                     } else {" + LS)
                .append("                        Message message = toJavabufMap.get(field.getType().getName()).assignToJavabuf(field.get(obj));"
                        + LS)
                .append("                        if (message != null) {" + LS)
                .append("                           messageBuilder.setField(fd, message);" + LS)
                .append("                        }" + LS)
                //                .append("                     }" + LS)
                //                .append("                  } else"
                //
                //
                //                      + ""
                //                      + "else {" + LS)
                .append("                    } else if (field.get(obj) != null) {" + LS)
                .append("                        if (Byte.class.equals(field.getType()) || byte.class.equals(field.getType())) {"
                        + LS)
                .append("                           Byte b = field.getByte(obj);" + LS)
                .append("                           messageBuilder.setField(fd, b.intValue());" + LS)
                .append("                        } else if (Short.class.equals(field.getType()) || short.class.equals(field.getType())) {"
                        + LS)
                .append("                           Short s = field.getShort(obj);" + LS)
                .append("                           messageBuilder.setField(fd, s.intValue());" + LS)
                .append("                        } else if (Character.class.equals(field.getType()) || char.class.equals(field.getType())) {"
                        + LS)
                .append("                           Character c = field.getChar(obj);" + LS)
                .append("                           messageBuilder.setField(fd, String.valueOf(c));" + LS)
                .append("                        } else if (Descriptors.FieldDescriptor.JavaType.BYTE_STRING.equals(fd.getJavaType())) {"
                        + LS)
                .append("                           ByteString bs = ByteString.copyFrom((byte[]) field.get(obj));" + LS)
                .append("                           messageBuilder.setField(fd, bs);" + LS)
                .append("                        } else {" + LS)
                .append("                           messageBuilder.setField(fd, field.get(obj));" + LS)
                .append("                        }" + LS)
                .append("                     }" + LS)
                //                .append("               }" + LS)
                .append("            } catch (Exception e) {" + LS)
                .append("                throw new RuntimeException(e);" + LS)
                .append("            }" + LS)
                .append("         };" + LS)
                .append("         return assignToJavabuf;" + LS)
                .append("      } catch (Exception e) {" + LS)
                .append("         throw new RuntimeException(e);" + LS)
                .append("      }" + LS)
                //                .append("      };" + LS)
                .append("   }" + LS + LS);
        /*
         * messageBuilder.setField(fd, field.get(obj));
         * }
         * }
         * // }
         * } catch (Exception e) {
         * throw new RuntimeException(e);
         * }
         * };
         * return assignToJavabuf;
         * } catch (Exception e) {
         * throw new RuntimeException(e);
         * }
         * }
         */
        sb.append("   private static AssignFromJavabuf fromJavabuf(Class<?> javaClass, FieldDescriptor fd) {" + LS)
                .append("      try {" + LS)
                .append("         AssignFromJavabuf assignFromJavabuf = (message, object) -> {" + LS)
                .append("            try {" + LS)
                //                .append("               if (isSuperClass(fd.getName())) {" + LS)
                //                .append("                  String superClassName = javaClassToJavabufClass(javaClass.getSuperclass().getName());"
                //                        + LS)
                //                .append("                  TranslateFromJavabuf t = fromJavabufMap.get(superClassName);" + LS)
                //                .append("                  FieldDescriptor sfd = getSuperField(message);" + LS)
                //                .append("                  Message superMessage = (Message) message.getField(sfd);" + LS)
                //                .append("                  t.assignExistingFromJavabuf(superMessage, object);" + LS)
                //                .append("               } else {" + LS)
                //                .append("                  final Field field = javaClass.getDeclaredField(javabufClassToJavaClass(fd.getName()));"
                //                        + LS)
                .append("                  final Field field = Utility.getField(javaClass, javabufClassToJavaClass(fd.getName()));"
                        + LS)
                .append("                  field.setAccessible(true);" + LS)
                .append("                  if (field.getType().isArray()) {" + LS)
                .append("                     String typeName = fd.toProto().getTypeName();" + LS)
                /*
                 * if (".google.protobuf.Any".equals(typeName)) {
                 *
                 * } else
                 * // typeName = typeName.substring(typeName.lastIndexOf(".") + 1);
                 * if ("dev_resteasy_grpc_arrays___ArrayHolder".equals(typeName.substring(typeName.lastIndexOf(".") + 1))) {
                 *
                 * String typeName = fd.toProto().getTypeName();
                 * if (".google.protobuf.Any".equals(typeName)) {
                 * if (message.getRepeatedFieldCount(fd) > 0) {
                 * Class clazz = Utility.extractClassFromAny((Any) message.getRepeatedField(fd, 0), INSTANCE);
                 * for (int i = 0; i < message.getRepeatedFieldCount(fd); i++) {
                 * Any any = (Any) message.getRepeatedField(fd, i);
                 * field.set(object, any.unpack(clazz));
                 * }
                 * }
                 *
                 * if (message.getRepeatedFieldCount(fd) > 0) {
                 * Class clazz = Utility.extractClassFromAny((Any) message.getRepeatedField(fd, 0), INSTANCE);
                 * Class javaClazz = INSTANCE.translatefromJavabufClass(clazz.getName());
                 * Object array = Array.newInstance(javaClazz, message.getRepeatedFieldCount(fd));
                 * for (int i = 0; i < message.getRepeatedFieldCount(fd); i++) {
                 * Any any = (Any) message.getRepeatedField(fd, i);
                 * Object o = INSTANCE.translateFromJavabuf(any.unpack(clazz));
                 * Array.set(array, i, o);
                 * // field.set(object, INSTANCE.translateFromJavabuf(any.unpack(clazz)));
                 * }
                 * Utility.setField(field, object, array);
                 * }
                 */
                .append("                     if (\".google.protobuf.Any\".equals(typeName)) {" + LS)
                .append("                        if (message.getRepeatedFieldCount(fd) > 0) {" + LS)
                .append("                           Class clazz = Utility.extractClassFromAny((Any) message.getRepeatedField(fd, 0), INSTANCE);"
                        + LS)
                .append("                           Class javaClazz = INSTANCE.translatefromJavabufClass(clazz.getName());"
                        + LS)
                .append("                           Object array = Array.newInstance(javaClazz, message.getRepeatedFieldCount(fd));"
                        + LS)
                .append("                           for (int i = 0; i < message.getRepeatedFieldCount(fd); i++) {" + LS)
                .append("                              Any any = (Any) message.getRepeatedField(fd, i);" + LS)
                .append("                              Object o = INSTANCE.translateFromJavabuf(any.unpack(clazz));" + LS)
                .append("                              Array.set(array, i, o);" + LS)
                //                .append("                              field.set(object, any.unpack(clazz));" + LS)
                //                .append("                              field.set(object, INSTANCE.translateFromJavabuf(any.unpack(clazz)));"
                //                        + LS)

                //                .append("                     typeName = typeName.substring(typeName.lastIndexOf(\".\") + 1);" + LS)
                .append("                           }" + LS)
                .append("                           Utility.setField(field, object, array, INSTANCE);" + LS)
                .append("                        }" + LS)
                .append("                     } else if (\"dev_resteasy_grpc_arrays___ArrayHolder\".equals(typeName.substring(typeName.lastIndexOf(\".\") + 1))) {"
                        + LS)
                .append("                        dev_resteasy_grpc_arrays___ArrayHolder ah = (dev_resteasy_grpc_arrays___ArrayHolder) message.getField(fd);"
                        + LS)
                .append("                        Utility.setField(field, object, ArrayUtility.getArray(translator, ah), INSTANCE);"
                        + LS)
                .append("                     } else if (Descriptors.FieldDescriptor.JavaType.STRING.equals(fd.getJavaType())) {"
                        + LS)
                .append("                        if (char.class.equals(field.getType().getComponentType())) {" + LS)
                .append("                           char[] cs = stringToChars((String) message.getField(fd));" + LS)
                .append("                           Utility.setField(field, object, cs, INSTANCE);" + LS)
                .append("                        } else {" + LS)
                .append("                            Character[] cs = stringToCharacters((String) message.getField(fd));" + LS)
                .append("                            Utility.setField(field, object, cs, INSTANCE);" + LS)
                .append("                        }" + LS)
                .append("                     } else if (byte.class.equals(field.getType().getComponentType())) {"
                        + LS)
                .append("                        ByteString bs = (ByteString) message.getField(fd);" + LS)
                .append("                        Utility.setField(field, object, bs.toByteArray(), INSTANCE);" + LS)
                .append("                      } else if (Byte.class.equals(field.getType().getComponentType())) {" + LS)
                .append("                         IntList il = (IntList) message.getField(fd);" + LS)
                .append("                         Byte[] bs = new Byte[il.size()];" + LS)
                .append("                         for (int i = 0; i < il.size(); i++) {" + LS)
                .append("                            bs[i] = il.get(i).byteValue();" + LS)
                .append("                         }" + LS)
                .append("                         Utility.setField(field, object, bs, INSTANCE);" + LS)
                .append("                     } else if (message.getField(fd) instanceof List) {" + LS)
                .append("                        List list = (List) message.getField(fd);" + LS)
                .append("                        if (list.size() == 0) {" + LS)
                .append("                           return;" + LS)
                .append("                        }" + LS)
                .append("                        if (message.getField(fd).getClass().getPackage().getName().startsWith(\"com.google.protobuf\")) {"
                        + LS)
                .append("                           if (field.get(object) == null) {" + LS)
                .append("                 /* FIX             Utility.setField(field, object, getArray(field, list.size()), INSTANCE); */"
                        + LS)
                .append("                           }" + LS)
                .append("                           if ((short.class.equals(field.getType().getComponentType())" + LS)
                .append("                             || Short.class.equals(field.getType().getComponentType()))" + LS)
                .append("                                && list.get(0) instanceof Integer) {" + LS)
                .append("                              for (int i = 0; i < list.size(); i++) {" + LS)
                .append("                                 Array.set(field.get(object), i, ((Integer) list.get(i)).shortValue());"
                        + LS)
                .append("                               }" + LS)
                .append("                           } else {" + LS)
                .append("                              for (int i = 0; i < list.size(); i++) {" + LS)
                .append("                                 Array.set(field.get(object), i, list.get(i));" + LS)
                .append("                              }" + LS)
                .append("                           }" + LS)
                .append("                        } else {" + LS)
                .append("                           if (fd.getMessageType().getName().endsWith(\"ELEMENT_WRAPPER\")) {" + LS)
                .append("        /*     FIX " + LS)
                .append("                              if (field.get(object) == null) {" + LS)
                .append("                                  int size = (int) ((ELEMENT_WRAPPER) list.get(0)).getPosition();"
                        + LS)
                .append("                                  Utility.setField(field, object, Array.newInstance(field.getType().getComponentType(), size), INSTANCE);"
                        + LS)
                .append("                              }" + LS)
                .append("                              for (int i = 1; i < list.size(); i++) {" + LS)
                .append("                                 int position = (int) ((ELEMENT_WRAPPER) list.get(i)).getPosition();"
                        + LS)
                .append("                                 Message element = unwrapArrayElement((ELEMENT_WRAPPER) list.get(i));"
                        + LS)
                .append("                                 Array.set(field.get(object), position, translator.translateFromJavabuf(element));"
                        + LS)
                .append("                              }" + LS)
                .append("      TO HERE */" + LS)

                /*
                 * if (fd.getMessageType().getName().endsWith("ELEMENT_WRAPPER")) {
                 * if (field.get(object) == null) {
                 * int size = (int) ((ELEMENT_WRAPPER) list.get(0)).getPosition();
                 * Utility.setField(field, object, Array.newInstance(field.getType().getComponentType(), size), INSTANCE);
                 * }
                 * if (field.get(object) == null) {
                 * Utility.setField(field, object, Array.newInstance(field.getType().getComponentType(), list.size()),
                 * INSTANCE);
                 * }
                 * for (int i = 1; i < list.size(); i++) {
                 * int position = (int) ((ELEMENT_WRAPPER) list.get(i)).getPosition();
                 * Message element = unwrapArrayElement((ELEMENT_WRAPPER) list.get(i));
                 * Array.set(field.get(object), position, translator.translateFromJavabuf(element));
                 * }
                 *
                 */
                //                .append("                           if (field.get(object) == null) {" + LS)
                //                .append("                              Utility.setField(field, object, Array.newInstance(field.getType().getComponentType(), list.size()), INSTANCE);"
                //                        + LS)
                //                .append("                            }" + LS)
                //                .append("                            if (fd.getMessageType().getName().endsWith(\"ELEMENT_WRAPPER\")) {" + LS)
                //                .append("                                for (int i = 0; i < list.size(); i++) {" + LS)
                //                .append("                                   int position = (int) ((ELEMENT_WRAPPER) list.get(i)).getPosition();"
                //                        + LS)
                //                .append("                                    Message element = unwrapArrayElement((ELEMENT_WRAPPER) list.get(i));"
                //                        + LS)
                //                .append("                                    Array.set(field.get(object), position, translator.translateFromJavabuf(element));"
                //                        + LS)
                //                .append("                                }" + LS)
                /*
                 * if (fd.getMessageType().getName().endsWith("ELEMENT_WRAPPER")) {
                 * for (int i = 0; i < list.size(); i++) {
                 * int position = (int) ((ELEMENT_WRAPPER) list.get(i)).getPosition();
                 * Message element = unwrapArrayElement((ELEMENT_WRAPPER) list.get(i));
                 * Array.set(field.get(object), position, translator.translateFromJavabuf(element));
                 * }
                 * }
                 */
                .append("                            } else for (int i = 0; i < list.size(); i++) {" + LS)
                .append("                              Array.set(field.get(object), i, translator.translateFromJavabuf((Message) list.get(i)));"
                        + LS)
                .append("                           }" + LS)
                .append("                        }" + LS)
                .append("                     }" + LS)
                /*
                 * } else if (Descriptors.FieldDescriptor.Type.MESSAGE.equals(fd.getType())
                 * && fromJavabufMap.keySet().contains(fd.getMessageType().getName())) {
                 * boolean b = message.hasField(fd);
                 * if (message.hasField(fd)) {
                 * Message submessage = (Message) message.getField(fd);
                 * Object obj = fromJavabufMap.get(fd.getMessageType().getName()).assignFromJavabuf(submessage);
                 * ArrayUtility.assignArray(field, object, obj);
                 * }
                 */
                .append("                  } else if (Descriptors.FieldDescriptor.Type.MESSAGE.equals(fd.getType())" + LS)
                .append("                     && fromJavabufMap.keySet().contains(fd.getMessageType().getName())) {" + LS)
                .append("                     if (message.hasField(fd)) {" + LS)
                .append("                        Message submessage = (Message) message.getField(fd);" + LS)
                .append("                        Object obj = fromJavabufMap.get(fd.getMessageType().getName()).assignFromJavabuf(submessage);"
                        + LS)
                //                .append("                        ArrayUtility.assignArray(field, object, obj);" + LS)
                .append("                        Utility.setField(field, object, obj, INSTANCE);" + LS)
                .append("                     }" + LS)
                .append("                  } else if (JavaType.MESSAGE.equals(fd.getJavaType()) && dev_resteasy_grpc_arrays___ArrayHolder.class.getSimpleName().equals(fd.getMessageType().getName())) {"
                        + LS)
                .append("                     dev_resteasy_grpc_arrays___ArrayHolder submessage = (dev_resteasy_grpc_arrays___ArrayHolder) message.getField(fd);"
                        + LS)
                .append("                     Object o = ArrayUtility.getArray(translator, submessage);" + LS)
                .append("                  } else {" + LS)
                .append("                     Object ooo = message.getField(fd);" + LS)
                .append("                     if (Integer.class.equals(ooo.getClass()) && (Byte.class.equals(field.getType()) || byte.class.equals(field.getType()))) {"
                        + LS)
                .append("                        Utility.setField(field, object, ((Integer) ooo).byteValue(), INSTANCE);" + LS)
                .append("                     } else if (Integer.class.equals(ooo.getClass()) && (Short.class.equals(field.getType()) || short.class.equals(field.getType()))) {"
                        + LS)
                .append("                        Utility.setField(field, object, ((Integer) ooo).shortValue(), INSTANCE);" + LS)
                .append("                     } else if (Integer.class.equals(ooo.getClass()) && (Character.class.equals(field.getType()) || char.class.equals(field.getType()))) {"
                        + LS)
                .append("                        int i = ((Integer)ooo).intValue();" + LS)
                .append("                        Utility.setField(field, object, Character.toChars(i)[0], INSTANCE);" + LS)
                .append("                     } else if (ooo instanceof ByteString) {" + LS)
                .append("                        Utility.setField(field, object, ((ByteString) ooo).newInput().readAllBytes(), INSTANCE);"
                        + LS)
                .append("                     } else {" + LS)
                .append("                        Utility.setField(field, object, ooo, INSTANCE);" + LS)
                //                .append("                     }" + LS)
                .append("                  }" + LS)
                .append("               }" + LS)
                .append("            } catch (Exception e) {" + LS)
                .append("               throw new RuntimeException(e);" + LS)
                .append("            }" + LS)
                .append("         };" + LS)
                .append("         return assignFromJavabuf;" + LS)
                .append("      } catch (Exception e) {" + LS)
                .append("         throw new RuntimeException(e);" + LS)
                .append("      }" + LS)
                .append("   }" + LS + LS);
        sb.append("   private static String javaClassToJavabufClass(String javaClassName) {" + LS)
                .append("      String javabufClassName = javaClassName.replace(\".\", \"_\");" + LS)
                .append("      int i = javabufClassName.lastIndexOf(\"_\");" + LS)
                .append("      javabufClassName = javabufClassName.substring(0, i) + \"___\" + javabufClassName.substring(i + 1);"
                        + LS)
                .append("      return javabufClassName;" + LS)
                .append("   }" + LS + LS);
        sb.append("   private static String javabufClassToJavaClass(String fieldName) {" + LS)
                .append("      int pos = fieldName.lastIndexOf(\"___\");" + LS)
                .append("      if (pos >= 0) {" + LS)
                .append("         return fieldName.substring(0, pos);" + LS)
                .append("      }" + LS)
                .append("      return fieldName;" + LS)
                .append("   }" + LS + LS);
        sb.append("   private static FieldDescriptor getSuperField(Message message) {" + LS)
                .append("      Map<FieldDescriptor, Object> map = message.getAllFields();" + LS)
                .append("      for (FieldDescriptor fd : map.keySet()) {" + LS)
                .append("         if (fd.getName().endsWith(\"___super\")) {" + LS)
                .append("            return fd;" + LS)
                .append("         }" + LS)
                .append("      }" + LS)
                .append("      return null;" + LS)
                .append("   }" + LS + LS);
        sb.append(
                "   private static boolean isSuperClass(String fieldName) {" + LS +
                        "      return fieldName.endsWith(\"___super\");" + LS +
                        "   }" + LS + LS);
        sb.append("   private static String charsToString(Object o) {" + LS)
                .append("      StringBuilder sb = new StringBuilder();" + LS)
                .append("      if (char.class.equals(o.getClass().getComponentType())) {" + LS)
                .append("         char[] array = (char[]) o;" + LS)
                .append("         for (int i = 0; i < array.length; i++) {" + LS)
                .append("            sb.append(array[i]);" + LS)
                .append("         }" + LS)
                .append("      } else {" + LS)
                .append("           Character[] array = (Character[]) o;" + LS)
                .append("           for (int i = 0; i < array.length; i++) {" + LS)
                .append("              sb.append(array[i]);" + LS)
                .append("            }" + LS)
                .append("      }" + LS)
                .append("      return sb.toString();" + LS)
                .append("   }" + LS + LS);
        sb.append("   private static char[] stringToChars(String s) {" + LS)
                .append("      char[] cs = new char[s.length()];" + LS)
                .append("      for (int i = 0; i < s.length(); i++) {" + LS)
                .append("         cs[i] = s.charAt(i);" + LS)
                .append("      }" + LS)
                .append("      return cs;" + LS)
                .append("   }" + LS + LS);
        sb.append("   private static Character[] stringToCharacters(String s) {" + LS)
                .append("      Character[] cs = new Character[s.length()];" + LS)
                .append("      for (int i = 0; i < s.length(); i++) {" + LS)
                .append("         cs[i] = s.charAt(i);" + LS)
                .append("      }" + LS)
                .append("      return cs;" + LS)
                .append("   }" + LS + LS);
        //        sb.append("   interface ReturnJavaClass {" + LS)
        //                .append("      Class<?> getJavaClass() throws Exception;" + LS)
        //                .append("   } " + LS + LS);
        //        sb.append("   private static ReturnJavaClass getReturnJavaClass(final Class<?> clazz) {" + LS)
        //                .append("      return new ReturnJavaClass() {" + LS)
        //                .append("         public Class<?> getJavaClass() throws Exception {" + LS)
        //                .append("            return clazz;" + LS)
        //                .append("         }" + LS)
        //                .append("      };" + LS)
        //                .append("   }" + LS + LS);
        sb.append("   private static ReturnJavaClass getReturnNonPublicJavaClass(final String classname) {" + LS)
                .append("      return new ReturnJavaClass() {" + LS)
                .append("         public Class<?> getJavaClass() {" + LS)
                .append("            try {" + LS)
                .append("               return Class.forName(classname);" + LS)
                .append("            } catch (Exception e) {" + LS)
                .append("               throw new RuntimeException(e);" + LS)
                .append("            }" + LS)
                .append("         }" + LS)
                .append("      };" + LS)
                .append("   }" + LS + LS);
        sb.append("   private static Constructor<?> getConstructor(String classname) throws Exception {" + LS)
                .append("      if (constructors.containsKey(classname)) {" + LS)
                .append("         return constructors.get(classname);" + LS)
                .append("      }" + LS)
                .append("      Constructor<?>[] conss = Class.forName(classname).getDeclaredConstructors();" + LS)
                .append("      if (conss.length == 0) {" + LS)
                .append("         return null;" + LS)
                .append("      }" + LS)
                .append("      Constructor<?> cons = conss[0];" + LS)
                .append("      for (int i = 1; i < conss.length; i++) {" + LS)
                .append("         if (conss[i].getParameterCount() < cons.getParameterCount()) {" + LS)
                .append("            cons = conss[i];" + LS)
                .append("         }" + LS)
                .append("      }" + LS)
                .append("      cons.setAccessible(true);" + LS)
                .append("      constructors.put(classname, cons);" + LS)
                .append("      return cons;" + LS)
                .append("   }" + LS + LS);
        sb.append("   static String squashName(String name) {" + LS)
                .append("      StringBuilder sb = new StringBuilder();" + LS)
                .append("      int pos = name.indexOf('_');" + LS)
                .append("      while (pos > 0) {" + LS)
                .append("         sb.append(name.substring(0, pos));" + LS)
                .append("         name = name.substring(pos + 1);" + LS)
                .append("         pos = name.indexOf('_');" + LS)
                .append("      }" + LS)
                .append("      sb.append(name);" + LS)
                .append("      return sb.toString().toLowerCase();" + LS)
                .append("   }" + LS + LS);
        /*
         * static String squashName(String name) {
         * StringBuilder sb = new StringBuilder();
         * int pos = name.indexOf('_');
         * while (pos > 0) {
         * sb.append(name.substring(0, pos));
         * name = name.substring(pos + 1);
         * pos = name.indexOf('_');
         * }
         * return sb.toString().toLowerCase();
         * }
         */
        //        for (Class<?> clazz : classes) {
        //            for (Class<?> innerClazz : clazz.getDeclaredClasses()) {
        //                if (innerClazz.getName().endsWith("ELEMENT_WRAPPER")) {
        //                    if (innerClazz.getEnumConstants().length > 0) {
        //                        sb.append("   static Message wrapArrayElement(Message m, int position) {" + LS)
        //                                .append("      ELEMENT_WRAPPER.Builder builder = ELEMENT_WRAPPER.newBuilder();" + LS)
        //                                .append("    }" + LS);
        //                    }
        //                    for (Object en : innerClazz.getEnumConstants()) {
        //                        System.out.println("enum: " + en);
        //                    }
        //                }
        //            }
        //        }

        for (Class<?> clazz : classes) {
            if (clazz.getName().endsWith("ELEMENT_WRAPPER")) {
                Class<?> builderClazz = null;
                Class<?> enumClazz = null;
                for (Class<?> innerClazz : clazz.getDeclaredClasses()) {
                    System.out.println("innerClazz: " + innerClazz.getName());
                    if (innerClazz.getName().endsWith("$Builder")) {
                        builderClazz = innerClazz;
                        for (Method m : innerClazz.getDeclaredMethods()) {
                            if (m.getName().startsWith("set")) {
                                System.out.println("m set: " + m.getName());
                            }
                        }
                    }
                    if (innerClazz.getName().endsWith("MessageTypeCase")) {
                        enumClazz = innerClazz;
                    }
                }
                StringBuilder sb2 = new StringBuilder();
                Object[] os = enumClazz.getEnumConstants();
                if (os.length > 0) {
                    sb.append("   static Message wrapArrayElement(Message m, int position) {" + LS)
                            .append("      ELEMENT_WRAPPER.Builder builder = ELEMENT_WRAPPER.newBuilder();" + LS)
                            .append("      switch(squashName(m.getDescriptorForType().getName())) {" + LS);
                    sb2.append("   static Message unwrapArrayElement(ELEMENT_WRAPPER wrapper) {" + LS);
                    Method setMethod = null;
                    for (int i = 0; i < os.length; i++) {
                        System.out.println("  enum 2: " + os[i].toString() + ", " + squashName(os[i].toString()));
                        String enumSquash = squashName(os[i].toString());
                        for (Method m : builderClazz.getDeclaredMethods()) {
                            System.out.println("builder m: " + m.getName());
                            if (m.getName().startsWith("set") && !m.getParameterTypes()[0].getName().endsWith("Builder")) {
                                String classname = m.getName().substring(3, m.getName().length() - 5);
                                if ("".equals(classname)) {
                                    continue;
                                }
                                classname = classname.substring(0, 1).toLowerCase() + classname.substring(1);
                                if ("any".equalsIgnoreCase(classname)) {
                                    classname = "com.google.protobuf.Any";
                                }
                                System.out.println("param: " + m.getParameterTypes()[0]);

                                String s = m.getName().substring(3, m.getName().length() - 5).toLowerCase();
                                //                                s = s.substring(0, s.length() - 5).toLowerCase();
                                System.out.println("enum 3: " + enumSquash + ", builder set: " + m.getName() + ", s:" + s);
                                System.out.println("enum 4: enumSquash: " + enumSquash + ", s: " + s);
                                if (enumSquash.equals(s)) {
                                    System.out.println("enum 5: enumSquash: " + enumSquash + ", s: " + s);
                                    setMethod = m;
                                    sb.append("         case \"").append(enumSquash).append("\":" + LS)
                                            .append("            builder.").append(m.getName()).append("((")
                                            .append(m.getParameterTypes()[0].getCanonicalName())
                                            .append(") m);" + LS)
                                            .append("            builder.setPosition(position);" + LS)
                                            .append("            return builder.build();" + LS);
                                    String hasMethod = "has" + m.getName().substring(3);
                                    String getMethod = "get" + m.getName().substring(3);
                                    sb2.append("      if (wrapper.").append(hasMethod).append("()) {" + LS)
                                            .append("         return wrapper.").append(getMethod).append("();" + LS)
                                            .append("      }" + LS);
                                }
                            }
                        }
                        //                        sb.append("      }" + LS);
                    }
                    //                    sb.append("   return builder.build();" + LS)
                    sb.append("      }" + LS)
                            .append("      return m;" + LS)
                            .append("   }" + LS + LS);
                    sb2.append("      throw new RuntimeException(\"unrecognized field: \" + wrapper);" + LS)
                            .append("   }" + LS + LS);
                    sb.append(sb2);

                    /*
                     * private static Object extractFromWrapper(ELEMENT_WRAPPER wrapper) {
                     * if (wrapper.hasAnyField()) {
                     * return wrapper.getAnyField();
                     * }
                     *
                     * }
                     */
                    //                    sb.append("   static Message unwrapArrayElement(ELEMENT_WRAPPER wrapper) {" + LS);
                    //                    Method getMethod = null;
                    //                    for (int i = 0; i < os.length; i++) {
                    //                       System.out.println("  enum 2: " + os[i].toString() + ", " + squashName(os[i].toString()));
                    //                       String enumSquash = squashName(os[i].toString());
                    //                       for (Method m : builderClazz.getDeclaredMethods()) {
                    //                          System.out.println("builder m: " + m.getName());
                    //                          if (m.getName().startsWith("has") && !m.getParameterTypes()[0].getName().endsWith("Builder")) {
                    //                             String classname = m.getName().substring(3, m.getName().length() - 5);
                    //                             if ("".equals(classname)) {
                    //                                continue;
                    //                             }
                    //                             classname = classname.substring(0, 1).toLowerCase() + classname.substring(1);
                    //                             if ("any".equalsIgnoreCase(classname)) {
                    //                                classname = "com.google.protobuf.Any";
                    //                             }
                    //                             System.out.println("param: " + m.getParameterTypes()[0]);
                    //
                    //                             String s = m.getName().substring(3, m.getName().length() - 5).toLowerCase();
                    //                             //                                s = s.substring(0, s.length() - 5).toLowerCase();
                    //                             System.out.println("enum 3: " + enumSquash + ", builder set: " + m.getName() + ", s:" + s);
                    //                             System.out.println("enum 4: enumSquash: " + enumSquash + ", s: " + s);
                    //                             if (enumSquash.equals(s)) {
                    //                                System.out.println("enum 5: enumSquash: " + enumSquash + ", s: " + s);
                    //                                setMethod = m;
                    //                                sb.append("         case \"").append(enumSquash).append("\":" + LS)
                    //                                .append("            builder.").append(m.getName()).append("((")
                    //                                .append(m.getParameterTypes()[0].getCanonicalName())
                    //                                .append(") m);" + LS)
                    //                                .append("            builder.setPosition(position);" + LS)
                    //                                .append("            return builder.build();" + LS);
                    //
                    //                             }
                    //                          }
                    //                       }
                    //                       //                        sb.append("      }" + LS);
                    //                    }
                    //                    //                    sb.append("   return builder.build();" + LS)
                    //                    sb.append("      }" + LS)
                    //                    .append("      return m;" + LS)
                    //                    .append("   }" + LS + LS);
                }
            }
        }
    }

    //  if (innerClazz.getName().endsWith("MessageTypeCase")) {
    //  Object[] os = innerClazz.getEnumConstants();
    //  if (os.length > 0) {
    //      sb.append("   static Message wrapArrayElement(Message m, int position) {" + LS)
    //              .append("      ELEMENT_WRAPPER.Builder builder = ELEMENT_WRAPPER.newBuilder();" + LS)
    //              .append("      switch(m.getDescriptorForType().getName()) {" + LS);
    //      for (int i = 0; i < os.length; i++) {
    //          System.out.println("  enum 2: " + os[i].toString() + ", " + squashName(os[i].toString()));
    //      }
    //      Method m = null;
    //      for ()
    //  }
    //  sb.append("    }" + LS);
    //}
    /*
     * java_util_HashMap_HIDDEN_Node j_u
     * JavaUtilHashMapHIDDENNode
     * JavaUtilHashMapHIDDENNode
     * dev_resteasy_grpc_arrays___ArrayHolder_Array: devresteasygrpcarraysArrayHolderArray
     */
    static String squashToCamel(String name) {
        StringBuilder sb = new StringBuilder();
        boolean start = true;
        for (int i = 0; i < name.length(); i++) {
            if (name.charAt(i) == '_') {
                start = true;
                continue;
            }
            sb.append(start ? name.substring(i, i + 1).toUpperCase() : name.substring(i, i + 1));
            start = false;
        }
        return sb.toString();
    }

    static String squashName(String name) {
        StringBuilder sb = new StringBuilder();
        int pos = name.indexOf('_');
        while (pos > 0) {
            sb.append(name.substring(0, pos));
            name = name.substring(pos + 1);
            pos = name.indexOf('_');
        }
        return sb.toString().toLowerCase();
    }
    /*
     * sb.append("   static Message wrapArrayElement(Message m, int position) {" + LS)
     * .append("      ELEMENT_WRAPPER.Builder builder = ELEMENT_WRAPPER.newBuilder();" + LS)
     * .append("      switch (m.getDescriptorForType().getName()) {" + LS);
     * // for ()
     *
     * static Message wrapArrayElement(Message m, int position, FieldDescriptor fd) {
     * ELEMENT_WRAPPER.Builder builder = ELEMENT_WRAPPER.newBuilder();
     * System.out.println(m.getDescriptorForType().getName());
     *
     * switch (m.getDescriptorForType().getName()) {
     * case "java_util_HashMap_HIDDEN_Node":
     * builder.setJavaUtilHashMapHIDDENNodeField((java_util_HashMap_HIDDEN_Node) m);
     * }
     * return m;
     * }
     *
     * private static Constructor<?> getConstructor(String classname) throws Exception {
     * if (constructors.containsKey(classname)) {
     * return constructors.get(classname);
     * }
     * Constructor<?>[] conss = Class.forName(classname).getConstructors();
     * if (conss.length == 0) {
     * return null;
     * }
     * Constructor<?> cons = conss[0];
     * for (int i = 1; i < conss.length; i++) {
     * if (conss[i].getParameterCount() < cons.getParameterCount()) {
     * cons = conss[i];
     * }
     * }
     * constructors.put(classname, cons);
     * return cons;
     * }
     */
    /*
     * interface returnJavaClass {
     * Class<?> getJavaClass() throws Exception;
     * }
     * private static returnJavaClass getReturnJavaClass(final Class<?> clazz) {
     * return new returnJavaClass() {
     * public Class<?> getJavaClass() throws Exception {
     * return clazz;
     * }
     * };
     * }
     *
     * private static returnJavaClass getReturnPrivateJavaClass(final String classname) {
     * return new returnJavaClass() {
     * public Class<?> getJavaClass() throws Exception {
     * return Class.forName(classname);
     * }
     * };
     * }
     */

    private static void createTranslatorToJavabuf(String[] args, Class<?> clazz, StringBuilder sb) throws Exception {
        if ("gEmpty".equals(clazz.getSimpleName())
                || "gCookie".equals(clazz.getSimpleName())
                || "gHeader".equals(clazz.getSimpleName())
                || "ServletInfo".equals(clazz.getSimpleName())
                || "gNewCookie".equals(clazz.getSimpleName())
                || "FormMap".equals(clazz.getSimpleName())
                || "FormValues".equals(clazz.getSimpleName())
                || "dev.resteasy.grpc.arrays.Array_proto$dev_resteasy_grpc_arrays___NONE".equals(clazz.getName())
                || "dev.resteasy.grpc.arrays.Array_proto$dev_resteasy_grpc_arrays___Any".equals(clazz.getName())
                || "dev.resteasy.grpc.arrays.Array_proto$dev_resteasy_grpc_arrays___String".equals(clazz.getName())) {
            return;
        }
        if (NULLABLE_WRAPPERS.contains(clazz.getSimpleName())) {
            return;
        }
        System.out.println("createTranslatorToJavabuf(0): " + clazz.getName());
        //        if (("dev.resteasy.grpc.arrays." + args[2] + "_proto$dev_resteasy_grpc_arrays___ArrayHolder").equals(clazz.getName())) {
        if ((args[1] + "_proto$dev_resteasy_grpc_arrays___ArrayHolder").equals(clazz.getName())) {
            System.out.println("calling createArrayHolderTranslatorToJavabuf()");
            createArrayHolderTranslatorToJavabuf(args, sb);
            return;
        }
        //        if ("dev.resteasy.grpc.arrays".equals(clazz.getPackage().getName())) {
        //            return;
        //        }
        //        if (clazz.getName().endsWith("_wrapper") || clazz.getName().endsWith("_Array")) {
        if (clazz.getName().endsWith("_wrapper")) {
            return;
        }
        sb.append("   public static class ")
                .append(fqnify(clazz.getSimpleName())).append("_ToJavabuf implements TranslateToJavabuf {" + LS);
        System.out.println("createTranslatorToJavabuf(0): " + clazz.getName() + fqnify(clazz.getSimpleName()));
        if (clazz.getName().contains("String")) {
            System.out.println("STRING" + clazz.getName());
        }
        if (PRIMITIVE_WRAPPER_TYPES.containsKey(clazz.getSimpleName())) {
            String simpleJavabufName = clazz.getSimpleName();
            String simpleJavaName = simpleJavabufName.substring(1);
            sb.append("" + LS)
                    .append("      public Message assignToJavabuf(Object x) {" + LS)
                    .append("         if (x == null) {" + LS)
                    .append("            return null;" + LS)
                    .append("         }" + LS)
                    .append("         ")
                    .append(simpleJavaName)
                    .append(" p = (")
                    .append(simpleJavaName)
                    .append(") x;" + LS)
                    .append("         ")
                    .append(clazz.getCanonicalName())
                    .append(".Builder builder = ")
                    .append(clazz.getCanonicalName())
                    .append(".newBuilder();" + LS)
                    .append("         return builder.setValue(p")
                    .append(GET_METHODS.get(simpleJavaName))
                    .append(").build();" + LS)
                    .append("      }" + LS + LS)
                    .append("      public void clear() {" + LS)
                    .append("         //" + LS)
                    .append("      }" + LS);
        } else if (clazz.getName().endsWith("___Array") || clazz.getName().endsWith("___WArray")) {
            /*
             * public interface TranslateToJavabuf {
             * Message assignToJavabuf(Object obj);
             * void clear();
             * }
             */
            /*
             * static class dev_resteasy_grpc_arrays___shortArray_ToJavabuf implements TranslateToJavabuf {
             * public Message assignToJavabuf(Object x) {
             * if (x == null) {
             * return null;
             * }
             * short[] ss = (short[]) x;
             * dev_resteasy_grpc_arrays___shortArray.Builder builder = dev_resteasy_grpc_arrays___shortArray.newBuilder();
             * for (int i = 0; i < ss.length; i++) {
             * builder.addShortField(ss[i]);
             * }
             * return builder.build();
             * }
             *
             * public void clear() {
             * //
             * }
             * } dev_resteasy_grpc_arrays___ArrayHolder___WArray -> dev_resteasy_grpc_arrays
             */
            System.out.println("\nSimpleName: " + clazz.getSimpleName());
            String javabufComponentClass = null;
            boolean isWrapper;
            if (clazz.getSimpleName().contains("___WArray")) {
                javabufComponentClass = clazz.getSimpleName().substring(0, clazz.getSimpleName().lastIndexOf("___WArray"));
                isWrapper = true;
            } else {
                javabufComponentClass = clazz.getSimpleName().substring(0, clazz.getSimpleName().lastIndexOf("___Array"));
                isWrapper = false;
            }
            //ArrayBuilderClass 1: dev.resteasy.grpc.arrays.Array_proto$dev_resteasy_grpc_arrays___String___WArray, dev_resteasy_grpc_arrays___String___

            /*
             * ArrayBuilderClass 1:
             * dev.resteasy.grpc.arrays.Array_proto$dev_resteasy_grpc_arrays___Integer___WArray,
             * dev_resteasy_grpc_arrays___Integer___WArray
             * dev_resteasy_grpc_arrays___Integer
             *
             * %1: Integer
             * %2: dev_resteasy_grpc_arrays___Integer
             * %3: int
             * private static final String ArrayWrapperBuilderClass = //
             * "      public Message assignToJavabuf(Object x) {%n"
             * + "         if (x == null) {%n"
             * + "            return null;%n"
             * + "         }%n"
             * + "         %1$s[] aa = (%1$s[]) x;%n"
             * + "         %2$s.Builder elementBuilder = %2$s.newBuilder();%n"
             * + "         %2$s___WArray.Builder arrayBuilder = %2$s___WArray.newBuilder();%n"
             * + "         for (int i = 0; i < aa.length; i++) {%n"
             * + "            if (aa[i] == null) {%n"
             * + "               elementBuilder.clear().setNoneField(dev_resteasy_grpc_arrays___NONE.newBuilder());%n"
             * + "            } else {%n"
             * + "               elementBuilder.set%3$sField(aa[i]);%n"
             * + "            }%n"
             * + "            arrayBuilder.clear().add%1$sField(elementBuilder);%n"
             * + "         }%n"
             * + "         return arrayBuilder.build();%n"
             * + "      }%n"
             * + "%n"
             * + "      public void clear() {%n"
             * + "         //%n"
             * + "      }%n"
             * ;
             * ArrayBuilderClass 1:
             * dev.resteasy.grpc.arrays.Array_proto$dev_resteasy_grpc_arrays___Integer___WArray, [clazz.getName()]
             * dev_resteasy_grpc_arrays___Integer___WArray [clazz.getSimpleName()]
             * dev_resteasy_grpc_arrays___Integer [clazz.getSimpleName().substring(0,
             * clazz.getSimpleName().lastIndexOf("___WArray"));]
             *
             * %1: Integer [javaComponentClass]
             * %2: dev_resteasy_grpc_arrays___Integer [javabufComponentClass]
             * %3: int
             *
             * ArrayBuilderClass 1:
             * dev.resteasy.grpc.arrays.Array_proto$dev_resteasy_grpc_arrays___int___Array, [clazz.getName()]
             * dev_resteasy_grpc_arrays___int___Array, [clazz.getSimpleName()]
             * dev_resteasy_grpc_arrays___int [clazz.getSimpleName().substring(0,
             * clazz.getSimpleName().lastIndexOf("___WArray"));]
             */
            System.out
                    .println("ArrayBuilderClass 1: " + clazz.getName() + ", " + clazz.getSimpleName() + ", "
                            + javabufComponentClass);
            //            String javaComponentClass = javabufComponentClass.substring(javabufComponentClass.lastIndexOf("___"));
            //            String javaComponentClass = "jCC";
            System.out.println("ArrayBuilderClass 2: " + clazz.getName() + ", " + javabufComponentClass);
            //            String javaComponentClass = javabufComponentClass.substring(0, javabufComponentClass.lastIndexOf("___"));
            String javaComponentClass = javabufComponentClass;
            if (javabufComponentClass.startsWith("dev_resteasy_grpc_arrays___")
                    && !"dev_resteasy_grpc_arrays___ArrayHolder".equals(javabufComponentClass)) {
                javaComponentClass = javabufComponentClass.substring(javabufComponentClass.lastIndexOf("___") + 3);
            }
            /*
             * dev_resteasy_grpc_arrays___ArrayHolder___WArray
             *
             * java_util_HashMap_HIDDEN_Node, java_util_HashMap_HIDDEN_Node, JavaUtilHashMapHIDDENNode
             * ArrayHolder, dev_resteasy_grpc_arrays___ArrayHolder, DevResteasyGrpcArraysArrayHolder
             * Any, dev_resteasy_grpc_arrays___Any, DevResteasyGrpcArraysAny
             * String, dev_resteasy_grpc_arrays___String, DevResteasyGrpcArraysString
             * Character, dev_resteasy_grpc_arrays___Character, Char
             * Character, dev_resteasy_grpc_arrays___Character, Char
             * Double, dev_resteasy_grpc_arrays___Double, Double
             * Double, dev_resteasy_grpc_arrays___Double, Double
             * Float, dev_resteasy_grpc_arrays___Float, Float
             * Float, dev_resteasy_grpc_arrays___Float, Float
             * Long, dev_resteasy_grpc_arrays___Long, Long
             * Long, dev_resteasy_grpc_arrays___Long, Long
             * Integer, dev_resteasy_grpc_arrays___Integer, Int
             * Integer, dev_resteasy_grpc_arrays___Integer, Int
             * Short, dev_resteasy_grpc_arrays___Short, Short
             * Short, dev_resteasy_grpc_arrays___Short, Short
             * Byte, dev_resteasy_grpc_arrays___Byte, Byte
             * Byte, dev_resteasy_grpc_arrays___Byte, Byte
             * Boolean, dev_resteasy_grpc_arrays___Boolean, Boolean
             * Boolean, dev_resteasy_grpc_arrays___Boolean, Boolean
             *
             * WRAPPER_TO_PRIMITIVE_CAMEL.put("Boolean", "Boolean");
             * WRAPPER_TO_PRIMITIVE_CAMEL.put("Byte", "Byte");
             * WRAPPER_TO_PRIMITIVE_CAMEL.put("Short", "Short");
             * WRAPPER_TO_PRIMITIVE_CAMEL.put("Integer", "Int");
             * WRAPPER_TO_PRIMITIVE_CAMEL.put("Long", "Long");
             * WRAPPER_TO_PRIMITIVE_CAMEL.put("Float", "Float");
             * WRAPPER_TO_PRIMITIVE_CAMEL.put("Double", "Double");
             * WRAPPER_TO_PRIMITIVE_CAMEL.put("Character", "Char");
             * ArrayBuilderClass 3:
             * dev_resteasy_grpc_arrays, [javaComponentClass]
             * dev.resteasy.grpc.example.CC1_proto$dev_resteasy_grpc_arrays___ArrayHolder___WArray, [class]
             * dev_resteasy_grpc_arrays, [javabufComponentClass]
             * DevResteasyGrpcArrays [methodClass]
             *
             *
             */
            //            String methodClass = (WRAPPER_TO_PRIMITIVE_CAMEL.containsKey(javaComponentClass))
            //                    ? WRAPPER_TO_PRIMITIVE_CAMEL.get(javaComponentClass)
            //                    : squashToCamel(javabufComponentClass);
            String methodClass = squashToCamel(javabufComponentClass);
            /*
             * dev.resteasy.grpc.example.CC1_proto$java_util_HashMap_HIDDEN_Node___Array,
             * va_util_HashMap_HIDDEN_Node,
             * java_util_HashMap_HIDDEN_Node,
             * JavaUtilHashMapHIDDENNode
             */
            System.out.println("ArrayBuilderClass 3: "
                    + javaComponentClass + ", "
                    + clazz.getName() + ", "
                    + javabufComponentClass + ", "
                    + methodClass);
            if ("Byte".equals(javaComponentClass)) {
                System.out.println("ArrayBuilderClass: Byte");
                sb.append(String.format(ArrayWrapperBuilderClassForBytes, javaComponentClass, javabufComponentClass));
            } else if ("Character".equals(javaComponentClass)) {
                sb.append(String.format(ArrayWrapperBuilderClassForCharacters, javaComponentClass, javabufComponentClass));
            } else if (JAVA_WRAPPER_TYPES.contains(javaComponentClass)) {
                if (isWrapper) {
                    sb.append(String.format(ArrayPrimitiveWrapperBuilderClass, javaComponentClass, javabufComponentClass,
                            javaComponentClass));
                } else {
                    String primitiveClass = WRAPPER_TO_PRIMITIVE.get(javaComponentClass);
                    methodClass = primitiveClass.substring(0, 1).toUpperCase() + primitiveClass.substring(1);
                    sb.append(String.format(ArrayPrimitiveBuilderClass, primitiveClass, javaComponentClass, methodClass));
                }
            } else {
                sb.append(String.format(ArrayWrapperBuilderClass, javaComponentClass, javabufComponentClass, methodClass));
            }
        } else {
            String classname = null;
            //clazz: class dev.resteasy.grpc.arrays.Array_proto$dev_resteasy_grpc_arrays___NONE, enclosing: class dev.resteasy.grpc.arrays.Array_proto, public: true, simple: dev_resteasy_grpc_arrays___NONE
            System.out.println("clazz: " + clazz + ", enclosing: " + clazz.getEnclosingClass() + ", public: "
                    + Modifier.isPublic(clazz.getModifiers()) + ", simple: " + clazz.getSimpleName());
            if (clazz.getEnclosingClass() == null || Modifier.isPublic(clazz.getModifiers())) {
                classname = originalSimpleName(clazz.getSimpleName());
            } else {
                Class<?> enclosingClass = clazz.getEnclosingClass();
                classname = "Utility.getHiddenClass(" + enclosingClass.getName() + ", " + clazz.getSimpleName() + ")";
            }
            System.out.println("getJavabufClassValue(clazz.getSimpleName(), false, false): " + clazz.getSimpleName()
                    + getJavabufClassValue(clazz.getSimpleName(), false, false));
            sb.append("      private static Descriptor descriptor = ").append(clazz.getCanonicalName())
                    .append(".getDescriptor();" + LS)
                    //                    .append("      private static ").append(fqnify(clazz.getSimpleName())).append(".Builder builder = ")
                    //                    .append(fqnify(clazz.getSimpleName())).append(".newBuilder();" + LS)
                    .append("      private static List<AssignToJavabuf> assignList = new ArrayList<AssignToJavabuf>();" + LS
                            + LS)
                    .append("      static {" + LS)
                    .append("         for (FieldDescriptor f : descriptor.getFields()) {" + LS)
                    .append("            String name = f.getName();" + LS)
                    .append("            if (descriptor.findFieldByName(name) == null) {" + LS)
                    .append("               continue;" + LS)
                    .append("            }" + LS)
                    //                    .append("            assignList.add(toJavabuf(").append(originalSimpleName(clazz.getSimpleName()))
                    //                    .append("            assignList.add(toJavabuf(").append(fromJavabufClassValue(classname));
                    .append("            assignList.add(toJavabuf(")
                    //                    .append(getJavabufClassValue(originalSimpleName(clazz.getSimpleName()), false));
                    //                    .append(getJavabufClassValue(originalSimpleName(clazz.getSimpleName()), false));
                    .append(getJavabufClassValue(clazz.getSimpleName(), false, false));
            //                  .append("            assignList.add(toJavabuf(").append(originalSimpleName(clazz.getSimpleName()))

            System.out
                    .println("createTranslatorToJavabuf(7): " + originalSimpleName(clazz.getSimpleName()) + ", "
                            + getJavabufClassValue(clazz.getSimpleName(), false, true));
            System.out
                    .println("createTranslatorToJavabuf(8): " + clazz.getSimpleName() + ", " + clazz.getSimpleName());
            //                    .append(".class, descriptor.findFieldByName(name)));" + LS)
            sb.append(", descriptor.findFieldByName(name)));" + LS)
                    .append("         }" + LS)
                    .append("      }" + LS + LS)
                    /*
                     * if (c1 == null) {
                     * return null;
                     * }
                     */
                    .append("      public Message assignToJavabuf(Object c1) {" + LS)
                    .append("         if (c1 == null) {" + LS)
                    .append("            return null;" + LS)
                    .append("         }" + LS)
                    .append("         ").append(fqnify(clazz.getSimpleName())).append(".Builder builder = ")
                    .append(fqnify(clazz.getSimpleName())).append(".newBuilder();" + LS)
                    .append("         for (AssignToJavabuf assignTo : assignList) {" + LS)
                    .append("            try {" + LS)
                    .append("               assignTo.assign(c1, builder);" + LS)
                    .append("            } catch (Exception e) {" + LS)
                    .append("               throw new RuntimeException(e);" + LS)
                    .append("            }" + LS)
                    .append("         }" + LS)
                    .append("         return builder.build();" + LS)
                    .append("      }" + LS + LS)
                    .append("      public void clear() {" + LS)
                    //                    .append("         builder.clear();" + LS)
                    .append("      }" + LS);
        }
        sb.append("   }" + LS + LS);
    }

    private static void createArrayHolderTranslatorToJavabuf(String[] args, StringBuilder sb) {
        sb.append("//////// ArrayHolder to Javabuf  ////////" + LS + LS);
        sb.append("   public static class dev_resteasy_grpc_arrays___ArrayHolder_ToJavabuf implements TranslateToJavabuf {" + LS
                + LS)
                .append("      public Message assignToJavabuf(Object obj) {" + LS)
                .append("         return ArrayUtility.getHolder(translator, obj);" + LS)
                .append("      }" + LS + LS)
                .append("      public void clear() {" + LS)
                .append("         //" + LS)
                .append("      }" + LS)
                .append("   }" + LS);
    }

    private static void createTranslatorFromJavabuf(String[] args, Class<?> clazz, StringBuilder sb)
            throws Exception {
        if (clazz.isInterface()) {
            return;
        }
        String originalName = originalSimpleName(clazz.getName());
        //dev.resteasy.grpc.example.CC1_proto$dev_resteasy_grpc_arrays___ArrayHolder
        System.out.println("createTranslatorFromJavabuf(): clazz: " + clazz.getName());
        System.out.println("createTranslatorFromJavabuf(): dev.resteasy.grpc.arrays." + args[2]
                + "proto$dev_resteasy_grpc_arrays___ArrayHolder.equals(clazz.getName())");
        //        if (("dev.resteasy.grpc.arrays." + args[2] + "proto$dev_resteasy_grpc_arrays___ArrayHolder").equals(clazz.getName())) {
        if ((args[1] + "_proto$dev_resteasy_grpc_arrays___ArrayHolder").equals(clazz.getName())) {
            System.out.println("createTranslatorFromJavabuf() calling createArrayHolderTranslatorFromJavabuf()");
            createArrayHolderTranslatorFromJavabuf(args, sb);
            return;
        }
        if ("dev.resteasy.grpc.arrays".equals(clazz.getPackage().getName())) {
            return;
        }
        //        String originalName = originalSimpleName(clazz.getName());
        System.out.println("createTranslatorFromJavabuf(): clazz: " + clazz.getName() + ", originalName: " + originalName);
        if ("gEmpty".equals(originalName)) {
            return;
        }
        if ("AbstractMessage".equals(clazz.getSimpleName())
                || "gCookie".equals(clazz.getSimpleName())
                || "gNewCookie".equals(clazz.getSimpleName())
                || "gHeader".equals(clazz.getSimpleName())
                || "ServletInfo".equals(clazz.getSimpleName())
                || "FormMap".equals(clazz.getSimpleName())
                || "FormValues".equals(clazz.getSimpleName())) {
            return;
        }
        if (PRIMITIVE_WRAPPER_TYPES.containsKey(originalName)) {
            sb.append("   public static class ")
                    .append(fqnify(clazz.getSimpleName())).append("_FromJavabuf implements TranslateFromJavabuf {" + LS)
                    .append("      private static Descriptor descriptor = ").append(clazz.getCanonicalName())
                    .append(".getDescriptor();" + LS);
            String javaName = originalName.substring(1);
            if ("gByte".equals(originalName)) {
                sb.append("      public ").append(javaName).append(" assignFromJavabuf(Message message) {" + LS)
                        .append("         FieldDescriptor fd = descriptor.getFields().get(0);" + LS)
                        .append("         return ((Integer) message.getField(fd)).byteValue();" + LS)
                        .append("      }" + LS + LS)
                        .append("      public void assignExistingFromJavabuf(Message message, Object obj) { }" + LS);
            } else if ("gShort".equals(originalName)) {
                sb.append("      public ").append(javaName).append(" assignFromJavabuf(Message message) {" + LS)
                        .append("         FieldDescriptor fd = descriptor.getFields().get(0);" + LS)
                        .append("         return ((Integer) message.getField(fd)).shortValue();" + LS)
                        .append("      }" + LS + LS)
                        .append("      public void assignExistingFromJavabuf(Message message, Object obj) { }" + LS);
            } else if ("gCharacter".equals(originalName)) {
                sb.append("      public ").append(javaName).append(" assignFromJavabuf(Message message) {" + LS)
                        .append("         FieldDescriptor fd = descriptor.getFields().get(0);" + LS)
                        .append("         return ((String) message.getField(fd)).charAt(0);" + LS)
                        .append("      }" + LS + LS)
                        .append("      public void assignExistingFromJavabuf(Message message, Object obj) { }" + LS);
            } else {
                sb.append("      public ").append(javaName).append(" assignFromJavabuf(Message message) {" + LS)
                        .append("         FieldDescriptor fd = descriptor.getFields().get(0);" + LS)
                        .append("         return (").append(javaName).append(") message.getField(fd);" + LS)
                        .append("      }" + LS + LS)
                        .append("      public void assignExistingFromJavabuf(Message message, Object obj) { }" + LS);
            }
        } else {
            //            Constructor<?> con = findConstructor(clazz, originalName, sb);
            //            System.out.println("findConstructor(): " + clazz + ", " + con);
            //            if (con == null) {
            //                return;
            //            }
            System.out.println("createTranslatorFromJavabuf(2): " + clazz.getSimpleName() + ", originalName: " + originalName);
            System.out.println("createTranslatorFromJavabuf(2): java:" + javabufToJava(clazz.getName(), originalName, true));
            System.out.println("createTranslatorFromJavabuf(2.3):");
            //            System.out.println("createTranslatorFromJavabuf(2): public: "
            //                    + isNonPublicInnerClass(clazz.getSimpleName(), javabufToJava(clazz.getName(), originalName, true)));
            Constructor<?> cons = findConstructor(clazz, originalName);
            if (cons == null) { // abstract class
                return;
            }
            System.out.println("createTranslatorFromJavabuf(2.4):");
            sb.append("   public static class ")
                    .append(fqnify(clazz.getSimpleName())).append("_FromJavabuf implements TranslateFromJavabuf {" + LS)
                    .append("      private static Descriptor descriptor = ").append(clazz.getCanonicalName())
                    .append(".getDescriptor();" + LS);
            System.out.println("createTranslatorFromJavabuf(2.5):");
            sb.append("      private static List<AssignFromJavabuf> assignList = new ArrayList<AssignFromJavabuf>();" + LS + LS)
                    .append("      static {" + LS)
                    .append("         for (FieldDescriptor f : descriptor.getFields()) {" + LS)
                    .append("            String name = f.getName();" + LS)
                    .append("            if (descriptor.findFieldByName(name) == null) {" + LS)
                    .append("               continue;" + LS)
                    .append("            }" + LS)
                    //                    .append("            assignList.add(fromJavabuf(").append(originalName)
                    .append("            assignList.add(fromJavabuf(")
                    .append(getJavabufClassValue(clazz.getSimpleName(), false, false))
                    //                    .append(".class, descriptor.findFieldByName(name)));" + LS)
                    .append(", descriptor.findFieldByName(name)));" + LS)

                    //                    .append(", descriptor.findFieldByName(name)));" + LS)
                    .append("         }" + LS)
                    .append("      }" + LS + LS);
            System.out.println("createTranslatorFromJavabuf(2.6):");
            //            Constructor<?> con = findConstructor(clazz, originalName, sb);
            //            System.out.println("findConstructor(): " + clazz + ", " + con);
            //            if (con != null) {
            //            sb.append("      public ").append(originalName).append(" assignFromJavabuf(Message message) {" + LS);
            //            Constructor<?> con = findConstructor(clazz, originalName, sb);
            //            System.out.println("findConstructor(): " + clazz + ", " + con);
            //            if (con != null) {
            System.out.println("createTranslatorFromJavabuf(3): clazz.getName(): " + clazz.getName());
            if (clazz.getName().contains("_HIDDEN_")) {
                sb.append("      public Object assignFromJavabuf(Message message) {" + LS)
                        .append("         try {" + LS)
                        .append("            Object obj = getConstructor(\"")
                        .append(originalInnerClassName(clazz.getSimpleName()))
                        //                        .append("\");" + LS)
                        .append("\").newInstance(");
                //                writeArguments(findConstructor(clazz, originalName), sb);
                writeArguments(cons, sb);
                sb.append(");" + LS)
                        .append("            for (AssignFromJavabuf assignFrom : assignList) {" + LS)
                        //                        .append("               try {" + LS)
                        .append("               assignFrom.assign(message, obj);" + LS)
                        .append("            }" + LS)
                        .append("            return obj;" + LS)
                        .append("            } catch (Exception e) {" + LS)
                        .append("               throw new RuntimeException(e);" + LS)
                        .append("            }" + LS);
            } else {
                Constructor<?> con = findConstructor(clazz, originalName);
                if (con != null) {
                    sb.append("      public ").append(originalName).append(" assignFromJavabuf(Message message) {" + LS);
                    writeConstructor(con, originalName, sb);
                    sb.append(");" + LS)
                            .append("         for (AssignFromJavabuf assignFrom : assignList) {" + LS)
                            .append("            try {" + LS)
                            .append("               assignFrom.assign(message, obj);" + LS)
                            .append("            } catch (Exception e) {" + LS)
                            .append("               throw new RuntimeException(e);" + LS)
                            .append("            }" + LS)
                            .append("         }" + LS)
                            .append("         return obj;" + LS);
                } else {
                    sb.append("      public ").append(originalName).append(" assignFromJavabuf(Message message) {" + LS);
                    sb.append("         return null;" + LS);
                }
            }
            //            else {
            //                sb.append("         return null;" + LS);
            //            }
            sb.append("      }" + LS + LS);
            sb.append("      public void assignExistingFromJavabuf(Message message, Object obj) {" + LS)
                    .append("         for (AssignFromJavabuf assignFrom : assignList) {" + LS)
                    .append("            try {" + LS)
                    .append("               assignFrom.assign(message, obj);" + LS)
                    .append("            } catch (Exception e) {" + LS)
                    .append("               throw new RuntimeException(e);" + LS)
                    .append("            }" + LS)
                    .append("         }" + LS)
                    .append("      }" + LS);
        }
        sb.append("   }" + LS + LS);
    }

    private static void createArrayHandlingClasses(StringBuilder sb) {
        for (Class<?> c : arrayClasses) {
            String componentClass = c.getSimpleName().substring(0, c.getSimpleName().lastIndexOf("_WArray"));
            String squash = squashToCamel(c.getSimpleName());
            System.out.println("createArrayHandlingClasses(): " + c.getSimpleName() + ", " + squash
                    + ", " + componentClass);
            //            sb.append(String.format(ArrayWrapperBuilderMethod, componentClass, squashToCamel(componentClass)));
            //            sb.append(LS + LS);
        }
    }

    private static void createArrayHolderTranslatorFromJavabuf(String[] args, StringBuilder sb) {
        System.out.println("entering createArrayHolderTranslatorFromJavabuf()");
        String prefix = args[1].substring(args[1].lastIndexOf('.') + 1);
        sb.append("//////// ArrayHolder from Javabuf" + LS + LS)
                .append("   public static class dev_resteasy_grpc_arrays___ArrayHolder_FromJavabuf implements TranslateFromJavabuf {"
                        + LS)
                .append("      public Object assignFromJavabuf(Message message) {" + LS)
                .append("         try {" + LS)
                .append("            return ArrayUtility.getArray(translator, (dev_resteasy_grpc_arrays___ArrayHolder) message);"
                        + LS)
                .append("         } catch (Exception e) {" + LS)
                .append("            throw new RuntimeException(e);" + LS)
                .append("         }" + LS)
                .append("      }" + LS + LS)
                .append("        public void assignExistingFromJavabuf(Message message, Object obj) {" + LS)
                .append("      }" + LS)
                .append("   }" + LS + LS);
        sb.append("   static Object getArray(Field field, int length) {" + LS)
                .append("      Class<?> clazz = field.getType();" + LS)
                .append("      Class<?> componentClass = clazz.getComponentType();" + LS)
                .append("      return Array.newInstance(componentClass, length);" + LS)
                .append("   }" + LS + LS);
        //        sb.append("   }" + LS);
        /*
         * static Message wrapArrayElement(Message m, int position, FieldDescriptor fd) {
         * ELEMENT_WRAPPER.Builder builder = ELEMENT_WRAPPER.newBuilder();
         * System.out.println(m.getDescriptorForType().getName());
         *
         * switch (m.getDescriptorForType().getName()) {
         * case "java_util_HashMap_HIDDEN_Node":
         * return builder.setJavaUtilHashMapHIDDENNodeField((java_util_HashMap_HIDDEN_Node) m)
         * .setPosition(position).build();
         * }
         */
    }

    private static void finishClass(StringBuilder sb) {
        sb.append("}" + LS);
    }

    private static void writeTranslatorClass(String[] args, String translatorClass, StringBuilder sb) throws IOException {
        String pkgPath = args[1].lastIndexOf(".") < 0 ? ""
                : args[1].substring(0, args[1].lastIndexOf(".")).replace(".", File.separator);
        Path path = Files.createDirectories(Path.of(args[0], pkgPath));
        if (path.resolve(translatorClass + ".java").toFile().exists()) {
            return;
        }
        Files.writeString(path.resolve(translatorClass + ".java"), sb.toString(), StandardCharsets.UTF_8);
    }

    private static String fqnify(String s) {
        return s.replace(".", "_");
    }

    private static String originalSimpleName(String s) {
        int i = s.lastIndexOf("___");
        if (i >= 0) {
            return s.substring(i + 3).replace('$', '.');
        }
        // inner class
        i = s.indexOf("_INNER_");
        if (i >= 0) {
            return s.substring(i + "_INNER_".length());
        }
        i = s.indexOf("_HIDDEN_");
        if (i >= 0) {
            return s.substring(i + "_HIDDEN_".length());
        }
        // primitive class
        i = s.lastIndexOf("$");
        if (i >= 0) {
            return s.substring(i + 1);
        }
        if (PRIMITIVE_WRAPPER_TYPES.containsKey(s)) {
            return s.substring(1);
        }
        return s;
    }

    private static String originalCanonicalClassName(String s) {
        System.out.println("originalCanonicalClassName(): " + s);
        if (PRIMITIVE_WRAPPER_TYPES.containsKey(s)) {
            return "java.lang." + s.substring(1);
        }
        int i = s.indexOf("$");
        if (i >= 0) {
            s = s.substring(i + 1);
        }
        int j = s.lastIndexOf("___");
        if (j < 0) {
            j = s.indexOf("_INNER_");
        }
        if (j < 0) {
            j = s.indexOf("_HIDDEN_");
        }
        if (j >= 0) {
            String pkg = s.substring(0, j).replace('_', '.');
            return pkg + "." + originalSimpleName(s);
        }
        return s;
    }

    private static String originalClassName(String s) {
        System.out.println("originalClassName(): in: " + s);
        if (PRIMITIVE_WRAPPER_TYPES.containsKey(s)) {
            return "java.lang." + s.substring(1);
        }
        //        int i = s.indexOf("$");
        //        if (i >= 0) {
        //            s = s.substring(i + 1);
        //        }
        if (s.endsWith("___Array")) {
            s = s.substring(0, s.length() - 8);
        } else if (s.endsWith("___WArray")) {
            s = s.substring(0, s.length() - 9);
        }
        int j = s.lastIndexOf("___");
        if (j >= 0) {
            String pkg = s.substring(0, j).replace('_', '.');
            return pkg + "." + originalSimpleName(s);
        }
        if (j < 0) {
            j = s.indexOf("_INNER_");
        }
        if (j < 0) {
            j = s.indexOf("_HIDDEN_");
        }
        if (j >= 0) {
            String pkg = s.substring(0, j).replace('_', '.');
            return pkg + "$" + originalSimpleName(s);
        }
        System.out.println("originalClassName(): out: " + s);
        //        return s;
        throw new RuntimeException("don't recognize: " + s);
    }

    private static String originalInnerClassName(String s) {
        System.out.println("originalClassName(): " + s);
        int i = s.indexOf("$");
        if (i >= 0) {
            s = s.substring(i + 1);
        }
        int j = s.indexOf("_INNER_");
        if (j < 0) {
            j = s.indexOf("_HIDDEN_");
        }
        String pkg = s.substring(0, j).replace('_', '.');
        return pkg + "$" + originalSimpleName(s);
    }

    private static Constructor<?> findConstructor(Class<?> clazz, String originalName)
            throws ClassNotFoundException {
        try {
            String className = javabufToJava(clazz.getName(), originalName, true);
            System.out.println(
                    "findConstructor(): clazz: " + clazz + ", originalName: " + originalName + ", className: " + className);
            Class<?> originalClazz = Class.forName(className);
            System.out.println("originalClazz: " + originalClazz.getName());
            if (Modifier.isAbstract(originalClazz.getModifiers())) {
                System.out.println("abstract: " + originalClazz);
                return null;
            }
            Constructor<?>[] cons = originalClazz.getDeclaredConstructors();
            if (cons.length == 0) {
                return null;
            }
            Constructor<?> con = cons[0];
            if (cons.length > 1) {
                for (int i = 1; i < cons.length; i++) {
                    if (cons[i].getParameterCount() < con.getParameterCount()) {
                        con = cons[i];
                    }
                }
            }
            return con;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void writeConstructor(Constructor<?> con, String originalName, StringBuilder sb) {
        sb.append("         ").append(originalName).append(" obj = new ").append(originalName).append("(");
        writeArguments(con, sb);
        //        boolean first = true;
        //        for (int i = 0; i < con.getParameterCount(); i++) {
        //            if (first) {
        //                first = false;
        //            } else {
        //                sb.append(", ");
        //            }
        //            if (PRIMITIVE_DEFAULTS.containsKey(con.getParameterTypes()[i])) {
        //                sb.append(PRIMITIVE_DEFAULTS.get(con.getParameterTypes()[i]));
        //            } else {
        //                sb.append("null");
        //            }
        //        }
        //        sb.append(");" + LS);
    }

    private static void writeArguments(Constructor<?> con, StringBuilder sb) {
        System.out.println("writeArguments(): " + con.getName() + ", " + con.getParameterCount());
        boolean first = true;
        for (int i = 0; i < con.getParameterCount(); i++) {
            System.out.println("param[" + i + "]: " + con.getParameters()[i]);
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            if (PRIMITIVE_DEFAULTS.containsKey(con.getParameterTypes()[i])) {
                sb.append(PRIMITIVE_DEFAULTS.get(con.getParameterTypes()[i]));
            } else {
                sb.append("null");
            }
        }
    }
    //staticInit(): class dev.resteasy.grpc.arrays.Array_proto$dev_resteasy_grpc_arrays___Long___Array, Array, dev.resteasy.grpc.arrays...Long.Array

    private static String javabufToJava(String javabufName, String simpleName, boolean canonical) {
        System.out.println("javabufToJava(): javabufName: " + javabufName + ", simpleName: " + simpleName);
        //dev_resteasy_grpc_example_CC3_INNER_CC3_Sub
        String tmp = javabufName;
        String tmpPkg = "";
        int n = tmp.lastIndexOf("$");
        if (n >= 0) {
            tmp = tmp.substring(n + 1);
        }
        n = tmp.lastIndexOf("___");
        System.out.println("javabufToJava(0): n: " + n);
        if (n >= 0) {
            tmpPkg = tmp.substring(0, n);
        } else {
            n = tmp.indexOf("_INNER_");
            System.out.println("javabufToJava(1): n: " + n);
            if (n >= 0) {
                tmpPkg = tmp.substring(0, n);
                tmpPkg = tmpPkg.replace("_", ".");
                //                n = simpleName.indexOf("_INNER_");
                //                if (n >= 0) {
                if (canonical) {
                    tmp = tmpPkg + "$" + tmp.substring(n + "_INNER_".length());
                } else {
                    tmp = tmpPkg + "." + tmp.substring(n + "_INNER_".length());
                }
                //                }
                //                tmp = tmp.replace("_", ".");
                System.out.println("javabufToJava(): returning " + tmp);
                return tmp;
            }
            n = tmp.indexOf("_HIDDEN_");
            System.out.println("javabufToJava(1): n: " + n);
            if (n >= 0) {
                tmpPkg = tmp.substring(0, n);
                tmp = tmpPkg + "$" + tmp.substring(n + "_HIDDEN_".length());
                tmp = tmp.replace("_", ".");
                System.out.println("javabufToJava(): returning " + tmp);
                return tmp;
            }
        }
        System.out.println("tmpPkg 1: " + tmpPkg);
        if (tmpPkg.length() > 0) {
            tmpPkg = tmpPkg.replace("_", ".");
            System.out.println("tmpPkg 2: " + tmpPkg);
            System.out.println("javabufToJava(2.5): returning: " + tmpPkg + "." + simpleName);
            return tmpPkg + "." + simpleName;
        }
        return simpleName;
    }

    private static String getJavabufClassValue(String simpleName, boolean finesseNonHiddenClasses, boolean canonical) {
        System.out.println("java classname: " + simpleName);
        System.out.println("java originalCanonicalClassName(classname): " + originalCanonicalClassName(simpleName));
        if (PRIMITIVE_WRAPPER_TYPES.containsKey(simpleName)) {
            return PRIMITIVE_WRAPPER_TYPES.get(simpleName).getName() + ".class";
        }
        try {
            if (simpleName.contains("_HIDDEN_")) {
                String classname = originalInnerClassName(simpleName);
                System.out.println("fromJavabufClassValue(): classname: " + classname);
                try {
                    Class<?> clazz = Class.forName(classname);
                    System.out.println("fromJavabufClassValue(): " + clazz.getCanonicalName());
                    //               if (Modifier.isPublic(clazz.getModifiers())) {
                    //                   return "getReturnJavaClass(" + originalCanonicalClassName(simpleName) + ".class)";
                    //               } else {
                    System.out.println("clazz.getCanonicalName(): " + clazz.getCanonicalName());
                    System.out.println(
                            "fromJavabufClassValue(2): returning \"getReturnNonPublicJavaClass(\" + clazz.getName() + \"");
                    //                return "getReturnNonPublicJavaClass(\"" + clazz.getCanonicalName() + "\")";
                    return "getReturnNonPublicJavaClass(\"" + clazz.getName() + "\").getJavaClass()";
                } catch (Exception e) {
                    System.out.println("getJavabufClassValue(exception): " + e.getClass() + ", " + e.getMessage());
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
                //               }
            }
            if (finesseNonHiddenClasses) {

                //            } else if (simpleName.contains("_INNER_")) {
                //                String classname = originalInnerClassName(simpleName);
                //                System.out.println("fromJavabufClassValue(): classname: " + classname);
                //                Class<?> clazz = Class.forName(classname);
                //                System.out.println("fromJavabufClassValue(): " + clazz.getCanonicalName());
                //                //                if (Modifier.isPublic(clazz.getModifiers())) {
                //                System.out.println(
                //                        "fromJavabufClassValue(1): returning \"getReturnJavaClass(\" + originalCanonicalClassName(simpleName) + .class)");
                return "getReturnJavaClass(" + originalCanonicalClassName(simpleName) + ".class)";
                //                } else {
                //                    return "getReturnNonPublicJavaClass(\"" + clazz.getCanonicalName() + "\")";
                //                }
            }
            //            return "getReturnJavaClass(" + originalCanonicalClassName(simpleName) + ".class)";
            System.out.println("getJavabufClassValue(11): " + javabufToJava(simpleName, originalSimpleName(simpleName), true));
            return javabufToJava(simpleName, originalSimpleName(simpleName), canonical) + ".class";
        } catch (Exception e) {
            e.printStackTrace();
            return "null";
        }
    }

    private static boolean isNonPublicInnerClass(String classname, String javaName) throws Exception {
        if (!classname.contains("_INNER_") && !classname.contains("_HIDDEN_")) {
            return false;
        }
        Class<?> clazz = Class.forName(javaName);
        return !Modifier.isPublic(clazz.getModifiers());
    }

    /*
     * public void set_java_util_HashMap_HIDDEN_Node_Array(Object[] array) {
     * dev_resteasy_grpc_arrays___ArrayHolder.Builder ahb = dev_resteasy_grpc_arrays___ArrayHolder.newBuilder();
     * java_util_HashMap_HIDDEN_Node_Array.Builder ab = java_util_HashMap_HIDDEN_Node_Array.newBuilder();
     * CC1_proto.java_util_HashMap_HIDDEN_Node_wrapper.Builder wb = java_util_HashMap_HIDDEN_Node_wrapper.newBuilder();
     * for (int i = 0; i < Array.getLength(array); i++) {
     * if (array[i] == null) {
     * wb.clear().setNoneField(Array_proto.dev_resteasy_grpc_arrays___NONE.newBuilder().build());
     * } else {
     * wb.clear().setJavaUtilHashMapHIDDENNodeField((java_util_HashMap_HIDDEN_Node) INSTANCE.translateToJavabuf(array[i]));
     * }
     * ab.addJavaUtilHashMapHIDDENNodeWrapperField(wb);
     * }
     * ahb.setJavaUtilHashMapHIDDENNodeArrayField(ab);
     * }
     */
    /*
     * for (Class<?> innerClazz : clazz.getDeclaredClasses()) {
     * System.out.println("innerClazz: " + innerClazz.getName());
     * if (innerClazz.getName().endsWith("$Builder")) {
     * builderClazz = innerClazz;
     * for (Method m : innerClazz.getDeclaredMethods()) {
     * if (m.getName().startsWith("set")) {
     * System.out.println("m set: " + m.getName());
     * }
     * }
     * }
     * if (innerClazz.getName().endsWith("MessageTypeCase")) {
     * enumClazz = innerClazz;
     * }
     */
    private static void createArrayHolderBuilderMethods(StringBuilder sb) {

    }
}
