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
package org.docx4j.model.text;

import org.docx4j.openpackaging.parts.Part;

/**
 * An object run's object as the document gives it (WD REQ-512): what it is, its relationship, its
 * part and the part's content type, its ProgID, and for an OLE 1.0 package the names its native data
 * holds, so that a host that reads embedded content itself can find the object and name it, and an
 * {@link EmbeddedFilter} can decide whether docx4j reads it (REQ-522). The WD's block model gives
 * the same members ({@code kind}, {@code relationshipId}, {@code part}, {@code contentType},
 * {@code label}, {@code fileName}, {@code command}); each is null here where the WD's is absent.
 *
 * @since 17.3.5 (CR-070)
 */
public final class TextObject {

	/** What the object is (REQ-512's {@code kind}). */
	public enum Kind {
		/** An embedded object: {@code o:OLEObject} of {@code Type="Embed"}. */
		EMBED,
		/** A linked object: {@code o:OLEObject} of {@code Type="Link"}; its relationship is external, so it has no part. */
		LINK,
		/** An ActiveX control: a {@code w:control}, or an {@code o:OLEObject} whose part is an ActiveX control's. */
		CONTROL
	}

	final Kind kind;
	final String relationshipId;
	final String progId;
	final Part part;
	final String label;
	final String fileName;
	final String command;
	final Object source;

	TextObject(Kind kind, String relationshipId, String progId, Part part, String[] packageNames, Object source) {
		this.kind = kind;
		this.relationshipId = relationshipId;
		this.progId = progId;
		this.part = part;
		this.label = packageNames == null ? null : packageNames[0];
		this.fileName = packageNames == null ? null : packageNames[1];
		this.command = packageNames == null ? null : packageNames[2];
		this.source = source;
	}

	/** {@link Kind#EMBED}, {@link Kind#LINK} ({@code o:OLEObject/@Type}) or {@link Kind#CONTROL}. */
	public Kind getKind() { return kind; }
	/** The {@code r:id} of the {@code o:OLEObject} or {@code w:control}; null for none. */
	public String getRelationshipId() { return relationshipId; }
	/** {@code o:OLEObject/@ProgID}, as {@link TextRun#getProgId()}; null for a {@code w:control}, whose class is in its part. */
	public String getProgId() { return progId; }
	/**
	 * The embedded part ({@code EmbeddedPackagePart} for an OOXML package, a {@code BinaryPart} for an
	 * OLE binary), or the control's part; null for a linked object and where the relationship does
	 * not resolve.
	 */
	public Part getPart() { return part; }
	/** The part's name ({@code /word/embeddings/Microsoft_Excel_Worksheet.xlsx}), the WD's {@code part}; null where there is no part. */
	public String getPartName() { return part == null || part.getPartName() == null ? null : part.getPartName().getName(); }
	/** The part's content type as the package declares it; null where there is no part. */
	public String getContentType() { return part == null ? null : part.getContentType(); }
	/**
	 * For an OLE 1.0 package (ProgID {@code Package}, an {@code Ole10Native} stream), the label its
	 * native data gives, usually the file's name ({@code notes.txt}); null for any other object, and
	 * where the package has none. Tika 3.3.3 gives a package it hands on this as its resource name.
	 */
	public String getPackageLabel() { return label; }
	/** For an OLE 1.0 package, the file name its native data gives, often with the path it was embedded from; else null. */
	public String getPackageFileName() { return fileName; }
	/** For an OLE 1.0 package, the command its native data gives, usually the file's path; else null. */
	public String getPackageCommand() { return command; }
	/** The JAXB object the object is read from: a {@code w:object} or a {@code w:control}. */
	public Object getSource() { return source; }

	@Override
	public String toString() {
		return kind + (progId == null ? "" : " " + progId) + (part == null ? "" : " " + getPartName())
				+ (label == null ? "" : " (" + label + ")");
	}
}
