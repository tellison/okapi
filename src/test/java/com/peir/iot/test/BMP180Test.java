package com.peir.iot.test;

import java.io.IOException;

import com.peir.iot.device.BMP180Device;
import com.pi4j.io.i2c.I2CFactory;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * TODO: Unit test for simple App.
 */
public class BMP180Test extends TestCase {

    private BMP180Device device;

    public BMP180Test(String testName) {
        super(testName);
    }

    protected void setUp() throws IOException {
        // Install a factory that produces a mock I2C device for testing in
        // absence of the real device.
        I2CFactory.setFactory(new MockFactory());

        // Create a device on our mock I2C bus.
        device = new BMP180Device();
        assertNotNull(device);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(BMP180Test.class);
    }

    /**
     * Check we can create a new instance of the device. This will initialize
     * the I2C communications and read the configuration settings.
     */
    public void testConstructor() throws IOException {
        BMP180Device bmp180 = new BMP180Device();
        assertNotNull(bmp180);
        //System.out.println(bmp180);
    }

    public void testGetChipID() throws IOException {
        assertEquals(BMP180Device.DEVICE_ID, device.getChipID());
    }

    /**
     * Check we can reset the device.
     */
    public void testSoftReset() throws IOException {
        // No response expected.
        device.softReset();
    }
    
    /**
     * Check we can get temp and pressure at default settings.
     */
    public void testGetTemperatureAndPressure() throws IOException {
        float[] values = device.getTemperatureAndPressure();
        System.out.printf("Temp=%f, Pressure=%f", values[0], values[1]);
    }

}
