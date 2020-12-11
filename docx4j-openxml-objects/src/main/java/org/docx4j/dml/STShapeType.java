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


package org.docx4j.dml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_ShapeType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_ShapeType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="line"/&gt;
 *     &lt;enumeration value="lineInv"/&gt;
 *     &lt;enumeration value="triangle"/&gt;
 *     &lt;enumeration value="rtTriangle"/&gt;
 *     &lt;enumeration value="rect"/&gt;
 *     &lt;enumeration value="diamond"/&gt;
 *     &lt;enumeration value="parallelogram"/&gt;
 *     &lt;enumeration value="trapezoid"/&gt;
 *     &lt;enumeration value="nonIsoscelesTrapezoid"/&gt;
 *     &lt;enumeration value="pentagon"/&gt;
 *     &lt;enumeration value="hexagon"/&gt;
 *     &lt;enumeration value="heptagon"/&gt;
 *     &lt;enumeration value="octagon"/&gt;
 *     &lt;enumeration value="decagon"/&gt;
 *     &lt;enumeration value="dodecagon"/&gt;
 *     &lt;enumeration value="star4"/&gt;
 *     &lt;enumeration value="star5"/&gt;
 *     &lt;enumeration value="star6"/&gt;
 *     &lt;enumeration value="star7"/&gt;
 *     &lt;enumeration value="star8"/&gt;
 *     &lt;enumeration value="star10"/&gt;
 *     &lt;enumeration value="star12"/&gt;
 *     &lt;enumeration value="star16"/&gt;
 *     &lt;enumeration value="star24"/&gt;
 *     &lt;enumeration value="star32"/&gt;
 *     &lt;enumeration value="roundRect"/&gt;
 *     &lt;enumeration value="round1Rect"/&gt;
 *     &lt;enumeration value="round2SameRect"/&gt;
 *     &lt;enumeration value="round2DiagRect"/&gt;
 *     &lt;enumeration value="snipRoundRect"/&gt;
 *     &lt;enumeration value="snip1Rect"/&gt;
 *     &lt;enumeration value="snip2SameRect"/&gt;
 *     &lt;enumeration value="snip2DiagRect"/&gt;
 *     &lt;enumeration value="plaque"/&gt;
 *     &lt;enumeration value="ellipse"/&gt;
 *     &lt;enumeration value="teardrop"/&gt;
 *     &lt;enumeration value="homePlate"/&gt;
 *     &lt;enumeration value="chevron"/&gt;
 *     &lt;enumeration value="pieWedge"/&gt;
 *     &lt;enumeration value="pie"/&gt;
 *     &lt;enumeration value="blockArc"/&gt;
 *     &lt;enumeration value="donut"/&gt;
 *     &lt;enumeration value="noSmoking"/&gt;
 *     &lt;enumeration value="rightArrow"/&gt;
 *     &lt;enumeration value="leftArrow"/&gt;
 *     &lt;enumeration value="upArrow"/&gt;
 *     &lt;enumeration value="downArrow"/&gt;
 *     &lt;enumeration value="stripedRightArrow"/&gt;
 *     &lt;enumeration value="notchedRightArrow"/&gt;
 *     &lt;enumeration value="bentUpArrow"/&gt;
 *     &lt;enumeration value="leftRightArrow"/&gt;
 *     &lt;enumeration value="upDownArrow"/&gt;
 *     &lt;enumeration value="leftUpArrow"/&gt;
 *     &lt;enumeration value="leftRightUpArrow"/&gt;
 *     &lt;enumeration value="quadArrow"/&gt;
 *     &lt;enumeration value="leftArrowCallout"/&gt;
 *     &lt;enumeration value="rightArrowCallout"/&gt;
 *     &lt;enumeration value="upArrowCallout"/&gt;
 *     &lt;enumeration value="downArrowCallout"/&gt;
 *     &lt;enumeration value="leftRightArrowCallout"/&gt;
 *     &lt;enumeration value="upDownArrowCallout"/&gt;
 *     &lt;enumeration value="quadArrowCallout"/&gt;
 *     &lt;enumeration value="bentArrow"/&gt;
 *     &lt;enumeration value="uturnArrow"/&gt;
 *     &lt;enumeration value="circularArrow"/&gt;
 *     &lt;enumeration value="leftCircularArrow"/&gt;
 *     &lt;enumeration value="leftRightCircularArrow"/&gt;
 *     &lt;enumeration value="curvedRightArrow"/&gt;
 *     &lt;enumeration value="curvedLeftArrow"/&gt;
 *     &lt;enumeration value="curvedUpArrow"/&gt;
 *     &lt;enumeration value="curvedDownArrow"/&gt;
 *     &lt;enumeration value="swooshArrow"/&gt;
 *     &lt;enumeration value="cube"/&gt;
 *     &lt;enumeration value="can"/&gt;
 *     &lt;enumeration value="lightningBolt"/&gt;
 *     &lt;enumeration value="heart"/&gt;
 *     &lt;enumeration value="sun"/&gt;
 *     &lt;enumeration value="moon"/&gt;
 *     &lt;enumeration value="smileyFace"/&gt;
 *     &lt;enumeration value="irregularSeal1"/&gt;
 *     &lt;enumeration value="irregularSeal2"/&gt;
 *     &lt;enumeration value="foldedCorner"/&gt;
 *     &lt;enumeration value="bevel"/&gt;
 *     &lt;enumeration value="frame"/&gt;
 *     &lt;enumeration value="halfFrame"/&gt;
 *     &lt;enumeration value="corner"/&gt;
 *     &lt;enumeration value="diagStripe"/&gt;
 *     &lt;enumeration value="chord"/&gt;
 *     &lt;enumeration value="arc"/&gt;
 *     &lt;enumeration value="leftBracket"/&gt;
 *     &lt;enumeration value="rightBracket"/&gt;
 *     &lt;enumeration value="leftBrace"/&gt;
 *     &lt;enumeration value="rightBrace"/&gt;
 *     &lt;enumeration value="bracketPair"/&gt;
 *     &lt;enumeration value="bracePair"/&gt;
 *     &lt;enumeration value="straightConnector1"/&gt;
 *     &lt;enumeration value="bentConnector2"/&gt;
 *     &lt;enumeration value="bentConnector3"/&gt;
 *     &lt;enumeration value="bentConnector4"/&gt;
 *     &lt;enumeration value="bentConnector5"/&gt;
 *     &lt;enumeration value="curvedConnector2"/&gt;
 *     &lt;enumeration value="curvedConnector3"/&gt;
 *     &lt;enumeration value="curvedConnector4"/&gt;
 *     &lt;enumeration value="curvedConnector5"/&gt;
 *     &lt;enumeration value="callout1"/&gt;
 *     &lt;enumeration value="callout2"/&gt;
 *     &lt;enumeration value="callout3"/&gt;
 *     &lt;enumeration value="accentCallout1"/&gt;
 *     &lt;enumeration value="accentCallout2"/&gt;
 *     &lt;enumeration value="accentCallout3"/&gt;
 *     &lt;enumeration value="borderCallout1"/&gt;
 *     &lt;enumeration value="borderCallout2"/&gt;
 *     &lt;enumeration value="borderCallout3"/&gt;
 *     &lt;enumeration value="accentBorderCallout1"/&gt;
 *     &lt;enumeration value="accentBorderCallout2"/&gt;
 *     &lt;enumeration value="accentBorderCallout3"/&gt;
 *     &lt;enumeration value="wedgeRectCallout"/&gt;
 *     &lt;enumeration value="wedgeRoundRectCallout"/&gt;
 *     &lt;enumeration value="wedgeEllipseCallout"/&gt;
 *     &lt;enumeration value="cloudCallout"/&gt;
 *     &lt;enumeration value="cloud"/&gt;
 *     &lt;enumeration value="ribbon"/&gt;
 *     &lt;enumeration value="ribbon2"/&gt;
 *     &lt;enumeration value="ellipseRibbon"/&gt;
 *     &lt;enumeration value="ellipseRibbon2"/&gt;
 *     &lt;enumeration value="leftRightRibbon"/&gt;
 *     &lt;enumeration value="verticalScroll"/&gt;
 *     &lt;enumeration value="horizontalScroll"/&gt;
 *     &lt;enumeration value="wave"/&gt;
 *     &lt;enumeration value="doubleWave"/&gt;
 *     &lt;enumeration value="plus"/&gt;
 *     &lt;enumeration value="flowChartProcess"/&gt;
 *     &lt;enumeration value="flowChartDecision"/&gt;
 *     &lt;enumeration value="flowChartInputOutput"/&gt;
 *     &lt;enumeration value="flowChartPredefinedProcess"/&gt;
 *     &lt;enumeration value="flowChartInternalStorage"/&gt;
 *     &lt;enumeration value="flowChartDocument"/&gt;
 *     &lt;enumeration value="flowChartMultidocument"/&gt;
 *     &lt;enumeration value="flowChartTerminator"/&gt;
 *     &lt;enumeration value="flowChartPreparation"/&gt;
 *     &lt;enumeration value="flowChartManualInput"/&gt;
 *     &lt;enumeration value="flowChartManualOperation"/&gt;
 *     &lt;enumeration value="flowChartConnector"/&gt;
 *     &lt;enumeration value="flowChartPunchedCard"/&gt;
 *     &lt;enumeration value="flowChartPunchedTape"/&gt;
 *     &lt;enumeration value="flowChartSummingJunction"/&gt;
 *     &lt;enumeration value="flowChartOr"/&gt;
 *     &lt;enumeration value="flowChartCollate"/&gt;
 *     &lt;enumeration value="flowChartSort"/&gt;
 *     &lt;enumeration value="flowChartExtract"/&gt;
 *     &lt;enumeration value="flowChartMerge"/&gt;
 *     &lt;enumeration value="flowChartOfflineStorage"/&gt;
 *     &lt;enumeration value="flowChartOnlineStorage"/&gt;
 *     &lt;enumeration value="flowChartMagneticTape"/&gt;
 *     &lt;enumeration value="flowChartMagneticDisk"/&gt;
 *     &lt;enumeration value="flowChartMagneticDrum"/&gt;
 *     &lt;enumeration value="flowChartDisplay"/&gt;
 *     &lt;enumeration value="flowChartDelay"/&gt;
 *     &lt;enumeration value="flowChartAlternateProcess"/&gt;
 *     &lt;enumeration value="flowChartOffpageConnector"/&gt;
 *     &lt;enumeration value="actionButtonBlank"/&gt;
 *     &lt;enumeration value="actionButtonHome"/&gt;
 *     &lt;enumeration value="actionButtonHelp"/&gt;
 *     &lt;enumeration value="actionButtonInformation"/&gt;
 *     &lt;enumeration value="actionButtonForwardNext"/&gt;
 *     &lt;enumeration value="actionButtonBackPrevious"/&gt;
 *     &lt;enumeration value="actionButtonEnd"/&gt;
 *     &lt;enumeration value="actionButtonBeginning"/&gt;
 *     &lt;enumeration value="actionButtonReturn"/&gt;
 *     &lt;enumeration value="actionButtonDocument"/&gt;
 *     &lt;enumeration value="actionButtonSound"/&gt;
 *     &lt;enumeration value="actionButtonMovie"/&gt;
 *     &lt;enumeration value="gear6"/&gt;
 *     &lt;enumeration value="gear9"/&gt;
 *     &lt;enumeration value="funnel"/&gt;
 *     &lt;enumeration value="mathPlus"/&gt;
 *     &lt;enumeration value="mathMinus"/&gt;
 *     &lt;enumeration value="mathMultiply"/&gt;
 *     &lt;enumeration value="mathDivide"/&gt;
 *     &lt;enumeration value="mathEqual"/&gt;
 *     &lt;enumeration value="mathNotEqual"/&gt;
 *     &lt;enumeration value="cornerTabs"/&gt;
 *     &lt;enumeration value="squareTabs"/&gt;
 *     &lt;enumeration value="plaqueTabs"/&gt;
 *     &lt;enumeration value="chartX"/&gt;
 *     &lt;enumeration value="chartStar"/&gt;
 *     &lt;enumeration value="chartPlus"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_ShapeType")
