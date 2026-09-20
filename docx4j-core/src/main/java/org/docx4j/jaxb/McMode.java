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
 * layout or parity measurement, anything that counts or draws: it sees the one
 * branch docx4j draws ({@link McSelection#selectedBranch}), so a text box's
 * paragraphs are visited once, as Word lays them out once.</p>
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

	/** One branch: the one {@link McSelection} selects. */
	READ,

	/** Every {@code mc:Choice}, then the {@code mc:Fallback}: the walk of docx4j before 17.2.0. */
	ALL
}
