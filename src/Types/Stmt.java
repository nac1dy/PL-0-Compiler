package Types;

import java.util.List;

import Lexer.Token;

public abstract class Stmt {
    public interface Visitor<R> {
        R visitOutputStmtStmt(OutputStmt stmt);

        R visitIfStmtStmt(IfStmt stmt);

        R visitWhileStmtStmt(WhileStmt stmt);

        R visitRepeatUntilStmtStmt(RepeatUntilStmt stmt);

        R visitInputStmtStmt(InputStmt stmt);

        R visitAssignStmtStmt(AssignStmt stmt);

        R visitCallStmtStmt(CallStmt stmt);

        R visitBeginEndStmtStmt(BeginEndStmt stmt);
    }

    public static class OutputStmt extends Stmt {
        public OutputStmt(Expr expression) {
            this.expression = expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitOutputStmtStmt(this);
        }

        public final Expr expression;
    }

    public static class IfStmt extends Stmt {
        public IfStmt(Condition condition, Stmt thenBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStmtStmt(this);
        }

        public final Condition condition;
        public final Stmt thenBranch;
    }

    public static class WhileStmt extends Stmt {
        public WhileStmt(Condition condition, Stmt body) {
            this.condition = condition;
            this.body = body;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhileStmtStmt(this);
        }

        public final Condition condition;
        public final Stmt body;
    }

    public static class RepeatUntilStmt extends Stmt {
        public RepeatUntilStmt(Stmt body, Condition condition) {
            this.body = body;
            this.condition = condition;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitRepeatUntilStmtStmt(this);
        }

        public final Stmt body;
        public final Condition condition;
    }

    public static class InputStmt extends Stmt {
        public InputStmt(Token name) {
            this.name = name;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitInputStmtStmt(this);
        }

        public final Token name;
    }

    public static class AssignStmt extends Stmt {
        public AssignStmt(Token name, Expr value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignStmtStmt(this);
        }

        public final Token name;
        public final Expr value;
    }

    public static class CallStmt extends Stmt {
        public CallStmt(Token name) {
            this.name = name;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitCallStmtStmt(this);
        }

        public final Token name;
    }

    public static class BeginEndStmt extends Stmt {
        public BeginEndStmt(List<Stmt> statements) {
            this.statements = statements;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBeginEndStmtStmt(this);
        }

        public final List<Stmt> statements;
    }

    public abstract <R> R accept(Visitor<R> visitor);
}
