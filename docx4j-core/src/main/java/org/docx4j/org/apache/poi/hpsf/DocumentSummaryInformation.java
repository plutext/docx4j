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

import static org.docx4j.org.apache.poi.hpsf.ClassIDPredefined.DOC_SUMMARY;
import static org.docx4j.org.apache.poi.hpsf.ClassIDPredefined.USER_PROPERTIES;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.docx4j.org.apache.poi.hpsf.wellknown.PropertyIDMap;

/**
 * Convenience class representing a DocumentSummary Information stream in a
 * Microsoft Office document.
 *
 * @see SummaryInformation
 */
@SuppressWarnings("unused")
public class DocumentSummaryInformation extends PropertySet {

    /**
     * The document name a document summary information stream
     * usually has in a POIFS filesystem.
     */
    public static final String DEFAULT_STREAM_NAME =
        "\005DocumentSummaryInformation";

    /**
     * The DocumentSummaryInformation's first and second sections' format ID.
     */
    public static final ClassID[] FORMAT_ID = {
        DOC_SUMMARY.getClassID(), USER_PROPERTIES.getClassID()
    };

    @Override
    public PropertyIDMap getPropertySetIDMap() {
        return PropertyIDMap.getDocumentSummaryInformationProperties();
    }


    /**
     * Creates an empty {@link DocumentSummaryInformation}.
     */
    public DocumentSummaryInformation() {
        getFirstSection().setFormatID(DOC_SUMMARY.getClassID());
    }


    /**
     * Creates a {@link DocumentSummaryInformation} from a given
     * {@link PropertySet}.
     *
     * @param ps A property set which should be created from a
     * document summary information stream.
     * @throws UnexpectedPropertySetTypeException if {@code ps}
     * does not contain a document summary information stream.
     */
    public DocumentSummaryInformation(final PropertySet ps)
    throws UnexpectedPropertySetTypeException {
        super(ps);
        if (!isDocumentSummaryInformation()) {
            throw new UnexpectedPropertySetTypeException("Not a " + getClass().getName());
        }
    }

    /**
     * Creates a {@link DocumentSummaryInformation} instance from an {@link
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
     */
    public DocumentSummaryInformation(final InputStream stream)
    throws NoPropertySetStreamException, IOException {
        super(stream);
    }

    /**
     * Returns the category (or {@code null}).
     *
     * @return The category value
     */
    public String getCategory() {
        return getPropertyStringValue(PropertyIDMap.PID_CATEGORY);
    }

    /**
     * Sets the category.
     *
     * @param category The category to set.
     */
    public void setCategory(final String category) {
        getFirstSection().setProperty(PropertyIDMap.PID_CATEGORY, category);
    }

    /**
     * Removes the category.
     */
    public void removeCategory() {
        remove1stProperty(PropertyIDMap.PID_CATEGORY);
    }



    /**
     * Returns the presentation format (or
     * {@code null}).
     *
     * @return The presentation format value
     */
    public String getPresentationFormat() {
        return getPropertyStringValue(PropertyIDMap.PID_PRESFORMAT);
    }

    /**
     * Sets the presentation format.
     *
     * @param presentationFormat The presentation format to set.
     */
    public void setPresentationFormat(final String presentationFormat) {
        getFirstSection().setProperty(PropertyIDMap.PID_PRESFORMAT, presentationFormat);
    }

    /**
     * Removes the presentation format.
     */
    public void removePresentationFormat() {
        remove1stProperty(PropertyIDMap.PID_PRESFORMAT);
    }



    /**
     * Returns the byte count or 0 if the {@link
     * DocumentSummaryInformation} does not contain a byte count.
     *
     * @return The byteCount value
     */
    public int getByteCount() {
        return getPropertyIntValue(PropertyIDMap.PID_BYTECOUNT);
    }

    /**
     * Sets the byte count.
     *
     * @param byteCount The byte count to set.
     */
    public void setByteCount(final int byteCount) {
        set1stProperty(PropertyIDMap.PID_BYTECOUNT, byteCount);
    }

    /**
     * Removes the byte count.
     */
    public void removeByteCount() {
        remove1stProperty(PropertyIDMap.PID_BYTECOUNT);
    }



