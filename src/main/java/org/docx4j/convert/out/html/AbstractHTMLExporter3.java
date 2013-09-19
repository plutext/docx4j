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

import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.convert.out.common.AbstractExporterDelegate;
import org.docx4j.convert.out.common.AbstractWmlExporter;
import org.docx4j.convert.out.common.AbstractWriterRegistry;
import org.docx4j.convert.out.common.ConversionSectionWrappers;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

public abstract class AbstractHTMLExporter3 extends AbstractWmlExporter<HTMLSettings, HTMLConversionContext>{
	
	protected AbstractWriterRegistry writerRegistry = null;
	
	/**
	 * usual use case
	 * 
	 * @param exporterDelegate
	 */
	protected AbstractHTMLExporter3(AbstractExporterDelegate<HTMLSettings, HTMLConversionContext> exporterDelegate) {
		super(exporterDelegate);
	}

	/**
	 * using a customised WriterRegistry
	 * 
	 * @param exporterDelegate
	 * @param writerRegistry
	 */
	protected AbstractHTMLExporter3(AbstractExporterDelegate<HTMLSettings, HTMLConversionContext> exporterDelegate, AbstractWriterRegistry writerRegistry) {
		super(exporterDelegate);
		this.writerRegistry = writerRegistry; 
	}
	

	@Override
	protected HTMLConversionContext createContext(
			HTMLSettings conversionSettings, 
			WordprocessingMLPackage preprocessedPackage,
			ConversionSectionWrappers sectionWrappers) {
		 if (writerRegistry==null) {
			 return new HTMLConversionContext(conversionSettings, preprocessedPackage, sectionWrappers);
		 } else {
			 return new HTMLConversionContext(writerRegistry, conversionSettings, preprocessedPackage, sectionWrappers);			 
		 }
	}
	
}
