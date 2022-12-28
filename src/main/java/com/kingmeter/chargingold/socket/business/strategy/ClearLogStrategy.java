package com.kingmeter.chargingold.socket.business.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.kingmeter.common.KingMeterMarker;
import com.kingmeter.dto.charging.v1.socket.in.ClearLogRequestDto;
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
public class ClearLogStrategy implements RequestStrategy {
    @Override
    public void process(RequestBody requestBody, ResponseBody responseBody, ChannelHandlerContext ctx) {
        ClearLogRequestDto requestDto = JSONObject.
                parseObject(requestBody.getData(), ClearLogRequestDto.class);
        long siteId = requestDto.getSid();
        log.info(new KingMeterMarker("Socket,ClearLog,D201"),
                "{}|{}", siteId,
                requestDto.getSls());

//        Map<String, String> result = new HashMap<>();
//        result.put("result",
//                JSON.toJSONString(requestDto));

        String key = "clear_site_log_" + siteId;

        Promise<Object> promise = CacheUtil.getInstance().getPROMISES().remove(key);
        if (promise != null) {
            promise.setSuccess(requestDto);
        }

//        CacheUtil.getInstance().getDeviceResultMap().put(key,result);
    }
}
