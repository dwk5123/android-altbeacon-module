/**
 * 
 * Modifications Copyright 2015 David Kopczyk. All rights reserved.
 * Adapted from LiferayBeaconsModule.java authored by James Falkner
 * 
 * Copyright 2015 Liferay, Inc. All rights reserved.
 * http://www.liferay.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author David Kopczyk
 */

package com.drtech.altbeacon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Beacon.Builder;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.kroll.common.TiConfig;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.util.TiConvert;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.RemoteException;

@Kroll.module(name = "AltbeaconAndroidModule", id = "com.drtech.altbeacon")
public class AndroidAltbeaconModuleModule extends KrollModule implements BeaconConsumer {
	private static BeaconTransmitter beaconTransmitter;
	private static BeaconManager beaconManager;
	private boolean autoRange = true;
	private boolean runInService = false;

	// Standard Debugging variables
	private static final String LCAT = "AltbeaconModule";
	private static final boolean DBG = TiConfig.LOGD;

	private static double PROXIMITY_IMMEDIATE = 0.3;
	private static double PROXIMITY_NEAR = 3.0;
	private static double PROXIMITY_FAR = 10.0;

	@Kroll.onAppCreate
	public static void onAppCreate(TiApplication app) {
		Log.d(LCAT, "Create beaconmanager, setup in foregroundmode");
		beaconManager = BeaconManager.getInstanceForApplication(app);
		beaconManager.setBackgroundMode(false);
	}

	/**
	 * See if Bluetooth 4.0 and LE is available on device
	 *
	 * @return true if iBeacons can be used, false otherwise
	 */
	@Kroll.method
	public boolean checkAvailability() {
		try {
			return beaconManager.checkAvailability();
		} catch (Exception ex) {
			return false;
		}
	}

	/**
	 * Binds the activity to the Beacon Service
	 */
	@Kroll.method
	public void bindBeaconService() {
		Log.d(LCAT, "bindService");
		beaconManager.bind(this);
	}

	/**
	 * Unbinds the activity to the Beacon Service
	 */
	@Kroll.method
	public void unbindBeaconService() {
		Log.d(LCAT, "unbindService");
		beaconManager.unbind(this);
	}

	// methods to bind and unbind
	public Context getApplicationContext() {
		return super.getActivity().getApplicationContext();
	}

	public void unbindService(ServiceConnection serviceConnection) {
		Log.d(LCAT, "unbindService");
		super.getActivity().getApplicationContext().unbindService(serviceConnection);
	}

