package org.jboss.resteasy.test.grpc;

import org.junit.Test;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

import dev.resteasy.grpc.example.CC1JavabufTranslator;
import dev.resteasy.grpc.example.CC1_proto;
import dev.resteasy.grpc.example.CC1_proto.gInteger;

public class GenericTest {

    @Test
    public void g() throws InvalidProtocolBufferException {
        Message m = CC1_proto.gInteger.newBuilder().setValue(17).build();
        Any any = Any.pack(m);
        System.out.println(any.getTypeUrl());
        String t = any.getTypeUrl().substring(any.getTypeUrl().indexOf('/') + 1);
        System.out.println(t);
        System.out.println(any.is(CC1_proto.gInteger.class));
        System.out.println(any.getValue());
        System.out.println(CC1_proto.gInteger.parseFrom(any.getValue()));
        gInteger gi = CC1_proto.gInteger.parseFrom(any.getValue());
        System.out.println(gi.getValue());
        m = CC1JavabufTranslator.translateToJavabuf(Integer.valueOf(19));
        System.out.println(m);
        System.out.println(m.getClass());
        ByteString bs = any.getValue();
        Object o = CC1JavabufTranslator.translateFromJavabuf(m);
        System.out.println(o);
        System.out.println(o.getClass());

        System.out.println(any.toString());
        gi = gInteger.newBuilder().mergeFrom(any.getValue()).build();
        //      System.out.println(gInteger.newBuilder().mergeFrom(any).build());

        Descriptor descriptor = Any.getDescriptor();
        FieldDescriptor fd = descriptor.getFields().get(1);
        o = any.getField(fd);

        m = CC1JavabufTranslator.translateToJavabuf(any);
        System.out.println(m);
        System.out.println(m.getClass());
    }

    @Test
    public void g2() throws InvalidProtocolBufferException {

    }

}
