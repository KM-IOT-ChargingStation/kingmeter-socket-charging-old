package com.kingmeter.chargingold.socket.rest;


import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ConnectionDto {
    private String deviceId;
    private String host;
    private int port;
}
