# PL-0-Compiler

Was ist schon "fertig" und was muss noch gemacht werden:

# Fertig:

- Lexer
- Parser für Expressions und Conditions

# Muss noch gemacht werden:

- Parser für Statements
- Code Generation

# Was macht der Code aktuell (kurzer Programmablauf):

1. Es gibt ein File, geschrieben in PL/0, welches der Lexer als Input bekommt.
2. Der Lexer liest dieses File und generiert aus dem gelesenen Code Tokens.
3. Diese Tokens werden dem Parser übergeben.
4. Der Parser liest die Tokens und generiert einen Abstract Syntax Tree (AST) für Expressions und Conditions (später auch Statements).
5. Der AST kann dann für die Code Generation verwendet werden (noch nicht implementiert).


# Aktuelle Probleme:

- Der Parser ist noch nicht vollständig implementiert, es fehlen noch die Regeln für Statements.
- Die Code Generation ist noch nicht implementiert. 
    - Das Problem da liegt im allgemeinen Verständnis von der Code Generation für einen PL/0 Compiler.
    - Das Layout ist bisschen weirde, muss sich nochmal angeschaut werden.
    - Die anzuzeigenen Daten müssen noch irgendwo hergeholt werden. 
-Fehler Handling fehlt


# Plan für nächstes mal:

Jetzt wo Lexer und Parser einen stand erreicht haben womit man schonmal arbeiten "könnte", ist es gut sich einen Überblick zu verschaffen, was in der Code Generation gemacht werden muss.

Das heißt welche Informationen zu den Befehlen brauch ich?
Was ist das Layout, bzw wie baue ich das Layout?
Welche Daten müssen angezeigt werden?

Das führt am ende wahrscheinlich dazu das im Parser nochmal dinge geändert werden, das heißt hinzugefügt oder entfernt.