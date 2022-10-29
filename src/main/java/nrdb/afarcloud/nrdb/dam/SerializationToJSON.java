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

package afarcloud.nrdb.dam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.influxdb.dto.QueryResult.Result;
import org.influxdb.dto.QueryResult.Series;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import afarcloud.nrdb.config.Constants;
import afarcloud.nrdb.util.DataTypes;
import afarcloud.nrdb.util.GlobalParameters;

public class SerializationToJSON {
	
	private static final String SCHEMA_MEASUREMENTS_SERIE = "measurements";
	private static final String SCHEMA_MEASUREMENTS_TAG = "name";
	
	private static final List<String> PARAMS_TIME; 
	static {
		PARAMS_TIME = new ArrayList<String> (Arrays.asList(new String[] { Constants.SRV_PARAM_START_TIME, Constants.SRV_PARAM_END_TIME}));
	}

	
	
    public static String errorToJSON(String sQuery,HashMap<String, String>hParams, String sMsg) throws JsonProcessingException {
		ObjectMapper oJSONMapper = new ObjectMapper();
		oJSONMapper.enable(SerializationFeature.INDENT_OUTPUT);
		ObjectNode nodeResult = oJSONMapper.createObjectNode();

		nodeResult.put("query", sQuery);
		
		nodeResult.put("error", sMsg);
		
		return oJSONMapper.writeValueAsString(nodeResult);	    	
    }
    
    private static ObjectNode _paramsToJSON(ObjectMapper oJSONMapper, HashMap<String, String>hParams) {
    	ObjectNode nodeParams = oJSONMapper.createObjectNode();
    	String sValue;
    	/* sort params */
    	TreeMap<String,String> hSortParams= new TreeMap<>(hParams);
    	for(Entry<String, String> eParam :hSortParams.entrySet()) {
    		sValue = eParam.getValue();
    		if(PARAMS_TIME.contains(eParam.getKey())){
    			try {
					sValue= DataTypes.stringDateToTimestamp(sValue);
				} catch (Exception e) {
					sValue = eParam.getValue();
				}
    		}
   			nodeParams.put(eParam.getKey(), sValue);

    	}
    	
    	return nodeParams;
    }
    
    
    /**
     * JSON example: (sRootElement = measurement; sNameElement = observations)
     * {
     * "query": "/getSensorTelemetry/historic?start_time=2020-04-01&provider=AIT&type=soil",
     * "params": {
     * "start_time": "2020-04-01",
     * "limit": "100"
     * },
     * "numResults": 236,
     * "results": [
     *   {
     *      "measurement": "battery",
     *      "observations": [...]
     *   },
     *   {
     *      "measurement": "air_temperature",
     *      "observations": [...]
     *   },
     *   {
     *      "measurement": "air_pressure",
     *      "observations": [...]
     *   }
     *  ]
     * }
     * 
     * 
     * 
     * 
     * @param sQuery http queryString simplified
     * @param hParams	param-value pairs used to construct the query
     * @param sRootElement literal of the property that identifies each aggregated collection 
     * @param sNameElement literal of the aggregated collection
     * @param results influxDB results
     * @param sPrefix prefix to eliminate from 'table' name
     * @return 
     * @throws JsonProcessingException
     */
    public static String resultsToJSONAgrByMeasurements(String sQuery, HashMap<String, String>hParams, List<Result> results, String sPrefix) throws JsonProcessingException {
    	ArrayList<String> lPrefix = null;    	
    	if (sPrefix!=null ) {
    		lPrefix= new ArrayList<String>();
    	   	lPrefix.add(sPrefix);
    	}
    	return resultsToJSONAgrByMeasurements(sQuery, hParams, results, lPrefix);
    }

