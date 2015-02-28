package team1619.scouting.server.utils;

import java.util.HashMap;
import java.util.Stack;


/**
 * This class implements a simple JSON parser.  The resulting JSON
 * object allow you to access the fields via their names.
 * * 
 * Created by tolkin on 2/15/2015.
 */
public class SCJSON extends HashMap<String, Object>
{
    private static enum SCJSONTokenType
    {
        OPEN_SCOPE,             // {
        CLOSE_SCOPE,            // }
        FIELD_SEPARATOR,        // ,
        NUMBER_VALUE,
        STRING_VALUE,
        ENTRY_SEPARATOR,        // :
        TRUE_VALUE,             // true
        FALSE_VALUE,            // false
        UNKNOWN,
        EOF                     // end of string
    }
    
    private static class SCJSONToken
    {
        private SCJSONTokenType fType;
        private Object fValue;
        
        public SCJSONToken( SCJSONTokenType type )
        {
            this( type, null );
        }
        
        public SCJSONToken( SCJSONTokenType type, Object value )
        {
            fType = type;
            fValue = value;
        }
        
        public SCJSONTokenType getType()
        {
            return fType;
        }
        
        public Object getValue()
        {
            return fValue;
        }
    }
    
    private static class SCJSONTokenizer
    {
        private int fPos;
        private String fBuffer;
        private SCJSONToken fLastToken;

        public SCJSONTokenizer( String buffer )
        {
            fBuffer = buffer;
            fPos = -1;
            fLastToken = null;
        }
        
        public void pushLastToken( SCJSONToken lastToken )
        {
            fLastToken = lastToken;
        }

        public SCJSONToken getNextToken()
        {
            if ( fLastToken != null )
            {
                SCJSONToken tokenToReturn = fLastToken;
                fLastToken = null;
                return tokenToReturn;
            }
            
            fPos++;

            if ( fPos >= fBuffer.length() )
            {
                return new SCJSONToken( SCJSONTokenType.EOF );
            }

            char c = fBuffer.charAt( fPos );
            // skip whitespace
            while ( fPos < fBuffer.length() && Character.isWhitespace( c ) )
            {
                fPos++;
                c = fBuffer.charAt( fPos );
            }

            switch ( c )
            {
                case '{':
                {
                    // open struct
                    return new SCJSONToken( SCJSONTokenType.OPEN_SCOPE );
                }
                case '}':
                {
                    // close struct
                    return new SCJSONToken( SCJSONTokenType.CLOSE_SCOPE );
                }
                case '"':
                case '\'':
                {
                    // beginning of a string literal 
                    return new SCJSONToken( SCJSONTokenType.STRING_VALUE, readString( c ) );
                }
                case ':':
                {
                    // entity separator
                    return new SCJSONToken( SCJSONTokenType.ENTRY_SEPARATOR );
                }
                case ',':
                {
                    // field separator
                    return new SCJSONToken( SCJSONTokenType.FIELD_SEPARATOR );
                }
                default:
                {
                    if ( Character.isDigit( c ) || c == '-' || c == '+' || c == '.' )
                    {
                        // assume number
                        return new SCJSONToken( SCJSONTokenType.NUMBER_VALUE, readNumber() );
                    }

                    String name = readName();

                    if ( "true".equalsIgnoreCase( name ) )
                    {
                        return new SCJSONToken( SCJSONTokenType.TRUE_VALUE );
                    }
                    else if ( "false".equalsIgnoreCase( name ) )
                    {
                        return new SCJSONToken( SCJSONTokenType.FALSE_VALUE );
                    }
                    else
                    {
                        // unknown token
                        return new SCJSONToken( SCJSONTokenType.UNKNOWN );
                    }
                }
            }
        }

        /**
         * Reads a string literal and returns the string without the quotes.
         * 
         * @param quote the opening quote character
         *  
         * @return the string
         */
        private String readString( char quote )
        {
            // start with next character
            fPos++;
            StringBuilder buf = new StringBuilder();
            
            while ( fPos < fBuffer.length() )
            {
                char c = fBuffer.charAt( fPos );

                if ( c == quote )
                {
                    // end of string
                    return buf.toString();
                }
                else if ( c == '\\' )
                {
                    // this is an escape character, so write next one
                    fPos++;
                    if ( fPos < fBuffer.length() )
                    {
                        c = fBuffer.charAt( fPos );
                    }
                    else
                    {
                        throw new SCJSONSyntaxException( "unexpected end of input" );
                    }
                }
                
                buf.append( c );
                fPos++;
            }
            
            throw new SCJSONSyntaxException( "Missing end of quoted string" );
        }

