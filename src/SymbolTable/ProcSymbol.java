package SymbolTable;

public final class ProcSymbol extends Symbol {
    private final int level;
    private final int procNum;
    private int localVarCounter = 0;

    public ProcSymbol(String name, int level, int procNum) {
        super(name);
        this.level = level;
        this.procNum = procNum;
    }

    public int getLevel() {
        return level;
    }

    public int getProcNum() {
        return procNum;
    }
    public int getLocalVarCounter() {
        return localVarCounter;
    }
    public void allocateLocalVar() {
        localVarCounter++;
    }
}
