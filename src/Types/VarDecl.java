package Types;

import Lexer.Token;

/**
 * var <name>
 */
public class VarDecl {
    public final Token name;

    public VarDecl(Token name) {
        this.name = name;
    }
}
