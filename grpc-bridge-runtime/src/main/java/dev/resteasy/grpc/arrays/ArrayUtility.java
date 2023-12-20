package dev.resteasy.grpc.arrays;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;

import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___AnyArray;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___ArrayHolder;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___ArrayHolderArray;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___BooleanArray;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___ByteArray;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___CharArray;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___DoubleArray;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___FloatArray;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___IntArray;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___LongArray;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___ShortArray;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___StringArray;
import dev.resteasy.grpc.bridge.runtime.protobuf.JavabufTranslator;

public class ArrayUtility {

    private static Map<String, String> PRIMITIVE_WRAPPER_TYPES = new HashMap<String, String>();
    private static Map<String, String> PRIMITIVE_TYPES = new HashMap<String, String>();

    static {
        PRIMITIVE_WRAPPER_TYPES.put("boolean", "java.lang.Boolean");
        PRIMITIVE_WRAPPER_TYPES.put("byte", "java.lang.Byte");
        PRIMITIVE_WRAPPER_TYPES.put("short", "java.lang.Short");
        PRIMITIVE_WRAPPER_TYPES.put("int", "java.lang.Integer");
        PRIMITIVE_WRAPPER_TYPES.put("long", "java.lang.Long");
        PRIMITIVE_WRAPPER_TYPES.put("float", "java.lang.Float");
        PRIMITIVE_WRAPPER_TYPES.put("double", "java.lang.Double");
        PRIMITIVE_WRAPPER_TYPES.put("char", "java.lang.Character");

        PRIMITIVE_TYPES.put("Z", "boolean");
        PRIMITIVE_TYPES.put("B", "byte");
        PRIMITIVE_TYPES.put("S", "short");
        PRIMITIVE_TYPES.put("I", "int");
        PRIMITIVE_TYPES.put("J", "long");
        PRIMITIVE_TYPES.put("F", "float");
        PRIMITIVE_TYPES.put("D", "double");
        PRIMITIVE_TYPES.put("C", "char");
    }

    public static Object getArray(Array_proto.dev_resteasy_grpc_arrays___ArrayHolder ah) throws Exception {
        return getArray(null, ah);
    }