@XmlEnum
public enum STShapeType {


    /**
     * Line Shape
     * 
     */
    @XmlEnumValue("line")
    LINE("line"),

    /**
     * Line Inverse Shape
     * 
     */
    @XmlEnumValue("lineInv")
    LINE_INV("lineInv"),

    /**
     * Triangle Shape
     * 
     */
    @XmlEnumValue("triangle")
    TRIANGLE("triangle"),

    /**
     * Right Triangle Shape
     * 
     */
    @XmlEnumValue("rtTriangle")
    RT_TRIANGLE("rtTriangle"),

    /**
     * Rectangle Shape
     * 
     */
    @XmlEnumValue("rect")
    RECT("rect"),

    /**
     * Diamond Shape
     * 
     */
    @XmlEnumValue("diamond")
    DIAMOND("diamond"),

    /**
     * Parallelogram Shape
     * 
     */
    @XmlEnumValue("parallelogram")
    PARALLELOGRAM("parallelogram"),

    /**
     * Trapezoid Shape
     * 
     */
    @XmlEnumValue("trapezoid")
    TRAPEZOID("trapezoid"),

    /**
     * Non-Isosceles Trapezoid Shape
     * 
     */
    @XmlEnumValue("nonIsoscelesTrapezoid")
    NON_ISOSCELES_TRAPEZOID("nonIsoscelesTrapezoid"),

