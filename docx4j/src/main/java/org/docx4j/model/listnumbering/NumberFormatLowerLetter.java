package org.docx4j.model.listnumbering;

public class NumberFormatLowerLetter extends NumberFormat {
	
	public String format( int in ) {
		
		String str = Integer.toString(in);
		StringBuilder out = new StringBuilder();
		
		for( int i=0; i < str.length() ; i++ ) {
			
			
			 int dig = Character.digit(str.charAt(i), 36);
			 
			
			char cdig = Character.forDigit(dig+9, 36);
		       
			
			System.out.println(str.charAt(i) + " --> " + cdig);
			
			out.append(cdig);
			
		}
		
		return out.toString();
		
	}
	
	  public static void main (String [] args)
	   {
		   
		  NumberFormatLowerLetter format = new NumberFormatLowerLetter();
		  
		  format.format(1);
		  format.format(2);
		  System.out.println(format.format(3));
		  
		  
		  
	   }

}
