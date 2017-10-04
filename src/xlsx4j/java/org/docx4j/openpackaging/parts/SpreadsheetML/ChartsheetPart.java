package org.docx4j.openpackaging.parts.SpreadsheetML;

import javax.xml.bind.JAXBException;

import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.xlsx4j.sml.CTChartsheet;

public class ChartsheetPart extends JaxbSmlPart<CTChartsheet> {
	
	public ChartsheetPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public ChartsheetPart() throws InvalidFormatException {
		super(new PartName("/xl/chartsheets/sheet1.xml"));
		init();
	}
	
	public void init() {
				
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.SPREADSHEETML_CHARTSHEET));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.SPREADSHEETML_CHARTSHEET);
		
	}
	
	@Override
	public void setJaxbElement(CTChartsheet jaxbElement) {
		super.setJaxbElement(jaxbElement);
		jaxbElement.setParent(this); // if you create a new ChartsheetPart
	}
	
	@Override
    public CTChartsheet unmarshal( java.io.InputStream is ) throws JAXBException {
		
		CTChartsheet w = super.unmarshal(is);
		w.setParent(this); // workaround for JAXB in Java 8 setting parent to JAXBElement!
		return w;
	}
	
	@Override
    public CTChartsheet unmarshal(org.w3c.dom.Element el) throws JAXBException {

		CTChartsheet w = super.unmarshal(el);
		w.setParent(this); // presume JAXB gets in wrong here too
		return w;
		
	}
	
	/**
	 * Get the WorkbookPart.
	 * 
	 * @return
	 * @since 3.3.3
	 */
	public WorkbookPart getWorkbookPart() {
		
		return ((SpreadsheetMLPackage)this.getPackage()).getWorkbookPart();
		
	}
}