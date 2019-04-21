package org.docx4j.openpackaging.packages;

import org.docx4j.openpackaging.PackageRelsUtil;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.parts.PartName;

/**
 * @author jharrop
 * @since 6.1.0
 */
public class WordprocessingMLTemplatePackage extends WordprocessingMLPackage {

	
	/**
	 * Constructor.  Also creates a new content type manager
	 * 
	 */	
	public WordprocessingMLTemplatePackage() {
		super();
		setContentType(new ContentType(ContentTypes.WORDPROCESSINGML_TEMPLATE));
	}
	/**
	 * Constructor.
	 *  
	 * @param contentTypeManager
	 *            The content type manager to use 
	 */
	public WordprocessingMLTemplatePackage(ContentTypeManager contentTypeManager) {
		super(contentTypeManager);
		setContentType(new ContentType(ContentTypes.WORDPROCESSINGML_TEMPLATE));
	}
	
	
}
