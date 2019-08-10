package org.docx4j.openpackaging.parts.WordprocessingML;

import static org.junit.Assert.*;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.wml.R;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StringToSoftReturnsTest {
	
	MainDocumentPart mdp;
	
	@Before
	public void setUp() throws Exception {
		
		mdp = new MainDocumentPart();
		
	}

	@Test
	public void linuxLineEndings1Test() throws InvalidFormatException {
		
		org.docx4j.wml.P p = mdp.createParagraphOfText("hello\n world");
		
		R r = (R)p.getContent().get(0);
//		System.out.println(XmlUtils.marshaltoString(r));
		assertTrue(r.getContent().size()==3); // counting the br
		
	}
	
	@Test
	public void linuxLineEndings2Test() throws InvalidFormatException {
		
		org.docx4j.wml.P p = mdp.createParagraphOfText("hello\n\n world");
		
		R r = (R)p.getContent().get(0);
//		System.out.println(XmlUtils.marshaltoString(r));
		assertTrue(r.getContent().size()==5); // counting the 2 br 
		
		/* the empty w:t should be OK: 
		 * 
			<w:r xmlns:cppr="http://schemas.microsoft.com/office/2006/coverPageProps" xmlns:dgm1611="http://schemas.microsoft.com/office/drawing/2016/11/diagram" xmlns:xdr="http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing" xmlns:w16se="http://schemas.microsoft.com/office/word/2015/wordml/symex" xmlns:wp15="http://schemas.microsoft.com/office/word/2012/wordprocessingDrawing" xmlns:wp14="http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing" xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture" xmlns:a1611="http://schemas.microsoft.com/office/drawing/2016/11/main" xmlns:a16svg="http://schemas.microsoft.com/office/drawing/2016/SVG/main" xmlns:am3d="http://schemas.microsoft.com/office/drawing/2017/model3d" xmlns:pvml="urn:schemas-microsoft-com:office:powerpoint" xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006" xmlns:dgm1612="http://schemas.microsoft.com/office/drawing/2016/12/diagram" xmlns:comp="http://schemas.openxmlformats.org/drawingml/2006/compatibility" xmlns:xvml="urn:schemas-microsoft-com:office:excel" xmlns:c173="http://schemas.microsoft.com/office/drawing/2017/03/chart" xmlns:anam3d="http://schemas.microsoft.com/office/drawing/2018/animation/model3d" xmlns:wpc="http://schemas.microsoft.com/office/word/2010/wordprocessingCanvas" xmlns:adec="http://schemas.microsoft.com/office/drawing/2017/decorative" xmlns:oda="http://opendope.org/answers" xmlns:a18hc="http://schemas.microsoft.com/office/drawing/2018/hyperlinkcolor" xmlns:odc="http://opendope.org/conditions" xmlns:wpg="http://schemas.microsoft.com/office/word/2010/wordprocessingGroup" xmlns:cdr="http://schemas.openxmlformats.org/drawingml/2006/chartDrawing" xmlns:odi="http://opendope.org/components" xmlns:msink="http://schemas.microsoft.com/ink/2010/main" xmlns:cdr14="http://schemas.microsoft.com/office/drawing/2010/chartDrawing" xmlns:iact="http://schemas.microsoft.com/office/powerpoint/2014/inkAction" xmlns:an18="http://schemas.microsoft.com/office/drawing/2018/animation" xmlns:wps="http://schemas.microsoft.com/office/word/2010/wordprocessingShape" xmlns:odq="http://opendope.org/questions" xmlns:w16cid="http://schemas.microsoft.com/office/word/2016/wordml/cid" xmlns:dsp="http://schemas.microsoft.com/office/drawing/2008/diagram" xmlns:odx="http://opendope.org/xpaths" xmlns:a15="http://schemas.microsoft.com/office/drawing/2012/main" xmlns:a14="http://schemas.microsoft.com/office/drawing/2010/main" xmlns:c15="http://schemas.microsoft.com/office/drawing/2012/chart" xmlns:a13cmd="http://schemas.microsoft.com/office/drawing/2013/main/command" xmlns:c14="http://schemas.microsoft.com/office/drawing/2007/8/2/chart" xmlns:a16="http://schemas.microsoft.com/office/drawing/2014/main" xmlns:odgm="http://opendope.org/SmartArt/DataHierarchy" xmlns:c16="http://schemas.microsoft.com/office/drawing/2014/chart" xmlns:dgm="http://schemas.openxmlformats.org/drawingml/2006/diagram" xmlns:thm15="http://schemas.microsoft.com/office/thememl/2012/main" xmlns:we="http://schemas.microsoft.com/office/webextensions/webextension/2010/11" xmlns:w10="urn:schemas-microsoft-com:office:word" xmlns:ns39="http://www.w3.org/2003/InkML" xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing" xmlns:sl="http://schemas.openxmlformats.org/schemaLibrary/2006/main" xmlns:ns38="http://www.w3.org/1998/Math/MathML" xmlns:w15="http://schemas.microsoft.com/office/word/2012/wordml" xmlns:w14="http://schemas.microsoft.com/office/word/2010/wordml" xmlns:dgm14="http://schemas.microsoft.com/office/drawing/2010/diagram" xmlns:c16ac="http://schemas.microsoft.com/office/drawing/2014/chart/ac" xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:b="http://schemas.openxmlformats.org/officeDocument/2006/bibliography" xmlns:c="http://schemas.openxmlformats.org/drawingml/2006/chart" xmlns:m="http://schemas.openxmlformats.org/officeDocument/2006/math" xmlns:wne="http://schemas.microsoft.com/office/word/2006/wordml" xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:cs="http://schemas.microsoft.com/office/drawing/2012/chartStyle" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:cx="http://schemas.microsoft.com/office/drawing/2014/chartex" xmlns:v="urn:schemas-microsoft-com:vml" xmlns:pic14="http://schemas.microsoft.com/office/drawing/2010/picture" xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main" xmlns:lc="http://schemas.openxmlformats.org/drawingml/2006/lockedCanvas" xmlns:wetp="http://schemas.microsoft.com/office/webextensions/taskpanes/2010/11">
			    <w:t>hello</w:t>
			    <w:br/>
			    <w:t></w:t>
			    <w:br/>
			    <w:t xml:space="preserve"> world</w:t>
			</w:r>
		 */
		
	}

	@Test
	public void linuxLineEndings3Test() throws InvalidFormatException {
		
		org.docx4j.wml.P p = mdp.createParagraphOfText("hello\nbeautiful\n world");
		
		R r = (R)p.getContent().get(0);
//		System.out.println(XmlUtils.marshaltoString(r));
		assertTrue(r.getContent().size()==5); // counting the br
		
	}
	
	@Test
	public void noNewlineTest() throws InvalidFormatException {
		
		org.docx4j.wml.P p = mdp.createParagraphOfText("hello world");
		
		R r = (R)p.getContent().get(0);
//		System.out.println(XmlUtils.marshaltoString(r));
		assertTrue(r.getContent().size()==1);
	}
	
	
	@Test
	public void slashRTest() throws InvalidFormatException {
		
		org.docx4j.wml.P p = mdp.createParagraphOfText("hello\r world");
		
		R r = (R)p.getContent().get(0);
//		System.out.println(XmlUtils.marshaltoString(r));
		assertTrue(r.getContent().size()==1);
		
	}

	@Test
	public void windowsLineEndingsTest() throws InvalidFormatException {
		
		org.docx4j.wml.P p = mdp.createParagraphOfText("hello\n\r world");
		
		R r = (R)p.getContent().get(0);
//		System.out.println(XmlUtils.marshaltoString(r));
		assertTrue(r.getContent().size()==3);
		
	}
	
}
