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
package org.pptx4j.convert.out;

import java.util.Map;

import org.docx4j.convert.out.AbstractConversionContext;
import org.docx4j.convert.out.AbstractConversionSettings;
import org.docx4j.convert.out.common.writer.AbstractMessageWriter;
import org.docx4j.model.PropertyResolver;
import org.docx4j.model.TransformState;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.parts.Part;
import org.pptx4j.convert.out.svginhtml.SvgExporter.SvgSettings;

public abstract class AbstractPmlConversionContext extends AbstractConversionContext {

	protected AbstractPmlConversionContext(AbstractMessageWriter messageWriter,
			AbstractConversionSettings conversionSettings) {
		super(messageWriter, conversionSettings);
		// TODO Auto-generated constructor stub
	}

	
}
