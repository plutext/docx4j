package org.docx4j.openpackaging.parts.relationships;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.relationships.ObjectFactory;
import org.docx4j.relationships.Relationship;
import org.junit.Test;

/**
 * Relationship id allocation, including the occupied-id cache which replaced
 * the per-add linear scans (adding n rels used to be O(n^2); a
 * hyperlink-dense import made that visible).  The cache is guarded by the
 * identity and size of the live relationship list, so external list-level
 * mutations must still be detected.
 */
public class RelIdAllocationTest {

	private static final ObjectFactory FACTORY = new ObjectFactory();

	private static Relationship hyperlink(String url) {
		Relationship rel = FACTORY.createRelationship();
		rel.setType(Namespaces.HYPERLINK);
		rel.setTarget(url);
		rel.setTargetMode("External");
		return rel;
	}

	private static RelationshipsPart docRels() throws Exception {
		return WordprocessingMLPackage.createPackage()
				.getMainDocumentPart().getRelationshipsPart();
	}

	@Test
	public void manyAddsAllocateUniqueIds() throws Exception {
		RelationshipsPart rp = docRels();
		Set<String> ids = new HashSet<>();
		for (Relationship existing : rp.getRelationships().getRelationship()) {
			ids.add(existing.getId());
		}
		int existing = ids.size();
		for (int i = 0; i < 20_000; i++) {
			Relationship rel = hyperlink("https://example.org/" + i);
			rp.addRelationship(rel);
			assertTrue("duplicate id " + rel.getId(), ids.add(rel.getId()));
		}
		assertEquals(existing + 20_000, rp.size());
	}

	@Test
	public void directListAddIsDetected() throws Exception {
		// code elsewhere sometimes appends to the live list without going
		// through addRelationship; the size guard must catch that
		RelationshipsPart rp = docRels();
		rp.getNextId(); // populate the cache
		Relationship direct = hyperlink("https://example.org/direct");
		direct.setId("rId9999");
		rp.getRelationships().getRelationship().add(direct);

		assertTrue(rp.isRelIdOccupied("rId9999"));
		Relationship added = hyperlink("https://example.org/next");
		rp.addRelationship(added);
		assertFalse("rId9999".equals(added.getId()));
	}

	@Test
	public void replacedJaxbElementIsDetected() throws Exception {
		RelationshipsPart rp = docRels();
		rp.getNextId(); // populate the cache

		org.docx4j.relationships.Relationships fresh = FACTORY.createRelationships();
		Relationship kept = hyperlink("https://example.org/kept");
		kept.setId("rId42");
		fresh.getRelationship().add(kept);
		rp.setRelationships(fresh);

		assertTrue(rp.isRelIdOccupied("rId42"));
		assertFalse(rp.isRelIdOccupied("rId1")); // the old rels are gone
	}

	@Test
	public void removalFreesNothingButIsSeen() throws Exception {
		RelationshipsPart rp = docRels();
		Relationship rel = hyperlink("https://example.org/x");
		rp.addRelationship(rel);
		String id = rel.getId();
		assertTrue(rp.isRelIdOccupied(id));

		rp.removeRelationship(rel);
		assertFalse(rp.isRelIdOccupied(id));
		// by design the allocator does not reuse removed ids
		Relationship next = hyperlink("https://example.org/y");
		rp.addRelationship(next);
		assertFalse(id.equals(next.getId()));
	}

	@Test
	public void inPlaceRenumberViaResetIdAllocator() throws Exception {
		// editing an EXISTING rel's id in place is invisible to the guards;
		// resetIdAllocator is the documented hook, and must clear the cache
		RelationshipsPart rp = docRels();
		Relationship rel = hyperlink("https://example.org/x");
		rp.addRelationship(rel);
		rp.getNextId(); // populate the cache
		rel.setId("rId777");
		rp.resetIdAllocator();

		assertTrue(rp.isRelIdOccupied("rId777"));
		Relationship next = hyperlink("https://example.org/y");
		rp.addRelationship(next);
		assertEquals("rId778", next.getId()); // allocator resumed past the renumber
	}

}
