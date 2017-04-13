package org.docx4j.convert.out.html;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.docx4j.TraversalUtil;
import org.docx4j.finders.SdtFinder;
import org.docx4j.finders.TcFinder;
import org.docx4j.model.PropertyResolver;
import org.docx4j.model.listnumbering.AbstractListNumberingDefinition;
import org.docx4j.model.listnumbering.ListLevel;
import org.docx4j.model.listnumbering.ListNumberingDefinition;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase.NumPr;
import org.docx4j.wml.SdtBlock;
import org.docx4j.wml.SdtContentBlock;
import org.docx4j.wml.SdtElement;
import org.docx4j.wml.SdtPr;
import org.docx4j.wml.Tag;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create list items in OL or UL (as appropriate).
 * 
 * We can't just use a LinkedList (stack) of list contexts,
 * which we push and pop, since we have to write complete
 * XML elements (as opposed to opening and closing tags).
 * 
 * So this means either extending org.docx4j.model.structure.jaxb
 * beyond sections, or some other approach, like wrapping 
 * list items in a content control.  Let's try that.
 * 
 * That's like org.docx4j.convert.out.common.preprocess.Containerization
 * 
 * So we have a 2 step process:
 * 
 * 1.  insert the content controls
 * 
 * 2.  use an SdtWriter to turn these into UL or OL.
 * 
 * This class does step 1.  
 * 
 * Step 2 is implemented by SdtToListSdtTagHandler;  it will only be used if you invoke
 * SdtWriter.registerTagHandler("HTML_ELEMENT", new SdtToListSdtTagHandler())
 * 
 * @author jharrop
 *
 */
public class ListsToContentControls {
	
	public static Logger log = LoggerFactory.getLogger(ListsToContentControls.class);		
	
	public ListsToContentControls(WordprocessingMLPackage wmlPackage) {
		this.wmlPackage = wmlPackage;
		mainDocument = wmlPackage.getMainDocumentPart();
		this.ndp=mainDocument.getNumberingDefinitionsPart();
		stylesPart = wmlPackage.getMainDocumentPart().getStyleDefinitionsPart();
		
		propertyResolver = wmlPackage.getMainDocumentPart().getPropertyResolver();
	}
	
	private WordprocessingMLPackage wmlPackage;
	private MainDocumentPart mainDocument;
	private NumberingDefinitionsPart ndp;
	private StyleDefinitionsPart stylesPart;
	
	private PropertyResolver propertyResolver;
	
    private LinkedList<ListSpec> listStack = null;

	public static class ListSpec {
		
		ListSpec(BigInteger numId, BigInteger ilvl) {
			this.numId = numId;
			this.ilvl = ilvl;
		}
		
		BigInteger ilvl;
		BigInteger numId;
		
		SdtBlock sdtList = null;
		
		
	}
	
	public static void process(WordprocessingMLPackage wmlPackage) {
		//TODO: Convert to visitor behaviour here like TraversalUtil.visit with onlyBody = false
		
		ListsToContentControls lc = new ListsToContentControls(wmlPackage);
		
		if (lc.ndp==null) {
			log.info("No NumberingDefinitionsPart, skipping");
			return;
		}
		lc.process();
		
//		try {
//			wmlPackage.save(new File("cc.docx"));
//		} catch (Docx4JException e) {
//			e.printStackTrace();
//		}
	}
	
	private void process() {
		List<Object> content = null;
		List<Object> groupedContent = null;
		
		///////////////////////////////////////////////
		// First, contents of existing content controls
		// .. find the content controls
		SdtFinder sdtFinder = new SdtFinder();
		new TraversalUtil(mainDocument.getContent(), sdtFinder);
		
		// .. loop through them
		for (SdtElement sdtEl : sdtFinder.getSdtList()) {
			content = sdtEl.getSdtContent().getContent();
			groupedContent = groupContent(content);
			
			if (groupedContent != null) {
				content.clear();
				content.addAll(groupedContent);
			}
			
		}
		
		
		///////////////////////////////////////////////
		// Second, contents of table cells
		TcFinder tcFinder = new TcFinder();
		tcFinder.setTraverseTables(true);
		new TraversalUtil(mainDocument.getContent(), tcFinder);
		for (Tc tc : tcFinder.tcList) {
			
			content = tc.getContent();
			groupedContent = groupContent(content);
			
			if (groupedContent != null) {
				content.clear();
				content.addAll(groupedContent);
			}
			
		}
		
		
		///////////////////////////////////////////////
		// Third, body level content
		content = mainDocument.getContent();
		groupedContent = groupContent(content);
		
		if (groupedContent != null) {
			content.clear();
			content.addAll(groupedContent);
		}
	}
	
	private void closeAllLists() {
		listStack.clear();
	}
	
	private void setTag(SdtBlock sdtList, BigInteger numId, BigInteger ilvl) {
		
		SdtPr sdtPr = new SdtPr();
		Tag tag = new Tag();
		sdtPr.setTag(tag);
		
		sdtList.setSdtPr(sdtPr);
		
		// Bullets = UL.  Work it out.
		ListNumberingDefinition lnd = ndp.getInstanceListDefinitions().get(numId.toString());
		if (lnd==null) {
			// Default to UL
			log.warn("Couldn't find instance list for numId " + numId);
			tag.setVal("HTML_ELEMENT=OL");
			return;
		}
		
		AbstractListNumberingDefinition ald = lnd.getAbstractListDefinition();
		if (ald==null) {
			// Default to UL
			log.warn("Couldn't find abstract list for instance list " + numId);
			tag.setVal("HTML_ELEMENT=OL");
			return;
		}
		
		ListLevel level = ald.getListLevels().get(ilvl.toString());
		if (level==null) {
			// Default to UL
			log.warn("Couldn't find level " + ilvl.toString() + " in instance list ");
			tag.setVal("HTML_ELEMENT=OL");
			return;			
		}
		
		if (level.IsBullet()) {
			tag.setVal("HTML_ELEMENT=UL");			
		} else {
			tag.setVal("HTML_ELEMENT=OL");						
		}
		
	}
	
