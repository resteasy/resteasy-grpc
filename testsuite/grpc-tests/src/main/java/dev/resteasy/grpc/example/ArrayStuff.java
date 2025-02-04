package dev.resteasy.grpc.example;

import java.util.Arrays;

public class ArrayStuff {

    //    public static class Stuff {
    //        int i;
    //
    //        Stuff() {
    //        }
    //
    //        public Stuff(int i) {
    //            this.i = i;
    //        }
    //
    //        public boolean equals(Object other) {
    //            if (!Stuff.class.equals(other.getClass())) {
    //                return false;
    //            }
    //            Stuff stuff = (Stuff) other;
    //            return this.i == stuff.i;
    //        }
    //    }

    boolean[] bos;
    boolean[][] boss;
    Boolean[] Bos;
    Boolean[][] Boss;

    byte[] bys;
    byte[][] byss;
    Byte[] Bys;
    Byte[][] Byss;

    short[] ss;
    short[][] sss;
    Short[] Ss;
    Short[][] Sss;

    int[] is;
    int[][] iss;
    Integer[] Is;
    Integer[][] Iss;

    long[] ls;
    long[][] lss;
    Long[] Ls;
    Long[][] Lss;

    float[] fs;
    float[][] fss;
    Float[] Fs;
    Float[][] Fss;

    double[] ds;
    double[][] dss;
    Double[] Ds;
    Double[][] Dss;

    char[] cs;
    char[][] css;
    Character[] Cs;
    Character[][] Css;

    //    Stuff[] sts;
    //    Stuff[][] stss;

    public ArrayStuff() {
    }

