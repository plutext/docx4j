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
package org.docx4j.wml;

/**
 * {@code w:framePr/@w:anchorLock} as the docx states it: present and true, present and false, or absent.
 *
 * <p>The generated {@code isAnchorLock()} returns a primitive {@code boolean}, false when the attribute is
 * absent (the schema's {@code default="false"} since 17.3.5, CR-059; {@code true} before). Style resolution needs
 * the difference, as for {@link AutospacingAccess}: a {@code w:framePr} that does not mention
 * {@code anchorLock} leaves the style's value alone, and one with nothing else set is empty. The field is
 * protected, so only a class in this package can read it.</p>
 *
 * @since 17.3.5
 */
public class FramePrAccess {

	private FramePrAccess() {}

	/** {@code w:anchorLock} as stated, or null where the attribute is absent. */
	public static Boolean getAnchorLock(CTFramePr framePr) {
		return framePr == null ? null : framePr.anchorLock;
	}
}
