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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("")
public class DD6 {

    @Path("list/set/list/variable")
    @POST
    public <T> List<Set<List<T>>> listSetListTest1(List<Set<List<T>>> l) {
        return l;
    }

    @Path("list/set/list/wildcard")
    @POST
    public <T> List<Set<List<?>>> listSetListTest2(List<Set<List<?>>> l) {
        return l;
    }

    @Path("list/set/list/string")
    @POST
    public <T> List<Set<List<String>>> listSetListTest3(List<Set<List<String>>> l) {
        return l;
    }

    @Path("list/set/list/object")
    @POST
    public <T> List<Set<List<Object>>> listListListTest4(List<Set<List<Object>>> l) {
        return l;
    }

    @Path("list/hashset/list/variable")
    @POST
    public <T> List<HashSet<List<T>>> listHashsetListTest1(List<HashSet<List<T>>> l) {
        return l;
    }

    @Path("list/hashset/list/wildcard")
    @POST
    public <T> List<HashSet<List<?>>> listHashsetListTest2(List<HashSet<List<?>>> l) {
        return l;
    }

    @Path("list/hashset/list/string")
    @POST
    public <T> List<HashSet<List<String>>> listHashsetListTest3(List<HashSet<List<String>>> l) {
        return l;
    }

    @Path("list/hashset/list/object")
    @POST
    public <T> List<HashSet<List<Object>>> listHashsetListTest4(List<HashSet<List<Object>>> l) {
        return l;
    }
}
