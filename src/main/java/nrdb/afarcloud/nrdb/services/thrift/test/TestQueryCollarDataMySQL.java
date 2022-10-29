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

import afarcloud.nrdb.services.thrift.TelemetryExtractorMySQL;

public class TestQueryCollarDataMySQL {

	public static void queryCollarLastData(Position oCentroid, int nRadio) {
		System.out.println(
				TelemetryExtractorMySQL.getCollarLastData(oCentroid,nRadio)
				);
	}

	public static void queryCollarLastDataByCollarUid(String sEntityName){
		System.out.println( 
				TelemetryExtractorMySQL.getCollarLastDataByCollarUid(sEntityName)
				);
	}
	
	
	public static void main(String[] args) {
		/* probar en escenario AS06*/
		String sEntityName="AI840";
		double lat = 40.698;
		double lon = -4.536;
		
	
		/** lastData */
		
		System.out.println("\n**********************************"
				+ "\nqueryCollarLastData"
				+ "\n**********************************");
		TestQueryCollarDataMySQL.queryCollarLastData(null,0);

		System.out.println("\n**********************************"
				+ "\nqueryCollarLastData + position => 50"
				+ "\n**********************************");
		
		TestQueryCollarDataMySQL.queryCollarLastData(new Position(lon, lat, 0),50);

		System.out.println("\n**********************************"
				+ "\nqueryCollarLastData + position => 500"
				+ "\n**********************************");
		
		TestQueryCollarDataMySQL.queryCollarLastData(new Position(lon, lat, 0),500);

		System.out.println("\n**********************************"
				+ "\nqueryCollarLastData + position => 1000"
				+ "\n**********************************");
		
		TestQueryCollarDataMySQL.queryCollarLastData(new Position(lon, lat, 0),1000);


		System.out.println("\n**********************************"
				+ "\nueryCollarLastDataByCollarUid"
				+ "\n**********************************");
		TestQueryCollarDataMySQL.queryCollarLastDataByCollarUid(sEntityName);
		
	}
}
