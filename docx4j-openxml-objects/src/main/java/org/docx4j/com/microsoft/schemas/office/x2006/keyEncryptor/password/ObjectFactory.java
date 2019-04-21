
package org.docx4j.com.microsoft.schemas.office.x2006.keyEncryptor.password;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.com.microsoft.schemas.office.x2006.keyEncryptor.password package. 
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

    private final static QName _EncryptedKey_QNAME = new QName("http://schemas.microsoft.com/office/2006/keyEncryptor/password", "encryptedKey");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.com.microsoft.schemas.office.x2006.keyEncryptor.password
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTPasswordKeyEncryptor }
     * 
     */
    public CTPasswordKeyEncryptor createCTPasswordKeyEncryptor() {
        return new CTPasswordKeyEncryptor();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPasswordKeyEncryptor }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/2006/keyEncryptor/password", name = "encryptedKey")
    public JAXBElement<CTPasswordKeyEncryptor> createEncryptedKey(CTPasswordKeyEncryptor value) {
        return new JAXBElement<CTPasswordKeyEncryptor>(_EncryptedKey_QNAME, CTPasswordKeyEncryptor.class, null, value);
    }

}
