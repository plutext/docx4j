/*
 *  Copyright 2011-2012, Plutext Pty Ltd.
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
package org.pptx4j.convert.in.xhtml;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.docx4j.XmlUtils;
import org.docx4j.dml.CTGraphicalObjectFrameLocking;
import org.docx4j.dml.CTHyperlink;
import org.docx4j.dml.CTNonVisualDrawingProps;
import org.docx4j.dml.CTNonVisualGraphicFrameProperties;
import org.docx4j.dml.CTPoint2D;
import org.docx4j.dml.CTPositiveSize2D;
import org.docx4j.dml.CTRegularTextRun;
import org.docx4j.dml.CTTable;
import org.docx4j.dml.CTTableCell;
import org.docx4j.dml.CTTableCol;
import org.docx4j.dml.CTTableGrid;
import org.docx4j.dml.CTTableRow;
import org.docx4j.dml.CTTextCharacterProperties;
import org.docx4j.dml.CTTextLineBreak;
import org.docx4j.dml.CTTextParagraph;
import org.docx4j.dml.CTTransform2D;
import org.docx4j.dml.Graphic;
import org.docx4j.dml.GraphicData;
import org.docx4j.model.properties.Property;
import org.docx4j.model.properties.PropertyFactory;
import org.docx4j.model.properties.run.AbstractRunProperty;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.PresentationML.MainPresentationPart;
import org.docx4j.openpackaging.parts.PresentationML.SlideLayoutPart;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.org.xhtmlrenderer.css.constants.CSSName;
import org.docx4j.org.xhtmlrenderer.css.constants.IdentValue;
import org.docx4j.org.xhtmlrenderer.css.style.CalculatedStyle;
import org.docx4j.org.xhtmlrenderer.css.style.DerivedValue;
import org.docx4j.org.xhtmlrenderer.css.style.FSDerivedValue;
import org.docx4j.org.xhtmlrenderer.newtable.TableBox;
import org.docx4j.org.xhtmlrenderer.newtable.TableCellBox;
import org.docx4j.org.xhtmlrenderer.newtable.TableRowBox;
import org.docx4j.org.xhtmlrenderer.newtable.TableSectionBox;
import org.docx4j.org.xhtmlrenderer.docx.DocxRenderer;
import org.docx4j.org.xhtmlrenderer.render.AnonymousBlockBox;
import org.docx4j.org.xhtmlrenderer.render.BlockBox;
import org.docx4j.org.xhtmlrenderer.render.Box;
import org.docx4j.org.xhtmlrenderer.render.InlineBox;
import org.docx4j.org.xhtmlrenderer.resource.XMLResource;
import org.docx4j.relationships.Relationship;
import org.pptx4j.pml.CTGraphicalObjectFrame;
import org.pptx4j.pml.CTGraphicalObjectFrameNonVisual;
import org.pptx4j.pml.Shape;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSValue;
import org.xml.sax.InputSource;

public class XHTMLImporter {
    
    private static final org.docx4j.dml.ObjectFactory DML_OBJECT_FACTORY = new org.docx4j.dml.ObjectFactory();
    private static final org.pptx4j.pml.ObjectFactory PML_OBJECT_FACTORY = org.pptx4j.jaxb.Context.getpmlObjectFactory();
    private static final org.docx4j.relationships.ObjectFactory RELATIONSHIPS_FACTORY = new org.docx4j.relationships.ObjectFactory();

    private static final String PARAGRAPH_SHAPE =            
                    "<p:sp xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" xmlns:p=\"http://schemas.openxmlformats.org/presentationml/2006/main\">" +
                    "  <p:nvSpPr>" + 
                    "    <p:cNvPr id=\"4\" name=\"Title 3\" />" +
                    "    <p:cNvSpPr>" +
                    "      <a:spLocks noGrp=\"1\" />" +
                    "    </p:cNvSpPr>" +
                    "    <p:nvPr>" +
                    "      <p:ph type=\"title\" />" +
                    "    </p:nvPr>" +
                    "  </p:nvSpPr>" +
                    "  <p:spPr />" +
                    "  <p:txBody>" +
                    "    <a:bodyPr />" +
                    "    <a:lstStyle />" +
                    "  </p:txBody>" +
                    "</p:sp>";

    private static final Logger LOG = Logger.getLogger(XHTMLImporter.class);
        
    private PresentationMLPackage presentationMLPackage;
    private DocxRenderer renderer;
    
    private RelationshipsPart rp;
    private MainPresentationPart pp;
    private SlideLayoutPart layoutPart;
    
    private XHTMLImporter(PresentationMLPackage wordMLPackage, SlidePart slidePart, DocxRenderer renderer) throws Exception{
        this.presentationMLPackage= wordMLPackage;
        this.renderer = renderer;
        
        pp = (MainPresentationPart)presentationMLPackage.getParts().getParts().get(new PartName("/ppt/presentation.xml"));     
        layoutPart = (SlideLayoutPart)presentationMLPackage.getParts().getParts().get(new PartName("/ppt/slideLayouts/slideLayout1.xml"));
        rp = slidePart.getRelationshipsPart();
    }

    /**
     * 
     * Convert the well formed XHTML contained in the string to a list of WML objects.
     * 
     * @param content
     * @param baseUrl
     * @param presentationMLPackage
     * @param slidePart 
     * @return
     */
    public static List<Object> convertSingleSlide(String content,  String baseUrl, PresentationMLPackage presentationMLPackage, SlidePart slidePart) throws Exception {
        DocxRenderer pptxRenderer = createRenderer(content, baseUrl);
        XHTMLImporter importer = new XHTMLImporter(presentationMLPackage, slidePart, pptxRenderer);
        return importer.traverse();
    }

    private static DocxRenderer createRenderer(String content, String baseUrl) {
        DocxRenderer pptxRenderer = new DocxRenderer();
        InputSource is = new InputSource(new BufferedReader(new StringReader(content)));
        Document dom = XMLResource.load(is).getDocument();
        
        pptxRenderer.setDocument(dom, baseUrl);
        pptxRenderer.layout();
        pptxRenderer.getRootBox().getLayer().getPages();
        
        return pptxRenderer;
    }

    private List<Object> traverse() throws Docx4JException, FileNotFoundException, JAXBException {
        return traverseChildren(renderer.getRootBox(), new TraversalSettings());
    }
    
    private List<Object> traverseChildren(BlockBox blockBox, TraversalSettings settings) throws Docx4JException, JAXBException {
        List<Object> converted = new ArrayList<Object>();
        for(Object o : blockBox.getChildren()) {
            converted.addAll(traversalResultToList(traverseChild((Box)o, settings)));
        }
        if (blockBox.getInlineContent() != null) {
            for (Object o : blockBox.getInlineContent()) {
                converted.addAll(traversalResultToList(tranverseInlineContent(o, settings)));
            }
        }
        return converted;
    }

    @SuppressWarnings("unchecked")
    private List<Object> traversalResultToList(Object switchNode) {
        if(switchNode == null) {
            return new ArrayList<Object>();
        } else if(switchNode instanceof List) {
            return (List<Object>) switchNode;
        } else {
            List<Object> list = new ArrayList<Object>();
            list.add(switchNode);
            return list;
        }
    }
    
    private Object traverseChild(Box box, TraversalSettings settings) throws Docx4JException, JAXBException {
        LOG.info(box.getClass().getName());
        if(box instanceof TableBox) {
            return processTable((TableBox) box, settings);
        } else if (box instanceof TableSectionBox) {
            // no support for table section in pptx, skipping to children
            return traverseChildren((TableSectionBox)box, settings);
        } else if (box instanceof TableRowBox) {
            return traverseTableRow((TableRowBox)box, settings);
        } else if (box instanceof TableCellBox) {
            return traverseTableCell((TableCellBox)box, settings);
        } else if (box instanceof AnonymousBlockBox) {
            return processAnonymousBlockBox((AnonymousBlockBox)box, settings);
        } else if (box instanceof BlockBox) {
            return traverseBlockBox((BlockBox)box, settings);
        } else {
            return new ArrayList<Object>();
        }
    }

    private Object processTable(TableBox tableBox, TraversalSettings settings) throws JAXBException, Docx4JException {
        CTTable ctTable = DML_OBJECT_FACTORY.createCTTable();
        ctTable.setTblGrid(createCtTableGrid(tableBox));

        CTGraphicalObjectFrame graphicFrame = createTableGraphicFrame();
        graphicFrame.getGraphic().getGraphicData().getAny().add(DML_OBJECT_FACTORY.createTbl(ctTable));

        //traverse
        List<Object> children = traverseChildren(tableBox, settings);
        List<CTTableRow> tableRows = filterList(children, CTTableRow.class);
        ctTable.getTr().addAll(tableRows);
        if(tableRows.size() != children.size()) {
            LOG.warn("Some table data lost");
        }

        return graphicFrame;
    }
    
    private CTTableGrid createCtTableGrid(TableBox tableBox) {
        CTTableGrid ctTableGrid = DML_OBJECT_FACTORY.createCTTableGrid();
        int[] colPos = tableBox.getColumnPos();
        for (int col=1; col<=tableBox.numEffCols(); col++) {
            CTTableCol gridCol = DML_OBJECT_FACTORY.createCTTableCol();
            ctTableGrid.getGridCol().add(gridCol);
            gridCol.setW((colPos[col] - colPos[col - 1]) * 10000);
        }
        return ctTableGrid;
    }

    private CTGraphicalObjectFrame createTableGraphicFrame() {
        CTGraphicalObjectFrame graphicFrame = PML_OBJECT_FACTORY.createCTGraphicalObjectFrame();
        CTGraphicalObjectFrameNonVisual nvGraphicFramePr = PML_OBJECT_FACTORY.createCTGraphicalObjectFrameNonVisual();
        CTNonVisualDrawingProps cNvPr = DML_OBJECT_FACTORY.createCTNonVisualDrawingProps();
        CTNonVisualGraphicFrameProperties cNvGraphicFramePr = DML_OBJECT_FACTORY.createCTNonVisualGraphicFrameProperties();
        CTGraphicalObjectFrameLocking graphicFrameLocks = DML_OBJECT_FACTORY.createCTGraphicalObjectFrameLocking();
        CTTransform2D xfrm = DML_OBJECT_FACTORY.createCTTransform2D();
        Graphic graphic = DML_OBJECT_FACTORY.createGraphic();
        GraphicData graphicData = DML_OBJECT_FACTORY.createGraphicData();

        // Build the parent-child relationship of this slides.xml
        graphicFrame.setNvGraphicFramePr(nvGraphicFramePr);
        nvGraphicFramePr.setCNvPr(cNvPr);
        cNvPr.setName("1");
        nvGraphicFramePr.setCNvGraphicFramePr(cNvGraphicFramePr);
        cNvGraphicFramePr.setGraphicFrameLocks(graphicFrameLocks);
        graphicFrameLocks.setNoGrp(true);
        nvGraphicFramePr.setNvPr(PML_OBJECT_FACTORY.createNvPr());
        graphicFrame.setXfrm(xfrm);

        CTPositiveSize2D ext = DML_OBJECT_FACTORY.createCTPositiveSize2D();
        ext.setCx(6096000);
        ext.setCy(741680);

        xfrm.setExt(ext);

        CTPoint2D off = DML_OBJECT_FACTORY.createCTPoint2D();
        xfrm.setOff(off);
        off.setX(1524000);
        off.setY(1397000);

        graphicFrame.setGraphic(graphic);
        graphic.setGraphicData(graphicData);
        graphicData.setUri("http://schemas.openxmlformats.org/drawingml/2006/table");

        return graphicFrame;
    }

    private CTTableRow traverseTableRow(TableRowBox tableRowBox, TraversalSettings settings) throws Docx4JException, JAXBException {
        CTTableRow ctTableRow = DML_OBJECT_FACTORY.createCTTableRow();
        ctTableRow.setH(tableRowBox.getHeight() * 10000);

        //traverse
        List<Object> children = traverseChildren(tableRowBox, settings);
        List<CTTableCell> tableCells = filterList(children, CTTableCell.class);
        ctTableRow.getTc().addAll(tableCells);
        if(tableCells.size() != children.size()) {
            LOG.warn("Some table row lost");
        }

        return ctTableRow;
    }

    private CTTableCell traverseTableCell(TableCellBox tableCellBox, TraversalSettings settings) throws JAXBException, Docx4JException {
        // traverse
        settings.setInTableCell(true);
        List<Object> children = traverseChildren(tableCellBox, settings);
        settings.setInTableCell(false);
        List<CTTextParagraph> cellContent = filterList(children, CTTextParagraph.class);
        if(cellContent.size() != children.size()) {
            LOG.warn("Some table cell content lost");
        }

        return createTableCell(cellContent);
    }
    
    @SuppressWarnings("unchecked")
    private <T> List<T> filterList(List<?> input, Class<T> clazz){
        ArrayList<T> result = new ArrayList<T>();
        for(Object o : input) {
            if(clazz.isInstance(o)) {
                result.add((T)o);
            }
        }
        return result;
    }

    private CTTableCell createTableCell(Collection<CTTextParagraph> children) throws JAXBException {
        String contents =
         "<a:tc xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\">" +
         "  <a:txBody>" +
         "    <a:bodyPr/>" +
         "    <a:lstStyle/>" +
         "  </a:txBody>" +
         "</a:tc>";
        CTTableCell ctTableCell = (CTTableCell)XmlUtils.unmarshalString(contents, org.docx4j.jaxb.Context.jc, CTTableCell.class);
        ctTableCell.getTxBody().getP().addAll(children);
        return ctTableCell;
    }

    private Object processAnonymousBlockBox(AnonymousBlockBox anonymousBlockBox, TraversalSettings settings) throws Docx4JException, JAXBException {
        List<Object> children = traverseChildren(anonymousBlockBox, settings);
        if(settings.isInTableCell()) {
            return children;
        } else {
            return createParagraphShape(createParagraph(children));
        }
    }

    private Object traverseBlockBox(BlockBox blockBox, TraversalSettings settings) throws Docx4JException, JAXBException {
        Element e = blockBox.getElement();
        if (blockBox.getElement() == null) {
            return new ArrayList<Object>();
        } else if(isHtmlOrBody(e)) {
            return traverseChildren(blockBox, settings);
        } else if (isParagraph(e)) {
            return processParagraph(blockBox, settings);
        } else {
            return new ArrayList<Object>();
        }
    }
    
    private Object tranverseInlineContent(Object o, TraversalSettings settings) {
        if (o instanceof InlineBox) {
            return traverseInlineBoxContent((InlineBox)o, settings);
        } else {
            LOG.debug("What to do with " + o.getClass().getName());
            return null;
        }
    }

    private Object processParagraph(BlockBox blockBox, TraversalSettings settings) throws JAXBException, Docx4JException {
        // traverse
        TraversalSettings localSettings = settings.clone();
        localSettings.setInTableCell(false);
        List<Object> children = traverseChildren(blockBox, localSettings);
        settings.setCssMap(null);

        CTTextParagraph paragraph = createParagraph(children);
        if(settings.isInTableCell()) {
            return paragraph;
        } else {
            return createParagraphShape(paragraph);
        }
    }

    private CTTextParagraph createParagraph(List<Object> children) {
        CTTextParagraph paragraph = DML_OBJECT_FACTORY.createCTTextParagraph();
        paragraph.getEGTextRun().addAll(children);
        return paragraph;
    }

    private Shape createParagraphShape(CTTextParagraph paragraph) throws JAXBException {
        Shape paragraphShape = (Shape) XmlUtils.unmarshalString(PARAGRAPH_SHAPE, org.pptx4j.jaxb.Context.jcPML, Shape.class);
        paragraphShape.getTxBody().getP().add(paragraph);
        return paragraphShape;
    }

    private Object traverseInlineBoxContent(InlineBox inlineBox, TraversalSettings settings) {
        Object content = processInlineBoxContent(inlineBox, settings);
        if(settings.isInTableCell()) {
            ArrayList<Object> contentList = new ArrayList<Object>();
            contentList.add(content);
            return createParagraph(contentList);
        } else {
            return content;
        }
    }

    private Object processInlineBoxContent(InlineBox inlineBox, TraversalSettings settings) {
        if(inlineBox.getElement() != null) {
            if(isHyperlink(inlineBox)) {
                return processHyperlink(inlineBox, settings);
            }
        }
        
        if (inlineBox.getTextNode() == null) {
            return processEmptyTextNode(inlineBox, settings);
        } else  {
            return processRegularTextNode(inlineBox, settings);
        }
    }
    
    private CTRegularTextRun processHyperlink(InlineBox inlineBox, TraversalSettings settings) {
        String hrefAttr = inlineBox.getElement().getAttribute("href");
        
        if(inlineBox.isStartsHere()) {
            settings.setHyperlink(hrefAttr);
        }
        
        CTRegularTextRun hyperlink = processRegularTextNode(inlineBox, settings);
        
        if(inlineBox.isEndsHere()) {
            settings.setHyperlink(null);
        }
        
        return hyperlink;
    }
     
    private CTTextLineBreak processEmptyTextNode(InlineBox inlineBox, TraversalSettings settings) {
        if (isLineBreak(inlineBox)) {
            return DML_OBJECT_FACTORY.createCTTextLineBreak();
        } else {
            LOG.debug("InlineBox has no text, so skipping");
            // TODO .. a span in a span? need to traverse?
            return null;
        }
    }

    private CTRegularTextRun processRegularTextNode(InlineBox inlineBox, TraversalSettings settings) {
        settings.setCssMap(getCascadedProperties(inlineBox.getStyle()));
        CTRegularTextRun run = DML_OBJECT_FACTORY.createCTRegularTextRun();
        run.setT(inlineBox.getTextNode().getTextContent());
        run.setRPr(createRunProperties(settings));
        return run;
    }

    private CTTextCharacterProperties createRunProperties(TraversalSettings settings) {
        CTTextCharacterProperties rPr = DML_OBJECT_FACTORY.createCTTextCharacterProperties();
        addStylingProperties(rPr, settings.getCssMap());
        if(settings.isHyperlinkTraversal()) {
            rPr.setHlinkClick(createHyperlink(settings.getHyperlink()));
        }
        return rPr;
    }
    
    private void addStylingProperties(CTTextCharacterProperties rPr, Map<String, CSSValue> cssMap) {
        for (String cssName : cssMap.keySet()) {
            Property p = PropertyFactory.createPropertyFromCssName(cssName, cssMap.get(cssName));
            if (p != null) {
                if (p instanceof AbstractRunProperty) {             
                    ((AbstractRunProperty)p).set(rPr);
                } else {
                    LOG.debug("Unknown property " + p.getClass().getName());
                }
            }
        }
    }

    private CTHyperlink createHyperlink(String url) {
        Relationship relationship = addHyperlinkRelationship(url);
        CTHyperlink hyperlink = DML_OBJECT_FACTORY.createCTHyperlink();
        hyperlink.setId(relationship.getId());
        return hyperlink;
    }
    
    private Relationship addHyperlinkRelationship(String url) {
        Relationship rel = RELATIONSHIPS_FACTORY.createRelationship();
        rel.setType(Namespaces.HYPERLINK);
        rel.setTarget(url);
        rel.setTargetMode("External");  
        
        // addRelationship sets the rel's @Id
        rp.addRelationship(rel);
        
        return rel;
    }
    
    private Map<String, CSSValue> getCascadedProperties(CalculatedStyle cs) {
        
        Map<String, CSSValue> cssMap = new HashMap<String, CSSValue>();
        
        FSDerivedValue[] derivedValues = cs.getDerivedValues();
        for (int i = 0; i < derivedValues.length; i++) {
                        
            CSSName name = CSSName.getByID(i);
            
            if (name.toString().startsWith("-fs")) continue;
                        
            FSDerivedValue val = cs.valueByName(name); // walks parents as necessary to get the value
            
            if (val != null && val instanceof DerivedValue) {    
                
                cssMap.put(name.toString(), ((DerivedValue)val).getCSSPrimitiveValue() );
                
            } else if (val != null && val instanceof IdentValue) {
                
                cssMap.put(name.toString(), ((IdentValue)val).getCSSPrimitiveValue() );

            } else  if (val!=null ) {
                
                LOG.debug("Skipping " +  name.toString() + " .. " + val.getClass().getName() );
            } else {
                LOG.debug("Skipping " +  name.toString() + " .. (null value)" );                
            }
        }
        
        return cssMap;
        
    }
    
    private boolean isLineBreak(InlineBox inlineBox) {
        return inlineBox.getElement().getNodeName().equals("br");
    }

    private boolean isHyperlink(InlineBox inlineBox) {
        return inlineBox.getElement().getNodeName().equals("a");
    }
    
    private boolean isHtmlOrBody(Element e) {
        return e.getNodeName().equals("html") || e.getNodeName().equals("body");
    }

    private boolean isParagraph(Element e) {
        return e.getNodeName().equals("p");
    }

}
