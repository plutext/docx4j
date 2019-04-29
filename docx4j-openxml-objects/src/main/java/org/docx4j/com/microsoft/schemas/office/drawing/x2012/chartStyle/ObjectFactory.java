
package org.docx4j.com.microsoft.schemas.office.drawing.x2012.chartStyle;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import org.docx4j.dml.CTAngle;
import org.docx4j.dml.CTComplementTransform;
import org.docx4j.dml.CTFixedPercentage;
import org.docx4j.dml.CTGammaTransform;
import org.docx4j.dml.CTGrayscaleTransform;
import org.docx4j.dml.CTInverseGammaTransform;
import org.docx4j.dml.CTInverseTransform;
import org.docx4j.dml.CTPercentage;
import org.docx4j.dml.CTPositiveFixedAngle;
import org.docx4j.dml.CTPositiveFixedPercentage;
import org.docx4j.dml.CTPositivePercentage;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.com.microsoft.schemas.office.drawing.x2012.chartStyle package. 
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

    private final static QName _ColorStyle_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2012/chartStyle", "colorStyle");
    private final static QName _ChartStyle_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2012/chartStyle", "chartStyle");
    private final static QName _CTStyleColorTint_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "tint");
    private final static QName _CTStyleColorShade_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "shade");
    private final static QName _CTStyleColorComp_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "comp");
    private final static QName _CTStyleColorInv_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "inv");
    private final static QName _CTStyleColorGray_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "gray");
    private final static QName _CTStyleColorAlpha_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "alpha");
    private final static QName _CTStyleColorAlphaOff_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "alphaOff");
    private final static QName _CTStyleColorAlphaMod_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "alphaMod");
    private final static QName _CTStyleColorHue_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "hue");
    private final static QName _CTStyleColorHueOff_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "hueOff");
    private final static QName _CTStyleColorHueMod_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "hueMod");
    private final static QName _CTStyleColorSat_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "sat");
    private final static QName _CTStyleColorSatOff_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "satOff");
    private final static QName _CTStyleColorSatMod_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "satMod");
    private final static QName _CTStyleColorLum_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "lum");
    private final static QName _CTStyleColorLumOff_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "lumOff");
    private final static QName _CTStyleColorLumMod_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "lumMod");
    private final static QName _CTStyleColorRed_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "red");
    private final static QName _CTStyleColorRedOff_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "redOff");
    private final static QName _CTStyleColorRedMod_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "redMod");
    private final static QName _CTStyleColorGreen_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "green");
    private final static QName _CTStyleColorGreenOff_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "greenOff");
    private final static QName _CTStyleColorGreenMod_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "greenMod");
    private final static QName _CTStyleColorBlue_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "blue");
    private final static QName _CTStyleColorBlueOff_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "blueOff");
    private final static QName _CTStyleColorBlueMod_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "blueMod");
    private final static QName _CTStyleColorGamma_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "gamma");
    private final static QName _CTStyleColorInvGamma_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "invGamma");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.com.microsoft.schemas.office.drawing.x2012.chartStyle
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTColorStyle }
     * 
     */
    public CTColorStyle createCTColorStyle() {
        return new CTColorStyle();
    }

    /**
     * Create an instance of {@link CTChartStyle }
     * 
     */
    public CTChartStyle createCTChartStyle() {
        return new CTChartStyle();
    }

    /**
     * Create an instance of {@link CTColorStyleVariation }
     * 
     */
    public CTColorStyleVariation createCTColorStyleVariation() {
        return new CTColorStyleVariation();
    }

    /**
     * Create an instance of {@link CTStyleColor }
     * 
     */
    public CTStyleColor createCTStyleColor() {
        return new CTStyleColor();
    }

    /**
     * Create an instance of {@link CTStyleReference }
     * 
     */
    public CTStyleReference createCTStyleReference() {
        return new CTStyleReference();
    }

    /**
     * Create an instance of {@link CTFontReference }
     * 
     */
    public CTFontReference createCTFontReference() {
        return new CTFontReference();
    }

    /**
     * Create an instance of {@link CTMarkerLayout }
     * 
     */
    public CTMarkerLayout createCTMarkerLayout() {
        return new CTMarkerLayout();
    }

    /**
     * Create an instance of {@link CTStyleEntry }
     * 
     */
    public CTStyleEntry createCTStyleEntry() {
        return new CTStyleEntry();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTColorStyle }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2012/chartStyle", name = "colorStyle")
    public JAXBElement<CTColorStyle> createColorStyle(CTColorStyle value) {
        return new JAXBElement<CTColorStyle>(_ColorStyle_QNAME, CTColorStyle.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTChartStyle }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2012/chartStyle", name = "chartStyle")
    public JAXBElement<CTChartStyle> createChartStyle(CTChartStyle value) {
        return new JAXBElement<CTChartStyle>(_ChartStyle_QNAME, CTChartStyle.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositiveFixedPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "tint", scope = CTStyleColor.class)
    public JAXBElement<CTPositiveFixedPercentage> createCTStyleColorTint(CTPositiveFixedPercentage value) {
        return new JAXBElement<CTPositiveFixedPercentage>(_CTStyleColorTint_QNAME, CTPositiveFixedPercentage.class, CTStyleColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositiveFixedPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "shade", scope = CTStyleColor.class)
    public JAXBElement<CTPositiveFixedPercentage> createCTStyleColorShade(CTPositiveFixedPercentage value) {
        return new JAXBElement<CTPositiveFixedPercentage>(_CTStyleColorShade_QNAME, CTPositiveFixedPercentage.class, CTStyleColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTComplementTransform }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "comp", scope = CTStyleColor.class)
    public JAXBElement<CTComplementTransform> createCTStyleColorComp(CTComplementTransform value) {
        return new JAXBElement<CTComplementTransform>(_CTStyleColorComp_QNAME, CTComplementTransform.class, CTStyleColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTInverseTransform }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "inv", scope = CTStyleColor.class)
    public JAXBElement<CTInverseTransform> createCTStyleColorInv(CTInverseTransform value) {
        return new JAXBElement<CTInverseTransform>(_CTStyleColorInv_QNAME, CTInverseTransform.class, CTStyleColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTGrayscaleTransform }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "gray", scope = CTStyleColor.class)
    public JAXBElement<CTGrayscaleTransform> createCTStyleColorGray(CTGrayscaleTransform value) {
        return new JAXBElement<CTGrayscaleTransform>(_CTStyleColorGray_QNAME, CTGrayscaleTransform.class, CTStyleColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositiveFixedPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "alpha", scope = CTStyleColor.class)
    public JAXBElement<CTPositiveFixedPercentage> createCTStyleColorAlpha(CTPositiveFixedPercentage value) {
        return new JAXBElement<CTPositiveFixedPercentage>(_CTStyleColorAlpha_QNAME, CTPositiveFixedPercentage.class, CTStyleColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTFixedPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "alphaOff", scope = CTStyleColor.class)
    public JAXBElement<CTFixedPercentage> createCTStyleColorAlphaOff(CTFixedPercentage value) {
        return new JAXBElement<CTFixedPercentage>(_CTStyleColorAlphaOff_QNAME, CTFixedPercentage.class, CTStyleColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositivePercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "alphaMod", scope = CTStyleColor.class)
    public JAXBElement<CTPositivePercentage> createCTStyleColorAlphaMod(CTPositivePercentage value) {
        return new JAXBElement<CTPositivePercentage>(_CTStyleColorAlphaMod_QNAME, CTPositivePercentage.class, CTStyleColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositiveFixedAngle }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "hue", scope = CTStyleColor.class)
    public JAXBElement<CTPositiveFixedAngle> createCTStyleColorHue(CTPositiveFixedAngle value) {
        return new JAXBElement<CTPositiveFixedAngle>(_CTStyleColorHue_QNAME, CTPositiveFixedAngle.class, CTStyleColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTAngle }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "hueOff", scope = CTStyleColor.class)
    public JAXBElement<CTAngle> createCTStyleColorHueOff(CTAngle value) {
        return new JAXBElement<CTAngle>(_CTStyleColorHueOff_QNAME, CTAngle.class, CTStyleColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositivePercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "hueMod", scope = CTStyleColor.class)
    public JAXBElement<CTPositivePercentage> createCTStyleColorHueMod(CTPositivePercentage value) {
        return new JAXBElement<CTPositivePercentage>(_CTStyleColorHueMod_QNAME, CTPositivePercentage.class, CTStyleColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "sat", scope = CTStyleColor.class)
    public JAXBElement<CTPercentage> createCTStyleColorSat(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTStyleColorSat_QNAME, CTPercentage.class, CTStyleColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "satOff", scope = CTStyleColor.class)
    public JAXBElement<CTPercentage> createCTStyleColorSatOff(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTStyleColorSatOff_QNAME, CTPercentage.class, CTStyleColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "satMod", scope = CTStyleColor.class)
    public JAXBElement<CTPercentage> createCTStyleColorSatMod(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTStyleColorSatMod_QNAME, CTPercentage.class, CTStyleColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "lum", scope = CTStyleColor.class)
    public JAXBElement<CTPercentage> createCTStyleColorLum(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTStyleColorLum_QNAME, CTPercentage.class, CTStyleColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "lumOff", scope = CTStyleColor.class)
    public JAXBElement<CTPercentage> createCTStyleColorLumOff(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTStyleColorLumOff_QNAME, CTPercentage.class, CTStyleColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "lumMod", scope = CTStyleColor.class)
    public JAXBElement<CTPercentage> createCTStyleColorLumMod(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTStyleColorLumMod_QNAME, CTPercentage.class, CTStyleColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "red", scope = CTStyleColor.class)
    public JAXBElement<CTPercentage> createCTStyleColorRed(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTStyleColorRed_QNAME, CTPercentage.class, CTStyleColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "redOff", scope = CTStyleColor.class)
    public JAXBElement<CTPercentage> createCTStyleColorRedOff(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTStyleColorRedOff_QNAME, CTPercentage.class, CTStyleColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "redMod", scope = CTStyleColor.class)
    public JAXBElement<CTPercentage> createCTStyleColorRedMod(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTStyleColorRedMod_QNAME, CTPercentage.class, CTStyleColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "green", scope = CTStyleColor.class)
    public JAXBElement<CTPercentage> createCTStyleColorGreen(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTStyleColorGreen_QNAME, CTPercentage.class, CTStyleColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "greenOff", scope = CTStyleColor.class)
    public JAXBElement<CTPercentage> createCTStyleColorGreenOff(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTStyleColorGreenOff_QNAME, CTPercentage.class, CTStyleColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "greenMod", scope = CTStyleColor.class)
    public JAXBElement<CTPercentage> createCTStyleColorGreenMod(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTStyleColorGreenMod_QNAME, CTPercentage.class, CTStyleColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "blue", scope = CTStyleColor.class)
    public JAXBElement<CTPercentage> createCTStyleColorBlue(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTStyleColorBlue_QNAME, CTPercentage.class, CTStyleColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "blueOff", scope = CTStyleColor.class)
    public JAXBElement<CTPercentage> createCTStyleColorBlueOff(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTStyleColorBlueOff_QNAME, CTPercentage.class, CTStyleColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "blueMod", scope = CTStyleColor.class)
    public JAXBElement<CTPercentage> createCTStyleColorBlueMod(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTStyleColorBlueMod_QNAME, CTPercentage.class, CTStyleColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTGammaTransform }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "gamma", scope = CTStyleColor.class)
    public JAXBElement<CTGammaTransform> createCTStyleColorGamma(CTGammaTransform value) {
        return new JAXBElement<CTGammaTransform>(_CTStyleColorGamma_QNAME, CTGammaTransform.class, CTStyleColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTInverseGammaTransform }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "invGamma", scope = CTStyleColor.class)
    public JAXBElement<CTInverseGammaTransform> createCTStyleColorInvGamma(CTInverseGammaTransform value) {
        return new JAXBElement<CTInverseGammaTransform>(_CTStyleColorInvGamma_QNAME, CTInverseGammaTransform.class, CTStyleColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositiveFixedPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "tint", scope = CTColorStyleVariation.class)
    public JAXBElement<CTPositiveFixedPercentage> createCTColorStyleVariationTint(CTPositiveFixedPercentage value) {
        return new JAXBElement<CTPositiveFixedPercentage>(_CTStyleColorTint_QNAME, CTPositiveFixedPercentage.class, CTColorStyleVariation.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositiveFixedPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "shade", scope = CTColorStyleVariation.class)
    public JAXBElement<CTPositiveFixedPercentage> createCTColorStyleVariationShade(CTPositiveFixedPercentage value) {
        return new JAXBElement<CTPositiveFixedPercentage>(_CTStyleColorShade_QNAME, CTPositiveFixedPercentage.class, CTColorStyleVariation.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTComplementTransform }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "comp", scope = CTColorStyleVariation.class)
    public JAXBElement<CTComplementTransform> createCTColorStyleVariationComp(CTComplementTransform value) {
        return new JAXBElement<CTComplementTransform>(_CTStyleColorComp_QNAME, CTComplementTransform.class, CTColorStyleVariation.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTInverseTransform }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "inv", scope = CTColorStyleVariation.class)
    public JAXBElement<CTInverseTransform> createCTColorStyleVariationInv(CTInverseTransform value) {
        return new JAXBElement<CTInverseTransform>(_CTStyleColorInv_QNAME, CTInverseTransform.class, CTColorStyleVariation.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTGrayscaleTransform }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "gray", scope = CTColorStyleVariation.class)
    public JAXBElement<CTGrayscaleTransform> createCTColorStyleVariationGray(CTGrayscaleTransform value) {
        return new JAXBElement<CTGrayscaleTransform>(_CTStyleColorGray_QNAME, CTGrayscaleTransform.class, CTColorStyleVariation.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositiveFixedPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "alpha", scope = CTColorStyleVariation.class)
    public JAXBElement<CTPositiveFixedPercentage> createCTColorStyleVariationAlpha(CTPositiveFixedPercentage value) {
        return new JAXBElement<CTPositiveFixedPercentage>(_CTStyleColorAlpha_QNAME, CTPositiveFixedPercentage.class, CTColorStyleVariation.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTFixedPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "alphaOff", scope = CTColorStyleVariation.class)
    public JAXBElement<CTFixedPercentage> createCTColorStyleVariationAlphaOff(CTFixedPercentage value) {
        return new JAXBElement<CTFixedPercentage>(_CTStyleColorAlphaOff_QNAME, CTFixedPercentage.class, CTColorStyleVariation.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositivePercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "alphaMod", scope = CTColorStyleVariation.class)
    public JAXBElement<CTPositivePercentage> createCTColorStyleVariationAlphaMod(CTPositivePercentage value) {
        return new JAXBElement<CTPositivePercentage>(_CTStyleColorAlphaMod_QNAME, CTPositivePercentage.class, CTColorStyleVariation.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositiveFixedAngle }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "hue", scope = CTColorStyleVariation.class)
    public JAXBElement<CTPositiveFixedAngle> createCTColorStyleVariationHue(CTPositiveFixedAngle value) {
        return new JAXBElement<CTPositiveFixedAngle>(_CTStyleColorHue_QNAME, CTPositiveFixedAngle.class, CTColorStyleVariation.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTAngle }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "hueOff", scope = CTColorStyleVariation.class)
    public JAXBElement<CTAngle> createCTColorStyleVariationHueOff(CTAngle value) {
        return new JAXBElement<CTAngle>(_CTStyleColorHueOff_QNAME, CTAngle.class, CTColorStyleVariation.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositivePercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "hueMod", scope = CTColorStyleVariation.class)
    public JAXBElement<CTPositivePercentage> createCTColorStyleVariationHueMod(CTPositivePercentage value) {
        return new JAXBElement<CTPositivePercentage>(_CTStyleColorHueMod_QNAME, CTPositivePercentage.class, CTColorStyleVariation.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "sat", scope = CTColorStyleVariation.class)
    public JAXBElement<CTPercentage> createCTColorStyleVariationSat(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTStyleColorSat_QNAME, CTPercentage.class, CTColorStyleVariation.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "satOff", scope = CTColorStyleVariation.class)
    public JAXBElement<CTPercentage> createCTColorStyleVariationSatOff(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTStyleColorSatOff_QNAME, CTPercentage.class, CTColorStyleVariation.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "satMod", scope = CTColorStyleVariation.class)
    public JAXBElement<CTPercentage> createCTColorStyleVariationSatMod(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTStyleColorSatMod_QNAME, CTPercentage.class, CTColorStyleVariation.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "lum", scope = CTColorStyleVariation.class)
    public JAXBElement<CTPercentage> createCTColorStyleVariationLum(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTStyleColorLum_QNAME, CTPercentage.class, CTColorStyleVariation.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "lumOff", scope = CTColorStyleVariation.class)
    public JAXBElement<CTPercentage> createCTColorStyleVariationLumOff(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTStyleColorLumOff_QNAME, CTPercentage.class, CTColorStyleVariation.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "lumMod", scope = CTColorStyleVariation.class)
    public JAXBElement<CTPercentage> createCTColorStyleVariationLumMod(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTStyleColorLumMod_QNAME, CTPercentage.class, CTColorStyleVariation.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "red", scope = CTColorStyleVariation.class)
    public JAXBElement<CTPercentage> createCTColorStyleVariationRed(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTStyleColorRed_QNAME, CTPercentage.class, CTColorStyleVariation.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "redOff", scope = CTColorStyleVariation.class)
    public JAXBElement<CTPercentage> createCTColorStyleVariationRedOff(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTStyleColorRedOff_QNAME, CTPercentage.class, CTColorStyleVariation.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "redMod", scope = CTColorStyleVariation.class)
    public JAXBElement<CTPercentage> createCTColorStyleVariationRedMod(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTStyleColorRedMod_QNAME, CTPercentage.class, CTColorStyleVariation.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "green", scope = CTColorStyleVariation.class)
    public JAXBElement<CTPercentage> createCTColorStyleVariationGreen(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTStyleColorGreen_QNAME, CTPercentage.class, CTColorStyleVariation.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "greenOff", scope = CTColorStyleVariation.class)
    public JAXBElement<CTPercentage> createCTColorStyleVariationGreenOff(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTStyleColorGreenOff_QNAME, CTPercentage.class, CTColorStyleVariation.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "greenMod", scope = CTColorStyleVariation.class)
    public JAXBElement<CTPercentage> createCTColorStyleVariationGreenMod(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTStyleColorGreenMod_QNAME, CTPercentage.class, CTColorStyleVariation.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "blue", scope = CTColorStyleVariation.class)
    public JAXBElement<CTPercentage> createCTColorStyleVariationBlue(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTStyleColorBlue_QNAME, CTPercentage.class, CTColorStyleVariation.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "blueOff", scope = CTColorStyleVariation.class)
    public JAXBElement<CTPercentage> createCTColorStyleVariationBlueOff(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTStyleColorBlueOff_QNAME, CTPercentage.class, CTColorStyleVariation.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "blueMod", scope = CTColorStyleVariation.class)
    public JAXBElement<CTPercentage> createCTColorStyleVariationBlueMod(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTStyleColorBlueMod_QNAME, CTPercentage.class, CTColorStyleVariation.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTGammaTransform }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "gamma", scope = CTColorStyleVariation.class)
    public JAXBElement<CTGammaTransform> createCTColorStyleVariationGamma(CTGammaTransform value) {
        return new JAXBElement<CTGammaTransform>(_CTStyleColorGamma_QNAME, CTGammaTransform.class, CTColorStyleVariation.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTInverseGammaTransform }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", name = "invGamma", scope = CTColorStyleVariation.class)
    public JAXBElement<CTInverseGammaTransform> createCTColorStyleVariationInvGamma(CTInverseGammaTransform value) {
        return new JAXBElement<CTInverseGammaTransform>(_CTStyleColorInvGamma_QNAME, CTInverseGammaTransform.class, CTColorStyleVariation.class, value);
    }

}
