package com.kingmeter.chargingold.socket.rest;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.kingmeter.chargingold.socket.business.code.ServerFunctionCodeType;
import com.kingmeter.common.KingMeterException;
import com.kingmeter.common.KingMeterMarker;
import com.kingmeter.common.ResponseCode;
import com.kingmeter.dto.charging.v1.rest.request.*;
import com.kingmeter.dto.charging.v1.rest.response.*;
import com.kingmeter.dto.charging.v1.socket.in.*;
import com.kingmeter.dto.charging.v1.socket.out.*;
import com.kingmeter.socket.framework.application.SocketApplication;
import com.kingmeter.socket.framework.dto.ResponseBody;
import com.kingmeter.socket.framework.util.CacheUtil;
import com.kingmeter.utils.HardWareUtils;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.alibaba.fastjson.JSON.toJSON;


@Slf4j
@Service
public class ChargingSocketApplication {

    @Autowired
    private SocketApplication socketApplication;

    /**
     * 扫码租车
     *
     * @param restDto
     */
    public ScanUnlockResponseRestDto scanUnlock(ScanUnlockRequestRestDto restDto) {
        long siteId = restDto.getSiteId();
        long dockId = restDto.getDockId();
        String userId = restDto.getUserId();

        int timezone = getTimezone(siteId);

        ScanUnlockResponseDto response = new
                ScanUnlockResponseDto(dockId, userId, restDto.getMinbsoc(),
                HardWareUtils.getInstance().getUtcTimeStampOnDevice(timezone));

        SocketChannel channel = socketApplication.sendSocketMsg(siteId,
                ServerFunctionCodeType.ScanUnLock,
                toJSON(response).toString());

        log.info(new KingMeterMarker("Socket,ScanUnLock,C102"),
                "{}|{}|{}|{}|{}", siteId, dockId, userId, restDto.getMinbsoc(),
                HardWareUtils.getInstance().getUtcTimeStampOnDevice(timezone));

        String key = "scan_" + userId + "_" + dockId;

        return (ScanUnlockResponseRestDto) socketApplication.waitForPromiseResult(key, channel);

//        CacheUtil.getInstance().getDeviceResultMap().remove(key);
//        //4,wait for lock response
//        Map<String, String> result = socketApplication.waitForMapResult(key);
//        return JSON.parseObject(result.get("ScanUnlock"), ScanUnlockResponseRestDto.class);
    }

    /**
     * 强制开锁
     *
     * @param siteId
     * @param dockId
     * @return
     */
    public ForceUnLockResponseRestDto foreUnlock(long siteId, long dockId, String userId) {
        ForceUnLockResponseDto responseDto =
                new ForceUnLockResponseDto(
                        dockId, userId);
        log.info(new KingMeterMarker("Socket,ForceUnLock,C104"),
                "{}|{}|{}|{}",
                siteId, dockId,
                userId, 0);

        String key = "force_" + dockId;
//        CacheUtil.getInstance().getDeviceResultMap().remove(key);

        SocketChannel channel = socketApplication.sendSocketMsg(siteId,
                ServerFunctionCodeType.ForceUnLock,
                JSONObject.toJSON(responseDto).toString());

        return (ForceUnLockResponseRestDto)socketApplication.waitForPromiseResult(key, channel);

//        //4,wait for lock response
//        Map<String, String> result = socketApplication.waitForMapResult(key);
//        return JSON.parseObject(result.get("ForceUnlock"), ForceUnLockResponseRestDto.class);
    }

