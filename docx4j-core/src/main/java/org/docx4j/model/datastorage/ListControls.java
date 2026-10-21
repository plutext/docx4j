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

import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.wml.CTSdtComboBox;
import org.docx4j.wml.CTSdtDropDownList;
import org.docx4j.wml.CTSdtListItem;
import org.docx4j.wml.SdtPr;

/**
 * Binding a drop-down list or combo box, as Word does it (measured in Word 365, 2026-10-22, on
 * docx4j-portfolio's bind-typed probe): the control shows the list item whose {@code w:value} is
 * the bound value, by its {@code w:displayText}, and Word writes the bound value into the list's
 * {@code w:lastValue}. A value in no item is shown as it is (measured for a combo box, whose text
 * is free; read the same way for a drop-down, unmeasured). Until 17.3.4 docx4j showed the value
 * itself in both controls, as LibreOffice and ONLYOFFICE do (docx4j-portfolio's finding).
 *
 * @since 17.3.4
 */
public final class ListControls {

	private ListControls() {}

	/** The control's drop-down list or combo box, or null for any other control. */
	public static Object listOf(SdtPr sdtPr) {
		if (sdtPr == null) return null;
		for (Object o : sdtPr.getRPrOrAliasOrLock()) {
			Object v = XmlUtils.unwrap(o);
			if (v instanceof CTSdtDropDownList || v instanceof CTSdtComboBox) return v;
		}
		return null;
	}

	private static List<CTSdtListItem> items(Object list) {
		if (list instanceof CTSdtDropDownList) return ((CTSdtDropDownList) list).getListItem();
		if (list instanceof CTSdtComboBox) return ((CTSdtComboBox) list).getListItem();
		return null;
	}

	/**
	 * What the control shows for a bound value: the matching item's display text; the value itself
	 * when no item has it, or the control is not a list, or the value is empty.
	 */
	public static String displayTextFor(SdtPr sdtPr, String value) {
		List<CTSdtListItem> items = items(listOf(sdtPr));
		if (items == null || value == null || value.isEmpty()) return value;
		for (CTSdtListItem item : items) {
			if (value.equals(item.getValue())) {
				return item.getDisplayText() == null ? value : item.getDisplayText();
			}
		}
		return value;
	}

	/**
	 * The bound value a shown text stands for, for {@code w:lastValue}: the value of the item whose
	 * display text it is (the first such item), else the text itself.
	 */
	public static String lastValueFor(Object list, String shownText) {
		List<CTSdtListItem> items = items(list);
		if (items == null || shownText == null) return shownText;
		for (CTSdtListItem item : items) {
			if (shownText.equals(item.getDisplayText()) && item.getValue() != null) return item.getValue();
		}
		return shownText;
	}

	/** Writes {@code w:lastValue} on the control's list, as Word's save does; nothing for any other control. */
	public static void setLastValue(SdtPr sdtPr, String shownText) {
		Object list = listOf(sdtPr);
		if (list == null || shownText == null || shownText.isEmpty()) return;
		String value = lastValueFor(list, shownText);
		if (list instanceof CTSdtDropDownList) ((CTSdtDropDownList) list).setLastValue(value);
		else ((CTSdtComboBox) list).setLastValue(value);
	}
}
