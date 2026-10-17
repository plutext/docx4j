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
package org.docx4j.textextraction;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.text.html.parser.ParserDelegator;
import org.docx4j.Docx4J;
import org.docx4j.TextUtils;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.model.images.DataUriConversionImageHandler;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/** Explicit baseline capture, never a conformance oracle or an automatic golden update. */
public final class ExtractionBaseline {
    public static void main(String[] args) throws Exception {
        Path fixtures = Path.of(args[0]), out = Path.of(args[1]);
        Files.createDirectories(out);
        List<Map<String, String>> rows = ExtractionConformance.table(fixtures.resolve("expected-tokens.tsv"));
        StringBuilder counts = new StringBuilder("fixture\tcase\ttoken\treader\tstatus\ttext_count\tserialized_count\n");
        StringBuilder manifest = new StringBuilder("Baseline only; no WD conformance claimed.\n")
                .append("java=").append(System.getProperty("java.version")).append('\n')
                .append("TextUtils.scope=main document JAXB element, unmodified\n")
                .append("Markdown.scope=MarkdownExporter defaults, unmodified\n")
                .append("HTML.scope=Docx4J.toHTML defaults with inline images, unmodified\n")
                .append("counts=REQ-1201 normalization; HTML text excludes attributes, styles and scripts\n");
        for (Class<?> type : List.of(TextUtils.class, Docx4J.class, Class.forName("org.docx4j.markdown.MarkdownExporter"), Class.forName("org.docx4j.markdown.WmlToMarkdown"), Class.forName("org.docx4j.jaxb.McSelection"))) {
            String location = type.getProtectionDomain().getCodeSource().getLocation().toString();
            manifest.append(type.getName()).append('=').append(location).append('\n');
            try (InputStream classBytes = type.getResourceAsStream("/" + type.getName().replace('.', '/') + ".class")) {
                manifest.append(type.getName()).append(".class.sha256=").append(sha(classBytes.readAllBytes())).append('\n');
            }
            Path jar = Path.of(type.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (Files.isRegularFile(jar)) manifest.append(type.getSimpleName()).append(".sha256=").append(sha(Files.readAllBytes(jar))).append('\n');
        }
        for (String fixture : List.of("extraction-probe.docx", "extraction-probe-external.docm")) {
            byte[] bytes = Files.readAllBytes(fixtures.resolve(fixture));
            manifest.append(fixture).append(".sha256=").append(sha(bytes)).append('\n');
            String stem = fixture.substring(0, fixture.lastIndexOf('.'));
            for (String reader : List.of("textutils", "markdown", "html")) {
                String raw = null, text = null, status = "OK";
                try {
                    WordprocessingMLPackage pkg = WordprocessingMLPackage.load(new ByteArrayInputStream(bytes));
                    if (reader.equals("textutils")) {
                        StringWriter writer = new StringWriter(); TextUtils.extractText(pkg.getMainDocumentPart().getJaxbElement(), writer);
                        raw = writer.toString(); text = raw;
                    } else if (reader.equals("markdown")) {
                        raw = Docx4J.toMarkdown(pkg); text = raw;
                    } else {
                        HTMLSettings settings = Docx4J.createHTMLSettings();
                        settings.setWmlPackage(pkg); settings.setImageHandler(new DataUriConversionImageHandler());
                        ByteArrayOutputStream output = new ByteArrayOutputStream(); Docx4J.toHTML(settings, output, Docx4J.FLAG_NONE);
                        raw = output.toString(StandardCharsets.UTF_8); text = htmlText(raw);
                    }
                    Files.writeString(out.resolve(stem + "." + reader + (reader.equals("html") ? ".html" : reader.equals("markdown") ? ".md" : ".txt")), raw, StandardCharsets.UTF_8);
                } catch (Exception | LinkageError e) {
                    status = "ERROR";
                    StringWriter failure = new StringWriter(); e.printStackTrace(new PrintWriter(failure));
                    Files.writeString(out.resolve(stem + "." + reader + ".failed.txt"), failure.toString(), StandardCharsets.UTF_8);
                }
                for (Map<String, String> row : rows) if (row.get("fixture").equals(fixture)) {
                    counts.append(fixture).append('\t').append(row.get("case")).append('\t').append(row.get("token")).append('\t')
                        .append(reader).append('\t').append(status).append('\t')
                        .append(text == null ? "" : ExtractionConformance.count(text, row.get("token"))).append('\t')
                        .append(raw == null ? "" : ExtractionConformance.count(raw, row.get("token"))).append('\n');
                }
                System.out.println(fixture + " " + reader + " " + status);
            }
        }
        Files.writeString(out.resolve("counts.tsv"), counts, StandardCharsets.UTF_8);
        Files.writeString(out.resolve("manifest.txt"), manifest, StandardCharsets.UTF_8);
    }
    static String htmlText(String html) throws Exception {
        StringBuilder out = new StringBuilder();
        new ParserDelegator().parse(new StringReader(html), new HTMLEditorKit.ParserCallback() {
            int ignored;
            @Override public void handleStartTag(HTML.Tag tag, MutableAttributeSet attributes, int position) {
                if (tag == HTML.Tag.STYLE || tag == HTML.Tag.SCRIPT || tag == HTML.Tag.HEAD) ignored++;
            }
            @Override public void handleEndTag(HTML.Tag tag, int position) {
                if (tag == HTML.Tag.STYLE || tag == HTML.Tag.SCRIPT || tag == HTML.Tag.HEAD) ignored--;
            }
            @Override public void handleText(char[] data, int position) { if (ignored == 0) out.append(data); }
        }, true);
        return out.toString();
    }
    static String sha(byte[] bytes) throws Exception {
        StringBuilder out = new StringBuilder();
        for (byte b : MessageDigest.getInstance("SHA-256").digest(bytes)) out.append(String.format("%02x", b & 255));
        return out.toString();
    }
}
