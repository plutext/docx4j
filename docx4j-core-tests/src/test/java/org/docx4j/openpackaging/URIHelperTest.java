/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== 

*
*  was package org.apache.poi.openxml4j.opc.TestPackagingURIHelper
*  
*  Modified by Jason Harrop 2008 02 16.
*
*
*/
package org.docx4j.openpackaging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;

public class URIHelperTest {

	private URI uriDocInSuperFolder;
	private URI uriImageInSubFolder;
	private URI uriDocInADifferentFolder;
	private URI uriRootPath;

	@Before
	public void setUp() {
		try {
			uriDocInSuperFolder = new URI("/word/document.xml");
			uriImageInSubFolder = new URI("/word/media/image1.gif");
			uriDocInADifferentFolder = new URI("/customXml/item1.xml");
			uriRootPath = new URI("/");
		} catch (Exception e) {	
			fail("URI violates RFC&nbsp;2396 or is null.");			
		}
		
	}

	@Test
	public void testRelativizeURIFromSuperFolderToSubFolder() throws Exception {
		// Document to image is down a directory
		final URI relativizedURI = URIHelper.relativizeURI(uriDocInSuperFolder, uriImageInSubFolder);
		assertEquals("media/image1.gif", relativizedURI.getPath());
	}

	@Test
	public void testRelativizeURIFromSubFolderToSuperFolder() throws Exception {
		// Image to document is up a directory
		final URI relativizedURI = URIHelper.relativizeURI(uriImageInSubFolder, uriDocInSuperFolder);
		assertEquals("../document.xml", relativizedURI.getPath());
	}

	@Test
	public void testRelativizeURIDocInATotalyDifferentFolder() throws Exception {
		// Document and CustomXML parts totally different [Julien C.]
		final URI relativizedURI = URIHelper.relativizeURI(uriDocInSuperFolder, uriDocInADifferentFolder);
		assertEquals("../customXml/item1.xml", relativizedURI.toString());
	}

	@Test
	public void testRelativizeURIDocToItselfInTheSamePlace() throws Exception {
		// Document to itself is the same place (empty URI)
		final URI relativizedURI = URIHelper.relativizeURI(uriDocInSuperFolder, uriDocInSuperFolder);
		assertEquals("", relativizedURI.getPath());
	}


	@Test
	public void testRelativizeURIDocToRootPath() throws Exception {
		// Document and root totally different
		final URI relativizedURI = URIHelper.relativizeURI(uriDocInSuperFolder, uriRootPath);
		assertEquals("../", relativizedURI.getPath());
	}

	@Test
	public void testRelativizeURIRootPathToDoc() throws Exception {
		// Document and root totally different
		final URI relativizedURI = URIHelper.relativizeURI(uriRootPath, uriDocInSuperFolder);;
		assertEquals(uriDocInSuperFolder.getPath(), relativizedURI.getPath());
	}


//	/**
//	 * Test createPartName(String, y)
//	 */
//	public void testCreatePartNameRelativeString()
//			throws InvalidFormatException {
//		PartName partNameToValid = URIHelper
//				.createPartName("/word/media/image1.gif");
//
//		Package pkg = Package.create("DELETEIFEXISTS.docx");
//		// Base part
//		PartName nameBase = URIHelper
//				.createPartName("/word/document.xml");
//		Part partBase = pkg.createPart(nameBase, ContentTypes.XML);
//		// Relative part name
//		PartName relativeName = URIHelper.createPartName(
//				"media/image1.gif", partBase);
//		assertTrue("The part name must be equal to "
//				+ partNameToValid.getName(), partNameToValid
//				.equals(relativeName));
//		pkg.revert();
//	}
//
//	/**
//	 * Test createPartName(URI, y)
//	 */
//	public void testCreatePartNameRelativeURI() throws Exception {
//		PartName partNameToValid = URIHelper
//				.createPartName("/word/media/image1.gif");
//
//		Package pkg = Package.create("DELETEIFEXISTS.docx");
//		// Base part
//		PartName nameBase = URIHelper
//				.createPartName("/word/document.xml");
//		Part partBase = pkg.createPart(nameBase, ContentTypes.XML);
//		// Relative part name
//		PartName relativeName = URIHelper.createPartName(
//				new URI("media/image1.gif"), partBase);
//		assertTrue("The part name must be equal to "
//				+ partNameToValid.getName(), partNameToValid
//				.equals(relativeName));
//		pkg.revert();
//	}
	

}
