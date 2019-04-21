package org.docx4j.model.shapes;

/*
 * Inspired/converted from org.apache.poi.xslf.model.geom.PresetGeometries
 *  which
 *  
 *  ====================================================================
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==================================================================== */

import java.io.InputStream;
import java.util.LinkedHashMap;

import org.docx4j.XmlUtils;
import org.docx4j.dml.CTCustomGeometry2D;
import org.docx4j.jaxb.Context;
import org.docx4j.utils.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PresetGeometries  extends LinkedHashMap<String, CTCustomGeometry2D> {
	
	// based on https://svn.apache.org/repos/asf/poi/trunk/src/ooxml/java/org/apache/poi/xslf/model/geom/PresetGeometries.java
	// by Yegor Kozlov
	
	private static Logger log = LoggerFactory.getLogger(PresetGeometries.class);	
	
    private static PresetGeometries _inst;
    
    public static PresetGeometries getInstance(){
        if(_inst == null) _inst = new PresetGeometries();

        return _inst;
    }
    
    private PresetGeometries(){
        try {
            InputStream is =
                    ResourceUtils.getResource("org/docx4j/model/shapes/presetShapeDefinitions.xml");
            read(is);
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private void read(InputStream is) throws Exception {
    	
	    Document domDoc = XmlUtils.getNewDocumentBuilder().parse(is);
	    
	    Element presetShapeDefinitons = domDoc.getDocumentElement();
        NodeList nodes = presetShapeDefinitons.getChildNodes();

        for (int i = 0; i < nodes.getLength(); i++) {
        	
           Node node = nodes.item(i);
           
           if (!(node instanceof Element)) continue;
           
           String name = node.getLocalName();
           CTCustomGeometry2D geom = (CTCustomGeometry2D)XmlUtils.unmarshal(node, Context.jc, CTCustomGeometry2D.class);

           if(containsKey(name)) {
               log.warn("Duplicate definition of " + name) ;  // happened for upDownArrow; that dupe now commented out
           }
           put(name, geom);
           
           log.debug(name);
           
        }	    
	       	
        
    }   
    
}
