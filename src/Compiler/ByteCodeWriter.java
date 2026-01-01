package Compiler;

/*
The Idea i have with this class is that in the CodeGenerator class, i will call methods from this class.
Because i already have implemented the visitor pattern, i know in which node i currently am, so i can call the corresponding method from this class.
The Byte code will then be printed in an file which i can later run on a virtual machine.

the layout will look like this:

<Hexvalue> : <adress> <bytecode instruction>

for Example:

0000: 1A EntryProc
0007: 03 PushAdrVarLocal
000A: 06 PushConst
000D: 07 StoreVal


FORMAT: LITTLE ENDIAN!

Ich brauche noch einen Proccounter, einen Varcounter.



*/

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import Compiler.Befehle.*;
import Types.Condition;
import Types.Expr;

public class ByteCodeWriter
{
    String path = Compiler.getFile();
    PrintWriter writer = null;

    public ByteCodeWriter()
    {
        try {
            writer = new PrintWriter(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("Error: Could not create bytecode file.");
            e.printStackTrace();
        }
    }



    public void writeHeader()
    {
        writer.println("Anzahl der Prozeduren auf 2 Byte");                 //Aktuell noch nicht möglich da der Parser noch keine Proceduren erkennt
        writer.println("Adressraumgröße auf 2 Byte (04 00)");               //Aktuell noch nicht möglich da der Parser noch keine Variablen erkennt
        writer.println("");
    }



    //                                              HELPER METHODS                                                                                                //
    //------------------------------------------------------------------------------------------------------------------------------------------------------------//


    public void writeBinaryExpr(Expr expr)
    {
        /*

         */
    }

    public void writeGroupingExpr(Expr expr)
    {

    }

    public void writeLiteralExpr(Expr expr)
    {

    }

    public void writeUnaryExpr(Expr expr)
    {

    }

    public void writeVariableExpr(Expr expr)
    {

    }

    public void writeUnaryCondition(Condition cond)
    {

    }

    public void writeBinaryCondition(Condition cond)
    {

    }

    public void bigToLittle(int value)
    {
        //Wandelt einen Integer Wert von Big Endian in Little Endian um
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------------//

    public void writeEnd()
    {

        //Konstanten in 4 Byte schrieben
        /*
        Const opcode: 0x00000007 (Konstante 7)
         */
    }
}
