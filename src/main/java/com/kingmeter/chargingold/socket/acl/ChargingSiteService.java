package com.kingmeter.chargingold.socket.acl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.kingmeter.chargingold.socket.business.code.ServerFunctionCodeType;
import com.kingmeter.common.KingMeterMarker;
import com.kingmeter.dto.charging.v1.rest.response.ForceUnLockResponseRestDto;
import com.kingmeter.dto.charging.v1.rest.response.QueryDockLockStatusResponseRestDto;
import com.kingmeter.dto.charging.v1.rest.response.ScanUnlockResponseRestDto;
import com.kingmeter.dto.charging.v1.socket.in.*;
import com.kingmeter.dto.charging.v1.rest.response.QueryDockInfoResponseRestDto;
import com.kingmeter.dto.charging.v1.rest.response.vo.DockStateInfoFromQueryDockInfoVOForRest;
import com.kingmeter.dto.charging.v1.socket.in.vo.DockStateInfoFromQueryDockInfoVO;
import com.kingmeter.dto.charging.v1.socket.in.vo.DockStateInfoFromHeartBeatVO;
import com.kingmeter.dto.charging.v1.socket.out.*;
import com.kingmeter.socket.framework.dto.ResponseBody;
import com.kingmeter.socket.framework.util.CacheUtil;
import com.kingmeter.utils.HardWareUtils;
import com.kingmeter.utils.MD5Util;
import com.kingmeter.utils.TokenResult;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ChargingSiteService {
    @Value("${kingmeter.requestBusiness}")
    private boolean requestBusiness;

    @Value("${kingmeter.default.companyCode}")
    private String defaultCompanyCode;

    @Value("${kingmeter.default.timezone}")
    private int defaultTimezone;


    @Value("${current.env}")
    private int env;

    @Value("${prod.wifi_name}")
    private String prod_wifi_name;

    @Value("${prod.wifi_pwd}")
    private String prod_wifi_pwd;

    @Value("${prod.ip}")
    private String prod_ip;

    @Value("${prod.port}")
    private int prod_port;

    @Value("${local.wifi_name}")
    private String local_wifi_name;

    @Value("${local.wifi_pwd}")
    private String local_wifi_pwd;

    @Value("${local.ip}")
    private String local_ip;

    @Value("${local.port}")
    private int local_port;


    @Autowired
    private BusinessService business;


    public LoginPermissionDto getSiteLoginPermission(SiteLoginRequestDto requestDto,
                                                     TokenResult tokenResult, SocketChannel channel) {
        String companyCode = defaultCompanyCode;
        int timezone = defaultTimezone;

        LoginResponseDto responseDto = new LoginResponseDto(0, "", "", 0,
                -1, -1,
                HardWareUtils.getInstance()
                        .getUtcTimeStampOnDevice(timezone));

        long siteId = requestDto.getSid();

        //123456
        byte[] passwordArray = {49, 50, 51, 52, 53, 54};
        String passwordMd5 = MD5Util.MD5Encode(passwordArray);

        if (!requestDto.getPwd().equals(passwordMd5)) {
            return null;
        }

        if (requestBusiness) {
            LoginPermissionDto permission = business.getLoginPermission(requestDto);
            if (permission == null) {
                return null;
            } else if (permission.getResponseDto().getSls() != 0) {
                return null;
            } else {
                responseDto = permission.getResponseDto();
                companyCode = permission.getCompanyCode();
                timezone = permission.getTimezone();
            }
        }

        Map<String, String> siteMap = CacheUtil.getInstance()
                .getDeviceInfoMap()
                .getOrDefault(siteId, new ConcurrentHashMap<>());

        siteMap.put("token", tokenResult.getToken());
        siteMap.put("bikeCount", "0");
        siteMap.put("dockCount", "0");
        siteMap.put("dockArray", "");
        siteMap.put("channelId", channel.id().asLongText());
        siteMap.put("pwd", requestDto.getPwd());
        siteMap.put("count", "0");

        siteMap.put("timezone", String.valueOf(timezone));
        siteMap.put("tempTime", String.valueOf(8 * 3600));

        CacheUtil.getInstance().getDeviceInfoMap().put(siteId, siteMap);
        CacheUtil.getInstance().dealWithLoginSucceed(String.valueOf(siteId),
                tokenResult.getToken(), tokenResult.getTokenArray(), channel);
        CacheUtil.getInstance().getDeviceResultMap().put(siteId + "_queryDockInfoFlag", new HashMap<>());

        return new LoginPermissionDto(responseDto, companyCode, timezone);
    }


    public void configureSiteInfoIfNeeded(long siteId, ResponseBody responseBody, ChannelHandlerContext ctx) {
        ConfigureSiteInfoResponseDto responseDto = null;
        //这里修改站点ip端口
        if (env == 0) {//prod
            responseDto = new ConfigureSiteInfoResponseDto(siteId, "123456",
                    local_ip, local_port,
                    local_wifi_name, local_wifi_pwd);

            log.info(new KingMeterMarker("Socket,ConfigureSiteInfo,F101"),
                    "{}|{}|{}|{}|{}", siteId, local_ip, local_port, local_wifi_name,
                    local_wifi_pwd);

        } else if (env == 1) {//local
            responseDto = new ConfigureSiteInfoResponseDto(siteId, "123456",
                    prod_ip, prod_port,
                    prod_wifi_name, prod_wifi_pwd);

            log.info(new KingMeterMarker("Socket,ConfigureSiteInfo,F101"),
                    "{}|{}|{}|{}|{}", siteId, prod_ip, prod_port, prod_wifi_name,
                    prod_wifi_pwd);
        }

        if (responseDto != null) {
            responseBody.setFunctionCodeArray(ServerFunctionCodeType.ConfigureSiteInfo);
            responseBody.setData(JSONObject.toJSON(responseDto).toString());
            ctx.writeAndFlush(responseBody);
        }
    }


    public void dealWithScanUnLock(long siteId, ScanUnLockRequestDto requestDto) {
        Map<String, String> siteMap = CacheUtil.getInstance().getDeviceInfoMap().get(siteId);

//        Map<String, String> result = new HashMap<>();
//        result.put("ScanUnlock",
//                JSON.toJSONString(new ScanUnlockResponseRestDto(siteId,
//                        requestDto.getKid(), requestDto.getBid(),
//                        requestDto.getUid(), requestDto.getGbs(),
//                        HardWareUtils.getInstance()
//                                .getLocalTimeStampByHardWareUtcTimeStamp(
//                                        Integer.parseInt(siteMap.get("timezone")),
//                                        requestDto.getTim()))));
//
//        CacheUtil.getInstance().getDeviceResultMap().put(
//                "scan_" + requestDto.getUid(), result);

        String key = "scan_" + requestDto.getUid() + "_" + requestDto.getKid();
        Promise<Object> promise = CacheUtil.getInstance().getPROMISES().remove(key);
        if (promise != null) {
            promise.setSuccess(new ScanUnlockResponseRestDto(siteId,
                    requestDto.getKid(), requestDto.getBid(),
                    requestDto.getUid(), requestDto.getGbs(),
                    HardWareUtils.getInstance()
                            .getLocalTimeStampByHardWareUtcTimeStamp(
                                    Integer.parseInt(siteMap.get("timezone")),
                                    requestDto.getTim())));
        }

        if (requestBusiness) business.dealWithScanUnLock(requestDto);
    }


    public void forceUnlockNotify(long siteId, ForceUnLockRequestDto requestDto) {
        Map<String, String> siteMap = CacheUtil.getInstance().getDeviceInfoMap().get(siteId);

//        Map<String, String> result = new HashMap<>();
//        result.put("ForceUnlock",
//                JSON.toJSONString(new ForceUnLockResponseRestDto(siteId,
//                        requestDto.getKid(), requestDto.getBid(),
//                        requestDto.getGbs(),
//                        requestDto.getUid(),
//                        HardWareUtils.getInstance()
//                                .getLocalTimeStampByHardWareUtcTimeStamp(
//                                        Integer.parseInt(siteMap.get("timezone")),
//                                        requestDto.getTim()))));
//        CacheUtil.getInstance().getDeviceResultMap().put(
//                "force_" + requestDto.getKid(), result);

        String key = "force_" + requestDto.getKid();
        Promise<Object> promise = CacheUtil.getInstance().getPROMISES().remove(key);
        if (promise != null) {
            promise.setSuccess(new ForceUnLockResponseRestDto(siteId,
                    requestDto.getKid(), requestDto.getBid(),
                    requestDto.getGbs(),
                    requestDto.getUid(),
                    HardWareUtils.getInstance()
                            .getLocalTimeStampByHardWareUtcTimeStamp(
                                    Integer.parseInt(siteMap.get("timezone")),
                                    requestDto.getTim())));
        }

        if (requestBusiness) business.forceUnlockNotify(requestDto);
    }

    public BikeInDockResponseDto createBikeInDockResponseDto(long siteId, BikeInDockRequestDto requestDto,
                                                             ResponseBody responseBody, ChannelHandlerContext ctx) {

        if (!requestBusiness) {
            Map<String, String> siteMap = CacheUtil.getInstance().getDeviceInfoMap().get(siteId);

            int ret = Integer.parseInt(siteMap.getOrDefault("ret", "0"));
            int acm = Integer.parseInt(siteMap.getOrDefault("acm", "0"));
            int cum = Integer.parseInt(siteMap.getOrDefault("cum", "0"));
            int tim = Integer.parseInt(siteMap.getOrDefault("tim", "0"));

            return new BikeInDockResponseDto(requestDto.getKid(), ret, acm, cum, tim);
        }
        return business.createBikeInDockResponseDto(requestDto);
    }


    public SiteHeartResponseDto createSiteHeartResponseDto(SiteHeartRequestDto requestDto) {
        long siteId = requestDto.getSid();

        log.info(new KingMeterMarker("Socket,HeartBeat,C301"),
                "{}|{}|{}", siteId,
                0,
                JSONObject.toJSONString(requestDto.getState()));

        Map<String, String> siteMap = CacheUtil.getInstance()
                .getDeviceInfoMap()
                .getOrDefault(siteId, new ConcurrentHashMap<>());

        int timezone = Integer.parseInt(siteMap.getOrDefault("timezone", String.valueOf(defaultTimezone)));

        long temp = Long.parseLong(siteMap.getOrDefault("tempTime", "0"));

//        long temp = System.currentTimeMillis() / 1000L +  tempTime;

//        return new SiteHeartResponseDto(HardWareUtils.getInstance().getUtcTimeStampOnDevice(
//                timezone),
//                0, -1);

        return new SiteHeartResponseDto(System.currentTimeMillis() / 1000l + temp,
                -1);
    }

    public void heartBeatNotify(SiteHeartRequestDto requestDto, ResponseBody responseBody, ChannelHandlerContext ctx) {
        long siteId = requestDto.getSid();
        DockStateInfoFromHeartBeatVO[] state = requestDto.getState();

        Map<String, String> siteMap = CacheUtil.getInstance()
                .getDeviceInfoMap()
                .getOrDefault(siteId, new ConcurrentHashMap<>());


        if (state != null && state.length > 0) {
            siteMap.put("dockArray", JSON.toJSONString(state));
        }

        int count = Integer.parseInt(siteMap.getOrDefault("count", "0"));

        if (env == 0) {//prod
            if (count == 0) {//第一次配置
                siteMap.put("count", "10");
                CacheUtil.getInstance().getDeviceInfoMap().put(siteId, siteMap);
                configureSiteInfoIfNeeded(siteId, responseBody, ctx);
            }
        } else if (env == 1) {//local
            if (count > 30) {
                log.info(new KingMeterMarker("Socket,RestartSite,FF01"),
                        "{}", siteId);
                //发送重启命令
                RestartSiteResponseDto responseDto
                        = new RestartSiteResponseDto(siteId);
                responseBody.setFunctionCodeArray(ServerFunctionCodeType.RestartSite);
                responseBody.setData(JSONObject.toJSON(responseDto).toString());
                ctx.writeAndFlush(responseBody);
            }else{
                if (count == 0) {//第一次配置
                    configureSiteInfoIfNeeded(siteId, responseBody, ctx);
                }
                siteMap.put("count", String.valueOf(++count));
                CacheUtil.getInstance().getDeviceInfoMap().put(siteId, siteMap);
            }
        }else{
            CacheUtil.getInstance().getDeviceInfoMap().put(siteId, siteMap);
        }

        if (requestBusiness) business.heartBeatNotify(requestDto);
    }

    public SwingCardUnLockResponseDto dealWithSwingCardUnlock(long siteId, SwingCardUnLockRequestDto requestDto) {

        if (!requestBusiness) {
            Map<String, String> siteMap = CacheUtil.getInstance().getDeviceInfoMap().get(siteId);

            int ast = Integer.parseInt(siteMap.getOrDefault("ast", "0"));
            int acm = Integer.parseInt(siteMap.getOrDefault("acm", "0"));
            int minbsoc = Integer.parseInt(siteMap.getOrDefault("minbsoc", "10"));

            return new SwingCardUnLockResponseDto(requestDto.getKid(),
                    ast, acm, minbsoc
            );
        } else {
            return business.dealWithSwingCardUnlock(requestDto);
        }
    }

    public void dealWithSwingCardConfirm(SwingCardUnLockRequestConfirmDto requestDto) {
        if (requestBusiness) business.dealWithSwingCardConfirm(requestDto);
    }

    public void dealWithQueryDockInfo(QueryDockInfoRequestDto requestDto) {
        DockStateInfoFromQueryDockInfoVOForRest[] stateForRest =
                new DockStateInfoFromQueryDockInfoVOForRest[requestDto.getState().length];

        for (int i = 0; i < requestDto.getState().length; i++) {
            DockStateInfoFromQueryDockInfoVO state = requestDto.getState()[i];
            stateForRest[i] = new DockStateInfoFromQueryDockInfoVOForRest(
                    state.getKid(), state.getKln(),
                    state.getBid()
            );
        }

        QueryDockInfoResponseRestDto rest =
                new QueryDockInfoResponseRestDto(requestDto.getSid(),
                        requestDto.getUid(),
                        stateForRest);

//        Map<String, String> result = new HashMap<>();
//        result.put("dockArray",
//                JSON.toJSONString(rest));
//
//        CacheUtil.getInstance().getDeviceResultMap().put(requestDto.getSid() + "_QueryDockInfo", result);

        String key = requestDto.getSid() + "_QueryDockInfo";
        Promise<Object> promise = CacheUtil.getInstance().getPROMISES().remove(key);
        if (promise != null) {
            promise.setSuccess(rest);
        }

        if (requestBusiness) business.dealWithQueryDockInfo(requestDto);
    }

    public void queryDockLockStatusNotify(long siteId, QueryDockLockStatusRequestDto requestDto) {
        QueryDockLockStatusResponseRestDto restDto =
                new QueryDockLockStatusResponseRestDto(
                        siteId, requestDto.getKid(),
                        requestDto.getBid(), requestDto.getUid(),
                        requestDto.getLks()
                );

//        Map<String, String> result = new HashMap<>();
//        result.put("DockLockStatus",
//                JSON.toJSONString(restDto));
//
//        CacheUtil.getInstance().getDeviceResultMap().put(siteId + "_QueryDockLockStatus", result);


        String key = siteId + "_QueryDockLockStatus";
        Promise<Object> promise = CacheUtil.getInstance().getPROMISES().remove(key);
        if (promise != null) {
            promise.setSuccess(restDto);
        }

        if (requestBusiness) business.queryDockLockStatusNotify(requestDto);
    }

    public void malfunctionUploadNotify(DockMalfunctionUploadRequestDto requestDto) {
        if (requestBusiness) business.malfunctionUploadNotify(requestDto);
    }

    public void offlineNotify(Long deviceId) {
        if (requestBusiness) business.offlineNotify(deviceId);
    }
}
