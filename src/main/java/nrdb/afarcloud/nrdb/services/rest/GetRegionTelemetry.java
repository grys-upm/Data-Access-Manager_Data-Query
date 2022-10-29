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
import afarcloud.nrdb.util.GlobalParameters;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;


@Path("/getRegionTelemetry/")
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen")
public class GetRegionTelemetry {

	private String getRemoteAddress(Request request) {
		String ipAddress = request.getHeader("X-FORWARDED-FOR");  
		   if (ipAddress == null) {  
		       ipAddress = request.getRemoteAddr();  
		   } 
		   return ipAddress;
	}
    
    @GET
    @Path("/latest/")
    @Produces({ "application/json", "application/csv" })
    @Operation(summary = "Retrieves the latest telemetry from regions that meet specific constraint", description = "", tags={ "Query region telemetry" })
    @ApiResponses(value = { 
    		@ApiResponse(responseCode = "200", description = "Successful Operation"),
    		@ApiResponse(responseCode = "205", description = "Invalid input"),    		
    		@ApiResponse(responseCode = "5XX", description = "Unexpected error")
    		})
    
    public Response getLatestRegionTelemetry(    			
    			@Parameter(in = ParameterIn.QUERY, description = "Number of telemetries to retrieve. Default 1, maximum defined by services provider.", required = true )
    				@QueryParam(Constants.SRV_PARAM_LIMIT) int nLimit,
    		
    			@Parameter(in = ParameterIn.QUERY, description = "List (comma separated values) of application domain to retrieve." , required = false) 
    				@QueryParam(Constants.SRV_PARAM_SERVICES) String sServices,
    			@Parameter(in = ParameterIn.QUERY, description = "List (comma separated values) of providers to retrieve." , required = false) 
    				@QueryParam(Constants.SRV_PARAM_PROVIDERS) String sProviders,
    				
       			@Parameter(in = ParameterIn.QUERY, description = "List (comma separated values) of the 'observedProperty' to retrieve." , required = false) 
    				@QueryParam(Constants.SRV_PARAM_OBSERVED_PROPERTIES) String sObsProperties,
    				
    			@Parameter(in = ParameterIn.QUERY, description = "List (comma separated values) of results. Valid values: Weed, DeadPlant, WaterStress" , required = false) 
    				@QueryParam(Constants.SRV_PARAM_REGION_RESULTS) String sResults,
    			    				
    			@Context UriInfo uriInfo, @Context Request request
    			) throws NotFoundException {    	
    	 
    		if (nLimit<1 || nLimit > GlobalParameters.getInstance().getIntObservations(GlobalParameters.RS_RESPONSE_MAX_OBSERVATIONS, 100) ) {
    			nLimit=1;
    		}
    		
    		HashMap<String, String> hConditions = new HashMap<String, String>();
    		
    		hConditions.put(Constants.SRV_PARAM_LIMIT, String.valueOf(nLimit) );
    		
    		
    		if (sServices!=null && !sServices.isEmpty())
    			hConditions.put(Constants.SRV_PARAM_SERVICES, sServices);
    		
    		if (sObsProperties!=null && !sObsProperties.isEmpty())
    			hConditions.put(Constants.SRV_PARAM_OBSERVED_PROPERTIES, sObsProperties);
    		
    		if (sProviders!=null && !sProviders.isEmpty())
    			hConditions.put(Constants.SRV_PARAM_PROVIDERS, sProviders);

    		if (sResults!=null && !sResults.isEmpty()) {
    			String sResultsPredicate=_listResultsToPredicate(sResults);
    			if (!sResultsPredicate.isEmpty()) {
    				hConditions.put(Constants.SRV_PARAM_REGION_RESULTS, sResultsPredicate);
    			}    			
    		}
    		
    		return TelemetryExtractor.getRegionTelemetry(request, hConditions);

    }

    private String _listResultsToPredicate (String sResults) {
		String sAux="";
		String[] aAux = sResults.split(",");
		for (int i=0; i<aAux.length; i++) {
			if (Constants.SRV_LIST_REGION_RESULTS.contains(aAux[i].trim())) {
				if(!sAux.isEmpty()) {
					sAux+= " OR ";
				}
				sAux+= " \"result\"='" + aAux[i].trim() +"'";
			}
		}
    	
		return sAux;
    }



