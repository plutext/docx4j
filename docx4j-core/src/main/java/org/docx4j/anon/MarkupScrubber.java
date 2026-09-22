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

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import jakarta.xml.bind.JAXBElement;

import org.docx4j.XmlUtils;
import org.docx4j.anon.JaxbGraphWalker.Action;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.w15.CTPerson;
import org.docx4j.w15.CTPresenceInfo;
import org.docx4j.w16cex.CTCommentExtensible;
import org.docx4j.wml.CTAltChunk;
import org.docx4j.wml.CTBookmark;
import org.docx4j.wml.CTControl;
import org.docx4j.wml.CTDataBinding;
import org.docx4j.wml.CTDocProtect;
import org.docx4j.wml.CTDocVars;
import org.docx4j.wml.CTFFData;
import org.docx4j.wml.CTFFName;
import org.docx4j.wml.CTMailMerge;
import org.docx4j.wml.CTMoveBookmark;
import org.docx4j.wml.CTObject;
import org.docx4j.wml.CTPictureBase;
import org.docx4j.wml.CTRel;
import org.docx4j.wml.CTSdtDate;
import org.docx4j.wml.CTSimpleField;
import org.docx4j.wml.CTTblPrBase;
import org.docx4j.wml.CTTrackChange;
import org.docx4j.wml.CTWriteProtection;
import org.docx4j.wml.Comments;
import org.docx4j.wml.FldChar;
import org.docx4j.wml.FontRel;
import org.docx4j.wml.Lvl;
import org.docx4j.wml.Numbering;
import org.docx4j.wml.P;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.Pict;
import org.docx4j.wml.RStyle;
import org.docx4j.wml.Style;
import org.docx4j.wml.Tag;

/**
 * The second visitor of the anonymiser's walk: identities, references and the
 * markup that points at embedded objects.
 * <ul>
 * <li>Identity: every {@link CTTrackChange} (w:ins, w:del, w:moveFrom,
 * w:moveTo, every *Change, w:cellIns/cellDel/cellMerge, w:numberingChange, and
 * w:comment) has its author renamed and its date fixed; move bookmarks the same;
 * comment initials renamed; people.xml authors and presence; commentsExtensible
 * dates.</li>
 * <li>References: bookmark and form-field names renamed (consistently with
 * the field instructions {@link ScrambleText} scrubs); hyperlink tooltips,
 * anchors and document locations; content-control tags and data bindings
 * removed, date-picker values fixed; field data blobs removed; custom style ids
 * renamed at the definition and at every reference.</li>
 * <li>Settings: mail merge, document variables, attached template and printer
 * settings relationships removed; protection hashes and salts cleared (the
 * protection itself stays, with no password).</li>
 * <li>Embedded objects, in STRICT mode: w:altChunk, o:OLEObject, w:control,
 * c:externalData, cx:externalData and the font-table embed elements are removed
 * with the parts they point at ({@link Anonymize} removes the parts); in KEEP
 * mode they stay, and the part is reported as kept unsafe.</li>
 * </ul>
 *
 * @since 17.2.0
 */
public class MarkupScrubber implements JaxbGraphWalker.Visitor {

	private final Names names;
	private final boolean strict;
	private final Consumer<String> notes;
	private String lastPersonAuthor;
	private String currentPartName = "";
	private Object replacement;

	/**
	 * "partName#relId" of every picture which stood in for a removed OLE object or
	 * ActiveX control: {@link MediaReplacer} gives those the labelled placeholder
	 * rather than the plain pixels.
	 */
	final Set<String> objectPreviews = new HashSet<String>();

	/** The part being walked: relationship ids are per part. */
	public void setCurrentPart(Part part) {
		currentPartName = part == null ? "" : part.getPartName().getName();
	}

	@Override
	public Object replacement() {
		return replacement;
	}

	/**
	 * @param names  the shared renamings
	 * @param strict remove the markup of embedded objects (their parts go too)
	 * @param notes  receives one line per removal, for the report
	 */
	public MarkupScrubber(Names names, boolean strict, Consumer<String> notes) {
		this.names = names;
		this.strict = strict;
		this.notes = notes;
	}

