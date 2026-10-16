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
package org.docx4j.model.revisions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 * CR-043 phase 3's agreement run: docx4j's Accept All and Reject All of each fixture, against what
 * the ports' made of it - docx4j-core-ts's {@code acceptAll}/{@code rejectAll}
 * ({@code revisions/agreement/ts-resolve.mjs}) and docx4j-python's {@code accept_all}/{@code reject_all}
 * ({@code py_resolve.py}), each saved as {@code <name>.accept.docx} and {@code <name>.reject.docx} -
 * compared by {@link DocumentDescription}. Writes each description to {@code <out>} and prints the
 * number of lines that differ; {@code diff} shows them.
 *
 * <p>Usage: {@code PortsAgreement <out> <ts-dir> <py-dir> <fixture.docx>...} (a port's directory
 * may be "-" to skip it).</p>
 */
public class PortsAgreement {

	public static void main(String[] args) throws Exception {
		Path out = Paths.get(args[0]);
		String[] ports = { "ts", "py" };
		Path[] dirs = { "-".equals(args[1]) ? null : Paths.get(args[1]), "-".equals(args[2]) ? null : Paths.get(args[2]) };
		for (int i = 3; i < args.length; i++) {
			File fixture = new File(args[i]);
			String base = fixture.getName().replace(".docx", "");
			for (String what : new String[] { "accept", "reject" }) {
				WordprocessingMLPackage pkg = WordprocessingMLPackage.load(fixture);
				if (what.equals("accept")) TrackedChanges.acceptAll(pkg);
				else TrackedChanges.rejectAll(pkg);
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				pkg.save(bos);
				String ours = DocumentDescription.of(WordprocessingMLPackage.load(new ByteArrayInputStream(bos.toByteArray())));
				Files.write(out.resolve(base + "." + what + ".docx4j.txt"), ours.getBytes(StandardCharsets.UTF_8));
				StringBuilder line = new StringBuilder(base + " " + what + ":");
				for (int p = 0; p < ports.length; p++) {
					if (dirs[p] == null) continue;
					File theirs = dirs[p].resolve(base + "." + what + ".docx").toFile();
					if (!theirs.exists()) {
						line.append(' ').append(ports[p]).append(" none");
						continue;
					}
					String description = DocumentDescription.of(WordprocessingMLPackage.load(theirs));
					Files.write(out.resolve(base + "." + what + "." + ports[p] + ".txt"), description.getBytes(StandardCharsets.UTF_8));
					line.append(' ').append(ports[p]).append(' ').append(differing(ours, description));
				}
				System.out.println(line);
			}
		}
	}

	/** How many lines of one description the other lacks, both ways. */
	static int differing(String a, String b) {
		List<String> x = new ArrayList<String>(List.of(a.split("\n"))), y = new ArrayList<String>(List.of(b.split("\n")));
		int n = 0;
		for (String s : new ArrayList<String>(x)) {
			if (y.remove(s)) x.remove(s);
		}
		n += x.size() + y.size();
		return n;
	}
}
