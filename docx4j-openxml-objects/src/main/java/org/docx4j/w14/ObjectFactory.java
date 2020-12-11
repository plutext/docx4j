
package org.docx4j.w14;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import org.docx4j.wml.CTEmpty;
import org.docx4j.wml.CTMarkup;
import org.docx4j.wml.CTTrackChange;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.w14 package. 
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

    private final static QName _ContentPart_QNAME = new QName("http://schemas.microsoft.com/office/word/2010/wordml", "contentPart");
    private final static QName _DocId_QNAME = new QName("http://schemas.microsoft.com/office/word/2010/wordml", "docId");
    private final static QName _ConflictMode_QNAME = new QName("http://schemas.microsoft.com/office/word/2010/wordml", "conflictMode");
    private final static QName _CustomXmlConflictInsRangeStart_QNAME = new QName("http://schemas.microsoft.com/office/word/2010/wordml", "customXmlConflictInsRangeStart");
    private final static QName _CustomXmlConflictInsRangeEnd_QNAME = new QName("http://schemas.microsoft.com/office/word/2010/wordml", "customXmlConflictInsRangeEnd");
    private final static QName _CustomXmlConflictDelRangeStart_QNAME = new QName("http://schemas.microsoft.com/office/word/2010/wordml", "customXmlConflictDelRangeStart");
    private final static QName _CustomXmlConflictDelRangeEnd_QNAME = new QName("http://schemas.microsoft.com/office/word/2010/wordml", "customXmlConflictDelRangeEnd");
    private final static QName _Shadow_QNAME = new QName("http://schemas.microsoft.com/office/word/2010/wordml", "shadow");
    private final static QName _DiscardImageEditingData_QNAME = new QName("http://schemas.microsoft.com/office/word/2010/wordml", "discardImageEditingData");
    private final static QName _DefaultImageDpi_QNAME = new QName("http://schemas.microsoft.com/office/word/2010/wordml", "defaultImageDpi");
    private final static QName _EntityPicker_QNAME = new QName("http://schemas.microsoft.com/office/word/2010/wordml", "entityPicker");
    private final static QName _Checkbox_QNAME = new QName("http://schemas.microsoft.com/office/word/2010/wordml", "checkbox");
    private final static QName _CTSchemeColorTint_QNAME = new QName("http://schemas.microsoft.com/office/word/2010/wordml", "tint");
    private final static QName _CTSchemeColorShade_QNAME = new QName("http://schemas.microsoft.com/office/word/2010/wordml", "shade");
    private final static QName _CTSchemeColorAlpha_QNAME = new QName("http://schemas.microsoft.com/office/word/2010/wordml", "alpha");
    private final static QName _CTSchemeColorHueMod_QNAME = new QName("http://schemas.microsoft.com/office/word/2010/wordml", "hueMod");
    private final static QName _CTSchemeColorSat_QNAME = new QName("http://schemas.microsoft.com/office/word/2010/wordml", "sat");
    private final static QName _CTSchemeColorSatOff_QNAME = new QName("http://schemas.microsoft.com/office/word/2010/wordml", "satOff");
    private final static QName _CTSchemeColorSatMod_QNAME = new QName("http://schemas.microsoft.com/office/word/2010/wordml", "satMod");
    private final static QName _CTSchemeColorLum_QNAME = new QName("http://schemas.microsoft.com/office/word/2010/wordml", "lum");
    private final static QName _CTSchemeColorLumOff_QNAME = new QName("http://schemas.microsoft.com/office/word/2010/wordml", "lumOff");
    private final static QName _CTSchemeColorLumMod_QNAME = new QName("http://schemas.microsoft.com/office/word/2010/wordml", "lumMod");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.w14
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTWordContentPart }
     * 
     */
    public CTWordContentPart createCTWordContentPart() {
        return new CTWordContentPart();
    }

    /**
     * Create an instance of {@link CTLongHexNumber }
     * 
     */
    public CTLongHexNumber createCTLongHexNumber() {
        return new CTLongHexNumber();
    }

    /**
     * Create an instance of {@link CTOnOff }
     * 
     */
    public CTOnOff createCTOnOff() {
        return new CTOnOff();
    }

    /**
     * Create an instance of {@link CTShadow }
     * 
     */
    public CTShadow createCTShadow() {
        return new CTShadow();
    }

    /**
     * Create an instance of {@link CTDefaultImageDpi }
     * 
     */
    public CTDefaultImageDpi createCTDefaultImageDpi() {
        return new CTDefaultImageDpi();
    }

    /**
     * Create an instance of {@link CTSdtCheckbox }
     * 
     */
    public CTSdtCheckbox createCTSdtCheckbox() {
        return new CTSdtCheckbox();
    }

    /**
     * Create an instance of {@link CTWordContentPartNonVisual }
     * 
     */
    public CTWordContentPartNonVisual createCTWordContentPartNonVisual() {
        return new CTWordContentPartNonVisual();
    }

    /**
     * Create an instance of {@link CTPercentage }
     * 
     */
    public CTPercentage createCTPercentage() {
        return new CTPercentage();
    }

    /**
     * Create an instance of {@link CTPositiveFixedPercentage }
     * 
     */
    public CTPositiveFixedPercentage createCTPositiveFixedPercentage() {
        return new CTPositiveFixedPercentage();
    }

    /**
     * Create an instance of {@link CTPositivePercentage }
     * 
     */
    public CTPositivePercentage createCTPositivePercentage() {
        return new CTPositivePercentage();
    }

    /**
     * Create an instance of {@link CTRelativeRect }
     * 
     */
    public CTRelativeRect createCTRelativeRect() {
        return new CTRelativeRect();
    }

    /**
     * Create an instance of {@link CTSRgbColor }
     * 
     */
    public CTSRgbColor createCTSRgbColor() {
        return new CTSRgbColor();
    }

    /**
     * Create an instance of {@link CTSchemeColor }
     * 
     */
    public CTSchemeColor createCTSchemeColor() {
        return new CTSchemeColor();
    }

    /**
     * Create an instance of {@link CTColor }
     * 
     */
    public CTColor createCTColor() {
        return new CTColor();
    }

    /**
     * Create an instance of {@link CTGradientStop }
     * 
     */
    public CTGradientStop createCTGradientStop() {
        return new CTGradientStop();
    }

    /**
     * Create an instance of {@link CTGradientStopList }
     * 
     */
    public CTGradientStopList createCTGradientStopList() {
        return new CTGradientStopList();
    }

    /**
     * Create an instance of {@link CTLinearShadeProperties }
     * 
     */
    public CTLinearShadeProperties createCTLinearShadeProperties() {
        return new CTLinearShadeProperties();
    }

    /**
     * Create an instance of {@link CTPathShadeProperties }
     * 
     */
    public CTPathShadeProperties createCTPathShadeProperties() {
        return new CTPathShadeProperties();
    }

    /**
     * Create an instance of {@link CTSolidColorFillProperties }
     * 
     */
    public CTSolidColorFillProperties createCTSolidColorFillProperties() {
        return new CTSolidColorFillProperties();
    }

    /**
     * Create an instance of {@link CTGradientFillProperties }
     * 
     */
    public CTGradientFillProperties createCTGradientFillProperties() {
        return new CTGradientFillProperties();
    }

    /**
     * Create an instance of {@link CTPresetLineDashProperties }
     * 
     */
    public CTPresetLineDashProperties createCTPresetLineDashProperties() {
        return new CTPresetLineDashProperties();
    }

    /**
     * Create an instance of {@link CTLineJoinMiterProperties }
     * 
     */
    public CTLineJoinMiterProperties createCTLineJoinMiterProperties() {
        return new CTLineJoinMiterProperties();
    }

    /**
     * Create an instance of {@link CTCamera }
     * 
     */
    public CTCamera createCTCamera() {
        return new CTCamera();
    }

    /**
     * Create an instance of {@link CTSphereCoords }
     * 
     */
    public CTSphereCoords createCTSphereCoords() {
        return new CTSphereCoords();
    }

    /**
     * Create an instance of {@link CTLightRig }
     * 
     */
    public CTLightRig createCTLightRig() {
        return new CTLightRig();
    }

    /**
     * Create an instance of {@link CTBevel }
     * 
     */
    public CTBevel createCTBevel() {
        return new CTBevel();
    }

    /**
     * Create an instance of {@link CTGlow }
     * 
     */
    public CTGlow createCTGlow() {
        return new CTGlow();
    }

    /**
     * Create an instance of {@link CTReflection }
     * 
     */
    public CTReflection createCTReflection() {
        return new CTReflection();
    }

    /**
     * Create an instance of {@link CTFillTextEffect }
     * 
     */
    public CTFillTextEffect createCTFillTextEffect() {
        return new CTFillTextEffect();
    }

    /**
     * Create an instance of {@link CTTextOutlineEffect }
     * 
     */
    public CTTextOutlineEffect createCTTextOutlineEffect() {
        return new CTTextOutlineEffect();
    }

    /**
     * Create an instance of {@link CTScene3D }
     * 
     */
    public CTScene3D createCTScene3D() {
        return new CTScene3D();
    }

    /**
     * Create an instance of {@link CTProps3D }
     * 
     */
    public CTProps3D createCTProps3D() {
        return new CTProps3D();
    }

    /**
     * Create an instance of {@link CTLigatures }
     * 
     */
    public CTLigatures createCTLigatures() {
        return new CTLigatures();
    }

    /**
     * Create an instance of {@link CTNumForm }
     * 
     */
    public CTNumForm createCTNumForm() {
        return new CTNumForm();
    }

    /**
     * Create an instance of {@link CTNumSpacing }
     * 
     */
    public CTNumSpacing createCTNumSpacing() {
        return new CTNumSpacing();
    }

    /**
     * Create an instance of {@link CTStyleSet }
     * 
     */
    public CTStyleSet createCTStyleSet() {
        return new CTStyleSet();
    }

    /**
     * Create an instance of {@link CTStylisticSets }
     * 
     */
    public CTStylisticSets createCTStylisticSets() {
        return new CTStylisticSets();
    }

    /**
     * Create an instance of {@link CTSdtCheckboxSymbol }
     * 
     */
    public CTSdtCheckboxSymbol createCTSdtCheckboxSymbol() {
        return new CTSdtCheckboxSymbol();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTWordContentPart }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2010/wordml", name = "contentPart")
    public JAXBElement<CTWordContentPart> createContentPart(CTWordContentPart value) {
        return new JAXBElement<CTWordContentPart>(_ContentPart_QNAME, CTWordContentPart.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTLongHexNumber }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2010/wordml", name = "docId")
    public JAXBElement<CTLongHexNumber> createDocId(CTLongHexNumber value) {
        return new JAXBElement<CTLongHexNumber>(_DocId_QNAME, CTLongHexNumber.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTOnOff }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2010/wordml", name = "conflictMode")
    public JAXBElement<CTOnOff> createConflictMode(CTOnOff value) {
        return new JAXBElement<CTOnOff>(_ConflictMode_QNAME, CTOnOff.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTTrackChange }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2010/wordml", name = "customXmlConflictInsRangeStart")
    public JAXBElement<CTTrackChange> createCustomXmlConflictInsRangeStart(CTTrackChange value) {
        return new JAXBElement<CTTrackChange>(_CustomXmlConflictInsRangeStart_QNAME, CTTrackChange.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTMarkup }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2010/wordml", name = "customXmlConflictInsRangeEnd")
    public JAXBElement<CTMarkup> createCustomXmlConflictInsRangeEnd(CTMarkup value) {
        return new JAXBElement<CTMarkup>(_CustomXmlConflictInsRangeEnd_QNAME, CTMarkup.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTTrackChange }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2010/wordml", name = "customXmlConflictDelRangeStart")
    public JAXBElement<CTTrackChange> createCustomXmlConflictDelRangeStart(CTTrackChange value) {
        return new JAXBElement<CTTrackChange>(_CustomXmlConflictDelRangeStart_QNAME, CTTrackChange.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTMarkup }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2010/wordml", name = "customXmlConflictDelRangeEnd")
    public JAXBElement<CTMarkup> createCustomXmlConflictDelRangeEnd(CTMarkup value) {
        return new JAXBElement<CTMarkup>(_CustomXmlConflictDelRangeEnd_QNAME, CTMarkup.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTShadow }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2010/wordml", name = "shadow")
    public JAXBElement<CTShadow> createShadow(CTShadow value) {
        return new JAXBElement<CTShadow>(_Shadow_QNAME, CTShadow.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTOnOff }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2010/wordml", name = "discardImageEditingData")
    public JAXBElement<CTOnOff> createDiscardImageEditingData(CTOnOff value) {
        return new JAXBElement<CTOnOff>(_DiscardImageEditingData_QNAME, CTOnOff.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTDefaultImageDpi }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2010/wordml", name = "defaultImageDpi")
    public JAXBElement<CTDefaultImageDpi> createDefaultImageDpi(CTDefaultImageDpi value) {
        return new JAXBElement<CTDefaultImageDpi>(_DefaultImageDpi_QNAME, CTDefaultImageDpi.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTEmpty }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2010/wordml", name = "entityPicker")
    public JAXBElement<CTEmpty> createEntityPicker(CTEmpty value) {
        return new JAXBElement<CTEmpty>(_EntityPicker_QNAME, CTEmpty.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTSdtCheckbox }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2010/wordml", name = "checkbox")
    public JAXBElement<CTSdtCheckbox> createCheckbox(CTSdtCheckbox value) {
        return new JAXBElement<CTSdtCheckbox>(_Checkbox_QNAME, CTSdtCheckbox.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositiveFixedPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2010/wordml", name = "tint", scope = CTSchemeColor.class)
    public JAXBElement<CTPositiveFixedPercentage> createCTSchemeColorTint(CTPositiveFixedPercentage value) {
        return new JAXBElement<CTPositiveFixedPercentage>(_CTSchemeColorTint_QNAME, CTPositiveFixedPercentage.class, CTSchemeColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositiveFixedPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2010/wordml", name = "shade", scope = CTSchemeColor.class)
    public JAXBElement<CTPositiveFixedPercentage> createCTSchemeColorShade(CTPositiveFixedPercentage value) {
        return new JAXBElement<CTPositiveFixedPercentage>(_CTSchemeColorShade_QNAME, CTPositiveFixedPercentage.class, CTSchemeColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositiveFixedPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2010/wordml", name = "alpha", scope = CTSchemeColor.class)
    public JAXBElement<CTPositiveFixedPercentage> createCTSchemeColorAlpha(CTPositiveFixedPercentage value) {
        return new JAXBElement<CTPositiveFixedPercentage>(_CTSchemeColorAlpha_QNAME, CTPositiveFixedPercentage.class, CTSchemeColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositivePercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2010/wordml", name = "hueMod", scope = CTSchemeColor.class)
    public JAXBElement<CTPositivePercentage> createCTSchemeColorHueMod(CTPositivePercentage value) {
        return new JAXBElement<CTPositivePercentage>(_CTSchemeColorHueMod_QNAME, CTPositivePercentage.class, CTSchemeColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2010/wordml", name = "sat", scope = CTSchemeColor.class)
    public JAXBElement<CTPercentage> createCTSchemeColorSat(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTSchemeColorSat_QNAME, CTPercentage.class, CTSchemeColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2010/wordml", name = "satOff", scope = CTSchemeColor.class)
    public JAXBElement<CTPercentage> createCTSchemeColorSatOff(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTSchemeColorSatOff_QNAME, CTPercentage.class, CTSchemeColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2010/wordml", name = "satMod", scope = CTSchemeColor.class)
    public JAXBElement<CTPercentage> createCTSchemeColorSatMod(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTSchemeColorSatMod_QNAME, CTPercentage.class, CTSchemeColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2010/wordml", name = "lum", scope = CTSchemeColor.class)
    public JAXBElement<CTPercentage> createCTSchemeColorLum(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTSchemeColorLum_QNAME, CTPercentage.class, CTSchemeColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2010/wordml", name = "lumOff", scope = CTSchemeColor.class)
    public JAXBElement<CTPercentage> createCTSchemeColorLumOff(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTSchemeColorLumOff_QNAME, CTPercentage.class, CTSchemeColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2010/wordml", name = "lumMod", scope = CTSchemeColor.class)
    public JAXBElement<CTPercentage> createCTSchemeColorLumMod(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTSchemeColorLumMod_QNAME, CTPercentage.class, CTSchemeColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositiveFixedPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2010/wordml", name = "tint", scope = CTSRgbColor.class)
    public JAXBElement<CTPositiveFixedPercentage> createCTSRgbColorTint(CTPositiveFixedPercentage value) {
        return new JAXBElement<CTPositiveFixedPercentage>(_CTSchemeColorTint_QNAME, CTPositiveFixedPercentage.class, CTSRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositiveFixedPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2010/wordml", name = "shade", scope = CTSRgbColor.class)
    public JAXBElement<CTPositiveFixedPercentage> createCTSRgbColorShade(CTPositiveFixedPercentage value) {
        return new JAXBElement<CTPositiveFixedPercentage>(_CTSchemeColorShade_QNAME, CTPositiveFixedPercentage.class, CTSRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositiveFixedPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2010/wordml", name = "alpha", scope = CTSRgbColor.class)
    public JAXBElement<CTPositiveFixedPercentage> createCTSRgbColorAlpha(CTPositiveFixedPercentage value) {
        return new JAXBElement<CTPositiveFixedPercentage>(_CTSchemeColorAlpha_QNAME, CTPositiveFixedPercentage.class, CTSRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPositivePercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2010/wordml", name = "hueMod", scope = CTSRgbColor.class)
    public JAXBElement<CTPositivePercentage> createCTSRgbColorHueMod(CTPositivePercentage value) {
        return new JAXBElement<CTPositivePercentage>(_CTSchemeColorHueMod_QNAME, CTPositivePercentage.class, CTSRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2010/wordml", name = "sat", scope = CTSRgbColor.class)
    public JAXBElement<CTPercentage> createCTSRgbColorSat(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTSchemeColorSat_QNAME, CTPercentage.class, CTSRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2010/wordml", name = "satOff", scope = CTSRgbColor.class)
    public JAXBElement<CTPercentage> createCTSRgbColorSatOff(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTSchemeColorSatOff_QNAME, CTPercentage.class, CTSRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2010/wordml", name = "satMod", scope = CTSRgbColor.class)
    public JAXBElement<CTPercentage> createCTSRgbColorSatMod(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTSchemeColorSatMod_QNAME, CTPercentage.class, CTSRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2010/wordml", name = "lum", scope = CTSRgbColor.class)
    public JAXBElement<CTPercentage> createCTSRgbColorLum(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTSchemeColorLum_QNAME, CTPercentage.class, CTSRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2010/wordml", name = "lumOff", scope = CTSRgbColor.class)
    public JAXBElement<CTPercentage> createCTSRgbColorLumOff(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTSchemeColorLumOff_QNAME, CTPercentage.class, CTSRgbColor.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2010/wordml", name = "lumMod", scope = CTSRgbColor.class)
    public JAXBElement<CTPercentage> createCTSRgbColorLumMod(CTPercentage value) {
        return new JAXBElement<CTPercentage>(_CTSchemeColorLumMod_QNAME, CTPercentage.class, CTSRgbColor.class, value);
    }

}
