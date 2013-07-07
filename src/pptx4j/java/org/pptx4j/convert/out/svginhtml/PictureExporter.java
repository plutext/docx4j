package org.pptx4j.convert.out.svginhtml;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.XmlUtils;
import org.docx4j.dml.CTBlip;
import org.docx4j.dml.CTPoint2D;
import org.docx4j.dml.CTPositiveSize2D;
import org.docx4j.model.images.AbstractWordXmlPicture;
import org.docx4j.openpackaging.parts.Part;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

public class PictureExporter extends AbstractWordXmlPicture {
	
	/**
	 * 
            <p:pic>
              <p:nvPicPr>
                <p:cNvPr id="1026" name="Picture 2"/>
                <p:cNvPicPr>
                  <a:picLocks noChangeAspect="1" noChangeArrowheads="1"/>
                </p:cNvPicPr>
                <p:nvPr/>
              </p:nvPicPr>
              <p:blipFill>
                <a:blip r:embed="rId2"/>  <-----------------------
                <a:srcRect/>
                <a:stretch>
                  <a:fillRect/>
                </a:stretch>
              </p:blipFill>
              <p:spPr bwMode="auto">
                <a:xfrm>
                  <a:off x="1357290" y="3643314"/>
                  <a:ext cx="619125" cy="742950"/>
                </a:xfrm>
                <a:prstGeom prst="rect">
                  <a:avLst/>
                </a:prstGeom>
                <a:noFill/>
                <a:ln w="9525">
                  <a:noFill/>
                  <a:miter lim="800000"/>
                  <a:headEnd/>
                  <a:tailEnd/>
                </a:ln>
                <a:effectLst/>
              </p:spPr>
            </p:pic>
            
            	 */

	protected static Logger log = LoggerFactory.getLogger(PictureExporter.class);	

	public static DocumentFragment createHtmlImg(
			SvgConversionContext context,
    		NodeIterator wpInline) {

    	PictureExporter converter = createPicture(context, wpInline);
    	
    	DocumentFragment df = getHtmlDocumentFragment(converter);
    	
    	// Now wrap
    	// <div style="position: absolute; width:300px; height:39px; left:360px; top:255px; border: red dashed;">
    	// around it
    	CTPoint2D offset = converter.pic.getSpPr().getXfrm().getOff();
    	Dimensions xy = converter.readDimensions( offset.getX(), offset.getY() );
    	
    	/*
    	 *  TODO, USE:
    	 *  
    	 *       	// Geometrical transforms
    	CTTransform2D xfrm = cxnSp.getSpPr().getXfrm();
    	Box b = new Box(xfrm.getOff().getX(), xfrm.getOff().getY(),
    			xfrm.getExt().getCx(), xfrm.getExt().getCy() );
    	
    	if (xfrm.getRot()!=0) {
    		b.rotate(xfrm.getRot());
    	}
    	if (xfrm.isFlipH() ) {
    		b.flipH();
    	}
    	if (xfrm.isFlipV() ) {
    		b.flipV();
    	}
    	
    	// Convert from EMU to pixels
    	b.toPixels();

    	 * 
    	 */
    	
    	Element div = df.getOwnerDocument().createElement("div");
    	
    	div.setAttribute("style", "position:absolute; left:" + xy.width + "px; top:" + xy.height + "px;");
    	
    	Node img = df.getFirstChild();
    	
    	df.removeChild(img);
    	df.appendChild(div);
    	
    	div.appendChild(img);
    	
    	return df;

    }
	
	
	org.pptx4j.pml.Pic pic=null;
	
    public static PictureExporter createPicture(SvgConversionContext context,
    		NodeIterator anchorOrInline) {
    	
    	PictureExporter converter = new PictureExporter();
    	
    	
    	
		try {
    		Node n = anchorOrInline.nextNode();
    		converter.pic = (org.pptx4j.pml.Pic)XmlUtils.unmarshal(n, org.pptx4j.jaxb.Context.jcPML, org.pptx4j.pml.Pic.class);
		} catch (JAXBException e1) {
			e1.printStackTrace();
		}
		
		
		log.info("** image: " + converter.pic.getClass().getName() );
    	
    	if (converter.pic.getBlipFill()==null
    			|| converter.pic.getBlipFill().getBlip()==null) {
    		log.error("blip missing!!");
    		return null;    		    		
    	}
    	
    	CTBlip blip = converter.pic.getBlipFill().getBlip();
    	
    	String imgRelId = blip.getEmbed();    	
    	if (imgRelId!=null) {
    		converter.handleImageRel(context.getImageHandler(), imgRelId, 
    				(Part)context.getResolvedLayout().relationships.getSourceP());
    	} else if (blip.getLink()!=null) {
    		converter.handleImageRel(context.getImageHandler(), blip.getLink(), 
    				(Part)context.getResolvedLayout().relationships.getSourceP());
    	} else {
    		log.error("not linked or embedded?!");
    	}

    	converter.dimensions = converter.readDimensions(
    			converter.pic.getSpPr().getXfrm().getExt());
    		// TODO: <a:off x="1357290" y="3643314"/>
    	
		return converter;
	}

    private final int extentToPixelConversionFactor = 9525; //12700;	

    
    private Dimensions readDimensions(long x, long y) {

    	Dimensions dimensions = new Dimensions();
    	
		dimensions.width= (int) x / extentToPixelConversionFactor;
		dimensions.widthUnit = "px";

		dimensions.height= (int) y / extentToPixelConversionFactor;
		dimensions.heightUnit = "px";
    	
    	return dimensions;
    }
    
    private Dimensions readDimensions(CTPositiveSize2D size2d) {
    	
    	/*
              <p:spPr bwMode="auto">
                <a:xfrm>
                  <a:off x="1357290" y="3643314"/>
                  <a:ext cx="619125" cy="742950"/>
                </a:xfrm>
    	 */
    	if (size2d==null) {
    		log.warn("wp:inline/wp:extent missing!");
    		return null;
    	}
    	
    	return readDimensions(size2d.getCx(), size2d.getCy() );
    	
    }
	
}
