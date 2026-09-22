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
package org.xlsx4j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.VMLPart;
import org.docx4j.utils.ResourceUtils;
import org.junit.Test;

/**
 * A vmlDrawing part - the legacy drawing a comment, a form control or an OLE
 * object's picture lives in - binds (CR-026). Its root element is
 * {@code <xml>} in no namespace, Office's XML island; until 17.2.1 docx4j's
 * schema declared that global element in a namespace of its own invention, so
 * every such part failed to unmarshal: the contents were unreachable, and a
 * strict package holding one could not be saved at all (the save converts
 * every part, and that one always threw).
 */
public class VmlDrawingPartTest {

	static List<VMLPart> vmlParts(OpcPackage pkg) {
		List<VMLPart> found = new ArrayList<VMLPart>();
		for (Part p : pkg.getParts().getParts().values()) {
			if (p instanceof VMLPart) found.add((VMLPart) p);
		}
		return found;
	}

	static OpcPackage reload(OpcPackage pkg) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		pkg.save(baos);
		return OpcPackage.load(new ByteArrayInputStream(baos.toByteArray()));
	}

	@Test
	public void theShapesAreReachable() throws Exception {
		OpcPackage pkg = OpcPackage.load(ResourceUtils.getResource("anon/comments.xlsx"));
		List<VMLPart> parts = vmlParts(pkg);
		assertEquals(1, parts.size());
		List<Object> any = parts.get(0).getContents().getAny();
		List<String> classes = new ArrayList<String>();
		for (Object o : any) classes.add(XmlUtils.unwrap(o).getClass().getName());
		assertEquals("[org.docx4j.vml.officedrawing.CTShapeLayout, org.docx4j.vml.CTShapetype, org.docx4j.vml.CTShape]",
				classes.toString());
	}

	@Test
	public void aTransitionalPackageRoundTrips() throws Exception {
		OpcPackage pkg = OpcPackage.load(ResourceUtils.getResource("anon/comments.xlsx"));
		VMLPart vml = vmlParts(pkg).get(0);
		vml.getContents(); // unmarshalled, so the save marshals it rather than copying its bytes
		OpcPackage again = reload(pkg);
		String xml = vmlParts(again).get(0).getXML();
		assertTrue(xml, xml.contains("<v:shape") || xml.contains(":shape "));
		assertTrue("the comment's anchor survives", xml.contains("Anchor"));
	}

	@Test
	public void aStrictPackageWithACommentSaves() throws Exception {
		// strict/strict-comments.xlsx is Excel 365's Strict save of docx4j's own anonymised
		// comments.xlsx: a strict package (conformance="strict", purl namespaces) whose
		// vmlDrawing part is, as always, in the ordinary VML namespaces. Before CR-026 this
		// threw: Failed to add parts from relationships of / ... Problem with part
		// /xl/drawings/vmlDrawing1.vml
		OpcPackage pkg = OpcPackage.load(ResourceUtils.getResource("strict/strict-comments.xlsx"));
		assertNotNull(vmlParts(pkg).get(0).getContents());

		OpcPackage again = reload(pkg);
		assertEquals(1, vmlParts(again).size());
		String sheet = ((org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart)
				again.getParts().get(new org.docx4j.openpackaging.parts.PartName("/xl/worksheets/sheet1.xml"))).getXML();
		assertFalse("saved transitional", sheet.contains("purl.oclc.org"));
		String vml = vmlParts(again).get(0).getXML();
		assertTrue(vml, vml.contains("ClientData"));
		assertFalse("the VML part has no strict dialect", vml.contains("purl.oclc.org"));
	}

}
