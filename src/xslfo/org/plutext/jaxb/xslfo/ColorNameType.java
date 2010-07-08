
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for color_Name_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="color_Name_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="aliceblue"/>
 *     &lt;enumeration value="antiquewhite"/>
 *     &lt;enumeration value="aqua"/>
 *     &lt;enumeration value="aquamarine"/>
 *     &lt;enumeration value="azure"/>
 *     &lt;enumeration value="beige"/>
 *     &lt;enumeration value="bisque"/>
 *     &lt;enumeration value="black"/>
 *     &lt;enumeration value="blanchedalmond"/>
 *     &lt;enumeration value="blue"/>
 *     &lt;enumeration value="blueviolet"/>
 *     &lt;enumeration value="brown"/>
 *     &lt;enumeration value="burlywood"/>
 *     &lt;enumeration value="cadetblue"/>
 *     &lt;enumeration value="chartreuse"/>
 *     &lt;enumeration value="chocolate"/>
 *     &lt;enumeration value="coral"/>
 *     &lt;enumeration value="cornflowerblue"/>
 *     &lt;enumeration value="cornsilk"/>
 *     &lt;enumeration value="crimson"/>
 *     &lt;enumeration value="cyan"/>
 *     &lt;enumeration value="darkblue"/>
 *     &lt;enumeration value="darkcyan"/>
 *     &lt;enumeration value="darkgoldenrod"/>
 *     &lt;enumeration value="darkgray"/>
 *     &lt;enumeration value="darkgreen"/>
 *     &lt;enumeration value="darkgrey"/>
 *     &lt;enumeration value="darkkhaki"/>
 *     &lt;enumeration value="darkmagenta"/>
 *     &lt;enumeration value="darkolivegreen"/>
 *     &lt;enumeration value="darkorange"/>
 *     &lt;enumeration value="darkorchid"/>
 *     &lt;enumeration value="darkred"/>
 *     &lt;enumeration value="darksalmon"/>
 *     &lt;enumeration value="darkseagreen"/>
 *     &lt;enumeration value="darkslateblue"/>
 *     &lt;enumeration value="darkslategray"/>
 *     &lt;enumeration value="darkslategrey"/>
 *     &lt;enumeration value="darkturquoise"/>
 *     &lt;enumeration value="darkviolet"/>
 *     &lt;enumeration value="deeppink"/>
 *     &lt;enumeration value="deepskyblue"/>
 *     &lt;enumeration value="dimgray"/>
 *     &lt;enumeration value="dimgrey"/>
 *     &lt;enumeration value="dodgerblue"/>
 *     &lt;enumeration value="firebrick"/>
 *     &lt;enumeration value="floralwhite"/>
 *     &lt;enumeration value="forestgreen"/>
 *     &lt;enumeration value="fuchsia"/>
 *     &lt;enumeration value="gainsboro"/>
 *     &lt;enumeration value="lightpink"/>
 *     &lt;enumeration value="lightsalmon"/>
 *     &lt;enumeration value="lightseagreen"/>
 *     &lt;enumeration value="lightskyblue"/>
 *     &lt;enumeration value="lightslategray"/>
 *     &lt;enumeration value="lightslategrey"/>
 *     &lt;enumeration value="lightsteelblue"/>
 *     &lt;enumeration value="lightyellow"/>
 *     &lt;enumeration value="lime"/>
 *     &lt;enumeration value="limegreen"/>
 *     &lt;enumeration value="linen"/>
 *     &lt;enumeration value="magenta"/>
 *     &lt;enumeration value="maroon"/>
 *     &lt;enumeration value="mediumaquamarine"/>
 *     &lt;enumeration value="mediumblue"/>
 *     &lt;enumeration value="mediumorchid"/>
 *     &lt;enumeration value="mediumpurple"/>
 *     &lt;enumeration value="mediumseagreen"/>
 *     &lt;enumeration value="mediumslateblue"/>
 *     &lt;enumeration value="mediumspringgreen"/>
 *     &lt;enumeration value="mediumturquoise"/>
 *     &lt;enumeration value="mediumvioletred"/>
 *     &lt;enumeration value="midnightblue"/>
 *     &lt;enumeration value="mintcream"/>
 *     &lt;enumeration value="mistyrose"/>
 *     &lt;enumeration value="moccasin"/>
 *     &lt;enumeration value="navajowhite"/>
 *     &lt;enumeration value="navy"/>
 *     &lt;enumeration value="oldlace"/>
 *     &lt;enumeration value="olive"/>
 *     &lt;enumeration value="olivedrab"/>
 *     &lt;enumeration value="orange"/>
 *     &lt;enumeration value="orangered"/>
 *     &lt;enumeration value="orchid"/>
 *     &lt;enumeration value="palegoldenrod"/>
 *     &lt;enumeration value="palegreen"/>
 *     &lt;enumeration value="paleturquoise"/>
 *     &lt;enumeration value="palevioletred"/>
 *     &lt;enumeration value="papayawhip"/>
 *     &lt;enumeration value="peachpuff"/>
 *     &lt;enumeration value="peru"/>
 *     &lt;enumeration value="pink"/>
 *     &lt;enumeration value="plum"/>
 *     &lt;enumeration value="powderblue"/>
 *     &lt;enumeration value="purple"/>
 *     &lt;enumeration value="red"/>
 *     &lt;enumeration value="rosybrown"/>
 *     &lt;enumeration value="royalblue"/>
 *     &lt;enumeration value="saddlebrown"/>
 *     &lt;enumeration value="salmon"/>
 *     &lt;enumeration value="ghostwhite"/>
 *     &lt;enumeration value="gold"/>
 *     &lt;enumeration value="goldenrod"/>
 *     &lt;enumeration value="gray"/>
 *     &lt;enumeration value="grey"/>
 *     &lt;enumeration value="green"/>
 *     &lt;enumeration value="greenyellow"/>
 *     &lt;enumeration value="honeydew"/>
 *     &lt;enumeration value="hotpink"/>
 *     &lt;enumeration value="indianred"/>
 *     &lt;enumeration value="indigo"/>
 *     &lt;enumeration value="ivory"/>
 *     &lt;enumeration value="khaki"/>
 *     &lt;enumeration value="lavender"/>
 *     &lt;enumeration value="lavenderblush"/>
 *     &lt;enumeration value="lawngreen"/>
 *     &lt;enumeration value="lemonchiffon"/>
 *     &lt;enumeration value="lightblue"/>
 *     &lt;enumeration value="lightcoral"/>
 *     &lt;enumeration value="lightcyan"/>
 *     &lt;enumeration value="lightgoldenrodyellow"/>
 *     &lt;enumeration value="lightgray"/>
 *     &lt;enumeration value="lightgreen"/>
 *     &lt;enumeration value="lightgrey"/>
 *     &lt;enumeration value="sandybrown"/>
 *     &lt;enumeration value="seagreen"/>
 *     &lt;enumeration value="seashell"/>
 *     &lt;enumeration value="sienna"/>
 *     &lt;enumeration value="silver"/>
 *     &lt;enumeration value="skyblue"/>
 *     &lt;enumeration value="slateblue"/>
 *     &lt;enumeration value="slategray"/>
 *     &lt;enumeration value="slategrey"/>
 *     &lt;enumeration value="snow"/>
 *     &lt;enumeration value="springgreen"/>
 *     &lt;enumeration value="steelblue"/>
 *     &lt;enumeration value="tan"/>
 *     &lt;enumeration value="teal"/>
 *     &lt;enumeration value="thistle"/>
 *     &lt;enumeration value="tomato"/>
 *     &lt;enumeration value="turquoise"/>
 *     &lt;enumeration value="violet"/>
 *     &lt;enumeration value="wheat"/>
 *     &lt;enumeration value="white"/>
 *     &lt;enumeration value="whitesmoke"/>
 *     &lt;enumeration value="yellow"/>
 *     &lt;enumeration value="yellowgreen"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "color_Name_Type")
