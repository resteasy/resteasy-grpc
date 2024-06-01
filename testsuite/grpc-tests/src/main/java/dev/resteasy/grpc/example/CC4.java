package dev.resteasy.grpc.example;

public class CC4 {
    public String s;
    public CC5 cc5;

    public CC4(String s, CC5 cc5) {
        this.s = s;
        this.cc5 = cc5;
    }

    public boolean equals(Object o) {
        if (!CC4.class.equals(o.getClass())) {
            return false;
        }
        CC4 cc4 = (CC4) o;
        return s.equals(cc4.s) && cc5.k == cc4.cc5.k;
    }
}
