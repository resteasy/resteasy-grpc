package dev.resteasy.grpc.example;

public class CC11<T extends TestClass> {
    private T t;

    public CC11(T t) {
        this.t = t;
    }

    T tNumber(T n) {
        return n;
    }

    public T getT() {
        return t;
    }

    public boolean equals(Object o) {
        if (!(o instanceof CC11)) {
            return false;
        }
        CC11 other = (CC11) o;
        return other.t.equals(t);
    }
}
