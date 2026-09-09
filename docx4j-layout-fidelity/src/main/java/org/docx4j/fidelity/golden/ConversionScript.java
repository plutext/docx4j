package org.docx4j.fidelity.golden;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Which VBS documents4j has Word run - shared by every tool here that drives Word, because
 * {@link WordGoldenRunner} and {@link ResaveInvariance} must not drift apart on it.
 *
 * <p>It decides two things that are not details, because each is a property of the golden
 * set rather than of a run, and a set has to say which it is ({@link #mode()},
 * {@link #markup()} and {@link #path()} go into the manifest written beside it):</p>
 *
 * <ul>
 * <li><b>Whether Word updates the document's fields</b> on the way through
 *     ({@code -Dfidelity.updateFields}). Measured on the corpus: a document rendered with the
 *     update reproduces its golden <em>exactly</em>, and the same document rendered without it
 *     differs by hundreds of lines over almost every page. Cut with the update off, both sides
 *     show what the file holds. (Measured since: the fields Word evaluates without an update -
 *     PAGE, NUMPAGES, PAGEREF, PRINTDATE, and DATE/TIME on open - are exactly the ones docx4j
 *     evaluates, so the difference the update makes is a regenerated TOC's text and little
 *     else; see the README.)</li>
 * <li><b>Whether Word prints its review markup</b> ({@code -Dfidelity.showMarkup}). Word paints
 *     comment balloons - and tracked changes - into a PDF according to the <em>application's</em>
 *     "display for review" state, not the document's, so a set cut on a machine where someone
 *     last looked at a document with markup showing holds balloon text in its text layer and a
 *     page scaled to make room for them. That cost one corpus's eight commented documents their
 *     baseline. docx4j renders no balloons, so a golden with them cannot be matched; the script
 *     therefore turns the markup display off itself, whatever the machine happens to be set to,
 *     unless told to keep it.</li>
 * </ul>
 *
 * <p>Both scripts are generated here from documents4j's own {@code word_convert.vbs} (an open,
 * a save, a close), so the two modes differ only in the field-update block and the same markup
 * handling reaches both. The resolution has three parts that are each easy to get wrong, which
 * is the other reason it lives in one place:</p>
 *
 * <ul>
 * <li>The script is named by a <b>system</b> property, {@link #SCRIPT_PROPERTY}.
 *     {@code Documents4jLocalServices} reads that first and only falls back to
 *     {@code docx4j.properties}, so a value there is the fallback, not the winner.</li>
 * <li>documents4j materialises the script <b>once</b>, in the bridge constructor
 *     ({@code AbstractMicrosoftOfficeBridge}), which is to say when the {@code LocalConverter}
 *     is built. So {@link #configure} has to run before the first conversion, and the mode
 *     cannot change within a run.</li>
 * <li>Turning the update <b>off</b> therefore cannot simply clear the property, because
 *     {@code docx4j.properties} would put the configured script back. A script is written
 *     and pointed at instead.</li>
 * </ul>
 */
final class ConversionScript {

	/**
	 * Says loudly when there is no {@code docx4j.properties} on the classpath, because the
	 * consequence is easy to miss and it decides what Word does with fields: the field-updating
	 * conversion script is named <em>in</em> {@code docx4j.properties}, so without that file
	 * {@code Documents4jLocalServices} has nothing to fall back to and Word runs documents4j's
	 * default script, which updates no field. A run configured that way may not reproduce the
	 * configuration the goldens were cut under, and a field difference it reports may be an
	 * artefact of its own setup. The warning names the fix rather than only the problem - and
	 * says nothing at all where {@code -Dfidelity.updateFields} has already settled the
	 * question, since the file is then not what decides it.
	 */
	static void warnAboutProperties() {
		if (System.getProperty(MODE_PROPERTY) != null) {
			/* The configuration is decided, so the warning would be noise - but the run still
			 * says that docx4j.properties was not what decided it. */
			System.out.println("docx4j.properties: not consulted - " + MODE_PROPERTY + " decides the script");
			return;
		}
		java.net.URL url = ConversionScript.class.getClassLoader().getResource("docx4j.properties");
		if (url != null) {
			System.out.println("docx4j.properties: " + url);
			return;
		}
		System.out.println("****************************************************************");
		System.out.println("*  WARNING: no docx4j.properties on the classpath               *");
		System.out.println("****************************************************************");
		System.out.println("The field-updating conversion script is named in docx4j.properties, under");
		System.out.println("  " + SCRIPT_PROPERTY);
		System.out.println("so without that file Word runs documents4j's default script, which updates no");
		System.out.println("field - which may not be how the goldens were cut. This run may therefore not");
		System.out.println("reproduce the golden's configuration, and a field difference it reports may be");
		System.out.println("an artefact of this run rather than a fact about the document.");
		System.out.println("Fix it by putting a directory that holds docx4j.properties FIRST on the");
		System.out.println("classpath, ahead of target\\classes and target\\lib\\*, e.g.");
		System.out.println("  java -cp \"conf;target\\classes;target\\lib\\*\" ...");
		System.out.println("(docx4j-samples-resources holds a reference copy), or name the script here");
		System.out.println("with -Dfidelity.updateFields=true -Dfidelity.fieldUpdateScript=<path>.");
		System.out.println();
	}

	/** What the run did about field updating, for the summary and the manifest - the question
	 *  is unanswerable from a PDF alone, so a run says which script cut it. */
	private static String description = "none - no conversion was run";

	/** {@code on}, {@code off} or {@code as-configured}: the field-update mode, for a manifest
	 *  that has to be read back by something other than a human. */
	private static String mode = "none";

	/** {@code off} (the script hides the review markup before it converts), {@code on} (it
	 *  leaves the display as it finds it), or {@code as-scripted} where a script this class did
	 *  not write is in charge and nothing here can say what it does. */
	private static String markup = "none";

	/** The script Word will run, absolute, or documents4j's own where none was named. */
	private static String path = "documents4j default (bundled)";

	/** The system property that decides the field-update mode. */
	static final String MODE_PROPERTY = "fidelity.updateFields";

	/** The system property that decides whether Word's review markup (comment balloons, tracked
	 *  changes) is printed: {@code true} leaves the machine's display-for-review state in charge,
	 *  as every set before this line was cut; unset or {@code false} has the script turn the
	 *  markup display off before it converts. */
	static final String MARKUP_PROPERTY = "fidelity.showMarkup";

	static String description() {
		return description;
	}

	static String mode() {
		return mode;
	}

	static String markup() {
		return markup;
	}

	static String path() {
		return path;
	}

	/** The property documents4j reads, and which {@code Documents4jLocalServices} sets from
	 *  {@code docx4j.properties} when it is not already a system property. */
	static final String SCRIPT_PROPERTY = "com.documents4j.conversion.msoffice.word_convert.vbs";

	/**
	 * Decides which VBS Word will run, and says so.
	 *
	 * <p>documents4j materialises its conversion script <em>once</em>, in the bridge
	 * constructor ({@code AbstractMicrosoftOfficeBridge}), which is to say when the
	 * {@code LocalConverter} is built - so the choice has to be made before the first
	 * conversion, and cannot be changed within a run. Hence one mode per run, and two runs
	 * into two output directories to compare.</p>
	 *
	 * <ul>
	 * <li>{@code -Dfidelity.updateFields=true} - fields are updated. The script is the one
	 *     named by {@code -Dfidelity.fieldUpdateScript=<path>} if given; otherwise this class
	 *     writes one into {@code dir}. Writing our own is not gold-plating: the sample
	 *     script in {@code docx4j-samples-resources} updates {@code TablesOfContents(1)}
	 *     inside the {@code On Error Resume Next} block that precedes its {@code Err} check,
	 *     so a document with <em>no</em> table of contents quits -2 and is reported as
	 *     "The input file seems to be corrupt". The generated script updates every story
	 *     range's fields and every TOC, and clears {@code Err} afterwards, so a document with
	 *     neither is converted rather than failed.</li>
	 * <li>{@code -Dfidelity.updateFields=false} - fields are not updated. Clearing the system
	 *     property is not enough, because {@code Documents4jLocalServices} would then fall
	 *     back to {@code docx4j.properties} and put the configured script back; so a script
	 *     is written into {@code dir} and pointed at explicitly: documents4j's own
	 *     {@code word_convert.vbs} - an open, a save and a close, touching no field - plus
	 *     the markup handling below.</li>
	 * <li>unset - whatever the machine is configured for is left alone, and the resolved
	 *     script is printed so the run says what it did.</li>
	 * </ul>
	 *
	 * <p>Either generated script turns Word's review-markup display off before converting,
	 * unless {@code -Dfidelity.showMarkup=true}: see {@link #MARKUP_PROPERTY}. A script named
	 * with {@code -Dfidelity.fieldUpdateScript} or by the machine's configuration does whatever
	 * it does, and the manifest says so ({@code markup=as-scripted}).</p>
	 */
	static void configure(File dir) throws IOException {
		boolean showMarkup = Boolean.parseBoolean(System.getProperty(MARKUP_PROPERTY, "false"));
		String want = System.getProperty(MODE_PROPERTY);
		if (want == null) {
			String configured = System.getProperty(SCRIPT_PROPERTY);
			if (configured == null) {
				configured = org.docx4j.Docx4jProperties.getProperty(SCRIPT_PROPERTY);
				description = configured == null
						? "as configured: documents4j's default script (no field update)"
						: "as configured: " + configured + " (from docx4j.properties)";
				if (configured != null) path = configured;
			} else {
				description = "as configured: " + configured + " (from -D)";
				path = configured;
			}
			mode = "as-configured";
			markup = "as-scripted";
			System.out.println("field update: not specified; " + description);
			System.out.println("  -Dfidelity.updateFields=true|false to decide it here");
			System.out.println("review markup: as the machine's script leaves it (" + MARKUP_PROPERTY
					+ " applies only to a script written here)");
			return;
		}
		boolean update = Boolean.parseBoolean(want);
		String named = update ? System.getProperty("fidelity.fieldUpdateScript") : null;
		File script;
		if (named != null) {
			script = new File(named);
			if (!script.isFile()) throw new IOException("no such script: " + script);
			markup = "as-scripted";
		} else {
			script = new File(dir, update ? "word_convert-updatefields.vbs" : "word_convert-nofields.vbs");
			Files.write(script.toPath(), script(update, showMarkup).getBytes(StandardCharsets.US_ASCII));
			markup = showMarkup ? "on" : "off";
		}
		System.setProperty(SCRIPT_PROPERTY, script.getAbsolutePath());
		mode = update ? "on" : "off";
		path = script.getAbsolutePath();
		description = (update ? "ON" : "OFF") + ", via " + path;
		System.out.println("field update: " + description);
		System.out.println("review markup: " + ("off".equals(markup)
				? "off - the script hides comment balloons and tracked changes before converting"
				: "on".equals(markup)
						? "ON - " + MARKUP_PROPERTY + "=true leaves the machine's display-for-review state in charge"
						: "as the named script leaves it"));
	}

	/**
	 * The conversion script: documents4j's default {@code word_convert.vbs} - which opens,
	 * converts and closes - with, in order, the markup display turned off (unless
	 * {@code showMarkup}) and a field update (if {@code updateFields}) inserted between the open
	 * and the conversion.  Generated rather than copied from documents4j's jar so that the two
	 * field modes share one text and one markup handling.
	 *
	 * <p>The markup block sets three things, each in its own {@code On Error Resume Next} so
	 * that a document without a window, or an older Word, costs nothing: the window's
	 * {@code View.ShowRevisionsAndComments}, which is the "display for review" state Word's
	 * PDF output follows; its {@code View.RevisionsFilter.Markup} ({@code wdRevisionsMarkupNone},
	 * 0), for a Word whose view honours the filter rather than the flag; and the document's
	 * {@code PrintRevisions}, the "print markup" print option, which the PDF path also
	 * consults.  The conversion itself stays {@code SaveAs ... 17}, as documents4j's own script
	 * does, so a set cut by this script is on the same footing as the sets already cut
	 * (moving to {@code ExportAsFixedFormat} would be a second change of basis).  The field
	 * update, where asked for, reaches every story range's fields (the body, but also the
	 * headers, footers and footnotes, which {@code Document.Fields} alone does not reach) and
	 * every table of contents, with {@code Err} cleared afterwards so that a document holding
	 * neither is still converted.  CRLF, because it is handed to {@code cscript}.</p>
	 */
	static String script(boolean updateFields, boolean showMarkup) {
		StringBuilder s = new StringBuilder();
		for (String line : HEAD) s.append(line).append("\r\n");
		s.append("' generated by org.docx4j.fidelity.golden.ConversionScript: documents4j's default\r\n");
		s.append("' word_convert.vbs" + (updateFields ? " plus a field update" : "")
				+ (showMarkup ? ", leaving the review-markup display as the machine has it"
						: ", with the review-markup display turned off") + "\r\n");
		s.append("' (fidelity.updateFields=" + updateFields + ", fidelity.showMarkup=" + showMarkup + ")\r\n");
		for (String line : OPEN) s.append(line).append("\r\n");
		if (!showMarkup) for (String line : HIDE_MARKUP) s.append(line).append("\r\n");
		if (updateFields) for (String line : UPDATE_FIELDS) s.append(line).append("\r\n");
		for (String line : CONVERT) s.append(line).append("\r\n");
		return s.toString();
	}

	private static final String[] HEAD = {
			"Const WdDoNotSaveChanges = 0",
			"Const WdExportFormatPDF = 17",
			"Const MagicFormatPDFA = 999",
			"Const MagicFormatFilteredHTML = 10",
			"Const msoEncodingUTF8 = 65001",
			"Const WdRevisionsMarkupNone = 0",
			"",
	};

	private static final String[] OPEN = {
			"",
			"Function ConvertFile( inputFile, outputFile, formatEnumeration )",
			"",
			"  Dim fileSystemObject",
			"  Dim wordApplication",
			"  Dim wordDocument",
			"",
			"  On Error Resume Next",
			"  Set wordApplication = GetObject(, \"Word.Application\")",
			"  If Err <> 0 Then",
			"    WScript.Quit -6",
			"  End If",
			"  On Error GoTo 0",
			"",
			"  Set fileSystemObject = CreateObject(\"Scripting.FileSystemObject\")",
			"  inputFile = fileSystemObject.GetAbsolutePathName(inputFile)",
			"",
			"  If fileSystemObject.FileExists(inputFile) Then",
			"",
			"    On Error Resume Next",
			"    Set wordDocument = wordApplication.Documents.Open(inputFile, False, True, False)",
			"    If wordDocument = \"\" OR Err <> 0 Then",
			"      WScript.Quit -2",
			"    End If",
			"    On Error GoTo 0",
			"",
	};

	private static final String[] HIDE_MARKUP = {
			"    ' Hide the review markup (comment balloons, tracked changes) before converting: Word's",
			"    ' PDF follows the application's display-for-review state, not the document's, so",
			"    ' without this a set depends on what the machine was last left showing.  Each on its",
			"    ' own so that a document with no window, or an older Word, costs nothing.",
			"    On Error Resume Next",
			"    wordDocument.ActiveWindow.View.ShowRevisionsAndComments = False",
			"    Err.Clear",
			"    wordDocument.ActiveWindow.View.RevisionsFilter.Markup = WdRevisionsMarkupNone",
			"    Err.Clear",
			"    wordDocument.PrintRevisions = False",
			"    Err.Clear",
			"    On Error GoTo 0",
			"",
	};

	private static final String[] UPDATE_FIELDS = {
			"    ' Update the fields.  A document with no fields and no table of contents is not a",
			"    ' failure, so Err is cleared rather than checked.",
			"    On Error Resume Next",
			"    Dim story",
			"    For Each story In wordDocument.StoryRanges",
			"      story.Fields.Update",
			"    Next",
			"    Dim toc",
			"    For Each toc In wordDocument.TablesOfContents",
			"      toc.Update",
			"    Next",
			"    Err.Clear",
			"    On Error GoTo 0",
			"",
	};

	private static final String[] CONVERT = {
			"    If formatEnumeration = MagicFormatFilteredHTML Then",
			"      wordDocument.WebOptions.Encoding = msoEncodingUTF8",
			"    End If",
			"",
			"    On Error Resume Next",
			"    If formatEnumeration = MagicFormatPDFA Then",
			"      wordDocument.ExportAsFixedFormat outputFile, WdExportFormatPDF, False, , , , , , , , , , , True",
			"    Else",
			"      wordDocument.SaveAs outputFile, formatEnumeration",
			"    End If",
			"",
			"    wordDocument.Close WdDoNotSaveChanges",
			"    If Err <> 0 Then",
			"      WScript.Quit -3",
			"    End If",
			"    On Error GoTo 0",
			"",
			"    WScript.Quit 2",
			"",
			"  Else",
			"",
			"    WScript.Quit -4",
			"",
			"  End If",
			"",
			"End Function",
			"",
			"Call ConvertFile( WScript.Arguments.Unnamed.Item(0), WScript.Arguments.Unnamed.Item(1),"
					+ " CInt(WScript.Arguments.Unnamed.Item(2)) )",
	};

	private ConversionScript() {}
}
