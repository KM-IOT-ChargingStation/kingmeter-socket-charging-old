package com.kingmeter.chargingold.socket.business.strategy;

import com.alibaba.fastjson.JSONObject;
import com.kingmeter.chargingold.socket.acl.ChargingSiteService;
import com.kingmeter.chargingold.socket.business.code.ServerFunctionCodeType;
import com.kingmeter.chargingold.socket.rest.TestMemoryCache;
import com.kingmeter.chargingold.socket.rest.TestRemoteLockDto;
import com.kingmeter.chargingold.socket.rest.TestUnLockDto;
import com.kingmeter.common.KingMeterMarker;
import com.kingmeter.dto.charging.v1.socket.in.RemoteLockRequestDto;
import com.kingmeter.dto.charging.v1.socket.out.RemoteLockConfirmResponseDto;
import com.kingmeter.dto.charging.v1.socket.out.ScanUnLockConfirmResponseDto;
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

/**
 * @description:
 * @author: crazyandy
 */
@Slf4j
@Component
public class RemoteLockStrategy implements RequestStrategy {
    @Autowired
    private ChargingSiteService chargingSiteService;
    @Override
    public void process(RequestBody requestBody, ResponseBody responseBody, ChannelHandlerContext ctx) {
        RemoteLockRequestDto requestDto =
                JSONObject.
                        parseObject(requestBody.getData(), RemoteLockRequestDto.class);
        long siteId = Long.parseLong(requestBody.getDeviceId());

        Map<String, String> siteMap = CacheUtil.getInstance().getDeviceInfoMap().get(siteId);

        TestRemoteLockDto unLockDto = TestMemoryCache.getInstance().getTestRemoteLockInfoMap().get(
                requestDto.getKid()
        );
        long currentTimeStamp = System.currentTimeMillis();
        if(unLockDto == null){
            unLockDto = new TestRemoteLockDto();
            unLockDto.setStartTimeStamp(currentTimeStamp);
        }

        log.info(new KingMeterMarker("Socket,RemoteLock,C10A"),
                "{}|{}|{}|{}|{}|{}|{}|{}",
                siteId,requestDto.getKid(),
                requestDto.getBid(),requestDto.getBs(),requestDto.getLs(),
                requestDto.getTim(),
                HardWareUtils.getInstance()
                        .getLocalTimeByHardWareTimeStamp(
                                Integer.parseInt(siteMap.get("timezone")),
                                requestDto.getTim()),
                ((float) (currentTimeStamp - unLockDto.getStartTimeStamp())) / 1000f);

        RemoteLockConfirmResponseDto responseDto =
                new RemoteLockConfirmResponseDto(requestDto.getKid());
        responseBody.setFunctionCodeArray(ServerFunctionCodeType.RemoteLockConfirm);
        responseBody.setData(JSONObject.toJSON(responseDto).toString());
        ctx.writeAndFlush(responseBody);

        log.info(new KingMeterMarker("Socket,RemoteLock,C10B"),
                "{}|{}", siteId, responseDto.getKid());


        //这里要调用扫码租车后续业务逻辑操作
        chargingSiteService.dealWithRemoteLock(siteId,requestDto);
    }
}
