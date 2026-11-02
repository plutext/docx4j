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
package org.docx4j.convert.out.fo;

import org.junit.Assume;

/**
 * For a test of Word layout, which since 17.3.6 is the docx4j FO renderer's (CR-071, D7): on Apache FOP
 * ({@code -Papache-fop}) docx4j lays out as plain FOP does, writes none of the docx4j: attributes and keeps a tab
 * after text as its stand-in, so such a test is skipped there.
 */
public final class WordLayoutOn {

	private WordLayoutOn() {
	}

	public static void assume() {
		Assume.assumeTrue("Word layout is the docx4j FO renderer's (hook word-layout); Apache FOP lays out as plain FOP",
				FopCapabilities.has(FopCapabilities.Capability.WORD_LAYOUT));
	}
}
