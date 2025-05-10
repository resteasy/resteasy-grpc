package dev.resteasy.grpc.example;

public class TestSubClass extends TestClass {

    Integer i;

    public TestSubClass() {
    }

    public TestSubClass(String s, Integer i) {
        super(s);
        this.i = i;
    }

    public Integer getI() {
        return i;
    }

    public boolean equals(Object o) {
        if (!(o instanceof TestSubClass)) {
            return false;
        }
        TestSubClass other = (TestSubClass) o;
        return super.equals(other) && this.i.equals(other.i);
    }
}
