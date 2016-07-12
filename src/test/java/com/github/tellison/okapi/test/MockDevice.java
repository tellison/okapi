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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.github.tellison.okapi.device.BMP180Device;
import com.pi4j.io.i2c.I2CDevice;

/*
 * This is a simple emulator for the BMP180 device that can respond to a number
 * of requests and commands. Used in test suite to test the logic of the wrapper
 * classes and their APIs in teh absence of a real device, and a mechanism for
 * forcing some configuration options etc to test the validity of the logic on a
 * 'known standard' device.
 * 
 * The responses are taken from the Bosch BMP180 data sheet.
 */
class MockI2CDevice implements I2CDevice {

    // Temperature and pressure control register information.
    private static final int CALIB_REGISTER_ADDRESS = 0xAA;
    private static final int ID_REGISTER_ADDRESS = 0xD0;
    private static final int SOFT_RESET_ADDRESS = 0xE0;
    private static final int CONTROL_REGISTER_ADDRESS = 0xF4;
    private static final int READ_DATA_ADDRESS = 0xF6;

    // Command information.
    private static final byte SOFT_RESET_COMMAND = (byte) 0xB6;
    private static final byte READ_PRESSURE_COMMAND = (byte) 0x34;
    private static final byte READ_TEMPERATURE_COMMAND = (byte) 0x2E;

    // Options for our emulated device's internal state
    enum DeviceStates {
        READING_TEMP, READING_PRESSURE
    }

    // What our emulation device is current doing.
    DeviceStates state;

    // A simple output to show mock device internals.
    DebugLogger logger = new DebugLogger();

    @Override
    public void write(byte b) throws IOException {
        write(new byte[] { b }, 0, 1);
    }

    @Override
    public void write(byte[] buffer, int offset, int size) throws IOException {
        // TODO Auto-generated method stub
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public void write(int address, byte b) throws IOException {
        write(address, new byte[] { b }, 0, 1);
    }

    @Override
    public void write(int address, byte[] buffer, int offset, int size) throws IOException {
        switch (address) {
            // Poking the soft reset
            case SOFT_RESET_ADDRESS:
                logger.log("Received reset command");
                if ((size < 1) || (buffer.length - size < offset) || (buffer[offset] != SOFT_RESET_COMMAND)) {
                    throw new IOException("Attempt to reset with invalid arguments.");
                }
                // Reset does nothing here
                break;

            // Poking the temp/pressure command
            case CONTROL_REGISTER_ADDRESS:
                if ((size != 1) || (buffer.length - size < offset)) {
                    throw new IOException("Attempt to send control with invalid arguments.");
                }
                int control = buffer[offset];
                // Ignore the sampling mode requested
                switch (control & 0b00111111) {
                    case READ_TEMPERATURE_COMMAND:
                        logger.log("Received read temperature command");
                        state = DeviceStates.READING_TEMP;
                        break;
                    case READ_PRESSURE_COMMAND:
                        logger.log("Received read pressure command");
                        state = DeviceStates.READING_PRESSURE;
                        break;
                    default:
                        throw new RuntimeException("Received unknown command on mock device");
                }
                break;

            default:
                throw new RuntimeException("Attempt to write to unknown address in mock device.");
        }
    }

    @Override
    public int read() throws IOException {
        throw new IOException("Cannot read byte from unspecified address on mock device");
    }

    @Override
    public int read(byte[] buffer, int offset, int size) throws IOException {
        throw new IOException("Cannot read into buffer from unspecified address on mock device");
    }

    @Override
    public int read(int address) throws IOException {
        byte[] buffer = new byte[4];
        int bytesRead = read(address, buffer, 0, buffer.length);
        switch (bytesRead) {
            case 0:
                throw new IOException("End of file reading address in mock device");
            case 1:
                return buffer[0];
            case 2:
                return ((buffer[0] << 8) & 0xFF00) + (buffer[1] & 0xFF);
            case 3:
                return ((buffer[0] << 16) & 0xFF0000) + ((buffer[1] << 8) & 0xFF00) + (buffer[2] & 0xFF);
            case 4:
                return ((buffer[0] << 24) & 0xFF0000) + ((buffer[1] << 16) & 0xFF0000) + ((buffer[2] << 8) & 0xFF00)
                        + (buffer[3] & 0xFF);
            default:
                throw new IOException("Invalid return code from read, attempt to read beyond end of buffer");
        }
    }

    @Override
    public int read(int address, byte[] buffer, int offset, int size) throws IOException {
        if (buffer.length - size < offset) {
            throw new RuntimeException("Read request buffer overflow");
        }
        switch (address) {
            // Request for calibration data response
            case CALIB_REGISTER_ADDRESS:
                byte[] calib = getCalibData();
                int len = Math.min(calib.length, size);
                System.arraycopy(calib, 0, buffer, offset, len);
                return len;

            // Request for device ID - a fixed value
            case ID_REGISTER_ADDRESS:
                logger.log("Returning device ID = " + BMP180Device.DEVICE_ID);
                buffer[offset] = 0;
                buffer[offset + 1] = BMP180Device.DEVICE_ID;
                return 2;

            // Request for a temperature/ pressure reading
            // Answer values for the worked example in the data sheet so we cand
            // check the answer
            case READ_DATA_ADDRESS:
                logger.log("Received a read data request");

                switch (state) {
                    case READING_TEMP:
                        logger.log("Returning a temperature reading");
                        if (size < 2) {
                            throw new RuntimeException("Temperature request buffer too small");
                        }
                        // Delay to mimic the device response time, really this
                        // would be
                        // between the control and the read, but hey.
                        try {
                            Thread.sleep(4, 500000);
                        } catch (InterruptedException ex) {
                        }
                        // Answer 27898
                        buffer[offset] = (byte) 0x6C;
                        buffer[offset + 1] = (byte) 0xFA;
                        return 2;
                    case READING_PRESSURE:
                        logger.log("Returning a pressure reading");
                        if (size < 3) {
                            throw new RuntimeException("Pressure request buffer too small");
                        }
                        // Delay to mimic the device response time, really this
                        // would be
                        // between the control and the read, but hey.
                        try {
                            Thread.sleep(4, 500000);
                        } catch (InterruptedException ex) {
                        }
                        // Answer 6103808 (23843 << 8)
                        buffer[offset] = (byte) 0x5D;
                        buffer[offset + 1] = (byte) 0x23;
                        buffer[offset + 2] = (byte) 0x00;
                        return 3;
                }

            default:
                throw new RuntimeException("Attempt to read from unknown location in mock device.");
        }
    }

    @Override
    public int read(byte[] writeBuffer, int writeOffset, int writeSize, byte[] readBuffer, int readOffset, int readSize)
            throws IOException {
        // TODO Auto-generated method stub
        throw new RuntimeException("Not yet implemented");
    }

    /*
     * Returns some mock calibration data (see device datasheet).
     */
    private byte[] getCalibData() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(22);

        try (DataOutputStream calibDataStream = new DataOutputStream(bos);) {
            calibDataStream.writeShort(408); // AC1
            calibDataStream.writeShort(-72); // AC2
            calibDataStream.writeShort(-14383); // AC3
            calibDataStream.writeShort(32741); // AC4
            calibDataStream.writeShort(32757); // AC5
            calibDataStream.writeShort(23153); // AC6
            calibDataStream.writeShort(6190); // B1
            calibDataStream.writeShort(4); // B2
            calibDataStream.writeShort(-32768); // MB
            calibDataStream.writeShort(-8711); // MC
            calibDataStream.writeShort(2868); // MD
            calibDataStream.close();

            return bos.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
