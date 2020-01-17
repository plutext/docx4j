
package org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingGroup;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingGroup package. 
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

    private final static QName _Wgp_QNAME = new QName("http://schemas.microsoft.com/office/word/2010/wordprocessingGroup", "wgp");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingGroup
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTWordprocessingGroup }
     * 
     */
    public CTWordprocessingGroup createCTWordprocessingGroup() {
        return new CTWordprocessingGroup();
    }

    /**
     * Create an instance of {@link CTGraphicFrame }
     * 
     */
    public CTGraphicFrame createCTGraphicFrame() {
        return new CTGraphicFrame();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTWordprocessingGroup }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2010/wordprocessingGroup", name = "wgp")
    public JAXBElement<CTWordprocessingGroup> createWgp(CTWordprocessingGroup value) {
        return new JAXBElement<CTWordprocessingGroup>(_Wgp_QNAME, CTWordprocessingGroup.class, null, value);
    }

}
