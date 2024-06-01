/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2023 Red Hat, Inc., and individual contributors
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

package dev.resteasy.grpc.bridge.runtime;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import com.google.protobuf.Any;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;

import dev.resteasy.grpc.bridge.runtime.protobuf.JavabufTranslator;

public final class Utility {

    private final static Map<Class<?>, Class<?>> WRAPPER_CLASSES = new HashMap<Class<?>, Class<?>>();
    static {
        WRAPPER_CLASSES.put(boolean.class, Boolean.class);
        WRAPPER_CLASSES.put(byte.class, Byte.class);
        WRAPPER_CLASSES.put(short.class, Short.class);
        WRAPPER_CLASSES.put(char.class, Character.class);
        WRAPPER_CLASSES.put(int.class, Integer.class);
        WRAPPER_CLASSES.put(long.class, Long.class);
        WRAPPER_CLASSES.put(float.class, Float.class);
        WRAPPER_CLASSES.put(double.class, Double.class);
    }

    private Utility() {
        // restrict instantiation
    }

    public static Class<?> extractClassFromAny(Any any, JavabufTranslator translator) throws Exception {
        String s = extractStringTypeFromAny(any);
        if (s == "" || s == null) {
            return null;
        }
        int pos = s.lastIndexOf('.');
        s = s.substring(pos + 1);
        String t = translator.getOuterClassname();
        String classname = t + "$" + s;
        Class<?> c = translator.translatefromJavabufClass(classname);
        if (WRAPPER_CLASSES.containsKey(c)) {
            c = WRAPPER_CLASSES.get(c);
        }
        return translator.translateToJavabufClass(c);
        //        return d;
        //        return translator.translateToJavabufClass(classname);
    }

    public static Class<?> extractTypeFromAny(Any any, ClassLoader cl, String outerClassName) throws ClassNotFoundException {
        String className = any.getTypeUrl().substring(any.getTypeUrl().indexOf('/') + 1);
        String pkg = className.substring(0, className.lastIndexOf('.') + 1);
        String innerClassName = className.substring(className.lastIndexOf('.') + 1);
        className = pkg + outerClassName + "$" + innerClassName;
        Class<?> clazz = cl.loadClass(className);
        return clazz;
    }

    public static String extractStringTypeFromAny(Any any) {
        return any.getTypeUrl().substring(any.getTypeUrl().indexOf('/') + 1);
    }

    public static String getJavaClassname(String classname) {
        String javaClassname = classname.replace("___", ".");
        return javaClassname.replace('_', '.');
    }

    public static String getJavabufClassname(String classname) {
        String javabufClassname = classname.replace('.', '_');
        int pos = javabufClassname.lastIndexOf('_');
        javabufClassname = javabufClassname.substring(0, pos) + "__" + javabufClassname.substring(pos);
        return javabufClassname;
    }

    public static String getClassnameFromProto(FieldDescriptor fd) {
        String typeName = fd.toProto().getTypeName();
        int i = typeName.lastIndexOf(".");
        String name = typeName.substring(i + 1);
        name = name.replace("___", ".");
        name = name.replace('_', '.');
        return name;
    }

    private static final VarHandle MODIFIERS;

    static {
        try {
            var lookup = MethodHandles.privateLookupIn(Field.class, MethodHandles.lookup());
            MODIFIERS = lookup.findVarHandle(Field.class, "modifiers", int.class);
        } catch (IllegalAccessException | NoSuchFieldException ex) {
            throw new RuntimeException(ex);
        }
    }

    /*
     * var lookup = MethodHandles.privateLookupIn(Field.class, MethodHandles.lookup());
     * VarHandle MODIFIERS = lookup.findVarHandle(Field.class, "modifiers", int.class);
     *
     * var emptyElementDataField = ArrayList.class.getDeclaredField("EMPTY_ELEMENTDATA");
     * // make field non-final
     * MODIFIERS.set(emptyElementDataField, emptyElementDataField.getModifiers() & ~Modifier.FINAL);
     *
     * // set field to new value
     * emptyElementDataField.setAccessible(true);
     * emptyElementDataField.set(null, new Object[] {"Hello", "World!"});
     *
     * var list = new ArrayList<>(0);
     *
     * // println uses toString(), and ArrayList.toString() indirectly relies on 'size'
     * var sizeField = ArrayList.class.getDeclaredField("size");
     * sizeField.setAccessible(true);
     * sizeField.set(list, 2); // the new "empty element data" has a length of 2
     *
     * System.out.println(list);
     */