    @GET
    @Path("/historic/")
    @Produces({ "application/json", "application/csv" })
    @Operation(summary = "Retrieves the telemetry in a time interval form regions that meet specific constraint. If not filter conditions are specified, retrieve 100 observations maximun per region.", description = "", tags={ "Query region telemetry interval" })
    @ApiResponses(value = { 
    		@ApiResponse(responseCode = "200", description = "Successful Operation"),
    		@ApiResponse(responseCode = "205", description = "Invalid input"),    		
    		@ApiResponse(responseCode = "5XX", description = "Unexpected error")
    		})
    
    public Response getHistoricRegionTelemetry(    			
    			@Parameter(in = ParameterIn.QUERY, description = "The start datetime of the interval time. Valid formats: yyyy-MM-dd HH:mm and yyyy-MM-dd" , required = true)
    				@QueryParam(Constants.SRV_PARAM_START_TIME) String sStartTime,
       			@Parameter(in = ParameterIn.QUERY, description = "The end datetime of the interval time. Valid formats: yyyy-MM-dd HH:mm and yyyy-MM-dd" , required = false)
    				@QueryParam(Constants.SRV_PARAM_END_TIME) String sEndTime,
    				
       			@Parameter(in = ParameterIn.QUERY, description = "List (comma separated values) of application domain to retrieve." , required = false) 
    				@QueryParam(Constants.SRV_PARAM_SERVICES) String sServices,
    			@Parameter(in = ParameterIn.QUERY, description = "List (comma separated values) of providers to retrieve." , required = false) 
    				@QueryParam(Constants.SRV_PARAM_PROVIDERS) String sProviders,
    				
       			@Parameter(in = ParameterIn.QUERY, description = "List (comma separated values) of the 'observedProperty' to retrieve." , required = false) 
    				@QueryParam(Constants.SRV_PARAM_OBSERVED_PROPERTIES) String sObsProperties,
    				
    			@Parameter(in = ParameterIn.QUERY, description = "List (comma separated values) of results. Valid values: Weed, DeadPlant, WaterStress" , required = false) 
    				@QueryParam(Constants.SRV_PARAM_REGION_RESULTS) String sResults,
    				       				
    			@Context UriInfo uriInfo, @Context Request request
    			) throws NotFoundException {    	

   		
    		HashMap<String, String> hConditions = new HashMap<String, String>();
    		
			/** Return the IP and the request ID of the session **/
			
			System.out.println("\nIP: "+ getRemoteAddress(request) +", Request ID: "+ request.getSession().getIdInternal());
			
			/** Return the IP and the request ID of the session **/
    		
    		// filter conditions
    		if (sServices!=null && !sServices.isEmpty())
    			hConditions.put(Constants.SRV_PARAM_SERVICES, sServices);
    		

    		if (sObsProperties!=null && !sObsProperties.isEmpty())
    			hConditions.put(Constants.SRV_PARAM_OBSERVED_PROPERTIES, sObsProperties);
    		
    		if (sProviders!=null && !sProviders.isEmpty())
    			hConditions.put(Constants.SRV_PARAM_PROVIDERS, sProviders);

    		if (sResults!=null && !sResults.isEmpty()) {
    			String sResultsPredicate=_listResultsToPredicate(sResults);
    			if (!sResultsPredicate.isEmpty()) {
    				hConditions.put(Constants.SRV_PARAM_REGION_RESULTS, sResultsPredicate);
    			}    			
    		}
    		
    		
    		// at last, one filter condition
    		if (hConditions.isEmpty()) {
    			hConditions.put(Constants.SRV_PARAM_LIMIT,
    							String.valueOf(GlobalParameters.getInstance().getIntObservations(GlobalParameters.RS_RESPONSE_MAX_OBSERVATIONS, 100))
    							);
    		}
    		
    		if (sStartTime!=null && !sStartTime.isEmpty())
    			hConditions.put(Constants.SRV_PARAM_START_TIME, sStartTime);

    		if (sEndTime!=null && !sEndTime.isEmpty())
    			hConditions.put(Constants.SRV_PARAM_END_TIME, sEndTime);
    		
    		String sQuery="";
    		
    		try {

    			sQuery = request.getDecodedRequestURI() +
						( (request.getQueryString()!=null) ?"?" +  request.getQueryString() :"");
				
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
    		
        		return TelemetryExtractor.getRegionTelemetry(request, hConditions);
        		
    		
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

