
package org.xlsx4j.schemas.microsoft.com.office.excel.x2010.spreadsheetDrawing;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.com.microsoft.schemas.office.excel.x2010.spreadsheetDrawing package. 
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

    private final static QName _ContentPart_QNAME = new QName("http://schemas.microsoft.com/office/excel/2010/spreadsheetDrawing", "contentPart");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.com.microsoft.schemas.office.excel.x2010.spreadsheetDrawing
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTContentPart }
     * 
     */
    public CTContentPart createCTContentPart() {
        return new CTContentPart();
    }

    /**
     * Create an instance of {@link CTApplicationNonVisualDrawingProps }
     * 
     */
    public CTApplicationNonVisualDrawingProps createCTApplicationNonVisualDrawingProps() {
        return new CTApplicationNonVisualDrawingProps();
    }

    /**
     * Create an instance of {@link CTContentPartNonVisual }
     * 
     */
    public CTContentPartNonVisual createCTContentPartNonVisual() {
        return new CTContentPartNonVisual();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTContentPart }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/excel/2010/spreadsheetDrawing", name = "contentPart")
    public JAXBElement<CTContentPart> createContentPart(CTContentPart value) {
        return new JAXBElement<CTContentPart>(_ContentPart_QNAME, CTContentPart.class, null, value);
    }

}
