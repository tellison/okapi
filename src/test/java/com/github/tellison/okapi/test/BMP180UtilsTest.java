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

import org.junit.Test;

import com.github.tellison.okapi.device.BMP180Utils;

public class BMP180UtilsTest {

    @Test
    public void testCelsiusToFahrenheit() {
        // A few well known values
        assertEquals(32.0f, BMP180Utils.celsiusToFahrenheit(0.0f), 0.001f);
        assertEquals(212.0f, BMP180Utils.celsiusToFahrenheit(100), 0.001f);
        assertEquals(-40f, BMP180Utils.celsiusToFahrenheit(-40.0f), 0.001f);
        assertEquals(98.6f, BMP180Utils.celsiusToFahrenheit(37.0f), 0.001f);
    }

    @Test
    public void testAbsoluteAltitude() {
        // regular values
        assertEquals(3_255.52, BMP180Utils.absoluteAltitude(1_011f, 700, 19), 0.01f);
        assertEquals(303.66, BMP180Utils.absoluteAltitude(1067f, 1029, 12), 0.01f);
        // negative altitude
        assertEquals(-268.24, BMP180Utils.absoluteAltitude(990f, 1021f, 25), 0.01f);
        // same pressure
        assertEquals(0f, BMP180Utils.absoluteAltitude(778, 778, 19), 0.01f);
    }

    @Test
    public void testSeaLevelPressure() {
        // regular values
        assertEquals(987.64f, BMP180Utils.seaLevelPressure(3_000f, 700f, 15), 0.1f);
        assertEquals(1_047.51f, BMP180Utils.seaLevelPressure(400f, 1_000f, 20), 0.1f);
        // same height
        assertEquals(717.9f, BMP180Utils.seaLevelPressure(0f, 717.9f, 10), 0.01f);
    }
}
