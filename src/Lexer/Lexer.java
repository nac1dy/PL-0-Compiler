package Lexer;
import Lexer.TokenType.*;

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
    List<Token> lexSomeTokens()
    {
        while(!isAtEnd()){
            start = current;
            lexAToken();
        }
        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    //Helper method to check if we are at the end of the source
    private boolean isAtEnd()
    {
        return current >= source.length();
    }

    private void lexAToken(){
        char c = advance();
        switch(c)
        {
            case '(': addToken(TokenType.LEFT_PAREN);break;
            case ')': addToken(TokenType.RIGHT_PAREN);break;

            case ',': addToken(TokenType.COMMA);break;
            case '.': addToken(TokenType.DOT);break;

            case '-': addToken(TokenType.MINUS);break;
            case '+': addToken(TokenType.PLUS);break;
            case '*': addToken(TokenType.STAR);break;

            case ';': addToken(TokenType.SEMICOLON);break;
            case '#': addToken(match('=') ? TokenType.NOT_EQUAL : TokenType.NOT);break;
            case '=': addToken(TokenType.EQUAL);break;
            case '<': addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);break;
            case '>': addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);break;
            case '/': addToken(TokenType.SLASH); break;
            case ' ':
            case'\r':
            case'\t': break;
            case'\n': line++; break;
            case '"': String(); break;
            default:
                if(isDigit(c))
                {
                    number();
                }
                else if(isAlpha(c))
                {
                    identifier();
                }
                break;
        }
    }

    private void addToken(TokenType type)
    {

    }

    private char advance() {
        return source.charAt(current++);
    }
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
    private boolean isAlpha(char c){
        return (c >='a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }
    private boolean isAlphaNumeric(char c){
        return isAlpha(c) || isDigit(c);
    }


    private void number() {
    }
    private void String() {
    }
    private void identifier() {
    }


    /*
    TODO:
    match()
    peek()
    advance()
    addToken()
    String()
    isDigit()
    number()
    isAlpha()
    identifier()
     */


    private static final Map<String, TokenType> keywords;

    static
    {
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
