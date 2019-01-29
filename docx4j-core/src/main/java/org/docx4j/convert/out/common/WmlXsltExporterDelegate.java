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

import org.docx4j.XmlUtils;
import org.docx4j.convert.out.AbstractConversionSettings;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.w3c.dom.Document;

/**
 * The …ExporterDelegate generates the html/fo document from the WordprocessingMLPackage.
 * Docx4j supports convert.out via both xslt and non-xslt based approaches.
 * So some …ExporterDelegate use a Xslt transformation;
 * the others use a visitor (…ExporterGenerator)
 * 
 * @since 3.0
 */
public class WmlXsltExporterDelegate<CS extends AbstractConversionSettings, CC extends AbstractWmlConversionContext> extends AbstractXsltExporterDelegate<CS, CC> {

	public WmlXsltExporterDelegate(String defaultTemplatesResource) {
		super(defaultTemplatesResource);
	}

	@Override
	protected Document getSourceDocument(
			CS conversionSettings,
			CC conversionContext)
			throws Docx4JException {
		ConversionSectionWrappers conversionSectionWrappers = conversionContext.getSections();
		Document ret = XmlUtils.marshaltoW3CDomDocument(conversionSectionWrappers.createSections(),
				Context.jcSectionModel);
		return ret;
	}

}
