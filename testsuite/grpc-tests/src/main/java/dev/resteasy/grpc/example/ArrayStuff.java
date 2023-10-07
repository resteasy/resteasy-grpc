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
            if (Stuff.class.equals(other.getClass())) {
                return false;
            }
            Stuff stuff = (Stuff) other;
            return this.i == stuff.i;
        }
    }

    int[] ints = new int[] { 1, 2 };
    int[][] intss = new int[][] { { 3, 4 }, { 5, 6 } };
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
        return Arrays.equals(ints, as.ints) &&
                Arrays.equals(intss, as.intss) &&
                Arrays.equals(intsss, as.intsss) &&
                Arrays.equals(ss, as.ss) &&
                Arrays.equals(sss, as.sss);
    }
}
