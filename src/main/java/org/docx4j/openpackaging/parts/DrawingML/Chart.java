package org.docx4j.openpackaging.parts.DrawingML;

import javax.xml.bind.JAXBElement;

import org.docx4j.dml.CTTableStyleList;
import org.docx4j.dml.chart.CTChartSpace;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.xlsx4j.jaxb.Context;
import org.xlsx4j.sml.CTStylesheet;

public class Chart  extends JaxbDmlPart<CTChartSpace> {
	// This part uses a JAXB content model from dml,
	// so we need to use that context.
	// Hence this class doesn't extent JaxbPmlPart.
	
	public Chart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public Chart() throws InvalidFormatException {
		super(new PartName("/word/charts/chart1.xml"));  // In a .xlsx could be "/xl/charts/chart1.xml"?
		init();
	}
	
	public void init() {
				
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.DRAWINGML_CHART));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.SPREADSHEETML_CHART);
		
	}

}