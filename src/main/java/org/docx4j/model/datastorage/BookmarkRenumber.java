package org.docx4j.model.datastorage;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.bind.JAXBElement;

import org.docx4j.TraversalUtil;
import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.XmlUtils;
import org.docx4j.finders.RangeFinder;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.CTBookmark;
import org.docx4j.wml.CTMarkupRange;
import org.docx4j.wml.CTPerm;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.RangePermissionStart;
import org.docx4j.wml.Text;
import org.jvnet.jaxb2_commons.ppp.Child;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BookmarkRenumber {
	
/*
 * Note that where the input docx contains:
 * 
        <w:bookmarkStart w:id="309" w:name="_Ref353188010" w:displacedByCustomXml="next"/>
        
        <w:sdt>
          <w:sdtPr>
            <w:alias w:val="Data value: lF2HS"/>
            <w:tag w:val="od:repeat=lF2HS"/>
            <w:id w:val="-414473234"/>
          </w:sdtPr>
          <w:sdtContent>
          
            <w:bookmarkEnd w:id="309" w:displacedByCustomXml="prev"/>
            
   A new start element will be created inside the sdtContent.
   
   So later, we may need to sweep through and remove the original bookmarkStart w:id="309",
   since it won't have a matching end tag anymore.
               
            
             */
	
	protected static Logger log = LoggerFactory.getLogger(BookmarkRenumber.class);	
	
	private BookmarkRenumber() {}
	
	BookmarkRenumber(WordprocessingMLPackage wordMLPackage) {
		this.wordMLPackage=wordMLPackage;
	}
	
	private WordprocessingMLPackage wordMLPackage; // so we can calculate bookmark starting ID on demand
	
	private AtomicInteger bookmarkId = null;
	
	protected AtomicInteger getBookmarkId() {
		
		if (bookmarkId==null) {
			// Work out starting ID
			bookmarkId = new AtomicInteger(initBookmarkIdStart());
		}
		return bookmarkId;
	}

	private int initBookmarkIdStart() {

		int highestId = 0;
		
		RangeFinder rt = new RangeFinder("CTBookmark", "CTMarkupRange");
		new TraversalUtil(wordMLPackage.getMainDocumentPart().getContent(), rt);
		
		for (CTBookmark bm : rt.getStarts()) {
			
			BigInteger id = bm.getId();
			if (id!=null && id.intValue()>highestId) {
				highestId = id.intValue();
			}
		}
		return highestId +1;
	}	
	
	
	//             fixRange( blockRange, "CTBookmark", "CTMarkupRange", null);
	
	protected void fixRange(List<Object> paragraphs, String startElement,
			String endElement, String refElement, long global, int instanceNumber) throws Exception {
				
		RangeTraverser rt = new RangeTraverser(startElement, endElement,
				refElement);
		new TraversalUtil(paragraphs, rt);

		Method startIdMethod = null;
		Method endIdMethod = null;
		
		
		
		// Delete unwanted _GoBack bookmark
		if (startElement.equals("CTBookmark")) {
		
			for (CTBookmark bm : rt.deletes) {
				BigInteger unwantedId = bm.getId(); 
				try {
					// Can't just remove the object from the parent,
					// since in the parent, it may be wrapped in a JAXBElement
					List<Object> theList = null;
					if (bm.getParent() instanceof List) {
						theList = (List)bm.getParent(); // eg blockRange.getContents()
					} else {
						theList = ((ContentAccessor)(bm.getParent())).getContent();
					}
					
					Object deleteMe = null;
					for (Object ox : theList) {
						if (XmlUtils.unwrap(ox).equals(bm)) {
							deleteMe = ox;
							break;
						}
					}
					if (deleteMe!=null) {
						theList.remove(deleteMe);						
					}
				} catch (ClassCastException e) {
					log.error(e.getMessage(), e);
				}
				
				// Now delete the closing tag
				// .. find it
				for (Object o : rt.ends) {

					if (endIdMethod == null)
						endIdMethod = findGetIdMethod(o);
					Object id = null;
					try {
						// BigInteger
						id = getId(endIdMethod, o);
						
						if (unwantedId.compareTo((BigInteger)id)==0) {
							// Found it
							try {				
								CTMarkupRange mr = (CTMarkupRange)o;
								List<Object> theList = null;
								if (mr.getParent() instanceof List) {
									theList = (List)mr.getParent(); // eg blockRange.getContents()
								} else {
									theList = ((ContentAccessor)(mr.getParent())).getContent();
								}
								
								Object deleteMe = null;
								for (Object ox : theList) {
									if (XmlUtils.unwrap(ox).equals(mr)) {
										deleteMe = ox;
										break;
									}
								}
								if (deleteMe!=null) {
									theList.remove(deleteMe);
								}
							} catch (ClassCastException e) {
								log.error(e.getMessage(), e);
							} 
							rt.ends.remove(o);
							break;
							
						}

					} catch (ClassCastException cce) {

						// String
						id = getIdString(endIdMethod, o);

						if (unwantedId.toString().equals(id) ) {
							// Found it
							try {				
								CTMarkupRange mr = (CTMarkupRange)o;
								List<Object> theList = null;
								if (mr.getParent() instanceof List) {
									theList = (List)mr.getParent(); // eg blockRange.getContents()
								} else {
									theList = ((ContentAccessor)(mr.getParent())).getContent();
								}
								Object deleteMe = null;
								for (Object ox : theList) {
									if (XmlUtils.unwrap(ox).equals(mr)) {
										deleteMe = ox;
									}
								}
								if (deleteMe!=null) {
									theList.remove(deleteMe);
								}
							} catch (ClassCastException e) {
								log.error(e.getMessage(), e);
							}
							rt.ends.remove(o);
							break;
						}
						
					}
				}
			}
		}
		
		// The below also renumbers bookmarks, so
		// that they are unique across documents.
		// Don't need to worry about refs to bookmarks,
		// since these are by name, not ID. eg
		// <w:instrText xml:space="preserve"> REF  MyBookmark </w:instrText>
		// except that we want those to be unique
		// across documents so prepend doc#_


		// for each opening point tag
		int counter = 0; // for bookmark renumbering
		for (Object o : rt.starts) {
			counter++;
//			long newId = global + counter;  // depending on what global is, these may collide!
			long newId = getBookmarkId().getAndIncrement();
			
			if (startIdMethod == null)
				startIdMethod = findGetIdMethod(o);

			Object id = null;
			boolean matched = false;
			try {
				// BigInteger (eg comment, bookmark)
				id = getId(startIdMethod, o);
				
				if (startElement.equals("CTBookmark")) {
					Method setIdMethod = findSetIdMethod(o);
					if (id instanceof BigInteger) {
						setIdMethod.invoke(o, BigInteger.valueOf(newId));
					} 
//					else if (id instanceof String) {
//						setIdMethod.invoke(o, "" + newId);
//					} 
					
					String oldName = ((CTBookmark)o).getName();
					String newName = oldName + "_" + instanceNumber ; // can't start with a number
					((CTBookmark)o).setName(newName);
					for (Object ref : rt.refs) {
						Text fieldInstr = (Text)ref;
						String fieldVal = fieldInstr.getValue();
						if (fieldVal.contains("REF ")
								&& fieldVal.contains(" " + oldName + " ") ) {
							fieldInstr.setValue(fieldVal.replace(oldName, newName));
						}
					}
				}

				// find the closing point tag
				BigInteger tmpId;
				for (Object end : rt.ends) {
					if (endIdMethod == null)
						endIdMethod = findGetIdMethod(end);
					tmpId = getId(endIdMethod, end);
					if (tmpId!=null
							&& tmpId.equals(id)) {
						// found it
						matched = true;
						
						if (endElement.equals("CTMarkupRange")) {
							Method setIdMethod = findSetIdMethod(end);
							if (id instanceof BigInteger) {
								setIdMethod.invoke(end, BigInteger.valueOf(newId));
							} 
//							else if (id instanceof String) {
//								setIdMethod.invoke(end, "" + newId);
//							} 
						}
						
						break;
					}
				}

			} catch (ClassCastException cce) {
				// String (eg permStart)
				id = getIdString(startIdMethod, o);

//				if (startElement.equals("CTBookmark")) {
//					Method setIdMethod = findSetIdMethod(o);
//					if (id instanceof BigInteger) {
//						setIdMethod.invoke(o, BigInteger.valueOf(newId));
//					} 
////					else if (id instanceof String) {
////						setIdMethod.invoke(o, "" + newId);
////					} 
//				}
				
				// find the closing point tag
				String tmpId;
				for (Object end : rt.ends) {
					if (endIdMethod == null)
						endIdMethod = findGetIdMethod(end);
					tmpId = getIdString(endIdMethod, end);
					if (tmpId!=null
							&& tmpId.equals(id)) {
						// found it
						matched = true;
						
//						if (endElement.equals("CTMarkupRange")) {
//							Method setIdMethod = findSetIdMethod(end);
//							if (id instanceof BigInteger) {
//								setIdMethod.invoke(end, BigInteger.valueOf(newId));
//							} 
////							else if (id instanceof String) {
////								setIdMethod.invoke(end, "" + newId);
////							} 
//						}
						
						break;
					}
				}
			}

			if (!matched) {

				// Object p = paragraphs.get( paragraphs.size() -1 );
				// if (p instanceof P) {
				// ((P)p).getParagraphContent().add(createObject(endElement,
				// id));
				// } else {
				// System.out.println("add a close tag in " +
				// p.getClass().getName() );
				// }

				/*
				 * CommentRangeEnd can be block level; Bookmark End can precede
				 * or follow a w:tbl closing tag.
				 * 
				 * So for now, insert these at block level. I haven't checked
				 * the other range tags.
				 * 
				 * I'm presuming the open tags can be block level as well.
				 */
				if (endElement.equals("CTMarkupRange")) {
					CTMarkupRange mr = Context.getWmlObjectFactory().createCTMarkupRange();
//	                mr.setId((BigInteger)id);
	                mr.setId(BigInteger.valueOf(newId));
	                JAXBElement<CTMarkupRange> bmEnd = Context.getWmlObjectFactory().createBodyBookmarkEnd(mr);				
					paragraphs.add(bmEnd);
				} else 
					if (endElement.equals("CTPerm")) {
						CTPerm mr = Context.getWmlObjectFactory().createCTPerm();
		                mr.setId((String)id);
		                JAXBElement<CTPerm> rEnd = Context.getWmlObjectFactory().createBodyPermEnd(mr);				
						paragraphs.add(rEnd);
				} else {
					paragraphs.add(createObject(endElement, id));
				}
				
				if (refElement != null) {
					// In practice this is always CommentReference,
					// so rely on that
					// if (p instanceof P) {
					// R.CommentReference cr =
					// Context.getWmlObjectFactory().createRCommentReference();
					// cr.setId(id);
					// ((P)p).getParagraphContent().add(cr);
					// // that should be put in a w:r
					// // <w:r><w:rPr><w:rStyle
					// w:val="CommentReference"/></w:rPr><w:commentReference
					// w:id="0"/></w:r>
					// } else {
					// System.out.println(" add a close tag in " +
					// p.getClass().getName() );
					// }

					// <w:r><w:rPr><w:rStyle
					// w:val="CommentReference"/></w:rPr><w:commentReference
					// w:id="0"/></w:r>
					P p = Context.getWmlObjectFactory().createP();
					R r = Context.getWmlObjectFactory().createR();
					p.getParagraphContent().add(r);

					R.CommentReference cr = Context.getWmlObjectFactory()
							.createRCommentReference();
					cr.setId( (BigInteger)id);
					r.getRunContent().add(cr);

					paragraphs.add(p);
				}

			}
		}

		for (Object o : rt.ends) {
			counter++;
			long newId = getBookmarkId().getAndIncrement();
				// only renumber here for ends without starts

			if (endIdMethod == null)
				endIdMethod = findGetIdMethod(o);
			Object id = null;
			boolean matched = false;
			try {
				// BigInteger
				id = getId(endIdMethod, o);

				// find the opening point tag
				BigInteger tmpId;
				for (Object start : rt.starts) {

					if (startIdMethod == null)
						startIdMethod = findGetIdMethod(start);
					tmpId = getId(startIdMethod, start);

					if (tmpId!=null
							&& tmpId.equals(id)) {
						// found it
						matched = true;
						break;
					} 
				}
			} catch (ClassCastException cce) {

				// String
				id = getIdString(endIdMethod, o);

				// find the opening point tag
				String tmpId;
				for (Object start : rt.starts) {

					if (startIdMethod == null)
						startIdMethod = findGetIdMethod(start);
					tmpId = getIdString(startIdMethod, start);

					if (tmpId!=null
							&& tmpId.equals(id)) {
						// found it
						matched = true;
						break;
					}
				}
			}

			if (!matched) {

				if (endElement.equals("CTMarkupRange")) {
					// missing start, so renumber
					Method setIdMethod = findSetIdMethod(o);
					if (id instanceof BigInteger) {
						setIdMethod.invoke(o, BigInteger.valueOf(newId));
					} 						
				}

				/* I'm presuming the open tags can be block level as well. */
				if (endElement.equals("CTPerm")) {
					RangePermissionStart mr = Context.getWmlObjectFactory().createRangePermissionStart();
	                mr.setId((String)id);
	                JAXBElement<RangePermissionStart> rs = 
	                	Context.getWmlObjectFactory().createBodyPermStart(mr);				
					paragraphs.add(rs);
				} else if (startElement.equals("CTBookmark")) {
					log.debug("Add w:bookmarkStart");
					Object newObject = createObject(startElement, newId);
					paragraphs.add(0, newObject);	
					
					if (newObject instanceof CTBookmark) {
						// Word complains if a bookmark doesn't have @w:name
						String newName = global + "_" + "bookmark" + counter;
						((CTBookmark)newObject).setName(newName);
						log.info(".. " + newName);
					}
					
				} else {
					paragraphs.add(0, createObject(startElement, id));
				}
			}
		}
	}
        
        private Method findGetIdMethod(Object o) throws Exception {
        	
        	// Have to do this because getDeclaredMethod
        	// doesn't find inherited methods, and for bookmarks,
        	// its inherited
        	Method[] methods = o.getClass().getMethods();
        	
        	for (int i=0; i<methods.length; i++) {
        		
        		if (methods[i].getName().equals("getId")) {
        			return methods[i];
        		}
        	}
        	log.error("Couldn't find getId for " + o.getClass().getName() );
        	//(new Throwable()).printStackTrace();
        	return null;
        }
        
        private Method findSetIdMethod(Object o) throws Exception {
        	
        	Method[] methods = o.getClass().getMethods();
        	
        	for (int i=0; i<methods.length; i++) {
        		
        		if (methods[i].getName().equals("setId")) {
        			return methods[i];
        		}
        	}
        	log.error("Couldn't find setId for " + o.getClass().getName() );
        	return null;
        }
        
        private BigInteger getId(Method idMethod, Object o) throws Exception {
        	
        	if (idMethod!=null) {
        		return (BigInteger)idMethod.invoke(o);
        	}
        	return null;
        }

        private static String getIdString(Method idMethod, Object o) throws Exception {
        	
        	if (idMethod!=null) {
        		return (String)idMethod.invoke(o);
        	}
        	return null;
        }
                
        private Object createObject(String name, Object id ) throws Exception {
        	
			ObjectFactory factory = Context.getWmlObjectFactory();
			log.debug("Looking for method create" + name);
			Method method = factory.getClass().getDeclaredMethod("create" + name);
			Object newObject = method.invoke(factory);
			
			Method setIdMethod = findSetIdMethod(newObject);
			if (setIdMethod==null) {
				log.error( "Couldn't findSetIdMethod for " + newObject.getClass().getName());				
			} else {
				log.debug( "FOund findSetIdMethod for " + newObject.getClass().getName());								
			}
			
			Class param = setIdMethod.getParameterTypes()[0];
			
			setIdMethod.invoke(newObject, convertObject(id, param));
						
			return newObject;
        	
        }
        
        private Object convertObject(Object id, Class c) throws Docx4JException {
        	
        	if (c.isAssignableFrom(id.getClass())) {
        		return id;
        	}
        	
        	if (c==BigInteger.class) {
        		if (id instanceof Long) {
        			return BigInteger.valueOf( (Long)id);
        		}
        	}
			throw new Docx4JException("TODO: Convert " + id.getClass().getName() + " to "+ c.getName() );
        	
        }
        
        static class RangeTraverser extends CallbackImpl {
			
        	List<Object> starts = new ArrayList<Object>();
        	List<Object> ends   = new ArrayList<Object>();
        	List<Object> refs   = new ArrayList<Object>();
        	List<CTBookmark> deletes   = new ArrayList<CTBookmark>();
        	
        	String startElement; 
        	String endElement; 
        	String refElement;
        	
        	RangeTraverser(String startElement, String endElement, String refElement) {
        		
        		this.startElement = "org.docx4j.wml." + startElement;
        		this.endElement   = "org.docx4j.wml." + endElement;
        		this.refElement   = "org.docx4j.wml." + refElement;        		
        	}

        	@Override
			public List<Object> apply(Object o) {
        		
				if (o.getClass().getName().equals(startElement)) {
					if (o instanceof CTBookmark) { // check for special case
						CTBookmark bookmark = (CTBookmark)o;
						if (bookmark.getName().equals("_GoBack")) {
							deletes.add(bookmark);
						} else {
							starts.add(o);							
						}
					} else {
						starts.add(o);
					}
				}
				
				if (o.getClass().getName().equals(endElement)) {
					ends.add(o);
				}

				if (o.getClass().getName().equals(refElement)) {
					refs.add(o);
				} else if (startElement.equals("org.docx4j.wml.CTBookmark") 
						&& o instanceof javax.xml.bind.JAXBElement
						&& ((JAXBElement)o).getName().getLocalPart().equals("instrText")) {
					refs.add( XmlUtils.unwrap(o) );
				}
				
				return null;
			}
        	
		}
	
//    	public static void main(String[] args) throws Exception {
//			
//    		String input_DOCX = System.getProperty("user.dir") + "/bm_test.docx";
//    		
//    		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage
//    				.load(new java.io.File(input_DOCX));
//    		
//			BookmarkRenumber.fixRange(
//					((SdtElement)repeated.get(i)).getSdtContent().getContent(), 
//					"CTBookmark", "CTMarkupRange", null, i);
//    		
//    	}
}
