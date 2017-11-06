package org.docx4j.model.fields;

import static org.junit.Assert.assertTrue;

import javax.xml.transform.TransformerException;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@RunWith(value = Parameterized.class)
public class FormattingSwitchHelperNoSwitchTests {

	// In all cases, output = input

	@Parameterized.Parameter(value=0)
	public String value;

	@Parameterized.Parameters(name = "{index}: test {0} parsed as is")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][]{
				{"mary smith"},
				{"mary\"smith"},
				{"\"mary smith\""},
				{"\"marysmith\""},
				{"\"mary smith\""},
				{"\"Mary Smith\""},
				{"\"Mary Smith\""},
				{"\"Mary Smith\" Capoop"},
				{"\"Mary SmiTH\""},
				{"\"mary SmiTH\""},
				{"mary smith"},
				{"Mary SmiTH"},
				{"\"mary smith\""},
				{"01"},
				{"0.1"},
				{"0.0"},
				{"0.00"},
				{"0."},
				{"\"0.1 A\""},
				{"\"0.1 1\""},
				{"\"0.1 .\""},
				{"\"0.00 0\""},
				{"\"0.00 1\""},
				{"\"0.00 A\""},
				{"0.1 A"},
				{"0.1 1"},
				{"0.1 ."},
				{"0.00 0"},
				{"0.00 1"},
				{"0.00 A"},
				{"0000123456"},
				{"000012345.006"},
				{"0000123AA456"},
				{"0000123AA45.006"},
				{"123"},
				{"-123"},
				{"123."},
				{"-123."},
				{"0"},
				{"00"},
				{"0."},
				{"01"},
				{"0.1"},
				{"0.0"},
				{"0.00"},
				{"0.00 0"},
				{"0.00 1"},
				{"0.00 A"},
				{"0.00 W"},
				{"\"01\""},
				{"\"0.1\""},
				{"\"0.0\""},
				{"\"0.00\""},
				{"\"0.00 0\""},
				{"\"0.00 1\""},
				{"\"0.00 A\""},
				{"\"0.00 W\""},
				{"0000123456"},
				{"000012345.006"},
				{"0000123AA456"},
				{"0000123AA45.006"}
		});
	}

	@Test
	public void testDataParsedAsIs()  throws TransformerException, Docx4JException {
		SwitchTestData data = new SwitchTestData("", value);
		doit("MERGEFIELD", data, value);
		doit("DOCPROPERTY", data, value);
	}


	// ---------------------------------------------------------------------------------------

	private void doit(String fieldname, SwitchTestData triple, String expectedResult)  throws TransformerException, Docx4JException {

		String instr = fieldname + " foo " + triple.format;
		String result = getFormat(instr, triple.val);
		assertTrue(result.equals(expectedResult));
	}

	private String getFormat(String instr, String val) throws TransformerException, Docx4JException {

		FldSimpleModel fsm = new FldSimpleModel();
		fsm.build(instr);
		return FormattingSwitchHelper.applyFormattingSwitch(null, fsm, val);
	}

	private static class SwitchTestData {

		String format;
		String val;

		public String toString() {
			return "format " + format + " to data " + val;
		}

		public SwitchTestData(String format, String val) {

			this.format = format;
			this.val = val;
		}
	}


}
