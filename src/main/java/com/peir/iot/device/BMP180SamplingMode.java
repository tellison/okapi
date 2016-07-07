package com.peir.iot.device;

/**
 * The set of modes available for sampling data from the BMP180 device.
 * 
 * The temperature value is always sampled at a rate of 4.5ms. The pressure
 * value can be over sampled by specifying one of the given modes defined by
 * this type. Different modes can be used to choose between power consumption,
 * speed of obtaining readings, and resolution.
 */
public enum BMP180SamplingMode {

	/** Single internal sample, 4.5ms (minimum) conversion time. */
	ULTRA_LOW_POWER(0, 5),

	/** Two internal samples, 7.5ms conversion time. */
	STANDARD(1, 8),

	/** Four internal samples, 13.5ms conversion time. */
	HIGH_RESOLUTION(2, 14),

	/** Eight internal samples, 25.5ms conversion time. */
	ULTRA_HIGH_RESOLUTION(3, 26);

	// Device over sampling setting value.
	private final int oss;

	// Minimum conversion time to allow, in ms, for this mode.
	private final int delay;

	BMP180SamplingMode(int oss, int delay) {
		this.oss = oss;
		this.delay = delay;
	}

	/* Returns the conversion delay (in ms) for this oversampling mode. */
	int getDelay() {
		return delay;
	}

	/* Returns this oversampling setting value. */
	int getOSS() {
		return oss;
	}
}