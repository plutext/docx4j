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
import java.util.Map;


import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.docx4j.XmlUtils;
import org.docx4j.wml.P;
import org.docx4j.wml.R;

import org.eclipse.compare.rangedifferencer.RangeDifference;
import org.docx4j.diff.StringComparator;

import org.eclipse.compare.rangedifferencer.RangeDifferencer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.topologi.diffx.Main;
import com.topologi.diffx.config.DiffXConfig;

public class ParagraphDifferencer {
	
	/*
	 * TODO:
	 * 
	 * - handle spaces properly (encode real spaces as something before splitting,
	 *   and add back in at end
	 *   
	 * - write xml:space="preserve" to generate w:t as necessary
	 * 
	 */

	protected static Logger log = Logger.getLogger(ParagraphDifferencer.class);
	
	
	public static String author = "unknown";
	
	public static Integer nextId = 0;

	static org.docx4j.wml.ObjectFactory wmlFactory = new org.docx4j.wml.ObjectFactory();
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		// Test setup
		String paraL = ParagraphDifferencerTest.BASE_DIR + "t2R";		
		String paraR = ParagraphDifferencerTest.BASE_DIR + "t4";
		P pl = loadParagraph(paraL);
		P pr = loadParagraph(paraR);
		
		// Result format
		StreamResult result = new StreamResult(System.out);

