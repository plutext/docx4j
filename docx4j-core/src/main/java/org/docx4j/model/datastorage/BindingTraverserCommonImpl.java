package org.docx4j.model.datastorage;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.opendope.XPathsPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	

}
