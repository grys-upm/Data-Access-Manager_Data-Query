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

package afarcloud.nrdb.export;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.influxdb.dto.QueryResult.Result;
import org.influxdb.dto.QueryResult.Series;

import afarcloud.nrdb.config.Constants;
import afarcloud.nrdb.dam.InfluxDataAccess;
import afarcloud.nrdb.dam.MySQLDataAccess;
import afarcloud.nrdb.services.rest.store.MySQLLoaderCollar;
import afarcloud.nrdb.util.DataTypes;
import afarcloud.nrdb.util.GlobalParameters;

public class MySQLExportCollar { 
	private static final String IDB_TO_MySQL_SELECT_COLLAR = String.join(", ", 
			new String[] {
					Constants.IDB_ENTITY_NAME, 
					Constants.IDB_LOCATION_LONGITUDE, Constants.IDB_LOCATION_LATITUDE, Constants.IDB_LOCATION_ALTITUDE,			
					Constants.IDB_COLLAR_RESOURCEALARM,
					Constants.IDB_COLLAR_ANOMALY_LOCATION, Constants.IDB_COLLAR_ANOMALY_TEMPERATURE,
					Constants.IDB_COLLAR_ANOMALY_DISTANCE, Constants.IDB_COLLAR_ANOMALY_POSITION,
					"LAST(" + Constants.IDB_COLLAR_TEMPERATURE + ") AS " + Constants.IDB_COLLAR_TEMPERATURE
			} );
	
	private static Map<String, String> IDB_TO_MYSQL_COLLAR;
	static {
		IDB_TO_MYSQL_COLLAR = new HashMap<>();

			IDB_TO_MYSQL_COLLAR.put(Constants.IDB_LOCATION_LONGITUDE, Constants.MySQL_LOCATION_LONGITUDE);
			IDB_TO_MYSQL_COLLAR.put(Constants.IDB_LOCATION_LATITUDE, Constants.MySQL_LOCATION_LATITUDE);
			IDB_TO_MYSQL_COLLAR.put(Constants.IDB_LOCATION_ALTITUDE, Constants.MySQL_LOCATION_ALTITUDE);
			IDB_TO_MYSQL_COLLAR.put(Constants.IDB_COLLAR_RESOURCEALARM, Constants.MySQL_COLLAR_RESOURCEALARM);
			IDB_TO_MYSQL_COLLAR.put(Constants.IDB_COLLAR_ANOMALY_LOCATION, Constants.MySQL_COLLAR_ANOMALY_LOCATION);
			IDB_TO_MYSQL_COLLAR.put(Constants.IDB_COLLAR_ANOMALY_TEMPERATURE, Constants.MySQL_COLLAR_ANOMALY_TEMPERATURE);
			IDB_TO_MYSQL_COLLAR.put(Constants.IDB_COLLAR_ANOMALY_DISTANCE, Constants.MySQL_COLLAR_ANOMALY_DISTANCE);
 			IDB_TO_MYSQL_COLLAR.put(Constants.IDB_COLLAR_ANOMALY_POSITION, Constants.MySQL_COLLAR_ANOMALY_POSITION);
			IDB_TO_MYSQL_COLLAR.put(Constants.IDB_COLLAR_TEMPERATURE, Constants.MySQL_COLLAR_TEMPERATURE);
	}

	public MySQLExportCollar() {
		// TODO Auto-generated constructor stub
	}
	
	public static  void lastInfluxDataToMySQL(String sDBInflux, String sDBMySQL) {
		InfluxDataAccess influxDA;
		MySQLDataAccess oMySQL = new MySQLDataAccess();
		MySQLLoaderCollar oMySQLLoader = null;
		String sQuery;
		
		
		influxDA= new InfluxDataAccess(sDBInflux);
		
		sQuery = " SELECT " + IDB_TO_MySQL_SELECT_COLLAR +
				 " FROM " +  Constants.IDB_COLLAR_MEASUREMENT +
				 " GROUP BY " + Constants.IDB_ENTITY_NAME;
		
		
		System.out.println(sQuery);
		
		List<Result> lResults = influxDA.queryDatabase(sQuery);
		HashMap<String, HashMap<String,Object>> hMySQLData = influxToMySQL(lResults, GlobalParameters.getInstance().getParameter(GlobalParameters.PREFIX_COLLAR ));
		
		System.out.println(hMySQLData.size());
		
		
		try {
			oMySQL.openConnection(sDBMySQL);
			oMySQLLoader = new MySQLLoaderCollar(oMySQL);
			int nRow;
			// open-close connection to each file
			if (hMySQLData.size()>0) {
				for(HashMap<String, Object>hValue : hMySQLData.values()) {
					nRow = oMySQLLoader.loadDataIntoMySQL(hValue);
					System.out.println ("\n\tJSONParser-COLLAR $MySQL INSERT: " + nRow);
				}				
			}
			
			// check insertion
			sQuery = "SELECT COUNT(*) AS numRows FROM " + Constants.MySQL_COLLAR_TABLE;
			ResultSet oRS = oMySQL.queryDatabase(sQuery);
			if (oRS!=null) {
				if (oRS.next()) {
					System.out.println("\t\tMYSQL:: " + oRS.getInt(1));
				}
				oRS.close();
			}
			oMySQL.closeConnection(); 
			oMySQLLoader = null;
			
		} catch (Exception e) {}	// do nothing



	}
	
