package org.docx4j.model.fields;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.Docx4JRuntimeException;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.FldChar;
import org.docx4j.wml.P;
import org.docx4j.wml.ProofErr;
import org.docx4j.wml.R;
import org.docx4j.wml.STFldCharType;
import org.docx4j.wml.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

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
	
	private static Logger log = LoggerFactory.getLogger(FieldsPreprocessor.class);		

    private final static QName _RInstrText_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", 
    		"instrText");
    private final static QName _PHyperlink_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", 
    		"hyperlink");
    
	
	static Templates xslt;			
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
		
//		XPathsPart xPathsPart = null;
				
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
	
	
	/**
	 * Convert the field(s) in the input P into a predictable
	 * format, and add a FieldRef object to the list for each
	 * top level field encountered.  
	 * 
	 * WARNING: this method should not be used where a field 
	 * in the P extends into a subsequent P.
	 * 
	 * @param p
	 * @param fieldRefs
	 * @return the modified P
	 */
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
		if(log.isDebugEnabled()) {
            log.debug(XmlUtils.marshaltoString(p));
        }
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

			// Handling for hyperlink (can occur in field result, and might contain another
			// nested field).  Since at present the field processing here is for
			// MERGEFIELD and DOCPROPERTY fields, this is currently just handled by else below.
			
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
				
				log.debug("Retaining" + XmlUtils.unwrap(o).getClass().getName());

				attachmentPoint.getContent().add(o);

				// prepare new run
				newR = Context.getWmlObjectFactory().createR();
				
			}

