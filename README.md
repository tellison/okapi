# A Java API to the Bosch BMP180 Digital pressure sensor.

This project provides a Java API to the Bosch BMP180 Digital pressure sensor.  The sensor is available
on an inter-integrated circuit (I2C) bus.

Key information about control of the device is found in the [device data sheet]
(http://www.datasheetspdf.com/PDF/BMP180/770150/1), which was the source for much of this material.

The project comes with tests that run on an included emulator class, and utilities for operations
typically undertaken with the results from this device.

## Example Usage

The simple use of this code is as follows:

```java
 try (BMP180Device device = new BMP180Device()) {
     float[] values = device.getTemperatureAndPressure();
     float temperature = values[0];
     float pressure = values[1];
 } catch (IOException e) {
   // Error
 }
```

## Problems and Issues

Any problems please raise a [Git issue] (https://github.com/tellison/okapi/issues).


## Release Notes

 - 0.1.1 - Bug fix on retrieving device id.
 - 0.1.0 - Initial version.
