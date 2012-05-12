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

package org.docx4j.docProps.extended;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.docProps.variantTypes.Vector;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;all>
 *         &lt;element name="Template" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Manager" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Company" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Pages" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="Words" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="Characters" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="PresentationFormat" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Lines" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="Paragraphs" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="Slides" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="Notes" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="TotalTime" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="HiddenSlides" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="MMClips" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="ScaleCrop" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="HeadingPairs" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}vector"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="TitlesOfParts" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}vector"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="LinksUpToDate" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="CharactersWithSpaces" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="SharedDoc" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="HyperlinkBase" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="HLinks" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}vector"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="HyperlinksChanged" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="DigSig" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}blob"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="Application" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="AppVersion" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DocSecurity" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *       &lt;/all>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {

})
@XmlRootElement(name = "Properties")
public class Properties {

    @XmlElement(name = "Template")
    protected String template;
    @XmlElement(name = "Manager")
    protected String manager;
    @XmlElement(name = "Company")
    protected String company;
    @XmlElement(name = "Pages")
    protected Integer pages;
    @XmlElement(name = "Words")
    protected Integer words;
    @XmlElement(name = "Characters")
    protected Integer characters;
    @XmlElement(name = "PresentationFormat")
    protected String presentationFormat;
    @XmlElement(name = "Lines")
    protected Integer lines;
    @XmlElement(name = "Paragraphs")
    protected Integer paragraphs;
    @XmlElement(name = "Slides")
    protected Integer slides;
    @XmlElement(name = "Notes")
    protected Integer notes;
    @XmlElement(name = "TotalTime")
    protected Integer totalTime;
    @XmlElement(name = "HiddenSlides")
    protected Integer hiddenSlides;
    @XmlElement(name = "MMClips")
    protected Integer mmClips;
    @XmlElement(name = "ScaleCrop")
    protected Boolean scaleCrop;
    @XmlElement(name = "HeadingPairs")
    protected Properties.HeadingPairs headingPairs;
    @XmlElement(name = "TitlesOfParts")
    protected Properties.TitlesOfParts titlesOfParts;
    @XmlElement(name = "LinksUpToDate")
    protected Boolean linksUpToDate;
    @XmlElement(name = "CharactersWithSpaces")
    protected Integer charactersWithSpaces;
    @XmlElement(name = "SharedDoc")
    protected Boolean sharedDoc;
    @XmlElement(name = "HyperlinkBase")
    protected String hyperlinkBase;
    @XmlElement(name = "HLinks")
    protected Properties.HLinks hLinks;
    @XmlElement(name = "HyperlinksChanged")
    protected Boolean hyperlinksChanged;
    @XmlElement(name = "DigSig")
    protected Properties.DigSig digSig;
    @XmlElement(name = "Application")
    protected String application;
    @XmlElement(name = "AppVersion")
    protected String appVersion;
    @XmlElement(name = "DocSecurity")
    protected Integer docSecurity;

