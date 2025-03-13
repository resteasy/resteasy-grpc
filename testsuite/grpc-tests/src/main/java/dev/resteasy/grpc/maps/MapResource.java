package dev.resteasy.grpc.maps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("")
public class MapResource {

    @Path("map/notype")
    @POST
    public Map mapNoType(Map m) {
        return m;
    }

    @Path("map/variable/variable")
    @POST
    public <K, V> Map<K, V> mapVarVar(Map<K, V> m) {
        return m;
    }

    @Path("map/wildcard/wildcard")
    @POST
    public Map<?, ?> mapWildWild(Map<?, ?> m) {
        return m;
    }

    @Path("map/string/string")
    @POST
    public Map<String, String> mapStringString(Map<String, String> m) {
        return m;
    }

    @Path("map/string/int")
    @POST
    public Map<String, Integer> mapStringInt(Map<String, Integer> m) {
        return m;
    }

    @Path("map/object/object")
    @POST
    public Map<Object, Object> mapObjObj(Map<Object, Object> m) {
        return m;
    }

    @Path("map/list/set")
    @POST
    public Map<List<Integer>, Set<Integer>> mapListSet(Map<List<Integer>, Set<Integer>> m) {
        return m;
    }

    @Path("map/arraylist/hashset")
    @POST
    public Map<ArrayList, HashSet> mapArraylistHashset(Map<ArrayList, HashSet> m) {
        return m;
    }

    @Path("map/map/map")
    @POST
    public Map<Map<String, String>, Map<Integer, Integer>> mapMapStringMapInt(
            Map<Map<String, String>, Map<Integer, Integer>> m) {
        return m;
    }

    @Path("map/m1/m1")
    @POST
    public Map<M1, M1> mapM1M1(Map<M1, M1> m) {
        return m;
    }

    @Path("hashmap/notype")
    @POST
    public HashMap hashmapNoType(HashMap m) {
        return m;
    }

    @Path("hashmap/variable/variable")
    @POST
    public <K, V> HashMap<K, V> hashmapVarVar(HashMap<K, V> m) {
        return m;
    }

    @Path("hashmap/wildcard/wildcard")
    @POST
    public HashMap<?, ?> hashmapWildWild(HashMap<?, ?> m) {
        return m;
    }

    @Path("hashmap/string/string")
    @POST
    public HashMap<String, String> hashmapStringString(HashMap<String, String> m) {
        return m;
    }

    @Path("hashmap/string/int")
    @POST
    public HashMap<String, Integer> hashmapStringInt(HashMap<String, Integer> m) {
        return m;
    }

    @Path("hashmap/object/object")
    @POST
    public HashMap<Object, Object> hashmapObjObj(HashMap<Object, Object> m) {
        return m;
    }

    @Path("hashmap/list/set")
    @POST
    public HashMap<List<Integer>, Set<Integer>> hashmapListSet(HashMap<List<Integer>, Set<Integer>> m) {
        return m;
    }

    @Path("hashmap/arraylist/hashset")
    @POST
    public HashMap<ArrayList, HashSet> hashmapArraylistHashset(HashMap<ArrayList, HashSet> m) {
        return m;
    }

    @Path("hashmap/hashmap/hashmap")
    @POST
    public HashMap<HashMap<String, String>, HashMap<Integer, Integer>> hashmapHashmapStringHashmapInt(
            HashMap<HashMap<String, String>, HashMap<Integer, Integer>> m) {
        return m;
    }

    @Path("hashmap/m1/m1")
    @POST
    public HashMap<M1, M1> hashmapM1M1(HashMap<M1, M1> m) {
        return m;
    }

    @Path("m1")
    @POST
    public M1 m1(M1 m1) {
        return m1;
    }
}
