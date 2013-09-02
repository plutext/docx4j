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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.utils.TraversalUtilVisitor;
import org.docx4j.wml.CTSimpleField;
import org.docx4j.wml.FldChar;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.STFldCharType;
import org.docx4j.wml.Text;

/** This class is something like the opposite to the FieldsPreprocessor. It will 
 *  combine complex fields to simple fields. If there are nested fields, then it 
 *  will try to combine the inner fields without touching the outer ones. 
 * 
 */
public class FieldsCombiner {
	
	private static Logger log = LoggerFactory.getLogger(FieldsCombiner.class);		
	
	protected static final CombineVisitor COMBINE_VISITOR = new CombineVisitor();
	
	/** Combine complex fields to w:fldSimple
	 * 
	 */
	public static void process(WordprocessingMLPackage wmlPackage) {
		log.info("starting");
		TraversalUtil.visit(wmlPackage, false, COMBINE_VISITOR);
		
		if (log.isDebugEnabled()) {
			log.debug(XmlUtils.marshaltoString(wmlPackage.getMainDocumentPart().getJaxbElement(), 
					true, true));
		}
		
	}
	
	protected static class CombineVisitor extends TraversalUtilVisitor<P> {

	    private final static QName _RInstrText_QNAME = 
	    		new QName(Namespaces.NS_WORD12, "instrText");

	    
		private static final int STATE_NONE = 0;
		private static final int STATE_EXPECT_BEGIN = 1;
		private static final int STATE_EXPECT_INSTR = 2;
		private static final int STATE_EXPECT_RESULT = 4;


		@Override
		public void apply(P element) {
			processContent(element.getContent());
		}	

		protected void processContent(List<Object> pContent) {
		List<Object> pResult = null;
		boolean haveChanges = false;
		boolean inField = false;
		Object item = null;
		STFldCharType fldCharType = null;
		int level = 0;
		int state = STATE_EXPECT_BEGIN;
		int markIdx = 0;
		List<Object> resultList = new ArrayList<Object>(2);
		StringBuilder instrTextBuffer = new StringBuilder(128);
		String tmpInstrText = null;
		
			if ((pContent != null) && (!pContent.isEmpty())) {
				pResult = new ArrayList<Object>(pContent.size());
				for (int i=0; i<pContent.size(); i++) {
					item = pContent.get(i);
					if (item instanceof R) {
						fldCharType = getFldCharType((R)item);
						if (fldCharType != null) {
							if (STFldCharType.BEGIN.equals(fldCharType)) {
								level++;
								state = STATE_EXPECT_INSTR;
								if (markIdx < i) {
									copyItems(pContent, markIdx, i, pResult);
									instrTextBuffer.setLength(0);
									resultList.clear();
								}
								markIdx = i;
							}
							else if (STFldCharType.SEPARATE.equals(fldCharType)) {
								state = STATE_EXPECT_RESULT;
							}
							else if (STFldCharType.END.equals(fldCharType)) {
								if (level > 0) {
									state = STATE_EXPECT_BEGIN;
									//Having empty (eg. XE) fldSimple causes interesting effects to the
									//layout in word - for the conversion process it probably makes sense,
									//but if you try to open the resulting document in word it's pure chaos.
									if ((!resultList.isEmpty()) &&
										(instrTextBuffer.length() > 0)) {
										pResult.add(createFldSimple(instrTextBuffer.toString(), resultList));
										haveChanges = true;
										markIdx = i + 1;
									}
									instrTextBuffer.setLength(0);
									resultList.clear();
									level--;
								}
							}
						}
						else {
							switch (state) {
								case STATE_EXPECT_INSTR:
									tmpInstrText = getInstrText((R)item);
									if (tmpInstrText != null) {
										instrTextBuffer.append(tmpInstrText);
									}
									break;
								case STATE_EXPECT_RESULT:
									resultList.add(item);
							}
						}
						
					}
					else if ((item instanceof JAXBElement) &&
							 (((JAXBElement)item).getValue() instanceof CTSimpleField)){
						if (markIdx < i) {
							copyItems(pContent, markIdx, i, pResult);
						}
						pResult.add(item);
						instrTextBuffer.setLength(0);
						resultList.clear();
						markIdx = i + 1;
						state = STATE_EXPECT_BEGIN;
					}
					else if ((item instanceof JAXBElement) &&
							 (((JAXBElement)item).getValue() instanceof P.Hyperlink)){
						processContent(((P.Hyperlink)((JAXBElement)item).getValue()).getContent());
					}
				}
				if (haveChanges) {
					if (markIdx < pContent.size()) {
						copyItems(pContent, markIdx, pContent.size(), pResult);
					}
					pContent.clear();
					pContent.addAll(pResult);
				}
			}
		}
		

		private String getInstrText(R run) {
		List<Object> rContent = run.getContent();
		Object item = null;
		Text text = null;
			for (int i=0; i<rContent.size(); i++) {
				item = rContent.get(i);
				if (item instanceof JAXBElement
						&& ((JAXBElement)item).getName().equals(_RInstrText_QNAME)) {
					text = (Text)((JAXBElement)item).getValue();
					break;
				}
			}
			return (text != null ? text.getValue() : null);
		}


		private Object createFldSimple(String instrText, List<Object> resultList) {
		CTSimpleField fldSimple = Context.getWmlObjectFactory().createCTSimpleField();
			fldSimple.setInstr(instrText);
			if ((resultList != null) && (!resultList.isEmpty())) {
				fldSimple.getContent().addAll(resultList);
			}
			return fldSimple;
		}


		private void copyItems(List<Object> source, int startIdx, int endIdx, List<Object> destination) {
			for (int i=startIdx; i<endIdx; i++) {
				destination.add(source.get(i));
			}
		}


		private STFldCharType getFldCharType(R r) {
		STFldCharType ret = null;
		List<Object> rContent = r.getContent();
		Object item = null;
			if ((rContent != null) && (!rContent.isEmpty())) {
				for (int i=0; i<rContent.size(); i++) {
					item = XmlUtils.unwrap(rContent.get(i));
					if (item instanceof FldChar) {
						ret = ((FldChar)item).getFldCharType();
						break;
					}
				}
			}
			return ret;
		}	
	}
	
}
