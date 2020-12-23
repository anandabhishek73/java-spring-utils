package com.abhishek.utils.collection.cache;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class PassThroughCache<K, V> {

    protected int capacity;
    protected FetchFromSource<K,V> callback;

    public PassThroughCache(FetchFromSource<K, V> callback){
        this(callback, 10);
    }
    public PassThroughCache(FetchFromSource<K, V> callback, int capacity ){
        log.debug("Creating new {} cache of capacity {}", getClass().getName(), capacity);
        this.capacity = capacity;
        this.callback = callback;
    }

    public interface FetchFromSource<K_ , V_ >{
        V_ fetch(K_ key);
    }

    public abstract V get(K key);
}

