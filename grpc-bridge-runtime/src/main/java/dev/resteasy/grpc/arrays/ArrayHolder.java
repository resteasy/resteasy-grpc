package dev.resteasy.grpc.arrays;

import java.util.List;

import com.google.protobuf.Any;

public class ArrayHolder {

    private boolean bottom;
    private String componentClass;
    private List<Any> list;

    public boolean isBottom() {
        return bottom;
    }

    public void setBottom(boolean bottom) {
        this.bottom = bottom;
    }

    public String getComponentClass() {
        return componentClass;
    }

    public void setComponentClass(String componentClass) {
        this.componentClass = componentClass;
    }

    public Any getAny(int i) {
        return list.get(i);
    }

    public void setAny(List<Any> list) {
        this.list = list;
    }

    public void addAny(Any any) {
        list.add(any);
    }
}
