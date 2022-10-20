package com.kingmeter.chargingold.socket.business.strategy;

import com.alibaba.fastjson.JSONObject;
import com.kingmeter.chargingold.socket.acl.ChargingSiteService;
import com.kingmeter.chargingold.socket.business.code.ServerFunctionCodeType;
import com.kingmeter.common.KingMeterMarker;
import com.kingmeter.dto.charging.v1.socket.in.SwingCardUnLockRequestDto;
import com.kingmeter.dto.charging.v1.socket.out.SwingCardUnLockResponseDto;
import com.kingmeter.socket.framework.dto.RequestBody;
import com.kingmeter.socket.framework.dto.ResponseBody;
import com.kingmeter.socket.framework.strategy.RequestStrategy;
import com.kingmeter.utils.HardWareUtils;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class SwingCardUnLockStrategy implements RequestStrategy {

    @Autowired
    private ChargingSiteService chargingSiteService;

    @Override
    public void process(RequestBody requestBody, ResponseBody responseBody, ChannelHandlerContext ctx) {

        SwingCardUnLockRequestDto requestDto = JSONObject.
                parseObject(requestBody.getData(), SwingCardUnLockRequestDto.class);
        long dockId = requestDto.getKid();
        long siteId = Long.parseLong(requestBody.getDeviceId());
        String cardNo = HardWareUtils.getInstance().correctCardNumber(
                Long.toHexString(requestDto.getCid())
        );

        log.info(new KingMeterMarker("Socket,SwingCardUnLock,C401"),
                "{}|{}|{}|{}|{}|{}", siteId, dockId, requestDto.getBid(),
                cardNo, "","");

        SwingCardUnLockResponseDto responseDto =
                chargingSiteService.dealWithSwingCardUnlock(siteId,requestDto);

        responseBody.setTokenArray(requestBody.getTokenArray());
        responseBody.setFunctionCodeArray(ServerFunctionCodeType.SwingCardUnLock);
        responseBody.setData(JSONObject.toJSON(responseDto).toString());
        ctx.writeAndFlush(responseBody);

        log.info(new KingMeterMarker("Socket,SwingCardUnLock,C402"),
                "{}|{}|{}|{}|{}|{}|{}|{}",
                siteId, responseDto.getKid(),
                responseDto.getAst(), responseDto.getAcm(), responseDto.getMinbat_soc(),
                "","","");
        //8771947000709|0|0|10|||
    }


}
