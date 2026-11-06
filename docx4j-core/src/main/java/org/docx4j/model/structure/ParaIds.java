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
package org.docx4j.model.structure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.docx4j.Docx4jProperties;
import org.docx4j.TraversalUtil;
import org.docx4j.jaxb.McMode;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.EndnotesPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FootnotesPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.P;
import org.docx4j.wml.Tr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@code w14:paraId} on every paragraph and row saved (CR-077).
 *
 * <p>With one paragraph or table row short of an id, in any story, Word renumbers every id in some
 * files at open and fills the gap in others (core-ts's Word checks 79 and 80, python's rounds;
 * so far the files that keep them hold comments).  So one new paragraph can cost a caller who
 * addresses a paragraph by its id all of them.</p>
 *
 * <p>docx4j's editing API is the JAXB tree, and {@code getContent().add(...)} passes through
 * no code of ours, so the ids are given at save: {@link #beforeSave(Part)}, from the save routes.
 * A part nobody read is copied as bytes, and a document with no id in it stays so.</p>
 *
 * @since 17.3.6
 */
public final class ParaIds {

	private static final Logger log = LoggerFactory.getLogger(ParaIds.class);

	/** Whether a save gives the ids; true by default. */
	public static final String PROPERTY = "docx4j.save.paraIds";

	private ParaIds() {}

	/**
	 * Called by the save routes on each part they write: {@link #assign(JaxbXmlPart)}, for a part
	 * that has been read, unless {@value #PROPERTY} is false.
	 */
	public static void beforeSave(Part part) {
		if (!(part instanceof JaxbXmlPart) || content(part) == null) return;
		JaxbXmlPart<?> jaxbPart = (JaxbXmlPart<?>) part;
		if (!jaxbPart.isUnmarshalled() || !Docx4jProperties.getProperty(PROPERTY, true)) return;
		int given = assign(jaxbPart);
		if (given > 0) log.debug("{}: {} w14:paraId given", part.getPartName(), given);
	}

	/**
	 * Gives an id to each {@code w:p} and {@code w:tr} of a part that has none, and to each that
	 * repeats an earlier one, as Word does (the first keeps it), where the part or the main
	 * document part uses them: a new footer with no ids cost {@code document.xml} all of its.
	 * {@code w14:paraId} alone: Word needs no {@code w14:textId} (check 80).
	 *
	 * <p>Only {@code document.xml} and the header, footer, footnotes, endnotes and comments parts:
	 * Word writes no id in a glossary (none of 207 paragraphs in 557 of its saves).  Only the branch
	 * read: a Fallback mostly has no id, and where it has one it repeats its Choice's, which Word
	 * writes over it, so a Fallback is left alone.  Every branch's ids count as in use.</p>
	 *
	 * @return how many ids were given
	 */
	public static int assign(JaxbXmlPart<?> part) {
		Object content = content(part);
		if (content == null) return 0;

		final Set<String> inUse = new HashSet<String>();
		new TraversalUtil(content, new TraversalUtil.CallbackImpl() {
			@Override
			public List<Object> apply(Object o) {
				String id = id(o);
				if (id != null && !id.isEmpty()) inUse.add(id.toUpperCase(Locale.ROOT));
				return null;
			}
		}, McMode.ALL);
		if (inUse.isEmpty() && !mainDocumentUsesThem(part)) return 0;

		final List<Object> read = new ArrayList<Object>();
		new TraversalUtil(content, new TraversalUtil.CallbackImpl() {
			@Override
			public List<Object> apply(Object o) {
				if (o instanceof P || o instanceof Tr) read.add(o);
				return null;
			}
		}, McMode.READ);

		int given = 0;
		Set<String> seen = new HashSet<String>();
		for (Object o : read) {
			String id = id(o);
			if (id != null && !id.isEmpty() && seen.add(id.toUpperCase(Locale.ROOT))) continue;
			do {
				id = String.format("%08X", ThreadLocalRandom.current().nextInt(1, 0x7FFFFFFF));
			} while (!inUse.add(id));
			seen.add(id);
			if (o instanceof P) ((P) o).setParaId(id);
			else ((Tr) o).setParaId(id);
			given++;
		}
		return given;
	}

	/** A main document part nobody read isn't read for this: then nothing refers to a new part. */
	private static boolean mainDocumentUsesThem(JaxbXmlPart<?> part) {
		if (part instanceof MainDocumentPart || !(part.getPackage() instanceof WordprocessingMLPackage)) return false;
		MainDocumentPart main = ((WordprocessingMLPackage) part.getPackage()).getMainDocumentPart();
		if (main == null || !main.isUnmarshalled()) return false;
		final boolean[] found = new boolean[1];
		new TraversalUtil(main.getContent(), new TraversalUtil.CallbackImpl() {
			@Override
			public List<Object> apply(Object o) {
				String id = id(o);
				if (id != null && !id.isEmpty()) found[0] = true;
				return null;
			}
			@Override
			public boolean shouldTraverse(Object o) {
				return !found[0];
			}
		}, McMode.ALL);
		return found[0];
	}

	private static String id(Object o) {
		return o instanceof P ? ((P) o).getParaId() : o instanceof Tr ? ((Tr) o).getParaId() : null;
	}

	/** What a walk of the part starts from; null for a part that takes no ids, or isn't read. */
	private static Object content(Part part) {
		if (!(part instanceof JaxbXmlPart) || !((JaxbXmlPart<?>) part).isUnmarshalled()) return null;
		if (part instanceof MainDocumentPart) return ((MainDocumentPart) part).getContent();
		if (part instanceof HeaderPart) return ((HeaderPart) part).getContent();
		if (part instanceof FooterPart) return ((FooterPart) part).getContent();
		if (part instanceof FootnotesPart) return ((FootnotesPart) part).getJaxbElement().getFootnote();
		if (part instanceof EndnotesPart) return ((EndnotesPart) part).getJaxbElement().getEndnote();
		if (part instanceof CommentsPart) return ((CommentsPart) part).getJaxbElement().getComment();
		return null;
	}
}
