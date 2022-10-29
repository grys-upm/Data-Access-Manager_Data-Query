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

/**
 * 
 */
package afarcloud.nrdb.services.rest.store.test;

import java.io.File;
import java.io.FilenameFilter;

import org.influxdb.dto.BatchPoints;

import afarcloud.nrdb.services.rest.store.JSONParser;
import afarcloud.nrdb.services.rest.store.NRDBJSONException;



 abstract class TestJSONParser {
		
	protected static JSONParser oParser;     
	/**
	 * cada subclase debe crear una instancia del parser que pretende probar
	 */
	public TestJSONParser() {
		
	}

	/**
	 * @param args
	 */
	protected void test(String[] args)  {
		// arguments validation
		if (args.length != 1){
			printUsage("ERROR: missing arguments");
			System.exit(1);
		}

		// source directory must exists and must be readable
		File fSource = new File(args[0]);
		if (!fSource.isDirectory() || !fSource.canRead()){
			printUsage("ERROR: '" + args[0] + "' does not exist o can not be read");
			System.exit(1);
		}

		File[] aFiles = fSource.listFiles( new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				// TODO Auto-generated method stub
				if (name.endsWith(".json")){
					return true;
				}
				return false;
			}
		 }
		);
		
 
				
		for (File fFile: aFiles){
			System.out.print("\n================================\n"+
							 fFile.getName());
			
		
			BatchPoints batchPoints;
			try {
				batchPoints = oParser.parse(fFile);
				System.out.format("\nTO INFLUXDB WRITE:: DB => %s\n%s\n",
						oParser.getDBName(),
						(batchPoints==null?"no data":batchPoints.lineProtocol())
						);
				
			} catch (NRDBJSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		}
	
	}

	
	
	/**
	 * Muestra mensaje de uso del programa.
	 *
	 * @param[in] mensaje  Mensaje adicional informativo en la línea inicial (null si no se desea)
	 */
	private static void printUsage(String mensaje){
		if (mensaje != null)
			System.err.println(mensaje);
		
		System.err.println(
				"Uso:\t " + Thread.currentThread().getContextClassLoader().getClass().getName()+ "<sourceDirectory>\n" +
				"\t\t<sourceDirectory>:\tpath for source directory" + 
				"\n");
	}

}


