package Compiler;

public enum Befehle
{
    // Ein Argument sind die zwei Bytes, die auf den Opcode im Bytecode folgen.

    // Argumente: Adresse
    PushValueLocalVar((byte) 0x00),
    // Argumente: Adresse
    PushValueMainVar((byte) 0x01),
    // Argumente: Adresse, Prozeduren-ID
    PushValueGlobalVar((byte) 0x02),
    // Argumente: Adresse
    PushAddressLocalVar((byte) 0x03),
    // Argumente: Adresse
    PushAddressMainVar((byte) 0x04),
    // Argumente: Adresse, Prozeduren-ID
    PushAddressGlobalVar((byte) 0x05),
    // Argumente: Konstanten-ID
    PushConstant((byte) 0x06),

    // auf Stack: oben = Wert, darunter = Zieladresse
    StoreValue((byte) 0x07),
    // auf Stack: oben = Wert
    OutputValue((byte) 0x08),
    // auf Stack: oben = Zieladresse
    InputToAddr((byte) 0x09),

    // Operatoren mit 1 Faktor
    // auf Stack: oben = Wert → Ergebnis auf Stack: -Wert
    Minusify((byte) 0xA),
    // auf Stack: oben = Wert → Ergebnis auf Stack: true (1) / false (0)
    IsOdd((byte) 0xB),

    // Operatoren mit 2 Faktoren
    // auf Stack: oben = Wert 2, darunter = Wert 1 → Ergebnis auf Stack: Wert 1 + Wert 2
    OpAdd((byte) 0xC),
    // auf Stack: oben = Wert 2, darunter = Wert 1 → Ergebnis auf Stack: Wert 1 - Wert 2
    OpSubtract((byte) 0xD),
    // auf Stack: oben = Wert 2, darunter = Wert 1 → Ergebnis auf Stack: Wert 1 * Wert 2
    OpMultiply((byte) 0xE),
    // auf Stack: oben = Wert 2, darunter = Wert 1 → Ergebnis auf Stack: Wert 1 / Wert 2
    OpDivide((byte) 0xF),

    // Boolean-Logik
    // auf Stack: oben = Wert 2, darunter = Wert 1 → Ergebnis auf Stack: Wert 1 == Wert 2
    CompareEq((byte) 0x10),
    // auf Stack: oben = Wert 2, darunter = Wert 1 → Ergebnis auf Stack: Wert 1 != Wert 2
    CompareNotEq((byte) 0x11),
    // auf Stack: oben = Wert 2, darunter = Wert 1 → Ergebnis auf Stack: Wert 1 < Wert 2
    CompareLT((byte) 0x12),
    // auf Stack: oben = Wert 2, darunter = Wert 1 → Ergebnis auf Stack: Wert 1 > Wert 2
    CompareGT((byte) 0x13),
    // auf Stack: oben = Wert 2, darunter = Wert 1 → Ergebnis auf Stack: Wert 1 ≤ Wert 2
    CompareLTEq((byte) 0x14),
    // auf Stack: oben = Wert 2, darunter = Wert 1 → Ergebnis auf Stack: Wert 1 ≥ Wert 2
    CompareGTEq((byte) 0x15),

    // Flow-Logik
    // Argumente: Prozedur-ID
    CallProc((byte) 0x16),
    // keine Argumente
    ReturnProc((byte) 0x17),
    // Argumente: Jump-Offset (auch negativ möglich)
    Jump((byte) 0x18),
    // Argumente: Jump-Offset, auf Stack: oben = Wert, der false (0) sein kann
    JumpIfFalse((byte) 0x19),
    // Argumente: Länge der Prozedur in Bytes, Prozedur-ID, Bytes auf Stack benötigt für Variablen
    EntryProc((byte) 0x1A),

    /*
    // erweiterte Codes
    // Argument: Null-terminierter String
    PutString,                  //01B
    // auf Stack: oben = beliebiges Datum
    Pop,                        //01C
    // auf Stack: oben = Adresse, die durch Daten an dieser ausgetauscht werden soll
    Swap,                       //01D
    */

    // nur für VM
    EndOfCode((byte) 0x1E);

    private final byte code;

    Befehle(byte code)
    {
        this.code = code;
    }

    public String toHexCode()
    {
        return String.format("%02X", code & 0xFF);
    }

    public byte getCode()
    {
        return code;
    }




}

/*
Jeder Befehl bekommt einen Byte-Wert zugewiesen, der im Bytecode steht.
Dieser Byte-Wert kann über das Attribut "code" abgerufen werden.


 */