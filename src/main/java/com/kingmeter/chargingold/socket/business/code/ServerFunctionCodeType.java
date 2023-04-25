package com.kingmeter.chargingold.socket.business.code;

/**
 * 这里都是指的 服务器发送 的功能编码
 */
public interface ServerFunctionCodeType {
    byte[] LoginType = {(byte) 192, (byte) 2};//("C0 02"),//登录返回
    byte[] ScanUnLock = {(byte) 193, (byte) 2};//("C1 02"),//扫码开桩取车
    byte[] ForceUnLock = {(byte) 193, (byte) 4};//("C1 04"),//强制开锁
    byte[] ScanUnLockConfirm = {(byte) 193, (byte) 5};//("C1 05"),//扫码开桩取车确认
    byte[] BikeInDock = {(byte) 194, (byte) 2};//("C2 02"),//车辆入桩
    byte[] SiteHeartBeatBig = {(byte) 195, (byte) 2};//("C3 02"),//站点心跳 大包
    byte[] SiteHeartBeatSmall = {(byte) 195, (byte) 4};//("C3 04"),//站点心跳 小包
    byte[] SwingCardUnLock = {(byte) 196, (byte) 2};//("C4 02"),//刷卡租车应答
    byte[] SwingCardConfirm = {(byte) 196, (byte) 4};//("C4 04"),//刷卡开桩请求确认
    byte[] QueryDockInfo = {(byte) 198, (byte) 2};//("C6 02"),//查询桩体信息
    byte[] CheckDockLockStatus = {(byte) 199, (byte) 2};//("C7 02"),//检测锁状态
    byte[] DockMalfunctionUpload = {(byte) 201, (byte) 2};//("C9 02"),//桩体故障上报
    byte[] QueryDockBikeInfo = {(byte) 204, (byte) 2};//("CC 02"),//桩体车辆信息同步

    byte[] QuerySiteInfo = {(byte) 240, (byte) 1};//("f0 01"),//查询配置信息
    byte[] ConfigureSiteInfo = {(byte) 241, (byte) 1};//("f1 01"),//配置站点信息
    byte[] RestartSite = {(byte) 255, (byte) 1};//("ff 01"),//重启站点
    byte[] QueryVersionOfComponents = {(byte) 16, (byte) 1};//("10 01"),//重启站点
    byte[] ExchangeBootLoad = {(byte) 17, (byte) 1};//("11 01"),//boot load 模式切换
    byte[] QueryLog = {(byte) 208, (byte) 2};//("D0 02"),//查询日志
    byte[] OpenOrCloseLog = {(byte) 209, (byte) 2};//("D1 02"),//开启或关闭日志
    byte[] ClearLog = {(byte) 210, (byte) 2};//("D2 02"),//清空日志


    byte[] ScanUnLockII = {(byte) 193, (byte) 6};//("c1 06"),//扫码开锁二代
    byte[] ScanUnLockIIConfirm = {(byte) 193, (byte) 8};//("c1 08"),//扫码开锁二代确认
    byte[] RemoteLock = {(byte) 193, (byte) 9};//("C1 09"),//远程关锁
    byte[] RemoteLockConfirm = {(byte) 193, (byte) 11};//("C1 0B"),//远程关锁确认
}
