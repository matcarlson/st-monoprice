/**
 *  ZOOZ 4-in-1 sensor
 *
 *  Copyright 2016 Simon Capper
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
 
/*
Device handler for ZOOZ ZSE40 4-in-1 sensor.
 
To add the zooz to a smart things network do the following:

1) Add this device handler to your "My Device Handlers" in the developer IDE.

2) Make sure you save *and* publish the new device in "My Device Handlers" in the IDE.

3) Find the "Z Wave Button" on the zooz. It is shown on the instructions that come with the Zoos,
it is a small hole on one edge of the Zooz next to a row of slightly larger holes, find something
that you can press it with, pin or something

4) Put the batteries into the zooz, close the case.

5) Get really close to the Smart Things hub, the zooz is a secure device and needs to talk directly 
to the hub, you can move it later once it is pared.

6) In the smart things app on you phone "add a new thing" and select Connect New Device, your phone 
will say it is looking for a new thing.

7) Press the z wave button twice quickly, the zooz will flash 6 times, about 30 seconds later
your phone will say it found a "ZOOZ ZSE40 4-in-1 Sensor"

NOTE: If something goes wrong with the secure pairing the device might show up as a "z-wave door/window sensor".
If you are not close to the hub when you pair it can cause it to show up like this.
You can fix this by deleting the device and re-adding or by going to the IDE and changing the device type 
to "ZOOZ ZSE40 4-in-1 sensor".

The device "raw description" in the IDE should start with this:
zw:Ss type:0701 
If it starts with "zw:S" (i.e. no little s) then the secure join has failed, you should
delete the device and try again (if you care about secure mode).

8) Name your device and select your configuration options. (You can come back and change these later at any time)

Biggest thing to think of when choosing the settings is battery life, you can set the settings in
a way that will drain the battery quickly if you are not careful.

- Motion Sensor Idle Time (minutes) (default 3 mins): this tells the zooz how long it has to detect no
motion before sending a "no motion detected" event back to the ST hub. Min 1 min, max 255 mins.
If you set this too low the zooz will send lots of motion/no-motion events and this drains the battery.

- Motion Sensor Sensitivity (1 high - 7 low) (default 4): sets how sensitive the zooz is, 1 is most sensitive
7 is least sensitive. Experimaent to see what works for you.

- Temperature alert level (1/10th °C) (default 1): (0.1°C - 5°C) The zooz only sends temperature info to the hub when the 
temp is below this value, it send a message every 3 mins (most of the time) when the temp is below this value.
(Note: the zooz does update the temp periodically even when below this value, see wakeup interval below)

- Humidity alert level (default 50): The zooz only sends humidity info to the hub when the 
humidity is above this value, it send a message up to one every 3 mins (most of the time) when the humidity is above 
this value.
This drains the battery so set this above what you'd normally expect to see and when you want to 
trigger a smart app quickly.
(Note: the zooz does update the humidity periodically even when below this value, see wakeup interval below)

- Illumination alert level (default 0 - disabled): The zooz only sends illumination info to the hub when the 
illumination is above this value, it send a message up to every 3 mins when the illumination is 
above this value.
This drains the battery so set this above what you'd normally expect to see and when you want to 
trigger a smart app quickly or if you don't need this just disable this by setting it to zero.
(Note: the zooz does update the humidity periodically even when below this value, see wakeup interval below)

- Wake Interval (minutes) (default: 30 mins). The zooz is a sleepy device because it runs off a battery, this
setting tells it how often to wake up to see if you've changed its configuration. 
When the zooz wakes up this driver reads the Temperature, Humidy and Illumination value from the Zooz, 
so unless sensors are at alert levels this interval is also controls how often the sensor values get updated.
If you make a configuration change to the Zooz, you have to wait up to the "Wake Interval" before it takes 
effect. (you can speed this up by pressing the z wave button on the zooz once, this forces the Zooz to 
wake up early).
The lower you set this value the faster the battery will drain.

- Led mode (1, 2 or 3) (default: 3)
Option 1 - LED off
Option 2 - LED is has a different color depending on the temperature, "breadth mode"
Option 3 - LED will blink when motion is detected or the temp goes below the alert level

Option 1 is best for the battery life, option 2 is the worst.

*/

