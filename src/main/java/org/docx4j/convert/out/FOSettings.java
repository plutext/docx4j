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
package org.docx4j.convert.out;

import java.io.File;

/** This class contains the configuration for the conversion process.<br>
 *  The conversion is done in two steps: 
 *  <ol>
 *  <li>Generate a fo document</li>
 *  <li>Render it into the desired format</li>
 *  </ol>
 *  
 * 
 */
public class FOSettings extends AbstractConversionSettings {
	/** There is no MIME that explicitly defines fo, only application/xml. If you want
	 *  as the result of the conversion process the fo document then you should 
	 *  use the MIME defined here.   
	 */
	public static final String INTERNAL_FO_MIME = "application/xml-fo";
	public static final String MIME_PDF = "application/pdf";
	
	public static final String APACHEFOP_CONFIGURATION = "apacheFopConfiguration";
	public static final String APACHEFOP_MIME = "apacheFopMime";
	public static final String CUSTOM_FO_RENDERER = "customFoRenderer";
	public static final String FO_DUMP_FILE = "foDumpFile";

	public FOSettings() {
		super();
		addFeatures(ConversionFeatures.DEFAULT_PDF_FEATURES);
	}

	public String getApacheFopConfiguration() {
		return (String)settings.get(APACHEFOP_CONFIGURATION);
	}

	/**
	 * A String with the configuration of the Apache fop factory. If no configuration 
	 * is passed then a default configuration will be generated for each document 
	 * (and a new factory created). 
	 */
	public void setApacheFopConfiguration(String apacheFopConfiguration) {
		settings.put(APACHEFOP_CONFIGURATION, apacheFopConfiguration);
	}

	public String getApacheFopMime() {
		return (String)settings.get(APACHEFOP_MIME);
	}

	/**
	 * The output format of the ApacheFORenderer. If no value is passed, then it will 
	 * be a PDF document. If INTERNAL_FO_MIME is used then the fo document will be 
	 * outputted to the OutputStream. 
	 */
	public void setApacheFopMime(String apacheFopMime) {
		settings.put(APACHEFOP_MIME, apacheFopMime);
	}

	public FORenderer getCustomFoRenderer() {
		return (FORenderer)settings.get(CUSTOM_FO_RENDERER);
	}

	/**
	 * If the rendering should be done with a different fo renderer, then you need to 
	 * pass here your custom implementation of the FORenderer interface. 
	 */
	public void setCustomFoRenderer(FORenderer customFoRenderer) {
		settings.put(CUSTOM_FO_RENDERER, customFoRenderer);
	}
	
	public File getFoDumpFile() {
		return (File)settings.get(FO_DUMP_FILE);
	}
	
	/**
	 * For testing and debugging you can pass here a File object. The intermediate fo document 
	 * will be outputted here. The outputted fo may contain placeholder for the 2 pass conversion. 
	 */
	public void setFoDumpFile(File foFile) {
		settings.put(FO_DUMP_FILE, foFile);
	}
	
	private boolean layoutMasterSetCalculationInProgress = false;

	public boolean lsLayoutMasterSetCalculationInProgress() {
		return layoutMasterSetCalculationInProgress;
	}

	/**
	 * The flag layoutMasterSetCalculationInProgress is used by LayoutMasterSetBuilder, to record
	 * whether for this conversion run, the correct extents have been calculated yet.
	 * User code should not alter this flag.
	 */
	public void setLayoutMasterSetCalculationInProgress(boolean layoutMasterSetCalculationInProgress) {
		this.layoutMasterSetCalculationInProgress = layoutMasterSetCalculationInProgress;
	}	
}	
