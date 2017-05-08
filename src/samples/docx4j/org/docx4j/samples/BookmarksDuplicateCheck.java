package org.docx4j.samples;
import java.math.BigInteger;
import java.util.ArrayList;
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
 * Perform certain bookmark integrity checks, and optionally, write a fixed output docx 
 */
public class BookmarksDuplicateCheck {
		
//	protected static Logger log = LoggerFactory.getLogger(BookmarksDuplicateCheck.class);
	
	/**
	 * Whether to attempt 
	 */
	private static boolean remediate = true;
	
	private static org.docx4j.wml.ObjectFactory factory = Context.getWmlObjectFactory();
	

	public static void main(String[] args) throws Exception {
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage
				.load(new java.io.File(System.getProperty("user.dir")
						+ "/your.docx"));
		MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
		
		// Before..
		// System.out.println(XmlUtils.marshaltoString(documentPart.getJaxbElement(), true, true));

		org.docx4j.wml.Document wmlDocumentEl = (org.docx4j.wml.Document) documentPart
				.getJaxbElement();
		Body body = wmlDocumentEl.getBody();
		
		BookmarksDuplicateCheck bti = new BookmarksDuplicateCheck();

		List<Object> faulty = bti.inspectBookmarks(body.getContent());
		
		if (remediate) {
			
			for (Object o : faulty) {
				
				if (o instanceof CTBookmark) {
					CTBookmark start = (CTBookmark)o;
					Object parent = start.getParent();
					if (parent instanceof ContentAccessor) {
						if (remove( ((ContentAccessor)parent).getContent(), o)) {
							
						} else {
							System.out.println("Couldn't find start " + start.getName() );
						}
					} else {
						System.out.println("TODO: handle parent:" + parent.getClass().getName());
					}
				}

				if (o instanceof CTMarkupRange /* ends */
						&& (!(o instanceof CTBookmark) /* exclude starts - note inheritance hierarchy */ )) {
					CTMarkupRange end = (CTMarkupRange)o;
					Object parent = end.getParent();
					if (parent instanceof ContentAccessor) {
						if (remove( ((ContentAccessor)parent).getContent(), o)) {
							
						} else {
							System.out.println("Couldn't find end " + end.getId().longValue() );
						}
					} else {
						System.out.println("TODO: handle parent:" + parent.getClass().getName());
					}
				}
				
			}
			
			if (faulty.size()==0) {
				System.out.println("Nothing to fix");
			} else {
				// System.out.println(XmlUtils.marshaltoString(documentPart.getJaxbElement(), true, true));
				wordMLPackage.save(new java.io.File(System.getProperty("user.dir") + "/OUT_BookmarksRemediated.docx"));
				
			}
			
		}
		
	}
	
	private static boolean remove(List list, Object deletion) {
		
		int i = getIndex(list, deletion);
		if (i>=0) {
			Object o = list.remove(i);
			return (o!=null);
		}
		return false;
	}
	
	private static int getIndex(List list, Object deletion) {

		int i = 0;
		for (Object o : list) {
			
			if (o==deletion
					|| XmlUtils.unwrap(o)==deletion) {
				return i;
			}	
			i++;
		}
		return -1;
	}

	private  List<Object> inspectBookmarks(List<Object> paragraphs) throws Exception {
		
		Set<String> names = new HashSet<String>();
		Set<BigInteger> startIds = new HashSet<BigInteger>();
		Set<BigInteger> endIds = new HashSet<BigInteger>();
		
		List<Object> faulty = new ArrayList<Object>(); 

		RangeFinder rt = new RangeFinder("CTBookmark", "CTMarkupRange");
		new TraversalUtil(paragraphs, rt);

		System.out.println("Checking starts " );
		
		for (CTBookmark bm : rt.getStarts()) {
			
			BigInteger id = bm.getId();
			String name = bm.getName();
			
			if (name==null && id == null) {
				System.out.println("Name and ID missing!");
				faulty.add(bm);
				
			} else if (name!=null && id != null) {
				
				if (!names.add(name)) {
					System.out.println("Already have " + name);
					faulty.add(bm);
				}
				if (!startIds.add(id)) {
					System.out.println("Already have " + id.longValue());
					faulty.add(bm);
				}
				
			} else if (name==null)  {
				System.out.println("Name missing for id " + id.longValue());
				if (!startIds.add(id)) {
					System.out.println(".. and already have " + id.longValue());
					faulty.add(bm);
				}
				
			} else if (id==null)  {
				System.out.println("ID missing for name " + name);
				if (!names.add(name)) {
					System.out.println(".. and already have " + name);
					faulty.add(bm);
				}
				
			}			
		}
		
		System.out.println("Checking ends " );
		
		for (CTMarkupRange bm : rt.getEnds()) {
			
			BigInteger id = bm.getId();
			
			if (id == null) {
				System.out.println("ID missing!");
				faulty.add(bm);
				
			} else if (id != null) {
				
				if (!endIds.add(id)) {
					System.out.println("Already have " + id.longValue());
					faulty.add(bm);
				}
				
			}			
		}
		

		System.out.println("Matching ends" );
		for (BigInteger i : startIds) {
			
			if (!endIds.contains(i)) {
				System.out.println("  Missing end for start " + i.longValue());	
				faulty.add(find(rt.getStarts(), i)); // so remove the corresponding start
			}
		}
		
		System.out.println("Matching starts" );
		for (BigInteger i : endIds) {
			
			if (!startIds.contains(i)) {
				System.out.println("  Missing start for end " + i.longValue());				
				faulty.add(find(rt.getEnds(), i)); // so remove the corresponding end
			}
		}
		
		System.out.println("Total faulty objects: " + faulty.size());				
		
		return faulty;
		
	}
	
	private CTBookmark find(List<CTBookmark> starts, BigInteger id) {
		
		for (CTBookmark bm : starts) {
			if (bm.getId()==id) {
				return bm;
			}
		}
		return null; //shouldn't happen
	}

	private CTMarkupRange find(List<CTMarkupRange> ends, BigInteger id) {
		
		for (CTMarkupRange bm : ends) {
			if (bm.getId()==id) {
				return bm;
			}
		}
		return null; //shouldn't happen
	}
	
}
