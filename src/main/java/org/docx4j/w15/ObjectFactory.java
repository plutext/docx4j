/*
 *  Copyright 2013, Plutext Pty Ltd.
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


package org.docx4j.w15; 

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTColor;
import org.docx4j.wml.CTDataBinding;
import org.docx4j.wml.CTDecimalNumber;
import org.docx4j.wml.CTEmpty;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.w15 package. 
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

    private final static QName _RepeatingSectionItem_QNAME = new QName("http://schemas.microsoft.com/office/word/2012/wordml", "repeatingSectionItem");
    private final static QName _Color_QNAME = new QName("http://schemas.microsoft.com/office/word/2012/wordml", "color");
    private final static QName _CommentsEx_QNAME = new QName("http://schemas.microsoft.com/office/word/2012/wordml", "commentsEx");
    private final static QName _RepeatingSection_QNAME = new QName("http://schemas.microsoft.com/office/word/2012/wordml", "repeatingSection");
    private final static QName _People_QNAME = new QName("http://schemas.microsoft.com/office/word/2012/wordml", "people");
    private final static QName _Appearance_QNAME = new QName("http://schemas.microsoft.com/office/word/2012/wordml", "appearance");
    private final static QName _DocId_QNAME = new QName("http://schemas.microsoft.com/office/word/2012/wordml", "docId");
    private final static QName _WebExtensionCreated_QNAME = new QName("http://schemas.microsoft.com/office/word/2012/wordml", "webExtensionCreated");
    private final static QName _ChartTrackingRefBased_QNAME = new QName("http://schemas.microsoft.com/office/word/2012/wordml", "chartTrackingRefBased");
    private final static QName _FootnoteColumns_QNAME = new QName("http://schemas.microsoft.com/office/word/2012/wordml", "footnoteColumns");
    private final static QName _DataBinding_QNAME = new QName("http://schemas.microsoft.com/office/word/2012/wordml", "dataBinding");
    private final static QName _Collapsed_QNAME = new QName("http://schemas.microsoft.com/office/word/2012/wordml", "collapsed");
    private final static QName _WebExtensionLinked_QNAME = new QName("http://schemas.microsoft.com/office/word/2012/wordml", "webExtensionLinked");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.w15
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTSdtAppearance }
     * 
     */
    public CTSdtAppearance createCTSdtAppearance() {
        return new CTSdtAppearance();
    }

    /**
     * Create an instance of {@link CTCommentsEx }
     * 
     */
    public CTCommentsEx createCTCommentsEx() {
        return new CTCommentsEx();
    }

    /**
     * Create an instance of {@link CTPeople }
     * 
     */
    public CTPeople createCTPeople() {
        return new CTPeople();
    }

    /**
     * Create an instance of {@link CTSdtRepeatedSection }
     * 
     */
    public CTSdtRepeatedSection createCTSdtRepeatedSection() {
        return new CTSdtRepeatedSection();
    }

    /**
     * Create an instance of {@link CTGuid }
     * 
     */
    public CTGuid createCTGuid() {
        return new CTGuid();
    }

    /**
     * Create an instance of {@link CTPresenceInfo }
     * 
     */
    public CTPresenceInfo createCTPresenceInfo() {
        return new CTPresenceInfo();
    }

    /**
     * Create an instance of {@link CTPerson }
     * 
     */
    public CTPerson createCTPerson() {
        return new CTPerson();
    }

    /**
     * Create an instance of {@link CTCommentEx }
     * 
     */
    public CTCommentEx createCTCommentEx() {
        return new CTCommentEx();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTEmpty }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2012/wordml", name = "repeatingSectionItem")
    public JAXBElement<CTEmpty> createRepeatingSectionItem(CTEmpty value) {
        return new JAXBElement<CTEmpty>(_RepeatingSectionItem_QNAME, CTEmpty.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTColor }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2012/wordml", name = "color")
    public JAXBElement<CTColor> createColor(CTColor value) {
        return new JAXBElement<CTColor>(_Color_QNAME, CTColor.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTCommentsEx }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2012/wordml", name = "commentsEx")
    public JAXBElement<CTCommentsEx> createCommentsEx(CTCommentsEx value) {
        return new JAXBElement<CTCommentsEx>(_CommentsEx_QNAME, CTCommentsEx.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTSdtRepeatedSection }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2012/wordml", name = "repeatingSection")
    public JAXBElement<CTSdtRepeatedSection> createRepeatingSection(CTSdtRepeatedSection value) {
        return new JAXBElement<CTSdtRepeatedSection>(_RepeatingSection_QNAME, CTSdtRepeatedSection.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPeople }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2012/wordml", name = "people")
    public JAXBElement<CTPeople> createPeople(CTPeople value) {
        return new JAXBElement<CTPeople>(_People_QNAME, CTPeople.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTSdtAppearance }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2012/wordml", name = "appearance")
    public JAXBElement<CTSdtAppearance> createAppearance(CTSdtAppearance value) {
        return new JAXBElement<CTSdtAppearance>(_Appearance_QNAME, CTSdtAppearance.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTGuid }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2012/wordml", name = "docId")
    public JAXBElement<CTGuid> createDocId(CTGuid value) {
        return new JAXBElement<CTGuid>(_DocId_QNAME, CTGuid.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BooleanDefaultTrue }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2012/wordml", name = "webExtensionCreated")
    public JAXBElement<BooleanDefaultTrue> createWebExtensionCreated(BooleanDefaultTrue value) {
        return new JAXBElement<BooleanDefaultTrue>(_WebExtensionCreated_QNAME, BooleanDefaultTrue.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BooleanDefaultTrue }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2012/wordml", name = "chartTrackingRefBased")
    public JAXBElement<BooleanDefaultTrue> createChartTrackingRefBased(BooleanDefaultTrue value) {
        return new JAXBElement<BooleanDefaultTrue>(_ChartTrackingRefBased_QNAME, BooleanDefaultTrue.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTDecimalNumber }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2012/wordml", name = "footnoteColumns")
    public JAXBElement<CTDecimalNumber> createFootnoteColumns(CTDecimalNumber value) {
        return new JAXBElement<CTDecimalNumber>(_FootnoteColumns_QNAME, CTDecimalNumber.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTDataBinding }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2012/wordml", name = "dataBinding")
    public JAXBElement<CTDataBinding> createDataBinding(CTDataBinding value) {
        return new JAXBElement<CTDataBinding>(_DataBinding_QNAME, CTDataBinding.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BooleanDefaultTrue }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2012/wordml", name = "collapsed")
    public JAXBElement<BooleanDefaultTrue> createCollapsed(BooleanDefaultTrue value) {
        return new JAXBElement<BooleanDefaultTrue>(_Collapsed_QNAME, BooleanDefaultTrue.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BooleanDefaultTrue }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2012/wordml", name = "webExtensionLinked")
    public JAXBElement<BooleanDefaultTrue> createWebExtensionLinked(BooleanDefaultTrue value) {
        return new JAXBElement<BooleanDefaultTrue>(_WebExtensionLinked_QNAME, BooleanDefaultTrue.class, null, value);
    }

}
