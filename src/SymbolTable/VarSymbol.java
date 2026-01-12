package SymbolTable;

public final class VarSymbol extends Symbol {
    private final int level;
    private final int slot; // 0,1,2,... (byteoffset slot * 4)

    public VarSymbol(String name, int level, int slot) {
        super(name);
        this.level = level;
        this.slot = slot;
    }

    public int getLevel() {
        return level;
    }

    public int getSlot() {
        return slot;
    }
}
