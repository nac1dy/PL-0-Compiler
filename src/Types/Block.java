package Types;

import Lexer.Token;

import java.util.List;

/**
 * PL/0-Block.
 *
 * (vereinfacht)
 *   block -> [constDecl] [varDecl] {procDecl} statement
 */
public class Block {
    public final List<ConstDecl> constDecls;
    public final List<VarDecl> varDecls;
    public final List<ProcDecl> procDecls;
    public final Stmt statement;

    public Block(List<ConstDecl> constDecls,
                 List<VarDecl> varDecls,
                 List<ProcDecl> procDecls,
                 Stmt statement)
    {
        this.constDecls = constDecls;
        this.varDecls = varDecls;
        this.procDecls = procDecls;
        this.statement = statement;
    }


}