@XmlEnum
public enum ColorNameType {

    @XmlEnumValue("aliceblue")
    ALICEBLUE("aliceblue"),
    @XmlEnumValue("antiquewhite")
    ANTIQUEWHITE("antiquewhite"),
    @XmlEnumValue("aqua")
    AQUA("aqua"),
    @XmlEnumValue("aquamarine")
    AQUAMARINE("aquamarine"),
    @XmlEnumValue("azure")
    AZURE("azure"),
    @XmlEnumValue("beige")
    BEIGE("beige"),
    @XmlEnumValue("bisque")
    BISQUE("bisque"),
    @XmlEnumValue("black")
    BLACK("black"),
    @XmlEnumValue("blanchedalmond")
    BLANCHEDALMOND("blanchedalmond"),
    @XmlEnumValue("blue")
    BLUE("blue"),
    @XmlEnumValue("blueviolet")
    BLUEVIOLET("blueviolet"),
    @XmlEnumValue("brown")
    BROWN("brown"),
    @XmlEnumValue("burlywood")
    BURLYWOOD("burlywood"),
    @XmlEnumValue("cadetblue")
    CADETBLUE("cadetblue"),
    @XmlEnumValue("chartreuse")
    CHARTREUSE("chartreuse"),
    @XmlEnumValue("chocolate")
    CHOCOLATE("chocolate"),
    @XmlEnumValue("coral")
    CORAL("coral"),
    @XmlEnumValue("cornflowerblue")
    CORNFLOWERBLUE("cornflowerblue"),
    @XmlEnumValue("cornsilk")
    CORNSILK("cornsilk"),
    @XmlEnumValue("crimson")
    CRIMSON("crimson"),
    @XmlEnumValue("cyan")
    CYAN("cyan"),
    @XmlEnumValue("darkblue")
    DARKBLUE("darkblue"),
    @XmlEnumValue("darkcyan")
    DARKCYAN("darkcyan"),
    @XmlEnumValue("darkgoldenrod")
    DARKGOLDENROD("darkgoldenrod"),
    @XmlEnumValue("darkgray")
    DARKGRAY("darkgray"),
    @XmlEnumValue("darkgreen")
    DARKGREEN("darkgreen"),
    @XmlEnumValue("darkgrey")
    DARKGREY("darkgrey"),
    @XmlEnumValue("darkkhaki")
    DARKKHAKI("darkkhaki"),
    @XmlEnumValue("darkmagenta")
    DARKMAGENTA("darkmagenta"),
    @XmlEnumValue("darkolivegreen")
    DARKOLIVEGREEN("darkolivegreen"),
    @XmlEnumValue("darkorange")
    DARKORANGE("darkorange"),
    @XmlEnumValue("darkorchid")
    DARKORCHID("darkorchid"),
    @XmlEnumValue("darkred")
    DARKRED("darkred"),
    @XmlEnumValue("darksalmon")
    DARKSALMON("darksalmon"),
    @XmlEnumValue("darkseagreen")
    DARKSEAGREEN("darkseagreen"),
    @XmlEnumValue("darkslateblue")
    DARKSLATEBLUE("darkslateblue"),
    @XmlEnumValue("darkslategray")
    DARKSLATEGRAY("darkslategray"),
    @XmlEnumValue("darkslategrey")
    DARKSLATEGREY("darkslategrey"),
    @XmlEnumValue("darkturquoise")
    DARKTURQUOISE("darkturquoise"),
    @XmlEnumValue("darkviolet")
    DARKVIOLET("darkviolet"),
    @XmlEnumValue("deeppink")
    DEEPPINK("deeppink"),
    @XmlEnumValue("deepskyblue")
    DEEPSKYBLUE("deepskyblue"),
    @XmlEnumValue("dimgray")
    DIMGRAY("dimgray"),
    @XmlEnumValue("dimgrey")
    DIMGREY("dimgrey"),
    @XmlEnumValue("dodgerblue")
    DODGERBLUE("dodgerblue"),
    @XmlEnumValue("firebrick")
    FIREBRICK("firebrick"),
    @XmlEnumValue("floralwhite")
    FLORALWHITE("floralwhite"),
    @XmlEnumValue("forestgreen")
    FORESTGREEN("forestgreen"),
    @XmlEnumValue("fuchsia")
    FUCHSIA("fuchsia"),
    @XmlEnumValue("gainsboro")
    GAINSBORO("gainsboro"),
    @XmlEnumValue("lightpink")
    LIGHTPINK("lightpink"),
    @XmlEnumValue("lightsalmon")
    LIGHTSALMON("lightsalmon"),
    @XmlEnumValue("lightseagreen")
    LIGHTSEAGREEN("lightseagreen"),
    @XmlEnumValue("lightskyblue")
    LIGHTSKYBLUE("lightskyblue"),
    @XmlEnumValue("lightslategray")
    LIGHTSLATEGRAY("lightslategray"),
    @XmlEnumValue("lightslategrey")
    LIGHTSLATEGREY("lightslategrey"),
    @XmlEnumValue("lightsteelblue")
    LIGHTSTEELBLUE("lightsteelblue"),
    @XmlEnumValue("lightyellow")
    LIGHTYELLOW("lightyellow"),
    @XmlEnumValue("lime")
    LIME("lime"),
    @XmlEnumValue("limegreen")
    LIMEGREEN("limegreen"),
    @XmlEnumValue("linen")
    LINEN("linen"),
    @XmlEnumValue("magenta")
    MAGENTA("magenta"),
    @XmlEnumValue("maroon")
    MAROON("maroon"),
    @XmlEnumValue("mediumaquamarine")
    MEDIUMAQUAMARINE("mediumaquamarine"),
    @XmlEnumValue("mediumblue")
    MEDIUMBLUE("mediumblue"),
    @XmlEnumValue("mediumorchid")
    MEDIUMORCHID("mediumorchid"),
    @XmlEnumValue("mediumpurple")
    MEDIUMPURPLE("mediumpurple"),
    @XmlEnumValue("mediumseagreen")
    MEDIUMSEAGREEN("mediumseagreen"),
    @XmlEnumValue("mediumslateblue")
    MEDIUMSLATEBLUE("mediumslateblue"),
    @XmlEnumValue("mediumspringgreen")
    MEDIUMSPRINGGREEN("mediumspringgreen"),
    @XmlEnumValue("mediumturquoise")
    MEDIUMTURQUOISE("mediumturquoise"),
    @XmlEnumValue("mediumvioletred")
    MEDIUMVIOLETRED("mediumvioletred"),
    @XmlEnumValue("midnightblue")
    MIDNIGHTBLUE("midnightblue"),
    @XmlEnumValue("mintcream")
    MINTCREAM("mintcream"),
    @XmlEnumValue("mistyrose")
    MISTYROSE("mistyrose"),
    @XmlEnumValue("moccasin")
    MOCCASIN("moccasin"),
    @XmlEnumValue("navajowhite")
    NAVAJOWHITE("navajowhite"),
    @XmlEnumValue("navy")
    NAVY("navy"),
    @XmlEnumValue("oldlace")
    OLDLACE("oldlace"),
    @XmlEnumValue("olive")
    OLIVE("olive"),
    @XmlEnumValue("olivedrab")
    OLIVEDRAB("olivedrab"),
    @XmlEnumValue("orange")
    ORANGE("orange"),
    @XmlEnumValue("orangered")
    ORANGERED("orangered"),
    @XmlEnumValue("orchid")
    ORCHID("orchid"),
    @XmlEnumValue("palegoldenrod")
    PALEGOLDENROD("palegoldenrod"),
    @XmlEnumValue("palegreen")
    PALEGREEN("palegreen"),
    @XmlEnumValue("paleturquoise")
    PALETURQUOISE("paleturquoise"),
    @XmlEnumValue("palevioletred")
    PALEVIOLETRED("palevioletred"),
    @XmlEnumValue("papayawhip")
    PAPAYAWHIP("papayawhip"),
    @XmlEnumValue("peachpuff")
    PEACHPUFF("peachpuff"),
    @XmlEnumValue("peru")
    PERU("peru"),
    @XmlEnumValue("pink")
    PINK("pink"),
    @XmlEnumValue("plum")
    PLUM("plum"),
    @XmlEnumValue("powderblue")
    POWDERBLUE("powderblue"),
    @XmlEnumValue("purple")
    PURPLE("purple"),
    @XmlEnumValue("red")
    RED("red"),
    @XmlEnumValue("rosybrown")
    ROSYBROWN("rosybrown"),
    @XmlEnumValue("royalblue")
    ROYALBLUE("royalblue"),
    @XmlEnumValue("saddlebrown")
    SADDLEBROWN("saddlebrown"),
    @XmlEnumValue("salmon")
    SALMON("salmon"),
    @XmlEnumValue("ghostwhite")
    GHOSTWHITE("ghostwhite"),
    @XmlEnumValue("gold")
    GOLD("gold"),
    @XmlEnumValue("goldenrod")
    GOLDENROD("goldenrod"),
    @XmlEnumValue("gray")
    GRAY("gray"),
    @XmlEnumValue("grey")
    GREY("grey"),
    @XmlEnumValue("green")
    GREEN("green"),
    @XmlEnumValue("greenyellow")
    GREENYELLOW("greenyellow"),
    @XmlEnumValue("honeydew")
    HONEYDEW("honeydew"),
    @XmlEnumValue("hotpink")
    HOTPINK("hotpink"),
    @XmlEnumValue("indianred")
    INDIANRED("indianred"),
    @XmlEnumValue("indigo")
    INDIGO("indigo"),
    @XmlEnumValue("ivory")
    IVORY("ivory"),
    @XmlEnumValue("khaki")
    KHAKI("khaki"),
    @XmlEnumValue("lavender")
    LAVENDER("lavender"),
    @XmlEnumValue("lavenderblush")
    LAVENDERBLUSH("lavenderblush"),
    @XmlEnumValue("lawngreen")
    LAWNGREEN("lawngreen"),
    @XmlEnumValue("lemonchiffon")
    LEMONCHIFFON("lemonchiffon"),
    @XmlEnumValue("lightblue")
    LIGHTBLUE("lightblue"),
    @XmlEnumValue("lightcoral")
    LIGHTCORAL("lightcoral"),
    @XmlEnumValue("lightcyan")
    LIGHTCYAN("lightcyan"),
    @XmlEnumValue("lightgoldenrodyellow")
    LIGHTGOLDENRODYELLOW("lightgoldenrodyellow"),
    @XmlEnumValue("lightgray")
    LIGHTGRAY("lightgray"),
    @XmlEnumValue("lightgreen")
    LIGHTGREEN("lightgreen"),
    @XmlEnumValue("lightgrey")
    LIGHTGREY("lightgrey"),
    @XmlEnumValue("sandybrown")
    SANDYBROWN("sandybrown"),
    @XmlEnumValue("seagreen")
    SEAGREEN("seagreen"),
    @XmlEnumValue("seashell")
    SEASHELL("seashell"),
    @XmlEnumValue("sienna")
    SIENNA("sienna"),
    @XmlEnumValue("silver")
    SILVER("silver"),
    @XmlEnumValue("skyblue")
    SKYBLUE("skyblue"),
    @XmlEnumValue("slateblue")
    SLATEBLUE("slateblue"),
    @XmlEnumValue("slategray")
    SLATEGRAY("slategray"),
    @XmlEnumValue("slategrey")
    SLATEGREY("slategrey"),
    @XmlEnumValue("snow")
    SNOW("snow"),
    @XmlEnumValue("springgreen")
    SPRINGGREEN("springgreen"),
    @XmlEnumValue("steelblue")
    STEELBLUE("steelblue"),
    @XmlEnumValue("tan")
    TAN("tan"),
    @XmlEnumValue("teal")
    TEAL("teal"),
    @XmlEnumValue("thistle")
    THISTLE("thistle"),
    @XmlEnumValue("tomato")
    TOMATO("tomato"),
    @XmlEnumValue("turquoise")
    TURQUOISE("turquoise"),
    @XmlEnumValue("violet")
    VIOLET("violet"),
    @XmlEnumValue("wheat")
    WHEAT("wheat"),
    @XmlEnumValue("white")
    WHITE("white"),
    @XmlEnumValue("whitesmoke")
    WHITESMOKE("whitesmoke"),
    @XmlEnumValue("yellow")
    YELLOW("yellow"),
    @XmlEnumValue("yellowgreen")
    YELLOWGREEN("yellowgreen");
    private final String value;

    ColorNameType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ColorNameType fromValue(String v) {
        for (ColorNameType c: ColorNameType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
