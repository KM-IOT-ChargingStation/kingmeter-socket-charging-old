package com.kingmeter.chargingold.socket.business.strategy;

import com.alibaba.fastjson.JSONObject;
import com.kingmeter.chargingold.socket.acl.ChargingSiteService;
import com.kingmeter.chargingold.socket.rest.TestMemoryCache;
import com.kingmeter.chargingold.socket.rest.TestUnLockDto;
import com.kingmeter.common.KingMeterMarker;
import com.kingmeter.dto.charging.v1.socket.in.ForceUnLockRequestDto;
import com.kingmeter.socket.framework.dto.RequestBody;
import com.kingmeter.socket.framework.dto.ResponseBody;
import com.kingmeter.socket.framework.strategy.RequestStrategy;
import com.kingmeter.socket.framework.util.CacheUtil;
import com.kingmeter.utils.HardWareUtils;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Map;


@Slf4j
@Component
public class ForceUnLockStrategy implements RequestStrategy {

    @Autowired
    private ChargingSiteService chargingSiteService;


    @Override
    public void process(RequestBody requestBody, ResponseBody responseBody, ChannelHandlerContext channelHandlerContext) {

        ForceUnLockRequestDto requestDto =
                JSONObject.
                        parseObject(requestBody.getData(), ForceUnLockRequestDto.class);

        long siteId = Long.parseLong(requestBody.getDeviceId());

        Map<String, String> siteMap = CacheUtil.getInstance().getDeviceInfoMap().get(siteId);

        TestUnLockDto unLockDto = TestMemoryCache.getInstance().getTestForceLockInfoMap().get(
                requestDto.getKid()
        );
        long currentTimeStamp = System.currentTimeMillis();
        if(unLockDto == null){
            unLockDto = new TestUnLockDto();
            unLockDto.setStartTimeStamp(currentTimeStamp);
        }

        log.info(new KingMeterMarker("Socket,ForceUnLock,C103"),
                "{}|{}|{}|{}|{}|{}|{}|{}",
                siteId, requestDto.getKid(),
                requestDto.getBid(), requestDto.getUid(), requestDto.getGbs(),
                requestDto.getTim(),
                HardWareUtils.getInstance()
                        .getLocalTimeByHardWareTimeStamp(
                                Integer.parseInt(siteMap.get("timezone")),
                                requestDto.getTim()),
                ((float) (currentTimeStamp - unLockDto.getStartTimeStamp())) / 1000f);

        chargingSiteService.forceUnlockNotify(siteId,requestDto);
    }
}
