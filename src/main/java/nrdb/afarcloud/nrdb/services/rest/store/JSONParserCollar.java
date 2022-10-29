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

 * 20200926 Sara Lana:: added insertion into mysql of the last observation
 *       if can't connect to MySQL continue execution performing influxDB injection  
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
import afarcloud.nrdb.dam.MySQLDataAccess;
import afarcloud.nrdb.util.GlobalParameters;
import ch.hsr.geohash.GeoHash;

/**
 * Collar
 *
 */
public class JSONParserCollar extends JSONParser {

	/***********    influxDB output    ************* */
	private static final List<String> INFLUX_TAGS; //special processing: resourceId, geohash:: type=>collar  
	private static final List<String> INFLUX_FIELDS; // special processing: geohash

	
	private static final String INFLUX_MEASUREMENT = Constants.IDB_COLLAR_MEASUREMENT;
	
	private static final String JSON_SEQUENCENUMBER = Constants.IDB_SEQUENCE_NUMBER;
	private static final String JSON_TEMPERATURE = Constants.IDB_COLLAR_TEMPERATURE;
	private static final String JSON_EPOCH = "resultTime";
	private static final String JSON_RESOURCEALARM = "resourceAlarm";

	
	private static final String JSON_RESOURCEID = "resourceId";

	private static final String JSON_LOCATION = "location";
	private static final String JSON_LONGITUDE = Constants.IDB_LOCATION_LONGITUDE;
	private static final String JSON_LATITUDE = Constants.IDB_LOCATION_LATITUDE;
	private static final String JSON_ALTITUDE = Constants.IDB_LOCATION_ALTITUDE;
	private static final String INFLUX_GEOHASH = Constants.IDB_LOCATION_GEOHASH;


	private static final String JSON_ANOMALIES = "anomalies";
	private static final String JSON_AN_ACTIVITY = Constants.IDB_COLLAR_ANOMALY_ACTIVITY;
	private static final String JSON_AN_DISTANCE = Constants.IDB_COLLAR_ANOMALY_DISTANCE;	
	private static final String JSON_AN_LOCATION = Constants.IDB_COLLAR_ANOMALY_LOCATION;
	private static final String JSON_AN_POSITION = Constants.IDB_COLLAR_ANOMALY_POSITION;
	private static final String JSON_AN_TEMPERATURE = Constants.IDB_COLLAR_ANOMALY_TEMPERATURE;

	
	private static final String JSON_ACCELERATION = "acceleration";
	private static final String JSON_ACC_X = Constants.IDB_COLLAR_ACC_X;
	private static final String JSON_ACC_Y = Constants.IDB_COLLAR_ACC_Y;
	private static final String JSON_ACC_Z = Constants.IDB_COLLAR_ACC_Z;
	

	private static final String JSON_COLLAR = "collar";
	private static final String JSON_COLLARS = "collars";
	
	
	static {
		INFLUX_TAGS = new ArrayList<String> (Arrays.asList(new String[] {}));
		//numeric fields must be float
		INFLUX_FIELDS = new ArrayList<String> (Arrays.asList(new String[] { JSON_TEMPERATURE, JSON_LONGITUDE, JSON_LATITUDE, JSON_ALTITUDE, INFLUX_GEOHASH, JSON_ACC_X, JSON_ACC_Y, JSON_ACC_Z, JSON_SEQUENCENUMBER, JSON_AN_TEMPERATURE, JSON_RESOURCEALARM, JSON_AN_ACTIVITY, JSON_AN_DISTANCE, JSON_AN_LOCATION, JSON_AN_POSITION }));
	}

	//properties to be extracted from JSON 
	private static final List<String> FIRSTLEVEL_PROPERTIES;
	private static final List<String> COLLAR_PROPERTIES;
	private static final List<String> LOCATION_PROPERTIES; 
	private static final List<String> ANOMALIES_PROPERTIES;
	private static final List<String> ACCELERATION_PROPERTIES;

    static {
    	FIRSTLEVEL_PROPERTIES = new ArrayList<String> (Arrays.asList(new String[] { JSON_SEQUENCENUMBER }));
    	COLLAR_PROPERTIES = new ArrayList<String> (Arrays.asList(new String[] { JSON_RESOURCEID, JSON_EPOCH, JSON_RESOURCEALARM, JSON_TEMPERATURE }));
    	LOCATION_PROPERTIES = new ArrayList<String> (Arrays.asList(new String[] { JSON_LONGITUDE, JSON_LATITUDE, JSON_ALTITUDE}));
    	ANOMALIES_PROPERTIES = new ArrayList<String> (Arrays.asList(new String[] { JSON_AN_ACTIVITY, JSON_AN_DISTANCE, JSON_AN_LOCATION, JSON_AN_POSITION, JSON_AN_TEMPERATURE }));
    	ACCELERATION_PROPERTIES = new ArrayList<String> (Arrays.asList(new String[] { JSON_ACC_X, JSON_ACC_Y, JSON_ACC_Z}));
    }
	
