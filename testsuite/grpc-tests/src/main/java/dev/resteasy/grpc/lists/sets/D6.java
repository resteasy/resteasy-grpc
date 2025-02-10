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
package dev.resteasy.grpc.lists.sets;

/**
 * This will be used as a generic type but not raw type, to test
 * generation of raw type version in .proto file.
 */
public class D6<T> {
    T t;

    public D6(T t) {
        this.t = t;
    }

    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (!D6.class.equals(other.getClass())) {
            return false;
        }
        D6 od6 = (D6) other;
        if (t == null) {
            return od6.t == null;
        }
        return t.equals(od6.t);
    }
}
