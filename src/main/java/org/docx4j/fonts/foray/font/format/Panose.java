/*
 * Copyright 2006 The FOray Project.
 *      http://www.foray.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This work is in part derived from the following work(s), used with the
 * permission of the licensor:
 *      Apache FOP, licensed by the Apache Software Foundation
 *
 */

/*
 * $LastChangedRevision: 10482 $
 * $LastChangedDate: 2008-03-23 02:59:39 +1100 (Sun, 23 Mar 2008) $
 * $LastChangedBy$
 */

/* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
 * 
 * This notice is included to meet the condition in clause 4(b) of the License. 
 */

//package org.foray.font.format;
package org.docx4j.fonts.foray.font.format;

import java.io.Serializable;



/**
 * A PANOSE-1 classification number.
 *
 * <p>References:</p>
 * <ul>
 *   <li><a href="http://fonts.apple.com/TTRefMan/RM06/Chap6OS2.html">The TTF OS/2 Table
 * doc</a></li>
 *   <li><a href="http://www.w3.org/Fonts/Panose/pan2.html#StaticDigits">Panose 2.0 White
 * Paper</a></li>
 *   <li><a href="http://www.byte.com/art/9405/sec12/art1.htm">The Panose Typeface-Matching
 * System</a></li>
 *   <li>Michael S. De Laurentis, PANOSE 1.0 Core Mapper Services, Hewlett-Packard Document
 * EWC-93-0023b, Hewlett-Packard Corporation, 101 Stewart, Suite 700, Seattle, WA 98101 (1993).</li>
 *   <li><a href="www.fonts.com/hp/panose/greybook">The "Grey Book"</a></li>
 * </ul>
 */
public final class Panose implements Serializable {
	
    private static final long serialVersionUID = -6678392067062344333L;

    /**
     * Enumeration of the fields that comprise a PANOSE description.
     * @see "http://fonts.apple.com/TTRefMan/RM06/Chap6OS2.html"
     */
    public enum Field {
        /** The bFamilyType field. */
        FAMILY_TYPE ((byte) 0, (byte) 5),

        /** The bSerifStyle field. */
        SERIF_STYLE ((byte) 1, (byte) 15),

        /** The bWeight field. */
        WEIGHT ((byte) 2, (byte) 11),

        /** The bProportion field. */
        PROPORTION ((byte) 3, (byte) 9),

        /** The bContrast field. */
        CONTRAST ((byte) 4, (byte) 9),

        /** The bStrokeVariatoon field. */
        STROKE_VARIATION ((byte) 5, (byte) 8),

        /** The bArmStyle field. */
        ARM_STYLE ((byte) 6, (byte) 11),

        /** The bLetterform field. */
        LETTERFORM ((byte) 7, (byte) 15),

        /** The bMidline field. */
        MIDLINE ((byte) 8, (byte) 13),

        /** The bXHeight field. */
        X_HEIGHT ((byte) 9, (byte) 7);

        /** The 0-based index of this element in the PANOSE array. */
        private byte index;

        /** The maximum value that is permissible for this element. */
        private byte maxValue;

        /**
         * Private Constructor.
         * @param index The 0-based index of this element in the PANOSE array.
         * @param maxValue The maximum value that is permissible for this element.
         */
        private Field(final byte index, final byte maxValue) {
            this.index = index;
            this.maxValue = maxValue;
        }

        /**
         * Returns the 0-based index of this element in the PANOSE array.
         * @return The 0-based index of this element in the PANOSE array.
         */
        public byte getIndex() {
            return this.index;
        }

        /**
         * Returns the maximum valid value for this field.
         * @return The maximum valid value for this field.
         */
        public byte getMaxValue() {
            return this.maxValue;
        }

    }

