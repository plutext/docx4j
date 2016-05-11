package org.docx4j.model.styles;

import org.docx4j.XmlUtils;
import org.docx4j.wml.Style;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author jharrop
 * @since 3.2.0
 */
public class BrokenStyleRemediator {

	/* Microsoft's SSRS creates styles such as:
	 * 
		  <w:style>
		    <w:name w:val="EmptyCellLayoutStyle"/>
		    <w:basedOn w:val="Normal"/>
		    <w:rPr>
		      <w:sz w:val="2"/>
		    </w:rPr>
		  </w:style>
		  
		  missing type and id!
		  
  	 */
	
	protected static Logger log = LoggerFactory.getLogger(BrokenStyleRemediator.class);
	
    public static void remediate(Style s) {
    	
    	if (s.getStyleId()==null) {

            if(log.isWarnEnabled()) {
                log.warn("Style is missing ID(!)");
                log.warn(XmlUtils.marshaltoString(s));
            }
    		
    		// Set the ID to the name
    		if (s.getName()!=null
    				&& s.getName().getVal()!=null) {
      	  		log.warn("remediating");
    			s.setStyleId(s.getName().getVal());
    		} else {
      	  		log.warn(".. ignoring");    			
    			return;
    		}
    	}
    	
      if (s.getType()==null) {

  		log.warn("Style is missing type");
  		
  		if (s.getBasedOn()!=null
  				&& s.getBasedOn().getVal()!=null
  				&& s.getBasedOn().getVal().equals("Normal")) {
  			
  	  		log.warn("remediating");
  	  		s.setType("paragraph");
  		
		} else {
			// Does type have a default value?
  	  		log.warn(".. ignoring");    			
			return;
		}  		
      }	
      
    }

}
