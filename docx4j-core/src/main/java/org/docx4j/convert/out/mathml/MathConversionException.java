/*
   Licensed to Plutext Pty Ltd under one or more contributor license agreements.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.

   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.docx4j.convert.out.mathml;

/**
 * Thrown when an OMML (or MathML) construct is outside the supported subset.
 * The caller is expected to catch this and fall back (eg emit the equation's
 * text), rather than let one exotic equation fail the whole document
 * conversion. See docs/developer/change-requests/CR-007-math-omml-mathml.md.
 *
 * @since 17.0.4
 */
public class MathConversionException extends Exception {

	private static final long serialVersionUID = 1L;

	public MathConversionException(String message) {
		super(message);
	}

	public MathConversionException(String message, Throwable cause) {
		super(message, cause);
	}
}
