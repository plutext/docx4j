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
 * <p>Java class for ST_Border.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_Border">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="nil"/>
 *     &lt;enumeration value="none"/>
 *     &lt;enumeration value="single"/>
 *     &lt;enumeration value="thick"/>
 *     &lt;enumeration value="double"/>
 *     &lt;enumeration value="dotted"/>
 *     &lt;enumeration value="dashed"/>
 *     &lt;enumeration value="dotDash"/>
 *     &lt;enumeration value="dotDotDash"/>
 *     &lt;enumeration value="triple"/>
 *     &lt;enumeration value="thinThickSmallGap"/>
 *     &lt;enumeration value="thickThinSmallGap"/>
 *     &lt;enumeration value="thinThickThinSmallGap"/>
 *     &lt;enumeration value="thinThickMediumGap"/>
 *     &lt;enumeration value="thickThinMediumGap"/>
 *     &lt;enumeration value="thinThickThinMediumGap"/>
 *     &lt;enumeration value="thinThickLargeGap"/>
 *     &lt;enumeration value="thickThinLargeGap"/>
 *     &lt;enumeration value="thinThickThinLargeGap"/>
 *     &lt;enumeration value="wave"/>
 *     &lt;enumeration value="doubleWave"/>
 *     &lt;enumeration value="dashSmallGap"/>
 *     &lt;enumeration value="dashDotStroked"/>
 *     &lt;enumeration value="threeDEmboss"/>
 *     &lt;enumeration value="threeDEngrave"/>
 *     &lt;enumeration value="outset"/>
 *     &lt;enumeration value="inset"/>
 *     &lt;enumeration value="apples"/>
 *     &lt;enumeration value="archedScallops"/>
 *     &lt;enumeration value="babyPacifier"/>
 *     &lt;enumeration value="babyRattle"/>
 *     &lt;enumeration value="balloons3Colors"/>
 *     &lt;enumeration value="balloonsHotAir"/>
 *     &lt;enumeration value="basicBlackDashes"/>
 *     &lt;enumeration value="basicBlackDots"/>
 *     &lt;enumeration value="basicBlackSquares"/>
 *     &lt;enumeration value="basicThinLines"/>
 *     &lt;enumeration value="basicWhiteDashes"/>
 *     &lt;enumeration value="basicWhiteDots"/>
 *     &lt;enumeration value="basicWhiteSquares"/>
 *     &lt;enumeration value="basicWideInline"/>
 *     &lt;enumeration value="basicWideMidline"/>
 *     &lt;enumeration value="basicWideOutline"/>
 *     &lt;enumeration value="bats"/>
 *     &lt;enumeration value="birds"/>
 *     &lt;enumeration value="birdsFlight"/>
 *     &lt;enumeration value="cabins"/>
 *     &lt;enumeration value="cakeSlice"/>
 *     &lt;enumeration value="candyCorn"/>
 *     &lt;enumeration value="celticKnotwork"/>
 *     &lt;enumeration value="certificateBanner"/>
 *     &lt;enumeration value="chainLink"/>
 *     &lt;enumeration value="champagneBottle"/>
 *     &lt;enumeration value="checkedBarBlack"/>
 *     &lt;enumeration value="checkedBarColor"/>
 *     &lt;enumeration value="checkered"/>
 *     &lt;enumeration value="christmasTree"/>
 *     &lt;enumeration value="circlesLines"/>
 *     &lt;enumeration value="circlesRectangles"/>
 *     &lt;enumeration value="classicalWave"/>
 *     &lt;enumeration value="clocks"/>
 *     &lt;enumeration value="compass"/>
 *     &lt;enumeration value="confetti"/>
 *     &lt;enumeration value="confettiGrays"/>
 *     &lt;enumeration value="confettiOutline"/>
 *     &lt;enumeration value="confettiStreamers"/>
 *     &lt;enumeration value="confettiWhite"/>
 *     &lt;enumeration value="cornerTriangles"/>
 *     &lt;enumeration value="couponCutoutDashes"/>
 *     &lt;enumeration value="couponCutoutDots"/>
 *     &lt;enumeration value="crazyMaze"/>
 *     &lt;enumeration value="creaturesButterfly"/>
 *     &lt;enumeration value="creaturesFish"/>
 *     &lt;enumeration value="creaturesInsects"/>
 *     &lt;enumeration value="creaturesLadyBug"/>
 *     &lt;enumeration value="crossStitch"/>
 *     &lt;enumeration value="cup"/>
 *     &lt;enumeration value="decoArch"/>
 *     &lt;enumeration value="decoArchColor"/>
 *     &lt;enumeration value="decoBlocks"/>
 *     &lt;enumeration value="diamondsGray"/>
 *     &lt;enumeration value="doubleD"/>
 *     &lt;enumeration value="doubleDiamonds"/>
 *     &lt;enumeration value="earth1"/>
 *     &lt;enumeration value="earth2"/>
 *     &lt;enumeration value="eclipsingSquares1"/>
 *     &lt;enumeration value="eclipsingSquares2"/>
 *     &lt;enumeration value="eggsBlack"/>
 *     &lt;enumeration value="fans"/>
 *     &lt;enumeration value="film"/>
 *     &lt;enumeration value="firecrackers"/>
 *     &lt;enumeration value="flowersBlockPrint"/>
 *     &lt;enumeration value="flowersDaisies"/>
 *     &lt;enumeration value="flowersModern1"/>
 *     &lt;enumeration value="flowersModern2"/>
 *     &lt;enumeration value="flowersPansy"/>
 *     &lt;enumeration value="flowersRedRose"/>
 *     &lt;enumeration value="flowersRoses"/>
 *     &lt;enumeration value="flowersTeacup"/>
 *     &lt;enumeration value="flowersTiny"/>
 *     &lt;enumeration value="gems"/>
 *     &lt;enumeration value="gingerbreadMan"/>
 *     &lt;enumeration value="gradient"/>
 *     &lt;enumeration value="handmade1"/>
 *     &lt;enumeration value="handmade2"/>
 *     &lt;enumeration value="heartBalloon"/>
 *     &lt;enumeration value="heartGray"/>
 *     &lt;enumeration value="hearts"/>
 *     &lt;enumeration value="heebieJeebies"/>
 *     &lt;enumeration value="holly"/>
 *     &lt;enumeration value="houseFunky"/>
 *     &lt;enumeration value="hypnotic"/>
 *     &lt;enumeration value="iceCreamCones"/>
 *     &lt;enumeration value="lightBulb"/>
 *     &lt;enumeration value="lightning1"/>
 *     &lt;enumeration value="lightning2"/>
 *     &lt;enumeration value="mapPins"/>
 *     &lt;enumeration value="mapleLeaf"/>
 *     &lt;enumeration value="mapleMuffins"/>
 *     &lt;enumeration value="marquee"/>
 *     &lt;enumeration value="marqueeToothed"/>
 *     &lt;enumeration value="moons"/>
 *     &lt;enumeration value="mosaic"/>
 *     &lt;enumeration value="musicNotes"/>
 *     &lt;enumeration value="northwest"/>
 *     &lt;enumeration value="ovals"/>
 *     &lt;enumeration value="packages"/>
 *     &lt;enumeration value="palmsBlack"/>
 *     &lt;enumeration value="palmsColor"/>
 *     &lt;enumeration value="paperClips"/>
 *     &lt;enumeration value="papyrus"/>
 *     &lt;enumeration value="partyFavor"/>
 *     &lt;enumeration value="partyGlass"/>
 *     &lt;enumeration value="pencils"/>
 *     &lt;enumeration value="people"/>
 *     &lt;enumeration value="peopleWaving"/>
 *     &lt;enumeration value="peopleHats"/>
 *     &lt;enumeration value="poinsettias"/>
 *     &lt;enumeration value="postageStamp"/>
 *     &lt;enumeration value="pumpkin1"/>
 *     &lt;enumeration value="pushPinNote2"/>
 *     &lt;enumeration value="pushPinNote1"/>
 *     &lt;enumeration value="pyramids"/>
 *     &lt;enumeration value="pyramidsAbove"/>
 *     &lt;enumeration value="quadrants"/>
 *     &lt;enumeration value="rings"/>
 *     &lt;enumeration value="safari"/>
 *     &lt;enumeration value="sawtooth"/>
 *     &lt;enumeration value="sawtoothGray"/>
 *     &lt;enumeration value="scaredCat"/>
 *     &lt;enumeration value="seattle"/>
 *     &lt;enumeration value="shadowedSquares"/>
 *     &lt;enumeration value="sharksTeeth"/>
 *     &lt;enumeration value="shorebirdTracks"/>
 *     &lt;enumeration value="skyrocket"/>
 *     &lt;enumeration value="snowflakeFancy"/>
 *     &lt;enumeration value="snowflakes"/>
 *     &lt;enumeration value="sombrero"/>
 *     &lt;enumeration value="southwest"/>
 *     &lt;enumeration value="stars"/>
 *     &lt;enumeration value="starsTop"/>
 *     &lt;enumeration value="stars3d"/>
 *     &lt;enumeration value="starsBlack"/>
 *     &lt;enumeration value="starsShadowed"/>
 *     &lt;enumeration value="sun"/>
 *     &lt;enumeration value="swirligig"/>
 *     &lt;enumeration value="tornPaper"/>
 *     &lt;enumeration value="tornPaperBlack"/>
 *     &lt;enumeration value="trees"/>
 *     &lt;enumeration value="triangleParty"/>
 *     &lt;enumeration value="triangles"/>
 *     &lt;enumeration value="tribal1"/>
 *     &lt;enumeration value="tribal2"/>
 *     &lt;enumeration value="tribal3"/>
 *     &lt;enumeration value="tribal4"/>
 *     &lt;enumeration value="tribal5"/>
 *     &lt;enumeration value="tribal6"/>
 *     &lt;enumeration value="twistedLines1"/>
 *     &lt;enumeration value="twistedLines2"/>
 *     &lt;enumeration value="vine"/>
 *     &lt;enumeration value="waveline"/>
 *     &lt;enumeration value="weavingAngles"/>
 *     &lt;enumeration value="weavingBraid"/>
 *     &lt;enumeration value="weavingRibbon"/>
 *     &lt;enumeration value="weavingStrips"/>
 *     &lt;enumeration value="whiteFlowers"/>
 *     &lt;enumeration value="woodwork"/>
 *     &lt;enumeration value="xIllusions"/>
 *     &lt;enumeration value="zanyTriangles"/>
 *     &lt;enumeration value="zigZag"/>
 *     &lt;enumeration value="zigZagStitch"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_Border")
