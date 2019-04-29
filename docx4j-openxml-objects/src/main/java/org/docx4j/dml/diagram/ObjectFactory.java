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


package org.docx4j.dml.diagram;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import org.docx4j.dml.CTTextBody;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.dml.diagram package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _T_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/diagram", "t");
    private final static QName _DataModel_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/diagram", "dataModel");
    private final static QName _LayoutDef_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/diagram", "layoutDef");
    private final static QName _LayoutDefHdr_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/diagram", "layoutDefHdr");
    private final static QName _LayoutDefHdrLst_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/diagram", "layoutDefHdrLst");
    private final static QName _RelIds_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/diagram", "relIds");
    private final static QName _ColorsDef_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/diagram", "colorsDef");
    private final static QName _ColorsDefHdr_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/diagram", "colorsDefHdr");
    private final static QName _ColorsDefHdrLst_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/diagram", "colorsDefHdrLst");
    private final static QName _StyleDef_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/diagram", "styleDef");
    private final static QName _StyleDefHdr_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/diagram", "styleDefHdr");
    private final static QName _StyleDefHdrLst_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/diagram", "styleDefHdrLst");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.dml.diagram
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTDataModel }
     * 
     */
    public CTDataModel createCTDataModel() {
        return new CTDataModel();
    }

    /**
     * Create an instance of {@link CTDiagramDefinition }
     * 
     */
    public CTDiagramDefinition createCTDiagramDefinition() {
        return new CTDiagramDefinition();
    }

    /**
     * Create an instance of {@link CTDiagramDefinitionHeader }
     * 
     */
    public CTDiagramDefinitionHeader createCTDiagramDefinitionHeader() {
        return new CTDiagramDefinitionHeader();
    }

    /**
     * Create an instance of {@link CTDiagramDefinitionHeaderLst }
     * 
     */
    public CTDiagramDefinitionHeaderLst createCTDiagramDefinitionHeaderLst() {
        return new CTDiagramDefinitionHeaderLst();
    }

    /**
     * Create an instance of {@link CTRelIds }
     * 
     */
    public CTRelIds createCTRelIds() {
        return new CTRelIds();
    }

    /**
     * Create an instance of {@link CTColorTransform }
     * 
     */
    public CTColorTransform createCTColorTransform() {
        return new CTColorTransform();
    }

    /**
     * Create an instance of {@link CTColorTransformHeader }
     * 
     */
    public CTColorTransformHeader createCTColorTransformHeader() {
        return new CTColorTransformHeader();
    }

    /**
     * Create an instance of {@link CTColorTransformHeaderLst }
     * 
     */
    public CTColorTransformHeaderLst createCTColorTransformHeaderLst() {
        return new CTColorTransformHeaderLst();
    }

    /**
     * Create an instance of {@link CTStyleDefinition }
     * 
     */
    public CTStyleDefinition createCTStyleDefinition() {
        return new CTStyleDefinition();
    }

    /**
     * Create an instance of {@link CTStyleDefinitionHeader }
     * 
     */
    public CTStyleDefinitionHeader createCTStyleDefinitionHeader() {
        return new CTStyleDefinitionHeader();
    }

    /**
     * Create an instance of {@link CTStyleDefinitionHeaderLst }
     * 
     */
    public CTStyleDefinitionHeaderLst createCTStyleDefinitionHeaderLst() {
        return new CTStyleDefinitionHeaderLst();
    }

    /**
     * Create an instance of {@link CTOrgChart }
     * 
     */
    public CTOrgChart createCTOrgChart() {
        return new CTOrgChart();
    }

    /**
     * Create an instance of {@link CTChildMax }
     * 
     */
    public CTChildMax createCTChildMax() {
        return new CTChildMax();
    }

    /**
     * Create an instance of {@link CTChildPref }
     * 
     */
    public CTChildPref createCTChildPref() {
        return new CTChildPref();
    }

    /**
     * Create an instance of {@link CTBulletEnabled }
     * 
     */
    public CTBulletEnabled createCTBulletEnabled() {
        return new CTBulletEnabled();
    }

    /**
     * Create an instance of {@link CTDirection }
     * 
     */
    public CTDirection createCTDirection() {
        return new CTDirection();
    }

    /**
     * Create an instance of {@link CTHierBranchStyle }
     * 
     */
    public CTHierBranchStyle createCTHierBranchStyle() {
        return new CTHierBranchStyle();
    }

    /**
     * Create an instance of {@link CTAnimOne }
     * 
     */
    public CTAnimOne createCTAnimOne() {
        return new CTAnimOne();
    }

    /**
     * Create an instance of {@link CTAnimLvl }
     * 
     */
    public CTAnimLvl createCTAnimLvl() {
        return new CTAnimLvl();
    }

    /**
     * Create an instance of {@link CTResizeHandles }
     * 
     */
    public CTResizeHandles createCTResizeHandles() {
        return new CTResizeHandles();
    }

    /**
     * Create an instance of {@link CTLayoutVariablePropertySet }
     * 
     */
    public CTLayoutVariablePropertySet createCTLayoutVariablePropertySet() {
        return new CTLayoutVariablePropertySet();
    }

    /**
     * Create an instance of {@link CTElemPropSet }
     * 
     */
    public CTElemPropSet createCTElemPropSet() {
        return new CTElemPropSet();
    }

    /**
     * Create an instance of {@link CTPt }
     * 
     */
    public CTPt createCTPt() {
        return new CTPt();
    }

    /**
     * Create an instance of {@link CTPtList }
     * 
     */
    public CTPtList createCTPtList() {
        return new CTPtList();
    }

    /**
     * Create an instance of {@link CTCxn }
     * 
     */
    public CTCxn createCTCxn() {
        return new CTCxn();
    }

    /**
     * Create an instance of {@link CTCxnList }
     * 
     */
    public CTCxnList createCTCxnList() {
        return new CTCxnList();
    }

    /**
     * Create an instance of {@link CTConstraint }
     * 
     */
    public CTConstraint createCTConstraint() {
        return new CTConstraint();
    }

    /**
     * Create an instance of {@link CTConstraints }
     * 
     */
    public CTConstraints createCTConstraints() {
        return new CTConstraints();
    }

    /**
     * Create an instance of {@link CTNumericRule }
     * 
     */
    public CTNumericRule createCTNumericRule() {
        return new CTNumericRule();
    }

    /**
     * Create an instance of {@link CTRules }
     * 
     */
    public CTRules createCTRules() {
        return new CTRules();
    }

    /**
     * Create an instance of {@link CTPresentationOf }
     * 
     */
    public CTPresentationOf createCTPresentationOf() {
        return new CTPresentationOf();
    }

    /**
     * Create an instance of {@link CTAdj }
     * 
     */
    public CTAdj createCTAdj() {
        return new CTAdj();
    }

    /**
     * Create an instance of {@link CTAdjLst }
     * 
     */
    public CTAdjLst createCTAdjLst() {
        return new CTAdjLst();
    }

    /**
     * Create an instance of {@link CTShape }
     * 
     */
    public CTShape createCTShape() {
        return new CTShape();
    }

    /**
     * Create an instance of {@link CTParameter }
     * 
     */
    public CTParameter createCTParameter() {
        return new CTParameter();
    }

    /**
     * Create an instance of {@link CTAlgorithm }
     * 
     */
    public CTAlgorithm createCTAlgorithm() {
        return new CTAlgorithm();
    }

    /**
     * Create an instance of {@link CTLayoutNode }
     * 
     */
    public CTLayoutNode createCTLayoutNode() {
        return new CTLayoutNode();
    }

    /**
     * Create an instance of {@link CTForEach }
     * 
     */
    public CTForEach createCTForEach() {
        return new CTForEach();
    }

    /**
     * Create an instance of {@link CTWhen }
     * 
     */
    public CTWhen createCTWhen() {
        return new CTWhen();
    }

    /**
     * Create an instance of {@link CTOtherwise }
     * 
     */
    public CTOtherwise createCTOtherwise() {
        return new CTOtherwise();
    }

    /**
     * Create an instance of {@link CTChoose }
     * 
     */
    public CTChoose createCTChoose() {
        return new CTChoose();
    }

    /**
     * Create an instance of {@link CTSampleData }
     * 
     */
    public CTSampleData createCTSampleData() {
        return new CTSampleData();
    }

    /**
     * Create an instance of {@link CTCategory }
     * 
     */
    public CTCategory createCTCategory() {
        return new CTCategory();
    }

    /**
     * Create an instance of {@link CTCategories }
     * 
     */
    public CTCategories createCTCategories() {
        return new CTCategories();
    }

    /**
     * Create an instance of {@link CTName }
     * 
     */
    public CTName createCTName() {
        return new CTName();
    }

    /**
     * Create an instance of {@link CTDescription }
     * 
     */
    public CTDescription createCTDescription() {
        return new CTDescription();
    }

    /**
     * Create an instance of {@link CTCTName }
     * 
     */
    public CTCTName createCTCTName() {
        return new CTCTName();
    }

    /**
     * Create an instance of {@link CTCTDescription }
     * 
     */
    public CTCTDescription createCTCTDescription() {
        return new CTCTDescription();
    }

    /**
     * Create an instance of {@link CTCTCategory }
     * 
     */
    public CTCTCategory createCTCTCategory() {
        return new CTCTCategory();
    }

    /**
     * Create an instance of {@link CTCTCategories }
     * 
     */
    public CTCTCategories createCTCTCategories() {
        return new CTCTCategories();
    }

    /**
     * Create an instance of {@link CTColors }
     * 
     */
    public CTColors createCTColors() {
        return new CTColors();
    }

    /**
     * Create an instance of {@link CTCTStyleLabel }
     * 
     */
    public CTCTStyleLabel createCTCTStyleLabel() {
        return new CTCTStyleLabel();
    }

    /**
     * Create an instance of {@link CTSDName }
     * 
     */
    public CTSDName createCTSDName() {
        return new CTSDName();
    }

    /**
     * Create an instance of {@link CTSDDescription }
     * 
     */
    public CTSDDescription createCTSDDescription() {
        return new CTSDDescription();
    }

    /**
     * Create an instance of {@link CTSDCategory }
     * 
     */
    public CTSDCategory createCTSDCategory() {
        return new CTSDCategory();
    }

    /**
     * Create an instance of {@link CTSDCategories }
     * 
     */
    public CTSDCategories createCTSDCategories() {
        return new CTSDCategories();
    }

    /**
     * Create an instance of {@link CTTextProps }
     * 
     */
    public CTTextProps createCTTextProps() {
        return new CTTextProps();
    }

    /**
     * Create an instance of {@link CTStyleLabel }
     * 
     */
    public CTStyleLabel createCTStyleLabel() {
        return new CTStyleLabel();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTTextBody }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/diagram", name = "t")
    public JAXBElement<CTTextBody> createT(CTTextBody value) {
        return new JAXBElement<CTTextBody>(_T_QNAME, CTTextBody.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTDataModel }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/diagram", name = "dataModel")
    public JAXBElement<CTDataModel> createDataModel(CTDataModel value) {
        return new JAXBElement<CTDataModel>(_DataModel_QNAME, CTDataModel.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTDiagramDefinition }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/diagram", name = "layoutDef")
    public JAXBElement<CTDiagramDefinition> createLayoutDef(CTDiagramDefinition value) {
        return new JAXBElement<CTDiagramDefinition>(_LayoutDef_QNAME, CTDiagramDefinition.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTDiagramDefinitionHeader }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/diagram", name = "layoutDefHdr")
    public JAXBElement<CTDiagramDefinitionHeader> createLayoutDefHdr(CTDiagramDefinitionHeader value) {
        return new JAXBElement<CTDiagramDefinitionHeader>(_LayoutDefHdr_QNAME, CTDiagramDefinitionHeader.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTDiagramDefinitionHeaderLst }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/diagram", name = "layoutDefHdrLst")
    public JAXBElement<CTDiagramDefinitionHeaderLst> createLayoutDefHdrLst(CTDiagramDefinitionHeaderLst value) {
        return new JAXBElement<CTDiagramDefinitionHeaderLst>(_LayoutDefHdrLst_QNAME, CTDiagramDefinitionHeaderLst.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTRelIds }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/diagram", name = "relIds")
    public JAXBElement<CTRelIds> createRelIds(CTRelIds value) {
        return new JAXBElement<CTRelIds>(_RelIds_QNAME, CTRelIds.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTColorTransform }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/diagram", name = "colorsDef")
    public JAXBElement<CTColorTransform> createColorsDef(CTColorTransform value) {
        return new JAXBElement<CTColorTransform>(_ColorsDef_QNAME, CTColorTransform.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTColorTransformHeader }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/diagram", name = "colorsDefHdr")
    public JAXBElement<CTColorTransformHeader> createColorsDefHdr(CTColorTransformHeader value) {
        return new JAXBElement<CTColorTransformHeader>(_ColorsDefHdr_QNAME, CTColorTransformHeader.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTColorTransformHeaderLst }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/diagram", name = "colorsDefHdrLst")
    public JAXBElement<CTColorTransformHeaderLst> createColorsDefHdrLst(CTColorTransformHeaderLst value) {
        return new JAXBElement<CTColorTransformHeaderLst>(_ColorsDefHdrLst_QNAME, CTColorTransformHeaderLst.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTStyleDefinition }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/diagram", name = "styleDef")
    public JAXBElement<CTStyleDefinition> createStyleDef(CTStyleDefinition value) {
        return new JAXBElement<CTStyleDefinition>(_StyleDef_QNAME, CTStyleDefinition.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTStyleDefinitionHeader }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/diagram", name = "styleDefHdr")
    public JAXBElement<CTStyleDefinitionHeader> createStyleDefHdr(CTStyleDefinitionHeader value) {
        return new JAXBElement<CTStyleDefinitionHeader>(_StyleDefHdr_QNAME, CTStyleDefinitionHeader.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTStyleDefinitionHeaderLst }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/diagram", name = "styleDefHdrLst")
    public JAXBElement<CTStyleDefinitionHeaderLst> createStyleDefHdrLst(CTStyleDefinitionHeaderLst value) {
        return new JAXBElement<CTStyleDefinitionHeaderLst>(_StyleDefHdrLst_QNAME, CTStyleDefinitionHeaderLst.class, null, value);
    }

}
