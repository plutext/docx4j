package org.docx4j.model.fields;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.opendope.XPathsPart;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.FldChar;
import org.docx4j.wml.P;
import org.docx4j.wml.ProofErr;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.STFldCharType;
import org.docx4j.wml.Text;

/**
 * This class puts fields into a "canonical" representation
 * (see FieldRef for description).
 * 
 * It does this in 2 steps:
 * - step 1: use XSLT to convert simple fields into complex ones
 * - step 2: put all the instructions into a single run
 * 
 * @author jharrop
 *
 */
public class FieldsPreprocessor {
	
	private static Logger log = Logger.getLogger(FieldsPreprocessor.class);		

    private final static QName _RInstrText_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", 
    		"instrText");
    private final static QName _PHyperlink_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", 
    		"hyperlink");
    
	
	static Templates xslt;			
	private static XPathFactory xPathFactory;
	private static XPath xPath;
	static {
		try {
			Source xsltSource = new StreamSource(
						org.docx4j.utils.ResourceUtils.getResource(
								"org/docx4j/model/fields/FieldsSimpleToComplex.xslt"));
			xslt = XmlUtils.getTransformerTemplate(xsltSource);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		}
		
		xPathFactory = XPathFactory.newInstance();
		xPath = xPathFactory.newXPath();		
	}
	
	private FieldsPreprocessor(List<FieldRef> fieldRefs) {
		this.fieldRefs = fieldRefs;
	}

	/**
	 * Convert any w:fldSimple in this part to complex field. 
	 * @param part
	 * @throws Docx4JException
	 */
	public static void complexifyFields(JaxbXmlPart part) throws Docx4JException {
		
		org.w3c.dom.Document doc = XmlUtils.marshaltoW3CDomDocument(
				part.getJaxbElement() ); 	
		
		XPathsPart xPathsPart = null;
				
		JAXBContext jc = Context.jc;
		try {
			// Use constructor which takes Unmarshaller, rather than JAXBContext,
			// so we can set JaxbValidationEventHandler
			Unmarshaller u = jc.createUnmarshaller();
			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());
			javax.xml.bind.util.JAXBResult result = new javax.xml.bind.util.JAXBResult(u );
								
			org.docx4j.XmlUtils.transform(doc, xslt, null, result);
			
			part.setJaxbElement(result);
		} catch (Exception e) {
			throw new Docx4JException("Problems transforming fields", e);			
		}
				
	}
	
	
	public static P canonicalise(P p, List<FieldRef> fieldRefs) {
		/*
		 * Result is something like:
		 * 
		        <w:p>
		            <w:r>
		                <w:fldChar w:fldCharType="begin"/>
		                <w:instrText xml:space="preserve"> DATE  </w:instrText>
		                <w:fldChar w:fldCharType="separate"/>
		            </w:r>
		            <w:r>
		                <w:t>4/12/2011</w:t>
		            </w:r>
		            <w:r>
		                <w:fldChar w:fldCharType="end"/>
		            </w:r>
		        </w:p>		  
		 */
		
		// TODO merge adjacent instrText elements
		
		FieldsPreprocessor fp = new FieldsPreprocessor(fieldRefs);
		return fp.canonicaliseInstance(p);
	}
	
	private P canonicaliseInstance(P p) {

		P newP = Context.getWmlObjectFactory().createP();
		newP.setPPr(p.getPPr());
		
		depth = 0;
		newR = Context.getWmlObjectFactory().createR();
//		fieldRPr = null;
		currentField = null;
		seenSeparate=false;
		
		handleContent(p.getContent(), newP);

		// log.debug(XmlUtils.marshaltoString(newP, true));

		return newP;
	}
	
	private List<FieldRef> fieldRefs;
	private int depth;
	private boolean seenSeparate; // TODO doesn't handle nested fields
	private FieldRef currentField;
