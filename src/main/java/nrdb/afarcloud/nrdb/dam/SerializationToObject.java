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
import java.util.List;

import org.influxdb.dto.QueryResult.Result;
import org.influxdb.dto.QueryResult.Series;


abstract class SerializationToObject {
    

    protected  List<Object> resultsToObject(List<Result> results, ArrayList<String> lPrefix) throws Exception {
    	List<Object> lResults= new ArrayList<Object>();
		for(Result res:results) {    				
			if (!res.hasError() && res.getSeries()!=null) {
				
				for (Series serie: res.getSeries()) {		
					lResults.addAll(_serieToNodeMeasurement(lPrefix, serie));					
				}
			}
		}
		
		
		return lResults;	
    }
    
    private  List<Object> _serieToNodeMeasurement(ArrayList<String> lPrefix,  Series serie) throws Exception {
    	List<Object> lObject = new ArrayList<Object>(); 
		Object oObj = null;
		String sMeasurement = (lPrefix!=null && !lPrefix.isEmpty())?_replacePrefix(serie.getName(), lPrefix) :serie.getName();
					
		List<List<Object>> lPoints = serie.getValues();   							
		for(List<Object> lPoint:lPoints) {
			oObj = _pointToObject(sMeasurement, serie.getColumns(), lPoint);
			if (oObj!=null)
				lObject.add(oObj);
			//lObject.add(_pointToObject(sMeasurement, serie.getColumns(), lPoint));
		}
		return lObject;
    }
 

    protected abstract Object _pointToObject(String sMeasurement, List<String>lColumns, List<Object> lPoint) throws Exception;
	
     
    private String _replacePrefix(String sValue, ArrayList<String> lPrefix) {
    	
		if(lPrefix!=null && !lPrefix.isEmpty()) {
			for(String sPrefix : lPrefix) {
				sValue = sValue.replaceAll("^"+sPrefix, "");
			}
		}
		return sValue;

    
    }
}
