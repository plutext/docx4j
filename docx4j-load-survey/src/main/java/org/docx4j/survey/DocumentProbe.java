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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.McLoadMode;
import org.docx4j.jaxb.McMode;
import org.docx4j.jaxb.McPurpose;
import org.docx4j.jaxb.McSelection;
import org.docx4j.jaxb.ResolvedAlternateContent;
import org.docx4j.mce.AlternateContent;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.wml.CTDocPart;
import org.docx4j.wml.CTEndnotes;
import org.docx4j.wml.CTFootnotes;
import org.docx4j.wml.CTFtnEdn;
import org.docx4j.wml.Comments;
import org.docx4j.wml.Document;
import org.docx4j.wml.Ftr;
import org.docx4j.wml.GlossaryDocument;
import org.docx4j.wml.Hdr;
import org.docx4j.wml.LenientInteger;

/**
 * Surveys one document on the calling thread: load it as docx4j's users do, read every JAXB part
 * (load leaves them to unmarshal on first use), count the mc:AlternateContent branches read, and
 * optionally save it. Everything but an OutOfMemoryError is caught and recorded.
 */
final class DocumentProbe {

	static final String DOCX4J_VERSION = LoadSurvey.docx4jVersion();

	private final boolean save;

	/** What the document's thread is doing, for a timeout's report. */
	volatile String step = "start";

	/** The row as far as it has got: a timeout or an OutOfMemoryError reports what it holds. */
	final Result result = new Result();

	DocumentProbe(boolean save) {
		this.save = save;
	}

	Result run(Path file, String sha, LogCapture.Session log) {
		Result r = result;
		r.set(Column.SHA256, sha);
		r.set(Column.DOCX4J_VERSION, DOCX4J_VERSION);
		try {
			r.set(Column.BYTES, Files.size(file));
		} catch (IOException e) {
			r.note("size: " + Describe.of(e));
		}
		LenientInteger.beginReport(); // the outermost scope: each part's scope inside it is counted here, not logged
		OpcPackage pkg = null;
		try {
			log.phase("load");
			step = "load";
			long t0 = System.nanoTime();
			try {
				pkg = OpcPackage.load(file.toFile(), McLoadMode.WORD);
			} finally {
				r.set(Column.LOAD_MS, ms(t0));
			}
			r.set(Column.LOADED, 1);
			r.set(Column.PACKAGE, pkg.getClass().getSimpleName());
			r.set(Column.STRICT, pkg.isWasStrict());
			List<Part> parts = new ArrayList<>(pkg.getParts().getParts().values());
			walk(read(pkg, parts, r, log), r, log);
		} catch (OutOfMemoryError e) {
			throw e;
		} catch (Throwable e) {
			// only load throws this far: read and walk keep their own
			r.set(Column.LOADED, pkg == null ? 0 : 1);
			r.set(Column.OUTCOME, "load_failed");
			r.set(Column.EXCEPTION, Describe.of(e));
			r.set(Column.ROOT_CAUSE, Describe.of(Describe.root(e)));
		} finally {
			lenient(r); // before the save, whose marshal checks the same values again
		}
		r.set(Column.WARNINGS, log.warnings());
		r.set(Column.ERRORS, log.errors());
		if (pkg != null) r.set(Column.PARTS_PREPROCESSED, log.preprocessedParts());
		if (save && pkg != null) {
			save(pkg, r, log);
		}
		if (log.overflow() > 0) r.note(log.overflow() + " log events beyond " + LogCapture.MAX_DISTINCT + " distinct messages");
		if (r.get(Column.OUTCOME) == null) {
			r.set(Column.OUTCOME, Integer.valueOf(0).equals(r.get(Column.MAIN_OK)) ? "main_unreadable" : "ok");
		}
		step = "done";
		return r;
	}

	/** Closes the survey's LenientInteger scope: the whole document's counts (CR-065, CR-066). */
	private static void lenient(Result r) {
		LenientInteger.Report rep = LenientInteger.endReport();
		if (rep == null) {
			r.note("LenientInteger report not closed (a part's scope left open)");
			return;
		}
		r.set(Column.LENIENT_TRUNCATED, rep.getTruncated());
		r.set(Column.LENIENT_EMPTY, rep.getEmpty());
		r.set(Column.LENIENT_DROPPED, rep.getDropped());
		r.set(Column.OUT_OF_RANGE, rep.getOutOfRange());
	}

	/** Reads every JAXB part; returns those that read. */
	private List<JaxbXmlPart<?>> read(OpcPackage pkg, List<Part> parts, Result r, LogCapture.Session log) {
		log.phase("read");
		long t0 = System.nanoTime();
		Part main = pkg instanceof WordprocessingMLPackage ? ((WordprocessingMLPackage) pkg).getMainDocumentPart() : null;
		Integer mainOk = pkg instanceof WordprocessingMLPackage && main == null ? 0 : null;
		if (pkg instanceof WordprocessingMLPackage && main == null) r.note("no main document part");
		int xml = 0;
		int unreadable = 0;
		List<JaxbXmlPart<?>> readable = new ArrayList<>();
		for (Part p : parts) {
			if (!(p instanceof JaxbXmlPart)) continue;
			xml++;
			String name = p.getPartName().getName();
			step = "read " + name;
			boolean ok;
			try {
				ok = ((JaxbXmlPart<?>) p).getContents() != null;
				if (!ok && r.get(Column.UNREADABLE) == null) r.set(Column.UNREADABLE, name + ": no contents");
			} catch (OutOfMemoryError e) {
				throw e;
			} catch (Exception | StackOverflowError e) {
				ok = false;
				if (r.get(Column.UNREADABLE) == null) r.set(Column.UNREADABLE, Describe.line(name + ": " + Describe.of(Describe.root(e))));
			}
			if (ok) readable.add((JaxbXmlPart<?>) p);
			else unreadable++;
			if (p == main) mainOk = ok ? 1 : 0;
		}
		r.set(Column.READ_MS, ms(t0));
		r.set(Column.PARTS, parts.size());
		r.set(Column.PARTS_XML, xml);
		r.set(Column.PARTS_UNREADABLE, unreadable);
		r.set(Column.MAIN_OK, mainOk);
		return readable;
	}

