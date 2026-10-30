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
package org.docx4j.samples;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.docx4j.Docx4J;
import org.docx4j.model.text.Inspection;
import org.docx4j.model.text.PlainText;
import org.docx4j.model.text.TextBlock;
import org.docx4j.model.text.TextOptions;
import org.docx4j.model.text.TextResult;
import org.docx4j.model.text.TextRun;
import org.docx4j.model.text.TextView;
import org.docx4j.model.text.TextWarning;
import org.docx4j.model.text.Texts;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 * Extract a document's text with the text model (CR-046; the portfolio's <em>WordprocessingML Text
 * Extraction</em> specification): the text a reader of the document gets, in a chosen view of its
 * tracked changes, with the stories, fields, bindings and chunks the options ask for, and the warnings
 * and inspection counts that say what the text is not.
 *
 * <p>Shown: plain text under the defaults (the accepted view: every tracked change taken, comments
 * and hidden text left out, a field's cached result, a bound control's value); the markup view with
 * headers, footers and comments; the block model, for a program that wants paragraphs, tables and
 * runs rather than a string; the warnings; and the inspection counts, so that whoever holds the text
 * knows how much of the file it is not. {@link InspectDocument} writes those findings in full.</p>
 *
 * <p>Markdown rather than plain text is docx4j-markdown's {@code MarkdownExporter}, which writes the
 * same model (the same text, in the same view, tracked changes as CriticMarkup in the markup view).</p>
 *
 * <p>Usage: {@code TextExtraction [input.docx [output.txt]]}. With no arguments it reads
 * {@code sample-docs/sample-docxv2.docx} under the working directory and writes {@code OUT_Text.txt} there.</p>
 */
public class TextExtraction {

	public static void main(String[] args) throws Exception {

		String inputfilepath = args.length > 0 ? args[0] : System.getProperty("user.dir") + "/sample-docs/sample-docxv2.docx";
		String outputfilepath = args.length > 1 ? args[1] : System.getProperty("user.dir") + "/OUT_Text.txt";

		WordprocessingMLPackage pkg = Docx4J.load(new File(inputfilepath));

		// 1. Plain text under the defaults, with the inspection counts in the result's metadata
		// (inspect: true costs about as much again as the extraction; it is off by default).
		TextOptions options = new TextOptions().inspect(true);
		TextResult result = Texts.of(pkg, options);
		String text = PlainText.of(result);
		Files.write(new File(outputfilepath).toPath(), text.getBytes(StandardCharsets.UTF_8));
		System.out.println("Plain text, " + text.length() + " characters, written to " + outputfilepath);

		// The counts: how many findings are in this text but not seen by a person (unseen), in the file
		// but not in this text (omitted), what Office would fetch or run (active), and the rest (info).
		Inspection.Counts counts = result.counts(Inspection.Format.PLAIN);
		System.out.println("Inspection counts for this text: " + counts);

		// The warnings: where the text is not what the document holds (a branch not taken, a field
		// with no result, a chunk not expanded, a picture not described), each with the block's address.
		for (TextWarning w : result.warnings()) {
			System.out.println("  warning " + w.getKind() + " at " + w.getAddress());
		}

		// 2. Another view and scope: tracked changes shown (insertions and deletions marked), with the
		// headers, footers and comments, and hidden text. The same call; only the options differ.
		TextOptions markup = new TextOptions().view(TextView.MARKUP)
				.include(TextOptions.Include.HEADERS, TextOptions.Include.FOOTERS, TextOptions.Include.COMMENTS)
				.hidden(true);
		String markupText = PlainText.of(Texts.of(pkg, markup));
		System.out.println("Markup view with headers, footers, comments and hidden text: " + markupText.length() + " characters");

		// 3. The block model, for a program: paragraphs (with style and address), tables (rows of
		// cells of blocks), charts; each paragraph's runs with what they are (text, a note reference,
		// a field, a placeholder) and their revision in the markup view.
		int paragraphs = 0, tables = 0;
		for (TextBlock block : result.blocks()) {
			if (block.getKind() == TextBlock.Kind.TABLE) tables++;
			if (block.getKind() != TextBlock.Kind.PARAGRAPH) continue;
			paragraphs++;
			if (paragraphs > 5) continue; // the first five, as a sample
			StringBuilder line = new StringBuilder(block.getAddress()).append(" [").append(block.getStyle() == null ? "-" : block.getStyle()).append("] ");
			for (TextRun run : block.getRuns()) line.append(run.getText());
			System.out.println("  " + line);
		}
		System.out.println(paragraphs + " paragraphs and " + tables + " tables in the main story and its text boxes, notes and comments as the options include them");

		// Other options worth knowing: fields(Fields.UPDATE) computes the fields the updater can,
		// without touching the package; fieldCodes(true) writes the instructions; bindings(SURFACE)
		// reads a bound control's stored text rather than its value; altchunks and embedded control
		// whether an HTML chunk or an embedded docx, xlsx or pptx is read; profile(Profile.WORD_TEXT)
		// is Word's own Save As plain text. See TextOptions.
	}
}
