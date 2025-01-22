package dev.resteasy.grpc.bridge.generator.protobuf;

import static dev.resteasy.grpc.bridge.runtime.Constants.ANY;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.jboss.logging.Logger;

import com.github.javaparser.ParseResult;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.AccessSpecifier;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration;
import com.github.javaparser.resolution.types.ResolvedArrayType;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.model.typesystem.ReferenceTypeImpl;
import com.github.javaparser.symbolsolver.reflectionmodel.ReflectionClassDeclaration;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.utils.Log;
import com.github.javaparser.utils.Pair;
import com.github.javaparser.utils.SourceRoot;

import dev.resteasy.grpc.bridge.runtime.servlet.HttpServletRequestImpl;

/**
 * Traverses a set of Jakarta REST resources and creates a protobuf representation.
 * <p/>
 * <ol>
 * <li>Find all Jakarta REST resource methods and resource locators and create an rpc entry for each</li>
 * <li>Find the transitive closure of the classes mentioned in the resource methods and locators
 * and create a message entry for each.</li>
 * </ol>
 * <p/>
 * </pre>
 * For example,
 * <p/>
 *
 * <pre>
 * public class CC1 {
 *
 *     &#064;Path("m1")
 *     &#064;GET
 *     String m1(CC2 cc2) {
 *         return "x";
 *     }
 *
 *     String m2(String s) {
 *         return "x";
 *     }
 *
 *     &#064;Path("m3")
 *     &#064;GET
 *     String m3(CC4 cc4) {
 *         return "x";
 *     }
 * }
 * </pre>
 *
 * together with the class definitions
 * <p/>
 *
 * <pre>
 * package io.grpc.examples;
 *
 * public class CC2 extends CC3 {
 *    int j;
 *
 *    public CC2(String s, int j) {
 *       super(s);
 *       this.j = j;
 *    }
 *
 *    public CC2() {}
 * }
 *
 * ========================
 * package io.grpc.examples;
 *
 * public class CC3 {
 *    String s;
 *
 *    public CC3(String s) {
 *       this.s = s;
 *    }
 *
 *    public CC3() {}
 * }
 *
 * ========================
 * package io.grpc.examples;
 *
 * public class CC4 {
 *    private String s;
 *    private CC5 cc5;
 *
 *    public CC4(String s, CC5 cc5) {
 *       this.s = s;
 *       this.cc5 = cc5;
 *    }
 *
 *    public CC4() {}
 * }
 *
 * ========================
 * package io.grpc.examples;
 *
 * public class CC5 {
 *    int k;
 *
 *    public CC5(int k) {
 *       this.k = k;
 *    }
 *
 *    public CC5() {}
 * }
 * </pre>
 *
 * is translated to CC1.proto:
 * <p/>
 *
 * <pre>
 * syntax = "proto3";
 * package io.grpc.examples;
 * option java_package = "io.grpc.examples";
 * option java_outer_classname = "CC1_proto";
 *
 * service CC1Service {
 *    rpc m1 (GeneralEntityMessage) returns (GeneralReturnMessage);
 *    rpc m3 (GeneralEntityMessage) returns (GeneralReturnMessage);
 * }
 *
 * message io_grpc_examples___CC2 {
 *    int32 j = 1;
 *    io_grpc_examples___CC3 cC3___super = 2;
 * }
 *
 * message io_grpc_examples___CC4 {
 *    string s = 1;
 *    io_grpc_examples___CC5 cc5 = 2;
 * }
 *
 * message io_grpc_examples___CC3 {
 *    string s = 1;
 * }
 *
 * message io_grpc_examples___CC5 {
 *    int32 k = 1;
 * }
 *
 * ...
 *
 * message GeneralEntityMessage {
 *    ServletInfo servletInfo = 1;
 *    string URL = 2;
 *    map&lt;string, gHeader&gt; headers = 3;
 *    repeated gCookie cookies = 4;
 *    string httpMethod = 5;
 *    oneof messageType {
 *       io_grpc_examples___CC4 io_grpc_examples___CC4_field = 6;
 *       io_grpc_examples___CC2 io_grpc_examples___CC2_field = 7;
 *       FormMap form_field = 8;
 *    }
 * }
 *
 * message GeneralReturnMessage {
 *    map<string, gHeader> headers = 1;
 *    repeated gNewCookie cookies = 2;
 *    gInteger status = 3;
 *    oneof messageType {
 *       gString gString_field = 4;
 *    }
 * }
 * </pre>
 * <p/>
 * <b>Notes.</b>
 * <ol>
 * <li>{@code CC1.m2()} is not a resource method, so it does not appear in CC1.proto.
 * <li>Protobuf syntax does not support inheritance, so {@code JavaToProtobufGenerator}
 * treats a superclass as a special field. For example, {@code CC2} is a subclass of {@code CC3},
 * so each instance of {@code CC2} has a field named {@code cC3___super} of {@code type io_grpc_examples___CC3}.
 * <li>{@code GeneralEntityMessage} and {@code GeneralReturnMessage} are general purpose classes for conveying
 * entity parameters to the server and responses back to the client. They are defined to hold all possible entity
 * and return types plus a variety of additional fields. For more information, see the User Guide.
 * </ol>
 */
public class JavaToProtobufGenerator {

    private static final Logger logger = Logger.getLogger(JavaToProtobufGenerator.class);
    private static final String LS = System.lineSeparator();

    private static String[] args;
    private static Map<String, String> TYPE_MAP = new HashMap<String, String>();
    private static Map<String, String> PRIMITIVE_WRAPPER_TYPES_FIELD = new HashMap<String, String>();
    private static Map<String, String> PRIMITIVE_WRAPPER_TYPES_IO = new HashMap<String, String>();
    private static Map<String, String> PRIMITIVE_WRAPPER_DEFINITIONS = new HashMap<String, String>();
    private static Map<String, String> PRIMITIVE_TYPES = new HashMap<String, String>();
    private static Set<String> ANNOTATIONS = new HashSet<String>();
    private static Set<String> HTTP_VERBS = new HashSet<String>();
    private static String prefix;
    private static boolean needEmpty = false;

    private static Set<ResolvedType> pendingTypes = ConcurrentHashMap.newKeySet();
    private static Set<String> entityMessageTypes = new HashSet<String>();
    private static Set<String> returnMessageTypes = new HashSet<String>();
    private static Set<String> jars;
    private static Set<String> additionalClasses;// = new CopyOnWriteArraySet<String>();
    private static Set<String> visited = new HashSet<String>();
    private static JavaSymbolSolver symbolSolver;
    private static CombinedTypeSolver combinedTypeSolver;
    private static ReferenceTypeImpl objectType;
    private static ClassVisitor classVisitor = new ClassVisitor();
    private static JakartaRESTResourceVisitor jakartaRESTResourceVisitor = new JakartaRESTResourceVisitor();
    private static boolean started = false;
    private static int counter = 1;
    private static boolean isSSE;
    private static String SSE_EVENT_CLASSNAME = "dev_resteasy_grpc_bridge_runtime_sse___SseEvent";
    private static Map<String, String> basicRepeatedTypes = new HashMap<String, String>();
    private static Map<String, String> basicRepeatedEntityTypes = new HashMap<String, String>();

    private static SortedSet<String> repeatedTypes = new TreeSet<String>();
    private static Map<String, String> REPEAT_MAP = new HashMap<String, String>();
    private static Map<String, String> WRAPPER_TO_JAVABUF_MAP = new HashMap<String, String>();
    private static Map<String, String> PRIMITIVE_ARRAY_TYPE = new HashMap<String, String>();
    private static Set<String> entityTypes = new HashSet<String>();
    private static Set<String> entityTypesForFile = new HashSet<String>();
    private static AtomicInteger classnameCounter = new AtomicInteger();
    private static Map<String, String> classnames = new ConcurrentHashMap<String, String>();
    private static Set<String> rpcNames = new HashSet<String>();
    private static Map<ResolvedReferenceType, ResolvedReferenceType> objectifiedTypes = new ConcurrentHashMap<ResolvedReferenceType, ResolvedReferenceType>();
    private static Map<String, String> classnameMap = new ConcurrentHashMap<String, String>();

    private static String arrayDef = "///////////////%n"
            + "message %1$s {%n"
            + "   repeated %2$s___wrapper wrapper___field = 1;%n"
            + "}%n%n"
            + "message %2$s___wrapper {%n"
            + "   oneof type {%n"
            + "      dev.resteasy.grpc.arrays.dev_resteasy_grpc_arrays___NONE none_field = 1;%n"
            + "      %2$s %2$s_field = 2;%n"
            + "   }%n"
            + "}%n%n";

