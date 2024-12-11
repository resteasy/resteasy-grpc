package dev.resteasy.grpc.lists.sets;

/**
 * This will be used as a generic type but not raw type, to test
 * generation of raw type version in .proto file.
 */
public class D6<T> {
    T t;

    public D6(T t) {
        this.t = t;
    }

    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (!D6.class.equals(other.getClass())) {
            return false;
        }
        D6 od6 = (D6) other;
        if (t == null) {
            return od6.t == null;
        }
        return t.equals(od6.t);
    }
}
