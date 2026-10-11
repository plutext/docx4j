package org.docx4j.openpackaging.parts.DrawingML;

import org.docx4j.com.microsoft.schemas.office.drawing.x2012.chartStyle.CTColorStyle;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;

/**
 * A chart's colour style part ({@code cs:colorStyle}, Office 2013 and later): the colours Office
 * gives series in turn.
 *
 * <p>Typed since 17.3.3 (CR-035 phase 0); it was generic, made raw, though it always held a
 * {@link CTColorStyle}.</p>
 *
 * @since 11.4.10
 */
public class ChartColorStylePart extends JaxbXmlPart<CTColorStyle> {

	public ChartColorStylePart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public ChartColorStylePart() throws InvalidFormatException {
		super(new PartName("/word/charts/colors1.xml")); 
		init();
	}
	
	public void init() {
				
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.CHART_COLOR_STYLE));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.CHART_COLOR_STYLE);
		
	}	
}
