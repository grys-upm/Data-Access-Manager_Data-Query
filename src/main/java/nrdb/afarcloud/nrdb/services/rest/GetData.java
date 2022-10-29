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
import java.io.CharConversionException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;


import org.glassfish.grizzly.http.server.Request;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult.Result;
import org.influxdb.dto.QueryResult.Series;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import afarcloud.nrdb.util.DataTypes;
import afarcloud.nrdb.util.GlobalParameters;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Path("/influx/")
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2020-02-30T12:25:59.595Z[GMT]")
public class GetData {

	protected static final URI docsUri=URI.create(Main.BASE_URI+"influx/");


	
    @GET
    @Path("/series/")
    @Produces({ "application/json" })
    @Operation(summary = "(Under development) ejecución de una query con varias series de resultado", description = "", tags={ "Query" })
    @ApiResponses(value = { 
    		@ApiResponse(responseCode = "200", description = "Successful Operation"),
    		@ApiResponse(responseCode = "5XX", description = "Unexpected error")
    		})
    
    public Response getData( @Context UriInfo uriInfo,@Context Request request) throws NotFoundException {
    		//https://github.com/influxdata/influxdb-java/blob/master/src/main/java/org/influxdb/dto/QueryResult.java
    		// select max(*), min(*), mean(*) from /h2o_/
    		// DB:: NOAA_water_database
    		// 192.138.134.153
    		// port:8086
    	
    	
    		String response = new String();
    		try{
    			ObjectMapper oJSONMapper = new ObjectMapper();
    			oJSONMapper.enable(SerializationFeature.INDENT_OUTPUT);
    		
    			InfluxDB influxDB = InfluxDBFactory.connect("http://192.168.134.153:8086", "prob", "prob");
    			influxDB.setDatabase("NOAA_water_database");
    			List<Result> observations = influxDB.query(new Query("SELECT max(*), min(*), mean(*) from /h2o_/")).getResults();
    			    			
    			return Response.status(200).entity(oJSONMapper.writeValueAsString(observations)).build();
			} catch (WebApplicationException | JsonProcessingException e) {

				e.printStackTrace();
		    	return Response.status(405).entity(response).build();
			}    
    }

    @GET
    @Path("/prob/")
    @Produces({ "application/json" })
    @Operation(summary = "(Under development) ejecución de una query con varias series de resultado", description = "", tags={ "Query" })
    @ApiResponses(value = { 
    		@ApiResponse(responseCode = "200", description = "Successful Operation"),
    		@ApiResponse(responseCode = "5XX", description = "Unexpected error")
    		})
    
