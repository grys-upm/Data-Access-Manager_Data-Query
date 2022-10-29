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

import com.afarcloud.thrift.CollarData;
import com.afarcloud.thrift.Position;

import afarcloud.nrdb.services.thrift.TelemetryExtractor;

public class TestQueryCollarData {

	public static void queryCollarLastData(Position oCentroid, int nRadio) {
		List<CollarData> lCD = TelemetryExtractor.getCollarLastData(oCentroid,nRadio);
		_showList(lCD);
	}

	public static void queryCollarLastDataByCollarUid(String sEntityName){
		List<CollarData> lCD = TelemetryExtractor.getCollarLastDataByCollarUid(sEntityName);
		_showList(lCD);
	}
	
	public static void queryCollarHistoricalData(Position oCentroid, int nRadio, long lStartTime, long lEndTime) {
		List<CollarData> lCD = TelemetryExtractor.getCollarHistoricalData(oCentroid,nRadio, lStartTime, lEndTime);
		_showList(lCD);
	}

	public static void queryCollarHistoricalDataByCollarUid(String sEntityName, long lStartTime, long lEndTime){
		List<CollarData> lCD = TelemetryExtractor.getCollarHistoricalDataByCollarUid(sEntityName, lStartTime, lEndTime);
		_showList(lCD);
	}
	
	
	private static void _showList(List<CollarData> lCD) {		
		if(lCD!=null && !lCD.isEmpty()) {
			System.out.println("\n-----> " + lCD.size());
			
			lCD.forEach((CollarData oCD)->{
				System.out.println("\n" + oCD.toString());
			});
			
		}
	}

	public static void main(String[] args) {
		/* probar en escenario AS06*/
		String sEntityName="AI840";
		double lat = 40.698;
		double lon = -4.536;
		
		long lStartTime = 1591605254; //2020-06-08T10:34:14
		long lEndTime = 1591778054; //2020-06-10T10:34:14
		
		/** lastData */
		
		System.out.println("\n**********************************"
				+ "\nqueryCollarLastData"
				+ "\n**********************************");
		TestQueryCollarData.queryCollarLastData(null,0);

		System.out.println("\n**********************************"
				+ "\nqueryCollarLastData + position => 50"
				+ "\n**********************************");
		
		TestQueryCollarData.queryCollarLastData(new Position(lon, lat, 0),50);

		System.out.println("\n**********************************"
				+ "\nqueryCollarLastData + position => 500"
				+ "\n**********************************");
		
		TestQueryCollarData.queryCollarLastData(new Position(lon, lat, 0),500);

		System.out.println("\n**********************************"
				+ "\nqueryCollarLastData + position => 1000"
				+ "\n**********************************");
		
		TestQueryCollarData.queryCollarLastData(new Position(lon, lat, 0),1000);


		System.out.println("\n**********************************"
				+ "\nueryCollarLastDataByCollarUid"
				+ "\n**********************************");
		TestQueryCollarData.queryCollarLastDataByCollarUid(sEntityName);
		
		
		/* historicalData */
		/*
		System.out.println("\n**********************************"
				+ "\nqueryCollarHistoricalData"
				+ "\n**********************************");
		TestQueryCollarData.queryCollarHistoricalData(null, 0, 0, 0);

		System.out.println("\n**********************************"
				+ "\nqueryCollarHistoricalData + position => 50"
				+ "\n**********************************");
		TestQueryCollarData.queryCollarHistoricalData(new Position(lon, lat, 0), 50, 0, 0);

		System.out.println("\n**********************************"
				+ "\nqueryCollarHistoricalData + position => 50 + startTime"
				+ "\n**********************************");
		TestQueryCollarData.queryCollarHistoricalData(new Position(lon, lat, 0), 50, lStartTime, 0);

		System.out.println("\n**********************************"
				+ "\nqueryCollarHistoricalData + position => 50 + startTime + endTime" 
				+ "\n**********************************");
		TestQueryCollarData.queryCollarHistoricalData(new Position(lon, lat, 0), 50, lStartTime, lEndTime);


		System.out.println("\n**********************************"
				+ "\nueryCollarHistoricalDataByCollarUid"
				+ "\n**********************************");
		TestQueryCollarData.queryCollarHistoricalDataByCollarUid(sEntityName, 0, 0);		

		System.out.println("\n**********************************"
				+ "\nueryCollarHistoricalDataByCollarUid + startTime"
				+ "\n**********************************");
		TestQueryCollarData.queryCollarHistoricalDataByCollarUid(sEntityName, lStartTime, 0);		

		System.out.println("\n**********************************"
				+ "\nueryCollarHistoricalDataByCollarUid + startTime + endTime"
				+ "\n**********************************");
		TestQueryCollarData.queryCollarHistoricalDataByCollarUid(sEntityName, lStartTime, lEndTime);
		*/

	}
}
