package Compiler;

import Lexer.Token;
import Lexer.TokenType;
import SymbolTable.*;
import Types.*;

import java.util.ArrayList;
import java.util.List;

public class CodeGenerator {
    List<Byte> bytecode = new ArrayList<>();

    //----------------------------Helper Methods----------------------------//
    public void write1Byte(int b) {
        bytecode.add((byte) (b & 0xFF));
    }

    public void write2Bytes(int b) {
        int v = b & 0xFFFF;
        bytecode.add((byte) (v & 0xFF));         // low
        bytecode.add((byte) ((v >>> 8) & 0xFF)); // high
    }

    public void write4Bytes(int b) {
        bytecode.add((byte) (b & 0xFF));
        bytecode.add((byte) ((b >>> 8) & 0xFF));
        bytecode.add((byte) ((b >>> 16) & 0xFF));
        bytecode.add((byte) ((b >>> 24) & 0xFF));
    }

    public int pos() {
        return bytecode.size();
    }

    public void patch2Bytes(int position, int value) {
        int v = value & 0xFFFF;
        bytecode.set(position, (byte) (v & 0xFF));
        bytecode.set(position + 1, (byte) ((v >>> 8) & 0xFF));
    }

    public byte[] toByteArray() {
        byte[] arr = new byte[bytecode.size()];
        for (int i = 0; i < bytecode.size(); i++) {
            arr[i] = bytecode.get(i);
        }
        return arr;
    }

    private void generateOpCode(Befehle op) {
        write1Byte(op.getCode());
    }

    private void generateOpCode(Befehle op, int... operand) {
        write1Byte(op.getCode());
        for (int o : operand) {
            write2Bytes(o);
        }
    }

    //----------------------------Helper Methods----------------------------//


    private void writeheader(DataBunker d) {
        write2Bytes(1 + d.getProcDeclToSymbol().size());
        System.out.println("Total procs: " + (1 + d.getProcDeclToSymbol().size()));
        write2Bytes(4);
    }

    private void writefooter(DataBunker d) {
        List<Integer> constpool = d.constPool;
        for (int value : constpool) {
            write4Bytes(value);
        }
    }

    private void writeStmt(Stmt s, DataBunker d, int procnum) {
        //TODO implement statement code generation
        switch (s) {

            case Stmt.BeginEndStmt beginEndStmt -> {
                for (Stmt stmt : beginEndStmt.statements) {
                    writeStmt(stmt, d, procnum);
                }
            }

            case Stmt.AssignStmt assignStmt -> {
                VarSymbol target = d.getBoundAssignTargets().get(assignStmt);
                 if (target.getOwnerProcnum() == 0) {
                    generateOpCode(Befehle.PushAddressMainVar, (target.getSlot() * 4));
                    writeExpr(assignStmt.value, d, procnum);
                    generateOpCode(Befehle.StoreValue);

                }
                else if (target.getOwnerProcnum() == procnum) {

                    generateOpCode(Befehle.PushAddressLocalVar, (target.getSlot() * 4));
                    writeExpr(assignStmt.value, d, procnum);
                    generateOpCode(Befehle.StoreValue);


                } else {
                    generateOpCode(Befehle.PushAddressGlobalVar, (target.getSlot() * 4), target.getOwnerProcnum());
                    writeExpr(assignStmt.value, d, procnum);
                    generateOpCode(Befehle.StoreValue);
                }
            }

            case Stmt.CallStmt callStmt ->
                    generateOpCode(Befehle.CallProc, d.getBoundCallTargets().get(callStmt).getProcNum());


            case Stmt.IfStmt ifStmt -> {
                writeCondition(ifStmt.condition, d, procnum);
                int thenstart = pos();
                int patchpos = thenstart + 1;
                int base = thenstart + 3;
                generateOpCode(Befehle.JumpIfFalse, 0); //placeholder
                writeStmt(ifStmt.thenBranch, d, procnum);
                int target = pos();
                int offset = target - base;
                patch2Bytes(patchpos, offset);


            }

            case Stmt.InputStmt inputStmt -> {
                VarSymbol target = d.getBoundInputTargets().get(inputStmt);
                if (target.getOwnerProcnum() == 0) {

                    generateOpCode(Befehle.PushAddressMainVar, (target.getSlot() * 4));
                    generateOpCode(Befehle.InputToAddr);

                }
                else if (target.getOwnerProcnum() == procnum) {

                    generateOpCode(Befehle.PushAddressLocalVar, (target.getSlot() * 4));
                    generateOpCode(Befehle.InputToAddr);

                }
                else {
                    generateOpCode(Befehle.PushAddressGlobalVar, (target.getSlot() * 4), target.getOwnerProcnum());
                    generateOpCode(Befehle.InputToAddr);
                }
            }

            case Stmt.OutputStmt outputStmt -> {
                writeExpr(outputStmt.expression, d, procnum);
                generateOpCode(Befehle.OutputValue);
            }

            case Stmt.WhileStmt whileStmt -> {

                int loopstart = pos();
                writeCondition(whileStmt.condition, d, procnum);
                int exitJump = pos();
                generateOpCode(Befehle.JumpIfFalse,0);
                int exitPatchPos = exitJump + 1;
                int exitbase = exitJump + 3;
                writeStmt(whileStmt.body, d, procnum);
                int backJumpPos = pos();
                int backbase = backJumpPos + 3;
                int backoffset = loopstart - backbase;
                generateOpCode(Befehle.Jump, backoffset);
                int endpos = pos();
                int exitoffset = endpos - exitbase;
                patch2Bytes(exitPatchPos, exitoffset);
            }

            case Stmt.RepeatUntilStmt reapUnStmt -> {

                int target = pos();
                writeStmt(reapUnStmt.body, d, procnum);
                writeCondition(reapUnStmt.condition, d, procnum);
                int jumpPos = pos();
                int base = jumpPos + 3;
                int offsetBack = target - base;
                generateOpCode(Befehle.JumpIfFalse, offsetBack);



            }


            case null, default ->
                //should not happen
                    throw new RuntimeException("Unknown statement type in code generation");
        }
    }

