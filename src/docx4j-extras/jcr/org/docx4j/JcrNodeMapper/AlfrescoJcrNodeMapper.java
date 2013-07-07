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
import javax.jcr.Property;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.utils.BufferUtil;
import java.io.IOException;

public class AlfrescoJcrNodeMapper implements NodeMapper {

	private static Logger log = LoggerFactory.getLogger(AlfrescoJcrNodeMapper.class);	
	
	public  Node getContentNode(Node node)  throws PathNotFoundException, RepositoryException {
		
		// Do nothing - Alfresco doesn't have jcr:content nodes.
		return node;
		
	}

	
	public  Node addFileNode(Node baseNode, String partName )  throws PathNotFoundException, RepositoryException {
		
		/* Alfresco's contentModel.xml defines:
		 * 
			<type name="cm:content">
		         <title>Content</title>
		         <parent>cm:cmobject</parent>
		         <archive>true</archive>
		         <properties>
		            <property name="cm:content">
		               <type>d:content</type>
		               <mandatory>false</mandatory>
						:
		            </property>
		         </properties>
		      </type>
		 */
		
		return baseNode.addNode(partName, "cm:content" ); 
		
	}

	public  Node addFolder(Node baseNode, String partName )  throws PathNotFoundException, RepositoryException {
				
		return baseNode.addNode(partName, "cm:folder" ); 
		
	}
	
	
	
	public Property getJcrData(Node contentNode) throws PathNotFoundException,
			RepositoryException {

		log.info("getting {http://www.alfresco.org/model/content/1.0}content");
		// Jackrabbit's jcr:data = Alfresco's cm:content

		/*
		 * return
		 * contentNode.getProperty("{http://www.alfresco.org/model/content/1.0}content");
		 * results in: 17.01.2008 22:42:15 *INFO * NodeImpl: Asked for
		 * property:{http://www.alfresco.org/model/content/1.0}content
		 * (NodeImpl.java, line 484)
		 * org.alfresco.service.namespace.InvalidQNameException: A QName must
		 * consist of a local name at
		 * org.alfresco.service.namespace.QName.createQName(QName.java:88) at
		 * org.alfresco.service.namespace.QName.createQName(QName.java:125) at
		 * org.alfresco.jcr.item.JCRPath$SimpleElement.<init>(JCRPath.java:118)
		 * at org.alfresco.jcr.item.JCRPath.<init>(JCRPath.java:70)
		 */

		return contentNode.getProperty("cm:content");

	}

	public byte[] getJcrDataAsBytes(Node contentNode) 
	 throws PathNotFoundException, RepositoryException, IOException {
		
		Property jcrData = getJcrData(contentNode);
		return BufferUtil.getBytesFromInputStream(
				jcrData.getStream() );
	}

	public String getJcrDataAsString(Node contentNode) 
	 throws PathNotFoundException, RepositoryException, IOException {
		
		// With Alfresco, you can't just use .getString()!
		// See ALFCOM-3049
		// Hence this long winded approach..
		
		Property jcrData = getJcrData(contentNode);
		byte[] bytes = BufferUtil.getBytesFromInputStream(
				jcrData.getStream() );
		return new String(bytes, "UTF-8"); // TODO: use encoding specified on the content object?
		
	}
	
	
	
	public void setJcrDataProperty(Node cmContentNode, java.io.InputStream is) throws Exception {
		// Alfresco has property named cm:content, not jcr:data
        cmContentNode.setProperty("cm:content", is );		
	}
	public void setJcrDataProperty(Node cmContentNode, String str) throws Exception {
		// Alfresco has property named cm:content, not jcr:data
        cmContentNode.setProperty("cm:content", str );		
	}
	
	public Node getFrozenNode(Version version) throws PathNotFoundException, RepositoryException {
		javax.jcr.NodeIterator ni = version.getNodes();
		return ni.nextNode();
		
	}

}