@XmlEnum
public enum STBorder {


    /**
     * No Border
     * 
     */
    @XmlEnumValue("nil")
    NIL("nil"),

    /**
     * No Border
     * 
     */
    @XmlEnumValue("none")
    NONE("none"),

    /**
     * Single Line Border
     * 
     */
    @XmlEnumValue("single")
    SINGLE("single"),

    /**
     * Single Line Border
     * 
     */
    @XmlEnumValue("thick")
    THICK("thick"),

    /**
     * Double Line Border
     * 
     */
    @XmlEnumValue("double")
    DOUBLE("double"),

    /**
     * Dotted Line Border
     * 
     */
    @XmlEnumValue("dotted")
    DOTTED("dotted"),

    /**
     * Dashed Line Border
     * 
     */
    @XmlEnumValue("dashed")
    DASHED("dashed"),

    /**
     * Dot Dash Line Border
     * 
     */
    @XmlEnumValue("dotDash")
    DOT_DASH("dotDash"),

    /**
     * Dot Dot Dash Line Border
     * 
     */
    @XmlEnumValue("dotDotDash")
    DOT_DOT_DASH("dotDotDash"),

    /**
     * Triple Line Border
     * 
     */
    @XmlEnumValue("triple")
    TRIPLE("triple"),

    /**
     * Thin, Thick Line Border
     * 
     */
    @XmlEnumValue("thinThickSmallGap")
    THIN_THICK_SMALL_GAP("thinThickSmallGap"),

