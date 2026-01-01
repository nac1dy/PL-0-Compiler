package Compiler;

public enum Befehle
{
    // Ein Argument sind die zwei Bytes, die auf den Opcode im Bytecode folgen.

    // Argumente: Adresse
    PushValueLocalVar,          //00
    // Argumente: Adresse
    PushValueMainVar,           //01
    // Argumente: Adresse, Prozeduren-ID
    PushValueGlobalVar,         //02
    // Argumente: Adresse
    PushAddressLocalVar,        //03
    // Argumente: Adresse
    PushAddressMainVar,         //04
    // Argumente: Adresse, Prozeduren-ID
    PushAddressGlobalVar,       //05
    // Argumente: Konstanten-ID
    PushConstant,               //06

    // auf Stack: oben = Wert, darunter = Zieladresse
    StoreValue,                 //07
    // auf Stack: oben = Wert
    OutputValue,                //08
    // auf Stack: oben = Zieladresse
    InputToAddr,                //09

    // Operatoren mit 1 Faktor
    // auf Stack: oben = Wert → Ergebnis auf Stack: -Wert
    Minusify,                   //0A
    // auf Stack: oben = Wert → Ergebnis auf Stack: true (1) / false (0)
    IsOdd,                      //0B

    // Operatoren mit 2 Faktoren
    // auf Stack: oben = Wert 2, darunter = Wert 1 → Ergebnis auf Stack: Wert 1 + Wert 2
    OpAdd,                      //0C
    // auf Stack: oben = Wert 2, darunter = Wert 1 → Ergebnis auf Stack: Wert 1 - Wert 2
    OpSubtract,                 //0D
    // auf Stack: oben = Wert 2, darunter = Wert 1 → Ergebnis auf Stack: Wert 1 * Wert 2
    OpMultiply,                 //0E
    // auf Stack: oben = Wert 2, darunter = Wert 1 → Ergebnis auf Stack: Wert 1 / Wert 2
    OpDivide,                   //0F

    // Boolean-Logik
    // auf Stack: oben = Wert 2, darunter = Wert 1 → Ergebnis auf Stack: Wert 1 == Wert 2
    CompareEq,                  //010
    // auf Stack: oben = Wert 2, darunter = Wert 1 → Ergebnis auf Stack: Wert 1 != Wert 2
    CompareNotEq,               //011
    // auf Stack: oben = Wert 2, darunter = Wert 1 → Ergebnis auf Stack: Wert 1 < Wert 2
    CompareLT,                  //012
    // auf Stack: oben = Wert 2, darunter = Wert 1 → Ergebnis auf Stack: Wert 1 > Wert 2
    CompareGT,                  //013
    // auf Stack: oben = Wert 2, darunter = Wert 1 → Ergebnis auf Stack: Wert 1 ≤ Wert 2
    CompareLTEq,                //014
    // auf Stack: oben = Wert 2, darunter = Wert 1 → Ergebnis auf Stack: Wert 1 ≥ Wert 2
    CompareGTEq,                //0x15

    // Flow-Logik
    // Argumente: Prozedur-ID
    CallProc,                   //016
    // keine Argumente
    ReturnProc,                 //017
    // Argumente: Jump-Offset (auch negativ möglich)
    Jump,                       //018
    // Argumente: Jump-Offset, auf Stack: oben = Wert, der false (0) sein kann
    JumpIfFalse,                //019
    // Argumente: Länge der Prozedur in Bytes, Prozedur-ID, Bytes auf Stack benötigt für Variablen
    EntryProc,                  //01A

    // erweiterte Codes
    // Argument: Null-terminierter String
    PutString,                  //01B
    // auf Stack: oben = beliebiges Datum
    Pop,                        //01C
    // auf Stack: oben = Adresse, die durch Daten an dieser ausgetauscht werden soll
    Swap,                       //01D

    // nur für VM
    EndOfCode,                  //01E,

}

