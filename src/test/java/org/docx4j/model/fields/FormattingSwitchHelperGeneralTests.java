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
public class FormattingSwitchHelperGeneralTests extends FormattingTestsBase{

	// For DOCPROPERTY and MERGEFIELD, = (and presumably all others), \* without arg in Word 2010 sp1
	// results in Error! Switch argument not specified, .

	@Parameterized.Parameter(value=0)
	public String format;

	@Parameterized.Parameter(value=1)
	public String input;

	@Parameterized.Parameter(value=2)
	public String result;

	@Parameterized.Parameters(name = "{index}: test {1}({0}) parsed as {2}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][]{
				{"\\* ", "mary smith", "Error! Switch argument not specified."},
				{"\\* Caps", "mary smith", "Mary Smith"},
				{"\\* Upper", "mary smith", "MARY SMITH"},
				{"\\* MERGEFORMAT", "Mary SmiTH", "Mary SmiTH"},
				// it was commented out
				//{"\\* madeupswitch", "\"mary smith\"", "Error! Unknown switch argument"},
				//{"\\* madeupswitch", "mary smith", "Error! Unknown switch argument"},
				{"\\* MERGEFORMAT", "01", "01"},
				{"\\* MERGEFORMAT", "0.1", "0.1"},
				{"\\* MERGEFORMAT", "0.0", "0.0"},
				{"\\* MERGEFORMAT", "0.00", "0.00"},
				{"\\* MERGEFORMAT", "0.", "0."},
				{"\\* MERGEFORMAT", "0.1 A", "0.1 A"},
				{"\\* MERGEFORMAT", "0.1 1", "0.1 1"},
				{"\\* MERGEFORMAT", "0.1 .", "0.1 ."},
				{"\\* MERGEFORMAT", "0.00 0", "0.00 0"},
				{"\\* MERGEFORMAT", "0.00 1", "0.00 1"},
				{"\\* MERGEFORMAT", "0.00 A", "0.00 A"},
				{"\\* MERGEFORMAT", "0000123456", "0000123456"},
				{"\\* MERGEFORMAT", "000012345.006", "000012345.006"},
				{"\\* MERGEFORMAT", "0000123AA456", "0000123AA456"},
				{"\\* MERGEFORMAT", "0000123AA45.006", "0000123AA45.006"},
				{"\\* Caps", "\"mary smith\"", "\"Mary Smith\""},
				{"\\* Caps", "\"marysmith\"", "\"Marysmith\""},
				{"\\* FirstCap", "\"mary smith\"", "\"Mary smith\""},
				{"\\* Lower", "\"Mary Smith\"", "\"mary smith\""},
				{"\\* Upper", "\"Mary Smith\"", "\"MARY SMITH\""},
				{"\\* MERGEFORMAT", "\"Mary SmiTH\"", "\"Mary SmiTH\""},
				{"\\* MERGEFORMAT", "\"mary SmiTH\"", "\"mary SmiTH\""},
				{"\\* MERGEFORMAT", "\"0.1 A\"", "\"0.1 A\""},
				{"\\* MERGEFORMAT", "\"0.1 1\"", "\"0.1 1\""},
				{"\\* MERGEFORMAT", "\"0.1 .\"", "\"0.1 .\""},
				{"\\* MERGEFORMAT", "\"0.00 0\"", "\"0.00 0\""},
				{"\\* MERGEFORMAT", "\"0.00 1\"", "\"0.00 1\""},
				{"\\* MERGEFORMAT", "\"0.00 A\"", "\"0.00 A\""}		});
	}

	@Test
	public void testDateParsed()throws TransformerException, Docx4JException {
		SwitchTestData triple = new SwitchTestData(format, input);
		doit("MERGEFIELD", triple, result);
		doit("DOCPROPERTY", triple, result);
	}
}
