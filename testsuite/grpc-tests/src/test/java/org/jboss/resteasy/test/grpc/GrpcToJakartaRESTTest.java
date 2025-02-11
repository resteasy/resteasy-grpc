/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2023 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.resteasy.test.grpc;

import java.util.concurrent.TimeUnit;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import dev.resteasy.grpc.example.CC1ServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@ExtendWith(ArquillianExtension.class)
@RunAsClient
public class GrpcToJakartaRESTTest extends AbstractGrpcToJakartaRESTTest {

    @Deployment
    public static Archive<?> deployPlainText() throws Exception {
        return doDeploy(GrpcToJakartaRESTTest.class.getSimpleName());
    }

    private static ManagedChannel channelPlaintext;

    private static CC1ServiceGrpc.CC1ServiceBlockingStub blockingStubPlaintext;

    private static CC1ServiceGrpc.CC1ServiceStub asyncStubPlaintext;

    private static CC1ServiceGrpc.CC1ServiceFutureStub futureStubPlaintext;

    @BeforeAll
    public static void beforeClass() throws Exception {
        accessServletContexts();
        channelPlaintext = ManagedChannelBuilder.forTarget("localhost:9555").usePlaintext().build();

        blockingStubPlaintext = CC1ServiceGrpc.newBlockingStub(channelPlaintext);
        asyncStubPlaintext = CC1ServiceGrpc.newStub(channelPlaintext);
        futureStubPlaintext = CC1ServiceGrpc.newFutureStub(channelPlaintext);
    }

    @AfterAll
    public static void afterClass() throws InterruptedException {
        if (channelPlaintext != null) {
            channelPlaintext.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    public void testPlaintext() throws Exception {
        doBlockingTest(blockingStubPlaintext);
        doAsyncTest(asyncStubPlaintext);
        doFutureTest(futureStubPlaintext);
    }
}
