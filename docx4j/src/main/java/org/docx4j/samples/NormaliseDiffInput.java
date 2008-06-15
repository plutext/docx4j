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

package org.docx4j.samples;


import java.util.List;
import java.util.ArrayList;


import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import org.docx4j.wml.P;

import org.eclipse.compare.internal.LCSSettings;
import org.eclipse.compare.rangedifferencer.IRangeComparator;
import org.eclipse.compare.rangedifferencer.RangeDifference;
import org.docx4j.diff.StringComparator;

import org.eclipse.compare.rangedifferencer.RangeDifferencer;

public class NormaliseDiffInput {

	public final static String RUN_DELIMITER = "|";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		String para1 = "/home/dev/workspace/docx4j/sample-docs/diff/t2L";		
		String para2 = "/home/dev/workspace/docx4j/sample-docs/diff/t2R";
		
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
		
		
		// Test setup
		
		// .. Get paragraphs
		P p1 = loadParagraph(para1);
		P p2 = loadParagraph(para2);
		
		// Get their string content
		String p1text = collateRuns(p1);
		System.out.println(p1text);									

		String p2text = collateRuns(p2);
		System.out.println(p2text);									
		
		// Compute LCS
		StringComparator left = new StringComparator(p1text);
		StringComparator right = new StringComparator(p2text);
		org.eclipse.compare.internal.LCSSettings settings = new org.eclipse.compare.internal.LCSSettings();
		
		RangeDifference[] rd = RangeDifferencer.findRanges(settings, left, right); 
		
		// Output
        for (int x=0; x<rd.length; x++) {    
        	System.out.println (
        			toRangeString( left, rd[x].leftStart(), rd[x].leftLength() )
        			+ rd[x].kindString() 
        			+ toRangeString( right, rd[x].rightStart(), rd[x].rightLength() ) );
        }
        
        // Now build appropriate replacement paragraph content
        List<Object> p1Replacement = new ArrayList<Object>();
        List<Object> p2Replacement = new ArrayList<Object>();
        int p1pos = 0;
        int p2pos = 0;
        for (int x=0; x<rd.length; x++) {
			System.out.println ( "x");
        	
        	if (rd[x].kind() == RangeDifference.NOCHANGE) {
    			System.out.println ( " .. p1 " + p1pos);
    				// TODO - calculate required substring to get!!
        		p1Replacement.add( p1.getParagraphContent().get(p1pos) );
        		p2Replacement.add( p2.getParagraphContent().get(p2pos) );
        		p2pos++;
        	} else if (rd[x].kind() == RangeDifference.CHANGE) {
        		
        		if ( "".equals( toRangeString( left, rd[x].leftStart(), rd[x].leftLength() ) )
        				&& RUN_DELIMITER.equals( toRangeString( right, rd[x].rightStart(), rd[x].rightLength() ) ) ) {
        			// We're now on to the right paragraph's next w:t
            		p2pos++;
        			
            		// .. and we need to insert an empty w:r/w:t structure in the left
            		
            			// TODO
            		
        		} else if ( RUN_DELIMITER.equals( toRangeString( left, rd[x].leftStart(), rd[x].leftLength() ) )
            				&& "".equals( toRangeString( right, rd[x].rightStart(), rd[x].rightLength() ) ) ) {
        			// We're now on to the left paragraph's next w:t
            		p1pos++;
        			
            		// .. and we need to insert an empty w:r/w:t structure in the right
            		
            			// TODO
            		
        		}
        	}

        }
		
		System.out.println("Done!" );									
		
	}
	
	private static String toRangeString(StringComparator sc, int start, int length) {
		
    	StringBuilder result = new StringBuilder();
        for (int x=start; x<(start+length); x++) {    
        	result.append(sc.getLeaf(x));
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
	

}
