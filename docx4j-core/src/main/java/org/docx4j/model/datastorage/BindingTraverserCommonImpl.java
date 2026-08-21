package org.docx4j.model.datastorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.model.sdt.QueryString;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.opendope.XPathsPart;
import org.docx4j.utils.XPathFactoryUtil;
import org.docx4j.wml.CTDataBinding;
import org.docx4j.wml.CTSdtText;
import org.docx4j.wml.P;
import org.docx4j.wml.SdtElement;
import org.docx4j.wml.SdtPr;
import org.docx4j.wml.Tag;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.NodeList;

public abstract class BindingTraverserCommonImpl implements BindingTraverserInterface {
	
	private static Logger log = LoggerFactory.getLogger(BindingTraverserCommonImpl.class);		
	
	
	public abstract Object traverseToBind(JaxbXmlPart part,
			org.docx4j.openpackaging.packages.OpcPackage pkg,
			Map<String, org.opendope.xpaths.Xpaths.Xpath> xpathsMap)
			throws Docx4JException;
	
	
	protected AtomicInteger bookmarkId = null;

	@Override
	public void setStartingIdForNewBookmarks(AtomicInteger bookmarkId) {
		this.bookmarkId = bookmarkId;
		
	}
	
	
	@Override
	public AtomicInteger getNextBookmarkId() {
		return bookmarkId;

	}

	protected static boolean isMultiline(SdtPr sdtPr) {

		for (Object o : sdtPr.getRPrOrAliasOrLock() ) {
			o = XmlUtils.unwrap(o);
			if (o instanceof CTSdtText) {
				return ((CTSdtText)o).isMultiLine();
			}
		}
		return false;
	}

	protected static boolean isRichText(SdtPr sdtPr) {

		for (Object o : sdtPr.getRPrOrAliasOrLock() ) {
			o = XmlUtils.unwrap(o);
			if (o instanceof SdtPr.RichText) return true;
		}
		return false;
	}

	/**
	 * Generate the content for a text-bound sdt, routing through
	 * BindingTraverserXSLT.xpathGenerateRuns / the pluggable ValueInserterPlainText
	 * exactly as the XSLT pathway does - so the sdtPr's w:rPr is applied to the
	 * generated runs, an empty result restores the placeholder, and a custom
	 * inserter set via BindingHandler.setValueInserterPlainText is honoured.
	 *
	 * The binding is resolved from the od:xpath tag id via xpathsMap where present
	 * (as bind.xslt does), falling back to the sdt's own w:dataBinding (which also
	 * covers w15:dataBinding, since SdtPr.getDataBinding() returns either).
	 *
	 * @return the generated content objects, or null where the value could not be
	 * obtained (in which case leave the sdt as it is)
	 * @since 17.0.4
	 */
	protected List<Object> generateBoundContent(
			org.docx4j.openpackaging.packages.OpcPackage pkg,
			JaxbXmlPart sourcePart,
			SdtPr sdtPr,
			Map<String, org.opendope.xpaths.Xpaths.Xpath> xpathsMap,
			boolean multiLine) {

		String storeItemId = null;
		String xpath = null;
		String prefixMappings = null;

		Tag tag = sdtPr.getTag();
		if (tag!=null && xpathsMap!=null) {
			HashMap<String, String> map = QueryString.parseQueryString(tag.getVal(), true);
			String xpathId = map.get(OpenDoPEHandler.BINDING_ROLE_XPATH);
			if (xpathId!=null) {
				org.opendope.xpaths.Xpaths.Xpath xp = xpathsMap.get(xpathId);
				if (xp==null) {
					log.warn("Couldn't find xpath with id " + xpathId);
				} else {
					storeItemId = xp.getDataBinding().getStoreItemID();
					xpath = xp.getDataBinding().getXpath();
					prefixMappings = xp.getDataBinding().getPrefixMappings();
				}
			}
		}
		if (xpath==null) {
			CTDataBinding binding = sdtPr.getDataBinding();
			if (binding==null) {
				log.warn("No binding found for " + (tag==null ? "(no tag)" : tag.getVal()));
				return null;
			}
			storeItemId = binding.getStoreItemID();
			xpath = binding.getXpath();
			prefixMappings = binding.getPrefixMappings();
		}

		DocumentFragment frag = BindingTraverserXSLT.xpathGenerateRuns(
				null /* no path cache here */,
				(WordprocessingMLPackage)pkg, sourcePart,
				pkg.getCustomXmlDataStorageParts(),
				storeItemId, xpath, prefixMappings,
				sdtPr, null, null, multiLine,
				new BindingTraverserXSLT.BookmarkCounter(
						bookmarkId==null ? new AtomicInteger() : bookmarkId));
		if (frag==null) return null;

		List<Object> contents = new ArrayList<Object>();
		for (org.w3c.dom.Node n = frag.getFirstChild(); n!=null; n = n.getNextSibling()) {
			if (n.getNodeType()!=org.w3c.dom.Node.ELEMENT_NODE) continue;
			Class<?> declaredType = declaredTypeFor(n.getLocalName());
			if (declaredType==null) {
				log.warn("Skipping unexpected generated element " + n.getLocalName());
				continue;
			}
			try {
				contents.add(XmlUtils.unmarshal(n, Context.jc, declaredType));
			} catch (jakarta.xml.bind.JAXBException e) {
				log.error(e.getMessage(), e);
				return null;
			}
		}
		return contents;
	}

