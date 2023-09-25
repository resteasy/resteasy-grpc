package dev.resteasy.grpc.example;

public class IntfImpl implements Intf {

    private String s;

    @Override
    public String getS() {
        return s;
    }

    @Override
    public void setS(String obj) {
        this.s = obj;
    }
}
