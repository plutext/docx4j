/*
   Licensed to Plutext Pty Ltd under one or more contributor license agreements.  
   
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
package org.docx4j.convert.out.common;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** There is one instance of the WriterRegistry for each output type (eg. FO, HTML). 
 * The WriterRegistry manages the Writers and
 * then converts dom nodes using the registered @see org.docx4j.convert.out.common.Writer
 * Writer classes on the other hand are customized, they are to be registered by the caller, and
 * they are singletons.  Registration uses the node name as key value.
 * <p>Here is an example how you might want to call Converter from an
 * xslt file <pre>
 * <xsl:template match="foo">
 *   <xsl:variable name="me" select="."/>
 *   <xsl:variable name="children">
 *      <xsl:apply-templates/>
 *   </xsl:variable>
 *   <xsl:copy-of select="java:org.docx4j.convert.out.Converter.toNode($me, $children)"/>
 *   ...
 * </pre>
 * This assumes that a writer is registered for "foo".
 * 
 *  @author Adam Schmideg
 *  
 */
public abstract class AbstractWriterRegistry {
	private final static Logger log = LoggerFactory.getLogger(AbstractWriterRegistry.class);
	
	private Map<String, Writer> writerInstances = new HashMap<String, Writer>();

	public AbstractWriterRegistry() {
		registerDefaultWriterInstances();
	}
	
	protected abstract void registerDefaultWriterInstances();
	
	public void registerWriter(Writer converter) {
	    writerInstances.put(converter.getID(), converter);
	}
	
	/** Factory Method to create the required Transform States for the context
	 * 
	 * @return
	 */
	public Map<String, Writer.TransformState> createTransformStates() {
	Map<String, Writer.TransformState> ret = new HashMap<String, Writer.TransformState>();
	Writer.TransformState state = null;
		for (Map.Entry<String, Writer> item : writerInstances.entrySet()) {
			state = item.getValue().createTransformState();
			if (state != null) {
				ret.put(item.getKey(), state);
			}
		}
		return ret;
	}
	  
	/** This method is called from the converters with XSL-Transformations
	 * 
	 * @param node
	 * @param childResults the already transformed node (element) content
	 * @param doc
	 * @return
	 */
	public Node toNode(AbstractWmlConversionContext context, Node node, NodeList childResults) {
		Object unmarshalledNode = null;
	    Document doc = XmlUtils.neww3cDomDocument();
	    /* A result tree fragment represents a fragment of the result tree. 
	       A result tree fragment is treated equivalently to a node-set that 
	       contains just a single root node.
	    */ 
	    Node content = ((childResults != null) && (childResults.getLength() > 0) ?
	    		         childResults.item(0) : null);
		Node ret = null;
		try {
			unmarshalledNode = XmlUtils.unmarshal(node);
		} catch (JAXBException e) {
			log.error("Cannot unmarshall " + node.getNodeName(), e);
		}
		if (unmarshalledNode != null) {
			ret = toNode(context, unmarshalledNode, node.getNodeName(), content, doc);
		}
		return ret;
	}
	
	/** This method is called from the converters that don't use XSLT 
	 * 
	 * @param node
	 * @param childResults the already transformed node (element) content
	 * @param doc
	 * @return
	 */
	public Node toNode(AbstractWmlConversionContext context, Object unmarshalledNode, String modelID, Node content, Document doc) {
		Node ret = null;
		try {
			Writer converter = writerInstances.get(modelID);
			if (converter == null) {

				// We don't have a converter for writing to an output format
				// Either this is a problem ..
				log.warn("No writer registered for " + modelID);
				// .. or the intent is to import to docx
				log.info("Generating wml from model.");
				org.w3c.dom.Document docMarshalled = XmlUtils.marshaltoW3CDomDocument(unmarshalledNode);
				DocumentFragment docfrag = docMarshalled.createDocumentFragment();
				docfrag.appendChild(docMarshalled.getDocumentElement());
				ret = docfrag;

			} else {
				ret = converter.toNode(context, unmarshalledNode, content, context.getTransformState(modelID), doc);
			}
		} catch (Exception e) {
			log.error("Cannot convert " + unmarshalledNode, e);
		}
		return ret;
	}
}
