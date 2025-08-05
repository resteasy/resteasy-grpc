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
package dev.resteasy.grpc.bridge.generator.protobuf;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.logging.Logger;

import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;

import dev.resteasy.grpc.bridge.runtime.HolderMap;
import dev.resteasy.grpc.bridge.runtime.Utility;
import dev.resteasy.grpc.bridge.runtime.protobuf.AssignFromJavabuf;
import dev.resteasy.grpc.bridge.runtime.protobuf.AssignToJavabuf;
import dev.resteasy.grpc.bridge.runtime.protobuf.JavabufTranslator;
import dev.resteasy.grpc.bridge.runtime.protobuf.ReturnJavaClass;
import dev.resteasy.grpc.bridge.runtime.protobuf.TranslateFromJavabuf;
import dev.resteasy.grpc.bridge.runtime.protobuf.TranslateToJavabuf;

/**
 * Generates a class, &lt;prefix&gt;JavabufTranslator, that can translate back and forth between a Java class and
 * its protobuf representation in Java (i.e., its javabuf version).
 * <p/>
 * For example,
 * <p/>
 * 1. Start with two classes:
 *
 * <pre>
 * package example.grpc;
 *
 * public class CC3 {
 *    String s;
 *    public CC3(String s) {
 *       this.s = s;
 *    }
 * }
 *
 * package example.grpc;
 *
 * public class CC2 extends CC3 {
 *    int j;
 *    public CC2(String s, int j) {
 *       super(s);
 *       this.j = j;
 *    }
 * }
 * </pre>
 *
 * 2. Generate an Example.proto file from the two classes:
 *
 * <pre>
 * syntax = "proto3";
 * package example.grpc;
 * option java_package = "example.grpc";
 * option java_outer_classname = "Example_proto";
 *
 * message example_grpc___CC3 {
 *   string s = 1;
 * }
 *
 * message example_grpc___CC2 {
 *   int32 j = 1;
 *   string s = 2;
 * }
 * </pre>
 *
 * 3. Use the protobuf compiler to generate class {@code Example_proto} with the javabuf versions
 * {@code Example_proto.example_grpc___CC2} and {@code Example_proto_example.grpc___CC3} of
 * {@code CC2} and {@code CC3}, respectively.
 * <p/>
 *
 * Now, {@code JavabufTranslatorGenerator} will generate an {@code ExampleJavabufTranslator} class that
 * can be used as follows:
 *
 * <pre>
 * &#064;Test
 * public void test() {
 *
 *     // 1. Create a CC2.
 *     CC2 cc2 = new CC2("abc", 19);
 *
 *     // 2. Translate to javabuf form
 *     Message message = ExampleJavabufTranslator.translateToJavabuf(cc2);
 *
 *     // 3. Manually create a javabuf representation of the same CC2 and demonstrate they're the same.
 *     Example_proto.example_grpc___CC2 cc2Message = Example_proto.example_grpc___CC2.newBuilder().setJ(19).setS("abc").build();
 *     Assert.assertEquals(message, cc2Message);
 *
 *     // 4. A. Translate the javabuf object created in step 2 back to its original java form.
 *     //    B. Demonstrate it's the same as the java object created in step 1.
 *     CC2 cc2_new = (CC2) ExampleJavabufTranslator.translateFromJavabuf(message);
 *     Assert.assertEquals(cc2, cc2_new);
 * }
 * </pre>
 */
public class JavabufTranslatorGenerator {

    private static Logger logger = Logger.getLogger(JavabufTranslatorGenerator.class);
    private static final String LS = System.lineSeparator();

    public interface AssignTo {
        void assign(Object from, DynamicMessage.Builder builder);
    }

    public interface AssignFrom {
        void assign(Message message, Object object);
    }

    private static final Map<String, Class<?>> PRIMITIVE_WRAPPER_TYPES = new HashMap<String, Class<?>>();
    private static final Map<String, String> GET_METHODS = new HashMap<String, String>();
    private static final Map<Class<?>, String> PRIMITIVE_DEFAULTS = new HashMap<Class<?>, String>();
    private static final Map<String, String> WRAPPER_TO_PRIMITIVE = new HashMap<String, String>();
    private static final Set<String> NULLABLE_WRAPPERS = new HashSet<String>();
    private static final Set<String> JAVA_WRAPPER_TYPES = new HashSet<String>();
    private static final Map<String, String> JAVA_WRAPPER_MAP = new HashMap<String, String>();
    private static final Map<String, String> ARRAY_CLASSES_TO_JAVABUF = new HashMap<String, String>();
    private static final Map<String, String> ARRAY_CLASSES_FROM_JAVABUF = new HashMap<String, String>();
    private static final Map<String, String> ARRAY_CLASSES_FROM_JAVABUF_STREAM = new HashMap<String, String>();
    private static final Map<String, String> BUILTIN_TO_JAVABUF = new HashMap<String, String>();

    private static final Map<String, String> AGGREGATES_INV = new HashMap<String, String>();
    private static final Map<String, String> GENERICS = new HashMap<String, String>();
    private static final Map<String, String> LISTS = new HashMap<String, String>();
    private static final Map<String, String> SETS = new HashMap<String, String>();
    private static final Map<String, String> MAPS = new HashMap<String, String>();
    private static final Map<String, String> MULTIMAPS = new HashMap<String, String>();
    private static final Map<String, String> RECORDS = new HashMap<String, String>();
    private static final Map<String, String> COLLECTION_TYPE = new HashMap<String, String>();
    private static final Map<String, String> KEY_TYPE_JAVA = new HashMap<String, String>();
    private static final Map<String, String> VALUE_TYPE_JAVA = new HashMap<String, String>();
    private static final Map<String, String> KEY_TYPE_JAVABUF = new HashMap<String, String>();
    private static final Map<String, String> VALUE_TYPE_JAVABUF = new HashMap<String, String>();
    private static final Map<String, String> CLASSNAMES = new HashMap<String, String>();
    private static final Set<String> IMPORTS = new HashSet<String>();

    private static final String ArrayPrimitiveBuilderClass = "      public Message assignToJavabuf(Object x) {%n"
            + "         if (x == null) {%n"
            + "            return null;%n"
            + "         }%n"
            + "         %1$s[] aa = (%1$s[]) x;%n"
            + "         dev_resteasy_grpc_arrays___%2$s___Array.Builder arrayBuilder = dev_resteasy_grpc_arrays___%2$s___Array.newBuilder();%n"
            + "         for (int i = 0; i < aa.length; i++) {%n"
            + "            arrayBuilder.add%3$sField(aa[i]);%n"
            + "         }%n"
            + "         return arrayBuilder.build();%n"
            + "      }%n"
            + "%n"
            + "      public void clear() {%n"
            + "         //%n"
            + "      }%n";

    private static final String ArrayPrimitiveWrapperBuilderClass = "      public Message assignToJavabuf(Object x) {%n"
            + "         if (x == null) {%n"
            + "            return null;%n"
            + "         }%n"
            + "         %1$s[] aa = (%1$s[]) x;%n"
            + "         %2$s___wrapper.Builder elementBuilder = %2$s___wrapper.newBuilder();%n"
            + "         %2$s___WArray.Builder arrayBuilder = %2$s___WArray.newBuilder();%n"
            + "         for (int i = 0; i < aa.length; i++) {%n"
            + "            if (aa[i] == null) {%n"
            + "               elementBuilder.clear().setNoneField(dev_resteasy_grpc_arrays___NONE.newBuilder());%n"
            + "            } else {%n"
            + "               elementBuilder.clear().set%3$sField(aa[i]);%n"
            + "            }%n"
            + "            arrayBuilder.addWrapperField(elementBuilder);%n"
            + "         }%n"
            + "         return arrayBuilder.build();%n"
            + "      }%n"
            + "%n"
            + "      public void clear() {%n"
            + "         //%n"
            + "      }%n";

    private static final String ArrayWrapperBuilderClassForByteArrays = "      public Message assignToJavabuf(Object x) {%n"
            + "         if (x == null) {%n"
            + "            return null;%n"
            + "         }%n"
            + "         byte[] aa = (byte[]) x;%n"
            + "         dev_resteasy_grpc_arrays___Byte___Array.Builder arrayBuilder = dev_resteasy_grpc_arrays___Byte___Array.newBuilder();%n"
            + "         arrayBuilder.setByteField(ByteString.copyFrom(aa));%n"
            + "         return arrayBuilder.build();%n"
            + "      }%n"
            + "%n"
            + "      public void clear() {%n"
            + "         //%n"
            + "      }%n";

    private static final String ArrayWrapperBuilderClassForCharArrays = "          public Message assignToJavabuf(Object x) {%n"
            + "         if (x == null) {%n"
            + "            return null;%n"
            + "         }%n"
            + "         char[] cs = (char[]) x;%n"
            + "         dev_resteasy_grpc_arrays___Character___Array.Builder arrayBuilder = dev_resteasy_grpc_arrays___Character___Array.newBuilder();%n"
            + "         for (int i = 0; i < cs.length; i++) {%n"
            + "            arrayBuilder.addCharField(String.valueOf(cs[i]));%n"
            + "         }%n"
            + "         return arrayBuilder.build();%n"
            + "      }%n"
            + "%n"
            + "      public void clear() {%n"
            + "         //%n"
            + "      }%n";

    private static final String ArrayWrapperBuilderClass = "      public Message assignToJavabuf(Object x) {%n"
            + "         if (x == null) {%n"
            + "            return null;%n"
            + "         }%n"
            + "         Object[] aa = (Object[]) x;%n"
            + "         %2$s___wrapper.Builder elementBuilder = %2$s___wrapper.newBuilder();%n"
            + "         %2$s___WArray.Builder arrayBuilder = %2$s___WArray.newBuilder();%n"
            + "         for (int i = 0; i < aa.length; i++) {%n"
            + "            if (aa[i] == null) {%n"
            + "               elementBuilder.clear().setNoneField(dev_resteasy_grpc_arrays___NONE.newBuilder());%n"
            + "            } else {%n"
            + "               elementBuilder.clear().set%3$sField((%2$s) INSTANCE.translateToJavabuf(aa[i]));%n"
            + "            }%n"
            + "            arrayBuilder.addWrapperField(elementBuilder);%n"
            + "         }%n"
            + "         return arrayBuilder.build();%n"
            + "      }%n"
            + "%n"
            + "      public void clear() {%n"
            + "         //%n"
            + "      }%n";

    private static final String ArrayWrapperBuilderClassForByteWrappers = "      public Message assignToJavabuf(Object x) {%n"
            + "         if (x == null) {%n"
            + "            return null;%n"
            + "         }%n"
            + "         %1$s[] aa = (%1$s[]) x;%n"
            + "         %2$s___wrapper.Builder elementBuilder = %2$s___wrapper.newBuilder();%n"
            + "         %2$s___WArray.Builder arrayBuilder = %2$s___WArray.newBuilder();%n"
            + "         for (int i = 0; i < aa.length; i++) {%n"
            + "            if (aa[i] == null) {%n"
            + "               elementBuilder.clear().setNoneField(dev_resteasy_grpc_arrays___NONE.newBuilder());%n"
            + "            } else {%n"
            + "               byte[] bs = new byte[] { aa[i].byteValue() };%n"
            + "               elementBuilder.clear().setByteField(ByteString.copyFrom(bs));%n"
            + "            }%n"
            + "            arrayBuilder.addWrapperField(elementBuilder);%n"
            + "         }%n"
            + "         return arrayBuilder.build();%n"
            + "      }%n"
            + "%n"
            + "      public void clear() {%n"
            + "         //%n"
            + "      }%n";

    private static final String ArrayWrapperBuilderClassForCharacterWrappers = "      public Message assignToJavabuf(Object x) {%n"
            + "         if (x == null) {%n"
            + "            return null;%n"
            + "         }%n"
            + "         %1$s[] aa = (%1$s[]) x;%n"
            + "         %2$s___wrapper.Builder elementBuilder = %2$s___wrapper.newBuilder();%n"
            + "         %2$s___WArray.Builder arrayBuilder = %2$s___WArray.newBuilder();%n"
            + "         for (int i = 0; i < aa.length; i++) {%n"
            + "            if (aa[i] == null) {%n"
            + "               elementBuilder.clear().setNoneField(dev_resteasy_grpc_arrays___NONE.newBuilder());%n"
            + "            } else {%n"
            + "               elementBuilder.set%1$sField(String.valueOf(aa[i]));%n"
            + "            }%n"
            + "            arrayBuilder.addWrapperField(elementBuilder);%n"
            + "         }%n"
            + "         return arrayBuilder.build();%n"
            + "      }%n"
            + "%n"
            + "      public void clear() {%n"
            + "         //%n"
            + "      }%n";

    private static final String ARRAY_WRAPPER_BUILDER_FOR_ANY_WRAPPERS_TO_JAVABUF = "      public Message assignToJavabuf(Object x) {%n"
            + "         if (x == null) {%n"
            + "            return null;%n"
            + "         }%n"
            + "         Object[] aa = (Object[]) x;%n"
            + "         dev_resteasy_grpc_arrays___Any___wrapper.Builder elementBuilder = dev_resteasy_grpc_arrays___Any___wrapper.newBuilder();%n"
            + "         dev_resteasy_grpc_arrays___Any___WArray.Builder arrayBuilder = dev_resteasy_grpc_arrays___Any___WArray.newBuilder();%n"
            + "         for (int i = 0; i < aa.length; i++) {%n"
            + "            if (aa[i] == null) {%n"
            + "               elementBuilder.clear().setNoneField(dev_resteasy_grpc_arrays___NONE.newBuilder());%n"
            + "            } else {%n"
            + "               elementBuilder.clear().setAnyField(Any.pack(INSTANCE.translateToJavabuf(aa[i])));%n"
            + "            }%n"
            + "            arrayBuilder.addWrapperField(elementBuilder);%n"
            + "         }%n"
            + "         return arrayBuilder.build();%n"
            + "      }%n"
            + "%n"
            + "      public void clear() {%n"
            + "         //%n"
            + "      }%n";

    private static final String ARRAY_WRAPPER_BUILDER_FOR_ANY_WRAPPERS_FROM_JAVABUF = "   public static class dev_resteasy_grpc_arrays___Any___WArray_FromJavabuf implements TranslateFromJavabuf {%n"
            + "      %n"
            + "      public Object assignFromJavabuf(Message message) {%n"
            + "         dev_resteasy_grpc_arrays___Any___WArray array = (dev_resteasy_grpc_arrays___Any___WArray) message;%n"
            + "         Object[] os = new Object[array.getWrapperFieldCount()];%n"
            + "         for (int i = 0; i < array.getWrapperFieldCount(); i++) {%n"
            + "            if (!array.getWrapperField(i).hasNoneField()) {%n"
            + "               Message m = null;%n"
            + "               try {%n"
            + "                  m = Utility.unpack(array.getWrapperField(i).getAnyField(), INSTANCE);%n"
            + "               } catch (Exception e) {%n"
            + "                   throw new RuntimeException(e);%n"
            + "               }%n"
            + "               Object element = INSTANCE.translateFromJavabuf(m);%n"
            + "               os[i] = element;%n"
            + "            }%n"
            + "         }%n"
            + "         return os;%n"
            + "      }%n"
            + "%n"
            + "      @Override%n"
            + "      public Object parseFromJavabuf(InputStream is) throws IOException {%n"
            + "         Message m = dev_resteasy_grpc_arrays___Any___WArray.parseFrom(is);%n"
            + "         return assignFromJavabuf(m);%n"
            + "      }%n"
            + "%n"
            + "      @Override%n"
            + "      public void assignExistingFromJavabuf(Message message, Object obj) { }%n"
            + "   }%n%n";

    private static final String ARRAYHOLDER_WARRAY_TO_JAVABUF = "   public static class dev_resteasy_grpc_arrays___ArrayHolder___WArray_ToJavabuf implements TranslateToJavabuf {%n"
            + "      public Message assignToJavabuf(Object x) {%n"
            + "         dev_resteasy_grpc_arrays___ArrayHolder___wrapper.Builder elementBuilder = dev_resteasy_grpc_arrays___ArrayHolder___wrapper.newBuilder();%n"
            + "         dev_resteasy_grpc_arrays___ArrayHolder___WArray.Builder arrayBuilder = dev_resteasy_grpc_arrays___ArrayHolder___WArray.newBuilder();%n"
            + "         for (int i = 0; i < Array.getLength(x); i++) {%n"
            + "            if (Array.get(x, i) == null) {%n"
            + "               elementBuilder.clear().setNoneField(dev_resteasy_grpc_arrays___NONE.newBuilder());%n"
            + "            } else {%n"
            + "               elementBuilder.clear().setDevResteasyGrpcArraysArrayHolderField((dev_resteasy_grpc_arrays___ArrayHolder) INSTANCE.translateToJavabuf(Array.get(x, i)));%n"
            + "            }%n"
            + "            arrayBuilder.addWrapperField(elementBuilder);%n"
            + "         }%n"
            + "         return arrayBuilder.setComponentType(x.getClass().getComponentType().getName()).build();%n"
            + "      }%n"
            + "%n"
            + "      public void clear() {%n"
            + "         //%n"
            + "      }%n"
            + "   }%n%n";

    private static final String ARRAYHOLDER_WARRAY_FROM_JAVABUF = "   public static class dev_resteasy_grpc_arrays___ArrayHolder___WArray_FromJavabuf implements TranslateFromJavabuf {%n"
            + "      public Object assignFromJavabuf(Message message) {%n"
            + "         try {%n"
            + "            dev_resteasy_grpc_arrays___ArrayHolder___WArray warray = (dev_resteasy_grpc_arrays___ArrayHolder___WArray) message;%n"
            + "            if (warray.getWrapperFieldCount() == 0) {%n"
            + "               return Array.newInstance(Class.forName(warray.getComponentType()), 0);%n"
            + "            }%n"
            + "            Object array = null;%n"
            + "            for (int i = 0; i < warray.getWrapperFieldCount(); i++) {%n"
            + "               Object element = null;%n"
            + "               if (warray.getWrapperField(i).getClass().getName().equals(\"google.protobuf.Any\")) {%n"
            + "                   Any any = (Any) warray.getWrapperField(i).getDevResteasyGrpcArraysArrayHolderField().getAnyField();%n"
            + "                   Message m = Utility.unpack(any, INSTANCE);%n"
            + "                   element = INSTANCE.translateFromJavabuf(m);%n"
            + "               } else {%n"
            //            + "                   Message msg = warray.getWrapperField(i);%n"
            + "                   if (!warray.getWrapperField(i).hasNoneField()) {%n"
            + "                       element = INSTANCE.translateFromJavabuf(warray.getWrapperField(i).getDevResteasyGrpcArraysArrayHolderField());%n"
            + "                   }%n"
            + "               }%n"
            + "               if (array == null) {%n"
            + "                  array = Array.newInstance(Class.forName(warray.getComponentType()), warray.getWrapperFieldCount());%n"
            + "               }%n"
            + "               Array.set(array, i, element);%n"
            + "            }%n"
            + "            if (array == null) {%n"
            + "               array = Array.newInstance(Class.forName(warray.getComponentType()), warray.getWrapperFieldCount());%n"
            + "            }%n"
            + "            return array;%n"
            + "         } catch (Exception e) {%n"
            + "            throw new RuntimeException(e);%n"
            + "         }%n"
            + "      }%n%n"
            + "      @Override%n"
            + "      public Object parseFromJavabuf(InputStream is) throws IOException {%n"
            + "         Message m = dev_resteasy_grpc_arrays___ArrayHolder___WArray.parseFrom(is);%n"
            + "         return assignFromJavabuf(m);%n"
            + "      }%n%n"
            + "      @Override%n"
            + "      public void assignExistingFromJavabuf(Message message, Object obj) { }%n"
            + "   }%n%n";

