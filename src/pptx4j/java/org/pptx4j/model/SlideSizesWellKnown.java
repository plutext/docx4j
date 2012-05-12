package org.pptx4j.model;

public enum SlideSizesWellKnown {

	  LETTER("letter"),
	  A3("A3"),
	  A4("A4"),
	  B4JIS("B4JIS"),
	  SCREEN4x3("screen4x3"),
	  SCREEN16x9("screen16x9"),
	  SCREEN16x10("screen16x10"),
	  LEDGER("ledger"),
	  B4ISO("B4ISO"),
	  B5ISO("B5ISO"),
	  MM35("35mm"),
	  OVERHEAD("overhead"),
	  BANNER("banner");
    
    private final String value;

    SlideSizesWellKnown(String v) {
        value = v;
    }

    public String value() {
        return value;
    }
}
