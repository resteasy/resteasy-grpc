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
package dev.resteasy.grpc.bridge.runtime.servlet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import jakarta.servlet.http.Part;

public class PartImpl implements Part {
    private String name;
    private InputStream is;
    private Collection<String> headerNames = new ArrayList<String>();

    public PartImpl(String name, String s) {
        this.name = name;
        is = new ByteArrayInputStream(s.getBytes());
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return is;
    }

    @Override
    public String getContentType() {
        return "application/grpc-part";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSubmittedFileName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getSize() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void write(String fileName) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void delete() throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public String getHeader(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<String> getHeaders(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<String> getHeaderNames() {
        return headerNames;
    }

}
