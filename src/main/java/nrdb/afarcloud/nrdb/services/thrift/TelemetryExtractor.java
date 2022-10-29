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
package afarcloud.nrdb.services.thrift;


import java.util.HashMap;
import java.util.List;

import org.influxdb.dto.QueryResult.Result;

import com.afarcloud.thrift.CollarData;
import com.afarcloud.thrift.ObservationType;
import com.afarcloud.thrift.Position;
import com.afarcloud.thrift.SensorData;
import com.afarcloud.thrift.SensorType;
import com.afarcloud.thrift.StateVector;

import afarcloud.nrdb.config.Constants;
import afarcloud.nrdb.dam.InfluxDataAccess;
import afarcloud.nrdb.dam.SerializationToCollarData;
import afarcloud.nrdb.dam.SerializationToSensorData;
import afarcloud.nrdb.dam.SerializationToStateVector;
import afarcloud.nrdb.util.DataTypes;
import afarcloud.nrdb.util.GlobalParameters;


public class TelemetryExtractor {
	
	/*
	 * Semantic Query Service
	 * 	- SensorData - LastData
	 */
	public static List<SensorData> getSensorLastData(Position oCentroid, int nRadio) {
		  return _getSensorData(1, oCentroid, nRadio , null, null, null, 0, 0);
	}

	public static List<SensorData> getSensorLastData(List<String> lEntityNames) {
		 String csvEntityName = (lEntityNames!= null && !lEntityNames.isEmpty())?String.join(",", lEntityNames):null;	
		 return _getSensorData(1, null,  0 , csvEntityName, null, null, 0, 0);
	}

	public static List<SensorData> getSensorLastDataBySensor(String sEntityName) {
		  return _getSensorData(1, null, 0, sEntityName, null, null, 0, 0);
	}
	  
  	public static List<SensorData> getSensorLastDataBySensorType(Position oCentroid, int nRadio, SensorType eSensorType) {
  		return _getSensorData(1, oCentroid, nRadio, null, eSensorType, null, 0, 0);
  	}

  	public static List<SensorData> getSensorLastDataBySensorType(List<String> lEntityNames, SensorType eSensorType) {  		
  		String csvEntityName = (lEntityNames!= null && !lEntityNames.isEmpty())?String.join(",", lEntityNames):null;
  		return _getSensorData(1, null, 0, csvEntityName, eSensorType, null, 0, 0);
  	}

    public static List<SensorData> getSensorLastDataByObservationType(Position oCentroid, int nRadio, ObservationType eObservationType) {
    	return _getSensorData(1, oCentroid, nRadio, null, null, eObservationType, 0, 0);    		
    }

    public static List<SensorData> getSensorLastDataByObservationType(List<String> lEntityNames, ObservationType eObservationType) {
    	String csvEntityName = (lEntityNames!= null && !lEntityNames.isEmpty())?String.join(",", lEntityNames):null;    	
    	return _getSensorData(1, null, 0, csvEntityName, null, eObservationType, 0, 0);    		
    }

	/*
	 * 	- SensorData - HistoricalData
	 */
    
	public static List<SensorData> getSensorHistoricalData(Position oCentroid, int nRadio, long lStartTime, long lEndTime)  {
		return _getSensorData(0, oCentroid, nRadio , null, null, null, lStartTime, lEndTime);
	}

	public static List<SensorData> getSensorHistoricalData(List<String> lEntityNames, long lStartTime, long lEndTime)  {
		String csvEntityName = (lEntityNames!= null && !lEntityNames.isEmpty())?String.join(",", lEntityNames):null;
		return _getSensorData(0, null, 0, csvEntityName, null, null, lStartTime, lEndTime);
	}

	public static List<SensorData> getSensorHistoricalDataBySensorUid(String sEntityName, long lStartTime, long lEndTime) {
		return _getSensorData(GlobalParameters.getInstance().getIntObservations(GlobalParameters.RS_RESPONSE_MAX_OBSERVATIONS_BY_UID, 500),
								null, 0, sEntityName, null, null, lStartTime, lEndTime);
	}

	public static List<SensorData> getSensorHistoricalDataBySensorType(Position oCentroid, int nRadio, SensorType eSensorType, long lStartTime, long lEndTime){
		return _getSensorData(0, oCentroid, nRadio, null, eSensorType, null, lStartTime, lEndTime);
	}

	public static List<SensorData> getSensorHistoricalDataBySensorType(List<String> lEntityNames, SensorType eSensorType, long lStartTime, long lEndTime){
		String csvEntityName = (lEntityNames!= null && !lEntityNames.isEmpty())?String.join(",", lEntityNames):null;		
		return _getSensorData(0, null, 0, csvEntityName, eSensorType, null, lStartTime, lEndTime);
	}

