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
        multiAttributeTile(name:"Khai", type:"generic", width:6, height:4) {
            tileAttribute("device.khai_value", key: "PRIMARY_CONTROL") {
    			attributeState("khai_value", label:'${currentValue}', unit:"", /*icon:"st.Weather.weather13",*/ backgroundColors:[
                    [value: 24, color: "#1c71ff"],
                    [value: 49, color: "#5c93ee"],
                    [value: 74, color: "#ff4040"],
                    [value: 100, color: "#d62d20"]
                ])
  			}
            tileAttribute("device.data_time", key: "SECONDARY_CONTROL") {
           		attributeState("data_time", label:'${currentValue}')
            }
		}
        valueTile("PM10", "device.pm10_value", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
        	state "pm10", label:'${currentValue}\n ¥ìg/m©ø', unit:"¥ìg/m©ø",backgroundColors:[
                    [value: 0, color: "#90d2a7"],
                    [value: 50, color: "#44b621"],
                    [value: 100, color: "#f1d801"],
                    [value: 200, color: "#bc2323"]
                ]
        }
        valueTile("PM25", "device.pm25_value", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
        	state "pm25", label:'${currentValue}\n ¥ìg/m©ø', unit:"¥ìg/m©ø",backgroundColors:[
                    [value: 0, color: "#90d2a7"],
                    [value: 50, color: "#44b621"],
                    [value: 100, color: "#f1d801"],
                    [value: 200, color: "#bc2323"]
                ]
        }
        valueTile("o3", "device.o3_value", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
            state "o3", label:'${currentValue}\n ppm', unit:"ppm",backgroundColors:[
                    [value: 0, color: "#90d2a7"],
                    [value: 150, color: "#44b621"],
                    [value: 300, color: "#f1d801"],
                    [value: 450, color: "#bc2323"]
                ]
        }
        standardTile("refresh", "device.refresh", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
            state "refresh", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        valueTile("refreshes", "device.refreshes", inactiveLable: false, decoration: "flat", width: 4, height: 1) {
			state "refreshes", label:'Refreshes Remaining Today: ${currentValue}'
		}
        standardTile("spacerlastUpdatedLeft", "spacerTile", decoration: "flat", width: 1, height: 1) {
 		}
        standardTile("spacerlastUpdatedRight", "spacerTile", decoration: "flat", width: 1, height: 1) {
 		}
        main "khai_value"
        details(["khai_value","pm10_value","pm25_value","o3_value","refresh","spacerlastUpdatedLeft", "refreshes","spacerlastUpdatedRight"])
	}
}
private getAPIKey() {
    return "8OAJRHK5%2BTn75SiXMtbgUvkVSWOfBvxYSmFZ6J9NMZsBNbsWwvLH7dPFg04%2FTqSiDYh00l181QE91qw4e1dNmw%3D%3D"
}
def parse(String description) {
	log.debug "Parsing '${description}'"
}
def refresh() { 
	poll()
}
// handle commands
def poll() {
    def station_name = "´Ü´ëµ¿"
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
                        sendEvent(name: "pm10_value", value: resp.data.list[0].pm10Value as Integer, unit: "¥ìg/m©ø", isStateChange: true)
                    }
                    else
                        sendEvent(name: "pm10_value", value: -1, unit: "", isStateChange: true)


                    if( resp.data.list[0].pm25Value )
                    { 
                        log.debug "PM25: ${resp.data.list[0].pm25Value}"
                        sendEvent(name: "pm25_value", value: resp.data.list[0].pm25Value as Integer, unit: "¥ìg/m©ø", isStateChange: true)
                    }
                    else
                        sendEvent(name: "pm25_value", value: -1, unit: "", isStateChange: true)

                     
                    if( resp.data.list[0].o3Value )
                    {
                        log.debug "Ozone: ${resp.data.list[0].o3Value}"
                        sendEvent(name: "o3_value", value: resp.data.list[0].o3Value as Double, unit: "ppm", isStateChange: true)
                    }
                    else
                        sendEvent(name: "o3_value", value: -1, unit: "", isStateChange: true)

                    log.debug "Khai value: ${resp.data[0].khaiValue}"
                    sendEvent(name: "khai_value", value: resp.data.list[0].khaiValue as Integer, unit: "", isStateChange: true)

                    def khai_grade = resp.data.list[0].khaiGrade
                    def khai_text 
                    if (khai_grade == 0) khai_text="GREAT"
                    else if (allpollu == 1) khai_text="GOOD"
                    else if (allpollu == 2) khai_text="FAIR"
                    else if (allpollu == 3) khai_text="POOR"
                    else if (allpollu == 4) khai_text="VERY POOR"

                    sendEvent(name:"data_time", value:khai_text + "(${resp.data.parm.stationName})" + " - Last Updated: " + resp.data.list[0].dataTime, isStateChange: true)
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
