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

package afarcloud.nrdb.services.rest;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult.Result;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import afarcloud.nrdb.services.rest.store.NRDBJSONException;
import afarcloud.nrdb.util.*;
import okhttp3.OkHttpClient;

public class DataLayer {
	
	protected static final URI docsUri=URI.create(Main.BASE_URI+"dataAccessManager/");
	protected final Response invalidScenarioException = Response.status(405).entity("405: \"Invalid input: not AFarCloud-scenario\". For more information, please refer to the API documentation: "+ docsUri).header("Access-Control-Allow-Origin", "*").build();
	protected final Response invalidParsedJsonException = Response.status(405).entity("405: \"Invalid input: The json can not be parsed\". For more information, please refer to the API documentation: "+ docsUri).header("Access-Control-Allow-Origin", "*").build();	

	public void injectData(afarcloud.nrdb.services.rest.store.JSONParser parser, String json) throws IOException, WebApplicationException, NRDBJSONException {
		
		boolean scenarioValid = false;
		
		try 
		{
			OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient().newBuilder()
				    .connectTimeout(40, TimeUnit.SECONDS)
				    .readTimeout(80, TimeUnit.SECONDS)
				    .writeTimeout(60, TimeUnit.SECONDS);
			
			InfluxDB influxDB = InfluxDBFactory.connect((GlobalParameters.getInstance().getParameter(GlobalParameters.DS_IDB_SERVER)+":"+GlobalParameters.getInstance().getParameter(GlobalParameters.DS_IDB_PORT)), GlobalParameters.getInstance().getParameter(GlobalParameters.DS_IDB_USER), GlobalParameters.getInstance().getParameter(GlobalParameters.DS_IDB_PASSWORD), okHttpClientBuilder);
		
			BatchPoints newInject = parser.parse(json);
			
			/**             DataBase Validation              **/
			if(newInject.getDatabase().matches(GlobalParameters.getInstance().getParameter(GlobalParameters.AFC_SCENARIO)))
			{
				scenarioValid = true;
				influxDB.write(newInject);	
			}else 
			{ 
				scenarioValid = false;
				throw new WebApplicationException();
			}
			
			influxDB.close();
			/**             DataBase Validation              **/	
			
		}catch (Exception e) 
		{
//			LogFile.getInstance().error(getClass(), "Error when trying to parse the Json, exception: "+ e );
//			log.error("Error when trying to parse the Json, exception: "+ e );	
			if(scenarioValid) 
			{
				LogFile.getInstance().error(getClass(), "Error when trying to parse the Json, exception: "+ e + invalidParsedJsonException.getEntity().toString() );
				final Response detailedException = Response.status(405).entity(invalidParsedJsonException.getEntity().toString()+"\nError: "+ e).build();
				throw new WebApplicationException(detailedException);
			}
			else
			{
				LogFile.getInstance().error(getClass(), "Error when trying to parse the Json, exception: "+ e + invalidScenarioException.getEntity().toString() );
				final Response detailedException = Response.status(405).entity(invalidScenarioException.getEntity().toString()+"\nError: "+ e).build();
				throw new WebApplicationException(detailedException);
			}
		}
	}

    
	public String queryMeasures(String measurement, String startDate, String endDate, String dataBase) throws IOException, WebApplicationException {
	
		boolean scenarioValid = false;
		List<Result> results = null;
		Gson gson = new Gson();
		JsonArray elements = new JsonArray();
		JsonObject root = new JsonObject();
		String result = new String();
		
		try 
		{
			OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient().newBuilder()
				    .connectTimeout(40, TimeUnit.SECONDS)
				    .readTimeout(80, TimeUnit.SECONDS)
				    .writeTimeout(60, TimeUnit.SECONDS);
			InfluxDB influxDB = InfluxDBFactory.connect((GlobalParameters.getInstance().getParameter(GlobalParameters.DS_IDB_SERVER)+":"+GlobalParameters.getInstance().getParameter(GlobalParameters.DS_IDB_PORT)), GlobalParameters.getInstance().getParameter(GlobalParameters.DS_IDB_USER), GlobalParameters.getInstance().getParameter(GlobalParameters.DS_IDB_PASSWORD), okHttpClientBuilder);
			
			/**             DataBase Validation and DB query              **/
			
			if(dataBase.matches(GlobalParameters.getInstance().getParameter(GlobalParameters.AFC_SCENARIO)))
			{
				scenarioValid = true;
				influxDB.setDatabase(dataBase);
				
				if(endDate.equals("0")) 
				{
					results = (influxDB.query(new Query("SELECT * FROM "+measurement+" WHERE time >= '"+startDate+"'")).getResults()) ;
					influxDB.close();
				}else
				{
					results = (influxDB.query(new Query("SELECT * FROM "+measurement+" WHERE time >= '"+startDate+"' AND time <= '"+endDate+"'")).getResults());
					influxDB.close();
				}
				
				
				for (int i =0; i<results.get(0).getSeries().get(0).getValues().size();i++) 
				{
					JsonObject element = new JsonObject();
					for(int j = 0; j<results.get(0).getSeries().get(0).getColumns().size();j++) 
					{
						if(results.get(0).getSeries().get(0).getValues().get(i).get(j) != null) 
						{
							element.addProperty(results.get(0).getSeries().get(0).getColumns().get(j), results.get(0).getSeries().get(0).getValues().get(i).get(j).toString());
					
						}
					}
				elements.add(element);
				}
				root.add("measurements", elements);
				result = gson.toJson(root);
				/**    Show json result     **/
//				System.out.println(result);		
				/**    Show json result     **/	
				return result;
				
			}else 
			{
				scenarioValid = false;
				throw new WebApplicationException();
			}
			
			/**             DataBase Validation and DB query              **/	
		
		}catch (Exception e) 
		{
//			log.error("Error when trying to create Json from query results, exception: "+ e );	
			if(!scenarioValid) 
			{
				LogFile.getInstance().warn(getClass(), e.toString()+", not a valid scenario");
				root.addProperty("Fail",e.toString()+", not a valid scenario");
//				log.error("Error when trying to validate the scenario, exception: "+ e );	
				result = gson.toJson(root);
				return result;
			}
			else
			{
				LogFile.getInstance().warn(getClass(), "Json builder fail, no results found");
				root.addProperty("Empty","Json builder fail, no results found");
//				log.error("Error when trying to create Json from query results, exception: "+ e );	
				result = gson.toJson(root);
				return result;
			}
		
		}
	} 
	
	
	