    public static String resultsToJSONAgrByMeasurements(String sQuery, HashMap<String, String>hParams, List<Result> results, ArrayList<String> lPrefix) throws JsonProcessingException {
		ObjectMapper oJSONMapper = new ObjectMapper();
		oJSONMapper.enable(SerializationFeature.INDENT_OUTPUT);
		ObjectNode nodeResult = oJSONMapper.createObjectNode();
		ArrayNode arrayMeasurements = oJSONMapper.createArrayNode();

		NumResult nResults = new NumResult(0);
		
		nodeResult.put("query", sQuery);
		if (hParams!=null ) {
			nodeResult.putPOJO("params",_paramsToJSON(oJSONMapper, hParams));
		}
		for(Result res:results) {    				
			if (!res.hasError() && res.getSeries()!=null) {
				
				for (Series serie: res.getSeries()) { 						
					ObjectNode nodeMeasurement = _serieToNodeMeasurement(oJSONMapper, lPrefix, serie, nResults);
					arrayMeasurements.add(nodeMeasurement);
				}    			
			}
		}
		nodeResult.put("numResults", nResults.get());
		if (!arrayMeasurements.isEmpty()) {
			
			nodeResult.putPOJO("results", arrayMeasurements);
		}
		
		return oJSONMapper.writeValueAsString(nodeResult);	
    }

    
    /**
     * 
     * {
  "query" : "/getObservationsBySensor/latest",
  "params" : {
    "limit" : "1"
  },
  "numResults" : 10,
  "results" : {
    "devices" : [ {
      "device" : "drone001",
      "resources" : [ {
        "resource" : "airPollutionSensor001",
        "measurements" : [ {
          "measurement" : "battery",
          "observations" : [  ]
        } ]
      }, {
        "resource" : "airTemperatureSensor001",
        "measurements" : [ ]
      } ]
    } ],
    "resources" : [ {
      "resource" : "airTemperatureSensor0012",
      "measurements" : [ {
        "measurement" : "soilTemperature",
        "observations" : [ ]
      } ]
    }, {
      "resource" : "afc_node_0100_5",
      "measurements" : [ {
        "measurement" : "temperature_teros21",
        "observations" : [  ]
      }, {
        "measurement" : "temperature_teros12",
        "observations" : [  ]
      }, {
        "measurement" : "soil_matrix_potential",
        "observations" : [  ]
      }, {
        "measurement" : "electrical_conductivity_pores",
        "observations" : [ ]
      }, {
        "measurement" : "electrical_conductivity_bulk",
        "observations" : [  ]
      } ]
    }
	]
  }
}
     * @param sQuery
     * @param hParams
     * @param lResults
     * @param sPrefix
     * @return
     * @throws JsonProcessingException
     */
    public static String resultsToJSONAgrBySensor(String sQuery, HashMap<String, String>hParams, List<Result> lResults, String sPrefix) throws JsonProcessingException {
    	ArrayList<String> lPrefix = null;    	
    	if (sPrefix!=null ) {
    		lPrefix= new ArrayList<String>();
    	   	lPrefix.add(sPrefix);
    	}
    	
    	return resultsToJSONAgrBySensor(sQuery, hParams, lResults, lPrefix);
    }
    
