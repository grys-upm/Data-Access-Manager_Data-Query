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

package afarcloud.nrdb.dam;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import afarcloud.nrdb.config.Constants;
import afarcloud.nrdb.util.GlobalParameters;
import ch.hsr.geohash.WGS84Point;
import ch.hsr.geohash.util.VincentyGeodesy;

public class MySQLDataAccess {

    private Connection oConnect = null;
    
    
    private static final Map<String, String> MySQL_PARAM_TO_COLUMNS;
	static {
		MySQL_PARAM_TO_COLUMNS = new HashMap<String, String>();

		MySQL_PARAM_TO_COLUMNS.put(Constants.SRV_PARAM_ENTITY_NAMES, Constants.MySQL_OM_ENTITY_NAME);
		MySQL_PARAM_TO_COLUMNS.put(Constants.SRV_PARAM_TYPES, Constants.MySQL_OM_TYPE);					
		MySQL_PARAM_TO_COLUMNS.put(Constants.SRV_PARAM_OBSERVED_PROPERTIES, Constants.MySQL_OM_OBSERVED_PROPERTY);
		MySQL_PARAM_TO_COLUMNS.put(Constants.SRV_PARAM_COLLAR_IDS, Constants.MySQL_COLLAR_ID);
	}

	// SPECIAL PARAMS => no string 
	public static final List<String> MySQL_LIST_SPECIAL;	
	
	
	static {
		MySQL_LIST_SPECIAL = new ArrayList<String> (Arrays.asList(
				new String[] {
				Constants.SRV_PARAM_LIMIT
				}
				));
		
	}
	
	public MySQLDataAccess() {
		// TODO Auto-generated constructor stub
	}
	
	
  public void openConnection(String sDBName) throws Exception {
	  _openConnection(sDBName);
  }
  public void openConnection() throws Exception {
	  	_openConnection( GlobalParameters.getInstance().getParameter(GlobalParameters.DS_MySQL_DBNAME) );
    }

 private void _openConnection(String sDBName) throws Exception {
	  
	  if (oConnect==null || oConnect.isClosed()) {
		  // This will load the MySQL driver, each DB has its own driver
	      Class.forName(GlobalParameters.getInstance().getParameter(GlobalParameters.DS_MySQL_DRIVER) ); //not needed any more No!! It is needed for mysql 5. If not it does not work
	
	      // Setup the connection with the DB
	      oConnect = DriverManager.getConnection(
	    		  "jdbc:mysql://" + GlobalParameters.getInstance().getParameter(GlobalParameters.DS_MySQL_SERVER) +
	    		  ":" + GlobalParameters.getInstance().getParameter(GlobalParameters.DS_MySQL_PORT) +
	    		  "/" +sDBName +
	    		  "?"+ "user=" + GlobalParameters.getInstance().getParameter(GlobalParameters.DS_MySQL_USER) +
	    		  "&password=" + GlobalParameters.getInstance().getParameter(GlobalParameters.DS_MySQL_PASSWORD) +
	    		  "&useSSL=false"
	              );
	      oConnect.setAutoCommit(true);
	  }
    }
	public void closeConnection() {
		try {
			if (oConnect != null) {
				
				oConnect.close();
			}
		} catch (SQLException e) { } // do nothing

	}
	
	/**
	 * This method provide the endpoint to query the database
	 * 
	 * @param query string with an SQL formated query
	 * @return 
	 */
	public ResultSet queryDatabase(String sQuery) {
        try {
            Statement oStatement = oConnect.createStatement();
            return oStatement.executeQuery(sQuery); //query

        } catch (SQLException e) {
        	return null;
        }
    }
	
	public ResultSet queryPreparedStatement (String sQuery, List<Object> lValue) {
		PreparedStatement oPS = _queryPreparedStatement(sQuery, lValue);
		if (oPS!=null) {
			try {
				if (oPS.execute()) {
					return oPS.getResultSet();
				}
			} catch (SQLException e) {
				System.out.println("ERROR$MySQL$queryPreparedStatement: " + e.getMessage());
			}
		}
		return null;
	}

