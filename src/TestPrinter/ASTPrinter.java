package TestPrinter;

import Types.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Debug-Printer für den AST.
 *
 * Ausgabeform: ASCII-Tree (Äste).
 */
public class ASTPrinter implements Expr.Visitor<ASTPrinter.Node>, Condition.Visitor<ASTPrinter.Node>, Stmt.Visitor<ASTPrinter.Node> {


    public String print(Program program) {
        if (program == null) return "<null program>";
        return render(programNode(program));
    }

    public Node programNode(Program program) {
        return node("Program", List.of(blockNode(program.block)));
    }

    public Node blockNode(Block block) {
        if (block == null) return node("<null block>");

        List<Node> kids = new ArrayList<>();

        if (block.constDecls != null && !block.constDecls.isEmpty()) {
            List<Node> constKids = new ArrayList<>();
            for (ConstDecl c : block.constDecls) {
                constKids.add(node(c.name.lexeme + " = " + c.value));
            }
            kids.add(node("const", constKids));
        }

        if (block.varDecls != null && !block.varDecls.isEmpty()) {
            List<Node> varKids = new ArrayList<>();
            for (VarDecl v : block.varDecls) {
                varKids.add(node(v.name.lexeme));
            }
            kids.add(node("var", varKids));
        }

        if (block.procDecls != null && !block.procDecls.isEmpty()) {
            List<Node> procKids = new ArrayList<>();
            for (ProcDecl p : block.procDecls) {
                procKids.add(node("procedure " + p.name.lexeme, List.of(blockNode(p.block))));
            }
            kids.add(node("procedures", procKids));
        }

        kids.add(node("statement", List.of(stmtNode(block.statement))));

        return node("Block", kids);
    }

    public Node stmtNode(Stmt stmt) {
        if (stmt == null) return node("<null stmt>");
        return stmt.accept(this);
    }

    public Node exprNode(Expr expr) {
        if (expr == null) return node("<null expr>");
        return expr.accept(this);
    }

    public Node condNode(Condition cond) {
        if (cond == null) return node("<null condition>");
        return cond.accept(this);
    }

    // ----------------- Stmt Visitor -----------------

    @Override
    public Node visitOutputStmtStmt(Stmt.OutputStmt stmt) {
        return node("output", List.of(node("expr", List.of(exprNode(stmt.expression)))));
    }

    @Override
    public Node visitIfStmtStmt(Stmt.IfStmt stmt) {
        return node("if",
                List.of(
                        node("condition", List.of(condNode(stmt.condition))),
                        node("then", List.of(stmtNode(stmt.thenBranch)))
                ));
    }

    @Override
    public Node visitWhileStmtStmt(Stmt.WhileStmt stmt) {
        return node("while",
                List.of(
                        node("condition", List.of(condNode(stmt.condition))),
                        node("do", List.of(stmtNode(stmt.body)))
                ));
    }

    @Override
    public Node visitRepeatUntilStmtStmt(Stmt.RepeatUntilStmt stmt) {
        return node("repeat",
                List.of(
                        node("body", List.of(stmtNode(stmt.body))),
                        node("until", List.of(condNode(stmt.condition)))
                ));
    }

    @Override
    public Node visitInputStmtStmt(Stmt.InputStmt stmt) {
        return node("input", List.of(node(stmt.name.lexeme)));
    }

    @Override
    public Node visitAssignStmtStmt(Stmt.AssignStmt stmt) {
        return node("assign",
                List.of(
                        node("name", List.of(node(stmt.name.lexeme))),
                        node("value", List.of(exprNode(stmt.value)))
                ));
    }

    @Override
    public Node visitCallStmtStmt(Stmt.CallStmt stmt) {
        return node("call", List.of(node(stmt.name.lexeme)));
    }

    @Override
    public Node visitBeginEndStmtStmt(Stmt.BeginEndStmt stmt) {
        List<Node> kids = new ArrayList<>();
        if (stmt.statements != null) {
            for (Stmt s : stmt.statements) {
                kids.add(stmtNode(s));
            }
        }
        return node("begin", kids);
    }

    // ----------------- Expr Visitor -----------------

    @Override
    public Node visitBinaryExpr(Expr.Binary expr) {
        return node("binary '" + expr.operator.lexeme + "'",
                List.of(
                        node("left", List.of(exprNode(expr.left))),
                        node("right", List.of(exprNode(expr.right)))
                ));
    }

    @Override
    public Node visitGroupingExpr(Expr.Grouping expr) {
        return node("group", List.of(exprNode(expr.expression)));
    }

    @Override
    public Node visitLiteralExpr(Expr.Literal expr) {
        return node("literal " + expr.value);
    }

    @Override
    public Node visitUnaryExpr(Expr.Unary expr) {
        return node("unary '" + expr.operator.lexeme + "'", List.of(exprNode(expr.right)));
    }

    @Override
    public Node visitVariableExpr(Expr.Variable expr) {
        return node("varref " + expr.name.lexeme);
    }

    // ----------------- Condition Visitor -----------------

    @Override
    public Node visitUnaryConditionCondition(Condition.UnaryCondition condition) {
        return node("cond '" + condition.operator.lexeme + "'", List.of(exprNode(condition.expression)));
    }

    @Override
    public Node visitBinaryConditionCondition(Condition.BinaryCondition condition) {
        return node("cond '" + condition.operator.lexeme + "'",
                List.of(
                        node("left", List.of(exprNode(condition.left))),
                        node("right", List.of(exprNode(condition.right)))
                ));
    }

    // ----------------- Tree primitives -----------------

    public static final class Node {
        public final String label;
        public final List<Node> children;

        private Node(String label, List<Node> children) {
            this.label = label;
            this.children = children;
        }
    }

    private static Node node(String label) {
        return new Node(label, List.of());
    }

    private static Node node(String label, List<Node> children) {
        return new Node(label, children == null ? List.of() : children);
    }

    private static String render(Node root) {
        StringBuilder sb = new StringBuilder();
        sb.append(root.label).append('\n');
        for (int i = 0; i < root.children.size(); i++) {
            boolean childLast = (i == root.children.size() - 1);
            renderInto(sb, root.children.get(i), "", childLast);
        }
        return sb.toString();
    }

    private static void renderInto(StringBuilder sb, Node node, String prefix, boolean isLast) {
        sb.append(prefix)
          .append(isLast ? "└── " : "├── ")
          .append(node.label)
          .append('\n');

        String childPrefix = prefix + (isLast ? "    " : "│   ");
        for (int i = 0; i < node.children.size(); i++) {
            boolean childLast = (i == node.children.size() - 1);
            renderInto(sb, node.children.get(i), childPrefix, childLast);
        }
    }
}
