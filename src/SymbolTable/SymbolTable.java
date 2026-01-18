package SymbolTable;


public class SymbolTable
{
    Scope currentScope;


    public Scope getCurrentScope() {
        return currentScope;
    }

    public void enterScope(int level, ProcSymbol owner){
        currentScope = new Scope(currentScope, level, owner);
    }

    public void exitScope(){
        if(currentScope == null)
        {
            throw new IllegalStateException("No scope to exit!");
        }

        currentScope = currentScope.getParent();
    }

    public void define(Symbol symbol)
    {
        if(currentScope == null)
        {
            throw new IllegalStateException("No scope to define symbol in!");
        }
        currentScope.define(symbol);
    }

    public Symbol resolve(String name)
    {
        // if no scope defined, return null
        if(currentScope == null)
        {
            return null;
        }
        return currentScope.resolve(name);
    }

}
