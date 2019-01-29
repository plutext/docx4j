package org.docx4j.openpackaging.parts.relationships;

import junit.framework.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart.AddPartBehaviour;
import org.docx4j.relationships.Relationship;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AddPartTests {
	
	protected static Logger log = LoggerFactory.getLogger(AddPartTests.class);
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		// Do this first, so it is not included
		// in timing of first test.
		Context.getWmlObjectFactory(); 
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testOverwriteNoPart() throws Exception {
		
		noPart(AddPartBehaviour.OVERWRITE_IF_NAME_EXISTS);		
	}	
	
	@Test
	public void testReuseWhenNoPart() throws Exception {
		
		noPart(AddPartBehaviour.REUSE_EXISTING);		
	}	

	@Test
	public void testRenameWhenNoPart() throws Exception {
		
		noPart(AddPartBehaviour.RENAME_IF_NAME_EXISTS);		
	}	

	private void noPart(AddPartBehaviour mode) throws Exception {
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		
		int relsCountBefore = wordMLPackage.getMainDocumentPart().getRelationshipsPart().size();
		int partsCountBefore = wordMLPackage.getParts().getParts().size();
		
		HeaderPart part = new HeaderPart();		
		wordMLPackage.getMainDocumentPart().addTargetPart(part, mode);
		
		// Just added a part
		Assert.assertTrue("hmm", wordMLPackage.getMainDocumentPart().getRelationshipsPart().size()==relsCountBefore+1);
		Assert.assertTrue("hmm", wordMLPackage.getParts().getParts().size()==partsCountBefore+1);		
	}
	
	
	@Test
	public void testOverwritePartExistsInPkg() throws Exception {
		
		inPkg( AddPartBehaviour.OVERWRITE_IF_NAME_EXISTS, 0, 1, true);
		
	}	
	
	@Test
	public void testReusePartExistsInPkg() throws Exception {
		
		// different behaviour in Java 8?
		boolean expectRefToExisting = true;
		inPkg( AddPartBehaviour.REUSE_EXISTING, 0, 1, expectRefToExisting);
		// partsAdded=0, since we're re-using
		// relsAdded=1, since we're adding to package level rels		
		
	}	
	
	
	@Test
	public void testRenamePartExistsInPkg() throws Exception {
		
		inPkg( AddPartBehaviour.RENAME_IF_NAME_EXISTS, 1, 1, true);
		
	}	

	private void inPkg(AddPartBehaviour mode, int partsAdded, int relsAdded, boolean expectReplace) throws Exception {
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();

		HeaderPart part = new HeaderPart();		
		System.out.println(part.getXML());
		Relationship rel1 = wordMLPackage.getMainDocumentPart().addTargetPart(part, mode);
		System.out.println("added part named " + part.getPartName());
		
//		int mdpRelsCountBefore = wordMLPackage.getMainDocumentPart().getRelationshipsPart().size();
		int rootRelsCountBefore = wordMLPackage.getRelationshipsPart().size();
		System.out.println(wordMLPackage.getRelationshipsPart().getXML());
		int partsCountBefore = wordMLPackage.getParts().getParts().size();

		// Add it again 
		// Note that we're adding this ELSEWHERE in the pkg for the purposes of this test only		
		HeaderPart part2 = new HeaderPart();	
		Relationship rel2 = wordMLPackage.addTargetPart(part2, mode); // NB wrong place for a header part; don't copy this code!
		System.out.println("added part2 named " + part2.getPartName());
		if (mode==AddPartBehaviour.REUSE_EXISTING) {
			// In the REUSE_EXISTING case, we won't be adding this part, so 
			// we can't add content to it (since its pkg isn't set)
						
			part2 = (HeaderPart)wordMLPackage.getRelationshipsPart().getPart(rel2);  // this should be a ref to part1
		} 
		part2.getContent().add(Context.getWmlObjectFactory().createP() );
		
		// Just added a part
		//Assert.assertTrue("hmm", wordMLPackage.getMainDocumentPart().getRelationshipsPart().size()==mdpRelsCountBefore+expectedIncrement);
		System.out.println(wordMLPackage.getRelationshipsPart().getXML());
		Assert.assertTrue("package level rels count", wordMLPackage.getRelationshipsPart().size()==rootRelsCountBefore+relsAdded);
		Assert.assertTrue("number of parts", wordMLPackage.getParts().getParts().size()==partsCountBefore+partsAdded);		
		
		// Check this part is the one we added - differentiated by its content
		HeaderPart result = (HeaderPart)wordMLPackage.getRelationshipsPart().getPart(rel2);
		System.out.println(result.getXML());
		if (expectReplace) {
			System.out.println("new part replaces existing");
			Assert.assertTrue("header content list count", result.getContent().size()==1);
		} else {
			Assert.assertTrue("header content list count", result.getContent().size()==0);			
		}
		
	}	
	
	@Test
	public void testOverwritePartExistsInRels() throws Exception {
		inRels( AddPartBehaviour.OVERWRITE_IF_NAME_EXISTS, 0, 0, true);
	}	

	@Test
	public void testReusePartExistsInRels() throws Exception {
		inRels( AddPartBehaviour.REUSE_EXISTING, 0, 0, false);
	}	
	
	@Test
	public void testRenamePartExistsInRels() throws Exception {
		inRels( AddPartBehaviour.RENAME_IF_NAME_EXISTS, 1, 1, true);
	}	

	private void inRels(AddPartBehaviour mode, int partsAdded, int relsAdded, boolean expectReplace) throws Exception {

		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		HeaderPart part = new HeaderPart();		
		wordMLPackage.getMainDocumentPart().addTargetPart(part,  mode);

		int relsCountBefore = wordMLPackage.getMainDocumentPart().getRelationshipsPart().size();
		int partsCountBefore = wordMLPackage.getParts().getParts().size();
		
		// Add it again
		HeaderPart part2 = new HeaderPart();		
		part2.getContent().add(Context.getWmlObjectFactory().createP() );
		Relationship rel = wordMLPackage.getMainDocumentPart().addTargetPart(part2, mode);
		
		// Just added a part
		Assert.assertTrue("hmm", wordMLPackage.getMainDocumentPart().getRelationshipsPart().size()==relsCountBefore+relsAdded);
		Assert.assertTrue("hmm", wordMLPackage.getParts().getParts().size()==partsCountBefore+partsAdded);
		
		// Check this part is the one we added - differentiated by its content
		HeaderPart result = (HeaderPart)wordMLPackage.getMainDocumentPart().getRelationshipsPart().getPart(rel);
		if (expectReplace) {
			Assert.assertTrue("hmm", result.getContent().size()==1);
		} else {
			Assert.assertTrue("hmm", result.getContent().size()==0);			
		}
		
	}
	
}
