package org.docx4j.JcrNodeMapper;

import javax.jcr.Node;

import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;


public interface NodeMapper {
	
	public abstract Node getContentNode(Node node) throws PathNotFoundException, RepositoryException;

	public abstract Property getJcrData(Node contentNode) 
	 throws PathNotFoundException, RepositoryException;
}
