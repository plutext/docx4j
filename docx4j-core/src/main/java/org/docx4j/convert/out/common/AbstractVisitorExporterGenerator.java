/*
   Licensed to Plutext Pty Ltd under one or more contributor license agreements.  
   
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
package org.docx4j.convert.out.common;

import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.common.writer.AbstractBookmarkStartWriter;
import org.docx4j.convert.out.common.writer.AbstractFldSimpleWriter;
import org.docx4j.convert.out.common.writer.AbstractHyperlinkWriter;
import org.docx4j.convert.out.common.writer.AbstractPictWriter;
import org.docx4j.convert.out.common.writer.AbstractSymbolWriter;
import org.docx4j.convert.out.common.writer.AbstractTableWriter;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.Br;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.LinkedList;
import java.util.List;

/**
 * The …ExporterGenerator is the visitor, that gets used in those cases where a document is done 
 * as a NonXSLT.  (docx4j supports convert.out via both xslt and non-xslt based approaches)
 * 
 * @since 3.0
 */
public abstract class AbstractVisitorExporterGenerator<CC extends AbstractWmlConversionContext> extends TraversalUtil.CallbackImpl {
	
	private static Logger log = LoggerFactory.getLogger(AbstractVisitorExporterGenerator.class);
	
	protected static final String TAB_DUMMY = "\u00A0\u00A0\u00A0";
	protected static final int NODE_BLOCK = 1;
	protected static final int NODE_INLINE = 2;
	
	protected static final int IMAGE_E10 = 1;
	protected static final int IMAGE_E20 = 2;
	
	
	protected CC conversionContext = null;
	
	protected Document document = null;
	
	protected Node parentNode = null;
	
	protected Element currentP = null; 
	protected Element currentSpan = null;
	
	protected LinkedList<Element> tr = new LinkedList<Element>();
	protected LinkedList<Element> tc = new LinkedList<Element>();

	//current paragraph style to inherit styles in rPr
	protected PPr pPr = null;
	protected RPr rPr = null;
	
	// E20 image
	protected Object anchorOrInline;

	
	protected AbstractVisitorExporterGenerator(CC conversionContext, Document document, Node parentNode) {
		this.conversionContext = conversionContext;
		this.document = document;
		this.parentNode = parentNode;
	}

	@Override
	public boolean shouldTraverse(Object o) {
		if ((o instanceof org.docx4j.wml.Tbl) ||
			(o instanceof org.docx4j.wml.P.Hyperlink) ||
			(o instanceof org.docx4j.wml.CTSimpleField) ||
			(o instanceof org.docx4j.vml.CTTextbox) ||
			(o instanceof org.docx4j.wml.FldChar)) {
			return false;
		} else {
			return true;
		}
	}
	
