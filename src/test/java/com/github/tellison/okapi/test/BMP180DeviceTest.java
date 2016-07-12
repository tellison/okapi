/*
 * Copyright 2016 Tim Ellison
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.tellison.okapi.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.tellison.okapi.device.BMP180Device;
import com.github.tellison.okapi.device.BMP180SamplingMode;
import com.pi4j.io.i2c.I2CFactory;

/**
 * Unit tests for the BMP180 device.
 */
public class BMP180DeviceTest {

    private BMP180Device device;
    private boolean usingRealDevice;

    @Before
    public void setUp() throws IOException {
        // To test the logic we use a mock device for the tests.
        // To run a less rigorous set of tests on the real device
        // specify -DuseRealDevice on the command-line
        usingRealDevice = System.getProperty("useRealDevice") != null;

        // Install a factory that produces a mock I2C device for testing in
        // absence of the real device.
        if (!usingRealDevice) {
            I2CFactory.setFactory(new MockFactory());
        }

        // Create a device on our mock I2C bus.
        device = new BMP180Device();
        assertNotNull(device);
    }

    @After
    public void tearDown() throws IOException {
        device.close();
    }

    /**
     * Check we can create a new instance of the device. This will initialize
     * the I2C communications and read the configuration settings.
     */
    @Test
    public void testConstructor() throws IOException {
        BMP180Device bmp180 = new BMP180Device();
        assertNotNull(bmp180);
    }

    /**
     * Should return known const ID.
     * 
     * @throws IOException
     */
    @Test
    public void testGetChipID() throws IOException {
        assertEquals(BMP180Device.DEVICE_ID, device.getChipID());
    }

    /**
     * Check we can reset the device.
     */
    @Test
    public void testSoftReset() throws IOException {
        // No response expected.
        device.softReset();
    }

    /**
     * Check we can get temp and pressure at default settings.
     */
    @Test
    public void testGetTemperatureAndPressure() throws IOException {
        float[] values = device.getTemperatureAndPressure(BMP180SamplingMode.ULTRA_LOW_POWER);
        checkValues(values[0], values[1]);
    }

    /**
     * Check we can get temp and pressure at various sampling settings.
     */
    @Test
    public void testGetTemperatureAndPressureSampling() throws IOException {
        for (BMP180SamplingMode mode : BMP180SamplingMode.values()) {
            float[] values = device.getTemperatureAndPressure(mode);
            checkValues(values[0], values[1]);
        }
    }

    /* Check the given values match our expectations. */
    private void checkValues(float temperature, float pressure) {
        System.out.printf("Temp=%f, Pressure=%f", temperature, pressure);
        if (usingRealDevice) {
            // The real device gives actual values. We don't know what they are.
            // Assume we are in reasonable earthy temperatures
            assertTrue(Float.isFinite(temperature));
            assertTrue(temperature > -10.0);
            assertTrue(temperature < 40.0);
            // Assume we are in the device's specific valid range (+9000m to
            // -500m relating to sea level)
            assertTrue(Float.isFinite(pressure));
            assertTrue(temperature > 300.0);
            assertTrue(temperature < 1100.0);
        } else {
            // The mock device always gives data sheet example results.
            assertEquals(15.0, temperature, 0.1f);
            assertEquals(699.64, pressure, 0.1f);
        }
    }

    /**
     * Check we can close the device.
     */
    @Test
    public void testClose() throws IOException {
        BMP180Device local = new BMP180Device();
        local.close();

        try {
            local.close();
            fail("Should throw exception on closing a closed device.");
        } catch (IOException ex) {
            // expected
        }
    }
}
