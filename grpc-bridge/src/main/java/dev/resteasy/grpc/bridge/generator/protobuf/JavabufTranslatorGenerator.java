package dev.resteasy.grpc.bridge.generator.protobuf;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.jboss.logging.Logger;

import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;

import dev.resteasy.grpc.bridge.runtime.protobuf.AssignFromJavabuf;
import dev.resteasy.grpc.bridge.runtime.protobuf.AssignToJavabuf;
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
        //      PRIMITIVE_WRAPPER_TYPES.put("gEmpty",     void.class);

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
            Class<?> wrapperClass = Class.forName(args[1] + "_proto", true, Thread.currentThread()
                    .getContextClassLoader());
            StringBuilder sb = new StringBuilder();
            classHeader(args, translatorClass, wrapperClass, sb);
            classBody(wrapperClass, sb);
            finishClass(sb);
            writeTranslatorClass(args, translatorClass, sb);
        } catch (Exception e) {
            logger.error(e);
        }
    }

    private static void classHeader(String[] args, String translatorClass, Class<?> wrapperClass, StringBuilder sb) {
        sb.append("package ").append(wrapperClass.getPackage().getName()).append(";" + LS + LS);
        imports(wrapperClass, sb);
        sb.append("public class ")
                .append(translatorClass)
                .append(" {" + LS);
    }

    private static void imports(Class<?> wrapperClass, StringBuilder sb) {
        sb.append("import java.lang.reflect.Field;" + LS)
                .append("import java.util.ArrayList;" + LS)
                .append("import java.util.HashMap;" + LS)
                .append("import java.util.List;" + LS)
                .append("import java.util.Map;" + LS)
                .append("import com.google.protobuf.Descriptors;" + LS)
                .append("import com.google.protobuf.Descriptors.Descriptor;" + LS)
                .append("import com.google.protobuf.Descriptors.FieldDescriptor;" + LS)
                .append("import com.google.protobuf.DynamicMessage;" + LS)
                .append("import com.google.protobuf.Message;" + LS)
                .append("import ").append(AssignFromJavabuf.class.getCanonicalName()).append(";" + LS)
                .append("import ").append(AssignToJavabuf.class.getCanonicalName()).append(";" + LS)
                .append("import ").append(TranslateFromJavabuf.class.getCanonicalName()).append(";" + LS)
                .append("import ").append(TranslateToJavabuf.class.getCanonicalName()).append(";" + LS);
        Class<?>[] classes = wrapperClass.getClasses();
        for (Class<?> clazz : classes) {
            if (clazz.isInterface()) {
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

    private static void classBody(Class<?> wrapperClass, StringBuilder sb) throws Exception {
        Class<?>[] classes = wrapperClass.getClasses();
        privateVariables(sb);
        staticInit(classes, sb);
        publicMethods(sb);
        privateMethods(sb);
        for (Class<?> clazz : classes) {
            if (clazz.isInterface()) {
                continue;
            }
            String simpleName = clazz.getSimpleName();
            if ("GeneralEntityMessage".equals(simpleName) || "GeneralReturnMessage".equals(simpleName)) {
                continue;
            }
            createTranslator(clazz, sb);
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
        }
        sb.append("   }" + LS + LS);
    }

    private static void publicMethods(StringBuilder sb) {
        sb.append("   public static boolean handlesToJavabuf(Class<?> clazz) {" + LS)
                .append("      return clazz.isPrimitive() || toJavabufMap.containsKey(clazz);" + LS)
                .append("   }" + LS + LS)
                .append("   public static boolean handlesFromJavabuf(Class<?> clazz) {" + LS)
                .append("      return clazz.isPrimitive() || toJavabufMap.containsKey(clazz);" + LS)
                .append("   }" + LS + LS)
                .append("   public static Message translateToJavabuf(Object o) {" + LS)
                .append("      TranslateToJavabuf ttj = toJavabufMap.get(o.getClass());" + LS)
                .append("      if (ttj == null) {" + LS)
                .append("         throw new RuntimeException(o.getClass() + \" is not recognized\");" + LS)
                .append("      }" + LS)
                .append("      ttj.clear();" + LS)
                .append("      return ttj.assignToJavabuf(o);" + LS)
                .append("   }" + LS + LS)
                .append("   @SuppressWarnings(\"rawtypes\")" + LS)
                .append("   public static Class translateToJavabufClass(Class<?> clazz) {" + LS)
                .append("      return toJavabufClassMap.get(clazz);" + LS)
                .append("   }" + LS + LS)
                .append("   public static Object translateFromJavabuf(Message message) {" + LS)
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

    private static void createTranslator(Class<?> clazz, StringBuilder sb) throws Exception {
        createTranslatorToJavabuf(clazz, sb);
        createTranslatorFromJavabuf(clazz, sb);
    }

    private static void privateVariables(StringBuilder sb) {
        sb.append(
                "   private static Map<Class<?>, TranslateToJavabuf> toJavabufMap = new HashMap<Class<?>, TranslateToJavabuf>();"
                        + LS);
        sb.append(
                "   private static Map<String, TranslateFromJavabuf> fromJavabufMap = new HashMap<String, TranslateFromJavabuf>();"
                        + LS + LS);
        sb.append("   @SuppressWarnings(\"rawtypes\")" + LS);
        sb.append("   private static Map<Class<?>, Class> toJavabufClassMap = new HashMap<Class<?>, Class>();" + LS);
    }

    private static void privateMethods(StringBuilder sb) {
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
                .append("                  final Field field = javaClass.getDeclaredField(fd.getName());" + LS)
                .append("                  field.setAccessible(true);" + LS)
                .append("                  if (!String.class.equals(field.getType()) && toJavabufMap.keySet().contains(field.getType())) {"
                        + LS)
                .append("                     Message message = toJavabufMap.get(field.getType()).assignToJavabuf(field.get(obj));"
                        + LS)
                .append("                     if (message != null) {" + LS)
                .append("                        messageBuilder.setField(fd, message);" + LS)
                .append("                     }" + LS)
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
                .append("                           messageBuilder.setField(fd, (int) c);" + LS)
                .append("                        } else {" + LS)
                .append("                           messageBuilder.setField(fd, field.get(obj));" + LS)
                .append("                        }" + LS)
                .append("                     }" + LS)
                .append("                  }" + LS)
                .append("               }" + LS)
                .append("            } catch (Exception e) {" + LS)
                .append("               //" + LS)
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
                .append("                  final Field field = javaClass.getDeclaredField(fd.getName());" + LS)
                .append("                  field.setAccessible(true);" + LS)
                .append("                  if (Descriptors.FieldDescriptor.Type.MESSAGE.equals(fd.getType())" + LS)
                .append("                      && fromJavabufMap.keySet().contains(fd.getMessageType().getName())) {" + LS)
                .append("                     Message submessage = (Message) message.getField(fd);" + LS)
                .append("                     Object obj = fromJavabufMap.get(fd.getMessageType().getName()).assignFromJavabuf(submessage);"
                        + LS)
                .append("                     field.set(object, obj);" + LS)
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
    }

    private static void createTranslatorToJavabuf(Class<?> clazz, StringBuilder sb) throws Exception {
        if ("gEmpty".equals(clazz.getSimpleName())
                || "gCookie".equals(clazz.getSimpleName())
                || "gHeader".equals(clazz.getSimpleName())
                || "ServletInfo".equals(clazz.getSimpleName())
                || "gNewCookie".equals(clazz.getSimpleName())
                || "FormMap".equals(clazz.getSimpleName())
                || "FormValues".equals(clazz.getSimpleName())) {
            return;
        }
        sb.append("   static class ")
                .append(fqnify(clazz.getSimpleName())).append("_ToJavabuf implements TranslateToJavabuf {" + LS);
        if (PRIMITIVE_WRAPPER_TYPES.containsKey(clazz.getSimpleName())) {
            String simpleJavabufName = clazz.getSimpleName();
            String simpleJavaName = simpleJavabufName.substring(1);
            sb.append("" + LS)
                    .append("      public Message assignToJavabuf(Object x) {" + LS)
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
                    .append("      private static DynamicMessage.Builder builder = DynamicMessage.newBuilder(descriptor);" + LS)
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

    private static void createTranslatorFromJavabuf(Class<?> clazz, StringBuilder sb) throws ClassNotFoundException {
        if (clazz.isInterface()) {
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
