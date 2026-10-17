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

import java.util.List;
import java.util.Objects;

/**
 * Plain text from the block model (WD section 7.1): each paragraph one line, a break one line more
 * (REQ-603); a table's rows a line each, their cells a tab apart (REQ-605's simple form; CR-046
 * phase 2 adds the listed and grid forms); the text exactly as the document holds it (REQ-602),
 * U+000A between lines and after the last.
 *
 * <p>What the extractor adds follows the WD's bracket convention (REQ-707): {@code [n]} at a note's
 * reference and at the start of the note. In the {@code markup} view, insertions are
 * {@code {++text++}}, deletions {@code {--text--}} and comments {@code {>>text<<}}, at the end of
 * their range with their replies after them (REQ-702, REQ-504); under {@code markup_detail} the
 * marker's opening is followed by {@code author:} or {@code author, date:}.</p>
 *
 * @since 17.3.4 (CR-046 phase 1)
 */
public final class PlainText {

	private PlainText() {}

	/** The plain text of a result. */
	public static String of(TextResult result) {
		StringBuilder sb = new StringBuilder();
		Writer w = new Writer(result, sb);
		for (TextBlock block : result.blocks()) {
			w.block(block);
			sb.append('\n');
		}
		return sb.toString();
	}

	private static final class Writer {
		final TextResult result;
		final TextOptions options;
		final StringBuilder sb;

		Writer(TextResult result, StringBuilder sb) {
			this.result = result;
			this.options = result.options();
			this.sb = sb;
		}

		void block(TextBlock block) {
			if (block.getKind() == TextBlock.Kind.TABLE) {
				List<TextRow> rows = block.getRows();
				for (int r = 0; r < rows.size(); r++) {
					if (r > 0) sb.append('\n');
					List<TextCell> cells = rows.get(r).getCells();
					for (int c = 0; c < cells.size(); c++) {
						if (c > 0) sb.append('\t');
						List<TextBlock> blocks = cells.get(c).getBlocks();
						for (int b = 0; b < blocks.size(); b++) {
							if (b > 0) sb.append(' ');
							block(blocks.get(b));
						}
					}
				}
				return;
			}
			if (block.getListLabel() != null) sb.append(block.getListLabel());
			List<TextRun> runs = block.getRuns();
			int i = 0;
			while (i < runs.size()) {
				TextRevision revision = runs.get(i).getRevision();
				int j = i;
				while (j < runs.size() && sameMarker(revision, runs.get(j).getRevision())) j++;
				if (revision != null) {
					sb.append(revision.getType() == TextRevision.Type.INSERTION ? "{++" : "{--");
					detail(revision.getAuthor(), revision.getDate());
				}
				for (int k = i; k < j; k++) run(runs.get(k));
				if (revision != null) sb.append(revision.getType() == TextRevision.Type.INSERTION ? "++}" : "--}");
				i = j;
			}
		}

		/** Runs under one marker: the same type, and the same author and date where the detail shows them. */
		private boolean sameMarker(TextRevision a, TextRevision b) {
			if (a == null || b == null) return a == b;
			if (a.getType() != b.getType()) return false;
			switch (options.getMarkupDetail()) {
			case AUTHOR: return Objects.equals(a.getAuthor(), b.getAuthor());
			case AUTHOR_DATE: return Objects.equals(a.getAuthor(), b.getAuthor()) && Objects.equals(a.getDate(), b.getDate());
			default: return true;
			}
		}

		private void detail(String author, String date) {
			switch (options.getMarkupDetail()) {
			case AUTHOR:
				sb.append(author == null ? "" : author).append(": ");
				break;
			case AUTHOR_DATE:
				sb.append(author == null ? "" : author).append(", ").append(date == null ? "" : date).append(": ");
				break;
			default:
			}
		}

		void run(TextRun run) {
			switch (run.getKind()) {
			case TEXT:
			case FIELD_CODE:
			case LIST_LABEL:
				sb.append(run.getText());
				break;
			case NOTE_REFERENCE:
				if (options.isNoteReferences() && options.isNotes() && run.getNoteNumber() > 0) sb.append('[').append(run.getNoteNumber()).append(']');
				break;
			case NOTE_MARK:
				if (run.getNoteNumber() > 0) sb.append('[').append(run.getNoteNumber()).append(']');
				break;
			case COMMENT_REFERENCE: {
				TextComment comment = result.comments().get(run.getCommentId());
				if (comment == null || comment.getParentId() != null && result.comments().containsKey(comment.getParentId())) break;
				comment(comment);
				for (TextComment reply : comment.getReplies()) comment(reply);
				break;
			}
			default:
			}
		}

		private void comment(TextComment comment) {
			sb.append("{>>");
			detail(comment.getAuthor(), comment.getDate());
			sb.append(comment.getText()).append("<<}");
		}
	}
}
