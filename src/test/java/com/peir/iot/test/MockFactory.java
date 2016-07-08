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

import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactoryProvider;

import junit.framework.TestCase;

/**
 * An I2C factory that returns a mock device bus.
 *
 */
class MockFactory extends TestCase implements I2CFactoryProvider {

    @Override
    public I2CBus getBus(int busNumber) throws IOException {
        assertEquals(I2CBus.BUS_1, busNumber);
        return new MockI2CBus();
    }
}
