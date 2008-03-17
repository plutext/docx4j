/*
 *  Copyright 2007, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is free software: you can use it, redistribute it and/or modify
    it under the terms of version 3 of the GNU Affero General Public License 
    as published by the Free Software Foundation.

    If you need the right to use it under a different license, please
    contact Plutext.

    docx4j is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License   
    along with docx4j.  If not, see <http://www.fsf.org/licensing/licenses/>.
    
 */

package org.docx4j.dml;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.dml package. 
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


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.dml
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link BaseStyles.FmtScheme }
     * 
     */
    public BaseStyles.FmtScheme createBaseStylesFmtScheme() {
        return new BaseStyles.FmtScheme();
    }

    /**
     * Create an instance of {@link BaseStyles.ExtLst }
     * 
     */
    public BaseStyles.ExtLst createBaseStylesExtLst() {
        return new BaseStyles.ExtLst();
    }

    /**
     * Create an instance of {@link BaseStyles.FontScheme }
     * 
     */
    public BaseStyles.FontScheme createBaseStylesFontScheme() {
        return new BaseStyles.FontScheme();
    }

    /**
     * Create an instance of {@link FontCollection }
     * 
     */
    public FontCollection createFontCollection() {
        return new FontCollection();
    }

    /**
     * Create an instance of {@link Theme.ExtLst }
     * 
     */
    public Theme.ExtLst createThemeExtLst() {
        return new Theme.ExtLst();
    }

    /**
     * Create an instance of {@link BaseStyles.ClrScheme }
     * 
     */
    public BaseStyles.ClrScheme createBaseStylesClrScheme() {
        return new BaseStyles.ClrScheme();
    }

    /**
     * Create an instance of {@link BaseStyles }
     * 
     */
    public BaseStyles createBaseStyles() {
        return new BaseStyles();
    }

    /**
     * Create an instance of {@link Theme.ObjectDefaults }
     * 
     */
    public Theme.ObjectDefaults createThemeObjectDefaults() {
        return new Theme.ObjectDefaults();
    }

    /**
     * Create an instance of {@link Theme.ExtraClrSchemeLst }
     * 
     */
    public Theme.ExtraClrSchemeLst createThemeExtraClrSchemeLst() {
        return new Theme.ExtraClrSchemeLst();
    }

    /**
     * Create an instance of {@link BaseStyles.FontScheme.ExtLst }
     * 
     */
    public BaseStyles.FontScheme.ExtLst createBaseStylesFontSchemeExtLst() {
        return new BaseStyles.FontScheme.ExtLst();
    }

    /**
     * Create an instance of {@link Theme.CustClrLst }
     * 
     */
    public Theme.CustClrLst createThemeCustClrLst() {
        return new Theme.CustClrLst();
    }

    /**
     * Create an instance of {@link TextFont }
     * 
     */
    public TextFont createTextFont() {
        return new TextFont();
    }

    /**
     * Create an instance of {@link FontCollection.Font }
     * 
     */
    public FontCollection.Font createFontCollectionFont() {
        return new FontCollection.Font();
    }

    /**
     * Create an instance of {@link Theme }
     * 
     */
    public Theme createTheme() {
        return new Theme();
    }

}
