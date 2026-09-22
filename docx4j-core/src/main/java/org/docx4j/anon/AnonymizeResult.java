package org.docx4j.anon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.parts.Part;

/**
 * What {@link Anonymize#go()} did, and whether the result is clean.
 * <p>
 * Since 17.2.0 (CR-019) the single answer is {@link #isClean()}: true only when
 * every part was scrubbed, replaced or removed, nothing was kept unsafe, and the
 * verification (if it ran) found no token of the original surviving. The
 * per-part record is {@link #getActions()}, and {@link #toJson()} is the
 * machine-readable report the CLI writes.
 */
public class AnonymizeResult {

	/** What was done with a part. */
	public enum Action {
		/** text scrambled, identities and references scrubbed */
		SCRUBBED,
		/** metadata fields cleared */
		CLEARED,
		/** image replaced by placeholder pixels */
		REPLACED,
		/** part removed, with its relationships */
		REMOVED,
		/** structure only: left as it was */
		KEPT,
		/** could not be made clean and was kept (KEEP mode, or a part which failed to load): the result is not clean */
		KEPT_UNSAFE
	}

	/** One part's record. */
	public static class PartAction {
		public final String partName;
		public final String partClass;
		public final Action action;
		public final String reason;

		PartAction(String partName, String partClass, Action action, String reason) {
			this.partName = partName;
			this.partClass = partClass;
			this.action = action;
			this.reason = reason;
		}

		@Override
		public String toString() {
			return partName + ": " + action + (reason == null ? "" : " (" + reason + ")");
		}
	}

	Anonymize.Mode mode;
	final List<PartAction> actions = new ArrayList<PartAction>();
	final List<String> notes = new ArrayList<String>();
	int externalTargetsReplaced = 0;
	int authorsRenamed = 0;

	Boolean verified = null; // null: verification did not run
	final List<Verify.Leak> leaks = new ArrayList<Verify.Leak>();

	public boolean hasGreek = false;
	public boolean hasCyrillic = false;
	public boolean hasHebrew = false;
	public boolean hasArabic = false;
	public boolean hasHiragana = false;
	public boolean hasKatakana = false;
	public boolean hasCJK = false;

	void record(Part p, Action action, String reason) {
		actions.add(new PartAction(p.getPartName().getName(), p.getClass().getSimpleName(), action, reason));
	}

	void record(String partName, String partClass, Action action, String reason) {
		actions.add(new PartAction(partName, partClass, action, reason));
	}

	public Anonymize.Mode getMode() {
		return mode;
	}

	/** Per part: what was done and why. */
	public List<PartAction> getActions() {
		return actions;
	}

	/**
	 * Element-level removals (w:altChunk, o:OLEObject, w:mailMerge ...), one line
	 * each, and anything else the report should say about the whole document - the
	 * conversion of a strict package, for one.
	 */
	public List<String> getNotes() {
		return notes;
	}

	public int getExternalTargetsReplaced() {
		return externalTargetsReplaced;
	}

	public int getAuthorsRenamed() {
		return authorsRenamed;
	}

	/** The parts kept although the tool could not make them clean. */
	public List<PartAction> getKeptUnsafe() {
		List<PartAction> result = new ArrayList<PartAction>();
		for (PartAction a : actions) {
			if (a.action == Action.KEPT_UNSAFE) result.add(a);
		}
		return result;
	}

	/** null if verification did not run; else whether it found no surviving token. */
	public Boolean getVerified() {
		return verified;
	}

	/** The tokens of the original which survived, where. Empty when clean. */
	public List<Verify.Leak> getLeaks() {
		return leaks;
	}

	/**
	 * The guarantee: no part kept unsafe, no unsafe object, and - when
	 * verification ran - no token of the original found in the output.
	 */
	public boolean isClean() {
		return getKeptUnsafe().isEmpty()
				&& !anyUnsafeObjects
				&& (verified == null || verified.booleanValue());
	}

	/**
	 * @deprecated since 17.2.0, use {@link #isClean()}
	 */
	@Deprecated
	public boolean isOK() {
		return isClean();
	}

