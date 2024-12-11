package dev.resteasy.grpc.bridge.runtime.protobuf;

import java.io.IOException;
import java.io.InputStream;

import jakarta.ws.rs.core.GenericType;

import com.google.protobuf.Message;

public interface JavabufTranslator {

    boolean handlesFromJavabuf(Class<?> clazz);

    boolean handlesToJavabuf(Class<?> clazz);

    Object translateFromJavabuf(Message message);

    Object parseFromJavabuf(Class<?> clazz, InputStream is) throws IOException;

    Message translateToJavabuf(Object o);

    Message translateToJavabuf(Object o, GenericType genericType);

    Class translateToJavabufClass(Class<?> clazz);

    Class translateToJavabufClass(String classname);

    Class translatefromJavabufClass(String classname);

    String getOuterClassname();
}
