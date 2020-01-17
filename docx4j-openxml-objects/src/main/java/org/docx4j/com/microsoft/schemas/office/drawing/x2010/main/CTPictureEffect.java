
package org.docx4j.com.microsoft.schemas.office.drawing.x2010.main;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_PictureEffect complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PictureEffect"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="artisticBlur" type="{http://schemas.microsoft.com/office/drawing/2010/main}CT_PictureEffectBlur"/&gt;
 *         &lt;element name="artisticCement" type="{http://schemas.microsoft.com/office/drawing/2010/main}CT_PictureEffectCement"/&gt;
 *         &lt;element name="artisticChalkSketch" type="{http://schemas.microsoft.com/office/drawing/2010/main}CT_PictureEffectChalkSketch"/&gt;
 *         &lt;element name="artisticCrisscrossEtching" type="{http://schemas.microsoft.com/office/drawing/2010/main}CT_PictureEffectCrisscrossEtching"/&gt;
 *         &lt;element name="artisticCutout" type="{http://schemas.microsoft.com/office/drawing/2010/main}CT_PictureEffectCutout"/&gt;
 *         &lt;element name="artisticFilmGrain" type="{http://schemas.microsoft.com/office/drawing/2010/main}CT_PictureEffectFilmGrain"/&gt;
 *         &lt;element name="artisticGlass" type="{http://schemas.microsoft.com/office/drawing/2010/main}CT_PictureEffectGlass"/&gt;
 *         &lt;element name="artisticGlowDiffused" type="{http://schemas.microsoft.com/office/drawing/2010/main}CT_PictureEffectGlowDiffused"/&gt;
 *         &lt;element name="artisticGlowEdges" type="{http://schemas.microsoft.com/office/drawing/2010/main}CT_PictureEffectGlowEdges"/&gt;
 *         &lt;element name="artisticLightScreen" type="{http://schemas.microsoft.com/office/drawing/2010/main}CT_PictureEffectLightScreen"/&gt;
 *         &lt;element name="artisticLineDrawing" type="{http://schemas.microsoft.com/office/drawing/2010/main}CT_PictureEffectLineDrawing"/&gt;
 *         &lt;element name="artisticMarker" type="{http://schemas.microsoft.com/office/drawing/2010/main}CT_PictureEffectMarker"/&gt;
 *         &lt;element name="artisticMosiaicBubbles" type="{http://schemas.microsoft.com/office/drawing/2010/main}CT_PictureEffectMosiaicBubbles"/&gt;
 *         &lt;element name="artisticPaintStrokes" type="{http://schemas.microsoft.com/office/drawing/2010/main}CT_PictureEffectPaintStrokes"/&gt;
 *         &lt;element name="artisticPaintBrush" type="{http://schemas.microsoft.com/office/drawing/2010/main}CT_PictureEffectPaintBrush"/&gt;
 *         &lt;element name="artisticPastelsSmooth" type="{http://schemas.microsoft.com/office/drawing/2010/main}CT_PictureEffectPastelsSmooth"/&gt;
 *         &lt;element name="artisticPencilGrayscale" type="{http://schemas.microsoft.com/office/drawing/2010/main}CT_PictureEffectPencilGrayscale"/&gt;
 *         &lt;element name="artisticPencilSketch" type="{http://schemas.microsoft.com/office/drawing/2010/main}CT_PictureEffectPencilSketch"/&gt;
 *         &lt;element name="artisticPhotocopy" type="{http://schemas.microsoft.com/office/drawing/2010/main}CT_PictureEffectPhotocopy"/&gt;
 *         &lt;element name="artisticPlasticWrap" type="{http://schemas.microsoft.com/office/drawing/2010/main}CT_PictureEffectPlasticWrap"/&gt;
 *         &lt;element name="artisticTexturizer" type="{http://schemas.microsoft.com/office/drawing/2010/main}CT_PictureEffectTexturizer"/&gt;
 *         &lt;element name="artisticWatercolorSponge" type="{http://schemas.microsoft.com/office/drawing/2010/main}CT_PictureEffectWatercolorSponge"/&gt;
 *         &lt;element name="backgroundRemoval" type="{http://schemas.microsoft.com/office/drawing/2010/main}CT_PictureEffectBackgroundRemoval"/&gt;
 *         &lt;element name="brightnessContrast" type="{http://schemas.microsoft.com/office/drawing/2010/main}CT_PictureEffectBrightnessContrast"/&gt;
 *         &lt;element name="colorTemperature" type="{http://schemas.microsoft.com/office/drawing/2010/main}CT_PictureEffectColorTemperature"/&gt;
 *         &lt;element name="saturation" type="{http://schemas.microsoft.com/office/drawing/2010/main}CT_PictureEffectSaturation"/&gt;
 *         &lt;element name="sharpenSoften" type="{http://schemas.microsoft.com/office/drawing/2010/main}CT_PictureEffectSharpenSoften"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="visible" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_PictureEffect", propOrder = {
    "artisticBlur",
    "artisticCement",
    "artisticChalkSketch",
    "artisticCrisscrossEtching",
    "artisticCutout",
    "artisticFilmGrain",
    "artisticGlass",
    "artisticGlowDiffused",
    "artisticGlowEdges",
    "artisticLightScreen",
    "artisticLineDrawing",
    "artisticMarker",
    "artisticMosiaicBubbles",
    "artisticPaintStrokes",
    "artisticPaintBrush",
    "artisticPastelsSmooth",
    "artisticPencilGrayscale",
    "artisticPencilSketch",
    "artisticPhotocopy",
    "artisticPlasticWrap",
    "artisticTexturizer",
    "artisticWatercolorSponge",
    "backgroundRemoval",
    "brightnessContrast",
    "colorTemperature",
    "saturation",
    "sharpenSoften"
})
public class CTPictureEffect implements Child
{