    /**
     * Thick, Thin Line Border
     * 
     */
    @XmlEnumValue("thickThinSmallGap")
    THICK_THIN_SMALL_GAP("thickThinSmallGap"),

    /**
     * Thin, Thick, Thin Line
     * 						Border
     * 
     */
    @XmlEnumValue("thinThickThinSmallGap")
    THIN_THICK_THIN_SMALL_GAP("thinThickThinSmallGap"),

    /**
     * Thin, Thick Line Border
     * 
     */
    @XmlEnumValue("thinThickMediumGap")
    THIN_THICK_MEDIUM_GAP("thinThickMediumGap"),

    /**
     * Thick, Thin Line Border
     * 
     */
    @XmlEnumValue("thickThinMediumGap")
    THICK_THIN_MEDIUM_GAP("thickThinMediumGap"),

    /**
     * Thin, Thick, Thin Line
     * 						Border
     * 
     */
    @XmlEnumValue("thinThickThinMediumGap")
    THIN_THICK_THIN_MEDIUM_GAP("thinThickThinMediumGap"),

    /**
     * Thin, Thick Line Border
     * 
     */
    @XmlEnumValue("thinThickLargeGap")
    THIN_THICK_LARGE_GAP("thinThickLargeGap"),

    /**
     * Thick, Thin Line Border
     * 
     */
    @XmlEnumValue("thickThinLargeGap")
    THICK_THIN_LARGE_GAP("thickThinLargeGap"),

    /**
     * Thin, Thick, Thin Line
     * 						Border
     * 
     */
    @XmlEnumValue("thinThickThinLargeGap")
    THIN_THICK_THIN_LARGE_GAP("thinThickThinLargeGap"),

    /**
     * Wavy Line Border
     * 
     */
    @XmlEnumValue("wave")
    WAVE("wave"),

    /**
     * Double Wave Line Border
     * 
     */
    @XmlEnumValue("doubleWave")
    DOUBLE_WAVE("doubleWave"),

    /**
     * Dashed Line Border
     * 
     */
    @XmlEnumValue("dashSmallGap")
    DASH_SMALL_GAP("dashSmallGap"),

    /**
     * Dash Dot Strokes Line
     * 						Border
     * 
     */
    @XmlEnumValue("dashDotStroked")
    DASH_DOT_STROKED("dashDotStroked"),

    /**
     *  3D Embossed Line Border
     * 
     */
    @XmlEnumValue("threeDEmboss")
    THREE_D_EMBOSS("threeDEmboss"),

    /**
     *  3D Engraved Line Border
     * 
     */
    @XmlEnumValue("threeDEngrave")
    THREE_D_ENGRAVE("threeDEngrave"),

    /**
     * Outset Line Border
     * 
     */
    @XmlEnumValue("outset")
    OUTSET("outset"),

    /**
     * Inset Line Border
     * 
     */
    @XmlEnumValue("inset")
    INSET("inset"),

    /**
     * Apples Art Border
     * 
     */
    @XmlEnumValue("apples")
    APPLES("apples"),

