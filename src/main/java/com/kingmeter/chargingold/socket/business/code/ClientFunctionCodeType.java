package com.kingmeter.chargingold.socket.business.code;



import com.kingmeter.chargingold.socket.business.strategy.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 这里的都是指 站点 发送的 功能编码
 */
public enum ClientFunctionCodeType {

    LoginType(0xc001, LoginStrategy.class),//站点登录
    ScanUnLock(0xc101, ScanUnLockStrategy.class),//扫码开桩取车
    ForceUnLock(0xc103, ForceUnLockStrategy.class),//强制开锁
    BikeInDock(0xc201, BikeInDockStrategy.class),//车辆入桩
    SiteHeartBeat(0xc301,HeartBeatStrategy.class),//站点心跳
    SwingCardUnLock(0xc401,SwingCardUnLockStrategy.class),//刷卡开桩取车
    SwingCardConfirm(0xc403,SwingCardConfirmStrategy.class),//刷卡开桩请求确认
    QueryDockInfo(0xc601, QueryDockInfoStrategy.class),//查询桩体信息
    CheckDockLockStatus(0xc701, CheckDockLockStatusStrategy.class),//检测锁状态
    MalfunctionUpload(0xc901,MalfunctionUploadStrategy.class),//桩体故障上报

    QuerySiteInfo(0xf002,QuerySiteInfoStrategy.class),//站点设置信息上报
    ConfigureSiteInfo(0xf102,ConfigureSiteInfoRequestStrategy.class),//站点设置返回
    RestartSite(0xff02,RestartSiteStrategy.class);//重启站点返回

    private int value;
    private Class className;

    ClientFunctionCodeType(int value, Class className) {
        this.value = value;
        this.className = className;
    }

    public int value() {
        return value;
    }

    public Class getClassName (){
        return className;
    }

    static Map<Integer, ClientFunctionCodeType> enumMap = new HashMap();

    static {
        for (ClientFunctionCodeType type : ClientFunctionCodeType.values()) {
            enumMap.put(type.value(), type);
        }
    }

    public static ClientFunctionCodeType getEnum(Integer value) {
        return enumMap.get(value);
    }

    public static boolean containsValue(Integer value) {
        return enumMap.containsKey(value);
    }
}
