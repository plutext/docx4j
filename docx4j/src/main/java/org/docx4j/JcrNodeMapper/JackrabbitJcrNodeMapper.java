package org.docx4j.JcrNodeMapper;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

public class JackrabbitJcrNodeMapper implements NodeMapper {
	
	public  Node getContentNode(Node node)  throws PathNotFoundException, RepositoryException{
		
		return node.getNode("jcr:content");
		
	}
	
	public  Node addFileNode(Node baseNode, String partName )  throws PathNotFoundException, RepositoryException {
		
		return baseNode.addNode(partName, "nt:file" );
		
	}
	
	
	public  Property getJcrData(Node contentNode) 
	 throws PathNotFoundException, RepositoryException {
			
			// Jackrabbit's jcr:data = Alfresco's cm:content
			return contentNode.getProperty("jcr:data");
			
		}

	public void setJcrDataProperty(Node cmContentNode, java.io.InputStream is) throws Exception {
		// Alfresco has property named cm:content, not jcr:data
        cmContentNode.setProperty("jcr:data", is );		
	}
	
	public void setJcrDataProperty(Node cmContentNode, String str) throws Exception {
		// Alfresco has property named cm:content, not jcr:data
        cmContentNode.setProperty("jcr:data", str );		
	}
	
}