    private static final String dev_resteasy_grpc_arrays___ArrayHolder_ToJavabuf = "   public static class dev_resteasy_grpc_arrays___ArrayHolder_ToJavabuf implements TranslateToJavabuf {%n"
            + "      public Message assignToJavabuf(Object x) {// X4 %n"
            + "         if (x == null) {%n"
            + "            return null;%n"
            + "         }%n"
            + "         try {%n"
            + "            dev_resteasy_grpc_arrays___ArrayHolder.Builder holderBuilder = dev_resteasy_grpc_arrays___ArrayHolder.newBuilder();%n"
            + "            dev_resteasy_grpc_arrays___ArrayHolder___wrapper.Builder holderWrapperBuilder = dev_resteasy_grpc_arrays___ArrayHolder___wrapper.newBuilder();%n"
            + "            dev_resteasy_grpc_arrays___ArrayHolder___WArray.Builder ahWBuilder = dev_resteasy_grpc_arrays___ArrayHolder___WArray.newBuilder();%n"
            + "            for (int i = 0; i < Array.getLength(x); i++) {%n"
            + "               if (Array.get(x, i) == null) {%n"
            + "                  holderWrapperBuilder.clear().setNoneField(dev_resteasy_grpc_arrays___NONE.newBuilder());%n"
            + "               } else {%n"
            + "                  Message message = INSTANCE.translateToJavabuf(Array.get(x, i));%n"
            + "                  Method method = arraySetters.get(message.getClass());%n"
            + "                  if (method != null) {%n"
            + "                     method.invoke(holderBuilder, message);%n"
            + "                     holderWrapperBuilder.clear().setDevResteasyGrpcArraysArrayHolderField(holderBuilder);%n"
            + "                  } else {%n"
            + "                     Any any = Any.pack(message);%n"
            + "                     holderBuilder.setAnyField(any);%n"
            + "                     holderWrapperBuilder.clear().setDevResteasyGrpcArraysArrayHolderField(holderBuilder);%n"
            + "                  }%n"
            + "               }%n"
            + "              ahWBuilder.addWrapperField(holderWrapperBuilder);%n"
            + "            }%n"
            + "            return ahWBuilder.setComponentType(x.getClass().getComponentType().getName()).build();%n"
            + "         } catch (Exception e) {%n"
            + "            throw new RuntimeException(e);%n"
            + "         }%n"
            + "      }%n%n"
            + "      public void clear() {%n"
            + "         //%n"
            + "      }%n"
            + "   }%n%n";

    private static final String ArrayWrapperJavabufToJavaBuiltin = "  public static class dev_resteasy_grpc_arrays___%1$s___WArray_FromJavabuf implements TranslateFromJavabuf {%n"
            + "      %n"
            + "      public Object assignFromJavabuf(Message message) {%n"
            + "         dev_resteasy_grpc_arrays___%1$s___WArray array = (dev_resteasy_grpc_arrays___%1$s___WArray) message;%n"
            + "         %1$s[] xs = new %1$s[array.getWrapperFieldCount()];%n"
            + "         for (int i = 0; i < array.getWrapperFieldCount(); i++) {%n"
            + "            if (array.getWrapperField(i).has%1$sField()) {%n"
            + "               xs[i] = array.getWrapperField(i).get%1$sField();%n"
            + "            }%n"
            + "         }%n"
            + "         return xs;%n"
            + "      }%n%n"
            + "      @Override%n"
            + "      public Object parseFromJavabuf(InputStream is) throws IOException {%n"
            + "         Message m = dev_resteasy_grpc_arrays___%1$s___WArray.parseFrom(is);%n"
            + "         return assignFromJavabuf(m);%n"
            + "      }%n%n"
            + "      @Override%n"
            + "      public void assignExistingFromJavabuf(Message message, Object obj) { }%n"
            + "   }%n%n%n";

    private static final String ArrayWrapperJavabufToByte = "  public static class dev_resteasy_grpc_arrays___%1$s___WArray_FromJavabuf implements TranslateFromJavabuf {%n"
            + "      %n"
            + "      public Object assignFromJavabuf(Message message) {%n"
            + "         dev_resteasy_grpc_arrays___%1$s___WArray array = (dev_resteasy_grpc_arrays___%1$s___WArray) message;%n"
            + "         %1$s[] xs = new %1$s[array.getWrapperFieldCount()];%n"
            + "         for (int i = 0; i < array.getWrapperFieldCount(); i++) {%n"
            + "            if (array.getWrapperField(i).has%1$sField()) {%n"
            + "               xs[i] = array.getWrapperField(i).get%1$sField().byteAt(0);%n"
            + "            }%n"
            + "         }%n"
            + "         return xs;%n"
            + "      }%n%n"
            + "      @Override%n"
            + "      public Object parseFromJavabuf(InputStream is) throws IOException {%n"
            + "         Message m = dev_resteasy_grpc_arrays___%1$s___WArray.parseFrom(is);%n"
            + "         return assignFromJavabuf(m);%n"
            + "      }%n%n"
            + "      @Override%n"
            + "      public void assignExistingFromJavabuf(Message message, Object obj) { }%n"
            + "   }%n%n%n";

    private static final String ArrayWrapperJavabufToCharacter = "  public static class dev_resteasy_grpc_arrays___%1$s___WArray_FromJavabuf implements TranslateFromJavabuf {%n"
            + "      %n"
            + "      public Object assignFromJavabuf(Message message) {%n"
            + "         dev_resteasy_grpc_arrays___%1$s___WArray array = (dev_resteasy_grpc_arrays___%1$s___WArray) message;%n"
            + "         %1$s[] xs = new %1$s[array.getWrapperFieldCount()];%n"
            + "         for (int i = 0; i < array.getWrapperFieldCount(); i++) {%n"
            + "            if (array.getWrapperField(i).has%1$sField()) {%n"
            + "               xs[i] = array.getWrapperField(i).get%1$sField().charAt(0);%n"
            + "            }%n"
            + "         }%n"
            + "         return xs;%n"
            + "      }%n%n"
            + "      @Override%n"
            + "      public Object parseFromJavabuf(InputStream is) throws IOException {%n"
            + "         Message m = dev_resteasy_grpc_arrays___%1$s___WArray.parseFrom(is);%n"
            + "         return assignFromJavabuf(m);%n"
            + "      }%n%n"
            + "      @Override%n"
            + "      public void assignExistingFromJavabuf(Message message, Object obj) { }%n"
            + "   }%n%n%n";

    private static final String ArrayWrapperJavabufToShort = "  public static class dev_resteasy_grpc_arrays___%1$s___WArray_FromJavabuf implements TranslateFromJavabuf {%n"
            + "      %n"
            + "      public Object assignFromJavabuf(Message message) {%n"
            + "         dev_resteasy_grpc_arrays___%1$s___WArray array = (dev_resteasy_grpc_arrays___%1$s___WArray) message;%n"
            + "         %1$s[] xs = new %1$s[array.getWrapperFieldCount()];%n"
            + "         for (int i = 0; i < array.getWrapperFieldCount(); i++) {%n"
            + "            if (array.getWrapperField(i).has%1$sField()) {%n"
            + "               xs[i] = (short) array.getWrapperField(i).get%1$sField();%n"
            + "            }%n"
            + "         }%n"
            + "         return xs;%n"
            + "      }%n%n"
            + "      @Override%n"
            + "      public Object parseFromJavabuf(InputStream is) throws IOException {%n"
            + "         Message m = dev_resteasy_grpc_arrays___%1$s___WArray.parseFrom(is);%n"
            + "         return assignFromJavabuf(m);%n"
            + "      }%n%n"
            + "      @Override%n"
            + "      public void assignExistingFromJavabuf(Message message, Object obj) { }%n"
            + "   }%n%n%n";

    private static final String WArrayJavabufToJava = "  public static class %1$s___WArray_FromJavabuf implements TranslateFromJavabuf {%n"
            + "      %n"
            + "      public Object assignFromJavabuf(Message message) {%n"
            + "         %1$s___WArray array = (%1$s___WArray) message;%n"
            + "         Object os = null;%n"
            + "         for (int i = 0; i < array.getWrapperFieldCount(); i++) {%n"
            + "            if (!array.getWrapperField(i).hasNoneField()) {%n"
            + "               Object element = INSTANCE.translateFromJavabuf(array.getWrapperField(i).get%2$sField());%n"
            + "               if (os == null) {%n"
            + "                  os = Array.newInstance(element.getClass(), array.getWrapperFieldCount());%n"
            + "               }%n"
            + "               Array.set(os, i, element);%n"
            + "            }%n"
            + "         }%n"
            + "         if (os == null) {%n"
            + "            try {%n"
            + "               os = Array.newInstance(Class.forName(\"%3$s\"), 0);%n"
            + "            } catch (Exception e) {%n"
            + "               throw new RuntimeException(e);%n"
            + "            }%n"
            + "         }%n"
            + "         return os;%n"
            + "      }%n%n"
            + "      @Override%n"
            + "      public Object parseFromJavabuf(InputStream is) throws IOException {%n"
            + "         Message m = %1$s___WArray.parseFrom(is);%n"
            + "         return assignFromJavabuf(m);%n"
            + "      }%n%n"
            + "      @Override%n"
            + "      public void assignExistingFromJavabuf(Message message, Object obj) { }%n"
            + "   }%n%n";

    private static final String ArrayHolderPrimitiveFromJavabuf = "   public static class dev_resteasy_grpc_arrays___%1$s___Array_FromJavabuf implements TranslateFromJavabuf {%n"
            + "      public Object assignFromJavabuf(Message message) {%n"
            + "         dev_resteasy_grpc_arrays___%1$s___Array array = (dev_resteasy_grpc_arrays___%1$s___Array) message;%n"
            + "         %2$s[] xs = new %2$s[array.get%3$sFieldCount()];%n"
            + "         for (int i = 0; i < array.get%3$sFieldCount(); i++) {%n"
            + "            xs[i] = array.get%3$sField(i);%n"
            + "         }%n"
            + "         return xs;%n"
            + "      }%n%n"
            + "      @Override%n"
            + "      public Object parseFromJavabuf(InputStream is) throws IOException {%n"
            + "         Message m = dev_resteasy_grpc_arrays___%1$s___Array.parseFrom(is);%n"
            + "         return assignFromJavabuf(m);%n"
            + "      }%n%n"
            + "      @Override%n"
            + "      public void assignExistingFromJavabuf(Message message, Object obj) { }%n"
            + "   }%n%n";

    private static final String ArrayHolderPrimitiveFromJavabuf_char = "   public static class dev_resteasy_grpc_arrays___Character___Array_FromJavabuf implements TranslateFromJavabuf {%n"
            + "      public Object assignFromJavabuf(Message message) {%n"
            + "         dev_resteasy_grpc_arrays___Character___Array array = (dev_resteasy_grpc_arrays___Character___Array) message;%n"
            + "         char[] xs = new char[array.getCharFieldCount()];%n"
            + "         for (int i = 0; i < array.getCharFieldCount(); i++) {%n"
            + "            xs[i] = array.getCharField(i).charAt(0);%n"
            + "         }%n"
            + "         return xs;%n"
            + "      }%n%n"
            + "      @Override%n"
            + "      public Object parseFromJavabuf(InputStream is) throws IOException {%n"
            + "         Message m = dev_resteasy_grpc_arrays___Character___Array.parseFrom(is);%n"
            + "         return assignFromJavabuf(m);%n"
            + "      }%n%n"
            + "      @Override%n"
            + "      public void assignExistingFromJavabuf(Message message, Object obj) { }%n"
            + "   }%n%n";

    private static final String ArrayHolderPrimitiveFromJavabuf_short = "   public static class dev_resteasy_grpc_arrays___Short___Array_FromJavabuf implements TranslateFromJavabuf {%n"
            + "      public Object assignFromJavabuf(Message message) {%n"
            + "         dev_resteasy_grpc_arrays___Short___Array array = (dev_resteasy_grpc_arrays___Short___Array) message;%n"
            + "         short[] xs = new short[array.getShortFieldCount()];%n"
            + "         for (int i = 0; i < array.getShortFieldCount(); i++) {%n"
            + "            xs[i] = (short) array.getShortField(i);%n"
            + "         }%n"
            + "         return xs;%n"
            + "      }%n%n"
            + "      @Override%n"
            + "      public Object parseFromJavabuf(InputStream is) throws IOException {%n"
            + "         Message m = dev_resteasy_grpc_arrays___Short___Array.parseFrom(is);%n"
            + "         return assignFromJavabuf(m);%n"
            + "      }%n%n"
            + "      @Override%n"
            + "      public void assignExistingFromJavabuf(Message message, Object obj) { }%n"
            + "   }%n%n";

    private static final String ArrayHolderPrimitiveFromJavabuf_byte = "   public static class dev_resteasy_grpc_arrays___Byte___Array_FromJavabuf implements TranslateFromJavabuf {%n"
            + "      public Object assignFromJavabuf(Message message) {%n"
            + "         dev_resteasy_grpc_arrays___Byte___Array array = (dev_resteasy_grpc_arrays___Byte___Array) message;%n"
            + "         ByteString bs = array.getByteField();%n"
            + "         byte[] xs = new byte[bs.size()];%n"
            + "         for (int i = 0; i < bs.size(); i++) {%n"
            + "            xs[i] = bs.byteAt(i);%n"
            + "         }%n"
            + "         return xs;%n"
            + "      }%n%n"
            + "      @Override%n"
            + "      public Object parseFromJavabuf(InputStream is) throws IOException {%n"
            + "         Message m = dev_resteasy_grpc_arrays___Byte___Array.parseFrom(is);%n"
            + "         return assignFromJavabuf(m);%n"
            + "      }%n%n"
            + "      @Override%n"
            + "      public void assignExistingFromJavabuf(Message message, Object obj) { }%n"
            + "   }%n%n";

    private static final String ListOrSetToJavabuf = "%n"
            + "      public Message assignToJavabuf(Object o) {%n"
            + "         if (o == null) {%n"
            + "            return null;%n"
            + "         }%n"
            + "         %2$s collection = (%2$s) o;%n"
            + "         %1$s.Builder builder = %1$s.newBuilder();%n"
            + "         builder.setClassname(o.getClass().getName());%n"
            + "         FieldDescriptor fd = %1$s.getDescriptor().findFieldByName(\"data\");%n"
            + "         for (Object element : collection) {%n"
            + "              builder.addRepeatedField(fd, %3$s);%n"
            + "         }%n"
            + "         return builder.build();%n"
            + "      }%n"
            + "%n"
            + "      public void clear() {%n"
            + "      }%n";

    private static final String ListOrSetFromJavabuf_fromClass = "%n"
            + "  public static class %1$s_FromJavabuf implements TranslateFromJavabuf {%n"
            + "%n"
            + "      @Override%n"
            + "      public Object assignFromJavabuf(Message message) throws IOException%n"
            + "      {%n"
            + "         %2$s collection = new %2$s();%n"
            + "         %1$s m = (%1$s) message;%n"
            + "         for (%3$s l : m.getDataList()) {%n"
            + "               collection.add(%4$s);%n"
            + "         }%n"
            + "         return collection;%n"
            + "      }%n"
            + "%n"
            + "      @Override%n"
            + "      public void assignExistingFromJavabuf(Message message, Object obj) throws IOException%n"
            + "      {%n"
            + "         %2$s collection = new %2$s();%n"
            + "         %1$s m = (%1$s) message;%n"
            + "         for (%3$s l : m.getDataList()) {%n"
            + "               collection.add(%4$s);%n"
            + "         }%n"
            + "      }%n"
            + "%n"
            + "      @Override%n"
            + "      public Object parseFromJavabuf(InputStream is) throws IOException%n"
            + "      {%n"
            + "         Message m = %1$s.parseFrom(is);%n"
            + "         return assignFromJavabuf(m);%n"
            + "      }%n";

    private static final String ListOrSetFromJavabuf_fromInterface = "%n"
            + "  public static class %1$s_FromJavabuf implements TranslateFromJavabuf {%n"
            + "%n"
            + "      @Override%n"
            + "      public Object assignFromJavabuf(Message message) throws IOException%n"
            + "      {%n"
            + "         try {%n"
            + "            %1$s m = (%1$s) message;%n"
            + "            String classname = m.getClassname();%n"
            + "            Collection collection = (Collection) Class.forName(classname).newInstance();%n"
            + "            for (%3$s l : m.getDataList()) {%n"
            + "               collection.add(%4$s);%n"
            + "            }%n"
            + "            return collection;%n"
            + "         } catch (Exception e) {%n"
            + "            throw new RuntimeException(e);%n"
            + "         }"
            + "      }%n"
            + "%n"
            + "      @Override%n"
            + "      public void assignExistingFromJavabuf(Message message, Object obj) throws IOException%n"
            + "      {%n"
            + "         try {%n"
            + "            %1$s m = (%1$s) message;%n"
            + "            String classname = m.getClassname();%n"
            + "            Collection collection = (Collection) Class.forName(classname).newInstance();%n"
            + "            for (%3$s l : m.getDataList()) {%n"
            + "               collection.add(%4$s);%n"
            + "            }%n"
            + "         } catch (Exception e) {%n"
            + "            throw new RuntimeException(e);%n"
            + "         }"
            + "      }%n"
            + "%n"
            + "      @Override%n"
            + "      public Object parseFromJavabuf(InputStream is) throws IOException%n"
            + "      {%n"
            + "         Message m = %1$s.parseFrom(is);%n"
            + "         return assignFromJavabuf(m);%n"
            + "      }%n";

    /*
     * %1: key type
     * %2: value type
     * %3: javabuf type
     * %4: key translation
     * %5: value translation
     */
    private static final String MAP_TO_JAVABUF = "%n"
            + "       @Override%n"
            + "       public Message assignToJavabuf(Object o) {%n"
            + "           if (o == null) {%n"
            + "              return null;%n"
            + "           }%n"
            + "           java.util.Map<%1$s, %2$s> map = (java.util.Map<%1$s, %2$s>) o;%n"
            + "           %3$s.Builder builder = %3$s.newBuilder();%n"
            + "           builder.setClassname(o.getClass().getName());%n"
            + "           %3$s.Pair.Builder pairBuilder = %3$s.Pair.newBuilder();%n"
            + "           FieldDescriptor fd = %3$s.getDescriptor().findFieldByName(\"data\");%n"
            + "           for (java.util.Map.Entry<%1$s, %2$s> entry : map.entrySet()) {%n"
            + "               builder.addRepeatedField(fd, pairBuilder.setKey(%4$s).setValue(%5$s).build());%n"
            + "               pairBuilder.clear();%n"
            + "           }%n"
            + "           return builder.build();%n"
            + "       }%n"
            + "%n"
            + "      public void clear() {%n"
            + "      }%n";

