package org.docx4j.model.fields.merge;

/**
 * The name of the data field.
 * 
 * When Word performs a mail merge, it treats this as case-insensitive
 * (and takes the first matching field).
 * 
 * The purpose of this class is to ensure the key provided is
 * case-insensitive.
 * 
 * @author jharrop
 *
 */
public class DataFieldName {
	
	String name;
	
	public DataFieldName(String name) {
		
		this.name = name.toUpperCase();
	}
	
	
	@Override public boolean equals(Object aThat) {
	    
		if (aThat instanceof DataFieldName) {
			return ( name.equals(
					((DataFieldName)aThat).name
					) );	    
		} else {
			return super.equals(aThat);
		}
	}
	
	@Override public int hashCode() {
		return name.hashCode();
	}

}
