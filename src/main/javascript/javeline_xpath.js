/********* IE XPath Object ************************************************
	Workaround for the lack of having an XPath parser on safari
	It works on Safari's document and XMLDocument object.
	
	It doesn't support the full XPath spec, but just enought for
	the skinning engine which needs XPath on the HTML document.
	
	Supports:
	- Compilation of xpath statements
	- Caching of XPath statements
****************************************************************************/
//Set up the $ method
IS_SAFARI = navigator.userAgent.toLowerCase().indexOf("safari") != -1 || navigator.userAgent.toLowerCase().indexOf("konqueror") != -1;
IS_SAFARI_OLD = IS_SAFARI && parseInt((navigator.userAgent.match(/AppleWebKit\/(\d+)/)||{})[1]) < 420;
IS_GECKO = document.implementation && document.implementation.createDocument && true;
IS_OPERA = navigator.userAgent.toLowerCase().indexOf("opera") != -1;
IS_IE = document.all && !IS_OPERA;

//This breaks for(x in o) loops in the old Safari
if(IS_SAFARI_OLD){
	HTMLHtmlElement = document.createElement("html").constructor;
	Node = HTMLElement = {};
	HTMLElement.prototype = HTMLHtmlElement.__proto__.__proto__;
	HTMLDocument = Document = document.constructor;
	var x = new DOMParser();
	XMLDocument = x.constructor;
	Element = x.parseFromString("<Single />", "text/xml").documentElement.constructor;
	x = null;
}

TAGNAME = IS_IE ? "baseName" : "localName";
var _$ = function(tag, doc, prefix, force){
	return (doc || document).getElementsByTagName((prefix && (force || IS_GECKO) ? prefix + ":" : "") + tag);
}

