/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2025 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.resteasy.test.grpc;

import java.io.BufferedReader;
import java.io.File;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

//import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.GenericType;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.wildfly.common.Assert;

import com.google.protobuf.Any;
import com.google.protobuf.Message;

import dev.resteasy.grpc.arrays.ArrayResource;
import dev.resteasy.grpc.arrays.Array_proto;
import dev.resteasy.grpc.bridge.runtime.Utility;
import dev.resteasy.grpc.example.CC1;
import dev.resteasy.grpc.example.CC1JavabufTranslator;
import dev.resteasy.grpc.example.CC1ServiceGrpc;
import dev.resteasy.grpc.example.CC1_Server;
import dev.resteasy.grpc.example.CC1_proto;
import dev.resteasy.grpc.example.CC1_proto.*;
import dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage;
import dev.resteasy.grpc.example.CC1_proto.GeneralReturnMessage;
import dev.resteasy.grpc.example.sub.CC8;
import dev.resteasy.grpc.lists.sets.DD1;
import dev.resteasy.grpc.maps.M1;
import dev.resteasy.grpc.maps.MapResource;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

@SuppressWarnings("deprecation")
@ExtendWith(ArquillianExtension.class)
@RunAsClient
public class GrpcMapTest {

    private static CC1JavabufTranslator translator = new CC1JavabufTranslator();
    private static ManagedChannel channelPlaintext;
    private static CC1ServiceGrpc.CC1ServiceBlockingStub blockingStubPlaintext;
    private static Set<String> entityClasses = new HashSet<String>();
    private static Map<String, Method> GET_MAP = new HashMap<String, Method>();
    private static Map<String, Method> SET_MAP = new HashMap<String, Method>();

