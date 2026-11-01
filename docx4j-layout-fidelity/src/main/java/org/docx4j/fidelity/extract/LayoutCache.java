/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.
 */
package org.docx4j.fidelity.extract;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * The layouts of Word's golden PDFs, kept on disk between runs: a golden does not change
 * from one scoring run to the next, yet extracting it was a tenth of each document's time
 * (measured 2026-11-01 on real-c2).
 *
 * <p>An entry is found by the SHA-256 of the PDF's bytes, under a directory named for a
 * fingerprint of everything else the extraction depends on: the class files of this package,
 * PDFBox's version, and every {@code fidelity.*} system property but the few that only steer
 * a run (thread count, timeout, fonts and so on).  A change to the extractor, a property, or
 * a re-cut golden therefore misses the cache rather than reading a stale layout; old
 * fingerprint directories are simply never read again.</p>
 *
 * <p>{@code -Dfidelity.layoutCache=<dir>} puts the cache there (default
 * {@code ~/.cache/docx4j-fidelity/layouts}); {@code -Dfidelity.layoutCache=off} extracts every
 * time, as every run did before.  The cache holds the goldens' text, so like the corpus it stays
 * on this machine.</p>
 */
public final class LayoutCache {

	private LayoutCache() {
	}

	/** System properties that steer a run but cannot change what a PDF extracts to. */
	private static final Set<String> RUN_ONLY = new HashSet<>(Arrays.asList("fidelity.threads",
			"fidelity.timeoutSeconds", "fidelity.fonts", "fidelity.listFonts", "fidelity.fontMapper",
			"fidelity.hyphenate", "fidelity.only", "fidelity.basis", "fidelity.maxUncompressed",
			"fidelity.layoutCache"));

	private static volatile Path dir;

	/** Word's side of a comparison: from the cache where it has this PDF, else extracted and kept. */
	public static PdfLayout reference(File pdf) throws IOException {
		Path cache = cacheDir();
		if (cache == null) return PdfLayoutExtractor.extract(pdf);
		Path entry = cache.resolve(sha256(Files.readAllBytes(pdf.toPath())) + ".layout");
		if (Files.isRegularFile(entry)) {
			try (ObjectInputStream in = new ObjectInputStream(
					new java.io.BufferedInputStream(Files.newInputStream(entry)))) {
				return (PdfLayout) in.readObject();
			} catch (Exception e) {
				System.out.println("layout cache: rereading " + pdf.getName() + " (" + e + ")");
			}
		}
		PdfLayout layout = PdfLayoutExtractor.extract(pdf);
		Path tmp = null;
		try { // a layout that cannot be kept is still this run's: the cache never costs a row
			tmp = Files.createTempFile(cache, "tmp-", ".layout");
			try (ObjectOutputStream out = new ObjectOutputStream(
					new java.io.BufferedOutputStream(Files.newOutputStream(tmp)))) {
				out.writeObject(layout);
			}
			Files.move(tmp, entry, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
		} catch (IOException e) {
			System.out.println("layout cache: not kept " + pdf.getName() + " (" + e + ")");
			if (tmp != null) Files.deleteIfExists(tmp);
		}
		return layout;
	}

	/** The fingerprint's directory, made on first use; null when the cache is off. */
	static Path cacheDir() throws IOException {
		Path d = dir;
		if (d != null) return d;
		synchronized (LayoutCache.class) {
			if (dir != null) return dir;
			String setting = System.getProperty("fidelity.layoutCache", "").trim();
			if (setting.equalsIgnoreCase("off")) return null;
			Path root = setting.isEmpty()
					? Paths.get(System.getProperty("user.home"), ".cache", "docx4j-fidelity", "layouts")
					: Paths.get(setting);
			String fingerprint = fingerprint();
			dir = Files.createDirectories(root.resolve(fingerprint.substring(0, 16)));
			System.out.println("layout cache: " + dir);
			return dir;
		}
	}

	/** Forget the directory, so the next use reads -Dfidelity.layoutCache again (tests). */
	static void reset() {
		dir = null;
	}

	/** SHA-256 over this package's class files, PDFBox's version and the extraction's properties. */
	static String fingerprint() throws IOException {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			for (byte[] bytes : packageClassFiles()) {
				md.update(sha256(bytes).getBytes("UTF-8"));
			}
			md.update(("pdfbox " + org.apache.pdfbox.util.Version.getVersion()).getBytes("UTF-8"));
			TreeMap<String, String> props = new TreeMap<>();
			for (String name : System.getProperties().stringPropertyNames()) {
				if (name.startsWith("fidelity.") && !RUN_ONLY.contains(name)) props.put(name, System.getProperty(name));
			}
			md.update(props.toString().getBytes("UTF-8"));
			return hex(md.digest());
		} catch (java.security.NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * The bytes of every class file of this package, sorted by name: the names from wherever the
	 * package's classes are loaded from (a directory or a jar, more than one where a class is
	 * shadowed), the bytes from the class loader, so they are the ones that run.
	 */
	private static List<byte[]> packageClassFiles() throws IOException {
		String pkg = LayoutCache.class.getPackage().getName().replace('.', '/');
		Set<String> names = new java.util.TreeSet<>();
		for (Class<?> c : new Class<?>[] { LayoutCache.class, PdfLayout.class, PdfLayoutExtractor.class }) {
			Path root;
			try {
				root = Paths.get(c.getProtectionDomain().getCodeSource().getLocation().toURI());
			} catch (Exception e) {
				throw new IOException("layout cache: where is " + c.getName() + " loaded from?", e);
			}
			FileSystem jar = Files.isRegularFile(root) ? FileSystems.newFileSystem(root, (ClassLoader) null) : null;
			try (Stream<Path> s = Files.list((jar == null ? root : jar.getPath("/")).resolve(pkg))) {
				s.map(f -> f.getFileName().toString()).filter(n -> n.endsWith(".class")).forEach(names::add);
			} finally {
				if (jar != null) jar.close();
			}
		}
		List<byte[]> result = new ArrayList<>();
		for (String name : names) {
			try (java.io.InputStream in = LayoutCache.class.getClassLoader().getResourceAsStream(pkg + "/" + name)) {
				if (in == null) throw new IOException("layout cache: no " + pkg + "/" + name + " on the class path");
				result.add(name.getBytes("UTF-8"));
				result.add(in.readAllBytes());
			}
		}
		return result;
	}

	private static String sha256(byte[] bytes) {
		try {
			return hex(MessageDigest.getInstance("SHA-256").digest(bytes));
		} catch (java.security.NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

	private static String hex(byte[] digest) {
		StringBuilder sb = new StringBuilder();
		for (byte b : digest) sb.append(String.format("%02x", b));
		return sb.toString();
	}
}
