-------------------- 

#### Dieses File soll die Kommentare ein wenig ersetzten im Codegenerator!

--------------------

# Layout

Opcodes = 1 Byte

Parameter = 2 Byte (short)

Werte in little endian 0004 -> 0400

## Header
- anzahl procedures (2 byte) 
- dann fest 04 00 (32 Bit adressraum)
- und dann wieder fest folgt 1a für entryproc was der start des programmes ist basically
  - und dazu kommen die informationen:
  - größe der procedure
  - nummer der procedure (main immer 0000, das heißt erste proc 0001)
  - speicherplatz für alle n Variablen (1 variable = 4 byte => n * 4)

## Ende
- alle konstanten in 4 byte

# Beispiel

```PL/0
!5
```

entstehender Bytecode:

01 00 04 00 1a 0c 00 00 00 00 00 06 00 00 08 17 05 00 00 00 

|----------------------||----------------------||----------|
        Header                  Code              Constlist


# Implementation

Der Aufbau ist relativ simpel, man startet mit dem Header welcher aus dem Adressraum (32Bit) besteht und der Anzahl Prozeduren 

Diese werden relativ simpel durch die methode write header geschrieben und die Anzahl Prozeduren steckt ja in der ProcDecl Liste

Dann werden alle Procedure Declarations aus der Liste geholt, diese werden dann absteigend sortiert um von innen nach außen die Procedures auszuwerten

### Warum eigentlich von Innen nach Außen?

------
Weil Informationen der Inneren Procedure den äußeren Bekannt sein muss, wenn man aufsteigend Sortieren und somit von außen nach innen Arbeitet (Hier geht es nur um die Codegen, nich das ausführen)
dann würde die Main die Informationen der Inneren Proc nicht kennen wenn sie diese Aufrufen wöllte. Das heißt bevor die Main, eine Funktion aufrufen kann, muss sie bereits bekannt sein.

Ausgeführt wird am ende von Außen nach Innen natürlich, aber das geht nur wenn die Äußersten Schichten Informationen über die Innersten haben

--------
und für jede Procedure Declaration wird dann eine neue Procedure declariert und einmal durchgelaufen und je nach dem was man matched, 
also Statement, Expression oder Condition, wird dann die dementsprechende Methode dafür gerufen

Dabei ist wichtig anzumerken das auch wenn von innen nach außen gearbeitet wird, die Variablen trotzdem wissen in welchem scope sie definiert sind 
durch die ownderprocnum und das Level welches in VarSymbol enthalten ist

Wie genau der Code generiert wird ist relativ einfach zu verstehen bis auf Conditional-Statements und Loops weil diese einen Jump benötigen

# Jumps

vor       : Positiv

zurück    : Negativ

Für Jumps ist es sich wichtig die Position zu merken bei der die Condition ausgewertet wird, egal ob war oder nicht wird dann ein JumpIfFalse generiert:
#### Warum?
- Ermöglicht effizienteres Auswerten
- Es wäre sonst nötig einmal den gesamten Block zu laden
  - so ist es möglich die Condition auszuwerten und den then block zu ignorieren wenn false, ansonsten wird er ausgeführt
- Wenn die Condition true ist, muss man den Jump-Offset berechnen, das ist wichtig loops und weil Sprünge relativ sind
- Außerdem kann man dann relativ simpel die stelle wo man den Platzhalter eingefügt hat wieder patchen wenn man sich die Position merkt
#### If
- Man berechnet die Base nach der Condition in dem man
  - pos() + 3 rechnet wobei pos() die aktuelle position in der ByteListe ist und +3 um opcode + Operand mit einzuberechnen#
   
  
```Pseudo
writeCondition(...)
jumpPos = pos() 
write JumpIfFalse 0
writeStmt(thenBranch)
target = pos()  (Ende des then-Blocks)
base = jumpPos + 3
offset = target - base
patchen
```

#### While
- Schwieriger weil man immer wieder zurück springt wenn die Condition true ist


Sprung nach vorne:
- direkt nach der Condition
- Target (wo wollen wir hinspringen) ist loop-ende

```Pseudo
exitJumpPos = Position des JumpIfFalse opcodes
exitBase = exitJumpPos + 3
exitTarget = endpos
exitOffset = exitTarget - exitBase (positiv)
```

Sprung zurück:
- Ende vom Body
- Target (wo wollen wir hinspringen) ist loop-Start
```Pseudo
backJumpPos = Position des Jump opcodes (vor schreiben des Jump Codes)
backBase = backJumpPos + 3
backTarget = loopStart
backOffset = backTarget - backBase (negativ)
```


#### Repeat Until (Zusatz)

- Eine Schleife ähnlich wie do-While nur das sie solange läuft bis das Statement True ist
- Sie läuft solange die Condition False ist und sobald sie True ist endet die Schleife

- Wenn also die Condition False ist jumpen wir an den start zurück
```Pseudo
jumpPos = pos() vor dem Schreiben von JumpIfFalse
base = jumpPos + 3
target = loopStart
offsetBack = target - base (negativ)
```


------------

Ich meinte zwar das die Code Gen "einfach" zu verstehen ist, dennoch würde ich gerne 1-2 Worte dazu verlieren. 
1. Mein Kumpel hatte mich noch darauf hingewiesen das es durchaus möglich ist die Code Gen, wenn man schon ein funktionierendes Visitor Pattern hat, auch durch das Visitor Pattern realisierbar ist.
2. Die Codegen basiert darauf das man wieder die jeweiligen Statements, Conditions und Expressions Matched und je dementsprechend den Code generiert. Dabei kann man sich entweder selber OpCode bauen oder wie in diesem Beispiel 
   einer vorgefertigten VM folgen welche spezifischen OpCode fordert. Das war es dann aber tatsächlich wirklich, bis auf die Jumps generiert man den Code mit den nötigen Informationen und die VM 
   sucht sich dann die Informationen aus dem Stack.


Es ist auf jeden fall zu empfehlen helper generate Methoden zu haben welche den Code in ein ByteArray schreiben oder in eine Byte Liste (nicht das toByteArray() vergessen!)
Außerdem ist in dieser Implementation für manche Statements zusatz Information nötig wie die Adresse im ByteArray welches man aber gut mitgeben kann in dem man den "Slot" der Liste in der man die Variable speichert mitgibt
Der Generator hat ja alle Informationen durch den SemanticTraveler, das heißt die VM hat diese Informationen auch, wir müssen sie ihr nur geben und das machen wir "so".

Naja, ich glaube das war es am ende, ein wenig darauf achten in welcher reihenfolge man den Code generiert und wann etwas "lokal", "global" oder "main" ist (auf Variablen bezogen) und die Jumps verstehen und dann kann man seien eigenen kleinen Code generator bauen.


