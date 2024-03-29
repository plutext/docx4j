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

package org.docx4j.docProps.variantTypes;

import java.math.BigDecimal;
import java.math.BigInteger;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for CT_Variant complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Variant">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}variant"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}vector"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}array"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}blob"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}oblob"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}empty"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}null"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}i1"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}i2"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}i4"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}i8"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}int"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}ui1"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}ui2"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}ui4"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}ui8"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}uint"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}r4"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}r8"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}decimal"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}lpstr"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}lpwstr"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}bstr"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}date"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}filetime"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}bool"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}cy"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}error"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}stream"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}ostream"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}storage"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}ostorage"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}vstream"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}clsid"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}cf"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Variant", propOrder = {
    "variant",
    "vector",
    "array",
    "blob",
    "oblob",
    "empty",
    "_null",
    "i1",
    "i2",
    "i4",
    "i8",
    "_int",
    "ui1",
    "ui2",
    "ui4",
    "ui8",
    "uint",
    "r4",
    "r8",
    "decimal",
    "lpstr",
    "lpwstr",
    "bstr",
    "date",
    "filetime",
    "bool",
    "cy",
    "error",
    "stream",
    "ostream",
    "storage",
    "ostorage",
    "vstream",
    "clsid",
    "cf"
})
public class Variant {

    protected Variant variant;
    protected Vector vector;
    protected Array array;
    protected byte[] blob;
    protected byte[] oblob;
    protected Empty empty;
    @XmlElement(name = "null")
    protected Null _null;
    protected Byte i1;
    protected Short i2;
    protected Integer i4;
    protected Long i8;
    @XmlElement(name = "int")
    protected Integer _int;
    @XmlSchemaType(name = "unsignedByte")
    protected Short ui1;
    @XmlSchemaType(name = "unsignedShort")
    protected Integer ui2;
    @XmlSchemaType(name = "unsignedInt")
    protected Long ui4;
    @XmlSchemaType(name = "unsignedLong")
    protected BigInteger ui8;
    @XmlSchemaType(name = "unsignedInt")
    protected Long uint;
    protected Float r4;
    protected Double r8;
    protected BigDecimal decimal;
    protected String lpstr;
    protected String lpwstr;
    protected String bstr;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar date;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar filetime;
    protected Boolean bool;
    protected String cy;
    protected String error;
    protected byte[] stream;
    protected byte[] ostream;
    protected byte[] storage;
    protected byte[] ostorage;
    protected Vstream vstream;
    protected String clsid;
    protected Cf cf;

    /**
     * Variant
     * 
     * @return
     *     possible object is
     *     {@link Variant }
     *     
     */
    public Variant getVariant() {
        return variant;
    }

    /**
     * Sets the value of the variant property.
     * 
     * @param value
     *     allowed object is
     *     {@link Variant }
     *     
     */
    public void setVariant(Variant value) {
        this.variant = value;
    }

    /**
     * Vector
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

    /**
     * Array
     * 
     * @return
     *     possible object is
     *     {@link Array }
     *     
     */
    public Array getArray() {
        return array;
    }

    /**
     * Sets the value of the array property.
     * 
     * @param value
     *     allowed object is
     *     {@link Array }
     *     
     */
    public void setArray(Array value) {
        this.array = value;
    }

    /**
     * Binary Blob
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

    /**
     * Binary Blob Object
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getOblob() {
        return oblob;
    }

    /**
     * Sets the value of the oblob property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setOblob(byte[] value) {
        this.oblob = ((byte[]) value);
    }

    /**
     * Empty
     * 
     * @return
     *     possible object is
     *     {@link Empty }
     *     
     */
    public Empty getEmpty() {
        return empty;
    }

    /**
     * Sets the value of the empty property.
     * 
     * @param value
     *     allowed object is
     *     {@link Empty }
     *     
     */
    public void setEmpty(Empty value) {
        this.empty = value;
    }

    /**
     * Null
     * 
     * @return
     *     possible object is
     *     {@link Null }
     *     
     */
    public Null getNull() {
        return _null;
    }

    /**
     * Sets the value of the null property.
     * 
     * @param value
     *     allowed object is
     *     {@link Null }
     *     
     */
    public void setNull(Null value) {
        this._null = value;
    }

    /**
     *  1-Byte Signed Integer
     * 
     * @return
     *     possible object is
     *     {@link Byte }
     *     
     */
    public Byte getI1() {
        return i1;
    }

    /**
     * Sets the value of the i1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link Byte }
     *     
     */
    public void setI1(Byte value) {
        this.i1 = value;
    }

