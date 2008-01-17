package org.docx4j.JcrNodeMapper;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

public class JackrabbitJcrNodeMapper implements NodeMapper {
	
	public  Node getContentNode(Node node)  throws PathNotFoundException, RepositoryException{
		
		return node.getNode("jcr:content");
		
	}
	
	public  Property getJcrData(Node contentNode) 
	 throws PathNotFoundException, RepositoryException {
			
			// Jackrabbit's jcr:data = Alfresco's cm:content
			return contentNode.getProperty("jcr:data");
			
		}

}
