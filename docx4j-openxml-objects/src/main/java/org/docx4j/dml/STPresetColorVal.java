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
 * <p>Java class for ST_PresetColorVal.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_PresetColorVal">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="aliceBlue"/>
 *     &lt;enumeration value="antiqueWhite"/>
 *     &lt;enumeration value="aqua"/>
 *     &lt;enumeration value="aquamarine"/>
 *     &lt;enumeration value="azure"/>
 *     &lt;enumeration value="beige"/>
 *     &lt;enumeration value="bisque"/>
 *     &lt;enumeration value="black"/>
 *     &lt;enumeration value="blanchedAlmond"/>
 *     &lt;enumeration value="blue"/>
 *     &lt;enumeration value="blueViolet"/>
 *     &lt;enumeration value="brown"/>
 *     &lt;enumeration value="burlyWood"/>
 *     &lt;enumeration value="cadetBlue"/>
 *     &lt;enumeration value="chartreuse"/>
 *     &lt;enumeration value="chocolate"/>
 *     &lt;enumeration value="coral"/>
 *     &lt;enumeration value="cornflowerBlue"/>
 *     &lt;enumeration value="cornsilk"/>
 *     &lt;enumeration value="crimson"/>
 *     &lt;enumeration value="cyan"/>
 *     &lt;enumeration value="dkBlue"/>
 *     &lt;enumeration value="dkCyan"/>
 *     &lt;enumeration value="dkGoldenrod"/>
 *     &lt;enumeration value="dkGray"/>
 *     &lt;enumeration value="dkGreen"/>
 *     &lt;enumeration value="dkKhaki"/>
 *     &lt;enumeration value="dkMagenta"/>
 *     &lt;enumeration value="dkOliveGreen"/>
 *     &lt;enumeration value="dkOrange"/>
 *     &lt;enumeration value="dkOrchid"/>
 *     &lt;enumeration value="dkRed"/>
 *     &lt;enumeration value="dkSalmon"/>
 *     &lt;enumeration value="dkSeaGreen"/>
 *     &lt;enumeration value="dkSlateBlue"/>
 *     &lt;enumeration value="dkSlateGray"/>
 *     &lt;enumeration value="dkTurquoise"/>
 *     &lt;enumeration value="dkViolet"/>
 *     &lt;enumeration value="deepPink"/>
 *     &lt;enumeration value="deepSkyBlue"/>
 *     &lt;enumeration value="dimGray"/>
 *     &lt;enumeration value="dodgerBlue"/>
 *     &lt;enumeration value="firebrick"/>
 *     &lt;enumeration value="floralWhite"/>
 *     &lt;enumeration value="forestGreen"/>
 *     &lt;enumeration value="fuchsia"/>
 *     &lt;enumeration value="gainsboro"/>
 *     &lt;enumeration value="ghostWhite"/>
 *     &lt;enumeration value="gold"/>
 *     &lt;enumeration value="goldenrod"/>
 *     &lt;enumeration value="gray"/>
 *     &lt;enumeration value="green"/>
 *     &lt;enumeration value="greenYellow"/>
 *     &lt;enumeration value="honeydew"/>
 *     &lt;enumeration value="hotPink"/>
 *     &lt;enumeration value="indianRed"/>
 *     &lt;enumeration value="indigo"/>
 *     &lt;enumeration value="ivory"/>
 *     &lt;enumeration value="khaki"/>
 *     &lt;enumeration value="lavender"/>
 *     &lt;enumeration value="lavenderBlush"/>
 *     &lt;enumeration value="lawnGreen"/>
 *     &lt;enumeration value="lemonChiffon"/>
 *     &lt;enumeration value="ltBlue"/>
 *     &lt;enumeration value="ltCoral"/>
 *     &lt;enumeration value="ltCyan"/>
 *     &lt;enumeration value="ltGoldenrodYellow"/>
 *     &lt;enumeration value="ltGray"/>
 *     &lt;enumeration value="ltGreen"/>
 *     &lt;enumeration value="ltPink"/>
 *     &lt;enumeration value="ltSalmon"/>
 *     &lt;enumeration value="ltSeaGreen"/>
 *     &lt;enumeration value="ltSkyBlue"/>
 *     &lt;enumeration value="ltSlateGray"/>
 *     &lt;enumeration value="ltSteelBlue"/>
 *     &lt;enumeration value="ltYellow"/>
 *     &lt;enumeration value="lime"/>
 *     &lt;enumeration value="limeGreen"/>
 *     &lt;enumeration value="linen"/>
 *     &lt;enumeration value="magenta"/>
 *     &lt;enumeration value="maroon"/>
 *     &lt;enumeration value="medAquamarine"/>
 *     &lt;enumeration value="medBlue"/>
 *     &lt;enumeration value="medOrchid"/>
 *     &lt;enumeration value="medPurple"/>
 *     &lt;enumeration value="medSeaGreen"/>
 *     &lt;enumeration value="medSlateBlue"/>
 *     &lt;enumeration value="medSpringGreen"/>
 *     &lt;enumeration value="medTurquoise"/>
 *     &lt;enumeration value="medVioletRed"/>
 *     &lt;enumeration value="midnightBlue"/>
 *     &lt;enumeration value="mintCream"/>
 *     &lt;enumeration value="mistyRose"/>
 *     &lt;enumeration value="moccasin"/>
 *     &lt;enumeration value="navajoWhite"/>
 *     &lt;enumeration value="navy"/>
 *     &lt;enumeration value="oldLace"/>
 *     &lt;enumeration value="olive"/>
 *     &lt;enumeration value="oliveDrab"/>
 *     &lt;enumeration value="orange"/>
 *     &lt;enumeration value="orangeRed"/>
 *     &lt;enumeration value="orchid"/>
 *     &lt;enumeration value="paleGoldenrod"/>
 *     &lt;enumeration value="paleGreen"/>
 *     &lt;enumeration value="paleTurquoise"/>
 *     &lt;enumeration value="paleVioletRed"/>
 *     &lt;enumeration value="papayaWhip"/>
 *     &lt;enumeration value="peachPuff"/>
 *     &lt;enumeration value="peru"/>
 *     &lt;enumeration value="pink"/>
 *     &lt;enumeration value="plum"/>
 *     &lt;enumeration value="powderBlue"/>
 *     &lt;enumeration value="purple"/>
 *     &lt;enumeration value="red"/>
 *     &lt;enumeration value="rosyBrown"/>
 *     &lt;enumeration value="royalBlue"/>
 *     &lt;enumeration value="saddleBrown"/>
 *     &lt;enumeration value="salmon"/>
 *     &lt;enumeration value="sandyBrown"/>
 *     &lt;enumeration value="seaGreen"/>
 *     &lt;enumeration value="seaShell"/>
 *     &lt;enumeration value="sienna"/>
 *     &lt;enumeration value="silver"/>
 *     &lt;enumeration value="skyBlue"/>
 *     &lt;enumeration value="slateBlue"/>
 *     &lt;enumeration value="slateGray"/>
 *     &lt;enumeration value="snow"/>
 *     &lt;enumeration value="springGreen"/>
 *     &lt;enumeration value="steelBlue"/>
 *     &lt;enumeration value="tan"/>
 *     &lt;enumeration value="teal"/>
 *     &lt;enumeration value="thistle"/>
 *     &lt;enumeration value="tomato"/>
 *     &lt;enumeration value="turquoise"/>
 *     &lt;enumeration value="violet"/>
 *     &lt;enumeration value="wheat"/>
 *     &lt;enumeration value="white"/>
 *     &lt;enumeration value="whiteSmoke"/>
 *     &lt;enumeration value="yellow"/>
 *     &lt;enumeration value="yellowGreen"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_PresetColorVal")
