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
package afarcloud.nrdb.services.rest.store;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/**
 * @author slana
 * 
 * altitude and geohash:: tag and field
 *
 */
public class JSONParserUtils {
 	
	
	/**
	 * the params must be thread save => only one thread 
	 * @param lProperties
	 * @return
	 */
	public static HashMap<String, Object> parseObject (JsonParser oParser, List<String> lProperties) throws Exception{
		HashMap<String, Object> hOut = new HashMap<String, Object>();

		    JsonToken jsonToken = oParser.nextToken();
	
			while(!JsonToken.END_OBJECT.equals(jsonToken)){
			    
			    if(JsonToken.FIELD_NAME.equals(jsonToken)){
			        String fieldName = oParser.getCurrentName();
		        	//move to next token:: JsonToken.START_ARRAY || JsonToken.START_OBJECT || JsonToken.VALUE_XXX
		            oParser.nextToken();
		            if(lProperties.contains(fieldName)){
		            	hOut.put(fieldName, parseValue(oParser));
		            }
			    }
			    jsonToken = oParser.nextToken();
			}

		return hOut;
		
	}
	
	public static ArrayList<String> _parseArrayOfValues(JsonParser oParser) throws Exception{
		ArrayList<String> lReturn = new ArrayList<String>();
		
		//move to next token:: JsonToken.START_ARRAY => START_OBJECT
	    JsonToken jsonToken = oParser.nextToken();
	    while (!JsonToken.END_ARRAY.equals(jsonToken)){	
	        if (jsonToken.isScalarValue()) {
	        	lReturn.add(oParser.getValueAsString());
	        }	        
			//move to next token:: JsonToken.START_OBJECT || JsonToken.END_ARRAY
		    jsonToken = oParser.nextToken();
	    } //END_ARRAY
		
		return lReturn;
	}

	/**
	 * @return
	 */
	public static Object parseValue(JsonParser oParser) throws Exception {
		Object oValue=null;
		try {
			
			JsonToken jsonToken = oParser.currentToken();
			
		/*	
			if (jsonToken == JsonToken.VALUE_NUMBER_INT || jsonToken == JsonToken.VALUE_NUMBER_FLOAT) {
				oValue= oParser.getNumberValue();
			}else if(jsonToken == JsonToken.VALUE_FALSE || jsonToken == JsonToken.VALUE_TRUE) {
				oValue = oParser.getBooleanValue();
			}else {
				oValue=oParser.getValueAsString();
			}
		*/
			if(jsonToken.isNumeric()) {
				oValue= oParser.getNumberValue();
//				oValue= oParser.getNumberValue().doubleValue();
			}else if(jsonToken.isBoolean()) {
				oValue = oParser.getBooleanValue();
			}else {
				oValue=oParser.getValueAsString();
			}

			 
		} catch (IOException e) {
			// TODO Auto-generated catch block
					oValue=oParser.getValueAsString();
		}
		return oValue;
	}	
	
	/**
	 * @param afcId
	 * @param sSuffix
	 * @return
	 */
	public static LinkedHashMap<String, String> parseAFC_Id(final String afcId, final String AFC_ID[], final int AFC_ID_PADDING, final String sSuffix){
		LinkedHashMap<String, String> hOut = new LinkedHashMap<String, String>();

		if (afcId!=null){
			String aAux[] = afcId.split(":");			
			if (aAux.length == AFC_ID.length + AFC_ID_PADDING) {
				for(int i=0; i<aAux.length-AFC_ID_PADDING; i++){
					hOut.put(AFC_ID[i]+sSuffix, aAux[i+AFC_ID_PADDING]);
				}
			}
		}
		return hOut;
	}
	
	/**
	 * @param afcId
	 * @param sSuffix
	 * @return
	 */
	public static LinkedHashMap<String, Object> parseAFC_Id_asField(final String afcId, final String AFC_ID[], final int AFC_ID_PADDING, final String sSuffix){
		LinkedHashMap<String, Object> hOut = new LinkedHashMap<String, Object>();

		if (afcId!=null){
			String aAux[] = afcId.split(":");			
			if (aAux.length == AFC_ID.length + AFC_ID_PADDING) {
				for(int i=0; i<aAux.length-AFC_ID_PADDING; i++){
					hOut.put(AFC_ID[i]+sSuffix, aAux[i+AFC_ID_PADDING]);
				}
			}
		}
		return hOut;
	}

 public static Object ObjectToFloat(Object oField) {
	 
	 if (oField instanceof Integer) {
		 return ((int)oField) * 1.0;		 
	 }else if (oField instanceof Long) {
		 return ((long)oField) * 1.0;
	 }else
		 return oField;
	 
		 
 }
	

}
