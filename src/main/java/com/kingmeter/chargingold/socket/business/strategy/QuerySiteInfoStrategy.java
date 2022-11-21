package com.kingmeter.chargingold.socket.business.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.kingmeter.common.KingMeterMarker;
import com.kingmeter.dto.charging.v1.socket.in.QuerySiteInfoRequestDto;
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
public class QuerySiteInfoStrategy implements RequestStrategy {
    @Override
    public void process(RequestBody requestBody, ResponseBody responseBody, ChannelHandlerContext ctx) {
        QuerySiteInfoRequestDto requestDto =
                JSONObject.
                        parseObject(requestBody.getData(), QuerySiteInfoRequestDto.class);

        long siteId = requestDto.getSite_id();

        log.info(new KingMeterMarker("Socket,QuerySiteInfo,F002"),
                "{}|{}",siteId,
                JSONObject.toJSONString(requestDto));

        Map<String, String> result = new HashMap<>();
        result.put("siteInfo",
                JSON.toJSONString(requestDto));

        String key = "query_site_info_" + siteId;

        CacheUtil.getInstance().getDeviceResultMap().put(key,result);
    }
}
