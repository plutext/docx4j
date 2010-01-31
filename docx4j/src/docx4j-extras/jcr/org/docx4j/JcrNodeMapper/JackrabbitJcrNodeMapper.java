/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License. 

    You may obtain a copy of the License at 

        http://www.apache.org/licenses/LICENSE-2.0 

    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS, 
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
    See the License for the specific language governing permissions and 
    limitations under the License.

 */
package org.docx4j.JcrNodeMapper;

import java.io.IOException;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;

import org.docx4j.utils.BufferUtil;

public class JackrabbitJcrNodeMapper implements NodeMapper {
	
	public  Node getContentNode(Node node)  throws PathNotFoundException, RepositoryException{
		
		return node.getNode("jcr:content");
		
	}
	
	public  Node addFileNode(Node baseNode, String partName )  throws PathNotFoundException, RepositoryException {
		
		return baseNode.addNode(partName, "nt:file" );
		
	}

	public  Node addFolder(Node baseNode, String partName )  throws PathNotFoundException, RepositoryException {
		
		return baseNode.addNode(partName, "nt:folder" );
		
	}
	
	
	public  Property getJcrData(Node contentNode) 
	 throws PathNotFoundException, RepositoryException {
			
			// Jackrabbit's jcr:data = Alfresco's cm:content
			return contentNode.getProperty("jcr:data");
			
		}
	
	public byte[] getJcrDataAsBytes(Node contentNode) 
	 throws PathNotFoundException, RepositoryException, IOException {
		
		Property jcrData = getJcrData(contentNode);
		return BufferUtil.getBytesFromInputStream(
				jcrData.getStream() );
	}

	public String getJcrDataAsString(Node contentNode) 
	 throws PathNotFoundException, RepositoryException, IOException {
				
		return getJcrData(contentNode).getString();		
	}
	

	public void setJcrDataProperty(Node cmContentNode, java.io.InputStream is) throws Exception {
		// Alfresco has property named cm:content, not jcr:data
        cmContentNode.setProperty("jcr:data", is );		
	}
	
	public void setJcrDataProperty(Node cmContentNode, String str) throws Exception {
		// Alfresco has property named cm:content, not jcr:data
        cmContentNode.setProperty("jcr:data", str );		
	}
	
	public Node getFrozenNode(Version version) throws PathNotFoundException, RepositoryException {
		return version.getNode("jcr:frozenNode");
	}
	
}
