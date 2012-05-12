package org.docx4j.openpackaging.parts.WordprocessingML;

import java.io.InputStream;

import net.arnx.wmf2svg.gdi.svg.SvgGdi;
import net.arnx.wmf2svg.gdi.wmf.WmfParser;

import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.ExternalTarget;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.utils.BufferUtil;
import org.w3c.dom.Document;

/**
 * Summary: docx4j can convert WMF files to SVG using pure Java approach.
 * 
 * Note regarding options for converting WMF files to SVG and/or PNG
 * (as at Feb 2010):

	- com.adobe.dp.office
	- wmf2svg
	- batik
	- freehep
	- imagemagick
	- openoffice
	
	wmf2tosvg is a good solution for WMF (although it has no EMF support). 
	
	For WMF, we'll use it. This can be revisited if/when com.adobe.dp.office 
	improves.
	
	(EMF is the real problem - see note in MetafileEmfPart)
		
	Batik has WMFTranscoder, but not EMFTranscoder! Even if it did,
	Batik is best avoided as it is no good on appengine since it uses 
	awt, and spawns threads. 
	
	imagemagick does a nice job with WMF, but has no support for EMF at all 
	(other than on Windows). So given that we are stuck with a partial
	solution, its best to use pure Java.
	
	For completeness, a note that openoffice can be used to convert EMF 
	and WMF; I found it worked well. 	

*/
public class MetafileWmfPart extends MetafilePart {
	
	
	public MetafileWmfPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}
	
	public MetafileWmfPart(ExternalTarget externalTarget) {
		super(externalTarget);
		init();
	}	
	
	public void init() {
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.IMAGE_WMF));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.IMAGE);
	}
	
	
	public SvgDocument toSVG() throws Exception {
		
		InputStream is = BufferUtil.newInputStream(this.getBuffer() );
		return new SvgDocument(is);
		// net.arnx.wmf2svg.Main.output(doc, out);
		
	}
	
	public static class SvgDocument {
		
		Document doc = null;
		public Document getDomDocument() {
			return doc;
		}

		public SvgDocument(InputStream wmfStream) throws Exception {

			WmfParser parser = new WmfParser();
			SvgGdi gdi = new SvgGdi();
			parser.parse(wmfStream, gdi);
			doc = gdi.getDocument();
		}
		
		
		
	}
	

}
