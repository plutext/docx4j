
package org.docx4j.dml;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.dml package. 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.dml
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link FontCollection.Font }
     * 
     */
    public FontCollection.Font createFontCollectionFont() {
        return new FontCollection.Font();
    }

    /**
     * Create an instance of {@link FontCollection }
     * 
     */
    public FontCollection createFontCollection() {
        return new FontCollection();
    }

    /**
     * Create an instance of {@link BaseStyles }
     * 
     */
    public BaseStyles createBaseStyles() {
        return new BaseStyles();
    }

    /**
     * Create an instance of {@link Theme }
     * 
     */
    public Theme createTheme() {
        return new Theme();
    }

    /**
     * Create an instance of {@link BaseStyles.FontScheme }
     * 
     */
    public BaseStyles.FontScheme createBaseStylesFontScheme() {
        return new BaseStyles.FontScheme();
    }

    /**
     * Create an instance of {@link TextFont }
     * 
     */
    public TextFont createTextFont() {
        return new TextFont();
    }

}
