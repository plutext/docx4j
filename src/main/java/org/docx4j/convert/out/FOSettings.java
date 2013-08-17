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
		addFeatures(DEFAULT_PDF_FEATURES);
	}

	public String getApacheFopConfiguration() {
		return (String)settings.get(APACHEFOP_CONFIGURATION);
	}

	public void setApacheFopConfiguration(String apacheFopConfiguration) {
		settings.put(APACHEFOP_CONFIGURATION, apacheFopConfiguration);
	}

	public String getApacheFopMime() {
		return (String)settings.get(APACHEFOP_MIME);
	}

	public void setApacheFopMime(String apacheFopMime) {
		settings.put(APACHEFOP_MIME, apacheFopMime);
	}

	public FORenderer getCustomFoRenderer() {
		return (FORenderer)settings.get(CUSTOM_FO_RENDERER);
	}

	public void setCustomFoRenderer(FORenderer customFoRenderer) {
		settings.put(CUSTOM_FO_RENDERER, customFoRenderer);
	}
	
	public File getFoDumpFile() {
		return (File)settings.get(FO_DUMP_FILE);
	}
	
	public void setFoDumpFile(File foFile) {
		settings.put(FO_DUMP_FILE, foFile);
	}
}	