    protected CTPictureEffectBlur artisticBlur;
    protected CTPictureEffectCement artisticCement;
    protected CTPictureEffectChalkSketch artisticChalkSketch;
    protected CTPictureEffectCrisscrossEtching artisticCrisscrossEtching;
    protected CTPictureEffectCutout artisticCutout;
    protected CTPictureEffectFilmGrain artisticFilmGrain;
    protected CTPictureEffectGlass artisticGlass;
    protected CTPictureEffectGlowDiffused artisticGlowDiffused;
    protected CTPictureEffectGlowEdges artisticGlowEdges;
    protected CTPictureEffectLightScreen artisticLightScreen;
    protected CTPictureEffectLineDrawing artisticLineDrawing;
    protected CTPictureEffectMarker artisticMarker;
    protected CTPictureEffectMosiaicBubbles artisticMosiaicBubbles;
    protected CTPictureEffectPaintStrokes artisticPaintStrokes;
    protected CTPictureEffectPaintBrush artisticPaintBrush;
    protected CTPictureEffectPastelsSmooth artisticPastelsSmooth;
    protected CTPictureEffectPencilGrayscale artisticPencilGrayscale;
    protected CTPictureEffectPencilSketch artisticPencilSketch;
    protected CTPictureEffectPhotocopy artisticPhotocopy;
    protected CTPictureEffectPlasticWrap artisticPlasticWrap;
    protected CTPictureEffectTexturizer artisticTexturizer;
    protected CTPictureEffectWatercolorSponge artisticWatercolorSponge;
    protected CTPictureEffectBackgroundRemoval backgroundRemoval;
    protected CTPictureEffectBrightnessContrast brightnessContrast;
    protected CTPictureEffectColorTemperature colorTemperature;
    protected CTPictureEffectSaturation saturation;
    protected CTPictureEffectSharpenSoften sharpenSoften;
    @XmlAttribute(name = "visible")
    protected Boolean visible;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the artisticBlur property.
     * 
     * @return
     *     possible object is
     *     {@link CTPictureEffectBlur }
     *     
     */
    public CTPictureEffectBlur getArtisticBlur() {
        return artisticBlur;
    }

    /**
     * Sets the value of the artisticBlur property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPictureEffectBlur }
     *     
     */
    public void setArtisticBlur(CTPictureEffectBlur value) {
        this.artisticBlur = value;
    }

    /**
     * Gets the value of the artisticCement property.
     * 
     * @return
     *     possible object is
     *     {@link CTPictureEffectCement }
     *     
     */
    public CTPictureEffectCement getArtisticCement() {
        return artisticCement;
    }

    /**
     * Sets the value of the artisticCement property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPictureEffectCement }
     *     
     */
    public void setArtisticCement(CTPictureEffectCement value) {
        this.artisticCement = value;
    }

    /**
     * Gets the value of the artisticChalkSketch property.
     * 
     * @return
     *     possible object is
     *     {@link CTPictureEffectChalkSketch }
     *     
     */
    public CTPictureEffectChalkSketch getArtisticChalkSketch() {
        return artisticChalkSketch;
    }

    /**
     * Sets the value of the artisticChalkSketch property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPictureEffectChalkSketch }
     *     
     */
    public void setArtisticChalkSketch(CTPictureEffectChalkSketch value) {
        this.artisticChalkSketch = value;
    }

