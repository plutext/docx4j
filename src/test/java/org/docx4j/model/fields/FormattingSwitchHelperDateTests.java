package org.docx4j.model.fields;

import org.docx4j.Docx4jProperties;
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

@RunWith(value = Parameterized.class)
public class FormattingSwitchHelperDateTests extends FormattingTestsBase {

	static boolean wasDateFormatInferencerUSA = false;
	static Locale initialLocale;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// Tests assume USA date format
		wasDateFormatInferencerUSA = Docx4jProperties.getProperty("docx4j.Fields.Dates.DateFormatInferencer.USA", false);
		Docx4jProperties.setProperty("docx4j.Fields.Dates.DateFormatInferencer.USA", true);
		initialLocale=Locale.getDefault();
		Locale.setDefault(Locale.ENGLISH); // otherwise dates will be formatted using local language standard
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		Docx4jProperties.setProperty("docx4j.Fields.Dates.DateFormatInferencer.USA", wasDateFormatInferencerUSA);
		Locale.setDefault(initialLocale);
	}

	@Parameterized.Parameter(value=0)
	public String format;

	@Parameterized.Parameter(value=1)
	public String input;

	@Parameterized.Parameter(value=2)
	public String result;

	@Parameterized.Parameters(name = "{index}: test {1}({0}) parsed as {2}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][]{
				{"\\@  ", "4/15/2013","Error! Switch argument not specified."},
				{"\\@ M/d/yyyy", "4/15/2013","4/15/2013"},
				{"\\@ \"dddd, MMMM dd, yyyy\"", "4/15/2013","Monday, April 15, 2013"},
				{"\\@ \"MMMM d, yyyy\"", "4/15/2013","April 15, 2013"},
				{"\\@ M/d/yy", "4/15/2013","4/15/13"},
				{"\\@ yyyy-MM-dd", "4/15/2013","2013-04-15"},
				{"\\@ d-MMM-yy", "4/15/2013","15-Apr-13"},
				{"\\@ M.d.yyyy", "4/15/2013","4.15.2013"},
				{"\\@ \"MMM. d, yy\"", "4/15/2013","Apr. 15, 13"},
				{"\\@ \"d MMMM yyyy\"", "4/15/2013","15 April 2013"},
				{"\\@ \"MMMM yy\"", "4/15/2013","April 13"},
				{"\\@ MMM-yy", "4/15/2013","Apr-13"},
				{"\\@ \"dddd, MMMM dd, yyyy\"", "4/15/2013","Monday, April 15, 2013"},
				{"\\@ \"MMMM d, yyyy\"", "4/15/2013","April 15, 2013"},
				{"\\@ \"MMM. d, yy\"", "4/15/2013","Apr. 15, 13"},
				{"\\@ \"d MMMM yyyy\"", "4/15/2013","15 April 2013"},
				{"\\@ \"MMMM yy\"", "4/15/2013","April 13"},
				{"\\@ dddd, MMMM dd, yyyy", "4/15/2013","Monday,"},
				{"\\@ MMMM d, yyyy", "4/15/2013","April"},
				{"\\@ MMM. d, yy", "4/15/2013","Apr."},
				{"\\@ d MMMM yyyy", "4/15/2013","15"},
				{"\\@ MMMM yy", "4/15/2013","April"}		});
	}

	@Test
	public void testDateParsed()throws TransformerException, Docx4JException {
		SwitchTestData data = new SwitchTestData(format, input);
		doit("MERGEFIELD", data, result);
		doit("DOCPROPERTY", data, result);
	}


	// TODO: where no time is passed in..
	// for DOCPROPERTY, Word uses 12:00 AM
	// for DATE and MERGEFIELD, Word uses current time
	// (for =, Word also used current date)

	// tests for time were commented out. To enable it, add to parameters values:
	//{"\\@ M/d/yyyy h:mm am/pm", "4/15/2013","1/3/2006 5:28 PM"},
	//{"\\@ M/d/yyyy h:mm:ss am/pm", "4/15/2013","1/3/2006 5:28:34 PM"},
	//{"\\@ \"M/d/yyyy h:mm am/pm\"", "4/15/2013","4/15/2013 5:28 PM"},
	//{"\\@ \"M/d/yyyy h:mm:ss am/pm\"", "4/15/2013","4/15/2013 5:28:34 PM"},
	//{"\\@ h:mm am/pm", "4/15/2013","5:28 PM"},
	//{"\\@ h:mm:ss am/pm", "4/15/2013","5:28:34 PM"},
	//{"\\@ \"h:mm am/pm\"", "4/15/2013","5:28 PM"},
	//{"\\@ \"h:mm:ss am/pm\"", "4/15/2013","5:28:34 PM"},
	//{"\\@ HH:mm", "4/15/2013","17:28"},
	//{"\\@ \"'Today is 'HH:mm:ss\"", "4/15/2013","Today is 17:28:34"}

}