    public ArrayStuff(boolean down) {
        if (down) {
            bos = new boolean[] { false, false, false };
            boss = new boolean[][] { { false, true }, { false, true } };
            Bos = new Boolean[] { Boolean.FALSE, Boolean.FALSE, Boolean.FALSE };
            Boss = new Boolean[][] { { Boolean.FALSE, Boolean.TRUE }, { Boolean.FALSE, Boolean.TRUE } };

            bys = new byte[] { (byte) 1, (byte) 2, (byte) 3 };
            byss = new byte[][] { { (byte) 4, (byte) 5 }, { (byte) 6, (byte) 7 } };
            Bys = new Byte[] { Byte.valueOf((byte) 8), Byte.valueOf((byte) 9), Byte.valueOf((byte) 10) };
            Byss = new Byte[][] { { Byte.valueOf((byte) 11), Byte.valueOf((byte) 12) },
                    { Byte.valueOf((byte) 13), Byte.valueOf((byte) 14) } };

            ss = new short[] { (short) 15, (short) 16, (short) 17 };
            sss = new short[][] { { (short) 18, (short) 19 }, { (short) 20, (short) 21 } };
            Ss = new Short[] { Short.valueOf((short) 22), Short.valueOf((short) 23), Short.valueOf((short) 24) };
            Sss = new Short[][] { { Short.valueOf((short) 25), Short.valueOf((short) 26) },
                    { Short.valueOf((short) 27), Short.valueOf((short) 28) } };

            is = new int[] { 29, 30, 31 };
            iss = new int[][] { { 32, 33 }, { 34, 35 } };
            Is = new Integer[] { Integer.valueOf(36), Integer.valueOf(37), Integer.valueOf(38) };
            Iss = new Integer[][] { { Integer.valueOf(39), Integer.valueOf(40) },
                    { Integer.valueOf(41), Integer.valueOf(42) } };

            ls = new long[] { 43L, 44L, 45L };
            lss = new long[][] { { 46L, 47L }, { 48L, 49L } };
            Ls = new Long[] { Long.valueOf(50L), Long.valueOf(51L), Long.valueOf(52L) };
            Lss = new Long[][] { { Long.valueOf(53L), Long.valueOf(54L) }, { Long.valueOf(55L), Long.valueOf(56L) } };

            fs = new float[] { 57.1f, 58.2f, 59.3f };
            fss = new float[][] { { 60.4f, 61.5f }, { 62.6f, 63.7f } };
            Fs = new Float[] { Float.valueOf(64.8f), Float.valueOf(65.9f), Float.valueOf(66.0f) };
            Fss = new Float[][] { { Float.valueOf(67.1f), Float.valueOf(68.2f) },
                    { Float.valueOf(69.3f), Float.valueOf(70.4f) } };

            ds = new double[] { 71.5d, 72.6d, 73.7d };
            dss = new double[][] { { 74.8d, 75.9d }, { 76.0d, 77.1d } };
            Ds = new Double[] { Double.valueOf(78.2d), Double.valueOf(79.3d), Double.valueOf(80.4d) };
            Dss = new Double[][] { { Double.valueOf(81.5d), Double.valueOf(82.6d) },
                    { Double.valueOf(83.7d), Double.valueOf(84.8d) } };

            cs = new char[] { 'a', 'b', 'c', 'd' };
            css = new char[][] { { 'e' }, { 'f' }, { 'g' }, { 'h' } };
            Cs = new Character[] { Character.valueOf('i'), Character.valueOf('j'), Character.valueOf('k'),
                    Character.valueOf('l') };
            Css = new Character[][] { { Character.valueOf('m') }, { Character.valueOf('n') }, { Character.valueOf('o') },
                    { Character.valueOf('p') } };

            //            sts = new Stuff[] { new Stuff(85), new Stuff(86), new Stuff(87) };
            //            stss = new Stuff[][] { { new Stuff(88), new Stuff(89) }, { new Stuff(90), new Stuff(91) },
            //                    { new Stuff(92), new Stuff(93) } };

        } else {
            bos = new boolean[] { true, false, false };
            boss = new boolean[][] { { true, false }, { true, false } };
            Bos = new Boolean[] { Boolean.FALSE, Boolean.TRUE, Boolean.FALSE };
            Boss = new Boolean[][] { { Boolean.TRUE, Boolean.FALSE }, { Boolean.TRUE, Boolean.FALSE } };

            bys = new byte[] { (byte) 11, (byte) 12, (byte) 13 };
            byss = new byte[][] { { (byte) 14, (byte) 15 }, { (byte) 16, (byte) 17 } };
            Bys = new Byte[] { Byte.valueOf((byte) 18), Byte.valueOf((byte) 19), Byte.valueOf((byte) 20) };
            Byss = new Byte[][] { { Byte.valueOf((byte) 21), Byte.valueOf((byte) 22) },
                    { Byte.valueOf((byte) 23), Byte.valueOf((byte) 24) } };

            ss = new short[] { (short) 25, (short) 26, (short) 27 };
            sss = new short[][] { { (short) 28, (short) 29 }, { (short) 30, (short) 31 } };
            Ss = new Short[] { Short.valueOf((short) 32), Short.valueOf((short) 33), Short.valueOf((short) 34) };
            Sss = new Short[][] { { Short.valueOf((short) 35), Short.valueOf((short) 36) },
                    { Short.valueOf((short) 37), Short.valueOf((short) 38) } };

            is = new int[] { 39, 40, 41 };
            iss = new int[][] { { 42, 43 }, { 44, 45 } };
            Is = new Integer[] { Integer.valueOf(46), Integer.valueOf(47), Integer.valueOf(48) };
            Iss = new Integer[][] { { Integer.valueOf(49), Integer.valueOf(50) },
                    { Integer.valueOf(51), Integer.valueOf(52) } };

            ls = new long[] { 53L, 54L, 55L };
            lss = new long[][] { { 56L, 57L }, { 58L, 59L } };
            Ls = new Long[] { Long.valueOf(60L), Long.valueOf(61L), Long.valueOf(62L) };
            Lss = new Long[][] { { Long.valueOf(63L), Long.valueOf(64L) }, { Long.valueOf(65L), Long.valueOf(66L) } };

            fs = new float[] { 67.1f, 68.2f, 69.3f };
            fss = new float[][] { { 70.4f, 71.5f }, { 72.6f, 73.7f } };
            Fs = new Float[] { Float.valueOf(74.8f), Float.valueOf(75.9f), Float.valueOf(76.0f) };
            Fss = new Float[][] { { Float.valueOf(77.1f), Float.valueOf(78.2f) },
                    { Float.valueOf(79.3f), Float.valueOf(80.4f) } };

            ds = new double[] { 81.5d, 82.6d, 83.7d };
            dss = new double[][] { { 84.8d, 85.9d }, { 86.0d, 87.1d } };
            Ds = new Double[] { Double.valueOf(88.2d), Double.valueOf(89.3d), Double.valueOf(90.4d) };
            Dss = new Double[][] { { Double.valueOf(91.5d), Double.valueOf(92.6d) },
                    { Double.valueOf(93.7d), Double.valueOf(94.8d) } };

            cs = new char[] { 'p', 'q', 'r', 's' };
            css = new char[][] { { 't' }, { 'u' }, { 'v' }, { 'w' } };
            Cs = new Character[] { Character.valueOf('x'), Character.valueOf('y'), Character.valueOf('z'),
                    Character.valueOf('0') };
            Css = new Character[][] { { Character.valueOf('1') }, { Character.valueOf('2') }, { Character.valueOf('3') },
                    { Character.valueOf('4') } };

            //            sts = new Stuff[] { new Stuff(95), new Stuff(96), new Stuff(97) };
            //            stss = new Stuff[][] { { new Stuff(98), new Stuff(99) }, { new Stuff(100), new Stuff(101) },
            //                    { new Stuff(102), new Stuff(103) } };

        }
    }

