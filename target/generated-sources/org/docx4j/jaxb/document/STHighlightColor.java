
package org.docx4j.jaxb.document;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_HighlightColor.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_HighlightColor">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="black"/>
 *     &lt;enumeration value="blue"/>
 *     &lt;enumeration value="cyan"/>
 *     &lt;enumeration value="green"/>
 *     &lt;enumeration value="magenta"/>
 *     &lt;enumeration value="red"/>
 *     &lt;enumeration value="yellow"/>
 *     &lt;enumeration value="white"/>
 *     &lt;enumeration value="darkBlue"/>
 *     &lt;enumeration value="darkCyan"/>
 *     &lt;enumeration value="darkGreen"/>
 *     &lt;enumeration value="darkMagenta"/>
 *     &lt;enumeration value="darkRed"/>
 *     &lt;enumeration value="darkYellow"/>
 *     &lt;enumeration value="darkGray"/>
 *     &lt;enumeration value="lightGray"/>
 *     &lt;enumeration value="none"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_HighlightColor")
@XmlEnum
public enum STHighlightColor {


    /**
     * Black Highlighting
     * 						Color
     * 
     */
    @XmlEnumValue("black")
    BLACK("black"),

    /**
     * Blue Highlighting Color
     * 
     */
    @XmlEnumValue("blue")
    BLUE("blue"),

    /**
     * Cyan Highlighting Color
     * 
     */
    @XmlEnumValue("cyan")
    CYAN("cyan"),

    /**
     * Green Highlighting
     * 						Color
     * 
     */
    @XmlEnumValue("green")
    GREEN("green"),

    /**
     * Magenta Highlighting
     * 						Color
     * 
     */
    @XmlEnumValue("magenta")
    MAGENTA("magenta"),

    /**
     * Red Highlighting Color
     * 
     */
    @XmlEnumValue("red")
    RED("red"),

    /**
     * Yellow Highlighting
     * 						Color
     * 
     */
    @XmlEnumValue("yellow")
    YELLOW("yellow"),

    /**
     * White Highlighting
     * 						Color
     * 
     */
    @XmlEnumValue("white")
    WHITE("white"),

    /**
     * Dark Blue Highlighting
     * 						Color
     * 
     */
    @XmlEnumValue("darkBlue")
    DARK_BLUE("darkBlue"),

    /**
     * Dark Cyan Highlighting
     * 						Color
     * 
     */
    @XmlEnumValue("darkCyan")
    DARK_CYAN("darkCyan"),

    /**
     * Dark Green Highlighting
     * 						Color
     * 
     */
    @XmlEnumValue("darkGreen")
    DARK_GREEN("darkGreen"),

    /**
     * Dark Magenta Highlighting
     * 						Color
     * 
     */
    @XmlEnumValue("darkMagenta")
    DARK_MAGENTA("darkMagenta"),

    /**
     * Dark Red Highlighting
     * 						Color
     * 
     */
    @XmlEnumValue("darkRed")
    DARK_RED("darkRed"),

    /**
     * Dark Yellow Highlighting
     * 						Color
     * 
     */
    @XmlEnumValue("darkYellow")
    DARK_YELLOW("darkYellow"),

    /**
     * Dark Gray Highlighting
     * 						Color
     * 
     */
    @XmlEnumValue("darkGray")
    DARK_GRAY("darkGray"),

    /**
     * Light Gray Highlighting
     * 						Color
     * 
     */
    @XmlEnumValue("lightGray")
    LIGHT_GRAY("lightGray"),

    /**
     * No Text Highlighting
     * 
     */
    @XmlEnumValue("none")
    NONE("none");
    private final String value;

    STHighlightColor(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STHighlightColor fromValue(String v) {
        for (STHighlightColor c: STHighlightColor.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
