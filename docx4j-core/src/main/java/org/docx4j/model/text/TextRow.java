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
package org.docx4j.model.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A table row of the block model: its cells, and whether it is a header row ({@code w:tblHeader}).
 *
 * @since 17.3.4 (CR-046 phase 1)
 */
public final class TextRow {

	final String address;
	final List<TextCell> cells = new ArrayList<TextCell>();
	boolean header;

	TextRow(String address) {
		this.address = address;
	}

	public String getAddress() { return address; }
	public List<TextCell> getCells() { return Collections.unmodifiableList(cells); }
	/** True for a row marked {@code w:tblHeader}. */
	public boolean isHeader() { return header; }
}
