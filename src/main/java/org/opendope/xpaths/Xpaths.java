
package org.opendope.xpaths;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


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
 *         &lt;element name="xpath" maxOccurs="unbounded">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="dataBinding">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;attribute name="xpath" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                           &lt;attribute name="storeItemID" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                           &lt;attribute name="prefixMappings" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *                 &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *                 &lt;attribute name="questionID" type="{http://www.w3.org/2001/XMLSchema}NCName" />
 *                 &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="description" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="comments" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="type" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *                 &lt;attribute name="prepopulate" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *                 &lt;attribute name="fieldWidth" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" />
 *                 &lt;attribute name="lower" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="lowerOperator" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="upper" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="upperOperator" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="source" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
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
    "xpath"
})
@XmlRootElement(name = "xpaths")
public class Xpaths {

    @XmlElement(required = true)
    protected List<Xpaths.Xpath> xpath;

    /**
     * Gets the value of the xpath property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the xpath property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getXpath().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Xpaths.Xpath }
     * 
     * 
     */
    public List<Xpaths.Xpath> getXpath() {
        if (xpath == null) {
            xpath = new ArrayList<Xpaths.Xpath>();
        }
        return this.xpath;
    }


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
     *         &lt;element name="dataBinding">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;attribute name="xpath" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *                 &lt;attribute name="storeItemID" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *                 &lt;attribute name="prefixMappings" type="{http://www.w3.org/2001/XMLSchema}string" />
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/sequence>
     *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}ID" />
     *       &lt;attribute name="questionID" type="{http://www.w3.org/2001/XMLSchema}NCName" />
     *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="description" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="comments" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="type" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
     *       &lt;attribute name="prepopulate" type="{http://www.w3.org/2001/XMLSchema}boolean" />
     *       &lt;attribute name="fieldWidth" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" />
     *       &lt;attribute name="lower" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="lowerOperator" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="upper" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="upperOperator" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="source" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "dataBinding"
    })
    @XmlRootElement(name="xpath", namespace="http://opendope.org/xpaths")
    public static class Xpath {

        @XmlElement(required = true)
        protected Xpaths.Xpath.DataBinding dataBinding;
        @XmlAttribute(required = true)
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        @XmlID
        @XmlSchemaType(name = "ID")
        protected String id;
        @XmlAttribute
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        @XmlSchemaType(name = "NCName")
        protected String questionID;
        @XmlAttribute
        protected String name;
        @XmlAttribute
        protected String description;
        @XmlAttribute
        protected String comments;
        @XmlAttribute
        protected String type;
        @XmlAttribute
        protected Boolean required;
        @XmlAttribute
        protected Boolean prepopulate;
        @XmlAttribute
        @XmlSchemaType(name = "positiveInteger")
        protected BigInteger fieldWidth;
        @XmlAttribute
        protected String lower;
        @XmlAttribute
        protected String lowerOperator;
        @XmlAttribute
        protected String upper;
        @XmlAttribute
        protected String upperOperator;
        @XmlAttribute
        protected String source;

        /**
         * Gets the value of the dataBinding property.
         * 
         * @return
         *     possible object is
         *     {@link Xpaths.Xpath.DataBinding }
         *     
         */
        public Xpaths.Xpath.DataBinding getDataBinding() {
            return dataBinding;
        }

        /**
         * Sets the value of the dataBinding property.
         * 
         * @param value
         *     allowed object is
         *     {@link Xpaths.Xpath.DataBinding }
         *     
         */
        public void setDataBinding(Xpaths.Xpath.DataBinding value) {
            this.dataBinding = value;
        }

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getId() {
            return id;
        }

        /**
         * Sets the value of the id property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setId(String value) {
            this.id = value;
        }

        /**
         * Gets the value of the questionID property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getQuestionID() {
            return questionID;
        }

        /**
         * Sets the value of the questionID property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setQuestionID(String value) {
            this.questionID = value;
        }

        /**
         * Gets the value of the name property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getName() {
            return name;
        }

        /**
         * Sets the value of the name property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setName(String value) {
            this.name = value;
        }

        /**
         * Gets the value of the description property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getDescription() {
            return description;
        }

        /**
         * Sets the value of the description property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setDescription(String value) {
            this.description = value;
        }

        /**
         * Gets the value of the comments property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getComments() {
            return comments;
        }

        /**
         * Sets the value of the comments property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setComments(String value) {
            this.comments = value;
        }

        /**
         * Gets the value of the type property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getType() {
            return type;
        }

        /**
         * Sets the value of the type property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setType(String value) {
            this.type = value;
        }

        /**
         * Gets the value of the required property.
         * 
         * @return
         *     possible object is
         *     {@link Boolean }
         *     
         */
        public Boolean isRequired() {
            return required;
        }

        /**
         * Sets the value of the required property.
         * 
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *     
         */
        public void setRequired(Boolean value) {
            this.required = value;
        }

        /**
         * Gets the value of the prepopulate property.
         * 
         * @return
         *     possible object is
         *     {@link Boolean }
         *     
         */
        public Boolean isPrepopulate() {
            return prepopulate;
        }

        /**
         * Sets the value of the prepopulate property.
         * 
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *     
         */
        public void setPrepopulate(Boolean value) {
            this.prepopulate = value;
        }

        /**
         * Gets the value of the fieldWidth property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getFieldWidth() {
            return fieldWidth;
        }

        /**
         * Sets the value of the fieldWidth property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setFieldWidth(BigInteger value) {
            this.fieldWidth = value;
        }

        /**
         * Gets the value of the lower property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getLower() {
            return lower;
        }

        /**
         * Sets the value of the lower property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setLower(String value) {
            this.lower = value;
        }

        /**
         * Gets the value of the lowerOperator property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getLowerOperator() {
            return lowerOperator;
        }

        /**
         * Sets the value of the lowerOperator property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setLowerOperator(String value) {
            this.lowerOperator = value;
        }

        /**
         * Gets the value of the upper property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getUpper() {
            return upper;
        }

        /**
         * Sets the value of the upper property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setUpper(String value) {
            this.upper = value;
        }

        /**
         * Gets the value of the upperOperator property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getUpperOperator() {
            return upperOperator;
        }

        /**
         * Sets the value of the upperOperator property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setUpperOperator(String value) {
            this.upperOperator = value;
        }

        /**
         * Gets the value of the source property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getSource() {
            return source;
        }

        /**
         * Sets the value of the source property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setSource(String value) {
            this.source = value;
        }


        /**
         * <p>Java class for anonymous complex type.
         * 
         * <p>The following schema fragment specifies the expected content contained within this class.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       &lt;attribute name="xpath" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
         *       &lt;attribute name="storeItemID" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
         *       &lt;attribute name="prefixMappings" type="{http://www.w3.org/2001/XMLSchema}string" />
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class DataBinding {

            @XmlAttribute(required = true)
            protected String xpath;
            @XmlAttribute(required = true)
            protected String storeItemID;
            @XmlAttribute
            protected String prefixMappings;

            /**
             * Gets the value of the xpath property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getXpath() {
                return xpath;
            }

            /**
             * Sets the value of the xpath property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setXpath(String value) {
                this.xpath = value;
            }

            /**
             * Gets the value of the storeItemID property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getStoreItemID() {
                return storeItemID;
            }

            /**
             * Sets the value of the storeItemID property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setStoreItemID(String value) {
                this.storeItemID = value;
            }

            /**
             * Gets the value of the prefixMappings property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getPrefixMappings() {
                return prefixMappings;
            }

            /**
             * Sets the value of the prefixMappings property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setPrefixMappings(String value) {
                this.prefixMappings = value;
            }

        }

    }

}
