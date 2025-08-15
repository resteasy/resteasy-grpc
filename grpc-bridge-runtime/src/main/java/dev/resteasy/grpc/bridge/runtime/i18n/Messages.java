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

package dev.resteasy.grpc.bridge.runtime.i18n;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.ws.rs.NotSupportedException;

import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;

/**
 *
 */
@MessageBundle(projectCode = "RESTEASY")
public interface Messages {

    @SuppressWarnings("removal")
    Messages MESSAGES = org.jboss.logging.Messages.getBundle(Messages.class);

    @Message(id = 1000, value = "Async processing already started")
    IllegalStateException asyncAlreadyStarted();

    @Message(id = 1100, value = "Async not started")
    IllegalStateException asyncNotStarted();

    @Message(id = 1200, value = "Async processing already started")
    IllegalStateException asyncProcessingAlreadyStarted();

    @Message(id = 1300, value = "Can't find: %s")
    RuntimeException cantFind(Object o);

    @Message(id = 1400, value = "Can't get javabuf dev.resteasy.grpc.bridge.runtime.servlet.HttpServletResponseImpl")
    RuntimeException cantGetHttpServletResponseImpl();

    @Message(id = 1500, value = "Can't get javabuf MessageBodyReader/Writer")
    RuntimeException cantGetMessageBodyReaderWriter();

    @Message(id = 1600, value = "Don't recognize type: %s")
    RuntimeException dontRecognizeType(Object o);

    @Message(id = 1700, value = "gRPC server ready")
    String gRPCServerReady();

    @Message(id = 1800, value = "Cannot call getWriter(), getOutputStream() already called")
    IllegalStateException getOutputStreamAlreadyCalled();

    @Message(id = 1900, value = "Cannot call getOutputStream(), getWriter() already called")
    IllegalStateException getWriterAlreadyCalled();

    @Message(id = 2000, value = "Header %s cannot be converted to a date")
    IllegalArgumentException headerCannotBeConvertedToDate(String header);

    @Message(id = 2100, value = "Header name was null")
    NullPointerException headerNameWasNull();

    @Message(id = 2200, value = "InputStream already returned")
    IllegalStateException inputStreamAlreadyReturned();

    @Message(id = 2300, value = "Is not an array: %s")
    RuntimeException isNotAnArray(Object o);

    @Message(id = 2400, value = "Method %s is not implemented")
    NotSupportedException isNotImplemented(String method);

    @Message(id = 2500, value = "Reader already returned")
    IllegalStateException readerAlreadyReturned();

    @Message(id = 2600, value = "ready")
    String ready();

    @Message(id = 2700, value = "Request %s was not original or a wrapper")
    IllegalArgumentException requestWasNotOriginalOrWrapper(ServletRequest request);

    @Message(id = 2800, value = "Response %s was not original or a wrapper")
    IllegalArgumentException responseWasNotOriginalOrWrapper(ServletResponse response);

    @Message(id = 2900, value = "*** server shut down")
    String serverShutDown();

    @Message(id = 3000, value = "Server started, listening on port %s")
    String serverStarted(int i);

    @Message(id = 3100, value = "*** shutting down gRPC server since JVM is shutting down")
    String shuttingDownGrpcServer();

    @Message(id = 3200, value = "started gRPC server on port %s")
    String startedGrpcServer(int i);

    @Message(id = 3300, value = "starting gRPC server on port %s")
    String startingGrpcServer(int i);

    @Message(id = 3400, value = "stopping gRPC server on port %s")
    String stoppingGrpcServer(int i);

    @Message(id = 3500, value = "Unable to process as Any: %s")
    RuntimeException unableToProcessAsAny(Object o);
}
