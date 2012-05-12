package org.docx4j.model.structure;

/**
 * These margin settings reflect the Word 2007 user interface.
 * 
 * w:header="708" w:footer="708" w:gutter="0" are all invariant
 * across these
 * 
 *  Normal: all are 1 inch = 2.54 cm
 *  Narrow: all are 1/2 inch
 *  Wide: left/right are 2 inch
 * 
 * @since 2.7
 */
public enum MarginsWellKnown {

		    NORMAL,     // <w:pgMar w:top="1440" w:right="1440" w:bottom="1440" w:left="1440" w:header="708" w:footer="708" w:gutter="0"/>
		    NARROW,     // <w:pgMar w:top="720"  w:right="720"  w:bottom="720"  w:left="720" w:header="708" w:footer="708" w:gutter="0"/>
		    MODERATE, // <w:pgMar w:top="1440" w:right="1080" w:bottom="1440" w:left="1080" w:header="708" w:footer="708" w:gutter="0"/>
		    WIDE;         // <w:pgMar w:top="1440" w:right="2880" w:bottom="1440" w:left="2880" w:header="708" w:footer="708" w:gutter="0"/>
		    
	  }
