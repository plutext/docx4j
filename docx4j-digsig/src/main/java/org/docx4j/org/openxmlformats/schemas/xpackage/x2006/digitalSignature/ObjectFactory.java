
package org.docx4j.org.openxmlformats.schemas.xpackage.x2006.digitalSignature;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.org.openxmlformats.schemas.xpackage.x2006.digitalSignature package. 
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

    private final static QName _RelationshipsGroupReference_QNAME = new QName("http://schemas.openxmlformats.org/package/2006/digital-signature", "RelationshipsGroupReference");
    private final static QName _SignatureTime_QNAME = new QName("http://schemas.openxmlformats.org/package/2006/digital-signature", "SignatureTime");
    private final static QName _RelationshipReference_QNAME = new QName("http://schemas.openxmlformats.org/package/2006/digital-signature", "RelationshipReference");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.org.openxmlformats.schemas.xpackage.x2006.digitalSignature
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTRelationshipsGroupReference }
     * 
     */
    public CTRelationshipsGroupReference createCTRelationshipsGroupReference() {
        return new CTRelationshipsGroupReference();
    }

    /**
     * Create an instance of {@link Docx4JRelationshipReferences }
     * 
     */
    public Docx4JRelationshipReferences createDocx4JRelationshipReferences() {
        return new Docx4JRelationshipReferences();
    }

    /**
     * Create an instance of {@link CTRelationshipReference }
     * 
     */
    public CTRelationshipReference createCTRelationshipReference() {
        return new CTRelationshipReference();
    }

    /**
     * Create an instance of {@link CTSignatureTime }
     * 
     */
    public CTSignatureTime createCTSignatureTime() {
        return new CTSignatureTime();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTRelationshipsGroupReference }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/package/2006/digital-signature", name = "RelationshipsGroupReference")
    public JAXBElement<CTRelationshipsGroupReference> createRelationshipsGroupReference(CTRelationshipsGroupReference value) {
        return new JAXBElement<CTRelationshipsGroupReference>(_RelationshipsGroupReference_QNAME, CTRelationshipsGroupReference.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTSignatureTime }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/package/2006/digital-signature", name = "SignatureTime")
    public JAXBElement<CTSignatureTime> createSignatureTime(CTSignatureTime value) {
        return new JAXBElement<CTSignatureTime>(_SignatureTime_QNAME, CTSignatureTime.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTRelationshipReference }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/package/2006/digital-signature", name = "RelationshipReference")
    public JAXBElement<CTRelationshipReference> createRelationshipReference(CTRelationshipReference value) {
        return new JAXBElement<CTRelationshipReference>(_RelationshipReference_QNAME, CTRelationshipReference.class, null, value);
    }

}
