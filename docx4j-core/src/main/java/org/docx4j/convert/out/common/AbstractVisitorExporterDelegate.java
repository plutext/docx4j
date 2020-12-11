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

import java.io.OutputStream;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.AbstractConversionSettings;
import org.docx4j.events.EventFinished;
import org.docx4j.events.StartEvent;
import org.docx4j.events.WellKnownProcessSteps;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.utils.XmlSerializerUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * The …ExporterDelegate generates the html/fo document from the WordprocessingMLPackage.
 * Docx4j supports convert.out via both xslt and non-xslt based approaches.
 * So some …ExporterDelegate use a Xslt transformation;
 * the others use a visitor (…ExporterGenerator)
 * 
 * @since 3.0
 */
public abstract class AbstractVisitorExporterDelegate<CS extends AbstractConversionSettings, CC extends AbstractWmlConversionContext> extends AbstractExporterDelegate<CS, CC> {
	public interface AbstractVisitorExporterGeneratorFactory<CC extends AbstractWmlConversionContext> {
		public AbstractVisitorExporterGenerator<CC> createInstance(CC conversionContext, Document document, Node parentNode);
	}
	
	protected AbstractVisitorExporterGeneratorFactory<CC> generatorFactory = null;

	
	protected AbstractVisitorExporterDelegate(AbstractVisitorExporterGeneratorFactory<CC> generatorFactory) {
		super();
		this.generatorFactory = generatorFactory;
	}


	@Override
	public void process(CS conversionSettings, CC conversionContext, OutputStream outputStream) throws Docx4JException {
		
		StartEvent startEvent = new StartEvent( conversionSettings.getOpcPackage(), WellKnownProcessSteps.OUT_AbstractVisitorExporterDelegate );
		startEvent.publish();		
		
		Document document = null;
		Element documentRoot = null;
		Element documentRootBody = null;
		Element sectionRoot = null;
		Element sectionRootBody = null;
		Element currentParent = null;
		Element flow = null;

		conversionContext.setCurrentPartMainDocument(); 
    	document = XmlUtils.neww3cDomDocument();
    	
    	currentParent = documentRoot = createDocumentRoot(conversionContext, document);
    	document.appendChild(documentRoot);
    	appendDocumentHeader(conversionContext, document, documentRoot); 
    	
    	documentRootBody = createDocumentBody(conversionContext, document, documentRoot);
    	if (documentRootBody != null) {
    		currentParent.appendChild(documentRootBody);
    		currentParent = documentRootBody;
    	}
    	
    	List<ConversionSectionWrapper> sectionWrappers = conversionContext.getSections().getList();
    	for (int secindex=0; secindex < sectionWrappers.size(); secindex++) {
    		ConversionSectionWrapper sectionWrapper = sectionWrappers.get(secindex);
    		conversionContext.getSections().next();
    		
    		sectionRoot = createSectionRoot(conversionContext, document, sectionWrapper, currentParent);
    		if (sectionRoot != null) {
    			currentParent.appendChild(sectionRoot);
    			currentParent = sectionRoot;
    		}
    		appendSectionHeader(conversionContext, document, sectionWrapper, currentParent);
    		sectionRootBody = createSectionBody(conversionContext, document, sectionWrapper, currentParent);
    		if (sectionRootBody != null) {
    			currentParent.appendChild(sectionRootBody);
    			currentParent = sectionRootBody;
    		}
    		
    		generateBodyContent(conversionContext, 
    				document, 
    				sectionWrapper.getContent(), 
    				currentParent);
    		
    		currentParent = sectionRoot;
    		if (currentParent == null) {
    			currentParent = documentRootBody;
    			if (currentParent == null) {
    				currentParent = documentRoot;
    			}
    		}
    		
    		appendSectionFooter(conversionContext, document, sectionWrapper, currentParent);
    		
    		currentParent = documentRootBody;
    		if (currentParent == null) {
				currentParent = documentRoot;
    		}
    	}
    	
    	appendDocumentFooter(conversionContext, document, documentRoot); 
    	
    	writeDocument(conversionContext, document, outputStream);
    	
		new EventFinished(startEvent).publish();
    	
	}


	protected abstract Element createDocumentRoot(CC conversionContext, Document document) throws Docx4JException;
	
	protected void appendDocumentHeader(CC conversionContext, 
			Document document, Element documentRoot) throws Docx4JException {
    	//default no document header, subclasses may change it  	
	}

	protected Element createDocumentBody(CC conversionContext, 
			Document document, Element documentRoot) {
    	//default noop, that is documentRoot is documentRootBody, subclasses may change it  	
		return null;
	}

	protected Element createSectionRoot(CC conversionContext, 
			Document document,  
			ConversionSectionWrapper sectionWrapper, 
			Element currentParent) throws Docx4JException {
    	//default noop, that is documentRoot is sectionRoot or sectionRoot, subclasses may change it  	
		return null;
	}

	protected void appendSectionHeader(CC conversionContext, Document document,
			ConversionSectionWrapper sectionWrapper,
			Element currentParent) throws Docx4JException {
    	//default no section header, subclasses may change it  	
	}

	protected Element createSectionBody(CC conversionContext, Document document, 
			ConversionSectionWrapper sectionWrapper,
			Element currentParent) throws Docx4JException {
    	//default noop, that is sectionRoot is bodyRoot and 
		//if sectionRoot is null then documentRoot becomes bodyRoot, 
		//subclasses may change it  	
    	return null;
	}

	protected void appendPartContent(CC conversionContext, Document document,
			Part part, List<Object> content, Element currentParent) throws Docx4JException {
	Part previousPart = conversionContext.getCurrentPart();
		conversionContext.setCurrentPart(part);
    	generateBodyContent(conversionContext, document, content, currentParent);
    	conversionContext.setCurrentPart(previousPart);
	}	

	protected void generateBodyContent(CC conversionContext, Document document,
			List<Object> content, Element currentParent) throws Docx4JException {
	AbstractVisitorExporterGenerator<CC> generator = 
			generatorFactory.createInstance(conversionContext, document, currentParent);
		new TraversalUtil(content, generator);
	}

	protected void appendSectionFooter(CC conversionContext, Document document,
			ConversionSectionWrapper sectionWrapper,
			Element currentParent) throws Docx4JException {
    	//default no section footer, subclasses may change it  	
	}

	protected void appendDocumentFooter(CC conversionContext, Document document,
			Element documentRoot) throws Docx4JException {
    	//default no document footer, subclasses may change it  	
	}

	protected void writeDocument(CC conversionContext, Document document, OutputStream outputStream) throws Docx4JException {
		
		XmlSerializerUtil.serialize(new DOMSource(document) , new StreamResult(outputStream), true, true);
	}
}
