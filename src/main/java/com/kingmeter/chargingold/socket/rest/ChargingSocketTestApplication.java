package com.kingmeter.chargingold.socket.rest;


import com.alibaba.fastjson.JSON;
import com.kingmeter.chargingold.socket.business.code.ServerFunctionCodeType;
import com.kingmeter.common.KingMeterMarker;
import com.kingmeter.dto.charging.v1.rest.response.ForceUnLockResponseRestDto;
import com.kingmeter.dto.charging.v1.socket.out.ScanUnlockResponseDto;
import com.kingmeter.dto.charging.v2.socket.in.vo.DockStateInfoFromHeartBeatVO;
import com.kingmeter.dto.charging.v2.socket.out.ForceUnLockResponseDto;
import com.kingmeter.socket.framework.application.SocketApplication;
import com.kingmeter.socket.framework.util.CacheUtil;
import com.kingmeter.utils.HardWareUtils;
import com.kingmeter.utils.StringUtil;
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

        Map<String, String> siteMap = CacheUtil.getInstance().getDeviceInfoMap().get(
                siteId
        );
        int timezone = Integer.parseInt(siteMap.get("timezone"));
        List<DockStateInfoFromHeartBeatVO> stateList = JSON.parseArray(siteMap.get("dockArray"), DockStateInfoFromHeartBeatVO.class);


        new Thread(new TestUnlockPerTime(siteId, times, perSite, perDock, stateList, timezone)).start();

        return "batch unlock succeed";
    }


    class TestUnlockPerTime implements Runnable {

        private long siteId;
        private int times;
        private long perSite;
        private long perDock;
        private List<DockStateInfoFromHeartBeatVO> stateList;
        private int timezone;

        public TestUnlockPerTime(long siteId, int times,
                                 long perSite, long perDock,
                                 List<DockStateInfoFromHeartBeatVO> stateList,
                                 int timezone) {
            this.siteId = siteId;
            this.times = times;
            this.perSite = perSite;
            this.perDock = perDock;
            this.stateList = stateList;
            this.timezone = timezone;
        }

        public void run() {
            for (int i = 0; i < times; i++) {
                boolean flag = TestMemoryCache.getInstance().getUnlockFlag()
                        .getOrDefault(siteId, false);
                if (!flag) {
                    TestMemoryCache.getInstance().getUnlockFlag().remove(siteId);
                    break;
                }
                Map<String, String> siteMap = CacheUtil.getInstance().getDeviceInfoMap().get(
                        siteId
                );
                boolean isDockReadyNow = true;
                String dockArrayStr = siteMap.getOrDefault("dockArray", "");
                if (StringUtil.isNotEmpty(dockArrayStr)) {
                    List<DockStateInfoFromHeartBeatVO> stateList =
                            JSON.parseArray(dockArrayStr, DockStateInfoFromHeartBeatVO.class);
                    for (DockStateInfoFromHeartBeatVO vo : stateList) {
                        if (vo.getBid() == 0) {
                            isDockReadyNow = false;
                            break;
                        }
                    }
                }
                if (!isDockReadyNow) {
                    try {
                        Thread.sleep(perSite);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    continue;
                }

                flag = scanUnlockSingle(siteId, timezone, stateList, perDock);
                if (!flag) {
                    TestMemoryCache.getInstance().getUnlockFlag().remove(siteId);
                    break;
                }
                try {
                    Thread.sleep(perSite);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean scanUnlockSingle(long siteId, int timezone, List<DockStateInfoFromHeartBeatVO> stateList, long perDock) {
        try {
            String userId = "123";

            for (DockStateInfoFromHeartBeatVO vo : stateList) {
                long dockId = vo.getKid();
                ScanUnlockResponseDto response = new
                        ScanUnlockResponseDto(vo.getKid(), userId, 0,
                        HardWareUtils.getInstance().getUtcTimeStampOnDevice(timezone));

                String key = "scan_" + userId + "_" + dockId;

                TestUnLockDto unLockDto = new TestUnLockDto();
                unLockDto.setSiteId(siteId);
                unLockDto.setDockId(vo.getKid());
                unLockDto.setStartTimeStamp(System.currentTimeMillis());
                TestMemoryCache.getInstance().getTestForceLockInfoMap().put(vo.getKid(), unLockDto);

                SocketChannel channel = socketApplication.sendSocketMsg(siteId,
                        ServerFunctionCodeType.ScanUnLock,
                        toJSON(response).toString());

                log.info(new KingMeterMarker("Socket,ScanUnLock,C102"),
                        "{}|{}|{}|{}|{}", siteId, dockId, userId, 0,
                        HardWareUtils.getInstance().getUtcTimeStampOnDevice(timezone));

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


    private boolean forceUnlockSingle(long siteId, int timezone, List<DockStateInfoFromHeartBeatVO> stateList, long perDock) {
        try {
            String userId = "123";

            for (DockStateInfoFromHeartBeatVO vo : stateList) {
                ForceUnLockResponseDto response = new
                        ForceUnLockResponseDto(vo.getKid(), userId,
                        HardWareUtils.getInstance().getUtcTimeStampOnDevice(timezone));

                String key = "force_" + vo.getKid();

                TestUnLockDto unLockDto = new TestUnLockDto();
                unLockDto.setSiteId(siteId);
                unLockDto.setDockId(vo.getKid());
                unLockDto.setStartTimeStamp(System.currentTimeMillis());
                TestMemoryCache.getInstance().getTestForceLockInfoMap().put(vo.getKid(), unLockDto);

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
