package org.docx4j.convert.out;

import java.util.List;

import org.docx4j.model.structure.HeaderFooterPolicy;
import org.docx4j.model.structure.SectionWrapper;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.wml.SectPr;

public class ConversionSectionWrapper extends SectionWrapper {
	protected List<Object> content = null;
	protected String id = null;
	
	protected ConversionSectionWrapper(SectPr sectPr, HeaderFooterPolicy previousHF, RelationshipsPart rels, String id, List<Object> content) {
		super(sectPr, previousHF, rels);
		this.id = id;
		this.content = content;
	}
	
	public String getId() {
		return id;
	}
	
	public List<Object> getContent() {
		return content;
	}
}