	private static Class<?> declaredTypeFor(String localName) {
		if ("r".equals(localName)) return org.docx4j.wml.R.class;
		if ("hyperlink".equals(localName)) return P.Hyperlink.class;
		if ("p".equals(localName)) return P.class;
		if ("tbl".equals(localName)) return Tbl.class;
		if ("tr".equals(localName)) return Tr.class;
		if ("tc".equals(localName)) return Tc.class;
		if ("bookmarkStart".equals(localName)) return org.docx4j.wml.CTBookmark.class;
		if ("bookmarkEnd".equals(localName)) return org.docx4j.wml.CTMarkupRange.class;
		return null;
	}

	/**
	 * Put the generated content into the sdt, rebuilding the existing content's
	 * structure as bind.xslt does: the generated runs land in the (first) paragraph
	 * of whatever p / tc / tr / tbl skeleton the template content had; other
	 * template content is replaced outright.
	 * @since 17.0.4
	 */
	protected void applyBoundContent(SdtElement sdt, List<Object> contents) {

		List<Object> sdtContent = sdt.getSdtContent().getContent();
		Object first = sdtContent.isEmpty() ? null : XmlUtils.unwrap(sdtContent.get(0));

		if (first instanceof P) {
			P p = (P)first;
			p.getContent().clear();
			p.getContent().addAll(contents);

		} else if (first instanceof Tc) {
			fillTc((Tc)first, contents);

		} else if (first instanceof Tr) {
			Tr tr = (Tr)first;
			Tc tc = firstDescendant(tr.getContent(), Tc.class);
			tr.getContent().clear();
			if (tc==null) tc = Context.getWmlObjectFactory().createTc();
			fillTc(tc, contents);
			tr.getContent().add(tc);

		} else if (first instanceof Tbl) {
			Tbl tbl = (Tbl)first;
			Tr tr = firstDescendant(tbl.getContent(), Tr.class);
			Tc tc = tr==null ? null : firstDescendant(tr.getContent(), Tc.class);
			tbl.getContent().clear();
			if (tr==null) tr = Context.getWmlObjectFactory().createTr();
			tr.getContent().clear();
			if (tc==null) tc = Context.getWmlObjectFactory().createTc();
			fillTc(tc, contents);
			tr.getContent().add(tc);
			tbl.getContent().add(tr);

		} else {
			// run-level sdt (content is run(s) directly), or empty
			sdtContent.clear();
			sdtContent.addAll(contents);
		}
	}

	private void fillTc(Tc tc, List<Object> contents) {
		P p = firstDescendant(tc.getContent(), P.class);
		tc.getContent().clear();
		if (p==null) p = Context.getWmlObjectFactory().createP();
		p.getContent().clear();
		p.getContent().addAll(contents);
		tc.getContent().add(p);
	}

