package org.docx4j.openpackaging.parts.WordprocessingML;

import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;

@Deprecated
public class ChartColorStylePart<E> extends org.docx4j.openpackaging.parts.DrawingML.ChartColorStylePart<E> {

	public ChartColorStylePart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public ChartColorStylePart() throws InvalidFormatException {
		super(new PartName("/word/charts/colors1.xml"));
		init();
	}
	
}
