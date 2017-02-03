package org.docx4j.openpackaging.parts.SpreadsheetML;

import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.xlsx4j.sml.CTCellStyle;
import org.xlsx4j.sml.CTStylesheet;
import org.xlsx4j.sml.CTXf;

public class Styles  extends JaxbSmlPart<CTStylesheet> {
	
	public Styles(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public Styles() throws InvalidFormatException {
		super(new PartName("/xl/styles.xml"));
		init();
	}
	
	public void init() {
				
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.SPREADSHEETML_STYLES));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.SPREADSHEETML_STYLES);
		
	}
	
	/**
	 * Get the cell style by index.
	 * 
	 * @param idx
	 * @return
	 */
	public CTCellStyle getStyleByIndex(long idx) {
		
		if (this.getJaxbElement().getCellStyles()!=null) {
			return this.getJaxbElement().getCellStyles().getCellStyle().get((int)idx);
		}
		
		return null;
	}
	
	/**
	 *  Cells in the Sheet Part reference the xf (data format) records by zero-based index.
	 *  
	 * @param idx
	 * @return
	 */
	public CTXf getXfByIndex(long idx) {

		if (this.getJaxbElement().getCellXfs()!=null) {
			return this.getJaxbElement().getCellXfs().getXf().get( (int)idx);
		}
		
		return null;
		
	}

}