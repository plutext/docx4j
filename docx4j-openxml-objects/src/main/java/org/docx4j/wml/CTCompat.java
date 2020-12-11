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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Compat complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Compat">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="useSingleBorderforContiguousCells" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="wpJustification" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="noTabHangInd" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="noLeading" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="spaceForUL" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="noColumnBalance" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="balanceSingleByteDoubleByteWidth" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="noExtraLineSpacing" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="doNotLeaveBackslashAlone" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="ulTrailSpace" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="doNotExpandShiftReturn" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="spacingInWholePoints" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="lineWrapLikeWord6" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="printBodyTextBeforeHeader" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="printColBlack" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="wpSpaceWidth" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="showBreaksInFrames" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="subFontBySize" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="suppressBottomSpacing" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="suppressTopSpacing" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="suppressSpacingAtTopOfPage" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="suppressTopSpacingWP" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="suppressSpBfAfterPgBrk" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="swapBordersFacingPages" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="convMailMergeEsc" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="truncateFontHeightsLikeWP6" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="mwSmallCaps" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="usePrinterMetrics" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="doNotSuppressParagraphBorders" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="wrapTrailSpaces" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="footnoteLayoutLikeWW8" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="shapeLayoutLikeWW8" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="alignTablesRowByRow" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="forgetLastTabAlignment" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="adjustLineHeightInTable" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="autoSpaceLikeWord95" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="noSpaceRaiseLower" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="doNotUseHTMLParagraphAutoSpacing" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="layoutRawTableWidth" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="layoutTableRowsApart" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="useWord97LineBreakRules" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="doNotBreakWrappedTables" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="doNotSnapToGridInCell" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="selectFldWithFirstOrLastChar" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="applyBreakingRules" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="doNotWrapTextWithPunct" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="doNotUseEastAsianBreakRules" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="useWord2002TableStyleRules" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="growAutofit" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="useFELayout" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="useNormalStyleForList" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="doNotUseIndentAsNumberingTabStop" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="useAltKinsokuLineBreakRules" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="allowSpaceOfSameStyleInTable" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="doNotSuppressIndentation" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="doNotAutofitConstrainedTables" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="autofitToFirstFixedWidthCell" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="underlineTabInNumList" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="displayHangulFixedWidth" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="splitPgBreakAndParaMark" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="doNotVertAlignCellWithSp" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="doNotBreakConstrainedForcedTable" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="doNotVertAlignInTxbx" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="useAnsiKerningPairs" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="cachedColBalance" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="compatSetting" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_CompatSetting" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Compat", propOrder = {
    "useSingleBorderforContiguousCells",
    "wpJustification",
    "noTabHangInd",
    "noLeading",
    "spaceForUL",
    "noColumnBalance",
    "balanceSingleByteDoubleByteWidth",
    "noExtraLineSpacing",
    "doNotLeaveBackslashAlone",
    "ulTrailSpace",
    "doNotExpandShiftReturn",
    "spacingInWholePoints",
    "lineWrapLikeWord6",
    "printBodyTextBeforeHeader",
    "printColBlack",
    "wpSpaceWidth",
    "showBreaksInFrames",
    "subFontBySize",
    "suppressBottomSpacing",
    "suppressTopSpacing",
    "suppressSpacingAtTopOfPage",
    "suppressTopSpacingWP",
    "suppressSpBfAfterPgBrk",
    "swapBordersFacingPages",
    "convMailMergeEsc",
    "truncateFontHeightsLikeWP6",
    "mwSmallCaps",
    "usePrinterMetrics",
    "doNotSuppressParagraphBorders",
    "wrapTrailSpaces",
    "footnoteLayoutLikeWW8",
    "shapeLayoutLikeWW8",
    "alignTablesRowByRow",
    "forgetLastTabAlignment",
    "adjustLineHeightInTable",
    "autoSpaceLikeWord95",
    "noSpaceRaiseLower",
    "doNotUseHTMLParagraphAutoSpacing",
    "layoutRawTableWidth",
    "layoutTableRowsApart",
    "useWord97LineBreakRules",
    "doNotBreakWrappedTables",
    "doNotSnapToGridInCell",
    "selectFldWithFirstOrLastChar",
    "applyBreakingRules",
    "doNotWrapTextWithPunct",
    "doNotUseEastAsianBreakRules",
    "useWord2002TableStyleRules",
    "growAutofit",
    "useFELayout",
    "useNormalStyleForList",
    "doNotUseIndentAsNumberingTabStop",
    "useAltKinsokuLineBreakRules",
    "allowSpaceOfSameStyleInTable",
    "doNotSuppressIndentation",
    "doNotAutofitConstrainedTables",
    "autofitToFirstFixedWidthCell",
    "underlineTabInNumList",
    "displayHangulFixedWidth",
    "splitPgBreakAndParaMark",
    "doNotVertAlignCellWithSp",
    "doNotBreakConstrainedForcedTable",
    "doNotVertAlignInTxbx",
    "useAnsiKerningPairs",
    "cachedColBalance",
    "compatSetting"
})
public class CTCompat implements Child
{

