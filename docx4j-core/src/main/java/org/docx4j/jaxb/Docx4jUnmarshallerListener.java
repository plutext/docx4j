package org.docx4j.jaxb;

import jakarta.xml.bind.Unmarshaller;

import org.docx4j.dml.CTNonVisualDrawingProps;
import org.docx4j.mce.AlternateContent;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Docx4jUnmarshallerListener extends Unmarshaller.Listener {

	protected static Logger log = LoggerFactory.getLogger(Docx4jUnmarshallerListener.class);
	
	private JaxbXmlPart part;

	public Docx4jUnmarshallerListener(JaxbXmlPart part) {
		this.part = part;
		log.debug(part.getClass().getName());
	}
	
//	@Override
//	public void beforeUnmarshal(Object target, Object parent) {
//
//	}

  @Override
  public void afterUnmarshal(Object target, Object parent) {
	  
		if (target instanceof AlternateContent.Choice) {
			
			AlternateContent.Choice choice = (AlternateContent.Choice)target;
			if (choice.getRequires()!=null) {
				part.addMcChoiceNamespace(choice.getRequires());
				if (log.isDebugEnabled()) {
					log.debug("after, Need to declare " + choice.getRequires() );
				}
			}
			if (choice.getIgnorable()!=null) {
				part.addMcChoiceNamespace(choice.getIgnorable());
			}
			if (choice.getMustUnderstand()!=null) {
				part.addMcChoiceNamespace(choice.getMustUnderstand());
			}
		} else if (target instanceof  CTNonVisualDrawingProps){
			
			CTNonVisualDrawingProps docPr = (CTNonVisualDrawingProps)target;
			part.getPackage().getDrawingPropsIdTracker().registerId(docPr.getId());
		}
//		else if (log.isDebugEnabled() 
//				) {
//
//			
//			Object unwrappd = XmlUtils.unwrap(target);
//			Object unwrappdP = XmlUtils.unwrap(parent);
//			
//			if (unwrappd instanceof Text) {
//			
//				// RI invokes twice!
//	
//				Text t = (Text)unwrappd;
//				log.debug(t.getValue() );			
//
//			} else if (unwrappdP instanceof PPr
//					|| unwrappdP instanceof ParaRPr
//					|| unwrappdP instanceof RPr 
//					|| unwrappdP instanceof  org.docx4j.wml.Tabs
//					|| unwrappdP instanceof  org.docx4j.wml.PPrBase.PBdr) {
//				
//				// suppress
//				
//			} else if (target instanceof JAXBElement) {
//				// MOXy doesn't invoke in this case!
//				log.debug(" JAXBEl " + ((JAXBElement)target).getName().getLocalPart());
//			} else {
//	
//				if (unwrappdP!=null)
//					log.debug(target.getClass().getName() );// + " parent " + unwrappdP.getClass().getName() );
//				
//			}
//		}
	}

}
