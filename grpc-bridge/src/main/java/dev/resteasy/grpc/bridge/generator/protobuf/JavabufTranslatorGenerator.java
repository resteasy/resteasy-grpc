package dev.resteasy.grpc.bridge.generator.protobuf;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.logging.Logger;

import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;

import dev.resteasy.grpc.arrays.ArrayHolder;
import dev.resteasy.grpc.arrays.ArrayUtility;
import dev.resteasy.grpc.arrays.Array_proto;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___ArrayHolder;
import dev.resteasy.grpc.bridge.runtime.protobuf.AssignFromJavabuf;
import dev.resteasy.grpc.bridge.runtime.protobuf.AssignToJavabuf;
import dev.resteasy.grpc.bridge.runtime.protobuf.JavabufTranslator;
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
    }

    public static void main(String[] args) {
        if (args == null || args.length != 2) {
            logger.info("need two args:");
            logger.info("  arg[0]: root directory");
            logger.info("  arg[1]: javabuf wrapper class name");
            return;
        }
        try {
            int index = args[1].lastIndexOf('.');
            String simpleName = index < 0 ? args[1] : args[1].substring(index + 1);
            String translatorClass = simpleName + "JavabufTranslator";
            Class<?>[] wrappedClasses = getWrappedClasses(args);
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
            if ("dev.resteasy.grpc.arrays.Array_proto".equals(clazz.getName())) {
                wrappedClasses.add(ArrayHolder.class);
            }
            wrappedClasses.addAll(Arrays.asList(clazz.getClasses()));
        }
        return wrappedClasses.toArray(new Class<?>[wrappedClasses.size()]);
    }

    private static List<Class<?>> getWrapperClasses(String[] args) throws ClassNotFoundException {
        List<Class<?>> list = new ArrayList<Class<?>>();
        Class<?> wrapperClass = Class.forName(args[1] + "_proto", true, Thread.currentThread()
                .getContextClassLoader());
        list.add(wrapperClass);
        try {
            Class<?> arrayWrapperClass = Class.forName("dev.resteasy.grpc.arrays.Array_proto", true,
                    Thread.currentThread().getContextClassLoader());
            list.add(arrayWrapperClass);
            return list;
        } catch (Exception ignore) {
            // Array_proto class is not available: ignore
            return list;
        }
    }

    private static void classHeader(String[] args, String translatorClass, Class<?>[] wrappedClasses, StringBuilder sb) {
        sb.append("package ").append(wrappedClasses[0].getPackage().getName()).append(";" + LS + LS);
        imports(wrappedClasses, sb);
        sb.append("public class ")
                .append(translatorClass)
                .append(" implements JavabufTranslator {" + LS);
    }

    private static void imports(Class<?>[] wrappedClasses, StringBuilder sb) {
        sb.append("import java.lang.reflect.Array;" + LS)
                .append("import java.lang.reflect.Field;" + LS)
                .append("import java.util.ArrayList;" + LS)
                .append("import java.util.HashMap;" + LS)
                .append("import java.util.HashSet;" + LS)
                .append("import java.util.List;" + LS)
                .append("import java.util.Map;" + LS)
                .append("import java.util.Set;" + LS)
                .append("import com.google.protobuf.ByteString;" + LS)
                .append("import com.google.protobuf.Descriptors;" + LS)
                .append("import com.google.protobuf.Descriptors.Descriptor;" + LS)
                .append("import com.google.protobuf.Descriptors.FieldDescriptor;" + LS)
                .append("import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;" + LS)
                .append("import com.google.protobuf.DynamicMessage;" + LS)
                .append("import com.google.protobuf.Internal.IntList;" + LS)
                .append("import com.google.protobuf.Message;" + LS)
                .append("import ").append(ArrayUtility.class.getCanonicalName()).append(";" + LS)
                .append("import ").append(AssignFromJavabuf.class.getCanonicalName()).append(";" + LS)
                .append("import ").append(AssignToJavabuf.class.getCanonicalName()).append(";" + LS)
                .append("import ").append(JavabufTranslator.class.getCanonicalName()).append(";" + LS)
                .append("import ").append(TranslateFromJavabuf.class.getCanonicalName()).append(";" + LS)
                .append("import ").append(TranslateToJavabuf.class.getCanonicalName()).append(";" + LS)
                .append("import dev.resteasy.grpc.arrays.Array_proto;" + LS)
                .append("import " + Array_proto.dev_resteasy_grpc_arrays___ArrayHolder.class.getCanonicalName())
                .append(";" + LS);
        for (Class<?> clazz : wrappedClasses) {
            if (clazz.isInterface()) {
                continue;
            }
            if ("dev.resteasy.grpc.arrays".equals(clazz.getPackageName()) && !ArrayHolder.class.equals(clazz)) {
                continue;
            }
            String simpleName = clazz.getSimpleName();
            if ("gEmpty".equals(simpleName)) {
                continue;
            }
            if (PRIMITIVE_WRAPPER_TYPES.containsKey(simpleName)) {
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
            } else {
                sb.append("import ")
                        .append(originalClassName(clazz.getName()))
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
        publicMethods(sb);
        privateMethods(sb, args);
        for (Class<?> clazz : wrappedClasses) {
            if (clazz.isInterface()) {
                continue;
            }
            String simpleName = clazz.getSimpleName();
            if ("GeneralEntityMessage".equals(simpleName) || "GeneralReturnMessage".equals(simpleName)) {
                continue;
            }
            createTranslator(args, clazz, sb);
        }
    }

    private static void staticInit(Class<?>[] classes, StringBuilder sb) {
        sb.append("   static {" + LS);
        for (Class<?> clazz : classes) {
            if (clazz.isInterface()) {
                continue;
            }
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
            if ("dev.resteasy.grpc.arrays".equals(clazz.getPackageName())
                    && !dev_resteasy_grpc_arrays___ArrayHolder.class.equals(clazz)) {
                continue;
            }
            sb.append("      toJavabufMap.put(")
                    .append(originalClassName(simpleName))
                    .append(".class, new ")
                    .append(simpleName)
                    .append("_ToJavabuf());" + LS);
            sb.append("      fromJavabufMap.put(")
                    .append("\"" + simpleName + "\"")
                    .append(", new ")
                    .append(simpleName)
                    .append("_FromJavabuf());" + LS);
            sb.append("      toJavabufClassMap.put(")
                    .append(originalClassName(simpleName) + ".class, ")
                    .append(simpleName + ".class);" + LS);
            sb.append("      fromJavabufClassMap.put(")
                    .append(simpleName + ".class, ")
                    .append(originalClassName(simpleName) + ".class);");
        }
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

    private static void publicMethods(StringBuilder sb) {
        sb.append("   public boolean handlesToJavabuf(Class<?> clazz) {" + LS)
                .append("      return clazz.isPrimitive() || toJavabufMap.containsKey(clazz);" + LS)
                .append("   }" + LS + LS)
                .append("   public boolean handlesFromJavabuf(Class<?> clazz) {" + LS)
                .append("      return clazz.isPrimitive() || toJavabufMap.containsKey(clazz);" + LS)
                .append("   }" + LS + LS)
                .append("   public Message translateToJavabuf(Object o) {" + LS)
                .append("      TranslateToJavabuf ttj = toJavabufMap.get(o.getClass());" + LS)
                .append("      if (ttj == null) {" + LS)
                .append("         throw new RuntimeException(o.getClass() + \" is not recognized\");" + LS)
                .append("      }" + LS)
                .append("      ttj.clear();" + LS)
                .append("      return ttj.assignToJavabuf(o);" + LS)
                .append("   }" + LS + LS)
                .append("   @SuppressWarnings(\"rawtypes\")" + LS)
                .append("   public Class translateToJavabufClass(Class<?> clazz) {" + LS)
                .append("      return toJavabufClassMap.get(clazz);" + LS)
                .append("   }" + LS + LS)
                .append("   @SuppressWarnings(\"rawtypes\")" + LS)
                .append("   public Class translatefromJavabufClass(Class<?> clazz) {" + LS)
                .append("      return fromJavabufClassMap.get(clazz);" + LS)
                .append("   }" + LS + LS)
                .append("   public Object translateFromJavabuf(Message message) {" + LS)
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
        sb.append(
                "   private static Map<Class<?>, TranslateToJavabuf> toJavabufMap = new HashMap<Class<?>, TranslateToJavabuf>();"
                        + LS);
        sb.append(
                "   private static Map<String, TranslateFromJavabuf> fromJavabufMap = new HashMap<String, TranslateFromJavabuf>();"
                        + LS);
        sb.append("   private static JavabufTranslator translator = new ").append(args[1]).append("JavabufTranslator();" + LS);
        sb.append(
                "   private static dev_resteasy_grpc_arrays___ArrayHolder_ToJavabuf arrayHolderToJavabuf = new dev_resteasy_grpc_arrays___ArrayHolder_ToJavabuf();"
                        + LS);
        sb.append("   private static Set<Class<?>> WRAPPER_TYPES = new HashSet<Class<?>>();" + LS + LS);
        sb.append("   @SuppressWarnings(\"rawtypes\")" + LS);
        sb.append("   private static Map<Class<?>, Class> toJavabufClassMap = new HashMap<Class<?>, Class>();" + LS);
        sb.append("   private static Map<Class<?>, Class> fromJavabufClassMap = new HashMap<Class<?>, Class>();" + LS);
    }

    private static void privateMethods(StringBuilder sb, String[] args) {
        sb.append("   private static AssignToJavabuf toJavabuf(Class<?> javaClass, FieldDescriptor fd) {" + LS)
                .append("      try {" + LS)
                .append("         AssignToJavabuf assignToJavabuf = (obj, messageBuilder) -> {" + LS)
                .append("            try {" + LS)
                .append("               if (isSuperClass(fd.getName())) {" + LS)
                .append("                  Message message = toJavabufMap.get(obj.getClass().getSuperclass()).assignToJavabuf(obj);"
                        + LS)
                .append("                  if (message != null) {" + LS)
                .append("                     messageBuilder.setField(fd, message);" + LS)
                .append("                  }" + LS)
                .append("               } else {" + LS)
                .append("                  final Field field = javaClass.getDeclaredField(javabufClassToJavaClass(fd.getName()));"
                        + LS)
                .append("                  field.setAccessible(true);" + LS)
                .append("                  if (!String.class.equals(field.getType()) && toJavabufMap.keySet().contains(field.getType())) {"
                        + LS)
                .append("                     Message message = toJavabufMap.get(field.getType()).assignToJavabuf(field.get(obj));"
                        + LS)
                .append("                     if (message != null) {" + LS)
                .append("                        messageBuilder.setField(fd, message);" + LS)
                .append("                     }" + LS)
                .append("                  } else if (field.getType().isArray()) {" + LS)
                .append("                        Object array = field.get(obj);" + LS)
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
                .append("                        } else {" + LS)
                .append("                           for (int i = 0; i < Array.getLength(array); i++) {" + LS)
                .append("                              messageBuilder.addRepeatedField(fd, translator.translateToJavabuf(Array.get(array, i)));"
                        + LS)
                .append("                           }" + LS)
                .append("                        }" + LS)
                .append("                  } else {" + LS)
                .append("                     if (field.get(obj) != null) {" + LS)
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
                .append("                  }" + LS)
                .append("               }" + LS)
                .append("            } catch (Exception e) {" + LS)
                .append("                throw new RuntimeException(e);" + LS)
                .append("            }" + LS)
                .append("         };" + LS)
                .append("         return assignToJavabuf;" + LS)
                .append("      } catch (Exception e) {" + LS)
                .append("         throw new RuntimeException(e);" + LS)
                .append("      }" + LS)
                .append("   }" + LS + LS);
        sb.append("   private static AssignFromJavabuf fromJavabuf(Class<?> javaClass, FieldDescriptor fd) {" + LS)
                .append("      try {" + LS)
                .append("         AssignFromJavabuf assignFromJavabuf = (message, object) -> {" + LS)
                .append("            try {" + LS)
                .append("               if (isSuperClass(fd.getName())) {" + LS)
                .append("                  String superClassName = javaClassToJavabufClass(javaClass.getSuperclass().getName());"
                        + LS)
                .append("                  TranslateFromJavabuf t = fromJavabufMap.get(superClassName);" + LS)
                .append("                  FieldDescriptor sfd = getSuperField(message);" + LS)
                .append("                  Message superMessage = (Message) message.getField(sfd);" + LS)
                .append("                  t.assignExistingFromJavabuf(superMessage, object);" + LS)
                .append("               } else {" + LS)
                .append("                  final Field field = javaClass.getDeclaredField(javabufClassToJavaClass(fd.getName()));"
                        + LS)
                .append("                  field.setAccessible(true);" + LS)
                .append("                  String typeName = fd.toProto().getTypeName();" + LS)
                .append("                  typeName = typeName.substring(typeName.lastIndexOf(\".\") + 1);" + LS)
                .append("                  if (field.getType().isArray()) {" + LS)
                .append("                     if (\"dev_resteasy_grpc_arrays___ArrayHolder\".equals(typeName)) {" + LS)
                .append("                        dev_resteasy_grpc_arrays___ArrayHolder ah = (dev_resteasy_grpc_arrays___ArrayHolder) message.getField(fd);"
                        + LS)
                .append("                        field.set(object, ArrayUtility.getArray(translator, ah));" + LS)
                .append("                     } else if (Descriptors.FieldDescriptor.JavaType.STRING.equals(fd.getJavaType())) {"
                        + LS)
                .append("                        if (char.class.equals(field.getType().getComponentType())) {" + LS)
                .append("                           char[] cs = stringToChars((String) message.getField(fd));" + LS)
                .append("                           field.set(object, cs);" + LS)
                .append("                        } else {" + LS)
                .append("                            Character[] cs = stringToCharacters((String) message.getField(fd));" + LS)
                .append("                            field.set(object, cs);" + LS)
                .append("                        }" + LS)
                .append("                     } else if (byte.class.equals(field.getType().getComponentType())) {"
                        + LS)
                .append("                        ByteString bs = (ByteString) message.getField(fd);" + LS)
                .append("                        field.set(object, bs.toByteArray());" + LS)
                .append("                      } else if (Byte.class.equals(field.getType().getComponentType())) {" + LS)
                .append("                         IntList il = (IntList) message.getField(fd);" + LS)
                .append("                         Byte[] bs = new Byte[il.size()];" + LS)
                .append("                         for (int i = 0; i < il.size(); i++) {" + LS)
                .append("                            bs[i] = il.get(i).byteValue();" + LS)
                .append("                         }" + LS)
                .append("                         field.set(object, bs);" + LS)
                .append("                     } else if (message.getField(fd) instanceof List) {" + LS)
                .append("                        List list = (List) message.getField(fd);" + LS)
                .append("                        if (list.size() == 0) {" + LS)
                .append("                           return;" + LS)
                .append("                        }" + LS)
                .append("                        if (message.getField(fd).getClass().getPackage().getName().startsWith(\"com.google.protobuf\")) {"
                        + LS)
                .append("                           if (field.get(object) == null) {" + LS)
                .append("                              field.set(object, getArray(field, list.size()));"
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
                .append("                           if (field.get(object) == null) {" + LS)
                .append("                              field.set(object, Array.newInstance(field.getType().getComponentType(), list.size()));"
                        + LS)
                .append("                            }" + LS)
                .append("                           for (int i = 0; i < list.size(); i++) {" + LS)
                .append("                              Array.set(field.get(object), i, translator.translateFromJavabuf((Message) list.get(i)));"
                        + LS)
                .append("                           }" + LS)
                .append("                        }" + LS)
                .append("                     }" + LS)
                .append("                  } else if (Descriptors.FieldDescriptor.Type.MESSAGE.equals(fd.getType())" + LS)
                .append("                     && fromJavabufMap.keySet().contains(fd.getMessageType().getName())) {" + LS)
                .append("                     Message submessage = (Message) message.getField(fd);" + LS)
                .append("                     Object obj = fromJavabufMap.get(fd.getMessageType().getName()).assignFromJavabuf(submessage);"
                        + LS)
                .append("                     ArrayUtility.assignArray(field, object, obj);" + LS)
                .append("                  } else if (JavaType.MESSAGE.equals(fd.getJavaType()) && Array_proto.dev_resteasy_grpc_arrays___ArrayHolder.class.getSimpleName().equals(fd.getMessageType().getName())) {"
                        + LS)
                .append("                     dev_resteasy_grpc_arrays___ArrayHolder submessage = (dev_resteasy_grpc_arrays___ArrayHolder) message.getField(fd);"
                        + LS)
                .append("                     Object o = ArrayUtility.getArray(translator, submessage);" + LS)
                .append("                  } else {" + LS)
                .append("                     Object ooo = message.getField(fd);" + LS)
                .append("                     if (Integer.class.equals(ooo.getClass()) && (Byte.class.equals(field.getType()) || byte.class.equals(field.getType()))) {"
                        + LS)
                .append("                        field.set(object, ((Integer) ooo).byteValue());" + LS)
                .append("                     } else if (Integer.class.equals(ooo.getClass()) && (Short.class.equals(field.getType()) || short.class.equals(field.getType()))) {"
                        + LS)
                .append("                        field.set(object, ((Integer) ooo).shortValue());" + LS)
                .append("                     } else if (Integer.class.equals(ooo.getClass()) && (Character.class.equals(field.getType()) || char.class.equals(field.getType()))) {"
                        + LS)
                .append("                        int i = ((Integer)ooo).intValue();" + LS)
                .append("                        field.set(object, Character.toChars(i)[0]);" + LS)
                .append("                     } else if (ooo instanceof ByteString) {" + LS)
                .append("                        field.set(object, ((ByteString) ooo).newInput().readAllBytes());" + LS)
                .append("                     } else {" + LS)
                .append("                        field.set(object, ooo);" + LS)
                .append("                     }" + LS)
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
    }

    private static void createTranslatorToJavabuf(String[] args, Class<?> clazz, StringBuilder sb) throws Exception {
        if ("gEmpty".equals(clazz.getSimpleName())
                || "gCookie".equals(clazz.getSimpleName())
                || "gHeader".equals(clazz.getSimpleName())
                || "ServletInfo".equals(clazz.getSimpleName())
                || "gNewCookie".equals(clazz.getSimpleName())
                || "FormMap".equals(clazz.getSimpleName())
                || "FormValues".equals(clazz.getSimpleName())) {
            return;
        }
        if ("dev.resteasy.grpc.arrays.Array_proto$dev_resteasy_grpc_arrays___ArrayHolder".equals(clazz.getName())) {
            createArrayHolderTranslatorToJavabuf(args, sb);
            return;
        }
        if ("dev.resteasy.grpc.arrays".equals(clazz.getPackage().getName())) {
            return;
        }
        sb.append("   static class ")
                .append(fqnify(clazz.getSimpleName())).append("_ToJavabuf implements TranslateToJavabuf {" + LS);
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
        } else {
            sb.append("      private static Descriptor descriptor = ").append(clazz.getCanonicalName())
                    .append(".getDescriptor();" + LS)
                    .append("      private static ").append(fqnify(clazz.getSimpleName())).append(".Builder builder = ")
                    .append(fqnify(clazz.getSimpleName())).append(".newBuilder();" + LS)
                    .append("      private static List<AssignToJavabuf> assignList = new ArrayList<AssignToJavabuf>();" + LS
                            + LS)
                    .append("      static {" + LS)
                    .append("         for (FieldDescriptor f : descriptor.getFields()) {" + LS)
                    .append("            String name = f.getName();" + LS)
                    .append("            if (descriptor.findFieldByName(name) == null) {" + LS)
                    .append("               continue;" + LS)
                    .append("            }" + LS)
                    .append("            assignList.add(toJavabuf(").append(originalSimpleName(clazz.getSimpleName()))
                    .append(".class, descriptor.findFieldByName(name)));" + LS)
                    .append("         }" + LS)
                    .append("      }" + LS + LS)
                    .append("      public Message assignToJavabuf(Object c1) {" + LS)
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
                    .append("         builder.clear();" + LS)
                    .append("      }" + LS);
        }
        sb.append("   }" + LS + LS);
    }

    private static void createArrayHolderTranslatorToJavabuf(String[] args, StringBuilder sb) {
        sb.append("//////// ArrayHolder to Javabuf  ////////" + LS + LS);
        sb.append("   static class dev_resteasy_grpc_arrays___ArrayHolder_ToJavabuf implements TranslateToJavabuf {" + LS + LS)
                .append("      public Message assignToJavabuf(Object obj) {" + LS)
                .append("         return ArrayUtility.getHolder(translator, obj);" + LS)
                .append("      }" + LS + LS)
                .append("      public void clear() {" + LS)
                .append("         //" + LS)
                .append("      }" + LS)
                .append("   }" + LS);
    }

    private static void createTranslatorFromJavabuf(String[] args, Class<?> clazz, StringBuilder sb)
            throws ClassNotFoundException {
        if (clazz.isInterface()) {
            return;
        }
        if ("dev.resteasy.grpc.arrays.Array_proto$dev_resteasy_grpc_arrays___ArrayHolder".equals(clazz.getName())) {
            createArrayHolderTranslatorFromJavabuf(args, sb);
            return;
        }
        if ("dev.resteasy.grpc.arrays".equals(clazz.getPackage().getName())) {
            return;
        }
        String originalName = originalSimpleName(clazz.getName());
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
        sb.append("   static class ")
                .append(fqnify(clazz.getSimpleName())).append("_FromJavabuf implements TranslateFromJavabuf {" + LS)
                .append("      private static Descriptor descriptor = ").append(clazz.getCanonicalName())
                .append(".getDescriptor();" + LS);
        if (PRIMITIVE_WRAPPER_TYPES.containsKey(originalName)) {
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
            sb.append("      private static List<AssignFromJavabuf> assignList = new ArrayList<AssignFromJavabuf>();" + LS + LS)
                    .append("      static {" + LS)
                    .append("         for (FieldDescriptor f : descriptor.getFields()) {" + LS)
                    .append("            String name = f.getName();" + LS)
                    .append("            if (descriptor.findFieldByName(name) == null) {" + LS)
                    .append("               continue;" + LS)
                    .append("            }" + LS)
                    .append("            assignList.add(fromJavabuf(").append(originalName)
                    .append(".class, descriptor.findFieldByName(name)));" + LS)
                    .append("         }" + LS)
                    .append("      }" + LS + LS)
                    .append("      public ").append(originalName).append(" assignFromJavabuf(Message message) {" + LS);
            findConstructor(clazz, originalName, sb);
            sb.append("         for (AssignFromJavabuf assignFrom : assignList) {" + LS)
                    .append("            try {" + LS)
                    .append("               assignFrom.assign(message, obj);" + LS)
                    .append("            } catch (Exception e) {" + LS)
                    .append("               throw new RuntimeException(e);" + LS)
                    .append("            }" + LS)
                    .append("         }" + LS)
                    .append("         return obj;" + LS)
                    .append("      }" + LS + LS)
                    .append("      public void assignExistingFromJavabuf(Message message, Object obj) {" + LS)
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

    private static void createArrayHolderTranslatorFromJavabuf(String[] args, StringBuilder sb) {
        String prefix = args[1].substring(args[1].lastIndexOf('.') + 1);
        sb.append("//////// ArrayHolder from Javabuf" + LS + LS)
                .append("   static class dev_resteasy_grpc_arrays___ArrayHolder_FromJavabuf implements TranslateFromJavabuf {"
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

    private static String originalClassName(String s) {
        if (PRIMITIVE_WRAPPER_TYPES.containsKey(s)) {
            return s.substring(1);
        }
        int i = s.indexOf("$");
        if (i >= 0) {
            s = s.substring(i + 1);
        }
        int j = s.lastIndexOf("___");
        if (j < 0) {
            j = s.indexOf("_INNER_");
        }
        if (j >= 0) {
            String pkg = s.substring(0, j).replace('_', '.');
            return pkg + "." + originalSimpleName(s);
        }
        return s;
    }

    private static void findConstructor(Class<?> clazz, String originalName, StringBuilder sb) throws ClassNotFoundException {
        String className = javabufToJava(clazz.getName(), originalName);
        Class<?> originalClazz = Class.forName(className);
        Constructor<?>[] cons = originalClazz.getConstructors();
        Constructor<?> con = cons[0];
        if (cons.length > 1) {
            for (int i = 1; i < cons.length; i++) {
                if (cons[i].getParameterCount() < con.getParameterCount()) {
                    con = cons[i];
                }
            }
        }
        sb.append("         ").append(originalName).append(" obj = new ").append(originalName).append("(");
        boolean first = true;
        for (int i = 0; i < con.getParameterCount(); i++) {
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
        sb.append(");" + LS);
    }

    private static String javabufToJava(String javabufName, String simpleName) {
        String tmp = javabufName;
        int n = tmp.lastIndexOf("$");
        if (n >= 0) {
            tmp = tmp.substring(n + 1);
        }
        n = tmp.lastIndexOf("___");
        if (n >= 0) {
            tmp = tmp.substring(0, n);
        } else {
            n = tmp.indexOf("_INNER_");
            if (n >= 0) {
                tmp = tmp.substring(0, n);
                n = simpleName.indexOf("_INNER_");
                if (n >= 0) {
                    tmp += "$" + simpleName.substring(n + "_INNER_".length());
                }
                tmp = tmp.replace("_", ".");
                return tmp;
            }
        }
        tmp = tmp.replace("_", ".");
        return tmp + "." + simpleName;
    }
}
