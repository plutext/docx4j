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

import java.util.Iterator;
import java.util.Map;

import org.docx4j.org.apache.poi.hpsf.wellknown.PropertyIDMap;
import org.docx4j.org.apache.poi.hpsf.wellknown.SectionIDMap;
import org.docx4j.org.apache.poi.util.CodePageUtil;

/**
 * <p>Convenience class representing a DocumentSummary Information stream in a
 * Microsoft Office document.</p>
 *
 * @see SummaryInformation
 */
public class DocumentSummaryInformation extends SpecialPropertySet
{
    /**
     * <p>The document name a document summary information stream
     * usually has in a POIFS filesystem.</p>
     */
    public static final String DEFAULT_STREAM_NAME =
        "\005DocumentSummaryInformation";

    public PropertyIDMap getPropertySetIDMap() {
    	return PropertyIDMap.getDocumentSummaryInformationProperties();
    }


    /**
     * <p>Creates a {@link DocumentSummaryInformation} from a given
     * {@link PropertySet}.</p>
     *
     * @param ps A property set which should be created from a
     * document summary information stream.
     * @throws UnexpectedPropertySetTypeException if <var>ps</var>
     * does not contain a document summary information stream.
     */
    public DocumentSummaryInformation(final PropertySet ps)
        throws UnexpectedPropertySetTypeException
    {
        super(ps);
        if (!isDocumentSummaryInformation())
            throw new UnexpectedPropertySetTypeException
                ("Not a " + getClass().getName());
    }

    
    /**
     * <p>Returns the category (or <code>null</code>).</p>
     *
     * @return The category value
     */
    public String getCategory()
    {
        return getPropertyStringValue(PropertyIDMap.PID_CATEGORY);
    }

