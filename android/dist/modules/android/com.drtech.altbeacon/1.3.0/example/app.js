//
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

var win = Ti.UI.createWindow({
	backgroundColor:'white'
});

var button1 = Ti.UI.createButton({
	title: 'Start Monitoring',
	top: 10,
	height: 50
});
button1.addEventListener('click', startMonitoring);
var button2 = Ti.UI.createButton({
	title: 'Start Ranging',
	top: 70,
	height: 50
});
button2.addEventListener('click', startRanging);
var label = Ti.UI.createLabel();
label.addEventListener('click', function() {
	label.text = '';
});
win.add(button1);
win.add(button2);
win.add(label);
win.open();

var TiBeacons = null;
if (Ti.Platform.name == "android") {
    TiBeacons = require('com.drtech.altbeacon');
    TiBeacons.addBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24");
    TiBeacons.bindBeaconService();
    label.text = "module is => " + TiBeacons + "and checkAvailability says: " + TiBeacons.checkAvailability();
} else {
    label.text = "com.drtech.altbeacon not supported on " + Ti.Platform.name;
}

function startMonitoring() {
	if (TiBeacons != null) {
		TiBeacons.addEventListener("enteredRegion", enteredRegionCallback);
		TiBeacons.addEventListener("exitedRegion", exitedRegionCallback);
		TiBeacons.addEventListener("beaconProximity", rangingCallback);
	    
	    TiBeacons.startMonitoringForRegion({
		  identifier: 'Region by UUID only',
		  uuid: 'E2C56DB5-DFFB-48D2-B060-D0F5A71096E0'
		});
	}
	button1.removeEventListener('click', startMonitoring);
	button1.title = 'Stop Monitoring';
	button1.addEventListener('click', stopMonitoring);
}

function startRanging() {
	if (TiBeacons != null) {
		TiBeacons.addEventListener("enteredRegion", enteredRegionCallback);
		TiBeacons.addEventListener("exitedRegion", exitedRegionCallback);
		TiBeacons.addEventListener("beaconProximity", rangingCallback);
	    
	    TiBeacons.startRangingForBeacons({
		  identifier: 'Region by UUID only',
		  uuid: 'E2C56DB5-DFFB-48D2-B060-D0F5A71096E0'
		});
	}
	button2.removeEventListener('click', startRanging);
	button2.title = 'Stop Ranging';
	button2.addEventListener('click', stopRanging);
}

function stopMonitoring() {
	TiBeacons.stopMonitoringAllRegions();
	if (TiBeacons != null) {
		TiBeacons.removeEventListener("enteredRegion", enteredRegionCallback);
		TiBeacons.removeEventListener("exitedRegion", exitedRegionCallback);
		TiBeacons.removeEventListener("beaconProximity", rangingCallback);
	}
	button1.removeEventListener('click', startMonitoring);
	button1.title = 'Start Monitoring';
	button1.addEventListener('click', startMonitoring);
}

function stopRanging() {
	TiBeacons.stopRangingForAllBeacons();
	if (TiBeacons != null) {
		TiBeacons.removeEventListener("enteredRegion", enteredRegionCallback);
		TiBeacons.removeEventListener("exitedRegion", exitedRegionCallback);
		TiBeacons.removeEventListener("beaconProximity", rangingCallback);
	}
	button2.removeEventListener('click', stopRanging);
	button2.title = 'Start Ranging';
	button2.addEventListener('click', startRanging);
}

function enteredRegionCallback(e) {
    console.log("identifer: " + e.identifier);
    label.text = "Entered region: " + e.identifier;
}

function exitedRegionCallback(e) {
    console.log("identifer: " + e.identifier);
    label.text = "Exited region: " + e.identifier;
}

function rangingCallback(e) {
    console.log("identifer: " + e.identifier);
    console.log("uuid: " + e.uuid);
    console.log("major: " + e.major);
    console.log("minor: " + e.minor);
    console.log("proximity: " + e.proximity);
    console.log("accuracy: " + e.accuracy);
    console.log("rssi: " + e.rssi);
    console.log("power: " + e.power);
    label.text = 	"Proximity:\n" +
    				"Identifier: " + e.identifier + "\n" + 
    				"UUID: " + e.uuid + "\n" + 
    				"Major: " + e.major + "\n" + 
    				"Minor: " + e.minor + "\n" + 
    				"Proximity: " + e.proximity + "\n" + 
    				"Distance: " + e.accuracy + "\n" + 
    				"RSSI: " + e.rssi + "\n" + 
    				"Power: " + e.power;
}

function monitorCallback(e) {
	
}
