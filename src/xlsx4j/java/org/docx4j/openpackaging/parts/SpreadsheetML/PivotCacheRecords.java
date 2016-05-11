package org.docx4j.openpackaging.parts.SpreadsheetML;

import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.xlsx4j.sml.CTPivotCacheRecords;

public class PivotCacheRecords  extends JaxbSmlPart<CTPivotCacheRecords> {
	
	public PivotCacheRecords(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public PivotCacheRecords() throws InvalidFormatException {
		super(new PartName("/xl/pivotCache/pivotCacheRecords1.xml"));
		init();
	}
	
	public void init() {
				
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.SPREADSHEETML_PIVOT_CACHE_RECORDS));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.SPREADSHEETML_PIVOT_CACHE_RECORDS);
		
	}

}