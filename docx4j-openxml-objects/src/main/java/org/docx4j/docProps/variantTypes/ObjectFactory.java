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
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.docProps.variantTypes package. 
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

    private final static QName _Ostream_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", "ostream");
    private final static QName _Date_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", "date");
    private final static QName _Variant_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", "variant");
    private final static QName _Ui2_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", "ui2");
    private final static QName _Ui1_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", "ui1");
    private final static QName _Lpstr_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", "lpstr");
    private final static QName _Oblob_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", "oblob");
    private final static QName _Stream_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", "stream");
    private final static QName _Null_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", "null");
    private final static QName _Empty_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", "empty");
    private final static QName _Vector_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", "vector");
    private final static QName _Ostorage_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", "ostorage");
    private final static QName _Decimal_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", "decimal");
    private final static QName _Cy_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", "cy");
    private final static QName _Int_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", "int");
    private final static QName _Error_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", "error");
    private final static QName _Lpwstr_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", "lpwstr");
    private final static QName _R4_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", "r4");
    private final static QName _Uint_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", "uint");
    private final static QName _I2_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", "i2");
    private final static QName _Clsid_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", "clsid");
    private final static QName _Bstr_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", "bstr");
    private final static QName _Array_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", "array");
    private final static QName _I1_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", "i1");
    private final static QName _Storage_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", "storage");
    private final static QName _I4_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", "i4");
    private final static QName _Cf_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", "cf");
    private final static QName _Vstream_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", "vstream");
    private final static QName _R8_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", "r8");
    private final static QName _Bool_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", "bool");
    private final static QName _I8_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", "i8");
    private final static QName _Ui4_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", "ui4");
    private final static QName _Filetime_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", "filetime");
    private final static QName _Ui8_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", "ui8");
    private final static QName _Blob_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", "blob");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.docProps.variantTypes
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Vstream }
     * 
     */
    public Vstream createVstream() {
        return new Vstream();
    }

    /**
     * Create an instance of {@link Variant }
     * 
     */
    public Variant createVariant() {
        return new Variant();
    }

    /**
     * Create an instance of {@link Array }
     * 
     */
    public Array createArray() {
        return new Array();
    }

    /**
     * Create an instance of {@link Vector }
     * 
     */
    public Vector createVector() {
        return new Vector();
    }

    /**
     * Create an instance of {@link Empty }
     * 
     */
    public Empty createEmpty() {
        return new Empty();
    }

    /**
     * Create an instance of {@link Cf }
     * 
     */
    public Cf createCf() {
        return new Cf();
    }

    /**
     * Create an instance of {@link Null }
     * 
     */
    public Null createNull() {
        return new Null();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link byte[]}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", name = "ostream")
    public JAXBElement<byte[]> createOstream(byte[] value) {
        return new JAXBElement<byte[]>(_Ostream_QNAME, byte[].class, null, ((byte[]) value));
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", name = "date")
    public JAXBElement<XMLGregorianCalendar> createDate(XMLGregorianCalendar value) {
        return new JAXBElement<XMLGregorianCalendar>(_Date_QNAME, XMLGregorianCalendar.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Variant }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", name = "variant")
    public JAXBElement<Variant> createVariant(Variant value) {
        return new JAXBElement<Variant>(_Variant_QNAME, Variant.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", name = "ui2")
    public JAXBElement<Integer> createUi2(Integer value) {
        return new JAXBElement<Integer>(_Ui2_QNAME, Integer.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Short }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", name = "ui1")
    public JAXBElement<Short> createUi1(Short value) {
        return new JAXBElement<Short>(_Ui1_QNAME, Short.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", name = "lpstr")
    public JAXBElement<String> createLpstr(String value) {
        return new JAXBElement<String>(_Lpstr_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link byte[]}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", name = "oblob")
    public JAXBElement<byte[]> createOblob(byte[] value) {
        return new JAXBElement<byte[]>(_Oblob_QNAME, byte[].class, null, ((byte[]) value));
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link byte[]}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", name = "stream")
    public JAXBElement<byte[]> createStream(byte[] value) {
        return new JAXBElement<byte[]>(_Stream_QNAME, byte[].class, null, ((byte[]) value));
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Null }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", name = "null")
    public JAXBElement<Null> createNull(Null value) {
        return new JAXBElement<Null>(_Null_QNAME, Null.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Empty }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", name = "empty")
    public JAXBElement<Empty> createEmpty(Empty value) {
        return new JAXBElement<Empty>(_Empty_QNAME, Empty.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Vector }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", name = "vector")
    public JAXBElement<Vector> createVector(Vector value) {
        return new JAXBElement<Vector>(_Vector_QNAME, Vector.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link byte[]}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", name = "ostorage")
    public JAXBElement<byte[]> createOstorage(byte[] value) {
        return new JAXBElement<byte[]>(_Ostorage_QNAME, byte[].class, null, ((byte[]) value));
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigDecimal }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", name = "decimal")
    public JAXBElement<BigDecimal> createDecimal(BigDecimal value) {
        return new JAXBElement<BigDecimal>(_Decimal_QNAME, BigDecimal.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", name = "cy")
    public JAXBElement<String> createCy(String value) {
        return new JAXBElement<String>(_Cy_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", name = "int")
    public JAXBElement<Integer> createInt(Integer value) {
        return new JAXBElement<Integer>(_Int_QNAME, Integer.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", name = "error")
    public JAXBElement<String> createError(String value) {
        return new JAXBElement<String>(_Error_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", name = "lpwstr")
    public JAXBElement<String> createLpwstr(String value) {
        return new JAXBElement<String>(_Lpwstr_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Float }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", name = "r4")
    public JAXBElement<Float> createR4(Float value) {
        return new JAXBElement<Float>(_R4_QNAME, Float.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Long }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", name = "uint")
    public JAXBElement<Long> createUint(Long value) {
        return new JAXBElement<Long>(_Uint_QNAME, Long.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Short }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", name = "i2")
    public JAXBElement<Short> createI2(Short value) {
        return new JAXBElement<Short>(_I2_QNAME, Short.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", name = "clsid")
    public JAXBElement<String> createClsid(String value) {
        return new JAXBElement<String>(_Clsid_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", name = "bstr")
    public JAXBElement<String> createBstr(String value) {
        return new JAXBElement<String>(_Bstr_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Array }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", name = "array")
    public JAXBElement<Array> createArray(Array value) {
        return new JAXBElement<Array>(_Array_QNAME, Array.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Byte }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", name = "i1")
    public JAXBElement<Byte> createI1(Byte value) {
        return new JAXBElement<Byte>(_I1_QNAME, Byte.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link byte[]}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", name = "storage")
    public JAXBElement<byte[]> createStorage(byte[] value) {
        return new JAXBElement<byte[]>(_Storage_QNAME, byte[].class, null, ((byte[]) value));
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", name = "i4")
    public JAXBElement<Integer> createI4(Integer value) {
        return new JAXBElement<Integer>(_I4_QNAME, Integer.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Cf }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", name = "cf")
    public JAXBElement<Cf> createCf(Cf value) {
        return new JAXBElement<Cf>(_Cf_QNAME, Cf.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Vstream }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", name = "vstream")
    public JAXBElement<Vstream> createVstream(Vstream value) {
        return new JAXBElement<Vstream>(_Vstream_QNAME, Vstream.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Double }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", name = "r8")
    public JAXBElement<Double> createR8(Double value) {
        return new JAXBElement<Double>(_R8_QNAME, Double.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", name = "bool")
    public JAXBElement<Boolean> createBool(Boolean value) {
        return new JAXBElement<Boolean>(_Bool_QNAME, Boolean.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Long }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", name = "i8")
    public JAXBElement<Long> createI8(Long value) {
        return new JAXBElement<Long>(_I8_QNAME, Long.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Long }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", name = "ui4")
    public JAXBElement<Long> createUi4(Long value) {
        return new JAXBElement<Long>(_Ui4_QNAME, Long.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", name = "filetime")
    public JAXBElement<XMLGregorianCalendar> createFiletime(XMLGregorianCalendar value) {
        return new JAXBElement<XMLGregorianCalendar>(_Filetime_QNAME, XMLGregorianCalendar.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", name = "ui8")
    public JAXBElement<BigInteger> createUi8(BigInteger value) {
        return new JAXBElement<BigInteger>(_Ui8_QNAME, BigInteger.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link byte[]}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", name = "blob")
    public JAXBElement<byte[]> createBlob(byte[] value) {
        return new JAXBElement<byte[]>(_Blob_QNAME, byte[].class, null, ((byte[]) value));
    }

}
