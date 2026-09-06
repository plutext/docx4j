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

package org.docx4j.org.apache.poi.hwmf.record;

import java.util.function.Supplier;

/**
 * Available record types for WMF
 * 
 * @see <a href="http://www.symantec.com/avcenter/reference/inside.the.windows.meta.file.format.pdf">Inside the Windows Meta File Format</a>
 */
public enum HwmfRecordType {
     eof(0x0000, null)
    ,animatePalette(0x0436, HwmfPalette.WmfAnimatePalette::new)
    ,arc(0x0817, HwmfDraw.WmfArc::new)
    ,bitBlt(0x0922, HwmfFill.WmfBitBlt::new)
    ,chord(0x0830, HwmfDraw.WmfChord::new)
    ,createBrushIndirect(0x02fc, HwmfMisc.WmfCreateBrushIndirect::new)
    ,createFontIndirect(0x02fb, HwmfText.WmfCreateFontIndirect::new)
    ,createPalette(0x00f7, HwmfPalette.WmfCreatePalette::new)
    ,createPatternBrush(0x01f9, HwmfMisc.WmfCreatePatternBrush::new)
    ,createPenIndirect(0x02fa, HwmfMisc.WmfCreatePenIndirect::new)
    ,createRegion(0x06ff, HwmfWindowing.WmfCreateRegion::new)
    ,deleteObject(0x01f0, HwmfMisc.WmfDeleteObject::new)
    ,dibBitBlt(0x0940, HwmfFill.WmfDibBitBlt::new)
    ,dibCreatePatternBrush(0x0142, HwmfMisc.WmfDibCreatePatternBrush::new)
    ,dibStretchBlt(0x0b41, HwmfFill.WmfDibStretchBlt::new)
    ,ellipse(0x0418, HwmfDraw.WmfEllipse::new)
    ,escape(0x0626, HwmfEscape::new)
    ,excludeClipRect(0x0415, HwmfWindowing.WmfExcludeClipRect::new)
    ,extFloodFill(0x0548, HwmfFill.WmfExtFloodFill::new)
    ,extTextOut(0x0a32, HwmfText.WmfExtTextOut::new)
    ,fillRegion(0x0228, HwmfFill.WmfFillRegion::new)
    ,floodFill(0x0419, HwmfFill.WmfFloodFill::new)
    ,frameRegion(0x0429, HwmfDraw.WmfFrameRegion::new)
    ,intersectClipRect(0x0416, HwmfWindowing.WmfIntersectClipRect::new)
    ,invertRegion(0x012a, HwmfFill.WmfInvertRegion::new)
    ,lineTo(0x0213, HwmfDraw.WmfLineTo::new)
    ,moveTo(0x0214, HwmfDraw.WmfMoveTo::new)
    ,offsetClipRgn(0x0220, HwmfWindowing.WmfOffsetClipRgn::new)
    ,offsetViewportOrg(0x0211, HwmfWindowing.WmfOffsetViewportOrg::new)
    ,offsetWindowOrg(0x020f, HwmfWindowing.WmfOffsetWindowOrg::new)
    ,paintRegion(0x012b, HwmfFill.WmfPaintRegion::new)
    ,patBlt(0x061d, HwmfFill.WmfPatBlt::new)
    ,pie(0x081a, HwmfDraw.WmfPie::new)
    ,polygon(0x0324, HwmfDraw.WmfPolygon::new)
    ,polyline(0x0325, HwmfDraw.WmfPolyline::new)
    ,polyPolygon(0x0538, HwmfDraw.WmfPolyPolygon::new)
    ,realizePalette(0x0035, HwmfPalette.WmfRealizePalette::new)
    ,rectangle(0x041b, HwmfDraw.WmfRectangle::new)
    ,resizePalette(0x0139, HwmfPalette.WmfResizePalette::new)
    ,restoreDc(0x0127, HwmfMisc.WmfRestoreDc::new)
    ,roundRect(0x061c, HwmfDraw.WmfRoundRect::new)
    ,saveDc(0x001e, HwmfMisc.WmfSaveDc::new)
    ,scaleViewportExt(0x0412, HwmfWindowing.WmfScaleViewportExt::new)
    ,scaleWindowExt(0x0410, HwmfWindowing.WmfScaleWindowExt::new)
    ,selectClipRegion(0x012c, HwmfWindowing.WmfSelectClipRegion::new)
    ,selectObject(0x012d, HwmfDraw.WmfSelectObject::new)
    ,selectPalette(0x0234, HwmfPalette.WmfSelectPalette::new)
    ,setBkColor(0x0201, HwmfMisc.WmfSetBkColor::new)
    ,setBkMode(0x0102, HwmfMisc.WmfSetBkMode::new)
    ,setDibToDev(0x0d33, HwmfFill.WmfSetDibToDev::new)
    ,setLayout(0x0149, HwmfMisc.WmfSetLayout::new)
    ,setMapMode(0x0103, HwmfMisc.WmfSetMapMode::new)
    ,setMapperFlags(0x0231, HwmfMisc.WmfSetMapperFlags::new)
    ,setPalEntries(0x0037, HwmfPalette.WmfSetPaletteEntries::new)
    ,setPixel(0x041f, HwmfDraw.WmfSetPixel::new)
    ,setPolyFillMode(0x0106, HwmfFill.WmfSetPolyfillMode::new)
    ,setRelabs(0x0105, HwmfMisc.WmfSetRelabs::new)
    ,setRop2(0x0104, HwmfMisc.WmfSetRop2::new)
    ,setStretchBltMode(0x0107, HwmfMisc.WmfSetStretchBltMode::new)
    ,setTextAlign(0x012e, HwmfText.WmfSetTextAlign::new)
    ,setTextCharExtra(0x0108, HwmfText.WmfSetTextCharExtra::new)
    ,setTextColor(0x0209, HwmfText.WmfSetTextColor::new)
    ,setTextJustification(0x020a, HwmfText.WmfSetTextJustification::new)
    ,setViewportExt(0x020e, HwmfWindowing.WmfSetViewportExt::new)
    ,setViewportOrg(0x020d, HwmfWindowing.WmfSetViewportOrg::new)
    ,setWindowExt(0x020c, HwmfWindowing.WmfSetWindowExt::new)
    ,setWindowOrg(0x020b, HwmfWindowing.WmfSetWindowOrg::new)
    ,stretchBlt(0x0b23, HwmfFill.WmfStretchBlt::new)
    ,stretchDib(0x0f43, HwmfFill.WmfStretchDib::new)
    ,textOut(0x0521, HwmfText.WmfTextOut::new)
    ;
    
    public final int id;
    public final Supplier<? extends HwmfRecord> constructor;
    
    HwmfRecordType(int id, Supplier<? extends HwmfRecord> constructor) {
        this.id = id;
        this.constructor = constructor;
    }
    
    public static HwmfRecordType getById(int id) {
        for (HwmfRecordType wrt : values()) {
            if (wrt.id == id) return wrt;
        }
        return null;
    }
}
