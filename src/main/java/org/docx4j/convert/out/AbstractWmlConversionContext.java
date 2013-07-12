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

import java.util.Map;

import org.docx4j.convert.out.common.writer.AbstractMessageWriter;
import org.docx4j.model.PropertyResolver;
import org.docx4j.model.TransformState;
import org.docx4j.model.fields.HyperlinkModel;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.wml.STFldCharType;

/**
 * See /docs/developer/Convert_Out.docx for an overview of
 * the design.
 * 
 * @author Alberto Zerolo
 *
 */
public abstract class AbstractWmlConversionContext extends AbstractConversionContext {
	
	private Map<String, TransformState> transformStates = null;
	private AbstractModelRegistry modelRegistry = null;
	
	//The part for the part tracker 
	protected Part currentPart = null;
	
	//The level of the current complex field definitions
	protected int complexFieldDefinitionLevel = 0;
	
	//The counters for the footnote and endnote number 
	protected int footnoteNumberCounter = 0;
	protected int endnoteNumberCounter = 0;
	
	//The section information that gets converted
	protected ConversionSectionWrappers conversionSectionWrappers = null;
	

	protected AbstractWmlConversionContext(AbstractModelRegistry modelRegistry, AbstractMessageWriter messageWriter, AbstractConversionSettings conversionSettings, ConversionSectionWrappers conversionSectionWrappers) {
		super(messageWriter, conversionSettings);
		this.modelRegistry = initializeModelRegistry(modelRegistry);
		this.transformStates = initializeTransformStates();
		this.conversionSectionWrappers = conversionSectionWrappers;
	}

	@Override
	protected OpcPackage initializeOpcPackage(AbstractConversionSettings conversionSettings, OpcPackage opcPackage) {
	OpcPackage ret = super.initializeOpcPackage(conversionSettings, opcPackage);
		if (!(ret instanceof WordprocessingMLPackage)) {
			throw new IllegalArgumentException("The opcPackage isn't a WordprocessingMLPackage, it is a " + ret.getClass().getName());
		}
		return ret;
	}

	protected AbstractModelRegistry initializeModelRegistry(AbstractModelRegistry registry) {
		return registry;
	}
	
	protected Map<String, TransformState> initializeTransformStates() {
		return getModelRegistry().createTransformStates();
	}
	
	public TransformState getTransformState(String name) {
		return (transformStates != null ? transformStates.get(name) : null);
	}
	
	public WordprocessingMLPackage getWmlPackage() {
		return (WordprocessingMLPackage)getOpcPackage();
	}
	
	public AbstractModelRegistry getModelRegistry() {
		return modelRegistry;
	}
	
	public PropertyResolver getPropertyResolver() {
		return getWmlPackage().getMainDocumentPart().getPropertyResolver();
	}
	
    public int getNextEndnoteNumber() {
    	return ++endnoteNumberCounter;
    }

    public int getNextFootnoteNumber() {
    	return ++footnoteNumberCounter;
    }

	public void setCurrentPart(Part currentPart) {
		this.currentPart = currentPart;
	}

	public Part getCurrentPart() {
		return currentPart;		
	}
	
	public void setCurrentPartMainDocument() {
		setCurrentPart(getWmlPackage().getMainDocumentPart());
	}
	
	public ConversionSectionWrappers getSections() {
		return conversionSectionWrappers;
	}

	@Override
	public void handleHyperlink(HyperlinkModel model) throws Docx4JException {
		getHyperlinkHandler().handleHyperlink(model, getOpcPackage(), getCurrentPart());
	}
	
	public void updateComplexFieldDefinition(STFldCharType fieldCharType) {
		//If the level == 1 then separate or end will reduce the level
		//If the level > 1 then only end will reduce the level
		//The level won't go below 0
		if (fieldCharType == STFldCharType.BEGIN) {
			complexFieldDefinitionLevel++;
		}
		else if (fieldCharType == STFldCharType.SEPARATE) {
			if (complexFieldDefinitionLevel == 1) complexFieldDefinitionLevel--; 
		}
		else if (fieldCharType == STFldCharType.END) {
			if (complexFieldDefinitionLevel > 0) complexFieldDefinitionLevel--;
		}
	}
	
	/** Returns true, if it is inside of the outmost any complex field 
	 *  definition (i.e. between the BEGIN and SEPARATE).
	 * 
	 * @return
	 */
	public boolean isInComplexFieldDefinition() {
		return (complexFieldDefinitionLevel > 0);
	}
}
