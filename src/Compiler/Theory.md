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