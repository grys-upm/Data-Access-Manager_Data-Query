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

package afarcloud.nrdb.dam;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult.Result;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;

import afarcloud.nrdb.config.Constants;
import afarcloud.nrdb.util.DataTypes;
import afarcloud.nrdb.util.GeoHashProximity;
import afarcloud.nrdb.util.GlobalParameters;
import ch.hsr.geohash.GeoHash;
import ch.hsr.geohash.WGS84Point;
import ch.hsr.geohash.util.VincentyGeodesy;
import okhttp3.OkHttpClient;


public class InfluxDataAccess {

	private static final Map<String, String> INFLUX_PARAM_TO_COLUMNS;
	static {
		INFLUX_PARAM_TO_COLUMNS = new HashMap<String, String>();

		INFLUX_PARAM_TO_COLUMNS.put(Constants.SRV_PARAM_ENTITY_NAMES, Constants.IDB_ENTITY_NAME);
		INFLUX_PARAM_TO_COLUMNS.put(Constants.SRV_PARAM_SERVICES, Constants.IDB_SERVICE);
		INFLUX_PARAM_TO_COLUMNS.put(Constants.SRV_PARAM_PROVIDERS, Constants.IDB_PROVIDER);
		INFLUX_PARAM_TO_COLUMNS.put(Constants.SRV_PARAM_TYPES, Constants.IDB_TYPE);
		INFLUX_PARAM_TO_COLUMNS.put(Constants.SRV_PARAM_DEVICES, "entityName_dev");				
		INFLUX_PARAM_TO_COLUMNS.put(Constants.SRV_PARAM_ALTITUDE, Constants.IDB_LOCATION_ALTITUDE);
		
		INFLUX_PARAM_TO_COLUMNS.put(Constants.SRV_PARAM_OBSERVED_PROPERTIES, "observedProperty");
		INFLUX_PARAM_TO_COLUMNS.put(Constants.SRV_PARAM_ALARM,Constants.IDB_COLLAR_RESOURCEALARM);
		
		INFLUX_PARAM_TO_COLUMNS.put(Constants.SRV_PARAM_VEHICLE_ID,Constants.IDB_VEHICLE_ID);
	}
	
	// tags to aggregate
	public static final String INFLUX_TAG_DEVICE="entityName_dev";
	public static final String INFLUX_TAG_RESOURCE="entityName";
	public static final String INFLUX_TAGS_AGR_BY_SENSOR = INFLUX_TAG_DEVICE+", " + INFLUX_TAG_RESOURCE;
	
	public static final String INFLUX_TAG_VEHICLE = Constants.IDB_VEHICLE_ID;
	public static final String INFLUX_TAGS_AGR_BY_VEHICLE = INFLUX_TAG_VEHICLE; 
	
	// SPECIAL PARAMS
	public static final List<String> INFLUX_LIST_SPECIAL;	
	
	
	static {
		INFLUX_LIST_SPECIAL = new ArrayList<String> (Arrays.asList(
				new String[] {
				Constants.SRV_PARAM_LIMIT, Constants.SRV_PARAM_START_TIME, Constants.SRV_PARAM_END_TIME, Constants.SRV_PARAM_ALTITUDE
				}
				));
		
	}

	
	private InfluxDB influxDB;
	
	public InfluxDataAccess() {
    	_openConnection(GlobalParameters.getInstance().getParameter(GlobalParameters.AFC_SCENARIO));
		
	}

	public InfluxDataAccess (String sDBName) {
    	_openConnection(sDBName);		
	}
	
	private void _openConnection(String sDBName) {
		/* time limit configuration */
		OkHttpClient.Builder okHttpCB = new OkHttpClient().newBuilder()
				.connectTimeout(40, TimeUnit.SECONDS)
				.readTimeout(120, TimeUnit.SECONDS)
				.writeTimeout(40, TimeUnit.SECONDS);
		
    	influxDB = InfluxDBFactory.connect(
				GlobalParameters.getInstance().getParameter(GlobalParameters.DS_IDB_SERVER)+":"+GlobalParameters.getInstance().getParameter(GlobalParameters.DS_IDB_PORT),
				GlobalParameters.getInstance().getParameter(GlobalParameters.DS_IDB_USER),
				GlobalParameters.getInstance().getParameter(GlobalParameters.DS_IDB_PASSWORD),
				okHttpCB
    			);
    	System.out.println("\n---->db::" + sDBName);
    	influxDB.setDatabase( sDBName );
	}
	
