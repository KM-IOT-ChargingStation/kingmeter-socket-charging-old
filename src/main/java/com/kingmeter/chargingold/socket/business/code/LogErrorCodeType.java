package com.kingmeter.chargingold.socket.business.code;

import java.util.HashMap;
import java.util.Map;

/**
 * @description:
 * @author: crazyandy
 */
public enum LogErrorCodeType {

    Restart(0,"T2 Restart by %s"),
    OffLine(1,"HeartBeat 6 times offline"),
    HeadError(2,"Heard Error"),
    TailError(3,"Tail Error"),
    CrcError(4,"Crc Error"),
    ReLogin(5,"Site send 6 heartbeat,no reply,login again"),
    ReBoot(6,"Site receive ff01 ,login again"),
    HeartBeatForbidden(7,"HeartBeat force login again"),
    ReLoginFailed(8,"ReLogin failed"),
    ReLoginFailed6Times(9,"ReLogin 6 times failed"),
    NetRecover(10,"Network recover"),
    NetDisconnectCount(11,"HeartBeat get response until sending %s times"),
    PowerResetCard(12,"Power Reset card cause reboot"),
    CannotConnectServer(13,"Cannot connect to server"),
    CannotSendDataToServer(14,"Cannot send data to server");


    private int code;
    private String desc;

    LogErrorCodeType(int code,String desc){
        this.code = code;
        this.desc = desc;
    }

    public int code() {
        return code;
    }

    public String desc() {
        return desc;
    }

    static Map<Integer, LogErrorCodeType> enumMap = new HashMap();

    static {
        for (LogErrorCodeType type : LogErrorCodeType.values()) {
            enumMap.put(type.code(), type);
        }
    }

    public static LogErrorCodeType getEnum(Integer code) {
        return enumMap.get(code);
    }

    public static boolean containsValue(Integer code) {
        return enumMap.containsKey(code);
    }
}