	protected Node getCurrentParent() {
		//this might be executed within a new level of traversal util
		//currentSpan or currentP might be null
		return (currentSpan != null ? 
				   currentSpan :
				   (currentP != null ? currentP : parentNode)
			   );
	}

	
	protected void convertToNode(CC conversionContext, 
							   Object unmarshalledNode, String modelId, 
							   Document document, Node parentNode) throws DOMException {

		// To use our existing model, first we need childResults.
		// We get these using a new Generator object.
		log.debug(modelId);
		
		DocumentFragment childResults = null;
		if (unmarshalledNode instanceof ContentAccessor) {
			childResults = document.createDocumentFragment();
			AbstractVisitorExporterGenerator<CC> generator = getFactory().createInstance(conversionContext, document, childResults);
			new TraversalUtil(((ContentAccessor)unmarshalledNode).getContent(), generator);
			
		} else if (unmarshalledNode instanceof org.docx4j.wml.Pict) {
			// if it contains a textbox..
			
			// repeating this...
			org.docx4j.vml.CTTextbox textBox = getTextBox((org.docx4j.wml.Pict)unmarshalledNode);
			
			if (textBox!=null) {
				
				childResults = document.createDocumentFragment();
				AbstractVisitorExporterGenerator<CC> generator = getFactory().createInstance(conversionContext, document, childResults);
				new TraversalUtil(textBox.getTxbxContent().getContent(), generator);
			}
			
		}
		
		Node resultNode = 
			 conversionContext.getWriterRegistry().toNode(
					 conversionContext, 
					 unmarshalledNode, 
					 modelId, 
					 childResults, 
					 document);
		

    	
 
		
		if (resultNode != null) {
			log.debug("Appending " + XmlUtils.w3CDomNodeToString(resultNode));
			parentNode.appendChild(resultNode);
		}
		
	}
	
	
	protected void rtlAwareAppendChildToCurrentP(Element child) {
		parentNode.appendChild(child);
	}
	
	
	@Override
	public void walkJAXBElements(Object o) {
		
		Node existingParentNode = parentNode;
		
		if (o instanceof org.docx4j.wml.Tr) {
			
			tr.push(document.createElementNS(Namespaces.NS_WORD12, "tr"));
			//parentNode is in this case the DocumentFragment, that get's passed 
			//to the TableModel/TableModelWriter
			parentNode.appendChild(tr.peek());
			
		} else if (o instanceof org.docx4j.wml.Tc) {
			
			tc.push(document.createElementNS(Namespaces.NS_WORD12, "tc"));
			(tr.peek()).appendChild(tc.peek());
			// now the html p content will go temporarily go in w:tc,
			// which is what we need for our existing table model.
			
			parentNode = tc.peek();
			
		}		
		
		super.walkJAXBElements(o);
		
		if (o instanceof org.docx4j.wml.Tr) {
			
			tr.pop();
			
		} else if (o instanceof org.docx4j.wml.Tc) {
			
			tc.pop();
			
			parentNode = existingParentNode; // restore
		}		
		
		
	}
	
	
	@Override
	public List<Object> apply(Object o) {
		
		if (o instanceof P) {
			
			currentP = createNode(document, NODE_BLOCK);
			
			// "pre" is no good, since wrapping does not happen
			// (so paragraph continues right over edge of page)
			//currentP.setAttribute( "white-space", "pre");
			
			currentSpan = null;
			if (tc.peek()!=null) {
				tc.peek().appendChild( currentP  );
			} else {
				parentNode.appendChild( currentP  );					
			}
			
			pPr = ((P)o).getPPr();
			currentP = handlePPr(conversionContext, pPr, false, currentP);
			
		} else if (o instanceof org.docx4j.wml.R) {
			
			if (!conversionContext.isInComplexFieldDefinition()) {
				// Convert run to span
				Element spanEl = createNode(document, NODE_INLINE);
				currentSpan = spanEl;
				
				rPr = ((R)o).getRPr();
				if ( rPr!=null ) {
					handleRPr(conversionContext, pPr, rPr, currentSpan);
				}

				if (currentP==null) {
					// Hyperlink special case
					parentNode.appendChild(spanEl);
				} else {
					rtlAwareAppendChildToCurrentP(spanEl);
				}

				
				// To merge nested span (which we could do if there is a single child span),
				// TraversalUtil Callback would need an after walk children
			}
			
		} else if (o instanceof org.docx4j.wml.FldChar) {
			conversionContext.updateComplexFieldDefinition(((org.docx4j.wml.FldChar)o).getFldCharType());

		} else if (o instanceof org.docx4j.wml.Text) {
			
			if (!conversionContext.isInComplexFieldDefinition()) {
				
				if (currentSpan==null) {
					// eg after <br/>
					log.error("null currentSpan! " + ((Text)o).getValue() );
					Element spanEl = createNode(document, NODE_INLINE);
					if (currentP==null) {
						// Hyperlink special case
						parentNode.appendChild(spanEl);
					} else {
						currentP.appendChild( spanEl  );
					}
					currentSpan = spanEl;
				}

				log.debug(((Text)o).getValue());
				
				DocumentFragment df = (DocumentFragment) conversionContext.getRunFontSelector().fontSelector(pPr, rPr, ((Text)o));
				XmlUtils.treeCopy(df, currentSpan);
				// TODO would be more efficient without the treeCopy
				// but fontSelector would need to be refactored a bit
				
			}

		} else if (o instanceof org.docx4j.wml.R.Tab) {
			convertTabToNode(conversionContext, document);
			
		} else if (o instanceof org.docx4j.wml.CTSimpleField) {

			convertToNode(conversionContext, 
						  o, AbstractFldSimpleWriter.WRITER_ID,
						  document, getCurrentParent());
			
		} else if (o instanceof org.docx4j.wml.P.Hyperlink) {
			
			convertToNode(conversionContext, 
						  o, AbstractHyperlinkWriter.WRITER_ID,
						  document, getCurrentParent());
			
		} else if (o instanceof org.docx4j.wml.CTBookmark) {

			convertToNode(conversionContext, 
						  o, AbstractBookmarkStartWriter.WRITER_ID,
						  document, getCurrentParent());
			

		} else if (o instanceof org.docx4j.wml.Tbl) {

			convertToNode(conversionContext, 
						  o, AbstractTableWriter.WRITER_ID,
						  document, (currentP != null ? currentP : parentNode));
			
			currentP=null;
			currentSpan=null;
			
		} else if (o instanceof org.docx4j.wml.Tr) {
			
			// done in walkJAXBElements
			
//			tr = document.createElementNS(Namespaces.NS_WORD12, "tr");
//			//parentNode is in this case the DocumentFragment, that get's passed 
//			//to the TableModel/TableModelWriter
//			parentNode.appendChild(tr);
			
		} else if (o instanceof org.docx4j.wml.Tc) {
			
			// done in walkJAXBElements

//			tc = document.createElementNS(Namespaces.NS_WORD12, "tc");
//			tr.appendChild(tc);
//			// now the html p content will go temporarily go in w:tc,
//			// which is what we need for our existing table model.
			
//			System.out.println("#wrapped in w:tc OK");
			
		} else if (o instanceof org.docx4j.dml.wordprocessingDrawing.Inline
				|| o instanceof org.docx4j.dml.wordprocessingDrawing.Anchor) {
			
			anchorOrInline = o;  // keep this until we handle CTBlip
			
		} else if (o instanceof org.docx4j.dml.CTBlip) {
            /*<w:drawing>
                <wp:inline distT="0" distB="0" distL="0" distR="0">
                  <a:graphic xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main">
                    <a:graphicData uri="http://schemas.openxmlformats.org/drawingml/2006/picture">
                      <pic:pic xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture">
                        <pic:blipFill>
                          <a:blip r:embed="rId10" cstate="print"/> */
			
			DocumentFragment foreignFragment = createImage(IMAGE_E20, conversionContext, anchorOrInline);
			anchorOrInline = null;
			
			currentP.appendChild( document.importNode(foreignFragment, true) );
			
		} else if (o instanceof org.docx4j.wml.Pict) {
	      /*<w:pict>
	          <v:shape id="_x0000_i1025" type="#_x0000_t75" style="width:428.25pt;height:321pt">
	            <v:imagedata r:id="rId4" o:title=""/>
	          </v:shape> */
			
			org.docx4j.vml.CTTextbox textBox = getTextBox((org.docx4j.wml.Pict)o);
			
			if (textBox==null) {
				// Assume it contains an image!
				DocumentFragment foreignFragment = createImage(IMAGE_E10, conversionContext, o);
				currentP.appendChild( document.importNode(foreignFragment, true) );
				
			} else {
				
				convertToNode(conversionContext, 
				  o, AbstractPictWriter.WRITER_ID,
				  document, getCurrentParent());
				
			}
			

			
		} else if (o instanceof Br) {
			
			handleBr((Br)o);
			
		} else if (o instanceof org.docx4j.wml.R.Sym) {

			convertToNode(conversionContext, 
						  o, AbstractSymbolWriter.WRITER_ID,
						  document, getCurrentParent());
			
		} else if ((o instanceof org.docx4j.wml.ProofErr) ||
				   (o instanceof org.docx4j.wml.R.LastRenderedPageBreak) ||
				   (o instanceof org.docx4j.wml.CTMarkupRange)) {
		//Ignore theese types, they don't need to be outputed/handled
		//CTMarkupRange is the w:bookmarkEnd
			
		} else {
			log.warn("Need to handle " + o.getClass().getName() );
            if(log.isDebugEnabled()) {
                log.debug(XmlUtils.marshaltoString(o));
            }
		}
		
		return null;
	}
	
