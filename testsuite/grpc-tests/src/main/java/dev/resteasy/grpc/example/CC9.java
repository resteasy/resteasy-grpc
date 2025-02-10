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

public class CC9 {

    boolean bo;
    byte by;
    short s;
    int i;
    long l;
    float f;
    double d;
    char c;
    CC3 cc3;

    Boolean bb;
    Short ss;
    Integer ii;

    public CC9(
            boolean bo,
            byte by,
            short s,
            int i,
            long l,
            float f,
            double d,
            char c,
            CC3 cc3) {
        this.bo = bo;
        this.by = by;
        this.s = s;
        this.i = i;
        this.l = l;
        this.f = f;
        this.d = d;
        this.c = c;
        this.cc3 = cc3;
    }
}
