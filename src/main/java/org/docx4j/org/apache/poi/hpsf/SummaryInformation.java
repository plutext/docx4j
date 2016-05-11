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

import java.util.Date;

import org.docx4j.org.apache.poi.hpsf.wellknown.PropertyIDMap;

/**
 * <p>Convenience class representing a Summary Information stream in a
 * Microsoft Office document.</p>
 *
 * @see DocumentSummaryInformation
 */
public final class SummaryInformation extends SpecialPropertySet {

    /**
     * <p>The document name a summary information stream usually has in a POIFS
     * filesystem.</p>
     */
    public static final String DEFAULT_STREAM_NAME = "\005SummaryInformation";

    public PropertyIDMap getPropertySetIDMap() {
    	return PropertyIDMap.getSummaryInformationProperties();
    }


    /**
     * <p>Creates a {@link SummaryInformation} from a given {@link
     * PropertySet}.</p>
     *
     * @param ps A property set which should be created from a summary
     *        information stream.
     * @throws UnexpectedPropertySetTypeException if <var>ps</var> does not
     *         contain a summary information stream.
     */
    public SummaryInformation(final PropertySet ps)
            throws UnexpectedPropertySetTypeException
    {
        super(ps);
        if (!isSummaryInformation())
            throw new UnexpectedPropertySetTypeException("Not a "
                    + getClass().getName());
    }



    /**
     * <p>Returns the title (or <code>null</code>).</p>
     *
     * @return The title or <code>null</code>
     */
    public String getTitle()
    {
        return getPropertyStringValue(PropertyIDMap.PID_TITLE);
    }



