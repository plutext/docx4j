package org.docx4j.model.structure;

/**
 * @since 2.7
 */
public enum PageSizePaper {

		    LETTER("letter"),
		    LEGAL("legal"),
		    A3("A3"),
		    A4("A4"),
		    A5("A5"),
		    B4JIS("B4JIS");
		    
		    private final String value;

		    PageSizePaper(String v) {
		        value = v;
		    }

		    public String value() {
		        return value;
		    }
	  }