	private static HashMap<String, HashMap<String,Object>> influxToMySQL(List<Result> lResults, String sPrefix){ 
		HashMap<String, HashMap<String,Object>> hMySQLData = new HashMap<String, HashMap<String,Object>>();
		String sKey;
		String sResName;
		String sObservedProperty;
		String sField;
		
		HashMap<String,Object> hValue;
		HashMap<String, Object> hPrevious;
		
		// key => MySQL_OM_ENTITY_NAME-MySQL_OM_OBSERVED_PROPERTY
		for (Result res: lResults) {
			if (!res.hasError() && res.getSeries()!=null) {
				for (Series serie: res.getSeries()) {		
					//tags :: aggregation
					if (
							(serie.getTags().get(InfluxDataAccess.INFLUX_TAG_RESOURCE)!=null &&
								!serie.getTags().get(InfluxDataAccess.INFLUX_TAG_RESOURCE).trim().isEmpty()
							) ||
							(serie.getTags().get(InfluxDataAccess.INFLUX_TAG_VEHICLE)!=null &&
							 !serie.getTags().get(InfluxDataAccess.INFLUX_TAG_VEHICLE).trim().isEmpty()
							) 							
						) {
						
						sResName = (serie.getTags().get(InfluxDataAccess.INFLUX_TAG_RESOURCE)!=null)?
	    							serie.getTags().get(InfluxDataAccess.INFLUX_TAG_RESOURCE):
	        						serie.getTags().get(InfluxDataAccess.INFLUX_TAG_VEHICLE)
	        						;
	    				sObservedProperty = (sPrefix!=null && !sPrefix.isEmpty()) ? serie.getName().replaceAll("^"+sPrefix, "") :serie.getName(); 
	    				sKey = sResName + "-" + sObservedProperty;
	    				//System.out.println(sKey);
	    				
	    				//There should only be one point, but the algorithm processes several and would only store the last recorded 
	    				List<List<Object>> lPoints = serie.getValues();   							
	    				for(List<Object> lPoint : lPoints) {
	    					hValue = new HashMap<String,Object>();
	    					hValue.put(Constants.MySQL_COLLAR_ID, sResName);
	    					
	    					for (int i=0; i<serie.getColumns().size();i++) {
	    						if (lPoint.get(i)!=null) {	    								    						
	    							switch (serie.getColumns().get(i)) {
									case "time":
										hValue.put(Constants.MySQL_TIME, DataTypes.stringDateToEpochSecond(lPoint.get(i).toString() ));
										break;

									default:
										sField = IDB_TO_MYSQL_COLLAR.get(serie.getColumns().get(i) );
										if (sField!=null) {
											hValue.put(sField, lPoint.get(i));
										}
										break;
									}
	    						}
	    					}
	    					//add
							//there should be one point
							 hPrevious=  hMySQLData.get(sKey);	    							
							if(	hPrevious == null ||
								Long.valueOf(String.valueOf(hPrevious.get(Constants.MySQL_TIME))) <  Long.valueOf(String.valueOf(hValue.get(Constants.MySQL_TIME)))
								) {
								hMySQLData.put(sKey, hValue);
							}
	    				}
	    				
					}
					
				}
			}
		}
		
		return hMySQLData;
	}
	

	public static void main(String[] args) {


		String[] aScenarios = new String[]{"AS01", "AS02", "AS03", "AS04", "AS05", "AS06", "AS07", "AS08", "AS09", "AS10", "AS11"};

	
		for (String sScenario : aScenarios) {
			System.out.printf("\n***********************************" + 
					"\n* Scenario %s" +
					"\n***********************************\n",
					sScenario
					);
			MySQLExportCollar.lastInfluxDataToMySQL(sScenario, "afarcloud_data_"+ sScenario.toLowerCase());
		}

		
	}

}