@XmlEnum
public enum STPresetColorVal {


    /**
     * Alice Blue Preset Color
     * 
     */
    @XmlEnumValue("aliceBlue")
    ALICE_BLUE("aliceBlue"),

    /**
     * Antique White Preset Color
     * 
     */
    @XmlEnumValue("antiqueWhite")
    ANTIQUE_WHITE("antiqueWhite"),

    /**
     * Aqua Preset Color
     * 
     */
    @XmlEnumValue("aqua")
    AQUA("aqua"),

    /**
     * Aquamarine Preset Color
     * 
     */
    @XmlEnumValue("aquamarine")
    AQUAMARINE("aquamarine"),

    /**
     * Azure Preset Color
     * 
     */
    @XmlEnumValue("azure")
    AZURE("azure"),

    /**
     * Beige Preset Color
     * 
     */
    @XmlEnumValue("beige")
    BEIGE("beige"),

    /**
     * Bisque Preset Color
     * 
     */
    @XmlEnumValue("bisque")
    BISQUE("bisque"),

    /**
     * Black Preset Color
     * 
     */
    @XmlEnumValue("black")
    BLACK("black"),

    /**
     * Blanched Almond Preset Color
     * 
     */
    @XmlEnumValue("blanchedAlmond")
    BLANCHED_ALMOND("blanchedAlmond"),

