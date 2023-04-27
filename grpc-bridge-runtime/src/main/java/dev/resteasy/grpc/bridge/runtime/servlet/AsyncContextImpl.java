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

package dev.resteasy.grpc.bridge.runtime.servlet;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import org.jboss.resteasy.concurrent.ContextualExecutors;

public class AsyncContextImpl implements AsyncContext {

    private static ExecutorService executorService = ContextualExecutors.threadPool();

    private ServletRequest servletRequest;
    private ServletResponse servletResponse;
    private Set<AsyncListener> listeners = new HashSet<AsyncListener>();
    long timeout;
    private volatile boolean complete = false;

    public AsyncContextImpl(final ServletRequest servletRequest, final ServletResponse servletResponse) {
        this.servletRequest = servletRequest;
        this.servletResponse = servletResponse;
    }

    public static ExecutorService getExecutorService() {
        return executorService;
    }

    @Override
    public ServletRequest getRequest() {
        return servletRequest;
    }

    @Override
    public ServletResponse getResponse() {
        return servletResponse;
    }

    @Override
    public boolean hasOriginalRequestAndResponse() {
        throw new RuntimeException("hasOriginalRequestAndResponse() not implemented");
    }

    @Override
    public void dispatch() {
        throw new RuntimeException("dispatch() not implemented");
    }

    @Override
    public void dispatch(String path) {
        throw new RuntimeException("dispatch() not implemented");
    }

    @Override
    public void dispatch(ServletContext context, String path) {
        throw new RuntimeException("dispatch() not implemented");
    }

    @Override
    public synchronized void complete() {
        if (complete) {
            return;
        }
        complete = true;
    }

    @Override
    public void start(Runnable run) {
        executorService.execute(run);
    }

    @Override
    public void addListener(AsyncListener listener) {
        listeners.add(listener);
    }

    @Override
    public void addListener(AsyncListener listener, ServletRequest servletRequest, ServletResponse servletResponse) {
        throw new RuntimeException("addListener() not implemented");
    }

    @Override
    public <T extends AsyncListener> T createListener(Class<T> clazz) throws ServletException {
        throw new RuntimeException("createListener() not implemented");
    }

    @Override
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Override
    public long getTimeout() {
        return timeout;
    }
}