	public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
		Log.d(LCAT, "bindService");
		return super.getActivity().getApplicationContext().bindService(intent, serviceConnection, i);
	}

	/**
	 * Check the activity is bound to the Beacon Service
	 */
	@Kroll.method
	public boolean beaconServiceIsBound() {
		return beaconManager.isBound(this);
	}

	/**
	 * Throttles down iBeacon library when app placed in background (but you have to
	 * detect this yourself, this module does not know when apps are put in background).
	 *
	 * @param flag Whether to enable background mode or not.
	 */
	@Kroll.method
	public void setBackgroundMode(boolean flag) {
		Log.d(LCAT, "setBackgroundMode: " + flag);

		if (!checkAvailability()) {
			Log.d(LCAT, "Bluetooth LE not available or no permissions on this device");
			return;
		}
		beaconManager.setBackgroundMode(flag);
	}

	/**
	 * @param flag Set run in service, to know the beaconscanner must be unbinded on destroy of the rootactivity
	 */
	@Kroll.method
	public void setRunInService(boolean flag) {
		Log.d(LCAT, "setRunInService: " + flag);

		this.runInService = flag;
	}

	/**
	 * Check this module is running inside a service
	 */
	@Kroll.method
	public boolean isRunInService() {
		return this.runInService;
	}

	/**
	 * Turns on auto ranging. When auto ranging is on, upon entering a region, this
	 * module will automatically begin ranging for beacons within that region, and
	 * stop ranging for beacons when the region is exited. Note ranging requires more
	 * battery power so care should be taken with this setting.
	 */
	@Kroll.method
	public void enableAutoRanging() {
		setAutoRange(true);
	}

	/**
	 * Turns off auto ranging. See description of enableAutoRanging for more details.
	 *
	 * @see #enableAutoRanging()
	 */
	@Kroll.method
	public void disableAutoRanging() {
		setAutoRange(false);
	}

	/**
	 * Turns auto ranging on or off. See description of enableAutoRanging for more details.
	 *
	 * @param autoRange if true, turns on auto ranging. Otherwise, turns it off.
	 *
	 * @see #enableAutoRanging()
	 *
	 */
	@Kroll.method
	public void setAutoRange(boolean autoRange) {
		Log.d(LCAT, "setAutoRange: " + autoRange);
		this.autoRange = autoRange;

	}

	/**
	 * Set the scan periods for the bluetooth scanner.
	 *
	 * @param scanPeriods the scan periods.
	 */
	@Kroll.method
	public void setScanPeriods(Object scanPeriods) {
		Log.d(LCAT, "setScanPeriods: " + scanPeriods);

		HashMap < String, Object > dict = (HashMap < String, Object > ) scanPeriods;

		int foregroundScanPeriod = TiConvert.toInt(dict, "foregroundScanPeriod");
		int foregroundBetweenScanPeriod = TiConvert.toInt(dict, "foregroundBetweenScanPeriod");
		int backgroundScanPeriod = TiConvert.toInt(dict, "backgroundScanPeriod");
		int backgroundBetweenScanPeriod = TiConvert.toInt(dict, "backgroundBetweenScanPeriod");

		beaconManager.setForegroundScanPeriod(foregroundScanPeriod);
		beaconManager.setForegroundBetweenScanPeriod(foregroundBetweenScanPeriod);
		beaconManager.setBackgroundScanPeriod(backgroundScanPeriod);
		beaconManager.setBackgroundBetweenScanPeriod(backgroundBetweenScanPeriod);
	}

	/**
	 * Start monitoring a region.
	 * @param region the region to monitor, expected to be a property dictionary from javascript code.
	 */
	@Kroll.method
	public void startMonitoringForRegion(Object region) {
		Log.d(LCAT, "startMonitoringForRegion: " + region);

		if (!checkAvailability()) {
			Log.d(LCAT, "Bluetooth LE not available or no permissions on this device");
			return;
		}
		try {
			HashMap < String, Object > dict = (HashMap < String, Object > ) region;

			String identifier = TiConvert.toString(dict, "identifier");
			String uuid = TiConvert.toString(dict, "uuid").toLowerCase();
			Integer major = (dict.get("major") != null) ? TiConvert.toInt(dict, "major") : null;
			Integer minor = (dict.get("minor") != null) ? TiConvert.toInt(dict, "minor") : null;

			Identifier id1 = Identifier.parse(uuid);
			Identifier id2 = (major == null) ? null : Identifier.fromInt(major);
			Identifier id3 = (minor == null) ? null : Identifier.fromInt(minor);
			//Region r = new Region(identifier, uuid, major, minor);

			Region r = new Region(identifier, id1, id2, id3);

			Log.d(LCAT, "Beginning to monitor region " + r);
			beaconManager.startMonitoringBeaconsInRegion(r);
		} catch (RemoteException ex) {
			Log.e(LCAT, "Cannot start monitoring region " + TiConvert.toString(region, "identifier"), ex);
		}
	}


	/**
	 * Compatibility method for popular iOS FOSS iBeacon library.
	 *
	 * @see #startRangingForRegion(Object)
	 *
	 * @param region the region to range, expected to be a property dictionary from javascript code.
	 */
	@Kroll.method
	public void startRangingForBeacons(Object region) {
		startRangingForRegion(region);
	}

	/**
	 * Start ranging a region. You can only range regions into which you have entered.
	 *
	 * @param region the region to range, expected to be a property dictionary from javascript code.
	 */
	@Kroll.method
	public void startRangingForRegion(Object region) {
		Log.d(LCAT, "startRangingForRegion: " + region);

		if (!checkAvailability()) {
			Log.d(LCAT, "Bluetooth LE not available or no permissions on this device");
			return;
		}
		try {
			HashMap < String, Object > dict = (HashMap < String, Object > ) region;

			String identifier = dict.get("identifier").toString(); //TiConvert.toString(dict, "identifier");
			String uuid = dict.get("uuid").toString(); //TiConvert.toString(dict, "uuid").toLowerCase();
			Integer major = (dict.get("major") != null) ? TiConvert.toInt(dict, "major") : null;
			Integer minor = (dict.get("minor") != null) ? TiConvert.toInt(dict, "minor") : null;

			Identifier id1 = Identifier.parse(uuid);
			Identifier id2 = (major == null) ? null : Identifier.fromInt(major);
			Identifier id3 = (minor == null) ? null : Identifier.fromInt(minor);
			Region r = new Region(identifier, id1, id2, id3);

			Log.d(LCAT, "Beginning to monitor region " + r);
			beaconManager.startRangingBeaconsInRegion(r);
		} catch (RemoteException ex) {
			Log.e(LCAT, "Cannot start ranging region " + TiConvert.toString(region, "identifier"), ex);
		}
	}

	/**
	 * Stop monitoring everything.
	 */
	@Kroll.method
	public void stopMonitoringAllRegions() {
		Log.d(LCAT, "stopMonitoringAllRegions");
		for (Region r: beaconManager.getMonitoredRegions()) {
			try {
				beaconManager.stopMonitoringBeaconsInRegion(r);
				Log.d(LCAT, "Stopped monitoring region " + r);
			} catch (RemoteException ex) {
				Log.e(LCAT, "Cannot stop monitoring region " + r.getUniqueId(), ex);
			}
		}

	}

	/**
	 * Stop ranging for everything.
	 */
	@Kroll.method
	public void stopRangingForAllBeacons() {
		Log.d(LCAT, "stopRangingForAllBeacons");
		for (Region r: beaconManager.getRangedRegions()) {
			try {
				beaconManager.stopRangingBeaconsInRegion(r);
				Log.d(LCAT, "Stopped ranging region " + r);
			} catch (RemoteException ex) {
				Log.e(LCAT, "Cannot stop ranging region " + r.getUniqueId(), ex);
			}
		}
	}

	/**
	 * Remove BeaconParser described by layout from the manager
	 */
	@Kroll.method
	public void addBeaconLayout(String layout) {
		Log.d(LCAT, "Adding new BeaconLayout: " + layout);
		if (beaconManager.isBound(this)) {
			Log.d(LCAT, "Can't add a new BeaconLayout - service is currently bound");
			return;
		}
		if (layout != null) {
			beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(layout));
		}

	}

	/**
	 * Add BeaconParser described by layout to the manager
	 */
	@Kroll.method
	public boolean removeBeaconLayout(String layout) {
		Log.d(LCAT, "Removing BeaconLayout: " + layout);
		boolean result = false;
		if (beaconManager.isBound(this)) {
			Log.d(LCAT, "Can't remove from BeaconLayout - service is currently bound");
			return false;
		}
		if (layout != null) {
			result = beaconManager.getBeaconParsers().remove(new BeaconParser().setBeaconLayout(layout));
		}
		return result;
	}

	@Override
	public void onPause(Activity activity) {
		// This method is called when the root context is being suspended
		Log.d(LCAT, "[MODULE LIFECYCLE EVENT] pause, switch to backgroundmode");
		if (!beaconManager.isBound(this)) {
			beaconManager.setBackgroundMode(true);
		}

		super.onPause(activity);
	}

	@Override
	public void onResume(Activity activity) {
		// This method is called when the root context is being resumed
		Log.d(LCAT, "[MODULE LIFECYCLE EVENT] resume, switch to foregroundmode");
		if (!beaconManager.isBound(this)) {
			beaconManager.setBackgroundMode(false);
		}

		super.onResume(activity);
	}

	@Override
	public void onDestroy(Activity activity) {
		// This method is called when the root context is being resumed
		Log.d(LCAT, "[MODULE LIFECYCLE EVENT] onDestroy");
		if (!beaconManager.isBound(this) && !runInService) {
			Log.d(LCAT, "[MODULE LIFECYCLE EVENT] onDestroy, unbindservice because it's running in an activity");
			beaconManager.unbind(this);
		} else {
			Log.d(LCAT, "[MODULE LIFECYCLE EVENT] onDestroy, not unbinded altbeacon, because it's running in a service. Unbind it on taskremoved in the service.");
		}

		super.onDestroy(activity);
	}

	public void onBeaconServiceConnect() {
		KrollDict e = new KrollDict();
		e.put("message", "success");
		fireEvent("serviceBound", e);
		beaconManager.setMonitorNotifier(new MonitorNotifier() {

			public void didEnterRegion(Region region) {
				Log.d(LCAT, "Entered region: " + region);

				try {
					if (autoRange) {
						Log.d(LCAT, "Beginning to autoRange region " + region);
						beaconManager.startRangingBeaconsInRegion(region);
					}
					KrollDict e = new KrollDict();
					e.put("identifier", region.getUniqueId());
					fireEvent("enteredRegion", e);
				} catch (RemoteException ex) {
					Log.e(LCAT, "Cannot turn on ranging for region " + region.getUniqueId(), ex);
				}
			}

			public void didExitRegion(Region region) {
				Log.d(LCAT, "Exited region: " + region);

				try {
					beaconManager.stopRangingBeaconsInRegion(region);
					KrollDict e = new KrollDict();
					e.put("identifier", region.getUniqueId());
					fireEvent("exitedRegion", e);
				} catch (RemoteException ex) {
					Log.e(LCAT, "Cannot turn off ranging for region " + region.getUniqueId(), ex);
				}
			}

			public void didDetermineStateForRegion(int state, Region region) {
				if (state == INSIDE) {
					try {
						if (autoRange) {
							Log.d(LCAT, "Beginning to autoRange region " + region);
							beaconManager.startRangingBeaconsInRegion(region);
						}
						KrollDict e = new KrollDict();
						e.put("identifier", region.getUniqueId());
						e.put("regionState", "inside");
						fireEvent("determinedRegionState", e);
					} catch (RemoteException e) {
						Log.e(LCAT, "Cannot turn on ranging for region during didDetermineState" + region);
					}
				} else if (state == OUTSIDE) {
					try {
						beaconManager.stopRangingBeaconsInRegion(region);
						KrollDict e = new KrollDict();
						e.put("identifier", region.getUniqueId());
						e.put("regionState", "outside");
						fireEvent("determinedRegionState", e);
					} catch (RemoteException e) {
						Log.e(LCAT, "Cannot turn off ranging for region during didDetermineState" + region);
					}
				} else {
					Log.d(LCAT, "Unknown region state: " + state + " for region: " + region);
				}

			}
		});

		beaconManager.setRangeNotifier(new RangeNotifier() {
			public void didRangeBeaconsInRegion(Collection < Beacon > beacons, Region region) {
				for (Beacon beacon: beacons) {
					// identifier, uuid,major,minor,proximity,fromProximity,accuracy,rssi
					KrollDict e = new KrollDict();
					e.put("identifier", region.getUniqueId());
					e.put("uuid", beacon.getId1().toString());
					e.put("major", beacon.getId2().toString());
					e.put("minor", beacon.getId3().toString());
					e.put("proximity", getProximityName((int) beacon.getDistance()));
					e.put("accuracy", beacon.getDistance());
					e.put("rssi", beacon.getRssi());
					e.put("power", beacon.getTxPower());
					//e.put("data", beacon.getDataFields());
					fireEvent("beaconProximity", e);
				}
			}
		});
	}

	/**
	 * Sets the upper proximity ranges.
	 */@Kroll.method
	public void setProximityBounds(Object bounds) {
		HashMap < String, Object > dict = (HashMap < String, Object > ) bounds;

		PROXIMITY_FAR = TiConvert.toDouble(dict, "far");
		PROXIMITY_NEAR = TiConvert.toDouble(dict, "near");
		PROXIMITY_IMMEDIATE = TiConvert.toDouble(dict, "immediate");
	}

	public static String getProximityName(double d) {
		if (d <= PROXIMITY_IMMEDIATE) {
			return "immediate";
		} else if (d <= PROXIMITY_NEAR) {
			return "near";
		} else if (d <= PROXIMITY_FAR) {
			return "far";
		} else {
			return "unknown";
		}
	}

	/**
	 * Checks to see if Altbeacon advertisement/transmission is supported on the device
	 */@Kroll.method
	public boolean isTransmissionSupported() {
		return (BeaconTransmitter.checkTransmissionSupported(getApplicationContext()) == BeaconTransmitter.SUPPORTED);
	}

	/**
	 * Creates a new Altbeacon advertisement - COMPLETELY UNTESTED
	 * Code adapted from http://altbeacon.github.io/android-beacon-library/beacon-transmitter.html
	 */@Kroll.method
	public void startBeaconAdvertisement(Object beaconData) {
		HashMap < String, Object > dict = (HashMap < String, Object > ) beaconData;

		String id1 = TiConvert.toString(dict, "uuid");
		String id2 = TiConvert.toString(dict, "major");
		String id3 = TiConvert.toString(dict, "minor");
		int mfrID = TiConvert.toInt(dict, "mfrid");
		int txPower = TiConvert.toInt(dict, "txpower");

		Builder builder = new Beacon.Builder().setId1(id1).setId2(id2).setId3(id3).setManufacturer(mfrID).setTxPower(txPower);
		Object[] dataObj = (Object[]) dict.get("data");
		if (dataObj != null) {
			String[] dataStr = TiConvert.toStringArray(dataObj);
			ArrayList < Long > data = new ArrayList < Long > ();
			for (String s: dataStr) {
				data.add(Long.parseLong(s));
			}
			builder.setDataFields(data);
		}

		String layout = TiConvert.toString(dict, "layout");
		BeaconParser parser = new BeaconParser().setBeaconLayout(layout);
		beaconTransmitter = new BeaconTransmitter(getApplicationContext(), parser);

		beaconTransmitter.startAdvertising(builder.build());
	}

	/**
	 * Stops the advertised beacon(s) - COMPLETELY UNTESTED
	 */@Kroll.method
	public void stopBeaconAdvertisement() {
		beaconTransmitter.stopAdvertising();
	}
}