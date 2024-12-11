package org.jboss.resteasy.test.grpc;

import java.util.HashSet;
import java.util.Set;

import dev.resteasy.grpc.example.CC1_proto;

public class InnerClassTest {

    private static final String ArrayWrapperBuilderMethod = "   public void set_%1$s(Object[] array) {\n"
            + "      dev_resteasy_grpc_arrays___ArrayHolder.Builder ahb = dev_resteasy_grpc_arrays___ArrayHolder.newBuilder();%n"
            + "      %1$s_Array.Builder ab = %1$s_Array.newBuilder();%n"
            + "      %1$s_wrapper.Builder wb = %1$s_wrapper.newBuilder();%n"
            + "      for (int i = 0; i < Array.getLength(array); i++) {%n"
            + "         if (array[i] == null) {%n"
            + "            wb.clear().setNoneField(Array_proto.dev_resteasy_grpc_arrays___NONE.newBuilder().build());%n"
            + "         } else {%n"
            + "            wb.clear().set%1$sField((%1$s) INSTANCE.translateToJavabuf(array[i]));%n"
            + "         }%n"
            + "         ab.add%1$sWrapperField(wb);%n"
            + "      }%n"
            + "      ahb.set%1$sArrayField(ab);%n"
            + "   }%n%n";

    public static void main(String[] args) {
        Set<String> classnames = new HashSet<String>();
        for (Class<?> c : CC1_proto.class.getDeclaredClasses()) {
            if ("dev_resteasy_grpc_arrays___ArrayHolder".equals(c.getSimpleName())) {
                for (Class<?> c2 : c.getDeclaredClasses()) {
                    System.out.println("inner: " + c2.getName() + ", " + c2.getSimpleName());
                    if ("MessageTypeCase".equals(c2.getSimpleName())) {
                        for (Object o : c2.getEnumConstants()) {
                            if (o.toString().contains("_FIELD")) {
                                System.out.println(o.toString().substring(0, o.toString().indexOf("_FIELD")));
                                classnames.add(o.toString().substring(0, o.toString().indexOf("_FIELD")).toLowerCase());
                            }
                        }
                    }
                }
            }
        }

        Set<Class<?>> classes = new HashSet<Class<?>>();
        for (Class<?> c : CC1_proto.class.getDeclaredClasses()) {
            if (classnames.contains(c.getSimpleName().toLowerCase())) {
                classes.add(c);
            }
        }
        System.out.println(classes.size());
        for (Class<?> c : classes) {
            System.out.println(c.getName());
            //                         wrapperBuilder.append(String.format(arrayDef, typeName));
            System.out.println(String.format(ArrayWrapperBuilderMethod, c.getSimpleName()));
        }
    }
}
