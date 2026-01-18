package SymbolTable;

public final class ConstSymbol extends Symbol {
    private final int level;
    private final int constIndex; //index in the constant table

    public ConstSymbol(String name, int level, int constIndex) {
        super(name);
        this.level = level;
        this.constIndex = constIndex;
    }

    public int getLevel() {
        return level;
    }

    public int getConstIndex() {
        return constIndex;
    }
}
