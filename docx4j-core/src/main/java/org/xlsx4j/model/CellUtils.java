/* NOTICE: This file contains code changed by Plutext Pty Ltd for use in docx4j/xlsx4j.
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

package org.xlsx4j.model;

import java.util.Date;

import org.docx4j.openpackaging.parts.SpreadsheetML.Styles;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xlsx4j.org.apache.poi.ss.usermodel.BuiltinFormats;
import org.xlsx4j.org.apache.poi.ss.usermodel.DataFormatter;
import org.xlsx4j.org.apache.poi.ss.usermodel.DateUtil;
import org.xlsx4j.sml.CTCellStyle;
import org.xlsx4j.sml.CTXf;
import org.xlsx4j.sml.Cell;

public class CellUtils {

	protected static Logger log = LoggerFactory.getLogger(CellUtils.class);
	
	protected CellUtils() {
		// no instances of this class
    }

    /**
     * Numeric Cell type (0)
     * @see #setCellType(int)
     * @see #getCellType()
     */
    public final static int CELL_TYPE_NUMERIC = 0;

    /**
     * String Cell type (1)
     * @see #setCellType(int)
     * @see #getCellType()
     */
    public final static int CELL_TYPE_STRING = 1;

    /**
     * Formula Cell type (2)
     * @see #setCellType(int)
     * @see #getCellType()
     */
    public final static int CELL_TYPE_FORMULA = 2;

    /**
     * Blank Cell type (3)
     * @see #setCellType(int)
     * @see #getCellType()
     */
    public final static int CELL_TYPE_BLANK = 3;

    /**
     * Boolean Cell type (4)
     * @see #setCellType(int)
     * @see #getCellType()
     */
    public final static int CELL_TYPE_BOOLEAN = 4;

    /**
     * Error Cell type (5)
     * @see #setCellType(int)
     * @see #getCellType()
     */
    public final static int CELL_TYPE_ERROR = 5;
    	
    private static final String FALSE_AS_STRING = "0";
    private static final String TRUE_AS_STRING  = "1";

    /**
     * Get the value of the cell as a number.
     * <p>
     * For strings we throw an exception. For blank cells we return a 0.
     * For formulas or error cells we return the precalculated value;
     * </p>
     * @return the value of the cell as a number
     * @throws IllegalStateException if the cell type returned by {@link #getCellType()} is CELL_TYPE_STRING
     * @exception NumberFormatException if the cell value isn't a parsable <code>double</code>.
     * @see DataFormatter for turning this number into a string similar to that which Excel would render this number as.
     */
    
    public static double getNumericCellValue(Cell _cell) {
    	
    	log.debug("In getNumericCellValue " + _cell.getV() );
    	
        int cellType = getCellType(_cell);
        switch(cellType) {
            case CELL_TYPE_BLANK:
                return 0.0;
            case CELL_TYPE_FORMULA:
            case CELL_TYPE_NUMERIC:
                if(_cell.getV()!=null) {
                   String v = _cell.getV();
                   if (v.isEmpty()) return 0.0;
                   try {
                      return Double.parseDouble(v);
                   } catch(NumberFormatException e) {
                      throw typeMismatch(CELL_TYPE_NUMERIC, CELL_TYPE_STRING, false);
                   }
                } else {
                   return 0.0;
                }
            default:
                throw typeMismatch(CELL_TYPE_NUMERIC, cellType, false);
        }
    } 
    
    /**
     * Used to help format error messages
     */
    private static String getCellTypeName(int cellTypeCode) {
        switch (cellTypeCode) {
            case CELL_TYPE_BLANK:   return "blank";
            case CELL_TYPE_STRING:  return "text";
            case CELL_TYPE_BOOLEAN: return "boolean";
            case CELL_TYPE_ERROR:   return "error";
            case CELL_TYPE_NUMERIC: return "numeric";
            case CELL_TYPE_FORMULA: return "formula";
        }
        return "#unknown cell type (" + cellTypeCode + ")#";
    }
    
    /**
     * Used to help format error messages
     */
    public static RuntimeException typeMismatch(int expectedTypeCode, int actualTypeCode, boolean isFormulaCell) {
        String msg = "Cannot get a "
            + getCellTypeName(expectedTypeCode) + " value from a "
            + getCellTypeName(actualTypeCode) + " " + (isFormulaCell ? "formula " : "") + "cell";
        return new IllegalStateException(msg);
    }    
    
    /**
     * Return the cell type.
     *
     * @return the cell type
     * @see Cell#CELL_TYPE_BLANK
     * @see Cell#CELL_TYPE_NUMERIC
     * @see Cell#CELL_TYPE_STRING
     * @see Cell#CELL_TYPE_FORMULA
     * @see Cell#CELL_TYPE_BOOLEAN
     * @see Cell#CELL_TYPE_ERROR
     */
    
    public static int getCellType(Cell _cell) {

        if (_cell.getF() != null 
//        		|| getSheet().isCellInArrayFormulaContext(this)
        		) {
            return CELL_TYPE_FORMULA;
        }

        int cellTypeCode =  getBaseCellType(_cell, true);
        
        log.debug( getCellTypeName(cellTypeCode));
        
        return cellTypeCode;
    }


    /**
     * Detect cell type based on the "t" attribute of the CTCell bean
     */
    private static int getBaseCellType(Cell _cell, boolean blankCells) {
        switch (_cell.getT()) {
            case B:
                return CELL_TYPE_BOOLEAN;
            case N:
                if (_cell.getV()==null && blankCells) {
                    // ooxml does have a separate cell type of 'blank'.  A blank cell gets encoded as
                    // (either not present or) a numeric cell with no value set.
                    // The formula evaluator (and perhaps other clients of this interface) needs to
                    // distinguish blank values which sometimes get translated into zero and sometimes
                    // empty string, depending on context
                    return CELL_TYPE_BLANK;
                }
                return CELL_TYPE_NUMERIC;
            case E:
                return CELL_TYPE_ERROR;
            case S: // String is in shared strings
            case INLINE_STR: // String is inline in cell
            case STR:
                 return CELL_TYPE_STRING;
            default:
                throw new IllegalStateException("Illegal cell type: " + _cell.getT());
        }
    }    
    
	  /**
	  * Return the cell's style.
	  *
	  * @return the cell's style.</code>
	  */
	 public static CTCellStyle getCellStyle(Cell _cell) {
		 
		 Styles stylesPart = WorksheetPart.getWorksheetPart( _cell).getWorkbookPart().getStylesPart();
		 
		 CTXf xf = stylesPart.getXfByIndex(_cell.getS());
		 if (xf == null) {
			 throw new RuntimeException("xf unexpectedly null");
		 }
	     return stylesPart.getStyleByIndex(xf.getXfId());
	 }
    
