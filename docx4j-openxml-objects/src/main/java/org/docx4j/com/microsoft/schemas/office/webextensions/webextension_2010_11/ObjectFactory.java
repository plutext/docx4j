
package org.docx4j.com.microsoft.schemas.office.webextensions.webextension_2010_11;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.com.microsoft.schemas.office.webextensions.webextension_2010_11 package. 
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

    private final static QName _Webextension_QNAME = new QName("http://schemas.microsoft.com/office/webextensions/webextension/2010/11", "webextension");
    private final static QName _Webextensionref_QNAME = new QName("http://schemas.microsoft.com/office/webextensions/webextension/2010/11", "webextensionref");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.com.microsoft.schemas.office.webextensions.webextension_2010_11
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTOsfWebExtension }
     * 
     */
    public CTOsfWebExtension createCTOsfWebExtension() {
        return new CTOsfWebExtension();
    }

    /**
     * Create an instance of {@link CTWebExtensionPartRef }
     * 
     */
    public CTWebExtensionPartRef createCTWebExtensionPartRef() {
        return new CTWebExtensionPartRef();
    }

    /**
     * Create an instance of {@link CTOsfWebExtensionProperty }
     * 
     */
    public CTOsfWebExtensionProperty createCTOsfWebExtensionProperty() {
        return new CTOsfWebExtensionProperty();
    }

    /**
     * Create an instance of {@link CTOsfWebExtensionPropertyBag }
     * 
     */
    public CTOsfWebExtensionPropertyBag createCTOsfWebExtensionPropertyBag() {
        return new CTOsfWebExtensionPropertyBag();
    }

    /**
     * Create an instance of {@link CTOsfWebExtensionBinding }
     * 
     */
    public CTOsfWebExtensionBinding createCTOsfWebExtensionBinding() {
        return new CTOsfWebExtensionBinding();
    }

    /**
     * Create an instance of {@link CTOsfWebExtensionBindingList }
     * 
     */
    public CTOsfWebExtensionBindingList createCTOsfWebExtensionBindingList() {
        return new CTOsfWebExtensionBindingList();
    }

    /**
     * Create an instance of {@link CTOsfWebExtensionReference }
     * 
     */
    public CTOsfWebExtensionReference createCTOsfWebExtensionReference() {
        return new CTOsfWebExtensionReference();
    }

    /**
     * Create an instance of {@link CTOsfWebExtensionReferenceList }
     * 
     */
    public CTOsfWebExtensionReferenceList createCTOsfWebExtensionReferenceList() {
        return new CTOsfWebExtensionReferenceList();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTOsfWebExtension }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/webextensions/webextension/2010/11", name = "webextension")
    public JAXBElement<CTOsfWebExtension> createWebextension(CTOsfWebExtension value) {
        return new JAXBElement<CTOsfWebExtension>(_Webextension_QNAME, CTOsfWebExtension.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTWebExtensionPartRef }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/webextensions/webextension/2010/11", name = "webextensionref")
    public JAXBElement<CTWebExtensionPartRef> createWebextensionref(CTWebExtensionPartRef value) {
        return new JAXBElement<CTWebExtensionPartRef>(_Webextensionref_QNAME, CTWebExtensionPartRef.class, null, value);
    }

}
