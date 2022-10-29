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

package afarcloud.nrdb.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import ch.hsr.geohash.GeoHash;
import ch.hsr.geohash.WGS84Point;

/**
 * 
 * ProximityHash: Geohashes in Proximity + Georaptor
 * 
 * Credits:
 *  Proximity: https://github.com/ashwin711/proximityhash
 *  GeoRaptor:Geohash Compression Tool =>
 *  	Nodejs: https://github.com/SHAINAAZ992/georaptor
 *  	Phyton: https://github.com/ashwin711/georaptor
 *
 */
public class GeoHashProximity {
	 /** Earth mean radius defined by WGS 84 in meters */
    public static final double EARTH_MEAN_RADIUS = 6371008.7714D;      // meters (WGS 84)
    
    private static final double GRID_WIDTH[] = {5009400.0, 1252300.0, 156500.0, 39100.0, 4900.0, 1200.0, 152.9, 38.2, 4.8, 1.2, 0.149, 0.0370};
	private static final double GRID_HEIGHT[] = {4992600.0, 624100.0, 156000.0, 19500.0, 4900.0, 609.4, 152.4, 19.0, 4.8, 0.595, 0.149, 0.0199};
	public GeoHashProximity() {
		// TODO Auto-generated constructor stub
	}

	public static boolean in_circle(double latitude, double longitude, double centre_latitude, double centre_longitude, int radius) {
		double x_diff = longitude - centre_longitude;
		double y_diff = latitude - centre_latitude;
		
		return (Math.pow(x_diff, 2) + Math.pow(y_diff, 2)) <= Math.pow(radius, 2);
		
	}
	
	/* no son coordenadas normalizadas por lo que no se puede devolver un WGS8Point */
	/*
	public static WGS84Point getCentroid (double latitude, double longitude, double height, double width) {
		return new WGS84Point(latitude + (height/2), longitude + (width /2));
		
	}
	*/

	public static double[] getCentroid (double latitude, double longitude, double height, double width) {
		return new double[] {latitude + (height/2), longitude + (width /2)};
		
	}
	
	public static WGS84Point convertToPoint(double y, double x, double latitude, double longitude) {
		double lat_diff = (x / EARTH_MEAN_RADIUS ) * (180/Math.PI);
		double long_diff = (y / EARTH_MEAN_RADIUS) * (180/Math.PI) / Math.cos(latitude*Math.PI/180);
		
		return new WGS84Point(latitude + lat_diff, longitude + long_diff);
	}
	
	public static List<String> getGeohashCircle(WGS84Point oPoint, int radius, int precision) {
		if (precision > GRID_WIDTH.length)
			return null;
		
		ArrayList<String> lGeoHash = new ArrayList<String>();
		ArrayList<WGS84Point> lPoint = new ArrayList<WGS84Point>();
		
		double height = GRID_HEIGHT[precision-1]/2;
		double width = GRID_WIDTH[precision-1]/2;
		
		int latitud_move = (int) Math.ceil(radius / height);
		int longitud_move = (int) Math.ceil(radius / width);
		
		double x = 0.0;
		double y = 0.0;
		
		double temp_latitude;
		double temp_longitude;
		//WGS84Point oCentroid;
		double [] aCentroid;
		
		for (int i=0; i<latitud_move; i++) {
			temp_latitude = y + (height * i);
			
			for (int j=0; j<longitud_move; j++) {
				temp_longitude = x + (width * j);
				
				if(in_circle(temp_latitude, temp_longitude, y, y, radius)) {
					aCentroid = getCentroid(temp_latitude, temp_longitude, height, width);
					lPoint.add(convertToPoint(aCentroid[0], aCentroid[1], oPoint.getLatitude(), oPoint.getLongitude()));
					lPoint.add(convertToPoint(-1*aCentroid[0], aCentroid[1], oPoint.getLatitude(), oPoint.getLongitude()));
					lPoint.add(convertToPoint(aCentroid[0], -1*aCentroid[1], oPoint.getLatitude(), oPoint.getLongitude()));
					lPoint.add(convertToPoint(-1*aCentroid[0], -1*aCentroid[1], oPoint.getLatitude(), oPoint.getLongitude()));
				}
			}
		}
		
		lPoint.forEach( (WGS84Point oP) -> lGeoHash.add(
					GeoHash.geoHashStringWithCharacterPrecision(
							oP.getLatitude(),
							oP.getLongitude(),
							precision
							)
				)
			);
		return lGeoHash;
	}
	/**
	 * georaptor functionality
	 * 
	 *  
	 * method to compress the output 
	 * 
	 */
	private static ArrayList<String> getCombinations(String string) {
	    String[] aBase32 = new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "b", "c", "d", "e", "f", "g", "h", "j", "k", "m","n", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};
	    ArrayList<String> lAcc = new ArrayList<String>();

