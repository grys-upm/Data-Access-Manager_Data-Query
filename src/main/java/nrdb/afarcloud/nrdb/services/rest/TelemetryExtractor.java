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

package afarcloud.nrdb.services.rest;


import java.io.BufferedReader;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.core.Response;

import org.glassfish.grizzly.http.server.Request;
import org.influxdb.dto.QueryResult.Result;

import com.fasterxml.jackson.core.JsonProcessingException;

import afarcloud.nrdb.config.Constants;
import afarcloud.nrdb.dam.InfluxDataAccess;
import afarcloud.nrdb.dam.SerializationToCSV;
import afarcloud.nrdb.dam.SerializationToJSON;
import afarcloud.nrdb.util.GlobalParameters;


public class TelemetryExtractor {

	/* ************** methods for exploring schema *****************/
    public static Response getSchemaMeasurements(Request request, HashMap<String ,String> hConditions) {	
		String sQuery="";
		try {
			
			sQuery = request.getDecodedRequestURI() +
					( (request.getQueryString()!=null) ?"?" +  request.getQueryString() :"");
			
    		InfluxDataAccess influxDA = new InfluxDataAccess();
    		List<Result> lResults = influxDA.getSchemaMeasurements(hConditions);
		   		
			
			return Response.status(200).entity(SerializationToJSON.resultsToJSONAgrByMeasurements(
							sQuery,
							hConditions,
							lResults,
							GlobalParameters.getInstance().LIST_PREFIX
					 		)
						).build();
		} catch (Exception e) {
				try {
					return Response.status(200).entity(SerializationToJSON.errorToJSON(
								sQuery,
								hConditions,
								"bad formed " + e.getMessage())
								)
							.build();
				} catch (JsonProcessingException e1) {
					return Response.status(500).entity("server error").build();
				}
		}	
    }
   
	/* ************** params validation: **************** */
    
    /********** SensorTelemetry and ObservationsSensor *****************/
	
	public static HashMap<String, String> getHistoricalTelemetryConditions (String sStartTime, String sEndTime, String sEntityNames, String sDevices, String sServices, String sTypes, String sProviders, int nAltitude, String sOrder, boolean csvFormat){

		HashMap<String, String> hConditions = _getTelemetryConditions(sEntityNames, sDevices, sServices,  sTypes, sProviders, sOrder, csvFormat);

		// at last, one filter condition, without altitude and without order
		if (hConditions.isEmpty() ||  hConditions.size()<2) {
			hConditions.put(Constants.SRV_PARAM_LIMIT,
							String.valueOf(GlobalParameters.getInstance().getIntObservations(GlobalParameters.RS_RESPONSE_MAX_OBSERVATIONS, 100))
							);
		}
		
		/**   Limit 500 observations  **/
//		else {
//			hConditions.put(Constants.SRV_PARAM_LIMIT,
//					String.valueOf(GlobalParameters.getInstance().getIntObservations(GlobalParameters.RS_RESPONSE_MAX_OBSERVATIONS, 750))
//					);
//		}
		
		/**   Limit 500 observations  **/
		
		if (nAltitude>0)
			hConditions.put(Constants.SRV_PARAM_ALTITUDE, String.valueOf(nAltitude) );    		
		
		hConditions.putAll(_getIntervalConditions(sStartTime, sEndTime));
		return hConditions;
	}

	 public static HashMap<String, String> getTelemetryConditions(int nLimit, String sEntityNames, String sDevices, String sServices, String sTypes, String sProviders, int nAltitude, String sOrder,  double lCentroid_longitude, double lCentroid_latitude, int nRadius, boolean csvFormat) {    	
	 	 		
	 		HashMap<String, String> hConditions = getTelemetryConditions(nLimit, sEntityNames, sDevices, sServices, sTypes, sProviders, nAltitude, sOrder, csvFormat);	 	
	 		
    		//proximity conditions
    		hConditions.putAll(_getProximityConditions( lCentroid_longitude, lCentroid_latitude,nRadius));
	 		
	 		return hConditions;
		 }

	
	 public static HashMap<String, String> getTelemetryConditions(int nLimit, String sEntityNames, String sDevices, String sServices, String sTypes, String sProviders, int nAltitude, String sOrder, boolean csvFormat) {    	
 	 
 		if (nLimit<1 || nLimit > GlobalParameters.getInstance().getIntObservations(GlobalParameters.RS_RESPONSE_MAX_OBSERVATIONS, 100) ) {
 			nLimit=1;
 		}
		
 		HashMap<String, String> hConditions = _getTelemetryConditions(sEntityNames, sDevices, sServices, sTypes, sProviders, sOrder, csvFormat);
 		
 		hConditions.put(Constants.SRV_PARAM_LIMIT, String.valueOf(nLimit) );
 		
 		if (nAltitude>0)
 			hConditions.put(Constants.SRV_PARAM_ALTITUDE, String.valueOf(nAltitude) );
 		
 		return hConditions;
	 }
	 
 
	 /************** collar conditions ********************/

