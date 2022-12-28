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
//        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
//        String record = "1670926225,11,3;1670857961,11,2;1670863922,11,2;1670863941,11,1;1670863954,11,1;1670863991,11,1;1670864020,11,1;1670864039,11,5;1670864078,11,2;1670864112,11,1;1670864640,11,2;1670865985,11,1;1670868840,11,1;1670869678,11,1;1670869711,11,2;1670869722,11,4;1670869866,11,3;1670870059,11,2;1670870072,11,4;1670870094,1;1670870094,5;1670870096,8;1670870096,5;1670870098,8;1670870098,5;1670870099,8;1670870099,5;1670870100,8;1670870108,5;1670870110,8;1670870110,5;1670870110,10;1670870170,1;1670870170,5;1670870172,8;1670870172,5;1670870172,9;1670870173,8;1670870173,5;1670870173,8;1670870173,5;1670870173,10;1670870188,11,1;1670870242,11,5;1670870258,1;1670870258,5;1670870260,8;1670870260,5;1670870261,8;1670870261,5;1670870261,8;1670870261,5;1670870261,8;1670870261,9;1670870269,5;1670870270,8;1670870270,5;1670870270,10;1670870308,11,3;1670870429,11,4;1670870444,1;1670870444,5;1670870446,8;1670870446,5;1670870448,8;1670870448,5;1670870450,8;1670870450,5;1670870452,8;1670870460,5;1670870462,8;1670870462,5;1670870462,9;";
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
