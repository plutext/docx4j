package org.docx4j.model.fields;

import org.docx4j.TraversalUtil;
import org.docx4j.jaxb.McMode;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.McSplice;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.CTSimpleField;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Refreshes the values of fields in the docx, simple or complex, through {@link FieldEvaluators}
 * (CR-067): by default every field it has an evaluator for, as Word's update-all does (until 17.3.5
 * DOCPROPERTY and DOCVARIABLE only; then the document information fields, #277; then, with CR-067
 * phase 1, CREATEDATE, SAVEDATE, REVNUM, INFO, SECTION, NOTEREF, REF, QUOTE, SEQ, DATE, TIME, PRINTDATE and
 * the user fields where the context supplies the user). {@link #only(Set)} narrows the set. (For MERGEFIELD, see the mergers;
 * for SEQ, {@link org.docx4j.model.fields.seq.FieldUpdaterSEQ}.)
 *
 * <p>A locked field ({@code w:fldLock}) is left as it is. A field whose value cannot be found keeps its
 * cached result. A document information field given an argument (AUTHOR "Jo"), with which Word sets
 * the property, is left as it is. PRINTDATE is {@code cp:lastPrinted}, as Word's F9 shows it (the PDF and HTML
 * writers paint the conversion's clock, the conversion being the printing); a document never printed keeps the
 * field's cached result.</p>
 *
 * <p>This updates the docx. If you don't want that, apply it to a clone instead.</p>
 *
 * @author jharrop
 */
public class FieldUpdater {

	private static Logger log = LoggerFactory.getLogger(FieldUpdater.class);

	WordprocessingMLPackage wordMLPackage;
	FieldContext context;

	StringBuilder report = null;

	private static final String DOCPROPERTY = "DOCPROPERTY";
	private static final String DOCVARIABLE = "DOCVARIABLE";

	/** The names to update, or null for every field {@link FieldEvaluators} has an evaluator for (D10, since phase 1). */
	private Set<String> fieldNames = null;

	public FieldUpdater(WordprocessingMLPackage wordMLPackage) {
		this.wordMLPackage = wordMLPackage;
		this.context = FieldContext.of(wordMLPackage);
	}

	/**
	 * Restricts (or widens) the update to these field names; a name {@link FieldEvaluators} has no
	 * evaluator for is ignored. The default is {@link #getFieldNames()}.
	 *
	 * @since 17.3.5 (CR-067 D10)
	 */
	public FieldUpdater only(Set<String> names) {
		Set<String> upper = new HashSet<String>();
		if (names != null) for (String n : names) if (n != null) upper.add(n.toUpperCase(Locale.ROOT));
		this.fieldNames = Collections.unmodifiableSet(upper);
		return this;
	}

	/** The field names this updater updates: the filter, or every name {@link FieldEvaluators} has. @since 17.3.5 */
	public Set<String> getFieldNames() {
		return fieldNames == null ? FieldEvaluators.names() : fieldNames;
	}

	/** The context the evaluators read (the clock, the language, the user ...). @since 17.3.5 */
	public FieldContext getContext() {
		return context;
	}

	public void update(boolean processHeadersAndFooters) throws Docx4JException {

		report = new StringBuilder();

		updatePart(wordMLPackage.getMainDocumentPart());

		if (processHeadersAndFooters) {
			RelationshipsPart rp = wordMLPackage.getMainDocumentPart().getRelationshipsPart();
			for (Relationship r : rp.getJaxbElement().getRelationship()) {
				if (r.getType().equals(Namespaces.HEADER)
						|| r.getType().equals(Namespaces.FOOTER)) {
					JaxbXmlPart part = (JaxbXmlPart) rp.getPart(r);
					report.append("\n" + part.getPartName() + "\n");
					log.debug("\n" + part.getPartName() + "\n");
					updatePart(part);
				}
			}
		}
		log.info(report.toString());
	}

	public void updatePart(JaxbXmlPart part) throws Docx4JException {
		context.part(part);
		context.readBookmarks(); // REF: the main part's bookmarks, whichever part the field is in
		updateSimple(part);
		updateComplex(part);
	}

	private boolean updates(String fieldName) {
		return fieldName != null && (fieldNames == null || fieldNames.contains(fieldName)) && FieldEvaluators.has(fieldName);
	}

