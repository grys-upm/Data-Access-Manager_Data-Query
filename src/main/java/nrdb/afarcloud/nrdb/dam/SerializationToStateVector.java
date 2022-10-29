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

import com.afarcloud.thrift.Battery;
import com.afarcloud.thrift.Orientation;
import com.afarcloud.thrift.Position;
import com.afarcloud.thrift.StateVector;

import afarcloud.nrdb.config.Constants;
import afarcloud.nrdb.util.DataTypes;
import afarcloud.nrdb.util.GlobalParameters;

public class SerializationToStateVector extends SerializationToObject {

	public SerializationToStateVector() {
		// TODO Auto-generated constructor stub
	}
	
	
	public  List<Object> resultsToStateVector(List<Result> lResults) throws Exception{
    	ArrayList<String> lPrefix =  new ArrayList<String>();
    	lPrefix.add(GlobalParameters.getInstance().getParameter(GlobalParameters.PREFIX_VEHICLE));
    	
    	return resultsToObject(lResults, lPrefix);
    }
    
	/**
	 * Returns information even if some field could not be transformed correctly.
	 *  
	 * @param sMeasurement
	 * @param lColumns
	 * @param lPoint
	 * @return
	 * @throws Exception
	 */
	@Override
	protected Object _pointToObject(String sMeasurement, List<String> lColumns, List<Object> lPoint) {
		 StateVector oSV = new StateVector();
		 Position oPos = new Position(); 
		 Orientation oOr = new Orientation();
		 Battery oBatt = new Battery();
		try {
			for (int i=0; i<lColumns.size(); i++) {
				 switch (lColumns.get(i)){
				 	case Constants.IDB_VEHICLE_ID: 
					 	oSV.setVehicleId(Integer.parseInt((String)lPoint.get(i)));
					 	break;
	
				 	case Constants.IDB_LOCATION_LONGITUDE:
				 		if(lPoint.get(i)!=null) {
				 			oPos.setLongitude((Double)lPoint.get(i));
				 		}
				 		break;
				 	case Constants.IDB_LOCATION_LATITUDE:
				 		if(lPoint.get(i)!=null) {
				 			oPos.setLatitude((Double)lPoint.get(i));
				 		}
				 		break;
				 	case Constants.IDB_LOCATION_ALTITUDE:				 		
				 		if(lPoint.get(i)!=null) {
				 			oPos.setAltitude((Double)lPoint.get(i));
				 		}				 		
				 		break;
	
				 	case Constants.IDB_VEHICLE_ORIENTATION_ROLL:
				 		if(lPoint.get(i)!=null) {
				 			oOr.setRoll((double)lPoint.get(i));
				 		}
				 		break;
				 	case Constants.IDB_VEHICLE_ORIENTATION_PITCH:
				 		if(lPoint.get(i)!=null) {
				 			oOr.setPitch((double)lPoint.get(i));
				 		}
				 		break;
				 	case Constants.IDB_VEHICLE_ORIENTATION_YAW:
				 		if(lPoint.get(i)!=null) {
				 			oOr.setYaw((double)lPoint.get(i));
				 		}
				 		break;
	
				 	case Constants.IDB_VEHICLE_BATTERY_CAPACITY:
				 		if(lPoint.get(i)!=null) {
				 			oBatt.setBatteryCapacity((double)lPoint.get(i));
				 		}
				 		break;
				 	case Constants.IDB_VEHICLE_BATTERY_PERCENTAGE:
				 		if(lPoint.get(i)!=null) {
				 			oBatt.setBatteryPercentage((double)lPoint.get(i));
				 		}
				 		break;
				 	
				 	case Constants.IDB_VEHICLE_LINEAR_SPEED:
				 		if(lPoint.get(i)!=null) {
				 			oSV.setLinearSpeed((double)lPoint.get(i));
				 		}
				 		break;
				 	case Constants.IDB_TIME:
							oSV.setLastUpdate(DataTypes.stringDateToEpochSecond((String)lPoint.get(i)) );
						break;
				 }
				 
			 }
			 if(oPos.isSetAltitude() || oPos.isSetLongitude() || oPos.isSetLatitude()) {
				 oSV.setPosition(oPos);
			 }
			 if(oOr.isSetPitch() || oOr.isSetRoll() || oOr.isSetYaw()) {
				 oSV.setOrientation(oOr);
			 }
			 if(oBatt.isSetBatteryCapacity() || oBatt.isSetBatteryPercentage()) {
				 oSV.setBattery(oBatt);
			 }
		}catch (Exception e) {
			// TODO: handle exception
			
			return null;
		}
		
		return oSV;
	}



}
