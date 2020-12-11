
package org.pptx4j.com.microsoft.schemas.office.powerpoint.x2015.main;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.pptx4j.com.microsoft.schemas.office.powerpoint.x2015.main package. 
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

    private final static QName _DesignElem_QNAME = new QName("http://schemas.microsoft.com/office/powerpoint/2015/main", "designElem");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.pptx4j.com.microsoft.schemas.office.powerpoint.x2015.main
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTDesignElement }
     * 
     */
    public CTDesignElement createCTDesignElement() {
        return new CTDesignElement();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTDesignElement }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/powerpoint/2015/main", name = "designElem")
    public JAXBElement<CTDesignElement> createDesignElem(CTDesignElement value) {
        return new JAXBElement<CTDesignElement>(_DesignElem_QNAME, CTDesignElement.class, null, value);
    }

}
