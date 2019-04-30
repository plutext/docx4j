/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License. 

    You may obtain a copy of the License at 

        http://www.apache.org/licenses/LICENSE-2.0 

    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS, 
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
    See the License for the specific language governing permissions and 
    limitations under the License.

 */


package org.docx4j.dml.chart;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import org.docx4j.dml.chartDrawing.CTDrawing;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.dml.chart package. 
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

    private final static QName _ChartSpace_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/chart", "chartSpace");
    public final static QName _UserShapes_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/chart", "userShapes");
    private final static QName _Chart_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/chart", "chart");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.dml.chart
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTChartSpace }
     * 
     */
    public CTChartSpace createCTChartSpace() {
        return new CTChartSpace();
    }

    /**
     * Create an instance of {@link CTRelId }
     * 
     */
    public CTRelId createCTRelId() {
        return new CTRelId();
    }

    /**
     * Create an instance of {@link CTBoolean }
     * 
     */
    public CTBoolean createCTBoolean() {
        return new CTBoolean();
    }

    /**
     * Create an instance of {@link CTDouble }
     * 
     */
    public CTDouble createCTDouble() {
        return new CTDouble();
    }

    /**
     * Create an instance of {@link CTUnsignedInt }
     * 
     */
    public CTUnsignedInt createCTUnsignedInt() {
        return new CTUnsignedInt();
    }

    /**
     * Create an instance of {@link CTExtension }
     * 
     */
    public CTExtension createCTExtension() {
        return new CTExtension();
    }

    /**
     * Create an instance of {@link CTExtensionList }
     * 
     */
    public CTExtensionList createCTExtensionList() {
        return new CTExtensionList();
    }

    /**
     * Create an instance of {@link CTNumVal }
     * 
     */
    public CTNumVal createCTNumVal() {
        return new CTNumVal();
    }

    /**
     * Create an instance of {@link CTNumData }
     * 
     */
    public CTNumData createCTNumData() {
        return new CTNumData();
    }

    /**
     * Create an instance of {@link CTNumRef }
     * 
     */
    public CTNumRef createCTNumRef() {
        return new CTNumRef();
    }

    /**
     * Create an instance of {@link CTNumDataSource }
     * 
     */
    public CTNumDataSource createCTNumDataSource() {
        return new CTNumDataSource();
    }

    /**
     * Create an instance of {@link CTStrVal }
     * 
     */
    public CTStrVal createCTStrVal() {
        return new CTStrVal();
    }

    /**
     * Create an instance of {@link CTStrData }
     * 
     */
    public CTStrData createCTStrData() {
        return new CTStrData();
    }

    /**
     * Create an instance of {@link CTStrRef }
     * 
     */
    public CTStrRef createCTStrRef() {
        return new CTStrRef();
    }

    /**
     * Create an instance of {@link CTTx }
     * 
     */
    public CTTx createCTTx() {
        return new CTTx();
    }

    /**
     * Create an instance of {@link CTTextLanguageID }
     * 
     */
    public CTTextLanguageID createCTTextLanguageID() {
        return new CTTextLanguageID();
    }

    /**
     * Create an instance of {@link CTLvl }
     * 
     */
    public CTLvl createCTLvl() {
        return new CTLvl();
    }

    /**
     * Create an instance of {@link CTMultiLvlStrData }
     * 
     */
    public CTMultiLvlStrData createCTMultiLvlStrData() {
        return new CTMultiLvlStrData();
    }

    /**
     * Create an instance of {@link CTMultiLvlStrRef }
     * 
     */
    public CTMultiLvlStrRef createCTMultiLvlStrRef() {
        return new CTMultiLvlStrRef();
    }

    /**
     * Create an instance of {@link CTAxDataSource }
     * 
     */
    public CTAxDataSource createCTAxDataSource() {
        return new CTAxDataSource();
    }

    /**
     * Create an instance of {@link CTSerTx }
     * 
     */
    public CTSerTx createCTSerTx() {
        return new CTSerTx();
    }

    /**
     * Create an instance of {@link CTLayoutTarget }
     * 
     */
    public CTLayoutTarget createCTLayoutTarget() {
        return new CTLayoutTarget();
    }

    /**
     * Create an instance of {@link CTLayoutMode }
     * 
     */
    public CTLayoutMode createCTLayoutMode() {
        return new CTLayoutMode();
    }

    /**
     * Create an instance of {@link CTManualLayout }
     * 
     */
    public CTManualLayout createCTManualLayout() {
        return new CTManualLayout();
    }

    /**
     * Create an instance of {@link CTLayout }
     * 
     */
    public CTLayout createCTLayout() {
        return new CTLayout();
    }

    /**
     * Create an instance of {@link CTTitle }
     * 
     */
    public CTTitle createCTTitle() {
        return new CTTitle();
    }

    /**
     * Create an instance of {@link CTRotX }
     * 
     */
    public CTRotX createCTRotX() {
        return new CTRotX();
    }

    /**
     * Create an instance of {@link CTHPercent }
     * 
     */
    public CTHPercent createCTHPercent() {
        return new CTHPercent();
    }

    /**
     * Create an instance of {@link CTRotY }
     * 
     */
    public CTRotY createCTRotY() {
        return new CTRotY();
    }

    /**
     * Create an instance of {@link CTDepthPercent }
     * 
     */
    public CTDepthPercent createCTDepthPercent() {
        return new CTDepthPercent();
    }

    /**
     * Create an instance of {@link CTPerspective }
     * 
     */
    public CTPerspective createCTPerspective() {
        return new CTPerspective();
    }

    /**
     * Create an instance of {@link CTView3D }
     * 
     */
    public CTView3D createCTView3D() {
        return new CTView3D();
    }

    /**
     * Create an instance of {@link CTSurface }
     * 
     */
    public CTSurface createCTSurface() {
        return new CTSurface();
    }

    /**
     * Create an instance of {@link CTDTable }
     * 
     */
    public CTDTable createCTDTable() {
        return new CTDTable();
    }

    /**
     * Create an instance of {@link CTGapAmount }
     * 
     */
    public CTGapAmount createCTGapAmount() {
        return new CTGapAmount();
    }

    /**
     * Create an instance of {@link CTOverlap }
     * 
     */
    public CTOverlap createCTOverlap() {
        return new CTOverlap();
    }

    /**
     * Create an instance of {@link CTBubbleScale }
     * 
     */
    public CTBubbleScale createCTBubbleScale() {
        return new CTBubbleScale();
    }

    /**
     * Create an instance of {@link CTSizeRepresents }
     * 
     */
    public CTSizeRepresents createCTSizeRepresents() {
        return new CTSizeRepresents();
    }

    /**
     * Create an instance of {@link CTFirstSliceAng }
     * 
     */
    public CTFirstSliceAng createCTFirstSliceAng() {
        return new CTFirstSliceAng();
    }

    /**
     * Create an instance of {@link CTHoleSize }
     * 
     */
    public CTHoleSize createCTHoleSize() {
        return new CTHoleSize();
    }

    /**
     * Create an instance of {@link CTSplitType }
     * 
     */
    public CTSplitType createCTSplitType() {
        return new CTSplitType();
    }

    /**
     * Create an instance of {@link CTCustSplit }
     * 
     */
    public CTCustSplit createCTCustSplit() {
        return new CTCustSplit();
    }

    /**
     * Create an instance of {@link CTSecondPieSize }
     * 
     */
    public CTSecondPieSize createCTSecondPieSize() {
        return new CTSecondPieSize();
    }

    /**
     * Create an instance of {@link CTNumFmt }
     * 
     */
    public CTNumFmt createCTNumFmt() {
        return new CTNumFmt();
    }

    /**
     * Create an instance of {@link CTLblAlgn }
     * 
     */
    public CTLblAlgn createCTLblAlgn() {
        return new CTLblAlgn();
    }

    /**
     * Create an instance of {@link CTDLblPos }
     * 
     */
    public CTDLblPos createCTDLblPos() {
        return new CTDLblPos();
    }

    /**
     * Create an instance of {@link CTDLbl }
     * 
     */
    public CTDLbl createCTDLbl() {
        return new CTDLbl();
    }

    /**
     * Create an instance of {@link CTDLbls }
     * 
     */
    public CTDLbls createCTDLbls() {
        return new CTDLbls();
    }

    /**
     * Create an instance of {@link CTMarkerStyle }
     * 
     */
    public CTMarkerStyle createCTMarkerStyle() {
        return new CTMarkerStyle();
    }

    /**
     * Create an instance of {@link CTMarkerSize }
     * 
     */
    public CTMarkerSize createCTMarkerSize() {
        return new CTMarkerSize();
    }

    /**
     * Create an instance of {@link CTMarker }
     * 
     */
    public CTMarker createCTMarker() {
        return new CTMarker();
    }

    /**
     * Create an instance of {@link CTDPt }
     * 
     */
    public CTDPt createCTDPt() {
        return new CTDPt();
    }

    /**
     * Create an instance of {@link CTTrendlineType }
     * 
     */
    public CTTrendlineType createCTTrendlineType() {
        return new CTTrendlineType();
    }

    /**
     * Create an instance of {@link CTOrder }
     * 
     */
    public CTOrder createCTOrder() {
        return new CTOrder();
    }

    /**
     * Create an instance of {@link CTPeriod }
     * 
     */
    public CTPeriod createCTPeriod() {
        return new CTPeriod();
    }

    /**
     * Create an instance of {@link CTTrendlineLbl }
     * 
     */
    public CTTrendlineLbl createCTTrendlineLbl() {
        return new CTTrendlineLbl();
    }

    /**
     * Create an instance of {@link CTTrendline }
     * 
     */
    public CTTrendline createCTTrendline() {
        return new CTTrendline();
    }

    /**
     * Create an instance of {@link CTErrDir }
     * 
     */
    public CTErrDir createCTErrDir() {
        return new CTErrDir();
    }

    /**
     * Create an instance of {@link CTErrBarType }
     * 
     */
    public CTErrBarType createCTErrBarType() {
        return new CTErrBarType();
    }

    /**
     * Create an instance of {@link CTErrValType }
     * 
     */
    public CTErrValType createCTErrValType() {
        return new CTErrValType();
    }

    /**
     * Create an instance of {@link CTErrBars }
     * 
     */
    public CTErrBars createCTErrBars() {
        return new CTErrBars();
    }

    /**
     * Create an instance of {@link CTUpDownBar }
     * 
     */
    public CTUpDownBar createCTUpDownBar() {
        return new CTUpDownBar();
    }

    /**
     * Create an instance of {@link CTUpDownBars }
     * 
     */
    public CTUpDownBars createCTUpDownBars() {
        return new CTUpDownBars();
    }

    /**
     * Create an instance of {@link CTLineSer }
     * 
     */
    public CTLineSer createCTLineSer() {
        return new CTLineSer();
    }

    /**
     * Create an instance of {@link CTScatterSer }
     * 
     */
    public CTScatterSer createCTScatterSer() {
        return new CTScatterSer();
    }

    /**
     * Create an instance of {@link CTRadarSer }
     * 
     */
    public CTRadarSer createCTRadarSer() {
        return new CTRadarSer();
    }

    /**
     * Create an instance of {@link CTBarSer }
     * 
     */
    public CTBarSer createCTBarSer() {
        return new CTBarSer();
    }

    /**
     * Create an instance of {@link CTAreaSer }
     * 
     */
    public CTAreaSer createCTAreaSer() {
        return new CTAreaSer();
    }

    /**
     * Create an instance of {@link CTPieSer }
     * 
     */
    public CTPieSer createCTPieSer() {
        return new CTPieSer();
    }

    /**
     * Create an instance of {@link CTBubbleSer }
     * 
     */
    public CTBubbleSer createCTBubbleSer() {
        return new CTBubbleSer();
    }

    /**
     * Create an instance of {@link CTSurfaceSer }
     * 
     */
    public CTSurfaceSer createCTSurfaceSer() {
        return new CTSurfaceSer();
    }

    /**
     * Create an instance of {@link CTGrouping }
     * 
     */
    public CTGrouping createCTGrouping() {
        return new CTGrouping();
    }

    /**
     * Create an instance of {@link CTChartLines }
     * 
     */
    public CTChartLines createCTChartLines() {
        return new CTChartLines();
    }

    /**
     * Create an instance of {@link CTLineChart }
     * 
     */
    public CTLineChart createCTLineChart() {
        return new CTLineChart();
    }

    /**
     * Create an instance of {@link CTLine3DChart }
     * 
     */
    public CTLine3DChart createCTLine3DChart() {
        return new CTLine3DChart();
    }

    /**
     * Create an instance of {@link CTStockChart }
     * 
     */
    public CTStockChart createCTStockChart() {
        return new CTStockChart();
    }

    /**
     * Create an instance of {@link CTScatterStyle }
     * 
     */
    public CTScatterStyle createCTScatterStyle() {
        return new CTScatterStyle();
    }

    /**
     * Create an instance of {@link CTScatterChart }
     * 
     */
    public CTScatterChart createCTScatterChart() {
        return new CTScatterChart();
    }

    /**
     * Create an instance of {@link CTRadarStyle }
     * 
     */
    public CTRadarStyle createCTRadarStyle() {
        return new CTRadarStyle();
    }

    /**
     * Create an instance of {@link CTRadarChart }
     * 
     */
    public CTRadarChart createCTRadarChart() {
        return new CTRadarChart();
    }

    /**
     * Create an instance of {@link CTBarGrouping }
     * 
     */
    public CTBarGrouping createCTBarGrouping() {
        return new CTBarGrouping();
    }

    /**
     * Create an instance of {@link CTBarDir }
     * 
     */
    public CTBarDir createCTBarDir() {
        return new CTBarDir();
    }

    /**
     * Create an instance of {@link CTShape }
     * 
     */
    public CTShape createCTShape() {
        return new CTShape();
    }

    /**
     * Create an instance of {@link CTBarChart }
     * 
     */
    public CTBarChart createCTBarChart() {
        return new CTBarChart();
    }

    /**
     * Create an instance of {@link CTBar3DChart }
     * 
     */
    public CTBar3DChart createCTBar3DChart() {
        return new CTBar3DChart();
    }

    /**
     * Create an instance of {@link CTAreaChart }
     * 
     */
    public CTAreaChart createCTAreaChart() {
        return new CTAreaChart();
    }

    /**
     * Create an instance of {@link CTArea3DChart }
     * 
     */
    public CTArea3DChart createCTArea3DChart() {
        return new CTArea3DChart();
    }

    /**
     * Create an instance of {@link CTPieChart }
     * 
     */
    public CTPieChart createCTPieChart() {
        return new CTPieChart();
    }

    /**
     * Create an instance of {@link CTPie3DChart }
     * 
     */
    public CTPie3DChart createCTPie3DChart() {
        return new CTPie3DChart();
    }

    /**
     * Create an instance of {@link CTDoughnutChart }
     * 
     */
    public CTDoughnutChart createCTDoughnutChart() {
        return new CTDoughnutChart();
    }

    /**
     * Create an instance of {@link CTOfPieType }
     * 
     */
    public CTOfPieType createCTOfPieType() {
        return new CTOfPieType();
    }

    /**
     * Create an instance of {@link CTOfPieChart }
     * 
     */
    public CTOfPieChart createCTOfPieChart() {
        return new CTOfPieChart();
    }

    /**
     * Create an instance of {@link CTBubbleChart }
     * 
     */
    public CTBubbleChart createCTBubbleChart() {
        return new CTBubbleChart();
    }

    /**
     * Create an instance of {@link CTBandFmt }
     * 
     */
    public CTBandFmt createCTBandFmt() {
        return new CTBandFmt();
    }

    /**
     * Create an instance of {@link CTBandFmts }
     * 
     */
    public CTBandFmts createCTBandFmts() {
        return new CTBandFmts();
    }

    /**
     * Create an instance of {@link CTSurfaceChart }
     * 
     */
    public CTSurfaceChart createCTSurfaceChart() {
        return new CTSurfaceChart();
    }

    /**
     * Create an instance of {@link CTSurface3DChart }
     * 
     */
    public CTSurface3DChart createCTSurface3DChart() {
        return new CTSurface3DChart();
    }

    /**
     * Create an instance of {@link CTAxPos }
     * 
     */
    public CTAxPos createCTAxPos() {
        return new CTAxPos();
    }

    /**
     * Create an instance of {@link CTCrosses }
     * 
     */
    public CTCrosses createCTCrosses() {
        return new CTCrosses();
    }

    /**
     * Create an instance of {@link CTCrossBetween }
     * 
     */
    public CTCrossBetween createCTCrossBetween() {
        return new CTCrossBetween();
    }

    /**
     * Create an instance of {@link CTTickMark }
     * 
     */
    public CTTickMark createCTTickMark() {
        return new CTTickMark();
    }

    /**
     * Create an instance of {@link CTTickLblPos }
     * 
     */
    public CTTickLblPos createCTTickLblPos() {
        return new CTTickLblPos();
    }

    /**
     * Create an instance of {@link CTSkip }
     * 
     */
    public CTSkip createCTSkip() {
        return new CTSkip();
    }

    /**
     * Create an instance of {@link CTTimeUnit }
     * 
     */
    public CTTimeUnit createCTTimeUnit() {
        return new CTTimeUnit();
    }

    /**
     * Create an instance of {@link CTAxisUnit }
     * 
     */
    public CTAxisUnit createCTAxisUnit() {
        return new CTAxisUnit();
    }

    /**
     * Create an instance of {@link CTBuiltInUnit }
     * 
     */
    public CTBuiltInUnit createCTBuiltInUnit() {
        return new CTBuiltInUnit();
    }

    /**
     * Create an instance of {@link CTPictureFormat }
     * 
     */
    public CTPictureFormat createCTPictureFormat() {
        return new CTPictureFormat();
    }

    /**
     * Create an instance of {@link CTPictureStackUnit }
     * 
     */
    public CTPictureStackUnit createCTPictureStackUnit() {
        return new CTPictureStackUnit();
    }

    /**
     * Create an instance of {@link CTPictureOptions }
     * 
     */
    public CTPictureOptions createCTPictureOptions() {
        return new CTPictureOptions();
    }

    /**
     * Create an instance of {@link CTDispUnitsLbl }
     * 
     */
    public CTDispUnitsLbl createCTDispUnitsLbl() {
        return new CTDispUnitsLbl();
    }

    /**
     * Create an instance of {@link CTDispUnits }
     * 
     */
    public CTDispUnits createCTDispUnits() {
        return new CTDispUnits();
    }

    /**
     * Create an instance of {@link CTOrientation }
     * 
     */
    public CTOrientation createCTOrientation() {
        return new CTOrientation();
    }

    /**
     * Create an instance of {@link CTLogBase }
     * 
     */
    public CTLogBase createCTLogBase() {
        return new CTLogBase();
    }

    /**
     * Create an instance of {@link CTScaling }
     * 
     */
    public CTScaling createCTScaling() {
        return new CTScaling();
    }

    /**
     * Create an instance of {@link CTLblOffset }
     * 
     */
    public CTLblOffset createCTLblOffset() {
        return new CTLblOffset();
    }

    /**
     * Create an instance of {@link CTCatAx }
     * 
     */
    public CTCatAx createCTCatAx() {
        return new CTCatAx();
    }

    /**
     * Create an instance of {@link CTDateAx }
     * 
     */
    public CTDateAx createCTDateAx() {
        return new CTDateAx();
    }

    /**
     * Create an instance of {@link CTSerAx }
     * 
     */
    public CTSerAx createCTSerAx() {
        return new CTSerAx();
    }

    /**
     * Create an instance of {@link CTValAx }
     * 
     */
    public CTValAx createCTValAx() {
        return new CTValAx();
    }

    /**
     * Create an instance of {@link CTPlotArea }
     * 
     */
    public CTPlotArea createCTPlotArea() {
        return new CTPlotArea();
    }

    /**
     * Create an instance of {@link CTPivotFmt }
     * 
     */
    public CTPivotFmt createCTPivotFmt() {
        return new CTPivotFmt();
    }

    /**
     * Create an instance of {@link CTPivotFmts }
     * 
     */
    public CTPivotFmts createCTPivotFmts() {
        return new CTPivotFmts();
    }

    /**
     * Create an instance of {@link CTLegendPos }
     * 
     */
    public CTLegendPos createCTLegendPos() {
        return new CTLegendPos();
    }

    /**
     * Create an instance of {@link CTLegendEntry }
     * 
     */
    public CTLegendEntry createCTLegendEntry() {
        return new CTLegendEntry();
    }

    /**
     * Create an instance of {@link CTLegend }
     * 
     */
    public CTLegend createCTLegend() {
        return new CTLegend();
    }

    /**
     * Create an instance of {@link CTDispBlanksAs }
     * 
     */
    public CTDispBlanksAs createCTDispBlanksAs() {
        return new CTDispBlanksAs();
    }

    /**
     * Create an instance of {@link CTChart }
     * 
     */
    public CTChart createCTChart() {
        return new CTChart();
    }

    /**
     * Create an instance of {@link CTStyle }
     * 
     */
    public CTStyle createCTStyle() {
        return new CTStyle();
    }

    /**
     * Create an instance of {@link CTPivotSource }
     * 
     */
    public CTPivotSource createCTPivotSource() {
        return new CTPivotSource();
    }

    /**
     * Create an instance of {@link CTProtection }
     * 
     */
    public CTProtection createCTProtection() {
        return new CTProtection();
    }

    /**
     * Create an instance of {@link CTHeaderFooter }
     * 
     */
    public CTHeaderFooter createCTHeaderFooter() {
        return new CTHeaderFooter();
    }

    /**
     * Create an instance of {@link CTPageMargins }
     * 
     */
    public CTPageMargins createCTPageMargins() {
        return new CTPageMargins();
    }

    /**
     * Create an instance of {@link CTExternalData }
     * 
     */
    public CTExternalData createCTExternalData() {
        return new CTExternalData();
    }

    /**
     * Create an instance of {@link CTPageSetup }
     * 
     */
    public CTPageSetup createCTPageSetup() {
        return new CTPageSetup();
    }

    /**
     * Create an instance of {@link CTPrintSettings }
     * 
     */
    public CTPrintSettings createCTPrintSettings() {
        return new CTPrintSettings();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTChartSpace }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/chart", name = "chartSpace")
    public JAXBElement<CTChartSpace> createChartSpace(CTChartSpace value) {
        return new JAXBElement<CTChartSpace>(_ChartSpace_QNAME, CTChartSpace.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTDrawing }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/chart", name = "userShapes")
    public JAXBElement<CTDrawing> createUserShapes(CTDrawing value) {
        return new JAXBElement<CTDrawing>(_UserShapes_QNAME, CTDrawing.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTRelId }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/chart", name = "chart")
    public JAXBElement<CTRelId> createChart(CTRelId value) {
        return new JAXBElement<CTRelId>(_Chart_QNAME, CTRelId.class, null, value);
    }

}
