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

 *  20200927 static method is not associated with any instance of a class so can't use abstract o superclass
 *  		==> calls to TelemetryExtractor static methods:
 *  			. Historical Data (from influxDB)
 *  			. Last and historical stateVector (from influxDB)
 *  
 *  Last method return queryString to query to MySQL
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
import afarcloud.nrdb.dam.MySQLDataAccess;
import afarcloud.nrdb.dam.SerializationToCollarData;
import afarcloud.nrdb.dam.SerializationToSensorData;
import afarcloud.nrdb.dam.SerializationToStateVector;
import afarcloud.nrdb.util.DataTypes;
import afarcloud.nrdb.util.GlobalParameters;


public class TelemetryExtractorMySQL {
	
	/*
	 * Semantic Query Service
	 * 	- SensorData - LastData
	 */
	public static String getSensorLastData(Position oCentroid, int nRadio) {
		  return _getSensorData(0, oCentroid, nRadio , null, null, null);
	}

	public static String getSensorLastData(List<String> lEntityNames) {
		 String csvEntityName = (lEntityNames!= null && !lEntityNames.isEmpty())?String.join(",", lEntityNames):null;	
		 return _getSensorData(0, null,  0 , csvEntityName, null, null);
	}

	public static String getSensorLastDataBySensor(String sEntityName) {
		  return _getSensorData(0, null, 0, sEntityName, null, null);
	}
	  
  	public static String getSensorLastDataBySensorType(Position oCentroid, int nRadio, SensorType eSensorType) {
  		return _getSensorData(0, oCentroid, nRadio, null, eSensorType, null);
  	}

  	public static String getSensorLastDataBySensorType(List<String> lEntityNames, SensorType eSensorType) {  		
  		String csvEntityName = (lEntityNames!= null && !lEntityNames.isEmpty())?String.join(",", lEntityNames):null;
  		return _getSensorData(0, null, 0, csvEntityName, eSensorType, null);
  	}

    public static String getSensorLastDataByObservationType(Position oCentroid, int nRadio, ObservationType eObservationType) {
    	return _getSensorData(0, oCentroid, nRadio, null, null, eObservationType);    		
    }

    public static String getSensorLastDataByObservationType(List<String> lEntityNames, ObservationType eObservationType) {
    	String csvEntityName = (lEntityNames!= null && !lEntityNames.isEmpty())?String.join(",", lEntityNames):null;    	
    	return _getSensorData(0, null, 0, csvEntityName, null, eObservationType);    		
    }

    
	/*
	 * Semantic Query Service
	 * 	- Collar - LastData
	 */
	
	public static String getCollarLastData(Position oCentroid, int nRadio) {
		  return  _getCollarData(0, oCentroid, nRadio, null);
	}
    
	public static String getCollarLastDataByCollarUid(String sEntityName) {		
		  return _getCollarData(0, null, 0, sEntityName);
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
    private static String _getSensorData(int nLimit, Position oCentroid, int nRadio, String csvEntityName, SensorType eSensorType, ObservationType eObservationType) {	
     	
    	HashMap<String, String> hConditions = _getConditions(nLimit, oCentroid, nRadio);		
		
 		if (csvEntityName!= null && !csvEntityName.isEmpty()) {
 			hConditions.put(Constants.SRV_PARAM_ENTITY_NAMES, csvEntityName);
 		}

    	
		if (eSensorType!= null) {
			hConditions.put(Constants.SRV_PARAM_TYPES, eSensorType.toString());
		}
		
		if(eObservationType!=null) {
			hConditions.put(Constants.SRV_PARAM_OBSERVED_PROPERTIES, eObservationType.toString());
		}
		
		
		return MySQLDataAccess.getQuery(Constants.MySQL_OM_TABLE,
										Constants.MySQL_LIST_OM_COLUMNS,
									    hConditions);
    }
    
    
    /**
     * _getCollarData
     * @param nLimit
     * @param oCentroid
     * @param nRadio
     * @param sEntityName
     * @return
     */
    private static String _getCollarData(int nLimit, Position oCentroid, int nRadio, String sEntityName) {	

     	HashMap<String, String> hConditions = _getConditions(nLimit, oCentroid, nRadio);

     	if (sEntityName!= null && !sEntityName.isEmpty()) {
 			hConditions.put(Constants.SRV_PARAM_COLLAR_IDS, sEntityName);
 		}

 		
		return MySQLDataAccess.getQuery(Constants.MySQL_COLLAR_TABLE,
										Constants.MySQL_LIST_COLLAR_COLUMNS,
										hConditions);
 		
     }
     

    
    /********************** common *****************************/
	 private static HashMap<String, String> _getConditions(int nLimit, Position oCentroid, int nRadio) {    	
	     	HashMap<String, String> hConditions = _getCommonConditions(nLimit, oCentroid, nRadio);
	 			 			 	
	 		return hConditions;
		 }


	 private static HashMap<String, String> _getCommonConditions(int nLimit, Position oCentroid, int nRadio) {    	
	     	HashMap<String, String> hConditions = new HashMap<String, String>();

	     	// mysql => limit clause makes no sense : only last observations stored
	     	if (nLimit>1) {
	     		hConditions.put(Constants.SRV_PARAM_LIMIT, String.valueOf(nLimit) );
	 		}    		
	 		
	 		
			if (nRadio > GlobalParameters.getInstance().getIntObservations(GlobalParameters.RS_RESPONSE_MAX_PROXIMITY_RADIO, 500)) {
				nRadio = GlobalParameters.getInstance().getIntObservations(GlobalParameters.RS_RESPONSE_MAX_PROXIMITY_RADIO, 500);
			}
			
	 		if(oCentroid!=null && nRadio>0) {
	 			hConditions.put(Constants.SRV_PARAM_CENTROID_LATITUDE, String.valueOf(oCentroid.getLatitude()) );
	 			hConditions.put(Constants.SRV_PARAM_CENTROID_LONGITUDE, String.valueOf(oCentroid.getLongitude()) );
	 			hConditions.put(Constants.SRV_PARAM_RADIUS, String.valueOf(nRadio));
	 		}
	 		
	 		return hConditions;
		 }


}

