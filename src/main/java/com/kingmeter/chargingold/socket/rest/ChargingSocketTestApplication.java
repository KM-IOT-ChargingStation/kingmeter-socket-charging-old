package com.kingmeter.chargingold.socket.rest;


import com.alibaba.fastjson.JSON;
import com.kingmeter.chargingold.socket.business.code.ServerFunctionCodeType;
import com.kingmeter.common.KingMeterMarker;
import com.kingmeter.dto.charging.v1.rest.response.ForceUnLockResponseRestDto;
import com.kingmeter.dto.charging.v2.socket.in.vo.DockStateInfoFromHeartBeatVO;
import com.kingmeter.dto.charging.v2.socket.out.ForceUnLockResponseDto;
import com.kingmeter.socket.framework.application.SocketApplication;
import com.kingmeter.socket.framework.util.CacheUtil;
import com.kingmeter.utils.HardWareUtils;
import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.alibaba.fastjson.JSON.toJSON;


@Slf4j
@Service
public class ChargingSocketTestApplication {

    @Autowired
    private SocketApplication socketApplication;


    public void stopUnlock(long siteId) {
        TestMemoryCache.getInstance().getUnlockFlag().remove(siteId);
    }


    public String batchUnlock(long siteId, int times,
                              long perSite, long perDock) {

        TestMemoryCache.getInstance().getUnlockFlag().put(siteId, true);

        new Thread(new TestUnlockPerTime(siteId, times, perSite, perDock)).start();

        return "batch unlock succeed";
    }


    public void stopCheckDockLock(long siteId) {
        TestMemoryCache.getInstance().getCheckLockFlag().remove(siteId);
    }

    public String batchCheckDockLock(long siteId, int times,
                                     long perSite, long perDock) {
        TestMemoryCache.getInstance().getCheckLockFlag().put(siteId, true);

        new Thread(new TestCheckDockLockPerTime(siteId, times, perSite, perDock)).start();

        return "batch check dock lock succeed";
    }

    class TestCheckDockLockPerTime implements Runnable {

        private long siteId;
        private int times;
        private long perSite;
        private long perDock;

        public TestCheckDockLockPerTime(long siteId, int times,
                                        long perSite, long perDock) {
            this.siteId = siteId;
            this.times = times;
            this.perSite = perSite;
            this.perDock = perDock;
        }

        public void run() {
            for (int i = 0; i < times; i++) {
                if (TestMemoryCache.getInstance().getCheckLockFlag().containsKey(siteId) &&
                        TestMemoryCache.getInstance().getCheckLockFlag().get(siteId)) {
                    Map<String, String> siteMap = CacheUtil.getInstance().getDeviceInfoMap().get(
                            siteId
                    );
                    List<DockStateInfoFromHeartBeatVO> stateList = JSON.parseArray(siteMap.get("dockArray"), DockStateInfoFromHeartBeatVO.class);

                    boolean flag = checkDockLockSingle(siteId, stateList, perDock);
                    if (!flag) {
                        TestMemoryCache.getInstance().getCheckLockFlag().remove(siteId);
                        break;
                    }
                    try {
                        Thread.sleep(perSite);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    break;
                }
            }
        }
    }

    private boolean checkDockLockSingle(long siteId, List<DockStateInfoFromHeartBeatVO> stateList, long perDock) {
        try {
            String userId = "123";

            Map<String, String> siteMap = CacheUtil.getInstance().getDeviceInfoMap().get(
                    siteId
            );

            int timezone = Integer.parseInt(siteMap.get("timezone"));

            for (DockStateInfoFromHeartBeatVO vo : stateList) {
                ForceUnLockResponseDto response = new
                        ForceUnLockResponseDto(vo.getKid(), userId,
                        HardWareUtils.getInstance().getUtcTimeStampOnDevice(timezone));

                socketApplication.sendSocketMsg(siteId,
                        ServerFunctionCodeType.CheckDockLockStatus,
                        toJSON(response).toString());

                log.info(new KingMeterMarker("Socket,CheckDockLockStatus,C702"),
                        "{}|{}|{}",
                        siteId, vo.getKid(), userId);

                try {
                    Thread.sleep(perDock);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    class TestUnlockPerTime implements Runnable {

        private long siteId;
        private int times;
        private long perSite;
        private long perDock;

        public TestUnlockPerTime(long siteId, int times,
                                 long perSite, long perDock) {
            this.siteId = siteId;
            this.times = times;
            this.perSite = perSite;
            this.perDock = perDock;
        }

        public void run() {
            for (int i = 0; i < times; i++) {
                if (TestMemoryCache.getInstance().getUnlockFlag().containsKey(siteId) &&
                        TestMemoryCache.getInstance().getUnlockFlag().get(siteId)) {
                    Map<String, String> siteMap = CacheUtil.getInstance().getDeviceInfoMap().get(
                            siteId
                    );
                    List<DockStateInfoFromHeartBeatVO> stateList = JSON.parseArray(siteMap.get("dockArray"), DockStateInfoFromHeartBeatVO.class);

                    boolean flag = unlockSingle(siteId, stateList, perDock);
                    if (!flag) {
                        TestMemoryCache.getInstance().getUnlockFlag().remove(siteId);
                        break;
                    }
                    try {
                        Thread.sleep(perSite);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    break;
                }
            }
        }
    }

    private boolean unlockSingle(long siteId, List<DockStateInfoFromHeartBeatVO> stateList, long perDock) {
        try {
            String userId = "123";

            Map<String, String> siteMap = CacheUtil.getInstance().getDeviceInfoMap().get(
                    siteId
            );

            int timezone = Integer.parseInt(siteMap.get("timezone"));

            for (DockStateInfoFromHeartBeatVO vo : stateList) {
                ForceUnLockResponseDto response = new
                        ForceUnLockResponseDto(vo.getKid(), userId,
                        HardWareUtils.getInstance().getUtcTimeStampOnDevice(timezone));

                String key = "force_" + vo.getKid();

                TestUnLockDto unLockDto = new TestUnLockDto();
                unLockDto.setSiteId(siteId);
                unLockDto.setDockId(vo.getKid());
                unLockDto.setStartTimeStamp(System.currentTimeMillis());
                TestMemoryCache.getInstance().getTestForceLockInfoMap().put(vo.getKid(),unLockDto);

                SocketChannel channel = socketApplication.sendSocketMsg(siteId,
                        ServerFunctionCodeType.ForceUnLock,
                        toJSON(response).toString());

                log.info(new KingMeterMarker("Socket,ForceUnLock,C104"),
                        "{}|{}|{}|{}",
                        siteId, vo.getKid(),
                        userId, response.getTim());

                socketApplication.waitForPromiseResult(key, channel);

                try {
                    Thread.sleep(perDock);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
