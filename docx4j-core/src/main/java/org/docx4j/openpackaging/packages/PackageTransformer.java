package org.docx4j.openpackaging.packages;

import java.util.List;
import java.util.Objects;

import org.docx4j.openpackaging.exceptions.Docx4JException;

import static java.util.stream.Collectors.toList;

/**
 * Implementations can perform a variety of tasks such as transforming, filtering, enriching, etc.
 * Inspired by dev.langchain4j.data.document.DocumentTransformer
 * @since 11.5.1
 */
public interface PackageTransformer {

    /**
     * Transforms the provided package.  An implementation may alter the incoming srcPkg,
     * so callers may wish to pass in a clone.
     *
     * @param pkg The package to be transformed.
     * @return The transformed package, or null if the package should be filtered out.
     */
	OpcPackage transform(OpcPackage srcPkg) throws Docx4JException;

    /**
     * Transforms all the provided documents.
     *
     * @param pkgs A list of OpcPackage to be transformed.
     * @return A list of transformed OpcPackage. The length of this list may be shorter or longer than the original list. 
     * Returns an empty list if all OpcPackage were filtered out.
     */
    default List<OpcPackage> transformAll(List<OpcPackage> pkgs) throws Docx4JException {
        return pkgs.stream()
                .map(arg0 -> {
					try {
						return transform(arg0);
					} catch (Docx4JException e) {
						return null;
					}
				})
                .filter(Objects::nonNull)
                .collect(toList());
    }
}