    /*
     * %1: javabuf class name
     * %2: key type
     * %3: value type
     * %4: key translation
     * %5: value translation
     */
    private static final String MAP_FROM_JAVABUF = "   public static class %1$s_FromJavabuf implements TranslateFromJavabuf {%n"
            + "%n"
            + "      @Override%n"
            + "      public Object assignFromJavabuf(Message message) throws IOException {%n"
            + "         try {%n"
            + "            %1$s m = (%1$s) message;%n"
            + "            String classname = m.getClassname();%n"
            + "            Map<%2$s, %3$s> map = (Map<%2$s, %3$s>) Class.forName(classname).newInstance();%n"
            + "            for (%1$s.Pair pair : m.getDataList()) {%n"
            + "               map.put((%2$s) %4$s, (%3$s) %5$s);%n"
            + "            }%n"
            + "            return map;%n"
            + "         } catch (Exception e) {%n"
            + "            %1$s m = (%1$s) message;%n"
            + "            String classname = m.getClassname();%n"
            + "            throw new RuntimeException(classname + \" \" + e);%n"
            + "         }%n"
            + "      }%n"
            + "%n"
            + "      @Override%n"
            + "      public void assignExistingFromJavabuf(Message message, Object obj) throws IOException {%n"
            + "         try {%n"
            + "            %1$s m = (%1$s) message;%n"
            + "            Map<%2$s, %3$s> map = (Map<%2$s, %3$s>) obj;%n"
            + "            for (%1$s.Pair pair : m.getDataList()) {%n"
            + "                map.put((%2$s) %4$s, (%3$s) %5$s);%n"
            + "            }%n"
            + "         } catch (Exception e) {%n"
            + "            throw new RuntimeException(e);%n"
            + "         }%n"
            + "      }%n"
            + "%n"
            + "      @Override%n"
            + "      public Object parseFromJavabuf(InputStream is) throws IOException {%n"
            + "         Message m = %1$s.parseFrom(is);%n"
            + "         return assignFromJavabuf(m);%n"
            + "      }%n";

    /*
     * %1: key type
     * %2: value type
     * %3: javabuf type
     * %4: key translation
     * %5: value translation
     */
    private static final String MULTIMAP_TO_JAVABUF = "%n"
            + "       @Override%n"
            + "       public Message assignToJavabuf(Object o) {%n"
            + "           if (o == null) {%n"
            + "              return null;%n"
            + "           }%n"
            + "           jakarta.ws.rs.core.MultivaluedMap<%1$s, %2$s> map = (jakarta.ws.rs.core.MultivaluedMap<%1$s, %2$s>) o;%n"
            + "           %3$s.Builder builder = %3$s.newBuilder();%n"
            + "           builder.setClassname(o.getClass().getName());%n"
            + "           %3$s.Pair.Builder pairBuilder = %3$s.Pair.newBuilder();%n"
            + "           FieldDescriptor fd = %3$s.getDescriptor().findFieldByName(\"data\");%n"
            + "           for (java.util.Map.Entry<%1$s, List<%2$s>> entry : map.entrySet()) {%n"
            + "               for (%2$s value : (List<%2$s>) entry.getValue()) {%n"
            + "                  builder.addRepeatedField(fd, pairBuilder.setKey(%4$s).setValue(%5$s).build());%n"
            + "                  pairBuilder.clear();%n"
            + "                }%n"
            + "           }%n"
            + "           return builder.build();%n"
            + "       }%n"
            + "%n"
            + "      public void clear() {%n"
            + "      }%n";

    /*
     * %1: javabuf class name
     * %2: key type
     * %3: value type
     * %4: key translation
     * %5: value translation
     */
    private static final String MULTIMAP_FROM_JAVABUF = "   public static class %1$s_FromJavabuf implements TranslateFromJavabuf {%n"
            + "%n"
            + "      @Override%n"
            + "      public Object assignFromJavabuf(Message message) throws IOException {%n"
            + "         try {%n"
            + "            %1$s m = (%1$s) message;%n"
            + "            String classname = m.getClassname();%n"
            + "            MultivaluedMap<%2$s, %3$s> map = (MultivaluedMap<%2$s, %3$s>) Class.forName(classname).newInstance();%n"
            + "            for (%1$s.Pair pair : m.getDataList()) {%n"
            + "               map.add((%2$s) %4$s, (%3$s) %5$s);%n"
            + "            }%n"
            + "            return map;%n"
            + "         } catch (Exception e) {%n"
            + "            %1$s m = (%1$s) message;%n"
            + "            String classname = m.getClassname();%n"
            + "            throw new RuntimeException(classname + \" \" + e);%n"
            + "         }%n"
            + "      }%n"
            + "%n"
            + "      @Override%n"
            + "      public void assignExistingFromJavabuf(Message message, Object obj) throws IOException {%n"
            + "         try {%n"
            + "            %1$s m = (%1$s) message;%n"
            + "            MultivaluedMap<%2$s, %3$s> map = (MultivaluedMap<%2$s, %3$s>)  obj;%n"
            + "            for (%1$s.Pair pair : m.getDataList()) {%n"
            + "               map.add((%2$s) %4$s, (%3$s) %5$s);%n"
            + "            }%n"
            + "         } catch (Exception e) {%n"
            + "            throw new RuntimeException(e);%n"
            + "         }%n"
            + "      }%n"
            + "%n"
            + "      @Override%n"
            + "      public Object parseFromJavabuf(InputStream is) throws IOException {%n"
            + "         Message m = %1$s.parseFrom(is);%n"
            + "         return assignFromJavabuf(m);%n"
            + "      }%n";

    private static final String RECORD_FROM_JAVABUF = "%n"
            + "   public static class %1$s_FromJavabuf implements TranslateFromJavabuf {%n"
            + "      private static Descriptor descriptor = %1$s.getDescriptor();%n"
            + "      private static List<AssignFromJavabuf> assignList = new ArrayList<AssignFromJavabuf>();%n"
            + "%n"
            + "      static {%n"
            + "         for (FieldDescriptor f : descriptor.getFields()) {%n"
            + "            String name = f.getName();%n"
            + "            if (descriptor.findFieldByName(name) == null) {%n"
            + "               continue;%n"
            + "            }%n"
            + "            assignList.add(fromJavabuf(%2$s.class, descriptor.findFieldByName(name)));%n"
            + "         }%n"
            + "      }%n"
            + "%n"
            + "      @Override%n"
            + "      public %2$s assignFromJavabuf(Message message) {%n"
            + "         HolderMap map = new HolderMap();%n"
            //            + "         int i = 0;%n"
            + "         for (AssignFromJavabuf assignFrom : assignList) {%n"
            + "            try {%n"
            + "               assignFrom.assign(message, map);%n"
            + "            } catch (Exception e) {%n"
            + "               throw new RuntimeException(e);%n"
            + "            }%n"
            + "         }%n"
            + "         return new %2$s(%4$s);%n"
            + "      }%n"
            + "%n"
            + "      @Override%n"
            + "      public void assignExistingFromJavabuf(Message message, Object obj) {%n"
            + "         throw new RuntimeException(\"Not implemented\");%n"
            + "      }%n"
            + "%n"
            + "      @Override%n"
            + "      public Object parseFromJavabuf(InputStream is) throws IOException {%n"
            + "         Message m = %1$s.parseFrom(is);%n"
            + "         return assignFromJavabuf(m);%n"
            + "      }%n";

    private static final String RAW_AGGREGATE_TO_JAVABUF = "   private static TranslateToJavabuf rawAggregateTranslationToJavabuf(Object o) {%n"
            + "      if (List.class.isAssignableFrom(o.getClass())) {%n"
            + "         return new java_util___List_ToJavabuf();%n"
            + "      } else if (Set.class.isAssignableFrom(o.getClass())) {%n"
            + "         return new java_util___Set_ToJavabuf();%n"
            + "      } else if (Map.class.isAssignableFrom(o.getClass())) {%n"
            + "         return new java_util___Map_ToJavabuf();%n"
            + "      }%n"
            + "      return null;%n"
            + "   }%n%n";

    private static final String RAW_AGGREGATE_FROM_JAVABUF = "   private static TranslateFromJavabuf rawAggregateTranslationFromJavabuf(String classname, Object o) {%n"
            + "      try {%n"
            + "         Class<?> clazz = Class.forName(classname);%n"
            + "         if (List.class.isAssignableFrom(clazz)) {%n"
            + "            return new java_util___List_FromJavabuf();%n"
            + "         } else if (Set.class.isAssignableFrom(clazz)) {%n"
            + "            return new java_util___Set_FromJavabuf();%n"
            + "         } else if (Map.class.isAssignableFrom(clazz)) {%n"
            + "            return new java_util___Map_FromJavabuf();%n"
            + "         }%n"
            + "         return null;%n"
            + "      } catch (Exception e) {%n"
            + "         throw new RuntimeException(e);%n"
            + "      }%n"
            + "   }%n%n";

    private static final String TO_PRIMITIVE_JAVABUF_ARRAY = "   private static void toPrimitiveJavabufArray(Builder builder, FieldDescriptor fd, Class<?> componentType,%n"
            + "           Object array) {%n"
            + "       if (byte.class.equals(componentType) || Byte.class.equals(componentType)) {%n"
            + "           builder.setField(fd, ByteString.copyFrom((byte[]) array));%n"
            + "       } else if (char.class.equals(componentType)) {%n"
            + "           builder.setField(fd, charsToString(array));%n"
            + "       } else if (boolean.class.equals(componentType)) {%n"
            + "           for (int i = 0; i < Array.getLength(array); i++) {%n"
            + "               builder.addRepeatedField(fd, ((Boolean) Array.get(array, i)).booleanValue());%n"
            + "           }%n"
            + "       } else if (short.class.equals(componentType)) {%n"
            + "           for (int i = 0; i < Array.getLength(array); i++) {%n"
            + "               builder.addRepeatedField(fd, ((Short) Array.get(array, i)).shortValue());%n"
            + "           }%n"
            + "       } else if (int.class.equals(componentType) || Integer.class.equals(componentType)) {%n"
            + "           for (int i = 0; i < Array.getLength(array); i++) {%n"
            + "               builder.addRepeatedField(fd, ((Integer) Array.get(array, i)).intValue());%n"
            + "           }%n"
            + "       } else if (long.class.equals(componentType)) {%n"
            + "           for (int i = 0; i < Array.getLength(array); i++) {%n"
            + "               builder.addRepeatedField(fd, ((Long) Array.get(array, i)).longValue());%n"
            + "           }%n"
            + "       } else if (float.class.equals(componentType)) {%n"
            + "           for (int i = 0; i < Array.getLength(array); i++) {%n"
            + "               builder.addRepeatedField(fd, ((Float) Array.get(array, i)).floatValue());%n"
            + "           }%n"
            + "       } else if (double.class.equals(componentType)) {%n"
            + "           for (int i = 0; i < Array.getLength(array); i++) {%n"
            + "               builder.addRepeatedField(fd, ((Double) Array.get(array, i)).doubleValue());%n"
            + "           }%n"
            + "       } else {%n"
            + "           throw new RuntimeException(\"don't recognize type: \" + componentType);%n"
            + "       }%n"
            + "   }%n%n";

    private static final String TO_PRIMITIVE_JAVA_ARRAY = "   private static Object toPrimitiveJavaArray(Object original, Class<?> clazz) {%n"
            + "      List<?> list = null;%n"
            + "      Object array = null;%n"
            + "      if (unwrap.containsKey(clazz)) {%n"
            + "         clazz = unwrap.get(clazz);%n"
            + "      }%n"
            + "      if (original instanceof ByteString) {%n"
            + "         if (((ByteString) original).size() == 0) {%n"
            + "            return null;%n"
            + "         }%n"
            + "         array = Array.newInstance(clazz, ((ByteString) original).size());%n"
            + "      } else if (original instanceof String) {%n"
            + "         if (((String) original).length() == 0) {%n"
            + "            return null;%n"
            + "         }%n"
            + "         array = Array.newInstance(clazz, ((String) original).length());%n"
            + "      } else {%n"
            + "         list = (List<?>) original;%n"
            + "         if (list == null || list.size() == 0) {%n"
            + "            return null;%n"
            + "         }%n"
            + "         array = Array.newInstance(clazz, list.size());%n"
            + "      }%n"
            + "      if (boolean.class.equals(clazz)) {%n"
            + "         for (int i = 0; i < list.size(); i++) {%n"
            + "            Array.set(array, i, list.get(i));%n"
            + "         }%n"
            + "      } else if (byte.class.equals(clazz)) {%n"
            + "         ((ByteString) original).copyTo(((byte[]) array), 0);%n"
            + "      } else if (short.class.equals(clazz)) {%n"
            + "         for (int i = 0; i < list.size(); i++) {%n"
            + "            Array.set(array, i, ((Integer)list.get(i)).shortValue());%n"
            + "         }%n"
            + "      } else if (int.class.equals(clazz)) {%n"
            + "         for (int i = 0; i < list.size(); i++) {%n"
            + "            Array.set(array, i, list.get(i));%n"
            + "         }%n"
            + "      } else if (long.class.equals(clazz)) {%n"
            + "         for (int i = 0; i < list.size(); i++) {%n"
            + "             Array.set(array, i, list.get(i));%n"
            + "         }%n"
            + "      } else if (float.class.equals(clazz)) {%n"
            + "         for (int i = 0; i < list.size(); i++) {%n"
            + "             Array.set(array, i, list.get(i));%n"
            + "         }%n"
            + "      } else if (double.class.equals(clazz)) {%n"
            + "         for (int i = 0; i < list.size(); i++) {%n"
            + "            Array.set(array, i, list.get(i));%n"
            + "         }%n"
            + "      } else if (char.class.equals(clazz)) {%n"
            + "         for (int i = 0; i < ((String) original).length(); i++) {%n"
            + "            Array.set(array, i, ((String) original).charAt(i));%n"
            + "         }%n"
            + "      } else {%n"
            + "          throw new RuntimeException(\"Don't recognize class: \" + clazz.getName());%n"
            + "      }%n"
            + "      return array;%n"
            + "   }%n%n";

    private static final String IS_PRIMITIVE = "   private static boolean isPrimitive(FieldDescriptor fd) {%n"
            + "      return !fd.toProto().getType().equals(FieldDescriptorProto.Type.TYPE_MESSAGE);%n"
            + "   }%n%n";

    private static final String UNWRAP_TABLE = "   private static Map<Class<?>, Class<?>> unwrap = new HashMap<Class<?>, Class<?>>();%n%n"
            + "   static {%n"
            + "      unwrap.put(Boolean.class, boolean.class);%n"
            + "      unwrap.put(Byte.class, byte.class);%n"
            + "      unwrap.put(Short.class, short.class);%n"
            + "      unwrap.put(Integer.class, int.class);%n"
            + "      unwrap.put(Long.class, long.class);%n"
            + "      unwrap.put(Float.class, float.class);%n"
            + "      unwrap.put(Double.class, double.class);%n"
            + "      unwrap.put(Character.class, char.class);%n"
            + "   }%n%n";

