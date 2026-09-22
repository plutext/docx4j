package org.docx4j.anon;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.DefaultXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;

/**
 * The table the anonymiser consults for every part: what class of thing it is,
 * and so what the walk does with it (CR-019: "PartsAnalyzer's classification
 * becomes the table the walk consults: per part class, one of scramble / clear /
 * replace / remove / keep-unsafe").
 * <p>
 * Anything not named here is {@link Treatment#UNSCRUBBABLE}: a part the tool
 * cannot make clean is removed (STRICT) or kept and reported (KEEP), never
 * silently passed through.
 */
public class PartsAnalyzer {

	public enum Treatment {
		/** structure only, nothing to do (a theme's colours, a chart style) */
		SAFE,
		/** a JAXB part the walk scrambles and scrubs */
		SCRUB,
		/** docProps and the like: cleared field by field */
		METADATA,
		/** an image: placeholder pixels of the same class */
		REPLACE_IMAGE,
		/** removed in either mode: custom XML, glossary, custom properties, thumbnails, signatures, labels */
		REMOVE,
		/** cannot be made clean (OLE, altChunk, VBA, embedded fonts, audio and video, unknown parts): removed in STRICT, kept and reported in KEEP */
		UNSCRUBBABLE
	}

	public static Treatment classify(Part p) {

		String name = p.getPartName().getName();

		// ---- removed whatever the mode (the module has always removed these)
		if (p instanceof org.docx4j.openpackaging.parts.CustomXmlDataStoragePart
				|| p instanceof org.docx4j.openpackaging.parts.CustomXmlDataStoragePropertiesPart
				|| p instanceof org.docx4j.openpackaging.parts.opendope.JaxbCustomXmlDataStoragePart
				|| p instanceof org.docx4j.openpackaging.parts.opendope.ComponentsPart
				|| p instanceof org.docx4j.openpackaging.parts.opendope.ConditionsPart
				|| p instanceof org.docx4j.openpackaging.parts.opendope.QuestionsPart
				|| p instanceof org.docx4j.openpackaging.parts.opendope.StandardisedAnswersPart
				|| p instanceof org.docx4j.openpackaging.parts.opendope.XPathsPart
				|| p instanceof org.docx4j.openpackaging.parts.WordprocessingML.BibliographyPart // b:Sources: names and titles
				|| p instanceof org.docx4j.openpackaging.parts.WordprocessingML.GlossaryDocumentPart
				|| p instanceof org.docx4j.openpackaging.parts.DocPropsCustomPart
				|| name.startsWith("/word/glossary")
				|| name.startsWith("/docProps/thumbnail.")
				|| name.startsWith("/_xmlsignatures/") // a signature over the original content, naming the signer
				|| name.startsWith("/docMetadata/") // sensitivity label: tenant and label ids
				|| name.equals("/word/stylesWithEffects.xml")) {
			return Treatment.REMOVE;
		}

		// ---- metadata, cleared field by field
		if (p instanceof org.docx4j.openpackaging.parts.DocPropsCorePart
				|| p instanceof org.docx4j.openpackaging.parts.DocPropsExtendedPart
				|| p instanceof org.docx4j.openpackaging.parts.DocPropsCoverPagePart) {
			return Treatment.METADATA;
		}

		// ---- images: placeholder pixels
		if (p instanceof org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage
				|| p instanceof org.docx4j.openpackaging.parts.WordprocessingML.MetafilePart) {
			return Treatment.REPLACE_IMAGE;
		}
		if (p instanceof DefaultXmlPart && isSvg(p)) {
			return Treatment.REPLACE_IMAGE;
		}

		// ---- the 2018 PowerPoint comments and their authors, and Excel's threaded comments and
		// persons: not bound, so a DefaultXmlPart, scrubbed as DOM (ModernCommentsScrubber)
		if (p instanceof DefaultXmlPart && isModernComments(p)) {
			return Treatment.SCRUB;
		}

		// ---- SpreadsheetML (CR-019 phase 3): removed whatever the mode - a pivot cache is a copy
		// of its source data, and a pivot table, slicer or timeline without its cache is a
		// repair prompt, so they go together (the pivot's cells stay as values); the data
		// model is data; an add-in's custom data and a survey are text
		if (p instanceof org.docx4j.openpackaging.parts.SpreadsheetML.PivotCacheDefinition
				|| p instanceof org.docx4j.openpackaging.parts.SpreadsheetML.PivotCacheRecords
				|| p instanceof org.docx4j.openpackaging.parts.SpreadsheetML.PivotTable
				|| p instanceof org.docx4j.openpackaging.parts.SpreadsheetML.SlicerCachePart
				|| p instanceof org.docx4j.openpackaging.parts.SpreadsheetML.SlicersPart
				|| p instanceof org.docx4j.openpackaging.parts.SpreadsheetML.TimelineCachePart
				|| p instanceof org.docx4j.openpackaging.parts.SpreadsheetML.TimelinesPart
				|| p instanceof org.docx4j.openpackaging.parts.SpreadsheetML.DataModelPart
				|| p instanceof org.docx4j.openpackaging.parts.SpreadsheetML.CustomDataPart
				|| p instanceof org.docx4j.openpackaging.parts.SpreadsheetML.CustomDataPropertiesPart
				|| p instanceof org.docx4j.openpackaging.parts.SpreadsheetML.SurveyPart) {
			return Treatment.REMOVE;
		}

		// ---- structure
		if (p instanceof org.docx4j.openpackaging.parts.DrawingML.ChartStylePart
				|| p instanceof org.docx4j.openpackaging.parts.DrawingML.ChartColorStylePart
				|| p instanceof org.docx4j.openpackaging.parts.relationships.RelationshipsPart) {
			return Treatment.SAFE;
		}

		// ---- JAXB parts the walk covers
		if (p instanceof org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart
				|| p instanceof org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart
				|| p instanceof org.docx4j.openpackaging.parts.WordprocessingML.FooterPart
				|| p instanceof org.docx4j.openpackaging.parts.WordprocessingML.FootnotesPart
				|| p instanceof org.docx4j.openpackaging.parts.WordprocessingML.EndnotesPart
				|| p instanceof org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart
				|| p instanceof org.docx4j.openpackaging.parts.WordprocessingML.CommentsExtendedPart
				|| p instanceof org.docx4j.openpackaging.parts.WordprocessingML.CommentsIdsPart
				|| p instanceof org.docx4j.openpackaging.parts.WordprocessingML.CommentsExtensiblePart
				|| p instanceof org.docx4j.openpackaging.parts.WordprocessingML.PeoplePart
				|| p instanceof org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart
				|| p instanceof org.docx4j.openpackaging.parts.WordprocessingML.WebSettingsPart
				|| p instanceof org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart
				|| p instanceof org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart
				|| p instanceof org.docx4j.openpackaging.parts.WordprocessingML.FontTablePart
				|| p instanceof org.docx4j.openpackaging.parts.ThemePart
				|| p instanceof org.docx4j.openpackaging.parts.DrawingML.ThemeOverridePart
				|| p instanceof org.docx4j.openpackaging.parts.DrawingML.Chart
				|| p instanceof org.docx4j.openpackaging.parts.DrawingML.ChartExSpacePart
				|| p instanceof org.docx4j.openpackaging.parts.DrawingML.ChartShapePart
				|| p instanceof org.docx4j.openpackaging.parts.DrawingML.DiagramDataPart
				|| p instanceof org.docx4j.openpackaging.parts.DrawingML.DiagramDrawingPart
				|| p instanceof org.docx4j.openpackaging.parts.DrawingML.DiagramLayoutPart
				|| p instanceof org.docx4j.openpackaging.parts.DrawingML.DiagramLayoutHeaderPart
				|| p instanceof org.docx4j.openpackaging.parts.DrawingML.DiagramColorsPart
				|| p instanceof org.docx4j.openpackaging.parts.DrawingML.DiagramStylePart
				|| p instanceof org.docx4j.openpackaging.parts.DrawingML.Drawing) {
			return Treatment.SCRUB;
		}

		// ---- PresentationML (CR-019 phase 2): the slides and their masters, layouts and
		// notes (a:t in every a:p), the presentation (section names, custom shows, the
		// modify verifier, the embedded-font list), legacy comments and their authors, tags,
		// table styles (names)
		if (p instanceof org.docx4j.openpackaging.parts.PresentationML.MainPresentationPart
				|| p instanceof org.docx4j.openpackaging.parts.PresentationML.SlidePart
				|| p instanceof org.docx4j.openpackaging.parts.PresentationML.SlideLayoutPart
				|| p instanceof org.docx4j.openpackaging.parts.PresentationML.SlideMasterPart
				|| p instanceof org.docx4j.openpackaging.parts.PresentationML.NotesSlidePart
				|| p instanceof org.docx4j.openpackaging.parts.PresentationML.NotesMasterPart
				|| p instanceof org.docx4j.openpackaging.parts.PresentationML.HandoutMasterPart
				|| p instanceof org.docx4j.openpackaging.parts.PresentationML.CommentsPart
				|| p instanceof org.docx4j.openpackaging.parts.PresentationML.CommentAuthorsPart
				|| p instanceof org.docx4j.openpackaging.parts.PresentationML.TagsPart
				|| p instanceof org.docx4j.openpackaging.parts.PresentationML.TableStylesPart) {
			return Treatment.SCRUB;
		}
		// structure, but walked (presProps carries the HTML publish target and the show's custom-show id)
		if (p instanceof org.docx4j.openpackaging.parts.PresentationML.PresentationPropertiesPart
				|| p instanceof org.docx4j.openpackaging.parts.PresentationML.ViewPropertiesPart) {
			return Treatment.SAFE;
		}

		// ---- SpreadsheetML: the workbook (sheet and defined names, protection, file sharing),
		// the sheets (cells, formulas, headers, validations, filters), shared strings, styles
		// (custom style names), tables, comments and their authors, connections and query
		// tables (connection strings, commands, URLs), external links (cached values, sheet
		// names), form-control properties (linked-cell formulas, list items); VML (comment
		// and control shapes)
		if (p instanceof org.docx4j.openpackaging.parts.SpreadsheetML.WorkbookPart
				|| p instanceof org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart
				|| p instanceof org.docx4j.openpackaging.parts.SpreadsheetML.ChartsheetPart
				|| p instanceof org.docx4j.openpackaging.parts.SpreadsheetML.SharedStrings
				|| p instanceof org.docx4j.openpackaging.parts.SpreadsheetML.Styles
				|| p instanceof org.docx4j.openpackaging.parts.SpreadsheetML.TablePart
				|| p instanceof org.docx4j.openpackaging.parts.SpreadsheetML.CommentsPart
				|| p instanceof org.docx4j.openpackaging.parts.SpreadsheetML.ConnectionsPart
				|| p instanceof org.docx4j.openpackaging.parts.SpreadsheetML.QueryTablePart
				|| p instanceof org.docx4j.openpackaging.parts.SpreadsheetML.ExternalLinkPart
				|| p instanceof org.docx4j.openpackaging.parts.SpreadsheetML.ControlPropertiesPart
				|| p instanceof org.docx4j.openpackaging.parts.VMLPart) {
			return Treatment.SCRUB;
		}
		if (p instanceof org.docx4j.openpackaging.parts.SpreadsheetML.CalcChain) {
			return Treatment.SAFE; // cell references only
		}

		// ---- everything else: OLE and embedded packages, altChunk, VBA, embedded fonts
		// (FontDataPart included), printer settings, ActiveX, ink, web extensions, audio and
		// video, unknown XML and binaries (a workbook's metadata and rich-value parts among them)
		return Treatment.UNSCRUBBABLE;
	}

