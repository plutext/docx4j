package org.docx4j.anon;

import org.docx4j.fonts.RunFontSelector;
import org.docx4j.fonts.RunFontSelector.RunFontCharacterVisitor;
import org.w3c.dom.Document;

public class RunFontCharVisitorMinimal implements RunFontCharacterVisitor {
		
		StringBuilder sb = new StringBuilder(1024); 
		
		@Override
		public void setDocument(Document document) {}
		

		public void addCharacterToCurrent(char c) {
	    	sb.append(c);		
		}
		
		@Override
		public void addCodePointToCurrent(int cp) {
			sb.append(
					new String(Character.toChars(cp)));
		}


		@Override
		public Object getResult() {
			return sb.toString();
		}

		private RunFontSelector runFontSelector;
		@Override
		public void setRunFontSelector(RunFontSelector runFontSelector) {
			this.runFontSelector = runFontSelector;
		}

		@Override
		public void setFallbackFont(String fontname) {}


		@Override
		public void finishPrevious() {
			
		}


		@Override
		public void createNew() {
			sb = new StringBuilder(1024); 
			fontname = null;
		}


		@Override
		public void setMustCreateNewFlag(boolean val) {
			// TODO Auto-generated method stub
			
		}


		@Override
		public boolean isReusable() {
			// TODO Auto-generated method stub
			return false;
		}


		String fontname;
		
		public String getFontname() {
			return fontname;
		}


		@Override
		public void fontAction(String fontname) {
			this.fontname = fontname;
			
		}
	};
