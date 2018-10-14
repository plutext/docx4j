package org.docx4j.jaxb;

import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.docx4j.Docx4jProperties;
import org.docx4j.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Docx4jMarshallerListener extends Marshaller.Listener {
	
	protected static Logger log = LoggerFactory.getLogger(Docx4jMarshallerListener.class);

    private XMLStreamWriter xsw;
    
	boolean isNewPkg = true;	
    
    public  Docx4jMarshallerListener(XMLStreamWriter xsw, boolean isNewPkg) {
        this.xsw = xsw;
		this.isNewPkg = isNewPkg;
    }
    
	// Don't try this with an OutputStream: https://stackoverflow.com/questions/52797519/jaxb-marshaller-listener-doesnt-play-nice-with-outputstream

    @Override
    public void beforeMarshal(Object source)  {
    	
    	if (Docx4jProperties.getProperty("docx4j.jaxb.marshal.suppressVersionComment", false)==false) {
    		
	        try {
	        	if (source instanceof org.docx4j.wml.Body) {
			        xsw.writeComment(Version.getPoweredBy(isNewPkg));
	        	}
	        } catch(XMLStreamException e) {
	        	log.error(e.getMessage(), e);
	        }
    	}
    }
    
//    @Override
//    public void afterMarshal(Object source) {
//        try {
//            xsw.writeComment("After:  " + source.toString());
//        } catch(XMLStreamException e) {
//            // TODO: handle exception
//        }
//    }

}
