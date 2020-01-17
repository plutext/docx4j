
package org.docx4j.com.microsoft.schemas.office.drawing.x2012.chart;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import org.docx4j.dml.CTShapeProperties;
import org.docx4j.dml.chart.CTBoolean;
import org.docx4j.dml.chart.CTChartLines;
import org.docx4j.dml.chart.CTLayout;
import org.docx4j.dml.chart.CTNumFmt;
import org.docx4j.dml.chart.CTPivotSource;
import org.docx4j.dml.chart.CTTx;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.com.microsoft.schemas.office.drawing.x2012.chart package. 
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

    private final static QName _PivotSource_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2012/chart", "pivotSource");
    private final static QName _NumFmt_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2012/chart", "numFmt");
    private final static QName _SpPr_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2012/chart", "spPr");
    private final static QName _Layout_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2012/chart", "layout");
    private final static QName _FullRef_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2012/chart", "fullRef");
    private final static QName _LevelRef_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2012/chart", "levelRef");
    private final static QName _FormulaRef_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2012/chart", "formulaRef");
    private final static QName _FilteredSeriesTitle_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2012/chart", "filteredSeriesTitle");
    private final static QName _FilteredCategoryTitle_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2012/chart", "filteredCategoryTitle");
    private final static QName _FilteredAreaSeries_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2012/chart", "filteredAreaSeries");
    private final static QName _FilteredBarSeries_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2012/chart", "filteredBarSeries");
    private final static QName _FilteredBubbleSeries_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2012/chart", "filteredBubbleSeries");
    private final static QName _FilteredLineSeries_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2012/chart", "filteredLineSeries");
    private final static QName _FilteredPieSeries_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2012/chart", "filteredPieSeries");
    private final static QName _FilteredRadarSeries_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2012/chart", "filteredRadarSeries");
    private final static QName _FilteredScatterSeries_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2012/chart", "filteredScatterSeries");
    private final static QName _FilteredSurfaceSeries_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2012/chart", "filteredSurfaceSeries");
    private final static QName _DatalabelsRange_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2012/chart", "datalabelsRange");
    private final static QName _CategoryFilterExceptions_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2012/chart", "categoryFilterExceptions");
    private final static QName _DlblFieldTable_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2012/chart", "dlblFieldTable");
    private final static QName _XForSave_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2012/chart", "xForSave");
    private final static QName _ShowDataLabelsRange_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2012/chart", "showDataLabelsRange");
    private final static QName _Tx_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2012/chart", "tx");
    private final static QName _ShowLeaderLines_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2012/chart", "showLeaderLines");
    private final static QName _LeaderLines_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2012/chart", "leaderLines");
    private final static QName _AutoCat_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2012/chart", "autoCat");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.com.microsoft.schemas.office.drawing.x2012.chart
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTFullRef }
     * 
     */
    public CTFullRef createCTFullRef() {
        return new CTFullRef();
    }

    /**
     * Create an instance of {@link CTLevelRef }
     * 
     */
    public CTLevelRef createCTLevelRef() {
        return new CTLevelRef();
    }

    /**
     * Create an instance of {@link CTFormulaRef }
     * 
     */
    public CTFormulaRef createCTFormulaRef() {
        return new CTFormulaRef();
    }

    /**
     * Create an instance of {@link CTFilteredSeriesTitle }
     * 
     */
    public CTFilteredSeriesTitle createCTFilteredSeriesTitle() {
        return new CTFilteredSeriesTitle();
    }

    /**
     * Create an instance of {@link CTFilteredCategoryTitle }
     * 
     */
    public CTFilteredCategoryTitle createCTFilteredCategoryTitle() {
        return new CTFilteredCategoryTitle();
    }

    /**
     * Create an instance of {@link CTFilteredAreaSer }
     * 
     */
    public CTFilteredAreaSer createCTFilteredAreaSer() {
        return new CTFilteredAreaSer();
    }

    /**
     * Create an instance of {@link CTFilteredBarSer }
     * 
     */
    public CTFilteredBarSer createCTFilteredBarSer() {
        return new CTFilteredBarSer();
    }

    /**
     * Create an instance of {@link CTFilteredBubbleSer }
     * 
     */
    public CTFilteredBubbleSer createCTFilteredBubbleSer() {
        return new CTFilteredBubbleSer();
    }

    /**
     * Create an instance of {@link CTFilteredLineSer }
     * 
     */
    public CTFilteredLineSer createCTFilteredLineSer() {
        return new CTFilteredLineSer();
    }

    /**
     * Create an instance of {@link CTFilteredPieSer }
     * 
     */
    public CTFilteredPieSer createCTFilteredPieSer() {
        return new CTFilteredPieSer();
    }

    /**
     * Create an instance of {@link CTFilteredRadarSer }
     * 
     */
    public CTFilteredRadarSer createCTFilteredRadarSer() {
        return new CTFilteredRadarSer();
    }

    /**
     * Create an instance of {@link CTFilteredScatterSer }
     * 
     */
    public CTFilteredScatterSer createCTFilteredScatterSer() {
        return new CTFilteredScatterSer();
    }

    /**
     * Create an instance of {@link CTFilteredSurfaceSer }
     * 
     */
    public CTFilteredSurfaceSer createCTFilteredSurfaceSer() {
        return new CTFilteredSurfaceSer();
    }

    /**
     * Create an instance of {@link CTSeriesDataLabelsRange }
     * 
     */
    public CTSeriesDataLabelsRange createCTSeriesDataLabelsRange() {
        return new CTSeriesDataLabelsRange();
    }

    /**
     * Create an instance of {@link CTCategoryFilterExceptions }
     * 
     */
    public CTCategoryFilterExceptions createCTCategoryFilterExceptions() {
        return new CTCategoryFilterExceptions();
    }

    /**
     * Create an instance of {@link CTDataLabelFieldTable }
     * 
     */
    public CTDataLabelFieldTable createCTDataLabelFieldTable() {
        return new CTDataLabelFieldTable();
    }

    /**
     * Create an instance of {@link CTCategoryFilterException }
     * 
     */
    public CTCategoryFilterException createCTCategoryFilterException() {
        return new CTCategoryFilterException();
    }

    /**
     * Create an instance of {@link CTDataLabelFieldTableEntry }
     * 
     */
    public CTDataLabelFieldTableEntry createCTDataLabelFieldTableEntry() {
        return new CTDataLabelFieldTableEntry();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPivotSource }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2012/chart", name = "pivotSource")
    public JAXBElement<CTPivotSource> createPivotSource(CTPivotSource value) {
        return new JAXBElement<CTPivotSource>(_PivotSource_QNAME, CTPivotSource.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTNumFmt }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2012/chart", name = "numFmt")
    public JAXBElement<CTNumFmt> createNumFmt(CTNumFmt value) {
        return new JAXBElement<CTNumFmt>(_NumFmt_QNAME, CTNumFmt.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTShapeProperties }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2012/chart", name = "spPr")
    public JAXBElement<CTShapeProperties> createSpPr(CTShapeProperties value) {
        return new JAXBElement<CTShapeProperties>(_SpPr_QNAME, CTShapeProperties.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTLayout }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2012/chart", name = "layout")
    public JAXBElement<CTLayout> createLayout(CTLayout value) {
        return new JAXBElement<CTLayout>(_Layout_QNAME, CTLayout.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTFullRef }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2012/chart", name = "fullRef")
    public JAXBElement<CTFullRef> createFullRef(CTFullRef value) {
        return new JAXBElement<CTFullRef>(_FullRef_QNAME, CTFullRef.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTLevelRef }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2012/chart", name = "levelRef")
    public JAXBElement<CTLevelRef> createLevelRef(CTLevelRef value) {
        return new JAXBElement<CTLevelRef>(_LevelRef_QNAME, CTLevelRef.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTFormulaRef }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2012/chart", name = "formulaRef")
    public JAXBElement<CTFormulaRef> createFormulaRef(CTFormulaRef value) {
        return new JAXBElement<CTFormulaRef>(_FormulaRef_QNAME, CTFormulaRef.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTFilteredSeriesTitle }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2012/chart", name = "filteredSeriesTitle")
    public JAXBElement<CTFilteredSeriesTitle> createFilteredSeriesTitle(CTFilteredSeriesTitle value) {
        return new JAXBElement<CTFilteredSeriesTitle>(_FilteredSeriesTitle_QNAME, CTFilteredSeriesTitle.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTFilteredCategoryTitle }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2012/chart", name = "filteredCategoryTitle")
    public JAXBElement<CTFilteredCategoryTitle> createFilteredCategoryTitle(CTFilteredCategoryTitle value) {
        return new JAXBElement<CTFilteredCategoryTitle>(_FilteredCategoryTitle_QNAME, CTFilteredCategoryTitle.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTFilteredAreaSer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2012/chart", name = "filteredAreaSeries")
    public JAXBElement<CTFilteredAreaSer> createFilteredAreaSeries(CTFilteredAreaSer value) {
        return new JAXBElement<CTFilteredAreaSer>(_FilteredAreaSeries_QNAME, CTFilteredAreaSer.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTFilteredBarSer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2012/chart", name = "filteredBarSeries")
    public JAXBElement<CTFilteredBarSer> createFilteredBarSeries(CTFilteredBarSer value) {
        return new JAXBElement<CTFilteredBarSer>(_FilteredBarSeries_QNAME, CTFilteredBarSer.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTFilteredBubbleSer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2012/chart", name = "filteredBubbleSeries")
    public JAXBElement<CTFilteredBubbleSer> createFilteredBubbleSeries(CTFilteredBubbleSer value) {
        return new JAXBElement<CTFilteredBubbleSer>(_FilteredBubbleSeries_QNAME, CTFilteredBubbleSer.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTFilteredLineSer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2012/chart", name = "filteredLineSeries")
    public JAXBElement<CTFilteredLineSer> createFilteredLineSeries(CTFilteredLineSer value) {
        return new JAXBElement<CTFilteredLineSer>(_FilteredLineSeries_QNAME, CTFilteredLineSer.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTFilteredPieSer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2012/chart", name = "filteredPieSeries")
    public JAXBElement<CTFilteredPieSer> createFilteredPieSeries(CTFilteredPieSer value) {
        return new JAXBElement<CTFilteredPieSer>(_FilteredPieSeries_QNAME, CTFilteredPieSer.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTFilteredRadarSer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2012/chart", name = "filteredRadarSeries")
    public JAXBElement<CTFilteredRadarSer> createFilteredRadarSeries(CTFilteredRadarSer value) {
        return new JAXBElement<CTFilteredRadarSer>(_FilteredRadarSeries_QNAME, CTFilteredRadarSer.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTFilteredScatterSer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2012/chart", name = "filteredScatterSeries")
    public JAXBElement<CTFilteredScatterSer> createFilteredScatterSeries(CTFilteredScatterSer value) {
        return new JAXBElement<CTFilteredScatterSer>(_FilteredScatterSeries_QNAME, CTFilteredScatterSer.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTFilteredSurfaceSer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2012/chart", name = "filteredSurfaceSeries")
    public JAXBElement<CTFilteredSurfaceSer> createFilteredSurfaceSeries(CTFilteredSurfaceSer value) {
        return new JAXBElement<CTFilteredSurfaceSer>(_FilteredSurfaceSeries_QNAME, CTFilteredSurfaceSer.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTSeriesDataLabelsRange }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2012/chart", name = "datalabelsRange")
    public JAXBElement<CTSeriesDataLabelsRange> createDatalabelsRange(CTSeriesDataLabelsRange value) {
        return new JAXBElement<CTSeriesDataLabelsRange>(_DatalabelsRange_QNAME, CTSeriesDataLabelsRange.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTCategoryFilterExceptions }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2012/chart", name = "categoryFilterExceptions")
    public JAXBElement<CTCategoryFilterExceptions> createCategoryFilterExceptions(CTCategoryFilterExceptions value) {
        return new JAXBElement<CTCategoryFilterExceptions>(_CategoryFilterExceptions_QNAME, CTCategoryFilterExceptions.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTDataLabelFieldTable }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2012/chart", name = "dlblFieldTable")
    public JAXBElement<CTDataLabelFieldTable> createDlblFieldTable(CTDataLabelFieldTable value) {
        return new JAXBElement<CTDataLabelFieldTable>(_DlblFieldTable_QNAME, CTDataLabelFieldTable.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTBoolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2012/chart", name = "xForSave")
    public JAXBElement<CTBoolean> createXForSave(CTBoolean value) {
        return new JAXBElement<CTBoolean>(_XForSave_QNAME, CTBoolean.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTBoolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2012/chart", name = "showDataLabelsRange")
    public JAXBElement<CTBoolean> createShowDataLabelsRange(CTBoolean value) {
        return new JAXBElement<CTBoolean>(_ShowDataLabelsRange_QNAME, CTBoolean.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTTx }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2012/chart", name = "tx")
    public JAXBElement<CTTx> createTx(CTTx value) {
        return new JAXBElement<CTTx>(_Tx_QNAME, CTTx.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTBoolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2012/chart", name = "showLeaderLines")
    public JAXBElement<CTBoolean> createShowLeaderLines(CTBoolean value) {
        return new JAXBElement<CTBoolean>(_ShowLeaderLines_QNAME, CTBoolean.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTChartLines }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2012/chart", name = "leaderLines")
    public JAXBElement<CTChartLines> createLeaderLines(CTChartLines value) {
        return new JAXBElement<CTChartLines>(_LeaderLines_QNAME, CTChartLines.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTBoolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2012/chart", name = "autoCat")
    public JAXBElement<CTBoolean> createAutoCat(CTBoolean value) {
        return new JAXBElement<CTBoolean>(_AutoCat_QNAME, CTBoolean.class, null, value);
    }

}
