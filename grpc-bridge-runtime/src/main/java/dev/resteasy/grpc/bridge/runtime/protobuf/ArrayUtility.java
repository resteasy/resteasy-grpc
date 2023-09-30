package dev.resteasy.grpc.bridge.runtime.protobuf;

import java.lang.reflect.Array;
import java.util.List;

import com.google.protobuf.Any;

import dev.resteasy.grpc.arrays.Array_proto;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___ArrayHolder;

public class ArrayUtility {

    public static Object getArray(JavabufTranslator translator, Array_proto.dev_resteasy_grpc_arrays___ArrayHolder ah)
            throws Exception {
        Class componentClass = Class.forName(ah.getComponentClass());

        Object array = Array.newInstance(componentClass, ah.getGoogleProtobufAnyFieldList().size());
        System.out.println(array.getClass());
        if (ah.getGoogleProtobufAnyFieldList().size() == 0) {
            return array;
        }
        List<Any> list = ah.getGoogleProtobufAnyFieldList();
        if (ah.getBottom()) {
            for (int i = 0; i < list.size(); i++) {
                Array.set(array, i, list.get(i).unpack(componentClass));
            }
        } else {
            //        	Class clazz = translator.toJavabufClass(dev_resteasy_grpc_arrays___ArrayHolder.class);
            for (int i = 0; i < list.size(); i++) {
                Array.set(array, i,
                        translator.translateFromJavabuf(list.get(i).unpack(dev_resteasy_grpc_arrays___ArrayHolder.class)));
            }
        }
        return array;
    }

    public static dev_resteasy_grpc_arrays___ArrayHolder getHolder(JavabufTranslator translator, Object o) {
        dev_resteasy_grpc_arrays___ArrayHolder.Builder builder = dev_resteasy_grpc_arrays___ArrayHolder.newBuilder();
        builder.setComponentClass(o.getClass().getComponentType().getName());
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
}
