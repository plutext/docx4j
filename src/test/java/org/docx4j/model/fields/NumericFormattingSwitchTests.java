package org.docx4j.model.fields;

import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.CTSimpleField;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;

public class NumericFormattingSwitchTests {
	
	static List<TestTriple> triples = new ArrayList<TestTriple>(); 
			
	static {
		
		// if no numeric-formatting-switch is present, a numeric result is formatted 
		// without leading spaces or trailing fractional zeros. 
		// If the result is negative, a leading minus sign is present. 
		triples.add(new TestTriple("123.4500", "", "123.45"));
		// If the result is a whole number, no radix point is present. 
		triples.add(new TestTriple("123", "", "123"));
		
		// Examples in spec
		// 0 Specifies the requisite numeric positions to display in the result. 
		// If the result does not include a digit in that position, 0 is displayed.
		
		triples.add(new TestTriple("9", "00.00", "09.00"));
		triples.add(new TestTriple("9.006", "00.00", "09.01"));  // rounds
		// # Specifies the requisite numeric positions to display in the result. 
		// If the result does not include a digit in that position, a space is displayed. Extra fractional digits are rounded off. 
		triples.add(new TestTriple("15", "$###", "$ 15"));
		triples.add(new TestTriple("9", "##.##", "9."));
		triples.add(new TestTriple("9.006", "##.##", "9.01"));
		triples.add(new TestTriple("9.006", "##.##0000", "09.006000"));
		triples.add(new TestTriple("9.006", "##.##00#0", "09.0060 0"));  // check this

		triples.add(new TestTriple("123.456", "####.0000", "123.4560"));
		triples.add(new TestTriple("123.456", "0000.####", "0123.456"));
		
		// x Drops digits to the left of the x placeholder. 
		// If the placeholder is to the right of the decimal point, the result is rounded to that place.
		// NOT IMPLEMENTED triples.add(new TestTriple("11111492", "x##", "492"));
		triples.add(new TestTriple("123456", "x#.#x", "56."));
		triples.add(new TestTriple("123.456", "x#.#x", "23.46"));
		triples.add(new TestTriple("0.125678", "0.00x", "0.126"));
		triples.add(new TestTriple("0.75", ".x", ".8"));

		triples.add(new TestTriple("0.75", ".000x", ".750"));
		triples.add(new TestTriple("0.75", ".###x", ".75"));
		triples.add(new TestTriple(".75", ".###x", ".75"));

		triples.add(new TestTriple("-0.75", ".###x", ".75")); // NB!
		triples.add(new TestTriple("-.75", ".###x", ".75"));
		
		triples.add(new TestTriple("0.125678", "0.00xx", "0.1257")); // it is the last x which is honoured
		triples.add(new TestTriple("0.125678", "0.x0x", "0.126"));   // it is the last x which is honoured
		
		// . Indicates the radix-point position.  The radix-point character displayed is locale-specific.
		triples.add(new TestTriple("95.4", "$###.00", "$ 95.40"));
		
		// , Separates groups of three digits. The separator character displayed is locale-specific.
		triples.add(new TestTriple("2456800", "$#,###,###", "$2,456,800"));
		
		// - Prepends a minus sign to a negative result, or prepends a space if the result is positive or 0.  
		triples.add(new TestTriple("-10", "-##", "-10"));
		triples.add(new TestTriple("10", "-##", " 10"));

		// + Prepends a plus sign to a positive result, a minus sign to a negative result, or a space if the result is 0. 
		triples.add(new TestTriple("10", "+##", "+10"));
		triples.add(new TestTriple("-10", "+##", "-10"));
		
		// Other character: Includes the specified character in the result at that position. 
		triples.add(new TestTriple("33", "##%", "33%"));
		// Note that these don't have to be at the end of the string
		triples.add(new TestTriple("33", "#%#", "3%3"));
		triples.add(new TestTriple("33", "#$#", "3$3"));
		
		// 'text' includes text in the result. 
		triples.add(new TestTriple("3.89", "\"$##0.00 'is the sales tax'\"", "$  3.89 is the sales tax"));
		// .. its only necessary to get special characters into the result
		triples.add(new TestTriple("3.89", "\"$##0.00 'is the ##.00 goodness'\"", "$  3.89 is the ##.00 goodness"));
		triples.add(new TestTriple("3.89", "\"$##0.00 is the '##.00' goodness\"", "$  3.89 is the ##.00 oodness")); // note missing 'g'!

		// literals don't start a new number; spaces don't either
		// The number can only appear once, so those things only insert other stuff
		triples.add(new TestTriple("34", "#'y'#", "3y4"));
		triples.add(new TestTriple("34", "\" # # \"", "3 4"));
		triples.add(new TestTriple("34", "\" $## $## \"", "$  $34"));
		triples.add(new TestTriple("34", "\" # 'y' # \"", "3 y 4"));

		triples.add(new TestTriple("3.89", "\"$##0.00 'is the  goodness'  +###  \"", "$  3.89 is the goodness +"));
		triples.add(new TestTriple("123.456789", "\"0.00 'is sales tax' -000000 \"", "123.45 is sales tax  678900'"));

		// How are spaces after literals handled? Respected
		triples.add(new TestTriple("34", "\" # 'y'     # \"", " 3 y     4"));

		// How are spaces in literals handled? Respected
		triples.add(new TestTriple("34", "\" # 'y     ' # \"", " 3 y      4"));
		
		// the following give syntax errors in Word since they need to be in quote marks
		// A space is an error unless we're in quotes
		//triples.add(new TestTriple("34", "# #", "33"));
		//triples.add(new TestTriple("34", "$## $##", "33"));
		//triples.add(new TestTriple("34", "# 'y' #", "3y3"));
		//triples.add(new TestTriple("15", "$ ###", "$ 15"));

		// an escaped quote mark
		triples.add(new TestTriple("123", "\" 0 ' \\\" '  \"", "123 \" "));
		triples.add(new TestTriple("123", "\" 0  \\\"   \"", "123 \" "));
		// Not sure how to include a single quote literally?
		//triples.add(new TestTriple("123", "\" 0 ' \' '  \"", "123.45 is sales tax  678900'"));

		// Characters which are special in Java
		triples.add(new TestTriple("123", "\"E ## \"", "123"));
		triples.add(new TestTriple("123", "##E ", "123"));
		triples.add(new TestTriple("123", "##'E' ", "123"));

		triples.add(new TestTriple("123", "##;-##", "123"));
		triples.add(new TestTriple("123", "##%", "123%"));

		triples.add(new TestTriple("123", "\"\u2030 ## \"", "‰ 123"));
		triples.add(new TestTriple("123", "\"## \u2030 \"", "123 ‰"));

		triples.add(new TestTriple("123", "\"\u00A4 ## \"", "¤ 123"));
		triples.add(new TestTriple("123", "\"## \u00A4 \"", "123 ¤"));
		
		// NOT IMPLEMENTED: numbered-item
		
		// NOT IMPLEMENTED:  positive-result ; negative-result  Specifies different sets of picture items for positive 
		//and negative results. A zero value uses the positive picture. 
//		triples.add(new TestTriple("1234.56", "$#,##0.00;-$#,##0.00", "$1,234.56"));
//		triples.add(new TestTriple("-1234.56", "$#,##0.00;-$#,##0.00", "-$1,234.56"));
		
		// NOT IMPLEMENTED: positive-result ; negative-result ; zero-result  Specifies different sets of picture items for positive, 
		// negative, and zero results. 
//		triples.add(new TestTriple("1234.56", "$#,##0.00;-$#,##0.00;$0", "$1,234.56"));
//		triples.add(new TestTriple("-1234.56", "$#,##0.00;-$#,##0.00;$0", "-$1,234.56"));
//		triples.add(new TestTriple("0", "$#,##0.00;-$#,##0.00;$0", "$0"));

		// Although the spec says input must be a number, Word will parse stuff like:
		//triples.add(new TestTriple("€180,000.00 EUR", "00", "123"));
			// syntax error with =, but might work with DOCPROPERTY?
		
	}
	