    protected BooleanDefaultTrue useSingleBorderforContiguousCells;
    protected BooleanDefaultTrue wpJustification;
    protected BooleanDefaultTrue noTabHangInd;
    protected BooleanDefaultTrue noLeading;
    protected BooleanDefaultTrue spaceForUL;
    protected BooleanDefaultTrue noColumnBalance;
    protected BooleanDefaultTrue balanceSingleByteDoubleByteWidth;
    protected BooleanDefaultTrue noExtraLineSpacing;
    protected BooleanDefaultTrue doNotLeaveBackslashAlone;
    protected BooleanDefaultTrue ulTrailSpace;
    protected BooleanDefaultTrue doNotExpandShiftReturn;
    protected BooleanDefaultTrue spacingInWholePoints;
    protected BooleanDefaultTrue lineWrapLikeWord6;
    protected BooleanDefaultTrue printBodyTextBeforeHeader;
    protected BooleanDefaultTrue printColBlack;
    protected BooleanDefaultTrue wpSpaceWidth;
    protected BooleanDefaultTrue showBreaksInFrames;
    protected BooleanDefaultTrue subFontBySize;
    protected BooleanDefaultTrue suppressBottomSpacing;
    protected BooleanDefaultTrue suppressTopSpacing;
    protected BooleanDefaultTrue suppressSpacingAtTopOfPage;
    protected BooleanDefaultTrue suppressTopSpacingWP;
    protected BooleanDefaultTrue suppressSpBfAfterPgBrk;
    protected BooleanDefaultTrue swapBordersFacingPages;
    protected BooleanDefaultTrue convMailMergeEsc;
    protected BooleanDefaultTrue truncateFontHeightsLikeWP6;
    protected BooleanDefaultTrue mwSmallCaps;
    protected BooleanDefaultTrue usePrinterMetrics;
    protected BooleanDefaultTrue doNotSuppressParagraphBorders;
    protected BooleanDefaultTrue wrapTrailSpaces;
    protected BooleanDefaultTrue footnoteLayoutLikeWW8;
    protected BooleanDefaultTrue shapeLayoutLikeWW8;
    protected BooleanDefaultTrue alignTablesRowByRow;
    protected BooleanDefaultTrue forgetLastTabAlignment;
    protected BooleanDefaultTrue adjustLineHeightInTable;
    protected BooleanDefaultTrue autoSpaceLikeWord95;
    protected BooleanDefaultTrue noSpaceRaiseLower;
    protected BooleanDefaultTrue doNotUseHTMLParagraphAutoSpacing;
    protected BooleanDefaultTrue layoutRawTableWidth;
    protected BooleanDefaultTrue layoutTableRowsApart;
    protected BooleanDefaultTrue useWord97LineBreakRules;
    protected BooleanDefaultTrue doNotBreakWrappedTables;
    protected BooleanDefaultTrue doNotSnapToGridInCell;
    protected BooleanDefaultTrue selectFldWithFirstOrLastChar;
    protected BooleanDefaultTrue applyBreakingRules;
    protected BooleanDefaultTrue doNotWrapTextWithPunct;
    protected BooleanDefaultTrue doNotUseEastAsianBreakRules;
    protected BooleanDefaultTrue useWord2002TableStyleRules;
    protected BooleanDefaultTrue growAutofit;
    protected BooleanDefaultTrue useFELayout;
    protected BooleanDefaultTrue useNormalStyleForList;
    protected BooleanDefaultTrue doNotUseIndentAsNumberingTabStop;
    protected BooleanDefaultTrue useAltKinsokuLineBreakRules;
    protected BooleanDefaultTrue allowSpaceOfSameStyleInTable;
    protected BooleanDefaultTrue doNotSuppressIndentation;
    protected BooleanDefaultTrue doNotAutofitConstrainedTables;
    protected BooleanDefaultTrue autofitToFirstFixedWidthCell;
    protected BooleanDefaultTrue underlineTabInNumList;
    protected BooleanDefaultTrue displayHangulFixedWidth;
    protected BooleanDefaultTrue splitPgBreakAndParaMark;
    protected BooleanDefaultTrue doNotVertAlignCellWithSp;
    protected BooleanDefaultTrue doNotBreakConstrainedForcedTable;
    protected BooleanDefaultTrue doNotVertAlignInTxbx;
    protected BooleanDefaultTrue useAnsiKerningPairs;
    protected BooleanDefaultTrue cachedColBalance;
    protected List<CTCompatSetting> compatSetting = new ArrayListWml<CTCompatSetting>(this);
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the useSingleBorderforContiguousCells property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getUseSingleBorderforContiguousCells() {
        return useSingleBorderforContiguousCells;
    }