	//first level properties
    private HashMap<String, Object> hFirstLevel = null;
	private ArrayList<HashMap <String, Object> > lCollars = null;
	
	MySQLDataAccess oMySQL = null;
	MySQLLoaderCollar oMySQLLoader = null;
	
		// key => MySQL_COLLAR_ID
	private HashMap<String, HashMap<String,Object>> hMySQLData = null;
	// End MySQL Loader   *************

	
	/**
	 * 
	 */
	public JSONParserCollar() {
		hFirstLevel = new HashMap<String, Object>();
		
		// Start MySQL Loader   ***********
		oMySQL = new MySQLDataAccess();		
		oMySQLLoader = new MySQLLoaderCollar(oMySQL);
		// End MySQL Loader   *************
	

	}

	@Override
	protected BatchPoints _parse() throws Exception{
		//init
		if (hFirstLevel!= null && hFirstLevel.size()>0) hFirstLevel.clear();
		if (lCollars!= null && lCollars.size()>0) lCollars.clear();
		JsonToken jsonToken;
		
		//root element must be a object		
		while(!oParser.isClosed()){
		    jsonToken = oParser.nextToken();
		    
		    if(JsonToken.FIELD_NAME.equals(jsonToken)){
		        String fieldName = oParser.getCurrentName();
	        	//move to next token:: JsonToken.START_ARRAY || JsonToken.START_OBJECT || JsonToken.VALUE_XXX
	            oParser.nextToken();

		        //System.out.println("\t\t" + fieldName);
	            if(FIRSTLEVEL_PROPERTIES.contains(fieldName)){
	            	hFirstLevel.put(fieldName, JSONParserUtils.parseValue(oParser));		        	
		        }else if (JSON_COLLAR.equalsIgnoreCase(fieldName)) {
		        	lCollars = new ArrayList<HashMap<String,Object>>();
		        	lCollars.add(_parseCollar());
		        }else if (JSON_COLLARS.equalsIgnoreCase(fieldName)) {
		        	lCollars = _parseCollars();
		        	
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
	
	private HashMap <String, Object> _parseCollar() throws Exception{
		HashMap <String, Object> hOut = new HashMap <String, Object>();
		HashMap <String, Object> hAux = null;		
		JsonToken jsonToken;

    	///move to next token:: JsonToken.START_OBJECT
	    jsonToken = oParser.nextToken();
	    
	    // parse observation
	    while(!JsonToken.END_OBJECT.equals(jsonToken)){
		    if(JsonToken.FIELD_NAME.equals(jsonToken)){
		        String fieldName = oParser.getCurrentName();
	        	//move to next token:: JsonToken.START_ARRAY || JsonToken.START_OBJECT || JsonToken.VALUE_XXX
	            oParser.nextToken();
	            if(COLLAR_PROPERTIES.contains(fieldName)){
	            	hOut.put(fieldName, JSONParserUtils.parseValue(oParser));		        	
		        }else if (JSON_LOCATION.equalsIgnoreCase(fieldName)) {		        	
		        	hAux = JSONParserUtils.parseObject(oParser, LOCATION_PROPERTIES);
		        	if (hAux != null && !hAux.isEmpty()) {
		    			if( hAux.containsKey(JSON_LATITUDE) && hAux.containsKey(JSON_LONGITUDE)) {
		    				hAux.put(INFLUX_GEOHASH, GeoHash.geoHashStringWithCharacterPrecision((double)hAux.get(JSON_LATITUDE), (double)hAux.get(JSON_LONGITUDE) , GEOHASH_PRECISION));
		    			}
		    			hOut.putAll(hAux);
		        	}
		        	
		        }else if (JSON_ANOMALIES.equalsIgnoreCase(fieldName)) {
		        	hAux = JSONParserUtils.parseObject(oParser, ANOMALIES_PROPERTIES);
		        	if (hAux != null && !hAux.isEmpty()) {
		        		hOut.putAll(hAux);
		        	}
		        }else if (JSON_ACCELERATION.equalsIgnoreCase(fieldName)){		        	
		        	hAux = JSONParserUtils.parseObject(oParser, ACCELERATION_PROPERTIES);
		        	if (hAux != null && !hAux.isEmpty()) {
		        		hOut.putAll(hAux);
		        	}
		        	
		        }else{
		        	if (oParser.currentToken().equals(JsonToken.START_ARRAY) || 
		        		oParser.currentToken().equals(JsonToken.START_OBJECT)){
		        		oParser.skipChildren();
		        	}
		        }
	            
		    }
		    jsonToken = oParser.nextToken();
	    } //END_OBJECT		    
		
		return hOut;
	}
	
	private ArrayList<HashMap <String, Object> > _parseCollars() throws Exception{
		ArrayList<HashMap <String, Object> > lCollars = new ArrayList<HashMap <String, Object> >();
		
		//move to next token:: JsonToken.START_ARRAY => START_OBJECT
	    JsonToken jsonToken = oParser.nextToken();
	    while (!JsonToken.END_ARRAY.equals(jsonToken)){	
	    	
	
		    lCollars.add(_parseCollar());
		
			//move to next token:: JsonToken.START_OBJECT || JsonToken.END_ARRAY
		    jsonToken = oParser.nextToken();
	    } //END_ARRAY
		
		
		return lCollars;		
	} 
	
	/** serialization to BatchPoints **/
	private BatchPoints _toInfluxDBBatchPoints() {		
		BatchPoints batchPoints = null;
		
		if (lCollars!=null && lCollars.size()>0) {
			// Start MySQL Loader   ***********
			hMySQLData = new HashMap<String, HashMap<String,Object>>(); 
			// End MySQL Loader   *************
			
			for(HashMap<String, Object> hCollar : lCollars) {
				if (hCollar.containsKey(JSON_RESOURCEID)) {
					//tags					
					LinkedHashMap<String, String> hTags = JSONParserUtils.parseAFC_Id((String) hCollar.get(JSON_RESOURCEID), AFC_ID, AFC_ID_PADDING, "");
					
					sResourceId = (String) hCollar.get(JSON_RESOURCEID);
					
					if(batchPoints==null & hTags!=null && hTags.containsKey(AFC_SCENARIO)) {
						sDBName = hTags.get(AFC_SCENARIO);
						
						batchPoints = BatchPoints
								.database(sDBName.toUpperCase())
						        .consistency(ConsistencyLevel.ALL)
						        .build();
					}
					
					for (String sTag : INFLUX_TAGS) {						
						if (hCollar.containsKey(sTag)) {
							hTags.put(sTag, String.valueOf(hCollar.get(sTag)).trim());
						} else if (hFirstLevel.containsKey(sTag)) {
							hTags.put(sTag, String.valueOf(hFirstLevel.get(sTag)).trim());
						}
					}
					
					//fields => if numeric must be float
					LinkedHashMap<String, Object> hFields = new LinkedHashMap <String, Object>();
					Object oField=null;
					for (String sField : INFLUX_FIELDS) {
						/*
						if (hCollar.containsKey(sField)) {
							hFields.put(sField, hCollar.get(sField));
						} else if (hFirstLevel.containsKey(sField)) {
							hFields.put(sField, hFirstLevel.get(sField));
						}
						*/
						if (hCollar.containsKey(sField)) {
							oField = hCollar.get(sField);
						} else if (hFirstLevel.containsKey(sField)) {
							oField = hFirstLevel.get(sField);
						}
						if (oField!=null) {
							hFields.put(sField,JSONParserUtils.ObjectToFloat(oField));
						}
						
					}	
					
					
					Long lEpoch = Long.valueOf(String.valueOf(hCollar.get(JSON_EPOCH)));
					TimeUnit time = TimeUnit.SECONDS;
					Long lEpochInfluxDB = time.toMicros(lEpoch);
						
					batchPoints.point(
							Point.measurement(GlobalParameters.getInstance().getParameter(GlobalParameters.PREFIX_COLLAR) + INFLUX_MEASUREMENT)
									.tag(hTags)
									.fields(hFields)
									.time(lEpochInfluxDB, TimeUnit.MICROSECONDS)
									.build()
							);
					
			// Start MySQL Loader   ***********
				// integrate values
				hFirstLevel.put(JSON_EPOCH, lEpoch);
				_toMySQLLoadData(hTags, hFields);
				// End MySQL Loader   *************

				}

			}
		}
		// Start MySQL Loader   ***********
		//needed for each file		
		try {
			oMySQL.openConnection();
			int nRow;
			// open-close connection to each file
			if (hMySQLData.size()>0) {
				for(HashMap<String, Object>hValue : hMySQLData.values()) {
					nRow = oMySQLLoader.loadDataIntoMySQL(hValue);
					//System.out.println ("\n\tJSONParser-OM $MySQL INSERT: " + nRow);
				}				
			}
			oMySQL.closeConnection(); 
		} catch (Exception e) {}	// do nothing
		// End MySQL Loader   *************

		return batchPoints;
		
	}
	
	
	// Start MySQL Loader   ***********
	private void _toMySQLLoadData(LinkedHashMap<String, String> hTags, LinkedHashMap<String, Object> hFields) {
				
		// key => MySQL_COLLAR_ID
		String sMySQL_Key =  String.valueOf(hTags.get(Constants.IDB_ENTITY_NAME));					
		
		HashMap<String, Object> hPrevious =  hMySQLData.get(sMySQL_Key);
		
		if(	hPrevious == null ||
			Long.valueOf(String.valueOf(hPrevious.get(Constants.MySQL_TIME))) <  Long.valueOf(String.valueOf(hFirstLevel.get(JSON_EPOCH)))
			) {
		
		
			HashMap<String, Object> hValue = new HashMap<String, Object>();
			hValue.put(Constants.MySQL_COLLAR_ID, hTags.get(Constants.IDB_ENTITY_NAME));
			
			hValue.put(Constants.MySQL_LOCATION_LONGITUDE,
						hFields.containsKey(JSON_LONGITUDE)? hFields.get(JSON_LONGITUDE):
						hTags.containsKey(JSON_LONGITUDE)? hTags.get(JSON_LONGITUDE): null 	
						);
			
			hValue.put(Constants.MySQL_LOCATION_LATITUDE,
						hFields.containsKey(JSON_LATITUDE)? hFields.get(JSON_LATITUDE):
						hTags.containsKey(JSON_LATITUDE)? hTags.get(JSON_LATITUDE): null 						
						);
			
			hValue.put(Constants.MySQL_LOCATION_ALTITUDE,
						hFields.containsKey(JSON_ALTITUDE)? hFields.get(JSON_ALTITUDE):
						hTags.containsKey(JSON_LATITUDE)? hTags.get(JSON_ALTITUDE): null
						);

			hValue.put(Constants.MySQL_COLLAR_TEMPERATURE,
					hFields.containsKey(JSON_TEMPERATURE)? hFields.get(JSON_TEMPERATURE):
					hTags.containsKey(JSON_TEMPERATURE)? hTags.get(JSON_TEMPERATURE): null
					);
			
			hValue.put(Constants.MySQL_COLLAR_RESOURCEALARM,
					hFields.containsKey(JSON_RESOURCEALARM)? hFields.get(JSON_RESOURCEALARM):
					hTags.containsKey(JSON_RESOURCEALARM)? hTags.get(JSON_RESOURCEALARM): null
					);

			hValue.put(Constants.MySQL_COLLAR_ANOMALY_LOCATION,
					hFields.containsKey(JSON_AN_LOCATION)? hFields.get(JSON_AN_LOCATION):
					hTags.containsKey(JSON_AN_LOCATION)? hTags.get(JSON_AN_LOCATION): null
					);

			hValue.put(Constants.MySQL_COLLAR_ANOMALY_TEMPERATURE,
					hFields.containsKey(JSON_AN_TEMPERATURE)? hFields.get(JSON_AN_TEMPERATURE):
					hTags.containsKey(JSON_AN_TEMPERATURE)? hTags.get(JSON_AN_TEMPERATURE): null
					);

			hValue.put(Constants.MySQL_COLLAR_ANOMALY_DISTANCE,
					hFields.containsKey(JSON_AN_DISTANCE)? hFields.get(JSON_AN_DISTANCE):
					hTags.containsKey(JSON_AN_DISTANCE)? hTags.get(JSON_AN_DISTANCE): null
					);
			
			hValue.put(Constants.MySQL_COLLAR_ANOMALY_POSITION,
					hFields.containsKey(JSON_AN_POSITION)? hFields.get(JSON_AN_POSITION):
					hTags.containsKey(JSON_AN_POSITION)? hTags.get(JSON_AN_POSITION): null
					);
			
			hValue.put(Constants.MySQL_TIME,
					Long.valueOf(String.valueOf(hFirstLevel.get(JSON_EPOCH)))
					);
		
		
			//System.out.println("\nMySQL Key: " + sMySQL_Key);
			//System.out.println("values: " + hValue.toString());
			
			hMySQLData.put(sMySQL_Key, hValue);
		}
		
	}
	// End MySQL Loader   *************

}
