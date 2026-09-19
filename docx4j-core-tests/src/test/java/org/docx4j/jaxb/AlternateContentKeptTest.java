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
package org.docx4j.jaxb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.mce.AlternateContent;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.utils.ResourceUtils;
import org.docx4j.wml.Numbering;
import org.docx4j.wml.P;
import org.junit.Test;

/**
 * CR-021 phase 2: the schema admits mc:AlternateContent wherever Word writes it, so
 * load keeps both branches instead of resolving the element to one - in w:p (a
 * drawing group Word writes as a paragraph child, its Choice holding a w:r/w:drawing)
 * and in w:numPicBullet (Word 365: a Choice Requires="v" holding w:pict, a Fallback
 * holding w:drawing) as it already did in w:r.  Save writes both branches back.
 */
public class AlternateContentKeptTest {

	private static final String NS = " xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\""
			+ " xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\""
			+ " xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\""
			+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
			+ " xmlns:wpg=\"http://schemas.microsoft.com/office/word/2010/wordprocessingGroup\""
			+ " xmlns:v=\"urn:schemas-microsoft-com:vml\"";

	/** The shape of Word's own re-save of a paragraph-level drawing group (CR-021 §8.1). */
	private static final String PARAGRAPH = "<w:p" + NS + "><w:pPr><w:ind w:right=\"560\"/></w:pPr>"
			+ "<mc:AlternateContent><mc:Choice Requires=\"wpg\"><w:r><w:rPr><w:noProof/></w:rPr><w:drawing>"
			+ "<wp:anchor distT=\"0\" distB=\"0\" distL=\"114300\" distR=\"114300\" simplePos=\"0\" relativeHeight=\"251659264\" behindDoc=\"0\" locked=\"0\" layoutInCell=\"1\" allowOverlap=\"1\">"
			+ "<wp:simplePos x=\"0\" y=\"0\"/><wp:positionH relativeFrom=\"margin\"><wp:posOffset>0</wp:posOffset></wp:positionH>"
			+ "<wp:positionV relativeFrom=\"paragraph\"><wp:posOffset>0</wp:posOffset></wp:positionV>"
			+ "<wp:extent cx=\"1181100\" cy=\"1485900\"/><wp:effectExtent l=\"0\" t=\"0\" r=\"0\" b=\"0\"/><wp:wrapNone/>"
			+ "<wp:docPr id=\"2\" name=\"Group 2\"/><a:graphic><a:graphicData uri=\"http://schemas.microsoft.com/office/word/2010/wordprocessingGroup\">"
			+ "<wpg:wgp><wpg:cNvGrpSpPr/><wpg:grpSpPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"1181100\" cy=\"1485900\"/>"
			+ "<a:chOff x=\"0\" y=\"0\"/><a:chExt cx=\"1181100\" cy=\"1485900\"/></a:xfrm></wpg:grpSpPr></wpg:wgp>"
			+ "</a:graphicData></a:graphic></wp:anchor></w:drawing></w:r></mc:Choice>"
			+ "<mc:Fallback><w:r><w:rPr><w:noProof/></w:rPr><w:pict><v:group id=\"Group 2\" style=\"position:absolute\"/></w:pict></w:r></mc:Fallback>"
			+ "</mc:AlternateContent>"
			+ "<w:r><w:t>after the group</w:t></w:r></w:p>";

	private static AlternateContent alternateContentIn(List<Object> content) {
		for (Object o : content) {
			Object u = XmlUtils.unwrap(o);
			if (u instanceof AlternateContent) return (AlternateContent) u;
		}
		return null;
	}

	@Test
	public void paragraphLevelElementIsKeptWithBothBranches() throws Exception {
		P p = (P) XmlUtils.unwrap(XmlUtils.unmarshalString(PARAGRAPH, Context.jc, P.class));
		AlternateContent ac = alternateContentIn(p.getContent());
		assertNotNull("mc:AlternateContent kept as a child of w:p", ac);
		assertEquals(1, ac.getChoice().size());
		assertEquals("wpg", ac.getChoice().get(0).getRequires());
		assertNotNull(ac.getFallback());
		assertEquals("the paragraph's other run is still there", 2, p.getContent().size());

		String xml = XmlUtils.marshaltoString(p, true, false, Context.jc);
		assertTrue(xml, xml.contains("<mc:Choice Requires=\"wpg\">"));
		assertTrue(xml, xml.contains("<mc:Fallback>"));
		assertTrue("the Choice's drawing group survives", xml.contains("<wpg:wgp>"));
		assertTrue("the Fallback's VML group survives", xml.contains("<v:group"));
	}

	@Test
	public void numPicBulletElementIsKeptAndRoundTrips() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(
				ResourceUtils.getResource("numPicBullet-word365-AlternateContent.docx"));
		Numbering numbering = pkg.getMainDocumentPart().getNumberingDefinitionsPart().getContents();
		assertEquals(1, numbering.getNumPicBullet().size());
		Numbering.NumPicBullet bullet = numbering.getNumPicBullet().get(0);
		assertNull("neither branch is unwrapped into the bullet itself", bullet.getPict());
		assertNull(bullet.getDrawing());
		AlternateContent ac = bullet.getAlternateContent();
		assertNotNull("Word 365's mc:AlternateContent is kept in w:numPicBullet", ac);
		assertEquals("v", ac.getChoice().get(0).getRequires());
		assertNotNull(ac.getFallback());
		assertTrue("the Choice holds the VML picture",
				XmlUtils.unwrap(ac.getChoice().get(0).getAny().get(0)) instanceof org.docx4j.wml.Pict);
		assertTrue("the Fallback holds the DrawingML picture",
				XmlUtils.unwrap(ac.getFallback().getAny().get(0)) instanceof org.docx4j.wml.Drawing);

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		Docx4J.save(pkg, bos);
		WordprocessingMLPackage again = WordprocessingMLPackage.load(new ByteArrayInputStream(bos.toByteArray()));
		Numbering.NumPicBullet reloaded = again.getMainDocumentPart().getNumberingDefinitionsPart().getContents()
				.getNumPicBullet().get(0);
		assertNotNull("both branches are written back", reloaded.getAlternateContent());
		assertEquals(1, reloaded.getAlternateContent().getChoice().size());
		assertNotNull(reloaded.getAlternateContent().getFallback());
	}
}
