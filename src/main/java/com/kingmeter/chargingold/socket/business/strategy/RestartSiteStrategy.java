package com.kingmeter.chargingold.socket.business.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.kingmeter.common.KingMeterMarker;
import com.kingmeter.dto.charging.v1.socket.in.ConfigureSiteInfoRequestDto;
import com.kingmeter.dto.charging.v1.socket.in.RestartSiteRequestDto;
import com.kingmeter.socket.framework.dto.RequestBody;
import com.kingmeter.socket.framework.dto.ResponseBody;
import com.kingmeter.socket.framework.strategy.RequestStrategy;
import com.kingmeter.socket.framework.util.CacheUtil;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @description:
 * @author: crazyandy
 */
@Slf4j
@Component
public class RestartSiteStrategy implements RequestStrategy {
    @Override
    public void process(RequestBody requestBody, ResponseBody responseBody, ChannelHandlerContext ctx) {
        RestartSiteRequestDto requestDto =
                JSONObject.
                        parseObject(requestBody.getData(), RestartSiteRequestDto.class);

        long siteId = requestDto.getSite_id();

        log.info(new KingMeterMarker("Socket,RestartSite,FF02"),
                "{}|{}",siteId,
                JSONObject.toJSONString(requestDto));

        Map<String, String> result = new HashMap<>();
        result.put("siteInfo",
                JSON.toJSONString(requestDto));

        String key = "restart_site_reply_" + siteId;

        CacheUtil.getInstance().getDeviceResultMap().put(key,result);
    }
}
