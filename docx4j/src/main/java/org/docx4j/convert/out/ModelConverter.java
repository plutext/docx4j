package org.docx4j.convert.out;

import javax.xml.transform.TransformerException;

import org.docx4j.model.Model;
import org.w3c.dom.Node;

public abstract class ModelConverter {
	
	
	  public void start() {
	  }
	  
	  public void stop() {
	  }
	  
	  private Converter mainConverter;
	  public void setMainConverter(Converter c) {		  
		  mainConverter = c;
	  }
	  public Converter getMainConverter() {
		  return mainConverter;
	  }
	  
	  public abstract Node toNode(Model m) throws TransformerException;

}
