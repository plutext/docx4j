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
package org.docx4j.jaxb;

/**
 * How a tree walk treats {@code mc:AlternateContent} (ECMA-376 Part 3): one
 * branch, or every branch.
 *
 * <p>{@link #READ} is for a reader - text extraction, numbering emulation, a
 * search, anything that counts: it sees one branch, so a text box's paragraphs are
 * visited once, as Word lays them out once.  Since 17.3.4 that branch is the one
 * docx4j reads for text ({@link McPurpose#TEXT}: the first Choice whose
 * {@code Requires} namespaces are all in the published understood set, else the
 * Fallback; CR-046 phase 3); before, the one it draws.</p>
 *
 * <p>{@link #DRAW} is for a walk that serves an output's layout: it sees the branch
 * docx4j draws ({@link McSelection#selectedBranch}, {@code docx4j.jaxb.mc.preferChoice},
 * the Fallback by default), which is not always one docx4j can read: the FO exporter
 * does not yet draw an inline {@code wps} text box (CR-021 section 8.6).</p>
 *
 * <p>{@link #ALL} is for a mutator - data binding, field update, mail merge,
 * anonymisation, find and replace, anything that edits: it sees every
 * {@code mc:Choice} and the {@code mc:Fallback}, so the edit lands in each
 * branch and the saved document says the same thing to every reader whichever
 * branch that reader understands.</p>
 *
 * <p>The default of {@link org.docx4j.TraversalUtil} is READ since 17.2.0
 * (before it, every walk saw every branch: CR-021).</p>
 *
 * @since 17.2.0 (CR-021 phase 1)
 */
public enum McMode {

	/**
	 * One branch: the one read for text ({@link McSelection#selectedBranch(McPurpose, org.docx4j.mce.AlternateContent)}
	 * for {@link McPurpose#TEXT}); before 17.3.4, the one drawn ({@link #DRAW}).
	 */
	READ,

	/** Every {@code mc:Choice}, then the {@code mc:Fallback}: the walk of docx4j before 17.2.0. */
	ALL,

	/**
	 * One branch: the one drawn ({@link McSelection#selectedBranch(org.docx4j.mce.AlternateContent)},
	 * {@link McPurpose#DRAW}), for a walk that serves an output's layout.
	 *
	 * @since 17.3.4 (CR-046 phase 3)
	 */
	DRAW
}