	/** A document information field given an argument is left as it is: Word sets the property from it. */
	private static boolean leftToWord(FieldInstruction instruction) {
		return FieldEvaluators.DOC_INFO.containsKey(instruction.getName()) && instruction.getArgument() != null;
	}

	private String keyOf(FieldInstruction instruction) {
		String key = instruction.getArgument();
		if (key == null && FieldEvaluators.DOC_INFO.containsKey(instruction.getName())) key = FieldEvaluators.DOC_INFO.get(instruction.getName());
		return key;
	}

	public void updateSimple(JaxbXmlPart part) throws Docx4JException {

		List contentList = ((ContentAccessor) part).getContent();

		// find fields
		SimpleFieldLocator fl = new SimpleFieldLocator();
		// CR-021: ALL - field results are written in every branch
		new TraversalUtil(contentList, fl, McMode.ALL);

		report.append("\n\nSimple Fields in " + part.getPartName() + "\n");
		report.append("============= \n");
		report.append("Found " + fl.simpleFields.size() + " simple fields \n ");

		for (CTSimpleField simpleField : fl.simpleFields) {

			if (simpleField.isFldLock()) {
				report.append("Locked, not updated: " + simpleField.getInstr() + "\n");
				continue;
			}

			FieldInstruction instruction = FieldInstruction.parse(simpleField.getInstr());
			if (!updates(instruction.getName())) {
				report.append("Ignoring " + simpleField.getInstr() + "\n");
				continue;
			}
			if (leftToWord(instruction)) {
				report.append("Not updated (Word sets the property from its argument): " + simpleField.getInstr() + "\n");
				continue;
			}

			context.section(FieldContext.sectionOf(part, simpleField));
			context.cells(FieldSources.cellsOf(simpleField));
			String val = FieldEvaluators.evaluate(instruction, context);
			if (val == null) {
				report.append(simpleField.getInstr() + "\n");
				report.append(keyOf(instruction) + " -> NOT FOUND! \n");
				continue;
			}
			report.append(simpleField.getInstr() + "\n");
			report.append("--> " + val + "\n");

			R r = null;
			if (simpleField.getInstr().toUpperCase().contains("MERGEFORMAT")) {
				// find the first run and use the formatting of that
				r = getFirstRun(simpleField.getContent());
			}
			if (r == null) {
				r = Context.getWmlObjectFactory().createR();
			} else {
				r.getContent().clear();
			}
			simpleField.getContent().clear();
			simpleField.getContent().add(r);
			setSimpleFieldConent(r, val);
		}
	}

	/** A NOTEREF with {@code \\f}: the result in the footnote reference style, as Word writes it (measured 2026-10-28). */
	private static void noteReferenceStyle(R r) {
		if (r == null) return;
		org.docx4j.wml.RPr rPr = r.getRPr() == null ? Context.getWmlObjectFactory().createRPr() : r.getRPr();
		org.docx4j.wml.RStyle style = Context.getWmlObjectFactory().createRStyle();
		style.setVal("FootnoteReference");
		rPr.setRStyle(style);
		r.setRPr(rPr);
	}

	protected void setSimpleFieldConent(R r, String val) {
		Text t = Context.getWmlObjectFactory().createText();
		t.setValue(val);
		r.getContent().add(t);
	}

	private R getFirstRun(List<Object> content) {
		for (Object o : content) {
			if (o instanceof R) return (R) o;
		}
		return null;
	}