    public static String resultsToJSONAgrBySensor(String sQuery, HashMap<String, String>hParams, List<Result> results, ArrayList<String> lPrefix) throws JsonProcessingException {
		ObjectMapper oJSONMapper = new ObjectMapper();
		oJSONMapper.enable(SerializationFeature.INDENT_OUTPUT);
		ObjectNode nodeResult = oJSONMapper.createObjectNode();	
		
		ArrayNode arrayMeasurements = oJSONMapper.createArrayNode();
		
		nodeResult.put("query", sQuery);
		nodeResult.putPOJO("params",_paramsToJSON(oJSONMapper, hParams));
		
		
		NumResult nResults = new NumResult(0);
		
		/* resource management => entityName */		
		HashMap <String, ArrayNode> hResources= new HashMap<String, ArrayNode>();

		/* devices management => entityName_dev */		
		HashMap <String, HashMap<String, ArrayNode>> hDevices= new HashMap<String, HashMap<String, ArrayNode>>();

		for(Result res:results) {    				
			if (!res.hasError() && res.getSeries()!=null) {
				
				for (Series serie: res.getSeries()) {	
					
					System.out.println("La serie es: "+serie);
					/* process Devices */
					if (serie.getTags().get(InfluxDataAccess.INFLUX_TAG_DEVICE)!=null &&
						!serie.getTags().get(InfluxDataAccess.INFLUX_TAG_DEVICE).trim().isEmpty()
						) {
						_parseSerieToDevice(oJSONMapper, lPrefix, serie, nResults, hDevices);
					}
					/* process Resource or Vehicle */
					else if (
							   (serie.getTags().get(InfluxDataAccess.INFLUX_TAG_RESOURCE)!=null &&
								!serie.getTags().get(InfluxDataAccess.INFLUX_TAG_RESOURCE).trim().isEmpty()
								) ||
							   (serie.getTags().get(InfluxDataAccess.INFLUX_TAG_VEHICLE)!=null &&
								!serie.getTags().get(InfluxDataAccess.INFLUX_TAG_VEHICLE).trim().isEmpty()
								) 							
							) {
						_parseSerieToResources(oJSONMapper, lPrefix, serie, nResults, hResources);
					}else {
						/* por si invocacion incorrecta y no agr by Device and/or resource*/
						ObjectNode nodeMeasurement = _serieToNodeMeasurement(oJSONMapper, lPrefix, serie, nResults);
						arrayMeasurements.add(nodeMeasurement);
					}
				}    			
			}
		}
		
		
		nodeResult.put("numResults", nResults.get());		
		ObjectNode nodeObjResults = oJSONMapper.createObjectNode();

		/* add devices */
		ArrayNode arrayDevices= _devicesToArrayJSON(oJSONMapper, hDevices);
		if (!arrayDevices.isEmpty()) {
			nodeObjResults.putPOJO(SerializationToJSONConstants.JSON_DEVICES_ARRAY_NAME, arrayDevices);
		}

		/* add resources */
		ArrayNode arrayResources= _resourcesToArrayJSON(oJSONMapper, hResources);
		if (!arrayResources.isEmpty()) {
			nodeObjResults.putPOJO(SerializationToJSONConstants.JSON_RESOURCES_ARRAY_NAME, arrayResources);
		}

		/* add measurements */
		if (!arrayMeasurements.isEmpty()) {
			nodeObjResults.putPOJO(SerializationToJSONConstants.JSON_MEASUREMENTS_ARRAY_NAME, arrayMeasurements);
		}

		nodeResult.putPOJO("results", nodeObjResults);
		return oJSONMapper.writeValueAsString(nodeResult);	
    }
    
    private static ArrayNode _devicesToArrayJSON(ObjectMapper oJSONMapper,HashMap <String, HashMap <String, ArrayNode>> hDevices) {
    	ArrayNode arrayDevices = oJSONMapper.createArrayNode();
    	if(!hDevices.isEmpty()) {
    		for (Map.Entry<String, HashMap <String, ArrayNode>> dev : hDevices.entrySet() ) {
    			ObjectNode nodeDevice = oJSONMapper.createObjectNode();
    			nodeDevice.put(SerializationToJSONConstants.JSON_DEVICE_NAME, dev.getKey());
    			nodeDevice.putPOJO(SerializationToJSONConstants.JSON_RESOURCES_ARRAY_NAME, _resourcesToArrayJSON(oJSONMapper, dev.getValue()));
    			arrayDevices.add(nodeDevice);
    		}
    	}
    	
    	return arrayDevices;
    }
    
    private static ArrayNode _resourcesToArrayJSON(ObjectMapper oJSONMapper, HashMap <String, ArrayNode> hResources) {
    	ArrayNode arrayResources = oJSONMapper.createArrayNode();
		if (!hResources.isEmpty()) {
			for(Map.Entry<String, ArrayNode> res: hResources.entrySet()) {
				ObjectNode nodeResource = oJSONMapper.createObjectNode();
				nodeResource.put(SerializationToJSONConstants.JSON_RESOURCE_NAME,res.getKey() );
				nodeResource.putPOJO(SerializationToJSONConstants.JSON_MEASUREMENTS_ARRAY_NAME, res.getValue());
				arrayResources.add(nodeResource);
			}
		}
		return arrayResources;
    }

    private static void  _parseSerieToDevice(ObjectMapper oJSONMapper, ArrayList<String> lPrefix,  Series serie, NumResult nResults, HashMap <String, HashMap <String, ArrayNode>> hDevices) throws JsonProcessingException {
    	String sDevName =serie.getTags().get(InfluxDataAccess.INFLUX_TAG_DEVICE);
    	//all devices has resources
    	HashMap <String, ArrayNode> hResources= (hDevices.get(sDevName)!=null)?hDevices.get(sDevName): new HashMap<String, ArrayNode>();
    	
    	//all devices has resources
    	/* process Resource => if no resource, resource name ="" */
		if ( 
				(serie.getTags().get(InfluxDataAccess.INFLUX_TAG_RESOURCE)!=null ) ||
				(serie.getTags().get(InfluxDataAccess.INFLUX_TAG_VEHICLE)!=null )
			) {
			_parseSerieToResources(oJSONMapper, lPrefix, serie, nResults, hResources);
		}

		hDevices.put(sDevName, hResources);
    }

