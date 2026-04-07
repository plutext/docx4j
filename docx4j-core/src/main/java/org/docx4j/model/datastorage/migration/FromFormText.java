/**
 *  Copyright 2026, Plutext Pty Ltd.
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

 **/
package org.docx4j.model.datastorage.migration;

import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.model.fields.ComplexFieldLocator;
import org.docx4j.model.fields.FieldRef;
import org.docx4j.model.fields.FieldsPreprocessor;
import org.docx4j.model.fields.formtext.FFDataUtil;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import org.docx4j.wml.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * This class will help you to migrate
 * from FORMTEXT
 * to use of content control data bindings.
 * 
 * After migrating, you'll be able to
 * use the OpenDoPE authoring tool to
 * add repeats, conditionals, and other
 * OpenDoPE features, if you need them.
 * 
 * Limitations: this first version
 * operates only on the main document part
 * (ie it won't process variables in
 *  headers/footers, footnotes/endnotes,
 *  or comments)
 *  
 * @author jharrop
 * @since 11.5.12
 */
public class FromFormText extends AbstractMigratorUsingAnswersFormat {
	
	private static Logger log = LoggerFactory.getLogger(FromFormText.class);
	
	public WordprocessingMLPackage migrate(WordprocessingMLPackage pkgIn) throws Exception {
		
		// TODO - test that OpenDoPE parts aren't already present
		// or if they are, that this docx is using our answer format
		// (since only that format is supported here)
		
		// Clone it
		WordprocessingMLPackage pkgOut = (WordprocessingMLPackage)pkgIn.clone();
				
		// Create the CustomXML parts
		createParts(pkgOut);
		
		FieldsPreprocessor.complexifyFields(pkgOut.getMainDocumentPart() );
        if(log.isDebugEnabled()) {
            log.debug("complexified: "
                    + XmlUtils.marshaltoString(pkgOut.getMainDocumentPart().getJaxbElement(), true));
        }
		
		// find fields
		ComplexFieldLocator fl = new ComplexFieldLocator();
		new TraversalUtil(pkgOut.getMainDocumentPart().getContent(), fl);
		log.info("Found " + fl.getStarts().size() + " fields ");
		
		
		// canonicalise and setup fieldRefs 
		List<FieldRef> fieldRefs = new ArrayList<FieldRef>();
		for( P p : fl.getStarts() ) {
			int index = ((ContentAccessor)p.getParent()).getContent().indexOf(p);
			P newP = FieldsPreprocessor.canonicalise(p, fieldRefs);
			((ContentAccessor)p.getParent()).getContent().set(index, newP);
		}
		
		// Populate
		for (FieldRef fr : fieldRefs) {
			
			if ( fr.getFldName().equals("FORMTEXT") ) {
				
				String key = FFDataUtil.getDatafieldNameFromFFData(fr.getFormFieldProperties());
								
				// Remove the field related runs
				int end = fr.getParent().getContent().indexOf(fr.getEndRun());
				int begin = fr.getParent().getContent().indexOf(fr.getBeginRun());
				for (int i = end; i>=begin; i--) {
					fr.getParent().getContent().remove(i);
				}
				
				// Now add a content control
				List<Object> replacementContent = new ArrayList<Object>();
				createContentControl(null, replacementContent, key);	
				
				fr.getParent().getContent().addAll(begin, replacementContent);
				
//				System.out.println(XmlUtils.marshaltoString(
//						fr.getParent(), true, true));
				
			}
		}
				
		return pkgOut;
	}
	



}
