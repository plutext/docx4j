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

import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.model.structure.jaxb.ObjectFactory;
import org.docx4j.model.structure.jaxb.Sections;
import org.docx4j.model.structure.jaxb.Sections.Section;
import org.w3c.dom.Element;

public class ConversionSectionWrappers {
	
	protected List<ConversionSectionWrapper> conversionSections = null;
	protected int currentSectionIndex = -1;
	protected ConversionSectionWrapper currentSection = null;
	
	public ConversionSectionWrappers(List<ConversionSectionWrapper> conversionSections) {
		this.conversionSections = conversionSections;
	}

	public Sections createSections() {
		
		ObjectFactory factory = new ObjectFactory();
		
		Sections ret = factory.createSections();
		for (int i=0; i<conversionSections.size(); i++) {
			ret.getSection().add(createSection(factory, conversionSections.get(i)));
		}
		return ret;
	}
    
	private Section createSection(ObjectFactory factory, ConversionSectionWrapper conversionSectionWrapper) {
		
		Section ret = factory.createSectionsSection();
		ret.setName(conversionSectionWrapper.getId());
		for (int i=0; i<conversionSectionWrapper.getContent().size(); i++) {
			// TODO: since the section model knows nothing about WML,
			// we have to marshall each object separately.
			// To fix this, next time wml is generated, include the section model there!
			ret.getAny().add(marshall(conversionSectionWrapper.getContent().get(i)));
		}
		return ret;
	}

	private static Element marshall(Object o) {
		
		try {
			org.w3c.dom.Document w3cDoc = 
				XmlUtils.marshaltoW3CDomDocument(o);
			
			
				/* Force the RelationshipsPart to be marshalled using
				 * the normal non-rels part NamespacePrefixMapper,
				 * since otherwise (because we'd be using 2 namespace
				 * prefix mappers?) we end up with errant xmlns="",
				 * which is wrong and stops Word 2007 from loading the
				 * document.
				 * 
				 * Note that xmlPackage.xsd defines:
				 * 	<xsd:complexType name="CT_XmlData">
						<xsd:sequence>
							<xsd:any processContents="skip" />
						</xsd:sequence>
				 *
				 * Note also that marshaltoString uses 
				 * just the normal non-rels part NamespacePrefixMapper,
				 * so if/when this is marshalled again, that could
				 * have been causing problems as well?? 
				 */
	        return w3cDoc.getDocumentElement();		        
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 		        
		return null;
		
	}

	public List<ConversionSectionWrapper> getList() {
		return conversionSections;
	}
	
	public void start() {
		currentSectionIndex = -1;
	}
	
	public void next() {
		
		currentSectionIndex++;
		currentSection = null;
		if ((currentSectionIndex >= 0) && (currentSectionIndex < conversionSections.size())) {
			currentSection = conversionSections.get(currentSectionIndex);
		}
		
	}
	
	public ConversionSectionWrapper getCurrentSection() {
		
		if (currentSection != null) 
			return currentSection;
		if (currentSectionIndex < 0) {
			throw new IllegalArgumentException("Trying to access a section, but moveNext wasn't called");
		}
		else if (currentSectionIndex >= conversionSections.size()) {
			throw new IllegalArgumentException("Trying to access more sections than avaiable");
		}
		else {
			throw new IllegalArgumentException("There is no current section"); //?????
		}
	}
	
	public ConversionSectionWrapper peekNextSection() {
		
		int nextSectionIndex=currentSectionIndex+1; 
		if ((nextSectionIndex >= 0) && (nextSectionIndex < conversionSections.size())) {
			return conversionSections.get(nextSectionIndex);
		} else {
			return null;
		}
		
	}
	
}
