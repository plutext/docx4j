package org.docx4j.model.datastorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.docx4j.TraversalUtil;
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

		org.opendope.xpaths.Xpaths.Xpath.DataBinding resolved = resolveBinding(sdtPr, xpathsMap);
		if (resolved==null) return null;
		String storeItemId = resolved.getStoreItemID();
		String xpath = resolved.getXpath();
		String prefixMappings = resolved.getPrefixMappings();

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

	/**
	 * Resolve an sdt's binding: the od:xpath tag id via xpathsMap where present
	 * (as bind.xslt does), falling back to the sdt's own w:dataBinding (which also
	 * covers w15:dataBinding, since SdtPr.getDataBinding() returns either).
	 * Null where there is no binding.
	 */
	private static org.opendope.xpaths.Xpaths.Xpath.DataBinding resolveBinding(
			SdtPr sdtPr, Map<String, org.opendope.xpaths.Xpaths.Xpath> xpathsMap) {

		Tag tag = sdtPr.getTag();
		if (tag!=null && xpathsMap!=null) {
			HashMap<String, String> map = QueryString.parseQueryString(tag.getVal(), true);
			String xpathId = map.get(OpenDoPEHandler.BINDING_ROLE_XPATH);
			if (xpathId!=null) {
				org.opendope.xpaths.Xpaths.Xpath xp = xpathsMap.get(xpathId);
				if (xp==null) {
					log.warn("Couldn't find xpath with id " + xpathId);
				} else {
					return xp.getDataBinding();
				}
			}
		}
		CTDataBinding binding = sdtPr.getDataBinding();
		if (binding==null) {
			log.warn("No binding found for " + (tag==null ? "(no tag)" : tag.getVal()));
			return null;
		}
		org.opendope.xpaths.Xpaths.Xpath.DataBinding result
				= new org.opendope.xpaths.Xpaths.Xpath.DataBinding();
		result.setStoreItemID(binding.getStoreItemID());
		result.setXpath(binding.getXpath());
		result.setPrefixMappings(binding.getPrefixMappings());
		return result;
	}

	/**
	 * Where the sdt content contains an authored drawing, replace just its
	 * a:blip/@r:embed with a rel to a new image part created from the bound
	 * base64 value, preserving everything else about the drawing - as
	 * bind.xslt's picture3/picture3richtext modes do.
	 *
	 * @return true if a blip was found (the drawing is preserved even where the
	 * value couldn't be obtained); false if there is no blip to replace
	 * @since 17.0.4
	 */
	protected boolean replaceBlipEmbed(SdtElement sdt,
			org.docx4j.openpackaging.packages.OpcPackage pkg,
			JaxbXmlPart sourcePart,
			Map<String, org.opendope.xpaths.Xpaths.Xpath> xpathsMap) {

		org.docx4j.dml.CTBlip blip = findBlip(sdt.getSdtContent().getContent());
		if (blip==null) return false;

		org.opendope.xpaths.Xpaths.Xpath.DataBinding binding = resolveBinding(sdt.getSdtPr(), xpathsMap);
		if (binding==null) return true; // nothing we can do; leave the drawing alone

		org.docx4j.openpackaging.parts.CustomXmlPart part
				= (org.docx4j.openpackaging.parts.CustomXmlPart)
						pkg.getCustomXmlDataStorageParts().get(binding.getStoreItemID().toLowerCase());
		if (part==null) {
			log.error("Couldn't locate part by storeItemId " + binding.getStoreItemID());
			return true;
		}
		try {
			String base64 = part.xpathGetString(binding.getXpath(), binding.getPrefixMappings());
			if (base64==null || base64.trim().length()==0) {
				log.warn(binding.getXpath() + " yielded no image data");
				return true;
			}
			String relId = BindingTraverserXSLT.createImagePartReturnRelId(
					(WordprocessingMLPackage)pkg, sourcePart, base64);
			if (relId!=null) {
				blip.setEmbed(relId);
			}
		} catch (Docx4JException e) {
			log.error(e.getMessage(), e);
		}
		return true;
	}

	/**
	 * The first a:blip in the content: drawing/(inline|anchor)/graphic/graphicData/pic/blipFill/blip.
	 */
	private static org.docx4j.dml.CTBlip findBlip(List<Object> content) {

		for (Object o : content) {
			o = XmlUtils.unwrap(o);
			if (o instanceof org.docx4j.wml.Drawing) {
				for (Object di : ((org.docx4j.wml.Drawing)o).getAnchorOrInline()) {
					di = XmlUtils.unwrap(di);
					org.docx4j.dml.Graphic graphic = null;
					if (di instanceof org.docx4j.dml.wordprocessingDrawing.Inline) {
						graphic = ((org.docx4j.dml.wordprocessingDrawing.Inline)di).getGraphic();
					} else if (di instanceof org.docx4j.dml.wordprocessingDrawing.Anchor) {
						graphic = ((org.docx4j.dml.wordprocessingDrawing.Anchor)di).getGraphic();
					}
					if (graphic==null || graphic.getGraphicData()==null) continue;
					for (Object any : graphic.getGraphicData().getAny()) {
						any = XmlUtils.unwrap(any);
						if (any instanceof org.docx4j.dml.picture.Pic) {
							org.docx4j.dml.picture.Pic pic = (org.docx4j.dml.picture.Pic)any;
							if (pic.getBlipFill()!=null && pic.getBlipFill().getBlip()!=null) {
								return pic.getBlipFill().getBlip();
							}
						}
					}
				}
			} else {
				List<Object> children = TraversalUtil.getChildrenImpl(o);
				if (children!=null) {
					org.docx4j.dml.CTBlip blip = findBlip(children);
					if (blip!=null) return blip;
				}
			}
		}
		return null;
	}

	/**
	 * Handle an od:Handler=picture sdt (rich text cc containing a w:drawing).
	 * With a width param, the content is replaced with a freshly sized image
	 * (as bind.xslt's 11.1.8 branch does); otherwise just the blip's r:embed
	 * is replaced, preserving the authored drawing.
	 * @since 17.0.4
	 */
	protected void applyHandlerPicture(SdtElement sdt,
			org.docx4j.openpackaging.packages.OpcPackage pkg,
			JaxbXmlPart sourcePart,
			Map<String, org.opendope.xpaths.Xpaths.Xpath> xpathsMap,
			Map<String, String> tagMap) {

		if (tagMap.containsKey("width")) {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Map<String, org.docx4j.openpackaging.parts.CustomXmlDataStoragePart> parts
					= (Map)pkg.getCustomXmlDataStorageParts();
			DocumentFragment frag = null;
			try {
				frag = BindingTraverserXSLT.xpathInjectImage(
						(WordprocessingMLPackage)pkg, sourcePart, parts, xpathsMap,
						sdt.getSdtPr().getTag().getVal(),
						"p", "r"); // request the run form; applyBoundContent does the shaping
			} catch (RuntimeException e) {
				log.error(e.getMessage(), e);
			}
			if (frag==null) return; // leave as is
			List<Object> contents = new ArrayList<Object>();
			for (org.w3c.dom.Node n = frag.getFirstChild(); n!=null; n = n.getNextSibling()) {
				if (n.getNodeType()!=org.w3c.dom.Node.ELEMENT_NODE) continue;
				Class<?> declaredType = declaredTypeFor(n.getLocalName());
				if (declaredType==null) continue;
				try {
					contents.add(XmlUtils.unmarshal(n, Context.jc, declaredType));
				} catch (jakarta.xml.bind.JAXBException e) {
					log.error(e.getMessage(), e);
					return;
				}
			}
			applyBoundContent(sdt, contents);
		} else {
			replaceBlipEmbed(sdt, pkg, sourcePart, xpathsMap);
		}
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
