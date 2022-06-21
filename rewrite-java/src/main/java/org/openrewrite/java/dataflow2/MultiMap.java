package org.openrewrite.java.dataflow2;

import org.openrewrite.internal.lang.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MultiMap<K,V> {

    private Map<K, ArrayList<V>> map = new HashMap<>();

    public void add(K key, V value) {
        ArrayList<V> list = map.get(key);
        if(list == null) {
            list = new ArrayList<>();
            map.put(key, list);
        }
        list.add(value);
    }

    public Collection<K> keySet() {
        return map.keySet();
    }

    public @NonNull ArrayList<V> get(K key) {
        ArrayList<V> list = map.get(key);
        return list;
    }

    public void put(K key, ArrayList<V> list) {
        map.put(key, list);
    }
}