    /**
     * Pentagon Shape
     * 
     */
    @XmlEnumValue("pentagon")
    PENTAGON("pentagon"),

    /**
     * Hexagon Shape
     * 
     */
    @XmlEnumValue("hexagon")
    HEXAGON("hexagon"),

    /**
     * Heptagon Shape
     * 
     */
    @XmlEnumValue("heptagon")
    HEPTAGON("heptagon"),

    /**
     * Octagon Shape
     * 
     */
    @XmlEnumValue("octagon")
    OCTAGON("octagon"),

    /**
     * Decagon Shape
     * 
     */
    @XmlEnumValue("decagon")
    DECAGON("decagon"),

    /**
     * Dodecagon Shape
     * 
     */
    @XmlEnumValue("dodecagon")
    DODECAGON("dodecagon"),

    /**
     * Four Pointed Star Shape
     * 
     */
    @XmlEnumValue("star4")
    STAR_4("star4"),

    /**
     * Five Pointed Star Shape
     * 
     */
    @XmlEnumValue("star5")
    STAR_5("star5"),

    /**
     * Six Pointed Star Shape
     * 
     */
    @XmlEnumValue("star6")
    STAR_6("star6"),

    /**
     * Seven Pointed Star Shape
     * 
     */
    @XmlEnumValue("star7")
    STAR_7("star7"),

    /**
     * Eight Pointed Star Shape
     * 
     */
    @XmlEnumValue("star8")
    STAR_8("star8"),

    /**
     * Ten Pointed Star Shape
     * 
     */
    @XmlEnumValue("star10")
    STAR_10("star10"),

    /**
     * Twelve Pointed Star Shape
     * 
     */
    @XmlEnumValue("star12")
    STAR_12("star12"),

    /**
     * Sixteen Pointed Star Shape
     * 
     */
    @XmlEnumValue("star16")
    STAR_16("star16"),

    /**
     * Twenty Four Pointed Star Shape
     * 
     */
    @XmlEnumValue("star24")
    STAR_24("star24"),

    /**
     * Thirty Two Pointed Star Shape
     * 
     */
    @XmlEnumValue("star32")
    STAR_32("star32"),

    /**
     * Round Corner Rectangle Shape
     * 
     */
    @XmlEnumValue("roundRect")
    ROUND_RECT("roundRect"),

    /**
     * One Round Corner Rectangle Shape
     * 
     */
    @XmlEnumValue("round1Rect")
    ROUND_1_RECT("round1Rect"),

    /**
     * Two Same-side Round Corner Rectangle Shape
     * 
     */
    @XmlEnumValue("round2SameRect")
    ROUND_2_SAME_RECT("round2SameRect"),

