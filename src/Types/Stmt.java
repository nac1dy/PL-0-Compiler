package Types;

import java.util.List;

import Lexer.Token;

public abstract class Stmt
{
    public interface Visitor<R>
{
 }


 public abstract <R> R accept(Visitor<R> visitor);
}