    private static String arrayHolderDef = "///////////////%n"
            + "message dev_resteasy_grpc_arrays___ArrayHolder___wrapper {%n"
            + "   oneof type {%n"
            + "      dev.resteasy.grpc.arrays.dev_resteasy_grpc_arrays___NONE none_field = 1;%n"
            + "      dev_resteasy_grpc_arrays___ArrayHolder dev_resteasy_grpc_arrays___ArrayHolder_field = 2;%n"
            + "   }%n"
            + "}%n%n"
            + "message dev_resteasy_grpc_arrays___ArrayHolder___WArray {%n"
            + "   string componentType = 1;%n"
            + "   repeated dev_resteasy_grpc_arrays___ArrayHolder___wrapper wrapper___field = 2;%n"
            + "}%n%n";

    static {
        TYPE_MAP.put("boolean", "bool");
        TYPE_MAP.put("byte", "int32");
        TYPE_MAP.put("short", "int32");
        TYPE_MAP.put("int", "int32");
        TYPE_MAP.put("long", "int64");
        TYPE_MAP.put("float", "float");
        TYPE_MAP.put("double", "double");
        TYPE_MAP.put("boolean", "bool");
        TYPE_MAP.put("char", "string");
        TYPE_MAP.put("String", "string");
        TYPE_MAP.put("java.lang.String", "string");

        REPEAT_MAP.put("bool", "dev.resteasy.grpc.arrays.dev_resteasy_grpc_arrays___Boolean___Array");
        REPEAT_MAP.put("bytes", "dev.resteasy.grpc.arrays.dev_resteasy_grpc_arrays___Byte___Array");
        REPEAT_MAP.put("int32", "dev.resteasy.grpc.arrays.dev_resteasy_grpc_arrays___Integer___Array");
        REPEAT_MAP.put("int64", "dev.resteasy.grpc.arrays.dev_resteasy_grpc_arrays___Long___Array");
        REPEAT_MAP.put("float", "dev.resteasy.grpc.arrays.dev_resteasy_grpc_arrays___Float___Array");
        REPEAT_MAP.put("double", "dev.resteasy.grpc.arrays.dev_resteasy_grpc_arrays___Double___Array");
        REPEAT_MAP.put("char", "dev.resteasy.grpc.arrays.dev_resteasy_grpc_arrays___Character___Array");
        REPEAT_MAP.put("string", "dev.resteasy.grpc.arrays.dev_resteasy_grpc_arrays___String___WArray");

        WRAPPER_TO_JAVABUF_MAP.put("java.lang.Boolean", "dev.resteasy.grpc.arrays.dev_resteasy_grpc_arrays___Boolean___WArray");
        WRAPPER_TO_JAVABUF_MAP.put("java.lang.Byte", "dev.resteasy.grpc.arrays.dev_resteasy_grpc_arrays___Byte___WArray");
        WRAPPER_TO_JAVABUF_MAP.put("java.lang.Short", "dev.resteasy.grpc.arrays.dev_resteasy_grpc_arrays___Short___WArray");
        WRAPPER_TO_JAVABUF_MAP.put("java.lang.Integer", "dev.resteasy.grpc.arrays.dev_resteasy_grpc_arrays___Integer___WArray");
        WRAPPER_TO_JAVABUF_MAP.put("java.lang.Long", "dev.resteasy.grpc.arrays.dev_resteasy_grpc_arrays___Long___WArray");
        WRAPPER_TO_JAVABUF_MAP.put("java.lang.Float", "dev.resteasy.grpc.arrays.dev_resteasy_grpc_arrays___Float___WArray");
        WRAPPER_TO_JAVABUF_MAP.put("java.lang.Double", "dev.resteasy.grpc.arrays.dev_resteasy_grpc_arrays___Double___WArray");
        WRAPPER_TO_JAVABUF_MAP.put("java.lang.Character",
                "dev.resteasy.grpc.arrays.dev_resteasy_grpc_arrays___Character___WArray");
        WRAPPER_TO_JAVABUF_MAP.put("java.lang.String", "dev.resteasy.grpc.arrays.dev_resteasy_grpc_arrays___String___WArray");

        PRIMITIVE_WRAPPER_TYPES_IO.put("boolean", "gBoolean");
        PRIMITIVE_WRAPPER_TYPES_IO.put("Boolean", "gBoolean");
        PRIMITIVE_WRAPPER_TYPES_IO.put("java.lang.Boolean", "gBoolean");
        PRIMITIVE_WRAPPER_TYPES_IO.put("byte", "gByte");
        PRIMITIVE_WRAPPER_TYPES_IO.put("Byte", "gByte");
        PRIMITIVE_WRAPPER_TYPES_IO.put("java.lang.Byte", "gByte");
        PRIMITIVE_WRAPPER_TYPES_IO.put("short", "gShort");
        PRIMITIVE_WRAPPER_TYPES_IO.put("Short", "gShort");
        PRIMITIVE_WRAPPER_TYPES_IO.put("java.lang.Short", "gShort");
        PRIMITIVE_WRAPPER_TYPES_IO.put("int", "gInteger");
        PRIMITIVE_WRAPPER_TYPES_IO.put("Integer", "gInteger");
        PRIMITIVE_WRAPPER_TYPES_IO.put("java.lang.Integer", "gInteger");
        PRIMITIVE_WRAPPER_TYPES_IO.put("long", "gLong");
        PRIMITIVE_WRAPPER_TYPES_IO.put("Long", "gLong");
        PRIMITIVE_WRAPPER_TYPES_IO.put("java.lang.Long", "gLong");
        PRIMITIVE_WRAPPER_TYPES_IO.put("float", "gFloat");
        PRIMITIVE_WRAPPER_TYPES_IO.put("Float", "gFloat");
        PRIMITIVE_WRAPPER_TYPES_IO.put("java.lang.Float", "gFloat");
        PRIMITIVE_WRAPPER_TYPES_IO.put("double", "gDouble");
        PRIMITIVE_WRAPPER_TYPES_IO.put("Double", "gDouble");
        PRIMITIVE_WRAPPER_TYPES_IO.put("java.lang.Double", "gDouble");
        PRIMITIVE_WRAPPER_TYPES_IO.put("char", "gCharacter");
        PRIMITIVE_WRAPPER_TYPES_IO.put("Character", "gCharacter");
        PRIMITIVE_WRAPPER_TYPES_IO.put("java.lang.Character", "gCharacter");
        PRIMITIVE_WRAPPER_TYPES_IO.put("string", "gString");
        PRIMITIVE_WRAPPER_TYPES_IO.put("String", "gString");
        PRIMITIVE_WRAPPER_TYPES_IO.put("java.lang.String", "gString");

        PRIMITIVE_WRAPPER_TYPES_FIELD.put("Boolean", "bool");
        PRIMITIVE_WRAPPER_TYPES_FIELD.put("Byte", "int32");
        PRIMITIVE_WRAPPER_TYPES_FIELD.put("Short", "int32");
        PRIMITIVE_WRAPPER_TYPES_FIELD.put("Integer", "int32");
        PRIMITIVE_WRAPPER_TYPES_FIELD.put("Long", "int64");
        PRIMITIVE_WRAPPER_TYPES_FIELD.put("Float", "float");
        PRIMITIVE_WRAPPER_TYPES_FIELD.put("Double", "double");
        PRIMITIVE_WRAPPER_TYPES_FIELD.put("Character", "int32");
        PRIMITIVE_WRAPPER_TYPES_FIELD.put("String", "string");
        PRIMITIVE_WRAPPER_TYPES_FIELD.put("java.lang.Boolean", "bool");
        PRIMITIVE_WRAPPER_TYPES_FIELD.put("java.lang.Byte", "int32");
        PRIMITIVE_WRAPPER_TYPES_FIELD.put("java.lang.Short", "int32");
        PRIMITIVE_WRAPPER_TYPES_FIELD.put("java.lang.Integer", "int32");
        PRIMITIVE_WRAPPER_TYPES_FIELD.put("java.lang.Long", "int64");
        PRIMITIVE_WRAPPER_TYPES_FIELD.put("java.lang.Float", "float");
        PRIMITIVE_WRAPPER_TYPES_FIELD.put("java.lang.Double", "double");
        PRIMITIVE_WRAPPER_TYPES_FIELD.put("java.lang.Character", "int32");
        PRIMITIVE_WRAPPER_TYPES_FIELD.put("java.lang.String", "string");

        PRIMITIVE_WRAPPER_DEFINITIONS.put("Boolean", "message gBoolean   {bool   value = $V$;}");
        PRIMITIVE_WRAPPER_DEFINITIONS.put("Byte", "message gByte      {int32  value = $V$;}");
        PRIMITIVE_WRAPPER_DEFINITIONS.put("Short", "message gShort     {int32  value = $V$;}");
        PRIMITIVE_WRAPPER_DEFINITIONS.put("Integer", "message gInteger   {int32  value = $V$;}");
        PRIMITIVE_WRAPPER_DEFINITIONS.put("Long", "message gLong      {int64  value = $V$;}");
        PRIMITIVE_WRAPPER_DEFINITIONS.put("Float", "message gFloat     {float  value = $V$;}");
        PRIMITIVE_WRAPPER_DEFINITIONS.put("Double", "message gDouble    {double value = $V$;}");
        PRIMITIVE_WRAPPER_DEFINITIONS.put("Character", "message gCharacter {string value = $V$;}");
        PRIMITIVE_WRAPPER_DEFINITIONS.put("String", "message gString    {string value = $V$;}");

        ANNOTATIONS.add("Context");
        ANNOTATIONS.add("CookieParam");
        ANNOTATIONS.add("HeaderParam");
        ANNOTATIONS.add("MatrixParam");
        ANNOTATIONS.add("PathParam");
        ANNOTATIONS.add("QueryParam");

        HTTP_VERBS.add("DELETE");
        HTTP_VERBS.add("HEAD");
        HTTP_VERBS.add("GET");
        HTTP_VERBS.add("OPTIONS");
        HTTP_VERBS.add("PATCH");
        HTTP_VERBS.add("POST");
        HTTP_VERBS.add("PUT");

        basicRepeatedTypes.put("none", "dev.resteasy.grpc.arrays.dev_resteasy_grpc_arrays___NONE");
        basicRepeatedTypes.put("boolean", "dev.resteasy.grpc.arrays.dev_resteasy_grpc_arrays___Boolean___Array");
        basicRepeatedTypes.put("java.lang.Boolean", "dev.resteasy.grpc.arrays.dev_resteasy_grpc_arrays___Boolean___WArray");
        basicRepeatedTypes.put("byte", "dev.resteasy.grpc.arrays.dev_resteasy_grpc_arrays___Byte___Array");
        basicRepeatedTypes.put("java.lang.Byte", "dev.resteasy.grpc.arrays.dev_resteasy_grpc_arrays___Byte___WArray");
        basicRepeatedTypes.put("short", "dev.resteasy.grpc.arrays.dev_resteasy_grpc_arrays___Short___Array");
        basicRepeatedTypes.put("java.lang.Short", "dev.resteasy.grpc.arrays.dev_resteasy_grpc_arrays___Short___WArray");
        basicRepeatedTypes.put("int", "dev.resteasy.grpc.arrays.dev_resteasy_grpc_arrays___Integer___Array");
        basicRepeatedTypes.put("java.lang.Integer", "dev.resteasy.grpc.arrays.dev_resteasy_grpc_arrays___Integer___WArray");
        basicRepeatedTypes.put("long", "dev.resteasy.grpc.arrays.dev_resteasy_grpc_arrays___Long___Array");
        basicRepeatedTypes.put("java.lang.Long", "dev.resteasy.grpc.arrays.dev_resteasy_grpc_arrays___Long___WArray");
        basicRepeatedTypes.put("float", "dev.resteasy.grpc.arrays.dev_resteasy_grpc_arrays___Float___Array");
        basicRepeatedTypes.put("java.lang.Float", "dev.resteasy.grpc.arrays.dev_resteasy_grpc_arrays___Float___WArray");
        basicRepeatedTypes.put("double", "dev.resteasy.grpc.arrays.dev_resteasy_grpc_arrays___Double___Array");
        basicRepeatedTypes.put("java.lang.Double", "dev.resteasy.grpc.arrays.dev_resteasy_grpc_arrays___Double___WArray");
        basicRepeatedTypes.put("char", "dev.resteasy.grpc.arrays.dev_resteasy_grpc_arrays___Character___Array");
        basicRepeatedTypes.put("java.lang.character", "dev.resteasy.grpc.arrays.dev_resteasy_grpc_arrays___Character___WArray");
        basicRepeatedTypes.put("java.lang.String", "dev.resteasy.grpc.arrays.dev_resteasy_grpc_arrays___String___WArray");
        basicRepeatedTypes.put("arrayHolder", "dev_resteasy_grpc_arrays___ArrayHolder___WArray");

        basicRepeatedEntityTypes.put("none", "dev.resteasy.grpc.arrays.Array_proto$dev_resteasy_grpc_arrays___NONE");
        basicRepeatedEntityTypes.put("boolean",
                "dev.resteasy.grpc.arrays.Array_proto$dev_resteasy_grpc_arrays___Boolean___Array");
        basicRepeatedEntityTypes.put("java.lang.Boolean",
                "dev.resteasy.grpc.arrays.Array_proto$dev_resteasy_grpc_arrays___Boolean___WArray");
        basicRepeatedEntityTypes.put("byte", "dev.resteasy.grpc.arrays.Array_proto$dev_resteasy_grpc_arrays___Byte___Array");
        basicRepeatedEntityTypes.put("java.lang.Byte",
                "dev.resteasy.grpc.arrays.Array_proto$dev_resteasy_grpc_arrays___Byte___WArray");
        basicRepeatedEntityTypes.put("short", "dev.resteasy.grpc.arrays.Array_proto$dev_resteasy_grpc_arrays___Short___Array");
        basicRepeatedEntityTypes.put("java.lang.Short",
                "dev.resteasy.grpc.arrays.Array_proto$dev_resteasy_grpc_arrays___Short___WArray");
        basicRepeatedEntityTypes.put("int", "dev.resteasy.grpc.arrays.Array_proto$dev_resteasy_grpc_arrays___Integer___Array");
        basicRepeatedEntityTypes.put("java.lang.Integer",
                "dev.resteasy.grpc.arrays.Array_proto$dev_resteasy_grpc_arrays___Integer___WArray");
        basicRepeatedEntityTypes.put("long", "dev.resteasy.grpc.arrays.Array_proto$dev_resteasy_grpc_arrays___Long___Array");
        basicRepeatedEntityTypes.put("java.lang.Long",
                "dev.resteasy.grpc.arrays.Array_proto$dev_resteasy_grpc_arrays___Long___WArray");
        basicRepeatedEntityTypes.put("float", "dev.resteasy.grpc.arrays.Array_proto$dev_resteasy_grpc_arrays___Float___Array");
        basicRepeatedEntityTypes.put("java.lang.Float",
                "dev.resteasy.grpc.arrays.Array_proto$dev_resteasy_grpc_arrays___Float___WArray");
        basicRepeatedEntityTypes.put("double",
                "dev.resteasy.grpc.arrays.Array_proto$dev_resteasy_grpc_arrays___Double___Array");
        basicRepeatedEntityTypes.put("java.lang.Double",
                "dev.resteasy.grpc.arrays.Array_proto$dev_resteasy_grpc_arrays___Double___WArray");
        basicRepeatedEntityTypes.put("char",
                "dev.resteasy.grpc.arrays.Array_proto$dev_resteasy_grpc_arrays___Character___Array");
        basicRepeatedEntityTypes.put("java.lang.character",
                "dev.resteasy.grpc.arrays.Array_proto$dev_resteasy_grpc_arrays___Character___WArray");
        basicRepeatedEntityTypes.put("java.lang.String",
                "dev.resteasy.grpc.arrays.Array_proto$dev_resteasy_grpc_arrays___String___WArray");
        basicRepeatedEntityTypes.put("arrayHolder", "dev_resteasy_grpc_arrays___ArrayHolder___WArray");

        PRIMITIVE_ARRAY_TYPE.put("boolean", "repeated bool");
        PRIMITIVE_ARRAY_TYPE.put("byte", "bytes");
        PRIMITIVE_ARRAY_TYPE.put("short", "repeated int32");
        PRIMITIVE_ARRAY_TYPE.put("int", "repeated int32");
        PRIMITIVE_ARRAY_TYPE.put("long", "repeated int64");
        PRIMITIVE_ARRAY_TYPE.put("float", "repeated float");
        PRIMITIVE_ARRAY_TYPE.put("double", "repeated double");
        PRIMITIVE_ARRAY_TYPE.put("char", "string");

        PRIMITIVE_TYPES.put("boolean", "Z");
        PRIMITIVE_TYPES.put("byte", "B");
        PRIMITIVE_TYPES.put("short", "S");
        PRIMITIVE_TYPES.put("int", "I");
        PRIMITIVE_TYPES.put("long", "L");
        PRIMITIVE_TYPES.put("float", "F");
        PRIMITIVE_TYPES.put("double", "D");
        PRIMITIVE_TYPES.put("char", "C");
    }

