package dev.resteasy.grpc.lists.sets;

import java.util.ArrayList;
import java.util.List;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("")
public class DD1 {

    //   @Path("list/arraylist/map")
    //   @POST
    //   public <T> Map<T, String> listTest2() {
    //      return null;
    //   }

    @Path("list/variable")
    @POST
    public <T> List<T> listTest1(List<T> l) {
        return l;
    }

    @Path("list/wildcard")
    @POST
    public List<?> listTest2(List<?> l) {
        return l;
    }

    @Path("list/string")
    @POST
    public List<String> listTest3(List<String> l) {
        return l;
    }

    @Path("list/object")
    @POST
    public List<Object> listTest4(List<Object> l) {
        return l;
    }

    @Path("list/notype")
    @POST
    public List listTest5(List l) {
        return l;
    }

    @Path("arraylist/variable")
    @POST
    public <T> ArrayList<T> arrayListTest1(ArrayList<T> l) {
        return l;
    }

    @Path("arraylist/wildcard")
    @POST
    public ArrayList<?> arrayListTest2(ArrayList<?> l) {
        return l;
    }

    @Path("arraylist/string")
    @POST
    public ArrayList<String> arrayListTest3(ArrayList<String> l) {
        return l;
    }

    @Path("arraylist/object")
    @POST
    public ArrayList<Object> arrayListTest4(ArrayList<Object> l) {
        return l;
    }

    @Path("arraylist/notype")
    @POST
    public ArrayList arrayListTest5(ArrayList l) {
        return l;
    }

    @Path("list/list/variable")
    @POST
    public <T> List<List<T>> listListTest1(List<List<T>> l) {
        return l;
    }

    @Path("list/list/wildcard")
    @POST
    public List<List<?>> listListTest2(List<List<?>> l) {
        return l;
    }

    @Path("list/list/string")
    @POST
    public List<List<String>> listListTest3(List<List<String>> l) {
        return l;
    }

    @Path("list/list/object")
    @POST
    public List<List<Object>> listListTest4(List<List<Object>> l) {
        return l;
    }

    @Path("list/list/notype")
    @POST
    public List<List> listListTest5(List<List> l) {
        return l;
    }

    @Path("list/arraylist/variable")
    @POST
    public <T> List<ArrayList<T>> listArraylistTest1(List<ArrayList<T>> l) {
        return l;
    }

    @Path("list/arraylist/wildcard")
    @POST
    public List<ArrayList<?>> listArraylistTest2(List<ArrayList<?>> l) {
        return l;
    }

    @Path("list/arraylist/string")
    @POST
    public List<ArrayList<String>> listArraylistTest3(List<ArrayList<String>> l) {
        return l;
    }

    @Path("list/arraylist/object")
    @POST
    public List<ArrayList<Object>> listArraylistTest4(List<ArrayList<Object>> l) {
        return l;
    }

    @Path("list/arraylist/notype")
    @POST
    public List<ArrayList> listArraylistTest5(List<ArrayList> l) {
        return l;
    }

    @Path("arraylist/list/variable")
    @POST
    public <T> ArrayList<List<T>> arraylistListTest1(ArrayList<List<T>> l) {
        return l;
    }

    @Path("arraylist/list/wildcard")
    @POST
    public ArrayList<List<?>> arraylistListTest2(ArrayList<List<?>> l) {
        return l;
    }

    @Path("arraylist/list/string")
    @POST
    public ArrayList<List<String>> arraylistListTest3(ArrayList<List<String>> l) {
        return l;
    }

    @Path("arraylist/list/object")
    @POST
    public ArrayList<List<Object>> arraylistListTest4(ArrayList<List<Object>> l) {
        return l;
    }

    @Path("arraylist/list/notype")
    @POST
    public ArrayList<List> arraylistListTest5(ArrayList<List> l) {
        return l;
    }

    @Path("arraylist/arraylist/variable")
    @POST
    public <T> ArrayList<ArrayList<T>> arraylistArraylistTest1(ArrayList<ArrayList<T>> l) {
        return l;
    }

    @Path("arraylist/arraylist/wildcard")
    @POST
    public ArrayList<ArrayList<?>> arraylistArraylistTest2(ArrayList<ArrayList<?>> l) {
        return l;
    }

    @Path("arraylist/arraylist/string")
    @POST
    public ArrayList<ArrayList<String>> arraylistArraylistTest3(ArrayList<ArrayList<String>> l) {
        return l;
    }

    @Path("arraylist/arraylist/object")
    @POST
    public ArrayList<ArrayList<Object>> arraylistArraylistTest4(ArrayList<ArrayList<Object>> l) {
        return l;
    }

