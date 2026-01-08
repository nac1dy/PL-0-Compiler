package Compiler;

import Compiler.Befehle.*;

public class Instruction
{
    public String address;
    public byte opcode;
    public Befehle befehl;

    public Instruction(String address, Befehle befehl, byte opcode)
    {
        this.address = address;
        this.befehl = befehl;
        this.opcode = opcode;
    }

    public String toString()
    {
        return address + ":  " + befehl.toHexCode() + "  " + befehl;
    }

    public String getAddress()
    {
        return this.address;
    }
    public byte getOpcode()
    {
        return this.opcode;
    }
    public Befehle getbefehl()
    {
        return this.befehl;
    }




}