	public int insertPreparedStatement (String sQuery, List<Object> lValue) {
		return _updatePreparedStatement(sQuery, lValue);
	}
	
	public int updatePreparedStatement (String sQuery, List<Object> lValue) {
		return _updatePreparedStatement(sQuery, lValue);
	}

	public int deletePreparedStatement (String sQuery, List<Object> lValue) {
		return _updatePreparedStatement(sQuery, lValue);
	}

	private int _updatePreparedStatement (String sQuery, List<Object> lValue) {
		int nRow = 0;
		PreparedStatement oPS = _queryPreparedStatement(sQuery, lValue);
		if (oPS!=null) {
			try {
				nRow = oPS.executeUpdate();
				
			} catch (SQLException e) {
				System.out.println("ERROR$MySQL$_updatePreparedStatement: " + e.getMessage());
			}
		}
		return nRow;
	}

	
	private PreparedStatement _queryPreparedStatement (String sQuery, List<Object> lValue) {
		Object oValue;
		try { 
			PreparedStatement oPS = oConnect.prepareStatement(sQuery);
			
			for(int i=1; i<=lValue.size(); i++){
				oValue = lValue.get(i-1);
				if (oValue==null) {
					oPS.setNull(i,  java.sql.Types.NULL);
				}else if (oValue instanceof String) {
					oPS.setString(i, (String) oValue);
				}else if (oValue instanceof Boolean) {					
					oPS.setBoolean(i, (Boolean) oValue); 
				}else if (oValue instanceof Double) {
					oPS.setDouble(i, (Double) oValue);
				}else if (oValue instanceof Integer) {
					oPS.setInt(i, (Integer) oValue);
				}else if (oValue instanceof Float) {
					oPS.setFloat(i, (Float) oValue);
				}else if (oValue instanceof Date) {
					oPS.setDate(i, (Date) oValue);
				}else if (oValue instanceof Long) {
					oPS.setLong(i, (Long) oValue);
				}																											
			}
			return oPS;
			
		} catch ( Exception e) {
			System.out.println("ERROR$MySQL$_queryPreparedStatement: " + e.getMessage());
			return null;
		}
		
	}
	
	/** *******************************************************
	 * 
	 */
	
	public static String getQuery(String sTableName, List<String>lColumns, HashMap<String,String> hConditions) {
		StringBuilder sbQuery = new StringBuilder();
		
		sbQuery.append(" SELECT ")
				.append(String.join(", ", lColumns))
				.append(" FROM " + sTableName)
				;

		String sWhere = _makeWhere(hConditions);

		if (!sWhere.isEmpty()) {
			sbQuery.append(" WHERE " + sWhere); 
		}

		//sQuery+=" ORDER BY time DESC";
		sbQuery.append( _makeOrder(hConditions.get(Constants.SRV_PARAM_ORDER)) );
		
		//limit clause
		if (hConditions.containsKey(Constants.SRV_PARAM_LIMIT)) {
			sbQuery.append(" LIMIT " + hConditions.get(Constants.SRV_PARAM_LIMIT) );
		}

		return sbQuery.toString();
	}
  
	
    private static String _makeWhere(HashMap<String,String> hConditions){
		
		String sWhere= "";
		String sAux;
		String sParam;
		
		if (hConditions!=null && !hConditions.isEmpty()) {	

			for (Entry<String, String> cond : hConditions.entrySet()) {
				sAux="";
				
				// list
				sParam = cond.getKey();
				if (MySQL_PARAM_TO_COLUMNS.containsKey(sParam) && !MySQL_LIST_SPECIAL.contains(sParam)) {
					sAux =  (Constants.SRV_LIST_PARAMS.contains(sParam))?
							 _listStringToPredicate(MySQL_PARAM_TO_COLUMNS.get(sParam), cond.getValue()) :							 
								 MySQL_PARAM_TO_COLUMNS.get(sParam) + "='" + cond.getValue() +"'" 
								 ;
					if (!sAux.isEmpty()) {
							if (!sWhere.isEmpty()) {
								sWhere+= " AND ";
							}
							sWhere+=" (" + sAux + ")"; 					
					}
					
				}else if(Constants.SRV_LIST_PREDICATES.contains(sParam)) {
					if (!sWhere.isEmpty()) {
						sWhere+= " AND ";
					}
					sWhere+=" (" + cond.getValue() + ")"; 					
				}
								
			}
				
			
			/* position condition
			 * a - only centroid => geohash based predicate
			 * b - centroid + radio => geohas proximity + georaptor based predicate
			 */
			if (hConditions.containsKey(Constants.SRV_PARAM_CENTROID_LATITUDE) && hConditions.containsKey(Constants.SRV_PARAM_CENTROID_LONGITUDE)) {
				//sAux = _makeWhereGeospatialRelation_GeoHash(hConditions);
				sAux = _makeWhereGeospatialRelation(hConditions);
				if (!sAux.isEmpty()) {
					if (!sWhere.isEmpty()) {
						sWhere+= " AND ";
					}
					sWhere+= sAux ;
				}
			}
			
    	}
		return sWhere;
    }