//			if (newR.getContent().size() > 0 && !attachmentPoint.getContent().contains(newR)) {
//				attachmentPoint.getContent().add(newR);
//			}
			
		}
		
	}
	
	private boolean fieldIsTopLevel() {
		return stack.size()==1;
	}
	
	private boolean inParentResult() {
		
		FieldRef thisField = null;
		try {
			thisField = stack.pop();
		} catch (NoSuchElementException e) {
			throw new Docx4JRuntimeException("No FieldRef yet", e);
		}
		
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
	
	/**
	 * Its preserved, if it is locked.
	 * 
	 * If it isn't locked, it is preserved unless its a MERGEFIELD or a DOCPROPERTY field.
	 * 
	 * @param fieldRef
	 * @return
	 */
	private boolean preserveResult(FieldRef fieldRef) {
		
		if (fieldRef.isLock()) return true;
		
		
		String fldName = fieldRef.getFldName();
		if (fldName==null) return true;
		
		if (fldName.equals("MERGEFIELD")
				|| fldName.equals("DOCPROPERTY")) {
			return false;
		}
		return true;
	}

	private boolean preserveParentResult() {
		
		FieldRef thisField = stack.pop();
		FieldRef parentField = stack.pop();
		boolean preserveParentResult = preserveResult(parentField);
		// restore stack
		stack.push(parentField);
		stack.push(thisField);
		return preserveParentResult;
	}
	
	private void handleRun(R existingRun, ContentAccessor newAttachPoint) {
		
		// note that the newR object persists between invocations of this method,
		// so you have to be careful to actually add it to the docx 
		// before re-creating it

        if(log.isDebugEnabled()) {
            log.debug("\nInput run: \n " + XmlUtils.marshaltoString(existingRun, true, true));
        }
		
		for (Object o2 : existingRun.getContent() ) {
			
			newR.setRPr(existingRun.getRPr());

			if (isCharType(o2, STFldCharType.BEGIN)) {
				
				log.debug("\n\n begin.. ");

				// Setup a FieldRef object 
				currentField = new FieldRef((FldChar)XmlUtils.unwrap(o2));							
				currentField.setParent(newAttachPoint);							
				currentField.setBeginRun(newR); // may as well do this
				
				stack.push(currentField);
				
				if (inParentResult()) {
					
					if (preserveParentResult()) {
						newR.getContent().add(o2);
					} else {
						log.debug(".. but in result, so don't add to run");
					}
					
				} else {
					
					if ( fieldIsTopLevel() ) { 
					
						newR = Context.getWmlObjectFactory().createR();
						newR.getContent().add(o2);					
						
						currentField.setBeginRun(newR); // IMPORTANT, so we can delete it when we perform mail merge
						
						fieldRefs.add(currentField);					
					} else {
						newR.getContent().add(o2);
						
						stack.peek().getInstructions().add(currentField);
					}
				}
				
			} else if (isCharType(o2, STFldCharType.SEPARATE)) {
				
				currentField.setSeenSeparate(true);
				
				if (inParentResult()) {

					if (preserveParentResult()) {
						newR.getContent().add(o2);
					} else {
						log.debug(".. but in result, so don't add to run");
					}

				} else {
				
					newR.getContent().add(o2);
					if (!newAttachPoint.getContent().contains(newR)) {
						newAttachPoint.getContent().add(newR);
                        if(log.isDebugEnabled()) {
                            log.debug("-- attaching -->" + XmlUtils.marshaltoString(newR, true, true));
                        }
					}
					
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

					if (preserveParentResult()) {
						
						newR.getContent().add(o2);

						if (currentField.getFldName().equals("FORMTEXT")) {
							/*
							 * Workaround for a bug in Word 2010.
							 * 
							 * If you have multiple FORMTEXT in a single run,
							 * for example:
							 * 
							 *      <w:fldChar w:fldCharType="begin">
							          <w:ffData>
							            <w:name w:val="Text12"/>
							            <w:enabled/>
							            <w:calcOnExit w:val="false"/>
							            <w:textInput/>
							          </w:ffData>
							        </w:fldChar>
							        <w:instrText xml:space="preserve"> FORMTEXT </w:instrText>
							        <w:fldChar w:fldCharType="separate"/>
							        <w:t> </w:t>
							        <w:fldChar w:fldCharType="end"/>
							        <w:fldChar w:fldCharType="begin">
							          <w:ffData>
							            <w:name w:val="Text12"/>
							            <w:enabled/>
							            <w:calcOnExit w:val="false"/>
							            <w:textInput/>
							          </w:ffData>
							        </w:fldChar>
							        <w:instrText xml:space="preserve"> FORMTEXT </w:instrText>
							        <w:fldChar w:fldCharType="separate"/>
							        <w:t> </w:t>
							        <w:fldChar w:fldCharType="end"/>						
							 *
							 * Word 2010 does not display all the w:t elements (ie spaces appear to
							 * be missing).
							 * 
							 * Adding w:t/@xml:space="preserve" doesn't help.
							 * 
							 * So the workaround here is to start a new run after each END tag.
							 */
							if (!newAttachPoint.getContent().contains(newR)) {
								newAttachPoint.getContent().add(newR);
                                if(log.isDebugEnabled()) {
                                    log.debug("-- attaching -->" + XmlUtils.marshaltoString(newR, true, true));
                                }
							}
							newR = Context.getWmlObjectFactory().createR();						
						}						
					} else {
						log.debug(".. but in result, so don't add to run");
					}

				} else {  // still in END processing

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
													
							if (!newAttachPoint.getContent().contains(newR)) {
								newAttachPoint.getContent().add(newR);
                                if(log.isDebugEnabled()) {
                                    log.debug("-- attaching -->" + XmlUtils.marshaltoString(newR, true, true));
                                }
							}
							
						}					
						
						// set up results slot - only for top-level fields
						newR = currentField.getResultsSlot(); // MERGEFORMAT processing below may have set this already
						if (newR==null) {
							newR = Context.getWmlObjectFactory().createR();
							currentField.setResultsSlot(newR);
						}
						if (!newAttachPoint.getContent().contains(newR)) { // test, since this is also done immediately before each loop ends
							newAttachPoint.getContent().add(newR);
                            if(log.isDebugEnabled()) {
                                log.debug("-- attaching -->" + XmlUtils.marshaltoString(newR, true, true));
                            }
						}
						
						
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
                        if(log.isDebugEnabled()) {
                            log.debug("-- attaching -->" + XmlUtils.marshaltoString(newR, true, true));
                        }
					}
				
					newR = Context.getWmlObjectFactory().createR();						
				
			} else if ( !currentField.haveSeenSeparate() ) {
				
				// Handles problems with empty w:instrText elements within complex field "begin" section
				Object o = XmlUtils.unwrap(o2);
				if (o instanceof Text && ((Text) o).getValue().trim().isEmpty()) {
					log.debug("Empty w:instrText found. Ignore it!");
					continue;
				}
				
				
//				log.debug("Processing " +((JAXBElement<Text>)o2).getValue().getValue() );
				
				currentField.getInstructions().add(o2);
				if (inParentResult()) {

					if (preserveParentResult()) {
						newR.getContent().add(o2);
					} else {
						log.debug(".. but in result, so don't add to run");
					}

				} else {				
					newR.getContent().add(o2);
				}

			} else if (preserveResult(currentField)) {
				// ie locked, or not MERGEFIELD, or DOCPROPERTY
				log.debug("preserveResult-> adding");
				newR.getContent().add(o2);		
				
				if (currentField.getResultsSlot()==null) {
					currentField.setResultsSlot(newR);  // no harm in doing this - same as in SEPARATE processing?
				} else if (currentField.getResultsSlot()!=newR) {
					log.warn("Multiple runs in results slot?");
				}
				
			} else {
				// result content .. can ignore unless it has \* MERGEFORMAT
				
				// if \* MERGEFORMAT, attach the rPr of first run in the result
				if (o2 instanceof R
						&& currentField.isMergeFormat() 
						&& currentField.getResultsSlot()==null) {

					R resultR = Context.getWmlObjectFactory().createR();
					currentField.setResultsSlot(resultR);
					resultR.setRPr(((R)o2).getRPr()); // could be null, but that's ok
					log.debug("MERGEFORMAT Set rPr");
				}
				
				
				
				// TODO: a TOC field usually has a PAGEREF wrapped in a hyperlink in its
				// result part.  We should either keep the entire result, or empty it.
				// only do this if the field has no nested field; we need a way to look ahead
				// to see whether a nested field is coming up)
				
				// we only want a single run between SEPARATOR and END,
				// and we added that in the SEPARATE stuff above
                if(log.isDebugEnabled()) {
                    log.debug("IGNORING " + XmlUtils.marshaltoString(o2, true, true));
                }
				
			} 

			// Doesn't solve the problem of Word failing to display some spaces.
//			if ( o2 instanceof Text
//					|| ((o2 instanceof JAXBElement
//							&& ((JAXBElement)o2).getName().equals(_RT_QNAME)) )	) {
//				Text t = (Text)XmlUtils.unwrap(o2);
//				t.setSpace("preserve");
//			}
			
			if (newR.getContent().size() > 0 && !newAttachPoint.getContent().contains(newR)) {
				newAttachPoint.getContent().add(newR);
			}
			
		} // end for (Object o2 : existingRun.getContent() )
		
	}
		
	    private final static QName _RT_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "t");
		
	
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
				log.debug(fldChar.getFldCharType().toString());				
			}
		}
		return false;
	}	
	
}
