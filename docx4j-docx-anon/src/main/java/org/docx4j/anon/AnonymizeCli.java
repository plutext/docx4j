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
package org.docx4j.anon;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.docx4j.fonts.IdentityPlusMapper;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 * The command line:
 * <pre>
 * java -cp "docx4j-docx-anon.jar:lib/*" org.docx4j.anon.AnonymizeCli in.docx out.docx [--keep] [--json report.json] [--no-verify] [--no-fonts]
 * </pre>
 * (the jar's manifest names this class, so {@code java -jar} works when the
 * dependencies - docx4j-core, a JAXB runtime such as docx4j-JAXB-ReferenceImpl,
 * lorem - are on the classpath).
 * <ul>
 * <li>{@code --keep}: KEEP mode - parts the tool cannot make clean stay, and are
 * reported; the default is STRICT, which removes them.</li>
 * <li>{@code --json file}: write the report there (it is always printed).</li>
 * <li>{@code --no-verify}: skip the before/after token check.</li>
 * <li>{@code --no-fonts}: do not discover the system's fonts (faster; the
 * non-Latin replacement characters are then not checked against the document's
 * fonts' glyphs).</li>
 * </ul>
 * The output is written whether or not the result is clean (in KEEP mode it
 * never is); the exit code is 0 only when {@code result.isClean()}, 1 when not,
 * 2 for a usage error, 3 when the document could not be processed.
 *
 * @since 17.2.0
 */
public class AnonymizeCli {

	public static void main(String[] args) throws Exception {
		System.exit(run(args));
	}

	static int run(String[] args) {

		String in = null, out = null, json = null;
		Anonymize.Mode mode = Anonymize.Mode.STRICT;
		boolean verify = true, fonts = true;

		for (int i = 0; i < args.length; i++) {
			String a = args[i];
			if (a.equals("--keep")) {
				mode = Anonymize.Mode.KEEP;
			} else if (a.equals("--json")) {
				if (i + 1 >= args.length) return usage("--json needs a file");
				json = args[++i];
			} else if (a.equals("--no-verify")) {
				verify = false;
			} else if (a.equals("--no-fonts")) {
				fonts = false;
			} else if (a.startsWith("--")) {
				return usage("unknown option " + a);
			} else if (in == null) {
				in = a;
			} else if (out == null) {
				out = a;
			} else {
				return usage("too many arguments");
			}
		}
		if (in == null || out == null) return usage(null);

		AnonymizeResult result;
		try {
			WordprocessingMLPackage pkg = WordprocessingMLPackage.load(new File(in));
			if (fonts) {
				pkg.setFontMapper(new IdentityPlusMapper());
			}
			Anonymize anon = new Anonymize(pkg, mode);
			anon.setVerify(verify);
			result = anon.go();

			try (OutputStream os = new FileOutputStream(out)) {
				pkg.save(os);
			}
			String report = result.toJson();
			if (json != null) {
				Files.write(new File(json).toPath(), report.getBytes(StandardCharsets.UTF_8));
			}
			System.out.println(report);
		} catch (Exception e) {
			System.err.println("docx4j-docx-anon: " + e);
			e.printStackTrace(System.err);
			return 3;
		}

		System.err.println(result.isClean() ? "clean" : "NOT clean - see the report");
		return result.isClean() ? 0 : 1;
	}

	private static int usage(String problem) {
		if (problem != null) System.err.println(problem);
		System.err.println("usage: AnonymizeCli in.docx out.docx [--keep] [--json report.json] [--no-verify] [--no-fonts]");
		return 2;
	}

}