    /**
     * 查询硬件版本
     *
     * @param siteId
     * @param dockId
     * @return
     */
    public QueryDockInfoResponseRestDto dealWithQueryDockInfo(long siteId, long dockId, String userId) {
        QueryDockInfoResponseDto queryDockInfoResponseDto =
                new QueryDockInfoResponseDto(dockId, userId);
        String key = siteId + "_QueryDockInfo";
//        CacheUtil.getInstance().getDeviceResultMap().remove(key);

        SocketChannel channel = socketApplication.sendSocketMsg(siteId,
                ServerFunctionCodeType.QueryDockInfo,
                toJSON(queryDockInfoResponseDto).toString());

        log.info(new KingMeterMarker("Socket,QueryDockInfo,C602"),
                "{}|{}", siteId, dockId);

        return (QueryDockInfoResponseRestDto)socketApplication.waitForPromiseResult(key, channel);

//        Map<String, String> result = socketApplication.waitForMapResult(key);
//        return JSON.parseObject(result.get("dockArray"), QueryDockInfoResponseRestDto.class);
    }

    public void dealWithQueryDockInfo2times(long siteId, long dockId, String userId, int times) {
        QueryDockInfoResponseDto queryDockInfoResponseDto =
                new QueryDockInfoResponseDto(dockId, userId);

        log.info(new KingMeterMarker("Socket,QueryDockInfo,C602"),
                "{}|{}", siteId, dockId);

        ResponseBody responseBody = socketApplication.createResponseBody(siteId,
                ServerFunctionCodeType.QueryDockInfo,
                toJSON(queryDockInfoResponseDto).toString());

        List<ResponseBody> list = new ArrayList<>();
        for (int i = 0; i < times; i++) {
            list.add(responseBody);
        }


        socketApplication.sendSocketMsg(String.valueOf(siteId), list);
    }

    /**
     * 查询桩体锁状态
     *
     * @param siteId
     * @param dockId
     * @param userId
     * @return
     */
    public QueryDockLockStatusResponseRestDto queryDockLockStatus(long siteId, long dockId, String userId) {
        QueryDockLockStatusResponseDto responseDto =
                new QueryDockLockStatusResponseDto(dockId,
                        userId);

        String key = siteId + "_QueryDockLockStatus";
//        CacheUtil.getInstance().getDeviceResultMap().remove(key);

        SocketChannel channel = socketApplication.sendSocketMsg(siteId,
                ServerFunctionCodeType.CheckDockLockStatus,
                JSONObject.toJSON(responseDto).toString());

        log.info(new KingMeterMarker("Socket,CheckDockLockStatus,C702"),
                "{}|{}|{}", siteId, dockId, userId);


        return (QueryDockLockStatusResponseRestDto)socketApplication.waitForPromiseResult(key, channel);

//        Map<String, String> result = socketApplication.waitForMapResult(key);
//        return JSON.parseObject(result.get("DockLockStatus"), QueryDockLockStatusResponseRestDto.class);
    }


    public QuerySiteInfoRequestDto querySiteInfo(long siteId) {
        log.info(new KingMeterMarker("Socket,QuerySiteInfo,F001"),
                "{}", siteId);

        QuerySiteInfoResponseDto querySiteInfoResponseDto = new QuerySiteInfoResponseDto(siteId);
        SocketChannel channel = socketApplication.sendSocketMsg(siteId,
                ServerFunctionCodeType.QuerySiteInfo,
                toJSON(querySiteInfoResponseDto).toString());
        String key = "query_site_info_" + siteId;

        return (QuerySiteInfoRequestDto)socketApplication.waitForPromiseResult(key, channel);

//        Map<String, String> result = socketApplication.waitForMapResult(key);
//        return JSON.parseObject(result.get("siteInfo"), QuerySiteInfoResponseRestDto.class);
    }