    private static void  _parseSerieToResources(ObjectMapper oJSONMapper, ArrayList<String> lPrefix,  Series serie, NumResult nResults, HashMap <String, ArrayNode> hResources) throws JsonProcessingException {
    	String sResName = (serie.getTags().get(InfluxDataAccess.INFLUX_TAG_RESOURCE)!=null)?
    						serie.getTags().get(InfluxDataAccess.INFLUX_TAG_RESOURCE):
    						serie.getTags().get(InfluxDataAccess.INFLUX_TAG_VEHICLE)
    			;
		ArrayNode arrayMeasurementsResource = (hResources.get(sResName)!=null)?hResources.get(sResName):oJSONMapper.createArrayNode();
		arrayMeasurementsResource.add(_serieToNodeMeasurement(oJSONMapper, lPrefix, serie, nResults));
		hResources.put(sResName, arrayMeasurementsResource);    	
    }
    
    
    private static ObjectNode _serieToNodeMeasurement(ObjectMapper oJSONMapper, ArrayList<String> lPrefix,  Series serie, NumResult nResults) throws JsonProcessingException {
		String sColumn;

		ObjectNode nodeMeasurement = oJSONMapper.createObjectNode();
		
		String sMeasurement = (lPrefix!=null && !lPrefix.isEmpty())?_replacePrefix(serie.getName(), lPrefix) :serie.getName();
		
		nodeMeasurement.put(SerializationToJSONConstants.JSON_MEASUREMENT_NAME, sMeasurement);
					
		ArrayNode arrayObservations = oJSONMapper.createArrayNode();
		List<List<Object>> lPoints = serie.getValues();   							
		for(List<Object> lPoint:lPoints) {
			ObjectNode nodePoint = oJSONMapper.createObjectNode();
			for (int i=0; i<serie.getColumns().size();i++) {
				if (lPoint.get(i)!=null) { 
					//exclude duplicated columnas: similar field name and tag name
					sColumn = serie.getColumns().get(i).replaceAll("_\\d$","");
					if (serie.getColumns().get(i)==sColumn || !serie.getColumns().contains(sColumn)) {
   						// conver javaObject to JsonNode
						
						
						/****** exception SHOW MEASUREMENTS => exclude not desirable measurement or remove prefix from measurement name ******/ 
						if (sMeasurement.equals(SCHEMA_MEASUREMENTS_SERIE) && sColumn.equals(SCHEMA_MEASUREMENTS_TAG)) {
							if (!_bExcludeMeasurement((String)lPoint.get(i), GlobalParameters.getInstance().LIST_PREFIX_TO_EXCLUDE)) {
								nodePoint.putPOJO(sColumn, _replacePrefix((String)lPoint.get(i), lPrefix));
							}
						}else {
							nodePoint.putPOJO(sColumn, lPoint.get(i));
						}
   					}
   					//System.out.format("\n\t- %s: %s ==> %s", serie.getColumns().get(i), lPoint.get(i).toString(), sColumn );
   				}
   			}
			/*** exception for SHOW MEASUREMENTS ***/
			if (!nodePoint.isEmpty() ) {
				nResults.increment();
				arrayObservations.add(nodePoint);
			}			
		}
		nodeMeasurement.putPOJO(SerializationToJSONConstants.JSON_OBSERVATIONS_ARRAY_NAME, arrayObservations);
		return nodeMeasurement;
		
    }
    
    private static String _replacePrefix(String sValue, ArrayList<String> lPrefix) {
    	
		if(lPrefix!=null && !lPrefix.isEmpty()) {
			for(String sPrefix : lPrefix) {
				sValue = sValue.replaceAll("^"+sPrefix, "");
			}
		}
		return sValue;
    }
    
    private static boolean _bExcludeMeasurement(String sMeasurement, ArrayList<String> lPrefix) {
    	boolean bExcluded = false;
    	int i=0;
    	while(i<lPrefix.size() && !bExcluded) {
    		bExcluded = sMeasurement.startsWith(lPrefix.get(i));
    		i++;
    	}
		return bExcluded;
    }
}
