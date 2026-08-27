/*
 *  Copyright 2007-2026, Plutext Pty Ltd.
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

package org.docx4j.samples;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.model.datastorage.DocxFetcher;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.w3c.dom.Document;


/**
 * This sample demonstrates the OpenDoPE component model:
 * a content control tagged od:component=id references a
 * separate docx (a reusable sub-template), which is fetched
 * and merged into the host document before data binding runs.
 *
 * See docs/OpenDoPE_Components.md for how this works.
 *
 * The host here is sample-docs/databinding/component-host.xml
 * (a Flat OPC package; docx4j loads that format transparently).
 * Its components part maps component id "comp1" to the IRI
 * "component-subdoc.docx"; the DocxFetcher below resolves that
 * IRI relative to the sample-docs/databinding directory.
 *
 * The component contains a content control bound to
 * /yourxml/magic.  The data we inject into the host at bind
 * time deliberately uses a different magic word ("xyzzy") to
 * the one saved in the component ("abracadabra"), to prove the
 * component's binding really is re-evaluated against the host's
 * data.
 *
 * NB: merging the fetched component into real WordML is
 * delegated to the commercial MergeDocx extension.  Without it
 * on your classpath, the component remains in the output as a
 * w:altChunk (Word will flatten it when you open the document,
 * but the binding won't be re-evaluated).
*/
public class ContentControlBindingComponents {

	public static void main(String[] args) throws Exception {

		String inputDir = System.getProperty("user.dir") + "/sample-docs/databinding/";

		String input_DOCX = inputDir + "component-host.xml"; // Flat OPC

		// The runtime data; /yourxml/magic is what the component binds to
		String input_XML = "<yourxml>goes here<magic>xyzzy</magic></yourxml>";

		// resulting docx
		String OUTPUT_DOCX = System.getProperty("user.dir") + "/OUT_ComponentsProcessed.docx";

		// Component processing is OFF by default; turn it on
		Docx4jProperties.setProperty(
				"docx4j.model.datastorage.OpenDoPEHandlerComponents.enabled", true);

		// Load the host template
		WordprocessingMLPackage wordMLPackage = Docx4J.load(new File(input_DOCX));

		// A DocxFetcher resolves a component's IRI to the bytes of
		// the component docx.  The IRI scheme is yours to define; here
		// an IRI is just a filename in the sample-docs/databinding dir.
		DocxFetcher docxFetcher = new DocxFetcher() {
			@Override
			public InputStream getDocxFromIRI(String iri) throws Docx4JException {
				try {
					return new FileInputStream(new File(inputDir, iri));
				} catch (Exception e) {
					throw new Docx4JException("Can't fetch component " + iri, e);
				}
			}
		};

		Document xmlDoc = XmlUtils.getNewDocumentBuilder().parse(
				new java.io.ByteArrayInputStream(input_XML.getBytes("UTF-8")));

		// Do the binding; only the signatures taking a DocxFetcher
		// support component processing.
		// FLAG_BIND_INSERT_XML: inject the passed XML into the document
		// FLAG_BIND_BIND_XML: bind the document and the xml (components are
		//                     fetched and merged at the start of this step)
		Docx4J.bind(wordMLPackage, xmlDoc,
				Docx4J.FLAG_BIND_INSERT_XML | Docx4J.FLAG_BIND_BIND_XML,
				docxFetcher);

		//Save the document
		Docx4J.save(wordMLPackage, new File(OUTPUT_DOCX), Docx4J.FLAG_NONE);
		System.out.println("Saved: " + OUTPUT_DOCX);
	}

}
