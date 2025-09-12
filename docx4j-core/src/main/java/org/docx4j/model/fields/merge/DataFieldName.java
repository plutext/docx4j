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
	
	private String name;

	private String nameCaseSensitive;
	
	/**
	 * Returns the data field name in caps (which is what a Word mail merge expects)
	 * @since 11.5.5
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the data field name as provided in the constructor (ie case sensitive);
	 * useful outside of mail merge applications.
	 * @since 11.5.5
	 */
	public String getNameAsProvided() {
		return nameCaseSensitive;
	}
	

	public DataFieldName(String name) {
		
		this.nameCaseSensitive = name;
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
