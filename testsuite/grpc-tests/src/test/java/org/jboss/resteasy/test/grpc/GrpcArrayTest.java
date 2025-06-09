package org.jboss.resteasy.test.grpc;

import com.google.protobuf.Message;
import dev.resteasy.grpc.arrays.Array_proto;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___Any___WArray;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___Boolean___Array;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___Boolean___WArray;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___Byte___Array;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___Byte___WArray;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___Character___Array;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___Character___WArray;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___Double___Array;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___Double___WArray;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___Float___Array;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___Float___WArray;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___Integer___Array;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___Integer___WArray;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___Long___Array;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___Long___WArray;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___Short___Array;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___Short___WArray;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___String___WArray;
import dev.resteasy.grpc.arrays.ArrayResource;
import dev.resteasy.grpc.arrays.ArrayStuff;
import dev.resteasy.grpc.bridge.runtime.protobuf.JavabufTranslator;
import dev.resteasy.grpc.example.CC1;
import dev.resteasy.grpc.example.CC1_proto.dev_resteasy_grpc_arrays___ArrayHolder;
import dev.resteasy.grpc.example.CC1_proto.dev_resteasy_grpc_arrays___ArrayHolder___WArray;
import dev.resteasy.grpc.example.CC1_proto.dev_resteasy_grpc_arrays___ArrayHolder___wrapper;
import dev.resteasy.grpc.example.CC1_proto.dev_resteasy_grpc_arrays___ArrayStuff;
import dev.resteasy.grpc.example.CC1_proto.dev_resteasy_grpc_arrays_ArrayStuff_INNER_Stuff;
import dev.resteasy.grpc.example.CC1_proto.dev_resteasy_grpc_arrays___ArrayStuff___WArray;
import dev.resteasy.grpc.example.CC1_proto.dev_resteasy_grpc_example___CC2___WArray;
import dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage;
import dev.resteasy.grpc.example.CC1_proto.GeneralReturnMessage;
import dev.resteasy.grpc.example.CC1_Server;
import dev.resteasy.grpc.example.CC1ServiceGrpc;
import dev.resteasy.grpc.example.CC2;
import dev.resteasy.grpc.example.sub.CC8;
import dev.resteasy.grpc.lists.sets.DD1;
import dev.resteasy.grpc.maps.MapResource;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
//import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.wildfly.common.Assert;


/**
 * Tests for a variety of arrays. See also the "array tests" section in
 *
 */
@ExtendWith(ArquillianExtension.class)
@RunAsClient
public class GrpcArrayTest {

    private static JavabufTranslator translator;
    private static ManagedChannel channelPlaintext;
    private static CC1ServiceGrpc.CC1ServiceBlockingStub blockingStub;
    private static dev_resteasy_grpc_arrays___ArrayHolder.Builder ahBuilder = dev_resteasy_grpc_arrays___ArrayHolder
            .newBuilder();
    private static dev_resteasy_grpc_arrays___ArrayHolder___wrapper.Builder ahwBuilder = dev_resteasy_grpc_arrays___ArrayHolder___wrapper
            .newBuilder();
    private static dev_resteasy_grpc_arrays___ArrayHolder___WArray.Builder ahwaBuilder = dev_resteasy_grpc_arrays___ArrayHolder___WArray
            .newBuilder();
    private static GeneralEntityMessage.Builder gemBuilder = GeneralEntityMessage.newBuilder();

