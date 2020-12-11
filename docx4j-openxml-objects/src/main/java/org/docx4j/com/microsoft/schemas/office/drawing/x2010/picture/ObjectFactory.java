
package org.docx4j.com.microsoft.schemas.office.drawing.x2010.picture;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import org.docx4j.dml.CTOfficeArtExtensionList;
import org.docx4j.dml.CTShapeStyle;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.com.microsoft.schemas.office.drawing.x2010.picture package. 
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

    private final static QName _Style_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2010/picture", "style");
    private final static QName _ExtLst_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2010/picture", "extLst");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.com.microsoft.schemas.office.drawing.x2010.picture
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTShapeStyle }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2010/picture", name = "style")
    public JAXBElement<CTShapeStyle> createStyle(CTShapeStyle value) {
        return new JAXBElement<CTShapeStyle>(_Style_QNAME, CTShapeStyle.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTOfficeArtExtensionList }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2010/picture", name = "extLst")
    public JAXBElement<CTOfficeArtExtensionList> createExtLst(CTOfficeArtExtensionList value) {
        return new JAXBElement<CTOfficeArtExtensionList>(_ExtLst_QNAME, CTOfficeArtExtensionList.class, null, value);
    }

}
