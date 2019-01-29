package org.docx4j.model;

import junit.framework.Assert;

import org.docx4j.XmlUtils;
import org.docx4j.wml.PPr;
import org.junit.Test;


/**
 * 
 * For this test, we need the styles part and a pPr,
 * together with a flag which specifies whether to pretend 
 * it is in a table.
 * 
 * @author jharrop
 *
 */
public class PropertyResolverEffectivePPrFromExpressTest {
	
/*
 * Well test w:spacing/@w:after, since the hierarchy contains:
 * 
	<w:docDefaults>
		<w:pPrDefault>
			<w:pPr>
				<w:spacing w:after="200" w:line="276" w:lineRule="auto" />
			</w:pPr>
		</w:pPrDefault>
	</w:docDefaults>

	<w:style w:type="paragraph" w:default="1" w:styleId="Normal">
		<w:name w:val="Normal" />
		<w:qFormat />
	</w:style>
	
	<w:style w:type="paragraph" w:styleId="Heading1">
		<w:name w:val="heading 1" />
		<w:basedOn w:val="Normal" />
		<w:pPr>
			<w:spacing w:before="480" w:after="0" /> 
			
			*/
	
	@Test
	public void testH1PlusExplicitSpacingAfter() throws Exception {
		
		String openXML = "<w:pPr xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
	            + "<w:pStyle w:val=\"Heading1\"/>"
	            + "<w:spacing w:after=\"720\"/>"
	            + "<w:rPr>"
	                + "<w:lang w:val=\"en-AU\"/>"
	            +"</w:rPr>"
	        +"</w:pPr>";
		PPr ppr = (PPr)XmlUtils.unmarshalString(openXML);
	
		
		PPr effective = PropertyResolverTestUtils.getEffectivePPrForExpress(ppr);
		Assert.assertEquals(effective.getSpacing().getAfter().intValue(), 720);
	}

	@Test
	public void testH1PlusNothingSpacingAfter() throws Exception {
		
		String openXML = "<w:pPr xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
	            + "<w:pStyle w:val=\"Heading1\"/>"
	            //+ "<w:spacing w:after=\"720\"/>"
	            + "<w:rPr>"
	                + "<w:lang w:val=\"en-AU\"/>"
	            +"</w:rPr>"
	        +"</w:pPr>";
		PPr ppr = (PPr)XmlUtils.unmarshalString(openXML);
	
		
		PPr effective = PropertyResolverTestUtils.getEffectivePPrForExpress(ppr);
		Assert.assertEquals(effective.getSpacing().getAfter().intValue(), 0);
	}
	
	

	@Test
	public void testNormalPlusExplicitSpacingAfter() throws Exception {
		
		String openXML = "<w:pPr xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
	            + "<w:spacing w:after=\"720\"/>"
	            + "<w:rPr>"
	                + "<w:lang w:val=\"en-AU\"/>"
	            +"</w:rPr>"
	        +"</w:pPr>";
		PPr ppr = (PPr)XmlUtils.unmarshalString(openXML);		
		
		PPr effective = PropertyResolverTestUtils.getEffectivePPrForExpress(ppr);
		Assert.assertEquals(effective.getSpacing().getAfter().intValue(), 720);
	}

	@Test
	public void testNormalPlusNothingSpacingAfter() throws Exception {
		
		String openXML = "<w:pPr xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
	            + "<w:rPr>"
	                + "<w:lang w:val=\"en-AU\"/>"
	            +"</w:rPr>"
	        +"</w:pPr>";
		PPr ppr = (PPr)XmlUtils.unmarshalString(openXML);		
		
		PPr effective = PropertyResolverTestUtils.getEffectivePPrForExpress(ppr);
		Assert.assertEquals(effective.getSpacing().getAfter().intValue(), 200);
	}
	
}
