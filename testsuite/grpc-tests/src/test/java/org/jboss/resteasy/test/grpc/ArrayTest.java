package org.jboss.resteasy.test.grpc;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___ArrayHolder;
import dev.resteasy.grpc.bridge.runtime.protobuf.ArrayUtility;
import dev.resteasy.grpc.bridge.runtime.protobuf.JavabufTranslator;
import dev.resteasy.grpc.example.CC1JavabufTranslator;

//import dev.resteasy.grpc.example.CC1JavabufTranslator;

public class ArrayTest {

    //    @Test
    //    public void testArray() throws Exception {
    //        int[][] intss = new int[][] { { 1, 2 }, { 3, 4 } };
    //        Array_proto.dev_resteasy_grpc_arrays_ArrayHolder ah = ArrayUtility.getHolder(new CC1JavabufTranslator(), intss);
    //        System.out.println(ah);
    //        ArrayUtility.getArray(new CC1JavabufTranslator(), ah);
    //    }
	
//	@Test
	public void testArrayUtility2() throws Exception {
		try {
//			Class.forName("LString");
			Class<?> clazz = Class.forName("[Ljava.lang.Integer;");
			System.out.println(clazz);
		} catch (Exception e) {
			e.printStackTrace();
		}
		JavabufTranslator translator = new CC1JavabufTranslator();
		int[][] intss = new int[][] { { 1, 2 }, { 3, 4 } };
		System.out.println(intss.getClass());
		Integer[][] intSS = new Integer[][] {{Integer.valueOf(1), Integer.valueOf(2)},{Integer.valueOf(3),Integer.valueOf(4)}};
		System.out.println(intSS.getClass());
		dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, intss);
		System.out.println(holder);
		Integer[][] intSS2 = (Integer[][])ArrayUtility.getArray(translator, holder, "CC1_proto");
		Assert.assertArrayEquals(intSS, intSS2);
	}
	
	@Test
	public void testArrayUtility3() throws Exception {
		JavabufTranslator translator = new CC1JavabufTranslator();
		Integer[][][] intSS = new Integer[][][]
				{{{Integer.valueOf(1), Integer.valueOf(2)}, {Integer.valueOf(3),Integer.valueOf(4)}},
				 {{Integer.valueOf(5), Integer.valueOf(6)}, {Integer.valueOf(7),Integer.valueOf(8)}}};

		System.out.println(intSS.getClass());
		dev_resteasy_grpc_arrays___ArrayHolder holder = ArrayUtility.getHolder(translator, intSS);
		System.out.println(holder);
		Integer[][][] intSS2 = (Integer[][][])ArrayUtility.getArray(translator, holder, "CC1_proto");
		Assert.assertArrayEquals(intSS, intSS2);
	}
}
