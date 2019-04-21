package org.docx4j.model.datastorage;

public class XPathConfiguration {

	private String prefix;
	private int index;
	
	public String getPrefix() {
		return prefix;
	}

	public int getIndex() {
		return index;
	}

	public XPathConfiguration(String prefix, int index) {
		super();
		this.prefix = prefix;
		this.index = index;
	}
}