	 public static HashMap<String, String> getHistoricalCollarConditions(
	   			String sStartTime, String sEndTime, String sEntityNames, String sServices, String sTypes, String sProviders, String sAnomalies, String sAlarm, int nAltitude, String sOrder, boolean csvFormat){

  		HashMap<String, String> hConditions = _getCollarConditions(sEntityNames, sServices, sTypes, sProviders, sAnomalies, sAlarm, sOrder, csvFormat);
		 
 		// at last, one filter condition, without altitude
 		if (hConditions.isEmpty()) {
 			hConditions.put(Constants.SRV_PARAM_LIMIT,
 							String.valueOf(GlobalParameters.getInstance().getIntObservations(GlobalParameters.RS_RESPONSE_MAX_OBSERVATIONS, 100))
 							);
 		}
// 		else {
// 			hConditions.put(Constants.SRV_PARAM_LIMIT,
//						String.valueOf(GlobalParameters.getInstance().getIntObservations(GlobalParameters.RS_RESPONSE_MAX_OBSERVATIONS, 750))
//						);
// 		}
 		
 		if (nAltitude>0)
 			hConditions.put(Constants.SRV_PARAM_ALTITUDE, String.valueOf(nAltitude) );    		
 		
		hConditions.putAll(_getIntervalConditions(sStartTime, sEndTime));
		 
		 return hConditions;
	 }
			 
	 public static HashMap<String, String> getCollarConditions(    			
 			int nLimit, String sEntityNames, String sServices, String sTypes, String sProviders, String sAnomalies, String sAlarm, int nAltitude, String sOrder,  double lCentroid_longitude, double lCentroid_latitude, int nRadius, boolean csvFormat) {    	
 		
  		HashMap<String, String> hConditions = getCollarConditions(nLimit, sEntityNames, sServices, sTypes, sProviders, sAnomalies, sAlarm, nAltitude, sOrder, csvFormat);     		

	 		
 		//proximity conditions
 		hConditions.putAll(_getProximityConditions( lCentroid_longitude, lCentroid_latitude,nRadius));
 		
 		return hConditions;
	 }

	 public static HashMap<String, String> getCollarConditions(    			
    			int nLimit, String sEntityNames, String sServices, String sTypes, String sProviders, String sAnomalies, String sAlarm, int nAltitude, String sOrder, boolean csvFormat) {    	
    		
     		HashMap<String, String> hConditions = _getCollarConditions(sEntityNames, sServices, sTypes, sProviders, sAnomalies, sAlarm, sOrder, csvFormat);     		

     		if (nLimit<1 || nLimit > GlobalParameters.getInstance().getIntObservations(GlobalParameters.RS_RESPONSE_MAX_OBSERVATIONS, 100) ) {
    			nLimit=1;
    		}    		
    		hConditions.put(Constants.SRV_PARAM_LIMIT, String.valueOf(nLimit) );
    		
    		if (nAltitude>0)
    			hConditions.put(Constants.SRV_PARAM_ALTITUDE, String.valueOf(nAltitude) );
    		
    		return hConditions;
    }

	 
    private static String _listAnomaliesToPredicate (String sAnomalies) {
		String sAux="";
		String[] aAux = sAnomalies.split(",");
		for (int i=0; i<aAux.length; i++) {
			if (Constants.SRV_LIST_COLLAR_ANOMALIES.contains(aAux[i].trim())) {
				if(!sAux.isEmpty()) {
					sAux+= " OR ";
				}
				sAux+= " \""+ aAux[i].trim() + "Anomaly\"='true'";
			}
		}
    	
		return sAux;
    }


	 /************** StateVector conditions ********************/
	 public static HashMap<String, String> getHistoricalStateVectorConditions(  			
			 String sStartTime, String sEndTime, String sVehiclesId, String sOrder, boolean csvFormat) {    	
			 	  	     	
			HashMap<String, String> hConditions = _getStateVectorConditions(sVehiclesId, sOrder, csvFormat);
			 	 		
			hConditions.putAll(_getIntervalConditions(sStartTime, sEndTime));
			
			hConditions.put(Constants.SRV_PARAM_LIMIT,
						String.valueOf(GlobalParameters.getInstance().getIntObservations(GlobalParameters.RS_RESPONSE_MAX_OBSERVATIONS, 100))
						);
			
			return hConditions;
		 }

