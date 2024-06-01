package dev.resteasy.grpc.arrays;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;

import dev.resteasy.grpc.example.CC1_proto.dev_resteasy_grpc_arrays___ArrayHolder;
import dev.resteasy.grpc.example.CC1_proto.dev_resteasy_grpc_arrays___ArrayHolder_Array;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___Boolean;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___BooleanWArray;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___Byte;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___ByteWArray;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___Character;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___CharacterWArray;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___Double;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___DoubleWArray;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___Float;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___FloatWArray;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___Integer;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___IntegerWArray;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___Long;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___LongWArray;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___NONE;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___Short;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___ShortWArray;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___String;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___StringWArray;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___any;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___anyArray;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___booleanArray;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___byteArray;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___charArray;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___doubleArray;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___floatArray;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___intArray;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___longArray;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___shortArray;
import dev.resteasy.grpc.bridge.runtime.Utility;
import dev.resteasy.grpc.bridge.runtime.protobuf.JavabufTranslator;

public class ArrayUtility {

    private static Map<String, String> PRIMITIVE_WRAPPER_TYPES = new HashMap<String, String>();
    private static Map<String, String> PRIMITIVE_TYPES = new HashMap<String, String>();

    private static dev_resteasy_grpc_arrays___NONE.Builder nBuilder = dev_resteasy_grpc_arrays___NONE.newBuilder();

    private static dev_resteasy_grpc_arrays___Boolean NULL_BOOLEAN = dev_resteasy_grpc_arrays___Boolean.newBuilder()
            .setNoneField(nBuilder).build();
    private static dev_resteasy_grpc_arrays___Byte NULL_BYTE = dev_resteasy_grpc_arrays___Byte.newBuilder()
            .setNoneField(nBuilder).build();
    private static dev_resteasy_grpc_arrays___Short NULL_SHORT = dev_resteasy_grpc_arrays___Short.newBuilder()
            .setNoneField(nBuilder).build();
    private static dev_resteasy_grpc_arrays___Integer NULL_INTEGER = dev_resteasy_grpc_arrays___Integer.newBuilder()
            .setNoneField(nBuilder).build();
    private static dev_resteasy_grpc_arrays___Long NULL_LONG = dev_resteasy_grpc_arrays___Long.newBuilder()
            .setNoneField(nBuilder).build();
    private static dev_resteasy_grpc_arrays___Float NULL_FLOAT = dev_resteasy_grpc_arrays___Float.newBuilder()
            .setNoneField(nBuilder).build();
    private static dev_resteasy_grpc_arrays___Double NULL_DOUBLE = dev_resteasy_grpc_arrays___Double.newBuilder()
            .setNoneField(nBuilder).build();
    private static dev_resteasy_grpc_arrays___Character NULL_CHARACTER = dev_resteasy_grpc_arrays___Character.newBuilder()
            .setNoneField(nBuilder).build();
    private static dev_resteasy_grpc_arrays___String NULL_STRING = dev_resteasy_grpc_arrays___String.newBuilder()
            .setNoneField(nBuilder).build();
    private static dev_resteasy_grpc_arrays___any NULL_ANY = dev_resteasy_grpc_arrays___any.newBuilder().setNoneField(nBuilder)
            .build();
    private static dev_resteasy_grpc_arrays___ArrayHolder NULL_ARRAY_HOLDER = dev_resteasy_grpc_arrays___ArrayHolder
            .newBuilder().setNoneField(nBuilder).build();

    private static dev_resteasy_grpc_arrays___Short.Builder sBuilder = dev_resteasy_grpc_arrays___Short.newBuilder();
    private static dev_resteasy_grpc_arrays___shortArray.Builder saBuilder = dev_resteasy_grpc_arrays___shortArray.newBuilder();

