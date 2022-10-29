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

package afarcloud.nrdb.services.thrift.test;

import java.util.List;

import com.afarcloud.thrift.Position;
import com.afarcloud.thrift.StateVector;

import afarcloud.nrdb.services.thrift.TelemetryExtractor;

public class TestQueryVehicleStateVector {

	public static void queryVehicleStateVector(Position oCentroid, int nRadio) {
		List<StateVector> lSV = TelemetryExtractor.getVehicleStateVector(oCentroid, nRadio);
		_showList(lSV);
	}

	public static void queryVehicleStateVectorByVehicle(int nVehicleId){
		List<StateVector> lSV = TelemetryExtractor.getVehicleStateVectorByVehicle(nVehicleId);
		_showList(lSV);
	}
	
	public static void queryVehicleHistoricalStateVectors(Position oCentroid, int nRadio, long lStartTime, long lEndTime) {
		List<StateVector> lSV = TelemetryExtractor.getVehicleHistoricalStateVectors(oCentroid, nRadio, lStartTime, lEndTime);
		_showList(lSV);
	}

	public static void queryVehicleHistoricalStateVectorByVehicleId(int nVehicleId, long lStartTime, long lEndTime){
		List<StateVector> lSV = TelemetryExtractor.getVehicleHistoricalStateVectorByVehicleId(nVehicleId, lStartTime, lEndTime);				
		_showList(lSV);
	}
	
	
	private static void _showList(List<StateVector> lSV) {		
		if(lSV!=null && !lSV.isEmpty()) {
			System.out.println("\n-----> " + lSV.size());
			/*
			lSV.forEach((StateVector oSV)->{
				System.out.println("\n" + oSV.toString());
			});
		*/	
		}
	}

	public static void main(String[] args) {
		/* probar en escenario AS12*/
		int nVehicleId= 5;
		double lat = 32.345;
		double lon = 12.563;
		
		long lStartTime = 1591605254; //2020-06-08T10:34:14
		long lEndTime = 1591778054; //2020-06-10T10:34:14
		
		/** lastData */
		
		System.out.println("\n**********************************"
				+ "\nqueryVehicleStateVector"
				+ "\n**********************************");
		TestQueryVehicleStateVector.queryVehicleStateVector(null,0);

		System.out.println("\n**********************************"
				+ "\nqueryVehicleStateVector + position => 50"
				+ "\n**********************************");		
		TestQueryVehicleStateVector.queryVehicleStateVector(new Position(lon, lat, 0),50);

		System.out.println("\n**********************************"
				+ "\nqueryVehicleStateVector + position => 500"
				+ "\n**********************************");
		
		TestQueryVehicleStateVector.queryVehicleStateVector(new Position(lon, lat, 0),500);

		System.out.println("\n**********************************"
				+ "\nqueryVehicleStateVector + position => 1000"
				+ "\n**********************************");
		
		TestQueryVehicleStateVector.queryVehicleStateVector(new Position(lon, lat, 0),1000);


		System.out.println("\n**********************************"
				+ "\nqueryVehicleStateVectorByVehicle"
				+ "\n**********************************");
		TestQueryVehicleStateVector.queryVehicleStateVectorByVehicle(nVehicleId);
		
		
		/* historicalData */		
		System.out.println("\n**********************************"
				+ "\nqueryVehicleHistoricalStateVectors"
				+ "\n**********************************");
		TestQueryVehicleStateVector.queryVehicleHistoricalStateVectors(null, 0, 0, 0);

		System.out.println("\n**********************************"
				+ "\nqueryVehicleHistoricalStateVectors + position => 50"
				+ "\n**********************************");
		TestQueryVehicleStateVector.queryVehicleHistoricalStateVectors(new Position(lon, lat, 0), 50, 0, 0);

		System.out.println("\n**********************************"
				+ "\nqueryVehicleHistoricalStateVectors + position => 50 + startTime"
				+ "\n**********************************");
		TestQueryVehicleStateVector.queryVehicleHistoricalStateVectors(new Position(lon, lat, 0), 50, lStartTime, 0);

		System.out.println("\n**********************************"
				+ "\nqueryVehicleHistoricalStateVectors + position => 50 + startTime + endTime" 
				+ "\n**********************************");
		TestQueryVehicleStateVector.queryVehicleHistoricalStateVectors(new Position(lon, lat, 0), 50, lStartTime, lEndTime);


		System.out.println("\n**********************************"
				+ "\nqueryVehicleHistoricalStateVectorByVehicleId"
				+ "\n**********************************");
		TestQueryVehicleStateVector.queryVehicleHistoricalStateVectorByVehicleId(nVehicleId, 0, 0);		

		System.out.println("\n**********************************"
				+ "\nqueryVehicleHistoricalStateVectorByVehicleId + startTime"
				+ "\n**********************************");
		TestQueryVehicleStateVector.queryVehicleHistoricalStateVectorByVehicleId(nVehicleId, lStartTime, 0);		

		System.out.println("\n**********************************"
				+ "\nqueryVehicleHistoricalStateVectorByVehicleId + startTime + endTime"
				+ "\n**********************************");
		TestQueryVehicleStateVector.queryVehicleHistoricalStateVectorByVehicleId(nVehicleId, lStartTime, lEndTime);
		

	}
}