    /**
     * Gets the value of the artisticCrisscrossEtching property.
     * 
     * @return
     *     possible object is
     *     {@link CTPictureEffectCrisscrossEtching }
     *     
     */
    public CTPictureEffectCrisscrossEtching getArtisticCrisscrossEtching() {
        return artisticCrisscrossEtching;
    }

    /**
     * Sets the value of the artisticCrisscrossEtching property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPictureEffectCrisscrossEtching }
     *     
     */
    public void setArtisticCrisscrossEtching(CTPictureEffectCrisscrossEtching value) {
        this.artisticCrisscrossEtching = value;
    }

    /**
     * Gets the value of the artisticCutout property.
     * 
     * @return
     *     possible object is
     *     {@link CTPictureEffectCutout }
     *     
     */
    public CTPictureEffectCutout getArtisticCutout() {
        return artisticCutout;
    }

    /**
     * Sets the value of the artisticCutout property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPictureEffectCutout }
     *     
     */
    public void setArtisticCutout(CTPictureEffectCutout value) {
        this.artisticCutout = value;
    }

    /**
     * Gets the value of the artisticFilmGrain property.
     * 
     * @return
     *     possible object is
     *     {@link CTPictureEffectFilmGrain }
     *     
     */
    public CTPictureEffectFilmGrain getArtisticFilmGrain() {
        return artisticFilmGrain;
    }

    /**
     * Sets the value of the artisticFilmGrain property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPictureEffectFilmGrain }
     *     
     */
    public void setArtisticFilmGrain(CTPictureEffectFilmGrain value) {
        this.artisticFilmGrain = value;
    }

    /**
     * Gets the value of the artisticGlass property.
     * 
     * @return
     *     possible object is
     *     {@link CTPictureEffectGlass }
     *     
     */
    public CTPictureEffectGlass getArtisticGlass() {
        return artisticGlass;
    }

    /**
     * Sets the value of the artisticGlass property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPictureEffectGlass }
     *     
     */
    public void setArtisticGlass(CTPictureEffectGlass value) {
        this.artisticGlass = value;
    }

    /**
     * Gets the value of the artisticGlowDiffused property.
     * 
     * @return
     *     possible object is
     *     {@link CTPictureEffectGlowDiffused }
     *     
     */
    public CTPictureEffectGlowDiffused getArtisticGlowDiffused() {
        return artisticGlowDiffused;
    }

    /**
     * Sets the value of the artisticGlowDiffused property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPictureEffectGlowDiffused }
     *     
     */
    public void setArtisticGlowDiffused(CTPictureEffectGlowDiffused value) {
        this.artisticGlowDiffused = value;
    }

    /**
     * Gets the value of the artisticGlowEdges property.
     * 
     * @return
     *     possible object is
     *     {@link CTPictureEffectGlowEdges }
     *     
     */
    public CTPictureEffectGlowEdges getArtisticGlowEdges() {
        return artisticGlowEdges;
    }

    /**
     * Sets the value of the artisticGlowEdges property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPictureEffectGlowEdges }
     *     
     */
    public void setArtisticGlowEdges(CTPictureEffectGlowEdges value) {
        this.artisticGlowEdges = value;
    }

    /**
     * Gets the value of the artisticLightScreen property.
     * 
     * @return
     *     possible object is
     *     {@link CTPictureEffectLightScreen }
     *     
     */
    public CTPictureEffectLightScreen getArtisticLightScreen() {
        return artisticLightScreen;
    }

    /**
     * Sets the value of the artisticLightScreen property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPictureEffectLightScreen }
     *     
     */
    public void setArtisticLightScreen(CTPictureEffectLightScreen value) {
        this.artisticLightScreen = value;
    }

    /**
     * Gets the value of the artisticLineDrawing property.
     * 
     * @return
     *     possible object is
     *     {@link CTPictureEffectLineDrawing }
     *     
     */
    public CTPictureEffectLineDrawing getArtisticLineDrawing() {
        return artisticLineDrawing;
    }

    /**
     * Sets the value of the artisticLineDrawing property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPictureEffectLineDrawing }
     *     
     */
    public void setArtisticLineDrawing(CTPictureEffectLineDrawing value) {
        this.artisticLineDrawing = value;
    }

    /**
     * Gets the value of the artisticMarker property.
     * 
     * @return
     *     possible object is
     *     {@link CTPictureEffectMarker }
     *     
     */
    public CTPictureEffectMarker getArtisticMarker() {
        return artisticMarker;
    }

