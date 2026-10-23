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
package org.docx4j.wml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.junit.Test;

/**
 * CR-059: the boolean attributes docx4j's 2008 schema gave {@code default="true"} answer as ECMA-376's prose
 * reads their absence. Off (§2.1's "off" rows) gives {@code false}; inherit ({@code w:lsdException}, from
 * {@code w:latentStyles}) and application-defined ({@code w:revisionView}) have no default, so the getter returns
 * {@code null}. The ones the prose leaves open, and {@code w:text}'s {@code multiLine}, follow Word 365 (CR-059 phase
 * 0, Jason, 2026-10-24): an absent {@code equalWidth} is equal columns (true; Word dropped the {@code w:col} widths and
 * omits {@code equalWidth="1"} itself), an absent {@code sep} draws no line, an absent {@code multiLine} allows no line
 * break, and the autospacing pair inherits from the style (null).
 */
public class WmlBooleanDefaultsTest {

	private static final String W = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";

	/** An element of the declared type with {@code attributes} and no content, unmarshalled. */
	private static Object element(Class<?> type, String attributes) throws Exception {
		String xml = "<w:x xmlns:w=\"" + W + "\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" " + attributes + "/>";
		org.w3c.dom.Document d = XmlUtils.getNewDocumentBuilder().parse(new java.io.ByteArrayInputStream(xml.getBytes("UTF-8")));
		return XmlUtils.unmarshal(d.getDocumentElement(), Context.jc, type);
	}

	private static Object get(Object o, String getter) throws Exception {
		Method m = o.getClass().getMethod(getter);
		return m.invoke(o);
	}

	/** Type, getter, and the attributes an element needs to unmarshal at all. */
	private static final Object[][] OFF = {
		{ CTBorder.class, "isShadow", "w:val=\"single\"" },
		{ CTBorder.class, "isFrame", "w:val=\"single\"" },
		{ CTEastAsianLayout.class, "isCombine", "" },
		{ CTEastAsianLayout.class, "isVert", "" },
		{ CTEastAsianLayout.class, "isVertCompress", "" },
		{ CTFramePr.class, "isAnchorLock", "" },
		{ CTWritingStyle.class, "isNlCheck", "w:lang=\"en-US\" w:vendorID=\"64\" w:dllVersion=\"131078\" w:checkStyle=\"0\" w:appName=\"MSWord\"" },
		{ CTDocProtect.class, "isFormatting", "" },
		{ CTDocProtect.class, "isEnforcement", "" },
		{ P.Hyperlink.class, "isHistory", "" },
		{ CTFtnEdnRef.class, "isCustomMarkFollows", "w:id=\"1\"" },
		{ CTWriteProtection.class, "isRecommended", "" },
		{ Lvl.LvlText.class, "isNull", "w:val=\"%1.\"" },
		{ Lvl.Legacy.class, "isLegacy", "" },
		{ Lvl.class, "isTentative", "w:ilvl=\"0\"" },
		{ FontRel.class, "isSubsetted", "r:id=\"rId1\"" },
		{ CTDocPartTypes.class, "isAll", "" },
		{ CTDocPartName.class, "isDecorated", "w:val=\"x\"" },
		{ Style.class, "isCustomStyle", "w:type=\"paragraph\"" },
		{ Style.class, "isDefault", "w:type=\"paragraph\"" },
		{ Styles.LatentStyles.class, "isDefLockedState", "" },
		{ Styles.LatentStyles.class, "isDefSemiHidden", "" },
		{ Styles.LatentStyles.class, "isDefUnhideWhenUsed", "" },
		{ Styles.LatentStyles.class, "isDefQFormat", "" },
		{ CTCaption.class, "isChapNum", "w:name=\"Figure\"" },
		{ CTCaption.class, "isNoLabel", "w:name=\"Figure\"" },
		{ FldChar.class, "isFldLock", "w:fldCharType=\"begin\"" },
		{ FldChar.class, "isDirty", "w:fldCharType=\"begin\"" },
		{ CTSimpleField.class, "isFldLock", "w:instr=\" PAGE \"" },
		{ CTSimpleField.class, "isDirty", "w:instr=\" PAGE \"" },
		{ CTColumns.class, "isSep", "w:num=\"2\"" },
		{ CTSdtText.class, "isMultiLine", "" },
	};