    /**
     * Returns the line count or 0 if the {@link
     * DocumentSummaryInformation} does not contain a line count.
     *
     * @return The line count value
     */
    public int getLineCount() {
        return getPropertyIntValue(PropertyIDMap.PID_LINECOUNT);
    }

    /**
     * Sets the line count.
     *
     * @param lineCount The line count to set.
     */
    public void setLineCount(final int lineCount) {
        set1stProperty(PropertyIDMap.PID_LINECOUNT, lineCount);
    }

    /**
     * Removes the line count.
     */
    public void removeLineCount() {
        remove1stProperty(PropertyIDMap.PID_LINECOUNT);
    }



    /**
     * Returns the par count or 0 if the {@link
     * DocumentSummaryInformation} does not contain a par count.
     *
     * @return The par count value
     */
    public int getParCount() {
        return getPropertyIntValue(PropertyIDMap.PID_PARCOUNT);
    }

    /**
     * Sets the par count.
     *
     * @param parCount The par count to set.
     */
    public void setParCount(final int parCount) {
        set1stProperty(PropertyIDMap.PID_PARCOUNT, parCount);
    }

    /**
     * Removes the par count.
     */
    public void removeParCount() {
        remove1stProperty(PropertyIDMap.PID_PARCOUNT);
    }



    /**
     * Returns the slide count or 0 if the {@link
     * DocumentSummaryInformation} does not contain a slide count.
     *
     * @return The slide count value
     */
    public int getSlideCount() {
        return getPropertyIntValue(PropertyIDMap.PID_SLIDECOUNT);
    }

    /**
     * Sets the slideCount.
     *
     * @param slideCount The slide count to set.
     */
    public void setSlideCount(final int slideCount) {
        set1stProperty(PropertyIDMap.PID_SLIDECOUNT, slideCount);
    }

    /**
     * Removes the slide count.
     */
    public void removeSlideCount() {
        remove1stProperty(PropertyIDMap.PID_SLIDECOUNT);
    }



    /**
     * Returns the note count or 0 if the {@link
     * DocumentSummaryInformation} does not contain a note count.
     *
     * @return The note count value
     */
    public int getNoteCount() {
        return getPropertyIntValue(PropertyIDMap.PID_NOTECOUNT);
    }

    /**
     * Sets the note count.
     *
     * @param noteCount The note count to set.
     */
    public void setNoteCount(final int noteCount) {
        set1stProperty(PropertyIDMap.PID_NOTECOUNT, noteCount);
    }

    /**
     * Removes the noteCount.
     */
    public void removeNoteCount() {
        remove1stProperty(PropertyIDMap.PID_NOTECOUNT);
    }



    /**
     * Returns the hidden count or 0 if the {@link
     * DocumentSummaryInformation} does not contain a hidden
     * count.
     *
     * @return The hidden count value
     */
    public int getHiddenCount() {
        return getPropertyIntValue(PropertyIDMap.PID_HIDDENCOUNT);
    }

    /**
     * Sets the hidden count.
     *
     * @param hiddenCount The hidden count to set.
     */
    public void setHiddenCount(final int hiddenCount) {
        set1stProperty(PropertyIDMap.PID_HIDDENCOUNT, hiddenCount);
    }

    /**
     * Removes the hidden count.
     */
    public void removeHiddenCount() {
        remove1stProperty(PropertyIDMap.PID_HIDDENCOUNT);
    }



    /**
     * Returns the mmclip count or 0 if the {@link
     * DocumentSummaryInformation} does not contain a mmclip
     * count.
     *
     * @return The mmclip count value
     */
    public int getMMClipCount() {
        return getPropertyIntValue(PropertyIDMap.PID_MMCLIPCOUNT);
    }

    /**
     * Sets the mmclip count.
     *
     * @param mmClipCount The mmclip count to set.
     */
    public void setMMClipCount(final int mmClipCount) {
        set1stProperty(PropertyIDMap.PID_MMCLIPCOUNT, mmClipCount);
    }

    /**
     * Removes the mmclip count.
     */
    public void removeMMClipCount() {
        remove1stProperty(PropertyIDMap.PID_MMCLIPCOUNT);
    }



