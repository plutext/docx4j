package org.jvnet.jaxb2_commons.xpath_tracker;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;

import java.util.Map;
import java.util.HashMap;

/**
 * Keeps track of the number of siblings element found so far.
 *
 * @author Kohsuke Kawaguchi
 */
public final class Histgram {
	
	private static Logger log = Logger.getLogger(Histgram.class);
	
    private final Map<QName,Integer> occurrence = new HashMap<QName, Integer>();

    private String current;
    private int currentValue;

    public void update(String uri, String localName, String qName) {
        QName qn = null;
        if (uri.equals("http://schemas.openxmlformats.org/spreadsheetml/2006/main")) {
        	// Avoid using the default namespace
        	qn = new QName(uri,localName, "s");
        	qName = qn.getPrefix() + ":" + qn.getLocalPart();
        	if (!qn.getPrefix().equals("s")) { // only a problem with MOXy
            	log.debug("ignoring requested prefix!");        		
        	}
        } else {
//        	log.debug("'" + uri +"'");
        	qn = new QName(uri,localName);
        }
        Integer v = occurrence.get(qn);
        if(v==null) {
        	v=1;
        } else {
        	v++;
        }

        occurrence.put(qn,v);
        current = qName;
        currentValue = v;
    }

    public void appendPath(StringBuilder buf) {
        if(current==null)
            return; // this is the head
        buf.append('/');
        buf.append(current);
        buf.append('[');
        buf.append(currentValue);
        buf.append(']');
    }
}
