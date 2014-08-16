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

import java.util.Map;

import org.docx4j.convert.out.AbstractConversionSettings;
import org.docx4j.convert.out.ConversionHyperlinkHandler;
import org.docx4j.convert.out.common.writer.AbstractMessageWriter;
import org.docx4j.fonts.RunFontSelector;
import org.docx4j.model.PropertyResolver;
import org.docx4j.model.styles.StyleTree;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.wml.STFldCharType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * See /docs/developer/Convert_Out.docx for an overview of
 * the design.
 * 
 * @author Alberto
 *
 */
public abstract class AbstractWmlConversionContext extends AbstractConversionContext {
	
	private static Logger log = LoggerFactory.getLogger(AbstractWmlConversionContext.class);
	
	
	private Map<String, Writer.TransformState> transformStates = null;
	private AbstractWriterRegistry writerRegistry = null;
	
	//The part for the part tracker 
	protected Part currentPart = null;
	
	//The level of the current complex field definitions
	protected int complexFieldDefinitionLevel = 0;
	
	//The counters for the footnote and endnote number 
	protected int footnoteNumberCounter = 0;
	protected int endnoteNumberCounter = 0;
	
	//The section information that gets converted
	protected ConversionSectionWrappers conversionSectionWrappers = null;
	
	//Shortcut to the style tree of the document 
	protected StyleTree styleTree = null;
	
	private RunFontSelector runFontSelector = null;
	
	protected AbstractWmlConversionContext(AbstractWriterRegistry writerRegistry, 
			AbstractMessageWriter messageWriter, AbstractConversionSettings conversionSettings, 
			WordprocessingMLPackage wmlPackage, ConversionSectionWrappers conversionSectionWrappers,
			RunFontSelector runFontSelector) {
		
		super(messageWriter, conversionSettings, wmlPackage);
		
		this.writerRegistry = initializeWriterRegistry(writerRegistry);
		this.transformStates = initializeTransformStates();
		this.conversionSectionWrappers = conversionSectionWrappers;
		this.styleTree = initializeStyleTree();
		this.runFontSelector = runFontSelector; 		
	}

	@Override
	protected OpcPackage initializeOpcPackage(AbstractConversionSettings conversionSettings, OpcPackage opcPackage) {
	OpcPackage ret = super.initializeOpcPackage(conversionSettings, opcPackage);
		if (!(ret instanceof WordprocessingMLPackage)) {
			throw new IllegalArgumentException("The opcPackage isn't a WordprocessingMLPackage, it is a " + ret.getClass().getName());
		}
		resolveLinkedAbstractNum((WordprocessingMLPackage)ret);
		return ret;
	}

	protected AbstractWriterRegistry initializeWriterRegistry(AbstractWriterRegistry registry) {
		return registry;
	}
	
	protected Map<String, Writer.TransformState> initializeTransformStates() {
		return getWriterRegistry().createTransformStates();
	}
	
	protected void resolveLinkedAbstractNum(WordprocessingMLPackage wmlPkg) {
		
		if (wmlPkg.getMainDocumentPart().getStyleDefinitionsPart(false)!=null
				&& wmlPkg.getMainDocumentPart().getNumberingDefinitionsPart()!=null) {
			
			 wmlPkg.getMainDocumentPart().getNumberingDefinitionsPart().resolveLinkedAbstractNum(
					 wmlPkg.getMainDocumentPart().getStyleDefinitionsPart(false));
		}
	}

	protected StyleTree initializeStyleTree() {
		//catching and swallowing an exception here isn't good,
		//that would cause later on a NPE
		return getWmlPackage().getMainDocumentPart().getStyleTree();
	}
	
	public Writer.TransformState getTransformState(String name) {
		return (transformStates != null ? transformStates.get(name) : null);
	}
	
	public WordprocessingMLPackage getWmlPackage() {
		return (WordprocessingMLPackage)getOpcPackage();
	}
	
	public AbstractWriterRegistry getWriterRegistry() {
		return writerRegistry;
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

	public StyleTree getStyleTree() {
		return styleTree;
	}
	
	/**
	 * @return the runFontSelector
	 */
	public RunFontSelector getRunFontSelector() {
		return runFontSelector;
	}
	
	@Override
	public void handleHyperlink(ConversionHyperlinkHandler.Model model) throws Docx4JException {
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