    /**
     * Returns {@code true} when scaling of the thumbnail is
     * desired, {@code false} if cropping is desired.
     *
     * @return The scale value
     */
    public boolean getScale() {
        return getPropertyBooleanValue(PropertyIDMap.PID_SCALE);
    }

    /**
     * Sets the scale.
     *
     * @param scale The scale to set.
     */
    public void setScale(final boolean scale) {
        set1stProperty(PropertyIDMap.PID_SCALE, scale);
    }

    /**
     * Removes the scale.
     */
    public void removeScale() {
        remove1stProperty(PropertyIDMap.PID_SCALE);
    }



    /**
     * <p>Returns the heading pair (or {@code null})
     * <strong>when this method is implemented. Please note that the
     * return type is likely to change!</strong>
     *
     * @return The heading pair value
     */
    public byte[] getHeadingPair() {
        notYetImplemented("Reading byte arrays ");
        return (byte[]) getProperty(PropertyIDMap.PID_HEADINGPAIR);
    }

    /**
     * Sets the heading pair.
     *
     * @param headingPair The heading pair to set.
     */
    public void setHeadingPair(final byte[] headingPair) {
        notYetImplemented("Writing byte arrays ");
    }

    /**
     * Removes the heading pair.
     */
    public void removeHeadingPair() {
        remove1stProperty(PropertyIDMap.PID_HEADINGPAIR);
    }



    /**
     * <p>Returns the doc parts (or {@code null})
     * <strong>when this method is implemented. Please note that the
     * return type is likely to change!</strong>
     *
     * @return The doc parts value
     */
    public byte[] getDocparts() {
        notYetImplemented("Reading byte arrays");
        return (byte[]) getProperty(PropertyIDMap.PID_DOCPARTS);
    }



    /**
     * Sets the doc parts.
     *
     * @param docparts The doc parts to set.
     */
    public void setDocparts(final byte[] docparts) {
        notYetImplemented("Writing byte arrays");
    }

    /**
     * Removes the doc parts.
     */
    public void removeDocparts() {
        remove1stProperty(PropertyIDMap.PID_DOCPARTS);
    }



    /**
     * Returns the manager (or {@code null}).
     *
     * @return The manager value
     */
    public String getManager() {
        return getPropertyStringValue(PropertyIDMap.PID_MANAGER);
    }

    /**
     * Sets the manager.
     *
     * @param manager The manager to set.
     */
    public void setManager(final String manager) {
        set1stProperty(PropertyIDMap.PID_MANAGER, manager);
    }

    /**
     * Removes the manager.
     */
    public void removeManager() {
        remove1stProperty(PropertyIDMap.PID_MANAGER);
    }



    /**
     * Returns the company (or {@code null}).
     *
     * @return The company value
     */
    public String getCompany() {
        return getPropertyStringValue(PropertyIDMap.PID_COMPANY);
    }

    /**
     * Sets the company.
     *
     * @param company The company to set.
     */
    public void setCompany(final String company) {
        set1stProperty(PropertyIDMap.PID_COMPANY, company);
    }

    /**
     * Removes the company.
     */
    public void removeCompany() {
        remove1stProperty(PropertyIDMap.PID_COMPANY);
    }


    /**
     * Returns {@code true} if the custom links are dirty.
     *
     * @return The links dirty value
     */
    public boolean getLinksDirty() {
        return getPropertyBooleanValue(PropertyIDMap.PID_LINKSDIRTY);
    }

    /**
     * Sets the linksDirty.
     *
     * @param linksDirty The links dirty value to set.
     */
    public void setLinksDirty(final boolean linksDirty) {
        set1stProperty(PropertyIDMap.PID_LINKSDIRTY, linksDirty);
    }

    /**
     * Removes the links dirty.
     */
    public void removeLinksDirty() {
        remove1stProperty(PropertyIDMap.PID_LINKSDIRTY);
    }


    /**
     * Returns the character count including whitespace, or 0 if the
     *  {@link DocumentSummaryInformation} does not contain this char count.
     * <p>This is the whitespace-including version of {@link SummaryInformation#getCharCount()}
     *
     * @return The character count or {@code null}
     */
    public int getCharCountWithSpaces() {
        return getPropertyIntValue(PropertyIDMap.PID_CCHWITHSPACES);
    }

