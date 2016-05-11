package org.docx4j.model.datastorage.xpathtracker;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.HashMap;

/**
 * Keeps track of the number of siblings element found so far.
 *
 * @author Kohsuke Kawaguchi
 */
public final class Histgram {
    private final Map<QName,Integer> occurrence = new HashMap<QName, Integer>();

    private String current;
    private int currentValue;

    public void update(String uri, String localName, String qName) {
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
