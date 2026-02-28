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

package org.docx4j.org.apache.poi.hpsf;

import org.docx4j.org.apache.poi.util.IOUtils;
import org.docx4j.org.apache.poi.util.LittleEndian;

/**
 * <p>Class to manipulate data in the Clipboard Variant ({@link
 * Variant#VT_CF VT_CF}) format.</p>
 *
 * @see SummaryInformation#getThumbnail()
 * @see <a href="https://msdn.microsoft.com/en-us/library/windows/desktop/ms649014(v=vs.85).aspx">Clipboard Operations</a>
 */
public final class Thumbnail {

    /**
     * <p>Offset in bytes where the Clipboard Format Tag starts in the
     * <code>byte[]</code> returned by {@link
     * SummaryInformation#getThumbnail()}</p>
     */
    public static final int OFFSET_CFTAG = 4;

    /**
     * <p>Offset in bytes where the Clipboard Format starts in the
     * <code>byte[]</code> returned by {@link
     * SummaryInformation#getThumbnail()}</p>
     *
     * <p>This is only valid if the Clipboard Format Tag is {@link
     * #CFTAG_WINDOWS}</p>
     */
    public static final int OFFSET_CF = 8;

    /**
     * <p>Offset in bytes where the Windows Metafile (WMF) image data
     * starts in the <code>byte[]</code> returned by {@link
     * SummaryInformation#getThumbnail()}</p>
     *
     * <p>There is only WMF data at this point in the
     * <code>byte[]</code> if the Clipboard Format Tag is {@link
     * #CFTAG_WINDOWS} and the Clipboard Format is {@link
     * #CF_METAFILEPICT}.</p>
     *
     * <p>Note: The <code>byte[]</code> that starts at
     * <code>OFFSET_WMFDATA</code> and ends at
     * <code>getThumbnail().length - 1</code> forms a complete WMF
     * image. It can be saved to disk with a <code>.wmf</code> file
     * type and read using a WMF-capable image viewer.</p>
     */
    public static final int OFFSET_WMFDATA = 20;

    /**
     * <p>Clipboard Format Tag - Windows clipboard format</p>
     *
     * <p>A <code>DWORD</code> indicating a built-in Windows clipboard
     * format value</p>
     */
    public static final int CFTAG_WINDOWS = -1;

    /**
     * <p>Clipboard Format Tag - Macintosh clipboard format</p>
     *
     * <p>A <code>DWORD</code> indicating a Macintosh clipboard format
     * value</p>
     */
    public static final int CFTAG_MACINTOSH = -2;

    /**
     * <p>Clipboard Format Tag - Format ID</p>
     *
     * <p>A GUID containing a format identifier (FMTID). This is
     * rarely used.</p>
     */
    public static final int CFTAG_FMTID = -3;

    /**
     * <p>Clipboard Format Tag - No Data</p>
     *
     * <p>A <code>DWORD</code> indicating No data. This is rarely
     * used.</p>
     */
    public static final int CFTAG_NODATA = 0;

    /**
     * <p>Clipboard Format - Windows metafile format. This is the
     * recommended way to store thumbnails in Property Streams.</p>
     *
     * <p><strong>Note:</strong> This is not the same format used in
     * regular WMF images. The clipboard version of this format has an
     * extra clipboard-specific header.</p>
     */
    public static final int CF_METAFILEPICT = 3;

    /**
     * <p>Clipboard Format - Device Independent Bitmap</p>
     */
    public static final int CF_DIB = 8;

    /**
     * <p>Clipboard Format - Enhanced Windows metafile format</p>
     */
    public static final int CF_ENHMETAFILE = 14;

    /**
     * <p>Clipboard Format - Bitmap</p>
     */
    public static final int CF_BITMAP = 2;

    //arbitrarily selected; may need to increase
    private static final int DEFAULT_MAX_RECORD_LENGTH = 1_000_000;
    private static int MAX_RECORD_LENGTH = DEFAULT_MAX_RECORD_LENGTH;

    /**
     * <p>A <code>byte[]</code> to hold a thumbnail image in ({@link
     * Variant#VT_CF VT_CF}) format.</p>
     */
    private byte[] _thumbnailData;

    /**
     * @param length the max record length allowed for SubRecord
     */
    public static void setMaxRecordLength(int length) {
        MAX_RECORD_LENGTH = length;
    }

    /**
     * @return the max record length allowed for SubRecord
     */
    public static int getMaxRecordLength() {
        return MAX_RECORD_LENGTH;
    }

    /**
     * <p>Default Constructor. If you use it then one you'll have to add
     * the thumbnail <code>byte[]</code> from {@link
     * SummaryInformation#getThumbnail()} to do any useful
     * manipulations, otherwise you'll get a
     * <code>NullPointerException</code>.</p>
     */
    public Thumbnail()
    {
        super();
    }



