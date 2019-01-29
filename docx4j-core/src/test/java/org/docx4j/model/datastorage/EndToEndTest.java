package org.docx4j.model.datastorage;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.bind.Binder;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;
import org.w3c.dom.Node;

public class EndToEndTest {

	/**
	 * This tests:
	 * - repeat of a table row
	 * - conditions: simple conditional inclusion/exclusion of a paragraph
	 */
	@Test
	public void testInvoice() throws Exception {
		
		String inputfilepath = System.getProperty("user.dir") 
			+ "/src/test/resources/OpenDoPE/invoice.docx";
		
		/*
		 * 
			<invoice>
		        <customer>
		          <name>Joe Bloggs</name>
		        </customer>
		        <items>
		          <item>
		            <name>apples</name>
		            <price>$20</price>
		          </item>
		          <item>
		            <name>bananas</name>
		            <price>$30</price>
		          </item>
		          <item>
		            <name>cherries</name>
		            <price>$40</price>
		          </item>
		        </items>
		        <misc>
		          <includeBankDetails>true</includeBankDetails>
		          <wantspam>false</wantspam>
		        </misc>
	      </invoice>
		 */
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));				

//		System.out.println(
//		XmlUtils.marshaltoString(
//				wordMLPackage.getMainDocumentPart().getJaxbElement(), true) );
		
		// Process conditionals and repeats
		OpenDoPEHandler odh = new OpenDoPEHandler(wordMLPackage);
		wordMLPackage = odh.preprocess();

		// Since the JAXB binding stuff seems to remember
		// old artifacts, we'll first create a 'clean' object here
		// from something we've marshalled to.  
		org.w3c.dom.Document xmlNode = XmlUtils.marshaltoW3CDomDocument(
				wordMLPackage.getMainDocumentPart().getJaxbElement());				
		Binder<Node> binder = Context.jc.createBinder();
		Object jaxbElement =  (org.docx4j.wml.Document) binder.unmarshal(xmlNode);
		
		// Test (1)
		// Before processing, the table contains 2 rows 
		// (a header row, and 1 for content).
		// Now it should contain 4 rows.
				
		List<Object> list = XmlUtils.getJAXBNodesViaXPath(binder, jaxbElement, 
				"//w:tr", false );		
		int count = list.size();
		
		assertTrue("expected 4 rows but got " + count, count==4 );
		
		// 3 cells should contain 'apples', but I don't test that
		
		// Test (2)
		// Conditions
		// <includeBankDetails>true</includeBankDetails>
		list = XmlUtils.getJAXBNodesViaXPath(binder, jaxbElement, 
				"//w:r[w:t[contains(text(),'Bank')]]", 
				false );
		count = list.size();
		assertTrue("expected 1 run but got " + count, count==1 );

		// <wantspam>false</wantspam>
		list = XmlUtils.getJAXBNodesViaXPath(binder, jaxbElement, 
				"//w:r[w:t[contains(text(),'left out')]]", 
				false );
		count = list.size();
		assertTrue("expected 0 runs but got " + count, count==0 );
		
		// Apply the bindings
		AtomicInteger bookmarkId = odh.getNextBookmarkId();
		BindingHandler bh = new BindingHandler(wordMLPackage);
		bh.setStartingIdForNewBookmarks(bookmarkId);
		bh.applyBindings(wordMLPackage.getMainDocumentPart());
		

		// create a 'clean' object again ..		
		xmlNode = XmlUtils.marshaltoW3CDomDocument(
				wordMLPackage.getMainDocumentPart().getJaxbElement());				
		binder = Context.jc.createBinder();
		jaxbElement =  (org.docx4j.wml.Document) binder.unmarshal(xmlNode);
		
		// Test (3)
		list = XmlUtils.getJAXBNodesViaXPath(binder, jaxbElement, 
				"//w:r[w:t[contains(text(),'cherries')]]", 
				false );
		count = list.size();
		assertTrue("expected 1 runs but got " + count, count==1 );
		
	}

	
	/**
	 * This tests nested repeats.
	 * 
	 * The outer repeat is of a table.
	 */
	@Test
	public void testNestedRepeats() throws Exception {
		
		String inputfilepath = System.getProperty("user.dir") 
			+ "/src/test/resources/OpenDoPE/CountryRegions.xml";
		
		/*
		      <root xmlns="">
		        <countries>
		          <country name="c1">
		            <region name="c1 r1">
		              <area>A  c1 r1</area>
		              <population>P c1 r1 </population>
		            </region>
		          </country>
		          <country name="c2">
		            <region name="c2 r1">
		              <area>A c2 r1 </area>
		              <population> P c2 r1</population>
		            </region>
		            <region name="c2 r2">
		              <area>A c2 r2 </area>
		              <population>P c2 r2 </population>
		            </region>
		          </country>
		          <country name="c3">
		            <region name="c3 r1">
		              <area>A c3 r1 </area>
		              <population> P c3 r1</population>
		            </region>
		            <region name="c3 r2">
		              <area> A c3 r2</area>
		              <population>P c3 r2 </population>
		            </region>
		            <region name="c3 r3">
		              <area> A c3 r2</area>
		              <population>P c3 r2 </population>
		            </region>
		          </country>
		        </countries>
		      </root>
		 */
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));				

