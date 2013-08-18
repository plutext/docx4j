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
