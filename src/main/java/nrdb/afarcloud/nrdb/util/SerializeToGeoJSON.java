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

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import afarcloud.nrdb.config.Constants;
import ch.hsr.geohash.BoundingBox;
import ch.hsr.geohash.GeoHash;
import ch.hsr.geohash.WGS84Point;
import ch.hsr.geohash.queries.GeoHashCircleQuery;
import ch.hsr.geohash.util.VincentyGeodesy;

public class SerializeToGeoJSON {
	
	public static final String TYPE = "type";
	public static final String FEATURE = "Feature";
	public static final String COORDINATES = "coordinates";
	public static final String GEOMETRY = "geometry";
	public static final String PROPERTIES = "properties";
	public static final String FEATURES = "features";
	
	public static final String GEOMETRY_POINT ="Point";
	public static final String GEOMETRY_MULTIPOINT ="Multipoint";
	public static final String GEOMETRY_LINESTRING ="LineString";
	public static final String GEOMETRY_MULTILINESTRING ="MultiLineString";
	public static final String GEOMETRY_POLYGON ="Polygon";
	public static final String GEOMETRY_MULTIPOLYGON ="MultiPolygon";
	
	
	/**
	 *  https://tools.ietf.org/html/rfc7946#section-3.1.2
	 *  For type "Point", the "coordinates" member is a single position.
	 * @param oPoint
	 * @return
	 * @throws JsonProcessingException 
	 */
	public static String pointToGeoJSON(WGS84Point oPoint, HashMap<String, String>hProperties) throws JsonProcessingException {
		ObjectMapper oJSONMapper = new ObjectMapper();
		oJSONMapper.enable(SerializationFeature.INDENT_OUTPUT);
		ObjectNode nodeResult = oJSONMapper.createObjectNode();
		
		nodeResult.put(SerializeToGeoJSON.TYPE, SerializeToGeoJSON.FEATURE);
		nodeResult.putPOJO(SerializeToGeoJSON.GEOMETRY, 
						 nodeGeometry(oJSONMapper, SerializeToGeoJSON.GEOMETRY_POINT, pointToPosition(oJSONMapper, oPoint)));
		
		if (hProperties!=null && !hProperties.isEmpty()) {
			nodeResult.putPOJO(SerializeToGeoJSON.PROPERTIES,
						nodeProperties(oJSONMapper, hProperties)
					);			
		}
		
		
		return oJSONMapper.writeValueAsString(nodeResult);
	}
	
	/**
	 *  https://tools.ietf.org/html/rfc7946#section-3.1.4
	 *  For type "LineString", the "coordinates" member is an array of two or more positions.
	 * @param lPoint
	 * @return
	 * @throws JsonProcessingException 
	 */
	public static String lineStringToGeoJSON(ArrayList<WGS84Point> lPoint, HashMap<String, String>hProperties) throws JsonProcessingException {
		ObjectMapper oJSONMapper = new ObjectMapper();
		
		oJSONMapper.enable(SerializationFeature.INDENT_OUTPUT);
		ObjectNode nodeResult = oJSONMapper.createObjectNode();
		
		nodeResult.put(SerializeToGeoJSON.TYPE, SerializeToGeoJSON.FEATURE);
		nodeResult.putPOJO(SerializeToGeoJSON.GEOMETRY, 
				 nodeGeometry(oJSONMapper, SerializeToGeoJSON.GEOMETRY_LINESTRING, pointListToArrayPositions(oJSONMapper, lPoint)));
		
		if (hProperties!=null && !hProperties.isEmpty()) {
			nodeResult.putPOJO(SerializeToGeoJSON.PROPERTIES,
						nodeProperties(oJSONMapper, hProperties)
					);			
		}
		
		return oJSONMapper.writeValueAsString(nodeResult);
	}
	
