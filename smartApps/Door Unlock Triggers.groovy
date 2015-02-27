/**
 *	Door Unlock Triggers
 *
 *	Copyright 2015 Gary D
 *
 *	Licensed under the Apache License, Version 2.0 WITH EXCEPTIONS; you may not use this file except
 *	in compliance with the License AND Exceptions. You may obtain a copy of the License at:
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *	on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *	for the specific language governing permissions and limitations under the License.
 *
 *
 *	Exceptions are:
 *		1.	This code may NOT be used without freely distributing the code freely and without limitation, 
 *			in source form.	 The distribution may be met with a link to source code,
 *		2.	This code may NOT be used, directly or indirectly, for the purpose of any type of monetary
 *			gain.	This code may not be used in a larger entity which is being sold, leased, or 
 *			anything other than freely given.
 *		3.	To clarify 1 and 2 above, if you use this code, it must for your own personal use, or be a 
 *			free project, and available to anyone with "no strings attached."  (You may require a free
 *			registration on a free website or portal in order to distribute the modifications.)
 *		4.	The above listed exceptions to this code do not apply to "SmartThings, Inc."  SmartThings,
 *			is granted a license to use this code under the terms of the Apache License (version 2) with
 *			no further exception.
 *
 */
definition(
	name: "Door Unlock Triggers",
	namespace: "garyd9",
	author: "Gary D",
	description: "Triggers switches, door controls, etc based on lock events",
	category: "Convenience",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
	iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {

	section("When a door unlocks...") 
	{
		input "lock1", "capability.lock", title: "Door Lock?", required: true
	}
	
	section("...and any of these codes (or methods) opened the door...") 
	{
		input "unlockCode", "enum", title: "User Code", multiple: true, metadata:[values:["1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27","28","29","30","Manual","Other"]]
	}
	section("...Then...")
	{
		input "doors", "capability.doorControl", title: "Close these doors", multiple: true, required: false
		input "switches", "capability.switch", title: "Turn on these switches", multiple: true, required: false
		input name: "sendPush", type: "bool", title: "Push notification to mobile devices?", defaultValue: false
	}
}

def installed() 
{
	initialize()
}

def updated() 
{
	unsubscribe()
	initialize()
}

def initialize() 
{
	subscribe(lock1, "lock.unlocked", lockHandler)
}


def performActions(evt)
{
	doors.eachWithIndex {s, i ->
		def state = s.latestValue("door")
		if (state == "open")
		{
			log.debug "$s is currently $state.	Closing it."
			s.close();
		}
		else
		{
			log.debug "$s is currently $state.	(can't close it)"
		}
	}

	switches.eachWithIndex {s, i ->
		def state = s.latestValue("switch")
		if (state == "off")
		{
			log.debug "$s is currently $state.	Turning it on."
			s.on()
		}
		else
		{
			log.debug "$s is already $state."
		}
	}
	if (sendPush)
	{
		sendPushMessage(evt.descriptionText)
	}
}

def lockHandler(evt) 
{
	log.debug "lockHandler:"
	log.debug "	   description:	 $evt.descriptionText"
	log.debug "	   unlockCode:	$unlockCode"
	
	if (evt.data != null)
	{
		def evData = parseJson(evt.data)
		if (unlockCode.contains((evData.usedCode).toString()))
		{
			performActions(evt)
		}
	}
	else // evdata is null
	{
		if (evt.descriptionText.contains("manually"))
		{
			if (unlockCode.contains("Manual"))
			{	// event describes a manual event, and unlockCode is looking for "manual"...
				performActions(evt)
			}
		}
		else if (unlockCode.contains("Other"))
		{	// event description doesn't contain "manually" and the evt.data is null... so it must be "other"
			performActions(evt)
		}
	}
}

