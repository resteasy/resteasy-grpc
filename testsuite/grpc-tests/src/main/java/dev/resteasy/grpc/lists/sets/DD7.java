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

import java.util.List;
import java.util.Set;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("")
public class DD7 {

    @Path("list/l3/s3/set/variable")
    @POST
    public <T> List<L3<S3<Set<T>>>> listL3S3SetTest1(List<L3<S3<Set<T>>>> l) {
        return l;
    }

    @Path("list/l3/s3/set/wildcard")
    @POST
    public List<L3<S3<Set<?>>>> listL3S3SetTest2(List<L3<S3<Set<?>>>> l) {
        return l;
    }

    @Path("list/l3/s3/set/string")
    @POST
    public List<L3<S3<Set<String>>>> listL3S3SetTest3(List<L3<S3<Set<String>>>> l) {
        return l;
    }

    @Path("list/l3/s3/set/object")
    @POST
    public List<L3<S3<Set<Object>>>> listL3S3SetTest4(List<L3<S3<Set<Object>>>> l) {
        return l;
    }

    @Path("list/l3/s3/set/raw")
    @POST
    public List<L3<S3<Set>>> listL3S3SetTest5(List<L3<S3<Set>>> l) {
        return l;
    }
}
