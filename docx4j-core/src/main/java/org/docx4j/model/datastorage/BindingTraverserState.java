package org.docx4j.model.datastorage;

import java.util.LinkedList;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.docx4j.jaxb.Context;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

/**
 * @author jharrop
 * @since 3.3.0
 */
public class BindingTraverserState {
	
	private static Logger log = LoggerFactory.getLogger(BindingTraverserState.class);
	
	
	LinkedList<Tc> tcStack = new LinkedList<Tc>();
	LinkedList<Tbl> tblStack = new LinkedList<Tbl>();
	
	public static void enteredTc(BindingTraverserState btState, NodeIterator nodeIterator) {
		
		Node n = nodeIterator.nextNode(); //It is never null
		if (n!=null) {
			try {
    			Unmarshaller u = Context.jc.createUnmarshaller();			
    			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());
    			Object jaxb = u.unmarshal(n);
    			btState.tcStack.push( (Tc)jaxb );
			} catch (ClassCastException e) {
				log.error("Couldn't cast  to RPr!");
			} catch (JAXBException e) {
				log.error(e.getMessage(), e);
			}        	        			
		}		
		
	}

	public static void exitedTc(BindingTraverserState btState) {
		
		btState.tcStack.pop();
	}
	
    public static void enteredTbl(BindingTraverserState btState, NodeIterator nodeIterator) {
        Node n = nodeIterator.nextNode(); //It is never null
        if (n != null) {
            try {
                Unmarshaller u = Context.jc.createUnmarshaller();
                u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());
                Object jaxb = u.unmarshal(n);
                btState.tblStack.push( (Tbl)jaxb);
            } catch (ClassCastException e) {
                log.error("Couldn't cast to Tbl!");
            } catch (JAXBException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public static void exitedTbl(BindingTraverserState btState) {
        btState.tblStack.pop();
    }
	
	/**
	 *  Our cache of XPath values, available 
	 *  if ENABLE_XPATH_CACHE = true.
	 *  
	 *  @since 3.3.1
	 */
	private Map<String, String> pathMap;

	protected Map<String, String> getPathMap() {
		return pathMap;
	}

	protected void setPathMap(Map<String, String> pathMap) {
		this.pathMap = pathMap;
	}
	
}
