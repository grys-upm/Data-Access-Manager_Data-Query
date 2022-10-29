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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;

import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

import afarcloud.nrdb.util.*;

public class Schema implements Comparable <Schema> {
	
	public static JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
	public static ArrayList<String> schemasName = new ArrayList<>();
	public static ArrayList<Schema> schemasMeasures = new ArrayList<>();
	public static ArrayList<Schema> schemasCollar = new ArrayList<>();
	public static ArrayList<Schema> schemasVehicle = new ArrayList<>();
	public static ArrayList<Schema> schemasStateVector = new ArrayList<>();
	public static ArrayList<Schema> schemasRegion = new ArrayList<>();

	private JsonSchema schema;
    private int uso;
    private String name;

    protected Schema(JsonSchema schema, int uso, String name) {
         this.schema = schema;
         this.uso = uso;
         this.name = name;
         
    }
    public JsonSchema getSchema() {
	return schema;
	}
    public int getUso() {
	return uso;
	}
    public void setUso(int uso) {
	this.uso = uso;
	}
	public void setName(String name) {
	this.name = name;
	}
	public String getName() {
	return name;
	}
 /*   public String getType() {
    return type;
    }
    public void setType(String type) {
    this.type = type;
	}*/
    
	@Override
	public int compareTo(Schema o) {
	if (uso > o.uso) {
       return -1;
	   }
	   if (uso < o.uso) {
	       return 1;
	   }
	return 0;
	}
 


// Method to load the schemas: takes the schemas URI as an argument.
public static void loadSchemas(String schemaURI) throws MalformedURLException, IOException, ProcessingException {
	
	
	//InfluxDB influxDB = InfluxDBFactory.connect("http://138.100.51.114:9091", "adminGRyS", "GRyS2020");

	try 
	{
	    String names = GlobalParameters.getInstance().getParameter(GlobalParameters.AFC_SQUEMAS);
	    String[] namesArray = names.split(",");
	    schemasName = new ArrayList<>(Arrays.asList(namesArray));
	   
		for (String s: schemasName) {
			
	        String filename=s+".json";
	        FileUtils.copyURLToFile(          
	        new URL(schemaURI+filename),
	        new File("src/main/resources/localSchemas/"+filename));

//	       Avoids loading Definition as a schema to prevent false validations.
	        		if(s.contains("Region"))
	        		{
	        			schemasRegion.add(new Schema(factory.getJsonSchema(schemaURI+filename),0,s));
	        		}
	        		else if (s.contains("Collar")) 
	        		{
	            	   //schemasCollar.add(new Schema(factory.getJsonSchema("resource:/localSchemas/"+filename),0,s));
	            	   schemasCollar.add(new Schema(factory.getJsonSchema(schemaURI+filename),0,s));
	            	   
	               }
	               else if(s.contains("Vehicle"))
	               {
	            	   //schemasVehicle.add(new Schema(factory.getJsonSchema("resource:/localSchemas/"+filename),0,s));
	            	   schemasVehicle.add(new Schema(factory.getJsonSchema(schemaURI+filename),0,s));
	               }
	               else if(s.contains("StateVector"))
	               {
	            	   //schemasVehicle.add(new Schema(factory.getJsonSchema("resource:/localSchemas/"+filename),0,s));
	            	   schemasStateVector.add(new Schema(factory.getJsonSchema(schemaURI+filename),0,s));
	               }
	               else if(!s.equals("Definitions")) 
	               {
	            	   //schemasMeasures.add(new Schema(factory.getJsonSchema("resource:/localSchemas/"+filename),0,s));  
	            	   schemasMeasures.add(new Schema(factory.getJsonSchema(schemaURI+filename),0,s));
	               }
	        		
			}
	        		
	}catch (Exception e) {
		
		JOptionPane.showMessageDialog(null, "Error guardando configuración de schemas\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
	}
	

  };

}