	/**
	 * The 2018 comments part (p188:cmLst) or its authors part (p188:authorLst), or Excel's
	 * threaded comments or persons part, by content type
	 */
	static boolean isModernComments(Part p) {
		try {
			String ct = p.getContentType();
			return ContentTypes.PRESENTATIONML_MODERN_COMMENTS.equals(ct)
					|| ContentTypes.PRESENTATIONML_MODERN_COMMENT_AUTHORS.equals(ct)
					|| ContentTypes.SPREADSHEETML_THREADED_COMMENTS.equals(ct)
					|| ContentTypes.SPREADSHEETML_PERSONS.equals(ct);
		} catch (Exception e) {
			return false;
		}
	}

	static boolean isSvg(Part p) {
		try {
			String ct = p.getContentType();
			if (ct != null) return ct.equals(ContentTypes.IMAGE_SVG);
		} catch (Exception e) {
			// fall through
		}
		return p.getPartName().getName().toLowerCase().endsWith(".svg");
	}

	/**
	 * The parts the tool cannot make clean.
	 *
	 * @deprecated since 17.2.0: {@link Anonymize} consults {@link #classify(Part)}; this
	 *             remains for callers of the older API and no longer touches the cover page part
	 */
	@Deprecated
	public static HashSet<Part> identifyUnsafeParts(Set<Entry<PartName, Part>> parts) throws Docx4JException {

		HashSet<Part> unsafeParts = new HashSet<Part>();
		for (Entry<PartName, Part> entry : parts) {
			if (classify(entry.getValue()) == Treatment.UNSCRUBBABLE) {
				unsafeParts.add(entry.getValue());
			}
		}
		return unsafeParts;
	}

}