metadata {
    definition (name: "Monoprice 15902 4-in-1 Motion Sensor", namespace: "matcarlson", author: "Mat Carlson") {
        capability "Battery"
        capability "Motion Sensor"
        capability "Tamper Alert"
        capability "Temperature Measurement"
        capability "Configuration"
        capability "Relative Humidity Measurement"
        capability "Illuminance Measurement"
        
        attribute "sensorlevels", "string"
        
        fingerprint deviceId: "2101", inClusters: "0x5E,0x98,0x72,0x86,0x5A,0x85,0x59,0x73,0x80,0x71,0x31,0x70,0x84,0x7A"
// zw:Ss type:0701 mfr:0109 prod:2021 model:2101 ver:5.01 zwv:4.05 lib:03 cc: sec:86 role:06 ff:8C07 ui:8C07
    }

    tiles(scale:2) {
        multiAttributeTile(name:"motion", type: "generic", width: 6, height: 4){
            tileAttribute ("device.motion", key: "PRIMARY_CONTROL") {
                attributeState "active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#53a7c0" 
                attributeState "inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff" 
            }
            tileAttribute ("device.sensorlevels", key: "SECONDARY_CONTROL") {
                attributeState "sensorlevels", label: '${currentValue}'
            }
        }
        standardTile("tamper", "device.tamper", decoration: "flat", width: 2, height: 2)
        {
            state("clear", label:'Secure', defaultState: true, icon:"", backgroundColor: "#ffffff"  )
            state("detected", label:'Tamper', icon:"st.alarm.alarm.alarm", backgroundColor: "#ffffff" )
        }
        valueTile("battery", "device.battery", decoration: "flat", width: 2, height: 2) 
        {
            state("battery", label:'${currentValue}% battery')
        }

        main "motion"
        details(["motion", "tamper", "battery"])
    }
    
    preferences {
        input "pirTimeout", "number", title: "Motion Sensor Idle Time (minutes)", defaultValue: 3,
              description: "Inactivity time before reporting no motion",
              required: false, displayDuringSetup: true, range: "1..255"
        input "pirSensitivity", "number", title: "Motion Sensor Sensitivity (1 high - 7 low)", defaultValue: 3,
              description: "1 is most sensitive, 7 is least sensitive",
              required: false, displayDuringSetup: true, range: "1..7"
        input "tempAlert", "number", title: "Temperature alert level (1/10th °C)", defaultValue: 1,
              description: "Temperature below which to send updates",
              required: false, displayDuringSetup: true, range: "1..50"
        input "humidityAlert", "number", title: "Humidity alert level", defaultValue: 50,
              description: "Humidity level above which to send updates",
              required: false, displayDuringSetup: true, range: "1..50"
        input "illumAlert", "number", title: "Illumination alert level", defaultValue: 0,
              description: "Illumination level above which to send updates, 0 to disable",
              required: false, displayDuringSetup: true, range: "0..50"
        input "ledMode", "number", title: "LED Mode", defaultValue: 3,
              description: "1) On when motion/alerted, 2 - Always on (drains battery), 3 - Blink LED",
              required: false, displayDuringSetup: true, range: "1..3"
        input "wakeInterval", "number", title: "Wake Interval (minutes)", defaultValue: 30,
              description: "Interval (in minutes) for polling configuration and sensor values, shorter interval reduces battery life.",
              required: false, displayDuringSetup: true, range: "10..10080"
    }
}

