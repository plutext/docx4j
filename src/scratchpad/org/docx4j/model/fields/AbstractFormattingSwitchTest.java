package org.docx4j.model.fields;

import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.DocPropsCustomPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.CTSimpleField;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;
import org.junit.Ignore;

/**
 * Please note, these aren't tests as such, but rather
 * code which helps you to understand how Word formats
 * fields, how it compares to what docx4j does, and 
 * opportunities to improve docx4j's ability to mimic 
 * Word.
 * 
 * Real JUnit tests can be found in src/test/java.
 *
 */
@Ignore
public class AbstractFormattingSwitchTest {

	List<SwitchTestQuad> quads = new ArrayList<SwitchTestQuad>();

	String formattingSwitch = null;
	String instruction = null;

	/**
	 * @param args
	 * @throws Docx4JException
	 */
	public static void main(String[] args) throws Docx4JException {

		AbstractFormattingSwitchTest nfst = new AbstractFormattingSwitchTest();
		nfst.testFormatting();
	}

//	@Test
	public void testFormatting() throws Docx4JException {

		StringBuffer sb = new StringBuffer();
		
		NumberExtractor nex = new NumberExtractor(".");		

		int i = 1;
		for (SwitchTestQuad tt : quads) {

			CTSimpleField simpleField = createSimpleField(tt, "myvar" +i, false);

			//System.out.println(XmlUtils.marshaltoString(simpleField, true, true));
			sb.append("\n\n" + simpleField.getInstr());

			FldSimpleModel fsm = new FldSimpleModel();
			try {
				fsm.build(simpleField.getInstr());
			} catch (TransformerException e) {
				e.printStackTrace();
			}

			try {
				//sb.append("\n" + "TOBE: " + tt.expectedResult);
				String result = FormattingSwitchHelper.applyFormattingSwitch(fsm, fsm.getFldArgument() );
				//sb.append("\n" + "ASIS: " + result);

//				assertEquals(result, tt.expectedResult);

				if (result.equals(tt.expectedResult)) {
					sb.append("\n OK");
				} else {
					sb.append("\n" + "ASIS: " + result);
					sb.append("\n" + "WORD: " + tt.word2010Emits);
				}
			} catch (Exception iae ) {
//				iae.printStackTrace();

//				assertEquals(tt.expectedResult, iae.getClass());

				if (tt.expectedResult==iae.getClass()) {
					sb.append("\n OK");
				} else {
					sb.append("\n" + iae.getMessage());
					sb.append("\n" + "WORD: " + tt.word2010Emits);
				}
			}
//			List<String> params = fsm.splitParameters(fsm.getFldParameterString());
//			for (String param : params) {
//				System.out.println(param);
//				if (param.startsWith("\\@")) {
//					System.out.println(FormattingSwitchHelper.formatDate(fsm));
//				}
//			}
//
//			String key = params.get(0);
//
//			System.out.println(dpr.getValue(key));
			
			if (formattingSwitch.equals("\\#")) {
				try {
					System.out.println(tt.val + " is " + nex.extractNumber(tt.val));									
				} catch (java.lang.IllegalStateException e) {
					System.out.println(e.getMessage());	
				}
			}

			
			i++;
		}

		System.out.println(sb.toString() );
		
	}

//	public void generateJUnitTestGeneral() throws Docx4JException {
//
//		StringBuffer sb = new StringBuffer();
//		for (SwitchTestQuad tt : quads) {
//			
//			// public void testApplyFormattingSwitch() throws TransformerException, Docx4JException {
//			if (this.formattingSwitch.equals("\\*")) {
//				sb.append("public void test" + this.instruction.trim().toLowerCase() + "String" + tt.format + "() throws TransformerException, Docx4JException {" );
//			} else if (this.formattingSwitch.equals("\\@")) {
//					sb.append("public void test" + this.instruction.trim().toLowerCase() + "Date" + tt.format + "() throws TransformerException, Docx4JException {" );
//			} else if (this.formattingSwitch.equals("\\#")) {
//				sb.append("public void test" + this.instruction.trim().toLowerCase() + "Number" + tt.format + "() throws TransformerException, Docx4JException {" );
//			}
//			sb.append("\n");
//			
//			// SwitchTestData triple = new SwitchTestData("MERGEFIELD", "\\* Upper", "Mary", "MARY");
//			sb.append("   SwitchTestData triple = new SwitchTestData(\"" + this.instruction + "\", \"\\" + this.formattingSwitch + " " + tt.format + "\", \"" + escape(tt.val) + "\", \"" + escape(tt.expectedResult.toString()) + "\");" );			
//			sb.append("\n");
//			sb.append("\n");
//		}
//		System.out.println(sb.toString() );
//	}