    private static dev_resteasy_grpc_arrays___Integer.Builder iBuilder = dev_resteasy_grpc_arrays___Integer.newBuilder();
    private static dev_resteasy_grpc_arrays___intArray.Builder iaBuilder = dev_resteasy_grpc_arrays___intArray.newBuilder();

    private static dev_resteasy_grpc_arrays___Long.Builder lBuilder = dev_resteasy_grpc_arrays___Long.newBuilder();
    private static dev_resteasy_grpc_arrays___longArray.Builder laBuilder = dev_resteasy_grpc_arrays___longArray.newBuilder();

    private static dev_resteasy_grpc_arrays___Float.Builder fBuilder = dev_resteasy_grpc_arrays___Float.newBuilder();
    private static dev_resteasy_grpc_arrays___floatArray.Builder faBuilder = dev_resteasy_grpc_arrays___floatArray.newBuilder();

    private static dev_resteasy_grpc_arrays___Double.Builder dBuilder = dev_resteasy_grpc_arrays___Double.newBuilder();
    private static dev_resteasy_grpc_arrays___doubleArray.Builder daBuilder = dev_resteasy_grpc_arrays___doubleArray
            .newBuilder();

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

    public static Object getArray(dev_resteasy_grpc_arrays___ArrayHolder ah) throws Exception {
        return getArray(null, ah);
    }

