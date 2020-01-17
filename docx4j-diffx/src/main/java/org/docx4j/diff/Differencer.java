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


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.eclipse.compare.StringComparator;
import org.eclipse.compare.rangedifferencer.RangeDifference;
import org.eclipse.compare.rangedifferencer.RangeDifferencer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.topologi.diffx.Docx4jDriver;
import com.topologi.diffx.Main;
import com.topologi.diffx.config.DiffXConfig;



/**
 * Capable of comparing a pair of:
 * - w:body (only lightly tested)
 * - w:sdtContent (used extensively)
 * - w:p (includes an algorithm aimed at producing a better diff)
 * 
 * See org.docx4j.samples.CompareDocuments for an example of how to use.
 * 
 * @author jason
 *
 */
public class Differencer {
	
	/*
	 * TODO:
	 * 
	 * - handle spaces properly (encode real spaces as something before splitting,
	 *   and add back in at end
	 *    
	 */

	protected static Logger log = LoggerFactory.getLogger(Differencer.class);


	// For XSLT
	public static void log(String message ) {		
		log.info(message);
	}
	
	

	static org.docx4j.wml.ObjectFactory wmlFactory = new org.docx4j.wml.ObjectFactory();
	
	// The rels used in the resulting diff
	private Map<Relationship, Part> composedRels = new HashMap<Relationship,Part>();
	public Map<Relationship, Part> getComposedRels() {
		return composedRels;
	}
	
	
	
    final private static SimpleDateFormat RFC3339_FORMAT 
    	= new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    
		// SimpleDateFormat is not thread-safe see:
		//   http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6231579
		//   http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6178997
		// solution is to use stateless MessageFormat instead:
		// final private static String RFC3339_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
		// final private static String RFC3339_PATTERN = "{0,date," + RFC3339_FORMAT + "}";    	

    static Templates xsltDiffx2Wml;
    
	/**
	 * org/docx4j/diff/diffx2wml.xslt will be used by default
	 * to transform the diff output into a Word docx with tracked
	 * changes. This method allows you to use your own xslt 
	 * instead.
	 * @param xsltDiffx2Wml
	 */
	public static void setXsltDiffx2Wml(Templates xsltDiffx2Wml) {
		Differencer.xsltDiffx2Wml = xsltDiffx2Wml;
	}
    
    
    
    static Templates xsltMarkupInsert;
    static Templates xsltMarkupDelete;
    
    static {
		try {
			Source xsltSource = new StreamSource(org.docx4j.utils.ResourceUtils
					.getResource("org/docx4j/diff/diffx2wml.xslt"));
			xsltDiffx2Wml = XmlUtils.getTransformerTemplate(xsltSource);

			xsltSource = new StreamSource(org.docx4j.utils.ResourceUtils
					.getResource("org/docx4j/diff/MarkupInsert.xslt"));
			xsltMarkupInsert = XmlUtils.getTransformerTemplate(xsltSource);

			xsltSource = new StreamSource(org.docx4j.utils.ResourceUtils
					.getResource("org/docx4j/diff/MarkupDelete.xslt"));
			xsltMarkupDelete = XmlUtils.getTransformerTemplate(xsltSource);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		}
    	
    }
    
//	/**
//	 * @param args
//	 */
//	public static void main(String[] args) throws Exception {
//		
//		String BASE_DIR = "/home/dev/workspace/docx4j/src/test/java/org/docx4j/diff/";
//		
//		// Test setup
//		String paraL = BASE_DIR + "t2R";		
//		String paraR = BASE_DIR + "t3L";
//		P pl = loadParagraph(paraL);
//		P pr = loadParagraph(paraR);
//		
//		// Result format
//		StreamResult result = new StreamResult(System.out);
//
//		// Run the diff - FIXME
//		Differencer pd = new Differencer();
//		pd.diff(pl, pr, result, null, null, null, null);
//		
//	}
	
