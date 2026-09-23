/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.docx4j.jaxb;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.wml.CTSettings;
import org.junit.Test;

/**
 * The tail of a settings part is written in Word's order:
 * w14:docId, w15:chartTrackingRefBased, w15:docId. Until 17.2.1 the schema
 * declared w15:chartTrackingRefBased first, so docx4j's save reordered the
 * three (Word opens either, but a port generating from the schema should not
 * have to know that).
 */
public class SettingsDocIdOrderTest {

	private static final String W14 = "http://schemas.microsoft.com/office/word/2010/wordml";
	private static final String W15 = "http://schemas.microsoft.com/office/word/2012/wordml";

	@Test
	public void wordsOrderIsKept() throws Exception {
		// Word Online's order on input: chartTrackingRefBased first
		String openXML = "<w:settings mc:Ignorable=\"w14 w15\" "
				+ "xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\" "
				+ "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" "
				+ "xmlns:w14=\"" + W14 + "\" xmlns:w15=\"" + W15 + "\">"
				+ "<w:listSeparator w:val=\",\"/>"
				+ "<w15:chartTrackingRefBased/>"
				+ "<w14:docId w14:val=\"4BAC4F0A\"/>"
				+ "<w15:docId w15:val=\"{78FB31FD-2D0A-4042-95DA-DFD2E5520F96}\"/>"
				+ "</w:settings>";

		DocumentSettingsPart dsp = new DocumentSettingsPart();
		dsp.setContents((CTSettings) XmlUtils.unwrap(XmlUtils.unmarshalString(openXML)));

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		dsp.marshal(baos);
		String result = baos.toString("UTF-8");

		int docId14 = result.indexOf(":docId w14:val");
		int chart = result.indexOf(":chartTrackingRefBased");
		int docId15 = result.indexOf(":docId w15:val");
		assertTrue("w14:docId missing: " + result, docId14 > 0);
		assertTrue("w15:chartTrackingRefBased missing: " + result, chart > 0);
		assertTrue("w15:docId missing: " + result, docId15 > 0);
		assertTrue("expected w14:docId, w15:chartTrackingRefBased, w15:docId: " + result,
				docId14 < chart && chart < docId15);
	}
}
