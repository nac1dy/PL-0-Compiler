# Was macht eine Symboltabelle in einem Compiler?

Sagen wir du bist der Compiler und schaust in den AST und siehst dort ein `x`. Die Symboltabelle sagt dir:
- was ist das?
- Konstante? Variable? Prozedur?
- wo liegt es zur Laufzeit (Level/Scope + Adresse/Slot/Displacement)?

Für die Codegenerierung ist das wichtig, weil Befehle wie `pushAdrVarLocal` / `pusValVarLocal` (bzw. deine Wrapper-Namen) Operanden erwarten,
und diese Operanden kommen aus der Symboltabelle.

Ergänzung: Eine Symboltabelle ist typischerweise **kein** Laufzeit-Speicher wie beim Interpreter.
Sie enthält Metadaten, damit der Compiler Bytecode erzeugen kann.

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

Korrektur: Eine Variable braucht in der Symboltabelle normalerweise **keinen Wert**.
Der Wert existiert erst zur Laufzeit in der VM (Stack/Activation Record). Der Compiler muss nur wissen, **wo** er liegt.

Ergänzung: Oft ist es praktisch, zwischen
- `slot` (0,1,2,...) und
- `byteOffset` (= slot * 4, wenn ein int 4 Byte ist)
zu unterscheiden. Welche Form du speicherst, hängt davon ab, was die VM-Befehle als Operand wollen.

PL/0 erlaubt Variablen in Prozeduren und wiederum Prozeduren **in** Prozeduren.
Das führt dazu, dass du verschachtelte Scopes brauchst (Scope-Kette / Stack von Scopes).

Korrektur: "Local / Global / Main" ist eher ein VM-Blick.
Sprachlich hast du eine **Scope-Kette** (aktueller Scope + Parent + Parent ...).
Ob ein Zugriff am Ende als `Local`/`Main`/`Global` kodiert wird, entscheidest du beim Codegen anhand des gefundenen Symbols.

# Speziell in dieser Implementierung

1. Es wird ein **zweifacher** Durchlauf verwendet.
   1) Pass 1 (Analyse/Symbolaufbau):
      - erstellt Scopes
      - vergibt Var-Slots
      - vergibt ProcIds
      - speichert Const-Werte
      - prüft einfache Fehler: „doppelt deklariert“, „benutzt aber nicht deklariert“

2. Pass 2 (Codegenerierung):
   - nutzt die Symboltabelle, um Operanden für Variablen-/Prozedur-/Konstantenbefehle zu erzeugen
   - besonders wichtig für Prozeduren, Variablenzugriffe und Kontrollstrukturen

Ergänzung: Kontrollstrukturen (if/while/repeat) brauchen zusätzlich meist **Backpatching**:
Du emittest erst einen Jump mit Platzhalter-Offset und trägst den echten Relativ-Offset später ein.
Das ist nicht Symboltabelle, aber hängt eng mit Codegen zusammen.

3. Dafür sind sinnvoll:
   - `Symbol` (Daten über CONST/VAR/PROC)
   - `Scope` (Map Name -> Symbol + Parent)
   - optional `ProcedureInfo` (z.B. procId, level, varCount, später codeStart/codeLen)

4. Wenn du Prozeduren verarbeitest:
   - lege ein ProcSymbol im aktuellen Scope an
   - betrete neuen Scope (Parent = alter Scope)
   - zähle/allokiere Variablen (Slots)
   - verlasse den Scope am Ende wieder
