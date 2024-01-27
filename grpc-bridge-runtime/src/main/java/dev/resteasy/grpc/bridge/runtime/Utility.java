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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import com.google.protobuf.Any;
import com.google.protobuf.Descriptors.FieldDescriptor;

import dev.resteasy.grpc.bridge.runtime.protobuf.JavabufTranslator;

public final class Utility {

    private Utility() {
        // restrict instantiation
    }

    public static Class<?> extractClassFromAny(Any any, JavabufTranslator translator) throws Exception {
        String s = extractStringTypeFromAny(any);
        int pos = s.lastIndexOf('.');
        s = s.substring(pos + 1);
        String t = translator.getOuterClassname();
        String classname = t + "$" + s;
        Class<?> c = translator.translatefromJavabufClass(classname);
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

    public static void setField(Field field, Object object, Object value) throws Exception {
        if (Modifier.isFinal(field.getModifiers())) {
            return;
        }
        field.set(object, value);
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
}
