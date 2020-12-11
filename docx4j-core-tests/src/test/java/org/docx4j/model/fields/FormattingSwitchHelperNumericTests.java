package org.docx4j.model.fields;

import static org.junit.Assert.assertTrue;

import javax.xml.transform.TransformerException;

import org.docx4j.Docx4jProperties;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.junit.Test;

public class FormattingSwitchHelperNumericTests {
	
	
	@Test
	public void testNumberNotSpecified() throws TransformerException, Docx4JException {
		// \# without arg results in Error! Switch argument not specified.
		   SwitchTestData data = new SwitchTestData("\\# ", "123");
		   doit("MERGEFIELD", data, "Error! Switch argument not specified.");
		   doit("DOCPROPERTY", data, "Error! Switch argument not specified.");
	}
	
	@Test
	public void testNumber31() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\# 00.00", "9");
	   doit("MERGEFIELD", data, "09.00");
	   doit("DOCPROPERTY", data, "09.00");
	} 
	 
	@Test
	public void testNumber32() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\# 00.00", "9.006");
	   doit("MERGEFIELD", data, "09.01");
	   doit("DOCPROPERTY", data, "09.01");
	} 
	 
	@Test
	public void testNumber33() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\# $###", "15");
	   doit("MERGEFIELD", data, "$15");
	   doit("DOCPROPERTY", data, "$15");
	} 
	 
	@Test
	public void testNumber34() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\# ##.##", "9");
	   doit("MERGEFIELD", data, "9");
	   doit("DOCPROPERTY", data, "9");
	} 
	 
	@Test
	public void testNumber35() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\# ##.##", "9.006");
	   doit("MERGEFIELD", data, "9.01");
	   doit("DOCPROPERTY", data, "9.01");
	} 
	 
	@Test
	public void testNumber36() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\# ##.##0000", "9.006");
	   doit("MERGEFIELD", data, "9.006");
	   doit("DOCPROPERTY", data, "9.006");
	} 
	 
	@Test
	public void testNumber37() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\# ##.##00#0", "9.006");
	   doit("MERGEFIELD", data, "9.006");
	   doit("DOCPROPERTY", data, "9.006");
	} 
	 
	@Test
	public void testNumber38() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\# ####.0000", "123.456");
	   doit("MERGEFIELD", data, "123.4560");
	   doit("DOCPROPERTY", data, "123.4560");
	} 
	 
	@Test
	public void testNumber39() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\# 0000.####", "123.456");
	   doit("MERGEFIELD", data, "0123.456");
	   doit("DOCPROPERTY", data, "0123.456");
	} 
	 
	@Test
	public void testNumber40() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\# #.0000", "123.456");
	   doit("MERGEFIELD", data, "123.4560");
	   doit("DOCPROPERTY", data, "123.4560");
	} 
	 
	@Test
	public void testNumber41() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\# 0.####", "123.456");
	   doit("MERGEFIELD", data, "123.456");
	   doit("DOCPROPERTY", data, "123.456");
	} 
	 
	 
	@Test
	public void testNumber44() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\# 0.00x", "0.125678");
	   doit("MERGEFIELD", data, "0.126");
	   doit("DOCPROPERTY", data, "0.126");
	} 
	 
	@Test
	public void testNumber45() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\# .x", "0.75");
	   doit("MERGEFIELD", data, ".8");
	   doit("DOCPROPERTY", data, ".8");
	} 
	 
	@Test
	public void testNumber46() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\# .000x", "0.75");
	   doit("MERGEFIELD", data, ".7500");
	   doit("DOCPROPERTY", data, ".7500");
	} 
	 
	@Test
	public void testNumber47() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\# .###x", "0.75");
	   doit("MERGEFIELD", data, ".75");
	   doit("DOCPROPERTY", data, ".75");
	} 
	 
	@Test
	public void testNumber48() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\# .###x", ".75");
	   doit("MERGEFIELD", data, ".75");
	   doit("DOCPROPERTY", data, ".75");
	} 
	 
	 
	@Test
	public void testNumber55() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\# 0.00xx", "0.125678");
	   doit("MERGEFIELD", data, "0.1257");
	   doit("DOCPROPERTY", data, "0.1257");
	} 
	 
	@Test
	public void testNumber56() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\# 0.x0x", "0.125678");
	   doit("MERGEFIELD", data, "0.126");
	   doit("DOCPROPERTY", data, "0.126");
	} 
	 
	@Test
	public void testNumber57() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\# $###.00", "95.4");
	   doit("MERGEFIELD", data, "$95.40");
	   doit("DOCPROPERTY", data, "$95.40");
	} 
	 
	 
	@Test
	public void testNumber60() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\# $#,###,###", "2456800");
	   doit("MERGEFIELD", data, "$2,456,800");
	   doit("DOCPROPERTY", data, "$2,456,800");
	} 
	 
	@Test
	public void testNumber61() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\# -##", "-10");
	   doit("MERGEFIELD", data, "-10");
	   doit("DOCPROPERTY", data, "-10");
	} 
	 
	@Test
	public void testNumber62() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\# -##", "10");
	   doit("MERGEFIELD", data, "10");
	   doit("DOCPROPERTY", data, "10");
	} 
	 
	@Test
	public void testNumber63() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\# +##", "10");
	   doit("MERGEFIELD", data, "10");
	   doit("DOCPROPERTY", data, "10");
	} 
	 
	@Test
	public void testNumber64() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\# +##", "-10");
	   doit("MERGEFIELD", data, "-10");
	   doit("DOCPROPERTY", data, "-10");
	} 
	 
	@Test
	public void testNumber65() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\# ##%", "33");
	   doit("MERGEFIELD", data, "33%");
	   doit("DOCPROPERTY", data, "33%");
	} 
	
	 
	@Test
	public void testNumber68() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\# \"$##0.00 'is the sales tax'\"", "3.89");
	   doit("MERGEFIELD", data, "$3.89 is the sales tax");
	   doit("DOCPROPERTY", data, "$3.89 is the sales tax");
	} 
	 
	@Test
	public void testNumber69() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\# \"$##0.00 'is the ##.00 goodness'\"", "3.89");
	   doit("MERGEFIELD", data, "$3.89 is the ##.00 goodness");
	   doit("DOCPROPERTY", data, "$3.89 is the ##.00 goodness");
	} 
	 
	@Test
	public void testNumber70() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\# \"$##0.00 is the '##.00' goodness\"", "3.89");
	   doit("MERGEFIELD", data, "$3.89 is the ##.00 goodness");
	   doit("DOCPROPERTY", data, "$3.89 is the ##.00 goodness");
	} 
	 
	 
	@Test
	public void testNumber72() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\# 00", "€180,000.00 EUR");
	   doit("MERGEFIELD", data, "180000");
	   doit("DOCPROPERTY", data, "180000");
	} 
	
	@Test
	public void testNumber42() throws TransformerException, Docx4JException {
		SwitchTestData data = new SwitchTestData("\\# x#.#x", "123456");
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
	 
	@Test
	public void testNumber43() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\# x#.#x", "123.456");
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
	@Test
	public void testNumber49() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\# .###x", "-0.75");
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
	 
	@Test
	public void testNumber50() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\# .###x", "-.75");
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
	 
	@Test
	public void testNumber51() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\# .###", "-0.75");
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
	 
	@Test
	public void testNumber52() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\# .###", "-.75");
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
	 
	@Test
	public void testNumber53() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\# .000", "-0.75");
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
	 
	@Test
	public void testNumber54() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\# .000", "-.75");
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
	
	@Test
	public void testNumber58() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\# .00", "95.4");
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
	 
	@Test
	public void testNumber59() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\# .##", "95.4");
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
	
	@Test
	public void testNumber66() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\# #%#", "33");
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
	 
	@Test
	public void testNumber67() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\# #$#", "33");
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
	
	@Test
	public void testNumber71() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\# #'y'#", "34");
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
	
	@Test
	public void testNumber73() throws TransformerException, Docx4JException {
	   Docx4jProperties.setProperty("docx4j.Fields.Numbers.JavaStylePercentHandling", true);
	   SwitchTestData data = new SwitchTestData("\\# ##%", "33");
	   doit("MERGEFIELD", data, "3300%");
	   doit("DOCPROPERTY", data, "3300%");
	   Docx4jProperties.setProperty("docx4j.Fields.Numbers.JavaStylePercentHandling", false);
	} 

	@Test
	public void testNumber74() throws TransformerException, Docx4JException {
	   Docx4jProperties.setProperty("docx4j.Fields.Numbers.JavaStylePercentHandling", true);
	   SwitchTestData data = new SwitchTestData("\\# ##%", ".33");
	   doit("MERGEFIELD", data, "33%");
	   doit("DOCPROPERTY", data, "33%");
	   Docx4jProperties.setProperty("docx4j.Fields.Numbers.JavaStylePercentHandling", false);
	} 

	@Test
	public void testNumber75() throws TransformerException, Docx4JException {
	   Docx4jProperties.setProperty("docx4j.Fields.Numbers.JavaStylePercentHandling", true);
	   SwitchTestData data = new SwitchTestData("\\# ##%", "0.33");
	   doit("MERGEFIELD", data, "33%");
	   doit("DOCPROPERTY", data, "33%");
	   Docx4jProperties.setProperty("docx4j.Fields.Numbers.JavaStylePercentHandling", false);
	} 

	@Test
	public void testNumber76() throws TransformerException, Docx4JException {
	   Docx4jProperties.setProperty("docx4j.Fields.Numbers.JavaStylePercentHandling", true);
	   SwitchTestData data = new SwitchTestData("\\# ##%", "1.33");
	   doit("MERGEFIELD", data, "133%");
	   doit("DOCPROPERTY", data, "133%");
	   Docx4jProperties.setProperty("docx4j.Fields.Numbers.JavaStylePercentHandling", false);
	} 

	
	// All these should return Error! Switch argument not specified.
	
	@Test
	public void testNumber1() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\# ", "AA");
	   doit("MERGEFIELD", data, "Error! Switch argument not specified.");  
	   doit("DOCPROPERTY", data, "Error! Switch argument not specified.");
	} 
	 
	@Test
	public void testNumber2() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\# ", "123.4500");
	   doit("MERGEFIELD", data, "Error! Switch argument not specified.");
	   doit("DOCPROPERTY", data, "Error! Switch argument not specified.");
	} 