    /**
     *  2-Byte Signed Integer
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public Short getI2() {
        return i2;
    }

    /**
     * Sets the value of the i2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setI2(Short value) {
        this.i2 = value;
    }

    /**
     *  4-Byte Signed Integer
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getI4() {
        return i4;
    }

    /**
     * Sets the value of the i4 property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setI4(Integer value) {
        this.i4 = value;
    }

    /**
     *  8-Byte Signed Integer
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getI8() {
        return i8;
    }

    /**
     * Sets the value of the i8 property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setI8(Long value) {
        this.i8 = value;
    }

    /**
     * Integer
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getInt() {
        return _int;
    }

    /**
     * Sets the value of the int property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setInt(Integer value) {
        this._int = value;
    }

    /**
     *  1-Byte Unsigned Integer
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public Short getUi1() {
        return ui1;
    }

    /**
     * Sets the value of the ui1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setUi1(Short value) {
        this.ui1 = value;
    }

    /**
     *  2-Byte Unsigned Integer
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getUi2() {
        return ui2;
    }

    /**
     * Sets the value of the ui2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setUi2(Integer value) {
        this.ui2 = value;
    }

    /**
     *  4-Byte Unsigned Integer
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getUi4() {
        return ui4;
    }

    /**
     * Sets the value of the ui4 property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setUi4(Long value) {
        this.ui4 = value;
    }

    /**
     *  8-Byte Unsigned Integer
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getUi8() {
        return ui8;
    }

    /**
     * Sets the value of the ui8 property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setUi8(BigInteger value) {
        this.ui8 = value;
    }

    /**
     * Unsigned Integer
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getUint() {
        return uint;
    }

    /**
     * Sets the value of the uint property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setUint(Long value) {
        this.uint = value;
    }

    /**
     *  4-Byte Real Number
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getR4() {
        return r4;
    }

    /**
     * Sets the value of the r4 property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setR4(Float value) {
        this.r4 = value;
    }

    /**
     *  8-Byte Real Number
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getR8() {
        return r8;
    }

    /**
     * Sets the value of the r8 property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setR8(Double value) {
        this.r8 = value;
    }

    /**
     * Decimal
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getDecimal() {
        return decimal;
    }

    /**
     * Sets the value of the decimal property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setDecimal(BigDecimal value) {
        this.decimal = value;
    }

    /**
     * LPSTR
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLpstr() {
        return lpstr;
    }

    /**
     * Sets the value of the lpstr property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLpstr(String value) {
        this.lpstr = value;
    }

    /**
     * LPWSTR
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLpwstr() {
        return lpwstr;
    }

    /**
     * Sets the value of the lpwstr property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLpwstr(String value) {
        this.lpwstr = value;
    }

    /**
     * Basic String
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBstr() {
        return bstr;
    }

    /**
     * Sets the value of the bstr property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBstr(String value) {
        this.bstr = value;
    }

    /**
     * Date and Time
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDate() {
        return date;
    }

    /**
     * Sets the value of the date property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDate(XMLGregorianCalendar value) {
        this.date = value;
    }

    /**
     * File Time
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getFiletime() {
        return filetime;
    }

    /**
     * Sets the value of the filetime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setFiletime(XMLGregorianCalendar value) {
        this.filetime = value;
    }

    /**
     * Boolean
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isBool() {
        return bool;
    }

    /**
     * Sets the value of the bool property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setBool(Boolean value) {
        this.bool = value;
    }

    /**
     * Currency
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCy() {
        return cy;
    }

    /**
     * Sets the value of the cy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCy(String value) {
        this.cy = value;
    }

    /**
     * Error Status Code
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getError() {
        return error;
    }

    /**
     * Sets the value of the error property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setError(String value) {
        this.error = value;
    }

    /**
     * Binary Stream
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getStream() {
        return stream;
    }

    /**
     * Sets the value of the stream property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setStream(byte[] value) {
        this.stream = ((byte[]) value);
    }

    /**
     * Binary Stream Object
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getOstream() {
        return ostream;
    }

    /**
     * Sets the value of the ostream property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setOstream(byte[] value) {
        this.ostream = ((byte[]) value);
    }

    /**
     * Binary Storage
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getStorage() {
        return storage;
    }

    /**
     * Sets the value of the storage property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setStorage(byte[] value) {
        this.storage = ((byte[]) value);
    }

    /**
     * Binary Storage Object
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getOstorage() {
        return ostorage;
    }

    /**
     * Sets the value of the ostorage property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setOstorage(byte[] value) {
        this.ostorage = ((byte[]) value);
    }

    /**
     * Binary Versioned Stream
     * 
     * @return
     *     possible object is
     *     {@link Vstream }
     *     
     */
    public Vstream getVstream() {
        return vstream;
    }

    /**
     * Sets the value of the vstream property.
     * 
     * @param value
     *     allowed object is
     *     {@link Vstream }
     *     
     */
    public void setVstream(Vstream value) {
        this.vstream = value;
    }

    /**
     * Class ID
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClsid() {
        return clsid;
    }

    /**
     * Sets the value of the clsid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClsid(String value) {
        this.clsid = value;
    }

    /**
     * Clipboard Data
     * 
     * @return
     *     possible object is
     *     {@link Cf }
     *     
     */
    public Cf getCf() {
        return cf;
    }

    /**
     * Sets the value of the cf property.
     * 
     * @param value
     *     allowed object is
     *     {@link Cf }
     *     
     */
    public void setCf(Cf value) {
        this.cf = value;
    }

}