	public List<Result> queryDatabase(String sQuery){
		return influxDB.query(new Query(sQuery)).getResults();
	}
	
	/* methods for exploring schema*/
    public List<Result> getSchemaMeasurements(HashMap<String,String> hConditions) throws Exception {
		
    	String sQuery = "SHOW Measurements";
    	String sWhere = _makeWhere(hConditions);
    	
    	
		if (!sWhere.isEmpty()) {
			sQuery+= " WHERE " + sWhere; 
		}
		
		System.out.println("\n Schema - Measurements:: Query:: \n" +sQuery);
					
		return influxDB.query(new Query(sQuery)).getResults();     	
    }
	
	/* methods for exploring data */
    public List<Result> getMeasurements(String measurements, HashMap<String,String> hConditions) throws Exception {
    	return getMeasurementsByTags(measurements, hConditions, null);
    }	
    public String getMeasurementsCSV(String measurements, HashMap<String,String> hConditions) throws Exception {
    	return getMeasurementsByTagsCSV(measurements, hConditions, null);
    }
    
    public List<Result> getMeasurementsByEntityName(String measurements, HashMap<String,String> hConditions) throws Exception {
    	return getMeasurementsByTags(measurements, hConditions, INFLUX_PARAM_TO_COLUMNS.get(Constants.SRV_PARAM_ENTITY_NAMES));
    }	

    public List<Result> getStateVectors(String measurements, HashMap<String,String> hConditions) throws Exception {
    	return getMeasurementsByTags(measurements, hConditions, null);
    }	
    
    public List<Result> getStateVectorsByVehicleId(String measurements, HashMap<String,String> hConditions) throws Exception {
    	return getMeasurementsByTags(measurements, hConditions, INFLUX_PARAM_TO_COLUMNS.get(Constants.SRV_PARAM_VEHICLE_ID));
    }	
        
    public List<Result> getMeasurementsByTags(String measurements, HashMap<String,String> hConditions, String tags) throws Exception {
    	String sQuery = " SELECT " +	
    			(
    				measurements!=null && measurements.equals(Constants.IDB_COLLAR_MEASUREMENT)? 
    						GlobalParameters.getInstance().getParameter(GlobalParameters.RS_RESPONSE_SELECT_COLLAR):
    						( (measurements!=null && measurements.equals(Constants.IDB_VEHICLE_MEASUREMENT) )?
    								GlobalParameters.getInstance().getParameter(GlobalParameters.RS_RESPONSE_SELECT_VEHICLE):
    								GlobalParameters.getInstance().getParameter(GlobalParameters.RS_RESPONSE_SELECT_OBSERVATION)
    						)
    						
    			) +  " FROM " +
					( 
						(measurements==null || measurements.trim().isEmpty())?
								// "/^"+ GlobalParameters.getInstance().getParameter(GlobalParameters.PREFIX_OBSERVATION ) + "/,"+Constants.IDB_COLLAR_MEASUREMENT :
								"/^"+ GlobalParameters.getInstance().getParameter(GlobalParameters.PREFIX_OBSERVATION ) + "/" :
									(
										measurements.equals(Constants.IDB_COLLAR_MEASUREMENT)? Constants.IDB_COLLAR_MEASUREMENT:
										(
												(measurements.equals(Constants.IDB_VEHICLE_MEASUREMENT) )? 
												"/^"+ GlobalParameters.getInstance().getParameter(GlobalParameters.PREFIX_VEHICLE ) + "/" :
												_listStringToFrom(measurements, GlobalParameters.getInstance().getParameter(GlobalParameters.PREFIX_OBSERVATION ) )
										)
									)
					);
    	String sWhere = _makeWhere(hConditions);
    	
		if (!sWhere.isEmpty()) {
			sQuery+= " WHERE " + sWhere; 
		}
		
		if(tags!=null && !tags.isEmpty()) {
			sQuery+=" GROUP BY " + tags;
		}
		
		//sQuery+=" ORDER BY time DESC";
		sQuery+= _makeOrder(hConditions.get(Constants.SRV_PARAM_ORDER));
		
		//limit clause
		if (hConditions.containsKey(Constants.SRV_PARAM_LIMIT)) {
			sQuery+= " LIMIT " + hConditions.get(Constants.SRV_PARAM_LIMIT);
		}
		

		System.out.println("\n OM:: Query:: \n" +sQuery);
	
		return influxDB.query(new Query(sQuery)).getResults();
    }
 

