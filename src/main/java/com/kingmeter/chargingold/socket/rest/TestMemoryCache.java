package com.kingmeter.chargingold.socket.rest;

import lombok.Data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


@Data
public class TestMemoryCache {

    private static TestMemoryCache instance;

    private TestMemoryCache() {
    }

    public static synchronized TestMemoryCache getInstance() {
        if (instance == null) {
            synchronized (TestMemoryCache.class) {
                if (instance == null) {
                    instance = new TestMemoryCache();
                }
            }
        }
        return instance;
    }

    public  Map<Long, Boolean> unlockFlag = Collections.synchronizedMap(new HashMap());
    public  Map<Long, Boolean> checkLockFlag = Collections.synchronizedMap(new HashMap());
    public  ConcurrentMap<Long, TestUnLockDto> testForceLockInfoMap = new ConcurrentHashMap<>();
    public  ConcurrentMap<Long, TestRemoteLockDto> testRemoteLockInfoMap = new ConcurrentHashMap<>();
}
