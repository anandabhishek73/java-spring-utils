package com.abhishek.utils.collection.cache;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class LRUCache<K, V> extends PassThroughCache<K, V> {
    private int currentSize;
    private int cacheHit = 0;
    private int cacheMiss = 0;


    private Slot<K, V> mruSlot;
    private Slot<K, V> lruSlot;

    private Map<K, Slot<K, V>> cache;

    public LRUCache(FetchFromSource<K, V> callback, int capacity) {
        super(callback, capacity);
        this.cache = new HashMap<>();
    }

    public LRUCache(FetchFromSource<K, V> callback) {
        super(callback);
    }

    public float getCacheEffeciency() {
        return (float) cacheHit / (cacheHit + cacheMiss);
    }

    @Override
    public V get(K key) {
        Slot<K, V> cachedSlot = cache.get(key);

        if (cachedSlot == null) {   // cache miss
            log.debug("Cache miss for key {}", key);
            cacheMiss++;
            V value = callback.fetch(key);
            if (currentSize < capacity) {
                cachedSlot = createNewSlot(key, value);
            } else {
                log.trace("Removing LRU slot : {} from cache ", lruSlot);
                cache.remove(lruSlot.getKey());
                lruSlot.setKey(key);
                lruSlot.setValue(value);
                moveToMRU(lruSlot);
                cachedSlot = mruSlot;
            }
            log.trace("Putting MRU slot : {} into cache ", mruSlot);
            cache.put(key, mruSlot);
        } else {
            log.debug("Cache hit for key {}", key);
            cacheHit++;
            moveToMRU(cachedSlot);
        }
        return cachedSlot.getValue();
    }

    private Slot<K, V> createNewSlot(K key, V value) {
        Slot<K, V> newSlot = new Slot<>(key, value);
        if (mruSlot == null) {  // First slot created in cache
            log.trace("Initializing mruSlot with {}", newSlot);
            mruSlot = newSlot;
            log.trace("Initializing lruSlot with {}", newSlot);
            lruSlot = newSlot;
        } else {
            log.trace("Pushing new Slot {} in front of mru slot {}", newSlot, mruSlot);
            mruSlot.recentSlot = newSlot;
            newSlot.olderSlot = mruSlot;
            mruSlot = newSlot;
            log.trace("Pushed new Slot {} in front of mru slot {}", newSlot, mruSlot);
        }
        this.currentSize++;
        log.trace("Created a new Slot {}. Cache size is now {}", newSlot, currentSize);
        return newSlot;
    }

    private void moveToMRU(Slot<K, V> slot) {

        // Removal from current position
        if (slot.recentSlot == null) {  // already at mru
            return;
        } else {
            slot.recentSlot.olderSlot = slot.olderSlot;
        }

        if (slot.olderSlot == null) {   // it was an LRU slot
            lruSlot = slot.recentSlot;
        } else {
            slot.olderSlot.recentSlot = slot.recentSlot;
        }

        // Insertion into head MRU
        slot.recentSlot = null;
        slot.olderSlot = mruSlot;
        mruSlot.recentSlot = slot;

        mruSlot = slot;
    }


    private static class Slot<K_, V_> {
        static int slotInstanceCounter = 0;

        private K_ key;
        private V_ value;
        Slot<K_, V_> recentSlot;
        Slot<K_, V_> olderSlot;

        Slot(K_ key, V_ value) {
            log.trace("Created #{} instance of {} class.", ++slotInstanceCounter, this.getClass());
            this.setKey(key);
            this.setValue(value);
        }

        @Override
        public String toString() {
            return "[" +
                    (recentSlot == null ? "null" : String.valueOf(recentSlot.getKey())) +
                    " <- (" + getKey() + ") -> " +
                    (olderSlot == null ? "null" : String.valueOf(olderSlot.getKey())) +
                    "]";
        }

        public K_ getKey() {
            return key;
        }

        public void setKey(K_ key) {
            K_ oldKey = this.key;
            log.trace("Replacing old key {} with new key {} for slot {}", oldKey, key, this);
            this.key = key;
        }

        public V_ getValue() {
            return value;
        }

        public void setValue(V_ value) {
            V_ oldValue = this.value;
            log.trace("Replacing old value {} with new value {} for slot {}", oldValue, value, this);
            this.value = value;
        }
    }

    @Override
    public String toString() {
        StringBuilder header = new StringBuilder("LRUCache ")
                .append("(Size ").append(currentSize).append("/").append(capacity).append(")")
                .append("(Efficiency ").append(getCacheEffeciency() * 100).append("%").append(")")
                .append("{");
        Slot<K, V> nextSlot = mruSlot;
        while (nextSlot != null) {
            header.append("\n").append(nextSlot);
            nextSlot = nextSlot.olderSlot;
        }
        return header.append("}").toString();
    }
}
