package org.docx4j.jaxb.moxy;

import org.eclipse.persistence.oxm.XMLUnmarshalListener;
import javax.xml.bind.Unmarshaller.Listener;

/**
 * @author jharrop
 * @since 11.4.7
 */
public class MoxyUnmarshalListener implements XMLUnmarshalListener {

	private Listener docx4jListener;
	
	public MoxyUnmarshalListener(Listener listener) {
		this.docx4jListener = listener;
	}
	
	
	@Override
	public void afterUnmarshal(Object target, Object parent) {
		docx4jListener.afterUnmarshal(target, parent);
		
	}

	@Override
	public void beforeUnmarshal(Object target, Object parent) {
		docx4jListener.beforeUnmarshal(target, parent);
		
	}

}
