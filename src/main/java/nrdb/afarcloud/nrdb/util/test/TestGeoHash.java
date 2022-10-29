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

package afarcloud.nrdb.util.test;


import java.util.List;

import afarcloud.nrdb.util.GeoHashProximity;
import ch.hsr.geohash.GeoHash;
import ch.hsr.geohash.WGS84Point;
import ch.hsr.geohash.queries.GeoHashCircleQuery;


public class TestGeoHash {
	
	public static void main(String[] args) {
		double lat =57.9202195;
		double lon = 16.4001396;
		
		int nlat = 0;
		int nlon = 0;
		
		System.out.printf("Point [%d, %d] - %s\n", nlat, nlon, GeoHash.geoHashStringWithCharacterPrecision((double)nlat, (double)nlon, 12));
		System.exit(0);
		
		System.out.printf("Point [%f, %f] - %s\n", lat, lon, GeoHash.geoHashStringWithCharacterPrecision(lat, lon, 12));
		
		/* pruebas con el paquete Geohash
		 * northEastCorner
		 * southWestCorner
		 *  */
		GeoHashCircleQuery oGeo =new GeoHashCircleQuery(new WGS84Point(lat,lon), 1000);
		System.out.println(oGeo);
		
		List<GeoHash> lGeo = oGeo.getSearchHashes();
		lGeo.forEach( (GeoHash oG) -> 
				System.out.println(
						GeoHash.geoHashStringWithCharacterPrecision(
								oG.getBoundingBoxCenter().getLatitude(),	
								oG.getBoundingBoxCenter().getLongitude(),
								7
								)
						)
				);
		

		/* pruebas con mi Proximity+Georaptor */
		
		List<String> lGeo2 = GeoHashProximity.getGeohashCircle(new WGS84Point(lat,lon), 1000, 7);
		/*
		lGeo2.forEach( (String sGeoHash) -> System.out.println(sGeoHash)
				);
				*/
		System.out.println("\n =====> " + lGeo2.size());
		
		lGeo2 = GeoHashProximity.compress(lGeo2, 3, 5);
		System.out.println("\nTras Georaptor:");
		lGeo2.forEach( (String sGeoHash) -> System.out.println(sGeoHash)
				);
		System.out.println("\n =====> " + lGeo2.size());		
	}
		
}

/*
 * pruebas en influx: =>> no funciona, me devuelve el boundingBox
 * 
 * select geohash,latitude,longitude from /^obs_/ where "geohash"=~/^u679td/ OR "geohash"=~/^u679mw/ OR "geohash"=~/^u679kw/ OR "geohash"=~/^u679sd/ LIMIT 1  
 */
