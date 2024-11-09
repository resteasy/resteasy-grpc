package dev.resteasy.grpc.lists.sets;

import java.util.HashSet;
import java.util.Set;

public class D2 {

    HashSet<Integer> l00;
    HashSet<?> l01;
    HashSet<S1> l02;
    HashSet<S2> l03;
    HashSet<S3<String>> l04;

    HashSet<Set<Float>> l10;
    HashSet<Set<?>> l11;
    HashSet<Set<S1>> l12;
    HashSet<Set<S2>> l13;
    HashSet<Set<S3<Integer>>> l14;

    Set<Integer> l20;
    Set<?> l21;
    Set<S1> l22;
    Set<S2> l23;
    Set<S3<Float>> l24;

    Set<Set<Float>> l30;
    Set<Set<?>> l31;
    Set<Set<S1>> l32;
    Set<Set<S2>> l33;
    Set<Set<S3<Long>>> l34;

    Set<HashSet<Float>> l40;
    Set<HashSet<?>> l41;
    Set<HashSet<S1>> l42;
    Set<HashSet<S2>> l43;
    Set<HashSet<S3<Long>>> l44;
}
