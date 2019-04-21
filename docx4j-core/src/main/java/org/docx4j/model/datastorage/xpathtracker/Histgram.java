package org.docx4j.model.datastorage.xpathtracker;

import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.HashMap;

/**
 * Keeps track of the number of siblings element found so far.
 *
 * @author Kohsuke Kawaguchi
 */
public class Histgram {
	
	private static Logger log = LoggerFactory.getLogger(Histgram.class);	
	
    private final Map<QName,Integer> occurrence = new HashMap<QName, Integer>();

    protected String current;
    protected int currentValue;

    public void update(String uri, String localName, String qName) {
    	
    	if (localName.contains(":") /* QName constructor allows that */) {
    		log.error("Unexpected localName " + localName);
    		throw new java.lang.IllegalArgumentException("Unexpected localName " + localName);
    	}    	
    	
        QName qn = new QName(uri,localName);
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
