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
package org.docx4j.model.text;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiPredicate;

import org.docx4j.Docx4jProperties;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.AltChunkType;
import org.docx4j.openpackaging.parts.WordprocessingML.AlternativeFormatInputPart;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * An HTML, XHTML or MHT {@code altChunk} read as Word reads it (CR-053), through docx4j-ImportXHTML,
 * reached by reflection (it is LGPL):
 * <ul>
 * <li>the bytes decoded in Word's order (an MHT chunk's first HTML part in its MIME charset);</li>
 * <li>parsed by jsoup, [HTML]'s algorithm, whatever the chunk's kind, since Word reads XHTML as HTML
 *     (D9);</li>
 * <li>the tree rewritten so that the importer's CSS engine applies what Word applies (D4), and made
 *     safe to lay out: nesting capped, spans clamped, floats defused (section 4b);</li>
 * <li>not imported where it has more elements, as parsed, than {@link #MAX_ELEMENTS} (D11);</li>
 * <li>imported with what {@code display: none} hides as hidden text, as Word makes it (D5, D8), and a
 *     table's row groups in source order, where the importer can (17.3.4 on; an earlier one leaves that
 *     content out, and lays a {@code tfoot} out last);</li>
 * <li>anything the import throws, an {@code Error} included, caught: the chunk is then not read; and
 *     where {@link #TIMEOUT_MILLIS} is set, an import that takes longer abandoned (D10).</li>
 * </ul>
 * Needs jsoup and docx4j-ImportXHTML on the class path ({@link #readerPresent()}).
 * @since 17.3.4
 */
public final class HtmlChunkImport {

	private static final Logger log = LoggerFactory.getLogger(HtmlChunkImport.class);

	static final String IMPORTER = "org.docx4j.convert.in.xhtml.XHTMLImporterImpl";

	/** The property naming the most elements a chunk's tree may have to be imported (D11); 50,000 by default. */
	public static final String MAX_ELEMENTS = "docx4j.model.text.HtmlChunkImport.maxElements";

	/**
	 * The property naming how long, in milliseconds, an import may take before it is abandoned (D10);
	 * 0, the default, for no limit. An abandoned import's thread runs on to its end, holding its CPU
	 * and heap; the limit is for a server that would rather lose a chunk than wait.
	 */
	public static final String TIMEOUT_MILLIS = "docx4j.model.text.HtmlChunkImport.timeoutMillis";

	private HtmlChunkImport() {}

	/** Whether docx4j-ImportXHTML is on the class path. */
	public static boolean importerPresent() {
		try {
			Class.forName(IMPORTER, false, HtmlChunkImport.class.getClassLoader());
			return true;
		} catch (ClassNotFoundException | LinkageError e) {
			return false;
		}
	}

	/** Whether an HTML reader is present (the WD's capability): jsoup and docx4j-ImportXHTML both on the class path. */
	public static boolean readerPresent() {
		return JsoupHtml.present() && importerPresent();
	}

	/**
	 * The chunk's content as Word imports it, made in {@code target} (its styles, numbering and images
	 * are added there): what {@code convertAltChunks} puts in the chunk's place. The importer fetches
	 * what the chunk refers to, as Word does on open. Without jsoup, an XHTML or MHT chunk is read by the
	 * importer's own XML parser, as before 17.3.4, and an HTML chunk is not read. Null where the chunk is
	 * not read; why is logged.
	 */
	public static List<Object> convert(WordprocessingMLPackage target, AlternativeFormatInputPart chunk) {
		String where = chunk.getPartName() == null ? "an altChunk" : chunk.getPartName().getName();
		if (!importerPresent()) {
			log.warn("docx4j-ImportXHTML isn't on the class path, so " + where + " is left as it is");
			return null;
		}
		if (JsoupHtml.present()) {
			Document dom = tree(chunk, where);
			return dom == null ? null : imported(target, dom, true, where);
		}
		if (chunk.getAltChunkType() == AltChunkType.Html) {
			log.warn("jsoup isn't on the class path, so the HTML chunk " + where + " is left as it is");
			return null;
		}
		final boolean mht = chunk.getAltChunkType() == AltChunkType.Mht;
		final byte[] bytes = chunk.getBytes();
		return guarded(where, () -> {
			Object instance = importer(target, true, where);
			if (instance == null) return null;
			Method convert = instance.getClass().getMethod(mht ? "convertMHT" : "convert", InputStream.class, String.class);
			return numbered(target, content(convert.invoke(instance, new ByteArrayInputStream(bytes), null)));
		});
	}

	/** The text model's reading: never fetching, into a scratch package. Null where the chunk is not read. */
	static List<Object> read(WordprocessingMLPackage scratch, AlternativeFormatInputPart chunk, String where) {
		Document dom = tree(chunk, where);
		return dom == null ? null : imported(scratch, dom, false, where);
	}

	/**
	 * The tree the importer lays out: the chunk decoded and parsed by jsoup, its elements counted as
	 * parsed (WD 0.27's REQ-513), then rewritten; null where it cannot be read, or is over the element
	 * limit. Needs jsoup.
	 */
	static Document tree(AlternativeFormatInputPart chunk, String where) {
		byte[] bytes;
		try {
			bytes = chunk.getBytes();
		} catch (RuntimeException e) {
			log.warn("Couldn't read the altChunk " + where + ": " + e);
			return null;
		}
		Document dom = parsed(bytes, mht(chunk), where, null);
		if (dom == null || over(dom, where) || !rewritten(dom, chunk.getPackage(), where)) return null;
		return dom;
	}

	/**
	 * The chunk's bytes decoded (an MHT chunk's first HTML part) and parsed by jsoup, not yet rewritten;
	 * null where it cannot be read. With {@code paths}, each element's path in the tree jsoup built (WD
	 * REQ-901) is recorded there: the rewrite moves nodes rather than copying them, so a path still names
	 * its element. Needs jsoup.
	 */
	static Document parsed(byte[] bytes, boolean mht, String where, Map<org.w3c.dom.Element, String> paths) {
		String html;
		try {
			if (mht) {
				List<String> parts = InspectPayloads.mhtHtml(bytes);
				if (parts.isEmpty()) {
					log.warn("The MHT chunk " + where + " has no HTML part");
					return null;
				}
				html = parts.get(0);
			} else {
				html = HtmlChunks.decode(bytes);
			}
		} catch (RuntimeException e) {
			log.warn("Couldn't read the altChunk " + where + ": " + e);
			return null;
		}
		try {
			Document dom = JsoupHtml.parse(html);
			if (paths != null) HtmlChunks.paths(dom, paths);
			return dom;
		} catch (RuntimeException | StackOverflowError e) {
			log.warn("jsoup couldn't read the altChunk " + where + ": " + e);
			return null;
		}
	}

	/** The parsed tree rewritten as Word reads it, the host package's styles counted (D4); false, logged, where that failed. */
	static boolean rewritten(Document dom, OpcPackage host, String where) {
		try {
			HtmlChunks.rewrite(dom, HtmlChunks.hostStyles(host));
			return true;
		} catch (RuntimeException | StackOverflowError e) {
			log.warn("Couldn't rewrite the altChunk " + where + ": " + e);
			return false;
		}
	}

	/** The most elements a chunk's tree may have to be imported ({@link #MAX_ELEMENTS}). */
	static int maxElements() {
		return Docx4jProperties.getProperty(MAX_ELEMENTS, 50000);
	}

	/** Whether the tree has more elements than {@link #MAX_ELEMENTS} allows; if so, logged. */
	static boolean over(Document dom, String where) {
		int max = maxElements();
		int count = HtmlChunks.count(dom);
		if (count <= max) return false;
		log.warn("The altChunk " + where + " has " + count + " elements, over the " + max + " " + MAX_ELEMENTS
				+ " allows, so it is not read");
		return true;
	}

	/** Whether the chunk is an MHT one: by its declared content type, else its type or extension. */
	static boolean mht(AlternativeFormatInputPart chunk) {
		String ct = AltChunks.contentType(chunk).toLowerCase(Locale.ROOT);
		if (!ct.isEmpty()) return ct.equals("message/rfc822") || ct.equals("multipart/related");
		if (chunk.getAltChunkType() == AltChunkType.Mht) return true;
		String ext = chunk.getPartName() == null ? "" : chunk.getPartName().getExtension().toLowerCase(Locale.ROOT);
		return ext.equals("mht") || ext.equals("mhtml");
	}

	/** The tree through the importer's {@code convert(Node)}, which lays out the Document itself, so its elements stay docx4j's. */
	private static List<Object> imported(WordprocessingMLPackage target, Document dom, boolean fetch, String where) {
		return guarded(where, () -> {
			Object instance = importer(target, fetch, where);
			if (instance == null) return null;
			Method convert = instance.getClass().getMethod("convert", org.w3c.dom.Node.class, String.class);
			return numbered(target, content(convert.invoke(instance, dom, null)));
		});
	}

	/** An inspection's import of a chunk (CR-053 phase 3): its content, in a scratch package, and what display:none hid. */
	static final class Inspected {
		/** The scratch package holding the content in its main document: the extraction's reading of the chunk in that inspection. */
		final WordprocessingMLPackage scratch;
		/**
		 * docx4j-ImportXHTML's {@code getHiddenContent}: each outermost hidden element that produced content, in
		 * document order, to that content (the paragraphs and tables it made, its hidden runs in others); null
		 * for an importer before 17.3.4, which leaves that content out.
		 */
		final Map<org.w3c.dom.Element, List<Object>> hidden;

		Inspected(WordprocessingMLPackage scratch, Map<org.w3c.dom.Element, List<Object>> hidden) {
			this.scratch = scratch;
			this.hidden = hidden;
		}
	}

	/**
	 * The tree imported as the text model imports it ({@link #read}: never fetching, guarded), for the
	 * inspector, which reports what the import hid; null where the import failed.
	 */
	@SuppressWarnings("unchecked")
	static Inspected inspected(Document dom, String where) {
		WordprocessingMLPackage scratch;
		try {
			scratch = WordprocessingMLPackage.createPackage();
		} catch (Exception e) {
			log.warn("Couldn't make a package to read " + where + ": " + e);
			return null;
		}
		Object[] hidden = new Object[1];
		List<Object> content = guarded(where, () -> {
			Object instance = importer(scratch, false, where);
			if (instance == null) return null;
			Method convert = instance.getClass().getMethod("convert", org.w3c.dom.Node.class, String.class);
			List<Object> c = numbered(scratch, content(convert.invoke(instance, dom, null)));
			try {
				hidden[0] = instance.getClass().getMethod("getHiddenContent").invoke(instance);
			} catch (NoSuchMethodException e) {
				// before 17.3.4
			}
			return c;
		});
		if (content == null) return null;
		scratch.getMainDocumentPart().getContent().addAll(content);
		return new Inspected(scratch, (Map<org.w3c.dom.Element, List<Object>>) hidden[0]);
	}

	/**
	 * The content, with the target's numbering maps rebuilt from what the import wrote: an importer that
	 * adds a {@code w:num} to the part's XML after docx4j has built its maps (docx4j-ImportXHTML's
	 * {@code li value}, a {@code startOverride}) leaves the lists after it without labels until the
	 * package is reloaded.
	 */
	private static List<Object> numbered(WordprocessingMLPackage target, List<Object> content) {
		NumberingDefinitionsPart ndp = target.getMainDocumentPart() == null ? null
				: target.getMainDocumentPart().getNumberingDefinitionsPart();
		if (content != null && ndp != null && ndp.getJaxbElement() != null) ndp.initialiseMaps();
		return content;
	}

	/** A new importer for the target, with display:none as hidden text where it can; null where it can't be kept from fetching. */
	private static Object importer(WordprocessingMLPackage target, boolean fetch, String where) throws Exception {
		Class<?> importer = Class.forName(IMPORTER, true, HtmlChunkImport.class.getClassLoader());
		Object instance = importer.getConstructor(WordprocessingMLPackage.class).newInstance(target);
		if (!fetch) {
			String fetches = neverFetch(importer, instance);
			if (fetches != null) {
				log.warn("docx4j-ImportXHTML couldn't be stopped from fetching (" + fetches + "), so " + where + " is not read");
				return null;
			}
		}
		try {
			importer.getMethod("setDisplayNoneAsHidden", boolean.class).invoke(instance, true);
		} catch (NoSuchMethodException e) {
			log.debug("docx4j-ImportXHTML before 17.3.4: what display:none hides in " + where + " is left out");
		}
		try {
			// CR-056: the chunk's links as written, a javascript: target included, so that the inspector
			// reports it (relationship.external); the outputs refuse it where it would run (LinkTargets)
			importer.getMethod("setLinkSchemesChecked", boolean.class).invoke(instance, false);
		} catch (NoSuchMethodException e) {
			log.debug("docx4j-ImportXHTML before its CR-005: a link in " + where + " is taken as written");
		}
		try {
			// a table's row groups in source order, a tfoot where it stands, as Word reads a chunk (WD 0.28, r11-8)
			importer.getMethod("setTableRowGroupsInSourceOrder", boolean.class).invoke(instance, true);
		} catch (NoSuchMethodException e) {
			log.debug("docx4j-ImportXHTML before 17.3.4: a tfoot in " + where + " is laid out last");
		}
		return instance;
	}

	@SuppressWarnings("unchecked")
	private static List<Object> content(Object o) {
		return (List<Object>) o;
	}

	/**
	 * The import, with anything it throws caught (an {@code Error} included: a layout engine's stack
	 * overflow is the chunk's, not the caller's), and, where {@link #TIMEOUT_MILLIS} is set, abandoned
	 * after that long. Null where it failed.
	 */
	static List<Object> guarded(String where, Callable<List<Object>> work) {
		long timeout = Docx4jProperties.getPropertyLong(TIMEOUT_MILLIS, 0);
		try {
			if (timeout <= 0) return work.call();
			FutureTask<List<Object>> task = new FutureTask<List<Object>>(work);
			Thread thread = new Thread(task, "docx4j altChunk import");
			thread.setDaemon(true);
			thread.start();
			try {
				return task.get(timeout, TimeUnit.MILLISECONDS);
			} catch (TimeoutException e) {
				log.warn("The import of " + where + " took longer than " + timeout + " ms (" + TIMEOUT_MILLIS
						+ "), so it is abandoned; its thread runs on");
				task.cancel(true);
				return null;
			} catch (ExecutionException e) {
				throw e.getCause();
			}
		} catch (Throwable t) {
			Throwable cause = t instanceof InvocationTargetException && t.getCause() != null ? t.getCause() : t;
			log.warn("docx4j-ImportXHTML couldn't read " + where + ": " + cause);
			return null;
		}
	}

	/**
	 * Leaves the importer unable to fetch (WD section 11: an extraction never follows a link): an
	 * image handler that adds nothing for an {@code img}, so that neither a URL nor a local file is
	 * read (nor a data URI decoded), and no resource opened, a stylesheet's {@code link} and
	 * {@code @import} included. From docx4j-ImportXHTML 17.3.4 that is its
	 * {@code setExternalResourcesAllowed(false)}; before, a user agent, reached through the protected
	 * {@code getRenderer}, that refuses every resource and resolves no URI. Null when done, else why
	 * it couldn't be: an earlier docx4j-ImportXHTML on the module path, which doesn't open its
	 * renderer to docx4j, or a version without these hooks.
	 */
	static String neverFetch(Class<?> importer, Object instance) {
		try {
			ClassLoader loader = importer.getClassLoader();
			Class<?> handler = Class.forName("org.docx4j.convert.in.xhtml.XHTMLImageHandler", false, loader);
			importer.getMethod("setXHTMLImageHandler", handler).invoke(instance, nothing(handler));
			Method allowed = null;
			try {
				allowed = importer.getMethod("setExternalResourcesAllowed", boolean.class);
			} catch (NoSuchMethodException e) {
				// before 17.3.4
			}
			if (allowed != null) {
				allowed.invoke(instance, false);
				return null;
			}

			// The renderer the conversion will use: getRenderer makes it once, with the scratch
			// package's styles as CSS.
			Method getRenderer = importer.getDeclaredMethod("getRenderer");
			getRenderer.setAccessible(true);
			Object renderer = getRenderer.invoke(instance);
			Object agent = renderer.getClass().getMethod("getDocx4jUserAgent").invoke(renderer);
			Class<?> naive = Class.forName("com.openhtmltopdf.swing.NaiveUserAgent", false, loader);
			Class<?> priority = Class.forName("com.openhtmltopdf.outputdevice.helper.ExternalResourceControlPriority", false, loader);
			Method control = naive.getMethod("setAccessController", priority, BiPredicate.class);
			BiPredicate<String, Object> refuse = (uri, type) -> false;
			for (Object when : priority.getEnumConstants()) control.invoke(agent, when, refuse);
			Class<?> resolver = Class.forName("com.openhtmltopdf.extend.FSUriResolver", false, loader);
			naive.getMethod("setUriResolver", resolver).invoke(agent, nothing(resolver));
			return null;
		} catch (Exception | LinkageError e) { // InaccessibleObjectException on the module path
			Throwable cause = e instanceof InvocationTargetException && e.getCause() != null ? e.getCause() : e;
			return cause.toString();
		}
	}

	/** An implementation of the interface whose every method does nothing and returns null, false or 0. */
	private static Object nothing(Class<?> type) {
		return Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type }, (proxy, method, args) -> {
			if (method.getDeclaringClass() == Object.class) {
				switch (method.getName()) {
				case "equals": return proxy == args[0];
				case "hashCode": return System.identityHashCode(proxy);
				default: return "nothing " + type.getName();
				}
			}
			Class<?> returns = method.getReturnType();
			return returns.isPrimitive() && returns != void.class ? Array.get(Array.newInstance(returns, 1), 0) : null;
		});
	}
}
