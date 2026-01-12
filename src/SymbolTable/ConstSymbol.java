package SymbolTable;

public final class ConstSymbol extends Symbol {
    private final int level;
    private final int constIndex; //index in the constant table
    private final int value;

    public ConstSymbol(String name, int level, int constIndex, int value) {
        super(name);
        this.level = level;
        this.constIndex = constIndex;
        this.value = value;
    }

    public int getLevel() {
        return level;
    }

    public int getConstIndex() {
        return constIndex;
    }
}