    /**
     * Arched Scallops Art
     * 						Border
     * 
     */
    @XmlEnumValue("archedScallops")
    ARCHED_SCALLOPS("archedScallops"),

    /**
     * Baby Pacifier Art Border
     * 
     */
    @XmlEnumValue("babyPacifier")
    BABY_PACIFIER("babyPacifier"),

    /**
     * Baby Rattle Art Border
     * 
     */
    @XmlEnumValue("babyRattle")
    BABY_RATTLE("babyRattle"),

    /**
     * Three Color Balloons Art
     * 						Border
     * 
     */
    @XmlEnumValue("balloons3Colors")
    BALLOONS_3_COLORS("balloons3Colors"),

    /**
     * Hot Air Balloons Art
     * 						Border
     * 
     */
    @XmlEnumValue("balloonsHotAir")
    BALLOONS_HOT_AIR("balloonsHotAir"),

    /**
     * Black Dash Art Border
     * 
     */
    @XmlEnumValue("basicBlackDashes")
    BASIC_BLACK_DASHES("basicBlackDashes"),

    /**
     * Black Dot Art Border
     * 
     */
    @XmlEnumValue("basicBlackDots")
    BASIC_BLACK_DOTS("basicBlackDots"),

    /**
     * Black Square Art Border
     * 
     */
    @XmlEnumValue("basicBlackSquares")
    BASIC_BLACK_SQUARES("basicBlackSquares"),

    /**
     * Thin Line Art Border
     * 
     */
    @XmlEnumValue("basicThinLines")
    BASIC_THIN_LINES("basicThinLines"),

    /**
     * White Dash Art Border
     * 
     */
    @XmlEnumValue("basicWhiteDashes")
    BASIC_WHITE_DASHES("basicWhiteDashes"),

    /**
     * White Dot Art Border
     * 
     */
    @XmlEnumValue("basicWhiteDots")
    BASIC_WHITE_DOTS("basicWhiteDots"),

    /**
     * White Square Art Border
     * 
     */
    @XmlEnumValue("basicWhiteSquares")
    BASIC_WHITE_SQUARES("basicWhiteSquares"),

    /**
     * Wide Inline Art Border
     * 
     */
    @XmlEnumValue("basicWideInline")
    BASIC_WIDE_INLINE("basicWideInline"),

    /**
     * Wide Midline Art Border
     * 
     */
    @XmlEnumValue("basicWideMidline")
    BASIC_WIDE_MIDLINE("basicWideMidline"),

    /**
     * Wide Outline Art Border
     * 
     */
    @XmlEnumValue("basicWideOutline")
    BASIC_WIDE_OUTLINE("basicWideOutline"),

    /**
     * Bats Art Border
     * 
     */
    @XmlEnumValue("bats")
    BATS("bats"),

    /**
     * Birds Art Border
     * 
     */
    @XmlEnumValue("birds")
    BIRDS("birds"),

    /**
     * Birds Flying Art Border
     * 
     */
    @XmlEnumValue("birdsFlight")
    BIRDS_FLIGHT("birdsFlight"),

    /**
     * Cabin Art Border
     * 
     */
    @XmlEnumValue("cabins")
    CABINS("cabins"),

    /**
     * Cake Art Border
     * 
     */
    @XmlEnumValue("cakeSlice")
    CAKE_SLICE("cakeSlice"),

    /**
     * Candy Corn Art Border
     * 
     */
    @XmlEnumValue("candyCorn")
    CANDY_CORN("candyCorn"),

    /**
     * Knot Work Art Border
     * 
     */
    @XmlEnumValue("celticKnotwork")
    CELTIC_KNOTWORK("celticKnotwork"),

    /**
     * Certificate Banner Art
     * 						Border
     * 
     */
    @XmlEnumValue("certificateBanner")
    CERTIFICATE_BANNER("certificateBanner"),

    /**
     * Chain Link Art Border
     * 
     */
    @XmlEnumValue("chainLink")
    CHAIN_LINK("chainLink"),

    /**
     * Champagne Bottle Art
     * 						Border
     * 
     */
    @XmlEnumValue("champagneBottle")
    CHAMPAGNE_BOTTLE("champagneBottle"),

    /**
     * Black and White Bar Art
     * 						Border
     * 
     */
    @XmlEnumValue("checkedBarBlack")
    CHECKED_BAR_BLACK("checkedBarBlack"),

    /**
     * Color Checked Bar Art
     * 						Border
     * 
     */
    @XmlEnumValue("checkedBarColor")
    CHECKED_BAR_COLOR("checkedBarColor"),

    /**
     * Checkerboard Art Border
     * 
     */
    @XmlEnumValue("checkered")
    CHECKERED("checkered"),

    /**
     * Christmas Tree Art
     * 						Border
     * 
     */
    @XmlEnumValue("christmasTree")
    CHRISTMAS_TREE("christmasTree"),

    /**
     * Circles And Lines Art
     * 						Border
     * 
     */
    @XmlEnumValue("circlesLines")
    CIRCLES_LINES("circlesLines"),

    /**
     * Circles and Rectangles Art
     * 						Border
     * 
     */
    @XmlEnumValue("circlesRectangles")
    CIRCLES_RECTANGLES("circlesRectangles"),

