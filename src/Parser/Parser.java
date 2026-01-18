package Parser;

import Compiler.*;
import Lexer.Token;
import Lexer.TokenType;
import Types.*;

import java.util.ArrayList;
import java.util.List;


public class Parser {

    public static class ParseError extends RuntimeException {
    }

    private final List<Token> tokens;
    private int current = 0;


    /**
     * tracker for the current line
     * after every consumed token, we update this to the line of that token
     */
    public int errorLine = 1;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;

    }

    // ----------------- Entry -----------------

    /*
     * Entry point of PL/0 Programm, oriented on the exact grammar
     * Next step would be "matching" a Block and a final DOT
     *
     */
    public Program parseProgram() {
        try {
            Block block = block();
            consume(TokenType.DOT, "Expect '.' after program.");
            consume(TokenType.EOF, "Expect end of file after '.'. Found: " + peek().type + " ('" + peek().lexeme + "')");
            return new Program(block);
        } catch (ParseError e) {
            return null;
        }
    }

    // ----------------- block + decls -----------------

    /*
     * In here we define all the main declarations of consts, variables and procedures with a last statement
     * the block is basically everything the first declaration and the last "end."
     * and in there, there can be only one main definition of variables at the start and then once we are in procedures we have global or local ones
     *
     */
    private Block block() {

        /*
         * First we match every declaration of consts and vars
         */
        List<ConstDecl> constDecls = new ArrayList<>();
        List<VarDecl> varDecls = new ArrayList<>();
        List<ProcDecl> procDecls = new ArrayList<>();

        // const ... ;
        try {
            if (match(TokenType.CONST)) {
                do {
                    Token name = consume(TokenType.IDENTIFIER, "Expect constant name.");
                    consume(TokenType.EQUAL, "Expect '=' after constant name.");
                    Token valueTok = consume(TokenType.NUMBER, "Expect number after '=' in const declaration.");
                    constDecls.add(new ConstDecl(name, (Integer) valueTok.literal));
                } while (match(TokenType.COMMA));
                consume(TokenType.SEMICOLON, "Expect ';' after const declaration.");
            }


            // var ... ;
            if (match(TokenType.VAR)) {
                do {
                    Token name = consume(TokenType.IDENTIFIER, "Expect variable name.");
                    varDecls.add(new VarDecl(name));
                } while (match(TokenType.COMMA));
                consume(TokenType.SEMICOLON, "Expect ';' after var declaration.");
            }

            // procedure ... ; block ;
            /*
             * once we hit procedures, we call a new Block and start again.
             */
            while (match(TokenType.PROCEDURE)) {
                Token name = consume(TokenType.IDENTIFIER, "Expect procedure name.");
                consume(TokenType.SEMICOLON, "Expect ';' after procedure name.");
                Block body = block();
                consume(TokenType.SEMICOLON, "Expect ';' after procedure block.");
                procDecls.add(new ProcDecl(name, body));
            }
        } catch (ParseError e) {
            synchronize();
        }

        Stmt statement = statement();
        return new Block(constDecls, varDecls, procDecls, statement);
    }

    // ----------------- statements -----------------


    /*
     * now that we are in the block and in particular in the statements section, we start matching different statements
     *
     * We do this just like the grammar says
     * and return the corresponding Stmt object which then is a node in the AST tree which we can later traverse
     *
     */

    private Stmt statement() {
        // assignment
        try {
            if (check(TokenType.IDENTIFIER)) {
                Token name = advance();
                consume(TokenType.ASSIGN, "Expect ':=' after identifier and 'call' before Identifier for Procedure Calls");
                Expr value = expression();
                return new Stmt.AssignStmt(name, value);
            }


            // call
            if (match(TokenType.CALL)) {
                Token name = consume(TokenType.IDENTIFIER, "Expect procedure name after 'call'.");
                return new Stmt.CallStmt(name);
            }

            // input
            if (match(TokenType.INPUT)) {
                Token name = consume(TokenType.IDENTIFIER, "Expect identifier after '?'.");
                return new Stmt.InputStmt(name);
            }

            // output
            if (match(TokenType.OUTPUT)) {
                Expr value = expression();
                return new Stmt.OutputStmt(value);
            }

            // begin ... end
            if (match(TokenType.BEGIN)) {
                List<Stmt> statements = new ArrayList<>();

                if (!check(TokenType.END)) {
                    statements.add(statement());
                    while (match(TokenType.SEMICOLON)) {
                        // ';' is a separator, not a terminator.
                        if (check(TokenType.END)) {
                            Compiler.error(errorLine, "This is PL/0 and not Java or C so ';' are only for seperating Statements, please remove the last ';' before the 'end' in the named line!");
                            break;
                        }
                        statements.add(statement());
                    }
                }

                consume(TokenType.END, "Expect 'end' after begin-block.");
                return new Stmt.BeginEndStmt(statements);
            }

            // if ... then ...
            if (match(TokenType.IF)) {
                Condition cond = condition();
                consume(TokenType.THEN, "Expect 'then' after condition.");
                Stmt thenBranch = statement();
                return new Stmt.IfStmt(cond, thenBranch);
            }

            // while ... do ...
            if (match(TokenType.WHILE)) {
                Condition cond = condition();
                consume(TokenType.DO, "Expect 'do' after condition.");
                Stmt body = statement();
                return new Stmt.WhileStmt(cond, body);
            }

            // repeat ... until ...
            if (match(TokenType.REPEAT)) {
                List<Stmt> parts = new ArrayList<>();
                parts.add(statement());
                while (match(TokenType.SEMICOLON)) {
                    if (check(TokenType.UNTIL)) {
                        Compiler.error(errorLine, "In your Repeat Until Statement is a wrong ';', please remove the last ';' before the 'until' keyword!");
                        break;
                    }
                    parts.add(statement());
                }

                consume(TokenType.UNTIL, "Expect 'until' after repeat-body.");
                Condition cond = condition();
                return new Stmt.RepeatUntilStmt(new Stmt.BeginEndStmt(parts), cond);
            }

            // empty statement (epsilon) is allowed in PL/0 grammar.
            return new Stmt.BeginEndStmt(new ArrayList<>());
        } catch (ParseError e) {
            synchronize();
            // After recovery, return an empty statement so parsing can continue.
            return new Stmt.BeginEndStmt(new ArrayList<>());
        }
    }
    // ----------------- condition -----------------

    /*
     * Conditions: we have unary and binary (exactly like expressions) and either we match odd and we know its unary
     * or we know, it's a binary, and we get the left expression, operator and right expression
     */
    public Condition condition() {
        if (match(TokenType.ODD)) {
            return new Condition.UnaryCondition(previous(), expression());
        }

        Expr left = expression();
        if (!match(TokenType.EQUAL, TokenType.NOT_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL, TokenType.GREATER, TokenType.GREATER_EQUAL)) {
            throw error(peek(), "Expect comparison operator after left expression in condition.");
        }
        Token operator = previous();
        Expr right = expression();
        return new Condition.BinaryCondition(left, operator, right);
    }

    // ----------------- expressions -----------------


    /*
     * also like before, we edited this a bit to match the PL/0 grammar more and we added a check for integers so we only allow
     * 1+1 and not 1.5+2.3 for example and also not 1 + a if a  is just a char and no variable
     *
     */
    private Expr expression() {
        // expression -> term ( ( "+" | "-" ) term )*

        if (match(TokenType.MINUS)) {
            Token operator = previous();
            Expr right = term();
            return new Expr.Unary(operator, right);
        } else {
            Expr expr = term(); //left expression

            while (match(TokenType.PLUS, TokenType.MINUS)) {
                Token operator = previous();    //operator ( "+", "-" )
                Expr right = term();            //right expression
                expr = new Expr.Binary(expr, operator, right); //override expr with new Binary expr
            }
            return expr;
        }
    }

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
        // Wichtig: EOF ist ein echtes Token (vom Lexer erzeugt) und muss auch am Ende noch matchen.
        if (type == TokenType.EOF) return peek().type == TokenType.EOF;
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    //consumes the current Token and returns it
    private Token advance() {
        if (!isAtEnd()) current++;
        Token prev = previous();
        // errorLine set to last consumed Token's line
        this.errorLine = prev.line;
        return prev;
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

        if (!isAtEnd()) this.errorLine = peek().line;
        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        // Determine the line number for the error message
        int line = (token != null) ? token.line : this.errorLine;

        if (token == null) {
            Compiler.error(line, message);
        } else if (token.type == TokenType.EOF) {
            Compiler.error(line, message);
        } else {
            Compiler.error(token, message);
        }
        return new ParseError();
    }


    /*
     * Error recovery: synchronize the parser by discarding tokens until
     * we reach a statement boundary.
     */
    private void synchronize() {

        if (!isAtEnd()) advance();

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

    private Expr term() {
        //term -> factor ( ( "+" | "-" ) factor )*

        Expr expr = factor(); //left expression

        while (match(TokenType.STAR, TokenType.SLASH)) {
            Token operator = previous();    //operator ( "+", "-" )
            Expr right = factor();          //right expression
            expr = new Expr.Binary(expr, operator, right); //override expr with new Binary expr
        }
        return expr;
    }

    private Expr factor() {
        //check that number is only integer for PL/0
        if (match(TokenType.NUMBER) && previous().literal instanceof Integer) {
            return new Expr.Literal(previous().literal);
        }
        if (match(TokenType.IDENTIFIER)) {
            return new Expr.Variable(previous());
        }
        if (match(TokenType.LEFT_PAREN)) {
            Expr expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect expression");
    }

}

