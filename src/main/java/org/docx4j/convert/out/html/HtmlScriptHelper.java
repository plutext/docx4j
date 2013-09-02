package org.docx4j.convert.out.html;

public class HtmlScriptHelper {

	public static void createDefaultScript(StringBuilder result) {
		result.append("function toggleDiv(divid){");
		result.append("if(document.getElementById(divid).style.display == 'none'){");
		result.append("document.getElementById(divid).style.display = 'block';");
		result.append("}else{");
		result.append("document.getElementById(divid).style.display = 'none';");
		result.append("}");
		result.append("}\n");
	}
	
}
