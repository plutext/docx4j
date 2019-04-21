package org.docx4j.finders;

import java.util.ArrayList;
import java.util.List;

import org.docx4j.TraversalUtil.CallbackImpl;

public class ClassFinder extends CallbackImpl {
	  
	  protected Class<?> typeToFind;
	  
	  public ClassFinder(Class<?> typeToFind) {
		  this.typeToFind = typeToFind;
	  }
		
		public List<Object> results = new ArrayList<Object>(); 
		
		@Override
		public List<Object> apply(Object o) {
			
			// Adapt as required
			if (o.getClass().equals(typeToFind)) {
				results.add(o);
			}
			return null;
		}
}
