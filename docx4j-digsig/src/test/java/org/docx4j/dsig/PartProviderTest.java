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
package org.docx4j.dsig;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.PartProvider;
import org.docx4j.openpackaging.parts.digitalsignature.SignatureOriginPart;
import org.docx4j.openpackaging.parts.digitalsignature.XmlSignaturePart;
import org.junit.Test;

/**
 * docx4j-core finds the signature part classes through the PartProvider service
 * (CR-033 phase 2), not by Class.forName: a signed package loads with the typed
 * parts whenever this module is present.
 */
public class PartProviderTest {

	@Test
	public void providerIsRegistered() {
		assertTrue("no PartProvider found; is META-INF/services (or module-info provides) in place?",
				PartProvider.providers().stream().anyMatch(
						p -> p.getClass().getName().equals(
								"org.docx4j.openpackaging.parts.digitalsignature.DigitalSignaturePartProvider")));
	}

	@Test
	public void officeSignedFileLoadsWithTypedParts() throws Exception {
		OpcPackage pkg = OpcPackage.load(new File("src/test/resources/goldens/word2016.docx"));
		Part sig = pkg.getParts().get(new PartName("/_xmlsignatures/sig1.xml"));
		Part origin = pkg.getParts().get(new PartName("/_xmlsignatures/origin.sigs"));
		assertTrue("sig1.xml is " + (sig == null ? "absent" : sig.getClass().getName()),
				sig instanceof XmlSignaturePart);
		assertTrue("origin.sigs is " + (origin == null ? "absent" : origin.getClass().getName()),
				origin instanceof SignatureOriginPart);
	}
}
