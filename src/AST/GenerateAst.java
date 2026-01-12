package AST;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.function.BinaryOperator;


public class GenerateAst
{
    public static void main(String[] args) throws IOException
    {
        if(args.length != 1)
        {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }
        String outputDir = args[0];
        defineAst(outputDir, "Expr", Arrays.asList(
                "Binary  : Expr left, Token operator, Expr right",
                "Grouping: Expr expression",
                "Literal : Object value",
                "Unary   : Token operator, Expr right",
                "Variable: Token name"

        ));
        //TODO
        defineAst(outputDir, "Stmt", Arrays.asList(
                "OutputStmt      : Expr expression",
                "IfStmt         : Condition condition, Stmt thenBranch",
                "WhileStmt      : Condition condition, Stmt body",
                "RepeatUntilStmt  : Stmt body, Condition condition",
                "InputStmt      : Token name",
                "AssignStmt     : Token name, Expr value",
                "CallStmt       : Token name",
                "BeginEndStmt   : List<Stmt> statements"


        ));
        defineAst(outputDir, "Condition", Arrays.asList(
                "UnaryCondition : Token operator, Expr expression",
                "BinaryCondition: Expr left, Token operator, Expr right"
        ));


    }

    private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException
    {
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, "UTF-8");

        writer.println("package Types;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("import Lexer.Token;");
        writer.println();
        writer.println("public abstract class " + baseName + '\n' + "{");

        defineVisitor(writer, baseName, types);

        writer.println();

        for(String type : types)
        {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);
        }
        writer.println();
        writer.println(" public abstract <R> R accept(Visitor<R> visitor);");
        writer.println("}");
        writer.close();
    }
    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types)
    {
        writer.println("    public interface Visitor<R>" + '\n' + "{");

        for(String type : types)
        {
            String typeName = type.split(":")[0].trim();
            writer.println("    R visit" + typeName + baseName + "(" + typeName + " " + baseName.toLowerCase() + ");");
        }
        writer.println(" }");
    }
    private static void defineType(PrintWriter writer, String baseName, String className, String fieldList)
    {
        writer.println("    public static class " + className + " extends " + baseName + '\n' + " {");

        writer.println("    public " + className + "(" + fieldList + ")" + '\n' + "{");

        String[] fields = fieldList.split(", ");
        for(String field : fields)
        {
            String name = field.split(" ")[1];
            writer.println("    this." + name + " = " + name + ";");
        }
        writer.println("    }");

        writer.println();
        writer.println("    @Override");
        writer.println("    public <R> R accept(Visitor<R> visitor)" + '\n' + "{");
        writer.println("    return visitor.visit" + className + baseName + "(this);");
        writer.println("    }");

        writer.println();
        for(String field : fields)
        {
            writer.println("    public final " + field + ";");
        }

        writer.println(" }");
    }
}
