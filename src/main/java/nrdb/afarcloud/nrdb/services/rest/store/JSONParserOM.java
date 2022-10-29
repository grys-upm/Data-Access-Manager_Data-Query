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

import ch.hsr.geohash.GeoHash;
import afarcloud.nrdb.config.Constants;
import afarcloud.nrdb.dam.MySQLDataAccess;
import afarcloud.nrdb.util.GlobalParameters;

/**
 * O&M: Observation and Measurements
 *
 */
public class JSONParserOM extends JSONParser {

	/***********    influxDB output    ************* */
	// tags  => add geohash
	private static final List<String> INFLUX_TAGS; //special processing: resourceId, deviceId, geohash
	//fields => add geohash  
	private static final List<String> INFLUX_FIELDS; // special processing: geohash

	private static final String INFLUX_DEVICEID_SUFFIX = "_dev";
	
	/****************************************************/
	private static final String JSON_RESOURCEID = "resourceId";
	private static final String JSON_DEVICEID = "deviceId";
	private static final String JSON_MEASUREMENT = "observedProperty";
	private static final String JSON_EPOCH = "resultTime";
	
	private static final String JSON_LOCATION = "location";
	private static final String JSON_LONGITUDE = Constants.IDB_LOCATION_LONGITUDE;
	private static final String JSON_LATITUDE = Constants.IDB_LOCATION_LATITUDE;
	private static final String JSON_ALTITUDE = Constants.IDB_LOCATION_ALTITUDE;
	private static final String INFLUX_GEOHASH = Constants.IDB_LOCATION_GEOHASH;
	private static final String JSON_SEQUENCENUMBER = Constants.IDB_SEQUENCE_NUMBER;
	
	private static final String JSON_RESULT = "result";
	private static final String JSON_UOM = Constants.IDB_OM_UOM;
	private static final String JSON_VALUE = Constants.IDB_OM_VALUE;

	private static final String JSON_OBSERVATIONS = "observations";

 
	
	static {
		INFLUX_TAGS = new ArrayList<String> (Arrays.asList(new String[] { JSON_UOM }));
		//numeric fields must be float
		INFLUX_FIELDS = new ArrayList<String> (Arrays.asList(new String[] { JSON_VALUE, JSON_LONGITUDE, JSON_LATITUDE, JSON_ALTITUDE, INFLUX_GEOHASH, JSON_SEQUENCENUMBER }));
	}

	//properties to be extracted from JSON 
	private static final List<String> FIRSTLEVEL_PROPERTIES;
	private static final List<String> LOCATION_PROPERTIES; 
	private static final List<String> RESULT_PROPERTIES;
	private static final List<String> OBSERVATION_PROPERTIES;
	

    static {
    	FIRSTLEVEL_PROPERTIES = new ArrayList<String> (Arrays.asList(new String[] { JSON_RESOURCEID, JSON_DEVICEID, JSON_MEASUREMENT, JSON_EPOCH, JSON_UOM, JSON_SEQUENCENUMBER }));
    	
    	LOCATION_PROPERTIES = new ArrayList<String> (Arrays.asList(new String[] { JSON_LONGITUDE, JSON_LATITUDE, JSON_ALTITUDE}));
    	RESULT_PROPERTIES = new ArrayList<String> (Arrays.asList(new String[] { JSON_VALUE, JSON_UOM }));
    	OBSERVATION_PROPERTIES = new ArrayList<String> (Arrays.asList(new String[] { JSON_RESOURCEID, JSON_MEASUREMENT, JSON_EPOCH}));
    }
	
	//first level properties
    private HashMap<String, Object> hFirstLevel = null;
    	
	private HashMap<String, Object> hLocation = null; // convert to geohash
	private HashMap<String, Object> hResult = null;	
	private ArrayList<HashMap <String, Object> > lObservations = null;

	
	
	// Start MySQL Loader   ***********
	MySQLDataAccess oMySQL = null;
	MySQLLoaderOM oMySQLLoader = null;
	