	@Override
	public Action visit(Object o, JAXBElement<?> wrapper) {

		// ---- identity
		if (o instanceof CTTrackChange) {
			CTTrackChange tc = (CTTrackChange) o;
			tc.setAuthor(names.author(tc.getAuthor()));
			if (tc.getDate() != null) tc.setDate(Names.fixedDate());
			if (tc.getDateUtc() != null) tc.setDateUtc(null);
			if (o instanceof Comments.Comment) {
				Comments.Comment c = (Comments.Comment) o;
				c.setInitials(names.initials(c.getInitials()));
			}
			return Action.CONTINUE;
		}
		if (o instanceof CTMoveBookmark) {
			CTMoveBookmark mb = (CTMoveBookmark) o;
			mb.setAuthor(names.author(mb.getAuthor()));
			if (mb.getDate() != null) mb.setDate(Names.fixedDate());
			mb.setName(Names.identifier(mb.getName()));
			return Action.CONTINUE;
		}
		if (o instanceof CTPerson) {
			CTPerson p = (CTPerson) o;
			p.setAuthor(names.author(p.getAuthor()));
			lastPersonAuthor = p.getAuthor();
			if (p.getContact() != null) p.setContact(null);
			return Action.CONTINUE;
		}
		if (o instanceof CTPresenceInfo) {
			CTPresenceInfo pi = (CTPresenceInfo) o;
			pi.setProviderId("None");
			// the parent person was visited just before this (pre-order): the user id
			// (an email or directory id) becomes that person's anonymised name
			if (pi.getUserId() != null) pi.setUserId(lastPersonAuthor != null ? lastPersonAuthor : "Author");
			return Action.SKIP_CHILDREN;
		}
		if (o instanceof CTCommentExtensible) {
			CTCommentExtensible ce = (CTCommentExtensible) o;
			if (ce.getDateUtc() != null) ce.setDateUtc(Names.fixedDate());
			return Action.CONTINUE;
		}

		// ---- references
		if (o instanceof CTBookmark) {
			CTBookmark b = (CTBookmark) o;
			b.setName(Names.identifier(b.getName()));
			return Action.SKIP_CHILDREN;
		}
		if (o instanceof CTFFName) {
			CTFFName n = (CTFFName) o;
			n.setVal(Names.identifier(n.getVal()));
			return Action.SKIP_CHILDREN;
		}
		if (o instanceof CTFFData) {
			return Action.CONTINUE; // its name, default, help text, entries are visited
		}
		if (o instanceof FldChar) {
			FldChar fc = (FldChar) o;
			if (fc.getFldData() != null) {
				fc.setFldData(null);
				notes.accept("w:fldData removed");
			}
			return Action.CONTINUE;
		}
		if (o instanceof CTSimpleField) {
			CTSimpleField f = (CTSimpleField) o;
			if (f.getFldData() != null) {
				f.setFldData(null);
				notes.accept("w:fldData removed");
			}
			return Action.CONTINUE;
		}
		if (o instanceof P.Hyperlink) {
			P.Hyperlink h = (P.Hyperlink) o;
			h.setTooltip(null);
			h.setDocLocation(null);
			if (h.getAnchor() != null) h.setAnchor(Names.identifier(h.getAnchor()));
			return Action.CONTINUE;
		}
		if (o instanceof Tag || o instanceof CTDataBinding) {
			return Action.REMOVE;
		}
		if (o instanceof CTSdtDate) {
			CTSdtDate d = (CTSdtDate) o;
			if (d.getFullDate() != null) d.setFullDate(Names.fixedDate());
			return Action.CONTINUE;
		}

		// ---- style ids (the names are ScrambleText's)
		if (o instanceof Style) {
			Style s = (Style) o;
			s.setStyleId(names.styleId(s.getStyleId()));
			return Action.CONTINUE;
		}
		if (o instanceof Style.BasedOn) {
			Style.BasedOn r = (Style.BasedOn) o;
			r.setVal(names.styleId(r.getVal()));
			return Action.SKIP_CHILDREN;
		}
		if (o instanceof Style.Next) {
			Style.Next r = (Style.Next) o;
			r.setVal(names.styleId(r.getVal()));
			return Action.SKIP_CHILDREN;
		}
		if (o instanceof Style.Link) {
			Style.Link r = (Style.Link) o;
			r.setVal(names.styleId(r.getVal()));
			return Action.SKIP_CHILDREN;
		}
		if (o instanceof PPrBase.PStyle) {
			PPrBase.PStyle r = (PPrBase.PStyle) o;
			r.setVal(names.styleId(r.getVal()));
			return Action.SKIP_CHILDREN;
		}
		if (o instanceof RStyle) {
			RStyle r = (RStyle) o;
			r.setVal(names.styleId(r.getVal()));
			return Action.SKIP_CHILDREN;
		}
		if (o instanceof CTTblPrBase.TblStyle) {
			CTTblPrBase.TblStyle r = (CTTblPrBase.TblStyle) o;
			r.setVal(names.styleId(r.getVal()));
			return Action.SKIP_CHILDREN;
		}
		if (o instanceof Lvl.PStyle) {
			Lvl.PStyle r = (Lvl.PStyle) o;
			r.setVal(names.styleId(r.getVal()));
			return Action.SKIP_CHILDREN;
		}
		if (o instanceof Numbering.AbstractNum.NumStyleLink) {
			Numbering.AbstractNum.NumStyleLink r = (Numbering.AbstractNum.NumStyleLink) o;
			r.setVal(names.styleId(r.getVal()));
			return Action.SKIP_CHILDREN;
		}
		if (o instanceof Numbering.AbstractNum.StyleLink) {
			Numbering.AbstractNum.StyleLink r = (Numbering.AbstractNum.StyleLink) o;
			r.setVal(names.styleId(r.getVal()));
			return Action.SKIP_CHILDREN;
		}

		// ---- settings
		if (o instanceof org.docx4j.wml.CTSettings) {
			// the document ids fingerprint a document across its copies
			org.docx4j.wml.CTSettings settings = (org.docx4j.wml.CTSettings) o;
			if (settings.getDocId14() != null) settings.getDocId14().setVal("00000001");
			if (settings.getDocId15() != null) settings.getDocId15().setVal("{00000000-0000-0000-0000-000000000001}");
			return Action.CONTINUE;
		}
		if (o instanceof CTMailMerge) {
			notes.accept("w:mailMerge removed");
			return Action.REMOVE;
		}
		if (o instanceof CTDocVars) {
			notes.accept("w:docVars removed");
			return Action.REMOVE;
		}
		if (o instanceof CTRel) {
			// w:attachedTemplate, w:printerSettings, mail-merge sources: relationships to
			// outside the package, or to a part STRICT removes
			notes.accept("settings relationship removed (attached template / printer settings / data source)");
			return Action.REMOVE;
		}
		if (o instanceof CTDocProtect) {
			CTDocProtect p = (CTDocProtect) o;
			p.setHash(null);
			p.setSalt(null);
			p.setCryptProviderType(null);
			p.setCryptAlgorithmClass(null);
			p.setCryptAlgorithmType(null);
			p.setCryptAlgorithmSid(null);
			p.setCryptSpinCount(null);
			p.setCryptProvider(null);
			p.setAlgIdExt(null);
			p.setAlgIdExtSource(null);
			p.setCryptProviderTypeExt(null);
			p.setCryptProviderTypeExtSource(null);
			return Action.SKIP_CHILDREN;
		}
		if (o instanceof CTWriteProtection) {
			CTWriteProtection p = (CTWriteProtection) o;
			p.setHash(null);
			p.setSalt(null);
			p.setCryptProviderType(null);
			p.setCryptAlgorithmClass(null);
			p.setCryptAlgorithmType(null);
			p.setCryptAlgorithmSid(null);
			p.setCryptSpinCount(null);
			p.setCryptProvider(null);
			p.setAlgIdExt(null);
			p.setAlgIdExtSource(null);
			p.setCryptProviderTypeExt(null);
			p.setCryptProviderTypeExtSource(null);
			return Action.SKIP_CHILDREN;
		}

		// ---- embedded objects
		if (strict) {
			if (o instanceof CTPictureBase) {
				// w:object / w:pict: if it holds an OLE object or a control, its VML picture
				// is the object's footprint and gets the labelled placeholder
				CTPictureBase pict = (CTPictureBase) o;
				boolean holdsObject = (o instanceof CTObject && ((CTObject) o).getControl() != null)
						|| (o instanceof Pict && ((Pict) o).getControl() != null);
				for (Object child : pict.getAnyAndAny()) {
					if (XmlUtils.unwrap(child) instanceof org.docx4j.vml.officedrawing.CTOLEObject) holdsObject = true;
				}
				if (holdsObject) {
					for (Object child : pict.getAnyAndAny()) {
						Object shape = XmlUtils.unwrap(child);
						if (shape instanceof org.docx4j.vml.CTShape) {
							for (JAXBElement<?> el : ((org.docx4j.vml.CTShape) shape).getPathOrFormulasOrHandles()) {
								if (el.getValue() instanceof org.docx4j.vml.CTImageData) {
									org.docx4j.vml.CTImageData data = (org.docx4j.vml.CTImageData) el.getValue();
									String relId = data.getId() != null ? data.getId() : data.getRelid();
									if (relId != null) objectPreviews.add(currentPartName + "#" + relId);
								}
							}
						}
					}
				}
				return Action.CONTINUE;
			}
			if (o instanceof CTAltChunk) {
				notes.accept("w:altChunk replaced by a marker paragraph");
				replacement = Placeholders.altChunkRemoved();
				return Action.REPLACE;
			}
			if (o instanceof org.docx4j.vml.officedrawing.CTOLEObject) {
				notes.accept("o:OLEObject removed (its picture stays, as the labelled placeholder)");
				return Action.REMOVE;
			}
			if (o instanceof CTControl) {
				notes.accept("w:control removed");
				return Action.REMOVE;
			}
			if (o instanceof FontRel) {
				notes.accept("embedded font reference removed");
				return Action.REMOVE;
			}
			String simple = o.getClass().getSimpleName();
			if (simple.equals("CTExternalData")) {
				notes.accept("chart externalData removed");
				return Action.REMOVE;
			}
			if (simple.equals("CTGeoCache") || simple.equals("CTGeography")) {
				notes.accept("chart geography cache removed");
				return Action.REMOVE;
			}
		}

		return Action.CONTINUE;
	}

}
