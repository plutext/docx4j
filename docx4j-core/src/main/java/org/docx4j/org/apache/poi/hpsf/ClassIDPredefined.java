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

package org.apache.poi.hpsf;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("java:S1192")
public enum ClassIDPredefined {
    /** OLE 1.0 package manager */
    OLE_V1_PACKAGE       ("{0003000C-0000-0000-C000-000000000046}", ".bin", null),
    /** Excel V3 - document */
    EXCEL_V3             ("{00030000-0000-0000-C000-000000000046}", ".xls", "application/vnd.ms-excel"),
    /** Excel V3 - chart */
    EXCEL_V3_CHART       ("{00030001-0000-0000-C000-000000000046}", null, null),
    /** Excel V3 - macro */
    EXCEL_V3_MACRO       ("{00030002-0000-0000-C000-000000000046}", null, null),
    /** Excel V7 / 95 - document */
    EXCEL_V7             ("{00020810-0000-0000-C000-000000000046}", ".xls", "application/vnd.ms-excel"),
    /** Excel V7 / 95 - workbook */
    EXCEL_V7_WORKBOOK    ("{00020841-0000-0000-C000-000000000046}", null, null),
    /** Excel V7 / 95 - chart */
    EXCEL_V7_CHART       ("{00020811-0000-0000-C000-000000000046}", null, null),
    /** Excel V8 / 97 - document */
    EXCEL_V8             ("{00020820-0000-0000-C000-000000000046}", ".xls", "application/vnd.ms-excel"),
    /** Excel V8 / 97 - chart */
    EXCEL_V8_CHART       ("{00020821-0000-0000-C000-000000000046}", null, null),
    /** Excel V11 / 2003 - document */
    EXCEL_V11            ("{00020812-0000-0000-C000-000000000046}", ".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    /** Excel V12 / 2007 - document */
    EXCEL_V12            ("{00020830-0000-0000-C000-000000000046}", ".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    /** Excel V12 / 2007 - macro */
    EXCEL_V12_MACRO      ("{00020832-0000-0000-C000-000000000046}", ".xlsm", "application/vnd.ms-excel.sheet.macroEnabled.12"),
    /** Excel V12 / 2007 - xlsb document */
    EXCEL_V12_XLSB       ("{00020833-0000-0000-C000-000000000046}", ".xlsb", "application/vnd.ms-excel.sheet.binary.macroEnabled.12"),
    /* Excel V14 / 2010 - document */
    EXCEL_V14            ("{00024500-0000-0000-C000-000000000046}", ".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    /* Excel V14 / 2010 - workbook */
    EXCEL_V14_WORKBOOK   ("{000208D5-0000-0000-C000-000000000046}", null, null),
    /* Excel V14 / 2010 - chart */
    EXCEL_V14_CHART      ("{00024505-0014-0000-C000-000000000046}", null, null),
    /** Excel V14 / 2010 - OpenDocument spreadsheet */
    EXCEL_V14_ODS        ("{EABCECDB-CC1C-4A6F-B4E3-7F888A5ADFC8}", ".ods", "application/vnd.oasis.opendocument.spreadsheet"),
    /** Word V7 / 95 - document */
    WORD_V7              ("{00020900-0000-0000-C000-000000000046}", ".doc", "application/msword"),
    /** Word V8 / 97 - document */
    WORD_V8              ("{00020906-0000-0000-C000-000000000046}", ".doc", "application/msword"),
    /** Word V12 / 2007 - document */
    WORD_V12             ("{F4754C9B-64F5-4B40-8AF4-679732AC0607}", ".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    /** Word V12 / 2007 - macro */
    WORD_V12_MACRO       ("{18A06B6B-2F3F-4E2B-A611-52BE631B2D22}", ".docm", "application/vnd.ms-word.document.macroEnabled.12"),
    /** Powerpoint V7 / 95 - document */
    POWERPOINT_V7        ("{EA7BAE70-FB3B-11CD-A903-00AA00510EA3}", ".ppt", "application/vnd.ms-powerpoint"),
    /** Powerpoint V7 / 95 - slide */
    POWERPOINT_V7_SLIDE  ("{EA7BAE71-FB3B-11CD-A903-00AA00510EA3}", null, null),
    /** Powerpoint V8 / 97 - document */
    POWERPOINT_V8        ("{64818D10-4F9B-11CF-86EA-00AA00B929E8}", ".ppt", "application/vnd.ms-powerpoint"),
    /** Powerpoint V8 / 97 - template */
    POWERPOINT_V8_TPL    ("{64818D11-4F9B-11CF-86EA-00AA00B929E8}", ".pot", "application/vnd.ms-powerpoint"),
    /** Powerpoint V12 / 2007 - document */
    POWERPOINT_V12       ("{CF4F55F4-8F87-4D47-80BB-5808164BB3F8}", ".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"),
    /** Powerpoint V12 / 2007 - macro */
    POWERPOINT_V12_MACRO ("{DC020317-E6E2-4A62-B9FA-B3EFE16626F4}", ".pptm", "application/vnd.ms-powerpoint.presentation.macroEnabled.12"),
    /** Publisher V12 */
    PUBLISHER_V12        ("{0002123D-0000-0000-C000-000000000046}", ".pub", "application/x-mspublisher"),
    /** Visio 2000 (V6) / 2002 (V10) - drawing */
    VISIO_V10            ("{00021A14-0000-0000-C000-000000000046}", ".vsd", "application/vnd.visio"),
    /** Equation Editor 3.0 */
    EQUATION_V3          ("{0002CE02-0000-0000-C000-000000000046}", null, null),
    /** AcroExch.Document */
    PDF                  ("{B801CA65-A1FC-11D0-85AD-444553540000}", ".pdf", "application/pdf"),
    /** Plain Text Persistent Handler **/
    TXT_ONLY             ("{5e941d80-bf96-11cd-b579-08002b30bfeb}", ".txt", "text/plain"),
    /** Microsoft Paint **/
    PAINT                ("{0003000A-0000-0000-C000-000000000046}", null, null),
    /** Standard Hyperlink / STD Moniker **/
    STD_MONIKER          ("{79EAC9D0-BAF9-11CE-8C82-00AA004BA90B}", null, null),
    /** URL Moniker **/
    URL_MONIKER          ("{79EAC9E0-BAF9-11CE-8C82-00AA004BA90B}", null, null),
    /** File Moniker **/
    FILE_MONIKER         ("{00000303-0000-0000-C000-000000000046}", null, null),
    /** Document summary information first property section **/
    DOC_SUMMARY          ("{D5CDD502-2E9C-101B-9397-08002B2CF9AE}", null, null),
    /** Document summary information user defined properties section **/
    USER_PROPERTIES      ("{D5CDD505-2E9C-101B-9397-08002B2CF9AE}", null, null),
    /** Summary information property section **/
    SUMMARY_PROPERTIES   ("{F29F85E0-4FF9-1068-AB91-08002B27B3D9}", null, null);
    ;


    private static final Map<String,ClassIDPredefined> LOOKUP = new HashMap<>();

    static {
        for (ClassIDPredefined p : values()) {
            LOOKUP.put(p.externalForm, p);
        }
    }

    private final String externalForm;
    private ClassID classId;
    private final String fileExtension;
    private final String contentType;

    ClassIDPredefined(final String externalForm, final String fileExtension, final String contentType) {
        this.externalForm = externalForm;
        this.fileExtension = fileExtension;
        this.contentType = contentType;
    }

    public ClassID getClassID() {
        synchronized (this) {
            // TODO: init classId directly in the constructor when old statics have been removed from ClassID
            if (classId == null) {
                classId = new ClassID(externalForm);
            }
        }
        return classId;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public String getContentType() {
        return contentType;
    }

    public static ClassIDPredefined lookup(final String externalForm) {
        return LOOKUP.get(externalForm);
    }

    public static ClassIDPredefined lookup(final ClassID classID) {
        return (classID == null) ? null : LOOKUP.get(classID.toString());
    }

    @SuppressWarnings("java:S1201")
    public boolean equals(ClassID classID) {
        return getClassID().equals(classID);
    }
}
