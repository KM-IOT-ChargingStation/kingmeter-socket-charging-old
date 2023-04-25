package com.kingmeter.chargingold.socket.rest;

import lombok.Data;

/**
 * @description:
 * @author: crazyandy
 */

@Data
public class TestRemoteLockDto {
    private long siteId;
    private long dockId;
    private int intervalTime;
    private long startTimeStamp;
}
