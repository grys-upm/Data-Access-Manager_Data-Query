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

import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;

import com.afarcloud.thrift.CollarData;
import com.afarcloud.thrift.ObservationType;
import com.afarcloud.thrift.Position;
import com.afarcloud.thrift.SemanticQueryService.AsyncIface;
import com.afarcloud.thrift.SensorData;
import com.afarcloud.thrift.SensorType;
import com.afarcloud.thrift.StateVector;

public class SemanticQueryAsyncHandler implements AsyncIface {

	@Override
	public void getAllVehicles(int requestId, AsyncMethodCallback resultHandler) throws TException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getAllMissions(int requestId, AsyncMethodCallback resultHandler) throws TException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getOngoingMissions(int requestId, AsyncMethodCallback resultHandler) throws TException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getMissionById(int requestId, int missionId, AsyncMethodCallback resultHandler) throws TException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getVehicle(int requestId, int vid, AsyncMethodCallback resultHandler) throws TException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void querySensorLastData(int requestId, Position regionCentre, int radius, AsyncMethodCallback resultHandler)
			throws TException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void querySensorLastDataBySensorUid(int requestId, String sensorUid, AsyncMethodCallback resultHandler)
			throws TException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void querySensorLastDataBySensorType(int requestId, Position regionCentre, int radius, SensorType sensorType,
			AsyncMethodCallback resultHandler) throws TException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void querySensorLastDataByObservationType(int requestId, Position regionCentre, int radius,
			ObservationType obserType, AsyncMethodCallback resultHandler) throws TException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void queryCollarLastData(int requestId, Position regionCentre, int radius, AsyncMethodCallback resultHandler)
			throws TException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void queryCollarLastDataByCollarUid(int requestId, String collarUid, AsyncMethodCallback resultHandler)
			throws TException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void queryVehicleLastStateVector(int requestId, Position regionCentre, int radius,
			AsyncMethodCallback resultHandler) throws TException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void queryVehicleLastStateVectorByVehicleId(int requestId, int vehicleId, AsyncMethodCallback resultHandler)
			throws TException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void querySensorHistoricalData(int requestId, Position regionCentre, int radius, long startTime,
			long endTime, AsyncMethodCallback resultHandler) throws TException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void querySensorHistoricalDataBySensorUid(int requestId, String sensorUid, long startTime, long endTime,
			AsyncMethodCallback resultHandler) throws TException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void querySensorHistoricalDataBySensorType(int requestId, Position regionCentre, int radius,
			SensorType sensorType, long startTime, long endTime, AsyncMethodCallback resultHandler) throws TException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void querySensorHistoricalDataByObservationType(int requestId, Position regionCentre, int radius,
			ObservationType obserType, long startTime, long endTime, AsyncMethodCallback resultHandler)
			throws TException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void queryCollarHistoricalData(int requestId, Position regionCentre, int radius, long startTime,
			long endTime, AsyncMethodCallback resultHandler) throws TException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void queryCollarHistoricalDataByCollarUid(int requestId, String collarUid, long startTime, long endTime,
			AsyncMethodCallback resultHandler) throws TException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void queryVehicleHistoricalStateVectors(int requestId, Position regionCentre, int radius, long startTime,
			long endTime, AsyncMethodCallback resultHandler) throws TException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void queryVehicleHistoricalStateVectorByVehicleId(int requestId, int vehicleId, long startTime, long endTime,
			AsyncMethodCallback resultHandler) throws TException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void storeEvent(int requestId, int missionId, int vehicleId, int subtype, String description,
			long timeReference, AsyncMethodCallback resultHandler) throws TException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void ping(AsyncMethodCallback resultHandler) throws TException {
		// TODO Auto-generated method stub
		
	}
}
