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
	 * <p><b>The default is empty - the preference is off.</b>  Measured over the 25
	 * documents of three real-document corpora that carry an
	 * {@code mc:Choice Requires="wps"}, choosing it moved two and left 23 untouched, and
	 * both of those two fell (0.932 -&gt; 0.894 and 0.927 -&gt; 0.897, a sum of -0.067):
	 * in each the Choice's DrawingML box lands within 0.6pt of the fallback's and of
	 * Word's, so nothing visible is gained, while the header-and-page-number pair it
	 * draws lands on one baseline instead of two.  The header text box the rule was
	 * written for is drawn correctly either way.  Set
	 * {@code docx4j.jaxb.mc.preferChoice=wps} to take the Choice where the fallback is
	 * missing or wrong for your documents.</p>
	 *
	 * <p>{@code wpg} (shape groups), {@code w14}, {@code wp14} and {@code v} are
	 * deliberately not suggested: a Choice we cannot in fact draw is worse than the
	 * fallback, and the fallback is what the producer wrote for exactly this case.</p>
	 *
	 * @since 17.1.0
	 */
	public static String mcPreferredChoiceRequires() {
		return org.docx4j.Docx4jProperties.getProperty("docx4j.jaxb.mc.preferChoice", "");
	}

	/**
	 * Whether an {@code mc:Choice} with this {@code @Requires} is one we prefer to the
	 * {@code mc:Fallback}; see {@link #mcPreferredChoiceRequires()}.
	 *
	 * @since 17.1.0
	 */
	public static boolean mcPrefersChoice(String requires) {
		if (requires == null || requires.length() == 0) return false;
		String preferred = mcPreferredChoiceRequires();
		if (preferred == null || preferred.trim().length() == 0) return false;
		java.util.Set<String> ok = new java.util.HashSet<String>(
				java.util.Arrays.asList(preferred.trim().split("\\s+")));
		// @Requires may name several namespace prefixes, all of which must be understood
		for (String needed : requires.trim().split("\\s+")) {
			if (!ok.contains(needed)) return false;
		}
		return true;
	}

}
