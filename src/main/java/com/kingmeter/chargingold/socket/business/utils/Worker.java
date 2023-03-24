package com.kingmeter.chargingold.socket.business.utils;

import com.kingmeter.chargingold.socket.acl.ChargingSiteService;
import com.kingmeter.chargingold.socket.business.code.ClientFunctionCodeType;
import com.kingmeter.chargingold.socket.business.code.ServerFunctionCodeType;
import com.kingmeter.common.KingMeterException;
import com.kingmeter.common.ResponseCode;
import com.kingmeter.dto.charging.v1.socket.in.SiteLoginRequestDto;
import com.kingmeter.dto.charging.v1.socket.out.ConfigureSiteInfoResponseDto;
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
import com.kingmeter.utils.CRCUtils;
import com.kingmeter.utils.StringUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
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
        return (RequestStrategy) getBean(ClientFunctionCodeType.getEnum(functionCode).getClassName());
    }

    @Override
    public void doDealWithOffline(SocketChannel channel, String deviceId) {
        if (StringUtil.isNotEmpty(deviceId)) {
            chargingSiteService.offlineNotify(Long.parseLong(deviceId));
            HeartUpdateCount.siteHeartBeatCount.remove(Long.parseLong(deviceId));
//            if (CacheUtil.getInstance().getDeviceResultMap().containsKey(deviceId + "_queryDockInfoFlag")) {
//                CacheUtil.getInstance().getDeviceResultMap().remove(deviceId + "_queryDockInfoFlag");
//            }
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
////        printInfo();
////        encode();
//        System.out.println("7B 22 73 69 74 65 5F 70 61 73 73 77 6F 72 64 22 3A 22 31 32 33 34 35 36 22 2C 22 6C 6F 67 69 6E 5F 70 6F 72 74 22 3A 31 30 30 33 33 2C 22 73 69 74 65 5F 69 64 22 3A 39 31 32 31 39 38 32 30 34 30 30 30 38 2C 22 6E 65 74 5F 77 69 66 69 70 73 64 22 3A 22 6B 69 6E 67 D9 01 00 00 95 01 00 00 6C 6F 67 69 6E 5F 69 70 22 3A 22 63 68 61 72 67 69 6E 67 2E 6B 6D 69 6F 74 2E 67 72 6F 75 70 22 2C 22 6E 65 74 5F 77 69 66 69 6E 61 6D 65 22 3A 22 4B 49 4E 47 4D 45 54 45 52 22 7D".replaceAll(" ",""));
//    }

    private static void printInfo() {
        String tmp = "40 3A 00 BF 62 7A 6C 0A 7C 65 25 03 6D 09 54 5F 0A 24 71 59 2B 34 10 06 6D 59 60 39 1F 1D 7B 59 32 1F 55 42 00 F1 01 7B 22 73 69 74 65 5F 70 61 73 73 77 6F 72 64 22 3A 22 31 32 33 34 35 36 22 2C 22 6C 6F 67 69 6E 5F 70 6F 72 74 22 3A 31 30 30 33 33 2C 22 73 69 74 65 5F 69 64 22 3A 39 31 32 31 39 38 32 30 34 30 30 30 38 2C 22 6E 65 74 5F 77 69 66 69 70 73 64 22 3A 22 6B 69 6E 67 D9 01 00 00 95 01 00 00 6C 6F 67 69 6E 5F 69 70 22 3A 22 63 68 61 72 67 69 6E 67 2E 6B 6D 69 6F 74 2E 67 72 6F 75 70 22 2C 22 6E 65 74 5F 77 69 66 69 6E 61 6D 65 22 3A 22 4B 49 4E 47 4D 45 54 45 52 22 7D 68 9F 0D 0A";
        ByteBuf message = ByteBufAllocator.DEFAULT.buffer();
        byte[] tmp_bytes = ByteUtil.toByteArray(tmp);
        message.writeBytes(tmp_bytes);


        HeaderCode head = new HeaderCode();
        head.setSTART_CODE_1((byte) Integer.parseInt("40", 16));
        head.setSTART_CODE_2((byte) Integer.parseInt("3A", 16));
        head.setEND_CODE_1((byte) Integer.parseInt("0D", 16));
        head.setEND_CODE_2((byte) Integer.parseInt("0A", 16));
        head.setTOKEN_LENGTH(32);

        SocketServerConfig config = new SocketServerConfig();
        config.setLoginFunctionCode(49153);

        KMDecoder decoder = new KMDecoder(head, config);
        decoder.decode(null, message, new ArrayList<>());
    }

//    public static void main(String[] args) {
//        encode2();
//    }

    private static void encode2() {
//        SiteLoginRequestDto
        SiteLoginRequestDto requestDto = new SiteLoginRequestDto();
        requestDto.setSid(9000000000001l);
        requestDto.setPwd("E10ADC3949BA59ABBE56E057F20F883E");
        String token = "00000000000000000000000000000000";


        ResponseBody response = new ResponseBody();
        response.setTokenArray(token.getBytes());
        byte[] array = {(byte) 192, (byte) 1};
        response.setFunctionCodeArray(array);//{(byte) 192, (byte) 1}
        response.setData(toJSON(requestDto).toString());
        response.setSTART_CODE_1((byte) 64);
        response.setSTART_CODE_2((byte) 58);
        response.setEND_CODE_1((byte) 13);
        response.setEND_CODE_2((byte) 10);
        response.setToken_length(32);
        response.setDeviceId(9000000000001l);

        byte[] dataArray = response.getData().getBytes();

        int dataCountLength = response.getToken_length() + 3 + dataArray.length;

        byte[] result = new byte[dataCountLength + 8];
        byte[] checkByteArray = new byte[dataCountLength];

        result[0] = response.getSTART_CODE_1();
        result[1] = response.getSTART_CODE_2();

        result[2] = (byte) (dataCountLength / 256);
        result[3] = (byte) (dataCountLength % 256);

        System.arraycopy(response.getTokenArray(), 0, checkByteArray,
                0, response.getToken_length());

        checkByteArray[response.getToken_length()] = 0;

        System.arraycopy(response.getFunctionCodeArray(), 0, checkByteArray,
                response.getToken_length() + 1, response.getFunctionCodeArray().length);

        //6, data
        System.arraycopy(dataArray, 0, checkByteArray,
                response.getToken_length() + 1 + response.getFunctionCodeArray().length,
                dataArray.length);

        byte[] checkTmp = CRCUtils.getInstance().getCheckCrcArray(checkByteArray);

        System.arraycopy(checkByteArray, 0, result, 4, checkByteArray.length);

        result[checkByteArray.length + 4] = checkTmp[0];
        result[checkByteArray.length + 5] = checkTmp[1];

        result[checkByteArray.length + 6] = response.getEND_CODE_1();
        result[checkByteArray.length + 7] = response.getEND_CODE_2();


        for (byte a:result) {
            System.out.print((int)a+" ");
        }

        System.out.println();

        System.out.println(ByteUtil.bytesToHexString(result));

    }

    private static void encode() {
        ConfigureSiteInfoResponseDto responseDto = new ConfigureSiteInfoResponseDto(
                9121982040008l, "123456",
                "192.168.1.100", 10033,
                "km_local", "km_local");
        String token = "256C582C293704734716075B6B493678023A0200009501000047202C5379035F";

        ResponseBody response = new ResponseBody();
        response.setTokenArray(token.getBytes());
        response.setFunctionCodeArray(ServerFunctionCodeType.QueryDockInfo);
        response.setData(toJSON(responseDto).toString());
        response.setSTART_CODE_1((byte) 64);
        response.setSTART_CODE_2((byte) 58);
        response.setEND_CODE_1((byte) 13);
        response.setEND_CODE_2((byte) 10);
        response.setToken_length(32);
        response.setDeviceId(9121982040008l);

        byte[] dataArray = response.getData().getBytes();

        int dataCountLength = response.getToken_length() + 3 + dataArray.length;

        byte[] result = new byte[dataCountLength + 8];
        byte[] checkByteArray = new byte[dataCountLength];

        result[0] = response.getSTART_CODE_1();
        result[1] = response.getSTART_CODE_2();

        result[2] = (byte) (dataCountLength / 256);
        result[3] = (byte) (dataCountLength % 256);

        System.arraycopy(response.getTokenArray(), 0, checkByteArray,
                0, response.getToken_length());

        checkByteArray[response.getToken_length()] = 0;

        System.arraycopy(response.getFunctionCodeArray(), 0, checkByteArray,
                response.getToken_length() + 1, response.getFunctionCodeArray().length);

        //6, data
        System.arraycopy(dataArray, 0, checkByteArray,
                response.getToken_length() + 1 + response.getFunctionCodeArray().length,
                dataArray.length);

        byte[] checkTmp = CRCUtils.getInstance().getCheckCrcArray(checkByteArray);

        System.arraycopy(checkByteArray, 0, result, 4, checkByteArray.length);

        result[checkByteArray.length + 4] = checkTmp[0];
        result[checkByteArray.length + 5] = checkTmp[1];

        result[checkByteArray.length + 6] = response.getEND_CODE_1();
        result[checkByteArray.length + 7] = response.getEND_CODE_2();

        System.out.println(result);
    }

}
