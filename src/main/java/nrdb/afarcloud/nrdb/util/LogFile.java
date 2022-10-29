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

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.xml.DOMConfigurator;;


/**
 * @proyect AFarCloud
 * 
 * @author UPM
 * @date 20
 */
public class LogFile {
	static private LogFile oInstance = null;

	private final String sFilename = "config/AFC_DMA_log4j.xml";
	private static Logger oLogRoot = null;
	/**
	 * 
	 */
	public LogFile() {
		try{
//			System.out.println("thread :: "+ Thread.currentThread().getContextClassLoader().getResource("").getPath());

			DOMConfigurator.configure(getClass().getClassLoader().getResource(this.sFilename));
			
			
		}catch(Throwable ioEx){
			initRoot();
			oLogRoot.warn(
					"$ERROR$DBM$LOAD_LOG4J:: filename '" + this.sFilename + "'\n " +
							"There must be in the CLASSPATH " +
							(ioEx.getMessage()!=null?ioEx.getMessage():"")
					);
		}		 	

	}

	/**
	 * DEBUG
	 */
	@SuppressWarnings("rawtypes")
	public void debug(Class oClass,  String sMsg){
		Logger oLogger= getLogger(oClass);
		if (oLogger!=null){
			oLogger.debug("["+ oClass.getName() + "] - " + sMsg);		
		}
	}

	/**
	 * WARN
	 */
	@SuppressWarnings("rawtypes")
	public void warn(Class oClass,  String sMsg){
		Logger oLogger= getLogger(oClass);
		if (oLogger!=null){
			oLogger.info("["+ oClass.getName() + "] - " + sMsg);		
		}
	}

	/**
	 * ERROR
	 */
	@SuppressWarnings("rawtypes")
	public void error(Class oClass,  String sMsg){
		Logger oLogger= getLogger(oClass);
		if (oLogger!=null){
			oLogger.error("["+ oClass.getName() + "] - " + sMsg);		
		}
	}

	/**
	 * FATAL
	 */
	@SuppressWarnings("rawtypes")
	public void fatal(Class oClass,  String sMsg){
		Logger oLogger= getLogger(oClass);
		if (oLogger!=null){
			oLogger.fatal("["+ oClass.getName() + "] - " + sMsg);		
		}
	}
	

	/**
	 * basic configuration: console
	 */
	private void initRoot() {
		oLogRoot = Logger.getRootLogger();
		oLogRoot.addAppender(new ConsoleAppender(new PatternLayout("%d %-5p [%t] - %m%n")));
	}

	@SuppressWarnings("rawtypes")
	private static Logger getLogger(Class clase){
		Logger oLogger = null;
		if (oLogRoot!=null) {
			oLogger = oLogRoot;
		} else {
			try {
				oLogger = Logger.getLogger(clase.getName());
			} catch (Exception e) {
				oLogger = Logger.getRootLogger();
			}
		}
		return oLogger;
	}

	/**
	 * singlenton pattern
	 */

	public static synchronized  LogFile getInstance(){
		if (oInstance == null){
			oInstance = new LogFile();
		}
		return oInstance;		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		LogFile.getInstance().debug(LogFile.class, "uno");

		LogFile.getInstance().warn(LogFile.class, "dos");
		LogFile.getInstance().error(LogFile.class, "tres");
	}

}