	private List<Object> groupContent(List<Object> bodyElts) {
		
		// Reset state
		listStack = new LinkedList<ListSpec>();
		
		List<Object> resultElts = new ArrayList<Object>();
		P paragraph = null;
		
		for (Object o : bodyElts) {
			
			Object unwrapped;
			if (o instanceof JAXBElement) {
				unwrapped = ((JAXBElement)o).getValue();
			} else {
				unwrapped=o;
			}

			/*
			 * We can nest lists, but any time a bare table 
			 * or paragraph etc is encountered (ie anything not a list item),
			 * we'll finish the lists.
			 */
					
			if (unwrapped instanceof P) {
				
				paragraph = (P)unwrapped;				
				PPr ppr = propertyResolver.getEffectivePPr(paragraph.getPPr());
				
				NumPr numPr = ppr.getNumPr();
				
				if (numPr==null) {
					closeAllLists();
					resultElts.add(unwrapped);
					continue;
				}
				
				/* It is numbered.
				 * 
				 * Cases:
				 * 
				 * - no current list
				 * 
				 * - same list, same level
				 * 
				 * - same list, different level
				 * 
				 * - different list
				 * 
				 * 
				 * If a list item uses the same list but is a different
				 * level, we'll push/pop levels as appropriate.
				 * 
				 * This implies that when we start, we'll push levels
				 * to get to the right starting level.
				 * 
				 * If its a different list, we'll pop all levels, and
				 * start again.
				 * 
				 * TODO: consider what styling to attach to the OL|UL.
				 * We should match the ImportXHTML behaviour.
				 * 
				 */
				
				BigInteger numId = numPr.getNumId().getVal();
				
				BigInteger ilvl = null;
				if (numPr.getIlvl()==null) {
					ilvl = BigInteger.ZERO;
				} else {
					ilvl = numPr.getIlvl().getVal();
				}
				log.debug("ilvl: " + ilvl.intValue());
				
				ListSpec listSpec = listStack.peek();
				if (listSpec==null
						|| (numId!=null
								&& !numId.equals(listSpec.numId))) {
					// new or different list
					log.debug("NEW LIST");
					
					// if its a different list, pop all levels
					if (listSpec!=null) {
						closeAllLists();						
					}
					
					// add appropriate levels
					for (int i=0; i<=ilvl.intValue(); i++) {
						
						log.debug("adding level " + i);
						
						listSpec = new ListSpec(numId, BigInteger.valueOf(i));
						listSpec.sdtList = new SdtBlock();
						setTag(listSpec.sdtList, numId, ilvl);			

						listSpec.sdtList.setSdtContent(new SdtContentBlock());
						
						if (listStack.peek()==null) {
							resultElts.add(listSpec.sdtList);
						} else {
							listStack.peek().sdtList.getSdtContent().getContent().add(listSpec.sdtList);
						}
						listStack.push(listSpec);
					}
					
					listSpec.sdtList.getSdtContent().getContent().add(paragraph);
				} else if (numId==null) {
					log.error("TODO: encountered null numId!");
					closeAllLists();
					resultElts.add(unwrapped);
					continue;	
				} else // (numId.equals(listSpec.numId)) 
				{
					// same list
					log.debug("listSpec.ilvl.intValue():" + listSpec.ilvl.intValue());
					
					if (ilvl.equals(listSpec.ilvl)) {
						// just add to it
						log.debug("same level");
					} else if (ilvl.compareTo(listSpec.ilvl)>0) {

						// deeper, so add levels
						for (int i=listSpec.ilvl.intValue()+1; i<=ilvl.intValue(); i++) {
							
							log.debug("adding level " + i);
							
							listSpec = new ListSpec(numId, BigInteger.valueOf(i));
							listSpec.sdtList = new SdtBlock();
							setTag(listSpec.sdtList, numId, ilvl);			
							
							listSpec.sdtList.setSdtContent(new SdtContentBlock());
							
							if (listStack.peek()==null) {
								resultElts.add(listSpec.sdtList);
							} else {
								listStack.peek().sdtList.getSdtContent().getContent().add(listSpec.sdtList);
							}
							listStack.push(listSpec);
						}
						
					} else {
						log.debug("must be pop...");
						// shallower, so pop levels
						for (int i=listSpec.ilvl.intValue(); i>ilvl.intValue(); i--) {
							log.debug("popping");
							listStack.pop();
							listSpec = listStack.peek();
							log.debug("popped!");
						}
						
					}
					listSpec.sdtList.getSdtContent().getContent().add(paragraph);										
				} 
				
				
			} else if (unwrapped instanceof Tbl) {
				closeAllLists();
				resultElts.add(unwrapped);
				
			} else {
				log.warn("TODO: handle " + unwrapped.getClass().getName());
				closeAllLists();
				resultElts.add(o);
			}
			
		}
		return resultElts;
	}	
	
		

}