    /**
     * Gets the value of the template property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTemplate() {
        return template;
    }

    /**
     * Sets the value of the template property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTemplate(String value) {
        this.template = value;
    }

    /**
     * Gets the value of the manager property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getManager() {
        return manager;
    }

    /**
     * Sets the value of the manager property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setManager(String value) {
        this.manager = value;
    }

    /**
     * Gets the value of the company property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCompany() {
        return company;
    }

    /**
     * Sets the value of the company property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCompany(String value) {
        this.company = value;
    }

    /**
     * Gets the value of the pages property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getPages() {
        return pages;
    }

    /**
     * Sets the value of the pages property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setPages(Integer value) {
        this.pages = value;
    }

    /**
     * Gets the value of the words property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getWords() {
        return words;
    }

    /**
     * Sets the value of the words property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setWords(Integer value) {
        this.words = value;
    }

    /**
     * Gets the value of the characters property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getCharacters() {
        return characters;
    }

    /**
     * Sets the value of the characters property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setCharacters(Integer value) {
        this.characters = value;
    }

    /**
     * Gets the value of the presentationFormat property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPresentationFormat() {
        return presentationFormat;
    }

    /**
     * Sets the value of the presentationFormat property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPresentationFormat(String value) {
        this.presentationFormat = value;
    }

    /**
     * Gets the value of the lines property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getLines() {
        return lines;
    }

    /**
     * Sets the value of the lines property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setLines(Integer value) {
        this.lines = value;
    }

    /**
     * Gets the value of the paragraphs property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getParagraphs() {
        return paragraphs;
    }

    /**
     * Sets the value of the paragraphs property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setParagraphs(Integer value) {
        this.paragraphs = value;
    }

    /**
     * Gets the value of the slides property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getSlides() {
        return slides;
    }

    /**
     * Sets the value of the slides property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setSlides(Integer value) {
        this.slides = value;
    }

    /**
     * Gets the value of the notes property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getNotes() {
        return notes;
    }

    /**
     * Sets the value of the notes property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setNotes(Integer value) {
        this.notes = value;
    }

    /**
     * Gets the value of the totalTime property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getTotalTime() {
        return totalTime;
    }

    /**
     * Sets the value of the totalTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setTotalTime(Integer value) {
        this.totalTime = value;
    }

    /**
     * Gets the value of the hiddenSlides property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getHiddenSlides() {
        return hiddenSlides;
    }

    /**
     * Sets the value of the hiddenSlides property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setHiddenSlides(Integer value) {
        this.hiddenSlides = value;
    }

    /**
     * Gets the value of the mmClips property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMMClips() {
        return mmClips;
    }

    /**
     * Sets the value of the mmClips property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMMClips(Integer value) {
        this.mmClips = value;
    }

    /**
     * Gets the value of the scaleCrop property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isScaleCrop() {
        return scaleCrop;
    }

    /**
     * Sets the value of the scaleCrop property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setScaleCrop(Boolean value) {
        this.scaleCrop = value;
    }

    /**
     * Gets the value of the headingPairs property.
     * 
     * @return
     *     possible object is
     *     {@link Properties.HeadingPairs }
     *     
     */
    public Properties.HeadingPairs getHeadingPairs() {
        return headingPairs;
    }

    /**
     * Sets the value of the headingPairs property.
     * 
     * @param value
     *     allowed object is
     *     {@link Properties.HeadingPairs }
     *     
     */
    public void setHeadingPairs(Properties.HeadingPairs value) {
        this.headingPairs = value;
    }

    /**
     * Gets the value of the titlesOfParts property.
     * 
     * @return
     *     possible object is
     *     {@link Properties.TitlesOfParts }
     *     
     */
    public Properties.TitlesOfParts getTitlesOfParts() {
        return titlesOfParts;
    }

    /**
     * Sets the value of the titlesOfParts property.
     * 
     * @param value
     *     allowed object is
     *     {@link Properties.TitlesOfParts }
     *     
     */
    public void setTitlesOfParts(Properties.TitlesOfParts value) {
        this.titlesOfParts = value;
    }

    /**
     * Gets the value of the linksUpToDate property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isLinksUpToDate() {
        return linksUpToDate;
    }

    /**
     * Sets the value of the linksUpToDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setLinksUpToDate(Boolean value) {
        this.linksUpToDate = value;
    }

    /**
     * Gets the value of the charactersWithSpaces property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getCharactersWithSpaces() {
        return charactersWithSpaces;
    }

    /**
     * Sets the value of the charactersWithSpaces property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setCharactersWithSpaces(Integer value) {
        this.charactersWithSpaces = value;
    }

    /**
     * Gets the value of the sharedDoc property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isSharedDoc() {
        return sharedDoc;
    }

    /**
     * Sets the value of the sharedDoc property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSharedDoc(Boolean value) {
        this.sharedDoc = value;
    }

    /**
     * Gets the value of the hyperlinkBase property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHyperlinkBase() {
        return hyperlinkBase;
    }

    /**
     * Sets the value of the hyperlinkBase property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHyperlinkBase(String value) {
        this.hyperlinkBase = value;
    }

    /**
     * Gets the value of the hLinks property.
     * 
     * @return
     *     possible object is
     *     {@link Properties.HLinks }
     *     
     */
    public Properties.HLinks getHLinks() {
        return hLinks;
    }

