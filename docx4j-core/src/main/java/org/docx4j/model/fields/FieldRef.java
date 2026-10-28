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

		if (instructions.isEmpty()) {
			// eg a field with empty or whitespace-only w:instrText (issue 682);
			// FieldsPreprocessor drops such instrText, leaving this list empty
			log.warn("Field has no instructions; can't determine field name");
			return null;
		}

		// The first word of the instruction's text before any nested field. Since 17.3.5 (#634) that text is read
		// whole, as Word may split it across runs ("MERGE", "FIELD name"); until then only its first run was read,
		// and where the instruction began with a nested field, that field's name was returned.
		StringBuilder text = new StringBuilder();
		for (Object o : instructions) {
			if (o instanceof FieldRef) break;
			o = XmlUtils.unwrap(o);
			if (o instanceof Text) text.append(((Text)o).getValue());
		}
		String name = FormattingSwitchHelper.getFldSimpleName(text.toString());
		if (name != null && name.length() > 1 && name.charAt(0) == '=') name = "="; // a formula's name is "=", whatever follows it (FieldInstruction)
		if (name == null) {
			// eg { { MERGEFIELD type } x }: Word names the field by the nested field's result, when it updates it
			log.info("Field instruction begins with a nested field; no name until Word updates it");
		}
		return name;
	}	
	
	/**
	 * The field's instruction: its {@code w:instrText}, concatenated, since Word may split it across runs. Null
	 * where the instruction is empty or holds a nested field (whose own FieldRef is among
	 * {@link #getInstructions()}).
	 *
	 * @return the instruction
	 * @since 17.3.5
	 */
	public String getInstr() {
		return FieldsPreprocessor.extractInstr(instructions);
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

	private boolean nested;
	/** Whether this field is inside another's instruction (its own FieldRef among the outer's {@link #getInstructions()}). @since 17.3.5 */
	public boolean isNested() {
		return nested;
	}
	public void setNested(boolean nested) {
		this.nested = nested;
	}

	/**
	 * Puts the result under a bookmark of the name, as Word stores a SET's value ({@code w:bookmarkStart} before the
	 * result slot, {@code w:bookmarkEnd} after it), replacing a bookmark of that name already there.
	 *
	 * @since 17.3.5
	 */
	public void bookmarkResult(String name, java.math.BigInteger id) {
		if (parent == null || resultsSlot == null) return;
		List<Object> content = parent.getContent();
		int slot = content.indexOf(resultsSlot);
		if (slot < 0) return;
		if (slot > 0) {
			Object before = XmlUtils.unwrap(content.get(slot - 1));
			if (before instanceof org.docx4j.wml.CTBookmark && name.equalsIgnoreCase(((org.docx4j.wml.CTBookmark) before).getName())) return; // already so
		}
		org.docx4j.wml.CTBookmark start = Context.getWmlObjectFactory().createCTBookmark();
		start.setId(id);
		start.setName(name);
		org.docx4j.wml.CTMarkupRange end = Context.getWmlObjectFactory().createCTMarkupRange();
		end.setId(id);
		content.add(slot + 1, Context.getWmlObjectFactory().createBodyBookmarkEnd(end));
		content.add(slot, Context.getWmlObjectFactory().createBodyBookmarkStart(start));
	}
	public boolean haveSeenSeparate() {
		return seenSeparate;
	}
	public void setSeenSeparate(boolean seenSeparate) {
		this.seenSeparate = seenSeparate;
	}
	
	private void processFldBegin() {
		formFieldProperties = fldCharBegin.getFfData();
		customFieldData = fldCharBegin.getFldData();
	}
	
	private Boolean mergeFormat;
	/**
	 * Whether the instruction has {@code \* MERGEFORMAT}. Since 17.3.5 the whole instruction is read, in any case,
	 * though Word splits it across runs; until then only its first part was.
	 *
	 * @return whether \* MERGEFORMAT is set
	 */
	public Boolean isMergeFormat() {
		
		if (mergeFormat==null) {
			StringBuilder instr = new StringBuilder();
			for (Object o : instructions) {
				o = XmlUtils.unwrap(o);
				if (o instanceof Text) instr.append(((Text)o).getValue()); // a nested field's switches aren't this one's
			}
			mergeFormat = instr.toString().toUpperCase().contains("MERGEFORMAT");
		}
		return mergeFormat;
	}

	/**
	 * Specifies that this field has been flagged by an application to indicate that its current results
	 * are invalid (stale) due to other modifications made to the document, and these contents should be 
	 * updated before they are displayed.
	 *
	 * <p>Read from the begin {@code w:fldChar}'s {@code w:dirty}; absent is false. Until 17.3.5 this
	 * returned a copy taken only once {@link #getFormFieldProperties()} had been called, so false for
	 * most fields whatever the document said (issue 697).</p>
	 *
	 * @return whether stale
	 * @see <a href="http://webapp.docx4java.org/OnlineDemo/ecma376/WordML/fldChar.html">the spec</a>
	 */
	public boolean isDirty() {
		return fldCharBegin != null && fldCharBegin.isDirty();
	}
	/**
	 * @param whether stale
	 * @see <a href="http://webapp.docx4java.org/OnlineDemo/ecma376/WordML/fldChar.html">the spec</a>
	 */
	public void setDirty(boolean dirty) {
		fldCharBegin.setDirty(dirty);
		
		// Note that this doesn't set dirty on any nested fields.  TODO: Consider whether it should.
	}

	/**
	 * Read from the begin {@code w:fldChar}'s {@code w:fldLock}; absent is false. Until 17.3.5 this
	 * returned a copy taken only once {@link #getFormFieldProperties()} had been called, so false for
	 * most fields, a locked one included (issue 697).
	 *
	 * @return the lock
	 * @see <a href="http://webapp.docx4java.org/OnlineDemo/ecma376/WordML/fldChar.html">the spec</a>
	 */
	public boolean isLock() {
		return fldCharBegin != null && fldCharBegin.isFldLock();
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
		if (formFieldProperties==null) {
			processFldBegin();
		}
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
	 * A list of the content between the w:fldChar begin and separate elements;
	 * in the simplest case, this will be a single w:instrText object;
	 * in a more general case of nested fields it will be a mixture of w:instrText and FieldRef objects
	 * (and possibly other things such as w:br).
	 * 
	 * To obtain the effective instruction in the general case, you'd need to process
	 * bottom-up ie evaluate the nested instructions (in the FieldRef objects).
	 * This is not done currently.
	 */
	public List<Object> getInstructions() {
		return instructions;
	}
	private List<Object> instructions = new ArrayList<Object>();
	
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
	
	/**
	 * Empties the result, as a merge does for a field it has no value for.
	 *
	 * @since 17.3.5
	 */
	public void clearResult() {
		resultsSlot.getContent().clear();
		removeRestOfResult();
	}

	/**
	 * Removes the old result's runs after the results slot. FieldsPreprocessor puts a result's runs into the slot,
	 * but a run after something else in the result (a bookmark, often Word's _GoBack) starts another; those runs
	 * go, and the bookmarks stay where they are. Since 17.3.5, when FieldsPreprocessor began keeping every result.
	 */
	private void removeRestOfResult() {
		if (parent == null || resultsSlot == null || endRun == null) return;
		List<Object> content = parent.getContent();
		int slot = content.indexOf(resultsSlot);
		int end = content.indexOf(endRun);
		if (slot < 0 || end < slot) return;
		for (int i = end - 1; i > slot; i--) {
			if (XmlUtils.unwrap(content.get(i)) instanceof R) content.remove(i);
		}
	}

	/**
	 * Gives the result about to be written the formatting it had before 17.3.5, for the fields whose old result
	 * FieldsPreprocessor dropped then (a MERGEFIELD, a DOCPROPERTY): with {@code \* MERGEFORMAT}, the old result's,
	 * as Word keeps it (it now has one to keep); without, the end run's. Call before {@link #setResult(String)}.
	 *
	 * @since 17.3.5
	 */
	public void formatNewResult() {
		if (!isMergeFormat()) {
			resultsSlot.setRPr(endRun == null || endRun.getRPr() == null ? null : XmlUtils.deepCopy(endRun.getRPr()));
		}
	}

	/**
	 * Writes the result as these runs (copies), the first's text and properties in the results slot and the rest
	 * after it: a REF's result in the bookmark's own formatting (CR-067 phase 1).
	 *
	 * @since 17.3.5
	 */
	public void setResultRuns(List<R> runs) {
		resultsSlot.getContent().clear();
		removeRestOfResult();
		if (runs == null || runs.isEmpty()) return;
		R first = runs.get(0);
		resultsSlot.setRPr(first.getRPr() == null ? null : XmlUtils.deepCopy(first.getRPr()));
		for (Object c : first.getContent()) resultsSlot.getContent().add(XmlUtils.deepCopy(c));
		if (parent == null) return;
		List<Object> content = parent.getContent();
		int at = content.indexOf(resultsSlot) + 1;
		for (int i = 1; i < runs.size(); i++) content.add(at++, XmlUtils.deepCopy(runs.get(i)));
	}

	public void setResult(String val) {
		
		resultsSlot.getContent().clear();
		removeRestOfResult(); // the whole old result is replaced (since 17.3.5)
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
