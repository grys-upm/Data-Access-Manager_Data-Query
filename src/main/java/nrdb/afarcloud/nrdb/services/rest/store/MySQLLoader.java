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
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import afarcloud.nrdb.dam.MySQLDataAccess;

public abstract class MySQLLoader {
	
	// to be initialized by classes
	protected static String sTableName;
	protected static List<String> COLUMNS;
	protected static String sParamsStatement;
	
	protected MySQLDataAccess oMySQL;
	
	/**
	 * @param oMySQL the object must be instantiated and must have an open connection 
	 */
	public MySQLLoader(MySQLDataAccess oMySQL) {	
		String[] aAux =  new String[COLUMNS.size()];
		
		Arrays.fill(aAux, "?");
		sParamsStatement = String.join(", ", new ArrayList<String>( Arrays.asList(aAux)) );
		
		this.oMySQL = oMySQL;		
	}

	
	
	public int loadDataIntoMySQL( HashMap<String, Object> hValue ) {
		StringBuilder sbQuery = new StringBuilder(); 
		List <Object> lValue= new ArrayList<Object>();
		Object oValue;
		int nRow = _deleteOldData(hValue);
		
		//System.out.println ("\n\tMySQL-LOADER$DELETE: " + nRow);
		sbQuery.append(" INSERT INTO ")
				.append(sTableName)
				.append(" (")
				.append(String.join(", ", COLUMNS))
				.append(") VALUES (")
				.append(sParamsStatement)
				.append(")")
				;			
		
		for(String sColumn : COLUMNS) {
			
			// object or null
			if (hValue.containsKey(sColumn)) {
				oValue = hValue.get(sColumn);
				if (oValue instanceof String) {
					if (  ((String) oValue).equalsIgnoreCase("true") ||
						   ((String) oValue).equalsIgnoreCase("false")
						 ) {
						oValue = Boolean.valueOf((String) oValue);
					}
				}
				lValue.add(oValue);
			}else {
				lValue.add(null);
			}
		}
				
		return oMySQL.updatePreparedStatement(sbQuery.toString(), lValue);		
	}
		
	protected abstract int _deleteOldData (HashMap<String, Object> hValue );	
	
}

