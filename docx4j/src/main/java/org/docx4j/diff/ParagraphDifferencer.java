/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
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

package org.docx4j.diff;


import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.ArrayList;


import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.docx4j.XmlUtils;
import org.docx4j.wml.P;

import org.eclipse.compare.internal.LCSSettings;
import org.eclipse.compare.rangedifferencer.IRangeComparator;
import org.eclipse.compare.rangedifferencer.RangeDifference;
import org.docx4j.diff.StringComparator;

import org.eclipse.compare.rangedifferencer.RangeDifferencer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.topologi.diffx.Main;
import com.topologi.diffx.config.DiffXConfig;

public class ParagraphDifferencer {

	public final static String RUN_DELIMITER = "|";

	static org.docx4j.wml.ObjectFactory wmlFactory = new org.docx4j.wml.ObjectFactory();
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		// Test setup
		String paraL = "/home/dev/workspace/docx4j/sample-docs/diff/t2L";		
		String paraR = "/home/dev/workspace/docx4j/sample-docs/diff/t2R";
		P pl = loadParagraph(paraL);
		P pr = loadParagraph(paraR);
		
		// Result format
		StreamResult result = new StreamResult(System.out);

		// Run the diff
		diff(pl, pr, result);
		
	}
	
	/**
	 * Compare 2 p objects, returning a result containing
	 * w:ins and w:del elements  
	 * 
	 * @param pl - the left paragraph
	 * @param pr - the right paragraph
	 * @param result 
	 */
	public static void diff(P pl, P pr, javax.xml.transform.Result result) {
		
		/* In order to get an optimal result when comparing 2 WML paragraphs,
		 * it helps if each can be made to contain matching runs.
		 * 
		 * TODO: ensure each w:r contains one and only one w:t 
		 * 
		 * The process for achieving this involves running the LCS algorithm
		 * on the string content of the paragraph.
		 * 
		 * At this point, you'd actually be done, if you didn't care about
		 * run formatting.  
		 * 
		 * But we do care about run formatting, so the relevant formatting 
		 * is then re-attached to each of the sets of runs.
		 * 
		 * The XML diff is then run on these 'normalised' paragraphs. 
		 * It will tell which of the w:t have been populated/deleted, and
		 * what formatting has changed on their w:r elements.   
		 */
		
		
		// Get their string content
		String pLeftText = collateRuns(pl);
		System.out.println(pLeftText);									

		String pRightText = collateRuns(pr);
		System.out.println(pRightText);									
		
		// Compute LCS
		StringComparator left = new StringComparator(pLeftText);
		StringComparator right = new StringComparator(pRightText);
		org.eclipse.compare.internal.LCSSettings settings = new org.eclipse.compare.internal.LCSSettings();
		
		RangeDifference[] rd = RangeDifferencer.findRanges(settings, left, right); 
		
		// Debug Output
        for (int x=0; x<rd.length; x++) {    
        	System.out.println (
        			toRangeString( left, rd[x].leftStart(), rd[x].leftLength(), true )
        			+ rd[x].kindString() 
        			+ toRangeString( right, rd[x].rightStart(), rd[x].rightLength(), true ) );
        }
        
        // Now build appropriate replacement paragraph content
        List<Object> pLeftReplacement = new ArrayList<Object>();
        List<Object> pRightReplacement = new ArrayList<Object>();
        
        // Which of the existing w:r we are up to
        int pLeftIndex = 0; 
        int pRightIndex = 0;    	

        // We'll need to add this in certain places
		org.docx4j.wml.R emptyStructure = wmlFactory.createR();
		org.docx4j.wml.Text newT = wmlFactory.createText();
		emptyStructure.getRunContent().add(newT);
        
        
        for (int x=0; x<rd.length; x++) {
        
        	// The original runs are always longer than 
        	// each rd
        	
        	if (rd[x].kind() == RangeDifference.NOCHANGE) {
        		
        		if ( RUN_DELIMITER.equals( toRangeString( left, rd[x].leftStart(), rd[x].leftLength(), false ) ) ) {
        			
        			// This is a boundary on both left and right objects
        			
        			// We're now on to the left paragraph's next w:t
            		pLeftIndex++;

        			// We're now on to the right paragraph's next w:t
            		pRightIndex++;
            		
        		} else {
        			
        			// Normal case        		
	
	        		org.docx4j.wml.R newLeftR = createRunStructure(left, rd[x].leftStart(), rd[x].leftLength(),
	        				pl, pLeftIndex );	        		
	        		pLeftReplacement.add( newLeftR );
	        		
	        		
	        		org.docx4j.wml.R newRightR = createRunStructure(right, rd[x].rightStart(), rd[x].rightLength(),
	        				pr, pRightIndex );	        		
	        		pRightReplacement.add( newRightR );
        		
        		}
        	} else if (rd[x].kind() == RangeDifference.CHANGE) {
        		
        		if ( "".equals( toRangeString( left, rd[x].leftStart(), rd[x].leftLength(), false ) )
        				&& RUN_DELIMITER.equals( toRangeString( right, rd[x].rightStart(), rd[x].rightLength(), false ) ) ) {
        			// We're now on to the right paragraph's next w:t
            		pRightIndex++;
        			
            		// .. and we need to insert an empty w:r/w:t structure in the left
	        		//pLeftReplacement.add( emptyStructure );
            		
        		} else if ( RUN_DELIMITER.equals( toRangeString( left, rd[x].leftStart(), rd[x].leftLength(), false ) )
            				&& "".equals( toRangeString( right, rd[x].rightStart(), rd[x].rightLength(), false ) ) ) {
        			// We're now on to the left paragraph's next w:t
            		pLeftIndex++;
        			
            		// .. and we need to insert an empty w:r/w:t structure in the right
	        		//pRightReplacement.add( emptyStructure );
            		
        		} else {
        			// both sides have changed
        			
	        		org.docx4j.wml.R newLeftR = createRunStructure(left, rd[x].leftStart(), rd[x].leftLength(),
	        				pl, pLeftIndex );	        		
	        		pLeftReplacement.add( newLeftR );
	        		
	        		
	        		org.docx4j.wml.R newRightR = createRunStructure(right, rd[x].rightStart(), rd[x].rightLength(),
	        				pr, pRightIndex );	        		
	        		pRightReplacement.add( newRightR );
        			
        		}
        	}

        }
		
        
        org.docx4j.wml.P newLeftP = wmlFactory.createP();
        newLeftP.setPPr(pl.getPPr());
        newLeftP.getParagraphContent().addAll(pLeftReplacement);

        org.docx4j.wml.P newRightP = wmlFactory.createP();
        newRightP.setPPr(pr.getPPr());
        newRightP.getParagraphContent().addAll(pRightReplacement);
        
        System.out.println("\n\n New left side \n\n" );
        String leftXmlNew = org.docx4j.XmlUtils.marshaltoString(newLeftP, true);
        System.out.println(leftXmlNew) ;

        System.out.println("\n\n New right side \n\n" );
        String rightXmlNew = org.docx4j.XmlUtils.marshaltoString(newRightP, true);
        System.out.println(rightXmlNew) ;
        
        System.out.println("\n\n Difference \n\n" );
        
        String diffx = getDiffxOutput(leftXmlNew, rightXmlNew);
        //String diffx = getDiffxOutput(rightXmlNew, leftXmlNew);
        System.out.println(diffx) ;

        // Debug purposes only!
        System.out.println("\n\n Compare naive difference \n\n" );
        
        String leftXmlOld = org.docx4j.XmlUtils.marshaltoString(pl, true);
        String rightXmlOld = org.docx4j.XmlUtils.marshaltoString(pr, true);
        String naive = getDiffxOutput(leftXmlOld, rightXmlOld);
        System.out.println(naive) ;
        
        
        System.out.println("\n\n WordML \n\n" );
		try {
			StreamSource src = new StreamSource(new StringReader(diffx));
			java.io.InputStream xslt = 
				org.docx4j.utils.ResourceUtils.getResource("org/docx4j/diff/diffx2wml.xslt");
			XmlUtils.transform(src, xslt, null, result);
			
		} catch (Exception exc) {
			exc.printStackTrace();
		}
                
		System.out.println("\n\n Done!" );									
		
	}
	
	
	private static org.docx4j.wml.R createRunStructure(StringComparator sc, int rdStart, int rdLength,
			P existingP, int rIndex ) {

		org.docx4j.wml.R newR = wmlFactory.createR();
		org.docx4j.wml.Text newT = wmlFactory.createText();
		newR.getRunContent().add(newT);
		newT.setValue(toRangeString( sc, rdStart, rdLength, true ));
		org.docx4j.wml.RPr existingRPr = ((org.docx4j.wml.R)existingP.getParagraphContent().get(rIndex)).getRPr(); 
		if ( existingRPr !=null )
			newR.setRPr(existingRPr);
		return newR;
		
	}
	
	private static String toRangeString(StringComparator sc, int start, int length, boolean space) {
		
    	StringBuilder result = new StringBuilder();
        for (int x=start; x<(start+length); x++) {  
        	if (space) {
        		result.append(sc.getLeaf(x) + " ");
        	} else {
        		result.append(sc.getLeaf(x));
        	}
        }
		return result.toString();
	}
	
	private static org.docx4j.wml.P loadParagraph(String filename) throws Exception {
		
		java.io.File f = new java.io.File(filename);
		java.io.InputStream is = new java.io.FileInputStream(f);
		JAXBContext jc = org.docx4j.jaxb.Context.jc;				
	    
		Unmarshaller u = jc.createUnmarshaller();
		
		//u.setSchema(org.docx4j.jaxb.WmlSchema.schema);
		u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());
					
		return (org.docx4j.wml.P)u.unmarshal( is );
		
		
		
	}

    public static String getRunString(org.docx4j.wml.P p, int i) {

    	StringBuilder result = new StringBuilder();
    	
    	List<Object> children = p.getParagraphContent();
    	
    	if ( children.get(i) instanceof org.docx4j.wml.R ) {
    		
    		org.docx4j.wml.R r = (org.docx4j.wml.R)children.get(i);
	    	List runContent = r.getRunContent();
    		
			for (Object o2 : runContent ) {					
				if (o2 instanceof javax.xml.bind.JAXBElement) {

					if (((JAXBElement) o2).getDeclaredType().getName().equals(
							"org.docx4j.wml.Text")) {
						// System.out.println("Found Text");
						org.docx4j.wml.Text t = (org.docx4j.wml.Text) ((JAXBElement) o2)
								.getValue();
						result.append(t.getValue());
					} else {
						System.out.println(((JAXBElement) o2).getDeclaredType().getName());
					}
				} else {
					System.out.println(o2.getClass().getName());
				}
			}    		
    		
    	} else {
	    	System.out.println("Encountered " + children.get(i).getClass().getName());	    	
	    	return null;
    		
    	}
    	
		return result.toString();
    	
    }
	
	
    public static String collateRuns(org.docx4j.wml.P p) {

    	StringBuilder result = new StringBuilder();
    	boolean resultIsEmpty = true;
    	
    	List<Object> children = p.getParagraphContent();
    	
//    	System.out.println("p.toString");
    	    	
		for (Object o : children ) {					
//			System.out.println("  " + o.getClass().getName() );
			if ( o instanceof org.docx4j.wml.R) {
//		    	System.out.println("Hit R");
				org.docx4j.wml.R  run = (org.docx4j.wml.R)o;
		    	List runContent = run.getRunContent();
				for (Object o2 : runContent ) {					
					if ( o2 instanceof javax.xml.bind.JAXBElement) {
						// TODO - unmarshall directly to Text.
						if ( ((JAXBElement)o2).getDeclaredType().getName().equals("org.docx4j.wml.Text") ) {
//					    	System.out.println("Found Text");
							org.docx4j.wml.Text t = (org.docx4j.wml.Text)((JAXBElement)o2).getValue();
							
							if (resultIsEmpty) {
								result.append( t.getValue() );	
								resultIsEmpty = false;
							} else {
								result.append( " " + RUN_DELIMITER + " " + t.getValue() );					
							}
							
						}
					} else {
//				    	System.out.println(o2.getClass().getName());						
					}
				}
			} 
		}
		return result.toString();
    	
    }
	
	private static String getDiffxOutput(String xml1, String xml2) {
		Reader xmlr1 = new StringReader(xml1);
		Reader xmlr2 = new StringReader(xml2);
		
		// output
		Writer out = new StringWriter();
		
		DiffXConfig diffxConfig = new DiffXConfig();
		diffxConfig.setIgnoreWhiteSpace(true);
		diffxConfig.setPreserveWhiteSpace(false);

		try {
			Main.diff(toNode(xmlr1, true), toNode(xmlr2, true), out, diffxConfig);
				// The signature which takes Reader objects appears to be broken
			out.close();
		} catch (Exception exc) {
			exc.printStackTrace();
			out = null;
		}
		
		return (out == null) ? null : out.toString();
	}

	/**
	 * Converts the reader to a node.
	 * 
	 * @param xml
	 *            The reader on the XML.
	 * @param isNSAware
	 *            Whether the factory should be namespace aware.
	 * 
	 * @return The corresponding node.
	 */
	private static Node toNode(Reader xml, boolean isNSAware) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(isNSAware);
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(xml));
			return document;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

}
