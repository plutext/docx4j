/*
 *  Copyright 2010-2013, Plutext Pty Ltd.
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

package org.docx4j.model.datastorage;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P.Hyperlink;


/**
 * Override this class if you want to customise hyperlink handling.
 * You can override what is recognised as a URL, and how that is 
 * converted into a hyperlink.
 * 
 * @author jharrop
 * @since 3.0.0
 */
public class BindingHyperlinkResolver {
	
	private static Logger log = LoggerFactory.getLogger(BindingHyperlinkResolver.class);		
	
	public void setHyperlinkStyle (
			String hyperlinkStyleID) {
		hyperlinkStyleId = hyperlinkStyleID;
	}

	public String getHyperlinkStyleId() {
		return hyperlinkStyleId;
	}
	
	private String hyperlinkStyleId = null;	
	
	/**
	 * Return the index of the position you wish to treat as a hyperlink.
	 * The characters from there to the first whitespace following will be treated as 
	 * the URL
	 * 
	 * @param text
	 * @return
	 */
	public int getIndexOfURL(String text) {

		log.debug("processing " + text);		
		
		int NOT_FOUND = 99999; // Since we'll calculate min, we don't want -1 for no match
		int pos1 = text.indexOf("http://")==-1 ? NOT_FOUND : text.indexOf("http://");
		int pos2 = text.indexOf("https://")==-1 ? NOT_FOUND : text.indexOf("https://");
		int pos3 = text.indexOf("mailto:")==-1 ? NOT_FOUND : text.indexOf("mailto:");
		
		int pos = Math.min(pos1,  Math.min(pos2, pos3));	
		
		if (pos==NOT_FOUND) return -1;
		
		return pos;
	}
	
	/**
	 * @param relId
	 * @param url
	 * @return
	 * @throws JAXBException
	 */
	public Hyperlink generateHyperlink(String relId, String url) throws JAXBException {
		
		String hpl = "<w:hyperlink r:id=\"" + relId + "\" xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" " +
		        "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" >" +
		        "<w:r>" +
		        "<w:rPr>" +
		        "<w:rStyle w:val=\"" + getHyperlinkStyleId() + "\" />" +  
		        "</w:rPr>" +
		        "<w:t>" + url + "</w:t>" +
		        "</w:r>" +
		        "</w:hyperlink>";

		return (Hyperlink)XmlUtils.unmarshalString(hpl);		
	}
	
	
	/**
	 * Enable the hyperlinkStyle in the docx. 
	 * 
	 * @param wordMLPackage
	 * @param hyperlinkStyleId
	 */
	public void activateHyperlinkStyle(WordprocessingMLPackage wordMLPackage) {
		
		if (hyperlinkStyleId !=null) {
			wordMLPackage.getMainDocumentPart().getPropertyResolver().activateStyle(getHyperlinkStyleId());
		}			
	}

}