//		System.out.println(
//		XmlUtils.marshaltoString(
//				wordMLPackage.getMainDocumentPart().getJaxbElement(), true) );
		
		// Process conditionals and repeats
		OpenDoPEHandler odh = new OpenDoPEHandler(wordMLPackage);
		wordMLPackage = odh.preprocess();

		// Since the JAXB binding stuff seems to remember
		// old artifacts, we'll first create a 'clean' object here
		// from something we've marshalled to.  		
		org.w3c.dom.Document xmlNode = XmlUtils.marshaltoW3CDomDocument(
				wordMLPackage.getMainDocumentPart().getJaxbElement());				
		Binder<Node> binder = Context.jc.createBinder();
		Object jaxbElement =  (org.docx4j.wml.Document) binder.unmarshal(xmlNode);
		
		// Test (1)
		// After processing, we should have 3 tables
				
		List<Object> list = XmlUtils.getJAXBNodesViaXPath(binder, jaxbElement, 
				"//w:tbl", false );		
		int count = list.size();		
		assertTrue("expected 3 tables but got " + count, count==3 );
		
		// First table has 2 rows
		list = XmlUtils.getJAXBNodesViaXPath(binder, jaxbElement, 
				"/descendant::w:tbl[1]/*[local-name()='sdt' or local-name()='tr']" , false );	
//		for (Object o : list){			
//			System.out.println(XmlUtils.marshaltoString(o, true));			
//		}
		count = list.size();		
		assertTrue("expected 2 rows but got " + count, count==2 );		

		// Second table has 3 rows
		list = XmlUtils.getJAXBNodesViaXPath(binder, jaxbElement, 
				"/descendant::w:tbl[2]/*[local-name()='sdt' or local-name()='tr']" , false );	
		count = list.size();		
		assertTrue("expected 3 rows but got " + count, count==3 );		

		// Third table has 4 rows
		list = XmlUtils.getJAXBNodesViaXPath(binder, jaxbElement, 
				"/descendant::w:tbl[3]/*[local-name()='sdt' or local-name()='tr']" , false );	
		count = list.size();		
		assertTrue("expected 4 rows but got " + count, count==4 );		
		
		// Apply the bindings
		AtomicInteger bookmarkId = odh.getNextBookmarkId();
		BindingHandler bh = new BindingHandler(wordMLPackage);
		bh.setStartingIdForNewBookmarks(bookmarkId);
		bh.applyBindings(wordMLPackage.getMainDocumentPart());

		// create a 'clean' object again ..		
		xmlNode = XmlUtils.marshaltoW3CDomDocument(
				wordMLPackage.getMainDocumentPart().getJaxbElement());				
		binder = Context.jc.createBinder();
		jaxbElement =  (org.docx4j.wml.Document) binder.unmarshal(xmlNode);
		
		// Test (3)
		// Test that the last w:tc in the last table contains 'P c3 r3'
		/*
                      <w:tc>
                        <w:tcPr>
                          <w:tcW w:type="dxa" w:w="2660"/>
                        </w:tcPr>
                        <w:sdt>
                          <w:sdtPr>
                            <w:dataBinding w:storeItemID="{D42EB9BD-AFC8-4100-9E42-B62BFF6817F8}" w:xpath="/root/countries/country[3]/region[3]/population"/>
                            <w:showingPlcHdr/>
                            <w:tag w:val="od:xpath=x6_2_2"/>
                            <w:id w:val="1472231447"/>
                          </w:sdtPr>
                          <w:sdtContent>
                            <w:p>
                              <w:r>
                                <w:t xml:space="preserve">P c3 r3 </w:t>
                              </w:r>
                            </w:p>
                          </w:sdtContent>
                        </w:sdt>
                      </w:tc>
                    </w:tr>
                  </w:sdtContent>
                </w:sdt>
              </w:tbl>
              <w:p/>
            </w:sdtContent>
          </w:sdt>
		 */
		
		list = XmlUtils.getJAXBNodesViaXPath(binder, jaxbElement, 
				"/descendant::w:tbl[3]/w:sdt[last()]/w:sdtContent/w:tr/w:tc[last()]" , false );
		String content = XmlUtils.marshaltoString(list.get(0), true);
		assertTrue("expected content to contain 'P c3 r3' but got " + content, content.contains("P c3 r3") );
		
	}
	
}
