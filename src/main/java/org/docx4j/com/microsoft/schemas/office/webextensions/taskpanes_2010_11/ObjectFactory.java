
package org.docx4j.com.microsoft.schemas.office.webextensions.taskpanes_2010_11;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.com.microsoft.schemas.office.webextensions.taskpanes_2010_11 package. 
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

    private final static QName _Taskpanes_QNAME = new QName("http://schemas.microsoft.com/office/webextensions/taskpanes/2010/11", "taskpanes");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.com.microsoft.schemas.office.webextensions.taskpanes_2010_11
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTOsfTaskpanes }
     * 
     */
    public CTOsfTaskpanes createCTOsfTaskpanes() {
        return new CTOsfTaskpanes();
    }

    /**
     * Create an instance of {@link CTOsfTaskpane }
     * 
     */
    public CTOsfTaskpane createCTOsfTaskpane() {
        return new CTOsfTaskpane();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTOsfTaskpanes }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/webextensions/taskpanes/2010/11", name = "taskpanes")
    public JAXBElement<CTOsfTaskpanes> createTaskpanes(CTOsfTaskpanes value) {
        return new JAXBElement<CTOsfTaskpanes>(_Taskpanes_QNAME, CTOsfTaskpanes.class, null, value);
    }

}
