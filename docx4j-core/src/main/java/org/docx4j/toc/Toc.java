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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.docx4j.jaxb.Context;
import org.docx4j.toc.switches.OSwitch;
import org.docx4j.toc.switches.SwitchInterface;
import org.docx4j.toc.switches.SwitchesFactory;
import org.docx4j.wml.FldChar;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.R;
import org.docx4j.wml.STFldCharType;
import org.docx4j.wml.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Toc {
	
	private static Logger log = LoggerFactory.getLogger(Toc.class);				
	

    private static final String ERROR_BOOKMARK_NOT_DEFINED = "Error! Bookmark not defined.";

    private static final String SPACE = " ";
    private static final String TOC_INSTRUCTION_MASK = "TOC ";
    private static final String DEFAULT_TOC_INSTRUCTION = "TOC \\o \"1-3\" \\h \\z \\u ";
    //private static final String TOC_HEADING_STYLE = "TOCHeading";
    
    protected static String DEFAULT_TOC_HEADING = "Contents";
    
	/**
     * Use the provided text for the ToC heading.
     * (Except in ToC update case where the existing heading P is to be re-used)
     * 
     * @param tocHeadingText
     */
    public static void setTocHeadingText(String tocHeadingText) {
		DEFAULT_TOC_HEADING = tocHeadingText;
	}
    protected static String getTocHeadingText() {
		return DEFAULT_TOC_HEADING;
	}


    private String tocInstruction;
    
    private String errorString = "";


    private static ObjectFactory wmlObjectFactory = Context.getWmlObjectFactory();

    private List<SwitchInterface> switchesList = new ArrayList<SwitchInterface>();

    private Toc()  { }
    
    public Toc(String tocInstruction) throws TocException {
        this.tocInstruction = tocInstruction.trim();
        parseInstruction();
    }

    private void parseInstruction() throws TocException {
    	
    	
        // 1. Incorrect toc instruction
        if(tocInstruction==null || !tocInstruction.startsWith(TOC_INSTRUCTION_MASK)){
//            errorString = ERROR_BOOKMARK_NOT_DEFINED;
//            return;
        	throw new TocException("Invalid TOC instruction " + tocInstruction);
        }
        String toParse = tocInstruction.substring(TOC_INSTRUCTION_MASK.length());
        // 2. Instruction " TOC " - All headings formatted with the built-in heading styles
        if(toParse.isEmpty()){
            switchesList.add(SwitchesFactory.getSwitch(OSwitch.ID));
            return;
        }
        // 3. Parsing switches
    	log.debug("starting to parse: " + toParse);
        String[] switchesArray = toParse.split(SPACE);
        SwitchInterface tocSwitch;
        String fieldArgument;
        for(int i = 0; i < switchesArray.length; i++) {
            tocSwitch = SwitchesFactory.getSwitch(switchesArray[i]);
            if(tocSwitch != null){
                if(i + 1 < switchesArray.length ){
                    fieldArgument = switchesArray[i + 1];
                    if(tocSwitch.hasFieldArgument() && SwitchesFactory.getSwitch(fieldArgument) == null) {
                        i++;
                    } else {
                        fieldArgument = null;
                    }
                } else {
                    fieldArgument = null;
                }
                errorString = tocSwitch.parseFieldArgument(fieldArgument);
                if(!errorString.isEmpty()){
//                    return;
                	throw new TocException(errorString);
                }
                switchesList.add(tocSwitch);
                log.debug("Got switch: " + tocSwitch);
            }
        }
        // 4. Check if switches were found, else use approach as for empty instruction
        if(switchesList.isEmpty()){
            switchesList.add(SwitchesFactory.getSwitch(OSwitch.ID));
        }
        sortSwitchesListByPriority();
    }

    /**
     * The switches are processed in a specific order.
     */
    private void sortSwitchesListByPriority() {
        Collections.sort(switchesList, new Comparator<SwitchInterface>() {
            @Override
            public int compare(SwitchInterface o1, SwitchInterface o2) {
                if(o1.getPriority() > o2.getPriority()){
                    return -1;
                } else {
                    return 1;
                }
            }
        });
    }
    
    public boolean hasErrors(){
        return !errorString.isEmpty();
    }

    public List<SwitchInterface> getSwitches() {
        return switchesList;
    }

//    public static P getTocHeading(String headingStyleId,String tocHeading) {
//    	return generateTocHeading(headingStyleId, tocHeading);
//    }

    @Deprecated
    public List<R> getTocInstruction() {
    	
    	return TocSdtUtils.getTocInstruction(tocInstruction);
    }


//    private P generateEmptyToc() {
//        // Create object for p
//        P p = wmlObjectFactory.createP();
//        // Create object for r
//        R r2 = wmlObjectFactory.createR();
//        p.getContent().add(r2);
//        // Create object for fldChar (wrapped in JAXBElement)
//        FldChar fldchar = wmlObjectFactory.createFldChar();
//        JAXBElement<FldChar> fldcharWrapped = wmlObjectFactory.createRFldChar(fldchar);
//        r2.getContent().add(fldcharWrapped);
//        fldchar.setFldCharType(STFldCharType.BEGIN);
//        // Create object for r
//        R r3 = wmlObjectFactory.createR();
//        p.getContent().add(r3);
//        // Create object for instrText (wrapped in JAXBElement)
//        Text text2 = wmlObjectFactory.createText();
//        JAXBElement<Text> textWrapped2 = wmlObjectFactory.createRInstrText(text2);
//        r3.getContent().add(textWrapped2);
//        text2.setValue(tocInstruction);
//        text2.setSpace(PRESERVE);
//        // Create object for r
//        R r4 = wmlObjectFactory.createR();
//        p.getContent().add(r4);
//        // Create object for fldChar (wrapped in JAXBElement)
//        FldChar fldchar2 = wmlObjectFactory.createFldChar();
//        JAXBElement<FldChar> fldcharWrapped2 = wmlObjectFactory.createRFldChar(fldchar2);
//        r4.getContent().add(fldcharWrapped2);
//        fldchar2.setFldCharType(STFldCharType.SEPARATE);
//        return p;
//    }

//    public P getErrorParagraph(){
//        // Create object for p
//        P p = wmlObjectFactory.createP();
//        // Create object for r
//        R r = wmlObjectFactory.createR();
//        p.getContent().add(r);
//        // Create object for t (wrapped in JAXBElement)
//        Text text = wmlObjectFactory.createText();
//        JAXBElement<Text> textWrapped = wmlObjectFactory.createRT(text);
//        r.getContent().add(textWrapped);
//        text.setValue(errorString);
//        return p;
//    }

}