    public static void main(String[] args) throws IOException {
        for (int i = 0; i < args.length; i++) {
            logger.debug("arg[" + i + "]: " + args[i]);
        }
        if (args == null || (args.length < 4)) {
            logger.info("need four args");
            logger.info("  arg[0]: root directory");
            logger.info("  arg[1]: package to be used in .proto file");
            logger.info("  arg[2]: java package to be used in .proto file");
            logger.info("  arg[3]: java outer classname to be generated from .proto file");
            logger.info("  -Djars: comma separated of jars [optional]");
            logger.info("  -Dclasses: comma separated of addition classes [optional]");
            return;
        }
        JavaToProtobufGenerator.args = args;
        prefix = args[3];
        logger.debug("starting JavaToProtobufGenerator");
        String s = System.getProperty("jars", "default");
        jars = "default".equals(s) || "".equals(s)
                ? new CopyOnWriteArraySet<String>()
                : new CopyOnWriteArraySet<String>(Arrays.asList(s.split(",")));
        s = System.getProperty("classes", "default");
        additionalClasses = "default".equals(s) || "".equals(s)
                ? new CopyOnWriteArraySet<String>()
                : new CopyOnWriteArraySet<String>(Arrays.asList(s.split(",")));
        logger.debug("additionalClasses: " + additionalClasses);
        StringBuilder sb = new StringBuilder();
        protobufHeader(args, sb);
        new JavaToProtobufGenerator().processClasses(args, sb);
        int i = 0;
        while (!pendingTypes.isEmpty() && i++ < 100) {
            for (ResolvedType r : pendingTypes) {
                if (r.isReferenceType()) {
                    classVisitor.visit(objectify(r.asReferenceType()), sb);
                }
            }
        }
        finishProto(sb);
        writeProtoFile(args, sb);
        createProtobufDirectory(args);
        writeEntityTypesFile(args);
    }

