package dev.resteasy.grpc.maps;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class M1 implements Map<String, Integer> {

    private HashMap<String, Integer> delegate = new HashMap<String, Integer>();

    public M1() {
    }

    public M1(String key, Integer value) {
        delegate.put(key, value);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (Entry<String, Integer> entry : delegate.entrySet()) {
            sb.append("[").append(entry.getKey()).append("->").append(entry.getValue()).append("]");
        }
        return sb.append("]").toString();
    }

    @Override
    public int hashCode() {
        return 17;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof M1)) {
            return false;
        }
        M1 m1 = (M1) o;
        if (m1.size() != size()) {
            return false;
        }
        Iterator<Entry<String, Integer>> it1 = delegate.entrySet().iterator();
        Iterator<Entry<String, Integer>> it2 = m1.entrySet().iterator();
        for (int i = 0; i < size(); i++) {
            Entry<String, Integer> e1 = it1.next();
            Entry<String, Integer> e2 = it2.next();
            if (!(e1.getKey().equals(e2.getKey())) || !(e1.getValue().equals(e2.getValue()))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    @Override
    public Integer get(Object key) {
        return delegate.get(key);
    }

    @Override
    public Integer put(String key, Integer value) {
        return delegate.put(key, value);
    }

    @Override
    public Integer remove(Object key) {
        return delegate.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Integer> m) {
        delegate.putAll(m);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public Set<String> keySet() {
        return delegate.keySet();
    }

    @Override
    public Collection<Integer> values() {
        return delegate.values();
    }

    @Override
    public Set<Entry<String, Integer>> entrySet() {
        return delegate.entrySet();
    }

}
