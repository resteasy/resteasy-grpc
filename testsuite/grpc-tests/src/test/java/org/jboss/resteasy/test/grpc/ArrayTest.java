package org.jboss.resteasy.test.grpc;

import org.junit.Assert;
import org.junit.Test;

import com.google.protobuf.Any;

import dev.resteasy.grpc.arrays.ArrayUtility;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___ArrayHolder;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___IntArray;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___StringArray;
import dev.resteasy.grpc.bridge.runtime.protobuf.JavabufTranslator;
import dev.resteasy.grpc.example.CC1JavabufTranslator;
import dev.resteasy.grpc.example.CC2;

public class ArrayTest {

    private JavabufTranslator translator = new CC1JavabufTranslator();

    @Test
    public void single_boolean_empty() throws Exception {
        boolean[] bs = new boolean[] {};
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, bs);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals((boolean[]) array, bs);
    }

    @Test
    public void single_boolean() throws Exception {
        boolean[] bs = new boolean[] { false, true };
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, bs);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals((boolean[]) array, bs);
    }

    @Test
    public void single_Boolean_empty() throws Exception {
        Boolean[] Bs = new Boolean[] {};
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, Bs);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals((Boolean[]) array, Bs);
    }

    @Test
    public void single_Boolean() throws Exception {
        Boolean[] Bs = new Boolean[] { Boolean.valueOf(true), Boolean.valueOf(false) };
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, Bs);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals((Boolean[]) array, Bs);
    }

    @Test
    public void single_byte_empty() throws Exception {
        byte[] bs = new byte[] {};
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, bs);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals((byte[]) array, bs);
    }

    @Test
    public void single_byte() throws Exception {
        byte[] bs = new byte[] { (byte) 3, (byte) 5 };
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, bs);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals((byte[]) array, bs);
    }

    @Test
    public void single_Byte_empty() throws Exception {
        Byte[] bs = new Byte[] {};
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, bs);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals((Byte[]) array, bs);
    }

    @Test
    public void single_Byte() throws Exception {
        Byte[] bs = new Byte[] { Byte.valueOf((byte) 7), Byte.valueOf((byte) 11) };
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, bs);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals((Byte[]) array, bs);
    }

    @Test
    public void single_short_empty() throws Exception {
        short[] ss = new short[] {};
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, ss);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals((short[]) array, ss);
    }

    @Test
    public void single_short() throws Exception {
        short[] ss = new short[] { (short) 13, (short) 17 };
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, ss);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals((short[]) array, ss);
    }

    @Test
    public void single_Short_empty() throws Exception {
        Short[] ss = new Short[] {};
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, ss);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals((Short[]) array, ss);
    }

    @Test
    public void single_Short() throws Exception {
        Short[] ss = new Short[] { Short.valueOf((short) 19), Short.valueOf((short) 23) };
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, ss);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals((Short[]) array, ss);
    }

    @Test
    public void single_int_empty() throws Exception {
        int[] is = new int[] {};
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, is);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals((int[]) array, is);
    }

    @Test
    public void single_int() throws Exception {
        int[] is = new int[] { 29, 31 };
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, is);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals((int[]) array, is);
    }

    @Test
    public void single_Integer_empty() throws Exception {
        Integer[] is = new Integer[] {};
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, is);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals((Integer[]) array, is);
    }

    @Test
    public void single_Integer() throws Exception {
        Integer[] is = new Integer[] { Integer.valueOf(37), Integer.valueOf(39) };
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, is);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals((Integer[]) array, is);
    }

    @Test
    public void single_long_empty() throws Exception {
        long[] ls = new long[] {};
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, ls);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals((long[]) array, ls);
    }

    @Test
    public void single_long() throws Exception {
        long[] ls = new long[] { 41, 47 };
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, ls);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals((long[]) array, ls);
    }

    @Test
    public void single_Long_empty() throws Exception {
        Long[] ls = new Long[] {};
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, ls);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals((Long[]) array, ls);
    }

    @Test
    public void single_Long() throws Exception {
        Long[] ls = new Long[] { Long.valueOf(49), Long.valueOf(53) };
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, ls);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals((Long[]) array, ls);
    }

    @Test
    public void single_float_empty() throws Exception {
        float[] fs = new float[] {};
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, fs);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals((float[]) array, fs, 0.1f);
    }

    @Test
    public void single_float() throws Exception {
        float[] fs = new float[] { 57.1f, 59.2f };
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, fs);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals((float[]) array, fs, 0.1f);
    }

    @Test
    public void single_Float_empty() throws Exception {
        Float[] fs = new Float[] {};
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, fs);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals((Float[]) array, fs);
    }

    @Test
    public void single_Float() throws Exception {
        Float[] fs = new Float[] { Float.valueOf(61.3f), Float.valueOf(67.4f) };
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, fs);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals((Float[]) array, fs);
    }

    @Test
    public void single_double_empty() throws Exception {
        double[] ds = new double[] {};
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, ds);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals((double[]) array, ds, 0.1d);
    }

    @Test
    public void single_double() throws Exception {
        double[] ds = new double[] { 71.5d, 73.6d };
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, ds);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals((double[]) array, ds, 0.1d);
    }

    @Test
    public void single_Double_empty() throws Exception {
        Double[] ds = new Double[] {};
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, ds);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals((Double[]) array, ds);
    }

    @Test
    public void single_Double() throws Exception {
        Double[] ds = new Double[] { Double.valueOf(79.7d), Double.valueOf(83.8d) };
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, ds);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals((Double[]) array, ds);
    }

    @Test
    public void single_char_empty() throws Exception {
        char[] cs = new char[] {};
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, cs);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals((char[]) array, cs);
    }

    @Test
    public void single_char() throws Exception {
        char[] cs = new char[] { 'a', 'b' };
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, cs);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals((char[]) array, cs);
    }

    @Test
    public void single_Chararacter_empty() throws Exception {
        Character[] cs = new Character[] {};
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, cs);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals((Character[]) array, cs);
    }

    @Test
    public void single_Chararacter() throws Exception {
        Character[] cs = new Character[] { Character.valueOf('c'), Character.valueOf('d') };
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, cs);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals((Character[]) array, cs);
    }

    @Test
    public void single_String_empty() throws Exception {
        String[] ss = new String[] {};
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, ss);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals((String[]) array, ss);
    }

    @Test
    public void single_String() throws Exception {
        String[] ss = new String[] { "ef", "gh" };
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, ss);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals((String[]) array, ss);
    }

    @Test
    public void single_Any_empty() throws Exception {
        Any[] as = new Any[0];
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, as);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals((Any[]) array, as);
    }

    @Test
    public void single_Any() throws Exception {
        Any[] as = new Any[2];
        dev_resteasy_grpc_arrays___IntArray.Builder builder = dev_resteasy_grpc_arrays___IntArray.newBuilder();
        as[0] = Any.pack(builder.addIntField(87).build());
        as[1] = Any.pack(builder.addIntField(89).build());
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, as);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals((Any[]) array, as);
    }

    @Test
    public void single_Message_empty() throws Exception {
        CC2[] cs = new CC2[] {};
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, cs);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals(cs, (CC2[]) array);
    }

    @Test
    public void single_Message() throws Exception {
        CC2[] cs = new CC2[] { new CC2("abc", 3), new CC2("xyz", 5) };
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, cs);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals(cs, (CC2[]) array);
    }

    @Test
    public void multiTest_boolean_empty() throws Exception {
        boolean[][] bss = new boolean[][] {};
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, bss);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals(bss, (boolean[][]) array);
    }

    @Test
    public void multiTest_boolean() throws Exception {
        boolean[][] bss = new boolean[][] { { false, true }, { true, false }, { true, false } };
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, bss);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals(bss, (boolean[][]) array);
    }

    @Test
    public void multiTest_Boolean_empty() throws Exception {
        Boolean[][] bss = new Boolean[][] {};
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, bss);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals(bss, (Boolean[][]) array);
    }

    @Test
    public void multiTest_Boolean() throws Exception {
        Boolean[][] bss = new Boolean[][] { { false, true }, { true, false }, { true, false } };
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, bss);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals(bss, (Boolean[][]) array);
    }

    @Test
    public void multiTest_byte_empty() throws Exception {
        byte[][] bss = new byte[][] {};
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, bss);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals(bss, (byte[][]) array);
    }

    @Test
    public void multiTest_byte() throws Exception {
        byte[][] bss = new byte[][] { { (byte) 2, (byte) 4 }, { (byte) 6, (byte) 8 } };
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, bss);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals(bss, (byte[][]) array);
    }

    @Test
    public void multiTest_Byte_empty() throws Exception {
        Byte[][] bss = new Byte[][] {};
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, bss);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals(bss, (Byte[][]) array);
    }

    @Test
    public void multiTest_Byte() throws Exception {
        Byte[][] bss = new Byte[][] { { Byte.valueOf((byte) 2), Byte.valueOf((byte) 4) },
                { Byte.valueOf((byte) 6), Byte.valueOf((byte) 8) } };
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, bss);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals(bss, (Byte[][]) array);
    }

    @Test
    public void multiTest_short_empty() throws Exception {
        short[][] sss = new short[][] {};
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, sss);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals(sss, (short[][]) array);
    }

    @Test
    public void multiTest_short() throws Exception {
        short[][] sss = new short[][] { { (short) 10, (short) 12 }, { (short) 14, (short) 16 } };
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, sss);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals(sss, (short[][]) array);
    }

    @Test
    public void multiTest_Short_empty() throws Exception {
        Short[][] sss = new Short[][] {};
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, sss);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals(sss, (Short[][]) array);
    }

    @Test
    public void multiTest_Short() throws Exception {
        Short[][] sss = new Short[][] { { Short.valueOf((short) 18), Short.valueOf((short) 20) },
                { Short.valueOf((short) 22), Short.valueOf((short) 24) } };
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, sss);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals(sss, (Short[][]) array);
    }

    @Test
    public void multiTest_int_empty() throws Exception {
        int[][] iss = new int[][] {};
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, iss);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals(iss, (int[][]) array);
    }

    @Test
    public void multiTest_int() throws Exception {
        int[][] iss = new int[][] { { 26, 28 }, { 30, 32 } };
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, iss);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals(iss, (int[][]) array);
    }

    @Test
    public void multiTest_Integer_empty() throws Exception {
        Integer[][] iss = new Integer[][] {};
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, iss);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals(iss, (Integer[][]) array);
    }

    @Test
    public void multiTest_Integer() throws Exception {
        Integer[][] iss = new Integer[][] { { 34, 36 }, { 38, 40 } };
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, iss);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals(iss, (Integer[][]) array);
    }

    @Test
    public void multiTest_long_empty() throws Exception {
        long[][] lss = new long[][] {};
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, lss);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals(lss, (long[][]) array);
    }

    @Test
    public void multiTest_long() throws Exception {
        long[][] lss = new long[][] { { 42L, 44L }, { 46L, 48L } };
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, lss);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals(lss, (long[][]) array);
    }

    @Test
    public void multiTest_Long_empty() throws Exception {
        Long[][] lss = new Long[][] {};
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, lss);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals(lss, (Long[][]) array);
    }

    @Test
    public void multiTest_Long() throws Exception {
        Long[][] lss = new Long[][] { { Long.valueOf(50L), Long.valueOf(52L) }, { Long.valueOf(54L), Long.valueOf(56L) } };
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, lss);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals(lss, (Long[][]) array);
    }

    @Test
    public void multiTest_float_empty() throws Exception {
        float[][] fss = new float[][] {};
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, fss);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals(fss, (float[][]) array);
    }

    @Test
    public void multiTest_float() throws Exception {
        float[][] fss = new float[][] { { 56.0f, 58.2f }, { 60.4f, 62.6f } };
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, fss);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals(fss, (float[][]) array);
    }

    @Test
    public void multiTest_Float_empty() throws Exception {
        Float[][] fss = new Float[][] {};
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, fss);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals(fss, (Float[][]) array);
    }

    @Test
    public void multiTest_Float() throws Exception {
        Float[][] fss = new Float[][] { { Float.valueOf(64.8f), Float.valueOf(66.0f) },
                { Float.valueOf(68.2f), Float.valueOf(70.4f) } };
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, fss);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals(fss, (Float[][]) array);
    }

    @Test
    public void multiTest_double_empty() throws Exception {
        double[][] dss = new double[][] {};
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, dss);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals(dss, (double[][]) array);
    }

    @Test
    public void multiTest_double() throws Exception {
        double[][] dss = new double[][] { { 72.6d, 74.8d }, { 76.0d, 78.2d } };
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, dss);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals(dss, (double[][]) array);
    }

    @Test
    public void multiTest_Double_empty() throws Exception {
        Double[][] dss = new Double[][] {};
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, dss);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals(dss, (Double[][]) array);
    }

    @Test
    public void multiTest_Double() throws Exception {
        Double[][] dss = new Double[][] { { Double.valueOf(80.4d), Double.valueOf(82.6d) },
                { Double.valueOf(84.8d), Double.valueOf(86.0d) } };
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, dss);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals(dss, (Double[][]) array);
    }

    @Test
    public void multiTest_char_empty() throws Exception {
        char[][] css = new char[][] {};
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, css);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals(css, (char[][]) array);
    }

    @Test
    public void multiTest_char() throws Exception {
        char[][] css = new char[][] { { 'l', 'm' }, { 'n', 'o' } };
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, css);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals(css, (char[][]) array);
    }

    @Test
    public void multiTest_Character_empty() throws Exception {
        Character[][] css = new Character[][] {};
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, css);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals(css, (Character[][]) array);
    }

    @Test
    public void multiTest_Character() throws Exception {
        Character[][] css = new Character[][] { { Character.valueOf('p'), Character.valueOf('q') },
                { Character.valueOf('r'), Character.valueOf('s') } };
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, css);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals(css, (Character[][]) array);
    }

    @Test
    public void multiTest_String_empty() throws Exception {
        String[][] css = new String[][] {};
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, css);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals(css, (String[][]) array);
    }

    @Test
    public void multiTest_String() throws Exception {
        String[][] css = new String[][] { { "tu", "vw" }, { "xyz", "abc" } };
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, css);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals(css, (String[][]) array);
    }

    @Test
    public void multiTest_Any_empty() throws Exception {
        Any[][] ass = new Any[][] {};
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, ass);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals(ass, (Any[][]) array);
    }

    @Test
    public void multiTest_Any() throws Exception {
        Any[][] ass = new Any[2][2];
        dev_resteasy_grpc_arrays___IntArray.Builder ibuilder = dev_resteasy_grpc_arrays___IntArray.newBuilder();
        dev_resteasy_grpc_arrays___StringArray.Builder sbuilder = dev_resteasy_grpc_arrays___StringArray.newBuilder();
        ass[0][0] = Any.pack(ibuilder.addIntField(111).build());
        ass[0][1] = Any.pack(sbuilder.addStringField("aaa").build());
        ass[1][0] = Any.pack(ibuilder.addIntField(222).build());
        ass[1][1] = Any.pack(sbuilder.addStringField("bbb").build());
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, ass);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals(ass, (Any[][]) array);
    }

    @Test
    public void multiTest_Message_empty() throws Exception {
        CC2[][] css = new CC2[][] {};
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, css);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals(css, (CC2[][]) array);
    }

    @Test
    public void multiTest_Message() throws Exception {
        CC2[][] css = new CC2[][] { { new CC2("abc", 3), new CC2("xyz", 5) }, { new CC2("lmn", 7), new CC2("pqr", 9) } };
        dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, css);
        Object array = ArrayUtility.getArray(translator, holder);
        Assert.assertArrayEquals(css, (CC2[][]) array);
    }
}
