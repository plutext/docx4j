package org.docx4j.model;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart;
import org.docx4j.utils.ResourceUtils;
import org.docx4j.wml.PPr;
import org.docx4j.wml.Styles;


public class PropertyResolverTestUtils {
	
	static PPr getEffectivePPrForStyle(String styleId) throws Exception {
		
		java.io.InputStream is = ResourceUtils.getResource("org/docx4j/model/styles-simple.xml");
		
		Styles styles = (Styles)XmlUtils.unmarshal(is);
		
		WordprocessingMLPackage pkg = createdPkgWithStyles(styles);
		
		PropertyResolver resolver = new PropertyResolver(pkg);
		
		PPr effective = resolver.getEffectivePPr(styleId);
		
		System.out.println(effective.getSpacing().getAfter());
		
		return effective;
		
	}
	
	static PPr getEffectivePPrForExpress(PPr expressPPr) throws Exception {

		java.io.InputStream is = ResourceUtils.getResource("org/docx4j/model/styles-simple.xml");
		
		Styles styles = (Styles)XmlUtils.unmarshal(is);
		
		WordprocessingMLPackage pkg = createdPkgWithStyles(styles);
		
		PropertyResolver resolver = new PropertyResolver(pkg);
		
		PPr effective = resolver.getEffectivePPr(expressPPr);
		
		System.out.println(effective.getSpacing().getAfter());
		
		return effective;
		
	}
	
	private static WordprocessingMLPackage createdPkgWithStyles(Styles styles) throws InvalidFormatException {
		
		// Create a package
		WordprocessingMLPackage wmlPack = new WordprocessingMLPackage();

		// Create main document part
		MainDocumentPart wordDocumentPart = new MainDocumentPart();		
		
		// Create main document part content
		org.docx4j.wml.ObjectFactory factory = Context.getWmlObjectFactory();
		org.docx4j.wml.Body  body = factory.createBody();		
		org.docx4j.wml.Document wmlDocumentEl = factory.createDocument();
		
		wmlDocumentEl.setBody(body);
						
		// Put the content in the part
		wordDocumentPart.setJaxbElement(wmlDocumentEl);
						
		// Add the main document part to the package relationships
		// (creating it if necessary)
		wmlPack.addTargetPart(wordDocumentPart);
				
		// Create a styles part
		StyleDefinitionsPart stylesPart = new org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart();
		try {
			
			stylesPart.setJaxbElement(styles);
			
			// Add the styles part to the main document part relationships
			// (creating it if necessary)
			wordDocumentPart.addTargetPart(stylesPart); // NB - add it to main doc part, not package!			
			
		} catch (Exception e) {
			e.printStackTrace();	
		}
		
		return wmlPack;
	}
}
