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
import java.util.ArrayList;
import java.util.Collections;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.glassfish.grizzly.http.server.Request;

import com.fasterxml.jackson.core.JsonParseException;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;

import afarcloud.nrdb.services.rest.store.*;
import afarcloud.nrdb.util.GlobalParameters;
import afarcloud.nrdb.util.LogFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Path("/store/")
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2020-01-30T12:25:59.595Z[GMT]")
public class PostDataManagerApi{
	
//	private static final Logger log = Logger.getLogger(PostDataManagerApi.class);
	public static final URI docsUri=URI.create(Main.BASE_URI+"dataAccessManager/");
	protected final Response invalidJsonException = Response.status(405).entity("405: \"Invalid input: not AFarCloud-compliant\". For more information, please refer to the API documentation: "+ docsUri).build();
	protected final Response invalidParsedJsonException = Response.status(405).entity("405: \"Invalid input: The json can not be parsed\". For more information, please refer to the API documentation: "+ docsUri).build();
	protected final Response notaJsonException =  Response.status(415).entity("415: \"Invalid input: not a JSON\". For more information, please refer to the API documentation: "+ docsUri).build();
	protected final Response invalidScenarioException = Response.status(405).entity("405: \"Invalid input: not AFarCloud-scenario\". For more information, please refer to the API documentation: "+ docsUri).build();
	private static int i = 0;

	
//	 This method checks which resource is being called by using @Context annotation, and selects...
//	 the corresponding schema in order to validate the body of the request.	
//   Method to validate Json.
	private boolean validateJson(String s, ArrayList<Schema> schemas) throws ProcessingException, IOException {
		
//		Reorder the collection attending to the demand.
		i++;
		if(i>=100) 
		{
			Collections.sort(schemas);
			
			i=0;
//			System.out.println(i + " veces se ha validado!!!!!!!");
//			System.out.println("Array ordenado por uso");
			for (int i = 0; i < schemas.size()-1; i++) {
	            System.out.println((i+1) + ". " + schemas.get(i).getName() + " - Uso: " + schemas.get(i).getUso());
	        }
			schemas.forEach((n) -> n.setUso(0));
		}
		
	  	for (Schema i:schemas) {
	//		Validates against schemas.
			if (ValidationUtils.isJsonValid(i.getSchema(), s))
			{
				i.setUso(i.getUso()+1); 
				i.getName();
				return true;
			}	
		}
	  	return false;
	}
	
	
	private String getRemoteAddress(Request request) {
	String ipAddress = request.getHeader("X-FORWARDED-FOR");  
	   if (ipAddress == null) {  
	       ipAddress = request.getRemoteAddr();  
	   } 
	   return ipAddress;
	}
		
