package org.docx4j.openpackaging.parts.SpreadsheetML;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.junit.Test;
import org.xlsx4j.sml.CTExternalBook;
import org.xlsx4j.sml.CTExternalLink;

/**
 * Regression test for https://github.com/plutext/docx4j/issues/613:
 * ExternalLinkPart's content type is CTExternalLink (the root JAXBElement is unwrapped
 * on unmarshal, as for every other part), so it must be declared as
 * JaxbSmlPart&lt;CTExternalLink&gt;.  Declared as JaxbSmlPart&lt;JAXBElement&lt;CTExternalLink&gt;&gt;,
 * getJaxbElement() threw ClassCastException.
 */
public class ExternalLinkPartTest {

	@Test
	public void roundTripThroughDeclaredType() throws Exception {

		SpreadsheetMLPackage pkg = SpreadsheetMLPackage.createPackage();

		ExternalLinkPart part = new ExternalLinkPart();
		CTExternalLink link = new CTExternalLink();
		CTExternalBook book = new CTExternalBook();
		book.setId("rId1");
		link.setExternalBook(book);
		part.setJaxbElement(link);           // compiles only with the corrected generic
		pkg.getWorkbookPart().addTargetPart(part);

		CTExternalLink same = part.getJaxbElement();
		assertSame(link, same);

		// marshals as the sml root element, no wrapper needed
		String xml = part.getXML();  // marshals with the part's (sml) context
		assertTrue(xml, xml.contains("externalLink"));

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		pkg.save(baos);
		SpreadsheetMLPackage reloaded = (SpreadsheetMLPackage) OpcPackage.load(new ByteArrayInputStream(baos.toByteArray()));

		RelationshipsPart rels = reloaded.getWorkbookPart().getRelationshipsPart();
		Part p = rels.getPart(rels.getRelationshipByType(Namespaces.SPREADSHEETML_EXTERNAL_LINK));
		assertTrue(p.getClass().getName(), p instanceof ExternalLinkPart);

		// the line from the issue: this threw ClassCastException before the fix
		CTExternalLink again = ((ExternalLinkPart) p).getJaxbElement();
		assertNotNull(again);
		assertEquals("rId1", again.getExternalBook().getId());
	}
}
