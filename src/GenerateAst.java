package com.craftinginterpreters.tool;


import java.util.Arrays;
import java.util.List;
import java.io.PrintWriter; 
import java.io.IOException;

public class GenerateAst
{
    public static void main(String[] argv) throws IOException
    {
        if (argv.length != 1)
        {
            System.err.println("Usage: generate_ast <output_directory>");
            System.exit(1);
        }

        String output_directory = argv[0];

        defineAst(output_directory, "Expr", Arrays.asList(
            "Super      : Token keyword, Token method",
            "This       : Token keyword",
            "Set        : Expr object, Token name, Expr value",
            "Get        : Expr object, Token name",
            "Assign     : Token name, Expr value",
            "Variable   : Token name",
            "Logical    : Expr left, Token operator, Expr right",
            "Binary     : Expr left, Token operator, Expr right",
            "Grouping   : Expr expression",
            "Literal    : Object value",
            "Unary      : Token operator, Expr right",
            "Call       : Expr callee, Token paren, List<Expr> arguments"
            )
        );
        defineAst(output_directory, "Stmt", Arrays.asList(
            "Class      : Token name, Expr.Variable superclass, List<Stmt.Function> methods, List<Stmt> fields",
            "Return     : Token keyword, Expr value",
            "Function   : Token name, List<Token> parameters, List<Stmt> body",
            "Block      : List<Stmt> statements",
            "If         : Expr condition, Stmt thenBranch, Stmt elseBranch",
            "While      : Expr condition, Stmt body",
            "Var        : Token token, Expr initializer",
            "Expression : Expr expression",
            "Print      : Expr expression"
            )
        );
    }


    private static void defineAst(String output_directory, String base_name, List<String> types) throws IOException
    {
        String path = output_directory + "/" + base_name + ".java";
        PrintWriter writer = new PrintWriter(path, "UTF-8");

        writer.println("/*********************************/");
        writer.println("/*                               */");
        writer.println("/*                               */");
        writer.println("/*      AUTO GENERATED FILE      */");
        writer.println("/*      DO NOT MODIFY  !!!       */");
        writer.println("/*                               */");
        writer.println("/*                               */");
        writer.println("/*********************************/");
        writer.println();

        writer.println("package com.craftinginterpreters.lox;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println("import static com.craftinginterpreters.lox.Token.*;");
        writer.println();


        writer.println("abstract class " + base_name);
        writer.println("{");
        defineVisitor(writer, base_name, types);


        for (String type : types)
        {
            String class_name = type.split(":")[0].trim();         
            String fields = type.split(":")[1].trim(); 
            defineType(writer, base_name, class_name, fields);
        }

        writer.println();
        writer.println("\tabstract <R> R accept(Visitor<R> visitor);");


        writer.println("}"); // end class
        writer.close();
    }


    private static void defineType(PrintWriter writer, String base_name, String class_name, String fields_string)
    {

        writer.println("\tstatic class " + class_name + " extends " + base_name);
        writer.println("\t{");

        String[] fields = fields_string.split(", ");

        // Constructor
        writer.println("\t\t" + class_name + "(" + fields_string + ")");
        writer.println("\t\t{");

        for (String field : fields)
        {
            String name = field.split(" ")[1].trim();
            writer.println("\t\t\tthis." + name + " = " + name + ";");
        }
        writer.println("\t\t}"); // end constructor
        writer.println();

        // Private variables
        for (String field : fields)
        {
            writer.println("\t\t" + field + ";");
        }

        writer.println();
        writer.println("\t\t<R> R accept(Visitor<R> visitor)");
        writer.println("\t\t{");
        writer.println("\t\t\treturn visitor.visit" + class_name + base_name + "(this);");
        writer.println("\t\t}");

        writer.println("\t}"); // end class
        writer.println();
    }

    private static void defineVisitor(PrintWriter writer, String base_name, List<String> types)
    {
        writer.println("\tinterface Visitor<R>");
        writer.println("\t{");

        for (String type : types)
        {
            String type_name = type.split(":")[0].trim();
            writer.println("\t\tR visit" + type_name + base_name + "(" + type_name + " " + base_name.toLowerCase() + ");");      
        }

        writer.println("\t}"); // end interface
        writer.println();
  }
}