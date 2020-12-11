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


package org.docx4j.vml;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.vml package. 
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

    private final static QName _Roundrect_QNAME = new QName("urn:schemas-microsoft-com:vml", "roundrect");
    private final static QName _Shape_QNAME = new QName("urn:schemas-microsoft-com:vml", "shape");
    private final static QName _Image_QNAME = new QName("urn:schemas-microsoft-com:vml", "image");
    private final static QName _Rect_QNAME = new QName("urn:schemas-microsoft-com:vml", "rect");
    private final static QName _Stroke_QNAME = new QName("urn:schemas-microsoft-com:vml", "stroke");
    private final static QName _Shadow_QNAME = new QName("urn:schemas-microsoft-com:vml", "shadow");
    private final static QName _Arc_QNAME = new QName("urn:schemas-microsoft-com:vml", "arc");
    private final static QName _Shapetype_QNAME = new QName("urn:schemas-microsoft-com:vml", "shapetype");
    private final static QName _Imagedata_QNAME = new QName("urn:schemas-microsoft-com:vml", "imagedata");
    private final static QName _Oval_QNAME = new QName("urn:schemas-microsoft-com:vml", "oval");
    private final static QName _Curve_QNAME = new QName("urn:schemas-microsoft-com:vml", "curve");
    private final static QName _Formulas_QNAME = new QName("urn:schemas-microsoft-com:vml", "formulas");
    private final static QName _Textbox_QNAME = new QName("urn:schemas-microsoft-com:vml", "textbox");
    private final static QName _Background_QNAME = new QName("urn:schemas-microsoft-com:vml", "background");
    private final static QName _Handles_QNAME = new QName("urn:schemas-microsoft-com:vml", "handles");
    private final static QName _Group_QNAME = new QName("urn:schemas-microsoft-com:vml", "group");
    private final static QName _Fill_QNAME = new QName("urn:schemas-microsoft-com:vml", "fill");
    private final static QName _Textpath_QNAME = new QName("urn:schemas-microsoft-com:vml", "textpath");
    private final static QName _Polyline_QNAME = new QName("urn:schemas-microsoft-com:vml", "polyline");
    private final static QName _Path_QNAME = new QName("urn:schemas-microsoft-com:vml", "path");
    private final static QName _Line_QNAME = new QName("urn:schemas-microsoft-com:vml", "line");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.vml
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTPath }
     * 
     */
    public CTPath createCTPath() {
        return new CTPath();
    }

    /**
     * Create an instance of {@link CTH }
     * 
     */
    public CTH createCTH() {
        return new CTH();
    }

    /**
     * Create an instance of {@link CTShadow }
     * 
     */
    public CTShadow createCTShadow() {
        return new CTShadow();
    }

    /**
     * Create an instance of {@link CTTextPath }
     * 
     */
    public CTTextPath createCTTextPath() {
        return new CTTextPath();
    }

    /**
     * Create an instance of {@link CTImageData }
     * 
     */
    public CTImageData createCTImageData() {
        return new CTImageData();
    }

    /**
     * Create an instance of {@link CTFill }
     * 
     */
    public CTFill createCTFill() {
        return new CTFill();
    }

    /**
     * Create an instance of {@link CTPolyLine }
     * 
     */
    public CTPolyLine createCTPolyLine() {
        return new CTPolyLine();
    }

    /**
     * Create an instance of {@link CTCurve }
     * 
     */
    public CTCurve createCTCurve() {
        return new CTCurve();
    }

    /**
     * Create an instance of {@link CTGroup }
     * 
     */
    public CTGroup createCTGroup() {
        return new CTGroup();
    }

    /**
     * Create an instance of {@link CTBackground }
     * 
     */
    public CTBackground createCTBackground() {
        return new CTBackground();
    }

    /**
     * Create an instance of {@link CTStroke }
     * 
     */
    public CTStroke createCTStroke() {
        return new CTStroke();
    }

    /**
     * Create an instance of {@link CTRect }
     * 
     */
    public CTRect createCTRect() {
        return new CTRect();
    }

    /**
     * Create an instance of {@link CTRoundRect }
     * 
     */
    public CTRoundRect createCTRoundRect() {
        return new CTRoundRect();
    }

    /**
     * Create an instance of {@link CTImage }
     * 
     */
    public CTImage createCTImage() {
        return new CTImage();
    }

    /**
     * Create an instance of {@link CTLine }
     * 
     */
    public CTLine createCTLine() {
        return new CTLine();
    }

    /**
     * Create an instance of {@link CTOval }
     * 
     */
    public CTOval createCTOval() {
        return new CTOval();
    }

    /**
     * Create an instance of {@link CTHandles }
     * 
     */
    public CTHandles createCTHandles() {
        return new CTHandles();
    }

    /**
     * Create an instance of {@link CTTextbox }
     * 
     */
    public CTTextbox createCTTextbox() {
        return new CTTextbox();
    }

    /**
     * Create an instance of {@link CTShapetype }
     * 
     */
    public CTShapetype createCTShapetype() {
        return new CTShapetype();
    }

    /**
     * Create an instance of {@link CTShape }
     * 
     */
    public CTShape createCTShape() {
        return new CTShape();
    }

    /**
     * Create an instance of {@link CTF }
     * 
     */
    public CTF createCTF() {
        return new CTF();
    }

    /**
     * Create an instance of {@link CTFormulas }
     * 
     */
    public CTFormulas createCTFormulas() {
        return new CTFormulas();
    }

    /**
     * Create an instance of {@link CTArc }
     * 
     */
    public CTArc createCTArc() {
        return new CTArc();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTRoundRect }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:schemas-microsoft-com:vml", name = "roundrect")
    public JAXBElement<CTRoundRect> createRoundrect(CTRoundRect value) {
        return new JAXBElement<CTRoundRect>(_Roundrect_QNAME, CTRoundRect.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTShape }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:schemas-microsoft-com:vml", name = "shape")
    public JAXBElement<CTShape> createShape(CTShape value) {
        return new JAXBElement<CTShape>(_Shape_QNAME, CTShape.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTImage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:schemas-microsoft-com:vml", name = "image")
    public JAXBElement<CTImage> createImage(CTImage value) {
        return new JAXBElement<CTImage>(_Image_QNAME, CTImage.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTRect }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:schemas-microsoft-com:vml", name = "rect")
    public JAXBElement<CTRect> createRect(CTRect value) {
        return new JAXBElement<CTRect>(_Rect_QNAME, CTRect.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTStroke }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:schemas-microsoft-com:vml", name = "stroke")
    public JAXBElement<CTStroke> createStroke(CTStroke value) {
        return new JAXBElement<CTStroke>(_Stroke_QNAME, CTStroke.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTShadow }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:schemas-microsoft-com:vml", name = "shadow")
    public JAXBElement<CTShadow> createShadow(CTShadow value) {
        return new JAXBElement<CTShadow>(_Shadow_QNAME, CTShadow.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTArc }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:schemas-microsoft-com:vml", name = "arc")
    public JAXBElement<CTArc> createArc(CTArc value) {
        return new JAXBElement<CTArc>(_Arc_QNAME, CTArc.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTShapetype }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:schemas-microsoft-com:vml", name = "shapetype")
    public JAXBElement<CTShapetype> createShapetype(CTShapetype value) {
        return new JAXBElement<CTShapetype>(_Shapetype_QNAME, CTShapetype.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTImageData }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:schemas-microsoft-com:vml", name = "imagedata")
    public JAXBElement<CTImageData> createImagedata(CTImageData value) {
        return new JAXBElement<CTImageData>(_Imagedata_QNAME, CTImageData.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTOval }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:schemas-microsoft-com:vml", name = "oval")
    public JAXBElement<CTOval> createOval(CTOval value) {
        return new JAXBElement<CTOval>(_Oval_QNAME, CTOval.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTCurve }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:schemas-microsoft-com:vml", name = "curve")
    public JAXBElement<CTCurve> createCurve(CTCurve value) {
        return new JAXBElement<CTCurve>(_Curve_QNAME, CTCurve.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTFormulas }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:schemas-microsoft-com:vml", name = "formulas")
    public JAXBElement<CTFormulas> createFormulas(CTFormulas value) {
        return new JAXBElement<CTFormulas>(_Formulas_QNAME, CTFormulas.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTTextbox }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:schemas-microsoft-com:vml", name = "textbox")
    public JAXBElement<CTTextbox> createTextbox(CTTextbox value) {
        return new JAXBElement<CTTextbox>(_Textbox_QNAME, CTTextbox.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTBackground }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:schemas-microsoft-com:vml", name = "background")
    public JAXBElement<CTBackground> createBackground(CTBackground value) {
        return new JAXBElement<CTBackground>(_Background_QNAME, CTBackground.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTHandles }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:schemas-microsoft-com:vml", name = "handles")
    public JAXBElement<CTHandles> createHandles(CTHandles value) {
        return new JAXBElement<CTHandles>(_Handles_QNAME, CTHandles.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTGroup }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:schemas-microsoft-com:vml", name = "group")
    public JAXBElement<CTGroup> createGroup(CTGroup value) {
        return new JAXBElement<CTGroup>(_Group_QNAME, CTGroup.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTFill }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:schemas-microsoft-com:vml", name = "fill")
    public JAXBElement<CTFill> createFill(CTFill value) {
        return new JAXBElement<CTFill>(_Fill_QNAME, CTFill.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTTextPath }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:schemas-microsoft-com:vml", name = "textpath")
    public JAXBElement<CTTextPath> createTextpath(CTTextPath value) {
        return new JAXBElement<CTTextPath>(_Textpath_QNAME, CTTextPath.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPolyLine }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:schemas-microsoft-com:vml", name = "polyline")
    public JAXBElement<CTPolyLine> createPolyline(CTPolyLine value) {
        return new JAXBElement<CTPolyLine>(_Polyline_QNAME, CTPolyLine.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPath }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:schemas-microsoft-com:vml", name = "path")
    public JAXBElement<CTPath> createPath(CTPath value) {
        return new JAXBElement<CTPath>(_Path_QNAME, CTPath.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTLine }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:schemas-microsoft-com:vml", name = "line")
    public JAXBElement<CTLine> createLine(CTLine value) {
        return new JAXBElement<CTLine>(_Line_QNAME, CTLine.class, null, value);
    }

}