    public boolean equals(Object other) {
        if (other == null || !ArrayStuff.class.equals(other.getClass())) {
            return false;
        }
        ArrayStuff as = (ArrayStuff) other;

        boolean b1 = Arrays.equals(bos, as.bos);
        boolean b2 = Arrays.deepEquals(boss, as.boss);
        boolean b3 = Arrays.deepEquals(Bos, as.Bos);
        boolean b4 = Arrays.deepEquals(Boss, as.Boss);

        boolean b5 = Arrays.equals(bys, as.bys);
        boolean b6 = Arrays.deepEquals(byss, as.byss);
        boolean b7 = Arrays.deepEquals(Bys, as.Bys);
        boolean b8 = Arrays.deepEquals(Byss, as.Byss);

        boolean b9 = Arrays.equals(ss, as.ss);
        boolean b10 = Arrays.deepEquals(sss, as.sss);
        boolean b11 = Arrays.deepEquals(Ss, as.Ss);
        boolean b12 = Arrays.deepEquals(Sss, as.Sss);

        boolean b13 = Arrays.equals(is, as.is);
        boolean b14 = Arrays.deepEquals(iss, as.iss);
        boolean b15 = Arrays.deepEquals(Is, as.Is);
        boolean b16 = Arrays.deepEquals(Iss, as.Iss);

        boolean b17 = Arrays.equals(ls, as.ls);
        boolean b18 = Arrays.deepEquals(lss, as.lss);
        boolean b19 = Arrays.deepEquals(Ls, as.Ls);
        boolean b20 = Arrays.deepEquals(Lss, as.Lss);

        boolean b21 = Arrays.equals(fs, as.fs);
        boolean b22 = Arrays.deepEquals(fss, as.fss);
        boolean b23 = Arrays.deepEquals(Fs, as.Fs);
        boolean b24 = Arrays.deepEquals(Fss, as.Fss);

        boolean b25 = Arrays.equals(ds, as.ds);
        boolean b26 = Arrays.deepEquals(dss, as.dss);
        boolean b27 = Arrays.deepEquals(Ds, as.Ds);
        boolean b28 = Arrays.deepEquals(Dss, as.Dss);

        boolean b29 = Arrays.equals(cs, as.cs);
        boolean b30 = Arrays.deepEquals(css, as.css);
        boolean b31 = Arrays.deepEquals(Cs, as.Cs);
        boolean b32 = Arrays.deepEquals(Css, as.Css);
        //        boolean b33 = Arrays.deepEquals(sts, as.sts);
        //        boolean b34 = Arrays.deepEquals(stss, as.stss);

        return b1 && b2 && b3 && b4 && b5 && b6 && b7 && b8 && b9 && b10 &&
                b11 && b12 && b13 && b14 && b15 && b16 && b17 && b18 && b19 && b20 &&
                b21 && b22 && b23 && b24 && b25 && b26 && b27 && b28 && b29 && b30 &&
                b31 && b32;// && b33 && b34;
    }
}
