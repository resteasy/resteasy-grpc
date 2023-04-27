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

package dev.resteasy.grpc.bridge.runtime.sse;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import jakarta.annotation.Priority;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.sse.SseEventSink;

import org.jboss.resteasy.core.ResourceMethodInvoker;
import org.jboss.resteasy.core.ResteasyContext;
import org.jboss.resteasy.core.interception.jaxrs.PostMatchContainerRequestContext;
import org.jboss.resteasy.spi.Dispatcher;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import dev.resteasy.grpc.bridge.runtime.servlet.AsyncMockServletOutputStream;
import dev.resteasy.grpc.bridge.runtime.servlet.HttpServletResponseImpl;

@Provider
@Priority(Integer.MAX_VALUE)
public class SseEventSinkInterceptor implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        ResourceMethodInvoker rmi = ((PostMatchContainerRequestContext) requestContext).getResourceMethod();
        if (rmi.isAsyncStreamProvider() || rmi.isSse()) {
            Dispatcher dispatcher = ResteasyContext.getContextData(Dispatcher.class);
            ResteasyProviderFactory providerFactory = dispatcher != null ? dispatcher.getProviderFactory()
                    : ResteasyProviderFactory.getInstance();
            MessageBodyWriter<?> writer = providerFactory.getMessageBodyWriter(SseEvent.class, null, null,
                    MediaType.WILDCARD_TYPE);
            if (writer == null || !isJavabufReaderWriter(writer)) {
                throw new RuntimeException("???");
            }
            HttpServletResponse response = ResteasyContext.getContextData(HttpServletResponse.class);
            if (!(response instanceof HttpServletResponseImpl)) {
                throw new RuntimeException("???");
            }
            AsyncMockServletOutputStream amsos = (AsyncMockServletOutputStream) response.getOutputStream();
            SseEventOutputImpl sink = new SseEventOutputImpl(writer, amsos);
            ResteasyContext.getContextDataMap().put(SseEventSink.class, sink);
        }
    }

    private static boolean isJavabufReaderWriter(MessageBodyWriter<?> writer) {
        try {
            Method method = writer.getClass()
                    .getDeclaredMethod("isWriteable", Class.class, Type.class, Annotation[].class,
                            MediaType.class);
            return (boolean) method.invoke(writer, SseEvent.class, null, null, null);
        } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            return false;
        }
    }
}
