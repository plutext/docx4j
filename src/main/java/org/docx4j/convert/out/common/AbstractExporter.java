/*
   Licensed to Plutext Pty Ltd under one or more contributor license agreements.  
   
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License. 

    You may obtain a copy of the License at 

        http://www.apache.org/licenses/LICENSE-2.0 

    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS, 
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
    See the License for the specific language governing permissions and 
    limitations under the License.

 */
package org.docx4j.convert.out.common;

import java.io.IOException;
import java.io.OutputStream;

import org.docx4j.convert.out.AbstractConversionSettings;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exporter are responsible to create the environment for the export process. 
 * They take care of the pre- and post processing (FORenderer). 
 * The conversion process itself is done with the …ExporterDelegate
 * 
 * @since 3.0
 */
public abstract class AbstractExporter<CS extends AbstractConversionSettings, CC extends AbstractConversionContext, PK extends OpcPackage> implements Exporter<CS> {
	protected static Logger LocalLog = LoggerFactory.getLogger(AbstractExporter.class);
	
	protected AbstractExporter() {
	}
	
	@Override
	public void export(CS conversionSettings, OutputStream outputStream) throws Docx4JException {
	PK preprocessedPackage = null;
	ConversionSectionWrappers sectionWrappers = null;
	CC conversionContext = null;
	OutputStream intermediateOutputStream = null;
	long startTime = System.currentTimeMillis();
	long currentTime = startTime;
	//TODO: The log of the conversionContext isn't avaiable until the
	//context has been created. When the log gets passed via the setting
	//use that one instead.
	Logger log = LocalLog;
	
		try {
			log.debug("Start conversion");
			preprocessedPackage = preprocess(conversionSettings);
			currentTime = logDebugStep(log, "Preprocessing", currentTime);
			sectionWrappers = createWrappers(conversionSettings, preprocessedPackage);
			currentTime = logDebugStep(log, "Create section wrappers", currentTime);
			conversionContext = createContext(conversionSettings, preprocessedPackage, sectionWrappers);
			log = conversionContext.getLog();
			currentTime = logDebugStep(log, "Create conversion context", currentTime);
			intermediateOutputStream = createIntermediateOutputStream(outputStream);
			process(conversionSettings, conversionContext, intermediateOutputStream);
			currentTime = logDebugStep(log, "Processing", currentTime);
			postprocess(conversionSettings, conversionContext, intermediateOutputStream, outputStream);
			currentTime = logDebugStep(log, "Postprocessing", currentTime);
			logDebugStep(log, "Conversion done", startTime);
			
		} catch (Exception e) {
			log.error("Exception exporting package", e);
			throw new Docx4JException("Exception exporting package", e);
		} finally {
			// Clean-up
			try {
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	protected long logDebugStep(Logger log, String stepLabel, long startTime) {
	long currentTime = 0;
		if (log.isDebugEnabled()) {
			currentTime = System.currentTimeMillis();
			log.debug(stepLabel + ", " + Long.toString(currentTime - startTime) + "ms");
		}
		return currentTime;
	}

	protected abstract PK preprocess(CS conversionSettings) throws Docx4JException;

	protected abstract ConversionSectionWrappers createWrappers(CS conversionSettings,  PK preprocessedPackage) throws Docx4JException;

	protected abstract CC createContext(CS conversionSettings, PK preprocessedPackage, ConversionSectionWrappers sectionWrappers);

	protected OutputStream createIntermediateOutputStream(OutputStream outputStream) throws Docx4JException {
		//default: no intermediate OutputStream needed (html)
		return outputStream;
	}

	protected abstract void process(CS conversionSettings, CC conversionContext, OutputStream outputStream) throws Docx4JException;
	
	protected void postprocess(CS conversionSettings, AbstractConversionContext conversionContext, OutputStream intermediateOutputStream, OutputStream outputStream) throws Docx4JException {
		//default: no postprocess needed, output has been done on target OutputStream
	}
	
}