    /**
     * Blue Preset Color
     * 
     */
    @XmlEnumValue("blue")
    BLUE("blue"),

    /**
     * Blue Violet Preset Color
     * 
     */
    @XmlEnumValue("blueViolet")
    BLUE_VIOLET("blueViolet"),

    /**
     * Brown Preset Color
     * 
     */
    @XmlEnumValue("brown")
    BROWN("brown"),

    /**
     * Burly Wood Preset Color
     * 
     */
    @XmlEnumValue("burlyWood")
    BURLY_WOOD("burlyWood"),

    /**
     * Cadet Blue Preset Color
     * 
     */
    @XmlEnumValue("cadetBlue")
    CADET_BLUE("cadetBlue"),

    /**
     * Chartreuse Preset Color
     * 
     */
    @XmlEnumValue("chartreuse")
    CHARTREUSE("chartreuse"),

    /**
     * Chocolate Preset Color
     * 
     */
    @XmlEnumValue("chocolate")
    CHOCOLATE("chocolate"),

    /**
     * Coral Preset Color
     * 
     */
    @XmlEnumValue("coral")
    CORAL("coral"),

    /**
     * Cornflower Blue Preset Color
     * 
     */
    @XmlEnumValue("cornflowerBlue")
    CORNFLOWER_BLUE("cornflowerBlue"),

    /**
     * Cornsilk Preset Color
     * 
     */
    @XmlEnumValue("cornsilk")
    CORNSILK("cornsilk"),

    /**
     * Crimson Preset Color
     * 
     */
    @XmlEnumValue("crimson")
    CRIMSON("crimson"),

    /**
     * Cyan Preset Color
     * 
     */
    @XmlEnumValue("cyan")
    CYAN("cyan"),

    /**
     * Dark Blue Preset Color
     * 
     */
    @XmlEnumValue("dkBlue")
    DK_BLUE("dkBlue"),

    /**
     * Dark Cyan Preset Color
     * 
     */
    @XmlEnumValue("dkCyan")
    DK_CYAN("dkCyan"),

    /**
     * Dark Goldenrod Preset Color
     * 
     */
    @XmlEnumValue("dkGoldenrod")
    DK_GOLDENROD("dkGoldenrod"),

    /**
     * Dark Gray Preset Color
     * 
     */
    @XmlEnumValue("dkGray")
    DK_GRAY("dkGray"),

    /**
     * Dark Green Preset Color
     * 
     */
    @XmlEnumValue("dkGreen")
    DK_GREEN("dkGreen"),

    /**
     * Dark Khaki Preset Color
     * 
     */
    @XmlEnumValue("dkKhaki")
    DK_KHAKI("dkKhaki"),

    /**
     * Dark Magenta Preset Color
     * 
     */
    @XmlEnumValue("dkMagenta")
    DK_MAGENTA("dkMagenta"),

    /**
     * Dark Olive Green Preset Color
     * 
     */
    @XmlEnumValue("dkOliveGreen")
    DK_OLIVE_GREEN("dkOliveGreen"),

    /**
     * Dark Orange Preset Color
     * 
     */
    @XmlEnumValue("dkOrange")
    DK_ORANGE("dkOrange"),

    /**
     * Dark Orchid Preset Color
     * 
     */
    @XmlEnumValue("dkOrchid")
    DK_ORCHID("dkOrchid"),

