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
 * A table cell of the block model: its blocks (paragraphs, and nested tables), how many grid columns
 * it spans, and whether it is in a vertical merge.
 *
 * @since 17.3.4 (CR-046 phase 1)
 */
public final class TextCell {

	final String address;
	final List<TextBlock> blocks = new ArrayList<TextBlock>();
	int gridSpan = 1;
	boolean mergeContinue;
	boolean mergeRestart;

	TextCell(String address) {
		this.address = address;
	}

	public String getAddress() { return address; }
	public List<TextBlock> getBlocks() { return Collections.unmodifiableList(blocks); }
	/** {@code w:gridSpan}, 1 by default. */
	public int getGridSpan() { return gridSpan; }
	/** True for a cell continuing a vertical merge ({@code w:vMerge} without {@code restart}). */
	public boolean isMergeContinue() { return mergeContinue; }
	/** True for a cell starting a vertical merge ({@code w:vMerge w:val="restart"}). */
	public boolean isMergeRestart() { return mergeRestart; }

	/** The cell's paragraphs' texts, nested tables' included, in document order. */
	void paragraphs(List<String> out) {
		for (TextBlock b : blocks) {
			if (b.kind == TextBlock.Kind.TABLE) {
				for (TextRow r : b.rows) for (TextCell c : r.cells) c.paragraphs(out);
			} else {
				out.add(b.getText());
			}
		}
	}
}