    public Response getProb( @Context UriInfo uriInfo,@Context Request request) throws NotFoundException {
    		//https://github.com/influxdata/influxdb-java/blob/master/src/main/java/org/influxdb/dto/QueryResult.java
    		// select max(*), min(*), mean(*) from /h2o_/
    		// DB:: NOAA_water_database
    		// 192.138.134.153
    		// port:8086
    	
    	
    		try{
    			ObjectMapper oJSONMapper = new ObjectMapper();
    			oJSONMapper.enable(SerializationFeature.INDENT_OUTPUT);
    		
    			InfluxDB influxDB = InfluxDBFactory.connect("http://192.168.134.153:8086", "prob", "prob");
    			influxDB.setDatabase("NOAA_water_database");
    			//List<Result> results = influxDB.query(new Query("SELECT max(*), min(*), mean(*) from /h2o_/")).getResults();
    			List<Result> results = influxDB.query(new Query("SELECT * from /h2o_/ order by time desc limit 5")).getResults();
    			
    			ObjectNode nodeResult = oJSONMapper.createObjectNode();
    			ArrayNode arrayMeasurements = oJSONMapper.createArrayNode();
    			
    			for(Result res:results) {    				
    				if (!res.hasError()) {
    					
    					for (Series serie: res.getSeries()) {
    						ObjectNode nodeMeasurement = oJSONMapper.createObjectNode();
    						nodeMeasurement.put("measurement", serie.getName());
    						
    						System.out.println("Serie:: " + serie.getName());
    						
    						ArrayNode arrayObservations = oJSONMapper.createArrayNode();
   							List<List<Object>> lPoints = serie.getValues();   							
   							for(List<Object> lPoint:lPoints) {
   								ObjectNode nodePoint = oJSONMapper.createObjectNode();
   	    						for (int i=0; i<serie.getColumns().size();i++) {
   	    							if (lPoint.get(i)!=null) {   	    				
   	    								nodePoint.put(serie.getColumns().get(i), lPoint.get(i).toString());
   	    								System.out.format("\n\t- %s: %s", serie.getColumns().get(i), lPoint.get(i).toString());
   	    							}
   	    						}
   	    						arrayObservations.add(nodePoint);
    								
   							}
   							nodeMeasurement.putPOJO("observations", arrayObservations);
   							arrayMeasurements.add(nodeMeasurement);
    					}    			
    				}
    			}
    			if (!arrayMeasurements.isEmpty()) {
    				nodeResult.putPOJO("results", arrayMeasurements);
    			}else {
    				nodeResult.put("results", "no data");
    			}
    			return Response.status(200).entity(oJSONMapper.writeValueAsString(nodeResult)).build();
    		
    			

			} catch (WebApplicationException | JsonProcessingException e) {

				e.printStackTrace();
		    	return Response.status(405).entity("error").build();
			}    
    }

    
    
    
    @GET
    @Path("/observations/measurements/")
    @Produces({ "application/json" })
    @Operation(summary = "cinco últimas observaciones asociadas de todas las measurement ", description = "", tags={ "Query" })
    @ApiResponses(value = { 
    		@ApiResponse(responseCode = "200", description = "Successful Operation"),
    		@ApiResponse(responseCode = "5XX", description = "Unexpected error")
    		})
    
    public Response getMeasurements(
    			
    			@Parameter(in = ParameterIn.QUERY, description = "type of measurement" , required = false) @QueryParam("type") String type,
    			@Context UriInfo uriInfo, @Context Request request
    			) throws NotFoundException {    	
    	
    		
    		HashMap<String, String> hConditions = new HashMap<String, String>();
    		if (type!=null)
    			hConditions.put("type", type);
    		
    		return _getMeasurements(request, null, hConditions);
    }

    @GET
    @Path("/observations/measurements/{measurement}")
    @Produces({ "application/json" })
    @Operation(summary = "cinco últimas observaciones asociadas de todas las measurement o a una ", description = "", tags={ "Query" })
    @ApiResponses(value = { 
    		@ApiResponse(responseCode = "200", description = "Successful Operation"),
    		@ApiResponse(responseCode = "5XX", description = "Unexpected error")
    		})
    
    public Response getMeasurement(
    			@Parameter(in = ParameterIn.PATH, description = "The desired measurement",required=true) @PathParam("measurement") String measurement,    		
    			@Context UriInfo uriInfo,@Context Request request
    			) throws NotFoundException {    	
    	
    		return _getMeasurements(request, measurement,null);
    }

    
    @GET
    @Path("/observations/resources/{entityName}")
    @Produces({ "application/json" })
    @Operation(summary = "cinco últimas observaciones asociadas de todas los recursos o a un entityName", description = "", tags={ "Query" })
    @ApiResponses(value = { 
    		@ApiResponse(responseCode = "200", description = "Successful Operation"),
	        @ApiResponse(responseCode = "415", description = "Invalid input: not a valid time param"),
    		@ApiResponse(responseCode = "5XX", description = "Unexpected error")
    		})
    
