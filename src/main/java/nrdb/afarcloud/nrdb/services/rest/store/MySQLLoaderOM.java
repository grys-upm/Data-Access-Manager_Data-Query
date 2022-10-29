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

package afarcloud.nrdb.services.rest.store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import afarcloud.nrdb.config.Constants;
import afarcloud.nrdb.dam.MySQLDataAccess;

public class MySQLLoaderOM extends MySQLLoader {
	

	
	static {
		sTableName = Constants.MySQL_OM_TABLE;
		COLUMNS = Constants.MySQL_LIST_OM_COLUMNS;
	}


	public MySQLLoaderOM(MySQLDataAccess oMySQL) {
		super(oMySQL);
		// TODO Auto-generated constructor stub
	}


	@Override
	protected int _deleteOldData(HashMap<String, Object> hValue) {
		// TODO Auto-generated method stub
		String sQuery = "DELETE FROM " + sTableName + " WHERE " +
				Constants.MySQL_OM_ENTITY_NAME + "= ? AND " +
				Constants.MySQL_OM_OBSERVED_PROPERTY + "= ? AND " + 
				Constants.MySQL_TIME + "<= ?"
				;
		List <Object> lValue= new ArrayList<Object>();

		lValue.add(hValue.get(Constants.MySQL_OM_ENTITY_NAME));
		lValue.add(hValue.get(Constants.MySQL_OM_OBSERVED_PROPERTY));
		lValue.add(hValue.get(Constants.MySQL_TIME));
		
		return oMySQL.deletePreparedStatement(sQuery, lValue);
	}

	
}

