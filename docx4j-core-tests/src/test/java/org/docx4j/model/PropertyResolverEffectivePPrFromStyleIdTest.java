package org.docx4j.model;

import junit.framework.Assert;

import org.docx4j.wml.PPr;
import org.junit.Test;


/**
 * 
 * For this test, all we need is a styles part.
 * 
 * @author jharrop
 *
 */
public class PropertyResolverEffectivePPrFromStyleIdTest {
	
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
	public void testNormalSpacingAfter() throws Exception {
		
		PPr effective = PropertyResolverTestUtils.getEffectivePPrForStyle("Normal");
		Assert.assertEquals(effective.getSpacing().getAfter().intValue(), 200);
	}
	

	@Test
	public void testHeading1SpacingAfter() throws Exception {
		
		PPr effective = PropertyResolverTestUtils.getEffectivePPrForStyle("Heading1");
		Assert.assertEquals(effective.getSpacing().getAfter().intValue(), 0);
	}
	
}
