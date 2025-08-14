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
package dev.resteasy.grpc.bridge.generator.i18n;

import java.lang.invoke.MethodHandles;

import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;

/**
 *
 */
@MessageBundle(projectCode = "RESTEASY-GRPC")
public interface Messages {
    Messages MESSAGES = org.jboss.logging.Messages.getBundle(MethodHandles.lookup(), Messages.class);

    int BASE = 40000;

    @Message(id = BASE + 200, value = "Bad syntax: %s")
    String badSyntax(String code);
    
    @Message(id = BASE + 100, value = "Don't recognize type: %s")
    String dontRecognizeType(String type);
    
    @Message(id = BASE + 30, value = "Not found: %s")
    String notFound(String filename);
    
    @Message(id = BASE + 0, value = "Not implemented: %s")
    String notImplemented(String method);
}
