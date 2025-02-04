package dev.resteasy.grpc.lists.sets;

import java.util.HashSet;
import java.util.Set;

public class D2 {

    HashSet<Integer> s00 = new HashSet<Integer>();
    HashSet<?> s01 = new HashSet<Byte>();
    HashSet<S1> s02 = new HashSet<S1>();
    HashSet<S2> s03 = new HashSet<S2>();
    HashSet<S3<String>> s04 = new HashSet<S3<String>>();

    HashSet<Set<Float>> s10 = new HashSet<Set<Float>>();
    HashSet<Set<?>> s11 = new HashSet<Set<?>>();
    HashSet<Set<S1>> s12 = new HashSet<Set<S1>>();
    HashSet<Set<S2>> s13 = new HashSet<Set<S2>>();
    HashSet<Set<S3<Integer>>> s14 = new HashSet<Set<S3<Integer>>>();

    Set<Integer> s20 = new HashSet<Integer>();
    Set<?> s21 = new HashSet<Short>();
    Set<S1> s22 = new HashSet<S1>();
    Set<S2> s23 = new HashSet<S2>();
    Set<S3<Float>> s24 = new HashSet<S3<Float>>();

    Set<Set<Float>> s30 = new HashSet<Set<Float>>();
    Set<Set<?>> s31 = new HashSet<Set<?>>();
    Set<Set<S1>> s32 = new HashSet<Set<S1>>();
    Set<Set<S2>> s33 = new HashSet<Set<S2>>();
    Set<Set<S3<Long>>> s34 = new HashSet<Set<S3<Long>>>();

    Set<HashSet<Float>> s40 = new HashSet<HashSet<Float>>();
    Set<HashSet<?>> s41 = new HashSet<HashSet<?>>();
    Set<HashSet<S1>> s42 = new HashSet<HashSet<S1>>();
    Set<HashSet<S2>> s43 = new HashSet<HashSet<S2>>();
    Set<HashSet<S3<Long>>> s44 = new HashSet<HashSet<S3<Long>>>();

    Set s50 = new HashSet();
    HashSet s51 = new HashSet();

    public boolean equals(Object o) {
        if (!D2.class.equals(o.getClass())) {
            return false;
        }
        D2 d2 = (D2) o;
        return CollectionEquals.equals(s00, d2.s00) &&
                CollectionEquals.equals(s01, d2.s01) &&
                CollectionEquals.equals(s02, d2.s02) &&
                CollectionEquals.equals(s03, d2.s03) &&
                CollectionEquals.equals(s04, d2.s04) &&
                CollectionEquals.equals(s10, d2.s10) &&
                CollectionEquals.equals(s11, d2.s11) &&
                CollectionEquals.equals(s12, d2.s12) &&
                CollectionEquals.equals(s13, d2.s13) &&
                CollectionEquals.equals(s14, d2.s14) &&
                CollectionEquals.equals(s20, d2.s20) &&
                CollectionEquals.equals(s21, d2.s21) &&
                CollectionEquals.equals(s22, d2.s22) &&
                CollectionEquals.equals(s23, d2.s23) &&
                CollectionEquals.equals(s24, d2.s24) &&
                CollectionEquals.equals(s30, d2.s30) &&
                CollectionEquals.equals(s32, d2.s31) &&
                CollectionEquals.equals(s32, d2.s32) &&
                CollectionEquals.equals(s33, d2.s33) &&
                CollectionEquals.equals(s34, d2.s34) &&
                CollectionEquals.equals(s40, d2.s40) &&
                CollectionEquals.equals(s41, d2.s41) &&
                CollectionEquals.equals(s42, d2.s42) &&
                CollectionEquals.equals(s43, d2.s43) &&
                CollectionEquals.equals(s44, d2.s44) &&
                CollectionEquals.equals(s50, d2.s50) &&
                CollectionEquals.equals(s51, d2.s51);
    }
}