	 public static HashMap<String, String> getStateVectorConditions(    			
			int nLimit, String sVehiclesId, String sOrder,  double lCentroid_longitude, double lCentroid_latitude, int nRadius, boolean csvFormat) {    	
		
 		HashMap<String, String> hConditions = getStateVectorConditions(nLimit, sVehiclesId, sOrder, csvFormat);     		
	 		
		//proximity conditions
		hConditions.putAll(_getProximityConditions( lCentroid_longitude, lCentroid_latitude,nRadius));
		
		return hConditions;
	 }

	 public static HashMap<String, String> getStateVectorConditions(  			
 		int nLimit, String sVehiclesId, String sOrder, boolean csvFormat) {    	
		 HashMap<String, String> hConditions = _getStateVectorConditions(sVehiclesId, sOrder, csvFormat);
		 
		 if (nLimit<1 || nLimit > GlobalParameters.getInstance().getIntObservations(GlobalParameters.RS_RESPONSE_MAX_OBSERVATIONS, 100) ) {
    			nLimit=1;
    		}    		
    	
		hConditions.put(Constants.SRV_PARAM_LIMIT, String.valueOf(nLimit) );
		
  		return hConditions;       		
	 }
    
	 /* ******************** internal ********************** */
	 private static HashMap<String, String> _getIntervalConditions(String sStartTime, String sEndTime){
		 HashMap<String, String> hConditions = new HashMap<String, String>();
		
		 if (sStartTime!=null && !sStartTime.isEmpty())
	 		hConditions.put(Constants.SRV_PARAM_START_TIME, sStartTime);

	 	if (sEndTime!=null && !sEndTime.isEmpty())
	 		hConditions.put(Constants.SRV_PARAM_END_TIME, sEndTime);
	 		
	 	return hConditions;
	 }
	 
    private static HashMap<String,String> _getProximityConditions(double lCentroid_longitude, double lCentroid_latitude, int nRadius){
    	HashMap<String, String> hConditions = new HashMap<String, String>();
    	
 		if ( (lCentroid_latitude!=0 || lCentroid_longitude!=0) && nRadius>0) {
 			hConditions.put(Constants.SRV_PARAM_CENTROID_LONGITUDE, String.valueOf(lCentroid_longitude));
 			hConditions.put(Constants.SRV_PARAM_CENTROID_LATITUDE, String.valueOf(lCentroid_latitude));
 			hConditions.put(Constants.SRV_PARAM_RADIUS, String.valueOf(nRadius));
 		}
 		
 		return hConditions;
    }
	 private static HashMap<String, String> _getTelemetryConditions(String sEntityNames, String sDevices, String sServices, String sTypes, String sProviders, String sOrder, boolean csvFormat) {    	
	 	 			
	 		HashMap<String, String> hConditions = new HashMap<String, String>();
	 		
	 		
	 		if (sEntityNames!=null && !sEntityNames.isEmpty())
	 			hConditions.put(Constants.SRV_PARAM_ENTITY_NAMES, sEntityNames);

	 		if (sDevices!=null && !sDevices.isEmpty())
	 			hConditions.put(Constants.SRV_PARAM_DEVICES, sDevices);
	 		
	 		if (sServices!=null && !sServices.isEmpty())
	 			hConditions.put(Constants.SRV_PARAM_SERVICES, sServices);
	 		
	 		if (sTypes!=null && !sTypes.isEmpty())
	 			hConditions.put(Constants.SRV_PARAM_TYPES, sTypes);
	 		
	 		if (sProviders!=null && !sProviders.isEmpty())
	 			hConditions.put(Constants.SRV_PARAM_PROVIDERS, sProviders);

	 		if (sOrder!=null && Constants.SRV_LIST_ORDER.contains(sOrder.toUpperCase()))
	 			hConditions.put(Constants.SRV_PARAM_ORDER, sOrder.toUpperCase());
	 		
	 		if (csvFormat == true )
	 			hConditions.put(Constants.SRV_PARAM_CSV, String.valueOf(csvFormat));
	 		
	 		return hConditions;
		 }
	 