	/**
	 * The id to be allocated to the ins/del
	 * @return
	 */
	public final static Integer getId() {		
		return ++nextId;		
	}
	public static Integer nextId = 0;

	
	/**
	 * Because the resulting document might be built out of the 
	 * results of a number of diffs, we need to be sure that the id's
	 * are unique across these diffs.
	 * 
	 * This is passed into the XSLT, where it is used as part
	 * of the generated rel id.
	 * 
	 * @return the 
	 */
	private String relsDiffIdentifier;  
	/**
	 * @param relsDiffIdentifier the relsDiffIdentifier to set
	 */
	public void setRelsDiffIdentifier(String relsDiffIdentifier) {
		this.relsDiffIdentifier = relsDiffIdentifier;
	}

	/**
	 * This is a Xalan extension function, invoked from diffx2wml.xslt
	 * 
	 * Any rel which is present in the results of the comparison must point to
	 * a valid target of the correct type, or the resulting document will
	 * be broken.  
	 * 
	 * So we pass the old and new rels objects, and
	 * progressively build up a List of relationships which will need to be
	 * in the resulting document.
	 * 
	 * Because the resulting document might be built out of the 
	 * results of a number of diffs, we need to be sure that the id's
	 * are unique across these diffs.
	 * 
	 * @return the 
	 */
	public static void registerRelationship(Differencer pd, 
			RelationshipsPart docPartRels, String relId,
			String newRelId ) {

		
		if (docPartRels==null) {
			// (In this case, Xalan won't even be able to find this function)
			return;
		}
		
		if (docPartRels.getRelationships()==null) {
			log.warn("relationships object is null!");
			return;
		}
		
		
		log.debug("Looking for rel " + relId);
		Relationship r = docPartRels.getRelationshipByID(relId);
		if (r==null) {
			log.error("Couldn't find rel " + relId);
			return;
		}
		
		Part p = docPartRels.getPart(r);
		
		Relationship r2 = (Relationship)XmlUtils.deepCopy(r, Context.jcRelationships);
		
		r2.setId(newRelId);
		log.debug(".. added rel " + newRelId + " -- " + r2.getTarget() );
		
		
		
		
		pd.composedRels.put(r2, p);
	}

	/**
	 * Compare 2 p objects, returning a result containing
	 * w:ins and w:del elements  
	 * 
	 * @param pl - the left paragraph
	 * @param pr - the right paragraph
	 * @param result 
	 */
	public void diff(P pl, P pr, javax.xml.transform.Result result, 
			String author, java.util.Calendar date,
			RelationshipsPart docPartRelsLeft, RelationshipsPart docPartRelsRight) {

		diff(pl, pr, result, 
				author, date, 
				docPartRelsLeft, docPartRelsRight,
				false);
	}

	public void diff(org.docx4j.wml.SdtContentBlock cbNewer, 
			org.docx4j.wml.SdtContentBlock cbOlder, 
			javax.xml.transform.Result result,
			String author, java.util.Calendar date,
			RelationshipsPart docPartRelsNewer, RelationshipsPart docPartRelsOlder) {
		
		this.diffWorker(org.docx4j.XmlUtils.marshaltoW3CDomDocument(cbNewer).getDocumentElement(), 
				org.docx4j.XmlUtils.marshaltoW3CDomDocument(cbOlder).getDocumentElement(), 
				result, author, date, docPartRelsNewer, docPartRelsOlder);
	}

	public void diff(org.docx4j.wml.Body newer, 
			org.docx4j.wml.Body older, 
			javax.xml.transform.Result result,
			String author, java.util.Calendar date,
			RelationshipsPart docPartRelsNewer, RelationshipsPart docPartRelsOlder) {
		
		this.diffWorker(
				org.docx4j.XmlUtils.marshaltoW3CDomDocument(newer).getDocumentElement(),
				org.docx4j.XmlUtils.marshaltoW3CDomDocument(older).getDocumentElement(), 				
				result, author, date, docPartRelsNewer, docPartRelsOlder);
	}
	
