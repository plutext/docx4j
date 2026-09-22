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
package org.docx4j.openpackaging.io3.stores;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * The count of bytes after the zip end of central directory record (the
 * "Upload" damage the 17.2.0 warning is for) is right for a file shorter than
 * the 64K tail the check reads and, since 17.2.1, for a longer one too: the
 * tail's offset was being added, so every real-sized package warned.
 */
public class ZipPartStoreTrailingBytesTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private File copy(String resource, String trailing) throws Exception {
		File f = tmp.newFile();
		try (InputStream is = getClass().getResourceAsStream("/" + resource)) {
			assertNotNull(resource, is);
			Files.copy(is, f.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
		}
		if (trailing != null) {
			Files.write(f.toPath(), trailing.getBytes(StandardCharsets.US_ASCII), StandardOpenOption.APPEND);
		}
		return f;
	}

	@Test
	public void shortFile() throws Exception {
		File f = copy("anon/chart.docx", null);
		assertTrue("fixture shorter than the tail", f.length() < 22 + 0xFFFF);
		assertEquals(0, ZipPartStore.bytesAfterCentralDirectory(f));
		assertEquals(6, ZipPartStore.bytesAfterCentralDirectory(copy("anon/chart.docx", "Upload")));
	}

	@Test
	public void longFile() throws Exception {
		File f = copy("anon/AutoShapes.pptx", null);
		assertTrue("fixture longer than the tail", f.length() > 22 + 0xFFFF);
		assertEquals(0, ZipPartStore.bytesAfterCentralDirectory(f));
		assertEquals(5, ZipPartStore.bytesAfterCentralDirectory(copy("anon/AutoShapes.pptx", "Subir")));
	}

}
