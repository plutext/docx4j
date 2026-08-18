package org.docx4j.fonts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashSet;
import java.util.Set;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.Fonts;
import org.junit.Assume;
import org.junit.Test;

/**
 * BestMatchingMapper used to go straight to the panose match, so a font which is
 * actually installed could be substituted away by another whose panose happened to
 * be closer to the value in the document's font table.  That is how a document
 * asking for (say) Franklin Gothic Demi could be rendered in Franklin Gothic Book.
 *
 * @since 17.0.3
 */
public class BestMatchingMapperTest {

	/** The font the document asks for is the best match for it, whatever the panose says. */
	@Test
	public void testExactNameBeatsPanose() throws Exception {

		// constructing the mapper triggers discovery of the physical fonts
		Mapper mapper = new BestMatchingMapper();

		PhysicalFont wanted = null;
		PhysicalFont other = null;
		for (PhysicalFont pf : PhysicalFonts.getPhysicalFonts().values()) {
			if (pf.getName() == null || pf.getPanose() == null) continue;
			if (PhysicalFonts.get(pf.getName()) == null) continue;  // must be findable by name
			if (wanted == null) {
				wanted = pf;
			} else if (!panoseHex(pf).equals(panoseHex(wanted))) {
				other = pf;
				break;
			}
		}
		Assume.assumeTrue("need two physical fonts with differing panose",
				wanted != null && other != null);

		// a document which asks for 'wanted' by name, but whose font table declares
		// the panose of 'other' - so the panose match alone would give us 'other'
		Set<String> fontsInUse = new HashSet<String>();
		fontsInUse.add(wanted.getName());
		mapper.populateFontMappings(fontsInUse, fontTable(wanted.getName(), panoseHex(other)));

		PhysicalFont mapped = mapper.get(wanted.getName());
		assertNotNull("an installed font was left unmapped", mapped);
		assertEquals("an installed font was substituted away by a panose match",
				wanted.getName(), mapped.getName());
	}

	private static String panoseHex(PhysicalFont pf) {
		StringBuilder sb = new StringBuilder();
		for (byte b : pf.getPanose().getPanoseArray()) {
			sb.append(String.format("%02X", b));
		}
		return sb.toString();
	}

	private static Fonts fontTable(String fontName, String panoseHex) throws Exception {
		return (Fonts)XmlUtils.unmarshalString(
				"<w:fonts xmlns:w=\"" + Namespaces.NS_WORD12 + "\">"
				+ "<w:font w:name=\"" + fontName + "\">"
				+ "<w:panose1 w:val=\"" + panoseHex + "\"/>"
				+ "<w:charset w:val=\"00\"/><w:family w:val=\"swiss\"/><w:pitch w:val=\"variable\"/>"
				+ "</w:font></w:fonts>", Context.jc, Fonts.class);
	}
}