    /**
     * Constant indicating the minimum italic value for the letterform field.
     * This is based on an analysis of the MS ClearType Collection fonts:
     * <ul>
     * <li>consolas             [ 2 11 6 9 2 2 4 3 2 4 ]  bold 6 ital 3</li>
     * <li>consolas-bold        [ 2 11 7 9 2 2 4 3 2 4 ]  bold 7 ital 3</li>
     * <li>consolas-italic      [ 2 11 6 9 2 2 4 10 2 4 ] bold 6 ital 10</li>
     * <li>consolas-bolditalic  [ 2 11 7 9 2 2 4 10 2 4 ] bold 7 ital 10</li>
     *
     * <li>cordianew            [ 2 11 3 4 2 2 2 2 2 4 ]  bold 3 ital 2</li>
     * <li>cordianew-bolditalic [ 2 11 6 4 2 2 2 9 2 4 ]  bold 6 ital 9</li>
     *
     * <li>calibri              [ 2 15 5 2 2 2 4 3 2 4 ]  bold 5 ital 3</li>
     * <li>calibri-bold         [ 2 15 7 2 3 4 4 3 2 4 ]  bold 7 ital 3</li>
     * <li>calibri-bolditalic   [ 2 15 7 2 3 4 4 10 2 4 ] bold 7 ital 10</li>
     *
     * <li>constantia           [ 2 3 6 2 5 3 6 3 3 3 ]   bold 6 ital 3</li>
     * <li>constantia-bold      [ 2 3 7 2 6 3 6 3 3 3 ]   bold 7 ital 3 (note the 6 at index 4)</li>
     * <li>constantia-italic    [ 2 3 6 2 5 3 6 10 3 3 ]  bold 6 ital 10</li>
     *
     * <li>candara-bold         [ 2 14 7 2 3 3 3 2 2 4 ]  bold 7 ital 2</li>
     * <li>candara-italic       [ 2 14 5 2 3 3 3 9 2 4 ]  bold 5 ital 9</li>
     * <li>candara-bolditalic   [ 2 14 7 2 3 3 3 9 2 4 ]  bold 7 ital 9</li>
     *
     * <li>cambria-bold         [ 2 4 8 3 5 4 6 3 2 4 ]   bold 8 ital 3</li>
     * <li>cambria-italic       [ 2 4 5 3 5 4 6 10 2 4 ]  bold 5 ital 10</li>
     * </ul>
     */
    private static final byte LETTERFORM_MIN_ITALIC = 9;

    /**
     * Constant indicating the minimum bold value for the weight field.
     * This is based on an analysis of the MS ClearType Collection fonts, which can be found at
     * {@link #LETTERFORM_MIN_ITALIC}.
     */
    private static final byte WEIGHT_MIN_BOLD = 7;

    /** An array of weights indicating that all elements in a comparison between two PANOSE values
     * shall be considered to be of the same weight. */
    private static final byte[] NEUTRAL_WEIGHTS = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
    /* Caveat: It is tempting to make NEUTRAL_WEIGHTS public and allow client apps to use it as a
     * parameter. However, there is no way to protect the data inside it from corruption, so we
     * have elected to keep it private and to allow "null" to be interpreted as the same value. */

    
    
    /** The encapsulated array of PANOSE numbers. */
    private byte[] panoseArray;

    /**
     * Private Constructor. Use {@link #makeInstance(byte[])} to create an instance of this class.
     * @param panoseArray The array of bytes recording the PANOSE classification.
     */
    private Panose(final byte[] panoseArray) {
        /* Clone the incoming array to protect our data from subsequent changes made to the original
         * array. */
        this.panoseArray = panoseArray.clone();
    }

    /**
     * Creates a new Panose instance, first checking it for validity.
     * @param panoseArray The array of bytes recording the PANOSE
     * classification.
     * @return The newly-created instance.
     * @throws FontException If <code>panoseArray</code> contains an illegal value.
     * @see #forceInstance(byte[])
     */
    public static Panose makeInstance(final byte[] panoseArray) {
        final String panoseValidationMessage = Panose.validPanose(panoseArray);
        if (panoseValidationMessage != null) {
            throw new IllegalArgumentException("Illegal Panose Array: " + panoseValidationMessage);
        }
        return new Panose(panoseArray);
    }

    /**
     * Creates a new Panose instance without any error checking.
     * @param panoseArray The array of bytes recording the PANOSE classification.
     * @return The newly-created instance.
     * @see #makeInstance(byte[])
     */
    public static Panose forceInstance(final byte[] panoseArray) {
        return new Panose(panoseArray);
    }

    /**
     * Returns a clone of the the array of bytes representing the PANOSE number.
     * To avoid the cost of this cloning operation, use {@link #getElement(int)} to obtain the
     * value of individual elements in the array.
     * @return The PANOSE array.
     */
    public byte[] getPanoseArray() {
        return this.panoseArray.clone();
    }

    /**
     * Returns a given element from the underlying Panose array.
     * @param index The index to the element desired.
     * @return The value of the element at <code>index</code>.
     */
    public byte getElement(final int index) {
        return this.panoseArray[index];
    }

    /**
     * Returns a given element from the underlying Panose array.
     * @param field The field for which the value is desired.
     * @return The value of the element at <code>field</code>.
     */
    public byte getElement(final Panose.Field field) {
        final int index = field.getIndex();
        return getElement(index);
    }