		// Run the diff
		diff(pl, pr, result);
		
	}
	
	public final static Integer getId() {
		
		return ++nextId;
		
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
		 * 
		 * In terms of actual performance (versus plain old diffx), the
		 * main case where the pre-processing helps:
		 * 
		 * 1. t2R cf t3L
		 * 
			  Left input 
					
					<w:p>
					    <w:r>
					        <w:t xml:space="preserve">The quick brown </w:t>
					    </w:r>
					    <w:r>
					        <w:rPr>
					            <w:b/>
					            <w:sz w:val="28"/>
					            <w:szCs w:val="28"/>
					        </w:rPr>
					        <w:t>fox</w:t>
					    </w:r>
					    <w:r>
					        <w:t xml:space="preserve"> jumped over the </w:t>
					    </w:r>
					    <w:r>
					        <w:rPr>
					            <w:u w:val="single"/>
					        </w:rPr>
					        <w:t>lazy</w:t>
					    </w:r>
					    <w:r>
					        <w:t xml:space="preserve"> dog.</w:t>
					    </w:r>
					</w:p> 
					
					
			  Right input 
					
					<w:p>
					    <w:r>
					        <w:t>The quick brown fox jumped high </w:t>
					    </w:r>
					    <w:r>
					        <w:t>high over the lazy dog.</w:t>
					    </w:r>
					</w:p>		 
					
					    
		 * 
		 */

		// debug only
        String leftXmlOld = null;
        String rightXmlOld = null;
        if (log.isInfoEnabled() ) {
	        leftXmlOld = org.docx4j.XmlUtils.marshaltoString(pl, true, true);
	        rightXmlOld = org.docx4j.XmlUtils.marshaltoString(pr, true, true);
        }
        
		// Compute LCS
		StringComparator left = new StringComparator(pl.toString());
		StringComparator right = new StringComparator(pr.toString());
		org.eclipse.compare.internal.LCSSettings settings = new org.eclipse.compare.internal.LCSSettings();
		
		RangeDifference[] rd = RangeDifferencer.findRanges(settings, left, right); 
		
		// Debug Output
		if (log.isDebugEnabled()) {
			log.debug("\n\n RangeDifferences \n\n");									
	        for (int x=0; x<rd.length; x++) {    
	        	log.debug (
	        			toRangeString( left, rd[x].leftStart(), rd[x].leftLength(), true )
	        			+ rd[x].kindString() 
	        			+ toRangeString( right, rd[x].rightStart(), rd[x].rightLength(), true ) );
	        }
		}
        
        // Now build appropriate replacement paragraph content
        List<Object> pLeftReplacement = new ArrayList<Object>();
        List<Object> pRightReplacement = new ArrayList<Object>();
        
        // Which of the _existing_ w:r we are up to
        int pLeftIndex = 0; 
        int pRightIndex = 0;    	
		
		int[] leftCounts = getParagraphRunTextWordCounts(pl); 
		
//    	StringBuilder debug = new StringBuilder();
//    	debug.append("{ ");
//		for (int i=0; i < leftCounts.length; i++) {
//	    	try {
//				debug.append( leftCounts[i] + ", ");
//			} catch (RuntimeException e) {
//			}
//		}
//    	System.out.println(debug);
		
		int[] rightCounts = getParagraphRunTextWordCounts(pr); 
		
		int leftWordCounter = -1;
		int rightWordCounter = -1;
        
        for (int x=0; x<rd.length; x++) {
        
        	// The original runs are always longer than 
        	// each rd

    		// We will definitely require a new run 
    		// structure for each side
    		R currentLeftStructure = createRunStructure("",
    				pl, pLeftIndex );
        	R currentRightStructure = createRunStructure("",
    				pr, pRightIndex );
        	
        	pLeftReplacement.add(currentLeftStructure);
        	pRightReplacement.add(currentRightStructure);
        	
        	if (rd[x].kind() == RangeDifference.NOCHANGE) {
        		log.debug("NOCHANGE");
        		// These are part of the string LCS,
        		// (though they might not be part of the
        		//  XML LCS once we've added their rPr 
        		//  back in.)  
        		// This is where we focus our efforts.
        		
            	        		
        		// Process the words in rd[x] one word at a time
                for (int i=rd[x].leftStart(); // left and right are identical
                		 i<(rd[x].leftStart()+rd[x].leftLength()); i++) {

            		// Our objective is to ensure that both the
            		// left and right paragraphs end up with 
            		// matching w:r/w:t boundaries.
            		
            		// So when either of the existing paragraphs
            		// contains a boundary, this need to be inserted
            		// in both results
                	
                	String word = left.getLeaf(i);
                	
                	leftWordCounter++;
                	rightWordCounter++;
                	
//            		log.debug(word);
                	
                	if ( leftWordCounter < sum(leftCounts, 0, pLeftIndex)
                			&& rightWordCounter < sum(rightCounts, 0, pRightIndex) ) {
                		
                		// it is ok to insert into current w:t
                		addWord(currentLeftStructure, word);
                		addWord(currentRightStructure, word);                		
                		
                	} else {  
                		
//                		log.debug("Hit boundary");
                		
                		// which boundary have we hit?
                		if (leftWordCounter == sum(leftCounts, 0, pLeftIndex)
                				&& rightWordCounter	== sum(rightCounts, 0, pRightIndex) ) {
                			// Quite likely, for example, same formatting in each
                			
                			// We're now on to each paragraph's next w:t
                    		pLeftIndex++;
                    		pRightIndex++;
                			
                		} else if (leftWordCounter == sum(leftCounts, 0, pLeftIndex) ) {
                			
                			// We're now on to the left paragraph's next w:t
                    		pLeftIndex++;
                			
                		} else {
                		
                			// We're now on to the right paragraph's next w:t
                    		pRightIndex++;
                		}
                		
                		currentLeftStructure = createRunStructure(word,
                				pl, pLeftIndex );
                    	currentRightStructure = createRunStructure(word,
                				pr, pRightIndex );
                    	
                    	pLeftReplacement.add(currentLeftStructure);
                    	pRightReplacement.add(currentRightStructure);
                		
                	}
                	
                }        		        		
                
        	} else if (rd[x].kind() == RangeDifference.CHANGE) {
        		log.debug("CHANGE");
        		// These aren't part of the string LCS,
        		// (so they shouldn't be part of 
        		//  the XML LCS)
        		
        		// All we need to do is make sure that 
        		// the input is round tripped.
        		            	        		
        		// Left side: Process the words in rd[x] one word at a time
            	// NB, can't just copy existing runs into the output            	
        		log.debug(".. left side");
                for (int i=rd[x].leftStart(); 
                		 i<(rd[x].leftStart()+rd[x].leftLength()); i++) {
                	                	
                	String word = left.getLeaf(i);
//            		log.debug(word);
                	leftWordCounter++;
                	
                	if ( leftWordCounter < sum(leftCounts, 0, pLeftIndex) ) {
            			// it is ok to insert into left's current w:t
                		addWord(currentLeftStructure, word);
                	} else {                		
                		// boundary hit                			
            			// We're now on to the left paragraph's next w:t
                		pLeftIndex++;
                		currentLeftStructure = createRunStructure(word,
                				pl, pLeftIndex );
                    	pLeftReplacement.add(currentLeftStructure);
                	}
                	
                }        		        		
        		
        		// Right side
        		log.debug(".. right side");
                for (int i=rd[x].rightStart(); 
                		 i<(rd[x].rightStart()+rd[x].rightLength()); i++) {
                	
                	String word = right.getLeaf(i);
            		log.debug(word);
                	rightWordCounter++;
                	
                	if ( rightWordCounter < sum(rightCounts, 0, pRightIndex) ) {
                		// it is ok to insert into right's current w:t
                		addWord(currentRightStructure, word);                		
                	} else {                		
                		// boundary hit                			
            			// We're now on to the right paragraph's next w:t
                		pRightIndex++;
                    	currentRightStructure = createRunStructure(word,
                				pr, pRightIndex );
                    	pRightReplacement.add(currentRightStructure);
                	}                	
                }        		        		
        		
        	}
        	
        }
		
        
        org.docx4j.wml.P newLeftP = wmlFactory.createP();
        newLeftP.setPPr(pl.getPPr());
        newLeftP.getParagraphContent().addAll(pLeftReplacement);

        org.docx4j.wml.P newRightP = wmlFactory.createP();
        newRightP.setPPr(pr.getPPr());
        newRightP.getParagraphContent().addAll(pRightReplacement);
        
		log.debug("\n\n Left input \n\n" );
        log.debug(leftXmlOld) ;
        
        log.debug("\n\n New left side \n\n" );
        String leftXmlNew = org.docx4j.XmlUtils.marshaltoString(newLeftP, true, true);
        log.debug(leftXmlNew) ;

		log.debug("\n\n Right input \n\n" );
        log.debug(rightXmlOld) ;        
        
        log.debug("\n\n New right side \n\n" );
        String rightXmlNew = org.docx4j.XmlUtils.marshaltoString(newRightP, true, true);
        log.debug(rightXmlNew) ;
        
        log.debug("\n\n Difference \n\n" );
        
        String diffx = getDiffxOutput(leftXmlNew, rightXmlNew);
        //String diffx = getDiffxOutput(rightXmlNew, leftXmlNew);
        log.debug(diffx) ;

        // Debug purposes only!
        log.debug("\n\n Compare naive difference \n\n" );
        
        String naive = getDiffxOutput(leftXmlOld, rightXmlOld);
        log.debug(naive) ;
        
        
        log.info("\n\n <p> difference with pre-processing</p> \n\n" );
		try {
			StreamSource src = new StreamSource(new StringReader(diffx));
			java.io.InputStream xslt = 
				org.docx4j.utils.ResourceUtils.getResource("org/docx4j/diff/diffx2html.xslt");
				//org.docx4j.utils.ResourceUtils.getResource("org/docx4j/diff/diffx2wml.xslt");
			Map<String, Object> transformParameters = new java.util.HashMap<String, Object>();
			transformParameters.put("author", author);
			XmlUtils.transform(src, xslt, transformParameters, result);
			
		} catch (Exception exc) {
			exc.printStackTrace();
		}

        log.info("\n\n <p> difference without preprocessing </p> \n\n" );
		try {
			StreamSource src = new StreamSource(new StringReader(naive));
			java.io.InputStream xslt = 
				org.docx4j.utils.ResourceUtils.getResource("org/docx4j/diff/diffx2html.xslt");
			//org.docx4j.utils.ResourceUtils.getResource("org/docx4j/diff/diffx2wml.xslt");
			Map<String, Object> transformParameters = new java.util.HashMap<String, Object>();
			transformParameters.put("author", author);
			XmlUtils.transform(src, xslt, transformParameters, result);
			
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		
		log.debug("\n\n Done!" );									
		
	}
	
	private static int sum( int[] array, int idx1, int idx2) {

    	StringBuilder debug = new StringBuilder();
    	
    	debug.append("{ ");

		int sum = 0;
		
		for (int i=idx1; i <= idx2; i++) {
	    	debug.append( array[i] + ", ");
			
			sum+=array[i];			
		}
    	debug.append("} = " + sum);
//    	System.out.println(debug);
		return sum;
		
	}
	
	/** Add a word to a w:r's existing w:t */ 
	private static void addWord(R r, String word) {
		
    	List runContent = r.getRunContent();
		    	
		for (Object o2 : runContent ) {	
			
			/* TODO - model assumes each w:r contains
			   only 1 w:t
			   
			   Check spec to see what the story is.
			   
			 */
			
			boolean found = false;
			
			if (o2 instanceof org.docx4j.wml.Text) {
					
					if (found) {
						log.debug("TODO: Handle multiple w:t in w:r!");
					}
					
					found = true;
					
					org.docx4j.wml.Text t = (org.docx4j.wml.Text)o2;
					
					String existingVal = t.getValue();
					
					t.setValue(existingVal + " " + word); // TODO smarter handling of spaces
					
			} else {
				log.debug(o2.getClass().getName());
			}
		}
		
		
		
	}

	
	private static org.docx4j.wml.R createRunStructure(String textVal,
			P existingP, int rIndex ) {

		org.docx4j.wml.R newR = wmlFactory.createR();
		org.docx4j.wml.Text newT = wmlFactory.createText();
		newR.getRunContent().add(newT);
		newT.setValue(textVal);
		org.docx4j.wml.RPr existingRPr = ((org.docx4j.wml.R)existingP.getParagraphContent().get(rIndex)).getRPr(); 
		if ( existingRPr !=null )
			newR.setRPr(existingRPr);
		return newR;
		
	}
	
	private static String toRangeString(StringComparator sc, int start, int length, boolean space) {
		
		// This method only exists for debug...
		
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
	
	protected static org.docx4j.wml.P loadParagraph(String filename) throws Exception {
		
		java.io.File f = new java.io.File(filename);
		java.io.InputStream is = new java.io.FileInputStream(f);
		JAXBContext jc = org.docx4j.jaxb.Context.jc;				
	    
		Unmarshaller u = jc.createUnmarshaller();
		
		//u.setSchema(org.docx4j.jaxb.WmlSchema.schema);
		u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());
					
		return (org.docx4j.wml.P)u.unmarshal( is );
		
		
		
	}

    public static int[] getParagraphRunTextWordCounts(P p) {
    	
    	List<Object> children = p.getParagraphContent();
    	
    	int i=0;
    	int[] result = new int[children.size()]; // one for each w:r
    	
    	for (Object o : children) {
    	
	    	if ( o instanceof org.docx4j.wml.R ) {
	    		
	    		org.docx4j.wml.R r = (org.docx4j.wml.R)o;
		    	List runContent = r.getRunContent();
	    		
		    	result[i]=0;
		    	
				for (Object o2 : runContent ) {	
					
					/* TODO - model assumes each w:r contains
					   only 1 w:t
					   
					   Check spec to see what the story is.
					   
					 */
					
					boolean found = false;
					
					if (o2 instanceof javax.xml.bind.JAXBElement) {
	
						if (((JAXBElement) o2).getDeclaredType().getName().equals(
								"org.docx4j.wml.Text")) {
							
							if (found) {
								log.debug("TODO: Handle multiple w:t in w:r!");
							}
							
							found = true;
							
							// System.out.println("Found Text");
							org.docx4j.wml.Text t = (org.docx4j.wml.Text) ((JAXBElement) o2)
									.getValue();
							
							result[i] = getWordCount( t.getValue() ); 
							
						} else {
							log.debug(((JAXBElement) o2).getDeclaredType().getName());
						}
					} else {
						log.debug(o2.getClass().getName());
					}
				}
				
				i++;
	    		
	    	} else {
		    	log.debug("Encountered " + children.get(i).getClass().getName());	    	
		    	return null;
	    		
	    	}
    	}    	
    	
    	return result;
    	
    }

    
    private static int getWordCount(String sentence) {
    	
		/* 
		 * Need to convert leading and trailing spaces
		 * in order to get correct count.
		 * 
		 * 	'a'     1
			' a'    2
			'a '    1
			' b '   2
			' b c ' 3
			'b c'   2
			'b  c'  3  <-- and also double spaces here
			
		 * 
		 * trim takes care of leading and trailing.
		 */
    	
    	return sentence.trim().split("\\s").length;
    	
    		// TODO - handle cases of 2 spaces in a row, within the sentence
    		// via an improved regex
    	
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
						// log.debug("Found Text");
						org.docx4j.wml.Text t = (org.docx4j.wml.Text) ((JAXBElement) o2)
								.getValue();
						result.append(t.getValue());
					} else {
						log.debug(((JAXBElement) o2).getDeclaredType().getName());
					}
				} else {
					log.debug(o2.getClass().getName());
				}
			}    		
    		
    	} else {
	    	log.debug("Encountered " + children.get(i).getClass().getName());	    	
	    	return null;
    		
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

	public static String getAuthor() {
		return author;
	}

	public static void setAuthor(String author) {
		ParagraphDifferencer.author = author;
	}

	/*String[] runtContents = "a".trim().split("\\s");
	System.out.println( "'a' " + runtContents.length );
	
	runtContents = " a".trim().split("\\s");
	System.out.println( "' a' " + runtContents.length );

	runtContents = "a ".trim().split("\\s");
	System.out.println( "'a ' " + runtContents.length );
	
	runtContents = " b ".trim().split("\\s");
	System.out.println( "' b ' " + runtContents.length );

	runtContents = " b c ".trim().split("\\s");
	System.out.println( "' b c ' " + runtContents.length );

	runtContents = "b c".trim().split("\\s");
	System.out.println( "'b c' " + runtContents.length );
	
	runtContents = "b  c".trim().split("\\s");
	System.out.println( "'b  c' " + runtContents.length );*/

}
