/*
 *  Copyright 2026, Plutext Pty Ltd.
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
import java.util.function.BooleanSupplier;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * What docx4j writes plainly on save (CR-061): every colour in the schema's form, holding Word's reading (CR-060
 * sections 12 and 13). A value Word accepts is written as upper-case RRGGBB and {@code auto} as {@code auto}; a value
 * Word rejects is left out where Word reads it as absent (a text or underline colour) and written {@code auto} where
 * Word reads it as automatic (a fill, a border colour). A revision's former run properties are written the same way,
 * with {@link WordRepairs}' drop of a highlight, vertAlign or em Word repairs away.
 *
 * <p>Marshal-only: an adapter is the identity on unmarshal, so the object in memory keeps what it read, and only what
 * is written changes. On by default: {@code docx4j.save.normalise=false} in docx4j.properties turns it off (docx4j-core
 * supplies the default through {@link #setDefault}), and {@code OpcPackage.setNormaliseOnSave} turns it on or off for
 * one package, by giving the save's marshaller configured adapters ({@link #configure}). Off, colours are written as
 * read and WordRepairs' drop is off too (CR-061 D2).</p>
 *
 * @since 17.3.5
 */
public final class NormaliseOnSave {

	private NormaliseOnSave() {
	}

	private static final Logger log = LoggerFactory.getLogger(NormaliseOnSave.class);

	private static volatile BooleanSupplier byDefault = () -> true;

	/** Where the default comes from: docx4j-core sets it to read {@code docx4j.save.normalise} */
	public static void setDefault(BooleanSupplier supplier) {
		byDefault = supplier == null ? () -> true : supplier;
	}

	/** Whether a save normalises, absent a package's own setting */
	public static boolean isOnByDefault() {
		return byDefault.getAsBoolean();
	}

	/** An adapter that does its work only while normalising is on: its own setting, else the default */
	public abstract static class Switched<V, B> extends XmlAdapter<V, B> {
		private final Boolean on;

		protected Switched() {
			this.on = null;
		}

		protected Switched(boolean on) {
			this.on = on;
		}

		public boolean isOn() {
			return on != null ? on : isOnByDefault();
		}

		/** The same adapter, its setting fixed */
		protected abstract Switched<V, B> fixed(boolean on);
	}

	/**
	 * Gives a marshaller adapters fixed on or off (a package's {@code setNormaliseOnSave}), in place of the ones JAXB
	 * makes, which follow the default.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void configure(Marshaller m, boolean on) {
		for (Switched<?, ?> a : new Switched<?, ?>[] { new ColourValue(), new UnderlineColour(), new FillOrBorderColour(),
				new ColourElement(), new WordRepairs.HighlightOnSave(), new WordRepairs.VertAlignOnSave(),
				new WordRepairs.EmOnSave() }) {
			m.setAdapter((Class) a.getClass(), a.fixed(on));
		}
	}

	/** Whether the marshal this marshaller is doing normalises */
	static boolean isOn(Marshaller m) {
		XmlAdapter<?, ?> a = m == null ? null : m.getAdapter(ColourElement.class);
		return a instanceof Switched ? ((Switched<?, ?>) a).isOn() : isOnByDefault();
	}

	private static String plain(String attribute, String val, String rejected) {
		if (val == null || "auto".equals(val)) return val;
		String hex = WordColorValues.normalise(val);
		String written = hex == null ? rejected : hex;
		if (!val.equals(written) && log.isDebugEnabled()) {
			log.debug("CR-061: " + attribute + " \"" + val + "\" written " + (written == null ? "nothing" : "\"" + written + "\""));
		}
		return written;
	}

	/**
	 * {@code w:color/@w:val}. A rejected value is written {@code auto}; that is reached only where the element is
	 * kept, beside a theme colour (the value is required, and the theme colour is what Word draws): elsewhere
	 * {@link ColourElement} leaves the element out.
	 */
	public static class ColourValue extends Switched<String, String> {
		public ColourValue() {
		}

		public ColourValue(boolean on) {
			super(on);
		}

