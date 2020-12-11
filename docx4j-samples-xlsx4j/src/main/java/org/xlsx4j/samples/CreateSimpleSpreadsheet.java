package org.xlsx4j.samples;

import java.io.File;
import java.io.IOException;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.docx4j.docProps.core.dc.elements.SimpleLiteral;
import org.docx4j.docProps.extended.Properties;
import org.docx4j.docProps.variantTypes.Vector;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.DocPropsCorePart;
import org.docx4j.openpackaging.parts.DocPropsExtendedPart;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.ThemePart;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.docx4j.utils.ResourceUtils;
import org.xlsx4j.jaxb.Context;
import org.xlsx4j.sml.CTRst;
import org.xlsx4j.sml.CTXstringWhitespace;
import org.xlsx4j.sml.Cell;
import org.xlsx4j.sml.Row;
import org.xlsx4j.sml.STCellType;
import org.xlsx4j.sml.SheetData;

public class CreateSimpleSpreadsheet {
	
	public static void main(String[] args) throws Exception {
	
		String outputfilepath = System.getProperty("user.dir") + "/OUT_CreateSimpleSpreadsheet.xlsx";
		
		SpreadsheetMLPackage pkg = SpreadsheetMLPackage.createPackage();
		
		// Add optional properties parts
		addCoreProps(pkg);
		addExtendedProps(pkg);

		// Add optional theme part
		addThemePart(pkg);
		
		WorksheetPart sheet = pkg.createWorksheetPart(new PartName("/xl/worksheets/sheet1.xml"), "Sheet1", 1);
		
		addContent(sheet);
				
		pkg.save(new File(outputfilepath));
						
		System.out.println("\n\n done .. " + outputfilepath);	
	}
	

	/* For example:
	 * 
		<cp:coreProperties xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dcterms="http://purl.org/dc/terms/" xmlns:dcmitype="http://purl.org/dc/dcmitype/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
		  <dc:creator>John Smith</dc:creator>
		  <cp:lastModifiedBy>Realtor AI</cp:lastModifiedBy>
		  <dcterms:created xsi:type="dcterms:W3CDTF">2019-02-11T09:25:48Z</dcterms:created>
		  <dcterms:modified xsi:type="dcterms:W3CDTF">2019-02-11T09:26:37Z</dcterms:modified>
		</cp:coreProperties>
	 */
	private static void addCoreProps(SpreadsheetMLPackage pkg) {
		
		pkg.addDocPropsCorePart();
		DocPropsCorePart corePart = pkg.getDocPropsCorePart();

		org.docx4j.docProps.core.CoreProperties coreProps = corePart.getJaxbElement();
		
		org.docx4j.docProps.core.dc.elements.ObjectFactory dcElFactory = new org.docx4j.docProps.core.dc.elements.ObjectFactory();
		
		SimpleLiteral creator = dcElFactory.createSimpleLiteral();
		coreProps.setCreator(creator);
		creator.getContent().add("John Smith");
		
	}

	
	/* app.xml, for example:
	 * 
		<Properties xmlns="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties" xmlns:vt="http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes">
		  <Application>Microsoft Excel</Application>
		  <DocSecurity>0</DocSecurity>
		  <ScaleCrop>false</ScaleCrop>
		  <HeadingPairs>
		    <vt:vector size="2" baseType="variant">
		      <vt:variant>
		        <vt:lpstr>Worksheets</vt:lpstr>
		      </vt:variant>
		      <vt:variant>
		        <vt:i4>1</vt:i4>
		      </vt:variant>
		    </vt:vector>
		  </HeadingPairs>
		  <TitlesOfParts>
		    <vt:vector size="1" baseType="lpstr">
		      <vt:lpstr>Sheet1</vt:lpstr>
		    </vt:vector>
		  </TitlesOfParts>
		  <Company></Company>
		  <LinksUpToDate>false</LinksUpToDate>
		  <SharedDoc>false</SharedDoc>
		  <HyperlinksChanged>false</HyperlinksChanged>
		  <AppVersion>16.0300</AppVersion>
		</Properties>
	 */
	private static void addExtendedProps(SpreadsheetMLPackage pkg) {
		
		pkg.addDocPropsExtendedPart();
		DocPropsExtendedPart extendedPart = pkg.getDocPropsExtendedPart();
		
		org.docx4j.docProps.extended.Properties extendedProps = extendedPart.getJaxbElement(); 
		
		// Let's set a couple of those values
		
		extendedProps.setApplication("xlsx4j");

		org.docx4j.docProps.extended.ObjectFactory factoryExtended = new org.docx4j.docProps.extended.ObjectFactory(); 
		org.docx4j.docProps.variantTypes.ObjectFactory factoryVariantTypes = new org.docx4j.docProps.variantTypes.ObjectFactory(); 
		
		// Title of Parts
		Properties.TitlesOfParts titleOfParts = new Properties.TitlesOfParts();		
		extendedProps.setTitlesOfParts(titleOfParts);
		
		Vector vector = factoryVariantTypes.createVector();		
		titleOfParts.setVector(vector);
		vector.setSize(1);
		vector.setBaseType("lpstr");
		
		JAXBElement<String> lpstr = factoryVariantTypes.createLpstr("Sheet1");
		vector.getVariantOrI1OrI2().add(lpstr);
				
	}
	
	private static void addContent(WorksheetPart sheet) {
		
		// Minimal content already present
		SheetData sheetData = sheet.getJaxbElement().getSheetData();
				
		// Now add
		Row row = Context.getsmlObjectFactory().createRow();
		//row.setR((long) 1);  // optional
		
		Cell cell = Context.getsmlObjectFactory().createCell();
		cell.setV("1234");
		cell.setR("A1");  // Apple Numbers needs this, or cell content won't show 
		row.getC().add(cell);
		
		Cell cell2 = createCell("hello world!");
		row.getC().add(cell2);
		cell2.setR("B1"); // be careful the numeral matches the row correctly, or Excel will complain
		
		sheetData.getRow().add(row);
	}
	
	/**
		// Note: if you are trying to add characters, not a number,
		// the easiest approach is to use inline strings (as opposed to the shared string table).
		// See http://openxmldeveloper.org/blog/b/openxmldeveloper/archive/2011/11/22/screen-cast-write-simpler-spreadsheetml-when-generating-spreadsheets.aspx
		// and http://www.docx4java.org/forums/xlsx-java-f15/cells-with-character-values-t874.html
	 */
	private static Cell createCell(String content) {

		Cell cell = Context.getsmlObjectFactory().createCell();
		
		CTXstringWhitespace ctx = Context.getsmlObjectFactory().createCTXstringWhitespace();
		ctx.setValue(content);
		
		CTRst ctrst = new CTRst();
		ctrst.setT(ctx);

		cell.setT(STCellType.INLINE_STR);
		cell.setIs(ctrst); // add ctrst as inline string
		
		return cell;
		
	}

	private static void addThemePart(SpreadsheetMLPackage pkg) throws InvalidFormatException, JAXBException, IOException   {
		
		ThemePart themePart = new ThemePart(new PartName("/xl/theme/theme1.xml")); // Excel doesn't like it being called "/word/theme/theme1.xml"
		pkg.getWorkbookPart().addTargetPart(themePart);
		
		themePart.unmarshal(ResourceUtils.getResource("org/xlsx4j/samples/theme1.xml"));
		
	}
}
