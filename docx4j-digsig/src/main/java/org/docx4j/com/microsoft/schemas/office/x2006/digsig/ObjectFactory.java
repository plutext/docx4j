
package org.docx4j.com.microsoft.schemas.office.x2006.digsig;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.com.microsoft.schemas.office.x2006.digsig package. 
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

    private final static QName _SignatureInfoV2_QNAME = new QName("http://schemas.microsoft.com/office/2006/digsig", "SignatureInfoV2");
    private final static QName _SignatureInfoV1_QNAME = new QName("http://schemas.microsoft.com/office/2006/digsig", "SignatureInfoV1");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.com.microsoft.schemas.office.x2006.digsig
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTSignatureInfoV2 }
     * 
     */
    public CTSignatureInfoV2 createCTSignatureInfoV2() {
        return new CTSignatureInfoV2();
    }

    /**
     * Create an instance of {@link CTSignatureInfoV1 }
     * 
     */
    public CTSignatureInfoV1 createCTSignatureInfoV1() {
        return new CTSignatureInfoV1();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTSignatureInfoV2 }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/2006/digsig", name = "SignatureInfoV2")
    public JAXBElement<CTSignatureInfoV2> createSignatureInfoV2(CTSignatureInfoV2 value) {
        return new JAXBElement<CTSignatureInfoV2>(_SignatureInfoV2_QNAME, CTSignatureInfoV2 .class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTSignatureInfoV1 }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/2006/digsig", name = "SignatureInfoV1")
    public JAXBElement<CTSignatureInfoV1> createSignatureInfoV1(CTSignatureInfoV1 value) {
        return new JAXBElement<CTSignatureInfoV1>(_SignatureInfoV1_QNAME, CTSignatureInfoV1 .class, null, value);
    }

}
