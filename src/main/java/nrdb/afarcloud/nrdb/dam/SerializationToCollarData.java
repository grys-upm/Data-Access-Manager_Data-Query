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
import java.util.List;

import org.influxdb.dto.QueryResult.Result;

import com.afarcloud.thrift.CollarData;
import com.afarcloud.thrift.Position;

import afarcloud.nrdb.config.Constants;
import afarcloud.nrdb.util.DataTypes;
import afarcloud.nrdb.util.GlobalParameters;

public class SerializationToCollarData extends SerializationToObject {

	public SerializationToCollarData() {
		// TODO Auto-generated constructor stub
	}
	
	
	public  List<Object> resultsToCollarData(List<Result> lResults) throws Exception{
    	ArrayList<String> lPrefix =  new ArrayList<String>();
    	lPrefix.add(GlobalParameters.getInstance().getParameter(GlobalParameters.PREFIX_COLLAR));
    	
    	return resultsToObject(lResults, lPrefix);
    }
    
	/**
	 * Returns information even if some field could not be transformed correctly.
	 *  In this way, inconsistencies in the system can be identified 
	 *  
	 * @param sMeasurement
	 * @param lColumns
	 * @param lPoint
	 * @return
	 * @throws Exception
	 */
	@Override
	protected Object _pointToObject(String sMeasurement, List<String> lColumns, List<Object> lPoint){
		 CollarData oCD = new CollarData();
		 Position oPos = new Position(); 
		 
		 try {
			for (int i=0; i<lColumns.size(); i++) {
				 switch (lColumns.get(i)){
				 	case Constants.IDB_ENTITY_NAME:
				 		oCD.setCollarUid((String)lPoint.get(i));
					 	break;
				 	case Constants.IDB_LOCATION_LONGITUDE:
				 		oPos.setLongitude((Double)lPoint.get(i));
				 		break;
				 	case Constants.IDB_LOCATION_LATITUDE:
				 		oPos.setLatitude((Double)lPoint.get(i));
				 		break;
				 	case Constants.IDB_LOCATION_ALTITUDE:
				 		oPos.setAltitude((Double)lPoint.get(i));
				 		break;
				 	case Constants.IDB_COLLAR_TEMPERATURE:
				 		oCD.setTemperature((Double)lPoint.get(i));			 		
				 		break;
				 	case Constants.IDB_COLLAR_RESOURCEALARM:
				 		oCD.setResourceAlarm(Boolean.valueOf((String)lPoint.get(i)));
				 		break;			 		
				 	case Constants.IDB_COLLAR_ANOMALY_ACTIVITY:
				 		//--------------------
				 		break;			 		
				 	case Constants.IDB_COLLAR_ANOMALY_DISTANCE:
				 		oCD.setDistanceAnomaly(Boolean.valueOf((String)lPoint.get(i)));
				 		break;
				 	case Constants.IDB_COLLAR_ANOMALY_LOCATION:
				 		oCD.setLocationAnomaly(Boolean.valueOf((String)lPoint.get(i)));			 		
				 		break;
				 	case Constants.IDB_COLLAR_ANOMALY_POSITION:
				 		oCD.setPositionAnomaly(Boolean.valueOf((String)lPoint.get(i)));
				 		break;
				 	case Constants.IDB_COLLAR_ANOMALY_TEMPERATURE:
				 		oCD.setTemperatureAnomaly(Boolean.valueOf((String)lPoint.get(i)));
				 		break;
				 		
				 	case Constants.IDB_TIME:
							oCD.setLastUpdate(DataTypes.stringDateToEpochSecond((String)lPoint.get(i)) );
						break;
				 }
			 }
				 if(oPos.isSetAltitude() || oPos.isSetLongitude() || oPos.isSetLatitude()) {
					 oCD.setCollarPosition(oPos);
				 }
		 }catch (Exception e) {
			// TODO: handle exception
			 return null;
		}
		return oCD;
	}



}
