/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2025 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.resteasy.grpc.bridge.runtime.protobuf;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

import jakarta.ws.rs.core.GenericType;

import com.google.protobuf.Message;

public interface JavabufTranslator {

    boolean handlesFromJavabuf(Type genericType, Class<?> clazz);

    boolean handlesToJavabuf(Type genericType, Class<?> clazz);

    Object translateFromJavabuf(Message message);

    Object parseFromJavabuf(Class<?> clazz, InputStream is) throws IOException;

    Message translateToJavabuf(Object o);

    Message translateToJavabuf(Object o, GenericType genericType);

    Class translateToJavabufClass(Class<?> clazz);

    Class translateToJavabufClass(String classname);

    Class translatefromJavabufClass(String classname);

    String getOuterClassname();

    GenericType<?> normalize(GenericType<?> genericType);

    Type normalize(Type type);
}
