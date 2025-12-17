package Lexer;

public class Token
{
    final TokenType type;                                           //a Variable type with the datatype TokenType from the Enumclass.
    final String lexeme;                                            //a "blob" like "var" or "="
    final Object literal;                                           //it's like the datatype, could be a String or a Number or a identifier
    final int line;                                                 //current line

    Token(TokenType type, String lexeme, Object literal, int line)  //basic constructor
    {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    public String toString()                                        //function to convert a Token into a String
    {
        return type + " " + lexeme + " " + literal;
    }
}
