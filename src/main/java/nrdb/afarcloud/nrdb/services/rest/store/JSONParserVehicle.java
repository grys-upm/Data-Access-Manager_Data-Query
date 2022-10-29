/* Copyright 2018-2021 Universidad Politécnica de Madrid (UPM).
 *
 * Authors:
 *    Sara Lana Serrano
 *    Mario San Emeterio de la Parte
 *    Vicente Hernández Díaz
 *    José-Fernan Martínez Ortega
 *
 * This software is distributed under a dual-license scheme:
 *
 * - For academic uses: Licensed under GNU Affero General Public License as
 *                      published by the Free Software Foundation, either
 *                      version 3 of the License, or (at your option) any
 *                      later version.
 *
 * - For any other use: Licensed under the Apache License, Version 2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * You can get a copy of the license terms in licenses/LICENSE.
 *
 */
/**
 * 
 */
package afarcloud.nrdb.services.rest.store;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB.ConsistencyLevel;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;

import com.fasterxml.jackson.core.JsonToken;

import afarcloud.nrdb.config.Constants;
import afarcloud.nrdb.util.GlobalParameters;
import ch.hsr.geohash.GeoHash;

/**
 * Vehicle
 *
 */
public class JSONParserVehicle extends JSONParser {

	/***********    influxDB output    ************* */
	// tags  => add geohash
	private static final List<String> INFLUX_TAGS; //geohash
	//fields => add geohash  
	private static final List<String> INFLUX_FIELDS; // special processing: geohash

	
	private static final String JSON_MEASUREMENT = Constants.IDB_VEHICLE_MEASUREMENT;
	
	/****************************************************/
	private static final String JSON_VEHICLE_ID = Constants.IDB_VEHICLE_ID;
	private static final String JSON_EPOCH = "lastUpdate";
	private static final String JSON_SEQUENCENUMBER = Constants.IDB_SEQUENCE_NUMBER;
	
	private static final String JSON_LOCATION = "location";	
	private static final String JSON_LONGITUDE = Constants.IDB_LOCATION_LONGITUDE;
	private static final String JSON_LATITUDE = Constants.IDB_LOCATION_LATITUDE;
	private static final String JSON_ALTITUDE = Constants.IDB_LOCATION_ALTITUDE;
	private static final String INFLUX_GEOHASH = Constants.IDB_LOCATION_GEOHASH;
	
	private static final String JSON_ORIENTATION = "orientation"; 
	private static final String JSON_ROLL = Constants.IDB_VEHICLE_ORIENTATION_ROLL;
	private static final String JSON_PITCH = Constants.IDB_VEHICLE_ORIENTATION_PITCH;
	private static final String JSON_YAW = Constants.IDB_VEHICLE_ORIENTATION_YAW;
	
	
	private static final String JSON_BATTERY = "battery"; 
	private static final String JSON_BATTERYCAPACITY = Constants.IDB_VEHICLE_BATTERY_CAPACITY; 
	private static final String JSON_BATTERYPERCENTAGE = Constants.IDB_VEHICLE_BATTERY_PERCENTAGE; 
	
	private static final String JSON_LINEARSPEED = Constants.IDB_VEHICLE_LINEAR_SPEED; 
	
		
	static {
		INFLUX_TAGS = new ArrayList<String> (Arrays.asList(new String[] {JSON_VEHICLE_ID}));
		//numeric fields must be float
		INFLUX_FIELDS = new ArrayList<String> (Arrays.asList(new String[] {JSON_LONGITUDE, JSON_LATITUDE, JSON_ALTITUDE, INFLUX_GEOHASH, JSON_ROLL, JSON_PITCH, JSON_YAW, JSON_BATTERYCAPACITY, JSON_BATTERYPERCENTAGE, JSON_LINEARSPEED, JSON_SEQUENCENUMBER }));
	}

	//properties to be extracted from JSON 
	private static final List<String> FIRSTLEVEL_PROPERTIES;
	private static final List<String> LOCATION_PROPERTIES; 
	private static final List<String> ORIENTATION_PROPERTIES;
	private static final List<String> BATTERY_PROPERTIES;

    static {
    	FIRSTLEVEL_PROPERTIES = new ArrayList<String> (Arrays.asList(new String[] { JSON_VEHICLE_ID, JSON_SEQUENCENUMBER, JSON_LINEARSPEED, JSON_EPOCH }));
    	
    	LOCATION_PROPERTIES = new ArrayList<String> (Arrays.asList(new String[] { JSON_LONGITUDE, JSON_LATITUDE, JSON_ALTITUDE}));
    	ORIENTATION_PROPERTIES = new ArrayList<String> (Arrays.asList(new String[] { JSON_ROLL, JSON_PITCH, JSON_YAW }));
    	BATTERY_PROPERTIES = new ArrayList<String> (Arrays.asList(new String[] { JSON_BATTERYCAPACITY, JSON_BATTERYPERCENTAGE}));
    }
	
	//first level properties
    private HashMap<String, Object> hFirstLevel = null;
    	
