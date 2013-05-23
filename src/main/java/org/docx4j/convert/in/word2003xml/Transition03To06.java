
package org.docx4j.convert.in.word2003xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.wml.Body;
import org.docx4j.wml.Fonts;
import org.docx4j.wml.Numbering;
import org.docx4j.wml.Styles;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}fonts"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}numbering"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}styles"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}body"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "fonts",
    "numbering",
    "styles",
    "body"
})
@XmlRootElement(name = "transition03to06")
public class Transition03To06 {

    @XmlElement(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
    protected Fonts fonts;
    @XmlElement(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
    protected Numbering numbering;
    @XmlElement(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
    protected Styles styles;
    @XmlElement(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
    protected Body body;

    /**
     * Gets the value of the fonts property.
     * 
     * @return
     *     possible object is
     *     {@link Fonts }
     *     
     */
    public Fonts getFonts() {
        return fonts;
    }

    /**
     * Sets the value of the fonts property.
     * 
     * @param value
     *     allowed object is
     *     {@link Fonts }
     *     
     */
    public void setFonts(Fonts value) {
        this.fonts = value;
    }

    /**
     * Gets the value of the numbering property.
     * 
     * @return
     *     possible object is
     *     {@link Numbering }
     *     
     */
    public Numbering getNumbering() {
        return numbering;
    }

    /**
     * Sets the value of the numbering property.
     * 
     * @param value
     *     allowed object is
     *     {@link Numbering }
     *     
     */
    public void setNumbering(Numbering value) {
        this.numbering = value;
    }

    /**
     * Gets the value of the styles property.
     * 
     * @return
     *     possible object is
     *     {@link Styles }
     *     
     */
    public Styles getStyles() {
        return styles;
    }

    /**
     * Sets the value of the styles property.
     * 
     * @param value
     *     allowed object is
     *     {@link Styles }
     *     
     */
    public void setStyles(Styles value) {
        this.styles = value;
    }

    /**
     * Gets the value of the body property.
     * 
     * @return
     *     possible object is
     *     {@link Body }
     *     
     */
    public Body getBody() {
        return body;
    }

    /**
     * Sets the value of the body property.
     * 
     * @param value
     *     allowed object is
     *     {@link Body }
     *     
     */
    public void setBody(Body value) {
        this.body = value;
    }

}
