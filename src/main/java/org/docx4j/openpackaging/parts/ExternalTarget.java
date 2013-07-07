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

package org.docx4j.openpackaging.parts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/** "When set to External, the target attribute may be a relative
 *  reference or a URI.  If the target attribute is a relative
 *  reference, then that reference is interpreted relative to the
 *  location of the package."
 *  
 *  <rel:Relationship Id="rId5" Target="http://guest%40public0902:guest@alpha.plutext.org/alfresco/plutextwebdav/User%20Homes/tester/evolution.docx/word/media/image1.png" 
 *       TargetMode="External" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/image" xmlns=""/>
 *       
 * Note: this method assumes an absolute location.
 * 
 * ECMA 376 is a little bit ambiguous about whether an
 * external resource is a "part" or not. 
 * 
 *        - the definition of part implies that it is; there is no definition of resource
 *        
 *        - but the definition of part name in 8.1.1 "refers to parts _within_ a package"
 *        (but is within logical or physical?)
 *        
 *        - 8.3 Relationships: "Parts often contain references to other parts
 *        in the package and to resources outside the package"
 *        
 * (POI has TargetMode and POIXMLRelation; they have PackagePart.addExternalRelationship
 * 
 * We'll call them external resources. 
 * 
 * When we load an external resource, it is loaded as a part.
 * 
 * But it is not stored in the package's parts collection; instead it is stored
 * in a collection called ExternalResources.
 * 
 * An ExternalTarget is the value of @target, made absolute
 * if necessary.
 * 
 * We don't need explicit support in RelationshipsPart for adding an external
 * resource; you can do that by creating a Relationship object, then calling
 * addRelationship (your reference in your @target should be relative, not absolute).
 */
public final class ExternalTarget {

	private static Logger log = LoggerFactory.getLogger(ExternalTarget.class);	
	
	String target;
	public String getValue() {
		return target;
	}

	public ExternalTarget(String target)
	{
		this.target = target;
	}
	
	@Override
	public boolean equals(Object other) {
		
		if (other == null
				|| !(other instanceof ExternalTarget))
			return false;
		return this.target.equals(
				((ExternalTarget) other).getValue());
	}

	@Override
	public int hashCode() {
		return this.target.hashCode();
	}
}