    /**
     * Two Diagonal Round Corner Rectangle Shape
     * 
     */
    @XmlEnumValue("round2DiagRect")
    ROUND_2_DIAG_RECT("round2DiagRect"),

    /**
     * One Snip One Round Corner Rectangle Shape
     * 
     */
    @XmlEnumValue("snipRoundRect")
    SNIP_ROUND_RECT("snipRoundRect"),

    /**
     * One Snip Corner Rectangle Shape
     * 
     */
    @XmlEnumValue("snip1Rect")
    SNIP_1_RECT("snip1Rect"),

    /**
     * Two Same-side Snip Corner Rectangle Shape
     * 
     */
    @XmlEnumValue("snip2SameRect")
    SNIP_2_SAME_RECT("snip2SameRect"),

    /**
     * Two Diagonal Snip Corner Rectangle Shape
     * 
     */
    @XmlEnumValue("snip2DiagRect")
    SNIP_2_DIAG_RECT("snip2DiagRect"),

    /**
     * Plaque Shape
     * 
     */
    @XmlEnumValue("plaque")
    PLAQUE("plaque"),

    /**
     * Ellipse Shape
     * 
     */
    @XmlEnumValue("ellipse")
    ELLIPSE("ellipse"),

    /**
     * Teardrop Shape
     * 
     */
    @XmlEnumValue("teardrop")
    TEARDROP("teardrop"),

    /**
     * Home Plate Shape
     * 
     */
    @XmlEnumValue("homePlate")
    HOME_PLATE("homePlate"),

    /**
     * Chevron Shape
     * 
     */
    @XmlEnumValue("chevron")
    CHEVRON("chevron"),

    /**
     * Pie Wedge Shape
     * 
     */
    @XmlEnumValue("pieWedge")
    PIE_WEDGE("pieWedge"),

    /**
     * Pie Shape
     * 
     */
    @XmlEnumValue("pie")
    PIE("pie"),

    /**
     * Block Arc Shape
     * 
     */
    @XmlEnumValue("blockArc")
    BLOCK_ARC("blockArc"),

    /**
     * Donut Shape
     * 
     */
    @XmlEnumValue("donut")
    DONUT("donut"),

    /**
     * No Smoking Shape
     * 
     */
    @XmlEnumValue("noSmoking")
    NO_SMOKING("noSmoking"),

    /**
     * Right Arrow Shape
     * 
     */
    @XmlEnumValue("rightArrow")
    RIGHT_ARROW("rightArrow"),

    /**
     * Left Arrow Shape
     * 
     */
    @XmlEnumValue("leftArrow")
    LEFT_ARROW("leftArrow"),

    /**
     * Up Arrow Shape
     * 
     */
    @XmlEnumValue("upArrow")
    UP_ARROW("upArrow"),

    /**
     * Down Arrow Shape
     * 
     */
    @XmlEnumValue("downArrow")
    DOWN_ARROW("downArrow"),

    /**
     * Striped Right Arrow Shape
     * 
     */
    @XmlEnumValue("stripedRightArrow")
    STRIPED_RIGHT_ARROW("stripedRightArrow"),

    /**
     * Notched Right Arrow Shape
     * 
     */
    @XmlEnumValue("notchedRightArrow")
    NOTCHED_RIGHT_ARROW("notchedRightArrow"),

    /**
     * Bent Up Arrow Shape
     * 
     */
    @XmlEnumValue("bentUpArrow")
    BENT_UP_ARROW("bentUpArrow"),

    /**
     * Left Right Arrow Shape
     * 
     */
    @XmlEnumValue("leftRightArrow")
    LEFT_RIGHT_ARROW("leftRightArrow"),

    /**
     * Up Down Arrow Shape
     * 
     */
    @XmlEnumValue("upDownArrow")
    UP_DOWN_ARROW("upDownArrow"),

    /**
     * Left Up Arrow Shape
     * 
     */
    @XmlEnumValue("leftUpArrow")
    LEFT_UP_ARROW("leftUpArrow"),

    /**
     * Left Right Up Arrow Shape
     * 
     */
    @XmlEnumValue("leftRightUpArrow")
    LEFT_RIGHT_UP_ARROW("leftRightUpArrow"),

    /**
     * Quad-Arrow Shape
     * 
     */
    @XmlEnumValue("quadArrow")
    QUAD_ARROW("quadArrow"),

    /**
     * Callout Left Arrow Shape
     * 
     */
    @XmlEnumValue("leftArrowCallout")
    LEFT_ARROW_CALLOUT("leftArrowCallout"),

    /**
     * Callout Right Arrow Shape
     * 
     */
    @XmlEnumValue("rightArrowCallout")
    RIGHT_ARROW_CALLOUT("rightArrowCallout"),

    /**
     * Callout Up Arrow Shape
     * 
     */
    @XmlEnumValue("upArrowCallout")
    UP_ARROW_CALLOUT("upArrowCallout"),

    /**
     * Callout Down Arrow Shape
     * 
     */
    @XmlEnumValue("downArrowCallout")
    DOWN_ARROW_CALLOUT("downArrowCallout"),

    /**
     * Callout Left Right Arrow Shape
     * 
     */
    @XmlEnumValue("leftRightArrowCallout")
    LEFT_RIGHT_ARROW_CALLOUT("leftRightArrowCallout"),

