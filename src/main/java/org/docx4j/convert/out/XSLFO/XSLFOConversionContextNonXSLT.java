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
package org.docx4j.convert.out.XSLFO;

import org.docx4j.convert.out.ConversionSectionWrappers;
import org.docx4j.convert.out.pdf.viaXSLFO.PdfConversionContext;
import org.docx4j.convert.out.pdf.viaXSLFO.PdfSettings;
import org.docx4j.model.styles.StyleTree;

/**
 * See /docs/developer/Convert_Out.docx for an overview of
 * the design.
 * 
 * @author Alberto Zerolo
 *
 */
public class XSLFOConversionContextNonXSLT extends PdfConversionContext {
	protected StyleTree styleTree;

	public XSLFOConversionContextNonXSLT(PdfSettings settings, ConversionSectionWrappers conversionSectionWrappers) {
		super(settings, conversionSectionWrappers);
		this.styleTree = initializeStyleTree(settings);
	}

	private StyleTree initializeStyleTree(PdfSettings settings) {
		//catching and swallowing an exception here isn't good,
		//that would cause later on a NPE
		return getWmlPackage().getMainDocumentPart().getStyleTree();
	}

	public StyleTree getStyleTree() {
		return styleTree;
	}
}