    public static Object getArray(JavabufTranslator translator, Array_proto.dev_resteasy_grpc_arrays___ArrayHolder ah)
            throws Exception {

        switch (ah.getComponentClass()) {

            case "boolean": {
                List<Boolean> list = ah.getBooleanArrayField().getBoolFieldList();
                boolean[] bs = new boolean[list.size()];
                for (int i = 0; i < bs.length; i++) {
                    bs[i] = list.get(i);
                }
                return bs;
            }

            case "java.lang.Boolean": {
                List<Boolean> list = ah.getBooleanArrayField().getBoolFieldList();
                Boolean[] Bs = new Boolean[list.size()];
                for (int i = 0; i < Bs.length; i++) {
                    Bs[i] = list.get(i);
                }
                return Bs;
            }

            case "byte": {
                return ah.getByteArrayField().getBytesField().toByteArray();
            }

            case "java.lang.Byte": {
                byte[] bs = ah.getByteArrayField().getBytesField().toByteArray();
                Byte[] Bs = new Byte[bs.length];
                for (int i = 0; i < bs.length; i++) {
                    Bs[i] = bs[i];
                }
                return Bs;
            }

            case "short": {
                List<Integer> list = ah.getShortArrayField().getShortFieldList();
                short[] ss = new short[list.size()];
                for (int i = 0; i < ss.length; i++) {
                    ss[i] = list.get(i).shortValue();
                }
                return ss;
            }

            case "java.lang.Short": {
                List<Integer> list = ah.getShortArrayField().getShortFieldList();
                Short[] ss = new Short[list.size()];
                for (int i = 0; i < ss.length; i++) {
                    ss[i] = list.get(i).shortValue();
                }
                return ss;
            }

            case "int": {
                List<Integer> list = ah.getIntArrayField().getIntFieldList();
                int[] is = new int[list.size()];
                for (int i = 0; i < is.length; i++) {
                    is[i] = list.get(i);
                }
                return is;
            }

            case "java.lang.Integer": {
                List<Integer> list = ah.getIntArrayField().getIntFieldList();
                Integer[] is = new Integer[list.size()];
                for (int i = 0; i < is.length; i++) {
                    is[i] = list.get(i);
                }
                return is;
            }

            case "long": {
                List<Long> list = ah.getLongArrayField().getLongFieldList();
                long[] ls = new long[list.size()];
                for (int i = 0; i < ls.length; i++) {
                    ls[i] = list.get(i);
                }
                return ls;
            }

            case "java.lang.Long": {
                List<Long> list = ah.getLongArrayField().getLongFieldList();
                Long[] ls = new Long[list.size()];
                for (int i = 0; i < ls.length; i++) {
                    ls[i] = list.get(i);
                }
                return ls;
            }

            case "float": {
                List<Float> list = ah.getFloatArrayField().getFloatFieldList();
                float[] fs = new float[list.size()];
                for (int i = 0; i < fs.length; i++) {
                    fs[i] = list.get(i);
                }
                return fs;
            }

            case "java.lang.Float": {
                List<Float> list = ah.getFloatArrayField().getFloatFieldList();
                Float[] fs = new Float[list.size()];
                for (int i = 0; i < fs.length; i++) {
                    fs[i] = list.get(i);
                }
                return fs;
            }

            case "double": {
                List<Double> list = ah.getDoubleArrayField().getDoubleFieldList();
                double[] ds = new double[list.size()];
                for (int i = 0; i < ds.length; i++) {
                    ds[i] = list.get(i);
                }
                return ds;
            }

            case "java.lang.Double": {
                List<Double> list = ah.getDoubleArrayField().getDoubleFieldList();
                Double[] ds = new Double[list.size()];
                for (int i = 0; i < ds.length; i++) {
                    ds[i] = list.get(i);
                }
                return ds;
            }

            case "char": {
                List<String> list = ah.getCharArrayField().getCharFieldList();
                char[] cs = new char[list.size()];
                for (int i = 0; i < cs.length; i++) {
                    cs[i] = list.get(i).charAt(0);
                }
                return cs;
            }

            case "java.lang.Character": {
                List<String> list = ah.getCharArrayField().getCharFieldList();
                Character[] cs = new Character[list.size()];
                for (int i = 0; i < cs.length; i++) {
                    cs[i] = list.get(i).charAt(0);
                }
                return cs;
            }

            case "java.lang.String": {
                List<String> list = ah.getStringArrayField().getStringFieldList();
                String[] ss = new String[list.size()];
                for (int i = 0; i < ss.length; i++) {
                    ss[i] = list.get(i);
                }
                return ss;
            }

            case "com.google.protobuf.Any": {
                List<Any> list = ah.getAnyArrayField().getAnyFieldList();
                Any[] as = new Any[list.size()];
                for (int i = 0; i < as.length; i++) {
                    as[i] = list.get(i);
                }
                return as;
            }

            default: {
                if (isArray(ah.getComponentClass())) {
                    if (ah.getArrayHolderArrayField().getArrayHolderFieldCount() == 0) {
                        return getNestedEmptyArray(ah.getComponentClass());
                    }
                    Object o = getArray(translator, ah.getArrayHolderArrayField().getArrayHolderField(0));
                    Object[] os = (Object[]) Array.newInstance(o.getClass(),
                            ah.getArrayHolderArrayField().getArrayHolderFieldCount());
                    os[0] = o;
                    for (int i = 1; i < ah.getArrayHolderArrayField().getArrayHolderFieldCount(); i++) {
                        os[i] = getArray(translator, ah.getArrayHolderArrayField().getArrayHolderField(i));
                    }
                    return os;
                }
                dev_resteasy_grpc_arrays___AnyArray aa = ah.getAnyArrayField();
                String className = ah.getComponentClass();
                if ("".equals(className) || className == null) {
                    return null;
                }
                Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
                Object array = createArray(className, aa.getAnyFieldCount());
                List<Any> list = aa.getAnyFieldList();
                for (int i = 0; i < list.size(); i++) {
                    @SuppressWarnings("unchecked")
                    Message element = aa.getAnyField(i).unpack(translator.translateToJavabufClass(clazz));
                    Array.set(array, i, translator.translateFromJavabuf(element));
                }
                return array;
            }
        }
    }

