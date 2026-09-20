/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 */
package org.docx4j.anon;

import java.util.ArrayList;
import java.util.List;

import org.docx4j.docProps.core.CoreProperties;
import org.docx4j.docProps.core.dc.elements.SimpleLiteral;
import org.docx4j.docProps.coverPageProps.CoverPageProperties;
import org.docx4j.docProps.extended.Properties;
import org.docx4j.openpackaging.Base;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.parts.DocPropsCoverPagePart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;

/**
 * The package-level metadata: docProps core and app, the cover-page properties,
 * and every relationship which points outside the package.
 *
 * @since 17.2.0
 */
public class MetadataScrubber {

	/** what an external target becomes; RFC 6761 reserves .invalid, so it resolves nowhere */
	public static final String EXTERNAL_PLACEHOLDER = "http://example.invalid/";

	private final OpcPackage pkg;
	private int externals = 0;

	public MetadataScrubber(OpcPackage pkg) {
		this.pkg = pkg;
	}

	/**
	 * docProps/core.xml: every descriptive property and identity cleared, the dates
	 * fixed, the revision reset; docProps/app.xml: company, manager, template, the
	 * heading and part titles (which are the document's headings), the hyperlink
	 * list and base, the signature; the cover-page part's company details.
	 */
	public void scrubDocProps() throws Docx4JException {

		if (pkg.getDocPropsCorePart() != null && pkg.getDocPropsCorePart().getContents() != null) {
			CoreProperties core = pkg.getDocPropsCorePart().getContents();
			core.setCategory(null);
			core.setContentStatus(null);
			core.setCreator(null);
			core.setDescription(null);
			core.setIdentifier(null);
			core.setKeywords(null);
			core.setSubject(null);
			core.setTitle(null);
			core.setLastModifiedBy(null);
			core.setLastPrinted(null);
			core.setVersion(null);
			if (core.getRevision() != null) core.setRevision("1");
			if (core.getCreated() != null) core.setCreated(fixedDateLiteral(core.getCreated()));
			if (core.getModified() != null) core.setModified(fixedDateLiteral(core.getModified()));
		}

		if (pkg.getDocPropsExtendedPart() != null && pkg.getDocPropsExtendedPart().getContents() != null) {
			Properties app = pkg.getDocPropsExtendedPart().getContents();
			app.setCompany(null);
			app.setManager(null);
			app.setTemplate(null);
			app.setHeadingPairs(null);
			app.setTitlesOfParts(null);
			app.setHyperlinkBase(null);
			app.setHLinks(null);
			app.setDigSig(null);
			app.setTotalTime(null);
		}

		for (Part p : new ArrayList<Part>(pkg.getParts().getParts().values())) {
			if (p instanceof DocPropsCoverPagePart) {
				CoverPageProperties cpp = ((DocPropsCoverPagePart) p).getContents();
				if (cpp != null) {
					cpp.setAbstract(null);
					cpp.setCompanyAddress(null);
					cpp.setCompanyEmail(null);
					cpp.setCompanyFax(null);
					cpp.setCompanyPhone(null);
					cpp.setPublishDate(null);
				}
			}
		}
	}

	/** the same literal (it carries xsi:type="dcterms:W3CDTF"), with the fixed date as its content */
	private static SimpleLiteral fixedDateLiteral(SimpleLiteral existing) {
		existing.getContent().clear();
		existing.getContent().add(Names.FIXED_DATE.toXMLFormat());
		return existing;
	}

	/**
	 * Every relationship of {@code TargetMode="External"}, in the package's own
	 * relationships and in every part's (hyperlinks, linked images, linked OLE,
	 * the attached template, mail-merge sources), gets a placeholder target,
	 * numbered so distinct targets stay distinct.
	 *
	 * @return how many were replaced
	 */
	public int scrubExternalTargets() {
		externals = 0;
		scrubExternalTargets(pkg);
		for (Part p : new ArrayList<Part>(pkg.getParts().getParts().values())) {
			scrubExternalTargets(p);
		}
		return externals;
	}

	private void scrubExternalTargets(Base base) {
		RelationshipsPart rp = base.getRelationshipsPart();
		if (rp == null || rp.getRelationships() == null) return;
		List<Relationship> rels = rp.getRelationships().getRelationship();
		for (Relationship r : rels) {
			if ("External".equals(r.getTargetMode())) {
				externals++;
				r.setTarget(EXTERNAL_PLACEHOLDER + externals);
			}
		}
	}

}
