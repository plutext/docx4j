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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.AltChunkType;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.net.httpserver.HttpServer;

/**
 * CR-057: {@code convertAltChunks} on a document whose HTML chunk names a stylesheet and images by URL
 * fetches nothing by default (D2): a docx is untrusted input wherever a server converts it. Measured
 * 2026-10-23 on 17.3.4 (docx4j-ImportXHTML's CR-005, Probe6): the stylesheet fetched once, the image four
 * times, a {@code file:} image opened and read. Green since phase 2: the property
 * {@code docx4j.altchunk.html.fetch} (D3) or {@code convertAltChunks(true)} (D4) restores Word's fetching.
 * The text model's own reading ({@link AltChunkNoFetchTest}) never fetched since CR-046 phase 3. Needs an
 * HTML reader on the class path (as a hand run puts it there), else skipped.
 */
public class AltChunkConvertFetchTest {

	private static final byte[] PNG = Base64.getDecoder().decode(
			"iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==");
	private static final List<String> requests = Collections.synchronizedList(new ArrayList<String>());
	private static HttpServer server;
	private static String base;
	private static File png;

	@BeforeClass
	public static void listen() throws Exception {
		server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
		server.createContext("/", exchange -> {
			requests.add(exchange.getRequestURI().getPath());
			byte[] body = exchange.getRequestURI().getPath().endsWith(".png") ? PNG : "p { color: red }".getBytes(StandardCharsets.UTF_8);
			exchange.sendResponseHeaders(200, body.length);
			try (OutputStream out = exchange.getResponseBody()) { out.write(body); }
		});
		server.start();
		base = "http://" + server.getAddress().getAddress().getHostAddress() + ":" + server.getAddress().getPort() + "/";
		png = File.createTempFile("altchunk-convert", ".png"); png.deleteOnExit();
		Files.write(png.toPath(), PNG);
	}

	@AfterClass
	public static void stop() { if (server != null) server.stop(0); }

	private static WordprocessingMLPackage chunked() throws Exception {
		assumeTrue("needs an HTML reader (jsoup and docx4j-ImportXHTML) on the class path", HtmlChunkImport.readerPresent());
		requests.clear();
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().addAltChunk(AltChunkType.Xhtml, ("<html xmlns='http://www.w3.org/1999/xhtml'>"
				+ "<head><link rel='stylesheet' href='" + base + "chunk.css'/></head>"
				+ "<body><p>CHUNK_TEXT</p><p><img src='" + base + "chunk.png' alt='over http'/></p>"
				+ "<p><img src='" + png.toURI() + "' alt='local file'/></p></body></html>").getBytes(StandardCharsets.UTF_8));
		return pkg;
	}

	private static int imageParts(WordprocessingMLPackage pkg) {
		int imageParts = 0;
		for (org.docx4j.openpackaging.parts.Part part : pkg.getParts().getParts().values())
			if (part instanceof org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage) imageParts++;
		return imageParts;
	}

	private static void assertFetchedNothing(WordprocessingMLPackage pkg) {
		assertEquals("requests made while converting", Collections.emptyList(), requests);
		assertEquals("image parts copied in from a URL or a local file", 0, imageParts(pkg));
		assertTrue("the chunk's text", XmlUtils.marshaltoString(pkg.getMainDocumentPart().getJaxbElement()).contains("CHUNK_TEXT"));
	}

	private static void assertFetched(WordprocessingMLPackage pkg) {
		assertTrue("the stylesheet requested: " + requests, requests.contains("/chunk.css"));
		assertTrue("the image requested: " + requests, requests.contains("/chunk.png"));
		assertEquals("image parts: the one over http and the local file", 2, imageParts(pkg));
	}

	@Test
	public void convertAltChunksFetchesNothingByDefault() throws Exception {
		WordprocessingMLPackage pkg = chunked();
		pkg.getMainDocumentPart().convertAltChunks();
		assertFetchedNothing(pkg);
	}

	@Test
	public void aCallCanAskForWordsFetching() throws Exception {
		WordprocessingMLPackage pkg = chunked();
		pkg.getMainDocumentPart().convertAltChunks(true);
		assertFetched(pkg);
	}

	@Test
	public void thePropertyCanAskForWordsFetching() throws Exception {
		WordprocessingMLPackage pkg = chunked();
		Docx4jProperties.setProperty(HtmlChunkImport.FETCH, true);
		try {
			pkg.getMainDocumentPart().convertAltChunks();
		} finally {
			Docx4jProperties.setProperty(HtmlChunkImport.FETCH, false);
		}
		assertFetched(pkg);
	}

	@Test
	public void aCallCanRefuseWhatThePropertyAllows() throws Exception {
		WordprocessingMLPackage pkg = chunked();
		Docx4jProperties.setProperty(HtmlChunkImport.FETCH, true);
		try {
			pkg.getMainDocumentPart().convertAltChunks(false);
		} finally {
			Docx4jProperties.setProperty(HtmlChunkImport.FETCH, false);
		}
		assertFetchedNothing(pkg);
	}
}
