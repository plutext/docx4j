package org.docx4j.model.properties.table.tr;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.TrPr;
import org.w3c.dom.Element;

public class TrCantSplit extends AbstractTrProperty {
	
	public final static String CSS_NAME = "page-break-inside";
	public final static String FO_NAME  = "keep-together.within-page"; 

	protected static final QName CANT_SPLIT = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "cantSplit");
	
	public TrCantSplit() {
		
		JAXBElement<BooleanDefaultTrue> cantSplit = Context.getWmlObjectFactory().createCTTrPrBaseCantSplit(new BooleanDefaultTrue());
		
		setObject(cantSplit);
	}

	public TrCantSplit(JAXBElement<?> cantSplit) {
		setObject(cantSplit);
	}
	
	@Override
	public void set(TrPr trPr) {
		
		
		if (getObject()!=null) {
			JAXBElement<?> newObject = (JAXBElement<?>)getObject();			
			
			JAXBElement<?> existing = XmlUtils.getListItemByQName(trPr.getCnfStyleOrDivIdOrGridBefore(), new QName(Namespaces.NS_WORD12, "cantSplit"));
			if (existing==null) {
				
				// just add it
				trPr.getCnfStyleOrDivIdOrGridBefore().add(newObject);
				
			} else {
				
				// replace it
				trPr.getCnfStyleOrDivIdOrGridBefore().remove(existing);
				trPr.getCnfStyleOrDivIdOrGridBefore().add(newObject);
			}
		}
	}

	/* 
	 * Note that browser support for CSS page-break-inside: avoid is limited
	 * (apparently it works in IE 9, but not in Chrome 22 or Firefox 15).
	 * See:
	 *   http://stackoverflow.com/questions/1539876/controlling-css-page-breaks-when-printing-in-webkit
	 *   
	 * In any case, wiring this class up to our XHTML output/import remains
	 * a small TODO.
	 *   
	 * (non-Javadoc)
	 * @see org.docx4j.model.properties.Property#getCssProperty()
	 */
	@Override
	public String getCssProperty() {

		JAXBElement<?> cantSplit = (JAXBElement<?>)getObject();			
		if (cantSplit!=null) {
			BooleanDefaultTrue val = (BooleanDefaultTrue)XmlUtils.unwrap(cantSplit);
			if (val.isVal()) {
				return composeCss(getCssName(), "avoid");
			}
		}
		return null;
	}

	@Override
	public String getCssName() {
		return CSS_NAME;
	}

	@Override
	public void setXslFO(Element foElement) {

		JAXBElement<?> cantSplit = (JAXBElement<?>)getObject();			
		if (cantSplit!=null) {
			BooleanDefaultTrue val = (BooleanDefaultTrue)XmlUtils.unwrap(cantSplit);
			if (val.isVal()) {
				foElement.setAttribute(FO_NAME, "always");
				
			}
		}
		
	}

}