    public static void setField(Field field, Object object, Object value, JavabufTranslator translator) throws Exception {
        if (Modifier.isFinal(field.getModifiers())) {
            if (Modifier.isStatic(field.getModifiers())) {
                return;
            }

            var lookup = MethodHandles.privateLookupIn(Field.class, MethodHandles.lookup());
            VarHandle MODIFIERS = lookup.findVarHandle(Field.class, "modifiers", int.class);

            // make field non-final
            MODIFIERS.set(field, field.getModifiers() & ~Modifier.FINAL);
            field.setAccessible(true);
            //            field.set(object, value);
            //            return;

            //            return;
            //           field.setAccessible(true);
            //           Field modifiersField = Field.class.getDeclaredField("modifiers");
            //           modifiersField.setAccessible(true);
            //           modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            //           System.out.println(Modifier.isFinal(field.getModifiers()));
            //            Method getDeclaredFields0 = Class.class.getDeclaredMethod("getDeclaredFields0", boolean.class);
            //            getDeclaredFields0.setAccessible(true);
            //            Field[] fields = (Field[]) getDeclaredFields0.invoke(Field.class, false);
            //            Field modifiers = null;
            //            for (Field each : fields) {
            //                if ("modifiers".equals(each.getName())) {
            //                    //                    System.out.println(field.get(Field.class));
            //                    //                    each.setAccessible(true);
            //                    //                    System.out.println(each.get(Field.class));
            //                    //                    System.out.println("before: " + each.getInt(Field.class));
            //                    //                    each.setInt(Field.class, each.getModifiers() & ~Modifier.FINAL);
            //                    //                    System.out.println("after: " + each.getInt(Field.class));
            //                    modifiers = each;
            //                    break;
            //                }
            //            }
            //            modifiers.setAccessible(true);
            //            System.out.println(modifiers.get(field));
            //            int m = field.getModifiers() & ~Modifier.FINAL;
            //            modifiers.setInt(field, m);
            //            //           modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            //            //           System.out.println(Modifier.isFinal(field.getModifiers()));
            //            //           assertNotNull(modifiers);
            //            //           System.out.println("modifiers after: " + modifiers);
        }
        //        System.out.println("setField(): " + value.getClass());
        //        Class<?> ufai = UnsafeFieldAccessorImpl.class;
        field.setAccessible(true);
        if (value == null) {
            field.set(object, value);
        } else if (value.getClass().isArray()) {
            if (field.getType().getComponentType().isPrimitive()) {
                field.set(object, value);
            } else {
                System.out.println(object);
                field.set(object, wrapArray(value));
                System.out.println(object);

            }
        } else if (Any.class.equals(value.getClass())) {
            Any any = (Any) value;
            System.out.println(any.getSerializedSize());
            if (any.getSerializedSize() == 0) {
                field.set(object, null);
            } else {
                Class clazz = extractClassFromAny(any, translator);
                Message message = any.unpack(clazz);
                Object javaObj = translator.translateFromJavabuf(message);
                field.set(object, javaObj);
            }
        } else {
            field.setAccessible(true);
            System.out.println("field: " + field.getName());
            field.set(object, value);
        }
    }

    public static Class<?> getHiddenClass(Class<?> clazz, String classname) {
        Class<?>[] classes = clazz.getDeclaredClasses();
        for (int i = 0; i < classes.length; i++) {
            if (classname.equals(classes[i].getName())) {
                return classes[i];
            }
        }
        return null;
    }

    public static Object[] wrapArray(Object o) {
        if (!o.getClass().isArray()) {
            throw new RuntimeException(o + " is not an array");
        }
        Class<?> clazz = o.getClass().getComponentType();
        if (!WRAPPER_CLASSES.containsKey(clazz)) {
            return (Object[]) o;
        }
        Object array = Array.newInstance(WRAPPER_CLASSES.get(clazz), Array.getLength(o));
        for (int i = 0; i < Array.getLength(array); i++) {
            Array.set(array, i, Array.get(o, i));
        }
        return (Object[]) array;
    }

    public static Field getField(Class<?> clazz, String name) {
        if (name.contains("___")) {
            String n = name.substring(name.indexOf("___") + 3);
            try {
                Integer.parseInt(n);
                name = name.substring(0, name.indexOf("___"));
            } catch (NumberFormatException nfe) {
                // ignore
            }
        }
        Class<?> c = clazz;
        while (c != null) {
            try {
                Field field = c.getDeclaredField(name);
                return field;
            } catch (Exception e) {
                c = c.getSuperclass();
            }
        }
        return null;
    }
}