	private static <T> T firstDescendant(List<Object> list, Class<T> type) {
		for (Object o : list) {
			o = XmlUtils.unwrap(o);
			if (type.isInstance(o)) return type.cast(o);
		}
		return null;
	}

	/**
	 * Bind a w14:checkbox content control: set w14:checked per the bound value,
	 * and replace the content with the appropriate glyph run, as bind.xslt does.
	 * @since 17.0.4
	 */
	protected void applyCheckboxBinding(SdtElement sdt,
			org.docx4j.openpackaging.packages.OpcPackage pkg) {

		SdtPr sdtPr = sdt.getSdtPr();
		Boolean result = BindingTraverserXSLT.getCheckboxResult(
				pkg.getCustomXmlDataStorageParts(), sdtPr);

		List<Object> contents = new ArrayList<Object>(1);
		if (result==null) {
			contents.add(missingRun());
		} else {
			org.docx4j.w14.CTSdtCheckbox checkbox
					= (org.docx4j.w14.CTSdtCheckbox)sdtPr.getByClass(org.docx4j.w14.CTSdtCheckbox.class);
			if (checkbox.getChecked()==null) {
				checkbox.setChecked(new org.docx4j.w14.CTOnOff());
			}
			checkbox.getChecked().setVal(result.booleanValue() ? "1" : "0");

			contents.add(BindingTraverserXSLT.checkboxRun(result.booleanValue(),
					(org.docx4j.wml.RPr)sdtPr.getByClass(org.docx4j.wml.RPr.class)));
		}
		applyBoundContent(sdt, contents);
	}

	/**
	 * Bind a w:date content control: replace the content with the value
	 * formatted per the w:date settings, as bind.xslt does.
	 * @since 17.0.4
	 */
	protected void applyDateBinding(SdtElement sdt,
			org.docx4j.openpackaging.packages.OpcPackage pkg) {

		org.docx4j.wml.R run = BindingTraverserXSLT.xpathDateRun(
				pkg.getCustomXmlDataStorageParts(), sdt.getSdtPr());

		List<Object> contents = new ArrayList<Object>(1);
		contents.add(run==null ? missingRun() : run);
		applyBoundContent(sdt, contents);
	}

	/**
	 * Equivalent of BindingTraverserXSLT.nullResultParagraph's marker content.
	 */
	private static org.docx4j.wml.R missingRun() {
		org.docx4j.wml.R run = Context.getWmlObjectFactory().createR();
		org.docx4j.wml.Text text = Context.getWmlObjectFactory().createText();
		text.setValue("[missing!]");
		run.getContent().add(text);
		return run;
	}

	/**
	 * Evaluate an od:RptPosCon condition (eg "position()&lt;last()-1") for the repeat
	 * instance at 1-based position pos, in a repeat with size instances.
	 *
	 * The expression is applied as an XPath predicate over a node-set of size nodes,
	 * so position() and last() behave as in bind.xslt (which evaluates the expression
	 * against the node-set of repeat instances).
	 *
	 * @since 17.0.4
	 */
	protected static boolean evaluateRptPosCon(String expression, int pos, int size) {

		if (expression==null) return false;
		if (pos<1 || size<1 || pos>size) {
			log.warn("Unexpected repeat position " + pos + " of " + size);
			return false;
		}
		try {
			org.w3c.dom.Document doc = XmlUtils.neww3cDomDocument();
			org.w3c.dom.Element root = doc.createElement("r");
			doc.appendChild(root);
			for (int i=0; i<size; i++) {
				root.appendChild(doc.createElement("i"));
			}
			NodeList matched = (NodeList)XPathFactoryUtil.newXPath().evaluate(
					"i[" + expression + "]", root, XPathConstants.NODESET);
			org.w3c.dom.Node target = root.getChildNodes().item(pos-1);
			for (int i=0; i<matched.getLength(); i++) {
				if (matched.item(i).isSameNode(target)) return true;
			}
			return false;
		} catch (XPathExpressionException e) {
			log.error("Can't evaluate repeat position condition '" + expression + "'", e);
			return false;
		}
	}

}
