package com.kingmeter.chargingold.socket.business.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HeartUpdateCount {
    /**
     * 从0开始，每次心跳递增1；
     * 站点心跳10秒钟一次，也就是说每10分钟会有60次心跳；
     * 那么该数值每到60次的时候，更新一次桩体信息，并将该数值从新赋值为1
     */
    public static Map<Long,Integer> siteHeartBeatCount =  new ConcurrentHashMap();
}
