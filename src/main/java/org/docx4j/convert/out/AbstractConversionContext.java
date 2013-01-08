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

import java.util.HashMap;
import java.util.Map;

import org.docx4j.model.images.ConversionImageHandler;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.traversal.NodeIterator;

/**
 * See /docs/developer/Convert_Out.docx for an overview of
 * the design.
 * 
 * @author Alberto Zerolo
 *
 */
public abstract class AbstractConversionContext {

	public static final String CONVERSION_CONTEXT_ID = "conversionContext";
	
	protected static final AbstractMessageWriter DUMMY_WRITER = new AbstractMessageWriter() {
		@Override
		public DocumentFragment notImplemented(NodeIterator nodes, String message) {return null;}
		@Override
		public DocumentFragment message(String message) {return null;}
		@Override
		protected String getOutputSuffix() {return null;}
		@Override
		protected String getOutputPrefix() {return null;}
	};
	
	private Map<String, Object> xsltParameters = null;
	private OpcPackage opcPackage = null;
	private ConversionImageHandler imageHandler = null;
	private AbstractMessageWriter messageWriter = null;

	protected AbstractConversionContext(AbstractMessageWriter messageWriter, AbstractConversionSettings conversionSettings) {
		initializeSettings(conversionSettings);
		this.messageWriter = initializeMessageWriter(messageWriter);
	}
	
	protected void initializeSettings(AbstractConversionSettings settings) {
		if (settings != null) {
			if (settings.getWmlPackage() == null) {
				throw new IllegalArgumentException("The OpcPackage is missing in the settings.");
			}
			this.imageHandler = initializeImageHandler(settings, settings.getImageHandler());
			this.opcPackage = initializeOpcPackage(settings, settings.getWmlPackage());
			this.xsltParameters = initializeXsltParameters(settings, settings.getSettings());
		}
	}

	protected ConversionImageHandler initializeImageHandler(AbstractConversionSettings settings, ConversionImageHandler handler) {
		return handler;
	}

	protected OpcPackage initializeOpcPackage(AbstractConversionSettings settings, OpcPackage opcPackage) {
		return opcPackage;
	}

	protected Map<String, Object> initializeXsltParameters(AbstractConversionSettings settings, Map<String, Object> settingParameters) {
	Map<String, Object> ret = new HashMap<String, Object>();
		if (settingParameters != null) {
			ret.putAll(settingParameters);
			//remove from the parameters items that get duplicated in the context
			ret.remove(AbstractConversionSettings.IMAGE_HANDLER);
			ret.remove(AbstractConversionSettings.WML_PACKAGE);
	
			//these are implicitly included in the ImageHandler
			ret.remove(AbstractConversionSettings.IMAGE_INCLUDE_UUID);
			ret.remove(AbstractConversionSettings.IMAGE_DIR_PATH);
		}
		ret.put(CONVERSION_CONTEXT_ID, this);
		return ret;
	}

	protected AbstractMessageWriter initializeMessageWriter(AbstractMessageWriter writer) {
		return (writer != null ? writer : DUMMY_WRITER);
	}
	
	//Only for the subclasses
	protected OpcPackage getOpcPackage() {
		return opcPackage;
	}

	public ConversionImageHandler getImageHandler() {
		return imageHandler;
	}

	public AbstractMessageWriter getMessageWriter() {
		return messageWriter;
	}

	public Map<String, Object> getXsltParameters() {
		return xsltParameters;
	}
	

}
