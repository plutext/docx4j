package org.docx4j.convert.out.XSLFO;

import java.math.BigInteger;

import org.apache.log4j.Logger;
import org.docx4j.wml.CTPageNumber;
import org.docx4j.wml.NumberFormat;
import org.docx4j.wml.SectPr;

public class PageNumberHelper {
	
	public static Logger log = Logger.getLogger(PageNumberHelper.class);	
	

    public static String getPageNumberFormat(SectPr sectPr) {
    	
    	if (sectPr==null) return "1";
    	
    	CTPageNumber pageNumber = sectPr.getPgNumType();
    	
    	if (pageNumber==null) return "1";
    	
    	NumberFormat format = pageNumber.getFmt();
    	
    	if (format==null) return "1";
    	
    	log.debug("w:pgNumType/@w:fmt=" + format.toString());
    	
//    	 *     &lt;enumeration value="decimal"/>
//    	 *     &lt;enumeration value="upperRoman"/>
//    	 *     &lt;enumeration value="lowerRoman"/>
//    	 *     &lt;enumeration value="upperLetter"/>
//    	 *     &lt;enumeration value="lowerLetter"/>    	
    	if (format==NumberFormat.DECIMAL)
    		return "1";
    	else if (format==NumberFormat.UPPER_ROMAN)
    		return "I";
    	else if (format==NumberFormat.LOWER_ROMAN)
    		return "i";
    	//else if (format.equals(NumberFormat.UPPER_LETTER))
    	else if (format==NumberFormat.UPPER_LETTER)
    		return "A";
    	else if (format==NumberFormat.LOWER_LETTER)
    		return "a";

        // TODO .. other formats
    		
    	return "1";
    }
	
    public static String getPageNumberInitial(SectPr sectPr) {

    	if (sectPr==null) return "1";
    	
    	CTPageNumber pageNumber = sectPr.getPgNumType();
    	
    	if (pageNumber==null) {
    		log.debug("No PgNumType");
    		return "1";
    	}
    	
    	BigInteger start = pageNumber.getStart();
    	
    	if (start==null) return "1";
    	
    	return start.toString();
    }
	
}
