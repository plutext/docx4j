
package org.docx4j.com.microsoft.schemas.office.thememl.x2012.main;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.com.microsoft.schemas.office.thememl.x2012.main package. 
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

    private final static QName _ThemeFamily_QNAME = new QName("http://schemas.microsoft.com/office/thememl/2012/main", "themeFamily");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.com.microsoft.schemas.office.thememl.x2012.main
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTThemeFamily }
     * 
     */
    public CTThemeFamily createCTThemeFamily() {
        return new CTThemeFamily();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTThemeFamily }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/thememl/2012/main", name = "themeFamily")
    public JAXBElement<CTThemeFamily> createThemeFamily(CTThemeFamily value) {
        return new JAXBElement<CTThemeFamily>(_ThemeFamily_QNAME, CTThemeFamily.class, null, value);
    }

}
