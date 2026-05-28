package org.docx4j;

import jakarta.xml.bind.Marshaller;
import org.docx4j.jaxb.NamespacePrefixMapperUtils;
import org.junit.Test;
import org.xlsx4j.jaxb.Context;
import org.xlsx4j.sml.SheetData;
import org.xlsx4j.sml.Worksheet;

import java.io.StringWriter;

import static org.junit.Assert.assertTrue;

public class SpreadsheetMLNamespacePrefixTest {

	@Test
	public void testWorksheetUsesDefaultSpreadsheetmlNamespace() throws Exception {
		Worksheet worksheet = new Worksheet();
		worksheet.setSheetData(new SheetData());

		Marshaller marshaller = Context.jcSML.createMarshaller();
		NamespacePrefixMapperUtils.setProperty(marshaller, NamespacePrefixMapperUtils.getPrefixMapper());

		StringWriter stringWriter = new StringWriter();
		marshaller.marshal(worksheet, stringWriter);

		String xml = stringWriter.toString();
		assertTrue("Main SpreadsheetML namespace must be declared as default namespace", xml.contains("xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\""));
	}
}