    static {
        Class<?> clazz;
        try {
            clazz = Class.forName("dev.resteasy.grpc.example.CC1JavabufTranslator");
            translator = (CC1JavabufTranslator) clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static {
        entityClasses.add("java.util.ArrayList<java.lang.Object>");
        entityClasses.add("java.util.HashSet<java.lang.Object>");
        entityClasses.add("java.util.HashSet<java.lang.String>");
        entityClasses.add("java.util.ArrayList<java.util.List<java.lang.String>>");
        entityClasses.add("java.util.ArrayList<java.util.ArrayList<java.lang.String>>");
        entityClasses.add("java.util.ArrayList<java.util.HashSet<java.lang.Object>>");
    }

    static {
        try {
            Class<?> builder = Class.forName("dev.resteasy.grpc.example.CC1_proto$GeneralEntityMessage$Builder");
            Class<?> response = Class.forName("dev.resteasy.grpc.example.CC1_proto$GeneralReturnMessage");
            final Path file = Path.of(System.getProperty("builddir").replace("\\", "\\\\") + File.separator + "entityTypes");
            try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                String line = reader.readLine();
                while (line != null) {
                    int n = line.indexOf(" ");
                    String l1 = line.substring(0, n);
                    String l2 = line.substring(n + 1);
                    if (entityClasses.contains(l1)) {
                        Class<?> javabufClass = Class.forName(l2);
                        if (l2.contains("$")) {
                            l2 = l2.substring(l2.lastIndexOf('$') + 1);
                        }
                        String methodSuffix = squashToCamel(l2) + "Field";
                        GET_MAP.put(l1, response.getDeclaredMethod("get" + methodSuffix));
                        SET_MAP.put(l1, builder.getDeclaredMethod("set" + methodSuffix, javabufClass));
                    }
                    line = reader.readLine();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static String squashToCamel(String name) {
        StringBuilder sb = new StringBuilder();
        boolean start = true;
        for (int i = 0; i < name.length(); i++) {
            if (name.charAt(i) == '_') {
                start = true;
                continue;
            }
            sb.append(start ? name.substring(i, i + 1).toUpperCase() : name.substring(i, i + 1));
            start = false;
        }
        return sb.toString();
    }

    @Deployment
    public static Archive<?> deploy() throws Exception {
        final var resolver = Maven.resolver()
                .loadPomFromFile("pom.xml");
        Archive<?> ar = ShrinkWrap.create(WebArchive.class, GrpcMapTest.class.getSimpleName() + ".war")
                .addClass(GrpcMapTest.class)
                .addPackage(CC1_Server.class.getPackage())
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
        //                ar.as(ZipExporter.class).exportTo(
        //                        new File("/tmp/maps.war"), true);
        return ar;
    }

    @BeforeAll
    public static void beforeClass() throws Exception {
        accessServletContexts();
        channelPlaintext = ManagedChannelBuilder.forTarget("localhost:9555").usePlaintext().build();
        blockingStubPlaintext = CC1ServiceGrpc.newBlockingStub(channelPlaintext);
    }

    @AfterAll
    public static void afterClass() throws InterruptedException {
        if (channelPlaintext != null) {
            channelPlaintext.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
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

    //=======================================================================================
    //
    //                java.util.Map interface
    //
    //=======================================================================================
    // Client: java.util.Map
    // Server: java.util.Map
    @Test
    public void testMapRaw() throws Exception {
        Map map = new HashMap();
        map.put(new M1("three", Integer.valueOf(3)), new M1("five", Integer.valueOf(5)));
        Message m = translator.translateToJavabuf(map);
        Any any = Any.pack(m);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setAnyField(any).build();
        GeneralReturnMessage response = blockingStubPlaintext.mapNoType(gem);
        any = response.getAnyField();
        Message result = Utility.unpack(any, translator);
        Map mmm = (Map) translator.translateFromJavabuf(result);
        Entry entry = (Entry) mmm.entrySet().iterator().next();
        Assert.assertTrue(new M1("three", Integer.valueOf(3)).toString().equals(entry.getKey().toString()) &&
                new M1("five", Integer.valueOf(5)).toString().equals(entry.getValue().toString()));
    }

    // Client: java.util.Map<java.lang.Integer, java.lang.Integer>
    // Server: Map<K, V>
    @Test
    public void testMapVariableVariable() throws Exception {
        java.util.Map<Integer, Integer> map = new java.util.HashMap<Integer, Integer>();
        map.put(Integer.valueOf(11), Integer.valueOf(13));
        GenericType<java.util.Map<Integer, Integer>> type = new GenericType<java.util.Map<Integer, Integer>>() {
        };
        Message m = translator.translateToJavabuf(map, type);
        Any any = Any.pack(m);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setAnyField(any).build();
        GeneralReturnMessage response = blockingStubPlaintext.mapVarVar(gem);
        any = response.getAnyField();
        Message result = Utility.unpack(any, translator);
        Assertions.assertEquals(map, translator.translateFromJavabuf(result));
    }

    // Client: java.util.Map<java.lang.Integer, java.lang.Integer>
    // Server: Map<K, V>
    @Test
    public void testMapVariableVariableNull() throws Exception {
        java.util.Map<Integer, Integer> map = new java.util.HashMap<Integer, Integer>();
        map.put(Integer.valueOf(11), Integer.valueOf(13));
        Message m = translator.translateToJavabuf(map, null);
        Any any = Any.pack(m);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setAnyField(any).build();
        GeneralReturnMessage response = blockingStubPlaintext.mapVarVar(gem);
        any = response.getAnyField();
        Message result = Utility.unpack(any, translator);
        Assertions.assertEquals(map, translator.translateFromJavabuf(result));
    }

    // Client: java.util.Map<java.lang.Integer, java.lang.Integer>
    // Server: Map<?, ?>
    @Test
    public void testMapWildcardWildcard() throws Exception {
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        map.put(Integer.valueOf(17), Integer.valueOf(19));
        GenericType<Map<Integer, Integer>> type = new GenericType<Map<Integer, Integer>>() {
        };
        Message m = translator.translateToJavabuf(map, type);
        Any any = Any.pack(m);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setAnyField(any).build();
        GeneralReturnMessage response = blockingStubPlaintext.mapWildWild(gem);
        any = response.getAnyField();
        Message result = Utility.unpack(any, translator);
        Assertions.assertEquals(map, translator.translateFromJavabuf(result));
    }

    // Client: java.util.Map<java.lang.String, java.lang.String>
    // Server: Map<java.lang.String, java.lang.String>
    @Test
    public void testMapStringStringGeneric() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        map.put("from", "to");
        GenericType<Map<String, String>> type = new GenericType<Map<String, String>>() {
        };
        Message m = translator.translateToJavabuf(map, type);
        Any any = Any.pack(m);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setAnyField(any).build();
        GeneralReturnMessage response = blockingStubPlaintext.mapStringString(gem);
        any = response.getAnyField();
        Message result = Utility.unpack(any, translator);
        Assertions.assertEquals(map, translator.translateFromJavabuf(result));
    }

    // Client: java.util.Map<java.lang.String, java.lang.Integer>
    // Server: Map<java.lang.String, java.lang.Integer>
    @Test
    public void testMapStringInteger() throws Exception {
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("23", Integer.valueOf(29));
        GenericType<Map<String, Integer>> type = new GenericType<Map<String, Integer>>() {
        };
        Message m = translator.translateToJavabuf(map, type);
        Any any = Any.pack(m);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setAnyField(any).build();
        GeneralReturnMessage response = blockingStubPlaintext.mapStringInt(gem);
        any = response.getAnyField();
        Message result = Utility.unpack(any, translator);
        Assertions.assertEquals(map, translator.translateFromJavabuf(result));
    }

    // Client: java.util.Map<java.lang.Object, java.lang.Object>
    // Server: Map<java.lang.Object, java.lang.Object>
    @Test
    public void testMapObjectObject() throws Exception {
        Map<Object, Object> map = new HashMap<Object, Object>();
        map.put(new M1("31", 37), new M1("39", 41));
        GenericType<Map<Object, Object>> type = new GenericType<Map<Object, Object>>() {
        };
        Message m = translator.translateToJavabuf(map, type);
        Any any = Any.pack(m);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setAnyField(any).build();
        GeneralReturnMessage response = blockingStubPlaintext.mapObjObj(gem);
        any = response.getAnyField();
        Message result = Utility.unpack(any, translator);
        Assertions.assertEquals(map, translator.translateFromJavabuf(result));
    }

    // Client: java.util.Map<List<Integer>, Set<Integer>>
    // Server: java.util.Map<List<Integer>, Set<Integer>>
    @Test
    public void testMapListSet() throws Exception {
        Map<List<Integer>, Set<Integer>> map = new HashMap<List<Integer>, Set<Integer>>();
        List<Integer> list = new ArrayList<Integer>();
        list.add(43);
        Set<Integer> set = new HashSet<Integer>();
        set.add(47);
        map.put(list, set);

        GenericType<Map<List<Integer>, Set<Integer>>> type = new GenericType<Map<List<Integer>, Set<Integer>>>() {
        };
        Message m = translator.translateToJavabuf(map, type);
        Any any = Any.pack(m);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setAnyField(any).build();
        GeneralReturnMessage response = blockingStubPlaintext.mapListSet(gem);
        any = response.getAnyField();
        Message result = Utility.unpack(any, translator);
        Assertions.assertEquals(map, translator.translateFromJavabuf(result));
    }
    /*
     * @Path("map/map/map")
     *
     * @POST
     * public Map<Map<String, String>, Map<Integer, Integer>> mapMapStringMapInt(
     * Map<Map<String, String>, Map<Integer, Integer>> m) {
     * return m;
     * }
     */

    // Client: Map<Map<String, String>, Map<Integer, Integer>>
    // Server: Map<Map<String, String>, Map<Integer, Integer>>
    @Test
    public void testMapMapMap() throws Exception {
        Map<Map<String, String>, Map<Integer, Integer>> map = new HashMap<Map<String, String>, Map<Integer, Integer>>();
        Map<String, String> smap = new HashMap<String, String>();
        smap.put("abc", "xyz");
        Map<Integer, Integer> imap = new HashMap<Integer, Integer>();
        imap.put(53, 57);
        map.put(smap, imap);
        GenericType<Map<Map<String, String>, Map<Integer, Integer>>> type = new GenericType<Map<Map<String, String>, Map<Integer, Integer>>>() {
        };
        Message m = translator.translateToJavabuf(map, type);
        Any any = Any.pack(m);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setAnyField(any).build();
        GeneralReturnMessage response = blockingStubPlaintext.mapMapStringMapInt(gem);
        any = response.getAnyField();
        Message result = Utility.unpack(any, translator);
        Assertions.assertEquals(map, translator.translateFromJavabuf(result));
    }

    // Client: Map<ArrayList, HashSet>
    // Server: Map<ArrayList, HashSet>
    @Test
    public void testMapArraylistHashset() throws Exception {
        Map<ArrayList, HashSet> map = new HashMap<ArrayList, HashSet>();
        ArrayList<String> list = new ArrayList<String>();
        list.add("abc");
        HashSet<Integer> set = new HashSet<Integer>();
        set.add(59);
        map.put(list, set);
        GenericType<Map<ArrayList, HashSet>> type = new GenericType<Map<ArrayList, HashSet>>() {
        };
        Message m = translator.translateToJavabuf(map, type);
        Any any = Any.pack(m);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setAnyField(any).build();
        GeneralReturnMessage response = blockingStubPlaintext.mapArraylistHashset(gem);
        any = response.getAnyField();
        Message result = Utility.unpack(any, translator);
        Assertions.assertEquals(map, translator.translateFromJavabuf(result));
    }

    // Client: HashMap<M1, M1>
    // Server: HashMap<M1, M1>
    @Test
    public void testMapM1M1() throws Exception {
        HashMap<M1, M1> map = new HashMap<M1, M1>();
        M1 m1a = new M1("61", 67);
        M1 m1b = new M1("71", 73);
        map.put(m1a, m1b);
        GenericType<Map<M1, M1>> type = new GenericType<Map<M1, M1>>() {
        };
        Message m = translator.translateToJavabuf(map, type);
        Any any = Any.pack(m);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setAnyField(any).build();
        GeneralReturnMessage response = blockingStubPlaintext.mapM1M1(gem);
        any = response.getAnyField();
        Message result = Utility.unpack(any, translator);
        Assertions.assertEquals(map, translator.translateFromJavabuf(result));
    }

    //=======================================================================================
    //
    //                java.util.HashMap class
    //
    //=======================================================================================
    // Client: java.util.HashMap
    // Server: java.util.HashMap
    @Test
    public void testHashMapRaw() throws Exception {
        HashMap map = new HashMap();
        map.put(new M1("three", Integer.valueOf(3)), new M1("five", Integer.valueOf(5)));
        java_util___HashMap m = (java_util___HashMap) translator.translateToJavabuf(map);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJavaUtilHashMapField(m).build();
        GeneralReturnMessage response = blockingStubPlaintext.hashmapNoType(gem);
        java_util___HashMap result = response.getJavaUtilHashMapField();
        Map mmm = (Map) translator.translateFromJavabuf(result);
        Entry entry = (Entry) mmm.entrySet().iterator().next();
        Assert.assertTrue(new M1("three", Integer.valueOf(3)).toString().equals(entry.getKey().toString()) &&
                new M1("five", Integer.valueOf(5)).toString().equals(entry.getValue().toString()));
    }

    // Client: java.util.HashMap<java.lang.Object, java.lang.Object>
    // Server: HashMap<K, V>
    @Test
    public void testHashmapVariableVariable() throws Exception {
        java.util.HashMap<Object, Object> map = new java.util.HashMap<Object, Object>();
        map.put(Integer.valueOf(11), Integer.valueOf(13));
        GenericType<java.util.HashMap<Object, Object>> type = new GenericType<java.util.HashMap<Object, Object>>() {
        };
        java_util___HashMap2 m = (java_util___HashMap2) translator.translateToJavabuf(map, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJavaUtilHashMap2Field(m).build();
        GeneralReturnMessage response = blockingStubPlaintext.hashmapVarVar(gem);
        Message result = response.getJavaUtilHashMap2Field();
        Assertions.assertEquals(map, translator.translateFromJavabuf(result));
    }

    // Client: java.util.HashMap
    // Server: HashMap<K, V> (returns java.util.HashMap<java.lang.Object, java.lang.Object>)
    @Test
    public void testHashmapVariableVariableNull() throws Exception {
        HashMap map = new java.util.HashMap<Object, Object>();
        map.put(Integer.valueOf(11), Integer.valueOf(13));
        java_util___HashMap m = (java_util___HashMap) translator.translateToJavabuf(map, null);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJavaUtilHashMapField(m).build();
        GeneralReturnMessage response = blockingStubPlaintext.hashmapNoType(gem);
        Message result = response.getJavaUtilHashMapField();
        Assertions.assertEquals(map, translator.translateFromJavabuf(result));
    }

    // Client: java.util.HashMap<java.lang.Object, java.lang.Object>
    // Server: HashMap<?, ?>
    @Test
    public void testHashmapWildcardWildcard() throws Exception {
        HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
        map.put(Integer.valueOf(17), Integer.valueOf(19));
        GenericType<HashMap<Object, Object>> type = new GenericType<HashMap<Object, Object>>() {
        };
        java_util___HashMap2 m = (java_util___HashMap2) translator.translateToJavabuf(map, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJavaUtilHashMap2Field(m).build();
        GeneralReturnMessage response = blockingStubPlaintext.hashmapWildWild(gem);
        Message result = response.getJavaUtilHashMap2Field();
        Assertions.assertEquals(map, translator.translateFromJavabuf(result));
    }

    // Client: java.util.HashMap<java.lang.String, java.lang.String>
    // Server: HashMap<java.lang.String, java.lang.String>
    @Test
    public void testHashmapStringStringGeneric() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        map.put("from", "to");
        GenericType<HashMap<String, String>> type = new GenericType<HashMap<String, String>>() {
        };
        java_util___HashMap3 m = (java_util___HashMap3) translator.translateToJavabuf(map, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJavaUtilHashMap3Field(m).build();
        GeneralReturnMessage response = blockingStubPlaintext.hashmapStringString(gem);
        Message result = response.getJavaUtilHashMap3Field();
        Assertions.assertEquals(map, translator.translateFromJavabuf(result));
    }

    // Client: java.util.HashMap<java.lang.String, java.lang.Integer>
    // Server: HashMap<java.lang.String, java.lang.Integer>
    @Test
    public void testHashmapStringInteger() throws Exception {
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        map.put("23", Integer.valueOf(29));
        GenericType<HashMap<String, Integer>> type = new GenericType<HashMap<String, Integer>>() {
        };
        java_util___HashMap4 m = (java_util___HashMap4) translator.translateToJavabuf(map, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJavaUtilHashMap4Field(m).build();
        GeneralReturnMessage response = blockingStubPlaintext.hashmapStringInt(gem);
        Message result = response.getJavaUtilHashMap4Field();
        Assertions.assertEquals(map, translator.translateFromJavabuf(result));
    }

    // Client: java.util.HashMap<java.lang.Object, java.lang.Object>
    // Server: HashMap<java.lang.Object, java.lang.Object>
    @Test
    public void testHashmapObjectObject() throws Exception {
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put(new M1("31", 37), new M1("39", 41));
        GenericType<HashMap<Object, Object>> type = new GenericType<HashMap<Object, Object>>() {
        };
        java_util___HashMap2 m = (java_util___HashMap2) translator.translateToJavabuf(map, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJavaUtilHashMap2Field(m).build();
        GeneralReturnMessage response = blockingStubPlaintext.hashmapObjObj(gem);
        Message result = response.getJavaUtilHashMap2Field();
        Assertions.assertEquals(map, translator.translateFromJavabuf(result));
    }

    // Client: java.util.HashMap<List<Integer>, Set<Integer>>
    // Server: java.util.HashMap<List<Integer>, Set<Integer>>
    @Test
    public void testHashmapListSet() throws Exception {
        HashMap<List<Integer>, Set<Integer>> map = new HashMap<List<Integer>, Set<Integer>>();
        List<Integer> list = new ArrayList<Integer>();
        list.add(43);
        Set<Integer> set = new HashSet<Integer>();
        set.add(47);
        map.put(list, set);
        GenericType<HashMap<List<Integer>, Set<Integer>>> type = new GenericType<HashMap<List<Integer>, Set<Integer>>>() {
        };
        java_util___HashMap5 m = (java_util___HashMap5) translator.translateToJavabuf(map, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJavaUtilHashMap5Field(m).build();
        GeneralReturnMessage response = blockingStubPlaintext.hashmapListSet(gem);
        Message result = response.getJavaUtilHashMap5Field();
        Assertions.assertEquals(map, translator.translateFromJavabuf(result));
    }

    // Client: HashMap<HashMap<String, String>, HashMap<Integer, Integer>>
    // Server: HashMap<HashMap<String, String>, HashMap<Integer, Integer>>
    @Test
    public void testHashmapMapMap() throws Exception {
        HashMap<HashMap<String, String>, HashMap<Integer, Integer>> map = new HashMap<HashMap<String, String>, HashMap<Integer, Integer>>();
        HashMap<String, String> smap = new HashMap<String, String>();
        smap.put("abc", "xyz");
        HashMap<Integer, Integer> imap = new HashMap<Integer, Integer>();
        imap.put(53, 57);
        map.put(smap, imap);
        GenericType<HashMap<HashMap<String, String>, HashMap<Integer, Integer>>> type = new GenericType<HashMap<HashMap<String, String>, HashMap<Integer, Integer>>>() {
        };
        java_util___HashMap7 m = (java_util___HashMap7) translator.translateToJavabuf(map, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJavaUtilHashMap7Field(m).build();
        GeneralReturnMessage response = blockingStubPlaintext.hashmapHashmapStringHashmapInt(gem);
        Message result = response.getJavaUtilHashMap7Field();
        Assertions.assertEquals(map, translator.translateFromJavabuf(result));
    }

    // Client: HashMap<ArrayList, HashSet>
    // Server: HashMap<ArrayList, HashSet>
    @Test
    public void testHashmapArraylistHashset() throws Exception {
        HashMap<ArrayList, HashSet> map = new HashMap<ArrayList, HashSet>();
        ArrayList<String> list = new ArrayList<String>();
        list.add("abc");
        HashSet<Integer> set = new HashSet<Integer>();
        set.add(59);
        map.put(list, set);
        GenericType<HashMap<ArrayList, HashSet>> type = new GenericType<HashMap<ArrayList, HashSet>>() {
        };
        java_util___HashMap6 m = (java_util___HashMap6) translator.translateToJavabuf(map, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJavaUtilHashMap6Field(m).build();
        GeneralReturnMessage response = blockingStubPlaintext.hashmapArraylistHashset(gem);
        Message result = response.getJavaUtilHashMap6Field();
        Assertions.assertEquals(map, translator.translateFromJavabuf(result));
    }

    // Client: Map<M1, M1>
    // Server: Map<M1, M1>
    @Test
    public void testHashmapM1M1() throws Exception {
        HashMap<M1, M1> map = new HashMap<M1, M1>();
        M1 m1a = new M1("61", 67);
        M1 m1b = new M1("71", 73);
        map.put(m1a, m1b);
        GenericType<HashMap<M1, M1>> type = new GenericType<HashMap<M1, M1>>() {
        };
        java_util___HashMap8 m = (java_util___HashMap8) translator.translateToJavabuf(map, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJavaUtilHashMap8Field(m).build();
        GeneralReturnMessage response = blockingStubPlaintext.hashmapM1M1(gem);
        Message result = response.getJavaUtilHashMap8Field();
        Assertions.assertEquals(map, translator.translateFromJavabuf(result));
    }

    //////////////////////////////////////////////////////////
    public static boolean mapEquals(Object o1, Object o2) {
        if (!(o1 instanceof Map) || (!(o2 instanceof Map))) {
            return false;
        }
        Map m1 = (Map) o1;
        Map m2 = (Map) o2;
        if (m1.size() != m2.size()) {
            return false;
        }
        for (Object key : m1.keySet()) {
            if (!m1.get(key).equals(m2.get(key))) {
                return false;
            }
        }
        return true;
    }
}
