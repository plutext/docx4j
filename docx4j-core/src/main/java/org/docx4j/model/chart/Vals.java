/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.docx4j.model.chart;

import java.lang.reflect.Method;

import org.docx4j.dml.chart.CTBoolean;
import org.docx4j.dml.chart.CTDouble;
import org.docx4j.dml.chart.CTUnsignedInt;

/**
 * The value of a chart element of the {@code <c:x val="..."/>} kind, absent as null.  Many such
 * elements have a type of their own, each with a {@code getVal()} returning an enumeration with
 * {@code value()} or a number, so this reads them alike.
 */
final class Vals {

	private Vals() {
	}

	static Boolean bool(CTBoolean b) {
		return b == null ? null : b.isVal();
	}

	static Long unsigned(CTUnsignedInt u) {
		return u == null ? null : u.getVal();
	}

	static Double dbl(CTDouble d) {
		return d == null ? null : d.getVal();
	}

	/** An enumeration's value as the file writes it ("autoZero", "outEnd"), or a number's text */
	static String str(Object ct) {
		if (ct == null) return null;
		try {
			Object v = ct.getClass().getMethod("getVal").invoke(ct);
			if (v == null) return null;
			try {
				Method value = v.getClass().getMethod("value");
				return String.valueOf(value.invoke(v));
			} catch (NoSuchMethodException e) {
				return String.valueOf(v);
			}
		} catch (ReflectiveOperationException e) {
			return null;
		}
	}

	/** A numeric value element's number (CTGapAmount, CTOverlap, CTAxisUnit ...) */
	static Double num(Object ct) {
		String s = str(ct);
		if (s == null) return null;
		try {
			return Double.valueOf(s);
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
