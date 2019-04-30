/*
 *  Copyright 2010-2012, Plutext Pty Ltd.
 *   
 *  This file is part of pptx4j, a component of docx4j.

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
package org.pptx4j.pml;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.pptx4j.pml package. 
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

    private final static QName _CmAuthorLst_QNAME = new QName("http://schemas.openxmlformats.org/presentationml/2006/main", "cmAuthorLst");
    private final static QName _CmLst_QNAME = new QName("http://schemas.openxmlformats.org/presentationml/2006/main", "cmLst");
    private final static QName _OleObj_QNAME = new QName("http://schemas.openxmlformats.org/presentationml/2006/main", "oleObj");
    private final static QName _SldSyncPr_QNAME = new QName("http://schemas.openxmlformats.org/presentationml/2006/main", "sldSyncPr");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.pptx4j.pml
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Presentation }
     * 
     */
    public Presentation createPresentation() {
        return new Presentation();
    }

    /**
     * Create an instance of {@link GroupShape }
     * 
     */
    public GroupShape createGroupShape() {
        return new GroupShape();
    }

    /**
     * Create an instance of {@link Pic }
     * 
     */
    public Pic createPic() {
        return new Pic();
    }

    /**
     * Create an instance of {@link CxnSp }
     * 
     */
    public CxnSp createCxnSp() {
        return new CxnSp();
    }

    /**
     * Create an instance of {@link Shape }
     * 
     */
    public Shape createShape() {
        return new Shape();
    }

    /**
     * Create an instance of {@link SlideLayoutIdList }
     * 
     */
    public SlideLayoutIdList createSlideLayoutIdList() {
        return new SlideLayoutIdList();
    }

    /**
     * Create an instance of {@link Presentation.SldIdLst }
     * 
     */
    public Presentation.SldIdLst createPresentationSldIdLst() {
        return new Presentation.SldIdLst();
    }

    /**
     * Create an instance of {@link Presentation.SldMasterIdLst }
     * 
     */
    public Presentation.SldMasterIdLst createPresentationSldMasterIdLst() {
        return new Presentation.SldMasterIdLst();
    }

    /**
     * Create an instance of {@link CTCommentAuthorList }
     * 
     */
    public CTCommentAuthorList createCTCommentAuthorList() {
        return new CTCommentAuthorList();
    }

    /**
     * Create an instance of {@link CTCommentList }
     * 
     */
    public CTCommentList createCTCommentList() {
        return new CTCommentList();
    }

    /**
     * Create an instance of {@link CTOleObject }
     * 
     */
    public CTOleObject createCTOleObject() {
        return new CTOleObject();
    }

    /**
     * Create an instance of {@link CTNotesMasterIdList }
     * 
     */
    public CTNotesMasterIdList createCTNotesMasterIdList() {
        return new CTNotesMasterIdList();
    }

    /**
     * Create an instance of {@link CTHandoutMasterIdList }
     * 
     */
    public CTHandoutMasterIdList createCTHandoutMasterIdList() {
        return new CTHandoutMasterIdList();
    }

    /**
     * Create an instance of {@link Presentation.SldSz }
     * 
     */
    public Presentation.SldSz createPresentationSldSz() {
        return new Presentation.SldSz();
    }

    /**
     * Create an instance of {@link CTSmartTags }
     * 
     */
    public CTSmartTags createCTSmartTags() {
        return new CTSmartTags();
    }

    /**
     * Create an instance of {@link CTEmbeddedFontList }
     * 
     */
    public CTEmbeddedFontList createCTEmbeddedFontList() {
        return new CTEmbeddedFontList();
    }

    /**
     * Create an instance of {@link CTCustomShowList }
     * 
     */
    public CTCustomShowList createCTCustomShowList() {
        return new CTCustomShowList();
    }

    /**
     * Create an instance of {@link CTPhotoAlbum }
     * 
     */
    public CTPhotoAlbum createCTPhotoAlbum() {
        return new CTPhotoAlbum();
    }

    /**
     * Create an instance of {@link CTCustomerDataList }
     * 
     */
    public CTCustomerDataList createCTCustomerDataList() {
        return new CTCustomerDataList();
    }

    /**
     * Create an instance of {@link CTKinsoku }
     * 
     */
    public CTKinsoku createCTKinsoku() {
        return new CTKinsoku();
    }

    /**
     * Create an instance of {@link CTModifyVerifier }
     * 
     */
    public CTModifyVerifier createCTModifyVerifier() {
        return new CTModifyVerifier();
    }

    /**
     * Create an instance of {@link CTExtensionList }
     * 
     */
    public CTExtensionList createCTExtensionList() {
        return new CTExtensionList();
    }

    /**
     * Create an instance of {@link PresentationPr }
     * 
     */
    public PresentationPr createPresentationPr() {
        return new PresentationPr();
    }

    /**
     * Create an instance of {@link CTPrintProperties }
     * 
     */
    public CTPrintProperties createCTPrintProperties() {
        return new CTPrintProperties();
    }

    /**
     * Create an instance of {@link CTShowProperties }
     * 
     */
    public CTShowProperties createCTShowProperties() {
        return new CTShowProperties();
    }

    /**
     * Create an instance of {@link Sld }
     * 
     */
    public Sld createSld() {
        return new Sld();
    }

    /**
     * Create an instance of {@link CommonSlideData }
     * 
     */
    public CommonSlideData createCommonSlideData() {
        return new CommonSlideData();
    }

    /**
     * Create an instance of {@link CTSlideTransition }
     * 
     */
    public CTSlideTransition createCTSlideTransition() {
        return new CTSlideTransition();
    }

    /**
     * Create an instance of {@link CTSlideTiming }
     * 
     */
    public CTSlideTiming createCTSlideTiming() {
        return new CTSlideTiming();
    }

    /**
     * Create an instance of {@link CTExtensionListModify }
     * 
     */
    public CTExtensionListModify createCTExtensionListModify() {
        return new CTExtensionListModify();
    }

    /**
     * Create an instance of {@link SldLayout }
     * 
     */
    public SldLayout createSldLayout() {
        return new SldLayout();
    }

    /**
     * Create an instance of {@link CTHeaderFooter }
     * 
     */
    public CTHeaderFooter createCTHeaderFooter() {
        return new CTHeaderFooter();
    }

    /**
     * Create an instance of {@link SldMaster }
     * 
     */
    public SldMaster createSldMaster() {
        return new SldMaster();
    }

    /**
     * Create an instance of {@link CTSlideMasterTextStyles }
     * 
     */
    public CTSlideMasterTextStyles createCTSlideMasterTextStyles() {
        return new CTSlideMasterTextStyles();
    }

    /**
     * Create an instance of {@link HandoutMaster }
     * 
     */
    public HandoutMaster createHandoutMaster() {
        return new HandoutMaster();
    }

    /**
     * Create an instance of {@link NotesMaster }
     * 
     */
    public NotesMaster createNotesMaster() {
        return new NotesMaster();
    }

    /**
     * Create an instance of {@link Notes }
     * 
     */
    public Notes createNotes() {
        return new Notes();
    }

    /**
     * Create an instance of {@link CTSlideSyncProperties }
     * 
     */
    public CTSlideSyncProperties createCTSlideSyncProperties() {
        return new CTSlideSyncProperties();
    }

    /**
     * Create an instance of {@link TagLst }
     * 
     */
    public TagLst createTagLst() {
        return new TagLst();
    }

    /**
     * Create an instance of {@link CTStringTag }
     * 
     */
    public CTStringTag createCTStringTag() {
        return new CTStringTag();
    }

    /**
     * Create an instance of {@link ViewPr }
     * 
     */
    public ViewPr createViewPr() {
        return new ViewPr();
    }

    /**
     * Create an instance of {@link CTNormalViewProperties }
     * 
     */
    public CTNormalViewProperties createCTNormalViewProperties() {
        return new CTNormalViewProperties();
    }

    /**
     * Create an instance of {@link CTSlideViewProperties }
     * 
     */
    public CTSlideViewProperties createCTSlideViewProperties() {
        return new CTSlideViewProperties();
    }

    /**
     * Create an instance of {@link CTOutlineViewProperties }
     * 
     */
    public CTOutlineViewProperties createCTOutlineViewProperties() {
        return new CTOutlineViewProperties();
    }

    /**
     * Create an instance of {@link CTNotesTextViewProperties }
     * 
     */
    public CTNotesTextViewProperties createCTNotesTextViewProperties() {
        return new CTNotesTextViewProperties();
    }

    /**
     * Create an instance of {@link CTSlideSorterViewProperties }
     * 
     */
    public CTSlideSorterViewProperties createCTSlideSorterViewProperties() {
        return new CTSlideSorterViewProperties();
    }

    /**
     * Create an instance of {@link CTNotesViewProperties }
     * 
     */
    public CTNotesViewProperties createCTNotesViewProperties() {
        return new CTNotesViewProperties();
    }

    /**
     * Create an instance of {@link CTSideDirectionTransition }
     * 
     */
    public CTSideDirectionTransition createCTSideDirectionTransition() {
        return new CTSideDirectionTransition();
    }

    /**
     * Create an instance of {@link CTCornerDirectionTransition }
     * 
     */
    public CTCornerDirectionTransition createCTCornerDirectionTransition() {
        return new CTCornerDirectionTransition();
    }

    /**
     * Create an instance of {@link CTEightDirectionTransition }
     * 
     */
    public CTEightDirectionTransition createCTEightDirectionTransition() {
        return new CTEightDirectionTransition();
    }

    /**
     * Create an instance of {@link CTOrientationTransition }
     * 
     */
    public CTOrientationTransition createCTOrientationTransition() {
        return new CTOrientationTransition();
    }

    /**
     * Create an instance of {@link CTInOutTransition }
     * 
     */
    public CTInOutTransition createCTInOutTransition() {
        return new CTInOutTransition();
    }

    /**
     * Create an instance of {@link CTOptionalBlackTransition }
     * 
     */
    public CTOptionalBlackTransition createCTOptionalBlackTransition() {
        return new CTOptionalBlackTransition();
    }

    /**
     * Create an instance of {@link CTSplitTransition }
     * 
     */
    public CTSplitTransition createCTSplitTransition() {
        return new CTSplitTransition();
    }

    /**
     * Create an instance of {@link CTWheelTransition }
     * 
     */
    public CTWheelTransition createCTWheelTransition() {
        return new CTWheelTransition();
    }

    /**
     * Create an instance of {@link CTTransitionStartSoundAction }
     * 
     */
    public CTTransitionStartSoundAction createCTTransitionStartSoundAction() {
        return new CTTransitionStartSoundAction();
    }

    /**
     * Create an instance of {@link CTTransitionSoundAction }
     * 
     */
    public CTTransitionSoundAction createCTTransitionSoundAction() {
        return new CTTransitionSoundAction();
    }

    /**
     * Create an instance of {@link CTTLIterateIntervalTime }
     * 
     */
    public CTTLIterateIntervalTime createCTTLIterateIntervalTime() {
        return new CTTLIterateIntervalTime();
    }

    /**
     * Create an instance of {@link CTTLIterateIntervalPercentage }
     * 
     */
    public CTTLIterateIntervalPercentage createCTTLIterateIntervalPercentage() {
        return new CTTLIterateIntervalPercentage();
    }

    /**
     * Create an instance of {@link CTTLIterateData }
     * 
     */
    public CTTLIterateData createCTTLIterateData() {
        return new CTTLIterateData();
    }

    /**
     * Create an instance of {@link CTTLSubShapeId }
     * 
     */
    public CTTLSubShapeId createCTTLSubShapeId() {
        return new CTTLSubShapeId();
    }

    /**
     * Create an instance of {@link CTTLTextTargetElement }
     * 
     */
    public CTTLTextTargetElement createCTTLTextTargetElement() {
        return new CTTLTextTargetElement();
    }

    /**
     * Create an instance of {@link CTTLOleChartTargetElement }
     * 
     */
    public CTTLOleChartTargetElement createCTTLOleChartTargetElement() {
        return new CTTLOleChartTargetElement();
    }

    /**
     * Create an instance of {@link CTTLShapeTargetElement }
     * 
     */
    public CTTLShapeTargetElement createCTTLShapeTargetElement() {
        return new CTTLShapeTargetElement();
    }

    /**
     * Create an instance of {@link CTTLTimeTargetElement }
     * 
     */
    public CTTLTimeTargetElement createCTTLTimeTargetElement() {
        return new CTTLTimeTargetElement();
    }

    /**
     * Create an instance of {@link CTTLTriggerTimeNodeID }
     * 
     */
    public CTTLTriggerTimeNodeID createCTTLTriggerTimeNodeID() {
        return new CTTLTriggerTimeNodeID();
    }

    /**
     * Create an instance of {@link CTTLTriggerRuntimeNode }
     * 
     */
    public CTTLTriggerRuntimeNode createCTTLTriggerRuntimeNode() {
        return new CTTLTriggerRuntimeNode();
    }

    /**
     * Create an instance of {@link CTTLTimeCondition }
     * 
     */
    public CTTLTimeCondition createCTTLTimeCondition() {
        return new CTTLTimeCondition();
    }

    /**
     * Create an instance of {@link CTTLTimeConditionList }
     * 
     */
    public CTTLTimeConditionList createCTTLTimeConditionList() {
        return new CTTLTimeConditionList();
    }

    /**
     * Create an instance of {@link CTTimeNodeList }
     * 
     */
    public CTTimeNodeList createCTTimeNodeList() {
        return new CTTimeNodeList();
    }

    /**
     * Create an instance of {@link CTTLCommonTimeNodeData }
     * 
     */
    public CTTLCommonTimeNodeData createCTTLCommonTimeNodeData() {
        return new CTTLCommonTimeNodeData();
    }

    /**
     * Create an instance of {@link CTTLTimeNodeParallel }
     * 
     */
    public CTTLTimeNodeParallel createCTTLTimeNodeParallel() {
        return new CTTLTimeNodeParallel();
    }

    /**
     * Create an instance of {@link CTTLTimeNodeSequence }
     * 
     */
    public CTTLTimeNodeSequence createCTTLTimeNodeSequence() {
        return new CTTLTimeNodeSequence();
    }

    /**
     * Create an instance of {@link CTTLTimeNodeExclusive }
     * 
     */
    public CTTLTimeNodeExclusive createCTTLTimeNodeExclusive() {
        return new CTTLTimeNodeExclusive();
    }

    /**
     * Create an instance of {@link CTTLBehaviorAttributeNameList }
     * 
     */
    public CTTLBehaviorAttributeNameList createCTTLBehaviorAttributeNameList() {
        return new CTTLBehaviorAttributeNameList();
    }

    /**
     * Create an instance of {@link CTTLCommonBehaviorData }
     * 
     */
    public CTTLCommonBehaviorData createCTTLCommonBehaviorData() {
        return new CTTLCommonBehaviorData();
    }

    /**
     * Create an instance of {@link CTTLAnimVariantBooleanVal }
     * 
     */
    public CTTLAnimVariantBooleanVal createCTTLAnimVariantBooleanVal() {
        return new CTTLAnimVariantBooleanVal();
    }

    /**
     * Create an instance of {@link CTTLAnimVariantIntegerVal }
     * 
     */
    public CTTLAnimVariantIntegerVal createCTTLAnimVariantIntegerVal() {
        return new CTTLAnimVariantIntegerVal();
    }

    /**
     * Create an instance of {@link CTTLAnimVariantFloatVal }
     * 
     */
    public CTTLAnimVariantFloatVal createCTTLAnimVariantFloatVal() {
        return new CTTLAnimVariantFloatVal();
    }

    /**
     * Create an instance of {@link CTTLAnimVariantStringVal }
     * 
     */
    public CTTLAnimVariantStringVal createCTTLAnimVariantStringVal() {
        return new CTTLAnimVariantStringVal();
    }

    /**
     * Create an instance of {@link CTTLAnimVariant }
     * 
     */
    public CTTLAnimVariant createCTTLAnimVariant() {
        return new CTTLAnimVariant();
    }

    /**
     * Create an instance of {@link CTTLTimeAnimateValue }
     * 
     */
    public CTTLTimeAnimateValue createCTTLTimeAnimateValue() {
        return new CTTLTimeAnimateValue();
    }

    /**
     * Create an instance of {@link CTTLTimeAnimateValueList }
     * 
     */
    public CTTLTimeAnimateValueList createCTTLTimeAnimateValueList() {
        return new CTTLTimeAnimateValueList();
    }

    /**
     * Create an instance of {@link CTTLAnimateBehavior }
     * 
     */
    public CTTLAnimateBehavior createCTTLAnimateBehavior() {
        return new CTTLAnimateBehavior();
    }

    /**
     * Create an instance of {@link CTTLByRgbColorTransform }
     * 
     */
    public CTTLByRgbColorTransform createCTTLByRgbColorTransform() {
        return new CTTLByRgbColorTransform();
    }

    /**
     * Create an instance of {@link CTTLByHslColorTransform }
     * 
     */
    public CTTLByHslColorTransform createCTTLByHslColorTransform() {
        return new CTTLByHslColorTransform();
    }

    /**
     * Create an instance of {@link CTTLByAnimateColorTransform }
     * 
     */
    public CTTLByAnimateColorTransform createCTTLByAnimateColorTransform() {
        return new CTTLByAnimateColorTransform();
    }

    /**
     * Create an instance of {@link CTTLAnimateColorBehavior }
     * 
     */
    public CTTLAnimateColorBehavior createCTTLAnimateColorBehavior() {
        return new CTTLAnimateColorBehavior();
    }

    /**
     * Create an instance of {@link CTTLAnimateEffectBehavior }
     * 
     */
    public CTTLAnimateEffectBehavior createCTTLAnimateEffectBehavior() {
        return new CTTLAnimateEffectBehavior();
    }

    /**
     * Create an instance of {@link CTTLPoint }
     * 
     */
    public CTTLPoint createCTTLPoint() {
        return new CTTLPoint();
    }

    /**
     * Create an instance of {@link CTTLAnimateMotionBehavior }
     * 
     */
    public CTTLAnimateMotionBehavior createCTTLAnimateMotionBehavior() {
        return new CTTLAnimateMotionBehavior();
    }

    /**
     * Create an instance of {@link CTTLAnimateRotationBehavior }
     * 
     */
    public CTTLAnimateRotationBehavior createCTTLAnimateRotationBehavior() {
        return new CTTLAnimateRotationBehavior();
    }

    /**
     * Create an instance of {@link CTTLAnimateScaleBehavior }
     * 
     */
    public CTTLAnimateScaleBehavior createCTTLAnimateScaleBehavior() {
        return new CTTLAnimateScaleBehavior();
    }

    /**
     * Create an instance of {@link CTTLCommandBehavior }
     * 
     */
    public CTTLCommandBehavior createCTTLCommandBehavior() {
        return new CTTLCommandBehavior();
    }

    /**
     * Create an instance of {@link CTTLSetBehavior }
     * 
     */
    public CTTLSetBehavior createCTTLSetBehavior() {
        return new CTTLSetBehavior();
    }

    /**
     * Create an instance of {@link CTTLCommonMediaNodeData }
     * 
     */
    public CTTLCommonMediaNodeData createCTTLCommonMediaNodeData() {
        return new CTTLCommonMediaNodeData();
    }

    /**
     * Create an instance of {@link CTTLMediaNodeAudio }
     * 
     */
    public CTTLMediaNodeAudio createCTTLMediaNodeAudio() {
        return new CTTLMediaNodeAudio();
    }

    /**
     * Create an instance of {@link CTTLMediaNodeVideo }
     * 
     */
    public CTTLMediaNodeVideo createCTTLMediaNodeVideo() {
        return new CTTLMediaNodeVideo();
    }

    /**
     * Create an instance of {@link CTTLTemplate }
     * 
     */
    public CTTLTemplate createCTTLTemplate() {
        return new CTTLTemplate();
    }

    /**
     * Create an instance of {@link CTTLTemplateList }
     * 
     */
    public CTTLTemplateList createCTTLTemplateList() {
        return new CTTLTemplateList();
    }

    /**
     * Create an instance of {@link CTTLBuildParagraph }
     * 
     */
    public CTTLBuildParagraph createCTTLBuildParagraph() {
        return new CTTLBuildParagraph();
    }

    /**
     * Create an instance of {@link CTTLBuildDiagram }
     * 
     */
    public CTTLBuildDiagram createCTTLBuildDiagram() {
        return new CTTLBuildDiagram();
    }

    /**
     * Create an instance of {@link CTTLOleBuildChart }
     * 
     */
    public CTTLOleBuildChart createCTTLOleBuildChart() {
        return new CTTLOleBuildChart();
    }

    /**
     * Create an instance of {@link CTTLGraphicalObjectBuild }
     * 
     */
    public CTTLGraphicalObjectBuild createCTTLGraphicalObjectBuild() {
        return new CTTLGraphicalObjectBuild();
    }

    /**
     * Create an instance of {@link CTBuildList }
     * 
     */
    public CTBuildList createCTBuildList() {
        return new CTBuildList();
    }

    /**
     * Create an instance of {@link CTEmpty }
     * 
     */
    public CTEmpty createCTEmpty() {
        return new CTEmpty();
    }

    /**
     * Create an instance of {@link CTIndexRange }
     * 
     */
    public CTIndexRange createCTIndexRange() {
        return new CTIndexRange();
    }

    /**
     * Create an instance of {@link CTSlideRelationshipListEntry }
     * 
     */
    public CTSlideRelationshipListEntry createCTSlideRelationshipListEntry() {
        return new CTSlideRelationshipListEntry();
    }

    /**
     * Create an instance of {@link CTSlideRelationshipList }
     * 
     */
    public CTSlideRelationshipList createCTSlideRelationshipList() {
        return new CTSlideRelationshipList();
    }

    /**
     * Create an instance of {@link CTCustomShowId }
     * 
     */
    public CTCustomShowId createCTCustomShowId() {
        return new CTCustomShowId();
    }

    /**
     * Create an instance of {@link CTCustomerData }
     * 
     */
    public CTCustomerData createCTCustomerData() {
        return new CTCustomerData();
    }

    /**
     * Create an instance of {@link CTTagsData }
     * 
     */
    public CTTagsData createCTTagsData() {
        return new CTTagsData();
    }

    /**
     * Create an instance of {@link CTExtension }
     * 
     */
    public CTExtension createCTExtension() {
        return new CTExtension();
    }

    /**
     * Create an instance of {@link CTCommentAuthor }
     * 
     */
    public CTCommentAuthor createCTCommentAuthor() {
        return new CTCommentAuthor();
    }

    /**
     * Create an instance of {@link CTComment }
     * 
     */
    public CTComment createCTComment() {
        return new CTComment();
    }

    /**
     * Create an instance of {@link CTOleObjectEmbed }
     * 
     */
    public CTOleObjectEmbed createCTOleObjectEmbed() {
        return new CTOleObjectEmbed();
    }

    /**
     * Create an instance of {@link CTOleObjectLink }
     * 
     */
    public CTOleObjectLink createCTOleObjectLink() {
        return new CTOleObjectLink();
    }

    /**
     * Create an instance of {@link CTControl }
     * 
     */
    public CTControl createCTControl() {
        return new CTControl();
    }

    /**
     * Create an instance of {@link CTControlList }
     * 
     */
    public CTControlList createCTControlList() {
        return new CTControlList();
    }

    /**
     * Create an instance of {@link CTNotesMasterIdListEntry }
     * 
     */
    public CTNotesMasterIdListEntry createCTNotesMasterIdListEntry() {
        return new CTNotesMasterIdListEntry();
    }

    /**
     * Create an instance of {@link CTHandoutMasterIdListEntry }
     * 
     */
    public CTHandoutMasterIdListEntry createCTHandoutMasterIdListEntry() {
        return new CTHandoutMasterIdListEntry();
    }

    /**
     * Create an instance of {@link CTEmbeddedFontDataId }
     * 
     */
    public CTEmbeddedFontDataId createCTEmbeddedFontDataId() {
        return new CTEmbeddedFontDataId();
    }

    /**
     * Create an instance of {@link CTEmbeddedFontListEntry }
     * 
     */
    public CTEmbeddedFontListEntry createCTEmbeddedFontListEntry() {
        return new CTEmbeddedFontListEntry();
    }

    /**
     * Create an instance of {@link CTCustomShow }
     * 
     */
    public CTCustomShow createCTCustomShow() {
        return new CTCustomShow();
    }

    /**
     * Create an instance of {@link CTHtmlPublishProperties }
     * 
     */
    public CTHtmlPublishProperties createCTHtmlPublishProperties() {
        return new CTHtmlPublishProperties();
    }

    /**
     * Create an instance of {@link CTShowInfoBrowse }
     * 
     */
    public CTShowInfoBrowse createCTShowInfoBrowse() {
        return new CTShowInfoBrowse();
    }

    /**
     * Create an instance of {@link CTShowInfoKiosk }
     * 
     */
    public CTShowInfoKiosk createCTShowInfoKiosk() {
        return new CTShowInfoKiosk();
    }

    /**
     * Create an instance of {@link CTPlaceholder }
     * 
     */
    public CTPlaceholder createCTPlaceholder() {
        return new CTPlaceholder();
    }

    /**
     * Create an instance of {@link NvPr }
     * 
     */
    public NvPr createNvPr() {
        return new NvPr();
    }

    /**
     * Create an instance of {@link CTGraphicalObjectFrameNonVisual }
     * 
     */
    public CTGraphicalObjectFrameNonVisual createCTGraphicalObjectFrameNonVisual() {
        return new CTGraphicalObjectFrameNonVisual();
    }

    /**
     * Create an instance of {@link CTGraphicalObjectFrame }
     * 
     */
    public CTGraphicalObjectFrame createCTGraphicalObjectFrame() {
        return new CTGraphicalObjectFrame();
    }

    /**
     * Create an instance of {@link CTRel }
     * 
     */
    public CTRel createCTRel() {
        return new CTRel();
    }

    /**
     * Create an instance of {@link CTBackgroundProperties }
     * 
     */
    public CTBackgroundProperties createCTBackgroundProperties() {
        return new CTBackgroundProperties();
    }

    /**
     * Create an instance of {@link CTBackground }
     * 
     */
    public CTBackground createCTBackground() {
        return new CTBackground();
    }

    /**
     * Create an instance of {@link CTNormalViewPortion }
     * 
     */
    public CTNormalViewPortion createCTNormalViewPortion() {
        return new CTNormalViewPortion();
    }

    /**
     * Create an instance of {@link CTCommonViewProperties }
     * 
     */
    public CTCommonViewProperties createCTCommonViewProperties() {
        return new CTCommonViewProperties();
    }

    /**
     * Create an instance of {@link CTOutlineViewSlideEntry }
     * 
     */
    public CTOutlineViewSlideEntry createCTOutlineViewSlideEntry() {
        return new CTOutlineViewSlideEntry();
    }

    /**
     * Create an instance of {@link CTOutlineViewSlideList }
     * 
     */
    public CTOutlineViewSlideList createCTOutlineViewSlideList() {
        return new CTOutlineViewSlideList();
    }

    /**
     * Create an instance of {@link CTGuide }
     * 
     */
    public CTGuide createCTGuide() {
        return new CTGuide();
    }

    /**
     * Create an instance of {@link CTGuideList }
     * 
     */
    public CTGuideList createCTGuideList() {
        return new CTGuideList();
    }

    /**
     * Create an instance of {@link CTCommonSlideViewProperties }
     * 
     */
    public CTCommonSlideViewProperties createCTCommonSlideViewProperties() {
        return new CTCommonSlideViewProperties();
    }

    /**
     * Create an instance of {@link GroupShape.NvGrpSpPr }
     * 
     */
    public GroupShape.NvGrpSpPr createGroupShapeNvGrpSpPr() {
        return new GroupShape.NvGrpSpPr();
    }

    /**
     * Create an instance of {@link Pic.NvPicPr }
     * 
     */
    public Pic.NvPicPr createPicNvPicPr() {
        return new Pic.NvPicPr();
    }

    /**
     * Create an instance of {@link CxnSp.NvCxnSpPr }
     * 
     */
    public CxnSp.NvCxnSpPr createCxnSpNvCxnSpPr() {
        return new CxnSp.NvCxnSpPr();
    }

    /**
     * Create an instance of {@link Shape.NvSpPr }
     * 
     */
    public Shape.NvSpPr createShapeNvSpPr() {
        return new Shape.NvSpPr();
    }

    /**
     * Create an instance of {@link SlideLayoutIdList.SldLayoutId }
     * 
     */
    public SlideLayoutIdList.SldLayoutId createSlideLayoutIdListSldLayoutId() {
        return new SlideLayoutIdList.SldLayoutId();
    }

    /**
     * Create an instance of {@link Presentation.SldIdLst.SldId }
     * 
     */
    public Presentation.SldIdLst.SldId createPresentationSldIdLstSldId() {
        return new Presentation.SldIdLst.SldId();
    }

    /**
     * Create an instance of {@link Presentation.SldMasterIdLst.SldMasterId }
     * 
     */
    public Presentation.SldMasterIdLst.SldMasterId createPresentationSldMasterIdLstSldMasterId() {
        return new Presentation.SldMasterIdLst.SldMasterId();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTCommentAuthorList }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/presentationml/2006/main", name = "cmAuthorLst")
    public JAXBElement<CTCommentAuthorList> createCmAuthorLst(CTCommentAuthorList value) {
        return new JAXBElement<CTCommentAuthorList>(_CmAuthorLst_QNAME, CTCommentAuthorList.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTCommentList }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/presentationml/2006/main", name = "cmLst")
    public JAXBElement<CTCommentList> createCmLst(CTCommentList value) {
        return new JAXBElement<CTCommentList>(_CmLst_QNAME, CTCommentList.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTOleObject }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/presentationml/2006/main", name = "oleObj")
    public JAXBElement<CTOleObject> createOleObj(CTOleObject value) {
        return new JAXBElement<CTOleObject>(_OleObj_QNAME, CTOleObject.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTSlideSyncProperties }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/presentationml/2006/main", name = "sldSyncPr")
    public JAXBElement<CTSlideSyncProperties> createSldSyncPr(CTSlideSyncProperties value) {
        return new JAXBElement<CTSlideSyncProperties>(_SldSyncPr_QNAME, CTSlideSyncProperties.class, null, value);
    }

}