    /**
     * Sets the value of the artisticMarker property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPictureEffectMarker }
     *     
     */
    public void setArtisticMarker(CTPictureEffectMarker value) {
        this.artisticMarker = value;
    }

    /**
     * Gets the value of the artisticMosiaicBubbles property.
     * 
     * @return
     *     possible object is
     *     {@link CTPictureEffectMosiaicBubbles }
     *     
     */
    public CTPictureEffectMosiaicBubbles getArtisticMosiaicBubbles() {
        return artisticMosiaicBubbles;
    }

    /**
     * Sets the value of the artisticMosiaicBubbles property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPictureEffectMosiaicBubbles }
     *     
     */
    public void setArtisticMosiaicBubbles(CTPictureEffectMosiaicBubbles value) {
        this.artisticMosiaicBubbles = value;
    }

    /**
     * Gets the value of the artisticPaintStrokes property.
     * 
     * @return
     *     possible object is
     *     {@link CTPictureEffectPaintStrokes }
     *     
     */
    public CTPictureEffectPaintStrokes getArtisticPaintStrokes() {
        return artisticPaintStrokes;
    }

    /**
     * Sets the value of the artisticPaintStrokes property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPictureEffectPaintStrokes }
     *     
     */
    public void setArtisticPaintStrokes(CTPictureEffectPaintStrokes value) {
        this.artisticPaintStrokes = value;
    }

    /**
     * Gets the value of the artisticPaintBrush property.
     * 
     * @return
     *     possible object is
     *     {@link CTPictureEffectPaintBrush }
     *     
     */
    public CTPictureEffectPaintBrush getArtisticPaintBrush() {
        return artisticPaintBrush;
    }

    /**
     * Sets the value of the artisticPaintBrush property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPictureEffectPaintBrush }
     *     
     */
    public void setArtisticPaintBrush(CTPictureEffectPaintBrush value) {
        this.artisticPaintBrush = value;
    }

    /**
     * Gets the value of the artisticPastelsSmooth property.
     * 
     * @return
     *     possible object is
     *     {@link CTPictureEffectPastelsSmooth }
     *     
     */
    public CTPictureEffectPastelsSmooth getArtisticPastelsSmooth() {
        return artisticPastelsSmooth;
    }

    /**
     * Sets the value of the artisticPastelsSmooth property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPictureEffectPastelsSmooth }
     *     
     */
    public void setArtisticPastelsSmooth(CTPictureEffectPastelsSmooth value) {
        this.artisticPastelsSmooth = value;
    }

    /**
     * Gets the value of the artisticPencilGrayscale property.
     * 
     * @return
     *     possible object is
     *     {@link CTPictureEffectPencilGrayscale }
     *     
     */
    public CTPictureEffectPencilGrayscale getArtisticPencilGrayscale() {
        return artisticPencilGrayscale;
    }

    /**
     * Sets the value of the artisticPencilGrayscale property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPictureEffectPencilGrayscale }
     *     
     */
    public void setArtisticPencilGrayscale(CTPictureEffectPencilGrayscale value) {
        this.artisticPencilGrayscale = value;
    }

    /**
     * Gets the value of the artisticPencilSketch property.
     * 
     * @return
     *     possible object is
     *     {@link CTPictureEffectPencilSketch }
     *     
     */
    public CTPictureEffectPencilSketch getArtisticPencilSketch() {
        return artisticPencilSketch;
    }

    /**
     * Sets the value of the artisticPencilSketch property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPictureEffectPencilSketch }
     *     
     */
    public void setArtisticPencilSketch(CTPictureEffectPencilSketch value) {
        this.artisticPencilSketch = value;
    }

    /**
     * Gets the value of the artisticPhotocopy property.
     * 
     * @return
     *     possible object is
     *     {@link CTPictureEffectPhotocopy }
     *     
     */
    public CTPictureEffectPhotocopy getArtisticPhotocopy() {
        return artisticPhotocopy;
    }

    /**
     * Sets the value of the artisticPhotocopy property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPictureEffectPhotocopy }
     *     
     */
    public void setArtisticPhotocopy(CTPictureEffectPhotocopy value) {
        this.artisticPhotocopy = value;
    }

    /**
     * Gets the value of the artisticPlasticWrap property.
     * 
     * @return
     *     possible object is
     *     {@link CTPictureEffectPlasticWrap }
     *     
     */
    public CTPictureEffectPlasticWrap getArtisticPlasticWrap() {
        return artisticPlasticWrap;
    }

