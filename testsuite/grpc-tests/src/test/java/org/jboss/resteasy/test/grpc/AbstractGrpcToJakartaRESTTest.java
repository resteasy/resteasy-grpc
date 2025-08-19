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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.GenericType;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
//import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.jupiter.api.Assertions;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Any;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;

import dev.resteasy.grpc.arrays.ArrayResource;
import dev.resteasy.grpc.arrays.Array_proto;
import dev.resteasy.grpc.bridge.runtime.Utility;
import dev.resteasy.grpc.bridge.runtime.protobuf.JavabufTranslator;
import dev.resteasy.grpc.example.CC1;
import dev.resteasy.grpc.example.CC10;
import dev.resteasy.grpc.example.CC11;
import dev.resteasy.grpc.example.CC12;
import dev.resteasy.grpc.example.CC1ServiceGrpc.CC1ServiceBlockingStub;
import dev.resteasy.grpc.example.CC1ServiceGrpc.CC1ServiceFutureStub;
import dev.resteasy.grpc.example.CC1ServiceGrpc.CC1ServiceStub;
import dev.resteasy.grpc.example.CC1_Server;
import dev.resteasy.grpc.example.CC1_proto;
import dev.resteasy.grpc.example.CC1_proto.FormMap;
import dev.resteasy.grpc.example.CC1_proto.FormValues;
import dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage;
import dev.resteasy.grpc.example.CC1_proto.GeneralReturnMessage;
import dev.resteasy.grpc.example.CC1_proto.ServletInfo;
import dev.resteasy.grpc.example.CC1_proto.dev_resteasy_grpc_example_InnerClasses_INNER_InnerClassHolder;
import dev.resteasy.grpc.example.CC1_proto.dev_resteasy_grpc_example_InnerClasses_INNER_PublicPrivate;
import dev.resteasy.grpc.example.CC1_proto.dev_resteasy_grpc_example_InnerClasses_INNER_PublicPublic;
import dev.resteasy.grpc.example.CC1_proto.dev_resteasy_grpc_example___CC2;
import dev.resteasy.grpc.example.CC1_proto.dev_resteasy_grpc_example___CC3;
import dev.resteasy.grpc.example.CC1_proto.dev_resteasy_grpc_example___CC4;
import dev.resteasy.grpc.example.CC1_proto.dev_resteasy_grpc_example___CC5;
import dev.resteasy.grpc.example.CC1_proto.dev_resteasy_grpc_example___CC7;
import dev.resteasy.grpc.example.CC1_proto.dev_resteasy_grpc_example___CC9;
import dev.resteasy.grpc.example.CC1_proto.dev_resteasy_grpc_example___IntfImpl;
import dev.resteasy.grpc.example.CC1_proto.dev_resteasy_grpc_example___Person;
import dev.resteasy.grpc.example.CC1_proto.dev_resteasy_grpc_example___RecordCC2;
import dev.resteasy.grpc.example.CC1_proto.gCookie;
import dev.resteasy.grpc.example.CC1_proto.gHeader;
import dev.resteasy.grpc.example.CC1_proto.gInteger;
import dev.resteasy.grpc.example.CC1_proto.gNewCookie;
import dev.resteasy.grpc.example.CC1_proto.gString;
import dev.resteasy.grpc.example.CC2;
import dev.resteasy.grpc.example.CC3;
import dev.resteasy.grpc.example.CC5;
import dev.resteasy.grpc.example.InnerClasses;
import dev.resteasy.grpc.example.Person;
import dev.resteasy.grpc.example.RecordCC2;
import dev.resteasy.grpc.example.RecordVariable;
import dev.resteasy.grpc.example.TestClass;
import dev.resteasy.grpc.example.TestSubClass;
import dev.resteasy.grpc.example.sub.CC8;
import dev.resteasy.grpc.lists.sets.DD1;
import dev.resteasy.grpc.maps.MapResource;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

/**
 * @tpSubChapter gRPC bridge plus WildFly grpc subsystem
 * @tpChapter grpc-tests tests
 * @tpSince RESTEasy 1.0.0
 */
abstract class AbstractGrpcToJakartaRESTTest {

    private static JavabufTranslator translator;

