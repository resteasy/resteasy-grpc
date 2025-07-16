package dev.resteasy.grpc.maps;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

@Path("")
public class MultiMapResource {

    @Path("multimap/notype")
    @POST
    public MultivaluedMap multimapNoType(MultivaluedMap m) {
        return m;
    }

    @Path("multimap/variable/variable")
    @POST
    public <K, V> MultivaluedMap<K, V> multimapVarVar(MultivaluedMap<K, V> m) {
        return m;
    }

    @Path("multimap/wildcard/wildcard")
    @POST
    public MultivaluedMap<?, ?> multimapWildWild(MultivaluedMap<?, ?> m) {
        return m;
    }

    @Path("multimap/string/string")
    @POST
    public MultivaluedMap<String, String> multimapStringString(MultivaluedMap<String, String> m) {
        return m;
    }

    @Path("multimap/string/int")
    @POST
    public MultivaluedMap<String, Integer> multimapStringInt(MultivaluedMap<String, Integer> m) {
        return m;
    }

    @Path("multimap/object/object")
    @POST
    public MultivaluedMap<Object, Object> multimapObjObj(MultivaluedMap<Object, Object> m) {
        return m;
    }

    @Path("multimap/list/set")
    @POST
    public MultivaluedMap<List<Integer>, Set<Integer>> multimapListSet(MultivaluedMap<List<Integer>, Set<Integer>> m) {
        return m;
    }

    @Path("multimap/arraylist/hashset")
    @POST
    public MultivaluedMap<ArrayList, HashSet> multimapArraylistHashset(MultivaluedMap<ArrayList, HashSet> m) {
        return m;
    }

    @Path("multimap/map/map")
    @POST
    public MultivaluedMap<MultivaluedMap<String, String>, MultivaluedMap<Integer, Integer>> multimapMapStringMapInt(
            MultivaluedMap<MultivaluedMap<String, String>, MultivaluedMap<Integer, Integer>> m) {
        return m;
    }

    @Path("multimap/m1/m1")
    @POST
    public MultivaluedMap<M1, M1> multimapM1M1(MultivaluedMap<M1, M1> m) {
        return m;
    }

    @Path("multihashmap/notype")
    @POST
    public MultivaluedHashMap multimapHashmapNoType(MultivaluedHashMap m) {
        return m;
    }

    @Path("multihashmap/variable/variable")
    @POST
    public <K, V> MultivaluedHashMap<K, V> multimapHashmapVarVar(MultivaluedHashMap<K, V> m) {
        return m;
    }

    @Path("multihashmap/wildcard/wildcard")
    @POST
    public MultivaluedHashMap<?, ?> multimapHashmapWildWild(MultivaluedHashMap<?, ?> m) {
        return m;
    }

    @Path("multihashmap/string/string")
    @POST
    public MultivaluedHashMap<String, String> multimapHashmapStringString(MultivaluedHashMap<String, String> m) {
        return m;
    }

    @Path("multihashmap/string/int")
    @POST
    public MultivaluedHashMap<String, Integer> multimapHashmapStringInt(MultivaluedHashMap<String, Integer> m) {
        return m;
    }

    @Path("multihashmap/object/object")
    @POST
    public MultivaluedHashMap<Object, Object> multimapHashmapObjObj(MultivaluedHashMap<Object, Object> m) {
        return m;
    }

    @Path("multihashmap/list/set")
    @POST
    public MultivaluedHashMap<List<Integer>, Set<Integer>> multimapHashmapListSet(
            MultivaluedHashMap<List<Integer>, Set<Integer>> m) {
        return m;
    }

    @Path("multihashmap/arraylist/hashset")
    @POST
    public MultivaluedHashMap<ArrayList, HashSet> multimapHashmapArraylistHashset(MultivaluedHashMap<ArrayList, HashSet> m) {
        return m;
    }

    @Path("multihashmap/hashmap/hashmap")
    @POST
    public MultivaluedHashMap<MultivaluedHashMap<String, String>, MultivaluedHashMap<Integer, Integer>> multimapHashmapHashmapStringHashmapInt(
            MultivaluedHashMap<MultivaluedHashMap<String, String>, MultivaluedHashMap<Integer, Integer>> m) {
        return m;
    }

    @Path("multihashmap/m1/m1")
    @POST
    public MultivaluedHashMap<M1, M1> multimapHashmapM1M1(MultivaluedHashMap<M1, M1> m) {
        return m;
    }
}