    /**
     * Callout Up Down Arrow Shape
     * 
     */
    @XmlEnumValue("upDownArrowCallout")
    UP_DOWN_ARROW_CALLOUT("upDownArrowCallout"),

    /**
     * Callout Quad-Arrow Shape
     * 
     */
    @XmlEnumValue("quadArrowCallout")
    QUAD_ARROW_CALLOUT("quadArrowCallout"),

    /**
     * Bent Arrow Shape
     * 
     */
    @XmlEnumValue("bentArrow")
    BENT_ARROW("bentArrow"),

    /**
     * U-Turn Arrow Shape
     * 
     */
    @XmlEnumValue("uturnArrow")
    UTURN_ARROW("uturnArrow"),

    /**
     * Circular Arrow Shape
     * 
     */
    @XmlEnumValue("circularArrow")
    CIRCULAR_ARROW("circularArrow"),

    /**
     * Left Circular Arrow Shape
     * 
     */
    @XmlEnumValue("leftCircularArrow")
    LEFT_CIRCULAR_ARROW("leftCircularArrow"),

    /**
     * Left Right Circular Arrow Shape
     * 
     */
    @XmlEnumValue("leftRightCircularArrow")
    LEFT_RIGHT_CIRCULAR_ARROW("leftRightCircularArrow"),

    /**
     * Curved Right Arrow Shape
     * 
     */
    @XmlEnumValue("curvedRightArrow")
    CURVED_RIGHT_ARROW("curvedRightArrow"),

    /**
     * Curved Left Arrow Shape
     * 
     */
    @XmlEnumValue("curvedLeftArrow")
    CURVED_LEFT_ARROW("curvedLeftArrow"),

    /**
     * Curved Up Arrow Shape
     * 
     */
    @XmlEnumValue("curvedUpArrow")
    CURVED_UP_ARROW("curvedUpArrow"),

    /**
     * Curved Down Arrow Shape
     * 
     */
    @XmlEnumValue("curvedDownArrow")
    CURVED_DOWN_ARROW("curvedDownArrow"),

    /**
     * Swoosh Arrow Shape
     * 
     */
    @XmlEnumValue("swooshArrow")
    SWOOSH_ARROW("swooshArrow"),

    /**
     * Cube Shape
     * 
     */
    @XmlEnumValue("cube")
    CUBE("cube"),

    /**
     * Can Shape
     * 
     */
    @XmlEnumValue("can")
    CAN("can"),

    /**
     * Lightning Bolt Shape
     * 
     */
    @XmlEnumValue("lightningBolt")
    LIGHTNING_BOLT("lightningBolt"),

    /**
     * Heart Shape
     * 
     */
    @XmlEnumValue("heart")
    HEART("heart"),

    /**
     * Sun Shape
     * 
     */
    @XmlEnumValue("sun")
    SUN("sun"),

    /**
     * Moon Shape
     * 
     */
    @XmlEnumValue("moon")
    MOON("moon"),

    /**
     * Smiley Face Shape
     * 
     */
    @XmlEnumValue("smileyFace")
    SMILEY_FACE("smileyFace"),

    /**
     * Irregular Seal 1 Shape
     * 
     */
    @XmlEnumValue("irregularSeal1")
    IRREGULAR_SEAL_1("irregularSeal1"),

    /**
     * Irregular Seal 2 Shape
     * 
     */
    @XmlEnumValue("irregularSeal2")
    IRREGULAR_SEAL_2("irregularSeal2"),

    /**
     * Folded Corner Shape
     * 
     */
    @XmlEnumValue("foldedCorner")
    FOLDED_CORNER("foldedCorner"),

    /**
     * Bevel Shape
     * 
     */
    @XmlEnumValue("bevel")
    BEVEL("bevel"),

    /**
     * Frame Shape
     * 
     */
    @XmlEnumValue("frame")
    FRAME("frame"),

    /**
     * Half Frame Shape
     * 
     */
    @XmlEnumValue("halfFrame")
    HALF_FRAME("halfFrame"),

    /**
     * Corner Shape
     * 
     */
    @XmlEnumValue("corner")
    CORNER("corner"),

    /**
     * Diagonal Stripe Shape
     * 
     */
    @XmlEnumValue("diagStripe")
    DIAG_STRIPE("diagStripe"),

    /**
     * Chord Shape
     * 
     */
    @XmlEnumValue("chord")
    CHORD("chord"),

    /**
     * Curved Arc Shape
     * 
     */
    @XmlEnumValue("arc")
    ARC("arc"),

    /**
     * Left Bracket Shape
     * 
     */
    @XmlEnumValue("leftBracket")
    LEFT_BRACKET("leftBracket"),

    /**
     * Right Bracket Shape
     * 
     */
    @XmlEnumValue("rightBracket")
    RIGHT_BRACKET("rightBracket"),

    /**
     * Left Brace Shape
     * 
     */
    @XmlEnumValue("leftBrace")
    LEFT_BRACE("leftBrace"),

    /**
     * Right Brace Shape
     * 
     */
    @XmlEnumValue("rightBrace")
    RIGHT_BRACE("rightBrace"),

    /**
     * Bracket Pair Shape
     * 
     */
    @XmlEnumValue("bracketPair")
    BRACKET_PAIR("bracketPair"),

    /**
     * Brace Pair Shape
     * 
     */
    @XmlEnumValue("bracePair")
    BRACE_PAIR("bracePair"),

