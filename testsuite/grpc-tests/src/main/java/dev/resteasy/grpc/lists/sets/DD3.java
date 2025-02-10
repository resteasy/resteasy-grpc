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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("")
public class DD3 {

    //   @Path("set/hashset/map")
    //   @POST
    //   public <T> Map<T, String> setTest2() {
    //      return null;
    //   }

    @Path("set/list/variable")
    @POST
    public <T> Set<List<T>> setListTest1(Set<List<T>> l) {
        return l;
    }

    @Path("set/list/wildcard")
    @POST
    public Set<List<?>> setListTest2(Set<List<?>> l) {
        return l;
    }

    @Path("set/list/string")
    @POST
    public Set<List<String>> setListTest3(Set<List<String>> l) {
        return l;
    }

    @Path("set/list/object")
    @POST
    public Set<List<Object>> setListTest4(Set<List<Object>> l) {
        return l;
    }

    @Path("set/list/notype")
    @POST
    public Set<List> setListTest5(Set<List> l) {
        return l;
    }

    @Path("set/arraylist/variable")
    @POST
    public <T> Set<ArrayList<T>> setArraylistTest1(Set<ArrayList<T>> l) {
        return l;
    }

    @Path("set/arraylist/wildcard")
    @POST
    public Set<ArrayList<?>> setArraylistTest2(Set<ArrayList<?>> l) {
        return l;
    }

    @Path("set/arraylist/string")
    @POST
    public Set<ArrayList<String>> setArraylistTest3(Set<ArrayList<String>> l) {
        return l;
    }

    @Path("set/arraylist/object")
    @POST
    public Set<ArrayList<Object>> setArraylistTest4(Set<ArrayList<Object>> l) {
        return l;
    }

    @Path("set/arraylist/notype")
    @POST
    public Set<ArrayList> setArraylistTest5(Set<ArrayList> l) {
        return l;
    }

    @Path("arraylist/set/variable")
    @POST
    public <T> ArrayList<Set<T>> arraylistSetTest1(ArrayList<Set<T>> l) {
        return l;
    }

    @Path("arraylist/set/wildcard")
    @POST
    public ArrayList<Set<?>> arraylistSetTest2(ArrayList<Set<?>> l) {
        return l;
    }

    @Path("arraylist/set/string")
    @POST
    public ArrayList<Set<String>> arraylistSetTest3(ArrayList<Set<String>> l) {
        return l;
    }

    @Path("arraylist/set/object")
    @POST
    public ArrayList<Set<Object>> arraylistSetTest4(ArrayList<Set<Object>> l) {
        return l;
    }

    @Path("arraylist/set/notype")
    @POST
    public ArrayList<Set> arraylistSetTest5(ArrayList<Set> l) {
        return l;
    }

    @Path("arraylist/hashset/variable")
    @POST
    public <T> ArrayList<HashSet<T>> arraylistHashsetTest1(ArrayList<HashSet<T>> l) {
        return l;
    }

    @Path("arraylist/hashset/wildcard")
    @POST
    public ArrayList<HashSet<?>> arraylistHashsetTest2(ArrayList<HashSet<?>> l) {
        return l;
    }

    @Path("arraylist/hashset/string")
    @POST
    public ArrayList<HashSet<String>> arraylistHashsetTest3(ArrayList<HashSet<String>> l) {
        return l;
    }

    @Path("arraylist/hashset/object")
    @POST
    public ArrayList<HashSet<Object>> arraylistHashsetTest4(ArrayList<HashSet<Object>> l) {
        return l;
    }

    @Path("arraylist/hashset/notype")
    @POST
    public ArrayList<HashSet> arraylistHashsetTest5(ArrayList<HashSet> l) {
        return l;
    }
}
