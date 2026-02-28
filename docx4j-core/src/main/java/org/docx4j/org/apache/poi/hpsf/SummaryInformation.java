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

import static org.docx4j.org.apache.poi.hpsf.ClassIDPredefined.SUMMARY_PROPERTIES;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import org.docx4j.org.apache.poi.hpsf.wellknown.PropertyIDMap;

/**
 * Convenience class representing a Summary Information stream in a
 * Microsoft Office document.
 *
 * @see DocumentSummaryInformation
 */
public final class SummaryInformation extends PropertySet {

    /**
     * The document name a summary information stream usually has in a POIFS filesystem.
     */
    public static final String DEFAULT_STREAM_NAME = "\005SummaryInformation";

    /**
     * The SummaryInformation's section's format ID.
     */
    public static final ClassID FORMAT_ID = SUMMARY_PROPERTIES.getClassID();

    @Override
    public PropertyIDMap getPropertySetIDMap() {
        return PropertyIDMap.getSummaryInformationProperties();
    }

    /**
     * Creates an empty SummaryInformation.
     */
    public SummaryInformation() {
        getFirstSection().setFormatID(FORMAT_ID);
    }

    /**
     * Creates a SummaryInformation from a given {@link
     * PropertySet}.
     *
     * @param ps A property set which should be created from a summary
     *        information stream.
     * @throws UnexpectedPropertySetTypeException if {@code ps} does not
     *         contain a summary information stream.
     */
    public SummaryInformation(final PropertySet ps) throws UnexpectedPropertySetTypeException {
        super(ps);
        if (!isSummaryInformation()) {
            throw new UnexpectedPropertySetTypeException("Not a " + getClass().getName());
        }
    }

    /**
     * Creates a SummaryInformation instance from an {@link
     * InputStream} in the Horrible Property Set Format.<p>
     *
     * The constructor reads the first few bytes from the stream
     * and determines whether it is really a property set stream. If
     * it is, it parses the rest of the stream. If it is not, it
     * resets the stream to its beginning in order to let other
     * components mess around with the data and throws an
     * exception.
     *
     * @param stream Holds the data making out the property set
     * stream.
     * @throws IOException
     *    if the {@link InputStream} cannot be accessed as needed.
     * @throws NoPropertySetStreamException
     *    if the input stream does not contain a property set.
     * @throws UnsupportedEncodingException
     *    if a character encoding is not supported.
     */
    public SummaryInformation(final InputStream stream)
    throws NoPropertySetStreamException, IOException {
        super(stream);
    }


    /**
     * @return The title or {@code null}
     */
    public String getTitle() {
        return getPropertyStringValue(PropertyIDMap.PID_TITLE);
    }



    /**
     * Sets the title.
     *
     * @param title The title to set.
     */
    public void setTitle(final String title) {
        set1stProperty(PropertyIDMap.PID_TITLE, title);
    }



    /**
     * Removes the title.
     */
    public void removeTitle() {
        remove1stProperty(PropertyIDMap.PID_TITLE);
    }



    /**
     * Returns the subject (or {@code null}).
     *
     * @return The subject or {@code null}
     */
    public String getSubject() {
        return getPropertyStringValue(PropertyIDMap.PID_SUBJECT);
    }



    /**
     * Sets the subject.
     *
     * @param subject The subject to set.
     */
    public void setSubject(final String subject) {
        set1stProperty(PropertyIDMap.PID_SUBJECT, subject);
    }



    /**
     * Removes the subject.
     */
    public void removeSubject() {
        remove1stProperty(PropertyIDMap.PID_SUBJECT);
    }



    /**
     * Returns the author (or {@code null}).
     *
     * @return The author or {@code null}
     */
    public String getAuthor() {
        return getPropertyStringValue(PropertyIDMap.PID_AUTHOR);
    }



    /**
     * Sets the author.
     *
     * @param author The author to set.
     */
    public void setAuthor(final String author) {
        set1stProperty(PropertyIDMap.PID_AUTHOR, author);
    }



    /**
     * Removes the author.
     */
    public void removeAuthor() {
        remove1stProperty(PropertyIDMap.PID_AUTHOR);
    }



    /**
     * Returns the keywords (or {@code null}).
     *
     * @return The keywords or {@code null}
     */
    public String getKeywords() {
        return getPropertyStringValue(PropertyIDMap.PID_KEYWORDS);
    }



    /**
     * Sets the keywords.
     *
     * @param keywords The keywords to set.
     */
    public void setKeywords(final String keywords) {
        set1stProperty(PropertyIDMap.PID_KEYWORDS, keywords);
    }



    /**
     * Removes the keywords.
     */
    public void removeKeywords() {
        remove1stProperty(PropertyIDMap.PID_KEYWORDS);
    }



    /**
     * Returns the comments (or {@code null}).
     *
     * @return The comments or {@code null}
     */
    public String getComments() {
        return getPropertyStringValue(PropertyIDMap.PID_COMMENTS);
    }



    /**
     * Sets the comments.
     *
     * @param comments The comments to set.
     */
    public void setComments(final String comments) {
        set1stProperty(PropertyIDMap.PID_COMMENTS, comments);
    }



