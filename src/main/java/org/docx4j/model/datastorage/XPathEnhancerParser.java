// $ANTLR 3.3 Nov 30, 2010 12:45:30 src\\main\\antlr\\XPathEnhancer.g 2011-06-19 16:21:01

/*
Based on the XPath 1.0 (http://www.w3.org/TR/1999/REC-xpath-19991116) grammar by Jan-Willem van den Broek, 
version 1.0, downloaded on 2011-06-18 from http://blog.jwbroek.com/2010/07/antlr-grammar-for-parsing-xpath-10.html
*/
package org.docx4j.model.datastorage;


import java.util.HashMap;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.BitSet;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.Parser;
import org.antlr.runtime.ParserRuleReturnScope;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenRewriteStream;
import org.antlr.runtime.TokenStream;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.AngleBracketTemplateLexer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XPathEnhancerParser extends Parser {
	
	private static Logger log = LoggerFactory.getLogger(XPathEnhancerParser.class);
	
	
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "PATHSEP", "ABRPATH", "LPAR", "RPAR", "LBRAC", "RBRAC", "MINUS", "PLUS", "DOT", "MUL", "DOTDOT", "AT", "COMMA", "PIPE", "LESS", "MORE", "LE", "GE", "COLON", "CC", "APOS", "QUOT", "AxisName", "NodeType", "Literal", "Number", "NCName", "Digits", "Whitespace", "NCNameStartChar", "NCNameChar", "PermittedHighSurrogateChar", "LowSurrogateChar", "'processing-instruction'", "'or'", "'and'", "'='", "'!='", "'div'", "'mod'", "'$'"
    };
    public static final int EOF=-1;
    public static final int T__37=37;
    public static final int T__38=38;
    public static final int T__39=39;
    public static final int T__40=40;
    public static final int T__41=41;
    public static final int T__42=42;
    public static final int T__43=43;
    public static final int T__44=44;
    public static final int PATHSEP=4;
    public static final int ABRPATH=5;
    public static final int LPAR=6;
    public static final int RPAR=7;
    public static final int LBRAC=8;
    public static final int RBRAC=9;
    public static final int MINUS=10;
    public static final int PLUS=11;
    public static final int DOT=12;
    public static final int MUL=13;
    public static final int DOTDOT=14;
    public static final int AT=15;
    public static final int COMMA=16;
    public static final int PIPE=17;
    public static final int LESS=18;
    public static final int MORE=19;
    public static final int LE=20;
    public static final int GE=21;
    public static final int COLON=22;
    public static final int CC=23;
    public static final int APOS=24;
    public static final int QUOT=25;
    public static final int AxisName=26;
    public static final int NodeType=27;
    public static final int Literal=28;
    public static final int Number=29;
    public static final int NCName=30;
    public static final int Digits=31;
    public static final int Whitespace=32;
    public static final int NCNameStartChar=33;
    public static final int NCNameChar=34;
    public static final int PermittedHighSurrogateChar=35;
    public static final int LowSurrogateChar=36;

    // delegates
    // delegators


        public XPathEnhancerParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public XPathEnhancerParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        
    protected StringTemplateGroup templateLib =
      new StringTemplateGroup("XPathEnhancerParserTemplates", AngleBracketTemplateLexer.class);

    public void setTemplateLib(StringTemplateGroup templateLib) {
      this.templateLib = templateLib;
    }
    public StringTemplateGroup getTemplateLib() {
      return templateLib;
    }
    /** allows convenient multi-value initialization:
     *  "new STAttrMap().put(...).put(...)"
     */
    @SuppressWarnings("unchecked")
    public static class STAttrMap extends HashMap {
      public STAttrMap put(String attrName, Object value) {
        super.put(attrName, value);
        return this;
      }
      public STAttrMap put(String attrName, int value) {
        super.put(attrName, new Integer(value));
        return this;
      }
    }

    public String[] getTokenNames() { return XPathEnhancerParser.tokenNames; }
    public String getGrammarFileName() { return "src\\main\\antlr\\XPathEnhancer.g"; }


    private String index;
    private String prefix;
    private int prefixLength;

    public static String enhanceXPath(final String prefix, final int index, final String xpath) {
    	
    	if (log.isDebugEnabled()) {
    		log.debug("prefix: " + prefix);
    		log.debug("index: " + index);
    		log.debug("xpath: " + xpath);
    	}
        
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


    public static class main_return extends ParserRuleReturnScope {
        public StringTemplate st;
        public Object getTemplate() { return st; }
        public String toString() { return st==null?null:st.toString(); }
    };

    // $ANTLR start "main"
    // src\\main\\antlr\\XPathEnhancer.g:87:1: main : expr ;
    public final XPathEnhancerParser.main_return main() throws RecognitionException {
        XPathEnhancerParser.main_return retval = new XPathEnhancerParser.main_return();
        retval.start = input.LT(1);

        try {
            // src\\main\\antlr\\XPathEnhancer.g:88:3: ( expr )
            // src\\main\\antlr\\XPathEnhancer.g:89:3: expr
            {
            pushFollow(FOLLOW_expr_in_main331);
            expr();

            state._fsp--;


            }

            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "main"

    public static class locationPath_return extends ParserRuleReturnScope {
        public StringTemplate st;
        public Object getTemplate() { return st; }
        public String toString() { return st==null?null:st.toString(); }
    };

    // $ANTLR start "locationPath"
    // src\\main\\antlr\\XPathEnhancer.g:92:1: locationPath : ( relativeLocationPath | absoluteLocationPathNoroot );
    public final XPathEnhancerParser.locationPath_return locationPath() throws RecognitionException {
        XPathEnhancerParser.locationPath_return retval = new XPathEnhancerParser.locationPath_return();
        retval.start = input.LT(1);

        try {
            // src\\main\\antlr\\XPathEnhancer.g:93:3: ( relativeLocationPath | absoluteLocationPathNoroot )
            int alt1=2;
            int LA1_0 = input.LA(1);

            if ( ((LA1_0>=DOT && LA1_0<=AT)||(LA1_0>=AxisName && LA1_0<=NodeType)||LA1_0==NCName||LA1_0==37) ) {
                alt1=1;
            }
            else if ( ((LA1_0>=PATHSEP && LA1_0<=ABRPATH)) ) {
                alt1=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 1, 0, input);

                throw nvae;
            }
            switch (alt1) {
                case 1 :
                    // src\\main\\antlr\\XPathEnhancer.g:94:3: relativeLocationPath
                    {
                    pushFollow(FOLLOW_relativeLocationPath_in_locationPath346);
                    relativeLocationPath();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // src\\main\\antlr\\XPathEnhancer.g:95:6: absoluteLocationPathNoroot
                    {
                    pushFollow(FOLLOW_absoluteLocationPathNoroot_in_locationPath353);
                    absoluteLocationPathNoroot();

                    state._fsp--;


                    }
                    break;

            }
            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "locationPath"

    public static class absoluteLocationPathNoroot_return extends ParserRuleReturnScope {
        public StringTemplate st;
        public Object getTemplate() { return st; }
        public String toString() { return st==null?null:st.toString(); }
    };

    // $ANTLR start "absoluteLocationPathNoroot"
    // src\\main\\antlr\\XPathEnhancer.g:98:1: absoluteLocationPathNoroot : originalAbsoluteLocationPathNoroot -> template(sharesPrefix= sharesPrefix($text) commonPrefix= prefix index= index remainingSuffix= remainingSuffix($text) originalPath= $text ) \"<if(sharesPrefix)><commonPrefix>[<index>]<remainingSuffix><else><originalPath><endif>\";
    public final XPathEnhancerParser.absoluteLocationPathNoroot_return absoluteLocationPathNoroot() throws RecognitionException {
        XPathEnhancerParser.absoluteLocationPathNoroot_return retval = new XPathEnhancerParser.absoluteLocationPathNoroot_return();
        retval.start = input.LT(1);

        try {
            // src\\main\\antlr\\XPathEnhancer.g:99:3: ( originalAbsoluteLocationPathNoroot -> template(sharesPrefix= sharesPrefix($text) commonPrefix= prefix index= index remainingSuffix= remainingSuffix($text) originalPath= $text ) \"<if(sharesPrefix)><commonPrefix>[<index>]<remainingSuffix><else><originalPath><endif>\")
            // src\\main\\antlr\\XPathEnhancer.g:100:3: originalAbsoluteLocationPathNoroot
            {
            pushFollow(FOLLOW_originalAbsoluteLocationPathNoroot_in_absoluteLocationPathNoroot368);
            originalAbsoluteLocationPathNoroot();

            state._fsp--;



            // TEMPLATE REWRITE
            // 101:3: -> template(sharesPrefix= sharesPrefix($text) commonPrefix= prefix index= index remainingSuffix= remainingSuffix($text) originalPath= $text ) \"<if(sharesPrefix)><commonPrefix>[<index>]<remainingSuffix><else><originalPath><endif>\"
            {
                retval.st = new StringTemplate(templateLib, "<if(sharesPrefix)><commonPrefix>[<index>]<remainingSuffix><else><originalPath><endif>",
              new STAttrMap().put("sharesPrefix",  sharesPrefix(input.toString(retval.start,input.LT(-1)))    ).put("commonPrefix",  prefix                 ).put("index",  index                  ).put("remainingSuffix",  remainingSuffix(input.toString(retval.start,input.LT(-1))) ).put("originalPath",  input.toString(retval.start,input.LT(-1))                  ));
            }

            ((TokenRewriteStream)input).replace(
              ((Token)retval.start).getTokenIndex(),
              input.LT(-1).getTokenIndex(),
              retval.st);
            }

            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "absoluteLocationPathNoroot"

    public static class originalAbsoluteLocationPathNoroot_return extends ParserRuleReturnScope {
        public StringTemplate st;
        public Object getTemplate() { return st; }
        public String toString() { return st==null?null:st.toString(); }
    };

    // $ANTLR start "originalAbsoluteLocationPathNoroot"
    // src\\main\\antlr\\XPathEnhancer.g:111:1: originalAbsoluteLocationPathNoroot : ( '/' relativeLocationPath | '//' relativeLocationPath );
    public final XPathEnhancerParser.originalAbsoluteLocationPathNoroot_return originalAbsoluteLocationPathNoroot() throws RecognitionException {
        XPathEnhancerParser.originalAbsoluteLocationPathNoroot_return retval = new XPathEnhancerParser.originalAbsoluteLocationPathNoroot_return();
        retval.start = input.LT(1);

        try {
            // src\\main\\antlr\\XPathEnhancer.g:112:3: ( '/' relativeLocationPath | '//' relativeLocationPath )
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0==PATHSEP) ) {
                alt2=1;
            }
            else if ( (LA2_0==ABRPATH) ) {
                alt2=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 2, 0, input);

                throw nvae;
            }
            switch (alt2) {
                case 1 :
                    // src\\main\\antlr\\XPathEnhancer.g:113:3: '/' relativeLocationPath
                    {
                    match(input,PATHSEP,FOLLOW_PATHSEP_in_originalAbsoluteLocationPathNoroot532); 
                    pushFollow(FOLLOW_relativeLocationPath_in_originalAbsoluteLocationPathNoroot534);
                    relativeLocationPath();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // src\\main\\antlr\\XPathEnhancer.g:114:5: '//' relativeLocationPath
                    {
                    match(input,ABRPATH,FOLLOW_ABRPATH_in_originalAbsoluteLocationPathNoroot540); 
                    pushFollow(FOLLOW_relativeLocationPath_in_originalAbsoluteLocationPathNoroot542);
                    relativeLocationPath();

                    state._fsp--;


                    }
                    break;

            }
            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "originalAbsoluteLocationPathNoroot"

    public static class relativeLocationPath_return extends ParserRuleReturnScope {
        public StringTemplate st;
        public Object getTemplate() { return st; }
        public String toString() { return st==null?null:st.toString(); }
    };

    // $ANTLR start "relativeLocationPath"
    // src\\main\\antlr\\XPathEnhancer.g:117:1: relativeLocationPath : step ( ( '/' | '//' ) step )* ;
    public final XPathEnhancerParser.relativeLocationPath_return relativeLocationPath() throws RecognitionException {
        XPathEnhancerParser.relativeLocationPath_return retval = new XPathEnhancerParser.relativeLocationPath_return();
        retval.start = input.LT(1);

        try {
            // src\\main\\antlr\\XPathEnhancer.g:118:3: ( step ( ( '/' | '//' ) step )* )
            // src\\main\\antlr\\XPathEnhancer.g:119:3: step ( ( '/' | '//' ) step )*
            {
            pushFollow(FOLLOW_step_in_relativeLocationPath557);
            step();

            state._fsp--;

            // src\\main\\antlr\\XPathEnhancer.g:120:3: ( ( '/' | '//' ) step )*
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( ((LA3_0>=PATHSEP && LA3_0<=ABRPATH)) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // src\\main\\antlr\\XPathEnhancer.g:121:5: ( '/' | '//' ) step
            	    {
            	    if ( (input.LA(1)>=PATHSEP && input.LA(1)<=ABRPATH) ) {
            	        input.consume();
            	        state.errorRecovery=false;
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        throw mse;
            	    }

            	    pushFollow(FOLLOW_step_in_relativeLocationPath597);
            	    step();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop3;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "relativeLocationPath"

    public static class step_return extends ParserRuleReturnScope {
        public StringTemplate st;
        public Object getTemplate() { return st; }
        public String toString() { return st==null?null:st.toString(); }
    };

    // $ANTLR start "step"
    // src\\main\\antlr\\XPathEnhancer.g:129:1: step : ( axisSpecifier nodeTest ( predicate )* | abbreviatedStep );
    public final XPathEnhancerParser.step_return step() throws RecognitionException {
        XPathEnhancerParser.step_return retval = new XPathEnhancerParser.step_return();
        retval.start = input.LT(1);

        try {
            // src\\main\\antlr\\XPathEnhancer.g:130:3: ( axisSpecifier nodeTest ( predicate )* | abbreviatedStep )
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==MUL||LA5_0==AT||(LA5_0>=AxisName && LA5_0<=NodeType)||LA5_0==NCName||LA5_0==37) ) {
                alt5=1;
            }
            else if ( (LA5_0==DOT||LA5_0==DOTDOT) ) {
                alt5=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 5, 0, input);

                throw nvae;
            }
            switch (alt5) {
                case 1 :
                    // src\\main\\antlr\\XPathEnhancer.g:131:3: axisSpecifier nodeTest ( predicate )*
                    {
                    pushFollow(FOLLOW_axisSpecifier_in_step617);
                    axisSpecifier();

                    state._fsp--;

                    pushFollow(FOLLOW_nodeTest_in_step619);
                    nodeTest();

                    state._fsp--;

                    // src\\main\\antlr\\XPathEnhancer.g:131:26: ( predicate )*
                    loop4:
                    do {
                        int alt4=2;
                        int LA4_0 = input.LA(1);

                        if ( (LA4_0==LBRAC) ) {
                            alt4=1;
                        }


                        switch (alt4) {
                    	case 1 :
                    	    // src\\main\\antlr\\XPathEnhancer.g:131:26: predicate
                    	    {
                    	    pushFollow(FOLLOW_predicate_in_step621);
                    	    predicate();

                    	    state._fsp--;


                    	    }
                    	    break;

                    	default :
                    	    break loop4;
                        }
                    } while (true);


                    }
                    break;
                case 2 :
                    // src\\main\\antlr\\XPathEnhancer.g:132:5: abbreviatedStep
                    {
                    pushFollow(FOLLOW_abbreviatedStep_in_step628);
                    abbreviatedStep();

                    state._fsp--;


                    }
                    break;

            }
            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "step"

    public static class axisSpecifier_return extends ParserRuleReturnScope {
        public StringTemplate st;
        public Object getTemplate() { return st; }
        public String toString() { return st==null?null:st.toString(); }
    };

    // $ANTLR start "axisSpecifier"
    // src\\main\\antlr\\XPathEnhancer.g:135:1: axisSpecifier : ( AxisName '::' | ( '@' )? );
    public final XPathEnhancerParser.axisSpecifier_return axisSpecifier() throws RecognitionException {
        XPathEnhancerParser.axisSpecifier_return retval = new XPathEnhancerParser.axisSpecifier_return();
        retval.start = input.LT(1);

        try {
            // src\\main\\antlr\\XPathEnhancer.g:136:3: ( AxisName '::' | ( '@' )? )
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0==AxisName) ) {
                int LA7_1 = input.LA(2);

                if ( (LA7_1==CC) ) {
                    alt7=1;
                }
                else if ( (LA7_1==EOF||(LA7_1>=PATHSEP && LA7_1<=ABRPATH)||(LA7_1>=RPAR && LA7_1<=PLUS)||LA7_1==MUL||(LA7_1>=COMMA && LA7_1<=COLON)||(LA7_1>=38 && LA7_1<=43)) ) {
                    alt7=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 7, 1, input);

                    throw nvae;
                }
            }
            else if ( (LA7_0==MUL||LA7_0==AT||LA7_0==NodeType||LA7_0==NCName||LA7_0==37) ) {
                alt7=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 7, 0, input);

                throw nvae;
            }
            switch (alt7) {
                case 1 :
                    // src\\main\\antlr\\XPathEnhancer.g:137:3: AxisName '::'
                    {
                    match(input,AxisName,FOLLOW_AxisName_in_axisSpecifier643); 
                    match(input,CC,FOLLOW_CC_in_axisSpecifier645); 

                    }
                    break;
                case 2 :
                    // src\\main\\antlr\\XPathEnhancer.g:138:5: ( '@' )?
                    {
                    // src\\main\\antlr\\XPathEnhancer.g:138:5: ( '@' )?
                    int alt6=2;
                    int LA6_0 = input.LA(1);

                    if ( (LA6_0==AT) ) {
                        alt6=1;
                    }
                    switch (alt6) {
                        case 1 :
                            // src\\main\\antlr\\XPathEnhancer.g:138:5: '@'
                            {
                            match(input,AT,FOLLOW_AT_in_axisSpecifier651); 

                            }
                            break;

                    }


                    }
                    break;

            }
            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "axisSpecifier"

    public static class nodeTest_return extends ParserRuleReturnScope {
        public StringTemplate st;
        public Object getTemplate() { return st; }
        public String toString() { return st==null?null:st.toString(); }
    };

    // $ANTLR start "nodeTest"
    // src\\main\\antlr\\XPathEnhancer.g:141:1: nodeTest : ( nameTest | NodeType '(' ')' | 'processing-instruction' '(' Literal ')' );
    public final XPathEnhancerParser.nodeTest_return nodeTest() throws RecognitionException {
        XPathEnhancerParser.nodeTest_return retval = new XPathEnhancerParser.nodeTest_return();
        retval.start = input.LT(1);

        try {
            // src\\main\\antlr\\XPathEnhancer.g:142:3: ( nameTest | NodeType '(' ')' | 'processing-instruction' '(' Literal ')' )
            int alt8=3;
            switch ( input.LA(1) ) {
            case MUL:
            case AxisName:
            case NCName:
                {
                alt8=1;
                }
                break;
            case NodeType:
                {
                alt8=2;
                }
                break;
            case 37:
                {
                alt8=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 8, 0, input);

                throw nvae;
            }

            switch (alt8) {
                case 1 :
                    // src\\main\\antlr\\XPathEnhancer.g:143:3: nameTest
                    {
                    pushFollow(FOLLOW_nameTest_in_nodeTest667);
                    nameTest();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // src\\main\\antlr\\XPathEnhancer.g:144:5: NodeType '(' ')'
                    {
                    match(input,NodeType,FOLLOW_NodeType_in_nodeTest673); 
                    match(input,LPAR,FOLLOW_LPAR_in_nodeTest675); 
                    match(input,RPAR,FOLLOW_RPAR_in_nodeTest677); 

                    }
                    break;
                case 3 :
                    // src\\main\\antlr\\XPathEnhancer.g:145:5: 'processing-instruction' '(' Literal ')'
                    {
                    match(input,37,FOLLOW_37_in_nodeTest683); 
                    match(input,LPAR,FOLLOW_LPAR_in_nodeTest685); 
                    match(input,Literal,FOLLOW_Literal_in_nodeTest687); 
                    match(input,RPAR,FOLLOW_RPAR_in_nodeTest689); 

                    }
                    break;

            }
            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "nodeTest"

    public static class predicate_return extends ParserRuleReturnScope {
        public StringTemplate st;
        public Object getTemplate() { return st; }
        public String toString() { return st==null?null:st.toString(); }
    };

    // $ANTLR start "predicate"
    // src\\main\\antlr\\XPathEnhancer.g:148:1: predicate : '[' expr ']' ;
    public final XPathEnhancerParser.predicate_return predicate() throws RecognitionException {
        XPathEnhancerParser.predicate_return retval = new XPathEnhancerParser.predicate_return();
        retval.start = input.LT(1);

        try {
            // src\\main\\antlr\\XPathEnhancer.g:149:3: ( '[' expr ']' )
            // src\\main\\antlr\\XPathEnhancer.g:150:3: '[' expr ']'
            {
            match(input,LBRAC,FOLLOW_LBRAC_in_predicate704); 
            pushFollow(FOLLOW_expr_in_predicate706);
            expr();

            state._fsp--;

            match(input,RBRAC,FOLLOW_RBRAC_in_predicate708); 

            }

            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "predicate"

    public static class abbreviatedStep_return extends ParserRuleReturnScope {
        public StringTemplate st;
        public Object getTemplate() { return st; }
        public String toString() { return st==null?null:st.toString(); }
    };

    // $ANTLR start "abbreviatedStep"
    // src\\main\\antlr\\XPathEnhancer.g:153:1: abbreviatedStep : ( '.' | '..' );
    public final XPathEnhancerParser.abbreviatedStep_return abbreviatedStep() throws RecognitionException {
        XPathEnhancerParser.abbreviatedStep_return retval = new XPathEnhancerParser.abbreviatedStep_return();
        retval.start = input.LT(1);

        try {
            // src\\main\\antlr\\XPathEnhancer.g:154:3: ( '.' | '..' )
            // src\\main\\antlr\\XPathEnhancer.g:
            {
            if ( input.LA(1)==DOT||input.LA(1)==DOTDOT ) {
                input.consume();
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "abbreviatedStep"

    public static class expr_return extends ParserRuleReturnScope {
        public StringTemplate st;
        public Object getTemplate() { return st; }
        public String toString() { return st==null?null:st.toString(); }
    };

    // $ANTLR start "expr"
    // src\\main\\antlr\\XPathEnhancer.g:159:1: expr : orExpr ;
    public final XPathEnhancerParser.expr_return expr() throws RecognitionException {
        XPathEnhancerParser.expr_return retval = new XPathEnhancerParser.expr_return();
        retval.start = input.LT(1);

        try {
            // src\\main\\antlr\\XPathEnhancer.g:160:3: ( orExpr )
            // src\\main\\antlr\\XPathEnhancer.g:161:3: orExpr
            {
            pushFollow(FOLLOW_orExpr_in_expr744);
            orExpr();

            state._fsp--;


            }

            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "expr"

    public static class primaryExpr_return extends ParserRuleReturnScope {
        public StringTemplate st;
        public Object getTemplate() { return st; }
        public String toString() { return st==null?null:st.toString(); }
    };

    // $ANTLR start "primaryExpr"
    // src\\main\\antlr\\XPathEnhancer.g:164:1: primaryExpr : ( variableReference | '(' expr ')' | Literal | Number | functionCall );
    public final XPathEnhancerParser.primaryExpr_return primaryExpr() throws RecognitionException {
        XPathEnhancerParser.primaryExpr_return retval = new XPathEnhancerParser.primaryExpr_return();
        retval.start = input.LT(1);

        try {
            // src\\main\\antlr\\XPathEnhancer.g:165:3: ( variableReference | '(' expr ')' | Literal | Number | functionCall )
            int alt9=5;
            switch ( input.LA(1) ) {
            case 44:
                {
                alt9=1;
                }
                break;
            case LPAR:
                {
                alt9=2;
                }
                break;
            case Literal:
                {
                alt9=3;
                }
                break;
            case Number:
                {
                alt9=4;
                }
                break;
            case AxisName:
            case NCName:
                {
                alt9=5;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 9, 0, input);

                throw nvae;
            }

            switch (alt9) {
                case 1 :
                    // src\\main\\antlr\\XPathEnhancer.g:166:3: variableReference
                    {
                    pushFollow(FOLLOW_variableReference_in_primaryExpr759);
                    variableReference();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // src\\main\\antlr\\XPathEnhancer.g:167:5: '(' expr ')'
                    {
                    match(input,LPAR,FOLLOW_LPAR_in_primaryExpr765); 
                    pushFollow(FOLLOW_expr_in_primaryExpr767);
                    expr();

                    state._fsp--;

                    match(input,RPAR,FOLLOW_RPAR_in_primaryExpr769); 

                    }
                    break;
                case 3 :
                    // src\\main\\antlr\\XPathEnhancer.g:168:5: Literal
                    {
                    match(input,Literal,FOLLOW_Literal_in_primaryExpr775); 

                    }
                    break;
                case 4 :
                    // src\\main\\antlr\\XPathEnhancer.g:169:5: Number
                    {
                    match(input,Number,FOLLOW_Number_in_primaryExpr781); 

                    }
                    break;
                case 5 :
                    // src\\main\\antlr\\XPathEnhancer.g:170:5: functionCall
                    {
                    pushFollow(FOLLOW_functionCall_in_primaryExpr787);
                    functionCall();

                    state._fsp--;


                    }
                    break;

            }
            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "primaryExpr"

    public static class functionCall_return extends ParserRuleReturnScope {
        public StringTemplate st;
        public Object getTemplate() { return st; }
        public String toString() { return st==null?null:st.toString(); }
    };

    // $ANTLR start "functionCall"
    // src\\main\\antlr\\XPathEnhancer.g:173:1: functionCall : functionName '(' ( expr ( ',' expr )* )? ')' ;
    public final XPathEnhancerParser.functionCall_return functionCall() throws RecognitionException {
        XPathEnhancerParser.functionCall_return retval = new XPathEnhancerParser.functionCall_return();
        retval.start = input.LT(1);

        try {
            // src\\main\\antlr\\XPathEnhancer.g:174:3: ( functionName '(' ( expr ( ',' expr )* )? ')' )
            // src\\main\\antlr\\XPathEnhancer.g:175:3: functionName '(' ( expr ( ',' expr )* )? ')'
            {
            pushFollow(FOLLOW_functionName_in_functionCall802);
            functionName();

            state._fsp--;

            match(input,LPAR,FOLLOW_LPAR_in_functionCall804); 
            // src\\main\\antlr\\XPathEnhancer.g:175:20: ( expr ( ',' expr )* )?
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( ((LA11_0>=PATHSEP && LA11_0<=LPAR)||LA11_0==MINUS||(LA11_0>=DOT && LA11_0<=AT)||(LA11_0>=AxisName && LA11_0<=NCName)||LA11_0==37||LA11_0==44) ) {
                alt11=1;
            }
            switch (alt11) {
                case 1 :
                    // src\\main\\antlr\\XPathEnhancer.g:175:21: expr ( ',' expr )*
                    {
                    pushFollow(FOLLOW_expr_in_functionCall807);
                    expr();

                    state._fsp--;

                    // src\\main\\antlr\\XPathEnhancer.g:175:26: ( ',' expr )*
                    loop10:
                    do {
                        int alt10=2;
                        int LA10_0 = input.LA(1);

                        if ( (LA10_0==COMMA) ) {
                            alt10=1;
                        }


                        switch (alt10) {
                    	case 1 :
                    	    // src\\main\\antlr\\XPathEnhancer.g:175:27: ',' expr
                    	    {
                    	    match(input,COMMA,FOLLOW_COMMA_in_functionCall810); 
                    	    pushFollow(FOLLOW_expr_in_functionCall812);
                    	    expr();

                    	    state._fsp--;


                    	    }
                    	    break;

                    	default :
                    	    break loop10;
                        }
                    } while (true);


                    }
                    break;

            }

            match(input,RPAR,FOLLOW_RPAR_in_functionCall818); 

            }

            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "functionCall"

    public static class unionExprNoRoot_return extends ParserRuleReturnScope {
        public StringTemplate st;
        public Object getTemplate() { return st; }
        public String toString() { return st==null?null:st.toString(); }
    };

    // $ANTLR start "unionExprNoRoot"
    // src\\main\\antlr\\XPathEnhancer.g:178:1: unionExprNoRoot : ( pathExprNoRoot ( '|' unionExprNoRoot )? | '/' '|' unionExprNoRoot );
    public final XPathEnhancerParser.unionExprNoRoot_return unionExprNoRoot() throws RecognitionException {
        XPathEnhancerParser.unionExprNoRoot_return retval = new XPathEnhancerParser.unionExprNoRoot_return();
        retval.start = input.LT(1);

        try {
            // src\\main\\antlr\\XPathEnhancer.g:179:3: ( pathExprNoRoot ( '|' unionExprNoRoot )? | '/' '|' unionExprNoRoot )
            int alt13=2;
            int LA13_0 = input.LA(1);

            if ( ((LA13_0>=ABRPATH && LA13_0<=LPAR)||(LA13_0>=DOT && LA13_0<=AT)||(LA13_0>=AxisName && LA13_0<=NCName)||LA13_0==37||LA13_0==44) ) {
                alt13=1;
            }
            else if ( (LA13_0==PATHSEP) ) {
                int LA13_2 = input.LA(2);

                if ( (LA13_2==PIPE) ) {
                    alt13=2;
                }
                else if ( ((LA13_2>=DOT && LA13_2<=AT)||(LA13_2>=AxisName && LA13_2<=NodeType)||LA13_2==NCName||LA13_2==37) ) {
                    alt13=1;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 13, 2, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 13, 0, input);

                throw nvae;
            }
            switch (alt13) {
                case 1 :
                    // src\\main\\antlr\\XPathEnhancer.g:180:3: pathExprNoRoot ( '|' unionExprNoRoot )?
                    {
                    pushFollow(FOLLOW_pathExprNoRoot_in_unionExprNoRoot833);
                    pathExprNoRoot();

                    state._fsp--;

                    // src\\main\\antlr\\XPathEnhancer.g:180:18: ( '|' unionExprNoRoot )?
                    int alt12=2;
                    int LA12_0 = input.LA(1);

                    if ( (LA12_0==PIPE) ) {
                        alt12=1;
                    }
                    switch (alt12) {
                        case 1 :
                            // src\\main\\antlr\\XPathEnhancer.g:180:19: '|' unionExprNoRoot
                            {
                            match(input,PIPE,FOLLOW_PIPE_in_unionExprNoRoot836); 
                            pushFollow(FOLLOW_unionExprNoRoot_in_unionExprNoRoot838);
                            unionExprNoRoot();

                            state._fsp--;


                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // src\\main\\antlr\\XPathEnhancer.g:181:5: '/' '|' unionExprNoRoot
                    {
                    match(input,PATHSEP,FOLLOW_PATHSEP_in_unionExprNoRoot846); 
                    match(input,PIPE,FOLLOW_PIPE_in_unionExprNoRoot848); 
                    pushFollow(FOLLOW_unionExprNoRoot_in_unionExprNoRoot850);
                    unionExprNoRoot();

                    state._fsp--;


                    }
                    break;

            }
            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "unionExprNoRoot"

    public static class pathExprNoRoot_return extends ParserRuleReturnScope {
        public StringTemplate st;
        public Object getTemplate() { return st; }
        public String toString() { return st==null?null:st.toString(); }
    };

    // $ANTLR start "pathExprNoRoot"
    // src\\main\\antlr\\XPathEnhancer.g:184:1: pathExprNoRoot : ( locationPath | filterExpr ( absoluteLocationPathNoroot )? );
    public final XPathEnhancerParser.pathExprNoRoot_return pathExprNoRoot() throws RecognitionException {
        XPathEnhancerParser.pathExprNoRoot_return retval = new XPathEnhancerParser.pathExprNoRoot_return();
        retval.start = input.LT(1);

        try {
            // src\\main\\antlr\\XPathEnhancer.g:185:3: ( locationPath | filterExpr ( absoluteLocationPathNoroot )? )
            int alt15=2;
            switch ( input.LA(1) ) {
            case AxisName:
                {
                switch ( input.LA(2) ) {
                case EOF:
                case PATHSEP:
                case ABRPATH:
                case RPAR:
                case LBRAC:
                case RBRAC:
                case MINUS:
                case PLUS:
                case MUL:
                case COMMA:
                case PIPE:
                case LESS:
                case MORE:
                case LE:
                case GE:
                case CC:
                case 38:
                case 39:
                case 40:
                case 41:
                case 42:
                case 43:
                    {
                    alt15=1;
                    }
                    break;
                case COLON:
                    {
                    int LA15_5 = input.LA(3);

                    if ( (LA15_5==MUL) ) {
                        alt15=1;
                    }
                    else if ( (LA15_5==AxisName||LA15_5==NCName) ) {
                        int LA15_6 = input.LA(4);

                        if ( (LA15_6==EOF||(LA15_6>=PATHSEP && LA15_6<=ABRPATH)||(LA15_6>=RPAR && LA15_6<=PLUS)||LA15_6==MUL||(LA15_6>=COMMA && LA15_6<=GE)||(LA15_6>=38 && LA15_6<=43)) ) {
                            alt15=1;
                        }
                        else if ( (LA15_6==LPAR) ) {
                            alt15=2;
                        }
                        else {
                            NoViableAltException nvae =
                                new NoViableAltException("", 15, 6, input);

                            throw nvae;
                        }
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 15, 5, input);

                        throw nvae;
                    }
                    }
                    break;
                case LPAR:
                    {
                    alt15=2;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 15, 1, input);

                    throw nvae;
                }

                }
                break;
            case PATHSEP:
            case ABRPATH:
            case DOT:
            case MUL:
            case DOTDOT:
            case AT:
            case NodeType:
            case 37:
                {
                alt15=1;
                }
                break;
            case NCName:
                {
                switch ( input.LA(2) ) {
                case COLON:
                    {
                    int LA15_5 = input.LA(3);

                    if ( (LA15_5==MUL) ) {
                        alt15=1;
                    }
                    else if ( (LA15_5==AxisName||LA15_5==NCName) ) {
                        int LA15_6 = input.LA(4);

                        if ( (LA15_6==EOF||(LA15_6>=PATHSEP && LA15_6<=ABRPATH)||(LA15_6>=RPAR && LA15_6<=PLUS)||LA15_6==MUL||(LA15_6>=COMMA && LA15_6<=GE)||(LA15_6>=38 && LA15_6<=43)) ) {
                            alt15=1;
                        }
                        else if ( (LA15_6==LPAR) ) {
                            alt15=2;
                        }
                        else {
                            NoViableAltException nvae =
                                new NoViableAltException("", 15, 6, input);

                            throw nvae;
                        }
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 15, 5, input);

                        throw nvae;
                    }
                    }
                    break;
                case EOF:
                case PATHSEP:
                case ABRPATH:
                case RPAR:
                case LBRAC:
                case RBRAC:
                case MINUS:
                case PLUS:
                case MUL:
                case COMMA:
                case PIPE:
                case LESS:
                case MORE:
                case LE:
                case GE:
                case 38:
                case 39:
                case 40:
                case 41:
                case 42:
                case 43:
                    {
                    alt15=1;
                    }
                    break;
                case LPAR:
                    {
                    alt15=2;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 15, 3, input);

                    throw nvae;
                }

                }
                break;
            case LPAR:
            case Literal:
            case Number:
            case 44:
                {
                alt15=2;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 15, 0, input);

                throw nvae;
            }

            switch (alt15) {
                case 1 :
                    // src\\main\\antlr\\XPathEnhancer.g:186:3: locationPath
                    {
                    pushFollow(FOLLOW_locationPath_in_pathExprNoRoot865);
                    locationPath();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // src\\main\\antlr\\XPathEnhancer.g:187:5: filterExpr ( absoluteLocationPathNoroot )?
                    {
                    pushFollow(FOLLOW_filterExpr_in_pathExprNoRoot871);
                    filterExpr();

                    state._fsp--;

                    // src\\main\\antlr\\XPathEnhancer.g:187:16: ( absoluteLocationPathNoroot )?
                    int alt14=2;
                    int LA14_0 = input.LA(1);

                    if ( ((LA14_0>=PATHSEP && LA14_0<=ABRPATH)) ) {
                        alt14=1;
                    }
                    switch (alt14) {
                        case 1 :
                            // src\\main\\antlr\\XPathEnhancer.g:187:16: absoluteLocationPathNoroot
                            {
                            pushFollow(FOLLOW_absoluteLocationPathNoroot_in_pathExprNoRoot873);
                            absoluteLocationPathNoroot();

                            state._fsp--;


                            }
                            break;

                    }


                    }
                    break;

            }
            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "pathExprNoRoot"

    public static class filterExpr_return extends ParserRuleReturnScope {
        public StringTemplate st;
        public Object getTemplate() { return st; }
        public String toString() { return st==null?null:st.toString(); }
    };

    // $ANTLR start "filterExpr"
    // src\\main\\antlr\\XPathEnhancer.g:190:1: filterExpr : primaryExpr ( predicate )* ;
    public final XPathEnhancerParser.filterExpr_return filterExpr() throws RecognitionException {
        XPathEnhancerParser.filterExpr_return retval = new XPathEnhancerParser.filterExpr_return();
        retval.start = input.LT(1);

        try {
            // src\\main\\antlr\\XPathEnhancer.g:191:3: ( primaryExpr ( predicate )* )
            // src\\main\\antlr\\XPathEnhancer.g:192:3: primaryExpr ( predicate )*
            {
            pushFollow(FOLLOW_primaryExpr_in_filterExpr890);
            primaryExpr();

            state._fsp--;

            // src\\main\\antlr\\XPathEnhancer.g:192:15: ( predicate )*
            loop16:
            do {
                int alt16=2;
                int LA16_0 = input.LA(1);

                if ( (LA16_0==LBRAC) ) {
                    alt16=1;
                }


                switch (alt16) {
            	case 1 :
            	    // src\\main\\antlr\\XPathEnhancer.g:192:15: predicate
            	    {
            	    pushFollow(FOLLOW_predicate_in_filterExpr892);
            	    predicate();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop16;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "filterExpr"

    public static class orExpr_return extends ParserRuleReturnScope {
        public StringTemplate st;
        public Object getTemplate() { return st; }
        public String toString() { return st==null?null:st.toString(); }
    };

    // $ANTLR start "orExpr"
    // src\\main\\antlr\\XPathEnhancer.g:195:1: orExpr : andExpr ( 'or' andExpr )* ;
    public final XPathEnhancerParser.orExpr_return orExpr() throws RecognitionException {
        XPathEnhancerParser.orExpr_return retval = new XPathEnhancerParser.orExpr_return();
        retval.start = input.LT(1);

        try {
            // src\\main\\antlr\\XPathEnhancer.g:196:3: ( andExpr ( 'or' andExpr )* )
            // src\\main\\antlr\\XPathEnhancer.g:197:3: andExpr ( 'or' andExpr )*
            {
            pushFollow(FOLLOW_andExpr_in_orExpr908);
            andExpr();

            state._fsp--;

            // src\\main\\antlr\\XPathEnhancer.g:197:11: ( 'or' andExpr )*
            loop17:
            do {
                int alt17=2;
                int LA17_0 = input.LA(1);

                if ( (LA17_0==38) ) {
                    alt17=1;
                }


                switch (alt17) {
            	case 1 :
            	    // src\\main\\antlr\\XPathEnhancer.g:197:12: 'or' andExpr
            	    {
            	    match(input,38,FOLLOW_38_in_orExpr911); 
            	    pushFollow(FOLLOW_andExpr_in_orExpr913);
            	    andExpr();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop17;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "orExpr"

    public static class andExpr_return extends ParserRuleReturnScope {
        public StringTemplate st;
        public Object getTemplate() { return st; }
        public String toString() { return st==null?null:st.toString(); }
    };

    // $ANTLR start "andExpr"
    // src\\main\\antlr\\XPathEnhancer.g:200:1: andExpr : equalityExpr ( 'and' equalityExpr )* ;
    public final XPathEnhancerParser.andExpr_return andExpr() throws RecognitionException {
        XPathEnhancerParser.andExpr_return retval = new XPathEnhancerParser.andExpr_return();
        retval.start = input.LT(1);

        try {
            // src\\main\\antlr\\XPathEnhancer.g:201:3: ( equalityExpr ( 'and' equalityExpr )* )
            // src\\main\\antlr\\XPathEnhancer.g:202:3: equalityExpr ( 'and' equalityExpr )*
            {
            pushFollow(FOLLOW_equalityExpr_in_andExpr930);
            equalityExpr();

            state._fsp--;

            // src\\main\\antlr\\XPathEnhancer.g:202:16: ( 'and' equalityExpr )*
            loop18:
            do {
                int alt18=2;
                int LA18_0 = input.LA(1);

                if ( (LA18_0==39) ) {
                    alt18=1;
                }


                switch (alt18) {
            	case 1 :
            	    // src\\main\\antlr\\XPathEnhancer.g:202:17: 'and' equalityExpr
            	    {
            	    match(input,39,FOLLOW_39_in_andExpr933); 
            	    pushFollow(FOLLOW_equalityExpr_in_andExpr935);
            	    equalityExpr();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop18;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "andExpr"

    public static class equalityExpr_return extends ParserRuleReturnScope {
        public StringTemplate st;
        public Object getTemplate() { return st; }
        public String toString() { return st==null?null:st.toString(); }
    };

    // $ANTLR start "equalityExpr"
    // src\\main\\antlr\\XPathEnhancer.g:205:1: equalityExpr : relationalExpr ( ( '=' | '!=' ) relationalExpr )* ;
    public final XPathEnhancerParser.equalityExpr_return equalityExpr() throws RecognitionException {
        XPathEnhancerParser.equalityExpr_return retval = new XPathEnhancerParser.equalityExpr_return();
        retval.start = input.LT(1);

        try {
            // src\\main\\antlr\\XPathEnhancer.g:206:3: ( relationalExpr ( ( '=' | '!=' ) relationalExpr )* )
            // src\\main\\antlr\\XPathEnhancer.g:207:3: relationalExpr ( ( '=' | '!=' ) relationalExpr )*
            {
            pushFollow(FOLLOW_relationalExpr_in_equalityExpr952);
            relationalExpr();

            state._fsp--;

            // src\\main\\antlr\\XPathEnhancer.g:208:3: ( ( '=' | '!=' ) relationalExpr )*
            loop19:
            do {
                int alt19=2;
                int LA19_0 = input.LA(1);

                if ( ((LA19_0>=40 && LA19_0<=41)) ) {
                    alt19=1;
                }


                switch (alt19) {
            	case 1 :
            	    // src\\main\\antlr\\XPathEnhancer.g:209:5: ( '=' | '!=' ) relationalExpr
            	    {
            	    if ( (input.LA(1)>=40 && input.LA(1)<=41) ) {
            	        input.consume();
            	        state.errorRecovery=false;
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        throw mse;
            	    }

            	    pushFollow(FOLLOW_relationalExpr_in_equalityExpr992);
            	    relationalExpr();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop19;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "equalityExpr"

    public static class relationalExpr_return extends ParserRuleReturnScope {
        public StringTemplate st;
        public Object getTemplate() { return st; }
        public String toString() { return st==null?null:st.toString(); }
    };

    // $ANTLR start "relationalExpr"
    // src\\main\\antlr\\XPathEnhancer.g:217:1: relationalExpr : additiveExpr ( ( '<' | '>' | '<=' | '>=' ) additiveExpr )* ;
    public final XPathEnhancerParser.relationalExpr_return relationalExpr() throws RecognitionException {
        XPathEnhancerParser.relationalExpr_return retval = new XPathEnhancerParser.relationalExpr_return();
        retval.start = input.LT(1);

        try {
            // src\\main\\antlr\\XPathEnhancer.g:218:3: ( additiveExpr ( ( '<' | '>' | '<=' | '>=' ) additiveExpr )* )
            // src\\main\\antlr\\XPathEnhancer.g:219:3: additiveExpr ( ( '<' | '>' | '<=' | '>=' ) additiveExpr )*
            {
            pushFollow(FOLLOW_additiveExpr_in_relationalExpr1012);
            additiveExpr();

            state._fsp--;

            // src\\main\\antlr\\XPathEnhancer.g:220:3: ( ( '<' | '>' | '<=' | '>=' ) additiveExpr )*
            loop20:
            do {
                int alt20=2;
                int LA20_0 = input.LA(1);

                if ( ((LA20_0>=LESS && LA20_0<=GE)) ) {
                    alt20=1;
                }


                switch (alt20) {
            	case 1 :
            	    // src\\main\\antlr\\XPathEnhancer.g:221:5: ( '<' | '>' | '<=' | '>=' ) additiveExpr
            	    {
            	    if ( (input.LA(1)>=LESS && input.LA(1)<=GE) ) {
            	        input.consume();
            	        state.errorRecovery=false;
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        throw mse;
            	    }

            	    pushFollow(FOLLOW_additiveExpr_in_relationalExpr1072);
            	    additiveExpr();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop20;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "relationalExpr"

    public static class additiveExpr_return extends ParserRuleReturnScope {
        public StringTemplate st;
        public Object getTemplate() { return st; }
        public String toString() { return st==null?null:st.toString(); }
    };

    // $ANTLR start "additiveExpr"
    // src\\main\\antlr\\XPathEnhancer.g:231:1: additiveExpr : multiplicativeExpr ( ( '+' | '-' ) multiplicativeExpr )* ;
    public final XPathEnhancerParser.additiveExpr_return additiveExpr() throws RecognitionException {
        XPathEnhancerParser.additiveExpr_return retval = new XPathEnhancerParser.additiveExpr_return();
        retval.start = input.LT(1);

        try {
            // src\\main\\antlr\\XPathEnhancer.g:232:3: ( multiplicativeExpr ( ( '+' | '-' ) multiplicativeExpr )* )
            // src\\main\\antlr\\XPathEnhancer.g:233:3: multiplicativeExpr ( ( '+' | '-' ) multiplicativeExpr )*
            {
            pushFollow(FOLLOW_multiplicativeExpr_in_additiveExpr1092);
            multiplicativeExpr();

            state._fsp--;

            // src\\main\\antlr\\XPathEnhancer.g:234:3: ( ( '+' | '-' ) multiplicativeExpr )*
            loop21:
            do {
                int alt21=2;
                int LA21_0 = input.LA(1);

                if ( ((LA21_0>=MINUS && LA21_0<=PLUS)) ) {
                    alt21=1;
                }


                switch (alt21) {
            	case 1 :
            	    // src\\main\\antlr\\XPathEnhancer.g:235:5: ( '+' | '-' ) multiplicativeExpr
            	    {
            	    if ( (input.LA(1)>=MINUS && input.LA(1)<=PLUS) ) {
            	        input.consume();
            	        state.errorRecovery=false;
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        throw mse;
            	    }

            	    pushFollow(FOLLOW_multiplicativeExpr_in_additiveExpr1132);
            	    multiplicativeExpr();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop21;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "additiveExpr"

    public static class multiplicativeExpr_return extends ParserRuleReturnScope {
        public StringTemplate st;
        public Object getTemplate() { return st; }
        public String toString() { return st==null?null:st.toString(); }
    };

    // $ANTLR start "multiplicativeExpr"
    // src\\main\\antlr\\XPathEnhancer.g:243:1: multiplicativeExpr : ( unaryExprNoRoot ( ( '*' | 'div' | 'mod' ) multiplicativeExpr )? | '/' ( ( 'div' | 'mod' ) multiplicativeExpr )? );
    public final XPathEnhancerParser.multiplicativeExpr_return multiplicativeExpr() throws RecognitionException {
        XPathEnhancerParser.multiplicativeExpr_return retval = new XPathEnhancerParser.multiplicativeExpr_return();
        retval.start = input.LT(1);

        try {
            // src\\main\\antlr\\XPathEnhancer.g:244:3: ( unaryExprNoRoot ( ( '*' | 'div' | 'mod' ) multiplicativeExpr )? | '/' ( ( 'div' | 'mod' ) multiplicativeExpr )? )
            int alt24=2;
            int LA24_0 = input.LA(1);

            if ( ((LA24_0>=ABRPATH && LA24_0<=LPAR)||LA24_0==MINUS||(LA24_0>=DOT && LA24_0<=AT)||(LA24_0>=AxisName && LA24_0<=NCName)||LA24_0==37||LA24_0==44) ) {
                alt24=1;
            }
            else if ( (LA24_0==PATHSEP) ) {
                int LA24_2 = input.LA(2);

                if ( ((LA24_2>=DOT && LA24_2<=AT)||LA24_2==PIPE||(LA24_2>=AxisName && LA24_2<=NodeType)||LA24_2==NCName||LA24_2==37) ) {
                    alt24=1;
                }
                else if ( (LA24_2==EOF||LA24_2==RPAR||(LA24_2>=RBRAC && LA24_2<=PLUS)||LA24_2==COMMA||(LA24_2>=LESS && LA24_2<=GE)||(LA24_2>=38 && LA24_2<=43)) ) {
                    alt24=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 24, 2, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 24, 0, input);

                throw nvae;
            }
            switch (alt24) {
                case 1 :
                    // src\\main\\antlr\\XPathEnhancer.g:245:3: unaryExprNoRoot ( ( '*' | 'div' | 'mod' ) multiplicativeExpr )?
                    {
                    pushFollow(FOLLOW_unaryExprNoRoot_in_multiplicativeExpr1152);
                    unaryExprNoRoot();

                    state._fsp--;

                    // src\\main\\antlr\\XPathEnhancer.g:246:3: ( ( '*' | 'div' | 'mod' ) multiplicativeExpr )?
                    int alt22=2;
                    int LA22_0 = input.LA(1);

                    if ( (LA22_0==MUL||(LA22_0>=42 && LA22_0<=43)) ) {
                        alt22=1;
                    }
                    switch (alt22) {
                        case 1 :
                            // src\\main\\antlr\\XPathEnhancer.g:247:5: ( '*' | 'div' | 'mod' ) multiplicativeExpr
                            {
                            if ( input.LA(1)==MUL||(input.LA(1)>=42 && input.LA(1)<=43) ) {
                                input.consume();
                                state.errorRecovery=false;
                            }
                            else {
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                throw mse;
                            }

                            pushFollow(FOLLOW_multiplicativeExpr_in_multiplicativeExpr1202);
                            multiplicativeExpr();

                            state._fsp--;


                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // src\\main\\antlr\\XPathEnhancer.g:254:5: '/' ( ( 'div' | 'mod' ) multiplicativeExpr )?
                    {
                    match(input,PATHSEP,FOLLOW_PATHSEP_in_multiplicativeExpr1213); 
                    // src\\main\\antlr\\XPathEnhancer.g:255:3: ( ( 'div' | 'mod' ) multiplicativeExpr )?
                    int alt23=2;
                    int LA23_0 = input.LA(1);

                    if ( ((LA23_0>=42 && LA23_0<=43)) ) {
                        alt23=1;
                    }
                    switch (alt23) {
                        case 1 :
                            // src\\main\\antlr\\XPathEnhancer.g:256:5: ( 'div' | 'mod' ) multiplicativeExpr
                            {
                            if ( (input.LA(1)>=42 && input.LA(1)<=43) ) {
                                input.consume();
                                state.errorRecovery=false;
                            }
                            else {
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                throw mse;
                            }

                            pushFollow(FOLLOW_multiplicativeExpr_in_multiplicativeExpr1253);
                            multiplicativeExpr();

                            state._fsp--;


                            }
                            break;

                    }


                    }
                    break;

            }
            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "multiplicativeExpr"

    public static class unaryExprNoRoot_return extends ParserRuleReturnScope {
        public StringTemplate st;
        public Object getTemplate() { return st; }
        public String toString() { return st==null?null:st.toString(); }
    };

    // $ANTLR start "unaryExprNoRoot"
    // src\\main\\antlr\\XPathEnhancer.g:264:1: unaryExprNoRoot : ( '-' )* unionExprNoRoot ;
    public final XPathEnhancerParser.unaryExprNoRoot_return unaryExprNoRoot() throws RecognitionException {
        XPathEnhancerParser.unaryExprNoRoot_return retval = new XPathEnhancerParser.unaryExprNoRoot_return();
        retval.start = input.LT(1);

        try {
            // src\\main\\antlr\\XPathEnhancer.g:265:3: ( ( '-' )* unionExprNoRoot )
            // src\\main\\antlr\\XPathEnhancer.g:266:3: ( '-' )* unionExprNoRoot
            {
            // src\\main\\antlr\\XPathEnhancer.g:266:3: ( '-' )*
            loop25:
            do {
                int alt25=2;
                int LA25_0 = input.LA(1);

                if ( (LA25_0==MINUS) ) {
                    alt25=1;
                }


                switch (alt25) {
            	case 1 :
            	    // src\\main\\antlr\\XPathEnhancer.g:266:3: '-'
            	    {
            	    match(input,MINUS,FOLLOW_MINUS_in_unaryExprNoRoot1273); 

            	    }
            	    break;

            	default :
            	    break loop25;
                }
            } while (true);

            pushFollow(FOLLOW_unionExprNoRoot_in_unaryExprNoRoot1276);
            unionExprNoRoot();

            state._fsp--;


            }

            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "unaryExprNoRoot"

    public static class qName_return extends ParserRuleReturnScope {
        public StringTemplate st;
        public Object getTemplate() { return st; }
        public String toString() { return st==null?null:st.toString(); }
    };

    // $ANTLR start "qName"
    // src\\main\\antlr\\XPathEnhancer.g:269:1: qName : nCName ( ':' nCName )? ;
    public final XPathEnhancerParser.qName_return qName() throws RecognitionException {
        XPathEnhancerParser.qName_return retval = new XPathEnhancerParser.qName_return();
        retval.start = input.LT(1);

        try {
            // src\\main\\antlr\\XPathEnhancer.g:270:3: ( nCName ( ':' nCName )? )
            // src\\main\\antlr\\XPathEnhancer.g:271:3: nCName ( ':' nCName )?
            {
            pushFollow(FOLLOW_nCName_in_qName1291);
            nCName();

            state._fsp--;

            // src\\main\\antlr\\XPathEnhancer.g:271:10: ( ':' nCName )?
            int alt26=2;
            int LA26_0 = input.LA(1);

            if ( (LA26_0==COLON) ) {
                alt26=1;
            }
            switch (alt26) {
                case 1 :
                    // src\\main\\antlr\\XPathEnhancer.g:271:11: ':' nCName
                    {
                    match(input,COLON,FOLLOW_COLON_in_qName1294); 
                    pushFollow(FOLLOW_nCName_in_qName1296);
                    nCName();

                    state._fsp--;


                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "qName"

    public static class functionName_return extends ParserRuleReturnScope {
        public StringTemplate st;
        public Object getTemplate() { return st; }
        public String toString() { return st==null?null:st.toString(); }
    };

    // $ANTLR start "functionName"
    // src\\main\\antlr\\XPathEnhancer.g:274:1: functionName : qName ;
    public final XPathEnhancerParser.functionName_return functionName() throws RecognitionException {
        XPathEnhancerParser.functionName_return retval = new XPathEnhancerParser.functionName_return();
        retval.start = input.LT(1);

        try {
            // src\\main\\antlr\\XPathEnhancer.g:275:3: ( qName )
            // src\\main\\antlr\\XPathEnhancer.g:276:3: qName
            {
            pushFollow(FOLLOW_qName_in_functionName1313);
            qName();

            state._fsp--;


            }

            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "functionName"

    public static class variableReference_return extends ParserRuleReturnScope {
        public StringTemplate st;
        public Object getTemplate() { return st; }
        public String toString() { return st==null?null:st.toString(); }
    };

    // $ANTLR start "variableReference"
    // src\\main\\antlr\\XPathEnhancer.g:279:1: variableReference : '$' qName ;
    public final XPathEnhancerParser.variableReference_return variableReference() throws RecognitionException {
        XPathEnhancerParser.variableReference_return retval = new XPathEnhancerParser.variableReference_return();
        retval.start = input.LT(1);

        try {
            // src\\main\\antlr\\XPathEnhancer.g:280:3: ( '$' qName )
            // src\\main\\antlr\\XPathEnhancer.g:281:3: '$' qName
            {
            match(input,44,FOLLOW_44_in_variableReference1329); 
            pushFollow(FOLLOW_qName_in_variableReference1331);
            qName();

            state._fsp--;


            }

            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "variableReference"

    public static class nameTest_return extends ParserRuleReturnScope {
        public StringTemplate st;
        public Object getTemplate() { return st; }
        public String toString() { return st==null?null:st.toString(); }
    };

    // $ANTLR start "nameTest"
    // src\\main\\antlr\\XPathEnhancer.g:284:1: nameTest : ( '*' | nCName ':' '*' | qName );
    public final XPathEnhancerParser.nameTest_return nameTest() throws RecognitionException {
        XPathEnhancerParser.nameTest_return retval = new XPathEnhancerParser.nameTest_return();
        retval.start = input.LT(1);

        try {
            // src\\main\\antlr\\XPathEnhancer.g:285:3: ( '*' | nCName ':' '*' | qName )
            int alt27=3;
            int LA27_0 = input.LA(1);

            if ( (LA27_0==MUL) ) {
                alt27=1;
            }
            else if ( (LA27_0==AxisName||LA27_0==NCName) ) {
                int LA27_2 = input.LA(2);

                if ( (LA27_2==COLON) ) {
                    int LA27_3 = input.LA(3);

                    if ( (LA27_3==MUL) ) {
                        alt27=2;
                    }
                    else if ( (LA27_3==AxisName||LA27_3==NCName) ) {
                        alt27=3;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 27, 3, input);

                        throw nvae;
                    }
                }
                else if ( (LA27_2==EOF||(LA27_2>=PATHSEP && LA27_2<=ABRPATH)||(LA27_2>=RPAR && LA27_2<=PLUS)||LA27_2==MUL||(LA27_2>=COMMA && LA27_2<=GE)||(LA27_2>=38 && LA27_2<=43)) ) {
                    alt27=3;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 27, 2, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 27, 0, input);

                throw nvae;
            }
            switch (alt27) {
                case 1 :
                    // src\\main\\antlr\\XPathEnhancer.g:286:3: '*'
                    {
                    match(input,MUL,FOLLOW_MUL_in_nameTest1346); 

                    }
                    break;
                case 2 :
                    // src\\main\\antlr\\XPathEnhancer.g:287:5: nCName ':' '*'
                    {
                    pushFollow(FOLLOW_nCName_in_nameTest1352);
                    nCName();

                    state._fsp--;

                    match(input,COLON,FOLLOW_COLON_in_nameTest1354); 
                    match(input,MUL,FOLLOW_MUL_in_nameTest1356); 

                    }
                    break;
                case 3 :
                    // src\\main\\antlr\\XPathEnhancer.g:288:5: qName
                    {
                    pushFollow(FOLLOW_qName_in_nameTest1362);
                    qName();

                    state._fsp--;


                    }
                    break;

            }
            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "nameTest"

    public static class nCName_return extends ParserRuleReturnScope {
        public StringTemplate st;
        public Object getTemplate() { return st; }
        public String toString() { return st==null?null:st.toString(); }
    };

    // $ANTLR start "nCName"
    // src\\main\\antlr\\XPathEnhancer.g:291:1: nCName : ( NCName | AxisName );
    public final XPathEnhancerParser.nCName_return nCName() throws RecognitionException {
        XPathEnhancerParser.nCName_return retval = new XPathEnhancerParser.nCName_return();
        retval.start = input.LT(1);

        try {
            // src\\main\\antlr\\XPathEnhancer.g:292:3: ( NCName | AxisName )
            // src\\main\\antlr\\XPathEnhancer.g:
            {
            if ( input.LA(1)==AxisName||input.LA(1)==NCName ) {
                input.consume();
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "nCName"

    // Delegated rules


 

    public static final BitSet FOLLOW_expr_in_main331 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_relativeLocationPath_in_locationPath346 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_absoluteLocationPathNoroot_in_locationPath353 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_originalAbsoluteLocationPathNoroot_in_absoluteLocationPathNoroot368 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PATHSEP_in_originalAbsoluteLocationPathNoroot532 = new BitSet(new long[]{0x000000204C00F000L});
    public static final BitSet FOLLOW_relativeLocationPath_in_originalAbsoluteLocationPathNoroot534 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ABRPATH_in_originalAbsoluteLocationPathNoroot540 = new BitSet(new long[]{0x000000204C00F000L});
    public static final BitSet FOLLOW_relativeLocationPath_in_originalAbsoluteLocationPathNoroot542 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_step_in_relativeLocationPath557 = new BitSet(new long[]{0x0000000000000032L});
    public static final BitSet FOLLOW_set_in_relativeLocationPath567 = new BitSet(new long[]{0x000000204C00F000L});
    public static final BitSet FOLLOW_step_in_relativeLocationPath597 = new BitSet(new long[]{0x0000000000000032L});
    public static final BitSet FOLLOW_axisSpecifier_in_step617 = new BitSet(new long[]{0x000000204C00A000L});
    public static final BitSet FOLLOW_nodeTest_in_step619 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_predicate_in_step621 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_abbreviatedStep_in_step628 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AxisName_in_axisSpecifier643 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_CC_in_axisSpecifier645 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AT_in_axisSpecifier651 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nameTest_in_nodeTest667 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NodeType_in_nodeTest673 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAR_in_nodeTest675 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAR_in_nodeTest677 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_37_in_nodeTest683 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAR_in_nodeTest685 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_Literal_in_nodeTest687 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAR_in_nodeTest689 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRAC_in_predicate704 = new BitSet(new long[]{0x000010207C00F470L});
    public static final BitSet FOLLOW_expr_in_predicate706 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_RBRAC_in_predicate708 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_abbreviatedStep0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_orExpr_in_expr744 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variableReference_in_primaryExpr759 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAR_in_primaryExpr765 = new BitSet(new long[]{0x000010207C00F470L});
    public static final BitSet FOLLOW_expr_in_primaryExpr767 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAR_in_primaryExpr769 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Literal_in_primaryExpr775 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Number_in_primaryExpr781 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_functionCall_in_primaryExpr787 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_functionName_in_functionCall802 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAR_in_functionCall804 = new BitSet(new long[]{0x000010207C00F4F0L});
    public static final BitSet FOLLOW_expr_in_functionCall807 = new BitSet(new long[]{0x0000000000010080L});
    public static final BitSet FOLLOW_COMMA_in_functionCall810 = new BitSet(new long[]{0x000010207C00F470L});
    public static final BitSet FOLLOW_expr_in_functionCall812 = new BitSet(new long[]{0x0000000000010080L});
    public static final BitSet FOLLOW_RPAR_in_functionCall818 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_pathExprNoRoot_in_unionExprNoRoot833 = new BitSet(new long[]{0x0000000000020002L});
    public static final BitSet FOLLOW_PIPE_in_unionExprNoRoot836 = new BitSet(new long[]{0x000010207C00F470L});
    public static final BitSet FOLLOW_unionExprNoRoot_in_unionExprNoRoot838 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PATHSEP_in_unionExprNoRoot846 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_PIPE_in_unionExprNoRoot848 = new BitSet(new long[]{0x000010207C00F470L});
    public static final BitSet FOLLOW_unionExprNoRoot_in_unionExprNoRoot850 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_locationPath_in_pathExprNoRoot865 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_filterExpr_in_pathExprNoRoot871 = new BitSet(new long[]{0x000000204C00F032L});
    public static final BitSet FOLLOW_absoluteLocationPathNoroot_in_pathExprNoRoot873 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_primaryExpr_in_filterExpr890 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_predicate_in_filterExpr892 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_andExpr_in_orExpr908 = new BitSet(new long[]{0x0000004000000002L});
    public static final BitSet FOLLOW_38_in_orExpr911 = new BitSet(new long[]{0x000010207C00F470L});
    public static final BitSet FOLLOW_andExpr_in_orExpr913 = new BitSet(new long[]{0x0000004000000002L});
    public static final BitSet FOLLOW_equalityExpr_in_andExpr930 = new BitSet(new long[]{0x0000008000000002L});
    public static final BitSet FOLLOW_39_in_andExpr933 = new BitSet(new long[]{0x000010207C00F470L});
    public static final BitSet FOLLOW_equalityExpr_in_andExpr935 = new BitSet(new long[]{0x0000008000000002L});
    public static final BitSet FOLLOW_relationalExpr_in_equalityExpr952 = new BitSet(new long[]{0x0000030000000002L});
    public static final BitSet FOLLOW_set_in_equalityExpr962 = new BitSet(new long[]{0x000010207C00F470L});
    public static final BitSet FOLLOW_relationalExpr_in_equalityExpr992 = new BitSet(new long[]{0x0000030000000002L});
    public static final BitSet FOLLOW_additiveExpr_in_relationalExpr1012 = new BitSet(new long[]{0x00000000003C0002L});
    public static final BitSet FOLLOW_set_in_relationalExpr1022 = new BitSet(new long[]{0x000010207C00F470L});
    public static final BitSet FOLLOW_additiveExpr_in_relationalExpr1072 = new BitSet(new long[]{0x00000000003C0002L});
    public static final BitSet FOLLOW_multiplicativeExpr_in_additiveExpr1092 = new BitSet(new long[]{0x0000000000000C02L});
    public static final BitSet FOLLOW_set_in_additiveExpr1102 = new BitSet(new long[]{0x000010207C00F470L});
    public static final BitSet FOLLOW_multiplicativeExpr_in_additiveExpr1132 = new BitSet(new long[]{0x0000000000000C02L});
    public static final BitSet FOLLOW_unaryExprNoRoot_in_multiplicativeExpr1152 = new BitSet(new long[]{0x00000C0000002002L});
    public static final BitSet FOLLOW_set_in_multiplicativeExpr1162 = new BitSet(new long[]{0x000010207C00F470L});
    public static final BitSet FOLLOW_multiplicativeExpr_in_multiplicativeExpr1202 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PATHSEP_in_multiplicativeExpr1213 = new BitSet(new long[]{0x00000C0000000002L});
    public static final BitSet FOLLOW_set_in_multiplicativeExpr1223 = new BitSet(new long[]{0x000010207C00F470L});
    public static final BitSet FOLLOW_multiplicativeExpr_in_multiplicativeExpr1253 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINUS_in_unaryExprNoRoot1273 = new BitSet(new long[]{0x000010207C00F470L});
    public static final BitSet FOLLOW_unionExprNoRoot_in_unaryExprNoRoot1276 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nCName_in_qName1291 = new BitSet(new long[]{0x0000000000400002L});
    public static final BitSet FOLLOW_COLON_in_qName1294 = new BitSet(new long[]{0x0000000044000000L});
    public static final BitSet FOLLOW_nCName_in_qName1296 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_qName_in_functionName1313 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_44_in_variableReference1329 = new BitSet(new long[]{0x0000000044002000L});
    public static final BitSet FOLLOW_qName_in_variableReference1331 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MUL_in_nameTest1346 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nCName_in_nameTest1352 = new BitSet(new long[]{0x0000000000400000L});
    public static final BitSet FOLLOW_COLON_in_nameTest1354 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_MUL_in_nameTest1356 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_qName_in_nameTest1362 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_nCName0 = new BitSet(new long[]{0x0000000000000002L});

}