    /**
     * Dark Red Preset Color
     * 
     */
    @XmlEnumValue("dkRed")
    DK_RED("dkRed"),

    /**
     * Dark Salmon Preset Color
     * 
     */
    @XmlEnumValue("dkSalmon")
    DK_SALMON("dkSalmon"),

    /**
     * Dark Sea Green Preset Color
     * 
     */
    @XmlEnumValue("dkSeaGreen")
    DK_SEA_GREEN("dkSeaGreen"),

    /**
     * Dark Slate Blue Preset Color
     * 
     */
    @XmlEnumValue("dkSlateBlue")
    DK_SLATE_BLUE("dkSlateBlue"),

    /**
     * Dark Slate Gray Preset Color
     * 
     */
    @XmlEnumValue("dkSlateGray")
    DK_SLATE_GRAY("dkSlateGray"),

    /**
     * Dark Turquoise Preset Color
     * 
     */
    @XmlEnumValue("dkTurquoise")
    DK_TURQUOISE("dkTurquoise"),

    /**
     * Dark Violet Preset Color
     * 
     */
    @XmlEnumValue("dkViolet")
    DK_VIOLET("dkViolet"),

    /**
     * Deep Pink Preset Color
     * 
     */
    @XmlEnumValue("deepPink")
    DEEP_PINK("deepPink"),

    /**
     * Deep Sky Blue Preset Color
     * 
     */
    @XmlEnumValue("deepSkyBlue")
    DEEP_SKY_BLUE("deepSkyBlue"),

    /**
     * Dim Gray Preset Color
     * 
     */
    @XmlEnumValue("dimGray")
    DIM_GRAY("dimGray"),

    /**
     * Dodger Blue Preset Color
     * 
     */
    @XmlEnumValue("dodgerBlue")
    DODGER_BLUE("dodgerBlue"),

    /**
     * Firebrick Preset Color
     * 
     */
    @XmlEnumValue("firebrick")
    FIREBRICK("firebrick"),

    /**
     * Floral White Preset Color
     * 
     */
    @XmlEnumValue("floralWhite")
    FLORAL_WHITE("floralWhite"),

    /**
     * Forest Green Preset Color
     * 
     */
    @XmlEnumValue("forestGreen")
    FOREST_GREEN("forestGreen"),

    /**
     * Fuchsia Preset Color
     * 
     */
    @XmlEnumValue("fuchsia")
    FUCHSIA("fuchsia"),

    /**
     * Gainsboro Preset Color
     * 
     */
    @XmlEnumValue("gainsboro")
    GAINSBORO("gainsboro"),

    /**
     * Ghost White Preset Color
     * 
     */
    @XmlEnumValue("ghostWhite")
    GHOST_WHITE("ghostWhite"),

    /**
     * Gold Preset Color
     * 
     */
    @XmlEnumValue("gold")
    GOLD("gold"),

    /**
     * Goldenrod Preset Color
     * 
     */
    @XmlEnumValue("goldenrod")
    GOLDENROD("goldenrod"),

    /**
     * Gray Preset Color
     * 
     */
    @XmlEnumValue("gray")
    GRAY("gray"),

    /**
     * Green Preset Color
     * 
     */
    @XmlEnumValue("green")
    GREEN("green"),

    /**
     * Green Yellow Preset Color
     * 
     */
    @XmlEnumValue("greenYellow")
    GREEN_YELLOW("greenYellow"),

    /**
     * Honeydew Preset Color
     * 
     */
    @XmlEnumValue("honeydew")
    HONEYDEW("honeydew"),

    /**
     * Hot Pink Preset Color
     * 
     */
    @XmlEnumValue("hotPink")
    HOT_PINK("hotPink"),

    /**
     * Indian Red Preset Color
     * 
     */
    @XmlEnumValue("indianRed")
    INDIAN_RED("indianRed"),

    /**
     * Indigo Preset Color
     * 
     */
    @XmlEnumValue("indigo")
    INDIGO("indigo"),

    /**
     * Ivory Preset Color
     * 
     */
    @XmlEnumValue("ivory")
    IVORY("ivory"),

