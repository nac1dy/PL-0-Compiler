package Lexer;

import Lexer.TokenType.*;
import Compiler.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lexer {

    private final String source;                                    //String of the source code
    private final List<Token> tokens = new ArrayList<>();           //ArrayList of Tokens
    private int start = 0;                                          //points to the first character in the lexeme being scanned
    private int current = 0;                                        //points at the character currently being considered
    private int line = 1;                                           //tracks on what source line "current" is on

    public Lexer(String source)                                     //Constuctor
    {
        this.source = source;
    }

    //Method to lex Tokens
    public List<Token> lexSomeTokens() {
        while (!isAtEnd()) {
            start = current;
            lexAToken();
        }
        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }
    //Method that lexes a single token
    private void lexAToken() {
        char c = advance();
        switch (c) {
            case '(':
                addToken(TokenType.LEFT_PAREN);
                break;
            case ')':
                addToken(TokenType.RIGHT_PAREN);
                break;

            case ',':
                addToken(TokenType.COMMA);
                break;
            case '.':
                addToken(TokenType.DOT);
                break;

            case '-':
                addToken(TokenType.MINUS);
                break;
            case '+':
                addToken(TokenType.PLUS);
                break;
            case '*':
                addToken(TokenType.STAR);
                break;

            case '!':
                addToken(TokenType.OUTPUT);
                break;
            case '?':
                addToken(TokenType.INPUT);
                break;

            case ':':
                addToken(match('=') ? TokenType.ASSIGN : null);
                break;
            case ';':
                addToken(TokenType.SEMICOLON);
                break;
            case '#':
                addToken(match('=') ? TokenType.NOT_EQUAL : TokenType.NOT);
                break;
            case '=':
                addToken(TokenType.EQUAL);
                break;
            case '<':
                addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
                break;
            case '>':
                addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
                break;
            case '/':
                addToken(TokenType.SLASH);
                break;

            //skip over meaningless characters
            //------------------------------
            case ' ':
            case '\r':
            case '\t':
                break;
            case '\n':
                line++;
                break;
            case '"':
                //String();

                Compiler.error(line, "Strings are not supported in PL/0.");
                break;
            //------------------------------
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Compiler.error(line, "Unexpected character: " + c);
                } break;
        }
    }

    //method that adds a token to the tokens list
    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }


    //                                  Helper
    //--------------------------------------------------------------------------------------------------------------------------------------------//


    //method that returns the next character in source
    private char advance() {
        return source.charAt(current++);
    }

    //Helper method to check if we are at the end of the source
    private boolean isAtEnd() {
        return current >= source.length();
    }

    //only consumes the current character if it matches the expected character
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    //looks at current unconsumed character (lookahead)
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    //helper function for number() to lookahead one character without consuming
    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }



//--------------------------------------------------------------------------------------------------------------------------------------------//



    //Functions to match numbers, Strings and identifiers
/* Funny thing, PL/0 doesn't have strings... will be added later maybe O_O

    private void String() {
        while(peek() != '"' && !isAtEnd())
        {
            if(peek() == '\n') line++;
            advance(); //returns the next character
        }
        if(isAtEnd())
        {
            Compiler.error(line, "Unterminated String");
            return;
        }
        advance(); //the closing "

        String value = source.substring(start+1, current-1); //everything between the quotes
        addToken(TokenType.STRING,value);
    }
*/

    //Handles numbers (only integers in PL/0)
    private void number() {
        while (isDigit(peek())) advance();

        if (peek() == '.' && isDigit(peekNext())) {
            Compiler.error(line, "Floating point numbers are not supported in PL/0.");
        } else {
            addToken(TokenType.NUMBER, Integer.parseInt(source.substring(start, current)));
        }
        //because PL/0 only has integer numbers we handle floating point numbers as an error
    }

    /*
        Handles identifiers and keywords, we have to differentiate between the two because:
        what if someone names a variable "and" or "if"? The Scanner would just think it's a keyword
        so we use the maximal munch principle: whichever matches the longest string of characters wins
        so "and" is an keyword, but "anderson" is an identifier
     */
    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text); //returns null if no type is found to the corresponding text (keyword)
        if (type == null) type = TokenType.IDENTIFIER;
        addToken(type);
    }

    private static final Map<String, TokenType> keywords;

    //map of keywords in PL/0
    static {
        keywords = new HashMap<>();
        keywords.put("begin", TokenType.BEGIN);
        keywords.put("end", TokenType.END);
        keywords.put("if", TokenType.IF);
        keywords.put("then", TokenType.THEN);
        keywords.put("while", TokenType.WHILE);
        keywords.put("do", TokenType.DO);
        keywords.put("repeat", TokenType.REPEAT);
        keywords.put("until", TokenType.UNTIL);
        keywords.put("call", TokenType.CALL);
        keywords.put("const", TokenType.CONST);
        keywords.put("var", TokenType.VAR);
        keywords.put("procedure", TokenType.PROCEDURE);
        keywords.put("odd", TokenType.ODD);
    }
}