	    for (String sPresent : aBase32)
	        lAcc.add(string.concat(sPresent));

	    return lAcc;
	}
	
	public static List<String> compress (List<String> lGeoHash, int minLevel, int maxLevel) {
		if( lGeoHash==null || lGeoHash.size() == 0) {
			return null;
		}
		
	
		HashSet<String> hDeleteGeoHash = new HashSet<String>();
		HashSet<String> hFinalGeohashes = new HashSet<String>();
		boolean bFlag = true;
		int nFinalGeohashesSize = 0;

		do {
			hFinalGeohashes.clear();
			hDeleteGeoHash.clear();			
			
			
			for(String sPresentGeoHash : lGeoHash) {
				
				//# Compress only if geohash length is greater than the min level
				if (sPresentGeoHash.length()>= minLevel) {
					String sPartGeoHash = sPresentGeoHash.substring(0, sPresentGeoHash.length()-1);
					
					//# Proceed only if not already processed
					if( !hDeleteGeoHash.contains(sPartGeoHash) && !hDeleteGeoHash.contains(sPresentGeoHash) ) {
						// # Generate combinations
	                    HashSet<String> hCombinations = new HashSet<String>(getCombinations(sPartGeoHash));
	                    
	                    //# If all generated combinations exist in the input set
	                    if (lGeoHash.containsAll(hCombinations)) {
	                    	//# Add part to temporary output
	                    	hFinalGeohashes.add(sPartGeoHash);
	                    	//# Add part to deleted geohash set
	                    	hDeleteGeoHash.add(sPartGeoHash);
	                    }
	                    // # Else add the geohash to the temp out and deleted set
	                    else {
	                    	hDeleteGeoHash.add(sPresentGeoHash);
	                    	
	                    	//# Forced compression if geohash length is greater than max level after combination check failure
	                    	if(sPresentGeoHash.length()>=maxLevel) {
	                    		hFinalGeohashes.add(sPresentGeoHash.substring(0, maxLevel));	                    		
	                    	}else {
	                    		hFinalGeohashes.add(sPresentGeoHash);
	                    	}
	                    }
	                    
	                    //# Break if compressed output size same as the last iteration	         
	                    bFlag = (nFinalGeohashesSize == hFinalGeohashes.size());
	                    
					}
				}
			}
			
			nFinalGeohashesSize = hFinalGeohashes.size();
			lGeoHash.clear();
			
			//# Temp output moved to the primary geohash set
			lGeoHash.addAll(hFinalGeohashes);

		}while (bFlag);
		
		
		return lGeoHash;
	}
	
	public static void main(String[] args) {
		/* collar AS06*/
		double lat = 40.693797208;
		double lon = -4.531425956;

		
		System.out.printf("Point [%f, %f] - %s\n", lat, lon, GeoHash.geoHashStringWithCharacterPrecision(lat, lon, 12));
		
		List<String> lGeo2 = GeoHashProximity.getGeohashCircle(new WGS84Point(lat,lon), 1000, 7);
		lGeo2.forEach( (String sGeoHash) -> System.out.println(sGeoHash)
				);
		System.out.println("\n =====> " + lGeo2.size());
		
		lGeo2 = GeoHashProximity.compress(lGeo2, 3, 6);
		System.out.println("\nTras Georaptor:");
		lGeo2.forEach( (String sGeoHash) -> System.out.println(sGeoHash)
				);
		System.out.println("\n =====> " + lGeo2.size());		
		
		/* prueba */		
		String sAux="";
		for(int i=0; i<lGeo2.size();i++) {
			lGeo2.add(i, "\"geohash\"=~/^"+lGeo2.get(i)+"/");
			/* al insertar en la misma posición, el elemento actual pasa a ser el siguiente */
			lGeo2.remove(i+1);
		}
		sAux = String.join(" OR ", lGeo2);
		System.out.println(sAux);
	}
		
}

/*
 * 
 * select geohash,latitude,longitude from /^obs_/ where "geohash"=~/^u679td/ OR "geohash"=~/^u679mw/ OR "geohash"=~/^u679kw/ OR "geohash"=~/^u679sd/ LIMIT 1  
 */