    /**
     * Khaki Preset Color
     * 
     */
    @XmlEnumValue("khaki")
    KHAKI("khaki"),

    /**
     * Lavender Preset Color
     * 
     */
    @XmlEnumValue("lavender")
    LAVENDER("lavender"),

    /**
     * Lavender Blush Preset Color
     * 
     */
    @XmlEnumValue("lavenderBlush")
    LAVENDER_BLUSH("lavenderBlush"),

    /**
     * Lawn Green Preset Color
     * 
     */
    @XmlEnumValue("lawnGreen")
    LAWN_GREEN("lawnGreen"),

    /**
     * Lemon Chiffon Preset Color
     * 
     */
    @XmlEnumValue("lemonChiffon")
    LEMON_CHIFFON("lemonChiffon"),

    /**
     * Light Blue Preset Color
     * 
     */
    @XmlEnumValue("ltBlue")
    LT_BLUE("ltBlue"),

    /**
     * Light Coral Preset Color
     * 
     */
    @XmlEnumValue("ltCoral")
    LT_CORAL("ltCoral"),

    /**
     * Light Cyan Preset Color
     * 
     */
    @XmlEnumValue("ltCyan")
    LT_CYAN("ltCyan"),

    /**
     * Light Goldenrod Yellow Preset Color
     * 
     */
    @XmlEnumValue("ltGoldenrodYellow")
    LT_GOLDENROD_YELLOW("ltGoldenrodYellow"),

    /**
     * Light Gray Preset Color
     * 
     */
    @XmlEnumValue("ltGray")
    LT_GRAY("ltGray"),

    /**
     * Light Green Preset Color
     * 
     */
    @XmlEnumValue("ltGreen")
    LT_GREEN("ltGreen"),

    /**
     * Light Pink Preset Color
     * 
     */
    @XmlEnumValue("ltPink")
    LT_PINK("ltPink"),

    /**
     * Light Salmon Preset Color
     * 
     */
    @XmlEnumValue("ltSalmon")
    LT_SALMON("ltSalmon"),

    /**
     * Light Sea Green Preset Color
     * 
     */
    @XmlEnumValue("ltSeaGreen")
    LT_SEA_GREEN("ltSeaGreen"),

    /**
     * Light Sky Blue Preset Color
     * 
     */
    @XmlEnumValue("ltSkyBlue")
    LT_SKY_BLUE("ltSkyBlue"),

    /**
     * Light Slate Gray Preset Color
     * 
     */
    @XmlEnumValue("ltSlateGray")
    LT_SLATE_GRAY("ltSlateGray"),

    /**
     * Light Steel Blue Preset Color
     * 
     */
    @XmlEnumValue("ltSteelBlue")
    LT_STEEL_BLUE("ltSteelBlue"),

    /**
     * Light Yellow Preset Color
     * 
     */
    @XmlEnumValue("ltYellow")
    LT_YELLOW("ltYellow"),

    /**
     * Lime Preset Color
     * 
     */
    @XmlEnumValue("lime")
    LIME("lime"),

    /**
     * Lime Green Preset Color
     * 
     */
    @XmlEnumValue("limeGreen")
    LIME_GREEN("limeGreen"),

    /**
     * Linen Preset Color
     * 
     */
    @XmlEnumValue("linen")
    LINEN("linen"),

    /**
     * Magenta Preset Color
     * 
     */
    @XmlEnumValue("magenta")
    MAGENTA("magenta"),

    /**
     * Maroon Preset Color
     * 
     */
    @XmlEnumValue("maroon")
    MAROON("maroon"),

    /**
     * Medium Aquamarine Preset Color
     * 
     */
    @XmlEnumValue("medAquamarine")
    MED_AQUAMARINE("medAquamarine"),

    /**
     * Medium Blue Preset Color
     * 
     */
    @XmlEnumValue("medBlue")
    MED_BLUE("medBlue"),

    /**
     * Medium Orchid Preset Color
     * 
     */
    @XmlEnumValue("medOrchid")
    MED_ORCHID("medOrchid"),

    /**
     * Medium Purple Preset Color
     * 
     */
    @XmlEnumValue("medPurple")
    MED_PURPLE("medPurple"),

