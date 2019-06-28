package org.docx4j.anon;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.docx4j.docProps.coverPageProps.CoverPageProperties;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.DocPropsCoverPagePart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;

public class PartsAnalyzer {
	
	public static HashSet<Part> identifyUnsafeParts(Set<Entry<PartName, Part>> parts) throws Docx4JException {
		
		HashSet<Part> unsafeParts = new HashSet<Part>(); 
		HashSet<Part> unsafeButRemovedParts = new HashSet<Part>(); 
		
		
		for (Entry<PartName, Part> entry : parts) {

			Part p = entry.getValue(); 
			
			if ( p instanceof org.docx4j.openpackaging.parts.WordprocessingML.BibliographyPart) {
				// assumed ok	
				
			} else if (p instanceof org.docx4j.openpackaging.parts.ActiveXControlXmlPart) {
				unsafeParts.add(p);
				
			} else if ( p instanceof org.docx4j.openpackaging.parts.CustomXmlDataStoragePart
						|| p instanceof org.docx4j.openpackaging.parts.opendope.JaxbCustomXmlDataStoragePart						
						|| p instanceof org.docx4j.openpackaging.parts.opendope.ComponentsPart
						|| p instanceof org.docx4j.openpackaging.parts.opendope.ConditionsPart
						|| p instanceof org.docx4j.openpackaging.parts.opendope.QuestionsPart
						|| p instanceof org.docx4j.openpackaging.parts.opendope.StandardisedAnswersPart
						|| p instanceof org.docx4j.openpackaging.parts.opendope.XPathsPart) {

				unsafeButRemovedParts.add(p);
				
			} else if (p.getPartName().getName().startsWith("/word/glossary")) {

				// assume
				unsafeButRemovedParts.add(p);				

			} else if (p instanceof org.docx4j.openpackaging.parts.DocPropsCoverPagePart) {
				
				// Expedient to handle part this here
				CoverPageProperties cpp = ((DocPropsCoverPagePart)p).getContents();
				if (cpp!=null) {
					cpp.setAbstract("some abstract");
					cpp.setCompanyAddress("some address");
					cpp.setCompanyEmail("foo@bar.com");
					cpp.setCompanyFax("12 123 1234");
					cpp.setCompanyPhone("12 123 1234");
				}
				
			} else if ( p instanceof org.docx4j.openpackaging.parts.DocPropsCorePart
						|| p instanceof org.docx4j.openpackaging.parts.DocPropsCustomPart
						|| p instanceof org.docx4j.openpackaging.parts.DocPropsExtendedPart ) {
				
				// Made safe elsewhere
			} else if ( p instanceof org.docx4j.openpackaging.parts.DrawingML.Chart
						|| p instanceof org.docx4j.openpackaging.parts.DrawingML.ChartShapePart
						|| p instanceof org.docx4j.openpackaging.parts.DrawingML.DiagramColorsPart
						|| p instanceof org.docx4j.openpackaging.parts.DrawingML.DiagramDataPart
						|| p instanceof org.docx4j.openpackaging.parts.DrawingML.DiagramDrawingPart
						|| p instanceof org.docx4j.openpackaging.parts.DrawingML.DiagramLayoutHeaderPart
						|| p instanceof org.docx4j.openpackaging.parts.DrawingML.DiagramLayoutPart
						|| p instanceof org.docx4j.openpackaging.parts.DrawingML.DiagramStylePart
						|| p instanceof org.docx4j.openpackaging.parts.DrawingML.Drawing
						|| p instanceof org.docx4j.openpackaging.parts.DrawingML.JaxbDmlPart) {

				unsafeParts.add(p);
				
			} else if (p instanceof org.docx4j.openpackaging.parts.DrawingML.ThemeOverridePart
					|| p instanceof org.docx4j.openpackaging.parts.ThemePart
					){
				
			} else if ( p instanceof org.docx4j.openpackaging.parts.PresentationML.CommentAuthorsPart
						|| p instanceof org.docx4j.openpackaging.parts.PresentationML.CommentsPart
						|| p instanceof org.docx4j.openpackaging.parts.PresentationML.FontDataPart
						|| p instanceof org.docx4j.openpackaging.parts.PresentationML.HandoutMasterPart
						|| p instanceof org.docx4j.openpackaging.parts.PresentationML.JaxbPmlPart
						|| p instanceof org.docx4j.openpackaging.parts.PresentationML.MainPresentationPart
						|| p instanceof org.docx4j.openpackaging.parts.PresentationML.NotesMasterPart
						|| p instanceof org.docx4j.openpackaging.parts.PresentationML.NotesSlidePart
						|| p instanceof org.docx4j.openpackaging.parts.PresentationML.PresentationPropertiesPart
						|| p instanceof org.docx4j.openpackaging.parts.PresentationML.SlideLayoutPart
						|| p instanceof org.docx4j.openpackaging.parts.PresentationML.SlideMasterPart
						|| p instanceof org.docx4j.openpackaging.parts.PresentationML.SlidePart
						|| p instanceof org.docx4j.openpackaging.parts.PresentationML.TableStylesPart
						|| p instanceof org.docx4j.openpackaging.parts.PresentationML.TagsPart
						|| p instanceof org.docx4j.openpackaging.parts.PresentationML.ViewPropertiesPart) {
				
				// Unexpected in docx
				unsafeParts.add(p);
				
			} else if ( p instanceof org.docx4j.openpackaging.parts.SpreadsheetML.CalcChain
						|| p instanceof org.docx4j.openpackaging.parts.SpreadsheetML.CommentsPart
						|| p instanceof org.docx4j.openpackaging.parts.SpreadsheetML.ConnectionsPart
						|| p instanceof org.docx4j.openpackaging.parts.SpreadsheetML.ExternalLinkPart
						|| p instanceof org.docx4j.openpackaging.parts.SpreadsheetML.JaxbSmlPart
						|| p instanceof org.docx4j.openpackaging.parts.SpreadsheetML.PivotCacheDefinition
						|| p instanceof org.docx4j.openpackaging.parts.SpreadsheetML.PivotCacheRecords
						|| p instanceof org.docx4j.openpackaging.parts.SpreadsheetML.PivotTable
						|| p instanceof org.docx4j.openpackaging.parts.SpreadsheetML.PrinterSettings
						|| p instanceof org.docx4j.openpackaging.parts.SpreadsheetML.QueryTablePart
						|| p instanceof org.docx4j.openpackaging.parts.SpreadsheetML.SharedStrings
						|| p instanceof org.docx4j.openpackaging.parts.SpreadsheetML.Styles
						|| p instanceof org.docx4j.openpackaging.parts.SpreadsheetML.TablePart
						|| p instanceof org.docx4j.openpackaging.parts.SpreadsheetML.WorkbookPart
						|| p instanceof org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart) {
				
				// Unexpected in docx
				unsafeParts.add(p);
				
			} else if ( p instanceof org.docx4j.openpackaging.parts.VMLBinaryPart
						|| p instanceof org.docx4j.openpackaging.parts.VMLPart) {
				
				unsafeParts.add(p);
				
			} else if (p instanceof org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart
						|| p instanceof org.docx4j.openpackaging.parts.WordprocessingML.CommentsExtendedPart
						|| p instanceof org.docx4j.openpackaging.parts.CustomXmlDataStoragePropertiesPart
						|| p instanceof org.docx4j.openpackaging.parts.WordprocessingML.FooterPart
						|| p instanceof org.docx4j.openpackaging.parts.WordprocessingML.FootnotesPart
						|| p instanceof org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart
						|| p instanceof org.docx4j.openpackaging.parts.WordprocessingML.ImageBmpPart
						|| p instanceof org.docx4j.openpackaging.parts.WordprocessingML.ImageGifPart
						|| p instanceof org.docx4j.openpackaging.parts.WordprocessingML.ImageJpegPart
						|| p instanceof org.docx4j.openpackaging.parts.WordprocessingML.ImagePngPart
						|| p instanceof org.docx4j.openpackaging.parts.WordprocessingML.ImageTiffPart
						|| p instanceof org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart
						|| p instanceof org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart // ?
						|| p instanceof org.docx4j.openpackaging.parts.WordprocessingML.EndnotesPart
						|| p instanceof org.docx4j.openpackaging.parts.WordprocessingML.FontTablePart						
						|| p instanceof org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart
						|| p instanceof org.docx4j.openpackaging.parts.WordprocessingML.ObfuscatedFontPart
						|| p instanceof org.docx4j.openpackaging.parts.TrueTypeFontPart  // unless the font name give something away
						|| p instanceof org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart // unless the style names give something away
						|| p.getPartName().getName().equals("/word/stylesWithEffects.xml")
						|| p instanceof org.docx4j.openpackaging.parts.WordprocessingML.WebSettingsPart) {
				
				// Should be ok
				
			} else if (p instanceof org.docx4j.openpackaging.parts.WordprocessingML.ImageEpsPart
						|| p instanceof org.docx4j.openpackaging.parts.WordprocessingML.AlternativeFormatInputPart
						|| p instanceof org.docx4j.openpackaging.parts.WordprocessingML.DocumentPart
						|| p instanceof org.docx4j.openpackaging.parts.WordprocessingML.EmbeddedPackagePart
						|| p instanceof org.docx4j.openpackaging.parts.WordprocessingML.GlossaryDocumentPart
						|| p instanceof org.docx4j.openpackaging.parts.WordprocessingML.MetafileEmfPart
						|| p instanceof org.docx4j.openpackaging.parts.WordprocessingML.MetafileWmfPart
						|| p instanceof org.docx4j.openpackaging.parts.WordprocessingML.MetafilePart
						|| p instanceof org.docx4j.openpackaging.parts.WordprocessingML.OleObjectBinaryPart
						|| p instanceof org.docx4j.openpackaging.parts.WordprocessingML.PeoplePart
						|| p instanceof org.docx4j.openpackaging.parts.WordprocessingML.VbaDataPart
						|| p instanceof org.docx4j.openpackaging.parts.WordprocessingML.VbaProjectBinaryPart
						|| p instanceof org.docx4j.openpackaging.parts.WordprocessingML.VbaProjectSignatureBin) {
				
				unsafeParts.add(p);
				
			} else if (p instanceof org.docx4j.openpackaging.parts.XmlPart
						|| p instanceof org.docx4j.openpackaging.parts.DefaultXmlPart
						
						|| p instanceof org.docx4j.openpackaging.parts.JaxbXmlPart
						|| p instanceof org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart) {

				unsafeParts.add(p);
				
						// Xml Dig Sig parts?
						
			} else {
				
				// Anything not specifically handled is potentially unsafe
				unsafeParts.add(p);
				
			}
				
		}
		return unsafeParts;
		
	}

}
