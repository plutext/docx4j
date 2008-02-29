/*
 *  Copyright 2007, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is free software: you can use it, redistribute it and/or modify
    it under the terms of version 3 of the GNU Affero General Public License 
    as published by the Free Software Foundation.

    docx4j is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License   
    along with docx4j.  If not, see <http://www.fsf.org/licensing/licenses/>.
    
 */

package org.docx4j.wml;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.wml package. 
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

    private final static QName _TblPr_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "tblPr");
    private final static QName _Body_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "body");
    private final static QName _TblPrEx_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "tblPrEx");
    private final static QName _Tbl_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "tbl");
    private final static QName _Ins_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "ins");
    private final static QName _DelText_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "delText");
    private final static QName _SectPr_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "sectPr");
    private final static QName _VAlign_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "vAlign");
    private final static QName _Tr_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "tr");
    private final static QName _TrPr_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "trPr");
    private final static QName _NoWrap_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "noWrap");
    private final static QName _TblBorders_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "tblBorders");
    private final static QName _InstrText_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "instrText");
    private final static QName _DelInstrText_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "delInstrText");
    private final static QName _TcBorders_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "tcBorders");
    private final static QName _T_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "t");
    private final static QName _Document_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "document");
    private final static QName _P_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "p");
    private final static QName _Tc_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "tc");
    private final static QName _Del_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "del");
    private final static QName _TcPr_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "tcPr");
    private final static QName _SdtPr_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "sdtPr");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.wml
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link DayLong }
     * 
     */
    public DayLong createDayLong() {
        return new DayLong();
    }

    /**
     * Create an instance of {@link Styles.DocDefaults }
     * 
     */
    public Styles.DocDefaults createStylesDocDefaults() {
        return new Styles.DocDefaults();
    }

    /**
     * Create an instance of {@link CTVerticalJc }
     * 
     */
    public CTVerticalJc createCTVerticalJc() {
        return new CTVerticalJc();
    }

    /**
     * Create an instance of {@link Color }
     * 
     */
    public Color createColor() {
        return new Color();
    }

    /**
     * Create an instance of {@link Underline }
     * 
     */
    public Underline createUnderline() {
        return new Underline();
    }

    /**
     * Create an instance of {@link TblPrEx }
     * 
     */
    public TblPrEx createTblPrEx() {
        return new TblPrEx();
    }

    /**
     * Create an instance of {@link PPr.OutlineLvl }
     * 
     */
    public PPr.OutlineLvl createPPrOutlineLvl() {
        return new PPr.OutlineLvl();
    }

    /**
     * Create an instance of {@link Numbering.Num.LvlOverride }
     * 
     */
    public Numbering.Num.LvlOverride createNumberingNumLvlOverride() {
        return new Numbering.Num.LvlOverride();
    }

    /**
     * Create an instance of {@link Row }
     * 
     */
    public Row createRow() {
        return new Row();
    }

    /**
     * Create an instance of {@link EndnoteRef }
     * 
     */
    public EndnoteRef createEndnoteRef() {
        return new EndnoteRef();
    }

    /**
     * Create an instance of {@link Sym }
     * 
     */
    public Sym createSym() {
        return new Sym();
    }

    /**
     * Create an instance of {@link Ptab }
     * 
     */
    public Ptab createPtab() {
        return new Ptab();
    }

    /**
     * Create an instance of {@link Numbering.AbstractNum }
     * 
     */
    public Numbering.AbstractNum createNumberingAbstractNum() {
        return new Numbering.AbstractNum();
    }

    /**
     * Create an instance of {@link Lvl }
     * 
     */
    public Lvl createLvl() {
        return new Lvl();
    }

    /**
     * Create an instance of {@link Numbering.AbstractNum.NumStyleLink }
     * 
     */
    public Numbering.AbstractNum.NumStyleLink createNumberingAbstractNumNumStyleLink() {
        return new Numbering.AbstractNum.NumStyleLink();
    }

    /**
     * Create an instance of {@link TblPrBase }
     * 
     */
    public TblPrBase createTblPrBase() {
        return new TblPrBase();
    }

    /**
     * Create an instance of {@link Styles.LatentStyles.LsdException }
     * 
     */
    public Styles.LatentStyles.LsdException createStylesLatentStylesLsdException() {
        return new Styles.LatentStyles.LsdException();
    }

    /**
     * Create an instance of {@link Bottom }
     * 
     */
    public Bottom createBottom() {
        return new Bottom();
    }

    /**
     * Create an instance of {@link Document }
     * 
     */
    public Document createDocument() {
        return new Document();
    }

    /**
     * Create an instance of {@link Numbering }
     * 
     */
    public Numbering createNumbering() {
        return new Numbering();
    }

    /**
     * Create an instance of {@link SdtPr }
     * 
     */
    public SdtPr createSdtPr() {
        return new SdtPr();
    }

    /**
     * Create an instance of {@link FontPanose }
     * 
     */
    public FontPanose createFontPanose() {
        return new FontPanose();
    }

    /**
     * Create an instance of {@link Highlight }
     * 
     */
    public Highlight createHighlight() {
        return new Highlight();
    }

    /**
     * Create an instance of {@link PPr.NumPr }
     * 
     */
    public PPr.NumPr createPPrNumPr() {
        return new PPr.NumPr();
    }

    /**
     * Create an instance of {@link PgNum }
     * 
     */
    public PgNum createPgNum() {
        return new PgNum();
    }

    /**
     * Create an instance of {@link Styles.Style.BasedOn }
     * 
     */
    public Styles.Style.BasedOn createStylesStyleBasedOn() {
        return new Styles.Style.BasedOn();
    }

    /**
     * Create an instance of {@link Numbering.NumPicBullet }
     * 
     */
    public Numbering.NumPicBullet createNumberingNumPicBullet() {
        return new Numbering.NumPicBullet();
    }

    /**
     * Create an instance of {@link Fonts.Font.AltName }
     * 
     */
    public Fonts.Font.AltName createFontsFontAltName() {
        return new Fonts.Font.AltName();
    }

    /**
     * Create an instance of {@link PPr.NumPr.Ilvl }
     * 
     */
    public PPr.NumPr.Ilvl createPPrNumPrIlvl() {
        return new PPr.NumPr.Ilvl();
    }

    /**
     * Create an instance of {@link TblPrExBase }
     * 
     */
    public TblPrExBase createTblPrExBase() {
        return new TblPrExBase();
    }

    /**
     * Create an instance of {@link Lvl.Legacy }
     * 
     */
    public Lvl.Legacy createLvlLegacy() {
        return new Lvl.Legacy();
    }

    /**
     * Create an instance of {@link MonthLong }
     * 
     */
    public MonthLong createMonthLong() {
        return new MonthLong();
    }

    /**
     * Create an instance of {@link HMerge }
     * 
     */
    public HMerge createHMerge() {
        return new HMerge();
    }

    /**
     * Create an instance of {@link PageSz }
     * 
     */
    public PageSz createPageSz() {
        return new PageSz();
    }

    /**
     * Create an instance of {@link Sdt }
     * 
     */
    public Sdt createSdt() {
        return new Sdt();
    }

    /**
     * Create an instance of {@link Lvl.LvlPicBulletId }
     * 
     */
    public Lvl.LvlPicBulletId createLvlLvlPicBulletId() {
        return new Lvl.LvlPicBulletId();
    }

    /**
     * Create an instance of {@link CTDecimalNumber }
     * 
     */
    public CTDecimalNumber createCTDecimalNumber() {
        return new CTDecimalNumber();
    }

    /**
     * Create an instance of {@link Ind }
     * 
     */
    public Ind createInd() {
        return new Ind();
    }

    /**
     * Create an instance of {@link Lvl.LvlRestart }
     * 
     */
    public Lvl.LvlRestart createLvlLvlRestart() {
        return new Lvl.LvlRestart();
    }

    /**
     * Create an instance of {@link Numbering.NumIdMacAtCleanup }
     * 
     */
    public Numbering.NumIdMacAtCleanup createNumberingNumIdMacAtCleanup() {
        return new Numbering.NumIdMacAtCleanup();
    }

    /**
     * Create an instance of {@link Styles }
     * 
     */
    public Styles createStyles() {
        return new Styles();
    }

    /**
     * Create an instance of {@link Numbering.Num }
     * 
     */
    public Numbering.Num createNumberingNum() {
        return new Numbering.Num();
    }

    /**
     * Create an instance of {@link Styles.Style.Link }
     * 
     */
    public Styles.Style.Link createStylesStyleLink() {
        return new Styles.Style.Link();
    }

    /**
     * Create an instance of {@link R }
     * 
     */
    public R createR() {
        return new R();
    }

    /**
     * Create an instance of {@link Numbering.AbstractNum.StyleLink }
     * 
     */
    public Numbering.AbstractNum.StyleLink createNumberingAbstractNumStyleLink() {
        return new Numbering.AbstractNum.StyleLink();
    }

    /**
     * Create an instance of {@link Styles.Style.Name }
     * 
     */
    public Styles.Style.Name createStylesStyleName() {
        return new Styles.Style.Name();
    }

    /**
     * Create an instance of {@link HpsMeasure }
     * 
     */
    public HpsMeasure createHpsMeasure() {
        return new HpsMeasure();
    }

    /**
     * Create an instance of {@link TcPrBase }
     * 
     */
    public TcPrBase createTcPrBase() {
        return new TcPrBase();
    }

    /**
     * Create an instance of {@link Tabs }
     * 
     */
    public Tabs createTabs() {
        return new Tabs();
    }

    /**
     * Create an instance of {@link Lvl.LvlJc }
     * 
     */
    public Lvl.LvlJc createLvlLvlJc() {
        return new Lvl.LvlJc();
    }

    /**
     * Create an instance of {@link Styles.DocDefaults.RPrDefault }
     * 
     */
    public Styles.DocDefaults.RPrDefault createStylesDocDefaultsRPrDefault() {
        return new Styles.DocDefaults.RPrDefault();
    }

    /**
     * Create an instance of {@link VerticalAlignRun }
     * 
     */
    public VerticalAlignRun createVerticalAlignRun() {
        return new VerticalAlignRun();
    }

    /**
     * Create an instance of {@link Left }
     * 
     */
    public Left createLeft() {
        return new Left();
    }

    /**
     * Create an instance of {@link Lock }
     * 
     */
    public Lock createLock() {
        return new Lock();
    }

    /**
     * Create an instance of {@link Shd }
     * 
     */
    public Shd createShd() {
        return new Shd();
    }

    /**
     * Create an instance of {@link Lvl.Start }
     * 
     */
    public Lvl.Start createLvlStart() {
        return new Lvl.Start();
    }

    /**
     * Create an instance of {@link Lvl.NumFmt }
     * 
     */
    public Lvl.NumFmt createLvlNumFmt() {
        return new Lvl.NumFmt();
    }

    /**
     * Create an instance of {@link SdtPr.Tag }
     * 
     */
    public SdtPr.Tag createSdtPrTag() {
        return new SdtPr.Tag();
    }

    /**
     * Create an instance of {@link RunTrackChange }
     * 
     */
    public RunTrackChange createRunTrackChange() {
        return new RunTrackChange();
    }

    /**
     * Create an instance of {@link PPr.PStyle }
     * 
     */
    public PPr.PStyle createPPrPStyle() {
        return new PPr.PStyle();
    }

    /**
     * Create an instance of {@link MonthShort }
     * 
     */
    public MonthShort createMonthShort() {
        return new MonthShort();
    }

    /**
     * Create an instance of {@link Tr2Bl }
     * 
     */
    public Tr2Bl createTr2Bl() {
        return new Tr2Bl();
    }

    /**
     * Create an instance of {@link Cr }
     * 
     */
    public Cr createCr() {
        return new Cr();
    }

    /**
     * Create an instance of {@link YearLong }
     * 
     */
    public YearLong createYearLong() {
        return new YearLong();
    }

    /**
     * Create an instance of {@link Top }
     * 
     */
    public Top createTop() {
        return new Top();
    }

    /**
     * Create an instance of {@link SdtPr.Alias }
     * 
     */
    public SdtPr.Alias createSdtPrAlias() {
        return new SdtPr.Alias();
    }

    /**
     * Create an instance of {@link Styles.Style.Next }
     * 
     */
    public Styles.Style.Next createStylesStyleNext() {
        return new Styles.Style.Next();
    }

    /**
     * Create an instance of {@link SoftHyphen }
     * 
     */
    public SoftHyphen createSoftHyphen() {
        return new SoftHyphen();
    }

    /**
     * Create an instance of {@link Numbering.Num.AbstractNumId }
     * 
     */
    public Numbering.Num.AbstractNumId createNumberingNumAbstractNumId() {
        return new Numbering.Num.AbstractNumId();
    }

    /**
     * Create an instance of {@link TcPrInner }
     * 
     */
    public TcPrInner createTcPrInner() {
        return new TcPrInner();
    }

    /**
     * Create an instance of {@link InsideV }
     * 
     */
    public InsideV createInsideV() {
        return new InsideV();
    }

    /**
     * Create an instance of {@link TblW }
     * 
     */
    public TblW createTblW() {
        return new TblW();
    }

    /**
     * Create an instance of {@link LastRenderedPageBreak }
     * 
     */
    public LastRenderedPageBreak createLastRenderedPageBreak() {
        return new LastRenderedPageBreak();
    }

    /**
     * Create an instance of {@link Right }
     * 
     */
    public Right createRight() {
        return new Right();
    }

    /**
     * Create an instance of {@link Lvl.PStyle }
     * 
     */
    public Lvl.PStyle createLvlPStyle() {
        return new Lvl.PStyle();
    }

    /**
     * Create an instance of {@link TblBorders }
     * 
     */
    public TblBorders createTblBorders() {
        return new TblBorders();
    }

    /**
     * Create an instance of {@link FootnoteRef }
     * 
     */
    public FootnoteRef createFootnoteRef() {
        return new FootnoteRef();
    }

    /**
     * Create an instance of {@link InsideH }
     * 
     */
    public InsideH createInsideH() {
        return new InsideH();
    }

    /**
     * Create an instance of {@link SectPr }
     * 
     */
    public SectPr createSectPr() {
        return new SectPr();
    }

    /**
     * Create an instance of {@link Styles.LatentStyles }
     * 
     */
    public Styles.LatentStyles createStylesLatentStyles() {
        return new Styles.LatentStyles();
    }

    /**
     * Create an instance of {@link FontSig }
     * 
     */
    public FontSig createFontSig() {
        return new FontSig();
    }

    /**
     * Create an instance of {@link ContinuationSeparator }
     * 
     */
    public ContinuationSeparator createContinuationSeparator() {
        return new ContinuationSeparator();
    }

    /**
     * Create an instance of {@link FontRel }
     * 
     */
    public FontRel createFontRel() {
        return new FontRel();
    }

    /**
     * Create an instance of {@link Fonts }
     * 
     */
    public Fonts createFonts() {
        return new Fonts();
    }

    /**
     * Create an instance of {@link NoBreakHyphen }
     * 
     */
    public NoBreakHyphen createNoBreakHyphen() {
        return new NoBreakHyphen();
    }

    /**
     * Create an instance of {@link CTRel }
     * 
     */
    public CTRel createCTRel() {
        return new CTRel();
    }

    /**
     * Create an instance of {@link SectType }
     * 
     */
    public SectType createSectType() {
        return new SectType();
    }

    /**
     * Create an instance of {@link FontPitch }
     * 
     */
    public FontPitch createFontPitch() {
        return new FontPitch();
    }

    /**
     * Create an instance of {@link TrPrBase }
     * 
     */
    public TrPrBase createTrPrBase() {
        return new TrPrBase();
    }

    /**
     * Create an instance of {@link TcPr }
     * 
     */
    public TcPr createTcPr() {
        return new TcPr();
    }

    /**
     * Create an instance of {@link Tbl }
     * 
     */
    public Tbl createTbl() {
        return new Tbl();
    }

    /**
     * Create an instance of {@link Body }
     * 
     */
    public Body createBody() {
        return new Body();
    }

    /**
     * Create an instance of {@link BooleanDefaultTrue }
     * 
     */
    public BooleanDefaultTrue createBooleanDefaultTrue() {
        return new BooleanDefaultTrue();
    }

    /**
     * Create an instance of {@link FontFamily }
     * 
     */
    public FontFamily createFontFamily() {
        return new FontFamily();
    }

    /**
     * Create an instance of {@link PageMar }
     * 
     */
    public PageMar createPageMar() {
        return new PageMar();
    }

    /**
     * Create an instance of {@link VMerge }
     * 
     */
    public VMerge createVMerge() {
        return new VMerge();
    }

    /**
     * Create an instance of {@link WordDocument }
     * 
     */
    public WordDocument createWordDocument() {
        return new WordDocument();
    }

    /**
     * Create an instance of {@link RPr.RFonts }
     * 
     */
    public RPr.RFonts createRPrRFonts() {
        return new RPr.RFonts();
    }

    /**
     * Create an instance of {@link PageNumber }
     * 
     */
    public PageNumber createPageNumber() {
        return new PageNumber();
    }

    /**
     * Create an instance of {@link CTUcharHexNumber }
     * 
     */
    public CTUcharHexNumber createCTUcharHexNumber() {
        return new CTUcharHexNumber();
    }

    /**
     * Create an instance of {@link Tab }
     * 
     */
    public Tab createTab() {
        return new Tab();
    }

    /**
     * Create an instance of {@link Separator }
     * 
     */
    public Separator createSeparator() {
        return new Separator();
    }

    /**
     * Create an instance of {@link RPr }
     * 
     */
    public RPr createRPr() {
        return new RPr();
    }

    /**
     * Create an instance of {@link PPr.NumPr.NumId }
     * 
     */
    public PPr.NumPr.NumId createPPrNumPrNumId() {
        return new PPr.NumPr.NumId();
    }

    /**
     * Create an instance of {@link Styles.DocDefaults.PPrDefault }
     * 
     */
    public Styles.DocDefaults.PPrDefault createStylesDocDefaultsPPrDefault() {
        return new Styles.DocDefaults.PPrDefault();
    }

    /**
     * Create an instance of {@link SdtContent }
     * 
     */
    public SdtContent createSdtContent() {
        return new SdtContent();
    }

    /**
     * Create an instance of {@link Text }
     * 
     */
    public Text createText() {
        return new Text();
    }

    /**
     * Create an instance of {@link TrPr }
     * 
     */
    public TrPr createTrPr() {
        return new TrPr();
    }

    /**
     * Create an instance of {@link Spacing }
     * 
     */
    public Spacing createSpacing() {
        return new Spacing();
    }

    /**
     * Create an instance of {@link Numbering.AbstractNum.Name }
     * 
     */
    public Numbering.AbstractNum.Name createNumberingAbstractNumName() {
        return new Numbering.AbstractNum.Name();
    }

    /**
     * Create an instance of {@link CTLongHexNumber }
     * 
     */
    public CTLongHexNumber createCTLongHexNumber() {
        return new CTLongHexNumber();
    }

    /**
     * Create an instance of {@link TcBorders }
     * 
     */
    public TcBorders createTcBorders() {
        return new TcBorders();
    }

    /**
     * Create an instance of {@link Tl2Br }
     * 
     */
    public Tl2Br createTl2Br() {
        return new Tl2Br();
    }

    /**
     * Create an instance of {@link Styles.Style }
     * 
     */
    public Styles.Style createStylesStyle() {
        return new Styles.Style();
    }

    /**
     * Create an instance of {@link Lvl.Suff }
     * 
     */
    public Lvl.Suff createLvlSuff() {
        return new Lvl.Suff();
    }

    /**
     * Create an instance of {@link Br }
     * 
     */
    public Br createBr() {
        return new Br();
    }

    /**
     * Create an instance of {@link P }
     * 
     */
    public P createP() {
        return new P();
    }

    /**
     * Create an instance of {@link RPr.RStyle }
     * 
     */
    public RPr.RStyle createRPrRStyle() {
        return new RPr.RStyle();
    }

    /**
     * Create an instance of {@link TcType }
     * 
     */
    public TcType createTcType() {
        return new TcType();
    }

    /**
     * Create an instance of {@link TcW }
     * 
     */
    public TcW createTcW() {
        return new TcW();
    }

    /**
     * Create an instance of {@link Fonts.Font }
     * 
     */
    public Fonts.Font createFontsFont() {
        return new Fonts.Font();
    }

    /**
     * Create an instance of {@link Numbering.Num.LvlOverride.StartOverride }
     * 
     */
    public Numbering.Num.LvlOverride.StartOverride createNumberingNumLvlOverrideStartOverride() {
        return new Numbering.Num.LvlOverride.StartOverride();
    }

    /**
     * Create an instance of {@link Lvl.LvlText }
     * 
     */
    public Lvl.LvlText createLvlLvlText() {
        return new Lvl.LvlText();
    }

    /**
     * Create an instance of {@link OnOff }
     * 
     */
    public OnOff createOnOff() {
        return new OnOff();
    }

    /**
     * Create an instance of {@link Id }
     * 
     */
    public Id createId() {
        return new Id();
    }

    /**
     * Create an instance of {@link YearShort }
     * 
     */
    public YearShort createYearShort() {
        return new YearShort();
    }

    /**
     * Create an instance of {@link PPr }
     * 
     */
    public PPr createPPr() {
        return new PPr();
    }

    /**
     * Create an instance of {@link Jc }
     * 
     */
    public Jc createJc() {
        return new Jc();
    }

    /**
     * Create an instance of {@link Styles.Style.Aliases }
     * 
     */
    public Styles.Style.Aliases createStylesStyleAliases() {
        return new Styles.Style.Aliases();
    }

    /**
     * Create an instance of {@link DayShort }
     * 
     */
    public DayShort createDayShort() {
        return new DayShort();
    }

    /**
     * Create an instance of {@link Numbering.AbstractNum.MultiLevelType }
     * 
     */
    public Numbering.AbstractNum.MultiLevelType createNumberingAbstractNumMultiLevelType() {
        return new Numbering.AbstractNum.MultiLevelType();
    }

    /**
     * Create an instance of {@link AnnotationRef }
     * 
     */
    public AnnotationRef createAnnotationRef() {
        return new AnnotationRef();
    }

    /**
     * Create an instance of {@link TblPr }
     * 
     */
    public TblPr createTblPr() {
        return new TblPr();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TblPr }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "tblPr")
    public JAXBElement<TblPr> createTblPr(TblPr value) {
        return new JAXBElement<TblPr>(_TblPr_QNAME, TblPr.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Body }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "body")
    public JAXBElement<Body> createBody(Body value) {
        return new JAXBElement<Body>(_Body_QNAME, Body.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TblPrEx }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "tblPrEx")
    public JAXBElement<TblPrEx> createTblPrEx(TblPrEx value) {
        return new JAXBElement<TblPrEx>(_TblPrEx_QNAME, TblPrEx.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Tbl }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "tbl")
    public JAXBElement<Tbl> createTbl(Tbl value) {
        return new JAXBElement<Tbl>(_Tbl_QNAME, Tbl.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RunTrackChange }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "ins")
    public JAXBElement<RunTrackChange> createIns(RunTrackChange value) {
        return new JAXBElement<RunTrackChange>(_Ins_QNAME, RunTrackChange.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Text }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "delText")
    public JAXBElement<Text> createDelText(Text value) {
        return new JAXBElement<Text>(_DelText_QNAME, Text.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SectPr }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "sectPr")
    public JAXBElement<SectPr> createSectPr(SectPr value) {
        return new JAXBElement<SectPr>(_SectPr_QNAME, SectPr.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTVerticalJc }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "vAlign")
    public JAXBElement<CTVerticalJc> createVAlign(CTVerticalJc value) {
        return new JAXBElement<CTVerticalJc>(_VAlign_QNAME, CTVerticalJc.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Row }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "tr")
    public JAXBElement<Row> createTr(Row value) {
        return new JAXBElement<Row>(_Tr_QNAME, Row.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TrPr }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "trPr")
    public JAXBElement<TrPr> createTrPr(TrPr value) {
        return new JAXBElement<TrPr>(_TrPr_QNAME, TrPr.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link OnOff }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "noWrap")
    public JAXBElement<OnOff> createNoWrap(OnOff value) {
        return new JAXBElement<OnOff>(_NoWrap_QNAME, OnOff.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TblBorders }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "tblBorders")
    public JAXBElement<TblBorders> createTblBorders(TblBorders value) {
        return new JAXBElement<TblBorders>(_TblBorders_QNAME, TblBorders.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Text }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "instrText")
    public JAXBElement<Text> createInstrText(Text value) {
        return new JAXBElement<Text>(_InstrText_QNAME, Text.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Text }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "delInstrText")
    public JAXBElement<Text> createDelInstrText(Text value) {
        return new JAXBElement<Text>(_DelInstrText_QNAME, Text.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TcBorders }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "tcBorders")
    public JAXBElement<TcBorders> createTcBorders(TcBorders value) {
        return new JAXBElement<TcBorders>(_TcBorders_QNAME, TcBorders.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Text }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "t")
    public JAXBElement<Text> createT(Text value) {
        return new JAXBElement<Text>(_T_QNAME, Text.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Document }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "document")
    public JAXBElement<Document> createDocument(Document value) {
        return new JAXBElement<Document>(_Document_QNAME, Document.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link P }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "p")
    public JAXBElement<P> createP(P value) {
        return new JAXBElement<P>(_P_QNAME, P.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TcType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "tc")
    public JAXBElement<TcType> createTc(TcType value) {
        return new JAXBElement<TcType>(_Tc_QNAME, TcType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RunTrackChange }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "del")
    public JAXBElement<RunTrackChange> createDel(RunTrackChange value) {
        return new JAXBElement<RunTrackChange>(_Del_QNAME, RunTrackChange.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TcPr }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "tcPr")
    public JAXBElement<TcPr> createTcPr(TcPr value) {
        return new JAXBElement<TcPr>(_TcPr_QNAME, TcPr.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SdtPr }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", name = "sdtPr")
    public JAXBElement<SdtPr> createSdtPr(SdtPr value) {
        return new JAXBElement<SdtPr>(_SdtPr_QNAME, SdtPr.class, null, value);
    }

}