// parse events into attributes
def parse(String description) {
    def result = null
    // supported classes
    // 0x20 - BasicSet, reports when motion detected (not used, alarm provides same info)
    // 0x70 - configuration V1
    // 0x72 - manufacturer specific V2
    // 0x80 - Battery V1
    // 0x84 - Wakeup V2
    // 0x85 - association V2
    // 0x86 - version V2
    // 0x5E - zwave plus info V2 (not supported by ST)
    // 0x98 - Security V1
    // 0x5A - Device Reset Locally V1
    // 0x59 - Association Grp Info V1
    // 0x73 - Powerlevel (RF power) V1
    // 0x71 - Notification V4
    // 0x31 - Sensor Multilevel V7
    // 0x7A - Firmware Update Md V2
    log.debug "raw: ${description}"
    state.sec = 0
    if (description.contains("command: 5E02")) {
        // for some reason this causes a crash in parse(), so skip it
        return null
    }
    def cmd = zwave.parse(description, 
                        [ 0x86:1, 0x70:1, 0x72:1, 0x80:1, 0x84:2, 0x85:1, 0x86:2, 
                          0x98:1, 0x5a:1, 0x59:1, 0x73:1, 0x71:3, 0x31:5, 0x7a:1 ])
    if (cmd) {
        result = zwaveEvent(cmd)
        if (result) {
            log.debug "Parsed command: ${result} raw: ${description}"
        } else {
            log.debug "Unhandled command: ${description}"
        }
    } else {
        log.debug "Unparsed command: ${description}"
    }
    return result
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
    log.debug "NotificationReport ${cmd}"
    // zooz reports 2 "burgler" event types, motion (8) and tamper (3)
    if (cmd.notificationType == 7) {
        switch (cmd.event) {
            case 0:
                if (cmd.eventParameter == [8]) {
                    return createEvent(name:"motion", value: "inactive")
                } else if (cmd.eventParameter == [3]) {
                    return createEvent(name:"tamper", value: "clear")
                }
                break
            case 3:
                return createEvent(name:"tamper", value: "detected")
                break
            case 8:
                return createEvent(name:"motion", value: "active")
                break
        }
    }
    return null
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
    log.debug "SensorMultilevelReport ${cmd}"
    def evt = []
    switch (cmd.sensorType) {
        case 1: //temp
            if (cmd.scale == 0) {
                // Celcius, convert to Fahrenheit
                def temp = ((cmd.scaledSensorValue * 9) / 5) + 32
                state.temp = Math.round(temp * 100)/100
            } else {
                state.temp = cmd.scaledSensorValue
            }
            evt += createEvent([name: "temperature", value: state.temp, unit: "fahrenheit"]) 
            break
        case 3: // light
            state.illuminance = cmd.scaledSensorValue 
            evt += createEvent([name: "illuminance", value: state.illuminance]) 
            break
        case 5: // humidity
            state.humidity = cmd.scaledSensorValue
            evt += createEvent([name: "humidity", value: state.humidity]) 
            break
    }
    evt += createEvent([name: "sensorlevels", value: "Temp:${state.temp}°F   Humidity:${state.humidity}%   Light:${state.illuminance}"])
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
    def secCmd = cmd.encapsulatedCommand([ 0x86:1, 0x70:1, 0x72:1, 0x80:1, 0x84:2, 0x85:1, 0x86:1,
                                           0x98:1, 0x5a:1, 0x59:1, 0x73:1, 0x71:3, 0x31:5, 0x7a:1 ])
    if (secCmd) {
    	state.sec = 1
        return zwaveEvent(secCmd)
    } else {
        log.debug "SecurityMessageEncapsulation cannot decode ${cmd}"
    }
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
    return createEvent(name:"battery", value: cmd.batteryLevel)
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
    log.debug "WakeUpNotification v2 ${cmd}"

    def cmds = readSensors()
    if (state.configRequired) {
        // send pending configure commands
        cmds += configCmds()
        state.configRequired = false
    }
    cmds += zwave.wakeUpV2.wakeUpNoMoreInformation().format()
    cmds = delayBetween(cmds, 600)
    return [response(cmds)]
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
    // Handles all Z-Wave commands we aren't interested in
    return null
}

def updated() {
	log.debug "updated"
    // called when the device is updated or perferences changed
    state.configRequired = true
}

def installed() {
	log.debug "installed"
    // called when the device is installed
    state.configRequired = true
}

