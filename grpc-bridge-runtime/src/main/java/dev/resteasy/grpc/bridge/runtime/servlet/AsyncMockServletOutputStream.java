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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class AsyncMockServletOutputStream extends MockServletOutputStream {

    private static final ByteArrayOutputStream CLOSE_MARKER = new ByteArrayOutputStream();

    private enum STATE {
        OPEN,
        CLOSING,
        CLOSED
    }

    ;

    private STATE state = STATE.OPEN;
    private volatile ArrayList<ByteArrayOutputStream> list = new ArrayList<ByteArrayOutputStream>();

    @Override
    public boolean isClosed() {
        return state == STATE.CLOSED;
    }

    public synchronized ByteArrayOutputStream await() throws InterruptedException {
        if (state == STATE.CLOSED) {
            return null;
        }
        if (state == STATE.CLOSING) {
            ByteArrayOutputStream baos = list.remove(0);
            if (CLOSE_MARKER == baos) {
                state = STATE.CLOSED;
                return null;
            }
            return baos;
        }
        while (true) {
            if (list.size() > 0) {
                ByteArrayOutputStream baos = list.remove(0);
                if (CLOSE_MARKER == baos) {
                    state = STATE.CLOSED;
                    return null;
                }
                return baos;
            }
            try {
                wait();
            } catch (InterruptedException e) {
                //
            }
        }
    }

    public synchronized void release() throws IOException {
        if (state != STATE.OPEN) {
            return;
        }
        list.add(getDelegate());
        notify();
    }

    public synchronized void release(ByteArrayOutputStream baos) throws IOException {
        if (state != STATE.OPEN) {
            return;
        }
        list.add(baos);
        notify();
    }

    @Override
    public synchronized void close() throws IOException {
        if (state != STATE.OPEN) {
            return;
        }
        if (list.isEmpty()) {
            state = STATE.CLOSED;
        } else {
            state = STATE.CLOSING;
        }
        list.add(CLOSE_MARKER);
        notifyAll();
    }
}