    /**
     * Wave Art Border
     * 
     */
    @XmlEnumValue("classicalWave")
    CLASSICAL_WAVE("classicalWave"),

    /**
     * Clocks Art Border
     * 
     */
    @XmlEnumValue("clocks")
    CLOCKS("clocks"),

    /**
     * Compass Art Border
     * 
     */
    @XmlEnumValue("compass")
    COMPASS("compass"),

    /**
     * Confetti Art Border
     * 
     */
    @XmlEnumValue("confetti")
    CONFETTI("confetti"),

    /**
     * Confetti Art Border
     * 
     */
    @XmlEnumValue("confettiGrays")
    CONFETTI_GRAYS("confettiGrays"),

    /**
     * Confetti Art Border
     * 
     */
    @XmlEnumValue("confettiOutline")
    CONFETTI_OUTLINE("confettiOutline"),

    /**
     * Confetti Streamers Art
     * 						Border
     * 
     */
    @XmlEnumValue("confettiStreamers")
    CONFETTI_STREAMERS("confettiStreamers"),

    /**
     * Confetti Art Border
     * 
     */
    @XmlEnumValue("confettiWhite")
    CONFETTI_WHITE("confettiWhite"),

    /**
     * Corner Triangle Art
     * 						Border
     * 
     */
    @XmlEnumValue("cornerTriangles")
    CORNER_TRIANGLES("cornerTriangles"),

    /**
     * Dashed Line Art Border
     * 
     */
    @XmlEnumValue("couponCutoutDashes")
    COUPON_CUTOUT_DASHES("couponCutoutDashes"),

    /**
     * Dotted Line Art Border
     * 
     */
    @XmlEnumValue("couponCutoutDots")
    COUPON_CUTOUT_DOTS("couponCutoutDots"),

    /**
     * Maze Art Border
     * 
     */
    @XmlEnumValue("crazyMaze")
    CRAZY_MAZE("crazyMaze"),

    /**
     * Butterfly Art Border
     * 
     */
    @XmlEnumValue("creaturesButterfly")
    CREATURES_BUTTERFLY("creaturesButterfly"),

    /**
     * Fish Art Border
     * 
     */
    @XmlEnumValue("creaturesFish")
    CREATURES_FISH("creaturesFish"),

    /**
     * Insects Art Border
     * 
     */
    @XmlEnumValue("creaturesInsects")
    CREATURES_INSECTS("creaturesInsects"),

    /**
     * Ladybug Art Border
     * 
     */
    @XmlEnumValue("creaturesLadyBug")
    CREATURES_LADY_BUG("creaturesLadyBug"),

    /**
     * Cross-stitch Art Border
     * 
     */
    @XmlEnumValue("crossStitch")
    CROSS_STITCH("crossStitch"),

    /**
     * Cupid Art Border
     * 
     */
    @XmlEnumValue("cup")
    CUP("cup"),

    /**
     * Archway Art Border
     * 
     */
    @XmlEnumValue("decoArch")
    DECO_ARCH("decoArch"),

    /**
     * Color Archway Art Border
     * 
     */
    @XmlEnumValue("decoArchColor")
    DECO_ARCH_COLOR("decoArchColor"),

    /**
     * Blocks Art Border
     * 
     */
    @XmlEnumValue("decoBlocks")
    DECO_BLOCKS("decoBlocks"),

    /**
     * Gray Diamond Art Border
     * 
     */
    @XmlEnumValue("diamondsGray")
    DIAMONDS_GRAY("diamondsGray"),

    /**
     * Double D Art Border
     * 
     */
    @XmlEnumValue("doubleD")
    DOUBLE_D("doubleD"),

    /**
     * Diamond Art Border
     * 
     */
    @XmlEnumValue("doubleDiamonds")
    DOUBLE_DIAMONDS("doubleDiamonds"),

    /**
     * Earth Art Border
     * 
     */
    @XmlEnumValue("earth1")
    EARTH_1("earth1"),

    /**
     * Earth Art Border
     * 
     */
    @XmlEnumValue("earth2")
    EARTH_2("earth2"),

    /**
     * Shadowed Square Art
     * 						Border
     * 
     */
    @XmlEnumValue("eclipsingSquares1")
    ECLIPSING_SQUARES_1("eclipsingSquares1"),

    /**
     * Shadowed Square Art
     * 						Border
     * 
     */
    @XmlEnumValue("eclipsingSquares2")
    ECLIPSING_SQUARES_2("eclipsingSquares2"),

    /**
     * Painted Egg Art Border
     * 
     */
    @XmlEnumValue("eggsBlack")
    EGGS_BLACK("eggsBlack"),

    /**
     * Fans Art Border
     * 
     */
    @XmlEnumValue("fans")
    FANS("fans"),

    /**
     * Film Reel Art Border
     * 
     */
    @XmlEnumValue("film")
    FILM("film"),

    /**
     * Firecracker Art Border
     * 
     */
    @XmlEnumValue("firecrackers")
    FIRECRACKERS("firecrackers"),