	/** A JSON document: mode, clean, verified, the actions, the notes, the leaks. */
	public String toJson() {
		StringBuilder sb = new StringBuilder();
		sb.append("{\n");
		sb.append("  \"mode\": ").append(json(mode == null ? null : mode.name())).append(",\n");
		sb.append("  \"clean\": ").append(isClean()).append(",\n");
		sb.append("  \"verified\": ").append(verified == null ? "null" : verified.toString()).append(",\n");
		sb.append("  \"externalTargetsReplaced\": ").append(externalTargetsReplaced).append(",\n");
		sb.append("  \"authorsRenamed\": ").append(authorsRenamed).append(",\n");
		sb.append("  \"scripts\": {");
		sb.append("\"greek\": ").append(hasGreek).append(", \"cyrillic\": ").append(hasCyrillic)
				.append(", \"hebrew\": ").append(hasHebrew).append(", \"arabic\": ").append(hasArabic)
				.append(", \"hiragana\": ").append(hasHiragana).append(", \"katakana\": ").append(hasKatakana)
				.append(", \"cjk\": ").append(hasCJK).append("},\n");
		sb.append("  \"parts\": [\n");
		for (int i = 0; i < actions.size(); i++) {
			PartAction a = actions.get(i);
			sb.append("    {\"part\": ").append(json(a.partName))
					.append(", \"class\": ").append(json(a.partClass))
					.append(", \"action\": ").append(json(a.action.name()))
					.append(", \"reason\": ").append(json(a.reason)).append("}");
			sb.append(i + 1 < actions.size() ? ",\n" : "\n");
		}
		sb.append("  ],\n");
		sb.append("  \"notes\": [");
		for (int i = 0; i < notes.size(); i++) {
			sb.append(i == 0 ? "\n    " : ",\n    ").append(json(notes.get(i)));
		}
		sb.append(notes.isEmpty() ? "],\n" : "\n  ],\n");
		sb.append("  \"leaks\": [");
		for (int i = 0; i < leaks.size(); i++) {
			Verify.Leak l = leaks.get(i);
			sb.append(i == 0 ? "\n    " : ",\n    ")
					.append("{\"part\": ").append(json(l.partName))
					.append(", \"where\": ").append(json(l.where))
					.append(", \"token\": ").append(json(l.token)).append("}");
		}
		sb.append(leaks.isEmpty() ? "]\n" : "\n  ]\n");
		sb.append("}\n");
		return sb.toString();
	}

	static String json(String s) {
		if (s == null) return "null";
		StringBuilder sb = new StringBuilder("\"");
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
			case '"': sb.append("\\\""); break;
			case '\\': sb.append("\\\\"); break;
			case '\n': sb.append("\\n"); break;
			case '\r': sb.append("\\r"); break;
			case '\t': sb.append("\\t"); break;
			default:
				if (c < 0x20) {
					sb.append(String.format("\\u%04x", (int) c));
				} else {
					sb.append(c);
				}
			}
		}
		return sb.append('"').toString();
	}

	// ---- the pre-17.2.0 inventory, still populated

	/**
	 * The parts the tool could not make clean (in STRICT mode they were removed,
	 * so this is the record of what went; in KEEP mode they are still in the package).
	 */
	public HashSet<Part> getUnsafeParts() {
		return unsafeParts;
	}
	HashSet<Part> unsafeParts = new HashSet<Part>();

	/**
	 * Objects flagged by the inventory walk, by story part.
	 */
	public HashMap<Part, Set<Object>> getUnsafeObjectsByPart() {
		return unsafeObjectsByPart;
	}
	HashMap<Part, Set<Object>> unsafeObjectsByPart = new HashMap<Part, Set<Object>>();

	boolean anyUnsafeObjects = false;

	public boolean hasAnyUnsafeObjects() {
		return anyUnsafeObjects;
	}

	/**
	 * Misc interesting objects, by story part, for further consideration
	 */
	public HashMap<Part, Set<Object>> getInventoryObjectsByPart() {
		return inventoryObjectsByPart;
	}
	HashMap<Part, Set<Object>> inventoryObjectsByPart = new HashMap<Part, Set<Object>>();

	/**
	 * Whether this docx contains VML
	 */
	public boolean containsVML() {
		return containsVML;
	}
	boolean containsVML = false;

	HashSet<String> fieldsPresent = new HashSet<String>();

	/** The field instructions found (before scrubbing). */
	public HashSet<String> getFieldsPresent() {
		return fieldsPresent;
	}

	public String reportUnsafeObjects() {

		StringBuilder sb = new StringBuilder();

		if (hasAnyUnsafeObjects()) {
			sb.append("The following objects may leak info: \n");
			for (Entry<Part, Set<Object>> entry : getUnsafeObjectsByPart().entrySet()) {

				Part p = entry.getKey();

				if (!entry.getValue().isEmpty()) {
					sb.append(p.getPartName().getName() + ", of type " + p.getClass().getName() + "\n");

					for (Object o : entry.getValue()) {

						if (o instanceof String) {
							sb.append(o + "\n");
						} else {
							sb.append(o.getClass().getName());
							try {
								sb.append(XmlUtils.marshaltoString(o) + "\n");
							} catch (Exception e) {
								sb.append("\n");
							}
						}

					}
				}

			}
		}
		return sb.toString();

	}

	/** A human-readable summary: clean or not, and why. */
	public String summary() {
		StringBuilder sb = new StringBuilder();
		sb.append(isClean() ? "CLEAN" : "NOT CLEAN").append(" (mode ").append(mode).append(")\n");
		for (PartAction a : actions) {
			sb.append("  ").append(a).append("\n");
		}
		for (String n : notes) {
			sb.append("  - ").append(n).append("\n");
		}
		if (externalTargetsReplaced > 0) {
			sb.append("  external targets replaced: ").append(externalTargetsReplaced).append("\n");
		}
		if (authorsRenamed > 0) {
			sb.append("  authors renamed: ").append(authorsRenamed).append("\n");
		}
		if (verified != null) {
			sb.append("  verified: ").append(verified).append(leaks.isEmpty() ? "" : ", leaks: " + leaks.size()).append("\n");
			for (Verify.Leak l : leaks) {
				sb.append("    ").append(l).append("\n");
			}
		}
		sb.append(reportUnsafeObjects());
		return sb.toString();
	}

}
