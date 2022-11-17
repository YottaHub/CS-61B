package bstmap;

import java.util.*;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {
    /** The number of Key-Value mappings. */
    private int size;
    /** The Root of the Binary Search Tree. */
    private BSTNode root;

    /** Initial an empty BSTMap */
    public BSTMap() {
        size = 0;
        root = null;
    }

    /** Removes all the mappings from this map. */
    @Override
    public void clear() {
        size = 0;
        root = null;
    }

    /** Return KEY's corresponding key-value mapping or its theoretical
     *  parent in this BST if no such KEY. */
    private BSTNode search(K key) {
        BSTNode n = root;
        BSTNode p = null;
        while (n != null) {
            p = n;
            int cmp = n.key.compareTo(key);
            if (cmp > 0) {
                n = n.left;
            } else if (cmp < 0) {
                n = n .right;
            } else {
                return n;
            }
        }
        return p;
    }

    /** Returns true if and only if this dictionary contains KEY as the
     *  key of some key-value pair. */
    @Override
    public boolean containsKey(K key) {
        BSTNode n = search(key);
        return n != null && n.key.compareTo(key) == 0;
    }

    /** Returns the value to which the specified key is mapped, or null if this
     *  map contains no mapping for the key.
     */
    @Override
    public V get(K key) {
        BSTNode n = search(key);
        return (n != null && n.key.compareTo(key) == 0) ? n.val : null;
    }

    /** Returns the number of key-value mappings in this map. */
    @Override
    public int size() {
        return size;
    }

    /** Inserts the key-value pair of KEY and VALUE into this dictionary,
     *  replacing the previous value associated to KEY, if any. */
    @Override
    public void put(K key, V value) {
        if (size == 0) {
            root = new BSTNode(key, value);
        } else {
            BSTNode n = search(key);
            int cmp = n.key.compareTo(key);
            if (cmp > 0) {
                n.left = new BSTNode(key, value);
            } else if (cmp < 0) {
                n.right = new BSTNode(key, value);
            } else {
                n.val = value;
            }
        }
        size++;
    }

    @Override
    public Set<K> keySet() {
        Set<K> s = new HashSet<>();
        for (K k : this) {
            s.add(k);
        }
        return s;
    }

    /** Removes KEY's mapping, and return the VALUE or null if no such key. */
    @Override
    public V remove(K key) {
        BSTNode n = search(key);
        if (n == null || n.key.compareTo(key) != 0) {
            // set doesn't contain KEY
            return null;
        } else {
            size--;
            this.root = remove(key, root);
            return n.val;
        }
    }

    private BSTNode remove(K key, BSTNode cur) {
        int cmp = cur.key.compareTo(key);
        if (cmp > 0) {
            cur.left = remove(key, cur.left);
        } else if (cmp < 0) {
            cur.right = remove(key, cur.right);
        } else {
            if (cur.left == null) {
                return cur.right;
            } else if ( cur.right== null) {
                return cur.left;
            } else {
                BSTNode p = cur.right;
                if (p.left == null) {
                    p.left = cur.left;
                    return p;
                }
                BSTNode h = p.left;
                while (h.left != null) {
                    p = h;
                    h = h.left;
                }
                p.left = null;
                cur.key = h.key;
                cur.val = h.val;
            }
        }
        return cur;
    }

    /** Removes the key-value mapping, and return the VALUE or null if
     *  no such mapping. */
    @Override
    public V remove(K key, V value) {
        BSTNode n = search(key);
        if (n == null || n.key.compareTo(key) != 0 || n.val != value) {
            // set doesn't contain this mapping
            return null;
        } else {
            size--;
            remove(key, root);
            return n.val;
        }
    }

    /** An iterator that iterates over the keys of the dictionary
     *  in increasing order. */
    @Override
    public Iterator<K> iterator() {
        ArrayList<K> keys = new ArrayList<>();
        Stack<BSTNode> stk = new Stack<>();
        stk.push(root);
        while (!stk.isEmpty()) {
            BSTNode n = stk.pop();
            if (n == null) {
                keys.add(stk.pop().key);
            } else {
                if (n.right != null) {
                    stk.push(n.right);
                }
                stk.push(n);
                stk.push(null);
                if (n.left != null) {
                    stk.push(n.left);
                }
            }
        }
        return keys.iterator();
    }

    /** Represents one node in the BST that stores the key-value pairs
     *  in the dictionary. */
    private class BSTNode {
        private K key;
        private V val;
        private BSTNode left;
        private BSTNode right;

        private BSTNode(K k, V v) {
            this.key = k;
            this.val = v;
        }
    }
}
