package Compiler;

import Types.Condition;
import Types.Expr;

public class CodeGenerator implements Expr.Visitor<Object>, Condition.Visitor<Object>
{

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        return null;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return null;
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return null;
    }

    @Override
    public Object visitUnaryConditionCondition(Condition.UnaryCondition condition) {
        return null;
    }

    @Override
    public Object visitBinaryConditionCondition(Condition.BinaryCondition condition) {
        return null;
    }
}
