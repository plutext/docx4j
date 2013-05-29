package org.docx4j.model.fields;

import java.util.List;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.docx4j.XmlUtils;
import org.docx4j.model.Model;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.P;
import org.w3c.dom.Node;

/** Model for the hyperlink tag.<br>
 *  
 * 
 */
public class HyperlinkModel extends Model {
	
	private static Logger log = Logger.getLogger(HyperlinkModel.class);
	
	public static final String MODEL_ID = "w:hyperlink";
	
	protected Node content = null;

	/** field-argument of the Hyperlink field or the targetUri of the relationship.
	 * corresponds to switch \l field-argument, the \l switch might be omitted.
	 */
	protected String target = null;
	
	/** Only avaiable if the information is read from a relationship. 
	 *  Otherwise always false.
	 */
	protected boolean external = false;
	
	/**
	 * Specifies the name of a bookmark in the current document which shall be the target of
	 * this hyperlink.<br> 
	 * If this attribute is omitted, then the default behavior shall be to navigate to the start of
	 * the document. If a hyperlink target is also specified using the r:id attribute, then this
	 * attribute shall be ignored.<br>
	 */
	protected String anchor = null;
	
	/**
	 * Specifies a location in the target of the hyperlink that has no bookmarks. The method by
	 * which the contents of this attribute are linked to document text is outside the scope of
	 * ECMA-376.
	 */
	protected String docLocation = null;
	
	//history - ignored
	
	/** 
	 * Specifies the ID of the relationship whose target shall be used as the target for this
	 * hyperlink.
	 */
	protected String rId = null;
	
	/**
	 * Specifies a frame within the parent HTML frameset for the target of the parent hyperlink
	 * when one exists.
	 * corresponds to switch \t field-argument
	 * corresponds to switch \n with tgtFrame = "_blank"
	 */
	protected String tgtFrame = null;

	/**
	 * Specifies a string which can be surfaced in a user interface as associated with the parent
	 * hyperlink.
	 * corresponds to switch \o field-argument
	 */
	protected String tooltip = null;

	// imageMapCoordinates switch \m ignored - requires a ismap attribute in an embedded img
	
//	P.Hyperlink pHyperlink;
//	/**
//	 * Where this model was built with a P.Hyperlink,
//	 * here it is.  Useful for debugging.
//	 * @return
//	 */
//	public P.Hyperlink getPHyperlink() {
//		return pHyperlink;
//	}
	
	@Override
	/** Default build method, get's called with a P.Hyperlink. 
	 */
	public void build(Object node, Node content) throws TransformerException {
		
		Relationship relationship = null;
		RelationshipsPart rPart = null;
		P.Hyperlink pHyperlink = (P.Hyperlink)node;
		
//		log.debug(XmlUtils.marshaltoString(hyperlink, true, true));
		
		this.content = content;
		setAnchor(pHyperlink.getAnchor());
		setDocLocation(pHyperlink.getDocLocation());
		setRId(pHyperlink.getId());
		setTgtFrame(pHyperlink.getTgtFrame());
		setTooltip(pHyperlink.getTooltip());
		if (getCurrentPart() == null) {
			log.warn("set currentPart (via conversionContext)");
		} else if ((getRId() != null) && 
			(getRId().length() > 0) ) {
			rPart = getCurrentPart().getRelationshipsPart();
			if (rPart == null) {
				log.error("RelationshipsPart is missing!");				
			} else {
				log.debug("looking for rel" + getRId());
				relationship = rPart.getRelationshipByID(getRId());
				if ((relationship != null) &&
					(Namespaces.HYPERLINK.equals(relationship.getType()))) {
					setTarget(relationship.getTarget());
					setExternal("External".equals(relationship.getTargetMode()));
				}
			}
		}
	}
	
	/** Read the relationship information from the relationships of
	 *  the parent part. 
	 * 
	 * @param parentPart
	 */
	protected void updateRelationshipData(Part parentPart) {
	RelationshipsPart rPart = (parentPart != null ?
							   parentPart.getRelationshipsPart() : null);
	Relationship relationship = null;
		if ((getRId() != null) && 
			(getRId().length() > 0) &&
			(rPart != null)) {
			relationship = rPart.getRelationshipByID(getRId());
			if ((relationship != null) &&
				(Namespaces.HYPERLINK.equals(relationship.getType()))) {
				setTarget(relationship.getTarget());
				setExternal("External".equals(relationship.getTargetMode()));
			}
		}
	}
	
