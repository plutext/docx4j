/*
 *  Copyright 2020, Plutext Pty Ltd.
 *   
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

package org.docx4j.convert.out.microsoft_graph;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.docx4j.Docx4J;
import org.docx4j.convert.out.MicrosoftGraphConversionSettings;
import org.docx4j.convert.out.common.Exporter;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.plutext.msgraph.convert.ConversionException;
import org.plutext.msgraph.convert.XlsxToPdfConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class XlsxToPdfExporter implements Exporter<MicrosoftGraphConversionSettings> {

	private static Logger log = LoggerFactory.getLogger(XlsxToPdfExporter.class);	
	
	private static XlsxToPdfExporter instance = null;
	
	public static Exporter<MicrosoftGraphConversionSettings> getInstance(XlsxToPdfConverter converter) {
		if (instance == null) {
			synchronized(XlsxToPdfExporter.class) {
				if (instance == null) {
					instance = new XlsxToPdfExporter(converter);
				}
			}
		}
		return instance;
	}
	
	private XlsxToPdfConverter converter;
	
	public XlsxToPdfExporter(XlsxToPdfConverter converter) {
		
		this.converter = converter;
	}

	/**
	 * Export to outputStream as PDF.  Make sure you have setOpcPackage in conversionSettings.
	 * That's the only value which matters here.
	 */
	@Override
	public void export(MicrosoftGraphConversionSettings conversionSettings, OutputStream outputStream)
			throws Docx4JException {
		
		OpcPackage pkg = conversionSettings.getOpcPackage();
		
		export(pkg, outputStream);
	}

	
	/**
	 * Export WordprocessingMLPackage to OutputStream
	 * 
	 * @param pkg
	 * @param outputStream
	 * @throws Docx4JException
	 */
	public void export(OpcPackage pkg , OutputStream outputStream) 
			throws Docx4JException {

		if (pkg instanceof SpreadsheetMLPackage) {

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Docx4J.save(pkg, baos);
			byte[] pdfBytes;
			try {
				pdfBytes = converter.convert(baos.toByteArray());
				IOUtils.write(pdfBytes, outputStream);
			} catch (ConversionException e) {
				throw new Docx4JException(e.getMessage(), e);
			} catch (IOException e) {
				throw new Docx4JException(e.getMessage(), e);
			}
			
		} else {
			throw new Docx4JException("XlsxToPdfExporter can't export " + pkg.getClass().getName() );
		}
			
	}

	/**
	 * Export docx file as PDF
	 * 
	 * @param officeFile
	 * @param target
	 * @throws Docx4JException
	 */
	public void export(File officeFile , OutputStream target) throws Docx4JException {
		
		try {
			byte[] pdfBytes = converter.convert(officeFile);
			IOUtils.write(pdfBytes, target);
		} catch (Exception e1) {
			throw new Docx4JException(e1.getMessage(), e1);
		}
				
	}
	
	
}	
