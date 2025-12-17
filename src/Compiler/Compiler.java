package Compiler;

import Lexer.*;
import Parser.Parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;


public class Compiler
{
    private static final CodeGenerator generator = new CodeGenerator();
    public static void main(String args[]) throws IOException   //just basic mainclass
    {
        if(args.length > 1)                                     //testing if the right amount of arguments is given
        {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        }
        else if(args.length == 1)                               //yes == we run the file
        {
            runFile(args[0]);
        }
    }

    private static void runFile(String Path) throws IOException
    {
        byte[] filedata = Files.readAllBytes(Paths.get(Path));
        run(new String(filedata));
    }

    private static void run(String source)
    {
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.scanTokens(); //todo

        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        generator.generate(statements);

    }

}
