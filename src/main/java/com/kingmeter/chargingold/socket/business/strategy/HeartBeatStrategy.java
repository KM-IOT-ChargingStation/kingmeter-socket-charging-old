package com.kingmeter.chargingold.socket.business.strategy;

import com.alibaba.fastjson.JSONObject;
import com.kingmeter.chargingold.socket.acl.ChargingSiteService;
import com.kingmeter.chargingold.socket.business.code.ServerFunctionCodeType;
import com.kingmeter.common.KingMeterMarker;
import com.kingmeter.dto.charging.v1.socket.in.SiteHeartRequestDto;
import com.kingmeter.dto.charging.v1.socket.out.SiteHeartResponseDto;
import com.kingmeter.socket.framework.dto.RequestBody;
import com.kingmeter.socket.framework.dto.ResponseBody;
import com.kingmeter.socket.framework.strategy.RequestStrategy;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class HeartBeatStrategy implements RequestStrategy {


    @Autowired
    private ChargingSiteService chargingSiteService;

    @Override
    public void process(RequestBody requestBody, ResponseBody responseBody, ChannelHandlerContext ctx) {

        SiteHeartResponseDto responseDto;

//        log.info("heartbeat source data is {}",requestBody.getData());

        SiteHeartRequestDto requestDto =
                JSONObject.
                        parseObject(requestBody.getData(), SiteHeartRequestDto.class);

        long siteId = requestDto.getSid();

        //log记录放在了createSiteHeartResponseDto方法中
        //主要是为了计算 温度
        responseDto = chargingSiteService.createSiteHeartResponseDto(requestDto);

        sendHeartBeatResponse(responseBody, ctx, responseDto);

        log.info(new KingMeterMarker("Socket,HeartBeat,C302"),
                "{}|{}|{}|{}", siteId, 0,0, responseDto.getTim());

        chargingSiteService.heartBeatNotify(requestDto, responseBody, ctx);
    }

    private void sendHeartBeatResponse(ResponseBody responseBody, ChannelHandlerContext ctx,
                                       SiteHeartResponseDto responseDto) {
        responseBody.setFunctionCodeArray(ServerFunctionCodeType.SiteHeartBeat);
        responseBody.setData(JSONObject.toJSON(responseDto).toString());
        ctx.writeAndFlush(responseBody);
    }

}
