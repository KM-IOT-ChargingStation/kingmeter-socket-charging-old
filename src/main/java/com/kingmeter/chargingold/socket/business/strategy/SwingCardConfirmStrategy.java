package com.kingmeter.chargingold.socket.business.strategy;

import com.alibaba.fastjson.JSONObject;
import com.kingmeter.chargingold.socket.acl.ChargingSiteService;
import com.kingmeter.chargingold.socket.business.code.ServerFunctionCodeType;
import com.kingmeter.common.KingMeterMarker;
import com.kingmeter.dto.charging.v1.socket.in.SwingCardUnLockRequestConfirmDto;
import com.kingmeter.dto.charging.v1.socket.out.SwingCardUnLockConfirmResponseDto;
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
public class SwingCardConfirmStrategy implements RequestStrategy {

    @Autowired
    private ChargingSiteService chargingSiteService;

    @Override
    public void process(RequestBody requestBody, ResponseBody responseBody, ChannelHandlerContext ctx) {
        SwingCardUnLockRequestConfirmDto requestDto = JSONObject.
                parseObject(requestBody.getData(), SwingCardUnLockRequestConfirmDto.class);

        long siteId = Long.parseLong(requestBody.getDeviceId());
        String cardNo = HardWareUtils.getInstance().correctCardNumber(
                Long.toHexString(requestDto.getCid())
        );


        Map<String, String> siteMap = CacheUtil.getInstance().getDeviceInfoMap().get(siteId);
        int timezone = Integer.parseInt(siteMap.get("timezone"));

        log.info(new KingMeterMarker("Socket,SwingCardUnLock,C403"),
                "{}|{}|{}|{}|{}|{}|{}|{}",
                siteId, requestDto.getKid(), requestDto.getBid(),
                cardNo,requestDto.getGbs(), "", requestDto.getTim(),
                HardWareUtils.getInstance().getLocalTimeByHardWareTimeStamp(timezone, requestDto.getTim()));

        responseBody.setTokenArray(requestBody.getTokenArray());
        responseBody.setFunctionCodeArray(ServerFunctionCodeType.SwingCardConfirm);

        SwingCardUnLockConfirmResponseDto responseDto = createResponse(siteId,requestDto, timezone);
        responseBody.setData(JSONObject.toJSON(responseDto).toString());
        ctx.writeAndFlush(responseBody);

        log.info(new KingMeterMarker("Socket,SwingCardUnLock,C404"),
                "{}|{}|{}|{}|{}", siteId, responseDto.getKid(),
                0, 0,0);

        chargingSiteService.dealWithSwingCardConfirm(requestDto);
    }


    private SwingCardUnLockConfirmResponseDto createResponse(
            long siteId,
            SwingCardUnLockRequestConfirmDto requestConfirmDto, int timezone) {

        return new SwingCardUnLockConfirmResponseDto(requestConfirmDto.getKid());
    }

}