    /**
     * Straight Connector 1 Shape
     * 
     */
    @XmlEnumValue("straightConnector1")
    STRAIGHT_CONNECTOR_1("straightConnector1"),

    /**
     * Bent Connector 2 Shape
     * 
     */
    @XmlEnumValue("bentConnector2")
    BENT_CONNECTOR_2("bentConnector2"),

    /**
     * Bent Connector 3 Shape
     * 
     */
    @XmlEnumValue("bentConnector3")
    BENT_CONNECTOR_3("bentConnector3"),

    /**
     * Bent Connector 4 Shape
     * 
     */
    @XmlEnumValue("bentConnector4")
    BENT_CONNECTOR_4("bentConnector4"),

    /**
     * Bent Connector 5 Shape
     * 
     */
    @XmlEnumValue("bentConnector5")
    BENT_CONNECTOR_5("bentConnector5"),

    /**
     * Curved Connector 2 Shape
     * 
     */
    @XmlEnumValue("curvedConnector2")
    CURVED_CONNECTOR_2("curvedConnector2"),

    /**
     * Curved Connector 3 Shape
     * 
     */
    @XmlEnumValue("curvedConnector3")
    CURVED_CONNECTOR_3("curvedConnector3"),

    /**
     * Curved Connector 4 Shape
     * 
     */
    @XmlEnumValue("curvedConnector4")
    CURVED_CONNECTOR_4("curvedConnector4"),

    /**
     * Curved Connector 5 Shape
     * 
     */
    @XmlEnumValue("curvedConnector5")
    CURVED_CONNECTOR_5("curvedConnector5"),

    /**
     * Callout 1 Shape
     * 
     */
    @XmlEnumValue("callout1")
    CALLOUT_1("callout1"),

    /**
     * Callout 2 Shape
     * 
     */
    @XmlEnumValue("callout2")
    CALLOUT_2("callout2"),

    /**
     * Callout 3 Shape
     * 
     */
    @XmlEnumValue("callout3")
    CALLOUT_3("callout3"),

    /**
     * Callout 1 Shape
     * 
     */
    @XmlEnumValue("accentCallout1")
    ACCENT_CALLOUT_1("accentCallout1"),

    /**
     * Callout 2 Shape
     * 
     */
    @XmlEnumValue("accentCallout2")
    ACCENT_CALLOUT_2("accentCallout2"),

    /**
     * Callout 3 Shape
     * 
     */
    @XmlEnumValue("accentCallout3")
    ACCENT_CALLOUT_3("accentCallout3"),

    /**
     * Callout 1 with Border Shape
     * 
     */
    @XmlEnumValue("borderCallout1")
    BORDER_CALLOUT_1("borderCallout1"),

    /**
     * Callout 2 with Border Shape
     * 
     */
    @XmlEnumValue("borderCallout2")
    BORDER_CALLOUT_2("borderCallout2"),

    /**
     * Callout 3 with Border Shape
     * 
     */
    @XmlEnumValue("borderCallout3")
    BORDER_CALLOUT_3("borderCallout3"),

    /**
     * Callout 1 with Border and Accent Shape
     * 
     */
    @XmlEnumValue("accentBorderCallout1")
    ACCENT_BORDER_CALLOUT_1("accentBorderCallout1"),

    /**
     * Callout 2 with Border and Accent Shape
     * 
     */
    @XmlEnumValue("accentBorderCallout2")
    ACCENT_BORDER_CALLOUT_2("accentBorderCallout2"),

    /**
     * Callout 3 with Border and Accent Shape
     * 
     */
    @XmlEnumValue("accentBorderCallout3")
    ACCENT_BORDER_CALLOUT_3("accentBorderCallout3"),

    /**
     * Callout Wedge Rectangle Shape
     * 
     */
    @XmlEnumValue("wedgeRectCallout")
    WEDGE_RECT_CALLOUT("wedgeRectCallout"),

    /**
     * Callout Wedge Round Rectangle Shape
     * 
     */
    @XmlEnumValue("wedgeRoundRectCallout")
    WEDGE_ROUND_RECT_CALLOUT("wedgeRoundRectCallout"),

    /**
     * Callout Wedge Ellipse Shape
     * 
     */
    @XmlEnumValue("wedgeEllipseCallout")
    WEDGE_ELLIPSE_CALLOUT("wedgeEllipseCallout"),

    /**
     * Callout Cloud Shape
     * 
     */
    @XmlEnumValue("cloudCallout")
    CLOUD_CALLOUT("cloudCallout"),

    /**
     * Cloud Shape
     * 
     */
    @XmlEnumValue("cloud")
    CLOUD("cloud"),

    /**
     * Ribbon Shape
     * 
     */
    @XmlEnumValue("ribbon")
    RIBBON("ribbon"),

    /**
     * Ribbon 2 Shape
     * 
     */
    @XmlEnumValue("ribbon2")
    RIBBON_2("ribbon2"),

    /**
     * Ellipse Ribbon Shape
     * 
     */
    @XmlEnumValue("ellipseRibbon")
    ELLIPSE_RIBBON("ellipseRibbon"),

    /**
     * Ellipse Ribbon 2 Shape
     * 
     */
    @XmlEnumValue("ellipseRibbon2")
    ELLIPSE_RIBBON_2("ellipseRibbon2"),

    /**
     * Left Right Ribbon Shape
     * 
     */
    @XmlEnumValue("leftRightRibbon")
    LEFT_RIGHT_RIBBON("leftRightRibbon"),

