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
package org.docx4j.openpackaging.parts.digitalsignature;

import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.PartProvider;

/**
 * Gives docx4j-core the two signature part classes, so that a signed package
 * loads with an {@link XmlSignaturePart} and a {@link SignatureOriginPart}
 * whenever this module is present.  Registered in module-info and in
 * META-INF/services; nothing calls it directly.
 *
 * @since 17.3.2 (CR-033 phase 2)
 */
public class DigitalSignaturePartProvider implements PartProvider {

	@Override
	public Part createPart(String contentType, PartName partName) throws Exception {
		if (ContentTypes.DIGITAL_SIGNATURE_XML_SIGNATURE_PART.equals(contentType)) {
			return new XmlSignaturePart(partName);
		}
		if (ContentTypes.DIGITAL_SIGNATURE_ORIGIN_PART.equals(contentType)) {
			return new SignatureOriginPart(partName);
		}
		return null;
	}

}
