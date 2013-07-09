/*
 *  Copyright 2007-2013, Plutext Pty Ltd.
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


package org.docx4j.wml; 

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_DocPartGallery.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_DocPartGallery">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="placeholder"/>
 *     &lt;enumeration value="any"/>
 *     &lt;enumeration value="default"/>
 *     &lt;enumeration value="docParts"/>
 *     &lt;enumeration value="coverPg"/>
 *     &lt;enumeration value="eq"/>
 *     &lt;enumeration value="ftrs"/>
 *     &lt;enumeration value="hdrs"/>
 *     &lt;enumeration value="pgNum"/>
 *     &lt;enumeration value="tbls"/>
 *     &lt;enumeration value="watermarks"/>
 *     &lt;enumeration value="autoTxt"/>
 *     &lt;enumeration value="txtBox"/>
 *     &lt;enumeration value="pgNumT"/>
 *     &lt;enumeration value="pgNumB"/>
 *     &lt;enumeration value="pgNumMargins"/>
 *     &lt;enumeration value="tblOfContents"/>
 *     &lt;enumeration value="bib"/>
 *     &lt;enumeration value="custQuickParts"/>
 *     &lt;enumeration value="custCoverPg"/>
 *     &lt;enumeration value="custEq"/>
 *     &lt;enumeration value="custFtrs"/>
 *     &lt;enumeration value="custHdrs"/>
 *     &lt;enumeration value="custPgNum"/>
 *     &lt;enumeration value="custTbls"/>
 *     &lt;enumeration value="custWatermarks"/>
 *     &lt;enumeration value="custAutoTxt"/>
 *     &lt;enumeration value="custTxtBox"/>
 *     &lt;enumeration value="custPgNumT"/>
 *     &lt;enumeration value="custPgNumB"/>
 *     &lt;enumeration value="custPgNumMargins"/>
 *     &lt;enumeration value="custTblOfContents"/>
 *     &lt;enumeration value="custBib"/>
 *     &lt;enumeration value="custom1"/>
 *     &lt;enumeration value="custom2"/>
 *     &lt;enumeration value="custom3"/>
 *     &lt;enumeration value="custom4"/>
 *     &lt;enumeration value="custom5"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_DocPartGallery")
@XmlEnum
public enum STDocPartGallery {


    /**
     * Structured Document Tag Placeholder Text
     * 						Gallery
     * 
     */
    @XmlEnumValue("placeholder")
    PLACEHOLDER("placeholder"),

    /**
     * All Galleries
     * 
     */
    @XmlEnumValue("any")
    ANY("any"),

    /**
     * No Gallery
     * 						Classification
     * 
     */
    @XmlEnumValue("default")
    DEFAULT("default"),

    /**
     * Document Parts Gallery
     * 
     */
    @XmlEnumValue("docParts")
    DOC_PARTS("docParts"),

    /**
     * Cover Page Gallery
     * 
     */
    @XmlEnumValue("coverPg")
    COVER_PG("coverPg"),

    /**
     * Equations Gallery
     * 
     */
    @XmlEnumValue("eq")
    EQ("eq"),

    /**
     * Footers Gallery
     * 
     */
    @XmlEnumValue("ftrs")
    FTRS("ftrs"),

    /**
     * Headers Gallery
     * 
     */
    @XmlEnumValue("hdrs")
    HDRS("hdrs"),

    /**
     * Page Numbers Gallery
     * 
     */
    @XmlEnumValue("pgNum")
    PG_NUM("pgNum"),

    /**
     * Table Gallery
     * 
     */
    @XmlEnumValue("tbls")
    TBLS("tbls"),

    /**
     * Watermark Gallery
     * 
     */
    @XmlEnumValue("watermarks")
    WATERMARKS("watermarks"),

    /**
     * AutoText Gallery
     * 
     */
    @XmlEnumValue("autoTxt")
    AUTO_TXT("autoTxt"),

    /**
     * Text Box Gallery
     * 
     */
    @XmlEnumValue("txtBox")
    TXT_BOX("txtBox"),

    /**
     * Page Numbers At Top
     * 						Gallery
     * 
     */
    @XmlEnumValue("pgNumT")
    PG_NUM_T("pgNumT"),

    /**
     * Page Numbers At Bottom
     * 						Gallery
     * 
     */
    @XmlEnumValue("pgNumB")
    PG_NUM_B("pgNumB"),

    /**
     * Page Numbers At Margins
     * 						Gallery
     * 
     */
    @XmlEnumValue("pgNumMargins")
    PG_NUM_MARGINS("pgNumMargins"),

    /**
     * Table of Contents
     * 						Gallery
     * 
     */
    @XmlEnumValue("tblOfContents")
    TBL_OF_CONTENTS("tblOfContents"),

    /**
     * Bibliography Gallery
     * 
     */
    @XmlEnumValue("bib")
    BIB("bib"),

    /**
     * Custom Quick Parts
     * 						Gallery
     * 
     */
    @XmlEnumValue("custQuickParts")
    CUST_QUICK_PARTS("custQuickParts"),

    /**
     * Custom Cover Page
     * 						Gallery
     * 
     */
    @XmlEnumValue("custCoverPg")
    CUST_COVER_PG("custCoverPg"),

    /**
     * Custom Equation Gallery
     * 
     */
    @XmlEnumValue("custEq")
    CUST_EQ("custEq"),

    /**
     * Custom Footer Gallery
     * 
     */
    @XmlEnumValue("custFtrs")
    CUST_FTRS("custFtrs"),

    /**
     * Custom Header Gallery
     * 
     */
    @XmlEnumValue("custHdrs")
    CUST_HDRS("custHdrs"),

    /**
     * Custom Page Number
     * 						Gallery
     * 
     */
    @XmlEnumValue("custPgNum")
    CUST_PG_NUM("custPgNum"),

    /**
     * Custom Table Gallery
     * 
     */
    @XmlEnumValue("custTbls")
    CUST_TBLS("custTbls"),

    /**
     * Custom Watermark
     * 						Gallery
     * 
     */
    @XmlEnumValue("custWatermarks")
    CUST_WATERMARKS("custWatermarks"),

    /**
     * Custom AutoText Gallery
     * 
     */
    @XmlEnumValue("custAutoTxt")
    CUST_AUTO_TXT("custAutoTxt"),

    /**
     * Custom Text Box Gallery
     * 
     */
    @XmlEnumValue("custTxtBox")
    CUST_TXT_BOX("custTxtBox"),

    /**
     * Custom Page Number At Top
     * 						Gallery
     * 
     */
    @XmlEnumValue("custPgNumT")
    CUST_PG_NUM_T("custPgNumT"),

    /**
     * Custom Page Number At Bottom
     * 						Gallery
     * 
     */
    @XmlEnumValue("custPgNumB")
    CUST_PG_NUM_B("custPgNumB"),

    /**
     * Custom Page Number At Margins
     * 						Gallery
     * 
     */
    @XmlEnumValue("custPgNumMargins")
    CUST_PG_NUM_MARGINS("custPgNumMargins"),

    /**
     * Custom Table of Contents
     * 						Gallery
     * 
     */
    @XmlEnumValue("custTblOfContents")
    CUST_TBL_OF_CONTENTS("custTblOfContents"),

    /**
     * Custom Bibliography
     * 						Gallery
     * 
     */
    @XmlEnumValue("custBib")
    CUST_BIB("custBib"),

    /**
     * Custom 1 Gallery
     * 
     */
    @XmlEnumValue("custom1")
    CUSTOM_1("custom1"),

    /**
     * Custom 2 Gallery
     * 
     */
    @XmlEnumValue("custom2")
    CUSTOM_2("custom2"),

    /**
     * Custom 3 Gallery
     * 
     */
    @XmlEnumValue("custom3")
    CUSTOM_3("custom3"),

    /**
     * Custom 4 Gallery
     * 
     */
    @XmlEnumValue("custom4")
    CUSTOM_4("custom4"),

    /**
     * Custom 5 Gallery
     * 
     */
    @XmlEnumValue("custom5")
    CUSTOM_5("custom5");
    private final String value;

    STDocPartGallery(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STDocPartGallery fromValue(String v) {
        for (STDocPartGallery c: STDocPartGallery.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
