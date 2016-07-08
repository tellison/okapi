package com.peir.iot.test;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.peir.iot.device.BMP180Device;
import com.pi4j.io.i2c.I2CDevice;

class MockI2CDevice implements I2CDevice {

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

    // Options for our emulated device's internal state
    enum DeviceStates {
        READING_TEMP, READING_PRESSURE
    }

    // What our emulation device is current doing.
    DeviceStates state;

    @Override
    public void write(byte b) throws IOException {
        write(new byte[] { b }, 0, 1);
    }

    @Override
    public void write(byte[] buffer, int offset, int size) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void write(int address, byte b) throws IOException {
        write(address, new byte[] { b }, 0, 1);
    }

    @Override
    public void write(int address, byte[] buffer, int offset, int size) throws IOException {
        switch (address) {
            case SOFT_RESET_ADDRESS:
                if ((size < 1) || (buffer.length - size < offset) || (buffer[offset] != SOFT_RESET_COMMAND)) {
                    throw new IOException("Attempt to reset with invalid arguments.");
                }
                // Reset does nothing here
                break;

            default:
                throw new RuntimeException("Attempt to write to unknown location in mock device.");
        }
    }

    @Override
    public int read() throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int read(byte[] buffer, int offset, int size) throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int read(int address) throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int read(int address, byte[] buffer, int offset, int size) throws IOException {
        switch (address) {
            // Request for calibration data response
            case CALIB_REGISTER_ADDRESS:
                byte[] calib = getCalibData();
                int len = Math.min(calib.length, size);
                System.arraycopy(calib, 0, buffer, offset, len);
                return len;
            // Request for device ID - a fixed value
            case ID_REGISTER_ADDRESS:
                return BMP180Device.DEVICE_ID;
            default:
                throw new RuntimeException("Attempt to read from unknown location in mock device.");
        }
    }

    @Override
    public int read(byte[] writeBuffer, int writeOffset, int writeSize, byte[] readBuffer, int readOffset, int readSize)
            throws IOException {
        // TODO Auto-generated method stub
        return 0;
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