    private static void protobufHeader(String[] args, StringBuilder sb) {
        sb.append("syntax = \"proto3\";" + LS);
        sb.append("package " + args[1].replace('-', '.') + ";" + LS);
        sb.append("import \"google/protobuf/any.proto\";" + LS);
        sb.append("import \"google/protobuf/timestamp.proto\";" + LS);
        sb.append("import \"dev/resteasy/grpc/arrays/arrays.proto\";" + LS);
        sb.append("option java_package = \"" + args[2] + "\";" + LS);
        sb.append("option java_outer_classname = \"" + args[3] + "_proto\";" + LS);
    }

    /**
     * Visit all Jakarta REST resource classes discovered in project's src/main/java
     */
    private void processClasses(String[] args, StringBuilder sb) throws IOException {
        Log.setAdapter(new Log.StandardOutStandardErrorAdapter());

        // SourceRoot is a tool that read and writes Java files from packages on a certain root directory.
        Path path = Path.of(args[0], "/src/main/java/");
        SourceRoot sourceRoot = new SourceRoot(path);
        TypeSolver reflectionTypeSolver = new ReflectionTypeSolver();
        TypeSolver javaParserTypeSolver = new JavaParserTypeSolver(path);
        combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(reflectionTypeSolver);
        combinedTypeSolver.add(javaParserTypeSolver);
        for (Iterator<String> it = jars.iterator(); it.hasNext();) {
            String s = it.next();
            combinedTypeSolver.add(new JarTypeSolver(s));
        }
        objectType = new ReferenceTypeImpl(rcd, combinedTypeSolver);
        symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        sourceRoot.getParserConfiguration().setSymbolResolver(symbolSolver);
        List<ParseResult<CompilationUnit>> list = sourceRoot.tryToParse();
        for (ParseResult<CompilationUnit> p : list) {
            jakartaRESTResourceVisitor.visit(p.getResult().get(), sb);
        }
        if (started) {
            sb.append("}" + LS);
        }
        processAdditionalClasses(symbolSolver, sb);
    }

    /*********************************************************************/
    /*********************** primary methods *****************************/
    /*********************************************************************/

