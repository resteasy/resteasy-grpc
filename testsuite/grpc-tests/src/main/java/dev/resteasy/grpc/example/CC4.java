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
package dev.resteasy.grpc.example;

public class CC4 {
    public String s;
    public CC5 cc5;

    public CC4(String s, CC5 cc5) {
        this.s = s;
        this.cc5 = cc5;
    }

    public boolean equals(Object o) {
        if (o == null || !CC4.class.equals(o.getClass())) {
            return false;
        }
        CC4 cc4 = (CC4) o;
        return s.equals(cc4.s) && cc5.k == cc4.cc5.k;
    }
}
