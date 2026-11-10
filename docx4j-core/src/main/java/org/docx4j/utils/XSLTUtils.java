package org.docx4j.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XSLTUtils {

	public static Logger log = LoggerFactory.getLogger(XSLTUtils.class);

	public static void logDebug(String message) {
		log.debug(message);
	}
	public static void logInfo(String message) {
		log.info(message);
	}
	/** The unknown elements read through in the part being transformed (CR-076). */
	private static final ThreadLocal<int[]> READ_THROUGH = ThreadLocal.withInitial(() -> new int[1]);

	/**
	 * mc-preprocessor.xslt reads an unknown {@code w:} element through, as Word does.
	 *
	 * @since 17.3.6 (CR-076)
	 */
	public static void noteReadThrough() {
		READ_THROUGH.get()[0]++;
	}

	/**
	 * One WARN a part, with the count of {@link #noteReadThrough()}, which it resets.
	 *
	 * @since 17.3.6 (CR-076 D4)
	 */
	public static void warnReadThrough() {
		int[] count = READ_THROUGH.get();
		if (count[0] > 0) {
			log.warn("Read " + count[0] + " element(s) Word doesn't know as Word reads them, what they hold kept (CR-076)");
			count[0] = 0;
		}
	}

	/**
	 * Whether a namespace is in docx4j's understood set: mc-preprocessor.xslt drops an element
	 * of any other from a text element, as Word drops it.
	 *
	 * @since 17.3.6 (CR-076)
	 */
	public static boolean isUnderstoodNamespace(String uri) {
		return org.docx4j.jaxb.McSelection.understoodNamespaces().contains(uri);
	}

	public static void logWarn(String message) {
		log.warn(message);
	}

	/**
	 * Which {@code mc:Choice} of an {@code mc:AlternateContent} to render, as a
	 * space-separated list of the {@code @Requires} prefixes we can draw; the first
	 * {@code mc:Choice} naming one of them wins, and otherwise the {@code mc:Fallback}
	 * is used as before.  Empty switches the preference off.
	 *
	 * <p>Word draws the first {@code mc:Choice} whose {@code @Requires} names a
	 * namespace it understands, and reaches the {@code mc:Fallback} only when it
	 * understands none of them (ECMA-376 Part 3, &#xa7;10.2.1).  docx4j always took the
	 * fallback.  For {@code Requires="wps"} - a DrawingML shape, whose text box both
	 * exporters do render ({@code wps:wsp/wps:txbx/w:txbxContent}) - that meant taking
	 * the VML {@code w:pict} twin Word never looks at, with the position it carries:
	 * measured on a corpus header holding one such pair, Word draws the box at
	 * x=528.0..581.8 and the fallback's {@code style="left:605.25pt"} put ours at
	 * 519.9..803.5, most of it off the page.</p>
	 *
	 * <p><b>The default is {@code wps wpg wpc}</b> since 17.3.7 (CR-080 D12, D13): a DrawingML shape's
	 * Choice is drawn, which the FO exporter paints as Word does (CR-080 phase 2), with an inline
	 * shape set on its line.  Gate b401 against 17.3.6's default: six documents move, all up
	 * (corpus 14372 a line; five probes, two to 1.0), none down; the HTML visitor writes a text
	 * box's text where the fallback wrote placeholders.  Until 17.3.6 the default was empty,
	 * measured over the 25 documents of three real-document corpora that carry an
	 * {@code mc:Choice Requires="wps"}, choosing it moved two and left 23 untouched, and
	 * both of those two fell (0.932 -&gt; 0.894 and 0.927 -&gt; 0.897, a sum of -0.067):
	 * in each the Choice's DrawingML box lands within 0.6pt of the fallback's and of
	 * Word's, so nothing visible is gained, while the header-and-page-number pair it
	 * draws lands on one baseline instead of two.  The header text box the rule was
	 * written for is drawn correctly either way.  Set {@code docx4j.jaxb.mc.preferChoice=}
	 * (empty) to take the fallback, as until 17.3.6.</p>
	 *
	 * <p>{@code wpg} (shape groups) and {@code wpc} (canvases) are in the default too since
	 * 17.3.7 (CR-080 D13), as docx4j draws them (CR-080 phase 3).  {@code w14}, {@code wp14}
	 * and {@code v} are deliberately not suggested: a Choice we cannot in fact draw is worse
	 * than the fallback, and the fallback is what the producer wrote for exactly this case.</p>
	 *
	 * @since 17.1.0
	 */
	public static String mcPreferredChoiceRequires() {
		// CR-021: the rule lives in McSelection; this is the XSLT-reachable hook
		return org.docx4j.jaxb.McSelection.preferredChoiceRequires();
	}

	/**
	 * Whether an {@code mc:Choice} with this {@code @Requires} is one we prefer to the
	 * {@code mc:Fallback}; see {@link #mcPreferredChoiceRequires()}.
	 *
	 * @since 17.1.0
	 */
	public static boolean mcPrefersChoice(String requires) {
		return org.docx4j.jaxb.McSelection.prefersChoice(requires);
	}

	/**
	 * Whether a {@code Requires} names only namespaces in the understood set, its prefixes read
	 * from the namespace nodes given ({@code namespace::*} of the element).
	 *
	 * @since 17.3.6 (WD REQ-305)
	 */
	public static boolean mcUnderstandsRequires(String requires, org.w3c.dom.traversal.NodeIterator namespaces) {
		java.util.Map<String, String> declared = new java.util.HashMap<String, String>();
		if (namespaces != null) {
			for (org.w3c.dom.Node n = namespaces.nextNode(); n != null; n = namespaces.nextNode()) {
				declared.put(n.getLocalName(), n.getNodeValue());
			}
		}
		return org.docx4j.jaxb.McSelection.understands(requires, declared::get);
	}

	/**
	 * Whether an output draws this {@code mc:Choice} for a chartex chart's
	 * ({@link org.docx4j.model.chart.ChartExBranch}): by default, where a chartex renderer draws
	 * the chart.  The XSLT pathways' {@code mc:AlternateContent} templates ask it of each Choice
	 * {@link #mcPrefersChoice} doesn't take.
	 *
	 * @param conversionContext the stylesheet's: a docx's (its current part) or a pptx's (its slide)
	 * @since 17.3.3 (CR-039)
	 */
	public static boolean mcSelectsChoice(Object conversionContext, org.w3c.dom.traversal.NodeIterator choice) {
		org.w3c.dom.Node n = choice == null ? null : choice.nextNode();
		if (!(n instanceof org.w3c.dom.Element)) return false;
		org.docx4j.openpackaging.parts.Part host = null;
		if (conversionContext instanceof org.docx4j.convert.out.common.AbstractWmlConversionContext) {
			host = ((org.docx4j.convert.out.common.AbstractWmlConversionContext) conversionContext).getCurrentPart();
		} else if (conversionContext instanceof org.pptx4j.convert.out.svginhtml.SvgConversionContext) {
			org.pptx4j.model.ResolvedLayout rl = ((org.pptx4j.convert.out.svginhtml.SvgConversionContext) conversionContext).getResolvedLayout();
			if (rl != null && rl.relationships != null && rl.relationships.getSourceP() instanceof org.docx4j.openpackaging.parts.Part) {
				host = (org.docx4j.openpackaging.parts.Part) rl.relationships.getSourceP();
			}
		}
		return org.docx4j.model.chart.ChartExBranch.takesChoice(host, (org.w3c.dom.Element) n);
	}

}
