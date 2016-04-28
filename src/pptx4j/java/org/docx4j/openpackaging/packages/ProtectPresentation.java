/**
 * 
 */
package org.docx4j.openpackaging.packages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unlike docx and xlsx, there are no additional protection 
 * settings for a pptx,just those in ProtectionSettings.
 * 
 * @author jharrop
 *
 */
public class ProtectPresentation extends ProtectionSettings {
	
	protected static Logger log = LoggerFactory.getLogger(ProtectPresentation.class);
	
	
	public ProtectPresentation(PresentationMLPackage pkg) {
		super(pkg);
	}
	
}