    /**
     * Medium Sea Green Preset Color
     * 
     */
    @XmlEnumValue("medSeaGreen")
    MED_SEA_GREEN("medSeaGreen"),

    /**
     * Medium Slate Blue Preset Color
     * 
     */
    @XmlEnumValue("medSlateBlue")
    MED_SLATE_BLUE("medSlateBlue"),

    /**
     * Medium Spring Green Preset Color
     * 
     */
    @XmlEnumValue("medSpringGreen")
    MED_SPRING_GREEN("medSpringGreen"),

    /**
     * Medium Turquoise Preset Color
     * 
     */
    @XmlEnumValue("medTurquoise")
    MED_TURQUOISE("medTurquoise"),

    /**
     * Medium Violet Red Preset Color
     * 
     */
    @XmlEnumValue("medVioletRed")
    MED_VIOLET_RED("medVioletRed"),

    /**
     * Midnight Blue Preset Color
     * 
     */
    @XmlEnumValue("midnightBlue")
    MIDNIGHT_BLUE("midnightBlue"),

    /**
     * Mint Cream Preset Color
     * 
     */
    @XmlEnumValue("mintCream")
    MINT_CREAM("mintCream"),

    /**
     * Misty Rose Preset Color
     * 
     */
    @XmlEnumValue("mistyRose")
    MISTY_ROSE("mistyRose"),

    /**
     * Moccasin Preset Color
     * 
     */
    @XmlEnumValue("moccasin")
    MOCCASIN("moccasin"),

    /**
     * Navajo White Preset Color
     * 
     */
    @XmlEnumValue("navajoWhite")
    NAVAJO_WHITE("navajoWhite"),

    /**
     * Navy Preset Color
     * 
     */
    @XmlEnumValue("navy")
    NAVY("navy"),

    /**
     * Old Lace Preset Color
     * 
     */
    @XmlEnumValue("oldLace")
    OLD_LACE("oldLace"),

    /**
     * Olive Preset Color
     * 
     */
    @XmlEnumValue("olive")
    OLIVE("olive"),

    /**
     * Olive Drab Preset Color
     * 
     */
    @XmlEnumValue("oliveDrab")
    OLIVE_DRAB("oliveDrab"),

    /**
     * Orange Preset Color
     * 
     */
    @XmlEnumValue("orange")
    ORANGE("orange"),

    /**
     * Orange Red Preset Color
     * 
     */
    @XmlEnumValue("orangeRed")
    ORANGE_RED("orangeRed"),

    /**
     * Orchid Preset Color
     * 
     */
    @XmlEnumValue("orchid")
    ORCHID("orchid"),

    /**
     * Pale Goldenrod Preset Color
     * 
     */
    @XmlEnumValue("paleGoldenrod")
    PALE_GOLDENROD("paleGoldenrod"),

    /**
     * Pale Green Preset Color
     * 
     */
    @XmlEnumValue("paleGreen")
    PALE_GREEN("paleGreen"),

    /**
     * Pale Turquoise Preset Color
     * 
     */
    @XmlEnumValue("paleTurquoise")
    PALE_TURQUOISE("paleTurquoise"),

    /**
     * Pale Violet Red Preset Color
     * 
     */
    @XmlEnumValue("paleVioletRed")
    PALE_VIOLET_RED("paleVioletRed"),

    /**
     * Papaya Whip Preset Color
     * 
     */
    @XmlEnumValue("papayaWhip")
    PAPAYA_WHIP("papayaWhip"),

    /**
     * Peach Puff Preset Color
     * 
     */
    @XmlEnumValue("peachPuff")
    PEACH_PUFF("peachPuff"),

    /**
     * Peru Preset Color
     * 
     */
    @XmlEnumValue("peru")
    PERU("peru"),

    /**
     * Pink Preset Color
     * 
     */
    @XmlEnumValue("pink")
    PINK("pink"),

    /**
     * Plum Preset Color
     * 
     */
    @XmlEnumValue("plum")
    PLUM("plum"),

    /**
     * Powder Blue Preset Color
     * 
     */
    @XmlEnumValue("powderBlue")
    POWDER_BLUE("powderBlue"),

