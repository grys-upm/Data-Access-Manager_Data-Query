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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import com.afarcloud.thrift.ObservationType;
import com.afarcloud.thrift.Position;
import com.afarcloud.thrift.SemanticQueryService;
import com.afarcloud.thrift.SensorData;
import com.afarcloud.thrift.SensorType;

import afarcloud.nrdb.services.thrift.TelemetryExtractorMySQL;
import afarcloud.nrdb.util.GlobalParameters;

public class TestQuerySensorDataMySQL {

	
	public static void querySensorLastData(Position oCentroid, int nRadio) {
		System.out.println( 
					TelemetryExtractorMySQL.getSensorLastData(oCentroid,nRadio)
					);
		
	}

	public static void querySensorLastDataBySensorUid(String sEntityName){
		System.out.println( 
				TelemetryExtractorMySQL.getSensorLastDataBySensor(sEntityName)
				);
	}
	
	public static void querySensorLastDataBySensorType(Position oCentroid, int nRadio, SensorType eSensorType) {
		System.out.println( 
				TelemetryExtractorMySQL.getSensorLastDataBySensorType(oCentroid, nRadio, eSensorType)
				);		
	}
	
	
	public static void querySensorLastDataByObservationType(Position oCentroid, int nRadio, ObservationType eObservationType) {
		System.out.println( 
				TelemetryExtractorMySQL.getSensorLastDataByObservationType(oCentroid, nRadio, eObservationType)
				);		
	}
	
	/** LastData without centroid */
	public static void querySensorLastData(List <String> lEntityNames){
		System.out.println( 
				TelemetryExtractorMySQL.getSensorLastData(lEntityNames)
				);
	}

	public static void querySensorLastDataBySensorType(List <String> lEntityNames, SensorType eSensorType) {
		System.out.println( 
				TelemetryExtractorMySQL.getSensorLastDataBySensorType(lEntityNames, eSensorType)
				);		
	}
	
	
	public static void querySensorLastDataByObservationType(List <String> lEntityNames, ObservationType eObservationType) {
		System.out.println( 
				TelemetryExtractorMySQL.getSensorLastDataByObservationType(lEntityNames, eObservationType)
				);		
	}
    private static Connection oConnect = null;
	
