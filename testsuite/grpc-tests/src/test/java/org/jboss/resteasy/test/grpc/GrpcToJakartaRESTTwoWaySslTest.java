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
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.as.controller.client.helpers.Operations.CompositeOperationBuilder;
import org.jboss.dmr.ModelNode;
import org.jboss.resteasy.setup.SnapshotServerSetupTask;
import org.jboss.resteasy.utils.ServerReload;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import dev.resteasy.grpc.example.CC1ServiceGrpc;
import io.grpc.ChannelCredentials;
import io.grpc.Grpc;
import io.grpc.ManagedChannel;
import io.grpc.TlsChannelCredentials;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@RunWith(Arquillian.class)
@RunAsClient
@ServerSetup(GrpcToJakartaRESTTwoWaySslTest.OneWaySslConfiguration.class)
public class GrpcToJakartaRESTTwoWaySslTest extends AbstractGrpcToJakartaRESTTest {

    public static class OneWaySslConfiguration extends SnapshotServerSetupTask {
        @Override
        protected void doSetup(final ManagementClient client, final String containerId) throws Exception {
            final CompositeOperationBuilder builder = CompositeOperationBuilder.create();

            // /subsystem=elytron/key-store=grpc-key-store:add(credential-reference={clear-text="secret"}, type=JKS,
            // path="server.keystore.jks", relative-to="jboss.server.config.dir", required=false)
            ModelNode address = Operations.createAddress("subsystem", "elytron", "key-store", "grpc-key-store");
            ModelNode op = Operations.createAddOperation(address);
            final ModelNode credentialRef = new ModelNode();
            credentialRef.get("clear-text").set(TestSslUtil.KEYSTORE_PASSWORD);
            op.get("credential-reference").set(credentialRef);
            op.get("type").set("JKS");
            op.get("path").set(TestSslUtil.getServerKeystoreFile().toString());
            //op.get("relative-to").set("jboss.server.config.dir");
            op.get("required").set(false);
            builder.addStep(op);

            // /subsystem=elytron/key-store=grpc-trust-store:add(credential-reference={clear-text="secret"}, type=JKS,
            // required=false, path="server.truststore.jks", relative-to="jboss.server.config.dir")
            address = Operations.createAddress("subsystem", "elytron", "key-store", "grpc-trust-store");
            op = Operations.createAddOperation(address);
            op.get("credential-reference").set(credentialRef);
            op.get("type").set("JKS");
            op.get("path").set(TestSslUtil.getServerTruststoreFile().toString());
            //op.get("relative-to").set("jboss.server.config.dir");
            builder.addStep(op);

            // /subsystem=elytron/key-manager=grpc-key-manager:add(key-store=grpc-key-store,
            // credential-reference={clear-text="secret"})
            address = Operations.createAddress("subsystem", "elytron", "key-manager", "grpc-key-manager");
            op = Operations.createAddOperation(address);
            op.get("key-store").set("grpc-key-store");
            op.get("credential-reference").set(credentialRef);
            builder.addStep(op);

            // /subsystem=elytron/trust-manager=grpc-key-store-trust-manager:add(key-store="grpc-trust-store")
            address = Operations.createAddress("subsystem", "elytron", "trust-manager", "grpc-key-store-trust-manager");
            op = Operations.createAddOperation(address);
            op.get("key-store").set("grpc-trust-store");
            builder.addStep(op);

            // /subsystem=elytron/server-ssl-context=grpc-ssl-context:add(cipher-suite-filter=DEFAULT, protocols=["TLSv1.2"],
            //      want-client-auth="false", need-client-auth="true", authentication-optional="false",
            //      use-cipher-suites-order="false", key-manager="grpc-key-manager",
            //      trust-manager="grpc-key-store-trust-manager")
            address = Operations.createAddress("subsystem", "elytron", "server-ssl-context", "grpc-ssl-context");
            op = Operations.createAddOperation(address);
            op.get("cipher-suite-filter").set("DEFAULT");
            final ModelNode protocols = new ModelNode().setEmptyList();
            protocols.add("TLSv1.2");
            op.get("protocols").set(protocols);
            op.get("want-client-auth").set(false);
            op.get("need-client-auth").set(true);
            op.get("authentication-optional").set(false);
            op.get("use-cipher-suites-order").set(false);
            op.get("key-manager").set("grpc-key-manager");
            op.get("trust-manager").set("grpc-key-store-trust-manager");
            builder.addStep(op);

            // /subsystem=undertow/server=default-server/https-listener=https:add(socket-binding=https, ssl-context="grpc-ssl-context")
            address = Operations.createAddress("subsystem", "undertow", "server", "default-server", "https-listener", "https");
            op = Operations.createAddOperation(address);
            op.get("socket-binding").set("https");
            op.get("ssl-context").set("grpc-ssl-context");
            builder.addStep(op);

            // /subsystem=grpc:write-attribute(name=key-manager-name, value="grpc-key-manager")
            address = Operations.createAddress("subsystem", "grpc");
            builder.addStep(Operations.createWriteAttributeOperation(address, "key-manager-name", "grpc-key-manager"));

            // /subsystem=grpc:write-attribute(name=trust-manager-name, value="grpc-key-store-trust-manager")
            address = Operations.createAddress("subsystem", "grpc");
            builder.addStep(
                    Operations.createWriteAttributeOperation(address, "trust-manager-name", "grpc-key-store-trust-manager"));

            final var result = client.getControllerClient().execute(builder.build());
            if (!Operations.isSuccessfulOutcome(result)) {
                throw new RuntimeException("Failed to configure SSL context: " + Operations.getFailureDescription(result));
            }
            ServerReload.reloadIfRequired(client.getControllerClient());
        }
    }

    private static ManagedChannel channelSslTwoway;

    private static CC1ServiceGrpc.CC1ServiceBlockingStub blockingStubSslTwoway;

    private static CC1ServiceGrpc.CC1ServiceStub asyncStubSslTwoway;

    private static CC1ServiceGrpc.CC1ServiceFutureStub futureStubSslTwoway;

    @Deployment
    public static Archive<?> deployPlainText() throws Exception {
        return doDeploy(GrpcToJakartaRESTTwoWaySslTest.class.getSimpleName());
    }

    @BeforeClass
    public static void beforeClass() throws Exception {
        accessServletContexts();
        ChannelCredentials creds = TlsChannelCredentials.newBuilder()
                .trustManager(TestSslUtil.getTrustManager())
                .keyManager(TestSslUtil.getClientKeyManager())
                .build();
        channelSslTwoway = Grpc.newChannelBuilderForAddress("localhost", 9555, creds).build();

        blockingStubSslTwoway = CC1ServiceGrpc.newBlockingStub(channelSslTwoway);

        asyncStubSslTwoway = CC1ServiceGrpc.newStub(channelSslTwoway);

        futureStubSslTwoway = CC1ServiceGrpc.newFutureStub(channelSslTwoway);
    }

    @AfterClass
    public static void afterClass() throws InterruptedException {
        channelSslTwoway.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
    }

    @Test
    public void testSslTwoway() throws Exception {
        doBlockingTest(blockingStubSslTwoway);
        doAsyncTest(asyncStubSslTwoway);
        doFutureTest(futureStubSslTwoway);
    }

}
