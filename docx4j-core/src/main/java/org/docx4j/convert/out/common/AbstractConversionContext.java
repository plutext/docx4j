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

import java.util.HashMap;
import java.util.Map;

import org.docx4j.convert.out.AbstractConversionSettings;
import org.docx4j.convert.out.ConversionHyperlinkHandler;
import org.docx4j.convert.out.common.writer.AbstractMessageWriter;
import org.docx4j.model.images.ConversionImageHandler;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.parts.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.traversal.NodeIterator;

/**
 * See /docs/developer/Convert_Out.docx for an overview of
 * the design.
 * 
 * @author Alberto
 *
 */
public abstract class AbstractConversionContext {
	
	private static final Logger log = LoggerFactory.getLogger(AbstractConversionContext.class);
	
	public static final String CONVERSION_CONTEXT_ID = "conversionContext";
	
	protected static final AbstractMessageWriter DUMMY_WRITER = new AbstractMessageWriter() {
		@Override
		public DocumentFragment notImplemented(AbstractConversionContext context, NodeIterator nodes, String message) {return null;}
		@Override
		public DocumentFragment message(AbstractConversionContext context, String message) {return null;}
		@Override
		protected String getOutputSuffix() {return null;}
		@Override
		protected String getOutputPrefix() {return null;}
	};
	
	protected static final ConversionHyperlinkHandler DUMMY_HYPERLINK_HANDLER = new ConversionHyperlinkHandler() {
		@Override
		public void handleHyperlink(ConversionHyperlinkHandler.Model hyperlinkModel, OpcPackage opcPackage, Part currentPart) throws Docx4JException {
			//do nothing
		}
	};
	
	private Map<String, Object> xsltParameters = null;
	private OpcPackage opcPackage = null;
	private ConversionImageHandler imageHandler = null;
	private ConversionHyperlinkHandler hyperlinkHandler = null;
	private AbstractMessageWriter messageWriter = null;

	private AbstractConversionSettings settings = null;
	/**
	 * @since 3.0.1
	 */
	public AbstractConversionSettings getConversionSettings() {
		return settings;
	}

	protected AbstractConversionContext(AbstractMessageWriter messageWriter, AbstractConversionSettings conversionSettings, OpcPackage localOpcPackage) {
		initializeSettings(conversionSettings, localOpcPackage);
		this.messageWriter = initializeMessageWriter(messageWriter);
	}
	
	protected void initializeSettings(AbstractConversionSettings settings, OpcPackage localOpcPackage) {
		if (settings != null) {
			this.settings=settings;
			if ((localOpcPackage == null) && (settings.getOpcPackage() == null)) {
				throw new IllegalArgumentException("The OpcPackage is missing in the settings.");
			}
			this.imageHandler = initializeImageHandler(settings, settings.getImageHandler());
			this.hyperlinkHandler = initializeHyperlinkHandler(settings, settings.getHyperlinkHandler());
			this.opcPackage = initializeOpcPackage(settings, 
					(localOpcPackage != null ? localOpcPackage : settings.getOpcPackage())
					);
			this.xsltParameters = initializeXsltParameters(settings, settings.getSettings());
		}
	}

	protected ConversionImageHandler initializeImageHandler(AbstractConversionSettings settings, ConversionImageHandler handler) {
		return handler;
	}

	protected ConversionHyperlinkHandler initializeHyperlinkHandler(AbstractConversionSettings settings, ConversionHyperlinkHandler handler) {
		return (handler != null ? handler : DUMMY_HYPERLINK_HANDLER);
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
			ret.remove(AbstractConversionSettings.OPC_PACKAGE);
	
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

	protected ConversionHyperlinkHandler getHyperlinkHandler() {
		return hyperlinkHandler;
	}

	public void handleHyperlink(ConversionHyperlinkHandler.Model model) throws Docx4JException {
		getHyperlinkHandler().handleHyperlink(model, getOpcPackage(), null);
	}

	public AbstractMessageWriter getMessageWriter() {
		return messageWriter;
	}

	public Map<String, Object> getXsltParameters() {
		return xsltParameters;
	}

	
	/**
	 * This method shouldn't be used from regular Java methods, since it
	 * means the log entry doesn't should the actual class causing the problem.
	 * 
	 * @return
	 */
	public Logger getLog() {
		return log;
	}

}