	public String queryResources(String entityName, String startDate, String endDate, String dataBase, String type) throws IOException, WebApplicationException {
		
		String resourceName = "entityName";
		boolean scenarioValid = false;
		List<Result> results = null;
		Gson gson = new Gson();
		JsonArray elements = new JsonArray();
		JsonObject root = new JsonObject();
		String result = new String();
		String measurements = "";
		
		try 
		{
			OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient().newBuilder()
				    .connectTimeout(40, TimeUnit.SECONDS)
				    .readTimeout(80, TimeUnit.SECONDS)
				    .writeTimeout(60, TimeUnit.SECONDS);
			InfluxDB influxDB = InfluxDBFactory.connect((GlobalParameters.getInstance().getParameter(GlobalParameters.DS_IDB_SERVER)+":"+GlobalParameters.getInstance().getParameter(GlobalParameters.DS_IDB_PORT)), GlobalParameters.getInstance().getParameter(GlobalParameters.DS_IDB_USER), GlobalParameters.getInstance().getParameter(GlobalParameters.DS_IDB_PASSWORD), okHttpClientBuilder);
			
			/**             DataBase Validation and DB query              **/
			
			if(dataBase.matches(GlobalParameters.getInstance().getParameter(GlobalParameters.AFC_SCENARIO)))
			{
				scenarioValid = true;
				influxDB.setDatabase(dataBase);
				
				results = influxDB.query(new Query("SHOW MEASUREMENTS")).getResults();
				for(int z = 0; z < results.get(0).getSeries().get(0).getValues().size(); z++) 
				{
					measurements += ((results.get(0).getSeries().get(0).getValues().get(z).get(0)) + ","); 
				}
				measurements = measurements.substring(0, measurements.length() - 1);
				if(endDate.equals("0"))
				{
					results = influxDB.query(new Query("SELECT * FROM "+measurements+" WHERE \""+resourceName+"\" = '"+entityName+"' AND time >= '"+startDate+"'")).getResults() ;

				}else
				{

					results = influxDB.query(new Query("SELECT * FROM "+measurements+" WHERE \""+resourceName+"\" = '"+entityName+"' AND time >= '"+startDate+"' AND time <= '"+endDate+"'")).getResults() ;
				}
				for (int i =0; i<results.get(0).getSeries().get(0).getValues().size();i++) 
				{
					JsonObject element = new JsonObject();
					for(int j = 0; j<results.get(0).getSeries().get(0).getColumns().size();j++) 
					{	
						if(results.get(0).getSeries().get(0).getValues().get(i).get(j) != null) 
						{
							element.addProperty(results.get(0).getSeries().get(0).getColumns().get(j), results.get(0).getSeries().get(0).getValues().get(i).get(j).toString());
						}
					}
					elements.add(element);
				}
				root.add(type, elements);
				result = gson.toJson(root);
				/**    Show json result     **/
//				System.out.println(result);		
				/**    Show json result     **/		
				return result;
				
			}else 
			{
				scenarioValid = false;
				throw new WebApplicationException();
			}
			/**             DataBase Validation and DB query              **/	
			
		}catch (Exception e) 
		{
			
			if(!scenarioValid) 
			{
				LogFile.getInstance().warn(getClass(), e.toString()+", not a valid scenario");
				root.addProperty("Fail",e.toString()+", not a valid scenario");
//				log.error("Error when trying to validate the scenario, exception: "+ e );	
				result = gson.toJson(root);
				return result;
			}
			else
			{
				LogFile.getInstance().warn(getClass(), "Json builder fail, no results found");
				root.addProperty("Empty","Json builder fail, no results found");
//				log.error("Error when trying to create Json from query results, exception: "+ e );	
				result = gson.toJson(root);
				return result;
			}
		
		}
	}
	
	
	public String queryList(String measurement, String resources, String startDate, String endDate, String dataBase) throws IOException, WebApplicationException {
		
		String resourceName = "entityName";
		boolean scenarioValid = false;
		List<Result> results = null;
		List<Result> measurements = null;
		Gson gson = new Gson();
		JsonArray elements = new JsonArray();
		JsonObject root = new JsonObject();
		String result = new String();
		
		try 
		{
			OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient().newBuilder()
				    .connectTimeout(40, TimeUnit.SECONDS)
				    .readTimeout(80, TimeUnit.SECONDS)
				    .writeTimeout(60, TimeUnit.SECONDS);
			InfluxDB influxDB = InfluxDBFactory.connect((GlobalParameters.getInstance().getParameter(GlobalParameters.DS_IDB_SERVER)+":"+GlobalParameters.getInstance().getParameter(GlobalParameters.DS_IDB_PORT)), GlobalParameters.getInstance().getParameter(GlobalParameters.DS_IDB_USER), GlobalParameters.getInstance().getParameter(GlobalParameters.DS_IDB_PASSWORD), okHttpClientBuilder);
			
			/**             DataBase Validation and DB query              **/
			
			if(dataBase.matches(GlobalParameters.getInstance().getParameter(GlobalParameters.AFC_SCENARIO)))
			{
				scenarioValid = true;
				influxDB.setDatabase(dataBase);
				
				if(endDate.equals("0")) 
				{
					if(measurement == "null") {
						measurement = "";
						measurements = influxDB.query(new Query("SHOW MEASUREMENTS")).getResults();
						for(int z = 0; z < measurements.get(0).getSeries().get(0).getValues().size(); z++) 
						{
							measurement += ((measurements.get(0).getSeries().get(0).getValues().get(z).get(0)) + ","); 
						}
						measurement = measurement.substring(0, measurement.length() - 1);
					}
					if(resources == "null") 
					{
						results = (influxDB.query(new Query("SELECT * FROM "+measurement+" WHERE time >= '"+startDate+"'")).getResults()) ;
						
					}else
					{
						resources = resources.replace(",", "|");
						results = (influxDB.query(new Query("SELECT * FROM "+measurement+" WHERE "+resourceName+" =~ /"+resources+"/ AND time >= '"+startDate+"'")).getResults()) ;
					}
				}else
				{
					if(measurement == "null") {
						measurement = "";
						measurements = influxDB.query(new Query("SHOW MEASUREMENTS")).getResults();
						for(int z = 0; z < measurements.get(0).getSeries().get(0).getValues().size(); z++) 
						{
							measurement += ((measurements.get(0).getSeries().get(0).getValues().get(z).get(0)) + ","); 
						}
						measurement = measurement.substring(0, measurement.length() - 1);
					}
					if(resources == "null") 
					{
						results = (influxDB.query(new Query("SELECT * FROM "+measurement+" WHERE time >= '"+startDate+"' AND time <= '"+endDate+"'")).getResults());
						
					}else 
					{
						resources = resources.replace(",", "|");
						results = (influxDB.query(new Query("SELECT * FROM "+measurement+" WHERE "+resourceName+" =~ /"+resources+"/ AND time >= '"+startDate+"' AND time <= '"+endDate+"'")).getResults());
					}
				}
				
				if(results.get(0).getSeries() == null) {
					return ("No se encuentran resultados");
				}else {
					for (int z = 0; z<results.get(0).getSeries().size(); z++) {
						
						for (int i =0; i<results.get(0).getSeries().get(z).getValues().size();i++) 
						{
							JsonObject element = new JsonObject();
							for(int j = 0; j<results.get(0).getSeries().get(0).getColumns().size();j++) 
							{
								if(results.get(0).getSeries().get(0).getValues().get(i).get(j) != null) 
								{
									element.addProperty(results.get(0).getSeries().get(0).getColumns().get(j), results.get(0).getSeries().get(0).getValues().get(i).get(j).toString());
							
								}
							}
						elements.add(element);
						}
						root.add(results.get(0).getSeries().get(z).getName()+" measurements", elements);
					}
					result = gson.toJson(root);
					/**    Show json result     **/
	//				System.out.println(result);		
					/**    Show json result     **/	
				}
				return result;
				
			}else 
			{
				scenarioValid = false;
				throw new WebApplicationException();
			}		
			/**             DataBase Validation and DB query              **/	
		
		}catch (Exception e) 
		{
//			log.error("Error when trying to create Json from query results, exception: "+ e );	
			if(!scenarioValid) 
			{
				LogFile.getInstance().warn(getClass(), e.toString()+", not a valid scenario");
				root.addProperty("Fail",e.toString()+", not a valid scenario");
//				log.error("Error when trying to validate the scenario, exception: "+ e );	
				result = gson.toJson(root);
				return result;
			}
			else
			{
				LogFile.getInstance().warn(getClass(), "Json builder fail, no results found");
				root.addProperty("Empty","Json builder fail, no results found");
//				log.error("Error when trying to create Json from query results, exception: "+ e );	
				result = gson.toJson(root);
				return result;
			}
		}
	}

}
