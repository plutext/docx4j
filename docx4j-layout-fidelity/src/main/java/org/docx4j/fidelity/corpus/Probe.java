package org.docx4j.fidelity.corpus;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/** One probe document: an id (the file basename), a description, and how to build it. */
public final class Probe {

	@FunctionalInterface
	public interface Builder {
		WordprocessingMLPackage build() throws Exception;
	}

	public final String id;
	public final String family;
	public final String description;
	private final Builder builder;

	public Probe(String id, String description, Builder builder) {
		this.id = id;
		this.family = id.contains("-") ? id.substring(0, id.indexOf('-')) : id;
		this.description = description;
		this.builder = builder;
	}

	public WordprocessingMLPackage build() throws Exception {
		return builder.build();
	}
}
