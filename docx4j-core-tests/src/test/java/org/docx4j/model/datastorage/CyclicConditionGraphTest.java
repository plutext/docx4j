package org.docx4j.model.datastorage;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.opendope.ConditionsPart;
import org.junit.Test;
import org.opendope.conditions.Condition;
import org.opendope.conditions.Conditionref;
import org.opendope.conditions.Conditions;
import org.opendope.conditions.Not;
import org.opendope.conditions.Xpathref;

/**
 * A crafted ConditionsPart whose conditions reference each other (directly or
 * transitively) via &lt;conditionref&gt; used to drive
 * Condition/And/Or/Not/Conditionref.evaluate into unbounded recursion and a
 * StackOverflowError at bind time, killing the worker thread — a DoS on any
 * service that binds an untrusted .docx (GHSA-qw8x-rxfh-9qqq).
 *
 * OpenDoPEHandler now rejects a cyclic (or dangling) condition graph up front,
 * with a Docx4JException instead of an Error.
 */
public class CyclicConditionGraphTest {

	private static Condition notRefCondition(String id, String refId) {
		Conditionref ref = new Conditionref();
		ref.setId(refId);
		Not not = new Not();
		not.setParticle(ref);
		Condition c = new Condition();
		c.setId(id);
		c.setParticle(not);
		return c;
	}

	private static WordprocessingMLPackage packageWithConditions(Conditions conditions)
			throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		ConditionsPart cp = new ConditionsPart(new PartName("/customXml/item1.xml"));
		cp.setJaxbElement(conditions);
		pkg.getMainDocumentPart().addTargetPart(cp);
		return pkg;
	}

	/** cond0 -> not(conditionref cond1); cond1 -> not(conditionref cond0). */
	@Test
	public void testMutuallyReferencingConditionsRejected() throws Exception {

		Conditions conditions = new Conditions();
		conditions.getCondition().add(notRefCondition("cond0", "cond1"));
		conditions.getCondition().add(notRefCondition("cond1", "cond0"));

		WordprocessingMLPackage pkg = packageWithConditions(conditions);

		try {
			new OpenDoPEHandler(pkg);
			fail("Expected Docx4JException for cyclic condition graph, but none was thrown");
		} catch (Docx4JException e) {
			assertTrue("message should identify the cycle: " + e.getMessage(),
					e.getMessage() != null && e.getMessage().contains("Cyclic"));
		}
	}

	/** A single condition that references itself. */
	@Test
	public void testSelfReferencingConditionRejected() throws Exception {

		Conditions conditions = new Conditions();
		conditions.getCondition().add(notRefCondition("self", "self"));

		WordprocessingMLPackage pkg = packageWithConditions(conditions);

		try {
			new OpenDoPEHandler(pkg);
			fail("Expected Docx4JException for self-referencing condition, but none was thrown");
		} catch (Docx4JException e) {
			assertTrue(e.getMessage() != null && e.getMessage().contains("Cyclic"));
		}
	}

	/** A conditionref whose target id does not exist would NPE at evaluate time. */
	@Test
	public void testDanglingConditionrefRejected() throws Exception {

		Conditions conditions = new Conditions();
		conditions.getCondition().add(notRefCondition("cond0", "doesNotExist"));

		WordprocessingMLPackage pkg = packageWithConditions(conditions);

		try {
			new OpenDoPEHandler(pkg);
			fail("Expected Docx4JException for dangling conditionref, but none was thrown");
		} catch (Docx4JException e) {
			assertTrue(e.getMessage() != null && e.getMessage().contains("missing condition"));
		}
	}

	/** A legitimate acyclic graph (conditionref chain ending at an xpathref) is accepted. */
	@Test
	public void testAcyclicConditionGraphAccepted() throws Exception {

		Conditions conditions = new Conditions();
		// cond0 -> not(conditionref cond1); cond1 -> xpathref (a real leaf)
		conditions.getCondition().add(notRefCondition("cond0", "cond1"));

		Xpathref xp = new Xpathref();
		xp.setId("someXPath");
		Condition leaf = new Condition();
		leaf.setId("cond1");
		leaf.setParticle(xp);
		conditions.getCondition().add(leaf);

		WordprocessingMLPackage pkg = packageWithConditions(conditions);

		// Must not throw; the constructor builds and validates the condition graph.
		OpenDoPEHandler odh = new OpenDoPEHandler(pkg);
		assertNotNull(odh);
	}

}