    public static dev_resteasy_grpc_arrays___ArrayHolder getHolder(Object o) {
        return getHolder(null, o);
    }

    public static dev_resteasy_grpc_arrays___ArrayHolder getHolder(JavabufTranslator translator, Object o) {
        if (!o.getClass().isArray()) {
            throw new RuntimeException("Expecting array");
        }
        dev_resteasy_grpc_arrays___ArrayHolder.Builder ahb = dev_resteasy_grpc_arrays___ArrayHolder.newBuilder();
        String componentClassName = o.getClass().getComponentType().getName();

        switch (componentClassName) {
            case "boolean": {
                boolean[] bs = (boolean[]) o;
                dev_resteasy_grpc_arrays___BooleanArray.Builder builder = dev_resteasy_grpc_arrays___BooleanArray.newBuilder();
                for (int i = 0; i < bs.length; i++) {
                    builder.addBoolField(bs[i]);
                }
                return ahb.setBooleanArrayField(builder).setComponentClass("boolean").build();
            }

            case "java.lang.Boolean": {
                Boolean[] bs = (Boolean[]) o;
                dev_resteasy_grpc_arrays___BooleanArray.Builder builder = dev_resteasy_grpc_arrays___BooleanArray.newBuilder();
                for (int i = 0; i < bs.length; i++) {
                    builder.addBoolField(bs[i]);
                }
                return ahb.setBooleanArrayField(builder).setComponentClass("java.lang.Boolean").build();
            }

            case "byte": {
                byte[] bs = (byte[]) o;
                dev_resteasy_grpc_arrays___ByteArray.Builder builder = dev_resteasy_grpc_arrays___ByteArray.newBuilder();
                builder.setBytesField(ByteString.copyFrom(bs));
                return ahb.setByteArrayField(builder).setComponentClass("byte").build();
            }

            case "java.lang.Byte": {
                Byte[] Bs = (Byte[]) o;
                dev_resteasy_grpc_arrays___ByteArray.Builder builder = dev_resteasy_grpc_arrays___ByteArray.newBuilder();
                byte[] bs = new byte[Bs.length];
                for (int i = 0; i < Bs.length; i++) {
                    bs[i] = Bs[i];
                }
                builder.setBytesField(ByteString.copyFrom(bs));
                return ahb.setByteArrayField(builder).setComponentClass("java.lang.Byte").build();
            }

            case "short": {
                short[] ss = (short[]) o;
                dev_resteasy_grpc_arrays___ShortArray.Builder builder = dev_resteasy_grpc_arrays___ShortArray.newBuilder();
                for (int i = 0; i < ss.length; i++) {
                    builder.addShortField(ss[i]);
                }
                return ahb.setShortArrayField(builder).setComponentClass("short").build();
            }

            case "java.lang.Short": {
                Short[] Ss = (Short[]) o;
                dev_resteasy_grpc_arrays___ShortArray.Builder builder = dev_resteasy_grpc_arrays___ShortArray.newBuilder();
                for (int i = 0; i < Ss.length; i++) {
                    builder.addShortField(Ss[i]);
                }
                return ahb.setShortArrayField(builder).setComponentClass("java.lang.Short").build();
            }

            case "int": {
                int[] is = (int[]) o;
                dev_resteasy_grpc_arrays___IntArray.Builder builder = dev_resteasy_grpc_arrays___IntArray.newBuilder();
                for (int i = 0; i < is.length; i++) {
                    builder.addIntField(is[i]);
                }
                return ahb.setIntArrayField(builder).setComponentClass("int").build();
            }

            case "java.lang.Integer": {
                Integer[] is = (Integer[]) o;
                dev_resteasy_grpc_arrays___IntArray.Builder builder = dev_resteasy_grpc_arrays___IntArray.newBuilder();
                for (int i = 0; i < is.length; i++) {
                    builder.addIntField(is[i]);
                }
                return ahb.setIntArrayField(builder).setComponentClass("java.lang.Integer").build();
            }

            case "long": {
                long[] ls = (long[]) o;
                dev_resteasy_grpc_arrays___LongArray.Builder builder = dev_resteasy_grpc_arrays___LongArray.newBuilder();
                for (int i = 0; i < ls.length; i++) {
                    builder.addLongField(ls[i]);
                }
                return ahb.setLongArrayField(builder).setComponentClass("long").build();
            }

            case "java.lang.Long": {
                Long[] ls = (Long[]) o;
                dev_resteasy_grpc_arrays___LongArray.Builder builder = dev_resteasy_grpc_arrays___LongArray.newBuilder();
                for (int i = 0; i < ls.length; i++) {
                    builder.addLongField(ls[i]);
                }
                return ahb.setLongArrayField(builder).setComponentClass("java.lang.Long").build();
            }

            case "float": {
                float[] fs = (float[]) o;
                dev_resteasy_grpc_arrays___FloatArray.Builder builder = dev_resteasy_grpc_arrays___FloatArray.newBuilder();
                for (int i = 0; i < fs.length; i++) {
                    builder.addFloatField(fs[i]);
                }
                return ahb.setFloatArrayField(builder).setComponentClass("float").build();
            }

            case "java.lang.Float": {
                Float[] fs = (Float[]) o;
                dev_resteasy_grpc_arrays___FloatArray.Builder builder = dev_resteasy_grpc_arrays___FloatArray.newBuilder();
                for (int i = 0; i < fs.length; i++) {
                    builder.addFloatField(fs[i]);
                }
                return ahb.setFloatArrayField(builder).setComponentClass("java.lang.Float").build();
            }

            case "double": {
                double[] ds = (double[]) o;
                dev_resteasy_grpc_arrays___DoubleArray.Builder builder = dev_resteasy_grpc_arrays___DoubleArray.newBuilder();
                for (int i = 0; i < ds.length; i++) {
                    builder.addDoubleField(ds[i]);
                }
                return ahb.setDoubleArrayField(builder).setComponentClass("double").build();
            }

            case "java.lang.Double": {
                Double[] fs = (Double[]) o;
                dev_resteasy_grpc_arrays___DoubleArray.Builder builder = dev_resteasy_grpc_arrays___DoubleArray.newBuilder();
                for (int i = 0; i < fs.length; i++) {
                    builder.addDoubleField(fs[i]);
                }
                return ahb.setDoubleArrayField(builder).setComponentClass("java.lang.Double").build();
            }

            case "char": {
                char[] cs = (char[]) o;
                dev_resteasy_grpc_arrays___CharArray.Builder builder = dev_resteasy_grpc_arrays___CharArray.newBuilder();
                for (int i = 0; i < cs.length; i++) {
                    builder.addCharField(String.valueOf(cs[i]));
                }
                return ahb.setCharArrayField(builder).setComponentClass("char").build();
            }

            case "java.lang.Character": {
                Character[] cs = (Character[]) o;
                dev_resteasy_grpc_arrays___CharArray.Builder builder = dev_resteasy_grpc_arrays___CharArray.newBuilder();
                for (int i = 0; i < cs.length; i++) {
                    builder.addCharField(String.valueOf(cs[i]));
                }
                return ahb.setCharArrayField(builder).setComponentClass("java.lang.Character").build();
            }

            case "java.lang.String": {
                String[] ss = (String[]) o;
                dev_resteasy_grpc_arrays___StringArray.Builder builder = dev_resteasy_grpc_arrays___StringArray.newBuilder();
                for (int i = 0; i < ss.length; i++) {
                    builder.addStringField(ss[i]);
                }
                return ahb.setStringArrayField(builder).setComponentClass("java.lang.String").build();
            }

            case "com.google.protobuf.Any": {
                Any[] as = (Any[]) o;
                dev_resteasy_grpc_arrays___AnyArray.Builder builder = dev_resteasy_grpc_arrays___AnyArray.newBuilder();
                for (int i = 0; i < as.length; i++) {
                    builder.addAnyField(as[i]);
                }
                return ahb.setAnyArrayField(builder).setComponentClass("com.google.protobuf.Any").build();
            }

            default: {
                Object[] objects = (Object[]) o;
                if (objects.getClass().getComponentType().isArray()) {
                    dev_resteasy_grpc_arrays___ArrayHolderArray.Builder builder = dev_resteasy_grpc_arrays___ArrayHolderArray
                            .newBuilder();
                    for (int i = 0; i < objects.length; i++) {
                        builder.addArrayHolderField(ArrayUtility.getHolder(translator, objects[i]));
                    }
                    Array.newInstance(objects.getClass().getComponentType(), 0);
                    try {
                        Array.newInstance(Class.forName(componentClassName), 0);
                        Class.forName(componentClassName);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    return dev_resteasy_grpc_arrays___ArrayHolder.newBuilder().setArrayHolderArrayField(builder)
                            .setComponentClass(componentClassName).build();
                }

                dev_resteasy_grpc_arrays___AnyArray.Builder builder = dev_resteasy_grpc_arrays___AnyArray.newBuilder();
                for (int i = 0; i < objects.length; i++) {
                    builder.addAnyField(Any.pack(translator.translateToJavabuf(objects[i])));
                }
                return ahb.setAnyArrayField(builder).setComponentClass(componentClassName).build();
            }
        }
    }

    public static void assignArray(Object to, Object from) {
        if (Array.getLength(from) != Array.getLength(to)) {
            throw new RuntimeException("array lengths differ");
        }
        if (to.getClass().equals(from.getClass()) || componentTypeWraps(to, from)) {
            for (int i = 0; i < Array.getLength(from); i++) {
                Array.set(to, i, Array.get(from, i));
            }
        } else {
            for (int i = 0; i < Array.getLength(to); i++) {
                assignArray(Array.get(to, i), Array.get(from, i));
            }
        }
    }

    private static boolean componentTypeWraps(Object from, Object to) {
        String fromComponentType = from.getClass().getComponentType().toString();
        String toComponentType = to.getClass().getComponentType().toString();
        if (fromComponentType == null || toComponentType == null) {
            return false;
        }
        return PRIMITIVE_WRAPPER_TYPES.get(fromComponentType).equals(toComponentType) ||
                PRIMITIVE_WRAPPER_TYPES.get(toComponentType).equals(fromComponentType);
    }

    public static void assignArray(Field field, Object target, Object obj)
            throws IllegalArgumentException, IllegalAccessException {
        if (field.getType().equals(obj.getClass())) {
            field.set(target, obj);
        } else {
            for (int i = 0; i < Array.getLength(obj); i++) {
                Array.set(target, i, obj);
            }
        }
    }

    private static Object createArray(String componentType, int length) throws Exception {
        if (PRIMITIVE_WRAPPER_TYPES.containsKey(componentType)) {
            switch (componentType) {
                case "boolean":
                    return new boolean[length];

                case "byte":
                    return new byte[length];

                case "short":
                    return new short[length];

                case "int":
                    return new int[length];

                case "long":
                    return new long[length];

                case "float":
                    return new float[length];

                case "double":
                    return new double[length];

                case "char":
                    return new char[length];
            }
        }
        return Array.newInstance(Class.forName(trimClassName(componentType)), length);
    }

    private static boolean isArray(String className) {
        return className.startsWith("[");
    }

    private static Object getNestedEmptyArray(String componentClassName) throws Exception {
        if (isArray(componentClassName)) {
            Object o = getNestedEmptyArray(componentClassName.substring(1));
            return Array.newInstance(o.getClass(), 0);
        }
        if (PRIMITIVE_TYPES.containsKey(componentClassName)) {
            return createArray(PRIMITIVE_TYPES.get(componentClassName), 0);
        }
        return createArray(componentClassName, 0);
    }

    private static String trimClassName(String className) {
        if (className.startsWith("L")) {
            return className.substring(1, className.length() - 1);
        }
        return className;
    }
}
