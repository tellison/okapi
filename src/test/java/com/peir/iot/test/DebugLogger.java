package com.peir.iot.test;

public class DebugLogger {
    
    public DebugLogger() {
        super();
    }

    public DebugLogger log(String message) {
        System.out.println("[mock BMP180 device] " + message);
        return this;
    }
    
}
