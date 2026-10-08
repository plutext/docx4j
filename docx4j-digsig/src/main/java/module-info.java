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
module org.docx4j.digsig {

	requires transitive org.docx4j.core;
	requires org.docx4j.generated_objects;
	requires jakarta.xml.bind;
	requires org.slf4j;
	requires java.xml;
	requires java.xml.crypto;
	requires java.desktop;

	// automatic modules: the name is from the manifest (Santuario) or the jar's file name (TextImageGen)
	requires org.apache.santuario.xmlsec;
	requires TextImageGen;

	requires org.bouncycastle.provider;
	requires org.bouncycastle.pkix;
	requires org.bouncycastle.util;

	exports org.docx4j.dsig;
	exports org.docx4j.dsig.anchor;
	exports org.docx4j.dsig.crypt;
	exports org.docx4j.dsig.crypt.facets;
	exports org.docx4j.dsig.crypt.services;
	exports org.docx4j.openpackaging.parts.digitalsignature;

	exports org.docx4j.org.etsi.uri.x01903.v13;
	exports org.docx4j.org.etsi.uri.x01903.v141;
	exports org.docx4j.com.microsoft.schemas.office.x2006.digsig;
	exports org.docx4j.org.openxmlformats.schemas.xpackage.x2006.digitalSignature;

	opens org.docx4j.org.etsi.uri.x01903.v13 to jakarta.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
	opens org.docx4j.org.etsi.uri.x01903.v141 to jakarta.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
	opens org.docx4j.com.microsoft.schemas.office.x2006.digsig to jakarta.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
	opens org.docx4j.org.openxmlformats.schemas.xpackage.x2006.digitalSignature to jakarta.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;

}