    /**
     * Sets the character count including whitespace
     *
     * @param count The character count to set.
     */
    public void setCharCountWithSpaces(int count) {
        set1stProperty(PropertyIDMap.PID_CCHWITHSPACES, count);
    }

    /**
     * Removes the character count
     */
    public void removeCharCountWithSpaces() {
        remove1stProperty(PropertyIDMap.PID_CCHWITHSPACES);
    }


    /**
     * Get if the User Defined Property Set has been updated outside of the
     * Application.<p>
     * If it has (true), the hyperlinks should be updated on document load.
     *
     * @return true, if the hyperlinks should be updated on document load
     */
    public boolean getHyperlinksChanged() {
        return getPropertyBooleanValue(PropertyIDMap.PID_HYPERLINKSCHANGED);
    }

    /**
     * Set the flag for if the User Defined Property Set has been updated outside
     *  of the Application.
     *
     * @param changed true, if the User Defined Property Set has been updated
     */
    public void setHyperlinksChanged(boolean changed) {
        set1stProperty(PropertyIDMap.PID_HYPERLINKSCHANGED, changed);
    }

    /**
     * Removes the flag for if the User Defined Property Set has been updated
     *  outside of the Application.
     */
    public void removeHyperlinksChanged() {
        remove1stProperty(PropertyIDMap.PID_HYPERLINKSCHANGED);
    }


    /**
     * Gets the version of the Application which wrote the
     *  Property set, stored with the two high order bytes having the major
     *  version number, and the two low order bytes the minor version number.<p>
     * This will be 0 if no version is set.
     *
     * @return the Application version
     */
    public int getApplicationVersion() {
        return getPropertyIntValue(PropertyIDMap.PID_VERSION);
    }

    /**
     * Sets the Application version, which must be a 4 byte int with
     *  the  two high order bytes having the major version number, and the
     *  two low order bytes the minor version number.
     *
     * @param version the Application version
     */
    public void setApplicationVersion(int version) {
        set1stProperty(PropertyIDMap.PID_VERSION, version);
    }

    /**
     * Removes the Application Version
     */
    public void removeApplicationVersion() {
        remove1stProperty(PropertyIDMap.PID_VERSION);
    }


    /**
     * Returns the VBA digital signature for the VBA project
     * embedded in the document (or {@code null}).
     *
     * @return the VBA digital signature
     */
    public byte[] getVBADigitalSignature() {
        Object value = getProperty(PropertyIDMap.PID_DIGSIG);
        return (value instanceof byte[]) ? (byte[])value : null;
    }

    /**
     * Sets the VBA digital signature for the VBA project
     *  embedded in the document.
     *
     * @param signature VBA Digital Signature for the project
     */
    public void setVBADigitalSignature(byte[] signature) {
        set1stProperty(PropertyIDMap.PID_DIGSIG, signature);
    }

    /**
     * Removes the VBA Digital Signature
     */
    public void removeVBADigitalSignature() {
        remove1stProperty(PropertyIDMap.PID_DIGSIG);
    }


    /**
     * Gets the content type of the file (or {@code null}).
     *
     * @return the content type of the file
     */
    public String getContentType() {
        return getPropertyStringValue(PropertyIDMap.PID_CONTENTTYPE);
    }

    /**
     * Sets the content type of the file
     *
     * @param type the content type of the file
     */
    public void setContentType(String type) {
        set1stProperty(PropertyIDMap.PID_CONTENTTYPE, type);
    }

    /**
     * Removes the content type of the file
     */
    public void removeContentType() {
        remove1stProperty(PropertyIDMap.PID_CONTENTTYPE);
    }


    /**
     * Gets the content status of the file (or {@code null}).
     *
     * @return the content status of the file
     */
    public String getContentStatus() {
        return getPropertyStringValue(PropertyIDMap.PID_CONTENTSTATUS);
    }

    /**
     * Sets the content status of the file
     *
     * @param status the content status of the file
     */
    public void setContentStatus(String status) {
        set1stProperty(PropertyIDMap.PID_CONTENTSTATUS, status);
    }

    /**
     * Removes the content status of the file
     */
    public void removeContentStatus() {
        remove1stProperty(PropertyIDMap.PID_CONTENTSTATUS);
    }