	/** Custom build method, get's used with a FldSimpleModel in those cases
	 *  where the hyperlink is defined within a Field
	 */
	public void build(FldSimpleModel fldSimpleModel, Node content) throws TransformerException {
		
		int idx = 0;
		List<String> parameters = fldSimpleModel.getFldParameters();
		String parameter = null;
		boolean isSwitch = false;
		char switchChar = '\0';
		String switchParameter = null;
		
		this.content = content;
		while (idx < parameters.size()) {
			parameter = parameters.get(idx);
			if ((parameter != null) && (parameter.length() > 0)) {//should allways be true
				isSwitch = ((parameter.charAt(0) == '\\') && (parameter.length() == 2));
				if (isSwitch) {
					switchChar = Character.toLowerCase(parameter.charAt(1));
					switch (switchChar) {
						case 'l': //target
							switchParameter = FormattingSwitchHelper.getSwitchValue(idx + 1, parameters);
							if (switchParameter != null) {
								setTarget(switchParameter);
								idx++;
							}
							break;
						case 't': //target frame name
							switchParameter = FormattingSwitchHelper.getSwitchValue(idx + 1, parameters);
							if (switchParameter != null) {
								setTgtFrame(switchParameter);
								idx++;
							}
							break;
						case 'n': //target frame = "_blank"
							setTgtFrame("_blank");
							break;
						case 'o': //tooltip
							switchParameter = FormattingSwitchHelper.getSwitchValue(idx + 1, parameters);
							if (switchParameter != null) {
								setTooltip(switchParameter);
								idx++;
							}
							break;
						case 'm': //image map coordinates
							break;
					}
				}
				else { 
					//should only happen once with the first value, all others should be preceeded by a switch
					if (idx == 0) {
						setTarget(FormattingSwitchHelper.getSwitchValue(idx, parameters));
					}
				}
			}
			idx++;
		}
		if ((getTarget() != null) && 
			(getTarget().length() > 0) &&
			((getTarget().indexOf('/') > -1) ||
			 (getTarget().indexOf('\\') > -1) ||
			 (getTarget().indexOf('.') > -1) ||
			 (getTarget().indexOf(':') > -1))) {
			/* Don't know if this is correct: assume an external target if it contains
			 * one of the following characters: \/:.
			 */
			setExternal(true);
		}
	}

	@Override
	public Object toJAXB() {
		return null;
	}
	
	public String getExternalTarget() {
		return (isExternal() ? getTarget() : null);
	}
	
	public String getInternalTarget() {
		
		String ret = (isExternal() ? null : getTarget());
		if (ret == null) {
			ret = getAnchor(); // this is a target, see http://webapp.docx4java.org/OnlineDemo/ecma376/WordML/hyperlink_2.html
		}
		if (ret == null) {
			ret = getDocLocation();
		}
		return ret;
	}
	
	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public boolean isExternal() {
		return external;
	}

	public void setExternal(boolean external) {
		this.external = external;
	}

	public String getAnchor() {
		return anchor;
	}

	public void setAnchor(String anchor) {
		this.anchor = anchor;
	}

	public String getDocLocation() {
		return docLocation;
	}

	public void setDocLocation(String docLocation) {
		this.docLocation = docLocation;
	}

	public String getRId() {
		return rId;
	}

	public void setRId(String rId) {
		this.rId = rId;
	}

	public String getTgtFrame() {
		return tgtFrame;
	}

	public void setTgtFrame(String tgtFrame) {
		this.tgtFrame = tgtFrame;
	}

	public String getTooltip() {
		return tooltip;
	}

	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}
	
	public Node getContent() {
		return content;
	}

	@Override
	public String toString() {
		return "HyperlinkModel [target=" + target + ", external=" + external
				+ ", anchor=" + anchor + ", docLocation=" + docLocation
				+ ", rId=" + rId + ", tgtFrame=" + tgtFrame + "]";
	}

}
