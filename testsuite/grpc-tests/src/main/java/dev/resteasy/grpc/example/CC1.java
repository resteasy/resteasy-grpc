/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2025 Red Hat, Inc., and individual contributors
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
package dev.resteasy.grpc.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.MatrixParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;

import org.jboss.resteasy.core.ResteasyContext;
import org.jboss.resteasy.plugins.providers.sse.OutboundSseEventImpl;

import dev.resteasy.grpc.example.InnerClasses.InnerClassHolder;
import dev.resteasy.grpc.example.InnerClasses.PublicPrivate;
import dev.resteasy.grpc.example.InnerClasses.PublicPublic;

@Path("p")
public class CC1 {

    @SuppressWarnings("rawtypes")
    @Path("set")
    @GET
    public Set set(HashSet set) {
        return set;
    }

    @Path("ready")
    @GET
    public String ready() {
        return "ready";
    }

    @Path("boolean")
    @POST
    public boolean getBoolean(boolean b) {
        return !b;
    }

    @Path("Boolean")
    @POST
    public Boolean getBooleanWrapper(Boolean b) {
        return Boolean.valueOf(!b);
    }

    @Path("byte")
    @POST
    public byte getByte(byte b) {
        int i = b + 1;
        return (byte) i;
    }

    @Path("Byte")
    @POST
    public Byte getByteWrapper(Byte b) {
        return Byte.valueOf((byte) (b.byteValue() + 1));
    }

    @Path("short")
    @POST
    public short getShort(short n) {
        int i = n + 1;
        return (short) i;
    }

    @Path("Short")
    @POST
    public Short getShortWrapper(Short n) {
        return Short.valueOf((short) (n.shortValue() + 1));
    }

    @Path("int")
    @POST
    public int getInt(int n) {
        return n + 1;
    }

    @Path("Integer")
    @POST
    public Integer getInteger(Integer n) {
        return Integer.valueOf(n.intValue() + 1);
    }

    @Path("long")
    @POST
    public long getLong(long n) {
        return n + 1;
    }

    @Path("Long")
    @POST
    public Long getLongWrapper(Long n) {
        return Long.valueOf(n.longValue() + 1);
    }

    @Path("float")
    @POST
    public float getFloat(float f) {
        return Float.valueOf((float) (f + 1.0f));
    }

    @Path("Float")
    @POST
    public Float getFloatWrapper(Float f) {
        return Float.valueOf((float) (f.floatValue() + 1.0f));
    }

    @Path("double")
    @POST
    public double getDouble(double d) {
        return Double.valueOf((double) (d + 1.0d));
    }

    @Path("Double")
    @POST
    public Double getDoubleWrapper(Double d) {
        return Double.valueOf((double) (d.floatValue() + 1.0d));
    }

    @Path("char")
    @POST
    public char getChar(char c) {
        return Character.toUpperCase(c);
    }

    @Path("Character")
    @POST
    public Character getCharacter(Character c) {
        return Character.toUpperCase(c);
    }

    @Path("string")
    @POST
    public String getString(String s) {
        return s.toUpperCase();
    }

    @Path("constructor")
    @GET
    public CC9 constructor() {
        return new CC9(
                true,
                (byte) 1,
                (short) 2,
                3,
                4L,
                5.0f,
                6.0d,
                '7',
                new CC3("eight"));
    }

    @Path("response")
    @GET
    public Response getResponse() {
        CC7 cc7 = new CC7("cc7", 11);
        return Response.ok(cc7).build();
    }

