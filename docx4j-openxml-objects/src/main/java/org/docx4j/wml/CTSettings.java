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


package org.docx4j.wml;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.docx4j.customxml.SchemaLibrary;
import org.docx4j.math.CTMathPr;
import org.docx4j.w14.CTDefaultImageDpi;
import org.docx4j.w14.CTLongHexNumber;
import org.docx4j.w14.CTOnOff;
import org.docx4j.w15.CTGuid;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Settings complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Settings">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="writeProtection" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_WriteProtection" minOccurs="0"/>
 *         &lt;element name="view" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_View" minOccurs="0"/>
 *         &lt;element name="zoom" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Zoom" minOccurs="0"/>
 *         &lt;element name="removePersonalInformation" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="removeDateAndTime" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="doNotDisplayPageBoundaries" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="displayBackgroundShape" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="printPostScriptOverText" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="printFractionalCharacterWidth" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="printFormsData" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="embedTrueTypeFonts" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="embedSystemFonts" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="saveSubsetFonts" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="saveFormsData" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="mirrorMargins" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="alignBordersAndEdges" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="bordersDoNotSurroundHeader" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="bordersDoNotSurroundFooter" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="gutterAtTop" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="hideSpellingErrors" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="hideGrammaticalErrors" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="activeWritingStyle" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_WritingStyle" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="proofState" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Proof" minOccurs="0"/>
 *         &lt;element name="formsDesign" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="attachedTemplate" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Rel" minOccurs="0"/>
 *         &lt;element name="linkStyles" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="stylePaneFormatFilter" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_ShortHexNumber" minOccurs="0"/>
 *         &lt;element name="stylePaneSortMethod" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_ShortHexNumber" minOccurs="0"/>
 *         &lt;element name="documentType" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_DocType" minOccurs="0"/>
 *         &lt;element name="mailMerge" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_MailMerge" minOccurs="0"/>
 *         &lt;element name="revisionView" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_TrackChangesView" minOccurs="0"/>
 *         &lt;element name="trackRevisions" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="doNotTrackMoves" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="doNotTrackFormatting" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="documentProtection" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_DocProtect" minOccurs="0"/>
 *         &lt;element name="autoFormatOverride" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="styleLockTheme" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="styleLockQFSet" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="defaultTabStop" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_TwipsMeasure" minOccurs="0"/>
 *         &lt;element name="autoHyphenation" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="consecutiveHyphenLimit" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="val" use="required">
 *                   &lt;simpleType>
 *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
 *                     &lt;/restriction>
 *                   &lt;/simpleType>
 *                 &lt;/attribute>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="hyphenationZone" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_TwipsMeasure" minOccurs="0"/>
 *         &lt;element name="doNotHyphenateCaps" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="showEnvelope" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="summaryLength" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="val" use="required">
 *                   &lt;simpleType>
 *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
 *                     &lt;/restriction>
 *                   &lt;/simpleType>
 *                 &lt;/attribute>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="clickAndTypeStyle" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="val" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="defaultTableStyle" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="val" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="evenAndOddHeaders" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="bookFoldRevPrinting" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="bookFoldPrinting" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="bookFoldPrintingSheets" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="val" use="required">
 *                   &lt;simpleType>
 *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
 *                     &lt;/restriction>
 *                   &lt;/simpleType>
 *                 &lt;/attribute>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="drawingGridHorizontalSpacing" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_TwipsMeasure" minOccurs="0"/>
 *         &lt;element name="drawingGridVerticalSpacing" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_TwipsMeasure" minOccurs="0"/>
 *         &lt;element name="displayHorizontalDrawingGridEvery" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="val" use="required">
 *                   &lt;simpleType>
 *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
 *                     &lt;/restriction>
 *                   &lt;/simpleType>
 *                 &lt;/attribute>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="displayVerticalDrawingGridEvery" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="val" use="required">
 *                   &lt;simpleType>
 *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
 *                     &lt;/restriction>
 *                   &lt;/simpleType>
 *                 &lt;/attribute>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="doNotUseMarginsForDrawingGridOrigin" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="drawingGridHorizontalOrigin" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_TwipsMeasure" minOccurs="0"/>
 *         &lt;element name="drawingGridVerticalOrigin" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_TwipsMeasure" minOccurs="0"/>
 *         &lt;element name="doNotShadeFormData" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="noPunctuationKerning" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="characterSpacingControl" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_CharacterSpacing" minOccurs="0"/>
 *         &lt;element name="printTwoOnOne" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="strictFirstAndLastChars" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="noLineBreaksAfter" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Kinsoku" minOccurs="0"/>
 *         &lt;element name="noLineBreaksBefore" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Kinsoku" minOccurs="0"/>
 *         &lt;element name="savePreviewPicture" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="doNotValidateAgainstSchema" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="saveInvalidXml" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="ignoreMixedContent" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="alwaysShowPlaceholderText" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="doNotDemarcateInvalidXml" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="saveXmlDataOnly" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="useXSLTWhenSaving" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="saveThroughXslt" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_SaveThroughXslt" minOccurs="0"/>
 *         &lt;element name="showXMLTags" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="alwaysMergeEmptyNamespace" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="updateFields" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="hdrShapeDefaults" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_ShapeDefaults" minOccurs="0"/>
 *         &lt;element name="footnotePr" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_FtnDocProps" minOccurs="0"/>
 *         &lt;element name="endnotePr" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_EdnDocProps" minOccurs="0"/>
 *         &lt;element name="compat" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Compat" minOccurs="0"/>
 *         &lt;element name="docVars" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_DocVars" minOccurs="0"/>
 *         &lt;element name="rsids" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_DocRsids" minOccurs="0"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/math}mathPr" minOccurs="0"/>
 *         &lt;element name="uiCompat97To2003" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="attachedSchema" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="val" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="themeFontLang" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Language" minOccurs="0"/>
 *         &lt;element name="clrSchemeMapping" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_ColorSchemeMapping" minOccurs="0"/>
 *         &lt;element name="doNotIncludeSubdocsInStats" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="doNotAutoCompressPictures" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="forceUpgrade" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="captions" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Captions" minOccurs="0"/>
 *         &lt;element name="readModeInkLockDown" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_ReadingModeInkLockDown" minOccurs="0"/>
 *         &lt;element name="smartTagType" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_SmartTagType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/schemaLibrary/2006/main}schemaLibrary" minOccurs="0"/>
 *         &lt;element name="shapeDefaults" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_ShapeDefaults" minOccurs="0"/>
 *         &lt;element name="doNotEmbedSmartTags" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="decimalSymbol" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="val" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="listSeparator" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="val" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element ref="{http://schemas.microsoft.com/office/word/2012/wordml}chartTrackingRefBased" minOccurs="0"/>
 *         &lt;element ref="{http://schemas.microsoft.com/office/word/2010/wordml}docId" minOccurs="0"/>
 *         &lt;element ref="{http://schemas.microsoft.com/office/word/2012/wordml}docId" minOccurs="0"/>
 *         &lt;element ref="{http://schemas.microsoft.com/office/word/2010/wordml}conflictMode" minOccurs="0"/>
 *         &lt;element ref="{http://schemas.microsoft.com/office/word/2010/wordml}discardImageEditingData" minOccurs="0"/>
 *         &lt;element ref="{http://schemas.microsoft.com/office/word/2010/wordml}defaultImageDpi" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute ref="{http://schemas.openxmlformats.org/markup-compatibility/2006}Ignorable"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Settings", propOrder = {
    "writeProtection",
    "view",
    "zoom",
    "removePersonalInformation",
    "removeDateAndTime",
    "doNotDisplayPageBoundaries",
    "displayBackgroundShape",
    "printPostScriptOverText",
    "printFractionalCharacterWidth",
    "printFormsData",
    "embedTrueTypeFonts",
    "embedSystemFonts",
    "saveSubsetFonts",
    "saveFormsData",
    "mirrorMargins",
    "alignBordersAndEdges",
    "bordersDoNotSurroundHeader",
    "bordersDoNotSurroundFooter",
    "gutterAtTop",
    "hideSpellingErrors",
    "hideGrammaticalErrors",
    "activeWritingStyle",
    "proofState",
    "formsDesign",
    "attachedTemplate",
    "linkStyles",
    "stylePaneFormatFilter",
    "stylePaneSortMethod",
    "documentType",
    "mailMerge",
    "revisionView",
    "trackRevisions",
    "doNotTrackMoves",
    "doNotTrackFormatting",
    "documentProtection",
    "autoFormatOverride",
    "styleLockTheme",
    "styleLockQFSet",
    "defaultTabStop",
    "autoHyphenation",
    "consecutiveHyphenLimit",
    "hyphenationZone",
    "doNotHyphenateCaps",
    "showEnvelope",
    "summaryLength",
    "clickAndTypeStyle",
    "defaultTableStyle",
    "evenAndOddHeaders",
    "bookFoldRevPrinting",
    "bookFoldPrinting",
    "bookFoldPrintingSheets",
    "drawingGridHorizontalSpacing",
    "drawingGridVerticalSpacing",
    "displayHorizontalDrawingGridEvery",
    "displayVerticalDrawingGridEvery",
    "doNotUseMarginsForDrawingGridOrigin",
    "drawingGridHorizontalOrigin",
    "drawingGridVerticalOrigin",
    "doNotShadeFormData",
    "noPunctuationKerning",
    "characterSpacingControl",
    "printTwoOnOne",
    "strictFirstAndLastChars",
    "noLineBreaksAfter",
    "noLineBreaksBefore",
    "savePreviewPicture",
    "doNotValidateAgainstSchema",
    "saveInvalidXml",
    "ignoreMixedContent",
    "alwaysShowPlaceholderText",
    "doNotDemarcateInvalidXml",
    "saveXmlDataOnly",
    "useXSLTWhenSaving",
    "saveThroughXslt",
    "showXMLTags",
    "alwaysMergeEmptyNamespace",
    "updateFields",
    "hdrShapeDefaults",
    "footnotePr",
    "endnotePr",
    "compat",
    "docVars",
    "rsids",
    "mathPr",
    "uiCompat97To2003",
    "attachedSchema",
    "themeFontLang",
    "clrSchemeMapping",
    "doNotIncludeSubdocsInStats",
    "doNotAutoCompressPictures",
    "forceUpgrade",
    "captions",
    "readModeInkLockDown",
    "smartTagType",
    "schemaLibrary",
    "shapeDefaults",
    "doNotEmbedSmartTags",
    "decimalSymbol",
    "listSeparator",
    "chartTrackingRefBased",
    "docId14",
    "docId15",
    "conflictMode",
    "discardImageEditingData",
    "defaultImageDpi"
})
@XmlRootElement(name = "settings")
public class CTSettings
    implements Child
{

    protected CTWriteProtection writeProtection;
    protected CTView view;
    protected CTZoom zoom;
    protected BooleanDefaultTrue removePersonalInformation;
    protected BooleanDefaultTrue removeDateAndTime;
    protected BooleanDefaultTrue doNotDisplayPageBoundaries;
    protected BooleanDefaultTrue displayBackgroundShape;
    protected BooleanDefaultTrue printPostScriptOverText;
    protected BooleanDefaultTrue printFractionalCharacterWidth;
    protected BooleanDefaultTrue printFormsData;
    protected BooleanDefaultTrue embedTrueTypeFonts;
    protected BooleanDefaultTrue embedSystemFonts;
    protected BooleanDefaultTrue saveSubsetFonts;
    protected BooleanDefaultTrue saveFormsData;
    protected BooleanDefaultTrue mirrorMargins;
    protected BooleanDefaultTrue alignBordersAndEdges;
    protected BooleanDefaultTrue bordersDoNotSurroundHeader;
    protected BooleanDefaultTrue bordersDoNotSurroundFooter;
    protected BooleanDefaultTrue gutterAtTop;
    protected BooleanDefaultTrue hideSpellingErrors;
    protected BooleanDefaultTrue hideGrammaticalErrors;
    protected List<CTWritingStyle> activeWritingStyle = new ArrayListWml<CTWritingStyle>(this);
    protected CTProof proofState;
    protected BooleanDefaultTrue formsDesign;
    protected CTRel attachedTemplate;
    protected BooleanDefaultTrue linkStyles;
    protected CTShortHexNumber stylePaneFormatFilter;
    protected CTShortHexNumber stylePaneSortMethod;
    protected CTDocType documentType;
    protected CTMailMerge mailMerge;
    protected CTTrackChangesView revisionView;
    protected BooleanDefaultTrue trackRevisions;
    protected BooleanDefaultTrue doNotTrackMoves;
    protected BooleanDefaultTrue doNotTrackFormatting;
    protected CTDocProtect documentProtection;
    protected BooleanDefaultTrue autoFormatOverride;
    protected BooleanDefaultTrue styleLockTheme;
    protected BooleanDefaultTrue styleLockQFSet;
    protected CTTwipsMeasure defaultTabStop;
    protected BooleanDefaultTrue autoHyphenation;
    protected CTSettings.ConsecutiveHyphenLimit consecutiveHyphenLimit;
    protected CTTwipsMeasure hyphenationZone;
    protected BooleanDefaultTrue doNotHyphenateCaps;
    protected BooleanDefaultTrue showEnvelope;
    protected CTSettings.SummaryLength summaryLength;
    protected CTSettings.ClickAndTypeStyle clickAndTypeStyle;
    protected CTSettings.DefaultTableStyle defaultTableStyle;
    protected BooleanDefaultTrue evenAndOddHeaders;
    protected BooleanDefaultTrue bookFoldRevPrinting;
    protected BooleanDefaultTrue bookFoldPrinting;
    protected CTSettings.BookFoldPrintingSheets bookFoldPrintingSheets;
    protected CTTwipsMeasure drawingGridHorizontalSpacing;
    protected CTTwipsMeasure drawingGridVerticalSpacing;
    protected CTSettings.DisplayHorizontalDrawingGridEvery displayHorizontalDrawingGridEvery;
    protected CTSettings.DisplayVerticalDrawingGridEvery displayVerticalDrawingGridEvery;
    protected BooleanDefaultTrue doNotUseMarginsForDrawingGridOrigin;
    protected CTTwipsMeasure drawingGridHorizontalOrigin;
    protected CTTwipsMeasure drawingGridVerticalOrigin;
    protected BooleanDefaultTrue doNotShadeFormData;
    protected BooleanDefaultTrue noPunctuationKerning;
    protected CTCharacterSpacing characterSpacingControl;
    protected BooleanDefaultTrue printTwoOnOne;
    protected BooleanDefaultTrue strictFirstAndLastChars;
    protected CTKinsoku noLineBreaksAfter;
    protected CTKinsoku noLineBreaksBefore;
    protected BooleanDefaultTrue savePreviewPicture;
    protected BooleanDefaultTrue doNotValidateAgainstSchema;
    protected BooleanDefaultTrue saveInvalidXml;
    protected BooleanDefaultTrue ignoreMixedContent;
    protected BooleanDefaultTrue alwaysShowPlaceholderText;
    protected BooleanDefaultTrue doNotDemarcateInvalidXml;
    protected BooleanDefaultTrue saveXmlDataOnly;
    protected BooleanDefaultTrue useXSLTWhenSaving;
    protected CTSaveThroughXslt saveThroughXslt;
    protected BooleanDefaultTrue showXMLTags;
    protected BooleanDefaultTrue alwaysMergeEmptyNamespace;
    protected BooleanDefaultTrue updateFields;
    protected CTShapeDefaults hdrShapeDefaults;
    protected CTFtnDocProps footnotePr;
    protected CTEdnDocProps endnotePr;
    protected CTCompat compat;
    protected CTDocVars docVars;
    protected CTDocRsids rsids;
    @XmlElement(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math")
    protected CTMathPr mathPr;
    protected BooleanDefaultTrue uiCompat97To2003;
    protected List<CTSettings.AttachedSchema> attachedSchema = new ArrayListWml<CTSettings.AttachedSchema>(this);
    protected CTLanguage themeFontLang;
    protected CTColorSchemeMapping clrSchemeMapping;
    protected BooleanDefaultTrue doNotIncludeSubdocsInStats;
    protected BooleanDefaultTrue doNotAutoCompressPictures;
    protected CTSettings.ForceUpgrade forceUpgrade;
    protected CTCaptions captions;
    protected CTReadingModeInkLockDown readModeInkLockDown;
    protected List<CTSmartTagType> smartTagType = new ArrayListWml<CTSmartTagType>(this);
    @XmlElement(namespace = "http://schemas.openxmlformats.org/schemaLibrary/2006/main")
    protected SchemaLibrary schemaLibrary;
    protected CTShapeDefaults shapeDefaults;
    protected BooleanDefaultTrue doNotEmbedSmartTags;
    protected CTSettings.DecimalSymbol decimalSymbol;
    protected CTSettings.ListSeparator listSeparator;
    @XmlElement(namespace = "http://schemas.microsoft.com/office/word/2012/wordml")
    protected BooleanDefaultTrue chartTrackingRefBased;
    @XmlElement(name = "docId", namespace = "http://schemas.microsoft.com/office/word/2010/wordml")
    protected CTLongHexNumber docId14;
    @XmlElement(name = "docId", namespace = "http://schemas.microsoft.com/office/word/2012/wordml")
    protected CTGuid docId15;
    @XmlElement(namespace = "http://schemas.microsoft.com/office/word/2010/wordml")
    protected CTOnOff conflictMode;
    @XmlElement(namespace = "http://schemas.microsoft.com/office/word/2010/wordml")
    protected CTOnOff discardImageEditingData;
    @XmlElement(namespace = "http://schemas.microsoft.com/office/word/2010/wordml")
    protected CTDefaultImageDpi defaultImageDpi;
    @XmlAttribute(name = "Ignorable", namespace = "http://schemas.openxmlformats.org/markup-compatibility/2006")
    protected String ignorable;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the writeProtection property.
     * 
     * @return
     *     possible object is
     *     {@link CTWriteProtection }
     *     
     */
    public CTWriteProtection getWriteProtection() {
        return writeProtection;
    }

    /**
     * Sets the value of the writeProtection property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTWriteProtection }
     *     
     */
    public void setWriteProtection(CTWriteProtection value) {
        this.writeProtection = value;
    }

    /**
     * Gets the value of the view property.
     * 
     * @return
     *     possible object is
     *     {@link CTView }
     *     
     */
    public CTView getView() {
        return view;
    }

    /**
     * Sets the value of the view property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTView }
     *     
     */
    public void setView(CTView value) {
        this.view = value;
    }

    /**
     * Gets the value of the zoom property.
     * 
     * @return
     *     possible object is
     *     {@link CTZoom }
     *     
     */
    public CTZoom getZoom() {
        return zoom;
    }

    /**
     * Sets the value of the zoom property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTZoom }
     *     
     */
    public void setZoom(CTZoom value) {
        this.zoom = value;
    }

    /**
     * Gets the value of the removePersonalInformation property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getRemovePersonalInformation() {
        return removePersonalInformation;
    }

    /**
     * Sets the value of the removePersonalInformation property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setRemovePersonalInformation(BooleanDefaultTrue value) {
        this.removePersonalInformation = value;
    }

    /**
     * Gets the value of the removeDateAndTime property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getRemoveDateAndTime() {
        return removeDateAndTime;
    }

    /**
     * Sets the value of the removeDateAndTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setRemoveDateAndTime(BooleanDefaultTrue value) {
        this.removeDateAndTime = value;
    }

    /**
     * Gets the value of the doNotDisplayPageBoundaries property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getDoNotDisplayPageBoundaries() {
        return doNotDisplayPageBoundaries;
    }

    /**
     * Sets the value of the doNotDisplayPageBoundaries property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setDoNotDisplayPageBoundaries(BooleanDefaultTrue value) {
        this.doNotDisplayPageBoundaries = value;
    }

    /**
     * Gets the value of the displayBackgroundShape property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getDisplayBackgroundShape() {
        return displayBackgroundShape;
    }

    /**
     * Sets the value of the displayBackgroundShape property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setDisplayBackgroundShape(BooleanDefaultTrue value) {
        this.displayBackgroundShape = value;
    }

    /**
     * Gets the value of the printPostScriptOverText property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getPrintPostScriptOverText() {
        return printPostScriptOverText;
    }

    /**
     * Sets the value of the printPostScriptOverText property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setPrintPostScriptOverText(BooleanDefaultTrue value) {
        this.printPostScriptOverText = value;
    }

    /**
     * Gets the value of the printFractionalCharacterWidth property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getPrintFractionalCharacterWidth() {
        return printFractionalCharacterWidth;
    }

    /**
     * Sets the value of the printFractionalCharacterWidth property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setPrintFractionalCharacterWidth(BooleanDefaultTrue value) {
        this.printFractionalCharacterWidth = value;
    }

    /**
     * Gets the value of the printFormsData property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getPrintFormsData() {
        return printFormsData;
    }

    /**
     * Sets the value of the printFormsData property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setPrintFormsData(BooleanDefaultTrue value) {
        this.printFormsData = value;
    }

    /**
     * Gets the value of the embedTrueTypeFonts property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getEmbedTrueTypeFonts() {
        return embedTrueTypeFonts;
    }

    /**
     * Sets the value of the embedTrueTypeFonts property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setEmbedTrueTypeFonts(BooleanDefaultTrue value) {
        this.embedTrueTypeFonts = value;
    }

    /**
     * Gets the value of the embedSystemFonts property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getEmbedSystemFonts() {
        return embedSystemFonts;
    }

    /**
     * Sets the value of the embedSystemFonts property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setEmbedSystemFonts(BooleanDefaultTrue value) {
        this.embedSystemFonts = value;
    }

    /**
     * Gets the value of the saveSubsetFonts property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getSaveSubsetFonts() {
        return saveSubsetFonts;
    }

    /**
     * Sets the value of the saveSubsetFonts property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setSaveSubsetFonts(BooleanDefaultTrue value) {
        this.saveSubsetFonts = value;
    }

    /**
     * Gets the value of the saveFormsData property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getSaveFormsData() {
        return saveFormsData;
    }

    /**
     * Sets the value of the saveFormsData property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setSaveFormsData(BooleanDefaultTrue value) {
        this.saveFormsData = value;
    }

    /**
     * Gets the value of the mirrorMargins property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getMirrorMargins() {
        return mirrorMargins;
    }

    /**
     * Sets the value of the mirrorMargins property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setMirrorMargins(BooleanDefaultTrue value) {
        this.mirrorMargins = value;
    }

    /**
     * Gets the value of the alignBordersAndEdges property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getAlignBordersAndEdges() {
        return alignBordersAndEdges;
    }

    /**
     * Sets the value of the alignBordersAndEdges property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setAlignBordersAndEdges(BooleanDefaultTrue value) {
        this.alignBordersAndEdges = value;
    }

    /**
     * Gets the value of the bordersDoNotSurroundHeader property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getBordersDoNotSurroundHeader() {
        return bordersDoNotSurroundHeader;
    }

    /**
     * Sets the value of the bordersDoNotSurroundHeader property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setBordersDoNotSurroundHeader(BooleanDefaultTrue value) {
        this.bordersDoNotSurroundHeader = value;
    }

    /**
     * Gets the value of the bordersDoNotSurroundFooter property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getBordersDoNotSurroundFooter() {
        return bordersDoNotSurroundFooter;
    }

    /**
     * Sets the value of the bordersDoNotSurroundFooter property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setBordersDoNotSurroundFooter(BooleanDefaultTrue value) {
        this.bordersDoNotSurroundFooter = value;
    }

    /**
     * Gets the value of the gutterAtTop property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getGutterAtTop() {
        return gutterAtTop;
    }

    /**
     * Sets the value of the gutterAtTop property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setGutterAtTop(BooleanDefaultTrue value) {
        this.gutterAtTop = value;
    }

    /**
     * Gets the value of the hideSpellingErrors property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getHideSpellingErrors() {
        return hideSpellingErrors;
    }

    /**
     * Sets the value of the hideSpellingErrors property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setHideSpellingErrors(BooleanDefaultTrue value) {
        this.hideSpellingErrors = value;
    }

    /**
     * Gets the value of the hideGrammaticalErrors property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getHideGrammaticalErrors() {
        return hideGrammaticalErrors;
    }

    /**
     * Sets the value of the hideGrammaticalErrors property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setHideGrammaticalErrors(BooleanDefaultTrue value) {
        this.hideGrammaticalErrors = value;
    }

    /**
     * Gets the value of the activeWritingStyle property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the activeWritingStyle property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getActiveWritingStyle().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTWritingStyle }
     * 
     * 
     */
    public List<CTWritingStyle> getActiveWritingStyle() {
        if (activeWritingStyle == null) {
            activeWritingStyle = new ArrayListWml<CTWritingStyle>(this);
        }
        return this.activeWritingStyle;
    }

    /**
     * Gets the value of the proofState property.
     * 
     * @return
     *     possible object is
     *     {@link CTProof }
     *     
     */
    public CTProof getProofState() {
        return proofState;
    }

    /**
     * Sets the value of the proofState property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTProof }
     *     
     */
    public void setProofState(CTProof value) {
        this.proofState = value;
    }

    /**
     * Gets the value of the formsDesign property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getFormsDesign() {
        return formsDesign;
    }

    /**
     * Sets the value of the formsDesign property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setFormsDesign(BooleanDefaultTrue value) {
        this.formsDesign = value;
    }

    /**
     * Gets the value of the attachedTemplate property.
     * 
     * @return
     *     possible object is
     *     {@link CTRel }
     *     
     */
    public CTRel getAttachedTemplate() {
        return attachedTemplate;
    }

    /**
     * Sets the value of the attachedTemplate property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTRel }
     *     
     */
    public void setAttachedTemplate(CTRel value) {
        this.attachedTemplate = value;
    }

    /**
     * Gets the value of the linkStyles property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getLinkStyles() {
        return linkStyles;
    }

    /**
     * Sets the value of the linkStyles property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setLinkStyles(BooleanDefaultTrue value) {
        this.linkStyles = value;
    }

    /**
     * Gets the value of the stylePaneFormatFilter property.
     * 
     * @return
     *     possible object is
     *     {@link CTShortHexNumber }
     *     
     */
    public CTShortHexNumber getStylePaneFormatFilter() {
        return stylePaneFormatFilter;
    }

    /**
     * Sets the value of the stylePaneFormatFilter property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTShortHexNumber }
     *     
     */
    public void setStylePaneFormatFilter(CTShortHexNumber value) {
        this.stylePaneFormatFilter = value;
    }

    /**
     * Gets the value of the stylePaneSortMethod property.
     * 
     * @return
     *     possible object is
     *     {@link CTShortHexNumber }
     *     
     */
    public CTShortHexNumber getStylePaneSortMethod() {
        return stylePaneSortMethod;
    }

    /**
     * Sets the value of the stylePaneSortMethod property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTShortHexNumber }
     *     
     */
    public void setStylePaneSortMethod(CTShortHexNumber value) {
        this.stylePaneSortMethod = value;
    }

    /**
     * Gets the value of the documentType property.
     * 
     * @return
     *     possible object is
     *     {@link CTDocType }
     *     
     */
    public CTDocType getDocumentType() {
        return documentType;
    }

    /**
     * Sets the value of the documentType property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDocType }
     *     
     */
    public void setDocumentType(CTDocType value) {
        this.documentType = value;
    }

    /**
     * Gets the value of the mailMerge property.
     * 
     * @return
     *     possible object is
     *     {@link CTMailMerge }
     *     
     */
    public CTMailMerge getMailMerge() {
        return mailMerge;
    }

    /**
     * Sets the value of the mailMerge property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTMailMerge }
     *     
     */
    public void setMailMerge(CTMailMerge value) {
        this.mailMerge = value;
    }

    /**
     * Gets the value of the revisionView property.
     * 
     * @return
     *     possible object is
     *     {@link CTTrackChangesView }
     *     
     */
    public CTTrackChangesView getRevisionView() {
        return revisionView;
    }

    /**
     * Sets the value of the revisionView property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTrackChangesView }
     *     
     */
    public void setRevisionView(CTTrackChangesView value) {
        this.revisionView = value;
    }

    /**
     * Gets the value of the trackRevisions property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getTrackRevisions() {
        return trackRevisions;
    }

    /**
     * Sets the value of the trackRevisions property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setTrackRevisions(BooleanDefaultTrue value) {
        this.trackRevisions = value;
    }

    /**
     * Gets the value of the doNotTrackMoves property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getDoNotTrackMoves() {
        return doNotTrackMoves;
    }

    /**
     * Sets the value of the doNotTrackMoves property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setDoNotTrackMoves(BooleanDefaultTrue value) {
        this.doNotTrackMoves = value;
    }

    /**
     * Gets the value of the doNotTrackFormatting property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getDoNotTrackFormatting() {
        return doNotTrackFormatting;
    }

    /**
     * Sets the value of the doNotTrackFormatting property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setDoNotTrackFormatting(BooleanDefaultTrue value) {
        this.doNotTrackFormatting = value;
    }

    /**
     * Gets the value of the documentProtection property.
     * 
     * @return
     *     possible object is
     *     {@link CTDocProtect }
     *     
     */
    public CTDocProtect getDocumentProtection() {
        return documentProtection;
    }

    /**
     * Sets the value of the documentProtection property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDocProtect }
     *     
     */
    public void setDocumentProtection(CTDocProtect value) {
        this.documentProtection = value;
    }

    /**
     * Gets the value of the autoFormatOverride property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getAutoFormatOverride() {
        return autoFormatOverride;
    }

    /**
     * Sets the value of the autoFormatOverride property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setAutoFormatOverride(BooleanDefaultTrue value) {
        this.autoFormatOverride = value;
    }

    /**
     * Gets the value of the styleLockTheme property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getStyleLockTheme() {
        return styleLockTheme;
    }

    /**
     * Sets the value of the styleLockTheme property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setStyleLockTheme(BooleanDefaultTrue value) {
        this.styleLockTheme = value;
    }

    /**
     * Gets the value of the styleLockQFSet property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getStyleLockQFSet() {
        return styleLockQFSet;
    }

    /**
     * Sets the value of the styleLockQFSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setStyleLockQFSet(BooleanDefaultTrue value) {
        this.styleLockQFSet = value;
    }

    /**
     * Gets the value of the defaultTabStop property.
     * 
     * @return
     *     possible object is
     *     {@link CTTwipsMeasure }
     *     
     */
    public CTTwipsMeasure getDefaultTabStop() {
        return defaultTabStop;
    }

    /**
     * Sets the value of the defaultTabStop property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTwipsMeasure }
     *     
     */
    public void setDefaultTabStop(CTTwipsMeasure value) {
        this.defaultTabStop = value;
    }

    /**
     * Gets the value of the autoHyphenation property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getAutoHyphenation() {
        return autoHyphenation;
    }

    /**
     * Sets the value of the autoHyphenation property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setAutoHyphenation(BooleanDefaultTrue value) {
        this.autoHyphenation = value;
    }

    /**
     * Gets the value of the consecutiveHyphenLimit property.
     * 
     * @return
     *     possible object is
     *     {@link CTSettings.ConsecutiveHyphenLimit }
     *     
     */
    public CTSettings.ConsecutiveHyphenLimit getConsecutiveHyphenLimit() {
        return consecutiveHyphenLimit;
    }

    /**
     * Sets the value of the consecutiveHyphenLimit property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSettings.ConsecutiveHyphenLimit }
     *     
     */
    public void setConsecutiveHyphenLimit(CTSettings.ConsecutiveHyphenLimit value) {
        this.consecutiveHyphenLimit = value;
    }

    /**
     * Gets the value of the hyphenationZone property.
     * 
     * @return
     *     possible object is
     *     {@link CTTwipsMeasure }
     *     
     */
    public CTTwipsMeasure getHyphenationZone() {
        return hyphenationZone;
    }

    /**
     * Sets the value of the hyphenationZone property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTwipsMeasure }
     *     
     */
    public void setHyphenationZone(CTTwipsMeasure value) {
        this.hyphenationZone = value;
    }

    /**
     * Gets the value of the doNotHyphenateCaps property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getDoNotHyphenateCaps() {
        return doNotHyphenateCaps;
    }

    /**
     * Sets the value of the doNotHyphenateCaps property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setDoNotHyphenateCaps(BooleanDefaultTrue value) {
        this.doNotHyphenateCaps = value;
    }

    /**
     * Gets the value of the showEnvelope property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getShowEnvelope() {
        return showEnvelope;
    }

    /**
     * Sets the value of the showEnvelope property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setShowEnvelope(BooleanDefaultTrue value) {
        this.showEnvelope = value;
    }

    /**
     * Gets the value of the summaryLength property.
     * 
     * @return
     *     possible object is
     *     {@link CTSettings.SummaryLength }
     *     
     */
    public CTSettings.SummaryLength getSummaryLength() {
        return summaryLength;
    }

    /**
     * Sets the value of the summaryLength property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSettings.SummaryLength }
     *     
     */
    public void setSummaryLength(CTSettings.SummaryLength value) {
        this.summaryLength = value;
    }

    /**
     * Gets the value of the clickAndTypeStyle property.
     * 
     * @return
     *     possible object is
     *     {@link CTSettings.ClickAndTypeStyle }
     *     
     */
    public CTSettings.ClickAndTypeStyle getClickAndTypeStyle() {
        return clickAndTypeStyle;
    }

    /**
     * Sets the value of the clickAndTypeStyle property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSettings.ClickAndTypeStyle }
     *     
     */
    public void setClickAndTypeStyle(CTSettings.ClickAndTypeStyle value) {
        this.clickAndTypeStyle = value;
    }

    /**
     * Gets the value of the defaultTableStyle property.
     * 
     * @return
     *     possible object is
     *     {@link CTSettings.DefaultTableStyle }
     *     
     */
    public CTSettings.DefaultTableStyle getDefaultTableStyle() {
        return defaultTableStyle;
    }

    /**
     * Sets the value of the defaultTableStyle property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSettings.DefaultTableStyle }
     *     
     */
    public void setDefaultTableStyle(CTSettings.DefaultTableStyle value) {
        this.defaultTableStyle = value;
    }

    /**
     * Gets the value of the evenAndOddHeaders property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getEvenAndOddHeaders() {
        return evenAndOddHeaders;
    }

    /**
     * Sets the value of the evenAndOddHeaders property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setEvenAndOddHeaders(BooleanDefaultTrue value) {
        this.evenAndOddHeaders = value;
    }

    /**
     * Gets the value of the bookFoldRevPrinting property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getBookFoldRevPrinting() {
        return bookFoldRevPrinting;
    }

    /**
     * Sets the value of the bookFoldRevPrinting property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setBookFoldRevPrinting(BooleanDefaultTrue value) {
        this.bookFoldRevPrinting = value;
    }

    /**
     * Gets the value of the bookFoldPrinting property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getBookFoldPrinting() {
        return bookFoldPrinting;
    }

    /**
     * Sets the value of the bookFoldPrinting property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setBookFoldPrinting(BooleanDefaultTrue value) {
        this.bookFoldPrinting = value;
    }

    /**
     * Gets the value of the bookFoldPrintingSheets property.
     * 
     * @return
     *     possible object is
     *     {@link CTSettings.BookFoldPrintingSheets }
     *     
     */
    public CTSettings.BookFoldPrintingSheets getBookFoldPrintingSheets() {
        return bookFoldPrintingSheets;
    }

    /**
     * Sets the value of the bookFoldPrintingSheets property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSettings.BookFoldPrintingSheets }
     *     
     */
    public void setBookFoldPrintingSheets(CTSettings.BookFoldPrintingSheets value) {
        this.bookFoldPrintingSheets = value;
    }

    /**
     * Gets the value of the drawingGridHorizontalSpacing property.
     * 
     * @return
     *     possible object is
     *     {@link CTTwipsMeasure }
     *     
     */
    public CTTwipsMeasure getDrawingGridHorizontalSpacing() {
        return drawingGridHorizontalSpacing;
    }

    /**
     * Sets the value of the drawingGridHorizontalSpacing property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTwipsMeasure }
     *     
     */
    public void setDrawingGridHorizontalSpacing(CTTwipsMeasure value) {
        this.drawingGridHorizontalSpacing = value;
    }

    /**
     * Gets the value of the drawingGridVerticalSpacing property.
     * 
     * @return
     *     possible object is
     *     {@link CTTwipsMeasure }
     *     
     */
    public CTTwipsMeasure getDrawingGridVerticalSpacing() {
        return drawingGridVerticalSpacing;
    }

    /**
     * Sets the value of the drawingGridVerticalSpacing property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTwipsMeasure }
     *     
     */
    public void setDrawingGridVerticalSpacing(CTTwipsMeasure value) {
        this.drawingGridVerticalSpacing = value;
    }

    /**
     * Gets the value of the displayHorizontalDrawingGridEvery property.
     * 
     * @return
     *     possible object is
     *     {@link CTSettings.DisplayHorizontalDrawingGridEvery }
     *     
     */
    public CTSettings.DisplayHorizontalDrawingGridEvery getDisplayHorizontalDrawingGridEvery() {
        return displayHorizontalDrawingGridEvery;
    }

    /**
     * Sets the value of the displayHorizontalDrawingGridEvery property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSettings.DisplayHorizontalDrawingGridEvery }
     *     
     */
    public void setDisplayHorizontalDrawingGridEvery(CTSettings.DisplayHorizontalDrawingGridEvery value) {
        this.displayHorizontalDrawingGridEvery = value;
    }

    /**
     * Gets the value of the displayVerticalDrawingGridEvery property.
     * 
     * @return
     *     possible object is
     *     {@link CTSettings.DisplayVerticalDrawingGridEvery }
     *     
     */
    public CTSettings.DisplayVerticalDrawingGridEvery getDisplayVerticalDrawingGridEvery() {
        return displayVerticalDrawingGridEvery;
    }

    /**
     * Sets the value of the displayVerticalDrawingGridEvery property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSettings.DisplayVerticalDrawingGridEvery }
     *     
     */
    public void setDisplayVerticalDrawingGridEvery(CTSettings.DisplayVerticalDrawingGridEvery value) {
        this.displayVerticalDrawingGridEvery = value;
    }

    /**
     * Gets the value of the doNotUseMarginsForDrawingGridOrigin property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getDoNotUseMarginsForDrawingGridOrigin() {
        return doNotUseMarginsForDrawingGridOrigin;
    }

    /**
     * Sets the value of the doNotUseMarginsForDrawingGridOrigin property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setDoNotUseMarginsForDrawingGridOrigin(BooleanDefaultTrue value) {
        this.doNotUseMarginsForDrawingGridOrigin = value;
    }

    /**
     * Gets the value of the drawingGridHorizontalOrigin property.
     * 
     * @return
     *     possible object is
     *     {@link CTTwipsMeasure }
     *     
     */
    public CTTwipsMeasure getDrawingGridHorizontalOrigin() {
        return drawingGridHorizontalOrigin;
    }

    /**
     * Sets the value of the drawingGridHorizontalOrigin property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTwipsMeasure }
     *     
     */
    public void setDrawingGridHorizontalOrigin(CTTwipsMeasure value) {
        this.drawingGridHorizontalOrigin = value;
    }

    /**
     * Gets the value of the drawingGridVerticalOrigin property.
     * 
     * @return
     *     possible object is
     *     {@link CTTwipsMeasure }
     *     
     */
    public CTTwipsMeasure getDrawingGridVerticalOrigin() {
        return drawingGridVerticalOrigin;
    }

    /**
     * Sets the value of the drawingGridVerticalOrigin property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTwipsMeasure }
     *     
     */
    public void setDrawingGridVerticalOrigin(CTTwipsMeasure value) {
        this.drawingGridVerticalOrigin = value;
    }

    /**
     * Gets the value of the doNotShadeFormData property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getDoNotShadeFormData() {
        return doNotShadeFormData;
    }

    /**
     * Sets the value of the doNotShadeFormData property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setDoNotShadeFormData(BooleanDefaultTrue value) {
        this.doNotShadeFormData = value;
    }

    /**
     * Gets the value of the noPunctuationKerning property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getNoPunctuationKerning() {
        return noPunctuationKerning;
    }

    /**
     * Sets the value of the noPunctuationKerning property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setNoPunctuationKerning(BooleanDefaultTrue value) {
        this.noPunctuationKerning = value;
    }

    /**
     * Gets the value of the characterSpacingControl property.
     * 
     * @return
     *     possible object is
     *     {@link CTCharacterSpacing }
     *     
     */
    public CTCharacterSpacing getCharacterSpacingControl() {
        return characterSpacingControl;
    }

    /**
     * Sets the value of the characterSpacingControl property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTCharacterSpacing }
     *     
     */
    public void setCharacterSpacingControl(CTCharacterSpacing value) {
        this.characterSpacingControl = value;
    }

    /**
     * Gets the value of the printTwoOnOne property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getPrintTwoOnOne() {
        return printTwoOnOne;
    }

    /**
     * Sets the value of the printTwoOnOne property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setPrintTwoOnOne(BooleanDefaultTrue value) {
        this.printTwoOnOne = value;
    }

    /**
     * Gets the value of the strictFirstAndLastChars property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getStrictFirstAndLastChars() {
        return strictFirstAndLastChars;
    }

    /**
     * Sets the value of the strictFirstAndLastChars property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setStrictFirstAndLastChars(BooleanDefaultTrue value) {
        this.strictFirstAndLastChars = value;
    }

    /**
     * Gets the value of the noLineBreaksAfter property.
     * 
     * @return
     *     possible object is
     *     {@link CTKinsoku }
     *     
     */
    public CTKinsoku getNoLineBreaksAfter() {
        return noLineBreaksAfter;
    }

    /**
     * Sets the value of the noLineBreaksAfter property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTKinsoku }
     *     
     */
    public void setNoLineBreaksAfter(CTKinsoku value) {
        this.noLineBreaksAfter = value;
    }

    /**
     * Gets the value of the noLineBreaksBefore property.
     * 
     * @return
     *     possible object is
     *     {@link CTKinsoku }
     *     
     */
    public CTKinsoku getNoLineBreaksBefore() {
        return noLineBreaksBefore;
    }

    /**
     * Sets the value of the noLineBreaksBefore property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTKinsoku }
     *     
     */
    public void setNoLineBreaksBefore(CTKinsoku value) {
        this.noLineBreaksBefore = value;
    }

    /**
     * Gets the value of the savePreviewPicture property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getSavePreviewPicture() {
        return savePreviewPicture;
    }

    /**
     * Sets the value of the savePreviewPicture property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setSavePreviewPicture(BooleanDefaultTrue value) {
        this.savePreviewPicture = value;
    }

    /**
     * Gets the value of the doNotValidateAgainstSchema property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getDoNotValidateAgainstSchema() {
        return doNotValidateAgainstSchema;
    }

    /**
     * Sets the value of the doNotValidateAgainstSchema property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setDoNotValidateAgainstSchema(BooleanDefaultTrue value) {
        this.doNotValidateAgainstSchema = value;
    }

    /**
     * Gets the value of the saveInvalidXml property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getSaveInvalidXml() {
        return saveInvalidXml;
    }

    /**
     * Sets the value of the saveInvalidXml property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setSaveInvalidXml(BooleanDefaultTrue value) {
        this.saveInvalidXml = value;
    }

    /**
     * Gets the value of the ignoreMixedContent property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getIgnoreMixedContent() {
        return ignoreMixedContent;
    }

    /**
     * Sets the value of the ignoreMixedContent property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setIgnoreMixedContent(BooleanDefaultTrue value) {
        this.ignoreMixedContent = value;
    }

    /**
     * Gets the value of the alwaysShowPlaceholderText property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getAlwaysShowPlaceholderText() {
        return alwaysShowPlaceholderText;
    }

    /**
     * Sets the value of the alwaysShowPlaceholderText property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setAlwaysShowPlaceholderText(BooleanDefaultTrue value) {
        this.alwaysShowPlaceholderText = value;
    }

    /**
     * Gets the value of the doNotDemarcateInvalidXml property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getDoNotDemarcateInvalidXml() {
        return doNotDemarcateInvalidXml;
    }

    /**
     * Sets the value of the doNotDemarcateInvalidXml property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setDoNotDemarcateInvalidXml(BooleanDefaultTrue value) {
        this.doNotDemarcateInvalidXml = value;
    }

    /**
     * Gets the value of the saveXmlDataOnly property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getSaveXmlDataOnly() {
        return saveXmlDataOnly;
    }

    /**
     * Sets the value of the saveXmlDataOnly property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setSaveXmlDataOnly(BooleanDefaultTrue value) {
        this.saveXmlDataOnly = value;
    }

    /**
     * Gets the value of the useXSLTWhenSaving property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getUseXSLTWhenSaving() {
        return useXSLTWhenSaving;
    }

    /**
     * Sets the value of the useXSLTWhenSaving property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setUseXSLTWhenSaving(BooleanDefaultTrue value) {
        this.useXSLTWhenSaving = value;
    }

    /**
     * Gets the value of the saveThroughXslt property.
     * 
     * @return
     *     possible object is
     *     {@link CTSaveThroughXslt }
     *     
     */
    public CTSaveThroughXslt getSaveThroughXslt() {
        return saveThroughXslt;
    }

    /**
     * Sets the value of the saveThroughXslt property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSaveThroughXslt }
     *     
     */
    public void setSaveThroughXslt(CTSaveThroughXslt value) {
        this.saveThroughXslt = value;
    }

    /**
     * Gets the value of the showXMLTags property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getShowXMLTags() {
        return showXMLTags;
    }

    /**
     * Sets the value of the showXMLTags property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setShowXMLTags(BooleanDefaultTrue value) {
        this.showXMLTags = value;
    }

    /**
     * Gets the value of the alwaysMergeEmptyNamespace property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getAlwaysMergeEmptyNamespace() {
        return alwaysMergeEmptyNamespace;
    }

    /**
     * Sets the value of the alwaysMergeEmptyNamespace property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setAlwaysMergeEmptyNamespace(BooleanDefaultTrue value) {
        this.alwaysMergeEmptyNamespace = value;
    }

    /**
     * Gets the value of the updateFields property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getUpdateFields() {
        return updateFields;
    }

    /**
     * Sets the value of the updateFields property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setUpdateFields(BooleanDefaultTrue value) {
        this.updateFields = value;
    }

    /**
     * Gets the value of the hdrShapeDefaults property.
     * 
     * @return
     *     possible object is
     *     {@link CTShapeDefaults }
     *     
     */
    public CTShapeDefaults getHdrShapeDefaults() {
        return hdrShapeDefaults;
    }

    /**
     * Sets the value of the hdrShapeDefaults property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTShapeDefaults }
     *     
     */
    public void setHdrShapeDefaults(CTShapeDefaults value) {
        this.hdrShapeDefaults = value;
    }

    /**
     * Gets the value of the footnotePr property.
     * 
     * @return
     *     possible object is
     *     {@link CTFtnDocProps }
     *     
     */
    public CTFtnDocProps getFootnotePr() {
        return footnotePr;
    }

    /**
     * Sets the value of the footnotePr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTFtnDocProps }
     *     
     */
    public void setFootnotePr(CTFtnDocProps value) {
        this.footnotePr = value;
    }

    /**
     * Gets the value of the endnotePr property.
     * 
     * @return
     *     possible object is
     *     {@link CTEdnDocProps }
     *     
     */
    public CTEdnDocProps getEndnotePr() {
        return endnotePr;
    }

    /**
     * Sets the value of the endnotePr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTEdnDocProps }
     *     
     */
    public void setEndnotePr(CTEdnDocProps value) {
        this.endnotePr = value;
    }

    /**
     * Gets the value of the compat property.
     * 
     * @return
     *     possible object is
     *     {@link CTCompat }
     *     
     */
    public CTCompat getCompat() {
        return compat;
    }

    /**
     * Sets the value of the compat property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTCompat }
     *     
     */
    public void setCompat(CTCompat value) {
        this.compat = value;
    }

    /**
     * Gets the value of the docVars property.
     * 
     * @return
     *     possible object is
     *     {@link CTDocVars }
     *     
     */
    public CTDocVars getDocVars() {
        return docVars;
    }

    /**
     * Sets the value of the docVars property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDocVars }
     *     
     */
    public void setDocVars(CTDocVars value) {
        this.docVars = value;
    }

    /**
     * Gets the value of the rsids property.
     * 
     * @return
     *     possible object is
     *     {@link CTDocRsids }
     *     
     */
    public CTDocRsids getRsids() {
        return rsids;
    }

    /**
     * Sets the value of the rsids property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDocRsids }
     *     
     */
    public void setRsids(CTDocRsids value) {
        this.rsids = value;
    }

    /**
     * properties of math in the document
     * 
     * @return
     *     possible object is
     *     {@link CTMathPr }
     *     
     */
    public CTMathPr getMathPr() {
        return mathPr;
    }

    /**
     * Sets the value of the mathPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTMathPr }
     *     
     */
    public void setMathPr(CTMathPr value) {
        this.mathPr = value;
    }

    /**
     * Gets the value of the uiCompat97To2003 property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getUiCompat97To2003() {
        return uiCompat97To2003;
    }

    /**
     * Sets the value of the uiCompat97To2003 property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setUiCompat97To2003(BooleanDefaultTrue value) {
        this.uiCompat97To2003 = value;
    }

    /**
     * Gets the value of the attachedSchema property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the attachedSchema property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAttachedSchema().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTSettings.AttachedSchema }
     * 
     * 
     */
    public List<CTSettings.AttachedSchema> getAttachedSchema() {
        if (attachedSchema == null) {
            attachedSchema = new ArrayListWml<CTSettings.AttachedSchema>(this);
        }
        return this.attachedSchema;
    }

    /**
     * Gets the value of the themeFontLang property.
     * 
     * @return
     *     possible object is
     *     {@link CTLanguage }
     *     
     */
    public CTLanguage getThemeFontLang() {
        return themeFontLang;
    }

    /**
     * Sets the value of the themeFontLang property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTLanguage }
     *     
     */
    public void setThemeFontLang(CTLanguage value) {
        this.themeFontLang = value;
    }

    /**
     * Gets the value of the clrSchemeMapping property.
     * 
     * @return
     *     possible object is
     *     {@link CTColorSchemeMapping }
     *     
     */
    public CTColorSchemeMapping getClrSchemeMapping() {
        return clrSchemeMapping;
    }

    /**
     * Sets the value of the clrSchemeMapping property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColorSchemeMapping }
     *     
     */
    public void setClrSchemeMapping(CTColorSchemeMapping value) {
        this.clrSchemeMapping = value;
    }

    /**
     * Gets the value of the doNotIncludeSubdocsInStats property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getDoNotIncludeSubdocsInStats() {
        return doNotIncludeSubdocsInStats;
    }

    /**
     * Sets the value of the doNotIncludeSubdocsInStats property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setDoNotIncludeSubdocsInStats(BooleanDefaultTrue value) {
        this.doNotIncludeSubdocsInStats = value;
    }

    /**
     * Gets the value of the doNotAutoCompressPictures property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getDoNotAutoCompressPictures() {
        return doNotAutoCompressPictures;
    }

    /**
     * Sets the value of the doNotAutoCompressPictures property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setDoNotAutoCompressPictures(BooleanDefaultTrue value) {
        this.doNotAutoCompressPictures = value;
    }

    /**
     * Gets the value of the forceUpgrade property.
     * 
     * @return
     *     possible object is
     *     {@link CTSettings.ForceUpgrade }
     *     
     */
    public CTSettings.ForceUpgrade getForceUpgrade() {
        return forceUpgrade;
    }

    /**
     * Sets the value of the forceUpgrade property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSettings.ForceUpgrade }
     *     
     */
    public void setForceUpgrade(CTSettings.ForceUpgrade value) {
        this.forceUpgrade = value;
    }

    /**
     * Gets the value of the captions property.
     * 
     * @return
     *     possible object is
     *     {@link CTCaptions }
     *     
     */
    public CTCaptions getCaptions() {
        return captions;
    }

    /**
     * Sets the value of the captions property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTCaptions }
     *     
     */
    public void setCaptions(CTCaptions value) {
        this.captions = value;
    }

    /**
     * Gets the value of the readModeInkLockDown property.
     * 
     * @return
     *     possible object is
     *     {@link CTReadingModeInkLockDown }
     *     
     */
    public CTReadingModeInkLockDown getReadModeInkLockDown() {
        return readModeInkLockDown;
    }

    /**
     * Sets the value of the readModeInkLockDown property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTReadingModeInkLockDown }
     *     
     */
    public void setReadModeInkLockDown(CTReadingModeInkLockDown value) {
        this.readModeInkLockDown = value;
    }

    /**
     * Gets the value of the smartTagType property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the smartTagType property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSmartTagType().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTSmartTagType }
     * 
     * 
     */
    public List<CTSmartTagType> getSmartTagType() {
        if (smartTagType == null) {
            smartTagType = new ArrayListWml<CTSmartTagType>(this);
        }
        return this.smartTagType;
    }

    /**
     * Custom XML Schema List
     * 
     * @return
     *     possible object is
     *     {@link SchemaLibrary }
     *     
     */
    public SchemaLibrary getSchemaLibrary() {
        return schemaLibrary;
    }

    /**
     * Sets the value of the schemaLibrary property.
     * 
     * @param value
     *     allowed object is
     *     {@link SchemaLibrary }
     *     
     */
    public void setSchemaLibrary(SchemaLibrary value) {
        this.schemaLibrary = value;
    }

    /**
     * Gets the value of the shapeDefaults property.
     * 
     * @return
     *     possible object is
     *     {@link CTShapeDefaults }
     *     
     */
    public CTShapeDefaults getShapeDefaults() {
        return shapeDefaults;
    }

    /**
     * Sets the value of the shapeDefaults property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTShapeDefaults }
     *     
     */
    public void setShapeDefaults(CTShapeDefaults value) {
        this.shapeDefaults = value;
    }

    /**
     * Gets the value of the doNotEmbedSmartTags property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getDoNotEmbedSmartTags() {
        return doNotEmbedSmartTags;
    }

    /**
     * Sets the value of the doNotEmbedSmartTags property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setDoNotEmbedSmartTags(BooleanDefaultTrue value) {
        this.doNotEmbedSmartTags = value;
    }

    /**
     * Gets the value of the decimalSymbol property.
     * 
     * @return
     *     possible object is
     *     {@link CTSettings.DecimalSymbol }
     *     
     */
    public CTSettings.DecimalSymbol getDecimalSymbol() {
        return decimalSymbol;
    }

    /**
     * Sets the value of the decimalSymbol property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSettings.DecimalSymbol }
     *     
     */
    public void setDecimalSymbol(CTSettings.DecimalSymbol value) {
        this.decimalSymbol = value;
    }

    /**
     * Gets the value of the listSeparator property.
     * 
     * @return
     *     possible object is
     *     {@link CTSettings.ListSeparator }
     *     
     */
    public CTSettings.ListSeparator getListSeparator() {
        return listSeparator;
    }

    /**
     * Sets the value of the listSeparator property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSettings.ListSeparator }
     *     
     */
    public void setListSeparator(CTSettings.ListSeparator value) {
        this.listSeparator = value;
    }

    /**
     * Gets the value of the chartTrackingRefBased property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getChartTrackingRefBased() {
        return chartTrackingRefBased;
    }

    /**
     * Sets the value of the chartTrackingRefBased property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setChartTrackingRefBased(BooleanDefaultTrue value) {
        this.chartTrackingRefBased = value;
    }

    /**
     * Gets the value of the docId14 property.
     * 
     * @return
     *     possible object is
     *     {@link CTLongHexNumber }
     *     
     */
    public CTLongHexNumber getDocId14() {
        return docId14;
    }

    /**
     * Sets the value of the docId14 property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTLongHexNumber }
     *     
     */
    public void setDocId14(CTLongHexNumber value) {
        this.docId14 = value;
    }

    /**
     * Gets the value of the docId15 property.
     * 
     * @return
     *     possible object is
     *     {@link CTGuid }
     *     
     */
    public CTGuid getDocId15() {
        return docId15;
    }

    /**
     * Sets the value of the docId15 property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGuid }
     *     
     */
    public void setDocId15(CTGuid value) {
        this.docId15 = value;
    }

    /**
     * Gets the value of the conflictMode property.
     * 
     * @return
     *     possible object is
     *     {@link CTOnOff }
     *     
     */
    public CTOnOff getConflictMode() {
        return conflictMode;
    }

    /**
     * Sets the value of the conflictMode property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOnOff }
     *     
     */
    public void setConflictMode(CTOnOff value) {
        this.conflictMode = value;
    }

    /**
     * Gets the value of the discardImageEditingData property.
     * 
     * @return
     *     possible object is
     *     {@link CTOnOff }
     *     
     */
    public CTOnOff getDiscardImageEditingData() {
        return discardImageEditingData;
    }

    /**
     * Sets the value of the discardImageEditingData property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOnOff }
     *     
     */
    public void setDiscardImageEditingData(CTOnOff value) {
        this.discardImageEditingData = value;
    }

    /**
     * Gets the value of the defaultImageDpi property.
     * 
     * @return
     *     possible object is
     *     {@link CTDefaultImageDpi }
     *     
     */
    public CTDefaultImageDpi getDefaultImageDpi() {
        return defaultImageDpi;
    }

    /**
     * Sets the value of the defaultImageDpi property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDefaultImageDpi }
     *     
     */
    public void setDefaultImageDpi(CTDefaultImageDpi value) {
        this.defaultImageDpi = value;
    }

    /**
     * Gets the value of the ignorable property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIgnorable() {
        return ignorable;
    }

    /**
     * Sets the value of the ignorable property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIgnorable(String value) {
        this.ignorable = value;
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


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attribute name="val" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class AttachedSchema implements Child
    {

        @XmlAttribute(name = "val", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected String val;
        @XmlTransient
        private Object parent;

        /**
         * Gets the value of the val property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getVal() {
            return val;
        }

        /**
         * Sets the value of the val property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setVal(String value) {
            this.val = value;
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


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attribute name="val" use="required">
     *         &lt;simpleType>
     *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
     *           &lt;/restriction>
     *         &lt;/simpleType>
     *       &lt;/attribute>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class BookFoldPrintingSheets implements Child
    {

        @XmlAttribute(name = "val", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
        protected BigInteger val;
        @XmlTransient
        private Object parent;

        /**
         * Gets the value of the val property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getVal() {
            return val;
        }

        /**
         * Sets the value of the val property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setVal(BigInteger value) {
            this.val = value;
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


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attribute name="val" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class ClickAndTypeStyle implements Child
    {

        @XmlAttribute(name = "val", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected String val;
        @XmlTransient
        private Object parent;

        /**
         * Gets the value of the val property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getVal() {
            return val;
        }

        /**
         * Sets the value of the val property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setVal(String value) {
            this.val = value;
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


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attribute name="val" use="required">
     *         &lt;simpleType>
     *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
     *           &lt;/restriction>
     *         &lt;/simpleType>
     *       &lt;/attribute>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class ConsecutiveHyphenLimit implements Child
    {

        @XmlAttribute(name = "val", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
        protected BigInteger val;
        @XmlTransient
        private Object parent;

        /**
         * Gets the value of the val property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getVal() {
            return val;
        }

        /**
         * Sets the value of the val property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setVal(BigInteger value) {
            this.val = value;
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


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attribute name="val" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class DecimalSymbol implements Child
    {

        @XmlAttribute(name = "val", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected String val;
        @XmlTransient
        private Object parent;

        /**
         * Gets the value of the val property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getVal() {
            return val;
        }

        /**
         * Sets the value of the val property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setVal(String value) {
            this.val = value;
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


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attribute name="val" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class DefaultTableStyle implements Child
    {

        @XmlAttribute(name = "val", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected String val;
        @XmlTransient
        private Object parent;

        /**
         * Gets the value of the val property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getVal() {
            return val;
        }

        /**
         * Sets the value of the val property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setVal(String value) {
            this.val = value;
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


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attribute name="val" use="required">
     *         &lt;simpleType>
     *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
     *           &lt;/restriction>
     *         &lt;/simpleType>
     *       &lt;/attribute>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class DisplayHorizontalDrawingGridEvery implements Child
    {

        @XmlAttribute(name = "val", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
        protected BigInteger val;
        @XmlTransient
        private Object parent;

        /**
         * Gets the value of the val property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getVal() {
            return val;
        }

        /**
         * Sets the value of the val property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setVal(BigInteger value) {
            this.val = value;
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


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attribute name="val" use="required">
     *         &lt;simpleType>
     *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
     *           &lt;/restriction>
     *         &lt;/simpleType>
     *       &lt;/attribute>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class DisplayVerticalDrawingGridEvery implements Child
    {

        @XmlAttribute(name = "val", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
        protected BigInteger val;
        @XmlTransient
        private Object parent;

        /**
         * Gets the value of the val property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getVal() {
            return val;
        }

        /**
         * Sets the value of the val property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setVal(BigInteger value) {
            this.val = value;
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


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class ForceUpgrade implements Child
    {

        @XmlTransient
        private Object parent;

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


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attribute name="val" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class ListSeparator implements Child
    {

        @XmlAttribute(name = "val", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected String val;
        @XmlTransient
        private Object parent;

        /**
         * Gets the value of the val property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getVal() {
            return val;
        }

        /**
         * Sets the value of the val property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setVal(String value) {
            this.val = value;
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


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attribute name="val" use="required">
     *         &lt;simpleType>
     *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
     *           &lt;/restriction>
     *         &lt;/simpleType>
     *       &lt;/attribute>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class SummaryLength implements Child
    {

        @XmlAttribute(name = "val", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
        protected BigInteger val;
        @XmlTransient
        private Object parent;

        /**
         * Gets the value of the val property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getVal() {
            return val;
        }

        /**
         * Sets the value of the val property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setVal(BigInteger value) {
            this.val = value;
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

}