    /**
     * <p>Creates a <code>Thumbnail</code> instance and initializes
     * with the specified image bytes.</p>
     *
     * @param thumbnailData The thumbnail data
     */
    public Thumbnail(final byte[] thumbnailData)
    {
        this._thumbnailData = thumbnailData;
    }



    /**
     * <p>Returns the thumbnail as a <code>byte[]</code> in {@link
     * Variant#VT_CF VT_CF} format.</p>
     *
     * @return The thumbnail value
     * @see SummaryInformation#getThumbnail()
     */
    public byte[] getThumbnail()
    {
        return _thumbnailData;
    }



    /**
     * <p>Sets the Thumbnail's underlying <code>byte[]</code> in
     * {@link Variant#VT_CF VT_CF} format.</p>
     *
     * @param thumbnail The new thumbnail value
     * @see SummaryInformation#getThumbnail()
     */
    public void setThumbnail(final byte[] thumbnail)
    {
        this._thumbnailData = thumbnail;
    }



    /**
     * <p>Returns an <code>int</code> representing the Clipboard
     * Format Tag</p>
     *
     * <p>Possible return values are:</p>
     * <ul>
     *  <li>{@link #CFTAG_WINDOWS CFTAG_WINDOWS}</li>
     *  <li>{@link #CFTAG_MACINTOSH CFTAG_MACINTOSH}</li>
     *  <li>{@link #CFTAG_FMTID CFTAG_FMTID}</li>
     *  <li>{@link #CFTAG_NODATA CFTAG_NODATA}</li>
     * </ul>
     *
     * @return A flag indicating the Clipboard Format Tag
     */
    public long getClipboardFormatTag()
    {
        return (long) LittleEndian.getInt(getThumbnail(),
                                                       OFFSET_CFTAG);
    }



    /**
     * <p>Returns an <code>int</code> representing the Clipboard
     * Format</p>
     *
     * <p>Will throw an exception if the Thumbnail's Clipboard Format
     * Tag is not {@link Thumbnail#CFTAG_WINDOWS CFTAG_WINDOWS}.</p>
     *
     * <p>Possible return values are:</p>
     *
     * <ul>
     *  <li>{@link #CF_METAFILEPICT CF_METAFILEPICT}</li>
     *  <li>{@link #CF_DIB CF_DIB}</li>
     *  <li>{@link #CF_ENHMETAFILE CF_ENHMETAFILE}</li>
     *  <li>{@link #CF_BITMAP CF_BITMAP}</li>
     * </ul>
     *
     * @return a flag indicating the Clipboard Format
     * @throws HPSFException if the Thumbnail isn't CFTAG_WINDOWS
     */
    public long getClipboardFormat() throws HPSFException
    {
        if (!(getClipboardFormatTag() == CFTAG_WINDOWS))
            throw new HPSFException("Clipboard Format Tag of Thumbnail must " +
                                    "be CFTAG_WINDOWS.");

        return LittleEndian.getInt(getThumbnail(), OFFSET_CF);
    }



    /**
     * <p>Returns the Thumbnail as a <code>byte[]</code> of WMF data
     * if the Thumbnail's Clipboard Format Tag is {@link
     * #CFTAG_WINDOWS CFTAG_WINDOWS} and its Clipboard Format is
     * {@link #CF_METAFILEPICT CF_METAFILEPICT}</p> <p>This
     * <code>byte[]</code> is in the traditional WMF file, not the
     * clipboard-specific version with special headers.</p>
     *
     * <p>See <a href="http://www.wvware.com/caolan/ora-wmf.html"
     * target="_blank">http://www.wvware.com/caolan/ora-wmf.html</a>
     * for more information on the WMF image format.</p>
     *
     * @return A WMF image of the Thumbnail
     * @throws HPSFException if the Thumbnail isn't CFTAG_WINDOWS and
     * CF_METAFILEPICT
     */
    public byte[] getThumbnailAsWMF() throws HPSFException
    {
        if (!(getClipboardFormatTag() == CFTAG_WINDOWS))
            throw new HPSFException("Clipboard Format Tag of Thumbnail must " +
                                    "be CFTAG_WINDOWS.");
        if (!(getClipboardFormat() == CF_METAFILEPICT)) {
            throw new HPSFException("Clipboard Format of Thumbnail must " +
                                    "be CF_METAFILEPICT.");
        }
        byte[] thumbnail = getThumbnail();
        return IOUtils.safelyClone(thumbnail, OFFSET_WMFDATA, thumbnail.length - OFFSET_WMFDATA, MAX_RECORD_LENGTH);
    }
}