	/**
	 * This is private, in order to control what objects the user
	 * can invoke diff on.  At present there are public methods for
	 * pairs of w:body, w:sdtContent, and w:p.  
	 * 
	 * TODO: consider/test w:table! 
	 */
	private void diffWorker(Node newer, 
			Node older, 
			javax.xml.transform.Result result,
			String author, java.util.Calendar date,
			RelationshipsPart docPartRelsNewer, RelationshipsPart docPartRelsOlder) {

		Writer diffxResult = new StringWriter();

		try {
			Docx4jDriver.diff(newer,
					   older,
					   diffxResult);
			toWML( diffxResult.toString(),  result, author, date,
				docPartRelsNewer,  docPartRelsOlder);
		} catch (Exception exc) {
			throw new RuntimeException("diffWorker failed.", exc);
		} finally {
			IOUtils.closeQuietly(diffxResult);
		}
	}
	
	public  void toWML(String in, javax.xml.transform.Result result, String author, java.util.Calendar date,
			RelationshipsPart docPartRelsNewer, RelationshipsPart docPartRelsOlder) {
		
		if (log.isDebugEnabled()) {
			log.debug("in: " + in);
		}
		
		try {
			
			XMLInputFactory inputFactory = XMLInputFactory.newInstance();
			inputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
			inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false); // a DTD is merely ignored, its presence doesn't cause an exception
			
			/* 2014 09 09
			 * 
			 * For unknown reasons, diffx may write:
			 * 
			 *    <a14:useLocalDpi dfx:insert="true" val="0" ins:val="true"/>
			 *    
			 * ie without the namespace being declared.
			 * 
			 * Maybe something to do with the namespace being declared deep in the input?
			 * 
			 *    <a14:useLocalDpi xmlns:a14="http://schemas.microsoft.com/office/drawing/2010/main" val="0"/>
			 *    
			 * This is a crude workaround.   
			 */
			int nsIndex = in.indexOf("xmlns:");
			int closeTag = in.indexOf(">", nsIndex);
			String topLevelDecs = in.substring(0, closeTag);
			
			log.debug(topLevelDecs);
			if (topLevelDecs.contains("xmlns:a14")) {
				// OK
			} else {
				in = topLevelDecs + " xmlns:a14=\"http://schemas.microsoft.com/office/drawing/2010/main\""
						+ in.substring(closeTag);
			}
			// 2017 10 02: workaround for where right side contains w14:paraId, but left side doesn't
			if (topLevelDecs.contains("xmlns:w14")) {
				// OK
			} else {
				in = topLevelDecs + " xmlns:w14=\"http://schemas.microsoft.com/office/word/2010/wordml\""
						+ in.substring(closeTag);
			}
			if (topLevelDecs.contains("xmlns:o")) {
				// OK
			} else {
				in = topLevelDecs + " xmlns:o=\"urn:schemas-microsoft-com:office:office\""
						+ in.substring(closeTag);
			}
			
			
			if (log.isDebugEnabled() ) {
				log.debug("Diff result:" + in);
			} 
			Reader reader = new StringReader(in);
			
			String simplified = null;
				try {
					simplified = combineAdjacent(inputFactory.createXMLStreamReader(reader) );
				} catch (XMLStreamException e) {
					e.printStackTrace();
//					log.debug("left: " + XmlUtils.marshaltoString(objectLeft, true, false));
//					log.debug("right: " + XmlUtils.marshaltoString(objectRight, true, false));					
				}
			
			log.debug("\n\n Diff'd input to transform: \n\n" + simplified );
							
			StreamSource src = new StreamSource(new StringReader(simplified));
			transformDiffxOutputToWml(result, author, date, docPartRelsNewer,
					docPartRelsOlder, src);
			
		} catch (Exception exc) {
			exc.printStackTrace();
		}			
		
	}

	/**
	 * @param result
	 * @param author
	 * @param date
	 * @param docPartRelsLeft
	 * @param docPartRelsRight
	 * @param src
	 * @throws Exception
	 */
	private void transformDiffxOutputToWml(javax.xml.transform.Result result,
			String author, java.util.Calendar date,
			RelationshipsPart docPartRelsLeft,
			RelationshipsPart docPartRelsRight, StreamSource src)
			throws Exception {
		Map<String, Object> transformParameters = new java.util.HashMap<String, Object>();
					
		String dateString;
		if (date!=null) {				
			dateString = RFC3339_FORMAT.format(date.getTime()) ;
		} else {
			// TODO FIXME - JAXB requires a real date.
			// What to give it?  
			// The alternative is to change the xslt
			// to omit the @date entirely if its unknown
			dateString = "2009-03-11T17:57:00Z";
		}
		transformParameters.put("Differencer", this);
		transformParameters.put("date", dateString);
		transformParameters.put("author", author);
		transformParameters.put("docPartRelsLeft",  docPartRelsLeft);
		transformParameters.put("docPartRelsRight", docPartRelsRight);
		transformParameters.put("relsDiffIdentifier", relsDiffIdentifier);  
		
		log.debug("invoking xsltDiffx2Wml");
		XmlUtils.transform(src, xsltDiffx2Wml, transformParameters, result);
	}
	
	public void markupAsInsertion(org.docx4j.wml.SdtContentBlock cbLeft, 
			javax.xml.transform.Result result,
			String author, java.util.Calendar date,
			RelationshipsPart docPartRelsLeft) {

		Writer diffxResult = new StringWriter();
				
		try {

	    	// Now marshall it
			JAXBContext jc = Context.jc;
			Marshaller marshaller=jc.createMarshaller();
			org.w3c.dom.Document doc = org.docx4j.XmlUtils.neww3cDomDocument();

			marshaller.marshal(cbLeft, doc);
			
			
			Map<String, Object> transformParameters = new java.util.HashMap<String, Object>();
						
			if (date!=null) {				
				String dateString = RFC3339_FORMAT.format(date.getTime()) ;
				transformParameters.put("date", dateString);
			}
			
			transformParameters.put("Differencer", this);
			transformParameters.put("author", author);
			transformParameters.put("docPartRelsLeft",  docPartRelsLeft);
			transformParameters.put("docPartRelsRight", null);
			transformParameters.put("relsDiffIdentifier", relsDiffIdentifier);  
			XmlUtils.transform(doc, xsltMarkupInsert, transformParameters, result);
			
		} catch (Exception exc) {
			exc.printStackTrace();
		}					

	}

	public void markupAsDeletion(org.docx4j.wml.SdtContentBlock cbLeft, 
			javax.xml.transform.Result result,
			String author, java.util.Calendar date,
			RelationshipsPart docPartRelsRight) {

		Writer diffxResult = new StringWriter();
				
		try {

	    	// Now marshall it
			JAXBContext jc = Context.jc;
			Marshaller marshaller=jc.createMarshaller();
			org.w3c.dom.Document doc = org.docx4j.XmlUtils.neww3cDomDocument();

			marshaller.marshal(cbLeft, doc);
			
			
			Map<String, Object> transformParameters = new java.util.HashMap<String, Object>();
						
			if (date!=null) {				
				String dateString = RFC3339_FORMAT.format(date.getTime()) ;
				transformParameters.put("date", dateString);
			}
			
			transformParameters.put("Differencer", this);
			transformParameters.put("author", author);
			transformParameters.put("docPartRelsLeft",  null);
			transformParameters.put("docPartRelsRight", docPartRelsRight);
			transformParameters.put("relsDiffIdentifier", relsDiffIdentifier);  
			log.debug("applying xsltMarkupDelete");
			XmlUtils.transform(doc, xsltMarkupDelete, transformParameters, result);
			
		} catch (Exception exc) {
			exc.printStackTrace();
		}					

	}
	
	
	/**
	 * Compare 2 p objects, returning a result containing
	 * w:ins and w:del elements  
	 * 
	 * @param pl - the left paragraph
	 * @param pr - the right paragraph
	 * @param result 
	 */
	public void diff(P pl, P pr, javax.xml.transform.Result result,
			String author, java.util.Calendar date,
			RelationshipsPart docPartRelsLeft, RelationshipsPart docPartRelsRight,
			boolean preProcess) {
		
		
		
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

        String leftXmlOld = null;
        String rightXmlOld = null;
        if (!preProcess || log.isDebugEnabled() ) {
	        leftXmlOld = org.docx4j.XmlUtils.marshaltoString(pl, true, false);
	        rightXmlOld = org.docx4j.XmlUtils.marshaltoString(pr, true, false);
	        	// NB boolean prettyprint must be set to false
	        	// with diffxConfig 
				//    .setIgnoreWhiteSpace(false);
				//    .setPreserveWhiteSpace(true);
	        	// because otherwise we get ins, del around
	        	// indentation whitespace, and this 
	        	// breaks the transform to wml.

        }

		if (!preProcess) {
			
	        String naive = getDiffxOutput(leftXmlOld, rightXmlOld);

	        // Debug purposes only!
	        log.debug("\n\n naive difference \n\n" );	        
	        log.debug(naive) ;
	        
	        
	        log.info("\n\n <p> difference without preprocessing </p> \n\n" );
			try {
				
				XMLInputFactory inputFactory = XMLInputFactory.newInstance();
				inputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
				inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false); // a DTD is merely ignored, its presence doesn't cause an exception
				
				//java.io.InputStream is = new java.io.ByteArrayInputStream(naive.getBytes("UTF-8"));
				Reader reader = new StringReader(naive);
				String simplified = combineAdjacent(inputFactory.createXMLStreamReader(reader) );
				
				log.debug("\n\n combineAdjacent: \n\n" + simplified );
								
				StreamSource src = new StreamSource(new StringReader(simplified));
				Map<String, Object> transformParameters = new java.util.HashMap<String, Object>();
				transformParameters.put("Differencer", this);
				transformParameters.put("author", author);
				transformParameters.put("docPartRelsLeft",  docPartRelsLeft);
				transformParameters.put("docPartRelsRight", docPartRelsRight);
				transformParameters.put("relsDiffIdentifier", relsDiffIdentifier);  
				XmlUtils.transform(src, xsltDiffx2Wml, transformParameters, result);
				
			} catch (Exception exc) {
				exc.printStackTrace();
			}			
			
			return;
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
        String leftXmlNew = org.docx4j.XmlUtils.marshaltoString(newLeftP, true, false);
        log.debug(leftXmlNew) ;

		log.debug("\n\n Right input \n\n" );
        log.debug(rightXmlOld) ;        
        
        log.debug("\n\n New right side \n\n" );
        String rightXmlNew = org.docx4j.XmlUtils.marshaltoString(newRightP, true, false);
        log.debug(rightXmlNew) ;
        
        log.debug("\n\n Difference \n\n" );
        
        String diffx = getDiffxOutput(leftXmlNew, rightXmlNew);
        //String diffx = getDiffxOutput(rightXmlNew, leftXmlNew);
        log.debug(diffx) ;

        log.info("\n\n <p> difference with pre-processing</p> \n\n" );
		try {
			StreamSource src = new StreamSource(new StringReader(diffx));
			transformDiffxOutputToWml(result, author, date, docPartRelsLeft,
					docPartRelsRight, src);			
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
		newT.setSpace("preserve");
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
		diffxConfig.setIgnoreWhiteSpace(false);
		diffxConfig.setPreserveWhiteSpace(true);

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
		// always namespace aware in docx4j
		try {
			Document document = XmlUtils.getNewDocumentBuilder().parse(new InputSource(xml));
			return document;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
    /* diffx treats each word as a token, and its output may
     * look like:
     * 
     *     <ins>Well,</ins><ins> </ins><ins>maybe</ins>
     * 
     * This method will change that to:
     * 
     *     <ins>Well, maybe</ins>
     */ 
    private static String combineAdjacent(XMLStreamReader reader) throws XMLStreamException
    {
        /* A more complex example:
         * 
<w:r><w:t>It<ins> </ins><ins>is</ins><ins> </ins><ins>good</ins> <ins>indeed</ins>
<del>would</del> <ins>very</ins><del>be</del> good to read paragraph 
spacing<ins> </ins><ins>properly</ins><ins> </ins><ins>I</ins><ins> </ins><ins>would</ins> <ins>say.</ins><del>property.</del></w:t></w:r>

            becomes 

<w:r><w:t>It<ins> is good</ins> <ins>indeed</ins>
<del>would</del> <ins>very</ins><del>be</del> good to read paragraph 
spacing<ins> properly I would</ins> <ins>say.</ins><del>property.</del></w:t></w:r>
         * 
         * 
         * 
         */

        String memory = null;

        
//        XmlWriterSettings settings = new XmlWriterSettings();
//        settings.OmitXmlDeclaration = true; // important!
//        settings.Encoding = Encoding.UTF8;
        java.io.StringWriter stringWriter = new java.io.StringWriter();

        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        XMLStreamWriter writer = factory.createXMLStreamWriter(stringWriter);
        
        while ( reader.hasNext() ) {
        	
        	int event = reader.next();
        	        	
        	switch (event) {
        	
        		case XMLStreamConstants.END_ELEMENT: 

                    if (reader.getLocalName().equals("ins"))
                    {
                        memory = "ins";
                        // and don't write it 
                        // until we see what the next 
                        // element is
                    }
                    else if (reader.getLocalName().equals("del"))
                    {
                        memory = "del";
                    }
                    else
                    {
                    	writer.writeEndElement();
                    }
                    break;
           

                case XMLStreamConstants.START_ELEMENT:
                	
                    try {
						if (memory != null)
						{
						    // There is an </ins> (or </del>) we have just ignored

						    if (memory.equals(reader.getLocalName()))
						    {
						        // Hit </ins><ins> 
						        // This is the case where
						        // we don't write either of them ...
						        memory = null;
						        continue;
						    }
						    else
						    {
						        // This is a different node,
						        // so write the </ins>
						    	writer.writeEndElement();
						        memory = null;
						    }
						}
						if (reader.getNamespaceURI() == null ) {
							writer.writeStartElement(reader.getLocalName());
							
						} else {
							writer.writeStartElement(reader.getPrefix(), reader.getLocalName(), reader.getNamespaceURI());
						}
						for (int i=0; i<reader.getAttributeCount() ; i++ ) {
							
							if (reader.getAttributeNamespace(i)==null) {
								//log.debug("Writing " + reader.getLocalName() + "/@" + reader.getAttributeLocalName(i) );
								writer.writeAttribute(
										reader.getAttributeLocalName(i), 
										reader.getAttributeValue(i) );
							} else {
								writer.writeAttribute(
										reader.getAttributePrefix(i), 
										reader.getAttributeNamespace(i), 
										reader.getAttributeLocalName(i), 
										reader.getAttributeValue(i));
							}
						}
						for (int i=0; i<reader.getNamespaceCount() ; i++ ) {
							writer.writeNamespace(
									reader.getNamespacePrefix(i),
									reader.getNamespaceURI(i) );
						}
					} catch (XMLStreamException e) {
						throw new XMLStreamException("Issue at element: " + reader.getLocalName() + "\n",e);
					}
                    
                    break;

                case XMLStreamConstants.CHARACTERS:
                    if (memory != null)
                    {
                        // eg "</ins>HERE"
                    	writer.writeEndElement();
                        memory = null;
                    }
                    writer.writeCharacters(reader.getText());
                    break;


                case XMLStreamConstants.START_DOCUMENT:

                    writer.writeStartDocument();
                    break;

                case XMLStreamConstants.END_DOCUMENT:

                    writer.writeEndDocument();
                    break;
                    
                default:
                	
                	// Ignore 
            }

        }

        writer.flush();
        writer.close();
        
        return stringWriter.toString();
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
    
	public static void main(String[] args) throws Exception {
		
		// Result format
		Writer diffxResult = new StringWriter();

		// Run the diff
		try {
			
			XMLInputFactory inputFactory = XMLInputFactory.newInstance();
			inputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
			inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false); // a DTD is merely ignored, its presence doesn't cause an exception
			
			//java.io.InputStream is = new java.io.ByteArrayInputStream(naive.getBytes("UTF-8"));
			String simplified = combineAdjacent(
					inputFactory.createXMLStreamReader(new FileInputStream(new File("tmp_adj.xml"))) );
			
			System.out.println("Done");
		} catch (Exception exc) {
			exc.printStackTrace();
			diffxResult = null;
		}
	}

    

}