	/**
	 *  https://tools.ietf.org/html/rfc7946#section-3.1.6 
	 *  For type "Polygon", the "coordinates" member MUST be an array of linear ring coordinate arrays.
	 *  A linear ring is a closed LineString with four or more positions. The first and last positions are equivalent,
	 *   and they MUST contain identical values; their representation SHOULD also be identical.  
	 * @param lPoint
	 * @return
	 * @throws JsonProcessingException 
	 */
	public static String polygonToGeoJSON(ArrayList<WGS84Point> lPoint, HashMap<String, String>hProperties) throws JsonProcessingException {
		ObjectMapper oJSONMapper = new ObjectMapper();
		
		oJSONMapper.enable(SerializationFeature.INDENT_OUTPUT);
		ObjectNode nodeResult = oJSONMapper.createObjectNode();
		
		nodeResult.put(SerializeToGeoJSON.TYPE, SerializeToGeoJSON.FEATURE);
		
		ArrayNode arrayPolygon = oJSONMapper.createArrayNode();
		arrayPolygon.add( pointListToArrayPositions(oJSONMapper, lPoint));
		
		nodeResult.putPOJO(SerializeToGeoJSON.GEOMETRY, 
				 nodeGeometry(oJSONMapper, SerializeToGeoJSON.GEOMETRY_POLYGON, arrayPolygon)
				 );
		
		if (hProperties!=null && !hProperties.isEmpty()) {
			nodeResult.putPOJO(SerializeToGeoJSON.PROPERTIES,
						nodeProperties(oJSONMapper, hProperties)
					);			
		}
		
		return oJSONMapper.writeValueAsString(nodeResult);
	}
	
	
	
	/**
	 * https://tools.ietf.org/html/rfc7946#section-3.1.1
	 */
	private static  ArrayNode pointToPosition(ObjectMapper oJSONMapper, WGS84Point oPoint) {
		
		ArrayNode arrayPosition = oJSONMapper.createArrayNode();
		arrayPosition.add(oPoint.getLongitude());
		arrayPosition.add(oPoint.getLatitude());
				
		return arrayPosition;
	}

	private static ArrayNode pointListToArrayPositions(ObjectMapper oJSONMapper, ArrayList<WGS84Point> lPoint) {
		ArrayNode arrayPositions = oJSONMapper.createArrayNode();
		lPoint.forEach((WGS84Point oPoint) -> {
			arrayPositions.add(pointToPosition(oJSONMapper, oPoint));
		});
		
		return arrayPositions;
	}

	private static ObjectNode nodeGeometry(ObjectMapper oJSONMapper, String sType, Object oCoordinates) throws JsonProcessingException {
		ObjectNode nodeGeometry = oJSONMapper.createObjectNode();
		
		nodeGeometry.put(SerializeToGeoJSON.TYPE, sType);
		nodeGeometry.putPOJO(SerializeToGeoJSON.COORDINATES, oCoordinates);
		return nodeGeometry;
	}
	