    /**
     * <p>Sets the title.</p>
     *
     * @param title The title to set.
     */
    public void setTitle(final String title)
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.setProperty(PropertyIDMap.PID_TITLE, title);
    }



    /**
     * <p>Removes the title.</p>
     */
    public void removeTitle()
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.removeProperty(PropertyIDMap.PID_TITLE);
    }



    /**
     * <p>Returns the subject (or <code>null</code>).</p>
     *
     * @return The subject or <code>null</code>
     */
    public String getSubject()
    {
        return getPropertyStringValue(PropertyIDMap.PID_SUBJECT);
    }



    /**
     * <p>Sets the subject.</p>
     *
     * @param subject The subject to set.
     */
    public void setSubject(final String subject)
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.setProperty(PropertyIDMap.PID_SUBJECT, subject);
    }



    /**
     * <p>Removes the subject.</p>
     */
    public void removeSubject()
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.removeProperty(PropertyIDMap.PID_SUBJECT);
    }



    /**
     * <p>Returns the author (or <code>null</code>).</p>
     *
     * @return The author or <code>null</code>
     */
    public String getAuthor()
    {
        return getPropertyStringValue(PropertyIDMap.PID_AUTHOR);
    }



    /**
     * <p>Sets the author.</p>
     *
     * @param author The author to set.
     */
    public void setAuthor(final String author)
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.setProperty(PropertyIDMap.PID_AUTHOR, author);
    }



    /**
     * <p>Removes the author.</p>
     */
    public void removeAuthor()
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.removeProperty(PropertyIDMap.PID_AUTHOR);
    }



    /**
     * <p>Returns the keywords (or <code>null</code>).</p>
     *
     * @return The keywords or <code>null</code>
     */
    public String getKeywords()
    {
        return getPropertyStringValue(PropertyIDMap.PID_KEYWORDS);
    }



    /**
     * <p>Sets the keywords.</p>
     *
     * @param keywords The keywords to set.
     */
    public void setKeywords(final String keywords)
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.setProperty(PropertyIDMap.PID_KEYWORDS, keywords);
    }



    /**
     * <p>Removes the keywords.</p>
     */
    public void removeKeywords()
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.removeProperty(PropertyIDMap.PID_KEYWORDS);
    }



    /**
     * <p>Returns the comments (or <code>null</code>).</p>
     *
     * @return The comments or <code>null</code>
     */
    public String getComments()
    {
        return getPropertyStringValue(PropertyIDMap.PID_COMMENTS);
    }



    /**
     * <p>Sets the comments.</p>
     *
     * @param comments The comments to set.
     */
    public void setComments(final String comments)
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.setProperty(PropertyIDMap.PID_COMMENTS, comments);
    }



    /**
     * <p>Removes the comments.</p>
     */
    public void removeComments()
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.removeProperty(PropertyIDMap.PID_COMMENTS);
    }



    /**
     * <p>Returns the template (or <code>null</code>).</p>
     *
     * @return The template or <code>null</code>
     */
    public String getTemplate()
    {
        return getPropertyStringValue(PropertyIDMap.PID_TEMPLATE);
    }



    /**
     * <p>Sets the template.</p>
     *
     * @param template The template to set.
     */
    public void setTemplate(final String template)
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.setProperty(PropertyIDMap.PID_TEMPLATE, template);
    }



    /**
     * <p>Removes the template.</p>
     */
    public void removeTemplate()
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.removeProperty(PropertyIDMap.PID_TEMPLATE);
    }



    /**
     * <p>Returns the last author (or <code>null</code>).</p>
     *
     * @return The last author or <code>null</code>
     */
    public String getLastAuthor()
    {
        return getPropertyStringValue(PropertyIDMap.PID_LASTAUTHOR);
    }



    /**
     * <p>Sets the last author.</p>
     *
     * @param lastAuthor The last author to set.
     */
    public void setLastAuthor(final String lastAuthor)
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.setProperty(PropertyIDMap.PID_LASTAUTHOR, lastAuthor);
    }



    /**
     * <p>Removes the last author.</p>
     */
    public void removeLastAuthor()
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.removeProperty(PropertyIDMap.PID_LASTAUTHOR);
    }



    /**
     * <p>Returns the revision number (or <code>null</code>). </p>
     *
     * @return The revision number or <code>null</code>
     */
    public String getRevNumber()
    {
        return getPropertyStringValue(PropertyIDMap.PID_REVNUMBER);
    }



    /**
     * <p>Sets the revision number.</p>
     *
     * @param revNumber The revision number to set.
     */
    public void setRevNumber(final String revNumber)
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.setProperty(PropertyIDMap.PID_REVNUMBER, revNumber);
    }



    /**
     * <p>Removes the revision number.</p>
     */
    public void removeRevNumber()
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.removeProperty(PropertyIDMap.PID_REVNUMBER);
    }



    /**
     * <p>Returns the total time spent in editing the document (or
     * <code>0</code>).</p>
     *
     * @return The total time spent in editing the document or 0 if the {@link
     *         SummaryInformation} does not contain this information.
     */
    public long getEditTime()
    {
        final Date d = (Date) getProperty(PropertyIDMap.PID_EDITTIME);
        if (d == null) {
            return 0;
        }
        return Util.dateToFileTime(d);
    }



    /**
     * <p>Sets the total time spent in editing the document.</p>
     *
     * @param time The time to set.
     */
    public void setEditTime(final long time)
    {
        final Date d = Util.filetimeToDate(time);
        final MutableSection s = (MutableSection) getFirstSection();
        s.setProperty(PropertyIDMap.PID_EDITTIME, Variant.VT_FILETIME, d);
    }



    /**
     * <p>Remove the total time spent in editing the document.</p>
     */
    public void removeEditTime()
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.removeProperty(PropertyIDMap.PID_EDITTIME);
    }



    /**
     * <p>Returns the last printed time (or <code>null</code>).</p>
     *
     * @return The last printed time or <code>null</code>
     */
    public Date getLastPrinted()
    {
        return (Date) getProperty(PropertyIDMap.PID_LASTPRINTED);
    }



    /**
     * <p>Sets the lastPrinted.</p>
     *
     * @param lastPrinted The lastPrinted to set.
     */
    public void setLastPrinted(final Date lastPrinted)
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.setProperty(PropertyIDMap.PID_LASTPRINTED, Variant.VT_FILETIME,
                lastPrinted);
    }



    /**
     * <p>Removes the lastPrinted.</p>
     */
    public void removeLastPrinted()
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.removeProperty(PropertyIDMap.PID_LASTPRINTED);
    }



    /**
     * <p>Returns the creation time (or <code>null</code>).</p>
     *
     * @return The creation time or <code>null</code>
     */
    public Date getCreateDateTime()
    {
        return (Date) getProperty(PropertyIDMap.PID_CREATE_DTM);
    }



    /**
     * <p>Sets the creation time.</p>
     *
     * @param createDateTime The creation time to set.
     */
    public void setCreateDateTime(final Date createDateTime)
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.setProperty(PropertyIDMap.PID_CREATE_DTM, Variant.VT_FILETIME,
                createDateTime);
    }



    /**
     * <p>Removes the creation time.</p>
     */
    public void removeCreateDateTime()
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.removeProperty(PropertyIDMap.PID_CREATE_DTM);
    }



    /**
     * <p>Returns the last save time (or <code>null</code>).</p>
     *
     * @return The last save time or <code>null</code>
     */
    public Date getLastSaveDateTime()
    {
        return (Date) getProperty(PropertyIDMap.PID_LASTSAVE_DTM);
    }



    /**
     * <p>Sets the total time spent in editing the document.</p>
     *
     * @param time The time to set.
     */
    public void setLastSaveDateTime(final Date time)
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s
                .setProperty(PropertyIDMap.PID_LASTSAVE_DTM,
                        Variant.VT_FILETIME, time);
    }



    /**
     * <p>Remove the total time spent in editing the document.</p>
     */
    public void removeLastSaveDateTime()
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.removeProperty(PropertyIDMap.PID_LASTSAVE_DTM);
    }



    /**
     * <p>Returns the page count or 0 if the {@link SummaryInformation} does
     * not contain a page count.</p>
     *
     * @return The page count or 0 if the {@link SummaryInformation} does not
     *         contain a page count.
     */
    public int getPageCount()
    {
        return getPropertyIntValue(PropertyIDMap.PID_PAGECOUNT);
    }



    /**
     * <p>Sets the page count.</p>
     *
     * @param pageCount The page count to set.
     */
    public void setPageCount(final int pageCount)
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.setProperty(PropertyIDMap.PID_PAGECOUNT, pageCount);
    }



    /**
     * <p>Removes the page count.</p>
     */
    public void removePageCount()
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.removeProperty(PropertyIDMap.PID_PAGECOUNT);
    }



    /**
     * <p>Returns the word count or 0 if the {@link SummaryInformation} does
     * not contain a word count.</p>
     *
     * @return The word count or <code>null</code>
     */
    public int getWordCount()
    {
        return getPropertyIntValue(PropertyIDMap.PID_WORDCOUNT);
    }



    /**
     * <p>Sets the word count.</p>
     *
     * @param wordCount The word count to set.
     */
    public void setWordCount(final int wordCount)
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.setProperty(PropertyIDMap.PID_WORDCOUNT, wordCount);
    }



    /**
     * <p>Removes the word count.</p>
     */
    public void removeWordCount()
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.removeProperty(PropertyIDMap.PID_WORDCOUNT);
    }



    /**
     * <p>Returns the character count or 0 if the {@link SummaryInformation}
     * does not contain a char count.</p>
     *
     * @return The character count or <code>null</code>
     */
    public int getCharCount()
    {
        return getPropertyIntValue(PropertyIDMap.PID_CHARCOUNT);
    }



    /**
     * <p>Sets the character count.</p>
     *
     * @param charCount The character count to set.
     */
    public void setCharCount(final int charCount)
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.setProperty(PropertyIDMap.PID_CHARCOUNT, charCount);
    }



    /**
     * <p>Removes the character count.</p>
     */
    public void removeCharCount()
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.removeProperty(PropertyIDMap.PID_CHARCOUNT);
    }



    /**
     * <p>Returns the thumbnail (or <code>null</code>) <strong>when this
     * method is implemented. Please note that the return type is likely to
     * change!</strong></p>
     *
     * <p><strong>Hint to developers:</strong> Drew Varner &lt;Drew.Varner
     * -at- sc.edu&gt; said that this is an image in WMF or Clipboard (BMP?)
     * format. However, we won't do any conversion into any image type but
     * instead just return a byte array.</p>
     *
     * @return The thumbnail or <code>null</code>
     */
    public byte[] getThumbnail()
    {
        return (byte[]) getProperty(PropertyIDMap.PID_THUMBNAIL);
    }



    /**
     * <p>Sets the thumbnail.</p>
     *
     * @param thumbnail The thumbnail to set.
     */
    public void setThumbnail(final byte[] thumbnail)
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.setProperty(PropertyIDMap.PID_THUMBNAIL, /* FIXME: */
                Variant.VT_LPSTR, thumbnail);
    }



    /**
     * <p>Removes the thumbnail.</p>
     */
    public void removeThumbnail()
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.removeProperty(PropertyIDMap.PID_THUMBNAIL);
    }



    /**
     * <p>Returns the application name (or <code>null</code>).</p>
     *
     * @return The application name or <code>null</code>
     */
    public String getApplicationName()
    {
        return getPropertyStringValue(PropertyIDMap.PID_APPNAME);
    }



    /**
     * <p>Sets the application name.</p>
     *
     * @param applicationName The application name to set.
     */
    public void setApplicationName(final String applicationName)
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.setProperty(PropertyIDMap.PID_APPNAME, applicationName);
    }



    /**
     * <p>Removes the application name.</p>
     */
    public void removeApplicationName()
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.removeProperty(PropertyIDMap.PID_APPNAME);
    }



    /**
     * <p>Returns a security code which is one of the following values:</p>
     *
     * <ul>
     *
     * <li><p>0 if the {@link SummaryInformation} does not contain a
     * security field or if there is no security on the document. Use
     * {@link PropertySet#wasNull()} to distinguish between the two
     * cases!</p></li>
     *
     * <li><p>1 if the document is password protected</p></li>
     *
     * <li><p>2 if the document is read-only recommended</p></li>
     *
     * <li><p>4 if the document is read-only enforced</p></li>
     *
     * <li><p>8 if the document is locked for annotations</p></li>
     *
     * </ul>
     *
     * @return The security code or <code>null</code>
     */
    public int getSecurity()
    {
        return getPropertyIntValue(PropertyIDMap.PID_SECURITY);
    }



    /**
     * <p>Sets the security code.</p>
     *
     * @param security The security code to set.
     */
    public void setSecurity(final int security)
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.setProperty(PropertyIDMap.PID_SECURITY, security);
    }



    /**
     * <p>Removes the security code.</p>
     */
    public void removeSecurity()
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.removeProperty(PropertyIDMap.PID_SECURITY);
    }

}
