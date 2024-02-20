package org.jboss.resteasy.test.grpc;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.google.protobuf.Message;

import dev.resteasy.grpc.bridge.runtime.protobuf.JavabufTranslator;
import dev.resteasy.grpc.example.ArrayStuff;
import dev.resteasy.grpc.example.CC1;
import dev.resteasy.grpc.example.CC1JavabufTranslator;
import dev.resteasy.grpc.example.CC1_proto;
import dev.resteasy.grpc.example.CC1_proto.java_util___HashMap;
import dev.resteasy.grpc.example.CC2;

public class ConsTest {

    @Test
    //    @Ignore
    public void testHashMap() {
        HashMap<Integer, String> map = new HashMap<Integer, String>();
        map.put(Integer.valueOf(3), "three");
        map.put(Integer.valueOf(5), "five");
        writeMap(map);
        //        HashMap<Integer, String> map3 = new HashMap<Integer, String>();
        //        map3.put(Integer.valueOf(3), "three");
        //        map3.put(Integer.valueOf(5), "five");
        //        Assert.assertEquals(map3, map);
        CC1JavabufTranslator translator = new CC1JavabufTranslator();
        java_util___HashMap jbmap = (java_util___HashMap) translator.translateToJavabuf(map);
        System.out.println(jbmap);
        HashMap<Integer, String> map2 = (HashMap<Integer, String>) translator.translateFromJavabuf(jbmap);
        writeMap(map2);
        System.out.println(map2.get(Integer.valueOf(3)));
        System.out.println(map2);
        for (Entry e : map2.entrySet()) {
            System.out.println(e);
            System.out.println(Integer.valueOf(3).equals(e.getKey()));
            System.out.println(map2.get(e.getKey()));
        }
        Assert.assertEquals(map, map2);
    }

