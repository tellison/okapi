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
	 * Returns the altitude given the measured and sea level pressures.
	 * 
	 * Given a reference sea level pressure (hPa) and measured pressure (hPa)
	 * calculates the altitude (m) using the international barometric formula.
	 * 
	 * @param seaLevelPressure
	 *            pressure at sea level, in hPa
	 * @param measuredPressure
	 *            pressure at altitude, in hPa
	 * @return altitude above sea level, in metres
	 */
	public static float absoluteAltitude(float seaLevelPressure, float measuredPressure) {
		double exponent = 1.0d / 5.255d;
		double temp = 1.0d - Math.pow(measuredPressure / seaLevelPressure, exponent);
		return (float) (44330.0d * temp);
	}
	
	/**
	 * Returns the pressure at sea-level given the known altitude and measured pressure.
	 * 
	 * Given a reference altitude (m) and measured pressure (hPa) at that altitude,
	 * calculates the pressure at sea level (hPa) using the international barometric formula.
	 * 
	 * @param absoluteAltitude
	 *            the altitude above sea level, in meters
	 * @param measuredPressure
	 *            pressure at altitude, in hPa
	 * @return air pressure at sea level, in hPa
	 */
	public static float sealevelPressure(float absoluteAltitude, float measuredPressure) {
		double term = 1.0d - (absoluteAltitude / 44330.0d);
		double denominator = Math.pow(term, 5.255d);
		return (float) (measuredPressure / denominator);
	}
}
