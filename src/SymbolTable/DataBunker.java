package SymbolTable;

import Types.Expr;
import Types.ProcDecl;
import Types.Stmt;

import java.util.List;
import java.util.Map;

public class DataBunker {
    public final List<Integer> constPool; // index -> value

    public final Map<Expr.Variable, Symbol> boundVariables;
    public final Map<Expr.Literal, Integer> boundLiterals;

    public final Map<Stmt.AssignStmt, VarSymbol> boundAssignTargets;
    public final Map<Stmt.InputStmt, VarSymbol> boundInputTargets;
    public final Map<Stmt.CallStmt, ProcSymbol> boundCallTargets;

    public final Map<ProcDecl, ProcSymbol> procDeclToSymbol;

    public DataBunker(
            List<Integer> constPool,
            Map<Expr.Variable, Symbol> boundVariables,
            Map<Expr.Literal, Integer> boundLiterals,
            Map<Stmt.AssignStmt, VarSymbol> boundAssignTargets,
            Map<Stmt.InputStmt, VarSymbol> boundInputTargets,
            Map<Stmt.CallStmt, ProcSymbol> boundCallTargets,
            Map<ProcDecl, ProcSymbol> procDeclToSymbol
    ) {
        this.constPool = constPool;
        this.boundVariables = boundVariables;
        this.boundLiterals = boundLiterals;
        this.boundAssignTargets = boundAssignTargets;
        this.boundInputTargets = boundInputTargets;
        this.boundCallTargets = boundCallTargets;
        this.procDeclToSymbol = procDeclToSymbol;
    }

    public Map<ProcDecl, ProcSymbol> getProcDeclToSymbol() {
        return procDeclToSymbol;
    }

    public Map<Stmt.CallStmt, ProcSymbol> getBoundCallTargets() {
        return boundCallTargets;
    }

    public Map<Stmt.InputStmt, VarSymbol> getBoundInputTargets() {
        return boundInputTargets;
    }

    public Map<Stmt.AssignStmt, VarSymbol> getBoundAssignTargets() {
        return boundAssignTargets;
    }

    public Map<Expr.Literal, Integer> getBoundLiterals() {
        return boundLiterals;
    }

    public Map<Expr.Variable, Symbol> getBoundVariables() {
        return boundVariables;
    }










    //DEBUG HELPER//








    public String debugDump() {
        StringBuilder sb = new StringBuilder();

        sb.append("==== DataBunker Dump ====\n");

        // 1) Const Pool
        sb.append("\n-- ConstPool (index -> value) --\n");
        for (int i = 0; i < constPool.size(); i++) {
            sb.append(String.format("[%d] = %d%n", i, constPool.get(i)));
        }

        // 2) Procedures
        sb.append("\n-- Procedures (from procDeclToSymbol) --\n");
        procDeclToSymbol.forEach((_, sym) -> sb.append(String.format(
                "proc name=%s procNum=%d level=%d localVarCount=%d%n",
                sym.getName(),
                sym.getProcNum(),
                sym.getLevel(),
                sym.getLocalVarCounter()
        )));

        // 3) Example bindings overview (optional but super useful)
        sb.append("\n-- Bindings: Assign targets --\n");
        boundAssignTargets.forEach((stmt, varSym) -> sb.append(String.format(
                "assign '%s' -> VarSymbol(name=%s, level=%d, slot=%d)%n",
                stmt.name.lexeme,
                varSym.getName(),
                varSym.getLevel(),
                varSym.getSlot()
        )));

        sb.append("\n-- Bindings: Input targets --\n");
        boundInputTargets.forEach((stmt, varSym) -> sb.append(String.format(
                "input '%s' -> VarSymbol(name=%s, level=%d, slot=%d)%n",
                stmt.name.lexeme,
                varSym.getName(),
                varSym.getLevel(),
                varSym.getSlot()
        )));

        sb.append("\n-- Bindings: Call targets --\n");
        boundCallTargets.forEach((stmt, procSym) -> sb.append(String.format(
                "call '%s' -> ProcSymbol(name=%s, procNum=%d, level=%d)%n",
                stmt.name.lexeme,
                procSym.getName(),
                procSym.getProcNum(),
                procSym.getLevel()
        )));

        sb.append("\n-- Bindings: Variable expressions --\n");
        boundVariables.forEach((exprVar, sym) -> {
            // sym kann VarSymbol oder ConstSymbol sein
            sb.append(String.format(
                    "var-expr '%s' -> %s%n",
                    exprVar.name.lexeme,
                    sym.toString()
            ));
        });

        return sb.toString();
    }

}
