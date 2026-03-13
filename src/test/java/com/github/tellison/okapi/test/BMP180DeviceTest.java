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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.tellison.okapi.device.BMP180Device;
import com.github.tellison.okapi.device.BMP180SamplingMode;
import com.pi4j.io.i2c.I2CFactory;

/**
 * Unit tests for the BMP180 device.
 */
public class BMP180DeviceTest {

    private BMP180Device device;
    private boolean usingRealDevice;

    @BeforeEach
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
        MockI2CDevice.resetSimulations();
        device = new BMP180Device();
        assertNotNull(device);
    }

    @AfterEach
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
        float[] values = device.getTemperatureAndPressure();
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
        System.out.printf("Temp=%f, Pressure=%f\n", temperature, pressure);
        if (usingRealDevice) {
            // The real device gives actual values. We don't know what they are.
            // Assume we are in reasonable earthy temperatures
            assertTrue(Float.isFinite(temperature));
            assertTrue(temperature > -10.0, "Temperature too low");
            assertTrue(temperature < 40.0, "Temperature too high");
            // Assume we are in the device's specific valid range (+9000m to
            // -500m relating to sea level)
            assertTrue(Float.isFinite(pressure));
            assertTrue(pressure > 300.0, "Pressure too low");
            assertTrue(pressure < 1100.0, "Pressure too high");
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
        tearDown();
        try {
            // The real device requires some time to reset.
            if (usingRealDevice) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
        }
        try {
            BMP180Device local = new BMP180Device();
            local.close();

            assertThrows(IOException.class, () -> local.close(), "Should throw exception on closing a closed device.");
            assertThrows(IOException.class, () -> local.getChipID(), "Should throw exception on interacting with a closed device.");
            assertThrows(IOException.class, () -> local.getTemperature(), "Should throw exception on interacting with a closed device.");
            assertThrows(IOException.class, () -> local.softReset(), "Should throw exception on interacting with a closed device.");

        } finally {
            setUp();
        }
    }

    /**
     * Negative tests validating error handling.
     */
    @Test
    public void testShortCalibrationRead() throws IOException {
        if (usingRealDevice) return;
        MockI2CDevice.simulateShortCalibRead = true;
        IOException exception = assertThrows(IOException.class, () -> new BMP180Device());
        assertTrue(exception.getMessage().contains("Only read "));
    }

    @Test
    public void testBadCalibrationData() throws IOException {
        if (usingRealDevice) return;
        MockI2CDevice.simulateBadCalibData = true;
        IOException exception = assertThrows(IOException.class, () -> new BMP180Device());
        assertTrue(exception.getMessage().contains("Error reading valid calibration data"));
    }

    @Test
    public void testShortDeviceIDRead() throws IOException {
        if (usingRealDevice) return;
        MockI2CDevice.simulateShortIdRead = true;
        IOException exception = assertThrows(IOException.class, () -> device.getChipID());
        assertTrue(exception.getMessage().contains("Error reading device id. Expected 1 byte"));
    }

    @Test
    public void testShortTemperatureRead() throws IOException {
        if (usingRealDevice) return;
        MockI2CDevice.simulateShortTempRead = true;
        IOException exception = assertThrows(IOException.class, () -> device.getTemperature());
        assertTrue(exception.getMessage().contains("Error reading temperature. Expected 2 bytes"));
    }

    @Test
    public void testShortPressureRead() throws IOException {
        if (usingRealDevice) return;
        MockI2CDevice.simulateShortPressureRead = true;
        IOException exception = assertThrows(IOException.class, () -> device.getPressure(BMP180SamplingMode.STANDARD));
        assertTrue(exception.getMessage().contains("Error reading pressure.  Expected 3 bytes"));
    }

    public static void main(String[] args) throws IOException {
        BMP180DeviceTest me = new BMP180DeviceTest();
        me.setUp();
        me.testConstructor();
        me.testGetChipID();
        me.testGetTemperatureAndPressure();
        me.testGetTemperatureAndPressureSampling();
        me.testSoftReset();
        me.testClose();
        me.tearDown();
    }
}