	abstract protected void handleBr(Br o);
	
	protected int getPos(List list, Object wanted) {
		
		int index = 0;
		for(Object o : list) {
			
			if (o==wanted) {
				return index;
			}
			index++;
		}
		return -1;
	}
	
	protected void convertTabToNode(CC conversionContext, Document document) throws DOMException {
		
		if (!conversionContext.isInComplexFieldDefinition()) {
			getCurrentParent().appendChild(document.createTextNode(TAB_DUMMY));
		}
	}
	
	
	private org.docx4j.vml.CTTextbox getTextBox(org.docx4j.wml.Pict pict) {

		org.docx4j.vml.CTShape shape = null;
		for (Object o2 : pict.getAnyAndAny() ) {
			
			o2 = XmlUtils.unwrap(o2);
//			System.out.println(o.getClass().getName());
			if (o2 instanceof org.docx4j.vml.CTShape) {
				shape = (org.docx4j.vml.CTShape)o2;
				break;
			}
		}
		if (shape==null) {
			log.warn("no shape in pict " );
			return null;
		} else {

			org.docx4j.vml.CTTextbox textBox = null;
			org.docx4j.vml.wordprocessingDrawing.CTWrap w10Wrap = null;  
			for (Object o2 : shape.getPathOrFormulasOrHandles() ) {
				
				o2 = XmlUtils.unwrap(o2);
				
				if (o2 instanceof org.docx4j.vml.CTTextbox) {
					textBox = (org.docx4j.vml.CTTextbox)o2;
				}
				if (o2 instanceof org.docx4j.vml.wordprocessingDrawing.CTWrap) {
					w10Wrap = (org.docx4j.vml.wordprocessingDrawing.CTWrap)o2;
				}
			}
			
			return textBox;
			
		}
		
	}

    protected abstract Element handlePPr(CC conversionContext, PPr pPrDirect, boolean sdt, Element currentParent);

    
    /**
     * On a block representing a run, we just put run properties
     * from this rPr node. The paragraph style rPr's have been
     * taken care of on the fo block which represents the paragraph.
     * @return
     */
    protected abstract void handleRPr(CC conversionContext, PPr pPrDirect, RPr rPrDirect, Element currentParent );
    
	protected abstract AbstractVisitorExporterDelegate.AbstractVisitorExporterGeneratorFactory<CC> getFactory();
	
	protected abstract DocumentFragment createImage(int imgType, CC conversionContext, Object anchorOrInline);
	
	protected abstract Element createNode(Document doc, int nodeType);
	
	
	
	protected Logger getLog() {
		return log;
	}
}
