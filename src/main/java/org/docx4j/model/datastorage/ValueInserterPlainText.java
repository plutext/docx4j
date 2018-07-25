/**
 * 
 */
package org.docx4j.model.datastorage;

import java.util.concurrent.atomic.AtomicInteger;

import org.docx4j.model.datastorage.BindingTraverserXSLT.BookmarkCounter;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.wml.RPr;
import org.opendope.xpaths.Xpaths;
import org.w3c.dom.DocumentFragment;

/**
 * @author jharrop
 * @since 6.0.1
 */
public interface ValueInserterPlainText {
	
	DocumentFragment toOpenXml(Xpaths.Xpath.DataBinding dataBinding, RPr rPr, boolean multiLine, BookmarkCounter bookmarkCounter, String result, 
			JaxbXmlPart sourcePart) throws Docx4JException ;
	

}
