/*
 *  Copyright 2026, Plutext Pty Ltd.
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
package org.docx4j.convert.out.common.preprocess;

import java.math.BigInteger;
import java.util.List;

import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.McMode;
import org.docx4j.model.PropertyResolver;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.utils.TraversalUtilVisitor;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTFFCheckBox;
import org.docx4j.wml.CTFFData;
import org.docx4j.wml.CTSimpleField;
import org.docx4j.wml.FldChar;
import org.docx4j.wml.HpsMeasure;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.STFldCharType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A legacy check box (a {@code FORMCHECKBOX} field) becomes a {@code w:fldSimple} the FO
 * writer can draw.  Word paints the state in the field's {@code w:ffData}, not a result,
 * as a stroked square with nothing in the text layer, and sets what follows past it; so
 * its size, state and colour, which the writer cannot see in a field's runs, travel in
 * the instruction: {@code FORMCHECKBOX <half-points> <0|1> <colour>}.
 *
 * <p>Measured on probe formcheckbox-metrics (Word's goldens, 5 November 2026, the same at
 * modes 12 and 15): the size is the field's first run's, whatever the font, or
 * {@code w:size} where the field states one; {@code w:checked} else {@code w:default} is
 * the state.  The colour is the run's (corpus document 326).  A hidden run paints
 * nothing, so is left alone, as is a field that does not end in its paragraph.</p>
 *
 * <p>The field has no text, as Word's has none: a tab after the box still counts as a
 * leading tab, which is what keeps corpus document 7235's {@code FORMCHECKBOX<tab>text} off
 * the table-of-contents leader.  {@code WordLayoutFixups.tabsAfterCheckBoxes} lays such a
 * tab out from where the box ends.  The field's runs go: Word's line does not count them
 * (corpus document 1140's lines are at Word's without them, 16pt off with them); the field
 * holds an empty run with the first one's properties, which the writer styles the box from.</p>
 *
 * @since 17.3.6
 */
public class FormCheckBoxes {

	private static Logger log = LoggerFactory.getLogger(FormCheckBoxes.class);

	/** The field's name, in the instruction written here. */
	public static final String FIELD = "FORMCHECKBOX";

	public static void process(WordprocessingMLPackage wmlPackage) {
		PropertyResolver resolver = null;
		try {
			resolver = wmlPackage.getMainDocumentPart().getPropertyResolver();
		} catch (Exception e) {
			log.warn("No property resolver; direct run properties only: " + e.getMessage());
		}
		final PropertyResolver propertyResolver = resolver;
		try {
			TraversalUtil.visit(wmlPackage, false, new TraversalUtilVisitor<P>() {
				@Override
				public void apply(P element) {
					replaceCheckBoxes(element, propertyResolver);
				}
			}, McMode.ALL);
		} catch (RuntimeException e) {
			log.warn("Check boxes not drawn: " + e.getMessage(), e);
		}
	}

	static void replaceCheckBoxes(P paragraph, PropertyResolver resolver) {
		List<Object> content = paragraph.getContent();
		if (content == null) return;
		FieldsCombiner.CombineVisitor.normaliseFieldRuns(content);
		for (int i = 0; i < content.size(); i++) {
			Object item = XmlUtils.unwrap(content.get(i));
			if (!(item instanceof R)) continue;
			R begin = (R) item;
			CTFFCheckBox box = checkBoxOf(begin);
			if (box == null) continue;
			int end = endOf(content, i);
			if (end < 0) continue; // it ends in another paragraph
			RPr effective = effectiveRPr(begin, paragraph, resolver);
			if (isOn(effective == null ? null : effective.getVanish())) continue;
			CTSimpleField field = simpleField(begin, box, effective);
			for (int j = end; j >= i; j--) {
				if (XmlUtils.unwrap(content.get(j)) instanceof R) content.remove(j);
			}
			content.add(i, Context.getWmlObjectFactory().createPFldSimple(field));
		}
	}

	private static CTSimpleField simpleField(R begin, CTFFCheckBox box, RPr effective) {
		boolean checked = box.getChecked() != null ? isOn(box.getChecked()) : isOn(box.getDefault());
		CTSimpleField field = Context.getWmlObjectFactory().createCTSimpleField();
		long halfPoints = halfPoints(box, effective);
		field.setInstr(" " + FIELD + " " + halfPoints + " " + (checked ? "1" : "0") + " " + colour(effective) + " ");
		R run = Context.getWmlObjectFactory().createR();
		RPr rPr = begin.getRPr() == null ? null : XmlUtils.deepCopy(begin.getRPr());
		if (!isOn(box.getSizeAuto()) && box.getSize() != null) {
			if (rPr == null) rPr = Context.getWmlObjectFactory().createRPr();
			HpsMeasure sz = Context.getWmlObjectFactory().createHpsMeasure();
			sz.setVal(BigInteger.valueOf(halfPoints));
			rPr.setSz(sz);
		}
		if (rPr != null) run.setRPr(rPr);
		run.setParent(field);
		field.getContent().add(run);
		return field;
	}

	private static long halfPoints(CTFFCheckBox box, RPr effective) {
		if (!isOn(box.getSizeAuto()) && box.getSize() != null && box.getSize().getVal() != null) {
			return box.getSize().getVal().longValue();
		}
		BigInteger sz = effective == null || effective.getSz() == null ? null : effective.getSz().getVal();
		return sz == null ? 20 : sz.longValue(); // ECMA-376: 10pt where nothing says otherwise
	}

	private static String colour(RPr effective) {
		String val = effective == null || effective.getColor() == null ? null : effective.getColor().getVal();
		return val == null || val.isEmpty() ? "auto" : val;
	}

	private static RPr effectiveRPr(R run, P paragraph, PropertyResolver resolver) {
		if (resolver != null) {
			try {
				return resolver.getEffectiveRPr(run.getRPr(), paragraph.getPPr());
			} catch (Exception e) {
				log.warn("Effective run properties: " + e.getMessage());
			}
		}
		return run.getRPr();
	}

	private static boolean isOn(BooleanDefaultTrue b) {
		return b != null && b.isVal();
	}

	/** The w:checkBox of a run that begins a field, or null. */
	private static CTFFCheckBox checkBoxOf(R run) {
		FldChar fldChar = fldCharOf(run);
		if (fldChar == null || !STFldCharType.BEGIN.equals(fldChar.getFldCharType())) return null;
		CTFFData ffData = fldChar.getFfData();
		if (ffData == null || ffData.getNameOrEnabledOrCalcOnExit() == null) return null;
		for (Object e : ffData.getNameOrEnabledOrCalcOnExit()) {
			Object v = XmlUtils.unwrap(e);
			if (v instanceof CTFFCheckBox) return (CTFFCheckBox) v;
		}
		return null;
	}

	/** The index of the run ending the field begun at this one, or -1. */
	private static int endOf(List<Object> content, int begin) {
		int level = 0;
		for (int j = begin; j < content.size(); j++) {
			Object o = XmlUtils.unwrap(content.get(j));
			FldChar fldChar = o instanceof R ? fldCharOf((R) o) : null;
			if (fldChar == null) continue;
			if (STFldCharType.BEGIN.equals(fldChar.getFldCharType())) level++;
			else if (STFldCharType.END.equals(fldChar.getFldCharType()) && --level == 0) return j;
		}
		return -1;
	}

	private static FldChar fldCharOf(R run) {
		for (Object c : run.getContent()) {
			Object u = XmlUtils.unwrap(c);
			if (u instanceof FldChar) return (FldChar) u;
		}
		return null;
	}
}
