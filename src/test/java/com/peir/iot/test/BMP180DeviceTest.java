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

package com.peir.iot.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.peir.iot.device.BMP180Device;
import com.peir.iot.device.BMP180SamplingMode;
import com.pi4j.io.i2c.I2CFactory;

/**
 * Unit tests for the BMP180 device.
 */
public class BMP180DeviceTest {

    private BMP180Device device;

    @Before
    public void setUp() throws IOException {
        // Install a factory that produces a mock I2C device for testing in
        // absence of the real device.
        I2CFactory.setFactory(new MockFactory());

        // Create a device on our mock I2C bus.
        device = new BMP180Device();
        assertNotNull(device);
    }

    /**
     * Check we can create a new instance of the device. This will initialize
     * the I2C communications and read the configuration settings.
     */
    @Test
    public void testConstructor() throws IOException {
        BMP180Device bmp180 = new BMP180Device();
        assertNotNull(bmp180);
        // System.out.println(bmp180);
    }

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
        System.out.printf("Temp=%f, Pressure=%f", values[0], values[1]);
        assertEquals(15.0, values[0], 0.01f);
        assertEquals(699.64, values[1], 0.01f);
    }
}
