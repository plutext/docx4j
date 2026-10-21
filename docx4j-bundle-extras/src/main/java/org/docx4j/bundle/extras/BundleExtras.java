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
package org.docx4j.bundle.extras;

/**
 * A marker for the docx4j-bundle-extras artifact: the fat jar of docx4j-export-fo (docx to PDF
 * through XSL-FO), docx4j-chart-render, the docx4j FO renderer (a fork of Apache FOP) and the four
 * font modules, shaded without what docx4j-bundle already holds.  It is put on the classpath
 * beside docx4j-bundle of the same version; alone it has no docx4j-core.  Not for use.
 */
public final class BundleExtras {
	private BundleExtras() {
	}
}