    /**
     * Removes the comments.
     */
    public void removeComments() {
        remove1stProperty(PropertyIDMap.PID_COMMENTS);
    }



    /**
     * Returns the template (or {@code null}).
     *
     * @return The template or {@code null}
     */
    public String getTemplate() {
        return getPropertyStringValue(PropertyIDMap.PID_TEMPLATE);
    }



    /**
     * Sets the template.
     *
     * @param template The template to set.
     */
    public void setTemplate(final String template) {
        set1stProperty(PropertyIDMap.PID_TEMPLATE, template);
    }



    /**
     * Removes the template.
     */
    public void removeTemplate() {
        remove1stProperty(PropertyIDMap.PID_TEMPLATE);
    }



    /**
     * Returns the last author (or {@code null}).
     *
     * @return The last author or {@code null}
     */
    public String getLastAuthor() {
        return getPropertyStringValue(PropertyIDMap.PID_LASTAUTHOR);
    }



    /**
     * Sets the last author.
     *
     * @param lastAuthor The last author to set.
     */
    public void setLastAuthor(final String lastAuthor) {
        set1stProperty(PropertyIDMap.PID_LASTAUTHOR, lastAuthor);
    }



    /**
     * Removes the last author.
     */
    public void removeLastAuthor() {
        remove1stProperty(PropertyIDMap.PID_LASTAUTHOR);
    }



    /**
     * Returns the revision number (or {@code null}).
     *
     * @return The revision number or {@code null}
     */
    public String getRevNumber() {
        return getPropertyStringValue(PropertyIDMap.PID_REVNUMBER);
    }



    /**
     * Sets the revision number.
     *
     * @param revNumber The revision number to set.
     */
    public void setRevNumber(final String revNumber) {
        set1stProperty(PropertyIDMap.PID_REVNUMBER, revNumber);
    }



    /**
     * Removes the revision number.
     */
    public void removeRevNumber() {
        remove1stProperty(PropertyIDMap.PID_REVNUMBER);
    }



    /**
     * Returns the total time spent in editing the document (or
     * {@code 0}).
     *
     * @return The total time spent in editing the document or 0 if the {@link
     *         SummaryInformation} does not contain this information.
     */
    public long getEditTime() {
        final Date d = (Date) getProperty(PropertyIDMap.PID_EDITTIME);
        if (d == null) {
            return 0;
        }
        return Filetime.dateToFileTime(d);
    }



    /**
     * Sets the total time spent in editing the document.
     *
     * @param time The time to set.
     */
    public void setEditTime(final long time) {
        final Date d = Filetime.filetimeToDate(time);
        getFirstSection().setProperty(PropertyIDMap.PID_EDITTIME, Variant.VT_FILETIME, d);
    }



    /**
     * Remove the total time spent in editing the document.
     */
    public void removeEditTime() {
        remove1stProperty(PropertyIDMap.PID_EDITTIME);
    }



    /**
     * Returns the last printed time (or {@code null}).
     *
     * @return The last printed time or {@code null}
     */
    public Date getLastPrinted() {
        return (Date) getProperty(PropertyIDMap.PID_LASTPRINTED);
    }



    /**
     * Sets the lastPrinted.
     *
     * @param lastPrinted The lastPrinted to set.
     */
    public void setLastPrinted(final Date lastPrinted) {
        getFirstSection().setProperty(PropertyIDMap.PID_LASTPRINTED, Variant.VT_FILETIME, lastPrinted);
    }



    /**
     * Removes the lastPrinted.
     */
    public void removeLastPrinted() {
        remove1stProperty(PropertyIDMap.PID_LASTPRINTED);
    }



    /**
     * Returns the creation time (or {@code null}).
     *
     * @return The creation time or {@code null}
     */
    public Date getCreateDateTime() {
        return (Date) getProperty(PropertyIDMap.PID_CREATE_DTM);
    }



    /**
     * Sets the creation time.
     *
     * @param createDateTime The creation time to set.
     */
    public void setCreateDateTime(final Date createDateTime) {
        getFirstSection().setProperty(PropertyIDMap.PID_CREATE_DTM, Variant.VT_FILETIME, createDateTime);
    }



    /**
     * Removes the creation time.
     */
    public void removeCreateDateTime() {
        remove1stProperty(PropertyIDMap.PID_CREATE_DTM);
    }



    /**
     * Returns the last save time (or {@code null}).
     *
     * @return The last save time or {@code null}
     */
    public Date getLastSaveDateTime() {
        return (Date) getProperty(PropertyIDMap.PID_LASTSAVE_DTM);
    }



    /**
     * Sets the total time spent in editing the document.
     *
     * @param time The time to set.
     */
    public void setLastSaveDateTime(final Date time) {
        final Section s = getFirstSection();
        s
                .setProperty(PropertyIDMap.PID_LASTSAVE_DTM,
                        Variant.VT_FILETIME, time);
    }



