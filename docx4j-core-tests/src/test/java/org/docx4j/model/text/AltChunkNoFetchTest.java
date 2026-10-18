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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.AltChunkType;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.net.httpserver.HttpServer;

/**
 * An {@code altChunk} is expanded without fetching anything (WD section 11, REQ-513), whether
 * docx4j-ImportXHTML is absent, as the build runs it, or present, as a hand run puts it on the
 * class path: its XHTML and MHT chunks then go through the importer, which {@code AltChunks} stops
 * from fetching (until 17.3.4 it fetched a stylesheet's {@code link} and {@code @import}, and an
 * {@code img} by URL or local file). A listener on the loopback interface counts the requests.
 */
public class AltChunkNoFetchTest {

	private static final byte[] PNG = Base64.getDecoder().decode(
			"iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==");

	private static final List<String> requests = Collections.synchronizedList(new ArrayList<String>());
	private static HttpServer server;
	private static String base;
	private static File png;
	private static File secret;

	@BeforeClass
	public static void listen() throws Exception {
		server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
		server.createContext("/", exchange -> {
			String path = exchange.getRequestURI().getPath();
			requests.add(path);
			byte[] body = path.endsWith(".png") ? PNG
					: (path.endsWith(".css") ? "p { color: red }" : "LEAKED_OVER_HTTP").getBytes(StandardCharsets.UTF_8);
			exchange.sendResponseHeaders(200, body.length);
			try (OutputStream out = exchange.getResponseBody()) {
				out.write(body);
			}
		});
		server.start();
		base = "http://" + server.getAddress().getAddress().getHostAddress() + ":" + server.getAddress().getPort() + "/";
		png = File.createTempFile("altchunk-no-fetch", ".png");
		png.deleteOnExit();
		Files.write(png.toPath(), PNG);
		secret = File.createTempFile("altchunk-no-fetch", ".txt");
		secret.deleteOnExit();
		Files.write(secret.toPath(), "LEAKED_FROM_FILE".getBytes(StandardCharsets.UTF_8));
	}

	@AfterClass
	public static void stop() {
		if (server != null) server.stop(0);
	}

	@Before
	public void clear() {
		requests.clear();
	}

	private static boolean importer() {
		try {
			Class.forName("org.docx4j.convert.in.xhtml.XHTMLImporterImpl");
			return true;
		} catch (ClassNotFoundException | LinkageError e) {
			return false;
		}
	}

	private static TextResult expand(AltChunkType type, String chunk) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().addAltChunk(type, chunk.getBytes(StandardCharsets.UTF_8));
		return Texts.of(pkg, new TextOptions());
	}

	private static String text(TextResult result) {
		StringBuilder sb = new StringBuilder();
		for (TextBlock block : result.blocks()) {
			for (TextRun run : block.getRuns()) if (run.getText() != null) sb.append(run.getText());
			sb.append('\n');
		}
		return sb.toString();
	}

	private static int images(TextResult result) {
		int n = 0;
		for (TextBlock block : result.blocks()) {
			for (TextRun run : block.getRuns()) if (run.getImage() != null) n++;
		}
		return n;
	}

	private static final String XHTML = "<html xmlns='http://www.w3.org/1999/xhtml'>";

	@Test
	public void xhtmlStylesheetsAndImages() throws Exception {
		TextResult result = expand(AltChunkType.Xhtml, XHTML
				+ "<head><link rel='stylesheet' href='" + base + "link.css'/>"
				+ "<style>@import url(" + base + "import.css); @font-face { font-family: F; src: url(" + base + "font.ttf) }"
				+ " p { font-family: F; background-image: url(" + base + "background.png) }</style></head>"
				+ "<body><p>CHUNK_TEXT</p>"
				+ "<p><img src='" + base + "image.png' alt='over http'/></p>"
				+ "<p><img src='" + png.toURI() + "' alt='local file'/></p>"
				+ "<p><img src='data:image/png;base64," + Base64.getEncoder().encodeToString(PNG) + "' alt='data uri'/></p>"
				+ "</body></html>");
		assertEquals("requests", Collections.emptyList(), requests);
		String text = text(result);
		assertTrue(text, text.contains("CHUNK_TEXT"));
		assertFalse(text, text.contains("MISSING IMAGE"));
		assertEquals("images", 0, images(result));
	}

	@Test
	public void xhtmlEntities() throws Exception {
		TextResult result = expand(AltChunkType.Xhtml, "<!DOCTYPE html SYSTEM '" + base + "doctype.dtd' ["
				+ "<!ENTITY http SYSTEM '" + base + "entity.txt'>"
				+ "<!ENTITY file SYSTEM '" + secret.toURI() + "'>]>"
				+ XHTML + "<body><p>CHUNK_TEXT &http; &file;</p></body></html>");
		assertEquals("requests", Collections.emptyList(), requests);
		String text = text(result);
		assertFalse(text, text.contains("LEAKED"));
	}

	@Test
	public void mht() throws Exception {
		TextResult result = expand(AltChunkType.Mht, "MIME-Version: 1.0\r\n"
				+ "Content-Type: multipart/related; boundary=\"B\"\r\n\r\n"
				+ "--B\r\nContent-Type: text/html; charset=\"utf-8\"\r\nContent-Location: file:///C:/chunk/page.htm\r\n\r\n"
				+ XHTML + "<head><link rel='stylesheet' href='" + base + "mht.css'/></head>"
				+ "<body><p>CHUNK_TEXT</p><p><img src='" + base + "mht.png' alt='m'/></p></body></html>\r\n"
				+ "--B--\r\n");
		assertEquals("requests", Collections.emptyList(), requests);
		String text = text(result);
		// read through the importer where it is present (so the hooks that stop it fetching took),
		// else not expanded
		assertEquals(text, importer(), text.contains("CHUNK_TEXT"));
		assertFalse(text, text.contains("MISSING IMAGE"));
		assertEquals("images", 0, images(result));
	}
}
