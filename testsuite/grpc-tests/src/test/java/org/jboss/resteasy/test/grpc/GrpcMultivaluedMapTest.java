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

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

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
public class GrpcMultivaluedMapTest {

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
        Archive<?> ar = ShrinkWrap.create(WebArchive.class, GrpcMultivaluedMapTest.class.getSimpleName() + ".war")
                .addClass(GrpcMultivaluedMapTest.class)
                .addPackage(CC1_Server.class.getPackage())
                //                .addPackage(ExampleApp.class.getPackage())
                .addPackage(CC1.class.getPackage())
                .addPackage(CC8.class.getPackage())
                .addPackage(DD1.class.getPackage())
                .addPackage(MapResource.class.getPackage())
                .addAsLibrary(resolver.resolve("dev.resteasy.grpc:grpc-bridge-runtime")
                        .withoutTransitivity()
                        .asSingleFile())
                .addClass(Array_proto.class)
                //                .addPackage(ArrayUtility.class.getPackage())
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsManifestResource("MANIFEST.MF", "MANIFEST.MF")
                .addAsWebInfResource("web.xml");
        //        ar.as(ZipExporter.class).exportTo(
        //                new File("/tmp/maps.war"), true);
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
    //                jakarta.ws.rs.core.MultivaluedMap interface
    //
    //=======================================================================================
    // Client: jakarta.ws.rs.core.MultivaluedMap
    // Server: jakarta.ws.rs.core.MultivaluedMap
    @Test
    public void testMultivaluedMapRaw() throws Exception {
        MultivaluedMap map = new MultivaluedHashMap();
        map.add(new M1("three", Integer.valueOf(3)), new M1("five", Integer.valueOf(5)));
        Message m = translator.translateToJavabuf(map);
        Any any = Any.pack(m);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setAnyField(any).build();
        GeneralReturnMessage response = blockingStubPlaintext.multimapNoType(gem);
        any = response.getAnyField();
        Message result = Utility.unpack(any, translator);
        MultivaluedMap mmm = (MultivaluedMap) translator.translateFromJavabuf(result);
        Entry entry = (Entry) mmm.entrySet().iterator().next();
        Assert.assertTrue(new M1("three", Integer.valueOf(3)).equals(entry.getKey()));
        Assert.assertTrue(new M1("five", Integer.valueOf(5)).equals(((List) entry.getValue()).iterator().next()));
    }

    // Client: jakarta.ws.rs.core.MultivaluedMap<java.lang.Integer, java.lang.Integer>
    // Server: MultivaluedMap<K, V>
    @Test
    public void testMultivaluedMapVariableVariable() throws Exception {
        MultivaluedMap<Integer, Integer> map = new MultivaluedHashMap<Integer, Integer>();
        map.add(Integer.valueOf(11), Integer.valueOf(13));
        GenericType<MultivaluedMap<Integer, Integer>> type = new GenericType<MultivaluedMap<Integer, Integer>>() {
        };
        Message m = translator.translateToJavabuf(map, type);
        Any any = Any.pack(m);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setAnyField(any).build();
        GeneralReturnMessage response = blockingStubPlaintext.multimapVarVar(gem);
        any = response.getAnyField();
        Message result = Utility.unpack(any, translator);
        Assertions.assertEquals(map, translator.translateFromJavabuf(result));
    }

    // Client: jakarta.ws.rs.core.MultivaluedMap<java.lang.Integer, java.lang.Integer>
    // Server: MultivaluedMap<K, V>
    @Test
    public void testMultivaluedMapVariableVariableNull() throws Exception {
        MultivaluedMap<Integer, Integer> map = new MultivaluedHashMap<Integer, Integer>();
        map.add(Integer.valueOf(11), Integer.valueOf(13));
        Message m = translator.translateToJavabuf(map, null);
        Any any = Any.pack(m);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setAnyField(any).build();
        GeneralReturnMessage response = blockingStubPlaintext.multimapVarVar(gem);
        any = response.getAnyField();
        Message result = Utility.unpack(any, translator);
        Assertions.assertEquals(map, translator.translateFromJavabuf(result));
    }