    public String getMeasurementsByTagsCSV(String measurements, HashMap<String,String> hConditions, String tags) throws Exception {
    	String sQuery = " SELECT " +	
    			(
    				measurements!=null && measurements.equals(Constants.IDB_COLLAR_MEASUREMENT)? 
    						GlobalParameters.getInstance().getParameter(GlobalParameters.RS_RESPONSE_SELECT_COLLAR):
    						( (measurements!=null && measurements.equals(Constants.IDB_VEHICLE_MEASUREMENT) )?
    								GlobalParameters.getInstance().getParameter(GlobalParameters.RS_RESPONSE_SELECT_VEHICLE):
    								GlobalParameters.getInstance().getParameter(GlobalParameters.RS_RESPONSE_SELECT_OBSERVATION)
    						)
    						
    			) +  " FROM " +
					( 
						(measurements==null || measurements.trim().isEmpty())?
								// "/^"+ GlobalParameters.getInstance().getParameter(GlobalParameters.PREFIX_OBSERVATION ) + "/,"+Constants.IDB_COLLAR_MEASUREMENT :
								"/^"+ GlobalParameters.getInstance().getParameter(GlobalParameters.PREFIX_OBSERVATION ) + "/" :
									(
										measurements.equals(Constants.IDB_COLLAR_MEASUREMENT)? Constants.IDB_COLLAR_MEASUREMENT:
										(
												(measurements.equals(Constants.IDB_VEHICLE_MEASUREMENT) )? 
												"/^"+ GlobalParameters.getInstance().getParameter(GlobalParameters.PREFIX_VEHICLE ) + "/" :
												_listStringToFrom(measurements, GlobalParameters.getInstance().getParameter(GlobalParameters.PREFIX_OBSERVATION ) )
										)
									)
					);
    	String sWhere = _makeWhere(hConditions);
    	
		if (!sWhere.isEmpty()) {
			sQuery+= " WHERE " + sWhere; 
		}
		
		if(tags!=null && !tags.isEmpty()) {
			sQuery+=" GROUP BY " + tags;
		}
		
		//sQuery+=" ORDER BY time DESC";
		sQuery+= _makeOrder(hConditions.get(Constants.SRV_PARAM_ORDER));
		
		//limit clause
		if (hConditions.containsKey(Constants.SRV_PARAM_LIMIT)) {
			sQuery+= " LIMIT " + hConditions.get(Constants.SRV_PARAM_LIMIT);
		}
		

		System.out.println("\n OM:: Query:: \n" +sQuery);
		
		
		
		String command =
				"curl -H \"Accept: application/csv\" -G '"+GlobalParameters.getInstance().getParameter(GlobalParameters.DS_IDB_SERVER)+":"+GlobalParameters.getInstance().getParameter(GlobalParameters.DS_IDB_PORT)+"/query?db="+GlobalParameters.getInstance().getParameter(GlobalParameters.AFC_SCENARIO)+"' --data-urlencode \"u=*******\" --data-urlencode \"p=*******\" --data-urlencode 'q="+sQuery+"'";
		System.out.println(command);
		
		@SuppressWarnings("resource")
		AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        AsyncHttpClient.BoundRequestBuilder r = asyncHttpClient.preparePost(GlobalParameters.getInstance().getParameter(GlobalParameters.DS_IDB_SERVER)+":"+GlobalParameters.getInstance().getParameter(GlobalParameters.DS_IDB_PORT)+"/query?db="+GlobalParameters.getInstance().getParameter(GlobalParameters.AFC_SCENARIO));
        r.addHeader("Accept", "application/csv");
        r.addQueryParam("u", GlobalParameters.getInstance().getParameter(GlobalParameters.DS_IDB_USER));
        r.addQueryParam("p", GlobalParameters.getInstance().getParameter(GlobalParameters.DS_IDB_PASSWORD));
        r.addQueryParam("q", sQuery);
        Future<Response> f = r.execute();

        Response res = f.get();

        System.out.println(res.getStatusCode() + ": " + res.getStatusText());
//        System.out.println(res.getResponseBody());

		return res.getResponseBody();
    }
    
