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

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import com.afarcloud.thrift.ObservationType;
import com.afarcloud.thrift.Position;
import com.afarcloud.thrift.SensorData;
import com.afarcloud.thrift.SensorType;

import afarcloud.nrdb.services.thrift.TelemetryExtractor;

public class TestQuerySensorData {

	
	public static void querySensorLastData(Position oCentroid, int nRadio) {
		List<SensorData> lSD = TelemetryExtractor.getSensorLastData(oCentroid,nRadio);
		_showList(lSD);
	}

	public static void querySensorLastDataBySensorUid(String sEntityName){
		List<SensorData> lSD = TelemetryExtractor.getSensorLastDataBySensor(sEntityName);
		_showList(lSD);
	}
	
	public static void querySensorLastDataBySensorType(Position oCentroid, int nRadio, SensorType eSensorType) {
		List<SensorData> lSD = TelemetryExtractor.getSensorLastDataBySensorType(oCentroid, nRadio, eSensorType);
		_showList(lSD);		
	}
	
	
	public static void querySensorLastDataByObservationType(Position oCentroid, int nRadio, ObservationType eObservationType) {
		List<SensorData> lSD = TelemetryExtractor.getSensorLastDataByObservationType(oCentroid, nRadio, eObservationType);
		_showList(lSD);		
	}
	
	/** LastData without centroid */
	public static void querySensorLastData(List <String> lEntityNames){
		List<SensorData> lSD = TelemetryExtractor.getSensorLastData(lEntityNames);
		_showList(lSD);
	}

	public static void querySensorLastDataBySensorType(List <String> lEntityNames, SensorType eSensorType) {
		List<SensorData> lSD = TelemetryExtractor.getSensorLastDataBySensorType(lEntityNames, eSensorType);
		_showList(lSD);		
	}
	
	
	public static void querySensorLastDataByObservationType(List <String> lEntityNames, ObservationType eObservationType) {
		List<SensorData> lSD = TelemetryExtractor.getSensorLastDataByObservationType(lEntityNames, eObservationType);
		_showList(lSD);		
	}

	/** historical **/
	
	public static void querySensorHistoricalData(Position oCentroid, int nRadio, long lStartTime, long lEndTime) {
		List<SensorData> lSD = TelemetryExtractor.getSensorHistoricalData(oCentroid,nRadio, lStartTime, lEndTime);
		_showList(lSD);
	}

	
	public static void querySensorHistoricalDataBySensorUid(String sEntityName, long lStartTime, long lEndTime){
		List<SensorData> lSD = TelemetryExtractor.getSensorHistoricalDataBySensorUid(sEntityName, lStartTime, lEndTime);
		_showList(lSD);
	}
	
	public static void querySensorHistoricalDataBySensorType(Position oCentroid, int nRadio, SensorType eSensorType, long lStartTime, long lEndTime) {
		List<SensorData> lSD = TelemetryExtractor.getSensorHistoricalDataBySensorType(oCentroid, nRadio, eSensorType, lStartTime, lEndTime);
		_showList(lSD);		
	}
	
	
	public static void querySensorHistoricalDataByObservationType(Position oCentroid, int nRadio, ObservationType eObservationType, long lStartTime, long lEndTime) {
		List<SensorData> lSD = TelemetryExtractor.getSensorHistoricalDataByObservationType(oCentroid, nRadio, eObservationType, lStartTime, lEndTime);
		_showList(lSD);		
	}
	
	/** historical - without centroid**/
	public static void querySensorHistoricalData(List <String> lEntityNames, long lStartTime, long lEndTime) {
		List<SensorData> lSD = TelemetryExtractor.getSensorHistoricalData(lEntityNames, lStartTime, lEndTime);
		_showList(lSD);
	}
	public static void querySensorHistoricalDataBySensorType(List <String> lEntityNames, SensorType eSensorType, long lStartTime, long lEndTime) {
		List<SensorData> lSD = TelemetryExtractor.getSensorHistoricalDataBySensorType(lEntityNames, eSensorType, lStartTime, lEndTime);
		_showList(lSD);		
	}
	
	
	public static void querySensorHistoricalDataByObservationType(List <String> lEntityNames, ObservationType eObservationType, long lStartTime, long lEndTime) {
		List<SensorData> lSD = TelemetryExtractor.getSensorHistoricalDataByObservationType(lEntityNames, eObservationType, lStartTime, lEndTime);
		_showList(lSD);		
	}
	
	
	private static void _showList(List<SensorData> lSD) {
		if(lSD!=null && !lSD.isEmpty()) {
			System.out.println("\n-----> " + lSD.size());
			
			lSD.forEach((SensorData oSD)->{
				System.out.println("\n" + oSD.toString());
			});
			
		}
	}

