package com.kingmeter.chargingold.socket.business.utils;

import com.kingmeter.chargingold.socket.acl.ChargingSiteService;
import com.kingmeter.chargingold.socket.business.code.ClientFunctionCodeType;
import com.kingmeter.chargingold.socket.business.code.ServerFunctionCodeType;
import com.kingmeter.common.KingMeterException;
import com.kingmeter.common.ResponseCode;
import com.kingmeter.dto.charging.v2.socket.out.QueryDockInfoResponseDto;
import com.kingmeter.socket.framework.business.WorkerTemplate;
import com.kingmeter.socket.framework.codec.KMDecoder;
import com.kingmeter.socket.framework.config.HeaderCode;
import com.kingmeter.socket.framework.config.LoggerConfig;
import com.kingmeter.socket.framework.config.SocketServerConfig;
import com.kingmeter.socket.framework.dto.ResponseBody;
import com.kingmeter.socket.framework.strategy.RequestStrategy;
import com.kingmeter.socket.framework.util.CacheUtil;
import com.kingmeter.utils.ByteUtil;
import com.kingmeter.utils.StringUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

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

    public static void main(String[] args) throws Exception{
//        printInfo();
    }

    private static void printInfo(){
        String tmp = "40 3A 00 68 35 2D 70 39 38 4F 70 04 11 16 09 5A 64 4E 46 45 5E 1D 60 69 25 7C 5F 7E 69 19 76 61 75 08 01 09 01 C0 01 7B 0A 09 22 73 69 64 22 3A 09 39 31 32 31 39 38 32 30 34 30 31 32 30 2C 0A 09 22 70 77 64 22 3A 09 22 45 31 30 41 44 43 33 39 34 39 42 41 35 39 41 42 42 45 35 36 45 30 35 37 46 32 30 46 38 38 33 45 22 0A 7D 95 6F 0D 0A";
        ByteBuf message = ByteBufAllocator.DEFAULT.buffer();
        byte[] tmp_bytes = ByteUtil.toByteArray(tmp);
        message.writeBytes(tmp_bytes);


        HeaderCode head = new HeaderCode();
        head.setSTART_CODE_1((byte)Integer.parseInt("40",16));
        head.setSTART_CODE_2((byte)Integer.parseInt("3A",16));
        head.setEND_CODE_1((byte)Integer.parseInt("0D",16));
        head.setEND_CODE_2((byte)Integer.parseInt("0A",16));
        head.setTOKEN_LENGTH(32);

        SocketServerConfig config = new SocketServerConfig();
        config.setLoginFunctionCode(49153);

        KMDecoder decoder = new KMDecoder(head,config);

        decoder.decode(null,message,new ArrayList<>());
    }

}
