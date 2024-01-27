package dev.resteasy.grpc.bridge.runtime.protobuf;

import com.google.protobuf.Message;

public interface JavabufTranslator {

    boolean handlesFromJavabuf(Class<?> clazz);

    boolean handlesToJavabuf(Class<?> clazz);

    Object translateFromJavabuf(Message message);

    Message translateToJavabuf(Object o);

    Class translateToJavabufClass(Class<?> clazz);

    Class translateToJavabufClass(String classname);

    Class translatefromJavabufClass(String classname);

    String getOuterClassname();
}