    private static void processAdditionalClasses(JavaSymbolSolver symbolSolver, StringBuilder sb) throws FileNotFoundException {
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);
        for (String filename : additionalClasses) {
            int n = filename.lastIndexOf(":");
            if (n < 0) {
                throw new RuntimeException("bad syntax: " + filename);
            }
            String dir = filename.substring(0, n).trim();
            filename = dir + File.separator + filename.substring(n + 1).replace(".", File.separator) + ".java";
            CompilationUnit cu = StaticJavaParser.parse(new File(filename));
            classVisitor.visit(cu, sb);
        }
        if (isSSE) {
            sb.append(LS)
                    .append("message dev_resteasy_grpc_bridge_runtime_sse___SseEvent {" + LS)
                    .append("  string comment = ").append(counter++).append(";").append(LS)
                    .append("  string id = ").append(counter++).append(";").append(LS)
                    .append("  string name = ").append(counter++).append(";").append(LS)
                    .append("  google.protobuf.Any data = ").append(counter++).append(";").append(LS)
                    .append("  int64 reconnectDelay = ").append(counter++).append(";").append(LS)
                    .append("}").append(LS);
        }
    }

    private static void finishProto(StringBuilder sb) {
        if (needEmpty) {
            sb.append(LS + "message gEmpty {}");
            entityMessageTypes.add("gEmpty");
            returnMessageTypes.add("gEmpty");
        }

        for (String wrapper : PRIMITIVE_WRAPPER_DEFINITIONS.values()) {
            counter = 1;
            sb.append(LS).append(wrapper.replace("$V$", String.valueOf(counter++)));
        }
        createGeneralEntityMessageType(sb);
        createGeneralReturnMessageType(sb);
    }

    private static void createGeneralEntityMessageType(StringBuilder sb) {
        counter = 1;
        sb.append(LS + LS + "message gHeader {" + LS).append("   repeated string values = ").append(counter++)
                .append(";" + LS + "}");
        counter = 1;
        sb.append(LS + LS + "message gCookie {" + LS)
                .append("   string name = ").append(counter++).append(";" + LS)
                .append("   string value = ").append(counter++).append(";" + LS)
                .append("   int32  version = ").append(counter++).append(";" + LS)
                .append("   string path = ").append(counter++).append(";" + LS)
                .append("   string domain = ").append(counter++).append(";" + LS)
                .append("}");
        counter = 1;
        sb.append(LS + LS + "message gNewCookie {" + LS)
                .append("   string name = ").append(counter++).append(";" + LS)
                .append("   string value = ").append(counter++).append(";" + LS)
                .append("   int32  version = ").append(counter++).append(";" + LS)
                .append("   string path = ").append(counter++).append(";" + LS)
                .append("   string domain = ").append(counter++).append(";" + LS)
                .append("   string comment = ").append(counter++).append(";" + LS)
                .append("   int32 maxAge = ").append(counter++).append(";" + LS)
                .append("   google.protobuf.Timestamp expiry = ").append(counter++).append(";" + LS)
                .append("   bool secure = ").append(counter++).append(";" + LS)
                .append("   bool httpOnly = ").append(counter++).append(";" + LS + LS)
                .append("   enum SameSite {" + LS)
                .append("      NONE   = 0;" + LS)
                .append("      LAX    = 1;" + LS)
                .append("      STRICT = 2;" + LS)
                .append("   }" + LS + LS)
                .append("   SameSite sameSite = ").append(counter++).append(";" + LS)
                .append("}");
        counter = 1;
        sb.append(LS + LS + "message ServletInfo {" + LS)
                .append("   string characterEncoding = ").append(counter++).append(";" + LS)
                .append("   string clientAddress = ").append(counter++).append(";" + LS)
                .append("   string clientHost = ").append(counter++).append(";" + LS)
                .append("   int32  clientPort = ").append(counter++).append(";" + LS)
                .append("}");
        counter = 1;
        sb.append(LS + LS + "message FormValues {" + LS)
                .append("   repeated string formValues_field = ").append(counter++).append(";" + LS)
                .append("}");
        counter = 1;
        sb.append(LS + LS + "message FormMap {" + LS)
                .append("   map<string, FormValues> formMap_field = ").append(counter++).append(";" + LS)
                .append("}");
        counter = 1;
        sb.append(LS + LS + "message GeneralEntityMessage {" + LS)
                .append("   ServletInfo servletInfo = ").append(counter++).append(";" + LS)
                .append("   string URL = ").append(counter++).append(";" + LS)
                .append("   map<string, gHeader> headers = ").append(counter++).append(";" + LS)
                .append("   repeated gCookie cookies = ").append(counter++).append(";" + LS)
                .append("   string httpMethod = ").append(counter++).append(";" + LS)
                .append("   oneof messageType {" + LS);
        for (String messageType : entityMessageTypes) {
            if (ANY.equals(messageType)) {
                continue;
            }
            String simpleMessageType = messageType.contains(".") ? messageType.substring(messageType.lastIndexOf('.') + 1)
                    : messageType;
            sb.append("      ")
                    .append(messageType)
                    .append(" ")
                    .append(namify(simpleMessageType)).append("_field")
                    .append(" = ")
                    .append(counter++)
                    .append(";" + LS);
        }
        sb.append("      dev_resteasy_grpc_arrays___ArrayHolder dev_resteasy_grpc_arrays___ArrayHolder_field = ")
                .append(counter++).append(";" + LS);
        sb.append("      FormMap form_field = ").append(counter++).append(";" + LS);
        sb.append("   }" + LS + "}" + LS);
    }

    private static void createGeneralReturnMessageType(StringBuilder sb) {
        counter = 1;
        sb.append(LS + "message GeneralReturnMessage {" + LS)
                .append("   map<string, gHeader> headers = ").append(counter++).append(";" + LS)
                .append("   repeated gNewCookie cookies = ").append(counter++).append(";" + LS)
                .append("   int32 status = ").append(counter++).append(";" + LS)
                .append("   oneof messageType {" + LS);
        for (String messageType : returnMessageTypes) {
            if (ANY.equals(messageType)) {
                continue;
            }
            String simpleMessageType = messageType.contains(".") ? messageType.substring(messageType.lastIndexOf('.') + 1)
                    : messageType;
            sb.append("      ")
                    .append(messageType)
                    .append(" ")
                    .append(namify(simpleMessageType)).append("_field")
                    .append(" = ")
                    .append(counter++)
                    .append(";" + LS);
        }
        sb.append("   }" + LS + "}" + LS);
    }

    private static void writeProtoFile(String[] args, StringBuilder sb) throws IOException {
        Path path = Files.createDirectories(Path.of(args[0], "src", "main", "proto"));
        //        if (path.resolve(args[3] + ".proto").toFile().exists()) {
        //            return;
        //        }
        counter = 0;
        createArrayDefs(args, sb);
        sb.append("//////////  types: //////////" + LS);
        for (String s : entityTypes) {
            sb.append("// ").append(s).append(LS);
        }
        sb.append(LS + "//////////  synthetic names: //////////" + LS);
        for (String s : classnames.keySet()) {
            sb.append("// ").append(classnames.get(s)).append("->");
            if (s.contains("<")) {
                s = s.substring(0, s.indexOf('<'));
            }
            sb.append(s).append(LS);
        }
        Files.writeString(path.resolve(args[3] + ".proto"), sb.toString(), StandardCharsets.UTF_8);
        //        Path path2 = Path.of("/tmp/CC1.proto");
        //        Files.writeString(path2, sb.toString(), StandardCharsets.UTF_8);
        logger.debug("done");
    }

    private static void createProtobufDirectory(String[] args) {
        String path = args[0] + "/target/generatedSources";
        for (String s : args[1].split("\\.")) {
            path += "/" + s;
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdir();
            }
        }
    }

    private static void createArrayDefs(String[] args, StringBuilder wrapperBuilder) {
        BufferedWriter writer = null;
        try {
            for (String type : repeatedTypes) {
                if ("google.protobuf.Any".equals(type)) {
                    continue;
                }
                String component = type.substring(0, type.indexOf("___WArray"));
                wrapperBuilder.append(String.format(arrayDef, type, component));
            }
            wrapperBuilder.append(String.format(arrayHolderDef));
            SortedSet<String> holderTypes = new TreeSet<String>(basicRepeatedTypes.values());
            for (String rt : repeatedTypes) {
                if ("google.protobuf.Any".equals(rt)) {
                    continue;
                }
                holderTypes.add(rt);
            }
            counter = 0;
            wrapperBuilder.append("message dev_resteasy_grpc_arrays___ArrayHolder {" + LS)
                    .append("   oneof messageType {" + LS);
            for (String type : holderTypes) {
                if (TYPE_MAP.containsKey(type)) {
                    continue;
                }
                String typeName = type.contains(".") ? type.substring(type.lastIndexOf('.') + 1) : type;
                wrapperBuilder.append("      ")
                        .append(type)
                        .append(" ")
                        .append(typeName)
                        .append("_field = ")
                        .append(++counter)
                        .append(";" + LS);
            }
            wrapperBuilder.append("   }" + LS).append("}" + LS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void writeEntityTypesFile(String[] args) throws IOException {
        Path path = Path.of(args[0], "/target");
        Files.createDirectories(path);
        File file = new File(path.toString() + "/entityTypes");
        try (FileWriter writer = new FileWriter(file)) {
            for (String type : entityTypesForFile) {
                String s1 = type.substring(1, type.indexOf(" "));
                String s2 = type.substring(type.indexOf(" ") + 1);
                writer.write(transformClassName(s1) + " " + s2 + LS);
            }
        } catch (Exception e) {
            logger.error("HMMMMMM");
        }
    }

    /****************************************************************************/
    /********************************** classes *********************************/
    /****************************************************************************/

    /**
     * Visits each class in the transitive closure of all classes referenced in the
     * signatures of resource methods. Creates a service with an rpc declaration for
     * each resource method or locator.
     */
    static class JakartaRESTResourceVisitor extends VoidVisitorAdapter<StringBuilder> {

        public void visit(final ClassOrInterfaceDeclaration subClass, StringBuilder sb) {
            if (subClass.getFullyQualifiedName().orElse("").startsWith("grpc.server")) {
                return;
            }
            Optional<AnnotationExpr> opt = subClass.getAnnotationByName("Path");
            SingleMemberAnnotationExpr annotationExpr = opt.isPresent() ? (SingleMemberAnnotationExpr) opt.get() : null;
            String classPath = "";
            if (annotationExpr != null) {
                classPath = annotationExpr.getMemberValue().toString();
                classPath = classPath.substring(1, classPath.length() - 1);
            }
            for (BodyDeclaration<?> bd : subClass.getMembers()) {
                if (bd instanceof MethodDeclaration) {
                    MethodDeclaration md = (MethodDeclaration) bd;
                    if (!isResourceOrLocatorMethod(md)) {
                        continue;
                    }
                    String methodPath = "";
                    opt = md.getAnnotationByName("Path");
                    annotationExpr = opt.isPresent() ? (SingleMemberAnnotationExpr) opt.get() : null;
                    if (annotationExpr != null) {
                        methodPath = annotationExpr.getMemberValue().toString();
                        methodPath = methodPath.substring(1, methodPath.length() - 1);
                    }
                    String httpMethod = getHttpMethod(md);

                    // Add service with a method for each resource method in class.
                    if (!started) {
                        sb.append(LS + "service ").append(prefix).append("Service {" + LS);
                        started = true;
                    }
                    String entityType = getEntityParameter(md, httpMethod);
                    String returnType = getReturnType(md, httpMethod);
                    entityMessageTypes.add(entityType);
                    returnMessageTypes.add(returnType);
                    String syncType = isSuspended(md) ? "suspended"
                            : (isCompletionStage(md) ? "completionStage" : (isSSE(md) ? "sse" : "sync"));
                    isSuspended(md);
                    sb.append("// ");
                    if (!("".equals(classPath))) {
                        sb.append(classPath).append("/");
                    }
                    sb.append(methodPath).append(" ")
                            .append(entityType).append(" ")
                            .append(returnType).append(" ")
                            .append(httpMethod).append(" ")
                            .append(syncType).append("" + LS);

                    sb.append("  rpc ")
                            .append(getRpcName(rpcNames, md.getNameAsString()))
                            .append(" (")
                            .append("GeneralEntityMessage")
                            .append(") returns (")
                            .append("sse".equals(syncType) ? "stream " : "")
                            .append("sse".equals(syncType) ? SSE_EVENT_CLASSNAME : "GeneralReturnMessage")
                            .append(");" + LS);

                    // Add each parameter and return type to resolvedTypes for further processing.
                    for (Parameter p : md.getParameters()) {

                        if (!isEntity(p)) {
                            continue;
                        }
                        if (p.getType().resolve().isPrimitive()) {
                            continue;
                        }
                        if (p.getType().isArrayType()) {
                            continue;
                        }
                        ReferenceTypeImpl rt = (ReferenceTypeImpl) p.getType().resolve();
                        ResolvedType objectified = objectify(rt);
                        if (!visited.contains(objectified.describe())) {
                            pendingTypes.add(objectified);
                        }
                    }
                }
            }
        }
    }

    /**
     * Visit all classes discovered by JakartaRESTResourceVisitor in the process of visiting all Jakarta REST resources
     */
    static class ClassVisitor extends VoidVisitorAdapter<StringBuilder> {

        /**
         * For each class, create a message type with a field for each variable in the class.
         */
        public void visit(ResolvedType clazz, StringBuilder sb) {
            pendingTypes.remove(clazz);
            if (visited.contains(clazz.describe())) {
                return;
            }
            counter = 1;
            doVisit(clazz, sb, true);
            visited.add(clazz.describe());
        }

        private void doVisit(ResolvedType resolvedType, StringBuilder sb, boolean start) {
            ResolvedReferenceType clazz = resolvedType.asReferenceType();
            Set<String> fieldNames = new HashSet<String>();
            if (PRIMITIVE_WRAPPER_TYPES_FIELD.containsKey(clazz.describe())) {
                return;
            }
            if (Response.class.getName().equals(clazz.describe())) {
                return;
            }
            boolean isList = isList(clazz);
            boolean isSet = isSet(clazz);
            ResolvedType objectified = objectify(clazz);
            if (isList) {
                sb.append(LS).append("// List: ").append(objectified.describe());
            } else if (isSet) {
                sb.append(LS).append("// Set: ").append(objectified.describe());
            } else if ("java.lang.Object".equals(objectified.describe())) {
                return;
            }
            if (start) {
                String innerClass = isInnerClass(resolvedType.asReferenceType().getTypeDeclaration().get());
                String javabufName = fqnifyClass(objectified, innerClass);
                sb.append(LS + "message ").append(javabufName).append(" {" + LS);
            }
            // Handle set or list
            if (isList || isSet) {
                sb.append("  string classname = ")
                        .append(counter++)
                        .append(";" + LS);
                visitCollection(objectified, fieldNames, sb);
                if (start) {
                    sb.append("}" + LS);
                }
                return;
            }

            // Handle superclass
            ResolvedType superClass = getSuperClass(clazz);
            if (superClass != null && !"java.lang.Object".equals(superClass.describe())) {
                doVisit(superClass, sb, false);
            }

            // Scan all variables in class.
            for (ResolvedFieldDeclaration rfd : clazz.getDeclaredFields()) {
                String type = null;
                if ("$assertionsDisabled".equals(rfd.getName())) {
                    continue;
                }
                if (rfd.getName() != null && rfd.getName().startsWith("this$")) {
                    continue;
                }
                if (clazz.describe().contains("safe")) {
                    continue;
                }
                if (TYPE_MAP.containsKey(rfd.getType().describe())) {
                    type = TYPE_MAP.get(rfd.getType().describe());
                } else if (PRIMITIVE_WRAPPER_TYPES_FIELD.containsKey(rfd.getType().describe())) {
                    type = PRIMITIVE_WRAPPER_TYPES_FIELD.get(rfd.getType().describe());
                } else if (rfd.getType() instanceof ResolvedArrayType) {
                    type = visitArray(rfd);
                    if (type == null) {
                        continue;
                    }
                } else { // Defined type
                    ResolvedType rt = rfd.getType();
                    if (rt.isReferenceType()) {
                        ResolvedType objectifiedField = objectify(rt.asReferenceType());
                        type = visitReferenceType(objectifiedField);
                        if (isSetOrList(rt.asReferenceType())) {
                            sb.append("  //").append(objectifiedField.describe()).append(LS);
                        }
                    } else if (rfd.getType().isTypeVariable()) {
                        Optional<ResolvedType> opt = clazz.getGenericParameterByName(rfd.getType().asTypeVariable().describe());
                        if (opt.isPresent() && opt.get().isReferenceType()) {
                            type = fqnifyClass(opt.get(), "___");
                        } else {
                            type = "google.protobuf.Any";
                        }
                    }
                }
                String fieldName = getFieldName(fieldNames, rfd.getName());
                if (type != null) {
                    sb.append("  ")
                            .append(type)
                            .append(" ")
                            .append(fieldName)
                            .append(" = ")
                            .append(counter++)
                            .append(";" + LS);
                }
            }
            if (start) {
                sb.append("}" + LS);
            }
        }
    }

    /**
     * 1. Create new type in which all wildcards and type variables are replaced with Object.class.
     * 2. Create non generic version of type.
     */
    private static ResolvedType objectify(ResolvedReferenceType clazz) {
        if (objectifiedTypes.containsKey(clazz)) {
            return objectifiedTypes.get(clazz);
        }
        List<ResolvedType> list = new ArrayList<ResolvedType>();
        Iterator<Pair<ResolvedTypeParameterDeclaration, ResolvedType>> iterator = clazz.getTypeParametersMap().iterator();
        while (iterator.hasNext()) {
            Pair<ResolvedTypeParameterDeclaration, ResolvedType> pair = iterator.next();
            if (pair.b.isTypeVariable() || pair.b.isWildcard()) {
                list.add(objectType);
            } else if (pair.b.isReferenceType()) {
                list.add(objectify(pair.b.asReferenceType()));
            }
        }
        ReferenceTypeImpl rti = new ReferenceTypeImpl(clazz.getTypeDeclaration().get(), list, combinedTypeSolver);
        objectifiedTypes.put(clazz, rti);
        List<ResolvedType> list2 = new ArrayList<ResolvedType>();
        // Create non generic version of clazz.
        ReferenceTypeImpl rti2 = new ReferenceTypeImpl(clazz.getTypeDeclaration().get(), list2, combinedTypeSolver);
        if (!visited.contains(rti2.describe())) {
            pendingTypes.add(rti2);
        }
        return rti;
    }

    public static ReflectionClassDeclaration rcd = new ReflectionClassDeclaration(Object.class, combinedTypeSolver);

    private static void visitCollection(ResolvedType resolvedType, Set<String> fieldNames, StringBuilder sb) {
        Pair<ResolvedTypeParameterDeclaration, ResolvedType> pair = getParameterType(resolvedType);
        ResolvedType rt = pair.b;
        String fqn = fqnifyClass(rt, "___");
        if (!"google.protobuf.Any".equals(fqn)) {
            classnameMap.put(rt.describe(), fqn);
            pendingTypes.add(rt.asReferenceType());
        }
        sb.append("  //").append(rt.describe()).append(LS)
                .append("  repeated ").append(fqn).append(" data = ").append(counter++).append(";" + LS);
    }

    private static String visitArray(ResolvedFieldDeclaration rfd) {
        ResolvedArrayType rat = (ResolvedArrayType) rfd.getType();
        ResolvedType ct = rat.getComponentType();
        String type = null;
        String fqn = null;
        if ("java.lang.Object".equals(ct.describe())) {
            return null;
        } else if (PRIMITIVE_ARRAY_TYPE.containsKey(ct.describe())) {
            type = PRIMITIVE_ARRAY_TYPE.get(ct.describe());
        } else if (WRAPPER_TO_JAVABUF_MAP.keySet().contains(ct.describe())) {
            type = WRAPPER_TO_JAVABUF_MAP.get(ct.describe());
        } else if (ct instanceof ResolvedArrayType) {
            type = "dev_resteasy_grpc_arrays___ArrayHolder___WArray";
            ResolvedType bat = getBasicArrayType((ResolvedArrayType) ct);
            if (bat.isReference()) {
                fqn = bat.asReferenceType().getQualifiedName(); // type variable?
                if (!visited.contains(fqn)) {
                    pendingTypes.add(objectify(bat.asReferenceType()));
                }
            }
        } else {
            if (!ct.isReferenceType()) {
                return null;
            }
            fqn = removeTypeVariables(ct.describe());
            if (PRIMITIVE_WRAPPER_TYPES_FIELD.containsKey(fqn)) {
                type = wrapRepeated(PRIMITIVE_WRAPPER_TYPES_FIELD.get(fqn));
            } else if (!visited.contains(fqn)) {
                pendingTypes.add(objectify(ct.asReferenceType()));
                type = wrapRepeated(fqnifyClass(fqn, isInnerClass(ct.asReferenceType()
                        .getTypeDeclaration()
                        .get())));
            }
        }
        return type;
    }

    private static String visitReferenceType(ResolvedType rt) {
        pendingTypes.add(rt);
        // Save all type variable values
        List<Pair<ResolvedTypeParameterDeclaration, ResolvedType>> pairs = rt.asReferenceType().getTypeParametersMap();
        ListIterator<Pair<ResolvedTypeParameterDeclaration, ResolvedType>> it = pairs.listIterator();
        while (it.hasNext()) {
            Pair<ResolvedTypeParameterDeclaration, ResolvedType> pair = it.next();
            if (pair.b.isReferenceType()) {
                pendingTypes.add(objectify(pair.b.asReferenceType()));
            }
        }
        String innerClass = isInnerClass(rt.asReferenceType().getTypeDeclaration().get());
        String fqn = fqnifyClass(rt, innerClass);
        classnameMap.put(rt.describe(), fqn);
        return fqn;
    }

    /*************************************************/
    /*************** utility methods *****************/
    /*************************************************/
    private static String getEntityParameter(MethodDeclaration md, String httpMethod) {
        if (HttpServletRequestImpl.LOCATOR.equals(httpMethod)) {
            return "google.protobuf.Any";
        }
        String protoClass = JavaToProtobufGenerator.args[1] + "." + JavaToProtobufGenerator.args[3] + "_proto$";
        for (Parameter p : md.getParameters()) {
            if (isEntity(p)) {
                ResolvedType rt = p.getType().resolve();
                if (rt.isReferenceType()) {
                    rt = objectify(rt.asReferenceType());
                }
                String javaType = rt.describe();
                String rawType = p.getTypeAsString();
                if (PRIMITIVE_WRAPPER_TYPES_IO.containsKey(rawType)) {
                    entityTypes.add(PRIMITIVE_WRAPPER_TYPES_IO.get(rawType));
                    entityTypesForFile.add("1" + javaType + " " + protoClass + PRIMITIVE_WRAPPER_TYPES_IO.get(rawType));
                    return PRIMITIVE_WRAPPER_TYPES_IO.get(rawType);
                }
                if (TYPE_MAP.containsKey(rawType)) {
                    entityTypes.add(TYPE_MAP.get(rawType));
                    entityTypesForFile.add("2" + javaType + " " + TYPE_MAP.get(rawType));
                    return TYPE_MAP.get(rawType);
                }
                if (rt.isArray()) {
                    if (basicRepeatedTypes.containsKey(rt.asArrayType().getComponentType().describe())) {
                        entityTypes
                                .add(basicRepeatedTypes.get(rt.asArrayType().getComponentType().describe()));
                        entityTypesForFile
                                .add("4" + javaType + " "
                                        + basicRepeatedEntityTypes.get(rt.asArrayType().getComponentType().describe()));
                        return basicRepeatedTypes.get(rt.asArrayType().getComponentType().describe());
                    } else if (rt.asArrayType().getComponentType().arrayLevel() == 0) {
                        String name = rt.describe().substring(0, rt.describe().indexOf("["));
                        name = fqnifyClass(name,
                                isInnerClass(rt.asArrayType().getComponentType().asReferenceType().getTypeDeclaration().get()));
                        name += "___WArray";
                        repeatedTypes.add(name);
                        entityTypes.add(name);
                        entityTypesForFile.add("5" + javaType + " " + protoClass + name);
                        return name;
                    } else {
                        entityTypes.add("dev_resteasy_grpc_arrays___ArrayHolder___WArray");
                        entityTypesForFile
                                .add("5" + javaType + " " + protoClass + "dev_resteasy_grpc_arrays___ArrayHolder___WArray");
                        return "dev_resteasy_grpc_arrays___ArrayHolder___WArray";
                    }
                }
                if (isInterface(rt)) {
                    return "google.protobuf.Any";
                }
                pendingTypes.add(rt);
                String s = fqnifyClass(rt, isInnerClass(rt.asReferenceType().getTypeDeclaration().get()));
                String javabufType = protoClass + s;
                entityTypes.add(s);
                entityTypesForFile.add("7" + javaType + " " + javabufType);
                return s;
            }
        }
        needEmpty = true;
        return "gEmpty";
    }

    private static boolean isInterface(ResolvedType rt) {
        String name = rt.describe();
        if (name.contains("<")) {
            name = name.substring(0, name.indexOf("<"));
        }
        List<ResolvedMethodDeclaration> list = rt.asReferenceType().getAllMethods();
        for (ResolvedMethodDeclaration rmd : list) {
            if (rmd.declaringType().getQualifiedName().equals(name)) {
                return rmd.declaringType().isInterface();
            }
        }
        List<ResolvedFieldDeclaration> list2 = rt.asReferenceType().getAllFieldsVisibleToInheritors();
        for (ResolvedFieldDeclaration rmd : list2) {
            if (rmd.declaringType().getQualifiedName().equals(name)) {
                return rmd.declaringType().isInterface();
            }
        }
        return true;
    }

    private static boolean isEntity(Parameter p) {
        for (AnnotationExpr ae : p.getAnnotations()) {
            if (ANNOTATIONS.contains(ae.getNameAsString())) {
                return false;
            }
        }
        String name = p.getTypeAsString();
        if (AsyncResponse.class.getName().equals(name) || AsyncResponse.class.getSimpleName().equals(name)) {
            return false;
        }
        return true;
    }

    private static String getReturnType(MethodDeclaration md, String httpMethod) {
        if (isSuspended(md) || HttpServletRequestImpl.LOCATOR.equals(httpMethod)) {
            return "google.protobuf.Any";
        }
        if (isSSE(md)) {
            return SSE_EVENT_CLASSNAME;
        }
        for (Node node : md.getChildNodes()) {
            if (node instanceof Type) {
                if (((Type) node).isTypeParameter()) {
                    continue;
                }
                if (node instanceof VoidType) {
                    return "google.protobuf.Any"; // ??
                }
                String rawType = ((Type) node).asString();
                int open = rawType.indexOf("<");
                int close = rawType.indexOf(">");
                if (open >= 0 && close > open) {
                    String type = rawType.substring(0, open);
                    String parameterType = rawType.substring(open + 1, close);
                    if (CompletionStage.class.getCanonicalName().contentEquals(type)
                            || CompletionStage.class.getSimpleName().contentEquals(type)) {
                        rawType = parameterType;
                    } else {
                        rawType = type;
                    }
                }
                if (PRIMITIVE_WRAPPER_TYPES_IO.containsKey(rawType)) {
                    return PRIMITIVE_WRAPPER_TYPES_IO.get(rawType);
                }
                if (TYPE_MAP.containsKey(rawType)) {
                    return TYPE_MAP.get(rawType);
                }
                if ("jakarta.ws.rs.core.Response".equals(rawType) || "Response".equals(rawType)) {
                    return "google.protobuf.Any";
                }
                ResolvedType rt = ((Type) node).resolve();
                if (rt.isArray()) {
                    if (basicRepeatedTypes.containsKey(rt.asArrayType().getComponentType().describe())) {
                        return basicRepeatedTypes.get(rt.asArrayType().getComponentType().describe());
                    } else if (rt.asArrayType().getComponentType().arrayLevel() == 0) {
                        String name = rt.describe().substring(0, rt.describe().indexOf("["));
                        name = fqnifyClass(name,
                                isInnerClass(rt.asArrayType().getComponentType().asReferenceType().getTypeDeclaration().get()));
                        name += "___WArray";
                        repeatedTypes.add(name);
                        return name;
                    } else {
                        return "dev_resteasy_grpc_arrays___ArrayHolder___WArray";
                    }
                }
                if (isInterface(rt)) {
                    return "google.protobuf.Any";
                }
                rt = objectify(rt.asReferenceType());
                pendingTypes.add(rt);
                return fqnifyClass(rt, isInnerClass(rt.asReferenceType().getTypeDeclaration().get()));
            }
        }
        needEmpty = true;
        return "gEmpty";
    }

    private static boolean isSuspended(MethodDeclaration md) {
        for (Parameter p : md.getParameters()) {
            for (AnnotationExpr ae : p.getAnnotations()) {
                if ("Suspended".equals(ae.getNameAsString())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isCompletionStage(MethodDeclaration md) {
        for (Node node : md.getChildNodes()) {
            if (node instanceof Type) {
                String rawType = ((Type) node).asString();
                int open = rawType.indexOf("<");
                int close = rawType.indexOf(">");
                if (open >= 0 && close > open) {
                    String type = rawType.substring(0, open);
                    if (CompletionStage.class.getCanonicalName().contentEquals(type)
                            || CompletionStage.class.getSimpleName().contentEquals(type)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean isSSE(MethodDeclaration md) {
        Optional<AnnotationExpr> opt = md.getAnnotationByName("Produces");
        if (opt.isEmpty()) {
            return false;
        }
        AnnotationExpr ae = opt.get();
        List<StringLiteralExpr> list1 = ae.findAll(StringLiteralExpr.class);
        for (Iterator<StringLiteralExpr> it = list1.iterator(); it.hasNext();) {
            StringLiteralExpr sle = it.next();
            if (MediaType.SERVER_SENT_EVENTS.equals(sle.getValue())) {
                isSSE = true;
                return true;
            }
        }
        List<FieldAccessExpr> list2 = ae.findAll(FieldAccessExpr.class);
        for (Iterator<FieldAccessExpr> it = list2.iterator(); it.hasNext();) {
            FieldAccessExpr fae = it.next();
            List<Node> list3 = fae.getChildNodes();
            if (list3.size() >= 2 && list3.get(0) instanceof NameExpr && list3.get(1) instanceof SimpleName) {
                NameExpr ne = (NameExpr) list3.get(0);
                SimpleName sn = (SimpleName) list3.get(1);
                if ("MediaType".equals(ne.getName().asString()) && "SERVER_SENT_EVENTS".equals(sn.asString())) {
                    isSSE = true;
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isResourceOrLocatorMethod(MethodDeclaration md) {
        for (AnnotationExpr ae : md.getAnnotations()) {
            if (HTTP_VERBS.contains(ae.getNameAsString().toUpperCase()) || "Path".equals(ae.getNameAsString())) {
                return true;
            }
        }
        return false;
    }

    private static String removeTypeVariables(String classType) {
        int left = classType.indexOf('<');
        if (left < 0) {
            return classType;
        }
        return classType.substring(0, left);
    }

    private static boolean isSetOrList(ResolvedReferenceType clazz) {
        if ("java.util.List".equals(clazz.getQualifiedName()) || "java.util.Set".equals(clazz.getQualifiedName())) {
            return true;
        }
        for (ResolvedReferenceType rrt : clazz.getAllInterfacesAncestors()) {
            if ("java.util.List".equals(rrt.getQualifiedName()) || "java.util.Set".equals(rrt.getQualifiedName())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isSet(ResolvedReferenceType clazz) {
        if ("java.util.Set".equals(clazz.getQualifiedName())) {
            return true;
        }
        for (ResolvedReferenceType rrt : clazz.getAllInterfacesAncestors()) {
            if ("java.util.Set".equals(rrt.getQualifiedName())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isList(ResolvedReferenceType clazz) {
        if ("java.util.List".equals(clazz.getQualifiedName())) {
            return true;
        }
        for (ResolvedReferenceType rrt : clazz.getAllInterfacesAncestors()) {
            if ("java.util.List".equals(rrt.getQualifiedName())) {
                return true;
            }
        }
        return false;
    }

    private static String isInnerClass(ResolvedReferenceTypeDeclaration clazz) {
        try {
            Optional<?> opt = clazz.containerType();
            if (opt.isEmpty()) {
                return "___";
            }
            ResolvedTypeDeclaration rtd = clazz.containerType().get();
            if (!rtd.isClass()) {
                return "___";
            }
            if (AccessSpecifier.PUBLIC.equals(clazz.asClass().accessSpecifier())) {
                return "_INNER_";
            }
            return "_HIDDEN_";
        } catch (Exception e) {
            return "___";
        }
    }

    private static String fqnifyClass(ResolvedType rt, String separator) {
        if (rt.isTypeVariable() || rt.isWildcard() || "java.lang.Object".equals(rt.describe())) {
            return "google.protobuf.Any";
        }
        if (PRIMITIVE_WRAPPER_TYPES_FIELD.containsKey(rt.describe())) {
            return PRIMITIVE_WRAPPER_TYPES_FIELD.get(rt.describe());
        }
        if (classnameMap.containsKey(rt.describe())) {
            return classnameMap.get(rt.describe());
        }
        String fqn = fqnifyClass(rt.describe(), separator);
        classnameMap.put(rt.describe(), fqn);
        return fqn;
    }

    private static String fqnifyClass(String s, String separator) {
        if ("?".equals(s)) {
            return "google.protobuf.Any";
        }
        s = disambiguateClasses(s);
        int l = s.lastIndexOf(".");
        if (l < 0) {
            return s;
        }
        String sPackage = s.substring(0, l).replace(".", "_");
        String className = s.substring(l + 1);
        return sPackage + separator + className;
    }

    private static String disambiguateClasses(String classname) {
        if (classname.contains("<")) {
            if (classnames.containsKey(classname)) {
                return classnames.get(classname);
            }
            String strippedClassName = stripTypeVariables(classname);
            String newName = strippedClassName + classnameCounter.getAndIncrement();
            classnames.put(classname, newName);
            return newName;
        }
        return classname;
    }

    private static String stripTypeVariables(String classname) {
        if (!classname.contains("<")) {
            return classname;
        }
        return classname.substring(0, classname.indexOf("<"));
    }

    private static String namify(String s) {
        return s.replace(".", "_");
    }

    private static Pair<ResolvedTypeParameterDeclaration, ResolvedType> getParameterType(ResolvedType resolvedType) {
        List<Pair<ResolvedTypeParameterDeclaration, ResolvedType>> lt = resolvedType.asReferenceType().getTypeParametersMap();
        if (lt.size() < 1) {
            for (ResolvedReferenceType rrt : resolvedType.asReferenceType().getAllAncestors()) {
                lt = rrt.asReferenceType().getTypeParametersMap();
                if (lt.size() > 0) {
                    break;
                }
            }
        }
        if (lt.size() < 1) {
            return null;
        }
        Pair<ResolvedTypeParameterDeclaration, ResolvedType> pair = lt.get(0);
        if (pair.b.isTypeVariable()) {
            return new Pair<ResolvedTypeParameterDeclaration, ResolvedType>(pair.a, objectType);
        }
        return lt.get(0);
    }

    private static String getHttpMethod(MethodDeclaration md) {
        if (!md.getAnnotationByName("DELETE").isEmpty()) {
            return "DELETE";
        }
        if (!md.getAnnotationByName("GET").isEmpty()) {
            return "GET";
        }
        if (!md.getAnnotationByName("HEAD").isEmpty()) {
            return "HEAD";
        }
        if (!md.getAnnotationByName("OPTIONS").isEmpty()) {
            return "OPTIONS";
        }
        if (!md.getAnnotationByName("PATCH").isEmpty()) {
            return "PATCH";
        }
        if (!md.getAnnotationByName("POST").isEmpty()) {
            return "POST";
        }
        if (!md.getAnnotationByName("PUT").isEmpty()) {
            return "PUT";
        }
        return HttpServletRequestImpl.LOCATOR;
    }

    private static ResolvedType getBasicArrayType(ResolvedArrayType rat) {
        if (rat.getComponentType().isArray()) {
            return getBasicArrayType((ResolvedArrayType) rat.getComponentType());
        } else {
            return rat.getComponentType();
        }
    }

    private static String getRpcName(Set<String> rpcNames, String proposedName) {
        String name = proposedName.substring(0, 1).toLowerCase() + proposedName.substring(1);
        int counter = 1;
        while (rpcNames.contains(name.toLowerCase())) {
            name = proposedName + "___" + counter++;
        }
        rpcNames.add(name.toLowerCase());
        return name;
    }

    private static String getFieldName(Set<String> fieldNames, String proposedName) {
        String name = proposedName;
        int counter = 1;
        while (fieldNames.contains(name.toLowerCase())) {
            name = proposedName + "___" + counter++;
        }
        fieldNames.add(name.toLowerCase());
        return name;
    }

    private static ResolvedType getSuperClass(ResolvedReferenceType clazz) {
        if (clazz.getAllAncestors().size() == 0) {
            return null;
        }
        return clazz.getAllAncestors().get(0);
    }

    private static String wrapRepeated(String type) {
        if (REPEAT_MAP.containsKey(type)) {
            type = REPEAT_MAP.get(type);
        } else {
            type += "___WArray";
            repeatedTypes.add(type);
        }
        return type;
    }

    private static String transformClassName(String s) {
        if (!s.endsWith("[]")) {
            return s;
        }
        String component = s.substring(0, s.indexOf("[]"));
        if (PRIMITIVE_TYPES.containsKey(component)) {
            return addLeft(PRIMITIVE_TYPES.get(component), depth(s));
        }
        return addLeft("L" + component + ";", depth(s));
    }

    private static int depth(String s) {
        if (s.endsWith("[]")) {
            return 1 + depth(s.substring(0, s.length() - 2));
        }
        return 0;
    }

    private static String addLeft(String s, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append("[");
        }
        return sb.append(s).toString();
    }

    static final boolean hasInterfaceType(FieldDeclaration f) {
        return f.resolve().declaringType().isInterface();
    }
}
