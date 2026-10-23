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
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.junit.Ignore;
import org.junit.Test;

/**
 * CR-060: every attribute ECMA-376 types {@code ST_OnOff} (docx4j binds them as {@code Boolean}) reads as Word 365
 * reads an on/off value: off for exactly {@code false}, {@code off} or {@code 0}, case-sensitive and untrimmed; on
 * for anything else, the empty string included (measured on the run properties, CR-060 section 1, rule 1; the same
 * simple type everywhere). An absent attribute still reads as CR-059 has it.
 */
public class OnOffAttributesTest {

	private static final String W = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";

	/** Type, getter, and the other attributes an element of the type is given. */
	private static final Object[][] ATTRIBUTES = {
		{ BooleanDefaultTrue.class, "isVal", "" },
		{ BooleanDefaultFalse.class, "isVal", "" },
		{ CTBorder.class, "isShadow", "w:val=\"single\"" },
		{ CTBorder.class, "isFrame", "w:val=\"single\"" },
		{ CTEastAsianLayout.class, "isCombine", "" },
		{ CTEastAsianLayout.class, "isVert", "" },
		{ CTEastAsianLayout.class, "isVertCompress", "" },
		{ CTFramePr.class, "isAnchorLock", "" },
		{ CTWritingStyle.class, "isNlCheck", "w:lang=\"en-US\" w:vendorID=\"64\" w:dllVersion=\"131078\" w:appName=\"MSWord\"" },
		{ CTWritingStyle.class, "isCheckStyle", "w:lang=\"en-US\" w:vendorID=\"64\" w:dllVersion=\"131078\" w:appName=\"MSWord\"" },
		{ CTDocProtect.class, "isFormatting", "" },
		{ CTDocProtect.class, "isEnforcement", "" },
		{ CTTrackChangesView.class, "isMarkup", "" },
		{ CTTrackChangesView.class, "isComments", "" },
		{ CTTrackChangesView.class, "isInsDel", "" },
		{ CTTrackChangesView.class, "isFormatting", "" },
		{ CTTrackChangesView.class, "isInkAnnotations", "" },
		{ PPrBase.Spacing.class, "isBeforeAutospacing", "w:before=\"0\"" },
		{ PPrBase.Spacing.class, "isAfterAutospacing", "w:after=\"0\"" },
		{ CTSimpleField.class, "isFldLock", "w:instr=\" PAGE \"" },
		{ CTSimpleField.class, "isDirty", "w:instr=\" PAGE \"" },
		{ FldChar.class, "isFldLock", "w:fldCharType=\"begin\"" },
		{ FldChar.class, "isDirty", "w:fldCharType=\"begin\"" },
		{ CTColumns.class, "isEqualWidth", "w:num=\"2\"" },
		{ CTColumns.class, "isSep", "w:num=\"2\"" },
		{ CTSdtText.class, "isMultiLine", "" },
		{ P.Hyperlink.class, "isHistory", "" },
		{ CTFtnEdnRef.class, "isCustomMarkFollows", "w:id=\"1\"" },
		{ CTReadingModeInkLockDown.class, "isActualPg", "w:w=\"100\" w:h=\"100\" w:fontSz=\"100\"" },
		{ CTWriteProtection.class, "isRecommended", "" },
		{ Lvl.LvlText.class, "isNull", "w:val=\"%1.\"" },
		{ Lvl.Legacy.class, "isLegacy", "" },
		{ Lvl.class, "isTentative", "w:ilvl=\"0\"" },
		{ FontRel.class, "isSubsetted", "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" r:id=\"rId1\"" },
		{ CTDocPartTypes.class, "isAll", "" },
		{ CTDocPartName.class, "isDecorated", "w:val=\"x\"" },
		{ Style.class, "isDefault", "w:type=\"paragraph\"" },
		{ Style.class, "isCustomStyle", "w:type=\"paragraph\"" },
		{ Styles.LatentStyles.LsdException.class, "isLocked", "w:name=\"Normal\"" },
		{ Styles.LatentStyles.LsdException.class, "isSemiHidden", "w:name=\"Normal\"" },
		{ Styles.LatentStyles.LsdException.class, "isUnhideWhenUsed", "w:name=\"Normal\"" },
		{ Styles.LatentStyles.LsdException.class, "isQFormat", "w:name=\"Normal\"" },
		{ Styles.LatentStyles.class, "isDefLockedState", "" },
		{ Styles.LatentStyles.class, "isDefSemiHidden", "" },
		{ Styles.LatentStyles.class, "isDefUnhideWhenUsed", "" },
		{ Styles.LatentStyles.class, "isDefQFormat", "" },
		{ CTCaption.class, "isChapNum", "w:name=\"Figure\"" },
		{ CTCaption.class, "isNoLabel", "w:name=\"Figure\"" },
	};

