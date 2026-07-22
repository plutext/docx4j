package org.docx4j.model.fields;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.JAXBException;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.Text;
import org.junit.Test;

/**
 * FieldsPreprocessor.extractInstr must concatenate w:instrText fragments,
 * since Word may split a field instruction across several runs
 * (eg at proofErr, rsid or formatting boundaries).
 *
 * See https://github.com/plutext/docx4j/issues/645
 */
public class ExtractInstrTests {

	@Test
	public void testConcatenatesFragments() {

		ObjectFactory factory = Context.getWmlObjectFactory();

		List<Object> instructions = new ArrayList<Object>();
		instructions.add(factory.createRInstrText(text(factory, " DOCPROPERTY  my")));
		instructions.add(factory.createRInstrText(text(factory, "prop  \\* MERGEFORMAT ")));

		assertEquals(" DOCPROPERTY  myprop  \\* MERGEFORMAT ",
				FieldsPreprocessor.extractInstr(instructions));
	}

	@Test
	public void testNullOnNonText() {

		ObjectFactory factory = Context.getWmlObjectFactory();

		List<Object> instructions = new ArrayList<Object>();
		instructions.add(factory.createRInstrText(text(factory, " DOCPROPERTY  myprop ")));
		instructions.add(factory.createR()); // eg a nested field

		assertNull(FieldsPreprocessor.extractInstr(instructions));
	}

	@Test
	public void testNullOnEmpty() {

		assertNull(FieldsPreprocessor.extractInstr(new ArrayList<Object>()));
	}

	@Test
	public void testCanonicaliseSplitInstruction() throws JAXBException, IOException {

		List<FieldRef> fieldRefs = new ArrayList<FieldRef>();
		FieldsPreprocessor.canonicalise(getP("Canon_DOCPROPERTY_split_instr.xml"), fieldRefs);

		FieldRef fieldRef = fieldRefs.get(0);
		assertEquals("DOCPROPERTY", fieldRef.getFldName());
		assertEquals(" DOCPROPERTY  myprop  \\* MERGEFORMAT ",
				FieldsPreprocessor.extractInstr(fieldRef.getInstructions()));
	}

	@Test
	public void testFieldUpdaterSplitDocProperty() throws Exception {

		WordprocessingMLPackage wmlPackage = WordprocessingMLPackage.createPackage();
		wmlPackage.addDocPropsCustomPart();
		wmlPackage.getDocPropsCustomPart().setProperty("myprop", "myvalue");

		P p = getP("Canon_DOCPROPERTY_split_instr.xml");
		wmlPackage.getMainDocumentPart().getContent().add(p);
		p.setParent(wmlPackage.getMainDocumentPart().getJaxbElement().getBody());

		FieldUpdater updater = new FieldUpdater(wmlPackage);
		updater.update(false);

		String xml = XmlUtils.marshaltoString(
				wmlPackage.getMainDocumentPart().getJaxbElement(), true, true);
		assertTrue("field result not updated: " + xml, xml.contains("myvalue"));
		assertFalse("stale field result still present: " + xml, xml.contains("TBD"));
	}

	private Text text(ObjectFactory factory, String value) {
		Text t = factory.createText();
		t.setValue(value);
		return t;
	}

	private P getP(String filename) throws JAXBException, IOException {

		return (P)XmlUtils.unmarshal(
				org.docx4j.utils.ResourceUtils.getResource("org/docx4j/model/fields/" + filename));
	}

}
