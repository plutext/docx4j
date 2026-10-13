/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.docx4j.model.chart;

import java.util.List;
import java.util.Locale;

import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.mce.AlternateContent;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.wml.ContentAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Whether the outputs draw a chartex chart's {@code mc:Choice} or its {@code mc:Fallback}
 * (CR-039).  Office writes a chartex chart (a waterfall, a histogram, ...) as an
 * {@code mc:AlternateContent}: its Choice holds the chart's frame, its Fallback a picture of the
 * chart (Word, PowerPoint) or a sentence (Excel).  The Choice's {@code Requires} prefix depends
 * on the chart's type ({@code cx1} to {@code cx8}), so a Choice is known for a chartex chart's by
 * what it holds: a frame whose {@code a:graphicData} names a {@code cx:chart}.
 *
 * <p>The property {@value #PROPERTY} decides:</p>
 * <ul>
 * <li>{@code auto} (the default): the Choice where a {@link ChartExRenderer} is present and its
 *     {@link ChartExRenderer#check check} says it draws that chart, else the Fallback.  Without a
 *     chartex renderer nothing is read and the outputs are as they were;</li>
 * <li>{@code fallback}: the Fallback, always;</li>
 * <li>{@code choice}: the Choice, always (its box stays empty where nothing draws the chart).</li>
 * </ul>
 * <p>A {@code cx} prefix listed in {@code docx4j.jaxb.mc.preferChoice} still takes the Choice, as
 * {@link org.docx4j.jaxb.McSelection} says.  The rule needs the part the Choice is in, to read its
 * chart: the outputs that draw give it ({@link org.docx4j.jaxb.McSelection#selectedBranch(AlternateContent, Part)});
 * a walk that draws nothing takes the Fallback.</p>
 *
 * @since 17.3.3 (CR-039)
 */
public final class ChartExBranch {

	private static final Logger log = LoggerFactory.getLogger(ChartExBranch.class);

	/** {@code auto} (the default), {@code fallback} or {@code choice}: see the class's note */
	public static final String PROPERTY = "docx4j.chart.chartex.branch";

	private enum Mode {
		AUTO, FALLBACK, CHOICE
	}

	private ChartExBranch() {
	}

	private static Mode mode() {
		String v = Docx4jProperties.getProperty(PROPERTY, "auto").trim().toLowerCase(Locale.ROOT);
		if ("fallback".equals(v)) return Mode.FALLBACK;
		if ("choice".equals(v)) return Mode.CHOICE;
		if (!"auto".equals(v) && !v.isEmpty()) log.warn(PROPERTY + "=" + v + " isn't auto, fallback or choice: auto");
		return Mode.AUTO;
	}

	/** Whether there is anything to decide: no part, fallback, or auto without a chartex renderer is the Fallback */
	private static boolean undecided(Part host) {
		if (host == null) return false;
		Mode m = mode();
		return m == Mode.CHOICE || m == Mode.AUTO && ChartExRenderer.getRenderer() != null;
	}

	/**
	 * Whether the outputs draw this Choice of an element in the host's content: true where it holds
	 * a chartex chart's frame and {@value #PROPERTY} says so (see the class's note)
	 *
	 * @param host the part whose relationships hold the chart (the conversion's current part)
	 */
	public static boolean takesChoice(Part host, AlternateContent.Choice choice) {
		if (choice == null || !undecided(host)) return false;
		return takes(chartEx(host, choice.getAny()));
	}

	/**
	 * {@link #takesChoice(Part, AlternateContent.Choice)} for an {@code mc:Choice} as DOM (the XSLT
	 * pathways')
	 */
	public static boolean takesChoice(Part host, Element choice) {
		if (choice == null || !undecided(host)) return false;
		// a plain DOM copy: ChartRef reads the frame with getElementsByTagNameNS
		Document d = XmlUtils.getNewDocumentBuilder().newDocument();
		Element copy = (Element) d.importNode(choice, true);
		d.appendChild(copy);
		ChartRef ref = ChartRef.of(host, copy, true);
		return takes(ref == null || !ref.isChartEx() ? null : ref);
	}

	private static boolean takes(ChartRef ref) {
		if (ref == null) return false;
		if (mode() == Mode.CHOICE) return true;
		try {
			ChartRenderCheck check = ChartExRenderer.getRenderer().check(ref.readChartEx(), ref.getHostTheme());
			if (!check.isDrawn()) log.info("The chart " + ref.getName() + " is drawn as its Fallback: " + check);
			return check.isDrawn();
		} catch (Exception e) {
			log.warn("The chart " + ref.getName() + " is drawn as its Fallback: " + e.getMessage(), e);
			return false;
		}
	}

	/** The chartex chart a Choice's content places, or null: a drawing's anchor, a frame, an anchor, a run's */
	private static ChartRef chartEx(Part host, List<Object> content) {
		for (Object o : content) {
			Object u = XmlUtils.unwrap(o);
			if (u instanceof org.docx4j.wml.Drawing) {
				for (Object a : ((org.docx4j.wml.Drawing) u).getAnchorOrInline()) {
					ChartRef ref = ChartRef.of(host, a, true);
					if (ref != null && ref.isChartEx()) return ref;
				}
			} else if (u instanceof ContentAccessor) {
				ChartRef ref = chartEx(host, ((ContentAccessor) u).getContent());
				if (ref != null) return ref;
			} else {
				ChartRef ref = ChartRef.of(host, u, true);
				if (ref != null && ref.isChartEx()) return ref;
			}
		}
		return null;
	}
}
