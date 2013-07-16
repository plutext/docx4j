package org.docx4j.openpackaging.parts.WordprocessingML;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.TraversalUtil;
import org.docx4j.jaxb.Context;
import org.docx4j.wml.Body;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTObject;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.Pict;
import org.docx4j.wml.SdtElement;
import org.docx4j.wml.SdtPr;
import org.docx4j.wml.Tr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainDocumentPartMceIgnorableHelper {
	
	private static Logger log = LoggerFactory.getLogger(MainDocumentPartMceIgnorableHelper.class);
		
	protected String getMceIgnorable(Body body) {
		
		List<Object> content = body.getContent();
		
		// To avoid the traversing a large docx,
		// we'll try to use a hack here.
		// The idea is to force JAXB to include
		// namespace declarations for w14 and w15, by
		// using them in an innocuous manner.
		// It works by adding the following to the first
		// paragraph encountered:
		
		// <w:p w14:textId="fdcbd571" w14:paraId="fdcbd571" >
		//  <w:pPr>
		//    <w15:collapsed w:val="false"/>
		//  </w:pPr>
		//
		// If this turns out to cause problems, it could be
		// made configurable in docx4j.properties

		if (content.size()==0) {
			return null;
		}
		
		P p = null;
		for (Object o : content ) {
			if (o instanceof P ) {
				p = (P)o;
				break;
			}
		}
		
		if (p==null) {
			// No top level paragraph, so
			// do the work of traversing the document
			
			log.debug("traversing for w14, w15");
			
			IgnorablePrefixFinder finder = new IgnorablePrefixFinder();
			if (body.getSectPr()!=null
				&& body.getSectPr().getFootnoteColumns()!=null) {
					finder.needW15 = true;													
			}
			new TraversalUtil(content, finder);
			
			String mceIgnorableVal = "";
			if (finder.needW14) {
				mceIgnorableVal = "w14";
			}
			
			if (finder.needW15) {
				mceIgnorableVal += " w15";
			} 
			
			return mceIgnorableVal;
			
			
		} else {
			// The quick hack
			
			// For W14, we'll check/set paraId, textId
			if (p.getParaId()==null) {
				// Values MUST be greater than 0 and less than 0x80000000
				// So let's 
				
				String uuid = java.util.UUID.randomUUID().toString();
				// That's 32 digits, but 8'll do nicely
				uuid = uuid.replace("-", "").substring(0, 8);
				
				p.setParaId(uuid);
				p.setTextId(uuid);
			}
			
			// For W15, collapse
			PPr ppr = p.getPPr();
			if (ppr==null) {
				ppr = Context.getWmlObjectFactory().createPPr();
				p.setPPr(ppr);
			}
			if (ppr.getCollapsed()==null) {
				BooleanDefaultTrue notCollapsed = new BooleanDefaultTrue(); 
				notCollapsed.setVal(Boolean.FALSE);
				ppr.setCollapsed(notCollapsed);
			}
		}
		
		return "w14 w15";
    	
    }
	
	 /* 
		<xsd:element ref="w14:checkbox"  minOccurs="0"/>
		<xsd:element ref="w14:entityPicker"  minOccurs="0"/>
		
		*/
	private static final List<String> w14SdtPrNames = new ArrayList<String>();
	
	 /* 
		<xsd:element ref="w15:appearance"  minOccurs="0"/>
		<xsd:element ref="w15:color"  minOccurs="0"/>
		<xsd:element ref="w15:repeatingSection"  minOccurs="0"/>
		<xsd:element ref="w15:repeatingSectionItem"  minOccurs="0"/>
		<xsd:element ref="w15:webExtensionCreated"  minOccurs="0"/>
		<xsd:element ref="w15:webExtensionLinked"  minOccurs="0"/>

		<xsd:element ref="w15:dataBinding"  minOccurs="0"/>
		
		*/
	private static final List<String> w15SdtPrNames = new ArrayList<String>();
	
	static {
		w14SdtPrNames.add("checkbox");
		w14SdtPrNames.add("entityPicker");
		
		w15SdtPrNames.add("appearance");
		w15SdtPrNames.add("color");
		w15SdtPrNames.add("repeatingSection");
		w15SdtPrNames.add("repeatingSectionItem");
		w15SdtPrNames.add("webExtensionCreated");
		w15SdtPrNames.add("webExtensionLinked");
		w15SdtPrNames.add("dataBinding");
		
	}
	
    private static class IgnorablePrefixFinder extends CallbackImpl {
		
		boolean needW14 = false;
		boolean needW15 = false;
    	
    	
    	@Override
		public List<Object> apply(Object o) {
    		
    		// NB: the tests in this method have to be comprehensive,
    		// so if support for glow etc is introduced, tests for those
    		// will need to be added
			
			if (o instanceof org.docx4j.wml.P) {
				P p = (P)o;
				
				// W14?
				if (p.getParaId()!=null) {
					needW14 = true;
				}
				// W15?
				if (!needW15) {
					PPr ppr = p.getPPr();
					if (ppr!=null) {
						if (ppr.getCollapsed()!=null) {
							needW15 = true;						
						}
						
						if (ppr.getSectPr()!=null
								&& ppr.getSectPr().getFootnoteColumns()!=null) {
							needW15 = true;													
						}
					}
				}
				
			} else if ( o instanceof SdtElement) {
				SdtPr sdtPr = ((SdtElement)o).getSdtPr();
				if (sdtPr!=null) {
					if (contains(sdtPr.getRPrOrAliasOrLock(), "http://schemas.microsoft.com/office/word/2010/wordml", w14SdtPrNames)) {
						needW14 = true;
					} 
					if (contains(sdtPr.getRPrOrAliasOrLock(), "http://schemas.microsoft.com/office/word/2012/wordml", w15SdtPrNames)) {
						needW15 = true;
					} 
				}
			} else if ( o instanceof Tr) {  // TODO does this need to be unwrapped?
				if ( ((Tr)o).getParaId()!=null) {
					needW14 = true;					
				}
			} else if ( o instanceof CTObject) {  
				if ( ((CTObject)o).getAnchorId()!=null) {
					needW14 = true;					
				}
			} else if ( o instanceof Pict) {  
				if ( ((Pict)o).getAnchorId()!=null) {
					needW14 = true;					
				}
			} 
			
			return null;
    	}
    	
        private boolean contains(List<Object> list, String namespace, List<String> elementNames) {
        	
        	for (Object o : list) {
        		
        		if (o instanceof JAXBElement) {
        			QName qname = ((JAXBElement)o).getName();
        			if (namespace.equals(qname.getNamespaceURI())
        					&&  elementNames.contains(qname.getLocalPart())) {
            			return true;        				
        			}
        		}
        	}
        	return false;
        }
    	
    	@Override
		public boolean shouldTraverse(Object o) {
    		
    		if (o instanceof org.docx4j.wml.P) {
    			return false;
    		}
    		
    		if (needW14 && needW15) {
    			// We know we're done
    			return false;
    		}
    		
    		// Keep going
			return true;
		}
    	
    }
    
	

}