    public List<Result> getRegions(HashMap<String,String> hConditions) throws Exception {
		
    	String sQuery = "SELECT "+ GlobalParameters.getInstance().getParameter(GlobalParameters.RS_RESPONSE_SELECT_REGION) +" FROM " + Constants.IDB_REGION_MEASUREMENT;
    	String sWhere = _makeWhere(hConditions);
    	
		if (!sWhere.isEmpty()) {
			sQuery+= " WHERE " + sWhere; 
		}
		
		//sQuery+=" ORDER BY time DESC";
		sQuery+= _makeOrder(hConditions.get(Constants.SRV_PARAM_ORDER));
		
		//limit clause
		if (hConditions.containsKey(Constants.SRV_PARAM_LIMIT)) {
			sQuery+= " LIMIT " + hConditions.get(Constants.SRV_PARAM_LIMIT);
		}
		

		System.out.println("\n Region:: Query:: \n" +sQuery);
					
		return influxDB.query(new Query(sQuery)).getResults();     	
    }

    private String _makeOrder(String sOrder) {
    	return  " ORDER BY time " +
    			(
    					(sOrder!=null && Constants.SRV_LIST_ORDER.contains(sOrder))? sOrder :"DESC"
    			);
    }
    
    private String _makeWhere(HashMap<String,String> hConditions) throws Exception {
					
		String sWhere="";
		String sAux;
		String sParam;
		
		if (hConditions!=null && !hConditions.isEmpty()) {	

			for (Entry<String, String> cond : hConditions.entrySet()) {
				sAux="";
				
				// list
				sParam = cond.getKey();
				if (INFLUX_PARAM_TO_COLUMNS.containsKey(sParam) && !INFLUX_LIST_SPECIAL.contains(sParam)) {
					sAux = (Constants.SRV_LIST_PARAMS.contains(sParam))?
							_listStringToPredicate(INFLUX_PARAM_TO_COLUMNS.get(sParam), cond.getValue()) :							 
							 "\""+ INFLUX_PARAM_TO_COLUMNS.get(sParam) + "\"='" + cond.getValue() +"'" 
							 ;
					if (!sAux.isEmpty()) {
							if (!sWhere.isEmpty()) {
								sWhere+= " AND ";
							}
							sWhere+=" (" + sAux + ")"; 					
					}
					
				}else if(Constants.SRV_LIST_PREDICATES.contains(sParam)) {
					if (!sWhere.isEmpty()) {
						sWhere+= " AND ";
					}
					sWhere+=" (" + cond.getValue() + ")"; 					
				}
								
			}
				

			// altitude condition
			if (hConditions.containsKey(Constants.SRV_PARAM_ALTITUDE)) {
				sAux = "altitude>=" + hConditions.get(Constants.SRV_PARAM_ALTITUDE);
				if (!sWhere.isEmpty()) {
					sWhere+= " AND ";
				}
				sWhere+=" (" + sAux + ")";
			}
	
			// temporal conditions
			if (hConditions.containsKey(Constants.SRV_PARAM_START_TIME)) {
				sAux = "time>='" + DataTypes.stringDateToTimestamp( hConditions.get(Constants.SRV_PARAM_START_TIME) ) + "'";
				if (hConditions.containsKey(Constants.SRV_PARAM_END_TIME)) {
					sAux += " AND time<'" + DataTypes.stringDateToTimestamp(hConditions.get(Constants.SRV_PARAM_END_TIME) ) + "'";
				}
				if (!sWhere.isEmpty()) {
					sWhere+= " AND ";
				}
				sWhere+=" (" + sAux + ")"; 					
			}
			
			/* position condition
			 * a - only centroid => geohash based predicate
			 * b - centroid + radio => geohas proximity + georaptor based predicate
			 */
			if (hConditions.containsKey(Constants.SRV_PARAM_CENTROID_LATITUDE) && hConditions.containsKey(Constants.SRV_PARAM_CENTROID_LONGITUDE)) {
				//sAux = _makeWhereGeospatialRelation_GeoHash(hConditions);
				sAux = _makeWhereGeospatialRelation(hConditions);
				if (!sAux.isEmpty()) {
					if (!sWhere.isEmpty()) {
						sWhere+= " AND ";
					}
					sWhere+= sAux ;
				}
			}
			
    	}
		return sWhere;
    }