    /**
     * Purple Preset Color
     * 
     */
    @XmlEnumValue("purple")
    PURPLE("purple"),

    /**
     * Red Preset Color
     * 
     */
    @XmlEnumValue("red")
    RED("red"),

    /**
     * Rosy Brown Preset Color
     * 
     */
    @XmlEnumValue("rosyBrown")
    ROSY_BROWN("rosyBrown"),

    /**
     * Royal Blue Preset Color
     * 
     */
    @XmlEnumValue("royalBlue")
    ROYAL_BLUE("royalBlue"),

    /**
     * Saddle Brown Preset Color
     * 
     */
    @XmlEnumValue("saddleBrown")
    SADDLE_BROWN("saddleBrown"),

    /**
     * Salmon Preset Color
     * 
     */
    @XmlEnumValue("salmon")
    SALMON("salmon"),

    /**
     * Sandy Brown Preset Color
     * 
     */
    @XmlEnumValue("sandyBrown")
    SANDY_BROWN("sandyBrown"),

    /**
     * Sea Green Preset Color
     * 
     */
    @XmlEnumValue("seaGreen")
    SEA_GREEN("seaGreen"),

    /**
     * Sea Shell Preset Color
     * 
     */
    @XmlEnumValue("seaShell")
    SEA_SHELL("seaShell"),

    /**
     * Sienna Preset Color
     * 
     */
    @XmlEnumValue("sienna")
    SIENNA("sienna"),

    /**
     * Silver Preset Color
     * 
     */
    @XmlEnumValue("silver")
    SILVER("silver"),

    /**
     * Sky Blue Preset Color
     * 
     */
    @XmlEnumValue("skyBlue")
    SKY_BLUE("skyBlue"),

    /**
     * Slate Blue Preset Color
     * 
     */
    @XmlEnumValue("slateBlue")
    SLATE_BLUE("slateBlue"),

    /**
     * Slate Gray Preset Color
     * 
     */
    @XmlEnumValue("slateGray")
    SLATE_GRAY("slateGray"),

    /**
     * Snow Preset Color
     * 
     */
    @XmlEnumValue("snow")
    SNOW("snow"),

    /**
     * Spring Green Preset Color
     * 
     */
    @XmlEnumValue("springGreen")
    SPRING_GREEN("springGreen"),

    /**
     * Steel Blue Preset Color
     * 
     */
    @XmlEnumValue("steelBlue")
    STEEL_BLUE("steelBlue"),

    /**
     * Tan Preset Color
     * 
     */
    @XmlEnumValue("tan")
    TAN("tan"),

    /**
     * Teal Preset Color
     * 
     */
    @XmlEnumValue("teal")
    TEAL("teal"),

    /**
     * Thistle Preset Color
     * 
     */
    @XmlEnumValue("thistle")
    THISTLE("thistle"),

    /**
     * Tomato Preset Color
     * 
     */
    @XmlEnumValue("tomato")
    TOMATO("tomato"),

    /**
     * Turquoise Preset Color
     * 
     */
    @XmlEnumValue("turquoise")
    TURQUOISE("turquoise"),

    /**
     * Violet Preset Color
     * 
     */
    @XmlEnumValue("violet")
    VIOLET("violet"),

    /**
     * Wheat Preset Color
     * 
     */
    @XmlEnumValue("wheat")
    WHEAT("wheat"),

    /**
     * White Preset Color
     * 
     */
    @XmlEnumValue("white")
    WHITE("white"),

    /**
     * White Smoke Preset Color
     * 
     */
    @XmlEnumValue("whiteSmoke")
    WHITE_SMOKE("whiteSmoke"),

    /**
     * Yellow Preset Color
     * 
     */
    @XmlEnumValue("yellow")
    YELLOW("yellow"),

    /**
     * Yellow Green Preset Color
     * 
     */
    @XmlEnumValue("yellowGreen")
    YELLOW_GREEN("yellowGreen");
    private final String value;

    STPresetColorVal(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STPresetColorVal fromValue(String v) {
        for (STPresetColorVal c: STPresetColorVal.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
