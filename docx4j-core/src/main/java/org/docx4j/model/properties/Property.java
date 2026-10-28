/*
 *  Copyright 2009, Plutext Pty Ltd.
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
package org.docx4j.model.properties;

import org.docx4j.model.color.ResolvedColor;
import org.docx4j.model.color.ThemeColors;
import org.docx4j.model.color.WordColors;
import org.docx4j.wml.CTBorder;
import org.docx4j.wml.CTShd;
import org.docx4j.wml.STThemeColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;

/**
 * A representation of a run or paragraph property,
 * designed for ease of conversion to or from
 * CSS or to XSL FO.
 * 
 * Currently this class assumes a one to one 
 * correspondence between properties.  This might
 * well have to change.
 * 
 * Examples of issues:
 * 
 * - w:ind maps to multiple css properties (left, right)
 * 
 * @author jason
 *
 */
public abstract class Property {
	
	protected static Logger log = LoggerFactory.getLogger(Property.class);	
	
	/**
	 * CR-066 D5: a CSS length that converts to a value outside Word's range for the attribute it is written to is
	 * reported at WARN; the value is still written as computed (the caller gets a document back, as ImportXHTML's
	 * users expect), and Word reads it modulo the attribute's width (16 bits for a twips measure; CR-066 section 3).
	 * The range is the attribute's, not the type's. The line carries the ports' shared code
	 * {@link org.docx4j.wml.LenientInteger#CODE}.
	 *
	 * @since 17.3.5
	 */
	protected static void warnIfOutsideWordRange(String cssName, CSSValue value, long converted, String unit,
			long min, long max, String attribute) {
		if (converted < min || converted > max) {
			// the message promises nothing about what the caller writes: ImportXHTML clamps it (its CR-007), docx4j writes it as computed
			log.warn("{}: {}: {} converts to {} {}, outside Word's range {}..{} for {}; Word reads it modulo 16 bits if written as is (CR-066)",
					org.docx4j.wml.LenientInteger.CODE, cssName, value == null ? null : value.getCssText(), converted, unit, min, max, attribute);
		}
	}
	
	protected Object object;
	/**
	 * @return the object
	 */
	public Object getObject() {
		return object;
	}
	/**
	 * @param object the object to set
	 */
	public void setObject(Object object) {
		this.object = object;
	}
	
	
	
	// convert to HTML (CSS) ------------------------------
	
	public abstract String getCssProperty();

	/**
	 * @since 2.7.2
	 */
	public abstract String getCssName();
	
	public final static String CSS_NULL = ""; 

	public final static String CSS_COLON = ":"; 
	public final static String CSS_SPACE = " "; 
	public final static String CSS_COMMA = ";"; 
	/**
	 * A docx colour (w:val, w:fill, w:color) as a CSS/XSL-FO colour.  Word tolerates a
	 * value which already carries the "#", but "##0000FF" makes FOP throw, aborting the
	 * whole export, so the "#" is added only where it is missing.
	 *
	 * @since 17.0.5
	 */
	public final static String hexColour(String value) {
		if (value==null) return null;
		String v = value.trim();
		while (v.startsWith("#")) {
			v = v.substring(1);
		}
		return "#" + v;
	}

	/**
	 * A shading's w:fill as Word reads it (CR-060 section 13): a classic HTML colour name or any hex spelling Word
	 * accepts, as "#RRGGBB"; null for automatic, which is "auto" and any value Word rejects ("F00", "bogus"): no
	 * shading, and over a style's fill Word draws none (it saves w:fill="auto").
	 *
	 * @since 17.3.5
	 */
	public final static String fillColour(String value) {
		String hex = WordColors.normalise(value);
		return hex == null ? null : "#" + hex;
	}

