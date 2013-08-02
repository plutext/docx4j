package org.docx4j.finders;

import java.util.ArrayList;
import java.util.List;

import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;

public class  TcFinder extends CallbackImpl {
	
	public List<Tc> tcList = new ArrayList<Tc>();  
			
	@Override
	public List<Object> apply(Object o) {
		
		if (o instanceof Tc ) {
			
			tcList.add((Tc)o);
		}			
		return null; 
	}
	
	@Override
	public boolean shouldTraverse(Object o) {
		
		// Yes, unless its a nested Tbl
		return !(o instanceof Tbl); 
	}
}
