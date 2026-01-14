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