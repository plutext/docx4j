package org.docx4j.model.fields;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.xml.transform.TransformerException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(value = Parameterized.class)
public class FormattingSwitchHelperInvalidNumericTests {

	static Locale initialLocale;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		initialLocale=Locale.getDefault();
		Locale.setDefault(Locale.ENGLISH); // decimal delimiter is different in another locale
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		Locale.setDefault(initialLocale);
	}

	@Parameterized.Parameter(value=0)
	public String format;

	@Parameterized.Parameter(value=1)
	public String input;

	@Parameterized.Parameters(name = "{index}: test {1}({0}) is invalid")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][]{
		{"\\# x#.#x", "123456"},
				{"\\# x#.#x", "123.456"},
				{"\\# .###x", "-0.75"},
				{"\\# .###x", "-.75"},
				{"\\# .###", "-0.75"},
				{"\\# .###", "-.75"},
				{"\\# .000", "-0.75"},
				{"\\# .000", "-.75"},
				{"\\# .00", "95.4"},
				{"\\# .##", "95.4"},
				{"\\# #%#", "33"},
				{"\\# #$#", "33"},
				{"\\# #'y'#", "34"}});
	}
	// COMPLEX !

	@Test
	public void testInvalidNumberDetected() throws TransformerException, Docx4JException {
		SwitchTestData data = new SwitchTestData(format, input);
		try {
			doit("MERGEFIELD", data, "class org.docx4j.model.fields.FieldFormattingException");
		} catch (Exception e) {
			System.out.println("[" + data.toString() + "] " + e.getMessage());
			assertTrue(FieldFormattingException.class.isAssignableFrom(e.getClass()));
		}
		try {
			doit("DOCPROPERTY", data, "class org.docx4j.model.fields.FieldFormattingException");
		} catch (Exception e) {
			System.out.println("[" + data.toString() + "] " + e.getMessage());
			assertTrue(FieldFormattingException.class.isAssignableFrom(e.getClass()));
		}
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
