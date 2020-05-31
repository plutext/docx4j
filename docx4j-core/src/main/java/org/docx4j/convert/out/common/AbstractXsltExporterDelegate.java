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

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.docx4j.XmlUtils;
import org.docx4j.convert.out.AbstractConversionSettings;
import org.docx4j.events.EventFinished;
import org.docx4j.events.StartEvent;
import org.docx4j.events.WellKnownProcessSteps;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.utils.ResourceUtils;
import org.w3c.dom.Document;

/**
 * The …ExporterDelegate generates the html/fo document from the WordprocessingMLPackage.
 * Docx4j supports convert.out via both xslt and non-xslt based approaches.
 * So some …ExporterDelegate use a Xslt transformation;
 * the others use a visitor (…ExporterGenerator)
 * 
 * @since 3.0
 */
public abstract class AbstractXsltExporterDelegate<CS extends AbstractConversionSettings, CC extends AbstractWmlConversionContext> extends AbstractExporterDelegate<CS, CC> {
	protected String defaultTemplatesResource = null;
	protected Templates defaultTemplates = null;

	protected AbstractXsltExporterDelegate(String defaultTemplatesResource) {
		this.defaultTemplatesResource = defaultTemplatesResource;
	}
	
	@Override
	public void process(CS conversionSettings, CC conversionContext, OutputStream outputStream) throws Docx4JException {
		
		StartEvent startEvent = new StartEvent( conversionSettings.getOpcPackage(), WellKnownProcessSteps.OUT_XsltExporterDelegate );
		startEvent.publish();		
		
		Document domDoc = getSourceDocument(conversionSettings, conversionContext);
		Templates templates = getTemplates(conversionSettings, conversionContext);
		Result intermediateResult = new StreamResult(outputStream);
		XmlUtils.transform(domDoc, templates, conversionContext.getXsltParameters(), intermediateResult);
		
		new EventFinished(startEvent).publish();
		
	}
	
	protected abstract Document getSourceDocument(CS conversionSettings, CC conversionContext) throws Docx4JException;
	
	protected Templates getTemplates(CS conversionSettings, CC conversionContext) throws Docx4JException {
	Templates ret = (Templates)conversionSettings.getCustomXsltTemplates();
		return (ret == null ? getDefaultTemplate() : ret);
	}

	protected Templates getDefaultTemplate() throws Docx4JException {
		if (defaultTemplates == null) {
			//Synchronize it on the XmlUtils.getTransformerFactory() in case 
			//somebody replaces the URIResolver
			synchronized(XmlUtils.getTransformerFactory()) {
				if (defaultTemplates == null) {
					defaultTemplates = loadDefaultTemplates();
				}
			}
		}
		return defaultTemplates;
	}
	
	protected Templates loadDefaultTemplates() throws Docx4JException  {
	Templates ret = null;
	Source xsltSource  = null;
		//do a lazy loading in case the user has a custom template
		try {
			xsltSource  = new StreamSource(ResourceUtils.getResource(defaultTemplatesResource));
			ret = XmlUtils.getTransformerTemplate(xsltSource);
		} catch (IOException e) {
			throw new Docx4JException("Exception loading default template \"" + defaultTemplatesResource + "\", " + e.getMessage(), e);
		} catch (TransformerConfigurationException e) {
			throw new Docx4JException("Exception loading default template \"" + defaultTemplatesResource + "\", " + e.getMessage(), e);
		}
		return ret;
	}

}
