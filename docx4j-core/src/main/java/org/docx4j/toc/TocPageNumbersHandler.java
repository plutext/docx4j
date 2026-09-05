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
    private static final String PAGE_VIEWPORT = "pageViewport";

    /** FOP's page viewport keys ("P95") mapped to the page's number.  The key is
     *  an identifier, not a page number: it is sequential only while FOP never
     *  re-lays a page out, and the Word layout managers (17.0.5) make it restart
     *  pages, after which a viewport keyed P95 can be page 5.  The formatted
     *  number (what the page shows, and what Word puts in a TOC) is used where it
     *  is an integer, else the plain number.  @since 17.0.5 */
    private final Map<String, Integer> viewportPages = new HashMap<String, Integer>();
    /** bookmark -> viewport key, resolved once the whole tree is read: the links
     *  are the TOC's own entries on its first pages, met before the viewports
     *  they point to */
    private final Map<String, String> pendingKeys = new HashMap<String, String>();

    private Map<String, Integer> pageNumbers;

    @Override
    public void startDocument() throws SAXException {
        pageNumbers = new HashMap<String, Integer>();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if(qName.equals(PAGE_VIEWPORT)){
            String key = attributes.getValue("key");
            String nr = attributes.getValue("formatted-nr");
            Integer page = null;
            try {
                if (nr != null) page = Integer.parseInt(nr.trim());
            } catch (NumberFormatException e) {
                // roman or lettered page numbers: fall back to the plain number
            }
            if (page == null) {
                try {
                    page = Integer.parseInt(attributes.getValue("nr").trim());
                } catch (Exception e) {
                    log.info("Page viewport without a number: key " + key);
                }
            }
            if (key != null && page != null) viewportPages.put(key, page);
            return;
        }
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
        String key = split[0].replace("(", "").trim();   // "P95"
        pendingKeys.put(pageRef, key);
        // the old reading of the key as a number, replaced at the end of the
        // document by the viewport's page number where a viewport was seen
        int pageNumber = 1;
        try{
            pageNumber = Integer.parseInt(split[0].replaceAll(L_BRACKET_P, ""));
        } catch(NumberFormatException ex){
            log.info("Invalid page number: " + value, ex);
        }
        pageNumbers.put(pageRef, pageNumber);
    }

    @Override
    public void endDocument() throws SAXException {
        resolve();
    }

    private void resolve() {
        for (Map.Entry<String, String> e : pendingKeys.entrySet()) {
            Integer page = viewportPages.get(e.getValue());
            if (page != null) pageNumbers.put(e.getKey(), page);
        }
    }

    public Map<String, Integer> getPageNumbers() {
        resolve();
        return pageNumbers;
    }
    
}