	private static ObjectNode nodeProperties(ObjectMapper oJSONMapper, HashMap<String, String>hProperties) throws JsonProcessingException {
		ObjectNode nodeProp = oJSONMapper.createObjectNode();
		hProperties.forEach((sProperty, sValue) ->
			nodeProp.put(sProperty, sValue)		
		);
		
		return nodeProp;
	}
	/*************************************/
	/**
	 * geoHash to closed polygonal line
	 * @param sGeoHash
	 * @return 
	 * @throws JsonProcessingException 
	 */
	public static String geoHashToGeoJSON(String sGeoHash) throws JsonProcessingException {		
		GeoHash oGeoH = GeoHash.fromGeohashString(sGeoHash);
		BoundingBox oBB =oGeoH.getBoundingBox();
		

		ArrayList<WGS84Point> lPoint = new ArrayList<WGS84Point>(5);
		lPoint.add(oBB.getNorthEastCorner());
		lPoint.add(oBB.getNorthWestCorner());
		lPoint.add(oBB.getSouthWestCorner());
		lPoint.add(oBB.getSouthEastCorner());
		lPoint.add(oBB.getNorthEastCorner());
		
		HashMap<String,String>hProp = new HashMap<String, String>();
		hProp.put("geohash", sGeoHash);		
				
		return SerializeToGeoJSON.lineStringToGeoJSON(lPoint, hProp);
	}
	
	
	public static String circleToGeoJSON(WGS84Point center, int radius) {
		StringBuilder sOut = new StringBuilder();	
		ArrayList<WGS84Point> lPoint = new ArrayList<WGS84Point>();
		
		/* un punto cada 10 grados */
		try {
			for(int i=0; i<36; i++) {
				lPoint.add(VincentyGeodesy.moveInDirection(center, i*10, radius));
			}
			
			HashMap<String, String>hProp = new HashMap<String,String>();
			hProp.put("name", "circle");
			sOut.append("\n" + 						
					SerializeToGeoJSON.lineStringToGeoJSON(lPoint, hProp) +
					//SerializeToGeoJSON.polygonToGeoJSON(lPoint, hProp) +
						",");
			
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return sOut.toString();		
	}
	
	public static String circleToGeoJSONBoundingBox(WGS84Point center, int radius) {
		StringBuilder sOut = new StringBuilder();
		WGS84Point northEastCorner = VincentyGeodesy.moveInDirection(VincentyGeodesy.moveInDirection(center, 0, radius), 90, radius);
		WGS84Point southWestCorner = VincentyGeodesy.moveInDirection(VincentyGeodesy.moveInDirection(center, 180, radius), 270, radius);
		
		HashMap<String, String>hProp = new HashMap<String,String>();
		hProp.put("name", "northEastCorner");
		
		try {
			
			/* corner */
			hProp.put("name", "northEastCorner");
			sOut.append("\n" + 						
						SerializeToGeoJSON.pointToGeoJSON(northEastCorner, hProp)
									 +
							",");
			
			hProp.put("name", "southWestCorner");
			sOut.append("\n" + 						
						SerializeToGeoJSON.pointToGeoJSON(southWestCorner, hProp)
									 +
							",");
		
			/* closed line */
			ArrayList<WGS84Point> lPoint = new ArrayList<WGS84Point>(5);
			lPoint.add(northEastCorner);
			lPoint.add(new WGS84Point(southWestCorner.getLatitude(),northEastCorner.getLongitude()) );
			lPoint.add( southWestCorner );
			lPoint.add(new WGS84Point(northEastCorner.getLatitude(), southWestCorner.getLongitude()) );
			lPoint.add(northEastCorner);
			hProp.put("name", "boundingBox");

			sOut.append("\n" + 						
					SerializeToGeoJSON.lineStringToGeoJSON(lPoint, hProp) +
						",");
			
			System.out.println("\n ("+
					"\"latitude\" >= " + southWestCorner.getLatitude() +
					" AND \"latitude\" <= " + northEastCorner.getLatitude() + 
					" AND \"longitude\" >= " + southWestCorner.getLongitude() +
					" AND \"longitude\" <= " + northEastCorner.getLongitude() +
					")"
					);
			
		} catch (JsonProcessingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return sOut.toString();		
	}


	
	public static String geoHashCircleToGeoJSON(WGS84Point center, int radius, boolean bGeoraptor) {
		StringBuilder sOut = new StringBuilder();
		/* compute geohash proximity */
		int nProximityPrecision = Constants.IDB_QUERY_GEOHASH_PROXIMITY_PRECISION;
		int nGeoraptorMax = Constants.IDB_QUERY_GEORAPTOR_MAX;
		if(radius<=100) {
			nProximityPrecision+= Constants.IDB_QUERY_GEOHASH_PROXIMITY_FACTOR_100;
			//nGeoraptorMax+= Constants.IDB_QUERY_GEOHASH_GEORAPTOR_MAX_100;
			nGeoraptorMax = 8;
		}else if(radius<=500) {
			nProximityPrecision+= Constants.IDB_QUERY_GEOHASH_PROXIMITY_FACTOR_500;
			nGeoraptorMax+= Constants.IDB_QUERY_GEOHASH_GEORAPTOR_MAX_500;
		}

		System.out.format("\n\t - ProximityPrecision: %d, \n\t - Georaptor Max: %d, \n\t - Georaptor Min: %d, ",
					nProximityPrecision,
					nGeoraptorMax,
					Constants.IDB_QUERY_GEORAPTOR_MIN
					);
		
		
		List<String> lGeo =  GeoHashProximity.getGeohashCircle(	center, radius, nProximityPrecision	);
			
		//reduce by georaptor 					
		if (bGeoraptor) {
			lGeo = GeoHashProximity.compress(lGeo, Constants.IDB_QUERY_GEORAPTOR_MIN, nGeoraptorMax);
		}
		
		lGeo.forEach( (String sG) -> {
			try {
				sOut.append("\n" + SerializeToGeoJSON.geoHashToGeoJSON(sG)+",");
			} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});

		
		return sOut.toString();			
	}

	public static void main(String[] args) {
		
		/* point
		lat = 57.9202195;
		lon = 16.4001396;
		*/
		/*
		try {
			System.out.println(SerializeToGeoJSON.pointToGeoJSON(new WGS84Point(lat, lon), null ) );
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
*/
		/* geohash */
		/*
		try {
			System.out.println(SerializeToGeoJSON.geoHashToGeoJSON(GeoHash.geoHashStringWithCharacterPrecision(lat, lon, 7)) );
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		*/
		/* pruebas Afshin 20200903
		 * observationType: Environmental
		 * method: getSensorLastDataBySensorType
		 * 
		 * Centre: Lat: 44.22466, Lng: 11.94185
		 * Radius: 1000
         * ==> georaptorMIN => 3
         * ==> georaptorMAX => 1 
         * ==> proximityPrecision => 7

         * 
         * Centre: Lat: 44.224444, Lng: 11.941667,
         * Radius: 500
		 * ==> georaptorMIN => 3
         * ==> georaptorMAX => 5 + 2
         * ==> proximityPrecision => 7 + 2

         * 
         * 
         * Centre: Lat: 44.224444, Lng: 11.941667,
         * Radius: 100
		 * ==> georaptorMIN => 3
         * ==> georaptorMAX => 5 + 2
         * ==> proximityPrecision => 7 + 2
 
		 *  */
		/* 
		double lat = 44.22466;
		double lon = 11.94185;
		*/
		
		
		//AS04 lat: 41.331229, lon:-4.993704  (position of the google mark)
		double lat = 41.331229;
		double lon = -4.993704;
		
		WGS84Point center = new WGS84Point(lat, lon);
		int radius = 1000;
		StringBuilder sOut = new StringBuilder();
		
		
		/* centroid */
		try {
			HashMap<String, String>hProp = new HashMap<String,String>();
			hProp.put("name", "Centroid");
			sOut.append(SerializeToGeoJSON.pointToGeoJSON(center, hProp ) + ", ");
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		/* circle */
		//sOut.append(SerializeToGeoJSON.circleToGeoJSON(center, radius));
		
		
		/* con proximity y georaptor */
		
		//sOut.append(SerializeToGeoJSON.geoHashCircleToGeoJSON(center, radius, false));
		//sOut.append(SerializeToGeoJSON.geoHashCircleToGeoJSON(center, radius, true));
		
		/* con aproximacion a cuadrado */
		sOut.append(SerializeToGeoJSON.circleToGeoJSONBoundingBox(center, radius));
			
		
		
		/* out */
		PrintStream strmOut;
		try {
			strmOut = new PrintStream("geoJSON.txt", "UTF-8");
			strmOut.println(
					"{\n"+
					"	\"type\" : \"FeatureCollection\", \n" +
					"	\"features\": [\n" +
					sOut.toString().substring(0, sOut.toString().lastIndexOf(',')) + "\n" + 
					"   ]\n"+
					"}"
					);
			strmOut.close();
			
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	
	
}