	@Path("/collar")		
	@POST  
    @Consumes({ "application/json" })    
    @Operation(summary = "Add a new measure/s from a collar to the Data Base", description = "", tags={ "collar" })
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "Successful Operation"),
        
        @ApiResponse(responseCode = "405", description = "Invalid input: not AFarCloud-compliant"),
        
        @ApiResponse(responseCode = "415", description = "Invalid input: not a JSON"),
        
        @ApiResponse(responseCode = "5XX", description = "Unexpected error") })
    
    	public Response addCollar(String s, @Context UriInfo uriInfo,@Context Request request) throws Exception, NRDBJSONException  {
		
		
        try { if (validateJson(s, Schema.schemasCollar)) {
        	
//        	LogFile.getInstance().debug(getClass(), "SessionID: "+request.getSession().getIdInternal()+" IP: "+ getRemoteAddress(request)+" 200: Successful request on: Collar schemas");
//	       	 log.info("SessionID: "+request.getSession().getIdInternal()+" IP: "+ getRemoteAddress(request)+" Successful request on: Collar schemas" );
        	
	       	 /**		 Parse Json and send Data		 **/
	       	  
		        	  afarcloud.nrdb.services.rest.store.JSONParserCollar parser = new JSONParserCollar();
		        	  DataLayer injection = new DataLayer();
		        	  injection.injectData(parser, s);
//		        	  injectData2Influx(parser, s);	        	  
	       	  
	       	  /**		 Parse Json and send Data		 **/
	        LogFile.getInstance().debug(getClass(), "SessionID: "+request.getSession().getIdInternal()+" IP: "+ getRemoteAddress(request)+" 200: Successful request on: Collar schemas; Resource ID: "+ parser.getResourceId());
		        	  
	       	return Response.status(200).entity("200: \"Successful operation\". \nFor more information, please refer to the API documentation: "+ docsUri +"\nRequest ID: "+request.getSession().getIdInternal()).build();
	       	
	         }
	         else 
	         {
	        	 String resourceId = "";
	        	 int p1 = s.indexOf("\"resourceId\":"); 
	        	 int p2 = s.indexOf('\"', p1+16);
	        	 resourceId = s.substring(p1+14, p2);
	        	 LogFile.getInstance().error(getClass(), "SessionID: "+request.getSession().getIdInternal()+" IP: "+ getRemoteAddress(request)+" Invalid Json Exception, not valid against schemas; ResourceId: "+ resourceId);
//	       	  log.error("SessionID: "+request.getSession().getIdInternal()+" IP: "+ getRemoteAddress(request)+" Invalid Json Exception");
	         }
	         throw new WebApplicationException(invalidJsonException);
	      
	        }
	        catch(JsonParseException ex){
	       	
	        	 String resourceId = "";
	        	 int p1 = s.indexOf("\"resourceId\":"); 
	        	 int p2 = s.indexOf('\"', p1+16);
	        	 resourceId = s.substring(p1+14, p2);
	        	LogFile.getInstance().error(getClass(), "SessionID: "+request.getSession().getIdInternal()+" IP: "+ getRemoteAddress(request)+" Invalid Json Exception "+ex +"; ResourceId: "+resourceId);
//		       	log.error("SessionID: "+request.getSession().getIdInternal()+" IP: "+ getRemoteAddress(request)+" Invalid Json Exception "+ex);
				
		        final Response detailedException = Response.status(405).entity(invalidJsonException.getEntity().toString()+"\nError: "+ex).build();
		        throw new WebApplicationException(detailedException);
	        }catch (NRDBJSONException e) {
	        	
	        	 String resourceId = "";
	        	 int p1 = s.indexOf("\"resourceId\":"); 
	        	 int p2 = s.indexOf('\"', p1+16);
	        	 resourceId = s.substring(p1+14, p2);
	        	LogFile.getInstance().warn(getClass(),"SessionID: "+request.getSession().getIdInternal()+" IP: "+ getRemoteAddress(request)+" Parser exception: "+e +"; ResourceId: "+resourceId);
//				log.error("SessionID: "+request.getSession().getIdInternal()+" IP: "+ getRemoteAddress(request)+" Parser exception: "+e);
				
				final Response detailedException = Response.status(405).entity(invalidParsedJsonException.getEntity().toString()+"\nError: "+e).build();
				throw new WebApplicationException(detailedException);
			}
		 
	 }
	
	
	@Path("/vehicle")		
	@POST  
    @Consumes({ "application/json" })    
    @Operation(summary = "Add a new vehicle measure/s to the Data Base", description = "", tags={ "vehicle" })
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "Successful Operation"),
        
        @ApiResponse(responseCode = "405", description = "Invalid input: not AFarCloud-compliant"),
        
        @ApiResponse(responseCode = "415", description = "Invalid input: not a JSON"),
        
        @ApiResponse(responseCode = "5XX", description = "Unexpected error") })
    
    	public Response addVehicle(String s, @Context UriInfo uriInfo,@Context Request request) throws Exception, NRDBJSONException  {
		
        try { if (validateJson(s, Schema.schemasStateVector)) {
        	
//        	LogFile.getInstance().debug(getClass(), "SessionID: "+request.getSession().getIdInternal()+" IP: "+ getRemoteAddress(request)+" 200: Successful request on: Collar schemas");
//	       	 log.info("SessionID: "+request.getSession().getIdInternal()+" IP: "+ getRemoteAddress(request)+" Successful request on: Collar schemas" );
        	
	       	 /**		 Parse Json and send Data		 **/
	       	  
        			  afarcloud.nrdb.services.rest.store.JSONParserVehicle parser = new JSONParserVehicle(GlobalParameters.getInstance().getParameter(GlobalParameters.AFC_SCENARIO));
		        	  DataLayer injection = new DataLayer();
		        	  injection.injectData(parser, s);
//		        	  injectData2Influx(parser, s);	        	  
	       	  
	       	  /**		 Parse Json and send Data		 **/
        	  String resourceId = "";
	        	 int p1 = s.indexOf("\"vehicleId\":"); 
//				    	 int p2 = s.indexOf('\"', p1+11);
//				    	 int p3 = s.indexOf(',', p1+11);
		    	 resourceId = s.substring(p1+12, p1+14);
//				    	  if(p3<p2) {
//				    		  resourceId = s.substring(p1+11, p3);
//				    	  }else if(p2<p3) {
//				    		  resourceId = s.substring(p1+11, p2);
//				    	  }
	    	  
	        LogFile.getInstance().debug(getClass(), "SessionID: "+request.getSession().getIdInternal()+" IP: "+ getRemoteAddress(request)+" 200: Successful request on: vehicle schemas; Vehicle ID: "+resourceId);
		        	  
	       	return Response.status(200).entity("200: \"Successful operation\". \nFor more information, please refer to the API documentation: "+ docsUri +"\nRequest ID: "+request.getSession().getIdInternal()).build();
	       	
	         }
	         else 
	         {
	        	 String resourceId = "";
	        	 int p1 = s.indexOf("\"vehicleId\":"); 
//		    	 int p2 = s.indexOf('\"', p1+11);
//		    	 int p3 = s.indexOf(',', p1+11);
		    	 resourceId = s.substring(p1+12, p1+14);
//		    	  if(p3<p2) {
//		    		  resourceId = s.substring(p1+11, p3);
//		    	  }else if(p2<p3) {
//		    		  resourceId = s.substring(p1+11, p2);
//		    	  }
	        	 LogFile.getInstance().error(getClass(), "SessionID: "+request.getSession().getIdInternal()+" IP: "+ getRemoteAddress(request)+" Invalid Json Exception, not valid against schemas; VehicleId: "+resourceId);
//	       	  log.error("SessionID: "+request.getSession().getIdInternal()+" IP: "+ getRemoteAddress(request)+" Invalid Json Exception");
	         }
	         throw new WebApplicationException(invalidJsonException);
	      
	        }
	        catch(JsonParseException ex){
	       	
	        	 String resourceId = "";
	        	 int p1 = s.indexOf("\"vehicleId\":"); 
//		    	 int p2 = s.indexOf('\"', p1+11);
//		    	 int p3 = s.indexOf(',', p1+11);
		    	 resourceId = s.substring(p1+12, p1+14);
//		    	  if(p3<p2) {
//		    		  resourceId = s.substring(p1+11, p3);
//		    	  }else if(p2<p3) {
//		    		  resourceId = s.substring(p1+11, p2);
//		    	  }
	        	LogFile.getInstance().error(getClass(), "SessionID: "+request.getSession().getIdInternal()+" IP: "+ getRemoteAddress(request)+" Invalid Json Exception "+ex +"; VehicleId: "+resourceId);
//		       	log.error("SessionID: "+request.getSession().getIdInternal()+" IP: "+ getRemoteAddress(request)+" Invalid Json Exception "+ex);
				
		        final Response detailedException = Response.status(405).entity(invalidJsonException.getEntity().toString()+"\nError: "+ex).build();
		        throw new WebApplicationException(detailedException);
	        }catch (NRDBJSONException e) {
	        	
	        	 String resourceId = "";
	        	 int p1 = s.indexOf("\"vehicleId\":"); 
//		    	 int p2 = s.indexOf('\"', p1+11);
//		    	 int p3 = s.indexOf(',', p1+11);
		    	 resourceId = s.substring(p1+12, p1+14);
//		    	  if(p3<p2) {
//		    		  resourceId = s.substring(p1+11, p3);
//		    	  }else if(p2<p3) {
//		    		  resourceId = s.substring(p1+11, p2);
//		    	  }
	        	LogFile.getInstance().warn(getClass(),"SessionID: "+request.getSession().getIdInternal()+" IP: "+ getRemoteAddress(request)+" Parser exception: "+e +"; VehicleId: "+resourceId);
//				log.error("SessionID: "+request.getSession().getIdInternal()+" IP: "+ getRemoteAddress(request)+" Parser exception: "+e);
				
				final Response detailedException = Response.status(405).entity(invalidParsedJsonException.getEntity().toString()+"\nError: "+e).build();
				throw new WebApplicationException(detailedException);
			}
		 
	 }
	
	
	@Path("/region")		
	@POST  
    @Consumes({ "application/json" })    
    @Operation(summary = "Add a new region measure/s to the Data Base", description = "", tags={ "region" })
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "Successful Operation"),
        
        @ApiResponse(responseCode = "405", description = "Invalid input: not AFarCloud-compliant"),
        
        @ApiResponse(responseCode = "415", description = "Invalid input: not a JSON"),
        
        @ApiResponse(responseCode = "5XX", description = "Unexpected error") })
    
    	public Response addRegion(String s, @Context UriInfo uriInfo,@Context Request request) throws Exception, NRDBJSONException  {
		
	        try { if (validateJson(s, Schema.schemasRegion)) {
	
	        	LogFile.getInstance().debug(getClass(), "SessionID: "+request.getSession().getIdInternal()+" IP: "+ getRemoteAddress(request)+" 200: Successful request on: Region schemas");
//	       	 log.info("SessionID: "+request.getSession().getIdInternal()+" IP: "+ getRemoteAddress(request)+" Successful request on: Region schemas" );
	
	       	 /**		 Parse Json and send Data		 **/
	       	  
	        		  afarcloud.nrdb.services.rest.store.JSONParserRegion parser = new JSONParserRegion();
		        	  DataLayer injection = new DataLayer();
		        	  injection.injectData(parser, s);  
	       	  
	       	  /**		 Parse Json and send Data		 **/
		        
	       	return Response.status(200).entity("200: \"Successful operation\". \nFor more information, please refer to the API documentation: "+ docsUri +"\nRequest ID: "+request.getSession().getIdInternal()).build();
	       	
	         }
	         else 
	         {
	        	 String resourceId = "";
	        	 int p1 = s.indexOf("\"resourceId\":"); 
	        	 int p2 = s.indexOf('\"', p1+16);
	        	 resourceId = s.substring(p1+14, p2);
	        	 LogFile.getInstance().error(getClass(), "SessionID: "+request.getSession().getIdInternal()+" IP: "+ getRemoteAddress(request)+" Invalid Json Exception; resourceID: "+resourceId);
//	       	  log.error("SessionID: "+request.getSession().getIdInternal()+" IP: "+ getRemoteAddress(request)+" Invalid Json Exception");
	         }
	         throw new WebApplicationException(invalidJsonException);
	      
	        }
	        catch(JsonParseException ex){
	        	 String resourceId = "";
	        	 int p1 = s.indexOf("\"resourceId\":"); 
	        	 int p2 = s.indexOf('\"', p1+16);
	        	 resourceId = s.substring(p1+16, p2);
	        	LogFile.getInstance().error(getClass(),"SessionID: "+request.getSession().getIdInternal()+" IP: "+ getRemoteAddress(request)+" Invalid Json Exception "+ex+"; ResourceId: "+resourceId);
//	       	 	log.error("SessionID: "+request.getSession().getIdInternal()+" IP: "+ getRemoteAddress(request)+" Invalid Json Exception "+ex);
			
		        final Response detailedException = Response.status(405).entity(invalidJsonException.getEntity().toString()+"\nError: "+ex).build();
		        throw new WebApplicationException(detailedException);
	        }catch (NRDBJSONException e) {
	        	 String resourceId = "";
	        	 int p1 = s.indexOf("\"resourceId\":"); 
	        	 int p2 = s.indexOf('\"', p1+16);
	        	 resourceId = s.substring(p1+14, p2);
	        	LogFile.getInstance().warn(getClass(),"SessionID: "+request.getSession().getIdInternal()+" IP: "+ getRemoteAddress(request)+" Parser exception: "+e+"; ResourceId: "+resourceId);
//	        	log.error("SessionID: "+request.getSession().getIdInternal()+" IP: "+ getRemoteAddress(request)+" Parser exception: "+e);
				
				final Response detailedException = Response.status(405).entity(invalidParsedJsonException.getEntity().toString()+"\nError: "+e).build();
				throw new WebApplicationException(detailedException);
			}
		 
	 }	
		
		
		
	@Path("/measures")
	@POST  
    @Consumes({ "application/json" })    
    @Operation(summary = "Add a new measure/s to the Data Base", description = "", tags={ "measures" })
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "Successful Operation"),
        
        @ApiResponse(responseCode = "405", description = "Invalid input: not AFarCloud-compliant"),
        
        @ApiResponse(responseCode = "415", description = "Invalid input: not a JSON"),
        
        @ApiResponse(responseCode = "5XX", description = "Unexpected error") })
    
    	public Response addMeasures(String s, @Context UriInfo uriInfo,@Context Request request) throws Exception, NRDBJSONException  {
		
	         try { if (validateJson(s, Schema.schemasMeasures)) {

//	        	 LogFile.getInstance().debug(getClass(), "SessionID: "+request.getSession().getIdInternal()+" IP: "+ getRemoteAddress(request)+" 200: Successful request on: Measures schema");
//	        	 log.info("SessionID: "+request.getSession().getIdInternal()+" IP: "+ getRemoteAddress(request)+" Successful request on: Measures schema" );
	 
	        	 /**		 Parse Json and send Data		 **/
	        	 
	        	 	  afarcloud.nrdb.services.rest.store.JSONParserOM parser = new JSONParserOM();
		        	  DataLayer injection = new DataLayer();
		        	  injection.injectData(parser, s);  
		        	  
	        	  
	        	  /**		 Parse Json and send Data		 **/
//		        	  System.out.println(request);
		        LogFile.getInstance().debug(getClass(), "SessionID: "+request.getSession().getIdInternal()+" IP: "+ getRemoteAddress(request)+" 200: Successful request on: Measures schemas; Resource ID: "+parser.getResourceId());
		  		    
	        	return Response.status(200).entity("200: \"Successful operation\". \nFor more information, please refer to the API documentation: "+ docsUri +"\nRequest ID: "+request.getSession().getIdInternal()).build();
	        	
	          }
	          else 
	          {
	        	  String resourceId = "";
	        	  int p1 = s.indexOf("\"resourceId\":"); 
	        	  int p2 = s.indexOf('\"', p1+16);
	        	  resourceId = s.substring(p1+14, p2);
	        	  LogFile.getInstance().error(getClass(),"SessionID: "+request.getSession().getIdInternal()+" IP: "+ getRemoteAddress(request)+" Invalid Json Exception, not valid against schemas; ResourceId: "+resourceId);
//	        	  log.error("SessionID: "+request.getSession().getIdInternal()+" IP: "+ getRemoteAddress(request)+" Invalid Json Exception");
	          }
	          throw new WebApplicationException(invalidJsonException);
	       
	         }
	         catch(JsonParseException ex){
	        	 
	        	  String resourceId = "";
	        	  int p1 = s.indexOf("\"resourceId\":"); 
	        	  int p2 = s.indexOf('\"', p1+16);
	        	  resourceId = s.substring(p1+14, p2);
	        	LogFile.getInstance().error(getClass(),"SessionID: "+request.getSession().getIdInternal()+" IP: "+ getRemoteAddress(request)+" Invalid Json Exception "+ex+"; ResourceId: "+resourceId);
//	        	log.error("SessionID: "+request.getSession().getIdInternal()+" IP: "+ getRemoteAddress(request)+" Invalid Json Exception "+ex);
    		
		        final Response detailedException = Response.status(405).entity(invalidJsonException.getEntity().toString()+"\nError: "+ex).build();
		        throw new WebApplicationException(detailedException);
		        
			}catch (NRDBJSONException e) {
	        	  String resourceId = "";
	        	  int p1 = s.indexOf("\"resourceId\":"); 
	        	  int p2 = s.indexOf('\"', p1+16);
	        	  resourceId = s.substring(p1+14, p2);
				LogFile.getInstance().warn(getClass(),"SessionID: "+request.getSession().getIdInternal()+" IP: "+ getRemoteAddress(request)+" Parser exception: "+e+"; ResourceId: "+resourceId);
//				log.error("SessionID: "+request.getSession().getIdInternal()+" IP: "+ getRemoteAddress(request)+" Parser exception: "+e);
				
				final Response detailedException = Response.status(405).entity(invalidParsedJsonException.getEntity().toString()+"\nError: "+e).build();
				throw new WebApplicationException(detailedException);
			}
	}	
	
}

