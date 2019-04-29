
package org.docx4j.dml.chart.x2007;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.dml.chart.x2007 package. 
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

    private final static QName _PivotOptions_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2007/8/2/chart", "pivotOptions");
    private final static QName _InvertSolidFillFmt_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2007/8/2/chart", "invertSolidFillFmt");
    private final static QName _Style_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2007/8/2/chart", "style");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.dml.chart.x2007
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTPivotOptions }
     * 
     */
    public CTPivotOptions createCTPivotOptions() {
        return new CTPivotOptions();
    }

    /**
     * Create an instance of {@link CTInvertSolidFillFmt }
     * 
     */
    public CTInvertSolidFillFmt createCTInvertSolidFillFmt() {
        return new CTInvertSolidFillFmt();
    }

    /**
     * Create an instance of {@link CTStyle }
     * 
     */
    public CTStyle createCTStyle() {
        return new CTStyle();
    }

    /**
     * Create an instance of {@link CTBooleanTrue }
     * 
     */
    public CTBooleanTrue createCTBooleanTrue() {
        return new CTBooleanTrue();
    }

    /**
     * Create an instance of {@link CTBooleanFalse }
     * 
     */
    public CTBooleanFalse createCTBooleanFalse() {
        return new CTBooleanFalse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPivotOptions }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2007/8/2/chart", name = "pivotOptions")
    public JAXBElement<CTPivotOptions> createPivotOptions(CTPivotOptions value) {
        return new JAXBElement<CTPivotOptions>(_PivotOptions_QNAME, CTPivotOptions.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTInvertSolidFillFmt }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2007/8/2/chart", name = "invertSolidFillFmt")
    public JAXBElement<CTInvertSolidFillFmt> createInvertSolidFillFmt(CTInvertSolidFillFmt value) {
        return new JAXBElement<CTInvertSolidFillFmt>(_InvertSolidFillFmt_QNAME, CTInvertSolidFillFmt.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTStyle }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2007/8/2/chart", name = "style")
    public JAXBElement<CTStyle> createStyle(CTStyle value) {
        return new JAXBElement<CTStyle>(_Style_QNAME, CTStyle.class, null, value);
    }

}
