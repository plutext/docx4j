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

import org.docx4j.mce.AlternateContent;

/**
 * An {@code mc:AlternateContent} a splice took out of a tree ({@link McSplice}; on load, CR-051
 * section 3.2 in {@link McBlockLevel#WORD} mode), kept so that nothing is lost to inspection: the
 * element with every branch, which branch's content was promoted in its place, and where it
 * stood.  Where it stood is an address worked out when it was taken out, not a live reference
 * (CR-051 section 10.4 b), so the record outlives a replaced tree or a re-unmarshal.
 *
 * <p>The promoted branch's objects are the ones now in the tree: the branch's list still names
 * them, and their parent is the container they were moved to.  The other branches' objects are
 * reachable through the element alone.</p>
 *
 * @since 17.3.4 (CR-051 phase 1)
 */
public final class ResolvedAlternateContent {

	private final AlternateContent element;
	private final Object branch;
	private final String containerName;
	private final String address;
	private final int index;
	private final int promoted;

	ResolvedAlternateContent(AlternateContent element, Object branch, String containerName, String address,
			int index, int promoted) {
		this.element = element;
		this.branch = branch;
		this.containerName = containerName;
		this.address = address;
		this.index = index;
		this.promoted = promoted;
	}

	/** The element taken out, whole: every Choice and the Fallback. */
	public AlternateContent getElement() {
		return element;
	}

	/**
	 * The {@link AlternateContent.Choice} or {@link AlternateContent.Fallback} whose content
	 * replaced the element; null where the element had no branch at all and was removed.
	 */
	public Object getBranch() {
		return branch;
	}

	/** The container's element name ({@code w:body}, {@code w:tc}, {@code w:sdtContent}, {@code w:tbl}, ...). */
	public String getContainerName() {
		return containerName;
	}

	/**
	 * The container's address from the part's root when the element was taken out: each step an
	 * element name with, where its parent holds a content list, its index in that list, as
	 * {@code w:document/w:body[0]/w:tbl[3]/w:tr[0]/w:tc[1]}.
	 */
	public String getAddress() {
		return address;
	}

	/** The index in the container's content list at which the element stood, and the promoted content now starts. */
	public int getIndex() {
		return index;
	}

	/** How many objects the promoted branch put in the element's place (0 where it was removed). */
	public int getPromotedCount() {
		return promoted;
	}

	@Override
	public String toString() {
		String which = branch instanceof AlternateContent.Choice
				? "Choice Requires=\"" + ((AlternateContent.Choice) branch).getRequires() + "\""
				: branch == null ? "none (removed)" : "Fallback";
		return "mc:AlternateContent at " + address + " index " + index + ": " + which + ", " + promoted + " promoted";
	}
}
