
package org.docx4j.com.microsoft.schemas.office.x2006.encryption;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.com.microsoft.schemas.office.x2006.encryption package. 
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

    private final static QName _Encryption_QNAME = new QName("http://schemas.microsoft.com/office/2006/encryption", "encryption");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.com.microsoft.schemas.office.x2006.encryption
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTEncryption }
     * 
     */
    public CTEncryption createCTEncryption() {
        return new CTEncryption();
    }

    /**
     * Create an instance of {@link CTKeyEncryptor }
     * 
     */
    public CTKeyEncryptor createCTKeyEncryptor() {
        return new CTKeyEncryptor();
    }

    /**
     * Create an instance of {@link CTDataIntegrity }
     * 
     */
    public CTDataIntegrity createCTDataIntegrity() {
        return new CTDataIntegrity();
    }

    /**
     * Create an instance of {@link CTKeyData }
     * 
     */
    public CTKeyData createCTKeyData() {
        return new CTKeyData();
    }

    /**
     * Create an instance of {@link CTKeyEncryptors }
     * 
     */
    public CTKeyEncryptors createCTKeyEncryptors() {
        return new CTKeyEncryptors();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTEncryption }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/2006/encryption", name = "encryption")
    public JAXBElement<CTEncryption> createEncryption(CTEncryption value) {
        return new JAXBElement<CTEncryption>(_Encryption_QNAME, CTEncryption.class, null, value);
    }

}
