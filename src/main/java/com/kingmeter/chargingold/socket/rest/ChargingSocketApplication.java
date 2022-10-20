package com.kingmeter.chargingold.socket.rest;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.kingmeter.chargingold.socket.business.code.ServerFunctionCodeType;
import com.kingmeter.common.KingMeterException;
import com.kingmeter.common.KingMeterMarker;
import com.kingmeter.common.ResponseCode;
import com.kingmeter.dto.charging.v1.rest.response.ScanUnlockResponseRestDto;
import com.kingmeter.dto.charging.v1.socket.out.QueryDockLockStatusResponseDto;
import com.kingmeter.dto.charging.v1.rest.request.ScanUnlockRequestRestDto;
import com.kingmeter.dto.charging.v1.rest.response.ForceUnLockResponseRestDto;
import com.kingmeter.dto.charging.v1.rest.response.QueryDockInfoResponseRestDto;
import com.kingmeter.dto.charging.v1.rest.response.QueryDockLockStatusResponseRestDto;
import com.kingmeter.dto.charging.v1.socket.out.ForceUnLockResponseDto;
import com.kingmeter.dto.charging.v1.socket.out.QueryDockInfoResponseDto;
import com.kingmeter.dto.charging.v1.socket.out.ScanUnlockResponseDto;
import com.kingmeter.socket.framework.application.SocketApplication;
import com.kingmeter.socket.framework.util.CacheUtil;
import com.kingmeter.utils.HardWareUtils;
import io.netty.channel.socket.SocketChannel;
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

        socketApplication.sendSocketMsg(siteId,
                ServerFunctionCodeType.ScanUnLock,
                toJSON(response).toString());

        log.info(new KingMeterMarker("Socket,ScanUnLock,C102"),
                "{}|{}|{}|{}|{}", siteId, dockId, userId, restDto.getMinbsoc(),
                HardWareUtils.getInstance().getUtcTimeStampOnDevice(timezone));

        String key = "scan_" + userId;
        CacheUtil.getInstance().getDeviceResultMap().remove(key);

        //4,wait for lock response
        Map<String, String> result = socketApplication.waitForMapResult(key);
        return JSON.parseObject(result.get("ScanUnlock"), ScanUnlockResponseRestDto.class);
    }

    /**
     * 强制开锁
     *
     * @param siteId
     * @param dockId
     * @return
     */
    public ForceUnLockResponseRestDto foreUnlock(long siteId, long dockId) {
        ForceUnLockResponseDto responseDto =
                new ForceUnLockResponseDto(
                        dockId);
        log.info(new KingMeterMarker("Socket,ForceUnLock,C104"),
                "{}|{}|{}|{}",
                siteId, dockId,
                0, 0);

        String key = "force_" + dockId;
        CacheUtil.getInstance().getDeviceResultMap().remove(key);

        socketApplication.sendSocketMsg(siteId,
                ServerFunctionCodeType.ForceUnLock,
                JSONObject.toJSON(responseDto).toString());

        //4,wait for lock response
        Map<String, String> result = socketApplication.waitForMapResult(key);
        return JSON.parseObject(result.get("ForceUnlock"), ForceUnLockResponseRestDto.class);
    }

    /**
     * 查询硬件版本
     * @param siteId
     * @param dockId
     * @return
     */
    public QueryDockInfoResponseRestDto dealWithQueryDockInfo(long siteId, long dockId,String userId) {
        QueryDockInfoResponseDto queryDockInfoResponseDto =
                new QueryDockInfoResponseDto(dockId,userId);
        String key = siteId + "_QueryDockInfo";
        CacheUtil.getInstance().getDeviceResultMap().remove(key);

        socketApplication.sendSocketMsg(siteId,
                ServerFunctionCodeType.QueryDockInfo,
                toJSON(queryDockInfoResponseDto).toString());

        log.info(new KingMeterMarker("Socket,QueryDockInfo,C602"),
                "{}|{}", siteId, dockId);

        Map<String, String> result = socketApplication.waitForMapResult(key);
        return JSON.parseObject(result.get("dockArray"), QueryDockInfoResponseRestDto.class);
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
        CacheUtil.getInstance().getDeviceResultMap().remove(key);

        socketApplication.sendSocketMsg(siteId,
                ServerFunctionCodeType.CheckDockLockStatus,
                JSONObject.toJSON(responseDto).toString());

        log.info(new KingMeterMarker("Socket,CheckDockLockStatus,C702"),
                "{}|{}|{}", siteId, dockId, userId);

        Map<String, String> result = socketApplication.waitForMapResult(key);
        return JSON.parseObject(result.get("DockLockStatus"), QueryDockLockStatusResponseRestDto.class);
    }



    /**
     * 查询 硬件设备列表
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

}
