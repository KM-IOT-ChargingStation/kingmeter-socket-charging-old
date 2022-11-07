package com.kingmeter.chargingold.socket.business.utils;

import com.kingmeter.chargingold.socket.acl.ChargingSiteService;
import com.kingmeter.chargingold.socket.business.code.ClientFunctionCodeType;
import com.kingmeter.chargingold.socket.business.code.ServerFunctionCodeType;
import com.kingmeter.common.KingMeterException;
import com.kingmeter.common.ResponseCode;
import com.kingmeter.dto.charging.v2.socket.out.QueryDockInfoResponseDto;
import com.kingmeter.socket.framework.business.WorkerTemplate;
import com.kingmeter.socket.framework.config.HeaderCode;
import com.kingmeter.socket.framework.dto.ResponseBody;
import com.kingmeter.socket.framework.strategy.RequestStrategy;
import com.kingmeter.socket.framework.util.CacheUtil;
import com.kingmeter.utils.StringUtil;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import static com.alibaba.fastjson.JSON.toJSON;
import static com.kingmeter.common.SpringContexts.getBean;

@Slf4j
@Component
public class Worker extends WorkerTemplate {

    @Autowired
    private ChargingSiteService chargingSiteService;

    @Autowired
    private HeaderCode headerCode;

    @Override
    public RequestStrategy getRequestStrategy(int functionCode) {
        return (RequestStrategy)getBean(ClientFunctionCodeType.getEnum(functionCode).getClassName());
    }

    @Override
    public void doDealWithOffline(SocketChannel channel, String deviceId) {
        if (StringUtil.isNotEmpty(deviceId)) {
            chargingSiteService.offlineNotify(Long.parseLong(deviceId));
            HeartUpdateCount.siteHeartBeatCount.remove(Long.parseLong(deviceId));
            if (CacheUtil.getInstance().getDeviceResultMap().containsKey(deviceId + "_queryDockInfoFlag")) {
                CacheUtil.getInstance().getDeviceResultMap().remove(deviceId + "_queryDockInfoFlag");
            }
        }
    }

