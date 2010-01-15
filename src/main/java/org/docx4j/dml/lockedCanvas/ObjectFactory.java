
package org.docx4j.dml.lockedCanvas;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import org.docx4j.dml.CTGvmlGroupShape;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.dml.lockedCanvas package. 
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

    private final static QName _LockedCanvas_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/lockedCanvas", "lockedCanvas");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.dml.lockedCanvas
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTGvmlGroupShape }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/lockedCanvas", name = "lockedCanvas")
    public JAXBElement<CTGvmlGroupShape> createLockedCanvas(CTGvmlGroupShape value) {
        return new JAXBElement<CTGvmlGroupShape>(_LockedCanvas_QNAME, CTGvmlGroupShape.class, null, value);
    }

}
