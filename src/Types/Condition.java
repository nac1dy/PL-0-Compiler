package Types;

import java.util.List;

import Lexer.Token;

public abstract class Condition
{
    public interface Visitor<R>
{
    R visitUnaryConditionCondition(UnaryCondition condition);
    R visitBinaryConditionCondition(BinaryCondition condition);
 }

    public static class UnaryCondition extends Condition
 {
    public UnaryCondition(Token operator, Expr expression)
{
    this.operator = operator;
    this.expression = expression;
    }

    @Override
    public <R> R accept(Visitor<R> visitor)
{
    return visitor.visitUnaryConditionCondition(this);
    }

    public final Token operator;
    public final Expr expression;
 }
    public static class BinaryCondition extends Condition
 {
    public BinaryCondition(Expr left, Token operator, Expr right)
{
    this.left = left;
    this.operator = operator;
    this.right = right;
    }

    @Override
    public <R> R accept(Visitor<R> visitor)
{
    return visitor.visitBinaryConditionCondition(this);
    }

    public final Expr left;
    public final Token operator;
    public final Expr right;
 }

 public abstract <R> R accept(Visitor<R> visitor);
}
