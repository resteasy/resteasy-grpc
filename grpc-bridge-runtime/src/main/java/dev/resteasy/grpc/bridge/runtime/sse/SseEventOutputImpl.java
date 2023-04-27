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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletionStage;

import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.SseEventSink;

import dev.resteasy.grpc.bridge.runtime.servlet.AsyncMockServletOutputStream;

public class SseEventOutputImpl extends GenericType<OutboundSseEvent> implements SseEventSink {
    private boolean closed;

    private final MessageBodyWriter<Object> writer;
    private final AsyncMockServletOutputStream amsos;

    @SuppressWarnings("unchecked")
    public SseEventOutputImpl(final MessageBodyWriter<?> writer, final AsyncMockServletOutputStream amsos) {
        this.writer = (MessageBodyWriter<Object>) writer;
        this.amsos = amsos;
    }

    @Override
    public void close() {
        closed = true;
        try {
            amsos.close();
        } catch (IOException e) {
            //
        }
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public CompletionStage<?> send(OutboundSseEvent event) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            writer.writeTo(event, null, null, null, null, null, baos);
        } catch (IOException e) {
            //
        }
        return null; //???
    }
}
