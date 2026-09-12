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
package org.docx4j.model.listnumbering;

/**
 * {@code w:numFmt hex}: 1, 2, ... 9, A, B, ... F, 10, 11.
 *
 * @since 17.1.1
 */
public class NumberFormatHex extends LabelFormatter {

	@Override
	public String format(int in) {
		return Integer.toHexString(in).toUpperCase();
	}
}
