package com.kingmeter.chargingold.socket.business.strategy;

import com.alibaba.fastjson.JSONObject;
import com.kingmeter.chargingold.socket.acl.ChargingSiteService;
import com.kingmeter.chargingold.socket.business.code.ServerFunctionCodeType;
import com.kingmeter.common.KingMeterMarker;
import com.kingmeter.dto.charging.v1.socket.in.SiteLoginRequestDto;
import com.kingmeter.dto.charging.v1.socket.out.LoginPermissionDto;
import com.kingmeter.dto.charging.v1.socket.out.LoginResponseDto;
import com.kingmeter.socket.framework.dto.RequestBody;
import com.kingmeter.socket.framework.dto.ResponseBody;
import com.kingmeter.socket.framework.strategy.RequestStrategy;
import com.kingmeter.socket.framework.util.CacheUtil;
import com.kingmeter.utils.HardWareUtils;
import com.kingmeter.utils.TokenResult;
import com.kingmeter.utils.TokenUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Slf4j
@Component()
public class LoginStrategy implements RequestStrategy {

    @Autowired
    private ChargingSiteService chargingSiteService;

    @Override
    public void process(RequestBody requestBody, ResponseBody responseBody,
                        ChannelHandlerContext ctx) {

        //解析传入data
        SiteLoginRequestDto loginParamsDto = JSONObject.
                parseObject(requestBody.getData(), SiteLoginRequestDto.class);

        long siteId = loginParamsDto.getSid();

        log.info(new KingMeterMarker("Socket,Login,C001"),
                "{}|{}|{}|{}", siteId,
                loginParamsDto.getPwd(), "", "");

        SocketChannel channel = (SocketChannel) ctx.channel();
        TokenResult tokenResult = TokenUtils.getInstance().getRandomSiteToken(
                CacheUtil.getInstance().getTokenAndDeviceIdMap()
        );

        LoginPermissionDto permission = chargingSiteService.getSiteLoginPermission(loginParamsDto,
                tokenResult, channel);
        if (permission == null) {
            ctx.close();
            return;
        }
        LoginResponseDto responseDto = permission.getResponseDto();

        //包装传出data
        responseBody.setTokenArray(tokenResult.getTokenArray());
        responseBody.setFunctionCodeArray(ServerFunctionCodeType.LoginType);
        responseBody.setData(JSONObject.toJSON(responseDto).toString());
        ctx.writeAndFlush(responseBody);


        log.info(new KingMeterMarker("Socket,Login,C002"),
                "{}|{}|{}|{}|{}|{}|{}|{}|{}|{}", siteId,
                responseDto.getSls(), responseDto.getPwd(),
                responseDto.getUrl(), responseDto.getPot(),
                0, 0, 2113, responseDto.getTim(),
                HardWareUtils.getInstance()
                        .getLocalTimeByHardWareTimeStamp(
                                permission.getTimezone(),
                                responseDto.getTim()));
    }

}
