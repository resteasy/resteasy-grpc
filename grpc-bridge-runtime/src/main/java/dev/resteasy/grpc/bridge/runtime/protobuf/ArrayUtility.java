package dev.resteasy.grpc.bridge.runtime.protobuf;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.protobuf.Any;

import dev.resteasy.grpc.arrays.Array_proto;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___ArrayHolder;
import dev.resteasy.grpc.bridge.runtime.Utility;

public class ArrayUtility {

    private static Map<String, String> PRIMITIVE_WRAPPER_TYPES = new HashMap<String, String>();
    private static Map<String, String> PRIMITIVE_TYPES = new HashMap<String, String>();

    static {
        PRIMITIVE_WRAPPER_TYPES.put("boolean", "java.lang.Boolean");
        PRIMITIVE_WRAPPER_TYPES.put("byte", "java.lang.Byte");
        PRIMITIVE_WRAPPER_TYPES.put("short", "java.lang.Short");
        PRIMITIVE_WRAPPER_TYPES.put("int", "java.lang.Integer");
        PRIMITIVE_WRAPPER_TYPES.put("long", "java.lang.Long");
        PRIMITIVE_WRAPPER_TYPES.put("float", "java.lang.Float");
        PRIMITIVE_WRAPPER_TYPES.put("double", "java.lang.Double");
        PRIMITIVE_WRAPPER_TYPES.put("char", "java.lang.Character");

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
            String outerClassName)
            throws Exception {
        Class javaComponentClass = Class.forName(ah.getComponentClass());
        Object array = Array.newInstance(javaComponentClass, ah.getGoogleProtobufAnyFieldList().size());
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
                Array.set(array, i, translator.translateFromJavabuf(list.get(i).unpack(protobufComponentType)));
            }
        } else {
            //        	Class clazz = translator.toJavabufClass(dev_resteasy_grpc_arrays___ArrayHolder.class);
            Class protobufComponentType = Utility.extractTypeFromAny(list.get(0),
                    Thread.currentThread().getContextClassLoader(), "Array_proto");
            for (int i = 0; i < list.size(); i++) {
                //        		Object o = translator.translateFromJavabuf(list.get(i).unpack(protobufComponentType));
                //        		System.out.println(o.getClass());
                //        		System.out.println(array.getClass());
                Array.set(array, i,
                        translator.translateFromJavabuf(list.get(i).unpack(protobufComponentType)));
            }
        }
        return array;
    }

    public static dev_resteasy_grpc_arrays___ArrayHolder getHolder(JavabufTranslator translator, Object o) {
        dev_resteasy_grpc_arrays___ArrayHolder.Builder builder = dev_resteasy_grpc_arrays___ArrayHolder.newBuilder();
        builder.setComponentClass(wrapPrimitiveTypes(o.getClass().getComponentType().getName()));
        if (o.getClass().getComponentType().isArray()) {
            builder.setBottom(false);
            for (int i = 0; i < Array.getLength(o); i++) {
                dev_resteasy_grpc_arrays___ArrayHolder ah = getHolder(translator, Array.get(o, i));
                Any any = Any.pack(ah);
                builder.addGoogleProtobufAnyField(any);
            }
        } else {
            builder.setBottom(true);
            for (int i = 0; i < Array.getLength(o); i++) {
                builder.addGoogleProtobufAnyField(Any.pack(translator.translateToJavabuf(Array.get(o, i))));
            }
        }
        return builder.build();
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
}
