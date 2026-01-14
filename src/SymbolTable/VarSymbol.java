package SymbolTable;

public final class VarSymbol extends Symbol {
    private final int level;
    private final int slot; // 0,1,2,... (byteoffset slot * 4)
    private final int ownerProcnum;

    public VarSymbol(String name, int level, int slot, int ownerProcnum) {
        super(name);
        this.level = level;
        this.slot = slot;
        this.ownerProcnum = ownerProcnum;
    }

    public int getLevel() {
        return level;
    }

    public int getSlot() {
        return slot;
    }

    public int getOwnerProcnum() {
        return ownerProcnum;
    }
}
