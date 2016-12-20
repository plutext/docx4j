package org.docx4j.samples;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
 * Perform certain bookmark integrity checks. 
 */
public class BookmarksDuplicateCheck {
		
//	protected static Logger log = LoggerFactory.getLogger(BookmarksDuplicateCheck.class);
	
	
	private static org.docx4j.wml.ObjectFactory factory = Context.getWmlObjectFactory();
	

	public static void main(String[] args) throws Exception {
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage
				.load(new java.io.File(System.getProperty("user.dir")
						+ "/Estructura.docx"));
		MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
		
		// Before..
		// System.out.println(XmlUtils.marshaltoString(documentPart.getJaxbElement(), true, true));

		org.docx4j.wml.Document wmlDocumentEl = (org.docx4j.wml.Document) documentPart
				.getJaxbElement();
		Body body = wmlDocumentEl.getBody();
		
		BookmarksDuplicateCheck bti = new BookmarksDuplicateCheck();

		bti.indexBookmarks(body.getContent());
		
		// After
		// System.out.println(XmlUtils.marshaltoString(documentPart.getJaxbElement(), true, true));
		
		// save the docx...
		wordMLPackage.save(new java.io.File(System.getProperty("user.dir") + "/OUT_BookmarksTextInserter.docx"));
	}

	private  void indexBookmarks(List<Object> paragraphs) throws Exception {
		
		Set<String> names = new HashSet<String>();
		Set<BigInteger> ids = new HashSet<BigInteger>();

		RangeFinder rt = new RangeFinder("CTBookmark", "CTMarkupRange");
		new TraversalUtil(paragraphs, rt);
		
		for (CTBookmark bm : rt.getStarts()) {
			
			BigInteger id = bm.getId();
			String name = bm.getName();
			
			if (name==null && id == null) {
				System.out.println("Name and ID missing!");
			} else if (name!=null && id != null) {
				
				if (!names.add(name)) {
					System.out.println("Already have " + name);
				}
				if (!ids.add(id)) {
					System.out.println("Already have " + id.longValue());
				}
				
			} else if (name==null)  {
				System.out.println("Name missing for id " + id.longValue());
				if (!ids.add(id)) {
					System.out.println(".. and already have " + id.longValue());
				}
				
			} else if (id==null)  {
				System.out.println("ID missing for name " + name);
				if (!names.add(name)) {
					System.out.println(".. and already have " + name);
				}
				
			}
			
		}

		
	}

	
}
