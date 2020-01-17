
package org.docx4j.com.microsoft.schemas.office.x2006.keyEncryptor.certificate;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.com.microsoft.schemas.office.x2006.keyEncryptor.certificate package. 
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

    private final static QName _EncryptedKey_QNAME = new QName("http://schemas.microsoft.com/office/2006/keyEncryptor/certificate", "encryptedKey");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.com.microsoft.schemas.office.x2006.keyEncryptor.certificate
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTCertificateKeyEncryptor }
     * 
     */
    public CTCertificateKeyEncryptor createCTCertificateKeyEncryptor() {
        return new CTCertificateKeyEncryptor();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTCertificateKeyEncryptor }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/2006/keyEncryptor/certificate", name = "encryptedKey")
    public JAXBElement<CTCertificateKeyEncryptor> createEncryptedKey(CTCertificateKeyEncryptor value) {
        return new JAXBElement<CTCertificateKeyEncryptor>(_EncryptedKey_QNAME, CTCertificateKeyEncryptor.class, null, value);
    }

}
