package SymbolTable;

import Types.*;
import java.util.*;

public class SemanticTraveler implements Expr.Visitor<Void>, Stmt.Visitor<Void>, Condition.Visitor<Void> {
    private final SymbolTable s;
    private final Map<Expr.Variable, Symbol> boundVariables = new HashMap<>();
    private final Map<Stmt.AssignStmt, VarSymbol> boundAssignTargets = new HashMap<>();
    private final Map<Stmt.CallStmt, ProcSymbol> boundCallTargets = new HashMap<>();
    private final Map<Stmt.InputStmt, VarSymbol> boundInputTargets = new HashMap<>();
    private final Map<Expr.Literal, Integer> boundLiterals = new HashMap<>();
    private final Map<ProcDecl, ProcSymbol> procDeclToSymbol = new HashMap<>();

    private Map<Integer, Integer> valueToIndex = new HashMap<>();
    private List<Integer> indexToValue = new ArrayList<>();

    private ProcSymbol currentProc; // ownerProc context
    int procnum = 0;

    public SemanticTraveler(SymbolTable symbols) {
        this.s = symbols;
    }

    public DataBunker analyze(Program program) {
        // 1) create synthetic main proc symbol (procNum=0, level=0)
        ProcSymbol mainproc = new ProcSymbol("main", 0, procnum);
        procnum++;
        s.enterScope(0, null);
        // 2) enter main scope (ownerProc = main)
        s.define(mainproc);
        currentProc = mainproc;
        // 3) analyzeBlock(program.block)
        analyzeBlock(program.block);
        // 4) exit scope
        s.exitScope();

        return new DataBunker(indexToValue,
                boundVariables,
                boundLiterals,
                boundAssignTargets,
                boundInputTargets,
                boundCallTargets,
                procDeclToSymbol);
    }

    private void analyzeBlock(Block block) {
        // order:
        // constDecls
        for(ConstDecl constDecl : block.constDecls)
        {
            String name = constDecl.name.lexeme;
            int value = constDecl.value;
            int level = s.getCurrentScope().getLevel();
            int constIndex = getOrCreateConstIndex(value);
            s.define(new ConstSymbol(name, level, constIndex, value));
        }
        // varDecls
        for(VarDecl varDecl : block.varDecls)
        {
            String name = varDecl.name.lexeme;
            int level = s.getCurrentScope().getLevel();
            int slot = allocatVarSlot(currentProc);
            s.define(new VarSymbol(name, level, slot, currentProc.getProcNum()));
        }
        // procDecl heads
        for(ProcDecl procDecl : block.procDecls)
        {
            String name = procDecl.name.lexeme;
            int level = s.getCurrentScope().getLevel() + 1;
            ProcSymbol procSym = new ProcSymbol(name, level, procnum);
            procnum++;
            s.define(procSym);
            procDeclToSymbol.put(procDecl, procSym);
        }
        // procDecl bodies
        for(ProcDecl procDecl : block.procDecls)
        {
            ProcSymbol procSym = procDeclToSymbol.get(procDecl);
            analyzeProcDecl(procDecl, procSym);
        }

        // statement
        block.statement.accept(this);
    }

    private int getOrCreateConstIndex(int value)
    {
        if(valueToIndex.containsKey(value))
        {
            return valueToIndex.get(value);
        }
        else
        {
            int index = indexToValue.size();
            indexToValue.add(value);
            valueToIndex.put(value, index);
            return index;
        }
    }

    private int allocatVarSlot(ProcSymbol procSym)
    {
        int slot = procSym.getLocalVarCounter();
        procSym.allocateLocalVar();
        return slot;
    }


    private void analyzeProcDecl(ProcDecl procDecl, ProcSymbol procSym) {
        // set currentProc = procSym
        ProcSymbol savedProc = currentProc;
        // enter scope (level = parent.level+1, ownerProc=procSym)
        currentProc = procSym;
        s.enterScope(procSym.getLevel(), procSym);
        // analyzeBlock(procDecl.block)
        analyzeBlock(procDecl.block);
        // exit scope
        s.exitScope();
        // restore currentProc
        currentProc = savedProc;
    }

