package org.docx4j.model.fields;

import org.docx4j.XmlUtils;
import org.docx4j.wml.CTSimpleField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import javax.xml.transform.TransformerException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A parsed {@code w:fldSimple} instruction: the field's name and its parameters.
 *
 * @deprecated since 17.3.5 (CR-067 D2): {@link FieldInstruction} is the one parser of a field
 *             instruction, and the writers, the updaters and the switch helper take it. This class
 *             delegates to it and is removed in the next minor release after 17.3.
 * @see <a href="http://webapp.docx4java.org/OnlineDemo/ecma376/WordML/XML.html">the spec</a>
 */
@Deprecated
public class FldSimpleModel {

	private static Logger log = LoggerFactory.getLogger(FldSimpleModel.class);

	protected CTSimpleField fldSimple = null;
	protected Node content = null;
	protected String fldName = null;
	protected String fldParameterString = null;
	protected List<String> fldParameters = null;
	private FieldInstruction instruction = null;

	public void build(CTSimpleField fldSimple, Node content) throws TransformerException {
		this.fldSimple = fldSimple;
		if (log.isDebugEnabled()) {
			log.debug("\n" + XmlUtils.marshaltoString(fldSimple, true, true));
		}
		this.content = content;
		setupNameParameterString(fldSimple.getInstr());
	}

	public void build(String inStr) throws TransformerException {
		reset();
		setupNameParameterString(inStr);
	}

	protected void setupNameParameterString(String text) {
		instruction = FieldInstruction.parse(text);
		fldName = instruction.getName();
		fldParameterString = instruction.getParameterText();
		fldParameters = null;
	}

	/** A model over an instruction already parsed, for a handler on this class's API. */
	public static FldSimpleModel of(FieldInstruction instruction, CTSimpleField fldSimple, Node content) {
		FldSimpleModel m = new FldSimpleModel();
		m.instruction = instruction == null ? FieldInstruction.parse(null) : instruction;
		m.fldName = m.instruction.getName();
		m.fldParameterString = m.instruction.getParameterText();
		m.fldSimple = fldSimple;
		m.content = content;
		return m;
	}

	/** The instruction this model parsed, as {@link FieldInstruction} reads it. */
	public FieldInstruction toInstruction() {
		if (instruction == null) instruction = FieldInstruction.parse(null);
		return instruction;
	}

	/** @deprecated since 17.3.5, {@link FieldInstruction#splitParameters(String)} */
	@Deprecated
	public static List<String> splitParameters(String text) {
		return FieldInstruction.splitParameters(text);
	}

	/** @deprecated since 17.3.5, with the class */
	@Deprecated
	public static void appendParameter(List<String> parameters, String value) {
		parameters.add(value);
	}

	/**
	 * The name of the field, for example DATE, MERGEFIELD
	 */
	public String getFldName() {
		return fldName;
	}

	public String getFldArgument() {
		return getFldParameters().get(0);
	}

	/**
	 * For example, "caContractId  \* UPPER"
	 */
	public String getFldParameterString() {
		return fldParameterString;
	}

	public List<String> getFldParameters() {
		if (fldParameters == null) {
			fldParameters = new ArrayList<String>(toInstruction().getParameters());
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
		fldName = null;
		fldParameterString = null;
		fldParameters = null;
		instruction = null;
	}
}
