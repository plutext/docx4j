package org.docx4j.model.bookmarks;
import java.io.IOException;
import java.io.Writer;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.finders.RangeFinder;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.CTBookmark;
import org.docx4j.wml.CTMarkupRange;
import org.docx4j.wml.ContentAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Perform certain bookmark integrity checks (ie check for duplicates, missing starts, ends), 
 * and optionally, write fixes in-place
 * @since 6.1
 */
public class BookmarksIntegrity {
		
	protected static Logger log = LoggerFactory.getLogger(BookmarksIntegrity.class);
	
	
	public BookmarksIntegrity() {}

	private Writer writer;
	
	/**
	 * To record (human readable) results
	 * @param writer
	 */
	public void setWriter(Writer writer) {
		this.writer = writer;
	}

	/**
	 * @param documentPart
	 * @param remediate
	 * @return
	 * @throws Exception
	 */
	public BookmarksStatus check(MainDocumentPart documentPart, boolean remediate) throws Exception {

		return check(documentPart.getContent(), remediate);
	}
	
	/**
	 * @param paragraphs
	 * @param remediate
	 * @return
	 * @throws IOException 
	 * @throws Exception
	 */
	public BookmarksStatus check(List<Object> contents, boolean remediate) throws IOException {
				
		// Before..
		// write(XmlUtils.marshaltoString(documentPart.getJaxbElement(), true, true));

		List<Object> faulty = inspectBookmarks(contents);
		
		if (remediate) {
			
			for (Object o : faulty) {
				
				if (o instanceof CTBookmark) {
					CTBookmark start = (CTBookmark)o;
					Object parent = start.getParent();
					if (parent instanceof ContentAccessor) {
//						write("attempting to remove " + start.getClass().getName());
						if (remove( ((ContentAccessor)parent).getContent(), o)) {
							// ok
						} else {
							// shouldn't happen
							write("FIXME Couldn't find start " + start.getName() + start.getId().intValue() );
							log.warn("FIXME Couldn't find start " + start.getName() + start.getId().intValue() );
						}
					} else {
						log.warn("TODO: handle parent:" + parent.getClass().getName());
						write("TODO: handle parent:" + parent.getClass().getName());
					}
				}

				if (o instanceof CTMarkupRange /* ends */
						&& (!(o instanceof CTBookmark) /* exclude starts - note inheritance hierarchy */ )) {
					CTMarkupRange end = (CTMarkupRange)o;
					Object parent = end.getParent();
					if (parent instanceof ContentAccessor) {
						if (remove( ((ContentAccessor)parent).getContent(), o)) {
							
						} else {
							// shouldn't happen
							write("FIXME Couldn't find end " + end.getId().longValue() );
							log.warn("FIXME Couldn't find end " + end.getId().longValue() );
						}
					} else {
						log.warn("TODO: handle parent:" + parent.getClass().getName());
						write("TODO: handle parent:" + parent.getClass().getName());
					}
				}
				
			}
		}
		
		if (faulty.size()==0) {
			log.debug("Nothing to fix");
			write("Nothing to fix");
			return BookmarksStatus.OK;
		} else if (remediate ){
			return BookmarksStatus.FIXED;				
		} else {
			return BookmarksStatus.BROKEN;
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

	private  List<Object> inspectBookmarks(List<Object> paragraphs) throws IOException {
		
		Set<String> names = new HashSet<String>();
		Set<BigInteger> startIds = new HashSet<BigInteger>();
		Set<BigInteger> endIds = new HashSet<BigInteger>();
		
		List<Object> faulty = new ArrayList<Object>(); 

		RangeFinder rt = new RangeFinder("CTBookmark", "CTMarkupRange");
		new TraversalUtil(paragraphs, rt);

		write("Checking starts " );
		
		for (CTBookmark bm : rt.getStarts()) {
			
			BigInteger id = bm.getId();
			String name = bm.getName();
			
			if (name==null && id == null) {
				write("Name and ID missing!");
				faulty.add(bm);
				
			} else if (name!=null && id != null) {
				
				if (!names.add(name)) {
					write("Already have name '" + name + "'");
					faulty.add(bm);
				}
				if (!startIds.add(id)) {
					write("Already have id " + id.longValue());
					if (!faulty.contains(bm) /* don't add it twice */ ) {
						faulty.add(bm);
					}
				}
				
			} else if (name==null)  {
				write("Name missing for id " + id.longValue());
				if (!startIds.add(id)) {
					write(".. and already have id " + id.longValue());
					faulty.add(bm);
				}
				
			} else if (id==null)  {
				write("ID missing for name " + name);
				if (!names.add(name)) {
					write(".. and already have name " + name);
					faulty.add(bm);
				}
				
			}			
		}
		
		write("Checking ends " );
		
		for (CTMarkupRange bm : rt.getEnds()) {
			
			BigInteger id = bm.getId();
			
			if (id == null) {
				write("ID missing!");
				faulty.add(bm);
				
			} else if (id != null) {
				
				if (!endIds.add(id)) {
					write("Already have " + id.longValue());
					faulty.add(bm);
				}
				
			}			
		}
		

		write("Matching ends" );
		for (BigInteger i : startIds) {
			
			if (!endIds.contains(i)) {
				write("  Missing end for start " + i.longValue());	
				faulty.add(find(rt.getStarts(), i)); // so remove the corresponding start
			}
		}
		
		write("Matching starts" );
		for (BigInteger i : endIds) {
			
			if (!startIds.contains(i)) {
				write("  Missing start for end " + i.longValue());				
				faulty.add(findEnds(rt.getEnds(), i)); // so remove the corresponding end
			}
		}
		
		write("Total faulty objects: " + faulty.size());				
		
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

	private CTMarkupRange findEnds(List<CTMarkupRange> ends, BigInteger id) {
		
		for (CTMarkupRange bm : ends) {
			if (bm.getId()==id) {
				return bm;
			}
		}
		return null; //shouldn't happen
	}

	private void write(String s) throws IOException {
		if (this.writer!=null) writer.write(s + "\n");
	}
	
	public enum  BookmarksStatus {
		FIXED, OK, BROKEN
	}
	
}