		@Override
		protected ColourValue fixed(boolean on) {
			return new ColourValue(on);
		}

		@Override
		public String unmarshal(String v) {
			return v;
		}

		@Override
		public String marshal(String v) {
			return isOn() ? plain("w:color/@w:val", v, "auto") : v;
		}
	}

	/** {@code w:u/@w:color}: a rejected value is left out (Word reads it as absent) */
	public static class UnderlineColour extends Switched<String, String> {
		public UnderlineColour() {
		}

		public UnderlineColour(boolean on) {
			super(on);
		}

		@Override
		protected UnderlineColour fixed(boolean on) {
			return new UnderlineColour(on);
		}

		@Override
		public String unmarshal(String v) {
			return v;
		}

		@Override
		public String marshal(String v) {
			return isOn() ? plain("w:u/@w:color", v, null) : v;
		}
	}

	/**
	 * {@code w:shd/@w:fill} and {@code @w:color}, a border's {@code w:color}, {@code w:background/@w:color}: a
	 * rejected value is written {@code auto} (Word reads it as automatic, over a style's colour too)
	 */
	public static class FillOrBorderColour extends Switched<String, String> {
		public FillOrBorderColour() {
		}

		public FillOrBorderColour(boolean on) {
			super(on);
		}

		@Override
		protected FillOrBorderColour fixed(boolean on) {
			return new FillOrBorderColour(on);
		}

		@Override
		public String unmarshal(String v) {
			return v;
		}

		@Override
		public String marshal(String v) {
			return isOn() ? plain("fill or border colour", v, "auto") : v;
		}
	}

	/**
	 * A {@code w:color} in run properties ({@link RPr}, {@link ParaRPr}; placed by ModifyGeneratedSources): left out
	 * where Word reads it as absent, its value rejected and no theme colour beside it
	 */
	public static class ColourElement extends Switched<Color, Color> {
		public ColourElement() {
		}

		public ColourElement(boolean on) {
			super(on);
		}

		@Override
		protected ColourElement fixed(boolean on) {
			return new ColourElement(on);
		}

		@Override
		public Color unmarshal(Color v) {
			return v;
		}

		@Override
		public Color marshal(Color v) {
			return isOn() && absent(v) ? null : v;
		}
	}

	/** A w:color Word reads as absent: its value rejected, and no theme attribute beside it */
	static boolean absent(Color c) {
		boolean a = c != null && !WordColorValues.stated(c.getVal()) && c.getThemeColor() == null
				&& c.getThemeTint() == null && c.getThemeShade() == null;
		if (a && log.isDebugEnabled()) log.debug("CR-061: w:color \"" + c.getVal() + "\" left out");
		return a;
	}

	/**
	 * A revision's former run properties as written ({@code CTRPrChange.RPr}, {@code CTParaRPrOriginal}: a list,
	 * swapped in for the marshal by their beforeMarshal, placed by ModifyGeneratedSources): without a w:color Word reads
	 * as absent, or a highlight, vertAlign or em Word repairs away. The list itself where there is nothing to leave out,
	 * or normalising is off.
	 */
	public static List<Object> filterOnSave(List<Object> list, Marshaller m) {
		if (list == null || list.isEmpty() || !isOn(m)) return list;
		List<Object> out = null;
		for (int i = 0; i < list.size(); i++) {
			Object o = list.get(i);
			Object v = o instanceof JAXBElement ? ((JAXBElement<?>) o).getValue() : o;
			boolean drop = (v instanceof Color && absent((Color) v))
					|| (v instanceof Highlight && !WordRepairs.isHighlightColor(((Highlight) v).getVal()))
					|| (v instanceof CTVerticalAlignRun && ((CTVerticalAlignRun) v).getVal() == null)
					|| (v instanceof CTEm && ((CTEm) v).getVal() == null);
			if (drop && out == null) out = new ArrayList<>(list.subList(0, i));
			if (!drop && out != null) out.add(o);
		}
		return out == null ? list : out;
	}
}
