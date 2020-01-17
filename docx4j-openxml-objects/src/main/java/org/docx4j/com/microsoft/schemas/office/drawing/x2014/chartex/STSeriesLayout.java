
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_SeriesLayout.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_SeriesLayout"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="boxWhisker"/&gt;
 *     &lt;enumeration value="clusteredColumn"/&gt;
 *     &lt;enumeration value="funnel"/&gt;
 *     &lt;enumeration value="paretoLine"/&gt;
 *     &lt;enumeration value="regionMap"/&gt;
 *     &lt;enumeration value="sunburst"/&gt;
 *     &lt;enumeration value="treemap"/&gt;
 *     &lt;enumeration value="waterfall"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_SeriesLayout")
@XmlEnum
public enum STSeriesLayout {

    @XmlEnumValue("boxWhisker")
    BOX_WHISKER("boxWhisker"),
    @XmlEnumValue("clusteredColumn")
    CLUSTERED_COLUMN("clusteredColumn"),
    @XmlEnumValue("funnel")
    FUNNEL("funnel"),
    @XmlEnumValue("paretoLine")
    PARETO_LINE("paretoLine"),
    @XmlEnumValue("regionMap")
    REGION_MAP("regionMap"),
    @XmlEnumValue("sunburst")
    SUNBURST("sunburst"),
    @XmlEnumValue("treemap")
    TREEMAP("treemap"),
    @XmlEnumValue("waterfall")
    WATERFALL("waterfall");
    private final String value;

    STSeriesLayout(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STSeriesLayout fromValue(String v) {
        for (STSeriesLayout c: STSeriesLayout.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
