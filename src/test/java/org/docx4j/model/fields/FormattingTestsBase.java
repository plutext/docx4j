package org.docx4j.model.fields;

import org.docx4j.openpackaging.exceptions.Docx4JException;

import javax.xml.transform.TransformerException;

import static org.junit.Assert.assertTrue;

/**
 * Created by enamakonov on 06.11.2017.
 */
abstract public class FormattingTestsBase {

	protected void doit(String fieldname, SwitchTestData triple, String expectedResult)  throws TransformerException, Docx4JException {

		String instr = fieldname + " foo " + triple.format;
		String result = getFormat(instr, triple.val);
		System.out.println(result);
		assertTrue(result.equals(expectedResult));
	}

	private String getFormat(String instr, String val) throws TransformerException, Docx4JException {

		FldSimpleModel fsm = new FldSimpleModel();
		fsm.build(instr);
		return FormattingSwitchHelper.applyFormattingSwitch(null, fsm, val);
	}

	protected static class SwitchTestData {

		String format;
		String val;

		public String toString() {
			return "format " + format + " to data " + val;
		}

		public SwitchTestData(String format, String val) {

            String s = this.format = format;
            this.val = val;
		}
	}
}
