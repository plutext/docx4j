/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
 *   
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

package org.docx4j.model.datastorage.migration;


import org.docx4j.XmlUtils;
import org.docx4j.jaxb.McMode;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.utils.SingleTraversalUtilVisitorCallback;
import org.docx4j.utils.TraversalUtilVisitor;
import org.docx4j.wml.Body;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;


/**
 * There are at least 3 approaches for replacing variables in 
 * a docx.
 * 
 * 1. text replacement on the document surface.
 * 2. using Merge Fields (see org.docx4j.model.fields.merge.MailMerger)
 * 3. binding content controls to an XML Part (via XPath)
 * 
 * Approach 3 is the recommended one when using docx4j. See the 
 * ContentControl* examples, Getting Started, and the subforum.
 * 
 * Approach 1, works in simple cases only. 
 * 
 * It won't work if your KEY is split across separate
 * runs in your docx (which often happens), or if you want 
 * to insert images, or multiple rows in a table.
 * 
 * This class tidies up your document, so your keys should
 * not be split across separate runs.
 */
public class VariablePrepare {
	
	private static Logger log = LoggerFactory.getLogger(VariablePrepare.class);	
	
	public static final int FLAG_NONE = 0;
	public static final int FLAG_REMOVE_PROOF_ERRORS = 1;
	public static final int FLAG_REMOVE_CONTENT_CONTROLS = 2;
	public static final int FLAG_REMOVE_RSIDS = 4;
	public static final int FLAG_REMOVE_BOOKMARKS = 8;
	
	/**
	 * @param wmlPackage
	 * @throws Exception
	 */
	public static void prepare(WordprocessingMLPackage wmlPackage) throws Exception {

		prepare(wmlPackage, FLAG_NONE);
	}
	
	/**
	 * @param wmlPackage
	 * @throws Exception
	 * @since 8.1.0
	 */
	public static void prepare(WordprocessingMLPackage wmlPackage, int flags) throws Exception {
	
		// Apply the filter
		WordprocessingMLPackage.FilterSettings filterSettings = new WordprocessingMLPackage.FilterSettings();
		
		if (flags == FLAG_NONE) {
			// do everything
			flags = (FLAG_REMOVE_PROOF_ERRORS |
					FLAG_REMOVE_CONTENT_CONTROLS |
					FLAG_REMOVE_RSIDS |
					FLAG_REMOVE_BOOKMARKS);
		}

		if ((flags & FLAG_REMOVE_PROOF_ERRORS) == FLAG_REMOVE_PROOF_ERRORS) {	
			filterSettings.setRemoveProofErrors(true);		
		}		
		if ((flags & FLAG_REMOVE_CONTENT_CONTROLS) == FLAG_REMOVE_CONTENT_CONTROLS) {	
			filterSettings.setRemoveContentControls(true);			
		}
		if ((flags & FLAG_REMOVE_RSIDS) == FLAG_REMOVE_RSIDS) {	
			filterSettings.setRemoveRsids(true);			
		}
		if ((flags & FLAG_REMOVE_BOOKMARKS) == FLAG_REMOVE_BOOKMARKS) {	
			filterSettings.setRemoveBookmarks(true);			
		}
		wmlPackage.filter(filterSettings);
		// Note the filter is deprecated, since its questionable whether this
		// is important enough to live in WordprocessingMLPackage,
		// and in any case probably should be replaced with a TraversalUtil
		// approach (which wouldn't involve marshal/unmarshall, and 
		// so should be more efficient).
		
        if(log.isDebugEnabled()) {
            log.debug(XmlUtils.marshaltoString(wmlPackage.getMainDocumentPart().getJaxbElement(), true, true));
        }
		
		// Now clean up some more
		org.docx4j.wml.Document wmlDocumentEl = wmlPackage.getMainDocumentPart().getJaxbElement();
		Body body =  wmlDocumentEl.getBody();
						
		SingleTraversalUtilVisitorCallback paragraphVisitor 
			= new SingleTraversalUtilVisitorCallback(
					new TraversalUtilParagraphVisitor());
		// CR-021: ALL - variables are prepared in every branch
		paragraphVisitor.setMcMode(McMode.ALL);
		paragraphVisitor.walkJAXBElements(body);

        if(log.isDebugEnabled()) {
            log.debug(XmlUtils.marshaltoString(wmlPackage.getMainDocumentPart().getJaxbElement(), true, true));
        }
	}
	
