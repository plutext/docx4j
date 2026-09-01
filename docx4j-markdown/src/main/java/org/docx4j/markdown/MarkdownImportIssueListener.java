package org.docx4j.markdown;

/**
 * Receives {@link MarkdownImportIssue}s as the importer encounters them.
 * The default listener logs each issue as a warning; supply your own (eg
 * collecting into a list) via
 * {@link MarkdownImportOptions#setIssueListener} for programmatic triage.
 */
@FunctionalInterface
public interface MarkdownImportIssueListener {

	void onIssue(MarkdownImportIssue issue);

}
