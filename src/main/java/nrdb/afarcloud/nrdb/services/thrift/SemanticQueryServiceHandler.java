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

import com.afarcloud.thrift.CollarData;
import com.afarcloud.thrift.Mission;
import com.afarcloud.thrift.MissionTag;
import com.afarcloud.thrift.ObservationType;
import com.afarcloud.thrift.Position;
import com.afarcloud.thrift.SemanticQueryService.Iface;
import com.afarcloud.thrift.SensorData;
import com.afarcloud.thrift.SensorType;
import com.afarcloud.thrift.StateVector;
import com.afarcloud.thrift.Vehicle;

public class SemanticQueryServiceHandler implements Iface {

	@Override
	public List<Vehicle> getAllVehicles(int requestId) throws TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<MissionTag> getAllMissions(int requestId) throws TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<MissionTag> getOngoingMissions(int requestId) throws TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Mission getMissionById(int requestId, int missionId) throws TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vehicle getVehicle(int requestId, int vid) throws TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SensorData> querySensorLastData(int requestId, Position regionCentre, int radius) throws TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SensorData> querySensorLastDataBySensorUid(int requestId, String sensorUid) throws TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SensorData> querySensorLastDataBySensorType(int requestId, Position regionCentre, int radius,
			SensorType sensorType) throws TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SensorData> querySensorLastDataByObservationType(int requestId, Position regionCentre, int radius,
			ObservationType obserType) throws TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CollarData> queryCollarLastData(int requestId, Position regionCentre, int radius) throws TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CollarData> queryCollarLastDataByCollarUid(int requestId, String collarUid) throws TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<StateVector> queryVehicleLastStateVector(int requestId, Position regionCentre, int radius)
			throws TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<StateVector> queryVehicleLastStateVectorByVehicleId(int requestId, int vehicleId) throws TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SensorData> querySensorHistoricalData(int requestId, Position regionCentre, int radius, long startTime,
			long endTime) throws TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SensorData> querySensorHistoricalDataBySensorUid(int requestId, String sensorUid, long startTime,
			long endTime) throws TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SensorData> querySensorHistoricalDataBySensorType(int requestId, Position regionCentre, int radius,
			SensorType sensorType, long startTime, long endTime) throws TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SensorData> querySensorHistoricalDataByObservationType(int requestId, Position regionCentre, int radius,
			ObservationType obserType, long startTime, long endTime) throws TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CollarData> queryCollarHistoricalData(int requestId, Position regionCentre, int radius, long startTime,
			long endTime) throws TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CollarData> queryCollarHistoricalDataByCollarUid(int requestId, String collarUid, long startTime,
			long endTime) throws TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<StateVector> queryVehicleHistoricalStateVectors(int requestId, Position regionCentre, int radius,
			long startTime, long endTime) throws TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<StateVector> queryVehicleHistoricalStateVectorByVehicleId(int requestId, int vehicleId, long startTime,
			long endTime) throws TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void storeEvent(int requestId, int missionId, int vehicleId, int subtype, String description,
			long timeReference) throws TException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String ping() throws TException {
		// TODO Auto-generated method stub
		return null;
	}
}
