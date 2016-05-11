package org.docx4j.model.fields;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.docx4j.XmlUtils;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;
import org.junit.Test;

public class CanonicalisationTests {

	@Test
	public void testBookmarked() throws JAXBException, IOException {
	
		List<FieldRef> fieldRefs = new ArrayList<FieldRef>();
		P resultP = FieldsPreprocessor.canonicalise(getP("Canon_bookmarked.xml"), fieldRefs);
		
		String xml = XmlUtils.marshaltoString(resultP, true, true);
		
		// bookmarks still present
		assertTrue(xml.contains("<w:bookmarkStart"));
		assertTrue(xml.contains("<w:bookmarkEnd"));
		
		// result slot contents
		FieldRef fieldRef1 =  fieldRefs.get(0);
		R r = fieldRef1.getResultsSlot();
		Text text = (Text)XmlUtils.unwrap(r.getContent().get(0));
		assertTrue(text.getValue().equals("(Customer)"));
		
	}
	
	@Test
	public void testFORMTEXTwithDefault() throws JAXBException, IOException {
	
		List<FieldRef> fieldRefs = new ArrayList<FieldRef>();
		P resultP = FieldsPreprocessor.canonicalise(getP("Canon_FORMTEXT_default.xml"), fieldRefs);
		
		
		// Should contain 3 runs
		assertTrue(resultP.getContent().size()==3);
		
		// 2nd run is the result slot
		FieldRef fieldRef1 =  fieldRefs.get(0);
		assertTrue(resultP.getContent().get(1)==fieldRef1.getResultsSlot());
	}

	@Test
	public void testFORMTEXTwithSpaces() throws JAXBException, IOException {
	
		List<FieldRef> fieldRefs = new ArrayList<FieldRef>();
		P resultP = FieldsPreprocessor.canonicalise(getP("Canon_FORMTEXT-spaces.xml"), fieldRefs);
		
		
		// Should contain 3 runs
		assertTrue(resultP.getContent().size()==3);
		
		// 2nd run is the result slot
		FieldRef fieldRef1 =  fieldRefs.get(0);
		assertTrue(resultP.getContent().get(1)==fieldRef1.getResultsSlot());
		
		//System.out.println(XmlUtils.marshaltoString(fieldRef1.getResultsSlot(), true, true));
	}
	
	@Test
	public void testFORMTEXTdefault_trail() throws JAXBException, IOException {
	
		List<FieldRef> fieldRefs = new ArrayList<FieldRef>();
		P resultP = FieldsPreprocessor.canonicalise(getP("Canon_FORMTEXTdefault_trail.xml"), fieldRefs);
		
		
		// Should contain 3 + 1 runs
		assertTrue(resultP.getContent().size()==4);
		
		// 2nd run is the result slot
		FieldRef fieldRef1 =  fieldRefs.get(0);
		assertTrue(resultP.getContent().get(1)==fieldRef1.getResultsSlot());
	}

	@Test
	public void testLeadFORMTEXTdefault() throws JAXBException, IOException {
	
		List<FieldRef> fieldRefs = new ArrayList<FieldRef>();
		P resultP = FieldsPreprocessor.canonicalise(getP("Canon_lead_FORMTEXTdefault.xml"), fieldRefs);
		
		
		// Should contain 1 + 3 runs
		assertTrue(resultP.getContent().size()==4);
		
		// 3rd run is the result slot
		FieldRef fieldRef1 =  fieldRefs.get(0);
		assertTrue(resultP.getContent().get(2)==fieldRef1.getResultsSlot());
	}

	@Test
	public void testLeadMERGEFIELDMergeFormat() throws JAXBException, IOException {
	
		List<FieldRef> fieldRefs = new ArrayList<FieldRef>();
		P resultP = FieldsPreprocessor.canonicalise(getP("Canon_lead_MERGEFIELD-MERGEFORMAT.xml"), fieldRefs);
		
		
		// Should contain 1 + 3 + 1 runs
		assertTrue(resultP.getContent().size()==5);
		
		// 3rd run is the result slot
		FieldRef fieldRef1 =  fieldRefs.get(0);
		assertTrue(resultP.getContent().get(2)==fieldRef1.getResultsSlot());
	}

	@Test
	public void testLeadMERGEFIELDUpper() throws JAXBException, IOException {
	
		List<FieldRef> fieldRefs = new ArrayList<FieldRef>();
		P resultP = FieldsPreprocessor.canonicalise(getP("Canon_lead_MERGEFIELDUpper_tail.xml"), fieldRefs);
		
		
		// Should contain 1 + 3 + 1 runs
		assertTrue(resultP.getContent().size()==5);
		
		// 3rd run is the result slot
		FieldRef fieldRef1 =  fieldRefs.get(0);
		assertTrue(resultP.getContent().get(2)==fieldRef1.getResultsSlot());
	}

	@Test
	public void testLeadMERGEFIELD() throws JAXBException, IOException {
	
		List<FieldRef> fieldRefs = new ArrayList<FieldRef>();
		P resultP = FieldsPreprocessor.canonicalise(getP("Canon_MERGEFIELD.xml"), fieldRefs);
		
		
		// Should contain 3 runs
		assertTrue(resultP.getContent().size()==3);
		
		// 2nd run is the result slot
		FieldRef fieldRef1 =  fieldRefs.get(0);
		assertTrue(resultP.getContent().get(1)==fieldRef1.getResultsSlot());
	}
	
	private P getP(String filename) throws JAXBException, IOException {
		
		return (P)XmlUtils.unmarshal(org.docx4j.utils.ResourceUtils.getResource("org/docx4j/model/fields/" + filename));
	}
	

}