    // Client: jakarta.ws.rs.core.MultivaluedMap<java.lang.Integer, java.lang.Integer>
    // Server: MultivaluedMap<?, ?>
    @Test
    public void testMultivaluedMapWildcardWildcard() throws Exception {
        MultivaluedMap<Integer, Integer> map = new MultivaluedHashMap<Integer, Integer>();
        map.add(Integer.valueOf(17), Integer.valueOf(19));
        GenericType<MultivaluedMap<Integer, Integer>> type = new GenericType<MultivaluedMap<Integer, Integer>>() {
        };
        Message m = translator.translateToJavabuf(map, type);
        Any any = Any.pack(m);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setAnyField(any).build();
        GeneralReturnMessage response = blockingStubPlaintext.multimapWildWild(gem);
        any = response.getAnyField();
        Message result = Utility.unpack(any, translator);
        Assertions.assertEquals(map, translator.translateFromJavabuf(result));
    }

    // Client: jakarta.ws.rs.core.MultivaluedMap<java.lang.String, java.lang.String>
    // Server: MultivaluedMap<java.lang.String, java.lang.String>
    @Test
    public void testMultivaluedMapStringStringGeneric() throws Exception {
        MultivaluedMap<String, String> map = new MultivaluedHashMap<String, String>();
        map.add("from", "to");
        GenericType<MultivaluedMap<String, String>> type = new GenericType<MultivaluedMap<String, String>>() {
        };
        Message m = translator.translateToJavabuf(map, type);
        Any any = Any.pack(m);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setAnyField(any).build();
        GeneralReturnMessage response = blockingStubPlaintext.multimapStringString(gem);
        any = response.getAnyField();
        Message result = Utility.unpack(any, translator);
        Assertions.assertEquals(map, translator.translateFromJavabuf(result));
    }

    // Client: jakarta.ws.rs.core.MultivaluedMap<java.lang.String, java.lang.Integer>
    // Server: MultivaluedMap<java.lang.String, java.lang.Integer>
    @Test
    public void testMultivaluedMapStringInteger() throws Exception {
        MultivaluedMap<String, Integer> map = new MultivaluedHashMap<String, Integer>();
        map.add("23", Integer.valueOf(29));
        GenericType<MultivaluedMap<String, Integer>> type = new GenericType<MultivaluedMap<String, Integer>>() {
        };
        Message m = translator.translateToJavabuf(map, type);
        Any any = Any.pack(m);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setAnyField(any).build();
        GeneralReturnMessage response = blockingStubPlaintext.multimapStringInt(gem);
        any = response.getAnyField();
        Message result = Utility.unpack(any, translator);
        Assertions.assertEquals(map, translator.translateFromJavabuf(result));
    }

    // Client: jakarta.ws.rs.core.MultivaluedMap<java.lang.Object, java.lang.Object>
    // Server: Map<java.lang.Object, java.lang.Object>
    @Test
    public void testMultivaluedMapObjectObject() throws Exception {
        MultivaluedMap<Object, Object> map = new MultivaluedHashMap<Object, Object>();
        map.add(new M1("31", 37), new M1("39", 41));
        GenericType<MultivaluedMap<Object, Object>> type = new GenericType<MultivaluedMap<Object, Object>>() {
        };
        Message m = translator.translateToJavabuf(map, type);
        Any any = Any.pack(m);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setAnyField(any).build();
        GeneralReturnMessage response = blockingStubPlaintext.multimapObjObj(gem);
        any = response.getAnyField();
        Message result = Utility.unpack(any, translator);
        Assertions.assertEquals(map, translator.translateFromJavabuf(result));
    }

