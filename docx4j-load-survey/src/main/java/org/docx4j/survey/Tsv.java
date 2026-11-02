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
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Tab-separated rows, one per line, a header first. A text value has no tab, line break or
 * double quote in it (each is written as a space, the quote as an apostrophe), so a row needs no
 * quoting: sqlite3's .import, DuckDB and a plain split all read it the same way. An empty field is
 * NULL.
 */
final class Tsv {

	private Tsv() {
	}

	static String clean(Object v) {
		if (v == null) return "";
		String s = v.toString();
		StringBuilder sb = null;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			char r = c < 0x20 || c == 0x7f ? ' ' : c == '"' ? '\'' : c;
			if (r != c && sb == null) sb = new StringBuilder(s.substring(0, i));
			if (sb != null) sb.append(r);
		}
		return sb == null ? s : sb.toString();
	}

	static String row(Object... values) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < values.length; i++) {
			if (i > 0) sb.append('\t');
			sb.append(clean(values[i]));
		}
		return sb.toString();
	}

	static String[] split(String line) {
		return line.split("\t", -1);
	}

	/**
	 * Cuts a line left unfinished (a run stopped mid-write) off the end of the file, so that the
	 * next row starts on a line of its own; that document is surveyed again.
	 *
	 * @return the bytes cut
	 */
	static long repairTail(Path p) throws IOException {
		if (!Files.exists(p)) return 0;
		try (RandomAccessFile f = new RandomAccessFile(p.toFile(), "rw")) {
			long len = f.length();
			if (len == 0) return 0;
			f.seek(len - 1);
			if (f.read() == '\n') return 0;
			long pos = len - 1;
			byte[] buf = new byte[65536];
			while (pos > 0) {
				int n = (int) Math.min(buf.length, pos);
				f.seek(pos - n);
				f.readFully(buf, 0, n);
				for (int i = n - 1; i >= 0; i--) {
					if (buf[i] == '\n') {
						long keep = pos - n + i + 1;
						f.setLength(keep);
						return len - keep;
					}
				}
				pos -= n;
			}
			f.setLength(0);
			return len;
		}
	}

	/** The file's first line, or null for a file absent or empty. */
	static String header(Path p) throws IOException {
		if (!Files.exists(p) || Files.size(p) == 0) return null;
		try (BufferedReader r = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
			return r.readLine();
		}
	}

	/**
	 * Keeps the header and the message rows of documents the main table holds: a run stopped
	 * after a document's messages and before its row leaves them behind, and they would be
	 * written again when the document is.
	 *
	 * @return the rows dropped
	 */
	static int dropOrphanMessages(Path messages, DoneSet done) throws IOException {
		if (header(messages) == null) return 0;
		Path tmp = messages.resolveSibling(messages.getFileName() + ".tmp");
		int dropped = 0;
		try (BufferedReader r = Files.newBufferedReader(messages, StandardCharsets.UTF_8);
				BufferedWriter w = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
			String line = r.readLine();
			w.write(line);
			w.write('\n');
			while ((line = r.readLine()) != null) {
				int tab = line.indexOf('\t');
				if (tab > 0 && done.contains(line.substring(0, tab))) {
					w.write(line);
					w.write('\n');
				} else {
					dropped++;
				}
			}
		}
		if (dropped > 0) {
			Files.move(tmp, messages, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
		} else {
			Files.delete(tmp);
		}
		return dropped;
	}
}
