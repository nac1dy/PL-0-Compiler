package Compiler;

import Lexer.*;
import Parser.Parser;
//import TestPrinter.ASTPrinter;
import Types.Condition;
import Types.Expr;
//import Parser.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;


public class Compiler {
    private static final CodeGenerator generator = new CodeGenerator();
    static boolean hadError = false;
    static String File = "";


    public static void main(String[] args) throws IOException   //just basic mainclass
    {

        //If too many arguments were passed, we print the usage
        if (args.length > 1) {
            System.out.println("Usage: pl0 [script]");
            System.exit(64);
        }
        //If file was passed we start the runFile function
        else if (args.length == 1) {
            File = args[0];
            runFile(args[0]);
        }
        else{
            runPrompt();
        }
        //in the future we could make a runprompt function here so that if no file is passed, we can run an interactive prompt
        //for that we would need to reset the hadError variable after each line
    }
    public static String getFile() {
        return File;
    }

    //If file was passed, we call this function
    private static void runFile(String Path) throws IOException {
        //take the data from the file and pass it in a byte-array
        //this gets converted to a String and passed to run()
        byte[] filedata = Files.readAllBytes(Paths.get(Path));
        run(new String(filedata));
        if (hadError) System.exit(65); //if there was an error, exit with code 65
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (; ; ) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);
            hadError = false;
        }
    }

    private static void run(String source) {
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.lexSomeTokens();

        Parser parser = new Parser(tokens);
        Condition condition = parser.parse();

        if(hadError) return;


        //System.out.println(new ASTPrinter().print(condition));

    }

    public static void error(int line, String message) {
        //report an error on a specific line, with a message
        //where is an optional parameter that can be more specific about the error location but it needs to implemented to work
        report(line, "", message);
    }

    private static void report(int line, String where, String message) {
        //print the error message to the standard error output
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    public static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }


}