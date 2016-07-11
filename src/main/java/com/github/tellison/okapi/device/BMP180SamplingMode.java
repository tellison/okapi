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

package com.github.tellison.okapi.device;

/**
 * The set of modes available for sampling data from the BMP180 device.
 * 
 * The temperature value is always sampled at a rate of 4.5ms. The pressure
 * value can be over sampled by specifying one of the given modes defined by
 * this type. Different modes can be used to choose between power consumption,
 * speed of obtaining readings, and resolution.
 */
public enum BMP180SamplingMode {

    /** Single internal sample, 4.5ms (the minimum) conversion time. */
    ULTRA_LOW_POWER(0, 4, 500000),

    /** Two internal samples, 7.5ms conversion time. */
    STANDARD(1, 7, 500000),

    /** Four internal samples, 13.5ms conversion time. */
    HIGH_RESOLUTION(2, 13, 500000),

    /** Eight internal samples, 25.5ms conversion time. */
    ULTRA_HIGH_RESOLUTION(3, 25, 500000);

    // Device over sampling setting value.
    private final int oss;

    // Minimum conversion time to allow for this mode (ms and ns)
    private final int delayMillis;
    private final int delayNanos;

    BMP180SamplingMode(int oss, int delayMillis, int delayNanos) {
        this.oss = oss;
        this.delayMillis = delayMillis;
        this.delayNanos = delayNanos;
    }

    /* Returns the millisecond conversion delay for this over sampling mode. */
    int getDelayMillis() {
        return delayMillis;
    }

    /* Returns the nanosecond conversion delay for this over sampling mode. */
    int getDelayNanos() {
        return delayNanos;
    }

    /* Returns this over sampling setting value. */
    int getOSS() {
        return oss;
    }
}
