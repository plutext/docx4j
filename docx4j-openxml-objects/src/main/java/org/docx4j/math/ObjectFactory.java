/*
 *  Copyright 2009-2012, Plutext Pty Ltd.
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
package org.docx4j.math;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import org.docx4j.wml.CTBookmark;
import org.docx4j.wml.CTFtnEdnRef;
import org.docx4j.wml.CTMarkup;
import org.docx4j.wml.CTMarkupRange;
import org.docx4j.wml.CTMoveBookmark;
import org.docx4j.wml.CTObject;
import org.docx4j.wml.CTPerm;
import org.docx4j.wml.CTRuby;
import org.docx4j.wml.CTTrackChange;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.FldChar;
import org.docx4j.wml.Pict;
import org.docx4j.wml.RPr;
import org.docx4j.wml.RangePermissionStart;
import org.docx4j.wml.RunTrackChange;
import org.docx4j.wml.Text;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.math package. 
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

    private final static QName _OMath_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/math", "oMath");
    private final static QName _OMathPara_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/math", "oMathPara");
    private final static QName _MathPr_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/math", "mathPr");
    private final static QName _CTOMathMoveFromRangeStart_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "moveFromRangeStart");
    private final static QName _CTOMathMoveFromRangeEnd_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "moveFromRangeEnd");
    private final static QName _CTOMathSPre_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/math", "sPre");
    private final static QName _CTOMathCustomXmlDelRangeEnd_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "customXmlDelRangeEnd");
    private final static QName _CTOMathMoveToRangeEnd_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "moveToRangeEnd");
    private final static QName _CTOMathBorderBox_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/math", "borderBox");
    private final static QName _CTOMathCustomXmlMoveToRangeEnd_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "customXmlMoveToRangeEnd");
    private final static QName _CTOMathCustomXmlInsRangeStart_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "customXmlInsRangeStart");
    private final static QName _CTOMathCustomXmlMoveToRangeStart_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "customXmlMoveToRangeStart");
    private final static QName _CTOMathCustomXmlInsRangeEnd_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "customXmlInsRangeEnd");
    private final static QName _CTOMathGroupChr_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/math", "groupChr");
    private final static QName _CTOMathPermStart_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "permStart");
    private final static QName _CTOMathMoveToRangeStart_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "moveToRangeStart");
    private final static QName _CTOMathFunc_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/math", "func");
    private final static QName _CTOMathBookmarkEnd_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "bookmarkEnd");
    private final static QName _CTOMathSSubSup_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/math", "sSubSup");
    private final static QName _CTOMathCustomXmlMoveFromRangeEnd_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "customXmlMoveFromRangeEnd");
    private final static QName _CTOMathLimUpp_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/math", "limUpp");
    private final static QName _CTOMathAcc_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/math", "acc");
    private final static QName _CTOMathMoveFrom_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "moveFrom");
    private final static QName _CTOMathCustomXmlMoveFromRangeStart_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "customXmlMoveFromRangeStart");
    private final static QName _CTOMathM_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/math", "m");
    private final static QName _CTOMathCustomXmlDelRangeStart_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "customXmlDelRangeStart");
    private final static QName _CTOMathLimLow_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/math", "limLow");
    private final static QName _CTOMathSSup_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/math", "sSup");
    private final static QName _CTOMathD_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/math", "d");
    private final static QName _CTOMathF_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/math", "f");
    private final static QName _CTOMathEqArr_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/math", "eqArr");
    private final static QName _CTOMathNary_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/math", "nary");
    private final static QName _CTOMathRad_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/math", "rad");
    private final static QName _CTOMathBar_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/math", "bar");
    private final static QName _CTOMathPermEnd_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "permEnd");
    private final static QName _CTOMathBox_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/math", "box");
    private final static QName _CTOMathR_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/math", "r");
    private final static QName _CTOMathBookmarkStart_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "bookmarkStart");
    private final static QName _CTOMathMoveTo_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "moveTo");
    private final static QName _CTOMathPhant_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/math", "phant");
    private final static QName _CTOMathSSub_QNAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/math", "sSub");
    private final static QName _CTRMonthShort_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "monthShort");
    private final static QName _CTRYearLong_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "yearLong");
    private final static QName _CTRFootnoteReference_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "footnoteReference");
    private final static QName _CTRPgNum_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "pgNum");
    private final static QName _CTREndnoteRef_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "endnoteRef");
    private final static QName _CTRSoftHyphen_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "softHyphen");
    private final static QName _CTREndnoteReference_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "endnoteReference");
    private final static QName _CTRT_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "t");
    private final static QName _CTRCr_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "cr");
    private final static QName _CTRFldChar_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "fldChar");
    private final static QName _CTRCommentReference_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "commentReference");
    private final static QName _CTRSeparator_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "separator");
    private final static QName _CTRRPr_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "rPr");
    private final static QName _CTRDayLong_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "dayLong");
    private final static QName _CTRAnnotationRef_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "annotationRef");
    private final static QName _CTRRuby_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "ruby");
    private final static QName _CTRObject_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "object");
    private final static QName _CTRTab_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "tab");
    private final static QName _CTRLastRenderedPageBreak_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "lastRenderedPageBreak");
    private final static QName _CTRDrawing_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "drawing");
    private final static QName _CTRDelInstrText_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "delInstrText");
    private final static QName _CTRInstrText_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "instrText");
    private final static QName _CTRSym_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "sym");
    private final static QName _CTRPict_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "pict");
    private final static QName _CTRContinuationSeparator_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "continuationSeparator");
    private final static QName _CTRYearShort_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "yearShort");
    private final static QName _CTRFootnoteRef_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "footnoteRef");
    private final static QName _CTRDayShort_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "dayShort");
    private final static QName _CTRMonthLong_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "monthLong");
    private final static QName _CTRPtab_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "ptab");
    private final static QName _CTRNoBreakHyphen_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "noBreakHyphen");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.math
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTOMathPara }
     * 
     */
    public CTOMathPara createCTOMathPara() {
        return new CTOMathPara();
    }

    /**
     * Create an instance of {@link CTOMath }
     * 
     */
    public CTOMath createCTOMath() {
        return new CTOMath();
    }

    /**
     * Create an instance of {@link CTMathPr }
     * 
     */
    public CTMathPr createCTMathPr() {
        return new CTMathPr();
    }

    /**
     * Create an instance of {@link CTMPr }
     * 
     */
    public CTMPr createCTMPr() {
        return new CTMPr();
    }

    /**
     * Create an instance of {@link CTMC }
     * 
     */
    public CTMC createCTMC() {
        return new CTMC();
    }

    /**
     * Create an instance of {@link CTLimUppPr }
     * 
     */
    public CTLimUppPr createCTLimUppPr() {
        return new CTLimUppPr();
    }

    /**
     * Create an instance of {@link CTAcc }
     * 
     */
    public CTAcc createCTAcc() {
        return new CTAcc();
    }

    /**
     * Create an instance of {@link CTShp }
     * 
     */
    public CTShp createCTShp() {
        return new CTShp();
    }

    /**
     * Create an instance of {@link CTMCPr }
     * 
     */
    public CTMCPr createCTMCPr() {
        return new CTMCPr();
    }

    /**
     * Create an instance of {@link CTSSubSupPr }
     * 
     */
    public CTSSubSupPr createCTSSubSupPr() {
        return new CTSSubSupPr();
    }

    /**
     * Create an instance of {@link CTBreakBinSub }
     * 
     */
    public CTBreakBinSub createCTBreakBinSub() {
        return new CTBreakBinSub();
    }

    /**
     * Create an instance of {@link CTFPr }
     * 
     */
    public CTFPr createCTFPr() {
        return new CTFPr();
    }

    /**
     * Create an instance of {@link CTOMathParaPr }
     * 
     */
    public CTOMathParaPr createCTOMathParaPr() {
        return new CTOMathParaPr();
    }

    /**
     * Create an instance of {@link CTSSubSup }
     * 
     */
    public CTSSubSup createCTSSubSup() {
        return new CTSSubSup();
    }

    /**
     * Create an instance of {@link CTScript }
     * 
     */
    public CTScript createCTScript() {
        return new CTScript();
    }

    /**
     * Create an instance of {@link CTDPr }
     * 
     */
    public CTDPr createCTDPr() {
        return new CTDPr();
    }

    /**
     * Create an instance of {@link CTMR }
     * 
     */
    public CTMR createCTMR() {
        return new CTMR();
    }

    /**
     * Create an instance of {@link CTYAlign }
     * 
     */
    public CTYAlign createCTYAlign() {
        return new CTYAlign();
    }

    /**
     * Create an instance of {@link CTXAlign }
     * 
     */
    public CTXAlign createCTXAlign() {
        return new CTXAlign();
    }

    /**
     * Create an instance of {@link CTBorderBox }
     * 
     */
    public CTBorderBox createCTBorderBox() {
        return new CTBorderBox();
    }

    /**
     * Create an instance of {@link CTOMathArg }
     * 
     */
    public CTOMathArg createCTOMathArg() {
        return new CTOMathArg();
    }

    /**
     * Create an instance of {@link CTAccPr }
     * 
     */
    public CTAccPr createCTAccPr() {
        return new CTAccPr();
    }

    /**
     * Create an instance of {@link CTStyle }
     * 
     */
    public CTStyle createCTStyle() {
        return new CTStyle();
    }

    /**
     * Create an instance of {@link CTManualBreak }
     * 
     */
    public CTManualBreak createCTManualBreak() {
        return new CTManualBreak();
    }

    /**
     * Create an instance of {@link CTRadPr }
     * 
     */
    public CTRadPr createCTRadPr() {
        return new CTRadPr();
    }

    /**
     * Create an instance of {@link CTSpacingRule }
     * 
     */
    public CTSpacingRule createCTSpacingRule() {
        return new CTSpacingRule();
    }

    /**
     * Create an instance of {@link CTPhantPr }
     * 
     */
    public CTPhantPr createCTPhantPr() {
        return new CTPhantPr();
    }

    /**
     * Create an instance of {@link CTLimLow }
     * 
     */
    public CTLimLow createCTLimLow() {
        return new CTLimLow();
    }

    /**
     * Create an instance of {@link CTText }
     * 
     */
    public CTText createCTText() {
        return new CTText();
    }

    /**
     * Create an instance of {@link CTNaryPr }
     * 
     */
    public CTNaryPr createCTNaryPr() {
        return new CTNaryPr();
    }

    /**
     * Create an instance of {@link CTOMathJc }
     * 
     */
    public CTOMathJc createCTOMathJc() {
        return new CTOMathJc();
    }

    /**
     * Create an instance of {@link CTChar }
     * 
     */
    public CTChar createCTChar() {
        return new CTChar();
    }

    /**
     * Create an instance of {@link CTNary }
     * 
     */
    public CTNary createCTNary() {
        return new CTNary();
    }

    /**
     * Create an instance of {@link CTRad }
     * 
     */
    public CTRad createCTRad() {
        return new CTRad();
    }

    /**
     * Create an instance of {@link CTRPR }
     * 
     */
    public CTRPR createCTRPR() {
        return new CTRPR();
    }

    /**
     * Create an instance of {@link CTLimUpp }
     * 
     */
    public CTLimUpp createCTLimUpp() {
        return new CTLimUpp();
    }

    /**
     * Create an instance of {@link CTEqArr }
     * 
     */
    public CTEqArr createCTEqArr() {
        return new CTEqArr();
    }

    /**
     * Create an instance of {@link CTSSup }
     * 
     */
    public CTSSup createCTSSup() {
        return new CTSSup();
    }

    /**
     * Create an instance of {@link CTLimLowPr }
     * 
     */
    public CTLimLowPr createCTLimLowPr() {
        return new CTLimLowPr();
    }

    /**
     * Create an instance of {@link CTInteger2 }
     * 
     */
    public CTInteger2 createCTInteger2() {
        return new CTInteger2();
    }

    /**
     * Create an instance of {@link CTCtrlPr }
     * 
     */
    public CTCtrlPr createCTCtrlPr() {
        return new CTCtrlPr();
    }

    /**
     * Create an instance of {@link CTM }
     * 
     */
    public CTM createCTM() {
        return new CTM();
    }

    /**
     * Create an instance of {@link CTF }
     * 
     */
    public CTF createCTF() {
        return new CTF();
    }

    /**
     * Create an instance of {@link CTTopBot }
     * 
     */
    public CTTopBot createCTTopBot() {
        return new CTTopBot();
    }

    /**
     * Create an instance of {@link CTD }
     * 
     */
    public CTD createCTD() {
        return new CTD();
    }

    /**
     * Create an instance of {@link CTBreakBin }
     * 
     */
    public CTBreakBin createCTBreakBin() {
        return new CTBreakBin();
    }

    /**
     * Create an instance of {@link CTTwipsMeasure }
     * 
     */
    public CTTwipsMeasure createCTTwipsMeasure() {
        return new CTTwipsMeasure();
    }

    /**
     * Create an instance of {@link CTR }
     * 
     */
    public CTR createCTR() {
        return new CTR();
    }

    /**
     * Create an instance of {@link CTBorderBoxPr }
     * 
     */
    public CTBorderBoxPr createCTBorderBoxPr() {
        return new CTBorderBoxPr();
    }

    /**
     * Create an instance of {@link CTLimLoc }
     * 
     */
    public CTLimLoc createCTLimLoc() {
        return new CTLimLoc();
    }

    /**
     * Create an instance of {@link CTUnSignedInteger }
     * 
     */
    public CTUnSignedInteger createCTUnSignedInteger() {
        return new CTUnSignedInteger();
    }

    /**
     * Create an instance of {@link CTInteger255 }
     * 
     */
    public CTInteger255 createCTInteger255() {
        return new CTInteger255();
    }

    /**
     * Create an instance of {@link CTFuncPr }
     * 
     */
    public CTFuncPr createCTFuncPr() {
        return new CTFuncPr();
    }

    /**
     * Create an instance of {@link CTMCS }
     * 
     */
    public CTMCS createCTMCS() {
        return new CTMCS();
    }

    /**
     * Create an instance of {@link CTBar }
     * 
     */
    public CTBar createCTBar() {
        return new CTBar();
    }

    /**
     * Create an instance of {@link CTOnOff }
     * 
     */
    public CTOnOff createCTOnOff() {
        return new CTOnOff();
    }

    /**
     * Create an instance of {@link CTBarPr }
     * 
     */
    public CTBarPr createCTBarPr() {
        return new CTBarPr();
    }

    /**
     * Create an instance of {@link CTFunc }
     * 
     */
    public CTFunc createCTFunc() {
        return new CTFunc();
    }

    /**
     * Create an instance of {@link CTBoxPr }
     * 
     */
    public CTBoxPr createCTBoxPr() {
        return new CTBoxPr();
    }

    /**
     * Create an instance of {@link CTString }
     * 
     */
    public CTString createCTString() {
        return new CTString();
    }

    /**
     * Create an instance of {@link CTBox }
     * 
     */
    public CTBox createCTBox() {
        return new CTBox();
    }

    /**
     * Create an instance of {@link CTOMathArgPr }
     * 
     */
    public CTOMathArgPr createCTOMathArgPr() {
        return new CTOMathArgPr();
    }

    /**
     * Create an instance of {@link CTSPrePr }
     * 
     */
    public CTSPrePr createCTSPrePr() {
        return new CTSPrePr();
    }

    /**
     * Create an instance of {@link CTSSub }
     * 
     */
    public CTSSub createCTSSub() {
        return new CTSSub();
    }

    /**
     * Create an instance of {@link CTFType }
     * 
     */
    public CTFType createCTFType() {
        return new CTFType();
    }

    /**
     * Create an instance of {@link CTEqArrPr }
     * 
     */
    public CTEqArrPr createCTEqArrPr() {
        return new CTEqArrPr();
    }

    /**
     * Create an instance of {@link CTSSupPr }
     * 
     */
    public CTSSupPr createCTSSupPr() {
        return new CTSSupPr();
    }

    /**
     * Create an instance of {@link CTPhant }
     * 
     */
    public CTPhant createCTPhant() {
        return new CTPhant();
    }

    /**
     * Create an instance of {@link CTGroupChrPr }
     * 
     */
    public CTGroupChrPr createCTGroupChrPr() {
        return new CTGroupChrPr();
    }

    /**
     * Create an instance of {@link CTSPre }
     * 
     */
    public CTSPre createCTSPre() {
        return new CTSPre();
    }

    /**
     * Create an instance of {@link CTGroupChr }
     * 
     */
    public CTGroupChr createCTGroupChr() {
        return new CTGroupChr();
    }

    /**
     * Create an instance of {@link CTSSubPr }
     * 
     */
    public CTSSubPr createCTSSubPr() {
        return new CTSSubPr();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTOMath }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", name = "oMath")
    public JAXBElement<CTOMath> createOMath(CTOMath value) {
        return new JAXBElement<CTOMath>(_OMath_QNAME, CTOMath.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTOMathPara }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", name = "oMathPara")
    public JAXBElement<CTOMathPara> createOMathPara(CTOMathPara value) {
        return new JAXBElement<CTOMathPara>(_OMathPara_QNAME, CTOMathPara.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTMathPr }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", name = "mathPr")
    public JAXBElement<CTMathPr> createMathPr(CTMathPr value) {
        return new JAXBElement<CTMathPr>(_MathPr_QNAME, CTMathPr.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTMoveBookmark }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "moveFromRangeStart", scope = CTOMath.class)
    public JAXBElement<CTMoveBookmark> createCTOMathMoveFromRangeStart(CTMoveBookmark value) {
        return new JAXBElement<CTMoveBookmark>(_CTOMathMoveFromRangeStart_QNAME, CTMoveBookmark.class, CTOMath.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTMarkupRange }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "moveFromRangeEnd", scope = CTOMath.class)
    public JAXBElement<CTMarkupRange> createCTOMathMoveFromRangeEnd(CTMarkupRange value) {
        return new JAXBElement<CTMarkupRange>(_CTOMathMoveFromRangeEnd_QNAME, CTMarkupRange.class, CTOMath.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTSPre }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", name = "sPre", scope = CTOMath.class)
    public JAXBElement<CTSPre> createCTOMathSPre(CTSPre value) {
        return new JAXBElement<CTSPre>(_CTOMathSPre_QNAME, CTSPre.class, CTOMath.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTMarkup }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "customXmlDelRangeEnd", scope = CTOMath.class)
    public JAXBElement<CTMarkup> createCTOMathCustomXmlDelRangeEnd(CTMarkup value) {
        return new JAXBElement<CTMarkup>(_CTOMathCustomXmlDelRangeEnd_QNAME, CTMarkup.class, CTOMath.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTMarkupRange }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "moveToRangeEnd", scope = CTOMath.class)
    public JAXBElement<CTMarkupRange> createCTOMathMoveToRangeEnd(CTMarkupRange value) {
        return new JAXBElement<CTMarkupRange>(_CTOMathMoveToRangeEnd_QNAME, CTMarkupRange.class, CTOMath.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTBorderBox }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", name = "borderBox", scope = CTOMath.class)
    public JAXBElement<CTBorderBox> createCTOMathBorderBox(CTBorderBox value) {
        return new JAXBElement<CTBorderBox>(_CTOMathBorderBox_QNAME, CTBorderBox.class, CTOMath.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTMarkup }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "customXmlMoveToRangeEnd", scope = CTOMath.class)
    public JAXBElement<CTMarkup> createCTOMathCustomXmlMoveToRangeEnd(CTMarkup value) {
        return new JAXBElement<CTMarkup>(_CTOMathCustomXmlMoveToRangeEnd_QNAME, CTMarkup.class, CTOMath.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTTrackChange }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "customXmlInsRangeStart", scope = CTOMath.class)
    public JAXBElement<CTTrackChange> createCTOMathCustomXmlInsRangeStart(CTTrackChange value) {
        return new JAXBElement<CTTrackChange>(_CTOMathCustomXmlInsRangeStart_QNAME, CTTrackChange.class, CTOMath.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTTrackChange }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "customXmlMoveToRangeStart", scope = CTOMath.class)
    public JAXBElement<CTTrackChange> createCTOMathCustomXmlMoveToRangeStart(CTTrackChange value) {
        return new JAXBElement<CTTrackChange>(_CTOMathCustomXmlMoveToRangeStart_QNAME, CTTrackChange.class, CTOMath.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTMarkup }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "customXmlInsRangeEnd", scope = CTOMath.class)
    public JAXBElement<CTMarkup> createCTOMathCustomXmlInsRangeEnd(CTMarkup value) {
        return new JAXBElement<CTMarkup>(_CTOMathCustomXmlInsRangeEnd_QNAME, CTMarkup.class, CTOMath.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTGroupChr }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", name = "groupChr", scope = CTOMath.class)
    public JAXBElement<CTGroupChr> createCTOMathGroupChr(CTGroupChr value) {
        return new JAXBElement<CTGroupChr>(_CTOMathGroupChr_QNAME, CTGroupChr.class, CTOMath.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RangePermissionStart }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "permStart", scope = CTOMath.class)
    public JAXBElement<RangePermissionStart> createCTOMathPermStart(RangePermissionStart value) {
        return new JAXBElement<RangePermissionStart>(_CTOMathPermStart_QNAME, RangePermissionStart.class, CTOMath.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTMoveBookmark }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "moveToRangeStart", scope = CTOMath.class)
    public JAXBElement<CTMoveBookmark> createCTOMathMoveToRangeStart(CTMoveBookmark value) {
        return new JAXBElement<CTMoveBookmark>(_CTOMathMoveToRangeStart_QNAME, CTMoveBookmark.class, CTOMath.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTFunc }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", name = "func", scope = CTOMath.class)
    public JAXBElement<CTFunc> createCTOMathFunc(CTFunc value) {
        return new JAXBElement<CTFunc>(_CTOMathFunc_QNAME, CTFunc.class, CTOMath.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTMarkupRange }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "bookmarkEnd", scope = CTOMath.class)
    public JAXBElement<CTMarkupRange> createCTOMathBookmarkEnd(CTMarkupRange value) {
        return new JAXBElement<CTMarkupRange>(_CTOMathBookmarkEnd_QNAME, CTMarkupRange.class, CTOMath.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTSSubSup }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", name = "sSubSup", scope = CTOMath.class)
    public JAXBElement<CTSSubSup> createCTOMathSSubSup(CTSSubSup value) {
        return new JAXBElement<CTSSubSup>(_CTOMathSSubSup_QNAME, CTSSubSup.class, CTOMath.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTMarkup }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "customXmlMoveFromRangeEnd", scope = CTOMath.class)
    public JAXBElement<CTMarkup> createCTOMathCustomXmlMoveFromRangeEnd(CTMarkup value) {
        return new JAXBElement<CTMarkup>(_CTOMathCustomXmlMoveFromRangeEnd_QNAME, CTMarkup.class, CTOMath.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTLimUpp }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", name = "limUpp", scope = CTOMath.class)
    public JAXBElement<CTLimUpp> createCTOMathLimUpp(CTLimUpp value) {
        return new JAXBElement<CTLimUpp>(_CTOMathLimUpp_QNAME, CTLimUpp.class, CTOMath.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTAcc }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", name = "acc", scope = CTOMath.class)
    public JAXBElement<CTAcc> createCTOMathAcc(CTAcc value) {
        return new JAXBElement<CTAcc>(_CTOMathAcc_QNAME, CTAcc.class, CTOMath.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RunTrackChange }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "moveFrom", scope = CTOMath.class)
    public JAXBElement<RunTrackChange> createCTOMathMoveFrom(RunTrackChange value) {
        return new JAXBElement<RunTrackChange>(_CTOMathMoveFrom_QNAME, RunTrackChange.class, CTOMath.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTTrackChange }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "customXmlMoveFromRangeStart", scope = CTOMath.class)
    public JAXBElement<CTTrackChange> createCTOMathCustomXmlMoveFromRangeStart(CTTrackChange value) {
        return new JAXBElement<CTTrackChange>(_CTOMathCustomXmlMoveFromRangeStart_QNAME, CTTrackChange.class, CTOMath.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTM }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", name = "m", scope = CTOMath.class)
    public JAXBElement<CTM> createCTOMathM(CTM value) {
        return new JAXBElement<CTM>(_CTOMathM_QNAME, CTM.class, CTOMath.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTTrackChange }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "customXmlDelRangeStart", scope = CTOMath.class)
    public JAXBElement<CTTrackChange> createCTOMathCustomXmlDelRangeStart(CTTrackChange value) {
        return new JAXBElement<CTTrackChange>(_CTOMathCustomXmlDelRangeStart_QNAME, CTTrackChange.class, CTOMath.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTLimLow }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", name = "limLow", scope = CTOMath.class)
    public JAXBElement<CTLimLow> createCTOMathLimLow(CTLimLow value) {
        return new JAXBElement<CTLimLow>(_CTOMathLimLow_QNAME, CTLimLow.class, CTOMath.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTSSup }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", name = "sSup", scope = CTOMath.class)
    public JAXBElement<CTSSup> createCTOMathSSup(CTSSup value) {
        return new JAXBElement<CTSSup>(_CTOMathSSup_QNAME, CTSSup.class, CTOMath.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTD }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", name = "d", scope = CTOMath.class)
    public JAXBElement<CTD> createCTOMathD(CTD value) {
        return new JAXBElement<CTD>(_CTOMathD_QNAME, CTD.class, CTOMath.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTF }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", name = "f", scope = CTOMath.class)
    public JAXBElement<CTF> createCTOMathF(CTF value) {
        return new JAXBElement<CTF>(_CTOMathF_QNAME, CTF.class, CTOMath.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTEqArr }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", name = "eqArr", scope = CTOMath.class)
    public JAXBElement<CTEqArr> createCTOMathEqArr(CTEqArr value) {
        return new JAXBElement<CTEqArr>(_CTOMathEqArr_QNAME, CTEqArr.class, CTOMath.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTNary }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", name = "nary", scope = CTOMath.class)
    public JAXBElement<CTNary> createCTOMathNary(CTNary value) {
        return new JAXBElement<CTNary>(_CTOMathNary_QNAME, CTNary.class, CTOMath.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTRad }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", name = "rad", scope = CTOMath.class)
    public JAXBElement<CTRad> createCTOMathRad(CTRad value) {
        return new JAXBElement<CTRad>(_CTOMathRad_QNAME, CTRad.class, CTOMath.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTBar }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", name = "bar", scope = CTOMath.class)
    public JAXBElement<CTBar> createCTOMathBar(CTBar value) {
        return new JAXBElement<CTBar>(_CTOMathBar_QNAME, CTBar.class, CTOMath.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPerm }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "permEnd", scope = CTOMath.class)
    public JAXBElement<CTPerm> createCTOMathPermEnd(CTPerm value) {
        return new JAXBElement<CTPerm>(_CTOMathPermEnd_QNAME, CTPerm.class, CTOMath.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTBox }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", name = "box", scope = CTOMath.class)
    public JAXBElement<CTBox> createCTOMathBox(CTBox value) {
        return new JAXBElement<CTBox>(_CTOMathBox_QNAME, CTBox.class, CTOMath.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTR }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", name = "r", scope = CTOMath.class)
    public JAXBElement<CTR> createCTOMathR(CTR value) {
        return new JAXBElement<CTR>(_CTOMathR_QNAME, CTR.class, CTOMath.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTBookmark }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "bookmarkStart", scope = CTOMath.class)
    public JAXBElement<CTBookmark> createCTOMathBookmarkStart(CTBookmark value) {
        return new JAXBElement<CTBookmark>(_CTOMathBookmarkStart_QNAME, CTBookmark.class, CTOMath.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RunTrackChange }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "moveTo", scope = CTOMath.class)
    public JAXBElement<RunTrackChange> createCTOMathMoveTo(RunTrackChange value) {
        return new JAXBElement<RunTrackChange>(_CTOMathMoveTo_QNAME, RunTrackChange.class, CTOMath.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPhant }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", name = "phant", scope = CTOMath.class)
    public JAXBElement<CTPhant> createCTOMathPhant(CTPhant value) {
        return new JAXBElement<CTPhant>(_CTOMathPhant_QNAME, CTPhant.class, CTOMath.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTSSub }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", name = "sSub", scope = CTOMath.class)
    public JAXBElement<CTSSub> createCTOMathSSub(CTSSub value) {
        return new JAXBElement<CTSSub>(_CTOMathSSub_QNAME, CTSSub.class, CTOMath.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTMoveBookmark }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "moveFromRangeStart", scope = CTOMathArg.class)
    public JAXBElement<CTMoveBookmark> createCTOMathArgMoveFromRangeStart(CTMoveBookmark value) {
        return new JAXBElement<CTMoveBookmark>(_CTOMathMoveFromRangeStart_QNAME, CTMoveBookmark.class, CTOMathArg.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTMarkupRange }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "moveFromRangeEnd", scope = CTOMathArg.class)
    public JAXBElement<CTMarkupRange> createCTOMathArgMoveFromRangeEnd(CTMarkupRange value) {
        return new JAXBElement<CTMarkupRange>(_CTOMathMoveFromRangeEnd_QNAME, CTMarkupRange.class, CTOMathArg.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTSPre }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", name = "sPre", scope = CTOMathArg.class)
    public JAXBElement<CTSPre> createCTOMathArgSPre(CTSPre value) {
        return new JAXBElement<CTSPre>(_CTOMathSPre_QNAME, CTSPre.class, CTOMathArg.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTMarkup }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "customXmlDelRangeEnd", scope = CTOMathArg.class)
    public JAXBElement<CTMarkup> createCTOMathArgCustomXmlDelRangeEnd(CTMarkup value) {
        return new JAXBElement<CTMarkup>(_CTOMathCustomXmlDelRangeEnd_QNAME, CTMarkup.class, CTOMathArg.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTMarkupRange }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "moveToRangeEnd", scope = CTOMathArg.class)
    public JAXBElement<CTMarkupRange> createCTOMathArgMoveToRangeEnd(CTMarkupRange value) {
        return new JAXBElement<CTMarkupRange>(_CTOMathMoveToRangeEnd_QNAME, CTMarkupRange.class, CTOMathArg.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTBorderBox }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", name = "borderBox", scope = CTOMathArg.class)
    public JAXBElement<CTBorderBox> createCTOMathArgBorderBox(CTBorderBox value) {
        return new JAXBElement<CTBorderBox>(_CTOMathBorderBox_QNAME, CTBorderBox.class, CTOMathArg.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTMarkup }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "customXmlMoveToRangeEnd", scope = CTOMathArg.class)
    public JAXBElement<CTMarkup> createCTOMathArgCustomXmlMoveToRangeEnd(CTMarkup value) {
        return new JAXBElement<CTMarkup>(_CTOMathCustomXmlMoveToRangeEnd_QNAME, CTMarkup.class, CTOMathArg.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTTrackChange }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "customXmlInsRangeStart", scope = CTOMathArg.class)
    public JAXBElement<CTTrackChange> createCTOMathArgCustomXmlInsRangeStart(CTTrackChange value) {
        return new JAXBElement<CTTrackChange>(_CTOMathCustomXmlInsRangeStart_QNAME, CTTrackChange.class, CTOMathArg.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTTrackChange }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "customXmlMoveToRangeStart", scope = CTOMathArg.class)
    public JAXBElement<CTTrackChange> createCTOMathArgCustomXmlMoveToRangeStart(CTTrackChange value) {
        return new JAXBElement<CTTrackChange>(_CTOMathCustomXmlMoveToRangeStart_QNAME, CTTrackChange.class, CTOMathArg.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTMarkup }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "customXmlInsRangeEnd", scope = CTOMathArg.class)
    public JAXBElement<CTMarkup> createCTOMathArgCustomXmlInsRangeEnd(CTMarkup value) {
        return new JAXBElement<CTMarkup>(_CTOMathCustomXmlInsRangeEnd_QNAME, CTMarkup.class, CTOMathArg.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTGroupChr }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", name = "groupChr", scope = CTOMathArg.class)
    public JAXBElement<CTGroupChr> createCTOMathArgGroupChr(CTGroupChr value) {
        return new JAXBElement<CTGroupChr>(_CTOMathGroupChr_QNAME, CTGroupChr.class, CTOMathArg.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RangePermissionStart }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "permStart", scope = CTOMathArg.class)
    public JAXBElement<RangePermissionStart> createCTOMathArgPermStart(RangePermissionStart value) {
        return new JAXBElement<RangePermissionStart>(_CTOMathPermStart_QNAME, RangePermissionStart.class, CTOMathArg.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTMoveBookmark }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "moveToRangeStart", scope = CTOMathArg.class)
    public JAXBElement<CTMoveBookmark> createCTOMathArgMoveToRangeStart(CTMoveBookmark value) {
        return new JAXBElement<CTMoveBookmark>(_CTOMathMoveToRangeStart_QNAME, CTMoveBookmark.class, CTOMathArg.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTFunc }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", name = "func", scope = CTOMathArg.class)
    public JAXBElement<CTFunc> createCTOMathArgFunc(CTFunc value) {
        return new JAXBElement<CTFunc>(_CTOMathFunc_QNAME, CTFunc.class, CTOMathArg.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTMarkupRange }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "bookmarkEnd", scope = CTOMathArg.class)
    public JAXBElement<CTMarkupRange> createCTOMathArgBookmarkEnd(CTMarkupRange value) {
        return new JAXBElement<CTMarkupRange>(_CTOMathBookmarkEnd_QNAME, CTMarkupRange.class, CTOMathArg.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTSSubSup }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", name = "sSubSup", scope = CTOMathArg.class)
    public JAXBElement<CTSSubSup> createCTOMathArgSSubSup(CTSSubSup value) {
        return new JAXBElement<CTSSubSup>(_CTOMathSSubSup_QNAME, CTSSubSup.class, CTOMathArg.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTMarkup }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "customXmlMoveFromRangeEnd", scope = CTOMathArg.class)
    public JAXBElement<CTMarkup> createCTOMathArgCustomXmlMoveFromRangeEnd(CTMarkup value) {
        return new JAXBElement<CTMarkup>(_CTOMathCustomXmlMoveFromRangeEnd_QNAME, CTMarkup.class, CTOMathArg.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTLimUpp }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", name = "limUpp", scope = CTOMathArg.class)
    public JAXBElement<CTLimUpp> createCTOMathArgLimUpp(CTLimUpp value) {
        return new JAXBElement<CTLimUpp>(_CTOMathLimUpp_QNAME, CTLimUpp.class, CTOMathArg.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTAcc }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", name = "acc", scope = CTOMathArg.class)
    public JAXBElement<CTAcc> createCTOMathArgAcc(CTAcc value) {
        return new JAXBElement<CTAcc>(_CTOMathAcc_QNAME, CTAcc.class, CTOMathArg.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RunTrackChange }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "moveFrom", scope = CTOMathArg.class)
    public JAXBElement<RunTrackChange> createCTOMathArgMoveFrom(RunTrackChange value) {
        return new JAXBElement<RunTrackChange>(_CTOMathMoveFrom_QNAME, RunTrackChange.class, CTOMathArg.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTTrackChange }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "customXmlMoveFromRangeStart", scope = CTOMathArg.class)
    public JAXBElement<CTTrackChange> createCTOMathArgCustomXmlMoveFromRangeStart(CTTrackChange value) {
        return new JAXBElement<CTTrackChange>(_CTOMathCustomXmlMoveFromRangeStart_QNAME, CTTrackChange.class, CTOMathArg.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTM }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", name = "m", scope = CTOMathArg.class)
    public JAXBElement<CTM> createCTOMathArgM(CTM value) {
        return new JAXBElement<CTM>(_CTOMathM_QNAME, CTM.class, CTOMathArg.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTTrackChange }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "customXmlDelRangeStart", scope = CTOMathArg.class)
    public JAXBElement<CTTrackChange> createCTOMathArgCustomXmlDelRangeStart(CTTrackChange value) {
        return new JAXBElement<CTTrackChange>(_CTOMathCustomXmlDelRangeStart_QNAME, CTTrackChange.class, CTOMathArg.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTLimLow }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", name = "limLow", scope = CTOMathArg.class)
    public JAXBElement<CTLimLow> createCTOMathArgLimLow(CTLimLow value) {
        return new JAXBElement<CTLimLow>(_CTOMathLimLow_QNAME, CTLimLow.class, CTOMathArg.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTSSup }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", name = "sSup", scope = CTOMathArg.class)
    public JAXBElement<CTSSup> createCTOMathArgSSup(CTSSup value) {
        return new JAXBElement<CTSSup>(_CTOMathSSup_QNAME, CTSSup.class, CTOMathArg.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTD }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", name = "d", scope = CTOMathArg.class)
    public JAXBElement<CTD> createCTOMathArgD(CTD value) {
        return new JAXBElement<CTD>(_CTOMathD_QNAME, CTD.class, CTOMathArg.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTF }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", name = "f", scope = CTOMathArg.class)
    public JAXBElement<CTF> createCTOMathArgF(CTF value) {
        return new JAXBElement<CTF>(_CTOMathF_QNAME, CTF.class, CTOMathArg.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTEqArr }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", name = "eqArr", scope = CTOMathArg.class)
    public JAXBElement<CTEqArr> createCTOMathArgEqArr(CTEqArr value) {
        return new JAXBElement<CTEqArr>(_CTOMathEqArr_QNAME, CTEqArr.class, CTOMathArg.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTNary }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", name = "nary", scope = CTOMathArg.class)
    public JAXBElement<CTNary> createCTOMathArgNary(CTNary value) {
        return new JAXBElement<CTNary>(_CTOMathNary_QNAME, CTNary.class, CTOMathArg.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTRad }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", name = "rad", scope = CTOMathArg.class)
    public JAXBElement<CTRad> createCTOMathArgRad(CTRad value) {
        return new JAXBElement<CTRad>(_CTOMathRad_QNAME, CTRad.class, CTOMathArg.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTBar }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", name = "bar", scope = CTOMathArg.class)
    public JAXBElement<CTBar> createCTOMathArgBar(CTBar value) {
        return new JAXBElement<CTBar>(_CTOMathBar_QNAME, CTBar.class, CTOMathArg.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPerm }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "permEnd", scope = CTOMathArg.class)
    public JAXBElement<CTPerm> createCTOMathArgPermEnd(CTPerm value) {
        return new JAXBElement<CTPerm>(_CTOMathPermEnd_QNAME, CTPerm.class, CTOMathArg.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTBox }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", name = "box", scope = CTOMathArg.class)
    public JAXBElement<CTBox> createCTOMathArgBox(CTBox value) {
        return new JAXBElement<CTBox>(_CTOMathBox_QNAME, CTBox.class, CTOMathArg.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTR }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", name = "r", scope = CTOMathArg.class)
    public JAXBElement<CTR> createCTOMathArgR(CTR value) {
        return new JAXBElement<CTR>(_CTOMathR_QNAME, CTR.class, CTOMathArg.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTBookmark }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "bookmarkStart", scope = CTOMathArg.class)
    public JAXBElement<CTBookmark> createCTOMathArgBookmarkStart(CTBookmark value) {
        return new JAXBElement<CTBookmark>(_CTOMathBookmarkStart_QNAME, CTBookmark.class, CTOMathArg.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RunTrackChange }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "moveTo", scope = CTOMathArg.class)
    public JAXBElement<RunTrackChange> createCTOMathArgMoveTo(RunTrackChange value) {
        return new JAXBElement<RunTrackChange>(_CTOMathMoveTo_QNAME, RunTrackChange.class, CTOMathArg.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPhant }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", name = "phant", scope = CTOMathArg.class)
    public JAXBElement<CTPhant> createCTOMathArgPhant(CTPhant value) {
        return new JAXBElement<CTPhant>(_CTOMathPhant_QNAME, CTPhant.class, CTOMathArg.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTSSub }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", name = "sSub", scope = CTOMathArg.class)
    public JAXBElement<CTSSub> createCTOMathArgSSub(CTSSub value) {
        return new JAXBElement<CTSSub>(_CTOMathSSub_QNAME, CTSSub.class, CTOMathArg.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link org.docx4j.wml.R.MonthShort }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "monthShort", scope = CTR.class)
    public JAXBElement<org.docx4j.wml.R.MonthShort> createCTRMonthShort(org.docx4j.wml.R.MonthShort value) {
        return new JAXBElement<org.docx4j.wml.R.MonthShort>(_CTRMonthShort_QNAME, org.docx4j.wml.R.MonthShort.class, CTR.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link org.docx4j.wml.R.YearLong }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "yearLong", scope = CTR.class)
    public JAXBElement<org.docx4j.wml.R.YearLong> createCTRYearLong(org.docx4j.wml.R.YearLong value) {
        return new JAXBElement<org.docx4j.wml.R.YearLong>(_CTRYearLong_QNAME, org.docx4j.wml.R.YearLong.class, CTR.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTFtnEdnRef }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "footnoteReference", scope = CTR.class)
    public JAXBElement<CTFtnEdnRef> createCTRFootnoteReference(CTFtnEdnRef value) {
        return new JAXBElement<CTFtnEdnRef>(_CTRFootnoteReference_QNAME, CTFtnEdnRef.class, CTR.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link org.docx4j.wml.R.PgNum }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "pgNum", scope = CTR.class)
    public JAXBElement<org.docx4j.wml.R.PgNum> createCTRPgNum(org.docx4j.wml.R.PgNum value) {
        return new JAXBElement<org.docx4j.wml.R.PgNum>(_CTRPgNum_QNAME, org.docx4j.wml.R.PgNum.class, CTR.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link org.docx4j.wml.R.EndnoteRef }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "endnoteRef", scope = CTR.class)
    public JAXBElement<org.docx4j.wml.R.EndnoteRef> createCTREndnoteRef(org.docx4j.wml.R.EndnoteRef value) {
        return new JAXBElement<org.docx4j.wml.R.EndnoteRef>(_CTREndnoteRef_QNAME, org.docx4j.wml.R.EndnoteRef.class, CTR.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link org.docx4j.wml.R.SoftHyphen }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "softHyphen", scope = CTR.class)
    public JAXBElement<org.docx4j.wml.R.SoftHyphen> createCTRSoftHyphen(org.docx4j.wml.R.SoftHyphen value) {
        return new JAXBElement<org.docx4j.wml.R.SoftHyphen>(_CTRSoftHyphen_QNAME, org.docx4j.wml.R.SoftHyphen.class, CTR.class, value);
    }

    /**
     * Create an instance of {@link CTR.RPrMath }}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", name = "rPr", scope = CTR.class)
    public CTR.RPrMath createCTRRPrMath(CTRPR value) {
        return new CTR.RPrMath(value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTFtnEdnRef }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "endnoteReference", scope = CTR.class)
    public JAXBElement<CTFtnEdnRef> createCTREndnoteReference(CTFtnEdnRef value) {
        return new JAXBElement<CTFtnEdnRef>(_CTREndnoteReference_QNAME, CTFtnEdnRef.class, CTR.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Text }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "t", scope = CTR.class)
    public JAXBElement<Text> createCTRT(Text value) {
        return new JAXBElement<Text>(_CTRT_QNAME, Text.class, CTR.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link org.docx4j.wml.R.Cr }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "cr", scope = CTR.class)
    public JAXBElement<org.docx4j.wml.R.Cr> createCTRCr(org.docx4j.wml.R.Cr value) {
        return new JAXBElement<org.docx4j.wml.R.Cr>(_CTRCr_QNAME, org.docx4j.wml.R.Cr.class, CTR.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FldChar }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "fldChar", scope = CTR.class)
    public JAXBElement<FldChar> createCTRFldChar(FldChar value) {
        return new JAXBElement<FldChar>(_CTRFldChar_QNAME, FldChar.class, CTR.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link org.docx4j.wml.R.CommentReference }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "commentReference", scope = CTR.class)
    public JAXBElement<org.docx4j.wml.R.CommentReference> createCTRCommentReference(org.docx4j.wml.R.CommentReference value) {
        return new JAXBElement<org.docx4j.wml.R.CommentReference>(_CTRCommentReference_QNAME, org.docx4j.wml.R.CommentReference.class, CTR.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link org.docx4j.wml.R.Separator }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "separator", scope = CTR.class)
    public JAXBElement<org.docx4j.wml.R.Separator> createCTRSeparator(org.docx4j.wml.R.Separator value) {
        return new JAXBElement<org.docx4j.wml.R.Separator>(_CTRSeparator_QNAME, org.docx4j.wml.R.Separator.class, CTR.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RPr }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "rPr", scope = CTR.class)
    public JAXBElement<RPr> createCTRRPr(RPr value) {
        return new JAXBElement<RPr>(_CTRRPr_QNAME, RPr.class, CTR.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link org.docx4j.wml.R.DayLong }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "dayLong", scope = CTR.class)
    public JAXBElement<org.docx4j.wml.R.DayLong> createCTRDayLong(org.docx4j.wml.R.DayLong value) {
        return new JAXBElement<org.docx4j.wml.R.DayLong>(_CTRDayLong_QNAME, org.docx4j.wml.R.DayLong.class, CTR.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link org.docx4j.wml.R.AnnotationRef }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "annotationRef", scope = CTR.class)
    public JAXBElement<org.docx4j.wml.R.AnnotationRef> createCTRAnnotationRef(org.docx4j.wml.R.AnnotationRef value) {
        return new JAXBElement<org.docx4j.wml.R.AnnotationRef>(_CTRAnnotationRef_QNAME, org.docx4j.wml.R.AnnotationRef.class, CTR.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTRuby }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "ruby", scope = CTR.class)
    public JAXBElement<CTRuby> createCTRRuby(CTRuby value) {
        return new JAXBElement<CTRuby>(_CTRRuby_QNAME, CTRuby.class, CTR.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTObject }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "object", scope = CTR.class)
    public JAXBElement<CTObject> createCTRObject(CTObject value) {
        return new JAXBElement<CTObject>(_CTRObject_QNAME, CTObject.class, CTR.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link org.docx4j.wml.R.Tab }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "tab", scope = CTR.class)
    public JAXBElement<org.docx4j.wml.R.Tab> createCTRTab(org.docx4j.wml.R.Tab value) {
        return new JAXBElement<org.docx4j.wml.R.Tab>(_CTRTab_QNAME, org.docx4j.wml.R.Tab.class, CTR.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link org.docx4j.wml.R.LastRenderedPageBreak }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "lastRenderedPageBreak", scope = CTR.class)
    public JAXBElement<org.docx4j.wml.R.LastRenderedPageBreak> createCTRLastRenderedPageBreak(org.docx4j.wml.R.LastRenderedPageBreak value) {
        return new JAXBElement<org.docx4j.wml.R.LastRenderedPageBreak>(_CTRLastRenderedPageBreak_QNAME, org.docx4j.wml.R.LastRenderedPageBreak.class, CTR.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Drawing }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "drawing", scope = CTR.class)
    public JAXBElement<Drawing> createCTRDrawing(Drawing value) {
        return new JAXBElement<Drawing>(_CTRDrawing_QNAME, Drawing.class, CTR.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Text }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "delInstrText", scope = CTR.class)
    public JAXBElement<Text> createCTRDelInstrText(Text value) {
        return new JAXBElement<Text>(_CTRDelInstrText_QNAME, Text.class, CTR.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Text }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "instrText", scope = CTR.class)
    public JAXBElement<Text> createCTRInstrText(Text value) {
        return new JAXBElement<Text>(_CTRInstrText_QNAME, Text.class, CTR.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link org.docx4j.wml.R.Sym }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "sym", scope = CTR.class)
    public JAXBElement<org.docx4j.wml.R.Sym> createCTRSym(org.docx4j.wml.R.Sym value) {
        return new JAXBElement<org.docx4j.wml.R.Sym>(_CTRSym_QNAME, org.docx4j.wml.R.Sym.class, CTR.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Pict }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "pict", scope = CTR.class)
    public JAXBElement<Pict> createCTRPict(Pict value) {
        return new JAXBElement<Pict>(_CTRPict_QNAME, Pict.class, CTR.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link org.docx4j.wml.R.ContinuationSeparator }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "continuationSeparator", scope = CTR.class)
    public JAXBElement<org.docx4j.wml.R.ContinuationSeparator> createCTRContinuationSeparator(org.docx4j.wml.R.ContinuationSeparator value) {
        return new JAXBElement<org.docx4j.wml.R.ContinuationSeparator>(_CTRContinuationSeparator_QNAME, org.docx4j.wml.R.ContinuationSeparator.class, CTR.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link org.docx4j.wml.R.YearShort }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "yearShort", scope = CTR.class)
    public JAXBElement<org.docx4j.wml.R.YearShort> createCTRYearShort(org.docx4j.wml.R.YearShort value) {
        return new JAXBElement<org.docx4j.wml.R.YearShort>(_CTRYearShort_QNAME, org.docx4j.wml.R.YearShort.class, CTR.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link org.docx4j.wml.R.FootnoteRef }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "footnoteRef", scope = CTR.class)
    public JAXBElement<org.docx4j.wml.R.FootnoteRef> createCTRFootnoteRef(org.docx4j.wml.R.FootnoteRef value) {
        return new JAXBElement<org.docx4j.wml.R.FootnoteRef>(_CTRFootnoteRef_QNAME, org.docx4j.wml.R.FootnoteRef.class, CTR.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link org.docx4j.wml.R.DayShort }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "dayShort", scope = CTR.class)
    public JAXBElement<org.docx4j.wml.R.DayShort> createCTRDayShort(org.docx4j.wml.R.DayShort value) {
        return new JAXBElement<org.docx4j.wml.R.DayShort>(_CTRDayShort_QNAME, org.docx4j.wml.R.DayShort.class, CTR.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link org.docx4j.wml.R.MonthLong }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "monthLong", scope = CTR.class)
    public JAXBElement<org.docx4j.wml.R.MonthLong> createCTRMonthLong(org.docx4j.wml.R.MonthLong value) {
        return new JAXBElement<org.docx4j.wml.R.MonthLong>(_CTRMonthLong_QNAME, org.docx4j.wml.R.MonthLong.class, CTR.class, value);
    }

    /**
     * Create an instance of {@link CTR.TMath }}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", name = "t", scope = CTR.class)
    public CTR.TMath createCTRTMath(CTText value) {
        return new CTR.TMath(value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link org.docx4j.wml.R.Ptab }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "ptab", scope = CTR.class)
    public JAXBElement<org.docx4j.wml.R.Ptab> createCTRPtab(org.docx4j.wml.R.Ptab value) {
        return new JAXBElement<org.docx4j.wml.R.Ptab>(_CTRPtab_QNAME, org.docx4j.wml.R.Ptab.class, CTR.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link org.docx4j.wml.R.NoBreakHyphen }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "noBreakHyphen", scope = CTR.class)
    public JAXBElement<org.docx4j.wml.R.NoBreakHyphen> createCTRNoBreakHyphen(org.docx4j.wml.R.NoBreakHyphen value) {
        return new JAXBElement<org.docx4j.wml.R.NoBreakHyphen>(_CTRNoBreakHyphen_QNAME, org.docx4j.wml.R.NoBreakHyphen.class, CTR.class, value);
    }

}
