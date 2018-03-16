/**
 *  AirKorea DTH
 *
 *  Copyright 2018 SeungCheol Lee
 *  Version 0.0.1 3/15/18
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

metadata {
	definition (name: "AirKorea", namespace: "slasehrLee", author: "SeungCheol Lee") {
		capability "Polling"
        capability "Refresh"
        capability "Sensor"
     
     	attribute "khai_value", "number"
        attribute "pm10_value", "number"
        attribute "pm25_value", "number"
        attribute "o3_value", "number"
        attribute "no2_value", "number"
        attribute "so2_value", "number"
        attribute "co_value", "number"
        attribute "data_time", "String"
	}
    preferences {
        input "station_name", "text", title: "station name", description: "The station name of your region", required: true
        input "refreshRate", "enum", title: "Data refresh rate", defaultValue: 0, options:[0: "Never" ,10: "Every 10 Minutes", 30: "Every 1/2 hour", 60 : "Every Hour", 240 :"Every 4 hours",
        	360: "Every 6 hours", 720: "Every 12 hours", 1440: "Once a day"], displayDuringSetup: true
    }
	simulator {
		// TODO: define status and reply messages here
	}
	tiles (scale: 2){   
        multiAttributeTile(name:"khai_value", type:"generic", width:6, height:4) {
            tileAttribute("device.khai_value", key: "PRIMARY_CONTROL") {
    			attributeState("default", label:'${currentValue}', unit:"", /*icon:"st.Weather.weather13",*/ backgroundColors:[
                    [value: 0, color: "#90d2a7"],
                    [value: 50, color: "#44b621"],
                    [value: 100, color: "#f1d801"],
                    [value: 150, color: "#bc2323"],
                    [value: 200, color: "#d62d20"]                    
                ])
  			}
            tileAttribute("device.data_time", key: "SECONDARY_CONTROL") {
           		attributeState("default", label:'${currentValue}')
            }
		}
        valueTile("pm10_value", "device.pm10_value", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
        	state "default", label:'${currentValue}μg/m³', unit:"μg/m³", backgroundColors:[
                    [value: 0, color: "#90d2a7"],
                    [value: 50, color: "#44b621"],
                    [value: 100, color: "#f1d801"],
                    [value: 200, color: "#bc2323"]
                ]
        }
        valueTile("pm25_value", "device.pm25_value", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
        	state "default", label:'${currentValue}μg/m³', unit:"μg/m³", backgroundColors:[
                    [value: 0, color: "#90d2a7"],
                    [value: 50, color: "#44b621"],
                    [value: 100, color: "#f1d801"],
                    [value: 200, color: "#bc2323"]
                ]
        }
        valueTile("o3_value", "device.o3_value", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
            state "default", label:'${currentValue}ppm', unit:"ppm", backgroundColors:[
                    [value: 0, color: "#90d2a7"],
                    [value: 0.005, color: "#44b621"],
                    [value: 0.01, color: "#f1d801"],
                    [value: 0.02, color: "#bc2323"]
                ]
        }
        valueTile("no2_value", "device.so2_value", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
            state "default", label:'${currentValue}ppm', unit:"ppm", backgroundColors:[
                    [value: 0, color: "#90d2a7"],
                    [value: 0.005, color: "#44b621"],
                    [value: 0.01, color: "#f1d801"],
                    [value: 0.02, color: "#bc2323"]
                ]
        }
        valueTile("so2_value", "device.so2_value", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
            state "default", label:'${currentValue}ppm', unit:"ppm", backgroundColors:[
                    [value: 0, color: "#90d2a7"],
                    [value: 0.005, color: "#44b621"],
                    [value: 0.01, color: "#f1d801"],
                    [value: 0.02, color: "#bc2323"]
                ]
        }
        valueTile("co_value", "device.o3_value", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
            state "default", label:'${currentValue}ppm', unit:"ppm", backgroundColors:[
                    [value: 0, color: "#90d2a7"],
                    [value: 0.005, color: "#44b621"],
                    [value: 0.01, color: "#f1d801"],
                    [value: 0.02, color: "#bc2323"]
                ]
        }
        standardTile("refresh", "device.refresh", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
            state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        main "khai_value"
        details(["khai_value","pm10_value","pm25_value","o3_value","no2_value","so2_value","co_value","refresh"])
	}
}
private getAPIKey() {
    return ""
}
def parse(String description) {
	log.debug "Parsing '${description}'"
}
def refresh() { 
	poll()
}
// handle commands
def poll() {
    if (station_name){
        def refreshTime =  refreshRate ? (refreshRate as int) * 60 : 0
        if (refreshTime > 0) {
            runIn (refreshTime, poll)
            log.debug "Data will repoll every ${refreshRate} minutes"   
        }
        else log.debug "Data will never repoll" 
        def accessKey = getAPIKey()
        def params = [
    	    uri: "http://openapi.airkorea.or.kr/openapi/services/rest/ArpltnInforInqireSvc/getMsrstnAcctoRltmMesureDnsty?stationName=" + /*urlEncode(station_name)*/ station_name + 
                  "&dataTerm=month&pageNo=1&numOfRows=1&ServiceKey=${accessKey}&_returnType=json",
        	contentType: 'application/json'
    	]
        try {
            httpGet(params) {resp ->
                resp.headers.each {
                    log.debug "${it.name} : ${it.value}"
                }
                // get the contentType of the response
                log.debug "response contentType: ${resp.contentType}"
                // get the status code of the response
                log.debug "response status code: ${resp.status}"
                if (resp.status == 200){
                    // get the data from the response body
                    //log.debug "response data: ${resp.data}"
              
                    if( resp.data.list[0].pm10Value )
                    {
                        log.debug "PM10: ${resp.data.list[0].pm10Value}"
                        sendEvent(name: "pm10_value", value: resp.data.list[0].pm10Value as Integer, unit: "μg/m³", isStateChange: true)
                    }
                    else
                        sendEvent(name: "pm10_value", value: -1, unit: "", isStateChange: true)


                    if( resp.data.list[0].pm25Value )
                    { 
                        log.debug "PM25: ${resp.data.list[0].pm25Value}"
                        sendEvent(name: "pm25_value", value: resp.data.list[0].pm25Value as Integer, unit: "μg/m³", isStateChange: true)
                    }
                    else
                        sendEvent(name: "pm25_value", value: -1, unit: "", isStateChange: true)

                     
                    if( resp.data.list[0].o3Value )
                    {
                        log.debug "Ozone: ${resp.data.list[0].o3Value}"
                        sendEvent(name: "o3_value", value: sprintf("%.3f", resp.data.list[0].o3Value as Double), unit: "ppm", isStateChange: true)
                    }
                    else
                        sendEvent(name: "o3_value", value: -1, unit: "", isStateChange: true)

                    def khai = resp.data.list[0].khaiValue as Integer
                    log.debug "Khai value: ${khai}"
                    sendEvent(name: "khai_value", value: khai, unit: "", isStateChange: true)

                    def khai_text 
                    if (!khai) khai_text="에러"
                    if (khai > 200) khai_text="매우 나쁨"
                    else if (khai > 150 ) khai_text="나쁨"
                    else if (khai > 100) khai_text="보통"
                    else if (khai > 50) khai_text="좋음"
                    else if (khai >= 0) khai_text="매우 좋음"
                    else khai_text="에러"

                    sendEvent(name:"data_time", value: khai_text + "(" + resp.data.parm.stationName + ") - Last Updated: " + resp.data.list[0].dataTime, isStateChange: true)
          		}
            	else if (resp.status==429) log.debug "You have exceeded the maximum number of refreshes today"	
                else if (resp.status==500) log.debug "Internal server error"
            }
        } catch (e) {
            log.error "error: $e"
        }
	}
    else log.debug "The station name is missing from the device settings"
}
