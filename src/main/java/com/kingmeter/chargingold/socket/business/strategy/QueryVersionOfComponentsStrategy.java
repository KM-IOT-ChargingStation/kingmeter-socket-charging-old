package com.kingmeter.chargingold.socket.business.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.kingmeter.common.KingMeterMarker;
import com.kingmeter.dto.charging.v1.socket.in.QueryVersionOfComponentsRequestDto;
import com.kingmeter.socket.framework.dto.RequestBody;
import com.kingmeter.socket.framework.dto.ResponseBody;
import com.kingmeter.socket.framework.strategy.RequestStrategy;
import com.kingmeter.socket.framework.util.CacheUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Promise;
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
public class QueryVersionOfComponentsStrategy implements RequestStrategy {
    @Override
    public void process(RequestBody requestBody, ResponseBody responseBody, ChannelHandlerContext ctx) {
        QueryVersionOfComponentsRequestDto requestDto =
                JSONObject.
                        parseObject(requestBody.getData(), QueryVersionOfComponentsRequestDto.class);

        long siteId = Long.parseLong(requestBody.getDeviceId());

        log.info(new KingMeterMarker("Socket,QueryVersionOfComponents,1002"),
                "{}|{}",siteId,
                JSONObject.toJSONString(requestDto));

//        Map<String, String> result = new HashMap<>();
//        result.put("siteInfo",
//                JSON.toJSONString(requestDto));

        String key = "query_site_version_reply_" + siteId;

        Promise<Object> promise = CacheUtil.getInstance().getPROMISES().remove(key);
        if (promise != null) {
            promise.setSuccess(requestDto);
        }

//        CacheUtil.getInstance().getDeviceResultMap().put(key,result);
    }
}
