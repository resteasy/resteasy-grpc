package dev.resteasy.grpc.example;

public class ArrayHolder {

    int i = 17;
    int[] ints = new int[] { 1, 2, 3 };
    Other[] others = new Other[] { new Other("a"), new Other("b") };

    public ArrayHolder() {

    }

    public ArrayHolder(int i, int[] ints, Other[] others) {
        this.i = i;
        for (int j = 0; j < ints.length; j++) {
            this.ints[j] = ints[j];
        }
        for (int j = 0; j < others.length; j++) {
            this.others[j] = others[j];
        }
    }

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public int[] getInts() {
        return ints;
    }

    public void setInts(int[] ints) {
        this.ints = ints;
    }

    public Other[] getOthers() {
        return others;
    }

    public void setOthers(Other[] others) {
        this.others = others;
    }
}

class Other {
    private String s;

    public Other(String s) {
        this.s = s;
    }

    public String getS() {
        return s;
    }

    public void setS(String s) {
        this.s = s;
    }
}
