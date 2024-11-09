package dev.resteasy.grpc.lists.sets;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class D3 {

    HashSet<Integer> l00;
    HashSet<?> l01;
    HashSet<S1> l02;
    HashSet<S2> l03;
    HashSet<S3<String>> l04;

    HashSet<Set<Float>> l10;
    HashSet<List<?>> l11;
    HashSet<List<S1>> l12;
    HashSet<List<S2>> l13;
    HashSet<List<S3<Integer>>> l14;

    List<Integer> l20;
    List<?> l21;
    List<S1> l22;
    HashSet<List<S2>> l23;
    List<S3<Float>> l24;

    List<List<Float>> l30;
    List<List<?>> l31;
    List<List<S1>> l32;
    List<List<S2>> l33;
    List<List<S3<Long>>> l34;

    List<HashSet<Float>> l40;
    List<HashSet<?>> l41;
    List<HashSet<S1>> l42;
    List<HashSet<S2>> l43;
    List<HashSet<S3<Long>>> l44;
}
