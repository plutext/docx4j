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
package org.docx4j.model.datastorage;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.docx4j.Docx4jProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Applies the docx4j.properties keys
 * <pre>
 *   docx4j.model.datastorage.BindingTraverser.XHTML.RunFormatting
 *   docx4j.model.datastorage.BindingTraverser.XHTML.ParagraphFormatting
 *   docx4j.model.datastorage.BindingTraverser.XHTML.TableFormatting
 * </pre>
 * (each a name of docx4j-ImportXHTML's <tt>FormattingOption</tt>: CLASS_TO_STYLE_ONLY,
 * CLASS_PLUS_OTHER or IGNORE_CLASS) to an <tt>XHTMLImporterImpl</tt>, by reflection,
 * since docx4j-core has no compile-time dependency on docx4j-ImportXHTML.
 *
 * An unset or blank key leaves the importer's own default in place; an unknown value,
 * or a setter the importer lacks, is logged and skipped.
 *
 * @see BindingHandler#setXHTMLImporterCustomizer(XHTMLImporterCustomizer) for anything
 *      beyond these three settings
 * @since 17.1.1 (CR-013)
 */
public final class XHTMLImporterFormatting {

	private static final Logger log = LoggerFactory.getLogger(XHTMLImporterFormatting.class);

	public static final String PROPERTY_PREFIX = "docx4j.model.datastorage.BindingTraverser.XHTML.";
	public static final String RUN = PROPERTY_PREFIX + "RunFormatting";
	public static final String PARAGRAPH = PROPERTY_PREFIX + "ParagraphFormatting";
	public static final String TABLE = PROPERTY_PREFIX + "TableFormatting";

	static final String FORMATTING_OPTION_CLASS = "org.docx4j.convert.in.xhtml.FormattingOption";
	public static final String CLASS_TO_STYLE_ONLY = "CLASS_TO_STYLE_ONLY";

	private static final String[][] KEYS_TO_SETTERS = {
			{ RUN, "setRunFormatting" },
			{ PARAGRAPH, "setParagraphFormatting" },
			{ TABLE, "setTableFormatting" } };

	private XHTMLImporterFormatting() {}

	/**
	 * @return the configured value of the key, trimmed; null if unset or blank
	 */
	public static String configured(String key) {
		String value = Docx4jProperties.getProperty(key);
		if (value == null) return null;
		value = value.trim();
		return value.isEmpty() ? null : value;
	}

	/**
	 * @return whether the key is set to CLASS_TO_STYLE_ONLY, in which case the importer
	 *         will ignore any @style docx4j might generate for that level
	 */
	public static boolean isClassToStyleOnly(String key) {
		return CLASS_TO_STYLE_ONLY.equals(configured(key));
	}

	/**
	 * Invoke the importer's setRunFormatting / setParagraphFormatting / setTableFormatting
	 * for each key that is set.
	 *
	 * @param importer      an org.docx4j.convert.in.xhtml.XHTMLImporterImpl (as Object)
	 * @param importerClass its class (the caller already has it from Class.forName)
	 * @return the number of setters invoked
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static int apply(Object importer, Class<?> importerClass) {

		boolean anyConfigured = false;
		for (String[] keyToSetter : KEYS_TO_SETTERS) {
			if (configured(keyToSetter[0]) != null) anyConfigured = true;
		}
		if (!anyConfigured) return 0;

		Class<?> enumClass;
		try {
			enumClass = Class.forName(FORMATTING_OPTION_CLASS, true, importerClass.getClassLoader());
		} catch (ClassNotFoundException e) {
			log.warn(FORMATTING_OPTION_CLASS + " not found alongside " + importerClass.getName()
					+ "; " + PROPERTY_PREFIX + "*Formatting settings ignored");
			return 0;
		}

		int applied = 0;
		for (String[] keyToSetter : KEYS_TO_SETTERS) {
			String key = keyToSetter[0];
			String setter = keyToSetter[1];
			String value = configured(key);
			if (value == null) continue;

			Object option;
			try {
				option = Enum.valueOf((Class) enumClass, value);
			} catch (IllegalArgumentException e) {
				log.warn("Ignoring " + key + "=" + value + ": not one of "
						+ Arrays.toString(enumClass.getEnumConstants()));
				continue;
			}
			try {
				Method m = importerClass.getMethod(setter, enumClass);
				m.invoke(importer, option);
				applied++;
				log.debug(setter + "(" + value + ")");
			} catch (NoSuchMethodException e) {
				log.warn(importerClass.getName() + " has no " + setter + "(" + enumClass.getSimpleName()
						+ "); " + key + " ignored");
			} catch (Exception e) {
				log.error("Couldn't invoke " + setter + "(" + value + "): " + e.getMessage(), e);
			}
		}
		return applied;
	}
}