    /**
     * Flowers Art Border
     * 
     */
    @XmlEnumValue("flowersBlockPrint")
    FLOWERS_BLOCK_PRINT("flowersBlockPrint"),

    /**
     * Daisy Art Border
     * 
     */
    @XmlEnumValue("flowersDaisies")
    FLOWERS_DAISIES("flowersDaisies"),

    /**
     * Flowers Art Border
     * 
     */
    @XmlEnumValue("flowersModern1")
    FLOWERS_MODERN_1("flowersModern1"),

    /**
     * Flowers Art Border
     * 
     */
    @XmlEnumValue("flowersModern2")
    FLOWERS_MODERN_2("flowersModern2"),

    /**
     * Pansy Art Border
     * 
     */
    @XmlEnumValue("flowersPansy")
    FLOWERS_PANSY("flowersPansy"),

    /**
     * Red Rose Art Border
     * 
     */
    @XmlEnumValue("flowersRedRose")
    FLOWERS_RED_ROSE("flowersRedRose"),

    /**
     * Roses Art Border
     * 
     */
    @XmlEnumValue("flowersRoses")
    FLOWERS_ROSES("flowersRoses"),

    /**
     * Flowers in a Teacup Art
     * 						Border
     * 
     */
    @XmlEnumValue("flowersTeacup")
    FLOWERS_TEACUP("flowersTeacup"),

    /**
     * Small Flower Art Border
     * 
     */
    @XmlEnumValue("flowersTiny")
    FLOWERS_TINY("flowersTiny"),

    /**
     * Gems Art Border
     * 
     */
    @XmlEnumValue("gems")
    GEMS("gems"),

    /**
     * Gingerbread Man Art
     * 						Border
     * 
     */
    @XmlEnumValue("gingerbreadMan")
    GINGERBREAD_MAN("gingerbreadMan"),

    /**
     * Triangle Gradient Art
     * 						Border
     * 
     */
    @XmlEnumValue("gradient")
    GRADIENT("gradient"),

    /**
     * Handmade Art Border
     * 
     */
    @XmlEnumValue("handmade1")
    HANDMADE_1("handmade1"),

    /**
     * Handmade Art Border
     * 
     */
    @XmlEnumValue("handmade2")
    HANDMADE_2("handmade2"),

    /**
     * Heart-Shaped Balloon Art
     * 						Border
     * 
     */
    @XmlEnumValue("heartBalloon")
    HEART_BALLOON("heartBalloon"),

    /**
     * Gray Heart Art Border
     * 
     */
    @XmlEnumValue("heartGray")
    HEART_GRAY("heartGray"),

    /**
     * Hearts Art Border
     * 
     */
    @XmlEnumValue("hearts")
    HEARTS("hearts"),

    /**
     * Pattern Art Border
     * 
     */
    @XmlEnumValue("heebieJeebies")
    HEEBIE_JEEBIES("heebieJeebies"),

    /**
     * Holly Art Border
     * 
     */
    @XmlEnumValue("holly")
    HOLLY("holly"),

    /**
     * House Art Border
     * 
     */
    @XmlEnumValue("houseFunky")
    HOUSE_FUNKY("houseFunky"),

    /**
     * Circular Art Border
     * 
     */
    @XmlEnumValue("hypnotic")
    HYPNOTIC("hypnotic"),

    /**
     * Ice Cream Cone Art Border
     * 
     */
    @XmlEnumValue("iceCreamCones")
    ICE_CREAM_CONES("iceCreamCones"),

    /**
     * Light Bulb Art Border
     * 
     */
    @XmlEnumValue("lightBulb")
    LIGHT_BULB("lightBulb"),

    /**
     * Lightning Art Border
     * 
     */
    @XmlEnumValue("lightning1")
    LIGHTNING_1("lightning1"),

    /**
     * Lightning Art Border
     * 
     */
    @XmlEnumValue("lightning2")
    LIGHTNING_2("lightning2"),

    /**
     * Map Pins Art Border
     * 
     */
    @XmlEnumValue("mapPins")
    MAP_PINS("mapPins"),

    /**
     * Maple Leaf Art Border
     * 
     */
    @XmlEnumValue("mapleLeaf")
    MAPLE_LEAF("mapleLeaf"),

    /**
     * Muffin Art Border
     * 
     */
    @XmlEnumValue("mapleMuffins")
    MAPLE_MUFFINS("mapleMuffins"),

    /**
     * Marquee Art Border
     * 
     */
    @XmlEnumValue("marquee")
    MARQUEE("marquee"),

    /**
     * Marquee Art Border
     * 
     */
    @XmlEnumValue("marqueeToothed")
    MARQUEE_TOOTHED("marqueeToothed"),

    /**
     * Moon Art Border
     * 
     */
    @XmlEnumValue("moons")
    MOONS("moons"),

    /**
     * Mosaic Art Border
     * 
     */
    @XmlEnumValue("mosaic")
    MOSAIC("mosaic"),

    /**
     * Musical Note Art Border
     * 
     */
    @XmlEnumValue("musicNotes")
    MUSIC_NOTES("musicNotes"),

