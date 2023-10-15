package dev.resteasy.grpc.arrays;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.protobuf.Any;

import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___ArrayHolder;
import dev.resteasy.grpc.bridge.runtime.Utility;
import dev.resteasy.grpc.bridge.runtime.protobuf.JavabufTranslator;

public class ArrayUtility {

    private static Map<String, String> PRIMITIVE_WRAPPER_TYPES = new HashMap<String, String>();
    private static Map<String, String> PRIMITIVE_WRAPPER_TYPES_INVERSE = new HashMap<String, String>();
    private static Map<String, String> PRIMITIVE_TYPES = new HashMap<String, String>();
    private static Map<String, Class<?>> PRIMITIVE_CLASSES = new HashMap<String, Class<?>>();

    static {
        //        try {
        //        PRIMITIVE_CLASSES.put("boolean", Class.forName("[Z]"));
        //        } catch (Exception e) {throw new RuntimeException(e);}
        PRIMITIVE_WRAPPER_TYPES.put("boolean", "java.lang.Boolean");
        PRIMITIVE_WRAPPER_TYPES.put("byte", "java.lang.Byte");
        PRIMITIVE_WRAPPER_TYPES.put("short", "java.lang.Short");
        PRIMITIVE_WRAPPER_TYPES.put("int", "java.lang.Integer");
        PRIMITIVE_WRAPPER_TYPES.put("long", "java.lang.Long");
        PRIMITIVE_WRAPPER_TYPES.put("float", "java.lang.Float");
        PRIMITIVE_WRAPPER_TYPES.put("double", "java.lang.Double");
        PRIMITIVE_WRAPPER_TYPES.put("char", "java.lang.Character");

        PRIMITIVE_WRAPPER_TYPES_INVERSE.put("java.lang.Boolean", "boolean");
        PRIMITIVE_WRAPPER_TYPES_INVERSE.put("java.lang.Byte", "byte");
        PRIMITIVE_WRAPPER_TYPES_INVERSE.put("java.lang.Short", "short");
        PRIMITIVE_WRAPPER_TYPES_INVERSE.put("java.lang.Integer", "int");
        PRIMITIVE_WRAPPER_TYPES_INVERSE.put("java.lang.Long", "long");
        PRIMITIVE_WRAPPER_TYPES_INVERSE.put("java.lang.Float", "float");
        PRIMITIVE_WRAPPER_TYPES_INVERSE.put("java.lang.Double", "double");
        PRIMITIVE_WRAPPER_TYPES_INVERSE.put("java.lang.Character", "char");

        PRIMITIVE_TYPES.put("Z", "Ljava.lang.Boolean;");
        PRIMITIVE_TYPES.put("B", "Ljava.lang.Byte;");
        PRIMITIVE_TYPES.put("S", "Ljava.lang.Short;");
        PRIMITIVE_TYPES.put("I", "Ljava.lang.Integer;");
        PRIMITIVE_TYPES.put("J", "Ljava.lang.Long;");
        PRIMITIVE_TYPES.put("F", "Ljava.lang.Float;");
        PRIMITIVE_TYPES.put("D", "Ljava.lang.Double;");
        PRIMITIVE_TYPES.put("C", "Ljava.lang.Character;");
    }

    public static Object getArray(JavabufTranslator translator, Array_proto.dev_resteasy_grpc_arrays___ArrayHolder ah,
            //            Class<?> targetClass, String outerClassName)
            String outerClassName)
            throws Exception {
        //        Class javaComponentClass = Class.forName(ah.getComponentClass());
        //        //        Class javaComponentClass = targetClass;
        //        Object array = Array.newInstance(javaComponentClass, ah.getGoogleProtobufAnyFieldList().size());
        Object array = createArray(ah.getComponentClass(), ah.getGoogleProtobufAnyFieldList().size());
        System.out.println(array.getClass());
        if (ah.getGoogleProtobufAnyFieldList().size() == 0) {
            return array;
        }
        //        String protobufComponentClass = Utility.extractTypeFromAny(list.get(0), null, null)
        List<Any> list = ah.getGoogleProtobufAnyFieldList();
        if (ah.getBottom()) {
            Class protobufComponentType = Utility.extractTypeFromAny(list.get(0),
                    Thread.currentThread().getContextClassLoader(), outerClassName);
            for (int i = 0; i < list.size(); i++) {
                //            	Object o = translator.translateFromJavabuf(list.get(i).unpack(protobufComponentType));
                //          		System.out.println(o.getClass());
                //        		System.out.println(array.getClass());
                //                Object o = translator.translateFromJavabuf(list.get(i).unpack(protobufComponentType));
                //                Array.set(array, i, o);
                Array.set(array, i, translator.translateFromJavabuf(list.get(i).unpack(protobufComponentType)));
            }
        } else {
            //        	Class clazz = translator.toJavabufClass(dev_resteasy_grpc_arrays___ArrayHolder.class);
            Class protobufComponentType = Utility.extractTypeFromAny(list.get(0),
                    Thread.currentThread().getContextClassLoader(), "Array_proto");
            for (int i = 0; i < list.size(); i++) {
                //                Array.set(array, i, Integer.valueOf(3));
                //                Object o = translator.translateFromJavabuf(list.get(i).unpack(protobufComponentType));
                //                Array.set(array, i, o);
                Array.set(array, i,
                        translator.translateFromJavabuf(list.get(i).unpack(protobufComponentType)));
            }
        }
        return array;
    }

