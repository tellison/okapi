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

public class BMP180Utils {

    /**
     * Converts a given temperature in degrees Celsius to degrees Fahrenheit.
     * 
     * @param degC
     *            the temperature in degrees Celsius.
     * @return the same temperate expressed in degrees Fahrenheit.
     */
    public static float celsiusToFahrenheit(float degC) {
        return (float) ((1.8 * degC) + 32.0);
    }

    /**
     * Returns the calculated altitude given the sea level and measured
     * pressures, and temperature at the measured point.
     * 
     * Given a reference mean sea level pressure (hPa) and measured pressure
     * (hPa) and temperature (C) calculates the altitude (m) using the
     * hypsometric formula. Only valid for altitudes within the Troposphere (up
     * to 11Km above seal level).
     * 
     * @param seaLevelPressure
     *            pressure at mean sea level, in hPa
     * @param measuredPressure
     *            pressure at altitude, in hPa
     * @param measuredTemperture
     *            temperature at the measured point, in deg. C.
     * @return altitude above sea level, in metres
     */
    public static float absoluteAltitude(float seaLevelPressure, float measuredPressure, float measuredTemperture) {
        double term = Math.pow(seaLevelPressure / measuredPressure, (1d / 5.257)) - 1d;
        double numerator = term * (measuredTemperture + 273.15);
        return (float) (numerator / 0.0065);
    }

    /**
     * Returns the calculated pressure at mean sea-level given a known altitude
     * above sea level and measured temperature and pressure at that known
     * altitude
     * 
     * Given a reference altitude (m) and measured pressure (hPa) an temperature
     * (C) at that altitude, calculates the pressure at sea level (hPa) using
     * the international barometric formula for a standard dry atmosphere.
     * 
     * @param heightASL
     *            the altitude above sea level, in meters
     * @param measuredPressure
     *            pressure at altitude, in hPa
     * @param measuredTemperature
     *            temperature at altitude, in de. C.
     * @return air pressure at sea level, in hPa
     */
    public static float seaLevelPressure(float heightASL, float measuredPressure, float measuredTemperature) {
        double heightFactor = 0.0065d * heightASL;
        double denom = heightFactor + measuredTemperature + 273.15;
        double term = 1.0d - (heightFactor / denom);
        double powTerm = Math.pow(term, -5.257d);
        return (float) (measuredPressure * powTerm);
    }
}
