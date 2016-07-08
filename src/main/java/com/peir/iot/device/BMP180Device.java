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

package com.peir.iot.device;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

/**
 * A BMP180 device connected via I2C.
 * 
 * Instances of this class represent a
 * <a href="http://www.datasheetspdf.com/datasheet/BMP180.html">Bosch BMP180
 * digital temperature and pressure sensor</a>.
 *
 */
public class BMP180Device {

    // Device information.
    public static final int DEVICE_I2C_ADDRESS = 0x77;
    public static final int DEVICE_ID = 0x55;

    // Temperature and pressure control register information.
    private static final int CALIB_REGISTER_ADDRESS = 0xAA;
    private static final int ID_REGISTER_ADDRESS = 0xD0;
    private static final int SOFT_RESET_ADDRESS = 0xE0;
    private static final int CONTROL_REGISTER_ADDRESS = 0xF4;
    private static final int READ_PRESSURE_ADDRESS = 0xF6;
    private static final int READ_TEMPERATURE_ADDRESS = 0xF6;

    // Command information.
    private static final byte SOFT_RESET_COMMAND = (byte) 0xB6;
    private static final byte READ_PRESSURE_COMMAND = (byte) 0x34;
    private static final byte READ_TEMPERATURE_COMMAND = (byte) 0x2E;

    // Calibration data information.
    private static final int CALIB_BYTES_LENGTH = 22;

    // Device reference.
    private I2CDevice device;

    // Calibration coefficients for this individual device.
    private short ac1, ac2, ac3;
    private int ac4, ac5, ac6;
    private short b1, b2, mb, mc, md;

