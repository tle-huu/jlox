package com.craftinginterpreters.lox;

import java.lang.StringBuilder;
import static com.craftinginterpreters.lox.Token.*;
import static com.craftinginterpreters.lox.TokenType.*; 


class RpnPrinter implements Expr.Visitor<String>
{
    String print(Expr expr)
    {
        return expr.accept(this);
    }

    @Override
    public String visitThisExpr(Expr.This expr) {
        return null;
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr)
    {
        StringBuilder builder = new StringBuilder();

        builder.append(expr.operator.lexeme)
               .append(" ")
               .append(print(expr.left))
               .append(" ")
               .append(print(expr.right));

        return builder.toString();
    }


    @Override
    public String visitSetExpr(Expr.Set expr) { return null;}
    
    @Override
    public String visitSuperExpr(Expr.Super expr) { return null;}


    @Override
    public String visitGetExpr(Expr.Get expr) { return null; }

    @Override
    public String visitCallExpr(Expr.Call expr) { return null; }

    @Override
    public String visitLogicalExpr(Expr.Logical expr)
    {
        return null;
    }

    @Override
    public String visitAssignExpr(Expr.Assign expr)
    {
        return null;
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr)
    {
        return null;
    }


    @Override
    public String visitGroupingExpr(Expr.Grouping expr)
    {
        return print(expr.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr)
    {
        if (expr.value == null) return "nil";

        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr)
    {
        StringBuilder builder = new StringBuilder();

        builder.append(expr.operator.lexeme)
               .append(print(expr.right));
        return builder.toString();
    }

    public static void main(String [] argv)
    {

        //    -123 * ( 45.67 )
        Expr expression = new Expr.Binary(
            new Expr.Unary(
                new Token(TokenType.MINUS, "-", null, 1),
                new Expr.Literal(123)),

            new Token(TokenType.STAR, "*", null, 1),

            new Expr.Grouping(
                new Expr.Literal(45.67)));

        Expr expression_2 = new Expr.Binary(

            new Expr.Literal(2),
            new Token(PLUS, "+", null, 1),
            new Expr.Literal(2)
            );

        Expr expression_3 = new Expr.Binary(
            new Expr.Literal("a"),
            new Token(TokenType.EQUAL_EQUAL, "==", null, 1),
            new Expr.Binary(
                    new Expr.Literal("b"),
                    new Token(TokenType.EQUAL_EQUAL, "==", null, 1),
                    new Expr.Binary(
                        new Expr.Literal("c"),
                        new Token(TokenType.EQUAL_EQUAL, "==", null, 1),
                        new Expr.Literal("d")
                        )
                    )
            );
        RpnPrinter rpn_printer = new RpnPrinter();

        String expression_string = rpn_printer.print(expression_3);

        System.out.println(expression_string);
    }
}