    private static String _makeWhereGeospatialRelation(HashMap<String,String> hConditions){
		
		String sWhere="";
		String sAux;
		
		if (hConditions!=null && !hConditions.isEmpty()) {	

	
			if (hConditions.containsKey(Constants.SRV_PARAM_CENTROID_LATITUDE) && hConditions.containsKey(Constants.SRV_PARAM_CENTROID_LONGITUDE)) {
				sAux="";
				
				if (!hConditions.containsKey(Constants.SRV_PARAM_RADIUS)) {								
					sAux=" ( "+Constants.MySQL_LOCATION_LATITUDE + "="+ Double.valueOf(hConditions.get(Constants.SRV_PARAM_CENTROID_LATITUDE)).doubleValue() +
							" AND " + Constants.MySQL_LOCATION_LONGITUDE + "="+ Double.valueOf(hConditions.get(Constants.SRV_PARAM_CENTROID_LONGITUDE)).doubleValue() +
							" )"
							; 					
				}else {
					/* compute boundingBox */
					WGS84Point oCenter = new WGS84Point(
							Double.valueOf(hConditions.get(Constants.SRV_PARAM_CENTROID_LATITUDE)).doubleValue(),
							Double.valueOf(hConditions.get(Constants.SRV_PARAM_CENTROID_LONGITUDE)).doubleValue()
							);
					int nRadio = Integer.valueOf(hConditions.get(Constants.SRV_PARAM_RADIUS)).intValue();
					
					WGS84Point northEastCorner = VincentyGeodesy.moveInDirection(VincentyGeodesy.moveInDirection(oCenter, 0, nRadio), 90, nRadio);
					WGS84Point southWestCorner = VincentyGeodesy.moveInDirection(VincentyGeodesy.moveInDirection(oCenter, 180, nRadio), 270, nRadio);
					
					sAux =	Constants.MySQL_LOCATION_LATITUDE + " >= " + southWestCorner.getLatitude() +
							" AND " + Constants.MySQL_LOCATION_LATITUDE + " <= " + northEastCorner.getLatitude() + 
							" AND " + Constants.MySQL_LOCATION_LONGITUDE + " >= " + southWestCorner.getLongitude() +
							" AND " + Constants.MySQL_LOCATION_LONGITUDE + " <= " + northEastCorner.getLongitude() 							
							;
				}
				
				sWhere=" (" + sAux + ")"; 					
				
			}
			
    	}
		return sWhere;
    }

    private static String _makeOrder(String sOrder) {
    	return  " ORDER BY " + Constants.MySQL_TIME + 
    			(
    					(sOrder!=null && Constants.SRV_LIST_ORDER.contains(sOrder))? sOrder :" DESC"
    			);
    }

    private static String _listStringToPredicate (String sField, String sCondition) {
		String sAux="";
		String[] aAux = sCondition.split(",");
		for (int i=0; i<aAux.length; i++) {
			if(!sAux.isEmpty()) {
				sAux+= " OR ";
			}
			sAux+= sField + "='" + aAux[i].trim() +"'"; 
		}
    	
		return sAux;
    }


    
}