    /**
     * Constructs a new representation of the device, and reads it's calibration
     * information.
     * 
     * @throws IOException
     */
    public BMP180Device() throws IOException {
        super();
        I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1);
        device = bus.getDevice(DEVICE_I2C_ADDRESS);
        readCalibrationData();
    }

    /*
     * Reads the calibration data from the device, and initialise the local
     * variables. Every sensor has 11 individual calibration coefficients.
     * 
     * @throws IOException An exception occurred reading data from the device.
     */
    private void readCalibrationData() throws IOException {
        // Read all of the calibration data into a byte array.
        byte[] calibData = new byte[CALIB_BYTES_LENGTH];
        int result = device.read(CALIB_REGISTER_ADDRESS, calibData, 0, CALIB_BYTES_LENGTH);
        if (result < CALIB_BYTES_LENGTH) {
            throw new IOException("Error reading calibration data.  Only read " + result);
        }

        // Extract calibration data values (see device data sheet).
        try (DataInputStream calibDataStream = new DataInputStream(new ByteArrayInputStream(calibData));) {
            ac1 = calibDataStream.readShort();
            ac2 = calibDataStream.readShort();
            ac3 = calibDataStream.readShort();
            ac4 = calibDataStream.readUnsignedShort();
            ac5 = calibDataStream.readUnsignedShort();
            ac6 = calibDataStream.readUnsignedShort();
            b1 = calibDataStream.readShort();
            b2 = calibDataStream.readShort();
            mb = calibDataStream.readShort();
            mc = calibDataStream.readShort();
            md = calibDataStream.readShort();

            // No value should be 0 or 0xFFFF if communications working.
            if (ac1 == 0 || ac2 == 0 || ac3 == 0 || ac4 == 0 || ac5 == 0 || ac6 == 0 || b1 == 0 || b2 == 0 || mb == 0
                    || mc == 0 || md == 0 || ac1 == 0xFFFF || ac2 == 0xFFFF || ac3 == 0xFFFF || ac4 == 0xFFFF
                    || ac5 == 0xFFFF || ac6 == 0xFFFF || b1 == 0xFFFF || b2 == 0xFFFF || mb == 0xFFFF || mc == 0xFFFF
                    || md == 0xFFFF) {
                throw new IOException("Error reading valid calibration data from device.");
            }
        }
    }

    /**
     * Returns the chip ID for this device.
     * 
     * The chip ID is always 0x55. This method can be used to ensure the device
     * is communicating correctly.
     * 
     * @throws IOException
     *             if there is an error communicating with the device.
     * @returns the device ID (0x55)
     */
    public int getChipID() throws IOException {
        byte[] data = new byte[2];
        int result = device.read(ID_REGISTER_ADDRESS, data, 0, data.length);
        if (result < data.length) {
            throw new IOException("Error reading device id. Expected 2 bytes but got " + result);
        }
        // Extract the device id
        return ((data[0] << 8) & 0xFF00) + (data[1] & 0xFF);
    }

    public float getTemperature() throws IOException {

        long ut = getUncalibratedTemperature();

        // Calculate the temperature compensation factor
        long t1 = ((ut - ac6) * ac5) >> 15;
        long t2 = ((long) mc << 11) / (t1 + md);
        long b5 = t1 + t2;

        // Calculate the real temperature
        return (float) ((b5 + 8) >> 4) / 10;
    }
    
    /**
     * Reads temperature and pressure from the device in standard mode.
     * 
     * The temperature is the true calibrated value, expressed in degrees
     * Celsius, and provided in steps of 0.1 deg.C. The pressure is the true
     * calibrated and temperature compensated value, expressed in hPa, and
     * provided in steps of 0.01hPa (0.01mbar).
     * 
     * @return the temperature and pressure values in a two element array of
     *         floats, where array[0] is the temperature and array[1] is the
     *         pressure.
     * 
     * @throws IOException
     *             An error occurred reading from the device.
     */
    public synchronized float[] getTemperatureAndPressure() throws IOException {
        return getTemperatureAndPressure(BMP180SamplingMode.STANDARD);
    }

    /**
     * Reads temperature and pressure from the device in the given mode.
     * 
     * The temperature is the true calibrated value, expressed in degrees
     * Celsius, and provided in steps of 0.1 deg.C. The pressure is the true
     * calibrated and temperature compensated value, expressed in hPa, and
     * provided in steps of 0.01hPa (0.01mbar).
     * 
     * @param mode
     *            the sampling mode requested for the device readings.
     * 
     * @return the temperature and pressure values in a two element array of
     *         floats, where array[0] is the temperature and array[1] is the
     *         pressure.
     * 
     * @throws IOException
     *             An error occurred reading from the device.
     */
    public synchronized float[] getTemperatureAndPressure(BMP180SamplingMode mode) throws IOException {
        //
        // Temperature
        //
        long ut = getUncalibratedTemperature();

        // Calculate the temperature compensation factor
        long t1 = ((ut - ac6) * ac5) >> 15;
        long t2 = ((long) mc << 11) / (t1 + md);
        long b5 = t1 + t2;

        // Calculate the real temperature
        float celsius = (float) ((b5 + 8) >> 4) / 10;

        //
        // Pressure
        //
        long up = getUncompensatedPressure(mode);

        // Calculate the true pressure (see device data sheet)
        long b6 = b5 - 4000;
        long p1 = (b2 * (b6 * b6) >> 12) >> 11;
        long p2 = ac2 * b6 >> 11;
        long p3 = p1 + p2;
        long b3 = (((ac1 * 4 + p3) << mode.getOSS()) + 2) / 4;
        p1 = ac3 * b6 >> 13;
        p2 = (b1 * ((b6 * b6) >> 12)) >> 16;
        p3 = ((p1 + p2) + 2) >> 2;
        long b4 = (ac4 * (p3 + 32768)) >> 15;
        long b7 = (up - b3) * (50000 >> mode.getOSS());

        long pa = (b7 < 0x80000000) ? (b7 * 2) / b4 : (b7 / b4) * 2;
        p1 = (pa >> 8) * (pa >> 8);
        p1 = (p1 * 3038) >> 16;
        p2 = (-7357 * pa) >> 16;

        pa += ((p1 + p2 + 3791) >> 4);

        float hPa = (float) (pa) / 100;

        // Return the two values.
        float[] result = new float[2];
        result[0] = celsius;
        result[1] = hPa;
        return result;
    }

    /*
     * Reads the uncalibrated temperature from the device in the given mode.
     */
    private long getUncalibratedTemperature() throws IOException {
        // Write the read temperature command to the command register
        device.write(CONTROL_REGISTER_ADDRESS, READ_TEMPERATURE_COMMAND);
        try {
            // Temperature can always be read at ultra low power speeds.
            Thread.sleep(BMP180SamplingMode.ULTRA_LOW_POWER.getDelay());
        } catch (InterruptedException ex) {
        }

        byte[] data = new byte[2];
        int result = device.read(READ_TEMPERATURE_ADDRESS, data, 0, data.length);
        if (result < data.length) {
            throw new IOException("Error reading temperature. Expected 2 bytes but got " + result);
        }
        // Extract the uncompensated temperature
        return ((data[0] << 8) & 0xFF00) + (data[1] & 0xFF);
    }

    /*
     * Reads the uncompensated pressure from the device in the given mode.
     * TODO: Where are we sending the required mode?
     */
    private long getUncompensatedPressure(BMP180SamplingMode mode) throws IOException {
        // The pressure command is calculated by the enum
        // Write the read pressure command to the command register
        device.write(CONTROL_REGISTER_ADDRESS, READ_PRESSURE_COMMAND);
        try {
            Thread.sleep(mode.getDelay());
        } catch (InterruptedException ex) {
        }

        // Read the uncompensated pressure value
        byte[] data = new byte[3];
        int result = device.read(READ_PRESSURE_ADDRESS, data, 0, data.length);
        if (result < data.length) {
            throw new IOException("Error reading pressure.  Expected 3 bytes but got " + result);
        }

        // Extract the uncompensated pressure as a three byte word
        long word = ((data[0] << 16) & 0xFF0000) + ((data[1] << 8) & 0xFF00) + (data[2] & 0xFF);
        return (word >> (8 - mode.getOSS()));
    }

    /**
     * Performs a soft reset of the device.
     * 
     * The device will conduct the same sequence as a power-on reset.
     * 
     * @throws IOException
     *             if there was a communications problem with the device.
     */
    public synchronized void softReset() throws IOException {
        // Write the reset command to the command register. No response given.
        device.write(SOFT_RESET_ADDRESS, SOFT_RESET_COMMAND);
    }

    /**
     * Returns a String representation of this object.
     * 
     * The string shows the current status of the calibration data.
     * 
     * @return a debug string showing information for this device.
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(this.getClass().getSimpleName());
        buffer.append("(\n\t");
        buffer.append("AC1=").append(ac1).append("\n\t");
        buffer.append("AC2=").append(ac2).append("\n\t");
        buffer.append("AC3=").append(ac3).append("\n\t");
        buffer.append("AC4=").append(ac4).append("\n\t");
        buffer.append("AC5=").append(ac5).append("\n\t");
        buffer.append("AC6=").append(ac6).append("\n\t");
        buffer.append("B1=").append(b1).append("\n\t");
        buffer.append("B2=").append(b2).append("\n\t");
        buffer.append("MB=").append(mb).append("\n\t");
        buffer.append("MC=").append(mc).append("\n\t");
        buffer.append("MD=").append(md);
        buffer.append(")");
        return buffer.toString();
    }
}