        /**
         * This reads a contiguous sequence of alphanumeric characters.
         *  
         * @return the name or identifier
         */
        private String readName()
        {
            char c = fBuffer.charAt( fPos );
            StringBuilder buf = new StringBuilder();
            
            do
            {
                buf.append( c );
                fPos++;
                c = fBuffer.charAt( fPos );
            }
            while ( fPos < fBuffer.length() && Character.isJavaIdentifierPart( c ) );

            fPos--;
            
            return buf.toString();
        }

        /**
         * Reads a number (integer or floating point).
         *  
         * @return the double rep of the number
         */
        private Double readNumber()
        {
            char c = fBuffer.charAt( fPos );
            StringBuilder buf = new StringBuilder();
            
            do
            {
                buf.append( c );
                fPos++;
                c = fBuffer.charAt( fPos );
            }
            while ( fPos < fBuffer.length() && ( Character.isDigit( c ) || c == '.' ) );

            fPos--;
            
            return Double.valueOf( buf.toString() );
        }
    }
    
    private static class SCJSONParser
    {
        private SCJSONTokenizer fTokenizer;
        private SCJSON fCurrentScope;
        private Stack<SCJSON> fScopes;
        
        public SCJSONParser( SCJSONTokenizer tokenizer )
        {
            fTokenizer = tokenizer;
            
            fScopes = new Stack<>();
        }
        
        public SCJSON parse()
        {
            SCJSONToken token = fTokenizer.getNextToken();
            
            SCJSON scopeToReturn;
            
            if ( token.getType() == SCJSONTokenType.OPEN_SCOPE )
            {
                if ( fCurrentScope != null )
                {
                    fScopes.push( fCurrentScope );
                }
                
                fCurrentScope = new SCJSON();
                do
                {
                    parseField();
                    token = fTokenizer.getNextToken();
                }
                while ( token.getType() == SCJSONTokenType.FIELD_SEPARATOR );
                
                if ( token.getType() != SCJSONTokenType.CLOSE_SCOPE )
                {
                    throw new SCJSONSyntaxException( "expecting }, found " + token.getType() );
                }

                scopeToReturn = fCurrentScope;

                if ( !fScopes.isEmpty() )
                {
                    fCurrentScope = fScopes.pop();
                }
            }
            else
            {
                throw new SCJSONSyntaxException( "unexpected start of JSON structure" );
            }
            
            return scopeToReturn;
        }

        private void parseField() 
        {
            SCJSONToken token = fTokenizer.getNextToken();
            
            if ( token.getType() != SCJSONTokenType.STRING_VALUE )
            {
                throw new SCJSONSyntaxException( "expecting field name, found " + token.getType() );
            }
            
            String key = (String)token.getValue();
            
            token = fTokenizer.getNextToken();
            
            if ( token.getType() != SCJSONTokenType.ENTRY_SEPARATOR )
            {
                throw new SCJSONSyntaxException( "expecting :, found " + token.getType() );
            }
            
            token = fTokenizer.getNextToken();
            
            switch ( token.getType() )
            {
                case STRING_VALUE:
                {
                    fCurrentScope.put( key, token.getValue() );
                    break;
                }
                case NUMBER_VALUE:
                {
                    fCurrentScope.put( key, token.getValue() );
                    break;
                }
                case TRUE_VALUE:
                {
                    fCurrentScope.put( key, Boolean.TRUE );
                    break;
                }
                case FALSE_VALUE:
                {
                    fCurrentScope.put( key, Boolean.FALSE );
                    break;
                }
                case OPEN_SCOPE:
                {
                    // nested structure
                    fTokenizer.pushLastToken( token );
                    fCurrentScope.put( key, parse() );
                    break;
                }
                default:
                {
                    throw new SCJSONSyntaxException( "unexpected token for value: " + token.getType() );
                }
            }
        }
    }
    
    public SCJSON()
    {
    }

    /**
     * Implements a simple JSON parser.
     *  
     * NOTE: We are not parsing arrays for now.
     *  
     * @param buffer the buffer holding the JSON object
     *  
     * @return an object that can be used to get values from the JSON object
     */
    public static SCJSON parse( String buffer )
    {
        SCJSONParser parser = new SCJSONParser( new SCJSONTokenizer( buffer ) );

        return parser.parse();
    }
}