    static {
        PRIMITIVE_WRAPPER_TYPES.put("gByte", byte.class);
        PRIMITIVE_WRAPPER_TYPES.put("gShort", short.class);
        PRIMITIVE_WRAPPER_TYPES.put("gInteger", int.class);
        PRIMITIVE_WRAPPER_TYPES.put("gLong", long.class);
        PRIMITIVE_WRAPPER_TYPES.put("gFloat", float.class);
        PRIMITIVE_WRAPPER_TYPES.put("gDouble", double.class);
        PRIMITIVE_WRAPPER_TYPES.put("gBoolean", boolean.class);
        PRIMITIVE_WRAPPER_TYPES.put("gCharacter", char.class);
        PRIMITIVE_WRAPPER_TYPES.put("gString", String.class);

        GET_METHODS.put("Byte", ".byteValue()");
        GET_METHODS.put("Short", ".shortValue()");
        GET_METHODS.put("Integer", ".intValue()");
        GET_METHODS.put("Long", ".longValue()");
        GET_METHODS.put("Float", ".floatValue()");
        GET_METHODS.put("Double", ".doubleValue()");
        GET_METHODS.put("Boolean", ".booleanValue()");
        GET_METHODS.put("Character", ".toString()");
        GET_METHODS.put("String", "");

        PRIMITIVE_DEFAULTS.put(boolean.class, "false");
        PRIMITIVE_DEFAULTS.put(byte.class, "(byte)0");
        PRIMITIVE_DEFAULTS.put(short.class, "(short)0");
        PRIMITIVE_DEFAULTS.put(int.class, "0");
        PRIMITIVE_DEFAULTS.put(long.class, "0L");
        PRIMITIVE_DEFAULTS.put(float.class, "0.0f");
        PRIMITIVE_DEFAULTS.put(double.class, "0.0d");
        PRIMITIVE_DEFAULTS.put(char.class, "'\\u0000'");

        WRAPPER_TO_PRIMITIVE.put("Boolean", "boolean");
        WRAPPER_TO_PRIMITIVE.put("Byte", "byte");
        WRAPPER_TO_PRIMITIVE.put("Short", "short");
        WRAPPER_TO_PRIMITIVE.put("Integer", "int");
        WRAPPER_TO_PRIMITIVE.put("Long", "long");
        WRAPPER_TO_PRIMITIVE.put("Float", "float");
        WRAPPER_TO_PRIMITIVE.put("Double", "double");
        WRAPPER_TO_PRIMITIVE.put("Character", "char");
        WRAPPER_TO_PRIMITIVE.put("String", "String");
        WRAPPER_TO_PRIMITIVE.put("Any", "Any");

        NULLABLE_WRAPPERS.add("dev_resteasy_grpc_arrays___Boolean");
        NULLABLE_WRAPPERS.add("dev_resteasy_grpc_arrays___Byte");
        NULLABLE_WRAPPERS.add("dev_resteasy_grpc_arrays___Short");
        NULLABLE_WRAPPERS.add("dev_resteasy_grpc_arrays___Integer");
        NULLABLE_WRAPPERS.add("dev_resteasy_grpc_arrays___Long");
        NULLABLE_WRAPPERS.add("dev_resteasy_grpc_arrays___Float");
        NULLABLE_WRAPPERS.add("dev_resteasy_grpc_arrays___Double");
        NULLABLE_WRAPPERS.add("dev_resteasy_grpc_arrays___Character");
        NULLABLE_WRAPPERS.add("dev_resteasy_grpc_arrays___String");

        JAVA_WRAPPER_TYPES.add("Boolean");
        JAVA_WRAPPER_TYPES.add("Byte");
        JAVA_WRAPPER_TYPES.add("Short");
        JAVA_WRAPPER_TYPES.add("Integer");
        JAVA_WRAPPER_TYPES.add("Long");
        JAVA_WRAPPER_TYPES.add("Float");
        JAVA_WRAPPER_TYPES.add("Double");
        JAVA_WRAPPER_TYPES.add("Character");
        JAVA_WRAPPER_TYPES.add("String");
        JAVA_WRAPPER_TYPES.add("java.lang.Boolean");
        JAVA_WRAPPER_TYPES.add("java.lang.Byte");
        JAVA_WRAPPER_TYPES.add("java.lang.Short");
        JAVA_WRAPPER_TYPES.add("java.lang.Integer");
        JAVA_WRAPPER_TYPES.add("java.lang.Long");
        JAVA_WRAPPER_TYPES.add("java.lang.Float");
        JAVA_WRAPPER_TYPES.add("java.lang.Double");
        JAVA_WRAPPER_TYPES.add("java.lang.Character");
        JAVA_WRAPPER_TYPES.add("java.lang.String");

        JAVA_WRAPPER_MAP.put("Boolean", "boolean");
        JAVA_WRAPPER_MAP.put("Byte", "byte");
        JAVA_WRAPPER_MAP.put("Short", "short");
        JAVA_WRAPPER_MAP.put("Integer", "int");
        JAVA_WRAPPER_MAP.put("Long", "long");
        JAVA_WRAPPER_MAP.put("Float", "float");
        JAVA_WRAPPER_MAP.put("Double", "double");
        JAVA_WRAPPER_MAP.put("Character", "char");

        ARRAY_CLASSES_TO_JAVABUF.put(new Boolean[0].getClass().getTypeName(),
                "new dev_resteasy_grpc_arrays___Boolean___WArray_ToJavabuf()");
        ARRAY_CLASSES_TO_JAVABUF.put(new boolean[0].getClass().getTypeName(),
                "new dev_resteasy_grpc_arrays___Boolean___Array_ToJavabuf()");
        ARRAY_CLASSES_TO_JAVABUF.put(new byte[0].getClass().getTypeName(),
                "new dev_resteasy_grpc_arrays___Byte___Array_ToJavabuf()");
        ARRAY_CLASSES_TO_JAVABUF.put(new Byte[0].getClass().getTypeName(),
                "new dev_resteasy_grpc_arrays___Byte___WArray_ToJavabuf()");
        ARRAY_CLASSES_TO_JAVABUF.put(new short[0].getClass().getTypeName(),
                "new dev_resteasy_grpc_arrays___Short___Array_ToJavabuf()");
        ARRAY_CLASSES_TO_JAVABUF.put(new Short[0].getClass().getTypeName(),
                "new dev_resteasy_grpc_arrays___Short___WArray_ToJavabuf()");
        ARRAY_CLASSES_TO_JAVABUF.put(new int[0].getClass().getTypeName(),
                "new dev_resteasy_grpc_arrays___Integer___Array_ToJavabuf()");
        ARRAY_CLASSES_TO_JAVABUF.put(new Integer[0].getClass().getTypeName(),
                "new dev_resteasy_grpc_arrays___Integer___WArray_ToJavabuf()");
        ARRAY_CLASSES_TO_JAVABUF.put(new long[0].getClass().getTypeName(),
                "new dev_resteasy_grpc_arrays___Long___Array_ToJavabuf()");
        ARRAY_CLASSES_TO_JAVABUF.put(new Long[0].getClass().getTypeName(),
                "new dev_resteasy_grpc_arrays___Long___WArray_ToJavabuf()");
        ARRAY_CLASSES_TO_JAVABUF.put(new float[0].getClass().getTypeName(),
                "new dev_resteasy_grpc_arrays___Float___Array_ToJavabuf()");
        ARRAY_CLASSES_TO_JAVABUF.put(new Float[0].getClass().getTypeName(),
                "new dev_resteasy_grpc_arrays___Float___WArray_ToJavabuf()");
        ARRAY_CLASSES_TO_JAVABUF.put(new double[0].getClass().getTypeName(),
                "new dev_resteasy_grpc_arrays___Double___Array_ToJavabuf()");
        ARRAY_CLASSES_TO_JAVABUF.put(new Double[0].getClass().getTypeName(),
                "new dev_resteasy_grpc_arrays___Double___WArray_ToJavabuf()");
        ARRAY_CLASSES_TO_JAVABUF.put(new char[0].getClass().getTypeName(),
                "new dev_resteasy_grpc_arrays___Character___Array_ToJavabuf()");
        ARRAY_CLASSES_TO_JAVABUF.put(new Character[0].getClass().getTypeName(),
                "new dev_resteasy_grpc_arrays___Character___WArray_ToJavabuf()");
        ARRAY_CLASSES_TO_JAVABUF.put(new String[0].getClass().getTypeName(),
                "new dev_resteasy_grpc_arrays___String___WArray_ToJavabuf()");
        ARRAY_CLASSES_TO_JAVABUF.put(new Object[0].getClass().getTypeName(),
                "new dev_resteasy_grpc_arrays___Any___WArray_ToJavabuf()");
        ARRAY_CLASSES_TO_JAVABUF.put("dev_resteasy_grpc_arrays___ArrayHolder",
                "new  dev_resteasy_grpc_arrays___ArrayHolder_ToJavabuf()");
        ARRAY_CLASSES_TO_JAVABUF.put("dev_resteasy_grpc_arrays___ArrayHolder___WArray",
                "new  dev_resteasy_grpc_arrays___ArrayHolder___WArray_ToJavabuf()");

        ARRAY_CLASSES_FROM_JAVABUF.put("dev_resteasy_grpc_arrays___Boolean___Array",
                "new dev_resteasy_grpc_arrays___Boolean___Array_FromJavabuf()");
        ARRAY_CLASSES_FROM_JAVABUF.put("dev_resteasy_grpc_arrays___Boolean___WArray",
                "new dev_resteasy_grpc_arrays___Boolean___WArray_FromJavabuf()");
        ARRAY_CLASSES_FROM_JAVABUF.put("dev_resteasy_grpc_arrays___Byte___Array",
                "new dev_resteasy_grpc_arrays___Byte___Array_FromJavabuf()");
        ARRAY_CLASSES_FROM_JAVABUF.put("dev_resteasy_grpc_arrays___Byte___WArray",
                "new dev_resteasy_grpc_arrays___Byte___WArray_FromJavabuf()");
        ARRAY_CLASSES_FROM_JAVABUF.put("dev_resteasy_grpc_arrays___Short___Array",
                "new dev_resteasy_grpc_arrays___Short___Array_FromJavabuf()");
        ARRAY_CLASSES_FROM_JAVABUF.put("dev_resteasy_grpc_arrays___Short___WArray",
                "new dev_resteasy_grpc_arrays___Short___WArray_FromJavabuf()");
        ARRAY_CLASSES_FROM_JAVABUF.put("dev_resteasy_grpc_arrays___Integer___Array",
                "new dev_resteasy_grpc_arrays___Integer___Array_FromJavabuf()");
        ARRAY_CLASSES_FROM_JAVABUF.put("dev_resteasy_grpc_arrays___Integer___WArray",
                "new dev_resteasy_grpc_arrays___Integer___WArray_FromJavabuf()");
        ARRAY_CLASSES_FROM_JAVABUF.put("dev_resteasy_grpc_arrays___Long___Array",
                "new dev_resteasy_grpc_arrays___Long___Array_FromJavabuf()");
        ARRAY_CLASSES_FROM_JAVABUF.put("dev_resteasy_grpc_arrays___Long___WArray",
                "new dev_resteasy_grpc_arrays___Long___WArray_FromJavabuf()");
        ARRAY_CLASSES_FROM_JAVABUF.put("dev_resteasy_grpc_arrays___Float___Array",
                "new dev_resteasy_grpc_arrays___Float___Array_FromJavabuf()");
        ARRAY_CLASSES_FROM_JAVABUF.put("dev_resteasy_grpc_arrays___Float___WArray",
                "new dev_resteasy_grpc_arrays___Float___WArray_FromJavabuf()");
        ARRAY_CLASSES_FROM_JAVABUF.put("dev_resteasy_grpc_arrays___Double___Array",
                "new dev_resteasy_grpc_arrays___Double___Array_FromJavabuf()");
        ARRAY_CLASSES_FROM_JAVABUF.put("dev_resteasy_grpc_arrays___Double___WArray",
                "new dev_resteasy_grpc_arrays___Double___WArray_FromJavabuf()");
        ARRAY_CLASSES_FROM_JAVABUF.put("dev_resteasy_grpc_arrays___Character___Array",
                "new dev_resteasy_grpc_arrays___Character___Array_FromJavabuf()");
        ARRAY_CLASSES_FROM_JAVABUF.put("dev_resteasy_grpc_arrays___Character___WArray",
                "new dev_resteasy_grpc_arrays___Character___WArray_FromJavabuf()");
        ARRAY_CLASSES_FROM_JAVABUF.put("dev_resteasy_grpc_arrays___String___WArray",
                "new dev_resteasy_grpc_arrays___String___WArray_FromJavabuf()");
        ARRAY_CLASSES_FROM_JAVABUF.put("dev_resteasy_grpc_arrays___Any___WArray",
                "new dev_resteasy_grpc_arrays___Any___WArray_FromJavabuf()");
        ARRAY_CLASSES_FROM_JAVABUF.put("dev_resteasy_grpc_arrays___ArrayHolder",
                "new  dev_resteasy_grpc_arrays___ArrayHolder_FromJavabuf()");
        ARRAY_CLASSES_FROM_JAVABUF.put("dev_resteasy_grpc_arrays___ArrayHolder___WArray",
                "new  dev_resteasy_grpc_arrays___ArrayHolder___WArray_FromJavabuf()");

        ARRAY_CLASSES_FROM_JAVABUF_STREAM.put(new boolean[0].getClass().getName(),
                "new dev_resteasy_grpc_arrays___Boolean___Array_FromJavabuf()");
        ARRAY_CLASSES_FROM_JAVABUF_STREAM.put(new Boolean[0].getClass().getName(),
                "new dev_resteasy_grpc_arrays___Boolean___WArray_FromJavabuf()");
        ARRAY_CLASSES_FROM_JAVABUF_STREAM.put(new byte[0].getClass().getName(),
                "new dev_resteasy_grpc_arrays___Byte___Array_FromJavabuf()");
        ARRAY_CLASSES_FROM_JAVABUF_STREAM.put(new Byte[0].getClass().getName(),
                "new dev_resteasy_grpc_arrays___Byte___WArray_FromJavabuf()");
        ARRAY_CLASSES_FROM_JAVABUF_STREAM.put(new short[0].getClass().getName(),
                "new dev_resteasy_grpc_arrays___Short___Array_FromJavabuf()");
        ARRAY_CLASSES_FROM_JAVABUF_STREAM.put(new Short[0].getClass().getName(),
                "new dev_resteasy_grpc_arrays___Short___WArray_FromJavabuf()");
        ARRAY_CLASSES_FROM_JAVABUF_STREAM.put(new int[0].getClass().getName(),
                "new dev_resteasy_grpc_arrays___Integer___Array_FromJavabuf()");
        ARRAY_CLASSES_FROM_JAVABUF_STREAM.put(new Integer[0].getClass().getName(),
                "new dev_resteasy_grpc_arrays___Integer___WArray_FromJavabuf()");
        ARRAY_CLASSES_FROM_JAVABUF_STREAM.put(new long[0].getClass().getName(),
                "new dev_resteasy_grpc_arrays___Long___Array_FromJavabuf()");
        ARRAY_CLASSES_FROM_JAVABUF_STREAM.put(new Long[0].getClass().getName(),
                "new dev_resteasy_grpc_arrays___Long___WArray_FromJavabuf()");
        ARRAY_CLASSES_FROM_JAVABUF_STREAM.put(new float[0].getClass().getName(),
                "new dev_resteasy_grpc_arrays___Float___Array_FromJavabuf()");
        ARRAY_CLASSES_FROM_JAVABUF_STREAM.put(new Float[0].getClass().getName(),
                "new dev_resteasy_grpc_arrays___Float___WArray_FromJavabuf()");
        ARRAY_CLASSES_FROM_JAVABUF_STREAM.put(new double[0].getClass().getName(),
                "new dev_resteasy_grpc_arrays___Double___Array_FromJavabuf()");
        ARRAY_CLASSES_FROM_JAVABUF_STREAM.put(new Double[0].getClass().getName(),
                "new dev_resteasy_grpc_arrays___Double___WArray_FromJavabuf()");
        ARRAY_CLASSES_FROM_JAVABUF_STREAM.put(new char[0].getClass().getName(),
                "new dev_resteasy_grpc_arrays___Character___Array_FromJavabuf()");
        ARRAY_CLASSES_FROM_JAVABUF_STREAM.put(new Character[0].getClass().getName(),
                "new dev_resteasy_grpc_arrays___Character___WArray_FromJavabuf()");
        ARRAY_CLASSES_FROM_JAVABUF_STREAM.put(new String[0].getClass().getName(),
                "new dev_resteasy_grpc_arrays___String___WArray_FromJavabuf()");
        ARRAY_CLASSES_FROM_JAVABUF_STREAM.put(new Object[0].getClass().getName(),
                "new dev_resteasy_grpc_arrays___Any___WArray_FromJavabuf()");
        ARRAY_CLASSES_FROM_JAVABUF_STREAM.put("dev_resteasy_grpc_arrays___ArrayHolder",
                "new  dev_resteasy_grpc_arrays___ArrayHolder_FromJavabuf()");
        ARRAY_CLASSES_FROM_JAVABUF_STREAM.put("dev_resteasy_grpc_arrays___ArrayHolder___WArray",
                "new  dev_resteasy_grpc_arrays___ArrayHolder___WArray_FromJavabuf()");

        BUILTIN_TO_JAVABUF.put("boolean", "new gBoolean_FromJavabuf()");
        BUILTIN_TO_JAVABUF.put("byte", "new gByte_FromJavabuf()");
        BUILTIN_TO_JAVABUF.put("short", "new gShort_FromJavabuf()");
        BUILTIN_TO_JAVABUF.put("int", "new gInteger_FromJavabuf()");
        BUILTIN_TO_JAVABUF.put("long", "new gLong_FromJavabuf()");
        BUILTIN_TO_JAVABUF.put("float", "new gFloat_FromJavabuf()");
        BUILTIN_TO_JAVABUF.put("double", "new gDouble_FromJavabuf()");
        BUILTIN_TO_JAVABUF.put("char", "new gCharacter_FromJavabuf()");

        IMPORTS.add("jakarta.ws.rs.core.GenericType");
        IMPORTS.add("jakarta.ws.rs.core.MultivaluedMap");
        IMPORTS.add("java.lang.reflect.Array");
        IMPORTS.add("java.lang.reflect.Const");
        IMPORTS.add("java.lang.reflect.Field");
        IMPORTS.add("java.lang.reflect.Method");
        IMPORTS.add("java.lang.reflect.Type");
        IMPORTS.add("java.util.AbstractList");
        IMPORTS.add("java.util.ArrayList");
        IMPORTS.add("java.util.HashMap");
        IMPORTS.add("java.util.HashSet");
        IMPORTS.add("java.util.Collection");
        IMPORTS.add("java.util.List");
        IMPORTS.add("java.util.Map");
        IMPORTS.add("java.util.Set");
        IMPORTS.add("com.google.protobuf.Any");
        IMPORTS.add("com.google.protobuf.ByteString");
        IMPORTS.add("com.google.protobuf.DescriptorProtos.FieldDescriptorProto");
        IMPORTS.add("com.google.protobuf.Descriptors");
        IMPORTS.add("com.google.protobuf.Descriptors.Descriptor");
        IMPORTS.add("com.google.protobuf.Descriptors.FieldDescriptor");
        IMPORTS.add("com.google.protobuf.Message");
        IMPORTS.add("com.google.protobuf.Message.Builder");
        IMPORTS.add(HolderMap.class.getCanonicalName());
        IMPORTS.add(Utility.class.getCanonicalName());
        IMPORTS.add(AssignFromJavabuf.class.getCanonicalName());
        IMPORTS.add(AssignToJavabuf.class.getCanonicalName());
        IMPORTS.add(JavabufTranslator.class.getCanonicalName());
        IMPORTS.add(ReturnJavaClass.class.getCanonicalName());
        IMPORTS.add(TranslateFromJavabuf.class.getCanonicalName());
        IMPORTS.add(TranslateToJavabuf.class.getCanonicalName());
    }

    public static void main(String[] args) {
        if (args == null || args.length != 5) {
            logger.info("need five args:");
            logger.info("  arg[0]: output directory");
            logger.info("  arg[1]: javabuf wrapper class name");
            logger.info("  arg[2]: prefix");
            logger.info("  arg[3): .proto file");
            logger.info("  arg[4): build directory (target)");
            return;
        }
        try {
            getAggregates(args[3]);
            getClassnameMap(args);
            int index = args[1].lastIndexOf('.');
            String simpleName = index < 0 ? args[1] : args[1].substring(index + 1);
            String translatorClass = simpleName + "JavabufTranslator";
            Class<?>[] wrappedClasses = getWrappedClasses(args);
            StringBuilder sb = new StringBuilder();
            classHeader(args, translatorClass, wrappedClasses, sb);
            classBody(args, wrappedClasses, sb);
            finishClass(sb);
            writeTranslatorClass(args, translatorClass, sb);
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
        }
    }

