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

/**
 * Where a chart's legend is ({@code c:legendPos}): Excel's bottom is the default
 *
 * @since 17.3.3 (CR-036)
 */
public enum LegendPosition {

	BOTTOM("b"), TOP("t"), LEFT("l"), RIGHT("r"), TOP_RIGHT("tr");

	private final String val;

	LegendPosition(String val) {
		this.val = val;
	}

	/** Its {@code c:legendPos/@val} */
	public String getVal() {
		return val;
	}
}
