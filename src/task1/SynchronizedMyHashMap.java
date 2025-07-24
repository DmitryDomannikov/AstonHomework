package task1;

import java.util.Objects;

public class SynchronizedMyHashMap<K, V> {
    private static final int DEFAULT_CAPACITY = 16;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    private Node<K, V>[] table;
    private int size;
    private final float loadFactor;

    // Конструктор
    public SynchronizedMyHashMap() {
        this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR);
    }
    public SynchronizedMyHashMap(int capacity, float loadFactor) {
        this.table = new Node[capacity];
        this.loadFactor = loadFactor;
    }

    // Узел для хранения данных
    private static class Node<K, V> {
        final K key;
        V value;
        Node<K, V> next;

        Node(K key, V value, Node<K, V> next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    // Вычисление индекса
    private int getIndex(K key) {
        if (key == null) return 0;
        int hash = Objects.hashCode(key);
        return (hash & 0x7FFFFFFF) % table.length;
    }

    // Проверка необходимости ресайза
    private void resizeIfNeeded() {
        if (size >= table.length * loadFactor) {
            int newCapacity = table.length * 2;
            Node<K, V>[] newTable = new Node[newCapacity];

            for (Node<K, V> node : table) {
                while (node != null) {
                    Node<K, V> next = node.next;
                    int newIndex = (node.key == null) ? 0 :
                            (Objects.hashCode(node.key) & 0x7FFFFFFF) % newCapacity;
                    node.next = newTable[newIndex];
                    newTable[newIndex] = node;
                    node = next;
                }
            }
            table = newTable;
        }
    }

    //  put()
    public synchronized V put(K key, V value) {
        resizeIfNeeded();

        int index = getIndex(key);
        Node<K, V> node = table[index];
        // Поиск существующего ключа
        while (node != null) {
            if (Objects.equals(key, node.key)) {
                V oldValue = node.value;
                node.value = value;
                return oldValue;
            }
            node = node.next;
        }
        // Добавление нового узла
        table[index] = new Node<>(key, value, table[index]);
        size++;
        return null;
    }
    // get()
    public synchronized V get(K key) {
        int index = getIndex(key);
        Node<K, V> node = table[index];

        while (node != null) {
            if (Objects.equals(key, node.key)) {
                return node.value;
            }
            node = node.next;
        }
        return null;
    }
    //  remove()
    public synchronized V remove(K key) {
        int index = getIndex(key);
        Node<K, V> node = table[index];
        Node<K, V> prev = null;

        while (node != null) {
            if (Objects.equals(key, node.key)) {
                if (prev == null) {
                    table[index] = node.next;
                } else {
                    prev.next = node.next;
                }
                size--;
                return node.value;
            }
            prev = node;
            node = node.next;
        }
        return null;
    }
    // size()
    public synchronized int size() {
        return size;
    }
    // проверка на пустоту
    public synchronized boolean isEmpty() {
        return size == 0;
    }
}

