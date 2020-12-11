
package org.docx4j.w15symex;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.w15symex package. 
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

    private final static QName _SymEx_QNAME = new QName("http://schemas.microsoft.com/office/word/2015/wordml/symex", "symEx");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.w15symex
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTSymEx }
     * 
     */
    public CTSymEx createCTSymEx() {
        return new CTSymEx();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTSymEx }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2015/wordml/symex", name = "symEx")
    public JAXBElement<CTSymEx> createSymEx(CTSymEx value) {
        return new JAXBElement<CTSymEx>(_SymEx_QNAME, CTSymEx.class, null, value);
    }

}
