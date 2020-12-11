
package org.docx4j.com.microsoft.schemas.office.powerpoint.x2014.inkAction;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.com.microsoft.schemas.office.powerpoint.x2014.inkAction package. 
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

    private final static QName _Actions_QNAME = new QName("http://schemas.microsoft.com/office/powerpoint/2014/inkAction", "actions");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.com.microsoft.schemas.office.powerpoint.x2014.inkAction
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTActions }
     * 
     */
    public CTActions createCTActions() {
        return new CTActions();
    }

    /**
     * Create an instance of {@link CTActionData }
     * 
     */
    public CTActionData createCTActionData() {
        return new CTActionData();
    }

    /**
     * Create an instance of {@link CTActionDataGroup }
     * 
     */
    public CTActionDataGroup createCTActionDataGroup() {
        return new CTActionDataGroup();
    }

    /**
     * Create an instance of {@link CTActionProperty }
     * 
     */
    public CTActionProperty createCTActionProperty() {
        return new CTActionProperty();
    }

    /**
     * Create an instance of {@link CTAction }
     * 
     */
    public CTAction createCTAction() {
        return new CTAction();
    }

    /**
     * Create an instance of {@link CTActionGroup }
     * 
     */
    public CTActionGroup createCTActionGroup() {
        return new CTActionGroup();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTActions }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/powerpoint/2014/inkAction", name = "actions")
    public JAXBElement<CTActions> createActions(CTActions value) {
        return new JAXBElement<CTActions>(_Actions_QNAME, CTActions.class, null, value);
    }

}
