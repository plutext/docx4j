/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.

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
package org.docx4j.wml;

/**
 * {@code w:beforeAutospacing} and {@code w:afterAutospacing} as the docx states them:
 * present and true, present and false, or absent.
 *
 * <p>XJC generates {@code isBeforeAutospacing()} returning a primitive {@code boolean},
 * which reports an absent attribute and an explicit {@code w:beforeAutospacing="0"} alike
 * as false.  Style resolution needs the difference: direct formatting saying
 * {@code w:beforeAutospacing="0"} must switch a style's {@code "1"} off, while direct
 * formatting that simply does not mention it must leave the style's value alone.  The
 * fields are protected, so only a class in this package can tell one from the other -
 * hence this one, rather than reflection on generated code.</p>
 *
 * @since 17.0.6
 */
public class AutospacingAccess {

	private AutospacingAccess() {}

	/** {@code w:beforeAutospacing} as stated, or null where the attribute is absent. */
	public static Boolean getBeforeAutospacing(PPrBase.Spacing spacing) {
		return spacing == null ? null : spacing.beforeAutospacing;
	}

	/** {@code w:afterAutospacing} as stated, or null where the attribute is absent. */
	public static Boolean getAfterAutospacing(PPrBase.Spacing spacing) {
		return spacing == null ? null : spacing.afterAutospacing;
	}
}
