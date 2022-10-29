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

import com.afarcloud.thrift.ObservationType;
import com.afarcloud.thrift.Position;
import com.afarcloud.thrift.SensorData;
import com.afarcloud.thrift.SensorType;

import afarcloud.nrdb.config.Constants;
import afarcloud.nrdb.util.DataTypes;
import afarcloud.nrdb.util.GlobalParameters;

public class SerializationToSensorData extends SerializationToObject {

	public SerializationToSensorData() {
		// TODO Auto-generated constructor stub
	}
	
	
	public  List<Object> resultsToSensorData(List<Result> lResults) throws Exception{
    	ArrayList<String> lPrefix =  new ArrayList<String>();
    	lPrefix.add(GlobalParameters.getInstance().getParameter(GlobalParameters.PREFIX_OBSERVATION));
    	
    	return resultsToObject(lResults, lPrefix);
    }
    
	/**
	 * Returns information even if some field could not be transformed correctly.
	 *  In this way, inconsistencies in the system can be identified (sensorType or ObservationType incorrect, etc.)
	 *  
	 * @param sMeasurement
	 * @param lColumns
	 * @param lPoint
	 * @return
	 * @throws Exception
	 */
	@Override
	protected Object _pointToObject(String sMeasurement, List<String> lColumns, List<Object> lPoint) {
		 SensorData oSD = new SensorData();
		 Position oPos = new Position(); 
		 
		 ObservationType eObservation = ObservationType.valueOf(sMeasurement);
		 if (eObservation!=null)
			 oSD.setObserType(eObservation);
		 try {
			for (int i=0; i<lColumns.size(); i++) {
				 switch (lColumns.get(i)){
				 	case Constants.IDB_ENTITY_NAME: 
					 	oSD.setSensorUid((String)lPoint.get(i));
					 	break;
				 	case Constants.IDB_TYPE:
				 		SensorType eSensorType = SensorType.valueOf((String)lPoint.get(i));
				 		if (eSensorType!=null)
				 			oSD.setSensorType(eSensorType);
				 		break;
				 	case Constants.IDB_OM_UOM:
				 		oSD.setUnit((String)lPoint.get(i));
				 		break;
				 	case Constants.IDB_OM_VALUE:
				 		oSD.setValue((Double)lPoint.get(i));
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
				 	case Constants.IDB_TIME:
							oSD.setLastUpdate(DataTypes.stringDateToEpochSecond((String)lPoint.get(i)) );
						break;
				 }
			 }
			 if(oPos.isSetAltitude() || oPos.isSetLongitude() || oPos.isSetLatitude()) {
				 oSD.setSensorPosition(oPos);
			 }
		 }catch (Exception e) {
			// TODO: handle exception
			 return null;
		}
		return oSD;
	}



}
