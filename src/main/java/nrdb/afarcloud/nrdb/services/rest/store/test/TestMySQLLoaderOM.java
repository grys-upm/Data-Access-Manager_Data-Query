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
package afarcloud.nrdb.services.rest.store.test;

import java.util.HashMap;


import afarcloud.nrdb.config.Constants;
import afarcloud.nrdb.dam.MySQLDataAccess;
import afarcloud.nrdb.services.rest.store.MySQLLoaderOM;


public class TestMySQLLoaderOM{

	public static void main(String[] args) {
		MySQLDataAccess oMySQL = new MySQLDataAccess();
		try {
			oMySQL.openConnection();
			
			MySQLLoaderOM oTest = new MySQLLoaderOM(oMySQL);
			
			HashMap<String, Object> hValue = new HashMap<String, Object>();
				hValue.put(Constants.MySQL_OM_ENTITY_NAME, "sensor001");
				hValue.put(Constants.MySQL_OM_OBSERVED_PROPERTY, "soil_temp");
				hValue.put(Constants.MySQL_OM_TYPE, "environmental");
				
				hValue.put(Constants.MySQL_LOCATION_LONGITUDE, 45.2);
				hValue.put(Constants.MySQL_LOCATION_LATITUDE, 5.23);
				hValue.put(Constants.MySQL_LOCATION_ALTITUDE, 12.3);
				
				hValue.put(Constants.MySQL_TIME, 1601114268);
				hValue.put(Constants.MySQL_OM_VALUE, 29);
				hValue.put(Constants.MySQL_OM_UOM, "qudt.org/vocab/unit/DEG_C");
			
			oTest.loadDataIntoMySQL(hValue);
			
			oMySQL.closeConnection();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	
}