	 private static HashMap<String, String> _getCollarConditions(    			
 			String sEntityNames, String sServices, String sTypes, String sProviders, String sAnomalies, String sAlarm, String sOrder, boolean csvFormat) {    	
 		
  		HashMap<String, String> hConditions = _getTelemetryConditions(sEntityNames, null, sServices, sTypes, sProviders, sOrder, csvFormat);
 		

 		if (sAnomalies!=null && !sAnomalies.isEmpty()) {
 			String sAnomaliesPredicate=_listAnomaliesToPredicate(sAnomalies);
 			if (!sAnomaliesPredicate.isEmpty()) {
 				hConditions.put(Constants.SRV_PARAM_ANOMALIES, sAnomaliesPredicate);
 			}    			
 		}

 		if(sAlarm!=null && (sAlarm.equalsIgnoreCase("true") || sAlarm.equalsIgnoreCase("false") ) ){
 			hConditions.put(Constants.SRV_PARAM_ALARM, sAlarm);
 		}
 		 		
 		return hConditions;
 }

	 private static HashMap<String, String> _getStateVectorConditions(    			
	 			String sVehiclesId, String sOrder, boolean csvFormat) {    	
	 		
	  		HashMap<String, String> hConditions = new HashMap<String, String>();
	 		if (sVehiclesId!= null && !sVehiclesId.isEmpty()) {
	 			hConditions.put(Constants.SRV_PARAM_VEHICLE_ID, sVehiclesId);
	 		}
	  		
	  		if (sOrder!=null && Constants.SRV_LIST_ORDER.contains(sOrder.toUpperCase()))
	 			hConditions.put(Constants.SRV_PARAM_ORDER, sOrder.toUpperCase());
	  		
	  		if (csvFormat == true )
	 			hConditions.put(Constants.SRV_PARAM_CSV, String.valueOf(csvFormat));
	  			  		
	 		return hConditions;
	 }

   
	 /* *************** results ***********************************/ 
    public static Response getOMTelemetry(Request request, String sMeasurements, HashMap<String ,String> hConditions, boolean bSerializeMeasurements) {	
		String sQuery="";
		System.out.println("getOMTelemetry");
		try {
			
			sQuery = request.getDecodedRequestURI() +
					( (request.getQueryString()!=null) ?"?" +  request.getQueryString() :"");
			
    		InfluxDataAccess influxDA = new InfluxDataAccess();
    		List<Result> lResults = null;
    		String lResultsCSV = null;
    		
    		if(hConditions.containsKey(Constants.SRV_PARAM_CSV) ) {
    			System.out.println("** CSV format required **");
    			lResultsCSV = influxDA.getMeasurementsCSV(sMeasurements, hConditions);
    		}else {
        		lResults = influxDA.getMeasurements(sMeasurements, hConditions);
    		}
    		//add measurements to serialize params
    		if (bSerializeMeasurements && sMeasurements!=null && !sMeasurements.trim().isEmpty()) {
    			hConditions.put(Constants.SRV_PARAM_MEASUREMENTS, sMeasurements);	    			
    		}
    		if(hConditions.containsKey("csv")) {
    			return Response.status(200).entity(SerializationToCSV.resultsToCSV(
						sQuery,
						hConditions,
						lResultsCSV,
						GlobalParameters.getInstance().getParameter(GlobalParameters.PREFIX_OBSERVATION )
				 		)
					).type("application/csv").build();
    		}
			return Response.status(200).entity(SerializationToJSON.resultsToJSONAgrByMeasurements(
							sQuery,
							hConditions,
							lResults,
							GlobalParameters.getInstance().getParameter(GlobalParameters.PREFIX_OBSERVATION )
					 		)
						).build();
		} catch (Exception e) {
				try {
					return Response.status(200).entity(SerializationToJSON.errorToJSON(
								sQuery,
								hConditions,
								"bad formed " + e.getMessage())
								)
							.build();
				} catch (JsonProcessingException e1) {
					return Response.status(500).entity("server error").build();
				}
		}	
    }
    
    
    public static Response getOMTelemetryBySensor(Request request, String sMeasurements, HashMap<String ,String> hConditions, boolean bSerializeMeasurements) {	
		String sQuery="";
		try {
			
			sQuery = request.getDecodedRequestURI() +
					( (request.getQueryString()!=null) ?"?" +  request.getQueryString() :"");
			
			
			
    		InfluxDataAccess influxDA = new InfluxDataAccess();
    		List<Result> lResults = null;
    		String lResultsCSV = null;
    		
    		if(hConditions.containsKey(Constants.SRV_PARAM_CSV) ) {
    			System.out.println("** CSV format required **");
    			lResultsCSV = influxDA.getMeasurementsByTagsCSV(sMeasurements, hConditions, InfluxDataAccess.INFLUX_TAGS_AGR_BY_SENSOR);
    		}else {
    			lResults = influxDA.getMeasurementsByTags(sMeasurements, hConditions, InfluxDataAccess.INFLUX_TAGS_AGR_BY_SENSOR);
    		}
			
    		//add measurements to serialize params
    		if (bSerializeMeasurements && sMeasurements!=null && !sMeasurements.trim().isEmpty()) {
    			hConditions.put(Constants.SRV_PARAM_MEASUREMENTS, sMeasurements);	    			
    		}
    		if(hConditions.containsKey("csv")) {
    			return Response.status(200).entity(SerializationToCSV.resultsToCSV(
						sQuery,
						hConditions,
						lResultsCSV,
						GlobalParameters.getInstance().getParameter(GlobalParameters.PREFIX_OBSERVATION )
				 		)
					).type("application/csv").build();
    		}

			return Response.status(200).entity(SerializationToJSON.resultsToJSONAgrBySensor(
							sQuery,
							hConditions,
							lResults,
							GlobalParameters.getInstance().getParameter(GlobalParameters.PREFIX_OBSERVATION )
					 		)
						).build();
		} catch (Exception e) {
				try {
					return Response.status(200).entity(SerializationToJSON.errorToJSON(
								sQuery,
								hConditions,
								"bad formed " + e.getMessage())
								)
							.build();
				} catch (JsonProcessingException e1) {
					return Response.status(500).entity("server error").build();
				}
		}	
    }

