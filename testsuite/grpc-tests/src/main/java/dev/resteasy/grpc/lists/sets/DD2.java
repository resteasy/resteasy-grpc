package dev.resteasy.grpc.lists.sets;

import java.util.HashSet;
import java.util.Set;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

public class DD2 {

    //   @Path("set/hashset/map")
    //   @POST
    //   public <T> Map<T, String> setTest2() {
    //      return null;
    //   }

    @Path("set/variable")
    @POST
    public <T> Set<T> setTest1(Set<T> l) {
        return l;
    }

    @Path("set/wildcard")
    @POST
    public Set<?> setTest2(Set<?> l) {
        return l;
    }

    @Path("set/string")
    @POST
    public Set<String> setTest3(Set<String> l) {
        return l;
    }

    @Path("set/object")
    @POST
    public Set<Object> setTest4(Set<Object> l) {
        return l;
    }

    @Path("set/notype")
    @POST
    public Set setTest5(Set l) {
        return l;
    }

    @Path("hashset/variable")
    @POST
    public <T> HashSet<T> hashSetTest1(HashSet<T> l) {
        return l;
    }

    @Path("hashset/wildcard")
    @POST
    public HashSet<?> hashSetTest2(HashSet<?> l) {
        return l;
    }

    @Path("hashset/string")
    @POST
    public HashSet<String> hashSetTest3(HashSet<String> l) {
        return l;
    }

    @Path("hashset/object")
    @POST
    public HashSet<Object> hashSetTest4(HashSet<Object> l) {
        return l;
    }

    @Path("hashset/notype")
    @POST
    public HashSet hashSetTest5(HashSet l) {
        return l;
    }

    @Path("set/set/variable")
    @POST
    public <T> Set<Set<T>> setSetTest1(Set<Set<T>> l) {
        return l;
    }

    @Path("set/set/wildcard")
    @POST
    public Set<Set<?>> setSetTest2(Set<Set<?>> l) {
        return l;
    }

    @Path("set/set/string")
    @POST
    public Set<Set<String>> setSetTest3(Set<Set<String>> l) {
        return l;
    }

    @Path("set/set/object")
    @POST
    public Set<Set<Object>> setSetTest4(Set<Set<Object>> l) {
        return l;
    }

    @Path("set/set/notype")
    @POST
    public Set<Set> setSetTest5(Set<Set> l) {
        return l;
    }
    
    @Path("set/hashset/variable")
    @POST
    public <T> Set<HashSet<T>> setHashsetTest1(Set<HashSet<T>> l) {
        return l;
    }

    @Path("set/hashset/wildcard")
    @POST
    public Set<HashSet<?>> setHashsetTest2(Set<HashSet<?>> l) {
        return l;
    }

    @Path("set/hashset/string")
    @POST
    public Set<HashSet<String>> setHashsetTest3(Set<HashSet<String>> l) {
        return l;
    }

    @Path("set/hashset/object")
    @POST
    public Set<HashSet<Object>> setHashsetTest4(Set<HashSet<Object>> l) {
        return l;
    }

    @Path("set/hashset/notype")
    @POST
    public Set<HashSet> setHashsetTest5(Set<HashSet> l) {
        return l;
    }

    @Path("hashset/set/variable")
    @POST
    public <T> HashSet<Set<T>> hashsetSetTest1(HashSet<Set<T>> l) {
        return l;
    }

    @Path("hashset/set/wildcard")
    @POST
    public HashSet<Set<?>> hashsetSetTest2(HashSet<Set<?>> l) {
        return l;
    }

    @Path("hashset/set/string")
    @POST
    public HashSet<Set<String>> hashsetSetTest3(HashSet<Set<String>> l) {
        return l;
    }

    @Path("hashset/set/object")
    @POST
    public HashSet<Set<Object>> hashsetSetTest4(HashSet<Set<Object>> l) {
        return l;
    }

    @Path("hashset/set/notype")
    @POST
    public HashSet<Set> hashsetSetTest5(HashSet<Set> l) {
        return l;
    }
    
    @Path("hashset/hashset/variable")
    @POST
    public <T> HashSet<HashSet<T>> hashsetHashsetTest1(HashSet<HashSet<T>> l) {
        return l;
    }

    @Path("hashset/hashset/wildcard")
    @POST
    public HashSet<HashSet<?>> hashsetHashsetTest2(HashSet<HashSet<?>> l) {
        return l;
    }

    @Path("hashset/hashset/string")
    @POST
    public HashSet<HashSet<String>> hashsetHashsetTest3(HashSet<HashSet<String>> l) {
        return l;
    }

    @Path("hashset/hashset/object")
    @POST
    public HashSet<HashSet<Object>> hashsetHashsetTest4(HashSet<HashSet<Object>> l) {
        return l;
    }

    @Path("hashset/hashset/notype")
    @POST
    public HashSet<HashSet> hashsetHashsetTest5(HashSet<HashSet> l) {
        return l;
    }
}
