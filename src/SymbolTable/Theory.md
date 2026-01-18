-------------------- 

#### Dieses File soll die Kommentare ein wenig ersetzten in der Symboltabelle/SemanticTraveler!

--------------------

Ich würde gerne etwas näher auf die Funktionsweise des SemanticTravelers eingehen da dies der wichtigste Part ist.
Der Rest sind Klassen die wichtige Objekte verkörpern.

Der SemanticTraveler ist die Implementation der visit... Methoden, das heißt, wenn wir den Baum ablaufen, "besuchen" wir verschiedene Nodes
je nach dem auf welcher wir sind, rufen wir die visit Methoden auf.

Jedoch fällt etwas auf.

Es gibt die visitBinaryExpr implementation:

````Java
@Override
public Void visitBinaryExpr(Expr.Binary expr) {
   expr.left.accept(this);
   expr.right.accept(this);
   return null;
}
````
Und was genau macht das jetzt, die Expr, die die visit Methode ruft, ruft von die Linke Expression die accept und von der Rechten Expression die accept Methode auf
Je nach dem was das für eine Expression ist, wird dann wieder die accept methode der Expression gerufen, und das hangelt sich immer weiter bis es zu Literal oder Grouping Expression kommt.

Das ist bisschen Kompliziert zu erklären aber die Idee ist, da der Semantic Traveler nicht von jeder visit Methode etwas braucht
reduziert man sich auf die Methoden die wichtig sind, und der rest ruft einfach die accept Methoden auf, dadurch endet man irgendwann an einem Punkt wo der Value der Expression in einer Liste gespeichert wird.

Bei Expressions ist das literalExpression für Konstanten und variableExpr für Variablen. 

Also... Fazit: Die Idee in dieser Implementation des Visitor Patterns ist das es "Rekursiv" die accept Methoden ruft bis es auf die "Relevanten" Visit Methoden trifft die dann wiederum die Informationen
für die Symboltabelle enthalten. Dafür wurde jetzt auch ein Output eingebaut der zeigt in welcher Reihenfolge die Visit Methoden gerufen werden.


Ich glaube das waren so die unverständlichsten Punkte in diesem Programm und ich hoffe ich konnte sie hiermit erklären.


Alles was hier jetzt folgt sind "Notes" die ich während des Programmierens mitgeschrieben habe.

-------------------------------
# Was macht eine Symboltabelle in einem Compiler?

Sagen wir du bist der Compiler und schaust in den AST und siehst dort ein `x`. Die Symboltabelle sagt dir:
- was ist das?
- Konstante? Variable? Prozedur?
- wo liegt es zur Laufzeit (Level/Scope + Adresse/Slot/Displacement)?

Für die Codegenerierung ist das wichtig, weil Befehle wie `pushAdrVarLocal` / `pusValVarLocal` (bzw. deine Wrapper-Namen) Operanden erwarten,
und diese Operanden kommen aus der Symboltabelle.

# PL/0 spezifisch

Es ist für die nächsten Schritte sehr wichtig, diese Symboltabelle zu haben, weil sie der springende Punkt für korrekten Bytecode ist.

Es ist im entfernten Sinne wie die Environments aus der Lox-Sprache (Crafting Interpreters),
abzüglich dessen, dass wir hier nicht "Name -> Wert" speichern, sondern "Name -> Speicherort/Metadaten".

Was ist speziell wichtig pro Symbol?
- Kategorie: CONST / VAR / PROC
- Name
- Scope/Level (Prozedurebene / Verschachtelung)
- für VAR: Adresse/Slot/Displacement
- für CONST: Wert (oder ein ConstPool-Index, falls du Konstanten am Ende sammelst)
- für PROC: ProcId (+ später Code-Startadresse/Size)


PL/0 erlaubt Variablen in Prozeduren und wiederum Prozeduren **in** Prozeduren.
Das führt dazu, dass du verschachtelte Scopes brauchst (Scope-Kette / Stack von Scopes).

# Speziell in dieser Implementierung

1. Es wird ein **zweifacher** Durchlauf verwendet.
   1) Pass 1 (Analyse/Symbolaufbau):
      - erstellt Scopes
      - vergibt Var-Slots
      - vergibt ProcIds
      - speichert Const-Werte
      - prüft einfache Fehler: „doppelt deklariert“, „benutzt aber nicht deklariert“ 

   2) Pass 2 (Codegenerierung):
      - nutzt die Symboltabelle, um Operanden für Variablen-/Prozedur-/Konstantenbefehle zu erzeugen
      - besonders wichtig für Prozeduren, Variablenzugriffe und Kontrollstrukturen

