package org.docx4j.JcrNodeMapper;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.log4j.Logger;
import org.merlin.io.OutputEngineInputStream;

public class AlfrescoJcrNodeMapper implements NodeMapper {

	private static Logger log = Logger.getLogger(AlfrescoJcrNodeMapper.class);	
	
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
	
	
	
	public  Property getJcrData(Node contentNode) 
 throws PathNotFoundException, RepositoryException {
		
		log.info("getting {http://www.alfresco.org/model/content/1.0}content"); 
		System.out.println("getting {http://www.alfresco.org/model/content/1.0}content"); 
		// Jackrabbit's jcr:data = Alfresco's cm:content
		
/*		
		return contentNode.getProperty("{http://www.alfresco.org/model/content/1.0}content");
  results in:
		17.01.2008 22:42:15 *INFO * NodeImpl: Asked for property:{http://www.alfresco.org/model/content/1.0}content (NodeImpl.java, line 484)
			org.alfresco.service.namespace.InvalidQNameException: A QName must consist of a local name
			        at org.alfresco.service.namespace.QName.createQName(QName.java:88)
			        at org.alfresco.service.namespace.QName.createQName(QName.java:125)
			        at org.alfresco.jcr.item.JCRPath$SimpleElement.<init>(JCRPath.java:118)
			        at org.alfresco.jcr.item.JCRPath.<init>(JCRPath.java:70)
 */
		
		return contentNode.getProperty("cm:content");
		
	}
	
	
	public void setJcrDataProperty(Node cmContentNode, java.io.InputStream is) throws Exception {
		// Alfresco has property named cm:content, not jcr:data
        cmContentNode.setProperty("cm:content", is );		
	}
	public void setJcrDataProperty(Node cmContentNode, String str) throws Exception {
		// Alfresco has property named cm:content, not jcr:data
        cmContentNode.setProperty("cm:content", str );		
	}

}
