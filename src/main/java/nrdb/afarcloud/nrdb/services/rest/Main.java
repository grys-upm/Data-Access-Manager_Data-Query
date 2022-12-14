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

import org.glassfish.grizzly.http.server.CLStaticHttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.ServerConfiguration;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;

import io.swagger.jaxrs.config.BeanConfig;

import java.io.IOException;
import java.net.URI;

/**
 * Main class.
 *
 */
public class Main {
    // Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = "http://0.0.0.0:9124/";

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer() {
        // create a resource config that scans for JAX-RS resources and providers
        // in afc.NRDBManager.DAM_RestServer package
//        final ResourceConfig rc = new ResourceConfig().packages("afarcloud.nrdb.services.rest");
        String resources = "afarcloud.nrdb.services.rest";
        
       	BeanConfig beanConfig = new BeanConfig();

    	beanConfig.setVersion("1.0.1");

    	beanConfig.setSchemes(new String[] { "http" });

    	beanConfig.setBasePath("");

    	beanConfig.setResourcePackage(resources);

    	beanConfig.setScan(true);

    	final ResourceConfig resourceConfig = new ResourceConfig();

    	resourceConfig.packages(resources);

    	resourceConfig.register(io.swagger.jaxrs.listing.ApiListingResource.class);

    	resourceConfig.register(io.swagger.jaxrs.listing.SwaggerSerializers.class);

    	resourceConfig.register(JacksonFeature.class);

    	resourceConfig.register(JacksonJsonProvider.class);
        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI

        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), resourceConfig);
    }

    /**
     * Main method.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
//        final HttpServer server = startServer();
        final HttpServer server = startServer();
        server.start();
        ClassLoader loader = Main.class.getClassLoader();

        CLStaticHttpHandler docsHandler = new CLStaticHttpHandler(loader, "swagger-ui/");
        CLStaticHttpHandler schemasHandler = new CLStaticHttpHandler(loader, "schemas/");  
        
        
        docsHandler.setFileCacheEnabled(false);
        schemasHandler.setFileCacheEnabled(false);
        
        ServerConfiguration cfg = server.getServerConfiguration();
        
        cfg.addHttpHandler(docsHandler, "/dataAccessManager/");
        cfg.addHttpHandler(schemasHandler, "/schemas/");
        
        try {
			Schema.loadSchemas(BASE_URI+"schemas/");
		} catch (ProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
        
        System.out.println(String.format("Jersey app started with WADL available at "
                + "%sapplication.wadl\nbase dir:: %s\n Hit enter to stop it...", BASE_URI, System.getProperty("user.dir")));
        System.in.read();
        server.shutdownNow();
    }
}