    /**
     * Vertical Scroll Shape
     * 
     */
    @XmlEnumValue("verticalScroll")
    VERTICAL_SCROLL("verticalScroll"),

    /**
     * Horizontal Scroll Shape
     * 
     */
    @XmlEnumValue("horizontalScroll")
    HORIZONTAL_SCROLL("horizontalScroll"),

    /**
     * Wave Shape
     * 
     */
    @XmlEnumValue("wave")
    WAVE("wave"),

    /**
     * Double Wave Shape
     * 
     */
    @XmlEnumValue("doubleWave")
    DOUBLE_WAVE("doubleWave"),

    /**
     * Plus Shape
     * 
     */
    @XmlEnumValue("plus")
    PLUS("plus"),

    /**
     * Process Flow Shape
     * 
     */
    @XmlEnumValue("flowChartProcess")
    FLOW_CHART_PROCESS("flowChartProcess"),

    /**
     * Decision Flow Shape
     * 
     */
    @XmlEnumValue("flowChartDecision")
    FLOW_CHART_DECISION("flowChartDecision"),

    /**
     * Input Output Flow Shape
     * 
     */
    @XmlEnumValue("flowChartInputOutput")
    FLOW_CHART_INPUT_OUTPUT("flowChartInputOutput"),

    /**
     * Predefined Process Flow Shape
     * 
     */
    @XmlEnumValue("flowChartPredefinedProcess")
    FLOW_CHART_PREDEFINED_PROCESS("flowChartPredefinedProcess"),

    /**
     * Internal Storage Flow Shape
     * 
     */
    @XmlEnumValue("flowChartInternalStorage")
    FLOW_CHART_INTERNAL_STORAGE("flowChartInternalStorage"),

    /**
     * Document Flow Shape
     * 
     */
    @XmlEnumValue("flowChartDocument")
    FLOW_CHART_DOCUMENT("flowChartDocument"),

    /**
     * Multi-Document Flow Shape
     * 
     */
    @XmlEnumValue("flowChartMultidocument")
    FLOW_CHART_MULTIDOCUMENT("flowChartMultidocument"),

    /**
     * Terminator Flow Shape
     * 
     */
    @XmlEnumValue("flowChartTerminator")
    FLOW_CHART_TERMINATOR("flowChartTerminator"),

    /**
     * Preparation Flow Shape
     * 
     */
    @XmlEnumValue("flowChartPreparation")
    FLOW_CHART_PREPARATION("flowChartPreparation"),

    /**
     * Manual Input Flow Shape
     * 
     */
    @XmlEnumValue("flowChartManualInput")
    FLOW_CHART_MANUAL_INPUT("flowChartManualInput"),

    /**
     * Manual Operation Flow Shape
     * 
     */
    @XmlEnumValue("flowChartManualOperation")
    FLOW_CHART_MANUAL_OPERATION("flowChartManualOperation"),

    /**
     * Connector Flow Shape
     * 
     */
    @XmlEnumValue("flowChartConnector")
    FLOW_CHART_CONNECTOR("flowChartConnector"),

    /**
     * Punched Card Flow Shape
     * 
     */
    @XmlEnumValue("flowChartPunchedCard")
    FLOW_CHART_PUNCHED_CARD("flowChartPunchedCard"),

    /**
     * Punched Tape Flow Shape
     * 
     */
    @XmlEnumValue("flowChartPunchedTape")
    FLOW_CHART_PUNCHED_TAPE("flowChartPunchedTape"),

    /**
     * Summing Junction Flow Shape
     * 
     */
    @XmlEnumValue("flowChartSummingJunction")
    FLOW_CHART_SUMMING_JUNCTION("flowChartSummingJunction"),

    /**
     * Or Flow Shape
     * 
     */
    @XmlEnumValue("flowChartOr")
    FLOW_CHART_OR("flowChartOr"),

    /**
     * Collate Flow Shape
     * 
     */
    @XmlEnumValue("flowChartCollate")
    FLOW_CHART_COLLATE("flowChartCollate"),

    /**
     * Sort Flow Shape
     * 
     */
    @XmlEnumValue("flowChartSort")
    FLOW_CHART_SORT("flowChartSort"),

    /**
     * Extract Flow Shape
     * 
     */
    @XmlEnumValue("flowChartExtract")
    FLOW_CHART_EXTRACT("flowChartExtract"),

    /**
     * Merge Flow Shape
     * 
     */
    @XmlEnumValue("flowChartMerge")
    FLOW_CHART_MERGE("flowChartMerge"),

    /**
     * Offline Storage Flow Shape
     * 
     */
    @XmlEnumValue("flowChartOfflineStorage")
    FLOW_CHART_OFFLINE_STORAGE("flowChartOfflineStorage"),

    /**
     * Online Storage Flow Shape
     * 
     */
    @XmlEnumValue("flowChartOnlineStorage")
    FLOW_CHART_ONLINE_STORAGE("flowChartOnlineStorage"),

    /**
     * Magnetic Tape Flow Shape
     * 
     */
    @XmlEnumValue("flowChartMagneticTape")
    FLOW_CHART_MAGNETIC_TAPE("flowChartMagneticTape"),

    /**
     * Magnetic Disk Flow Shape
     * 
     */
    @XmlEnumValue("flowChartMagneticDisk")
    FLOW_CHART_MAGNETIC_DISK("flowChartMagneticDisk"),

