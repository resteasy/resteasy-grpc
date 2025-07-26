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

    public boolean equals(Object o) {
        if (!(o instanceof CC10)) {
            return false;
        }
        CC10 other = (CC10) o;
        return other.t.equals(t);
    }
}
