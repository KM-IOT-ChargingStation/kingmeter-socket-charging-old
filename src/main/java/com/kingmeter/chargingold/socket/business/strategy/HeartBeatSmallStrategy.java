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
public class HeartBeatSmallStrategy implements RequestStrategy {


    @Override
    public void process(RequestBody requestBody, ResponseBody responseBody, ChannelHandlerContext ctx) {
        dealWithSmallHeartBeat(requestBody, responseBody, ctx);
    }

    private void dealWithSmallHeartBeat(RequestBody requestBody, ResponseBody responseBody, ChannelHandlerContext ctx) {
        JSONObject obj = JSONObject.
                parseObject(requestBody.getData());

        long siteId = obj.getLong("sid");
        long sn = 0;
        if(obj.containsKey("sn")){
            sn = obj.getLong("sn");
        }

        log.info(new KingMeterMarker("Socket,HeartBeat,C303"),
                "{}|{}", siteId,sn);

        SiteHeartResponseDto responseDto = new SiteHeartResponseDto(System.currentTimeMillis() / 1000l + 8 * 3600,
                -1);
        responseBody.setFunctionCodeArray(ServerFunctionCodeType.SiteHeartBeatSmall);
        responseBody.setData(JSONObject.toJSON(responseDto).toString());
        ctx.writeAndFlush(responseBody);

        log.info(new KingMeterMarker("Socket,HeartBeat,C304"),
                "{}|{}", siteId, responseDto.getTim());

        responseDto = null;

    }

}
