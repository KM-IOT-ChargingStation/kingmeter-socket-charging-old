package com.kingmeter.chargingold.socket.business.strategy;

import com.alibaba.fastjson.JSONObject;
import com.kingmeter.chargingold.socket.acl.ChargingSiteService;
import com.kingmeter.common.KingMeterMarker;
import com.kingmeter.dto.charging.v1.socket.in.QueryDockInfoRequestDto;
import com.kingmeter.socket.framework.dto.RequestBody;
import com.kingmeter.socket.framework.dto.ResponseBody;
import com.kingmeter.socket.framework.strategy.RequestStrategy;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class QueryDockInfoStrategy implements RequestStrategy {


    @Autowired
    private ChargingSiteService chargingSiteService;


    @Override
    public void process(RequestBody requestBody, ResponseBody responseBody, ChannelHandlerContext ctx) {
        QueryDockInfoRequestDto requestDto =
                JSONObject.
                        parseObject(requestBody.getData(), QueryDockInfoRequestDto.class);

        long siteId = requestDto.getSid();

        log.info(new KingMeterMarker("Socket,QueryDockInfo,C601"),
                "{}|{}|{}|{}|{}|{}",siteId,
                "","","","",
                JSONObject.toJSONString(requestDto.getState()));

        chargingSiteService.dealWithQueryDockInfo(requestDto);
    }


}