//	 
//	@Test
//	public void testNumber3() throws TransformerException, Docx4JException {
//	   SwitchTestData data = new SwitchTestData("\\# ", "-123.4500");
//	   doit("MERGEFIELD", data, "-123.4500");
//	   doit("DOCPROPERTY", data, "-123.4500");
//	} 
//	 
//	@Test
//	public void testNumber4() throws TransformerException, Docx4JException {
//	   SwitchTestData data = new SwitchTestData("\\# ", "123");
//	   doit("MERGEFIELD", data, "123");
//	   doit("DOCPROPERTY", data, "123");
//	} 
//	 
//	@Test
//	public void testNumber5() throws TransformerException, Docx4JException {
//	   SwitchTestData data = new SwitchTestData("\\# ", "-123");
//	   doit("MERGEFIELD", data, "-123");
//	   doit("DOCPROPERTY", data, "-123");
//	} 
//	 
//	@Test
//	public void testNumber6() throws TransformerException, Docx4JException {
//	   SwitchTestData data = new SwitchTestData("\\# ", "123.");
//	   doit("MERGEFIELD", data, "123");
//	   doit("DOCPROPERTY", data, "123."); // NB result is different
//	} 
//	 
//	@Test
//	public void testNumber7() throws TransformerException, Docx4JException {
//	   SwitchTestData data = new SwitchTestData("\\# ", "-123.");
//	   doit("MERGEFIELD", data, "-123");
//	   doit("DOCPROPERTY", data, "-123."); // NB result is different
//	} 
//	 
//	@Test
//	public void testNumber8() throws TransformerException, Docx4JException {
//	   SwitchTestData data = new SwitchTestData("\\# ", "0");
//	   doit("MERGEFIELD", data, "0");
//	   doit("DOCPROPERTY", data, "0");
//	} 
//	 
//	@Test
//	public void testNumber9() throws TransformerException, Docx4JException {
//	   SwitchTestData data = new SwitchTestData("\\# ", "00");
//	   doit("MERGEFIELD", data, "00");
//	   doit("DOCPROPERTY", data, "00");
//	} 
//	 
//	@Test
//	public void testNumber10() throws TransformerException, Docx4JException {
//	   SwitchTestData data = new SwitchTestData("\\# ", "0.");
//	   doit("MERGEFIELD", data, "0");
//	   doit("DOCPROPERTY", data, "0.");  // NB result is different
//	} 
//	 
//	@Test
//	public void testNumber11() throws TransformerException, Docx4JException {
//	   SwitchTestData data = new SwitchTestData("\\# ", "01");
//	   doit("MERGEFIELD", data, "01");
//	   doit("DOCPROPERTY", data, "01");
//	} 
//	 
//	@Test
//	public void testNumber12() throws TransformerException, Docx4JException {
//	   SwitchTestData data = new SwitchTestData("\\# ", "0.1");
//	   doit("MERGEFIELD", data, "0.1");
//	   doit("DOCPROPERTY", data, "0.1");
//	} 
//	 
//	@Test
//	public void testNumber13() throws TransformerException, Docx4JException {
//	   SwitchTestData data = new SwitchTestData("\\# ", "0.0");
//	   doit("MERGEFIELD", data, "0.0");
//	   doit("DOCPROPERTY", data, "0.0");
//	} 
//	 
//	@Test
//	public void testNumber14() throws TransformerException, Docx4JException {
//	   SwitchTestData data = new SwitchTestData("\\# ", "0.00");
//	   doit("MERGEFIELD", data, "0.00");
//	   doit("DOCPROPERTY", data, "0.00");
//	} 
//	 
//	@Test
//	public void testNumber15() throws TransformerException, Docx4JException {
//	   SwitchTestData data = new SwitchTestData("\\# ", "0.00 0");
//	   doit("MERGEFIELD", data, "0.00 0");
//	   doit("DOCPROPERTY", data, "0.00 0");
//	} 
//	 
//	@Test
//	public void testNumber16() throws TransformerException, Docx4JException {
//	   SwitchTestData data = new SwitchTestData("\\# ", "0.00 1");
//	   doit("MERGEFIELD", data, "0.00 1");
//	   doit("DOCPROPERTY", data, "0.00 1");
//	} 
//	 
//	@Test
//	public void testNumber17() throws TransformerException, Docx4JException {
//	   SwitchTestData data = new SwitchTestData("\\# ", "0.00 A");
//	   doit("MERGEFIELD", data, "0.00 A");
//	   doit("DOCPROPERTY", data, "0.00 A");
//	} 
//	 
//	@Test
//	public void testNumber18() throws TransformerException, Docx4JException {
//	   SwitchTestData data = new SwitchTestData("\\# ", "0.00 W");
//	   doit("MERGEFIELD", data, "0.00 W");
//	   doit("DOCPROPERTY", data, "0.00 W");
//	} 
//	 
//	@Test
//	public void testNumber19() throws TransformerException, Docx4JException {
//	   SwitchTestData data = new SwitchTestData("\\# ", "\"01\"");
//	   doit("MERGEFIELD", data, "01");
//	   doit("DOCPROPERTY", data, "01");
//	} 
//	 
//	@Test
//	public void testNumber20() throws TransformerException, Docx4JException {
//	   SwitchTestData data = new SwitchTestData("\\# ", "\"0.1\"");
//	   doit("MERGEFIELD", data, "0.1");
//	   doit("DOCPROPERTY", data, "0.1");
//	} 
//	 
//	@Test
//	public void testNumber21() throws TransformerException, Docx4JException {
//	   SwitchTestData data = new SwitchTestData("\\# ", "\"0.0\"");
//	   doit("MERGEFIELD", data, "0.0");
//	   doit("DOCPROPERTY", data, "0.0");
//	} 
//	 
//	@Test
//	public void testNumber22() throws TransformerException, Docx4JException {
//	   SwitchTestData data = new SwitchTestData("\\# ", "\"0.00\"");
//	   doit("MERGEFIELD", data, "0.00");
//	   doit("DOCPROPERTY", data, "0.00");
//	} 
//	 
//	@Test
//	public void testNumber23() throws TransformerException, Docx4JException {
//	   SwitchTestData data = new SwitchTestData("\\# ", "\"0.00 0\"");
//	   doit("MERGEFIELD", data, "0.00 0");
//	   doit("DOCPROPERTY", data, "0.00 0");
//	} 
//	 
//	@Test
//	public void testNumber24() throws TransformerException, Docx4JException {
//	   SwitchTestData data = new SwitchTestData("\\# ", "\"0.00 1\"");
//	   doit("MERGEFIELD", data, "0.00 1");
//	   doit("DOCPROPERTY", data, "0.00 1");
//	} 
//	 
//	@Test
//	public void testNumber25() throws TransformerException, Docx4JException {
//	   SwitchTestData data = new SwitchTestData("\\# ", "\"0.00 A\"");
//	   doit("MERGEFIELD", data, "0.00 A");
//	   doit("DOCPROPERTY", data, "0.00 A");
//	} 
//	 
//	@Test
//	public void testNumber26() throws TransformerException, Docx4JException {
//	   SwitchTestData data = new SwitchTestData("\\# ", "\"0.00 W\"");
//	   doit("MERGEFIELD", data, "0.00 W");
//	   doit("DOCPROPERTY", data, "0.00 W");
//	} 
//	 
//	@Test
//	public void testNumber27() throws TransformerException, Docx4JException {
//	   SwitchTestData data = new SwitchTestData("\\# ", "0000123456");
//	   doit("MERGEFIELD", data, "0000123456");
//	   doit("DOCPROPERTY", data, "0000123456");
//	} 
//	 
//	@Test
//	public void testNumber28() throws TransformerException, Docx4JException {
//	   SwitchTestData data = new SwitchTestData("\\# ", "000012345.006");
//	   doit("MERGEFIELD", data, "000012345.006");
//	   doit("DOCPROPERTY", data, "000012345.006");
//	} 
//	 
//	@Test
//	public void testNumber29() throws TransformerException, Docx4JException {
//	   SwitchTestData data = new SwitchTestData("\\# ", "0000123AA456");
//	   doit("MERGEFIELD", data, "0000123AA456");
//	   doit("DOCPROPERTY", data, "0000123AA456");
//	} 
//	 
//	@Test
//	public void testNumber30() throws TransformerException, Docx4JException {
//	   SwitchTestData data = new SwitchTestData("\\# ", "0000123AA45.006");
//	   doit("MERGEFIELD", data, "0000123AA45.006");
//	   doit("DOCPROPERTY", data, "0000123AA45.006");
//	} 
	 
	
	
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
