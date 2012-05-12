grammar XPathEnhancer;

options {
  output   = template;
  rewrite  = true;
}

tokens {
  PATHSEP = '/';
  ABRPATH = '//';
  LPAR    = '(';
  RPAR    = ')';
  LBRAC   = '[';
  RBRAC   = ']';
  MINUS   = '-';
  PLUS    = '+';
  DOT     = '.';
  MUL     = '*';
  DOTDOT  = '..';
  AT      = '@';
  COMMA   = ',';
  PIPE    = '|';
  LESS    = '<';
  MORE    = '>';
  LE      = '<=';
  GE      = '>=';
  COLON   = ':';
  CC      = '::';
  APOS    = '\'';
  QUOT    = '\"';
}

@parser::header {
/*
Based on the XPath 1.0 (http://www.w3.org/TR/1999/REC-xpath-19991116) grammar by Jan-Willem van den Broek, 
version 1.0, downloaded on 2011-06-18 from http://blog.jwbroek.com/2010/07/antlr-grammar-for-parsing-xpath-10.html

That page said "Do with this code as you will."

*/
package org.docx4j.model.datastorage;

}

@lexer::header {
package org.docx4j.model.datastorage;
}

@members {
private String index;
private String prefix;
private int prefixLength;

public static String enhanceXPath(final String prefix, final int index, final String xpath) {
    
  final ANTLRStringStream stringStream = new ANTLRStringStream(xpath);
  final XPathEnhancerLexer xl = new XPathEnhancerLexer(stringStream);
  final TokenRewriteStream trs = new TokenRewriteStream(xl);
  final XPathEnhancerParser xp = new XPathEnhancerParser(trs, index, prefix);
    
  try {
    xp.main();
  } catch (RecognitionException e) {
    throw new IllegalArgumentException(xpath + " is no valid XPath expression", e);
  }
  
  final String enhanced = trs.toString();
  return enhanced;     
}

public XPathEnhancerParser(TokenStream input, final int index, final String prefix) {
    this(input);
    this.index = "" + index;
    this.prefix = prefix;
    this.prefixLength = prefix.length();
}
     
private boolean sharesPrefix(final String path) {
  return path != null && path.startsWith(prefix);
}

private String remainingSuffix(final String path) {
  if (path == null || path.length() < prefixLength) {
    return null;
  } else {
    return path.substring(prefixLength);
  }
}
}

main
  : 
  expr
  ;

locationPath
  :
  relativeLocationPath
  |  absoluteLocationPathNoroot
  ;

absoluteLocationPathNoroot
  :
  originalAbsoluteLocationPathNoroot
  ->  
  template ( sharesPrefix    = { sharesPrefix($text)    },
             commonPrefix    = { prefix                 }, 
             index           = { index                  }, 
             remainingSuffix = { remainingSuffix($text) },
             originalPath    = { $text                  } )
              
           "<if(sharesPrefix)><commonPrefix>[<index>]<remainingSuffix><else><originalPath><endif>"
  ;

originalAbsoluteLocationPathNoroot
  :
  '/' relativeLocationPath
  | '//' relativeLocationPath
  ;

relativeLocationPath
  :
  step
  (
    (
      '/'
      | '//'
    )
    step
  )*
  ;

step
  :
  axisSpecifier nodeTest predicate*
  | abbreviatedStep
  ;

axisSpecifier
  :
  AxisName '::'
  | '@'?
  ;

nodeTest
  :
  nameTest
  | NodeType '(' ')'
  | 'processing-instruction' '(' Literal ')'
  ;

predicate
  :
  '[' expr ']'
  ;

abbreviatedStep
  :
  '.'
  | '..'
  ;

expr
  :
  orExpr
  ;

primaryExpr
  :
  variableReference
  | '(' expr ')'
  | Literal
  | Number
  | functionCall
  ;

functionCall
  :
  functionName '(' (expr (',' expr)*)? ')'
  ;

unionExprNoRoot
  :
  pathExprNoRoot ('|' unionExprNoRoot)?
  | '/' '|' unionExprNoRoot
  ;

