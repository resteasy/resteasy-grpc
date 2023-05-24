# gRPC Bridge Project

The RESTEasy **gRPC Bridge Project** (aka **resteasy-grpc**) project
(<https://github.com/resteasy/resteasy-grpc>) has been developed to
enable communication between gRPC clients and Jakarta REST servers. The
classes in resteasy-grpc's **grpc-bridge** module are able to scan a
directory tree of Jakarta REST resource classes and generate classes
that form an intermediary layer between the gRPC runtime and a Jakarta
REST server. Those generated classes are supported by the
**grpc-bridge-runtime** module. The process of generating the files is
facilitated by the maven archetype
<https://github.com/resteasy/gRPCtoJakartaREST-archetype>.

## protobuf and gRPC

[gRPC](https://grpc.io/), introduced by Google, is a relatively recent
member of the extended Remote Procedure Call (RPC) / Remote Method
Invocation (RMI) family of client-server frameworks. An example of the
former is the venerable [CORBA](https://www.corba.org/), and an example
of the latter is [Java RMI](https://docs.oracle.com/javase/tutorial/rmi/). An early generation
of [JBossRemoting](https://jbossremoting.jboss.org/documentation/v2.html)
presented aspects of both.

### protobuf IDL file

gRPC shares with CORBA the concept of a programming language independent
**Interface Definition Language** (IDL) that can be translated into code
in any language for which a compiler exists. gRPC is built on top of
[protobuf](https://developers.google.com/protocol-buffers), a data
definiton framework, also from Google, which has an IDL and a precisely
defined wire format. For example,

        syntax = "proto3";
        package org.greet;
        option java_package = "org.greet";
        option java_outer_classname = "Greet_proto";

        message Greeting {
          string s = 1;
        }

is a protobuf IDL file that defines a data type called `Greeting` with a
single string field. Note that each message field is associated with a
distinct integer; e.g., `s` is associated with 1, which supports the
translation to and from the wire format. When it is compiled by the protobuf compiler (**protoc**),
the output is a several hundred line class called `org.greet.Greet_proto`:

        package org.greet;

        public final class Greet_proto {

           public static final class Greeting extends
                 com.google.protobuf.GeneratedMessageV3 implements GreetingOrBuilder {

              public java.lang.String getS() {
                 ...
              }

              public static final class Builder extends
                    com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
                    org.jboss.greeting.Greeting_proto.GreetingOrBuilder {

                 public Builder setS() {
                    ...
                 }
              }
           }
        }
       

The `Builder` class supports creating a `Greeting`

        org.greet.Greet_proto.Greeting.Builder builder = org.greet.Greet_proto.Greeting.newBuilder();
        Greeting greeting = builder.setS("foo").build();
       

and the `getS()` method supports retrieving the contents of the `Greeting`.

gRPC extends the protobuf IDL with a syntax for defining methods. For
example,

        syntax = "proto3";
        package org.greet;
        option java_package = "org.greet";
        option java_outer_classname = "Greet_proto";

        message Greeting {
           string s = 1;
        }

        message gString {string value = 2;}

        service GreetService {
           rpc greet (gString) returns (Greeting);
        }

adds the `greet` remote call. Note that it also adds a `gString` type
since remote calls can't use the builtin `string` type. When this
extended file is compiled by protoc together with its gRPC plugin, the result, in
addition to `org.greet.Greet_proto`, is the class
`org.greet.GreetServiceGrpc`, also with hundreds of lines:

        public final class GreetServiceGrpc {
           ...
           public static final class GreetServiceBlockingStub extends
                 io.grpc.stub.AbstractBlockingStub<GreetServiceBlockingStub> {
              ...
              public org.greet.Greet_proto.Greeting greet(org.greet.Greet_proto.gString request) {
                 ...
              }
           }
           ...
           public static final class GreetServiceStub extends
                 io.grpc.stub.AbstractAsyncStub<GreetServiceStub> {
              ...
              public void greet(org.greet.Greet_proto.gString request,
                 io.grpc.stub.StreamObserver<org.greet.Greet_proto.gString> responseObserver) {
                 ...
              }
           }
        }
       

### Client stubs

The inner class `GreetServiceGrpc.GreetServiceBlockingStub` is used on
the client side to invoke the methods defined in the IDL file. In
this case, there is only one, `greet()`, and an invocation would look
something like:

        private static String target = "localhost:8082";
        private static ManagedChannel channel;
        private static GreetServiceBlockingStub blockingStub;

        public static void setup() throws Exception {
           channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
           blockingStub = GreetServiceGrpc.newBlockingStub(channel);
        }

        public void test() throws Exception {
           org.greet.Greet_proto.gString gs = org.greet.Greet_proto.gString.newBuilder().setS("foo").build();
           org.greet.Greet_proto.Greeting response = blockingStub.greet(gs);
           String s = response.getS();
           ...
        }       

There are also

-   GreetServiceStub: asynchronous client stub
-   GreetServiceFutureStub: client stub which returns a GrpcFuture

### Server stubs

The gRPC plugin also generates the inner class
`GreetServiceGrpc.GreetServiceImplBase`, which has a default method for
each rpc entry in the IDL file. The default method will indicate that
the method is not implemented. The idea is that the developer should
create a class extending `GreetServiceImplBase` with implementing
methods. A simple example is

        public void greet(org.greet.Greet_proto.gString request, StreamObserver<org.greet.Greet_proto.Greeting> responseObserver) {
           String name = request.getValue();
           org.greet.Greet_proto.Greeting greeting = org.greet.Greet_proto.Greeting.newBuilder().setS("hello, " + name).build();
           responseObserver.onNext(greeting);
        }
       

### google.protobuf.Any

As we will see below, there are situations in which the actual type of a
message cannot be determined until runtime, and protobuf has a general
purpose type, `google.protobuf.Any`, which can hold any type of message.
The definition of `Any` is

        message Any {
           string type_url = 1;
           bytes value = 2;
        }   

The `value` field has built-in type `bytes`, which "May contain any
arbitrary sequence of bytes no longer than 2^32", according to
<https://developers.google.com/protocol-buffers/docs/proto3>. The type
of the message stored in the `value` is described by the URL in the
`type_url` field. Consider, for example,

        gString gs = gString.newBuilder().setValue("abc").build();
        Message m = Any.pack(gs);
        System.out.println(m);
       

The output is

        type_url: "type.googleapis.com/org.greet.gString"
        value: "\272\001\003abc"

The string "\\272\\001\\003abc" is the internal representation of a
`gString`, the details of which are beyond the scope of this discussion.
See <https://developers.google.com/protocol-buffers/docs/encoding> for
details. The URL is "type.googleapis.com/org.greet.gString", where the
path "org.greet.gString" gives the type of the object represented in
the `value` field.

The advantage of the `type_url` field is that it can be used to retrieve
the value of the `Any`. Consider, for example, the code

        Any any = null;
        if (/* some predicate */) {
           gString gs = gString.newBuilder().setValue("abc").build();
           any = Any.pack(gs);
        } else {
           gInteger gi = gInteger.newBuilder().setValue(7).build();
           any = Any.pack(gi);
        }
        /* send any */
       

Then, the `Any` can be unpacked as follows:

        /* get any */
        if (any.getTypeUrl().endsWith("org.greet.gString")) {
           gString gs = any.unpack(gString.class);
           System.out.println("gs: " + gs);
        } else if (any.getTypeUrl().endsWith("org.greet.gInteger")) {
           gInteger gi = any.unpack(gInteger.class);
           System.out.println("gi: " + gi);
        }   

## Connecting a gRPC client to a Jakarta REST server

A gRPC client needs to access the client stubs like
`GreetServiceBlockingStub`, which are generated from an IDL file
by the protobuf compiler together with its gRPC plugin. That is, the process
starts with an IDL file. Where does the IDL file come from? In a typical
case, the IDL file is part of the design and is created manually early
in the process. In the situation we are addressing here, though, we have
a pre-existing Jakarta REST service to which the IDL file must conform.
Now, in principle, it could be generated manually from the Jakarta REST
resource classes, but that would be tedious and error prone.
resteasy-grpc's grpc-bridge module automates the process.

### Generating an IDL file

The class
`dev.resteasy.grpc.bridge.generator.protobuf.JavaToProtobufGenerator`
traverses, with the help of the Java parser
<https://github.com/javaparser/javaparser>, a set of Jakarta REST
resource classes. For each class that appears as an entity type or a
return type of a resource method or resource locator,
`JavaToProtobufGenerator` generates a protobuf message. For each
resource method or resource locator, it generates an rpc entry.

Note that not all message types can be discovered by syntactic
examination, since a resource method could return a
`jakarta.ws.rs.core.Response`, where the type of the actual entity
depends on the behavior of the method. Technically, it's a
non-computable problem. There is a mechanism for manually specifying
addtional classes, which we will discuss in [Building the bridge
project](#building_bridge).

Given `org.greet.Greeting`

        package org.greet;

        public class Greeting {
           private String s;

           public Greeting(String s) {
              this.s = s;
           }
        }   

and `org.greet.Greeter`

        package org.greet;

        import jakarta.ws.rs.GET;
        import jakarta.ws.rs.Path;

        @Path("")
        public class Greeter {

           @GET
           @Path("greet")
           public Greeting greet(String s) {
              return new Greeting("hello, " + s);
           }
        }

`JavaToProtobufGenerator` will generate the IDL file Greet.proto:

        syntax = "proto3";
        package org.greet;
        import "google/protobuf/any.proto";
        import "google/protobuf/timestamp.proto";
        option java_package = "org.greet";
        option java_outer_classname = "Greet_proto";

        service GreetService {
        // /greet gString org_greet___Greeting GET sync
          rpc greet (GeneralEntityMessage) returns (GeneralReturnMessage);
        }

        message org_greet___Greeting {
          string s = 1;
        }

        message gInteger   {int32  value = 2;}
        message gFloat     {float  value = 3;}
        message gCharacter {string value = 4;}
        message gByte      {int32  value = 5;}
        message gLong      {int64  value = 6;}
        message gString    {string value = 7;}
        message gBoolean   {bool   value = 8;}
        message gDouble    {double value = 9;}
        message gShort     {int32  value = 10;}

        message gHeader {
           repeated string values = 11;
        }

        message gCookie {
           string name = 12;
           string value = 13;
           int32  version = 14;
           string path = 15;
           string domain = 16;
        }

        message gNewCookie {
           string name = 17;
           string value = 18;
           int32  version = 19;
           string path = 20;
           string domain = 21;
           string comment = 22;
           int32 maxAge = 23;
           google.protobuf.Timestamp expiry = 24;
           bool secure = 25;
           bool httpOnly = 26;

           enum SameSite {
              NONE   = 0;
              LAX    = 1;
              STRICT = 2;
           }

           SameSite sameSite = 27;
        }

        message ServletInfo {
           string characterEncoding = 28;
           string clientAddress = 29;
           string clientHost = 30;
           int32  clientPort = 31;
        }

        message FormValues {
           repeated string formValues_field = 32;
        }

        message FormMap {
           map<string, FormValues> formMap_field = 33;
        }

        message GeneralEntityMessage {
           ServletInfo servletInfo = 34;
           string URL = 35;
           map<string, gHeader> headers = 36;
           repeated gCookie cookies = 37;
           oneof messageType {
              gString gString_field = 38;
              FormMap form_field = 39;
           }
        }

        message GeneralReturnMessage {
           map<string, gHeader> headers = 40;
           repeated gNewCookie cookies = 41;
           gInteger status = 42;
           oneof messageType {
              org_greet___Greeting org_greet___Greeting_field = 43;
           }
        }

Clearly, the generated IDL file is more complicated than the one
discussed earlier. The more interesting distinctions are the following:

1.  protobuf does not have a notion of packages, so the class
    org.greet.Greeting
    is represented as
    org_greet\_\_\_Greeting

2.  Some information pertaining to the rpc entries is saved in comments
    for future use. In the example, "/greet gString org_greet\_\_\_Greeting GET sync" means:
    -   the path to the greet() method is "/greet"
    -   the type of the entity parameter is gString
    -   the type of the response entity is org_greet\_\_\_Greeting
    -   the HTTP verb on the resource method is GET
    -   the resource method is synchronous

3.  The `GeneralEntityMessage`
    message type is used as the request value for all methods. Something
    like this complex structure is necessary to bridge the gap between
    gRPC requests and Jakarta REST requests. In particular, while gRPC
    allows only a single request value, Jakarta REST allows, besides the
    entity parameter itself, things like headers, cookies, query
    parameters, etc. `GeneralEntityMessage`
    can accomodate all of those. Also, consider the element

           oneof messageType {
              gString gString_field = 42;
           }

    `oneof` is a protobuf construct that allows a field to be populated with a
    value whose type is one of the types listed in the
    `oneof` list. There's only one rpc method here, so there's only one type
    in the list. But suppose there were another rpc method with the comment

        // /float gFloat gInteger GET sync

    Then the `oneof` field would look like

            oneof messageType {
              gString gString_field = 42;
              gFloat gFloat_field = 43;
           }        

    This way, resource methods with a `String`
    entity type or a `float`
    entity type could be represented.

4.  The `GeneralReturnMessage` message type plays a role like
    `GeneralEntityMessage` but for return values.

5.  The world of cookie specification is somewhat fragmented, but the
    definitions here are intended to be generally applicable.

**Note.** There is a version of the classes mentioned here, `Greeting`,
etc., available to play with at
<https://github.com/resteasy/resteasy-examples/tree/main/grpc-bridge-example>.

### Inheritance

gRPC and Jakarta REST have different semantics, and the classes
`GeneralEntityMessage` and `GeneralReturnMessage` introduced in the
preceding section help to bridge the differences. Another fundamental
difference is the lack of a notion of inheritance in gRPC. That is, the
protobuf IDL supports nested structures but does not have a notion of a
structure being derived from another structure.
`JavaToProtobufGenerator` generates a special field to represent a
parent class. Let's define the class `GeneralGreeting`

        package org.greet;

        public class GeneralGreeting extends Greeting {
           private String salute;
   
           public GeneralGreeting(String salute, String s) {
              super(s);
              this.salute = salute;
           }
        }

and extend `Greeter`:

        @Path("")
        public class Greeter {
            ...

            @GET
            @Path("salute")
            public GeneralGreeting generalGreet(@QueryParam("salute") String salute, String s) {
                return getGeneralGreeting(salute, s);
            }

            private GeneralGreeting getGeneralGreeting(String salute, String name) {
                return new GeneralGreeting(salute, name);
            }
        }

Then `JavaToProtobufGenerator` will make the following adjustments to
Greet.proto:

        ...
        service GreetService { // 1
        // /greet gString org_greet___Greeting GET sync
          rpc greet (GeneralEntityMessage) returns (GeneralReturnMessage);
        // /salute gString org_greet___GeneralGreeting GET sync
          rpc generalGreet (GeneralEntityMessage) returns (GeneralReturnMessage);
        }
        ...
        message org_greet___GeneralGreeting { // 2
          string salute = 2;
          org_greet___Greeting greeting___super = 3;
        }
        ...
        message GeneralReturnMessage {
           map<string, gHeader> headers = 42;
           repeated gNewCookie cookies = 43;
           gInteger status = 44;
           oneof messageType { // 3
              org_greet___Greeting org_greet___Greeting_field = 45;
              org_greet___GeneralGreeting org_greet___GeneralGreeting_field = 46;
           }
        }

Note the following:

1.  A second rpc entry is generated.
2.  The new message type `org_greet___GeneralGreeting` is generated.
3.  A second message type option is added to the oneof field in `GeneralReturnMessage`.

Especially, note the field `greeting___super` in
`org_greet___GeneralGreeting`. The syntax "\_\_\_super" indicates that
the content of that field represents, in Java terms, the parent class
`org.greet.Greeting`. The classes in grpc-bridge that subsequently
process Greet.proto treat it accordingly.

Note, by the way, that `getGeneralGreeting()` doesn't lead to an rpc entry. That's because,
lacking in Jakarta REST annotations, it's not a resource method.

### Runtime intermediary layer on the server

The gRPC runtime accepts a gRPC request and dispatches it to
`GreetServiceGrpc`, whose methods are meant to be overridden by
"business logic" methods. Here, though, the business logic already exists
in the Jakarta REST resource class(es), so we want the request to be
forwarded to a Jakarta REST resource method, and we need code that
transforms a gRPC request to a Jakarta REST request. The class
`dev.resteasy.grpc.bridge.generator.ServiceGrpcExtender` in grpc-bridge
will generate `org.greet.GreetServiceGrpcImpl` with the necessary
methods.

Given the updated version of `org.greet.Greeter`, there will be two
methods in `GreetServiceGrpc` that need to be overridden. For example,

        public void greet(org.greet.Greet_proto.GeneralEntityMessage param, StreamObserver<org.greet.Greet_proto.GeneralReturnMessage> responseObserver);
       

will be overridden by

        public void greet(org.greet.Greet_proto.GeneralEntityMessage param, StreamObserver<org.greet.Greet_proto.GeneralReturnMessage> responseObserver) {
           HttpServletRequest request = null;
           try {
              HttpServletResponseImpl response = new HttpServletResponseImpl("org_greet___Greeting", "sync", Greet_Server.getContext(), builder, fd); // 1
              GeneratedMessageV3 actualParam = param.getGStringField();
              request = getHttpServletRequest(param, actualParam, "//greet", response, "GET", "org_greet___Greeting"); // 2
              HttpServletDispatcher servlet = getServlet(); // 3
              activateRequestContext(); // 4
              servlet.service(request.getMethod(), request, response); // 5
              MockServletOutputStream msos = (MockServletOutputStream) response.getOutputStream();
              ByteArrayOutputStream baos = msos.getDelegate();
              ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
              org_greet___Greeting reply = org_greet___Greeting.parseFrom(bais); // 6
              org.greet.Greet_proto.GeneralReturnMessage.Builder grmb = createGeneralReturnMessageBuilder(response);
              grmb.setOrgGreetGreetingField(reply);
              responseObserver.onNext(grmb.build()); // 7
           } catch (Exception e) {
              responseObserver.onError(e);
           } finally {
              responseObserver.onCompleted();
              if (requestContextController != null) {
                 requestContextController.deactivate();
              }
              if (tccl != null) {
                 Thread.currentThread().setContextClassLoader(tccl);
              }
           }

The general mission of `greet()` is to create a servlet environment for
the RESTEasy resource method to run in. More specifically, without going
into too much detail, the following steps occur:

1.  create a servlet response
2.  create a servlet request
3.  find the target servlet inside RESTEasy
4.  activate a CDI context
5.  call the
    service()
    method of the target servlet
6.  parse the response object
7.  pass the response back to the gRPC runtime

### Translating Java classes
<a name="translating_java_classes"/>

Note that the sequence

                  org.greet.Greeting (Java class)
                          -> (translated by JavaToProtobufGenerator) -> 
                          -> org_greet___Greeting (protobuf message)
                          -> (translated by protoc) -> 
                          -> org.greet.Greet_proto.org_greet___Greeting (Java class)

turns the Java class `org.greet.Greeting` into a second Java class
`org.greet.Greet_proto.org_greet___Greeting` by way of the protobuf
message type `org_greet___Greeting`. For clarity, we refer to
`org.greet.Greet_proto.org_greet___Greeting` as the **javabuf** version
of `org.greet.Greeting`.

Two classes are generated to translate back and forth between a Java
class and its javabuf counterpart. In particular, the class
`dev.resteasy.grpc.bridge.generator.protobuf.JavabufTranslatorGenerator`
generates a class like `org.greet.GreetJavabufTranslator`, which has two
static methods

        public static Message translateToJavabuf(Object o);
        public static Object translateFromJavabuf(Message message);

which do the translations. Note that all javabuf classes implement the
interface `com.google.protobuf.Message`. Without going too deeply into
the details, `GreetJavabufTranslator` has two classes for each message
type; for example,

        static class org_greet___Greeting_ToJavabuf implements TranslateToJavabuf { ... }
        static class org_greet___Greeting_FromJavabuf implements TranslateFromJavabuf { ... }

Each class has a list of lambdas, each lambda being responsible for
translating one field.

`GreetJavabufTranslator` does the heavy lifting of the translations. It
is called from the class `org.greet.GreetMessageBodyReaderWriter`, which
is generated by
`dev.resteasy.grpc.bridge.generator.protobuf.ReaderWriterGenerator`.
`GreetMessageBodyReaderWriter` implements
`jakarta.ws.rs.ext.MessageBodyReader` and
`jakarta.ws.rs.ext.MessageBodyWriter`, so it's registered as a provider
with the RESTEasy runtime. The request entity and the response entity
are instances of javabuf classes, so it's important that
`GreetMessageBodyReaderWriter` is always used instead of any other
providers. Since some built-in providers like
`org.jboss.resteasy.plugins.providers.StringTextStar` are very general,
it is important to guarantee that `GreetMessageBodyReaderWriter` has the
highest priority. One strategy available in RESTEasy is to eliminate
**all** built-in providers and then add back any that are necessary. For
example, that can be accomplished in a web.xml file as follows:

        <servlet>
           <servlet-name>GreetServlet</servlet-name>
           <servlet-class>
              dev.resteasy.grpc.bridge.runtime.servlet.GrpcHttpServletDispatcher
           </servlet-class>
        </servlet>
        
        <!-- 
           The intention is that GreetMessageBodyReaderWriter (with the help of GreetJavabufTranslator)
           will handle all reading and writing of data objects. Therefore, we

           1. eliminate all builtin providers, and then
           2. add back builtin providers other than MessageBodyReaders and MessageBodyWriters.

         -->
        <context-param>
            <param-name>resteasy.use.builtin.providers</param-name>
            <param-value>false</param-value>
        </context-param>
        <context-param>
            <param-name>resteasy.servlet.mapping.prefix</param-name>
            <param-value>/grpcToJakartaRest</param-value>
        </context-param>
        ...
         <context-param>
           <param-name>resteasy.providers</param-name>
           <param-value>
              org.jboss.resteasy.client.jaxrs.internal.CompletionStageRxInvokerProvider,
              org.jboss.resteasy.plugins.interceptors.CacheControlFeature,
              org.jboss.resteasy.plugins.interceptors.ClientContentEncodingAnnotationFeature,
              org.jboss.resteasy.plugins.interceptors.MessageSanitizerContainerResponseFilter,
              org.jboss.resteasy.plugins.interceptors.ServerContentEncodingAnnotationFeature,
              org.jboss.resteasy.plugins.providers.AsyncStreamingOutputProvider,
              org.jboss.resteasy.plugins.providers.CompletionStageProvider,
              org.jboss.resteasy.plugins.providers.jackson.PatchMethodFilter,
              org.jboss.resteasy.plugins.providers.jackson.UnrecognizedPropertyExceptionHandler,
              org.jboss.resteasy.plugins.providers.jaxb.XmlJAXBContextFinder,
              org.jboss.resteasy.plugins.providers.jsonp.JsonpPatchMethodFilter,
              org.jboss.resteasy.plugins.providers.ReactiveStreamProvider,
              org.jboss.resteasy.plugins.validation.ResteasyViolationExceptionMapper,
              org.jboss.resteasy.plugins.validation.ValidatorContextResolver,
              org.jboss.resteasy.plugins.validation.ValidatorContextResolverCDI,
              org.jboss.resteasy.security.doseta.ClientDigitalSigningHeaderDecoratorFeature,
              org.jboss.resteasy.security.doseta.ClientDigitalVerificationHeaderDecoratorFeature,
              org.jboss.resteasy.security.doseta.DigitalSigningInterceptor,
              org.jboss.resteasy.security.doseta.DigitalVerificationInterceptor,
              org.jboss.resteasy.security.doseta.ServerDigitalSigningHeaderDecoratorFeature,
              org.jboss.resteasy.security.doseta.ServerDigitalVerificationHeaderDecoratorFeature
           </param-value>
        </context-param>

        <servlet-mapping>
           <servlet-name>GreetServlet</servlet-name>
           <url-pattern>/grpcToJakartaRest/*</url-pattern>
        </servlet-mapping>

Of course, the list of providers can be reduced to those that are
actually needed.

### SSE
<a name="SSE"/>

One area in which gRPC has richer semantics than Jakarta REST is
streaming, where gRPC supports streaming in two directions, client to
server and server to client, while Jakarta REST supports streaming only
from server to client. In particular, Jakarta REST adopts a version of
the **Server Sent Events** (SSE) specification (
<https://html.spec.whatwg.org/multipage/server-sent-events.html>) to
describe server to client streaming. RESTEasy's support of SSE is
discussed in Section "Server-Sent Events (SSE)" of the [RESTEasy User Guide](https://resteasy.dev/docs/).

The examples so far have demonstrated simple call / response semantics.
A few changes are necessary to support SSE streaming. Suppose
`org.greet.Greeter` is extended with method `sseGreet`:

        private ArrayList<String> names = new ArrayList<String>();

        @GET
        @Path("stream")
        @Produces(MediaType.SERVER_SENT_EVENTS)
        public void sseGreet(@Context SseEventSink eventSink, @Context Sse sse) {
           ExecutorService executor = Executors.newFixedThreadPool(3);
           final Map<Class<?>, Object> map = ResteasyContext.getContextDataMap();
           executor.execute(() -> {
              ResteasyContext.addCloseableContextDataLevel(map);
              try (SseEventSink sink = eventSink) {
                 Iterator<String> it = names.iterator();
                 while (it.hasNext()) {
                    eventSink.send(sse.newEvent("hello, " + it.next()));
                 }
              }
           });
        }

A couple of additions appear in Greet.proto:

-   A new message type is added:

        message org_jboss_resteasy_grpc_runtime_sse___SseEvent {
          string comment = 1;
          string id = 2;
          string name = 3;
          google.protobuf.Any data = 4;
          int64 reconnectDelay = 5;
        }
                 
-   a new rpc entry is added:

        // stream gEmpty org_jboss_resteasy_grpc_runtime_sse___SseEvent GET sse
          rpc sseGreet (GeneralEntityMessage) returns (stream org_jboss_resteasy_grpc_runtime_sse___SseEvent);                 

    Note that `returns (stream org_jboss_resteasy_grpc_runtime_sse___SseEvent)` indicates that the call returns a stream of
    `org_jboss_resteasy_grpc_runtime_sse___SseEvent` objects.

The overriding method in `GreetServiceGrpcImpl` changes to handle
multiple return messages:

        @java.lang.Override
        public void sseGreet(org.greet.Greet_proto.GeneralEntityMessage param, StreamObserver<org.greet.Greet_proto.org_jboss_resteasy_grpc_runtime_sse___SseEvent> responseObserver) {
            HttpServletRequest request = null;
            try {
                HttpServletResponseImpl response = new HttpServletResponseImpl("org_jboss_resteasy_grpc_sse_runtime___SseEvent", "sse", Greet_Server.getContext(), builder, fd);
                GeneratedMessageV3 actualParam = param.getGEmptyField();
                request = getHttpServletRequest(param, actualParam, "/stream", response, "GET", "org_jboss_resteasy_grpc_sse_runtime___SseEvent");
                HttpServletDispatcher servlet = getServlet();
                activateRequestContext();
                servlet.service(request.getMethod(), request, response);
                AsyncMockServletOutputStream amsos = (AsyncMockServletOutputStream) response.getOutputStream();
                while (true) {
                    if (amsos.isClosed()) {
                        break;
                    }
                    ByteArrayOutputStream baos = amsos.await();
                    if (amsos.isClosed()) {
                        break;
                    }
                    byte[] bytes = baos.toByteArray();
                    if (bytes.length == 2 && bytes[0] == 10 && bytes[1] == 10) {
                        continue;
                    }
                    try {
                        org_jboss_resteasy_grpc_runtime_sse___SseEvent sseEvent = org_jboss_resteasy_grpc_runtime_sse___SseEvent.parseFrom(bytes);
                        responseObserver.onNext(sseEvent);
                    } catch (Exception e) {
                        continue;
                    }
                }
            } catch (Exception e) {
                responseObserver.onError(e);
            } finally {
                responseObserver.onCompleted();
                if (requestContextController != null) {
                    requestContextController.deactivate();
                }
                if (tccl != null) {
                    Thread.currentThread().setContextClassLoader(tccl);
                }
            }
        }

These changes are generated automatically, so no intervention is
required. However, the application code on the client side needs to be
adjusted. It could look, for example, something like this:

        Iterator<org_jboss_resteasy_grpc_runtime_sse___SseEvent> response = blockingStub.sseGreet(gem);
        while (response.hasNext()) {
           org_jboss_resteasy_grpc_runtime_sse___SseEvent sseEvent = response.next();
           Any any = sseEvent.getData();
           gString gString = any.unpack(gString.class);
           System.out.println(gString.getValue());
        }

Note, in particular, the treatment of the `data` field. The class
`jakarta.ws.rs.sse.OutboundSseEvent` has a `data` field of type
`java.lang.Object`. The corresponding field in the definition of
`org_jboss_resteasy_grpc_runtime_sse___SseEvent` in Greet.proto has type
`google.protobuf.Any`, which translates to `com.google.protobuf.Any` in
Greet_proto. Note that the method `Any.pack()` has signature

        public static <T extends com.google.protobuf.Message> Any pack(T message, java.lang.String typeUrlPrefix);

so we have to translate the `Object` into a `Message`; The translation
is handled by `GreetJavabufTranslator`, which implies that the type of
the field must be one processed by `JavaToProtobufGenerator`. If it is
not discovered automatically, it can be passed into
`JavaToProtobufGenerator` with the "classes" parameter, described in
[Building the bridge project](#building_bridge).

### Other uses of `google.protobuf.Any`
<a name="other_uses_Any"/>

Consider the resource method

        public Response m() {
           if (test()) {
              return Response.ok(new X()).build();
           } else {
              return Response.ok(new Y()).build();
           }
        }

Will it return an `X` or a `Y`? If `test()` is

        public boolean test() {
           return true;
        }

it's clear that `m()` will return an `X`, and, moreover, that can be
determined statically at compile time. But it's a well known fact in
theoretical computer science, first proved by Alan Turing \[see, for
example, <https://en.wikipedia.org/wiki/Halting_problem>\], that not all
questions can be answered algorithmically.

We can't tell if `m()` returns an `X` or a `Y`, but we know it returns
an `Object`. This is another case in which the protobuf type
`google.protobuf.Any` is useful.

Suppose we add the resource method

        @GET
        @Path("greet/response")
        public Response response(String name) {
           return Response.ok("hello " + name).build();
        }

to `org.greet.Greeter`. Then the oneof field of `GeneralReturnMessage`
becomes

        oneof messageType {
            org_greet___Greeting org_greet___Greeting_field = 44;
            org_greet___GeneralGreeting org_greet___GeneralGreeting_field = 45;
            google.protobuf.Any google_protobuf_Any_field = 46;
        }

augmented by the `google_protobuf_Any_field` field.

Together, `GreetMessageBodyReaderWriter` and `GreetJavabufTranslator`
turn `"hello " + name` in the `Response` into a `gString`
and then pack it into an `Any`. Then we might have the following on the
client side:

        GeneralEntityMessage.Builder messageBuilder = GeneralEntityMessage.newBuilder();
        gString gs = gString.newBuilder().setValue("Bill").build();
        GeneralEntityMessage gem = messageBuilder.setGStringField(gs).build();
        try {
           GeneralReturnMessage grm = blockingStub.response(gem);
           Any any = grm.getGoogleProtobufAnyField();
           System.out.println(any.unpack(gString.class));
        } catch (StatusRuntimeException e) {
           //
        } 

Another case in which we can't statically determine the return type is
when an asynchronous resource method uses the `@Suspended` annotation:

        @GET
        @Path("suspend")
        public void suspend(@Suspended final AsyncResponse response) {
           Thread t = new Thread() {
           @Override
              public void run() {
                 try {
                    response.resume("suspend");
                 } catch (Exception e) {
                    response.resume(e);
                 }
              }
           };
           t.start();
        }

Similar unpacking would happen on the client side.

## Automating the generation of the intermediary classes

### Building the bridge project
<a name="building_bridge"/>

There are a lot of moving parts in the generation of the classes that
make up the gRPC to Jakarta REST intermediary layer, so we've gathered
the details together into a maven archetype in the
**gRPCtoJakartaREST-archetype** github project (
<https://github.com/resteasy/gRPCtoJakartaREST-archetype>).

gRPCtoJakartaREST-archetype starts with a Jakarta REST maven project,
called the **target project**, and creates a **bridge project**, which
extends the target project with additional classes that form an
intermediate layer that liaises between the gRPC world and the Jakarta
REST world. Note that the bridge project can function as a Jakarta REST
project, and so it can replace the target project.

To begin, gRPCtoJakartaREST-archetype generates a mostly empty bridge
project, consisting mainly of a pom.xml file that can build the
intermediary classes and generate a WAR. It assumes that the target
project's source JAR is available in an accessible repository. In this
example, we assume the existence of org.greet:greet:0.0.1-SNAPSHOT. To
generate the initial state of the bridge project, run

        mvn archetype:generate -B \
           -DarchetypeGroupId=dev.resteasy.grpc \
           -DarchetypeArtifactId=gRPCtoJakartaREST-archetype \
           -DarchetypeVersion=1.0.0.Alpha1 \
           -DgroupId=org.greet \
           -DartifactId=greet \
           -Dversion=0.0.1-SNAPSHOT \
           -Dgenerate-prefix=Greet \
           -Dgenerate-package=org.greet \
           -Dresteasy-version=6.2.4.Final \
           -Dgrpc-bridge-version=1.0.0.Alpha1

The following parameters need to be supplied:

-   archetypeGroupId: gRPCtoJakartaREST-archetype's groupId
-   archetypeArtifactId: gRPCtoJakartaREST-archetype's archetypeId
-   archetypeVersion: gRPCtoJakartaREST-archetype's version
-   groupId: groupId of the target project
-   artifactId: artifactId of the target project
-   version: version of the target project
-   generate-prefix: the prefix for generated classes
-   generate-package: Java package for generated classes
-   resteasy-version: version of RESTEasy to use
-   grpc-bridge-version: version of resteasy-grpc to use

The values of archetypeVersion, resteasy-version, and
grpc-bridge-version, of course, are subject to change.

The result of running the archetype is a new project with GAV
groupId:artifactId.grpc:version. For example, from target project
org.greet:greet:0.0.1-SNAPSHOT we will get bridge project
org.greet:greet.grpc:0.0.1-SNAPSHOT. At this point, the layout of the
new project is

        +- pom.xml
        +- src/main/webapp
        |  +- META-INF
        |  |  +- beans.xml
        |  +- WEB-INF
        |     +- web.xml
        +- src/main/resources
        |  +- buildjar
        |  +- deployjar

The most important file is pom.xml, which describes the sequence of
events necessary for generating a WAR with the contents of the target
project plus the intermediary layer. The other files are

-   beans.xml: empty file
-   web.xml: implements the Jakarta REST provider removal described
    above (See [Translating Java classes](#translating_java_classes).)
-   buildjar: a bash script that produces a JAR file
-   deployjar: a bash script that deploys the JAR built by buildjar to a
    maven repository (See ([Output products](#output_products).)

Once the new project is created, the pom.xml can be used to copy the
Java classes from the target project and generate the intermediary
classes:

        mvn clean install 

There are also some optional parameters:

-   classes: additional classes not detected by syntactic scanning
-   release.type: deploy as a snapshot or otherwise. Defaults to
    "snapshot". (See ([Output products](#output_products).)
-   inWildFly: the generated WAR will be run in WildFly. Defaults to
    "true". (See [Output products](#output_products).)

The syntax for the "classes" parameter is

              (DIR ":" CLASSNAME) ("," DIR ":" CLASSNAME)*
       

where

-   DIR: directory of the class's source
-   CLASSNAME: fully qualified name of the class

For example,

        mvn -Dclasses=/home/bob/greet/src/java/main:org.greet.Extra clean install 

When the project is built, the layout is as follows:

        +- pom.xml
        +- src/main/java
        |  +- org.greet
        |     +- GeneralGreeting.java
        |     +  Greeter.java
        |     +  Greeting.java
        +- src/main/proto
        |  +- Greet.proto
        +- src/main/webapp
        |  +- META-INF
        |  |  +- beans.xml
        |  +- WEB-INF
        |     +- web.xml
        +- src/main/resources
        |  +- buildjar
        |  +- deployjar
        +- target/generated-sources/protobuf
        |  +- java
        |     +- org.greet
        |     |  +- Greet_proto.java
        |  +- grpc-java
        |     +- org.greet
        |        +- Greet_Server.java 
        |        +- GreetJavabufTranslator.java
        |        +- GreetMessageBodyReaderWriter.java
        |        +- GreetServiceGrpc.java
        |        +- GreetServiceGrpcImpl.java
        |  +- greet.grpc-0.0.1-SNAPSHOT.jar
        |  +- greet.grpc-0.0.1-SNAPSHOT.war
        |  +- greet.grpc-0.0.1-SNAPSHOT-sources.jar

**Notes**

-   The intermediary layer classes discussed above are in
    target/generated-sources/protobuf/grpc-java.
-   We'll discuss `Greet_Server` below in [Using the generated WAR](#using_war)
-   For any of the files \<prefix\>.proto,
    \<prefix\>JavabufTranslator.java,
    \<prefix\>MessageBodyReaderWriter.java, or
    \<prefix\>ServiceGrpcImpl.java, if the file exists, the build
    process will not overwrite it. This makes it possible to build a
    file and then tweak it for subsequent builds.

### Output products
<a name="output_products"/>

The packaging type of the project created by the archetype is "war",
so, in the example, mvn install creates greet.grpc-0.0.1-SNAPSHOT.war.
The project also uses the src/main/resources/buildjar bash script to
create greet.grpc-0.0.1-SNAPSHOT.jar with all of the compiled classes.

Going a step further, mvn deploy can deploy the WAR and JAR (where the
deployjar bash script manages the latter) to a remote repository. Note
that deployjar hard codes the JBoss repositories as follows:

        if [ ${RELEASE_TYPE} == "snapshot" ]; then
           URL=https://repository.jboss.org/nexus/content/repositories/snapshots/
        else
           URL=https://repository.jboss.org/nexus/service/local/staging/deploy/maven2/
        fi

These presumably need to be modified. Maven repository configuration is
beyond the scope of this document.

Depending on the environment to which it will be deployed, the WAR's
WEB-INF/lib directory can contain only grpc-bridge-runtime-1.0.0.Alpha1.jar
or it can be populated with all of the protobuf, gRPC,
and other JARs necessary for the intermediary classes to run. For
example, if the WAR is to be deployed to an instance of WildFly running
with the gRPC subsystem
<https://github.com/wildfly-extras/wildfly-grpc-feature-pack>, then it
should be built with parameter "inWildFly" set to "true" (or
anything other than "false"), which will result in a WEB/lib
directory with just grpc-bridge-runtime-1.0.0.Alpha1.jar. Setting it to "false"
will populate WEB-INF/lib appropriately.

## Using the generated WAR
<a name="using_war"/>

If the WAR is deployed to an instance of WildFly running with the grpc
subsystem <https://github.com/wildfly-extras/wildfly-grpc-feature-pack>,
then `GreetServiceGrpcImpl` will be recognized and registered with the
gRPC runtime.

Once `GreetServiceGrpcImpl` is registered, a gRPC client can send
requests, but there's one more thing to do to set up the intermediary
layer. The overriding methods in `GreetServiceGrpcImpl` need to be able
to dispatch the request to the appropriate servlet. A Jakarta REST
request to `dev.resteasy.grpc.server.Greet_Server` in the bridge
project's target/generated-sources/protobuf/grpc-java directory will
cause the handling servlet to be stored by `GrpcHttpServletDispatcher`
so that it can be retrieved by the intermediary code for subsequent gRPC
requests. Moreover, calling `Greet_Server.startContext()` in particular
will accomplish the other initial requirement, which is storing a
reference to the servlet's `jakarta.servlet.ServletContext`. For
example,

        curl http://localhost:8080/greet.grpc-0.0.1-SNAPSHOT/grpcToJakartaRest/grpcserver/context

Alternatively, if the generated WAR is not running in an instance of
WildFly with the grpc subsystem,

        curl http://localhost:8080/greet.grpc-0.0.1-SNAPSHOT/grpcToJakartaRest/grpcserver/start

will initiate the gRPC server runtime.

Alternatively, the step can be done programmatically, as in `org.jboss.restesy.test.grpc.AbstractGrpcToJakartaRESTTest`
in the resteasy-grpc-testsuite in resteasy-grpc:

        try (
                Client client = ClientBuilder.newClient();
                var response = client.target("http://localhost:8080/grpc-test/grpcserver/context")
                        .request()
                        .get()) {
            final var message = response.getStatus() + ": " + response.readEntity(String.class);
            Assert.assertEquals(message, response.getStatus(), 200);
        }

By the way, `AbstractGrpcToJakartaRESTTest` has a lot of client side code that might be useful to look at.

## Human intervention
<a name="human_intervention"/>

As much as possible, grpc-bridge and grpc-bridge-runtime automate the
conversion back and forth between the gRPC and Jakarta REST worlds, but
there are some situations in which manual intervention is required, for
one reason or another.

### Bridge project creation time
<a name="creation_time"/>

The bridge project is meant to be an extension of the target project.
Now, the main reason for installing a pom.xml file in the bridge project
is to capture the sequence of events necessary to create the various
classes in the intermediary layer. But it may be necessary to merge into
it pieces of the target project pom.xml, dependencies, for example, in
order to capture the construction of the target project.

One function of the bridge pom.xml is to copy the classes from the
target project. Those are clearly necessary. But there may be other
pieces of the target project like resource files that are also needed.
They could be copied manually, or the bridge pom.xml could be extended.

### Compile time
<a name="compile_time"/>

We have already discussed, in [SSE](#SSE) and [Other uses of google.protobuf.Any](#other_uses_Any),
situations in which it is not possible to determine
statically all classes that are sent over the network. For example, if a
resource method returns `Response`, it may not be possible to determine
the type of the returned entity. That means that, when
`JavaToProtobufGenerator` scans for classes, it may not find all of
those used, in which case the "classes" command line argument,
described in [Building the bridge project](#building_bridge) can be used
to supply those that are not detected.

Also, we mentioned in [Building the bridge project](#building_bridge)
that running maven to build the bridge project results in copying Java
classes from the target project. It should be noted that if other files
are needed, that would need to be handled separately.

### Servlet environment
<a name="servlet_environment"/>

Although the Jakarta REST specification does not mandate its use, a
servlet container is a common environment for running Jakarta REST
applications, and, in that case, the spec mandates the availability by
injection of certain servlet related types:

        The @Context annotation can be used to indicate a dependency on a Servlet-defined
        resource. A Servlet-based implementation MUST support injection of the following
        Servlet-defined types: ServletConfig, ServletContext, HttpServletRequest, and
        HttpServletResponse.

RESTEasy supports servlets, and, accordingly, grpc-bridge creates a
servlet environment for Jakarta REST resources to execute in, including
the four mandated servlet types.

Note that [Using the generated WAR](#using_war) discusses a step
involving a Jakarta REST client call that must be taken before gRPC
calls can be made. It is responsible not only for storing the servlet,
but it also results in storing references to a `ServletContext` and
`ServletConfig` for later use.

The other two injectable classes, `HttpServletRequest` and
`HttpServletResponse`, are supplied by the grpc-bridge runtime.
Normally, those classes would be created by a servlet container which
has an actual HTTP network connection from which information like URLs,
headers, and addresses can be obtained, but for the grpc-bridge runtime,
the HTTP connection is hidden by the gRPC runtime. Some information can
be derived or approximated; for example, in the absence of path
parameters, the path can be derived from the `@Path` annotation(s). In
many cases, though, `HttpServletRequest` relies on the client to spell
out any information needed for a given computation. Recall that
`GeneralEntityMessage` has slots for all kinds of information:

        message GeneralEntityMessage {
           ServletInfo servletInfo = 45;
           string URL = 46;
           map<string, gHeader> headers = 47;
           repeated gCookie cookies = 48;
           string httpMethod = 49;
           oneof messageType {
           ...
           }
        }

Some of these fields, e.g., cookies and headers, are naturally supplied
by the client. On the other hand, the information in

        message ServletInfo {
           string characterEncoding = 39;
           string clientAddress = 40;
           string clientHost = 41;
           int32  clientPort = 42;
        }

which would normally come from the network connection, must be supplied
explicitly as part of the invocation.