    static {
        Class<?> clazz;
        try {
            clazz = Class.forName("dev.resteasy.grpc.example.CC1JavabufTranslator");
            translator = (JavabufTranslator) clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static Archive<?> doDeploy(final String deploymentName) throws Exception {
        final var resolver = Maven.resolver()
                .loadPomFromFile("pom.xml");
        Archive<?> ar = ShrinkWrap.create(WebArchive.class, deploymentName + ".war")
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
        //        ar.as(ZipExporter.class).exportTo(new File("/tmp/collections.war"), true);
        return ar;
    }

    static void accessServletContexts() {
        try (
                Client client = ClientBuilder.newClient();
                var response = client.target("http://localhost:8080/grpc-test/grpcToJakartaRest/grpcserver/context")
                        .request()
                        .get()) {
            final var message = response.getStatus() + ": " + response.readEntity(String.class);
            Assertions.assertEquals(204, response.getStatus(), message);
        }
    }

    /****************************************************************************************/
    /****************************************************************************************/
    void doBlockingTest(CC1ServiceBlockingStub stub) throws Exception {
        this.testBoolean(stub);
        this.testBooleanWithUnnecessaryURL(stub);
        this.testBooleanWrapper(stub);
        this.testByte(stub);
        this.testByteWrapper(stub);
        this.testChar(stub);
        this.testCharacter(stub);
        this.testCompletionStage(stub);
        this.testConstructor(stub);
        this.testConsumes(stub);
        this.testCookieParams(stub);
        this.testDouble(stub);
        this.testDoubleWrapper(stub);
        this.testFloat(stub);
        this.testFloatWrapper(stub);
        this.testHeaderParams(stub);
        this.testInheritance(stub);
        this.testInnerClass(stub);
        this.testInt(stub);
        this.testInteger(stub);
        this.testJaxrsResponse(stub);
        this.testLocatorGet(stub);
        this.testLocatorPost(stub);
        this.testLong(stub);
        this.testLongWrapper(stub);
        this.testMatrixParams(stub);
        this.testParamsList(stub);
        this.testParamsSet(stub);
        this.testParamsSortedSet(stub);
        this.testPathParams(stub);
        this.testProduces(stub);
        this.testProducesJson(stub);
        this.testQueryParams(stub);
        this.testReferenceField(stub);
        this.testResponse(stub);
        this.testServerCookies(stub);
        this.testServerHeaders(stub);
        this.testServletConfigServletName(stub);
        this.testServletContextInitParam(stub);
        this.testServletContextPath(stub);
        this.testServletInfo(stub);
        this.testServletParams(stub);
        this.testServletPath(stub);
        this.testServletResponse(stub);
        this.testShort(stub);
        this.testShortWrapper(stub);
        this.testSSE(stub);
        this.testString(stub);
        this.testSuspend(stub);
        this.testCopy(stub);
        this.testInterfaceEntity(stub);
        this.testInterfaceReturn(stub);
        this.testInnerPublicPublic(stub);
        this.testInnerPublicPrivate(stub);
        this.testInnerHolder(stub);
        this.testGenericWildcardObject(stub);
        this.testGenericWildcardWildcard(stub);
        this.testGenericVariableWildcard(stub);
        this.testGenericVariableObject(stub);
        this.testGenericInteger(stub);
        this.testGenericFloat(stub);
        this.testGenericMap(stub);
        this.testGenericWildcardLowerObject(stub);
        this.testGenericWildcardLowerWildcard(stub);
        this.testGenericVariableUpperTestClass(stub);
        this.testGenericVariablePrimitiveArray(stub);
        this.testGenericVariableReferenceArray(stub);
        this.testC11GenericWildcardTestClass(stub);
        this.testC11GenericVariableTestClass(stub);
        this.testRecordPerson(stub);
        this.testRecordCC2(stub);
        this.testRecordWildcard(stub);
        this.testRecordVariable(stub);
        this.testRecordString(stub);
        this.testRecordInteger(stub);
        this.testRecordArrayPrimitive(stub);
        this.testRecordArrayCC3(stub);
        this.testRecordMap(stub);
        this.testReturnValueOnly(stub);
        this.testCollidingMethodNames(stub);
    }

    void doAsyncTest(CC1ServiceStub asyncStub) throws Exception {
        testIntAsyncStub(asyncStub);
        testSseAsyncStub(asyncStub);
    }

    void doFutureTest(CC1ServiceFutureStub futureStub) throws Exception {
        testIntFutureStub(futureStub);
    }

    /****************************************************************************************/
    /****************************************************************************************/
    void testBoolean(CC1ServiceBlockingStub stub) throws Exception {
        dev.resteasy.grpc.example.CC1_proto.gBoolean n = dev.resteasy.grpc.example.CC1_proto.gBoolean.newBuilder()
                .setValue(false)
                .build();
        dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage.Builder builder = dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage
                .newBuilder();
        GeneralEntityMessage gem = builder.setGBooleanField(n).build();
        GeneralReturnMessage response;
        try {
            response = stub.getBoolean(gem);
            dev.resteasy.grpc.example.CC1_proto.gBoolean expected = dev.resteasy.grpc.example.CC1_proto.gBoolean.newBuilder()
                    .setValue(true).build();
            Assertions.assertEquals(expected, response.getGBooleanField());
        } catch (StatusRuntimeException e) {
            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testBooleanWithUnnecessaryURL(CC1ServiceBlockingStub stub) throws Exception {
        dev.resteasy.grpc.example.CC1_proto.gBoolean n = dev.resteasy.grpc.example.CC1_proto.gBoolean.newBuilder()
                .setValue(false)
                .build();
        dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage.Builder builder = dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage
                .newBuilder();
        GeneralEntityMessage gem = builder.setURL("http://localhost:8080/p/boolean").setGBooleanField(n).build();
        GeneralReturnMessage response;
        try {
            response = stub.getBoolean(gem);
            dev.resteasy.grpc.example.CC1_proto.gBoolean expected = dev.resteasy.grpc.example.CC1_proto.gBoolean.newBuilder()
                    .setValue(true).build();
            Assertions.assertEquals(expected, response.getGBooleanField());
        } catch (StatusRuntimeException e) {
            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testBooleanWrapper(CC1ServiceBlockingStub stub) throws Exception {
        dev.resteasy.grpc.example.CC1_proto.gBoolean n = dev.resteasy.grpc.example.CC1_proto.gBoolean.newBuilder()
                .setValue(false)
                .build();
        dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage.Builder builder = dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage
                .newBuilder();
        GeneralEntityMessage gem = builder.setGBooleanField(n).build();
        GeneralReturnMessage response;
        try {
            response = stub.getBooleanWrapper(gem);
            dev.resteasy.grpc.example.CC1_proto.gBoolean expected = dev.resteasy.grpc.example.CC1_proto.gBoolean.newBuilder()
                    .setValue(true).build();
            Assertions.assertEquals(expected, response.getGBooleanField());
        } catch (StatusRuntimeException e) {
            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testByte(CC1ServiceBlockingStub stub) throws Exception {
        dev.resteasy.grpc.example.CC1_proto.gByte n = dev.resteasy.grpc.example.CC1_proto.gByte.newBuilder().setValue(3)
                .build();
        dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage.Builder builder = dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage
                .newBuilder();
        GeneralEntityMessage gem = builder.setGByteField(n).build();
        GeneralReturnMessage response;
        try {
            response = stub.getByte(gem);
            dev.resteasy.grpc.example.CC1_proto.gByte expected = dev.resteasy.grpc.example.CC1_proto.gByte.newBuilder()
                    .setValue(4)
                    .build();
            Assertions.assertEquals(expected, response.getGByteField());
        } catch (StatusRuntimeException e) {

            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testByteWrapper(CC1ServiceBlockingStub stub) throws Exception {
        dev.resteasy.grpc.example.CC1_proto.gByte n = dev.resteasy.grpc.example.CC1_proto.gByte.newBuilder().setValue(7)
                .build();
        dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage.Builder builder = dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage
                .newBuilder();
        GeneralEntityMessage gem = builder.setGByteField(n).build();
        GeneralReturnMessage response;
        try {
            response = stub.getByteWrapper(gem);
            dev.resteasy.grpc.example.CC1_proto.gByte expected = dev.resteasy.grpc.example.CC1_proto.gByte.newBuilder()
                    .setValue(8)
                    .build();
            Assertions.assertEquals(expected, response.getGByteField());
        } catch (StatusRuntimeException e) {

            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testShort(CC1ServiceBlockingStub stub) throws Exception {
        dev.resteasy.grpc.example.CC1_proto.gShort n = dev.resteasy.grpc.example.CC1_proto.gShort.newBuilder()
                .setValue(3)
                .build();
        dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage.Builder builder = dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage
                .newBuilder();
        GeneralEntityMessage gem = builder.setGShortField(n).build();
        GeneralReturnMessage response;
        try {
            response = stub.getShort(gem);
            dev.resteasy.grpc.example.CC1_proto.gShort expected = dev.resteasy.grpc.example.CC1_proto.gShort.newBuilder()
                    .setValue(4)
                    .build();
            Assertions.assertEquals(expected, response.getGShortField());
        } catch (StatusRuntimeException e) {

            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testShortWrapper(CC1ServiceBlockingStub stub) throws Exception {
        dev.resteasy.grpc.example.CC1_proto.gShort n = dev.resteasy.grpc.example.CC1_proto.gShort.newBuilder()
                .setValue(7)
                .build();
        dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage.Builder builder = dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage
                .newBuilder();
        GeneralEntityMessage gem = builder.setGShortField(n).build();
        GeneralReturnMessage response;
        try {
            response = stub.getShortWrapper(gem);
            dev.resteasy.grpc.example.CC1_proto.gShort expected = dev.resteasy.grpc.example.CC1_proto.gShort.newBuilder()
                    .setValue(8)
                    .build();
            Assertions.assertEquals(expected, response.getGShortField());
        } catch (StatusRuntimeException e) {

            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testInt(CC1ServiceBlockingStub stub) throws Exception {
        dev.resteasy.grpc.example.CC1_proto.gInteger n = dev.resteasy.grpc.example.CC1_proto.gInteger.newBuilder()
                .setValue(3)
                .build();
        dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage.Builder builder = dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage
                .newBuilder();
        GeneralEntityMessage gem = builder.setGIntegerField(n).build();
        GeneralReturnMessage response;
        try {
            response = stub.getInt(gem);
            dev.resteasy.grpc.example.CC1_proto.gInteger expected = dev.resteasy.grpc.example.CC1_proto.gInteger.newBuilder()
                    .setValue(4)
                    .build();
            Assertions.assertEquals(expected, response.getGIntegerField());
        } catch (StatusRuntimeException e) {

            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testInteger(CC1ServiceBlockingStub stub) throws Exception {
        dev.resteasy.grpc.example.CC1_proto.gInteger n = dev.resteasy.grpc.example.CC1_proto.gInteger.newBuilder()
                .setValue(3)
                .build();
        dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage.Builder builder = dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage
                .newBuilder();
        GeneralEntityMessage gem = builder.setGIntegerField(n).build();
        GeneralReturnMessage response;
        try {
            response = stub.getInteger(gem);
            dev.resteasy.grpc.example.CC1_proto.gInteger expected = dev.resteasy.grpc.example.CC1_proto.gInteger.newBuilder()
                    .setValue(4)
                    .build();
            Assertions.assertEquals(expected, response.getGIntegerField());
        } catch (StatusRuntimeException e) {

            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testLong(CC1ServiceBlockingStub stub) throws Exception {
        dev.resteasy.grpc.example.CC1_proto.gLong n = dev.resteasy.grpc.example.CC1_proto.gLong.newBuilder()
                .setValue(3L)
                .build();
        dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage.Builder builder = dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage
                .newBuilder();
        GeneralEntityMessage gem = builder.setURL("http://localhost:8080" + "/p/long").setGLongField(n).build();
        GeneralReturnMessage response;
        try {
            response = stub.getLong(gem);
            dev.resteasy.grpc.example.CC1_proto.gLong expected = dev.resteasy.grpc.example.CC1_proto.gLong.newBuilder()
                    .setValue(4L)
                    .build();
            Assertions.assertEquals(expected, response.getGLongField());
        } catch (StatusRuntimeException e) {

            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testLongWrapper(CC1ServiceBlockingStub stub) throws Exception {
        dev.resteasy.grpc.example.CC1_proto.gLong n = dev.resteasy.grpc.example.CC1_proto.gLong.newBuilder()
                .setValue(3L)
                .build();
        dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage.Builder builder = dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage
                .newBuilder();
        GeneralEntityMessage gem = builder.setURL("http://localhost:8080" + "/p/Long").setGLongField(n).build();
        GeneralReturnMessage response;
        try {
            response = stub.getLongWrapper(gem);
            dev.resteasy.grpc.example.CC1_proto.gLong expected = dev.resteasy.grpc.example.CC1_proto.gLong.newBuilder()
                    .setValue(4L)
                    .build();
            Assertions.assertEquals(expected, response.getGLongField());
        } catch (StatusRuntimeException e) {

            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testFloat(CC1ServiceBlockingStub stub) throws Exception {
        dev.resteasy.grpc.example.CC1_proto.gFloat n = dev.resteasy.grpc.example.CC1_proto.gFloat.newBuilder()
                .setValue(3.0f)
                .build();
        dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage.Builder builder = dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage
                .newBuilder();
        GeneralEntityMessage gem = builder.setURL("http://localhost:8080" + "/p/float").setGFloatField(n).build();
        GeneralReturnMessage response;
        try {
            response = stub.getFloat(gem);
            dev.resteasy.grpc.example.CC1_proto.gFloat expected = dev.resteasy.grpc.example.CC1_proto.gFloat.newBuilder()
                    .setValue(4.0f)
                    .build();
            Assertions.assertEquals(expected, response.getGFloatField());
        } catch (StatusRuntimeException e) {

            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testFloatWrapper(CC1ServiceBlockingStub stub) throws Exception {
        dev.resteasy.grpc.example.CC1_proto.gFloat n = dev.resteasy.grpc.example.CC1_proto.gFloat.newBuilder()
                .setValue(3.0f)
                .build();
        dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage.Builder builder = dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage
                .newBuilder();
        GeneralEntityMessage gem = builder.setURL("http://localhost:8080" + "/p/Float").setGFloatField(n).build();
        GeneralReturnMessage response;
        try {
            response = stub.getFloat(gem);
            dev.resteasy.grpc.example.CC1_proto.gFloat expected = dev.resteasy.grpc.example.CC1_proto.gFloat.newBuilder()
                    .setValue(4.0f)
                    .build();
            Assertions.assertEquals(expected, response.getGFloatField());
        } catch (StatusRuntimeException e) {

            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testDouble(CC1ServiceBlockingStub stub) throws Exception {
        dev.resteasy.grpc.example.CC1_proto.gDouble n = dev.resteasy.grpc.example.CC1_proto.gDouble.newBuilder()
                .setValue(3.0d)
                .build();
        dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage.Builder builder = dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage
                .newBuilder();
        GeneralEntityMessage gem = builder.setURL("http://localhost:8080" + "/p/double").setGDoubleField(n).build();
        GeneralReturnMessage response;
        try {
            response = stub.getDouble(gem);
            dev.resteasy.grpc.example.CC1_proto.gDouble expected = dev.resteasy.grpc.example.CC1_proto.gDouble.newBuilder()
                    .setValue(4.0d)
                    .build();
            Assertions.assertEquals(expected, response.getGDoubleField());
        } catch (StatusRuntimeException e) {

            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testDoubleWrapper(CC1ServiceBlockingStub stub) throws Exception {
        dev.resteasy.grpc.example.CC1_proto.gDouble n = dev.resteasy.grpc.example.CC1_proto.gDouble.newBuilder()
                .setValue(3.0d)
                .build();
        dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage.Builder builder = dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage
                .newBuilder();
        GeneralEntityMessage gem = builder.setURL("http://localhost:8080" + "/p/Double").setGDoubleField(n).build();
        GeneralReturnMessage response;
        try {
            response = stub.getDouble(gem);
            dev.resteasy.grpc.example.CC1_proto.gDouble expected = dev.resteasy.grpc.example.CC1_proto.gDouble.newBuilder()
                    .setValue(4.0d)
                    .build();
            Assertions.assertEquals(expected, response.getGDoubleField());
        } catch (StatusRuntimeException e) {

            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testChar(CC1ServiceBlockingStub stub) throws Exception {
        dev.resteasy.grpc.example.CC1_proto.gCharacter n = dev.resteasy.grpc.example.CC1_proto.gCharacter.newBuilder()
                .setValue("a")
                .build();
        dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage.Builder builder = dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage
                .newBuilder();
        GeneralEntityMessage gem = builder.setURL("http://localhost:8080" + "/p/char").setGCharacterField(n).build();
        GeneralReturnMessage response;
        try {
            response = stub.getChar(gem);
            dev.resteasy.grpc.example.CC1_proto.gCharacter expected = dev.resteasy.grpc.example.CC1_proto.gCharacter
                    .newBuilder()
                    .setValue("A").build();
            Assertions.assertEquals(expected, response.getGCharacterField());
        } catch (StatusRuntimeException e) {

            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testCharacter(CC1ServiceBlockingStub stub) throws Exception {
        dev.resteasy.grpc.example.CC1_proto.gCharacter n = dev.resteasy.grpc.example.CC1_proto.gCharacter.newBuilder()
                .setValue("a")
                .build();
        dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage.Builder builder = dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage
                .newBuilder();
        GeneralEntityMessage gem = builder.setURL("http://localhost:8080" + "/p/Character")
                .setGCharacterField(n)
                .build();
        GeneralReturnMessage response;
        try {
            response = stub.getChar(gem);
            dev.resteasy.grpc.example.CC1_proto.gCharacter expected = dev.resteasy.grpc.example.CC1_proto.gCharacter
                    .newBuilder()
                    .setValue("A").build();
            Assertions.assertEquals(expected, response.getGCharacterField());
        } catch (StatusRuntimeException e) {

            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testString(CC1ServiceBlockingStub stub) throws Exception {
        dev.resteasy.grpc.example.CC1_proto.gString n = dev.resteasy.grpc.example.CC1_proto.gString.newBuilder()
                .setValue("abc")
                .build();
        dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage.Builder builder = dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage
                .newBuilder();
        GeneralEntityMessage gem = builder.setURL("http://localhost:8080" + "/p/string").setGStringField(n).build();
        GeneralReturnMessage response;
        try {
            response = stub.getString(gem);
            dev.resteasy.grpc.example.CC1_proto.gString expected = dev.resteasy.grpc.example.CC1_proto.gString.newBuilder()
                    .setValue("ABC").build();
            Assertions.assertEquals(expected, response.getGStringField());
        } catch (StatusRuntimeException e) {

            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testConstructor(CC1ServiceBlockingStub stub) throws Exception {
        dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage.Builder builder = dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage
                .newBuilder();
        GeneralEntityMessage gem = builder.setURL("http://localhost:8080" + "/p/constructor").build();
        GeneralReturnMessage response;
        try {
            response = stub.constructor(gem);
            dev_resteasy_grpc_example___CC3 cc3 = dev_resteasy_grpc_example___CC3.newBuilder().setS("eight").build();
            dev_resteasy_grpc_example___CC9.Builder cc9Builder = dev_resteasy_grpc_example___CC9.newBuilder();
            dev_resteasy_grpc_example___CC9 expected = cc9Builder.setBo(true)
                    .setBy((byte) 1)
                    .setS((short) 2)
                    .setI(3)
                    .setL(4L)
                    .setF(5.0f)
                    .setD(6.0d)
                    .setC("7")
                    .setCc3(cc3)
                    .build();
            Assertions.assertEquals(expected, response.getDevResteasyGrpcExampleCC9Field());
        } catch (StatusRuntimeException e) {

            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testProduces(CC1ServiceBlockingStub stub) throws Exception {
        dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage.Builder builder = dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage
                .newBuilder();
        GeneralEntityMessage gem = builder.setURL("http://localhost:8080/p/produces").build();
        GeneralReturnMessage response;
        try {
            response = stub.produces(gem);
            dev.resteasy.grpc.example.CC1_proto.gString expected = dev.resteasy.grpc.example.CC1_proto.gString.newBuilder()
                    .setValue("produces").build();
            Assertions.assertEquals(expected, response.getGStringField());
        } catch (StatusRuntimeException e) {

            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testProducesJson(CC1ServiceBlockingStub stub) throws Exception {
        dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage.Builder builder = dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage
                .newBuilder();
        GeneralEntityMessage gem = builder.setURL("http://localhost:8080/p/produces/json").build();
        GeneralReturnMessage response;
        try {
            response = stub.produces(gem);
            dev.resteasy.grpc.example.CC1_proto.gString expected = dev.resteasy.grpc.example.CC1_proto.gString.newBuilder()
                    .setValue("producesJson").build();
            Assertions.assertEquals(expected, response.getGStringField());
        } catch (StatusRuntimeException e) {

            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testConsumes(CC1ServiceBlockingStub stub) throws Exception {
        dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage.Builder builder = dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage
                .newBuilder();
        GeneralEntityMessage gem = builder.setURL("http://localhost:8080" + "/p/consumes").build();
        GeneralReturnMessage response;
        try {
            response = stub.produces(gem);
            dev.resteasy.grpc.example.CC1_proto.gString expected = dev.resteasy.grpc.example.CC1_proto.gString.newBuilder()
                    .setValue("consumes").build();
            Assertions.assertEquals(expected, response.getGStringField());
        } catch (StatusRuntimeException e) {

            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testPathParams(CC1ServiceBlockingStub stub) throws Exception {
        dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage.Builder builder = dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage
                .newBuilder();
        GeneralEntityMessage gem = builder.setURL("http://localhost:8080/p/path/aa/param/bb").build();
        GeneralReturnMessage response;
        try {
            response = stub.pathParams(gem);
            dev.resteasy.grpc.example.CC1_proto.gString expected = dev.resteasy.grpc.example.CC1_proto.gString.newBuilder()
                    .setValue("xaaybbz").build();
            Assertions.assertEquals(expected, response.getGStringField());
        } catch (StatusRuntimeException e) {

            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testQueryParams(CC1ServiceBlockingStub stub) throws Exception {
        dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage.Builder builder = dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage
                .newBuilder();
        GeneralEntityMessage gem = builder.setURL("http://localhost:8080/p/query?q1=a&q2=b").build();
        GeneralReturnMessage response;
        try {
            response = stub.queryParams(gem);
            dev.resteasy.grpc.example.CC1_proto.gString expected = dev.resteasy.grpc.example.CC1_proto.gString.newBuilder()
                    .setValue("xaybz").build();
            Assertions.assertEquals(expected, response.getGStringField());
        } catch (StatusRuntimeException e) {

            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testMatrixParams(CC1ServiceBlockingStub stub) throws Exception {
        dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage.Builder builder = dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage
                .newBuilder();
        GeneralEntityMessage gem = builder.setURL("http://localhost:8080/p/matrix;m1=a;m2=b/more;m3=c").build();
        GeneralReturnMessage response;
        try {
            response = stub.matrixParams(gem);
            dev.resteasy.grpc.example.CC1_proto.gString expected = dev.resteasy.grpc.example.CC1_proto.gString.newBuilder()
                    .setValue("waxbycz").build();
            Assertions.assertEquals(expected, response.getGStringField());
        } catch (StatusRuntimeException e) {

            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    /**
     * Clarify treatment of cookies
     */
    void testCookieParams(CC1ServiceBlockingStub stub) throws Exception {
        dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage.Builder messageBuilder = dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage
                .newBuilder();
        messageBuilder.setURL("http://localhost:8080/p/cookieParams");
        gCookie.Builder cookieBuilder1 = gCookie.newBuilder();
        gCookie.Builder cookieBuilder2 = gCookie.newBuilder();
        gCookie cookie1 = cookieBuilder1.setName("c1").setValue("v1").setPath("a/b").setDomain("d1").build();
        gCookie cookie2 = cookieBuilder2.setName("c2").setValue("v2").build();
        messageBuilder.addCookies(cookie1).addCookies(cookie2);
        GeneralEntityMessage gem = messageBuilder.build();
        GeneralReturnMessage response;
        try {
            response = stub.cookieParams(gem);
            Assertions.assertEquals("xc1=v1;d1,a/b,0yc2=v2;,,0z", response.getGStringField().getValue());
        } catch (StatusRuntimeException e) {

            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testHeaderParams(CC1ServiceBlockingStub stub) throws Exception {
        dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage.Builder messageBuilder = dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage
                .newBuilder();
        messageBuilder.setURL("http://localhost:8080" + "/p/headerParams");
        gHeader.Builder headerBuilder1 = gHeader.newBuilder();
        gHeader header1 = headerBuilder1.addValues("v1.1").addValues("v1.2").build();
        messageBuilder.putHeaders("h1", header1);
        gHeader.Builder headerBuilder2 = gHeader.newBuilder();
        gHeader header2 = headerBuilder2.addValues("v2").build();
        messageBuilder.putHeaders("h2", header2);
        GeneralEntityMessage gem = messageBuilder.build();
        GeneralReturnMessage response;
        try {
            response = stub.headerParams(gem);
            Assertions.assertEquals("xv1.1yv2z", response.getGStringField().getValue());
        } catch (StatusRuntimeException e) {

            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testParamsList(CC1ServiceBlockingStub stub) throws Exception {
        dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage.Builder builder = GeneralEntityMessage.newBuilder();
        builder.putHeaders("h1", gHeader.newBuilder().addValues("hv1").addValues("hv2").build());
        GeneralEntityMessage gem = builder
                .setURL("http://localhost:8080" + "/p/params;m1=mv1;m1=mv2/pv1/list/pv2?q1=qv1&q1=qv2").build();
        GeneralReturnMessage response;
        try {
            response = stub.paramsList(gem);
            gString expected = gString.newBuilder().setValue("hv1hv2mv1mv2pv1pv2qv1qv2").build();
            Assertions.assertEquals(expected, response.getGStringField());
        } catch (StatusRuntimeException e) {

            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testParamsSet(CC1ServiceBlockingStub stub) throws Exception {
        GeneralEntityMessage.Builder builder = GeneralEntityMessage.newBuilder();
        builder.putHeaders("h1", gHeader.newBuilder().addValues("hv1").addValues("hv2").build());
        GeneralEntityMessage gem = builder.setURL("http://localhost:8080" + "/p/params;m1=mv1;m1=mv2/pv1/set/pv2?q1=qv1&q1=qv2")
                .build();
        GeneralReturnMessage response;
        try {
            response = stub.paramsSet(gem);
            gString expected = gString.newBuilder().setValue("hv1hv2mv1mv2pv1pv2qv1qv2").build();
            Assertions.assertEquals(expected, response.getGStringField());
        } catch (StatusRuntimeException e) {

            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testParamsSortedSet(CC1ServiceBlockingStub stub) throws Exception {
        dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage.Builder builder = dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage
                .newBuilder();
        builder.putHeaders("h1", gHeader.newBuilder().addValues("hv1").addValues("hv2").build());
        GeneralEntityMessage gem = builder
                .setURL("http://localhost:8080" + "/p/params;m1=mv1;m1=mv2/pv1/sortedset/pv2?q1=qv1&q1=qv2").build();
        GeneralReturnMessage response;
        try {
            response = stub.paramsSortedSet(gem);
            dev.resteasy.grpc.example.CC1_proto.gString expected = dev.resteasy.grpc.example.CC1_proto.gString.newBuilder()
                    .setValue("hv1hv2mv1mv2pv1pv2qv1qv2").build();
            Assertions.assertEquals(expected, response.getGStringField());
        } catch (StatusRuntimeException e) {

            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testResponse(CC1ServiceBlockingStub stub) throws Exception {
        GeneralEntityMessage.Builder messageBuilder = GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = messageBuilder.build();
        try {
            GeneralReturnMessage response = stub.getResponse(gem);
            dev_resteasy_grpc_example___CC7 cc7 = dev_resteasy_grpc_example___CC7.newBuilder()
                    .setM(11)
                    .setS("cc7")
                    .build();
            Any any = response.getAnyField();
            Assertions.assertEquals(cc7, any.unpack(dev_resteasy_grpc_example___CC7.class));
        } catch (StatusRuntimeException e) {

            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testSuspend(CC1ServiceBlockingStub stub) throws Exception {
        dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage.Builder messageBuilder = dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage
                .newBuilder();
        messageBuilder.setURL("http://localhost:8080/p/suspend");
        GeneralEntityMessage gem = messageBuilder.build();
        try {
            GeneralReturnMessage response = stub.suspend(gem);
            Any any = response.getAnyField();
            gString gS = any.unpack(gString.class);
            String s = gS.getValue();
            Assertions.assertEquals("suspend", s);
        } catch (StatusRuntimeException e) {

            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testCompletionStage(CC1ServiceBlockingStub stub) throws Exception {
        dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage.Builder messageBuilder = dev.resteasy.grpc.example.CC1_proto.GeneralEntityMessage
                .newBuilder();
        messageBuilder.setURL("http://localhost:8080/p/async/cs");
        GeneralEntityMessage gem = messageBuilder.build();
        try {
            GeneralReturnMessage response = stub.getResponseCompletionStage(gem);
            Assertions.assertEquals("cs", response.getGStringField().getValue());
        } catch (StatusRuntimeException e) {

            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testServletContextPath(CC1ServiceBlockingStub stub) throws Exception {
        GeneralEntityMessage.Builder messageBuilder = GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = messageBuilder.build();
        GeneralReturnMessage response;
        try {
            response = stub.contextPath(gem);
            Assertions.assertEquals("/grpc-test", response.getGStringField().getValue());
        } catch (StatusRuntimeException e) {

            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testServletContextInitParam(CC1ServiceBlockingStub stub) throws Exception {
        GeneralEntityMessage.Builder messageBuilder = GeneralEntityMessage.newBuilder();
        messageBuilder.setURL("http://localhost:8080/p/servletContext");
        GeneralEntityMessage gem = messageBuilder.build();
        GeneralReturnMessage response;
        try {
            response = stub.servletContext(gem);
            Assertions.assertEquals("/grpcToJakartaRest", response.getGStringField().getValue());
        } catch (StatusRuntimeException e) {

            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testServletConfigServletName(CC1ServiceBlockingStub stub) throws Exception {
        GeneralEntityMessage.Builder messageBuilder = GeneralEntityMessage.newBuilder();
        messageBuilder.setURL("http://localhost:8080/p/servletConfig");
        GeneralEntityMessage gem = messageBuilder.build();
        GeneralReturnMessage response;
        try {
            response = stub.servletConfig(gem);
            Assertions.assertEquals("GrpcServlet", response.getGStringField().getValue());
        } catch (StatusRuntimeException e) {

            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testSSE(CC1ServiceBlockingStub stub) throws Exception {
        CC1_proto.GeneralEntityMessage.Builder messageBuilder = CC1_proto.GeneralEntityMessage.newBuilder();
        messageBuilder.setURL("http://localhost:8080/p/sse");
        GeneralEntityMessage gem = messageBuilder.build();
        Iterator<CC1_proto.dev_resteasy_grpc_bridge_runtime_sse___SseEvent> response;
        try {
            response = stub.sse(gem);
        } catch (StatusRuntimeException e) {
            Assertions.fail("fail");
            return;
        }
        ArrayList<CC1_proto.dev_resteasy_grpc_bridge_runtime_sse___SseEvent> list = new ArrayList<CC1_proto.dev_resteasy_grpc_bridge_runtime_sse___SseEvent>();
        while (response.hasNext()) {
            CC1_proto.dev_resteasy_grpc_bridge_runtime_sse___SseEvent sseEvent = response.next();
            list.add(sseEvent);
        }
        Assertions.assertEquals(4, list.size());
        for (int k = 0; k < 3; k++) {
            CC1_proto.dev_resteasy_grpc_bridge_runtime_sse___SseEvent sseEvent = list.get(k);
            Assertions.assertEquals("name" + (k + 1), sseEvent.getName());
            Any any = sseEvent.getData();
            gString gString = any.unpack(gString.class);
            Assertions.assertEquals("event" + (k + 1), gString.getValue());
        }
        CC1_proto.dev_resteasy_grpc_bridge_runtime_sse___SseEvent sseEvent = list.get(3);
        Assertions.assertEquals("name4", sseEvent.getName());
        Any any = sseEvent.getData();
        dev_resteasy_grpc_example___CC5 cc5 = (dev_resteasy_grpc_example___CC5) any
                .unpack(dev_resteasy_grpc_example___CC5.class);
        Assertions.assertEquals(dev_resteasy_grpc_example___CC5.newBuilder().setK(4).build(), cc5);
    }

    void testInheritance(CC1ServiceBlockingStub stub) throws Exception {
        dev_resteasy_grpc_example___CC2 cc2 = dev_resteasy_grpc_example___CC2.newBuilder()
                .setJ(17)
                .setS("thag")
                .build();
        GeneralEntityMessage.Builder messageBuilder = GeneralEntityMessage.newBuilder();
        messageBuilder.setURL("http://localhost:8080/p/inheritance").setDevResteasyGrpcExampleCC2Field(cc2);
        GeneralEntityMessage gem = messageBuilder.build();
        GeneralReturnMessage response;
        try {
            response = stub.inheritance(gem);
            cc2 = dev_resteasy_grpc_example___CC2.newBuilder().setJ(18).setS("xthagy").build();
            Assertions.assertEquals(cc2, response.getDevResteasyGrpcExampleCC2Field());
        } catch (StatusRuntimeException e) {

            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testReferenceField(CC1ServiceBlockingStub stub) throws Exception {
        dev_resteasy_grpc_example___CC5 cc5 = dev_resteasy_grpc_example___CC5.newBuilder().setK(11).build();
        dev_resteasy_grpc_example___CC4 cc4 = dev_resteasy_grpc_example___CC4.newBuilder()
                .setS("grog")
                .setCc5(cc5)
                .build();
        GeneralEntityMessage.Builder messageBuilder = GeneralEntityMessage.newBuilder();
        messageBuilder.setURL("http://localhost:8080/p/reference").setDevResteasyGrpcExampleCC4Field(cc4);
        GeneralEntityMessage gem = messageBuilder.build();
        GeneralReturnMessage response;
        try {
            response = stub.referenceField(gem);
            cc5 = dev_resteasy_grpc_example___CC5.newBuilder().setK(12).build();
            cc4 = dev_resteasy_grpc_example___CC4.newBuilder().setS("xgrogy").setCc5(cc5).build();
            Assertions.assertEquals(cc4, response.getDevResteasyGrpcExampleCC4Field());
        } catch (StatusRuntimeException e) {

            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testServletInfo(CC1ServiceBlockingStub stub) throws Exception {
        ServletInfo servletInfo = ServletInfo.newBuilder()
                .setCharacterEncoding("utf-16")
                .setClientAddress("1.2.3.4")
                .setClientHost("bluemonkey")
                .setClientPort(7777).build();
        gString gstring = gString.newBuilder().setValue("servletInfo").build();
        GeneralEntityMessage gem = GeneralEntityMessage.newBuilder()
                .setURL("http://localhost:8080/p/servletInfo")
                .setServletInfo(servletInfo)
                .setGStringField(gstring).build();
        try {
            GeneralReturnMessage response = stub.testServletInfo(gem);
            Assertions.assertEquals("UTF-16|1.2.3.5|BLUEMONKEY|7778", response.getGStringField().getValue());
        } catch (StatusRuntimeException e) {

            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    /**
     * Clarify treatment of cookies
     */
    void testServerCookies(CC1ServiceBlockingStub stub) throws Exception {
        GeneralEntityMessage.Builder messageBuilder = GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = messageBuilder.build();
        GeneralReturnMessage response;
        try {
            response = stub.serverCookies(gem);
            List<gNewCookie> list = response.getCookiesList();
            Assertions.assertEquals(2, list.size());
            gNewCookie c1 = gNewCookie.newBuilder()
                    .setDomain("d1")
                    .setMaxAge(-1)
                    .setName("n1")
                    .setPath("p1")
                    .setValue("v1")
                    .build();
            gNewCookie c2 = gNewCookie.newBuilder()
                    .setDomain("d2")
                    .setMaxAge(17)
                    .setName("n2")
                    .setPath("p2")
                    .setValue("v2")
                    .setHttpOnly(true)
                    .setSecure(true)
                    .build();
            if ("n1".equals(list.get(0).getName())) {
                Assertions.assertEquals(c1, list.get(0));
                Assertions.assertEquals(c2, list.get(1));
            } else {
                Assertions.assertEquals(c1, list.get(1));
                Assertions.assertEquals(c2, list.get(0));
            }
            Assertions.assertEquals("cookies", response.getGStringField().getValue());
        } catch (StatusRuntimeException e) {

            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testServerHeaders(CC1ServiceBlockingStub stub) throws Exception {
        GeneralEntityMessage.Builder messageBuilder = GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = messageBuilder.build();
        GeneralReturnMessage response;
        try {
            response = stub.serverHeaders(gem);
            Map<String, CC1_proto.gHeader> headers = response.getHeadersMap();
            gHeader gh1 = gHeader.newBuilder().addValues("v1a").addValues("v1b").build();
            Assertions.assertEquals(gh1, headers.get("h1"));
            gHeader gh2 = gHeader.newBuilder().addValues("v2").build();
            Assertions.assertEquals(gh2, headers.get("h2"));
            Assertions.assertEquals("headers", response.getGStringField().getValue());
        } catch (StatusRuntimeException e) {

            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testServletPath(CC1ServiceBlockingStub stub) throws Exception {
        String contextPath;
        {
            GeneralEntityMessage.Builder messageBuilder = GeneralEntityMessage.newBuilder();
            GeneralEntityMessage gem = messageBuilder.build();
            GeneralReturnMessage response;
            try {
                response = stub.servletPath(gem);
                String result = response.getGStringField().getValue();

                // get context path
                int i = result.indexOf('|');
                contextPath = result.substring(0, i);

                // servlet path
                int j = result.indexOf('|', i + 1);
                Assertions.assertEquals("", result.substring(i + 1, j));

                // path
                i = j + 1;
                j = result.indexOf('|', i);
                Assertions.assertEquals("/p/servletPath", result.substring(i, j));

                // HttpServletRequest.getPathTranslated()
                Assertions.assertTrue(result.substring(j + 1)
                        .contains(File.separator + "p" + File.separator + "servletPath"));
            } catch (StatusRuntimeException e) {
                Assertions.fail("fail");
                return;
            }
        }
        {
            GeneralEntityMessage.Builder messageBuilder = GeneralEntityMessage.newBuilder();
            GeneralEntityMessage gem = messageBuilder
                    .setURL("http://localhost:8080" + contextPath + "/grpcToJakartaRest/p/servletPath").build();
            GeneralReturnMessage response;
            try {
                response = stub.servletPath(gem);
                String result = response.getGStringField().getValue();

                // context path
                int i = result.indexOf('|');
                Assertions.assertEquals(contextPath, result.substring(0, i));

                // servlet path
                int j = result.indexOf('|', i + 1);
                Assertions.assertEquals("/grpcToJakartaRest", result.substring(i + 1, j));

                // path
                i = j + 1;
                j = result.indexOf('|', i);
                Assertions.assertEquals("/p/servletPath", result.substring(i, j));

                // HttpServletRequest.getPathTranslated()
                Assertions.assertTrue(result.substring(j + 1)
                        .contains(File.separator + "p" + File.separator + "servletPath"));
            } catch (StatusRuntimeException e) {
                Assertions.fail("fail");
                return;
            }
        }
    }

    void testServletParams(CC1ServiceBlockingStub stub) throws Exception {
        Map<String, FormValues> formMap = new HashMap<String, FormValues>();
        FormValues.Builder formValuesBuilderP2 = FormValues.newBuilder();
        formValuesBuilderP2.addFormValuesField("f2a").addFormValuesField("f2b");
        formMap.put("p2", formValuesBuilderP2.build());

        FormValues.Builder formValuesBuilderP3 = FormValues.newBuilder();
        formValuesBuilderP3.addFormValuesField("f3a").addFormValuesField("f3b");
        formMap.put("p3", formValuesBuilderP3.build());

        FormMap.Builder formMapBuilder = FormMap.newBuilder();
        formMapBuilder.putAllFormMapField(formMap);
        GeneralEntityMessage.Builder messageBuilder = GeneralEntityMessage.newBuilder();
        messageBuilder.setFormField(formMapBuilder.build());

        messageBuilder.setURL("http://localhost:8080/p/servletParams?p1=q1&p2=q2");
        GeneralEntityMessage gem = messageBuilder.build();
        GeneralReturnMessage response;
        try {
            response = stub.servletParams(gem);
            String s = response.getGStringField().getValue();
            Assertions.assertTrue(s.startsWith("q1|q2|f2a|f3a"));
            Assertions.assertTrue(s.contains("p1->q1"));
            Assertions.assertTrue(s.contains("p2->f2af2bq2"));
            Assertions.assertTrue(s.contains("p3->f3af3b"));
        } catch (StatusRuntimeException e) {

            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    /**
     * Clarify treatment of cookies
     */
    void testJaxrsResponse(CC1ServiceBlockingStub stub) throws Exception {
        GeneralEntityMessage.Builder messageBuilder = GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = messageBuilder.build();
        GeneralReturnMessage response;
        try {
            response = stub.jaxrsResponse(gem);
            Assertions.assertEquals(2, response.getCookiesCount());
            gNewCookie expectedCookie1 = gNewCookie.newBuilder()
                    .setDomain("d1")
                    .setName("n1")
                    .setPath("p1")
                    .setValue("v1")
                    .setMaxAge(11)
                    .setExpiry(Timestamp.newBuilder().setSeconds(111))
                    .setHttpOnly(true)
                    .setVersion(1)
                    .build();
            gNewCookie expectedCookie2 = gNewCookie.newBuilder()
                    .setDomain("d2")
                    .setName("n2")
                    .setPath("p2")
                    .setValue("v2")
                    .setMaxAge(17)
                    .setExpiry(Timestamp.newBuilder().setSeconds(222))
                    .setSecure(true)
                    .setVersion(1)
                    .build();
            Assertions.assertTrue(expectedCookie1.equals(response.getCookies(0))
                    && expectedCookie2.equals(response.getCookies(1))
                    || expectedCookie1.equals(response.getCookies(1)) && expectedCookie2.equals(response.getCookies(0)));
            Map<String, CC1_proto.gHeader> headers = response.getHeadersMap();
            Assertions.assertEquals(1, headers.get("h1").getValuesCount());
            Assertions.assertEquals("v1", headers.get("h1").getValues(0));
            Assertions.assertEquals(222, response.getStatus());
            Assertions.assertEquals(1, headers.get("Content-Type").getValuesCount());
            Assertions.assertEquals("x/y", headers.get("Content-Type").getValues(0));
        } catch (StatusRuntimeException e) {

            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testServletResponse(CC1ServiceBlockingStub stub) throws Exception {
        GeneralEntityMessage.Builder messageBuilder = GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = messageBuilder.build();
        GeneralReturnMessage response;
        try {
            response = stub.servletResponse(gem);
            Map<String, CC1_proto.gHeader> headers = response.getHeadersMap();

            Assertions.assertEquals(1, headers.get("d1").getValuesCount());
            Assertions.assertEquals(1, headers.get("h1").getValuesCount());
            Assertions.assertEquals(1, headers.get("i1").getValuesCount());

            Assertions.assertEquals(2, headers.get("d2").getValuesCount());
            Assertions.assertEquals(2, headers.get("h2").getValuesCount());
            Assertions.assertEquals(2, headers.get("i2").getValuesCount());

            Assertions.assertEquals(1, headers.get("d3").getValuesCount());
            Assertions.assertEquals(1, headers.get("h3").getValuesCount());
            Assertions.assertEquals(1, headers.get("i3").getValuesCount());

            Assertions.assertTrue(headers.get("d1").getValues(0).contains("02 Jan 1970"));
            Assertions.assertEquals("v1", headers.get("h1").getValues(0));
            Assertions.assertEquals("13", headers.get("i1").getValues(0));

            Assertions.assertTrue(headers.get("d2").getValues(0).contains("03 Jan 1970"));
            Assertions.assertTrue(headers.get("d2").getValues(1).contains("04 Jan 1970"));
            Assertions.assertEquals("v2a", headers.get("h2").getValues(0));
            Assertions.assertEquals("v2b", headers.get("h2").getValues(1));
            Assertions.assertEquals("19", headers.get("i2").getValues(0));
            Assertions.assertEquals("29", headers.get("i2").getValues(1));

            Assertions.assertTrue(headers.get("d3").getValues(0).contains("06 Jan 1970"));
            Assertions.assertEquals("v3b", headers.get("h3").getValues(0));
            Assertions.assertEquals("41", headers.get("i3").getValues(0));

            Assertions.assertEquals(1, response.getCookiesCount());
            gNewCookie expectedCookie = gNewCookie.newBuilder().setDomain("d1").setMaxAge(3).setName("n1").setPath("p1")
                    .setValue("v1").build();
            Assertions.assertEquals(expectedCookie, response.getCookies(0));

            Assertions.assertEquals(223, response.getStatus());
        } catch (StatusRuntimeException e) {
            Assertions.fail("fail 2");
            return;
        }
    }

    void testInnerClass(CC1ServiceBlockingStub stub) throws Exception {
        GeneralEntityMessage.Builder messageBuilder = GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = messageBuilder.build();
        GeneralReturnMessage response;
        try {
            response = stub.inner(gem);
            CC1_proto.dev_resteasy_grpc_example_CC1_INNER_InnerClass.Builder builder = CC1_proto.dev_resteasy_grpc_example_CC1_INNER_InnerClass
                    .newBuilder();
            CC1_proto.dev_resteasy_grpc_example_CC1_INNER_InnerClass inner = builder.setI(3).setS("three").build();
            Assertions.assertEquals(inner, response.getDevResteasyGrpcExampleCC1INNERInnerClassField());
        } catch (StatusRuntimeException e) {

            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testLocatorGet(CC1ServiceBlockingStub stub) throws Exception {
        GeneralEntityMessage.Builder messageBuilder = GeneralEntityMessage.newBuilder();
        messageBuilder.setURL("/p/locator/get").setHttpMethod("GET");
        GeneralEntityMessage gem = messageBuilder.build();
        GeneralReturnMessage response;
        try {
            response = stub.locator(gem);
            Assertions.assertEquals("/p/locator/get",
                    response.getAnyField().unpack(CC1_proto.gString.class).getValue());
        } catch (Exception e) {

            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testLocatorPost(CC1ServiceBlockingStub stub) throws Exception {
        GeneralEntityMessage.Builder messageBuilder = GeneralEntityMessage.newBuilder();
        messageBuilder.setURL("/p/locator/post/abc").setHttpMethod("POST");
        messageBuilder.setAnyField(Any.pack(gString.newBuilder().setValue("xyz").build()));
        GeneralEntityMessage gem = messageBuilder.build();
        GeneralReturnMessage response;
        try {
            response = stub.locator(gem);
            Assertions.assertEquals("abc|xyz", response.getAnyField()
                    .unpack(CC1_proto.gString.class)
                    .getValue());
        } catch (Exception e) {

            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testCopy(CC1ServiceBlockingStub stub) throws Exception {
        CC1_proto.gString n = CC1_proto.gString.newBuilder().setValue("abc").build();
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setURL("http://localhost:8080" + "/p/copy").setGStringField(n).build();
        GeneralReturnMessage response;
        try {
            response = stub.copy(gem);
            CC1_proto.gString expected = dev.resteasy.grpc.example.CC1_proto.gString.newBuilder()
                    .setValue("abc")
                    .build();
            Assertions.assertEquals(expected, response.getGStringField());
        } catch (StatusRuntimeException e) {

            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testInterfaceEntity(CC1ServiceBlockingStub stub) throws Exception {
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        CC1_proto.dev_resteasy_grpc_example___IntfImpl entity = CC1_proto.dev_resteasy_grpc_example___IntfImpl.newBuilder()
                .setS("abc").build();
        Any entityAny = Any.pack(entity);
        GeneralEntityMessage gem = builder.setURL("http://localhost:8080" + "/p/interface/entity")
                .setAnyField(entityAny)
                .build();
        GeneralReturnMessage response;
        try {
            response = stub.intfEntity(gem);
            gString gs = response.getGStringField();
            Assertions.assertEquals("abc", gs.getValue());
        } catch (StatusRuntimeException e) {
            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testInterfaceReturn(CC1ServiceBlockingStub stub) throws Exception {
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setURL("http://localhost:8080" + "/p/interface/return").build();
        GeneralReturnMessage response;
        try {
            response = stub.intfReturn(gem);
            Any any = response.getAnyField();
            Class clazz = Utility.extractTypeFromAny(any, CC1_proto.class.getClassLoader(), "CC1_proto");
            dev_resteasy_grpc_example___IntfImpl impl = (dev_resteasy_grpc_example___IntfImpl) any.unpack(clazz);
            Assertions.assertEquals("xyz", impl.getS());
        } catch (StatusRuntimeException e) {
            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testInnerPublicPublic(CC1ServiceBlockingStub stub) throws Exception {
        InnerClasses.PublicPublic pp = new InnerClasses.PublicPublic(3);
        dev_resteasy_grpc_example_InnerClasses_INNER_PublicPublic m = (dev_resteasy_grpc_example_InnerClasses_INNER_PublicPublic) translator
                .translateToJavabuf(pp);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setURL("http://localhost:8080" + "/p/inner/public/public")
                .setDevResteasyGrpcExampleInnerClassesINNERPublicPublicField(m).build();
        GeneralReturnMessage response;
        try {
            response = stub.publicPublic(gem);
            dev_resteasy_grpc_example_InnerClasses_INNER_PublicPublic result = response
                    .getDevResteasyGrpcExampleInnerClassesINNERPublicPublicField();
            Assertions.assertTrue(pp.equals(translator.translateFromJavabuf(result)));
        } catch (StatusRuntimeException e) {
            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testInnerPublicPrivate(CC1ServiceBlockingStub stub) throws Exception {
        Class<?> clazz = null;
        for (Class<?> c : InnerClasses.class.getDeclaredClasses()) {
            if ("dev.resteasy.grpc.example.InnerClasses$PublicPrivate".equals(c.getName())) {
                clazz = c;
                break;
            }
        }
        Constructor<?> cons = clazz.getDeclaredConstructor(int.class);
        cons.setAccessible(true);
        InnerClasses.PublicPrivate pp = (InnerClasses.PublicPrivate) cons.newInstance(5);
        dev_resteasy_grpc_example_InnerClasses_INNER_PublicPrivate m = (dev_resteasy_grpc_example_InnerClasses_INNER_PublicPrivate) translator
                .translateToJavabuf(pp);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setURL("http://localhost:8080" + "/p/inner/public/private")
                .setDevResteasyGrpcExampleInnerClassesINNERPublicPrivateField(m).build();
        GeneralReturnMessage response;
        try {
            response = stub.publicPrivate(gem);
            dev_resteasy_grpc_example_InnerClasses_INNER_PublicPrivate result = response
                    .getDevResteasyGrpcExampleInnerClassesINNERPublicPrivateField();
            Assertions.assertTrue(pp.equals(translator.translateFromJavabuf(result)));
        } catch (StatusRuntimeException e) {
            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testInnerHolder(CC1ServiceBlockingStub stub) throws Exception {
        InnerClasses.InnerClassHolder ich = new InnerClasses.InnerClassHolder(3);
        dev_resteasy_grpc_example_InnerClasses_INNER_InnerClassHolder m = (dev_resteasy_grpc_example_InnerClasses_INNER_InnerClassHolder) translator
                .translateToJavabuf(ich);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setURL("http://localhost:8080" + "/p/inner/holder")
                .setDevResteasyGrpcExampleInnerClassesINNERInnerClassHolderField(m).build();
        GeneralReturnMessage response;
        try {
            response = stub.innerClassHolder(gem);
            dev_resteasy_grpc_example_InnerClasses_INNER_InnerClassHolder result = response
                    .getDevResteasyGrpcExampleInnerClassesINNERInnerClassHolderField();
            Assertions.assertTrue(ich.equals(translator.translateFromJavabuf(result)));
        } catch (StatusRuntimeException e) {
            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testGenericWildcardObject(CC1ServiceBlockingStub stub) throws Exception {
        CC10<Integer> cc10 = new CC10<Integer>(17);
        GenericType<CC10<Object>> type = new GenericType<CC10<Object>>() {
        };
        Message m = translator.translateToJavabuf(cc10, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = ((GeneralEntityMessage.Builder) JavabufClassTranslator
                .getSetter("dev.resteasy.grpc.example.CC10<java.lang.Object>")
                .invoke(builder, m)).setURL("http://localhost:8080" + "/generic/cc10/wildcard").build();
        GeneralReturnMessage response;
        try {
            response = stub.cc10Wildcrd(gem);
            Message result = (Message) JavabufClassTranslator.getGetter("dev.resteasy.grpc.example.CC10<java.lang.Object>")
                    .invoke(response);
            CC10<Integer> cc10a = (CC10<Integer>) translator.translateFromJavabuf(result);
            Assertions.assertTrue(Integer.valueOf(17).equals(((Integer) cc10a.getT())));
        } catch (StatusRuntimeException e) {
            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testGenericWildcardWildcard(CC1ServiceBlockingStub stub) throws Exception {
        CC10<Integer> cc10 = new CC10<Integer>(17);
        GenericType<CC10<?>> type = new GenericType<CC10<?>>() {
        };
        Message m = translator.translateToJavabuf(cc10, translator.normalize(type));
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = ((GeneralEntityMessage.Builder) JavabufClassTranslator
                .getSetter("dev.resteasy.grpc.example.CC10<java.lang.Object>")
                .invoke(builder, m)).setURL("http://localhost:8080" + "/generic/cc10/wildcard").build();
        GeneralReturnMessage response;
        try {
            response = stub.cc10Wildcrd(gem);
            Message result = (Message) JavabufClassTranslator.getGetter("dev.resteasy.grpc.example.CC10<java.lang.Object>")
                    .invoke(response);
            CC10<?> cc10a = (CC10<?>) translator.translateFromJavabuf(result);
            Assertions.assertTrue(Integer.valueOf(17).equals(((Integer) cc10a.getT())));
        } catch (StatusRuntimeException e) {
            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testGenericVariableWildcard(CC1ServiceBlockingStub stub) throws Exception {
        CC10<Integer> cc10 = new CC10<Integer>(17);
        GenericType<CC10<?>> type = new GenericType<CC10<?>>() {
        };
        Message m = translator.translateToJavabuf(cc10, translator.normalize(type));
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = ((GeneralEntityMessage.Builder) JavabufClassTranslator
                .getSetter("dev.resteasy.grpc.example.CC10<java.lang.Object>")
                .invoke(builder, m)).setURL("http://localhost:8080" + "/generic/cc10/variable").build();
        GeneralReturnMessage response;
        try {
            response = stub.cc10Variable(gem);
            Message result = (Message) JavabufClassTranslator.getGetter("dev.resteasy.grpc.example.CC10<java.lang.Object>")
                    .invoke(response);
            CC10<?> cc10a = (CC10<?>) translator.translateFromJavabuf(result);
            Assertions.assertTrue(Integer.valueOf(17).equals(((Integer) cc10a.getT())));
        } catch (StatusRuntimeException e) {
            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testGenericVariableObject(CC1ServiceBlockingStub stub) throws Exception {
        CC10<Integer> cc10 = new CC10<Integer>(17);
        GenericType<CC10<Object>> type = new GenericType<CC10<Object>>() {
        };
        Message m = translator.translateToJavabuf(cc10, translator.normalize(type));
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = ((GeneralEntityMessage.Builder) JavabufClassTranslator
                .getSetter("dev.resteasy.grpc.example.CC10<java.lang.Object>")
                .invoke(builder, m)).setURL("http://localhost:8080" + "/generic/cc10/variable").build();
        GeneralReturnMessage response;
        try {
            response = stub.cc10Variable(gem);
            Message result = (Message) JavabufClassTranslator.getGetter("dev.resteasy.grpc.example.CC10<java.lang.Object>")
                    .invoke(response);
            CC10<Integer> cc10a = (CC10<Integer>) translator.translateFromJavabuf(result);
            Assertions.assertTrue(Integer.valueOf(17).equals(((Integer) cc10a.getT())));
        } catch (StatusRuntimeException e) {
            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testGenericInteger(CC1ServiceBlockingStub stub) throws Exception {
        CC10<Integer> cc10 = new CC10<Integer>(17);
        GenericType<CC10<Integer>> type = new GenericType<CC10<Integer>>() {
        };
        Message m = translator.translateToJavabuf(cc10, translator.normalize(type));
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = ((GeneralEntityMessage.Builder) JavabufClassTranslator
                .getSetter("dev.resteasy.grpc.example.CC10<java.lang.Integer>")
                .invoke(builder, m)).setURL("http://localhost:8080" + "/generic/cc10/Integer").build();
        GeneralReturnMessage response;
        try {
            response = stub.cc10Integer(gem);
            Message result = (Message) JavabufClassTranslator.getGetter("dev.resteasy.grpc.example.CC10<java.lang.Integer>")
                    .invoke(response);
            CC10<Integer> cc10a = (CC10<Integer>) translator.translateFromJavabuf(result);
            Assertions.assertTrue(Integer.valueOf(17).equals(((Integer) cc10a.getT())));
        } catch (StatusRuntimeException e) {
            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testGenericFloat(CC1ServiceBlockingStub stub) throws Exception {
        CC10<Float> cc10 = new CC10<Float>(19.0F);
        GenericType<CC10<Float>> type = new GenericType<CC10<Float>>() {
        };
        Message m = translator.translateToJavabuf(cc10, translator.normalize(type));
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = ((GeneralEntityMessage.Builder) JavabufClassTranslator
                .getSetter("dev.resteasy.grpc.example.CC10<java.lang.Float>")
                .invoke(builder, m)).setURL("http://localhost:8080" + "/generic/cc10/Float").build();
        GeneralReturnMessage response;
        try {
            response = stub.cc10Float(gem);
            Message result = (Message) JavabufClassTranslator.getGetter("dev.resteasy.grpc.example.CC10<java.lang.Float>")
                    .invoke(response);
            CC10<Float> cc10a = (CC10<Float>) translator.translateFromJavabuf(result);
            Assertions.assertTrue(Float.valueOf(19.0F).equals(((Float) cc10a.getT())));
        } catch (StatusRuntimeException e) {
            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    /*
     * Map<String, String> map = new HashMap<String, String>();
     * map.put("abc", "xyz")
     * CC10<Map<String, String>> cc10 = new CC10<Map<String, String>>();
     * GenericType<dev.resteasy.grpc.example.CC10<java.util.Map<java.lang.String, java.lang.String>>> type
     * = new GenericType<dev.resteasy.grpc.example.CC10<java.util.Map<java.lang.String, java.lang.String>>> () {
     * };
     */
    void testGenericMap(CC1ServiceBlockingStub stub) throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        map.put("abc", "xyz");
        CC10<Map<String, String>> cc10 = new CC10<Map<String, String>>(map);
        GenericType<dev.resteasy.grpc.example.CC10<java.util.HashMap<java.lang.String, java.lang.String>>> type = new GenericType<dev.resteasy.grpc.example.CC10<java.util.HashMap<java.lang.String, java.lang.String>>>() {
        };
        Message m = translator.translateToJavabuf(cc10, translator.normalize(type));
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = ((GeneralEntityMessage.Builder) JavabufClassTranslator
                .getSetter("dev.resteasy.grpc.example.CC10<java.util.HashMap<java.lang.String,java.lang.String>>")
                .invoke(builder, m)).setURL("http://localhost:8080" + "/generic/cc10/map").build();
        GeneralReturnMessage response;
        try {
            response = stub.cc10Map(gem);
            Message result = (Message) JavabufClassTranslator
                    .getGetter("dev.resteasy.grpc.example.CC10<java.util.HashMap<java.lang.String,java.lang.String>>")
                    .invoke(response);
            CC10<java.util.Map<java.lang.String, java.lang.String>> cc10a = (CC10<java.util.Map<java.lang.String, java.lang.String>>) translator
                    .translateFromJavabuf(result);
            Assertions.assertTrue(cc10.equals(cc10a));
        } catch (StatusRuntimeException e) {
            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testGenericWildcardLowerObject(CC1ServiceBlockingStub stub) throws Exception {
        CC10<TestSubClass> cc10 = new CC10<TestSubClass>(new TestSubClass("forty-one", 43));
        GenericType<CC10<Object>> type = new GenericType<CC10<Object>>() {
        };
        Message m = translator.translateToJavabuf(cc10, translator.normalize(type));
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = ((GeneralEntityMessage.Builder) JavabufClassTranslator
                .getSetter("dev.resteasy.grpc.example.CC10<java.lang.Object>")
                .invoke(builder, m)).setURL("http://localhost:8080" + "/generic/cc10/wildcard/lower").build();
        GeneralReturnMessage response;
        try {
            response = stub.cc10WildcardLower(gem);
            Message result = (Message) JavabufClassTranslator.getGetter("dev.resteasy.grpc.example.CC10<java.lang.Object>")
                    .invoke(response);
            CC10<TestSubClass> cc10a = (CC10<TestSubClass>) translator.translateFromJavabuf(result);
            Assertions.assertTrue(cc10.getT().equals(cc10a.getT()));
        } catch (StatusRuntimeException e) {
            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testGenericWildcardLowerWildcard(CC1ServiceBlockingStub stub) throws Exception {
        CC10<TestSubClass> cc10 = new CC10<TestSubClass>(new TestSubClass("forty-one", 43));
        GenericType<CC10<?>> type = new GenericType<CC10<?>>() {
        };
        Message m = translator.translateToJavabuf(cc10, translator.normalize(type));
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = ((GeneralEntityMessage.Builder) JavabufClassTranslator
                .getSetter("dev.resteasy.grpc.example.CC10<java.lang.Object>")
                .invoke(builder, m)).setURL("http://localhost:8080" + "/generic/cc10/wildcard/lower").build();
        GeneralReturnMessage response;
        try {
            response = stub.cc10WildcardLower(gem);
            Message result = (Message) JavabufClassTranslator.getGetter("dev.resteasy.grpc.example.CC10<java.lang.Object>")
                    .invoke(response);
            CC10<?> cc10a = (CC10<?>) translator.translateFromJavabuf(result);
            Assertions.assertTrue(cc10.getT().equals(cc10a.getT()));
        } catch (StatusRuntimeException e) {
            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testGenericVariableUpperTestClass(CC1ServiceBlockingStub stub) throws Exception {
        CC10<TestClass> cc10 = new CC10<TestClass>(new TestClass("forty-one"));
        GenericType<CC10<Object>> type = new GenericType<CC10<Object>>() {
        };
        Message m = translator.translateToJavabuf(cc10, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = ((GeneralEntityMessage.Builder) JavabufClassTranslator
                .getSetter("dev.resteasy.grpc.example.CC10<java.lang.Object>")
                .invoke(builder, m)).setURL("http://localhost:8080" + "/generic/cc10/variable/upper").build();
        GeneralReturnMessage response;
        try {
            response = stub.cc10VariableUpper(gem);
            Message result = (Message) JavabufClassTranslator
                    .getGetter("dev.resteasy.grpc.example.CC10<java.lang.Object>").invoke(response);
            CC10<TestClass> cc10a = (CC10<TestClass>) translator.translateFromJavabuf(result);
            Assertions.assertTrue(cc10.getT().getS().equals(cc10a.getT().getS()));
        } catch (StatusRuntimeException e) {
            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testGenericVariablePrimitiveArray(CC1ServiceBlockingStub stub) throws Exception {
        CC10<int[]> cc10 = new CC10<int[]>(new int[] { 3, 5, 7 });
        GenericType<CC10<int[]>> type = new GenericType<CC10<int[]>>() {
        };
        Message m = translator.translateToJavabuf(cc10, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = ((GeneralEntityMessage.Builder) JavabufClassTranslator
                .getSetter("dev.resteasy.grpc.example.CC10<int[]>")
                .invoke(builder, m)).setURL("http://localhost:8080" + "/generic/cc10/variable/array/primitive").build();
        GeneralReturnMessage response;
        try {
            response = stub.cc10VariableArrayPrimitive(gem);
            Message result = (Message) JavabufClassTranslator
                    .getGetter("dev.resteasy.grpc.example.CC10<int[]>").invoke(response);
            CC10<int[]> cc10a = (CC10<int[]>) translator.translateFromJavabuf(result);
            Assertions.assertTrue(Arrays.equals((int[]) cc10.getT(), (int[]) cc10a.getT()));
        } catch (StatusRuntimeException e) {
            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testGenericVariableReferenceArray(CC1ServiceBlockingStub stub) throws Exception {
        CC10<CC5[]> cc10 = new CC10<CC5[]>(new CC5[] { new CC5(7), null, new CC5(11) });
        GenericType<CC10<CC5[]>> type = new GenericType<CC10<CC5[]>>() {
        };
        Message m = translator.translateToJavabuf(cc10, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = ((GeneralEntityMessage.Builder) JavabufClassTranslator
                .getSetter("dev.resteasy.grpc.example.CC10<dev.resteasy.grpc.example.CC5[]>")
                .invoke(builder, m)).setURL("http://localhost:8080" + "/generic/cc10/variable/array/reference").build();
        GeneralReturnMessage response;
        try {
            response = stub.cc10VariableArrayReference(gem);
            Message result = (Message) JavabufClassTranslator
                    .getGetter("dev.resteasy.grpc.example.CC10<dev.resteasy.grpc.example.CC5[]>").invoke(response);
            CC10<CC5[]> cc10a = (CC10<CC5[]>) translator.translateFromJavabuf(result);
            CC5[] cc5a = cc10.getT();
            CC5[] cc5b = cc10a.getT();
            Assertions.assertTrue(Arrays.deepEquals((CC5[]) cc10.getT(), (CC5[]) cc10a.getT()));
        } catch (StatusRuntimeException e) {
            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testC11GenericWildcardTestClass(CC1ServiceBlockingStub stub) throws Exception {
        CC11<TestClass> cc11 = new CC11<TestClass>(new TestClass("cde"));
        GenericType<CC11<TestClass>> type = new GenericType<CC11<TestClass>>() {
        };
        Message m = translator.translateToJavabuf(cc11, translator.normalize(type));
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = ((GeneralEntityMessage.Builder) JavabufClassTranslator
                .getSetter("dev.resteasy.grpc.example.CC11<dev.resteasy.grpc.example.TestClass>")
                .invoke(builder, m)).setURL("http://localhost:8080" + "/generic/cc11/wildcard").build();
        GeneralReturnMessage response;
        try {
            response = stub.cc11Wildcard(gem);
            Message result = (Message) JavabufClassTranslator
                    .getGetter("dev.resteasy.grpc.example.CC11<dev.resteasy.grpc.example.TestClass>").invoke(response);
            CC11<TestClass> cc11a = (CC11<TestClass>) translator.translateFromJavabuf(result);
            Assertions.assertTrue(cc11.equals(cc11a));
        } catch (StatusRuntimeException e) {
            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testC11GenericVariableTestClass(CC1ServiceBlockingStub stub) throws Exception {
        CC11<TestClass> cc11 = new CC11<TestClass>(new TestClass("cde"));
        GenericType<CC11<TestClass>> type = new GenericType<CC11<TestClass>>() {
        };
        Message m = translator.translateToJavabuf(cc11, translator.normalize(type));
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = ((GeneralEntityMessage.Builder) JavabufClassTranslator
                .getSetter("dev.resteasy.grpc.example.CC11<dev.resteasy.grpc.example.TestClass>")
                .invoke(builder, m)).setURL("http://localhost:8080" + "/generic/cc11/wildcard").build();
        GeneralReturnMessage response;
        try {
            response = stub.cc11Variable(gem);
            Message result = (Message) JavabufClassTranslator
                    .getGetter("dev.resteasy.grpc.example.CC11<dev.resteasy.grpc.example.TestClass>").invoke(response);
            CC11<TestClass> cc11a = (CC11<TestClass>) translator.translateFromJavabuf(result);
            Assertions.assertTrue(cc11.equals(cc11a));
        } catch (StatusRuntimeException e) {
            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testRecordPerson(CC1ServiceBlockingStub stub) throws Exception {
        Person person = new Person("Bill");
        dev_resteasy_grpc_example___Person m = (dev_resteasy_grpc_example___Person) translator.translateToJavabuf(person);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setDevResteasyGrpcExamplePersonField(m)
                .setURL("http://localhost:8080" + "/p/record/string").build();
        GeneralReturnMessage response;
        try {
            response = stub.recordString(gem);
            Message result = response.getDevResteasyGrpcExamplePersonField();
            Person p2 = (Person) translator.translateFromJavabuf(result);
            Assertions.assertTrue(person.equals(p2));
        } catch (StatusRuntimeException e) {
            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testRecordCC2(CC1ServiceBlockingStub stub) throws Exception {
        CC2 cc2 = new CC2("Bill", 19);
        RecordCC2 r = new RecordCC2(cc2, 23);
        dev_resteasy_grpc_example___RecordCC2 m = (dev_resteasy_grpc_example___RecordCC2) translator.translateToJavabuf(r);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setDevResteasyGrpcExampleRecordCC2Field(m)
                .setURL("http://localhost:8080" + "/p/record/cc2").build();
        GeneralReturnMessage response;
        try {
            response = stub.recordCC2(gem);
            Message result = response.getDevResteasyGrpcExampleRecordCC2Field();
            RecordCC2 r2 = (RecordCC2) translator.translateFromJavabuf(result);
            Assertions.assertTrue(r.equals(r2));
        } catch (StatusRuntimeException e) {
            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    /*
     * GeneralEntityMessage gem = ((GeneralEntityMessage.Builder) JavabufClassTranslator
     * .getSetter("dev.resteasy.grpc.example.CC11<dev.resteasy.grpc.example.TestClass>")
     * .invoke(builder, m)).setURL("http://localhost:8080" + "/generic/cc11/wildcard").build();
     *
     */
    void testRecordWildcard(CC1ServiceBlockingStub stub) throws Exception {
        CC2 cc2 = new CC2("bill", 19);
        CC3 cc3 = new CC3("ted");
        RecordVariable<CC3> r = new RecordVariable<CC3>(cc2, 29, cc3);
        GenericType<RecordVariable<Object>> type = new GenericType<RecordVariable<Object>>() {
        };
        Message m = translator.translateToJavabuf(r, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = ((GeneralEntityMessage.Builder) JavabufClassTranslator
                .getSetter("dev.resteasy.grpc.example.RecordVariable<java.lang.Object>")
                .invoke(builder, m)).setURL("http://localhost:8080" + "/p/record/variable/wildcard").build();
        GeneralReturnMessage response;
        try {
            response = stub.recordVariableWildcard(gem);
            Message result = (Message) JavabufClassTranslator
                    .getGetter("dev.resteasy.grpc.example.RecordVariable<java.lang.Object>").invoke(response);
            RecordVariable<Object> r2 = (RecordVariable<Object>) translator.translateFromJavabuf(result);
            Assertions.assertTrue(r.equals(r2));
        } catch (StatusRuntimeException e) {
            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    /*
     * @POST
     *
     * @Path("record/variable/type")
     * public <T> RecordVariable<T> recordVariableVariable(RecordVariable<T> r) {
     * return r;
     * }
     */
    void testRecordVariable(CC1ServiceBlockingStub stub) throws Exception {
        CC2 cc2 = new CC2("bill", 19);
        CC5 cc5 = new CC5(23);
        RecordVariable<CC5> r = new RecordVariable<CC5>(cc2, 29, cc5);
        GenericType<RecordVariable<Object>> type = new GenericType<RecordVariable<Object>>() {
        };
        Message m = translator.translateToJavabuf(r, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = ((GeneralEntityMessage.Builder) JavabufClassTranslator
                .getSetter("dev.resteasy.grpc.example.RecordVariable<java.lang.Object>")
                .invoke(builder, m)).setURL("http://localhost:8080" + "/p/record/variable/variable").build();
        GeneralReturnMessage response;
        try {
            response = stub.recordVariableVariable(gem);
            Message result = (Message) JavabufClassTranslator
                    .getGetter("dev.resteasy.grpc.example.RecordVariable<java.lang.Object>").invoke(response);
            RecordVariable<Object> r2 = (RecordVariable<Object>) translator.translateFromJavabuf(result);
            Assertions.assertTrue(r.equals(r2));
        } catch (StatusRuntimeException e) {
            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testRecordString(CC1ServiceBlockingStub stub) throws Exception {
        CC2 cc2 = new CC2("bill", 19);
        RecordVariable<String> r = new RecordVariable<String>(cc2, 29, "string");
        GenericType<RecordVariable<String>> type = new GenericType<RecordVariable<String>>() {
        };
        Message m = translator.translateToJavabuf(r, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = ((GeneralEntityMessage.Builder) JavabufClassTranslator
                .getSetter("dev.resteasy.grpc.example.RecordVariable<java.lang.String>")
                .invoke(builder, m)).setURL("http://localhost:8080" + "/p/record/variable/string").build();
        GeneralReturnMessage response;
        try {
            response = stub.recordVariableString(gem);
            Message result = (Message) JavabufClassTranslator
                    .getGetter("dev.resteasy.grpc.example.RecordVariable<java.lang.String>").invoke(response);
            RecordVariable<String> r2 = (RecordVariable<String>) translator.translateFromJavabuf(result);
            Assertions.assertTrue(r.equals(r2));
        } catch (StatusRuntimeException e) {
            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testRecordInteger(CC1ServiceBlockingStub stub) throws Exception {
        CC2 cc2 = new CC2("bill", 19);
        RecordVariable<Integer> r = new RecordVariable<Integer>(cc2, 29, 31);
        GenericType<RecordVariable<Integer>> type = new GenericType<RecordVariable<Integer>>() {
        };
        Message m = translator.translateToJavabuf(r, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = ((GeneralEntityMessage.Builder) JavabufClassTranslator
                .getSetter("dev.resteasy.grpc.example.RecordVariable<java.lang.Integer>")
                .invoke(builder, m)).setURL("http://localhost:8080" + "/p/record/variable/integer").build();
        GeneralReturnMessage response;
        try {
            response = stub.recordVariableInteger(gem);
            Message result = (Message) JavabufClassTranslator
                    .getGetter("dev.resteasy.grpc.example.RecordVariable<java.lang.Integer>").invoke(response);
            RecordVariable<Integer> r2 = (RecordVariable<Integer>) translator.translateFromJavabuf(result);
            Assertions.assertTrue(r.equals(r2));
        } catch (StatusRuntimeException e) {
            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testRecordMap(CC1ServiceBlockingStub stub) throws Exception {
        CC2 cc2 = new CC2("bill", 19);
        Map<String, String> map = new HashMap<String, String>();
        map.put("abc", "xyz");
        RecordVariable<Map> r = new RecordVariable<Map>(cc2, 29, map);
        GenericType<RecordVariable<java.util.HashMap<java.lang.String, java.lang.String>>> type = new GenericType<RecordVariable<java.util.HashMap<java.lang.String, java.lang.String>>>() {
        };
        Message m = translator.translateToJavabuf(r, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = ((GeneralEntityMessage.Builder) JavabufClassTranslator
                .getSetter("dev.resteasy.grpc.example.RecordVariable<java.util.HashMap<java.lang.String,java.lang.String>>")
                .invoke(builder, m)).setURL("http://localhost:8080" + "/p/record/variable/map").build();
        GeneralReturnMessage response;
        try {
            response = stub.recordVariableMapStringString(gem);
            Message result = (Message) JavabufClassTranslator
                    .getGetter(
                            "dev.resteasy.grpc.example.RecordVariable<java.util.HashMap<java.lang.String,java.lang.String>>")
                    .invoke(response);
            RecordVariable<Map> r2 = (RecordVariable<Map>) translator.translateFromJavabuf(result);
            Map map2 = r2.t();
            Assertions.assertTrue(map.equals(map2));
        } catch (StatusRuntimeException e) {
            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testRecordArrayPrimitive(CC1ServiceBlockingStub stub) throws Exception {
        CC2 cc2 = new CC2("bill", 19);
        int[] is = new int[] { 51, 53 };
        RecordVariable<int[]> r = new RecordVariable<int[]>(cc2, 29, is);
        GenericType<RecordVariable<int[]>> type = new GenericType<RecordVariable<int[]>>() {
        };
        Message m = translator.translateToJavabuf(r, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = ((GeneralEntityMessage.Builder) JavabufClassTranslator
                .getSetter("dev.resteasy.grpc.example.RecordVariable<int[]>")
                .invoke(builder, m)).setURL("http://localhost:8080" + "/p/record/variable/array/primitive").build();
        GeneralReturnMessage response;
        try {
            response = stub.recordVariableArrayPrimitive(gem);
            Message result = (Message) JavabufClassTranslator
                    .getGetter("dev.resteasy.grpc.example.RecordVariable<int[]>")
                    .invoke(response);
            RecordVariable<int[]> r2 = (RecordVariable<int[]>) translator.translateFromJavabuf(result);
            int[] is2 = r2.t();
            Assertions.assertTrue(Arrays.equals(is, is2));
        } catch (StatusRuntimeException e) {
            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testRecordArrayCC3(CC1ServiceBlockingStub stub) throws Exception {
        CC2 cc2 = new CC2("bill", 19);
        int[] is = new int[] { 51, 53 };
        CC3[] cc3s = new CC3[] { new CC3("abc"), null, new CC3("pq"), null, new CC3("xyz") };
        RecordVariable<CC3[]> r = new RecordVariable<CC3[]>(cc2, 29, cc3s);
        GenericType<dev.resteasy.grpc.example.RecordVariable<dev.resteasy.grpc.example.CC3[]>> type = new GenericType<dev.resteasy.grpc.example.RecordVariable<dev.resteasy.grpc.example.CC3[]>>() {
        };
        Message m = translator.translateToJavabuf(r, type);
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = ((GeneralEntityMessage.Builder) JavabufClassTranslator
                .getSetter("dev.resteasy.grpc.example.RecordVariable<dev.resteasy.grpc.example.CC3[]>")
                .invoke(builder, m)).setURL("http://localhost:8080" + "/p/record/variable/array/reference").build();
        GeneralReturnMessage response;
        try {
            response = stub.recordVariableArrayReference(gem);
            Message result = (Message) JavabufClassTranslator
                    .getGetter("dev.resteasy.grpc.example.RecordVariable<dev.resteasy.grpc.example.CC3[]>")
                    .invoke(response);
            RecordVariable<CC3[]> r2 = (RecordVariable<CC3[]>) translator.translateFromJavabuf(result);
            Assertions.assertTrue(r.cc2().equals(r2.cc2()));
            Assertions.assertTrue(r.i() == r2.i());
            Assertions.assertTrue(Arrays.deepEquals((CC3[]) r.t(), (CC3[]) r2.t()));
        } catch (StatusRuntimeException e) {
            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testReturnValueOnly(CC1ServiceBlockingStub stub) throws Exception {
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.build();
        GeneralReturnMessage response;
        try {
            response = stub.returnCC12(gem);
            Message result = (Message) response.getDevResteasyGrpcExampleCC12Field();
            Object o = translator.translateFromJavabuf(result);
            Assertions.assertTrue(o instanceof CC12);
        } catch (StatusRuntimeException e) {
            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testCollidingMethodNames(CC1ServiceBlockingStub stub) throws Exception {
        CC1_proto.GeneralEntityMessage.Builder builder = CC1_proto.GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setURL("p/same/1").setGIntegerField(gInteger.newBuilder().setValue(7)).build();
        GeneralReturnMessage response;
        try {
            response = stub.same(gem);
            Message result = (Message) response.getGIntegerField();
            Object o = translator.translateFromJavabuf(result);
            Assertions.assertTrue(o.equals(7));
        } catch (StatusRuntimeException e) {
            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
        try {
            gem = builder.setURL("p/same/2").setGDoubleField(CC1_proto.gDouble.newBuilder().setValue(11.3)).build();
            response = stub.same1(gem);
            Message result = (Message) response.getGDoubleField();
            Object o = translator.translateFromJavabuf(result);
            Assertions.assertTrue(o.equals(11.3));
        } catch (StatusRuntimeException e) {
            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    static class GeneralReturnMessageHolder<T> {
        ArrayList<T> values = new ArrayList<T>();

        T getValue() {
            return values.get(0);
        }

        void setValue(T value) {
            values.add(value);
        }

        void addValue(T value) {
            values.add(value);
        }

        Iterator<T> iterator() {
            return values.iterator();
        }

        int size() {
            return values.size();
        }
    }

    void testIntAsyncStub(CC1ServiceStub asyncStub) throws Exception {
        gInteger n = gInteger.newBuilder().setValue(3).build();
        GeneralEntityMessage.Builder builder = GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setGIntegerField(n).build();
        CountDownLatch latch = new CountDownLatch(1);
        GeneralReturnMessageHolder<Integer> grmh = new GeneralReturnMessageHolder<Integer>();
        StreamObserver<GeneralReturnMessage> responseObserver = new StreamObserver<GeneralReturnMessage>() {
            @Override
            public void onNext(GeneralReturnMessage value) {
                grmh.setValue(value.getGIntegerField().getValue());
                latch.countDown();
            }

            @Override
            public void onError(Throwable t) {
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        };
        try {
            asyncStub.getInt(gem, responseObserver);
            latch.await();
            Assertions.assertEquals((Integer) 4, grmh.getValue());
        } catch (StatusRuntimeException e) {

            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    void testSseAsyncStub(CC1ServiceStub asyncStub) throws Exception {
        GeneralEntityMessage.Builder builder = GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.build();
        CountDownLatch latch = new CountDownLatch(1);
        GeneralReturnMessageHolder<CC1_proto.dev_resteasy_grpc_bridge_runtime_sse___SseEvent> grmh = new GeneralReturnMessageHolder<CC1_proto.dev_resteasy_grpc_bridge_runtime_sse___SseEvent>();
        StreamObserver<CC1_proto.dev_resteasy_grpc_bridge_runtime_sse___SseEvent> responseObserver = new StreamObserver<CC1_proto.dev_resteasy_grpc_bridge_runtime_sse___SseEvent>() {

            @Override
            public void onNext(CC1_proto.dev_resteasy_grpc_bridge_runtime_sse___SseEvent value) {
                grmh.addValue(value);
            }

            @Override
            public void onError(Throwable t) {
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        };
        try {
            asyncStub.sse(gem, responseObserver);
            latch.await();
            Assertions.assertEquals(4, grmh.size());
            Iterator<CC1_proto.dev_resteasy_grpc_bridge_runtime_sse___SseEvent> it = grmh.iterator();
            for (int i = 0; i < 3; i++) {
                CC1_proto.dev_resteasy_grpc_bridge_runtime_sse___SseEvent sseEvent = it.next();
                Assertions.assertEquals("name" + (i + 1), sseEvent.getName());
                byte[] bytes = sseEvent.getData().toByteArray();
                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                Any any = Any.parseFrom(CodedInputStream.newInstance(bais));
                gString gString = any.unpack(gString.class);
                Assertions.assertEquals("event" + (i + 1), gString.getValue());
            }
            CC1_proto.dev_resteasy_grpc_bridge_runtime_sse___SseEvent sseEvent = it.next();
            Assertions.assertEquals("name4", sseEvent.getName());
            Any any = sseEvent.getData();
            dev_resteasy_grpc_example___CC5 cc5 = any.unpack(dev_resteasy_grpc_example___CC5.class);
            Assertions.assertEquals(dev_resteasy_grpc_example___CC5.newBuilder().setK(4).build(), cc5);
        } catch (StatusRuntimeException e) {
            Assertions.fail("fail");
            return;
        }
    }

    void testIntFutureStub(CC1ServiceFutureStub futureStub) throws Exception {
        gInteger n = gInteger.newBuilder().setValue(3).build();
        GeneralEntityMessage.Builder builder = GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setGIntegerField(n).build();
        try {
            ListenableFuture<GeneralReturnMessage> future = futureStub.getInt(gem);
            Assertions.assertEquals(4, future.get().getGIntegerField().getValue());
        } catch (StatusRuntimeException e) {

            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assertions.fail(writer.toString());
            }
        }
    }

    //////////////////////////////
    static String getJavaFieldName(String fieldName) {
        int pos = fieldName.lastIndexOf("___");
        if (pos >= 0) {
            return fieldName.substring(pos);
        }
        return fieldName;
    }
}
