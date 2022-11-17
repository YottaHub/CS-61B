package hashmap;

import java.util.*;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author Y
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    public Iterator<K> iterator() {
        return new MyHashMapIter();
    }

    private class MyHashMapIter implements Iterator<K> {
        int i = 0;
        Iterator<Node> bucket;

        public MyHashMapIter() {
            bucket = findNextBucket();
        }

        public boolean hasNext() {
            return bucket != null;
        }

        public K next() {
            K nextKey = bucket.next().key;
            if (!bucket.hasNext()) {
                bucket = findNextBucket();
            }
            return nextKey;
        }

        private Iterator<Node> findNextBucket() {
            Collection<Node> nextBucket;
            while (i < M) {
                nextBucket = buckets[i];
                if (!nextBucket.isEmpty()) {
                    return nextBucket.iterator();
                } else {
                    i++;
                }
            }
            return null;
        }
    }

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */
    private Collection<Node>[] buckets;
    private int N = 0;
    private int M;
    private double loadFactor;

    /** Constructors */
    public MyHashMap() {
        M = 16;
        loadFactor = 0.75;
        buckets = createTable(M);
    }

    public MyHashMap(int initialSize) {
        M = initialSize;
        loadFactor = 0.75;
        buckets = createTable(M);
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        M = initialSize;
        loadFactor = maxLoad;
        buckets = createTable(M);
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     * <p>
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     * <p>
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     * <p>
     * Override this method to use different data structures as
     * the underlying bucket type
     * <p>
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new LinkedList<>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        Collection<Node>[] table = new Collection[tableSize];
        for (int i = 0; i < M; i++) {
            table[i] = createBucket();
        }
        return table;
    }

    public void clear() {
        N = 0;
        M = 16;
        buckets = createTable(M);
    }

    public boolean containsKey(K key) {
        for (Node n : buckets[indexOf(key)]) {
            if (n.key.equals(key)) {
                return true;
            }
        }
        return false;
    }

    public V get(K key) {
        for (Node n : buckets[indexOf(key)]) {
            if (n.key.equals(key)) {
                return n.value;
            }
        }
        return null;
    }

    public int size() {
        return N;
    }

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for the key,
     * the old value is replaced.
     */
    public void put(K key, V value) {
        if (loadFactor < (double) N/M) {
            resize(2*M);
        }
        int i = indexOf(key);
        boolean found = false;
        if (!buckets[i].isEmpty()) {
            for (Node n : buckets[i]) {
                if (n.key.equals(key)) {
                    n.value = value;
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            N++;
            buckets[i].add(createNode(key, value));
        }
    }

    public Set<K> keySet() {
        Set<K> keys = new HashSet<>();
        for (Collection<Node> b : buckets) {
            for (Node n : b) {
                keys.add(n.key);
            }
        }
        return keys;
    }

    public V remove(K key) {
        int i = indexOf(key);
        Node found = null;
        for (Node n : buckets[i]) {
            if (n.key.equals(key)) {
                found = n;
                break;
            }
        }
        if (found != null) {
            N--;
            buckets[i].remove(found);
            return found.value;
        }
        return null;
    }

    public V remove(K key, V value) {
        int i = indexOf(key);
        Node found = null;
        for (Node n : buckets[i]) {
            if (n.key.equals(key) && n.value.equals(value)) {
                found = n;
                break;
            }
        }
        if (found != null) {
            N--;
            buckets[i].remove(found);
            return found.value;
        }
        return null;
    }

    private void resize(int capacity) {
        M = capacity;
        Collection<Node>[] container = createTable(M);
        for (Collection<Node> b : buckets) {
            for (Node n : b) {
                container[indexOf(n.key)].add(n);
            }
        }
        buckets = container;
    }

    private int indexOf(K key) {
        return Math.floorMod(key.hashCode(), M);
    }
}