    /**
     * Remove the total time spent in editing the document.
     */
    public void removeLastSaveDateTime() {
        remove1stProperty(PropertyIDMap.PID_LASTSAVE_DTM);
    }



    /**
     * Returns the page count or 0 if the SummaryInformation does
     * not contain a page count.
     *
     * @return The page count or 0 if the SummaryInformation does not
     *         contain a page count.
     */
    public int getPageCount() {
        return getPropertyIntValue(PropertyIDMap.PID_PAGECOUNT);
    }



    /**
     * Sets the page count.
     *
     * @param pageCount The page count to set.
     */
    public void setPageCount(final int pageCount) {
        set1stProperty(PropertyIDMap.PID_PAGECOUNT, pageCount);
    }



    /**
     * Removes the page count.
     */
    public void removePageCount() {
        remove1stProperty(PropertyIDMap.PID_PAGECOUNT);
    }



    /**
     * Returns the word count or 0 if the SummaryInformation does
     * not contain a word count.
     *
     * @return The word count or {@code null}
     */
    public int getWordCount() {
        return getPropertyIntValue(PropertyIDMap.PID_WORDCOUNT);
    }



    /**
     * Sets the word count.
     *
     * @param wordCount The word count to set.
     */
    public void setWordCount(final int wordCount) {
        set1stProperty(PropertyIDMap.PID_WORDCOUNT, wordCount);
    }



    /**
     * Removes the word count.
     */
    public void removeWordCount() {
        remove1stProperty(PropertyIDMap.PID_WORDCOUNT);
    }



    /**
     * Returns the character count or 0 if the SummaryInformation
     * does not contain a char count.
     *
     * @return The character count or {@code null}
     */
    public int getCharCount() {
        return getPropertyIntValue(PropertyIDMap.PID_CHARCOUNT);
    }



    /**
     * Sets the character count.
     *
     * @param charCount The character count to set.
     */
    public void setCharCount(final int charCount) {
        set1stProperty(PropertyIDMap.PID_CHARCOUNT, charCount);
    }



    /**
     * Removes the character count.
     */
    public void removeCharCount() {
        remove1stProperty(PropertyIDMap.PID_CHARCOUNT);
    }



    /**
     * Returns the thumbnail (or {@code null}) <strong>when this
     * method is implemented. Please note that the return type is likely to
     * change!</strong><p>
     *
     * To process this data, you may wish to make use of the
     *  {@link Thumbnail} class. The raw data is generally
     *  an image in WMF or Clipboard (BMP?) format
     *
     * @return The thumbnail or {@code null}
     */
    public byte[] getThumbnail() {
        return (byte[]) getProperty(PropertyIDMap.PID_THUMBNAIL);
    }

    /**
     * Returns the thumbnail (or {@code null}), processed
     *  as an object which is (largely) able to unpack the thumbnail
     *  image data.
     *
     * @return The thumbnail or {@code null}
     */
    public Thumbnail getThumbnailThumbnail() {
        byte[] data = getThumbnail();
        if (data == null) {
            return null;
        }
        return new Thumbnail(data);
    }



    /**
     * Sets the thumbnail.
     *
     * @param thumbnail The thumbnail to set.
     */
    public void setThumbnail(final byte[] thumbnail) {
        getFirstSection().setProperty(PropertyIDMap.PID_THUMBNAIL, /* FIXME: */ Variant.VT_LPSTR, thumbnail);
    }



    /**
     * Removes the thumbnail.
     */
    public void removeThumbnail() {
        remove1stProperty(PropertyIDMap.PID_THUMBNAIL);
    }



    /**
     * Returns the application name (or {@code null}).
     *
     * @return The application name or {@code null}
     */
    public String getApplicationName() {
        return getPropertyStringValue(PropertyIDMap.PID_APPNAME);
    }



    /**
     * Sets the application name.
     *
     * @param applicationName The application name to set.
     */
    public void setApplicationName(final String applicationName) {
        set1stProperty(PropertyIDMap.PID_APPNAME, applicationName);
    }



    /**
     * Removes the application name.
     */
    public void removeApplicationName() {
        remove1stProperty(PropertyIDMap.PID_APPNAME);
    }



    /**
     * Returns a security code which is one of the following values:
     *
     * <ul>
     *
     * <li>0 if the SummaryInformation does not contain a
     * security field or if there is no security on the document. Use
     * {@link PropertySet#wasNull()} to distinguish between the two
     * cases!
     *
     * <li>1 if the document is password protected
     *
     * <li>2 if the document is read-only recommended
     *
     * <li>4 if the document is read-only enforced
     *
     * <li>8 if the document is locked for annotations
     *
     * </ul>
     *
     * @return The security code or {@code null}
     */
    public int getSecurity() {
        return getPropertyIntValue(PropertyIDMap.PID_SECURITY);
    }



    /**
     * Sets the security code.
     *
     * @param security The security code to set.
     */
    public void setSecurity(final int security) {
        set1stProperty(PropertyIDMap.PID_SECURITY, security);
    }



    /**
     * Removes the security code.
     */
    public void removeSecurity() {
        remove1stProperty(PropertyIDMap.PID_SECURITY);
    }

}
