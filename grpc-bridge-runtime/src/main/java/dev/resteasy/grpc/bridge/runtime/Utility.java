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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.Map;

import org.jboss.resteasy.spi.util.Types.ResteasyParameterizedType;

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

    public static Class<?> extractClassFromAny(Any any, JavabufTranslator translator) {
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
    }

    public static Class<?> extractTypeFromAny(Any any, ClassLoader cl, String outerClassName) throws ClassNotFoundException {
        String className = any.getTypeUrl().substring(any.getTypeUrl().indexOf('/') + 1);
        String pkg = className.substring(0, className.lastIndexOf('.') + 1);
        String innerClassName = className.substring(className.lastIndexOf('.') + 1);
        className = pkg + outerClassName + "$" + innerClassName;
        className = "dev.resteasy.grpc.example.CC1_proto$java_util___ArrayList";
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
        if (javabufClassname.startsWith("[")) {
            javabufClassname = javabufClassname.substring(2, javabufClassname.length() - 1) + "___WArray";
        }
        return javabufClassname;
    }

    public static String classnameToGetter(String s) {
        if (s.length() == 0) {
            return s;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("get");
        boolean upper = true;
        for (int i = 0; i < s.length(); i++) {
            if ('_' == s.charAt(i)) {
                upper = true;
                continue;
            }
            if (upper) {
                sb.append(s.substring(i, i + 1).toUpperCase());
                upper = false;
            } else {
                sb.append(s.charAt(i));
            }
        }
        sb.append("Field");
        return sb.toString();
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
        }
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
                System.out.println(field.getGenericType().getTypeName());
                return field;
            } catch (Exception e) {
                c = c.getSuperclass();
            }
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    public static Object getHiddenObject(Class<?> clazz, String hidden) {
        try {
            for (Class<?> c : clazz.getDeclaredClasses()) {
                if (hidden.equals(c.getSimpleName())) {

                    return c.newInstance();
                }
            }
            throw new RuntimeException("can't find " + clazz.getName() + "." + hidden);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Type objectify(Type type) {
        if (!(type instanceof ParameterizedType)) {
            return type;
        }
        ParameterizedType ptype = (ParameterizedType) type;
        Type[] types = ptype.getActualTypeArguments();
        Type[] newTypes = new Type[types.length];
        for (int i = 0; i < types.length; i++) {
            if (types[i] instanceof TypeVariable || types[i] instanceof WildcardType) {
                newTypes[i] = Object.class;
            } else if (types[i] instanceof ParameterizedType) {
                newTypes[i] = objectify((ParameterizedType) types[i]);
            } else {
                newTypes[i] = types[i];
            }
        }
        return new GrpcParameterizedType(newTypes, ptype.getRawType(), ptype.getOwnerType());
    }

    public static class GrpcParameterizedType extends ResteasyParameterizedType {
        public GrpcParameterizedType(Type[] actuals, Type rawType, Type ownerType) {
            super(actuals, rawType, ownerType);
        }

        @Override
        public String toString() {
            String s = super.toString();
            s = s.replace("class ", "");
            return s.replace("interface ", "");
        }
    }
}
