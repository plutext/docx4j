package org.docx4j.markdown;

import java.lang.reflect.Method;
import java.util.List;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Routes embedded HTML blocks through docx4j-ImportXHTML, if it is on the
 * classpath — via reflection, so docx4j-markdown does not depend on it.
 */
class XhtmlFallback {

	private static final Logger log = LoggerFactory.getLogger(XhtmlFallback.class);

	private static final String IMPORTER_CLASS = "org.docx4j.convert.in.xhtml.XHTMLImporterImpl";

	private XhtmlFallback() {
	}

	/**
	 * @return the converted block content, or null if ImportXHTML is absent
	 *         or the fragment could not be converted (in which case the
	 *         caller drops the HTML)
	 */
	@SuppressWarnings("unchecked")
	static List<Object> tryConvert(WordprocessingMLPackage pkg, String html) {
		try {
			Class<?> clazz = Class.forName(IMPORTER_CLASS);
			Object importer = clazz.getConstructor(WordprocessingMLPackage.class).newInstance(pkg);
			Method convert = clazz.getMethod("convert", String.class, String.class);
			return (List<Object>) convert.invoke(importer, html, null);
		} catch (ClassNotFoundException e) {
			log.warn("HtmlPolicy.IMPORT_XHTML but docx4j-ImportXHTML is not on the classpath; dropping HTML");
			return null;
		} catch (Exception e) {
			log.warn("ImportXHTML could not convert the HTML fragment (is it well-formed XHTML?); dropping it", e);
			return null;
		}
	}

}
