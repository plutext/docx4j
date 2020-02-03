package org.docx4j.model.fields;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.wml.CTFFData;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.FldChar;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The objective of this class is to represent a complex field
 * (containing nested fields, if any; nested fields are 
 * represented by FieldRef object of their own).
 * 
 * TODO, consider whether to make this abstract, with 
 * differing concrete implementations for top level and nested fields.
 * 
 * Background. There are simple fields:
 * 
      <w:fldSimple w:instr=" DATE ">
        <w:r>
          <w:rPr>
            <w:noProof/>
          </w:rPr>
          <w:instrText>4/12/2011</w:instrText>  <!-- ???? -->
        </w:r>
      </w:fldSimple>
 * 
 * and there are complex fields:
 * 
    <w:p>
      <w:r>
        <w:fldChar w:fldCharType="begin"/>
      </w:r>
      <w:r>
        <w:instrText xml:space="preserve"> REF hi \h </w:instrText>
      </w:r>
      <w:r>
        <w:fldChar w:fldCharType="separate"/>
      </w:r>
      <w:r>
        <w:t>Hello</w:t>
      </w:r>
    </w:p> 
    
    
 * A simple field can also take the complex form:
 * 
      <w:r>
        <w:fldChar w:fldCharType="begin"/>
        <w:instrText xml:space="preserve">DATE </w:instrText>
        <w:fldChar w:fldCharType="separate"/>
      </w:r>
      <w:r>
        <w:t>4/12/2011</w:t>
      </w:r>
      <w:r>
        <w:fldChar w:fldCharType="end"/>
      </w:r>

 * A complex field can contain nested fields, in either
 * its instruction part, or result part.
 * 
 * we need to represent nest fields in the instructions
 * part only (since nested fields in the results part
 * get re-generated).
 * 
 * An example of a nested field in the instructions part:
 * 
    <w:p>
      <w:r>
        <w:fldChar w:fldCharType="begin"/>
      </w:r>
      <w:r>
        <w:instrText xml:space="preserve"> IF </w:instrText>
      </w:r>
      <w:fldSimple w:instr=" DATE ">
        <w:r>
          <w:rPr>
            <w:noProof/>
          </w:rPr>
          <w:instrText>4/12/2011</w:instrText>
        </w:r>
      </w:fldSimple>
      <w:r>
        <w:instrText xml:space="preserve">="4/12/2011" "it is 4/12" "not 4/12" </w:instrText>
      </w:r>
      <w:r>
        <w:fldChar w:fldCharType="separate"/>
      </w:r>
      <w:r>
        <w:rPr>
          <w:noProof/>
        </w:rPr>
        <w:t>today</w:t>
      </w:r>
      <w:r>
        <w:fldChar w:fldCharType="end"/>
      </w:r>
    </w:p>
    
 * An example of nested fields in the results part:
 * 
 *     <w:p w:rsidR="00BA0732" w:rsidRDefault="00BA0732">
      <w:pPr>
        <w:pStyle w:val="TOC1"/>
        <w:tabs>
          <w:tab w:val="right" w:leader="dot" w:pos="9016"/>
        </w:tabs>
        <w:rPr>
          <w:noProof/>
        </w:rPr>
      </w:pPr>
      <w:r>
        <w:fldChar w:fldCharType="begin"/>
      </w:r>
      <w:r>
        <w:instrText xml:space="preserve"> TOC \o "1-3" \h \z \ u</w:instrText>
      </w:r>
      <w:r>
        <w:fldChar w:fldCharType="separate"/>
      </w:r>
      <w:hyperlink w:anchor="_Toc310757867" w:history="1">
        <w:r w:rsidRPr="0061726E">
          <w:rPr>
            <w:rStyle w:val="Hyperlink"/>
            <w:noProof/>
          </w:rPr>
          <w:t>one</w:t>
        </w:r>
        <w:r>
          <w:rPr>
            <w:noProof/>
            <w:webHidden/>
          </w:rPr>
          <w:tab/>
        </w:r>
        <w:r>
          <w:rPr>
            <w:noProof/>
            <w:webHidden/>
          </w:rPr>
          <w:fldChar w:fldCharType="begin"/>
        </w:r>
        <w:r>
          <w:rPr>
            <w:noProof/>
            <w:webHidden/>
          </w:rPr>
          <w:instrText xml:space="preserve"> PAGEREF _Toc310757867 \h </w:instrText>
        </w:r>
        <w:r>
          <w:rPr>
            <w:noProof/>
            <w:webHidden/>
          </w:rPr>
        </w:r>
        <w:r>
          <w:rPr>
            <w:noProof/>
            <w:webHidden/>
          </w:rPr>
          <w:fldChar w:fldCharType="separate"/>
        </w:r>
        <w:r>
          <w:rPr>
            <w:noProof/>
            <w:webHidden/>
          </w:rPr>
          <w:t>1</w:t>
        </w:r>
        <w:r>
          <w:rPr>
            <w:noProof/>
            <w:webHidden/>
          </w:rPr>
          <w:fldChar w:fldCharType="end"/>
        </w:r>
      </w:hyperlink>
    </w:p>
    <w:p w:rsidR="00BA0732" w:rsidRDefault="00BA0732">
      <w:pPr>
        <w:pStyle w:val="TOC2"/>
        <w:tabs>
          <w:tab w:val="right" w:leader="dot" w:pos="9016"/>
        </w:tabs>
        <w:rPr>
          <w:noProof/>
        </w:rPr>
      </w:pPr>
      <w:hyperlink w:anchor="_Toc310757868" w:history="1">
        <w:r w:rsidRPr="0061726E">
          <w:rPr>
            <w:rStyle w:val="Hyperlink"/>
            <w:noProof/>
          </w:rPr>
          <w:t>oneone</w:t>
        </w:r>
        <w:r>
          <w:rPr>
            <w:noProof/>
            <w:webHidden/>
          </w:rPr>
          <w:tab/>
        </w:r>
        <w:r>
          <w:rPr>
            <w:noProof/>
            <w:webHidden/>
          </w:rPr>
          <w:fldChar w:fldCharType="begin"/>
        </w:r>
        <w:r>
          <w:rPr>
            <w:noProof/>
            <w:webHidden/>
          </w:rPr>
          <w:instrText xml:space="preserve"> PAGEREF _Toc310757868 \h </w:instrText>
        </w:r>
        <w:r>
          <w:rPr>
            <w:noProof/>
            <w:webHidden/>
          </w:rPr>
        </w:r>
        <w:r>
          <w:rPr>
            <w:noProof/>
            <w:webHidden/>
          </w:rPr>
          <w:fldChar w:fldCharType="separate"/>
        </w:r>
        <w:r>
          <w:rPr>
            <w:noProof/>
            <w:webHidden/>
          </w:rPr>
          <w:t>1</w:t>
        </w:r>
        <w:r>
          <w:rPr>
            <w:noProof/>
            <w:webHidden/>
          </w:rPr>
          <w:fldChar w:fldCharType="end"/>
        </w:r>
      </w:hyperlink>
    </w:p>
    <w:p w:rsidR="00BA0732" w:rsidRDefault="00BA0732" w:rsidP="00BA0732">
      <w:r>
        <w:fldChar w:fldCharType="end"/>
      </w:r>
    </w:p>
 * 
 * In general, you can "canonicalise" the field representation
 * to be 
 * (i)   instructions, contained within a single run 
 * (ii)  results, immediately following, though not nec just as following siblings
 * (iii) the final <w:fldChar w:fldCharType="end"/>
 * 
 * Since the purpose of our field support is to update the field 
 * results, we can delete (ii) and (iii) before adding them in again.
 * 
 * The document is preprocessed to put it into this form. 
 * 
 * @author jharrop
 *
 */
