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
package org.docx4j.convert.out;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.docx4j.XmlUtils;
import org.docx4j.model.Model;
import org.docx4j.model.SymbolModel;
import org.docx4j.model.TransformState;
import org.docx4j.model.table.TableModel;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** There is one instance of the ModelRegistry for each output type (eg. PDF, HTML). 
 * The ModelRegistry acts as a factory for the Models and the Converters (aka Writers) and
 * then converts dom nodes in two steps:<ul>
 * <li>Find a registered @see org.docx4j.model.Model and initialize it with the
 * input node.
 * <li>Find a registered @see org.docx4j.model.ModelConverter and call it with the
 * model.
 * </ul>
 * Model classes represent one or more Word object internally, they can
 * probably be used without subclassing, and a new instance is created
 * for each matching node.  Writer classes on the other hand are
 * probably customized, they are to be registered by the caller, and
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
 * This assumes that a model and a writer are registered for "foo".
 * 
 *  @author Adam Schmideg
 *  
 */
public abstract class AbstractModelRegistry {
	private final static Logger log = Logger.getLogger(AbstractModelRegistry.class);
	
	private Map<String, Class> modelClasses = new HashMap<String, Class>();
	private Map<String, ModelConverter> converterInstances = new HashMap<String, ModelConverter>();

	public AbstractModelRegistry() {
		registerDefaultModelClasses();
		registerDefaultConverterInstances();
	}

	protected void registerDefaultModelClasses() {
	    registerModel(TableModel.MODEL_ID, TableModel.class);
	    registerModel(SymbolModel.MODEL_ID, SymbolModel.class);
//    	registerModel("w:p", ParagraphModel.class);
//    	registerModel("w:t", TextModel.class);
//    	registerModel("wp:inline", ImageModel.class);
//    	registerModel("w:hyperlink", HyperlinkModel.class);
//    	registerModel("w:bookmarkStart", BookmarkModel.class);
	}
	  
	public void registerModel(String name, Class modelClass) {
		modelClasses.put(name, modelClass);
	}
	
	protected abstract void registerDefaultConverterInstances();
	
	public void registerConverter(ModelConverter converter) {
	    if (!modelClasses.containsKey(converter.getID()))
	      throw new IllegalArgumentException("No model registered for " + converter.getID());
	    converterInstances.put(converter.getID(), converter);
	}
	
	/** Factory Method to create the required Transform States for the context
	 * 
	 * @return
	 */
	public Map<String, TransformState> createTransformStates() {
	Map<String, TransformState> ret = new HashMap<String, TransformState>();
	TransformState state = null;
		for (Map.Entry<String, ModelConverter> item : converterInstances.entrySet()) {
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
			Class c = modelClasses.get(modelID);
			if (c == null) {
				log.error("No model registered for " + modelID);
				throw new IllegalArgumentException("No model registered for " + modelID);
			} else {
				log.debug("Using model " + c.getName() + " for node " + modelID);
			}
			try {
				Model model = (Model)c.newInstance();
				model.setWordMLPackage(context.getWmlPackage());
				model.build(unmarshalledNode, content);

				ModelConverter converter = converterInstances.get(modelID);
				if (converter == null) {

					// We don't have a converter for writing to an output format
					// Either this is a problem ..
					log.warn("No writer registered for " + modelID);
					// .. or the intent is to import to docx
					log.info("Generating wml from model.");
					Object o = model.toJAXB();
					org.w3c.dom.Document docMarshalled = XmlUtils.marshaltoW3CDomDocument(o);
					DocumentFragment docfrag = docMarshalled.createDocumentFragment();
					docfrag.appendChild(docMarshalled.getDocumentElement());
					return docfrag;

				} else {
					return converter.toNode(context, model, context.getTransformState(modelID), doc);
				}
			} catch (Exception e) {
				log.error("Cannot convert " + unmarshalledNode, e);
				return null;
			}
		}
}