    private String _makeWhereGeospatialRelation_GeoHash(HashMap<String,String> hConditions) throws Exception {
		
		String sWhere="";
		String sAux;
		
		if (hConditions!=null && !hConditions.isEmpty()) {	

			/* position condition
			 * a - only centroid => geohash based predicate
			 * b - centroid + radio => geohas proximity + georaptor based predicate
			 */
			if (hConditions.containsKey(Constants.SRV_PARAM_CENTROID_LATITUDE) && hConditions.containsKey(Constants.SRV_PARAM_CENTROID_LONGITUDE)) {
				sAux="";
				
				if (!hConditions.containsKey(Constants.SRV_PARAM_RADIUS)) {					
					String sGeo = GeoHash.geoHashStringWithCharacterPrecision(
							Double.valueOf(hConditions.get(Constants.SRV_PARAM_CENTROID_LATITUDE)).doubleValue(),
							Double.valueOf(hConditions.get(Constants.SRV_PARAM_CENTROID_LONGITUDE)).doubleValue(),
							Constants.IDB_GEOHASH_PRECISION);					
				
					sAux=" ( \""+Constants.IDB_LOCATION_GEOHASH + "\"='"+sGeo +"')"; 					
				}else {
					/* compute geohash proximity */
					int nRadio = Integer.valueOf(hConditions.get(Constants.SRV_PARAM_RADIUS)).intValue();
					int nProximityPrecision = Constants.IDB_QUERY_GEOHASH_PROXIMITY_PRECISION;
					int nGeoraptorMax = Constants.IDB_QUERY_GEORAPTOR_MAX;
					if(nRadio<=100) {
						nProximityPrecision+= Constants.IDB_QUERY_GEOHASH_PROXIMITY_FACTOR_100;
						nGeoraptorMax+= Constants.IDB_QUERY_GEOHASH_GEORAPTOR_MAX_100;
					}else if(nRadio<=500) {
						nProximityPrecision+= Constants.IDB_QUERY_GEOHASH_PROXIMITY_FACTOR_500;
						nGeoraptorMax+= Constants.IDB_QUERY_GEOHASH_GEORAPTOR_MAX_500;
					}
					
					
					List<String>lGeo = GeoHashProximity.getGeohashCircle(
							new WGS84Point(Double.valueOf(hConditions.get(Constants.SRV_PARAM_CENTROID_LATITUDE)).doubleValue(),
									Double.valueOf(hConditions.get(Constants.SRV_PARAM_CENTROID_LONGITUDE)).doubleValue()),
									nRadio,
									nProximityPrecision);
					
					/*reduce by georaptor */					
					lGeo = GeoHashProximity.compress(lGeo, Constants.IDB_QUERY_GEORAPTOR_MIN, nGeoraptorMax);
					/* make predicate */
					for(int i=0; i<lGeo.size();i++) {
						lGeo.add(i, "\""+ Constants.IDB_LOCATION_GEOHASH +"\"=~/^"+lGeo.get(i)+"/");
						/* al insertar en la misma posición, el elemento actual pasa a ser el siguiente */
						lGeo.remove(i+1);
					}
					sAux = String.join(" OR ", lGeo);
				}
				
				sWhere=" (" + sAux + ")"; 					
				
			}
			
    	}
		return sWhere;
    }

