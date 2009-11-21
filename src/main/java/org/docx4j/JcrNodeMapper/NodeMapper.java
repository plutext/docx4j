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

import javax.jcr.Node;

import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import java.io.IOException;


public interface NodeMapper {
	
	public abstract Node getContentNode(Node node) throws PathNotFoundException, RepositoryException;

	public abstract Node addFileNode(Node baseNode, String partName )  throws PathNotFoundException, RepositoryException;

	public abstract Node addFolder(Node baseNode, String partName )  throws PathNotFoundException, RepositoryException;
	
	
	public abstract Property getJcrData(Node contentNode) 
	 throws PathNotFoundException, RepositoryException;

	public abstract byte[] getJcrDataAsBytes(Node contentNode) 
	 throws PathNotFoundException, RepositoryException, IOException;

	public abstract String getJcrDataAsString(Node contentNode) 
	 throws PathNotFoundException, RepositoryException, IOException;

	public void setJcrDataProperty(Node cmContentNode, java.io.InputStream is) 
	throws Exception;
	
	public void setJcrDataProperty(Node cmContentNode, String str) 
	throws Exception;
	
	public Node getFrozenNode(Version version) throws PathNotFoundException, RepositoryException;	
	
}
