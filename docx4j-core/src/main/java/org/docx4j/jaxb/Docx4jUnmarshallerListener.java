package org.docx4j.jaxb;

import java.util.ArrayList;
import java.util.List;

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

	/** Each mc:AlternateContent whose parent is a block, row or cell container, as unmarshalled (CR-051 section 3.2, step 1). */
	private final List<AlternateContent> blockLevel = new ArrayList<AlternateContent>();

	/** Each mc:AlternateContent with a w:sdt directly in a branch, delivered as a DOM element to bind once the tree stands (CR-051 section 11.3). */
	private final List<AlternateContent> branchSdts = new ArrayList<AlternateContent>();

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

	/**
	 * The block-, row- and cell-level {@code mc:AlternateContent} elements this unmarshal built,
	 * innermost first, as the callback fired; the list is cleared.  The part splices them when the
	 * unmarshal returns ({@link JaxbXmlPart#resolveAlternateContent}; CR-051 section 3.2).  Nothing
	 * is edited in the callback itself, which fires child first while the parent's list is still
	 * filling.
	 *
	 * @since 17.3.4 (CR-051 phase 1)
	 */
	public List<AlternateContent> takeBlockLevelElements() {
		List<AlternateContent> taken = new ArrayList<AlternateContent>(blockLevel);
		blockLevel.clear();
		return taken;
	}

	/**
	 * The {@code mc:AlternateContent} elements, at any level, holding a {@code w:sdt} directly in
	 * a branch, which arrives as a DOM element in {@link McBranchSdt#NAMESPACE}; the list is
	 * cleared.  The part binds them by their container's kind when the unmarshal returns
	 * ({@link McBranchSdt#rebind}; CR-051 section 11.3).
	 *
	 * @since 17.3.4
	 */
	public List<AlternateContent> takeBranchSdtElements() {
		List<AlternateContent> taken = new ArrayList<AlternateContent>(branchSdts);
		branchSdts.clear();
		return taken;
	}

  @Override
  public void afterUnmarshal(Object target, Object parent) {
	  
		if (target instanceof AlternateContent) {
			
			if (McBranchSdt.holdsBranchSdt((AlternateContent) target)) branchSdts.add((AlternateContent) target);
			
			// CR-051 section 3.2, step 1: recorded, for the splice when the unmarshal returns.
			// One directly inside another element's branch is recorded too: whether its
			// container is in scope is settled at the splice (McSplice.effectiveContainer),
			// since here the branch's own parent is not yet set (the callback fires child first).
			Object container = parent != null ? parent : ((AlternateContent) target).getParent();
			if (McSplice.inScope(container)
					|| container instanceof AlternateContent.Choice || container instanceof AlternateContent.Fallback) {
				blockLevel.add((AlternateContent) target);
			}
			
		} else if (target instanceof AlternateContent.Choice) {
			
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