    // Client: jakarta.ws.rs.core.MultivaluedMap<List<Integer>, Set<Integer>>
    // Server: jakarta.ws.rs.core.MultivaluedMap<List<Integer>, Set<Integer>>
    @Test
    public void testMultivaluedMapListSet() throws Exception {
        MultivaluedMap<List<Integer>, Set<Integer>> map = new MultivaluedHashMap<List<Integer>, Set<Integer>>();
        List<Integer> list = new ArrayList<Integer>();
        list.add(43);
        Set<Integer> set = new HashSet<Integer>();
        set.add(47);
        map.add(list, set);
        GenericType<MultivaluedMap<List<Integer>, Set<Integer>>> type = new GenericType<MultivaluedMap<List<Integer>, Set<Integer>>>() {
        };
        Message m = translator.translateToJavabuf(map, type);
        Any any = Any.pack(m);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setAnyField(any).build();
        GeneralReturnMessage response = blockingStubPlaintext.multimapListSet(gem);
        any = response.getAnyField();
        Message result = Utility.unpack(any, translator);
        Assertions.assertEquals(map, translator.translateFromJavabuf(result));
    }

    // Client: Map<Map<String, String>, Map<Integer, Integer>>
    // Server: Map<Map<String, String>, Map<Integer, Integer>>
    @Test
    public void testMultivaluedMapMapMap() throws Exception {
        MultivaluedMap<MultivaluedMap<String, String>, MultivaluedMap<Integer, Integer>> map = new MultivaluedHashMap<MultivaluedMap<String, String>, MultivaluedMap<Integer, Integer>>();
        MultivaluedMap<String, String> smap = new MultivaluedHashMap<String, String>();
        smap.add("abc", "xyz");
        MultivaluedMap<Integer, Integer> imap = new MultivaluedHashMap<Integer, Integer>();
        imap.add(53, 57);
        map.add(smap, imap);
        GenericType<MultivaluedMap<MultivaluedMap<String, String>, MultivaluedMap<Integer, Integer>>> type = new GenericType<MultivaluedMap<MultivaluedMap<String, String>, MultivaluedMap<Integer, Integer>>>() {
        };
        Message m = translator.translateToJavabuf(map, type);
        Any any = Any.pack(m);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setAnyField(any).build();
        GeneralReturnMessage response = blockingStubPlaintext.multimapMapStringMapInt(gem);
        any = response.getAnyField();
        Message result = Utility.unpack(any, translator);
        Assertions.assertEquals(map, translator.translateFromJavabuf(result));
    }

    // Client: Map<ArrayList, HashSet>
    // Server: Map<ArrayList, HashSet>
    @Test
    public void testMultivaluedMapArraylistHashset() throws Exception {
        MultivaluedMap<ArrayList, HashSet> map = new MultivaluedHashMap<ArrayList, HashSet>();
        ArrayList<String> list = new ArrayList<String>();
        list.add("abc");
        HashSet<Integer> set = new HashSet<Integer>();
        set.add(59);
        map.add(list, set);
        GenericType<MultivaluedMap<ArrayList, HashSet>> type = new GenericType<MultivaluedMap<ArrayList, HashSet>>() {
        };
        Message m = translator.translateToJavabuf(map, type);
        Any any = Any.pack(m);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setAnyField(any).build();
        GeneralReturnMessage response = blockingStubPlaintext.multimapArraylistHashset(gem);
        any = response.getAnyField();
        Message result = Utility.unpack(any, translator);
        Assertions.assertEquals(map, translator.translateFromJavabuf(result));
    }

    // Client: MultivaluedHashMap<M1, M1>
    // Server: HashMap<M1, M1>
    @Test
    public void testMultivaluedMapM1M1() throws Exception {
        MultivaluedHashMap<M1, M1> map = new MultivaluedHashMap<M1, M1>();
        M1 m1a = new M1("61", 67);
        M1 m1b = new M1("71", 73);
        map.add(m1a, m1b);
        GenericType<MultivaluedMap<M1, M1>> type = new GenericType<MultivaluedMap<M1, M1>>() {
        };
        Message m = translator.translateToJavabuf(map, type);
        Any any = Any.pack(m);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setAnyField(any).build();
        GeneralReturnMessage response = blockingStubPlaintext.multimapM1M1(gem);
        any = response.getAnyField();
        Message result = Utility.unpack(any, translator);
        Assertions.assertEquals(map, translator.translateFromJavabuf(result));
    }