    public static dev_resteasy_grpc_arrays___ArrayHolder getHolder(JavabufTranslator translator, Object o) {
        dev_resteasy_grpc_arrays___ArrayHolder.Builder builder = dev_resteasy_grpc_arrays___ArrayHolder.newBuilder();
        //        builder.setComponentClass(wrapPrimitiveTypes(o.getClass().getComponentType().getName()));
        builder.setComponentClass(o.getClass().getComponentType().getName());
        //        builder.setComponentClass(o.getClass().getName());
        System.out.println(o.getClass());
        System.out.println(o.getClass().getComponentType());
        if (o.getClass().getComponentType().isArray()) {
            builder.setBottom(false);
            for (int i = 0; i < Array.getLength(o); i++) {
                dev_resteasy_grpc_arrays___ArrayHolder ah = getHolder(translator, Array.get(o, i));
                Any any = Any.pack(ah);
                builder.addGoogleProtobufAnyField(any);
            }
        } else {
            System.out.println(o.getClass().getComponentType().isPrimitive());
            builder.setBottom(true);
            for (int i = 0; i < Array.getLength(o); i++) {
                builder.addGoogleProtobufAnyField(Any.pack(translator.translateToJavabuf(Array.get(o, i))));
            }
        }
        return builder.build();
    }

    public static void assignArray(Object to, Object from) {
        if (Array.getLength(from) != Array.getLength(to)) {
            throw new RuntimeException("array lengths differ");
        }
        if (to.getClass().equals(from.getClass()) || componentTypeWraps(to, from)) {
            for (int i = 0; i < Array.getLength(from); i++) {
                Array.set(to, i, Array.get(from, i));
            }
        } else {
            for (int i = 0; i < Array.getLength(to); i++) {
                assignArray(Array.get(to, i), Array.get(from, i));
            }
        }
    }

    private static boolean componentTypeWraps(Object from, Object to) {
        String fromComponentType = from.getClass().getComponentType().toString();
        String toComponentType = to.getClass().getComponentType().toString();
        if (fromComponentType == null || toComponentType == null) {
            return false;
        }
        return PRIMITIVE_WRAPPER_TYPES.get(fromComponentType).equals(toComponentType) ||
                PRIMITIVE_WRAPPER_TYPES.get(toComponentType).equals(fromComponentType);
    }

    public static void assignArray(Field field, Object target, Object obj)
            throws IllegalArgumentException, IllegalAccessException {
        if (field.getType().equals(obj.getClass())) {
            field.set(target, obj);
        } else {
            for (int i = 0; i < Array.getLength(obj); i++) {
                Array.set(target, i, obj);
            }
        }
    }

    //    [Ljava.lang.Integer
    private static String wrapPrimitiveTypes(String type) {
        if (PRIMITIVE_WRAPPER_TYPES.containsKey(type)) {
            return PRIMITIVE_WRAPPER_TYPES.get(type);
        }
        int i = type.lastIndexOf('[');
        String prim = type.substring(i + 1);
        if (PRIMITIVE_TYPES.containsKey(prim)) {
            return type.substring(0, i + 1) + PRIMITIVE_TYPES.get(prim);
        }
        return type;
    }

    /*
     * PRIMITIVE_WRAPPER_TYPES.put("boolean", "java.lang.Boolean");
     * PRIMITIVE_WRAPPER_TYPES.put("byte", "java.lang.Byte");
     * PRIMITIVE_WRAPPER_TYPES.put("short", "java.lang.Short");
     * PRIMITIVE_WRAPPER_TYPES.put("int", "java.lang.Integer");
     * PRIMITIVE_WRAPPER_TYPES.put("long", "java.lang.Long");
     * PRIMITIVE_WRAPPER_TYPES.put("float", "java.lang.Float");
     * PRIMITIVE_WRAPPER_TYPES.put("double", "java.lang.Double");
     * PRIMITIVE_WRAPPER_TYPES.put("char", "java.lang.Character");
     */
    private static Object createArray(String componentType, int length) throws Exception {
        if (PRIMITIVE_WRAPPER_TYPES.containsKey(componentType)) {
            switch (componentType) {
                case "boolean":
                    return new boolean[length];

                case "byte":
                    return new byte[length];

                case "short":
                    return new short[length];

                case "int":
                    return new int[length];

                case "long":
                    return new long[length];

                case "float":
                    return new float[length];

                case "double":
                    return new double[length];

                case "char":
                    return new char[length];

                //                default:
                //                    throw new RuntimeException();
            }
        }
        return Array.newInstance(Class.forName(componentType), length);
    }
}
