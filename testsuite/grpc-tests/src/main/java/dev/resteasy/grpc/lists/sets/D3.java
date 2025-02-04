package dev.resteasy.grpc.lists.sets;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class D3 {

    HashSet<Integer> l00 = new HashSet<Integer>();
    HashSet<?> l01 = new HashSet<Byte>();
    HashSet<S1> l02 = new HashSet<S1>();
    HashSet<S2> l03 = new HashSet<S2>();
    HashSet<S3<String>> l04 = new HashSet<S3<String>>();

    HashSet<Set<Float>> l10 = new HashSet<Set<Float>>();
    HashSet<List<?>> l11 = new HashSet<List<?>>();
    HashSet<List<S1>> l12 = new HashSet<List<S1>>();
    HashSet<List<S2>> l13 = new HashSet<List<S2>>();
    HashSet<List<S3<Integer>>> l14 = new HashSet<List<S3<Integer>>>();

    List<Integer> l20 = new ArrayList<Integer>();
    List<?> l21 = new ArrayList<Short>();
    List<S1> l22 = new ArrayList<S1>();
    HashSet<List<S2>> l23 = new HashSet<List<S2>>();
    List<S3<Float>> l24 = new ArrayList<S3<Float>>();

    List<List<Float>> l30 = new ArrayList<List<Float>>();
    List<List<?>> l31 = new ArrayList<List<?>>();
    List<List<S1>> l32 = new ArrayList<List<S1>>();
    List<List<S2>> l33 = new ArrayList<List<S2>>();
    List<List<S3<Long>>> l34 = new ArrayList<List<S3<Long>>>();

    List<HashSet<Float>> l40 = new ArrayList<HashSet<Float>>();
    List<HashSet<?>> l41 = new ArrayList<HashSet<?>>();
    List<HashSet<S1>> l42 = new ArrayList<HashSet<S1>>();
    List<HashSet<S2>> l43 = new ArrayList<HashSet<S2>>();
    List<HashSet<S3<Long>>> l44 = new ArrayList<HashSet<S3<Long>>>();

    public boolean equals(Object o) {
        if (!D3.class.equals(o.getClass())) {
            return false;
        }
        D3 d3 = (D3) o;
        return CollectionEquals.equals(l00, d3.l00) &&
                CollectionEquals.equals(l01, d3.l01) &&
                CollectionEquals.equals(l02, d3.l02) &&
                CollectionEquals.equals(l03, d3.l03) &&
                CollectionEquals.equals(l04, d3.l04) &&
                CollectionEquals.equals(l10, d3.l10) &&
                CollectionEquals.equals(l11, d3.l11) &&
                CollectionEquals.equals(l12, d3.l12) &&
                CollectionEquals.equals(l13, d3.l13) &&
                CollectionEquals.equals(l14, d3.l14) &&
                CollectionEquals.equals(l20, d3.l20) &&
                CollectionEquals.equals(l21, d3.l21) &&
                CollectionEquals.equals(l22, d3.l22) &&
                CollectionEquals.equals(l23, d3.l23) &&
                CollectionEquals.equals(l24, d3.l24) &&
                CollectionEquals.equals(l30, d3.l30) &&
                CollectionEquals.equals(l32, d3.l31) &&
                CollectionEquals.equals(l32, d3.l32) &&
                CollectionEquals.equals(l33, d3.l33) &&
                CollectionEquals.equals(l34, d3.l34) &&
                CollectionEquals.equals(l40, d3.l40) &&
                CollectionEquals.equals(l41, d3.l41) &&
                CollectionEquals.equals(l42, d3.l42) &&
                CollectionEquals.equals(l43, d3.l43) &&
                CollectionEquals.equals(l44, d3.l44);
    }
}
