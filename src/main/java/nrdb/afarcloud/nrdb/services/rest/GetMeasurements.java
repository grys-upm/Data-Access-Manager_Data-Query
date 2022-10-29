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

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;


import org.glassfish.grizzly.http.server.Request;
import com.fasterxml.jackson.core.JsonProcessingException;

import afarcloud.nrdb.dam.SerializationToJSON;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;


@Path("/getMeasurements/")
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen")
public class GetMeasurements {

    
    @GET
    @Produces({ "application/json" })
    @Operation(summary = "Returns a list of measurements for the database in a time interval. If not time interval is specified all measurements are returned.", description = "", tags={ "Schema-measurement" })
    @ApiResponses(value = { 
    		@ApiResponse(responseCode = "200", description = "Successful Operation"),
    		@ApiResponse(responseCode = "205", description = "Invalid input"),    		
    		@ApiResponse(responseCode = "5XX", description = "Unexpected error")
    		})
    
    public Response getMeasurements(    			
    				@Context UriInfo uriInfo, @Context Request request
    			) throws NotFoundException {    	

   		    		
    		String sQuery="";
    		
    		try {
    			sQuery = request.getDecodedRequestURI() +
						( (request.getQueryString()!=null) ?"?" +  request.getQueryString() :"");
    					
        		return TelemetryExtractor.getSchemaMeasurements(request, null);        		
    		
			} catch (Exception e) {
				// TODO Auto-generated catch block
					try {
						return Response.status(200).entity(SerializationToJSON.errorToJSON(
									sQuery,
									null,
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

