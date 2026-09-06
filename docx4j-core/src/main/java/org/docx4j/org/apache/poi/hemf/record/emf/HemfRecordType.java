/* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
 * The package name has been changed; there may also be other changes.
 *
 * This notice is included to meet the condition in clause 4(b) of the License.
 */

/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.docx4j.org.apache.poi.hemf.record.emf;

import java.util.function.Supplier;

import org.docx4j.org.apache.poi.util.Internal;

@Internal
public enum HemfRecordType {

    header(0x00000001, HemfHeader::new),
    polyBezier(0x00000002, HemfDraw.EmfPolyBezier::new),
    polygon(0x00000003, HemfDraw.EmfPolygon::new),
    polyline(0x00000004, HemfDraw.EmfPolyline::new),
    polyBezierTo(0x00000005, HemfDraw.EmfPolyBezierTo::new),
    polylineTo(0x00000006, HemfDraw.EmfPolylineTo::new),
    polyPolyline(0x00000007, HemfDraw.EmfPolyPolyline::new),
    polyPolygon(0x00000008, HemfDraw.EmfPolyPolygon::new),
    setWindowExtEx(0x00000009, HemfWindowing.EmfSetWindowExtEx::new),
    setWindowOrgEx(0x0000000A, HemfWindowing.EmfSetWindowOrgEx::new),
    setViewportExtEx(0x0000000B, HemfWindowing.EmfSetViewportExtEx::new),
    setViewportOrgEx(0x0000000C, HemfWindowing.EmfSetViewportOrgEx::new),
    setBrushOrgEx(0x0000000D, HemfMisc.EmfSetBrushOrgEx::new),
    eof(0x0000000E, HemfMisc.EmfEof::new),
    setPixelV(0x0000000F, HemfDraw.EmfSetPixelV::new),
    setMapperFlags(0x00000010, HemfMisc.EmfSetMapperFlags::new),
    setMapMode(0x00000011, HemfMisc.EmfSetMapMode::new),
    setBkMode(0x00000012, HemfMisc.EmfSetBkMode::new),
    setPolyfillMode(0x00000013, HemfFill.EmfSetPolyfillMode::new),
    setRop2(0x00000014, HemfMisc.EmfSetRop2::new),
    setStretchBltMode(0x00000015, HemfMisc.EmfSetStretchBltMode::new),
    setTextAlign(0x00000016, HemfText.EmfSetTextAlign::new),
    setcoloradjustment(0x00000017, UnimplementedHemfRecord::new),
    setTextColor(0x00000018, HemfText.EmfSetTextColor::new),
    setBkColor(0x00000019, HemfMisc.EmfSetBkColor::new),
    setOffsetClipRgn(0x0000001A, HemfWindowing.EmfSetOffsetClipRgn::new),
    setMoveToEx(0x0000001B, HemfDraw.EmfSetMoveToEx::new),
    setmetargn(0x0000001C, UnimplementedHemfRecord::new),
    setExcludeClipRect(0x0000001D, HemfWindowing.EmfSetExcludeClipRect::new),
    setIntersectClipRect(0x0000001E, HemfWindowing.EmfSetIntersectClipRect::new),
    scaleViewportExtEx(0x0000001F, HemfWindowing.EmfScaleViewportExtEx::new),
    scaleWindowExtEx(0x00000020, HemfWindowing.EmfScaleWindowExtEx::new),
    saveDc(0x00000021, HemfMisc.EmfSaveDc::new),
    restoreDc(0x00000022, HemfMisc.EmfRestoreDc::new),
    setWorldTransform(0x00000023, HemfMisc.EmfSetWorldTransform::new),
    modifyWorldTransform(0x00000024, HemfMisc.EmfModifyWorldTransform::new),
    selectObject(0x00000025, HemfDraw.EmfSelectObject::new),
    createPen(0x00000026, HemfMisc.EmfCreatePen::new),
    createBrushIndirect(0x00000027, HemfMisc.EmfCreateBrushIndirect::new),
    deleteobject(0x00000028, HemfMisc.EmfDeleteObject::new),
    anglearc(0x00000029, UnimplementedHemfRecord::new),
    ellipse(0x0000002A, HemfDraw.EmfEllipse::new),
    rectangle(0x0000002B, HemfDraw.EmfRectangle::new),
    roundRect(0x0000002C, HemfDraw.EmfRoundRect::new),
    arc(0x0000002D, HemfDraw.EmfArc::new),
    chord(0x0000002E, HemfDraw.EmfChord::new),
    pie(0x0000002F, HemfDraw.EmfPie::new),
    selectPalette(0x00000030, HemfPalette.EmfSelectPalette::new),
    createPalette(0x00000031, HemfPalette.EmfCreatePalette::new),
    setPaletteEntries(0x00000032, HemfPalette.EmfSetPaletteEntries::new),
    resizePalette(0x00000033, HemfPalette.EmfResizePalette::new),
    realizePalette(0x0000034, HemfPalette.EmfRealizePalette::new),
    extFloodFill(0x00000035, HemfFill.EmfExtFloodFill::new),
    lineTo(0x00000036, HemfDraw.EmfLineTo::new),
    arcTo(0x00000037, HemfDraw.EmfArcTo::new),
    polyDraw(0x00000038, HemfDraw.EmfPolyDraw::new),
    setarcdirection(0x00000039, UnimplementedHemfRecord::new),
    setMiterLimit(0x0000003A, HemfMisc.EmfSetMiterLimit::new),
    beginPath(0x0000003B, HemfDraw.EmfBeginPath::new),
    endPath(0x0000003C, HemfDraw.EmfEndPath::new),
    closeFigure(0x0000003D, HemfDraw.EmfCloseFigure::new),
    fillPath(0x0000003E, HemfDraw.EmfFillPath::new),
    strokeAndFillPath(0x0000003F, HemfDraw.EmfStrokeAndFillPath::new),
    strokePath(0x00000040, HemfDraw.EmfStrokePath::new),
    flattenPath(0x00000041, HemfDraw.EmfFlattenPath::new),
    widenPath(0x00000042, HemfDraw.EmfWidenPath::new),
    selectClipPath(0x00000043, HemfWindowing.EmfSelectClipPath::new),
    abortPath(0x00000044, HemfDraw.EmfAbortPath::new),
    // no 45 ?!
    comment(0x00000046, HemfComment.EmfComment::new),
    fillRgn(0x00000047, HemfFill.EmfFillRgn::new),
    frameRgn(0x00000048, HemfFill.EmfFrameRgn::new),
    invertRgn(0x00000049, HemfFill.EmfInvertRgn::new),
    paintRgn(0x0000004A, HemfFill.EmfPaintRgn::new),
    extSelectClipRgn(0x0000004B, HemfFill.EmfExtSelectClipRgn::new),
    bitBlt(0x0000004C, HemfFill.EmfBitBlt::new),
    stretchBlt(0x0000004D, HemfFill.EmfStretchBlt::new),
    maskblt(0x0000004E, UnimplementedHemfRecord::new),
    plgblt(0x0000004F, UnimplementedHemfRecord::new),
    setDiBitsToDevice(0x00000050, HemfFill.EmfSetDiBitsToDevice::new),
    stretchDiBits(0x00000051, HemfFill.EmfStretchDiBits::new),
    extCreateFontIndirectW(0x00000052, HemfText.EmfExtCreateFontIndirectW::new),
    extTextOutA(0x00000053, HemfText.EmfExtTextOutA::new),
    extTextOutW(0x00000054, HemfText.EmfExtTextOutW::new),
    polyBezier16(0x00000055, HemfDraw.EmfPolyBezier16::new),
    polygon16(0x00000056, HemfDraw.EmfPolygon16::new),
    polyline16(0x00000057, HemfDraw.EmfPolyline16::new),
    polyBezierTo16(0x00000058, HemfDraw.EmfPolyBezierTo16::new),
    polylineTo16(0x00000059, HemfDraw.EmfPolylineTo16::new),
    polyPolyline16(0x0000005A, HemfDraw.EmfPolyPolyline16::new),
    polyPolygon16(0x0000005B, HemfDraw.EmfPolyPolygon16::new),
    polyDraw16(0x0000005C, HemfDraw.EmfPolyDraw16::new),
    createMonoBrush(0x0000005D, HemfMisc.EmfCreateMonoBrush::new),
    createDibPatternBrushPt(0x0000005E, HemfMisc.EmfCreateDibPatternBrushPt::new),
    extCreatePen(0x0000005F, HemfMisc.EmfExtCreatePen::new),
    polytextouta(0x00000060, HemfText.PolyTextOutA::new),
    polytextoutw(0x00000061, HemfText.PolyTextOutW::new),
    seticmmode(0x00000062, HemfPalette.EmfSetIcmMode::new),
    createcolorspace(0x00000063, UnimplementedHemfRecord::new),
    setcolorspace(0x00000064, UnimplementedHemfRecord::new),
    deletecolorspace(0x00000065, UnimplementedHemfRecord::new),
    glsrecord(0x00000066, UnimplementedHemfRecord::new),
    glsboundedrecord(0x00000067, UnimplementedHemfRecord::new),
    pixelformat(0x00000068, UnimplementedHemfRecord::new),
    drawescape(0x00000069, UnimplementedHemfRecord::new),
    extescape(0x0000006A, UnimplementedHemfRecord::new),
    // no 6b ?!
    smalltextout(0x0000006C, UnimplementedHemfRecord::new),
    forceufimapping(0x0000006D, UnimplementedHemfRecord::new),
    namedescape(0x0000006E, UnimplementedHemfRecord::new),
    colorcorrectpalette(0x0000006F, UnimplementedHemfRecord::new),
    seticmprofilea(0x00000070, UnimplementedHemfRecord::new),
    seticmprofilew(0x00000071, UnimplementedHemfRecord::new),
    alphaBlend(0x00000072, HemfFill.EmfAlphaBlend::new),
    setlayout(0x00000073, UnimplementedHemfRecord::new),
    transparentblt(0x00000074, UnimplementedHemfRecord::new),
    // no 75 ?!
    gradientfill(0x00000076, UnimplementedHemfRecord::new),
    setlinkdufis(0x00000077, UnimplementedHemfRecord::new),
    settextjustification(0x00000078, HemfText.SetTextJustification::new),
    colormatchtargetw(0x00000079, UnimplementedHemfRecord::new),
    createcolorspacew(0x0000007A, UnimplementedHemfRecord::new);


    public final long id;
    public final Supplier<? extends HemfRecord> constructor;

    HemfRecordType(long id, Supplier<? extends HemfRecord> constructor) {
        this.id = id;
        this.constructor = constructor;
    }

    public static HemfRecordType getById(long id) {
        for (HemfRecordType wrt : values()) {
            if (wrt.id == id) return wrt;
        }
        return null;
    }
}