    public static Response getStateVectorsByVehicle(Request request, HashMap<String ,String> hConditions) {	
 		String sQuery="";
 		try {
 			
 			sQuery = request.getDecodedRequestURI() +
 					( (request.getQueryString()!=null) ?"?" +  request.getQueryString() :"");
 			
     		InfluxDataAccess influxDA = new InfluxDataAccess();
    		List<Result> lResults = null;
    		String lResultsCSV = null;
    		
    		if(hConditions.containsKey(Constants.SRV_PARAM_CSV) ) {
    			System.out.println("** CSV format required **");
    			lResultsCSV = influxDA.getMeasurementsByTagsCSV(Constants.IDB_VEHICLE_MEASUREMENT, hConditions, InfluxDataAccess.INFLUX_TAGS_AGR_BY_VEHICLE);
    		}else {
    			lResults = influxDA.getMeasurementsByTags(Constants.IDB_VEHICLE_MEASUREMENT, hConditions, InfluxDataAccess.INFLUX_TAGS_AGR_BY_VEHICLE);
    		}
    		if(hConditions.containsKey("csv")) {
    			return Response.status(200).entity(SerializationToCSV.resultsToCSV(
						sQuery,
						hConditions,
						lResultsCSV,
						GlobalParameters.getInstance().getParameter(GlobalParameters.PREFIX_VEHICLE )
				 		)
					).type("application/csv").build();
    		}
     		
 			return Response.status(200).entity(SerializationToJSON.resultsToJSONAgrBySensor(
 							sQuery,
 							hConditions,
 							lResults,
 							GlobalParameters.getInstance().getParameter(GlobalParameters.PREFIX_VEHICLE )
 					 		)
 						).build();
 		} catch (Exception e) {
 				try {
 					return Response.status(200).entity(SerializationToJSON.errorToJSON(
 								sQuery,
 								hConditions,
 								"bad formed " + e.getMessage())
 								)
 							.build();
 				} catch (JsonProcessingException e1) {
 					return Response.status(500).entity("server error").build();
 				}
 		}	
     }

    public static Response getRegionTelemetry(Request request, HashMap<String ,String> hConditions) {	
		String sQuery="";
		try {
			
			sQuery = request.getDecodedRequestURI() +
					( (request.getQueryString()!=null) ?"?" +  request.getQueryString() :"");
			
    		InfluxDataAccess influxDA = new InfluxDataAccess();
    		List<Result> lResults = influxDA.getRegions(hConditions);
			
			return Response.status(200).entity(SerializationToJSON.resultsToJSONAgrByMeasurements(
							sQuery,
							hConditions,
							lResults,
							GlobalParameters.getInstance().getParameter(GlobalParameters.PREFIX_OBSERVATION )
					 		)
						).build();
		} catch (Exception e) {
				try {
					return Response.status(200).entity(SerializationToJSON.errorToJSON(
								sQuery,
								hConditions,
								"bad formed " + e.getMessage())
								)
							.build();
				} catch (JsonProcessingException e1) {
					return Response.status(500).entity("server error").build();
				}
		}	
    }
    

 }

