package org.docx4j.jaxb;

import jakarta.xml.bind.Marshaller;
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

	/** Whether to write docx4j's version comment at the body. */
	private final boolean versionComment;

	/** Where each mc:Choice's own namespace declarations go, or null (CR-052). */
	private final McDeclarations.Plan plan;
    
    public  Docx4jMarshallerListener(XMLStreamWriter xsw, boolean isNewPkg) {
        this(xsw, isNewPkg, true, null);
    }

    /**
     * @param versionComment whether to write docx4j's version comment at the body (a part's save does;
     *     {@code XmlUtils}' marshals do not)
     * @param plan the declarations each mc:Choice gets on its own element, put there through the
     *     {@link org.docx4j.utils.XMLStreamWriterWrapper} the marshal writes to, which {@code xsw}
     *     must then be; null for none (CR-052)
     * @since 17.3.4
     */
    public  Docx4jMarshallerListener(XMLStreamWriter xsw, boolean isNewPkg, boolean versionComment, McDeclarations.Plan plan) {
        this.xsw = xsw;
		this.isNewPkg = isNewPkg;
		this.versionComment = versionComment;
		this.plan = plan;
    }
    
	// Don't try this with an OutputStream: https://stackoverflow.com/questions/52797519/jaxb-marshaller-listener-doesnt-play-nice-with-outputstream

    @Override
    public void beforeMarshal(Object source)  {
    	
    	if (versionComment && Docx4jProperties.getProperty("docx4j.jaxb.marshal.suppressVersionComment", false)==false) {
    		
	        try {
	        	if (source instanceof org.docx4j.wml.Body) {
			        xsw.writeComment(Version.getPoweredBy(isNewPkg));
	        	}
	        } catch(XMLStreamException e) {
	        	log.error(e.getMessage(), e);
	        }
    	}

    	// CR-052: a Choice's own declarations, on its start tag, which both runtimes write after this returns
    	if (plan != null && source instanceof org.docx4j.mce.AlternateContent.Choice
    			&& xsw instanceof org.docx4j.utils.XMLStreamWriterWrapper) {
    		java.util.Map<String, String> decls = plan.onChoice((org.docx4j.mce.AlternateContent.Choice) source);
    		if (decls != null) ((org.docx4j.utils.XMLStreamWriterWrapper) xsw).declareOnNextStartElement(decls);
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