2. Dafür sind sinnvoll:
   - `Symbol` (Daten über CONST/VAR/PROC)
   - `Scope` (Map Name -> Symbol + Parent)
   - optional `ProcedureInfo` (z.B. procId, level, varCount, später codeStart/codeLen)

3. Wenn du Prozeduren verarbeitest:
   - lege ein ProcSymbol im aktuellen Scope an
   - betrete neuen Scope (Parent = alter Scope)
   - zähle/allokiere Variablen (Slots)
   - verlasse den Scope am Ende wieder


# Rules für unsere Symboltabelle

1. Jeder Block = neuer Scope
2. Keine Duplicates
3. Suche geht vom aktuellen scope bis ins globale scope
4. Innere Scopes dürfen Namen von äußeren Variablen überdecken
5. Assign/Input nur auf var
6. Call nur für procedures

Die symboltabelle muss mir sagen in welcher proc/in welchem Scope die variable definiert ist und welchen offset sie von dem Scope wo sie gerufen wird hat zu dem scop wo sie definiert wird
adresse ist das einzige wichtige und die setzt sich aus procnum und offset in varliste(im eigenen Scope)

Consts haben Liste(alle uniquen zahlen) und Hashmap(nur für selber definierte)


---------------------------------------------

# Welche Symbole

- ConstSymbol(name, value, constIndex, level)
- VarSymbol(name, slot, level, ownerProcNum)
- ProcSymbol(name, procNum, level, localVarCount)


# Welche "Fragen" muss ich stellen können

- defineConst(name, value)
- defineVar(name)
- defineProc(name)
- resolve(name) sucht im aktuellen scope, sonst parent weil shadowing ok ist

-> Generator soll später nichtmehr suchen sondern direkt info zu Symbol bekommen

# Scope

Jeder Block neuer Scope -> neue Procedure erzeugt neuen Scope in für ihren Block und wird Owner der Procedure

Level beschreibt dabei die Tiefe des Scopes (global = 0, main = 1, local = 2)


# Nummerierung

mainproc hat procnum 0 (immer)
jede danach definierte Procedure bekommt eindeutige Nummer >0

Variablen haben pro procedure eine "Liste" in welcher der Index auschlaggebend ist an welcher stelle die Variable steht
slot 0 = erste var, usw...
localVarCounter ist dann der Zähler für alles Variablen


# Constants

Sollen in einer gemeinsamen Map gestored werden mit dem Value und dem Index als information. 
Da Konstanten einfach am ende des bytecodes aufgezählt werden, ermöglicht es mir dann, 

```pseudo
pool[index] (in 4 bytes, little endian)
```

Map für definieren, also Map<Integer, Integer> valueToIndex

und Liste für Bytecode, List<Integer> valuesByIndex

```pseudo
Start: nextIndex = 0, valueToIndex = {}, valuesByIndex = []
7 neu → index 0
valueToIndex[7]=0, valuesByIndex=[7]
1 neu → index 1
valueToIndex[1]=1, valuesByIndex=[7,1]
7 schon da → index 0
valuesByIndex bleibt [7,1]
42 neu → index 2
valuesByIndex=[7,1,42]
```


--------------------------------------

# Wann neuer Scope

Program hat einen Block der wiederum der Globale Scope ist.

Jede neue Procedure hat dann einen eigenen Block => eigenen Scope
=> **Ein Scope pro Bock**


# Level

Kann man sich wie eine Ordner Struktur vorstellen, main ist der root ordner mit level 0

die Ordner im Root ordner haben dann jeweils eine eigene, eindeutige Zahl > 0.

Level steigt also mit jeder Prozedur verschachtelung. 

Wenn jedoch 3 prozeduren hinter einander stehen, also 

```PL/0
procedure a
...
begin
end
procedure b
...
begin
end
procedure c
...
begin
end
```

dann haben alle grundsätzlich das level 1, da ich ja in procedure a nicht in procedure b schauen würde ob da irgendwas definiert ist
ich gehe ja beim traversieren der scopes immer ein scope höher und das höhere scope ist der root ordner


# Owner

Jeder scope hat einen Owner (also jeder inhalt eines Ordners, hat einen Ort, in dem der Inhalt gelagert wird)

Das ist vorallem wichtig wenn eine Variable definiert wird, dann muss verfügbar sein in welcher proc das passiert das die variablen mit dem scope der proc verbunden wird
und der jeweilige varcounter hochgezählt wird


# Parent

Ist ähnlich wie der Owner, nur das man sich das vorstellen kann wie

root ist parent von User ordner sozusagen, das heißt wenn man die kette hochläuft geht man von 

... -> ... -> User -> root (beispielhaft)

wobei die "..." die child scopes sind


natürlich hat dementsprechen der root keinen Parent
