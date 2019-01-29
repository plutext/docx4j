package org.docx4j.events;

/**
 * Used where we don't have a PackageIdentifier yet eg where package is being loaded.
 *
 */
public class PackageIdentifierTransient implements PackageIdentifier {
	
	private String name;
	public String name() {
		return name;
	}	
	
	public PackageIdentifierTransient(String name) {
		this.name = name;
	}

}
