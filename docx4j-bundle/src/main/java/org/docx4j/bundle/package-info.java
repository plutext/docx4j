/**
 * docx4j-bundle is a shaded "fat" jar: docx4j-core plus the JAXB reference
 * implementation runtime and all of their dependencies, relocated into a single
 * self-contained artifact.
 *
 * <p>It exists as a convenience for environments where assembling a classpath is
 * awkward &mdash; notably calling docx4j from Python via JPype (see
 * {@code docs/Docx4j_from_Python.md}) &mdash; where a single downloadable jar is
 * simpler than a directory of dependencies. For ordinary Maven/Gradle projects,
 * depend on {@code org.docx4j:docx4j-core} plus a JAXB runtime selector instead;
 * do not add this bundle as a normal dependency.</p>
 *
 * <p>For PDF output add {@code docx4j-export-fo} and its dependencies alongside
 * the bundle. This package itself carries no API; it exists so the published
 * artifact ships the sources and javadoc jars Maven Central requires.</p>
 */
package org.docx4j.bundle;