XPath = {
	cache : {},
	
	getChildNode : function(htmlNode, tagName, info, count, num, sResult){
		var numfound = 0, result = null, data = info[count];

		var nodes = htmlNode.childNodes;
		if(!nodes) return; //Weird bug in Safari
		for(var i=0;i<nodes.length;i++){
			if(tagName && (nodes[i].style ? nodes[i].tagName.toLowerCase() : nodes[i].tagName) != tagName) continue;// || numsearch && ++numfound != numsearch

			if(data) data[0](nodes[i], data[1], info, count+1, numfound++ , sResult);
			else sResult.push(nodes[i]);
		}
		
		//commented out :  && (!numsearch || numsearch == numfound)
	},
	
	doQuery : function(htmlNode, qData, info, count, num, sResult){
		var result = null, data = info[count];
		var query = qData[0];
		var returnResult = qData[1];
		try{var qResult = eval(query);}catch(e){return;}
		
		if(returnResult) return sResult.push(qResult);
		if(!qResult) return;
		
		if(data) data[0](htmlNode, data[1], info, count+1, 0, sResult);
		else sResult.push(htmlNode);
	},
	
	getTextNode : function(htmlNode, empty, info, count, num, sResult){
		var result = null, data = info[count];

		var nodes = htmlNode.childNodes;
		for(var i=0;i<nodes.length;i++){
			if(nodes[i].nodeType != 3 && nodes[i].nodeType != 4) continue;
			
			if(data) data[0](nodes[i], data[1], info, count+1, i, sResult);
			else sResult.push(nodes[i]);
		}
	},
	
	getAnyNode : function(htmlNode, empty, info, count, num, sResult){
		var result = null, data = info[count];

		var sel = [], nodes = htmlNode.childNodes;
		for(var i=0;i<nodes.length;i++){
			if(data) data[0](nodes[i], data[1], info, count+1, i, sResult);
			else sResult.push(nodes[i]);
		}
	},
	
	getAttributeNode : function(htmlNode, attrName, info, count, num, sResult){
		if(!htmlNode || htmlNode.nodeType != 1) return;
		
		var result = null, data = info[count];
		var value = htmlNode.getAttributeNode(attrName);//htmlNode.attributes[attrName];//

		if(data) data[0](value, data[1], info, count+1, 0, sResult);
		else if(value) sResult.push(value);
	},
	
	getAllNodes : function(htmlNode, x, info, count, num, sResult){
		var result = null, data = info[count];
		var tagName = x[0];
		var inclSelf = x[1];
		var prefix = x[2];

		if(inclSelf && (htmlNode.tagName == tagName || tagName == "*")){
			if(data) data[0](htmlNode, data[1], info, count+1, 0, sResult);
			else sResult.push(htmlNode);
		}

		var nodes = _$(tagName, htmlNode, tagName==prefix?"":prefix);//htmlNode.getElementsByTagName(tagName);
		for(var i=0;i<nodes.length;i++){
			if(data) data[0](nodes[i], data[1], info, count+1, i, sResult);
			else sResult.push(nodes[i]);
		}
	},
	
	getAllAncestorNodes : function(htmlNode, x, info, count, num, sResult){
		var result = null, data = info[count];
		var tagName = x[0];
		var inclSelf = x[1];
		var prefix = x[2];
		
		var i = 0, s = inclSelf ? htmlNode : htmlNode.parentNode;
		while(s && s.nodeType == 1){
			if(s.tagName == tagName || tagName == "*" || tagName == "node()"){
				if(data) data[0](s, data[1], info, count+1, i++, sResult);
				else sResult.push(s);
			}
			s = s.parentNode
		}
	},
	
	getParentNode : function(htmlNode, empty, info, count, num, sResult){
		var result = null, data = info[count];
		var node = htmlNode.parentNode;
		
		if(data) data[0](node, data[1], info, count+1, 0, sResult);
		else if(node) sResult.push(node);
	},
	
	//precsiblg[3] might not be conform spec
	getPrecedingSibling : function(htmlNode, tagName, info, count, num, sResult){
		var result = null, data = info[count];
		
		var node = htmlNode.previousSibling;
		while(node){
			if(tagName != "node()" && (node.style ? node.tagName.toLowerCase() : node.tagName) != tagName){
				node = node.previousSibling;
				continue;
			}
			
			if(data) data[0](node, data[1], info, count+1, 0, sResult);
			else if(node){
				sResult.push(node);
				break;
			}
		}
	},
	
	//flwsiblg[3] might not be conform spec
	getFollowingSibling : function(htmlNode, tagName, info, count, num, sResult){
		var result = null, data = info[count];
		
		var node = htmlNode.nextSibling;
		while(node){
			if(tagName != "node()" && (node.style ? node.tagName.toLowerCase() : node.tagName) != tagName){
				node = node.nextSibling;
				continue;
			}
			
			if(data) data[0](node, data[1], info, count+1, 0, sResult);
			else if(node){
				sResult.push(node);
				break;
			}
		}
	},
	
	multiXpaths : function(contextNode, list, info, count, num, sResult){
		for(var i=0;i<list.length;i++){
			var info = list[i][0];
			var rootNode = (info[3] ? contextNode.ownerDocument.documentElement : contextNode);//document.body
			info[0](rootNode, info[1], list[i], 1, 0, sResult);
		}
		
		sResult.makeUnique();
	},
	
	compile : function(sExpr){
		sExpr = sExpr.replace(/\[(\d+)\]/g, "/##$1");
		sExpr = sExpr.replace(/\|\|(\d+)\|\|\d+/g, "##$1");
		sExpr = sExpr.replace(/\.\|\|\d+/g, ".");
		sExpr = sExpr.replace(/\[([^\]]*)\]/g, function(match, m1){
			return "/##" + m1.replace(/\|/g, "_@_");
		}); //wrong assumption think of |

		if(sExpr == "/" || sExpr == ".") return sExpr;
		
		//Mark // elements
		//sExpr = sExpr.replace(/\/\//g, "/[]/self::");
		sExpr = sExpr.replace(/\/\//g, "descendant::");
		
		//Check if this is an absolute query
		return this.processXpath(sExpr);
	},
	
	processXpath : function(sExpr){
		var results = new Array();
		sExpr = sExpr.replace(/('[^']*)\|([^']*')/g, "$1_@_$2");
		sExpr = sExpr.split("\|");
		for(var i=0;i<sExpr.length;i++)
			sExpr[i] = sExpr[i].replace(/_\@\_/g, "|");//replace(/('[^']*)\_\@\_([^']*')/g, "$1|$2");
		
		if(sExpr.length == 1) sExpr = sExpr[0];
		else{
			for(var i=0;i<sExpr.length;i++) sExpr[i] = this.processXpath(sExpr[i]);
			results.push([this.multiXpaths, sExpr]);
			return results;
		}
		
		var isAbsolute = sExpr.match(/^\/[^\/]/);
		var sections = sExpr.split("/");
		for(var i=0;i<sections.length;i++){
			if(sections[i] == "." || sections[i] == "") continue;
			else if(sections[i].match(/^[\w-_\.]+(?:\:[\w-_\.]+){0,1}$/)) results.push([this.getChildNode, sections[i]]);//.toUpperCase()
			else if(sections[i].match(/^\#\#(\d+)$/)) results.push([this.doQuery, ["num+1 == " + parseInt(RegExp.$1)]]);
			else if(sections[i].match(/^\#\#(.*)$/)){
				
				//FIX THIS CODE
				var query = RegExp.$1;
				var m = [query.match(/\(/g), query.match(/\)/g)];
				if(m[0] || m[1]){
					while(!m[0] && m[1] || m[0] && !m[1] || m[0].length != m[1].length){
						if(!sections[++i]) break;
						query += "/" + sections[i];
						m = [query.match(/\(/g), query.match(/\)/g)];
					}
				}
				
				results.push([this.doQuery, [this.compileQuery(query)]]);
			}
			else if(sections[i] == "*") results.push([this.getChildNode, null]); //FIX - put in def function
			else if(sections[i].substr(0,2) == "[]") results.push([this.getAllNodes, ["*", false]]);//sections[i].substr(2) || 
			else if(sections[i].match(/descendant-or-self::node\(\)$/)) results.push([this.getAllNodes, ["*", true]]);
			else if(sections[i].match(/descendant-or-self::([^\:]*)(?:\:(.*)){0,1}$/)) results.push([this.getAllNodes, [RegExp.$2 || RegExp.$1, true, RegExp.$1]]);
			else if(sections[i].match(/descendant::([^\:]*)(?:\:(.*)){0,1}$/)) results.push([this.getAllNodes, [RegExp.$2 || RegExp.$1, false, RegExp.$1]]);
			else if(sections[i].match(/ancestor-or-self::([^\:]*)(?:\:(.*)){0,1}$/)) results.push([this.getAllAncestorNodes, [RegExp.$2 || RegExp.$1, true, RegExp.$1]]);
			else if(sections[i].match(/ancestor::([^\:]*)(?:\:(.*)){0,1}$/)) results.push([this.getAllAncestorNodes, [RegExp.$2 || RegExp.$1, false, RegExp.$1]]);
			else if(sections[i].match(/^\@(.*)$/)) results.push([this.getAttributeNode, RegExp.$1]);
			else if(sections[i] == "text()") results.push([this.getTextNode, null]);
			else if(sections[i] == "node()") results.push([this.getAnyNode, null]);//FIX - put in def function
			else if(sections[i] == "..") results.push([this.getParentNode, null]);
			else if(sections[i].match(/following-sibling::(.*)$/)) results.push([this.getFollowingSibling, RegExp.$1.toLowerCase()]);
			else if(sections[i].match(/preceding-sibling::(.*)$/)) results.push([this.getPrecedingSibling, RegExp.$1.toLowerCase()]);
			else if(sections[i].match(/self::(.*)$/)) results.push([this.doQuery, ["XPath.doXpathFunc('local-name', htmlNode) == '" + RegExp.$1 + "'"]]);
			else{
				var query = sections[i];
			
				//FIX THIS CODE
				//add some checking here
				var m = [query.match(/\(/g), query.match(/\)/g)];
				if(m[0] || m[1]){
					while(!m[0] && m[1] || m[0] && !m[1] || m[0].length != m[1].length){
						if(!sections[++i]) break;
						query += "/" + sections[i];
						m = [query.match(/\(/g), query.match(/\)/g)];
					}
				}

				results.push([this.doQuery, [this.compileQuery(query), true]])
			
				//throw new Error(1503, "---- Javeline Error ----\nMessage : Could not match XPath statement: '" + sections[i] + "' in '" + sExpr + "'");
			}
		}

		results[0][3] = isAbsolute;
		return results;
	},
	
	compileQuery : function(code){
		var c = new CodeCompilation(code);
		return c.compile();
	},
	
	doXpathFunc : function(type, arg1, arg2, arg3){
		switch(type){
			case "not": return !arg1;
			case "position()": return num == arg1;
			case "format-number": return new String(Math.round(parseFloat(arg1)*100)/100).replace(/(\.\d?\d?)$/, function(m1){return m1.pad(3, "0", PAD_RIGHT)});; //this should actually do something
			case "floor": return Math.floor(arg1);
			case "ceiling": return Math.ceil(arg1);
			case "starts-with": return arg1 ? arg1.substr(0, arg2.length) == arg2 : false;
			case "string-length": return arg1 ? arg1.length : 0;
			case "count": return arg1 ? arg1.length : 0;
			case "last": return arg1 ? arg1[arg1.length-1] : null;
			case "local-name": return arg1 ? arg1.tagName : "";//[TAGNAME]
			case "substring": return arg1 && arg2 ? arg1.substring(arg2, arg3 || 0) : "";
			case "contains": return arg1 && arg2 ? arg1.indexOf(arg2) > -1 : false;
			case "concat": 
				for(var str="",i=1;i<arguments.length;i++){
					if(typeof arguments[i] == "object"){
						str += getNodeValue(arguments[i][0]);
						continue;
					}
					str += arguments[i];
				}
			return str;
		}
	},
	
	selectNodeExtended : function(sExpr, contextNode, match){
		var sResult = this.selectNodes(sExpr, contextNode);

		if(sResult.length == 0) return null;
		if(!match) return sResult[0];
		
		for(var i=0;i<sResult.length;i++){
			if(getNodeValue(sResult[i]) == match) return sResult[i];
		}
		
		return null;
	},
	
	selectNodes : function(sExpr, contextNode){
		if(!this.cache[sExpr]) this.cache[sExpr] = this.compile(sExpr);
		
		//setStatus("Processing custom XPath: " + sExpr + ":" + contextNode.serialize().replace(/</g, "&lt;"));
		
		if(typeof this.cache[sExpr] == "string"){
			if(this.cache[sExpr] == ".") return [contextNode];
			if(this.cache[sExpr] == "/") return [contextNode.nodeType == 9 ? contextNode : contextNode.ownerDocument.documentElement];
		}

		if(typeof this.cache[sExpr] == "string" && this.cache[sExpr] == ".") return [contextNode];
		
		var info = this.cache[sExpr][0];
		var rootNode = (info[3] && !contextNode.nodeType == 9 ? contextNode.ownerDocument.documentElement : contextNode);//document.body
		var sResult = [];

		info[0](rootNode, info[1], this.cache[sExpr], 1, 0, sResult);
		
		return sResult;
	}
}

function getNodeValue(sResult){
	if(sResult.nodeType == 1) return sResult.firstChild ? sResult.firstChild.nodeValue : "";
	if(sResult.nodeType > 1 || sResult.nodeType < 5) return sResult.nodeValue;
	return sResult;
}

function CodeCompilation(code){
	this.data = {
		F : [],
		S : [],
		I : [],
		X : []
	};
	
	this.compile = function(){
		code = code.replace(/ or /g, " || ");
		code = code.replace(/ and /g, " && ");
		code = code.replace(/!=/g, "{}");
		code = code.replace(/=/g, "==");
		code = code.replace(/\{\}/g, "!=");
		
		// Tokenize
		this.tokenize();
		
		// Insert
		this.insert();

		return code;
	}
	
	this.tokenize = function(){
		//Functions
		var data = this.data.F;
		code = code.replace(/(format-number|contains|substring|local-name|last|node|position|round|starts-with|string|string-length|sum|floor|ceiling|concat|count|not)\s*\(/g, function(d, match){return (data.push(match) - 1) + "F_";});

		//Strings
		var data = this.data.S;
		code = code.replace(/'([^']*)'/g, function(d, match){return (data.push(match) - 1) + "S_";});
		code = code.replace(/"([^"]*)"/g, function(d, match){return (data.push(match) - 1) + "S_";});

		//Xpath
		var data = this.data.X;
		code = code.replace(/(^|\W|\_)([\@\.\/A-Za-z][\.\@\/\w]*(?:\(\)){0,1})/g, function(d, m1, m2){return m1 + (data.push(m2) - 1) + "X_";});
		code = code.replace(/(\.[\.\@\/\w]*)/g, function(d, m1, m2){return (data.push(m1) - 1) + "X_";});
		
		//Ints
		var data = this.data.I; 
		code = code.replace(/(\d+)(\W)/g, function(d, m1, m2){return (data.push(m1) - 1) + "I_" + m2;});
	}
	
	this.insert = function(){
		var data = this.data;
		code = code.replace(/(\d+)X_\s*==\s*(\d+S_)/g, function(d, nr, str){
			return "XPath.selectNodeExtended('" +  data.X[nr].replace(/'/g, "\\'") + "', htmlNode, " + str + ")";
		});
		
		code = code.replace(/(\d+)([FISX])_/g, function(d, nr, type){
			var value = data[type][nr];
			
			if(type == "F"){
				return "XPath.doXpathFunc('" + value + "', ";
			}
			else if(type == "S"){
				return "'" + value + "'";	
			}
			else if(type == "I"){
				return value;
			}
			else if(type == "X"){
				return "XPath.selectNodeExtended('" + value.replace(/'/g, "\\'") + "', htmlNode)";
			}
		});
	}
}

if(IS_SAFARI){
	HTMLDocument.prototype.selectNodes = 
	Element.prototype.selectNodes = 
	XMLDocument.prototype.selectNodes = function(sExpr, contextNode){
		return XPath.selectNodes(sExpr, contextNode || this);
	}
	HTMLDocument.prototype.selectSingleNode = 
	Element.prototype.selectSingleNode = 
	XMLDocument.prototype.selectSingleNode = function(sExpr, contextNode){
		return XPath.selectNodes(sExpr, contextNode || this)[0]; // This could be optimized in the XPath object
	}
}