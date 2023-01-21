package cc.bukkitPlugin.pds.util;

public class Pair<K, V> {

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    private final K key;

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
