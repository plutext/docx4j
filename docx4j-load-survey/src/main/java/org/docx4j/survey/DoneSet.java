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
package org.docx4j.survey;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * The documents a table already holds, by the first 64 bits of each SHA-256: 8 bytes a document,
 * where a set of strings takes about 150, and among four million documents the chance that two
 * share 64 bits is under one in a million.
 */
final class DoneSet {

	private long[] keys = new long[1024];
	private int size;

	static DoneSet read(Path tsv) throws IOException {
		DoneSet d = new DoneSet();
		if (Tsv.header(tsv) != null) {
			try (BufferedReader r = Files.newBufferedReader(tsv, StandardCharsets.UTF_8)) {
				r.readLine();
				String line;
				while ((line = r.readLine()) != null) {
					int tab = line.indexOf('\t');
					d.add(tab < 0 ? line : line.substring(0, tab));
				}
			}
		}
		Arrays.sort(d.keys, 0, d.size);
		return d;
	}

	private void add(String sha) {
		Long k = key(sha);
		if (k == null) return;
		if (size == keys.length) keys = Arrays.copyOf(keys, size * 2);
		keys[size++] = k;
	}

	boolean contains(String sha) {
		Long k = key(sha);
		return k != null && Arrays.binarySearch(keys, 0, size, k) >= 0;
	}

	int size() {
		return size;
	}

	static Long key(String sha) {
		if (sha == null || sha.length() < 16) return null;
		try {
			return Long.parseUnsignedLong(sha.substring(0, 16), 16);
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
