package org.docx4j.JcrNodeMapper;

import javax.jcr.Node;

import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;


public interface NodeMapper {
	
	public abstract Node getContentNode(Node node) throws PathNotFoundException, RepositoryException;

	public abstract Node addFileNode(Node baseNode, String partName )  throws PathNotFoundException, RepositoryException;
	
	
	public abstract Property getJcrData(Node contentNode) 
	 throws PathNotFoundException, RepositoryException;
	
	public void setJcrDataProperty(Node cmContentNode, java.io.InputStream is) 
	throws Exception;
	
}
