package org.docx4j;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.apache.log4j.Logger;
import org.docx4j.dml.picture.Pic;
import org.docx4j.dml.wordprocessingDrawing.Anchor;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Body;
import org.docx4j.wml.CTObject;
import org.docx4j.wml.CTSdtContentRow;
import org.docx4j.wml.Pict;

/**
 * Traverse a list of JAXB objects (eg document.xml's document/body 
 * children), and do something to them.
 * 
 * This is similar to what one could do via XSLT,
 * but avoids marshalling/unmarshalling.  The downside is that
 * not everything will necessarily get traversed here
 * since visitChildren is not (yet) comprehensive.
 * 
 * @author jharrop
 *
 */
public class TraversalUtil {

	private static Logger log = Logger.getLogger(TraversalUtil.class);
	
	public interface Callback {

		void walkJAXBElements(Object parent);

		List<Object> getChildren(Object o);

		/**
		 * Visits a node in pre order (before its children have been visited).
		 * 
		 * A node is visited only if all its parents have been traversed (
		 * {@link #shouldTraverse(Object)}).</p>
		 * 
		 * <p>
		 * Implementations can have side effects.
		 * </p>
		 */
		List<Object> apply(Object o);

		/**
		 * Decide whether this node's children should be traversed.
		 * 
		 * @return whether the children of this node should be visited
		 */
		boolean shouldTraverse(Object o);

	}	

	public static abstract class CallbackImpl implements Callback {

		// Depth first
		public void walkJAXBElements(Object parent) {
			
			List children = getChildren(parent);
			if (children != null) {

				for (Object o : children) {

					// if its wrapped in javax.xml.bind.JAXBElement, get its
					// value; this is ok, provided the results of the Callback
					// won't be marshalled
					o = XmlUtils.unwrap(o);
					
					this.apply(o);

					if (this.shouldTraverse(o)) {
						walkJAXBElements(o);
					}

				}
			}
		}

		public List<Object> getChildren(Object o) {
			return TraversalUtil.getChildrenImpl(o);
		}

		/**
		 * Visits a node in pre order (before its children have been visited).
		 * 
		 * A node is visited only if all its parents have been traversed (
		 * {@link #shouldTraverse(Object)}).</p>
		 * 
		 * <p>
		 * Implementations can have side effects.
		 * </p>
		 */
		public abstract List<Object> apply(Object o);

		/**
		 * Decide whether this node's children should be traversed.
		 * 
		 * @return whether the children of this node should be visited
		 */
		public boolean shouldTraverse(Object o) {
			return true;
		}

	}

	Callback cb;

	public TraversalUtil(Object parent, Callback cb) {

		this.cb = cb;
		cb.walkJAXBElements(parent);
	}

	static void visitChildrenImpl(Object o) {

	}