    /**
     * Gets the document language, which is normally unset and empty (or {@code null}).
     *
     * @return the document language
     */
    public String getLanguage() {
        return getPropertyStringValue(PropertyIDMap.PID_LANGUAGE);
    }

    /**
     * Set the document language
     *
     * @param language the document language
     */
    public void setLanguage(String language) {
        set1stProperty(PropertyIDMap.PID_LANGUAGE, language);
    }

    /**
     * Removes the document language
     */
    public void removeLanguage() {
        remove1stProperty(PropertyIDMap.PID_LANGUAGE);
    }


    /**
     * Gets the document version as a string, which is normally unset and empty
     *  (or {@code null}).
     *
     *  @return the document verion
     */
    public String getDocumentVersion() {
        return getPropertyStringValue(PropertyIDMap.PID_DOCVERSION);
    }

    /**
     * Sets the document version string
     *
     * @param version the document version string
     */
    public void setDocumentVersion(String version) {
        set1stProperty(PropertyIDMap.PID_DOCVERSION, version);
    }

    /**
     * Removes the document version string
     */
    public void removeDocumentVersion() {
        remove1stProperty(PropertyIDMap.PID_DOCVERSION);
    }


    /**
     * Gets the custom properties.
     *
     * @return The custom properties.
     */
    public CustomProperties getCustomProperties() {
        CustomProperties cps = null;
        if (getSectionCount() >= 2) {
            cps = new CustomProperties();
            final Section section = getSections().get(1);
            final Map<Long,String> dictionary = section.getDictionary();
            final Property[] properties = section.getProperties();
            int propertyCount = 0;
            for (Property p : properties) {
                final long id = p.getID();
                if (id == PropertyIDMap.PID_CODEPAGE) {
                    cps.setCodepage((Integer)p.getValue());
                } else if (id > PropertyIDMap.PID_CODEPAGE && dictionary != null) {
                    propertyCount++;
                    final CustomProperty cp = new CustomProperty(p, dictionary.get(id));
                    cps.put(cp.getName(), cp);
                }
            }
            if (cps.size() != propertyCount) {
                cps.setPure(false);
            }
        }
        return cps;
    }

    /**
     * Sets the custom properties.
     *
     * @param customProperties The custom properties
     */
    public void setCustomProperties(final CustomProperties customProperties) {
        ensureSection2();
        final Section section = getSections().get(1);
        final Map<Long,String> dictionary = customProperties.getDictionary();
        // section.clear();

        /* Set the codepage. If both custom properties and section have a
         * codepage, the codepage from the custom properties wins, else take the
         * one that is defined. If none is defined, take ISO-8859-1. */
        int cpCodepage = customProperties.getCodepage();
        if (cpCodepage < 0) {
            cpCodepage = section.getCodepage();
        }
        if (cpCodepage < 0) {
            cpCodepage = Property.DEFAULT_CODEPAGE;
        }
        customProperties.setCodepage(cpCodepage);
        section.setCodepage(cpCodepage);
        section.setDictionary(dictionary);
        for (CustomProperty p : customProperties.properties()) {
            section.setProperty(p);
        }
    }

    /**
     * Creates section 2 if it is not already present.
     */
    private void ensureSection2() {
        if (getSectionCount() < 2) {
            Section s2 = new Section();
            s2.setFormatID(USER_PROPERTIES.getClassID());
            addSection(s2);
        }
    }

    /**
     * Removes the custom properties.
     */
    public void removeCustomProperties() {
        if (getSectionCount() < 2) {
            throw new HPSFRuntimeException("Illegal internal format of Document SummaryInformation stream: second section is missing.");
        }

        List<Section> l = new LinkedList<>(getSections());
        clearSections();
        int idx = 0;
        for (Section s : l) {
            if (idx++ != 1) {
                addSection(s);
            }
        }
    }

    /**
     * Throws an {@link UnsupportedOperationException} with a message text
     * telling which functionality is not yet implemented.
     *
     * @param msg text telling was leaves to be implemented, e.g.
     * "Reading byte arrays".
     */
    private void notYetImplemented(final String msg) {
        throw new UnsupportedOperationException(msg + " is not yet implemented.");
    }
}