//	private RPr fieldRPr;
	private R newR;
	
	private void handleContent(List<Object> objects, ContentAccessor attachmentPoint) {
		// handles case where the run(s) containing the field are inside a P, or inside a P.Hyperlink 
		// (eg a PAGEREF in a table of contents).
		
		for (Object o : objects ) {

			// Handling for hyperlink in field result, which might contain another
			// nested field. Comment this out (making it handled by else case below), 
			// until we have better handling for nested fields
			
//			if ( o instanceof P.Hyperlink
//					|| ((o instanceof JAXBElement
//							&& ((JAXBElement)o).getName().equals(_PHyperlink_QNAME)) )	) {
//
//				// Add the previous run, if necessary
//				if (newR.getContent().size() > 0) {
//					attachmentPoint.getContent().add(newR);
//					newR = Context.getWmlObjectFactory().createR();
//				}
//				
//				P.Hyperlink hyperlink = (P.Hyperlink)XmlUtils.unwrap(o);
//				P.Hyperlink newH = Context.getWmlObjectFactory().createPHyperlink();
//				attachmentPoint.getContent().add(newH);
//				
//				// recurse to handle runs inside hyperlink
//				handleContent(hyperlink.getContent(), newH );
//
//				// don't want our last run being added by handleContent at 2 levels in the iteration 
//				newR=Context.getWmlObjectFactory().createR();
//				
//			} else 
			
				if ( o instanceof R ) {
				
				R existingRun = (R)o;
				handleRun(existingRun, attachmentPoint);

			} else if (o instanceof ProofErr) {
				// Ignore
				// What happens if we ignore eg grammarStart, but its matching
				// grammarEnd is outside and retained?
				// Well, a stray spellStart doesn't matter to Word 2010, so
				// assume others would be ok as well.
			} else {
				// its not something we're interested in
				
				System.out.println(XmlUtils.unwrap(o));

				// Add the previous run, if necessary
				if (newR.getContent().size() > 0) {
					attachmentPoint.getContent().add(newR);
					newR = Context.getWmlObjectFactory().createR();
				}

				attachmentPoint.getContent().add(o);

				// TODO .. detect separator, and remove stuff?
				// This model can't really do that right now, since it
				// works only within the paragraph.
			}

		}
		if (newR.getContent().size() > 0 && !attachmentPoint.getContent().contains(newR)) {
			attachmentPoint.getContent().add(newR);
		}
		
	}
	
	private void handleRun(R existingRun, ContentAccessor newAttachPoint) {
		
		// note that the newR object persists between invocations of this method,
		// so you have to be careful to actually add it to the docx 
		// before re-creating it
		
		log.debug("\nInput run: \n " + XmlUtils.marshaltoString(existingRun, true, true));
		
		for (Object o2 : existingRun.getContent() ) {

			if (isCharType(o2, STFldCharType.BEGIN)) {
				
//				log.debug("begin.. ");
				seenSeparate = false;
				
				depth++;
				System.out.println("depth: " + depth);
				if (depth==1 ) { 
				
					newR = Context.getWmlObjectFactory().createR();
					newR.setRPr(existingRun.getRPr());
					
					newR.getContent().add(o2);
					
					// Setup our FieldRef object - only top level fields for now
					currentField = new FieldRef();							
					fieldRefs.add(currentField);
					currentField.setParent(newAttachPoint);							
					currentField.setBeginRun(newR);

				}
			} else if (isCharType(o2, STFldCharType.SEPARATE)) {
				
				seenSeparate = true;
				
				newR.getContent().add(o2);
				if (depth==1 ) {
					// Top level field separator
					newAttachPoint.getContent().add(newR);
					log.debug("\n-- attaching -->" + XmlUtils.marshaltoString(newR, true, true));
					
					// May as well set this; we'll insert our result into
					// this (or recreate it).
					newR.setRPr(existingRun.getRPr() ); 
					
					newR = Context.getWmlObjectFactory().createR();
					newR.setRPr(existingRun.getRPr());
					
					currentField.setResultsSlot(newR); // FIXME: ensure newR is actually added!
					
				}
			}
			else if (isCharType(o2, STFldCharType.END)) {
				
				log.debug(".. end ");
				
				if (!seenSeparate) {
					log.debug(".. ADDING SEP ..  ");
					// Word 2010 can produce a docx where:
					//  <w:r>
					//    <w:fldChar w:fldCharType="separate"/>
					//  </w:r>
					// is missing (valid per spec), so add it
//					R separateR = Context.getWmlObjectFactory().createR();							
					FldChar fldChar = Context.getWmlObjectFactory().createFldChar();
					fldChar.setFldCharType(STFldCharType.SEPARATE);
					newR.getContent().add(fldChar);
					newAttachPoint.getContent().add(newR);
					log.debug("\n-- attaching -->" + XmlUtils.marshaltoString(newR, true, true));
					
					newR = Context.getWmlObjectFactory().createR();
					newR.setRPr(existingRun.getRPr());
					
					currentField.setResultsSlot(newR); 
				}
				
				depth--;
				if (depth==0 ) {
					// Top level field end - gets its own w:r
					newAttachPoint.getContent().add(newR);
					newR.getContent().add(o2);
					log.debug("\n-- attaching -->" + XmlUtils.marshaltoString(newR, true, true));
										
					currentField.setEndRun(newR);
					
					newR = Context.getWmlObjectFactory().createR();
					newR.setRPr(existingRun.getRPr());
					
				} else {
					newR.getContent().add(o2);							
				}
				
			} else if (o2 instanceof JAXBElement
					&& ((JAXBElement)o2).getName().equals(_RInstrText_QNAME)) {
				
//				log.debug("Processing " +((JAXBElement<Text>)o2).getValue().getValue() );
				
				currentField.setInstrText( (JAXBElement<Text>)o2);

				newR.getContent().add(o2);	
				

			} else if (depth==1 && seenSeparate) {
				// TODO: a TOC field usually has a PAGEREF wrapped in a hyperlink in its
				// result part.  We should either keep the entire result, or empty it.
				// only do this if the field has no nested field; we need a way to look ahead
				// to see whether a nested field is coming up)
				
				// we only want a single run between SEPARATOR and END,
				// and we added that in the SEPARATE stuff above
				System.out.println("IGNORING " + XmlUtils.marshaltoString(o2, true, true));
			} else {
				newR.getContent().add(o2);

				newAttachPoint.getContent().add(newR);
				log.debug("-- attaching -->" + XmlUtils.marshaltoString(newR, true, true));
				
				newR = Context.getWmlObjectFactory().createR();
				newR.setRPr(existingRun.getRPr());
			}
		} // end for (Object o2 : existingRun.getContent() )
		
	}
	
//	public static boolean containsCharType(Object o, STFldCharType charType) {
//		
//		if (o instanceof R) {
//			for (Object o2 : ((R)o).getContent() ) {
//				
//				if (isCharType(o2, charType)) {
//						return true;
//				}				
//			}
//		} 
//		return false;
//	}	

	public static boolean isCharType(Object o2, STFldCharType charType) {
		
		o2 = XmlUtils.unwrap(o2);
		
		if (o2 instanceof org.docx4j.wml.FldChar) {
			FldChar fldChar = (FldChar)o2;
			if (fldChar.getFldCharType().equals(charType) ) {
				return true;
			}
		}
		return false;
	}	
	
}