	public void updateComplex(JaxbXmlPart part) throws Docx4JException {

		List contentList = ((ContentAccessor) part).getContent();

		// find fields
		ComplexFieldLocator fl = new ComplexFieldLocator();
		// CR-021: ALL - field results are written in every branch
		new TraversalUtil(contentList, fl, McMode.ALL);

		report.append("\n Complex Fields in " + part.getPartName() + "\n");
		report.append("============== \n");
		report.append("Found " + fl.getStarts().size() + " fields \n");

		// canonicalise and setup fieldRefs
		List<FieldRef> fieldRefs = new ArrayList<FieldRef>();
		for (P p : fl.getStarts()) {
			int index;
			// the parent's live list, a Choice's or Fallback's included (CR-051 section 10.3, item 1)
			List<Object> siblings = McSplice.contentOf(p.getParent());
			if (siblings == null) {
				throw new Docx4JException("Unexpected parent: " + (p.getParent() == null ? "null" : p.getParent().getClass().getName()));
			}
			index = siblings.indexOf(p);
			P newP = FieldsPreprocessor.canonicalise(p, fieldRefs);
			siblings.set(index, newP);
		}

		// Populate
		for (FieldRef fr : fieldRefs) {
			if (fr.getInstructions().isEmpty()) {
				// eg a field with empty or whitespace-only w:instrText (issue 682)
				continue;
			}
			if (fr.isLock()) {
				report.append("Locked, not updated: " + fr.getFldName() + "\n");
				continue;
			}
			String fldName = fr.getFldName();
			if (!updates(fldName)) {
				report.append("Ignoring " + fldName + "\n");
				continue;
			}
			String instr = instructionOf(fr, part); // nested fields evaluated inside-out (D4)
			if (instr == null) {
				report.append("Ignoring " + fldName + " (a nested field in its instruction has no result)\n");
				continue;
			}
			FieldInstruction instruction = FieldInstruction.parse(instr);
			if (leftToWord(instruction)) {
				report.append("Not updated (Word sets the property from its argument): " + instr + "\n");
				continue;
			}

			context.section(FieldContext.sectionOf(part, fr.getParent()));
			context.cells(FieldSources.cellsOf(fr.getParent()));
			String val = FieldEvaluators.evaluate(instruction, context);
			if (val == null) {
				report.append(instr + "\n");
				report.append(keyOf(instruction) + " -> NOT FOUND! \n");
				continue;
			}
			report.append(instr + "\n");
			report.append("--> " + val + "\n");
			if (DOCPROPERTY.equals(fldName)) fr.formatNewResult(); // its old result is kept now (FieldsPreprocessor)
			List<R> runs = "REF".equals(fldName) && instruction.getArgument() != null
					? context.bookmarkRuns().get(instruction.getArgument().toLowerCase(Locale.ROOT)) : null;
			if (runs != null && !runs.isEmpty() && instruction.getSwitchValues("\\*").isEmpty()) {
				fr.setResultRuns(runs); // the bookmark's formatting, as Word keeps it (measured 2026-10-28)
			} else {
				fr.setResult(val);
				if ("NOTEREF".equals(fldName) && instruction.hasSwitch("\\f")) noteReferenceStyle(fr.getResultsSlot());
				if ("SET".equals(fldName) && instruction.getArgument() != null) {
					// Word stores a SET's value under a bookmark of its name (measured 2026-10-28, CR-067 section 13.2)
					if (nextBookmarkId == null) nextBookmarkId = FieldSources.nextBookmarkId(wordMLPackage);
					fr.bookmarkResult(instruction.getArgument(), nextBookmarkId);
					nextBookmarkId = nextBookmarkId.add(java.math.BigInteger.ONE);
				}
			}
		}
	}

	private java.math.BigInteger nextBookmarkId;

	/**
	 * The field's instruction with each nested field replaced by its result (D4: inside-out), or null where a nested
	 * field has no evaluator or no result (the outer keeps its cached result). A nested field's own stored result is
	 * not rewritten (the canonical form gives it no result slot); Word rewrites it on its own update.
	 */
	String instructionOf(FieldRef fr, JaxbXmlPart part) throws Docx4JException {
		if (fr.getInstructions().isEmpty()) return null;
		StringBuilder sb = new StringBuilder();
		for (Object o : fr.getInstructions()) {
			Object u = org.docx4j.XmlUtils.unwrap(o);
			if (u instanceof org.docx4j.wml.Text) {
				sb.append(((org.docx4j.wml.Text) u).getValue());
			} else if (u instanceof FieldRef) {
				FieldRef nested = (FieldRef) u;
				String instr = instructionOf(nested, part);
				if (instr == null) return null;
				FieldInstruction instruction = FieldInstruction.parse(instr);
				if (instruction.getName() == null || !FieldEvaluators.has(instruction.getName())) return null;
				context.section(FieldContext.sectionOf(part, fr.getParent()));
				context.cells(FieldSources.cellsOf(fr.getParent()));
				String value = FieldEvaluators.evaluate(instruction, context);
				if (value == null) return null;
				sb.append(FieldContext.asOperand(value));
			} else {
				return null;
			}
		}
		return sb.toString();
	}
}
