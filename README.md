# Altbeacon Titanium Module

## Description

This module provides an interface to the [Altbeacon](https://github.com/AltBeacon/android-beacon-library "Altbeacon Library") library for Android.  It was modified from the [Liferay module](https://github.com/jamesfalkner/liferay-android-beacons) that used the RadiusNetwork iBeacon library for Android.

## Accessing the android-altbeacon-module Module

To access this module from JavaScript, you would do the following:
```
    var Altbeacon = require("com.drtech.altbeacon");
```

## Reference

### Altbeacon.checkAvailability()

Returns true if the device has BLE capability/Bluetooth is turned on.

### Altbeacon.bindBeaconService()

Binds the Android application context to the BeaconService provided by the library.  If a custom BeaconParser is required, make sure the parser is added BEFORE calling this method.

### Altbeacon.unbindBeaconService()

Unbinds the BeaconService binded with ```Altbeacon.bindBeaconService()```. See description of bindBeaconService for more details.

### Altbeacon.beaconServiceIsBound()

Checks the beacon service is binded to the current application context. See description of bindBeaconService for more details.

### Altbeacon.setBackgroundMode(boolean flag)

Throttles down Altbeacon library when app placed in background (but you have to detect this yourself, this module does not know when apps are put in background).

### Altbeacon.enableAutoRanging()

Turns on auto ranging. When auto ranging is on, upon entering a region, this module will automatically begin ranging for beacons within that region, and stop ranging for beacons when the region is exited. Note ranging requires more battery power so care should be taken with this setting.
	 
### Altbeacon.disableAutoRanging()

Turns off auto ranging. See description of enableAutoRanging for more details.

### Altbeacon.setAutoRange(boolean autoRange)

Turns auto ranging on or off. See description of enableAutoRanging for more details.

### Altbeacon.setScanPeriods(scanPeriods)

Set the scan periods for the bluetooth scanner.


Structure of scanPeriods:
```
{
	foregroundScanPeriod: MILLISECONDS,
	foregroundBetweenScanPeriod: MILLISECONDS,
	backgroundScanPeriod: MILLISECONDS,
	backgroundBetweenScanPeriod: MILLISECONDS
}
```


If this method is not called, the following are the defaults:
```
{
	foregroundScanPeriod: 1200,
	foregroundBetweenScanPeriod: 2300,
	backgroundScanPeriod: 10000,
	backgroundBetweenScanPeriod: 60000
}
```

### Altbeacon.startMonitoringForRegion(region)

Start monitoring a region. Will trigger events 'enteredRegion', 'exitedRegion', and 'determinedRegionState'.


Structure of a region:
```
{
	identifier: 'Some arbitrary ID',
	uuid: '00000000-0000-0000-0000-000000000000',
	major: INT or null,
	minor: INT or null
}
```

### Altbeacon.startRangingForBeacons(region)

Compatibility method for popular iOS FOSS iBeacon library. See startRangingForRegion for further details.

### Altbeacon.startRangingForRegion(region)

Start ranging a region. You can only range regions into which you have entered.  Will fire the event 'beaconProximity' for each scan.  See startMonitoringForRegion for the structure of a region.

### Altbeacon.stopMonitoringAllRegions()

Stop monitoring everything.

### Altbeacon.stopRangingForAllBeacons()

Stop ranging everything.

### Altbeacon.addBeaconLayout(String layout)

Add BeaconParser described by layout to the manager. This method MUST be called before calling bindBeaconService or it will fail.

### Altbeacon.removeBeaconLayout(String layout)

Remove BeaconParser described by layout from the manager. This method MUST be called before calling bindBeaconService or it will fail.

### Altbeacon.setProximityBounds(bounds)

Allows the user to customize the proximity bounds.  These bounds are used for approximate distances from a beacon.


Data structure:
```
{
	far: METERS,
	near: METERS,
	immediate: METERS
}
```

### Altbeacon.startBeaconAdvertisement(parameters) 

Starts broadcasting as a Beacon.  The parameters required are described below.  This method is completely untested.


Data structure:
```
{
	uuid: 00000000-0000-0000-000000000000,
	major: INT,
	minor: INT,
	mfrid: INT,
	txpower: INT,
	data: [INT, INT, ...] or NULL,
	layout: BeaconParserLayout
}
```

### Altbeacon.stopBeaconAdvertisement() 

Stops Beacon advertising - as with startBeaconAdvertisement, this method is also completely untested.

## Usage

```
var TiBeacons = null;
if (Ti.Platform.name == "android") {
    TiBeacons = require('com.drtech.altbeacon');
    TiBeacons.addBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24");
    TiBeacons.bindBeaconService();
    label.text = "module is => " + TiBeacons + "and checkAvailability says: " + TiBeacons.checkAvailability();
} else {
    label.text = "com.drtech.altbeacon not supported on " + Ti.Platform.name;
}


```

## Author

David Kopczyk
dwk5123@gmail.com

## License

Licensed under the Apache 2.0 license.  Modifications Copyright 2015 David Kopczyk.
