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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Slf4j
@Component()
public class LoginStrategy implements RequestStrategy {

    @Autowired
    private ChargingSiteService chargingSiteService;

    @Value("${kingmeter.default.companyCode}")
    private String defaultCompanyCode;

    @Value("${kingmeter.default.timezone}")
    private int defaultTimezone;

    @Override
    public void process(RequestBody requestBody, ResponseBody responseBody,
                        ChannelHandlerContext ctx) {

        //解析传入data
        SiteLoginRequestDto loginParamsDto = JSONObject.
                parseObject(requestBody.getData(), SiteLoginRequestDto.class);

        long siteId = loginParamsDto.getSid();

        SocketChannel channel = (SocketChannel) ctx.channel();

        String oldToken = requestBody.getToken();
        byte[] oldTokenArray = requestBody.getTokenArray();

        TokenResult tokenResult = TokenUtils.getInstance().getRandomSiteToken(
                oldToken, oldTokenArray,
                CacheUtil.getInstance().getTokenAndDeviceIdMap()
        );

        int timezone = defaultTimezone;
        String companyCode = defaultCompanyCode;

        LoginResponseDto responseDto = new LoginResponseDto(0, "", "", 0,
                -1, -1,
                HardWareUtils.getInstance()
                        .getUtcTimeStampOnDevice(timezone));

        log.warn(new KingMeterMarker("Socket,Login,C001"),
                "{}|{}|{}|{}", siteId, ctx.channel().id().asLongText(), "0",
                channel.remoteAddress());

        if (!tokenResult.isReLogin()) {
            log.info(new KingMeterMarker("Socket,Login,C001"),
                    "{}|{}|{}|{}", siteId,
                    loginParamsDto.getPwd(), "1", "");
            LoginPermissionDto permission = chargingSiteService.getSiteLoginPermission(loginParamsDto,
                    tokenResult, channel);
            if (permission == null) {
                ctx.close();
                return;
            }
            responseDto = permission.getResponseDto();
            companyCode = permission.getCompanyCode();
            timezone = permission.getTimezone();
        } else {
            log.info(new KingMeterMarker("Socket,Login,C001"),
                    "{}|{}|{}|{}", siteId,
                    loginParamsDto.getPwd(), "2", "");

        }
        log.info(new KingMeterMarker("Socket,Login,C002"),
                "{}|{}|{}|{}|{}|{}|{}|{}|{}|{}", siteId,
                responseDto.getSls(), responseDto.getPwd(),
                responseDto.getUrl(), responseDto.getPot(),
                0, 0, Integer.parseInt(companyCode), responseDto.getTim(),
                HardWareUtils.getInstance()
                        .getLocalTimeByHardWareTimeStamp(
                                timezone,
                                responseDto.getTim()));

        //包装传出data
        responseBody.setTokenArray(tokenResult.getTokenArray());
        responseBody.setFunctionCodeArray(ServerFunctionCodeType.LoginType);
        responseBody.setData(JSONObject.toJSON(responseDto).toString());
        ctx.writeAndFlush(responseBody);
    }

}
