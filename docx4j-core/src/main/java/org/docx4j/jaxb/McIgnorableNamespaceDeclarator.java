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
 * Note that there is a difference between the prefixes which need to be
 * in mc:Ignorable, and the requirement to pre-declare namespaces used
 * in mc:Choice (eg Requires="wpg")
 * 
 * But since both require the namespace to be pre-declared, this mechanism is 
 * used for both. (search for getMcChoiceNamespaces)
 * 
 * In order to pre-declare any namespaces used in mc:Choice (eg Requires="wpg"),
 * an Unmarshaller.Listener (Docx4jUnmarshallerListener) detects and records them.  
 * 
 * This works, except in 3 cases:
 * (i)  where binder is used
 * (ii) where we take the JAXBResult of a transformation (eg running the pre-processor) 
 * since you can't register a listener in either of these cases, and
 * (iii)where you have added mc:Choice content to the docx yourself.
 * 
 * If you have mc:Choice content in one of these cases, then you need to 
 * manage the mcChoiceNamespaces manually.  On your part object, invoke addMcChoiceNamespace.  
 *  
 * @author jharrop
 */
public interface McIgnorableNamespaceDeclarator {
	
	public void setMcIgnorable(String mcIgnorable);

}
