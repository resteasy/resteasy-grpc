package org.jboss.resteasy.test.grpc;

import dev.resteasy.grpc.example.CC1ServiceGrpcImpl;
import dev.resteasy.grpc.example.CC1_proto;
import dev.resteasy.grpc.example.CC1_proto.GeneralReturnMessage;
import dev.resteasy.grpc.example.CC1_proto.gString;
import io.grpc.stub.StreamObserver;

public class CC1ServiceGrpcImplSub extends CC1ServiceGrpcImpl {

    @java.lang.Override
    public void copy(CC1_proto.GeneralEntityMessage param,
            StreamObserver<dev.resteasy.grpc.example.CC1_proto.GeneralReturnMessage> responseObserver) {
        try {
            gString reply = gString.newBuilder().setValue("xyz").build();
            GeneralReturnMessage.Builder grmb = GeneralReturnMessage.newBuilder();
            grmb.setGStringField(reply);
            responseObserver.onNext(grmb.build());
        } catch (Exception e) {
            responseObserver.onError(e);
        } finally {
            responseObserver.onCompleted();
        }
    }
}
