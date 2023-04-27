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

package org.jboss.resteasy.grpc;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
class SourceWriter implements AutoCloseable {

    private final BufferedWriter writer;
    private final int indentation;

    private int indent;

    SourceWriter(final Path file) throws IOException {
        writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8);
        indentation = 4;
    }

    SourceWriter startBlock(final String line) throws IOException {
        writeLine(line);
        indent += indentation;
        return this;
    }

    SourceWriter startBlock(final String... text) throws IOException {
        for (var s : text) {
            writer.write(s);
        }
        indent += indentation;
        return this;
    }

    SourceWriter writeLine() throws IOException {
        writer.newLine();
        return this;
    }

    SourceWriter writeLine(final String line) throws IOException {
        if (line.isBlank()) {
            writer.newLine();
            return this;
        }
        if (indent != 0) {
            for (var i = 0; i < indent; i++) {
                writer.append(' ');
            }
        }
        writer.write(line);
        writer.newLine();
        return this;
    }

    SourceWriter writeLine(final String... text) throws IOException {
        if (indent != 0) {
            for (var i = 0; i < indent; i++) {
                writer.append(' ');
            }
        }
        for (var s : text) {
            writer.write(s);
        }
        writer.newLine();
        return this;
    }

    SourceWriter endBlock() throws IOException {
        return endBlock("}");
    }

    SourceWriter endBlock(final String line) throws IOException {
        indent -= indentation;
        if (indent < 0) {
            indent = 0;
        }
        return writeLine(line);
    }

    SourceWriter endAndStart(final String line) throws IOException {
        indent -= indentation;
        if (indent < 0) {
            indent = 0;
        }
        return startBlock(line);
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