    public static void main(String[] args) {
		try {
			Class.forName(GlobalParameters.getInstance().getParameter(GlobalParameters.DS_MySQL_DRIVER) );
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} //not needed any more No!! It is needed for mysql 5. If not it does not work
	  	
	      // Setup the connection with the DB
	      try {
			oConnect = DriverManager.getConnection(
					  "jdbc:mysql://" + GlobalParameters.getInstance().getParameter(GlobalParameters.DS_MySQL_SERVER) +
					  ":" + GlobalParameters.getInstance().getParameter(GlobalParameters.DS_MySQL_PORT) +
					  "/" +"AS11" +
					  "?"+ "user=" + GlobalParameters.getInstance().getParameter(GlobalParameters.DS_MySQL_USER) +
					  "&password=" + GlobalParameters.getInstance().getParameter(GlobalParameters.DS_MySQL_PASSWORD) +
					  "&useSSL=false"
			          );
			oConnect.setAutoCommit(true);
			
			Statement oStatement = oConnect.createStatement();
			ResultSet result = oStatement.executeQuery("SELECT resourceId, observedProperty, resourceType, longitude, altitude, latitude, lastUpdate, lastValue, uom FROM lastMeasurement WHERE  (resourceId='afc_ait_001') ORDER BY lastUpdate DESC");
			System.out.println("Result: "+result.toString());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	      
	}
	
	
//	public static void main(String[] args) {
//		
//		String sEntityName="afc_ait_001";
//		SensorType eSensorType = SensorType.soil;
//		ObservationType eObservation = ObservationType.battery;
//		
//		double lat = 45.45123;
//		double lon = 25.25456;
//
//			
//		System.out.println("\n**********************************"
//				+ "\nSensorLastData "
//				+ "\n**********************************");
//		TestQuerySensorDataMySQL.querySensorLastData(null,0);
//
//		
//		
//		System.out.println("\n**********************************"
//				+ "\nSensorLastData + position => 50 " 
//				+ "\n**********************************");
//		TestQuerySensorDataMySQL.querySensorLastData(new Position(lon,lat,0),50);
//
//		System.out.println("\n**********************************"
//				+ "\nSensorLastData + position => 500 " 
//				+ "\n**********************************");
//		TestQuerySensorDataMySQL.querySensorLastData(new Position(lon,lat,0),500);
//
//		System.out.println("\n**********************************"
//				+ "\nSensorLastData + position => 1000 " 
//				+ "\n**********************************");
//		TestQuerySensorDataMySQL.querySensorLastData(new Position(lon,lat,0),1000);
//
//		
//		System.out.println("\n**********************************"
//				+ "\nSensorLastDataBySensorUid"
//				+ "\n**********************************");
//		TestQuerySensorDataMySQL.querySensorLastDataBySensorUid(sEntityName);
//		
//
//		
//		
//		System.out.println("\n**********************************"
//				+ "\nSensorLastDataBySensorType"
//				+ "\n**********************************");
//		TestQuerySensorDataMySQL.querySensorLastDataBySensorType(null, 0, eSensorType);
//
//		System.out.println("\n**********************************"
//				+ "\nSensorLastDataBySensorType  + position => 500 "
//				+ "\n**********************************");
//		TestQuerySensorDataMySQL.querySensorLastDataBySensorType(new Position(lon,lat,0), 500, eSensorType);
//
//
//		
//		
//		System.out.println("\n**********************************"
//				+ "\nSensorLastDataByObservationType "
//				+ "\n**********************************");
//		TestQuerySensorDataMySQL.querySensorLastDataByObservationType(null, 0, eObservation);
//
//		
//		System.out.println("\n**********************************"
//				+ "\nSensorLastDataByObservationType  + position => 500"
//				+ "\n**********************************");
//		TestQuerySensorDataMySQL.querySensorLastDataByObservationType(new Position(lon,lat,0), 500, eObservation);
//		 
//
//
//		
//		List<String> lEntityNames = Arrays.asList("afc_node_01","afc_node_0100_4","afc_node_0100_5");
//		/**** LastData - without centroid ***/
//		
//		System.out.println("\n**********************************"
//				+ "\nSensorLastData + null" 
//				+ "\n**********************************");
//		TestQuerySensorDataMySQL.querySensorLastData(null);
//
//		System.out.println("\n**********************************"
//				+ "\nSensorLastData + lsEntityNames" 
//				+ "\n**********************************");
//		TestQuerySensorDataMySQL.querySensorLastData(lEntityNames);
//		
//		System.out.println("\n**********************************"
//				+ "\nSensorLastDataBySensorType + lsEntityNames "
//				+ "\n**********************************");
//		TestQuerySensorDataMySQL.querySensorLastDataBySensorType(lEntityNames, eSensorType);
//
//		System.out.println("\n**********************************"
//				+ "\nSensorLastDataByObservationType + lsEntityNames "
//				+ "\n**********************************");
//		TestQuerySensorDataMySQL.querySensorLastDataByObservationType(lEntityNames, eObservation);
//		
//		
//		/* pruebas Afshin 20200903
//		 * observationType: Environmental
//		 * method: getSensorLastDataBySensorType
//		 * 
//		 * Centre: Lat: 44.22466, Lng: 11.94185
//		 * Radius: 1000
//         *
//         * Centre: Lat: 44.224444, Lng: 11.941667,
//         * Radius: 100
//		 * 
//		 *  
//
//		System.out.println("\n**********************************"
//						+ "\nSensorLastDataBySensorType  + Position(11.94185,44.22466,0); radius: 1000"
//						+ "\n**********************************");
//		TestQuerySensorDataMySQL.querySensorLastDataBySensorType(new Position(11.94185,44.22466,0), 1000, SensorType.environmental);
//		
//		System.out.println("\n**********************************"
//				+ "\nSensorLastDataBySensorType  + Position(11.941667,44.224444,0); radius: 100"
//				+ "\n**********************************");
//		//TestQuerySensorDataMySQL.querySensorLastDataBySensorType(new Position(11.941667, 44.224444, 0), 100, SensorType.environmental);		
//		TestQuerySensorDataMySQL.querySensorLastDataBySensorType(new Position(11.94185,44.22466,0), 100, SensorType.environmental);
//		
//		*/
//		/* pruebas rendimiento escenario AS04- Valladolid lat: 41.331229, lon:-4.993704  (position of the google mark) */
//
//		lat = 41.331229;
//		lon = -4.993704;
//		Position oPos = new Position(lon, lat, 0);
//
//		eObservation = ObservationType.air_temperature;
//		eSensorType = SensorType.environmental;
//		int nRadius = 1000;
//
//		System.out.println("\n**********************************"
//				+ "\nSensorLastData " 
//				+ "\n**********************************");
//		Instant start = Instant.now();
//		TestQuerySensorDataMySQL.querySensorLastData(oPos, nRadius );
//		Instant end = Instant.now();
//		System.out.println(Duration.between(start, end));
//
//
//		System.out.println("\n**********************************"
//				+ "\nSensorLastDataBySensorType "  +  eSensorType
//				+ "\n**********************************");
//		start = Instant.now();
//		TestQuerySensorDataMySQL.querySensorLastDataBySensorType(oPos, nRadius, eSensorType);
//		end = Instant.now();
//		System.out.println(Duration.between(start, end));
//
//		
//		System.out.println("\n**********************************"
//				+ "\nSensorLastDataBySensorType " + eObservation
//				+ "\n**********************************");
//		start = Instant.now();
//		TestQuerySensorDataMySQL.querySensorLastDataByObservationType(oPos, nRadius, eObservation);
//		end = Instant.now();
//		System.out.println(Duration.between(start, end));
//
//	}
}
