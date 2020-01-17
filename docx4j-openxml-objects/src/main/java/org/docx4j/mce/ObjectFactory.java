
package org.docx4j.mce;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.mce package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.mce
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link AlternateContent }
     * 
     */
    public AlternateContent createAlternateContent() {
        return new AlternateContent();
    }

    /**
     * Create an instance of {@link AlternateContent.Choice }
     * 
     */
    public AlternateContent.Choice createAlternateContentChoice() {
        return new AlternateContent.Choice();
    }

    /**
     * Create an instance of {@link AlternateContent.Fallback }
     * 
     */
    public AlternateContent.Fallback createAlternateContentFallback() {
        return new AlternateContent.Fallback();
    }

}
