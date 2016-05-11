/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License. 

    You may obtain a copy of the License at 

        http://www.apache.org/licenses/LICENSE-2.0 

    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS, 
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
    See the License for the specific language governing permissions and 
    limitations under the License.

 */

package org.docx4j.openpackaging.parts.PresentationML;

import java.util.Random;

import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.exceptions.PartUnrecognisedException;
import org.docx4j.openpackaging.parts.JaxbXmlPartXPathAware;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.pptx4j.jaxb.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public abstract class JaxbPmlPart<E> extends JaxbXmlPartXPathAware<E>  {
	
	protected static Logger log = LoggerFactory.getLogger(JaxbPmlPart.class);
	
	
	public final static String COMMON_SLIDE_DATA = 
	    "<p:cSld  xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" xmlns:p=\"http://schemas.openxmlformats.org/presentationml/2006/main\">"
		    + "<p:spTree>"
		        + "<p:nvGrpSpPr>"
		            + "<p:cNvPr id=\"1\" name=\"\"/>"
		            + "<p:cNvGrpSpPr/>"
		            + "<p:nvPr/>"
		        + "</p:nvGrpSpPr>"
		        + "<p:grpSpPr>"
		            + "<a:xfrm>"
		                + "<a:off x=\"0\" y=\"0\"/>"
		                + "<a:ext cx=\"0\" cy=\"0\"/>"
		                + "<a:chOff x=\"0\" y=\"0\"/>"
		                + "<a:chExt cx=\"0\" cy=\"0\"/>"
		            + "</a:xfrm>"
		        + "</p:grpSpPr>"
		    + "</p:spTree>"
		+ "</p:cSld>";	
	
	protected final static String COLOR_MAPPING = "<p:clrMap xmlns:p=\"http://schemas.openxmlformats.org/presentationml/2006/main\" bg1=\"lt1\" tx1=\"dk1\" bg2=\"lt2\" tx2=\"dk2\" accent1=\"accent1\" accent2=\"accent2\" accent3=\"accent3\" accent4=\"accent4\" accent5=\"accent5\" accent6=\"accent6\" hlink=\"hlink\" folHlink=\"folHlink\"/>";
	
	protected static Random random = new Random();
	
	public static long getSlideLayoutOrMasterId() {
		// See spec 4.8.18 (ST_SlideLayoutId) and 4.8.20 (ST_SlideMasterId)
		long val = random.nextInt(2147483647) + 2147483648l;
		return val;
	}
	protected long getSlideId() {
		// See spec 4.8.17 (ST_SlideId)
		long val = random.nextInt(2147483392) + 256;
		return val;
	}
	
	public JaxbPmlPart(PartName partName) throws InvalidFormatException {
		super(partName);
		setJAXBContext(Context.jcPML);						
	}

	public JaxbPmlPart() throws InvalidFormatException {
		super(new PartName("/ppt/presentation.xml"));
		setJAXBContext(Context.jcPML);						
	}

	public static Part newPartForContentType(String contentType, String partName)
	throws InvalidFormatException, PartUnrecognisedException {
		
		if (contentType.equals(ContentTypes.PRESENTATIONML_MAIN)
				|| contentType.equals(ContentTypes.PRESENTATIONML_TEMPLATE)
				|| contentType.equals(ContentTypes.PRESENTATIONML_MACROENABLED)
				|| contentType.equals(ContentTypes.PRESENTATIONML_TEMPLATE_MACROENABLED)
				) {
			return new MainPresentationPart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.PRESENTATIONML_SLIDE)) {
			return new SlidePart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.PRESENTATIONML_SLIDE_MASTER)) {
			return new SlideMasterPart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.PRESENTATIONML_SLIDE_LAYOUT)) {
			return new SlideLayoutPart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.PRESENTATIONML_COMMENTS)) {
			return new CommentsPart(new PartName(partName));			
		} else if (contentType.equals(ContentTypes.PRESENTATIONML_TABLE_STYLES)) {
			return new TableStylesPart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.PRESENTATIONML_PRES_PROPS)) {
			return new PresentationPropertiesPart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.PRESENTATIONML_VIEW_PROPS)) {
			return new ViewPropertiesPart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.PRESENTATIONML_TAGS)) {
			return new TagsPart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.PRESENTATIONML_HANDOUT_MASTER)) {
			return new HandoutMasterPart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.PRESENTATIONML_NOTES_MASTER)) {
			return new NotesMasterPart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.PRESENTATIONML_NOTES_SLIDE)) {
			return new NotesSlidePart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.PRESENTATIONML_COMMENT_AUTHORS)) {
			return new CommentAuthorsPart(new PartName(partName));			
		} else {
			throw new PartUnrecognisedException("No subclass found for " 
					+ partName + " (content type '" + contentType + "')");					
		}
	}	
	
	
}
