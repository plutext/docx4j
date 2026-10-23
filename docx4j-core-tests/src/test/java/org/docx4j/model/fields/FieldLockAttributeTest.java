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
package org.docx4j.model.fields;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.wml.CTDocProtect;
import org.docx4j.wml.CTSettings;
import org.docx4j.wml.CTSimpleField;
import org.docx4j.wml.FldChar;
import org.docx4j.wml.P;
import org.docx4j.wml.STDocProtect;
import org.junit.Test;

/**
 * Issue 697: an absent {@code w:fldLock}, {@code w:dirty} (on {@code w:fldChar} and {@code w:fldSimple})
 * or {@code w:enforcement} (on {@code w:documentProtection}) means off. ECMA-376 gives none of them a
 * default; docx4j's schema copy had {@code default="true"}, so until 17.3.5 the generated getters read
 * every field as locked and dirty and every protection as enforced.
 */
public class FieldLockAttributeTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static FldChar fldChar(String attributes) throws Exception {
		return (FldChar) XmlUtils.unwrap(XmlUtils.unmarshalString("<w:fldChar " + W + " w:fldCharType=\"begin\"" + attributes + "/>"));
	}

	private static CTSimpleField fldSimple(String attributes) throws Exception {
		return (CTSimpleField) XmlUtils.unwrap(XmlUtils.unmarshalString("<w:fldSimple " + W + " w:instr=\" PAGE \"" + attributes + "/>"));
	}

	private static CTDocProtect protection(String attributes) throws Exception {
		CTSettings settings = (CTSettings) XmlUtils.unwrap(XmlUtils.unmarshalString(
				"<w:settings " + W + "><w:documentProtection w:edit=\"readOnly\"" + attributes + "/></w:settings>"));
		return settings.getDocumentProtection();
	}

	@Test
	public void anAbsentAttributeIsOff() throws Exception {
		assertFalse(fldChar("").isFldLock());
		assertFalse(fldChar("").isDirty());
		assertFalse(fldSimple("").isFldLock());
		assertFalse(fldSimple("").isDirty());
		assertFalse(protection("").isEnforcement());
	}

	@Test
	public void aWrittenAttributeIsReadAsWritten() throws Exception {
		assertTrue(fldChar(" w:fldLock=\"true\"").isFldLock());
		assertTrue(fldChar(" w:fldLock=\"1\" w:dirty=\"1\"").isDirty());
		assertFalse(fldChar(" w:fldLock=\"false\"").isFldLock());
		assertTrue(fldSimple(" w:fldLock=\"1\"").isFldLock());
		assertTrue(fldSimple(" w:dirty=\"true\"").isDirty());
		assertTrue(protection(" w:enforcement=\"1\"").isEnforcement());
		assertFalse(protection(" w:enforcement=\"0\"").isEnforcement());
	}

	/** The absent attribute stays absent when the part is saved: the default is not written out. */
	@Test
	public void anAbsentAttributeIsNotWritten() throws Exception {
		String xml = XmlUtils.marshaltoString(fldChar(""), true, false);
		assertFalse(xml, xml.contains("fldLock") || xml.contains("dirty"));
	}

	/** FieldRef reads the lock and the dirty flag from the begin w:fldChar. */
	@Test
	public void aComplexFieldWithoutFldLockIsNotLocked() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		P p = (P) XmlUtils.unmarshalString("<w:p " + W + ">"
				+ "<w:r><w:fldChar w:fldCharType=\"begin\"/></w:r><w:r><w:instrText xml:space=\"preserve\"> PAGE </w:instrText></w:r>"
				+ "<w:r><w:fldChar w:fldCharType=\"separate\"/></w:r><w:r><w:t>1</w:t></w:r><w:r><w:fldChar w:fldCharType=\"end\"/></w:r>"
				+ "<w:r><w:fldChar w:fldCharType=\"begin\" w:fldLock=\"1\"/></w:r><w:r><w:instrText xml:space=\"preserve\"> PAGE </w:instrText></w:r>"
				+ "<w:r><w:fldChar w:fldCharType=\"separate\"/></w:r><w:r><w:t>1</w:t></w:r><w:r><w:fldChar w:fldCharType=\"end\"/></w:r>"
				+ "</w:p>");
		pkg.getMainDocumentPart().getContent().add(p);
		List<FieldRef> fieldRefs = new ArrayList<FieldRef>();
		FieldsPreprocessor.canonicalise(p, fieldRefs);
		assertTrue(fieldRefs.size() == 2);
		assertFalse("unlocked", fieldRefs.get(0).isLock());
		assertFalse("not dirty", fieldRefs.get(0).isDirty());
		assertTrue("locked as written", fieldRefs.get(1).isLock());
	}

	/**
	 * A locked MERGEFIELD keeps its result through FieldUpdater, formatting and all. Until 17.3.5
	 * FieldRef.isLock() was false for every field FieldsPreprocessor met, so the result was dropped as
	 * an unlocked MERGEFIELD's is.
	 */
	@Test
	public void aLockedMergefieldKeepsItsResult() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		P p = (P) XmlUtils.unmarshalString("<w:p " + W + ">"
				+ "<w:r><w:fldChar w:fldCharType=\"begin\" w:fldLock=\"1\"/></w:r><w:r><w:instrText xml:space=\"preserve\"> MERGEFIELD name \\* MERGEFORMAT </w:instrText></w:r>"
				+ "<w:r><w:fldChar w:fldCharType=\"separate\"/></w:r><w:r><w:rPr><w:b/></w:rPr><w:t>«name»</w:t></w:r><w:r><w:fldChar w:fldCharType=\"end\"/></w:r>"
				+ "</w:p>");
		pkg.getMainDocumentPart().getContent().add(p);
		new FieldUpdater(pkg).update(true);
		String xml = XmlUtils.marshaltoString(pkg.getMainDocumentPart().getContent().get(0), true, false);
		assertTrue(xml, xml.contains("«name»") && xml.contains("<w:b/>"));
	}

	/** A documentProtection without w:enforcement is not in force (ECMA-376 17.15.1.29). */
	@Test
	public void protectionWithoutEnforcementIsNotRestrictEditing() throws Exception {
		DocumentSettingsPart dsp = new DocumentSettingsPart();
		dsp.setJaxbElement((CTSettings) XmlUtils.unwrap(XmlUtils.unmarshalString(
				"<w:settings " + W + "><w:documentProtection w:edit=\"readOnly\"/></w:settings>")));
		assertFalse(dsp.isRestrictEditingWith(STDocProtect.READ_ONLY));
		dsp.getJaxbElement().getDocumentProtection().setEnforcement(Boolean.TRUE);
		assertTrue(dsp.isRestrictEditingWith(STDocProtect.READ_ONLY));
	}
}
