package dev.resteasy.grpc.example;

public class CC10<T> {
    private T t;

    public CC10(T t) {
        this.t = t;
    }

    T tNumber(T n) {
        return n;
    }

    public T getT() {
        return t;
    }
}