public class FieldRef {
	
	private static Logger log = LoggerFactory.getLogger(FieldRef.class);			
	
	public FieldRef(FldChar fldCharBegin) {
		this.fldCharBegin = fldCharBegin;
	}
	
	private FldChar fldCharBegin;
	
	protected String fldName = null;
	/**
	 * The name of the (outer most) field, for example DATE, MERGEFIELD.
	 * 
	 * Assume for now that this is contained in instructions.get(0).
	 * 
	 * @see <a href="http://webapp.docx4java.org/OnlineDemo/ecma376/WordML/file_2.html">field syntax</a>
	 * @return
	 */
	public String getFldName() {
		Object o = XmlUtils.unwrap(instructions.get(0));
		if (o instanceof Text) {
			return FormattingSwitchHelper.getFldSimpleName( ((Text)o).getValue() );
		} else {
			log.error("TODO: extract field name from " + o.getClass().getName() );
			if (o instanceof FieldRef) {
				// contains a nested field?!
				FieldRef nested = (FieldRef)o;
				log.error("Nested field " + nested.getFldName() );				
				
			} else {
                if(log.isErrorEnabled()) {
                    log.error(XmlUtils.marshaltoString(instructions.get(0), true, true));
                }
			}
			return null;
		}
	}	
	
	private ContentAccessor parent;
	public ContentAccessor getParent() {
		return parent;
	}
	public void setParent(ContentAccessor parent) {
		this.parent = parent;
	}

	
	/**
	 * The run 
	 * 
	 *       <w:r>
                <w:fldChar w:fldCharType="begin"/>
                <w:instrText xml:space="preserve"> ... </w:instrText>
                <w:fldChar w:fldCharType="separate"/>
            </w:r>
            
	 */
	private R beginRun;
	public R getBeginRun() {
		return beginRun;
	}
	public void setBeginRun(R beginRun) {
		this.beginRun = beginRun;
	}

