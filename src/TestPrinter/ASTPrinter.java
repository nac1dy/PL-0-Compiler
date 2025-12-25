package TestPrinter;

import Types.Condition;
import Types.Expr;


public class ASTPrinter implements Expr.Visitor<String>, Condition.Visitor<String> {


    public String print(Condition condition) {
        return condition.accept(this);
    }


    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }


    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.expression);
    }


    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) return "nil";
        return expr.value.toString();
    }


    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        return expr.name.lexeme;
    }


    private String parenthesize(String name, Expr... expressions) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expr expr : expressions) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }
    private String parenthesize(String name, Condition... conditions) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Condition cond : conditions) {
            builder.append(" ");
            builder.append(cond.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }

    @Override
    public String visitUnaryConditionCondition(Condition.UnaryCondition condition) {
        return parenthesize(condition.operator.lexeme, condition.expression);
    }


    @Override
    public String visitBinaryConditionCondition(Condition.BinaryCondition condition) {
        return parenthesize(condition.operator.lexeme, condition.left, condition.right);
    }
}
