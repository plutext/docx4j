
package org.pptx4j.com.microsoft.schemas.office.powerpoint.x201510.main;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.pptx4j.com.microsoft.schemas.office.powerpoint.x201510.main package. 
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

    private final static QName _RevInfo_QNAME = new QName("http://schemas.microsoft.com/office/powerpoint/2015/10/main", "revInfo");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.pptx4j.com.microsoft.schemas.office.powerpoint.x201510.main
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTRevisionInfo }
     * 
     */
    public CTRevisionInfo createCTRevisionInfo() {
        return new CTRevisionInfo();
    }

    /**
     * Create an instance of {@link CTClientRevision }
     * 
     */
    public CTClientRevision createCTClientRevision() {
        return new CTClientRevision();
    }

    /**
     * Create an instance of {@link CTClientRevisionList }
     * 
     */
    public CTClientRevisionList createCTClientRevisionList() {
        return new CTClientRevisionList();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTRevisionInfo }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/powerpoint/2015/10/main", name = "revInfo")
    public JAXBElement<CTRevisionInfo> createRevInfo(CTRevisionInfo value) {
        return new JAXBElement<CTRevisionInfo>(_RevInfo_QNAME, CTRevisionInfo.class, null, value);
    }

}
