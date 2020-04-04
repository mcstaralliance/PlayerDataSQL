package cc.bukkitPlugin.pds.util;

import lombok.Getter;

public class Pair<K, V> {

    @Getter
    private final K key;
    @Getter
    private final V value;

    public Pair(K pKey, V pValue) {
        this.key = pKey;
        this.value = pValue;
    }

    @Override
    public String toString() {
        return this.getKey() + " = " + this.getValue();
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

}