    /**
     * Sets the value of the artisticPlasticWrap property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPictureEffectPlasticWrap }
     *     
     */
    public void setArtisticPlasticWrap(CTPictureEffectPlasticWrap value) {
        this.artisticPlasticWrap = value;
    }

    /**
     * Gets the value of the artisticTexturizer property.
     * 
     * @return
     *     possible object is
     *     {@link CTPictureEffectTexturizer }
     *     
     */
    public CTPictureEffectTexturizer getArtisticTexturizer() {
        return artisticTexturizer;
    }

    /**
     * Sets the value of the artisticTexturizer property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPictureEffectTexturizer }
     *     
     */
    public void setArtisticTexturizer(CTPictureEffectTexturizer value) {
        this.artisticTexturizer = value;
    }

    /**
     * Gets the value of the artisticWatercolorSponge property.
     * 
     * @return
     *     possible object is
     *     {@link CTPictureEffectWatercolorSponge }
     *     
     */
    public CTPictureEffectWatercolorSponge getArtisticWatercolorSponge() {
        return artisticWatercolorSponge;
    }

    /**
     * Sets the value of the artisticWatercolorSponge property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPictureEffectWatercolorSponge }
     *     
     */
    public void setArtisticWatercolorSponge(CTPictureEffectWatercolorSponge value) {
        this.artisticWatercolorSponge = value;
    }

    /**
     * Gets the value of the backgroundRemoval property.
     * 
     * @return
     *     possible object is
     *     {@link CTPictureEffectBackgroundRemoval }
     *     
     */
    public CTPictureEffectBackgroundRemoval getBackgroundRemoval() {
        return backgroundRemoval;
    }

    /**
     * Sets the value of the backgroundRemoval property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPictureEffectBackgroundRemoval }
     *     
     */
    public void setBackgroundRemoval(CTPictureEffectBackgroundRemoval value) {
        this.backgroundRemoval = value;
    }

    /**
     * Gets the value of the brightnessContrast property.
     * 
     * @return
     *     possible object is
     *     {@link CTPictureEffectBrightnessContrast }
     *     
     */
    public CTPictureEffectBrightnessContrast getBrightnessContrast() {
        return brightnessContrast;
    }

    /**
     * Sets the value of the brightnessContrast property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPictureEffectBrightnessContrast }
     *     
     */
    public void setBrightnessContrast(CTPictureEffectBrightnessContrast value) {
        this.brightnessContrast = value;
    }

    /**
     * Gets the value of the colorTemperature property.
     * 
     * @return
     *     possible object is
     *     {@link CTPictureEffectColorTemperature }
     *     
     */
    public CTPictureEffectColorTemperature getColorTemperature() {
        return colorTemperature;
    }

    /**
     * Sets the value of the colorTemperature property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPictureEffectColorTemperature }
     *     
     */
    public void setColorTemperature(CTPictureEffectColorTemperature value) {
        this.colorTemperature = value;
    }

    /**
     * Gets the value of the saturation property.
     * 
     * @return
     *     possible object is
     *     {@link CTPictureEffectSaturation }
     *     
     */
    public CTPictureEffectSaturation getSaturation() {
        return saturation;
    }

    /**
     * Sets the value of the saturation property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPictureEffectSaturation }
     *     
     */
    public void setSaturation(CTPictureEffectSaturation value) {
        this.saturation = value;
    }

    /**
     * Gets the value of the sharpenSoften property.
     * 
     * @return
     *     possible object is
     *     {@link CTPictureEffectSharpenSoften }
     *     
     */
    public CTPictureEffectSharpenSoften getSharpenSoften() {
        return sharpenSoften;
    }

    /**
     * Sets the value of the sharpenSoften property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPictureEffectSharpenSoften }
     *     
     */
    public void setSharpenSoften(CTPictureEffectSharpenSoften value) {
        this.sharpenSoften = value;
    }

    /**
     * Gets the value of the visible property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isVisible() {
        if (visible == null) {
            return true;
        } else {
            return visible;
        }
    }

    /**
     * Sets the value of the visible property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setVisible(Boolean value) {
        this.visible = value;
    }

    /**
     * Gets the parent object in the object tree representing the unmarshalled xml document.
     * 
     * @return
     *     The parent object.
     */
    public Object getParent() {
        return this.parent;
    }

    public void setParent(Object parent) {
        this.parent = parent;
    }

    /**
     * This method is invoked by the JAXB implementation on each instance when unmarshalling completes.
     * 
     * @param parent
     *     The parent object in the object tree.
     * @param unmarshaller
     *     The unmarshaller that generated the instance.
     */
    public void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
        setParent(parent);
    }

}