    //使用查询桩体信息命令作为测试连通性命令
    @Override
    public ResponseBody getConnectionTestCommand(String deviceId) {
        QueryDockInfoResponseDto queryDockInfoResponseDto =
                new QueryDockInfoResponseDto(0);

        byte[] tokenArray;
        if (CacheUtil.getInstance().getDeviceIdAndTokenArrayMap().containsKey(deviceId)) {
            tokenArray = CacheUtil.getInstance().getDeviceIdAndTokenArrayMap().get(deviceId);
        } else {
            throw new KingMeterException(ResponseCode.Device_Not_Logon);
        }
        ResponseBody responseBody = new ResponseBody();
        responseBody.setTokenArray(tokenArray);
        responseBody.setFunctionCodeArray(ServerFunctionCodeType.QueryDockInfo);
        responseBody.setData(toJSON(queryDockInfoResponseDto).toString());
        responseBody.setSTART_CODE_1(headerCode.getSTART_CODE_1());
        responseBody.setSTART_CODE_2(headerCode.getSTART_CODE_2());
        responseBody.setEND_CODE_1(headerCode.getEND_CODE_1());
        responseBody.setEND_CODE_2(headerCode.getEND_CODE_2());
        responseBody.setToken_length(headerCode.getTOKEN_LENGTH());
        responseBody.setDeviceId(Long.parseLong(deviceId));
        return responseBody;
    }

//    public static void main(String[] args) throws Exception{
//        //E10ADC3949BA59ABBE56E057F20F883E
//
//        String tmp = "40 3A 03 46 21 70 34 79 09 65 16 64 48 71 01 68 48 11 41 1D 7B 7B 5B 22 5C 11 30 2D 3A 7A 5B 74 38 6E 58 72 01 C3 01 7B 0A 09 22 73 69 64 22 3A 09 39 31 32 31 31 31 32 30 30 30 30 33 30 2C 0A 09 22 73 63 66 22 3A 09 30 2C 0A 09 22 73 74 75 22 3A 09 30 2C 0A 09 22 73 75 72 22 3A 09 22 22 2C 0A 09 22 73 76 6C 22 3A 09 22 22 2C 0A 09 22 73 74 61 74 65 22 3A 09 5B 7B 0A 09 09 09 22 6B 69 64 22 3A 09 31 31 32 31 31 31 32 30 30 30 30 33 30 2C 0A 09 09 09 22 62 69 64 22 3A 09 30 2C 0A 09 09 09 22 62 73 6F 63 22 3A 09 30 2C 0A 09 09 09 22 6B 6D 6F 73 22 3A 09 30 0A 09 09 7D 2C 20 7B 0A 09 09 09 22 6B 69 64 22 3A 09 31 31 32 31 31 31 32 30 30 30 30 32 39 2C 0A 09 09 09 22 62 69 64 22 3A 09 30 2C 0A 09 09 09 22 62 73 6F 63 22 3A 09 30 2C 0A 09 09 09 22 6B 6D 6F 73 22 3A 09 30 0A 09 09 7D 2C 20 7B 0A 09 09 09 22 6B 69 64 22 3A 09 31 31 32 31 31 31 32 30 30 30 30 32 38 2C 0A 09 09 09 22 62 69 64 22 3A 09 30 2C 0A 09 09 09 22 62 73 6F 63 22 3A 09 30 2C 0A 09 09 09 22 6B 6D 6F 73 22 3A 09 30 0A 09 09 7D 2C 20 7B 0A 09 09 09 22 6B 69 64 22 3A 09 31 31 32 31 31 31 32 30 30 30 30 32 37 2C 0A 09 09 09 22 62 69 64 22 3A 09 30 2C 0A 09 09 09 22 62 73 6F 63 22 3A 09 30 2C 0A 09 09 09 22 6B 6D 6F 73 22 3A 09 30 0A 09 09 7D 2C 20 7B 0A 09 09 09 22 6B 69 64 22 3A 09 31 31 32 31 31 31 32 30 30 30 30 32 36 2C 0A 09 09 09 22 62 69 64 22 3A 09 30 2C 0A 09 09 09 22 62 73 6F 63 22 3A 09 30 2C 0A 09 09 09 22 6B 6D 6F 73 22 3A 09 30 0A 09 09 7D 2C 20 7B 0A 09 09 09 22 6B 69 64 22 3A 09 31 31 32 31 31 31 32 30 30 30 30 32 35 2C 0A 09 09 09 22 62 69 64 22 3A 09 30 2C 0A 09 09 09 22 62 73 6F 63 22 3A 09 30 2C 0A 09 09 09 22 6B 6D 6F 73 22 3A 09 30 0A 09 09 7D 2C 20 7B 0A 09 09 09 22 6B 69 64 22 3A 09 31 31 32 31 31 31 32 30 30 30 30 32 34 2C 0A 09 09 09 22 62 69 64 22 3A 09 30 2C 0A 09 09 09 22 62 73 6F 63 22 3A 09 30 2C 0A 09 09 09 22 6B 6D 6F 73 22 3A 09 30 0A 09 09 7D 2C 20 7B 0A 09 09 09 22 6B 69 64 22 3A 09 31 31 32 31 31 31 32 30 30 30 30 32 33 2C 0A 09 09 09 22 62 69 64 22 3A 09 30 2C 0A 09 09 09 22 62 73 6F 63 22 3A 09 30 2C 0A 09 09 09 22 6B 6D 6F 73 22 3A 09 30 0A 09 09 7D 2C 20 7B 0A 09 09 09 22 6B 69 64 22 3A 09 31 31 32 37 32 30 34 30 30 30 35 33 30 2C 0A 09 09 09 22 62 69 64 22 3A 09 30 2C 0A 09 09 09 22 62 73 6F 63 22 3A 09 30 2C 0A 09 09 09 22 6B 6D 6F 73 22 3A 09 30 0A 09 09 7D 2C 20 7B 0A 09 09 09 22 6B 69 64 22 3A 09 31 31 32 31 31 31 32 30 30 30 30 32 31 2C 0A 09 09 09 22 62 69 64 22 3A 09 30 2C 0A 09 09 09 22 62 73 6F 63 22 3A 09 30 2C 0A 09 09 09 22 6B 6D 6F 73 22 3A 09 30 0A 09 09 7D 5D 0A 7D 3C 2E 0D 0A";
//        tmp = "40 3A 03 5A 1C 75 0C 2F 15 3E 13 21 21 4F 21 6A 04 18 07 68 4F 3C 3C 0D 27 00 37 51 5F 1E 0B 48 5A 0C 08 27 01 C3 01 7B 0A 09 22 73 69 64 22 3A 09 39 31 32 37 32 30 34 30 30 30 36 36 35 2C 0A 09 22 73 63 66 22 3A 09 30 2C 0A 09 22 73 74 75 22 3A 09 30 2C 0A 09 22 73 75 72 22 3A 09 22 22 2C 0A 09 22 73 76 6C 22 3A 09 22 22 2C 0A 09 22 73 74 61 74 65 22 3A 09 5B 7B 0A 09 09 09 22 6B 69 64 22 3A 09 31 31 32 37 32 30 34 30 30 30 36 36 35 2C 0A 09 09 09 22 62 69 64 22 3A 09 30 2C 0A 09 09 09 22 62 73 6F 63 22 3A 09 30 2C 0A 09 09 09 22 6B 6D 6F 73 22 3A 09 33 30 32 0A 09 09 7D 2C 20 7B 0A 09 09 09 22 6B 69 64 22 3A 09 31 31 32 37 32 30 34 30 30 30 36 37 31 2C 0A 09 09 09 22 62 69 64 22 3A 09 30 2C 0A 09 09 09 22 62 73 6F 63 22 3A 09 30 2C 0A 09 09 09 22 6B 6D 6F 73 22 3A 09 32 39 39 0A 09 09 7D 2C 20 7B 0A 09 09 09 22 6B 69 64 22 3A 09 31 31 32 37 32 30 34 30 30 30 36 37 30 2C 0A 09 09 09 22 62 69 64 22 3A 09 30 2C 0A 09 09 09 22 62 73 6F 63 22 3A 09 30 2C 0A 09 09 09 22 6B 6D 6F 73 22 3A 09 32 38 38 0A 09 09 7D 2C 20 7B 0A 09 09 09 22 6B 69 64 22 3A 09 31 31 32 37 32 30 34 30 30 30 36 36 33 2C 0A 09 09 09 22 62 69 64 22 3A 09 30 2C 0A 09 09 09 22 62 73 6F 63 22 3A 09 30 2C 0A 09 09 09 22 6B 6D 6F 73 22 3A 09 32 39 34 0A 09 09 7D 2C 20 7B 0A 09 09 09 22 6B 69 64 22 3A 09 31 31 32 37 32 30 34 30 30 30 36 37 32 2C 0A 09 09 09 22 62 69 64 22 3A 09 30 2C 0A 09 09 09 22 62 73 6F 63 22 3A 09 30 2C 0A 09 09 09 22 6B 6D 6F 73 22 3A 09 33 30 30 0A 09 09 7D 2C 20 7B 0A 09 09 09 22 6B 69 64 22 3A 09 31 31 32 37 32 30 34 30 30 30 35 34 33 2C 0A 09 09 09 22 62 69 64 22 3A 09 30 2C 0A 09 09 09 22 62 73 6F 63 22 3A 09 30 2C 0A 09 09 09 22 6B 6D 6F 73 22 3A 09 32 39 31 0A 09 09 7D 2C 20 7B 0A 09 09 09 22 6B 69 64 22 3A 09 31 31 32 37 32 30 34 30 30 30 36 36 32 2C 0A 09 09 09 22 62 69 64 22 3A 09 30 2C 0A 09 09 09 22 62 73 6F 63 22 3A 09 30 2C 0A 09 09 09 22 6B 6D 6F 73 22 3A 09 32 39 30 0A 09 09 7D 2C 20 7B 0A 09 09 09 22 6B 69 64 22 3A 09 31 31 32 37 32 30 34 30 30 30 36 36 36 2C 0A 09 09 09 22 62 69 64 22 3A 09 30 2C 0A 09 09 09 22 62 73 6F 63 22 3A 09 30 2C 0A 09 09 09 22 6B 6D 6F 73 22 3A 09 32 39 33 0A 09 09 7D 2C 20 7B 0A 09 09 09 22 6B 69 64 22 3A 09 31 31 32 37 32 30 34 30 30 30 36 36 37 2C 0A 09 09 09 22 62 69 64 22 3A 09 30 2C 0A 09 09 09 22 62 73 6F 63 22 3A 09 30 2C 0A 09 09 09 22 6B 6D 6F 73 22 3A 09 33 30 31 0A 09 09 7D 2C 20 7B 0A 09 09 09 22 6B 69 64 22 3A 09 31 31 32 37 32 30 34 30 30 30 35 34 32 2C 0A 09 09 09 22 62 69 64 22 3A 09 30 2C 0A 09 09 09 22 62 73 6F 63 22 3A 09 30 2C 0A 09 09 09 22 6B 6D 6F 73 22 3A 09 32 39 39 0A 09 09 7D 5D 0A 7D 1B 36 0D 0A";
//        tmp = "40 3A 03 5A 1C 75 0C 2F 15 3E 13 21 21 4F 21 6A 04 18 07 68 4F 3C 3C 0D 27 00 37 51 5F 1E 0B 48 5A 0C 08 27 01 C3 01 7B 0A 09 22 73 69 64 22 3A 09 39 31 32 37 32 30 34 30 30 30 36 36 35 2C 0A 09 22 73 63 66 22 3A 09 30 2C 0A 09 22 73 74 75 22 3A 09 30 2C 0A 09 22 73 75 72 22 3A 09 22 22 2C 0A 09 22 73 76 6C 22 3A 09 22 22 2C 0A 09 22 73 74 61 74 65 22 3A 09 5B 7B 0A 09 09 09 22 6B 69 64 22 3A 09 31 31 32 37 32 30 34 30 30 30 36 36 35 2C 0A 09 09 09 22 62 69 64 22 3A 09 30 2C 0A 09 09 09 22 62 73 6F 63 22 3A 09 30 2C 0A 09 09 09 22 6B 6D 6F 73 22 3A 09 33 30 32 0A 09 09 7D 2C 20 7B 0A 09 09 09 22 6B 69 64 22 3A 09 31 31 32 37 32 30 34 30 30 30 36 37 31 2C 0A 09 09 09 22 62 69 64 22 3A 09 30 2C 0A 09 09 09 22 62 73 6F 63 22 3A 09 30 2C 0A 09 09 09 22 6B 6D 6F 73 22 3A 09 32 39 39 0A 09 09 7D 2C 20 7B 0A 09 09 09 22 6B 69 64 22 3A 09 31 31 32 37 32 30 34 30 30 30 36 37 30 2C 0A 09 09 09 22 62 69 64 22 3A 09 30 2C 0A 09 09 09 22 62 73 6F 63 22 3A 09 30 2C 0A 09 09 09 22 6B 6D 6F 73 22 3A 09 32 38 38 0A 09 09 7D 2C 20 7B 0A 09 09 09 22 6B 69 64 22 3A 09 31 31 32 37 32 30 34 30 30 30 36 36 33 2C 0A 09 09 09 22 62 69 64 22 3A 09 30 2C 0A 09 09 09 22 62 73 6F 63 22 3A 09 30 2C 0A 09 09 09 22 6B 6D 6F 73 22 3A 09 32 39 34 0A 09 09 7D 2C 20 7B 0A 09 09 09 22 6B 69 64 22 3A 09 31 31 32 37 32 30 34 30 30 30 36 37 32 2C 0A 09 09 09 22 62 69 64 22 3A 09 30 2C 0A 09 09 09 22 62 73 6F 63 22 3A 09 30 2C 0A 09 09 09 22 6B 6D 6F 73 22 3A 09 33 30 30 0A 09 09 7D 2C 20 7B 0A 09 09 09 22 6B 69 64 22 3A 09 31 31 32 37 32 30 34 30 30 30 35 34 33 2C 0A 09 09 09 22 62 69 64 22 3A 09 30 2C 0A 09 09 09 22 62 73 6F 63 22 3A 09 30 2C 0A 09 09 09 22 6B 6D 6F 73 22 3A 09 32 39 31 0A 09 09 7D 2C 20 7B 0A 09 09 09 22 6B 69 64 22 3A 09 31 31 32 37 32 30 34 30 30 30 36 36 32 2C 0A 09 09 09 22 62 69 64 22 3A 09 30 2C 0A 09 09 09 22 62 73 6F 63 22 3A 09 30 2C 0A 09 09 09 22 6B 6D 6F 73 22 3A 09 32 39 30 0A 09 09 7D 2C 20 7B 0A 09 09 09 22 6B 69 64 22 3A 09 31 31 32 37 32 30 34 30 30 30 36 36 36 2C 0A 09 09 09 22 62 69 64 22 3A 09 30 2C 0A 09 09 09 22 62 73 6F 63 22 3A 09 30 2C 0A 09 09 09 22 6B 6D 6F 73 22 3A 09 32 39 33 0A 09 09 7D 2C 20 7B 0A 09 09 09 22 6B 69 64 22 3A 09 31 31 32 37 32 30 34 30 30 30 36 36 37 2C 0A 09 09 09 22 62 69 64 22 3A 09 30 2C 0A 09 09 09 22 62 73 6F 63 22 3A 09 30 2C 0A 09 09 09 22 6B 6D 6F 73 22 3A 09 33 30 31 0A 09 09 7D 2C 20 7B 0A 09 09 09 22 6B 69 64 22 3A 09 31 31 32 37 32 30 34 30 30 30 35 34 32 2C 0A 09 09 09 22 62 69 64 22 3A 09 30 2C 0A 09 09 09 22 62 73 6F 63 22 3A 09 30 2C 0A 09 09 09 22 6B 6D 6F 73 22 3A 09 32 39 39 0A 09 09 7D 5D 0A 7D 1B 36 0D 0A";
//        ByteBuf message = ByteBufAllocator.DEFAULT.buffer();
//        byte[] tmp_bytes = ByteUtil.toByteArray(tmp);
//        message.writeBytes(tmp_bytes);
//
//
//        HeaderCode head = new HeaderCode();
//        head.setSTART_CODE_1((byte)Integer.parseInt("40",16));
//        head.setSTART_CODE_2((byte)Integer.parseInt("3A",16));
//        head.setEND_CODE_1((byte)Integer.parseInt("0D",16));
//        head.setEND_CODE_2((byte)Integer.parseInt("0A",16));
//        head.setTOKEN_LENGTH(32);
//
//        SocketServerConfig config = new SocketServerConfig();
//        config.setLoginFunctionCode(49153);
//
//        LoggerConfig loggerConfig = new LoggerConfig();
//        KMDecoder decoder = new KMDecoder(head,config,loggerConfig);
//
//        decoder.decode(null,message,new ArrayList<>());
//
//    }
}
