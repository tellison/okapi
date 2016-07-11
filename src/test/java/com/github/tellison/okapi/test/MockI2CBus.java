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

import java.io.IOException;

import com.github.tellison.okapi.device.BMP180Device;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;

/*
 * A mock I2C bus that hosts a mock BMP180 device.
 *
 */
class MockI2CBus implements I2CBus {
    @Override
    public I2CDevice getDevice(int address) throws IOException {
        if (BMP180Device.DEVICE_I2C_ADDRESS != address) {
            throw new IOException("Invalid address requested for mock device on I2C bus.");
        }
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
