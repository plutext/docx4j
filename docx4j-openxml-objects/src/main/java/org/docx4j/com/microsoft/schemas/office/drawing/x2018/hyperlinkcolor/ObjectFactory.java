
package org.docx4j.com.microsoft.schemas.office.drawing.x2018.hyperlinkcolor;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.com.microsoft.schemas.office.drawing.x2018.hyperlinkcolor package. 
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

    private final static QName _HlinkClr_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2018/hyperlinkcolor", "hlinkClr");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.com.microsoft.schemas.office.drawing.x2018.hyperlinkcolor
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTHyperlinkColor }
     * 
     */
    public CTHyperlinkColor createCTHyperlinkColor() {
        return new CTHyperlinkColor();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTHyperlinkColor }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2018/hyperlinkcolor", name = "hlinkClr")
    public JAXBElement<CTHyperlinkColor> createHlinkClr(CTHyperlinkColor value) {
        return new JAXBElement<CTHyperlinkColor>(_HlinkClr_QNAME, CTHyperlinkColor.class, null, value);
    }

}
