package SymbolTable;

import Types.Expr;

import java.util.HashMap;
import java.util.Map;

public class Scope {
    Scope parent;
    final Map<String, Symbol> symbols = new HashMap<>();
    int level;
    ProcSymbol owner;

    public Scope(Scope parent) {
        this.parent = parent;
    }
    public Scope(Scope parent, int level, ProcSymbol owner) {
        this.parent = parent;
        this.level = level;
        this.owner = owner;
    }

    public Scope getParent() {
        return parent;
    }

    public void define(Symbol symbol)
    {
        String name = symbol.getName();

        if(symbols.containsKey(name))
        {
            throw new IllegalArgumentException("Symbol " + name + " is already defined in this scope.");
        }

        symbols.put(name, symbol);
    }

    public Symbol resolve(String name){
        Symbol local = symbols.get(name);

        if(local != null) return local;
        return parent != null ? parent.resolve(name) : null;
    }


    public ProcSymbol getOwner() {
        return owner;
    }
    public int getLevel() {
        return level;
    }

}
