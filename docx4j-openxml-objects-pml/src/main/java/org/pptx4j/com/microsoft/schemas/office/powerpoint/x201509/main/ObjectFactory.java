
package org.pptx4j.com.microsoft.schemas.office.powerpoint.x201509.main;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.pptx4j.com.microsoft.schemas.office.powerpoint.x201509.main package. 
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

    private final static QName _Morph_QNAME = new QName("http://schemas.microsoft.com/office/powerpoint/2015/09/main", "morph");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.pptx4j.com.microsoft.schemas.office.powerpoint.x201509.main
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTMorphTransition }
     * 
     */
    public CTMorphTransition createCTMorphTransition() {
        return new CTMorphTransition();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTMorphTransition }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/powerpoint/2015/09/main", name = "morph")
    public JAXBElement<CTMorphTransition> createMorph(CTMorphTransition value) {
        return new JAXBElement<CTMorphTransition>(_Morph_QNAME, CTMorphTransition.class, null, value);
    }

}