    private final static QName _RT_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "t");
    
//	public static void main(String[] args) throws Exception {
//
//		String inputfilepath = System.getProperty("user.dir") + "/absoluteAnchor.docx";
//		WordprocessingMLPackage wmlPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
//		P p = (P)wmlPackage.getMainDocumentPart().getContent().get(2);
//		System.out.println(XmlUtils.marshaltoString(p, true, true));
//		joinupRuns(p);
//		System.out.println(XmlUtils.marshaltoString(p, true, true));
//	}
//	
	
	public static void joinupRuns(P p) {

		List<Object> existingContents = p.getContent();
		List<Object> newContents = new ArrayList<Object>();
		
		R currentR = null;
		String currentRPrString = null;
		
		// First join up runs with same run properties
		for (Object o : existingContents) {
			
			if (o instanceof R) {
				
				if (currentR==null) { // first object, or after something not a run
					currentR=(R)o;
					if (currentR.getRPr()!=null) {
						currentRPrString = XmlUtils.marshaltoString(currentR.getRPr(), true);
					}
					newContents.add(currentR);
				} else {
					RPr other = ((R)o).getRPr();
					
					boolean makeNewRun = true; // unless proven otherwise
					
					if (currentRPrString==null && other==null) makeNewRun=false;
					if (currentRPrString!=null && other!=null) {
						// Simple minded notion of equality
						if ( XmlUtils.marshaltoString(other, true).equals(currentRPrString) )  makeNewRun=false; 
					}
					
					if (makeNewRun) {
						currentR=(R)o;
						if (currentR.getRPr()==null) {
							currentRPrString = null;
						} else {
							currentRPrString = XmlUtils.marshaltoString(currentR.getRPr(), true);
						}
						newContents.add(currentR);
					} else {
						currentR.getContent().addAll( ((R)o).getContent() );
					}
				}
				
			} else {
				// not a run (eg w:ins) .. just add it and move on
				newContents.add(o);
				currentR = null;
				currentRPrString = null;
			}
			
		}
				
		// Now, in each run, join up adjacent text nodes
		for (Object o : newContents) {
			
			if (o instanceof R) {
				
				List<Object> newRunContents = new ArrayList<Object>();	
				JAXBElement currentT = null;
				for ( Object rc : ((R)o).getContent() ) {
					
					if (rc instanceof JAXBElement
							&& ((JAXBElement)rc).getName().equals(_RT_QNAME)) {
						
						if (currentT==null) { // first object, or after something not a w:t
							currentT=(JAXBElement)rc;
							newRunContents.add(currentT);
						} else {
							Text currentText = (Text)XmlUtils.unwrap(currentT);
							String val = currentText.getValue();
							
							currentText.setValue(val + ((Text)XmlUtils.unwrap(rc)).getValue() );								
						}
						
						// <w:t xml:space="preserve">
						if (((Text)XmlUtils.unwrap(rc)).getSpace()!=null
								&& ((Text)XmlUtils.unwrap(rc)).getSpace().equals("preserve")) { // any of them
							((Text)XmlUtils.unwrap(currentT)).setSpace("preserve");
						}
						
					} else {
						log.debug(rc.getClass().getName());
						// not text .. just add it and move on
						newRunContents.add(rc);
						currentT = null;
					}
				
				}
				
				((R)o).getContent().clear();
				((R)o).getContent().addAll(newRunContents);
				
			}
		
		}
		
		joinupPlaceholders(newContents);
		
		// Now replace w:p contents
		p.getContent().clear();
		p.getContent().addAll(newContents);
		
	}

	/**
	 * Joins the runs a ${key} placeholder is split across, whatever their formatting, so variableReplace finds the
	 * key: the text through the closing } goes into the run where ${ begins, in its formatting (the value replaces
	 * it anyway). Only runs holding nothing but text are joined, and only where the } is found; the text after the
	 * }, in the run holding it, keeps its own run. Since 17.3.5 (#572): until then a placeholder whose runs were
	 * formatted differently ("${first_" in 10pt, "name}" in the default size) stayed split, and wasn't replaced.
	 */
	private static void joinupPlaceholders(List<Object> contents) {
		for (int i = 0; i < contents.size(); i++) {
			Text open = lastText(contents.get(i));
			if (open == null || !opensPlaceholder(open.getValue(), contents, i)) continue;
			int close = -1;
			for (int j = i + 1; j < contents.size(); j++) {
				Text t = onlyText(contents.get(j));
				if (t == null) break;
				if (t.getValue().indexOf('}') >= 0) {
					close = j;
					break;
				}
			}
			if (close < 0) continue;
			StringBuilder joined = new StringBuilder(open.getValue());
			for (int j = i + 1; j < close; j++) joined.append(onlyText(contents.get(j)).getValue());
			Text last = onlyText(contents.get(close));
			int brace = last.getValue().indexOf('}');
			joined.append(last.getValue(), 0, brace + 1);
			setText(open, joined.toString());
			String rest = last.getValue().substring(brace + 1);
			if (rest.isEmpty()) {
				contents.remove(close);
			} else {
				setText(last, rest);
			}
			for (int j = close - 1; j > i; j--) contents.remove(j);
		}
	}

	/** Whether the text opens a placeholder its run doesn't close: an unclosed ${, or a $ the next run's { follows */
	private static boolean opensPlaceholder(String text, List<Object> contents, int i) {
		int start = text.lastIndexOf("${");
		if (start >= 0 && text.indexOf('}', start) < 0) return true;
		if (text.endsWith("$") && i + 1 < contents.size()) {
			Text next = onlyText(contents.get(i + 1));
			return next != null && next.getValue().startsWith("{");
		}
		return false;
	}

	/** The run's w:t, where it holds nothing else; null otherwise */
	private static Text onlyText(Object o) {
		if (!(o instanceof R) || ((R)o).getContent().size() != 1) return null;
		return lastText(o);
	}

	/** The run's last w:t, where its content ends with one; null otherwise */
	private static Text lastText(Object o) {
		if (!(o instanceof R) || ((R)o).getContent().isEmpty()) return null;
		List<Object> content = ((R)o).getContent();
		Object last = content.get(content.size() - 1);
		if (last instanceof JAXBElement && ((JAXBElement)last).getName().equals(_RT_QNAME)) {
			return (Text)XmlUtils.unwrap(last);
		}
		return null;
	}

	private static void setText(Text t, String value) {
		t.setValue(value);
		if (value.startsWith(" ") || value.endsWith(" ")) t.setSpace("preserve");
	}

	public static class TraversalUtilParagraphVisitor extends TraversalUtilVisitor<P> {
		
		@Override
		public void apply(P p, Object parent, List<Object> siblings) {
			joinupRuns(p);
		}
	
	}
	
//	public static class MySingleTraversalUtilVisitorCallback extends SingleTraversalUtilVisitorCallback {
//		
//		public MySingleTraversalUtilVisitorCallback(TraversalUtilVisitor visitor) {
//			super(visitor);
//		}
//		
//		@Override
//		public boolean shouldTraverse(Object o) {
//			return !(o instanceof P); 
//		}
//		
//	}



}