    public ConfigureSiteInfoRequestDto configureSiteInfo(ConfigureSiteInfoRequestRestDto restDto) {
        long siteId = restDto.getSite_id();

        ConfigureSiteInfoResponseDto responseDto
                = new ConfigureSiteInfoResponseDto(siteId, restDto.getSite_password(),
                restDto.getLogin_ip(), restDto.getLogin_port(),
                restDto.getNet_wifiname(), restDto.getNet_wifipsd());


        log.info(new KingMeterMarker("Socket,ConfigureSiteInfo,F101"),
                "{}|{}|{}|{}|{}", siteId, restDto.getLogin_ip(), restDto.getLogin_port(),
                restDto.getNet_wifiname(), restDto.getNet_wifipsd());

        SocketChannel channel = socketApplication.sendSocketMsg(siteId,
                ServerFunctionCodeType.ConfigureSiteInfo,
                toJSON(responseDto).toString());
        String key = "configure_site_info_" + siteId;

        return (ConfigureSiteInfoRequestDto)socketApplication.waitForPromiseResult(key, channel);

//        Map<String, String> result = socketApplication.waitForMapResult(key);
//        return JSON.parseObject(result.get("siteInfo"), ConfigureSiteInfoResponseRestDto.class);
    }


    public void configureSiteInfo2times(ConfigureSiteInfoRequestRestDto restDto) {
        long siteId = restDto.getSite_id();

        ConfigureSiteInfoResponseDto responseDto
                = new ConfigureSiteInfoResponseDto(siteId, restDto.getSite_password(),
                restDto.getLogin_ip(), restDto.getLogin_port(),
                restDto.getNet_wifiname(), restDto.getNet_wifipsd());


        log.info(new KingMeterMarker("Socket,ConfigureSiteInfo,F101"),
                "{}|{}|{}|{}|{}", siteId, restDto.getLogin_ip(), restDto.getLogin_port(),
                restDto.getNet_wifiname(), restDto.getNet_wifipsd());

        ResponseBody responseBody = socketApplication.createResponseBody(siteId,
                ServerFunctionCodeType.ConfigureSiteInfo,
                toJSON(responseDto).toString());
        List<ResponseBody> list = new ArrayList<>();
        for (int i = 0; i < restDto.getSucc_count(); i++) {
            list.add(responseBody);
        }


        socketApplication.sendSocketMsg(String.valueOf(siteId), list);
    }


    public RestartSiteResponseRestDto restartSite(long siteId) {
        RestartSiteResponseDto responseDto
                = new RestartSiteResponseDto(siteId);
        socketApplication.sendSocketMsg(siteId,
                ServerFunctionCodeType.RestartSite,
                toJSON(responseDto).toString());

        log.info(new KingMeterMarker("Socket,RestartSite,FF01"),
                "{}", siteId);

//        try{
//            Thread.sleep(1000);
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//        CacheUtil.getInstance().getDeviceIdAndChannelMap().get(String.valueOf(siteId)).close();

        return null;
//        String key = "restart_site_reply_" + siteId;
//        Map<String, String> result = socketApplication.waitForMapResult(key);
//        return JSON.parseObject(result.get("siteInfo"), RestartSiteResponseRestDto.class);
    }

    /**
     * 查询 硬件设备列表
     *
     * @return
     */
    public List<ConnectionDto> queryConnection() {
        List<ConnectionDto> result = new ArrayList<>();
        Map<String, SocketChannel> map = CacheUtil.getInstance().getDeviceIdAndChannelMap();
        for (Map.Entry<String, SocketChannel> entry : map.entrySet()) {
            String deviceId = entry.getKey();
            SocketChannel channel = entry.getValue();
            result.add(new ConnectionDto(deviceId, channel.remoteAddress().getHostString(),
                    channel.remoteAddress().getPort()));
        }
        return result;
    }


    private int getTimezone(long siteId) {
        Map<String, String> siteMap = CacheUtil.getInstance().getDeviceInfoMap().get(
                siteId
        );
        if (siteMap == null) throw new KingMeterException(ResponseCode.Device_Not_Logon);

        return Integer.parseInt(siteMap.get("timezone"));
    }

