package org.docx4j.samples;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.finders.RangeFinder;
import org.docx4j.jaxb.Context;
import org.docx4j.model.fields.merge.DataFieldName;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Body;
import org.docx4j.wml.CTBookmark;
import org.docx4j.wml.CTMarkupRange;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Replace text between w:bookmarkStarts and corresponding w:bookmarkEnds
 * with specified data, matching on bookmark's @w:name 
 *
 */
public class BookmarksReplaceWithText {
	
	/*
	 * Requirements:
	 * - bookmarkStart and End must be in the same paragraph
	 * - no attempt is made to check whether the start of some other bookmark is
	 *   in that range.  If it is, it will get deleted!
	 * - no attempt is made to preserve the rPr
	 * - mdp only right now
	 */
	
	protected static Logger log = LoggerFactory.getLogger(BookmarksReplaceWithText.class);
	
	private static boolean DELETE_BOOKMARK = false;
	
	private static org.docx4j.wml.ObjectFactory factory = Context.getWmlObjectFactory();
	

	public static void main(String[] args) throws Exception {
		
		Map<DataFieldName, String> map = new HashMap<DataFieldName, String>();
		map.put( new DataFieldName("bm1"), "whale shark");
		map.put( new DataFieldName("bm2"), "dolphin");
		

		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage
				.load(new java.io.File(System.getProperty("user.dir")
						+ "/bm1.docx"));
		MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
		
		// Before..
		// System.out.println(XmlUtils.marshaltoString(documentPart.getJaxbElement(), true, true));

		org.docx4j.wml.Document wmlDocumentEl = (org.docx4j.wml.Document) documentPart
				.getJaxbElement();
		Body body = wmlDocumentEl.getBody();
		
		BookmarksReplaceWithText bti = new BookmarksReplaceWithText();

		bti.replaceBookmarkContents(body.getContent(), map);
		
		// After
		//System.out.println(XmlUtils.marshaltoString(documentPart.getJaxbElement(), true, true));
		
		// save the docx...
		wordMLPackage.save(new java.io.File(System.getProperty("user.dir") + "/OUT_BookmarksTextInserter.docx"));
	}

	private  void replaceBookmarkContents(List<Object> paragraphs,  Map<DataFieldName, String> data) throws Exception {

		RangeFinder rt = new RangeFinder("CTBookmark", "CTMarkupRange");
		new TraversalUtil(paragraphs, rt);
		
		for (CTBookmark bm : rt.getStarts()) {
			
			// do we have data for this one?
			if (bm.getName()==null) continue;
			String value = data.get(new DataFieldName(bm.getName()));
			if (value==null) continue;
			System.out.println(bm.getName() + ", " + value);
				
			try {
				// Can't just remove the object from the parent,
				// since in the parent, it may be wrapped in a JAXBElement
				List<Object> theList = null;
				if (bm.getParent() instanceof P) {
					theList = ((ContentAccessor)(bm.getParent())).getContent();
				} else {
					continue;
				}

				int rangeStart = -1;
				int rangeEnd=-1;
				int i = 0;
				for (Object ox : theList) {
					Object listEntry = XmlUtils.unwrap(ox); 
					if (listEntry.equals(bm)) {
						if (DELETE_BOOKMARK) {
							rangeStart=i;
						} else {
							rangeStart=i+1;							
						}
					} else if (listEntry instanceof  CTMarkupRange) {
						if ( ((CTMarkupRange)listEntry).getId().equals(bm.getId())) {
							if (DELETE_BOOKMARK) {
								rangeEnd=i;
							} else {
								rangeEnd= i > rangeStart ? i-1 : i;	 // handle empty bookmark case						
							}
							break;
						}
					}
					i++;
				}
				System.out.println(rangeStart + ", " + rangeEnd);
				
				if (rangeStart>0 && rangeEnd>=rangeStart) {
					
					// Delete the bookmark range
					if (rangeEnd>rangeStart) {
						for (int j =rangeEnd; j>=rangeStart; j--) {
							theList.remove(j);
						}
					}
					
					// now add a run
					org.docx4j.wml.R  run = factory.createR();
					org.docx4j.wml.Text  t = factory.createText();
					run.getContent().add(t);		
					t.setValue(value);
					
					theList.add(rangeStart, run);
				}
				
			} catch (ClassCastException cce) {
				log.error(cce.getMessage(), cce);
			}
		}

		
	}

	
}