    /**
     * Magnetic Drum Flow Shape
     * 
     */
    @XmlEnumValue("flowChartMagneticDrum")
    FLOW_CHART_MAGNETIC_DRUM("flowChartMagneticDrum"),

    /**
     * Display Flow Shape
     * 
     */
    @XmlEnumValue("flowChartDisplay")
    FLOW_CHART_DISPLAY("flowChartDisplay"),

    /**
     * Delay Flow Shape
     * 
     */
    @XmlEnumValue("flowChartDelay")
    FLOW_CHART_DELAY("flowChartDelay"),

    /**
     * Alternate Process Flow Shape
     * 
     */
    @XmlEnumValue("flowChartAlternateProcess")
    FLOW_CHART_ALTERNATE_PROCESS("flowChartAlternateProcess"),

    /**
     * Off-Page Connector Flow Shape
     * 
     */
    @XmlEnumValue("flowChartOffpageConnector")
    FLOW_CHART_OFFPAGE_CONNECTOR("flowChartOffpageConnector"),

    /**
     * Blank Button Shape
     * 
     */
    @XmlEnumValue("actionButtonBlank")
    ACTION_BUTTON_BLANK("actionButtonBlank"),

    /**
     * Home Button Shape
     * 
     */
    @XmlEnumValue("actionButtonHome")
    ACTION_BUTTON_HOME("actionButtonHome"),

    /**
     * Help Button Shape
     * 
     */
    @XmlEnumValue("actionButtonHelp")
    ACTION_BUTTON_HELP("actionButtonHelp"),

    /**
     * Information Button Shape
     * 
     */
    @XmlEnumValue("actionButtonInformation")
    ACTION_BUTTON_INFORMATION("actionButtonInformation"),

    /**
     * Forward or Next Button Shape
     * 
     */
    @XmlEnumValue("actionButtonForwardNext")
    ACTION_BUTTON_FORWARD_NEXT("actionButtonForwardNext"),

    /**
     * Back or Previous Button Shape
     * 
     */
    @XmlEnumValue("actionButtonBackPrevious")
    ACTION_BUTTON_BACK_PREVIOUS("actionButtonBackPrevious"),

    /**
     * End Button Shape
     * 
     */
    @XmlEnumValue("actionButtonEnd")
    ACTION_BUTTON_END("actionButtonEnd"),

    /**
     * Beginning Button Shape
     * 
     */
    @XmlEnumValue("actionButtonBeginning")
    ACTION_BUTTON_BEGINNING("actionButtonBeginning"),

    /**
     * Return Button Shape
     * 
     */
    @XmlEnumValue("actionButtonReturn")
    ACTION_BUTTON_RETURN("actionButtonReturn"),

    /**
     * Document Button Shape
     * 
     */
    @XmlEnumValue("actionButtonDocument")
    ACTION_BUTTON_DOCUMENT("actionButtonDocument"),

    /**
     * Sound Button Shape
     * 
     */
    @XmlEnumValue("actionButtonSound")
    ACTION_BUTTON_SOUND("actionButtonSound"),

    /**
     * Movie Button Shape
     * 
     */
    @XmlEnumValue("actionButtonMovie")
    ACTION_BUTTON_MOVIE("actionButtonMovie"),

    /**
     * Gear 6 Shape
     * 
     */
    @XmlEnumValue("gear6")
    GEAR_6("gear6"),

    /**
     * Gear 9 Shape
     * 
     */
    @XmlEnumValue("gear9")
    GEAR_9("gear9"),

    /**
     * Funnel Shape
     * 
     */
    @XmlEnumValue("funnel")
    FUNNEL("funnel"),

    /**
     * Plus Math Shape
     * 
     */
    @XmlEnumValue("mathPlus")
    MATH_PLUS("mathPlus"),

    /**
     * Minus Math Shape
     * 
     */
    @XmlEnumValue("mathMinus")
    MATH_MINUS("mathMinus"),

    /**
     * Multiply Math Shape
     * 
     */
    @XmlEnumValue("mathMultiply")
    MATH_MULTIPLY("mathMultiply"),

    /**
     * Divide Math Shape
     * 
     */
    @XmlEnumValue("mathDivide")
    MATH_DIVIDE("mathDivide"),

    /**
     * Equal Math Shape
     * 
     */
    @XmlEnumValue("mathEqual")
    MATH_EQUAL("mathEqual"),

    /**
     * Not Equal Math Shape
     * 
     */
    @XmlEnumValue("mathNotEqual")
    MATH_NOT_EQUAL("mathNotEqual"),

    /**
     * Corner Tabs Shape
     * 
     */
    @XmlEnumValue("cornerTabs")
    CORNER_TABS("cornerTabs"),

    /**
     * Square Tabs Shape
     * 
     */
    @XmlEnumValue("squareTabs")
    SQUARE_TABS("squareTabs"),

    /**
     * Plaque Tabs Shape
     * 
     */
    @XmlEnumValue("plaqueTabs")
    PLAQUE_TABS("plaqueTabs"),

    /**
     * Chart X Shape
     * 
     */
    @XmlEnumValue("chartX")
    CHART_X("chartX"),

    /**
     * Chart Star Shape
     * 
     */
    @XmlEnumValue("chartStar")
    CHART_STAR("chartStar"),

    /**
     * Chart Plus Shape
     * 
     */
    @XmlEnumValue("chartPlus")
    CHART_PLUS("chartPlus");
    private final String value;

    STShapeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STShapeType fromValue(String v) {
        for (STShapeType c: STShapeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
