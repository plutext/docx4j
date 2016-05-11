/*
   Licensed to Plutext Pty Ltd under one or more contributor license agreements.  
   
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License. 

    You may obtain a copy of the License at 

        http://www.apache.org/licenses/LICENSE-2.0 

    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS, 
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
    See the License for the specific language governing permissions and 
    limitations under the License.

 */
package org.docx4j.convert.out.common.preprocess;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.utils.AbstractTraversalUtilVisitorCallback;
import org.docx4j.wml.CTBookmark;
import org.docx4j.wml.CTMarkupRange;
import org.docx4j.wml.P;


/** This class moves w:bookmarkStart into the next paragraph if they 
 *  are outside of a paragraph. This is done so that the BoomarkStartModelWriter
 *  are able to generate a correct output. 
 *  If w:bookmarkStart is moved, then a corresponding w:bookmarkEnd is written after
 *  it and it's w:bookmarkEnd deleted (as the conversion ignores any bookmarkEnd, it doesn't
 *  matter where they are).
 * 
 */
public class BookmarkMover {
	
	protected static Logger log = LoggerFactory.getLogger(BookmarkMover.class);	
	
	
	/** Move bookmarks into a paragraph
	 * 
	 */
	public static void process(WordprocessingMLPackage wmlPackage) {
		TraversalUtil.visit(wmlPackage, false, new BookmarkMoverVisitor());
	}
	
	protected static class BookmarkMoverVisitor extends AbstractTraversalUtilVisitorCallback {
		
		protected static final QName QNAME_BOOKMARK_START = new QName(Namespaces.NS_WORD12, "bookmarkStart");
		protected static final QName QNAME_BOOKMARK_END = new QName(Namespaces.NS_WORD12, "bookmarkEnd");
		protected List<Object> bookmarksStartToMove = new ArrayList<Object>();
		protected Set<BigInteger> bookmarksEndToRemove = new TreeSet<BigInteger>();
		protected P currentParagraph = null;

		@Override
		public void walkJAXBElements(Object parent) {
		List<Object> srcChildren = getChildren(XmlUtils.unwrap(parent));
		List<Object> destChildren = null;
		Object child = null;
			if ((srcChildren != null) && (!srcChildren.isEmpty())) {
				destChildren = new ArrayList<Object>(srcChildren.size());
				for (int c=0; c<srcChildren.size(); c++) {
					child = srcChildren.get(c);
					if (isBookmarkStart(child)) {
						if (currentParagraph == null) {
							appendBookmarksToMove(child);
						}
						else {
							destChildren.add(child);
						}
					}
					else if (isBookmarkEnd(child)) {
						if (!removeBookmarkEnd(child)) {
							destChildren.add(child);
						}
					}
					else {
						destChildren.add(child);
						if (isParagraph(child)) {
							try {
								currentParagraph = (P)child;
								walkJAXBElements(child);
								moveBookmarks(child);
							}
							finally {
								currentParagraph = null;
							}
						}
						else {
							walkJAXBElements(child);
						}
					}
				}
				if ((destChildren.size() != srcChildren.size())) {
					srcChildren.clear();
					srcChildren.addAll(destChildren);
				}
			}
		}

		private boolean isBookmarkStart(Object child) {
			return (child instanceof JAXBElement
					&& ((JAXBElement)child).getName().equals(QNAME_BOOKMARK_START));
		}

		private void appendBookmarksToMove(Object child) {
			
			CTBookmark bm = (CTBookmark)XmlUtils.unwrap(child);
			JAXBElement<CTMarkupRange> jaxbBmEnd = null;
			CTMarkupRange bmEnd = null;
			bookmarksStartToMove.add(child);
		
			//The bookmarkEnd is put directly after a moved bookmarkStart in the paragraph.
			//the corresponding bookmarkEnd is later deleted 
			//(this ensures that the bookmarkEnd isn't before the bookmarkStart  
			bmEnd = Context.getWmlObjectFactory().createCTMarkupRange();
			bmEnd.setId(bm.getId());
			jaxbBmEnd = Context.getWmlObjectFactory().createPBookmarkEnd(bmEnd);
			bookmarksStartToMove.add(jaxbBmEnd);
			
			bookmarksEndToRemove.add(bm.getId());
		}

		private boolean isBookmarkEnd(Object child) {
			return (child instanceof JAXBElement
					&& ((JAXBElement)child).getName().equals(QNAME_BOOKMARK_END));
		}

		private boolean removeBookmarkEnd(Object child) {
			CTMarkupRange bmEnd = (CTMarkupRange)XmlUtils.unwrap(child);
			if (bmEnd.getId()==null) {
				// malformed -> remove it
				return true;
			} else {
				return bookmarksEndToRemove.remove(bmEnd.getId());
			}
		}

		private boolean isParagraph(Object child) {
			return (child instanceof P);
		}

		private void moveBookmarks(Object child) {
		P p = null;
		List<Object> content = null;
			if (!bookmarksStartToMove.isEmpty()) {
				p = (P)XmlUtils.unwrap(child);
				content = p.getContent();
				content.addAll(0, bookmarksStartToMove);
				bookmarksStartToMove.clear();
			}
		}

		@Override
		protected List<Object> apply(Object child, Object parent, List children) {
			// noop apply would be called from the overwritten walkJAXBElements,
			// it isn't called in this subclass.
			return null;
		}
		
	}
	
}
