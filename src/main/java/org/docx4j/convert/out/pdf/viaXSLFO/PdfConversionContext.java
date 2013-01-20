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
package org.docx4j.convert.out.pdf.viaXSLFO;

import org.docx4j.convert.out.AbstractWmlConversionContext;
import org.docx4j.convert.out.AbstractConversionSettings;
import org.docx4j.convert.out.AbstractMessageWriter;
import org.docx4j.convert.out.AbstractModelRegistry;
import org.docx4j.convert.out.ConversionSectionWrappers;
import org.docx4j.model.images.ConversionImageHandler;
import org.w3c.dom.traversal.NodeIterator;

/**
 * See /docs/developer/Convert_Out.docx for an overview of
 * the design.
 * 
 * @author Alberto Zerolo
 *
 */
public class PdfConversionContext extends AbstractWmlConversionContext {

	protected InField inFieldTracker = new InField();
	
	//The model registry is per output type a singleton
	protected static final AbstractModelRegistry PDF_MODEL_REGISTRY = 
		new AbstractModelRegistry() {
			@Override
			protected void registerDefaultConverterInstances() {
				registerConverter(new TableWriter());
				registerConverter(new SymbolWriter());
			}
		};
			
	//The message writer for pdf	
	protected static final AbstractMessageWriter PDF_MESSAGE_WRITER = new AbstractMessageWriter() {
		@Override
		protected String getOutputPrefix() {
			return "<fo:block xmlns:fo=\"http://www.w3.org/1999/XSL/Format\"  " 
					+ "font-size=\"12pt\" "
		        	+ "color=\"red\" "
		        	+ "font-family=\"sans-serif\" "
		        	+ "line-height=\"15pt\" "
		        	+ "space-after.optimum=\"3pt\" "
		        	+ "text-align=\"justify\"> ";
		}
		@Override
		protected String getOutputSuffix() {
			return "</fo:block>";
		}
	};

	public PdfConversionContext(PdfSettings settings, ConversionSectionWrappers conversionSectionWrappers) {
		super(PDF_MODEL_REGISTRY, PDF_MESSAGE_WRITER, settings, conversionSectionWrappers);
	}
	
	@Override
	protected ConversionImageHandler initializeImageHandler(AbstractConversionSettings settings, ConversionImageHandler handler) {
		if (handler == null) {
			//setup a private image handler if there is none in the configuration.
			handler = (settings.getImageDirPath() != null ? 
					new PDFConversionImageHandler(settings.getImageDirPath(), true) : 
					new PDFConversionImageHandler());
		}
		return handler;
	}
	
	protected InField getInFieldTracker() {
		return inFieldTracker;
	}
	
	public void inFieldUpdateState(NodeIterator fldCharNodeIt) {
		getInFieldTracker().updateState(fldCharNodeIt);
	}
	
	public boolean inFieldGetState() {
		return getInFieldTracker().getState();
	}
	
}
