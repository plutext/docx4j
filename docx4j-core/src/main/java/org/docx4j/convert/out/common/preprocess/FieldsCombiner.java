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

import jakarta.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.utils.TraversalUtilVisitor;
import org.docx4j.wml.CTFFDDList;
import org.docx4j.wml.CTFFData;
import org.docx4j.wml.CTFFTextInput;
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
		// before combining: a legacy form field's result is not in the document at all,
		// it is in the w:ffData of its begin, so it has to be written out as a result
		// run while the begin/separate/end structure is still there
		expandFormFieldResults(wmlPackage);
		// before combining: the operands of a resultless IF are themselves fields, and
		// combining them first would turn them into live fldSimples we then paint
		removeResultlessIfFields(wmlPackage);
		TraversalUtil.visit(wmlPackage, false, COMBINE_VISITOR);
		
		if (log.isDebugEnabled()) {
			log.debug(XmlUtils.marshaltoString(wmlPackage.getMainDocumentPart().getJaxbElement(), 
					true, true));
		}
		
	}
	
	// -------------------------------------------------- legacy (w:ffData) form fields

	/**
	 * A legacy form field - <code>FORMDROPDOWN</code>, <code>FORMTEXT</code>,
	 * <code>FORMCHECKBOX</code> - keeps its state in the <code>w:ffData</code> of its
	 * <code>w:fldChar w:fldCharType="begin"</code>, and Word paints that state, not the
	 * field result: for a drop-down the <code>w:listEntry</code> the
	 * <code>w:ddList/w:result</code> index selects (0 where there is no
	 * <code>w:result</code>, ECMA-376 17.16.20), for a text field its
	 * <code>w:textInput/w:default</code> where nothing has been typed into it.  Neither
	 * FO pathway nor HTML reads <code>w:ffData</code> at all, and both emit nothing for
	 * a <code>w:fldChar</code>, so a drop-down came out as the empty
	 * <code>fo:inline</code> of its bookmark.
	 *
	 * <p>Measured against Word 365: a drop-down offering four honorifics, with no
	 * <code>w:result</code> and whose <code>separate</code> is immediately followed by
	 * its <code>end</code>, is painted by Word as the first of them - its line runs
	 * 297.7..413.6 where ours began at 302.7 and ended at 378.3, the whole entry
	 * missing.</p>
	 *
	 * <p>Where the field has no <code>separate</code> at all one is synthesised, since
	 * ECMA-376 17.16.18 puts the field result between the separate and the end and
	 * {@link CombineVisitor} will not combine a field without one.  A field which
	 * already paints something between its separate and its end keeps what it has: that
	 * is the value the user typed, and Word paints it over the default.</p>
	 *
	 * <p><strong>A checkbox is deliberately left alone.</strong>  Word does not draw it
	 * with a glyph: measured over the goldens of the 15 documents of three corpora which
	 * hold one, every <code>FORMCHECKBOX</code> in Word's own PDF is a <em>stroked
	 * square path</em> - 0.72pt line, side 7.44 to 11.28pt with the field's font size -
	 * and the PDF's text layer has nothing at all where it sits.  Writing a
	 * <code>&#x2610;</code> would put a character on the line that Word's line does not
	 * have.  What is lost is only the advance: measured on three documents, the text
	 * after the box starts 9.68, 12.4 and 12.76pt left of Word's, on 772 fields.
	 * Recorded in word-layout-rules.md &#xa7;5.7.</p>
	 *
	 * <p>Set <code>docx4j.convert.out.fields.formFieldResults</code> to false to keep
	 * the old behaviour.</p>
	 *
	 * @since 17.1.0
	 */
	static void expandFormFieldResults(WordprocessingMLPackage wmlPackage) {
		if (!org.docx4j.Docx4jProperties.getProperty(
				"docx4j.convert.out.fields.formFieldResults", true)) return;
		try {
			TraversalUtil.visit(wmlPackage, false, new FormFieldVisitor());
		} catch (RuntimeException e) {
			log.warn("Couldn't expand a form field result: " + e.getMessage(), e);
		}
	}

	private static class FormFieldVisitor extends TraversalUtilVisitor<P> {
		@Override
		public void apply(P element) {
			expandFormFieldResults(element.getContent());
		}
	}

	static void expandFormFieldResults(List<Object> pContent) {

		if (pContent == null) return;
		// the begin's w:fldChar may share a run with the instruction (see
		// normaliseFieldRuns), and the separate we insert has to be a run of its own
		CombineVisitor.normaliseFieldRuns(pContent);

		for (int i = 0; i < pContent.size(); i++) {
			Object item = XmlUtils.unwrap(pContent.get(i));
			if (!(item instanceof R)) continue;
			R begin = (R)item;
			CTFFData ffData = ffDataOfBegin(begin);
			if (ffData == null) continue;
			String result = formFieldResult(ffData);

			int level = 0, sep = -1, end = -1;
			for (int j = i; j < pContent.size() && end < 0; j++) {
				Object o = XmlUtils.unwrap(pContent.get(j));
				if (!(o instanceof R)) continue;
				STFldCharType t = fldCharTypeOf((R)o);
				if (t == null) continue;
				if (STFldCharType.BEGIN.equals(t)) level++;
				else if (STFldCharType.SEPARATE.equals(t)) { if (level == 1) sep = j; }
				else if (STFldCharType.END.equals(t)) { if (--level == 0) end = j; }
			}
			if (end < 0) continue;			// unbalanced; leave it alone
			if (result == null || (sep >= 0 && paintsSomething(pContent, sep + 1, end))) {
				i = end;
				continue;
			}

			R resultRun = Context.getWmlObjectFactory().createR();
			if (begin.getRPr() != null) resultRun.setRPr(XmlUtils.deepCopy(begin.getRPr()));
			resultRun.setParent(begin.getParent());
			Text t = Context.getWmlObjectFactory().createText();
			t.setValue(result);
			t.setSpace("preserve");
			resultRun.getContent().add(t);

			if (sep < 0) {
				R sepRun = Context.getWmlObjectFactory().createR();
				if (begin.getRPr() != null) sepRun.setRPr(XmlUtils.deepCopy(begin.getRPr()));
				sepRun.setParent(begin.getParent());
				FldChar separate = Context.getWmlObjectFactory().createFldChar();
				separate.setFldCharType(STFldCharType.SEPARATE);
				sepRun.getContent().add(separate);
				pContent.add(end, sepRun);
				pContent.add(end + 1, resultRun);
				i = end + 2;
			} else {
				pContent.add(sep + 1, resultRun);
				i = end + 1;
			}
		}
	}

	/** The w:ffData of a run which begins a complex field, or null. */
	private static CTFFData ffDataOfBegin(R run) {
		for (Object c : run.getContent()) {
			Object u = XmlUtils.unwrap(c);
			if (u instanceof FldChar) {
				FldChar fc = (FldChar)u;
				return STFldCharType.BEGIN.equals(fc.getFldCharType()) ? fc.getFfData() : null;
			}
		}
		return null;
	}

	private static STFldCharType fldCharTypeOf(R run) {
		for (Object c : run.getContent()) {
			Object u = XmlUtils.unwrap(c);
			if (u instanceof FldChar) return ((FldChar)u).getFldCharType();
		}
		return null;
	}

	/** What Word paints for this form field, or null where it paints nothing of its own
	 *  (a checkbox, or a text field with no default). */
	private static String formFieldResult(CTFFData ffData) {
		if (ffData.getNameOrEnabledOrCalcOnExit() == null) return null;
		for (JAXBElement<?> e : ffData.getNameOrEnabledOrCalcOnExit()) {
			Object v = (e == null ? null : e.getValue());
			if (v instanceof CTFFDDList) {
				CTFFDDList dd = (CTFFDDList)v;
				List<CTFFDDList.ListEntry> entries = dd.getListEntry();
				if (entries == null || entries.isEmpty()) return null;
				int idx = 0;
				if (dd.getResult() != null && dd.getResult().getVal() != null) {
					idx = dd.getResult().getVal().intValue();
				}
				if (idx < 0 || idx >= entries.size()) idx = 0;
				return entries.get(idx).getVal();
			} else if (v instanceof CTFFTextInput) {
				CTFFTextInput ti = (CTFFTextInput)v;
				return (ti.getDefault() == null ? null : ti.getDefault().getVal());
			}
		}
		return null;
	}

	/** Whether the field result already holds something Word would paint. */
	private static boolean paintsSomething(List<Object> pContent, int from, int to) {
		for (int i = from; i < to && i < pContent.size(); i++) {
			Object u = XmlUtils.unwrap(pContent.get(i));
			if (!(u instanceof R)) return true;	// a hyperlink, an SDT, a picture ...
			for (Object c : ((R)u).getContent()) {
				Object x = XmlUtils.unwrap(c);
				if (x instanceof FldChar) continue;
				if (c instanceof JAXBElement && INSTR_TEXT_QNAME.equals(((JAXBElement)c).getName())) continue;
				return true;
			}
		}
		return false;
	}

	// ------------------------------------------------------------ a resultless IF field

	/**
	 * A complex field with no <code>w:fldChar w:fldCharType="separate"</code> has no
	 * result (ECMA-376 17.16.18: the field result is what lies between the separate and
	 * the end), so Word paints nothing for it - everything from the begin to the end is
	 * field instruction.  For an <code>IF</code> field that is how Word writes a
	 * conditional block of a footer: {@code IF { PAGE } = { NUMPAGES } "…" ""}, with the
	 * true branch - which may be a whole table - sitting in <code>w:instrText</code>.
	 *
	 * <p>We kept the structure, so the operands' own <code>PAGE</code> and
	 * <code>NUMPAGES</code> fields (which do have a separate, so FieldsCombiner turns
	 * them into live fields) were painted, and the true branch's table reserved its
	 * height.  Measured against Word 365 on an 8-page document whose footer is exactly
	 * that: Word's footer is the page number alone ("1/8" at y=811.2), ours had a stray
	 * "19" at y=724.4 and a <code>region-after extent="112.251pt"</code> against Word's
	 * ~47 - <strong>65pt of body lost on every page</strong>, which is the 6 missing
	 * lines and the 9th page.</p>
	 *
	 * <p>Restricted to <code>IF</code> deliberately.  Measured over 435 corpus
	 * documents, a field with no separate is most often <code>PAGE</code> (24
	 * occurrences in 21 documents), <code>FORMCHECKBOX</code>, <code>MERGEFIELD</code>
	 * or <code>XE</code> - all of which we do render, and Word renders too once it
	 * updates the field on print.  Only <code>IF</code> carries its branches in the
	 * instruction, so only <code>IF</code> loses content by being kept (10 occurrences
	 * in 3 documents).  An <code>IF</code> that <em>does</em> have a separate keeps
	 * its cached result, which is what Word shows.</p>
	 *
	 * <p>The condition itself is not evaluated, and does not need to be: the branch
	 * Word chooses on all but the last page of these documents is the empty one, and
	 * neither branch can be painted conditionally anyway - FOP resolves
	 * <code>fo:page-number</code> at layout time, long after the FO is written, so an
	 * IF whose operand is <code>PAGE</code> cannot be decided per page.  Recorded as a
	 * limitation in word-layout-rules.md &#xa7;10.</p>
	 *
	 * <p>Set <code>docx4j.convert.out.fields.dropResultlessIf</code> to false to keep
	 * the old behaviour.</p>
	 *
	 * @since 17.1.0
	 */
	static void removeResultlessIfFields(WordprocessingMLPackage wmlPackage) {
		if (!org.docx4j.Docx4jProperties.getProperty(
				"docx4j.convert.out.fields.dropResultlessIf", true)) return;
		BlockListCollector collector = new BlockListCollector();
		TraversalUtil.visit(wmlPackage, false, collector);
		for (List<Object> blocks : collector.lists) {
			try {
				removeResultlessIfFields(blocks);
			} catch (RuntimeException e) {
				log.warn("Couldn't drop a resultless IF field: " + e.getMessage(), e);
			}
		}
	}

	/** Every distinct block-level content list holding a w:p, in document order.  A
	 *  field's begin and end are commonly in different paragraphs of the same list
	 *  (with the branch's table between them), so the unit of work is the list. */
	private static class BlockListCollector extends TraversalUtilVisitor<P> {
		final List<List<Object>> lists = new ArrayList<List<Object>>();
		private final java.util.Set<List<Object>> seen =
				java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<List<Object>, Boolean>());

		@Override
		public void apply(P element, Object parent, List<Object> siblings) {
			if (siblings != null && seen.add(siblings)) lists.add(siblings);
		}
	}

	/** One open complex field. */
	private static class OpenField {
		int blockIdx, runIdx;
		boolean hasSeparate;
		boolean pureBeginRun;
		final StringBuilder instr = new StringBuilder(64);
	}

	static void removeResultlessIfFields(List<Object> blocks) {

		List<int[]> spans = new ArrayList<int[]>(); // beginBlock, beginRun, endBlock, endRun
		java.util.Deque<OpenField> stack = new java.util.ArrayDeque<OpenField>();

		for (int b = 0; b < blocks.size(); b++) {
			Object block = XmlUtils.unwrap(blocks.get(b));
			if (!(block instanceof P)) {
				// a table, or an SDT: it can only ever be *between* a begin and an end,
				// and then the whole block goes, so its insides need no index
				continue;
			}
			List<Object> content = ((P)block).getContent();
			for (int r = 0; r < content.size(); r++) {
				Object item = XmlUtils.unwrap(content.get(r));
				if (!(item instanceof R)) {
					// a fldChar inside a hyperlink or a run-level SDT would not be
					// addressable by (block, run), so leave the whole list alone
					if (containsFldChar(content.get(r))) return;
					continue;
				}
				R run = (R)item;
				boolean pure = isFieldOnlyRun(run);
				for (Object c : run.getContent()) {
					Object u = XmlUtils.unwrap(c);
					if (u instanceof FldChar) {
						STFldCharType type = ((FldChar)u).getFldCharType();
						if (STFldCharType.BEGIN.equals(type)) {
							OpenField f = new OpenField();
							f.blockIdx = b;
							f.runIdx = r;
							f.pureBeginRun = pure;
							stack.push(f);
						} else if (STFldCharType.SEPARATE.equals(type)) {
							if (!stack.isEmpty()) stack.peek().hasSeparate = true;
						} else if (STFldCharType.END.equals(type)) {
							if (stack.isEmpty()) continue;
							OpenField f = stack.pop();
							if (stack.isEmpty() && !f.hasSeparate && f.pureBeginRun && pure
									&& isIfField(f.instr)) {
								spans.add(new int[] { f.blockIdx, f.runIdx, b, r });
							}
						}
					} else if (c instanceof JAXBElement
							&& INSTR_TEXT_QNAME.equals(((JAXBElement)c).getName())) {
						if (!stack.isEmpty() && !stack.peek().hasSeparate) {
							Object v = ((JAXBElement)c).getValue();
							if (v instanceof Text && ((Text)v).getValue() != null) {
								stack.peek().instr.append(((Text)v).getValue());
							}
						}
					}
				}
			}
		}

		for (int i = spans.size() - 1; i >= 0; i--) {
			int[] s = spans.get(i);
			int beginBlock = s[0], beginRun = s[1], endBlock = s[2], endRun = s[3];
			if (beginBlock == endBlock) {
				List<Object> content = ((P)XmlUtils.unwrap(blocks.get(beginBlock))).getContent();
				content.subList(beginRun, Math.min(endRun + 1, content.size())).clear();
			} else {
				List<Object> endContent = ((P)XmlUtils.unwrap(blocks.get(endBlock))).getContent();
				endContent.subList(0, Math.min(endRun + 1, endContent.size())).clear();
				if (endBlock > beginBlock + 1) {
					blocks.subList(beginBlock + 1, endBlock).clear();
				}
				List<Object> beginContent = ((P)XmlUtils.unwrap(blocks.get(beginBlock))).getContent();
				beginContent.subList(beginRun, beginContent.size()).clear();
			}
		}
	}

	/** The first token of the instruction is IF. */
	private static boolean isIfField(StringBuilder instr) {
		String s = instr.toString().trim();
		int sp = 0;
		while (sp < s.length() && !Character.isWhitespace(s.charAt(sp))) sp++;
		return "IF".equalsIgnoreCase(s.substring(0, sp));
	}

	/** A run holding nothing but w:fldChar and w:instrText - so deleting the whole run
	 *  loses no painted content. */
	private static boolean isFieldOnlyRun(R run) {
		for (Object c : run.getContent()) {
			if (XmlUtils.unwrap(c) instanceof FldChar) continue;
			if (c instanceof JAXBElement && INSTR_TEXT_QNAME.equals(((JAXBElement)c).getName())) continue;
			return false;
		}
		return true;
	}

	/** Whether this paragraph child holds a w:fldChar somewhere inside it - a field
	 *  begun inside a hyperlink or a run-level SDT, which (block, run) cannot address. */
	private static boolean containsFldChar(Object o) {
		Object u = XmlUtils.unwrap(o);
		if (u instanceof FldChar) return true;
		if (u instanceof org.docx4j.wml.SdtElement) {
			org.docx4j.wml.SdtElement sdt = (org.docx4j.wml.SdtElement)u;
			return sdt.getSdtContent() != null && containsFldChar(sdt.getSdtContent());
		}
		if (u instanceof org.docx4j.wml.ContentAccessor) {
			for (Object c : ((org.docx4j.wml.ContentAccessor)u).getContent()) {
				if (containsFldChar(c)) return true;
			}
		}
		return false;
	}

	private final static QName INSTR_TEXT_QNAME =
			new QName(Namespaces.NS_WORD12, "instrText");

	protected static class CombineVisitor extends TraversalUtilVisitor<P> {

	    private final static QName _RInstrText_QNAME = 
	    		new QName(Namespaces.NS_WORD12, "instrText");

	    
		private static final int STATE_NONE = 0;
		private static final int STATE_EXPECT_BEGIN = 1;
		private static final int STATE_EXPECT_INSTR = 2;
		private static final int STATE_EXPECT_RESULT = 4;


		@Override
		public void apply(P element) {
			normaliseFieldRuns(element.getContent());
			processContent(element.getContent());
		}

		/**
		 * Word often writes a whole field header in one run -
		 * {@code <w:r><w:fldChar begin/><w:instrText> PAGE </w:instrText><w:fldChar separate/></w:r>}
		 * - and the state machine below looks at one {@code w:fldChar} per run
		 * ({@link #getFldCharType}), so it saw the BEGIN, never the SEPARATE, and the
		 * field was never combined into a {@code w:fldSimple}.  The runs were then
		 * emitted as the field's cached result: measured, a 76-page document printed
		 * "Página 73 de 76" on every page where Word prints 2 ... 76, and the FO held no
		 * {@code fo:page-number} at all.  Splitting such a run into one run per item,
		 * each keeping the run properties, renders identically and lets the state
		 * machine see the field.
		 *
		 * @since 17.1.0
		 */
		static void normaliseFieldRuns(List<Object> pContent) {

			if (pContent==null) return;
			for (int i=0; i<pContent.size(); i++) {
				if (!(pContent.get(i) instanceof R)) continue;
				R r = (R)pContent.get(i);
				List<Object> rContent = r.getContent();
				if (rContent==null || rContent.size()<2) continue;
				boolean fieldPart = false;
				for (Object c : rContent) {
					Object u = XmlUtils.unwrap(c);
					if (u instanceof FldChar
							|| (c instanceof JAXBElement
								&& _RInstrText_QNAME.equals(((JAXBElement)c).getName()))) {
						fieldPart = true;
						break;
					}
				}
				if (!fieldPart) continue;

				List<Object> split = new ArrayList<Object>(rContent.size());
				for (Object c : rContent) {
					R n = Context.getWmlObjectFactory().createR();
					if (r.getRPr()!=null) n.setRPr(XmlUtils.deepCopy(r.getRPr()));
					n.setParent(r.getParent());
					n.getContent().add(c);
					split.add(n);
				}
				pContent.remove(i);
				pContent.addAll(i, split);
				i += split.size() - 1;
			}
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
