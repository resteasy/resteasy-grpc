package dev.resteasy.grpc.example;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("generic")
public class GenericResource {

    @POST
    @Path("cc10/wildcard")
    public CC10<?> cc10Wildcrd(CC10<?> cc10) {
        return cc10;
    }

    @POST
    @Path("cc10/variable")
    public <T> CC10<T> cc10Variable(CC10<T> cc10) {
        return cc10;
    }

    @POST
    @Path("cc10/Integer")
    public CC10<Integer> cc10Integer(CC10<Integer> cc10) {
        return cc10;
    }

    @POST
    @Path("cc10/Float")
    public CC10<Float> cc10Float(CC10<Float> cc10) {
        return cc10;
    }

    @POST
    @Path("cc10/wildcard/lower")
    public CC10<? super TestSubClass> cc10WildcardLower(CC10<? super TestSubClass> cc10) {
        return cc10;
    }

    @POST
    @Path("cc10/variable/upper")
    public <T extends TestClass> CC10<T> cc10VariableUpper(CC10<T> cc10) {
        return cc10;
    }

    @POST
    @Path("cc11/wildcard")
    public CC11<?> cc11Wildcard(CC11<?> cc11) {
        return cc11;
    }

    @POST
    @Path("cc11/variable")
    public <T extends TestClass> CC11<T> cc11Variable(CC11<T> cc11) {
        return cc11;
    }

    /*
     * Just to get TestClass into CC1.proto
     */
    @POST
    @Path("testclass")
    public TestClass testClass(TestClass tc) {
        return tc;
    }

    /*
     * Just to get TestSubClass into CC1.proto
     */
    @POST
    @Path("testsubclass")
    public TestSubClass testSubClass(TestSubClass tsc) {
        return tsc;
    }
}