def configure() {
    state.sec = 1
    delayBetween( configCmds() + readSensors(), 600)
}

def readSensors() {
    [
        secure(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:3)), // light
        secure(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:5)), // humidity
        secure(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:1)), // temp
    ]
}

def getWakeIntervalPref() {
	(wakeInterval < 10 || wakeInterval > 10080) ? 1800 : (wakeInterval * 60).toInteger()
}

def getLedModePref() {
	(ledMode < 1 || ledMode > 3) ? 3 : ledMode.toInteger()
}

def getIllumAlertPref() {
	(illumAlert >= 5 && illumAlert <= 50) ? illumAlert.toInteger() : 0
}

def getHumidityAlertPref() {
	(humidityAlert < 1 || humidityAlert > 50) ? 50 : humidityAlert.toInteger()
}

def getTempAlertPref() {
	(tempAlert < 1 || tempAlert > 50 ) ? 1 : tempAlert.toInteger()
}

def getPirTimeoutPref() {
	(pirTimeout < 1 || pirTimeout > 255 ) ? 3 : pirTimeout.toInteger()
}

def getPirSensitivityPref() {
	(pirSensitivity < 1 || pirSensitivity > 7) ? 3 : pirSensitivity.toInteger()
}

def configCmds() {
    log.debug "configure, tempAlertPref:${tempAlertPref} humidityAlertPref:${humidityAlertPref}"
    log.debug "configure, illumAlertPref:${illumAlertPref} pirTimeoutPref:${pirTimeoutPref}"
    log.debug "configure, pirSensitivityPref:${pirSensitivityPref} ledModePref:${ledModePref}" 
    log.debug "configure, wakeIntervalPref:${wakeIntervalPref}"
    def cmds = [
        secure(zwave.batteryV1.batteryGet()),
        secure(zwave.associationV2.associationSet(groupingIdentifier:1, nodeId:zwaveHubNodeId)),

        secure(zwave.configurationV1.configurationSet(scaledConfigurationValue: tempAlertPref, parameterNumber: 2)),
        secure(zwave.configurationV1.configurationSet(scaledConfigurationValue: humidityAlertPref, parameterNumber: 3)),
        secure(zwave.configurationV1.configurationSet(scaledConfigurationValue: illumAlertPref, parameterNumber: 4)),
        secure(zwave.configurationV1.configurationSet(scaledConfigurationValue: pirTimeoutPref, parameterNumber: 5)),
        secure(zwave.configurationV1.configurationSet(scaledConfigurationValue: pirSensitivityPref, parameterNumber: 6)),
        secure(zwave.configurationV1.configurationSet(scaledConfigurationValue: ledModePref, parameterNumber: 7)),

        secure(zwave.wakeUpV2.wakeUpIntervalSet(seconds: wakeIntervalPref, nodeid:zwaveHubNodeId)),
        
/*
        secure(zwave.configurationV1.configurationGet(parameterNumber: 1)), // Temp scale C(0) or F(1)
        secure(zwave.configurationV1.configurationGet(parameterNumber: 2)), // Temp trigger 0.1 - 5 (1-50)
        secure(zwave.configurationV1.configurationGet(parameterNumber: 3)), // Hiumidity trigger 1%-50%
        secure(zwave.configurationV1.configurationGet(parameterNumber: 4)), // Light trigger, None(0), 5%-50%
        secure(zwave.configurationV1.configurationGet(parameterNumber: 5)), // Motion Timeout 1-255 Mins
        secure(zwave.configurationV1.configurationGet(parameterNumber: 6)), // Motion Sensitivity 1-7 (1 is most sensitive)
        secure(zwave.configurationV1.configurationGet(parameterNumber: 7)), // LED Mode (1,2 or 3). 
                                                                            // - 1 is On when temp/motion are triggered
                                                                            // - 2 is fancy temp color scheme that wastes battery 
                                                                            // - 3 is brief flash when temp/motion trigger fires
*/
        
    ]
    return cmds
}

def secure(cmd) {
	if (state.sec) {
    	return zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
    } else {
    	return cmd.format()
    }
}
