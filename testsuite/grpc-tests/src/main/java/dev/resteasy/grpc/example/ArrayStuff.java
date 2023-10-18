package dev.resteasy.grpc.example;

import java.util.Arrays;

public class ArrayStuff {

    static class Stuff {
        int i;

        public Stuff() {
        }

        public Stuff(int i) {
            this.i = i;
        }

        public boolean equals(Object other) {
            System.out.println(Stuff.class);
            System.out.println(other.getClass());
            if (!Stuff.class.equals(other.getClass())) {
                return false;
            }
            Stuff stuff = (Stuff) other;
            return this.i == stuff.i;
        }
    }

    int[][] intss = new int[][] { { 3, 4 }, { 5, 6 } };
    int[] ints = new int[] { 1, 2 };
    int[][][] intsss = new int[][][] { { { 7 }, { 8 } }, { { 9 }, { 10 } } };
    Stuff[] ss = new Stuff[] { new Stuff(11), new Stuff(12) };
    Stuff[][] sss = new Stuff[][] { { new Stuff(13), new Stuff(14) }, { new Stuff(15), new Stuff(16) } };

    public ArrayStuff() {
    }

    public ArrayStuff(int i) {
        ints = new int[] { i++, i++ };
        intss = new int[][] { { i++, i++ }, { i++, i++ } };
        int[][][] intsss = new int[][][] { { { i++ }, { i++ } }, { { i++ }, { i++ } } };
        ss = new Stuff[] { new Stuff(i++), new Stuff(i++) };
        sss = new Stuff[][] { { new Stuff(i++), new Stuff(i++) }, { new Stuff(i++), new Stuff(i++) } };
    }

    public boolean equals(Object other) {
        if (!ArrayStuff.class.equals(other.getClass())) {
            return false;
        }
        ArrayStuff as = (ArrayStuff) other;
        boolean b1 = Arrays.equals(ints, as.ints);
        boolean b2 = Arrays.deepEquals(intss, as.intss);
        boolean b3 = Arrays.deepEquals(intsss, as.intsss);
        boolean b4 = Arrays.deepEquals(ss, as.ss);
        boolean b5 = Arrays.deepEquals(sss, as.sss);
        return Arrays.equals(ints, as.ints)
                && Arrays.deepEquals(intss, as.intss)
                && Arrays.deepEquals(intsss, as.intsss)
                && Arrays.deepEquals(ss, as.ss)
                && Arrays.deepEquals(sss, as.sss);
    }
}