    //=======================================================================================
    //
    //                jakarta.ws.rs.core.MultivaluedHashMap class
    //
    //=======================================================================================
    // Client: MultivaluedHashMap
    // Server: MultivaluedHashMap
    @Test
    public void testMultivaluedHashMapRaw() throws Exception {
        MultivaluedHashMap map = new MultivaluedHashMap();
        map.add(new M1("three", Integer.valueOf(3)), new M1("five", Integer.valueOf(5)));
        jakarta_ws_rs_core___MultivaluedHashMap m = (jakarta_ws_rs_core___MultivaluedHashMap) translator
                .translateToJavabuf(map);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJakartaWsRsCoreMultivaluedHashMapField(m).build();
        GeneralReturnMessage response = blockingStubPlaintext.multimapHashmapNoType(gem);
        jakarta_ws_rs_core___MultivaluedHashMap result = response.getJakartaWsRsCoreMultivaluedHashMapField();
        MultivaluedHashMap mmm = (MultivaluedHashMap) translator.translateFromJavabuf(result);
        Assertions.assertEquals(map, mmm);
    }

    // Client: MultivaluedHashMap<java.lang.Object, java.lang.Object>
    // Server: MultivaluedHashMap<K, V>
    @Test
    public void testMultivaluedHashmapVariableVariable() throws Exception {
        MultivaluedHashMap<Object, Object> map = new MultivaluedHashMap<Object, Object>();
        map.add(Integer.valueOf(11), Integer.valueOf(13));
        GenericType<MultivaluedHashMap<Object, Object>> type = new GenericType<MultivaluedHashMap<Object, Object>>() {
        };
        jakarta_ws_rs_core___MultivaluedHashMap9 m = (jakarta_ws_rs_core___MultivaluedHashMap9) translator
                .translateToJavabuf(map, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJakartaWsRsCoreMultivaluedHashMap9Field(m).build();
        GeneralReturnMessage response = blockingStubPlaintext.multimapHashmapVarVar(gem);
        Message result = response.getJakartaWsRsCoreMultivaluedHashMap9Field();
        Assertions.assertEquals(map, translator.translateFromJavabuf(result));
    }

    // Client: MultivaluedHashMap
    // Server: MultivaluedHashMap
    @Test
    public void testMultivaluedHashmapVariableVariableNull() throws Exception {
        MultivaluedHashMap map = new MultivaluedHashMap<Object, Object>();
        map.add(Integer.valueOf(11), Integer.valueOf(13));
        jakarta_ws_rs_core___MultivaluedHashMap m = (jakarta_ws_rs_core___MultivaluedHashMap) translator.translateToJavabuf(map,
                null);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJakartaWsRsCoreMultivaluedHashMapField(m).build();
        GeneralReturnMessage response = blockingStubPlaintext.multimapHashmapNoType(gem);
        Message result = response.getJakartaWsRsCoreMultivaluedHashMapField();
        Assertions.assertEquals(map, translator.translateFromJavabuf(result));
    }

    // Client: MultivaluedHashMap<java.lang.Object, java.lang.Object>
    // Server: MultivaluedHashMap<?, ?>
    @Test
    public void testMultivaluedHashmapWildcardWildcard() throws Exception {
        MultivaluedHashMap<Integer, Integer> map = new MultivaluedHashMap<Integer, Integer>();
        map.add(Integer.valueOf(17), Integer.valueOf(19));
        GenericType<MultivaluedHashMap<Object, Object>> type = new GenericType<MultivaluedHashMap<Object, Object>>() {
        };
        jakarta_ws_rs_core___MultivaluedHashMap9 m = (jakarta_ws_rs_core___MultivaluedHashMap9) translator
                .translateToJavabuf(map, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJakartaWsRsCoreMultivaluedHashMap9Field(m).build();
        GeneralReturnMessage response = blockingStubPlaintext.multimapHashmapWildWild(gem);
        Message result = response.getJakartaWsRsCoreMultivaluedHashMap9Field();
        Assertions.assertEquals(map, translator.translateFromJavabuf(result));
    }

    // Client: MultivaluedHashMap<java.lang.String, java.lang.String>
    // Server: MultivaluedHashMap<java.lang.String, java.lang.String>
    @Test
    public void testMultivaluedHashmapStringStringGeneric() throws Exception {
        MultivaluedHashMap<String, String> map = new MultivaluedHashMap<String, String>();
        map.add("from", "to");
        GenericType<MultivaluedHashMap<String, String>> type = new GenericType<MultivaluedHashMap<String, String>>() {
        };
        jakarta_ws_rs_core___MultivaluedHashMap10 m = (jakarta_ws_rs_core___MultivaluedHashMap10) translator
                .translateToJavabuf(map, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJakartaWsRsCoreMultivaluedHashMap10Field(m).build();
        GeneralReturnMessage response = blockingStubPlaintext.multimapHashmapStringString(gem);
        Message result = response.getJakartaWsRsCoreMultivaluedHashMap10Field();
        Assertions.assertEquals(map, translator.translateFromJavabuf(result));
    }

    // Client: MultivaluedHashMap<java.lang.String, java.lang.Integer>
    // Server: MultivaluedHashMap<java.lang.String, java.lang.Integer>
    @Test
    public void testMultivaluedHashmapStringInteger() throws Exception {
        MultivaluedHashMap<String, Integer> map = new MultivaluedHashMap<String, Integer>();
        map.add("23", Integer.valueOf(29));
        GenericType<MultivaluedHashMap<String, Integer>> type = new GenericType<MultivaluedHashMap<String, Integer>>() {
        };
        jakarta_ws_rs_core___MultivaluedHashMap11 m = (jakarta_ws_rs_core___MultivaluedHashMap11) translator
                .translateToJavabuf(map, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJakartaWsRsCoreMultivaluedHashMap11Field(m).build();
        GeneralReturnMessage response = blockingStubPlaintext.multimapHashmapStringInt(gem);
        Message result = response.getJakartaWsRsCoreMultivaluedHashMap11Field();
        Assertions.assertEquals(map, translator.translateFromJavabuf(result));
    }

    // Client: MultivaluedHashMap<java.lang.Object, java.lang.Object>
    // Server: MultivaluedHashMap<java.lang.Object, java.lang.Object>
    @Test
    public void testMultivaluedHashmapObjectObject() throws Exception {
        MultivaluedHashMap<Object, Object> map = new MultivaluedHashMap<Object, Object>();
        map.add(new M1("31", 37), new M1("39", 41));
        GenericType<MultivaluedHashMap<Object, Object>> type = new GenericType<MultivaluedHashMap<Object, Object>>() {
        };
        jakarta_ws_rs_core___MultivaluedHashMap9 m = (jakarta_ws_rs_core___MultivaluedHashMap9) translator
                .translateToJavabuf(map, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJakartaWsRsCoreMultivaluedHashMap9Field(m).build();
        GeneralReturnMessage response = blockingStubPlaintext.multimapHashmapObjObj(gem);
        Message result = response.getJakartaWsRsCoreMultivaluedHashMap9Field();
        Assertions.assertEquals(map, translator.translateFromJavabuf(result));
    }

    // Client: MultivaluedHashMap<List<Integer>, Set<Integer>>
    // Server: MultivaluedHashMap<List<Integer>, Set<Integer>>
    @Test
    public void testMultivaluedHashmapListSet() throws Exception {
        MultivaluedHashMap<List<Integer>, Set<Integer>> map = new MultivaluedHashMap<List<Integer>, Set<Integer>>();
        List<Integer> list = new ArrayList<Integer>();
        list.add(43);
        Set<Integer> set = new HashSet<Integer>();
        set.add(47);
        map.add(list, set);
        GenericType<MultivaluedHashMap<List<Integer>, Set<Integer>>> type = new GenericType<MultivaluedHashMap<List<Integer>, Set<Integer>>>() {
        };
        jakarta_ws_rs_core___MultivaluedHashMap12 m = (jakarta_ws_rs_core___MultivaluedHashMap12) translator
                .translateToJavabuf(map, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJakartaWsRsCoreMultivaluedHashMap12Field(m).build();
        GeneralReturnMessage response = blockingStubPlaintext.multimapHashmapListSet(gem);
        Message result = response.getJakartaWsRsCoreMultivaluedHashMap12Field();
        Assertions.assertEquals(map, translator.translateFromJavabuf(result));
    }

    // Client: MultivaluedHashMap<MultivaluedHashMap<String, String>, MultivaluedHashMap<Integer, Integer>>
    // Server: MultivaluedHashMap<MultivaluedHashMap<String, String>, MultivaluedHashMap<Integer, Integer>>
    @Test
    public void testMultivaluedHashmapMapMap() throws Exception {
        MultivaluedHashMap<MultivaluedHashMap<String, String>, MultivaluedHashMap<Integer, Integer>> map = new MultivaluedHashMap<MultivaluedHashMap<String, String>, MultivaluedHashMap<Integer, Integer>>();
        MultivaluedHashMap<String, String> smap = new MultivaluedHashMap<String, String>();
        smap.add("abc", "xyz");
        MultivaluedHashMap<Integer, Integer> imap = new MultivaluedHashMap<Integer, Integer>();
        imap.add(53, 57);
        map.add(smap, imap);
        GenericType<MultivaluedHashMap<MultivaluedHashMap<String, String>, MultivaluedHashMap<Integer, Integer>>> type = new GenericType<MultivaluedHashMap<MultivaluedHashMap<String, String>, MultivaluedHashMap<Integer, Integer>>>() {
        };
        jakarta_ws_rs_core___MultivaluedHashMap14 m = (jakarta_ws_rs_core___MultivaluedHashMap14) translator
                .translateToJavabuf(map, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJakartaWsRsCoreMultivaluedHashMap14Field(m).build();
        GeneralReturnMessage response = blockingStubPlaintext.multimapHashmapHashmapStringHashmapInt(gem);
        Message result = response.getJakartaWsRsCoreMultivaluedHashMap14Field();
        Assertions.assertEquals(map, translator.translateFromJavabuf(result));
    }

    // Client: MultivaluedHashMap<ArrayList, HashSet>
    // Server: MultivaluedHashMap<ArrayList, HashSet>
    @Test
    public void testMultivaluedHashmapArraylistHashset() throws Exception {
        MultivaluedHashMap<ArrayList, HashSet> map = new MultivaluedHashMap<ArrayList, HashSet>();
        ArrayList<String> list = new ArrayList<String>();
        list.add("abc");
        HashSet<Integer> set = new HashSet<Integer>();
        set.add(59);
        map.add(list, set);
        GenericType<MultivaluedHashMap<ArrayList, HashSet>> type = new GenericType<MultivaluedHashMap<ArrayList, HashSet>>() {
        };
        jakarta_ws_rs_core___MultivaluedHashMap13 m = (jakarta_ws_rs_core___MultivaluedHashMap13) translator
                .translateToJavabuf(map, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJakartaWsRsCoreMultivaluedHashMap13Field(m).build();
        GeneralReturnMessage response = blockingStubPlaintext.multimapHashmapArraylistHashset(gem);
        Message result = response.getJakartaWsRsCoreMultivaluedHashMap13Field();
        Assertions.assertEquals(map, translator.translateFromJavabuf(result));
    }

    // Client: MultivaluedHashMap<M1, M1>
    // Server: MultivaluedHashMap<M1, M1>
    @Test
    public void testMultivaluedHashmapM1M1() throws Exception {
        MultivaluedHashMap<M1, M1> map = new MultivaluedHashMap<M1, M1>();
        M1 m1a = new M1("61", 67);
        M1 m1b = new M1("71", 73);
        map.add(m1a, m1b);
        GenericType<MultivaluedHashMap<M1, M1>> type = new GenericType<MultivaluedHashMap<M1, M1>>() {
        };
        jakarta_ws_rs_core___MultivaluedHashMap15 m = (jakarta_ws_rs_core___MultivaluedHashMap15) translator
                .translateToJavabuf(map, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJakartaWsRsCoreMultivaluedHashMap15Field(m).build();
        GeneralReturnMessage response = blockingStubPlaintext.multimapHashmapM1M1(gem);
        Message result = response.getJakartaWsRsCoreMultivaluedHashMap15Field();
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
