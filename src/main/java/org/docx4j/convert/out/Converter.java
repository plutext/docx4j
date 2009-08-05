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

import java.io.*;
import java.util.*;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.docx4j.model.Model;
import org.docx4j.model.table.TableModel;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 * The singleton instance of this class converts dom nodes in two
 * steps.<ul>
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
public class Converter {
  private final static Logger logger = Logger.getLogger(Converter.class);

  private static Converter instance;

  static {
    instance = new Converter();
  }

  private WordprocessingMLPackage wmlPackage;
//  private File inputFile;
//  private File outputDir;
  private Map<String, Class> modelClasses;
  private Map<String, ModelConverter> converters;

  public static Converter getInstance() {
    return instance;
  }

  public Converter() {
    modelClasses = new HashMap<String, Class>();
    modelClasses.put("w:tbl", TableModel.class);
//    modelClasses.put("w:p", ParagraphModel.class);
//    modelClasses.put("w:t", TextModel.class);
//    modelClasses.put("wp:inline", ImageModel.class);
//    modelClasses.put("w:hyperlink", HyperlinkModel.class);
//    modelClasses.put("w:bookmarkStart", BookmarkModel.class);
    
    converters = new HashMap<String, ModelConverter>();
    
  }

  /**
   * Called before a document is processed.  Converters are assumed to
   * have been registered.
   */
  public void start(WordprocessingMLPackage wmlPackage) { 
//  public void start(WordprocessingMLPackage wmlPackage, 
//      File inputFile, File outputDir) {
    this.wmlPackage = wmlPackage;
//    this.inputFile = inputFile;
//    this.outputDir = outputDir;
    // call start for each converter
    for (ModelConverter conv: converters.values())
      conv.start();
  }

  public void stop() {
    // call stop for each converter
    for (ModelConverter conv: converters.values())
      conv.stop();
  }

  public WordprocessingMLPackage getWmlPackage() {
    return wmlPackage;
  }

//  public File getInputFile() {
//    return inputFile;
//  }
//
//  public File getOutputDir() {
//    return outputDir;
//  }

  public Converter registerModelConverter(String tag, ModelConverter writer) {
    if (! modelClasses.containsKey(tag))
      throw new IllegalArgumentException("No model registered for " + tag);
    writer.setMainConverter(this);
    converters.put(tag, writer);
    return this;
  }

//  public static Node toNode(Node node) {
//	  return toNode(node, null);
//  }
  
  public static Node toNode(Node node, NodeList children) {

    Converter inst = Converter.getInstance();
    Class c = inst.modelClasses.get(node.getNodeName());    
    if (c == null) {
    	logger.error("No model registered for " + 
        node.getNodeName());
      throw new IllegalArgumentException("No model registered for " + 
        node.getNodeName());
    } else {
    	logger.debug("Using model " + c.getName() + " for node " + node.getNodeName());
    }
    ModelConverter converter = inst.converters.get(node.getNodeName());
    if (converter == null)
      throw new IllegalArgumentException("No writer registered for " + 
        node.getNodeName());
    try {
      Model model = (Model)c.newInstance();
      model.build(inst, node, children);
      return converter.toNode(model);
    }
    catch (Exception e) {
      logger.error("Cannot convert " + node, e);
      return null;
    }
  }

}
