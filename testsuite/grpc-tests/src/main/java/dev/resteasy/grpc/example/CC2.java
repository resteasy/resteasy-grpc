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

public class CC2 extends CC3 {

    public int j;

    public CC2(String s, int j) {
        super(s);
        this.j = j;
    }

    public boolean equals(Object other) {
        if (other == null || !CC2.class.equals(other.getClass())) {
            return false;
        }
        CC2 cc2 = (CC2) other;
        return cc2.s.equals(s) && cc2.j == j;
    }
}
