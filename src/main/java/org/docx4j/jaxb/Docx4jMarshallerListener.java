package org.docx4j.jaxb;

import javax.xml.bind.Marshaller;
import javax.xml.stream.*;

import org.docx4j.Version;

public class Docx4jMarshallerListener extends Marshaller.Listener {

    private XMLStreamWriter xsw;

    public  Docx4jMarshallerListener(XMLStreamWriter xsw) {
        this.xsw = xsw;
    }

    @Override
    public void beforeMarshal(Object source)  {
        try {
        	if (source instanceof org.docx4j.wml.Body) {
        		xsw.writeComment("This docx was marshalled (via JAXB) using docx4j " + Version.getDocx4jVersion() );
        	}
        } catch(XMLStreamException e) {
            // TODO: handle exception
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
