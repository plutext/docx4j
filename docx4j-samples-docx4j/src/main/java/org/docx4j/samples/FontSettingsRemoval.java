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

package org.docx4j.samples;


import java.io.File;
import java.math.BigInteger;
import java.util.List;
import java.util.Map.Entry;

import org.docx4j.Docx4J;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.EndnotesPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FootnotesPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart;
import org.docx4j.utils.SingleTraversalUtilVisitorCallback;
import org.docx4j.utils.TraversalUtilVisitor;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.DocDefaults;
import org.docx4j.wml.DocDefaults.RPrDefault;
import org.docx4j.wml.HpsMeasure;
import org.docx4j.wml.Lvl;
import org.docx4j.wml.Numbering;
import org.docx4j.wml.Numbering.AbstractNum;
import org.docx4j.wml.Numbering.Num;
import org.docx4j.wml.Numbering.Num.LvlOverride;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.RFonts;
import org.docx4j.wml.RPr;
import org.docx4j.wml.RPrAbstract;
import org.docx4j.wml.Style;


/**
 * Example showing how to remove font type and size settings
 * from throughout the document, and setting these in rPrDefault.
 * 
 * @author jharrop
 *
 */
public class FontSettingsRemoval extends AbstractSample {

	// target settings
	private final static String FONT_NAME = "Arial";
	private final static int FONT_SIZE_POINTS = 11;
	
	public static void main(String[] args) throws Exception {

		try {
			getInputFilePath(args);
		} catch (IllegalArgumentException e) {
	    	inputfilepath = System.getProperty("user.dir") + "/yourfile.docx";
		}
		System.out.println(inputfilepath);	    	
		
		
		// Load the docx
		WordprocessingMLPackage wordMLPackage = Docx4J.load(new java.io.File(inputfilepath));

		// Remove settings from MDP, headers/footers, footnotes/endnotes, comments
		for( Entry<PartName, Part> entry : wordMLPackage.getParts().getParts().entrySet() ) {
			
			Part part = entry.getValue();
			
			if (part instanceof MainDocumentPart
					|| part instanceof HeaderPart 
					|| part instanceof FooterPart 
					|| part instanceof EndnotesPart 
					|| part instanceof FootnotesPart 
					|| part instanceof CommentsPart) {
				
				// traverse this
				SingleTraversalUtilVisitorCallback runVisitor 
				= new SingleTraversalUtilVisitorCallback(
						new TraversalUtilParagraphVisitor());
				
				if (part instanceof ContentAccessor  ) {
					
					ContentAccessor contents = (ContentAccessor)part;
					runVisitor.walkJAXBElements(contents);
					
				} else {
					System.err.println("TODO: handle " + part.getClass().getName() );
				}
				
			}
					
		}
		
		// Remove settings from numbering part
		if (wordMLPackage.getMainDocumentPart().getNumberingDefinitionsPart()!=null) {
			Numbering numbering = wordMLPackage.getMainDocumentPart().getNumberingDefinitionsPart().getContents();
			for(AbstractNum abstractNum : numbering.getAbstractNum() ) {
				handleAbstractNum(abstractNum);
			}
			for(Num num : numbering.getNum() ) {
				handleNum(num);
			}
		}
		
		// Remove settings from styles part
		StyleDefinitionsPart sdp = wordMLPackage.getMainDocumentPart().getStyleDefinitionsPart(true);
		for (Style s : sdp.getContents().getStyle()) {
			
			if (s.getRPr()!=null) {
				RPr rPr = s.getRPr(); 
				rPr.setRFonts(null);
				rPr.setSz(null);
				rPr.setSzCs(null);
			}
		}
		
		/* Finally, set new default: arial 11
		 * 
		    <w:docDefaults>
		        <w:rPrDefault>
		            <w:rPr>
		                <w:rFonts w:ascii="Arial" w:hAnsi="Arial" w:cs="Arial"/>
		                <w:sz w:val="22"/>
		                <w:szCs w:val="22"/>
                	 */
		if (sdp.getContents().getDocDefaults()==null) {
			sdp.getContents().setDocDefaults(new DocDefaults());
		}
		DocDefaults docDefaults = sdp.getContents().getDocDefaults();
		if (docDefaults.getRPrDefault()==null) {
			docDefaults.setRPrDefault( new RPrDefault() );
		}
		RPrDefault rPrDefault = docDefaults.getRPrDefault();
		if (rPrDefault.getRPr()==null) {
			rPrDefault.setRPr( new RPr() );
		}
		RPr rPr = rPrDefault.getRPr();
        // Create object for sz
        HpsMeasure hpsmeasure = Context.getWmlObjectFactory().createHpsMeasure(); 
        rPr.setSz(hpsmeasure); 
            hpsmeasure.setVal( BigInteger.valueOf( FONT_SIZE_POINTS * 2) ); 
        // Create object for rFonts
        RFonts rfonts = Context.getWmlObjectFactory().createRFonts(); 
        rPr.setRFonts(rfonts); 
            rfonts.setAscii( FONT_NAME ); 
            rfonts.setCs( FONT_NAME ); 
            rfonts.setHAnsi( FONT_NAME ); 
        // Create object for szCs
        HpsMeasure hpsmeasure2 = Context.getWmlObjectFactory().createHpsMeasure(); 
        rPr.setSzCs(hpsmeasure2); 
            hpsmeasure2.setVal( BigInteger.valueOf( 22) );		
		
		// Save it
		String outputfilepath = System.getProperty("user.dir") + "/OUT_FontSettingsRemoval.docx";
		Docx4J.save(wordMLPackage, new File(outputfilepath), Docx4J.FLAG_NONE); //(FLAG_NONE == default == zipped docx)
		
		System.out.println("Saved: " + outputfilepath);
	}
	
	public static class TraversalUtilParagraphVisitor extends TraversalUtilVisitor<P> {
				
		@Override
		public void apply(P p, Object parent, List<Object> siblings) {
			
			/* First remove from pPr
		      <w:pPr>
		        <w:rPr>
		          <w:rFonts w:cstheme="minorHAnsi"/>
		          <w:sz w:val="24"/>
		          <w:szCs w:val="24"/>
		        </w:rPr>
		      </w:pPr>
	       */
			
			if (p.getPPr()!=null) {
				
				handleRPr( p.getPPr().getRPr());
			}

			// Next, remove from any run
			for(Object o : p.getContent()) {
				if (o instanceof R) {
					handleRPr( ((R)o).getRPr());					
				}
			}
		}
		
		private void handleRPr(RPrAbstract rPr) {
			if (rPr==null) return;
			
			rPr.setRFonts(null);
			rPr.setSz(null);
			rPr.setSzCs(null);
		}
	
	}	
	
	private static void handleAbstractNum(AbstractNum abstractNum) {
		
		for (Lvl lvl : abstractNum.getLvl()) {
			lvl.setRPr(null);
		}
		
	}
	private static void handleNum(Num num) {
		
		List<LvlOverride> lvlOverrides = num.getLvlOverride();
		if (lvlOverrides==null) return;
		for (LvlOverride override : lvlOverrides ) {
			if (override.getLvl()!=null) {
				override.getLvl().setRPr(null);
			}
		}
	}


}