//	 /**
//	  * Get the index of the number format (numFmt) record used by this cell format.
//	  *
//	  * @return the index of the number format
//	  */
//	 public long getDataFormatIndexZZZ(CTCellStyle cellStyle) {
//		 CTXf xf = this.stylesPart.getXfByIndex(cellStyle.getXfId());
//		 if (xf == null) {
//			 throw new RuntimeException("xf unexpectedly null");
//		 }
//		 
//		 System.out.println("Using NumFmtId " + xf.getNumFmtId());
//		 
//	     return xf.getNumFmtId();
//	     //(short)_cellXf.getNumFmtId();
//	 }

	 /**
	  * Get the index of the number format (numFmt) record used by this cell format.
	  *
	  * @return the index of the number format
	  */
	 public static long getNumberFormatIndex(Cell _cell) {
		 
		 Styles stylesPart = WorksheetPart.getWorksheetPart( _cell).getWorkbookPart().getStylesPart();
		 
		 CTXf xf = stylesPart.getXfByIndex(_cell.getS());
		 if (xf == null) {
			 throw new RuntimeException("xf unexpectedly null");
		 }
		 
		 log.debug("Using NumFmtId " + xf.getNumFmtId());
		 
	     return xf.getNumFmtId();
	     //(short)_cellXf.getNumFmtId();
	 }
	 
	 /**
	  * Get the contents of the format string, by looking up
	  * the StylesSource
	  *
	  * @return the number format string
	  */
	 public static String getNumberFormatString(Cell _cell) {
	     long idx = getNumberFormatIndex(_cell);
	     return getFormat((int)idx);
	 }

	    /**
	     * get the format string that matches the given format index
	     * @param index of a format
	     * @return string represented at index of format or null if there is not a  format at that index
	     */
	    private  static String getFormat(int index) {
	        //String fmt = stylesSource.getNumberFormatAt(index);
	    	String fmt = null;
	        if(fmt == null) {
	        	fmt = BuiltinFormats.getBuiltinFormat(index);
	        }
			 log.debug("Using BuiltinFormat " + fmt);
	        
	        return fmt;
	    }	 

	    /**
	     * Get the value of the cell as a date.
	     * <p>
	     * For strings we throw an exception. For blank cells we return a null.
	     * </p>
	     * @return the value of the cell as a date
	     * @throws IllegalStateException if the cell type returned by {@link #getCellType()} is CELL_TYPE_STRING
	     * @exception NumberFormatException if the cell value isn't a parsable <code>double</code>.
	     * @see DataFormatter for formatting  this date into a string similar to how excel does.
	     */
	    
	    public static Date getDateCellValue(Cell _cell) {
	    	
	        boolean date1904 = WorksheetPart.getWorksheetPart( _cell).getWorkbookPart().isDate1904();
	    	
	        int cellType = getCellType(_cell);
	        if (cellType == CELL_TYPE_BLANK) {
	            return null;
	        }

	        double value = getNumericCellValue(_cell);
	        //boolean date1904 = getSheet().getWorkbook().isDate1904();
	        return DateUtil.getJavaDate(value, date1904);
	    }

	    /**
	     * Get the value of the cell as a boolean.
	     * <p>
	     * For strings, numbers, and errors, we throw an exception. For blank cells we return a false.
	     * </p>
	     * @return the value of the cell as a boolean
	     * @throws IllegalStateException if the cell type returned by {@link #getCellType()}
	     *   is not CELL_TYPE_BOOLEAN, CELL_TYPE_BLANK or CELL_TYPE_FORMULA
	     */
	    
	    public static boolean getBooleanCellValue(Cell _cell) {
	        int cellType = getCellType(_cell);
	        switch(cellType) {
	            case CELL_TYPE_BLANK:
	                return false;
	            case CELL_TYPE_BOOLEAN:
	                return _cell.getV()!=null && TRUE_AS_STRING.equals(_cell.getV());
	            case CELL_TYPE_FORMULA:
	                //YK: should throw an exception if requesting boolean value from a non-boolean formula
	                return _cell.getV()!=null && TRUE_AS_STRING.equals(_cell.getV());
	            default:
	                throw typeMismatch(CELL_TYPE_BOOLEAN, cellType, false);
	        }
	    }	    
}
