package org.docx4j.model.fields;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.opendope.XPathsPart;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.FldChar;
import org.docx4j.wml.P;
import org.docx4j.wml.ProofErr;
import org.docx4j.wml.R;
import org.docx4j.wml.STFldCharType;

/**
 * This class puts fields into a "canonical" representation
 * (see FieldRef for description).
 * 
 * It does this in 2 steps:
 * - step 1: use XSLT to convert simple fields into complex ones
 * - step 2: put all the instructions into a single run
 * 
 * Currently the canonicalisation is done at the paragraph level,
 * so it is not suitable for fields (such as TOC) which extend across paragraphs.
 * TOC will need to be regenerated (using Word) if touched by canonicalisation.
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
		 
		 * Note that the content between begin and separate could be more complex
		 * including nested fields.
		 **/
		
		
		FieldsPreprocessor fp = new FieldsPreprocessor(fieldRefs);
		return fp.canonicaliseInstance(p);
	}
	
	private P canonicaliseInstance(P p) {

		P newP = Context.getWmlObjectFactory().createP();
		newP.setPPr(p.getPPr());
		
		newR = Context.getWmlObjectFactory().createR();
//		fieldRPr = null;
		
		stack = new LinkedList<FieldRef>();
		
		handleContent(p.getContent(), newP);

		// log.debug(XmlUtils.marshaltoString(newP, true));

		return newP;
	}
	
	/**
	 * A list of FieldRef objects representing outermost fields
	 * only.
	 */
	private List<FieldRef> fieldRefs;
	
	
	private LinkedList<FieldRef> stack;
	private FieldRef currentField=null;
	
	private R newR;
	
	private void handleContent(List<Object> objects, ContentAccessor attachmentPoint) {
		// handles case where the run(s) containing the field are inside a P, or inside a P.Hyperlink 
		// (eg a PAGEREF in a table of contents).
		
		for (Object o : objects ) {

			// Handling for hyperlink in field result, which might contain another
			// nested field. Since this is in the result, we drop it.
			
			//	if ( o instanceof P.Hyperlink
			//			|| ((o instanceof JAXBElement
			//					&& ((JAXBElement)o).getName().equals(_PHyperlink_QNAME)) )	) {
			//	
			
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
				
				log.debug(XmlUtils.unwrap(o));

				// Add the previous run, if necessary
				if (newR.getContent().size() > 0) {
					attachmentPoint.getContent().add(newR);
					newR = Context.getWmlObjectFactory().createR();
				}

				attachmentPoint.getContent().add(o);
			}

		}
		if (newR.getContent().size() > 0 && !attachmentPoint.getContent().contains(newR)) {
			attachmentPoint.getContent().add(newR);
		}
		
	}
	
	private boolean fieldIsTopLevel() {
		return stack.size()==1;
	}
	
	private boolean inParentResult() {
		
		FieldRef thisField = stack.pop();
		try {
			FieldRef parentField = stack.pop();
			boolean inResult = parentField.haveSeenSeparate();
			// restore stack
			stack.push(parentField);
			stack.push(thisField);
			return inResult;
		} catch (NoSuchElementException e) {
			// No parent
			// restore stack
			stack.push(thisField);
			return false;
		}
		
	}
	
	private void handleRun(R existingRun, ContentAccessor newAttachPoint) {
		
		// note that the newR object persists between invocations of this method,
		// so you have to be careful to actually add it to the docx 
		// before re-creating it
		
		log.debug("\nInput run: \n " + XmlUtils.marshaltoString(existingRun, true, true));
		
		for (Object o2 : existingRun.getContent() ) {
			
			newR.setRPr(existingRun.getRPr());

			if (isCharType(o2, STFldCharType.BEGIN)) {
				
				log.debug("\n\n begin.. ");

				// Setup a FieldRef object 
				currentField = new FieldRef((FldChar)XmlUtils.unwrap(o2));							
				currentField.setParent(newAttachPoint);							
				currentField.setBeginRun(newR);
				
				stack.push(currentField);
				
				if (inParentResult()) {

					log.debug(".. but in result, so don't add to run");
					// TODO: if parentField.isLock(), we should preserve its result 
					
				} else {
					
					if ( fieldIsTopLevel() ) { 
					
						newR = Context.getWmlObjectFactory().createR();
						newR.getContent().add(o2);					
	
						fieldRefs.add(currentField);					
					} else {
						newR.getContent().add(o2);
						
						stack.peek().getInstructions().add(currentField);
					}
				}
				
			} else if (isCharType(o2, STFldCharType.SEPARATE)) {
				
				currentField.setSeenSeparate(true);

				if (inParentResult()) {

					log.debug(".. but in result, so don't add to run");

				} else {
				
					newR.getContent().add(o2);
					newAttachPoint.getContent().add(newR);
					
					if ( fieldIsTopLevel() ) {
						// Top level field separator
						
						// Create result slot
						newR = Context.getWmlObjectFactory().createR();
						currentField.setResultsSlot(newR); 
					}
				}
					
			} else if (isCharType(o2, STFldCharType.END)) {
				
				log.debug("\n\n .. end ");
				
				if (inParentResult()) {

					log.debug(".. but in result, so don't add to run");

				} else {

					if ( fieldIsTopLevel() ) {
					
						if (!currentField.haveSeenSeparate()) {
							// Word 2010 can produce a docx where:
							//  <w:r>
							//    <w:fldChar w:fldCharType="separate"/>
							//  </w:r>
							// is missing (valid per spec).
							
							// For top level fields only, we add this
							log.debug(".. ADDING SEP ..  ");
	
	//						R separateR = Context.getWmlObjectFactory().createR();							
							FldChar fldChar = Context.getWmlObjectFactory().createFldChar();
							fldChar.setFldCharType(STFldCharType.SEPARATE);
							newR.getContent().add(fldChar);
													
							newAttachPoint.getContent().add(newR);
							log.debug("\n-- attaching -->" + XmlUtils.marshaltoString(newR, true, true));
							
						}					
						
						// set up results slot - only for top-level fields
						newR = Context.getWmlObjectFactory().createR();
						currentField.setResultsSlot(newR); 						
						newAttachPoint.getContent().add(newR);
						
						
						// create a run specifically for end char
						newR = Context.getWmlObjectFactory().createR();
						newAttachPoint.getContent().add(newR);
						newR.getContent().add(o2);
						currentField.setEndRun(newR);
						
						//for whatever follows the field
						newR = Context.getWmlObjectFactory().createR();
						
					} else {
						newR.getContent().add(o2);							
					}
					
				}
				
				stack.pop();
				currentField = stack.peek();
				
			} else if (currentField==null) {
					// run content before or after the field
					// - preserve this content
					
					newR.getContent().add(o2);

					if (!newAttachPoint.getContent().contains(newR)) {
						newAttachPoint.getContent().add(newR);
						log.debug("-- attaching -->" + XmlUtils.marshaltoString(newR, true, true));
					}
				
					newR = Context.getWmlObjectFactory().createR();						
				
			} else if ( !currentField.haveSeenSeparate() ) {
				
//				log.debug("Processing " +((JAXBElement<Text>)o2).getValue().getValue() );
				
				if (inParentResult()) {

					log.debug(".. but in result, so don't add to run");

				} else {				
					currentField.getInstructions().add(o2);
					newR.getContent().add(o2);
				}

			} else {
				// result content .. can ignore
				
				// TODO: a TOC field usually has a PAGEREF wrapped in a hyperlink in its
				// result part.  We should either keep the entire result, or empty it.
				// only do this if the field has no nested field; we need a way to look ahead
				// to see whether a nested field is coming up)
				
				// we only want a single run between SEPARATOR and END,
				// and we added that in the SEPARATE stuff above
				log.debug("IGNORING " + XmlUtils.marshaltoString(o2, true, true));
				
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
			} else {
				log.debug(fldChar.getFldCharType());				
			}
		}
		return false;
	}	
	
}