	public static List<SensorData> getSensorHistoricalDataByObservationType(Position oCentroid, int nRadio, ObservationType eObservationType, long lStartTime, long lEndTime){
		return _getSensorData(0, oCentroid, nRadio, null, null, eObservationType, lStartTime, lEndTime);
	}

	public static List<SensorData> getSensorHistoricalDataByObservationType(List<String> lEntityNames, ObservationType eObservationType, long lStartTime, long lEndTime){
		String csvEntityName = (lEntityNames!= null && !lEntityNames.isEmpty())?String.join(",", lEntityNames):null;		
		return _getSensorData(0, null, 0, csvEntityName, null, eObservationType, lStartTime, lEndTime);
	}

    
	/*
	 * Semantic Query Service
	 * 	- Collar - LastData
	 */
	public static List<CollarData> getCollarLastData(Position oCentroid, int nRadio) {
		  return  _getCollarData(1, oCentroid, nRadio, null, 0, 0);
	}
    
	public static List<CollarData> getCollarLastDataByCollarUid(String sEntityName) {
		
		  return _getCollarData(GlobalParameters.getInstance().getIntObservations(GlobalParameters.RS_RESPONSE_MAX_OBSERVATIONS_BY_UID, 500),
				  				null, 0, sEntityName, 0, 0);
	}
	
	/*
	 * 	- Collar - HistoricalData
	 */    
	public static List<CollarData> getCollarHistoricalData(Position oCentroid, int nRadio, long lStartTime, long lEndTime) {
		return  _getCollarData(0, oCentroid, nRadio, null, lStartTime, lEndTime);
	}


	public static List<CollarData> getCollarHistoricalDataByCollarUid(String sEntityName, long lStartTime, long lEndTime) {
		  return _getCollarData(GlobalParameters.getInstance().getIntObservations(GlobalParameters.RS_RESPONSE_MAX_OBSERVATIONS_BY_UID, 500),
				  				null, 0, sEntityName, lStartTime, lEndTime);
	}
	
    
	/*
	 * Semantic Query Service
	 * 	- Vehicle - LastData
	 */
	public static List<StateVector> getVehicleStateVector(Position oCentroid, int nRadio) {
		return _getVehicleStateVector(1, oCentroid, nRadio, 0, 0, 0);
	}
    
	public static List<StateVector> getVehicleStateVectorByVehicle(int nVehicleId) {
		return _getVehicleStateVector(GlobalParameters.getInstance().getIntObservations(GlobalParameters.RS_RESPONSE_MAX_OBSERVATIONS_BY_UID, 500),
									null, 0, nVehicleId, 0, 0);
	}

	/*
	 * 	- Vehicle - HistoricalData
	 */

	public static List<StateVector> getVehicleHistoricalStateVectors(Position oCentroid, int nRadio,	long lStartTime, long lEndTime) {
		return _getVehicleStateVector(0, oCentroid, nRadio, 0, lStartTime, lEndTime); 
	}

	public static List<StateVector> getVehicleHistoricalStateVectorByVehicleId(int nVehicleId, long lStartTime, long lEndTime){
		return _getVehicleStateVector(0, null, 0, nVehicleId, lStartTime, lEndTime);
	}
	
    
    /************************** protected * **********************/
	
    /**
     * _getSensorData
     * @param nLimit
     * @param oCentroid
     * @param nRadio
     * @param csvEntityName => comma separated values
     * @param eSensorType
     * @param eObservationType
     * @return
     */
    private static List<SensorData> _getSensorData(int nLimit, Position oCentroid, int nRadio, String csvEntityName, SensorType eSensorType, ObservationType eObservationType, long lStartTime, long lEndTime) {	
		List<Object> lSensorData = null;
     	HashMap<String, String> hConditions = _getConditions(nLimit, oCentroid, nRadio, csvEntityName, lStartTime, lEndTime);
		String sMeasurements = null;
		
		
		if (eSensorType!= null) {
			hConditions.put(Constants.SRV_PARAM_TYPES, eSensorType.toString());
		}
		
		if(eObservationType!=null) {
			sMeasurements = eObservationType.toString();
		}
		
		try {
			
    		InfluxDataAccess influxDA = new InfluxDataAccess();
    		SerializationToSensorData oSerializationSD = new SerializationToSensorData();
    		
    		List<Result> lResults = influxDA.getMeasurementsByEntityName(sMeasurements, hConditions);
    		lSensorData= oSerializationSD.resultsToSensorData(lResults);
		} catch (Exception e) {
			// do nothing
			lSensorData=null;
		}	
		
		return (List<SensorData>)(Object) lSensorData;
    }
    
    
    /**
     * _getCollarData
     * @param nLimit
     * @param oCentroid
     * @param nRadio
     * @param sEntityName
     * @return
     */
    private static List<CollarData> _getCollarData(int nLimit, Position oCentroid, int nRadio, String sEntityName, long lStartTime, long lEndTime) {	
 		List<Object> lCollarData = null;
     	HashMap<String, String> hConditions = _getConditions(nLimit, oCentroid, nRadio, sEntityName, lStartTime, lEndTime);
 		String sMeasurements = Constants.IDB_COLLAR_MEASUREMENT;
 		
 		
 		try {
 			
     		InfluxDataAccess influxDA = new InfluxDataAccess();
     		List<Result> lResults = influxDA.getMeasurementsByEntityName(sMeasurements, hConditions);
     		
     		SerializationToCollarData oSerializationCD = new SerializationToCollarData();     		
     		lCollarData= oSerializationCD.resultsToCollarData(lResults);
 		} catch (Exception e) {
 			// do nothing
 			lCollarData=null;
 		}	
 		
 		return (List<CollarData>)(Object) lCollarData;
     }
     
