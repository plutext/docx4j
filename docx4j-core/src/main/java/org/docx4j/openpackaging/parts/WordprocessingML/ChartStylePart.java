package org.docx4j.openpackaging.parts.WordprocessingML;

import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;

@Deprecated
public class ChartStylePart<E> extends org.docx4j.openpackaging.parts.DrawingML.ChartStylePart<E> {

	public ChartStylePart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public ChartStylePart() throws InvalidFormatException {
		super(new PartName("/word/charts/style1.xml"));
		init();
	}
}
