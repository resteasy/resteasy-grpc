/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2023 Red Hat, Inc., and individual contributors
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

import com.google.protobuf.Message;

/**
 * Given a protobuf descriptor file generated by {@link JavaToProtobufGenerator},
 * an implementation of {@code TranslateFromJavabuf} can translate a javauf class
 * back to its original corresponding Java class.
 * <p/>
 * For example, let Java class {@code a.b.C} be transformed to protobuf message type
 * {@code a_b___C} by {@link JavaToProtobufGenerator}, which is then compiled by {@code protoc}
 * to a {@code com.google.protobuf.Message}, called, say (depending on configuration parameters),
 * {@code a.b.Outer_proto.a_b____C}. Then {@link JavabufTranslatorGenerator} can create
 * a class {@code OuterJavabufTranslator} with an instance {@code a_b___CC_FromJavabuf}
 * of {@code TranslateFromJavabuf}, which can be used as follows:
 *
 * <pre>
 *    a.b.Outer_proto.a_b____C c_proto = ...;
 *    C c = OuterJavabufTranslator.translateFromJavabuf(c_proto); // Uses {@code
 * a_b___CC_FromJavabuf
 * }
 * </pre>
 */
public interface TranslateFromJavabuf {

    /**
     * Copies the contents of a {@code com.google.protobuf.Message} into a new instance of the target class.
     */
    Object assignFromJavabuf(Message message) throws Exception;

    /**
     * Parses from an {@java.lang.InputStream} and passes the result to assignFromJavabuf();
     */
    Object parseFromJavabuf(InputStream is) throws IOException;

    /**
     * Copies the contents of a {@code com.google.protobuf.Message} into {@code obj}, an instance of the target class.
     */
    void assignExistingFromJavabuf(Message message, Object obj) throws Exception;
}