    public Response getTorcosObsByResource(
    				@Parameter(in = ParameterIn.PATH, description = "The desired Entity Name",required=true) @PathParam("entityName") String entityName,
    				@Parameter(in = ParameterIn.QUERY, description = "The start time delimiter for the time range desired",required = true) @QueryParam("start_time") String startTime,
    				@Context UriInfo uriInfo,@Context Request request) throws NotFoundException {
    	
    		try{
    			String sQuery =request.getDecodedRequestURI() + (request.getQueryString()!=null ?"?"+ request.getQueryString():"");
    			
    			if (startTime!=null) {
    				try {
						startTime = _stringDateToTimestamp(startTime);
					} catch (Exception e) {
						startTime=null;
					}finally {
						if (startTime==null || startTime.isEmpty())
							return Response.status(415).entity(_errorToJSON(sQuery, "Invalid start_time parameter")).build();

					}
    				
    				System.out.println("\nStart-Time:: " + startTime);
    			}
    			
    			InfluxDB influxDB = _influxConnect();    			
    			List<Result> results = influxDB.query(new Query("SELECT * from /^obs_/ where \"entityName\" = '"+ entityName+ "' AND time>'" + startTime + "' order by time desc")).getResults();
    			
    			return Response.status(200).entity(_resultsToJSON(
    													sQuery,
    													"observations",
    													"measurement",
    													results,
    													GlobalParameters.getInstance().getParameter(GlobalParameters.PREFIX_OBSERVATION )
    													)
    											  ).build();
    		
    			

			} catch (WebApplicationException | JsonProcessingException | CharConversionException e) {

				e.printStackTrace();
		    	return Response.status(405).entity("error").build();
			}    
    }

    /********************************************************************************************/
    private Response _getMeasurements(Request request, String measurement, HashMap<String,String> hConditions) {
    	try{
    		
			InfluxDB influxDB = _influxConnect();
			String sQuery = "SELECT * FROM " +
					( 
						(measurement==null || measurement.trim().isEmpty())?
								"/^"+ GlobalParameters.getInstance().getParameter(GlobalParameters.PREFIX_OBSERVATION ) +"/" :
									GlobalParameters.getInstance().getParameter(GlobalParameters.PREFIX_OBSERVATION ) + measurement
					);
			
			String sWhere="";
			String sAux;
			if (hConditions!=null && !hConditions.isEmpty()) {	

				for (Entry<String, String> cond : hConditions.entrySet()) {
					sAux="";
					if(cond.getKey().equals("type")) {
						sAux = _listStringToPredicate("type", cond.getValue());
					}	
					
					if (!sAux.isEmpty()) {
						if (!sWhere.isEmpty()) {
							sWhere+= " AND ";
						}
						sWhere+=" (" + sAux + ")"; 					
					}
				}
				
			}
			
			if (!sWhere.isEmpty()) {
				sQuery+= " WHERE " + sWhere; 
			}
			sQuery+=" ORDER BY time LIMIT 5";

			System.out.println("\n Query:: \n" +sQuery);
			
			//List<Result> results = influxDB.query(new Query("SELECT * from /obs_/ order by time desc limit 5")).getResults();
			List<Result> results = influxDB.query(new Query(sQuery)).getResults();
			
			
			return Response.status(200).entity(_resultsToJSON(request.getDecodedRequestURI(),
															 "observations",
															 "measurement",
															 results,
															 GlobalParameters.getInstance().getParameter(GlobalParameters.PREFIX_OBSERVATION )
															 )
											).build();
		
			

		} catch (WebApplicationException | JsonProcessingException | CharConversionException e) {

			e.printStackTrace();
	    	return Response.status(405).entity("error").build();
		}    
    }

    
    private String _listStringToPredicate (String sField, String sCondition) {
		String sAux="";
		String[] aAux = sCondition.split(",");
		for (int i=0; i<aAux.length; i++) {
			if(!sAux.isEmpty()) {
				sAux+= " OR ";
			}
			sAux+= " \""+ sField + "\"='" +aAux[i]+"'"; 
		}
    	
		return sAux;
    }
    
    /******** output json *************/
    