	private boolean seenSeparate=false; 
	public boolean haveSeenSeparate() {
		return seenSeparate;
	}
	public void setSeenSeparate(boolean seenSeparate) {
		this.seenSeparate = seenSeparate;
	}
	
	private void processFldBegin() {
		formFieldProperties = fldCharBegin.getFfData();
		customFieldData = fldCharBegin.getFldData();
		dirty = fldCharBegin.isDirty();
		lock = fldCharBegin.isFldLock();
	}
	
	private Boolean mergeFormat;
	/**
	 * @return whether \* MERGEFORMAT is set
	 */
	public Boolean isMergeFormat() {
		
		if (mergeFormat==null) {
			//Work it out. Assume for now that this is contained in instructions.get(0).
			mergeFormat = Boolean.FALSE;
			Object o = XmlUtils.unwrap(instructions.get(0));
			if (o instanceof Text) {
				String instr = ((Text)o).getValue();
				if (instr.contains("MERGEFORMAT")) {
					mergeFormat = Boolean.TRUE;
				}
			} else {
                if(log.isErrorEnabled()) {
                    log.error("TODO: extract field name from " + o.getClass().getName());
                    log.error(XmlUtils.marshaltoString(instructions.get(0), true, true));
                }
			}
		}
		return mergeFormat;
	}

	private boolean dirty;

	/**
	 * Specifies that this field has been flagged by an application to indicate that its current results
	 * are invalid (stale) due to other modifications made to the document, and these contents should be 
	 * updated before they are displayed.
	 * @return whether stale
	 * @see <a href="http://webapp.docx4java.org/OnlineDemo/ecma376/WordML/fldChar.html">the spec</a>
	 */
	public boolean isDirty() {
		return dirty;
	}
	/**
	 * @param whether stale
	 * @see <a href="http://webapp.docx4java.org/OnlineDemo/ecma376/WordML/fldChar.html">the spec</a>
	 */
	public void setDirty(boolean dirty) {
		this.dirty = dirty;
		fldCharBegin.setDirty(dirty);
		
		// Note that this doesn't set dirty on any nested fields.  TODO: Consider whether it should.
	}

	private boolean lock;
	
