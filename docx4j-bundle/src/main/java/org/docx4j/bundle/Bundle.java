package org.docx4j.bundle;

/**
 * Marker type for the docx4j shaded "fat" jar.
 *
 * <p>This class carries no functionality. The value of {@code docx4j-bundle} is
 * the packaged classpath &mdash; docx4j-core plus the JAXB reference
 * implementation runtime and their dependencies in one self-contained artifact
 * &mdash; not any API declared here. It exists so the published artifact has a
 * public type to document, satisfying Maven Central's requirement that a
 * component ship non-empty sources and javadoc jars.</p>
 *
 * <p>See {@code org.docx4j.bundle} package documentation, and
 * {@code design-rationale.md} in the module root, for why the bundle is shaped
 * this way.</p>
 */
public final class Bundle {

	private Bundle() {
	}
}