    static {
        Class<?> clazz;
        try {
            clazz = Class.forName("dev.resteasy.grpc.example.CC1JavabufTranslator");
            translator = (JavabufTranslator) clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Deployment
    static Archive<?> doDeploy() throws Exception {
        final var resolver = Maven.resolver()
                .loadPomFromFile("pom.xml");
        Archive<?> ar = ShrinkWrap.create(WebArchive.class, GrpcArrayTest.class.getSimpleName() + ".war")
                .addPackage(CC1_Server.class.getPackage())
                .addPackage(AbstractGrpcToJakartaRESTTest.class.getPackage())
                .addPackage(CC1.class.getPackage())
                .addPackage(CC8.class.getPackage())
                .addPackage(DD1.class.getPackage())
                .addPackage(ArrayResource.class.getPackage())
                .addPackage(MapResource.class.getPackage())
                .addAsLibrary(resolver.resolve("dev.resteasy.grpc:grpc-bridge-runtime")
                        .withoutTransitivity()
                        .asSingleFile())
                .addClass(Array_proto.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsManifestResource("MANIFEST.MF", "MANIFEST.MF")
                .addAsWebInfResource("web.xml");
        //        ar.as(ZipExporter.class).exportTo(new File("/tmp/array.war"), true);
        return ar;
    }

    @BeforeAll
    public static void beforeClass() throws Exception {
        accessServletContexts();
        channelPlaintext = ManagedChannelBuilder.forTarget("localhost:9555").usePlaintext().build();
        blockingStub = CC1ServiceGrpc.newBlockingStub(channelPlaintext);
    }

    @AfterAll
    public static void afterClass() throws InterruptedException {
        if (channelPlaintext != null) {
            channelPlaintext.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @BeforeEach
    public void before() {
        clearBuilders();
    }

    static void accessServletContexts() {
        try (
                Client client = ClientBuilder.newClient();
                var response = client.target("http://localhost:8080/grpc-test/grpcToJakartaRest/grpcserver/context")
                        .request()
                        .get()) {
            Assertions.assertEquals(204, response.getStatus());
        }
    }

    @Test
    public void single_boolean_empty() throws Exception {
        boolean[] array = new boolean[] {};
        dev_resteasy_grpc_arrays___Boolean___Array jbArray = (dev_resteasy_grpc_arrays___Boolean___Array) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysBooleanArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.singlePrimitiveBoolean(gem);
        dev_resteasy_grpc_arrays___Boolean___Array result = response.getDevResteasyGrpcArraysBooleanArrayField();
        boolean[] array2 = (boolean[]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.equals(array, array2));
    }

    @Test
    public void single_boolean() throws Exception {
        boolean[] array = new boolean[] { false, true };
        dev_resteasy_grpc_arrays___Boolean___Array jbArray = (dev_resteasy_grpc_arrays___Boolean___Array) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysBooleanArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.singlePrimitiveBoolean(gem);
        dev_resteasy_grpc_arrays___Boolean___Array result = response.getDevResteasyGrpcArraysBooleanArrayField();
        boolean[] array2 = (boolean[]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.equals(array, array2));
    }

    @Test
    public void single_Boolean_empty() throws Exception {
        Boolean[] array = new Boolean[] {};
        dev_resteasy_grpc_arrays___Boolean___WArray jbArray = (dev_resteasy_grpc_arrays___Boolean___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysBooleanWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.singleBoolean(gem);
        dev_resteasy_grpc_arrays___Boolean___WArray result = response.getDevResteasyGrpcArraysBooleanWArrayField();
        Boolean[] array2 = (Boolean[]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void single_Boolean() throws Exception {
        Boolean[] array = new Boolean[] { null, Boolean.valueOf(true), null, Boolean.valueOf(false), null };
        dev_resteasy_grpc_arrays___Boolean___WArray jbArray = (dev_resteasy_grpc_arrays___Boolean___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysBooleanWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.singleBoolean(gem);
        dev_resteasy_grpc_arrays___Boolean___WArray result = response.getDevResteasyGrpcArraysBooleanWArrayField();
        Boolean[] array2 = (Boolean[]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void single_byte_empty() throws Exception {
        byte[] array = new byte[] {};
        dev_resteasy_grpc_arrays___Byte___Array jbArray = (dev_resteasy_grpc_arrays___Byte___Array) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysByteArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.singlePrimitiveByte(gem);
        dev_resteasy_grpc_arrays___Byte___Array result = response.getDevResteasyGrpcArraysByteArrayField();
        byte[] array2 = (byte[]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.equals(array, array2));
    }

    @Test
    public void single_byte() throws Exception {
        byte[] array = new byte[] { (byte) 3, (byte) 5 };
        dev_resteasy_grpc_arrays___Byte___Array jbArray = (dev_resteasy_grpc_arrays___Byte___Array) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysByteArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.singlePrimitiveByte(gem);
        dev_resteasy_grpc_arrays___Byte___Array result = response.getDevResteasyGrpcArraysByteArrayField();
        byte[] array2 = (byte[]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.equals(array, array2));
    }

    @Test
    public void single_Byte_empty() throws Exception {
        Byte[] array = new Byte[] {};
        dev_resteasy_grpc_arrays___Byte___WArray jbArray = (dev_resteasy_grpc_arrays___Byte___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysByteWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.singleByte(gem);
        dev_resteasy_grpc_arrays___Byte___WArray result = response.getDevResteasyGrpcArraysByteWArrayField();
        Byte[] array2 = (Byte[]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void single_Byte() throws Exception {
        Byte[] array = new Byte[] { null, Byte.valueOf((byte) 7), null, Byte.valueOf((byte) 11), null };
        dev_resteasy_grpc_arrays___Byte___WArray jbArray = (dev_resteasy_grpc_arrays___Byte___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysByteWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.singleByte(gem);
        dev_resteasy_grpc_arrays___Byte___WArray result = response.getDevResteasyGrpcArraysByteWArrayField();
        Byte[] array2 = (Byte[]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void single_short_empty() throws Exception {
        short[] array = new short[] {};
        dev_resteasy_grpc_arrays___Short___Array jbArray = (dev_resteasy_grpc_arrays___Short___Array) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysShortArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.singlePrimitiveShort(gem);
        dev_resteasy_grpc_arrays___Short___Array result = response.getDevResteasyGrpcArraysShortArrayField();
        short[] array2 = (short[]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.equals(array, array2));
    }

    @Test
    public void single_short() throws Exception {
        short[] array = new short[] { (short) 3, (short) 5 };
        dev_resteasy_grpc_arrays___Short___Array jbArray = (dev_resteasy_grpc_arrays___Short___Array) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysShortArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.singlePrimitiveShort(gem);
        dev_resteasy_grpc_arrays___Short___Array result = response.getDevResteasyGrpcArraysShortArrayField();
        short[] array2 = (short[]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.equals(array, array2));
    }

    @Test
    public void single_Short_empty() throws Exception {
        Short[] array = new Short[] {};
        dev_resteasy_grpc_arrays___Short___WArray jbArray = (dev_resteasy_grpc_arrays___Short___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysShortWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.singleShort(gem);
        dev_resteasy_grpc_arrays___Short___WArray result = response.getDevResteasyGrpcArraysShortWArrayField();
        Short[] array2 = (Short[]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void single_Short() throws Exception {
        Short[] array = new Short[] { null, Short.valueOf((short) 19), null, Short.valueOf((short) 23), null };
        dev_resteasy_grpc_arrays___Short___WArray jbArray = (dev_resteasy_grpc_arrays___Short___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysShortWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.singleShort(gem);
        dev_resteasy_grpc_arrays___Short___WArray result = response.getDevResteasyGrpcArraysShortWArrayField();
        Short[] array2 = (Short[]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void single_int_empty() throws Exception {
        int[] array = new int[] {};
        dev_resteasy_grpc_arrays___Integer___Array jbArray = (dev_resteasy_grpc_arrays___Integer___Array) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysIntegerArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.singlePrimitiveInt(gem);
        dev_resteasy_grpc_arrays___Integer___Array result = response.getDevResteasyGrpcArraysIntegerArrayField();
        int[] array2 = (int[]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.equals(array, array2));
    }

    @Test
    public void single_int() throws Exception {
        int[] array = new int[] { (int) 3, (int) 5 };
        dev_resteasy_grpc_arrays___Integer___Array jbArray = (dev_resteasy_grpc_arrays___Integer___Array) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysIntegerArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.singlePrimitiveInt(gem);
        dev_resteasy_grpc_arrays___Integer___Array result = response.getDevResteasyGrpcArraysIntegerArrayField();
        int[] array2 = (int[]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.equals(array, array2));
    }

    @Test
    public void single_Integer_empty() throws Exception {
        Integer[] array = new Integer[] {};
        dev_resteasy_grpc_arrays___Integer___WArray jbArray = (dev_resteasy_grpc_arrays___Integer___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysIntegerWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.singleInteger(gem);
        dev_resteasy_grpc_arrays___Integer___WArray result = response.getDevResteasyGrpcArraysIntegerWArrayField();
        Integer[] array2 = (Integer[]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void single_Integer() throws Exception {
        Integer[] array = new Integer[] { null, Integer.valueOf((Integer) 19), null, Integer.valueOf((Integer) 23), null };
        dev_resteasy_grpc_arrays___Integer___WArray jbArray = (dev_resteasy_grpc_arrays___Integer___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysIntegerWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.singleInteger(gem);
        dev_resteasy_grpc_arrays___Integer___WArray result = response.getDevResteasyGrpcArraysIntegerWArrayField();
        Integer[] array2 = (Integer[]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void single_long_empty() throws Exception {
        long[] array = new long[] {};
        dev_resteasy_grpc_arrays___Long___Array jbArray = (dev_resteasy_grpc_arrays___Long___Array) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysLongArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.singlePrimitiveLong(gem);
        dev_resteasy_grpc_arrays___Long___Array result = response.getDevResteasyGrpcArraysLongArrayField();
        long[] array2 = (long[]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.equals(array, array2));
    }

    @Test
    public void single_long() throws Exception {
        long[] array = new long[] { (long) 3, (long) 5 };
        dev_resteasy_grpc_arrays___Long___Array jbArray = (dev_resteasy_grpc_arrays___Long___Array) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysLongArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.singlePrimitiveLong(gem);
        dev_resteasy_grpc_arrays___Long___Array result = response.getDevResteasyGrpcArraysLongArrayField();
        long[] array2 = (long[]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.equals(array, array2));
    }

    @Test
    public void single_Long_empty() throws Exception {
        Long[] array = new Long[] {};
        dev_resteasy_grpc_arrays___Long___WArray jbArray = (dev_resteasy_grpc_arrays___Long___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysLongWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.singleLong(gem);
        dev_resteasy_grpc_arrays___Long___WArray result = response.getDevResteasyGrpcArraysLongWArrayField();
        Long[] array2 = (Long[]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void single_Long() throws Exception {
        Long[] array = new Long[] { null, Long.valueOf((long) 19), null, Long.valueOf((long) 23), null };
        dev_resteasy_grpc_arrays___Long___WArray jbArray = (dev_resteasy_grpc_arrays___Long___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysLongWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.singleLong(gem);
        dev_resteasy_grpc_arrays___Long___WArray result = response.getDevResteasyGrpcArraysLongWArrayField();
        Long[] array2 = (Long[]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void single_float_empty() throws Exception {
        float[] array = new float[] {};
        dev_resteasy_grpc_arrays___Float___Array jbArray = (dev_resteasy_grpc_arrays___Float___Array) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysFloatArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.singlePrimitiveFloat(gem);
        dev_resteasy_grpc_arrays___Float___Array result = response.getDevResteasyGrpcArraysFloatArrayField();
        float[] array2 = (float[]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.equals(array, array2));
    }

    @Test
    public void single_float() throws Exception {
        float[] array = new float[] { 57.1f, 59.2f };
        dev_resteasy_grpc_arrays___Float___Array jbArray = (dev_resteasy_grpc_arrays___Float___Array) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysFloatArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.singlePrimitiveFloat(gem);
        dev_resteasy_grpc_arrays___Float___Array result = response.getDevResteasyGrpcArraysFloatArrayField();
        float[] array2 = (float[]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.equals(array, array2));
    }

    @Test
    public void single_Float_empty() throws Exception {
        Float[] array = new Float[] {};
        dev_resteasy_grpc_arrays___Float___WArray jbArray = (dev_resteasy_grpc_arrays___Float___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysFloatWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.singleFloat(gem);
        dev_resteasy_grpc_arrays___Float___WArray result = response.getDevResteasyGrpcArraysFloatWArrayField();
        Float[] array2 = (Float[]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void single_Float() throws Exception {
        Float[] array = new Float[] { null, Float.valueOf(61.3f), null, Float.valueOf(67.4f), null };
        dev_resteasy_grpc_arrays___Float___WArray jbArray = (dev_resteasy_grpc_arrays___Float___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysFloatWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.singleFloat(gem);
        dev_resteasy_grpc_arrays___Float___WArray result = response.getDevResteasyGrpcArraysFloatWArrayField();
        Float[] array2 = (Float[]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void single_double_empty() throws Exception {
        double[] array = new double[] {};
        dev_resteasy_grpc_arrays___Double___Array jbArray = (dev_resteasy_grpc_arrays___Double___Array) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysDoubleArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.singlePrimitiveDouble(gem);
        dev_resteasy_grpc_arrays___Double___Array result = response.getDevResteasyGrpcArraysDoubleArrayField();
        double[] array2 = (double[]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.equals(array, array2));
    }

    @Test
    public void single_double() throws Exception {
        double[] array = new double[] { 71.5d, 73.6d };
        dev_resteasy_grpc_arrays___Double___Array jbArray = (dev_resteasy_grpc_arrays___Double___Array) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysDoubleArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.singlePrimitiveDouble(gem);
        dev_resteasy_grpc_arrays___Double___Array result = response.getDevResteasyGrpcArraysDoubleArrayField();
        double[] array2 = (double[]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.equals(array, array2));
    }

    @Test
    public void single_Double_empty() throws Exception {
        Double[] array = new Double[] {};
        dev_resteasy_grpc_arrays___Double___WArray jbArray = (dev_resteasy_grpc_arrays___Double___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysDoubleWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.singleDouble(gem);
        dev_resteasy_grpc_arrays___Double___WArray result = response.getDevResteasyGrpcArraysDoubleWArrayField();
        Double[] array2 = (Double[]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void single_Double() throws Exception {
        Double[] array = new Double[] { null, Double.valueOf(79.7d), null, Double.valueOf(83.8d), null };
        dev_resteasy_grpc_arrays___Double___WArray jbArray = (dev_resteasy_grpc_arrays___Double___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysDoubleWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.singleDouble(gem);
        dev_resteasy_grpc_arrays___Double___WArray result = response.getDevResteasyGrpcArraysDoubleWArrayField();
        Double[] array2 = (Double[]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void single_char_empty() throws Exception {
        char[] array = new char[] {};
        dev_resteasy_grpc_arrays___Character___Array jbArray = (dev_resteasy_grpc_arrays___Character___Array) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysCharacterArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.singlePrimitiveChar(gem);
        dev_resteasy_grpc_arrays___Character___Array result = response.getDevResteasyGrpcArraysCharacterArrayField();
        char[] array2 = (char[]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.equals(array, array2));
    }

    @Test
    public void single_char() throws Exception {
        char[] array = new char[] { 'a', 'b' };
        dev_resteasy_grpc_arrays___Character___Array jbArray = (dev_resteasy_grpc_arrays___Character___Array) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysCharacterArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.singlePrimitiveChar(gem);
        dev_resteasy_grpc_arrays___Character___Array result = response.getDevResteasyGrpcArraysCharacterArrayField();
        char[] array2 = (char[]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.equals(array, array2));
    }

    @Test
    public void single_Character_empty() throws Exception {
        Character[] array = new Character[] {};
        dev_resteasy_grpc_arrays___Character___WArray jbArray = (dev_resteasy_grpc_arrays___Character___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysCharacterWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.singleCharacter(gem);
        dev_resteasy_grpc_arrays___Character___WArray result = response.getDevResteasyGrpcArraysCharacterWArrayField();
        Character[] array2 = (Character[]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void single_Character() throws Exception {
        Character[] array = new Character[] { null, Character.valueOf('c'), null, Character.valueOf('d'), null };
        dev_resteasy_grpc_arrays___Character___WArray jbArray = (dev_resteasy_grpc_arrays___Character___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysCharacterWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.singleCharacter(gem);
        dev_resteasy_grpc_arrays___Character___WArray result = response.getDevResteasyGrpcArraysCharacterWArrayField();
        Character[] array2 = (Character[]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void single_String_empty() throws Exception {
        String[] array = new String[] {};
        dev_resteasy_grpc_arrays___String___WArray jbArray = (dev_resteasy_grpc_arrays___String___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysStringWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.singleString(gem);
        dev_resteasy_grpc_arrays___String___WArray result = response.getDevResteasyGrpcArraysStringWArrayField();
        String[] array2 = (String[]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void single_String() throws Exception {
        String[] array = new String[] { null, String.valueOf('c'), null, String.valueOf('d'), null };
        dev_resteasy_grpc_arrays___String___WArray jbArray = (dev_resteasy_grpc_arrays___String___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysStringWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.singleString(gem);
        dev_resteasy_grpc_arrays___String___WArray result = response.getDevResteasyGrpcArraysStringWArrayField();
        String[] array2 = (String[]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void single_Object_empty() throws Exception {
        Object[] array = new Object[] {};
        dev_resteasy_grpc_arrays___Any___WArray jbArray = (dev_resteasy_grpc_arrays___Any___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysAnyWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.singleObject(gem);
        dev_resteasy_grpc_arrays___Any___WArray result = response.getDevResteasyGrpcArraysAnyWArrayField();
        Object[] array2 = (Object[]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void single_Object() throws Exception {
        Object[] array = new Object[] { null, String.valueOf('c'), null, String.valueOf('d'), null };
        dev_resteasy_grpc_arrays___Any___WArray jbArray = (dev_resteasy_grpc_arrays___Any___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysAnyWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.singleObject(gem);
        dev_resteasy_grpc_arrays___Any___WArray result = response.getDevResteasyGrpcArraysAnyWArrayField();
        Object[] array2 = (Object[]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void single_CC2_empty() throws Exception {
        CC2[] array = new CC2[] {};
        dev_resteasy_grpc_example___CC2___WArray jbArray = (dev_resteasy_grpc_example___CC2___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcExampleCC2WArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.singleCC2(gem);
        dev_resteasy_grpc_example___CC2___WArray result = response.getDevResteasyGrpcExampleCC2WArrayField();
        CC2[] array2 = (CC2[]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void single_CC2() throws Exception {
        CC2[] array = new CC2[] { new CC2("abc", 3), null, new CC2("xyz", 5) };
        dev_resteasy_grpc_example___CC2___WArray jbArray = (dev_resteasy_grpc_example___CC2___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcExampleCC2WArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.singleCC2(gem);
        dev_resteasy_grpc_example___CC2___WArray result = response.getDevResteasyGrpcExampleCC2WArrayField();
        CC2[] array2 = (CC2[]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void multiTest_boolean_empty() throws Exception {
        boolean[][] array = new boolean[][] {};
        dev_resteasy_grpc_arrays___ArrayHolder___WArray jbArray = (dev_resteasy_grpc_arrays___ArrayHolder___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysArrayHolderWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.multiBooleanPrimitive(gem);
        dev_resteasy_grpc_arrays___ArrayHolder___WArray result = response.getDevResteasyGrpcArraysArrayHolderWArrayField();
        boolean[][] array2 = (boolean[][]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void multiTest_boolean() throws Exception {
        boolean[][] array = new boolean[][] { { false, true }, { true, false }, { true, false } };
        dev_resteasy_grpc_arrays___ArrayHolder___WArray jbArray = (dev_resteasy_grpc_arrays___ArrayHolder___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysArrayHolderWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.multiBooleanPrimitive(gem);
        dev_resteasy_grpc_arrays___ArrayHolder___WArray result = response.getDevResteasyGrpcArraysArrayHolderWArrayField();
        boolean[][] array2 = (boolean[][]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void multiTest_Boolean_empty() throws Exception {
        Boolean[][] array = new Boolean[][] {};
        dev_resteasy_grpc_arrays___ArrayHolder___WArray jbArray = (dev_resteasy_grpc_arrays___ArrayHolder___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysArrayHolderWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.multiBoolean(gem);
        dev_resteasy_grpc_arrays___ArrayHolder___WArray result = response.getDevResteasyGrpcArraysArrayHolderWArrayField();
        Boolean[][] array2 = (Boolean[][]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void multiTest_Boolean() throws Exception {
        Boolean[][] array = new Boolean[][] { { false, true }, null, { true, false }, { true, false }, null };
        dev_resteasy_grpc_arrays___ArrayHolder___WArray jbArray = (dev_resteasy_grpc_arrays___ArrayHolder___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysArrayHolderWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.multiBoolean(gem);
        dev_resteasy_grpc_arrays___ArrayHolder___WArray result = response.getDevResteasyGrpcArraysArrayHolderWArrayField();
        Boolean[][] array2 = (Boolean[][]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void multiTest_byte_empty() throws Exception {
        byte[][] array = new byte[][] {};
        dev_resteasy_grpc_arrays___ArrayHolder___WArray jbArray = (dev_resteasy_grpc_arrays___ArrayHolder___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysArrayHolderWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.multiBytePrimitive(gem);
        dev_resteasy_grpc_arrays___ArrayHolder___WArray result = response.getDevResteasyGrpcArraysArrayHolderWArrayField();
        byte[][] array2 = (byte[][]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void multiTest_byte() throws Exception {
        byte[][] array = new byte[][] { null, { (byte) 2, (byte) 4 }, null, { (byte) 6, (byte) 8 }, null };
        dev_resteasy_grpc_arrays___ArrayHolder___WArray jbArray = (dev_resteasy_grpc_arrays___ArrayHolder___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysArrayHolderWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.multiBytePrimitive(gem);
        dev_resteasy_grpc_arrays___ArrayHolder___WArray result = response.getDevResteasyGrpcArraysArrayHolderWArrayField();
        byte[][] array2 = (byte[][]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void multiTest_Byte_empty() throws Exception {
        Byte[][] array = new Byte[][] {};
        dev_resteasy_grpc_arrays___ArrayHolder___WArray jbArray = (dev_resteasy_grpc_arrays___ArrayHolder___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysArrayHolderWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.multiByte(gem);
        dev_resteasy_grpc_arrays___ArrayHolder___WArray result = response.getDevResteasyGrpcArraysArrayHolderWArrayField();
        Byte[][] array2 = (Byte[][]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void multiTest_Byte() throws Exception {
        Byte[][] array = new Byte[][] { null, { Byte.valueOf((byte) 2), null, Byte.valueOf((byte) 4) },
                { Byte.valueOf((byte) 6), Byte.valueOf((byte) 8) }, null };
        dev_resteasy_grpc_arrays___ArrayHolder___WArray jbArray = (dev_resteasy_grpc_arrays___ArrayHolder___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysArrayHolderWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.multiByte(gem);
        dev_resteasy_grpc_arrays___ArrayHolder___WArray result = response.getDevResteasyGrpcArraysArrayHolderWArrayField();
        Byte[][] array2 = (Byte[][]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void multiTest_short_empty() throws Exception {
        short[][] array = new short[][] {};
        dev_resteasy_grpc_arrays___ArrayHolder___WArray jbArray = (dev_resteasy_grpc_arrays___ArrayHolder___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysArrayHolderWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.multiShortPrimitive(gem);
        dev_resteasy_grpc_arrays___ArrayHolder___WArray result = response.getDevResteasyGrpcArraysArrayHolderWArrayField();
        short[][] array2 = (short[][]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void multiTest_short() throws Exception {
        short[][] array = new short[][] { null, { (short) 10, (short) 12 }, null, { (short) 14, (short) 16 }, null };
        dev_resteasy_grpc_arrays___ArrayHolder___WArray jbArray = (dev_resteasy_grpc_arrays___ArrayHolder___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysArrayHolderWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.multiShortPrimitive(gem);
        dev_resteasy_grpc_arrays___ArrayHolder___WArray result = response.getDevResteasyGrpcArraysArrayHolderWArrayField();
        short[][] array2 = (short[][]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void multiTest_Short_empty() throws Exception {
        Short[][] array = new Short[][] {};
        dev_resteasy_grpc_arrays___ArrayHolder___WArray jbArray = (dev_resteasy_grpc_arrays___ArrayHolder___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysArrayHolderWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.multiShort(gem);
        dev_resteasy_grpc_arrays___ArrayHolder___WArray result = response.getDevResteasyGrpcArraysArrayHolderWArrayField();
        Short[][] array2 = (Short[][]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void multiTest_Short() throws Exception {
        Short[][] array = new Short[][] { null, { Short.valueOf((short) 18), null, Short.valueOf((short) 20) },
                { Short.valueOf((short) 22), Short.valueOf((short) 24) }, null };
        dev_resteasy_grpc_arrays___ArrayHolder___WArray jbArray = (dev_resteasy_grpc_arrays___ArrayHolder___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysArrayHolderWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.multiShort(gem);
        dev_resteasy_grpc_arrays___ArrayHolder___WArray result = response.getDevResteasyGrpcArraysArrayHolderWArrayField();
        Short[][] array2 = (Short[][]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void multiTest_int_empty() throws Exception {
        int[][] array = new int[][] {};
        dev_resteasy_grpc_arrays___ArrayHolder___WArray jbArray = (dev_resteasy_grpc_arrays___ArrayHolder___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysArrayHolderWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.multiIntPrimitive(gem);
        dev_resteasy_grpc_arrays___ArrayHolder___WArray result = response.getDevResteasyGrpcArraysArrayHolderWArrayField();
        int[][] array2 = (int[][]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void multiTest_int() throws Exception {
        int[][] array = new int[][] { null, { 26, 28 }, null, { 30, 32 }, null };
        dev_resteasy_grpc_arrays___ArrayHolder___WArray jbArray = (dev_resteasy_grpc_arrays___ArrayHolder___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysArrayHolderWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.multiIntPrimitive(gem);
        dev_resteasy_grpc_arrays___ArrayHolder___WArray result = response.getDevResteasyGrpcArraysArrayHolderWArrayField();
        int[][] array2 = (int[][]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void multiTest_Integer_empty() throws Exception {
        Integer[][] array = new Integer[][] {};
        dev_resteasy_grpc_arrays___ArrayHolder___WArray jbArray = (dev_resteasy_grpc_arrays___ArrayHolder___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysArrayHolderWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.multiInteger(gem);
        dev_resteasy_grpc_arrays___ArrayHolder___WArray result = response.getDevResteasyGrpcArraysArrayHolderWArrayField();
        Integer[][] array2 = (Integer[][]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void multiTest_Integer() throws Exception {
        Integer[][] array = new Integer[][] { null, { null, 34, 36 }, null, { 38, 40, null } };
        dev_resteasy_grpc_arrays___ArrayHolder___WArray jbArray = (dev_resteasy_grpc_arrays___ArrayHolder___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysArrayHolderWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.multiInteger(gem);
        dev_resteasy_grpc_arrays___ArrayHolder___WArray result = response.getDevResteasyGrpcArraysArrayHolderWArrayField();
        Integer[][] array2 = (Integer[][]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void multiTest_long_empty() throws Exception {
        long[][] array = new long[][] {};
        dev_resteasy_grpc_arrays___ArrayHolder___WArray jbArray = (dev_resteasy_grpc_arrays___ArrayHolder___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysArrayHolderWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.multiLongPrimitive(gem);
        dev_resteasy_grpc_arrays___ArrayHolder___WArray result = response.getDevResteasyGrpcArraysArrayHolderWArrayField();
        long[][] array2 = (long[][]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void multiTest_long() throws Exception {
        long[][] array = new long[][] { { 42L, 44L }, null, { 46L, 48L } };
        dev_resteasy_grpc_arrays___ArrayHolder___WArray jbArray = (dev_resteasy_grpc_arrays___ArrayHolder___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysArrayHolderWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.multiLongPrimitive(gem);
        dev_resteasy_grpc_arrays___ArrayHolder___WArray result = response.getDevResteasyGrpcArraysArrayHolderWArrayField();
        long[][] array2 = (long[][]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void multiTest_Long_empty() throws Exception {
        Long[][] array = new Long[][] {};
        dev_resteasy_grpc_arrays___ArrayHolder___WArray jbArray = (dev_resteasy_grpc_arrays___ArrayHolder___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysArrayHolderWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.multiLong(gem);
        dev_resteasy_grpc_arrays___ArrayHolder___WArray result = response.getDevResteasyGrpcArraysArrayHolderWArrayField();
        Long[][] array2 = (Long[][]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void multiTest_Long() throws Exception {
        Long[][] array = new Long[][] { { null, Long.valueOf(50L), Long.valueOf(52L) }, null,
                { Long.valueOf(54L), Long.valueOf(56L) }, null };
        dev_resteasy_grpc_arrays___ArrayHolder___WArray jbArray = (dev_resteasy_grpc_arrays___ArrayHolder___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysArrayHolderWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.multiLong(gem);
        dev_resteasy_grpc_arrays___ArrayHolder___WArray result = response.getDevResteasyGrpcArraysArrayHolderWArrayField();
        Long[][] array2 = (Long[][]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void multiTest_float_empty() throws Exception {
        float[][] array = new float[][] {};
        dev_resteasy_grpc_arrays___ArrayHolder___WArray jbArray = (dev_resteasy_grpc_arrays___ArrayHolder___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysArrayHolderWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.multiFloatPrimitive(gem);
        dev_resteasy_grpc_arrays___ArrayHolder___WArray result = response.getDevResteasyGrpcArraysArrayHolderWArrayField();
        float[][] array2 = (float[][]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void multiTest_float() throws Exception {
        float[][] array = new float[][] { null, { 56.0f, 58.2f }, null, { 60.4f, 62.6f }, null };
        dev_resteasy_grpc_arrays___ArrayHolder___WArray jbArray = (dev_resteasy_grpc_arrays___ArrayHolder___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysArrayHolderWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.multiFloatPrimitive(gem);
        dev_resteasy_grpc_arrays___ArrayHolder___WArray result = response.getDevResteasyGrpcArraysArrayHolderWArrayField();
        float[][] array2 = (float[][]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void multiTest_Float_empty() throws Exception {
        Float[][] array = new Float[][] {};
        dev_resteasy_grpc_arrays___ArrayHolder___WArray jbArray = (dev_resteasy_grpc_arrays___ArrayHolder___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysArrayHolderWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.multiFloat(gem);
        dev_resteasy_grpc_arrays___ArrayHolder___WArray result = response.getDevResteasyGrpcArraysArrayHolderWArrayField();
        Float[][] array2 = (Float[][]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void multiTest_Float() throws Exception {
        Float[][] array = new Float[][] { { Float.valueOf(64.8f), null, Float.valueOf(66.0f) },
                { Float.valueOf(68.2f), null, Float.valueOf(70.4f) } };
        dev_resteasy_grpc_arrays___ArrayHolder___WArray jbArray = (dev_resteasy_grpc_arrays___ArrayHolder___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysArrayHolderWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.multiFloat(gem);
        dev_resteasy_grpc_arrays___ArrayHolder___WArray result = response.getDevResteasyGrpcArraysArrayHolderWArrayField();
        Float[][] array2 = (Float[][]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void multiTest_double_empty() throws Exception {
        double[][] array = new double[][] {};
        dev_resteasy_grpc_arrays___ArrayHolder___WArray jbArray = (dev_resteasy_grpc_arrays___ArrayHolder___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysArrayHolderWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.multiDoublePrimitive(gem);
        dev_resteasy_grpc_arrays___ArrayHolder___WArray result = response.getDevResteasyGrpcArraysArrayHolderWArrayField();
        double[][] array2 = (double[][]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void multiTest_double() throws Exception {
        double[][] array = new double[][] { { 72.6d, 74.8d }, null, { 76.0d, 78.2d } };
        dev_resteasy_grpc_arrays___ArrayHolder___WArray jbArray = (dev_resteasy_grpc_arrays___ArrayHolder___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysArrayHolderWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.multiDoublePrimitive(gem);
        dev_resteasy_grpc_arrays___ArrayHolder___WArray result = response.getDevResteasyGrpcArraysArrayHolderWArrayField();
        double[][] array2 = (double[][]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void multiTest_Double_empty() throws Exception {
        Double[][] array = new Double[][] {};
        dev_resteasy_grpc_arrays___ArrayHolder___WArray jbArray = (dev_resteasy_grpc_arrays___ArrayHolder___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysArrayHolderWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.multiDouble(gem);
        dev_resteasy_grpc_arrays___ArrayHolder___WArray result = response.getDevResteasyGrpcArraysArrayHolderWArrayField();
        Double[][] array2 = (Double[][]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void multiTest_Double() throws Exception {
        Double[][] array = new Double[][] { null, { null, Double.valueOf(80.4d), Double.valueOf(82.6d) },
                { Double.valueOf(84.8d), Double.valueOf(86.0d), null } };
        dev_resteasy_grpc_arrays___ArrayHolder___WArray jbArray = (dev_resteasy_grpc_arrays___ArrayHolder___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysArrayHolderWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.multiDouble(gem);
        dev_resteasy_grpc_arrays___ArrayHolder___WArray result = response.getDevResteasyGrpcArraysArrayHolderWArrayField();
        Double[][] array2 = (Double[][]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void multiTest_char_empty() throws Exception {
        char[][] array = new char[][] {};
        dev_resteasy_grpc_arrays___ArrayHolder___WArray jbArray = (dev_resteasy_grpc_arrays___ArrayHolder___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysArrayHolderWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.multiCharPrimitive(gem);
        dev_resteasy_grpc_arrays___ArrayHolder___WArray result = response.getDevResteasyGrpcArraysArrayHolderWArrayField();
        char[][] array2 = (char[][]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void multiTest_char() throws Exception {
        char[][] array = new char[][] { { 'l', 'm' }, null, { 'n', 'o' } };
        dev_resteasy_grpc_arrays___ArrayHolder___WArray jbArray = (dev_resteasy_grpc_arrays___ArrayHolder___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysArrayHolderWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.multiCharPrimitive(gem);
        dev_resteasy_grpc_arrays___ArrayHolder___WArray result = response.getDevResteasyGrpcArraysArrayHolderWArrayField();
        char[][] array2 = (char[][]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void multiTest_Character_empty() throws Exception {
        Character[][] array = new Character[][] {};
        dev_resteasy_grpc_arrays___ArrayHolder___WArray jbArray = (dev_resteasy_grpc_arrays___ArrayHolder___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysArrayHolderWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.multiCharacter(gem);
        dev_resteasy_grpc_arrays___ArrayHolder___WArray result = response.getDevResteasyGrpcArraysArrayHolderWArrayField();
        Character[][] array2 = (Character[][]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void multiTest_Character() throws Exception {
        Character[][] array = new Character[][] { { Character.valueOf('p'), Character.valueOf('q'), null },
                { Character.valueOf('r'), Character.valueOf('s'), null } };
        dev_resteasy_grpc_arrays___ArrayHolder___WArray jbArray = (dev_resteasy_grpc_arrays___ArrayHolder___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysArrayHolderWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.multiCharacter(gem);
        dev_resteasy_grpc_arrays___ArrayHolder___WArray result = response.getDevResteasyGrpcArraysArrayHolderWArrayField();
        Character[][] array2 = (Character[][]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void multiTest_String_empty() throws Exception {
        String[][] array = new String[][] {};
        dev_resteasy_grpc_arrays___ArrayHolder___WArray jbArray = (dev_resteasy_grpc_arrays___ArrayHolder___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysArrayHolderWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.multiString(gem);
        dev_resteasy_grpc_arrays___ArrayHolder___WArray result = response.getDevResteasyGrpcArraysArrayHolderWArrayField();
        String[][] array2 = (String[][]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void multiTest_String() throws Exception {
        String[][] array = new String[][] { { null, null, "tu", "vw" }, { "xyz", null, "abc" } };
        dev_resteasy_grpc_arrays___ArrayHolder___WArray jbArray = (dev_resteasy_grpc_arrays___ArrayHolder___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysArrayHolderWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.multiString(gem);
        dev_resteasy_grpc_arrays___ArrayHolder___WArray result = response.getDevResteasyGrpcArraysArrayHolderWArrayField();
        String[][] array2 = (String[][]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void multiTest2_CC2_empty() throws Exception {
        CC2[][] array = new CC2[][] {};
        dev_resteasy_grpc_arrays___ArrayHolder___WArray jbArray = (dev_resteasy_grpc_arrays___ArrayHolder___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysArrayHolderWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.multiCC2(gem);
        dev_resteasy_grpc_arrays___ArrayHolder___WArray result = response.getDevResteasyGrpcArraysArrayHolderWArrayField();
        CC2[][] array2 = (CC2[][]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void multiTest2_CC2() throws Exception {
        CC2[][] array = new CC2[2][2];
        CC2[] cc2a = new CC2[2];
        cc2a[0] = new CC2("three", 3);
        cc2a[1] = new CC2("five", 5);
        array[0] = cc2a;
        CC2[] cc2b = new CC2[2];
        cc2b[0] = new CC2("seven", 7);
        cc2b[1] = new CC2("eleven", 11);
        array[1] = cc2b;
        dev_resteasy_grpc_arrays___ArrayHolder___WArray jbArray = (dev_resteasy_grpc_arrays___ArrayHolder___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysArrayHolderWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.multiCC2(gem);
        dev_resteasy_grpc_arrays___ArrayHolder___WArray result = response.getDevResteasyGrpcArraysArrayHolderWArrayField();
        CC2[][] array2 = (CC2[][]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void multiTest2_Object() throws Exception {
        Object[][] array = new Object[2][2];
        CC2[] cc2a = new CC2[2];
        cc2a[0] = new CC2("three", 3);
        cc2a[1] = new CC2("five", 5);
        array[0] = cc2a;
        CC2[] cc2b = new CC2[2];
        cc2b[0] = new CC2("seven", 7);
        cc2b[1] = new CC2("eleven", 11);
        array[1] = cc2b;
        Message m = translator.translateToJavabuf(array);
        Object[][] array2 = (Object[][]) translator.translateFromJavabuf(m);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void multiTest3_CC2() throws Exception {
        CC2[][][] array = new CC2[2][2][2];
        CC2[] cc2a = new CC2[2];
        cc2a[0] = new CC2("three", 3);
        cc2a[1] = new CC2("five", 5);
        array[0][0] = cc2a;
        CC2[] cc2b = new CC2[2];
        cc2b[0] = new CC2("seven", 7);
        cc2b[1] = new CC2("eleven", 11);
        array[0][1] = cc2b;
        CC2[] cc2c = new CC2[2];
        cc2c[0] = new CC2("thirteen", 13);
        cc2c[1] = new CC2("seventeen", 17);
        array[1][0] = cc2c;
        CC2[] cc2d = new CC2[2];
        cc2d[0] = new CC2("nineteen", 17);
        cc2d[1] = new CC2("twenty-three", 11);
        array[1][1] = cc2d;
        dev_resteasy_grpc_arrays___ArrayHolder___WArray jbArray = (dev_resteasy_grpc_arrays___ArrayHolder___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysArrayHolderWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.tripleCC2(gem);
        dev_resteasy_grpc_arrays___ArrayHolder___WArray result = response.getDevResteasyGrpcArraysArrayHolderWArrayField();
        CC2[][][] array2 = (CC2[][][]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    public void multiTest3_Object() throws Exception {
        Object[][][] array = new Object[2][2][2];
        CC2[] cc2a = new CC2[2];
        cc2a[0] = new CC2("three", 3);
        cc2a[1] = new CC2("five", 5);
        array[0][0] = cc2a;
        CC2[] cc2b = new CC2[2];
        cc2b[0] = new CC2("seven", 7);
        cc2b[1] = new CC2("eleven", 11);
        array[0][1] = cc2b;
        CC2[] cc2c = new CC2[2];
        cc2c[0] = new CC2("thirteen", 13);
        cc2c[1] = new CC2("seventeen", 17);
        array[1][0] = cc2c;
        CC2[] cc2d = new CC2[2];
        cc2d[0] = new CC2("nineteen", 17);
        cc2d[1] = new CC2("twenty-three", 11);
        array[1][1] = cc2d;
        dev_resteasy_grpc_arrays___ArrayHolder___WArray jbArray = (dev_resteasy_grpc_arrays___ArrayHolder___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysArrayHolderWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.tripleObject(gem);
        dev_resteasy_grpc_arrays___ArrayHolder___WArray result = response.getDevResteasyGrpcArraysArrayHolderWArrayField();
        Object[][][] array2 = (Object[][][]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    ////////////////////////////////////////////
    ///       miscellaneous array tests      ///
    ///////////////////////////////////////////
    @Test
    void testArraysInts5() throws Exception {
        int[][][][][] array = new int[][][][][] { { { { { 1, 2, 3 } } } }, { { { { 4, 5 } } } } };
        dev_resteasy_grpc_arrays___ArrayHolder___WArray jbArray = (dev_resteasy_grpc_arrays___ArrayHolder___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysArrayHolderWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.arraysInt5(gem);
        dev_resteasy_grpc_arrays___ArrayHolder___WArray result = response.getDevResteasyGrpcArraysArrayHolderWArrayField();
        int[][][][][] array2 = (int[][][][][]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    @Test
    void testArrayStuff() throws Exception {
        ArrayStuff as = new ArrayStuff(false);
        dev_resteasy_grpc_arrays___ArrayStuff as1 = (dev_resteasy_grpc_arrays___ArrayStuff) translator
                .translateToJavabuf(as);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysArrayStuffField(as1).build();
        GeneralReturnMessage response;
        response = blockingStub.arrayStuff(gem);
        dev_resteasy_grpc_arrays___ArrayStuff as2 = response.getDevResteasyGrpcArraysArrayStuffField();
        ArrayStuff expected = new ArrayStuff(true);
        Assert.assertTrue(expected.equals(translator.translateFromJavabuf(as2)));
    }

    @Test
    void testArrayStuffStuff() throws Exception {
        ArrayStuff.Stuff ass = new ArrayStuff.Stuff(7);
        dev_resteasy_grpc_arrays_ArrayStuff_INNER_Stuff ass1 = (dev_resteasy_grpc_arrays_ArrayStuff_INNER_Stuff) translator
                .translateToJavabuf(ass);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysArrayStuffINNERStuffField(ass1).build();
        GeneralReturnMessage response;
        response = blockingStub.arrayStuffStuff(gem);
        dev_resteasy_grpc_arrays_ArrayStuff_INNER_Stuff result = response.getDevResteasyGrpcArraysArrayStuffINNERStuffField();
        ArrayStuff.Stuff expected = new ArrayStuff.Stuff(14);
        Assert.assertTrue(expected.equals(translator.translateFromJavabuf(result)));
    }

    @Test
    void testArrayStuffWArray() throws Exception {
        ArrayStuff[] array = new ArrayStuff[] { new ArrayStuff(true), new ArrayStuff(false) };
        dev_resteasy_grpc_arrays___ArrayStuff___WArray jbArray = (dev_resteasy_grpc_arrays___ArrayStuff___WArray) translator
                .translateToJavabuf(array);
        GeneralEntityMessage gem = gemBuilder.setDevResteasyGrpcArraysArrayStuffWArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.arrayStuffArray(gem);
        dev_resteasy_grpc_arrays___ArrayStuff___WArray result = response.getDevResteasyGrpcArraysArrayStuffWArrayField();
        ArrayStuff[] array2 = (ArrayStuff[]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.deepEquals(array, array2));
    }

    //============================================================================
    private static void clearBuilders() {
        ahBuilder.clear();
        ahwBuilder.clear();
        ahwaBuilder.clear();
        gemBuilder.clear();
    }
}
