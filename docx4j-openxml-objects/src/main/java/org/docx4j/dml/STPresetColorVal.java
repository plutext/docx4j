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
 * &lt;simpleType name="ST_PresetColorVal"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="aliceBlue"/&gt;
 *     &lt;enumeration value="antiqueWhite"/&gt;
 *     &lt;enumeration value="aqua"/&gt;
 *     &lt;enumeration value="aquamarine"/&gt;
 *     &lt;enumeration value="azure"/&gt;
 *     &lt;enumeration value="beige"/&gt;
 *     &lt;enumeration value="bisque"/&gt;
 *     &lt;enumeration value="black"/&gt;
 *     &lt;enumeration value="blanchedAlmond"/&gt;
 *     &lt;enumeration value="blue"/&gt;
 *     &lt;enumeration value="blueViolet"/&gt;
 *     &lt;enumeration value="brown"/&gt;
 *     &lt;enumeration value="burlyWood"/&gt;
 *     &lt;enumeration value="cadetBlue"/&gt;
 *     &lt;enumeration value="chartreuse"/&gt;
 *     &lt;enumeration value="chocolate"/&gt;
 *     &lt;enumeration value="coral"/&gt;
 *     &lt;enumeration value="cornflowerBlue"/&gt;
 *     &lt;enumeration value="cornsilk"/&gt;
 *     &lt;enumeration value="crimson"/&gt;
 *     &lt;enumeration value="cyan"/&gt;
 *     &lt;enumeration value="dkBlue"/&gt;
 *     &lt;enumeration value="dkCyan"/&gt;
 *     &lt;enumeration value="dkGoldenrod"/&gt;
 *     &lt;enumeration value="dkGray"/&gt;
 *     &lt;enumeration value="dkGreen"/&gt;
 *     &lt;enumeration value="dkKhaki"/&gt;
 *     &lt;enumeration value="dkMagenta"/&gt;
 *     &lt;enumeration value="dkOliveGreen"/&gt;
 *     &lt;enumeration value="dkOrange"/&gt;
 *     &lt;enumeration value="dkOrchid"/&gt;
 *     &lt;enumeration value="dkRed"/&gt;
 *     &lt;enumeration value="dkSalmon"/&gt;
 *     &lt;enumeration value="dkSeaGreen"/&gt;
 *     &lt;enumeration value="dkSlateBlue"/&gt;
 *     &lt;enumeration value="dkSlateGray"/&gt;
 *     &lt;enumeration value="dkTurquoise"/&gt;
 *     &lt;enumeration value="dkViolet"/&gt;
 *     &lt;enumeration value="deepPink"/&gt;
 *     &lt;enumeration value="deepSkyBlue"/&gt;
 *     &lt;enumeration value="dimGray"/&gt;
 *     &lt;enumeration value="dodgerBlue"/&gt;
 *     &lt;enumeration value="firebrick"/&gt;
 *     &lt;enumeration value="floralWhite"/&gt;
 *     &lt;enumeration value="forestGreen"/&gt;
 *     &lt;enumeration value="fuchsia"/&gt;
 *     &lt;enumeration value="gainsboro"/&gt;
 *     &lt;enumeration value="ghostWhite"/&gt;
 *     &lt;enumeration value="gold"/&gt;
 *     &lt;enumeration value="goldenrod"/&gt;
 *     &lt;enumeration value="gray"/&gt;
 *     &lt;enumeration value="green"/&gt;
 *     &lt;enumeration value="greenYellow"/&gt;
 *     &lt;enumeration value="honeydew"/&gt;
 *     &lt;enumeration value="hotPink"/&gt;
 *     &lt;enumeration value="indianRed"/&gt;
 *     &lt;enumeration value="indigo"/&gt;
 *     &lt;enumeration value="ivory"/&gt;
 *     &lt;enumeration value="khaki"/&gt;
 *     &lt;enumeration value="lavender"/&gt;
 *     &lt;enumeration value="lavenderBlush"/&gt;
 *     &lt;enumeration value="lawnGreen"/&gt;
 *     &lt;enumeration value="lemonChiffon"/&gt;
 *     &lt;enumeration value="ltBlue"/&gt;
 *     &lt;enumeration value="ltCoral"/&gt;
 *     &lt;enumeration value="ltCyan"/&gt;
 *     &lt;enumeration value="ltGoldenrodYellow"/&gt;
 *     &lt;enumeration value="ltGray"/&gt;
 *     &lt;enumeration value="ltGreen"/&gt;
 *     &lt;enumeration value="ltPink"/&gt;
 *     &lt;enumeration value="ltSalmon"/&gt;
 *     &lt;enumeration value="ltSeaGreen"/&gt;
 *     &lt;enumeration value="ltSkyBlue"/&gt;
 *     &lt;enumeration value="ltSlateGray"/&gt;
 *     &lt;enumeration value="ltSteelBlue"/&gt;
 *     &lt;enumeration value="ltYellow"/&gt;
 *     &lt;enumeration value="lime"/&gt;
 *     &lt;enumeration value="limeGreen"/&gt;
 *     &lt;enumeration value="linen"/&gt;
 *     &lt;enumeration value="magenta"/&gt;
 *     &lt;enumeration value="maroon"/&gt;
 *     &lt;enumeration value="medAquamarine"/&gt;
 *     &lt;enumeration value="medBlue"/&gt;
 *     &lt;enumeration value="medOrchid"/&gt;
 *     &lt;enumeration value="medPurple"/&gt;
 *     &lt;enumeration value="medSeaGreen"/&gt;
 *     &lt;enumeration value="medSlateBlue"/&gt;
 *     &lt;enumeration value="medSpringGreen"/&gt;
 *     &lt;enumeration value="medTurquoise"/&gt;
 *     &lt;enumeration value="medVioletRed"/&gt;
 *     &lt;enumeration value="midnightBlue"/&gt;
 *     &lt;enumeration value="mintCream"/&gt;
 *     &lt;enumeration value="mistyRose"/&gt;
 *     &lt;enumeration value="moccasin"/&gt;
 *     &lt;enumeration value="navajoWhite"/&gt;
 *     &lt;enumeration value="navy"/&gt;
 *     &lt;enumeration value="oldLace"/&gt;
 *     &lt;enumeration value="olive"/&gt;
 *     &lt;enumeration value="oliveDrab"/&gt;
 *     &lt;enumeration value="orange"/&gt;
 *     &lt;enumeration value="orangeRed"/&gt;
 *     &lt;enumeration value="orchid"/&gt;
 *     &lt;enumeration value="paleGoldenrod"/&gt;
 *     &lt;enumeration value="paleGreen"/&gt;
 *     &lt;enumeration value="paleTurquoise"/&gt;
 *     &lt;enumeration value="paleVioletRed"/&gt;
 *     &lt;enumeration value="papayaWhip"/&gt;
 *     &lt;enumeration value="peachPuff"/&gt;
 *     &lt;enumeration value="peru"/&gt;
 *     &lt;enumeration value="pink"/&gt;
 *     &lt;enumeration value="plum"/&gt;
 *     &lt;enumeration value="powderBlue"/&gt;
 *     &lt;enumeration value="purple"/&gt;
 *     &lt;enumeration value="red"/&gt;
 *     &lt;enumeration value="rosyBrown"/&gt;
 *     &lt;enumeration value="royalBlue"/&gt;
 *     &lt;enumeration value="saddleBrown"/&gt;
 *     &lt;enumeration value="salmon"/&gt;
 *     &lt;enumeration value="sandyBrown"/&gt;
 *     &lt;enumeration value="seaGreen"/&gt;
 *     &lt;enumeration value="seaShell"/&gt;
 *     &lt;enumeration value="sienna"/&gt;
 *     &lt;enumeration value="silver"/&gt;
 *     &lt;enumeration value="skyBlue"/&gt;
 *     &lt;enumeration value="slateBlue"/&gt;
 *     &lt;enumeration value="slateGray"/&gt;
 *     &lt;enumeration value="snow"/&gt;
 *     &lt;enumeration value="springGreen"/&gt;
 *     &lt;enumeration value="steelBlue"/&gt;
 *     &lt;enumeration value="tan"/&gt;
 *     &lt;enumeration value="teal"/&gt;
 *     &lt;enumeration value="thistle"/&gt;
 *     &lt;enumeration value="tomato"/&gt;
 *     &lt;enumeration value="turquoise"/&gt;
 *     &lt;enumeration value="violet"/&gt;
 *     &lt;enumeration value="wheat"/&gt;
 *     &lt;enumeration value="white"/&gt;
 *     &lt;enumeration value="whiteSmoke"/&gt;
 *     &lt;enumeration value="yellow"/&gt;
 *     &lt;enumeration value="yellowGreen"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
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