    public static Object getArray(JavabufTranslator translator, dev_resteasy_grpc_arrays___ArrayHolder ah) throws Exception {

        switch (ah.getComponentClass()) {

            case "boolean": {
                dev_resteasy_grpc_arrays___booleanArray ba = ah.getDevResteasyGrpcArraysBooleanArrayField();
                boolean[] bs = new boolean[ba.getBoolFieldCount()];
                for (int i = 0; i < bs.length; i++) {
                    bs[i] = ba.getBoolField(i);
                }
                return bs;
            }

            case "java.lang.Boolean": {
                List<dev_resteasy_grpc_arrays___Boolean> list = ah.getDevResteasyGrpcArraysBooleanWArrayField().getBooleanFieldList();
                Boolean[] Bs = new Boolean[list.size()];
                for (int i = 0; i < Bs.length; i++) {
                    Bs[i] = list.get(i).getBoolField();
                }
                return Bs;
            }

            case "byte": {
               return byteStringToByteArray(ah.getDevResteasyGrpcArraysByteArrayField().getByteField());
            }

            case "java.lang.Byte": {
                List<Array_proto.dev_resteasy_grpc_arrays___Byte> list = ah.getDevResteasyGrpcArraysByteArrayField().;
                Byte[] Bs = new Byte[list.size()];
                for (int i = 0; i < Bs.length; i++) {
                    if (list.get(i).hasByteField()) {
                        Bs[i] = list.get(i).getByteField().byteAt(0);
                    }
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
                List<Array_proto.dev_resteasy_grpc_arrays___Short> list = ah.getShortWArrayField().getShortFieldList();
                Short[] ss = new Short[list.size()];
                for (int i = 0; i < ss.length; i++) {
                    if (list.get(i).hasShortField()) {
                        ss[i] = (short) list.get(i).getShortField();
                    }
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
                List<Array_proto.dev_resteasy_grpc_arrays___Integer> list = ah.getIntegerWArrayField().getIntegerFieldList();
                Integer[] is = new Integer[list.size()];
                for (int i = 0; i < is.length; i++) {
                    if (list.get(i).hasIntField()) {
                        is[i] = list.get(i).getIntField();
                    }
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
                List<Array_proto.dev_resteasy_grpc_arrays___Long> list = ah.getLongWArrayField().getLongFieldList();
                Long[] ls = new Long[list.size()];
                for (int i = 0; i < ls.length; i++) {
                    if (list.get(i).hasLongField()) {
                        ls[i] = list.get(i).getLongField();
                    }
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
                List<Array_proto.dev_resteasy_grpc_arrays___Float> list = ah.getFloatWArrayField().getFloatFieldList();
                Float[] fs = new Float[list.size()];
                for (int i = 0; i < fs.length; i++) {
                    if (list.get(i).hasFloatField()) {
                        fs[i] = list.get(i).getFloatField();
                    }
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
                List<Array_proto.dev_resteasy_grpc_arrays___Double> list = ah.getDoubleWArrayField().getDoubleFieldList();
                Double[] ds = new Double[list.size()];
                for (int i = 0; i < ds.length; i++) {
                    if (list.get(i).hasDoubleField()) {
                        ds[i] = list.get(i).getDoubleField();
                    }
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
                List<Array_proto.dev_resteasy_grpc_arrays___Character> list = ah.getCharacterWArrayField()
                        .getCharacterFieldList();
                Character[] cs = new Character[list.size()];
                for (int i = 0; i < cs.length; i++) {
                    if (list.get(i).hasCharField()) {
                        cs[i] = list.get(i).getCharField().charAt(0);
                    }
                }
                return cs;
            }

            case "java.lang.String": {
                List<Array_proto.dev_resteasy_grpc_arrays___String> list = ah.getStringWArrayField().getStringFieldList();
                ;
                String[] ss = new String[list.size()];
                for (int i = 0; i < ss.length; i++) {
                    if (list.get(i).hasStringField()) {
                        ss[i] = list.get(i).getStringField();
                    }
                }
                return ss;
            }

            case "com.google.protobuf.Any": {
                List<dev_resteasy_grpc_arrays___any> list = ah.getAnyArrayField().getAnyFieldList();
                Any[] as = new Any[list.size()];
                for (int i = 0; i < as.length; i++) {
                    if (list.get(i).hasAnyField()) {
                        as[i] = (Any) list.get(i).getAnyField();
                    }
                }
                return as;
            }

            //            case "java.lang.Object": {
            //                dev_resteasy_grpc_arrays___anyArray aa = ah.getAnyArrayField();
            //                Object[] os = new Object[aa.getAnyFieldCount()];
            //                if (os.length == 0) {
            //                    return os;
            //                }
            //                @SuppressWarnings("rawtypes")
            //                Class clazz = Utility.extractClassFromAny(aa.getAnyField(0), translator);
            //                for (int i = 0; i < os.length; i++) {
            //                    @SuppressWarnings("unchecked")
            //                    Message m = aa.getAnyField(i).unpack(clazz);
            //                    os[i] = translator.translateFromJavabuf(m);
            //                }
            //                return os;
            //            }
            //
            //            default: {
            //                if (isArray(ah.getComponentClass())) {
            //                    if (ah.getArrayHolderArrayField().getArrayHolderFieldCount() == 0) {
            //                        return getNestedEmptyArray(ah.getComponentClass());
            //                    }
            //                    Object o = getArray(translator, ah.getArrayHolderArrayField().getArrayHolderField(0));
            //                    Object[] os = (Object[]) Array.newInstance(o.getClass(),
            //                            ah.getArrayHolderArrayField().getArrayHolderFieldCount());
            //                    os[0] = o;
            //                    for (int i = 1; i < ah.getArrayHolderArrayField().getArrayHolderFieldCount(); i++) {
            //                        os[i] = getArray(translator, ah.getArrayHolderArrayField().getArrayHolderField(i));
            //                    }
            //                    return os;
            //                }
            //                dev_resteasy_grpc_arrays___anyArray aa = ah.getAnyArrayField();
            //                String className = ah.getComponentClass();
            //                if ("".equals(className) || className == null) {
            //                    return null;
            //                }
            //                if ("java.lang.Object".equals(className)) {
            //                    Object[] os = new Object[aa.getAnyFieldCount()];
            //                    if (os.length == 0) {
            //                        return os;
            //                    }
            //                    @SuppressWarnings("rawtypes")
            //                    Class clazz = Utility.extractClassFromAny(aa.getAnyField(0), translator);
            //                    for (int i = 0; i < os.length; i++) {
            //                        @SuppressWarnings("unchecked")
            //                        Message m = aa.getAnyField(i).unpack(clazz);
            //                        os[i] = translator.translateFromJavabuf(m);
            //                    }
            //                    return os;
            //                }
            //                Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
            //                Object array = createArray(className, aa.getAnyFieldCount());
            //                List<Any> list = aa.getAnyFieldList();
            //                for (int i = 0; i < list.size(); i++) {
            //                    @SuppressWarnings("unchecked")
            //                    Message element = aa.getAnyField(i).unpack(translator.translateToJavabufClass(clazz));
            //                    Array.set(array, i, translator.translateFromJavabuf(element));
            //                }
            //                return array;
            //            }
        }
        return null;
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
                baBuilder.clear();
                boolean[] bs = (boolean[]) o;
                for (int i = 0; i < bs.length; i++) {
                    baBuilder.addBoolField(bs[i]).build();
                }
                return ahb.setDevResteasyGrpcArraysBooleanArrayField(baBuilder).setComponentClass("boolean").build();
            }

            case "java.lang.Boolean": {
               bawBuilder.clear();
               Boolean[] bs = (Boolean[]) o;
               for (int i = 0; i < bs.length; i++) {
                  if (bs[i] == null) {
                     bBuilder.setNoneField(NULL);
                  } else {
                     bawBuilder.addBooleanField(bBuilder.setBoolField(bs[i]));
                  }
//                (bBuilder.setBoolField(bs[i]).build());
               }
               return ahb.setDevResteasyGrpcArraysBooleanWArrayField(bawBuilder).setComponentClass("java.lang.Boolean").build();
            }

            case "byte": {
               byte[] bs = (byte[]) o;
               dev_resteasy_grpc_arrays___byteArray.Builder builder = dev_resteasy_grpc_arrays___byteArray.newBuilder();
               builder.setByteField(ByteString.copyFrom(bs));
               return ahb.setDevResteasyGrpcArraysByteArrayField(builder).setComponentClass("byte").build();
            }

            //            case "java.lang.Byte": {
            //                Byte[] Bs = (Byte[]) o;
            //                dev_resteasy_grpc_arrays___ByteArray.Builder builder = dev_resteasy_grpc_arrays___ByteArray.newBuilder();
            //                byte[] bs = new byte[Bs.length];
            //                for (int i = 0; i < Bs.length; i++) {
            //                    bs[i] = Bs[i];
            //                }
            //                builder.setBytesField(ByteString.copyFrom(bs));
            //                return ahb.setByteArrayField(builder).setComponentClass("java.lang.Byte").build();
            //            }
            //
            //            case "short": {
            //                short[] ss = (short[]) o;
            //                dev_resteasy_grpc_arrays___shortArray.Builder builder = dev_resteasy_grpc_arrays___shortArray.newBuilder();
            //                for (int i = 0; i < ss.length; i++) {
            //                    builder.addShortField(ss[i]);
            //                }
            //                return ahb.setShortArrayField(builder).setComponentClass("short").build();
            //            }
            //
            //            case "java.lang.Short": {
            //                Short[] Ss = (Short[]) o;
            //                dev_resteasy_grpc_arrays___shortArray.Builder builder = dev_resteasy_grpc_arrays___shortArray.newBuilder();
            //                for (int i = 0; i < Ss.length; i++) {
            //                    builder.addShortField(Ss[i]);
            //                }
            //                return ahb.setShortArrayField(builder).setComponentClass("java.lang.Short").build();
            //            }
            //
            //            case "int": {
            //                int[] is = (int[]) o;
            //                dev_resteasy_grpc_arrays___intArray.Builder builder = dev_resteasy_grpc_arrays___intArray.newBuilder();
            //                for (int i = 0; i < is.length; i++) {
            //                    builder.addIntField(is[i]);
            //                }
            //                return ahb.setIntArrayField(builder).setComponentClass("int").build();
            //            }
            //
            //            case "java.lang.Integer": {
            //                Integer[] is = (Integer[]) o;
            //                dev_resteasy_grpc_arrays___intArray.Builder builder = dev_resteasy_grpc_arrays___intArray.newBuilder();
            //                for (int i = 0; i < is.length; i++) {
            //                    builder.addIntField(is[i]);
            //                }
            //                return ahb.setIntArrayField(builder).setComponentClass("java.lang.Integer").build();
            //            }
            //
            //            case "long": {
            //                long[] ls = (long[]) o;
            //                dev_resteasy_grpc_arrays___longArray.Builder builder = dev_resteasy_grpc_arrays___longArray.newBuilder();
            //                for (int i = 0; i < ls.length; i++) {
            //                    builder.addLongField(ls[i]);
            //                }
            //                return ahb.setLongArrayField(builder).setComponentClass("long").build();
            //            }
            //
            //            case "java.lang.Long": {
            //                Long[] ls = (Long[]) o;
            //                dev_resteasy_grpc_arrays___longArray.Builder builder = dev_resteasy_grpc_arrays___longArray.newBuilder();
            //                for (int i = 0; i < ls.length; i++) {
            //                    builder.addLongField(ls[i]);
            //                }
            //                return ahb.setLongArrayField(builder).setComponentClass("java.lang.Long").build();
            //            }
            //
            //            case "float": {
            //                float[] fs = (float[]) o;
            //                dev_resteasy_grpc_arrays___floatArray.Builder builder = dev_resteasy_grpc_arrays___floatArray.newBuilder();
            //                for (int i = 0; i < fs.length; i++) {
            //                    builder.addFloatField(fs[i]);
            //                }
            //                return ahb.setFloatArrayField(builder).setComponentClass("float").build();
            //            }
            //
            //            case "java.lang.Float": {
            //                Float[] fs = (Float[]) o;
            //                dev_resteasy_grpc_arrays___floatArray.Builder builder = dev_resteasy_grpc_arrays___floatArray.newBuilder();
            //                for (int i = 0; i < fs.length; i++) {
            //                    builder.addFloatField(fs[i]);
            //                }
            //                return ahb.setFloatArrayField(builder).setComponentClass("java.lang.Float").build();
            //            }
            //
            //            case "double": {
            //                double[] ds = (double[]) o;
            //                dev_resteasy_grpc_arrays___doubleArray.Builder builder = dev_resteasy_grpc_arrays___doubleArray.newBuilder();
            //                for (int i = 0; i < ds.length; i++) {
            //                    builder.addDoubleField(ds[i]);
            //                }
            //                return ahb.setDoubleArrayField(builder).setComponentClass("double").build();
            //            }
            //
            //            case "java.lang.Double": {
            //                Double[] fs = (Double[]) o;
            //                dev_resteasy_grpc_arrays___doubleArray.Builder builder = dev_resteasy_grpc_arrays___DoubleArray.newBuilder();
            //                for (int i = 0; i < fs.length; i++) {
            //                    builder.addDoubleField(fs[i]);
            //                }
            //                return ahb.setDoubleArrayField(builder).setComponentClass("java.lang.Double").build();
            //            }
            //
            //            case "char": {
            //                char[] cs = (char[]) o;
            //                dev_resteasy_grpc_arrays___CharArray.Builder builder = dev_resteasy_grpc_arrays___CharArray.newBuilder();
            //                for (int i = 0; i < cs.length; i++) {
            //                    builder.addCharField(String.valueOf(cs[i]));
            //                }
            //                return ahb.setCharArrayField(builder).setComponentClass("char").build();
            //            }
            //
            //            case "java.lang.Character": {
            //                Character[] cs = (Character[]) o;
            //                dev_resteasy_grpc_arrays___CharArray.Builder builder = dev_resteasy_grpc_arrays___CharArray.newBuilder();
            //                for (int i = 0; i < cs.length; i++) {
            //                    builder.addCharField(String.valueOf(cs[i]));
            //                }
            //                return ahb.setCharArrayField(builder).setComponentClass("java.lang.Character").build();
            //            }
            //
            //            case "java.lang.String": {
            //                String[] ss = (String[]) o;
            //                dev_resteasy_grpc_arrays___StringArray.Builder builder = dev_resteasy_grpc_arrays___StringArray.newBuilder();
            //                for (int i = 0; i < ss.length; i++) {
            //                    builder.addStringField(ss[i]);
            //                }
            //                return ahb.setStringArrayField(builder).setComponentClass("java.lang.String").build();
            //            }
            //
            //            case "com.google.protobuf.Any": {
            //                Any[] as = (Any[]) o;
            //                dev_resteasy_grpc_arrays___anyArray.Builder builder = dev_resteasy_grpc_arrays___anyArray.newBuilder();
            //                for (int i = 0; i < as.length; i++) {
            //                    builder.addAnyField(as[i]);
            //                }
            //                return ahb.setAnyArrayField(builder).setComponentClass("com.google.protobuf.Any").build();
            //            }
            //
            //            default: {
            //                Object[] objects = (Object[]) o;
            //                if (objects.getClass().getComponentType().isArray()) {
            //                    dev_resteasy_grpc_arrays___ArrayHolderArray.Builder builder = dev_resteasy_grpc_arrays___ArrayHolderArray
            //                            .newBuilder();
            //                    for (int i = 0; i < objects.length; i++) {
            //                        builder.addArrayHolderField(ArrayUtility.getHolder(translator, objects[i]));
            //                    }
            //                    Array.newInstance(objects.getClass().getComponentType(), 0);
            //                    try {
            //                        Array.newInstance(Class.forName(componentClassName), 0);
            //                        Class.forName(componentClassName);
            //                    } catch (Exception e) {
            //                        throw new RuntimeException(e);
            //                    }
            //                    return dev_resteasy_grpc_arrays___ArrayHolder.newBuilder().setArrayHolderArrayField(builder)
            //                            .setComponentClass(componentClassName).build();
            //                }
            //
            //                dev_resteasy_grpc_arrays___anyArray.Builder builder = dev_resteasy_grpc_arrays___anyArray.newBuilder();
            //                for (int i = 0; i < objects.length; i++) {
            //                    builder.addAnyField(Any.pack(translator.translateToJavabuf(objects[i])));
            //                }
            //                return ahb.setAnyArrayField(builder).setComponentClass(componentClassName).build();
            //            }
        }return null;

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

    public static String getComponentClass(dev_resteasy_grpc_arrays___ArrayHolder holder) {
        if (holder.hasDevResteasyGrpcArraysArrayHolderArrayField()) {
           dev_resteasy_grpc_arrays___ArrayHolder_Array holderArray = holder.getDevResteasyGrpcArraysArrayHolderArrayField();
            if (holderArray.getDevResteasyGrpcArraysArrayHolderWrapperFieldCount() == 0) {
                return "";
            }
            return getComponentClass(holderArray.getDevResteasyGrpcArraysArrayHolderWrapperField(0).getDevResteasyGrpcArraysArrayHolderField());
        }
        return holder.getComponentClass();
    }

    public static Class<?> getComponentClass(Class<?> clazz) {
        if (clazz.getComponentType().isArray()) {
            return getComponentClass(clazz.getComponentType());
        }
        return clazz.getComponentType();
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

    private static byte[] byteStringToByteArray(ByteString bs) {
       byte[] bytes = new byte[bs.size()];
       for (int i = 0; i < bs.size(); i++) {
          bytes[i] = bs.byteAt(i);
       }
       return bytes;
    }
}
