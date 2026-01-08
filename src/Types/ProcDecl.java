package Types;

import Lexer.Token;

/**
 * procedure <name> ; <block> ;
 */
public class ProcDecl {
    public final Token name;
    public final Block block;

    public ProcDecl(Token name, Block block) {
        this.name = name;
        this.block = block;
    }
}
