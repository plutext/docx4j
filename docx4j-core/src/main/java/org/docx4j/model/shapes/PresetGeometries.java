/* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
 * It is based on Apache POI's org.apache.poi.xslf.model.geom.PresetGeometries:
 * the package name has been changed, and the definitions are read into docx4j's
 * own JAXB classes; there may also be other changes.
 *
 * This notice is included to meet the condition in clause 4(b) of the License.
 */

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

/**
 * The DrawingML preset shape definitions ({@code presetShapeDefinitions.xml}, from ECMA-376 Part 1), by preset name.
 * {@link ShapeGeometry} evaluates them (CR-080), and the text warps ({@link #getTextWarps()}, CR-083).
 */
public class PresetGeometries  extends LinkedHashMap<String, CTCustomGeometry2D> {
	
	// based on https://svn.apache.org/repos/asf/poi/trunk/src/ooxml/java/org/apache/poi/xslf/model/geom/PresetGeometries.java
	// by Yegor Kozlov
	
	private static Logger log = LoggerFactory.getLogger(PresetGeometries.class);	
	
    /** Read once, on first use, by whichever thread comes first (CR-080 phase 1). */
    private static final class Holder {
        static final PresetGeometries INSTANCE = new PresetGeometries("org/docx4j/model/shapes/presetShapeDefinitions.xml");
    }

    /** The text warps, read once, on first use (CR-083 phase 2). */
    private static final class WarpHolder {
        static final PresetGeometries INSTANCE = new PresetGeometries("org/docx4j/model/shapes/presetTextWarpDefinitions.xml");
    }

    /**
     * The 40 text warp presets ({@code a:prstTxWarp}, {@code presetTextWarpDefinitions.xml}, from ECMA-376 Part 1),
     * by name, {@code textNoShape} included (it has no paths).
     *
     * @since 17.3.7 (CR-083)
     */
    public static PresetGeometries getTextWarps(){
        return WarpHolder.INSTANCE;
    }
    
    public static PresetGeometries getInstance(){
        return Holder.INSTANCE;
    }
    
    private PresetGeometries(String resource){
        try {
            InputStream is =
                    ResourceUtils.getResource(resource);
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
