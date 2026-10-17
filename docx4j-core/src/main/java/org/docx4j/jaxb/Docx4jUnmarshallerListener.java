package org.docx4j.jaxb;

import javax.xml.stream.XMLStreamReader;

import jakarta.xml.bind.Unmarshaller;

import org.docx4j.dml.CTNonVisualDrawingProps;
import org.docx4j.mce.AlternateContent;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Docx4jUnmarshallerListener extends Unmarshaller.Listener {

	protected static Logger log = LoggerFactory.getLogger(Docx4jUnmarshallerListener.class);
	
	private static final String MCE_NS = "http://schemas.openxmlformats.org/markup-compatibility/2006";

	private JaxbXmlPart part;

	private XMLStreamReader reader;

	public Docx4jUnmarshallerListener(JaxbXmlPart part) {
		this.part = part;
		log.debug(part.getClass().getName());
	}

	/**
	 * The reader the unmarshaller pulls from, whose namespace context resolves each
	 * {@code mc:Choice}'s {@code Requires} prefixes by URI as it is unmarshalled
	 * ({@link McSelection#recordRequiresNamespaces}; CR-046). Without one (a DOM source), the
	 * prefixes are resolved by docx4j's conventional table when they are read.
	 *
	 * @since 17.3.4
	 */
	public Docx4jUnmarshallerListener(JaxbXmlPart part, XMLStreamReader reader) {
		this(part);
		this.reader = reader;
	}
	
//	@Override
//	public void beforeUnmarshal(Object target, Object parent) {
//
//	}

  @Override
  public void afterUnmarshal(Object target, Object parent) {
	  
		if (target instanceof AlternateContent.Choice) {
			
			AlternateContent.Choice choice = (AlternateContent.Choice)target;
			// the reader stands at the element's end tag, where the declarations in force at
			// the element are still in scope; anywhere else, record nothing
			if (reader != null && reader.isEndElement() && "Choice".equals(reader.getLocalName())
					&& MCE_NS.equals(reader.getNamespaceURI())) {
				McSelection.recordRequiresNamespaces(choice, reader.getNamespaceContext());
			}
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
			if (part.getPackage()!=null) {
				part.getPackage().getDrawingPropsIdTracker().registerId(docPr.getId());
			}
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
