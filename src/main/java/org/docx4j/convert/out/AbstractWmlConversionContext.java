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

import org.docx4j.model.PropertyResolver;
import org.docx4j.model.TransformState;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;

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
}