	/** Each mc:AlternateContent read: those resolved at load, then those kept in the text stories' trees. */
	private void walk(List<JaxbXmlPart<?>> parts, Result r, LogCapture.Session log) {
		log.phase("walk");
		Map<String, int[]> branches = new HashMap<>();
		int[] elements = { 0 };
		int resolved = 0;
		try {
			for (JaxbXmlPart<?> jp : parts) {
				Object contents = jp.getContents(); // read already
				step = "mc " + jp.getPartName().getName();
				for (ResolvedAlternateContent rac : jp.getResolvedAlternateContent()) {
					resolved++;
					elements[0]++;
					count(branches, label(rac.getBranch()));
				}
				List<Object> roots = storyRoots(contents);
				if (roots.isEmpty()) continue;
				new TraversalUtil(roots, new TraversalUtil.CallbackImpl() {
					@Override
					public List<Object> apply(Object o) {
						Object u = XmlUtils.unwrap(o);
						if (u instanceof AlternateContent) {
							elements[0]++;
							count(branches, label(McSelection.selectedBranch(McPurpose.TEXT, (AlternateContent) u)));
						}
						return null;
					}
				}, McMode.READ);
			}
			r.set(Column.MC_ELEMENTS, elements[0]);
			r.set(Column.MC_RESOLVED, resolved);
			r.set(Column.MC_CHOICES, branches.isEmpty() ? null : format(branches));
		} catch (OutOfMemoryError e) {
			throw e;
		} catch (Exception | StackOverflowError e) {
			r.note("mc count failed in " + step + ": " + Describe.of(e));
		}
	}

	/** The block-level content of a text story's root, or nothing for another part. */
	static List<Object> storyRoots(Object contents) {
		List<Object> roots = new ArrayList<>();
		if (contents instanceof Document || contents instanceof Hdr || contents instanceof Ftr) {
			roots.add(contents);
		} else if (contents instanceof CTFootnotes) {
			for (CTFtnEdn n : ((CTFootnotes) contents).getFootnote()) roots.add(n.getContent());
		} else if (contents instanceof CTEndnotes) {
			for (CTFtnEdn n : ((CTEndnotes) contents).getEndnote()) roots.add(n.getContent());
		} else if (contents instanceof Comments) {
			for (Comments.Comment c : ((Comments) contents).getComment()) roots.add(c.getContent());
		} else if (contents instanceof GlossaryDocument && ((GlossaryDocument) contents).getDocParts() != null) {
			for (CTDocPart dp : ((GlossaryDocument) contents).getDocParts().getDocPart()) {
				if (dp.getDocPartBody() != null) roots.add(dp.getDocPartBody());
			}
		}
		return roots;
	}

	/** A Choice by its Requires as written, the Fallback, or none (an element with no branch). */
	static String label(Object branch) {
		if (branch instanceof AlternateContent.Choice) {
			String req = ((AlternateContent.Choice) branch).getRequires();
			return req == null || req.trim().isEmpty() ? "Choice" : req.trim().replaceAll("\\s+", "+");
		}
		return branch instanceof AlternateContent.Fallback ? "Fallback" : "none";
	}

	private static void count(Map<String, int[]> m, String k) {
		m.computeIfAbsent(k, x -> new int[1])[0]++;
	}

	/** "wps=5 Fallback=2": the most frequent first. */
	static String format(Map<String, int[]> m) {
		List<Map.Entry<String, int[]>> es = new ArrayList<>(m.entrySet());
		Collections.sort(es, (a, b) -> a.getValue()[0] != b.getValue()[0] ? b.getValue()[0] - a.getValue()[0] : a.getKey().compareTo(b.getKey()));
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, int[]> e : es) {
			if (sb.length() > 0) sb.append(' ');
			sb.append(e.getKey()).append('=').append(e.getValue()[0]);
		}
		return sb.toString();
	}

	private void save(OpcPackage pkg, Result r, LogCapture.Session log) {
		log.phase("save");
		step = "save";
		long t0 = System.nanoTime();
		try {
			pkg.save(OutputStream.nullOutputStream());
			r.set(Column.SAVED, 1);
		} catch (OutOfMemoryError e) {
			throw e;
		} catch (Exception | StackOverflowError e) {
			r.set(Column.SAVED, 0);
			r.set(Column.SAVE_EXCEPTION, Describe.withRoot(e));
		} finally {
			r.set(Column.SAVE_MS, ms(t0));
		}
	}

	private static long ms(long t0) {
		return Math.round((System.nanoTime() - t0) / 1e6);
	}
}
