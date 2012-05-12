/*
 *  Copyright 2009, Plutext Pty Ltd.
 *   
 *  This Javascript file is currently kept in the svn repository of docx4j project
 	(see http://dev.plutext.org), at
 	http://dev.plutext.org/trac/docx4j/browser/trunk/docx4j/src/main/javascript/wml_fix.js
 	(contributions welcome!)
 
    It is not however, considered part of the docx4j project, which is
    is licensed under the Apache License, Version 2.0.

	This JavaScript file is free software: you can
    redistribute it and/or modify it under the terms of the GNU
    General Public License (GNU GPL) as published by the Free Software
    Foundation, either version 3 of the License, or (at your option)
    any later version.  The code is distributed WITHOUT ANY WARRANTY;
    without even the implied warranty of MERCHANTABILITY or FITNESS
    FOR A PARTICULAR PURPOSE.  See the GNU GPL for more details.

    As additional permission under GNU GPL version 3 section 7, you
    may distribute this code without the copy of the GNU GPL normally 
    required by section 4, provided you include this license notice. 

 */
 
  function setAttributes()
  {

	var paragraphs = document.getElementsByTagName("w:p");
	//alert(paragraphs.length);
	for (var i=0 ; i<paragraphs.length; i++) {

		// @class
		var pStyle = paragraphs[i].getElementsByTagName("w:pStyle");
		var styleVal;
		if (pStyle.length>0) {
			styleVal = pStyle[0].getAttribute("w:val");
		} else {
			styleVal = "Normal";
		}
		paragraphs[i].setAttribute("class", styleVal+"_pPr " + styleVal+"_rPr ");

		// @style
		var pPr = paragraphs[i].getElementsByTagName("w:pPr");
		if (pPr.length>0) {
			var classVal = createBlockCss(pPr[0]);
			//alert(i + "-->" + classVal);
			if (classVal!="") {
				paragraphs[i].setAttribute("style", classVal );
			}
		}
	}

	var runs = document.getElementsByTagName("w:r");
	//alert(runs.length);
	for (var i=0 ; i<runs.length; i++) {

		// @class
		var rStyle = runs[i].getElementsByTagName("w:rStyle");
		if (rStyle.length>0) {
			var styleVal = rStyle[0].getAttribute("w:val");
			runs[i].setAttribute("class", styleVal);
		}

		// @style
		var rPr = runs[i].getElementsByTagName("w:rPr");
		if (rPr.length>0) {
			var classVal = createRunCss(rPr[0]) ;
			//alert(i + "-->" + classVal);
			if (classVal!="") {
				runs[i].setAttribute("style", classVal);
			}
		}
	}

	// tables - just basic formatting for now
	// w:tbl should look like a table
	var tables = document.getElementsByTagName("w:tbl");
	for (var i=0 ; i<tables.length; i++) {

		tables[i].setAttribute("class", "table");

		// w:tr should look like a table row
		var rows = tables[i].getElementsByTagName("w:tr");
		for (var j=0 ; j<rows.length; j++) {
			rows[j].setAttribute("class", "tr");

			// w:tc should look like a table cell
			var cells = rows[j].getElementsByTagName("w:tc");
			for (var k=0 ; k<cells.length; k++) {
				cells[k].setAttribute("class", "td");
			} 
		}
	}

	// images - just inline ones for now
	// <w:drawing><wp:inline>
	// we use XPath / XSLT compatibility library 0.1 (zip) 
	// for  http://www.ajax.org/downloads/get/did/11
	// since DOM 3 xpath fails (in FF3 at least) to find
	// namespaced nodes in our HTML doc
	// see http://www.rubendaniels.com/2007/02/23/safari-wrench/3/
	var parts = XPath.selectNodes("//pkg:part", document);
	//alert("Found document rels; count: " + parts.length);
	var images = new Array();
	var documentRelsPart;
	for( i=0 ; i<parts.length; i++) {
		var pkgName = parts[i].getAttribute("pkg:name");
		var pkgContentType = parts[i].getAttribute("pkg:contentType");
		if (pkgName!="/word/document.xml") {
			parts[i].setAttribute("style", "display:none;");
		}
		//    <pkg:part pkg:name="/word/_rels/document.xml.rels">
		if (pkgName=="/word/_rels/document.xml.rels") {
			documentRelsPart=parts[i];
			//alert("Found doc rels " + pkgName);
		}
		if (pkgContentType.startsWith("image/")) {
			images[pkgName]=parts[i];
			//alert("Found image " + pkgName);
		} 
		 
	}
	var wbody = XPath.selectNodes("//w:body", document)[0];
	var drawings = XPath.selectNodes("//w:drawing", wbody);
	//alert("Found w:drawing; count: " + drawings.length);

	var documentRels = new Array();
	if (drawings.length>0) {
		// We need a map of the rels
		// <rel:Relationship Id="rId10" Target="media/image3.png" 
		//	Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/image" />
		var rels = XPath.selectNodes("//rel:Relationship", documentRelsPart);
		if (rels.length==0) {
			// default namespace?
			rels = XPath.selectNodes("//Relationship", documentRelsPart);
		}
		if (rels.length==0) {
			alert("Couldn't find any rels in the rels!");
		}
		for( i=0 ; i<rels.length; i++) {
			var id = rels[i].getAttribute("Id");
			documentRels[id] = rels[i];
		}
	}

	for( i=0 ; i<drawings.length; i++) {
		// look at <a:blip r:embed="rId10">
		var wblip = XPath.selectNodes("//a:blip", drawings[i])[0];
		if (wblip.getAttribute("r:link")!=null) {
			// TODO
			alert("Linked images aren't handled yet");
		} else if (wblip.getAttribute("r:embed")!=null) {
			// get the rel
			var theRel = documentRels[wblip.getAttribute("r:embed")];
			var thePartName = "/word/" + theRel.getAttribute("Target");
			var imagePart = images[thePartName];
			// now embed it :-)

			var img = document.createElement("img");
			// <img src="data:image/gif;base64,R0lGODlhEAAOALMAAOazToeHh0tLS/7LZv/0jvb29t/f3//Ub/
			var binaryData = XPath.selectNodes("//pkg:binaryData", imagePart)[0];
			var base64data = "";
			// Get the value, trying to handle the case
			// where it is split across separate text nodes
			for( j=0 ; j<binaryData.childNodes.length; j++) {
				base64data = base64data + binaryData.childNodes[j].nodeValue;
			}
			img.setAttribute("src", "data:" + imagePart.getAttribute("pkg:contentType")
				+";base64,"+base64data); 
			drawings[i].appendChild(img);
		}
	}


}

