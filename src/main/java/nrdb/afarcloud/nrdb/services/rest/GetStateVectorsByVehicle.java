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
import java.util.HashMap;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;


import org.glassfish.grizzly.http.server.Request;

import com.fasterxml.jackson.core.JsonProcessingException;

import afarcloud.nrdb.config.Constants;
import afarcloud.nrdb.dam.SerializationToJSON;
import afarcloud.nrdb.util.DataTypes;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;


@Path("/getStateVectorsByVehicle/")
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen")
public class GetStateVectorsByVehicle {

	private String getRemoteAddress(Request request) {
		String ipAddress = request.getHeader("X-FORWARDED-FOR");  
		   if (ipAddress == null) {  
		       ipAddress = request.getRemoteAddr();  
		   } 
		   return ipAddress;
	}
    
    @GET
    @Path("/latest/")
    @Produces({ "application/json", "applications/csv" })
    @Operation(summary = "Retrieves the latest state vectors from vehicles that meet specific constraint", description = "", tags={ "Query vehicle's state vectors" })
    @ApiResponses(value = { 
    		@ApiResponse(responseCode = "200", description = "Successful Operation"),
    		@ApiResponse(responseCode = "205", description = "Invalid input"),    		
    		@ApiResponse(responseCode = "5XX", description = "Unexpected error")
    		})
    
    public Response getLatestStateVectorsByVehicle(    			
    			@Parameter(in = ParameterIn.QUERY, description = "Number of state vectors to retrieve by vehicle. Default 1, maximum defined by services provider.", required = true )
    				@QueryParam(Constants.SRV_PARAM_LIMIT) int nLimit,
    		
        		@Parameter(in = ParameterIn.QUERY, description = "List (comma separated values) of vehicle identifier (vehicleId) to retrieve." , required = false)
    				@QueryParam(Constants.SRV_PARAM_VEHICLE_ID) String sVehiclesId, 
    				
        		@Parameter(in = ParameterIn.QUERY, description = "Temporal order: ascending (ASC) or descending (DESC). Default value: DESC.", required = false )
    				@QueryParam(Constants.SRV_PARAM_ORDER) String sOrder,

    			// proximity parameter
           		@Parameter(in = ParameterIn.QUERY, description = "Proximity parameter - Centroid longitude: longitude of the centroid of the search area. To compute proximity based search, all proximity parameters are mandatories", required = false )
        				@QueryParam(Constants.SRV_PARAM_CENTROID_LONGITUDE) double lCentroide_long,
        		@Parameter(in = ParameterIn.QUERY, description = "Proximity parameter - Centroid latitude: latitude of the centroid of the search area. To compute proximity based search, all proximity parameters are mandatories", required = false )
        				@QueryParam(Constants.SRV_PARAM_CENTROID_LATITUDE) double lCentroide_lat,        		
        		@Parameter(in = ParameterIn.QUERY, description = "Proximity parameter - Search radius expressed in meters. Maximun 1000 meters. To compute proximity based search, all proximity parameters are mandatories", required = false )
        				@QueryParam(Constants.SRV_PARAM_RADIUS) int nRadius,        
        				
        		@Parameter(in = ParameterIn.QUERY, description = "Results in CSV format", required = false )
        				@QueryParam(Constants.SRV_PARAM_CSV) boolean csvFormat,

    			@Context UriInfo uriInfo, @Context Request request
    			) throws NotFoundException {    	
    	 
    		   		
    		HashMap<String, String> hConditions = TelemetryExtractor.getStateVectorConditions(nLimit, sVehiclesId, sOrder, lCentroide_long, lCentroide_lat, nRadius, csvFormat);
    		
    		return TelemetryExtractor.getStateVectorsByVehicle(request, hConditions);

    }


    @GET
    @Path("/historic/")
    @Produces({ "application/json", "application/csv" })
    @Operation(summary = "Retrieves a maximun of 100 state vector in a time interval from vehicles that meet specific constraint.", description = "", tags={ "Query vehicle's state vectors interval" })
    @ApiResponses(value = { 
    		@ApiResponse(responseCode = "200", description = "Successful Operation"),
    		@ApiResponse(responseCode = "205", description = "Invalid input"),    		
    		@ApiResponse(responseCode = "5XX", description = "Unexpected error")
    		})
    
    public Response getHistoricStateVectorsByVehicle(    			
    			@Parameter(in = ParameterIn.QUERY, description = "The start datetime of the interval time. Valid formats: yyyy-MM-dd HH:mm and yyyy-MM-dd" , required = true)
    				@QueryParam(Constants.SRV_PARAM_START_TIME) String sStartTime,
       			@Parameter(in = ParameterIn.QUERY, description = "The end datetime of the interval time. Valid formats: yyyy-MM-dd HH:mm and yyyy-MM-dd" , required = false)
    				@QueryParam(Constants.SRV_PARAM_END_TIME) String sEndTime,
    				
    				
          		@Parameter(in = ParameterIn.QUERY, description = "List (comma separated values) of vehicle identifier (vehicleId) to retrieve." , required = false)
        				@QueryParam(Constants.SRV_PARAM_VEHICLE_ID) String sVehiclesId, 

        		@Parameter(in = ParameterIn.QUERY, description = "Temporal order: ascending (ASC) or descending (DESC). Default value: DESC.", required = false )
        				@QueryParam(Constants.SRV_PARAM_ORDER) String sOrder,
        				
        		@Parameter(in = ParameterIn.QUERY, description = "Results in CSV format", required = false )
        				@QueryParam(Constants.SRV_PARAM_CSV) boolean csvFormat,
    				
    			@Context UriInfo uriInfo, @Context Request request
    			) throws NotFoundException {    	

   		
    		HashMap<String, String> hConditions = TelemetryExtractor.getHistoricalStateVectorConditions(sStartTime, sEndTime, sVehiclesId, sOrder, csvFormat);    		
    		
    		String sQuery="";
    		
    		try {

    			sQuery = request.getDecodedRequestURI() +
						( (request.getQueryString()!=null) ?"?" +  request.getQueryString() :"");
    			
    			/** Return the IP and the request ID of the session **/
    			
    			System.out.println("\nIP: "+ getRemoteAddress(request) +", Request ID: "+ request.getSession().getIdInternal());
    			
    			/** Return the IP and the request ID of the session **/
				
    			// startTime validation
    			if (sStartTime==null || sStartTime.isEmpty() || !DataTypes.bIsTimestamp(sStartTime)) {
					return Response.status(415).entity(SerializationToJSON.errorToJSON(
							sQuery,
							hConditions,
							"Invalid " + Constants.SRV_PARAM_START_TIME + " parameter")
							)
						.build();
    			}
    			
    			//endTime validation
    			if (sEndTime!=null && !DataTypes.bIsTimestamp(sEndTime)) {
					return Response.status(415).entity(SerializationToJSON.errorToJSON(
							sQuery,
							hConditions,
							"Invalid " + Constants.SRV_PARAM_END_TIME + " parameter")
							)
						.build();
    			}
    		
    			return TelemetryExtractor.getStateVectorsByVehicle(request, hConditions);
    		
			} catch (Exception e) {
				// TODO Auto-generated catch block
					try {
						return Response.status(200).entity(SerializationToJSON.errorToJSON(
									sQuery,
									hConditions,
									"bad formed")
									)
								.build();
					} catch (JsonProcessingException e1) {
						// TODO Auto-generated catch block
						return Response.status(500).entity("server error").build();
					}
			}
			
			
    }

}

