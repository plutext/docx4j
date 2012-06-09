/*
 * Copyright 2012 Plutext Pty Ltd.
 * 
 * This file is part of docx4j.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.docx4j.openpackaging.parts.WordprocessingML;

import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.ExternalTarget;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;

/**
 * Summary: At present, EMF files are best converted to SVG using OpenOffice.
 * 
 * Note regarding options for converting EMF files to SVG and/or PNG
 * (as at Feb 2010):

	- com.adobe.dp.office
	- wmf2svg
	- batik
	- freehep
	- imagemagick
	- openoffice
	
	(Could mono's libgdiplus help? Search for 'mono metafile')

	wmf2tosvg is a good solution for WMF, but it has no EMF support. 
	
	FreeHEP has EMF2SVG, but the output wasn't much good (perhaps 
	because office drawings aren't its primary focus). It would 
	also be a very complex dependency.
	
	Batik has WMFTranscoder, but not EMFTranscoder! It looks like 
	one could be added from package 
	org.eclipse.gmf.runtime.draw2d.ui.render.awt.internal.svg.metafile 
	with a bit of work, but still, batik is no good on appengine since 
	it uses awt, and spawns threads. So avoid it..
	
	imagemagick has no support for EMF at all (other than on Windows). 
	
	openoffice can be used to convert EMF; I found it worked well. 
	
	Until the EMFParser in com.adobe.dp.office is fixed, this may be 
	a good option (depending on your environment). 
	
	Maybe the code to use openoffice for EMF conversions could go in 
	src/docx4j-extras, so only those who wanted to use it would need 
	to worry about the dependencies.
	
	TODO: Have a look at cairo, which has java bindings.  Can it be used
	to read EMF? See also pymfvu - UniCovertor will ultimately be able
	to import EMF
 *
 */
public class MetafileEmfPart extends MetafilePart {
	
	public MetafileEmfPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}
	
	public MetafileEmfPart(ExternalTarget externalTarget) {
		super(externalTarget);
		init();
	}	
	
	public void init() {
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.IMAGE_EMF));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.IMAGE);
	}

}