String.prototype.startsWith = function(str){
    return (this.indexOf(str) === 0);
}



    function createBlockCss(pPr) {

	var result = "";

	// Here is where we do the real work.  
	// There are a lot of paragraph properties
	// The below list is taken directly from PPrBase.
	
	//PPrBase.PStyle pStyle;
	
		// Ignore
	
	//BooleanDefaultTrue keepNext;
	if (pPr.getElementsByTagName("w:keepNext").length>0) {
		result = result +  "page-break-after:avoid;" ;
	}
	

	//BooleanDefaultTrue keepLines;
	//if (pPr.getElementsByTagName("w:keepLines").length>0) {
	//}

	//BooleanDefaultTrue pageBreakBefore;
	if (pPr.getElementsByTagName("w:pageBreakBefore").length>0) {
		result = result +  "page-break-before:always;" ;
	}

	//CTFramePr framePr;
	//BooleanDefaultTrue widowControl;
	//if (pPr.getElementsByTagName("w:widowControl").length>0) {
	//}

	//PPrBase.NumPr numPr;
	
		// High priority

	//BooleanDefaultTrue suppressLineNumbers;
	//if (pPr.getElementsByTagName("w:suppressLineNumbers").length>0) {
	//}
	//PPrBase.PBdr pBdr;
	
		// Medium priority

	//CTShd shd;
	
		// Medium priority

	//Tabs tabs;
	
		// ???

	//BooleanDefaultTrue suppressAutoHyphens;
	//BooleanDefaultTrue kinsoku;
	//BooleanDefaultTrue wordWrap;
	//BooleanDefaultTrue overflowPunct;
	//BooleanDefaultTrue topLinePunct;
	//BooleanDefaultTrue autoSpaceDE;
	//BooleanDefaultTrue autoSpaceDN;
	//BooleanDefaultTrue bidi;
	//BooleanDefaultTrue adjustRightInd;
	//BooleanDefaultTrue snapToGrid;

	//PPrBase.Spacing spacing;
	//if (pPr.getElementsByTagName("w:spacing").length>0) {
	//}

	//PPrBase.Ind ind;
	if (pPr.getElementsByTagName("w:ind").length>0) {
		
		// Just handle left for the moment
		var left = pPr.getElementsByTagName("w:ind")[0].getAttribute("w:left");
		if (left!=null) {
			// 720 twip = 1 inch;
			var inches = left/720;
			result = result +  "position: relative; left: " + inches + "in;" ;
		}
		
	}

	//BooleanDefaultTrue contextualSpacing;
	//BooleanDefaultTrue mirrorIndents;
	//BooleanDefaultTrue suppressOverlap;
	//Jc jc;
	if (pPr.getElementsByTagName("w:jc").length>0) {
		var val = pPr.getElementsByTagName("w:jc")[0].getAttribute("w:val");
		if (val == "left" || val == "center" || val == "right") {			
			result = result +  "text-align: " + val + ";" ;
		} else if (val == "both") {
			result = result +  "text-align:justify;text-justify:inter-ideograph;" ;
		} // ignore the other possibilities for now
	}

	//TextDirection textDirection;
	//PPrBase.TextAlignment textAlignment;
	if (pPr.getElementsByTagName("w:textAlignment").length>0) {
		var val = pPr.getElementsByTagName("w:textAlignment")[0].getAttribute("w:val");
		if (val = "top"  || val == "bottom" || val == "baseline" ) 			
		{						
			result = result +  "vertical-align: " + val + ";" ;
		} else if (val == "center") {
			result = result +  "vertical-align: middle;" ;
		} else if (val == "auto") {
			result = result +  "vertical-align: baseline;" ;
		}
	}

	//CTTextboxTightWrap textboxTightWrap;
	//PPrBase.OutlineLvl outlineLvl;
	
		// Medium priority

	//PPrBase.DivId divId;
	//CTCnf cnfStyle;

	return result;
    
    }
    
    function createRunCss(rPr) {

	var result = "";

	// Here is where we do the real work.  
	// There are a lot of run properties
	// The below list is taken directly from RPr, and so
	// is comprehensive.
	
/*	
	if (rPr.getElementsByTagName("w:rFonts").length>0) {
		
		String font = rFonts.getAscii();
		
		if (font==null) {
			// TODO - actually what Word does in this case
			// is inherit the default document font eg Calibri
			// (which is what it shows in its user interface)
			font = rFonts.getCs();
		}
		
		if (font==null) {
			log.error("Font was null in: " + XmlUtils.marshaltoString(rPr, true, true));
			font=Mapper.FONT_FALLBACK;
		}
		
		log.info("Font: " + font);
		
		PhysicalFont pf = wmlPackage.getFontMapper().getFontMappings().get(font);
		if (pf!=null) {					
			result = result +  "font-family: " + pf.getName() + ";" ;
		} else {
			log.error("No mapping from " + font);
		}
	}
  */  

	//BooleanDefaultTrue b;
	if (rPr.getElementsByTagName("w:b").length>0) {
		result = result +  "font-weight: bold;" ;
	}

	//BooleanDefaultTrue bCs;
	//BooleanDefaultTrue i;
	if (rPr.getElementsByTagName("w:i").length>0) {
		result = result +  "font-style: italic;" ;
	}

	//BooleanDefaultTrue iCs;
	//BooleanDefaultTrue caps;
	//if (rPr.getElementsByTagName("w:caps").length>0) {
	//}

	//BooleanDefaultTrue smallCaps;
	//if (rPr.getElementsByTagName("w:smallCaps").length>0) {
	//}

	//BooleanDefaultTrue strike;
	if (rPr.getElementsByTagName("w:strike").length>0) {
		result = result +  "text-decoration:line-through;";
	}
	//BooleanDefaultTrue dstrike;
	//BooleanDefaultTrue outline;
	//BooleanDefaultTrue shadow;
	//BooleanDefaultTrue emboss;
	//BooleanDefaultTrue imprint;
	//BooleanDefaultTrue noProof;
	//BooleanDefaultTrue snapToGrid;
	//BooleanDefaultTrue vanish;
	//BooleanDefaultTrue webHidden;
	//Color color;
	if (rPr.getElementsByTagName("w:color").length>0) {
		result = result +  "color: #" + rPr.getElementsByTagName("w:color")[0].getAttribute("w:val") + ";" ;
		// ignore theme stuff
	}

	//CTSignedTwipsMeasure spacing;
	//CTTextScale w;
	//HpsMeasure kern;
	//CTSignedHpsMeasure position;
	//HpsMeasure sz;
	if (rPr.getElementsByTagName("w:sz").length>0) {
		var pts = rPr.getElementsByTagName("w:sz")[0].getAttribute("w:val")/2;
		result = result +  "font-size:" + pts + "pt;" ;
	}

	//HpsMeasure szCs;
	//Highlight highlight;
	//U u;
	if (rPr.getElementsByTagName("w:u").length>0) {
		var u = rPr.getElementsByTagName("w:u")[0].getAttribute("w:val");
		if (u==null ) {
			// This does happen
			result = result +  "text-decoration:underline;" ;
		} else if (u !="none") {
			result = result +  "text-decoration:underline;" ;
		} 
		// How to handle <w:u w:color="FF0000"> ie coloured underline?
	}

	//CTTextEffect effect;
	//CTBorder bdr;
	//CTShd shd;
	//CTFitText fitText;
	//CTVerticalAlignRun vertAlign;
	//BooleanDefaultTrue rtl;
	//BooleanDefaultTrue cs;
	//CTEm em;
	//CTLanguage lang;
	//CTEastAsianLayout eastAsianLayout;
	//BooleanDefaultTrue specVanish;
	//BooleanDefaultTrue oMath;
	//CTRPrChange rPrChange;

	return result;
    	
    }

setAttributes();
