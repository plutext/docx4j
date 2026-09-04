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
package org.docx4j.fop.wordlayout;

import org.apache.fop.layoutmgr.KnuthGlue;

/**
 * The extra leading below a line, as a Knuth glue: counted between lines,
 * discarded when a page break is taken just before it (FOP, like Knuth,
 * treats glue after a taken break as belonging to the page before it).
 * That is Word's rule: the last line of a page keeps only its text box
 * within the margins, and a page's first line starts with its ascent.
 * {@link WordFlowLayoutManager} moves the glue of a paragraph's last line
 * behind the break possibility that follows the paragraph.
 *
 * @since 17.0.5
 */
public class LeadingGlue extends KnuthGlue {

	public LeadingGlue(int width) {
		super(width, 0, 0, null, true);
	}
}
