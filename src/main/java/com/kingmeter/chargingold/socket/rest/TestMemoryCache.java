package com.kingmeter.chargingold.socket.rest;

import lombok.Data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


@Data
public class TestMemoryCache {

    private volatile static TestMemoryCache instance;

    private TestMemoryCache() {
    }

    public static TestMemoryCache getInstance() {
        if (instance == null) {
            synchronized (TestMemoryCache.class) {
                if (instance == null) {
                    instance = new TestMemoryCache();
                }
            }
        }
        return instance;
    }

    public volatile Map<Long, Boolean> unlockFlag = Collections.synchronizedMap(new HashMap());
    public volatile Map<Long, Boolean> checkLockFlag = Collections.synchronizedMap(new HashMap());
    public volatile ConcurrentMap<Long, TestUnLockDto> testForceLockInfoMap = new ConcurrentHashMap<>();
}
