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

import java.io.File;
import java.util.Scanner;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.glassfish.grizzly.http.server.Request;

import io.swagger.v3.oas.annotations.Operation;

@Path("/conf/")
public class GetLogs {

	private final static File dir = new File("src/log");
//	private final static File dir = new File("/home/msaneme/dataAccessManager/logs");
	
    @GET
    @Path("/logs")
    @Produces({ "application/json" })
    @Operation(summary = "Check the latest logs", description = "", tags={ "Logs" })
    
    public Response getLogs(@Context UriInfo uriInfo,@Context Request request) throws NotFoundException {
		
		String response = new String();
		
    	 try {
    	    File listFile[] = dir.listFiles();
    		for(int i = 0; i<listFile.length; i++) {
	             Scanner input = new Scanner(new File(dir+"/"+listFile[i].getName()));
	             while (input.hasNextLine()) {
	                 String line = input.nextLine();
	                 response += (line + "\n");
	             }
	             input.close();
    		}

         } catch (Exception ex) {
             ex.printStackTrace();
         }

    	
    	return Response.status(200).entity(response).build();
    		
    }
    
}