    @Path("async/cs")
    @GET
    public CompletionStage<String> getResponseCompletionStage() {
        final CompletableFuture<String> response = new CompletableFuture<>();
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    response.complete("cs");
                } catch (Exception e) {
                    response.completeExceptionally(e);
                }
            }
        };
        t.start();
        return response;
    }

    @Path("cc7")
    @GET
    public CC7 getCC7() {
        CC7 cc7 = new CC7("cc7", 11);
        return cc7;
    }

    @Path("produces")
    @Produces("abc/xyz")
    @GET
    public String produces() {
        return "produces";
    }

    @Path("consumes")
    @Consumes("xyz/abc")
    @GET
    public String consumes() {
        return "consumes";
    }

    @Path("path/{p1}/param/{p2}")
    @GET
    public String pathParams(@PathParam("p1") String p1, @PathParam("p2") String p2) {
        return "x" + p1 + "y" + p2 + "z";
    }

    @Path("query")
    @GET
    public String queryParams(@QueryParam("q1") String q1, @QueryParam("q2") String q2) {
        return "x" + q1 + "y" + q2 + "z";
    }

    @Path("matrix/more")
    @GET
    public String matrixParams(@MatrixParam("m1") String m1, @MatrixParam("m2") String m2, @MatrixParam("m3") String m3) {
        return "w" + m1 + "x" + m2 + "y" + m3 + "z";
    }

    @Path("cookieParams")
    @GET
    public String cookieParams(@CookieParam("c1") Cookie c1, @CookieParam("c2") Cookie c2) {
        return "x" + cookieStringify(c1) + "y" + cookieStringify(c2) + "z";
    }

    private String cookieStringify(Cookie c) {
        StringBuilder sb = new StringBuilder();
        sb.append(c.getName())
                .append("=")
                .append(c.getValue())
                .append(";")
                .append(c.getDomain()).append(",")
                .append(c.getPath()).append(",")
                .append(c.getVersion());
        return sb.toString();
    }

    @Path("headerParams")
    @GET
    public String headerParams(@HeaderParam("h1") String h1, @HeaderParam("h2") String h2) {
        return "x" + h1 + "y" + h2 + "z";
    }

    @Path("params/{p1}/list/{p1}")
    @GET
    public String paramsList(
            @HeaderParam("h1") final List<String> headerList,
            @MatrixParam("m1") final List<String> matrixList,
            @PathParam("p1") final List<String> pathList,
            @QueryParam("q1") final List<String> queryList) {
        StringBuilder sb = new StringBuilder();
        for (String s : headerList) {
            sb.append(s);
        }
        for (String s : matrixList) {
            sb.append(s);
        }
        for (String s : pathList) {
            sb.append(s);
        }
        for (String s : queryList) {
            sb.append(s);
        }
        return sb.toString();
    }

    @Path("params/{p1}/set/{p1}")
    @GET
    public String paramsSet(
            @HeaderParam("h1") final Set<String> headerSet,
            @MatrixParam("m1") final Set<String> matrixSet,
            @PathParam("p1") final Set<String> pathSet,
            @QueryParam("q1") final Set<String> querySet) {
        List<String> headerList = new ArrayList<String>(headerSet);
        List<String> matrixList = new ArrayList<String>(matrixSet);
        List<String> pathList = new ArrayList<String>(pathSet);
        List<String> queryList = new ArrayList<String>(querySet);
        Collections.sort(headerList);
        Collections.sort(matrixList);
        Collections.sort(pathList);
        Collections.sort(queryList);
        StringBuilder sb = new StringBuilder();
        for (String s : headerList) {
            sb.append(s);
        }
        for (String s : matrixList) {
            sb.append(s);
        }
        for (String s : pathList) {
            sb.append(s);
        }
        for (String s : queryList) {
            sb.append(s);
        }
        return sb.toString();
    }

    @Path("params/{p1}/sortedset/{p1}")
    @GET
    public String paramsSortedSet(
            @HeaderParam("h1") final SortedSet<String> headerSet,
            @MatrixParam("m1") final SortedSet<String> matrixSet,
            @PathParam("p1") final SortedSet<String> pathSet,
            @QueryParam("q1") final SortedSet<String> querySet) {
        StringBuilder sb = new StringBuilder();
        for (String s : headerSet) {
            sb.append(s);
        }
        for (String s : matrixSet) {
            sb.append(s);
        }
        for (String s : pathSet) {
            sb.append(s);
        }
        for (String s : querySet) {
            sb.append(s);
        }
        return sb.toString();
    }

    @GET
    @Path("suspend")
    public void suspend(@Suspended final AsyncResponse response) {
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    Response jaxrs = Response.ok("suspend").build();
                    response.resume(jaxrs);
                } catch (Exception e) {
                    response.resume(e);
                }
            }
        };
        t.start();
    }

    @GET
    @Path("contextPath")
    public String contextPath(@Context HttpServletRequest request) {
        String contextPath = request.getServletContext().getContextPath();
        return contextPath;
    }

    @Path("inheritance")
    @POST
    public CC2 inheritance(CC2 cc2) {
        return new CC2("x" + cc2.s + "y", cc2.j + 1);
    }

    @Path("reference")
    @POST
    public CC4 referenceField(CC4 cc4) {
        CC5 newCC5 = new CC5(cc4.cc5.k + 1);
        CC4 newCC4 = new CC4("x" + cc4.s + "y", newCC5);
        //            CC4 newCC4 = new CC4(cc4.s + 1, newCC5);
        return newCC4;
    }

    String m2(String s) {
        return "x";
    }

    @Path("m3")
    @POST
    public String m3(CC4 cc4) {
        return "x";
    }

    @Path("m4")
    @POST
    public boolean m4(int i) {
        return true;
    }

    @Path("m5")
    @GET
    public String m5() {
        return "m5";
    }

    @Path("m7")
    @POST
    public CC6 m7(int i) {
        CC7 cc7 = new CC7("m7", i + 1);
        CC6 cc6 = new CC6(i + 2, cc7);
        return cc6;
    }

    @Path("servletInfo")
    @POST
    public String testServletInfo(@Context HttpServletRequest request) {
        String characterEncoding = request.getCharacterEncoding().toUpperCase();
        String remoteAddr = request.getRemoteAddr();
        remoteAddr = remoteAddr.substring(0, remoteAddr.lastIndexOf(".") + 1) + "5";
        String remoteHost = request.getRemoteHost().toUpperCase();
        int remotePort = request.getRemotePort() + 1;
        return characterEncoding + "|" + remoteAddr + "|" + remoteHost + "|" + remotePort;
    }

    /**
     * Clarify definition of cookies
     */
    @Path("server/cookies")
    @GET
    public String serverCookies(@Context HttpServletResponse response) {
        jakarta.servlet.http.Cookie c1 = new jakarta.servlet.http.Cookie("n1", "v1");
        c1.setComment("c1");
        c1.setDomain("d1");
        c1.setPath("p1");
        c1.setVersion(0);
        response.addCookie(c1);
        jakarta.servlet.http.Cookie c2 = new jakarta.servlet.http.Cookie("n2", "v2");
        c2.setComment("c2");
        c2.setDomain("d2");
        c2.setMaxAge(17);
        c2.setPath("p2");
        c2.setVersion(0);
        c2.setHttpOnly(true);
        c2.setSecure(true);
        response.addCookie(c2);
        return "cookies";
    }

    @Path("server/headers")
    @GET
    public String serverHeaders(@Context HttpServletResponse response) {
        response.addHeader("h1", "v1a");
        response.addHeader("h1", "v1b");
        response.addHeader("h2", "v2");
        return "headers";
    }

    @Path("servletPath")
    @GET
    public String servletPath(@Context HttpServletRequest request) {
        return request.getContextPath() + "|" + request.getServletPath() + "|" + request.getPathInfo() + "|"
                + request.getPathTranslated();
    }

    @Path("servletParams")
    @POST
    public String servletParams(@QueryParam("p1") String q1, @QueryParam("p2") String q2,
            @FormParam("p2") String f2, @FormParam("p3") String f3,
            @Context HttpServletRequest request) {
        StringBuilder sb = new StringBuilder(q1 + "|" + q2 + "|" + f2 + "|" + f3 + "|");
        for (Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            sb.append(entry.getKey() + "->");
            for (int i = 0; i < entry.getValue().length; i++) {
                sb.append(entry.getValue()[i]);
            }
            sb.append("|");
        }
        return sb.toString();
    }

    /**
     * Clarify definition of cookies
     */
    @Path("jaxrsResponse")
    @GET
    public Response jaxrsResponse() {
        Response.ResponseBuilder response = Response.ok("jaxrsResponse");
        NewCookie.Builder newCookieBuilder = new NewCookie.Builder("n1");
        NewCookie newCookie = newCookieBuilder.domain("d1").path("p1").value("v1")
                .maxAge(11).expiry(new Date(111111)).secure(false).httpOnly(true).build();
        response.cookie(newCookie);
        newCookieBuilder = new NewCookie.Builder("n2");
        newCookie = newCookieBuilder.value("v2").path("p2").domain("d2")
                .maxAge(17).expiry(new Date(222222)).secure(true).httpOnly(false).build();
        response.cookie(newCookie);
        response.header("h1", "v1");
        response.status(222);
        response.type("x/y");
        return response.build();
    }

    @Path("servletResponse")
    @GET
    public Response servletResponse(@Context HttpServletResponse response) {
        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("n1", "v1");
        cookie.setDomain("d1");
        cookie.setMaxAge(3);
        cookie.setPath("p1");
        cookie.setValue("v1");
        cookie.setVersion(7);
        response.addCookie(cookie);
        response.setContentType("x/y");

        response.addDateHeader("d1", 1000 * 60 * 60 * 24 * 1);
        response.addHeader("h1", "v1");
        response.addIntHeader("i1", 13);

        response.setDateHeader("d2", 1000 * 60 * 60 * 24 * 2);
        response.setHeader("h2", "v2a");
        response.setIntHeader("i2", 19);

        response.addDateHeader("d2", 1000 * 60 * 60 * 24 * 3);
        response.addHeader("h2", "v2b");
        response.addIntHeader("i2", 29);

        response.addDateHeader("d3", 1000 * 60 * 60 * 24 * 4);
        response.addHeader("h3", "v3a");
        response.addIntHeader("i3", 37);
        response.setDateHeader("d3", 1000 * 60 * 60 * 24 * 5);
        response.setHeader("h3", "v3b");
        response.setIntHeader("i3", 41);

        response.setStatus(222);
        return Response.ok("servletResponse").status(223).build();
    }

    public static class InnerClass {
        private int i = 3;
        private String s = "three";

        public int getI() {
            return i;
        }

        public void setI(int i) {
            this.i = i;
        }

        public String getS() {
            return s;
        }

        public void setS(String s) {
            this.s = s;
        }
    }

    @Path("inner")
    @GET
    public InnerClass inner() {
        return new InnerClass();
    }

    @GET
    public String notSubresourceGet() {
        return "notSubresourceGet";
    }

    @POST
    public String notSubresourcePost() {
        return "notSubresourcePost";
    }

    @Path("locator")
    public Subresource locator(@Context UriInfo uriInfo) {
        return new Subresource(uriInfo);
    }

    public static class Subresource {
        private String path;

        public Subresource() {
        }

        public Subresource(UriInfo uriInfo) {
            this.path = uriInfo.getPath();
        }

        @GET
        @Path("get")
        public String get() {
            return path;
        }

        @POST
        @Path("post/{p}")
        public String post(@PathParam("p") String p, String entity) {
            return p + "|" + entity;
        }
    }

    @Path("servletContext")
    @GET
    public String servletContext(@Context ServletContext servletContext) {
        return servletContext.getInitParameter("resteasy.servlet.mapping.prefix");
    }

    @Path("servletConfig")
    @GET
    public String servletConfig(@Context ServletConfig servletConfig) {
        return servletConfig.getServletName();
    }

    @GET
    @Path("sse")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void sse(@Context SseEventSink eventSink, @Context Sse sse) {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        final Map<Class<?>, Object> map = ResteasyContext.getContextDataMap();
        executor.execute(() -> {
            ResteasyContext.addCloseableContextDataLevel(map);
            try (SseEventSink sink = eventSink) {
                eventSink.send(sse.newEvent("name1", "event1"));
                eventSink.send(sse.newEvent("name2", "event2"));
                eventSink.send(sse.newEvent("name3", "event3"));
                OutboundSseEventImpl.BuilderImpl builder = new OutboundSseEventImpl.BuilderImpl();
                builder.name("name4").data(new CC5(4));
                eventSink.send(builder.build());
            }
        });
    }

    @GET
    @Path("copy")
    public String copy(String s) {
        return s;
    }

    @GET
    @Path("interface/impl")
    public void intfImpl(IntfImpl implIntf) {
        // not used: includes IntfImpl in .proto file
    }

    @GET
    @Path("interface/entity")
    public String intfEntity(Intf intf) {
        return intf.getS();
    }

    @GET
    @Path("interface/return")
    public Intf intfReturn() {
        Intf intf = new IntfImpl();
        intf.setS("xyz");
        return intf;
    }

    @POST
    @Path("inner/public/public")
    public PublicPublic publicPublic(PublicPublic pp) {
        return pp;
    }

    @POST
    @Path("inner/public/private")
    public PublicPrivate publicPrivate(PublicPrivate pp) {
        return pp;
    }

    @POST
    @Path("inner/holder")
    public InnerClassHolder innerClassHolder(InnerClassHolder ich) {
        return ich;
    }
}