    private void writeExpr(Expr e, DataBunker d, int procnum) {
        switch (e) {

            case Expr.Literal literal -> {
                int constIndex = d.getBoundLiterals().get(literal);
                generateOpCode(Befehle.PushConstant, constIndex);
            }

            case Expr.Variable variable -> {
                Symbol sym = d.getBoundVariables().get(variable);
                if (sym instanceof VarSymbol) {

                    if (((VarSymbol) sym).getOwnerProcnum() == 0) {
                        generateOpCode(Befehle.PushValueMainVar, (((VarSymbol) sym).getSlot() * 4));
                    } else if (((VarSymbol) sym).getOwnerProcnum() == procnum) {
                        generateOpCode(Befehle.PushValueLocalVar, (((VarSymbol) sym).getSlot() * 4));
                    } else {
                        generateOpCode(Befehle.PushValueGlobalVar, (((VarSymbol) sym).getSlot() * 4), ((VarSymbol) sym).getOwnerProcnum());
                    }
                } else if (sym instanceof ConstSymbol) {
                    if (((ConstSymbol) sym).getLevel() == 0) {
                        generateOpCode(Befehle.PushConstant, ((ConstSymbol) sym).getConstIndex());
                    } else {
                        throw new RuntimeException("Local constants not supported");
                    }
                } else {
                    throw new RuntimeException("Unknown variable symbol type in code generation");
                }
            }

            case Expr.Binary binary -> {
                writeExpr(binary.left, d, procnum);
                writeExpr(binary.right, d, procnum);
                Token operator = binary.operator;

                if (operator.type == TokenType.PLUS) {
                    generateOpCode(Befehle.OpAdd);
                } else if (operator.type == TokenType.MINUS) {
                    generateOpCode(Befehle.OpSubtract);
                } else if (operator.type == TokenType.STAR) {
                    generateOpCode(Befehle.OpMultiply);
                } else if (operator.type == TokenType.SLASH) {
                    generateOpCode(Befehle.OpDivide);
                } else {
                    throw new RuntimeException("Unknown binary operator in code generation");
                }
            }

            case Expr.Grouping grouping -> writeExpr(grouping.expression, d, procnum);

            case Expr.Unary unary -> {
                writeExpr(unary.right, d, procnum);
                Token operator = unary.operator;

                if (operator.type == TokenType.MINUS) {
                    generateOpCode(Befehle.Minusify);
                } else {
                    throw new RuntimeException("Unknown unary operator in code generation");
                }
            }

            case null, default ->
                //should not happen
                    throw new RuntimeException("Unknown expression type in code generation");
        }
    }