    private String _makeWhereGeospatialRelation(HashMap<String,String> hConditions){
		
		String sWhere="";
		String sAux;
		
		if (hConditions!=null && !hConditions.isEmpty()) {	

	
			if (hConditions.containsKey(Constants.SRV_PARAM_CENTROID_LATITUDE) && hConditions.containsKey(Constants.SRV_PARAM_CENTROID_LONGITUDE)) {
				sAux="";
				
				if (!hConditions.containsKey(Constants.SRV_PARAM_RADIUS)) {					
					String sGeo = GeoHash.geoHashStringWithCharacterPrecision(
							Double.valueOf(hConditions.get(Constants.SRV_PARAM_CENTROID_LATITUDE)).doubleValue(),
							Double.valueOf(hConditions.get(Constants.SRV_PARAM_CENTROID_LONGITUDE)).doubleValue(),
							Constants.IDB_GEOHASH_PRECISION);					
				
					sAux=" ( \""+Constants.IDB_LOCATION_GEOHASH + "\"='"+sGeo +"')"; 					
				}else {
					/* compute boundingBox */
					WGS84Point oCenter = new WGS84Point(
							Double.valueOf(hConditions.get(Constants.SRV_PARAM_CENTROID_LATITUDE)).doubleValue(),
							Double.valueOf(hConditions.get(Constants.SRV_PARAM_CENTROID_LONGITUDE)).doubleValue()
							);
					int nRadio = Integer.valueOf(hConditions.get(Constants.SRV_PARAM_RADIUS)).intValue();
					
					WGS84Point northEastCorner = VincentyGeodesy.moveInDirection(VincentyGeodesy.moveInDirection(oCenter, 0, nRadio), 90, nRadio);
					WGS84Point southWestCorner = VincentyGeodesy.moveInDirection(VincentyGeodesy.moveInDirection(oCenter, 180, nRadio), 270, nRadio);
					/*
					sAux =	"\"latitude\" >= " + southWestCorner.getLatitude() +
							" AND \"latitude\" <= " + northEastCorner.getLatitude() + 
							" AND \"longitude\" >= " + southWestCorner.getLongitude() +
							" AND \"longitude\" <= " + northEastCorner.getLongitude() 							
							;
					*/
					
					sAux =	"\"" + Constants.IDB_LOCATION_LATITUDE + "\" >= " + southWestCorner.getLatitude() +
							" AND \"" + Constants.IDB_LOCATION_LATITUDE + "\" <= " + northEastCorner.getLatitude() + 
							" AND \"" + Constants.IDB_LOCATION_LONGITUDE + "\" >= " + southWestCorner.getLongitude() +
							" AND \"" + Constants.IDB_LOCATION_LONGITUDE + "\" <= " + northEastCorner.getLongitude() 							
							;
	
				}
				
				sWhere=" (" + sAux + ")"; 					
				
			}
			
    	}
		return sWhere;
    }

    private static String _listStringToPredicate (String sField, String sCondition) {
		String sAux="";
		String[] aAux = sCondition.split(",");
		for (int i=0; i<aAux.length; i++) {
			if(!sAux.isEmpty()) {
				sAux+= " OR ";
			}
			sAux+= " \""+ sField + "\"='" + aAux[i].trim() +"'"; 
		}
    	
		return sAux;
    }
    
    private static String _listStringToFrom(String sCondition, String sPrefix) {
		String sAux="";
		String[] aAux = sCondition.split(",");
		for (int i=0; i<aAux.length; i++) {
			if(!sAux.isEmpty()) {
				sAux+= ",";
			}
			sAux+= "\""+ sPrefix + aAux[i].trim().replace("\"", "\\\"")+ "\""; 
		}
    	
		return sAux;
    }

    	
}

