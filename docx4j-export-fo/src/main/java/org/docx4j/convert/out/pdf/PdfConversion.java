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
package org.docx4j.convert.out.pdf;

import java.io.OutputStream;

import org.docx4j.convert.out.pdf.viaXSLFO.PdfSettings;
import org.docx4j.openpackaging.exceptions.Docx4JException;


/**
 * There are 3 ways a package can be converted to PDF:
 * 
 * 1. via XSL FO
 * 1a - using XSLT to generate the XSL FO
 * 1b - using TraversalUtils to generate the XSL FO
 * 
 * 2. via HTML, using docX2HTML.xslt
 * 
 * 3. via iText
 * 
 * Method 1a is the standard way of doing things
 * Method 1b is experimental (Dec 2012)
 * 
 * Methods 2 & 3 are in docx4j extras
 * 
 * @author jharrop
 * @deprecated
 */
public abstract class PdfConversion  {
	
	@Deprecated
	public abstract void outputXSLFO(OutputStream os, PdfSettings settings) throws Docx4JException;	

	@Deprecated
	public abstract void output(OutputStream os, PdfSettings settings) throws Docx4JException;
	
}