    /**
     * Patterned Art Border
     * 
     */
    @XmlEnumValue("northwest")
    NORTHWEST("northwest"),

    /**
     * Oval Art Border
     * 
     */
    @XmlEnumValue("ovals")
    OVALS("ovals"),

    /**
     * Package Art Border
     * 
     */
    @XmlEnumValue("packages")
    PACKAGES("packages"),

    /**
     * Black Palm Tree Art
     * 						Border
     * 
     */
    @XmlEnumValue("palmsBlack")
    PALMS_BLACK("palmsBlack"),

    /**
     * Color Palm Tree Art
     * 						Border
     * 
     */
    @XmlEnumValue("palmsColor")
    PALMS_COLOR("palmsColor"),

    /**
     * Paper Clip Art Border
     * 
     */
    @XmlEnumValue("paperClips")
    PAPER_CLIPS("paperClips"),

    /**
     * Papyrus Art Border
     * 
     */
    @XmlEnumValue("papyrus")
    PAPYRUS("papyrus"),

    /**
     * Party Favor Art Border
     * 
     */
    @XmlEnumValue("partyFavor")
    PARTY_FAVOR("partyFavor"),

    /**
     * Party Glass Art Border
     * 
     */
    @XmlEnumValue("partyGlass")
    PARTY_GLASS("partyGlass"),

    /**
     * Pencils Art Border
     * 
     */
    @XmlEnumValue("pencils")
    PENCILS("pencils"),

    /**
     * Character Art Border
     * 
     */
    @XmlEnumValue("people")
    PEOPLE("people"),

    /**
     * Waving Character Border
     * 
     */
    @XmlEnumValue("peopleWaving")
    PEOPLE_WAVING("peopleWaving"),

    /**
     * Character With Hat Art
     * 						Border
     * 
     */
    @XmlEnumValue("peopleHats")
    PEOPLE_HATS("peopleHats"),

    /**
     * Poinsettia Art Border
     * 
     */
    @XmlEnumValue("poinsettias")
    POINSETTIAS("poinsettias"),

    /**
     * Postage Stamp Art Border
     * 
     */
    @XmlEnumValue("postageStamp")
    POSTAGE_STAMP("postageStamp"),

    /**
     * Pumpkin Art Border
     * 
     */
    @XmlEnumValue("pumpkin1")
    PUMPKIN_1("pumpkin1"),

    /**
     * Push Pin Art Border
     * 
     */
    @XmlEnumValue("pushPinNote2")
    PUSH_PIN_NOTE_2("pushPinNote2"),

    /**
     * Push Pin Art Border
     * 
     */
    @XmlEnumValue("pushPinNote1")
    PUSH_PIN_NOTE_1("pushPinNote1"),

    /**
     * Pyramid Art Border
     * 
     */
    @XmlEnumValue("pyramids")
    PYRAMIDS("pyramids"),

    /**
     * Pyramid Art Border
     * 
     */
    @XmlEnumValue("pyramidsAbove")
    PYRAMIDS_ABOVE("pyramidsAbove"),

    /**
     * Quadrants Art Border
     * 
     */
    @XmlEnumValue("quadrants")
    QUADRANTS("quadrants"),

    /**
     * Rings Art Border
     * 
     */
    @XmlEnumValue("rings")
    RINGS("rings"),

    /**
     * Safari Art Border
     * 
     */
    @XmlEnumValue("safari")
    SAFARI("safari"),

    /**
     * Saw tooth Art Border
     * 
     */
    @XmlEnumValue("sawtooth")
    SAWTOOTH("sawtooth"),

    /**
     * Gray Saw tooth Art Border
     * 
     */
    @XmlEnumValue("sawtoothGray")
    SAWTOOTH_GRAY("sawtoothGray"),

    /**
     * Scared Cat Art Border
     * 
     */
    @XmlEnumValue("scaredCat")
    SCARED_CAT("scaredCat"),

    /**
     * Umbrella Art Border
     * 
     */
    @XmlEnumValue("seattle")
    SEATTLE("seattle"),

    /**
     * Shadowed Squares Art
     * 						Border
     * 
     */
    @XmlEnumValue("shadowedSquares")
    SHADOWED_SQUARES("shadowedSquares"),

    /**
     * Shark Tooth Art Border
     * 
     */
    @XmlEnumValue("sharksTeeth")
    SHARKS_TEETH("sharksTeeth"),

    /**
     * Bird Tracks Art Border
     * 
     */
    @XmlEnumValue("shorebirdTracks")
    SHOREBIRD_TRACKS("shorebirdTracks"),

    /**
     * Rocket Art Border
     * 
     */
    @XmlEnumValue("skyrocket")
    SKYROCKET("skyrocket"),

    /**
     * Snowflake Art Border
     * 
     */
    @XmlEnumValue("snowflakeFancy")
    SNOWFLAKE_FANCY("snowflakeFancy"),

    /**
     * Snowflake Art Border
     * 
     */
    @XmlEnumValue("snowflakes")
    SNOWFLAKES("snowflakes"),

    /**
     * Sombrero Art Border
     * 
     */
    @XmlEnumValue("sombrero")
    SOMBRERO("sombrero"),

