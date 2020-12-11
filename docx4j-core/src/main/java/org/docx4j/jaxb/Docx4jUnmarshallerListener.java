package org.docx4j.jaxb;

import javax.xml.bind.Unmarshaller;

import org.docx4j.mce.AlternateContent;
import org.docx4j.openpackaging.parts.JaxbXmlPart;

public class Docx4jUnmarshallerListener extends Unmarshaller.Listener {

	private JaxbXmlPart part;

	public Docx4jUnmarshallerListener(JaxbXmlPart part) {
		this.part = part;
	}

//	@Override
//	public void beforeUnmarshal(Object target, Object parent) {
//
//	}

  @Override
  public void afterUnmarshal(Object target, Object parent) {
	  
		if (target instanceof AlternateContent.Choice) {
			
			AlternateContent.Choice choice = (AlternateContent.Choice)target;
			//System.out.print("after, Need to declare " + choice.getRequires() );
			
			if (choice.getRequires()!=null) {
				part.addMcChoiceNamespace(choice.getRequires());
			}
			if (choice.getIgnorable()!=null) {
				part.addMcChoiceNamespace(choice.getIgnorable());
			}
			if (choice.getMustUnderstand()!=null) {
				part.addMcChoiceNamespace(choice.getMustUnderstand());
			}
		}
	}

}
