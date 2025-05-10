package dev.resteasy.grpc.arrays;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import dev.resteasy.grpc.example.CC2;

@Path("")
public class ArrayResource {

    @Path("single/boolean")
    @POST
    public boolean[] singlePrimitiveBoolean(boolean[] array) {
        return array;
    }

    @Path("single/Boolean")
    @POST
    public Boolean[] singleBoolean(Boolean[] array) {
        return array;
    }

    @Path("single/byte")
    @POST
    public byte[] singlePrimitiveByte(byte[] array) {
        return array;
    }

    @Path("single/Byte")
    @POST
    public Byte[] singleByte(Byte[] array) {
        return array;
    }

    @Path("single/short")
    @POST
    public short[] singlePrimitiveShort(short[] array) {
        return array;
    }

    @Path("single/Short")
    @POST
    public Short[] singleShort(Short[] array) {
        return array;
    }

    @Path("single/int")
    @POST
    public int[] singlePrimitiveInt(int[] array) {
        return array;
    }

    @Path("single/Integer")
    @POST
    public Integer[] singleInteger(Integer[] array) {
        return array;
    }

    @Path("single/long")
    @POST
    public long[] singlePrimitiveLong(long[] array) {
        return array;
    }

    @Path("single/Long")
    @POST
    public Long[] singleLong(Long[] array) {
        return array;
    }

    @Path("single/float")
    @POST
    public float[] singlePrimitiveFloat(float[] array) {
        return array;
    }

    @Path("single/Float")
    @POST
    public Float[] singleFloat(Float[] array) {
        return array;
    }

    @Path("single/double")
    @POST
    public double[] singlePrimitiveDouble(double[] array) {
        return array;
    }

    @Path("single/Double")
    @POST
    public Double[] singleDouble(Double[] array) {
        return array;
    }

    @Path("single/char")
    @POST
    public char[] singlePrimitiveChar(char[] array) {
        return array;
    }

    @Path("single/Character")
    @POST
    public Character[] singleCharacter(Character[] array) {
        return array;
    }

    @Path("single/String")
    @POST
    public String[] singleString(String[] array) {
        return array;
    }

    @Path("single/Object")
    @POST
    public Object[] singleObject(Object[] array) {
        return array;
    }

    @Path("single/CC2")
    @POST
    public CC2[] singleCC2(CC2[] array) {
        return array;
    }

    @Path("multi/boolean")
    @POST
    public boolean[][] multiBooleanPrimitive(boolean[][] array) {
        return array;
    }

    @Path("multi/Boolean")
    @POST
    public Boolean[][] multiBoolean(Boolean[][] array) {
        return array;
    }

    @Path("multi/byte")
    @POST
    public byte[][] multiBytePrimitive(byte[][] array) {
        return array;
    }

    @Path("multi/Byte")
    @POST
    public Byte[][] multiByte(Byte[][] array) {
        return array;
    }

    @Path("multi/short")
    @POST
    public short[][] multiShortPrimitive(short[][] array) {
        return array;
    }

    @Path("multi/Short")
    @POST
    public Short[][] multiShort(Short[][] array) {
        return array;
    }

    @Path("multi/int")
    @POST
    public int[][] multiIntPrimitive(int[][] array) {
        return array;
    }

    @Path("multi/Integer")
    @POST
    public Integer[][] multiInteger(Integer[][] array) {
        return array;
    }

    @Path("multi/long")
    @POST
    public long[][] multiLongPrimitive(long[][] array) {
        return array;
    }

    @Path("multi/Long")
    @POST
    public Long[][] multiLong(Long[][] array) {
        return array;
    }

    @Path("multi/float")
    @POST
    public float[][] multiFloatPrimitive(float[][] array) {
        return array;
    }

    @Path("multi/Float")
    @POST
    public Float[][] multiFloat(Float[][] array) {
        return array;
    }

    @Path("multi/double")
    @POST
    public double[][] multiDoublePrimitive(double[][] array) {
        return array;
    }

    @Path("multi/Double")
    @POST
    public Double[][] multiDouble(Double[][] array) {
        return array;
    }

    @Path("multi/char")
    @POST
    public char[][] multiCharPrimitive(char[][] array) {
        return array;
    }

    @Path("multi/Character")
    @POST
    public Character[][] multiCharacter(Character[][] array) {
        return array;
    }

    @Path("multi/String")
    @POST
    public String[][] multiString(String[][] array) {
        return array;
    }

    @Path("multi/Object")
    @POST
    public Object[][] multiObject(Object[][] array) {
        return array;
    }

    @Path("multi/CC2")
    @POST
    public CC2[][] multiCC2(CC2[][] array) {
        return array;
    }

    @Path("triple/CC2")
    @POST
    public CC2[][][] tripleCC2(CC2[][][] array) {
        return array;
    }

    @Path("triple/Object")
    @POST
    public Object[][][] tripleObject(Object[][][] array) {
        return array;
    }

    @GET
    @Path("arrays/int/5")
    public int[][][][][] arraysInt5(int[][][][][] array) {
        return array;
    }

    @GET
    @Path("arrays/stuff")
    public ArrayStuff arrayStuff(ArrayStuff as) {
        if (as.equals(new ArrayStuff(false))) {
            return new ArrayStuff(true);
        }
        return new ArrayStuff(false);
    }

    @GET
    @Path("arrays/stuff/array")
    public ArrayStuff[] arrayStuffArray(ArrayStuff[] ass) {
        return ass;
    }

    @GET
    @Path("arraystuff/stuff")
    public ArrayStuff.Stuff arrayStuffStuff(ArrayStuff.Stuff ass) {
        return new ArrayStuff.Stuff(ass.i * 2);
    }
}
