package Lexer;

public enum TokenType
{
    //PL/0 Types

    //Single-character tokens.
    PLUS, MINUS, STAR, SLASH,       // + - * /     // odd
    EQUAL, NOT_EQUAL, LESS, LESS_EQUAL, GREATER, GREATER_EQUAL, // = <> < <= > >=
    NOT,                            // #
    LEFT_PAREN, RIGHT_PAREN,       // ( )
    COMMA, SEMICOLON,              // , ;
    DOT,                        // .
    ASSIGN,                        // :=
    INPUT, OUTPUT,                 // ?, !
    EOF,                           // \n

    //Keywords.
    BEGIN, END,                    // begin end
    IF, THEN,                      // if then
    WHILE, DO,                     // while do
    REPEAT, UNTIL,                 // repeat until
    CALL,                          // call
    CONST, VAR, PROCEDURE,         // const var procedure
    ODD,

    //Literals
    IDENTIFIER, NUMBER,             // identifier number
}
