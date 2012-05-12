// $ANTLR 3.3 Nov 30, 2010 12:45:30 src\\main\\antlr\\XPathEnhancer.g 2011-06-19 16:21:01

package org.docx4j.model.datastorage;


import org.antlr.runtime.BaseRecognizer;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.DFA;
import org.antlr.runtime.EarlyExitException;
import org.antlr.runtime.IntStream;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;

public class XPathEnhancerLexer extends Lexer {
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

    public XPathEnhancerLexer() {;} 
    public XPathEnhancerLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public XPathEnhancerLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "src\\main\\antlr\\XPathEnhancer.g"; }

    // $ANTLR start "PATHSEP"
    public final void mPATHSEP() throws RecognitionException {
        try {
            int _type = PATHSEP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src\\main\\antlr\\XPathEnhancer.g:7:9: ( '/' )
            // src\\main\\antlr\\XPathEnhancer.g:7:11: '/'
            {
            match('/'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "PATHSEP"

    // $ANTLR start "ABRPATH"
    public final void mABRPATH() throws RecognitionException {
        try {
            int _type = ABRPATH;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src\\main\\antlr\\XPathEnhancer.g:8:9: ( '//' )
            // src\\main\\antlr\\XPathEnhancer.g:8:11: '//'
            {
            match("//"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ABRPATH"

    // $ANTLR start "LPAR"
    public final void mLPAR() throws RecognitionException {
        try {
            int _type = LPAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src\\main\\antlr\\XPathEnhancer.g:9:6: ( '(' )
            // src\\main\\antlr\\XPathEnhancer.g:9:8: '('
            {
            match('('); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LPAR"

    // $ANTLR start "RPAR"
    public final void mRPAR() throws RecognitionException {
        try {
            int _type = RPAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src\\main\\antlr\\XPathEnhancer.g:10:6: ( ')' )
            // src\\main\\antlr\\XPathEnhancer.g:10:8: ')'
            {
            match(')'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RPAR"

    // $ANTLR start "LBRAC"
    public final void mLBRAC() throws RecognitionException {
        try {
            int _type = LBRAC;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src\\main\\antlr\\XPathEnhancer.g:11:7: ( '[' )
            // src\\main\\antlr\\XPathEnhancer.g:11:9: '['
            {
            match('['); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LBRAC"

    // $ANTLR start "RBRAC"
    public final void mRBRAC() throws RecognitionException {
        try {
            int _type = RBRAC;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src\\main\\antlr\\XPathEnhancer.g:12:7: ( ']' )
            // src\\main\\antlr\\XPathEnhancer.g:12:9: ']'
            {
            match(']'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RBRAC"

    // $ANTLR start "MINUS"
    public final void mMINUS() throws RecognitionException {
        try {
            int _type = MINUS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src\\main\\antlr\\XPathEnhancer.g:13:7: ( '-' )
            // src\\main\\antlr\\XPathEnhancer.g:13:9: '-'
            {
            match('-'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "MINUS"

    // $ANTLR start "PLUS"
    public final void mPLUS() throws RecognitionException {
        try {
            int _type = PLUS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src\\main\\antlr\\XPathEnhancer.g:14:6: ( '+' )
            // src\\main\\antlr\\XPathEnhancer.g:14:8: '+'
            {
            match('+'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "PLUS"

    // $ANTLR start "DOT"
    public final void mDOT() throws RecognitionException {
        try {
            int _type = DOT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src\\main\\antlr\\XPathEnhancer.g:15:5: ( '.' )
            // src\\main\\antlr\\XPathEnhancer.g:15:7: '.'
            {
            match('.'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DOT"

    // $ANTLR start "MUL"
    public final void mMUL() throws RecognitionException {
        try {
            int _type = MUL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src\\main\\antlr\\XPathEnhancer.g:16:5: ( '*' )
            // src\\main\\antlr\\XPathEnhancer.g:16:7: '*'
            {
            match('*'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "MUL"

    // $ANTLR start "DOTDOT"
    public final void mDOTDOT() throws RecognitionException {
        try {
            int _type = DOTDOT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src\\main\\antlr\\XPathEnhancer.g:17:8: ( '..' )
            // src\\main\\antlr\\XPathEnhancer.g:17:10: '..'
            {
            match(".."); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DOTDOT"

    // $ANTLR start "AT"
    public final void mAT() throws RecognitionException {
        try {
            int _type = AT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src\\main\\antlr\\XPathEnhancer.g:18:4: ( '@' )
            // src\\main\\antlr\\XPathEnhancer.g:18:6: '@'
            {
            match('@'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "AT"

    // $ANTLR start "COMMA"
    public final void mCOMMA() throws RecognitionException {
        try {
            int _type = COMMA;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src\\main\\antlr\\XPathEnhancer.g:19:7: ( ',' )
            // src\\main\\antlr\\XPathEnhancer.g:19:9: ','
            {
            match(','); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COMMA"

    // $ANTLR start "PIPE"
    public final void mPIPE() throws RecognitionException {
        try {
            int _type = PIPE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src\\main\\antlr\\XPathEnhancer.g:20:6: ( '|' )
            // src\\main\\antlr\\XPathEnhancer.g:20:8: '|'
            {
            match('|'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "PIPE"

    // $ANTLR start "LESS"
    public final void mLESS() throws RecognitionException {
        try {
            int _type = LESS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src\\main\\antlr\\XPathEnhancer.g:21:6: ( '<' )
            // src\\main\\antlr\\XPathEnhancer.g:21:8: '<'
            {
            match('<'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LESS"

    // $ANTLR start "MORE"
    public final void mMORE() throws RecognitionException {
        try {
            int _type = MORE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src\\main\\antlr\\XPathEnhancer.g:22:6: ( '>' )
            // src\\main\\antlr\\XPathEnhancer.g:22:8: '>'
            {
            match('>'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "MORE"

    // $ANTLR start "LE"
    public final void mLE() throws RecognitionException {
        try {
            int _type = LE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src\\main\\antlr\\XPathEnhancer.g:23:4: ( '<=' )
            // src\\main\\antlr\\XPathEnhancer.g:23:6: '<='
            {
            match("<="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LE"

    // $ANTLR start "GE"
    public final void mGE() throws RecognitionException {
        try {
            int _type = GE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src\\main\\antlr\\XPathEnhancer.g:24:4: ( '>=' )
            // src\\main\\antlr\\XPathEnhancer.g:24:6: '>='
            {
            match(">="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "GE"

    // $ANTLR start "COLON"
    public final void mCOLON() throws RecognitionException {
        try {
            int _type = COLON;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src\\main\\antlr\\XPathEnhancer.g:25:7: ( ':' )
            // src\\main\\antlr\\XPathEnhancer.g:25:9: ':'
            {
            match(':'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COLON"

    // $ANTLR start "CC"
    public final void mCC() throws RecognitionException {
        try {
            int _type = CC;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src\\main\\antlr\\XPathEnhancer.g:26:4: ( '::' )
            // src\\main\\antlr\\XPathEnhancer.g:26:6: '::'
            {
            match("::"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "CC"

    // $ANTLR start "APOS"
    public final void mAPOS() throws RecognitionException {
        try {
            int _type = APOS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src\\main\\antlr\\XPathEnhancer.g:27:6: ( '\\'' )
            // src\\main\\antlr\\XPathEnhancer.g:27:8: '\\''
            {
            match('\''); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "APOS"

    // $ANTLR start "QUOT"
    public final void mQUOT() throws RecognitionException {
        try {
            int _type = QUOT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src\\main\\antlr\\XPathEnhancer.g:28:6: ( '\\\"' )
            // src\\main\\antlr\\XPathEnhancer.g:28:8: '\\\"'
            {
            match('\"'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "QUOT"

    // $ANTLR start "T__37"
    public final void mT__37() throws RecognitionException {
        try {
            int _type = T__37;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src\\main\\antlr\\XPathEnhancer.g:29:7: ( 'processing-instruction' )
            // src\\main\\antlr\\XPathEnhancer.g:29:9: 'processing-instruction'
            {
            match("processing-instruction"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__37"

    // $ANTLR start "T__38"
    public final void mT__38() throws RecognitionException {
        try {
            int _type = T__38;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src\\main\\antlr\\XPathEnhancer.g:30:7: ( 'or' )
            // src\\main\\antlr\\XPathEnhancer.g:30:9: 'or'
            {
            match("or"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__38"

    // $ANTLR start "T__39"
    public final void mT__39() throws RecognitionException {
        try {
            int _type = T__39;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src\\main\\antlr\\XPathEnhancer.g:31:7: ( 'and' )
            // src\\main\\antlr\\XPathEnhancer.g:31:9: 'and'
            {
            match("and"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__39"

    // $ANTLR start "T__40"
    public final void mT__40() throws RecognitionException {
        try {
            int _type = T__40;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src\\main\\antlr\\XPathEnhancer.g:32:7: ( '=' )
            // src\\main\\antlr\\XPathEnhancer.g:32:9: '='
            {
            match('='); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__40"

    // $ANTLR start "T__41"
    public final void mT__41() throws RecognitionException {
        try {
            int _type = T__41;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src\\main\\antlr\\XPathEnhancer.g:33:7: ( '!=' )
            // src\\main\\antlr\\XPathEnhancer.g:33:9: '!='
            {
            match("!="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__41"

    // $ANTLR start "T__42"
    public final void mT__42() throws RecognitionException {
        try {
            int _type = T__42;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src\\main\\antlr\\XPathEnhancer.g:34:7: ( 'div' )
            // src\\main\\antlr\\XPathEnhancer.g:34:9: 'div'
            {
            match("div"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__42"

    // $ANTLR start "T__43"
    public final void mT__43() throws RecognitionException {
        try {
            int _type = T__43;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src\\main\\antlr\\XPathEnhancer.g:35:7: ( 'mod' )
            // src\\main\\antlr\\XPathEnhancer.g:35:9: 'mod'
            {
            match("mod"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__43"

    // $ANTLR start "T__44"
    public final void mT__44() throws RecognitionException {
        try {
            int _type = T__44;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src\\main\\antlr\\XPathEnhancer.g:36:7: ( '$' )
            // src\\main\\antlr\\XPathEnhancer.g:36:9: '$'
            {
            match('$'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__44"

    // $ANTLR start "NodeType"
    public final void mNodeType() throws RecognitionException {
        try {
            int _type = NodeType;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src\\main\\antlr\\XPathEnhancer.g:298:3: ( 'comment' | 'text' | 'processing-instruction' | 'node' )
            int alt1=4;
            switch ( input.LA(1) ) {
            case 'c':
                {
                alt1=1;
                }
                break;
            case 't':
                {
                alt1=2;
                }
                break;
            case 'p':
                {
                alt1=3;
                }
                break;
            case 'n':
                {
                alt1=4;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 1, 0, input);

                throw nvae;
            }

            switch (alt1) {
                case 1 :
                    // src\\main\\antlr\\XPathEnhancer.g:299:3: 'comment'
                    {
                    match("comment"); 


                    }
                    break;
                case 2 :
                    // src\\main\\antlr\\XPathEnhancer.g:300:5: 'text'
                    {
                    match("text"); 


                    }
                    break;
                case 3 :
                    // src\\main\\antlr\\XPathEnhancer.g:301:5: 'processing-instruction'
                    {
                    match("processing-instruction"); 


                    }
                    break;
                case 4 :
                    // src\\main\\antlr\\XPathEnhancer.g:302:5: 'node'
                    {
                    match("node"); 


                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NodeType"

    // $ANTLR start "Number"
    public final void mNumber() throws RecognitionException {
        try {
            int _type = Number;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src\\main\\antlr\\XPathEnhancer.g:306:3: ( Digits ( '.' ( Digits )? )? | '.' Digits )
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( ((LA4_0>='0' && LA4_0<='9')) ) {
                alt4=1;
            }
            else if ( (LA4_0=='.') ) {
                alt4=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 4, 0, input);

                throw nvae;
            }
            switch (alt4) {
                case 1 :
                    // src\\main\\antlr\\XPathEnhancer.g:307:3: Digits ( '.' ( Digits )? )?
                    {
                    mDigits(); 
                    // src\\main\\antlr\\XPathEnhancer.g:307:10: ( '.' ( Digits )? )?
                    int alt3=2;
                    int LA3_0 = input.LA(1);

                    if ( (LA3_0=='.') ) {
                        alt3=1;
                    }
                    switch (alt3) {
                        case 1 :
                            // src\\main\\antlr\\XPathEnhancer.g:307:11: '.' ( Digits )?
                            {
                            match('.'); 
                            // src\\main\\antlr\\XPathEnhancer.g:307:15: ( Digits )?
                            int alt2=2;
                            int LA2_0 = input.LA(1);

                            if ( ((LA2_0>='0' && LA2_0<='9')) ) {
                                alt2=1;
                            }
                            switch (alt2) {
                                case 1 :
                                    // src\\main\\antlr\\XPathEnhancer.g:307:15: Digits
                                    {
                                    mDigits(); 

                                    }
                                    break;

                            }


                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // src\\main\\antlr\\XPathEnhancer.g:308:5: '.' Digits
                    {
                    match('.'); 
                    mDigits(); 

                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "Number"

    // $ANTLR start "Digits"
    public final void mDigits() throws RecognitionException {
        try {
            // src\\main\\antlr\\XPathEnhancer.g:313:3: ( ( '0' .. '9' )+ )
            // src\\main\\antlr\\XPathEnhancer.g:314:3: ( '0' .. '9' )+
            {
            // src\\main\\antlr\\XPathEnhancer.g:314:3: ( '0' .. '9' )+
            int cnt5=0;
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( ((LA5_0>='0' && LA5_0<='9')) ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // src\\main\\antlr\\XPathEnhancer.g:314:4: '0' .. '9'
            	    {
            	    matchRange('0','9'); 

            	    }
            	    break;

            	default :
            	    if ( cnt5 >= 1 ) break loop5;
                        EarlyExitException eee =
                            new EarlyExitException(5, input);
                        throw eee;
                }
                cnt5++;
            } while (true);


            }

        }
        finally {
        }
    }
    // $ANTLR end "Digits"

    // $ANTLR start "AxisName"
    public final void mAxisName() throws RecognitionException {
        try {
            int _type = AxisName;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src\\main\\antlr\\XPathEnhancer.g:318:3: ( 'ancestor' | 'ancestor-or-self' | 'attribute' | 'child' | 'descendant' | 'descendant-or-self' | 'following' | 'following-sibling' | 'namespace' | 'parent' | 'preceding' | 'preceding-sibling' | 'self' )
            int alt6=13;
            alt6 = dfa6.predict(input);
            switch (alt6) {
                case 1 :
                    // src\\main\\antlr\\XPathEnhancer.g:319:3: 'ancestor'
                    {
                    match("ancestor"); 


                    }
                    break;
                case 2 :
                    // src\\main\\antlr\\XPathEnhancer.g:320:5: 'ancestor-or-self'
                    {
                    match("ancestor-or-self"); 


                    }
                    break;
                case 3 :
                    // src\\main\\antlr\\XPathEnhancer.g:321:5: 'attribute'
                    {
                    match("attribute"); 


                    }
                    break;
                case 4 :
                    // src\\main\\antlr\\XPathEnhancer.g:322:5: 'child'
                    {
                    match("child"); 


                    }
                    break;
                case 5 :
                    // src\\main\\antlr\\XPathEnhancer.g:323:5: 'descendant'
                    {
                    match("descendant"); 


                    }
                    break;
                case 6 :
                    // src\\main\\antlr\\XPathEnhancer.g:324:5: 'descendant-or-self'
                    {
                    match("descendant-or-self"); 


                    }
                    break;
                case 7 :
                    // src\\main\\antlr\\XPathEnhancer.g:325:5: 'following'
                    {
                    match("following"); 


                    }
                    break;
                case 8 :
                    // src\\main\\antlr\\XPathEnhancer.g:326:5: 'following-sibling'
                    {
                    match("following-sibling"); 


                    }
                    break;
                case 9 :
                    // src\\main\\antlr\\XPathEnhancer.g:327:5: 'namespace'
                    {
                    match("namespace"); 


                    }
                    break;
                case 10 :
                    // src\\main\\antlr\\XPathEnhancer.g:328:5: 'parent'
                    {
                    match("parent"); 


                    }
                    break;
                case 11 :
                    // src\\main\\antlr\\XPathEnhancer.g:329:5: 'preceding'
                    {
                    match("preceding"); 


                    }
                    break;
                case 12 :
                    // src\\main\\antlr\\XPathEnhancer.g:330:5: 'preceding-sibling'
                    {
                    match("preceding-sibling"); 


                    }
                    break;
                case 13 :
                    // src\\main\\antlr\\XPathEnhancer.g:331:5: 'self'
                    {
                    match("self"); 


                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "AxisName"

    // $ANTLR start "Literal"
    public final void mLiteral() throws RecognitionException {
        try {
            int _type = Literal;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src\\main\\antlr\\XPathEnhancer.g:335:3: ( '\"' (~ '\"' )* '\"' | '\\'' (~ '\\'' )* '\\'' )
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( (LA9_0=='\"') ) {
                alt9=1;
            }
            else if ( (LA9_0=='\'') ) {
                alt9=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 9, 0, input);

                throw nvae;
            }
            switch (alt9) {
                case 1 :
                    // src\\main\\antlr\\XPathEnhancer.g:336:3: '\"' (~ '\"' )* '\"'
                    {
                    match('\"'); 
                    // src\\main\\antlr\\XPathEnhancer.g:336:7: (~ '\"' )*
                    loop7:
                    do {
                        int alt7=2;
                        int LA7_0 = input.LA(1);

                        if ( ((LA7_0>='\u0000' && LA7_0<='!')||(LA7_0>='#' && LA7_0<='\uFFFF')) ) {
                            alt7=1;
                        }


                        switch (alt7) {
                    	case 1 :
                    	    // src\\main\\antlr\\XPathEnhancer.g:336:7: ~ '\"'
                    	    {
                    	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='!')||(input.LA(1)>='#' && input.LA(1)<='\uFFFF') ) {
                    	        input.consume();

                    	    }
                    	    else {
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    break loop7;
                        }
                    } while (true);

                    match('\"'); 

                    }
                    break;
                case 2 :
                    // src\\main\\antlr\\XPathEnhancer.g:337:5: '\\'' (~ '\\'' )* '\\''
                    {
                    match('\''); 
                    // src\\main\\antlr\\XPathEnhancer.g:337:10: (~ '\\'' )*
                    loop8:
                    do {
                        int alt8=2;
                        int LA8_0 = input.LA(1);

                        if ( ((LA8_0>='\u0000' && LA8_0<='&')||(LA8_0>='(' && LA8_0<='\uFFFF')) ) {
                            alt8=1;
                        }


                        switch (alt8) {
                    	case 1 :
                    	    // src\\main\\antlr\\XPathEnhancer.g:337:10: ~ '\\''
                    	    {
                    	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='&')||(input.LA(1)>='(' && input.LA(1)<='\uFFFF') ) {
                    	        input.consume();

                    	    }
                    	    else {
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    break loop8;
                        }
                    } while (true);

                    match('\''); 

                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "Literal"

    // $ANTLR start "Whitespace"
    public final void mWhitespace() throws RecognitionException {
        try {
            int _type = Whitespace;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src\\main\\antlr\\XPathEnhancer.g:341:3: ( ( ' ' | '\\t' | '\\n' | '\\r' )+ )
            // src\\main\\antlr\\XPathEnhancer.g:342:3: ( ' ' | '\\t' | '\\n' | '\\r' )+
            {
            // src\\main\\antlr\\XPathEnhancer.g:342:3: ( ' ' | '\\t' | '\\n' | '\\r' )+
            int cnt10=0;
            loop10:
            do {
                int alt10=2;
                int LA10_0 = input.LA(1);

                if ( ((LA10_0>='\t' && LA10_0<='\n')||LA10_0=='\r'||LA10_0==' ') ) {
                    alt10=1;
                }


                switch (alt10) {
            	case 1 :
            	    // src\\main\\antlr\\XPathEnhancer.g:
            	    {
            	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||input.LA(1)=='\r'||input.LA(1)==' ' ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    if ( cnt10 >= 1 ) break loop10;
                        EarlyExitException eee =
                            new EarlyExitException(10, input);
                        throw eee;
                }
                cnt10++;
            } while (true);


                _channel = HIDDEN;
               

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "Whitespace"

    // $ANTLR start "NCName"
    public final void mNCName() throws RecognitionException {
        try {
            int _type = NCName;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src\\main\\antlr\\XPathEnhancer.g:355:3: ( NCNameStartChar ( NCNameChar )* )
            // src\\main\\antlr\\XPathEnhancer.g:356:3: NCNameStartChar ( NCNameChar )*
            {
            mNCNameStartChar(); 
            // src\\main\\antlr\\XPathEnhancer.g:356:19: ( NCNameChar )*
            loop11:
            do {
                int alt11=2;
                int LA11_0 = input.LA(1);

                if ( ((LA11_0>='-' && LA11_0<='.')||(LA11_0>='0' && LA11_0<='9')||(LA11_0>='A' && LA11_0<='Z')||LA11_0=='_'||(LA11_0>='a' && LA11_0<='z')||LA11_0=='\u00B7'||(LA11_0>='\u00C0' && LA11_0<='\u00D6')||(LA11_0>='\u00D8' && LA11_0<='\u00F6')||(LA11_0>='\u00F8' && LA11_0<='\u037D')||(LA11_0>='\u037F' && LA11_0<='\u1FFF')||(LA11_0>='\u200C' && LA11_0<='\u200D')||(LA11_0>='\u203F' && LA11_0<='\u2040')||(LA11_0>='\u2070' && LA11_0<='\u218F')||(LA11_0>='\u2C00' && LA11_0<='\u2FEF')||(LA11_0>='\u3001' && LA11_0<='\uDB7F')||(LA11_0>='\uF900' && LA11_0<='\uFDCF')||(LA11_0>='\uFDF0' && LA11_0<='\uFFFD')) ) {
                    alt11=1;
                }


                switch (alt11) {
            	case 1 :
            	    // src\\main\\antlr\\XPathEnhancer.g:356:19: NCNameChar
            	    {
            	    mNCNameChar(); 

            	    }
            	    break;

            	default :
            	    break loop11;
                }
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NCName"

    // $ANTLR start "NCNameStartChar"
    public final void mNCNameStartChar() throws RecognitionException {
        try {
            // src\\main\\antlr\\XPathEnhancer.g:361:3: ( 'A' .. 'Z' | '_' | 'a' .. 'z' | '\\u00C0' .. '\\u00D6' | '\\u00D8' .. '\\u00F6' | '\\u00F8' .. '\\u02FF' | '\\u0370' .. '\\u037D' | '\\u037F' .. '\\u1FFF' | '\\u200C' .. '\\u200D' | '\\u2070' .. '\\u218F' | '\\u2C00' .. '\\u2FEF' | '\\u3001' .. '\\uD7FF' | '\\uF900' .. '\\uFDCF' | '\\uFDF0' .. '\\uFFFD' | PermittedHighSurrogateChar LowSurrogateChar )
            int alt12=15;
            int LA12_0 = input.LA(1);

            if ( ((LA12_0>='A' && LA12_0<='Z')) ) {
                alt12=1;
            }
            else if ( (LA12_0=='_') ) {
                alt12=2;
            }
            else if ( ((LA12_0>='a' && LA12_0<='z')) ) {
                alt12=3;
            }
            else if ( ((LA12_0>='\u00C0' && LA12_0<='\u00D6')) ) {
                alt12=4;
            }
            else if ( ((LA12_0>='\u00D8' && LA12_0<='\u00F6')) ) {
                alt12=5;
            }
            else if ( ((LA12_0>='\u00F8' && LA12_0<='\u02FF')) ) {
                alt12=6;
            }
            else if ( ((LA12_0>='\u0370' && LA12_0<='\u037D')) ) {
                alt12=7;
            }
            else if ( ((LA12_0>='\u037F' && LA12_0<='\u1FFF')) ) {
                alt12=8;
            }
            else if ( ((LA12_0>='\u200C' && LA12_0<='\u200D')) ) {
                alt12=9;
            }
            else if ( ((LA12_0>='\u2070' && LA12_0<='\u218F')) ) {
                alt12=10;
            }
            else if ( ((LA12_0>='\u2C00' && LA12_0<='\u2FEF')) ) {
                alt12=11;
            }
            else if ( ((LA12_0>='\u3001' && LA12_0<='\uD7FF')) ) {
                alt12=12;
            }
            else if ( ((LA12_0>='\uF900' && LA12_0<='\uFDCF')) ) {
                alt12=13;
            }
            else if ( ((LA12_0>='\uFDF0' && LA12_0<='\uFFFD')) ) {
                alt12=14;
            }
            else if ( ((LA12_0>='\uD800' && LA12_0<='\uDB7F')) ) {
                alt12=15;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 12, 0, input);

                throw nvae;
            }
            switch (alt12) {
                case 1 :
                    // src\\main\\antlr\\XPathEnhancer.g:362:3: 'A' .. 'Z'
                    {
                    matchRange('A','Z'); 

                    }
                    break;
                case 2 :
                    // src\\main\\antlr\\XPathEnhancer.g:363:5: '_'
                    {
                    match('_'); 

                    }
                    break;
                case 3 :
                    // src\\main\\antlr\\XPathEnhancer.g:364:5: 'a' .. 'z'
                    {
                    matchRange('a','z'); 

                    }
                    break;
                case 4 :
                    // src\\main\\antlr\\XPathEnhancer.g:365:5: '\\u00C0' .. '\\u00D6'
                    {
                    matchRange('\u00C0','\u00D6'); 

                    }
                    break;
                case 5 :
                    // src\\main\\antlr\\XPathEnhancer.g:366:5: '\\u00D8' .. '\\u00F6'
                    {
                    matchRange('\u00D8','\u00F6'); 

                    }
                    break;
                case 6 :
                    // src\\main\\antlr\\XPathEnhancer.g:367:5: '\\u00F8' .. '\\u02FF'
                    {
                    matchRange('\u00F8','\u02FF'); 

                    }
                    break;
                case 7 :
                    // src\\main\\antlr\\XPathEnhancer.g:368:5: '\\u0370' .. '\\u037D'
                    {
                    matchRange('\u0370','\u037D'); 

                    }
                    break;
                case 8 :
                    // src\\main\\antlr\\XPathEnhancer.g:369:5: '\\u037F' .. '\\u1FFF'
                    {
                    matchRange('\u037F','\u1FFF'); 

                    }
                    break;
                case 9 :
                    // src\\main\\antlr\\XPathEnhancer.g:370:5: '\\u200C' .. '\\u200D'
                    {
                    matchRange('\u200C','\u200D'); 

                    }
                    break;
                case 10 :
                    // src\\main\\antlr\\XPathEnhancer.g:371:5: '\\u2070' .. '\\u218F'
                    {
                    matchRange('\u2070','\u218F'); 

                    }
                    break;
                case 11 :
                    // src\\main\\antlr\\XPathEnhancer.g:372:5: '\\u2C00' .. '\\u2FEF'
                    {
                    matchRange('\u2C00','\u2FEF'); 

                    }
                    break;
                case 12 :
                    // src\\main\\antlr\\XPathEnhancer.g:373:5: '\\u3001' .. '\\uD7FF'
                    {
                    matchRange('\u3001','\uD7FF'); 

                    }
                    break;
                case 13 :
                    // src\\main\\antlr\\XPathEnhancer.g:374:5: '\\uF900' .. '\\uFDCF'
                    {
                    matchRange('\uF900','\uFDCF'); 

                    }
                    break;
                case 14 :
                    // src\\main\\antlr\\XPathEnhancer.g:375:5: '\\uFDF0' .. '\\uFFFD'
                    {
                    matchRange('\uFDF0','\uFFFD'); 

                    }
                    break;
                case 15 :
                    // src\\main\\antlr\\XPathEnhancer.g:376:5: PermittedHighSurrogateChar LowSurrogateChar
                    {
                    mPermittedHighSurrogateChar(); 
                    mLowSurrogateChar(); 

                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "NCNameStartChar"

    // $ANTLR start "PermittedHighSurrogateChar"
    public final void mPermittedHighSurrogateChar() throws RecognitionException {
        try {
            // src\\main\\antlr\\XPathEnhancer.g:388:3: ( '\\uD800' .. '\\uDB7F' )
            // src\\main\\antlr\\XPathEnhancer.g:389:3: '\\uD800' .. '\\uDB7F'
            {
            matchRange('\uD800','\uDB7F'); 

            }

        }
        finally {
        }
    }
    // $ANTLR end "PermittedHighSurrogateChar"

    // $ANTLR start "LowSurrogateChar"
    public final void mLowSurrogateChar() throws RecognitionException {
        try {
            // src\\main\\antlr\\XPathEnhancer.g:394:3: ( '\\uDC00' .. '\\uDFFF' )
            // src\\main\\antlr\\XPathEnhancer.g:395:3: '\\uDC00' .. '\\uDFFF'
            {
            matchRange('\uDC00','\uDFFF'); 

            }

        }
        finally {
        }
    }
    // $ANTLR end "LowSurrogateChar"

    // $ANTLR start "NCNameChar"
    public final void mNCNameChar() throws RecognitionException {
        try {
            // src\\main\\antlr\\XPathEnhancer.g:400:3: ( NCNameStartChar | '-' | '.' | '0' .. '9' | '\\u00B7' | '\\u0300' .. '\\u036F' | '\\u203F' .. '\\u2040' )
            int alt13=7;
            int LA13_0 = input.LA(1);

            if ( ((LA13_0>='A' && LA13_0<='Z')||LA13_0=='_'||(LA13_0>='a' && LA13_0<='z')||(LA13_0>='\u00C0' && LA13_0<='\u00D6')||(LA13_0>='\u00D8' && LA13_0<='\u00F6')||(LA13_0>='\u00F8' && LA13_0<='\u02FF')||(LA13_0>='\u0370' && LA13_0<='\u037D')||(LA13_0>='\u037F' && LA13_0<='\u1FFF')||(LA13_0>='\u200C' && LA13_0<='\u200D')||(LA13_0>='\u2070' && LA13_0<='\u218F')||(LA13_0>='\u2C00' && LA13_0<='\u2FEF')||(LA13_0>='\u3001' && LA13_0<='\uDB7F')||(LA13_0>='\uF900' && LA13_0<='\uFDCF')||(LA13_0>='\uFDF0' && LA13_0<='\uFFFD')) ) {
                alt13=1;
            }
            else if ( (LA13_0=='-') ) {
                alt13=2;
            }
            else if ( (LA13_0=='.') ) {
                alt13=3;
            }
            else if ( ((LA13_0>='0' && LA13_0<='9')) ) {
                alt13=4;
            }
            else if ( (LA13_0=='\u00B7') ) {
                alt13=5;
            }
            else if ( ((LA13_0>='\u0300' && LA13_0<='\u036F')) ) {
                alt13=6;
            }
            else if ( ((LA13_0>='\u203F' && LA13_0<='\u2040')) ) {
                alt13=7;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 13, 0, input);

                throw nvae;
            }
            switch (alt13) {
                case 1 :
                    // src\\main\\antlr\\XPathEnhancer.g:401:3: NCNameStartChar
                    {
                    mNCNameStartChar(); 

                    }
                    break;
                case 2 :
                    // src\\main\\antlr\\XPathEnhancer.g:402:5: '-'
                    {
                    match('-'); 

                    }
                    break;
                case 3 :
                    // src\\main\\antlr\\XPathEnhancer.g:403:5: '.'
                    {
                    match('.'); 

                    }
                    break;
                case 4 :
                    // src\\main\\antlr\\XPathEnhancer.g:404:5: '0' .. '9'
                    {
                    matchRange('0','9'); 

                    }
                    break;
                case 5 :
                    // src\\main\\antlr\\XPathEnhancer.g:405:5: '\\u00B7'
                    {
                    match('\u00B7'); 

                    }
                    break;
                case 6 :
                    // src\\main\\antlr\\XPathEnhancer.g:406:5: '\\u0300' .. '\\u036F'
                    {
                    matchRange('\u0300','\u036F'); 

                    }
                    break;
                case 7 :
                    // src\\main\\antlr\\XPathEnhancer.g:407:5: '\\u203F' .. '\\u2040'
                    {
                    matchRange('\u203F','\u2040'); 

                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "NCNameChar"

    public void mTokens() throws RecognitionException {
        // src\\main\\antlr\\XPathEnhancer.g:1:8: ( PATHSEP | ABRPATH | LPAR | RPAR | LBRAC | RBRAC | MINUS | PLUS | DOT | MUL | DOTDOT | AT | COMMA | PIPE | LESS | MORE | LE | GE | COLON | CC | APOS | QUOT | T__37 | T__38 | T__39 | T__40 | T__41 | T__42 | T__43 | T__44 | NodeType | Number | AxisName | Literal | Whitespace | NCName )
        int alt14=36;
        alt14 = dfa14.predict(input);
        switch (alt14) {
            case 1 :
                // src\\main\\antlr\\XPathEnhancer.g:1:10: PATHSEP
                {
                mPATHSEP(); 

                }
                break;
            case 2 :
                // src\\main\\antlr\\XPathEnhancer.g:1:18: ABRPATH
                {
                mABRPATH(); 

                }
                break;
            case 3 :
                // src\\main\\antlr\\XPathEnhancer.g:1:26: LPAR
                {
                mLPAR(); 

                }
                break;
            case 4 :
                // src\\main\\antlr\\XPathEnhancer.g:1:31: RPAR
                {
                mRPAR(); 

                }
                break;
            case 5 :
                // src\\main\\antlr\\XPathEnhancer.g:1:36: LBRAC
                {
                mLBRAC(); 

                }
                break;
            case 6 :
                // src\\main\\antlr\\XPathEnhancer.g:1:42: RBRAC
                {
                mRBRAC(); 

                }
                break;
            case 7 :
                // src\\main\\antlr\\XPathEnhancer.g:1:48: MINUS
                {
                mMINUS(); 

                }
                break;
            case 8 :
                // src\\main\\antlr\\XPathEnhancer.g:1:54: PLUS
                {
                mPLUS(); 

                }
                break;
            case 9 :
                // src\\main\\antlr\\XPathEnhancer.g:1:59: DOT
                {
                mDOT(); 

                }
                break;
            case 10 :
                // src\\main\\antlr\\XPathEnhancer.g:1:63: MUL
                {
                mMUL(); 

                }
                break;
            case 11 :
                // src\\main\\antlr\\XPathEnhancer.g:1:67: DOTDOT
                {
                mDOTDOT(); 

                }
                break;
            case 12 :
                // src\\main\\antlr\\XPathEnhancer.g:1:74: AT
                {
                mAT(); 

                }
                break;
            case 13 :
                // src\\main\\antlr\\XPathEnhancer.g:1:77: COMMA
                {
                mCOMMA(); 

                }
                break;
            case 14 :
                // src\\main\\antlr\\XPathEnhancer.g:1:83: PIPE
                {
                mPIPE(); 

                }
                break;
            case 15 :
                // src\\main\\antlr\\XPathEnhancer.g:1:88: LESS
                {
                mLESS(); 

                }
                break;
            case 16 :
                // src\\main\\antlr\\XPathEnhancer.g:1:93: MORE
                {
                mMORE(); 

                }
                break;
            case 17 :
                // src\\main\\antlr\\XPathEnhancer.g:1:98: LE
                {
                mLE(); 

                }
                break;
            case 18 :
                // src\\main\\antlr\\XPathEnhancer.g:1:101: GE
                {
                mGE(); 

                }
                break;
            case 19 :
                // src\\main\\antlr\\XPathEnhancer.g:1:104: COLON
                {
                mCOLON(); 

                }
                break;
            case 20 :
                // src\\main\\antlr\\XPathEnhancer.g:1:110: CC
                {
                mCC(); 

                }
                break;
            case 21 :
                // src\\main\\antlr\\XPathEnhancer.g:1:113: APOS
                {
                mAPOS(); 

                }
                break;
            case 22 :
                // src\\main\\antlr\\XPathEnhancer.g:1:118: QUOT
                {
                mQUOT(); 

                }
                break;
            case 23 :
                // src\\main\\antlr\\XPathEnhancer.g:1:123: T__37
                {
                mT__37(); 

                }
                break;
            case 24 :
                // src\\main\\antlr\\XPathEnhancer.g:1:129: T__38
                {
                mT__38(); 

                }
                break;
            case 25 :
                // src\\main\\antlr\\XPathEnhancer.g:1:135: T__39
                {
                mT__39(); 

                }
                break;
            case 26 :
                // src\\main\\antlr\\XPathEnhancer.g:1:141: T__40
                {
                mT__40(); 

                }
                break;
            case 27 :
                // src\\main\\antlr\\XPathEnhancer.g:1:147: T__41
                {
                mT__41(); 

                }
                break;
            case 28 :
                // src\\main\\antlr\\XPathEnhancer.g:1:153: T__42
                {
                mT__42(); 

                }
                break;
            case 29 :
                // src\\main\\antlr\\XPathEnhancer.g:1:159: T__43
                {
                mT__43(); 

                }
                break;
            case 30 :
                // src\\main\\antlr\\XPathEnhancer.g:1:165: T__44
                {
                mT__44(); 

                }
                break;
            case 31 :
                // src\\main\\antlr\\XPathEnhancer.g:1:171: NodeType
                {
                mNodeType(); 

                }
                break;
            case 32 :
                // src\\main\\antlr\\XPathEnhancer.g:1:180: Number
                {
                mNumber(); 

                }
                break;
            case 33 :
                // src\\main\\antlr\\XPathEnhancer.g:1:187: AxisName
                {
                mAxisName(); 

                }
                break;
            case 34 :
                // src\\main\\antlr\\XPathEnhancer.g:1:196: Literal
                {
                mLiteral(); 

                }
                break;
            case 35 :
                // src\\main\\antlr\\XPathEnhancer.g:1:204: Whitespace
                {
                mWhitespace(); 

                }
                break;
            case 36 :
                // src\\main\\antlr\\XPathEnhancer.g:1:215: NCName
                {
                mNCName(); 

                }
                break;

        }

    }


    protected DFA6 dfa6 = new DFA6(this);
    protected DFA14 dfa14 = new DFA14(this);
    static final String DFA6_eotS =
        "\42\uffff\1\47\6\uffff\1\55\1\57\1\61\6\uffff";
    static final String DFA6_eofS =
        "\62\uffff";
    static final String DFA6_minS =
        "\1\141\1\156\1\uffff\1\145\1\157\1\uffff\1\141\1\uffff\1\143\1"+
        "\uffff\1\163\1\154\1\uffff\2\145\1\143\1\154\1\143\1\163\1\145\1"+
        "\157\1\145\1\164\1\156\1\167\1\144\1\157\1\144\2\151\1\162\1\141"+
        "\2\156\1\55\1\156\2\147\2\uffff\1\164\3\55\6\uffff";
    static final String DFA6_maxS =
        "\1\163\1\164\1\uffff\1\145\1\157\1\uffff\1\162\1\uffff\1\143\1"+
        "\uffff\1\163\1\154\1\uffff\2\145\1\143\1\154\1\143\1\163\1\145\1"+
        "\157\1\145\1\164\1\156\1\167\1\144\1\157\1\144\2\151\1\162\1\141"+
        "\2\156\1\55\1\156\2\147\2\uffff\1\164\3\55\6\uffff";
    static final String DFA6_acceptS =
        "\2\uffff\1\4\2\uffff\1\11\1\uffff\1\15\1\uffff\1\3\2\uffff\1\12"+
        "\31\uffff\1\2\1\1\4\uffff\1\10\1\7\1\14\1\13\1\6\1\5";
    static final String DFA6_specialS =
        "\62\uffff}>";
    static final String[] DFA6_transitionS = {
            "\1\1\1\uffff\1\2\1\3\1\uffff\1\4\7\uffff\1\5\1\uffff\1\6\2"+
            "\uffff\1\7",
            "\1\10\5\uffff\1\11",
            "",
            "\1\12",
            "\1\13",
            "",
            "\1\14\20\uffff\1\15",
            "",
            "\1\16",
            "",
            "\1\17",
            "\1\20",
            "",
            "\1\21",
            "\1\22",
            "\1\23",
            "\1\24",
            "\1\25",
            "\1\26",
            "\1\27",
            "\1\30",
            "\1\31",
            "\1\32",
            "\1\33",
            "\1\34",
            "\1\35",
            "\1\36",
            "\1\37",
            "\1\40",
            "\1\41",
            "\1\42",
            "\1\43",
            "\1\44",
            "\1\45",
            "\1\46",
            "\1\50",
            "\1\51",
            "\1\52",
            "",
            "",
            "\1\53",
            "\1\54",
            "\1\56",
            "\1\60",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA6_eot = DFA.unpackEncodedString(DFA6_eotS);
    static final short[] DFA6_eof = DFA.unpackEncodedString(DFA6_eofS);
    static final char[] DFA6_min = DFA.unpackEncodedStringToUnsignedChars(DFA6_minS);
    static final char[] DFA6_max = DFA.unpackEncodedStringToUnsignedChars(DFA6_maxS);
    static final short[] DFA6_accept = DFA.unpackEncodedString(DFA6_acceptS);
    static final short[] DFA6_special = DFA.unpackEncodedString(DFA6_specialS);
    static final short[][] DFA6_transition;

    static {
        int numStates = DFA6_transitionS.length;
        DFA6_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA6_transition[i] = DFA.unpackEncodedString(DFA6_transitionS[i]);
        }
    }

    class DFA6 extends DFA {

        public DFA6(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 6;
            this.eot = DFA6_eot;
            this.eof = DFA6_eof;
            this.min = DFA6_min;
            this.max = DFA6_max;
            this.accept = DFA6_accept;
            this.special = DFA6_special;
            this.transition = DFA6_transition;
        }
        public String getDescription() {
            return "317:1: AxisName : ( 'ancestor' | 'ancestor-or-self' | 'attribute' | 'child' | 'descendant' | 'descendant-or-self' | 'following' | 'following-sibling' | 'namespace' | 'parent' | 'preceding' | 'preceding-sibling' | 'self' );";
        }
    }
    static final String DFA14_eotS =
        "\1\uffff\1\43\6\uffff\1\45\4\uffff\1\47\1\51\1\53\1\54\1\56\3\41"+
        "\2\uffff\2\41\1\uffff\3\41\1\uffff\2\41\17\uffff\2\41\1\101\17\41"+
        "\1\uffff\1\122\2\41\1\125\1\41\1\127\12\41\1\uffff\2\41\1\uffff"+
        "\1\41\1\uffff\2\41\2\147\2\41\1\152\7\41\1\152\1\uffff\2\41\1\uffff"+
        "\2\41\1\152\13\41\1\147\4\41\1\152\5\41\1\152\1\41\1\152\1\41\2"+
        "\152\3\41\1\152\34\41\1\152\3\41\1\152\1\41\1\152\1\41\1\152\3\41"+
        "\1\u00b7\1\uffff";
    static final String DFA14_eofS =
        "\u00b8\uffff";
    static final String DFA14_minS =
        "\1\11\1\57\6\uffff\1\56\4\uffff\2\75\1\72\2\0\1\141\1\162\1\156"+
        "\2\uffff\1\145\1\157\1\uffff\1\150\1\145\1\141\1\uffff\1\157\1\145"+
        "\17\uffff\1\145\1\162\1\55\1\143\1\164\1\166\1\163\1\144\1\155\1"+
        "\151\1\170\1\144\1\155\2\154\2\143\1\145\1\uffff\1\55\1\145\1\162"+
        "\1\55\1\143\1\55\1\155\1\154\1\164\2\145\1\154\1\146\2\145\1\156"+
        "\1\uffff\1\163\1\151\1\uffff\1\145\1\uffff\1\145\1\144\2\55\1\163"+
        "\1\157\1\55\1\163\1\144\2\164\1\142\2\156\1\55\1\uffff\1\160\1\167"+
        "\1\uffff\1\163\1\151\1\55\1\157\1\165\1\144\1\164\1\141\2\151\1"+
        "\156\1\162\1\164\1\141\1\55\1\143\2\156\1\147\1\55\1\145\1\156\1"+
        "\145\2\147\1\55\1\157\1\55\1\164\3\55\1\163\1\162\1\55\1\163\2\151"+
        "\1\55\1\157\1\151\1\156\1\142\1\163\1\162\1\142\1\163\1\154\1\145"+
        "\1\55\1\154\1\164\1\151\1\154\1\163\1\151\1\162\1\156\1\146\1\145"+
        "\1\156\1\165\1\147\1\55\1\154\1\147\1\143\1\55\1\146\1\55\1\164"+
        "\1\55\1\151\1\157\1\156\1\55\1\uffff";
    static final String DFA14_maxS =
        "\1\ufffd\1\57\6\uffff\1\71\4\uffff\2\75\1\72\2\uffff\2\162\1\164"+
        "\2\uffff\1\151\1\157\1\uffff\1\157\1\145\1\157\1\uffff\1\157\1\145"+
        "\17\uffff\1\157\1\162\1\ufffd\1\144\1\164\1\166\1\163\1\144\1\155"+
        "\1\151\1\170\1\144\1\155\2\154\2\143\1\145\1\uffff\1\ufffd\1\145"+
        "\1\162\1\ufffd\1\143\1\ufffd\1\155\1\154\1\164\2\145\1\154\1\146"+
        "\2\145\1\156\1\uffff\1\163\1\151\1\uffff\1\145\1\uffff\1\145\1\144"+
        "\2\ufffd\1\163\1\157\1\ufffd\1\163\1\144\2\164\1\142\2\156\1\ufffd"+
        "\1\uffff\1\160\1\167\1\uffff\1\163\1\151\1\ufffd\1\157\1\165\1\144"+
        "\1\164\1\141\2\151\1\156\1\162\1\164\1\141\1\ufffd\1\143\2\156\1"+
        "\147\1\ufffd\1\145\1\156\1\145\2\147\1\ufffd\1\157\1\ufffd\1\164"+
        "\2\ufffd\1\55\1\163\1\162\1\ufffd\1\163\2\151\1\55\1\157\1\151\1"+
        "\156\1\142\1\163\1\162\1\142\1\163\1\154\1\145\1\55\1\154\1\164"+
        "\1\151\1\154\1\163\1\151\1\162\1\156\1\146\1\145\1\156\1\165\1\147"+
        "\1\ufffd\1\154\1\147\1\143\1\ufffd\1\146\1\ufffd\1\164\1\ufffd\1"+
        "\151\1\157\1\156\1\ufffd\1\uffff";
    static final String DFA14_acceptS =
        "\2\uffff\1\3\1\4\1\5\1\6\1\7\1\10\1\uffff\1\12\1\14\1\15\1\16\10"+
        "\uffff\1\32\1\33\2\uffff\1\36\3\uffff\1\40\2\uffff\1\43\1\44\1\2"+
        "\1\1\1\13\1\11\1\21\1\17\1\22\1\20\1\24\1\23\1\25\1\42\1\26\22\uffff"+
        "\1\30\20\uffff\1\31\2\uffff\1\34\1\uffff\1\35\17\uffff\1\37\2\uffff"+
        "\1\41\114\uffff\1\27";
    static final String DFA14_specialS =
        "\20\uffff\1\1\1\0\u00a6\uffff}>";
    static final String[] DFA14_transitionS = {
            "\2\40\2\uffff\1\40\22\uffff\1\40\1\26\1\21\1\uffff\1\31\2\uffff"+
            "\1\20\1\2\1\3\1\11\1\7\1\13\1\6\1\10\1\1\12\35\1\17\1\uffff"+
            "\1\15\1\25\1\16\1\uffff\1\12\32\41\1\4\1\uffff\1\5\1\uffff\1"+
            "\41\1\uffff\1\24\1\41\1\32\1\27\1\41\1\36\6\41\1\30\1\34\1\23"+
            "\1\22\2\41\1\37\1\33\6\41\1\uffff\1\14\103\uffff\27\41\1\uffff"+
            "\37\41\1\uffff\u0208\41\160\uffff\16\41\1\uffff\u1c81\41\14"+
            "\uffff\2\41\142\uffff\u0120\41\u0a70\uffff\u03f0\41\21\uffff"+
            "\uab7f\41\u1d80\uffff\u04d0\41\40\uffff\u020e\41",
            "\1\42",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\44\1\uffff\12\35",
            "",
            "",
            "",
            "",
            "\1\46",
            "\1\50",
            "\1\52",
            "\0\55",
            "\0\55",
            "\1\60\20\uffff\1\57",
            "\1\61",
            "\1\62\5\uffff\1\63",
            "",
            "",
            "\1\65\3\uffff\1\64",
            "\1\66",
            "",
            "\1\70\6\uffff\1\67",
            "\1\71",
            "\1\73\15\uffff\1\72",
            "",
            "\1\74",
            "\1\75",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\77\11\uffff\1\76",
            "\1\100",
            "\2\41\1\uffff\12\41\7\uffff\32\41\4\uffff\1\41\1\uffff\32"+
            "\41\74\uffff\1\41\10\uffff\27\41\1\uffff\37\41\1\uffff\u0286"+
            "\41\1\uffff\u1c81\41\14\uffff\2\41\61\uffff\2\41\57\uffff\u0120"+
            "\41\u0a70\uffff\u03f0\41\21\uffff\uab7f\41\u1d80\uffff\u04d0"+
            "\41\40\uffff\u020e\41",
            "\1\103\1\102",
            "\1\104",
            "\1\105",
            "\1\106",
            "\1\107",
            "\1\110",
            "\1\111",
            "\1\112",
            "\1\113",
            "\1\114",
            "\1\115",
            "\1\116",
            "\1\117",
            "\1\120",
            "\1\121",
            "",
            "\2\41\1\uffff\12\41\7\uffff\32\41\4\uffff\1\41\1\uffff\32"+
            "\41\74\uffff\1\41\10\uffff\27\41\1\uffff\37\41\1\uffff\u0286"+
            "\41\1\uffff\u1c81\41\14\uffff\2\41\61\uffff\2\41\57\uffff\u0120"+
            "\41\u0a70\uffff\u03f0\41\21\uffff\uab7f\41\u1d80\uffff\u04d0"+
            "\41\40\uffff\u020e\41",
            "\1\123",
            "\1\124",
            "\2\41\1\uffff\12\41\7\uffff\32\41\4\uffff\1\41\1\uffff\32"+
            "\41\74\uffff\1\41\10\uffff\27\41\1\uffff\37\41\1\uffff\u0286"+
            "\41\1\uffff\u1c81\41\14\uffff\2\41\61\uffff\2\41\57\uffff\u0120"+
            "\41\u0a70\uffff\u03f0\41\21\uffff\uab7f\41\u1d80\uffff\u04d0"+
            "\41\40\uffff\u020e\41",
            "\1\126",
            "\2\41\1\uffff\12\41\7\uffff\32\41\4\uffff\1\41\1\uffff\32"+
            "\41\74\uffff\1\41\10\uffff\27\41\1\uffff\37\41\1\uffff\u0286"+
            "\41\1\uffff\u1c81\41\14\uffff\2\41\61\uffff\2\41\57\uffff\u0120"+
            "\41\u0a70\uffff\u03f0\41\21\uffff\uab7f\41\u1d80\uffff\u04d0"+
            "\41\40\uffff\u020e\41",
            "\1\130",
            "\1\131",
            "\1\132",
            "\1\133",
            "\1\134",
            "\1\135",
            "\1\136",
            "\1\137",
            "\1\140",
            "\1\141",
            "",
            "\1\142",
            "\1\143",
            "",
            "\1\144",
            "",
            "\1\145",
            "\1\146",
            "\2\41\1\uffff\12\41\7\uffff\32\41\4\uffff\1\41\1\uffff\32"+
            "\41\74\uffff\1\41\10\uffff\27\41\1\uffff\37\41\1\uffff\u0286"+
            "\41\1\uffff\u1c81\41\14\uffff\2\41\61\uffff\2\41\57\uffff\u0120"+
            "\41\u0a70\uffff\u03f0\41\21\uffff\uab7f\41\u1d80\uffff\u04d0"+
            "\41\40\uffff\u020e\41",
            "\2\41\1\uffff\12\41\7\uffff\32\41\4\uffff\1\41\1\uffff\32"+
            "\41\74\uffff\1\41\10\uffff\27\41\1\uffff\37\41\1\uffff\u0286"+
            "\41\1\uffff\u1c81\41\14\uffff\2\41\61\uffff\2\41\57\uffff\u0120"+
            "\41\u0a70\uffff\u03f0\41\21\uffff\uab7f\41\u1d80\uffff\u04d0"+
            "\41\40\uffff\u020e\41",
            "\1\150",
            "\1\151",
            "\2\41\1\uffff\12\41\7\uffff\32\41\4\uffff\1\41\1\uffff\32"+
            "\41\74\uffff\1\41\10\uffff\27\41\1\uffff\37\41\1\uffff\u0286"+
            "\41\1\uffff\u1c81\41\14\uffff\2\41\61\uffff\2\41\57\uffff\u0120"+
            "\41\u0a70\uffff\u03f0\41\21\uffff\uab7f\41\u1d80\uffff\u04d0"+
            "\41\40\uffff\u020e\41",
            "\1\153",
            "\1\154",
            "\1\155",
            "\1\156",
            "\1\157",
            "\1\160",
            "\1\161",
            "\2\41\1\uffff\12\41\7\uffff\32\41\4\uffff\1\41\1\uffff\32"+
            "\41\74\uffff\1\41\10\uffff\27\41\1\uffff\37\41\1\uffff\u0286"+
            "\41\1\uffff\u1c81\41\14\uffff\2\41\61\uffff\2\41\57\uffff\u0120"+
            "\41\u0a70\uffff\u03f0\41\21\uffff\uab7f\41\u1d80\uffff\u04d0"+
            "\41\40\uffff\u020e\41",
            "",
            "\1\162",
            "\1\163",
            "",
            "\1\164",
            "\1\165",
            "\2\41\1\uffff\12\41\7\uffff\32\41\4\uffff\1\41\1\uffff\32"+
            "\41\74\uffff\1\41\10\uffff\27\41\1\uffff\37\41\1\uffff\u0286"+
            "\41\1\uffff\u1c81\41\14\uffff\2\41\61\uffff\2\41\57\uffff\u0120"+
            "\41\u0a70\uffff\u03f0\41\21\uffff\uab7f\41\u1d80\uffff\u04d0"+
            "\41\40\uffff\u020e\41",
            "\1\166",
            "\1\167",
            "\1\170",
            "\1\171",
            "\1\172",
            "\1\173",
            "\1\174",
            "\1\175",
            "\1\176",
            "\1\177",
            "\1\u0080",
            "\2\41\1\uffff\12\41\7\uffff\32\41\4\uffff\1\41\1\uffff\32"+
            "\41\74\uffff\1\41\10\uffff\27\41\1\uffff\37\41\1\uffff\u0286"+
            "\41\1\uffff\u1c81\41\14\uffff\2\41\61\uffff\2\41\57\uffff\u0120"+
            "\41\u0a70\uffff\u03f0\41\21\uffff\uab7f\41\u1d80\uffff\u04d0"+
            "\41\40\uffff\u020e\41",
            "\1\u0081",
            "\1\u0082",
            "\1\u0083",
            "\1\u0084",
            "\1\u0085\1\41\1\uffff\12\41\7\uffff\32\41\4\uffff\1\41\1\uffff"+
            "\32\41\74\uffff\1\41\10\uffff\27\41\1\uffff\37\41\1\uffff\u0286"+
            "\41\1\uffff\u1c81\41\14\uffff\2\41\61\uffff\2\41\57\uffff\u0120"+
            "\41\u0a70\uffff\u03f0\41\21\uffff\uab7f\41\u1d80\uffff\u04d0"+
            "\41\40\uffff\u020e\41",
            "\1\u0086",
            "\1\u0087",
            "\1\u0088",
            "\1\u0089",
            "\1\u008a",
            "\1\u008b\1\41\1\uffff\12\41\7\uffff\32\41\4\uffff\1\41\1\uffff"+
            "\32\41\74\uffff\1\41\10\uffff\27\41\1\uffff\37\41\1\uffff\u0286"+
            "\41\1\uffff\u1c81\41\14\uffff\2\41\61\uffff\2\41\57\uffff\u0120"+
            "\41\u0a70\uffff\u03f0\41\21\uffff\uab7f\41\u1d80\uffff\u04d0"+
            "\41\40\uffff\u020e\41",
            "\1\u008c",
            "\2\41\1\uffff\12\41\7\uffff\32\41\4\uffff\1\41\1\uffff\32"+
            "\41\74\uffff\1\41\10\uffff\27\41\1\uffff\37\41\1\uffff\u0286"+
            "\41\1\uffff\u1c81\41\14\uffff\2\41\61\uffff\2\41\57\uffff\u0120"+
            "\41\u0a70\uffff\u03f0\41\21\uffff\uab7f\41\u1d80\uffff\u04d0"+
            "\41\40\uffff\u020e\41",
            "\1\u008d",
            "\2\41\1\uffff\12\41\7\uffff\32\41\4\uffff\1\41\1\uffff\32"+
            "\41\74\uffff\1\41\10\uffff\27\41\1\uffff\37\41\1\uffff\u0286"+
            "\41\1\uffff\u1c81\41\14\uffff\2\41\61\uffff\2\41\57\uffff\u0120"+
            "\41\u0a70\uffff\u03f0\41\21\uffff\uab7f\41\u1d80\uffff\u04d0"+
            "\41\40\uffff\u020e\41",
            "\1\u008e\1\41\1\uffff\12\41\7\uffff\32\41\4\uffff\1\41\1\uffff"+
            "\32\41\74\uffff\1\41\10\uffff\27\41\1\uffff\37\41\1\uffff\u0286"+
            "\41\1\uffff\u1c81\41\14\uffff\2\41\61\uffff\2\41\57\uffff\u0120"+
            "\41\u0a70\uffff\u03f0\41\21\uffff\uab7f\41\u1d80\uffff\u04d0"+
            "\41\40\uffff\u020e\41",
            "\1\u008f",
            "\1\u0090",
            "\1\u0091",
            "\1\u0092\1\41\1\uffff\12\41\7\uffff\32\41\4\uffff\1\41\1\uffff"+
            "\32\41\74\uffff\1\41\10\uffff\27\41\1\uffff\37\41\1\uffff\u0286"+
            "\41\1\uffff\u1c81\41\14\uffff\2\41\61\uffff\2\41\57\uffff\u0120"+
            "\41\u0a70\uffff\u03f0\41\21\uffff\uab7f\41\u1d80\uffff\u04d0"+
            "\41\40\uffff\u020e\41",
            "\1\u0093",
            "\1\u0094",
            "\1\u0095",
            "\1\u0096",
            "\1\u0097",
            "\1\u0098",
            "\1\u0099",
            "\1\u009a",
            "\1\u009b",
            "\1\u009c",
            "\1\u009d",
            "\1\u009e",
            "\1\u009f",
            "\1\u00a0",
            "\1\u00a1",
            "\1\u00a2",
            "\1\u00a3",
            "\1\u00a4",
            "\1\u00a5",
            "\1\u00a6",
            "\1\u00a7",
            "\1\u00a8",
            "\1\u00a9",
            "\1\u00aa",
            "\1\u00ab",
            "\1\u00ac",
            "\1\u00ad",
            "\1\u00ae",
            "\2\41\1\uffff\12\41\7\uffff\32\41\4\uffff\1\41\1\uffff\32"+
            "\41\74\uffff\1\41\10\uffff\27\41\1\uffff\37\41\1\uffff\u0286"+
            "\41\1\uffff\u1c81\41\14\uffff\2\41\61\uffff\2\41\57\uffff\u0120"+
            "\41\u0a70\uffff\u03f0\41\21\uffff\uab7f\41\u1d80\uffff\u04d0"+
            "\41\40\uffff\u020e\41",
            "\1\u00af",
            "\1\u00b0",
            "\1\u00b1",
            "\2\41\1\uffff\12\41\7\uffff\32\41\4\uffff\1\41\1\uffff\32"+
            "\41\74\uffff\1\41\10\uffff\27\41\1\uffff\37\41\1\uffff\u0286"+
            "\41\1\uffff\u1c81\41\14\uffff\2\41\61\uffff\2\41\57\uffff\u0120"+
            "\41\u0a70\uffff\u03f0\41\21\uffff\uab7f\41\u1d80\uffff\u04d0"+
            "\41\40\uffff\u020e\41",
            "\1\u00b2",
            "\2\41\1\uffff\12\41\7\uffff\32\41\4\uffff\1\41\1\uffff\32"+
            "\41\74\uffff\1\41\10\uffff\27\41\1\uffff\37\41\1\uffff\u0286"+
            "\41\1\uffff\u1c81\41\14\uffff\2\41\61\uffff\2\41\57\uffff\u0120"+
            "\41\u0a70\uffff\u03f0\41\21\uffff\uab7f\41\u1d80\uffff\u04d0"+
            "\41\40\uffff\u020e\41",
            "\1\u00b3",
            "\2\41\1\uffff\12\41\7\uffff\32\41\4\uffff\1\41\1\uffff\32"+
            "\41\74\uffff\1\41\10\uffff\27\41\1\uffff\37\41\1\uffff\u0286"+
            "\41\1\uffff\u1c81\41\14\uffff\2\41\61\uffff\2\41\57\uffff\u0120"+
            "\41\u0a70\uffff\u03f0\41\21\uffff\uab7f\41\u1d80\uffff\u04d0"+
            "\41\40\uffff\u020e\41",
            "\1\u00b4",
            "\1\u00b5",
            "\1\u00b6",
            "\2\41\1\uffff\12\41\7\uffff\32\41\4\uffff\1\41\1\uffff\32"+
            "\41\74\uffff\1\41\10\uffff\27\41\1\uffff\37\41\1\uffff\u0286"+
            "\41\1\uffff\u1c81\41\14\uffff\2\41\61\uffff\2\41\57\uffff\u0120"+
            "\41\u0a70\uffff\u03f0\41\21\uffff\uab7f\41\u1d80\uffff\u04d0"+
            "\41\40\uffff\u020e\41",
            ""
    };

    static final short[] DFA14_eot = DFA.unpackEncodedString(DFA14_eotS);
    static final short[] DFA14_eof = DFA.unpackEncodedString(DFA14_eofS);
    static final char[] DFA14_min = DFA.unpackEncodedStringToUnsignedChars(DFA14_minS);
    static final char[] DFA14_max = DFA.unpackEncodedStringToUnsignedChars(DFA14_maxS);
    static final short[] DFA14_accept = DFA.unpackEncodedString(DFA14_acceptS);
    static final short[] DFA14_special = DFA.unpackEncodedString(DFA14_specialS);
    static final short[][] DFA14_transition;

    static {
        int numStates = DFA14_transitionS.length;
        DFA14_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA14_transition[i] = DFA.unpackEncodedString(DFA14_transitionS[i]);
        }
    }

    class DFA14 extends DFA {

        public DFA14(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 14;
            this.eot = DFA14_eot;
            this.eof = DFA14_eof;
            this.min = DFA14_min;
            this.max = DFA14_max;
            this.accept = DFA14_accept;
            this.special = DFA14_special;
            this.transition = DFA14_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( PATHSEP | ABRPATH | LPAR | RPAR | LBRAC | RBRAC | MINUS | PLUS | DOT | MUL | DOTDOT | AT | COMMA | PIPE | LESS | MORE | LE | GE | COLON | CC | APOS | QUOT | T__37 | T__38 | T__39 | T__40 | T__41 | T__42 | T__43 | T__44 | NodeType | Number | AxisName | Literal | Whitespace | NCName );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            IntStream input = _input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA14_17 = input.LA(1);

                        s = -1;
                        if ( ((LA14_17>='\u0000' && LA14_17<='\uFFFF')) ) {s = 45;}

                        else s = 46;

                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA14_16 = input.LA(1);

                        s = -1;
                        if ( ((LA14_16>='\u0000' && LA14_16<='\uFFFF')) ) {s = 45;}

                        else s = 44;

                        if ( s>=0 ) return s;
                        break;
            }
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 14, _s, input);
            error(nvae);
            throw nvae;
        }
    }
 

}