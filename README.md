# PL-0-Compiler

Was ist schon "fertig" und was muss noch gemacht werden:

# Fertig:

- Lexer
- Parser (WOHOOOO)

# Muss noch gemacht werden:

- Symboltabelle
- Semantische Analyse
- Code Generation
- Bessere Error Messages

# Was macht der Code aktuell (kurzer Programmablauf):

1. Es gibt ein File, geschrieben in PL/0, welches der Lexer als Input bekommt.
2. Der Lexer liest dieses File und generiert aus dem gelesenen Code Tokens.
3. Diese Tokens werden dem Parser übergeben.
4. Der Parser liest die Tokens und generiert einen Abstract Syntax Tree (AST) für Expressions und Conditions (später auch Statements).
5. Der AST kann dann für die Code Generation verwendet werden (noch nicht implementiert).


# Aktuelle Probleme:

- Symboltabelle muss noch implementiert werden
- Die Code Generation ist noch nicht implementiert. 
    - Das Problem da liegt im allgemeinen Verständnis von der Code Generation für einen PL/0 Compiler.
    - Das Layout ist bisschen weirde, muss sich nochmal angeschaut werden.
    - Die anzuzeigenen Daten müssen noch irgendwo hergeholt werden. 
- Fehler Handling fehlt


# Plan für nächstes mal:

Mit dem Thema Symboltabelle auseinander setzten und diese implementieren

Scopes schonmal anschauen 

so langsam an das code generieren rantasten

ERROR MESSAGES **SCHÖNER** MACHEN