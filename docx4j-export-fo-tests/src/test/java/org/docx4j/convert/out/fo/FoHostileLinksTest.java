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
package org.docx4j.convert.out.fo;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.docx4j.Docx4J;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.*;
import org.junit.Test;

/**
 * CR-056 phase 0: the FO output writes no {@code external-destination} whose target is a script scheme
 * (measured 2026-10-23 on 17.3.4: {@code url(javascript:alert(1))}, which FOP writes into the PDF as a
 * URI action); a benign target keeps its link. Red until phase 1.
 */
public class FoHostileLinksTest {

	private static final ObjectFactory F = Context.getWmlObjectFactory();

	static WordprocessingMLPackage docx() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		for (String target : new String[] { "javascript:alert(1)", " \u0001java\tscri\npt:alert(2)", "vbscript:MsgBox(3)", "https://example.org/ok", "#bookmark1", "sub/page.html", "javascript&#58;alert(8)" }) {
			Relationship rel = new org.docx4j.relationships.ObjectFactory().createRelationship();
			rel.setType(Namespaces.HYPERLINK); rel.setTarget(target); rel.setTargetMode("External");
			mdp.getRelationshipsPart().addRelationship(rel);
			P p = F.createP(); P.Hyperlink h = F.createPHyperlink(); h.setId(rel.getId());
			R r = F.createR(); Text t = F.createText(); t.setValue("text for " + target.trim()); r.getContent().add(t);
			h.getContent().add(r); p.getContent().add(h); mdp.getContent().add(p);
		}
		return pkg;
	}

	static String fo() throws Exception {
		FOSettings settings = Docx4J.createFOSettings();
		settings.setOpcPackage(docx());
		settings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		Docx4J.toFO(settings, os, Docx4J.FLAG_NONE);
		return os.toString(StandardCharsets.UTF_8);
	}

	@Test
	public void noScriptSchemeExternalDestination() throws Exception {
		String fo = fo();
		String lower = fo.toLowerCase();
		assertFalse("javascript: external-destination\n" + fo, lower.contains("url(javascript:"));
		assertFalse("sneaky javascript: external-destination\n" + fo, lower.contains("url(java") || lower.contains("url(\u0001"));
		assertFalse("vbscript: external-destination\n" + fo, lower.contains("url(vbscript:"));
		assertFalse("entity-encoded javascript: external-destination (CR-056 §14)\n" + fo, lower.contains("url(javascript&#58;") || lower.contains("url(javascript&amp;#58;"));
		assertTrue("the entity-encoded link's text kept\n" + fo, fo.contains("text for javascript&amp;#58;alert(8)") || fo.contains("text for javascript&#58;alert(8)"));
		assertTrue("the text kept\n" + fo, fo.contains("text for javascript:alert(1)"));
	}

	@Test
	public void benignLinksKept() throws Exception {
		String fo = fo();
		assertTrue(fo, fo.contains("external-destination=\"url(https://example.org/ok)\""));
	}
}
