/*
 *  Copyright 2025, Plutext Pty Ltd.
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
package org.docx4j.samples;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.toc.TocIntoSdt;
import org.docx4j.toc.TocSdtUtils;
import org.docx4j.wml.CTSdtDocPart;
import org.docx4j.wml.CTSimpleField;
import org.docx4j.wml.FldChar;
import org.docx4j.wml.P;
import org.docx4j.wml.STFldCharType;
import org.docx4j.wml.SdtBlock;
import org.docx4j.wml.SdtContentBlock;
import org.docx4j.wml.SdtPr;
import org.docx4j.wml.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.JAXBElement;

/**
 *  Modern versions of Word typically insert a ToC within an SDT (aka content control).
 *  
 *  For example:
 * 
    <w:sdt>
      <w:sdtPr>
        <w:docPartObj>
          <w:docPartGallery w:val="Table of Contents"/>
          
 * This was not the case for now-ancient versions of Word.  
 * 
 * And nor is it the case for a table of figures even now.  For example:
 * 
    <w:p>
      <w:fldSimple w:instr=" TOC \h \z \c &quot;Figure&quot; ">
 *
 *  or
 * 
      <w:r>
        <w:fldChar w:fldCharType="begin"/>
      </w:r>
      <w:r>
        <w:instrText xml:space="preserve"> TOC \h \z \c "Figure" </w:instrText>
      </w:r>
      <w:r>
        <w:fldChar w:fldCharType="separate"/>
        :
 * 
 * The purpose of this class is to find a "bare" TOC and wrap it in a content control.
 * 
 * Once this has been done, you can use TocGenerator's updateToc method on it.
 * 
 */
public class TocIntoSdtDemo  { 

	private static Logger log = LoggerFactory.getLogger(TocIntoSdtDemo.class);
	
	static String inputfilepath = System.getProperty("user.dir") + "/TOC.docx";

	static String outputfilepath = System.getProperty("user.dir") + "/OUT_TocIntoSdt.docx";
	    
    public static void main(String[] args) throws Exception{
    	
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new File(inputfilepath));
        System.out.println(wordMLPackage.getMainDocumentPart().getXML());

        TocIntoSdt tocIntoSdt = new TocIntoSdt();
        tocIntoSdt.process(wordMLPackage);
        
        wordMLPackage.save(new java.io.File(outputfilepath) );        
    }
    
}