	/**
	 * @param args
	 * @throws Docx4JException 
	 */
	public static void main(String[] args) throws Docx4JException {
//		generateSampleDocx();
		testFormatting();
	}

	public static void testFormatting() throws Docx4JException {

		StringBuffer sb = new StringBuffer(); 

		for (TestTriple tt : triples) {
			
			CTSimpleField simpleField = createSimpleField(tt);
			
			//System.out.println(XmlUtils.marshaltoString(simpleField, true, true));
			sb.append("\n\n" + simpleField.getInstr());
			
			FldSimpleModel fsm = new FldSimpleModel();
			try {
				fsm.build(simpleField.getInstr());
			} catch (TransformerException e) {
				e.printStackTrace();
			}
			
			try {
				sb.append("\n" + "TOBE: " + tt.expectedResult);
				String result = FldSimpleUnitsHelper.applyFormattingSwitch(fsm, fsm.getFldArgument() );
				sb.append("\n" + "ASIS: " + result);
			} catch (java.lang.IllegalArgumentException iae ) {
				System.out.println(iae.getMessage());
				sb.append("\n" + iae.getMessage());
			}
//			List<String> params = fsm.splitParameters(fsm.getFldParameterString());
//			for (String param : params) {
//				System.out.println(param);
//				if (param.startsWith("\\@")) {
//					System.out.println(FldSimpleUnitsHelper.formatDate(fsm));
//				}
//			}
//			
//			String key = params.get(0);
//			
//			System.out.println(dpr.getValue(key));
			
		}
		
		System.out.println(sb.toString() );

	}
	
	
	public static void generateSampleDocx() throws Docx4JException {

		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();
		
		
		org.docx4j.wml.ObjectFactory factory = Context.getWmlObjectFactory();
		
		for (TestTriple tt : triples) {
			org.docx4j.wml.P  p = factory.createP();
			p.getContent().add(createSimpleField(tt));
			mdp.getContent().add(p);
		}

		
	   	// Pretty print the main document part
//		System.out.println(
//				XmlUtils.marshaltoString(mdp.getJaxbElement(), true, true) );
		
		// Optionally save it
		String filename = System.getProperty("user.dir") + "/OUT_NumericFormatting.docx";
		wordMLPackage.save(new java.io.File(filename) );
		System.out.println("Saved " + filename);
	}
	
	private static CTSimpleField createSimpleField(TestTriple triple) {
		 
		ObjectFactory wmlObjectFactory = Context.getWmlObjectFactory();
		
		CTSimpleField field = wmlObjectFactory.createCTSimpleField();
		String instr = null;
		if (triple.format==null || triple.format.equals("")) {
			instr = "=" + triple.number;
		} else {
			instr = "=" + triple.number + " \\# " + triple.format;
		}
 
		field.setInstr(instr);
		
		R r = wmlObjectFactory.createR();
		Text t = wmlObjectFactory.createText();
		
		r.getContent().add(t);
		field.getContent().add(r);
		
		t.setValue("guess");
		
		return field;
	}

	
	public static class TestTriple {
		
		String number;
		String format; 
		String expectedResult;
		
		public TestTriple(String number, String format, String expectedResult) {
			
			this.number = number;			
			this.format = format;
			this.expectedResult = expectedResult;
		}
		
		
	}

}
