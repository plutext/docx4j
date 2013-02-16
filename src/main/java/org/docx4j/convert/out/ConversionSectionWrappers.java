package org.docx4j.convert.out;

import java.util.ArrayList;
import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.model.structure.HeaderFooterPolicy;
import org.docx4j.model.structure.jaxb.ObjectFactory;
import org.docx4j.model.structure.jaxb.Sections;
import org.docx4j.model.structure.jaxb.Sections.Section;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.Document;
import org.w3c.dom.Element;

public class ConversionSectionWrappers {
	protected List<ConversionSectionWrapper> conversionSections = null;
	protected int currentSectionIndex = -1;
	protected ConversionSectionWrapper currentSection = null;
	
	protected ConversionSectionWrappers(List<ConversionSectionWrapper> conversionSections) {
		this.conversionSections = conversionSections;
	}
    
	public static ConversionSectionWrappers build(Document doc, WordprocessingMLPackage wmlPackage) {
		List<ConversionSectionWrapper> conversionSections = new ArrayList<ConversionSectionWrapper>();
		List<Object> sectionContent = new ArrayList<Object>();
		ConversionSectionWrapper currentSectionWrapper = null;
		RelationshipsPart rels = wmlPackage.getMainDocumentPart().getRelationshipsPart();
		HeaderFooterPolicy previousHF = null;
		BooleanDefaultTrue evenAndOddHeaders = null;				
		int conversionSectionIndex = 0;

		if ((wmlPackage.getMainDocumentPart().getDocumentSettingsPart() != null) &&
			(wmlPackage.getMainDocumentPart().getDocumentSettingsPart().getJaxbElement() != null)) {
			evenAndOddHeaders = wmlPackage.getMainDocumentPart().getDocumentSettingsPart().getJaxbElement().getEvenAndOddHeaders();
		}
		
		//org.docx4j.wml.Document doc = (org.docx4j.wml.Document)wordMLPackage.getMainDocumentPart().getJaxbElement();
		for (Object o : doc.getBody().getContent() ) {
			
			if (o instanceof org.docx4j.wml.P) {
				if (((org.docx4j.wml.P)o).getPPr() != null ) {
					org.docx4j.wml.PPr ppr = ((org.docx4j.wml.P)o).getPPr();
					if (ppr.getSectPr()!=null) {

						// According to the ECMA-376 2ed, if type is not specified, read it as next page
						// However Word 2007 sometimes treats it as continuous, and sometimes doesn't??						
						
						if ( ppr.getSectPr().getType()!=null
								     && ppr.getSectPr().getType().getVal().equals("continuous")) {
							// In case there are some headers/footers that get inherited by the next section
							previousHF = new HeaderFooterPolicy(ppr.getSectPr(), previousHF, rels, evenAndOddHeaders);
							// If its continuous, don't add a section
						} else {
							currentSectionWrapper = new ConversionSectionWrapper(
									ppr.getSectPr(), previousHF, rels, evenAndOddHeaders, 
									"s" + Integer.toString(++conversionSectionIndex), 
									sectionContent); 
							conversionSections.add(currentSectionWrapper);
							previousHF = currentSectionWrapper.getHeaderFooterPolicy();
							sectionContent = new ArrayList<Object>();
						}
					}
				}				
			} 
			sectionContent.add(o);
		}
		
		currentSectionWrapper = new ConversionSectionWrapper(
				doc.getBody().getSectPr(), previousHF, rels, evenAndOddHeaders, 
				"s" + Integer.toString(++conversionSectionIndex), 
				sectionContent); 
		
		conversionSections.add(currentSectionWrapper);
		return new ConversionSectionWrappers(conversionSections);				
	}

	public Sections createSections() {
	ObjectFactory factory = new ObjectFactory();
		
		Sections ret = factory.createSections();
		for (int i=0; i<conversionSections.size(); i++) {
			ret.getSection().add(createSection(factory, conversionSections.get(i)));
		}
		return ret;
	}
    
	private Section createSection(ObjectFactory factory, ConversionSectionWrapper conversionSectionWrapper) {
	Section ret = factory.createSectionsSection();
		ret.setName(conversionSectionWrapper.getId());
		for (int i=0; i<conversionSectionWrapper.getContent().size(); i++) {
			// TODO: since the section model knows nothing about WML,
			// we have to marshall each object separately.
			// To fix this, next time wml is generated, include the section model there!
			ret.getAny().add(marshall(conversionSectionWrapper.getContent().get(i)));
		}
		return ret;
	}

	private static Element marshall(Object o) {
		
		try {
			org.w3c.dom.Document w3cDoc = 
				XmlUtils.marshaltoW3CDomDocument(o);
			
			
				/* Force the RelationshipsPart to be marshalled using
				 * the normal non-rels part NamespacePrefixMapper,
				 * since otherwise (because we'd be using 2 namespace
				 * prefix mappers?) we end up with errant xmlns="",
				 * which is wrong and stops Word 2007 from loading the
				 * document.
				 * 
				 * Note that xmlPackage.xsd defines:
				 * 	<xsd:complexType name="CT_XmlData">
						<xsd:sequence>
							<xsd:any processContents="skip" />
						</xsd:sequence>
				 *
				 * Note also that marshaltoString uses 
				 * just the normal non-rels part NamespacePrefixMapper,
				 * so if/when this is marshalled again, that could
				 * have been causing problems as well?? 
				 */
	        return w3cDoc.getDocumentElement();		        
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 		        
		return null;
		
	}

	public List<ConversionSectionWrapper> getList() {
		return conversionSections;
	}
	
	public void start() {
		currentSectionIndex = -1;
	}
	
	public void next() {
		currentSectionIndex++;
		currentSection = null;
		if ((currentSectionIndex >= 0) && (currentSectionIndex < conversionSections.size())) {
			currentSection = conversionSections.get(currentSectionIndex);
		}
		
	}
	
	public ConversionSectionWrapper getCurrentSection() {
		if (currentSection != null) 
			return currentSection;
		if (currentSectionIndex < 0) {
			throw new IllegalArgumentException("Trying to access a section, but moveNext wasn't called");
		}
		else if (currentSectionIndex >= conversionSections.size()) {
			throw new IllegalArgumentException("Trying to access more sections than avaiable");
		}
		else {
			throw new IllegalArgumentException("There is no current section"); //?????
		}
	}
}
