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

/**
 * Region
 *
 */
 public class JSONParserRegion extends JSONParser {
		
	/* no default value */
	static {		
		AFC_ID = new String[] { AFC_SCENARIO, Constants.IDB_SERVICE, Constants.IDB_PROVIDER, Constants.IDB_ENTITY_NAME};		
	}

	/***********    influxDB output    ************* */
	private static final List<String> INFLUX_TAGS; //special processing: resourceId, geohash:: 
	private static final List<String> INFLUX_FIELDS; // special processing: geohash

	
	private static final String INFLUX_MEASUREMENT = Constants.IDB_REGION_MEASUREMENT;
	private static final String JSON_SEQUENCENUMBER = Constants.IDB_SEQUENCE_NUMBER;
	private static final String JSON_COMPONENTID = "componentId";
	
	
	private static final String JSON_OBSERVED_PROPERTY = Constants.IDB_REGION_OBSERVED_PROPERTY;
	private static final String JSON_RESULT = Constants.IDB_REGION_OBSERVED_RESULT;
	private static final String JSON_EPOCH = "resultTime";
	
	private static final String JSON_LOCATION = "locationDimension";
	private static final String JSON_LOC_NUMPOINTS = "nPoints";
	private static final String JSON_LOC_LAT_ARRAY = "latCoordinates";
	private static final String JSON_LOC_LONG_ARRAY = "lonCoordinates";
	
	private static final String INFLUX_GEO_NUMPOINTS = Constants.IDB_REGION_GEO_NUMPOINTS;
	private static final String INFLUX_GEO_COORDINATES = Constants.IDB_REGION_GEO_COORDINATES; /* FORMATO :: [ [<lat>,<long>],[<lat>,<long>],...,] */
	

	private static final String JSON_REGION = "region";
	private static final String JSON_REGIONS = "regions";

	static {
		INFLUX_TAGS = new ArrayList<String> (Arrays.asList(new String[] { JSON_SEQUENCENUMBER, JSON_OBSERVED_PROPERTY, INFLUX_GEO_NUMPOINTS, JSON_RESULT}));
		//numeric fields must be float
		INFLUX_FIELDS = new ArrayList<String> (Arrays.asList(new String[] { INFLUX_GEO_COORDINATES }));
	}

	//properties to be extracted from JSON 
	private static final List<String> FIRSTLEVEL_PROPERTIES;
	private static final List<String> REGION_PROPERTIES;
	
    static {
    	FIRSTLEVEL_PROPERTIES = new ArrayList<String> (Arrays.asList(new String[] {JSON_COMPONENTID, JSON_SEQUENCENUMBER }));
    	REGION_PROPERTIES = new ArrayList<String> (Arrays.asList(new String[] {JSON_OBSERVED_PROPERTY, JSON_RESULT, JSON_EPOCH }));
    }

    //first level properties
    private HashMap<String, Object> hFirstLevel = null;
	private ArrayList<HashMap <String, Object> > lRegions = null;
    
	/**
	 * 
	 */
	public JSONParserRegion() {
		hFirstLevel = new HashMap<String, Object>();

	}

	@Override
	protected BatchPoints _parse() throws Exception {
		//init
		if (hFirstLevel!= null && hFirstLevel.size()>0) hFirstLevel.clear();
		
		if (lRegions!= null && lRegions.size()>0) lRegions.clear();
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
		        }else if (JSON_REGION.equalsIgnoreCase(fieldName)) {
		        	lRegions = new ArrayList<HashMap<String,Object>>();
		        	lRegions.add(_parseRegion());
		        }else if (JSON_REGIONS.equalsIgnoreCase(fieldName)) {
		        	lRegions = _parseRegions();
		        	
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

	
	private HashMap<String, Object> _parseLocation () throws Exception{
		HashMap<String, Object> hOut = new HashMap<String, Object>();
		Number nPoint = 0;
		ArrayList<String> lLat = null;
		ArrayList<String> lLong = null;
		JsonToken jsonToken = oParser.nextToken();
		

			while(!JsonToken.END_OBJECT.equals(jsonToken)){
			    
			    if(JsonToken.FIELD_NAME.equals(jsonToken)){
			        String fieldName = oParser.getCurrentName();
		        	//move to next token:: JsonToken.START_ARRAY || JsonToken.START_OBJECT || JsonToken.VALUE_XXX
		            oParser.nextToken();
		            if(JSON_LOC_NUMPOINTS.equals(fieldName)){
		            	if(oParser.currentToken() == JsonToken.VALUE_NUMBER_INT) {
		            		nPoint = oParser.getNumberValue();
		            		hOut.put(JSON_LOC_NUMPOINTS,nPoint);
		            	}
		            }else if(JSON_LOC_LAT_ARRAY.equals(fieldName)) {
		            	lLat =  JSONParserUtils._parseArrayOfValues(oParser);
		            }else if(JSON_LOC_LONG_ARRAY.equals(fieldName)) {
		            	lLong= JSONParserUtils._parseArrayOfValues(oParser);
		            }		            
			    }
			    jsonToken = oParser.nextToken();
			}
			if (nPoint.intValue()>0 &&
					lLat!=null && lLat.size()==nPoint.intValue() &&
					lLong!=null && lLong.size()==nPoint.intValue()
					) {
				String[] aLat = new String[lLat.size()];
				aLat = lLat.toArray(aLat);
				
				String[] aLong = new String[lLong.size()];
				aLong = lLong.toArray(aLong);

				String sGeo ="[";
				for(int i=0; i<nPoint.intValue(); i++){
					if (i>0) {
						sGeo+= ", ";
					}
					sGeo+="[" + aLat[i] + ", " + aLong[i]+"]";
				}
				sGeo+="]";
				
				hOut.put(JSON_LOC_LAT_ARRAY, lLat.toString());
				hOut.put(JSON_LOC_LONG_ARRAY, lLong.toString());
				hOut.put(INFLUX_GEO_NUMPOINTS, nPoint);
				hOut.put(INFLUX_GEO_COORDINATES, sGeo);
			}
			
		return hOut;
		
	}
		
	
	private HashMap <String, Object> _parseRegion() throws Exception{
		HashMap <String, Object> hOut = new HashMap <String, Object>();		
		JsonToken jsonToken;

    	///move to next token:: JsonToken.START_OBJECT
	    jsonToken = oParser.nextToken();
	    
	    // parse observation
	    while(!JsonToken.END_OBJECT.equals(jsonToken)){
		    if(JsonToken.FIELD_NAME.equals(jsonToken)){
		        String fieldName = oParser.getCurrentName();
	        	//move to next token:: JsonToken.START_ARRAY || JsonToken.START_OBJECT || JsonToken.VALUE_XXX
	            oParser.nextToken();
	            if(REGION_PROPERTIES.contains(fieldName)){
	            	hOut.put(fieldName, JSONParserUtils.parseValue(oParser));		        	
		        }else if (JSON_LOCATION.equalsIgnoreCase(fieldName)) {		 
		        	hOut.putAll(_parseLocation());
		        	
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
	
	private ArrayList<HashMap <String, Object> > _parseRegions() throws Exception{
		ArrayList<HashMap <String, Object> > lRegions = new ArrayList<HashMap <String, Object> >();
		
		//move to next token:: JsonToken.START_ARRAY => START_OBJECT
	    JsonToken jsonToken = oParser.nextToken();
	    while (!JsonToken.END_ARRAY.equals(jsonToken)){	
	    	
	
		    lRegions.add(_parseRegion());
		
			//move to next token:: JsonToken.START_OBJECT || JsonToken.END_ARRAY
		    jsonToken = oParser.nextToken();
	    } //END_ARRAY
		
		
		return lRegions;		
	} 
	
	/** serialization to BatchPoints **/
	private BatchPoints _toInfluxDBBatchPoints() {		
		BatchPoints batchPoints = null;
		
		if (hFirstLevel.containsKey(JSON_COMPONENTID) && lRegions!= null && lRegions.size()>0) { 
			/* componentId */		
			LinkedHashMap<String, String> hResourceId=  JSONParserUtils.parseAFC_Id((String) hFirstLevel.get(JSON_COMPONENTID), AFC_ID, AFC_ID_PADDING, "");
			
			for(HashMap<String, Object> hRegion : lRegions) {
				//tags
				LinkedHashMap<String, String> hTags = new LinkedHashMap<String, String>(hResourceId);
				
				if(batchPoints==null & hTags!=null && hTags.containsKey(AFC_SCENARIO)) {
					sDBName = hTags.get(AFC_SCENARIO);
					
					batchPoints = BatchPoints
							.database(sDBName.toUpperCase())
					        .consistency(ConsistencyLevel.ALL)
					        .build();
				}
				
				for (String sTag : INFLUX_TAGS) {
					if (hRegion.containsKey(sTag)) {
						hTags.put(sTag, String.valueOf(hRegion.get(sTag)).trim());
					} else if (hFirstLevel.containsKey(sTag)) {
						hTags.put(sTag, String.valueOf(hFirstLevel.get(sTag)).trim());
					}
				}
				
				//fields => if numeric must be float
				LinkedHashMap<String, Object> hFields = new LinkedHashMap <String, Object>();
				Object oField=null;
				for (String sField : INFLUX_FIELDS) {
					/*
					if (hRegion.containsKey(sField)) {
						hFields.put(sField, hRegion.get(sField));
					} else if (hFirstLevel.containsKey(sField)) {
						hFields.put(sField, hFirstLevel.get(sField));
					}
					*/
					if (hRegion.containsKey(sField)) {
						oField = hRegion.get(sField);
					} else if (hFirstLevel.containsKey(sField)) {
						oField = hFirstLevel.get(sField);
					}
					if (oField!=null) {
						hFields.put(sField,JSONParserUtils.ObjectToFloat(oField));
					}

				}	
				
				Long lEpoch = Long.valueOf(String.valueOf(hRegion.get(JSON_EPOCH)));
				TimeUnit time = TimeUnit.SECONDS;
				lEpoch = time.toMicros(lEpoch);


				batchPoints.point(
						Point.measurement(GlobalParameters.getInstance().getParameter(GlobalParameters.PREFIX_REGION) + INFLUX_MEASUREMENT)
								.tag(hTags)
								.fields(hFields)
								.time(lEpoch, TimeUnit.MICROSECONDS)
								.build()
						);

			}
		}	
		
		return batchPoints;
	}	

}