    /**
     * _getVehicleStateVector
     * @param nLimit
     * @param oCentroid
     * @param nRadio
     * @param sEntityName
     * @return
     */
    private static List<StateVector> _getVehicleStateVector(int nLimit, Position oCentroid, int nRadio, int nVehicleId, long lStartTime, long lEndTime) {	
 		List<Object> lStateVector = null;
     	HashMap<String, String> hConditions = _getVehicleConditions(nLimit, oCentroid, nRadio, nVehicleId, lStartTime, lEndTime);
 		String sMeasurements = Constants.IDB_VEHICLE_MEASUREMENT;
 		
 		
 		try {
 			
     		InfluxDataAccess influxDA = new InfluxDataAccess();
     		List<Result> lResults = influxDA.getStateVectorsByVehicleId(sMeasurements, hConditions);
     		
     		SerializationToStateVector oSerializationSV = new SerializationToStateVector();     		
     		lStateVector= oSerializationSV.resultsToStateVector(lResults);
 		} catch (Exception e) {
 			// do nothing 			
 			e.printStackTrace();
 			lStateVector=null;
 		}	
 		
 		return (List<StateVector>)(Object) lStateVector;
     }

    
    /********************** common *****************************/
	 private static HashMap<String, String> _getConditions(int nLimit, Position oCentroid, int nRadio, String sEntityName, long lStartTime, long lEndTime) {    	
	     	HashMap<String, String> hConditions = _getCommonConditions(nLimit, oCentroid, nRadio, lStartTime, lEndTime);
	 		
	 		if (sEntityName!= null && !sEntityName.isEmpty()) {
	 			hConditions.put(Constants.SRV_PARAM_ENTITY_NAMES, sEntityName);
	 		}
	 			 		
	 		return hConditions;
		 }

	 private static HashMap<String, String> _getVehicleConditions(int nLimit, Position oCentroid, int nRadio, int nVehicleId, long lStartTime, long lEndTime) {    	
	     	HashMap<String, String> hConditions = _getCommonConditions(nLimit, oCentroid, nRadio, lStartTime, lEndTime);
	 		
	 		if (nVehicleId!= 0 ) {
	 			hConditions.put(Constants.SRV_PARAM_VEHICLE_ID, String.valueOf(nVehicleId));
	 		}
	 			 		
	 		return hConditions;
		 }


	 private static HashMap<String, String> _getCommonConditions(int nLimit, Position oCentroid, int nRadio,  long lStartTime, long lEndTime) {    	
	     	HashMap<String, String> hConditions = new HashMap<String, String>();

	     	if (nLimit<1) {
	 			nLimit=GlobalParameters.getInstance().getIntObservations(GlobalParameters.RS_RESPONSE_MAX_OBSERVATIONS, 100);
	 		}    		
	 		
	     	hConditions.put(Constants.SRV_PARAM_LIMIT, String.valueOf(nLimit) );
	 		
			if (nRadio > GlobalParameters.getInstance().getIntObservations(GlobalParameters.RS_RESPONSE_MAX_PROXIMITY_RADIO, 500)) {
				nRadio = GlobalParameters.getInstance().getIntObservations(GlobalParameters.RS_RESPONSE_MAX_PROXIMITY_RADIO, 500);
			}
			
	 		if(oCentroid!=null && nRadio>0) {
	 			hConditions.put(Constants.SRV_PARAM_CENTROID_LATITUDE, String.valueOf(oCentroid.getLatitude()) );
	 			hConditions.put(Constants.SRV_PARAM_CENTROID_LONGITUDE, String.valueOf(oCentroid.getLongitude()) );
	 			hConditions.put(Constants.SRV_PARAM_RADIUS, String.valueOf(nRadio));
	 		}
	 		
	 		if (lStartTime>0) {
	 			hConditions.put(Constants.SRV_PARAM_START_TIME, DataTypes.epochSecondToStringDate(lStartTime));
	 			if (lEndTime>0) {
	 				hConditions.put(Constants.SRV_PARAM_END_TIME, DataTypes.epochSecondToStringDate(lEndTime));
	 			}	 			
	 		}
	 		
	 		return hConditions;
		 }


}

