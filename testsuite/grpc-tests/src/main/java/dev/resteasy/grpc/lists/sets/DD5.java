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

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("")
public class DD5 {

    @Path("d1/integer")
    @POST
    public D1<Integer> d1Integer(D1<Integer> d1) {
        return d1;
    }

    @Path("d1/raw")
    @POST
    public D1 d1Raw(D1 d1) {
        return d1;
    }

    @Path("d2")
    @POST
    public D2 d2(D2 d2) {
        return d2;
    }

    @Path("d3")
    @POST
    public D3 d3(D3 d3) {
        return d3;
    }

    @Path("d4")
    @POST
    public D4 d4(D4 d4) {
        return d4;
    }

    @Path("d5")
    @POST
    public D5 d5(D5 d5) {
        return d5;
    }

    @Path("d6")
    @POST
    public D6<String> d6(D6<String> d6) {
        return d6;
    }
}
