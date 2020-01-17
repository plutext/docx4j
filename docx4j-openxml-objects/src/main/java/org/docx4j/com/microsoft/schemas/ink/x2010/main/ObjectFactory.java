
package org.docx4j.com.microsoft.schemas.ink.x2010.main;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.com.microsoft.schemas.ink.x2010.main package. 
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

    private final static QName _Context_QNAME = new QName("http://schemas.microsoft.com/ink/2010/main", "context");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.com.microsoft.schemas.ink.x2010.main
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTCtxNode }
     * 
     */
    public CTCtxNode createCTCtxNode() {
        return new CTCtxNode();
    }

    /**
     * Create an instance of {@link CTProperty }
     * 
     */
    public CTProperty createCTProperty() {
        return new CTProperty();
    }

    /**
     * Create an instance of {@link CTCtxLink }
     * 
     */
    public CTCtxLink createCTCtxLink() {
        return new CTCtxLink();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTCtxNode }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/ink/2010/main", name = "context")
    public JAXBElement<CTCtxNode> createContext(CTCtxNode value) {
        return new JAXBElement<CTCtxNode>(_Context_QNAME, CTCtxNode.class, null, value);
    }

}
