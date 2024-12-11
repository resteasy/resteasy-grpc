package org.jboss.resteasy.test.grpc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.GenericType;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.protobuf.Message;

import dev.resteasy.grpc.arrays.Array_proto;
import dev.resteasy.grpc.example.CC1;
import dev.resteasy.grpc.example.CC1JavabufTranslator;
import dev.resteasy.grpc.example.CC1ServiceGrpc;
import dev.resteasy.grpc.example.CC1_Server;
import dev.resteasy.grpc.example.CC1_proto;
import dev.resteasy.grpc.example.CC1_proto.*;
import dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage;
import dev.resteasy.grpc.example.CC1_proto.GeneralReturnMessage;
import dev.resteasy.grpc.example.sub.CC8;
import dev.resteasy.grpc.lists.sets.D1;
import dev.resteasy.grpc.lists.sets.D3;
import dev.resteasy.grpc.lists.sets.DD1;
import dev.resteasy.grpc.lists.sets.L3;
import dev.resteasy.grpc.lists.sets.S1;
import dev.resteasy.grpc.lists.sets.S3;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

@RunWith(Arquillian.class)
@RunAsClient
public class GrpcListsAndSetsTest {

    private static CC1JavabufTranslator translator = new CC1JavabufTranslator();
    private static ManagedChannel channelPlaintext;

    private static CC1ServiceGrpc.CC1ServiceBlockingStub blockingStubPlaintext;

