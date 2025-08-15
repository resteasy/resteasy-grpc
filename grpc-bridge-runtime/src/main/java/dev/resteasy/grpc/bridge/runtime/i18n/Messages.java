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

import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;

/**
 *
 */
@MessageBundle(projectCode = "RESTEASY")
public interface Messages {
    Messages MESSAGES = org.jboss.logging.Messages.getBundle(Messages.class);
    // int BASE = 21000;

    @Message(id = 21000, value = "Async processing already started")
    IllegalStateException asyncAlreadyStarted();

    @Message(id = 21505, value = "Async not started")
    IllegalStateException asyncNotStarted();

    @Message(id = 21510, value = "Async processing already started")
    IllegalStateException asyncProcessingAlreadyStarted();

    @Message(id = 21515, value = "Cannot call getWriter(), getOutputStream() already called")
    IllegalStateException getOutputStreamAlreadyCalled();

    @Message(id = 21520, value = "Cannot call getOutputStream(), getWriter() already called")
    IllegalStateException getWriterAlreadyCalled();

    @Message(id = 21525, value = "Cannot get ServletContext")
    IllegalStateException cantGetServletContext();

    @Message(id = 21530, value = "Header name was null")
    NullPointerException headerNameWasNull();

    @Message(id = 21535, value = "Header %s cannot be converted to a date")
    IllegalArgumentException headerCannotBeConvertedToDate(String header);

    @Message(id = 21540, value = "InputStream already returned")
    String inputStreamAlreadyReturned();

    @Message(id = 21545, value = "Method %s is not implemented")
    String isNotImplemented(String method);

    @Message(id = 21550, value = "Reader already returned")
    IllegalArgumentException readerAlreadyReturned();

    @Message(id = 21555, value = "Request %s was not original or a wrapper")
    IllegalArgumentException requestWasNotOriginalOrWrapper(ServletRequest request);

    @Message(id = 21560, value = "Response %s was not original or a wrapper")
    IllegalArgumentException responseWasNotOriginalOrWrapper(ServletResponse response);

}