    /**
     * Sets the value of the hLinks property.
     * 
     * @param value
     *     allowed object is
     *     {@link Properties.HLinks }
     *     
     */
    public void setHLinks(Properties.HLinks value) {
        this.hLinks = value;
    }

    /**
     * Gets the value of the hyperlinksChanged property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isHyperlinksChanged() {
        return hyperlinksChanged;
    }

    /**
     * Sets the value of the hyperlinksChanged property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setHyperlinksChanged(Boolean value) {
        this.hyperlinksChanged = value;
    }

    /**
     * Gets the value of the digSig property.
     * 
     * @return
     *     possible object is
     *     {@link Properties.DigSig }
     *     
     */
    public Properties.DigSig getDigSig() {
        return digSig;
    }

    /**
     * Sets the value of the digSig property.
     * 
     * @param value
     *     allowed object is
     *     {@link Properties.DigSig }
     *     
     */
    public void setDigSig(Properties.DigSig value) {
        this.digSig = value;
    }

    /**
     * Gets the value of the application property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getApplication() {
        return application;
    }

    /**
     * Sets the value of the application property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setApplication(String value) {
        this.application = value;
    }

    /**
     * Gets the value of the appVersion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAppVersion() {
        return appVersion;
    }

    /**
     * Sets the value of the appVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAppVersion(String value) {
        this.appVersion = value;
    }

    /**
     * Gets the value of the docSecurity property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getDocSecurity() {
        return docSecurity;
    }

    /**
     * Sets the value of the docSecurity property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setDocSecurity(Integer value) {
        this.docSecurity = value;
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
     *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}blob"/>
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
        "blob"
    })
    public static class DigSig {

        @XmlElement(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", required = true)
        protected byte[] blob;

        /**
         * Binary
         * 										Blob
         * 
         * @return
         *     possible object is
         *     byte[]
         */
        public byte[] getBlob() {
            return blob;
        }

        /**
         * Sets the value of the blob property.
         * 
         * @param value
         *     allowed object is
         *     byte[]
         */
        public void setBlob(byte[] value) {
            this.blob = ((byte[]) value);
        }

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
     *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}vector"/>
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
        "vector"
    })
    public static class HeadingPairs {

        @XmlElement(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", required = true)
        protected Vector vector;

        /**
         * 
         * 										Vector
         * 
         * @return
         *     possible object is
         *     {@link Vector }
         *     
         */
        public Vector getVector() {
            return vector;
        }

        /**
         * Sets the value of the vector property.
         * 
         * @param value
         *     allowed object is
         *     {@link Vector }
         *     
         */
        public void setVector(Vector value) {
            this.vector = value;
        }

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
     *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}vector"/>
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
        "vector"
    })
    public static class HLinks {

        @XmlElement(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", required = true)
        protected Vector vector;

        /**
         * 
         * 										Vector
         * 
         * @return
         *     possible object is
         *     {@link Vector }
         *     
         */
        public Vector getVector() {
            return vector;
        }

        /**
         * Sets the value of the vector property.
         * 
         * @param value
         *     allowed object is
         *     {@link Vector }
         *     
         */
        public void setVector(Vector value) {
            this.vector = value;
        }

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
     *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}vector"/>
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
        "vector"
    })
    public static class TitlesOfParts {

        @XmlElement(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", required = true)
        protected Vector vector;

        /**
         * 
         * 										Vector
         * 
         * @return
         *     possible object is
         *     {@link Vector }
         *     
         */
        public Vector getVector() {
            return vector;
        }

        /**
         * Sets the value of the vector property.
         * 
         * @param value
         *     allowed object is
         *     {@link Vector }
         *     
         */
        public void setVector(Vector value) {
            this.vector = value;
        }

    }

}
