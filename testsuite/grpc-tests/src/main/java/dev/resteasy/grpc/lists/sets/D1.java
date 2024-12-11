package dev.resteasy.grpc.lists.sets;

import java.util.ArrayList;
import java.util.List;

public class D1<T> {

    ArrayList<T> at = new ArrayList<T>();
    ArrayList<Integer> l00 = new ArrayList<Integer>();;
    ArrayList<?> l01 = new ArrayList<Byte>();
    ArrayList<S1> l02 = new ArrayList<S1>();
    ArrayList<S2> l03 = new ArrayList<S2>();
    ArrayList<S3<String>> l04 = new ArrayList<S3<String>>();

    ArrayList<List<Float>> l10 = new ArrayList<List<Float>>();
    ArrayList<List<?>> l11 = new ArrayList<List<?>>();
    ArrayList<List<S1>> l12 = new ArrayList<List<S1>>();
    ArrayList<List<S2>> l13 = new ArrayList<List<S2>>();
    ArrayList<List<S3<Integer>>> l14 = new ArrayList<List<S3<Integer>>>();

    List<Integer> l20 = new ArrayList<Integer>();
    List<?> l21 = new ArrayList<Short>();
    List<S1> l22 = new ArrayList<S1>();
    List<S2> l23 = new ArrayList<S2>();
    List<S3<Float>> l24 = new ArrayList<S3<Float>>();

    List<List<Float>> l30 = new ArrayList<List<Float>>();
    List<List<?>> l31 = new ArrayList<List<?>>();
    List<List<S1>> l32 = new ArrayList<List<S1>>();
    List<List<S2>> l33 = new ArrayList<List<S2>>();
    List<List<S3<Long>>> l34 = new ArrayList<List<S3<Long>>>();

    List<ArrayList<Float>> l40 = new ArrayList<ArrayList<Float>>();
    List<ArrayList<?>> l41 = new ArrayList<ArrayList<?>>();
    List<ArrayList<S1>> l42 = new ArrayList<ArrayList<S1>>();
    List<ArrayList<S2>> l43 = new ArrayList<ArrayList<S2>>();
    List<ArrayList<S3<Long>>> l44 = new ArrayList<ArrayList<S3<Long>>>();

    List l50 = new ArrayList();
    ArrayList l51 = new ArrayList();

    public boolean equals(Object o) {
        if (!D1.class.equals(o.getClass())) {
            return false;
        }
        D1 d1 = (D1) o;
        return CollectionEquals.equals(l00, d1.l00) &&
                CollectionEquals.equals(l01, d1.l01) &&
                CollectionEquals.equals(l02, d1.l02) &&
                CollectionEquals.equals(l03, d1.l03) &&
                CollectionEquals.equals(l04, d1.l04) &&
                CollectionEquals.equals(l10, d1.l10) &&
                CollectionEquals.equals(l11, d1.l11) &&
                CollectionEquals.equals(l12, d1.l12) &&
                CollectionEquals.equals(l13, d1.l13) &&
                CollectionEquals.equals(l14, d1.l14) &&
                CollectionEquals.equals(l20, d1.l20) &&
                CollectionEquals.equals(l21, d1.l21) &&
                CollectionEquals.equals(l22, d1.l22) &&
                CollectionEquals.equals(l23, d1.l23) &&
                CollectionEquals.equals(l24, d1.l24) &&
                CollectionEquals.equals(l30, d1.l30) &&
                CollectionEquals.equals(l32, d1.l31) &&
                CollectionEquals.equals(l32, d1.l32) &&
                CollectionEquals.equals(l33, d1.l33) &&
                CollectionEquals.equals(l34, d1.l34) &&
                CollectionEquals.equals(l40, d1.l40) &&
                CollectionEquals.equals(l41, d1.l41) &&
                CollectionEquals.equals(l42, d1.l42) &&
                CollectionEquals.equals(l43, d1.l43) &&
                CollectionEquals.equals(l44, d1.l44) &&
                CollectionEquals.equals(l50, d1.l50) &&
                CollectionEquals.equals(l51, d1.l51);
    }
}
