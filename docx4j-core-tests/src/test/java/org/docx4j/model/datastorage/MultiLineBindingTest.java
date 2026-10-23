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
package org.docx4j.model.datastorage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;

/**
 * CR-059 D4: a plain-text control bound to a value holding a newline, as Word 365 shows it (the CR's phase 0 probe,
 * Jason, 2026-10-24, the fixture here). With {@code w:multiLine="1"} the newline is a line break; without
 * {@code multiLine}, or with {@code "0"}, Word drops it and the two lines run together, {@code ML_LINE_ONEML_LINE_TWO}.
 * Until 17.3.5 the NonXSLT traverser read the absent attribute as true (the schema's 2008 default) and wrote a
 * {@code w:br} where Word and the XSLT traverser do not.
 */
public class MultiLineBindingTest {

	private static final String IMPL_PROPERTY = "docx4j.model.datastorage.BindingHandler.Implementation";

	private static String bound(String implementation) throws Exception {
		Docx4jProperties.setProperty(IMPL_PROPERTY, implementation);
		try {
			WordprocessingMLPackage pkg = WordprocessingMLPackage.load(new java.io.File(
					System.getProperty("user.dir") + "/src/test/resources/OpenDoPE/text-control-multiline.docx"));
			Docx4J.bind(pkg, (org.w3c.dom.Document) null, Docx4J.FLAG_BIND_BIND_XML);
			return XmlUtils.marshaltoString(pkg.getMainDocumentPart().getJaxbElement(), true, false);
		} finally {
			Docx4jProperties.setProperty(IMPL_PROPERTY, "BindingTraverserXSLT");
		}
	}

	/** The control's content, its runs' properties and run tags aside. */
	private static String content(String xml, String alias) {
		Matcher m = Pattern.compile("<w:sdt>(?:(?!<w:sdt>).)*?<w:alias w:val=\"" + alias + "\"/>.*?<w:sdtContent>(.*?)</w:sdtContent>", Pattern.DOTALL).matcher(xml);
		assertTrue(alias, m.find());
		return m.group(1).replaceAll("<w:rPr>.*?</w:rPr>", "").replaceAll("</?w:r>", "").replaceAll(" xml:space=\"preserve\"", "");
	}

	@Test
	public void aNewlineIsALineBreakOnlyWithMultiLine() throws Exception {
		for (String implementation : new String[] { "BindingTraverserXSLT", "BindingTraverserNonXSLT" }) {
			String xml = bound(implementation);
			assertEquals(implementation + " ML1, no multiLine", "<w:t>ML_LINE_ONEML_LINE_TWO</w:t>", content(xml, "ML1"));
			assertEquals(implementation + " ML2, multiLine=1", "<w:t>ML_LINE_ONE</w:t><w:br/><w:t>ML_LINE_TWO</w:t>", content(xml, "ML2"));
			assertEquals(implementation + " ML3, multiLine=0", "<w:t>ML_LINE_ONEML_LINE_TWO</w:t>", content(xml, "ML3"));
		}
	}
}