    // Visitor overrides for Stmt/Expr/Condition below...

    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        expr.left.accept(this);
        expr.right.accept(this);
        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        expr.expression.accept(this);
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) return null;

        if (expr.value instanceof Integer i) {
            int constIndex = getOrCreateConstIndex(i);
            boundLiterals.put(expr, constIndex);
            return null;
        }
        else {
            throw new IllegalArgumentException("Only integer literals are supported.");
        }
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        expr.right.accept(this);
        return null;
    }

    @Override
    public Void visitVariableExpr(Expr.Variable expr)
    {
        if(s.resolve(expr.name.lexeme) == null || s.resolve(expr.name.lexeme) instanceof ProcSymbol)
        {
            throw new IllegalArgumentException("Undefined variable: " + expr.name.lexeme + "does not name a value (probably procedure).");
        }
        else{
            Symbol sym = s.resolve(expr.name.lexeme);
            boundVariables.put(expr, sym);
        }
        return null;
    }

    @Override
    public Void visitUnaryConditionCondition(Condition.UnaryCondition condition) {
        condition.expression.accept(this);
        return null;
    }

    @Override
    public Void visitBinaryConditionCondition(Condition.BinaryCondition condition) {
        condition.left.accept(this);
        condition.right.accept(this);
        return null;
    }

    @Override
    public Void visitOutputStmtStmt(Stmt.OutputStmt stmt) {
        stmt.expression.accept(this);
        return null;
    }

    @Override
    public Void visitIfStmtStmt(Stmt.IfStmt stmt) {
        stmt.condition.accept(this);
        stmt.thenBranch.accept(this);
        return null;
    }

    @Override
    public Void visitWhileStmtStmt(Stmt.WhileStmt stmt) {
        stmt.condition.accept(this);
        stmt.body.accept(this);
        return null;
    }

    @Override
    public Void visitRepeatUntilStmtStmt(Stmt.RepeatUntilStmt stmt) {
        stmt.body.accept(this);
        stmt.condition.accept(this);
        return null;
    }

    @Override
    public Void visitInputStmtStmt(Stmt.InputStmt stmt) {
        if(s.resolve(stmt.name.lexeme) == null || !(s.resolve(stmt.name.lexeme) instanceof VarSymbol))
        {
            throw new IllegalArgumentException("Undefined variable: " + stmt.name.lexeme + " is not a variable.");
        }
        else{
            Symbol sym = s.resolve(stmt.name.lexeme);
            boundInputTargets.put(stmt, (VarSymbol) sym);
        }
        return null;
    }

    @Override
    public Void visitAssignStmtStmt(Stmt.AssignStmt stmt) {
        if(s.resolve(stmt.name.lexeme) == null || !(s.resolve(stmt.name.lexeme) instanceof VarSymbol))
        {
            throw new IllegalArgumentException("Undefined variable: " + stmt.name.lexeme + " is not a variable.");
        }
        else{
            Symbol sym = s.resolve(stmt.name.lexeme);
            boundAssignTargets.put(stmt, (VarSymbol) sym);
            stmt.value.accept(this);
        }
        return null;
    }

    @Override
    public Void visitCallStmtStmt(Stmt.CallStmt stmt) {
        if(s.resolve(stmt.name.lexeme) == null || !(s.resolve(stmt.name.lexeme) instanceof ProcSymbol))
        {
            throw new IllegalArgumentException("Undefined procedure: " + stmt.name.lexeme + " is not a procedure.");
        }
        else{
            Symbol sym = s.resolve(stmt.name.lexeme);
            boundCallTargets.put(stmt, (ProcSymbol) sym);
        }
        return null;
    }

    @Override
    public Void visitBeginEndStmtStmt(Stmt.BeginEndStmt stmt) {
        for(Stmt statement : stmt.statements)
        {
            statement.accept(this);
        }
        return null;
    }
}
