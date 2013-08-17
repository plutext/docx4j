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
package org.pptx4j.convert.out.svginhtml;

import org.docx4j.convert.out.AbstractConversionSettings;
import org.docx4j.convert.out.common.writer.AbstractMessageWriter;
import org.docx4j.convert.out.html.HTMLConversionImageHandler;
import org.docx4j.model.images.ConversionImageHandler;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.pptx4j.convert.out.AbstractPmlConversionContext;
import org.pptx4j.model.ResolvedLayout;

public class SvgConversionContext extends AbstractPmlConversionContext {
	//The message writer for svg (the same as for html)	
	protected static final AbstractMessageWriter SVG_MESSAGE_WRITER = new AbstractMessageWriter() {
		@Override
		protected String getOutputSuffix() {
			return "</div>";
		}
		@Override
		protected String getOutputPrefix() {
			return "<div style=\"color:red\" >";
		}
	};		
	
	protected ResolvedLayout resolvedLayout = null;
	
	protected SvgConversionContext(SvgExporter.SvgSettings conversionSettings, PresentationMLPackage localPmlPackage, ResolvedLayout resolvedLayout) {
		super(SVG_MESSAGE_WRITER, conversionSettings, localPmlPackage);
		this.resolvedLayout = resolvedLayout;
	}

	@Override
	protected ConversionImageHandler initializeImageHandler(AbstractConversionSettings settings, ConversionImageHandler handler) {
	SvgExporter.SvgSettings svgSettings = (SvgExporter.SvgSettings)settings;
		if (handler == null) {
			handler = new HTMLConversionImageHandler(
						svgSettings.getImageDirPath(), 
						svgSettings.getImageTargetUri(), 
						svgSettings.isImageIncludeUUID());
		}
		return handler;
	}
	
	public ResolvedLayout getResolvedLayout() {
		return resolvedLayout;
	}
	
}
