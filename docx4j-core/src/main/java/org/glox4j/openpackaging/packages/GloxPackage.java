package org.glox4j.openpackaging.packages;

import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.DrawingML.DiagramLayoutHeaderPart;
import org.docx4j.openpackaging.parts.DrawingML.DiagramLayoutPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 2.7.0
 * @author jharrop
 *
 */
public class GloxPackage  extends OpcPackage {
	
	protected static Logger log = LoggerFactory.getLogger(GloxPackage.class);
		
	
	/**
	 * Constructor.  Also creates a new content type manager
	 * 
	 */	
	public GloxPackage() {
		super();
		//setContentType(new ContentType(ContentTypes.PRESENTATIONML_MAIN)); 		
	}
	/**
	 * Constructor.
	 *  
	 * @param contentTypeManager
	 *            The content type manager to use 
	 */
	public GloxPackage(ContentTypeManager contentTypeManager) {
		super(contentTypeManager);
		//setContentType(new ContentType(ContentTypes.PRESENTATIONML_MAIN));
	}
	
	public static GloxPackage load(java.io.File gloxFile) throws Docx4JException {
		
		return (GloxPackage)OpcPackage.load(gloxFile);
	}
	
	public boolean setPartShortcut(Part part, String relationshipType) {
		if (relationshipType.equals(Namespaces.DRAWINGML_DIAGRAM_LAYOUT)) {
			diagramLayoutPart = (DiagramLayoutPart)part;
			log.info("Set shortcut for diagramLayoutPart");
			return true;			
		} else if (relationshipType.equals(Namespaces.DRAWINGML_DIAGRAM_LAYOUT_HEADER)) {
			diagramLayoutHeaderPart = (DiagramLayoutHeaderPart)part;
			log.info("Set shortcut for diagramLayoutHeaderPart");
			return true;			
		} else {	
			return false;
		}
	}
	
	private DiagramLayoutPart diagramLayoutPart;
	public DiagramLayoutPart getDiagramLayoutPart() {
		return diagramLayoutPart;
	}
	
	private DiagramLayoutHeaderPart diagramLayoutHeaderPart;
	public DiagramLayoutHeaderPart getDiagramLayoutHeaderPart() {
		return diagramLayoutHeaderPart;
	}
	

}