    /**
     * this is for wifimaster v4.5
     *
     * @param restDto
     */
    public QueryVersionOfComponentsRequestDto queryVersionOfComponents(
            QueryVersionOfComponentsRequestRestDto restDto) {
        long siteId = restDto.getSite_id();
        QueryVersionOfComponentsResponseDto responseDto
                = new QueryVersionOfComponentsResponseDto(
                siteId, restDto.getUpdate_dev());
        SocketChannel channel = socketApplication.sendSocketMsg(siteId,
                ServerFunctionCodeType.QueryVersionOfComponents,
                toJSON(responseDto).toString());

        log.info(new KingMeterMarker("Socket,QueryVersionOfComponents,1001"),
                "{}", siteId);

        String key = "query_site_version_reply_" + siteId;

        return (QueryVersionOfComponentsRequestDto)socketApplication.waitForPromiseResult(key,channel);
//
//        Map<String, String> result = socketApplication.waitForMapResult(key);
//        return JSON.parseObject(result.get("siteInfo"), QueryVersionOfComponentsResponseRestDto.class);
    }

    /**
     * 切换 boot load 模式
     *
     * @param restDto
     * @return
     */
    public ExchangeBootLoadResponseRestDto exchangeBootLoad(ExchangeBootLoadRequestRestDto restDto) {
        long siteId = restDto.getSite_id();
        ExchangeBootLoadResponseDto responseDto
                = new ExchangeBootLoadResponseDto(
                siteId, restDto.getEnter_bld());
        SocketChannel channel = socketApplication.sendSocketMsg(siteId,
                ServerFunctionCodeType.ExchangeBootLoad,
                toJSON(responseDto).toString());

        log.info(new KingMeterMarker("Socket,ExchangeBootLoad,1101"),
                "{}|{}", siteId, restDto.getEnter_bld());

        String key = "exchange_boot_load_reply_" + siteId;

        return (ExchangeBootLoadResponseRestDto)socketApplication.waitForPromiseResult(key,channel);

//        Map<String, String> result = socketApplication.waitForMapResult(key);
//        return JSON.parseObject(result.get("siteInfo"), ExchangeBootLoadResponseRestDto.class);
    }

    //查询日志
    public QueryLogRequestRestDto queryLog(long siteId) {
        QueryLogResponseDto responseDto =
                new QueryLogResponseDto(siteId);
        SocketChannel channel = socketApplication.sendSocketMsg(siteId,
                ServerFunctionCodeType.QueryLog,
                toJSON(responseDto).toString());
        log.info(new KingMeterMarker("Socket,QueryLog,D002"),
                "{}", siteId);

        String key = "query_site_log_" + siteId;
        return (QueryLogRequestRestDto) socketApplication.waitForPromiseResult(key, channel);
    }

    //开启或关闭日志
    public OpenOrCloseLogRequestDto openOrCloseLog(long siteId, int flag) {
        OpenOrCLoseLogResponseDto responseDto =
                new OpenOrCLoseLogResponseDto(siteId, flag);
        SocketChannel channel = socketApplication.sendSocketMsg(siteId,
                ServerFunctionCodeType.OpenOrCloseLog,
                toJSON(responseDto).toString());
        log.info(new KingMeterMarker("Socket,QueryLog,D102"),
                "{}", siteId);

        String key = "open_close_site_log_" + siteId;

        return (OpenOrCloseLogRequestDto) socketApplication.waitForPromiseResult(key, channel);

//        Map<String, String> result = socketApplication.waitForMapResult(key);
//        return JSON.parseObject(result.get("result"), OpenOrCloseLogRequestDto.class);
    }

    //清除日志
    public ClearLogRequestDto clearLog(long siteId) {
        ClearLogResponseDto responseDto =
                new ClearLogResponseDto(siteId);
        SocketChannel channel = socketApplication.sendSocketMsg(siteId,
                ServerFunctionCodeType.ClearLog,
                toJSON(responseDto).toString());
        log.info(new KingMeterMarker("Socket,QueryLog,D202"),
                "{}", siteId);

        String key = "clear_site_log_" + siteId;

        return (ClearLogRequestDto)socketApplication.waitForPromiseResult(key,channel);

//        Map<String, String> result = socketApplication.waitForMapResult(key);
//        return JSON.parseObject(result.get("result"), ClearLogRequestDto.class);
    }

}