	public static void main(String[] args) {
		
		String sEntityName="sensor_ait_001";
		SensorType eSensorType = SensorType.soil;
		ObservationType eObservation = ObservationType.battery;
		
		double lat = 43.723337;
		double lon = 10.339545;

		long lStartTime = 1634262327; //2020-06-08T10:34:14
		long lEndTime = 1637262327; //2020-06-10T10:34:14

		
		/*
		System.out.println("\n**********************************"
				+ "\nSensorLastData "
				+ "\n**********************************");
		TestQuerySensorData.querySensorLastData(null,0);

		
		
		System.out.println("\n**********************************"
				+ "\nSensorLastData + position => 50 " 
				+ "\n**********************************");
		TestQuerySensorData.querySensorLastData(new Position(lon,lat,0),50);

		System.out.println("\n**********************************"
				+ "\nSensorLastData + position => 500 " 
				+ "\n**********************************");
		TestQuerySensorData.querySensorLastData(new Position(lon,lat,0),500);

		System.out.println("\n**********************************"
				+ "\nSensorLastData + position => 1000 " 
				+ "\n**********************************");
		TestQuerySensorData.querySensorLastData(new Position(lon,lat,0),1000);

		
		System.out.println("\n**********************************"
				+ "\nSensorLastDataBySensorUid"
				+ "\n**********************************");
		TestQuerySensorData.querySensorLastDataBySensorUid(sEntityName);
		

		
		
		System.out.println("\n**********************************"
				+ "\nSensorLastDataBySensorType"
				+ "\n**********************************");
		TestQuerySensorData.querySensorLastDataBySensorType(null, 0, eSensorType);

		System.out.println("\n**********************************"
				+ "\nSensorLastDataBySensorType  + position => 500 "
				+ "\n**********************************");
		TestQuerySensorData.querySensorLastDataBySensorType(new Position(lon,lat,0), 500, eSensorType);


		
		
		System.out.println("\n**********************************"
				+ "\nSensorLastDataByObservationType "
				+ "\n**********************************");
		TestQuerySensorData.querySensorLastDataByObservationType(null, 0, eObservation);

		
		System.out.println("\n**********************************"
				+ "\nSensorLastDataByObservationType  + position => 500"
				+ "\n**********************************");
		TestQuerySensorData.querySensorLastDataByObservationType(new Position(lon,lat,0), 500, eObservation);
		 */

		/****** historical *****/
		/*
		System.out.println("\n**********************************"
				+ "\nSensorHistoricalData "
				+ "\n**********************************");
		TestQuerySensorData.querySensorHistoricalData(null,0, 0, 0);

		System.out.println("\n**********************************"
				+ "\nSensorHistoricaltData  + startTime + endTime" 
				+ "\n**********************************");
		TestQuerySensorData.querySensorHistoricalData(null,0, lStartTime, lEndTime);

		System.out.println("\n**********************************"
				+ "\nSensorHistoricaltData + position => 50 " 
				+ "\n**********************************");
		TestQuerySensorData.querySensorHistoricalData(new Position(lon,lat,0),50, 0, 0);
		
		System.out.println("\n**********************************"
				+ "\nSensorHistoricaltData + position => 50 + startTime + endTime" 
				+ "\n**********************************");
		TestQuerySensorData.querySensorHistoricalData(new Position(lon,lat,0),50, lStartTime, lEndTime);
		 */
		
		/*****/
		/*
		System.out.println("\n**********************************"
				+ "\nSensorHistoricalDataBySensorUid"
				+ "\n**********************************");
		TestQuerySensorData.querySensorHistoricalDataBySensorUid(sEntityName, lStartTime, lEndTime);
		*/
		
		/*****/
		
		/*
		System.out.println("\n**********************************"
				+ "\nSensorHistoricalDataBySensorType"
				+ "\n**********************************");
		TestQuerySensorData.querySensorHistoricalDataBySensorType(null, 0, eSensorType, lStartTime, lEndTime);

		System.out.println("\n**********************************"
				+ "\nSensorHistoricalDataBySensorType  + position => 500 "
				+ "\n**********************************");
		TestQuerySensorData.querySensorHistoricalDataBySensorType(new Position(lon,lat,0), 500, eSensorType, lStartTime, lEndTime);
		*/
		
		/*****/		
		
		/*
		System.out.println("\n**********************************"
				+ "\nSensorHistoricalDataByObservationType "
				+ "\n**********************************");
		TestQuerySensorData.querySensorHistoricalDataByObservationType(null, 0, eObservation, lStartTime, lEndTime);

		
		System.out.println("\n**********************************"
				+ "\nSensorHistoricalDataByObservationType  + position => 500"
				+ "\n**********************************");
		TestQuerySensorData.querySensorHistoricalDataByObservationType(new Position(lon,lat,0), 500, eObservation, lStartTime, lEndTime);
		*/

		
		List<String> lEntityNames = Arrays.asList("afc_node_0100_2","afc_node_0100_4","afc_node_0100_0");
		/**** LastData - without centroid ***/
		/*
		System.out.println("\n**********************************"
				+ "\nSensorLastData + null" 
				+ "\n**********************************");
		TestQuerySensorData.querySensorLastData(null);

		System.out.println("\n**********************************"
				+ "\nSensorLastData + lsEntityNames" 
				+ "\n**********************************");
		TestQuerySensorData.querySensorLastData(lEntityNames);
		
		System.out.println("\n**********************************"
				+ "\nSensorLastDataBySensorType + lsEntityNames "
				+ "\n**********************************");
		TestQuerySensorData.querySensorLastDataBySensorType(lEntityNames, eSensorType);

		System.out.println("\n**********************************"
				+ "\nSensorLastDataByObservationType + lsEntityNames "
				+ "\n**********************************");
		TestQuerySensorData.querySensorLastDataByObservationType(lEntityNames, eObservation);
		*/
		
		/**** historical - without centroid ***/
/*
		System.out.println("\n**********************************"
				+ "\nSensorHistoricaltData + lsEntityNames " 
				+ "\n**********************************");
		TestQuerySensorData.querySensorHistoricalData(lEntityNames, lStartTime, lEndTime);

		System.out.println("\n**********************************"
				+ "\nSensorHistoricalDataBySensorType + lsEntityNames "
				+ "\n**********************************");
		TestQuerySensorData.querySensorHistoricalDataBySensorType(lEntityNames, eSensorType, lStartTime, lEndTime);

		System.out.println("\n**********************************"
				+ "\nSensorHistoricalDataByObservationType + lsEntityNames"
				+ "\n**********************************");
		TestQuerySensorData.querySensorHistoricalDataByObservationType(lEntityNames, eObservation, lStartTime, lEndTime);

*/	
		
		
		/* pruebas Afshin 20200903
		 * observationType: Environmental
		 * method: getSensorLastDataBySensorType
		 * 
		 * Centre: Lat: 44.22466, Lng: 11.94185
		 * Radius: 1000
         *
         * Centre: Lat: 44.224444, Lng: 11.941667,
         * Radius: 100
		 * 
		 *  

		System.out.println("\n**********************************"
						+ "\nSensorLastDataBySensorType  + Position(11.94185,44.22466,0); radius: 1000"
						+ "\n**********************************");
		TestQuerySensorData.querySensorLastDataBySensorType(new Position(11.94185,44.22466,0), 1000, SensorType.environmental);
		
		System.out.println("\n**********************************"
				+ "\nSensorLastDataBySensorType  + Position(11.941667,44.224444,0); radius: 100"
				+ "\n**********************************");
		//TestQuerySensorData.querySensorLastDataBySensorType(new Position(11.941667, 44.224444, 0), 100, SensorType.environmental);		
		TestQuerySensorData.querySensorLastDataBySensorType(new Position(11.94185,44.22466,0), 100, SensorType.environmental);
		
		*/
		/* pruebas rendimiento escenario AS04- Valladolid lat: 41.331229, lon:-4.993704  (position of the google mark) */
		
		lat = 41.331229;
		lon = -4.993704;
		Position oPos = new Position(lon, lat, 0);

		eObservation = ObservationType.air_temperature;
		eSensorType = SensorType.environmental;
		int nRadius = 1000;

		System.out.println("\n**********************************"
				+ "\nSensorLastData " 
				+ "\n**********************************");
		Instant start = Instant.now();
		TestQuerySensorData.querySensorLastData(oPos, nRadius );
		Instant end = Instant.now();
		System.out.println(Duration.between(start, end));


		System.out.println("\n**********************************"
				+ "\nSensorLastDataBySensorType "  +  eSensorType
				+ "\n**********************************");
		start = Instant.now();
		TestQuerySensorData.querySensorLastDataBySensorType(oPos, nRadius, eSensorType);
		end = Instant.now();
		System.out.println(Duration.between(start, end));

		
		System.out.println("\n**********************************"
				+ "\nSensorLastDataBySensorType " + eObservation
				+ "\n**********************************");
		start = Instant.now();
		TestQuerySensorData.querySensorLastDataByObservationType(oPos, nRadius, eObservation);
		end = Instant.now();
		System.out.println(Duration.between(start, end));

	}
}
