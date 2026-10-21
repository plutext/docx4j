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
 * Until 17.3.4 this worked except in 3 cases:
 * (i)  where binder is used (should now work in 11.4.7 and later)
 * (ii) where we take the JAXBResult of a transformation (so instead, use a DOMResult) 
 * since you can't register a listener in either of these cases, and
 * (iii)where you have added mc:Choice content to the docx yourself.
 * 
 * Since 17.3.4 (CR-052) a marshal reads every mc:Choice from the tree itself
 * ({@link McDeclarations}), so a Choice added, moved or copied in needs no registration
 * for a prefix docx4j's table knows; a prefix it does not know is declared on the Choice
 * with the binding recorded at unmarshal ({@link McSourceBindings}), or the one your code
 * sets with {@link McSourceBindings#set}.  addMcChoiceNamespace still works, for the root.
 *  
 * @author jharrop
 */
public interface McIgnorableNamespaceDeclarator {
	
	public void setMcIgnorable(String mcIgnorable);

	/**
	 * The value {@link #setMcIgnorable} last set (on this thread, for the runtime mappers), so
	 * that a marshal can put back what it found when it is done: a marshal nested in another
	 * then leaves the outer one's value in place, and one on its own leaves the thread as it was
	 * (CR-049).  Null where none is set, and for a mapper that keeps none (the relationships
	 * part's).
	 *
	 * @since 17.3.4
	 */
	default String getMcIgnorable() {
		return null;
	}

}