pathExprNoRoot
  :
  locationPath
  | filterExpr absoluteLocationPathNoroot ?
  ;

filterExpr
  :
  primaryExpr predicate*
  ;

orExpr
  :
  andExpr ('or' andExpr)*
  ;

andExpr
  :
  equalityExpr ('and' equalityExpr)*
  ;

equalityExpr
  :
  relationalExpr
  (
    (
      '='
      | '!='
    )
    relationalExpr
  )*
  ;

relationalExpr
  :
  additiveExpr
  (
    (
      '<'
      | '>'
      | '<='
      | '>='
    )
    additiveExpr
  )*
  ;

additiveExpr
  :
  multiplicativeExpr
  (
    (
      '+'
      | '-'
    )
    multiplicativeExpr
  )*
  ;

multiplicativeExpr
  :
  unaryExprNoRoot
  (
    (
      '*'
      | 'div'
      | 'mod'
    )
    multiplicativeExpr
  )?
  | '/'
  (
    (
      'div'
      | 'mod'
    )
    multiplicativeExpr
  )?
  ;

unaryExprNoRoot
  :
  '-'* unionExprNoRoot
  ;

qName
  :
  nCName (':' nCName)?
  ;

functionName
  :
  qName // Does not match nodeType, as per spec.
  ;

variableReference
  :
  '$' qName
  ;

nameTest
  :
  '*'
  | nCName ':' '*'
  | qName
  ;

nCName
  :
  NCName
  | AxisName
  ;

NodeType
  :
  'comment'
  | 'text'
  | 'processing-instruction'
  | 'node'
  ;

Number
  :
  Digits ('.' Digits?)?
  | '.' Digits
  ;

fragment
Digits
  :
  ('0'..'9')+
  ;

AxisName
  :
  'ancestor'
  | 'ancestor-or-self'
  | 'attribute'
  | 'child'
  | 'descendant'
  | 'descendant-or-self'
  | 'following'
  | 'following-sibling'
  | 'namespace'
  | 'parent'
  | 'preceding'
  | 'preceding-sibling'
  | 'self'
  ;

Literal
  :
  '"' ~'"'* '"'
  | '\'' ~'\''* '\''
  ;

Whitespace
  :
  (
    ' '
    | '\t'
    | '\n'
    | '\r'
  )+
  
   {
    $channel = HIDDEN;
   }
  ;

NCName
  :
  NCNameStartChar NCNameChar*
  ;

fragment
NCNameStartChar
  :
  'A'..'Z'
  | '_'
  | 'a'..'z'
  | '\u00C0'..'\u00D6'
  | '\u00D8'..'\u00F6'
  | '\u00F8'..'\u02FF'
  | '\u0370'..'\u037D'
  | '\u037F'..'\u1FFF'
  | '\u200C'..'\u200D'
  | '\u2070'..'\u218F'
  | '\u2C00'..'\u2FEF'
  | '\u3001'..'\uD7FF'
  | '\uF900'..'\uFDCF'
  | '\uFDF0'..'\uFFFD'
  | PermittedHighSurrogateChar LowSurrogateChar
  ;

// UniCode supplementary character support added
// 2011-06-18 Karsten Tinnefeld
// cf. http://java.sun.com/javase/technologies/core/basic/intl/faq.jsp
// [#x10000-#xEFFFF] is [#xD800 0xDC00-#xDB7F 0xDFFF] in UTF-16
// cf. http://en.wikipedia.org/wiki/UTF-16/UCS-2
// cf. http://www.w3.org/TR/xml11/#sec-common-syn

fragment
PermittedHighSurrogateChar
  :
  '\uD800'..'\uDB7F'
  ;

fragment
LowSurrogateChar
  :
  '\uDC00'..'\uDFFF'
  ;

fragment
NCNameChar
  :
  NCNameStartChar
  | '-'
  | '.'
  | '0'..'9'
  | '\u00B7'
  | '\u0300'..'\u036F'
  | '\u203F'..'\u2040'
  ;
