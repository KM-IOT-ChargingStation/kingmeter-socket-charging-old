package com.kingmeter.chargingold.socket.business.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.kingmeter.chargingold.socket.acl.ChargingSiteService;
import com.kingmeter.chargingold.socket.business.code.ServerFunctionCodeType;
import com.kingmeter.common.KingMeterMarker;
import com.kingmeter.dto.charging.v1.socket.in.BikeInDockRequestDto;
import com.kingmeter.dto.charging.v1.socket.in.vo.DockStateInfoFromHeartBeatVO;
import com.kingmeter.dto.charging.v1.socket.out.BikeInDockResponseDto;
import com.kingmeter.socket.framework.dto.RequestBody;
import com.kingmeter.socket.framework.dto.ResponseBody;
import com.kingmeter.socket.framework.strategy.RequestStrategy;
import com.kingmeter.socket.framework.util.CacheUtil;
import com.kingmeter.utils.HardWareUtils;
import com.kingmeter.utils.StringUtil;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


@Slf4j
@Component
public class BikeInDockStrategy implements RequestStrategy {


    @Autowired
    private ChargingSiteService chargingSiteService;

    @Override
    public void process(RequestBody requestBody, ResponseBody responseBody, ChannelHandlerContext ctx) {
        //解析传入data
        BikeInDockRequestDto requestDto = JSONObject.
                parseObject(requestBody.getData(), BikeInDockRequestDto.class);

        long siteId = Long.parseLong(requestBody.getDeviceId());

        Map<String, String> siteMap = CacheUtil.getInstance().getDeviceInfoMap().get(siteId);

        log.info(new KingMeterMarker("Socket,BikeInDock,C201"),
                "{}|{}|{}|{}|{}", siteId,
                requestDto.getKid(), requestDto.getBid(), requestDto.getStm(),
                HardWareUtils.getInstance()
                        .getLocalTimeByHardWareTimeStamp(
                                Integer.parseInt(siteMap.get("timezone")),
                                requestDto.getStm()));

        responseBody.setTokenArray(requestBody.getTokenArray());

        BikeInDockResponseDto responseDto =
                chargingSiteService.createBikeInDockResponseDto(siteId,requestDto,responseBody,ctx);
        responseBody.setData(JSONObject.toJSON(responseDto).toString());
        responseBody.setFunctionCodeArray(ServerFunctionCodeType.BikeInDock);
        ctx.writeAndFlush(responseBody);

//        String dockArrayStr = siteMap.getOrDefault("dockArray","");
//        if(StringUtil.isNotEmpty(dockArrayStr)){
//            List<DockStateInfoFromHeartBeatVO> stateList =
//                    JSON.parseArray(dockArrayStr,DockStateInfoFromHeartBeatVO.class);
//            for (DockStateInfoFromHeartBeatVO vo:stateList) {
//                if(vo.getKid()==requestDto.getKid()){
//
//                }
//            }
//        }


        log.info(new KingMeterMarker("Socket,BikeInDock,C202"),
                "{}|{}|{}|{}|{}|{}", siteId, responseDto.getKid(),
                responseDto.getRet(), responseDto.getAcm(),
                responseDto.getCum(), responseDto.getTim());

    }


}
