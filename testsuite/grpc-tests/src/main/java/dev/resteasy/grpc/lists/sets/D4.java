package dev.resteasy.grpc.lists.sets;

import java.util.List;
import java.util.Set;

public class D4 {

    L3<Integer> l00;
    L3<?> l01;
    L3<L1> l02;
    L3<L2> l03;
    L3<L3<String>> l04;

    L3<Set<Float>> l10;
    L3<List<?>> l11;
    L3<List<L1>> l12;
    L3<List<L2>> l13;
    L3<List<L3<Integer>>> l14;

    List<Integer> l20;
    List<?> l21;
    List<L1> l22;
    List<L2> l23;
    List<L3<Float>> l24;

    List<List<Float>> l30;
    List<List<?>> l31;
    List<List<L1>> l32;
    List<List<L2>> l33;
    List<List<L3<Long>>> l34;

    List<L3<Float>> l40;
    List<L3<?>> l41;
    List<L3<L1>> l42;
    List<L3<L2>> l43;
    List<L3<L3<Long>>> l44;
}
