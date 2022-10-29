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

import java.io.File;

import org.influxdb.dto.BatchPoints;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;

import afarcloud.nrdb.config.Constants;


public abstract class JSONParser implements JSONParserInterface {

	protected static String AFC_ID[];
	protected static final int AFC_ID_PADDING = 2;
	
	protected static final String AFC_SCENARIO = Constants.IDB_SCENARIO;

	protected static final int GEOHASH_PRECISION = Constants.IDB_GEOHASH_PRECISION;
	
	static {		
		AFC_ID = new String[] { AFC_SCENARIO, Constants.IDB_SERVICE, Constants.IDB_PROVIDER, Constants.IDB_TYPE, Constants.IDB_ENTITY_NAME};		
	}

	protected String sDBName = "";	
	protected String sResourceId = "";
	protected JsonParser oParser;
	
	
	public JSONParser() {
		// TODO Auto-generated constructor stub
	}
	
	public BatchPoints parse(File fFile) throws NRDBJSONException {
		BatchPoints oReturn = null;
		try {
			oParser = new JsonFactory().createParser(fFile);
			oReturn = _parse();
		} catch (Exception e) {
			new NRDBJSONException(e.toString());
		}
		return oReturn;		
	}

	
	public BatchPoints parse(String sJson) throws NRDBJSONException {
		BatchPoints oReturn = null;
		try {
			oParser = new JsonFactory().createParser(sJson);
			oReturn = _parse();
		} catch (Exception e) {
			new NRDBJSONException(e.toString());
		}
		return oReturn;
	}

	public String getDBName() {
		return sDBName;
	}
	public String getResourceId() {
		return sResourceId;
	}
	
	protected abstract BatchPoints _parse() throws Exception;

}