    private String _errorToJSON(String sQuery, String sMsg) throws JsonProcessingException {
		ObjectMapper oJSONMapper = new ObjectMapper();
		oJSONMapper.enable(SerializationFeature.INDENT_OUTPUT);
		ObjectNode nodeResult = oJSONMapper.createObjectNode();

		nodeResult.put("query", sQuery);
		nodeResult.put("error", sMsg);
		
		return oJSONMapper.writeValueAsString(nodeResult);	

    	
    }
    private String _resultsToJSON(String sQuery, String sRootElement, String sNameElement, List<Result> results, String sPrefix) throws JsonProcessingException {
		ObjectMapper oJSONMapper = new ObjectMapper();
		oJSONMapper.enable(SerializationFeature.INDENT_OUTPUT);
		ObjectNode nodeResult = oJSONMapper.createObjectNode();
		ArrayNode arrayMeasurements = oJSONMapper.createArrayNode();
		
		
		String sColumn;
		String sSerie;
		int nResults = 0;
		nodeResult.put("query", sQuery);
		for(Result res:results) {    				
			if (!res.hasError()) {
				
				for (Series serie: res.getSeries()) {
					ObjectNode nodeMeasurement = oJSONMapper.createObjectNode();
					
					sSerie =(sPrefix!=null)?serie.getName().replaceAll("^"+sPrefix, ""):serie.getName(); 
					nodeMeasurement.put(sNameElement, sSerie);
					
					//System.out.println("Serie:: " + serie.getName());
					
					ArrayNode arrayObservations = oJSONMapper.createArrayNode();
						List<List<Object>> lPoints = serie.getValues();   							
						for(List<Object> lPoint:lPoints) {
							ObjectNode nodePoint = oJSONMapper.createObjectNode();
   						for (int i=0; i<serie.getColumns().size();i++) {
   							if (lPoint.get(i)!=null) { 
   								//exclude duplicated columnas: similar field name and tag name
   							
   								sColumn = serie.getColumns().get(i).replaceAll("_\\d$","");
   								if (serie.getColumns().get(i)==sColumn || !serie.getColumns().contains(sColumn)) {
   									nodePoint.put(sColumn, lPoint.get(i).toString());
   								}
   								
   								//System.out.format("\n\t- %s: %s ==> %s", serie.getColumns().get(i), lPoint.get(i).toString(), sColumn );
   							}
   						}
   						nResults++;
   						arrayObservations.add(nodePoint);
							
						}
						nodeMeasurement.putPOJO(sRootElement, arrayObservations);
						arrayMeasurements.add(nodeMeasurement);
				}    			
			}
		}
		nodeResult.put("numResults", nResults);
		if (!arrayMeasurements.isEmpty()) {
			
			nodeResult.putPOJO("results", arrayMeasurements);
		}
		
		return oJSONMapper.writeValueAsString(nodeResult);	
    }

    
    /********* DB **************/
    private InfluxDB _influxConnect() {
		//InfluxDB influxDB = InfluxDBFactory.connect("http://138.100.51.114:9091", "adminGRyS", "GRyS2020");
    	InfluxDB influxDB = InfluxDBFactory.connect(
    						GlobalParameters.getInstance().getParameter(GlobalParameters.DS_MySQL_SERVER)+":"+GlobalParameters.getInstance().getParameter(GlobalParameters.DS_IDB_PORT),
    						GlobalParameters.getInstance().getParameter(GlobalParameters.DS_IDB_USER),
    						GlobalParameters.getInstance().getParameter(GlobalParameters.DS_IDB_PASSWORD)
    			);
    	
		influxDB.setDatabase(GlobalParameters.getInstance().getParameter(GlobalParameters.AFC_SCENARIO));
		return influxDB;
    }
    
    /** utils 
     * @throws Exception **/

    private String _stringDateToTimestamp(String sDate) throws Exception {
    	SimpleDateFormat dateDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    	//SimpleDateFormat dateDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	
        //dateDF.setTimeZone(TimeZone.getTimeZone("UTC"));
        
    	java.util.Date dDate = DataTypes.dGetTimestamp(sDate);
    	
    	
    	if (dDate!= null)
    		return dateDF.format(dDate);
    	
    	return null;
    	
    }
}

