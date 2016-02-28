/*
 *  Copyright 2013-2016, Plutext Pty Ltd.
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
package org.docx4j.toc;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class TocPageNumbersHandler extends DefaultHandler {
	
	private static Logger log = LoggerFactory.getLogger(TocPageNumbersHandler.class);	

    private static final String L_BRACKET_P = "\\(P";
    private static final String R_BRACKET = ")";
    private static final String COMMA = ",";
    private static final String INTERNAL_LINK = "internal-link";
    private static final String INLINE_PARENT = "inlineparent";

    private Map<String, Integer> pageNumbers;

    @Override
    public void startDocument() throws SAXException {
        pageNumbers = new HashMap<String, Integer>();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if(qName.equals(INLINE_PARENT)){
            String aQName;
            for(int i=0; i < attributes.getLength(); i++){
                aQName = attributes.getQName(i);
                if(aQName.equals(INTERNAL_LINK)){
                    parseValue(attributes.getValue(i));
                }
            }
        }
    }

    private void parseValue(String value){
        String[] split = value.split(COMMA);
        String pageRef = split[1].replace(R_BRACKET, "");
        int pageNumber = 1;
        try{
            pageNumber = Integer.parseInt(split[0].replaceAll(L_BRACKET_P, ""));
        } catch(NumberFormatException ex){
            log.info("Invalid page number: " + value, ex);
        }
        pageNumbers.put(pageRef, pageNumber);
    }

    public Map<String, Integer> getPageNumbers() {
        return pageNumbers;
    }
    
}