	public static List<Object> getChildrenImpl(Object o) {

		log.debug("getting children of " + o.getClass().getName() );
		if (o instanceof org.docx4j.wml.Text) return null;
		
		// Short circuit for common elements
		if (o instanceof List) {
			// Handy if you have your own list of objects you wish to process
			return (List<Object>) o;
		} else if (o instanceof org.docx4j.wml.R) {
			return ((org.docx4j.wml.R) o).getRunContent();
		} else	 if (o instanceof org.docx4j.wml.P) {
			return ((org.docx4j.wml.P) o).getParagraphContent();
		} else if (o instanceof org.docx4j.wml.Tc) {
			return ((org.docx4j.wml.Tc) o).getEGBlockLevelElts();
		} else if (o instanceof org.docx4j.wml.Tr) {
			return ((org.docx4j.wml.Tr) o).getEGContentCellContent();
		} else if (o instanceof org.docx4j.wml.Tbl) {
			return ((org.docx4j.wml.Tbl) o).getEGContentRowContent();
		} else if (o instanceof org.docx4j.wml.SdtBlock) {
			return ((org.docx4j.wml.SdtBlock) o).getSdtContent()
					.getContent();
		} else if (o instanceof org.docx4j.wml.CTSdtContentRow) {
			return ((org.docx4j.wml.CTSdtContentRow) o)
					.getEGContentRowContent();
		} else if (o instanceof org.docx4j.wml.SdtContentBlock) {
			return ((org.docx4j.wml.SdtContentBlock) o)
					.getEGContentBlockContent();
		} else if (o instanceof org.docx4j.wml.CTSdtContentRun) {
			return ((org.docx4j.wml.CTSdtContentRun) o).getParagraphContent();
		} else if (o instanceof org.docx4j.wml.SdtRun) {
			return ((org.docx4j.wml.SdtRun) o).getSdtContent()
					.getContent();
		} else if (o instanceof org.docx4j.wml.CTSdtRow) {
			return ((org.docx4j.wml.CTSdtRow) o).getSdtContent()
					.getContent();
		} else if (o instanceof org.docx4j.wml.CTSdtCell) {
			return ((org.docx4j.wml.CTSdtCell) o).getSdtContent().getContent();
		} else if (o instanceof org.docx4j.wml.Body) {
			return ((org.docx4j.wml.Body) o).getEGBlockLevelElts();
		} else if (o instanceof org.docx4j.wml.P.Hyperlink) {
			return ((org.docx4j.wml.P.Hyperlink)o).getParagraphContent();
		} else if (o instanceof org.docx4j.dml.wordprocessingDrawing.Inline) {
			org.docx4j.dml.wordprocessingDrawing.Inline inline = (org.docx4j.dml.wordprocessingDrawing.Inline)o;
			if (inline.getGraphic()!=null) {
				log.info("found a:graphic");
				org.docx4j.dml.Graphic graphic = inline.getGraphic();
				if (graphic.getGraphicData()!=null) {
					org.docx4j.dml.GraphicData graphicData = graphic.getGraphicData();
					// Its not graphicData.getAny() we're typically interested in
					if (graphicData.getPic()!=null
						&& graphicData.getPic().getBlipFill()!=null
						&& graphicData.getPic().getBlipFill().getBlip()!=null) {
						log.info("found CTBlip");
						List<Object> artificialList = new ArrayList<Object>();
						artificialList.add(graphicData.getPic().getBlipFill().getBlip());
						return artificialList;
					} else {
						// Chart is in here
						return graphicData.getAny();						
					}
					
				}
			}
		} else if (o instanceof Pict) {
			return ((Pict)o).getAnyAndAny(); // (why didn't the reflection below find this?)
		} else if (o instanceof org.docx4j.vml.CTShape) {				
//			return ((org.docx4j.vml.CTShape)o).getAny();
			List<Object> artificialList = new ArrayList<Object>();
			for (JAXBElement<?> j : ((org.docx4j.vml.CTShape)o).getPathOrFormulasOrHandles() ) {
				artificialList.add(j);				
			}
			return artificialList;
		} else if (o instanceof org.docx4j.vml.CTTextbox) {				
//			return ((org.docx4j.vml.CTTextbox)o).getAny();
			return ((org.docx4j.vml.CTTextbox)o).getTxbxContent().getEGBlockLevelElts();
				// grandchildren

//		} else if (o instanceof org.docx4j.wml.CTTxbxContent) {				
//			return ((org.docx4j.wml.CTTxbxContent)o).getEGBlockLevelElts();
		} else if (o instanceof CTObject) {
			return ((CTObject)o).getAnyAndAny();
		}

		// OK, what is this? Use reflection ..
		// This should work for things including w:drawing
		log.debug(".. looking for method which returns list "  );
		try {
			Method[] methods = o.getClass().getDeclaredMethods();
			for (int i = 0; i<methods.length; i++) {
				Method m = methods[i];
				if (m.getReturnType().getName().equals("java.util.List") ) {					
					return (List<Object>)m.invoke(o);					
				}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.debug(".. no list member");
		return null;
	}
	

	public static void main(String[] args) throws Exception {

		String inputfilepath = System.getProperty("user.dir")
				+ "/sample-docs/sample-docx.xml";

		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage
				.load(new java.io.File(inputfilepath));
		MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();

		org.docx4j.wml.Document wmlDocumentEl = (org.docx4j.wml.Document) documentPart
				.getJaxbElement();
		Body body = wmlDocumentEl.getBody();

		new TraversalUtil(body,

		new Callback() {

			String indent = "";
			
			@Override
			public List<Object> apply(Object o) {
				
				String text = "";
				if (o instanceof org.docx4j.wml.Text)
					text = ((org.docx4j.wml.Text)o).getValue();
				
				System.out.println(indent + o.getClass().getName() + "  \"" + text + "\"");
				return null;
			}

			@Override
			public boolean shouldTraverse(Object o) {
				return true;
			}

			// Depth first
			@Override
			public void walkJAXBElements(Object parent) {

				indent += "    ";
				
				List children = getChildren(parent);
				if (children != null) {

					for (Object o : children) {

						// if its wrapped in javax.xml.bind.JAXBElement, get its
						// value; this is ok, provided the results of the Callback
						// won't be marshalled
						o = XmlUtils.unwrap(o);

						this.apply(o);

						if (this.shouldTraverse(o)) {
							walkJAXBElements(o);
						}

					}
				}
				
				indent = indent.substring(0, indent.length()-4);
			}

			@Override
			public List<Object> getChildren(Object o) {
				return TraversalUtil.getChildrenImpl(o);
			}

		}

		);

	}

	public static void replaceChildren(Object o, List<Object> newChildren) {
		
		// Available if you need something like this.  Would be used with
		// something like:
		
		/*
		 * 	  public void walkJAXBElements(Object parent){
		  // Breadth first
				
			  List<Object> newChildren = new ArrayList<Object>(); 
			  
				List children = getChildren(parent);
				if (children==null) {
					log.warn("no children: " + parent.getClass().getName() );
				} else {
					for (Object o : getChildren(parent) ) {

						// if its wrapped in javax.xml.bind.JAXBElement, get its value
						o = XmlUtils.unwrap(o);
						
						newChildren.addAll(this.apply(o));
					}
				}				
				// Replace list, so we'll traverse all the new sdts we've just created
				replaceChildren(parent, newChildren);
				
				
				for (Object o : getChildren(parent) ) {
					
					this.apply(o);
					
					if ( this.shouldTraverse(o) ) {
						walkJAXBElements(o);
					}
					
				}
				
			}

		 */

		log.debug("Clearing " + o.getClass().getName() );
		
		if (o instanceof org.docx4j.wml.SdtBlock) {

			((org.docx4j.wml.SdtBlock) o).getSdtContent()
					.getContent().clear();
			((org.docx4j.wml.SdtBlock) o).getSdtContent()
					.getContent().addAll(newChildren);

		} else if (o instanceof org.docx4j.wml.Body) {

			((org.docx4j.wml.Body) o).getEGBlockLevelElts().clear();
			((org.docx4j.wml.Body) o).getEGBlockLevelElts().addAll(newChildren);
			
		} else if (o instanceof org.docx4j.wml.P) {

			((org.docx4j.wml.P) o).getParagraphContent().clear();
			((org.docx4j.wml.P) o).getParagraphContent().addAll(newChildren);

		} else if (o instanceof org.docx4j.wml.R) {

			((org.docx4j.wml.R) o).getRunContent().clear();
			((org.docx4j.wml.R) o).getRunContent().addAll(newChildren);

		} else if (o instanceof org.docx4j.wml.CTSdtContentRow) {
			((org.docx4j.wml.CTSdtContentRow) o).getEGContentRowContent()
					.clear();
			((org.docx4j.wml.CTSdtContentRow) o).getEGContentRowContent()
					.addAll(newChildren);
		} else if (o instanceof org.docx4j.wml.SdtContentBlock) {
			((org.docx4j.wml.SdtContentBlock) o).getEGContentBlockContent()
					.clear();
			((org.docx4j.wml.SdtContentBlock) o).getEGContentBlockContent()
					.addAll(newChildren);
		} else if (o instanceof org.docx4j.wml.CTSdtContentRun) {
			((org.docx4j.wml.CTSdtContentRun) o).getParagraphContent().clear();
			((org.docx4j.wml.CTSdtContentRun) o).getParagraphContent().addAll(
					newChildren);

		} else if (o instanceof org.docx4j.wml.SdtRun) {

			((org.docx4j.wml.SdtRun) o).getSdtContent().getContent()
					.clear();
			((org.docx4j.wml.SdtRun) o).getSdtContent().getContent()
					.addAll(newChildren);

		} else if (o instanceof org.docx4j.wml.CTSdtRow) {

			((org.docx4j.wml.CTSdtRow) o).getSdtContent()
					.getContent().clear();
			((org.docx4j.wml.CTSdtRow) o).getSdtContent()
					.getContent().addAll(newChildren);

		} else if (o instanceof org.docx4j.wml.Tbl) {

			((org.docx4j.wml.Tbl) o).getEGContentRowContent().clear();
			((org.docx4j.wml.Tbl) o).getEGContentRowContent().addAll(
					newChildren);

		} else if (o instanceof org.docx4j.wml.Tr) {

			((org.docx4j.wml.Tr) o).getEGContentCellContent().clear();
			((org.docx4j.wml.Tr) o).getEGContentCellContent().addAll(
					newChildren);

		} else if (o instanceof org.docx4j.wml.Tc) {

			((org.docx4j.wml.Tc) o).getEGBlockLevelElts().clear();
			((org.docx4j.wml.Tc) o).getEGBlockLevelElts().addAll(newChildren);
			
		} else if (o instanceof org.docx4j.wml.CTTxbxContent) {
			
			((org.docx4j.wml.CTTxbxContent) o).getEGBlockLevelElts().clear();
			((org.docx4j.wml.CTTxbxContent) o).getEGBlockLevelElts().addAll(newChildren);			

		} else {
			
			log.warn("Don't know how to replaceChildren in " + o.getClass().getName() ); 

			if (o instanceof org.w3c.dom.Node) {
				log.warn(" IGNORED " + ((org.w3c.dom.Node) o).getNodeName());
			} else {
//				log.warn(" IGNORED " + o.getClass().getName());
			}

		}
	}
	// private void describeDrawing( org.docx4j.wml.Drawing d) {
	//			
	// log.info("In wml.Drawing" );
	//			
	// if ( d.getAnchorOrInline().get(0) instanceof Anchor ) {
	//				
	// log.info(" ENCOUNTERED w:drawing/wp:anchor " );
	// // That's all for now...
	//				
	// } else if ( d.getAnchorOrInline().get(0) instanceof Inline ) {
	//				
	// // Extract
	// w:drawing/wp:inline/a:graphic/a:graphicData/pic:pic/pic:blipFill/a:blip/@r:embed
	//				
	// Inline inline = (Inline )d.getAnchorOrInline().get(0);
	//				
	// Pic pic = inline.getGraphic().getGraphicData().getPic();
	//					
	// if (pic!=null) {
	// log.info("image relationship: " + pic.getBlipFill().getBlip().getEmbed()
	// );
	// }
	//				
	//				
	// } else {
	//				
	// log.info(" Didn't get Inline :(  How to handle " +
	// d.getAnchorOrInline().get(0).getClass().getName() );
	// }
	//			
	// }

}
