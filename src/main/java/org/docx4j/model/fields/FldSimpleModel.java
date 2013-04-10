package org.docx4j.model.fields;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.docx4j.XmlUtils;
import org.docx4j.model.Model;
import org.docx4j.wml.CTSimpleField;
import org.w3c.dom.Node;

/** Just a basic model for w:fldSimple that gets used in the 
 *  FldSimpleModelConverter for the conversion to pdf/html
 * 
 *  
 *  field:
 *  	field-type field-argument [switches]
 */
public class FldSimpleModel extends Model {
	
	private static Logger log = Logger.getLogger(FldSimpleModel.class);			
	
	public static final String MODEL_ID = "w:fldSimple";
	
	protected CTSimpleField fldSimple = null;
	protected Node content = null;
	protected String fldType = null;
	protected String fldArgument = null; 
	protected String fldParameterString = null;
	protected List<String> fldParameters = null;
	
	@Override
	public void build(Object node, Node content) throws TransformerException {
		this.fldSimple = (CTSimpleField)node;
		
		log.debug(XmlUtils.marshaltoString(fldSimple, true, true));
		
		this.content = content;
		setupNameParameterString(fldSimple.getInstr());
	}
	
	public void build(String inStr) throws TransformerException {
		reset();
		setupNameParameterString(inStr);
	}
	
	protected void setupNameParameterString(String text) {
		
		int nameStart = 0;
		int nameEnd = 0;
		if ((text != null) && (text.length() > 0)) {
			while ((nameStart < text.length()) && (text.charAt(nameStart) == ' '))
				nameStart++;
			if (nameStart < text.length() ) {
				nameEnd = nameStart + 1;
				if (text.charAt(nameStart) == '=') {
					// Special case =nn
					fldType = "=";
				} else {
					while ((nameEnd < text.length()) && (text.charAt(nameEnd) != ' '))
						nameEnd++;
					fldType = text.substring(nameStart, nameEnd);
				}
			}
			if (nameStart < nameEnd) {
				if (nameEnd < text.length()) {
					fldParameterString = text.substring(nameEnd).trim();
				}
			}
		}
		if (fldType != null) fldType = fldType.toUpperCase(); 
	}

	public static List<String> splitParameters(String text) {
		
		List<String> ret = Collections.EMPTY_LIST;
		int valStart = -1;
		boolean inLiteral = false;
		char ch = '\0';
		if ((text != null) && (text.length() > 0)) {
			ret = new ArrayList<String>(4);
			for (int chidx = 0; chidx<text.length(); chidx++) {
				ch = text.charAt(chidx);
				if (ch == '"') {
					if (inLiteral) {
						//add literals with the enclosing "
						appendParameter(ret, text.substring(valStart, chidx + 1));
						inLiteral = false;
						valStart = -1;
					}
					else {
						valStart = chidx;
						inLiteral = true;
					}
				}
				else if (ch == ' ') {
					if ((valStart > -1) && (!inLiteral)) {
						appendParameter(ret, text.substring(valStart, chidx));
						valStart = -1;
					}
				}
				else if (valStart == -1) {
					valStart = chidx;
				}
			}
			if (valStart > -1) {
				appendParameter(ret, text.substring(valStart));
				valStart = -1;
			}
		}
		return ret;
	}
	
	public static void appendParameter(List<String> parameters, String value) {
		if (parameters.size() > 0) {
			String previousParameter = parameters.get(parameters.size()-1);
			if (("\\#".equals(previousParameter)) ||
				("\\@".equals(previousParameter)) ||	
				("\\*".equals(previousParameter))) {
				parameters.set(parameters.size() - 1, previousParameter + value);
			}
			else {
				parameters.add(value);
			}
		}
		else {
			parameters.add(value);
		}
	}

	public String getFldType() {
		return fldType;
	}

	public String getFldArgument() {
		return getFldParameters().get(0);
	}
	
	public String getFldParameterString() {
		return fldParameterString;
	}
	
	public List<String> getFldParameters() {
		if (fldParameters == null) {
			fldParameters = splitParameters(fldParameterString);
		}
		return fldParameters;
	}

	public CTSimpleField getFldSimple() {
		return fldSimple;
	}
	
	public Node getContent() {
		return content;
	}
	
	protected void reset() {
		fldSimple = null;
		content = null;
		fldType = null;
		fldParameterString = null;
		fldParameters = null;
	}
	
	@Override
	public Object toJAXB() {
		return fldSimple;
	}

}