	/**
	 * @return the lock
	 * @see <a href="http://webapp.docx4java.org/OnlineDemo/ecma376/WordML/fldChar.html">the spec</a>
	 */
	public boolean isLock() {
		return lock;
	}
	/**
	 * Specifies that the parent complex field shall not have its field result recalculated, even if 
	 * an application attempts to recalculate the results of all fields in the document or a 
	 * recalculation is explicitly requested.
	 * 
	 * @param lock the lock to set
	 * @see <a href="http://webapp.docx4java.org/OnlineDemo/ecma376/WordML/fldChar.html">the spec</a>
	 */
	public void setLock(boolean lock) {
		this.lock = lock;
		fldCharBegin.setFldLock(lock);
	}

	private Text customFieldData;
	
	/**
	 * application-specific data associated with this field.
	 * 
	 * @return the customFieldData
	 * @see <a href="http://webapp.docx4java.org/OnlineDemo/ecma376/WordML/fldData_1.html">the spec</a>
	 */
	public Text getCustomFieldData() {
		return customFieldData;
	}
	/**
	 * @param customFieldData the customFieldData to set
	 * @see <a href="http://webapp.docx4java.org/OnlineDemo/ecma376/WordML/fldData_1.html">the spec</a>
	 */
	public void setCustomFieldData(Text customFieldData) {
		this.customFieldData = customFieldData;
		fldCharBegin.setFldData(customFieldData);
	}

	private CTFFData formFieldProperties;

	/**
	 * Properties specific to FORMCHECKBOX, FORMDROPDOWN, FORMTEXT
	 * 
	 * @return the formFieldProperties
	 * @see <a href="http://webapp.docx4java.org/OnlineDemo/ecma376/WordML/ffData.html">the spec</a>
	 */
	public CTFFData getFormFieldProperties() {
		return formFieldProperties;
	}
	/**
	 * @param formFieldProperties the formFieldProperties to set
	 * @see <a href="http://webapp.docx4java.org/OnlineDemo/ecma376/WordML/ffData.html">the spec</a>
	 */
	public void setFormFieldProperties(CTFFData formFieldProperties) {
		this.formFieldProperties = formFieldProperties;
		fldCharBegin.setFfData(formFieldProperties);
	}

	/**
	 * The run 
	 * 
            <w:r>
                <w:fldChar w:fldCharType="end"/>
            </w:r>
            
        Store a reference to it so we can delete it.
	 */
	private R endRun;
	public R getEndRun() {
		return endRun;
	}
	public void setEndRun(R endRun) {
		this.endRun = endRun;
	}
	
	
	/**
	 * A list of the content between the outermost w:fldChar begin and separate elements;
	 * in the simplest case, this will be a single w:instrText object;
	 * in a more general case it will be a mixture of w:instrText and FieldRef objects
	 * (and possibly other things such as w:br).
	 */
	private List<Object> instructions = new ArrayList<Object>();
	/**
	 * @return the instructions
	 */
	public List<Object> getInstructions() {
		return instructions;
	}
	
//	private JAXBElement<Text> instrText;
//	public String getInstr() {
//		return instrText.getValue().getValue();
//	}
//	public void setInstrText(JAXBElement<Text> instrTextIn) {
//		if (this.instrText==null){
//			this.instrText = instrTextIn;
//		} else {
//			// Merge
//			String text = this.getInstr() + instrTextIn.getValue().getValue();
//			this.instrText.getValue().setValue(text);
//		}
//	}


	private R resultsSlot;
	public R getResultsSlot() {
		return resultsSlot;
	}
	public void setResultsSlot(R resultsSlot) {
		this.resultsSlot = resultsSlot;
	}
	
	public void setResult(String val) {
		
		resultsSlot.getContent().clear();
		String[] splitted = val.split("\\R");
		
		// our docfrag may contain several runs
		boolean firsttoken = true;
		for (int i = 0; i < splitted.length; i++) {						
			String line = splitted[i];
			
			if (firsttoken) {
				firsttoken = false;
			} else {
				resultsSlot.getContent().add(Context.getWmlObjectFactory().createBr());
			}
			
			org.docx4j.wml.Text text = Context.getWmlObjectFactory().createText();
			resultsSlot.getContent().add(text);
			if (line.startsWith(" ") || line.endsWith(" ") ) {
				// TODO: tab character?
				text.setSpace("preserve");
			}
			text.setValue(line);
		}
	}

}