	/** A value as written, and Word's reading of it */
	private static final Object[][] VALUES = {
		{ "false", false }, { "off", false }, { "0", false },
		{ "true", true }, { "on", true }, { "1", true },
		{ "", true }, { " ", true }, { " false", true }, { "false ", true }, { "FALSE", true }, { "Off", true },
		{ "00", true }, { "no", true }, { "yes", true } };

	private static Object element(Class<?> type, String attributes) throws Exception {
		String xml = "<w:x xmlns:w=\"" + W + "\" " + attributes + "/>";
		org.w3c.dom.Document d = XmlUtils.getNewDocumentBuilder().parse(new java.io.ByteArrayInputStream(xml.getBytes("UTF-8")));
		return XmlUtils.unmarshal(d.getDocumentElement(), Context.jc, type);
	}

	private static String attribute(String getter) {
		return Character.toLowerCase(getter.charAt(2)) + getter.substring(3);
	}

	@Ignore("CR-060 phase 1")
	@Test
	public void everyOnOffAttributeReadsAsWordReadsIt() throws Exception {
		List<String> diffs = new ArrayList<>();
		for (Object[] row : ATTRIBUTES) {
			Class<?> type = (Class<?>) row[0];
			String getter = (String) row[1];
			Method m = type.getMethod(getter);
			for (Object[] v : VALUES) {
				String attrs = row[2] + " w:" + attribute(getter) + "=\"" + v[0] + "\"";
				Object got = m.invoke(element(type, attrs));
				if (!v[1].equals(got)) {
					diffs.add(type.getSimpleName() + "." + getter + "() for \"" + v[0] + "\": " + got + ", Word " + v[1]);
				}
			}
		}
		assertTrue(diffs.size() + " readings differ from Word's:\n" + String.join("\n", diffs), diffs.isEmpty());
	}

	/** The table above is every Boolean attribute in org.docx4j.wml, and each is bound through the adapter. */
	@Ignore("CR-060 phase 1")
	@Test
	public void everyBooleanAttributeIsBoundThroughTheAdapter() throws Exception {
		Set<String> tested = new HashSet<>();
		for (Object[] row : ATTRIBUTES) {
			tested.add(((Class<?>) row[0]).getName() + "." + attribute((String) row[1]));
		}
		Set<String> found = new TreeSet<>();
		List<String> unbound = new ArrayList<>();
		for (Class<?> c : classes()) {
			for (Field f : c.getDeclaredFields()) {
				XmlAttribute a = f.getAnnotation(XmlAttribute.class);
				if (a == null || f.getType() != Boolean.class) continue;
				String name = c.getName() + "." + a.name();
				found.add(name);
				XmlJavaTypeAdapter adapter = f.getAnnotation(XmlJavaTypeAdapter.class);
				if (adapter == null || !adapter.value().getName().equals("org.docx4j.wml.OnOffAdapter")) unbound.add(name);
			}
		}
		assertEquals("Boolean attributes in org.docx4j.wml: " + found, 48, found.size());
		assertEquals("tested: " + found, found, new TreeSet<>(tested));
		assertTrue("not bound through OnOffAdapter: " + unbound, unbound.isEmpty());
	}

	/** The classes ObjectFactory creates, with their nested classes */
	private static Set<Class<?>> classes() {
		Set<Class<?>> out = new HashSet<>();
		List<Class<?>> todo = new ArrayList<>();
		for (Method m : ObjectFactory.class.getMethods()) {
			if (m.getName().startsWith("create") && m.getReturnType().getPackage() != null
					&& m.getReturnType().getPackage().getName().equals("org.docx4j.wml")) {
				todo.add(m.getReturnType());
			}
		}
		while (!todo.isEmpty()) {
			Class<?> c = todo.remove(todo.size() - 1);
			if (!out.add(c)) continue;
			for (Class<?> n : c.getDeclaredClasses()) todo.add(n);
		}
		return out;
	}
}
