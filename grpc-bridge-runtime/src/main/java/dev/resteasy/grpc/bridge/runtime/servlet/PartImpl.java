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
