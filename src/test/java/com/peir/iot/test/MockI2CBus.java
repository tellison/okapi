package com.peir.iot.test;

import java.io.IOException;

import com.peir.iot.device.BMP180Device;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;

import junit.framework.TestCase;

class MockI2CBus extends TestCase implements I2CBus {
    @Override
    public I2CDevice getDevice(int address) throws IOException {
        assertEquals(BMP180Device.DEVICE_I2C_ADDRESS, address);
        return new MockI2CDevice();
    }

    @Override
    public String getFileName() {
        return "Undefined file name";
    }

    @Override
    public int getFileDescriptor() {
        return -1;
    }

    @Override
    public void close() throws IOException {
        // Do nothing
    }
}
