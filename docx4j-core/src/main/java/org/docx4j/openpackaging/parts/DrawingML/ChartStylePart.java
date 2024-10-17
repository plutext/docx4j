package org.docx4j.openpackaging.parts.DrawingML;

import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;

/**
 * @since 11.4.10
 */
public class ChartStylePart<E> extends JaxbXmlPart<E> {

	public ChartStylePart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public ChartStylePart() throws InvalidFormatException {
		super(new PartName("/word/charts/style1.xml")); 
		init();
	}
	
	public void init() {
				
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.CHART_STYLE));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.CHART_STYLE);
		
	}	
}