    /**
     * Southwest-themed Art
     * 						Border
     * 
     */
    @XmlEnumValue("southwest")
    SOUTHWEST("southwest"),

    /**
     * Stars Art Border
     * 
     */
    @XmlEnumValue("stars")
    STARS("stars"),

    /**
     * Stars On Top Art Border
     * 
     */
    @XmlEnumValue("starsTop")
    STARS_TOP("starsTop"),

    /**
     *  3-D Stars Art Border
     * 
     */
    @XmlEnumValue("stars3d")
    STARS_3_D("stars3d"),

    /**
     * Stars Art Border
     * 
     */
    @XmlEnumValue("starsBlack")
    STARS_BLACK("starsBlack"),

    /**
     * Stars With Shadows Art
     * 						Border
     * 
     */
    @XmlEnumValue("starsShadowed")
    STARS_SHADOWED("starsShadowed"),

    /**
     * Sun Art Border
     * 
     */
    @XmlEnumValue("sun")
    SUN("sun"),

    /**
     * Whirligig Art Border
     * 
     */
    @XmlEnumValue("swirligig")
    SWIRLIGIG("swirligig"),

    /**
     * Torn Paper Art Border
     * 
     */
    @XmlEnumValue("tornPaper")
    TORN_PAPER("tornPaper"),

    /**
     * Black Torn Paper Art
     * 						Border
     * 
     */
    @XmlEnumValue("tornPaperBlack")
    TORN_PAPER_BLACK("tornPaperBlack"),

    /**
     * Tree Art Border
     * 
     */
    @XmlEnumValue("trees")
    TREES("trees"),

    /**
     * Triangle Art Border
     * 
     */
    @XmlEnumValue("triangleParty")
    TRIANGLE_PARTY("triangleParty"),

    /**
     * Triangles Art Border
     * 
     */
    @XmlEnumValue("triangles")
    TRIANGLES("triangles"),

    /**
     * Tribal Art Border One
     * 
     */
    @XmlEnumValue("tribal1")
    TRIBAL_1("tribal1"),

    /**
     * Tribal Art Border Two
     * 
     */
    @XmlEnumValue("tribal2")
    TRIBAL_2("tribal2"),

    /**
     * Tribal Art Border Three
     * 
     */
    @XmlEnumValue("tribal3")
    TRIBAL_3("tribal3"),

    /**
     * Tribal Art Border Four
     * 
     */
    @XmlEnumValue("tribal4")
    TRIBAL_4("tribal4"),

    /**
     * Tribal Art Border Five
     * 
     */
    @XmlEnumValue("tribal5")
    TRIBAL_5("tribal5"),

    /**
     * Tribal Art Border Six
     * 
     */
    @XmlEnumValue("tribal6")
    TRIBAL_6("tribal6"),

    /**
     * Twisted Lines Art Border
     * 
     */
    @XmlEnumValue("twistedLines1")
    TWISTED_LINES_1("twistedLines1"),

    /**
     * Twisted Lines Art Border
     * 
     */
    @XmlEnumValue("twistedLines2")
    TWISTED_LINES_2("twistedLines2"),

    /**
     * Vine Art Border
     * 
     */
    @XmlEnumValue("vine")
    VINE("vine"),

    /**
     * Wavy Line Art Border
     * 
     */
    @XmlEnumValue("waveline")
    WAVELINE("waveline"),

    /**
     * Weaving Angles Art
     * 						Border
     * 
     */
    @XmlEnumValue("weavingAngles")
    WEAVING_ANGLES("weavingAngles"),

    /**
     * Weaving Braid Art Border
     * 
     */
    @XmlEnumValue("weavingBraid")
    WEAVING_BRAID("weavingBraid"),

    /**
     * Weaving Ribbon Art
     * 						Border
     * 
     */
    @XmlEnumValue("weavingRibbon")
    WEAVING_RIBBON("weavingRibbon"),

    /**
     * Weaving Strips Art
     * 						Border
     * 
     */
    @XmlEnumValue("weavingStrips")
    WEAVING_STRIPS("weavingStrips"),

    /**
     * White Flowers Art Border
     * 
     */
    @XmlEnumValue("whiteFlowers")
    WHITE_FLOWERS("whiteFlowers"),

    /**
     * Woodwork Art Border
     * 
     */
    @XmlEnumValue("woodwork")
    WOODWORK("woodwork"),

    /**
     * Crisscross Art Border
     * 
     */
    @XmlEnumValue("xIllusions")
    X_ILLUSIONS("xIllusions"),

    /**
     * Triangle Art Border
     * 
     */
    @XmlEnumValue("zanyTriangles")
    ZANY_TRIANGLES("zanyTriangles"),

    /**
     * Zigzag Art Border
     * 
     */
    @XmlEnumValue("zigZag")
    ZIG_ZAG("zigZag"),

    /**
     * Zigzag stitch
     * 
     */
    @XmlEnumValue("zigZagStitch")
    ZIG_ZAG_STITCH("zigZagStitch");
    private final String value;

    STBorder(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STBorder fromValue(String v) {
        for (STBorder c: STBorder.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
