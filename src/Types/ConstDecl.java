package Types;

import Lexer.Token;

/**
 * const <name> = <number>
 */
public class ConstDecl {
    public final Token name;
    public final int value;

    public ConstDecl(Token name, int value) {
        this.name = name;
        this.value = value;
    }
}
