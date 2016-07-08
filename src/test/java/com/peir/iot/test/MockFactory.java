package com.peir.iot.test;

import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactoryProvider;

import junit.framework.TestCase;

class MockFactory extends TestCase implements I2CFactoryProvider {

    @Override
    public I2CBus getBus(int busNumber) throws IOException {
        assertEquals(I2CBus.BUS_1, busNumber);
        return new MockI2CBus();
    }
}
