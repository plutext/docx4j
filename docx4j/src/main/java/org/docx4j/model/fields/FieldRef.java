package org.docx4j.model.fields;

import javax.xml.bind.JAXBElement;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;

/**
 * There are simple fields:
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
	
	private P parent;
	public P getParent() {
		return parent;
	}
	public void setParent(P parent) {
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
            
        Store a reference to it so we can delete it.
	 */
	private R beginRun;
	public R getBeginRun() {
		return beginRun;
	}
	public void setBeginRun(R beginRun) {
		this.beginRun = beginRun;
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
	
	private JAXBElement<Text> instrText;
//	public JAXBElement<Text> getInstrText() {
//		return instrText;
//	}
	public String getInstr() {
		return instrText.getValue().getValue();
	}
	public void setInstrText(JAXBElement<Text> instrText) {
		this.instrText = instrText;
	}

	private R resultsSlot;
	public R getResultsSlot() {
		return resultsSlot;
	}
	public void setResultsSlot(R resultsSlot) {
		this.resultsSlot = resultsSlot;
	}
	
	public void setResult(String val) {
		
		Text t = null;
		if (resultsSlot.getContent().size()==0) {
			t = Context.getWmlObjectFactory().createText();
			resultsSlot.getContent().add(t);
		} else {
			// Assume child Text
			t = (Text)XmlUtils.unwrap(resultsSlot.getContent().get(0));
		}
		t.setValue(val);		
	}

}
