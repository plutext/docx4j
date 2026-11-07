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

import jakarta.xml.bind.JAXBException;

/**
 * A part whose root isn't one it can hold, named, so that the read fails once, as CR-065 D1 has it, and the preprocessor
 * isn't tried on what it can't mend (CR-079).
 *
 * @since 17.3.6
 */
@SuppressWarnings("serial")
public class UnexpectedRootException extends JAXBException {

	private final transient Object held;

	public UnexpectedRootException(String message) {
		this(message, null);
	}

	/** @param held what stood in a branch at the root, as the branch holds it, in place of the part's root; null where the part's own root is meant */
	public UnexpectedRootException(String message, Object held) {
		super(message);
		this.held = held;
	}

	/** What stood in a branch at the root: a bound object, a JAXBElement, or for a name nothing binds a DOM element; null for the part's own root. */
	public Object getHeld() {
		return held;
	}
}
