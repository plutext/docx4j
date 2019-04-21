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


package org.docx4j.samples;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart.AddPartBehaviour;
import org.docx4j.relationships.Relationship;

/**
 * 'Copy' a part from one package to another.
 * 
 * @author Jason Harrop
 */
public class PartCopy {

	public static void main(String[] args) throws Exception {
		
		// Source package		
		String inputfilepath = System.getProperty("user.dir") + "/sample-docs/chart.docx";
		// Contains (see PartsList sample):
        // Part /word/charts/chart1.xml [org.docx4j.openpackaging.parts.DrawingML.Chart] http://schemas.openxmlformats.org/officeDocument/2006/relationships/chart containing JaxbElement:org.docx4j.dml.chart.CTChartSpace
        //    Part /word/embeddings/Microsoft_Excel_Worksheet1.xlsx [org.docx4j.openpackaging.parts.WordprocessingML.EmbeddedPackagePart] http://schemas.openxmlformats.org/officeDocument/2006/relationships/package

		WordprocessingMLPackage wmlSourcePkg = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
		
		// Destination
		System.out.println( "Creating package..");
		WordprocessingMLPackage wmlDestPkg = WordprocessingMLPackage.createPackage();
		
		// Find the part we want to copy.  
		// Note 1, this example does not copy parts recursively,
		// so it won't copy Part /word/embeddings/Microsoft_Excel_Worksheet1.xlsx
		// Note 2: Really, you should clone - this is not a 'copy' as such:
		// the part will end up being referenced in both packages,
		// though part.getPackage() will only return the dest package.
		// So, be careful, you should throw away source package after doing this!  
		// TODO: This example probably should be re-worked.
		RelationshipsPart rp = wmlSourcePkg.getMainDocumentPart().getRelationshipsPart();
		Relationship rel = rp.getRelationshipByType(Namespaces.SPREADSHEETML_CHART);		
		Part p = rp.getPart(rel);
		
		System.out.println(p.getPartName().getName() );
		
		//p.setPartName( new PartName(p.getPartName().getName()) ); // same name
			// WARNING: that has the effect of removing the part of old name 
			// from wmlSourcePkg, and adding it again - with same name.
			// So there is not much point in doing that.
		
//		System.out.println(p.getContentType() );
//		System.out.println(p.getRelationshipType() );
		
		// Now try adding it
		wmlDestPkg.getMainDocumentPart().addTargetPart(p, AddPartBehaviour.OVERWRITE_IF_NAME_EXISTS);
		// Don't change the part name here with p.setPartName, because the p is not a clone,
		// so changing its name would make source pkg inconsistent!
		// The same problem could occur if we usd AddPartBehaviour.RENAME_IF_NAME_EXISTS
		System.out.println("Done" );
		
//		p.setOwningRelationshipPart(owningRelationshipPart)
//		p.setPackage(pack)
		
		// To make the output docx make sense, there should be
		// an appropriate reference from the document to the part,
		// but that is outside the scope of this example.
				
	}
	
}
