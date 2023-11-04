package dev.resteasy.grpc.arrays;

public class ArrayHolder {

    /*
     * string componentClass = 1;
     * bool bottom = 2;
     * repeated google.protobuf.Any google_protobuf_Any_field = 3;
     */

    private String componentClass;
    private boolean bottom;
    Object[] objects;
}