	public void generateJUnitTest() throws Docx4JException {

		StringBuffer sb = new StringBuffer();
		int i=1;
		for (SwitchTestQuad tt : quads) {
			
			sb.append("@Test\n");
			sb.append("public void testDate" + i + "() throws TransformerException, Docx4JException {" );
			sb.append("\n");
			
			// SwitchTestData triple = new SwitchTestData("\\* Upper", "Mary");
			sb.append("   SwitchTestData data = new SwitchTestData(\"\\" + this.formattingSwitch + " " + tt.format + "\", \"" + escape(tt.val.toString()) + "\");" );			
			sb.append("\n");

			// doit("MERGEFIELD", data, "Mary Smith"); 
			sb.append("   doit(\"MERGEFIELD\", data, \"" + escape(tt.expectedResult.toString()) + "\");" );			
			sb.append("\n");
			
			// doit("DOCPROPERTY", data, "Mary Smith");
			sb.append("   doit(\"DOCPROPERTY\", data, \"" + escape(tt.expectedResult.toString()) + "\");" );			
			sb.append("\n");
			
			
			
			sb.append("} \n \n");
			i++;
		}

		System.out.println(sb.toString() );
		
	}

	
	private String escape(String input) {
		return input.replace("\"", "\\\"");
	}

	public void generateSampleDocx(String filename) throws Docx4JException {
		
		// Also create mailmerge data source
		StringBuilder fieldname = new StringBuilder();
		StringBuilder row1 = new StringBuilder();

		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();

		org.docx4j.openpackaging.parts.DocPropsCustomPart docPropsCustomPart = new DocPropsCustomPart();
		wordMLPackage.addTargetPart(docPropsCustomPart);
		org.docx4j.docProps.custom.ObjectFactory cpfactory = new org.docx4j.docProps.custom.ObjectFactory();
		org.docx4j.docProps.custom.Properties customProps = cpfactory.createProperties(); 
		docPropsCustomPart.setJaxbElement(customProps);
		

		org.docx4j.wml.ObjectFactory factory = Context.getWmlObjectFactory();

		int i = 1;
		for (SwitchTestQuad tt : quads) {
			//org.docx4j.wml.P  p = factory.createP();
			org.docx4j.wml.P  p = mdp.addParagraphOfText(tt.val  +" " +  tt.format + " --> ");
			
			p.getContent().add(createSimpleField(tt, "myvar"+i, true));
			
			// Ok, let's add a custom property.
			org.docx4j.docProps.custom.Properties.Property newProp = cpfactory.createPropertiesProperty();
			newProp.setName("myvar"+i);
			newProp.setFmtid(docPropsCustomPart.fmtidValLpwstr ); // Magic string
			newProp.setPid( customProps.getNextId() ); 
			newProp.setLpwstr(tt.val);
			
			// .. add it
			customProps.getProperty().add(newProp);
			
			// For mailmerge
			fieldname.append("\"myvar" + i + "\",");
			row1.append("\"" + tt.val + "\",");
			
			i++;
		}


	   	// Pretty print the main document part
//		System.out.println(
//				XmlUtils.marshaltoString(mdp.getJaxbElement(), true, true) );

		// Optionally save it
		String path = System.getProperty("user.dir") + "/" + filename;
		wordMLPackage.save(new java.io.File(path) );
		System.out.println("Saved " + path);
		
		System.out.println(fieldname.toString());
		System.out.println(row1.toString());
	}

	private CTSimpleField createSimpleField(SwitchTestQuad triple, String varname, boolean useVarname) {

		ObjectFactory wmlObjectFactory = Context.getWmlObjectFactory();

		CTSimpleField field = wmlObjectFactory.createCTSimpleField();
		String instr = null;
		if (triple.format==null ) { 
			if (useVarname && (instruction.equals("DOCPROPERTY ") 
					|| instruction.equals("MERGEFIELD "))) {
				instr = instruction + varname + " " + formattingSwitch;				
			} else {
				instr = instruction +triple.val + " " + formattingSwitch;
			}
		} else {
			if (useVarname && (instruction.equals("DOCPROPERTY ") 
					|| instruction.equals("MERGEFIELD "))) {
				if (triple.format.equals("")) {
					instr = instruction + varname  + " " + formattingSwitch;
				} else
				{
					instr = instruction + varname + " " + formattingSwitch + " " + triple.format;
				}
			} else {
				instr = instruction + triple.val + " " + formattingSwitch + " " + triple.format;
			}
		}

		field.setInstr(instr);

		R r = wmlObjectFactory.createR();
		Text t = wmlObjectFactory.createText();

		r.getContent().add(t);
		field.getContent().add(r);

		t.setValue("guess");

		return field;
	}


	public static class SwitchTestQuad {

		String val;
		String format;
		Object expectedResult;
		String word2010Emits;

		public SwitchTestQuad(String val, String format, String word2010Emits, Object expectedResult) {

			this.val = val;
			this.format = format;
			this.word2010Emits = word2010Emits;
			this.expectedResult = expectedResult;

		}

		public SwitchTestQuad(String val, String format, String expectedResult) {

			this.val = val;
			this.format = format;
			this.expectedResult = expectedResult;
			this.word2010Emits = expectedResult;

		}


	}

}