	private HashMap<String, Object> hLocation = null; // convert to geohash
	private HashMap<String, Object> hOrientation = null;	
	private HashMap<String, Object> hBattery = null;

	
	// Scenario
	private String sDBName;
	/**
	 * 
	 * !OJO!!! diferente al resto, es necesario pasarle el escenario en el constructor
	 */
	public JSONParserVehicle(String sScenario) {
		sDBName = sScenario;
		hFirstLevel = new HashMap<String, Object>();		
	}

	@Override
	protected BatchPoints _parse() throws Exception{
		//init
		if (hFirstLevel!= null && hFirstLevel.size()>0) hFirstLevel.clear();
		if (hLocation!= null && hLocation.size()>0) hLocation.clear();
		if (hOrientation!= null && hOrientation.size()>0) hOrientation.clear();
		if (hBattery!= null && hBattery.size()>0) hBattery.clear();
		JsonToken jsonToken;
		
		//root element must be a object		
		while(!oParser.isClosed()){
		    jsonToken = oParser.nextToken();

		    //System.out.println("jsonToken = " + jsonToken);
		    
		    if(JsonToken.FIELD_NAME.equals(jsonToken)){
		        String fieldName = oParser.getCurrentName();
	        	//move to next token:: JsonToken.START_ARRAY || JsonToken.START_OBJECT || JsonToken.VALUE_XXX
	            oParser.nextToken();

		        //System.out.println("\t\t" + fieldName);
	            if(FIRSTLEVEL_PROPERTIES.contains(fieldName)){
	            	hFirstLevel.put(fieldName, JSONParserUtils.parseValue(oParser));		        	
		        }else if (JSON_LOCATION.equalsIgnoreCase(fieldName)) {
		        	this.hLocation = JSONParserUtils.parseObject(oParser, LOCATION_PROPERTIES);
		        }else if (JSON_ORIENTATION.equalsIgnoreCase(fieldName)) {
		        	this.hOrientation = JSONParserUtils.parseObject(oParser, ORIENTATION_PROPERTIES);
		        }else if (JSON_BATTERY.equalsIgnoreCase(fieldName)){		        	
		        	this.hBattery = JSONParserUtils.parseObject(oParser, BATTERY_PROPERTIES);
		        	
		        }else{
		        	if (oParser.currentToken().equals(JsonToken.START_ARRAY) || 
		        		oParser.currentToken().equals(JsonToken.START_OBJECT)){
		        		oParser.skipChildren();
		        	}
		        }
		        
		        
		    }
		}
		oParser.close();	
		
		return _toInfluxDBBatchPoints();
	}

	
	/** serialization to BatchPoints **/
	private BatchPoints _toInfluxDBBatchPoints() {

		if(hLocation!=null && !hLocation.isEmpty()){
			hFirstLevel.putAll(hLocation);
			if( hFirstLevel.containsKey(JSON_LATITUDE) && hFirstLevel.containsKey(JSON_LONGITUDE)) {
				String sGeo = GeoHash.geoHashStringWithCharacterPrecision((double)hFirstLevel.get(JSON_LATITUDE), (double)hFirstLevel.get(JSON_LONGITUDE) , GEOHASH_PRECISION);
				hFirstLevel.put(INFLUX_GEOHASH, sGeo);
			}
		}
		
		if(hOrientation!=null && !hOrientation.isEmpty()){
			hFirstLevel.putAll(hOrientation);
		}
		if(hBattery!=null && !hBattery.isEmpty()){
			hFirstLevel.putAll(hBattery);
		}

		
		BatchPoints batchPoints = BatchPoints
				.database(sDBName.toUpperCase())
		        .consistency(ConsistencyLevel.ALL)
		        .build();
		
	
		if (hFirstLevel!=null && !hFirstLevel.isEmpty()){
			//tags :: [,<tag-key>=<tag-value>...]
			LinkedHashMap<String, String> hTags = new LinkedHashMap<String, String>();									

			for(String sTag: INFLUX_TAGS){
				if(hFirstLevel.containsKey(sTag)){
					hTags.put(sTag, String.valueOf(hFirstLevel.get(sTag)));
				}
			}

			//fields:: <field-key>=<field-value>[,<field2-key>=<field2-value>...]
			LinkedHashMap<String, Object> hFields = new LinkedHashMap <String, Object>();

			//fields => if numeric must be float
			for(String sField: INFLUX_FIELDS){
				if(hFirstLevel.containsKey(sField)){
					//hFields.put(sField,  hFirstLevel.get(sField));
					hFields.put(sField, JSONParserUtils.ObjectToFloat(hFirstLevel.get(sField)) );
				}
			}

			Long lEpoch = Long.valueOf(String.valueOf(hFirstLevel.get(JSON_EPOCH)));
			TimeUnit time = TimeUnit.SECONDS;
			lEpoch = time.toMicros(lEpoch);

			batchPoints.point(
					Point.measurement(GlobalParameters.getInstance().getParameter(GlobalParameters.PREFIX_VEHICLE) + JSON_MEASUREMENT )
					.tag(hTags)
					.fields(hFields)
					.time(lEpoch, TimeUnit.MICROSECONDS)
					.build()
					);
		}

	
		
		return batchPoints;
	}
	
}