    void writeMap(HashMap map) {
        try {
            System.out.println("\n========================");
            Field field = map.getClass().getDeclaredField("table");
            field.setAccessible(true);
            Object nodes = field.get(map);
            System.out.println(nodes.getClass());
            System.out.println(nodes.getClass().componentType());
            Field[] fields = nodes.getClass().componentType().getDeclaredFields();
            for (Field f : fields) {
                System.out.println(f);
                f.setAccessible(true);
            }
            for (int i = 0; i < Array.getLength(nodes); i++) {
                if (Array.get(nodes, i) != null) {
                    for (int j = 0; j < fields.length; j++) {
                        System.out.println(
                                "node[" + i + "]: " + fields[j].toString() + ": " + fields[j].get(Array.get(nodes, i)));
                    }
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    @Ignore
    public void testArrayStuff() throws Exception {
        ArrayStuff as = new ArrayStuff(false);
        CC1JavabufTranslator translator = new CC1JavabufTranslator();
        Message m = translator.translateToJavabuf(as);
        System.out.println(m);
        ArrayStuff as2 = (ArrayStuff) translator.translateFromJavabuf(m);
        Assert.assertEquals(as, as2);
    }

    @Test
    @Ignore
    public void arrayTest() {
        Integer[] is = new Integer[] { Integer.valueOf(3), Integer.valueOf(5) };
        CC1JavabufTranslator translator = new CC1JavabufTranslator();
        Message m = translator.translateToJavabuf(is);
        System.out.println(m);
        Integer[] is2 = (Integer[]) translator.translateFromJavabuf(m);
        Assert.assertArrayEquals(is, is2);
    }

    @Test
    @Ignore
    public void hashSetTest() throws SecurityException, ClassNotFoundException {
        //       Class<?> clazz = Class.forName("java.util.HashMap$Node");
        //       Constructor[] conss0 = clazz.getDeclaredConstructors();
        //        Constructor[] conss = Class.forName("java.util.HashMap$Node").getConstructors();
        //        Constructor[] conss2 = Class.forName("java.util.HashMap.Node").getConstructors();
        HashSet<Integer> set = new HashSet<Integer>();
        set.add(Integer.valueOf(3));
        set.add(Integer.valueOf(5));
        CC1JavabufTranslator translator = new CC1JavabufTranslator();
        Message hset = translator.translateToJavabuf(set);
        System.out.println(hset);
        Object o = translator.translateFromJavabuf(hset);
        System.out.println(o);
        Assert.assertEquals(set, o);
    }

    @Test
    @Ignore
    public void arrayListTest() {
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < 3; i++) {
            list.add(i);
        }
        //        Object[] os = new Object[2];
        //        Integer[] is = new Integer[] {Integer.valueOf(3), Integer.valueOf(5)};
        //        os = is;
        //        is[0] = 3;
        //        int[] is2 = new int[] {5, 7};
        //        os = Utility.wrapArray(is2);
        //        System.out.println(os.getClass() + ", " + is.getClass());
        CC1JavabufTranslator translator = new CC1JavabufTranslator();
        CC1_proto.java_util___ArrayList al = (CC1_proto.java_util___ArrayList) translator.translateToJavabuf(list);
        Object o = translator.translateFromJavabuf(al);
        System.out.println("o: " + o);
        Assert.assertEquals(list, o);
    }

    @Test
    @Ignore
    public void hashmapTest() throws ClassNotFoundException, InstantiationException, IllegalAccessException,
            NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        Class<?>[] cs = HashMap.class.getDeclaredClasses();
        for (int i = 0; i < cs.length; i++) {
            System.out.println(cs[i].getName());
        }
        Class<?> c = Class.forName("java.util.HashMap$Node");
        System.out.println(c);
        System.out.println(c.getName() + ", " + c.getCanonicalName());
        //        Class<?> d = Class.forName("java.util.HashMap.Node");
        //        System.out.println(d);
        Constructor<?> cons = c.getDeclaredConstructors()[0];
        cons.setAccessible(true);
        System.out.println(cons.newInstance(0, null, null, null));
        //       System.out.println(c.newInstance());
        HashMap map = new HashMap();
        map.put(null, null);
    }

    @Test
    @Ignore
    public void superTest() {
        CC1JavabufTranslator translator = new CC1JavabufTranslator();
        CC2 cc2 = new CC2("one", 3);
        Message m = translator.translateToJavabuf(cc2);
        Object o = translator.translateFromJavabuf(m);
        Assert.assertEquals(cc2, o);
    }

    @Test
    @Ignore
    public void objectsArrayTest() {
        JavabufTranslator translator = new CC1JavabufTranslator();
        //        System.out.println(gShort.class.getName());
        //        System.out.println(gShort.class.getCanonicalName());
        //        gShort gs = gShort.newBuilder().setValue(3).build();
        //        Any any = Any.pack(gs);
        //        System.out.println(any.getTypeUrl());
        //        System.out.println("extract: " + Utility.extractStringTypeFromAny(any));
        //        Class<?> cn = Utility.extractClassFromAny(any, translator);
        //        System.out.println(cn);
        //
        //        dev_resteasy_grpc_example___CC2.Builder cb = dev_resteasy_grpc_example___CC2.newBuilder();
        //        dev_resteasy_grpc_example___CC2 cc2 = cb.setJ(3).build();
        //        Any cc2Any = Any.pack(cc2);
        //        System.out.println("\n" + dev_resteasy_grpc_example___CC2.class.getName());
        //        System.out.println(dev_resteasy_grpc_example___CC2.class.getCanonicalName());
        //        System.out.println(cc2Any.getTypeUrl());
        //        System.out.println("extract: " + Utility.extractStringTypeFromAny(cc2Any));
        //        cn = Utility.extractClassFromAny(cc2Any, translator);
        //        System.out.println(cn);

        Object[] os = new Object[] { Integer.valueOf(3), Integer.valueOf(5) };
        Message m = translator.translateToJavabuf(os);
        Object[] os2 = (Object[]) translator.translateFromJavabuf(m);
        Assert.assertEquals(os, os2);
    }

    public static class FT {
        private int i;

        public FT(int i) {
            this.i = i;
        }
    }

    public static class FT2 {
        public FT ft1 = new FT(3);
        public FT ft2 = new FT(5);
    }

    //    @Test
    //    //@Ignore
    //    public void fieldTest() throws NoSuchFieldException, SecurityException {
    //        FT2.class.getField("ft1");
    //        FT2.class.getField("ft2");
    //
    //    }

    //    @Test
    //    //@Ignore
    //    public void testCC3() {
    //        CC1JavabufTranslator translator = new CC1JavabufTranslator();
    //        CC3 cc3 = new CC3("abc");
    //        CC1_proto.dev_resteasy_grpc_example___CC3 cc3_jb = (dev_resteasy_grpc_example___CC3) translator.translateToJavabuf(cc3);
    //    }

    @Test
    @Ignore
    public void privateTest() throws Exception {
        //       java.util.HashMap.Node
        try {
            Class<?>[] cs = HashMap.class.getDeclaredClasses();
            for (int i = 0; i < cs.length; i++) {
                System.out.println(cs[i].getName() + ", " + Modifier.isPrivate(cs[i].getModifiers()));
                if (!Modifier.isPublic(cs[i].getModifiers())) {
                    System.out.println(cs[i].getTypeName());
                    System.out.println(cs[i].getSimpleName());
                    if ("Node".equals(cs[i].getSimpleName())) {
                        System.out.println(cs[i].getName());
                        System.out.println(cs[i].getCanonicalName());
                        Constructor<?>[] conss = cs[i].getDeclaredConstructors();
                        System.out.println(conss.length);
                        for (int j = 0; j < conss.length; j++) {
                            conss[j].setAccessible(true);
                            System.out.println(conss[j].getParameterCount());
                            Object o = conss[j].newInstance(11, null, null, null);
                            System.out.println("new: " + o);
                            //                     System.out.println("new: " + conss[j].newInstance());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //    @Test
    ////    //@Ignore
    //    public void arrayListTest() {
    //       JavabufTranslator translator
    //    }

    @Test
    @Ignore
    public void cons() throws ClassNotFoundException {
        Constructor<?>[] cons = java.util.AbstractCollection.class.getConstructors();
        Constructor<?>[] cons2 = java.util.AbstractCollection.class.getDeclaredConstructors();

        System.out.println(cons.length);
        System.out.println(cons2.length);

        System.out.println(CC1.InnerClass.class.getName());
        Class<?> clazz = Class.forName(CC1.InnerClass.class.getName(), true,
                Thread.currentThread().getContextClassLoader());
        System.out.println(clazz.getName());
        System.out.println(clazz.getCanonicalName());
        System.out.println("ccc");

        Class<?> clazz2 = Class.forName(CC1.InnerClass.class.getName(), true,
                Thread.currentThread().getContextClassLoader());
        System.out.println(clazz2.getName());
        System.out.println(clazz2.getCanonicalName());
    }

    @Test
    @Ignore
    public void InnerTest() throws ClassNotFoundException {
        Class<?> clazz = Class.forName("dev.resteasy.grpc.example.ArrayStuff$Stuff");
        System.out.println(clazz);
        Class<?> clazz2 = Class.forName("dev.resteasy.grpc.example.ArrayStuff.Stuff");
        System.out.println(clazz2);
    }

    static class C {
    }

    static abstract class D extends C {
    }

    static class E extends D {
    }
}
