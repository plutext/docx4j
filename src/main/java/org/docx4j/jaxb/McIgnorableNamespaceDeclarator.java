/**
 * 
 */
package org.docx4j.jaxb;

/**
 * Docx4j will ensure that each namespace in mc:Ignorable is properly declared
 * (as it needs to be in order for Office to open the file), 
 * but if you add mc:Ignorable content, it is still generally up to you to set the mc:Ignorable 
 * attribute correctly.  Exceptions are DocumentSettingsPart, where docx4j
 * tries to set it correctly for you, and pptx where currently "v" is assumed.
 * 
 * @author jharrop
 */
public interface McIgnorableNamespaceDeclarator {
	
	public void setMcIgnorable(String mcIgnorable);

}
