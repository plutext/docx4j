package org.docx4j.model.fields;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.xml.transform.TransformerException;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

@RunWith(value = Parameterized.class)
public class FormattingSwitchHelperNumericTests extends FormattingTestsBase {

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

	@Parameterized.Parameter(value=2)
	public String result;

	@Parameterized.Parameters(name = "{index}: test {1}({0}) parsed as {2}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][]{
				{"\\# ", "123","Error! Switch argument not specified."},
				{"\\# 00.00", "9","09.00"},
				{"\\# 00.00", "9.006","09.01"},
				{"\\# $###", "15","$15"},
				{"\\# ##.##", "9","9"},
				{"\\# ##.##", "9.006","9.01"},
				{"\\# ##.##0000", "9.006","9.006"},
				{"\\# ##.##00#0", "9.006","9.006"},
				{"\\# ####.0000", "123.456","123.4560"},
				{"\\# 0000.####", "123.456","0123.456"},
				{"\\# #.0000", "123.456","123.4560"},
				{"\\# 0.####", "123.456","123.456"},
				{"\\# 0.00x", "0.125678","0.126"},
				{"\\# .x", "0.75",".8"},
				{"\\# .000x", "0.75",".7500"},
				{"\\# .###x", "0.75",".75"},
				{"\\# .###x", ".75",".75"},
				{"\\# 0.00xx", "0.125678","0.1257"},
				{"\\# 0.x0x", "0.125678","0.126"},
				{"\\# $###.00", "95.4","$95.40"},
				{"\\# $#,###,###", "2456800","$2,456,800"},
				{"\\# -##", "-10","-10"},
				{"\\# -##", "10","10"},
				{"\\# +##", "10","10"},
				{"\\# +##", "-10","-10"},
				{"\\# ##%", "33","33%"},
				{"\\# \"$##0.00 'is the sales tax'\"", "3.89","$3.89 is the sales tax"},
				{"\\# \"$##0.00 'is the ##.00 goodness'\"", "3.89","$3.89 is the ##.00 goodness"},
				{"\\# \"$##0.00 is the '##.00' goodness\"", "3.89","$3.89 is the ##.00 goodness"},
				{"\\# 00", "€180,000.00 EUR","180000"}
		});
	}

	// following assertions were commented out. To test it, add following parameters:
	//{"\\# ", "-123.4500","-123.4500"},
	//{"\\# ", "123","123"},
	//{"\\# ", "-123","-123"},
	//{"\\# ", "123.","123"},
	//{"\\# ", "-123.","-123"},
	//{"\\# ", "0","0"},
	//{"\\# ", "00","00"},
	//{"\\# ", "0.","0"},
	//{"\\# ", "01","01"},
	//{"\\# ", "0.1","0.1"},
	//{"\\# ", "0.0","0.0"},
	//{"\\# ", "0.00","0.00"},
	//{"\\# ", "0.00 0","0.00 0"},
	//{"\\# ", "0.00 1","0.00 1"},
	//{"\\# ", "0.00 A","0.00 A"},
	//{"\\# ", "0.00 W","0.00 W"},
	//{"\\# ", "\"01\"","01"},
	//{"\\# ", "\"0.1\"","0.1"},
	//{"\\# ", "\"0.0\"","0.0"},
	//{"\\# ", "\"0.00\"","0.00"},
	//{"\\# ", "\"0.00 0\"","0.00 0"},
	//{"\\# ", "\"0.00 1\"","0.00 1"},
	//{"\\# ", "\"0.00 A\"","0.00 A"},
	//{"\\# ", "\"0.00 W\"","0.00 W"},
	//{"\\# ", "0000123456","0000123456"},
	//{"\\# ", "000012345.006","000012345.006"},
	//{"\\# ", "0000123AA456","0000123AA456"},
	//{"\\# ", "0000123AA45.006","0000123AA45.006"}

	@Test
	public void testNumberParsed()throws TransformerException, Docx4JException {
		SwitchTestData data = new SwitchTestData(format, input);
		doit("MERGEFIELD", data, result);
		doit("DOCPROPERTY", data, result);
	}

}