    /**
     * Sets the value of the useSingleBorderforContiguousCells property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setUseSingleBorderforContiguousCells(BooleanDefaultTrue value) {
        this.useSingleBorderforContiguousCells = value;
    }

    /**
     * Gets the value of the wpJustification property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getWpJustification() {
        return wpJustification;
    }

    /**
     * Sets the value of the wpJustification property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setWpJustification(BooleanDefaultTrue value) {
        this.wpJustification = value;
    }

    /**
     * Gets the value of the noTabHangInd property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getNoTabHangInd() {
        return noTabHangInd;
    }

    /**
     * Sets the value of the noTabHangInd property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setNoTabHangInd(BooleanDefaultTrue value) {
        this.noTabHangInd = value;
    }

    /**
     * Gets the value of the noLeading property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getNoLeading() {
        return noLeading;
    }

    /**
     * Sets the value of the noLeading property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setNoLeading(BooleanDefaultTrue value) {
        this.noLeading = value;
    }

    /**
     * Gets the value of the spaceForUL property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getSpaceForUL() {
        return spaceForUL;
    }

    /**
     * Sets the value of the spaceForUL property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setSpaceForUL(BooleanDefaultTrue value) {
        this.spaceForUL = value;
    }

    /**
     * Gets the value of the noColumnBalance property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getNoColumnBalance() {
        return noColumnBalance;
    }

    /**
     * Sets the value of the noColumnBalance property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setNoColumnBalance(BooleanDefaultTrue value) {
        this.noColumnBalance = value;
    }

    /**
     * Gets the value of the balanceSingleByteDoubleByteWidth property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getBalanceSingleByteDoubleByteWidth() {
        return balanceSingleByteDoubleByteWidth;
    }

    /**
     * Sets the value of the balanceSingleByteDoubleByteWidth property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setBalanceSingleByteDoubleByteWidth(BooleanDefaultTrue value) {
        this.balanceSingleByteDoubleByteWidth = value;
    }

    /**
     * Gets the value of the noExtraLineSpacing property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getNoExtraLineSpacing() {
        return noExtraLineSpacing;
    }

    /**
     * Sets the value of the noExtraLineSpacing property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setNoExtraLineSpacing(BooleanDefaultTrue value) {
        this.noExtraLineSpacing = value;
    }

    /**
     * Gets the value of the doNotLeaveBackslashAlone property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getDoNotLeaveBackslashAlone() {
        return doNotLeaveBackslashAlone;
    }

    /**
     * Sets the value of the doNotLeaveBackslashAlone property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setDoNotLeaveBackslashAlone(BooleanDefaultTrue value) {
        this.doNotLeaveBackslashAlone = value;
    }

    /**
     * Gets the value of the ulTrailSpace property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getUlTrailSpace() {
        return ulTrailSpace;
    }

    /**
     * Sets the value of the ulTrailSpace property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setUlTrailSpace(BooleanDefaultTrue value) {
        this.ulTrailSpace = value;
    }

    /**
     * Gets the value of the doNotExpandShiftReturn property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getDoNotExpandShiftReturn() {
        return doNotExpandShiftReturn;
    }

    /**
     * Sets the value of the doNotExpandShiftReturn property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setDoNotExpandShiftReturn(BooleanDefaultTrue value) {
        this.doNotExpandShiftReturn = value;
    }

    /**
     * Gets the value of the spacingInWholePoints property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getSpacingInWholePoints() {
        return spacingInWholePoints;
    }

    /**
     * Sets the value of the spacingInWholePoints property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setSpacingInWholePoints(BooleanDefaultTrue value) {
        this.spacingInWholePoints = value;
    }

    /**
     * Gets the value of the lineWrapLikeWord6 property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getLineWrapLikeWord6() {
        return lineWrapLikeWord6;
    }

    /**
     * Sets the value of the lineWrapLikeWord6 property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setLineWrapLikeWord6(BooleanDefaultTrue value) {
        this.lineWrapLikeWord6 = value;
    }

    /**
     * Gets the value of the printBodyTextBeforeHeader property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getPrintBodyTextBeforeHeader() {
        return printBodyTextBeforeHeader;
    }

    /**
     * Sets the value of the printBodyTextBeforeHeader property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setPrintBodyTextBeforeHeader(BooleanDefaultTrue value) {
        this.printBodyTextBeforeHeader = value;
    }

    /**
     * Gets the value of the printColBlack property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getPrintColBlack() {
        return printColBlack;
    }

    /**
     * Sets the value of the printColBlack property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setPrintColBlack(BooleanDefaultTrue value) {
        this.printColBlack = value;
    }

    /**
     * Gets the value of the wpSpaceWidth property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getWpSpaceWidth() {
        return wpSpaceWidth;
    }

    /**
     * Sets the value of the wpSpaceWidth property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setWpSpaceWidth(BooleanDefaultTrue value) {
        this.wpSpaceWidth = value;
    }

    /**
     * Gets the value of the showBreaksInFrames property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getShowBreaksInFrames() {
        return showBreaksInFrames;
    }

    /**
     * Sets the value of the showBreaksInFrames property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setShowBreaksInFrames(BooleanDefaultTrue value) {
        this.showBreaksInFrames = value;
    }

    /**
     * Gets the value of the subFontBySize property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getSubFontBySize() {
        return subFontBySize;
    }

    /**
     * Sets the value of the subFontBySize property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setSubFontBySize(BooleanDefaultTrue value) {
        this.subFontBySize = value;
    }

    /**
     * Gets the value of the suppressBottomSpacing property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getSuppressBottomSpacing() {
        return suppressBottomSpacing;
    }

    /**
     * Sets the value of the suppressBottomSpacing property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setSuppressBottomSpacing(BooleanDefaultTrue value) {
        this.suppressBottomSpacing = value;
    }

    /**
     * Gets the value of the suppressTopSpacing property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getSuppressTopSpacing() {
        return suppressTopSpacing;
    }

    /**
     * Sets the value of the suppressTopSpacing property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setSuppressTopSpacing(BooleanDefaultTrue value) {
        this.suppressTopSpacing = value;
    }

    /**
     * Gets the value of the suppressSpacingAtTopOfPage property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getSuppressSpacingAtTopOfPage() {
        return suppressSpacingAtTopOfPage;
    }

    /**
     * Sets the value of the suppressSpacingAtTopOfPage property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setSuppressSpacingAtTopOfPage(BooleanDefaultTrue value) {
        this.suppressSpacingAtTopOfPage = value;
    }

    /**
     * Gets the value of the suppressTopSpacingWP property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getSuppressTopSpacingWP() {
        return suppressTopSpacingWP;
    }

    /**
     * Sets the value of the suppressTopSpacingWP property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setSuppressTopSpacingWP(BooleanDefaultTrue value) {
        this.suppressTopSpacingWP = value;
    }

    /**
     * Gets the value of the suppressSpBfAfterPgBrk property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getSuppressSpBfAfterPgBrk() {
        return suppressSpBfAfterPgBrk;
    }

    /**
     * Sets the value of the suppressSpBfAfterPgBrk property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setSuppressSpBfAfterPgBrk(BooleanDefaultTrue value) {
        this.suppressSpBfAfterPgBrk = value;
    }

    /**
     * Gets the value of the swapBordersFacingPages property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getSwapBordersFacingPages() {
        return swapBordersFacingPages;
    }

    /**
     * Sets the value of the swapBordersFacingPages property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setSwapBordersFacingPages(BooleanDefaultTrue value) {
        this.swapBordersFacingPages = value;
    }

    /**
     * Gets the value of the convMailMergeEsc property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getConvMailMergeEsc() {
        return convMailMergeEsc;
    }

    /**
     * Sets the value of the convMailMergeEsc property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setConvMailMergeEsc(BooleanDefaultTrue value) {
        this.convMailMergeEsc = value;
    }

    /**
     * Gets the value of the truncateFontHeightsLikeWP6 property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getTruncateFontHeightsLikeWP6() {
        return truncateFontHeightsLikeWP6;
    }

    /**
     * Sets the value of the truncateFontHeightsLikeWP6 property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setTruncateFontHeightsLikeWP6(BooleanDefaultTrue value) {
        this.truncateFontHeightsLikeWP6 = value;
    }

    /**
     * Gets the value of the mwSmallCaps property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getMwSmallCaps() {
        return mwSmallCaps;
    }

    /**
     * Sets the value of the mwSmallCaps property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setMwSmallCaps(BooleanDefaultTrue value) {
        this.mwSmallCaps = value;
    }

    /**
     * Gets the value of the usePrinterMetrics property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getUsePrinterMetrics() {
        return usePrinterMetrics;
    }

    /**
     * Sets the value of the usePrinterMetrics property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setUsePrinterMetrics(BooleanDefaultTrue value) {
        this.usePrinterMetrics = value;
    }

    /**
     * Gets the value of the doNotSuppressParagraphBorders property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getDoNotSuppressParagraphBorders() {
        return doNotSuppressParagraphBorders;
    }

    /**
     * Sets the value of the doNotSuppressParagraphBorders property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setDoNotSuppressParagraphBorders(BooleanDefaultTrue value) {
        this.doNotSuppressParagraphBorders = value;
    }

    /**
     * Gets the value of the wrapTrailSpaces property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getWrapTrailSpaces() {
        return wrapTrailSpaces;
    }

    /**
     * Sets the value of the wrapTrailSpaces property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setWrapTrailSpaces(BooleanDefaultTrue value) {
        this.wrapTrailSpaces = value;
    }

    /**
     * Gets the value of the footnoteLayoutLikeWW8 property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getFootnoteLayoutLikeWW8() {
        return footnoteLayoutLikeWW8;
    }

    /**
     * Sets the value of the footnoteLayoutLikeWW8 property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setFootnoteLayoutLikeWW8(BooleanDefaultTrue value) {
        this.footnoteLayoutLikeWW8 = value;
    }

    /**
     * Gets the value of the shapeLayoutLikeWW8 property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getShapeLayoutLikeWW8() {
        return shapeLayoutLikeWW8;
    }

    /**
     * Sets the value of the shapeLayoutLikeWW8 property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setShapeLayoutLikeWW8(BooleanDefaultTrue value) {
        this.shapeLayoutLikeWW8 = value;
    }

    /**
     * Gets the value of the alignTablesRowByRow property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getAlignTablesRowByRow() {
        return alignTablesRowByRow;
    }

    /**
     * Sets the value of the alignTablesRowByRow property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setAlignTablesRowByRow(BooleanDefaultTrue value) {
        this.alignTablesRowByRow = value;
    }

    /**
     * Gets the value of the forgetLastTabAlignment property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getForgetLastTabAlignment() {
        return forgetLastTabAlignment;
    }

    /**
     * Sets the value of the forgetLastTabAlignment property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setForgetLastTabAlignment(BooleanDefaultTrue value) {
        this.forgetLastTabAlignment = value;
    }

    /**
     * Gets the value of the adjustLineHeightInTable property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getAdjustLineHeightInTable() {
        return adjustLineHeightInTable;
    }

    /**
     * Sets the value of the adjustLineHeightInTable property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setAdjustLineHeightInTable(BooleanDefaultTrue value) {
        this.adjustLineHeightInTable = value;
    }

    /**
     * Gets the value of the autoSpaceLikeWord95 property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getAutoSpaceLikeWord95() {
        return autoSpaceLikeWord95;
    }

    /**
     * Sets the value of the autoSpaceLikeWord95 property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setAutoSpaceLikeWord95(BooleanDefaultTrue value) {
        this.autoSpaceLikeWord95 = value;
    }

    /**
     * Gets the value of the noSpaceRaiseLower property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getNoSpaceRaiseLower() {
        return noSpaceRaiseLower;
    }

    /**
     * Sets the value of the noSpaceRaiseLower property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setNoSpaceRaiseLower(BooleanDefaultTrue value) {
        this.noSpaceRaiseLower = value;
    }

    /**
     * Gets the value of the doNotUseHTMLParagraphAutoSpacing property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getDoNotUseHTMLParagraphAutoSpacing() {
        return doNotUseHTMLParagraphAutoSpacing;
    }

    /**
     * Sets the value of the doNotUseHTMLParagraphAutoSpacing property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setDoNotUseHTMLParagraphAutoSpacing(BooleanDefaultTrue value) {
        this.doNotUseHTMLParagraphAutoSpacing = value;
    }

    /**
     * Gets the value of the layoutRawTableWidth property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getLayoutRawTableWidth() {
        return layoutRawTableWidth;
    }

    /**
     * Sets the value of the layoutRawTableWidth property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setLayoutRawTableWidth(BooleanDefaultTrue value) {
        this.layoutRawTableWidth = value;
    }

    /**
     * Gets the value of the layoutTableRowsApart property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getLayoutTableRowsApart() {
        return layoutTableRowsApart;
    }

    /**
     * Sets the value of the layoutTableRowsApart property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setLayoutTableRowsApart(BooleanDefaultTrue value) {
        this.layoutTableRowsApart = value;
    }

    /**
     * Gets the value of the useWord97LineBreakRules property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getUseWord97LineBreakRules() {
        return useWord97LineBreakRules;
    }

    /**
     * Sets the value of the useWord97LineBreakRules property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setUseWord97LineBreakRules(BooleanDefaultTrue value) {
        this.useWord97LineBreakRules = value;
    }

    /**
     * Gets the value of the doNotBreakWrappedTables property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getDoNotBreakWrappedTables() {
        return doNotBreakWrappedTables;
    }

    /**
     * Sets the value of the doNotBreakWrappedTables property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setDoNotBreakWrappedTables(BooleanDefaultTrue value) {
        this.doNotBreakWrappedTables = value;
    }

    /**
     * Gets the value of the doNotSnapToGridInCell property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getDoNotSnapToGridInCell() {
        return doNotSnapToGridInCell;
    }

    /**
     * Sets the value of the doNotSnapToGridInCell property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setDoNotSnapToGridInCell(BooleanDefaultTrue value) {
        this.doNotSnapToGridInCell = value;
    }

    /**
     * Gets the value of the selectFldWithFirstOrLastChar property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getSelectFldWithFirstOrLastChar() {
        return selectFldWithFirstOrLastChar;
    }

    /**
     * Sets the value of the selectFldWithFirstOrLastChar property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setSelectFldWithFirstOrLastChar(BooleanDefaultTrue value) {
        this.selectFldWithFirstOrLastChar = value;
    }

    /**
     * Gets the value of the applyBreakingRules property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getApplyBreakingRules() {
        return applyBreakingRules;
    }

    /**
     * Sets the value of the applyBreakingRules property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setApplyBreakingRules(BooleanDefaultTrue value) {
        this.applyBreakingRules = value;
    }

    /**
     * Gets the value of the doNotWrapTextWithPunct property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getDoNotWrapTextWithPunct() {
        return doNotWrapTextWithPunct;
    }

    /**
     * Sets the value of the doNotWrapTextWithPunct property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setDoNotWrapTextWithPunct(BooleanDefaultTrue value) {
        this.doNotWrapTextWithPunct = value;
    }

    /**
     * Gets the value of the doNotUseEastAsianBreakRules property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getDoNotUseEastAsianBreakRules() {
        return doNotUseEastAsianBreakRules;
    }

    /**
     * Sets the value of the doNotUseEastAsianBreakRules property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setDoNotUseEastAsianBreakRules(BooleanDefaultTrue value) {
        this.doNotUseEastAsianBreakRules = value;
    }

    /**
     * Gets the value of the useWord2002TableStyleRules property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getUseWord2002TableStyleRules() {
        return useWord2002TableStyleRules;
    }

    /**
     * Sets the value of the useWord2002TableStyleRules property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setUseWord2002TableStyleRules(BooleanDefaultTrue value) {
        this.useWord2002TableStyleRules = value;
    }

    /**
     * Gets the value of the growAutofit property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getGrowAutofit() {
        return growAutofit;
    }

    /**
     * Sets the value of the growAutofit property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setGrowAutofit(BooleanDefaultTrue value) {
        this.growAutofit = value;
    }

    /**
     * Gets the value of the useFELayout property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getUseFELayout() {
        return useFELayout;
    }

    /**
     * Sets the value of the useFELayout property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setUseFELayout(BooleanDefaultTrue value) {
        this.useFELayout = value;
    }

    /**
     * Gets the value of the useNormalStyleForList property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getUseNormalStyleForList() {
        return useNormalStyleForList;
    }

    /**
     * Sets the value of the useNormalStyleForList property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setUseNormalStyleForList(BooleanDefaultTrue value) {
        this.useNormalStyleForList = value;
    }

    /**
     * Gets the value of the doNotUseIndentAsNumberingTabStop property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getDoNotUseIndentAsNumberingTabStop() {
        return doNotUseIndentAsNumberingTabStop;
    }

    /**
     * Sets the value of the doNotUseIndentAsNumberingTabStop property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setDoNotUseIndentAsNumberingTabStop(BooleanDefaultTrue value) {
        this.doNotUseIndentAsNumberingTabStop = value;
    }

    /**
     * Gets the value of the useAltKinsokuLineBreakRules property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getUseAltKinsokuLineBreakRules() {
        return useAltKinsokuLineBreakRules;
    }

    /**
     * Sets the value of the useAltKinsokuLineBreakRules property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setUseAltKinsokuLineBreakRules(BooleanDefaultTrue value) {
        this.useAltKinsokuLineBreakRules = value;
    }

    /**
     * Gets the value of the allowSpaceOfSameStyleInTable property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getAllowSpaceOfSameStyleInTable() {
        return allowSpaceOfSameStyleInTable;
    }

    /**
     * Sets the value of the allowSpaceOfSameStyleInTable property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setAllowSpaceOfSameStyleInTable(BooleanDefaultTrue value) {
        this.allowSpaceOfSameStyleInTable = value;
    }

    /**
     * Gets the value of the doNotSuppressIndentation property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getDoNotSuppressIndentation() {
        return doNotSuppressIndentation;
    }

    /**
     * Sets the value of the doNotSuppressIndentation property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setDoNotSuppressIndentation(BooleanDefaultTrue value) {
        this.doNotSuppressIndentation = value;
    }

    /**
     * Gets the value of the doNotAutofitConstrainedTables property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getDoNotAutofitConstrainedTables() {
        return doNotAutofitConstrainedTables;
    }

    /**
     * Sets the value of the doNotAutofitConstrainedTables property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setDoNotAutofitConstrainedTables(BooleanDefaultTrue value) {
        this.doNotAutofitConstrainedTables = value;
    }

    /**
     * Gets the value of the autofitToFirstFixedWidthCell property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getAutofitToFirstFixedWidthCell() {
        return autofitToFirstFixedWidthCell;
    }

    /**
     * Sets the value of the autofitToFirstFixedWidthCell property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setAutofitToFirstFixedWidthCell(BooleanDefaultTrue value) {
        this.autofitToFirstFixedWidthCell = value;
    }

    /**
     * Gets the value of the underlineTabInNumList property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getUnderlineTabInNumList() {
        return underlineTabInNumList;
    }

    /**
     * Sets the value of the underlineTabInNumList property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setUnderlineTabInNumList(BooleanDefaultTrue value) {
        this.underlineTabInNumList = value;
    }

    /**
     * Gets the value of the displayHangulFixedWidth property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getDisplayHangulFixedWidth() {
        return displayHangulFixedWidth;
    }

    /**
     * Sets the value of the displayHangulFixedWidth property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setDisplayHangulFixedWidth(BooleanDefaultTrue value) {
        this.displayHangulFixedWidth = value;
    }

    /**
     * Gets the value of the splitPgBreakAndParaMark property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getSplitPgBreakAndParaMark() {
        return splitPgBreakAndParaMark;
    }

    /**
     * Sets the value of the splitPgBreakAndParaMark property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setSplitPgBreakAndParaMark(BooleanDefaultTrue value) {
        this.splitPgBreakAndParaMark = value;
    }

    /**
     * Gets the value of the doNotVertAlignCellWithSp property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getDoNotVertAlignCellWithSp() {
        return doNotVertAlignCellWithSp;
    }

    /**
     * Sets the value of the doNotVertAlignCellWithSp property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setDoNotVertAlignCellWithSp(BooleanDefaultTrue value) {
        this.doNotVertAlignCellWithSp = value;
    }

    /**
     * Gets the value of the doNotBreakConstrainedForcedTable property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getDoNotBreakConstrainedForcedTable() {
        return doNotBreakConstrainedForcedTable;
    }

    /**
     * Sets the value of the doNotBreakConstrainedForcedTable property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setDoNotBreakConstrainedForcedTable(BooleanDefaultTrue value) {
        this.doNotBreakConstrainedForcedTable = value;
    }

    /**
     * Gets the value of the doNotVertAlignInTxbx property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getDoNotVertAlignInTxbx() {
        return doNotVertAlignInTxbx;
    }

    /**
     * Sets the value of the doNotVertAlignInTxbx property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setDoNotVertAlignInTxbx(BooleanDefaultTrue value) {
        this.doNotVertAlignInTxbx = value;
    }

    /**
     * Gets the value of the useAnsiKerningPairs property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getUseAnsiKerningPairs() {
        return useAnsiKerningPairs;
    }

    /**
     * Sets the value of the useAnsiKerningPairs property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setUseAnsiKerningPairs(BooleanDefaultTrue value) {
        this.useAnsiKerningPairs = value;
    }

    /**
     * Gets the value of the cachedColBalance property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getCachedColBalance() {
        return cachedColBalance;
    }

    /**
     * Sets the value of the cachedColBalance property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setCachedColBalance(BooleanDefaultTrue value) {
        this.cachedColBalance = value;
    }

    /**
     * Gets the value of the compatSetting property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the compatSetting property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCompatSetting().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTCompatSetting }
     * 
     * 
     */
    public List<CTCompatSetting> getCompatSetting() {
        if (compatSetting == null) {
            compatSetting = new ArrayListWml<CTCompatSetting>(this);
        }
        return this.compatSetting;
    }
    
    public CTCompatSetting getCompatSetting(String name, String uri) {
    	
    	for (CTCompatSetting setting : getCompatSetting() ) {
    		
    		if (name.equals(setting.getName())
    				&& uri.equals(setting.getUri()) ) {
    			return setting;
    		}
    	}
    	return null;
    }

    public void setCompatSetting(String name, String uri, String val) {
    	
    	CTCompatSetting setting = getCompatSetting( name,  uri);
    	if (setting==null) {
    		setting = new CTCompatSetting();
    		getCompatSetting().add(setting);
    	}
		setting.setName(name);
		setting.setUri(uri);
		setting.setVal(val);
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