    static {
        Class<?> clazz;
        try {
            clazz = Class.forName("dev.resteasy.grpc.example.CC1JavabufTranslator");
            translator = (CC1JavabufTranslator) clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Deployment
    public static Archive<?> deploy() throws Exception {
        final var resolver = Maven.resolver()
                .loadPomFromFile("pom.xml");
        Archive<?> ar = ShrinkWrap.create(WebArchive.class, GrpcListsAndSetsTest.class.getSimpleName() + ".war")
                .addClass(GrpcListsAndSetsTest.class)
                .addPackage(CC1_Server.class.getPackage())
                //                .addPackage(ExampleApp.class.getPackage())
                .addPackage(CC1.class.getPackage())
                .addPackage(CC8.class.getPackage())
                .addPackage(DD1.class.getPackage())
                .addAsLibrary(resolver.resolve("dev.resteasy.grpc:grpc-bridge-runtime")
                        .withoutTransitivity()
                        .asSingleFile())
                .addClass(Array_proto.class)
                //                .addPackage(ArrayUtility.class.getPackage())
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsManifestResource("MANIFEST.MF", "MANIFEST.MF")
                .addAsWebInfResource("web.xml");
        //       ar.as(ZipExporter.class).exportTo(
        //               new File("/tmp/arrays.war"), true);
        return ar;
    }

    @BeforeClass
    public static void beforeClass() throws Exception {
        accessServletContexts();
        channelPlaintext = ManagedChannelBuilder.forTarget("localhost:9555").usePlaintext().build();
        blockingStubPlaintext = CC1ServiceGrpc.newBlockingStub(channelPlaintext);
    }

    @AfterClass
    public static void afterClass() throws InterruptedException {
        channelPlaintext.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
    }

    static void accessServletContexts() {
        try (//http://localhost:8080/grpc-test/grpcToJakartaRest/grpcserver/context
                Client client = ClientBuilder.newClient();
                //                var response = client.target("http://localhost:8080/grpc-test/grpcserver/context")
                var response = client.target("http://localhost:8080/grpc-test/grpcToJakartaRest/grpcserver/context")
                        .request()
                        .get()) {
            Assert.assertEquals(204, response.getStatus());
        }
    }

    //=======================================================================================
    //
    //                Single layer: e.g. sets of objects, lists of objects
    //
    //=======================================================================================

    // Client: java.util.List<java.lang.Object>
    // Server: List<Object>
    @Test
    public void testListObjectGeneric() {
        java.util.List<java.lang.Object> coll = new java.util.ArrayList<java.lang.Object>();
        coll.add(new S1());
        GenericType<java.util.List<java.lang.Object>> type = new GenericType<java.util.List<java.lang.Object>>() {
        };
        Message m = translator.translateToJavabuf(coll, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJavaUtilList39Field((java_util___List39) m).build();
        GeneralReturnMessage response = blockingStubPlaintext.listTest4(gem);
        java_util___List39 result = response.getJavaUtilList39Field();
        Assert.assertEquals(coll, translator.translateFromJavabuf(result));
    }

    // Client: java.util.List<java.lang.String>
    // Server: List<String>
    @Test
    public void testListStringGeneric() {
        java.util.List<java.lang.String> coll = new java.util.ArrayList<java.lang.String>();
        coll.add("abc");
        GenericType<java.util.List<java.lang.String>> type = new GenericType<java.util.List<java.lang.String>>() {
        };
        Message m = translator.translateToJavabuf(coll, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJavaUtilList40Field((java_util___List40) m).build();
        GeneralReturnMessage response = blockingStubPlaintext.listTest3(gem);
        java_util___List40 result = response.getJavaUtilList40Field();
        Assert.assertEquals(coll, translator.translateFromJavabuf(result));
    }

    // Client: java.util.List<java.lang.Object>
    // Server: List<T>
    @Test
    public void testListS1GenericTypeVariable() {
        java.util.List<dev.resteasy.grpc.lists.sets.S1> coll = new java.util.ArrayList<dev.resteasy.grpc.lists.sets.S1>();
        coll.add(new S1());
        GenericType<java.util.List<java.lang.Object>> type = new GenericType<java.util.List<java.lang.Object>>() {
        };
        Message m = translator.translateToJavabuf(coll, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJavaUtilList39Field((java_util___List39) m).build();
        GeneralReturnMessage response = blockingStubPlaintext.listTest1(gem);
        java_util___List39 result = response.getJavaUtilList39Field();
        Assert.assertEquals(coll, translator.translateFromJavabuf(result));
    }

    // Client: java.util.List<java.lang.Object>
    // Server: List<?>
    @Test
    public void testListS1GenericWildcard() {
        java.util.List<dev.resteasy.grpc.lists.sets.S1> coll = new java.util.ArrayList<dev.resteasy.grpc.lists.sets.S1>();
        coll.add(new S1());
        GenericType<java.util.List<java.lang.Object>> type = new GenericType<java.util.List<java.lang.Object>>() {
        };
        Message m = translator.translateToJavabuf(coll, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJavaUtilList39Field((java_util___List39) m).build();
        GeneralReturnMessage response = blockingStubPlaintext.listTest2(gem);
        java_util___List39 result = response.getJavaUtilList39Field();
        Assert.assertEquals(coll, translator.translateFromJavabuf(result));
    }

    // Client: java.util.List<java.lang.Object>
    // Server: List<Object>
    @Test
    public void testListS1GenericObject() {
        java.util.List<dev.resteasy.grpc.lists.sets.S1> coll = new java.util.ArrayList<dev.resteasy.grpc.lists.sets.S1>();
        coll.add(new S1());
        GenericType<java.util.List<java.lang.Object>> type = new GenericType<java.util.List<java.lang.Object>>() {
        };
        Message m = translator.translateToJavabuf(coll, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJavaUtilList39Field((java_util___List39) m).build();
        GeneralReturnMessage response = blockingStubPlaintext.listTest4(gem);
        java_util___List39 result = response.getJavaUtilList39Field();
        Assert.assertEquals(coll, translator.translateFromJavabuf(result));
    }

    // Client: null
    // Server: ArrayList
    @Test
    public void testListS1RawTypeVariable() {
        java.util.List<dev.resteasy.grpc.lists.sets.S1> coll = new java.util.ArrayList<dev.resteasy.grpc.lists.sets.S1>();
        coll.add(new S1());
        Message m = translator.translateToJavabuf(coll, null);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJavaUtilArrayListField((java_util___ArrayList) m).build();
        GeneralReturnMessage response = blockingStubPlaintext.arrayListTest5(gem);
        java_util___ArrayList result = response.getJavaUtilArrayListField();
        Assert.assertEquals(coll, translator.translateFromJavabuf(result));
    }

    // Client: null
    // Server: ArrayList
    @Test
    public void testArrayListObjectRaw() {
        java.util.List<java.lang.Object> coll = new java.util.ArrayList<java.lang.Object>();
        coll.add(new S1());
        Message m = translator.translateToJavabuf(coll, null);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJavaUtilArrayListField((java_util___ArrayList) m).build();
        GeneralReturnMessage response = blockingStubPlaintext.arrayListTest5(gem);
        java_util___ArrayList result = response.getJavaUtilArrayListField();
        Assert.assertEquals(coll, translator.translateFromJavabuf(result));
    }

    // Client: java.util.ArrayList<java.lang.Object>
    // Server: ArrayList<Object>
    @Test
    public void testArraylistObjectGeneric() {
        List<Object> collection = new ArrayList<Object>();
        collection.add(new S1());
        GenericType<java.util.ArrayList<java.lang.Object>> type = new GenericType<java.util.ArrayList<java.lang.Object>>() {
        };
        Message m = translator.translateToJavabuf(collection, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJavaUtilArrayList38Field((java_util___ArrayList38) m).build();
        GeneralReturnMessage response = blockingStubPlaintext.arrayListTest4(gem);
        java_util___ArrayList38 result = response.getJavaUtilArrayList38Field();
        Assert.assertTrue(CollectionEquals.equals(collection, translator.translateFromJavabuf(result)));
    }

    // Client: java.util.HashSet<java.lang.Object>
    // Server: HashSet<T>
    @Test
    public void testHashSetStringGenericTypeVariable() {
        java.util.HashSet<java.lang.String> set = new java.util.HashSet<java.lang.String>();
        set.add("abc");
        GenericType<java.util.HashSet<java.lang.Object>> type = new GenericType<java.util.HashSet<java.lang.Object>>() {
        };
        Message m = translator.translateToJavabuf(set, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJavaUtilHashSet4Field((java_util___HashSet4) m).build();
        GeneralReturnMessage response = blockingStubPlaintext.hashSetTest1(gem);
        java_util___HashSet4 result = response.getJavaUtilHashSet4Field();
        Assert.assertTrue(CollectionEquals.equals(set, translator.translateFromJavabuf(result)));
    }

    // Client: java.util.HashSet<java.lang.Object>
    // Server: HashSet<?>
    @Test
    public void testHashSetStringGenericTypeWildcard() {
        java.util.HashSet<java.lang.String> set = new java.util.HashSet<java.lang.String>();
        set.add("abc");
        GenericType<java.util.HashSet<java.lang.Object>> type = new GenericType<java.util.HashSet<java.lang.Object>>() {
        };
        Message m = translator.translateToJavabuf(set, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJavaUtilHashSet4Field((java_util___HashSet4) m).build();
        GeneralReturnMessage response = blockingStubPlaintext.hashSetTest2(gem);
        java_util___HashSet4 result = response.getJavaUtilHashSet4Field();
        Assert.assertTrue(CollectionEquals.equals(set, translator.translateFromJavabuf(result)));
    }

    // Client: java.util.HashSet<java.lang.String>
    // Server: HashSet<String>
    @Test
    public void testHashSetStringGenericTypeString() {
        java.util.HashSet<java.lang.String> set = new java.util.HashSet<java.lang.String>();
        set.add("abc");
        GenericType<java.util.HashSet<java.lang.String>> type = new GenericType<java.util.HashSet<java.lang.String>>() {
        };
        Message m = translator.translateToJavabuf(set, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJavaUtilHashSet5Field((java_util___HashSet5) m).build();
        GeneralReturnMessage response = blockingStubPlaintext.hashSetTest3(gem);
        java_util___HashSet5 result = response.getJavaUtilHashSet5Field();
        Assert.assertTrue(CollectionEquals.equals(set, translator.translateFromJavabuf(result)));
    }

    // Client: java.util.HashSet<java.lang.Object>
    // Server: HashSet<Object>
    @Test
    public void testHashSetStringGenericTypeObject() {
        java.util.HashSet<java.lang.String> set = new java.util.HashSet<java.lang.String>();
        set.add("abc");
        GenericType<java.util.HashSet<java.lang.Object>> type = new GenericType<java.util.HashSet<java.lang.Object>>() {
        };
        Message m = translator.translateToJavabuf(set, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJavaUtilHashSet4Field((java_util___HashSet4) m).build();
        GeneralReturnMessage response = blockingStubPlaintext.hashSetTest4(gem);
        java_util___HashSet4 result = response.getJavaUtilHashSet4Field();
        Assert.assertTrue(CollectionEquals.equals(set, translator.translateFromJavabuf(result)));
    }

    // Client: java.util.HashSet
    // Server: HashSet
    @Test
    public void testHashSetStringGenericTypeRaw() {
        java.util.HashSet<java.lang.String> set = new java.util.HashSet<java.lang.String>();
        set.add("abc");
        GenericType<java.util.HashSet> type = new GenericType<java.util.HashSet>() {
        };
        Message m = translator.translateToJavabuf(set, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJavaUtilHashSetField((java_util___HashSet) m).build();
        GeneralReturnMessage response = blockingStubPlaintext.hashSetTest5(gem);
        java_util___HashSet result = response.getJavaUtilHashSetField();
        Assert.assertTrue(CollectionEquals.equals(set, translator.translateFromJavabuf(result)));
    }

    // Client: null
    // Server: HashSet
    @Test
    public void testHashSetStringRaw() {
        java.util.HashSet<java.lang.String> set = new java.util.HashSet<java.lang.String>();
        set.add("abc");
        Message m = translator.translateToJavabuf(set, null);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJavaUtilHashSetField((java_util___HashSet) m).build();
        GeneralReturnMessage response = blockingStubPlaintext.hashSetTest5(gem);
        java_util___HashSet result = response.getJavaUtilHashSetField();
        Assert.assertTrue(CollectionEquals.equals(set, translator.translateFromJavabuf(result)));
    }

    // Client: java.util.Set<java.lang.Object>
    // Server: Set<T>
    @Test
    public void testSetStringGenericTypeVariable() {
        java.util.Set<java.lang.String> set = new java.util.HashSet<java.lang.String>();
        set.add("abc");
        GenericType<java.util.Set<java.lang.Object>> type = new GenericType<java.util.Set<java.lang.Object>>() {
        };
        Message m = translator.translateToJavabuf(set, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJavaUtilSet2Field((java_util___Set2) m).build();
        GeneralReturnMessage response = blockingStubPlaintext.setTest1(gem);
        java_util___Set2 result = response.getJavaUtilSet2Field();
        Assert.assertTrue(CollectionEquals.equals(set, translator.translateFromJavabuf(result)));
    }

    // Client: java.util.Set<java.lang.Object>
    // Server: Set<?>
    @Test
    public void testSetStringGenericTypeWildcard() {
        java.util.Set<java.lang.String> set = new java.util.HashSet<java.lang.String>();
        set.add("abc");
        GenericType<java.util.Set<java.lang.Object>> type = new GenericType<java.util.Set<java.lang.Object>>() {
        };
        Message m = translator.translateToJavabuf(set, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJavaUtilSet2Field((java_util___Set2) m).build();
        GeneralReturnMessage response = blockingStubPlaintext.setTest2(gem);
        java_util___Set2 result = response.getJavaUtilSet2Field();
        Assert.assertTrue(CollectionEquals.equals(set, translator.translateFromJavabuf(result)));
    }

    // Client: java.util.Set<java.lang.String>
    // Server: Set<String>
    @Test
    public void testSetStringGenericTypeString() {
        java.util.Set<java.lang.String> set = new java.util.HashSet<java.lang.String>();
        set.add("abc");
        GenericType<java.util.Set<java.lang.String>> type = new GenericType<java.util.Set<java.lang.String>>() {
        };
        Message m = translator.translateToJavabuf(set, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJavaUtilSet3Field((java_util___Set3) m).build();
        GeneralReturnMessage response = blockingStubPlaintext.setTest3(gem);
        java_util___Set3 result = response.getJavaUtilSet3Field();
        Assert.assertTrue(CollectionEquals.equals(set, translator.translateFromJavabuf(result)));
    }

    // Client: java.util.Set<java.lang.Object>
    // Server: Set<Object>
    @Test
    public void testSetStringGenericTypeObject() {
        java.util.Set<java.lang.String> set = new java.util.HashSet<java.lang.String>();
        set.add("abc");
        GenericType<java.util.Set<java.lang.Object>> type = new GenericType<java.util.Set<java.lang.Object>>() {
        };
        Message m = translator.translateToJavabuf(set, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJavaUtilSet2Field((java_util___Set2) m).build();
        GeneralReturnMessage response = blockingStubPlaintext.setTest4(gem);
        java_util___Set2 result = response.getJavaUtilSet2Field();
        Assert.assertTrue(CollectionEquals.equals(set, translator.translateFromJavabuf(result)));
    }

    // Client: java.util.Set<java.lang.String>
    // Server: Set<String>
    @Test
    public void testSetStringGenericType() {
        java.util.Set<java.lang.String> set = new java.util.HashSet<java.lang.String>();
        set.add("abc");
        GenericType<java.util.Set<java.lang.String>> type = new GenericType<java.util.Set<java.lang.String>>() {
        };
        Message m = translator.translateToJavabuf(set, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJavaUtilSet3Field((java_util___Set3) m).build();
        GeneralReturnMessage response = blockingStubPlaintext.setTest3(gem);
        java_util___Set3 result = response.getJavaUtilSet3Field();
        Assert.assertTrue(CollectionEquals.equals(set, translator.translateFromJavabuf(result)));
    }

    // Client: null
    // Server: HashSet
    @Test
    public void testSetStringRaw() {
        java.util.HashSet<java.lang.String> set = new java.util.HashSet<java.lang.String>();
        set.add("abc");
        Message m = translator.translateToJavabuf(set, null);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJavaUtilHashSetField((java_util___HashSet) m).build();
        GeneralReturnMessage response = blockingStubPlaintext.hashSetTest5(gem);
        java_util___HashSet result = response.getJavaUtilHashSetField();
        Assert.assertTrue(CollectionEquals.equals(set, translator.translateFromJavabuf(result)));
    }

    // Client: java.util.ArrayList<java.lang.Object>
    // Server: ArrayList<Object>
    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void testListMiscArrayListObject() {
        Collection collection = new ArrayList<Object>();
        collection.add(Integer.valueOf(3));
        collection.add("abc");
        S1 s1 = new S1();
        s1.add("xyz");
        collection.add(s1);
        GenericType<java.util.ArrayList<java.lang.Object>> type = new GenericType<java.util.ArrayList<java.lang.Object>>() {
        };
        Message m = translator.translateToJavabuf(collection, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJavaUtilArrayList38Field((java_util___ArrayList38) m).build();
        GeneralReturnMessage response = blockingStubPlaintext.arrayListTest4(gem);
        java_util___ArrayList38 result = response.getJavaUtilArrayList38Field();
        Assert.assertTrue(CollectionEquals.equals(collection, translator.translateFromJavabuf(result)));
    }

    // Client: null
    // Server: ArrayList<Object>
    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void testListMiscArrayListRaw() {
        Collection collection = new ArrayList<Object>();
        collection.add(Integer.valueOf(3));
        collection.add("abc");
        S1 s1 = new S1();
        s1.add("xyz");
        collection.add(s1);
        Message m = translator.translateToJavabuf(collection, null);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJavaUtilArrayListField((java_util___ArrayList) m).build();
        GeneralReturnMessage response = blockingStubPlaintext.arrayListTest5(gem);
        java_util___ArrayList result = response.getJavaUtilArrayListField();
        Assert.assertTrue(CollectionEquals.equals(collection, translator.translateFromJavabuf(result)));
    }

    //=======================================================================================
    //
    //       Doubly nested layers: e.g. sets of sets of objects, lists of sets of objects
    //
    //=======================================================================================

    // Client: java.util.Set<java.util.HashSet<java.lang.String>>
    // Server: Set<HashSet<String>>
    @Test
    public void testSetHashSetString() {
        HashSet<String> s1 = new HashSet<String>();
        s1.add("pqr");
        Set<HashSet<String>> s2 = new HashSet<HashSet<String>>();
        s2.add(s1);
        GenericType<java.util.Set<java.util.HashSet<java.lang.String>>> type = new GenericType<java.util.Set<java.util.HashSet<java.lang.String>>>() {
        };
        Message m = translator.translateToJavabuf(s2, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJavaUtilSet10Field((java_util___Set10) m).build();
        GeneralReturnMessage response = blockingStubPlaintext.setHashsetTest3(gem);
        java_util___Set10 result = response.getJavaUtilSet10Field();
        Assert.assertTrue(CollectionEquals.equals(s2, translator.translateFromJavabuf(result)));
    }

    // Client: null
    // Server: HashSet
    @Test
    public void testSetHashSetStringRaw() {
        HashSet<String> s1 = new HashSet<String>();
        s1.add("pqr");
        Set<HashSet<String>> s2 = new HashSet<HashSet<String>>();
        s2.add(s1);
        Message m = translator.translateToJavabuf(s2, null);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJavaUtilHashSetField((java_util___HashSet) m).build();
        GeneralReturnMessage response = blockingStubPlaintext.hashSetTest5(gem);
        java_util___HashSet result = response.getJavaUtilHashSetField();
        Assert.assertTrue(CollectionEquals.equals(s2, translator.translateFromJavabuf(result)));
    }

    // Client: null
    // Server: HashSet
    @Test
    public void testSetHashSetIntegerRaw() {
        HashSet<Integer> s1 = new HashSet<Integer>();
        s1.add(Integer.valueOf(7));
        Set<HashSet<Integer>> s2 = new HashSet<HashSet<Integer>>();
        s2.add(s1);
        Message m = translator.translateToJavabuf(s2, null);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJavaUtilHashSetField((java_util___HashSet) m).build();
        GeneralReturnMessage response = blockingStubPlaintext.hashSetTest5(gem);
        java_util___HashSet result = response.getJavaUtilHashSetField();
        Assert.assertTrue(CollectionEquals.equals(s2, translator.translateFromJavabuf(result)));
    }

    // Client: java.util.List<java.util.List<java.lang.String>>
    // Server: List<List<String>>
    @Test
    public void testListListString() {
        List<String> l1 = new ArrayList<String>();
        l1.add("abc");
        List<List<String>> l2 = new ArrayList<List<String>>();
        l2.add(l1);
        CC1JavabufTranslator translator = new CC1JavabufTranslator();
        GenericType<java.util.List<java.util.List<java.lang.String>>> type = new GenericType<java.util.List<java.util.List<java.lang.String>>>() {
        };
        Message m = translator.translateToJavabuf(l2, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJavaUtilList43Field((java_util___List43) m).build();
        GeneralReturnMessage response = blockingStubPlaintext.listListTest3(gem);
        java_util___List43 result = response.getJavaUtilList43Field();
        Assert.assertTrue(CollectionEquals.equals(l2, translator.translateFromJavabuf(result)));
    }

    // Client: java.util.ArrayList<java.util.List<java.lang.String>>
    // Server: ArrayList<List<String>>
    @Test
    public void testArrayListListString() {
        List<String> l1 = new ArrayList<String>();
        l1.add("abc");
        ArrayList<List<String>> l2 = new ArrayList<List<String>>();
        l2.add(l1);
        GenericType<java.util.ArrayList<java.util.List<java.lang.String>>> type = new GenericType<java.util.ArrayList<java.util.List<java.lang.String>>>() {
        };
        Message m = translator.translateToJavabuf(l2, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJavaUtilArrayList49Field((java_util___ArrayList49) m).build();
        GeneralReturnMessage response = blockingStubPlaintext.arraylistListTest3(gem);
        java_util___ArrayList49 result = response.getJavaUtilArrayList49Field();
        Assert.assertTrue(CollectionEquals.equals(l2, translator.translateFromJavabuf(result)));
    }

    // Client: ArrayList<java.util.ArrayList<java.lang.String>>
    // Server: ArrayList<ArrayList<String>>
    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void testArrayListArrayListStringGeneric() {
        Collection collection = new ArrayList<java.util.ArrayList<java.lang.String>>();
        ArrayList<String> list = new ArrayList<String>();
        list.add("abc");
        list.add("xyz");
        collection.add(list);
        //        doTest(collection, type);
        GenericType type = new GenericType<ArrayList<java.util.ArrayList<java.lang.String>>>() {
        };
        Message m = translator.translateToJavabuf(collection, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJavaUtilArrayList52Field((java_util___ArrayList52) m).build();
        GeneralReturnMessage response = blockingStubPlaintext.arraylistArraylistTest3(gem);
        java_util___ArrayList52 result = response.getJavaUtilArrayList52Field();
        Assert.assertTrue(CollectionEquals.equals(collection, translator.translateFromJavabuf(result)));
    }

    // Client: null
    // Server: ArrayList
    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void testArrayListArrayListStringRaw() {
        Collection collection = new ArrayList<java.util.ArrayList<java.lang.String>>();
        ArrayList<String> list = new ArrayList<String>();
        list.add("abc");
        list.add("xyz");
        collection.add(list);
        Message m = translator.translateToJavabuf(collection, null);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJavaUtilArrayListField((java_util___ArrayList) m).build();
        GeneralReturnMessage response = blockingStubPlaintext.arrayListTest5(gem);
        java_util___ArrayList result = response.getJavaUtilArrayListField();
        Assert.assertTrue(CollectionEquals.equals(collection, translator.translateFromJavabuf(result)));
    }

    // Client: null
    // Server: ArrayList
    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void testArrayListArrayListIntegerRaw() {
        Collection collection = new ArrayList<java.util.ArrayList<java.lang.Integer>>();
        ArrayList<Integer> list = new ArrayList<Integer>();
        list.add(Integer.valueOf(3));
        list.add(Integer.valueOf(5));
        collection.add(list);
        Message m = translator.translateToJavabuf(collection, null);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJavaUtilArrayListField((java_util___ArrayList) m).build();
        GeneralReturnMessage response = blockingStubPlaintext.arrayListTest5(gem);
        java_util___ArrayList result = response.getJavaUtilArrayListField();
        Assert.assertTrue(CollectionEquals.equals(collection, translator.translateFromJavabuf(result)));
    }

    // Client: java.util.ArrayList<java.util.HashSet<java.lang.Object>>
    // Server: ArrayList<HashSet<Object>>
    @Test
    public void testArraylistHashSetObject() {
        ArrayList<HashSet<Object>> collection = new ArrayList<HashSet<Object>>();
        HashSet<Object> set = new HashSet<Object>();
        set.add(new S1());
        collection.add(set);
        GenericType<java.util.ArrayList<java.util.HashSet<java.lang.Object>>> type = new GenericType<java.util.ArrayList<java.util.HashSet<java.lang.Object>>>() {
        };
        Message m = translator.translateToJavabuf(collection, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJavaUtilArrayList27Field((java_util___ArrayList27) m).build();
        GeneralReturnMessage response = blockingStubPlaintext.arraylistHashsetTest4(gem);
        java_util___ArrayList27 result = response.getJavaUtilArrayList27Field();
        Assert.assertTrue(CollectionEquals.equals(collection, translator.translateFromJavabuf(result)));
    }

    // Client: java.util.Set<java.util.ArrayList<java.lang.String>>
    // Server: Set<ArrayList<String>>
    @Test
    public void testSetArrayListString() {
        Set<ArrayList<String>> collection = new HashSet<ArrayList<String>>();
        ArrayList<String> list = new ArrayList<String>();
        list.add("abc");
        list.add("xyz");
        collection.add(list);
        GenericType<java.util.Set<java.util.ArrayList<java.lang.String>>> type = new GenericType<java.util.Set<java.util.ArrayList<java.lang.String>>>() {
        };
        Message m = translator.translateToJavabuf(collection, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJavaUtilSet22Field((java_util___Set22) m).build();
        GeneralReturnMessage response = blockingStubPlaintext.setArraylistTest3(gem);
        java_util___Set22 result = response.getJavaUtilSet22Field();
        Assert.assertTrue(CollectionEquals.equals(collection, translator.translateFromJavabuf(result)));
    }

    //=======================================================================================
    //
    //      Triply nested: e.g. lists of sets of lists of objects
    //
    //=======================================================================================

    // Client: java.util.List<dev.resteasy.grpc.lists.sets.L3<dev.resteasy.grpc.lists.sets.S3<java.util.Set<Object>>>>> type = new GenericType<java.util.List<dev.resteasy.grpc.lists.sets.L3<dev.resteasy.grpc.lists.sets.S3<java.util.Set<Object>>>>>
    // Server: List<L3<S3<Set<T>>>>
    @Test
    public void testListL3S3SetVariable() {
        Set<Object> set = new HashSet<Object>();
        set.add("abc");
        S3<Set<Object>> s3 = new S3<Set<Object>>();
        s3.add(set);
        L3<S3<Set<Object>>> l3 = new L3<S3<Set<Object>>>();
        l3.add(s3);
        List<L3<S3<Set<Object>>>> collection = new ArrayList<L3<S3<Set<Object>>>>();
        collection.add(l3);
        GenericType<java.util.List<dev.resteasy.grpc.lists.sets.L3<dev.resteasy.grpc.lists.sets.S3<java.util.Set<Object>>>>> type = new GenericType<java.util.List<dev.resteasy.grpc.lists.sets.L3<dev.resteasy.grpc.lists.sets.S3<java.util.Set<Object>>>>>() {
        };
        Message m = translator.translateToJavabuf(collection, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJavaUtilList34Field((java_util___List34) m).build();
        GeneralReturnMessage response = blockingStubPlaintext.listL3S3SetTest1(gem);
        java_util___List34 result = response.getJavaUtilList34Field();
        Assert.assertTrue(CollectionEquals.equals(collection, translator.translateFromJavabuf(result)));
    }

    // Client: java.util.List<dev.resteasy.grpc.lists.sets.L3<dev.resteasy.grpc.lists.sets.S3<java.util.Set<Object>>>>> type = new GenericType<java.util.List<dev.resteasy.grpc.lists.sets.L3<dev.resteasy.grpc.lists.sets.S3<java.util.Set<Object>>>>>
    // Server: List<L3<S3<Set<?>>>>
    @Test
    public void testListL3S3SetWildcard() {
        Set<Object> set = new HashSet<Object>();
        set.add("abc");
        S3<Set<Object>> s3 = new S3<Set<Object>>();
        s3.add(set);
        L3<S3<Set<Object>>> l3 = new L3<S3<Set<Object>>>();
        l3.add(s3);
        List<L3<S3<Set<Object>>>> collection = new ArrayList<L3<S3<Set<Object>>>>();
        collection.add(l3);
        GenericType<java.util.List<dev.resteasy.grpc.lists.sets.L3<dev.resteasy.grpc.lists.sets.S3<java.util.Set<Object>>>>> type = new GenericType<java.util.List<dev.resteasy.grpc.lists.sets.L3<dev.resteasy.grpc.lists.sets.S3<java.util.Set<Object>>>>>() {
        };
        Message m = translator.translateToJavabuf(collection, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJavaUtilList34Field((java_util___List34) m).build();
        GeneralReturnMessage response = blockingStubPlaintext.listL3S3SetTest2(gem);
        java_util___List34 result = response.getJavaUtilList34Field();
        Assert.assertTrue(CollectionEquals.equals(collection, translator.translateFromJavabuf(result)));
    }

    // Client: java.util.List<dev.resteasy.grpc.lists.sets.L3<dev.resteasy.grpc.lists.sets.S3<java.util.Set<Object>>>>> type = new GenericType<java.util.List<dev.resteasy.grpc.lists.sets.L3<dev.resteasy.grpc.lists.sets.S3<java.util.Set<String>>>>>
    // Server: List<L3<S3<Set<String>>>>
    @Test
    public void testListL3S3SetString() {
        Set<String> set = new HashSet<String>();
        set.add("abc");
        S3<Set<String>> s3 = new S3<Set<String>>();
        s3.add(set);
        L3<S3<Set<String>>> l3 = new L3<S3<Set<String>>>();
        l3.add(s3);
        List<L3<S3<Set<String>>>> collection = new ArrayList<L3<S3<Set<String>>>>();
        collection.add(l3);
        GenericType<java.util.List<dev.resteasy.grpc.lists.sets.L3<dev.resteasy.grpc.lists.sets.S3<java.util.Set<String>>>>> type = new GenericType<java.util.List<dev.resteasy.grpc.lists.sets.L3<dev.resteasy.grpc.lists.sets.S3<java.util.Set<String>>>>>() {
        };
        Message m = translator.translateToJavabuf(collection, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJavaUtilList35Field((java_util___List35) m).build();
        GeneralReturnMessage response = blockingStubPlaintext.listL3S3SetTest3(gem);
        java_util___List35 result = response.getJavaUtilList35Field();
        Assert.assertTrue(CollectionEquals.equals(collection, translator.translateFromJavabuf(result)));
    }

    // Client: java.util.List<dev.resteasy.grpc.lists.sets.L3<dev.resteasy.grpc.lists.sets.S3<java.util.Set<Object>>>>> type = new GenericType<java.util.List<dev.resteasy.grpc.lists.sets.L3<dev.resteasy.grpc.lists.sets.S3<java.util.Set<Object>>>>>
    // Server: List<L3<S3<Set<?>>>>
    @Test
    public void testListL3S3SetObject() {
        Set<Object> set = new HashSet<Object>();
        set.add("abc");
        S3<Set<Object>> s3 = new S3<Set<Object>>();
        s3.add(set);
        L3<S3<Set<Object>>> l3 = new L3<S3<Set<Object>>>();
        l3.add(s3);
        List<L3<S3<Set<Object>>>> collection = new ArrayList<L3<S3<Set<Object>>>>();
        collection.add(l3);
        GenericType<java.util.List<dev.resteasy.grpc.lists.sets.L3<dev.resteasy.grpc.lists.sets.S3<java.util.Set<Object>>>>> type = new GenericType<java.util.List<dev.resteasy.grpc.lists.sets.L3<dev.resteasy.grpc.lists.sets.S3<java.util.Set<Object>>>>>() {
        };
        Message m = translator.translateToJavabuf(collection, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJavaUtilList34Field((java_util___List34) m).build();
        GeneralReturnMessage response = blockingStubPlaintext.listL3S3SetTest4(gem);
        java_util___List34 result = response.getJavaUtilList34Field();
        Assert.assertTrue(CollectionEquals.equals(collection, translator.translateFromJavabuf(result)));
    }

    // Client: java.util.List<dev.resteasy.grpc.lists.sets.L3<dev.resteasy.grpc.lists.sets.S3<java.util.Set>>>> type = new GenericType<java.util.List<dev.resteasy.grpc.lists.sets.L3<dev.resteasy.grpc.lists.sets.S3<java.util.Set>>>>
    // Server: List<L3<S3<Set>>>
    @SuppressWarnings("rawtypes")
    @Test
    public void testListL3S3SetRaw() {
        Set set = new HashSet();
        set.add("abc");
        S3<Set> s3 = new S3<Set>();
        s3.add(set);
        L3<S3<Set>> l3 = new L3<S3<Set>>();
        l3.add(s3);
        List<L3<S3<Set>>> collection = new ArrayList<L3<S3<Set>>>();
        collection.add(l3);
        GenericType<java.util.List<dev.resteasy.grpc.lists.sets.L3<dev.resteasy.grpc.lists.sets.S3<java.util.Set>>>> type = new GenericType<java.util.List<dev.resteasy.grpc.lists.sets.L3<dev.resteasy.grpc.lists.sets.S3<java.util.Set>>>>() {
        };
        Message m = translator.translateToJavabuf(collection, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJavaUtilList36Field((java_util___List36) m).build();
        GeneralReturnMessage response = blockingStubPlaintext.listL3S3SetTest5(gem);
        java_util___List36 result = response.getJavaUtilList36Field();
        Assert.assertTrue(CollectionEquals.equals(collection, translator.translateFromJavabuf(result)));
    }

    //=======================================================================================
    //
    //      Classes with a variety set and list fields
    //
    //=======================================================================================

    @Test
    public void testD1Integer() throws Exception {
        D1<Integer> d1 = new D1<Integer>();
        GenericType<dev.resteasy.grpc.lists.sets.D1<Integer>> type = new GenericType<dev.resteasy.grpc.lists.sets.D1<Integer>>() {
        };
        Message m = translator.translateToJavabuf(d1, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setDevResteasyGrpcListsSetsD1Field((dev_resteasy_grpc_lists_sets___D1) m).build();
        GeneralReturnMessage response = blockingStubPlaintext.d1Integer(gem);
        dev_resteasy_grpc_lists_sets___D1 result = response.getDevResteasyGrpcListsSetsD1Field();
        Assert.assertEquals(d1, translator.translateFromJavabuf(result));
    }

    @Test
    public void testD1Raw() throws Exception {
        D1 d1 = new D1();
        Message m = translator.translateToJavabuf(d1, null);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setDevResteasyGrpcListsSetsD1Field((dev_resteasy_grpc_lists_sets___D1) m).build();
        GeneralReturnMessage response = blockingStubPlaintext.d1Raw(gem);
        dev_resteasy_grpc_lists_sets___D1 result = response.getDevResteasyGrpcListsSetsD1Field();
        Assert.assertEquals(d1, translator.translateFromJavabuf(result));
    }

    @Test
    public void testD3() {
        D3 d3 = new D3();
        Message m = translator.translateToJavabuf(d3, null);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setDevResteasyGrpcListsSetsD3Field((dev_resteasy_grpc_lists_sets___D3) m).build();
        GeneralReturnMessage response = blockingStubPlaintext.d3(gem);
        dev_resteasy_grpc_lists_sets___D3 result = response.getDevResteasyGrpcListsSetsD3Field();
        Assert.assertEquals(d3, translator.translateFromJavabuf(result));
    }

    //////////////////////////////////////////////////////////
    public static class CollectionEquals {

        public static boolean equals(Object o1, Object o2) {
            if (!o1.getClass().equals(o2.getClass())) {
                return false;
            }
            if (!Collection.class.isAssignableFrom(o1.getClass()) || !Collection.class.isAssignableFrom(o2.getClass())) {
                return false;
            }
            Object[] o1s = ((Collection) o1).toArray();
            Object[] o2s = ((Collection) o2).toArray();
            if (o1s.length != o2s.length) {
                return false;
            }
            for (int i = 0; i < o1s.length; i++) {
                Object o1s1 = o1s[i];
                Object o2s1 = o2s[i];
                if (Collection.class.isAssignableFrom(o1s1.getClass()) && Collection.class.isAssignableFrom(o2s1.getClass())) {
                    return equals(o1s1, o2s1);
                }
                if ((o1s1 == null && o2s1 != null) || (o1s1 != null && o2s1 == null)) {
                    return false;
                }
                if (o1s1 == null) {
                    continue;
                }
                if (!o1s1.equals(o2s1)) {
                    return false;
                }
            }
            return true;
        }

        public static boolean contains(Object o, Collection<?> c) {
            for (Object oo : c) {
                if (Collection.class.isAssignableFrom(oo.getClass())) {
                    if (equals(o, oo)) {
                        return true;
                    }
                } else {
                    if (o == null) {
                        return oo == null;
                    } else if (o.equals(oo)) {
                        return true;
                    } else {
                        continue;
                    }
                }
            }
            return false;
        }
    }
}