	/**
	 * A border's w:color as Word reads it (CR-060 section 13): a colour name or hex spelling Word accepts, as
	 * "#RRGGBB"; automatic ("auto", and any value Word rejects, which over a style's colour Word draws as automatic
	 * and saves as "auto") as black.
	 *
	 * @since 17.3.5
	 */
	public final static String borderColour(String value) {
		String hex = WordColors.normalise(value);
		return hex == null ? "#000000" : "#" + hex;
	}

	private ThemeColors themeColors;

	/**
	 * The theme in which a WordprocessingML theme colour of this property is drawn; null, the
	 * default, writes the value written beside the theme reference, as before 17.3.3.
	 * {@link PropertyFactory} sets it from the package (CR-034).
	 *
	 * @since 17.3.3
	 */
	public ThemeColors getThemeColors() {
		return themeColors;
	}

	/** @since 17.3.3 */
	public void setThemeColors(ThemeColors themeColors) {
		this.themeColors = themeColors;
	}

	/**
	 * A colour with a theme reference ({@code w:themeColor}, {@code w:themeFill}) as Word draws it,
	 * "#RRGGBB", where that differs from the value written beside it: Word draws the theme
	 * reference and keeps the value as a cache, which can be stale (an older theme's) or absent
	 * ("auto") (CR-034 §11, P1).
	 *
	 * @return null where there is no theme reference or no {@link #getThemeColors()}, or the value
	 * written is the colour drawn, for the caller to write the value as it always has
	 * @since 17.3.3
	 */
	public String themeColour(String val, STThemeColor themeColor, String tint, String shade) {
		if (themeColors == null || themeColor == null) return null;
		String written = WordColors.normalise(val); // a name or a hex spelling Word accepts (CR-060)
		ResolvedColor drawn = WordColors.resolve(written, themeColor, tint, shade, themeColors);
		if (drawn == null || drawn.equals(ResolvedColor.ofHex(written))) return null;
		return hexColour(drawn.toHex());
	}

	/**
	 * A shading's fill ({@code w:fill}, {@code w:themeFill}) as {@link #themeColour} gives it.
	 *
	 * @since 17.3.3
	 */
	public String themedFill(CTShd shd) {
		return shd == null ? null
				: themeColour(shd.getFill(), shd.getThemeFill(), shd.getThemeFillTint(), shd.getThemeFillShade());
	}

	/**
	 * A border's colour ({@code w:color}, {@code w:themeColor}) as {@link #themeColour} gives it.
	 *
	 * @since 17.3.3
	 */
	public String themedColour(CTBorder border) {
		return border == null ? null
				: themeColour(border.getColor(), border.getThemeColor(), border.getThemeTint(), border.getThemeShade());
	}

	public final static String composeCss(String name, String value) {
		return name + CSS_COLON + CSS_SPACE + value + CSS_COMMA;
		
	}
	
	// convert to XSL FO ----------------------------------
	
	public abstract void setXslFO(Element foElement); 
	
	// convert from CSS  ----------------------------------
	
	public void debug(String propertyName, CSSValue value) {
		short valueType = value.getCssValueType();
		
		switch (valueType) {
		
			case CSSValue.CSS_CUSTOM:
				log.warn("valueType CUSTOM for " + propertyName + " - NOT IMPLEMENTED ");
				break;
			case CSSValue.CSS_INHERIT:
				log.warn("valueType INHERIT for " + propertyName + " - NOT IMPLEMENTED ");
				break;
			case CSSValue.CSS_PRIMITIVE_VALUE:
				log.debug("valueType PRIMITIVE for " + propertyName );
				CSSPrimitiveValue cssPrimitiveValue = (CSSPrimitiveValue)value;
				log.debug("PrimitiveType: " + cssPrimitiveValue.getPrimitiveType() );
				break;
			case CSSValue.CSS_VALUE_LIST:
				log.debug("valueType LIST for " + propertyName );
				CSSValueList cssValueList = (CSSValueList)value;
				break;

			default:
				log.error("Unexpected valueType " + valueType + " for " + propertyName );
				return;
				
		}
		
	}

}
