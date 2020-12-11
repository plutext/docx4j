
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chart;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import org.docx4j.dml.CTShapeProperties;
import org.docx4j.dml.chart.CTBoolean;
import org.docx4j.dml.chart.CTDLbl;
import org.docx4j.dml.chart.CTMarker;
import org.docx4j.dml.chart.CTUnsignedInt;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.com.microsoft.schemas.office.drawing.x2014.chart package. 
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

    private final static QName _Datapointuniqueidmap_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2014/chart", "datapointuniqueidmap");
    private final static QName _SpPr_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2014/chart", "spPr");
    private final static QName _Explosion_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2014/chart", "explosion");
    private final static QName _InvertIfNegative_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2014/chart", "invertIfNegative");
    private final static QName _Bubble3D_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2014/chart", "bubble3D");
    private final static QName _Marker_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2014/chart", "marker");
    private final static QName _DLbl_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2014/chart", "dLbl");
    private final static QName _CategoryFilterExceptions_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2014/chart", "categoryFilterExceptions");
    private final static QName _PivotOptions16_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2014/chart", "pivotOptions16");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.com.microsoft.schemas.office.drawing.x2014.chart
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTChartDataPointUniqueIDMap }
     * 
     */
    public CTChartDataPointUniqueIDMap createCTChartDataPointUniqueIDMap() {
        return new CTChartDataPointUniqueIDMap();
    }

    /**
     * Create an instance of {@link CTCategoryFilterExceptions }
     * 
     */
    public CTCategoryFilterExceptions createCTCategoryFilterExceptions() {
        return new CTCategoryFilterExceptions();
    }

    /**
     * Create an instance of {@link CTPivotOptions16 }
     * 
     */
    public CTPivotOptions16 createCTPivotOptions16() {
        return new CTPivotOptions16();
    }

    /**
     * Create an instance of {@link CTChartUniqueID }
     * 
     */
    public CTChartUniqueID createCTChartUniqueID() {
        return new CTChartUniqueID();
    }

    /**
     * Create an instance of {@link CTChartDataPointUniqueIDMapEntry }
     * 
     */
    public CTChartDataPointUniqueIDMapEntry createCTChartDataPointUniqueIDMapEntry() {
        return new CTChartDataPointUniqueIDMapEntry();
    }

    /**
     * Create an instance of {@link CTBooleanFalse }
     * 
     */
    public CTBooleanFalse createCTBooleanFalse() {
        return new CTBooleanFalse();
    }

    /**
     * Create an instance of {@link CTCategoryFilterException }
     * 
     */
    public CTCategoryFilterException createCTCategoryFilterException() {
        return new CTCategoryFilterException();
    }

    /**
     * Create an instance of {@link CTNumFilteredLiteralCache }
     * 
     */
    public CTNumFilteredLiteralCache createCTNumFilteredLiteralCache() {
        return new CTNumFilteredLiteralCache();
    }

    /**
     * Create an instance of {@link CTStrFilteredLiteralCache }
     * 
     */
    public CTStrFilteredLiteralCache createCTStrFilteredLiteralCache() {
        return new CTStrFilteredLiteralCache();
    }

    /**
     * Create an instance of {@link CTMultiLvlStrFilteredLiteralCache }
     * 
     */
    public CTMultiLvlStrFilteredLiteralCache createCTMultiLvlStrFilteredLiteralCache() {
        return new CTMultiLvlStrFilteredLiteralCache();
    }

    /**
     * Create an instance of {@link CTLiteralDataChart }
     * 
     */
    public CTLiteralDataChart createCTLiteralDataChart() {
        return new CTLiteralDataChart();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTChartDataPointUniqueIDMap }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2014/chart", name = "datapointuniqueidmap")
    public JAXBElement<CTChartDataPointUniqueIDMap> createDatapointuniqueidmap(CTChartDataPointUniqueIDMap value) {
        return new JAXBElement<CTChartDataPointUniqueIDMap>(_Datapointuniqueidmap_QNAME, CTChartDataPointUniqueIDMap.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTShapeProperties }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2014/chart", name = "spPr")
    public JAXBElement<CTShapeProperties> createSpPr(CTShapeProperties value) {
        return new JAXBElement<CTShapeProperties>(_SpPr_QNAME, CTShapeProperties.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTUnsignedInt }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2014/chart", name = "explosion")
    public JAXBElement<CTUnsignedInt> createExplosion(CTUnsignedInt value) {
        return new JAXBElement<CTUnsignedInt>(_Explosion_QNAME, CTUnsignedInt.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTBoolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2014/chart", name = "invertIfNegative")
    public JAXBElement<CTBoolean> createInvertIfNegative(CTBoolean value) {
        return new JAXBElement<CTBoolean>(_InvertIfNegative_QNAME, CTBoolean.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTBoolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2014/chart", name = "bubble3D")
    public JAXBElement<CTBoolean> createBubble3D(CTBoolean value) {
        return new JAXBElement<CTBoolean>(_Bubble3D_QNAME, CTBoolean.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTMarker }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2014/chart", name = "marker")
    public JAXBElement<CTMarker> createMarker(CTMarker value) {
        return new JAXBElement<CTMarker>(_Marker_QNAME, CTMarker.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTDLbl }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2014/chart", name = "dLbl")
    public JAXBElement<CTDLbl> createDLbl(CTDLbl value) {
        return new JAXBElement<CTDLbl>(_DLbl_QNAME, CTDLbl.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTCategoryFilterExceptions }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2014/chart", name = "categoryFilterExceptions")
    public JAXBElement<CTCategoryFilterExceptions> createCategoryFilterExceptions(CTCategoryFilterExceptions value) {
        return new JAXBElement<CTCategoryFilterExceptions>(_CategoryFilterExceptions_QNAME, CTCategoryFilterExceptions.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPivotOptions16 }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2014/chart", name = "pivotOptions16")
    public JAXBElement<CTPivotOptions16> createPivotOptions16(CTPivotOptions16 value) {
        return new JAXBElement<CTPivotOptions16>(_PivotOptions16_QNAME, CTPivotOptions16 .class, null, value);
    }

}