    static void getAggregates(String path) {
        Path file = Paths.get(path);
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("// List: ")) {
                    String javaClassname = getJavaClassname(line, 9);
                    String javabufClassname = getJavabufClassname(reader);
                    String collectionType = getCollectionType(reader);
                    AGGREGATES_INV.put(javaClassname, javabufClassname);
                    LISTS.put(javabufClassname, javaClassname);
                    COLLECTION_TYPE.put(javabufClassname, collectionType);
                } else if (line.startsWith("// Set: ")) {
                    String javaClassname = getJavaClassname(line, 8);
                    String javabufClassname = getJavabufClassname(reader);
                    String collectionType = getCollectionType(reader);
                    AGGREGATES_INV.put(javaClassname, javabufClassname);
                    SETS.put(javabufClassname, javaClassname);
                    COLLECTION_TYPE.put(javabufClassname, collectionType);
                } else if (line.startsWith("// Multimap: ")) {
                    String javaClassname = getJavaClassname(line, 13);
                    String javabufClassname = getJavabufClassname(reader);
                    AGGREGATES_INV.put(javaClassname, javabufClassname);
                    MULTIMAPS.put(javabufClassname, javaClassname);
                    processMapTypes(reader, javabufClassname);
                } else if (line.startsWith("// Map: ")) {
                    String javaClassname = getJavaClassname(line, 8);
                    String javabufClassname = getJavabufClassname(reader);
                    AGGREGATES_INV.put(javaClassname, javabufClassname);
                    MAPS.put(javabufClassname, javaClassname);
                    processMapTypes(reader, javabufClassname);
                } else if (line.startsWith("// Record: ")) {
                    boolean generic = line.contains("<");
                    String javaClassname = getJavaClassname(line, 11);
                    String javabufClassname = getJavabufClassname(reader);
                    RECORDS.put(javabufClassname, javaClassname);
                    if (generic) {
                        GENERICS.put(javabufClassname, javaClassname);
                    }
                } else if (line.startsWith("// Type: ") && line.contains("<")) {
                    String javaClassname = getJavaClassname(line, 9);
                    String javabufClassname = getJavabufClassname(reader);
                    GENERICS.put(javabufClassname, javaClassname);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void getClassnameMap(String[] args) {
        Path file = Paths.get(args[3]);
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                if ("//////////  synthetic names: //////////".equals(line)) {
                    break;
                }
            }
            while ((line = reader.readLine()) != null) {
                int pos = line.indexOf("->");
                CLASSNAMES.put(line.substring(3, pos), line.substring(pos + 2));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String getJavaClassname(String line, int beginning) throws Exception {
        return line.substring(beginning);
    }

    private static String getJavabufClassname(BufferedReader reader) throws Exception {
        String line = reader.readLine();
        int end = line.indexOf(' ', 9);
        return line.substring(8, end);
    }

    private static String getCollectionType(BufferedReader reader) throws Exception {
        String line = reader.readLine();
        line = reader.readLine();
        return line.substring(4);
    }

    private static void processMapTypes(BufferedReader reader, String javabufClassname) throws IOException {
        String line = reader.readLine();
        line = reader.readLine();
        String keyTypeJava = line.substring(4, line.indexOf("->"));
        String valueTypeJava = line.substring(line.indexOf("->") + 2);
        KEY_TYPE_JAVA.put(javabufClassname, keyTypeJava);
        VALUE_TYPE_JAVA.put(javabufClassname, valueTypeJava);
        reader.readLine();
        line = reader.readLine();
        String keyTypeJavabuf = line.substring(4, line.indexOf(" ", 5));
        KEY_TYPE_JAVABUF.put(javabufClassname, keyTypeJavabuf);
        line = reader.readLine();
        String valueTypeJavabuf = line.substring(4, line.indexOf(" ", 5));
        VALUE_TYPE_JAVABUF.put(javabufClassname, valueTypeJavabuf);
    }

    public static Class<?>[] getWrappedClasses(String[] args) throws ClassNotFoundException {
        List<Class<?>> wrapperClasses = getWrapperClasses(args);
        List<Class<?>> wrappedClasses = new ArrayList<Class<?>>();
        for (Class<?> clazz : wrapperClasses) {
            wrappedClasses.addAll(Arrays.asList(clazz.getClasses()));
        }
        return wrappedClasses.toArray(new Class<?>[wrappedClasses.size()]);
    }

    private static List<Class<?>> getWrapperClasses(String[] args) throws ClassNotFoundException {
        List<Class<?>> list = new ArrayList<Class<?>>();
        Class<?> wrapperClass = Class.forName(args[1] + "_proto", true, Thread.currentThread()
                .getContextClassLoader());
        list.add(wrapperClass);
        try {
            Class<?> arrayWrapperClass = Class.forName("dev.resteasy.grpc.arrays.Array_proto", true,
                    Thread.currentThread().getContextClassLoader());
            list.add(arrayWrapperClass);
            return list;
        } catch (Exception ignore) {
            // Array_proto class is not available: ignore
            ignore.printStackTrace();
            return list;
        }
    }

    private static void classHeader(String[] args, String translatorClass, Class<?>[] wrappedClasses, StringBuilder sb) {
        sb.append("package ").append(wrappedClasses[0].getPackage().getName()).append(";" + LS + LS);
        imports(wrappedClasses, sb, args);
        sb.append("@SuppressWarnings({\"deprecation\", \"rawtypes\", \"unchecked\"})" + LS);
        sb.append("public class ")
                .append(translatorClass)
                .append(" implements JavabufTranslator {" + LS);
    }

    private static void imports(Class<?>[] wrappedClasses, StringBuilder sb, String[] args) {
        sb.append("import jakarta.ws.rs.core.GenericType;" + LS)
                .append("import jakarta.ws.rs.core.MultivaluedMap;" + LS)
                .append("import java.lang.reflect.Array;" + LS)
                .append("import java.lang.reflect.Constructor;" + LS)
                .append("import java.lang.reflect.Field;" + LS)
                .append("import java.lang.reflect.Method;" + LS)
                .append("import java.lang.reflect.Type;" + LS)
                .append("import java.util.AbstractList;" + LS)
                .append("import java.util.ArrayList;" + LS)
                .append("import java.util.HashMap;" + LS)
                .append("import java.util.HashSet;" + LS)
                .append("import java.util.Collection;" + LS)
                .append("import java.util.List;" + LS)
                .append("import java.util.Map;" + LS)
                .append("import java.util.Set;" + LS)
                .append("import com.google.protobuf.Any;" + LS)
                .append("import com.google.protobuf.ByteString;" + LS)
                .append("import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;" + LS)
                .append("import com.google.protobuf.Descriptors;" + LS)
                .append("import com.google.protobuf.Descriptors.Descriptor;" + LS)
                .append("import com.google.protobuf.Descriptors.FieldDescriptor;" + LS)
                .append("import com.google.protobuf.Message;" + LS)
                .append("import com.google.protobuf.Message.Builder;" + LS)
                .append("import ").append(HolderMap.class.getCanonicalName()).append(";" + LS)
                .append("import ").append(Utility.class.getCanonicalName()).append(";" + LS)
                .append("import ").append(AssignFromJavabuf.class.getCanonicalName()).append(";" + LS)
                .append("import ").append(AssignToJavabuf.class.getCanonicalName()).append(";" + LS)
                .append("import ").append(JavabufTranslator.class.getCanonicalName()).append(";" + LS)
                .append("import ").append(ReturnJavaClass.class.getCanonicalName()).append(";" + LS)
                .append("import ").append(TranslateFromJavabuf.class.getCanonicalName()).append(";" + LS)
                .append("import ").append(TranslateToJavabuf.class.getCanonicalName()).append(";" + LS);
        for (Class<?> clazz : wrappedClasses) {
            if (IMPORTS.contains(clazz.getCanonicalName())) {
                continue;
            }
            if (clazz.isInterface()) {
                continue;
            }
            if (clazz.getSimpleName().endsWith("OrBuilder")) {
                continue;
            }
            String simpleName = clazz.getSimpleName();
            if ("gEmpty".equals(simpleName)) {
                continue;
            }
            if (LISTS.containsKey(clazz.getSimpleName()) ||
                    SETS.containsKey(clazz.getSimpleName()) ||
                    MAPS.containsKey(clazz.getSimpleName()) ||
                    MULTIMAPS.containsKey(clazz.getSimpleName()) ||
                    RECORDS.containsKey(clazz.getSimpleName())) {
                String s = clazz.getName().replace("$", ".");
                if (IMPORTS.contains(s)) {
                    continue;
                }
                IMPORTS.add(s);
                sb.append("import ").append(s).append(";" + LS);
                continue;
            }
            String elementType = simpleName.contains("_Array") ? simpleName.substring(0, simpleName.indexOf("_Array")) : "";
            String wrappedType = simpleName.contains("_wrapper") ? simpleName.substring(0, simpleName.indexOf("_wrapper")) : "";
            if (PRIMITIVE_WRAPPER_TYPES.containsKey(simpleName)
                    || PRIMITIVE_WRAPPER_TYPES.containsKey(elementType)
                    || PRIMITIVE_WRAPPER_TYPES.containsKey(wrappedType)) {
                String s = clazz.getName().replace("$", ".");
                if (IMPORTS.contains(s)) {
                    continue;
                }
                IMPORTS.add(s);
                sb.append("import ").append(s).append(";" + LS);
            } else if ("GeneralEntityMessage".equals(simpleName)
                    || "GeneralReturnMessage".equals(simpleName)
                    || "ServletInfo".equals(simpleName)
                    || "gNewCookie".equals(simpleName)
                    || "gCookie".equals(simpleName)
                    || "gHeader".equals(simpleName)
                    || "FormMap".equals(simpleName)
                    || "FormValues".equals(simpleName)) {
                continue;
            } else if (clazz.getName().contains("_HIDDEN_")
                    || "dev_resteasy_grpc_arrays___ArrayHolder".equals(simpleName)
                    || "dev_resteasy_grpc_arrays___ArrayHolder___WArray".equals(simpleName)
                    || clazz.getName().endsWith("___Array")
                    || clazz.getName().endsWith("___WArray")
                    || clazz.getName().endsWith("_wrapper")
                    || "dev.resteasy.grpc.arrays".equals(clazz.getPackageName())) {
                String s = clazz.getName().replace("$", ".");
                if (IMPORTS.contains(s)) {
                    continue;
                }
                IMPORTS.add(s);
                sb.append("import ").append(s).append(";" + LS);
            } else {
                String s = disambiguateClassname(originalCanonicalClassName(clazz.getName()));
                if (!IMPORTS.contains(s)) {
                    IMPORTS.add(s);
                    sb.append("import ").append(s).append(";" + LS);
                }
                s = clazz.getName().replace("$", ".");
                if (!IMPORTS.contains(s)) {
                    IMPORTS.add(s);
                    sb.append("import ").append(s).append(";" + LS);
                }
            }
        }
        sb.append("import java.io.IOException;" + LS).append("import java.io.InputStream;" + LS);
        sb.append("" + LS);
    }

    private static void classBody(String[] args, Class<?>[] wrappedClasses, StringBuilder sb) throws Exception {
        privateVariables(sb, args);
        staticInit(wrappedClasses, args, sb);
        publicMethods(sb, wrappedClasses[0], args);
        privateMethods(sb, wrappedClasses, args);
        sb.append("   /******************************************************************************************" + LS)
                .append("                     TranslateFromJavabuf and TranslateToJavabuf classes      " + LS)
                .append("   ******************************************************************************************/" + LS);
        for (Class<?> clazz : wrappedClasses) {
            if (clazz.isInterface()) {
                continue;
            }
            String simpleName = clazz.getSimpleName();
            if ("GeneralEntityMessage".equals(simpleName) || "GeneralReturnMessage".equals(simpleName)) {
                continue;
            }
            createTranslator(args, clazz, sb);
        }
    }

    private static void staticInit(Class<?>[] classes, String[] args, StringBuilder sb) throws ClassNotFoundException {
        sb.append(LS + "   static {" + LS);
        Iterator<String> iteratorTo = ARRAY_CLASSES_TO_JAVABUF.keySet().iterator();
        Iterator<String> iteratorFrom = ARRAY_CLASSES_FROM_JAVABUF.keySet().iterator();
        for (int i = 0; i < ARRAY_CLASSES_TO_JAVABUF.size(); i++) {
            String keyTo = iteratorTo.next();
            String keyFrom = iteratorFrom.next();
            sb.append("      toJavabufMap.put(\"").append(keyTo).append("\", ").append(ARRAY_CLASSES_TO_JAVABUF.get(keyTo))
                    .append(");" + LS);
            sb.append("      fromJavabufMap.put(\"").append(keyFrom).append("\", ")
                    .append(ARRAY_CLASSES_FROM_JAVABUF.get(keyFrom)).append(");" + LS);
        }
        for (Class<?> clazz : classes) {
            if (clazz.isInterface()) {
                continue;
            }
            String simpleName = clazz.getSimpleName();
            if ("gEmpty".equals(simpleName)
                    || "GeneralEntityMessage".equals(simpleName)
                    || "GeneralReturnMessage".equals(simpleName)
                    || "ServletInfo".equals(simpleName)
                    || "gNewCookie".equals(simpleName)
                    || "gCookie".equals(simpleName)
                    || "gHeader".equals(simpleName)
                    || "FormMap".equals(simpleName)
                    || "FormValues".equals(simpleName)) {
                continue;
            }
            if ("dev.resteasy.grpc.arrays".equals(clazz.getPackageName())) {
                continue;
            } // ???
            if (clazz.getSimpleName().startsWith("dev_resteasy_grpc_arrays___ArrayHolder") ||
                    clazz.getSimpleName().endsWith("___wrapper")) {
                continue;
            }

            boolean isAbstract = false;
            if (!PRIMITIVE_WRAPPER_TYPES.containsKey(simpleName)) {
                try {
                    isAbstract = isAbstractOrInterface(clazz);
                } catch (ClassNotFoundException e) {
                    logger.warn("Class not found: " + simpleName);
                    continue;
                }
            }
            if (simpleName.endsWith("___Array")) {
                String name = originalClassName(simpleName);
                if ("dev.resteasy.grpc.arrays.Integer".equals(name)) {
                    name = "dev.resteasy.grpc.arrays.Int";
                } else if ("dev.resteasy.grpc.arrays.Character".equals(name)) {
                    name = "dev.resteasy.grpc.arrays.Char";
                }
                sb.append(LS + "      toJavabufMap.put(\"")
                        .append(name + ".Array" + "\" , new ")
                        .append(simpleName)
                        .append("_ToJavabuf());" + LS);
                sb.append("      fromJavabufMap.put(")
                        .append("\"" + simpleName + "\"")
                        .append(", new ")
                        .append(simpleName)
                        .append("_FromJavabu());" + LS);
            } else if (simpleName.endsWith("___WArray")) {
                String javaName = originalClassName(simpleName);
                sb.append(LS + "      toJavabufMap.put(\"")
                        .append(javaName + "[]\" , new ")
                        .append(simpleName)
                        .append("_ToJavabuf());" + LS);
                sb.append("      fromJavabufMap.put(")
                        .append("\"" + simpleName + "\"")
                        .append(", new ")
                        .append(simpleName)
                        .append("_FromJavabuf());" + LS);
            } else {
                String originalClassName = GENERICS.containsKey(simpleName) ? GENERICS.get(simpleName)
                        : originalClassName(simpleName);
                if (!isAbstract) {
                    sb.append(LS + "      toJavabufMap.put(\"")
                            .append(originalClassName + "\" , new ")
                            .append(simpleName)
                            .append("_ToJavabuf());" + LS);
                    sb.append("      fromJavabufMap.put(")
                            .append("\"" + simpleName + "\"")
                            .append(", new ")
                            .append(simpleName)
                            .append("_FromJavabuf());" + LS);
                    sb.append("      toJavabufClassMap.put(\"")
                            .append(originalClassName + "\", ")
                            .append(simpleName + ".class);" + LS);
                    sb.append("      fromJavabufClassMap.put(")
                            .append(simpleName + ".class.getName(), ")
                            .append(getJavabufClassValue(simpleName, false, true) + ");" + LS);
                }
            }
        }
        sb.append(LS);
        for (String s : AGGREGATES_INV.keySet()) {
            sb.append(LS + "      toJavabufMap.put(\"")
                    .append(s + "\" , new ")
                    .append(AGGREGATES_INV.get(s))
                    .append("_ToJavabuf());" + LS);
            sb.append("      fromJavabufMap.put(\"")
                    .append(AGGREGATES_INV.get(s))
                    .append("\", new ")
                    .append(AGGREGATES_INV.get(s))
                    .append("_FromJavabuf());" + LS);
        }
        sb.append(LS);
        sb.append(LS);
        sb.append("      WRAPPER_TYPES.add(java.lang.Boolean.class);" + LS);
        sb.append("      WRAPPER_TYPES.add(java.lang.Byte.class);" + LS);
        sb.append("      WRAPPER_TYPES.add(java.lang.Short.class);" + LS);
        sb.append("      WRAPPER_TYPES.add(java.lang.Integer.class);" + LS);
        sb.append("      WRAPPER_TYPES.add(java.lang.Long.class);" + LS);
        sb.append("      WRAPPER_TYPES.add(java.lang.Float.class);" + LS);
        sb.append("      WRAPPER_TYPES.add(java.lang.Double.class);" + LS);
        sb.append("      WRAPPER_TYPES.add(java.lang.Character.class);" + LS);
        sb.append("      WRAPPER_TYPES.add(java.lang.String.class);" + LS + LS);
        sb.append("      Method[] methods = dev_resteasy_grpc_arrays___ArrayHolder.Builder.class.getDeclaredMethods();" + LS)
                .append("      for (Method m : methods) {" + LS)
                .append("         if (m.getName().startsWith(\"set\")) {" + LS)
                .append("            arraySetters.put(m.getParameterTypes()[0], m);" + LS)
                .append("         }" + LS)
                .append("      }" + LS);
        sb.append("   }" + LS + LS);
        writeNormalizer(args, sb);
    }

    private static void writeNormalizer(String[] args, StringBuilder sb) {
        sb.append("   static {" + LS);
        Path file = Paths.get(args[4], "normalizer");
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line = reader.readLine();
            while (line != null) {
                int n = line.indexOf("|");
                String l1 = line.substring(0, n);
                String l2 = line.substring(n + 1);
                sb.append("      NORMALIZER.put(\"")
                        .append(l1)
                        .append("\", new GenericType<")
                        .append(l2)
                        .append(">() {});")
                        .append(LS);
                line = reader.readLine();
            }
            sb.append("   }" + LS + LS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private static boolean isAbstractOrInterface(Class<?> clazz) throws ClassNotFoundException {
        if (clazz.getName().endsWith("___Array") || clazz.getName().endsWith("___WArray")) {
            return false;
        }
        String originalName = originalSimpleName(clazz.getName());
        String className = javabufToJava(clazz.getName(), originalName, false);
        Class<?> c = Class.forName(className, true,
                Thread.currentThread().getContextClassLoader());
        if (List.class.isAssignableFrom(c) || Map.class.isAssignableFrom(c) || Set.class.isAssignableFrom(c)) {
            return false;
        }
        return Modifier.isAbstract(c.getModifiers()) || c.isInterface();
    }

    private static void publicMethods(StringBuilder sb, Class<?> clazz, String[] args) {
        sb.append("   /******************************************************************************************" + LS)
                .append("      Implement dev.resteasy.grpc.bridge.runtime.protobuf.JavabufTranslator interface" + LS)
                .append("   ******************************************************************************************/" + LS)
                .append("   @Override" + LS)
                .append("   public boolean handlesFromJavabuf(Type genericType, Class<?> clazz) {" + LS)
                .append("      return clazz.isPrimitive() || " + LS)
                .append("         (genericType != null && toJavabufMap.containsKey(simplifyTypeName(normalize(genericType).getTypeName()))) || "
                        + LS)
                .append("         toJavabufMap.containsKey(clazz.getName());" + LS)
                .append("   }" + LS + LS)

                .append("   @Override" + LS)
                .append("   public boolean handlesToJavabuf(Type genericType, Class<?> clazz) {" + LS)
                .append("      return clazz.isPrimitive() || " + LS)
                .append("         (genericType != null && toJavabufMap.containsKey(simplifyTypeName(normalize(genericType).getTypeName()))) || "
                        + LS)
                .append("         toJavabufMap.containsKey(clazz.getName());" + LS)
                .append("   }" + LS + LS)

                .append("   @Override" + LS)
                .append("   public Object translateFromJavabuf(Message message) {" + LS)
                .append("      String s = null;" + LS)
                .append("      try {" + LS)
                .append("         s = message.getDescriptorForType().getFullName();" + LS)
                .append("         s = s.substring(s.lastIndexOf(\".\") + 1);" + LS)
                .append("         TranslateFromJavabuf tfj = fromJavabufMap.get(s);" + LS)
                .append("         if (tfj == null) {" + LS)
                .append("            tfj = rawAggregateTranslationFromJavabuf(s, message);" + LS)
                .append("         }" + LS)
                .append("         if (tfj == null) {" + LS)
                .append("            throw new RuntimeException(\"translateFromJavabuf(): \" + message.getClass() + \", \" + s + \" is not recognized\" + message.toString());"
                        + LS)
                .append("         }" + LS)
                .append("         return tfj.assignFromJavabuf(message);" + LS)
                .append("      } catch (Exception e) {" + LS)
                .append("         throw new RuntimeException(e);" + LS)
                .append("      }" + LS)
                .append("   }" + LS + LS)

                .append("   @Override" + LS)
                .append("   public Message translateToJavabuf(Object o) {" + LS)
                .append("      return translateToJavabuf(o, null);" + LS)
                .append("   }" + LS + LS)

                .append("   @Override" + LS)
                .append("   public Message translateToJavabuf(Object o, GenericType genericType) {" + LS)
                .append("      TranslateToJavabuf ttj = null;" + LS)
                .append("      if (genericType != null) {" + LS)
                .append("         GenericType<?> gt = normalize(genericType);" + LS)
                .append("         ttj = toJavabufMap.get(simplifyTypeName(gt.getType().toString()));" + LS)
                .append("         if (ttj == null) {" + LS)
                .append("            ttj = toJavabufMap.get(genericType.getRawType().getName());" + LS)
                .append("         }" + LS)
                .append("      }" + LS)
                .append("      if (ttj == null) {" + LS)
                .append("         ttj = toJavabufMap.get(o.getClass().getTypeName());" + LS)
                .append("      }" + LS)
                .append("      if (ttj == null && o.getClass().isArray() && o.getClass().componentType().isArray()) {" + LS)
                .append("         return new dev_resteasy_grpc_arrays___ArrayHolder_ToJavabuf().assignToJavabuf(o);" + LS)
                .append("      }" + LS)
                .append("      if (ttj == null) {" + LS)
                .append("         ttj = rawAggregateTranslationToJavabuf(o);" + LS)
                .append("      }" + LS)
                .append("      if (ttj == null) {" + LS)
                .append("         throw new RuntimeException(\"translateToJavabuf(): \" + o.getClass().getName() + \" is not recognized\");"
                        + LS)
                .append("      }" + LS)
                .append("      ttj.clear();" + LS)
                .append("      return ttj.assignToJavabuf(o);" + LS)
                .append("   }" + LS + LS)

                .append("   @Override" + LS)
                .append("   public Class translatefromJavabufClass(String classname) {" + LS)
                .append("      return fromJavabufClassMap.get(classname);" + LS)
                .append("   }" + LS + LS)

                .append("   @Override" + LS)
                .append("   public Class translateToJavabufClass(Class<?> clazz) {" + LS)
                .append("      return toJavabufClassMap.get(clazz.getName());" + LS)
                .append("   }" + LS + LS)

                .append("   @Override" + LS)
                .append("   public String getOuterClassname() {" + LS)
                .append("      return \"").append(clazz.getDeclaringClass().getName()).append("\";" + LS)
                .append("   }" + LS + LS)

                .append("   @Override" + LS)
                .append("   public GenericType<?> normalize(GenericType<?> genericType) {" + LS)
                .append("      if (NORMALIZER.containsKey(simplifyTypeName(genericType.getType().getTypeName()))) {" + LS)
                .append("         return NORMALIZER.get(simplifyTypeName(genericType.getType().getTypeName()));" + LS)
                .append("      }" + LS)
                .append("      return Utility.objectify(genericType);" + LS)
                .append("   }" + LS + LS)

                .append("   @Override" + LS)
                .append("   public Type normalize(Type type) {" + LS)
                .append("       if (NORMALIZER.containsKey(simplifyTypeName(type.getTypeName()))) {" + LS)
                .append("          return NORMALIZER.get(simplifyTypeName(type.getTypeName())).getType();" + LS)
                .append("       }" + LS)
                .append("       return Utility.objectify(type);" + LS)
                .append("     }" + LS + LS);
    }

    private static void createTranslator(String[] args, Class<?> clazz, StringBuilder sb) throws Exception {
        createTranslatorToJavabuf(args, clazz, sb);
        createTranslatorFromJavabuf(args, clazz, sb);
    }

    private static void privateVariables(StringBuilder sb, String[] args) {
        sb.append("   private static JavabufTranslator INSTANCE = new " + args[2] + "JavabufTranslator();" + LS);
        sb.append(
                "   private static Map<String, TranslateToJavabuf> toJavabufMap = new HashMap<String, TranslateToJavabuf>();"
                        + LS);
        sb.append(
                "   private static Map<String, TranslateFromJavabuf> fromJavabufMap = new HashMap<String, TranslateFromJavabuf>();"
                        + LS);
        sb.append("   private static JavabufTranslator translator = new ").append(args[1]).append("JavabufTranslator();" + LS);
        sb.append("   private static Set<Class<?>> WRAPPER_TYPES = new HashSet<Class<?>>();" + LS);
        sb.append(
                "   private static Map<String, GenericType<?>> NORMALIZER = new HashMap<String, GenericType<?>>();" + LS + LS);
        sb.append("   private static Map<String, Class> toJavabufClassMap = new HashMap<String, Class>();" + LS);
        sb.append("   private static Map<String, Class<?>> fromJavabufClassMap = new HashMap<String, Class<?>>();"
                + LS);
        sb.append("   private static final Map<Class<?>, Method> arraySetters = new HashMap<Class<?>, Method>();" + LS);
        sb.append("   private static Map<String, Constructor<?>> constructors = new HashMap<String, Constructor<?>>();" + LS);
    }

    private static void privateMethods(StringBuilder sb, Class<?>[] classes, String[] args) {
        sb.append("   /******************************************************************************************" + LS)
                .append("                                    private methods" + LS)
                .append("   ******************************************************************************************/" + LS)
                .append("   private static AssignToJavabuf toJavabuf(Class<?> clazz, FieldDescriptor fd) {" + LS)
                .append("      try {" + LS)
                .append("         AssignToJavabuf assignToJavabuf = (obj, messageBuilder) -> {" + LS)
                .append("            try {" + LS)
                .append("               if (obj == null) {" + LS)
                .append("                  return;" + LS)
                .append("               }" + LS)
                .append("               final Field field = Utility.getField(clazz, fd.getName());" + LS)
                .append("               field.setAccessible(true);" + LS)
                .append("               if (field.getType().isArray() || (field.get(obj) != null && field.get(obj).getClass().isArray())) {"
                        + LS)
                .append("                  Object array = field.get(obj);" + LS)
                .append("                  if (array == null) {" + LS)
                .append("                     return;" + LS)
                .append("                  }" + LS)
                .append("                  Class<?> componentType = array.getClass().getComponentType();" + LS)
                .append("                  if (isPrimitive(fd)) {" + LS)
                .append("                     toPrimitiveJavabufArray(messageBuilder, fd, componentType, array);" + LS)
                .append("                  } else {" + LS)
                .append("                     messageBuilder.setField(fd, INSTANCE.translateToJavabuf(array));" + LS)
                .append("                  }" + LS)
                .append("               } else if (\".google.protobuf.Any\".equals(fd.toProto().getTypeName())) {" + LS)
                .append("                  if (field.get(obj) != null) {" + LS)
                .append("                     Class<?> c = field.get(obj).getClass();" + LS)
                .append("                     if (Any.class.equals(c)) {" + LS)
                .append("                        messageBuilder.setField(fd, field.get(obj));" + LS)
                .append("                     } else {" + LS)
                .append("                        Message message = toJavabufMap.get(c.getName()).assignToJavabuf(field.get(obj));"
                        + LS)
                .append("                        messageBuilder.setField(fd, Any.pack(message));" + LS)
                .append("                     }" + LS)
                .append("                  }" + LS)
                .append("               } else if (field.get(obj) != null" + LS)
                .append("                        && !field.get(obj).getClass().isPrimitive()" + LS)
                .append("                        && !WRAPPER_TYPES.contains(field.get(obj).getClass())) {" + LS)
                .append("                  if (toJavabufMap.keySet().contains(getTypeName(fd))) {" + LS)
                .append("                     Message message = toJavabufMap.get(getTypeName(fd)).assignToJavabuf(field.get(obj));"
                        + LS)
                .append("                     if (message != null) {" + LS)
                .append("                        messageBuilder.setField(fd, message);" + LS)
                .append("                     }" + LS)
                .append("                  } else if (toJavabufMap.keySet().contains(field.get(obj).getClass().getTypeName())) {"
                        + LS)
                .append("                     Message message = toJavabufMap.get(field.get(obj).getClass().getTypeName()).assignToJavabuf(field.get(obj));"
                        + LS)
                .append("                     if (message != null) {" + LS)
                .append("                        messageBuilder.setField(fd, message);" + LS)
                .append("                     }" + LS)
                .append("                  } else {" + LS)
                .append("                     throw new RuntimeException(field.get(obj) + \" type not recognized\");" + LS)
                .append("                  }" + LS)
                .append("               } else if (field.get(obj) != null) {" + LS)
                .append("                  if (Byte.class.equals(field.getType()) || byte.class.equals(field.getType())) {"
                        + LS)
                .append("                     Byte b = field.getByte(obj);" + LS)
                .append("                     messageBuilder.setField(fd, b.intValue());" + LS)
                .append("                  } else if (Short.class.equals(field.getType()) || short.class.equals(field.getType())) {"
                        + LS)
                .append("                     Short s = field.getShort(obj);" + LS)
                .append("                     messageBuilder.setField(fd, s.intValue());" + LS)
                .append("                  } else if (Character.class.equals(field.getType()) || char.class.equals(field.getType())) {"
                        + LS)
                .append("                     Character c = field.getChar(obj);" + LS)
                .append("                     messageBuilder.setField(fd, String.valueOf(c));" + LS)
                .append("                  } else if (Descriptors.FieldDescriptor.JavaType.BYTE_STRING.equals(fd.getJavaType())) {"
                        + LS)
                .append("                     ByteString bs = ByteString.copyFrom((byte[]) field.get(obj));" + LS)
                .append("                     messageBuilder.setField(fd, bs);" + LS)
                .append("                  } else {" + LS)
                .append("                     messageBuilder.setField(fd, field.get(obj));" + LS)
                .append("                  }" + LS)
                .append("               }" + LS)
                .append("            } catch (Exception e) {" + LS)
                .append("               throw new RuntimeException(e);" + LS)
                .append("            }" + LS)
                .append("         };" + LS)
                .append("         return assignToJavabuf;" + LS)
                .append("      } catch (Exception e) {" + LS)
                .append("         throw new RuntimeException(e);" + LS)
                .append("      }" + LS)
                .append("   }" + LS + LS);
        sb.append("   private static AssignFromJavabuf fromJavabuf(Class<?> javaClass, FieldDescriptor fd) {" + LS)
                .append("      try {" + LS)
                .append("         AssignFromJavabuf assignFromJavabuf = (message, object) -> {" + LS)
                .append("            try {" + LS)
                .append("               final Field field = Utility.getField(javaClass, javabufClassToJavaClass(fd.getName()));"
                        + LS)
                .append("               field.setAccessible(true);" + LS)
                .append("               if (field.getType().isArray() && !field.getType().getName().startsWith(\"[[\") && field.getType().getComponentType().isPrimitive()) {"
                        + LS)
                .append("                  Object m = message.getField(fd);" + LS)
                .append("                  Object array = toPrimitiveJavaArray(m, field.getType().getComponentType());" + LS)
                .append("                  Utility.setField(field, object, array, INSTANCE);" + LS)
                .append("               } else if (message.getField(fd) instanceof AbstractList) {" + LS)
                .append("                  List<?> list = (List<?>) message.getField(fd);" + LS)
                .append("                  if (list.size() > 0) {" + LS)
                .append("                     Class<?> clazz = list.get(0).getClass();" + LS)
                .append("                     Object array = toPrimitiveJavaArray(list, clazz);" + LS)
                .append("                     Utility.setField(field, object, array, INSTANCE);" + LS)
                .append("                  }" + LS)
                .append("               } else if (Descriptors.FieldDescriptor.Type.MESSAGE.equals(fd.getType())" + LS)
                .append("                     && fromJavabufMap.keySet().contains(fd.getMessageType().getName())) {" + LS)
                .append("                  if (message.hasField(fd)) {" + LS)
                .append("                     Message submessage = (Message) message.getField(fd);" + LS)
                .append("                     Object obj = fromJavabufMap.get(fd.getMessageType().getName()).assignFromJavabuf(submessage);"
                        + LS)
                .append("                     Utility.setField(field, object, obj, INSTANCE);" + LS)
                .append("                  }" + LS)
                .append("               } else {" + LS)
                .append("                  Object ooo = message.getField(fd);" + LS)
                .append("                  if (Integer.class.equals(ooo.getClass()) && (Byte.class.equals(field.getType()) || byte.class.equals(field.getType()))) {"
                        + LS)
                .append("                     Utility.setField(field, object, ((Integer) ooo).byteValue(), INSTANCE);" + LS)
                .append("                  } else if (Integer.class.equals(ooo.getClass()) && (Short.class.equals(field.getType()) || short.class.equals(field.getType()))) {"
                        + LS)
                .append("                     Utility.setField(field, object, ((Integer) ooo).shortValue(), INSTANCE);" + LS)
                .append("                  } else if (Integer.class.equals(ooo.getClass()) && (Character.class.equals(field.getType()) || char.class.equals(field.getType()))) {"
                        + LS)
                .append("                     int i = ((Integer)ooo).intValue();" + LS)
                .append("                     Utility.setField(field, object, Character.toChars(i)[0], INSTANCE);" + LS)
                .append("                  } else if (ooo instanceof ByteString) {" + LS)
                .append("                     Utility.setField(field, object, ((ByteString) ooo).newInput().readAllBytes(), INSTANCE);"
                        + LS)
                .append("                  } else {" + LS)
                .append("                     Utility.setField(field, object, ooo, INSTANCE);" + LS)
                .append("                  }" + LS)
                .append("               }" + LS)
                .append("            } catch (Exception e) {" + LS)
                .append("               throw new RuntimeException(e);" + LS)
                .append("            }" + LS)
                .append("         };" + LS)
                .append("         return assignFromJavabuf;" + LS)
                .append("      } catch (Exception e) {" + LS)
                .append("         throw new RuntimeException(e);" + LS)
                .append("      }" + LS)
                .append("   }" + LS + LS);
        sb.append("   private static String javabufClassToJavaClass(String fieldName) {" + LS)
                .append("      int pos = fieldName.lastIndexOf(\"___\");" + LS)
                .append("      if (pos >= 0) {" + LS)
                .append("         return fieldName.substring(0, pos);" + LS)
                .append("      }" + LS)
                .append("      return fieldName;" + LS)
                .append("   }" + LS + LS);
        sb.append("   private static String getTypeName(FieldDescriptor fd) {" + LS)
                .append("      String s = fd.toProto().getTypeName();" + LS)
                .append("      if (s.length() > 0 && s.charAt(0) == '.') {" + LS)
                .append("         s = s.substring(1);" + LS)
                .append("      }" + LS)
                .append("      if (s.contains(\".\")) {" + LS)
                .append("         s = s.substring(s.lastIndexOf('.') + 1);" + LS)
                .append("      }" + LS)
                .append("      return javabufToJava(s);" + LS)
                .append("   }" + LS + LS);
        sb.append("   private static String javabufToJava(String name) {" + LS)
                .append("      if (name.contains(\"___\")) {" + LS)
                .append("         name = name.replace(\"___\", \".\");" + LS)
                .append("      } else if (name.contains(\"_INNER_\")) {" + LS)
                .append("         name = name.replace(\"_INNER_\", \".\");" + LS)
                .append("      } else if (name.contains(\"_HIDDEN_\")) {" + LS)
                .append("         name = name.replace(\"_HIDDEN_\", \".\");" + LS)
                .append("      }" + LS)
                .append("      return name.replaceAll(\"_\", \".\");" + LS)
                .append("   }" + LS + LS);
        sb.append("   private static String charsToString(Object o) {" + LS)
                .append("      StringBuilder sb = new StringBuilder();" + LS)
                .append("      if (char.class.equals(o.getClass().getComponentType())) {" + LS)
                .append("         char[] array = (char[]) o;" + LS)
                .append("         for (int i = 0; i < array.length; i++) {" + LS)
                .append("            sb.append(array[i]);" + LS)
                .append("         }" + LS)
                .append("      } else {" + LS)
                .append("           Character[] array = (Character[]) o;" + LS)
                .append("           for (int i = 0; i < array.length; i++) {" + LS)
                .append("              sb.append(array[i]);" + LS)
                .append("            }" + LS)
                .append("      }" + LS)
                .append("      return sb.toString();" + LS)
                .append("   }" + LS + LS);
        sb.append("   private static ReturnJavaClass getReturnNonPublicJavaClass(final String classname) {" + LS)
                .append("      return new ReturnJavaClass() {" + LS)
                .append("         public Class<?> getJavaClass() {" + LS)
                .append("            try {" + LS)
                .append("               return Class.forName(classname);" + LS)
                .append("            } catch (Exception e) {" + LS)
                .append("               throw new RuntimeException(e);" + LS)
                .append("            }" + LS)
                .append("         }" + LS)
                .append("      };" + LS)
                .append("   }" + LS + LS);
        sb.append("   private static Constructor<?> getConstructor(String classname) throws Exception {" + LS)
                .append("      if (constructors.containsKey(classname)) {" + LS)
                .append("         return constructors.get(classname);" + LS)
                .append("      }" + LS)
                .append("      Constructor<?>[] conss = Class.forName(classname).getDeclaredConstructors();" + LS)
                .append("      if (conss.length == 0) {" + LS)
                .append("         return null;" + LS)
                .append("      }" + LS)
                .append("      Constructor<?> cons = conss[0];" + LS)
                .append("      for (int i = 1; i < conss.length; i++) {" + LS)
                .append("         if (conss[i].getParameterCount() < cons.getParameterCount()) {" + LS)
                .append("            cons = conss[i];" + LS)
                .append("         }" + LS)
                .append("      }" + LS)
                .append("      cons.setAccessible(true);" + LS)
                .append("      constructors.put(classname, cons);" + LS)
                .append("      return cons;" + LS)
                .append("   }" + LS + LS);
        sb.append("   private static String simplifyTypeName(String name) {" + LS)
                .append("      return name.replace(\"class \", \"\").replace(\"interface \", \"\");" + LS)
                .append("   }" + LS + LS);
        sb.append("   static String squashName(String name) {" + LS)
                .append("      StringBuilder sb = new StringBuilder();" + LS)
                .append("      int pos = name.indexOf('_');" + LS)
                .append("      while (pos > 0) {" + LS)
                .append("         sb.append(name.substring(0, pos));" + LS)
                .append("         name = name.substring(pos + 1);" + LS)
                .append("         pos = name.indexOf('_');" + LS)
                .append("      }" + LS)
                .append("      sb.append(name);" + LS)
                .append("      return sb.toString().toLowerCase();" + LS)
                .append("   }" + LS + LS);
        sb.append(String.format(RAW_AGGREGATE_TO_JAVABUF));
        sb.append(String.format(RAW_AGGREGATE_FROM_JAVABUF));
        sb.append(String.format(TO_PRIMITIVE_JAVABUF_ARRAY));
        sb.append(String.format(TO_PRIMITIVE_JAVA_ARRAY));
        sb.append(String.format(UNWRAP_TABLE));
        sb.append(String.format(IS_PRIMITIVE));
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

    static String squashName(String name) {
        StringBuilder sb = new StringBuilder();
        int pos = name.indexOf('_');
        while (pos > 0) {
            sb.append(name.substring(0, pos));
            name = name.substring(pos + 1);
            pos = name.indexOf('_');
        }
        return sb.toString().toLowerCase();
    }

    private static void createTranslatorToJavabuf(String[] args, Class<?> clazz, StringBuilder sb) throws Exception {
        if ("gEmpty".equals(clazz.getSimpleName())
                || "gCookie".equals(clazz.getSimpleName())
                || "gHeader".equals(clazz.getSimpleName())
                || "ServletInfo".equals(clazz.getSimpleName())
                || "gNewCookie".equals(clazz.getSimpleName())
                || "FormMap".equals(clazz.getSimpleName())
                || "FormValues".equals(clazz.getSimpleName())
                || "dev.resteasy.grpc.arrays.Array_proto$dev_resteasy_grpc_arrays___NONE".equals(clazz.getName())
                || "dev.resteasy.grpc.arrays.Array_proto$dev_resteasy_grpc_arrays___Any".equals(clazz.getName())
                || "dev.resteasy.grpc.arrays.Array_proto$dev_resteasy_grpc_arrays___String".equals(clazz.getName())) {
            return;
        }
        if (NULLABLE_WRAPPERS.contains(clazz.getSimpleName())) {
            return;
        }
        if ((args[1] + "_proto$dev_resteasy_grpc_arrays___ArrayHolder").equals(clazz.getName())) {
            return;
        }
        if ((args[1] + "_proto$dev_resteasy_grpc_arrays___ArrayHolder___WArray").equals(clazz.getName())) {
            sb.append("   /******************************************************************************************" + LS)
                    .append("      TranslateToJavabuf and TranslateFromJavabuf for: X2" + LS)
                    .append("         " + clazz.getName() + LS)
                    .append("   ******************************************************************************************/"
                            + LS)
                    .append(String.format(ARRAYHOLDER_WARRAY_TO_JAVABUF));
            return;
        }
        if (clazz.getName().endsWith("_wrapper")) {
            return;
        }
        sb.append("   /******************************************************************************************" + LS)
                .append("      TranslateToJavabuf and TranslateFromJavabuf for: X3" + LS)
                .append("         " + clazz.getName() + LS)
                .append("   ******************************************************************************************/" + LS);
        sb.append("   public static class ")
                .append(fqnify(clazz.getSimpleName())).append("_ToJavabuf implements TranslateToJavabuf {" + LS);
        if (PRIMITIVE_WRAPPER_TYPES.containsKey(clazz.getSimpleName())) {
            String simpleJavabufName = clazz.getSimpleName();
            String simpleJavaName = simpleJavabufName.substring(1);
            sb.append("" + LS)
                    .append("      public Message assignToJavabuf(Object x) {" + LS)
                    .append("         if (x == null) {" + LS)
                    .append("            return null;" + LS)
                    .append("         }" + LS)
                    .append("         ")
                    .append(simpleJavaName)
                    .append(" p = (")
                    .append(simpleJavaName)
                    .append(") x;" + LS)
                    .append("         ")
                    .append(clazz.getCanonicalName())
                    .append(".Builder builder = ")
                    .append(clazz.getCanonicalName())
                    .append(".newBuilder();" + LS)
                    .append("         return builder.setValue(p")
                    .append(GET_METHODS.get(simpleJavaName))
                    .append(").build();" + LS)
                    .append("      }" + LS + LS)
                    .append("      public void clear() {" + LS)
                    .append("         //" + LS)
                    .append("      }" + LS);
        } else if (clazz.getName().endsWith("___Array") || clazz.getName().endsWith("___WArray")) {
            String javabufComponentClass = null;
            boolean isWrapper;
            if (clazz.getSimpleName().contains("___WArray")) {
                javabufComponentClass = clazz.getSimpleName().substring(0, clazz.getSimpleName().lastIndexOf("___WArray"));
                isWrapper = true;
            } else {
                javabufComponentClass = clazz.getSimpleName().substring(0, clazz.getSimpleName().lastIndexOf("___Array"));
                isWrapper = false;
            }
            String javaComponentClass = javabufComponentClass;
            if (javabufComponentClass.startsWith("dev_resteasy_grpc_arrays___")
                    && !"dev_resteasy_grpc_arrays___ArrayHolder".equals(javabufComponentClass)) {
                javaComponentClass = javabufComponentClass.substring(javabufComponentClass.lastIndexOf("___") + 3);
            }
            String methodClass = squashToCamel(javabufComponentClass);
            if ("Byte".equals(javaComponentClass) && isWrapper) {
                sb.append(String.format(ArrayWrapperBuilderClassForByteWrappers, javaComponentClass, javabufComponentClass));
            } else if ("Character".equals(javaComponentClass) && isWrapper) {
                sb.append(
                        String.format(ArrayWrapperBuilderClassForCharacterWrappers, javaComponentClass, javabufComponentClass));
            } else if (JAVA_WRAPPER_TYPES.contains(javaComponentClass)) {
                if (isWrapper) {
                    sb.append(String.format(ArrayPrimitiveWrapperBuilderClass, javaComponentClass, javabufComponentClass,
                            javaComponentClass));
                } else {
                    String primitiveClass = WRAPPER_TO_PRIMITIVE.get(javaComponentClass);
                    methodClass = primitiveClass.substring(0, 1).toUpperCase() + primitiveClass.substring(1);
                    if ("byte".equals(primitiveClass)) {
                        sb.append(String.format(ArrayWrapperBuilderClassForByteArrays, primitiveClass, javaComponentClass,
                                methodClass));
                    } else if ("char".equals(primitiveClass)) {
                        sb.append(String.format(ArrayWrapperBuilderClassForCharArrays, primitiveClass, javaComponentClass,
                                methodClass));
                    } else
                        sb.append(String.format(ArrayPrimitiveBuilderClass, primitiveClass, javaComponentClass, methodClass));
                }
            } else if ("dev_resteasy_grpc_arrays___Any".equals(javabufComponentClass)) {
                sb.append(String.format(ARRAY_WRAPPER_BUILDER_FOR_ANY_WRAPPERS_TO_JAVABUF));
            } else {
                javaComponentClass = javabufToJava(javabufComponentClass, javaComponentClass, true);
                if (javaComponentClass.contains("___")) {
                    javaComponentClass = javabufComponentClass.substring(javabufComponentClass.lastIndexOf("___") + 3);
                }
                sb.append(String.format(ArrayWrapperBuilderClass, javaComponentClass, javabufComponentClass, methodClass));
            }
        } else if (LISTS.containsKey(clazz.getSimpleName())) {
            String assignment = null;
            if (JAVA_WRAPPER_TYPES.contains(COLLECTION_TYPE.get(clazz.getSimpleName()))) {
                assignment = "element";
            } else if ("java.lang.Object".equals(COLLECTION_TYPE.get(clazz.getSimpleName()))) {
                assignment = "Any.pack(translator.translateToJavabuf(element))";
            } else if (LISTS.get(clazz.getSimpleName()).contains("<")) {
                String generic = LISTS.get(clazz.getSimpleName());
                int start = generic.indexOf('<');
                generic = generic.substring(start + 1, generic.lastIndexOf('>'));
                assignment = "translator.translateToJavabuf(element, new GenericType<" + generic + ">(){})";
            } else {
                assignment = "translator.translateToJavabuf(element)";
            }
            sb.append(String.format(ListOrSetToJavabuf,
                    clazz.getSimpleName(),
                    LISTS.get(clazz.getSimpleName()),
                    assignment));
        } else if (SETS.containsKey(clazz.getSimpleName())) {
            String assignment = null;
            if (JAVA_WRAPPER_TYPES.contains(COLLECTION_TYPE.get(clazz.getSimpleName()))) {
                assignment = "element";
            } else if ("java.lang.Object".equals(COLLECTION_TYPE.get(clazz.getSimpleName()))) {
                assignment = "Any.pack(translator.translateToJavabuf(element))";
            } else if (SETS.get(clazz.getSimpleName()).contains("<")) {
                String generic = SETS.get(clazz.getSimpleName());
                int start = generic.indexOf('<');
                generic = generic.substring(start + 1, generic.lastIndexOf('>'));
                assignment = "translator.translateToJavabuf(element, new GenericType<" + generic + ">(){})";
            } else {
                assignment = "translator.translateToJavabuf(element)";
            }
            sb.append(String.format(ListOrSetToJavabuf,
                    clazz.getSimpleName(),
                    SETS.get(clazz.getSimpleName()),
                    assignment));
        } else if (MULTIMAPS.containsKey(clazz.getSimpleName())) {
            sb.append(String.format(MULTIMAP_TO_JAVABUF,
                    KEY_TYPE_JAVA.get(clazz.getSimpleName()),
                    VALUE_TYPE_JAVA.get(clazz.getSimpleName()),
                    clazz.getSimpleName(),
                    getMultimapKeyAssignmentToJavabuf(clazz.getSimpleName(), KEY_TYPE_JAVA, KEY_TYPE_JAVABUF, "Key"),
                    getMultimapValueAssignmentToJavabuf(clazz.getSimpleName(), VALUE_TYPE_JAVA, VALUE_TYPE_JAVABUF)));
        } else if (MAPS.containsKey(clazz.getSimpleName())) {
            sb.append(String.format(MAP_TO_JAVABUF,
                    KEY_TYPE_JAVA.get(clazz.getSimpleName()),
                    VALUE_TYPE_JAVA.get(clazz.getSimpleName()),
                    clazz.getSimpleName(),
                    getMapAssignmentToJavabuf(clazz.getSimpleName(), KEY_TYPE_JAVA, KEY_TYPE_JAVABUF, "Key"),
                    getMapAssignmentToJavabuf(clazz.getSimpleName(), VALUE_TYPE_JAVA, VALUE_TYPE_JAVABUF, "Value")));
        } else {
            sb.append("      private static Descriptor descriptor = ").append(clazz.getCanonicalName())
                    .append(".getDescriptor();" + LS)
                    .append("      private static List<AssignToJavabuf> assignList = new ArrayList<AssignToJavabuf>();" + LS
                            + LS)
                    .append("      static {" + LS)
                    .append("         for (FieldDescriptor f : descriptor.getFields()) {" + LS)
                    .append("            String name = f.getName();" + LS)
                    .append("            if (descriptor.findFieldByName(name) == null) {" + LS)
                    .append("               continue;" + LS)
                    .append("            }" + LS)
                    .append("            assignList.add(toJavabuf(")
                    .append(getJavabufClassValue(clazz.getSimpleName(), false, true));
            sb.append(", descriptor.findFieldByName(name)));" + LS)
                    .append("         }" + LS)
                    .append("      }" + LS + LS)
                    .append("      public Message assignToJavabuf(Object c1) {" + LS)
                    .append("         if (c1 == null) {" + LS)
                    .append("            return null;" + LS)
                    .append("         }" + LS)
                    .append("         ").append(fqnify(clazz.getSimpleName())).append(".Builder builder = ")
                    .append(fqnify(clazz.getSimpleName())).append(".newBuilder();" + LS)
                    .append("         for (AssignToJavabuf assignTo : assignList) {" + LS)
                    .append("            try {" + LS)
                    .append("               assignTo.assign(c1, builder);" + LS)
                    .append("            } catch (Exception e) {" + LS)
                    .append("               throw new RuntimeException(e);" + LS)
                    .append("            }" + LS)
                    .append("         }" + LS)
                    .append("         return builder.build();" + LS)
                    .append("      }" + LS + LS)
                    .append("      public void clear() {" + LS)
                    .append("      }" + LS);
        }
        sb.append("   }" + LS + LS);
    }

    private static void createTranslatorFromJavabuf(String[] args, Class<?> clazz, StringBuilder sb)
            throws Exception {
        if (clazz.isInterface()) {
            return;
        }
        if (clazz.getName().endsWith("___wrapper")) {
            return;
        }
        if ("dev.resteasy.grpc.arrays.Array_proto$dev_resteasy_grpc_arrays___NONE".equals(clazz.getName())) {
            return;
        }
        String originalName = disambiguateClassname(originalCanonicalClassName(clazz.getName()));
        originalName = originalName.substring(originalName.lastIndexOf('.') + 1);
        if ((args[1] + "_proto$dev_resteasy_grpc_arrays___ArrayHolder").equals(clazz.getName())) {
            createArrayHolderTranslatorFromJavabuf(args, clazz, sb);
            sb.append(String.format(dev_resteasy_grpc_arrays___ArrayHolder_ToJavabuf));
            return;
        }
        if (clazz.getSimpleName().endsWith("___WArray")) {
            String s = clazz.getSimpleName().substring(0, clazz.getSimpleName().lastIndexOf("___WArray"));
            if (NULLABLE_WRAPPERS.contains(s)) {
                s = s.substring(s.lastIndexOf("___") + 3);
                if ("Byte".equals(s)) {
                    sb.append(String.format(ArrayWrapperJavabufToByte, s));
                } else if ("Character".equals(s)) {
                    sb.append(String.format(ArrayWrapperJavabufToCharacter, s));
                } else if ("Short".equals(s)) {
                    sb.append(String.format(ArrayWrapperJavabufToShort, s));
                } else {
                    sb.append(String.format(ArrayWrapperJavabufToJavaBuiltin, s));
                }
            } else if ("dev_resteasy_grpc_arrays___Any___WArray".equals(clazz.getSimpleName())) {
                sb.append(String.format(ARRAY_WRAPPER_BUILDER_FOR_ANY_WRAPPERS_FROM_JAVABUF));
            } else if ("dev_resteasy_grpc_arrays___ArrayHolder___WArray".equals(clazz.getSimpleName())) {
                sb.append(String.format(ARRAYHOLDER_WARRAY_FROM_JAVABUF));
            } else {
                sb.append(String.format(WArrayJavabufToJava, s, squashToCamel(s), originalClassName(s)));
            }
            return;
        } else if (clazz.getSimpleName().endsWith("___Array")) {
            if ("dev_resteasy_grpc_arrays___Byte___Array".equals(clazz.getSimpleName())) {
                sb.append(String.format(ArrayHolderPrimitiveFromJavabuf_byte));
            } else if ("dev_resteasy_grpc_arrays___Character___Array".equals(clazz.getSimpleName())) {
                sb.append(String.format(ArrayHolderPrimitiveFromJavabuf_char));
            } else if ("dev_resteasy_grpc_arrays___Short___Array".equals(clazz.getSimpleName())) {
                sb.append(String.format(ArrayHolderPrimitiveFromJavabuf_short));
            } else {
                String s = clazz.getSimpleName().substring(0, clazz.getSimpleName().lastIndexOf("___Array"));
                String s1 = s.substring(s.lastIndexOf("___") + 3); // Integer
                String s2 = JAVA_WRAPPER_MAP.get(s1); // int
                String s3 = s2.substring(0, 1).toUpperCase() + s2.substring(1); // Int
                sb.append(String.format(ArrayHolderPrimitiveFromJavabuf, s1, s2, s3));
            }
            return;
        }
        if ("gEmpty".equals(originalName)) {
            return;
        }
        if ("AbstractMessage".equals(clazz.getSimpleName())
                || "gCookie".equals(clazz.getSimpleName())
                || "gNewCookie".equals(clazz.getSimpleName())
                || "gHeader".equals(clazz.getSimpleName())
                || "ServletInfo".equals(clazz.getSimpleName())
                || "FormMap".equals(clazz.getSimpleName())
                || "FormValues".equals(clazz.getSimpleName())) {
            return;
        }
        if (PRIMITIVE_WRAPPER_TYPES.containsKey(originalName)) {
            sb.append("   public static class ")
                    .append(fqnify(clazz.getSimpleName())).append("_FromJavabuf implements TranslateFromJavabuf {" + LS)
                    .append("      private static Descriptor descriptor = ").append(clazz.getCanonicalName())
                    .append(".getDescriptor();" + LS);
            String javaName = originalName.substring(1);
            if ("gByte".equals(originalName)) {
                sb.append("      public ").append(javaName).append(" assignFromJavabuf(Message message) {" + LS)
                        .append("         FieldDescriptor fd = descriptor.getFields().get(0);" + LS)
                        .append("         return ((Integer) message.getField(fd)).byteValue();" + LS)
                        .append("      }" + LS + LS)
                        .append("      public void assignExistingFromJavabuf(Message message, Object obj) { }" + LS);
            } else if ("gShort".equals(originalName)) {
                sb.append("      public ").append(javaName).append(" assignFromJavabuf(Message message) {" + LS)
                        .append("         FieldDescriptor fd = descriptor.getFields().get(0);" + LS)
                        .append("         return ((Integer) message.getField(fd)).shortValue();" + LS)
                        .append("      }" + LS + LS)
                        .append("      public void assignExistingFromJavabuf(Message message, Object obj) { }" + LS);
            } else if ("gCharacter".equals(originalName)) {
                sb.append("      public ").append(javaName).append(" assignFromJavabuf(Message message) {" + LS)
                        .append("         FieldDescriptor fd = descriptor.getFields().get(0);" + LS)
                        .append("         return ((String) message.getField(fd)).charAt(0);" + LS)
                        .append("      }" + LS + LS)
                        .append("      public void assignExistingFromJavabuf(Message message, Object obj) { }" + LS);
            } else {
                sb.append("      public ").append(javaName).append(" assignFromJavabuf(Message message) {" + LS)
                        .append("         FieldDescriptor fd = descriptor.getFields().get(0);" + LS)
                        .append("         return (").append(javaName).append(") message.getField(fd);" + LS)
                        .append("      }" + LS + LS)
                        .append("      public void assignExistingFromJavabuf(Message message, Object obj) { }" + LS + LS);
            }
            sb.append("      @Override" + LS)
                    .append("      public Object parseFromJavabuf(InputStream is) throws IOException {" + LS)
                    .append("         Message m = ").append(originalName).append(".parseFrom(is);" + LS)
                    .append("         return assignFromJavabuf(m);" + LS)
                    .append("      }" + LS);
        } else if (LISTS.containsKey(clazz.getSimpleName())) {
            String javaclassName = LISTS.get(clazz.getSimpleName());
            int i = javaclassName.indexOf("<");
            if (i >= 0) {
                javaclassName = javaclassName.substring(0, i);
            }
            Class<?> javaclass = Class.forName(javaclassName);
            String s = AGGREGATES_INV.get(COLLECTION_TYPE.get(clazz.getSimpleName()));
            String t = null;
            if ("java.lang.Object".equals(COLLECTION_TYPE.get(clazz.getSimpleName()))) {
                s = "Any";
                t = "translator.translateFromJavabuf(l.unpack((Class) Utility.extractClassFromAny(l, translator)))";
            } else if (s == null) {
                s = COLLECTION_TYPE.get(clazz.getSimpleName());
                t = "l";
            } else {
                t = String.format("(%1$s) translator.translateFromJavabuf(l)",
                        COLLECTION_TYPE.get(clazz.getSimpleName()));
            }
            if (javaclass.isInterface()) {
                sb.append(String.format(ListOrSetFromJavabuf_fromInterface,
                        clazz.getSimpleName(),
                        LISTS.get(clazz.getSimpleName()),
                        s,
                        t));
            } else {
                sb.append(String.format(ListOrSetFromJavabuf_fromClass,
                        clazz.getSimpleName(),
                        LISTS.get(clazz.getSimpleName()),
                        s,
                        t));
            }
        } else if (SETS.containsKey(clazz.getSimpleName())) {
            String javaclassName = SETS.get(clazz.getSimpleName());
            int i = javaclassName.indexOf("<");
            if (i >= 0) {
                javaclassName = javaclassName.substring(0, i);
            }
            Class<?> javaclass = Class.forName(javaclassName);
            String s = AGGREGATES_INV.get(COLLECTION_TYPE.get(clazz.getSimpleName()));
            String t = null;
            if ("java.lang.Object".equals(COLLECTION_TYPE.get(clazz.getSimpleName()))) {
                s = "Any";
                t = "translator.translateFromJavabuf(l.unpack((Class) Utility.extractClassFromAny(l, translator)))";
            } else if (s == null) {
                s = COLLECTION_TYPE.get(clazz.getSimpleName());
                t = "l";
            } else {
                t = String.format("(%1$s) translator.translateFromJavabuf(l)",
                        COLLECTION_TYPE.get(clazz.getSimpleName()));
            }
            if (javaclass.isInterface()) {
                sb.append(String.format(ListOrSetFromJavabuf_fromInterface,
                        clazz.getSimpleName(),
                        SETS.get(clazz.getSimpleName()),
                        s,
                        t));
            } else {
                sb.append(String.format(ListOrSetFromJavabuf_fromClass,
                        clazz.getSimpleName(),
                        SETS.get(clazz.getSimpleName()),
                        s,
                        t));
            }
        } else if (MULTIMAPS.containsKey(clazz.getSimpleName())) {
            sb.append(String.format(MULTIMAP_FROM_JAVABUF,
                    clazz.getSimpleName(),
                    KEY_TYPE_JAVA.get(clazz.getSimpleName()),
                    VALUE_TYPE_JAVA.get(clazz.getSimpleName()),
                    getMultimapAssignmentFromJavabuf(clazz.getSimpleName(), KEY_TYPE_JAVA, "pair.getKey()"),
                    getMultimapAssignmentFromJavabuf(clazz.getSimpleName(), VALUE_TYPE_JAVA, "pair.getValue()")));
        } else if (MAPS.containsKey(clazz.getSimpleName())) {
            sb.append(String.format(MAP_FROM_JAVABUF,
                    clazz.getSimpleName(),
                    KEY_TYPE_JAVA.get(clazz.getSimpleName()),
                    VALUE_TYPE_JAVA.get(clazz.getSimpleName()),
                    getMapAssignmentFromJavabuf(clazz.getSimpleName(), KEY_TYPE_JAVA, "Key"),
                    getMapAssignmentFromJavabuf(clazz.getSimpleName(), VALUE_TYPE_JAVA, "Value")));
        } else if (RECORDS.containsKey(clazz.getSimpleName())) {
            String javaClassString = RECORDS.get(clazz.getSimpleName());
            String javaClassStringSimple = javaClassString.contains("<")
                    ? javaClassString.substring(0, javaClassString.indexOf("<"))
                    : javaClassString;
            Class<?> javaClass = Class.forName(javaClassStringSimple);
            StringBuilder sb0 = new StringBuilder(); // generate parameter values
            Constructor<?> cons = javaClass.getConstructors()[0];
            Parameter[] params = cons.getParameters();
            int i = 0;
            boolean first = true;
            for (Field field : javaClass.getDeclaredFields()) {
                if (!first) {
                    sb0.append(", ");
                }
                first = false;
                sb0.append("(")
                        .append(field.getType().getSimpleName())
                        .append(") map.get(")
                        .append("\"" + params[i].getName() + "\")");
                i++;
            }
            sb.append(String.format(RECORD_FROM_JAVABUF,
                    clazz.getSimpleName(),
                    javaClass.getSimpleName(),
                    javaClass.getDeclaredFields().length,
                    sb0.toString()));
        } else {
            Constructor<?> cons = null;
            try {
                cons = findConstructor(clazz, originalName);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            if (cons == null) { // abstract class
                return;
            }
            sb.append("   public static class ")
                    .append(disambiguateClassname(fqnify(clazz.getSimpleName())))
                    .append("_FromJavabuf implements TranslateFromJavabuf {" + LS)
                    .append("      private static Descriptor descriptor = ").append(clazz.getCanonicalName())
                    .append(".getDescriptor();" + LS);
            sb.append("      private static List<AssignFromJavabuf> assignList = new ArrayList<AssignFromJavabuf>();" + LS + LS)
                    .append("      static {" + LS)
                    .append("         for (FieldDescriptor f : descriptor.getFields()) {" + LS)
                    .append("            String name = f.getName();" + LS)
                    .append("            if (descriptor.findFieldByName(name) == null) {" + LS)
                    .append("               continue;" + LS)
                    .append("            }" + LS)
                    .append("            assignList.add(fromJavabuf(")
                    .append(getJavabufClassValue(clazz.getSimpleName(), false, true))
                    .append(", descriptor.findFieldByName(name)));" + LS)
                    .append("         }" + LS)
                    .append("      }" + LS + LS);
            if (clazz.getName().contains("_HIDDEN_") || !Modifier.isPublic(cons.getModifiers())) {
                sb.append("      public Object assignFromJavabuf(Message message) {" + LS)
                        .append("         try {" + LS)
                        .append("            Object obj = getConstructor(\"")
                        .append(originalInnerClassName(clazz.getSimpleName()))
                        .append("\").newInstance(");
                writeArguments(cons, true, sb);
                sb.append(");" + LS)
                        .append("            for (AssignFromJavabuf assignFrom : assignList) {" + LS)
                        .append("               assignFrom.assign(message, obj);" + LS)
                        .append("            }" + LS)
                        .append("            return obj;" + LS)
                        .append("            } catch (Exception e) {" + LS)
                        .append("               throw new RuntimeException(e);" + LS)
                        .append("            }" + LS);
            } else {
                Constructor<?> con = findConstructor(clazz, originalName);
                if (con != null) {
                    sb.append("      public ").append(disambiguateClassname(originalName))
                            .append(" assignFromJavabuf(Message message) {" + LS);
                    writeConstructor(con, originalName, sb);
                    sb.append(");" + LS)
                            .append("         for (AssignFromJavabuf assignFrom : assignList) {" + LS)
                            .append("            try {" + LS)
                            .append("               assignFrom.assign(message, obj);" + LS)
                            .append("            } catch (Exception e) {" + LS)
                            .append("               throw new RuntimeException(e);" + LS)
                            .append("            }" + LS)
                            .append("         }" + LS)
                            .append("         return obj;" + LS);
                } else {
                    sb.append("      public ").append(originalName).append(" assignFromJavabuf(Message message) {" + LS);
                    sb.append("         return null;" + LS);
                }
            }
            sb.append("      }" + LS + LS);
            sb.append("      public void assignExistingFromJavabuf(Message message, Object obj) {" + LS)
                    .append("         for (AssignFromJavabuf assignFrom : assignList) {" + LS)
                    .append("            try {" + LS)
                    .append("               assignFrom.assign(message, obj);" + LS)
                    .append("            } catch (Exception e) {" + LS)
                    .append("               throw new RuntimeException(e);" + LS)
                    .append("            }" + LS)
                    .append("         }" + LS)
                    .append("      }" + LS);
            sb.append("      @Override" + LS)
                    .append("      public Object parseFromJavabuf(InputStream is) throws IOException {" + LS)
                    .append("         Message m = ").append(fqnify(clazz.getSimpleName())).append(".parseFrom(is);" + LS)
                    .append("         return assignFromJavabuf(m);" + LS)
                    .append("      }" + LS);
        }
        sb.append("   }" + LS + LS);
    }

    private static void createArrayHolderTranslatorFromJavabuf(String[] args, Class<?> clazz, StringBuilder sb) {
        sb.append("   /******************************************************************************************" + LS)
                .append("      TranslateToJavabuf and TranslateFromJavabuf for: X1" + LS)
                .append("         " + clazz.getName() + LS)
                .append("   ******************************************************************************************/"
                        + LS);
        sb.append("   public static class dev_resteasy_grpc_arrays___ArrayHolder_FromJavabuf implements TranslateFromJavabuf {"
                + LS)
                .append("      public Object assignFromJavabuf(Message message) {" + LS)
                .append("         dev_resteasy_grpc_arrays___ArrayHolder holder = (dev_resteasy_grpc_arrays___ArrayHolder) message;"
                        + LS)
                .append("         Map<FieldDescriptor, Object> map = holder.getAllFields();" + LS)
                .append("         Object array = holder.getField(map.keySet().iterator().next());" + LS)
                .append("         if (array.getClass().getName().equals(\"com.google.protobuf.Any\")) {" + LS)
                .append("            try {" + LS)
                .append("               array = Utility.unpack((Any) array, INSTANCE);" + LS)
                .append("            } catch (Exception e) {" + LS)
                .append("               throw new RuntimeException(e);" + LS)
                .append("            }" + LS)
                .append("         }" + LS)
                .append("         return INSTANCE.translateFromJavabuf((Message) array);" + LS)
                .append("      }" + LS + LS)
                .append("      @Override" + LS)
                .append("      public Object parseFromJavabuf(InputStream is) throws IOException {" + LS)
                .append("         Message m =  dev_resteasy_grpc_arrays___ArrayHolder.parseFrom(is);" + LS)
                .append("         return assignFromJavabuf(m);" + LS)
                .append("      }" + LS + LS)
                .append("      public void assignExistingFromJavabuf(Message message, Object obj) {" + LS)
                .append("      }" + LS + LS)
                .append("   }" + LS + LS);
    }

    private static void finishClass(StringBuilder sb) {
        sb.append("}" + LS);
    }

    private static void writeTranslatorClass(String[] args, String translatorClass, StringBuilder sb) throws IOException {
        String pkgPath = args[1].lastIndexOf(".") < 0 ? ""
                : args[1].substring(0, args[1].lastIndexOf(".")).replace(".", File.separator);
        Path path = Files.createDirectories(Path.of(args[0], pkgPath));
        if (path.resolve(translatorClass + ".java").toFile().exists()) {
            return;
        }
        Files.writeString(path.resolve(translatorClass + ".java"), sb.toString(), StandardCharsets.UTF_8);
    }

    private static String fqnify(String s) {
        return s.replace(".", "_");
    }

    private static String originalSimpleName(String s) {
        int i = s.lastIndexOf("___");
        if (i >= 0) {
            return s.substring(i + 3).replace('$', '.');
        }
        // inner class
        i = s.indexOf("_INNER_");
        if (i >= 0) {
            return s.substring(i + "_INNER_".length());
        }
        i = s.indexOf("_HIDDEN_");
        if (i >= 0) {
            return s.substring(i + "_HIDDEN_".length());
        }
        // primitive class
        i = s.lastIndexOf("$");
        if (i >= 0) {
            return s.substring(i + 1);
        }
        if (PRIMITIVE_WRAPPER_TYPES.containsKey(s)) {
            return s.substring(1);
        }
        return s;
    }

    private static String originalCanonicalClassName(String s) {
        if (PRIMITIVE_WRAPPER_TYPES.containsKey(s)) {
            return "java.lang." + s.substring(1);
        }
        int i = s.indexOf("$");
        if (i >= 0) {
            s = s.substring(i + 1);
        }
        int j = s.lastIndexOf("___");
        if (j < 0) {
            j = s.indexOf("_INNER_");
        }
        if (j < 0) {
            j = s.indexOf("_HIDDEN_");
        }
        if (j >= 0) {
            String pkg = s.substring(0, j).replace('_', '.');
            return pkg + "." + originalSimpleName(s);
        }
        return s;
    }

    private static String originalClassName(String s) {
        if (PRIMITIVE_WRAPPER_TYPES.containsKey(s)) {
            return "java.lang." + s.substring(1);
        }
        if (s.endsWith("___Array")) {
            s = s.substring(0, s.length() - 8);
        } else if (s.endsWith("___WArray")) {
            s = s.substring(0, s.length() - 9);
        }
        int j = s.lastIndexOf("___");
        if (j >= 0) {
            String pkg = s.substring(0, j).replace('_', '.');
            return pkg + "." + originalSimpleName(s);
        }
        if (j < 0) {
            j = s.indexOf("_INNER_");
        }
        if (j < 0) {
            j = s.indexOf("_HIDDEN_");
        }
        if (j >= 0) {
            String pkg = s.substring(0, j).replace('_', '.');
            return pkg + "$" + originalSimpleName(s);
        }
        throw new RuntimeException("originalClassName() 2: don't recognize: " + s);
    }

    private static String originalInnerClassName(String s) {
        int i = s.indexOf("$");
        if (i >= 0) {
            s = s.substring(i + 1);
        }
        int j = s.indexOf("_INNER_");
        if (j < 0) {
            j = s.indexOf("_HIDDEN_");
        }
        String pkg = s.substring(0, j).replace('_', '.');
        return pkg + "$" + originalSimpleName(s);
    }

    private static Constructor<?> findConstructor(Class<?> clazz, String originalName)
            throws ClassNotFoundException {
        String className = null;
        try {
            className = javabufToJava(clazz.getName(), originalName, false);
            Class<?> originalClazz = Class.forName(className);
            if (Modifier.isAbstract(originalClazz.getModifiers())) {
                return null;
            }
            Constructor<?>[] cons = originalClazz.getDeclaredConstructors();
            if (cons.length == 0) {
                return null;
            }
            Constructor<?> con = cons[0];
            if (cons.length > 1) {
                for (int i = 1; i < cons.length; i++) {
                    if (cons[i].getParameterCount() < con.getParameterCount()) {
                        con = cons[i];
                    }
                }
            }
            return con;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static void writeConstructor(Constructor<?> con, String originalName, StringBuilder sb) {
        sb.append("         ").append(originalName).append(" obj = new ").append(originalName).append("(");
        writeArguments(con, true, sb);
    }

    private static void writeArguments(Constructor<?> con, boolean nonStaticInner, StringBuilder sb) {
        boolean first = true;
        for (int i = 0; i < con.getParameterCount(); i++) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            if (PRIMITIVE_DEFAULTS.containsKey(con.getParameterTypes()[i])) {
                sb.append(PRIMITIVE_DEFAULTS.get(con.getParameterTypes()[i]));
            } else {
                sb.append("null");
            }
        }
    }

    private static String javabufToJava(String javabufName, String simpleName, boolean canonical) {
        String tmp = javabufName;
        String tmpPkg = "";
        int n = tmp.lastIndexOf("$");
        if (n >= 0) {
            tmp = tmp.substring(n + 1);
        }
        n = tmp.lastIndexOf("___");
        if (n >= 0) {
            tmpPkg = tmp.substring(0, n);
        } else {
            n = tmp.indexOf("_INNER_");
            if (n >= 0) {
                tmpPkg = tmp.substring(0, n);
                tmpPkg = tmpPkg.replace("_", ".");
                if (canonical) {
                    tmp = tmpPkg + "." + tmp.substring(n + "_INNER_".length());
                } else {
                    tmp = tmpPkg + "$" + tmp.substring(n + "_INNER_".length());
                }
                return disambiguateClassname(tmp);
            }
            n = tmp.indexOf("_HIDDEN_");
            if (n >= 0) {
                tmpPkg = tmp.substring(0, n);
                tmp = tmpPkg + "$" + tmp.substring(n + "_HIDDEN_".length());
                tmp = tmp.replace("_", ".");
                return disambiguateClassname(tmp);
            }
        }
        if (tmpPkg.length() > 0) {
            tmpPkg = tmpPkg.replace("_", ".");
            return disambiguateClassname(tmpPkg + "." + simpleName);
        }
        return disambiguateClassname(simpleName);
    }

    private static String disambiguateClassname(String name) {
        if (CLASSNAMES.containsKey(name)) {
            return CLASSNAMES.get(name);
        }
        return name;
    }

    private static String getJavabufClassValue(String simpleName, boolean finesseNonHiddenClasses, boolean canonical) {
        if (PRIMITIVE_WRAPPER_TYPES.containsKey(simpleName)) {
            return PRIMITIVE_WRAPPER_TYPES.get(simpleName).getName() + ".class";
        }
        try {
            if (simpleName.contains("_HIDDEN_")) {
                String classname = originalInnerClassName(simpleName);
                try {
                    Class<?> clazz = Class.forName(classname);
                    return "getReturnNonPublicJavaClass(\"" + clazz.getName() + "\").getJavaClass()";
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
            if (finesseNonHiddenClasses) {
                return "getReturnJavaClass(" + originalCanonicalClassName(simpleName) + ".class)";
            }
            return javabufToJava(simpleName, originalSimpleName(simpleName), canonical) + ".class";
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static String getMapAssignmentToJavabuf(String javabufName, Map<String, String> javaMap,
            Map<String, String> javabufMap, String field) {
        String assignment = null;
        if (JAVA_WRAPPER_TYPES.contains(javaMap.get(javabufName))) {
            assignment = "entry.get" + field + "()";
        } else if ("java.lang.Object".equals(javaMap.get(javabufName))) {
            assignment = "Any.pack(translator.translateToJavabuf(entry.get" + field + "()))";
        } else if (javaMap.get(javabufName).contains("<")) {
            String generic = javaMap.get(javabufName);
            assignment = "(" + javabufMap.get(javabufName) + ") translator.translateToJavabuf(entry.get"
                    + field
                    + "(), new GenericType<" + generic + ">(){})";
        } else {
            assignment = "(" + javabufMap.get(javabufName) + ") translator.translateToJavabuf(entry.get"
                    + field
                    + "())";
        }
        return assignment;
    }

    private static String getMapAssignmentFromJavabuf(String javabufName, Map<String, String> map, String field) {
        String assignment = null;
        if (JAVA_WRAPPER_TYPES.contains(map.get(javabufName))) {
            assignment = "pair.get" + field + "()";
        } else if ("java.lang.Object".equals(map.get(javabufName))) {
            assignment = "translator.translateFromJavabuf(Utility.unpack(pair.get" + field + "(),  translator))";
        } else {
            assignment = "translator.translateFromJavabuf(pair.get" + field + "())";
        }
        return assignment;
    }

    private static String getMultimapKeyAssignmentToJavabuf(String javabufName, Map<String, String> javaMap,
            Map<String, String> javabufMap, String field) {
        String assignment = null;
        if (JAVA_WRAPPER_TYPES.contains(javaMap.get(javabufName))) {
            assignment = "entry.get" + field + "()";
        } else if ("java.lang.Object".equals(javaMap.get(javabufName))) {
            assignment = "Any.pack(translator.translateToJavabuf(entry.get" + field + "()))";
        } else if (javaMap.get(javabufName).contains("<")) {
            String generic = javaMap.get(javabufName);
            assignment = "(" + javabufMap.get(javabufName) + ") translator.translateToJavabuf(entry.get"
                    + field
                    + "(), new GenericType<" + generic + ">(){})";
        } else {
            assignment = "(" + javabufMap.get(javabufName) + ") translator.translateToJavabuf(entry.get"
                    + field
                    + "())";
        }
        return assignment;
    }

    private static String getMultimapValueAssignmentToJavabuf(String javabufName, Map<String, String> javaMap,
            Map<String, String> javabufMap) {
        String assignment = null;
        if (JAVA_WRAPPER_TYPES.contains(javaMap.get(javabufName))) {
            assignment = "value";
        } else if ("java.lang.Object".equals(javaMap.get(javabufName))) {
            assignment = "Any.pack(translator.translateToJavabuf(value))";
        } else if (javaMap.get(javabufName).contains("<")) {
            String generic = javaMap.get(javabufName);
            assignment = "(" + javabufMap.get(javabufName) + ") translator.translateToJavabuf(value, new GenericType<" + generic
                    + ">(){})";
        } else {
            assignment = "(" + javabufMap.get(javabufName) + ") translator.translateToJavabuf(value)";
        }
        return assignment;
    }

    private static String getMultimapAssignmentFromJavabuf(String javabufName, Map<String, String> javaMap,
            String field) {
        String assignment = null;
        if (JAVA_WRAPPER_TYPES.contains(javaMap.get(javabufName))) {
            assignment = field;
        } else if ("java.lang.Object".equals(javaMap.get(javabufName))) {
            assignment = "translator.translateFromJavabuf(Utility.unpack(" + field + ",  translator))";
        } else {
            assignment = "translator.translateFromJavabuf(" + field + ")";
        }
        return assignment;
    }
}
