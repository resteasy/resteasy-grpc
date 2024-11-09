package dev.resteasy.grpc.lists.sets;

import java.util.ArrayList;
import java.util.List;

public class D1 {

    ArrayList<Integer> l00;
    ArrayList<?> l01;
    ArrayList<S1> l02;
    ArrayList<S2> l03;
    ArrayList<S3<String>> l04;

    ArrayList<List<Float>> l10;
    ArrayList<List<?>> l11;
    ArrayList<List<S1>> l12;
    ArrayList<List<S2>> l13;
    ArrayList<List<S3<Integer>>> l14;

    List<Integer> l20;
    List<?> l21;
    List<S1> l22;
    List<S2> l23;
    List<S3<Float>> l24;

    List<List<Float>> l30;
    List<List<?>> l31;
    List<List<S1>> l32;
    List<List<S2>> l33;
    List<List<S3<Long>>> l34;

    List<ArrayList<Float>> l40;
    List<ArrayList<?>> l41;
    List<ArrayList<S1>> l42;
    List<ArrayList<S2>> l43;
    List<ArrayList<S3<Long>>> l44;
}