    /**
     * <p>Sets the category.</p>
     *
     * @param category The category to set.
     */
    public void setCategory(final String category)
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.setProperty(PropertyIDMap.PID_CATEGORY, category);
    }

    /**
     * <p>Removes the category.</p>
     */
    public void removeCategory()
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.removeProperty(PropertyIDMap.PID_CATEGORY);
    }



    /**
     * <p>Returns the presentation format (or
     * <code>null</code>).</p>
     *
     * @return The presentation format value
     */
    public String getPresentationFormat()
    {
        return getPropertyStringValue(PropertyIDMap.PID_PRESFORMAT);
    }

    /**
     * <p>Sets the presentation format.</p>
     *
     * @param presentationFormat The presentation format to set.
     */
    public void setPresentationFormat(final String presentationFormat)
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.setProperty(PropertyIDMap.PID_PRESFORMAT, presentationFormat);
    }

    /**
     * <p>Removes the presentation format.</p>
     */
    public void removePresentationFormat()
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.removeProperty(PropertyIDMap.PID_PRESFORMAT);
    }



    /**
     * <p>Returns the byte count or 0 if the {@link
     * DocumentSummaryInformation} does not contain a byte count.</p>
     *
     * @return The byteCount value
     */
    public int getByteCount()
    {
        return getPropertyIntValue(PropertyIDMap.PID_BYTECOUNT);
    }

    /**
     * <p>Sets the byte count.</p>
     *
     * @param byteCount The byte count to set.
     */
    public void setByteCount(final int byteCount)
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.setProperty(PropertyIDMap.PID_BYTECOUNT, byteCount);
    }

    /**
     * <p>Removes the byte count.</p>
     */
    public void removeByteCount()
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.removeProperty(PropertyIDMap.PID_BYTECOUNT);
    }



    /**
     * <p>Returns the line count or 0 if the {@link
     * DocumentSummaryInformation} does not contain a line count.</p>
     *
     * @return The line count value
     */
    public int getLineCount()
    {
        return getPropertyIntValue(PropertyIDMap.PID_LINECOUNT);
    }

    /**
     * <p>Sets the line count.</p>
     *
     * @param lineCount The line count to set.
     */
    public void setLineCount(final int lineCount)
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.setProperty(PropertyIDMap.PID_LINECOUNT, lineCount);
    }

    /**
     * <p>Removes the line count.</p>
     */
    public void removeLineCount()
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.removeProperty(PropertyIDMap.PID_LINECOUNT);
    }



    /**
     * <p>Returns the par count or 0 if the {@link
     * DocumentSummaryInformation} does not contain a par count.</p>
     *
     * @return The par count value
     */
    public int getParCount()
    {
        return getPropertyIntValue(PropertyIDMap.PID_PARCOUNT);
    }

    /**
     * <p>Sets the par count.</p>
     *
     * @param parCount The par count to set.
     */
    public void setParCount(final int parCount)
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.setProperty(PropertyIDMap.PID_PARCOUNT, parCount);
    }

    /**
     * <p>Removes the par count.</p>
     */
    public void removeParCount()
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.removeProperty(PropertyIDMap.PID_PARCOUNT);
    }



    /**
     * <p>Returns the slide count or 0 if the {@link
     * DocumentSummaryInformation} does not contain a slide count.</p>
     *
     * @return The slide count value
     */
    public int getSlideCount()
    {
        return getPropertyIntValue(PropertyIDMap.PID_SLIDECOUNT);
    }

    /**
     * <p>Sets the slideCount.</p>
     *
     * @param slideCount The slide count to set.
     */
    public void setSlideCount(final int slideCount)
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.setProperty(PropertyIDMap.PID_SLIDECOUNT, slideCount);
    }

    /**
     * <p>Removes the slide count.</p>
     */
    public void removeSlideCount()
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.removeProperty(PropertyIDMap.PID_SLIDECOUNT);
    }



    /**
     * <p>Returns the note count or 0 if the {@link
     * DocumentSummaryInformation} does not contain a note count.</p>
     *
     * @return The note count value
     */
    public int getNoteCount()
    {
        return getPropertyIntValue(PropertyIDMap.PID_NOTECOUNT);
    }

    /**
     * <p>Sets the note count.</p>
     *
     * @param noteCount The note count to set.
     */
    public void setNoteCount(final int noteCount)
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.setProperty(PropertyIDMap.PID_NOTECOUNT, noteCount);
    }

    /**
     * <p>Removes the noteCount.</p>
     */
    public void removeNoteCount()
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.removeProperty(PropertyIDMap.PID_NOTECOUNT);
    }



    /**
     * <p>Returns the hidden count or 0 if the {@link
     * DocumentSummaryInformation} does not contain a hidden
     * count.</p>
     *
     * @return The hidden count value
     */
    public int getHiddenCount()
    {
        return getPropertyIntValue(PropertyIDMap.PID_HIDDENCOUNT);
    }

    /**
     * <p>Sets the hidden count.</p>
     *
     * @param hiddenCount The hidden count to set.
     */
    public void setHiddenCount(final int hiddenCount)
    {
        final MutableSection s = (MutableSection) getSections().get(0);
        s.setProperty(PropertyIDMap.PID_HIDDENCOUNT, hiddenCount);
    }

    /**
     * <p>Removes the hidden count.</p>
     */
    public void removeHiddenCount()
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.removeProperty(PropertyIDMap.PID_HIDDENCOUNT);
    }



    /**
     * <p>Returns the mmclip count or 0 if the {@link
     * DocumentSummaryInformation} does not contain a mmclip
     * count.</p>
     *
     * @return The mmclip count value
     */
    public int getMMClipCount()
    {
        return getPropertyIntValue(PropertyIDMap.PID_MMCLIPCOUNT);
    }

    /**
     * <p>Sets the mmclip count.</p>
     *
     * @param mmClipCount The mmclip count to set.
     */
    public void setMMClipCount(final int mmClipCount)
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.setProperty(PropertyIDMap.PID_MMCLIPCOUNT, mmClipCount);
    }

    /**
     * <p>Removes the mmclip count.</p>
     */
    public void removeMMClipCount()
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.removeProperty(PropertyIDMap.PID_MMCLIPCOUNT);
    }



    /**
     * <p>Returns <code>true</code> when scaling of the thumbnail is
     * desired, <code>false</code> if cropping is desired.</p>
     *
     * @return The scale value
     */
    public boolean getScale()
    {
        return getPropertyBooleanValue(PropertyIDMap.PID_SCALE);
    }

    /**
     * <p>Sets the scale.</p>
     *
     * @param scale The scale to set.
     */
    public void setScale(final boolean scale)
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.setProperty(PropertyIDMap.PID_SCALE, scale);
    }

    /**
     * <p>Removes the scale.</p>
     */
    public void removeScale()
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.removeProperty(PropertyIDMap.PID_SCALE);
    }



    /**
     * <p>Returns the heading pair (or <code>null</code>)
     * <strong>when this method is implemented. Please note that the
     * return type is likely to change!</strong>
     *
     * @return The heading pair value
     */
    public byte[] getHeadingPair()
    {
        notYetImplemented("Reading byte arrays ");
        return (byte[]) getProperty(PropertyIDMap.PID_HEADINGPAIR);
    }

    /**
     * <p>Sets the heading pair.</p>
     *
     * @param headingPair The heading pair to set.
     */
    public void setHeadingPair(final byte[] headingPair)
    {
        notYetImplemented("Writing byte arrays ");
    }

    /**
     * <p>Removes the heading pair.</p>
     */
    public void removeHeadingPair()
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.removeProperty(PropertyIDMap.PID_HEADINGPAIR);
    }



    /**
     * <p>Returns the doc parts (or <code>null</code>)
     * <strong>when this method is implemented. Please note that the
     * return type is likely to change!</strong>
     *
     * @return The doc parts value
     */
    public byte[] getDocparts()
    {
        notYetImplemented("Reading byte arrays");
        return (byte[]) getProperty(PropertyIDMap.PID_DOCPARTS);
    }



    /**
     * <p>Sets the doc parts.</p>
     *
     * @param docparts The doc parts to set.
     */
    public void setDocparts(final byte[] docparts)
    {
        notYetImplemented("Writing byte arrays");
    }

    /**
     * <p>Removes the doc parts.</p>
     */
    public void removeDocparts()
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.removeProperty(PropertyIDMap.PID_DOCPARTS);
    }



    /**
     * <p>Returns the manager (or <code>null</code>).</p>
     *
     * @return The manager value
     */
    public String getManager()
    {
        return getPropertyStringValue(PropertyIDMap.PID_MANAGER);
    }

    /**
     * <p>Sets the manager.</p>
     *
     * @param manager The manager to set.
     */
    public void setManager(final String manager)
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.setProperty(PropertyIDMap.PID_MANAGER, manager);
    }

    /**
     * <p>Removes the manager.</p>
     */
    public void removeManager()
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.removeProperty(PropertyIDMap.PID_MANAGER);
    }



    /**
     * <p>Returns the company (or <code>null</code>).</p>
     *
     * @return The company value
     */
    public String getCompany()
    {
        return getPropertyStringValue(PropertyIDMap.PID_COMPANY);
    }

    /**
     * <p>Sets the company.</p>
     *
     * @param company The company to set.
     */
    public void setCompany(final String company)
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.setProperty(PropertyIDMap.PID_COMPANY, company);
    }

    /**
     * <p>Removes the company.</p>
     */
    public void removeCompany()
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.removeProperty(PropertyIDMap.PID_COMPANY);
    }


    /**
     * <p>Returns <code>true</code> if the custom links are dirty.</p> <p>
     *
     * @return The links dirty value
     */
    public boolean getLinksDirty()
    {
        return getPropertyBooleanValue(PropertyIDMap.PID_LINKSDIRTY);
    }

    /**
     * <p>Sets the linksDirty.</p>
     *
     * @param linksDirty The links dirty value to set.
     */
    public void setLinksDirty(final boolean linksDirty)
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.setProperty(PropertyIDMap.PID_LINKSDIRTY, linksDirty);
    }

    /**
     * <p>Removes the links dirty.</p>
     */
    public void removeLinksDirty()
    {
        final MutableSection s = (MutableSection) getFirstSection();
        s.removeProperty(PropertyIDMap.PID_LINKSDIRTY);
    }


    /**
     * <p>Gets the custom properties.</p>
     *
     * @return The custom properties.
     */
    public CustomProperties getCustomProperties()
    {
        CustomProperties cps = null;
        if (getSectionCount() >= 2)
        {
            cps = new CustomProperties();
            final Section section = getSections().get(1);
            final Map<Long,String> dictionary = section.getDictionary();
            final Property[] properties = section.getProperties();
            int propertyCount = 0;
            for (int i = 0; i < properties.length; i++)
            {
                final Property p = properties[i];
                final long id = p.getID();
                if (id != 0 && id != 1)
                {
                    propertyCount++;
                    final CustomProperty cp = new CustomProperty(p,
                            dictionary.get(Long.valueOf(id)));
                    cps.put(cp.getName(), cp);
                }
            }
            if (cps.size() != propertyCount)
                cps.setPure(false);
        }
        return cps;
    }

    /**
     * <p>Sets the custom properties.</p>
     *
     * @param customProperties The custom properties
     */
    public void setCustomProperties(final CustomProperties customProperties)
    {
        ensureSection2();
        final MutableSection section = (MutableSection) getSections().get(1);
        final Map<Long,String> dictionary = customProperties.getDictionary();
        section.clear();

        /* Set the codepage. If both custom properties and section have a
         * codepage, the codepage from the custom properties wins, else take the
         * one that is defined. If none is defined, take Unicode. */
        int cpCodepage = customProperties.getCodepage();
        if (cpCodepage < 0)
            cpCodepage = section.getCodepage();
        if (cpCodepage < 0)
            cpCodepage = CodePageUtil.CP_UNICODE;
        customProperties.setCodepage(cpCodepage);
        section.setCodepage(cpCodepage);
        section.setDictionary(dictionary);
        for (final Iterator<CustomProperty> i = customProperties.values().iterator(); i.hasNext();)
        {
            final Property p = i.next();
            section.setProperty(p);
        }
    }

    /**
     * <p>Creates section 2 if it is not already present.</p>
     *
     */
    private void ensureSection2()
    {
        if (getSectionCount() < 2)
        {
            MutableSection s2 = new MutableSection();
            s2.setFormatID(SectionIDMap.DOCUMENT_SUMMARY_INFORMATION_ID[1]);
            addSection(s2);
        }
    }

    /**
     * <p>Removes the custom properties.</p>
     */
    public void removeCustomProperties()
    {
        if (getSectionCount() >= 2)
            getSections().remove(1);
        else
            throw new HPSFRuntimeException("Illegal internal format of Document SummaryInformation stream: second section is missing.");
    }


    /**
     * <p>Throws an {@link UnsupportedOperationException} with a message text
     * telling which functionality is not yet implemented.</p>
     *
     * @param msg text telling was leaves to be implemented, e.g.
     * "Reading byte arrays".
     */
    private void notYetImplemented(final String msg)
    {
        throw new UnsupportedOperationException(msg + " is not yet implemented.");
    }
}
