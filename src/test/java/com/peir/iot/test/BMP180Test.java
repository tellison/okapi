package com.peir.iot.test;

import java.io.IOException;

import com.peir.iot.device.BMP180Device;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactoryProvider;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * TODO: Unit test for simple App.
 */
public class BMP180Test extends TestCase {

    public BMP180Test(String testName) {
        super(testName);
    }

    protected void setUp() {
        // Install a factory that produces a mock I2C device for testing in
        // absence of the real device.
        I2CFactory.setFactory(new MockFactory());
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(BMP180Test.class);
    }

    /**
     * Check we can create a new instance of the device. This will initialize
     * the I2C communications and read the configurationn settings.
     */
    public void testConstructor() throws IOException {
        BMP180Device device = new BMP180Device();
        assertNotNull(device);
        System.out.println(device);
    }
}