		// key => MySQL_OM_ENTITY_NAME-MySQL_OM_OBSERVED_PROPERTY
	private HashMap<String, HashMap<String,Object>> hMySQLData = null;
	// End MySQL Loader   *************
	
	/**
	 * 
	 * 
	 */
	public JSONParserOM() {
		hFirstLevel = new HashMap<String, Object>();
		
		// Start MySQL Loader   ***********
		oMySQL = new MySQLDataAccess();		
		oMySQLLoader = new MySQLLoaderOM(oMySQL);
		// End MySQL Loader   *************

	}

	@Override
	protected BatchPoints _parse() throws Exception{
		//init
		if (hFirstLevel!= null && hFirstLevel.size()>0) hFirstLevel.clear();
		if (hLocation!= null && hLocation.size()>0) hLocation.clear();
		if(hResult!= null && hResult.size()>0) hResult.clear();
		if (lObservations!= null && lObservations.size()>0) lObservations.clear();
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
		        }else if (JSON_RESULT.equalsIgnoreCase(fieldName)) {
		        	this.hResult = JSONParserUtils.parseObject(oParser, RESULT_PROPERTIES);
		        }else if (JSON_OBSERVATIONS.equalsIgnoreCase(fieldName)){		        	
		        	//observations
		        	lObservations = this._parseObservations();
		        	
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

	
	/**
	 * @return
	 */
	private ArrayList<HashMap <String, Object> > _parseObservations() throws Exception{
		ArrayList<HashMap <String, Object> > lObservations = new ArrayList<HashMap <String, Object> >(); 


		//move to next token:: JsonToken.START_ARRAY
	    JsonToken jsonToken = oParser.nextToken();
	    while (!JsonToken.END_ARRAY.equals(jsonToken)){
	    	
	    	
	    	///move to next token:: JsonToken.START_OBJECT
		    jsonToken = oParser.nextToken();
		    // parse observation
		    HashMap <String, Object> hOut = new HashMap <String, Object>();
		    while(!JsonToken.END_OBJECT.equals(jsonToken)){
			    if(JsonToken.FIELD_NAME.equals(jsonToken)){
			        String fieldName = oParser.getCurrentName();
		        	//move to next token:: JsonToken.START_ARRAY || JsonToken.START_OBJECT || JsonToken.VALUE_XXX
		            oParser.nextToken();
		            if(OBSERVATION_PROPERTIES.contains(fieldName)){
		            	hOut.put(fieldName, JSONParserUtils.parseValue(oParser));
		            }else if ("result".equalsIgnoreCase(fieldName)) {
		            	hOut.putAll( JSONParserUtils.parseObject(oParser, RESULT_PROPERTIES) );
		            }
			    }
			    
			    jsonToken = oParser.nextToken();
		    } //END_OBJECT
		    if (!hOut.isEmpty()){
		    	lObservations.add(hOut);
		    }
			//move to next token:: JsonToken.START_OBJECT || JsonToken.END_ARRAY
		    jsonToken = oParser.nextToken();
	    } //END_ARRAY
			

		return lObservations;
	}	
	
	/** serialization to BatchPoints **/
	private BatchPoints _toInfluxDBBatchPoints() {
		
		/* deviceId and resourceId */		
		LinkedHashMap<String, String> hResourceId= (hFirstLevel.containsKey(JSON_RESOURCEID))
				? JSONParserUtils.parseAFC_Id((String) hFirstLevel.get(JSON_RESOURCEID), AFC_ID, AFC_ID_PADDING, "")
				: null;
		
		sResourceId = (String) hFirstLevel.get(JSON_RESOURCEID);
				
		if(hResourceId!=null && hResourceId.containsKey(AFC_SCENARIO)) {
			sDBName = hResourceId.get(AFC_SCENARIO);
		}
		
		LinkedHashMap<String, String> hDeviceId= (hFirstLevel.containsKey(JSON_DEVICEID))
				? JSONParserUtils.parseAFC_Id((String) hFirstLevel.get(JSON_DEVICEID), AFC_ID, AFC_ID_PADDING,  INFLUX_DEVICEID_SUFFIX)
				: null;

		if(sDBName.isEmpty() && hDeviceId!=null && hDeviceId.containsKey(AFC_SCENARIO+INFLUX_DEVICEID_SUFFIX)) {
				sDBName = hDeviceId.get(AFC_SCENARIO+INFLUX_DEVICEID_SUFFIX);
		}
				
		if(hLocation!=null && !hLocation.isEmpty()){
			hFirstLevel.putAll(hLocation);
			if( hFirstLevel.containsKey(JSON_LATITUDE) && hFirstLevel.containsKey(JSON_LONGITUDE)) {
				String sGeo = GeoHash.geoHashStringWithCharacterPrecision((double)hFirstLevel.get(JSON_LATITUDE), (double)hFirstLevel.get(JSON_LONGITUDE) , GEOHASH_PRECISION);
				hFirstLevel.put(INFLUX_GEOHASH, sGeo);
			}
		}
		
		if(hResult!=null && !hResult.isEmpty()){
			hFirstLevel.putAll(hResult);
		}
		
		BatchPoints batchPoints = BatchPoints
				.database(sDBName.toUpperCase())
		        .consistency(ConsistencyLevel.ALL)
		        .build();
		
		if (lObservations != null && lObservations.size() > 0) {
			// Start MySQL Loader   ***********
			hMySQLData = new HashMap<String, HashMap<String,Object>>(); 
			// End MySQL Loader   ************* 
			
			for (HashMap<String, Object> hObservation : lObservations) {
				
				//tags
				LinkedHashMap<String, String> hTags = (hObservation.containsKey(JSON_RESOURCEID))
						? JSONParserUtils.parseAFC_Id((String) hObservation.get(JSON_RESOURCEID), AFC_ID, AFC_ID_PADDING, "")
						: new LinkedHashMap<String, String>(hResourceId);
//				LinkedHashMap<String, String> hTags = new LinkedHashMap<String, String>();

				if( hObservation.containsKey(JSON_LATITUDE) && hObservation.containsKey(JSON_LONGITUDE)) {
						String sGeo = GeoHash.geoHashStringWithCharacterPrecision((double)hObservation.get(JSON_LATITUDE), (double)hObservation.get(JSON_LONGITUDE) , GEOHASH_PRECISION);
						hObservation.put(INFLUX_GEOHASH, sGeo);
				}		

				for (String sTag : INFLUX_TAGS) {
					if (hObservation.containsKey(sTag)) {
						hTags.put(sTag, String.valueOf(hObservation.get(sTag)).trim());
					} else if (hFirstLevel.containsKey(sTag)) {
						hTags.put(sTag, String.valueOf(hFirstLevel.get(sTag)).trim());
					}
				}
				
				if(hDeviceId!=null) {
					hTags.putAll(hDeviceId);
				}
			
				//fields => if numeric must be float
				LinkedHashMap<String, Object> hFields = new LinkedHashMap <String, Object>();
//				LinkedHashMap<String, Object> hFields = (hObservation.containsKey(JSON_RESOURCEID))
//						? JSONParserUtils.parseAFC_Id_asField((String) hObservation.get(JSON_RESOURCEID), AFC_ID, AFC_ID_PADDING, "")
//						: new LinkedHashMap<String, Object>(hResourceId);
						
				Object oField;
				for (String sField : INFLUX_FIELDS) {
					if (hObservation.containsKey(sField)) {
						oField = hObservation.get(sField);
					}
					else {
						oField = hFirstLevel.get(sField);
					}
					hFields.put(sField,JSONParserUtils.ObjectToFloat(oField));
					/*
					if (hObservation.containsKey(sField)) {						
						hFields.put(sField, hObservation.get(sField));
					} else if (hFirstLevel.containsKey(sField)) {
						hFields.put(sField, hFirstLevel.get(sField));
					}
					*/
					
					
				}	
//				if(hDeviceId!=null) {
//					hFields.putAll(hDeviceId);
//				}
				String sMeasurement = (hObservation.containsKey(JSON_MEASUREMENT))
							?String.valueOf(hObservation.get(JSON_MEASUREMENT)).trim()
							:String.valueOf(hFirstLevel.get(JSON_MEASUREMENT)).trim();
				Long lEpoch = (hObservation.containsKey(JSON_EPOCH))
							?Long.valueOf(String.valueOf(hObservation.get(JSON_EPOCH)))
							:Long.valueOf(String.valueOf(hFirstLevel.get(JSON_EPOCH)));

				TimeUnit time = TimeUnit.SECONDS;
				Long lEpochInfluxDB = time.toMicros(lEpoch);
							
				System.out.println("Las tags son las siguientes"+hTags);
				batchPoints.point(
						Point.measurement(GlobalParameters.getInstance().getParameter(GlobalParameters.PREFIX_OBSERVATION) + sMeasurement)
								.tag(hTags)
								.fields(hFields)
								.time(lEpochInfluxDB, TimeUnit.MICROSECONDS)
								.build()
						);

				// Start MySQL Loader   ***********
					// integrate values
				hFirstLevel.put(JSON_EPOCH, lEpoch);
				hFirstLevel.put(JSON_MEASUREMENT, sMeasurement);
				_toMySQLLoadData(hTags, hFields);
				// End MySQL Loader   *************

			}

		}else{ //SimpleMeasurement
			if (hResourceId!=null && !hResourceId.isEmpty()){
				hMySQLData = new HashMap<String, HashMap<String,Object>>();
				
				//tags :: [,<tag-key>=<tag-value>...]
				LinkedHashMap<String, String> hTags = new LinkedHashMap<String, String>(hResourceId);									
				
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
						Point.measurement(GlobalParameters.getInstance().getParameter(GlobalParameters.PREFIX_OBSERVATION) + String.valueOf(hFirstLevel.get(JSON_MEASUREMENT)))
								.tag(hTags)
								.fields(hFields)
								.time(lEpoch, TimeUnit.MICROSECONDS)
								.build()
						);
				
				// Start MySQL Loader   ***********
				_toMySQLLoadData(hTags, hFields);
				// End MySQL Loader   *************
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
				
		// key => MySQL_OM_ENTITY_NAME-MySQL_OM_OBSERVED_PROPERTY
		String sMySQL_Key =  String.valueOf(hTags.get(Constants.IDB_ENTITY_NAME)) + "-" + String.valueOf(hFirstLevel.get(JSON_MEASUREMENT));					
		
		HashMap<String, Object> hPrevious =  hMySQLData.get(sMySQL_Key);
		
		if(	hPrevious == null ||
			Long.valueOf(String.valueOf(hPrevious.get(Constants.MySQL_TIME))) <  Long.valueOf(String.valueOf(hFirstLevel.get(JSON_EPOCH)))
			) {
		
		
			HashMap<String, Object> hValue = new HashMap<String, Object>();
			hValue.put(Constants.MySQL_OM_ENTITY_NAME, hTags.get(Constants.IDB_ENTITY_NAME));
			hValue.put(Constants.MySQL_OM_OBSERVED_PROPERTY, hFirstLevel.get(JSON_MEASUREMENT));
			hValue.put(Constants.MySQL_OM_TYPE,	hTags.get(Constants.IDB_TYPE));
			
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
			
			hValue.put(Constants.MySQL_OM_UOM,
						hTags.containsKey(JSON_UOM)? hTags.get(JSON_UOM): null
						);
			
			hValue.put(Constants.MySQL_TIME,
					Long.valueOf(String.valueOf(hFirstLevel.get(JSON_EPOCH)))
					);
		
			hValue.put(Constants.MySQL_OM_VALUE,
					hFields.containsKey(JSON_VALUE)? hFields.get(JSON_VALUE): null
					);
		
			//System.out.println("\nMySQL Key: " + sMySQL_Key);
			//System.out.println("values: " + hValue.toString());
			
			hMySQLData.put(sMySQL_Key, hValue);
		}
		
	}
	// End MySQL Loader   *************

}

