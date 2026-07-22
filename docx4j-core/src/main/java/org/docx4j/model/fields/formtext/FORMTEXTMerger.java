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
package org.docx4j.model.fields.formtext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.model.fields.AbstractMerger;
import org.docx4j.model.fields.ComplexFieldLocator;
import org.docx4j.model.fields.FieldRef;
import org.docx4j.model.fields.merge.DataFieldName;
import org.docx4j.model.fields.merge.MailMerger.OutputField;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Body;
import org.docx4j.wml.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Microsoft intended FORMTEXT fields for 
 * a user interactively filling forms via the Word UI
 * (compared to MERGEFIELD which are intended for the
 *  automated case).
 *  
 * Even so, organisations sometimes wish to 
 * automate documents containing FORMTEXT fields. 
 * 
 * This class facilitates that.
 * 
 * Datafield name is expected to be in textInput/default. 
 * 
 * @author jharrop
 * @since 11.5.2
 */
public class FORMTEXTMerger extends AbstractMerger {

	private static Logger log = LoggerFactory.getLogger(FORMTEXTMerger.class);	
	
	public FORMTEXTMerger(WordprocessingMLPackage input) {
		super(input);
	}
	
	protected List<Object> performOnInstance( 
			List<Object> contentList, 
			Map<DataFieldName, String> datamap,
			FormTextFieldNames formTextFieldNames /* not used here */) throws Docx4JException {
		
		// We need our fieldRefs point to the correct objects;
		// the easiest way to do this is to create them after cloning!
		
		// to facilitate cloning, wrap the list in a body
		Body shell = Context.getWmlObjectFactory().createBody();
		shell.getContent().addAll(contentList);
		Body shellClone = (Body)XmlUtils.deepCopy(shell);
		
		// find fields
		ComplexFieldLocator fl = new ComplexFieldLocator();
		new TraversalUtil(shellClone, fl);
		log.info("Found " + fl.getStarts().size() + " fields ");		
		
		// canonicalise and setup fieldRefs 
		List<FieldRef> fieldRefs = new ArrayList<FieldRef>();
		canonicaliseStarts(fl, fieldRefs);
		
		// Populate
		for (FieldRef fr : fieldRefs) {
			
			if ( "FORMTEXT".equals(fr.getFldName()) ) {				
				
				String datafieldName = FFDataUtil.getDatafieldNameFromFFData(fr.getFormFieldProperties());
				
				if (datafieldName==null) {
					log.warn("No instructions found in this field");
					// TODO for various cases
					continue;
				}
				
				String val = datamap.get( new DataFieldName(datafieldName));

				if (StringUtils.isBlank(val)) {
					log.warn("Couldn't find value for key: '" + datafieldName + "'");
                    if (fieldFate.equals(OutputField.REMOVED)) {
                        // Remove the mergefield from the document
                        removeSimpleField(fr);

                        // Concatenate all content still present in the parent
                        String text = getTextInsideContent(fr.getParent());

                        // If the parent still contains data, don't delete it
                        if (StringUtils.isBlank(text)) {
                            recursiveRemove(shellClone, fr.getParent());
                        }
                    }				
				} else {
					
					// No formatting for FORMTEXT
					fr.setResult(val);
				}
				
				if (fieldFate.equals(OutputField.AS_FORMTEXT_REGULAR)) {					
				
					// remove <w:highlight w:val="lightGray"/>, if present
					// (corresponds in Word to clicking Legacy Forms > Form Field Shading)
					// so that the result is not printed in grey
					R resultR = fr.getResultsSlot();
					if (resultR.getRPr()!=null
							&& resultR.getRPr().getHighlight()!=null) {
						resultR.getRPr().setHighlight(null);
					}
					
				} else if (fieldFate.equals(OutputField.KEEP_MERGEFIELD)) {
					
					// TODO: unexpected in this case.
					
				} else {
					// If doing an actual mail merge, the begin-separate run is removed, as is the end run				
					fr.getParent().getContent().remove(fr.getBeginRun());
					fr.getParent().getContent().remove(fr.getEndRun());					
				}
				
//				System.out.println("AFTER " +XmlUtils.marshaltoString(
//						fr.getParent(), true, true));
				
			}
		}
		
		return shellClone.getContent();

	}
	


}
