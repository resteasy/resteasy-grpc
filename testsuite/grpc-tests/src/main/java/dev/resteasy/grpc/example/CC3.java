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

public class CC3 {

    public String s;

    public CC3(String s) {
        this.s = s;
    }

    public CC3() {
    }

    public boolean equals(Object other) {
        if (other == null || !CC3.class.equals(other.getClass())) {
            return false;
        }
        CC3 cc3 = (CC3) other;
        return this.s.equals(cc3.s);
    }

    public static class CC3_Sub {
        String s;
    }
}
