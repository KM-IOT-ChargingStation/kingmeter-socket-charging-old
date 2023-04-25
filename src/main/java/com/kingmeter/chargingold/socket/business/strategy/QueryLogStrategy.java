package com.kingmeter.chargingold.socket.business.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.kingmeter.chargingold.socket.business.code.LogErrorCodeType;
import com.kingmeter.common.KingMeterMarker;
import com.kingmeter.dto.charging.v1.rest.request.QueryLogRequestRestDto;
import com.kingmeter.dto.charging.v1.socket.in.QueryLogRequestDto;
import com.kingmeter.socket.framework.dto.RequestBody;
import com.kingmeter.socket.framework.dto.ResponseBody;
import com.kingmeter.socket.framework.strategy.RequestStrategy;
import com.kingmeter.socket.framework.util.CacheUtil;
import com.kingmeter.utils.HardWareUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description:
 * @author: crazyandy
 */

@Slf4j
@Component
public class QueryLogStrategy implements RequestStrategy {


    @Override
    public void process(RequestBody requestBody, ResponseBody responseBody, ChannelHandlerContext ctx) {
        QueryLogRequestDto requestDto = JSONObject.
                parseObject(requestBody.getData(), QueryLogRequestDto.class);

        long siteId = requestDto.getSid();
        String record = requestDto.getRecord();
        log.info(new KingMeterMarker("Socket,QueryLog,D001"),
                "{}|{}", siteId, record);

        QueryLogRequestRestDto restDto = new QueryLogRequestRestDto();
        restDto.setSiteId(siteId);
        restDto.setResult(decodeRecord(record));
//        Map<String, String> result = new HashMap<>();
//        result.put("result", JSON.toJSONString(restDto));

        String key = "query_site_log_" + siteId;

//        CacheUtil.getInstance().getDeviceResultMap().put(key, result);

        Promise<Object> promise = CacheUtil.getInstance().getPROMISES().remove(key);
        if (promise != null) {
            promise.setSuccess(restDto);
        }
    }


    private static List<Map<String, String>> decodeRecord(String record) {
        List<Map<String, String>> result = new ArrayList<>();
        if (record != null && !record.equals("")) {
            String[] rowArray = record.split(";");
            for (String tmp : rowArray) {
                String[] column_array = tmp.split(",");
                long current = Long.parseLong(column_array[0]);
                int code = Integer.parseInt(column_array[1]);

                String now = HardWareUtils.getInstance()
                        .getLocalTimeByHardWareTimeStamp(8,
                                current);

                Map<String, String> map = new HashMap<>();
                if (column_array.length == 2) {
                    map.put(now,
                            LogErrorCodeType.getEnum(code).desc());
                } else {
                    map.put(now,
                            String.format(LogErrorCodeType.getEnum(code).desc(),
                                    column_array[2])
                    );
                }


                result.add(map);
            }
        }
        return result;
    }


//    public static void main(String[] args) {
//        String record = "1672301631,11,1;1672301646,1;1672301646,5;1672301648,8;1672301648,5;1672301650,8;1672301650,5;1672301652,8;1672301652,5;1672301654,8;1672301668,5;1672301670,8;1672301670,5;1672301671,10;1672301672,1;1672301672,5;1672301674,8;1672301674,5;1672301674,9;1672301676,8;1672301676,5;1104537620,5;1104537620,10;1672303549,11,1;1672303598,11,1;1672304628,11,1;1672304701,11,1;1672305022,11,1;1672305262,11,1;1672305761,11,1;1672305841,11,1;1672305921,11,1;1672306081,11,1;1672306162,11,1;1672306321,11,1;1672306639,11,1;1672306806,11,1;1104543125,5;1104543125,10;1104543125,15,0;1104543125,15,0;1104543125,15,0;1104543125,15,0;1104543125,15,0;1104543125,15,0;1104543125,15,0;1104543125,15,0;1104543125,15,0;1104543125,15,0;1104543125,15,0;1104543125,15,0;1104543125,15,0;1104543125,15,0;1104543125,15,0;1104543125,15,0;1672307266,15,0;1672307266,15,0;1672307266,15,0;1672307266,15,0;1672307266,15,0;1672307266,15,0;1672307266,15,0;1672307266,15,0;1672307301,6;1104543298,5;1104543298,10;1104543298,15,0;1104543298,15,0;1104543298,15,0;1104543298,15,0;1104543298,15,0;1104543298,15,0;1104543298,15,0;1104543298,15,0;1672307440,15,0;1672307440,15,0;1672307440,15,0;1672307440,15,0;1672307440,15,0;1672307440,15,0;1672307440,15,0;1672307440,15,0;1672307480,11,1;1672308250,1;1672308250,15,0;1672308250,5;1672308253,8;1672308253,5;1672308256,8;1672308256,5;1672308259,8;1672308259,5;1672308260,8;1672308268,5;1672308270,8;1672308270,5;1672308270,10;1672308270,15,0;1672308270,15,0;1672308270,15,0;1672308270,15,0;1672308270,15,0;1672308270,15,0;1672308270,15,0;1672308270,15,0;";
////        String record = (System.currentTimeMillis() )+",1;";
//
//        System.out.println(record.length());
//
//
//        List<Map<String, String>> result = decodeRecord(record);
//
//        for (Map<String, String> map:result
//             ) {
//            System.out.println(map.toString());
//        }
//    }
}