	private static final Object[][] NO_DEFAULT = {
		{ Styles.LatentStyles.LsdException.class, "isLocked", "w:name=\"Normal\"" },
		{ Styles.LatentStyles.LsdException.class, "isSemiHidden", "w:name=\"Normal\"" },
		{ Styles.LatentStyles.LsdException.class, "isUnhideWhenUsed", "w:name=\"Normal\"" },
		{ CTTrackChangesView.class, "isMarkup", "" },
		{ CTTrackChangesView.class, "isComments", "" },
		{ CTTrackChangesView.class, "isInsDel", "" },
		{ CTTrackChangesView.class, "isFormatting", "" },
		{ CTTrackChangesView.class, "isInkAnnotations", "" },
		{ PPrBase.Spacing.class, "isBeforeAutospacing", "w:before=\"0\"" },
		{ PPrBase.Spacing.class, "isAfterAutospacing", "w:after=\"0\"" },
	};

	/** Word's reading of an absent equalWidth: equal columns. */
	@Test
	public void anAbsentEqualWidthIsEqualColumns() throws Exception {
		assertTrue(((CTColumns) element(CTColumns.class, "w:num=\"2\"")).isEqualWidth());
		assertFalse(((CTColumns) element(CTColumns.class, "w:num=\"2\" w:equalWidth=\"0\"")).isEqualWidth());
	}

	@Test
	public void anAbsentOffAttributeIsFalse() throws Exception {
		for (Object[] row : OFF) {
			Object o = element((Class<?>) row[0], (String) row[2]);
			assertEquals(((Class<?>) row[0]).getSimpleName() + "." + row[1] + "()", Boolean.FALSE, get(o, (String) row[1]));
		}
	}

	@Test
	public void anAbsentInheritedOrApplicationAttributeIsNull() throws Exception {
		for (Object[] row : NO_DEFAULT) {
			Object o = element((Class<?>) row[0], (String) row[2]);
			assertNull(((Class<?>) row[0]).getSimpleName() + "." + row[1] + "()", get(o, (String) row[1]));
		}
	}

	/** An attribute that is written reads as written, whatever its default. */
	@Test
	public void aWrittenAttributeReadsAsWritten() throws Exception {
		assertTrue(((Lvl) element(Lvl.class, "w:ilvl=\"0\" w:tentative=\"1\"")).isTentative());
		assertTrue(((Style) element(Style.class, "w:type=\"paragraph\" w:customStyle=\"1\"")).isCustomStyle());
		assertEquals(Boolean.TRUE, ((Styles.LatentStyles.LsdException) element(Styles.LatentStyles.LsdException.class, "w:name=\"Normal\" w:locked=\"1\"")).isLocked());
		assertEquals(Boolean.FALSE, ((Styles.LatentStyles.LsdException) element(Styles.LatentStyles.LsdException.class, "w:name=\"Normal\" w:semiHidden=\"0\"")).isSemiHidden());
		assertTrue(((CTTrackChangesView) element(CTTrackChangesView.class, "w:markup=\"true\"")).isMarkup());
		assertTrue(((Styles.LatentStyles) element(Styles.LatentStyles.class, "w:defLockedState=\"1\"")).isDefLockedState());
	}

	/** An absent attribute is not written back, so a document round-trips as it came. */
	@Test
	public void anAbsentAttributeIsNotWritten() throws Exception {
		Lvl lvl = (Lvl) element(Lvl.class, "w:ilvl=\"0\"");
		String xml = XmlUtils.marshaltoString(new jakarta.xml.bind.JAXBElement<Lvl>(new javax.xml.namespace.QName(W, "lvl"), Lvl.class, lvl), true, false);
		assertFalse(xml, xml.contains("tentative"));
	}
}
