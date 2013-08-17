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

import org.docx4j.convert.out.AbstractConversionSettings;
import org.docx4j.convert.out.common.AbstractConversionContext;
import org.docx4j.convert.out.common.writer.AbstractMessageWriter;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.pptx4j.convert.out.svginhtml.SvgExporter;

public abstract class AbstractPmlConversionContext extends AbstractConversionContext {

	protected AbstractPmlConversionContext(AbstractMessageWriter messageWriter,
			AbstractConversionSettings conversionSettings, PresentationMLPackage localPmlPackage) {
		super(messageWriter, conversionSettings, localPmlPackage);
	}

	@Override
	protected void initializeSettings(AbstractConversionSettings settings, OpcPackage opcPackage) {
	SvgExporter.SvgSettings svgSettings = null;
		if (settings == null) {
			settings = new SvgExporter.SvgSettings();
		}
		else if (!(settings instanceof SvgExporter.SvgSettings)) {
			throw new IllegalArgumentException("The class of the settings isn't SvgExporter.SvgSettings, it is " + settings.getClass().getName());
		}
		svgSettings = (SvgExporter.SvgSettings)settings;
		super.initializeSettings(svgSettings, opcPackage);
	}

	@Override
	protected OpcPackage initializeOpcPackage(AbstractConversionSettings settings, OpcPackage opcPackage) {
	OpcPackage ret = super.initializeOpcPackage(settings, opcPackage);
		if (!(ret instanceof PresentationMLPackage)) {
			throw new IllegalArgumentException("The opcPackage isn't a PresentationMLPackage, it is a " + ret.getClass().getName());
		}
		return ret;
	}

	public PresentationMLPackage getPmlPackage() {
		return (PresentationMLPackage)getOpcPackage();
	}
	
}