    /**
     * Computes the weighted "closeness" of another Panose to this value.
     * @param otherPanose Another Panose instance which is being compared to this.
     * @param weights 10-element byte array of weights that should be used for each of the elements
     * in the comparison.
     * Values in this array must be between 0 and 127 inclusive.
     * (This constant is documented at http://www.w3.org/Fonts/Panose/pan2.html#StaticDigits).
     * Use null if all elements are to be weighted equally.
     * @return The weighted difference between the two Panose values.
     * A smaller value indicates that the two values are closer, and a larger
     * value indicates that they are farther apart.
     */
    public long difference(final Panose otherPanose, final byte[] weights) {
        /* This is a partial implementation of the "PANOSE Matching Heuristic" documented at:
         * http://www.w3.org/Fonts/Panose/pan2.html#Heuristic. **/
        byte[] weightsToUse = null;
        if (weights == null) {
            weightsToUse = Panose.NEUTRAL_WEIGHTS;
        } else {
            validateWeights(weights);
            weightsToUse = weights;
        }
        long difference = 0;
        for (int i = 0; i < Panose.Field.values().length; i++) {
//            final int digit = panoseDescription.length - i;
//            final int weight = (int) Math.round(Math.pow(2, digit - 1));
            final int weight = weightsToUse[i];

            final int thisDifference = this.getElement(i) - otherPanose.getElement(i);
            difference += weight * thisDifference * thisDifference;
        }
        return difference;
    }

    /**
     * Examines an array of weights, throwing various unchecked exceptions if the data is not valid.
     * @param weights The array of weights to be tested.
     */
    private static void validateWeights(final byte[] weights) {
        if (weights == null) {
            throw new NullPointerException("Weights may not be null");
        }
        if (weights.length != Panose.Field.values().length) {
            throw new IllegalArgumentException("Weights size expected: "
                    + Panose.Field.values().length + ", actual: " + weights.length);
        }
        for (int i = 0; i < weights.length; i++) {
            final byte weight = weights[i];
            if (weight < 0
                    || weight > Byte.MAX_VALUE) {
                throw new IllegalArgumentException("Weight element " + i + " is outside the range "
                        + "of 0 thru 127.");
            }
        }
    }

    /**
     * Tests the validity of a panose description.
     * @param panoseDescription The panose values to be tested.
     * @return Null for a valid PANOSE description. For an invalid PANOSE description, returns a
     * descriptive message indicating which element is invalid.
     */
    public static String validPanose(final byte[] panoseDescription) {
        if (panoseDescription == null) {
            return "Panose description cannot be null.";
        }
        if (panoseDescription.length != Panose.Field.values().length) {
            return "Illegal Panose description size: " + panoseDescription.length;
        }
        for (int i = 0; i < panoseDescription.length; i++) {
            final byte theByte = panoseDescription[i];
            final Panose.Field panoseField = Panose.Field.values()[i];
            final byte maxValue = panoseField.getMaxValue();
            if (theByte < 0
                    || theByte > maxValue) {
                return "Invalid value " + theByte + " > " + maxValue + " in position " + i
                        + " of " + toString(panoseDescription);
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return toString(this.panoseArray);
    }

    /**
     * Returns a String representation of a Panose array.
     * @param panoseArray The Panose array to be expressed as a String.
     * @return The String representation of <code>panoseArray</code>.
     */
    private static String toString(final byte[] panoseArray) {
        final StringBuilder sb = new StringBuilder(30);
        sb.append("[ ");
        for (int i = 0; i < panoseArray.length; i++) {
            final byte theByte = panoseArray[i];
            sb.append(theByte + " ");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Returns the bold version of this Panose instance.
     * @return If this already describes a bold font, returns this. Otherwise, returns a new
     * Panose instance that is identical to this, except describing a bold font.
     */
    public Panose getBold() {
        final byte weightValue = this.getElement(Panose.Field.WEIGHT);
        if (weightValue >= Panose.WEIGHT_MIN_BOLD) {
            /* This Panose value is already bold. */
            return this;
        }
        final byte[] newArray = this.panoseArray.clone();
        newArray[Panose.Field.WEIGHT.getIndex()] = Panose.WEIGHT_MIN_BOLD;
        return Panose.forceInstance(newArray);
    }

    /**
     * Returns the italic version of this Panose instance.
     * @return If this already describes an italic font, returns this. Otherwise, returns a new
     * Panose instance that is identical to this, except describing an italic font.
     */
    public Panose getItalic() {
        final byte letterformValue = this.getElement(Panose.Field.LETTERFORM);
        if (letterformValue >= Panose.LETTERFORM_MIN_ITALIC) {
            /* This Panose value is already italic. */
            return this;
        }
        final byte[] newArray = this.panoseArray.clone();
        newArray[Panose.Field.LETTERFORM.getIndex()] = Panose.LETTERFORM_MIN_ITALIC;
        return Panose.forceInstance(newArray);
    }


}
