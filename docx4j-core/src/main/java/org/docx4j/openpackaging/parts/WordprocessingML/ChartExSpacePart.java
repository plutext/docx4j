package org.docx4j.openpackaging.parts.WordprocessingML;

import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;


@Deprecated
public class ChartExSpacePart extends org.docx4j.openpackaging.parts.DrawingML.ChartExSpacePart {

	public ChartExSpacePart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public ChartExSpacePart() throws InvalidFormatException {
		super(new PartName("/word/charts/chartEx1.xml"));
		init();
	}
	
}
