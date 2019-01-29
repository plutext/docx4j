package org.docx4j.wml;

import java.util.List;

import org.jvnet.jaxb2_commons.ppp.Child;

/**
 * @since 2.7
 */
public interface CTCustomXmlElement extends Child {

    /**
     * Gets the value of the customXmlPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTCustomXmlPr }
     *     
     */
    public CTCustomXmlPr getCustomXmlPr();
    
    /**
     * Sets the value of the customXmlPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTCustomXmlPr }
     *     
     */
    public void setCustomXmlPr(CTCustomXmlPr value);
    
    /**
     * Gets the value of the uri property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUri();

    /**
     * Sets the value of the uri property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUri(String value);

    /**
     * Gets the value of the element property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getElement();

    /**
     * Sets the value of the element property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setElement(String value);
    
    
    public List<Object> getContent();
}
