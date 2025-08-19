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

package dev.resteasy.grpc.bridge.generator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
class ServerGrpcGenerator {

    private final Path sourceDir;
    private final String packageName;
    private final String prefix;

    ServerGrpcGenerator(final Path sourceDir, final String packageName, final String prefix) {
        this.sourceDir = sourceDir;
        this.packageName = packageName;
        this.prefix = prefix;
    }

    public String generate() throws IOException {
        final var file = sourceDir.resolve(packageName.replace('.', File.separatorChar))
                .resolve(prefix + "_Server.java");
        Files.createDirectories(file.getParent());
        try (SourceWriter writer = new SourceWriter(file)) {
            // Write the package name
            writer.writeLine("package ", packageName, ";");

            // Write the imports
            writer.writeLine("import dev.resteasy.grpc.bridge.runtime.i18n.Messages;")
                    .writeLine("import java.security.AccessController;")
                    .writeLine("import java.security.PrivilegedExceptionAction;")
                    .writeLine("import java.util.concurrent.TimeUnit;")
                    .writeLine("import java.util.logging.Logger;")
                    .writeLine()
                    .writeLine("import io.grpc.Server;")
                    .writeLine("import io.grpc.ServerBuilder;")
                    .writeLine("import jakarta.servlet.ServletContext;")
                    .writeLine("import jakarta.servlet.http.HttpServletRequest;")
                    .writeLine("import jakarta.ws.rs.GET;")
                    .writeLine("import jakarta.ws.rs.Path;")
                    .writeLine("import jakarta.ws.rs.core.Context;")
                    .writeLine("import jakarta.ws.rs.core.Response;")
                    .writeLine("import java.util.concurrent.atomic.AtomicBoolean;");

            // Start the type
            writer.writeLine("@Path(\"grpcserver\")")
                    .startBlock("public class ", prefix, "_Server {")
                    .writeLine();

            // Write our fields
            writer.writeLine(
                    "private static final Logger logger = Logger.getLogger(", prefix, "_Server.class.getName());")
                    .writeLine("private static ServletContext servletContext;")
                    .writeLine("private static int PORT = 8082;")
                    .writeLine("private Server server;")
                    .writeLine("private static final AtomicBoolean servletContextInitialized = new AtomicBoolean(false);");

            // Write the main method
            writer.writeLine("/**")
                    .writeLine("* Main launches the server from the command line.")
                    .writeLine("*/")
                    .startBlock("public static void main(String[] args) throws Exception, InterruptedException {")
                    .writeLine("final ", prefix, "_Server server = new ", prefix, "_Server();")
                    .writeLine("server.start();")
                    .writeLine("server.blockUntilShutdown();")
                    .endBlock();

            // The static servlet context
            writer.startBlock("public static ServletContext getServletContext() {")
                    .writeLine("return servletContext;")
                    .endBlock();

            writer.writeLine("@Path(\"context\")")
                    .writeLine("@GET")
                    .startBlock("public Response startContext(@Context HttpServletRequest request) throws Exception {")
                    .writeLine("if (!servletContextInitialized.getAndSet(true)) {")
                    .writeLine("   servletContext = request.getServletContext();")
                    .writeLine("}")
                    .writeLine("return Response.status(204).build();")
                    .endBlock()
                    .writeLine();

            writer.writeLine("@Path(\"start\")")
                    .writeLine("@GET")
                    .startBlock("public String startGRPC(@Context HttpServletRequest request) throws Exception {")
                    .writeLine("servletContext = request.getServletContext();")
                    .writeLine("final ", prefix, "_Server server = new ", prefix, "_Server();")
                    .startBlock("new Thread() {")
                    .writeLine("@SuppressWarnings({\"deprecation\", \"removal\"})")
                    .startBlock("public void run() {")
                    .startBlock("try {")
                    .startBlock("if (System.getSecurityManager() == null) {")
                    .writeLine("server.start();")
                    .endAndStart("} else {")
                    .writeLine(
                            "AccessController.doPrivileged((PrivilegedExceptionAction<Void>) () -> {server.start(); return null;});")
                    .endBlock()
                    .writeLine("logger.info(Messages.MESSAGES.startedGrpcServer(PORT));")
                    .writeLine("server.blockUntilShutdown();")
                    .endAndStart("} catch (Exception e) {")
                    .endBlock()
                    .endBlock()
                    .endBlock("}.start();")
                    .writeLine("return Messages.MESSAGES.startingGrpcServer(PORT);")
                    .endBlock()
                    .writeLine();

            writer.writeLine("@Path(\"ready\")")
                    .writeLine("@GET")
                    .startBlock("public String ready() {")
                    .writeLine("logger.info(Messages.MESSAGES.gRPCServerReady());")
                    .writeLine("return Messages.MESSAGES.ready();")
                    .endBlock()
                    .writeLine();

            writer.writeLine("@Path(\"stop\")")
                    .writeLine("@GET")
                    .startBlock("public void stopGRPC() throws Exception {")
                    .writeLine("logger.info(Messages.MESSAGES.stoppingGrpcServer(PORT));")
                    .writeLine("stop();")
                    .endBlock()
                    .writeLine();

            writer.writeLine("/**")
                    .writeLine("* Start gRPC server.")
                    .writeLine("*/")
                    .writeLine("@SuppressWarnings({\"removal\", \"deprecation\"})")
                    .startBlock("private void start() throws Exception {")
                    .startBlock("if (System.getSecurityManager() == null) {")
                    .writeLine("server = ServerBuilder.forPort(PORT)")
                    .writeLine(".addService(new ", prefix, "ServiceGrpcImpl())")
                    .writeLine(".build()")
                    .writeLine(".start();")
                    .endAndStart("} else {")
                    .startBlock("AccessController.doPrivileged((PrivilegedExceptionAction<Server>) () -> {")
                    .writeLine("server = ServerBuilder.forPort(PORT)")
                    .writeLine(".addService(new ", prefix, "ServiceGrpcImpl())")
                    .writeLine(".build()")
                    .writeLine(".start();")
                    .writeLine("return server;")
                    .endBlock("});")
                    .endBlock()
                    .writeLine("logger.info(Messages.MESSAGES.serverStarted(PORT));")
                    .startBlock("Runtime.getRuntime().addShutdownHook(new Thread() {")
                    .writeLine("@Override")
                    .startBlock("public void run() {")
                    .writeLine("// Use stderr here since the logger may have been reset by its JVM shutdown hook.")
                    .writeLine("System.err.println(Messages.MESSAGES.shuttingDownGrpcServer());")
                    .startBlock("try {")
                    .writeLine(prefix, "_Server.this.stop();")
                    .endAndStart("} catch (InterruptedException e) {")
                    .endBlock()
                    .writeLine("System.err.println(Messages.MESSAGES.serverShutDown());")
                    .endBlock()
                    .endBlock("});")
                    .endBlock()
                    .writeLine();

            writer.writeLine("/**")
                    .writeLine("* Stop gRPC server.")
                    .writeLine("*/")
                    .startBlock("private void stop() throws InterruptedException {")
                    .startBlock("if (server != null) {")
                    .writeLine("server.shutdown().awaitTermination(30, TimeUnit.SECONDS);")
                    .endBlock()
                    .endBlock()
                    .writeLine();

            writer.writeLine("/**")
                    .writeLine("* Await termination on the main thread since the grpc library uses daemon threads.")
                    .writeLine("*/")
                    .startBlock("private void blockUntilShutdown() throws InterruptedException {")
                    .startBlock("if (server != null) {")
                    .writeLine("server.awaitTermination();")
                    .endBlock()
                    .endBlock()
                    .writeLine();

            // End the class
            writer.endBlock();
        }
        return packageName.endsWith(".") ? packageName + prefix + "_Server" : packageName + "." + prefix + "_Server";
    }
}
