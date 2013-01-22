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
package org.docx4j.convert.out.html;

import org.docx4j.convert.out.AbstractWmlConversionContext;
import org.docx4j.convert.out.AbstractConversionSettings;
import org.docx4j.convert.out.AbstractMessageWriter;
import org.docx4j.convert.out.AbstractModelRegistry;
import org.docx4j.convert.out.ConversionSectionWrappers;
import org.docx4j.convert.out.html.AbstractHtmlExporter.HtmlSettings;
import org.docx4j.fonts.Mapper;
import org.docx4j.model.images.ConversionImageHandler;

/**
 * See /docs/developer/Convert_Out.docx for an overview of
 * the design.
 * 
 * @author Alberto Zerolo
 *
 */
public class HTMLConversionContext extends AbstractWmlConversionContext {
	protected Mapper fontMapper = null;
	
	//The model registry is per output type a singleton
	protected static final AbstractModelRegistry HTML_MODEL_REGISTRY = 
		new AbstractModelRegistry() {
			@Override
			protected void registerDefaultConverterInstances() {
				registerConverter(new TableWriter());
				registerConverter(new SymbolWriter());
			}
		};

	//The message writer for html	
	protected static final AbstractMessageWriter HTML_MESSAGE_WRITER = 
		new AbstractMessageWriter() {
		
			@Override
			protected String getOutputPrefix() {
				return "<div style=\"color:red\" >";
			}
			@Override
			protected String getOutputSuffix() {
				return "</div>";
			}
		};		
	
	public HTMLConversionContext(HtmlSettings settings, ConversionSectionWrappers conversionSectionWrappers) {
		super(HTML_MODEL_REGISTRY, HTML_MESSAGE_WRITER, settings, conversionSectionWrappers);
	}

	@Override
	protected void initializeSettings(AbstractConversionSettings settings) {
	HtmlSettings htmlSettings = null;
		if (settings == null) {
			settings = new HtmlSettings();
		}
		else if (!(settings instanceof HtmlSettings)) {
			throw new IllegalArgumentException("The class of the settings isn't HtmlSettings, it is " + settings.getClass().getName());
		}
		htmlSettings = (HtmlSettings)settings;
		super.initializeSettings(htmlSettings);
		fontMapper = htmlSettings.getFontMapper();
		if (fontMapper == null) {
			fontMapper = getWmlPackage().getFontMapper();
		}
	}

	@Override
	protected ConversionImageHandler initializeImageHandler(AbstractConversionSettings settings, ConversionImageHandler handler) {
	HtmlSettings htmlSettings = (HtmlSettings)settings;
		if (handler == null) {
			handler = new HTMLConversionImageHandler(
						htmlSettings.getImageDirPath(), 
						htmlSettings.getImageTargetUri(), 
						htmlSettings.isImageIncludeUUID());
		}
		return handler;
	}

	public Mapper getFontMapper() {
		return fontMapper;
	}
}
