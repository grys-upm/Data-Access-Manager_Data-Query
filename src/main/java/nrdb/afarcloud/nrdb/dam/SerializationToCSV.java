package afarcloud.nrdb.dam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import afarcloud.nrdb.config.Constants;
import afarcloud.nrdb.util.DataTypes;

public class SerializationToCSV {
	
	private static final List<String> PARAMS_TIME; 
	static {
		PARAMS_TIME = new ArrayList<String> (Arrays.asList(new String[] { Constants.SRV_PARAM_START_TIME, Constants.SRV_PARAM_END_TIME}));
	}

   /**
    * @param sQuery
    * @param hParams
    * @param lResultsCSV
    * @param sPrefix
    * @return
    */
    public static String resultsToCSV( String sQuery, HashMap<String, String>hParams, String lResultsCSV, String sPrefix ){
		String response = "";
		response += "Query: "+sQuery+"\n";
		response += "Params: "+_paramsTo(hParams);
		response += "\nResults: \n"+ lResultsCSV;
//    	response = lResultsCSV;
    	if(lResultsCSV.contains("name")) {
    		return response;
    	}
    	
    	return response +"\nNo results found";
    	
    }


    private static String _paramsTo(HashMap<String, String>hParams) {
    
    	String params = "";
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
   			params += "\n"+eParam.getKey() +" = "+ sValue;

    	}
    	
    	return params;
    }
    
    
}
