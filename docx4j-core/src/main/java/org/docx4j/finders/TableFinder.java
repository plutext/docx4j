package org.docx4j.finders;

import java.util.ArrayList;
import java.util.List;

import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;

public class  TableFinder extends CallbackImpl {
	
	boolean traveseTables=false;
	
	/**
	 * Defaults to false; set this to true unless you don't
	 * want to traverse a table (eg a nested table).
	 * NB: If traversing from body level, you'll need to set it to true!
	 * 
	 * @param traveseNestedTable
	 */
	public void setTraverseTables(boolean traveseTables) {
		this.traveseTables = traveseTables;
	}

	public List<Tbl> tblList = new ArrayList<Tbl>();  
			
	@Override
	public List<Object> apply(Object o) {
		
		if (o instanceof Tbl ) {
			tblList.add((Tbl)o);
		}			
		return null; 
	}
	
	@Override
	public boolean shouldTraverse(Object o) {
		
		if (traveseTables) {
			return true;
		} else {
			// Yes, unless its a nested Tbl
			return !(o instanceof Tbl);
		}
	}
}