    @Path("arraylist/arraylist/notype")
    @POST
    public ArrayList<ArrayList> arraylistArraylistTest5(ArrayList<ArrayList> l) {
        return l;
    }
    //    @Path("arraylist/arraylist/")
    //    @POST
    //    public ArrayList<ArrayList<?>> s4() {
    //       return null;
    //    }

    /*
     * @Path("s3")
     *
     * @POST
     * public <T> S3<T> s3() {
     * return new S3();
     * }
     *
     * @Path("s4")
     *
     * @POST
     * public S3<Object> s4() {
     * return new S3();
     * }
     *
     * @Path("s5")
     *
     * @POST
     * public S3<Object> s5() {
     * return new S3();
     * }
     */
    /*
     * @Path("map")
     *
     * @POST
     * public String map(Map<List<?>, Set<?>> map) {
     * return "map";
     * }
     *
     *
     * @Path("list/arraylist/concrete")
     *
     * @POST
     * public ArrayList<String> listTest1(ArrayList<String> l) {
     * return l;
     * }
     *
     * @Path("list/arraylist/variable")
     *
     * @POST
     * public ArrayList<?> listTest2(ArrayList<?> l) {
     * return l;
     * }
     *
     * @Path("list/arraylist/concrete/nested")
     *
     * @POST
     * public ArrayList<ArrayList<String>> listTest3(ArrayList<ArrayList<String>> l) {
     * return l;
     * }
     *
     * @Path("list/arraylist/variable/nested")
     *
     * @POST
     * public ArrayList<ArrayList<?>> listTest4(ArrayList<ArrayList<?>> l) {
     * return l;
     * }
     *
     * @Path("list/l1")
     *
     * @POST
     * public L1 l1(L1 l1) {
     * return l1;
     * }
     *
     * @Path("list/l2")
     *
     * @POST
     * public L2 l2(L2 l) {
     * return l;
     * }
     *
     * @Path("list/l3")
     *
     * @POST
     * public L3<L2> l3(L3<L2> l) {
     * return l;
     * }
     *
     * @Path("list/d")
     *
     * @POST
     * public D1 d1(D1 d) {
     * return d;
     * }
     *
     * @Path("list/s3")
     *
     * @POST
     * public S3<Integer> s3(S3<String> s3s) {
     * return new S3<Integer>();
     * }
     *
     * @Path("l1")
     *
     * @POST
     * List<?> l1(List<?> l1) {
     * return l1;
     * }
     *
     * @Path("Int")
     *
     * @POST
     * public Integer iii(java.lang.Integer i) {
     * return i;
     * }
     *
     * @Path("int")
     *
     * @POST
     * public int iiii(int i) {
     * return i;
     * }
     *
     * @Path("set")
     *
     * @POST
     * public S1 setTest(S1 s) {
     * s.add("abc");
     * return s;
     * }
     *
     * @Path("set")
     *
     * @POST
     * public S2 setTest2(S2 s) {
     * S1 s1 = s.iterator().next();
     * String st = s1.iterator().next();
     * S1 s1a = new S1();
     * s1a.add(st + "abc");
     * S2 s2a = new S2();
     * s2a.add(s1a);
     * return s2a;
     * }
     *
     * @Path("set/set/int")
     *
     * @POST
     * public Set<Integer> s1(Set<Integer> s) {
     * return s;
     * }
     *
     * @Path("set/set/any")
     *
     * @POST
     * public Set<?> s2(Set<?> s) {
     * return s;
     * }
     *
     * @Path("set/set/t")
     *
     * @POST
     * public <T> Set<T> s3(Set<T> s) {
     * return s;
     * }
     *
     * @Path("set/set/any")
     *
     * @POST
     * public Set<?> s4(Set<?> s) {
     * return s;
     * }
     *
     * @Path("set/set/upper")
     *
     * @POST
     * public <T extends S1> Set<T> s5(Set<T> s) {
     * return s;
     * }
     *
     * @Path("set/set/lower")
     *
     * @POST
     * public Set<? super S1> s6(Set<? super S1> s) {
     * return s;
     * }
     *
     * @Path("set/d2")
     *
     * @POST
     * public D2 d2(D2 d) {
     * return d;
     * }
     *
     * @Path("set/list/d3")
     *
     * @POST
     * public D3 d3(D3 d) {
     * return d;
     * }
     *
     * @Path("set/list/d4")
     *
     * @POST
     * public D4 d4(D4 d) {
     * return d;
     * }
     */
}
