package dev.resteasy.grpc.example;

public class TestClass {

    String s;

    public TestClass() {
    }

    public TestClass(String s) {
        this.s = s;
    }

    public String getS() {
        return s;
    }

    public boolean equals(Object o) {
        if (!(o instanceof TestClass)) {
            return false;
        }
        TestClass other = (TestClass) o;
        return this.s.equals(other.s);
    }
}
