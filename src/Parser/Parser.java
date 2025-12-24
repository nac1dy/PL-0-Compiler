package Parser;

import Lexer.Token;
import Lexer.TokenType;
import Lexer.TokenType.*;
import Types.*;
import Compiler.Compiler;

import java.util.List;

//TODO Add Condition Parsing

public class Parser {

    public static class ParseError extends RuntimeException {}

    public final List<Token> tokens;       //List of Tokens, passed from the Lexer
    public int current = 0;                //Variable to point to the next Token that is to be parsed

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public Expr parse() {
        try {
            return expression();
        } catch (ParseError error) {
            return null;
        }
    }

    private Expr expression() {
        //expression -> term
        return term();
    }

/*

TODO I Will make a hole Condition Parser where this is then parsed


    private Expr equality() {
        //equality -> comparison ( ( "!=" | "==" ) comparison )*

        //this is the first comparison on the left side
        Expr expr = comparison(); //left expression

        //then we check for more comparisons with equality operators
        // (...)* -> while-loop
        while (match(TokenType.NOT_EQUAL, TokenType.EQUAL)) {
            Token operator = previous();    //operator ( "#", "=" )
            Expr right = comparison();      //right expression
            expr = new Expr.Binary(expr, operator, right); //override expr with new Binary expr
        }

        return expr;
    }
*/
    //                      HELPER
    //----------------------------------------------------------------------------------------------------------------------------------------------------

    //checks if the current Token is any of the given type
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    //checks if the current Token is of the given type
    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    //consumes the current Token and returns it
    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    //checks if we are at the end of the Token list
    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    //returns the current Token without consuming it
    private Token peek() {
        return tokens.get(current);
    }

    //returns the most recently consumed Token without consuming it
    private Token previous() {
        return tokens.get(current - 1);
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();

        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        Compiler.error(token, message);
        return new ParseError();
    }

    /*
    TODO: maybe have to edit that because semicolon in PL/0 are weirde
     */

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) return;

            switch (peek().type) {
                case ASSIGN:
                case CALL:
                case INPUT:
                case OUTPUT:
                case BEGIN:
                case END:
                case IF:
                case THEN:
                case WHILE:
                case DO:
                case REPEAT:
                case UNTIL:
                    return;
            }

            advance();
        }
    }

    //----------------------------------------------------------------------------------------------------------------------------------------------------

/*
TODO I Will make a hole Condition Parser where this is then parsed

    private Expr comparison() {
        //comparison -> term (( ">" | ">=" | "<" | "<=" ) term )*

        Expr expr = term(); //left expression

        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            Token operator = previous();    //operator ( ">", ">=", "<", "<=" )
            Expr right = term();            //right expression
            expr = new Expr.Binary(expr, operator, right); //override expr with new Binary expr
        }
        return expr;
    }
*/

    private Expr term() {
        //term -> factor ( ( "+" | "-" ) factor )*

        Expr expr = factor(); //left expression

        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token operator = previous();    //operator ( "+", "-" )
            Expr right = factor();          //right expression
            expr = new Expr.Binary(expr, operator, right); //override expr with new Binary expr
        }
        return expr;
    }

    private Expr factor() {
        Expr expr = unary(); //left expression

        while (match(TokenType.STAR, TokenType.SLASH)) {
            Token operator = previous();    //operator ( "*", "/" )
            Expr right = unary();           //right expression
            expr = new Expr.Binary(expr, operator, right); //override expr with new Binary expr
        }
        return expr;
    }

    private Expr unary() {
        if (match(TokenType.MINUS, TokenType.ODD)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return primary();
    }

    private Expr primary() {
        if (match(TokenType.NUMBER)) {
            return new Expr.Literal(previous().literal);
        }
        if (match(TokenType.LEFT_PAREN)) {
            Expr expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }
        throw error(peek(), "Expect expression");
    }
}
