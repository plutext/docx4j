package org.docx4j.fidelity.golden;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Which VBS documents4j has Word run - shared by every tool here that drives Word, because
 * {@link WordGoldenRunner} and {@link ResaveInvariance} must not drift apart on it.
 *
 * <p>It decides whether Word updates a document's fields on the way through, which is not a
 * detail. Measured on the corpus: a document rendered with the field update reproduces its
 * golden <em>exactly</em>, and the same document rendered without it differs by hundreds of
 * lines over almost every page. So the goldens already cut were cut with the update ON, and
 * every field-bearing document in the corpora is being scored against a reference holding
 * Word's recomputed field text - which docx4j, rendering the stored result the docx carries,
 * never produces. A set cut with the update off has both sides showing the stored result, and
 * the comparison is then layout alone.</p>
 *
 * <p>That is a property of a golden set rather than of a run, so a set has to say which it is:
 * {@link #mode()} and {@link #path()} go into the manifest written beside it.</p>
 *
 * <p>The resolution has three parts that are each easy to get wrong, which is the other reason
 * it lives in one place:</p>
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
 *     {@code docx4j.properties} would put the configured script back. documents4j's own
 *     bundled script is unpacked and pointed at instead.</li>
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

	/** The script Word will run, absolute, or documents4j's own where none was named. */
	private static String path = "documents4j default (bundled)";

	/** The system property that decides the mode. */
	static final String MODE_PROPERTY = "fidelity.updateFields";

	static String description() {
		return description;
	}

	static String mode() {
		return mode;
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
	 *     back to {@code docx4j.properties} and put the configured script back; so
	 *     documents4j's own bundled {@code word_convert.vbs} is unpacked from the classpath
	 *     into {@code dir} and pointed at explicitly. That script opens, saves and closes,
	 *     and touches no field.</li>
	 * <li>unset - whatever the machine is configured for is left alone, and the resolved
	 *     script is printed so the run says what it did.</li>
	 * </ul>
	 */
	static void configure(File dir) throws IOException {
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
			System.out.println("field update: not specified; " + description);
			System.out.println("  -Dfidelity.updateFields=true|false to decide it here");
			return;
		}
		if (Boolean.parseBoolean(want)) {
			String named = System.getProperty("fidelity.fieldUpdateScript");
			File script;
			if (named != null) {
				script = new File(named);
				if (!script.isFile()) throw new IOException("no such script: " + script);
			} else {
				script = new File(dir, "word_convert-updatefields.vbs");
				Files.write(script.toPath(), UPDATING_SCRIPT.getBytes(StandardCharsets.US_ASCII));
			}
			System.setProperty(SCRIPT_PROPERTY, script.getAbsolutePath());
			mode = "on";
			path = script.getAbsolutePath();
			description = "ON, via " + path;
		} else {
			File script = new File(dir, "word_convert-nofields.vbs");
			try (InputStream in = ConversionScript.class.getClassLoader().getResourceAsStream("word_convert.vbs")) {
				if (in == null) {
					throw new IOException("documents4j's word_convert.vbs is not on the classpath;"
							+ " name a no-op script with -Dfidelity.fieldUpdateScript= instead");
				}
				Files.copy(in, script.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
			System.setProperty(SCRIPT_PROPERTY, script.getAbsolutePath());
			mode = "off";
			path = script.getAbsolutePath();
			description = "OFF, via documents4j's own script at " + path;
		}
		System.out.println("field update: " + description);
	}

	/**
	 * documents4j's default conversion script with a field update added: every story range's
	 * fields (the body, but also the headers, footers and footnotes, which
	 * {@code Document.Fields} alone does not reach) and every table of contents, with
	 * {@code Err} cleared afterwards so that a document holding neither is still converted.
	 * CRLF, because it is handed to {@code cscript}.
	 */
	private static final String UPDATING_SCRIPT = String.join("\r\n",
			"' generated by org.docx4j.fidelity.golden.ConversionScript - documents4j's default",
			"' word_convert.vbs, plus a field update, so that the effect of the field update can be",
			"' measured by cutting the same document with and without it.",
			"Const WdDoNotSaveChanges = 0",
			"Const WdExportFormatPDF = 17",
			"Const MagicFormatPDFA = 999",
			"Const MagicFormatFilteredHTML = 10",
			"Const msoEncodingUTF8 = 65001",
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
			"");

	private ConversionScript() {}
}