    private void writeCondition(Condition c, DataBunker d, int procnum) {

        switch(c)
        {
            case Condition.BinaryCondition binaryCondition -> {
                writeExpr(binaryCondition.left, d, procnum);
                writeExpr(binaryCondition.right, d, procnum);
                Token operator = binaryCondition.operator;

                if (operator.type == TokenType.EQUAL) {
                    generateOpCode(Befehle.CompareEq);
                } else if (operator.type == TokenType.NOT_EQUAL) {
                    generateOpCode(Befehle.CompareNotEq);
                } else if (operator.type == TokenType.LESS) {
                    generateOpCode(Befehle.CompareLT);
                } else if (operator.type == TokenType.GREATER) {
                    generateOpCode(Befehle.CompareGT);
                } else if (operator.type == TokenType.LESS_EQUAL) {
                    generateOpCode(Befehle.CompareLTEq);
                } else if (operator.type == TokenType.GREATER_EQUAL) {
                    generateOpCode(Befehle.CompareGTEq);
                } else {
                    throw new RuntimeException("Unknown condition operator in code generation");
                }
            }
            case Condition.UnaryCondition unaryCondition -> {
                writeExpr(unaryCondition.expression, d, procnum);

                generateOpCode(Befehle.IsOdd);
            }

            case null, default ->
                //should not happen
                    throw new RuntimeException("Unknown condition type in code generation");
        }
    }

    private void writeprocs(Program p, DataBunker d) {

        int procstart;
        int sizePos;
        int procend;
        int procsize;

        List<ProcDecl> procs = sortlistbyprocnum(collectProcDecls(p.block), d);
        System.out.println("Procedures to generate: " + collectProcDecls(p.block).size());

        System.out.println("Procedures to generate: " + procs.size());
        for (ProcDecl pd : procs) {
            ProcSymbol ps = d.getProcDeclToSymbol().get(pd);
            procstart = pos();
            int localBytes = ps.getLocalVarCounter() * 4;
            sizePos = writeEntryProc(ps.getProcNum(), localBytes);
            writeStmt(pd.block.statement, d, ps.getProcNum());
            generateOpCode(Befehle.ReturnProc);
            procend = pos();
            procsize = procend - procstart;
            patch2Bytes(sizePos, procsize);

            //debug output of important proc info
            System.out.println("Generated proc: " + ps.getName() +
                    " (procnum=" + ps.getProcNum() +
                    ", level=" + ps.getLevel() +
                    ", localVars=" + ps.getLocalVarCounter() +
                    ", size=" + procsize + " bytes)" +
                    ", procstart=" + procstart +
                    ", procend=" + procend);
        }

        procstart = pos();
        sizePos = writeEntryProc(0, (p.block.varDecls.size() * 4));
        writeStmt(p.block.statement, d, 0);
        generateOpCode(Befehle.ReturnProc);
        procend = pos();
        procsize = procend - procstart;
        patch2Bytes(sizePos, procsize);
        System.out.println("Generated main proc (procnum=0, level=0, localVars=" + p.block.varDecls.size() +
                ", size=" + procsize + " bytes)" +
                ", procstart=" + procstart +
                ", procend=" + procend);
    }

    private List<ProcDecl> sortlistbyprocnum(List<ProcDecl> procs, DataBunker d) {
        List<ProcDecl> sorted = new ArrayList<>();
        List<ProcDecl> unsorted = new ArrayList<>(procs);

        while (!unsorted.isEmpty()) {
            ProcDecl minProc = unsorted.getFirst();
            ProcSymbol minSym = d.getProcDeclToSymbol().get(minProc);
            for (ProcDecl pd : unsorted) {
                ProcSymbol ps = d.getProcDeclToSymbol().get(pd);
                if (ps.getProcNum() > minSym.getProcNum()) {
                    minProc = pd;
                    minSym = ps;
                }
            }
            sorted.add(minProc);
            unsorted.remove(minProc);
        }

        return sorted;
    }

    private List<ProcDecl> collectProcDecls(Block root) {
        List<ProcDecl> procs = new ArrayList<>();
        for (ProcDecl pd : root.procDecls) {
            procs.add(pd);
            procs.addAll(collectProcDecls(pd.block));
        }

        return procs;
    }

    private int writeEntryProc(int procnum, int localBytes) {
        int procstart = pos();
        generateOpCode(Befehle.EntryProc, 0, procnum, localBytes);
        return procstart + 1;
    }

    public byte[] generate(Program p, DataBunker d) {
        bytecode.clear();


        writeheader(d);


        writeprocs(p, d);


        writefooter(d);


        return toByteArray();
    }
}
