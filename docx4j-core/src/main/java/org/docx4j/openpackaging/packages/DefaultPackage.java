package org.docx4j.openpackaging.packages;

import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultPackage extends OpcPackage {

	protected static Logger log = LoggerFactory.getLogger(DefaultPackage.class);
		
	
	/**
	 * Constructor.  Also creates a new content type manager
	 * 
	 */	
	public DefaultPackage() {
		super();
		//setContentType(new ContentType(ContentTypes.PRESENTATIONML_MAIN)); 		
	}
	/**
	 * Constructor.
	 *  
	 * @param contentTypeManager
	 *            The content type manager to use 
	 */
	public DefaultPackage(ContentTypeManager contentTypeManager) {
		super(contentTypeManager);
		//setContentType(new ContentType(ContentTypes.PRESENTATIONML_MAIN));
	}
	
	
}
